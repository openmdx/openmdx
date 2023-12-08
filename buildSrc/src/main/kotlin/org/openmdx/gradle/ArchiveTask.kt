/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: ArchiveTask
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.gradle

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.io.FileInputStream
import java.util.*

open class ArchiveTask() : Jar() {

	@get:Input
    var env: Properties 
	init {
		env = Properties()
		env.load(FileInputStream(File(getProject().getRootDir(), "build.properties")))
	}

	@Internal var projectDir = project.projectDir
	@Internal var buildDirAsFile = project.layout.buildDirectory.asFile.get()

	@Input var projectImplementationVersion: String
	init {
		projectImplementationVersion = project.getVersion().toString();
	}

	@Input var projectSpecificationVersion: String
	init {
		projectSpecificationVersion = project.getVersion().toString()
	}

	@Input var projectVendorId: String
	init {
		val v = env.getProperty("project.vendor.id");
		projectVendorId = if(v == null) "org.openmdx" else v;
	}

	@Input var projectVendorName: String
	init {
		val v = env.getProperty("project.vendor.name");
		projectVendorName = if(v == null) "openMDX" else v;
	}

	fun getManifest(
		specificationTitle: String,
		implementationTitle: String
	): Map<String,String> {
		return mapOf(
			"Gradle-Version" to "Gradle " + project.getGradle().getGradleVersion(),
			"Created-By" to System.getProperty("java.runtime.version"),
			"Specification-Vendor" to projectVendorName,
			"Implementation-Vendor" to projectVendorName,
			"Implementation-Vendor-Id" to projectVendorId,
			"Specification-Version" to projectSpecificationVersion,
			"Implementation-Version" to projectImplementationVersion,
			"Specification-Title" to specificationTitle,
			"Implementation-Title" to implementationTitle
	    )
	}
	
}
