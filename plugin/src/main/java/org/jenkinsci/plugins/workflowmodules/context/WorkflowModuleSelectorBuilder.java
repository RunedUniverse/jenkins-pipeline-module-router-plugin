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

import static org.jenkinsci.plugins.workflowmodules.context.WorkflowModule.valActive;
import static org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleSelector.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

public class WorkflowModuleSelectorBuilder implements Serializable {

	private static final long serialVersionUID = 1L;

	protected final Set<String> withIds = new LinkedHashSet<>(0);
	protected boolean _withIds = false;
	protected Boolean active = null;
	protected boolean _active = false;
	protected final Set<String> withTags = new LinkedHashSet<>(0);
	protected boolean _withTags = false;
	protected final Set<String> withTagIn = new LinkedHashSet<>(0);
	protected boolean _withTagIn = false;

	protected Set<String> value_withIds() {
		return this.withIds;
	}

	protected boolean check_withIds() {
		return this._withIds;
	}

	protected Boolean value_active() {
		return this.active;
	}

	protected boolean check_active() {
		return this._active;
	}

	protected Set<String> value_withTags() {
		return this.withTags;
	}

	protected boolean check_withTags() {
		return this._withTags;
	}

	protected Set<String> value_withTagIn() {
		return this.withTagIn;
	}

	protected boolean check_withTagIn() {
		return this._withTagIn;
	}

	public void setWithIds(Collection<String> selectedIds) {
		this.withIds.addAll(selectedIds);
		this._withIds = true;
	}

	public void setActive(Boolean active) {
		this.active = valActive(active);
		this._active = true;
	}

	public void setWithTags(Collection<String> tags) {
		for (String tag : tags) {
			this.withTags.add(WorkflowModule.valTag(tag));
		}
		this._withTags = true;
	}

	public void setWithTagIn(Collection<String> tags) {
		for (String tag : tags) {
			this.withTagIn.add(WorkflowModule.valTag(tag));
		}
		this._withTagIn = true;
	}

	public Predicate<WorkflowModule> filter() {
		Predicate<WorkflowModule> filter = any();
		// possibly add more checks later
		if (check_withIds()) {
			filter = filter.and(withIdIn(value_withIds()));
		}
		if (check_active()) {
			filter = filter.and(active(value_active()));
		}
		if (check_withTags()) {
			filter = filter.and(withAllTags(value_withTags()));
		}
		if (check_withTagIn()) {
			filter = filter.and(withTagIn(value_withTagIn()));
		}
		return filter;
	}
}
