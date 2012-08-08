/*
 * ====================================================================
 * Name:        $Id: XMLGregorianCalendarMarshaller.java,v 1.23 2008/09/25 23:35:03 hburger Exp $
 * Description: XMLGregorianCalendarMarshaller 
 * Revision:    $Revision: 1.23 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/25 23:35:03 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DatatypeFormat;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.spi.DatatypeFactories;

/**
 * XMLGregorianCalendarMarshaller
 */
@SuppressWarnings("unchecked")
public class XMLGregorianCalendarMarshaller {

    /**
     * Constructor 
     *
     * @param timeType
     * @param dateType
     * @param dateTimeType
     * @param xmlDatatypes
     * 
     * @throws ServiceException
     */
    protected XMLGregorianCalendarMarshaller(
        String dateTimeZone,
        boolean xmlDatatypes,
        DataTypes sqlDataTypes
    ) throws ServiceException {
        this.sqlDataTypes = sqlDataTypes;
        this.datatypeFormat = xmlDatatypes ? new DatatypeFormat[]{
            DatatypeFormat.newInstance(false), // EXTENDED
            DatatypeFormat.newInstance(true) // BASIC
        } : null;
            this.xmlDatatypes = xmlDatatypes;
            this.dateTimeFormatBefore1970 = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSS"
            );
            this.dateTimeFormatBefore1970.setTimeZone(
                XMLGregorianCalendarMarshaller.UTC
            );
            this.dateTimeFormatSince1970 = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSS"
            );
            this.dateTimeFormatSince1970.setTimeZone(
                TimeZone.getTimeZone(dateTimeZone)
            );
    }

    /**
     * Factory method
     * 
     * @param timeType
     * @param dateType
     * @param dateTimeType
     * @param dateTimeZone 
     * @param xmlDatatypes
     * @return a new XML GregorianCalendar Marshaller Instance
     * 
     * @throws ServiceException
     */
    public static XMLGregorianCalendarMarshaller newInstance(
        String timeType,
        String dateType,
        String dateTimeType, 
        String dateTimeZone, 
        boolean xmlDatatypes,
        DataTypes sqlDataTypes
    ) throws ServiceException{
        if(!TIME_TYPES.contains(timeType)) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.INVALID_CONFIGURATION,
            "Unsupported time type",
            new BasicException.Parameter("supported", TIME_TYPES),
            new BasicException.Parameter("requested", timeType)
        );
        if(!DATE_TYPES.contains(dateType)) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.INVALID_CONFIGURATION,
            "Unsupported date type",
            new BasicException.Parameter("supported", DATE_TYPES),
            new BasicException.Parameter("requested", dateType)
        );
        if(!DATETIME_TYPES.contains(dateTimeType)) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.INVALID_CONFIGURATION,
            "Unsupported dateTime type",
            new BasicException.Parameter("supported", DATETIME_TYPES),
            new BasicException.Parameter("requested", dateTimeType)
        );
        return new XMLGregorianCalendarMarshaller(
            dateTimeZone,
            xmlDatatypes,
            sqlDataTypes
        );
    }

    /**
     * datatypeFormat index
     */
    private final static int EXTENDED = 0;

    /**
     * datatypeFormat index
     */
    private final static int BASIC = 1;

    /**
     * 
     */
    private DataTypes sqlDataTypes;

    /**
     * The type used to store <code>org::w3c::time</code> values, i.e. one of<ul>
     * <li><code>STANDARD</code>
     * <li><code>TIME</code>
     * <li><code>CHARACTER</code> <i>(default)</i>
     * <li><code>NUMERIC</code>
     * </ul>
     * 
     * @see LayerConfigurationEntries#TIME_TYPE_STANDARD
     * @see LayerConfigurationEntries#TIME_TYPE_TIME
     * @see LayerConfigurationEntries#TIME_TYPE_CHARACTER
     * @see LayerConfigurationEntries#TIME_TYPE_NUMERIC
     */
    private static final List TIME_TYPES = Arrays.asList(
        LayerConfigurationEntries.TIME_TYPE_STANDARD,
        LayerConfigurationEntries.TIME_TYPE_TIME,
        LayerConfigurationEntries.TIME_TYPE_CHARACTER,
        LayerConfigurationEntries.TIME_TYPE_NUMERIC
    );

    /**
     * The type used to store <code>org::w3c::date</code> values, i.e. one of<ul>
     * <li><code>STANDARD</code>
     * <li><code>DATE</code>
     * <li><code>CHARACTER</code> <i>(default)</i>
     * </ul>
     * 
     * @see LayerConfigurationEntries#DATE_TYPE_STANDARD
     * @see LayerConfigurationEntries#DATE_TYPE_DATE
     * @see LayerConfigurationEntries#DATE_TYPE_CHARACTER
     */
    private static final List DATE_TYPES = Arrays.asList(
        LayerConfigurationEntries.DATE_TYPE_STANDARD,
        LayerConfigurationEntries.DATE_TYPE_DATE,
        LayerConfigurationEntries.DATE_TYPE_CHARACTER
    );

    /**
     * The type used to store <code>org::w3c::dateTime</code> values, i.e. one of<ul>
     * <li><code>STANDARD</code>
     * <li><code>TIMESTAMP</code>
     * <li><code>TIMESTAMP_WITH_TIMEZONE</code>
     * <li><code>CHARACTER</code> <i>(default)</i>
     * <li><code>NUMERIC</code>
     * </ul>
     * @see LayerConfigurationEntries#DATETIME_TYPE_STANDARD
     * @see LayerConfigurationEntries#DATETIME_TYPE_TIMESTAMP
     * @see LayerConfigurationEntries#DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE
     * @see LayerConfigurationEntries#DATETIME_TYPE_CHARACTER
     * @see LayerConfigurationEntries#DATETIME_TYPE_NUMERIC
     */
    private static final List DATETIME_TYPES = Arrays.asList(
        LayerConfigurationEntries.DATETIME_TYPE_STANDARD,
        LayerConfigurationEntries.DATETIME_TYPE_TIMESTAMP,
        LayerConfigurationEntries.DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE,
        LayerConfigurationEntries.DATETIME_TYPE_CHARACTER,
        LayerConfigurationEntries.DATETIME_TYPE_NUMERIC            
    );

    /**
     * Non-<code>null</code> if <code>xmlDatatypes</code> is <code>true</code>.
     */
    private final DatatypeFormat[] datatypeFormat;

    /**
     * The <code>xmlDatatypes</code> flag.
     */
    private final boolean xmlDatatypes;

    /**
     * 
     */
    protected final static TimeZone UTC = TimeZone.getTimeZone("UTC");

    /**
     * For values >= 1979-01-01T00:00:00.000Z
     */
    private final SimpleDateFormat dateTimeFormatSince1970;

    /**
     * For values < 1979-01-01T00:00:00.000Z
     */
    private final SimpleDateFormat dateTimeFormatBefore1970;

    /**
     * 
     * 
     * @param source
     * @param connection
     * @param sqlProperties 
     * 
     * @return
     * 
     * @throws ServiceException
     */
    public Object marshal(
        Object source, 
        Connection connection
    ) throws ServiceException {
        if(source instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar value = (XMLGregorianCalendar) source;
            QName schemaType = value.getXMLSchemaType();
            if(DatatypeConstants.TIME.equals(schemaType)) {
                String timeType = sqlDataTypes.getTimeType(connection).intern();
                if(timeType == LayerConfigurationEntries.TIME_TYPE_NUMERIC) {
                    long milliseconds = value.getMillisecond() + 1000L * (
                            value.getSecond() + 60L * (
                                    value.getMinute() + 60L * (
                                            value.getHour()
                                    )
                            )
                    );
                    return BigDecimal.valueOf(milliseconds, 3);
                } else {
                    String time = value.toString();
                    if (timeType == LayerConfigurationEntries.TIME_TYPE_CHARACTER) {
                        return time.replaceAll(":", "");
                    } else {
                        int i = time.indexOf('.');
                        return Time.valueOf(
                            i < 0 ? time : time.substring(0, i)
                        );
                    }
                }
            } else if (DatatypeConstants.DATETIME.equals(schemaType)) {
                String dateTimeType = sqlDataTypes.getDateTimeType(connection).intern();
                if(dateTimeType == LayerConfigurationEntries.DATETIME_TYPE_TIMESTAMP) {
                    return new Timestamp(
                        value.toGregorianCalendar().getTimeInMillis()
                    );
                } else if(dateTimeType == LayerConfigurationEntries.DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE) {
                    return sqlDateTime(value, true);
                } else if(dateTimeType == LayerConfigurationEntries.DATETIME_TYPE_NUMERIC) {
                    return value.getFractionalSecond().add(
                        BigDecimal.valueOf(value.toGregorianCalendar().getTimeInMillis() / 1000)
                    );
                } else if(dateTimeType == LayerConfigurationEntries.DATETIME_TYPE_CHARACTER) {
                    return value.normalize().toXMLFormat().replaceAll("[:\\-]","");
                } else {
                    return sqlDateTime(value, false);
                }
            } else if (DatatypeConstants.DATE.equals(schemaType)) {
                String dateType = sqlDataTypes.getDateType(connection).intern();
                String date = value.toString();
                return dateType == LayerConfigurationEntries.DATE_TYPE_CHARACTER ?
                    date.replaceAll("-", "") :
                        (Object) Date.valueOf(date);    
            } else throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                getClass().getName() +
                " supports only the XML datatypes [time, datetime, date]",
                new BasicException.Parameter("value", value)
            );
        } else {
            return source;
        }
    }

    /**
     * Use UTC for org::w3c::dateTime values < 1970-01-01T00:00:00.000Z
     * 
     * @param xmlDateTime
     * @param withTimeZone
     * 
     * @return the SQL date time representation
     */
    private String sqlDateTime(
        XMLGregorianCalendar xmlDateTime,
        boolean withTimeZone
    ){
        long millisecondsSince1970 = xmlDateTime.toGregorianCalendar().getTimeInMillis();
        SimpleDateFormat format = millisecondsSince1970 < 0 ? this.dateTimeFormatBefore1970 : this.dateTimeFormatSince1970;
        String sqlDateTime = format.format(new Date(millisecondsSince1970));
        return withTimeZone ? 
            sqlDateTime + ' ' + format.getTimeZone().getID() :
                sqlDateTime;

    }

    /**
     * 
     * @param source
     * @return
     * @throws ServiceException
     */
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        if(source instanceof String) {
            String value = ((String) source).replaceFirst(" ", "T");
            boolean extended = value.indexOf('-') >= 0 || value.indexOf(':') >= 0;
            return this.datatypeFormat == null ?
                (extended ? value.replaceAll("[:\\-]", "") : value ) :
                    (this.datatypeFormat[extended ? EXTENDED : BASIC].marshal(value));
        } else if(source instanceof Time) {
            String value = "T" + source;
            return this.datatypeFormat == null ?
                value.replaceAll(":", "") :
                    this.datatypeFormat[EXTENDED].marshal(value);
        } else if (source instanceof Timestamp) {
            Timestamp value = (Timestamp) source;
            long milliseconds = value.getTime();
            if(this.xmlDatatypes) {
                java.util.GregorianCalendar calendar = new GregorianCalendar(UTC);
                calendar.setTimeInMillis(milliseconds);
                XMLGregorianCalendar target = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(calendar); 
                target.setFractionalSecond(
                    // JAXP 1.4.0 issue
                    // we get E notation of BigDecimal if (-scale + coeff.length-1) < -6
                    // Adjust to micros so we never get E notation
                    BigDecimal.valueOf(value.getNanos() / 1000, 6)
                );
                return target;
            } else {
                return DateFormat.getInstance().format(
                    new java.util.Date(milliseconds)
                );
            }
        } else if (source instanceof Date) {
            String value = source.toString();
            return this.datatypeFormat == null ?
                value.replaceAll("-", "") :
                    this.datatypeFormat[EXTENDED].marshal(value);
        } else if (source instanceof BigDecimal) {
            BigDecimal value = (BigDecimal)source;
            long milliseconds = value.movePointRight(3).longValue();
            if(this.xmlDatatypes) {
                java.util.GregorianCalendar calendar = new GregorianCalendar(UTC);
                calendar.setTimeInMillis(milliseconds);
                XMLGregorianCalendar target = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(calendar); 
                target.setFractionalSecond(
                    value.subtract(BigDecimal.valueOf(milliseconds / 1000))
                );
                return target;
            } else {
                return DateFormat.getInstance().format(
                    new java.util.Date(milliseconds)
                );
            }
        } else {
            return source;
        }
    }

}
