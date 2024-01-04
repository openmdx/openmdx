/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Date Marshaller
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
package org.openmdx.base.accessor.spi;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.marshalling.TypeSafeMarshaller;

/**
 * Date Marshaller
 */
public class DateMarshaller {

    /**
     * Constructor 
     */
    private DateMarshaller(
    ) {
        // Avoid instantiation
    }

    /**
     * {@code Datatype} Instance <-> ISO 8601 Basic {@code String}
     */
    public static final TypeSafeMarshaller<String,XMLGregorianCalendar> BASIC_FORMAT_TO_DATATYPE = new DatatypeMarshaller<XMLGregorianCalendar>(
        XMLGregorianCalendar.class
    ){

        @Override
        protected String toBasicFormat(XMLGregorianCalendar datatype) {
            return datatype.toXMLFormat().replaceAll("-", "");
        }
        
    };
    
    /**
     * Normalizing Marshaller
     */
    public static final Marshaller NORMALIZE = new NormalizingMarshaller(
        XMLGregorianCalendar.class
    ){

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.spi.NormalizingMarshaller#normalize(java.lang.Object)
         */
        @Override
        protected Object normalize(
            Object source
        ) throws ServiceException {
            if(source instanceof String) {
                String value = (String) source;
                int limit = value.indexOf('T');
                if(limit > 0) {
                    return super.normalize(value.substring(0, limit));
                }
            }
            return super.normalize(source);
        }
    
    };
    
}
