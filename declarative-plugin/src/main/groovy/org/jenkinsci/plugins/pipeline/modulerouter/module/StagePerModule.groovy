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