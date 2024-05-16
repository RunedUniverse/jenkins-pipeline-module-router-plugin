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

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import hudson.FilePath;
import lombok.Getter;

import static org.jenkinsci.plugins.workflowmodules.context.WorkflowModule.*;

@Restricted(NoExternalUse.class)
public class WorkflowModuleContainer implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(WorkflowModuleContainer.class.getName());

	private final Map<String, WorkflowModule> modules = new ConcurrentHashMap<>();

	@Getter
	private FilePath workspace = null;
	private transient Path workspaceRemote = null;

	protected Path getWorkspaceRemote() {
		if (this.workspaceRemote != null)
			return this.workspaceRemote;
		try {
			this.workspaceRemote = Paths.get(this.workspace.absolutize()
					.getRemote())
					.normalize();
		} catch (IOException | InterruptedException e) {
			LOGGER.warning("Invalid workspace path");
		}
		return this.workspaceRemote;
	}

	public void setWorkspace(FilePath workspace) {
		this.workspace = workspace;
		if (this.workspace == null) {
			this.workspaceRemote = null;
			return;
		}
		try {
			this.workspaceRemote = Paths.get(workspace.absolutize()
					.getRemote())
					.normalize();
		} catch (IOException | InterruptedException e) {
			LOGGER.warning("Invalid workspace path");
		}
	}

	public WorkflowModule createModule(String id, String path)
			throws IllegalStateException, IOException, InterruptedException {
		id = valId(id);
		path = valPath(path);
		WorkflowModule module = this.modules.get(id);
		if (module != null) {
			throw new IllegalStateException("Module »id« already exists!");
		}
		this.modules.put(id, module = new WorkflowModule(id, path));
		if (!isPathWorkspaceDescendant(path)) {
			throw new IllegalStateException("Module »path« is not descendant of the current node!");
		}
		module.setPath(this.workspace.child(path));
		return module;
	}

	public boolean isPathWorkspaceDescendant(String rawPath) {
		final Path wsRemote = getWorkspaceRemote();
		if (wsRemote == null) {
			LOGGER.warning("Invalid workspace path (workspace == null)");
			return false;
		}
		return Paths.get(rawPath)
				.normalize()
				.startsWith(wsRemote);
	}

	public Set<WorkflowModule> getModules(Predicate<WorkflowModule> filter) {
		final Set<WorkflowModule> values = new LinkedHashSet<>(this.modules.values());
		if (filter == null)
			return values;
		filter = filter.negate();
		for (Iterator<WorkflowModule> i = values.iterator(); i.hasNext();) {
			if (filter.test(i.next())) {
				i.remove();
			}
		}
		return values;
	}

	public boolean trueForAllModules(Predicate<WorkflowModule> filter) {
		final Set<WorkflowModule> values = new LinkedHashSet<>(this.modules.values());
		if (filter == null)
			return false;
		filter = filter.negate();
		for (WorkflowModule module : values) {
			if (filter.test(module))
				return false;
		}
		return true;
	}

	public boolean trueForAnyModules(Predicate<WorkflowModule> filter) {
		final Set<WorkflowModule> values = new LinkedHashSet<>(this.modules.values());
		if (filter == null)
			return false;
		for (WorkflowModule module : values) {
			if (filter.test(module))
				return true;
		}
		return false;
	}

	public WorkflowModule getModule(String id) {
		return this.modules.get(valId(id));
	}

	public String relPath(WorkflowModule from, WorkflowModule to) {
		if (from == null || to == null)
			return null;
		try {
			Path fromPath = Paths.get(from.path()
					.getRemote());
			Path toPath = Paths.get(to.path()
					.getRemote());

			final String relPath = fromPath.relativize(toPath)
					.toString();
			if (relPath.isEmpty())
				return ".";
			return relPath;
		} catch (InvalidPathException e) {
			LOGGER.severe("Failed to resolve Paths!\n" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
