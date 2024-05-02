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
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModule;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleDynamicContext;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;

import hudson.Extension;
import lombok.Getter;

public class StagePerModuleStep extends Step {

	@Getter
	private String name;

	@Getter
	private Boolean parallelize;

	@Getter
	private Set<String> selectedIds = new LinkedHashSet<>(0);

	@DataBoundConstructor
	public StagePerModuleStep() {
	}

	@DataBoundSetter
	public void setName(String name) {
		this.name = name;
	}

	@DataBoundSetter
	public void setParallelize(Boolean parallelize) {
		this.parallelize = parallelize;
	}

	@DataBoundSetter
	public void setSelectedIds(Collection<String> selectedIds) {
		this.selectedIds.addAll(selectedIds);
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public static class StagePerModuleExecution extends StepExecution {

		private static final long serialVersionUID = 1L;

		public StagePerModuleExecution(StepContext context) {
			super(context);
		}

		@Override
		public boolean start() throws Exception {
			// TODO Auto-generated method stub
			return false;
		}

	}

	@Extension
	public static class StagePerModuleDescriptor extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return "stagePerModule";
		}

		@Override
		public String getDisplayName() {
			return "Execute Body as Stage per Module";
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return ImmutableSet.of(WorkflowModuleDynamicContext.class);
		}

		@Override
		public Set<? extends Class<?>> getProvidedContext() {
			return ImmutableSet.of(WorkflowModule.class);
		}

	}

}
