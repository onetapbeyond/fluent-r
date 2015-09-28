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
package io.onetapbeyond.fluent.r.deployr

import static io.onetapbeyond.fluent.r.deployr.TestConstants.*
import static io.onetapbeyond.fluent.r.DeployRTaskBuilder.*
import static io.onetapbeyond.fluent.r.encoding.DeployRDataFactory.*
import io.onetapbeyond.fluent.r.*
import io.onetapbeyond.fluent.r.tasks.DeployRTask
import com.revo.deployr.client.*
import com.revo.deployr.client.factory.*
import com.revo.deployr.client.data.RData
import com.revo.deployr.client.auth.basic.RBasicAuthentication
import com.revo.deployr.client.broker.*
import com.revo.deployr.client.broker.config.*
import com.revo.deployr.client.broker.task.*
import com.revo.deployr.client.factory.RBrokerFactory

/*
 * DeployR Project Fluent R Integration Tests.
 */
class TestOnProjectTests extends GroovyTestCase {

	private RClient rClient
	private RProject rProject
	private DeployRTask projectTask

	private String endpoint = System.getProperty("endpoint")
	private String username = System.getProperty("username")
	private String password = System.getProperty("password")
	private boolean blindTrust = Boolean.getBoolean("blindTrust")

	void setUp() {

		try {

			rClient = RClientFactory.createClient(endpoint)
			def authToken = new RBasicAuthentication(username, password)
			def rUser = rClient.login(authToken)

			// Test: Project on DeployRTask
			rProject = rUser.createProject()
			projectTask = fluentTask(rProject)

		} catch(Exception ex) {

			if(rProject) {
				try { rProject.close() } catch(Exception pex) {}
			}

			if(rClient) {
				try { rClient.release() } catch(Exception pex) {}				
			}
		}
	}

	void tearDown() {

		if(rProject) {
			try {
				rProject.close() } catch(Exception pex) {}
		}

		if(rClient) {
			try { rClient.release() } catch(Exception pex) {}				
		}
	}

	void testScriptCallOnProjectNoInputs() {

		def result = projectTask.stream(SCRIPT_CALL_NO_INPUTS)
								.blindTrust(blindTrust)
								.execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null
	}


	void testScriptCallRepeatedOnProjectNoInputs() {

		def result = projectTask.stream(SCRIPT_CALL_NO_INPUTS)
								.blindTrust(blindTrust)
								.execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

		result = fluentTask(projectTask).stream(SCRIPT_CALL_NO_INPUTS)
										.blindTrust(blindTrust)
										.execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null
	}

	void testScriptCallOnProjectRDataInputs() {

		def result = projectTask.stream(SCRIPT_CALL_REQUIRED_INPUTS)
								.send(RDATA_INPUTS_EXPECTED)
								.blindTrust(blindTrust)
								.execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}

	void testScriptCallOnProjectMismatchInputs() {

		def result = projectTask.stream(SCRIPT_CALL_REQUIRED_INPUTS)
								.send(RDATA_INPUTS_MISMATCH)
								.blindTrust(blindTrust)
								.execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentServerException

	}

	void testScriptCallOnProjectNotFound() {

		def result = projectTask.stream(SCRIPT_CALL_NOT_FOUND)
								.blindTrust(blindTrust)
								.execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentServerException

	}

	void testScriptCallOnProjectBadSyntax() {

		def result = projectTask.stream(SCRIPT_CALL_BAD_SYNTAX)
							 	.blindTrust(blindTrust)
							 	.execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentSyntaxException

	}

	void testScriptCallOnProjectLoadWd() {

		def result = projectTask.stream(SCRIPT_CALL_LOAD_WD)
							 	.blindTrust(blindTrust)
							 	.execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}

	void testScriptCallOnProjectLoadWdNotFound() {

		def result = projectTask.stream(SCRIPT_CALL_LOAD_WD_NOT_FOUND)
							 	.blindTrust(blindTrust)
							 	.execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentServerException

	}

	void testScriptCallOnProjectLoadWdBadSyntax() {

		def result = projectTask.stream(SCRIPT_CALL_LOAD_WD_BAD_SYNTAX)
							 	.blindTrust(blindTrust)
							 	.execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentSyntaxException

	}


	void testScriptCallOnProjectLoadWorkspace() {

		def result = projectTask.stream(SCRIPT_CALL_LOAD_WORKSPACE)
						 		.blindTrust(blindTrust)
						 		.execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}

	void testScriptCallOnProjectLoadWorkspaceNotFound() {

		def result = projectTask.stream(SCRIPT_CALL_LOAD_WORKSPACE_NOT_FOUND)
							 	.blindTrust(blindTrust)
							 	.execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentServerException

	}

	void testScriptCallOnProjectLoadWorkspaceBadSyntax() {

		def result = projectTask.stream(SCRIPT_CALL_LOAD_WORKSPACE_BAD_SYNTAX)
							 	.blindTrust(blindTrust)
							 	.execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentSyntaxException

	}
	
}
	