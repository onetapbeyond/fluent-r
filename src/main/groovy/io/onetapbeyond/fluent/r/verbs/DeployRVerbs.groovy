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
package io.onetapbeyond.fluent.r.verbs

import io.onetapbeyond.fluent.r.nouns.FileVocab
import io.onetapbeyond.fluent.r.nouns.RuntimeVocab

/**
 * DeployR Fluent R Integration DSL Base Script
 *
 * This class adds the verbs [load, execute, fetch, store]
 * for the DeployR Fluent R DSL to the base script.
 */
abstract class DeployRVerbs extends Script {

    def load(String... artifact) {
        this.binding.R.load artifact
    }

    def load(FileVocab fileType) {
        this.binding.R.load fileType
    }

    def execute(String... scriptName) {
        this.binding.R.execute scriptName
    }

    def fetch(String... artifact) {
        this.binding.R.fetch artifact
    }

    def fetch(RuntimeVocab runtimeType) {
        this.binding.R.fetch runtimeType
    }

}
