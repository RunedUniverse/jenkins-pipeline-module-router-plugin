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
import java.util.Set;
import java.util.function.Predicate;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModule;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleContainer;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleSelectorBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;

import hudson.Extension;
import hudson.Util;
import lombok.Getter;

public class CheckAllModulesStep extends Step {

	public static final String FUNCTION_NAME = "checkAllModules";

	protected static final String MATCH_ALL = "ALL";
	protected static final String MATCH_ANY = "ANY";

	protected final WorkflowModuleSelectorBuilder builder = new WorkflowModuleSelectorBuilder();

	protected String match = null;
	@Getter
	protected boolean _match = false;

	@DataBoundConstructor
	public CheckAllModulesStep() {
	}

	public String getMatch() {
		return this.match == null ? MATCH_ANY : match.toUpperCase();
	}

	@DataBoundSetter
	public void setMatch(String match) {
		this.match = Util.fixEmptyAndTrim(match);
		this._match = true;
	}

	@DataBoundSetter
	public void setWithIds(Collection<String> ids) {
		this.builder.setWithIds(ids);
	}

	@DataBoundSetter
	public void setActive(Boolean active) {
		this.builder.setActive(active);
	}

	@DataBoundSetter
	public void setWithTags(Collection<String> tags) {
		this.builder.setWithTags(tags);
	}

	@DataBoundSetter
	public void setWithTagIn(Collection<String> tags) {
		this.builder.setWithTagIn(tags);
	}

	public Predicate<WorkflowModule> filter() {
		return this.builder.filter();
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new CheckAllModulesExecution(context, this);
	}

	public static class CheckAllModulesExecution extends SynchronousStepExecution<Boolean> {

		private static final long serialVersionUID = 1L;

		private CheckAllModulesStep step;

		protected CheckAllModulesExecution(StepContext context, CheckAllModulesStep step) {
			super(context);
			this.step = step;
		}

		@Override
		protected Boolean run() throws Exception {
			final WorkflowModuleContainer container = getContext().get(WorkflowModuleContainer.class);
			if (container == null) {
				// theoretically unreachable
				return false;
			}
			switch (this.step.getMatch()) {
			case MATCH_ALL:
				return container.trueForAllModules(this.step.filter());
			case MATCH_ANY:
			default:
				return container.trueForAnyModules(this.step.filter());
			}
		}
	}

	@Extension
	public static class CheckAllModulesDescriptor extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return FUNCTION_NAME;
		}

		@Override
		public String getDisplayName() {
			return "Run checks for all selected modules";
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
