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
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

// TODO update versions where restriction came from runtime target
dependencies {
    constraints {

        val atomikosVersion = "6.0.0"
        val bootstrapVersion = "4.19.5"  // 5.19.5 isn't released yet
        val cacheVersion = "1.1.1"
        val flexmarkVersion = "0.64.8"
        val groovyVersion = "3.0.+"
        val jdoVersion = "3.1"
        val jakartaVersion = "10.0.0"
        val junitVersion = "5.13.0"
        val junitPlatformVersion = "1.13.0"
        val ldapVersion = "2.1.+"
        val manifoldVersion = "2025.1.11"
        val mockitoVersion = "5.18.0"
        val oracleVersion = "23.8.0.25.04"
        val postgresVersion = "42.7.+"
        val radiusVersion = "1.1.+"
        val servletVersion = "6.1.+"
        val tomcatVersion = "11.0.+"

        api("com.atomikos:transactions-jta:$atomikosVersion:jakarta")
        api("com.atomikos:transactions-jdbc:$atomikosVersion:jakarta")
        api("com.vladsch.flexmark:flexmark:$flexmarkVersion")
        api("jakarta.platform:jakarta.jakartaee-api:$jakartaVersion")
        api("jakarta.servlet:jakarta.servlet-api:$servletVersion")
        api("javax.cache:cache-api:$cacheVersion")
        api("javax.jdo:jdo-api:$jdoVersion")
        api("org.apache.directory.api:apache-ldap-api:$ldapVersion")
        api("org.apache.tomcat:tomcat-catalina:$tomcatVersion")
        api("org.codehaus.groovy:groovy:$groovyVersion")
        api("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        api("org.mockito:mockito-core:$mockitoVersion")
        api("org.mockito:mockito-junit-jupiter:$mockitoVersion")
        api("org.openmdx:openmdx-base:$bootstrapVersion")
        api("org.tinyradius:tinyradius:$radiusVersion")
        api("systems.manifold:manifold-preprocessor:$manifoldVersion")

        runtime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
        runtime("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
        runtime("com.oracle.database.jdbc:ojdbc17:$oracleVersion")
        runtime("org.postgresql:postgresql:$postgresVersion")
    }

}
