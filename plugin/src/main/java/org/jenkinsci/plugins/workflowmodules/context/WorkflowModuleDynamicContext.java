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

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import static org.jenkinsci.plugins.workflowmodules.context.WorkflowModule.*;

@Restricted(NoExternalUse.class)
public class WorkflowModuleDynamicContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String, WorkflowModule> modules = new ConcurrentHashMap<>();

	public WorkflowModule createModule(String id, String path) throws IllegalStateException {
		id = valId(id);
		path = valPath(path);
		WorkflowModule module = this.modules.get(id);
		if (module != null) {
			throw new IllegalStateException("Module »id« already exists!");
		}
		this.modules.put(id, module = new WorkflowModule(id, path));
		return module;
	}

	public WorkflowModule getModule(String id) {
		return this.modules.get(valId(id));
	}

	public Set<WorkflowModule> getModules(Collection<String> ids) {
		final Set<WorkflowModule> selection = new LinkedHashSet<>(0);
		if (ids == null || ids.isEmpty()) {
			selection.addAll(modules.values());
		} else {
			for (String id : ids) {
				selection.add(this.modules.get(id));
			}
		}
		return selection;
	}

	public Set<WorkflowModule> getActiveModules() {
		final Set<WorkflowModule> selection = new LinkedHashSet<>(0);
		for (WorkflowModule module : this.modules.values()) {
			if (module.active())
				selection.add(module);
		}
		return selection;
	}

}
