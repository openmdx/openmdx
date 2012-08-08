/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestContext.java,v 1.1 2008/11/14 10:26:15 hburger Exp $
 * Description: Test Aspects 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/14 10:26:15 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package test.openmdx.state2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.spi.DateStateViewContext;
import org.w3c.spi.DatatypeFactories;

/**
 * TestAspects
 */
public class TestContext {
    
    /**
     * [20000401,20000430]
     */
    final static protected String C1 = "[20000401,20000430]";
    
    /**
     * [20000401,20000430]
     */
    final static protected String C2 = "[20000401,20000430]";
    
    /**
     * (-&#x221e;,&#x221e;)
     */
    final static protected String C3 = "(-\u221E,\u221E)";
    
    /**
     * (-&#x221e;,20000430]
     */
    final static protected String C4 = "(-\u221E,20000430]";
    
    /**
     * [20000401,&#x221e;)
     */
    final static protected String C5 = "[20000401,\u221E)";
    
    @Test  
    public void dateStateContext(
    ){
        DateStateContext c1 = DateStateViewContext.newTimeRangeViewContext(
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
                2000,
                4,
                1,
                DatatypeConstants.FIELD_UNDEFINED
            ),
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
                2000,
                4,
                30,
                DatatypeConstants.FIELD_UNDEFINED
            )
        );
        DateStateContext c2 = DateStateViewContext.newTimeRangeViewContext(
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
                2000,
                4,
                1,
                DatatypeConstants.FIELD_UNDEFINED
            ),
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
                2000,
                4,
                30,
                DatatypeConstants.FIELD_UNDEFINED
            )
       );
       DateStateContext c3 = DateStateViewContext.newTimeRangeViewContext(null,null);
       assertNotSame("identitiy", c1, c2); 
       assertEquals("equality", c1, c2); 
       assertEquals("hashCode", c1.hashCode(), c2.hashCode()); 
       DateStateContext c4 = DateStateViewContext.newTimeRangeViewContext(
           null,
           DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
               2000,
               4,
               30,
               DatatypeConstants.FIELD_UNDEFINED
           )
       );
       DateStateContext c5 = DateStateViewContext.newTimeRangeViewContext(
           DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
               2000,
               4,
               1,
               DatatypeConstants.FIELD_UNDEFINED
           ),
           null
       );
       assertEquals("toString", C1, c1.toString()); 
       assertEquals("toString", C2, c2.toString()); 
       assertEquals("toString", C3, c3.toString());        
       assertEquals("toString", C4, c4.toString()); 
       assertEquals("toString", C5, c5.toString());        
    }
    
    @Test  
    public void noDate(
    ){
        XMLGregorianCalendar null1 = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar();
        XMLGregorianCalendar null2= DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar();
        assertEquals("NULL", null1, null2);
    }
    
}
