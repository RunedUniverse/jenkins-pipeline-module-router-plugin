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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleDynamicContext;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;

import hudson.Extension;
import lombok.Getter;

public class IsModuleSelectionActiveStep extends Step {

	@Getter
	private Set<String> selectedIds = new LinkedHashSet<>(0);

	@DataBoundConstructor
	public IsModuleSelectionActiveStep() {
	}

	@DataBoundSetter
	public void setSelectedIds(Collection<String> selectedIds) {
		this.selectedIds.addAll(selectedIds);
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new IsModuleSelectionActiveExecution(context, this);
	}

	public static class IsModuleSelectionActiveExecution extends SynchronousStepExecution<Boolean> {

		private static final long serialVersionUID = 1L;

		private IsModuleSelectionActiveStep step;

		protected IsModuleSelectionActiveExecution(StepContext context, IsModuleSelectionActiveStep step) {
			super(context);
			this.step = step;
		}

		@Override
		protected Boolean run() throws Exception {
			final WorkflowModuleDynamicContext dynamicContext = getContext().get(WorkflowModuleDynamicContext.class);
			if (dynamicContext == null)
				return false;
			return !dynamicContext.getModules(this.step.getSelectedIds())
					.isEmpty();
		}

	}

	@Extension
	public static class IsModuleSelectionActiveDescriptor extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return "isModuleSelectionActive";
		}

		@Override
		public String getDisplayName() {
			return "Check if Module Selection is active";
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
