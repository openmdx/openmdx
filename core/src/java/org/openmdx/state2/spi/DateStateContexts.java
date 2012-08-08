/*
 * ====================================================================
 * Description: Date State Contexts
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/27 16:46:56 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.state2.spi;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.resource.cci.InteractionSpec;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.spi.DatatypeFactories;

/**
 * Date State Contexts
 */
public class DateStateContexts {

    protected DateStateContexts(
    ) {
        // Avoid instantiation 
    }

    protected static InteractionSpec newWritableDateStateContext(
        String validFrom,
        String validTo
    ) throws ServiceException {
        return DateStateViewContext.newTimeRangeViewContext(
            fromBasicFormat(validFrom),
            fromBasicFormat(validTo)
        );
    }

    protected static InteractionSpec newReadOnlyDateStateContext(
        String validFor,
        String invalidatedAt
    ) throws ServiceException {
        try {
            return DateStateViewContext.newTimePointViewContext(
                fromBasicFormat(validFor),
                new Date(
                    DateFormat.getInstance().parse(invalidatedAt).getTime() - 1
                )
            );
        } catch (ParseException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Could not parse the given date-time value",
                new BasicException.Parameter("invalidatedAt", invalidatedAt)
            );
        }
    }

    /**
     * Parse a date's basic format representation according to ISO 8601
     * 
     * @param date the date's basic format representation
     * 
     * @return the date's XML Gregorian Calendar representation
     * 
     * @throws ServiceException
     */
    public static XMLGregorianCalendar fromBasicFormat(
        String date
    ) throws ServiceException{
        if(date == null) {
            return null;
        } else try {
            return DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(
                date.substring(0, 4) + '-' + date.substring(4, 6) + '-' + date.substring(6)
            );
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                "Could not parse the given date value",
                new BasicException.Parameter("date", date)
            );
        }
    }

    /**
     * Retrieve a date's basic format representation according to ISO 8601
     * 
     * @param date an XML Gregorian Calendar Date
     * 
     * @return the date's basic format representation
     */
    public static String toBasicFormat(
        XMLGregorianCalendar date
    ){
        if(date == null) {
            return null;
        } else {
            String standardFormat = date.toString();
            int length = standardFormat.length();
            return 
            standardFormat.substring(0, length - 6) +
            standardFormat.substring(length - 5, length - 3) +
            standardFormat.substring(length - 2);
        }
    }

    /**
     * Retrieve the current date
     * 
     * @return the current date
     */
    public static XMLGregorianCalendar today(
    ){
        GregorianCalendar calendar = new GregorianCalendar();
        return DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
            calendar.get(Calendar.YEAR), 
            calendar.get(Calendar.MONTH) + 1, 
            calendar.get(Calendar.DAY_OF_MONTH),
            DatatypeConstants.FIELD_UNDEFINED
        );
    }

    /**
     * Tells whether two dates are adjacent
     * 
     * @param date1
     * @param date2
     * 
     * @return <code>true</code> if two dates are adjacent
     * @throws ServiceException  
     */
    public static boolean adjacent (
        String date1,
        String date2
    ) throws ServiceException {
        XMLGregorianCalendar value1 = fromBasicFormat(date1);
        XMLGregorianCalendar value2 = fromBasicFormat(date2);
        value1.add(ONE_DAY);
        return value1.equals(value2);
    }

    /**
     * Retrieve the previous day
     * 
     * @param date
     * 
     * @return the previous day
     */
    public static XMLGregorianCalendar predecessor(
        XMLGregorianCalendar date
    ){
        XMLGregorianCalendar predecessor = (XMLGregorianCalendar) date.clone();
        predecessor.add(MINUS_ONE_DAY);
        return predecessor;
    }

    /**
     * Retrieve the next day
     * 
     * @param date
     * 
     * @return the next day
     */
    public static XMLGregorianCalendar successor(
        XMLGregorianCalendar date
    ){
        XMLGregorianCalendar successor = (XMLGregorianCalendar) date.clone();
        successor.add(ONE_DAY);
        return successor;
    }
    
    /**
     * Plus one day
     */
    public static final Duration ONE_DAY = DatatypeFactories.xmlDatatypeFactory(
    ).newDurationDayTime(
        true, // isPositive
        1, // day
        0, // hour
        0, // minute
        0 // second
    );

    /**
     * Minus one day
     */
    public static final Duration MINUS_ONE_DAY = DatatypeFactories.xmlDatatypeFactory(
    ).newDurationDayTime(
        false, // isPositive
        1, // day
        0, // hour
        0, // minute
        0 // second
    );
    
}
