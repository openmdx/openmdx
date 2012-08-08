/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DatatypeFormat.java,v 1.9 2009/01/04 21:16:13 wfro Exp $
 * Description: DatatypeFormat 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/04 21:16:13 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
package org.openmdx.base.text.format;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.spi.DatatypeFactories;

/**
 * DatatypeFormat<ul>
 * <li>Marshals <code>String</code> values in ISO 8601 basic or extended 
 *     format to XML datatype instances and leaves non-<code>String</code>
 *     objects as they are.<br>
 *     <em>Do not provide any other <code>String</code> object as argument for
 *     the <code>marshal()</code> method!</em>
 * <li>Unmarshals <code>Duration</code> and <code>XMLGregorianCalendar</code>
 *     instances to their basic or extended ISO 8601 compliant
 *     <code>String</code> representation and leaves instances of all other 
 *     classes as they are.<br>
 * <em>The resolution is restricted to Milliseconds if <code>basicFormat</code> is
 *     <code>true</code>.</em>
 * </ul>
 * <p>
 */
public class DatatypeFormat implements Marshaller {

    /**
     * Constructor 
     * @param basicFormat The <code>marshal()</code> and <code>unmarshal()</code>
     * methods use <em>ISO 8601 basic format</em> if <code>basicFormat</code> is 
     * <code>true</code> and <em>ISO 8601 extended format</em> if 
     * <code>basicFormat</code> is <code>false</code>.
     * 
     * @throws DatatypeConfigurationException if the <code>DatatypeFactory</code>
     * can't be acquired
     */
    private DatatypeFormat(
        boolean basicFormat
    ) throws DatatypeConfigurationException {
        this.basicFormat = basicFormat;
    }

    /**
     * Factory method
     * 
     * @param basicFormat use ISO 8601 basic format if <code>true</code>;
     * ISO 8601 extended format otherwise.
     * 
     * @return a new DatatypeFormat instance
     * 
     * @throws ServiceException if the <code>DatatypeFactory</code>
     * can't be acquired
     */
    public static DatatypeFormat newInstance(
        boolean basicFormat
    ) throws ServiceException{
        try {
            return new DatatypeFormat(basicFormat);
        } catch (DatatypeConfigurationException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "DatatypeFactory acquisition failed"
            );
        }
    }

    /**
     * Use ISO 8601 basic format if <code>true</code>;
     * ISO 8601 extended format otherwise.
     */
    private final boolean basicFormat;

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        if(source instanceof Duration) {
            return source.toString(); 
        } else if (source instanceof XMLGregorianCalendar){
            String value = source.toString();
            if(basicFormat) {
                value = value.replaceAll("[\\-:]","");
                if(value.endsWith("Z")) { 
                    int t = value.indexOf('T');
                    if(t > 0) {
                        if(value.length() - t > 12) {
                            value = value.substring(0, t + 11) + 'Z';
                        } else if (value.length() - t < 12) {
                            value = value.substring(0, value.length() - 1);
                            char c = value.indexOf('.') < 0 ? '.' : '0';
                            while (value.length() - t < 11) {
                                value += c;
                                c = '0';
                            }
                            return value + 'Z';
                        }
                    }
                }
            }
            return value;
        } else {
            return source;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#marshal(java.lang.Object)
     */
    public Object marshal(
        Object source
    ) throws ServiceException {
        if(source instanceof String) {
            String value = (String)source;
            return 
            value.startsWith("P") || value.startsWith("-P") ? parseDuration(value) :
                value.indexOf('T') < 0 ? parseDate(value) :
                    (Object) parseDateTime(value);
        } else {
            return source;
        }
    }

    /**
     * Parse a duration <code>String</code>
     * 
     * @param duration a duration as <code>String</code>
     * 
     * @return a corrsponding <code>Duration</code> instance
     */
    public Duration parseDuration(
        String duration
    ){
        return DatatypeFactories.xmlDatatypeFactory().newDuration(duration);
    }

    /**
     * Parse a date <code>String</code>
     * 
     * @param date a date in the format yyyymmdd 
     * 
     * @return a corresponding <code>XMLGregorianCalendar</code> instance
     */
    public XMLGregorianCalendar parseDate(
        String date
    ){
        int t = date.length();
        return DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(
            this.basicFormat ? new StringBuilder(
                date
            ).insert(
                t - 2, 
                '-'
            ).insert(
                t - 4, 
                '-'
            ).toString(
            ) : date
        );
    }

    /**
     * Parse a date-time <code>String</code>
     * 
     * @param dateTime a date in the format yyyymmddThhmmss.mmmZ 
     * 
     * @return a corresponding <code>XMLGregorianCalendar</code> instance
     */
    public XMLGregorianCalendar parseDateTime(
        String dateTime
    ){
        int t = dateTime.indexOf('T');
        return t < 0 ? parseDate(
            dateTime
        ) : DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(
            this.basicFormat ? new StringBuilder(
                dateTime
            ).insert(
                t + 5,
                ':'
            ).insert(
                t + 3,
                ':'
            ).insert(
                t - 2, 
                '-'
            ).insert(
                t - 4, 
                '-'
            ).toString(
            ) : dateTime
        );
    }

}
