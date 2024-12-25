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

repositories {
	mavenCentral()
    maven {
        url = uri("https://datura.econoffice.ch/maven2")
    }
	maven {
		url = uri("file:" + File(project.rootDir, "publish/build/repos/releases"))
	}
}

val projectFlavour = project.extra["projectFlavour"] as String
val projectSpecificationVersion = project.extra["projectSpecificationVersion"] as String
val projectMaintenanceVersion = project.extra["projectMaintenanceVersion"] as String
val runtimeCompatibility = project.extra["runtimeCompatibility"] as JavaVersion

eclipse {
	project {
    	name = "openMDX $projectFlavour ~ Portal"
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
	implementation(platform(project(projectPlatform)))
	implementation(project(":core"))
	implementation("jakarta.platform:jakarta.jakartaee-api")
	implementation("javax.jdo:jdo-api")
	implementation("org.codehaus.groovy:groovy")
	// test
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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
			srcDir(layout.buildDirectory.dir("generated/sources/main/java"))
		}
		resources {
			srcDir("src/main/resources")
			srcDir(layout.buildDirectory.dir("generated/resources/main"))
		}
	}
	test {
		java {
			srcDir("src/test/java")
			srcDir(layout.buildDirectory.dir("generated/sources/test/java"))
		}
		resources {
			srcDir("src/test/resources")
		}
	}
}

touch(file(layout.buildDirectory.dir("generated/sources/js/portal-all.js")))

tasks {
	test {
		useJUnitPlatform()
		maxHeapSize = "4G"
	}
	distTar {
		dependsOn(
			":portal:openmdx-portal.jar",
			":portal:openmdx-portal-sources.jar"
		)
	}
	distZip {
		dependsOn(
			":portal:openmdx-portal.jar",
			":portal:openmdx-portal-sources.jar"
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
	            into(layout.buildDirectory.dir("generated/sources/main/java"))
	            include(
	                "**/*.java"
	            )
	        }
	    }
	}

	val buildDirAsFile = layout.buildDirectory.asFile.get()

	register<JavaExec>("compress-and-append-prototype") {
		val fileName = "prototype.js"
		val dirName = "$projectDir/src/js/org/prototypejs"
		classpath = fileTree(
			File("$projectDir/etc/yuicompressor", "yuicompressor.jar")
		)
	    args = listOf(
	        "--line-break", "1000",
	        "-o", "$buildDirAsFile/generated/sources/js/$fileName",
	        "$dirName/$fileName"
	    )
	}
	register<JavaExec>("compress-and-append-ssf") {
		val fileName = "ssf.js"
		val dirName = "$projectDir/src/js/org/openmdx/portal"
		classpath = fileTree(
			File("$projectDir/etc/yuicompressor", "yuicompressor.jar")
		)
	    args = listOf(
	        "--line-break", "1000",
	        "-o", "$buildDirAsFile/generated/sources/js/$fileName",
	        "$dirName/$fileName"
	    )
	}
	register<JavaExec>("compress-and-append-calendar") {
		val fileName = "calendar.js"
		val dirName = "$projectDir/src/js/com/dynarch/calendar"
		classpath = fileTree(
			File("$projectDir/etc/yuicompressor", "yuicompressor.jar")
		)
	    args = listOf(
	        "--line-break", "1000",
	        "-o", "$buildDirAsFile/generated/sources/js/$fileName",
	        "$dirName/$fileName"
	    )
	}
	register<JavaExec>("compress-and-append-calendar-setup") {
		val fileName = "calendar-setup.js"
		val dirName = "$projectDir/src/js/com/dynarch/calendar"
		classpath = fileTree(
			File("$projectDir/etc/yuicompressor", "yuicompressor.jar")
		)
	    args = listOf(
	        "--line-break", "1000",
	        "-o", "$buildDirAsFile/generated/sources/js/$fileName",
	        "$dirName/$fileName"
	    )
	}
	register<JavaExec>("compress-and-append-guicontrol") {
		val fileName = "guicontrol.js"
		val dirName = "$projectDir/src/js/org/openmdx/portal"
		classpath = fileTree(
			File("$projectDir/etc/yuicompressor", "yuicompressor.jar")
		)
	    args = listOf(
	        "--line-break", "1000",
	        "-o", "$buildDirAsFile/generated/sources/js/$fileName",
	        "$dirName/$fileName"
	    )
	}
	register<JavaExec>("compress-and-append-wiky") {
		val fileName = "wiky.js"
		val dirName = "$projectDir/src/js/net/wiky"
		classpath = fileTree(
			File("$projectDir/etc/yuicompressor", "yuicompressor.jar")
		)
	    args = listOf(
	        "--line-break", "1000",
	        "-o", "$buildDirAsFile/generated/sources/js/$fileName",
	        "$dirName/$fileName"
	    )
	}
	register<JavaExec>("compress-and-append-wiky-lang") {
		val fileName = "wiky.lang.js"
		val dirName = "$projectDir/src/js/net/wiky"
		classpath = fileTree(
			File("$projectDir/etc/yuicompressor", "yuicompressor.jar")
		)
	    args = listOf(
	        "--line-break", "1000",
	        "-o", "$buildDirAsFile/generated/sources/js/$fileName",
	        "$dirName/$fileName"
	    )
	}
	register<JavaExec>("compress-and-append-wiky-math") {
		val fileName = "wiky.math.js"
		val dirName = "$projectDir/src/js/net/wiky"
		classpath = fileTree(
			File("$projectDir/etc/yuicompressor", "yuicompressor.jar")
		)
	    args = listOf(
	        "--line-break", "1000",
	        "-o", "$buildDirAsFile/generated/sources/js/$fileName",
	        "$dirName/$fileName"
	    )
	}
	register("compress-and-append-js") {
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
	        f.writeText(file(layout.buildDirectory.dir("generated/sources/js/prototype.js")).readText())
	        f.appendText(file(layout.buildDirectory.dir("generated/sources/js/ssf.js")).readText())
	        f.appendText(file(layout.buildDirectory.dir("generated/sources/js/calendar.js")).readText())
	        f.appendText(file(layout.buildDirectory.dir("generated/sources/js/calendar-setup.js")).readText())
	        f.appendText(file(layout.buildDirectory.dir("generated/sources/js/guicontrol.js")).readText())
	        f.appendText(file(layout.buildDirectory.dir("generated/sources/js/wiky.js")).readText())
	        f.appendText(file(layout.buildDirectory.dir("generated/sources/js/wiky.lang.js")).readText())
	        f.appendText(file(layout.buildDirectory.dir("generated/sources/js/wiky.math.js")).readText())
	    }
	}
	compileJava {
	    dependsOn("generate-model")
	}
	assemble {
		dependsOn(
            "openmdx-portal.jar",
            "openmdx-portal-sources.jar",
            "openmdx-inspector.war"
        )
	}

	val openmdxPortalIncludes = listOf(
		"org/openmdx/portal/*/**",
		"org/openmdx/ui1/*/**",
		"META-INF/orm.xml",
		"META-INF/openmdxmof.properties"
	)

	val openmdxPortalExcludes = listOf<String>( )

	named("processResources", Copy::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	named("processTestResources", Copy::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	register<org.openmdx.gradle.ArchiveTask>("openmdx-portal.jar") {
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		dependsOn(
			":portal:compileJava",
			":portal:generate-model",
			":portal:processResources"
		)
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(this.buildDirAsFile, "classes/main/java"),
			File(this.buildDirAsFile, "resources/main"),
			"src/main/resources",
			zipTree(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}.openmdx-xmi.zip"))
		)
		include(openmdxPortalIncludes)
		exclude(openmdxPortalExcludes)
	}
	register<org.openmdx.gradle.ArchiveTask>("openmdx-portal-sources.jar") {
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/lib"))
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
			File(this.buildDirAsFile, "generated/sources/main/java")
		)
		include(openmdxPortalIncludes)
		exclude(openmdxPortalExcludes)
	}
	register<org.openmdx.gradle.ArchiveTask>("openmdx-inspector.war") {
		dependsOn("compress-and-append-js")
		destinationDirectory.set(File(project.rootDir, "build${projectFlavour}/${project.name}/deployment-unit"))
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
}

distributions {
    main {
		distributionBaseName.set("openmdx-${project.version}-${project.name}-jre-${runtimeCompatibility}")
        contents {
        	// portal
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
			var path = "jre-${runtimeCompatibility}/${project.name}/lib"
			from("../$path") { into(path) }
			path = "jre-${runtimeCompatibility}/gradle/repo"
			from("../$path") { into(path) }
        }
    }
}
