/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PathTransformationTest.java,v 1.7 2009/06/02 13:03:37 hburger Exp $
 * Description: Path Transformation Test
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/02 13:03:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package test.openmdx.base.naming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.naming.XRI_2Marshaller;

public class PathTransformationTest {
    
    /**
     * Add an instance variable for each part of the fixture 
     */
    protected String[] uri1;

    /**
     * Add an instance variable for each part of the fixture 
     */
    private String[] uri2;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected String[] xri1;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected String[] xri2;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected String[] iri2;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected String xri1compatibility_16;
    
    /**
     * Add an instance variable for each part of the fixture 
     */
    protected String[] paths;

    private static final boolean LEGACY_STRING_REPRESENTATION = true;
    
    @Before
    public void setUp() {
        paths = new String[]{
            null, // Must be the first entry
            "org::openmdx::preferences1/provider/Java::Properties/segment/(java::comp//env)",
            "org::openmdx::preferences1/provider/Java::Properties/segment/(java::comp)",
            "",
            "::*",
            "A::B",
            "A::B/::*",
            "org::openmdx::preferences1/provider/Java::Properties/segment/System",
            "org::openmdx::preferences1/provider/Java::Properties/segment/(+resource//application-preferences.xml)",
            "A::B/Fran\u00e7ois",
            "A::B/provider/P::Q/segment/S.T/object/RR_1;state=0",
            "A::B/provider/P::Q/segment/S.T/object/012345;transient",
            "A::B/B::::B0//B1/C",
            "A::B/B//B0%3AB1/C",
            "A::B/provider/${PROVIDER}/segment/${SEGMENT}",
            "A::B/ !\"$%00&'*+,-.0123456789::;<=>@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~", // '(', ')', '#' & '?' require escape!
            "::*/provider/::*/segment/%",
            "A::B/provider/::org*/segment/org%",
            "A::B/provider/::org::*/segment/org::%"
        };
        uri1 = new String[]{
            null,
            "spice://org:openmdx:preferences1/provider/Java:Properties/segment/(java:comp%2fenv)",
            "spice://org:openmdx:preferences1/provider/Java:Properties/segment/(java:comp)",
            "spice:/",
            "spice://:*",
            "spice://A:B",
            "spice://A:B/:*",
            "spice://org:openmdx:preferences1/provider/Java:Properties/segment/System",
            "spice://org:openmdx:preferences1/provider/Java:Properties/segment/(+resource%2fapplication-preferences.xml)",
            "spice://A:B/Fran%c3%a7ois",
            "spice://A:B/provider/P:Q/segment/S.T/object/RR_1;state=0",
            "spice://A:B/provider/P:Q/segment/S.T/object/012345;transient",
            "spice://A:B/B%3a%3aB0%2fB1/C",
            "spice://A:B/B%2fB0%253AB1/C",
            "spice://A:B/provider/$%7bPROVIDER%7d/segment/$%7bSEGMENT%7d",
            "spice://A:B/%20!%22$%2500&'*+,-.0123456789:;%3c=%3e@ABCDEFGHIJKLMNOPQRSTUVWXYZ%5b%5c%5d%5e_%60abcdefghijklmnopqrstuvwxyz%7b%7c%7d~",
            "spice://:*/provider/:*/segment/%",
            "spice://A:B/provider/:org*/segment/org%",
            "spice://A:B/provider/:org:*/segment/org:%"
        };
        xri1 = new String[]{
            null,
            "xri:@openmdx:org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp/env)",
            "xri:@openmdx:org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp)",
            "xri:@openmdx",
            "xri:@openmdx:**",
            "xri:@openmdx:A.B",
            "xri:@openmdx:A.B/**",
            "xri:@openmdx:org.openmdx.preferences1/provider/Java:Properties/segment/System",
            "xri:@openmdx:org.openmdx.preferences1/provider/Java:Properties/segment/(+resource/application-preferences.xml)",
            "xri:@openmdx:A.B/Fran\u00e7ois",
            "xri:@openmdx:A.B/provider/P:Q/segment/S.T/object/RR_1;state=0",
            "xri:@openmdx:A.B/provider/P:Q/segment/S.T/object/012345;transient",
            "xri:@openmdx:A.B/(@openmdx:B.B0/B1)/C",
            "xri:@openmdx:A.B/(@openmdx:B/B0%253AB1)/C",
            "xri:@openmdx:A.B/provider/$%7BPROVIDER%7D/segment/$%7BSEGMENT%7D",
            "xri:@openmdx:A.B/%20!%22$%2500&'*+,-.0123456789:;%3C=%3E@ABCDEFGHIJKLMNOPQRSTUVWXYZ%5B%5C%5D%5E_%60abcdefghijklmnopqrstuvwxyz%7B%7C%7D~",
            "xri:@openmdx:**/provider/**/segment/***",
            "xri:@openmdx:A.B/provider/org**/segment/org***",
            "xri:@openmdx:A.B/provider/org:**/segment/org:***",
            "xri:@openmdx:A.B/(@openmdx:B/B0%3AB1)/C",
            "xri:(ejb/e.jar)",
            "xri:(../../somewhere/else/e.jar)",
            "xri://www.openmdx.org/dtd/openmdx-ejb-jar_1_0.dtd"
        };
        xri1compatibility_16 = "xri:@openmdx:*/provider/:*/segment/%";
        xri2 = new String[]{
            null,
            "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp/env)",
            "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp)",
            "xri://@openmdx",
            "xri://@openmdx*($..)",
            "xri://@openmdx*A.B",
            "xri://@openmdx*A.B/($..)",
            "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/System",
            "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(+resource/application-preferences.xml)",
            "xri://@openmdx*A.B/Fran\u00e7ois",
            "xri://@openmdx*A.B/provider/P:Q/segment/S.T/object/($t*ces*RR_1%3Bstate%3D0)",
            "xri://@openmdx*A.B/provider/P:Q/segment/S.T/object/012345;transient",
            "xri://@openmdx*A.B/(@openmdx*B.B0/B1)/C",
            "xri://@openmdx*A.B/(@openmdx*B/($t*ces*B0%253AB1))/C",
            "xri://@openmdx*A.B/provider/($t*ces*%24%7BPROVIDER%7D)/segment/($t*ces*%24%7BSEGMENT%7D)",
            "xri://@openmdx*A.B/($t*ces*%20%21%22%24%2500%26%27%2A%2B%2C-.0123456789%3A%3B%3C%3D%3E%40ABCDEFGHIJKLMNOPQRSTUVWXYZ%5B%5C%5D%5E_%60abcdefghijklmnopqrstuvwxyz%7B%7C%7D~)",
            "xri://@openmdx*($..)/provider/($..)/segment/($...)",
            "xri://@openmdx*A.B/provider/($.*org)*($..)/segment/($.*org)*($..)/($...)",
            "xri://@openmdx*A.B/provider/($.*org:)*($..)/segment/($.*org:)*($..)/($...)", // A::B/provider/::org::*/segment/org::%
            "xri://@openmdx*A.B/(@openmdx*B/($t*ces*B0%253AB1))/C",
            "xri://(ejb/e.jar)",
            "xri://(../../somewhere/else/e.jar)",
            "xri://www.openmdx.org/dtd/openmdx-ejb-jar_1_0.dtd",
            "xri://@openmdx*(+example)/provider/java:properties/segment/(java:comp/env)",
            "xri://@openmdx*(+example)/provider/java:properties*($(+class)/org::openmdx::base::Provider)"
        };
        iri2 = new String[]{
            null,
            "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp%2Fenv)",
            "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp)",
            "xri://@openmdx",
            "xri://@openmdx*($..)",
            "xri://@openmdx*A.B",
            "xri://@openmdx*A.B/($..)",
            "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/System",
            "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(+resource%2Fapplication-preferences.xml)",
            "xri://@openmdx*A.B/Fran\u00e7ois",
            "xri://@openmdx*A.B/provider/P:Q/segment/S.T/object/($t*ces*RR_1%253Bstate%253D0)",
            "xri://@openmdx*A.B/provider/P:Q/segment/S.T/object/012345;transient",
            "xri://@openmdx*A.B/(@openmdx*B.B0%2FB1)/C",
            "xri://@openmdx*A.B/(@openmdx*B%2F($t*ces*B0%25253AB1))/C",
            "xri://@openmdx*A.B/provider/($t*ces*%2524%257BPROVIDER%257D)/segment/($t*ces*%2524%257BSEGMENT%257D)",            
            "xri://@openmdx*A.B/($t*ces*%2520%2521%2522%2524%252500%2526%2527%252A%252B%252C-.0123456789%253A%253B%253C%253D%253E%2540ABCDEFGHIJKLMNOPQRSTUVWXYZ%255B%255C%255D%255E_%2560abcdefghijklmnopqrstuvwxyz%257B%257C%257D~)",
            "xri://@openmdx*($..)/provider/($..)/segment/($...)",
            "xri://@openmdx*A.B/provider/($.*org)*($..)/segment/($.*org)*($..)/($...)",
            "xri://@openmdx*A.B/provider/($.*org:)*($..)/segment/($.*org:)*($..)/($...)",
            "xri://@openmdx*A.B/(@openmdx*B%2F($t*ces*B0%25253AB1))/C",
            "xri://(ejb%2Fe.jar)",
            "xri://(..%2F..%2Fsomewhere%2Felse%2Fe.jar)",
            "xri://www.openmdx.org/dtd/openmdx-ejb-jar_1_0.dtd",
            "xri://@openmdx*(+example)/provider/java:properties/segment/(java:comp%2Fenv)",
            "xri://@openmdx*(+example)/provider/java:properties*($(+class)%2Forg::openmdx::base::Provider)"
        };
        uri2 = new String[]{
            null,
            "@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp%2Fenv)",
            "@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp)",
            "@openmdx",
            "@openmdx*($..)",
            "@openmdx*A.B",
            "@openmdx*A.B/($..)",
            "@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/System",
            "@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(+resource%2Fapplication-preferences.xml)",
            "@openmdx*A.B/Fran%C3%A7ois",
            "@openmdx*A.B/provider/P:Q/segment/S.T/object/($t*ces*RR_1%253Bstate%253D0)",
            "@openmdx*A.B/provider/P:Q/segment/S.T/object/012345;transient",
            "@openmdx*A.B/(@openmdx*B.B0%2FB1)/C",
            "@openmdx*A.B/(@openmdx*B%2F($t*ces*B0%25253AB1))/C",
            "@openmdx*A.B/provider/($t*ces*%2524%257BPROVIDER%257D)/segment/($t*ces*%2524%257BSEGMENT%257D)",            
            "@openmdx*A.B/($t*ces*%2520%2521%2522%2524%252500%2526%2527%252A%252B%252C-.0123456789%253A%253B%253C%253D%253E%2540ABCDEFGHIJKLMNOPQRSTUVWXYZ%255B%255C%255D%255E_%2560abcdefghijklmnopqrstuvwxyz%257B%257C%257D~)",
            "@openmdx*($..)/provider/($..)/segment/($...)",
            "@openmdx*A.B/provider/($.*org)*($..)/segment/($.*org)*($..)/($...)",
            "@openmdx*A.B/provider/($.*org:)*($..)/segment/($.*org:)*($..)/($...)",
            "@openmdx*A.B/(@openmdx*B%2F($t*ces*B0%25253AB1))/C",
        };
    }

//    @Test
//    public void testX2P(
//    ) throws ServiceException {
//        for (
//            int i = 1;
//            i < paths.length;
//            i++
//        ) assertEquals(
//            "[" + i + "]: " + paths[i],
//            paths[i],
//            new Path(xri1[i]).toString()
//        );
//    }
//
//    @Test
//    public void testU2P(
//    ) throws ServiceException {
//        for (
//            int i = 1;
//            i < uri1.length;
//            i++
//        ) {
//            assertEquals(
//                "[" + i + "]: " + paths[i],
//                paths[i],
//                new Path(uri1[i]).toString()
//            );
//            assertEquals(
//                "[" + i + "]: " + paths[i],
//                paths[i],
//                new Path(uri2[i]).toString()
//            );
//        }
//    }
//
//    @Test
//    public void testP2P(
//    ) throws ServiceException {
//        for (
//            int i = 1;
//            i < paths.length;
//            i++
//        ) assertEquals(
//            "[" + i + "]: " + paths[i],
//            paths[i],
//            new Path(paths[i]).toString()
//        );
//    }
//    
    @Test
    public void testO2P(
    ) throws ServiceException {
        for (
            int i = 1;
            i < paths.length;
            i++
        ) assertEquals(
            "[" + i + "]: " + paths[i],
            new Path(paths[i]),
            new Path(xri2[i])
        );
    }

    @Test
    public void testO2I(
    ) throws ServiceException {
        for (
            int i = 16;
            i < paths.length;
            i++
        ) assertEquals(
            "[" + i + "]: " + paths[i],
            iri2[i],
            new Path(paths[i]).toIRI().toString()
        );
    }

    @Test
    public void testI2P(
    ) throws ServiceException, URISyntaxException {
        for (
            int i = 1;
            i < paths.length;
            i++
        ) assertEquals (
            "[" + i + "]: " + paths[i],
            new Path(paths[i]),
            new Path(new URI(iri2[i]))
        );
    }

    @Test
    public void testU2U(
    ) throws ServiceException, URISyntaxException {
        for (
            int i = 1;
            i < paths.length;
            i++
        ) {
            URI uri = new URI(iri2[i]);
            Path path = new Path(paths[i]); 
            assertEquals (
                "[" + i + "]: " + paths[i],
                iri2[i],
                uri.toString()
            );
            int s = iri2[i].indexOf('/', 6);
            assertEquals(
                "[" + i + "]: " + paths[i],
                iri2[i].substring(6, s > 0 ? s : iri2[i].length()),
                uri.getAuthority()
            );
            assertEquals(
                "[" + i + "]: " + paths[i],
                "xri",
                uri.getScheme()
            );
            assertEquals(
                "[" + i + "]: " + paths[i],
                xri2[i].substring(4),
                uri.getSchemeSpecificPart()
            );
            if(path.size() > 0){
                assertEquals(
                    "[" + i + "]: " + paths[i],
                    new Path("test").getDescendant(path.getSuffix(1)),
                    new Path("xri://@openmdx*test" + uri.getPath())
                );
            }
        }
    }
    
    
    @Test
    public void testP2O(
    ) throws ServiceException {
        for (
            int i = 1;
            i < paths.length;
            i++
        ) assertEquals(
            "[" + i + "]: " + paths[i],
            xri2[i],
            XRI_2Marshaller.getInstance().marshal(
                new Path(paths[i]).getSuffix(0)
            ).toString()
        );
    }

    @Test
    public void testP2I(
    ) throws ServiceException {
        for (
            int i = 1;
            i < 19;
            i++
        ){
            String xriValue = xri2[i];
            String iriValue = iri2[i];
            URI iri = new Path(xriValue).toIRI();
            assertEquals("IRI [" + i + "]: " + iriValue, iriValue, iri.toString());
        }
    }
    
    @Test
    public void testI2X(
    ) throws ServiceException {
        for (
            int i = 1;
            i < 19;
            i++
        ) assertEquals(
            "[" + i + "]: " + iri2[i],
            xri2[i],
            new Path(URI.create(iri2[i])).toXRI()
        );
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testP2U(
    ) throws ServiceException {
        for (
            int i = 1;
            i < paths.length;
            i++
        ) assertEquals(
            "[" + i + "]: " + paths[i],
            uri1[i],
            new Path(paths[i]).toUri()
        );
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testP2X(
    ) throws ServiceException {
        for (
            int i = 1;
            i < paths.length;
            i++
        ) assertEquals(
            "[" + i + "]: " + paths[i],
            xri1[i],
            new Path(paths[i]).toXri().toString()
        );
        assertEquals(
            xri1compatibility_16,
            xri1[16],
            new Path(xri1compatibility_16).toXri().toString()
        );
    }

    @Test
    public void testI2I(
    ) throws ServiceException {
        for (
            int i = 1;
            i < iri2.length;
            i++
        ) assertEquals(
            "[" + i + "]: " + iri2[i],
            iri2 [i],
            URI.create(iri2[i]).toString()
        );
    }

    @Test
    public void testX2I(
    ) throws ServiceException {
        for (
            int i = 1;
            i < 19;
            i++
        ) {
            Path objectId =new Path(xri2[i]); 
            URI iri = objectId.toIRI();
            assertEquals(
	            "iri2["+i+"] ("+xri2[i]+")",
	            iri2[i],
	            iri.toString()
	        );
            assertEquals(
                "uri2["+i+"] ("+xri2[i]+")",
	            uri2[i],
	            objectId.toURI()
	        );
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCR0003329(
    ){
        Path p = new Path("xri:@openmdx:a/b/c").add(new Path("xri:@openmdx:1/2/3")); 
        Path p1 = new Path(p.toXri());
        assertEquals(
            "p.toString()", 
            LEGACY_STRING_REPRESENTATION ? "a/b/c/1//2//3" : "xri://@openmdx*a/b/c/(@openmdx*1/2/3)", 
            p.toString()
        );
        assertEquals("p.toXri()", "xri:@openmdx:a/b/c/(@openmdx:1/2/3)", p.toXri());
        assertEquals("p.toUri()", "spice://a/b/c/1%2f2%2f3", p.toUri());
        assertEquals("1/2/3", p.getBase(), p1.getBase());
        Path p2 = new Path(new String[]{"x", "y", "a/b/c/1//2//3"});
        assertEquals(
            "p2.toString()", 
            LEGACY_STRING_REPRESENTATION ? "x/y/a//b//c//1////2////3" : "xri://@openmdx*x/y/(@openmdx*a/b/c/(@openmdx*1/2/3))", 
            p2.toString()
        );
        assertEquals("p2.toXri()", "xri:@openmdx:x/y/(@openmdx:a/b/c/(@openmdx:1/2/3))", p2.toXri());
        assertEquals("p2.toUri()", "spice://x/y/a%2fb%2fc%2f1%2f%2f2%2f%2f3", p2.toUri());
//        XRI xref = new XRI("@openmdx:1/2/3");
//        assertTrue("isAbsolute()", xref.isAbsolute());
//        assertFalse("isRelative()", xref.isRelative());
//        assertEquals("xref aurhority path", "@openmdx:1", xref.getAuthorityPath().toString());
//        assertEquals("xref xri path", "/2/3", xref.getXRIPath().toString());
    }

    @Test
    public void testCR20006216(
    ){
        assertEquals(paths[8], "(+resource/application-preferences.xml)", new Path(paths[8]).getComponent(4).get(0));
        PathComponent stateId = new PathComponent("sins:0:");
        assertEquals("Object Id", "sins", stateId.get(0));
        assertEquals("State Number", "0", stateId.get(1));
        assertFalse("PlaceHolder", stateId.isPlaceHolder());
        assertTrue("Private", stateId.isPrivate());        
    }
    
}
