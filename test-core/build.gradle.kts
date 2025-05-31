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
	java
	`java-library`
	eclipse
	distribution
}

val projectFlavour = project.extra["projectFlavour"] as String
val projectSpecificationVersion = project.extra["projectSpecificationVersion"] as String
val projectMaintenanceVersion = project.extra["projectMaintenanceVersion"] as String
val runtimeCompatibility = project.extra["runtimeCompatibility"] as JavaVersion

eclipse {
	project {
    	name = "openMDX $projectFlavour ~ Test Core"
    }
    jdt {
        sourceCompatibility = runtimeCompatibility
        targetCompatibility = runtimeCompatibility
        javaRuntimeName = "JavaSE-${runtimeCompatibility.majorVersion}"
    }
}

fun touch(file: File) {
	ant.withGroovyBuilder { "touch"("file" to file, "mkdirs" to true) }
}

project.configurations.maybeCreate("openmdxBootstrap")
val openmdxBootstrap by configurations

dependencies {
    val projectPlatform = ":openmdx-${projectFlavour}-platform"
    // main
    implementation(project(":core"))
    implementation(platform(project(projectPlatform)))
    implementation("jakarta.platform:jakarta.jakartaee-api")
    if(runtimeCompatibility.isJava8()) {
        implementation(group= "javax.jdo", name = "jdo-api")
    } else {
        implementation(group= "javax.jdo", name = "jdo-api"){
            exclude(group = "javax.transaction", module = "transaction-api")
        }
    }
    implementation("javax.cache:cache-api")
    implementation("org.junit.jupiter:junit-jupiter-api")
    // test
    testImplementation(project(":core"))
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("jakarta.servlet:jakarta.servlet-api")
	if(runtimeCompatibility.isJava8()) {
		testRuntimeOnly(group = "com.atomikos", name = "transactions-jta")
		testRuntimeOnly(group = "com.atomikos", name = "transactions-jdbc")
        testRuntimeOnly(group = "com.oracle.database.jdbc", name = "ojdbc8")
	} else {
		testRuntimeOnly(group = "com.atomikos", name = "transactions-jta", classifier = "jakarta")
		testRuntimeOnly(group = "com.atomikos", name = "transactions-jdbc", classifier = "jakarta")
        testRuntimeOnly(group = "com.oracle.database.jdbc", name = "ojdbc17")
	}
    // manifold preprocessor
    compileOnly("systems.manifold:manifold-preprocessor")
    annotationProcessor(platform(project(projectPlatform)))
    annotationProcessor("systems.manifold:manifold-preprocessor")
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
            srcDir("src/test/openmdx-${projectFlavour}/java")
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
		"--flavour=" + projectFlavour,
		"--pathMapSymbol=openMDX 2 ~ Core (EMF)",
		"--pathMapPath=file:" + File(project.rootDir, "core/src/model/emf") + "/",
		"--pathMapSymbol=openMDX 2 ~ Security (EMF)",
		"--pathMapPath=file:" + File(project.rootDir, "security/src/model/emf") + "/",
		"--pathMapSymbol=openMDX 2 ~ Portal (EMF)",
		"--pathMapPath=file:" + File(project.rootDir, "portal/src/model/emf") + "/",
		"--url=file:src/model/emf/models.uml",
		"--xmi=emf",
		"--out=" + project.layout.buildDirectory.file("generated/sources/model/openmdx-${project.name}-models.zip").get().asFile,
		"--openmdxjdo=" + File(project.projectDir, "src/main/resources"),
		"--format=xmi1",
	    "--format=test.openmdx.application.mof.mapping.java.MyMapper(cci2)",
	    "--format=test.openmdx.application.mof.mapping.java.MyMapper(jmi1)",
	    "--format=test.openmdx.application.mof.mapping.java.MyMapper(jpa3)",
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
}


tasks {
	assemble {
		dependsOn(
        )
	}
}

distributions {
    main {
        distributionBaseName.set("openmdx-${project.version}-${project.name}-jre-${runtimeCompatibility}")
        contents {
        	// test-core
        	from(".") {
                into(project.name)
                include("LICENSE", "*.LICENSE", "NOTICE", "*.properties", "build*.*", "*.xml", "*.kts")
            }
            from("src") { into("${project.name}/src") }
            // etc
            from("etc") { into("${project.name}/etc") }
            // rootDir
            from("..") { include("*.properties", "*.kts" ) }
            // jre-...
            var path = "${project.name}/lib"
            from("../build$projectFlavour/$path") {
                into("jre-$runtimeCompatibility/$path")
            }
            path = "gradle/repo"
            from("../build$projectFlavour/$path") {
                into("jre-$runtimeCompatibility/$path")
            }
        }
    }
}
