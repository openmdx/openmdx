/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: infrastructure: date format
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
package org.w3c.format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * This class provides thread-safe DateFormatters
 */
public class DateTimeFormat extends ThreadLocal<SimpleDateFormat> {

    /**
     * Creates a DateFormat object for the specified pattern.
     */
    protected DateTimeFormat(
    	String pattern,
    	String timeZone,
    	boolean lenient
    ){
    	this.pattern = pattern;
        this.rejectE = isPatternWithoutText(pattern);
        this.timeZone = timeZone == null ? null : TimeZone.getTimeZone(timeZone);
        this.lenient = lenient;
    }
 
    /**
     * The pattern for this instance
     */
    final private String pattern;
    
    /**
     * JAXP Issue 12 handling
     */
    final boolean rejectE;

    /**
     * The time zone to be used
     */
    final private TimeZone timeZone;
    
    /**
     * Distinguish between lenient and strict parsers
     */
    final private boolean lenient;

    /**
     * Match YYYY[...]MMDD
     */
    public static final Pattern BASIC_DATE_PATTERN = Pattern.compile("^\\d{8,}$");

    /**
     * Match YYYY[...]-MM-DD
     */
    public static final Pattern EXTENDED_DATE_PATTERN = Pattern.compile("^\\d{4,}-\\d{2}-\\d{2}$");

    /**
     * Associates patterns with thread maps
     */
    final static private ConcurrentMap<String, DateTimeFormat> patternMap = 
        new ConcurrentHashMap<String, DateTimeFormat>();
    
    /**
     * ISO 8601:2004 compliant extended format
     */
    protected final static String EXTENDED_UTC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        
    /**
     * ISO 8601:2004 compliant extended format
     */
    public final static DateTimeFormat EXTENDED_UTC_FORMAT = new Lenient(
        EXTENDED_UTC_PATTERN,
        "0000-01-01T00:00:00.000Z",
        "Z", "+00:00", "-00:00", "+00", "-00"
    );
    
    /**
     * ISO 8601:2004 compliant basic pattern
     */
    protected final static String BASIC_UTC_PATTERN = "yyyyMMdd'T'HHmmss.SSS'Z'";

    /**
     * ISO 8601:2004 compliant basic format
     */
    public final static DateTimeFormat BASIC_UTC_FORMAT = new Lenient(
        BASIC_UTC_PATTERN,
        "00000101T000000.000Z",
        "Z", "+0000", "-0000", "+00", "-00"
    );

    /**
     * (Not very smart) test do decide whether exponents can be detected and rejected.
     * 
     * @param pattern the pattern
     * 
     * @return <code>true</code> if exponents shall be detected and rejected
     */
    static final boolean isPatternWithoutText(
        String pattern
    ){
        for(
           int i = 0;
           i < pattern.length();
           i++
        ){
            char c = pattern.charAt(i);
            if(Character.isLetter(c) && BASIC_UTC_PATTERN.indexOf(c) < 0) {
                return false;
            }
        }
        return true;   
    }
    
    /**
     * Returns a DateFormat object for the  given pattern.
     * 
     * @param pattern
     * 
     * @return a DateFormat object for the  given pattern
     */
    public static DateTimeFormat getInstance(
        String pattern
    ){
        return getInstance(pattern,"UTC",false);
    }

    /**
     * Returns a DateFormat object for the  given arguments
     * 
     * @param pattern the pattern to be used
     * @param timeZone the time zone id, or <code>null</code> for local time zone
     * @param lenient tells whether parsing is lenient or strict
     * 
     * @return the requested <code>DateTimeFormat</code>
     */
    public static DateTimeFormat getInstance(
        String pattern,
        String timeZone,
        boolean lenient
    ){
        String id = pattern + '*' + (timeZone == null ? "LOCAL" : timeZone) + '*' + (lenient ? "LENIENT" : "STRICT");
        DateTimeFormat instance = patternMap.get(id);
        if(instance == null) {
            DateTimeFormat concurrent = patternMap.putIfAbsent(
                id,
                instance = new DateTimeFormat(pattern, timeZone, lenient)
            );
            return concurrent == null ? instance : concurrent;
        } else {
            return instance;
        }
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.ThreadLocal#initialValue()
     */
    @Override
    protected SimpleDateFormat initialValue() {
    	SimpleDateFormat formatter = new SimpleDateFormat(pattern);
    	formatter.setLenient(this.lenient);
    	if(this.timeZone != null) {
        	formatter.setTimeZone(this.timeZone);
    	}
    	return formatter;
    }	

    /**
     * Formats a Date into a date/time string.	
     *
     * @param	date	the time value to be formatted into a time string.
     *
     * @return	the formatted time string.
     */	
    public String format(
    	Date date
    ){
    	return get().format(date);
    }
    
    /**
     * Parse a date/time string.
     *
     * @param	text	The date/time string to be parsed
     *
     * @return	A Date, or null if the input could not be parsed
     *
     * @exception	ParseException
     *				If the given string cannot be parsed as a date.
     */
    public Date parse(
    	String text
    ) throws ParseException {
        if(this.rejectE) {
            int e = text.indexOf('E'); 
            if(e > 0) throw new ParseException(
                "Unparseable date: " + text + " (May be you are using the broken JAXP release 1.4.0?)",
                e            
            );
        }
    	return get().parse(text);
    }
            
    
    //------------------------------------------------------------------------
    // Class Lenient
    //------------------------------------------------------------------------
    
    /**
     * Complete the century in case of a two digit year
     * <p>
     * The difference between the current year and the resulting year is not greater than fifty years.
     * 
     * @param date
     * 
     * @return a normalized value (with maybe trailing zeroes, as opposed to W3's XML schema recommendation
     * 
     * @throws ParseException
     */
    public static String completeCentury(
        String date
    ) throws ParseException, NumberFormatException {
        int firstHyphen = date.indexOf('-'); 
        if(
            firstHyphen != 2 && 
            (firstHyphen >= 0 || date.length() != 6)
        ) {
            return date;
        } else {
            int y2 = Integer.parseInt(date.substring(0, 2));
            int y4 = Calendar.getInstance().get(Calendar.YEAR);
            int d = y2 - y4 % 100;
            int c2 = y4 / 100 + (d <= -50 ? 1 : d > 50 ? -1 : 0);
            return String.valueOf(c2) + date;
        }
    }


    /**
     * Handle the XMLGregorianCalendar's nanoseconds
     */
    private static class Lenient extends DateTimeFormat {

        /**
         * Constructor 
         *
         * @param pattern
         * @param defaultValue for missing characters 
         * @param utcTimezone UTC time zone representations
         */
        Lenient(
            String pattern,
            String defaultValue,
            String... utcTimezone
        ) {
            super(pattern, "UTC", false);
            this.defaultValue = defaultValue;
            this.utcTimezone = utcTimezone;
        }

        /**
         * Default value for missing characters 
         */
        private final String defaultValue;
        
        /**
         * The UTC representations
         */
        private final String[] utcTimezone;
        
        /**
         * Test and remove the time zone
         * 
         * @param text
         * 
         * @return the value without time zone
         * 
         * @throws ParseException if the UTC time zone field is missing
         */
        private final String validateAndRemoveTimezone(
            String text
        ) throws ParseException {
            for(String utcTimezone : this.utcTimezone) {
                if(text.endsWith(utcTimezone)) {
                    return text.substring(0, text.length() - utcTimezone.length());
                }
            }
            throw new ParseException(
                "Value does not end with UTC timezone field. " +
                "value=" + text + "; " +
                "acceptable=" +  Arrays.asList(this.utcTimezone),
                text.length()
            );
        }
        
        /**
         * Convert to millisecond accuracy
         * 
         * @param text
         * 
         * @return a normalized value (with maybe trailing zeroes, as opposed to W3's XML schema recommendation
         * 
         * @throws ParseException
         */
        private final String adjustToMillisecondAccuracyAndAddTimezone(
            String rawText
        ) throws ParseException {
            //
            // Replace standard compliant fraction separator by canonical one
            //
            String text;
            if(rawText.indexOf(',') > 0) {
                text = rawText.replace(',', '.');
            } else {
                text = rawText;
            }
            //
            // Add missing century in case of a two digit year
            //
            int timeSeparator = text.indexOf('T'); 
            String oldDate = timeSeparator < 0 ? text : text.substring(0, timeSeparator);
            String newDate = completeCentury(oldDate);
            if(newDate.length() > oldDate.length()) {
                text = timeSeparator < 0 ? newDate : newDate + text.substring(timeSeparator);
            }
            //
            // Adjust accuracy
            //
            return text.length() >= this.defaultValue.length() ? 
                text.substring(0, this.defaultValue.length() -1) + "Z" :
                text + defaultValue.substring(text.length(), defaultValue.length());
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.text.format.DateFormat#parse(java.lang.String)
         */
        @Override
        public Date parse(
            String text
        ) throws ParseException {
            return super.parse(
                adjustToMillisecondAccuracyAndAddTimezone(
                    validateAndRemoveTimezone(text)
                )
            );        
        }

    }

}
