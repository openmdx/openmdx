/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test XRI Protocol Handler
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
package org.openmdx.kernel.xri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmdx.kernel.loading.Resources;

/**
 * Test XRI Protocol Handler
 */
public class XRIProtocolHandlerTest {
	
    /**
     * A resource archove
     */
    private static final String ZIP_RESOURCE = "org/openmdx/kernel/xri/a.zip";

    /**
     * The base URL for relative URL tests
     */
    protected static URL base;
    
	@BeforeAll
    public static void setUp(
    ) throws Exception {
	    System.setProperty("java.protocol.handler.pkgs","org.openmdx.kernel.url.protocol");
        base = newURL(Resources.toResourceXRI("!b!b/c*c/(xri://@d*d/e)?q")); 
    }

    protected static URL newURL(
        String uri
    ) throws MalformedURLException{
        URL url = new URL(null, uri);
        Assertions.assertEquals(uri, url.toExternalForm(), "Absolute XRI");
        return url;
    }

    protected URL assertResolution(
        String uri,
        String resolvedURI
    ) throws MalformedURLException{
        URL url = new URL(base, uri);
        Assertions.assertEquals(resolvedURI, url.toExternalForm(), "Resolved XRI");
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
        URL aResource = newURL("xri:+resource/org/openmdx/kernel/xri/resource.txt");
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
        URL aResource = newURL(Resources.toResourceXRI("org/openmdx/kernel/xri/resource.txt"));
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
        Assertions.assertNotNull(stream, "Missing content in " + id);
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, "UTF-8")
            );
        ) {
            Assertions.assertEquals(0xFEFF, reader.read(), "Introductional character");;
            int i = 0;
            for(
                String l = reader.readLine();
                l != null;
                l = reader.readLine()
            )
				Assertions.assertEquals(expected[i++], l, id + " at line " + i);
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
	    Assertions.assertNotNull(zip, ZIP_RESOURCE);
	    Assertions.assertEquals("file", zip.getProtocol(), "File Resource");
	    URL zipIndex = newURL(XRI_2Protocols.ZIP_PREFIX + zip + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
	    Assertions.assertNotNull(zipIndex, "ZAR Index");
	    Assertions.assertEquals("ZIP index", getHeader(zipIndex), "ZAR header");
	    Assertions.assertTrue(zipIndex.openConnection() instanceof JarURLConnection,"zipIndex connection instance of JarURLConnection ");
	    URL ear = newURL(XRI_2Protocols.ZIP_PREFIX + zip + XRI_2Protocols.ZIP_SEPARATOR + "an.ear");
	    Assertions.assertNotNull(ear, "EAR");
	    URL earIndex = newURL(XRI_2Protocols.ZIP_PREFIX + ear + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
	    Assertions.assertNotNull(earIndex, "EAR Index");
	    Assertions.assertEquals("EAR index", getHeader(earIndex), "EAR header");
	    System.out.println(earIndex.toString());
	    Assertions.assertTrue(earIndex.openConnection() instanceof JarURLConnection,"earIndex connection instance of JarURLConnection ");
	    URL war = newURL(XRI_2Protocols.ZIP_PREFIX + ear + XRI_2Protocols.ZIP_SEPARATOR + "a.war");
	    Assertions.assertNotNull(war, "WAR");
	    URL warIndex = newURL(XRI_2Protocols.ZIP_PREFIX + war + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
	    Assertions.assertNotNull(warIndex, "WAR Index");
	    Assertions.assertEquals("WAR index", getHeader(warIndex), "WAR header");
	    System.out.println(warIndex.toString());
	    Assertions.assertTrue(earIndex.openConnection() instanceof JarURLConnection,"warIndex connection instance of JarURLConnection ");
	    URL jar = newURL(XRI_2Protocols.ZIP_PREFIX + war + XRI_2Protocols.ZIP_SEPARATOR + "WEB-INF/lib/a.jar");
	    Assertions.assertNotNull(jar, "JAR");
	    URL jarIndex = newURL(XRI_2Protocols.ZIP_PREFIX + jar + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
	    Assertions.assertNotNull(jarIndex, "JAR Index");
	    Assertions.assertEquals("JAR index", getHeader(jarIndex), "JAR header");
	    System.out.println(jarIndex.toString());
	    Assertions.assertTrue(jarIndex.openConnection() instanceof JarURLConnection,"jarIndex connection instance of JarURLConnection ");
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
        Assertions.assertNotNull(zip, ZIP_RESOURCE);
        Assertions.assertEquals("file", zip.getProtocol(), "File Resource");
        URL zipIndex = newURL("xri:+zip.(" + zip + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
        Assertions.assertNotNull(zipIndex, "ZAR Index");
        Assertions.assertEquals("ZIP index", getHeader(zipIndex), "ZAR header");
        System.out.println(zipIndex.toString());
        Assertions.assertTrue(zipIndex.openConnection() instanceof JarURLConnection,"zipIndex connection instance of JarURLConnection ");
        URL ear = newURL("xri:+zip.(" + zip + XRI_2Protocols.ZIP_SEPARATOR + "an.ear");
        Assertions.assertNotNull(ear, "EAR");
        URL earIndex = newURL("xri:+zip.(" + ear + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
        Assertions.assertNotNull(earIndex, "EAR Index");
        Assertions.assertEquals("EAR index", getHeader(earIndex), "EAR header");
        System.out.println(earIndex.toString());
        Assertions.assertTrue(earIndex.openConnection() instanceof JarURLConnection,"earIndex connection instance of JarURLConnection ");
        URL war = newURL("xri:+zip.(" + ear + XRI_2Protocols.ZIP_SEPARATOR + "a.war");
        Assertions.assertNotNull(war, "WAR");
        URL warIndex = newURL("xri:+zip.(" + war + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
        Assertions.assertNotNull(warIndex, "WAR Index");
        Assertions.assertEquals("WAR index", getHeader(warIndex), "WAR header");
        System.out.println(warIndex.toString());
        Assertions.assertTrue(earIndex.openConnection() instanceof JarURLConnection,"warIndex connection instance of JarURLConnection ");
        URL jar = newURL("xri:+zip.(" + war + XRI_2Protocols.ZIP_SEPARATOR + "WEB-INF/lib/a.jar");
        Assertions.assertNotNull(jar, "JAR");
        URL jarIndex = newURL("xri:+zip.("+ jar + XRI_2Protocols.ZIP_SEPARATOR + "index.txt");
        Assertions.assertNotNull(jarIndex, "JAR Index");
        Assertions.assertEquals("JAR index", getHeader(jarIndex), "JAR header");
        System.out.println(jarIndex.toString());
        Assertions.assertTrue(jarIndex.openConnection() instanceof JarURLConnection,"jarIndex connection instance of JarURLConnection ");
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
	    Assertions.assertEquals("JAR index", getHeader(jarIndex), "JAR header");
	    Assertions.assertEquals("Test Class Loading Succeeded", object.toString(), "Load Class From EAR");
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
	    try(BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))){
    	    return reader.readLine();
	    }
	}

}
