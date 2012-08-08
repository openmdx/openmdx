/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestXRIProtocolHandler.java,v 1.11 2005/08/16 22:33:04 hburger Exp $
 * Description: Test XRI Protocol Handler
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/08/16 22:33:04 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.kernel.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.kernel.url.protocol.XriProtocols;

/**
 * Test XRI Protocol Handler
 */
public class TestXRIProtocolHandler 
    extends TestCase
{

    /**
     * Constructs a test case with the given name.
     */
    public TestXRIProtocolHandler(String name) {
        super(name);
    }
    
    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    
    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite() {
        return new TestSuite(TestXRIProtocolHandler.class);
    }

    
    //------------------------------------------------------------------------
    // test +resource authority
    //------------------------------------------------------------------------

    /**
     * Test Resource Loading
     * <p>
     * Test starts with "test" for reflective invocation
     * 
     * @throws Exception
     */
    public void testResourceAuthority1(
    ) throws Exception {
        URL aResource = new URL(
            "xri:+resource/org/openmdx/test/kernel/url/resource.txt"
        );
        verifyContent(
            aResource.toString(),
            aResource.openStream(),
            EXPECTED_CONTENT
        );
    }
        
    /**
     * Test Resource Loading
     * <p>
     * Test starts with "test" for reflective invocation
     * 
     * @throws Exception
     */
    public void testResourceAuthority2(
    ) throws Exception {
        URL aResource = new URL(
            "xri://+resource/org/openmdx/test/kernel/url/resource.txt"
        );
        verifyContent(
            aResource.toString(),
            aResource.openStream(),
            EXPECTED_CONTENT
        );
    }

    /**
     * Content Verification
     * 
     * @param id
     * @param stream
     * 
     * @throws IOException
     */
    protected void verifyContent(
        String id,
        InputStream stream,
        String[] expected
    ) throws IOException{
        assertNotNull("Missing content in " + id, stream);
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, "UTF-8")
        );
        try {
            assertEquals("Introductional character",0xFEFF, reader.read());
            int i = 0;
            for(
                String l = reader.readLine();
                l != null;
                l = reader.readLine()
            ) assertEquals(id + " at line " + i, expected[i++], l);
        } finally {
            reader.close();
        }
    }

    /**
     * 
     */
    private static final String[] EXPECTED_CONTENT = new String[]{
      "Meier",
      "M\u00fcller" // "Müller"
    };

    
    //------------------------------------------------------------------------
    // test +zip authority
    //------------------------------------------------------------------------

	/**
	 * Test Resource Loading
	 * <p>
	 * Test starts with "test" for reflective invocation
	 * 
	 * @throws Exception
	 */
	public void testZipAuthority2(
	) throws Exception {
	    URL zip = getClass().getClassLoader().getResource(ZIP_RESOURCE);
	    assertNotNull(ZIP_RESOURCE, zip);
	    assertEquals("File Resource", "file", zip.getProtocol());
	    URL zipIndex = new URL(XriProtocols.ZIP_PREFIX + zip + XriProtocols.ZIP_SEPARATOR + "index.txt");
	    assertNotNull("ZAR Index", zipIndex);
	    assertEquals("ZAR header", "ZIP index", getHeader(zipIndex));
	    assertTrue("zipIndex connection instance of JarURLConnection ", zipIndex.openConnection() instanceof JarURLConnection);
	    URL ear = new URL(XriProtocols.ZIP_PREFIX + zip + XriProtocols.ZIP_SEPARATOR + "an.ear");
	    assertNotNull("EAR", ear);
	    URL earIndex = new URL(XriProtocols.ZIP_PREFIX + ear + XriProtocols.ZIP_SEPARATOR + "index.txt");
	    assertNotNull("EAR Index", earIndex);
	    assertEquals("EAR header", "EAR index", getHeader(earIndex));
	    System.out.println(earIndex.toString());
	    assertTrue("earIndex connection instance of JarURLConnection ", earIndex.openConnection() instanceof JarURLConnection);
	    URL war = new URL(XriProtocols.ZIP_PREFIX + ear + XriProtocols.ZIP_SEPARATOR + "a.war");
	    assertNotNull("WAR", war);
	    URL warIndex = new URL(XriProtocols.ZIP_PREFIX + war + XriProtocols.ZIP_SEPARATOR + "index.txt");
	    assertNotNull("WAR Index", warIndex);
	    assertEquals("WAR header", "WAR index", getHeader(warIndex));
	    System.out.println(warIndex.toString());
	    assertTrue("warIndex connection instance of JarURLConnection ", earIndex.openConnection() instanceof JarURLConnection);
	    URL jar = new URL(XriProtocols.ZIP_PREFIX + war + XriProtocols.ZIP_SEPARATOR + "WEB-INF/lib/a.jar");
	    assertNotNull("JAR", jar);
	    URL jarIndex = new URL(XriProtocols.ZIP_PREFIX + jar + XriProtocols.ZIP_SEPARATOR + "index.txt");
	    assertNotNull("JAR Index", jarIndex);
	    assertEquals("JAR header", "JAR index", getHeader(jarIndex));
	    System.out.println(jarIndex.toString());
	    assertTrue("jarIndex connection instance of JarURLConnection ", jarIndex.openConnection() instanceof JarURLConnection);
	}

    /**
     * Test Resource Loading
     * <p>
     * Test starts with "test" for reflective invocation
     * 
     * @throws Exception
     */
    public void testZipAuthority1(
    ) throws Exception {
        URL zip = getClass().getClassLoader().getResource(ZIP_RESOURCE);
        assertNotNull(ZIP_RESOURCE, zip);
        assertEquals("File Resource", "file", zip.getProtocol());
        URL zipIndex = new URL("xri:+zip.(" + zip + XriProtocols.ZIP_SEPARATOR + "index.txt");
        assertNotNull("ZAR Index", zipIndex);
        assertEquals("ZAR header", "ZIP index", getHeader(zipIndex));
        System.out.println(zipIndex.toString());
        assertTrue("zipIndex connection instance of JarURLConnection ", zipIndex.openConnection() instanceof JarURLConnection);
        URL ear = new URL("xri:+zip.(" + zip + XriProtocols.ZIP_SEPARATOR + "an.ear");
        assertNotNull("EAR", ear);
        URL earIndex = new URL("xri:+zip.(" + ear + XriProtocols.ZIP_SEPARATOR + "index.txt");
        assertNotNull("EAR Index", earIndex);
        assertEquals("EAR header", "EAR index", getHeader(earIndex));
        System.out.println(earIndex.toString());
        assertTrue("earIndex connection instance of JarURLConnection ", earIndex.openConnection() instanceof JarURLConnection);
        URL war = new URL("xri:+zip.(" + ear + XriProtocols.ZIP_SEPARATOR + "a.war");
        assertNotNull("WAR", war);
        URL warIndex = new URL("xri:+zip.(" + war + XriProtocols.ZIP_SEPARATOR + "index.txt");
        assertNotNull("WAR Index", warIndex);
        assertEquals("WAR header", "WAR index", getHeader(warIndex));
        System.out.println(warIndex.toString());
        assertTrue("warIndex connection instance of JarURLConnection ", earIndex.openConnection() instanceof JarURLConnection);
        URL jar = new URL("xri:+zip.(" + war + XriProtocols.ZIP_SEPARATOR + "WEB-INF/lib/a.jar");
        assertNotNull("JAR", jar);
        URL jarIndex = new URL("xri:+zip.("+ jar + XriProtocols.ZIP_SEPARATOR + "index.txt");
        assertNotNull("JAR Index", jarIndex);
        assertEquals("JAR header", "JAR index", getHeader(jarIndex));
        System.out.println(jarIndex.toString());
        assertTrue("jarIndex connection instance of JarURLConnection ", jarIndex.openConnection() instanceof JarURLConnection);
    }

    /**
	 * Test Resource Loading
	 * <p>
	 * Test starts with "test" for reflective invocation
	 * 
	 * @throws Exception
	 */
	public void testZipClassLoader(
	) throws Exception {
	    URL jar = new URL(XriProtocols.ZIP_PREFIX + XriProtocols.ZIP_PREFIX + XriProtocols.ZIP_PREFIX + getClass().getClassLoader().getResource(ZIP_RESOURCE) + XriProtocols.ZIP_SEPARATOR + "an.ear" + XriProtocols.ZIP_SEPARATOR + "a.war" + XriProtocols.ZIP_SEPARATOR + "WEB-INF/lib/a.jar");
	    System.out.println(jar.toString());
	    ClassLoader classLoader = new URLClassLoader(new URL[]{jar}); 
	    Object object = Class.forName(
	        "org.openmdx.test.kernel.url.TestClass",
	        true,
	        classLoader            
	    ).newInstance();
	    URL jarIndex = classLoader.getResource("index.txt");
	    assertEquals("JAR header", "JAR index", getHeader(jarIndex));
	    assertEquals("Load Class From EAR", "Test Class Loading Succeeded", object.toString());
	}

	/**
	 * Get the first line
	 * 
	 * @param url
	 * @return
	 */
	private static String getHeader(URL url) throws IOException{
	    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
	    String line = reader.readLine();
	    reader.close();
	    return line;
	}

	/**
	 * 
	 */
	private static final String ZIP_RESOURCE = "org/openmdx/test/kernel/url/a.zip";

}
