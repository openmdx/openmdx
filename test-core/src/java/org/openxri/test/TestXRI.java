/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestXRI.java,v 1.12 2007/05/28 20:52:33 hburger Exp $
 * Description: TestXRI 
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/05/28 20:52:33 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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

package org.openxri.test;

//import java.net.URI;
//import java.net.URISyntaxException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.openxri.XRISubSegment;
import org.openxri.XRI;
import org.openxri.XRIParseException;
import org.openxri.XRITest;
import org.openxri.XRef;

/**
 * TestXRI
 *
 */
public class TestXRI
    extends XRITest
{

    /**
     * Pass control to the non-graphical test runner
     */
    public static void main(
        String[] args
    ) {
        TestRunner.run(suite());
    }

    private static final String[] xris = new String[]{
        "xri://@example/asterisk*%2A",
        "xri://@a*a/!b!b/c*c/(xri://@d*d/e)?q",
        "xri://@a*a/!b!b/c*c/(xri://@d*d/e)?y",
        "xri://@a*a/!b!b/c*c/(xri://@d*d/e)?q#s",
        "xri://@a*a/!b!b/c*c/(xri://@d*d/e)?q",
        "xri://(mailto:john.doe@example.com)/favorites/home",
        "xri://@example/(mailto:john.doe@example.com)/favorites/home",
        "xri://@example/(xri://@example2/abc)",
        "xri://@openmdx*A.A0/Fran\u00e7ois", // "xri://@openmdx*A.B/François"
        "xri://@openmdx*A.A0/(@openmdx*B.B0/B1)/C",
        "xri://@openmdx*A.A0/(xri://@openmdx*B.B0/B1)/C",
        "xri://(+foo!boo*a)",
        "xri://@example/!b!b/c*c/(xri://@d*d/e)?q#s",        
        "xri://@example/!b!b/c*c/(xri://@d*d/e)",
        "xri://@example/(http://www.openxri.org)", // SF1181030
        "xri://@example/(http://example.com)/john.doe", // SF1181030
        "xri://@example/(urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6)", // SF1181030
        "xri://@example/!(urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6)", // SF1181030
        "xri://@example/(xri://@example2/abc?id=1)", // SF1505889 
        "xri://@example/(xri://@example2/abc#top)", // SF1505889 
        "xri://@example/(http://example.com#bottom)/john.doe", // SF1181030 & SF1505889
        "xri://(http://example.com)/john.doe" // SF1657924 
    };
    
    private static final String[] iris = new String[]{
        "xri://@example/asterisk*%252A",
        "xri://@a*a/!b!b/c*c/(xri:%2F%2F@d*d%2Fe)?q",
        "xri://@a*a/!b!b/c*c/(xri:%2F%2F@d*d%2Fe)?y", 
        "xri://@a*a/!b!b/c*c/(xri:%2F%2F@d*d%2Fe)?q#s",
        "xri://@a*a/!b!b/c*c/(xri:%2F%2F@d*d%2Fe)?q",
        "xri://(mailto:john.doe@example.com)/favorites/home",
        "xri://@example/(mailto:john.doe@example.com)/favorites/home",
        "xri://@example/(xri:%2F%2F@example2%2Fabc)",
        "xri://@openmdx*A.A0/Fran\u00e7ois", // "xri:@openmdx:A.B/François"
        "xri://@openmdx*A.A0/(@openmdx*B.B0%2FB1)/C",
        "xri://@openmdx*A.A0/(xri:%2F%2F@openmdx*B.B0%2FB1)/C",
        "xri://(+foo!boo*a)",
        "xri://@example/!b!b/c*c/(xri:%2F%2F@d*d%2Fe)?q#s",
        "xri://@example/!b!b/c*c/(xri:%2F%2F@d*d%2Fe)",
        "xri://@example/(http:%2F%2Fwww.openxri.org)",
        "xri://@example/(http:%2F%2Fexample.com)/john.doe",
        "xri://@example/(urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6)",
        "xri://@example/!(urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6)",
        "xri://@example/(xri:%2F%2F@example2%2Fabc%3Fid=1)",
        "xri://@example/(xri:%2F%2F@example2%2Fabc%23top)",
        "xri://@example/(http:%2F%2Fexample.com%23bottom)/john.doe",
        "xri://(http:%2F%2Fexample.com)/john.doe"
    };

    private static final boolean SF1181030resolved = true;
    private static final int SF1181030first = 13;
    private static final int SF1181030last = 17;
    
    private static final boolean SF1505889resolved = true;
    private static final int SF1505889first = 18;
    private static final int SF1505889last = 20;
    
    private static final String[] invalidSubSegments = new String[]{
        "a/b",
        "a(b",
        "a)b",
        "(ab",
        "ab)"
    };
    
    /**
     * 
     */
    public static Test suite(
    ){
        return new TestSuite(TestXRI.class);
    }

    /**
     * 
     */
    public void testObjectId(
    ) throws XRIParseException {
        for(
            int i = 0;
            i < xris.length;
            i++
        ) if(
            (SF1181030resolved || i < SF1181030first || i > SF1181030last) &&
            (SF1505889resolved || i < SF1505889first || i > SF1505889last)
        ) {
            XRI xri = new XRI(xris[i]);
            assertEquals("xris[" + i + "].toString(): " + xris[i], xris[i], xri.toString());
            assertEquals("xris[" + i + "].toIRINormalForm(): " + xris[i], iris[i], xri.toIRINormalForm());
        }
    }

    /**
     * 
     */
    public void testValidCrossReference(
    ){
        int count = 0;
        for(
           int i = 0;
           i < xris.length;
           i++
        ){
            if(
                xris[i].indexOf('?') < 0 &&
                xris[i].indexOf('#') < 0
            ) {
                String crossReference = '(' + xris[i] + ')';
                new XRef(crossReference);
                count++;
            }
        }
        System.out.println("Sucessfully tested " + count + " testable cross references");
    }

    public void testInvalidSubSegment(){
        for(
            int i = 0;
            i < invalidSubSegments.length;
            i++
        ){
            try {
                new XRISubSegment(invalidSubSegments[i]);
                fail("Not a valid SubSegment: " + invalidSubSegments[i]);
            } catch (XRIParseException exception) {
                // Expected
            }
        }
    }
    
//  public void testURN() throws URISyntaxException{
//      URI urn = new URI("urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6");        
//      System.out.println("opaque: " + urn.isOpaque());
//      System.out.println("authority: " + urn.getAuthority());
//      urn = new URI("urn", "uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6", null, null, null);        
//      System.out.println("opaque: " + urn.isOpaque());
//      System.out.println("authority: " + urn.getAuthority());
//  }

}
