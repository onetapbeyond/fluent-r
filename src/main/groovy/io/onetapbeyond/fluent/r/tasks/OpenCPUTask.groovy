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
import io.onetapbeyond.fluent.r.dsl.OpenCPU
import io.onetapbeyond.fluent.r.nouns.FileVocab
import io.onetapbeyond.fluent.r.nouns.RepoVocab
import io.onetapbeyond.fluent.r.nouns.RuntimeVocab
import io.onetapbeyond.fluent.r.nouns.SessionVocab
import io.onetapbeyond.fluent.r.results.*
import io.onetapbeyond.fluent.r.verbs.OpenCPUVerbs

import com.google.gson.Gson
import groovy.text.SimpleTemplateEngine
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

import java.nio.file.Path

/**
 * OpenCPUTask supports the execution of the OpenCPU Fluent R DSL.
 *
 * For a complete description of working with OpenCPUTask and the
 *  OpenCPU Fluent R DSL visit the Fluent R project home page
 * on <a href="https://github.com/onetapbeyond/fluent-r/">github</a>.
 */
class OpenCPUTask extends FluentTask {

    /*
     * Task Configuration Options
     *
     * String endpoint  : OpenCPU Server Endpoint
     */
    public Map config = [:]

    // Task Internal Runtime Flags
    private boolean fetchPlots = false

    /**
     * Use {@link io.onetapbeyond.fluent.r.OpenCPUTaskBuilder} to create
     * instances of OpenCPUTask.
     */
    public OpenCPUTask() {

        /*
         * Build GroovyShell Runtime Configuration for task.
         */
        imports.addStaticStars(FileVocab.name,
                SessionVocab.name,
                RuntimeVocab.name,
                RepoVocab.name)
        def secure = new SecureASTCustomizer()
        secure.starImportsBlacklist = ["java.lang.System"]
        secure.indirectImportCheckEnabled = true
        compilerConfig.addCompilationCustomizers(imports, secure)
    }

    /**
     * Send input data as a query string on task execution.
     * <p>
     * A query string has the format "name=value". When there
     * are two or more inputs use the &#38; character to 
     * separate the inputs, for example: "n=10&#38;mean=5".
     */
    public OpenCPUTask send(String data) {
        this.send = data
        this
    }

    /**
     * Send input data as JSON represented as Map on task execution.
     * <p>
     * Here is an example of simple JSON and it's corresponding Map
     * representation:
     * <p>
     * { "n" : 10, "mean" :5 } = [ "n" : 10, "mean" : 5 ]
     */
    public OpenCPUTask send(Object data) {
        this.send = data
        this
    }

    /**
     * Execute Fluent R DSL on task and return results.
     */
    public FluentResult execute() {

        def fluentResult

        try {

            def binding = new Binding([
                    R: new OpenCPU()
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
             * corresponding OpenCPU API calls on task.
             */
            def parsedDSL = parseDSL(binding.R)

            /*
             * Build OpenCPU API call endpoints based on the
             * building blocks prepared on parseDSL().
             */
            def callEndpoints = buildCallEndpoints(parsedDSL)

            def execChainFiles = []
            def execChainPlots = []
            def execChainObjs = [:]
            def execChainConsole = ''

            callEndpoints?.each { callOnCPU ->

                /*
                 * Execute item on execution chain and retrieve
                 * outputs requested by Fluent R DSL on task.
                 */
                def execResult = executeOnServer(callOnCPU, parsedDSL)

                execChainFiles += execResult.execFiles
                execChainPlots += execResult.execPlots
                execChainObjs += execResult.execObjs
                execChainConsole += execResult.execConsole
            }

            /*
             * Build FluentResult on task execution success.
             */
            fluentResult = new TaskResult([
                    console  : execChainConsole,
                    plots    : execChainPlots,
                    files       : execChainFiles,
                    objects: execChainObjs
            ])

        } catch (Exception ex) {

            def error = ex.message
            def errorCause = ex

            if(ex instanceof groovyx.net.http.HttpResponseException) {
                try {
                    /*
                     * Extract server generated error message.
                     */
                    error = ex.response.data.text
                } catch(Exception rex) {
                    error = "Call rejected by OpenCPU server."
                } finally {
                    errorCause = new FluentServerException()
                }
            } else
            if(ex instanceof javax.net.ssl.SSLHandshakeException) {
                error = ex.cause ? ex.cause.message : error
                errorCause = new FluentServerException()
            }
            if(ex instanceof MissingMethodException) {
                error = HELP
                errorCause = new FluentSyntaxException()
            }

            /*
             * Build FluentResult on task execution failure.
             */
            fluentResult = new TaskResult([
                    success: false,
                    error  : error,
                    errorCause  : errorCause
            ])

        }

        /*
         * Return FluentTask execution result.
         */
        fluentResult

    }

    /*
     * OpenCPUTask private utility methods.
     */

    private List<String> buildCallEndpoints(Map parsedDSL) {

        def callEndpoints = []

        try {

            parsedDSL.executionChain.each { execution ->

                def template

                /*
                 * Determine if Fluent R DSL "execute" request
                 * on task is for "function" or "script".
                 */
                execution.exectype =
                        isScript(execution.function) ? SCRIPT : FUNCTION

                /*
                 * Determine TEMPLATE for building OpenCPU API endpoint 
                 * based on Fluent R DSL "execute" request on task.
                 */
                switch (execution.repo) {

                    case RepoVocab.opencpu:
                        if (execution.user == EXEC_USER_UNDEFINED)
                            template = OCPU_EXEC_TEMPLATE
                        else
                            template = OCPU_PRIVATE_EXEC_TEMPLATE
                        break

                    case RepoVocab.cran:
                        template = CRAN_EXEC_TEMPLATE
                        break

                    case RepoVocab.github:
                        template = GITHUB_EXEC_TEMPLATE
                        break

                    case RepoVocab.gist:
                        template = GIST_EXEC_TEMPLATE
                        break

                }

                /*
                 * Map RepoVocab.opencpu to "library" per
                 * OpenCPU API endpoint policy.
                 */
                if (execution.repo == RepoVocab.opencpu)
                    execution.repo = OCPU_SERVER_REPO

                /*
                 * Generate OpenCPU API call endpoint based on 
                 * TEMPLATE corresponding Fluent R DSL "execute" request.
                 */
                def execCall = engine.createTemplate(template)
                                     .make(execution).toString()

                callEndpoints << execCall.toString()
            }

        } catch (Exception bex) {

            /*
             * Allow failures during build to propogate
             * up to execute() for handling, reporting.
             */
            throw bex
        }

        callEndpoints
    }

    private Map executeOnServer(String apiCall, Map parsedDSL) {

        def execFiles = []
        def execPlots = []
        def execObjs = [:]
        def execConsole

        try {

            def funcOnCall = apiCall[apiCall.lastIndexOf("/")+1..-1]

            def cpuConn = new RESTClient(config.endpoint)
            secureConnection(cpuConn)

            String data = null
            String contentType = JSON_CONTENT_TYPE
            if (send) {
                if (send instanceof String) {
                    data = send as String
                    contentType = URLENC_CONTENT_TYPE
                } else if (send instanceof Map) {
                    data = gson.toJson(send)
                }
                send = null
            }

            /*
             * Exceptions on cpuConn.post fall through and are 
             * propagated up to execute() for reporting.
             */
            cpuConn.post(path: apiCall,
                    body: data,
                    requestContentType: contentType) { resp, execData ->

                if (resp.status == 201) { // POST OK

                    def ocpuLocation =
                            resp.headers[OCPU_HEADER_LOCATION].value

                    execFiles = fetchRequestedFiles(parsedDSL, ocpuLocation)
                    execObjs = fetchRequestedObjects(parsedDSL,
                                                    ocpuLocation, funcOnCall)
                    execConsole = fetchRequestedConsole(parsedDSL, ocpuLocation)
                    execPlots = fetchRequestedPlots(parsedDSL, ocpuLocation)

                }
            }

        } catch (Exception cex) {
            /*
             * Exceptions are propagated up to execute() for reporting.
             */ 
            throw cex
        }

        [
                execFiles  : execFiles,
                execPlots  : execPlots,
                execObjs   : execObjs,
                execConsole: execConsole
        ]

    }

    private List<URL> fetchRequestedFiles(Map parsedDSL,
                                          String ocpuLocation) {

        List<URL> requestedFiles = []

        parsedDSL?.fetchWd.each { filename ->
            def fileMap = ["location": ocpuLocation,
                           "filename": filename]
            def filePath =
                    engine.createTemplate(OCPU_DIRECTORY_FILE_TEMPLATE)
                            .make(fileMap).toString()

            /*
             * Capture working directory file fetched on execution.
             */
            requestedFiles << new URL(filePath)
        }

        requestedFiles
    }

    private Map fetchRequestedObjects(Map parsedDSL,
                                      String ocpuLocation,
                                      String funcOnCall) {

        Map requestedObjects = [:]

        /*
         * Handle implicit request for CPU_DEFAULT_OBJECT object
         * for each task on the execution chain.
         */
        parsedDSL?.fetchWorkspace.add(OCPU_DEFAULT_OBJECT)

        parsedDSL?.fetchWorkspace.each { objname ->

            try {

                def objMap = ["objname": objname]

                def objEndpoint =
                        engine.createTemplate(OCPU_WORKSPACE_OBJ_TEMPLATE)
                                .make(objMap).toString()

                def objConn = new RESTClient(ocpuLocation)
                secureConnection(objConn)

                /*
                 * Exceptions on objConn.get fall through and are 
                 * propagated up to execute() for reporting.
                 */
                objConn.get(path: objEndpoint) { resp, objData ->

                    if (resp.status == 200) { // GET OK

                        /*
                         * Skip implicit CPU_DEFAULT_OBJECT if null.
                         */
                        if(objData) {
                            if(objMap.objname.equals(OCPU_DEFAULT_OBJECT)) {
                                requestedObjects.put(funcOnCall, objData)
                            } else {
                                requestedObjects.put(objMap.objname, objData)
                            }
                        } 

                    } else { // General error.

                        def errGen = "Object fetch error code: ${resp.status}"
                        throw new Exception(errGen)
                    }

                }

            } catch (Exception wex) {
                /*
                 * Swallow exception as requested objects may
                 * (legitimately) not be availble on the current
                 * execution within the task execution chain.
                 */
            }
        }

        requestedObjects
    }

    private String fetchRequestedConsole(Map parsedDSL,
                                         String ocpuLocation) {

        String requestedConsole

        if (RuntimeVocab.console in parsedDSL.fetchRuntime) {
            def consoleEndpoint =
                    engine.createTemplate(OCPU_CONSOLE_TEMPLATE)
                          .make().toString()

            def consoleConn = new RESTClient(ocpuLocation)
            secureConnection(consoleConn)

            /*
             * Exceptions on consoleConn.get fall through and are 
             * propagated up to execute() for reporting.
             */
            consoleConn.get(path: consoleEndpoint) { resp, conData ->

                if (resp.status == 200) { // GET OK

                    /*
                     * Capture session console output fetched on execution.
                     */
                    requestedConsole = conData.text

                } else { // General error.

                    def errGen = "Console fetch error code: ${resp.status}"
                    throw new Exception(errGen)

                }
            }
        }

        requestedConsole
    }

    private List<URL> fetchRequestedPlots(Map parsedDSL,
                                          String ocpuLocation) {

        List<URL> requestedPlots = []

        if (RuntimeVocab.plots in parsedDSL.fetchRuntime) {

            boolean fetchingPlots = true

            def plotConn = new RESTClient(ocpuLocation)
            secureConnection(plotConn)
            def plotEndpoint = ocpuLocation + OCPU_GRAPHICS

            /*
             * Exceptions on plotConn.get fall through and are 
             * propagated up to execute() for reporting.
             */
            plotConn.get(path: plotEndpoint) { resp, plotData ->

                def plotCount = 0

                if (resp.status == 200) { // GET OK

                    plotData.eachLine { plotLine ->
                        /*
                         * Exclude "last" as it's a duplicate.
                         */
                        if(plotLine && plotLine != "last")
                            plotCount++
                    }

                }

                /*
                 * Build URL for each plot reported following task execution.
                 */
                plotCount.times { plotIndex ->

                    def plotMap = ["location": ocpuLocation,
                                   "plotIndex": plotIndex+1]
                    def plotPath = engine.createTemplate(OCPU_GRAPHICS_TEMPLATE)
                                         .make(plotMap).toString()

                    /*
                     * Capture graphics generated plot fetched on execution.
                     */
                    requestedPlots << new URL(plotPath)
                }
            }
        }

        requestedPlots
    }

    private Map parseDSL(OpenCPU dsl) {

        def executionChain = []
        def fetchWd = []
        def fetchWorkspace = []

        // Parse Fluent R "execute" Requests
        dsl.executables.each { executable ->

            def execMap = [
                    function: executable.function,
                    pkg     : executable.pkg,
                    repo    : executable.repo ?: RepoVocab.opencpu,
                    user    : executable.user ?: EXEC_USER_UNDEFINED
            ]

            executionChain.add(execMap)
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

        // Return parsed Fluent R DSL for task.
        return [executionChain: executionChain,
                fetchWd       : fetchWd,
                fetchWorkspace: fetchWorkspace,
                fetchRuntime  : fetchRuntime]
    }

    /*
     * Method to determine if Fluent R DSL "function"
     * field indicates an executable R function or script.
     */

    private boolean isScript(name) {

        boolean fncIsScript = false
        if (name.contains(".")) {
            def extension = name[name.lastIndexOf("."), -1]
            fncIsScript = extension?.toLowerCase() in scriptTypes
        }
        fncIsScript
    }

    private void secureConnection(restConn) {
        if(allowSelfSignedCert)
            restConn.ignoreSSLIssues()
    }

    /*
     * OpenCPU API constants.
     */
    private static final String OCPU_HEADER_SESSION = "X-ocpu-session"
    private static final String OCPU_HEADER_LOCATION = "Location"
    private static final String OCPU_SERVER_REPO = "library"
    private static final String OCPU_DEFAULT_OBJECT = ".val"
    private static final String OCPU_DEFAULT_OBJECT_ALIAS = "result"
    private static final String OCPU_GRAPHICS = "graphics"
    private static final String EXEC_USER_UNDEFINED = "undefined"
    private static final String JSON_CONTENT_TYPE = "application/json"
    private static final String URLENC_CONTENT_TYPE =
            "application/x-www-form-urlencoded"

    /*
     * OpenCPU API Endpoint Templates.
     */
    private static String OCPU_EXEC_TEMPLATE =
            "/ocpu/\$repo/\$pkg/\$exectype/\$function"
    private static String OCPU_PRIVATE_EXEC_TEMPLATE =
            "/ocpu/user/\$user/\$repo/\$pkg/\$exectype/\$function"
    private static String CRAN_EXEC_TEMPLATE =
            "/ocpu/\$repo/\$pkg/\$exectype/\$function"
    private static String GITHUB_EXEC_TEMPLATE =
            "/ocpu/github/\$user/\$pkg/\$exectype/\$function"
    private static String GIST_EXEC_TEMPLATE =
            "/ocpu/gist/\$user/\$pkg/\$function"
    private static String OCPU_DIRECTORY_FILE_TEMPLATE =
            "\${location}files/\$filename"
    private static String OCPU_WORKSPACE_OBJ_TEMPLATE =
            "R/\$objname/json"
    private static String OCPU_CONSOLE_TEMPLATE = "console/text"
    private static String OCPU_GRAPHICS_TEMPLATE =
            "\${location}graphics/\$plotIndex/png"

    /*
     * OpenCPU script and reproducible document type extensions.
     */
    private final List scriptTypes =
            [".r", ".tex", ".rnw", ".md", ".rmd", ".brew"]

    /*
     * OpenCPU API executables.
     */
    private final String FUNCTION = "R"
    private final String SCRIPT = "scripts"
    
    // GroovyShell Configuration
    private static CompilerConfiguration compilerConfig =
            new CompilerConfiguration([scriptBaseClass: OpenCPUVerbs.name])
    private static ImportCustomizer imports = new ImportCustomizer()

    private static SimpleTemplateEngine engine = new SimpleTemplateEngine()
    private static Gson gson = new Gson()

    /*
     * OpenCPU Fluent R DSL Help.
     */
    private static final String HELP = """
Fluent R invalid grammar detected. Use:

execute FUNCTION|SCRIPT from PACKAGE [on cran | github | opencpu] [by user]
fetch console | plots
fetch X[,Y,Z] from wd | workspace
"""

}