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

class TestConstants {

	/*
	 * OnOpenCPU Function Test Constants.
	 */

	static final String FUNCTION_CALL_NO_INPUTS = """
	execute "ls" from "base"
	"""

	static final String FUNCTION_CALL_REQUIRED_INPUTS = """
	execute "rnorm" from "stats"
	""" 

	static final String FUNCTION_CALL_NOT_FOUND = """
	execute "bad" from "stats"
	"""

	static final String FUNCTION_CALL_BAD_SYNTAX = """
	execute "ls" within "base"
	"""

	/*
	 * OnOpenCPU Script Test Constants.
	 */

	static final String SCRIPT_CALL_NO_INPUTS = """
	execute "ch01.R" from "MASS"
	"""

	static final String SCRIPT_CALL_NOT_FOUND = """
	execute "bad.R" from "MASS"
	"""

	static final String SCRIPT_CALL_BAD_SYNTAX = """
	execute "ch01.R" within "MASS"
	"""

	static final String QUERY_STRING_INPUTS = "n=11&mean=5"

	static final Map JSON_MAP_INPUTS = [ "n" : 11, "mean" : 5] 

	/*
	 * OnGithub Function Test Constants.
	 */

	static final String GITHUB_FUNCTION_CALL_REQUIRED_INPUTS = """
	execute "tv" from "tvscore" on github by "opencpu"
	""" 

	static final String GITHUB_FUNCTION_CALL_NOT_FOUND = """
	execute "tv" from "tvscore" on github by "unknown"
	"""

	static final String GITHUB_FUNCTION_CALL_BAD_SYNTAX = """
	execute "tv" from "tvscore" within github by "opencpu"
	"""

	static final Map GITHUB_FUNCTION_MAP_INPUTS =
				[ "input" :
					[
					  "age":26, "marital" : "MARRIED",
					  "age":41, "marital" : "DIVORCED",
					  "age":53, "marital" : "NEVER MARRIED"
					]
				]

	/*
	 * OnCRAN Function Test Constants
	 */

	static final String CRAN_FUNCTION_CALL_NO_INPUTS = """
	execute "ch01.R" from "MASS" on cran
	"""

	static final String CRAN_FUNCTION_CALL_REQUIRED_INPUTS = """
	execute "rnorm" from "stats" on cran
	""" 

	static final String CRAN_FUNCTION_CALL_NOT_FOUND = """
	execute "bad" from "stats" on cran
	"""

	static final String CRAN_FUNCTION_CALL_BAD_SYNTAX = """
	execute "ls" within "base" on cran
	"""

	/*
	 * OnCRAN Script Test Constants
	 */

	static final String CRAN_SCRIPT_CALL_NO_INPUTS = """
	execute "ch01.R" from "MASS" on cran
	"""

	static final String CRAN_SCRIPT_CALL_NOT_FOUND = """
	execute "bad.R" from "MASS" on cran
	"""

	static final String CRAN_SCRIPT_CALL_BAD_SYNTAX = """
	execute "ch01.R" within "MASS" on cran
	"""

	static final String CRAN_QUERY_STRING_INPUTS = "n=11&mean=5"

	static final Map CRAN_JSON_MAP_INPUTS = [ "n" : 11, "mean" : 5] 

}
	