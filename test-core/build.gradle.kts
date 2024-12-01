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

import java.io.FileInputStream
import java.util.*

plugins {
	java
	`java-library`
	eclipse
	distribution
}

repositories {
	mavenCentral()
    maven {
        url = uri("https://datura.econoffice.ch/maven2")
    }
}

var env = Properties()
env.load(FileInputStream(File(project.rootDir, "build.properties")))
val targetPlatform = JavaVersion.valueOf(env.getProperty("target.platform"))

eclipse {
	project {
    	name = "openMDX 2 ~ Test Core"
    }
    jdt {
		sourceCompatibility = targetPlatform
    	targetCompatibility = targetPlatform
    	javaRuntimeName = "JavaSE-$targetPlatform"
    }
}

fun getProjectImplementationVersion(): String {
	return project.version.toString();
}

fun getDeliverDir(): File {
	return File(project.rootDir, "jre-" + targetPlatform + "/" + project.name);
}

fun touch(file: File) {
	ant.withGroovyBuilder { "touch"("file" to file, "mkdirs" to true) }
}

project.configurations.maybeCreate("openmdxBootstrap")
val openmdxBootstrap by configurations

dependencies {
    // main
    implementation(project(":core"))
    implementation("javax:javaee-api:8.0.+")
    implementation("javax.jdo:jdo-api:3.1")
    implementation("javax.cache:cache-api:1.1.+")    
    implementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    // test
    testImplementation(project(":core"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    testImplementation("org.mockito:mockito-core:5.14.2")    
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")    
 	testRuntimeOnly("org.postgresql:postgresql:42.7.+")
	testRuntimeOnly("javax.servlet:javax.servlet-api:3.1.0")
	testRuntimeOnly("com.atomikos:transactions-jta:6.0.0")
	testRuntimeOnly("com.atomikos:transactions-jdbc:6.0.0")
    // openmdxBootstrap
    openmdxBootstrap(project(":core"))
}

sourceSets {
    main {
        java {
            srcDir("src/main/java")
            srcDir(layout.buildDirectory.dir("generated/sources/java/main"))
        }
        resources {
        	srcDir("src/main/resources")
            srcDir(layout.buildDirectory.dir("generated/resources/main"))
        }
    }
    test {
        java {
            srcDir("src/test/java")
            srcDir(layout.buildDirectory.dir("generated/sources/java/test"))
        }
        resources {
        	srcDir("src/test/resources")
        }
    }
    create("openmdxDatatype") {
    	java {
        	srcDir("src/model/java")
    	}
    }
}

tasks.named<AbstractCompile>("compileOpenmdxDatatypeJava") {
    classpath = configurations["openmdxBootstrap"]
}

tasks.withType<Test> {
    this.classpath.forEach { println(it) }
}
tasks.test {
    useJUnitPlatform()
    maxHeapSize = "4G"
}

project.tasks.named("processResources", Copy::class.java) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
project.tasks.named("processTestResources", Copy::class.java) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<org.openmdx.gradle.GenerateModelsTask>("generate-model") {
	dependsOn("openmdxDatatypeClasses")
    inputs.dir("$projectDir/src/model/emf")
    inputs.dir("$projectDir/src/main/resources")
    outputs.file(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}-models.zip"))
    outputs.file(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}.openmdx-xmi.zip"))
    classpath(configurations["openmdxBootstrap"])
    classpath(sourceSets["openmdxDatatype"].runtimeClasspath)
	args = listOf(
		"--pathMapSymbol=openMDX 2 ~ Core (EMF)",
		"--pathMapPath=file:" + File(project.rootDir, "core/src/model/emf") + "/",
		"--pathMapSymbol=openMDX 2 ~ Security (EMF)",
		"--pathMapPath=file:" + File(project.rootDir, "security/src/model/emf") + "/",
		"--pathMapSymbol=openMDX 2 ~ Portal (EMF)",
		"--pathMapPath=file:" + File(project.rootDir, "portal/src/model/emf") + "/",
		"--url=file:src/model/emf/models.uml",
		"--xmi=emf",
		"--out=" + File(project.getBuildDir(), "generated/sources/model/openmdx-${project.name}-models.zip"),
		"--openmdxjdo=" + File(project.projectDir, "src/main/resources"),
		"--dataproviderVersion=2",
		"--format=xmi1",
	    "--format=test.openmdx.application.mof.mapping.java.PrimitiveTypeMapper_1(cci2)",
	    "--format=test.openmdx.application.mof.mapping.java.PrimitiveTypeMapper_1(jmi1)",
	    "--format=test.openmdx.application.mof.mapping.java.PrimitiveTypeMapper_1(jpa3)",
		"--format=mof1",            
		"%"
	)
    doFirst {
    }
    doLast {
        copy {
            from(
                zipTree(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}-models.zip"))
            )
            into(layout.buildDirectory.dir("generated/sources/java/main"))
            include(
                "**/*.java"
            )
        }
        copy {
            from(
                zipTree(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}.openmdx-xmi.zip"))
            )
            into(layout.buildDirectory.dir("generated/resources/main"))
        }
    }
}

tasks.compileJava {
    dependsOn("generate-model")
    options.release.set(Integer.valueOf(targetPlatform.majorVersion))
}


tasks {
	assemble {
		dependsOn(
        )
	}
}

distributions {
    main {
    	distributionBaseName.set("openmdx-" + getProjectImplementationVersion() + "-${project.name}-jre-" + targetPlatform)
        contents {
        	// test-core
        	from(".") { into(project.name); include("LICENSE", "*.LICENSE", "NOTICE", "*.properties", "build*.*", "*.xml", "*.kts") }
            from("src") { into(project.name + "/src") }
            // etc
            from("etc") { into(project.name + "/etc") }
            // rootDir
            from("..") { include("*.properties", "*.kts" ) }
            // jre-...
            var path = "jre-$targetPlatform/${project.name}/lib"
            from("../$path") { into(path) }
            path = "jre-$targetPlatform/gradle/repo"
            from("../$path") { into(path) }
        }
    }
}
