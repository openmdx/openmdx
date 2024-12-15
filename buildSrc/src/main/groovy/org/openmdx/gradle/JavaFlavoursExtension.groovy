// Copyright (c) Microsoft Corporation.
// All rights reserved.
//
// This code is licensed under the MIT License.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
package org.openmdx.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test

class JavaFlavoursExtension {
	private final List<String> flavours = []
	private final Project project
	FlavourPathResolver javaPathResolver = { String flavour -> "src/$flavour/java" }
	FlavourPathResolver resourcesPathResolver = { String flavour -> "src/$flavour/resources" }
	FlavourPathResolver generatedJavaPathResolver = { String flavour -> project.layout.buildDirectory.dir("generated/sources/${flavour}/java").toString() }
	FlavourPathResolver generatedResourcesPathResolver = { String flavour -> project.layout.buildDirectory.dir("generated/resources/${flavour}").toString() }
	FlavourPathResolver testJavaPathResolver = { String flavour -> "src/${flavour}Test/java" }
	FlavourPathResolver testResourcesPathResolver = { String flavour -> "src/${flavour}Test/resources" }
	FlavourPathResolver generatedTestJavaPathResolver = { String flavour -> project.layout.buildDirectory.dir("generated/sources/${flavour}Test/java").toString() }
	FlavourPathResolver generatedTestResourcesPathResolver = { String flavour -> project.layout.buildDirectory.dir("generated/resources/${flavour}Test").toString() }

	JavaFlavoursExtension(Project project) {
		this.project = project
	}

	List<String> getFlavours() {
		return Collections.unmodifiableList(flavours)
	}

	void flavour(String flavour) {
		flavours << flavour

		project.with {
			SourceSet sourceSet = sourceSets.create(flavour)
			sourceSet.compileClasspath += sourceSets.main.output
			sourceSet.runtimeClasspath += sourceSets.main.output
			sourceSet.java.srcDir { -> javaPathResolver.getPath(flavour) }
			sourceSet.java.srcDir { -> generatedJavaPathResolver.getPath(flavour) }
			sourceSet.resources.srcDir { -> resourcesPathResolver.getPath(flavour) }
			sourceSet.resources.srcDir { -> generatedResourcesPathResolver.getPath(flavour) }

			SourceSet testSourceSet = sourceSets.create("${flavour}Test")
			testSourceSet.compileClasspath += (sourceSets.main.output + sourceSets.test.output + sourceSet.output)
			testSourceSet.runtimeClasspath += (sourceSets.main.output + sourceSets.test.output + sourceSet.output)
			testSourceSet.java.srcDir { -> testJavaPathResolver.getPath(flavour) }
			testSourceSet.java.srcDir { -> generatedTestJavaPathResolver.getPath(flavour) }
			testSourceSet.resources.srcDir { -> testResourcesPathResolver.getPath(flavour) }
			testSourceSet.resources.srcDir { -> generatedTestResourcesPathResolver.getPath(flavour) }

			['api', 'implementation', 'compileOnly', 'compileOnlyApi', 'runtimeOnly', 'testImplementation', 'testCompileOnly', 'testRuntimeOnly'].each { String suffix ->
				// these configurations were magically created when we added the source sets above
				Configuration config = configurations.maybeCreate("${flavour}${suffix.capitalize()}")
				config.extendsFrom(configurations.getByName(suffix))
				if(suffix.startsWith('test')) {
					Configuration flavourConfig = configurations.getByName("${flavour}${suffix.substring(4)}")
					config.extendsFrom(flavourConfig) 
				}
			}
			Task testTask = tasks.create(name: "${flavour}Test", type: Test) {
				group = JavaBasePlugin.VERIFICATION_GROUP
				description = "Runs the tests for ${flavour}."
				testClassesDirs = testSourceSet.output.classesDirs
				classpath = testSourceSet.runtimeClasspath
			}
			check.dependsOn testTask

			Task jarTask = tasks.create(name: "${flavour}Jar", type: Jar) {
				group = BasePlugin.BUILD_GROUP
				description = "Assembles a jar archive containing the $flavour classes combined with the main classes."
				from sourceSet.output
				from sourceSets.main.output
				archiveClassifier.set(flavour)
			}

			artifacts {
				archives jarTask
			}
			assemble.dependsOn jarTask
			
			
		}

	}

}