/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Date Time Values 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2018, OMEX AG, Switzerland
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

package org.openmdx.base.dataprovider.layer.persistence.jdbc;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.rest.cci.ObjectRecord;
import org.w3c.time.TimeZones;

/**
 * Date Time Values
 */
class DateTimeValues {

    /**
     * Replace XMLGregorianCalendar values of type <code>DATETIME</code>
     * by <code>Date</code> values.
     * 
     * @param object the <code>ObjectRecord</code>
     */
    @SuppressWarnings("unchecked")
    protected static final void normalizeDateTimeValues(
        ObjectRecord object
    ) {
        for(Map.Entry<?,Object> entry : ((Map<?,Object>)object.getValue()).entrySet()) {
            final Object value = entry.getValue();
            if(value instanceof List<?>) {
                for(ListIterator<Object> i = ((List<Object>) value).listIterator(); i.hasNext();) {
                    final Object v = i.next();
                    final Object n = normalizeDateTimeValue(v);
                    if(n != v) {
                        i.set(n);
                    }
                }
            } else if (value instanceof Map<?,?>) {
                for(Map.Entry<?,Object> e : ((Map<?,Object>)value).entrySet()) {
                    final Object v = e.getValue();
                    final Object n = normalizeDateTimeValue(v);
                    if(n != v) {
                        e.setValue(n);
                    }
                }
            } else {
                final Object n = normalizeDateTimeValue(value);
                if(n != value) {
                    entry.setValue(n);
                }
            }
        }
    }

    /**
     * Replace XMLGregorianCalendar values of type <code>DATETIME</code>
     * by their corresponding <code>Date</code> values.
     * 
     * @param value
     * @return the corresponding <code>Date</code> value in case of an
     * XMLGregorianCalendar value of type <code>DATETIME</code>, the 
     * original value otherwise
     */
    private static final Object normalizeDateTimeValue(Object value) {
        if (value instanceof XMLGregorianCalendar){
            final XMLGregorianCalendar datatypeValue = (XMLGregorianCalendar) value;
            if (DatatypeConstants.DATETIME.equals(datatypeValue.getXMLSchemaType())) {
                return toDate(datatypeValue);
            }
        }
        return value;
    }

    private static Date toDate(XMLGregorianCalendar datatypeValue) {
        final TimeZone timeZone = TimeZones.toTimeZone(datatypeValue.getTimezone());
        return datatypeValue.toGregorianCalendar(timeZone, null, null).getTime(); 
    }
    
}
