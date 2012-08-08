/*
 * ====================================================================
 * Description: Date State Contexts
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:24 $
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
package org.openmdx.compatibility.state1.view;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.resource.cci.InteractionSpec;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date State Contexts
 */
public class DateStateContexts {

    protected DateStateContexts(
    ) {
        // Avoid instantiation 
    }

    public static DateStateContext newDateStateContext(
        XMLGregorianCalendar validFor,
        Date validAt
    ) {
        return new DateStateViewContext(
            validFor,
            validAt
        );
    }

    public static DateStateContext newDateStateContext(
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ) {
        return new DateStateViewContext(
            validFrom,
            validTo
        );
    }

    public static Path getStatedObject(
        Object_1_0 object
    ) throws ServiceException {
        return getStatedObject(object.objGetPath());
    }

    protected static Path getStatedObject(
        Path state
    ){
        if(state == null) {
            return null;
        } else {
            String base = state.getBase();
            int s = base.indexOf(';');
            if(s < 0) {
                //
                // Persistent-new DateState instance
                //
                return state;
            } else {
                //
                // Persistent DateState instance
                //
                Path statedObject = state.getParent();
                statedObject.add(
                    base.substring(0, s)
                );
                return statedObject;
            }
        }
    }

    protected static InteractionSpec newWritableDateStateContext(
        String validFrom,
        String validTo
    ) throws ServiceException {
        return new DateStateViewContext(
            fromBasicFormat(validFrom),
            fromBasicFormat(validTo)
        );
    }

    protected static InteractionSpec newReadOnlyDateStateContext(
        String validFor,
        String invalidatedAt
    ) throws ServiceException {
        try {
            return new DateStateViewContext(
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

    public static InteractionSpec newDateStateContext(
        Object_1_0 prefetched
    ) throws ServiceException {
        String invalidatedAt = (String)prefetched.objGetValue(State_1_Attributes.INVALIDATED_AT);
        return invalidatedAt == null ? DateStateContexts.newWritableDateStateContext(
            (String)prefetched.objGetValue(State_1_Attributes.STATE_VALID_FROM),
            (String)prefetched.objGetValue(State_1_Attributes.STATE_VALID_TO)
        ) : DateStateContexts.newReadOnlyDateStateContext (
            (String)prefetched.objGetValue(State_1_Attributes.STATE_VALID_FROM),
            invalidatedAt
        );
    }

    /**
     * Compare two XMLGregorianCalendar values where <code>null</code> is
     * considered to be smaller than every other value.
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareValidFrom(
        XMLGregorianCalendar d1,
        XMLGregorianCalendar d2
    ){
        return d1 == null ? (
                d2 == null ? 0 : -1
        ) : (
                d2 == null ? 1 : d1.compare(d2)
        );
    }

    /**
     * Compare two XMLGregorianCalendar values where <code>null</code> is
     * considered to be greater than every other value.
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareValidTo(
        XMLGregorianCalendar d1,
        XMLGregorianCalendar d2
    ){
        return d1 == null ? (
                d2 == null ? 0 : 1
        ) : (
                d2 == null ? -1 : d1.compare(d2)
        );
    }

    public static XMLGregorianCalendar fromBasicFormat(
        String date
    ) throws ServiceException{
        if(date == null) {
            return null;
        } else try {
            return datatypeFactory.newXMLGregorianCalendar(
                date.substring(0, 4) + '-' + date.substring(4, 6) + '-' + date.substring(6)
            );
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Could not parse the given date value",
                new BasicException.Parameter("date", date)
            );
        }
    }

    public static String toBasicFormat(
        XMLGregorianCalendar date
    ){
        if(date == null) {
            return null;
        } else {
            String standardFormat = date.toString();
            return 
            standardFormat.substring(0, standardFormat.length() - 6) +
            standardFormat.substring(standardFormat.length() - 5, standardFormat.length() - 3) +
            standardFormat.substring(standardFormat.length() - 2);
        }
    }

    /**
     * Retrieve the Datatype Factory
     * 
     * @return the datatype factory to be used
     */
    private static DatatypeFactory newDatatypeFactory(
    ){
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException exception) {
            Logger logger = LoggerFactory.getLogger(DateStateContexts.class);
            logger.error(
                "DatatypeFactory acquisition failed, " +
                "DateStateViews will not be able to provide current date", 
                exception
            );
            return null;
        }
    }

    /**
     * Retrieve the current date
     * 
     * @return the current date
     */
    static XMLGregorianCalendar today(
    ){
        if(DateStateContexts.datatypeFactory == null) {
            return null;
        } else {
            GregorianCalendar calendar = new GregorianCalendar();
            return DateStateContexts.datatypeFactory.newXMLGregorianCalendarDate(
                calendar.get(Calendar.YEAR), 
                calendar.get(Calendar.MONTH) + 1, 
                calendar.get(Calendar.DAY_OF_MONTH),
                DatatypeConstants.FIELD_UNDEFINED
            );
        }
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
     * The DatatypeFactory instance
     */
    static private final DatatypeFactory datatypeFactory = newDatatypeFactory();    

    /**
     * One day
     */
    static final Duration ONE_DAY = datatypeFactory == null ? 
        null : 
            datatypeFactory.newDurationDayTime(true, 1, 0, 0, 0);

}
