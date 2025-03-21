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
    throw GradleException("ERROR: JRE_18 not set " +
            "(e.g. export JRE_18=/usr/lib/jvm/java-8-openjdk-amd64/jre)")
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

project.configurations.maybeCreate("openmdxBootstrap")
val openmdxBootstrap by configurations
project.configurations.maybeCreate("jakartaeeApi")
val jakartaeeApi by configurations

dependencies {
    val projectPlatform = ":openmdx-${projectFlavour}-platform"
    // implementation
    implementation(project(":core"))
    implementation(project(":security"))
    implementation(platform(project(projectPlatform)))
    implementation("jakarta.platform:jakarta.jakartaee-api")
    if (runtimeCompatibility.isJava8()) {
        implementation(files(File(System.getenv("JRE_18"), "lib/rt.jar")))
    }
    // manifold preprocessor
    compileOnly("systems.manifold:manifold-preprocessor")
    annotationProcessor(platform(project(projectPlatform)))
    annotationProcessor("systems.manifold:manifold-preprocessor")
    // openmdxBootstrap
    openmdxBootstrap(project(":core"))
    // jakartaee-api
    jakartaeeApi(platform(project(projectPlatform)))
    jakartaeeApi("jakarta.platform:jakarta.jakartaee-api")
}

sourceSets {
    main {
        java {
            srcDir("src/main/java")
            srcDir("src/main/openmdx-${projectFlavour}/java")
        }
    }
}

tasks {

    val openmdxCommonIncludes = listOf<String>()
    val openmdxCommonExcludes = listOf(
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
    val openmdxClientIncludes = listOf<String>()
    val openmdxClientExcludes = listOf(
        "org/ietf/jgss/**",
        "org/openmdx/dalvik/**"
    )
    val openmdxDalvikIncludes = listOf<String>()
    val openmdxDalvikExcludes = listOf(
        "META-INF/**",
        "javax/transaction/**",
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
                ).matching { include("META-INF/openmdxmof.properties") }.singleFile.readText()
            )
            f.appendText(
                zipTree(
                    File(
                        project.rootDir,
                        "build/openmdx-${projectFlavour}/security/lib/openmdx-security.jar"
                    )
                ).matching { include("META-INF/openmdxmof.properties") }.singleFile.readText()
            )
        }
    }
    test {
        useJUnitPlatform()
        maxHeapSize = "4G"
    }
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
    register<org.openmdx.gradle.ArchiveTask>("openmdx-client.jar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(
            ":core:openmdx-base.jar",
            ":core:openmdx-system.jar",
            ":client:compileJava",
            ":client:processResources",
            ":security:openmdx-security.jar"
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
                    zipTree(File(project.rootDir, "build/openmdx-${projectFlavour}/core/lib/openmdx-base.jar")),
                    zipTree(File(project.rootDir, "build/openmdx-${projectFlavour}/core/lib/openmdx-system.jar")),
                    zipTree(File(project.rootDir, "build/openmdx-${projectFlavour}/security/lib/openmdx-security.jar"))
                ).include(
                    openmdxCommonIncludes
                ).exclude(
                    openmdxCommonExcludes
                )
            },
            copySpec {
                from(configurations["jakartaeeApi"].filter { it.name.endsWith("jar") }.map { zipTree(it) }).include(
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
        destinationDirectory.set(File(project.rootDir, "build/openmdx-${projectFlavour}/${project.name}/lib"))
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
                    "src/main/java", File(buildDirAsFile, "generated/sources/java/main")
                ).include(
                    openmdxClientIncludes
                ).exclude(
                    openmdxClientExcludes
                )
            },
            copySpec {
                from(
                    zipTree(File(project.rootDir, "build/openmdx-${projectFlavour}/core/lib/openmdx-base-sources.jar")),
                    zipTree(File(project.rootDir, "build/openmdx-${projectFlavour}/core/lib/openmdx-system-sources.jar")),
                    zipTree(File(project.rootDir, "build/openmdx-${projectFlavour}/security/lib/openmdx-security-sources.jar"))
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
            "openmdx-client.jar", ":client:compileJava", ":client:processResources"
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
                    zipTree(File(project.rootDir, "build/openmdx-${projectFlavour}/client/lib/openmdx-client.jar"))
                ).include(
                    "META-INF/*.properties"
                ).exclude(
                    "META-INF/openmdx-xml-outputfactory.properties"
                ).eachFile {
                    path = "org/openmdx/dalvik/metainf/$name"
                }
            }, copySpec {
                from(
                    zipTree(File(project.rootDir, "build/openmdx-${projectFlavour}/client/lib/openmdx-client.jar"))
                ).include(
                    openmdxDalvikIncludes
                ).exclude(
                    openmdxDalvikExcludes
                )
            }, copySpec {
                from(
                    File(buildDirAsFile, "classes/main/java"), File(buildDirAsFile, "resources/main"), "src/main/dalvik"
                ).exclude(
                    "META-INF/**"
                )
            }
        )
    }
    register<org.openmdx.gradle.ArchiveTask>("openmdx-dalvik-sources.jar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(":client:openmdx-client-sources.jar")
        destinationDirectory.set(File(project.rootDir, "build/openmdx-${projectFlavour}/${project.name}/lib"))
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
                    zipTree(File(project.rootDir, "build/openmdx-${projectFlavour}/client/lib/openmdx-client-sources.jar"))
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