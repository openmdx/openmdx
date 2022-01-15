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
    	name = "openMDX 2 ~ Tomcat"
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
    implementation(project(":security"))
    implementation("javax:javaee-api:8.0.+")
    implementation("org.apache.tomcat:tomcat-catalina:9.0.+")
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

val openmdxCatalinaIncludes = listOf(
    "org/openmdx/catalina/*/**"
)

val openmdxCatalinaExcludes = listOf<String>( )

tasks {
	test {
	    useJUnitPlatform()
	    maxHeapSize = "4G"
	}
	compileJava {
 	   options.release.set(Integer.valueOf(targetPlatform.getMajorVersion()))
	}
	assemble {
		dependsOn(
            "catalina-openmdx.jar",
            "catalina-openmdx-sources.jar"
        )
	}
	distTar {
		dependsOn(
			"catalina-openmdx.jar",
			"catalina-openmdx-sources.jar"
		)
	}
	distZip {
		dependsOn(
			"catalina-openmdx.jar",
			"catalina-openmdx-sources.jar"
		)
	}
	register<org.openmdx.gradle.ArchiveTask>("catalina-openmdx.jar") {
	    dependsOn(
	        ":tomcat:compileJava",
	        ":tomcat:processResources"
	    )
		destinationDirectory.set(File(getDeliverDir(), "lib"))
		archiveFileName.set("catalina-openmdx.jar")
	    includeEmptyDirs = false
		manifest {
	        attributes(
	        	getManifest(
	        		"openMDX Catalina",
	        		"catalina-openmdx"
	        	)
	        )
	    }
		from(
			File(buildDir, "classes/java/main"),
			File(buildDir, "resources/main"),
			"src/main/resources"
		)
		include(openmdxCatalinaIncludes)
		exclude(openmdxCatalinaExcludes)
	}
	register<org.openmdx.gradle.ArchiveTask>("catalina-openmdx-sources.jar") {
		destinationDirectory.set(File(getDeliverDir(), "lib"))
		archiveFileName.set("catalina-openmdx-sources.jar")
	    includeEmptyDirs = false
		manifest {
	        attributes(
	        	getManifest(
	        		"openMDX Catalina Sources",
	        		"catalina-openmdx-sources"
	        	)
	        )
	    }
		from(
			"src/main/java",
			File(buildDir, "generated/sources/java/main")
		)
		include(openmdxCatalinaIncludes)
		exclude(openmdxCatalinaExcludes)
	}
}

distributions {
    main {
    	distributionBaseName.set("openmdx-" + getProjectImplementationVersion() + "-" + project.getName() + "-jre-" + targetPlatform)
        contents {
        	// test-core
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
