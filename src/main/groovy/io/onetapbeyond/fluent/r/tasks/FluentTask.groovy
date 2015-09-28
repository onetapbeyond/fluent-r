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
import java.nio.file.Path

/**
 * Defines the common interface implemented by all Fluent R tasks.
 */
abstract class FluentTask {

    Object stream // Fluent R DSL
    Object send   // Task Data Inputs
    boolean allowSelfSignedCert // SSL Policy Configuration

    /**
     * Send input data encoded as {@link String} on task execution.
     */
    abstract FluentTask send(String data)

    /**
     * Send input data encoded as {@link Object} on task execution.
     * <p>
     * Supported {@link Object} encodings are specific to the type 
     * of task, see the following for details:
     * @see io.onetapbeyond.fluent.r.tasks.DeployRTask
     * @see io.onetapbeyond.fluent.r.tasks.OpenCPUTask
     */
    abstract FluentTask send(Object data)

    /**
     * Execute Fluent R DSL on task and return results.
     */
    abstract FluentResult execute()

    /**
     * Ingest Fluent R DSL for task from {@link java.lang.String}.
     */
    public FluentTask stream(String dsl) {
        this.stream = dsl
        this
    }

    /**
     * Ingest Fluent R DSL for task from {@link java.io.InputStream}.
     */
    public FluentTask stream(InputStream dsl) {
        this.stream = new InputStreamReader(dsl)
        this
    }

    /**
     * Ingest Fluent R DSL for task from {@link java.io.File}.
     */
    public FluentTask stream(File dsl) {
        this.stream = dsl
        this
    }

    /**
     * Ingest Fluent R DSL for task from {@link java.io.Reader}.
     */
    public FluentTask stream(Reader dsl) {
        this.stream = dsl
        this
    }

    /**
     * Ingest Fluent R DSL for task from {@link java.nio.file.Path}.
     */
    public FluentTask stream(Path dsl) {
        this.stream = dsl.toFile()
        this
    }

    /**
     * Ingest Fluent R DSL for task from {@link java.net.URL}.
     */
    public FluentTask stream(URL dsl) {
        this.stream = dsl.toURI()
        this
    }

    /**
     * Ingest Fluent R DSL for task from {@link java.net.URI}.
     */
    public FluentTask stream(URI dsl) {
        this.stream = dsl
        this
    }

    /**
     * Configure SSL policy for task communication with R 
     * integration servers.
     */
    public FluentTask blindTrust(boolean blindTrust) {
        this.allowSelfSignedCert = blindTrust
        this
    }

}