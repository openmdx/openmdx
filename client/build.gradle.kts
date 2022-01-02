/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: build.gradle.kts
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2020-2021, OMEX AG, Switzerland
 * All rights reserved.
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

import org.gradle.kotlin.dsl.*
import org.w3c.dom.Element
import java.util.*
import java.io.*

plugins {
	java
	`java-library`
	eclipse
	distribution
}

repositories {
	mavenCentral()
}

var env = Properties()
env.load(FileInputStream(File(project.getRootDir(), "build.properties")))
val targetPlatform = JavaVersion.valueOf(env.getProperty("target.platform"))

if(System.getenv("JRE_18") == null) {
   throw GradleException("ERROR: JRE_18 not set (e.g. export JRE_18=/usr/lib/jvm/java-8-openjdk-amd64/jre)")
}

eclipse {
	project {
    	name = "openMDX 2 ~ Client"
    }
    jdt {
		sourceCompatibility = targetPlatform
    	targetCompatibility = targetPlatform
    	javaRuntimeName = "JavaSE-" + targetPlatform    	
    }
}

fun getProjectImplementationVersion(): String {
	return project.getVersion().toString();
}

fun getDeliverDir(): File {
	return File(project.getRootDir(), "jre-" + targetPlatform + "/" + project.getName());
}

fun touch(file: File) {
	ant.withGroovyBuilder { "touch"("file" to file, "mkdirs" to true) }
}

project.getConfigurations().maybeCreate("openmdxBootstrap")
val openmdxBootstrap by configurations
project.getConfigurations().maybeCreate("javaeeApi")
val javaeeApi by configurations

dependencies {
    // implementation
    implementation("javax:javaee-api:8.0.+")
    implementation(project(":core"))
    implementation(project(":security"))
    implementation(files(File(System.getenv("JRE_18"), "lib/rt.jar"))) 
    // openmdxBootstrap
    openmdxBootstrap(project(":core"))
    // javaee-api    
    javaeeApi("javax:javaee-api:8.0.+")
}

sourceSets {
    main {
        java {
            srcDir("src/main/java")
            srcDir("$buildDir/generated/sources/java/main")
        }
        resources {
        	srcDir("src/main/resources")
        }        
    }
}

project.tasks.named("processResources", Copy::class.java) {
    duplicatesStrategy = DuplicatesStrategy.WARN
}
project.tasks.named("processTestResources", Copy::class.java) {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "4G"
}

tasks.compileJava {
	dependsOn(
		":core:openmdx-base.jar",
		":security:openmdx-security.jar"
	)
	doFirst {
		var f = File("$buildDir/resources/main/META-INF/openmdxmof.properties")
		touch(f)
		f.writeText(zipTree(File(getDeliverDir(), "../core/lib/openmdx-base.jar")).matching { include("META-INF/openmdxmof.properties") }.singleFile.readText() )
		f.appendText(zipTree(File(getDeliverDir(), "../security/lib/openmdx-security.jar")).matching { include("META-INF/openmdxmof.properties") }.singleFile.readText() )
	}
    options.release.set(Integer.valueOf(targetPlatform.getMajorVersion()))
}

tasks {
	assemble {
		dependsOn(
            "openmdx-client.jar",
            "openmdx-client-sources.jar",
            "openmdx-dalvik.jar",
            "openmdx-dalvik-sources.jar"
        )
	}
}

val openmdxCommonIncludes = listOf<String>(
)

val openmdxCommonExcludes = listOf<String>(
    "META-INF/MANIFEST.MF",
	"META-INF/openmdxmof.properties",
	"META-INF/openmdxExceptionMapper.properties",
	"org/omg/**/jpa3",
	"org/omg/**/jpa3/*",
	"org/openmdx/**/jpa3",
	"org/openmdx/**/jpa3/*",
	"org/openmdx/application/airsync/**",
	"org/openmdx/application/dataprovider/layer/persistence/jdbc/*/**",
	"org/openmdx/application/mof/externalizer/*/**",
	"org/openmdx/application/naming/*/**",
	"org/openmdx/application/dataprovider/kernel/*/**",
	"org/openmdx/application/rest/adapter/*/**",
	"org/openmdx/application/rest/ejb/*/**",
	"org/openmdx/application/rest/http/servlet/*/**",
	"org/openmdx/application/transaction/*/**",
	"org/openmdx/kernel/ejb/*/**",
	"org/openmdx/kernel/lightweight/*/**",
	"org/openmdx/kernel/naming/*/**",
	"org/openmdx/kernel/servlet/*/**",
	"org/openmdx/kernel/sql/*/**",
	"org/openmdx/uses/org/apache/commons/*/**",
	"org/openmdx/application/rest/http/RestServlet_*",
	"org/openmdx/application/rest/http/RemoteUserLoginModule*",
	"org/openmdx/application/rest/http/RequestCallbackHandler*",
	"org/openmdx/application/rest/http/TrustingLoginModule*",
	"org/openmdx/base/resource/adapter/*/**"
)

val openmdxClientIncludes = listOf<String>(
)

val openmdxClientExcludes = listOf<String>(
    "org/ietf/jgss/*/**",
    "org/openmdx/dalvik/*/**"
)

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-client.jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN
	dependsOn(
		":core:openmdx-base.jar",
		":core:openmdx-system.jar",
		":security:openmdx-security.jar"
	)
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-client.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Client Library",
        		"openmdx-client"
        	)
        )
    }
    with(
    	copySpec {
			from(
				File(buildDir, "classes/java/main"),
				File(buildDir, "resources/main"),
				"src/main/resources"
			).include(
				openmdxClientIncludes
			).exclude(
				openmdxClientExcludes
			)
		},
		copySpec {
			from(
				zipTree(File(getDeliverDir(), "../core/lib/openmdx-base.jar")),
				zipTree(File(getDeliverDir(), "../core/lib/openmdx-system.jar")),
				zipTree(File(getDeliverDir(), "../security/lib/openmdx-security.jar"))
			).include(
				openmdxCommonIncludes
			).exclude(
				openmdxCommonExcludes
			)
		},
		copySpec {
			from(
				configurations["javaeeApi"].filter { it.name.endsWith("jar") }.map { zipTree(it) }	
			).include(
				"javax/transaction/Synchronization.*"
			)
		}
	)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-client-sources.jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN
	dependsOn(
		":core:openmdx-base-sources.jar",
		":core:openmdx-system-sources.jar",
		":security:openmdx-security-sources.jar"
	)
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-client-sources.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Client Sources",
        		"openmdx-client-sources"
        	)
        )
    }
    with(
    	copySpec {
			from(
				"src/main/java",
				File(buildDir, "generated/sources/java/main")
			).include(
				openmdxClientIncludes
			).exclude(
				openmdxClientExcludes
			)
		},
    	copySpec {
			from(
				zipTree(File(getDeliverDir(), "../core/lib/openmdx-base-sources.jar")),
				zipTree(File(getDeliverDir(), "../core/lib/openmdx-system-sources.jar")),
				zipTree(File(getDeliverDir(), "../security/lib/openmdx-security-sources.jar"))
			).include(
				openmdxCommonIncludes
			).exclude(
				openmdxCommonExcludes
			)
		}
	)
}

val openmdxDalvikIncludes = listOf<String>(
)

val openmdxDalvikExcludes = listOf<String>(
	"META-INF/*/**",
	"javax/transaction/*/**",
	"org/openmdx/kernel/platform/platform.properties",
	"org/openmdx/base/**/stream",
	"org/openmdx/base/**/stream/*",
	"org/openmdx/application/**/stream",
	"org/openmdx/application/**/stream/*",
	"**/*.wbxml",
	"**/*.xml",
	"**/*.xsd",
	"**/xmi1",
	"org/omg/primitivetypes/**"
)

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-dalvik.jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN
	dependsOn("openmdx-client.jar")
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-dalvik.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Dalvik Library",
        		"openmdx-dalvik"
        	)
        )
    }
    with(
    	copySpec {
    		from(
    			zipTree(File(getDeliverDir(), "../client/lib/openmdx-client.jar"))
    		).include(
				"META-INF/*.properties"
			).exclude(
				"META-INF/openmdx-xml-outputfactory.properties"
			).eachFile {
				path = "org/openmdx/dalvik/metainf/" + name
			}
    	},
    	copySpec {
			from(
				zipTree(File(getDeliverDir(), "../client/lib/openmdx-client.jar"))
			).include(
				openmdxDalvikIncludes
			).exclude(
				openmdxDalvikExcludes
			)
		},
    	copySpec {
			from(
				File(buildDir, "classes/java/main"),
				File(buildDir, "resources/main"),
				"src/main/dalvik"
			).exclude(
				"META-INF/*/**"
			)
		}
	)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-dalvik-sources.jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-dalvik-sources.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Dalvik Sources",
        		"openmdx-dalvik-sources"
        	)
        )
    }
    with(
    	copySpec {
			from(
				"src/main/java",
				File(buildDir, "generated/sources/java/main"),
				zipTree(File(getDeliverDir(), "../client/lib/openmdx-client-sources.jar"))
			).include(
				openmdxDalvikIncludes
			).exclude(
				openmdxDalvikExcludes
			)
		}
	)
}

distributions {
    main {
    	distributionBaseName.set("openmdx-" + getProjectImplementationVersion() + "-" + project.getName() + "-jre-" + targetPlatform)
        contents {
        	// client
        	from(".") { into(project.getName()); include("LICENSE", "*.LICENSE", "NOTICE", "*.properties", "build*.*", "*.xml", "*.kts") }
            from("src") { into(project.getName() + "/src") }
            // etc
            from("etc") { into(project.getName() + "/etc") }
            // rootDir
            from("..") { include("*.properties", "*.kts" ) }
            // jre-...
            from("../jre-" + targetPlatform + "/" + project.getName() + "/lib") { into("jre-" + targetPlatform + "/" + project.getName() + "/lib") }
            from("../jre-" + targetPlatform + "/gradle/repo") { into("jre-" + targetPlatform + "/gradle/repo") }
        }
    }
}
