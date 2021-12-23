/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test Gregorian Calendar
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2009, OMEX AG, Switzerland
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
package test.util;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;
import org.w3c.format.DateTimeFormat;

/**
 * Test Gregorian Calendar
 */
public class TestGregorianCalendar {

    @Test
    public void testCR0003661 (
    ){
        System.out.println("Calendar Time Zone: " + new GregorianCalendar().getTimeZone().getID());
        {
            TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin"); 
            Date minDate = newGregorianCalendar(
                timeZone,
                1, 
                Calendar.JANUARY, 
                1
            ).getTime(); 
            assertEquals(timeZone.getDisplayName(), "00011231T230000.000Z", DateTimeFormat.BASIC_UTC_FORMAT.format(minDate));
        }
        {
            TimeZone timeZone = UTC; 
            Date minDate = newGregorianCalendar(
                timeZone,
                1, 
                Calendar.JANUARY, 
                1
            ).getTime(); 
            assertEquals(timeZone.getDisplayName(), "00010101T000000.000Z", DateTimeFormat.BASIC_UTC_FORMAT.format(minDate));
        }
        {
            TimeZone timeZone = TimeZone.getTimeZone("GMT"); 
            Date minDate = newGregorianCalendar(
                timeZone,
                1, 
                Calendar.JANUARY, 
                1
            ).getTime(); 
            assertEquals(timeZone.getDisplayName(), "00010101T000000.000Z", DateTimeFormat.BASIC_UTC_FORMAT.format(minDate));
        }
    }
    
    /**
     * Coordinated Universal Time
     */
    protected static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    /**
     * Create a <code>GregorianCalendar</code> for a give time zone and date
     *  
     * @param zone
     * @param year
     * @param month
     * @param date
     * 
     * @return a <code>GregorianCalendar</code> for a give time zone and date
     */
    protected static Calendar newGregorianCalendar(
        TimeZone zone, 
        int year,
        int month,
        int date        
    ){
        Calendar calendar = new GregorianCalendar(zone);
        calendar.clear();
        calendar.set(GregorianCalendar.YEAR, year);
        calendar.set(GregorianCalendar.MONTH, month);
        calendar.set(GregorianCalendar.DATE, date);
        return calendar;
    }

}
