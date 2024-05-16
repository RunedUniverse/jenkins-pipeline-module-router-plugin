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
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModule;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleContainer;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;

import hudson.Extension;
import hudson.FilePath;
import lombok.Getter;

public class AddModuleStep extends Step {

	@Getter
	private String id;

	@Getter
	private String path;

	@Getter
	private String name = null;

	@Getter
	private Set<String> tags = new LinkedHashSet<>();

	@Getter
	private Boolean active = true;

	@DataBoundConstructor
	public AddModuleStep(String id, String path) {
		this.id = id;
		this.path = path;
	}

	@DataBoundSetter
	public void setName(String name) {
		this.name = name;
	}

	@DataBoundSetter
	public void setTags(Collection<String> tags) {
		this.tags.addAll(tags);
	}

	@DataBoundSetter
	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new AddModuleExecution(context, this);
	}

	public static class AddModuleExecution extends SynchronousStepExecution<Void> {

		private static final long serialVersionUID = 1L;

		private AddModuleStep step;

		protected AddModuleExecution(StepContext context, AddModuleStep step) {
			super(context);
			this.step = step;
		}

		@Override
		protected Void run() throws Exception {
			final FilePath dir = getContext().get(FilePath.class);
			// clean the path + resolve current path from pushd
			final String path = dir.child(this.step.getPath())
					.absolutize()
					.getRemote();
			final WorkflowModuleContainer container = getContext().get(WorkflowModuleContainer.class);
			if (container == null)
				return null;
			final WorkflowModule module = container.createModule(this.step.getId(), path);
			module.rename(this.step.getName());
			module.tags()
					.addAll(this.step.getTags());
			module.activate(this.step.getActive());
			return null;
		}
	}

	@Extension
	public static class AddModuleDescriptor extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return "addModule";
		}

		@Override
		public String getDisplayName() {
			return "Add a Workflow Module";
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return ImmutableSet.of(FilePath.class, WorkflowModuleContainer.class);
		}

		@Override
		public Set<? extends Class<?>> getProvidedContext() {
			return ImmutableSet.of();
		}
	}
}
