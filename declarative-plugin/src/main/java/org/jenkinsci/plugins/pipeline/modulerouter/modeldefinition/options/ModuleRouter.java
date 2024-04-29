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
package org.jenkinsci.plugins.pipeline.modulerouter.modeldefinition.options;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.pipeline.modeldefinition.options.DeclarativeOption;
import org.jenkinsci.plugins.pipeline.modeldefinition.options.DeclarativeOptionDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import net.sf.json.JSONObject;

public class ModuleRouter extends DeclarativeOption {
	private static final long serialVersionUID = 1L;

	private Map<String, Module> modules = new LinkedHashMap<>();

	private String initScript = null;

	@DataBoundConstructor
	public ModuleRouter() {
	}

	@Extension
	@Symbol("moduleRouter")
	public static class Descriptor extends DeclarativeOptionDescriptor {
		@Override
		@NonNull
		public String getDisplayName() {
			return "define modules in this workspace";
		}

		@Override
		public boolean canUseInStage() {
			return true;
		}

		@Override
		public DeclarativeOption newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			final DeclarativeOption o = super.newInstance(req, null);
			if (o instanceof ModuleRouter) {
				ModuleRouter option = (ModuleRouter) o;
				for (Object obj : formData.getJSONArray("modules")) {
					if (obj instanceof JSONObject) {
						JSONObject moduleObj = (JSONObject) obj;
						ModuleRouter.Module module = new Module(moduleObj.getString("id"))
								.path(moduleObj.getString("path"));
						if (moduleObj.has("name")) {
							module.path(moduleObj.getString("name"));
						}
						option.modules.put(module.id(), module);
					}
				}
				if (formData.has("initModule")) {
					option.initScript = formData.getJSONObject("initModule")
							.toString();
				}
			}
			return o;
		}

	}

	public static class Module {
		private String id;
		private String name;
		private FilePath path;

		public Module(String id) {
			this.id = id;
		}

		public String id() {
			return id;
		}

		public String name() {
			return name;
		}

		public Module name(String name) {
			this.name = name;
			return this;
		}

		public FilePath path() {
			return path;
		}

		public Module path(String path) {
			// TODO rel ref to workspace
			this.path = new FilePath(new File(path));
			return this;
		}

	}

}
