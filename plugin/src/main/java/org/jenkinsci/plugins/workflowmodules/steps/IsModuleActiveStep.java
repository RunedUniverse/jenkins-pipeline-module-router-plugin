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

import java.util.Set;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModule;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleDynamicContext;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;

import hudson.Extension;
import lombok.Getter;

public class IsModuleActiveStep extends Step {

	@Getter
	private String id;

	@DataBoundConstructor
	public IsModuleActiveStep() {
	}

	@DataBoundSetter
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new IsModuleActiveExecution(context, this);
	}

	public static class IsModuleActiveExecution extends SynchronousStepExecution<Boolean> {

		private static final long serialVersionUID = 1L;

		private IsModuleActiveStep step;

		protected IsModuleActiveExecution(StepContext context, IsModuleActiveStep step) {
			super(context);
			this.step = step;
		}

		@Override
		protected Boolean run() throws Exception {
			final WorkflowModuleDynamicContext dynamicContext = getContext().get(WorkflowModuleDynamicContext.class);
			if (dynamicContext == null)
				return false;
			final WorkflowModule module = dynamicContext.getModule(step.getId());
			if (module == null)
				return false;
			return module.active();
		}

	}

	@Extension
	public static class IsModuleActiveDescriptor extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return "isModuleActive";
		}

		@Override
		public String getDisplayName() {
			return "Check if Module is active";
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return ImmutableSet.of(WorkflowModuleDynamicContext.class);
		}

		@Override
		public Set<? extends Class<?>> getProvidedContext() {
			return ImmutableSet.of();
		}

	}

}
