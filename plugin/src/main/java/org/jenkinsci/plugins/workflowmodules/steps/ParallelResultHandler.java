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

import static org.jenkinsci.plugins.workflow.cps.persistence.PersistenceContext.PROGRAM;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.plugins.workflow.cps.persistence.PersistIn;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflowmodules.steps.StagePerModuleExecution.FailFastCause;

import com.cloudbees.groovy.cps.Outcome;

import hudson.AbortException;
import hudson.model.Result;
import hudson.model.TaskListener;

/**
 * ParallelResultHandler derived from {@link ParallelStep.ResultHandler} by
 * Kohsuke Kawaguchi
 * <p>
 *
 * @author VenaNocta
 */
@PersistIn(PROGRAM)
public class ParallelResultHandler<E extends StepExecution> implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ParallelResultHandler.class.getName());

	protected final StepContext context;
	protected final E stepExecution;
	protected final boolean failFast;

	private transient Logger logger = null;
	/** Have we called stop on the StepExecution? */
	private boolean stopSent = false;
	/**
	 * If we fail fast, we need to record the first failure.
	 *
	 * <p>
	 * We use a set because we may be encountering the same abort being delivered
	 * across branches. We use a linked hash set to maintain insertion order.
	 */
	private final LinkedHashSet<Throwable> failures = new LinkedHashSet<>();

	/**
	 * Collect the results of sub-workflows as they complete. The key set is fully
	 * populated from the beginning.
	 */
	private final Map<String, Outcome> outcomes = new HashMap<>();

	public ParallelResultHandler(final StepContext context, final E stepExecution, final boolean failFast) {
		this.context = context;
		this.stepExecution = stepExecution;
		this.failFast = failFast;
	}

	public ParallelResultHandler<E> setLogger(Logger logger) {
		this.logger = logger;
		return this;
	}

	protected Logger getLogger() {
		return this.logger == null ? LOGGER : this.logger;
	}

	protected ParallelResultHandler.Callback callbackFor(String name) {
		this.outcomes.put(name, null);
		return new Callback(this, name);
	}

	protected void stopSent() {
		this.stopSent = true;
	}

	public boolean isStopSent() {
		return this.stopSent;
	}

	protected static class Callback extends BodyExecutionCallback {

		private static final long serialVersionUID = 1L;

		protected final ParallelResultHandler<?> handler;
		protected final String name;

		Callback(ParallelResultHandler<?> handler, String name) {
			this.handler = handler;
			this.name = name;
		}

		@Override
		public void onSuccess(StepContext context, Object result) {
			handler.outcomes.put(name, new Outcome(result, null));
			checkAllDone(false);
		}

		@Override
		public void onFailure(StepContext context, Throwable t) {
			handler.outcomes.put(name, new Outcome(null, t));
			try {
				context.get(TaskListener.class)
						.getLogger()
						.println("Failed in branch " + name);
			} catch (IOException | InterruptedException x) {
				this.handler.getLogger()
						.log(Level.WARNING, null, x);
			}
			handler.failures.add(t);
			checkAllDone(true);
		}

		private void checkAllDone(boolean stepFailed) {
			Map<String, Object> success = new HashMap<>();
			for (Entry<String, Outcome> e : handler.outcomes.entrySet()) {
				Outcome o = e.getValue();

				if (o == null) {
					// some of the results are not yet ready
					if (stepFailed && handler.failFast && !handler.isStopSent()) {
						handler.stopSent();
						try {
							handler.stepExecution
									.stop(new FlowInterruptedException(Result.ABORTED, true, new FailFastCause(name)));
						} catch (Exception x) {
							this.handler.getLogger()
									.log(Level.WARNING, null, x);
						}
					}
					return;
				}
				if (o.isFailure()) {
					if (handler.failures.isEmpty()) {
						// in case the plugin is upgraded whilst a parallel step is running
						handler.failures.add(e.getValue()
								.getAbnormal());
					}
					// recorded in the onFailure
				} else {
					success.put(e.getKey(), o.getNormal());
				}
			}
			// all done
			List<Throwable> toAttach = new ArrayList<>(handler.failures);
			if (!handler.failFast) {
				toAttach.sort(new ThrowableComparator(new ArrayList<>(handler.failures)));
			}
			if (!toAttach.isEmpty()) {
				Throwable head = toAttach.get(0);
				for (int i = 1; i < toAttach.size(); i++) {
					head.addSuppressed(toAttach.get(i));
				}
				handler.context.onFailure(head);
			} else {
				handler.context.onSuccess(success);
			}
		}
	}

	/**
	 * Sorts {@link Throwable Throwables} in order of most to least severe. General
	 * {@link Throwable Throwables} are most severe, followed by instances of
	 * {@link AbortException}, and then instances of
	 * {@link FlowInterruptedException}, which are ordered by
	 * {@link FlowInterruptedException#getResult()}.
	 */
	public static final class ThrowableComparator implements Comparator<Throwable>, Serializable {

		private static final long serialVersionUID = 1L;

		private final List<Throwable> insertionOrder;

		public ThrowableComparator() {
			this.insertionOrder = new ArrayList<>();
		}

		public ThrowableComparator(List<Throwable> insertionOrder) {
			this.insertionOrder = insertionOrder;
		}

		@Override
		public int compare(Throwable t1, Throwable t2) {
			if (!(t1 instanceof FlowInterruptedException) && t2 instanceof FlowInterruptedException) {
				// FlowInterruptedException is always less severe than any other exception.
				return -1;
			} else if (t1 instanceof FlowInterruptedException && !(t2 instanceof FlowInterruptedException)) {
				// FlowInterruptedException is always less severe than any other exception.
				return 1;
			} else if (!(t1 instanceof AbortException) && t2 instanceof AbortException) {
				// AbortException is always less severe than any exception other than
				// FlowInterruptedException.
				return -1;
			} else if (t1 instanceof AbortException && !(t2 instanceof AbortException)) {
				// AbortException is always less severe than any exception other than
				// FlowInterruptedException.
				return 1;
			} else if (t1 instanceof FlowInterruptedException && t2 instanceof FlowInterruptedException) {
				// Two FlowInterruptedExceptions are compared by their results.
				FlowInterruptedException fie1 = (FlowInterruptedException) t1;
				FlowInterruptedException fie2 = (FlowInterruptedException) t2;
				Result r1 = fie1.getResult();
				Result r2 = fie2.getResult();
				if (r1.isWorseThan(r2)) {
					return -1;
				} else if (r1.isBetterThan(r2)) {
					return 1;
				}
			} else if (insertionOrder.contains(t1) && insertionOrder.contains(t2)) {
				// Break ties by insertion order. Earlier errors are worse.
				int index1 = insertionOrder.indexOf(t1);
				int index2 = insertionOrder.indexOf(t2);
				if (index1 < index2) {
					return -1;
				} else if (index1 > index2) {
					return 1;
				}
			}
			return 0;
		}
	}
}