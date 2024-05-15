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

import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModule;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleContainer;

import hudson.Extension;
import hudson.model.Run;

@Extension
public class ModuleGlobalVar extends GlobalVariable {

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

		Object obj = script.invokeMethod("getContext", WorkflowModuleContainer.class);
		WorkflowModuleContainer container = null;
		if (obj instanceof WorkflowModuleContainer) {
			container = (WorkflowModuleContainer) obj;
		} else
			return null;

		obj = script.invokeMethod("getContext", WorkflowModule.class);
		WorkflowModule module = null;
		if (obj instanceof WorkflowModule) {
			module = (WorkflowModule) obj;
		} else
			return null;
		return new ModuleProxy(container, module);
	}
}
