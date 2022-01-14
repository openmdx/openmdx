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
/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: build.gradle.kts
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
<<<<<<< HEAD
 * Copyright 
 * (c) 2020-2021, OMEX AG, Switzerland
 * (c) 2022, Datura Informatik+Organisation AG, Switzerland
 * All rights reserved.
 * 
=======
>>>>>>> 1af28ea6cbf66fb856d3b65c9ef5f5e64a2269f9
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

eclipse {
	project {
    	name = "openMDX 2 ~ Security"
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

dependencies {
    // implementation
    implementation(project(":core"))
    implementation("javax:javaee-api:8.0.+")
    implementation("javax.jdo:jdo-api:3.1")    
    implementation("org.apache.directory.api:apache-ldap-api:2.0.+")    
    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.mockito:mockito-core:4.2.0")    
    testImplementation("org.mockito:mockito-junit-jupiter:4.2.0")    
    // openmdxBootstrap
    openmdxBootstrap(project(":core"))
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
    test {
        java {
            srcDir("src/test/java")
        }
        resources {
        	srcDir("src/test/resources")
        }        
    }
}

tasks {
	test {
	    useJUnitPlatform()
	    maxHeapSize = "4G"
	}
	distTar {
		dependsOn(
			":security:openmdx-security.jar",
			":security:openmdx-authentication.jar",
			":security:openmdx-ldap.jar",
			":security:openmdx-pki.jar",
			":security:openmdx-radius.jar",
			":security:openmdx-resource.jar",
			":security:openmdx-security-sources.jar",
			":security:openmdx-authentication-sources.jar",
			":security:openmdx-ldap-sources.jar",
			":security:openmdx-pki-sources.jar",
			":security:openmdx-radius-sources.jar",
			":security:openmdx-resource-sources.jar"
		)
	}
	distZip {
		dependsOn(
			":security:openmdx-security.jar",
			":security:openmdx-authentication.jar",
			":security:openmdx-ldap.jar",
			":security:openmdx-pki.jar",
			":security:openmdx-radius.jar",
			":security:openmdx-resource.jar",
			":security:openmdx-security-sources.jar",
			":security:openmdx-authentication-sources.jar",
			":security:openmdx-ldap-sources.jar",
			":security:openmdx-pki-sources.jar",
			":security:openmdx-radius-sources.jar",
			":security:openmdx-resource-sources.jar"
		)
	}
	register<org.openmdx.gradle.GenerateModelsTask>("generate-model") {
	    inputs.dir("${projectDir}/src/model/emf")
	    inputs.dir("${projectDir}/src/main/resources")
	    outputs.file("${buildDir}/generated/sources/model/openmdx-" + project.getName() + "-models.zip")
	    outputs.file("${buildDir}/generated/sources/model/openmdx-" + project.getName() + ".openmdx-xmi.zip")
	    classpath = configurations["openmdxBootstrap"]
	    doFirst {
	    }
	    doLast {
	        copy {
	            from(
	                zipTree("${buildDir}/generated/sources/model/openmdx-" + project.getName() + "-models.zip")
	            )
	            into("$buildDir/generated/sources/java/main")
	            include(
	                "**/*.java"
	            )
	        }
	    }
	}
	compileJava {
	    dependsOn("generate-model")
	    options.release.set(Integer.valueOf(targetPlatform.getMajorVersion()))
	}
	assemble {
		dependsOn(
            "openmdx-security.jar",
            "openmdx-security-sources.jar",
            "openmdx-authentication.jar",
            "openmdx-authentication-sources.jar",
            "openmdx-radius.jar",
            "openmdx-radius-sources.jar",
            "openmdx-ldap.jar",
            "openmdx-ldap-sources.jar",
            "openmdx-pki.jar",
            "openmdx-pki-sources.jar",
            "openmdx-resource.jar",
            "openmdx-resource-sources.jar"
        )
	}
}

project.tasks.named("processResources", Copy::class.java) {
    duplicatesStrategy = DuplicatesStrategy.WARN
}
project.tasks.named("processTestResources", Copy::class.java) {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

val openmdxSecurityIncludes = listOf<String>(  
    "org/openmdx/security/*/**",
    "org/openmdx/uses/layout/*",
    "META-INF/orm.xml",
    "META-INF/openmdxmof.properties"
)

val openmdxSecurityExcludes = listOf<String>(
    "org/openmdx/security/auth/callback/*/**",
    "org/openmdx/security/auth/passcode/*/**",
    "org/openmdx/security/radius/*/**"
)

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-security.jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN
	dependsOn(
		":security:compileJava",
		":security:generate-model",
		":security:processResources"
	)
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-security.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Security Library",
        		"openmdx-security"
        	)
        )
    }
	from(
		File(buildDir, "classes/java/main"),
		File(buildDir, "resources/main"),
		"src/main/resources",
		zipTree(File(buildDir, "generated/sources/model/openmdx-" + project.getName() + ".openmdx-xmi.zip"))
	)
	include(openmdxSecurityIncludes)
	exclude(openmdxSecurityExcludes)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-security-sources.jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-security-sources.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Security Sources",
        		"openmdx-security-sources"
        	)
        )
    }
	from(
		"src/main/java",
		File(buildDir, "generated/sources/java/main")
	)
	include(openmdxSecurityIncludes)
	exclude(openmdxSecurityExcludes)
}

val openmdxAuthenticationIncludes = listOf<String>(  
	"org/openmdx/security/auth/callback/*/**",
	"org/openmdx/security/auth/passcode/*/**"
)

val openmdxAuthenticationExcludes = listOf<String>(
)

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-authentication.jar") {
    duplicatesStrategy = DuplicatesStrategy.WARN
	dependsOn(
		":security:compileJava",
		":security:processResources"
	)
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-authentication.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Authentication Library",
        		"openmdx-authentication"
        	)
        )
    }
	from(
		File(buildDir, "classes/java/main"),
		File(buildDir, "resources/main"),
		"src/main/resources"
	)
	include(openmdxAuthenticationIncludes)
	exclude(openmdxAuthenticationExcludes)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-authentication-sources.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-authentication-sources.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Authentication Sources",
        		"openmdx-authentication-sources"
        	)
        )
    }
	from(
		"src/main/java",
		File(buildDir, "generated/sources/java/main")
	)
	include(openmdxAuthenticationIncludes)
	exclude(openmdxAuthenticationExcludes)
}

val openmdxRadiusIncludes = listOf<String>(
	"org/openmdx/kernel/collection/*/**",
	"org/openmdx/kernel/text/*/**",
	"org/openmdx/resource/radius/*/**",
	"org/openmdx/security/radius/*/**",
	"org/openmdx/uses/net/sourceforge/jradiusclient/*/**",
	"org/openmdx/uses/org/apache/commons/collections/*/**",
	"org/openmdx/uses/org/apache/commons/pool/*/**"
)

val openmdxRadiusExcludes = listOf<String>(
)

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-radius.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	dependsOn(
		":core:openmdx-base.jar",
		":security:compileJava",
		":security:processResources"
	)
	archiveFileName.set("openmdx-radius.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Radius Library",
        		"openmdx-radius"
        	)
        )
    }
	from(
		File(buildDir, "classes/java/main"),
		File(buildDir, "resources/main"),
		"src/main/resources",
		zipTree(File(getDeliverDir(), "../core/lib/openmdx-base.jar"))		
	)
	include(openmdxRadiusIncludes)
	exclude(openmdxRadiusExcludes)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-radius-sources.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-radius-sources.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Radius Sources",
        		"openmdx-radius-sources"
        	)
        )
    }
	from(
		"src/main/java",
		File(buildDir, "generated/sources/java/main")
	)
	include(openmdxRadiusIncludes)
	exclude(openmdxRadiusExcludes)
}

val openmdxLdapIncludes = listOf<String>(
	"org/openmdx/resource/ldap/*/**"
)

val openmdxLdapExcludes = listOf<String>(
)

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-ldap.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	dependsOn(
		":security:compileJava",
		":security:processResources"
	)
	archiveFileName.set("openmdx-ldap.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Ldap Library",
        		"openmdx-ldap"
        	)
        )
    }
	from(
		File(buildDir, "classes/java/main"),
		File(buildDir, "resources/main"),
		"src/main/resources"
	)
	include(openmdxLdapIncludes)
	exclude(openmdxLdapExcludes)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-ldap-sources.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-ldap-sources.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Ldap Sources",
        		"openmdx-ldap-sources"
        	)
        )
    }
	from(
		"src/main/java",
		File(buildDir, "generated/sources/java/main")
	)
	include(openmdxLdapIncludes)
	exclude(openmdxLdapExcludes)
}

val openmdxPkiIncludes = listOf<String>(
	"org/openmdx/resource/pki/*/**"
)

val openmdxPkiExcludes = listOf<String>(
)

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-pki.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	dependsOn(
		":security:compileJava",
		":security:processResources"
	)
	archiveFileName.set("openmdx-pki.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Pki Library",
        		"openmdx-pki"
        	)
        )
    }
	from(
		File(buildDir, "classes/java/main"),
		File(buildDir, "resources/main"),
		"src/main/resources"
	)
	include(openmdxPkiIncludes)
	exclude(openmdxPkiExcludes)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-pki-sources.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-pki-sources.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Pki Sources",
        		"openmdx-pki-sources"
        	)
        )
    }
	from(
		"src/main/java",
		File(buildDir, "generated/sources/java/main")
	)
	include(openmdxPkiIncludes)
	exclude(openmdxPkiExcludes)
}

val openmdxResourceIncludes = listOf<String>(
	"org/openmdx/resource/cci/*/**",
	"org/openmdx/resource/spi/*/**"
)

val openmdxResourceExcludes = listOf<String>(
)

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-resource.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	dependsOn(
		":security:compileJava",
		":security:processResources"
	)
	archiveFileName.set("openmdx-resource.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Resource Library",
        		"openmdx-resource"
        	)
        )
    }
	from(
		File(buildDir, "classes/java/main"),
		File(buildDir, "resources/main"),
		"src/main/resources"
	)
	include(openmdxResourceIncludes)
	exclude(openmdxResourceExcludes)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-resource-sources.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-resource-sources.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Resource Sources",
        		"openmdx-resource-sources"
        	)
        )
    }
	from(
		"src/main/java",
		File(buildDir, "generated/sources/java/main")
	)
	include(openmdxResourceIncludes)
	exclude(openmdxResourceExcludes)
}

distributions {
    main {
    	distributionBaseName.set("openmdx-" + getProjectImplementationVersion() + "-" + project.getName() + "-jre-" + targetPlatform)
        contents {
        	// security
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
