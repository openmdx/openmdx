/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: build.gradle.kts
 * Owner:       the original authors-
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
    id("com.microsoft.javaflavours")
    id("systems.manifold.manifold-gradle-plugin")
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
val targetPlatformOpenmdx4 = JavaVersion.valueOf(env.getProperty("target.platform.openmdx4"))

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
project.configurations.maybeCreate("javaeeApi")
val javaeeApi by configurations

configurations {
    // needed
    register("compile")
    register("testCompile")
    register("runtime")
    register("testRuntime")
    for (i in 2..4) {
        register("openmdx${i}Compile")
        register("openmdx${i}TestCompile")
        register("openmdx${i}Runtime")
        register("openmdx${i}TestRuntime")
    }
}

configure<com.microsoft.javaflavours.JavaFlavoursExtension> {
    flavour("${project.extra["mdx2"]}")
    flavour("${project.extra["mdx3"]}")
    flavour("${project.extra["mdx4"]}")
    val testJavaPathResolver: (String) -> String = { flavour -> "src/$flavour-test/java" }
    val testResourcesPathResolver: (String) -> String = { flavour -> "src/$flavour-test/resources" }
}

sourceSets {
//	main {
//    java { srcDir("src/main/java"); srcDir(layout.buildDirectory.dir("generated/sources/java/main")) }
//    resources { srcDir("src/main/resources") }
//	}
    val openmdx2 by getting {
        java { srcDir("src/main/java"); srcDir(layout.buildDirectory.dir("generated/sources/java/main")) }
        resources { srcDir("src/main/resources") }
    }

    val openmdx3 by getting {
        java { srcDir("src/main/java"); srcDir(layout.buildDirectory.dir("generated/sources/java/main")) }
        resources { srcDir("src/main/resources") }
    }

    val openmdx4 by getting {
        java { srcDir("src/main/java"); srcDir(layout.buildDirectory.dir("generated/sources/java/main")) }
        resources { srcDir("src/main/resources") }
    }
}

dependencies {
    // implementation
    implementation("javax:javaee-api:8.0.+")
    implementation(project(":core"))
    implementation(project(":security"))
	implementation(files(File(System.getenv("JRE_18"), "lib/rt.jar")))
//	"openmdx2Implementation"(files(File(System.getenv("JRE_18"), "lib/rt.jar")))
//	"openmdx3Implementation"(files(File(System.getenv("JRE_18"), "lib/rt.jar")))
    // manifold preprocessor
    implementation(libs.systems.manifold.preprocessor)
    implementation(libs.systems.manifold.props.rt)
    implementation(libs.systems.manifold.ext)

    // javaee-api
    javaeeApi(libs.javax.javaee.api)
    implementation(libs.javax.javaee.api)

//	"openmdx2Implementation"(javaee)
//	"openmdx3Implementation"(javaee)

//	"openmdx4Implementation"("javax:javaee-api:8.0.+")
	"openmdx4Implementation"(libs.jakarta.platform.jakartaee.api)

//	"openmdx3Implementation"("org.apache.commons:commons-lang3:3.12.0")

}

sourceSets {
    main {
        java {
            srcDir("src/main/java")
			srcDir(layout.buildDirectory.dir("generated/sources/java/main"))
tasks.withType<JavaCompile> {
//	options.compilerArgs.add("-Xplugin:Manifold")
    if (name.contains("Openmdx2")) {
//		options.compilerArgs.add("-AJAVA_FLAVOUR_MDX2")
        javaCompiler = javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(targetPlatform.majorVersion))
        }
    } else if (name.contains("Openmdx3")) {
        javaCompiler = javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(targetPlatform.majorVersion))
        }
    } else if (name.contains("Openmdx4")) {
        javaCompiler = javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(targetPlatformOpenmdx4.majorVersion))
        }
    }
}

tasks {
	val openmdxCommonIncludes = listOf<String>( )
	val openmdxCommonExcludes = listOf(
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
	val openmdxClientIncludes = listOf<String>( )
	val openmdxClientExcludes = listOf(
		"org/ietf/jgss/*/**",
		"org/openmdx/dalvik/*/**"
	)
	val openmdxDalvikIncludes = listOf<String>( )
	val openmdxDalvikExcludes = listOf(
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

	named("processResources", Copy::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	named("processTestResources", Copy::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

    for (i in 2..4) {
        named("openmdx${i}Jar", Jar::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    }

    test {
        useJUnitPlatform()
        maxHeapSize = "4G"
    }
    compileJava {

        dependsOn(
            ":core:openmdx-base.jar", ":security:openmdx-security.jar"
        )
        doFirst {
            var f = file(layout.buildDirectory.dir("resources/main/META-INF/openmdxmof.properties"))
            touch(f)
            f.writeText(
                zipTree(
                    File(
                        getDeliverDir(), "../core/lib/openmdx-base.jar"
                    )
                ).matching { include("META-INF/openmdxmof.properties") }.singleFile.readText()
            )
            f.appendText(
                zipTree(
                    File(
                        getDeliverDir(), "../security/lib/openmdx-security.jar"
                    )
                ).matching { include("META-INF/openmdxmof.properties") }.singleFile.readText()
            )
        }
        options.release.set(Integer.valueOf(targetPlatform.majorVersion))
    }

    distTar {
        dependsOn(
            "openmdx-client.jar", "openmdx-dalvik.jar", "openmdx-client-sources.jar", "openmdx-dalvik-sources.jar"
        )
    }
    distZip {
        dependsOn(
            "openmdx-client.jar", "openmdx-dalvik.jar", "openmdx-client-sources.jar", "openmdx-dalvik-sources.jar"
        )
    }
    assemble {
        dependsOn(
            "openmdx-client.jar", "openmdx-client-sources.jar", "openmdx-dalvik.jar", "openmdx-dalvik-sources.jar"
        )
	}
	register<org.openmdx.gradle.ArchiveTask>("openmdx-client.jar") {
	    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		dependsOn(
			":core:openmdx-base.jar",
			":core:openmdx-system.jar",
			":client:compileJava",
			":client:processResources",
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
					File(buildDirAsFile, "classes/java/main"),
					File(buildDirAsFile, "resources/main"),
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
	register<org.openmdx.gradle.ArchiveTask>("openmdx-client-sources.jar") {
	    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
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
					File(buildDirAsFile, "generated/sources/java/main")
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
	register<org.openmdx.gradle.ArchiveTask>("openmdx-dalvik.jar") {
	    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		dependsOn(
			"openmdx-client.jar",
			":client:compileJava",
			":client:processResources"
		)
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
					path = "org/openmdx/dalvik/metainf/$name"
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
					File(buildDirAsFile, "classes/java/main"),
					File(buildDirAsFile, "resources/main"),
					"src/main/dalvik"
				).exclude(
					"META-INF/*/**"
				)
			}
		)
	}

	register<org.openmdx.gradle.ArchiveTask>("openmdx-dalvik-sources.jar") {
	    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		dependsOn(":client:openmdx-client-sources.jar")
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
					File(buildDirAsFile, "generated/sources/java/main"),
					zipTree(File(getDeliverDir(), "../client/lib/openmdx-client-sources.jar"))
				).include(
					openmdxDalvikIncludes
				).exclude(
					openmdxDalvikExcludes
				)
			}
		)
	}

}

distributions {
	main {
		distributionBaseName.set("openmdx-" + getProjectImplementationVersion() + "-${project.name}-jre-" + targetPlatform)
		contents {
			// client
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

//    main {
//        distributionBaseName.set("openmdx-${getProjectImplementationVersion()}-${project.name}-jre-$targetPlatform")
//        contents {
//            // client
//            from(".") {
//                into(project.name); include(
//                "LICENSE", "*.LICENSE", "NOTICE", "*.properties", "build*.*", "*.xml", "*.kts"
//            )
//            }
//            from("src") { into(project.name + "/src") }
//            // etc
//            from("etc") { into(project.name + "/etc") }
//            // rootDir
//            from("..") { include("*.properties", "*.kts") }
//            // jre-...
//            var path = "openmdx-$targetPlatform/${project.name}/lib"
//            from("../$path") { into(path) }
//            path = "openmdx-$targetPlatform/gradle/repo"
//            from("../$path") { into(path) }
//        }
//    }

    // Create unique distribution tasks based on extra keys
    arrayOf(
        "${project.extra["mdx2"]}", "${project.extra["mdx3"]}", "${project.extra["mdx4"]}"
    ).forEach { flavour ->

        val version = "${project.extra["${flavour}Version"]}" ?: return@forEach // Skip if version is not defined
        val jre = if (flavour == "openmdx4") targetPlatformOpenmdx4.majorVersion else targetPlatform

        create(flavour) {
            distributionBaseName.set("openmdx-$version-${project.name}-jre-$jre")
            contents {
                // client
                from(".") {
                    into(project.name)
                    include("LICENSE", "*.LICENSE", "NOTICE", "*.properties", "build*.*", "*.xml", "*.kts")
                }
                val buildDirAsFile = project.layout.buildDirectory.asFile.get()
                from(File(buildDirAsFile, "classes/java/$flavour")) { into(project.name + "/src") }
                // etc
                from("etc") { into(project.name + "/etc") }
                // rootDir
                from("..") { include("*.properties", "*.kts") }
                // jre-...
                var path = "openmdx-$jre/${project.name}-$version/lib"
                from("../$path") { into(path) }
                path = "openmdx-$targetPlatform/gradle/repo"
                from("../$path") { into(path) }
            }

        }
    }
}

var flav = project.extra["mdx2"]
tasks.named<Zip>("${flav}DistZip") {
    dependsOn("compileOpenmdx2Java")
//    archiveFileName.set(getArchiveName(flav, targetPlatform))
}

flav = project.extra["mdx3"]
tasks.named<Zip>("${flav}DistZip") {
    dependsOn("compileOpenmdx3Java")
//    archiveFileName.set(getArchiveName(flav, targetPlatform))
}
flav = project.extra["mdx4"]
tasks.named<Zip>("${flav}DistZip"){
    dependsOn("compileOpenmdx4Java")
//    archiveFileName.set(getArchiveName(flav, targetPlatformOpenmdx4.majorVersion))
}


flav = project.extra["mdx2"]
tasks.named<Tar>("${flav}DistTar") {
    dependsOn("compileOpenmdx2Java")
//    archiveFileName.set(getArchiveName(flav, targetPlatform))
}

flav = project.extra["mdx3"]
tasks.named<Tar>("${flav}DistTar") {
    dependsOn("compileOpenmdx3Java")
//    archiveFileName.set(getArchiveName(flav, targetPlatform))
}

flav = project.extra["mdx4"]
tasks.named<Tar>("${flav}DistTar") {
    dependsOn("compileOpenmdx4Java")
//    archiveFileName.set(getArchiveName(flav, targetPlatformOpenmdx4.majorVersion))
}

tasks.named<Zip>("${flav}DistZip") {
    dependsOn("compileOpenmdx3Java")
}