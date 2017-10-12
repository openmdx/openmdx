/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Date 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2011, OMEX AG, Switzerland
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
package org.w3c.cci2;

import static javax.xml.datatype.DatatypeConstants.DATE;
import static javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED;
import static javax.xml.datatype.DatatypeConstants.INDETERMINATE;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 * Date
 * <p>
 * An unmodifiable XMLGregorianCalendar implementation representing a date
 */
public final class ImmutableDate
    extends XMLGregorianCalendar
    implements Serializable, ImmutableDatatype<XMLGregorianCalendar>, Comparable<XMLGregorianCalendar>
{

    /**
     * Constructor 
     * <p>
     * <em>Note:<br>
     * The value is tested by the factory, not by the constructor
     *
     * @param basicValue value in ISO 8601:2000 basic format
     */
    public ImmutableDate(
        String basicValue
    ){
        this.basicValue = basicValue;
    }
    
    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#add(javax.xml.datatype.Duration)
     */
    @Override
    public void add(Duration duration) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#clear()
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(READONLY);
    }

    /**
     * Retrieve the mutable equivalent
     * 
     * @return the mutable equivalent
     */
    private XMLGregorianCalendar toMutableDate(){
        final DatatypeFactory datatypeFactory = MutableDatatypeFactory.xmlDatatypeFactory();
        return getEon().signum() == 0 ? datatypeFactory.newXMLGregorianCalendarDate(
            getYear(),
            getMonth(),
            getDay(),
            FIELD_UNDEFINED
        ) : datatypeFactory.newXMLGregorianCalendar(
                getEonAndYear(), 
                getMonth(),
                getDay(),
                FIELD_UNDEFINED, // hour
                FIELD_UNDEFINED, // minute
                FIELD_UNDEFINED, // second
                null, // fractionalSecond
                FIELD_UNDEFINED // time-zone
        );
    }
    
    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#clone()
     */
    @Override
    public XMLGregorianCalendar clone(
    ) {
        return toMutableDate();
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#compare(javax.xml.datatype.XMLGregorianCalendar)
     */
    @Override
    public int compare(XMLGregorianCalendar that) {
        return that instanceof ImmutableDate ?
            compareTo((ImmutableDate)that) :
            toMutableDate().compare(that);
    }

    
    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getDay()
     */
    @Override
    public int getDay(
    ) {
        int i = this.basicValue.length() - 2;
        return 
            10 * Character.digit(this.basicValue.charAt(i++), 10) +
            Character.digit(this.basicValue.charAt(i), 10);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getEon()
     */
    @Override
    public BigInteger getEon() {
        if(this.eon == null) {
            int i = this.basicValue.length();
            this.eon = i > 13 ? 
                new BigInteger(this.basicValue.substring(0, i - 13) + "000000000") :
                BigInteger.ZERO;
        }
        return this.eon;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getEonAndYear()
     */
    @Override
    public BigInteger getEonAndYear() {
        if(this.eonAndYear == null) {
            int i = this.basicValue.length();
            this.eonAndYear = i > 13 ? 
                new BigInteger(this.basicValue.substring(0, i - 4)) :
                BigInteger.valueOf(getYear());
        }
        return this.eonAndYear;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getFractionalSecond()
     */
    @Override
    public BigDecimal getFractionalSecond() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getHour()
     */
    @Override
    public int getHour() {
        return FIELD_UNDEFINED;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getMinute()
     */
    @Override
    public int getMinute() {
        return FIELD_UNDEFINED;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getMonth()
     */
    @Override
    public int getMonth() {
        int i = this.basicValue.length() - 4;
        return 
            10 * Character.digit(this.basicValue.charAt(i++), 10) +
            Character.digit(this.basicValue.charAt(i), 10);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getSecond()
     */
    @Override
    public int getSecond() {
        return FIELD_UNDEFINED;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getTimeZone(int)
     */
    @Override
    public TimeZone getTimeZone(int defaultZoneoffset) {
        return defaultZoneoffset == FIELD_UNDEFINED ?
            TimeZone.getDefault() :
            TimeZone.getTimeZone(getTimeZoneId(defaultZoneoffset));
    }

    /**
     * Calculate a custom time zone id
     * 
     * @param timeZoneOffset
     * 
     * @return the corresponding time zone id
     */
    private String getTimeZoneId(
        int timeZoneOffset
    ){
        char sign;
        int absoluteOffset;
        if(timeZoneOffset < 0){
            sign = '-';
            absoluteOffset = - timeZoneOffset;
        } else {
            sign = '+';
            absoluteOffset = timeZoneOffset;
        }
        String s = Integer.toString(
            10000 + 100 * (absoluteOffset / 60) + (absoluteOffset % 60)
        );
        return "GMT" + sign + s.substring(1, 3) + ':' + s.substring(3);
    }
    
    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getTimezone()
     */
    @Override
    public int getTimezone() {
        return FIELD_UNDEFINED;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getXMLSchemaType()
     */
    @Override
    public QName getXMLSchemaType() {
        return DATE;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#getYear()
     */
    @Override
    public int getYear() {
        int year = 0;
        for(
             int i = 0, iLimit = this.basicValue.length() - 4;
             i < iLimit;
             i++
        ){
             year *= 10;
             year += Character.digit(this.basicValue.charAt(i), 10);
        }
        return year;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#isValid()
     */
    @Override
    public boolean isValid() {
        return true;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#normalize()
     */
    @Override
    public XMLGregorianCalendar normalize() {
        return this;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#reset()
     */
    @Override
    public void reset() {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#setDay(int)
     */
    @Override
    public void setDay(int day) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#setFractionalSecond(java.math.BigDecimal)
     */
    @Override
    public void setFractionalSecond(BigDecimal fractional) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#setHour(int)
     */
    @Override
    public void setHour(int hour) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#setMillisecond(int)
     */
    @Override
    public void setMillisecond(int millisecond) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#setMinute(int)
     */
    @Override
    public void setMinute(int minute) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#setMonth(int)
     */
    @Override
    public void setMonth(int month) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#setSecond(int)
     */
    @Override
    public void setSecond(int second) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#setTimezone(int)
     */
    @Override
    public void setTimezone(int offset) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#setYear(java.math.BigInteger)
     */
    @Override
    public void setYear(BigInteger year) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#setYear(int)
     */
    @Override
    public void setYear(int year) {
        throw new UnsupportedOperationException(READONLY);
    }

    /**
     * Create a gregorian calendar without default values
     * 
     * @param timezone
     * @param locale
     * 
     * @return
     */
    private GregorianCalendar toGregorianCalendar(
        TimeZone timezone,
        Locale locale
    ) {
        GregorianCalendar result = new GregorianCalendar(
            timezone == null ? TimeZone.getDefault() : timezone, 
            locale == null ? Locale.getDefault() : locale
        );
        result.clear();
        result.setGregorianChange(PURE_GREGORIAN_CHANGE);
        result.set(Calendar.ERA, GregorianCalendar.AD);
        result.set(getYear(), getMonth() - 1, getDay());
        return result;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#toGregorianCalendar()
     */
    @Override
    public GregorianCalendar toGregorianCalendar() {
        return toGregorianCalendar(null, null);
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#toGregorianCalendar(java.util.TimeZone, java.util.Locale, javax.xml.datatype.XMLGregorianCalendar)
     */
    @Override
    public GregorianCalendar toGregorianCalendar(
        TimeZone timezone,
        Locale locale,
        XMLGregorianCalendar defaults
    ) {
        GregorianCalendar result = toGregorianCalendar(timezone, locale);
        if(defaults != null) {
            int h = defaults.getHour();
            if(h != FIELD_UNDEFINED) {
                result.set(Calendar.HOUR_OF_DAY, h);
            }
            int m = defaults.getMinute();
            if (m != FIELD_UNDEFINED) {
                result.set(Calendar.MINUTE, m);
            }
            int s = defaults.getSecond();
            if (s != FIELD_UNDEFINED) {
                result.set(Calendar.SECOND, s);
            }
            int f = defaults.getMillisecond();
            if (f != FIELD_UNDEFINED) {
                result.set(Calendar.MILLISECOND, f);
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see javax.xml.datatype.XMLGregorianCalendar#toXMLFormat()
     */
    @Override
    public String toXMLFormat() {
        if(this.extendedValue == null) {
            int i = this.basicValue.length();
            this.extendedValue = new StringBuilder(
                i + 2
            ).append(
                this.basicValue.substring(0, i - 4)
            ).append(
                '-'
            ).append(
                this.basicValue.substring(i - 4, i - 2)
            ).append(
                '-'
            ).append(
                this.basicValue.substring(i - 2, i)
            ).toString();
        }
        return this.extendedValue;
    }


    //------------------------------------------------------------------------
    // Implements Comparable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(XMLGregorianCalendar that) {
        if(that instanceof ImmutableDate) {
            return compareTo((ImmutableDate)that);
        } else {
            int result = toMutableDate().compare(that);
            if(result == INDETERMINATE) {
                throw new IllegalArgumentException(
                    "Value not comparable to org::w3c::date: " + that.toXMLFormat()
                );
            }
            return result;
        }
    }

    /**
     * Compare to native instances
     * 
     * @param that
     * 
     * @return 
     */
    private int compareTo(
        ImmutableDate that
    ) {
        int result = this.basicValue.length() - that.basicValue.length();
        if(result == 0) {
            result = this.basicValue.compareTo(that.basicValue);
        }
        return result < 0 ? -1 : result == 0 ? 0 : +1;
    }

    
    //------------------------------------------------------------------------
    // Implements Datatype
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.w3c.spi.Datatype#toBasicFormat()
     */
    public String toBasicFormat() {
        return this.basicValue;
    }

    /**
     * There is no need for the deserialized object to be immutable
     * 
     * @return a mutable counterpart of this object
     * 
     * @throws ObjectStreamException
     */
    private Object writeReplace() throws ObjectStreamException {
        return toMutableDate();
    }
    
    
    //------------------------------------------------------------------------
    // Members
    //------------------------------------------------------------------------
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -6028198343774949922L;

    /**
     * @serial ISO8601:2000 Basic Format
     */
    private final String basicValue;

    /**
     * ISO8601:2000 Extended Format
     */
    private transient String extendedValue;

    /**
     * 
     */
    private transient BigInteger eonAndYear;

    /**
     * 
     */
    private transient BigInteger eon;    
    
    /**
     * 
     */
    private static final String READONLY = 
        "This " + XMLGregorianCalendar.class.getName() + 
        " instance is read-only, use clone() to get a modifiable copy.";
    
    /**
     *   <p>Obtain a pure Gregorian Calendar by calling
     *   GregorianCalendar.setChange(PURE_GREGORIAN_CHANGE). </p>
     */
    private static final java.util.Date PURE_GREGORIAN_CHANGE = new java.util.Date(
        Long.MIN_VALUE
    );

}
