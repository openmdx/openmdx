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
    	name = "openMDX 2 ~ Portal"
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
    implementation("javax:javaee-api:8.0.+")
    implementation("javax.jdo:jdo-api:3.1")
    implementation("org.codehaus.groovy:groovy:3.0.+")
    implementation(project(":core"))
    // openmdxBootstrap
    openmdxBootstrap(project(":core"))
	// test
    testImplementation("junit:junit:4.12")
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
            srcDir("$buildDir/generated/sources/java/test")
        }
        resources {
        	srcDir("src/test/resources")
        }
    }    
}

touch(File(buildDir, "generated/sources/js/portal-all.js"))

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

tasks.register<JavaExec>("compress-and-append-prototype") {
	val fileName = "prototype.js"
	val dirName = "${projectDir}/src/js/org/prototypejs"
	classpath = fileTree(
		File("${projectDir}/etc/yuicompressor", "yuicompressor.jar")
	)
    args = listOf(
        "--line-break", "1000",
        "-o", "$buildDir/generated/sources/js/${fileName}",
        "${dirName}/${fileName}"
    )
}

tasks.register<JavaExec>("compress-and-append-ssf") {
	val fileName = "ssf.js"
	val dirName = "${projectDir}/src/js/org/openmdx/portal"
	classpath = fileTree(
		File("${projectDir}/etc/yuicompressor", "yuicompressor.jar")
	)
    args = listOf(
        "--line-break", "1000",
        "-o", "$buildDir/generated/sources/js/${fileName}",
        "${dirName}/${fileName}"
    )
}

tasks.register<JavaExec>("compress-and-append-calendar") {
	val fileName = "calendar.js"
	val dirName = "${projectDir}/src/js/com/dynarch/calendar"
	classpath = fileTree(
		File("${projectDir}/etc/yuicompressor", "yuicompressor.jar")
	)
    args = listOf(
        "--line-break", "1000",
        "-o", "$buildDir/generated/sources/js/${fileName}",
        "${dirName}/${fileName}"
    )
}

tasks.register<JavaExec>("compress-and-append-calendar-setup") {
	val fileName = "calendar-setup.js"
	val dirName = "${projectDir}/src/js/com/dynarch/calendar"
	classpath = fileTree(
		File("${projectDir}/etc/yuicompressor", "yuicompressor.jar")
	)
    args = listOf(
        "--line-break", "1000",
        "-o", "$buildDir/generated/sources/js/${fileName}",
        "${dirName}/${fileName}"
    )
}

tasks.register<JavaExec>("compress-and-append-guicontrol") {
	val fileName = "guicontrol.js"
	val dirName = "${projectDir}/src/js/org/openmdx/portal"
	classpath = fileTree(
		File("${projectDir}/etc/yuicompressor", "yuicompressor.jar")
	)
    args = listOf(
        "--line-break", "1000",
        "-o", "$buildDir/generated/sources/js/${fileName}",
        "${dirName}/${fileName}"
    )
}

tasks.register<JavaExec>("compress-and-append-wiky") {
	val fileName = "wiky.js"
	val dirName = "${projectDir}/src/js/net/wiky"
	classpath = fileTree(
		File("${projectDir}/etc/yuicompressor", "yuicompressor.jar")
	)
    args = listOf(
        "--line-break", "1000",
        "-o", "$buildDir/generated/sources/js/${fileName}",
        "${dirName}/${fileName}"
    )
}

tasks.register<JavaExec>("compress-and-append-wiky-lang") {
	val fileName = "wiky.lang.js"
	val dirName = "${projectDir}/src/js/net/wiky"
	classpath = fileTree(
		File("${projectDir}/etc/yuicompressor", "yuicompressor.jar")
	)
    args = listOf(
        "--line-break", "1000",
        "-o", "$buildDir/generated/sources/js/${fileName}",
        "${dirName}/${fileName}"
    )
}

tasks.register<JavaExec>("compress-and-append-wiky-math") {
	val fileName = "wiky.math.js"
	val dirName = "${projectDir}/src/js/net/wiky"
	classpath = fileTree(
		File("${projectDir}/etc/yuicompressor", "yuicompressor.jar")
	)
    args = listOf(
        "--line-break", "1000",
        "-o", "$buildDir/generated/sources/js/${fileName}",
        "${dirName}/${fileName}"
    )
}

tasks.register("compress-and-append-js") {
	dependsOn("compress-and-append-prototype")
	dependsOn("compress-and-append-ssf")
	dependsOn("compress-and-append-calendar")
	dependsOn("compress-and-append-calendar-setup")
	dependsOn("compress-and-append-guicontrol")
	dependsOn("compress-and-append-wiky")
	dependsOn("compress-and-append-wiky-lang")
	dependsOn("compress-and-append-wiky-math")
    doLast {
    	val f = File(projectDir, "src/war/openmdx-inspector.war/js/portal-all.js")
        f.writeText(File("$buildDir/generated/sources/js/prototype.js").readText())
        f.appendText(File("$buildDir/generated/sources/js/ssf.js").readText())
        f.appendText(File("$buildDir/generated/sources/js/calendar.js").readText())
        f.appendText(File("$buildDir/generated/sources/js/calendar-setup.js").readText())
        f.appendText(File("$buildDir/generated/sources/js/guicontrol.js").readText())
        f.appendText(File("$buildDir/generated/sources/js/wiky.js").readText())
        f.appendText(File("$buildDir/generated/sources/js/wiky.lang.js").readText())
        f.appendText(File("$buildDir/generated/sources/js/wiky.math.js").readText())
    }
}

tasks.compileJava {
    dependsOn("generate-model")
    options.release.set(Integer.valueOf(targetPlatform.getMajorVersion()))
}

tasks {
	assemble {
		dependsOn(
            "openmdx-portal.jar",
            "openmdx-portal-sources.jar",
            "openmdx-inspector.war"
        )
	}
}

val openmdxPortalIncludes = listOf<String>(  
    "org/openmdx/portal/*/**",
    "org/openmdx/ui1/*/**",
    "META-INF/orm.xml",
    "META-INF/openmdxmof.properties"
)

val openmdxPortalExcludes = listOf<String>(
)

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-portal.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-portal.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Portal Library",
        		"openmdx-portal"
        	)
        )
    }
	from(
		File(buildDir, "classes/java/main"),
		File(buildDir, "resources/main"),
		"src/main/resources",
		zipTree(File(buildDir, "generated/sources/model/openmdx-" + project.getName() + ".openmdx-xmi.zip"))
	)
	include(openmdxPortalIncludes)
	exclude(openmdxPortalExcludes)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-portal-sources.jar") {
	destinationDirectory.set(File(getDeliverDir(), "lib"))
	archiveFileName.set("openmdx-portal-sources.jar")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Portal Sources",
        		"openmdx-portal-sources"
        	)
        )
    }
	from(
		"src/main/java",
		File(buildDir, "generated/sources/java/main")
	)
	include(openmdxPortalIncludes)
	exclude(openmdxPortalExcludes)
}

tasks.register<org.openmdx.gradle.ArchiveTask>("openmdx-inspector.war") {
	dependsOn("compress-and-append-js")
	destinationDirectory.set(File(getDeliverDir(), "deployment-unit"))
	archiveFileName.set("openmdx-inspector.war")
    includeEmptyDirs = false
	manifest {
        attributes(
        	getManifest(
        		"openMDX Inspector Library",
        		"openmdx-inspector"
        	)
        )
    }
    with(
    	copySpec {
			from("src/war/openmdx-inspector.war")
		},
    	copySpec {
			from("src/js/org/openmdx")    	
			into("js/openmdx")
    	},
    	copySpec {
			from("src/js/org/wymeditor")    	
			into("js/wymeditor")
    	},
    	copySpec {
			from("src/js/net/wiky")    	
			into("js/wiky")
    	},
    	copySpec {
			from("src/js/org/jit")    	
			into("js/jit")
    	},
    	copySpec {
			from("src/js/com/dynarch/calendar/lang")    	
			into("js/calendar/lang")
    	},
    	copySpec {
			from("src/js/org/prototypejs")    	
			into("js")
    	},
    	copySpec {
			from("src/js/com/yahoo/yui/assets")    	
			into("js/yui/build/assets")
    	},
    	copySpec {
			from("src/js/com/bootstrap")    	
			into("js/bootstrap")
    	},
    	copySpec {
			from("src/js/com/jquery")    	
			into("js/jquery")
    	},
    	copySpec {
			from("src/js/com/popper")    	
			into("js/popper")
    	},
    	copySpec {
			from("src/js/org/openmdx/portal")    	
			into("js")
			include(
				"ssf.js",
				"guicontrol.js"
			)
    	}
    )
}

distributions {
    main {
    	distributionBaseName.set("openmdx-" + getProjectImplementationVersion() + "-" + project.getName() + "-jre-" + targetPlatform)
        contents {
        	// portal
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
