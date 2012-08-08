/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestDateFormat.java,v 1.5 2006/11/22 12:12:17 hburger Exp $
 * Description: TestDateFormat 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/11/22 12:12:17 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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

package org.openmdx.test.base.text.pattern;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.kernel.exception.BasicException;

/**
 * TestDateFormat
 *
 */
public class TestDateFormat
    extends TestCase
{

    /**
     * Constructor 
     *
     */
    public TestDateFormat() {
        super();
    }

    /**
     * Constructor 
     *
     * @param name
     */
    public TestDateFormat(String name) {
        super(name);
    }

    /**
     * Timestamps may be entered in one of the following ways<ul>
     * <li>20051219T230000.000Z
     * <li>20-12-2005 00:00:00
     * <li>20.12.2005
     * </ul>
     */
    public void testSwitch() {
        assertEquals("20051219T230000.000Z", 20, "20051219T230000.000Z".length());
        assertEquals("20-12-2005 00:00:00", 19, "20-12-2005 00:00:00".length());
        assertEquals("20.12.2005", 10, "20.12.2005".length());
    }

    public void testParse() throws ParseException {
        DateFormat format20 = DateFormat.getInstance();
        SimpleDateFormat format19 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        SimpleDateFormat format10 = new SimpleDateFormat("dd.MM.yyyy");
        Date date20 = format20.parse("20051220T000000.000Z");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date20);
        int offset = TimeZone.getDefault().getOffset(
            calendar.get(Calendar.ERA),
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.DAY_OF_WEEK),
            calendar.get(Calendar.MILLISECOND) + 1000 * (
                calendar.get(Calendar.SECOND) + 60 * (
                    calendar.get(Calendar.MINUTE) + 60 * (
                        calendar.get(Calendar.HOUR)
                    )
                )
            )                
        );
//      assertEquals("offest", TimeZone.getDefault().getOffset(date20.getTime()), offset);
        Date date = new Date (date20.getTime() - offset);
        Date date19 = format19.parse("20-12-2005 00:00:00");
        Date date10 = format10.parse("20.12.2005");
        assertEquals("20-12-2005 00:00:00", date, date19);
        assertEquals("20.12.2005", date, date10);
    }
    
    public void testTimeZone() throws ServiceException, ParseException{
        assertEquals("Z (UTC)", "20010929T154521.798Z", getDateFromString("2001-09-29T15:45:21.798Z"));
        assertEquals("GMT+01:00 (UTC-1h)", "20010929T144521.798Z", getDateFromString("2001-09-29T15:45:21.798GMT+01:00"));
        assertEquals("GMT+02:00 (UTC-2h)", "20010929T134521.798Z", getDateFromString("2001-09-29T15:45:21.798GMT+02:00"));

        assertEquals("CEST with summer date (UTC-2h)", "20010929T134521.798Z", getDateFromString("2001-09-29T15:45:21.798CEST"));
        assertEquals("CET  with winter date (UTC-1h)", "20010129T144521.798Z", getDateFromString("2001-01-29T15:45:21.798CET"));
        assertEquals("CEST with winter date (UTC-1h)", "20010129T134521.798Z", getDateFromString("2001-01-29T15:45:21.798CEST"));

        if("Europe/Berlin".equals(TimeZone.getDefault().getID())) {
            assertEquals(
                "Local summer date (UTC-2h)", 
                "20010929T134521.798Z", 
                getDateFromString("2001-09-29T15:45:21.798")
            );
            assertEquals(
                "Local winter date (UTC-1h)", 
                "20010129T144521.798Z", 
                getDateFromString("2001-01-29T15:45:21.798")
             );
        } else {
            System.out.println("Skipping Europe/Berlin test");
        }
    }
    
    /**
     * Code extracted from XMLImporter
     * 
     * @param dateTime
     * @return
     * @throws ServiceException
     */
    private String getDateFromString(
        String dateTime
    ) throws ServiceException {
        // Convert time zone from ISO 8601 to SimpleDateFormat 
        int timePosition = dateTime.indexOf('T');
        if(dateTime.endsWith("Z")){ // SimpleDateFormat can't handle 'Z' time zone designator
          dateTime=dateTime.substring(0,dateTime.length()-1)+"GMT+00:00";
        } else {
            int timeZonePosition = dateTime.lastIndexOf('-');
            if(timeZonePosition < timePosition) timeZonePosition = dateTime.lastIndexOf('+');
            if(
                timeZonePosition > timePosition &&
                ! dateTime.regionMatches(true, timeZonePosition-3, "GMT", 0, 3)
            ) dateTime=dateTime.substring(
                0, 
                timeZonePosition
            ) + "GMT" + dateTime.substring(
                timeZonePosition
            );
        }        
        int timeLength = dateTime.length() - timePosition - 1;
        try {
            return DateFormat.getInstance().format(
                dateTime.indexOf('.', timePosition) == -1 ? 
                (timeLength == 8 ? localSecondFormat.parse(dateTime) : secondFormat.parse(dateTime)) : 
                (timeLength == 12 ? localMillisecondFormat.parse(dateTime) : millisecondFormat.parse(dateTime))
            );
        } catch (ParseException e) {
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("value", dateTime)
                },
                "DateTime conversion failed"
            );
        }
    }

    private final static DateFormat secondFormat = DateFormat.getInstance(
        "yyyy-MM-dd'T'HH:mm:ssz"
    );
    private final static DateFormat millisecondFormat = DateFormat.getInstance(
        "yyyy-MM-dd'T'HH:mm:ss.SSSz"
    );
    private final SimpleDateFormat localSecondFormat = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss"
    );
    private final SimpleDateFormat localMillisecondFormat = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSS"
    );

}
