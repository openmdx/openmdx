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
import java.util.regex.Pattern;
import java.util.*

plugins {
	java
	`java-library`
	eclipse
	distribution
	id("org.openmdx.java-flavours-plugin")
	id("systems.manifold.manifold-gradle-plugin")
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

var flavour = currentFlavour()
var flavourVersion = currentFlavourVersion()
var flavourJavaVersion : JavaVersion = project.extra["${flavour}JavaVersion"] as JavaVersion

eclipse {
	project {
    	name = "openMDX ${flavourVersion} ~ Core"
    }
    jdt {
		sourceCompatibility = flavourJavaVersion
    	targetCompatibility = flavourJavaVersion
    	javaRuntimeName = "JavaSE-$flavourJavaVersion"
    }
}

fun currentFlavour(): String {
    val taskRequestsStr = gradle.startParameter.taskRequests.toString()
    val pattern = Pattern.compile("\\w*([Oo]penmdx[2-4])\\w*")
    val matcher = pattern.matcher(taskRequestsStr)
    val flavour = if (matcher.find()) {
        matcher.group(1).lowercase()
    } else {
        "main"
    }
    return flavour
}

fun currentFlavourVersion(): String {
    val taskRequestsStr = gradle.startParameter.taskRequests.toString()
    val pattern = Pattern.compile("\\w*[Oo]penmdx([2-4])\\w*")
    val matcher = pattern.matcher(taskRequestsStr)
    val flavourVersion = if (matcher.find()) {
        matcher.group(1)
    } else {
        "2"
    }
    return flavourVersion
}

fun getProjectImplementationVersion(): String {
	return "${flavourVersion}.${project.version}";
}

fun getDeliverDir(): File {
	return File(project.rootDir, "${flavour}/${project.name}");
}

fun touch(file: File) {
	ant.withGroovyBuilder { "touch"("file" to file, "mkdirs" to true) }
}

fun getVersionClass(packageName: String, projectImplementationVersion: String): String {

	return """

        package ${packageName};

        public class Version {

            /**
             * The Flavour version tells which openMDX version is used:<ul>
             * <li>openMDX 2: Jakarta EE 8 and classic JMI API
             * <li>openMDX 3: Jakarta EE 8 and contemporary JMI API
             * <li>openMDX 4: Jakarta EE 10 and contemporary JMI API
             * </ol>
             */
            private final static String FLAVOUR_VERSION = "${projectImplementationVersion.substring(0, 1)}";
            
            /**
             * A specification version is compliant with a given one if<ol>
             * <li>its major numbers is equal to the given one
             * <li>its minor number is greater than or equal to the given one
             * </ol>
             */
            private final static String SPECIFICATION_VERSION = "${projectImplementationVersion.substring(0, 4)}";
            
            /**
             * An implementation version is compliant with a given one if
             * their string representations are equal.
             */
            private final static String IMPLEMENTATION_VERSION = "${projectImplementationVersion}";
            
            /**
             * Get the openMDX flavour version:<ul>
             * <li>openMDX 2: Jakarta EE 8 and classic JMI API
             * <li>openMDX 3: Jakarta EE 8 and contemporary JMI API
             * <li>openMDX 4: Jakarta EE 10 and contemporary JMI API
             * </ul>
             *
             * @return the flavour version
             */
            public static String getFlavourVersion(
            ){
                return Version.FLAVOUR_VERSION;
            }

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

configure<org.openmdx.gradle.JavaFlavoursExtension> {
	flavour("openmdx2")
	flavour("openmdx3")
	flavour("openmdx4")
}

project.configurations.maybeCreate("openmdxBase")
project.configurations.maybeCreate("openmdxBootstrap")
project.configurations.maybeCreate("jdoApi")
project.configurations.maybeCreate("cacheApi")
val openmdxBase by configurations
val openmdxBootstrap by configurations
val jdoApi by configurations
val cacheApi by configurations
val openmdx4Implementation by configurations

configurations {
	// needed
	register("compile")
	register("testCompile")
	register("runtime")
	register("testRuntime")
	for (i in 2..4) {
        register("openmdx${i}Build")
		register("openmdx${i}Compile")
		register("openmdx${i}TestCompile")
		register("openmdx${i}Runtime")
		register("openmdx${i}TestRuntime")
	}
}

sourceSets {
	main {
		java { srcDir("src/main/java"); srcDir(layout.buildDirectory.dir("generated/sources/main/java")); srcDir(layout.buildDirectory.dir("generated/sources/openmdx2/java")) }
		resources { srcDir("src/main/resources") }
	}
}

dependencies {

    // implementation
    implementation(libs.javax.javaee.api)
    implementation(libs.javax.jdo.api)
    implementation(libs.javax.cache.api)
	implementation(libs.vladsch.flexmark)
	implementation(libs.atomikos.transaction.jta)
	implementation(libs.atomikos.transaction.jdbc)
    // Test
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // openmdxBase
    openmdxBase(libs.openmdx.base)
    // openmdxBootstrap
    openmdxBootstrap(files(file(layout.buildDirectory.dir("generated/classes/openmdxBootstrap"))))
    openmdxBootstrap(libs.javax.javaee.api)
	openmdxBootstrap(libs.vladsch.flexmark)
	// manifold preprocessor
	implementation(libs.systems.manifold.preprocessor)
	implementation(libs.systems.manifold.props.rt)
	implementation(libs.systems.manifold.ext)
    // jdo-api
    jdoApi(libs.javax.jdo.api)
    // cache-api
    cacheApi(libs.javax.cache.api)
	openmdx4Implementation("jakarta.platform:jakarta.jakartaee-api:10.0.0")

}

tasks.withType<JavaCompile> {
    val flavourLanguageVersion : JavaLanguageVersion = project.extra["${flavour}JavaLanguageVersion"] as JavaLanguageVersion    
	javaCompiler = javaToolchains.compilerFor {
		languageVersion.set(flavourLanguageVersion)
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

	for (i in 2..4) {
		named("openmdx${i}Jar", Jar::class.java) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	}

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
	            into(layout.buildDirectory.dir("generated/sources/main/java"))
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
    		for (i in 2..4) {
    		    var v: String = "${i}." + project.version.toString();    
		    	// base/Version
				var f: File = file(layout.buildDirectory.dir("generated/sources/openmdx${i}/java/org/openmdx/base/Version.java"))
		        touch(f)
				f.writeText(getVersionClass("org.openmdx.base", v))
		    	// application/Version
		        f = file(layout.buildDirectory.dir("generated/sources/openmdx${i}/java/org/openmdx/application/Version.java"))
		        touch(f)
		        f.writeText(getVersionClass("org.openmdx.application", v))
		        // system/Version
		        f = file(layout.buildDirectory.dir("generated/sources/openmdx${i}/java/org/openmdx/system/Version.java"))
		        touch(f)
		        f.writeText(getVersionClass("org.openmdx.system", v))
	        }
	    }
//	    options.release.set(Integer.valueOf(flavourJavaVersion.majorVersion))
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
			File(buildDirAsFile, "classes/main/java"),
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
			File(buildDirAsFile, "generated/sources/main/java")
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
	  		File(buildDirAsFile, "classes/main/java")
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
			File(buildDirAsFile, "generated/sources/main/java")
		)
		include(openmdxSystemIncludes)
		exclude(openmdxSystemExcludes)
	}
}

distributions {
    main {
        val mainTargetPlatform = JavaVersion.VERSION_1_8
    	distributionBaseName.set("openmdx-" + getProjectImplementationVersion() + "-core-jre-" + mainTargetPlatform)
        contents {
        	// core
        	from(".") { into("core"); include("LICENSE", "*.LICENSE", "NOTICE", "*.properties", "build*.*", "*.xml", "*.kts") }
            from("src") { into("core/src") }
            // etc
            from("etc") { into("core/etc") }
            // rootDir
            from("..") { include("*.properties", "*.kts" ) }
            // jre-...
			var path = "jre-$mainTargetPlatform/${project.name}/lib"
			from("../$path") { into(path) }
			path = "jre-$mainTargetPlatform/gradle/repo"
			from("../$path") { into(path) }
        }
    }
}
