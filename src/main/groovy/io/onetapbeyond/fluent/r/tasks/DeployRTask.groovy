/*
 * Copyright 2015 David Russell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.onetapbeyond.fluent.r.tasks

import io.onetapbeyond.fluent.r.FluentResult
import io.onetapbeyond.fluent.r.FluentSyntaxException
import io.onetapbeyond.fluent.r.FluentServerException
import io.onetapbeyond.fluent.r.dsl.DeployR
import io.onetapbeyond.fluent.r.nouns.FileVocab
import io.onetapbeyond.fluent.r.nouns.RuntimeVocab
import io.onetapbeyond.fluent.r.nouns.SessionVocab
import io.onetapbeyond.fluent.r.results.*
import io.onetapbeyond.fluent.r.verbs.DeployRVerbs

import com.revo.deployr.client.*
import com.revo.deployr.client.auth.RAuthentication
import com.revo.deployr.client.auth.basic.RBasicAuthentication
import com.revo.deployr.client.broker.*
import com.revo.deployr.client.broker.config.*
import com.revo.deployr.client.broker.engine.*
import com.revo.deployr.client.broker.options.*
import com.revo.deployr.client.data.RData
import com.revo.deployr.client.factory.*
import com.revo.deployr.client.params.*

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.syntax.SyntaxException

import java.nio.file.Path

/**
 * DeployRTask supports the execution of the DeployR Fluent R DSL.
 *
 * For a complete description of working with DeployRTask and the
 * DeployR Fluent R DSL visit the Fluent R project home page
 * on <a href="https://github.com/onetapbeyond/fluent-r/">github</a>.
 */
class DeployRTask extends FluentTask {

    /*
     * Task Configuration Options
     *
     * String endpoint  : DeployR Server Endpoint
     * String host      : DeployR Server Host
     * int port         : DeployR Server Port
     * boolean https    : DeployR Server 
     * String username  : DeployR Server Account Username
     * String password  : DeployR Server Account Password
     * RBroker broker   : RBroker Task Runtime
     * RProject project : RProject Task Runtime
     */
    public Map config = [:]

    // Task Internal Runtime Controls
    private RBroker liveBroker
    private boolean fetchPlots = false

    public DeployRTask() {

        /*
         * Build GroovyShell Runtime Configuration for task.
         */
        imports.addStaticStars(FileVocab.name,
                SessionVocab.name,
                RuntimeVocab.name)
        def secure = new SecureASTCustomizer()
        secure.starImportsBlacklist = ["java.lang.System"]
        secure.indirectImportCheckEnabled = true
        compilerConfig.addCompilationCustomizers(imports, secure)
    }

    /**
     * Send input data as a csv string on task execution.
     * <p>
     * A csv string has the format "name,value". When there
     * are two or more inputs simply use the "," character to 
     * separate the inputs, for example: "n,10,mean,5".
     */
    public DeployRTask send(String data) {
        this.send = data
        this
    }

    /**
     * Send either a single input data value as a DeployR-encoded 
     * {@link com.revo.deployr.client.data.RData} or 
     * send two or more input data values as a list of 
     * {@link com.revo.deployr.client.data.RData} on task execution.
     * <p>
     * In order to create encodings for common data types use the 
     * {@link io.onetapbeyond.fluent.r.encoding.DeployRDataFactory}.
     * For the complete set of supported data type encodings
     * use the {@link com.revo.deployr.client.factory.RDataFactory}.
     */
    public DeployRTask send(Object data) {
        this.send = data
        this
    }

    /**
     * Execute Fluent R DSL on task and return results.
     */
    public FluentResult execute() {

        def fluentResult
        def internalBroker = false

        try {

            def binding = new Binding([
                    R: new DeployR()
            ])

            def shell = new GroovyShell(this.class.classLoader,
                    binding, compilerConfig)

            /*
             * Evaluate the Fluent R DSL for the current task.
             */
            shell.evaluate(stream)
            /*
             * Parse evaluated Fluent R DSL outputs to gather
             * building blocks in preparation for constructing
             * corresponding DeployR API calls on task.
             */
            def parsedDSL = parseDSL(binding.R)

            if (config.project) {

                /*
                 * Execution on explicit, external RProject.
                 */
                def execOptions = buildCallOptions(parsedDSL, config.project)

                try {

                    def execResult = config.project.executeScript(
                        parsedDSL.executionChain*.filename.join(','),
                        parsedDSL.executionChain*.directory.join(','),
                        parsedDSL.executionChain*.author.join(','),
                        null,
                        execOptions)

                    /*
                     * Build FluentResult from data returned on 
                     * successful RProjectExecution.
                     */

                    def execConsole = execResult.about().console
                    def execPlots =
                        execResult.about().results?.collect { result ->
                            result.about().url
                        }
                    def execFiles =
                        execResult.about().artifacts?.collect { artifact ->
                            artifact.about().url
                        }
                    Map<String, Object> execObjs = [:]
                    execResult.about().workspaceObjects.each { rData ->
                        execObjs.put(rData.name, rData)
                    }

                    fluentResult = new TaskResult([
                        console : execConsole,
                        plots   : execPlots,
                        files   : execFiles,
                        objects : execObjs
                    ])

                } catch(Exception dex) {

                    fluentResult = new TaskResult([
                        success: false,
                        error  : dex.message,
                        errorCause  : new FluentServerException()
                    ])
                }

            } else {

                liveBroker = config.broker

                if(!liveBroker) {
                    /*
                     * Execution on implicit, internal RBroker.
                     */
                    liveBroker = buildInternalBroker(binding.R)
                    internalBroker = true
                }

                def task = buildTask(parsedDSL)

                def token = liveBroker.submit(task)
                def taskResult = token.result

                Map<String, Object> requestedObjs = [:]
                taskResult.generatedObjects.each { rObj ->
                    requestedObjs.put(rObj.name, rObj)
                }

                if (taskResult.isSuccess()) {

                    fluentResult = new TaskResult([
                        console  : taskResult.generatedConsole,
                        plots    : fetchPlots ? taskResult.generatedPlots :
                                   null,
                        files    : taskResult.generatedFiles,
                        objects  : requestedObjs
                    ])

                } else {

                    fluentResult = new TaskResult([
                        success: false,
                        error  : taskResult.failure.message,
                        errorCause  : new FluentServerException()
                    ])
                }

            }

        } catch (Exception ex) {

            def error = ex.message
            def errorCause = ex

            if(ex instanceof MissingMethodException) {
                error = HELP
                errorCause = new FluentSyntaxException()
            }

            fluentResult = new TaskResult([
                success: false,
                error  : error,
                errorCause  : errorCause
            ])

        } finally {

            if (internalBroker) {
                try {
                    liveBroker.shutdown()
                } catch (Exception shex) {
                }
            }
        }

        /*
         * Return FluentTask execution result.
         */
        fluentResult
    }

    /*
     * DeployRTask private utility methods.
     */

    private RBroker buildInternalBroker(DeployR dsl) {

        def internalEndpoint = config.endpoint

        if(!internalEndpoint) {

            internalEndpoint =
                new StringBuffer(config.https ? "https" : "http")
                                .append("://")
                                .append(config.host)
                                .append(":")
                                .append(config.port)
                                .append("/deployr")
                                .toString()
        }

        def authToken
        if (config.username && config.password) {
            authToken = new RBasicAuthentication(config.username,
                                                 config.password)
        }

        DiscreteBrokerConfig brokerConfig =
                buildInternalBrokerConfig(internalEndpoint, authToken)
        brokerConfig.allowSelfSignedSSLCert = allowSelfSignedCert

        def iBroker = RBrokerFactory.discreteTaskBroker(brokerConfig);
        iBroker
    }

    private RBrokerConfig buildInternalBrokerConfig(String endpoint,
                                        RAuthentication authToken) {

        /*
         * Config only built if internalBroker. Note, all
         * "load" artifacts are applied directly on TaskOptions.
         */

        DiscreteBrokerConfig brokerConfig =
                new DiscreteBrokerConfig(endpoint, authToken);

        brokerConfig
    }

    private RTask buildTask(Map parsedDSL) {

        def task

        def taskOptions = buildCallOptions(parsedDSL, liveBroker)
        fetchPlots = RuntimeVocab.plots in parsedDSL.fetchRuntime

        if (liveBroker instanceof PooledTaskBroker) {

            task = RTaskFactory.pooledTask(
                    parsedDSL.executionChain*.filename.join(','),
                    parsedDSL.executionChain*.directory.join(','),
                    parsedDSL.executionChain*.author.join(','),
                    null,
                    taskOptions)

        } else
        if (liveBroker instanceof DiscreteTaskBroker) {

            task = RTaskFactory.discreteTask(
                    parsedDSL.executionChain*.filename.join(','),
                    parsedDSL.executionChain*.directory.join(','),
                    parsedDSL.executionChain*.author.join(','),
                    null,
                    taskOptions)
        }

        task
    }

    private Object buildCallOptions(Map parsedDSL, Object execEnv) {

        def execOptions
        def wdOpts, wsOpts

        if(execEnv instanceof DiscreteTaskBroker) {
            execOptions = new DiscreteTaskOptions()
            wdOpts = new TaskPreloadOptions()
            wsOpts = new TaskPreloadOptions()
        } else
        if(execEnv instanceof PooledTaskBroker) {
            execOptions = new PooledTaskOptions()
            wdOpts = new TaskPreloadOptions()
            wsOpts = new TaskPreloadOptions()
        } else
        if(execEnv instanceof RProject) {
            execOptions = new ProjectExecutionOptions()
            wdOpts = new ProjectPreloadOptions()
            wsOpts = new ProjectPreloadOptions()
        }

        if (parsedDSL.preloadWd?.size()) {
            wdOpts.filename = parsedDSL.preloadWd*.filename.join(',')
            wdOpts.directory = parsedDSL.preloadWd*.directory.join(',')
            wdOpts.author = parsedDSL.preloadWd*.author.join(',')
            execOptions.preloadDirectory = wdOpts
        }

        execOptions.preloadByDirectory =
                parsedDSL.preloadByDirectory*.join(',')

        if (parsedDSL.preloadWorkspace?.size()) {
            wsOpts.filename = parsedDSL.preloadWorkspace*.filename.join(',')
            wsOpts.directory = parsedDSL.preloadWorkspace*.directory.join(',')
            wsOpts.author = parsedDSL.preloadWorkspace*.author.join(',')
            execOptions.preloadWorkspace = wsOpts
        }

        if(send instanceof String)
            execOptions.csvrinputs = send
        else
        if(send instanceof RData)
            execOptions.rinputs = Arrays.asList(send)
        else
        if(send instanceof List<RData>)
            execOptions.rinputs = send
                
        execOptions.routputs = parsedDSL.fetchWorkspace

        if (parsedDSL.fetchWd?.size()) {
            execOptions.artifactsoff = false
        } else {
            execOptions.artifactsoff = true
        }

        execOptions.consoleoff =
            !(RuntimeVocab.console in parsedDSL.fetchRuntime)

        execOptions
    }

    private Map parseDSL(DeployR dsl) {

        def preloadWd = []
        def preloadByDirectory = []
        def preloadWorkspace = []
        def executionChain = []
        def fetchWd = []
        def fetchWorkspace = []

        // Parse Fluent R "load" Requests
        dsl.loadables.each { loadable ->

            switch (loadable.environment) {

                case SessionVocab.wd:
                case SessionVocab.directory:

                    if (loadable.artifacts == FileVocab.all) {
                        preloadByDirectory.add(loadable.directory ?: ROOT)
                    } else {
                        loadable.artifacts.each { filename ->
                            preloadWd.add([
                            filename : filename,
                            directory: loadable.directory ?: ROOT,
                            author   : loadable.author ?: config.username
                            ])
                        }
                    }

                    break

                case SessionVocab.workspace:

                    if (loadable.artifacts == FileVocab.all) {
                        throw new SyntaxException("Fluent R does not " +
                            "support the (all) syntax when loading " +
                            "into a workspace.", 0, 0)
                    } else {
                        loadable.artifacts.each { filename ->
                            preloadWorkspace.add([
                            filename : filename,
                            directory: loadable.directory ?: ROOT,
                            author   : loadable.author ?: config.username
                            ])
                        }
                    }
                    break

                default:
                    break
            }

        }

        // Parse Fluent R "execute" Requests
        dsl.executables.each { executable ->

            executable.artifacts.each { filename ->
                executionChain.add([
                    filename : filename,
                    directory: executable.directory ?: ROOT,
                    author   : executable.author ?: config.username
                ])
            }
        }

        // Parse Fluent R "fetch" Requests
        dsl.fetchables.each { fetchable ->

            switch (fetchable.environment) {

                case SessionVocab.wd:
                case SessionVocab.directory:

                    fetchable.artifacts.each { filename ->
                        fetchWd.add(filename)
                    }
                    break

                case SessionVocab.workspace:

                    fetchable.artifacts.each { objectname ->
                        fetchWorkspace.add(objectname)
                    }
                    break

                default:
                    break
            }

        }
        def fetchRuntime = dsl.fetchRuntime

        return [preloadWd         : preloadWd,
                preloadByDirectory: preloadByDirectory,
                preloadWorkspace  : preloadWorkspace,
                executionChain    : executionChain,
                fetchWd           : fetchWd,
                fetchWorkspace    : fetchWorkspace,
                fetchRuntime      : fetchRuntime]
    }

    // GroovyShell Configuration
    private static CompilerConfiguration compilerConfig =
            new CompilerConfiguration([scriptBaseClass: DeployRVerbs.name])
    private static ImportCustomizer imports = new ImportCustomizer()

    // DeployR Repository Root Directory
    private static final String ROOT = "root"

    private static final String HELP = """
Fluent R invalid grammar detected. Use:

load A[,B,C] into wd | workspace from DIRECTORY by AUTHOR
execute M[,N,O] from DIRECTORY by AUTHOR
fetch console | plots
fetch X[,Y,Z] from wd | workspace
"""
}