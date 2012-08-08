/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AlternativeDatatypeFactory.java,v 1.17 2010/02/16 18:40:42 hburger Exp $
 * Description: Immutable Datatype Factory
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/02/16 18:40:42 $
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
package org.w3c.spi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.ImmutableDate;
import org.w3c.cci2.ImmutableDateTime;
import org.w3c.format.DateTimeFormat;

/**
 * Alternative Datatype Factory
 */
class AlternativeDatatypeFactory implements ImmutableDatatypeFactory {

    /**
     * Match YYYY[...]-MM-DD
     */
    private static final Pattern extendedDatePattern = Pattern.compile("^\\d{4,}-\\d{2}-\\d{2}$");

    /**
     * Match YYYY[...]MMDD
     */
    private static final Pattern basicDatePattern = Pattern.compile("^\\d{8,}$");

    private static final BigInteger MONTHS_PER_YEAR = BigInteger.valueOf(12);
    private static final BigDecimal SECONDS_PER_MINUTE = BigDecimal.valueOf(60);
    private static final BigInteger MINUTES_PER_HOUR = BigInteger.valueOf(60);
    private static final BigInteger HOURS_PER_DAY = BigInteger.valueOf(24);
    
    
    //------------------------------------------------------------------------
    // Implements DatatypeFactory
    //------------------------------------------------------------------------
    
    /**
     * Create an UTC based immutable date-time instance
     * 
     * @param value the basic or extended representation
     * 
     * @return a corresponding date-time instance
     * 
     * @exception IllegalArgumentException
     * if the value can't be parse
     */
    public ImmutableDateTime newDateTime(
        String value
    ){
        if(value == null) {
            return null;
        } else try {
            DateTimeFormat dateTimeFormat = DateTimeFormat.BASIC_UTC_FORMAT;
            int firstMinus = value.indexOf('-');
            if(firstMinus > 0) {
                int timeSeparator = value.indexOf('T');
                if(timeSeparator < 0 || firstMinus < timeSeparator) {
                    dateTimeFormat = DateTimeFormat.EXTENDED_UTC_FORMAT;
                }
            }
            return toDateTime(dateTimeFormat.parse(value));
        } catch (ParseException exception) {
        	throw BasicException.initHolder(
        		new IllegalArgumentException(
        		    "Could not parse as org::w3c::dateTime value",
        		    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("value", value)
                    )
                )
            );
        }
    }
    
    /**
     * Create a date instance
     * 
     * @param value the basic or extended representation
     * 
     * @return a corresponding date-time instance
     * 
     * @exception IllegalArgumentException
     * if the value can't be parsed
     */
    public ImmutableDate newDate(
        String value
    ){
        if(value == null) {
            return null;
        } else try {
            value = DateTimeFormat.completeCentury(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                "Century completion failure",
                exception
            );
        }
        if(basicDatePattern.matcher(value).matches()) {
            return new ImmutableDate(value);
        } else if(extendedDatePattern.matcher(value).matches()) {
            return new ImmutableDate(value.replaceAll("-", ""));
        } else {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "The value does not match the org::w3c::date pattern",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("pattern", "YYYY[...]-MM-DD"),
                        new BasicException.Parameter("value", value)
                    )
                )
            );
        }
    }

    /**
     * Create a duration instance
     * 
     * @param value the representation with designators
     * 
     * @return a corresponding Duration instance
     * 
     * @exception IllegalArgumentException
     * if the value can't be parsed
     */
    public Duration newDuration(
        String value
    ){
        if(value == null) {
            return null;
        } else {
            boolean yearMonth = value.indexOf('Y') > 0 || value.indexOf('M') > 0;
            boolean dayTime = value.indexOf('D') > 0 || value.indexOf('T') > 0;   
            return 
                yearMonth == dayTime ? DatatypeFactories.xmlDatatypeFactory().newDuration(value) :
                yearMonth ? DatatypeFactories.xmlDatatypeFactory().newDurationYearMonth(value) :
                DatatypeFactories.xmlDatatypeFactory().newDurationDayTime(value);
        }
    }

    /**
     * Create a date-time instance
     * 
     * @param value an internal representation
     * 
     * @return a corresponding date-time instance
     * 
     * @exception IllegalArgumentException
     * if the value is not an org::w3c::dateTime instance
     */
    public ImmutableDateTime toDateTime(
        java.util.Date value
    ){
        return 
        value == null ? null :
            value instanceof ImmutableDateTime ? (ImmutableDateTime)value :
            new ImmutableDateTime(value.getTime());
    }
    
    /**
     * Create a date instance
     * 
     * @param value an internal representation
     * 
     * @return a corresponding date-time instance
     * 
     * @exception IllegalArgumentException
     * if the value is not an org::w3c::date instance
     */
    public ImmutableDate toDate(
        javax.xml.datatype.XMLGregorianCalendar value
    ){
        return 
            value == null ? null :
            value instanceof ImmutableDate ? (ImmutableDate)value :
            newDate(value.toXMLFormat());
    }
    
    /* (non-Javadoc)
     * @see org.w3c.spi.ImmutableDatatypeFactory#normalizedDuration(javax.xml.datatype.Duration)
     */
    public Duration toNormalizedDuration(
        Duration value
    ) {
        BigInteger years = (BigInteger) value.getField(DatatypeConstants.YEARS);
        BigInteger months = (BigInteger) value.getField(DatatypeConstants.MONTHS);
        BigInteger days =  (BigInteger) value.getField(DatatypeConstants.DAYS);
        BigInteger hours =  (BigInteger) value.getField(DatatypeConstants.HOURS);
        BigInteger minutes =  (BigInteger) value.getField(DatatypeConstants.MINUTES);
        BigDecimal seconds = (BigDecimal) value.getField(DatatypeConstants.SECONDS);            
        boolean normalized = true;
        if(seconds != null && seconds.compareTo(SECONDS_PER_MINUTE) >= 0) {
            normalized = false;
            BigDecimal[] minutesAndSeconds = seconds.divideAndRemainder(SECONDS_PER_MINUTE);
            minutes = minutes == null ? minutesAndSeconds[0].toBigInteger() : minutes.add(
                minutesAndSeconds[0].toBigInteger()
            );
            seconds = minutesAndSeconds[1];
        }
        if(minutes != null && minutes.compareTo(MINUTES_PER_HOUR) >= 0) {
            normalized = false;
            BigInteger[] hoursAndMinutes = minutes.divideAndRemainder(MINUTES_PER_HOUR);
            hours = hours == null ? hoursAndMinutes[0] : hours.add(
                hoursAndMinutes[0]
            );
            minutes = hoursAndMinutes[1];
        }
        if(hours != null && hours.compareTo(HOURS_PER_DAY) >= 0) {
            normalized = false;
            BigInteger[] daysAndHours = hours.divideAndRemainder(HOURS_PER_DAY);
            days = days == null ? daysAndHours[0] : days.add(
                daysAndHours[0]
            );
            hours = daysAndHours[1];
        }
        if(months != null && months.compareTo(MONTHS_PER_YEAR) >= 0) {
            normalized = false;
            BigInteger[] yearsAndMonths = months.divideAndRemainder(MONTHS_PER_YEAR);
            years = years == null ? yearsAndMonths[0] : years.add(
                yearsAndMonths[0]
            );
            months = yearsAndMonths[1];
        }
        return normalized ? value : DatatypeFactories.xmlDatatypeFactory().newDuration(
            value.getSign() > 0, 
            years, 
            months, 
            days, 
            hours, 
            minutes, 
            seconds
        );
    }

    
}