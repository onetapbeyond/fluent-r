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
import io.onetapbeyond.fluent.r.tasks.DeployRTask
import com.revo.deployr.client.RProject
import com.revo.deployr.client.broker.RBroker

/**
 * Static factory for building DeployR Fluent R tasks.
 */
class DeployRTaskBuilder {

    /**
     * Build DeployR Fluent R task targeting server
     * at endpoint provided. The endpoint format is
     * as follows: [http|https]://host:port/deployr.
     *
     * @see io.onetapbeyond.fluent.r.tasks.DeployRTask
     */
    static DeployRTask fluentTask(String endpoint) {
        build([endpoint: endpoint])
    }

    /**
     * Build DeployR Fluent R task targeting server
     * at the endpoint provided. The task will be
     * executed on the server with the permissions of
     * the authenticated user identified by "username".
     *
     * @see io.onetapbeyond.fluent.r.tasks.DeployRTask
     */
    static DeployRTask fluentTask(String endpoint,
                                   String username, String password) {
        build([endpoint : endpoint,
               username: username,
               password: password])
    }

    /**
     * Build DeployR Fluent R task targeting server
     * at http://host:port/deployr. The task will
     * be executed on the server with the permissions
     * of an anonymous user.
     *
     * @see io.onetapbeyond.fluent.r.tasks.DeployRTask
     */
    static DeployRTask fluentTask(String host, int port) {
        build([host: host, port: port])
    }

    /**
     * Build DeployR Fluent R task targeting server
     * at [http|https]://host:port/deployr. The task will
     * be executed on the server with the permissions of
     * an anonymous user.
     *
     * @see io.onetapbeyond.fluent.r.tasks.DeployRTask
     */
    static DeployRTask fluentTask(String host, int port,
                                   boolean https) {
        build([host : host, port : port, https: https])
    }

    /**
     * Build DeployR Fluent R task targeting server
     * at [http|https]://host:port/deployr. The task will
     * be executed on the server with the permissions of
     * the authenticated user identified by "username".
     *
     * @see io.onetapbeyond.fluent.r.tasks.DeployRTask
     */
    static DeployRTask fluentTask(String host, int port,
                                   boolean https,
                                   String username, String password) {
        build([host    : host,
               port    : port,
               https   : https,
               username: username,
               password: password])
    }

    /**
     * Build DeployR Fluent R task targeting server
     * at [http|https]://host:port/deployr. The task will
     * be executed using the supplied broker instance. The
     * task will be executed on the server with the permissions
     * of the user associated with the broker. If there is no
     * user associated with the broker instance, the task will
     * be executed on the server with the permissions of an
     * anonymous user.
     *
     * @see io.onetapbeyond.fluent.r.tasks.DeployRTask
     */
    static DeployRTask fluentTask(RBroker broker) {
        build([broker: broker])
    }

    /**
     * Build DeployR Fluent R task targeting server
     * at [http|https]://host:port/deployr. The task will
     * be executed using the supplied project instance. The
     * task will be executed on the server with the permissions
     * of the user associated with the project.
     *
     * @see io.onetapbeyond.fluent.r.tasks.DeployRTask
     */
    static DeployRTask fluentTask(RProject project) {
        build([project: project])
    }

    /**
     * Build DeployR Fluent R task using the existing
     * configuration associated with the FluentTask 
     * passed as a parameter on the method.
     *
     * @see io.onetapbeyond.fluent.r.tasks.DeployRTask
     */
    static DeployRTask fluentTask(FluentTask task) {
        build(task.config)
    }

    /*
     * Method responsible for the creation of DeployRTask.
     */
    private static DeployRTask build(Map config) {
        new DeployRTask([config:config])
    }

}
