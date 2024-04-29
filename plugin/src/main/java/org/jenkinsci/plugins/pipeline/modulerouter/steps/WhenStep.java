package org.jenkinsci.plugins.pipeline.modulerouter.steps;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.TagsAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.support.steps.StageStep;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;

public class WhenStep extends Step {

	@DataBoundConstructor
	public WhenStep() {
	}

	@Override
	public StepExecution start(StepContext context) throws Exception {
		return new WhenExecution(context);
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

	public static class WhenExecution extends StepExecution {

		private static final long serialVersionUID = 1L;

		public WhenExecution(@NonNull StepContext context) {
			super(context);
		}

		@Override
		public boolean start() throws Exception {
			getContext().newBodyInvoker()
					.withCallback(new WhenCallback())
					.start();
			return false;
		}

	}

	protected static class WhenCallback extends BodyExecutionCallback {

		private static final long serialVersionUID = 1L;

		@Override
		public void onSuccess(StepContext context, Object result) {
			FlowNode flowNode = null;
			try {
				flowNode = context.get(FlowNode.class);
			} catch (IOException | InterruptedException e) {
				context.onFailure(e);
				return;
			}
			flowNode = getStage(flowNode);
			if (flowNode == null) {
				context.onFailure(new NullPointerException("when is not inside a stage!"));
				return;
			}
			if (!(result instanceof Boolean)) {
				context.onFailure(new ClassCastException("body return value " + result + " is not boolean"));
				return;
			}
			if ((Boolean) result) {
				context.onSuccess(null);
				return;
			}
			addTagToFlowNode(flowNode, "STAGE_STATUS", "SKIPPED_FOR_CONDITIONAL");
		}

		@Override
		public void onFailure(StepContext context, Throwable t) {
			context.onFailure(t);
		}

	}

	@Extension
	protected static class WhenDescriptor extends StepDescriptor {

		@Override
		public String getFunctionName() {
			return "when";
		}

		@NonNull
		@Override
		public String getDisplayName() {
			return "When condition for workflow stages";
		}

		@Override
		public boolean takesImplicitBlockArgument() {
			return true;
		}

		@Override
		public Set<? extends Class<?>> getRequiredContext() {
			return Collections.singleton(null);
		}

	}

}
