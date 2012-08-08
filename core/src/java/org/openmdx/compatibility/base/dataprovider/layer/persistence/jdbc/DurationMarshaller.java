/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DurationMarshaller.java,v 1.9 2008/09/10 08:55:19 hburger Exp $
 * Description: DurationMarshaller 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:19 $
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
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;

/**
 * DurationMarshaller
 */
public class DurationMarshaller
implements Marshaller
{

    /**
     * Constructor 
     *
     * @param durationType
     * @param xmlDatatypes
     * 
     * @throws ServiceException 
     */
    protected DurationMarshaller(
        String durationType, 
        boolean xmlDatatypes
    ) throws ServiceException {
        if(!DURATION_TYPES.contains(durationType)) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.INVALID_CONFIGURATION,
            "Unsupported duration type",
            new BasicException.Parameter("supported", DURATION_TYPES),
            new BasicException.Parameter("requested", durationType)
        );
        this.durationType = durationType.intern();
        try {
            this.datatypeFactory = xmlDatatypes ? DatatypeFactory.newInstance() : null;
        } catch (DatatypeConfigurationException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "DatatypeFactory acquisition failed"
            );
        }
        if(this.durationType == LayerConfigurationEntries.DURATION_TYPE_INTERVAL) {
            this.YEAR_TO_MONTH = Pattern.compile(
                "^(-?)([0-9]+)-([0-9]+)$"
            );
            this.DAY_TO_SECOND = Pattern.compile(
                "^(-?)([0-9]+) ([0-9]+)(?::([0-9]+)(?::([0-9]+\\.[0-9]*)))?$"
            );
        } else {
            this.YEAR_TO_MONTH = null;
            this.DAY_TO_SECOND = null;
        }
    }

    /**
     * Factory
     * 
     * @param type the duration type
     * @param xmlDatatypes 
     * 
     * @return an new <code>DurationMarshaller</code> instance
     * @throws ServiceException 
     */
    public static DurationMarshaller newInstance(
        String durationType, 
        boolean xmlDatatypes
    ) throws ServiceException{
        return new DurationMarshaller(durationType, xmlDatatypes);
    }

    /**
     * Non-<code>null</code> if <code>xmlDatatypes</code> is <code>true</code>.
     */
    private final DatatypeFactory datatypeFactory;

    /**
     * The type used to store <code>org::w3c::duration</code> values, i.e. one of<ul>
     * <li><code>INTERVAL</code> <i>(domain defined by the database field definition)</i>
     * <li><code>CHARACTER</code> <i>(default)</i>
     * <li><code>NUMERIC</code> <i>(domain <b>either</b> year-month <b>or</b> date-time intervals!)</i>
     * </ul>
     * 
     * @see LayerConfigurationEntries#DURATION_TYPE_INTERVAL
     * @see LayerConfigurationEntries#DURATION_TYPE_CHARACTER
     * @see LayerConfigurationEntries#DURATION_TYPE_NUMERIC
     */
    private final String durationType;  
    private static final List<String> DURATION_TYPES = Arrays.asList(
        LayerConfigurationEntries.DURATION_TYPE_INTERVAL,
        LayerConfigurationEntries.DURATION_TYPE_CHARACTER,
        LayerConfigurationEntries.DURATION_TYPE_NUMERIC
    );

    private static final BigDecimal DAY_TIME_ZERO = BigDecimal.valueOf(0, 3);
    private static final BigInteger MONTHS_PER_YEAR = BigInteger.valueOf(12);
    private static final BigInteger HOURS_PER_DAY = BigInteger.valueOf(24);
    private static final BigInteger MINUTES_PER_HOUR = BigInteger.valueOf(60);
    private static final BigInteger SECONDS_PER_MINUTE = BigInteger.valueOf(60);

    final private Pattern YEAR_TO_MONTH;
    final private Pattern DAY_TO_SECOND;

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#marshal(java.lang.Object)
     */
    public Object marshal(
        Object source
    ) throws ServiceException {
        if(source instanceof Duration) {
            if(LayerConfigurationEntries.DURATION_TYPE_CHARACTER == durationType) {
                return source.toString();
            } else if (
                    LayerConfigurationEntries.DURATION_TYPE_INTERVAL == durationType ||
                    LayerConfigurationEntries.DURATION_TYPE_NUMERIC == durationType 
            ) {
                Duration duration = (Duration) source;
                int signum = duration.getSign();
                BigInteger years = duration.isSet(DatatypeConstants.YEARS) ? 
                    (BigInteger) duration.getField(DatatypeConstants.YEARS) :
                        BigInteger.ZERO;
                    BigInteger months = duration.isSet(DatatypeConstants.MONTHS) ? 
                        (BigInteger) duration.getField(DatatypeConstants.MONTHS) :
                            BigInteger.ZERO;
                        BigInteger days = duration.isSet(DatatypeConstants.DAYS) ?
                            (BigInteger) duration.getField(DatatypeConstants.DAYS) :
                                BigInteger.ZERO;
                            BigInteger hours = duration.isSet(DatatypeConstants.HOURS) ?
                                (BigInteger) duration.getField(DatatypeConstants.HOURS) :
                                    BigInteger.ZERO;
                                BigInteger minutes = duration.isSet(DatatypeConstants.MINUTES) ?
                                    (BigInteger) duration.getField(DatatypeConstants.MINUTES) :
                                        BigInteger.ZERO;
                                    BigDecimal seconds = duration.isSet(DatatypeConstants.SECONDS) ?
                                        (BigDecimal) duration.getField(DatatypeConstants.SECONDS) :
                                            DAY_TIME_ZERO;
                                        boolean yearMonth = 
                                            years.signum() != 0 ||
                                            months.signum() != 0;
                                        boolean dayTime =
                                            days.signum() != 0 ||
                                            hours.signum() != 0 ||
                                            minutes.signum() != 0 ||
                                            seconds.signum() != 0;                
                                        if(yearMonth & dayTime) throw new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.TRANSFORMATION_FAILURE,
                                            "The duration must be either a year-month or a day-time duration",
                                            new BasicException.Parameter("duration", duration)
                                        );
                                        if(!yearMonth & !dayTime) yearMonth = 
                                            duration.isSet(DatatypeConstants.YEARS) ||
                                            duration.isSet(DatatypeConstants.MONTHS);
                                        if (LayerConfigurationEntries.DURATION_TYPE_INTERVAL == durationType) {
                                            return (
                                                    signum < 0 ? "-" : ""
                                            ) + (
                                                    yearMonth ? 
                                                        years + "-" + months :
                                                            days + " " + hours + ":" + minutes + ":" + seconds
                                            );
                                        } else {
                                            if(yearMonth) {
                                                BigInteger value = months.add(
                                                    years.multiply(MONTHS_PER_YEAR)
                                                );
                                                return signum < 0 ? value.negate() : value;
                                            } else {
                                                BigDecimal value = seconds.add(
                                                    new BigDecimal(
                                                        minutes.add(
                                                            hours.add(
                                                                days.multiply(HOURS_PER_DAY)
                                                            ).multiply(MINUTES_PER_HOUR)
                                                        ).multiply(SECONDS_PER_MINUTE)
                                                    )
                                                );
                                                return signum < 0 ? value.negate() : value;
                                            }
                                        }
            } else {
                return source;
            }
        } else {
            return source;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        if(source == null) {
            return source;
        } else if(LayerConfigurationEntries.DURATION_TYPE_CHARACTER == durationType) {
            return toDuration((CharSequence)source);
        } else if (LayerConfigurationEntries.DURATION_TYPE_INTERVAL == durationType){
            String value = source.toString();
            Matcher matcher;
            if((matcher = DAY_TO_SECOND.matcher(value)).matches()) {
                StringBuilder duration = new StringBuilder(
                ).append(
                    matcher.group(1)
                ).append(
                    'P'
                ).append(
                    matcher.group(2)
                ).append(
                    "DT"
                ).append(
                    matcher.group(3)
                ).append(
                    "H"
                );
                if(matcher.group(4) != null) duration.append(
                    matcher.group(4)
                ).append(
                    "M"
                );
                if(matcher.group(5) != null) duration.append(
                    matcher.group(5)
                ).append(
                    "S"
                );
                return toDuration(duration);                
            } else if ((matcher = YEAR_TO_MONTH.matcher(value)).matches()) {
                StringBuilder duration = new StringBuilder(
                ).append(
                    matcher.group(1)
                ).append(
                    'P'
                ).append(
                    matcher.group(2)
                ).append(
                    "Y"
                ).append(
                    matcher.group(3)
                ).append(
                    "M"
                );
                return toDuration(duration);                
            } else throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                getClass().getName() +
                " expects at least two fields (years and months or days and hours)",
                new BasicException.Parameter("value", value)
            );
        } else if (LayerConfigurationEntries.DURATION_TYPE_NUMERIC == durationType) {
            if(source instanceof Number) {
                Number value = (Number)source;
                return source instanceof BigDecimal && ((BigDecimal)source).scale() > 0 ?
                    toDuration("T", value, "S") :
                        toDuration("", value, "M");
            } else throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                getClass().getName() + " expects durationType " +
                LayerConfigurationEntries.DURATION_TYPE_NUMERIC + 
                " values being instances of " + Number.class.getName(),
                new BasicException.Parameter("class", source.getClass().getName()),
                new BasicException.Parameter("value", source)
            );             
        } else {
            return source;
        }
    }

    private Object toDuration(
        String prefix,
        Number infix,
        String suffix
    ){
        String value = infix.toString();
        return toDuration(
            value.charAt(0) == '-' ? (
                    "-P" + prefix + value.substring(1) + suffix
            ) : (
                    "P" + prefix + value + suffix 
            )
        );
    }

    protected Object toDuration(
        CharSequence source
    ){
        if(this.datatypeFactory == null) {
            return source;
        } else {
            boolean yearMonth = false;
            boolean dayTime = false;
            for(
                    int i = 0, iLimit = source.length();
                    i < iLimit;
                    i++
            ) {
                switch(source.charAt(i)) {
                    case 'Y': case 'M': yearMonth = true; break;
                    case 'D': case 'T': dayTime = true; break;
                }
            }
            String duration = source.toString();
            if(yearMonth == dayTime) {
                return this.datatypeFactory.newDuration(duration);
            } else if (yearMonth) {
                return this.datatypeFactory.newDurationYearMonth(duration);
            } else { // dayTime
                return this.datatypeFactory.newDurationDayTime(duration);
            }
        }
    }

}
