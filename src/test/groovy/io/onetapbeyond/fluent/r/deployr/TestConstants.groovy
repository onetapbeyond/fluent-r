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

import static io.onetapbeyond.fluent.r.encoding.DeployRDataFactory.*
import com.revo.deployr.client.data.RData

class TestConstants {

	static final String SCRIPT_CALL_NO_INPUTS = """
	execute "Histogram of Auto Sales" from "root" by "testuser"
	"""

	static final String SCRIPT_CALL_REQUIRED_INPUTS = """
	load "fraudModel.rData" into wd from "example-fraud-score" by "testuser"
	execute "ccFraudScore.R" from "example-fraud-score" by "testuser"
	"""

	static final String SCRIPT_CALL_NOT_FOUND = """
	execute "bad.R" from "onetapbeyond" by "otb"
	"""

	static final String SCRIPT_CALL_BAD_SYNTAX = """
	execute "Histogram of Auto Sales" within "root" by "testuser"
	"""

	static final String SCRIPT_CALL_LOAD_WD = """
	load "fraudModel.rData" into wd from "example-fraud-score" by "testuser"
	execute "Histogram of Auto Sales" from "root" by "testuser"
	"""

	static final String SCRIPT_CALL_LOAD_WD_NOT_FOUND = """
	load "bad.rData" into wd from "example-fraud-score" by "testuser"
	execute "Histogram of Auto Sales" from "root" by "testuser"
	"""

	static final String SCRIPT_CALL_LOAD_WD_BAD_SYNTAX = """
	load "fraudModel.rData" to wd from "example-fraud-score" by "testuser"
	"""

	static final String SCRIPT_CALL_LOAD_WORKSPACE = """
	load "fraudModel.rData" into workspace from "example-fraud-score" by "testuser"
	execute "Histogram of Auto Sales" from "root" by "testuser"
	"""

	static final String SCRIPT_CALL_LOAD_WORKSPACE_NOT_FOUND = """
	load "bad.rData" into workspace from "example-fraud-score" by "testuser"
	execute "Histogram of Auto Sales" from "root" by "testuser"
	"""

	static final String SCRIPT_CALL_LOAD_WORKSPACE_BAD_SYNTAX = """
	load "fraudModel.rData" to workspace from "example-fraud-score" by "testuser"
	"""

	static final List<RData> RDATA_INPUTS_EXPECTED = Arrays.asList(
		NUMERIC("bal", 1000), NUMERIC("trans", 35), NUMERIC("credit", 85))

	static final List<RData> RDATA_INPUTS_MISMATCH = Arrays.asList(
		LITERAL("bal", "1000"), NUMERIC("trans", 35), NUMERIC("credit", 85))

}
	