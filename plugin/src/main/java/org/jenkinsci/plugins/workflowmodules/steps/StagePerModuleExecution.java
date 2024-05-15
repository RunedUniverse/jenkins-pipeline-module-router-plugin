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

import static org.jenkinsci.plugins.workflow.cps.persistence.PersistenceContext.FLOW_NODE;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.cps.CpsStepContext;
import org.jenkinsci.plugins.workflow.cps.persistence.PersistIn;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.BodyExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModule;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleContainer;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.model.CauseOfInterruption;

public class StagePerModuleExecution extends StepExecution {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(StagePerModuleExecution.class.getName());

	private transient StagePerModuleStep step;

	private final List<BodyExecution> bodies = new LinkedList<>();

	public StagePerModuleExecution(final StepContext context, final StagePerModuleStep step) {
		super(context);
		this.step = step;
	}

	@Override
	public boolean start() throws Exception {
		final FlowNode node = getContext().get(FlowNode.class);
		final CpsStepContext cps = (CpsStepContext) getContext();

		node.addAction(new LabelAction(step.getName()));

		if (!getContext().hasBody()) {
			cps.get(TaskListener.class)
					.getLogger()
					.println("No branches to run");
			cps.onSuccess(Collections.<String, Object>emptyMap());
			return true;
		}

		WorkflowModuleContainer container = cps.get(WorkflowModuleContainer.class);

		ParallelResultHandler<StagePerModuleExecution> r = new ParallelResultHandler<>(cps, this, step.isFailFast())
				.setLogger(LOGGER);

		for (WorkflowModule module : container.getModules(this.step.filter())) {
			BodyExecution body = cps.newBodyInvoker()
					.withStartAction(new ParallelLabelAction(module.name()))
					.withCallback(r.callbackFor(module.name()))
					.withContext(module)
					.start();
			bodies.add(body);
		}

		return false;
	}

	@Override
	public void stop(Throwable cause) {
		// Despite suggestion in JENKINS-26148, super.stop does not work here, even
		// accounting for the direct call from checkAllDone.
		for (BodyExecution body : bodies) {
			body.cancel(cause);
		}
	}

	@PersistIn(FLOW_NODE)
	protected static class ParallelLabelAction extends LabelAction implements ThreadNameAction {

		private final String branchName;

		ParallelLabelAction(String branchName) {
			super(null);
			this.branchName = branchName;
		}

		@Override
		public String getDisplayName() {
			return "Branch: " + branchName;
		}

		@NonNull
		@Override
		public String getThreadName() {
			return branchName;
		}
	}

	/**
	 * Used to abort a running branch body in the case of {@code failFast} taking
	 * effect.
	 */
	protected static final class FailFastCause extends CauseOfInterruption {

		private static final long serialVersionUID = 1L;

		private final String failingBranch;

		FailFastCause(String failingBranch) {
			this.failingBranch = failingBranch;
		}

		@Override
		public String getShortDescription() {
			return "Failed in branch " + failingBranch;
		}

	}

}