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
import com.revo.deployr.client.factory.*
import com.revo.deployr.client.data.RData
import com.revo.deployr.client.auth.basic.RBasicAuthentication
import com.revo.deployr.client.broker.*
import com.revo.deployr.client.broker.config.*
import com.revo.deployr.client.factory.RBrokerFactory

/*
 * DeployR DiscreteTaskBroker Fluent R Integration Tests.
 */
class TaskOnDiscreteBrokerTests extends GroovyTestCase {

	private RBroker anonBroker
	private DeployRTask anonTask
	private RBroker authBroker
	private DeployRTask authTask

	private String endpoint = System.getProperty("endpoint")
	private String username = System.getProperty("username")
	private String password = System.getProperty("password")
	private boolean blindTrust = Boolean.getBoolean("blindTrust")

	void setUp() {

		try {

			def authToken = new RBasicAuthentication(username, password)
			def anonConfig = new DiscreteBrokerConfig(endpoint)
			def authConfig = new DiscreteBrokerConfig(endpoint, authToken)

			// Test: Anonymous DiscreteTaskBroker on DeployRTask
			anonBroker = RBrokerFactory.discreteTaskBroker(anonConfig)
			anonTask = fluentTask(anonBroker)

			// Test: Authenticated DiscreteTaskBroker on DeployRTask
			authBroker = RBrokerFactory.discreteTaskBroker(authConfig)
			authTask = fluentTask(authBroker)

		} catch(Exception ex) {

			if(anonBroker) {
				try { anonBroker.shutdown() } catch(Exception anex) {}
			}
			if(authBroker) {
				try { authBroker.shutdown() } catch(Exception auex) {}
			}
		}
	}

	void tearDown() {

		if(anonBroker) {
			try { anonBroker.shutdown() } catch(Exception anex) {}
		}
		if(authBroker) {
			try { authBroker.shutdown() } catch(Exception auex) {}
		}
	}

	void testScriptCallOnAnonBrokerNoInputs() {

		def result = anonTask.stream(SCRIPT_CALL_NO_INPUTS)
							 .blindTrust(blindTrust)
							 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}

	void testScriptCallOnAuthBrokerNoInputs() {

		def result = authTask.stream(SCRIPT_CALL_NO_INPUTS)
							 .blindTrust(blindTrust)
							 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null
	}

	void testScriptCallRepeatedOnAuthBrokerNoInputs() {

		def result = authTask.stream(SCRIPT_CALL_NO_INPUTS)
							 .blindTrust(blindTrust)
							 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

		result = fluentTask(authTask).stream(SCRIPT_CALL_NO_INPUTS)
									 .blindTrust(blindTrust)
									 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null
	}

	void testScriptCallOnAuthBrokerRDataInputs() {

		def result = authTask.stream(SCRIPT_CALL_REQUIRED_INPUTS)
							 .send(RDATA_INPUTS_EXPECTED)
							 .blindTrust(blindTrust)
							 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}

	void testScriptCallOnAuthBrokerMismatchInputs() {

		def result = authTask.stream(SCRIPT_CALL_REQUIRED_INPUTS)
							 .send(RDATA_INPUTS_MISMATCH)
							 .blindTrust(blindTrust)
							 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentServerException

	}

	void testScriptCallOnAuthBrokerNotFound() {

		def result = authTask.stream(SCRIPT_CALL_NOT_FOUND)
							 .blindTrust(blindTrust)
							 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentServerException

	}

	void testScriptCallOnAuthBrokerBadSyntax() {

		def result = authTask.stream(SCRIPT_CALL_BAD_SYNTAX)
							 .blindTrust(blindTrust)
							 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentSyntaxException

	}

	void testScriptCallOnAuthBrokerLoadWd() {

		def result = authTask.stream(SCRIPT_CALL_LOAD_WD)
							 .blindTrust(blindTrust)
							 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}

	void testScriptCallOnAuthBrokerLoadWdNotFound() {

		def result = authTask.stream(SCRIPT_CALL_LOAD_WD_NOT_FOUND)
							 .blindTrust(blindTrust)
							 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentServerException

	}

	void testScriptCallOnAuthBrokerLoadWdBadSyntax() {

		def result = authTask.stream(SCRIPT_CALL_LOAD_WD_BAD_SYNTAX)
							 .blindTrust(blindTrust)
							 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentSyntaxException

	}


	void testScriptCallOnAuthBrokerLoadWorkspace() {

		def result = authTask.stream(SCRIPT_CALL_LOAD_WORKSPACE)
							 .blindTrust(blindTrust)
							 .execute()

		assert result.success
		assert result.error == null
		assert result.errorCause == null

	}

	void testScriptCallOnAuthBrokerLoadWorkspaceNotFound() {

		def result = authTask.stream(SCRIPT_CALL_LOAD_WORKSPACE_NOT_FOUND)
							 .blindTrust(blindTrust)
							 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentServerException

	}

	void testScriptCallOnAuthBrokerLoadWorkspaceBadSyntax() {

		def result = authTask.stream(SCRIPT_CALL_LOAD_WORKSPACE_BAD_SYNTAX)
							 .blindTrust(blindTrust)
							 .execute()

		assert !result.success
		assertNotNull result.error
		assertNotNull result.errorCause
		assert result.errorCause instanceof FluentSyntaxException

	}

}
	