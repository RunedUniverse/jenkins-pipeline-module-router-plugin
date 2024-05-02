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
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleDynamicContext;
import org.kohsuke.stapler.DataBoundConstructor;
import com.google.common.collect.ImmutableSet;

import hudson.Extension;

public class IsAnyModuleActiveStep extends Step {

	@DataBoundConstructor
	public IsAnyModuleActiveStep() {
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new IsAnyModuleActiveExecution(context);
	}

	public static class IsAnyModuleActiveExecution extends SynchronousStepExecution<Boolean> {

		private static final long serialVersionUID = 1L;

		protected IsAnyModuleActiveExecution(StepContext context) {
			super(context);
		}

		@Override
		protected Boolean run() throws Exception {
			final WorkflowModuleDynamicContext dynamicContext = getContext().get(WorkflowModuleDynamicContext.class);
			if (dynamicContext == null)
				return false;
			return !dynamicContext.getActiveModules()
					.isEmpty();
		}

	}

	@Extension
	public static class IsAnyModuleActiveDescriptor extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return "isAnyModuleActive";
		}

		@Override
		public String getDisplayName() {
			return "Check if any Module is active";
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
