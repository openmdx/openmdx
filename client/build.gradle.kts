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

if (runtimeCompatibility.isJava8() && System.getenv("JRE_18") == null) {
    throw GradleException(
        "ERROR: JRE_18 not set " +
                "(e.g. export JRE_18=/usr/lib/jvm/java-8-openjdk-amd64/jre)"
    )
}

eclipse {
    project {
        name = "openMDX $projectFlavour ~ Client"
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

project.configurations.maybeCreate("jakartaeeApi")
val jakartaeeApi by configurations

java {
    sourceSets {
        // Default 'main' source set
        getByName("main") {
            resources.srcDirs("src/main/resources", "src/main/openmdx-${projectFlavour}/resources")
        }
        // Additional 'dalvik' source set
        create("dalvik") {
            java.srcDirs("src/dalvik/java", "src/dalvik/openmdx-${projectFlavour}/java")
            resources.srcDirs("src/dalvik/resources", "src/dalvik/openmdx-${projectFlavour}/resources")
        }
    }
}

dependencies {
    val projectPlatform = ":openmdx-${projectFlavour}-platform"
    // implementation
    add("dalvikImplementation", platform(project(projectPlatform)))
    add("dalvikImplementation", "jakarta.platform:jakarta.jakartaee-api")
    add("dalvikImplementation", files(File(System.getenv("JRE_18"), "lib/rt.jar")))
    add("dalvikImplementation", project(":core"))
    add("dalvikImplementation", project(":security"))
    // manifold preprocessor
    add("dalvikCompileOnly", "systems.manifold:manifold-preprocessor")
    annotationProcessor(platform(project(projectPlatform)))
    annotationProcessor("systems.manifold:manifold-preprocessor")
    // jakartaee-api
    jakartaeeApi(platform(project(projectPlatform)))
    jakartaeeApi("jakarta.platform:jakarta.jakartaee-api")
}

tasks {

    val openmdxClientIncludes = listOf(
        "*/transaction/Synchronization.*"
    )
    val openmdxClientExcludes = listOf(
        "META-INF/MANIFEST.MF",
        "META-INF/openmdxmof.properties",
        "META-INF/openmdxExceptionMapper.properties",
        "org/omg/**/jpa3",
        "org/omg/**/jpa3/*",
        "org/openmdx/**/jpa3",
        "org/openmdx/**/jpa3/*",
        "org/openmdx/application/airsync/**",
        "org/openmdx/application/dataprovider/layer/persistence/jdbc**",
        "org/openmdx/application/mof/externalizer/**",
        "org/openmdx/application/naming/**",
        "org/openmdx/application/dataprovider/kernel/**",
        "org/openmdx/application/rest/adapter/**",
        "org/openmdx/application/rest/ejb/**",
        "org/openmdx/application/rest/http/servlet/**",
        "org/openmdx/application/transaction/**",
        "org/openmdx/kernel/ejb/**",
        "org/openmdx/kernel/lightweight/**",
        "org/openmdx/kernel/naming/**",
        "org/openmdx/kernel/servlet/**",
        "org/openmdx/kernel/sql/**",
        "org/openmdx/uses/org/apache/commons/**",
        "org/openmdx/application/rest/http/RestServlet_*",
        "org/openmdx/application/rest/http/RemoteUserLoginModule*",
        "org/openmdx/application/rest/http/RequestCallbackHandler*",
        "org/openmdx/application/rest/http/TrustingLoginModule*",
        "org/openmdx/base/resource/adapter/**"
    )
    val openmdxDalvikExcludes = listOf(
        "META-INF/**",
        "javax/transaction/**",
        "org/omg/**/jpa3",
        "org/omg/**/jpa3/*",
        "org/omg/primitivetypes/**",
        "org/openmdx/**/jpa3",
        "org/openmdx/**/jpa3/*",
        "org/openmdx/application/**/stream",
        "org/openmdx/application/**/stream/*",
        "org/openmdx/application/airsync/**",
        "org/openmdx/application/dataprovider/layer/persistence/jdbc**",
        "org/openmdx/application/mof/externalizer/**",
        "org/openmdx/application/naming/**",
        "org/openmdx/application/dataprovider/kernel/**",
        "org/openmdx/application/rest/adapter/**",
        "org/openmdx/application/rest/ejb/**",
        "org/openmdx/application/rest/http/servlet/**",
        "org/openmdx/application/transaction/**",
        "org/openmdx/base/**/stream",
        "org/openmdx/base/**/stream/*",
        "org/openmdx/kernel/ejb/**",
        "org/openmdx/kernel/lightweight/**",
        "org/openmdx/kernel/naming/**",
        "org/openmdx/kernel/platform/platform.properties",
        "org/openmdx/kernel/servlet/**",
        "org/openmdx/kernel/sql/**",
        "org/openmdx/uses/org/apache/commons/**",
        "org/openmdx/application/rest/http/RestServlet_*",
        "org/openmdx/application/rest/http/RemoteUserLoginModule*",
        "org/openmdx/application/rest/http/RequestCallbackHandler*",
        "org/openmdx/application/rest/http/TrustingLoginModule*",
        "org/openmdx/base/resource/adapter/**",
        "**/*.wbxml",
        "**/*.xml",
        "**/*.xsd",
        "**/xmi1"
    )

    compileJava {
        dependsOn(
            ":core:openmdx-base.jar",
            ":security:openmdx-security.jar"
        )
        doFirst {
            val f = file(layout.buildDirectory.dir("resources/main/META-INF/openmdxmof.properties"))
            touch(f)
            f.writeText(
                zipTree(
                    File(
                        project.rootDir,
                        "build/openmdx-${projectFlavour}/core/lib/openmdx-base.jar"
                    )
                ).matching {
                    include("META-INF/openmdxmof.properties")
                }.singleFile.readText()
            )
            f.appendText(
                zipTree(
                    File(
                        project.rootDir,
                        "build/openmdx-${projectFlavour}/security/lib/openmdx-security.jar"
                    )
                ).matching {
                    include("META-INF/openmdxmof.properties")
                }.singleFile.readText()
            )
        }
    }

    named("compileDalvikJava") {
        val task = this as JavaCompile
        task.source = sourceSets["dalvik"].java
        task.classpath = sourceSets["dalvik"].compileClasspath
        task.destinationDirectory.set(sourceSets["dalvik"].java.destinationDirectory)
        task.sourceCompatibility = runtimeCompatibility.toString()
        task.targetCompatibility = runtimeCompatibility.toString()
        dependsOn(
            ":core:openmdx-base.jar",
            ":security:openmdx-security.jar"
        )
        doFirst {
            val f = file(layout.buildDirectory.dir("resources/dalvik/org/openmdx/dalvik/metainf/openmdxmof.properties"))
            touch(f)
            f.writeText(
                zipTree(
                    File(
                        project.rootDir,
                        "build/openmdx-${projectFlavour}/core/lib/openmdx-base.jar"
                    )
                ).matching {
                    include("META-INF/openmdxmof.properties")
                }.singleFile.readText()
            )
            f.appendText(
                zipTree(
                    File(
                        project.rootDir,
                        "build/openmdx-${projectFlavour}/security/lib/openmdx-security.jar"
                    )
                ).matching {
                    include("META-INF/openmdxmof.properties")
                }.singleFile.readText()
            )
        }
    }

    named("processResources", Copy::class.java) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    named("processDalvikResources", Copy::class.java) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    test {
        useJUnitPlatform()
        maxHeapSize = "4G"
    }

    if (runtimeCompatibility.isJava8()) {
        distTar {
            dependsOn(
                "openmdx-client.jar",
                "openmdx-dalvik.jar",
                "openmdx-client-sources.jar",
                "openmdx-dalvik-sources.jar"
            )
        }
        distZip {
            dependsOn(
                "openmdx-client.jar",
                "openmdx-dalvik.jar",
                "openmdx-client-sources.jar",
                "openmdx-dalvik-sources.jar"
            )
        }
        assemble {
            dependsOn(
                "openmdx-client.jar",
                "openmdx-client-sources.jar",
                "openmdx-dalvik.jar",
                "openmdx-dalvik-sources.jar"
            )
        }
    } else {
        distTar {
            dependsOn(
                "openmdx-client.jar",
                "openmdx-client-sources.jar",
            )
        }
        distZip {
            dependsOn(
                "openmdx-client.jar",
                "openmdx-client-sources.jar",
            )
        }
        assemble {
            dependsOn(
                "openmdx-client.jar",
                "openmdx-client-sources.jar",
            )
        }
    }

    register<org.openmdx.gradle.ArchiveTask>("openmdx-client.jar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(
            ":core:openmdx-base.jar",
            ":core:openmdx-system.jar",
            ":security:openmdx-security.jar",
            ":client:compileJava",
            ":client:processResources"
        )
        destinationDirectory.set(File(project.rootDir, "build/openmdx-${projectFlavour}/${project.name}/lib"))
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
                    File(buildDirAsFile, "resources/main")
                )
            },
            copySpec {
                from(
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/core/lib/openmdx-base.jar"
                        )
                    ),
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/core/lib/openmdx-system.jar"
                        )
                    ),
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/security/lib/openmdx-security.jar"
                        )
                    )
                ).exclude(
                    openmdxClientExcludes
                )
            },
            copySpec {
                from(
                    configurations["jakartaeeApi"].filter {
                        it.name.endsWith("jar")
                    }.map {
                        zipTree(it)
                    }
                ).include(
                    openmdxClientIncludes
                )
            }
        )
    }

    register<org.openmdx.gradle.ArchiveTask>("openmdx-dalvik.jar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(
            ":core:openmdx-base.jar",
            ":core:openmdx-system.jar",
            ":security:openmdx-security.jar",
            ":client:compileDalvikJava",
            ":client:processDalvikResources"
        )
        destinationDirectory.set(File(project.rootDir, "build/openmdx-${projectFlavour}/${project.name}/lib"))
        archiveFileName.set("openmdx-dalvik.jar")
        includeEmptyDirs = false
        manifest {
            attributes(
                getManifest(
                    "openMDX Dalvik Library", "openmdx-dalvik"
                )
            )
        }
        with(
            copySpec {
                from(
                    File(
                        buildDirAsFile,
                        "classes/java/dalvik"
                    ),
                    File(
                        buildDirAsFile,
                        "resources/dalvik"
                    )
                )
            },
            copySpec {
                from(
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/core/lib/openmdx-base.jar"
                        )
                    ),
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/core/lib/openmdx-system.jar"
                        )
                    ),
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/security/lib/openmdx-security.jar"
                        )
                    )
                ).exclude(
                    openmdxDalvikExcludes
                )
            }
        )
    }

    register<org.openmdx.gradle.ArchiveTask>("openmdx-client-sources.jar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(
            ":core:openmdx-base-sources.jar",
            ":core:openmdx-system-sources.jar",
            ":security:openmdx-security-sources.jar",
            ":client:processResources"
        )
        destinationDirectory.set(
            File(
                project.rootDir,
                "build/openmdx-${projectFlavour}/${project.name}/lib"
            )
        )
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
                    File(buildDirAsFile, "resources/main")
                )
            },
            copySpec {
                from(
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/core/lib/openmdx-base-sources.jar"
                        )
                    ),
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/core/lib/openmdx-system-sources.jar"
                        )
                    ),
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/security/lib/openmdx-security-sources.jar"
                        )
                    )
                ).exclude(
                    openmdxClientExcludes
                )
            }
        )
    }

    register<org.openmdx.gradle.ArchiveTask>("openmdx-dalvik-sources.jar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        destinationDirectory.set(File(project.rootDir, "build/openmdx-${projectFlavour}/${project.name}/lib"))
        archiveFileName.set("openmdx-dalvik-sources.jar")
        includeEmptyDirs = false
        dependsOn(
            ":core:openmdx-base-sources.jar",
            ":core:openmdx-system-sources.jar",
            ":security:openmdx-security-sources.jar",
            ":client:processDalvikResources"
        )
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
                    File("src/dalvik/java"),
                    File("src/dalvik/openmdx-${projectFlavour}/java"),
                    File(buildDirAsFile, "resources/dalvik")
                )
            },
            copySpec {
                from(
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/core/lib/openmdx-base-sources.jar"
                        )
                    ),
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/core/lib/openmdx-system-sources.jar"
                        )
                    ),
                    zipTree(
                        File(
                            project.rootDir,
                            "build/openmdx-${projectFlavour}/security/lib/openmdx-security-sources.jar"
                        )
                    )
                ).exclude(
                    openmdxDalvikExcludes
                )
            }
        )
    }
}

distributions {
    main {
        distributionBaseName.set("openmdx-${project.version}-${project.name}-jre-${runtimeCompatibility}")
        contents {
            // client
            from(".") {
                into(project.name); include(
                "LICENSE",
                "*.LICENSE",
                "NOTICE",
                "*.properties",
                "build*.*",
                "*.xml",
                "*.kts"
            )
            }
            from("src") { into("${project.name}/src") }
            // etc
            from("etc") { into("${project.name}/etc") }
            // rootDir
            from("..") { include("*.properties", "*.kts") }
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