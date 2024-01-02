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
    maven {
       url = uri("file:" + File(project.rootDir, "publish/build/repos/releases"))
    }
}

var env = Properties()
env.load(FileInputStream(File(project.rootDir, "build.properties")))
val targetPlatform = JavaVersion.valueOf(env.getProperty("target.platform"))

eclipse {
	project {
    	name = "openMDX 2 ~ Core"
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

fun getVersionClass(packageName: String): String {

	return """

        package ${packageName};

        public class Version {

            /**
            * A specification version is compliant with a given one if<ol>
            * <li>its major numbers is equal to the given one
            * <li>its minor number is greater than or equal to the given one
            * </ol>
            */
            private final static String SPECIFICATION_VERSION = "${project.version.toString().substring(0, 4)}";
            
            /**
            * An implementation version is compliant with a given one if
            * their string representations are equal.
            */
            private final static String IMPLEMENTATION_VERSION = "${project.version}";

            /**
            * Get the specification version.
            *
            * @return the implementation version
            */
            public static String getSpecificationVersion(
            ){
                return Version.SPECIFICATION_VERSION;
            }

            /**
            * Get the implementation version.
            *
            * @return the implementation version
            */
            public static String getImplementationVersion(
            ){
                return Version.IMPLEMENTATION_VERSION;
            }
                    
            /**
            * Define a main so that the version may easily be printed by a tool
            * 
            * @param arguments	the program arguments
            */
            public static void main(
                String arguments[]
            ) {
                System.out.println(
                    new StringBuilder(
                        Version.class.getName()
                    ).append(
                        '='
                    ).append(
                        Version.getImplementationVersion()
                    )
                );
            }
        }
        """.trimIndent()
}

project.configurations.maybeCreate("openmdxBase")
project.configurations.maybeCreate("openmdxBootstrap")
project.configurations.maybeCreate("jdoApi")
project.configurations.maybeCreate("cacheApi")
val openmdxBase by configurations
val openmdxBootstrap by configurations
val jdoApi by configurations
val cacheApi by configurations

dependencies {
    // implementation
    implementation("javax:javaee-api:8.0.+")
    implementation("javax.jdo:jdo-api:3.1")
    implementation("javax.cache:cache-api:1.1.+")
	implementation("com.vladsch.flexmark:flexmark:0.62.2")
	implementation("com.atomikos:transactions-jta:6.0.0")
	implementation("com.atomikos:transactions-jdbc:6.0.0")
    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.junit.platform:junit-platform-commons:1.10.1")
    testImplementation("org.junit.platform:junit-platform-engine:1.10.1")
    testImplementation("org.mockito:mockito-core:4.2.0")    
    testImplementation("org.mockito:mockito-junit-jupiter:4.2.0")    
    // openmdxBase
    openmdxBase("org.openmdx:openmdx-base:2.18.10")
    // openmdxBootstrap
    openmdxBootstrap(files(file(layout.buildDirectory.dir("generated/classes/openmdxBootstrap"))))
    openmdxBootstrap("javax:javaee-api:8.0.+")
	openmdxBootstrap("com.vladsch.flexmark:flexmark:0.62.2")
    // jdo-api
    jdoApi("javax.jdo:jdo-api:3.1")
    // cache-api
    cacheApi("javax.cache:cache-api:1.1.+")
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

	val openmdxBaseIncludes = listOf(
		"javax/cache/*/**",
		"javax/jdo/*/**",
		"javax/jmi/*/**",
		"net/*/NetPackage*",
		"net/rfc/*/RfcPackage*",
		"org/*/OrgPackage*",
		"org/ietf/*/IetfPackage*",
		"org/iso/*/IsoPackage*",
		"org/oasisopen/*/**",
		"org/omg/*/**",
		"org/openmdx/*/OpenmdxPackage*",
		"org/openmdx/*1/**",
		"org/openmdx/*2/**",
		"org/openmdx/application/*/**",
		"org/openmdx/base/*/**",
		"org/openmdx/dalvik/uses/*/**",
		"org/openmdx/exception/*/**",
		"org/openmdx/kernel/*/**",
		"org/openmdx/jdo/*/**",
		"org/openmdx/uses/gnu/*/**",
		"org/openmdx/uses/java/**",
		"org/openmdx/uses/org/apache/commons/collections/*/**",
		"org/openmdx/uses/org/apache/commons/fileupload/*/**",
		"org/openmdx/uses/org/apache/commons/io/*/**",
		"org/openmdx/uses/org/apache/commons/pool/*/**",
		"org/openmdx/uses/org/apache/commons/pool2/*/**",
		"org/un/*/UnPackage*",
		"org/w3c/*/**",
		"org/xmi/*",
		"META-INF/orm.xml",
		"META-INF/openmdx*.properties"
	)
	val openmdxBaseExcludes = listOf(
		"org/openmdx/compatibility/kernel/url/protocol/*/**",
		"org/openmdx/kernel/url/protocol/*/**"
	)
	val openmdxSystemIncludes = listOf(
		"org/openmdx/compatibility/kernel/url/protocol/*/**",
		"org/openmdx/kernel/logging/*/**",
		"org/openmdx/kernel/url/protocol/*/**",
		"org/openmdx/kernel/xri/*",
		"org/openmdx/system/*/**"
	)
	val openmdxSystemExcludes = listOf<String>( )

	named("processResources", Copy::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	named("processTestResources", Copy::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

	register<org.openmdx.gradle.GenerateModelsTask>("generate-model") {
	    inputs.dir("$projectDir/src/model/emf")
	    inputs.dir("$projectDir/src/main/resources")
	    outputs.file(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}-models.zip"))
	    outputs.file(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}.openmdx-xmi.zip"))
	    classpath = configurations["openmdxBootstrap"]
	    doFirst {
	        project.copy {
	            from(project.zipTree(project.configurations.getByName("openmdxBase").singleFile))
	            into(layout.buildDirectory.dir("generated/classes/openmdxBootstrap"))
	        }
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
	        copy {
	            from(
	                zipTree(layout.buildDirectory.dir("generated/sources/model/openmdx-${project.name}-models.zip"))
	            )
	            into(layout.buildDirectory.dir("generated/resources/main"))
	            include(
	                "**/openmdxorm.properties"
	            )
	        }
	    }
	}
	test {
	    useJUnitPlatform()
	    maxHeapSize = "4G"
	}
	distTar {
		dependsOn(
			":core:openmdx-base.jar",
			":core:openmdx-system.jar",
			":core:openmdx-base-sources.jar",
			":core:openmdx-system-sources.jar"
		)
	}
	distZip {
		dependsOn(
			":core:openmdx-base.jar",
			":core:openmdx-system.jar",
			":core:openmdx-base-sources.jar",
			":core:openmdx-system-sources.jar"
		)
	}
	compileJava {
	    dependsOn("generate-model")
	    doFirst {
	    	// base/Version
			var f: File = file(layout.buildDirectory.dir("generated/sources/java/main/org/openmdx/base/Version.java"))
			f.writeText(getVersionClass("org.openmdx.base"))
	    	// application/Version
	        f = file(layout.buildDirectory.dir("generated/sources/java/main/org/openmdx/application/Version.java"))
	        touch(f)
	        f.writeText(getVersionClass("org.openmdx.application"))
	        // system/Version
	        f = file(layout.buildDirectory.dir("generated/sources/java/main/org/openmdx/system/Version.java"))
	        touch(f)
	        f.writeText(getVersionClass("org.openmdx.system"))
	    }
	    options.release.set(Integer.valueOf(targetPlatform.majorVersion))
	}
	assemble {
		dependsOn(
            "openmdx-base.jar",
            "openmdx-base-sources.jar",
            "openmdx-system.jar",
            "openmdx-system-sources.jar"
        )
	}
	register<org.openmdx.gradle.ArchiveTask>("openmdx-base.jar") {
	    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	    dependsOn(
	        ":core:compileJava",
	        ":core:processResources"
	    )
		destinationDirectory.set(File(getDeliverDir(), "lib"))
		archiveFileName.set("openmdx-base.jar")
	    includeEmptyDirs = false
		manifest {
	        attributes(
	        	getManifest(
	        		"openMDX Base Library",
	        		"openmdx-base"
	        	)
	        )
	    }
		from(
			File(buildDirAsFile, "classes/java/main"),
			File(buildDirAsFile, "resources/main"),
			File(buildDirAsFile, "generated/resources/main"),
			"src/main/resources",
	        configurations["cacheApi"].filter { it.name.endsWith("jar") }.map { zipTree(it) },
	        configurations["jdoApi"].filter { it.name.endsWith("jar") }.map { zipTree(it) }
		)
		include(openmdxBaseIncludes)
		exclude(openmdxBaseExcludes)
	}
	register<org.openmdx.gradle.ArchiveTask>("openmdx-base-sources.jar") {
		destinationDirectory.set(File(getDeliverDir(), "lib"))
		archiveFileName.set("openmdx-base-sources.jar")
	    includeEmptyDirs = false
		manifest {
	        attributes(
	        	getManifest(
	        		"openMDX Base Sources",
	        		"openmdx-base-sources"
	        	)
	        )
	    }
		from(
			"src/main/java",
			File(buildDirAsFile, "generated/sources/java/main")
		)
		include(openmdxBaseIncludes)
		exclude(openmdxBaseExcludes)
	}
	register<org.openmdx.gradle.ArchiveTask>("openmdx-system.jar") {
		destinationDirectory.set(File(getDeliverDir(), "lib"))
	    dependsOn(":core:compileJava")
		archiveFileName.set("openmdx-system.jar")
	    includeEmptyDirs = false
		manifest {
	        attributes(
	        	getManifest(
	        		"openMDX System Library",
	        		"openmdx-system"
	        	)
	        )
	    }
	    from(
	  		File(buildDirAsFile, "classes/java/main")
	  	)
	  	include(openmdxSystemIncludes)
	  	exclude(openmdxSystemExcludes)
	}
	register<org.openmdx.gradle.ArchiveTask>("openmdx-system-sources.jar") {
		destinationDirectory.set(File(getDeliverDir(), "lib"))
		archiveFileName.set("openmdx-system-sources.jar")
	    includeEmptyDirs = false
		manifest {
	        attributes(
	        	getManifest(
	        		"openMDX System Sources",
	        		"openmdx-system-sources"
	        	)
	        )
	    }
		from(
			"src/main/java",
			File(buildDirAsFile, "generated/sources/java/main")
		)
		include(openmdxSystemIncludes)
		exclude(openmdxSystemExcludes)
	}
}

distributions {
    main {
    	distributionBaseName.set("openmdx-" + getProjectImplementationVersion() + "-core-jre-" + targetPlatform)
        contents {
        	// core
        	from(".") { into("core"); include("LICENSE", "*.LICENSE", "NOTICE", "*.properties", "build*.*", "*.xml", "*.kts") }
            from("src") { into("core/src") }
            // etc
            from("etc") { into("core/etc") }
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
