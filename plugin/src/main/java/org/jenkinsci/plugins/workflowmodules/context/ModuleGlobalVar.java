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

import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import groovy.lang.GroovyObjectSupport;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;

@Extension
public class ModuleGlobalVar extends GlobalVariable {

	@Override
	public String getName() {
		return "module";
	}

	@Override
	public Object getValue(CpsScript script) throws Exception {
		if (script == null)
			return null;
		final Run<?, ?> run = script.$build();
		if (run == null)
			return null;

		Object obj = script.invokeMethod("getContext", WorkflowModuleContainer.class);
		WorkflowModuleContainer container = null;
		if (obj instanceof WorkflowModuleContainer) {
			container = (WorkflowModuleContainer) obj;
		} else
			return null;

		obj = script.invokeMethod("getContext", WorkflowModule.class);
		WorkflowModule module = null;
		if (obj instanceof WorkflowModule) {
			module = (WorkflowModule) obj;
		} else
			return null;
		return new ProxyModule(container, module);
	}

	public static class ProxyModule extends GroovyObjectSupport {

		private final WorkflowModuleContainer container;
		private final WorkflowModule module;

		public ProxyModule(final WorkflowModuleContainer container, final WorkflowModule module) {
			this.container = container;
			this.module = module;
		}

		public String id() {
			return this.module.id();
		}

		public String rawPath() {
			return this.module.rawPath();
		}

		public FilePath path() {
			return this.module.path();
		}

		public String name() {
			return this.module.name();
		}

		public void rename(String name) {
			this.module.rename(name);
		}

		public Boolean active() {
			return this.module.active();
		}

		public void activate(Boolean value) {
			this.module.activate(value);
		}

		public String relPathFrom(String moduleId) {
			final WorkflowModule from = this.container.getModule(moduleId);
			if (from == null) {
				throw new IllegalStateException("Module with id »" + moduleId + "« not found!");
			}
			return this.container.relPath(from, this.module);
		}

		public String relPathTo(String moduleId) {
			final WorkflowModule to = this.container.getModule(moduleId);
			if (to == null) {
				throw new IllegalStateException("Module with id »" + moduleId + "« not found!");
			}
			return this.container.relPath(this.module, to);
		}

		// block unwanted access

		@Override
		public Object getProperty(String property) {
			switch (property) {
			case "this":
			case "container":
			case "module":
				return null;
			}
			return super.getProperty(property);
		}

		@Override
		public void setProperty(String property, Object newValue) {
			switch (property) {
			case "container":
			case "module":
				return;
			}
			super.setProperty(property, newValue);
		}

		@Override
		public Object invokeMethod(String name, Object args) {
			switch (name) {
			case "getContainer":
			case "setContainer":
			case "getModule":
			case "setModule":
			case "getMetaClass":
			case "setMetaClass":
				return null;
			}
			return super.invokeMethod(name, args);
		}

	}
}
