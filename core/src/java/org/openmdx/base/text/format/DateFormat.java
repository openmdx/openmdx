/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DateFormat.java,v 1.7 2007/01/22 16:05:58 hburger Exp $
 * Description: infrastructure: date format
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/01/22 16:05:58 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * This class provides thread-safe DateFormatters
 */
public class DateFormat extends ThreadLocal {

    /**
     * Creates a DateFormat object for the spiecified pattern.
     * 
     * @deprecated use DateFormat.getInstance(String)
     */
    public DateFormat(
    	String pattern
    ){
    	this.pattern = pattern;
        this.rejectE = isPatternWithoutText(pattern);
    }
        
    private static final boolean isPatternWithoutText(
        String pattern
    ){
        for(
           int i = 0;
           i < pattern.length();
           i++
        ){
            char c = pattern.charAt(i);
            if(Character.isLetter(c) && BASIC_FORMAT.indexOf(c) < 0) {
                return false;
            }
        }
        return true;   
    }
    
    /**
     * Returns a DateFormat object for the	"yyyyMMdd'T'HHmmss.SSS'Z'" pattern.
     */
    public static DateFormat getInstance(){
    	return instance;
    }

    /**
     * Returns a DateFormat object for the  given pattern.
     * 
     * @param pattern
     * 
     * @return a DateFormat object for the  given pattern
     */
    public static synchronized DateFormat getInstance(
        String pattern
    ){
        DateFormat instance = (DateFormat)patternMap.get(pattern);
        if(instance == null) patternMap.put(
            pattern,
            instance = new DateFormat(pattern)
        );
        return instance;
    }
    
    /* (non-Javadoc)
     * @see java.lang.ThreadLocal#initialValue()
     */
    protected Object initialValue() {
    	SimpleDateFormat formatter = new SimpleDateFormat(pattern);
    	formatter.setLenient(false);
    	formatter.setTimeZone(UTC);
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
    	return getFormat().format(date);
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
    	return getFormat().parse(text);
    }
    
    /**
     * Get the thread local <code>SimpleDateFormat</code>er
     * 
     * @return a thread local <code>SimpleDateFormat</code>er
     */
    private final SimpleDateFormat getFormat(
    ){
        return (SimpleDateFormat)get();
    }
		
    
    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------
	
    /**
     * The pattern for this instance
     */
    final private String pattern;
    
    /**
     * JAXP Issue 12 handling
     */
    final boolean rejectE;

    
    //------------------------------------------------------------------------
    // Class members
    //------------------------------------------------------------------------
    
    /**
     * Associates patterns with thread maps
     */
    final static private Map patternMap = new HashMap();
    
    /**
     * An instance with the default format "yyyyMMdd'T'HHmmss.SSS'Z'"
     */
    final static private DateFormat instance = new Lenient();    
    
    /**
     * The UTC time zone
     */
    final private static TimeZone UTC = TimeZone.getTimeZone("UTC");

    /**
     * 
     */
    private final static String BASIC_FORMAT =  "yyyyMMdd'T'HHmmss.SSS'Z'";
    
    
    //------------------------------------------------------------------------
    // Class Lenient
    //------------------------------------------------------------------------
    
    /**
     * Handle the XMLGregorianCalendar's nanoseconds
     */
    private static class Lenient extends DateFormat {

        /**
         * Constructor 
         *
         * @param pattern
         */
        Lenient() {
            super(BASIC_FORMAT);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.text.format.DateFormat#parse(java.lang.String)
         */
        public Date parse(
            String text
        ) throws ParseException {
            return super.parse(
                text.length() > 20 && text.endsWith("Z") ? 
                text.substring(0, 19) + 'Z' :
                text
            );        
        }

    }

}
