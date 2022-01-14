/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Date 
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
package org.w3c.jpa3;

import java.text.ParseException;

import org.w3c.cci2.ImmutableDate;
import org.w3c.format.DateTimeFormat;

/**
 * Date
 */
public class Date {

    /**
     * Avoid instantiation 
     */
    protected Date() {
    }

    /**
     * Convert an XML Gregorian Calendar to an SQL date
     * 
     * @param cciDate the XML Gregorian Calendar to be converted
     * 
     * @return a corresponding SQL date instance
     */
    public static final java.sql.Date toJDO (
        javax.xml.datatype.XMLGregorianCalendar cciDate
    ){
        return cciDate == null ? null : java.sql.Date.valueOf(cciDate.toXMLFormat());
    }

    /**
     * Convert an SQL date to an XML Gregorian Calendar
     * 
     * @param jdoDate the SQL date to be converted
     * 
     * @return a corresponding XML Gregorian Calendar instance
     */
    public static final javax.xml.datatype.XMLGregorianCalendar toCCI (
        java.sql.Date jdoDate
    ){
        String value = jdoDate == null ?
            null :
                jdoDate.toString();
        if(value == null) {
            return null;
        } else try {
            value = DateTimeFormat.completeCentury(value);
        } catch (ParseException exception) {
            throw new IllegalArgumentException(exception);
        }
        if(DateTimeFormat.BASIC_DATE_PATTERN.matcher(value).matches()) {
            return new ImmutableDate(value);
        } else if(DateTimeFormat.EXTENDED_DATE_PATTERN.matcher(value).matches()) {
            return new ImmutableDate(value.replaceAll("-", ""));
        } else {
            throw new IllegalArgumentException(
                "The value does not match the org::w3c::date pattern. Pattern=YYYY[...]-MM-DD. Value=" + value
            );
        }
    }

}
