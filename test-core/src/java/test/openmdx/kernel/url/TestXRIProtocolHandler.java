/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test XRI Protocol Handler
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package test.openmdx.kernel.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.kernel.url.protocol.XRI_2Protocols;

/**
 * Test XRI Protocol Handler
 */
public class TestXRIProtocolHandler {

    /**
     * 
     */
    private static final String ZIP_RESOURCE = "test/openmdx/kernel/url/a.zip";

    /**
     * The base URL for relative URL tests
     */
    protected static URL base;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @BeforeClass
    public static void setUp(
    ) throws Exception {
        base = newURL("xri://+resource/!b!b/c*c/(xri://@d*d/e)?q"); 
    }

    protected static URL newURL(
        String uri
    ) throws MalformedURLException{
        URL url = new URL(uri);
        assertEquals("Absolute XRI", uri, url.toExternalForm());
        return url;
    }

    protected URL assertResolution(
        String uri,
        String resolvedURI
    ) throws MalformedURLException{
        URL url = new URL(base, uri);
        assertEquals("Resolved XRI", resolvedURI, url.toExternalForm());
        return url;
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
    @Test
    public void testResourceAuthority1(
    ) throws Exception {
        URL aResource = newURL("xri:+resource/test/openmdx/kernel/url/resource.txt");
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
    @Test
    public void testResourceAuthority2(
    ) throws Exception {
        URL aResource = newURL("xri://+resource/test/openmdx/kernel/url/resource.txt");
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
      "M\u00fcller"
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
    @Test
	public void testZipAuthority2(
	) throws Exception {
	    URL zip = getClass().getClassLoader().getResource(ZIP_RESOURCE);
	    assertNotNull(ZIP_RESOURCE, zip);
	    assertEquals("File Resource", "file", zip.getProtocol());
	    URL zipIndex = newURL(XRI_2Protocols.ZIP_PREFIX + zip + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
	    assertNotNull("ZAR Index", zipIndex);
	    assertEquals("ZAR header", "ZIP index", getHeader(zipIndex));
	    assertTrue("zipIndex connection instance of JarURLConnection ", zipIndex.openConnection() instanceof JarURLConnection);
	    URL ear = newURL(XRI_2Protocols.ZIP_PREFIX + zip + XRI_2Protocols.ZIP_SEPARATOR + "an.ear");
	    assertNotNull("EAR", ear);
	    URL earIndex = newURL(XRI_2Protocols.ZIP_PREFIX + ear + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
	    assertNotNull("EAR Index", earIndex);
	    assertEquals("EAR header", "EAR index", getHeader(earIndex));
	    System.out.println(earIndex.toString());
	    assertTrue("earIndex connection instance of JarURLConnection ", earIndex.openConnection() instanceof JarURLConnection);
	    URL war = newURL(XRI_2Protocols.ZIP_PREFIX + ear + XRI_2Protocols.ZIP_SEPARATOR + "a.war");
	    assertNotNull("WAR", war);
	    URL warIndex = newURL(XRI_2Protocols.ZIP_PREFIX + war + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
	    assertNotNull("WAR Index", warIndex);
	    assertEquals("WAR header", "WAR index", getHeader(warIndex));
	    System.out.println(warIndex.toString());
	    assertTrue("warIndex connection instance of JarURLConnection ", earIndex.openConnection() instanceof JarURLConnection);
	    URL jar = newURL(XRI_2Protocols.ZIP_PREFIX + war + XRI_2Protocols.ZIP_SEPARATOR + "WEB-INF/lib/a.jar");
	    assertNotNull("JAR", jar);
	    URL jarIndex = newURL(XRI_2Protocols.ZIP_PREFIX + jar + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
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
    @Test
    public void testZipAuthority1(
    ) throws Exception {
        URL zip = getClass().getClassLoader().getResource(ZIP_RESOURCE);
        assertNotNull(ZIP_RESOURCE, zip);
        assertEquals("File Resource", "file", zip.getProtocol());
        URL zipIndex = newURL("xri:+zip.(" + zip + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
        assertNotNull("ZAR Index", zipIndex);
        assertEquals("ZAR header", "ZIP index", getHeader(zipIndex));
        System.out.println(zipIndex.toString());
        assertTrue("zipIndex connection instance of JarURLConnection ", zipIndex.openConnection() instanceof JarURLConnection);
        URL ear = newURL("xri:+zip.(" + zip + XRI_2Protocols.ZIP_SEPARATOR + "an.ear");
        assertNotNull("EAR", ear);
        URL earIndex = newURL("xri:+zip.(" + ear + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
        assertNotNull("EAR Index", earIndex);
        assertEquals("EAR header", "EAR index", getHeader(earIndex));
        System.out.println(earIndex.toString());
        assertTrue("earIndex connection instance of JarURLConnection ", earIndex.openConnection() instanceof JarURLConnection);
        URL war = newURL("xri:+zip.(" + ear + XRI_2Protocols.ZIP_SEPARATOR + "a.war");
        assertNotNull("WAR", war);
        URL warIndex = newURL("xri:+zip.(" + war + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
        assertNotNull("WAR Index", warIndex);
        assertEquals("WAR header", "WAR index", getHeader(warIndex));
        System.out.println(warIndex.toString());
        assertTrue("warIndex connection instance of JarURLConnection ", earIndex.openConnection() instanceof JarURLConnection);
        URL jar = newURL("xri:+zip.(" + war + XRI_2Protocols.ZIP_SEPARATOR + "WEB-INF/lib/a.jar");
        assertNotNull("JAR", jar);
        URL jarIndex = newURL("xri:+zip.("+ jar + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
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
    @Test
	public void testZipClassLoader(
	) throws Exception {
	    URL jar = newURL(XRI_2Protocols.ZIP_PREFIX + XRI_2Protocols.ZIP_PREFIX + XRI_2Protocols.ZIP_PREFIX + getClass().getClassLoader().getResource(ZIP_RESOURCE) + XRI_2Protocols.ZIP_SEPARATOR + "an.ear" + XRI_2Protocols.ZIP_SEPARATOR + "a.war" + XRI_2Protocols.ZIP_SEPARATOR + "WEB-INF/lib/a.jar");
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

    @Test
	public void testNormalExamples(
	) throws MalformedURLException{
	    assertResolution("!g!g", "xri://+resource/!b!b/c*c/!g!g");
	    assertResolution("./!g!g", "xri://+resource/!b!b/c*c/!g!g");
	    assertResolution("!g!g/", "xri://+resource/!b!b/c*c/!g!g/");
	    assertResolution("/!g!g", "xri://+resource/!g!g");
	    //@!g!g = Not a legal relative XRI reference
	    assertResolution("?y", "xri://+resource/!b!b/c*c/(xri://@d*d/e)?y");
	    assertResolution("!g!g?y", "xri://+resource/!b!b/c*c/!g!g?y");
	    assertResolution("#s", "xri://+resource/!b!b/c*c/(xri://@d*d/e)?q#s");
	    assertResolution("!g!g#s", "xri://+resource/!b!b/c*c/!g!g#s");
	    assertResolution("!g!g?y#s", "xri://+resource/!b!b/c*c/!g!g?y#s");
	    assertResolution(";x", "xri://+resource/!b!b/c*c/;x");
	    assertResolution("!g!g;x", "xri://+resource/!b!b/c*c/!g!g;x");
	    assertResolution("!g!g;x?y#s", "xri://+resource/!b!b/c*c/!g!g;x?y#s");
	    assertResolution("", "xri://+resource/!b!b/c*c/(xri://@d*d/e)?q");
	    assertResolution(".", "xri://+resource/!b!b/c*c/");
	    assertResolution("./", "xri://+resource/!b!b/c*c/");
	    assertResolution("..", "xri://+resource/!b!b/");
	    assertResolution("../", "xri://+resource/!b!b/");
	    assertResolution("../!g!g", "xri://+resource/!b!b/!g!g");
	    assertResolution("../..", "xri://+resource/");
	    assertResolution("../../", "xri://+resource/");
	    assertResolution("../../!g!g", "xri://+resource/!g!g");
	}	    
	
    @Test
    public void testAbnormalExamples(
    ) throws MalformedURLException {
        //
        // As in IRIs and URIs, the ".." syntax cannot be used to change the authority component of an XRI.
        //
        assertResolution("../../../!g!g", "xri://+resource/!g!g");
        assertResolution("../../../../!g!g", "xri://+resource/!g!g");
        //
        // As in IRIs and URIs, "." and ".." have a special meaning only when they appear as complete path segments.
        //
        assertResolution("/./!g!g", "xri://+resource/!g!g");
        assertResolution("/../!g!g", "xri://+resource/!g!g");
        assertResolution("!g!g.", "xri://+resource/!b!b/c*c/!g!g.");
        assertResolution(".!g!g", "xri://+resource/!b!b/c*c/.!g!g");
        assertResolution("!g!g..", "xri://+resource/!b!b/c*c/!g!g..");
        assertResolution("..!g!g", "xri://+resource/!b!b/c*c/..!g!g");
        //
        //  XRI parsers, like IRI and URI parsers, must be prepared for superfluous or nonsensical uses of "." and "..".
        //
        assertResolution("./../!g!g", "xri://+resource/!b!b/!g!g");
        assertResolution("./!g!g/.", "xri://+resource/!b!b/c*c/!g!g/");
        assertResolution("!g!g/./h", "xri://+resource/!b!b/c*c/!g!g/h");
        assertResolution("!g!g/../h", "xri://+resource/!b!b/c*c/h");
        assertResolution("!g!g;x=1/./y", "xri://+resource/!b!b/c*c/!g!g;x=1/y");
        assertResolution("!g!g;x=1/../y", "xri://+resource/!b!b/c*c/y");
        //
        // XRI parsers, like IRI and URI parsers, must take care to separate the reference's query and/or fragment components from the path component before merging it with the base path and removing dot-segments.
        //
        assertResolution("!g!g?y/./x", "xri://+resource/!b!b/c*c/!g!g?y/./x");
        assertResolution("!g!g?y/../x", "xri://+resource/!b!b/c*c/!g!g?y/../x");
        assertResolution("!g!g#s/./x", "xri://+resource/!b!b/c*c/!g!g#s/./x");
        assertResolution("!g!g#s/../x", "xri://+resource/!b!b/c*c/!g!g#s/../x");
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

}
