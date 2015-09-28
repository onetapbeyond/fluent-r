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
package io.onetapbeyond.fluent.r.dsl

import io.onetapbeyond.fluent.r.nouns.RepoVocab
import io.onetapbeyond.fluent.r.nouns.RuntimeVocab
import io.onetapbeyond.fluent.r.nouns.SessionVocab
import org.codehaus.groovy.syntax.SyntaxException

/**
 * OpenCPU Fluent R Integration Module
 */
class OpenCPU {

    public List<Map> executables = []
    public List<Map> fetchables = []
    public List<RuntimeVocab> fetchRuntime = []
    public List<Map> storables = []

    def execute(String function) {

        def artifactMap = ["function": function,
                           "pkg"     : null,
                           "repo"    : RepoVocab.opencpu]
        executables.add(artifactMap)

        ["from": { String pkg ->
            artifactMap.pkg = pkg

            ["on": { RepoVocab repo ->
                artifactMap.repo = repo

                ["by": { String user ->
                    artifactMap.user = user
                }]

            }]

        }]
    }

    def fetch(String... artifacts) {

        def artifactMap = ["artifacts"  : artifacts,
                           "environment": null]
        fetchables.add(artifactMap)

        ["from": { SessionVocab environment ->
            artifactMap.environment = environment
        }]
    }

    def fetch(RuntimeVocab... artifacts) {

        artifacts.each { runtimeArtifact ->
            fetchRuntime << runtimeArtifact
        }
    }

    def methodMissing(String name, args) {

        throw new SyntaxException(
                "Fluent R does not support the following syntax: $name.",
                0, 0
        )
    }

}
  