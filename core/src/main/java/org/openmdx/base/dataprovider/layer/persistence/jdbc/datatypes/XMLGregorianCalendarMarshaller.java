/*
 * ====================================================================
 * Description: XMLGregorianCalendarMarshaller 
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.namespace.QName;

import org.openmdx.base.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.DataTypes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
#if CLASSIC_CHRONO_TYPES
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi.ImmutableDatatypeFactory;
#endif

import org.w3c.spi2.Datatypes;

/**
 * XMLGregorianCalendarMarshaller
 */
public class XMLGregorianCalendarMarshaller {

    /**
     * Constructor 
     *
     * @param dateTimeStandardTimeZone
     * @param dateTimeDaylightSavingTimeZone TODO
     * @param sqlDataTypes
     * @param dateTimePrecision 
     * @throws ServiceException
     */
    protected XMLGregorianCalendarMarshaller(
        String dateTimeStandardTimeZone,
        String dateTimeDaylightSavingTimeZone,
        DataTypes sqlDataTypes, 
        TimeUnit dateTimePrecision
    ) throws ServiceException {
        this.sqlDataTypes = sqlDataTypes;
        this.dateTimeFormatBefore1970 = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS"
        );
        this.dateTimeFormatBefore1970.setTimeZone(
            XMLGregorianCalendarMarshaller.UTC
        );
        this.dateTimeFormatSince1970 = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss"
        );
        this.dateTimeFormatSince1970.setTimeZone(
            TimeZone.getTimeZone(dateTimeStandardTimeZone)
        );
        this.dateTimeDaylightSavingTimeZone = dateTimeDaylightSavingTimeZone;
        this.dateTimePrecision = dateTimePrecision;
    }

    /**
     * The precision used for date/time values since {@code 1970-01-01T00:00:00Z}.
     */
    private final TimeUnit dateTimePrecision;
    
    /**
     * 
     */
    private final String dateTimeDaylightSavingTimeZone;
    
    /**
     * 
     */
    private DataTypes sqlDataTypes;

    /**
     * The type used to store {@code org::w3c::time} values, i.e. one of<ul>
     * <li>{@code STANDARD}
     * <li>{@code TIME}
     * <li>{@code CHARACTER} <i>(default)</i>
     * <li>{@code NUMERIC}
     * </ul>
     * 
     * @see LayerConfigurationEntries#TIME_TYPE_STANDARD
     * @see LayerConfigurationEntries#TIME_TYPE_TIME
     * @see LayerConfigurationEntries#TIME_TYPE_CHARACTER
     * @see LayerConfigurationEntries#TIME_TYPE_NUMERIC
     */
    private static final List<String> TIME_TYPES = Arrays.asList(
        LayerConfigurationEntries.TIME_TYPE_STANDARD,
        LayerConfigurationEntries.TIME_TYPE_TIME,
        LayerConfigurationEntries.TIME_TYPE_CHARACTER,
        LayerConfigurationEntries.TIME_TYPE_NUMERIC
    );

    /**
     * The type used to store {@code org::w3c::date} values, i.e. one of<ul>
     * <li>{@code STANDARD}
     * <li>{@code DATE}
     * <li>{@code CHARACTER} <i>(default)</i>
     * </ul>
     * 
     * @see LayerConfigurationEntries#DATE_TYPE_STANDARD
     * @see LayerConfigurationEntries#DATE_TYPE_DATE
     * @see LayerConfigurationEntries#DATE_TYPE_CHARACTER
     */
    private static final List<String> DATE_TYPES = Arrays.asList(
        LayerConfigurationEntries.DATE_TYPE_STANDARD,
        LayerConfigurationEntries.DATE_TYPE_DATE,
        LayerConfigurationEntries.DATE_TYPE_CHARACTER
    );

    /**
     * The type used to store {@code org::w3c::dateTime} values, i.e. one of<ul>
     * <li>{@code STANDARD}
     * <li>{@code TIMESTAMP}
     * <li>{@code TIMESTAMP_WITH_TIMEZONE}
     * <li>{@code CHARACTER} <i>(default)</i>
     * <li>{@code NUMERIC}
     * </ul>
     * @see LayerConfigurationEntries#DATETIME_TYPE_STANDARD
     * @see LayerConfigurationEntries#DATETIME_TYPE_TIMESTAMP
     * @see LayerConfigurationEntries#DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE
     * @see LayerConfigurationEntries#DATETIME_TYPE_CHARACTER
     * @see LayerConfigurationEntries#DATETIME_TYPE_NUMERIC
     */
    private static final List<String> DATETIME_TYPES = Arrays.asList(
        LayerConfigurationEntries.DATETIME_TYPE_STANDARD,
        LayerConfigurationEntries.DATETIME_TYPE_TIMESTAMP,
        LayerConfigurationEntries.DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE,
        LayerConfigurationEntries.DATETIME_TYPE_CHARACTER,
        LayerConfigurationEntries.DATETIME_TYPE_NUMERIC            
    );

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
     * Factory method
     * 
     * @return a new XML GregorianCalendar Marshaller Instance
     * 
     * @throws ServiceException
     */
    public static XMLGregorianCalendarMarshaller newInstance(
        String timeType,
        String dateType,
        String dateTimeType, 
        String dateTimeStandardTimeZone, 
        String dateTimeDaylightSavingTimeZone,
        String dateTimePrecision, 
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
        TimeUnit precision;
        try {
            precision = TimeUnit.valueOf(dateTimePrecision);
        } catch (RuntimeException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Unsupported dateTimePrecision value",
                new BasicException.Parameter("supported", TimeUnit.SECONDS, TimeUnit.MILLISECONDS, TimeUnit.MICROSECONDS, TimeUnit.NANOSECONDS),
                new BasicException.Parameter("requested", dateTimePrecision)
            );
        }
        return new XMLGregorianCalendarMarshaller(
            dateTimeStandardTimeZone,
            dateTimeDaylightSavingTimeZone,
            sqlDataTypes, 
            precision
        );
    }

    /**
     * 
     * 
     * @param source
     * @param connection
     * @return
     * 
     * @throws ServiceException
     */
    public Object marshal(
        Object source, 
        Connection connection
    ) throws ServiceException {
        if(Datatypes.DATE_CLASS.isInstance(source)) {
            #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif value = (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) source;
            QName schemaType = value.getXMLSchemaType();
            if(DatatypeConstants.TIME.equals(schemaType)) {
                String timeType = sqlDataTypes.getTimeType(connection).intern();
                if(timeType.equals(LayerConfigurationEntries.TIME_TYPE_NUMERIC)) {
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
                    Timestamp timestamp = new Timestamp(
                        value.toGregorianCalendar().getTimeInMillis()
                    );
                    BigDecimal fractionalSeconds = value.getFractionalSecond();
                    if(fractionalSeconds != null && fractionalSeconds.scale() > 3) {
                        timestamp.setNanos(fractionalSeconds.scaleByPowerOfTen(9).intValue());
                    }
                    return timestamp;
                } else if(dateTimeType.equals(LayerConfigurationEntries.DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE)) {
                    return sqlDateTime(value, true);
                } else if(dateTimeType.equals(LayerConfigurationEntries.DATETIME_TYPE_NUMERIC)) {
                    return value.getFractionalSecond().add(
                        BigDecimal.valueOf(value.toGregorianCalendar().getTimeInMillis() / 1000)
                    );
                } else if(dateTimeType.equals(LayerConfigurationEntries.DATETIME_TYPE_CHARACTER)) {
                    return value.normalize().toXMLFormat().replaceAll("[:\\-]","");
                } else {
                    return sqlDateTime(value, false);
                }
            } else if (DatatypeConstants.DATE.equals(schemaType)) {
                String dateType = sqlDataTypes.getDateType(connection).intern();
                String date = value.toString();
                if(dateType.equals(LayerConfigurationEntries.DATE_TYPE_CHARACTER)) {
                    return date.replaceAll("-", "");
                } else try {
                    return Date.valueOf(date);    
                } catch (IllegalArgumentException exception) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSFORMATION_FAILURE,
                        "Unable to convert the value into an SQL date, maybe there are some inappropriate fields set",
                        new BasicException.Parameter("dateType", dateType),
                        new BasicException.Parameter("value", date),
                        new BasicException.Parameter("valid", value.isValid()),
                        new BasicException.Parameter("years", value.getEonAndYear()),
                        new BasicException.Parameter("months", maskUndefined(value.getMonth())),
                        new BasicException.Parameter("days", maskUndefined(value.getDay())),
                        new BasicException.Parameter("hours", maskUndefined(value.getHour())),
                        new BasicException.Parameter("minutes", maskUndefined(value.getMinute())),
                        new BasicException.Parameter("seconds", maskUndefined(value.getSecond(), value.getFractionalSecond())),
                        new BasicException.Parameter("timzone", maskUndefined(value.getTimezone()))
                    );
                }
            } else throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                getClass().getName() + " supports only the XML datatypes [time, datetime, date]",
                new BasicException.Parameter("value", value)
            );
        } else {
            return source;
        }
    }

    /**
     * Stringify a Datatype field
     * 
     * @param value the {@code int} representation of the field value
     * 
     * @return the {@code String} representation of the field value, or 
     * {@code null} if the field is undefined
     */
    private static String maskUndefined(
        int value
    ){
        return value == DatatypeConstants.FIELD_UNDEFINED ? null : Integer.toString(value);
    }

    /**
     * Stringify the seconds fields
     * 
     * @param seconds
     * @param fractionalSeconds
     * 
     * @return the complete seconds fields
     */
    private static String maskUndefined(
        int seconds,
        BigDecimal fractionalSeconds
    ){
        if(seconds == DatatypeConstants.FIELD_UNDEFINED) {
            return null;
        } else if (fractionalSeconds == null) {
            return Integer.toString(seconds);
        } else {
            return fractionalSeconds.add(BigDecimal.valueOf(seconds)).toString();
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
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif xmlDateTime,
        boolean withTimeZone
    ){
    	final long millisecondsSince1970 = xmlDateTime.toGregorianCalendar().getTimeInMillis();
    	final SimpleDateFormat format;
    	final String part1;
        if(millisecondsSince1970 < 0){
            format = this.dateTimeFormatBefore1970; // including fractional part in MILLISECONDS precision
            part1 = "";
        } else {
            format = this.dateTimeFormatSince1970; // excluding fractional part
            switch(this.dateTimePrecision) {
                case NANOSECONDS: 
                    part1 = "." + String.valueOf(1000000000 + xmlDateTime.getFractionalSecond().movePointRight(9).intValue()).substring(1);
                    break;
                case MICROSECONDS:
                    part1 = "." + String.valueOf(1000000 + xmlDateTime.getFractionalSecond().movePointRight(6).intValue()).substring(1);
                    break;
                case MILLISECONDS:
                    part1 = "." + String.valueOf(1000 + xmlDateTime.getFractionalSecond().movePointRight(3).intValue()).substring(1);
                    break;
                case SECONDS: default: 
                    part1 = "";
            }
        }
        final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif dateTime = #if CLASSIC_CHRONO_TYPES new java.util.Date #else java.time.Instant.ofEpochMilli#endif(millisecondsSince1970);
        final String part2 = withTimeZone ? (" " + getTimeZone(format.getTimeZone(), dateTime)) : ""; 
        final String part0 = format.format(dateTime);
        return part0 + part1 + part2;
    }

	/**
	 * Build the time zone information
	 * 
	 * @return the time zone region and including DST information
	 */
	private  String getTimeZone(
            TimeZone standardTimeZone,
            #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif dateTime
    ) {
		return standardTimeZone.inDaylightTime(dateTime) ? this.dateTimeDaylightSavingTimeZone : standardTimeZone.getID();
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
            return parse(((String) source).replaceFirst(" ", "T"));
        } else if(source instanceof Time) {
            return parse("T" + source);
        } else if (source instanceof Timestamp) {
            Timestamp value = (Timestamp) source;
            long milliseconds = value.getTime();
            java.util.GregorianCalendar calendar = new GregorianCalendar(UTC);
            calendar.setTimeInMillis(milliseconds);
            #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif target
                    = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(calendar);
            switch(this.dateTimePrecision){
                case NANOSECONDS:
                    //
                    // Be aware JAXP 1.4.0 issue:
                    // we get E notation of BigDecimal if (-scale + coeff.length-1) < -6
                    //
                    target.setFractionalSecond(BigDecimal.valueOf(value.getNanos(), 9));
                    break;
                case MICROSECONDS:
                    target.setFractionalSecond(BigDecimal.valueOf(value.getNanos() / 1000, 6));
                    break;
                case SECONDS:
                    target.setFractionalSecond(BigDecimal.ZERO);
                    break;
                default:
                    break;
            }
            return target;
        } else if(source instanceof java.time.LocalDateTime) {
        	java.time.LocalDateTime value = (java.time.LocalDateTime)source;
            java.util.GregorianCalendar calendar = GregorianCalendar.from(ZonedDateTime.of(value, ZoneOffset.UTC));
            #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif target = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(calendar);
            switch(this.dateTimePrecision) {
                case NANOSECONDS:
                    //
                    // Be aware JAXP 1.4.0 issue:
                    // we get E notation of BigDecimal if (-scale + coeff.length-1) < -6
                    //
                    target.setFractionalSecond(BigDecimal.valueOf(value.getNano(), 9));
                    break;
                case MICROSECONDS:
                    target.setFractionalSecond(BigDecimal.valueOf(value.getNano() / 1000, 6));
                    break;
                case SECONDS:
                    target.setFractionalSecond(BigDecimal.ZERO);
                    break;
                default:
                    break;
            }
            return target;
        } else if (Datatypes.DATE_TIME_CLASS.isInstance(source)) {
            return parse(source.toString());
        } else if (source instanceof BigDecimal) {
            BigDecimal value = (BigDecimal)source;
            long milliseconds = value.movePointRight(3).longValue();
            java.util.GregorianCalendar calendar = new GregorianCalendar(UTC);
            calendar.setTimeInMillis(milliseconds);
            #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif target
                    = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(calendar);
            target.setFractionalSecond(
                value.subtract(BigDecimal.valueOf(milliseconds / 1000))
            );
            return target;
        } else {
            return source;
        }
    }

    /**
     * Unmarshal an XML datatype value
     * 
     * @param value
     * 
     * @return the corresponding datatype value
     */
    private Object parse(
        String value
    ){
        return 
            value.startsWith("P") || value.startsWith("-P") ? Datatypes.create(Datatypes.DURATION_CLASS, value) :
            value.indexOf('T') < 0 ? Datatypes.create(Datatypes.DATE_CLASS, value) :
            Datatypes.create(Datatypes.DATE_TIME_CLASS, value);
    }
       
}
