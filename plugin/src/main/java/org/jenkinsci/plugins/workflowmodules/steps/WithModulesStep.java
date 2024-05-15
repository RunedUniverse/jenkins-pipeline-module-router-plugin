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

import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleContainer;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.collect.ImmutableSet;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.TaskListener;

public class WithModulesStep extends Step implements Serializable {

	private static final long serialVersionUID = 1L;

	@DataBoundConstructor
	public WithModulesStep() {
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new WithModulesExecution(context, this);
	}

	public static class WithModulesExecution extends StepExecution {

		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused")
		private final WithModulesStep step;

		private WorkflowModuleContainer dynamicContext;

		public WithModulesExecution(StepContext context, WithModulesStep step) {
			super(context);
			this.step = step;
			this.dynamicContext = new WorkflowModuleContainer();
		}

		@Override
		public boolean start() throws Exception {
			StepContext context = getContext();
			this.dynamicContext.setWorkspace(context.get(FilePath.class));
			context.newBodyInvoker()
					.withContext(this.dynamicContext)
					.withCallback(BodyExecutionCallback.wrap(context))
					.start();
			return false;
		}

	}

	@Extension
	public static class WithModulesDescriptor extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return "withModules";
		}

		@NonNull
		@Override
		public String getDisplayName() {
			return "Allocate Container for Workflow Modules";
		}

		@Override
		public boolean takesImplicitBlockArgument() {
			return true;
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return ImmutableSet.of(TaskListener.class, FilePath.class);
		}

		@Override
		public Set<? extends Class<?>> getProvidedContext() {
			return ImmutableSet.of(WorkflowModuleContainer.class);
		}

	}
}
