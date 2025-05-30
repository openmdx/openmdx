/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: build.gradle.kts
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
plugins {
    kotlin("jvm") version "2.1.0"
}

val projectFlavour = providers.gradleProperty("flavour").getOrElse("5")
val projectSpecificationVersion = "21"
val projectMaintenanceVersion = "0"
val runtimeCompatibility = if (projectFlavour < "4") JavaVersion.VERSION_1_8 else JavaVersion.VERSION_21
val classicChronoTypes = projectFlavour == "2" || projectFlavour == "4"

allprojects {

    group = "org.openmdx"
    version = "${projectFlavour}.${projectSpecificationVersion}.${projectMaintenanceVersion}"
    layout.buildDirectory.set(layout.projectDirectory.dir("build/openmdx-${projectFlavour}"))

    ext {
        extra["projectFlavour"] = projectFlavour
        extra["projectSpecificationVersion"] = projectSpecificationVersion
        extra["projectMaintenanceVersion"] = projectMaintenanceVersion
        extra["runtimeCompatibility"] = runtimeCompatibility
    }
    
	repositories {
		mavenCentral()
	    maven {
	        url = uri("https://datura.econoffice.ch/maven2")
	    }
        maven {
	       url = uri("file:" + File(project.rootDir, "publish/build/openmdx-${projectFlavour}/repos/releases"))
        }
	}

    tasks.withType<JavaCompile> {
        sourceCompatibility = runtimeCompatibility.majorVersion
        targetCompatibility = runtimeCompatibility.majorVersion
        options.release = runtimeCompatibility.majorVersion.toInt()
        options.generatedSourceOutputDirectory = layout.buildDirectory.dir("generated/sources/annotationProcessor/java/main").get().asFile
        options.annotationProcessorPath = configurations.annotationProcessor.get()
        options.compilerArgs.add("-Xplugin:Manifold")
        options.compilerArgs.add("-Amanifold.source.target=${runtimeCompatibility.majorVersion}")
        options.compilerArgs.add("-Amanifold.dump.generated=true")
        if(runtimeCompatibility.isJava8) options.compilerArgs.add("-Xlint:-options")
        if(classicChronoTypes) options.compilerArgs.add("-ACLASSIC_CHRONO_TYPES")
    }

}
