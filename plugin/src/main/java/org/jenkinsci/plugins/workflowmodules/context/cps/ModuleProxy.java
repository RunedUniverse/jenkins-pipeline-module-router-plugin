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
package org.jenkinsci.plugins.workflowmodules.context.cps;

import java.io.Serializable;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModule;
import org.jenkinsci.plugins.workflowmodules.context.WorkflowModuleContainer;

import hudson.FilePath;

public class ModuleProxy implements Serializable {

	private static final long serialVersionUID = 1L;

	protected final WorkflowModuleContainer container;
	protected final WorkflowModule module;

	public ModuleProxy(final WorkflowModuleContainer container, final WorkflowModule module) {
		this.container = container;
		this.module = module;
	}

	@Whitelisted
	public String id() {
		return this.module.id();
	}

	@Whitelisted
	public String rawPath() {
		return this.module.rawPath();
	}

	@Whitelisted
	public FilePath path() {
		return this.module.path();
	}

	@Whitelisted
	public String name() {
		return this.module.name();
	}

	@Whitelisted
	public void rename(String name) {
		this.module.rename(name);
	}

	@Whitelisted
	public Boolean active() {
		return this.module.active();
	}

	@Whitelisted
	public void activate(Boolean value) {
		this.module.activate(value);
	}

	@Whitelisted
	public String relPathFrom(String moduleId) {
		final WorkflowModule from = this.container.getModule(moduleId);
		if (from == null) {
			throw new IllegalStateException("Module with id »" + moduleId + "« not found!");
		}
		return this.container.relPath(from, this.module);
	}

	@Whitelisted
	public String relPathFrom(ModuleProxy proxy) {
		return this.container.relPath(proxy.module, this.module);
	}

	@Whitelisted
	public String relPathTo(String moduleId) {
		final WorkflowModule to = this.container.getModule(moduleId);
		if (to == null) {
			throw new IllegalStateException("Module with id »" + moduleId + "« not found!");
		}
		return this.container.relPath(this.module, to);
	}

	@Whitelisted
	public String relPathTo(ModuleProxy proxy) {
		return this.container.relPath(this.module, proxy.module);
	}

	@Override
	@Whitelisted
	public boolean equals(Object obj) {
		if (obj instanceof ModuleProxy) {
			ModuleProxy other = (ModuleProxy) obj;
			return this.module.equals(other.module);
		}
		return super.equals(obj);
	}

	@Override
	@Whitelisted
	public int hashCode() {
		return this.module.hashCode();
	}

}