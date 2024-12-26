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
    	name = "openMDX ${projectFlavour} ~ Security"
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
	implementation(platform(project(projectPlatform)))
    implementation("jakarta.platform:jakarta.jakartaee-api")
    implementation("javax.jdo:jdo-api")
    implementation("org.apache.directory.api:apache-ldap-api")
    // Test
	testRuntimeOnly(platform(project(projectPlatform)))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
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
	    inputs.dir("$projectDir/src/model/emf")
	    inputs.dir("$projectDir/src/main/resources")
	    outputs.file(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}-models.zip"))
	    outputs.file(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}.openmdx-xmi.zip"))
	    classpath = configurations["openmdxBootstrap"]
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
	    }
	}
	compileJava {
	    dependsOn("generate-model")
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

	named("processResources", Copy::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	named("processTestResources", Copy::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

	val openmdxSecurityIncludes = listOf(
		"org/openmdx/security/*/**",
		"org/openmdx/uses/layout/*",
		"META-INF/orm.xml",
		"META-INF/openmdxmof.properties"
	)

	val openmdxSecurityExcludes = listOf(
		"org/openmdx/security/auth/callback/*/**",
		"org/openmdx/security/auth/passcode/*/**",
		"org/openmdx/security/radius/*/**"
	)

	register<org.openmdx.gradle.ArchiveTask>("openmdx-security.jar") {
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		dependsOn(
			":security:compileJava",
			":security:generate-model",
			":security:processResources"
		)
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "classes/main/java"),
			File(buildDirAsFile, "resources/main"),
			"src/main/resources",
			zipTree(layout.buildDirectory.dir("generated/sources/model/openmdx-" + project.name + ".openmdx-xmi.zip"))
		)
		include(openmdxSecurityIncludes)
		exclude(openmdxSecurityExcludes)
	}

	register<org.openmdx.gradle.ArchiveTask>("openmdx-security-sources.jar") {
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "generated/sources/main/java")
		)
		include(openmdxSecurityIncludes)
		exclude(openmdxSecurityExcludes)
	}

	val openmdxAuthenticationIncludes = listOf(
		"org/openmdx/security/auth/callback/*/**",
		"org/openmdx/security/auth/passcode/*/**"
	)

	val openmdxAuthenticationExcludes = listOf<String>( )

	register<org.openmdx.gradle.ArchiveTask>("openmdx-authentication.jar") {
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		dependsOn(
			":security:compileJava",
			":security:processResources"
		)
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "classes/main/java"),
			File(buildDirAsFile, "resources/main"),
			"src/main/resources"
		)
		include(openmdxAuthenticationIncludes)
		exclude(openmdxAuthenticationExcludes)
	}

	register<org.openmdx.gradle.ArchiveTask>("openmdx-authentication-sources.jar") {
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "generated/sources/main/java")
		)
		include(openmdxAuthenticationIncludes)
		exclude(openmdxAuthenticationExcludes)
	}

	val openmdxRadiusIncludes = listOf(
		"org/openmdx/kernel/collection/*/**",
		"org/openmdx/kernel/text/*/**",
		"org/openmdx/resource/radius/*/**",
		"org/openmdx/security/radius/*/**",
		"org/openmdx/uses/net/sourceforge/jradiusclient/*/**",
		"org/openmdx/uses/org/apache/commons/collections/*/**",
		"org/openmdx/uses/org/apache/commons/pool/*/**"
	)

	val openmdxRadiusExcludes = listOf<String>( )

	register<org.openmdx.gradle.ArchiveTask>("openmdx-radius.jar") {
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "classes/main/java"),
			File(buildDirAsFile, "resources/main"),
			"src/main/resources",
			zipTree(File(project.rootDir, "build${projectFlavour}/core/lib/openmdx-base.jar"))
		)
		include(openmdxRadiusIncludes)
		exclude(openmdxRadiusExcludes)
	}

	register<org.openmdx.gradle.ArchiveTask>("openmdx-radius-sources.jar") {
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "generated/sources/main/java")
		)
		include(openmdxRadiusIncludes)
		exclude(openmdxRadiusExcludes)
	}

	val openmdxLdapIncludes = listOf(
		"org/openmdx/resource/ldap/*/**"
	)
	val openmdxLdapExcludes = listOf<String>( )

	register<org.openmdx.gradle.ArchiveTask>("openmdx-ldap.jar") {
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "classes/main/java"),
			File(buildDirAsFile, "resources/main"),
			"src/main/resources"
		)
		include(openmdxLdapIncludes)
		exclude(openmdxLdapExcludes)
	}

	register<org.openmdx.gradle.ArchiveTask>("openmdx-ldap-sources.jar") {
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "generated/sources/main/java")
		)
		include(openmdxLdapIncludes)
		exclude(openmdxLdapExcludes)
	}

	val openmdxPkiIncludes = listOf(
		"org/openmdx/resource/pki/*/**"
	)

	val openmdxPkiExcludes = listOf<String>( )

	register<org.openmdx.gradle.ArchiveTask>("openmdx-pki.jar") {
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "classes/main/java"),
			File(buildDirAsFile, "resources/main"),
			"src/main/resources"
		)
		include(openmdxPkiIncludes)
		exclude(openmdxPkiExcludes)
	}

	register<org.openmdx.gradle.ArchiveTask>("openmdx-pki-sources.jar") {
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "generated/sources/main/java")
		)
		include(openmdxPkiIncludes)
		exclude(openmdxPkiExcludes)
	}

	val openmdxResourceIncludes = listOf(
		"org/openmdx/resource/cci/*/**",
		"org/openmdx/resource/spi/*/**"
	)
	val openmdxResourceExcludes = listOf<String>( )

	register<org.openmdx.gradle.ArchiveTask>("openmdx-resource.jar") {
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "classes/main/java"),
			File(buildDirAsFile, "resources/main"),
			"src/main/resources"
		)
		include(openmdxResourceIncludes)
		exclude(openmdxResourceExcludes)
	}
	register<org.openmdx.gradle.ArchiveTask>("openmdx-resource-sources.jar") {
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(buildDirAsFile, "generated/sources/main/java")
		)
		include(openmdxResourceIncludes)
		exclude(openmdxResourceExcludes)
	}
}

distributions {
    main {
    	distributionBaseName.set("openmdx-${project.version}-${project.name}-jre-${runtimeCompatibility}")
        contents {
        	// security
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
