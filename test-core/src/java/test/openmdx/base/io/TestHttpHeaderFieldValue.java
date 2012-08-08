/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestHttpHeaderFieldValue.java,v 1.2 2010/02/27 06:52:33 hburger Exp $
 * Description: Test HTTP Headers
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/02/27 06:52:33 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package test.openmdx.base.io;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

import org.junit.Test;
import org.openmdx.base.io.HttpHeaderFieldContent;
import org.openmdx.base.io.HttpHeaderFieldValue;

/**
 * Test HTTP Headers
 */
public class TestHttpHeaderFieldValue {

    /**
     * Test "Accept" header
     */
    @Test
    public void testAccept(){
        HttpHeaderFieldValue fieldValue = newHeaderFieldValue(
            "text/plain; q=\"0.5\", text/html, text/x-dvi; q=0.8, text/x-c"
        );
        assertEquals(
            "text/html",
            fieldValue.getPreferredContent("text/xml").getValue()
        );
        Iterator<String> expected = Arrays.asList(
            "text/html", 
            "text/x-c", 
            "text/x-dvi", 
            "text/plain"
        ).iterator();
        for(HttpHeaderFieldContent entry : fieldValue) {
            assertEquals(expected.next(), entry.getValue());
        }
    }

    /**
     * Test "Content-Type" header
     */
    @Test
    public void testContentType(){
        HttpHeaderFieldValue fieldValue = newHeaderFieldValue(
            "text/xml;charset=\"utf-8\""
        );
        HttpHeaderFieldContent fieldContent = fieldValue.getPreferredContent("text/plain;charset=ISO-8859-1"); 
        assertEquals(
            "text/xml",
            fieldContent.getValue()
        );
        assertEquals(
            "utf-8",
            fieldContent.getParameterValue("charset",null)
        );
    }
    
    /**
     * Headers factory
     * 
     * @param commaSeparatedList
     * 
     * @return a new HttpHeaders instance
     */
    private static final HttpHeaderFieldValue newHeaderFieldValue(
        String commaSeparatedList
    ){
        final String[] headers = commaSeparatedList.split(",");
        return new HttpHeaderFieldValue(
            new Enumeration<String>(){

                private int cursor = 0;
                
                @Override
                public boolean hasMoreElements() {
                    return this.cursor < headers.length;
                }

                @Override
                public String nextElement() {
                    return headers[this.cursor++].trim();
                }
                
            }
        );
    }

}
