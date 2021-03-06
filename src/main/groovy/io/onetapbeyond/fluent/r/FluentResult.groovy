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

/**
 * Defines the interface for objects holding the result data of a completed Fluent R task.
 */
interface FluentResult {

	/**
	 * Determine if task execution was successful.
	 */
    boolean isSuccess()
	/**
	 * Retrieve error message if task execution failed.
	 */
    String getError()
	/**
	 * Retrieve cause of error if task execution failed.
	 * <p>
	 * {@link io.onetapbeyond.fluent.r.FluentServerException} 
	 * indicates a Fluent R task execution error has been detected
	 * by the server.
	 * <p>
	 * {@link io.onetapbeyond.fluent.r.FluentSyntaxException}
	 * indicates a Fluent R DSL syntax error has been detected
	 * by the builder.
	 */
    Exception getErrorCause()

	/**
	 * Retrieve R console output generated by task, if fetch 
	 * indicated by Fluent R DSL.
	 */
    String getConsole()
	/**
	 * Retrieve graphics device plots generated by task, if fetch
	 * indicated by Fluent R DSL.
	 */
    List<URL> getPlots()
	/**
	 * Retrieve working directory files generated by task, if fetch
	 * indicated by Fluent R DSL.
	 */
    List<URL> getFiles()
	/**
	 * Retrieve workspace objects generated by task, if fetch
	 * indicated by Fluent R DSL.
	 */
    Map<String, Object> getObjects()
}
