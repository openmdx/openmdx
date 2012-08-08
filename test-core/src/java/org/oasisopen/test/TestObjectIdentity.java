/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestObjectIdentity.java,v 1.10 2007/05/28 20:57:02 hburger Exp $
 * Description: Test Object Identity
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/05/28 20:57:02 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other
 * organizations as lusted in the NOTICE file.
 */
package org.oasisopen.test;

import java.net.URI;
//import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openxri.AuthorityPath;
import org.openxri.XRISubSegment;
import org.openxri.XRI;
import org.openxri.XRIPath;
import org.openxri.XRISegment;
import org.openxri.XRef;


/**
 * Test Object Identity
 */
public class TestObjectIdentity
    extends TestCase
{

    /**
     * Constructs a test case with the given name.
     */
    public TestObjectIdentity(String name) {
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
        return new TestSuite(TestObjectIdentity.class);
    }

    /**
     * Test XRI
     * 
     * @throws Exception
     */
    public void testXRI() throws Exception {
        String xri = "xri://@openmdx*org.openmdx.preferences1/provider/Java*Properties/segment/(java:comp/env)";
        XRI identity = new XRI(xri);
        AuthorityPath authorityPath = identity.getAuthorityPath();
        assertEquals("authority", "@openmdx*org.openmdx.preferences1", authorityPath.toString());
        XRIPath xriPath = identity.getXRIPath();
        assertEquals("xriPath", "/provider/Java*Properties/segment/(java:comp/env)", xriPath.toString());
        assertEquals("xriPath[]", 4, xriPath.getNumSegments());
        XRISegment xriSegment = xriPath.getSegmentAt(0);
        assertEquals("xriPath[0]", "*provider", xriSegment.toString());
        assertEquals("xriPath[0][]", 1, xriSegment.getNumSubSegments());
        xriSegment = xriPath.getSegmentAt(1);
        assertEquals("xriPath segment[1]", "*Java*Properties", xriSegment.toString());
        assertEquals("xriPath subSegments[1]", 2, xriSegment.getNumSubSegments());
        XRISubSegment subSegment = xriSegment.getSubSegmentAt(0);
        assertEquals("xriPath[1][0]", "*Java", subSegment.toString());
        assertFalse("Persistency", subSegment.isPersistant());
        subSegment = xriSegment.getSubSegmentAt(1);
        XRef xRef = subSegment.getXRef();
        assertNull("No cross-reference", xRef);
        assertEquals("xriPath[1][1]", "*Properties", subSegment.toString());
        xriSegment = xriPath.getSegmentAt(3);
        assertEquals("xriPath[3]", "*(java:comp/env)", xriSegment.toString());
        assertEquals("xriPath[3][]", 1, xriSegment.getNumSubSegments());
        subSegment = xriSegment.getSubSegmentAt(0);
        assertEquals("xriPath[3][0]", "*(java:comp/env)", subSegment.toString());
        xRef = subSegment.getXRef();
        assertEquals ("XRef", "(java:comp/env)", xRef.toString());
        assertNull("xriFragment",  identity.getFragment());
        assertNull("xriQuery",  identity.getQuery());
        assertEquals("xri", xri, identity.toString());
    }

    /**
     * Test IRI
     * 
     * @throws Exception
     */
    public void testIRI() throws Exception {
        URI iri = new URI ("A(B%456");
        assertEquals("blanks", "A(B%456", iri.toString());
    }

//    public void testJDOSegments(){
//    	//
//    	// String
//    	//
//    	JDOIdentityBuilder identityBuilder = new JDOIdentityBuilder();    	
//    	identityBuilder.appendString(true);
//    	assertEquals("Final String", "!b6b75e20-3048-11da-ae89-0002a5d5c51b", identityBuilder.toString());
//		//    	
//    	identityBuilder = new JDOIdentityBuilder();    	
//    	identityBuilder.appendString(false);
//    	assertEquals("Transient String", "($t*uuid*b6b75e20-3048-11da-ae89-0002a5d5c51b)", identityBuilder.toString());
//    	//
//    	// Number
//    	//
//    	identityBuilder = new JDOIdentityBuilder();    	
//    	identityBuilder.appendNumber(true);
//    	assertEquals("Final Number", "!242871594654712913482626436732097512731", identityBuilder.toString());
//    	//
//    	identityBuilder = new JDOIdentityBuilder();    	
//    	identityBuilder.appendNumber(false);
//    	assertEquals("Transient Number", "-242871594654712913482626436732097512731", identityBuilder.toString());
//    	//
//    	// IRI
//    	//
//    	identityBuilder = new JDOIdentityBuilder();    	
//    	identityBuilder.appendIRI(true);
//    	assertEquals("Final IRI", "!(urn:uuid:b6b75e20-3048-11da-ae89-0002a5d5c51b)", identityBuilder.toString());
//    	//
//    	identityBuilder = new JDOIdentityBuilder();    	
//    	identityBuilder.appendIRI(false);
//    	assertEquals("Transient IRI", "($t*uuid*b6b75e20-3048-11da-ae89-0002a5d5c51b)", identityBuilder.toString());
//    	//
//    	// OID
//    	//
//    	identityBuilder = new JDOIdentityBuilder();    	
//    	identityBuilder.appendOID(true);
//    	assertEquals("Final OID", "!2.25.242871594654712913482626436732097512731", identityBuilder.toString());
//    	//
//    	identityBuilder = new JDOIdentityBuilder();    	
//    	identityBuilder.appendOID(false);
//    	assertEquals("Transient OID", "2.25.242871594654712913482626436732097512731", identityBuilder.toString());
//    	//
//    	// UUID
//    	//
//    	identityBuilder = new JDOIdentityBuilder();    	
//    	identityBuilder.appendUUID(true);
//    	assertEquals("Final UUID", "!trdeIDBIEdquiQACpdXFGw", identityBuilder.toString());
//    	//
//    	identityBuilder = new JDOIdentityBuilder();    	
//    	identityBuilder.appendUUID(false);
//    	assertEquals("Transient UUID", "trdeIDBIEdquiQACpdXFGw", identityBuilder.toString());
//
//    }
//
//    public void testXRISegments(){
//    	//
//    	// String
//    	//
//    	XRIIdentityBuilder identityBuilder = new XRIIdentityBuilder();    	
//    	identityBuilder.appendString(true);
//    	assertEquals("Final String", "!b6b75e20-3048-11da-ae89-0002a5d5c51b", identityBuilder.toString());
//		//    	
//    	identityBuilder = new XRIIdentityBuilder();    	
//    	identityBuilder.appendString(false);
//    	assertEquals("Transient String", "($t*uuid*b6b75e20-3048-11da-ae89-0002a5d5c51b)", identityBuilder.toString());
//    	//
//    	// Number
//    	//
//    	identityBuilder = new XRIIdentityBuilder();    	
//    	identityBuilder.appendNumber(true);
//    	assertEquals("Final Number", "!242871594654712913482626436732097512731", identityBuilder.toString());
//    	//
//    	identityBuilder = new XRIIdentityBuilder();    	
//    	identityBuilder.appendNumber(false);
//    	assertEquals("Transient Number", "-242871594654712913482626436732097512731", identityBuilder.toString());
//    	//
//    	// IRI
//    	//
//    	identityBuilder = new XRIIdentityBuilder();    	
//    	identityBuilder.appendIRI(true);
//    	assertEquals("Final IRI", "!(urn:uuid:b6b75e20-3048-11da-ae89-0002a5d5c51b)", identityBuilder.toString());
//    	//
//    	identityBuilder = new XRIIdentityBuilder();    	
//    	identityBuilder.appendIRI(false);
//    	assertEquals("Transient IRI", "($t*uuid*b6b75e20-3048-11da-ae89-0002a5d5c51b)", identityBuilder.toString());
//    	//
//    	// OID
//    	//
//    	identityBuilder = new XRIIdentityBuilder();    	
//    	identityBuilder.appendOID(true);
//    	assertEquals("Final OID", "!(urn:oid:2.25.242871594654712913482626436732097512731)", identityBuilder.toString());
//    	//
//    	identityBuilder = new XRIIdentityBuilder();    	
//    	identityBuilder.appendOID(false);
//    	assertEquals("Transient OID", "($t*oid*2.25.242871594654712913482626436732097512731)", identityBuilder.toString());
//    	//
//    	// UUID
//    	//
//    	identityBuilder = new XRIIdentityBuilder();    	
//    	identityBuilder.appendUUID(true);
//    	assertEquals("Final UUID", "!(urn:uuid:b6b75e20-3048-11da-ae89-0002a5d5c51b)", identityBuilder.toString());
//    	//
//    	identityBuilder = new XRIIdentityBuilder();    	
//    	identityBuilder.appendUUID(false);
//    	assertEquals("Transient UUID", "($t*uuid*b6b75e20-3048-11da-ae89-0002a5d5c51b)", identityBuilder.toString());
//
//    }
//
//    /**
//     * JDOIdentityBuilder using a gine UUID
//     */
//    static class JDOIdentityBuilder extends org.oasisopen.jdo2.JDOSegmentBuilder {
//
//		/* (non-Javadoc)
//		 * @see org.oasisopen.jdo2.AbstractIdentityBuilder#newUUID()
//		 */
//		@Override
//		protected UUID newUUID() {
//			return BLACK;
//		}
//		
//		private static UUID BLACK = UUID.fromString("b6b75e20-3048-11da-ae89-0002a5d5c51b");
//    	
//    }
//    
//    /**
//     * JDOIdentityBuilder using a gine UUID
//     */
//    static class XRIIdentityBuilder extends org.oasisopen.jdo2.XRISegmentBuilder {
//
//		/* (non-Javadoc)
//		 * @see org.oasisopen.jdo2.AbstractIdentityBuilder#newUUID()
//		 */
//		@Override
//		protected UUID newUUID() {
//			return BLACK;
//		}
//		
//		private static UUID BLACK = UUID.fromString("b6b75e20-3048-11da-ae89-0002a5d5c51b");
//    	
//    }

}
