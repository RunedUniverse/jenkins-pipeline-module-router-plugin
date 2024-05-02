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

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.TagsAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.support.steps.StageStep;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;

public class SkipStageStep extends Step {

	@DataBoundConstructor
	public SkipStageStep() {
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new SkipStageExecution(context);
	}

	public static FlowNode getStage(FlowNode flowNode) {
		for (BlockStartNode node : flowNode.iterateEnclosingBlocks()) {
			if (isStage(node))
				return node;
		}
		return null;
	}

	public static boolean isStage(FlowNode node) {
		if (node == null)
			return false;

		if (node instanceof StepStartNode
				&& ((StepStartNode) node).getDescriptor() instanceof StageStep.DescriptorImpl) {
			// This is a true stage.
			return true;
		}
		return node.getAction(LabelAction.class) != null && node.getAction(ThreadNameAction.class) != null;
	}

	protected static void addTagToFlowNode(FlowNode node, String tagName, String tagValue) {
		if (node == null)
			return;

		TagsAction tagsAction = node.getAction(TagsAction.class);
		if (tagsAction == null) {
			tagsAction = new TagsAction();
			tagsAction.addTag(tagName, tagValue);
			node.addAction(tagsAction);
		} else if (tagsAction.getTagValue(tagName) == null) {
			tagsAction.addTag(tagName, tagValue);
		}
		try {
			node.save();
		} catch (IOException e) {
		}
	}

	public static class SkipStageExecution extends StepExecution {

		private static final long serialVersionUID = 1L;

		public SkipStageExecution(@NonNull StepContext context) {
			super(context);
		}

		@Override
		public boolean start() throws Exception {
			StepContext context = getContext();
			FlowNode flowNode = null;
			try {
				flowNode = context.get(FlowNode.class);
			} catch (IOException | InterruptedException e) {
				context.onFailure(e);
				return true;
			}
			flowNode = getStage(flowNode);
			if (flowNode == null) {
				context.onFailure(new NullPointerException("skipStage is not inside a stage!"));
				return true;
			}
			addTagToFlowNode(flowNode, "STAGE_STATUS", "SKIPPED_FOR_CONDITIONAL");
			return true;
		}

	}

	@Extension
	public static class WhenDescriptor extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return "skipStage";
		}

		@NonNull
		@Override
		public String getDisplayName() {
			return "Mark enclosing Stage as skipped";
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return Collections.singleton(FlowNode.class);
		}

	}

}
