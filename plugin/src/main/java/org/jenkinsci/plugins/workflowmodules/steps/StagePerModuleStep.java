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
import java.util.function.Predicate;

import org.jenkinsci.plugins.workflow.cps.CpsVmThreadOnly;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModule;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleContainer;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;

import hudson.Extension;
import lombok.Getter;

import static org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleContainer.*;

public class StagePerModuleStep extends Step {

	@Getter
	private final Set<String> selectIds = new LinkedHashSet<>(0);
	private boolean _selectedIds = false;

	@Getter
	private String name;

	/**
	 * should a failure in a parallel branch terminate other still executing
	 * branches.
	 */
	@Getter
	private boolean failFast = false;

	@DataBoundConstructor
	public StagePerModuleStep() {
	}

	@DataBoundSetter
	public void setName(String name) {
		this.name = name;
	}

	@DataBoundSetter
	public void setFailFast(Boolean failFast) {
		this.failFast = failFast;
	}

	@DataBoundSetter
	public void setSelectedIds(Collection<String> selectedIds) {
		this.selectIds.addAll(selectedIds);
		this._selectedIds = true;
	}

	public Predicate<WorkflowModule> filter() {
		Predicate<WorkflowModule> filter = selectAll();
		// possibly add more checks later
		if (_selectedIds) {
			filter = filter.and(selectByIds(getSelectIds()));
		}
		return filter;
	}

	@Override
	@CpsVmThreadOnly("CPS program calls this, which is run by CpsVmThread")
	public StepExecution start(StepContext context) throws Exception {
		return new StagePerModuleExecution(context, this);
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
			return ImmutableSet.of(WorkflowModuleContainer.class);
		}

		@Override
		public Set<? extends Class<?>> getProvidedContext() {
			return ImmutableSet.of(WorkflowModule.class);
		}

	}

}
