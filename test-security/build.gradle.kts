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
    	name = "openMDX $projectFlavour ~ Test Security"
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
    // implementation
    implementation(project(":core"))
    implementation(project(":security"))
    implementation(platform(project(projectPlatform)))
    implementation("jakarta.platform:jakarta.jakartaee-api")
    implementation("org.apache.directory.api:apache-ldap-api")
    implementation("org.tinyradius:tinyradius")
    implementation("org.junit.jupiter:junit-jupiter-api")
    // manifold preprocessor
    compileOnly("systems.manifold:manifold-preprocessor")
    annotationProcessor(platform(project(projectPlatform)))
    annotationProcessor("systems.manifold:manifold-preprocessor")
    // test
    testImplementation(project(":core"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testRuntimeOnly("com.atomikos:transactions-jta")
    testRuntimeOnly("com.atomikos:transactions-jdbc")
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
	assemble {
		dependsOn()
	}
    named("processResources", Copy::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    named("processTestResources", Copy::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
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
