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
package io.onetapbeyond.fluent.r.opencpu

import static io.onetapbeyond.fluent.r.opencpu.TestConstants.*
import static io.onetapbeyond.fluent.r.OpenCPUTaskBuilder.*
import io.onetapbeyond.fluent.r.*
import io.onetapbeyond.fluent.r.tasks.OpenCPUTask

class TaskOnOpenCPUTests extends GroovyTestCase {

	private OpenCPUTask task

	private String endpoint = System.getProperty("endpoint")
	private boolean blindTrust = Boolean.getBoolean("blindTrust")

	void setUp() {
		task = fluentTask(endpoint)
	}

	void tearDown() {
		
	}

	void testFunctionCallNoInputs() {

		def result = task.stream(FUNCTION_CALL_NO_INPUTS)
						 .blindTrust(blindTrust)
						 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}

	void testFunctionCallRepeatedNoInputs() {

		def result = task.stream(FUNCTION_CALL_NO_INPUTS)
						 .blindTrust(blindTrust)
						 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

		result = fluentTask(task).stream(FUNCTION_CALL_NO_INPUTS)
								 .blindTrust(blindTrust)
								 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}

	void testFunctionCallQueryStringInputs() {

		def result = task.stream(FUNCTION_CALL_REQUIRED_INPUTS)
						 .send(QUERY_STRING_INPUTS)
						 .blindTrust(blindTrust)
						 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}


	void testFunctionCallJsonMapInputs() {

		def result = task.stream(FUNCTION_CALL_REQUIRED_INPUTS)
						 .send(JSON_MAP_INPUTS)
						 .blindTrust(blindTrust)
						 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}

	void testFunctionCallMissingInputs() {

		def result = task.stream(FUNCTION_CALL_REQUIRED_INPUTS)
						 .blindTrust(blindTrust)
						 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentServerException

	}

	void testFunctionCallNotFound() {

		def result = task.stream(FUNCTION_CALL_NOT_FOUND)
						 .blindTrust(blindTrust)
						 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentServerException

	}

	void testFunctionCallBadSyntax() {

		def result = task.stream(FUNCTION_CALL_BAD_SYNTAX)
						 .blindTrust(blindTrust)
						 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentSyntaxException

	}

	void testScriptCallNoInputs() {
		
		def result = task.stream(SCRIPT_CALL_NO_INPUTS)
						 .blindTrust(blindTrust)
						 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}

	void testScriptCallNotFound() {

		def result = task.stream(SCRIPT_CALL_NOT_FOUND)
						 .blindTrust(blindTrust)
						 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentServerException

	}

	void testScriptCallBadSyntax() {

		def result = task.stream(FUNCTION_CALL_BAD_SYNTAX)
						 .blindTrust(blindTrust)
						 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentSyntaxException

	}

}
	