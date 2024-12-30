/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: build.gradle.kts for ossrh publishing
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
    `java-library`
    `maven-publish`
    signing
    eclipse
}

val projectFlavour = project.extra["projectFlavour"] as String
val projectSpecificationVersion = project.extra["projectSpecificationVersion"] as String
val projectMaintenanceVersion = project.extra["projectMaintenanceVersion"] as String
val runtimeCompatibility = project.extra["runtimeCompatibility"] as JavaVersion

java {
    sourceCompatibility = runtimeCompatibility
    targetCompatibility = runtimeCompatibility
}

eclipse {
	project {
    	name = "openMDX ${projectFlavour} ~ Publish"
    }
}

publishing {
    repositories {
        maven {
        	// Publish to local
        	/**/
            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
            /**/
            // Publish to OSSRH
            /*
            credentials {
                username = project.property("ossrhUsername").toString()
                password = project.property("ossrhPassword").toString()
            }
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            */
            // Url
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
    publications {
        create<MavenPublication>("openmdxSystem") {
            artifactId = "openmdx-system"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/core/lib/openmdx-system.jar")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/core/lib/openmdx-system-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-system-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-system")
                description.set("openMDX/System Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxBase") {
            artifactId = "openmdx-base"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/core/lib/openmdx-base.jar")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/core/lib/openmdx-base-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-base-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-base")
                description.set("openMDX/Base Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxBaseModels") {
            artifactId = "openmdx-base-models"
            artifact(project.artifacts.add("archives", File("${rootDir}/core/build/generated/sources/model/openmdx-core.openmdx-emf.zip")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-base-models-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-base-models-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-base-models")
                description.set("openMDX/Base Models Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxClient") {
            artifactId = "openmdx-client"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/client/lib/openmdx-client.jar")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/client/lib/openmdx-client-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-client-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-client")
                description.set("openMDX/Client Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxDalvik") {
            artifactId = "openmdx-dalvik"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/client/lib/openmdx-dalvik.jar")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/client/lib/openmdx-dalvik-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-dalvik-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-dalvik")
                description.set("openMDX/Dalvik Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxSecurity") {
            artifactId = "openmdx-security"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-security.jar")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-security-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-security-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-security")
                description.set("openMDX/Security Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxSecurityModels") {
            artifactId = "openmdx-security-models"
            artifact(project.artifacts.add("archives", File("${rootDir}/security/build/generated/sources/model/openmdx-security.openmdx-emf.zip")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-security-models-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-security-models-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-security-models")
                description.set("openMDX/Security Models Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxAuthentication") {
            artifactId = "openmdx-authentication"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-authentication.jar")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-authentication-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-authentication-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-authentication")
                description.set("openMDX/Authentication Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxLdap") {
            artifactId = "openmdx-ldap"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-ldap.jar")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-ldap-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-ldap-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-ldap")
                description.set("openMDX/Ldap Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxPki") {
            artifactId = "openmdx-pki"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-pki.jar")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-pki-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-pki-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-pki")
                description.set("openMDX/Pki Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxRadius") {
            artifactId = "openmdx-radius"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-radius.jar")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-radius-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-radius-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-radius")
                description.set("openMDX/Radius Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxResource") {
            artifactId = "openmdx-resource"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-resource.jar")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/security/lib/openmdx-resource-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-resource-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-resource")
                description.set("openMDX/Resource Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxPortal") {
            artifactId = "openmdx-portal"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/portal/lib/openmdx-portal.jar")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/portal/lib/openmdx-portal-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-portal-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-portal")
                description.set("openMDX/Portal Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxPortalModels") {
            artifactId = "openmdx-portal-models"
            artifact(project.artifacts.add("archives", File("${rootDir}/portal/build/generated/sources/model/openmdx-portal.openmdx-emf.zip")) { type = "jar" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-portal-models-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-portal-models-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-portal-models")
                description.set("openMDX/Portal Models Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxInspector") {
            artifactId = "openmdx-inspector"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/portal/deployment-unit/openmdx-inspector.war")) { type = "war" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-inspector-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/openmdx-inspector-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("openmdx-inspector")
                description.set("openMDX/Inspector Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
        create<MavenPublication>("openmdxCatalina") {
            artifactId = "catalina-openmdx"
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/tomcat/lib/catalina-openmdx.jar")) { type = "war" })
            artifact(project.artifacts.add("archives", File("${rootDir}/build${projectFlavour}/tomcat/lib/catalina-openmdx-sources.jar")) { type = "jar"; classifier = "sources" })
            artifact(project.artifacts.add("archives", File("$projectDir/src/main/maven/catalina-openmdx-javadoc.jar")) { type = "jar"; classifier = "javadoc" })
            pom {
                name.set("catalina-openmdx")
                description.set("openMDX/Catalina Library")
                url.set("http://www.openmdx.org")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("wfro")
                        name.set("Werner Froidevaux")
                        email.set("wfro64@users.noreply.github.com")
                    }
                    developer {
                        id.set("dirty-harry")
                        name.set("Harry")
                        email.set("dirty-harry@users.sourceforge.net")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openmdx/openmdx.git")
                    developerConnection.set("scm:git:ssh://github.com/openmdx/openmdx.git")
                    url.set("https://github.com/openmdx/openmdx/tree/master")
                }
            }
        }
    }
}

signing {
    /*
    sign(publishing.publications["openmdxSystem"])
    sign(publishing.publications["openmdxBase"])
    sign(publishing.publications["openmdxBaseModels"])
    sign(publishing.publications["openmdxClient"])
    sign(publishing.publications["openmdxDalvik"])
    sign(publishing.publications["openmdxSecurity"])
    sign(publishing.publications["openmdxSecurityModels"])
    sign(publishing.publications["openmdxAuthentication"])
    sign(publishing.publications["openmdxLdap"])
    sign(publishing.publications["openmdxPki"])
    sign(publishing.publications["openmdxRadius"])
    sign(publishing.publications["openmdxResource"])
    sign(publishing.publications["openmdxPortal"])
    sign(publishing.publications["openmdxPortalModels"])
    sign(publishing.publications["openmdxInspector"])
    sign(publishing.publications["openmdxCatalina"])
    */
}
