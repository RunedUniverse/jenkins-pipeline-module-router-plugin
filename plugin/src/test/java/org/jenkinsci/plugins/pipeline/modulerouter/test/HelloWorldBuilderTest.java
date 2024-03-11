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
package org.jenkinsci.plugins.pipeline.modulerouter.test;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;

import org.jenkinsci.plugins.pipeline.modulerouter.HelloWorldBuilder;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class HelloWorldBuilderTest {

	@Rule
	public JenkinsRule jenkins = new JenkinsRule();

	final String name = "Bobby";

	@Test
	public void testConfigRoundtrip() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList()
				.add(new HelloWorldBuilder(name));
		project = jenkins.configRoundtrip(project);
		jenkins.assertEqualDataBoundBeans(new HelloWorldBuilder(name), project.getBuildersList()
				.get(0));
	}

	@Test
	public void testConfigRoundtripFrench() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		HelloWorldBuilder builder = new HelloWorldBuilder(name);
		builder.setUseFrench(true);
		project.getBuildersList()
				.add(builder);
		project = jenkins.configRoundtrip(project);

		HelloWorldBuilder lhs = new HelloWorldBuilder(name);
		lhs.setUseFrench(true);
		jenkins.assertEqualDataBoundBeans(lhs, project.getBuildersList()
				.get(0));
	}

	@Test
	public void testBuild() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		HelloWorldBuilder builder = new HelloWorldBuilder(name);
		project.getBuildersList()
				.add(builder);

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins.assertLogContains("Hello, " + name, build);
	}

	@Test
	public void testBuildFrench() throws Exception {

		FreeStyleProject project = jenkins.createFreeStyleProject();
		HelloWorldBuilder builder = new HelloWorldBuilder(name);
		builder.setUseFrench(true);
		project.getBuildersList()
				.add(builder);

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins.assertLogContains("Bonjour, " + name, build);
	}

	@Test
	public void testScriptedPipeline() throws Exception {
		String agentLabel = "my-agent";
		jenkins.createOnlineSlave(Label.get(agentLabel));
		WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
		String pipelineScript = "node {greet '" + name + "'}";
		job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
		WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
		String expectedString = "Hello, " + name + "!";
		jenkins.assertLogContains(expectedString, completedBuild);
	}
}
