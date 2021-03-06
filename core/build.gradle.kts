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
	jcenter()
}

var env = Properties()
env.load(FileInputStream(File(project.getRootDir(), "build.properties")))
val targetPlatform = JavaVersion.valueOf(env.getProperty("target.platform"))

eclipse {
	project {
    	name = "openMDX 2 ~ Core"
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

fun getVersionClass(packageName: String): String {
	
	return """
	
        package ${packageName};

        public class Version {

            /**
            * A specification version is compliant with a given one if<ol>
            * <li>its major numbers is equal to the given one
            * <li>its minor number is greater than or equal to the given one
            * </ol>
            */
            private final static String SPECIFICATION_VERSION = "${project.version.toString().substring(0, 4)}";
            
            /**
            * An implementation version is compliant with a given one if
            * their string representations are equal.
            */
            private final static String IMPLEMENTATION_VERSION = "${project.version}";

            /**
            * Get the specification version.
            *
            * @return the implementation version
            */
            public static String getSpecificationVersion(
            ){
                return Version.SPECIFICATION_VERSION;
            }

            /**
            * Get the implementation version.
            *
            * @return the implementation version
            */
            public static String getImplementationVersion(
            ){
                return Version.IMPLEMENTATION_VERSION;
            }
                    
            /**
            * Define a main so that the version may easily be printed by a tool
            * 
            * @param arguments	the program arguments
            */
            public static void main(
                String arguments[]
            ) {
                System.out.println(
                    new StringBuilder(
                        Version.class.getName()
                    ).append(
                        '='
                    ).append(
                        Version.getImplementationVersion()
                    )
                );
            }
            
        }
        """.trimIndent()

}

project.getConfigurations().maybeCreate("openmdxBase")
project.getConfigurations().maybeCreate("openmdxBootstrap")
project.getConfigurations().maybeCreate("jdoApi")
project.getConfigurations().maybeCreate("cacheApi")
val openmdxBase by configurations
val openmdxBootstrap by configurations
val jdoApi by configurations
val cacheApi by configurations

dependencies {
    // implementation
    implementation("javax:javaee-api:8.0.+")
    implementation("javax.jdo:jdo-api:3.1")
    implementation("javax.cache:cache-api:1.1.+")
    // openmdxBase
    openmdxBase("org.openmdx:openmdx-base:2.17.7")
    // openmdxBootstrap
    openmdxBootstrap(files(File(project.getBuildDir(), "generated/classes/openmdxBootstrap")))
    openmdxBootstrap("javax:javaee-api:8.0.+")
    // jdo-api
    jdoApi("javax.jdo:jdo-api:3.1")
    // cache-api
    cacheApi("javax.cache:cache-api:1.1.+")
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

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "4G"
}

tasks.register<org.openmdx.gradle.GenerateModelsTask>("generate-model") {
    inputs.dir("${projectDir}/src/model/emf")
    inputs.dir("${projectDir}/src/main/resources")
    outputs.file("${buildDir}/generated/sources/model/openmdx-" + project.getName() + "-models.zip")
    outputs.file("${buildDir}/generated/sources/model/openmdx-" + project.getName() + ".openmdx-xmi.zip")
    classpath = configurations["openmdxBootstrap"]
    doFirst {
        project.copy {
            from(project.zipTree(project.getConfigurations().getByName("openmdxBase").singleFile))
            into(File(project.getBuildDir(), "generated/classes/openmdxBootstrap"))
        }
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

tasks.compileJava {
    dependsOn("generate-model")
    doFirst {
    	var f: File
    	// base/Version
    	f = File("${buildDir}/generated/sources/java/main/org/openmdx/base/Version.java")
    	touch(f)
    	f.writeText(getVersionClass("org.openmdx.base"))
    	// application/Version
        f = File("${buildDir}/generated/sources/java/main/org/openmdx/application/Version.java")
        touch(f)
        f.writeText(getVersionClass("org.openmdx.application"))
        // system/Version
        f = File("${buildDir}/generated/sources/java/main/org/openmdx/system/Version.java")
        touch(f)
        f.writeText(getVersionClass("org.openmdx.system"))
    }
    options.release.set(Integer.valueOf(targetPlatform.getMajorVersion()))
}

tasks {
	assemble {
		dependsOn(
            "openmdx-base.jar",
            "openmdx-base-sources.jar",
            "openmdx-system.jar",
            "openmdx-system-sources.jar"
        )
	}
}

val openmdxBaseIncludes = listOf<String>(
    "javax/cache/*/**",
    "javax/jdo/*/**",
	"javax/jmi/*/**",
	"net/*/NetPackage*",
	"net/rfc/*/RfcPackage*",
	"org/*/OrgPackage*",
	"org/ietf/*/IetfPackage*",
	"org/iso/*/IsoPackage*",
	"org/oasisopen/*/**",
	"org/omg/*/**",
	"org/openmdx/*/OpenmdxPackage*",
	"org/openmdx/*1/**",
	"org/openmdx/*2/**",
	"org/openmdx/application/*/**",
	"org/openmdx/base/*/**",
	"org/openmdx/dalvik/uses/*/**",
	"org/openmdx/exception/*/**",
	"org/openmdx/kernel/*/**",
	"org/openmdx/jdo/*/**",
	"org/openmdx/uses/gnu/*/**",
	"org/openmdx/uses/java/**",
	"org/openmdx/uses/org/apache/commons/collections/*/**",
	"org/openmdx/uses/org/apache/commons/fileupload/*/**",
	"org/openmdx/uses/org/apache/commons/io/*/**",
	"org/openmdx/uses/org/apache/commons/pool/*/**",
	"org/openmdx/uses/org/apache/commons/pool2/*/**",
	"org/un/*/UnPackage*",
	"org/w3c/*/**",
	"org/xmi/*",
	"META-INF/orm.xml",
	"META-INF/openmdx*.properties"
)

val openmdxBaseExcludes = listOf<String>(
	"org/openmdx/compatibility/kernel/url/protocol/*/**",
	"org/openmdx/kernel/url/protocol/*/**"
)

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-base.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-base.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Base Library",
        		"openmdx-base"
        	)
        )
    }
	from(
		File(buildDir, "classes/java/main"),
		File(buildDir, "resources/main"),
		"src/main/resources",
		zipTree(File(buildDir, "generated/sources/model/openmdx-" + project.getName() + ".openmdx-xmi.zip")),
        configurations["cacheApi"].filter { it.name.endsWith("jar") }.map { zipTree(it) },
        configurations["jdoApi"].filter { it.name.endsWith("jar") }.map { zipTree(it) }
	)
	include(openmdxBaseIncludes)
	exclude(openmdxBaseExcludes)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-base-sources.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-base-sources.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Base Sources",
        		"openmdx-base-sources"
        	)
        )
    }
	from(
		"src/main/java",
		File(buildDir, "generated/sources/java/main")
	)
	include(openmdxBaseIncludes)
	exclude(openmdxBaseExcludes)
}

val openmdxSystemIncludes = listOf<String>(
	"org/openmdx/compatibility/kernel/url/protocol/*/**",
	"org/openmdx/kernel/logging/*/**",
	"org/openmdx/kernel/url/protocol/*/**",
	"org/openmdx/kernel/xri/*",
	"org/openmdx/system/*/**"
)

val openmdxSystemExcludes = listOf<String>(
)

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-system.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-system.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX System Library",
        		"openmdx-system"
        	)
        )
    }
    from(
  		File(buildDir, "classes/java/main")
  	)
  	include(openmdxSystemIncludes)
  	exclude(openmdxSystemExcludes)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-system-sources.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-system-sources.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX System Sources",
        		"openmdx-system-sources"
        	)
        )
    }
	from(
		"src/main/java",
		File(buildDir, "generated/sources/java/main")
	)
	include(openmdxSystemIncludes)
	exclude(openmdxSystemExcludes)
}

distributions {
    main {
    	distributionBaseName.set("openmdx-" + getProjectImplementationVersion() + "-core-jre-" + targetPlatform)
        contents {
        	// core
        	from(".") { into("core"); include("LICENSE", "*.LICENSE", "NOTICE", "*.properties", "build*.*", "*.xml", "*.kts") }
            from("src") { into("core/src") }
            // etc
            from("etc") { into("core/etc") }
            // rootDir
            from("..") { include("*.properties", "*.kts" ) }
            // jre-...
            from("../jre-" + targetPlatform + "/core/lib") { into("jre-" + targetPlatform + "/core/lib") }
            from("../jre-" + targetPlatform + "/gradle/repo") { into("jre-" + targetPlatform + "/gradle/repo") }
        }
    }
}
