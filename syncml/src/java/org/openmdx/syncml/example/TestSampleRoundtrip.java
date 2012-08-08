/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: TestSampleRoundtrip.java,v 1.8 2007/04/02 00:56:04 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/04/02 00:56:04 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of OMEX AG nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
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
 * 
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */
package org.openmdx.syncml.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.openmdx.syncml.engine.SyncOptions;
import org.openmdx.syncml.xlt.SmlEncoding_t;

import junit.framework.TestCase;

public class TestSampleRoundtrip extends TestCase {

    //-----------------------------------------------------------------------
    protected void setUp(
    ) throws Exception {
    }
    
    //-----------------------------------------------------------------------
    protected void tearDown() throws Exception {
    }
    
    //-----------------------------------------------------------------------
    private byte[] toByteArray(
        InputStream is
    ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b = 0;
        while((b = is.read()) >= 0) {
            baos.write(b);
        }
        return baos.toByteArray();
    }
    
    //-----------------------------------------------------------------------
    public void testRoundtrip(
        byte[] request
    ) throws Exception {
        
        System.out.println("Sending request");
        System.out.println(request);
        
        // Get SyncML response
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        SampleProvider.process(
            request, 
            response
        );
        response.close();
        System.err.println();
        System.err.println("Received response");
        System.err.println(response.toString());        
        
    }

    //-----------------------------------------------------------------------
    public void testRoundtrip0(
    ) throws Exception {
        ByteArrayOutputStream request = new ByteArrayOutputStream();        
        SyncOptions options = new SyncOptions();
        options.encoding = SmlEncoding_t.SML_XML;           
        SampleClient.sendMessage0(
            options,
            request
        );
        request.close();
        this.testRoundtrip(
            request.toByteArray()
        );
    }
       
    //-----------------------------------------------------------------------
    public void testRoundtrip1(
    ) throws Exception {
        InputStream is = new URL("file:C:/pj/omex/openmdx/syncml/src/resource/org/openmdx/syncml/example/message1.xml").openStream();
        this.testRoundtrip(
            this.toByteArray(is)
        );
    }
    
    //-----------------------------------------------------------------------
    public void testRoundtrip2(
    ) throws Exception {
        InputStream is = new URL("file:C:/pj/omex/openmdx/syncml/src/resource/org/openmdx/syncml/example/message2.xml").openStream();
        this.testRoundtrip(
            this.toByteArray(is)
        );
    }
    
    //-----------------------------------------------------------------------
    public void testRoundtrip3(
    ) throws Exception {
        InputStream is = new URL("file:C:/pj/omex/openmdx/syncml/src/resource/org/openmdx/syncml/example/message3.xml").openStream();
        this.testRoundtrip(
            this.toByteArray(is)
        );
    }
    
    //-----------------------------------------------------------------------
    public void testRoundtrip4(
    ) throws Exception {
        InputStream is = new URL("file:C:/pj/omex/openmdx/syncml/src/resource/org/openmdx/syncml/example/message4.xml").openStream();
        this.testRoundtrip(
            this.toByteArray(is)
        );
    }
    
    //-----------------------------------------------------------------------
    public void testRoundtrip5(
    ) throws Exception {
        InputStream is = new URL("file:C:/pj/omex/openmdx/syncml/src/resource/org/openmdx/syncml/example/message5.xml").openStream();
        this.testRoundtrip(
            this.toByteArray(is)
        );
    }
    
    //-----------------------------------------------------------------------
    public void testRoundtrip6(
    ) throws Exception {
        InputStream is = new URL("file:C:/pj/omex/openmdx/syncml/src/resource/org/openmdx/syncml/example/message6.xml").openStream();
        this.testRoundtrip(
            this.toByteArray(is)
        );
    }
    
    //-----------------------------------------------------------------------
    public void testRoundtrip7(
    ) throws Exception {
        InputStream is = new URL("file:C:/pj/omex/openmdx/syncml/src/resource/org/openmdx/syncml/example/message7.xml").openStream();
        this.testRoundtrip(
            this.toByteArray(is)
        );
    }
    
    //-----------------------------------------------------------------------
    public void testRoundtrip8(
    ) throws Exception {
        InputStream is = new URL("file:C:/pj/omex/openmdx/syncml/src/resource/org/openmdx/syncml/example/message8.xml").openStream();
        this.testRoundtrip(
            this.toByteArray(is)
        );
    }
    
    //-----------------------------------------------------------------------
    public void testRoundtrip9(
    ) throws Exception {
        InputStream is = new URL("file:C:/pj/omex/openmdx/syncml/src/resource/org/openmdx/syncml/example/message9.xml").openStream();
        this.testRoundtrip(
            this.toByteArray(is)
        );
    }
        
    //-----------------------------------------------------------------------
    public void testRoundtrip10(
    ) throws Exception {
        InputStream is = new URL("file:C:/pj/omex/openmdx/syncml/src/resource/org/openmdx/syncml/example/message10.xml").openStream();
        this.testRoundtrip(
            this.toByteArray(is)
        );
    }
    
    //-----------------------------------------------------------------------
    public void testRoundtrip11(
    ) throws Exception {
        InputStream is = new URL("file:C:/pj/omex/openmdx/syncml/src/resource/org/openmdx/syncml/example/message11.xml").openStream();
        this.testRoundtrip(
            this.toByteArray(is)
        );
    }
    
}
