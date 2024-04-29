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
package org.jenkinsci.plugins.pipeline.modulerouter.module

import org.jenkinsci.plugins.pipeline.modeldefinition.model.Stage

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * An individual stage template to be executed within the build per (selected) module.
 * 
 * @author VenaNocta
 */
@ToString
@EqualsAndHashCode
@SuppressFBWarnings(value = "SE_NO_SERIALVERSIONID")
class StagePerModule extends Stage {

	// SUSPENDING PROJECT:
	// Stage cant be resolved
	// -> since upstream the wrong sources are bundled
	// -> see README.md

	Boolean parallelize
}