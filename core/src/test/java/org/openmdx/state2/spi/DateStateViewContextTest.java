/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Date State Context Test
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
package org.openmdx.state2.spi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.state2.cci.DateStateContext;
import org.w3c.time.ChronoUtils;

/**
 * Date State Context Test
 */
public class DateStateViewContextTest {
    
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
            ChronoUtils.createDate(2000, 4, 1),
            ChronoUtils.createDate(2000, 4, 30)
        );
        DateStateContext c2 = DateStateViewContext.newTimeRangeViewContext(
            ChronoUtils.createDate(2000, 4, 1),
            ChronoUtils.createDate(2000, 4, 30)
       );
       DateStateContext c3 = DateStateViewContext.newTimeRangeViewContext(null,null);
       Assertions.assertNotSame(c1,  c2, "identitiy"); 
       Assertions.assertEquals(c1,  c2, "equality"); 
       Assertions.assertEquals(c1.hashCode(),  c2.hashCode(), "hashCode"); 
       DateStateContext c4 = DateStateViewContext.newTimeRangeViewContext(
           null,
           ChronoUtils.createDate(2000, 4, 30)
       );
       DateStateContext c5 = DateStateViewContext.newTimeRangeViewContext(
               ChronoUtils.createDate(2000, 4, 1),
           null
       );
       Assertions.assertEquals(C1,  c1.toString(), "toString"); 
       Assertions.assertEquals(C2,  c2.toString(), "toString"); 
       Assertions.assertEquals(C3,  c3.toString(), "toString");        
       Assertions.assertEquals(C4,  c4.toString(), "toString"); 
       Assertions.assertEquals(C5,  c5.toString(), "toString");        
    }
    
    @Test  
    public void noDate(
    ){
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif null1 =
                #if CLASSIC_CHRONO_TYPES org.w3c.spi.DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar()
				#else java.time.LocalDate.now()
				#endif;
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif null2 =
                #if CLASSIC_CHRONO_TYPES org.w3c.spi.DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar()
				#else java.time.LocalDate.now()
				#endif;
        Assertions.assertEquals(null1,  null2, "NULL");
    }
    
}
