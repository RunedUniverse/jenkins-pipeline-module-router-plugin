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
package org.jenkinsci.plugins.workflowmodules.context;

import java.util.Collection;
import java.util.function.Predicate;

public interface WorkflowModuleSelector {

	public static Predicate<WorkflowModule> any() {
		return m -> m != null;
	}

	public static Predicate<WorkflowModule> active() {
		return m -> m == null ? false : m.active();
	}

	public static Predicate<WorkflowModule> active(Boolean state) {
		return new Predicate<WorkflowModule>() {

			@Override
			public boolean test(WorkflowModule m) {
				if (m == null)
					return false;
				final Boolean mState = m.active();
				if (mState == null) {
					return state == null;
				}
				return mState.equals(state);
			}
		};
	}

	public static Predicate<WorkflowModule> withIdIn(Collection<String> ids) {
		return m -> (m == null || ids == null) ? false : ids.contains(m.id());
	}

	public static Predicate<WorkflowModule> withAllTags(Collection<String> tags) {
		return m -> (m == null || tags == null) ? false
				: m.tags()
						.containsAll(tags);
	}

	public static Predicate<WorkflowModule> withTagIn(Collection<String> tags) {
		return new Predicate<WorkflowModule>() {

			@Override
			public boolean test(final WorkflowModule m) {
				if (m == null || tags == null)
					return false;
				final Collection<String> mTags = m.tags();
				for (String tag : tags) {
					if (mTags.contains(tag))
						return true;
				}
				return false;
			}
		};
	}
}
