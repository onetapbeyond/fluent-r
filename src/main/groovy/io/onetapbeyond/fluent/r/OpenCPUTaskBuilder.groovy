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
package io.onetapbeyond.fluent.r

import io.onetapbeyond.fluent.r.tasks.FluentTask
import io.onetapbeyond.fluent.r.tasks.OpenCPUTask

/**
 * Static factory for building OpenCPU Fluent R tasks.
 * @see io.onetapbeyond.fluent.r.tasks.OpenCPUTask
 */
class OpenCPUTaskBuilder {

    /**
     * Build OpenCPU Fluent R task targeting server
     * at endpoint provided.
     *
     * @see io.onetapbeyond.fluent.r.tasks.OpenCPUTask
     */
    static OpenCPUTask fluentTask(String endpoint) {
        build([endpoint: endpoint])
    }

    /**
     * Build OpenCPU Fluent R task using the existing
     * configuration associated with the FluentTask 
     * passed as a parameter on the method.
     *
     * @see io.onetapbeyond.fluent.r.tasks.OpenCPUTask
     */
    static OpenCPUTask fluentTask(FluentTask task) {
        build(task.config)
    }

    /*
     * Method responsible for the creation of OpenCPUTask.
     */
    private static OpenCPUTask build(Map config) {
        new OpenCPUTask([config:config])
    }

}