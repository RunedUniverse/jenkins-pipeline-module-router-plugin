/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.workflowmodules.context.cps;

import java.util.HashMap;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.DSL;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.jenkinsci.plugins.workflowmodules.steps.GetModuleStep;
import org.jenkinsci.plugins.workflowmodules.steps.PerModuleStep;

import hudson.Extension;
import hudson.model.Run;

@Extension
public class ModuleGlobalVar extends GlobalVariable {

	// See {@link CpsScript}
	private static final String STEPS_VAR = "steps";

	@Override
	public String getName() {
		return "module";
	}

	@Override
	public Object getValue(CpsScript script) throws Exception {
		if (script == null)
			return null;
		final Run<?, ?> run = script.$build();
		if (run == null)
			return null;
		final DSL dsl = (DSL) script.getProperty(STEPS_VAR);
		final Object result = dsl.invokeMethod(GetModuleStep.FUNCTION_NAME, new HashMap<>());
		if (result == null)
			throw new IllegalStateException(String.format(
					"No Module defined in current context! Make sure you call this global variable in the context of a step like '%s'",
					PerModuleStep.FUNCTION_NAME));
		return result;
	}
}
