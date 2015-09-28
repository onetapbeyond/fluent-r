/*
 * Copyright 2105 David Russell
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
package io.onetapbeyond.fluent.r.encoding

import com.revo.deployr.client.data.RData
import com.revo.deployr.client.factory.RDataFactory

/**
 * Factory to simplfiy building DeployR-encoded data for use
 * when sending inputs on task executions.
 */
class DeployRDataFactory {

    /**
     * Encode task input as R "data.frame".
     */
    static RData DATAFRAME(String name,
                           List<RData> value) {
        RDataFactory.createDataFrame(name, value)
    }

    /**
     * Encode task input as R "data.table".
     */
    static RData DATATABLE(InputStream is,
                           String delimiter,
                           boolean hasHeader,
                           boolean nullMissingData) {
        RDataFactory.createDataTable(is, delimiter, hasHeader, nullMissingData)
    }

    /**
     * Encode task input as R "date".
     */
    static RData DATE(String name,
                      Date value,
                      String format) {
        RDataFactory.createDate(name, value, format)
    }

    /**
     * Encode task input as R "factor".
     */
    static RData FACTOR(String name,
                        List value,
                        List levels,
                        List labels,
                        boolean ordered) {
        RDataFactory.createFactor(name, value, levels, labels, ordered)
    }

    /**
     * Encode task input as R "logical".
     */
    static RData LOGICAL(String name,
                         boolean value) {
        RDataFactory.createBoolean(name, value)
    }

    /**
     * Encode task input as R "vector" of "logical".
     */
    static RData LOGICALVECTOR(String name,
                               List<Boolean> value) {
        RDataFactory.createBooleanVector(name, value)
    }

    /**
     * Encode task input as R "list".
     */
    static RData LIST(String name,
                      List<RData> value) {
        RDataFactory.createList(name, value)
    }

    /**
     * Encode task input as R "literal".
     */
    static RData LITERAL(String name,
                         String value) {
        RDataFactory.createString(name, value)
    }

    /**
     * Encode task input as R "vector" of "literal".
     */
    static RData LITERALVECTOR(String name,
                               List<String> value) {
        RDataFactory.createStringVector(name, value)
    }

    /**
     * Encode task input as R "numeric".
     */
    static RData NUMERIC(String name,
                         double value) {
        RDataFactory.createNumeric(name, value)
    }

    /**
     * Encode task input as R "vector" of "numeric".
     */
    static RData NUMERICVECTOR(String name,
                               List<Double> value) {
        RDataFactory.createNumericVector(name, value)
    }

}
