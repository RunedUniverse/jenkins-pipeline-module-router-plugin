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
package org.jenkinsci.plugins.workflowmodules.steps;

import java.io.Serializable;
import java.util.Set;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModule;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleContainer;
import org.jenkinsci.plugins.workflowmodules.context.cps.ModuleProxy;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;

import hudson.Extension;
import hudson.Util;
import lombok.Getter;

public class GetModuleStep extends Step implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String FUNCTION_NAME = "getModule";

	@Getter
	private String id = null;

	@DataBoundConstructor
	public GetModuleStep() {
	}

	@DataBoundSetter
	public void setId(String id) {
		this.id = Util.fixEmptyAndTrim(id);
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new IsModuleActiveExecution(context, this);
	}

	public static class IsModuleActiveExecution extends SynchronousStepExecution<ModuleProxy> {

		private static final long serialVersionUID = 1L;

		private GetModuleStep step;

		protected IsModuleActiveExecution(StepContext context, GetModuleStep step) {
			super(context);
			this.step = step;
		}

		@Override
		protected ModuleProxy run() throws Exception {
			final WorkflowModuleContainer container = getContext().get(WorkflowModuleContainer.class);
			if (container == null)
				return null;
			final WorkflowModule module;
			final String moduleId = this.step.getId();
			if (moduleId == null) {
				module = getContext().get(WorkflowModule.class);
			} else {
				module = container.getModule(moduleId);
				if (module == null) {
					throw new IllegalStateException(String.format("Module with id »%s« is not defined!", moduleId));
				}
			}
			if (module == null)
				return null;
			return new ModuleProxy(container, module);
		}
	}

	@Extension
	public static class IsModuleActiveDescriptor extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return FUNCTION_NAME;
		}

		@Override
		public String getDisplayName() {
			return "Get active module or by Id";
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return ImmutableSet.of(WorkflowModuleContainer.class);
		}

		@Override
		public Set<? extends Class<?>> getProvidedContext() {
			return ImmutableSet.of();
		}
	}
}
