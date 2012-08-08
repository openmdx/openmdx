/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestXMLGregorianCalendar.java,v 1.2 2006/01/11 08:45:47 hburger Exp $
 * Description: Test XMLGregorianCalendar
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/01/11 08:45:47 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.uses.javax.xml.datatype.jre.before1_5;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.text.format.DateFormat;
import org.openmdx.uses.javax.xml.datatype.DatatypeConfigurationException;
import org.openmdx.uses.javax.xml.datatype.DatatypeConstants;
import org.openmdx.uses.javax.xml.datatype.DatatypeFactory;
import org.openmdx.uses.javax.xml.datatype.XMLGregorianCalendar;


/**
 * Test XMLGregorianCalendar
 */
public class TestXMLGregorianCalendar extends TestCase {

    /**
     * @param name
     */
    public TestXMLGregorianCalendar(String name) {
        super(name);
    }

    /**
     * 
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) 
    {
        junit.textui.TestRunner.run (suite());
    }
    
    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite() 
    {
        return new TestSuite(TestXMLGregorianCalendar.class);
    }

    /**
     * A Datatype Factory Instance
     */
    protected DatatypeFactory xmlDatatypeFactory;
    
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     * @throws DatatypeConfigurationException
     */
    protected void setUp() throws DatatypeConfigurationException{
        this.xmlDatatypeFactory = DatatypeFactory.newInstance();
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testCalendarToDate(){
        XMLGregorianCalendar xmlGregorianCalendar = this.xmlDatatypeFactory.newXMLGregorianCalendarDate(
            2005, 05, 13, 
            DatatypeConstants.FIELD_UNDEFINED
        );
		System.out.println("xmlGregorianCalendar = '" + xmlGregorianCalendar + "'");
        GregorianCalendar gregorianCalendar = xmlGregorianCalendar.toGregorianCalendar();
        assertEquals("xmlGregorianCalendar.toString()", "2005-05-13", xmlGregorianCalendar.toString());
        assertEquals("xmlGregorianCalendar.getDay()", 13, xmlGregorianCalendar.getDay());
        assertEquals("xmlGregorianCalendar.getMonth()", 5, xmlGregorianCalendar.getMonth());
        assertEquals("xmlGregorianCalendar.getYear()", 2005, xmlGregorianCalendar.getYear());
        assertEquals("calendar.get(Calendar.DAY_OF_MONTH)", 13, gregorianCalendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("calendar.get(Calendar.MONTH)",05-1,gregorianCalendar.get(Calendar.MONTH));
		assertEquals("calendar.get(Calendar.YEAR)",2005,gregorianCalendar.get(Calendar.YEAR));
		Date date = gregorianCalendar.getTime();
		assertEquals("date", new Date(2005-1900,05-1,13), date);
		System.out.println(
		    "date for Locale '" + Locale.getDefault() + "' = '" + date +
		    "', i.e. '" + DateFormat.getInstance().format(date) + "'"
		);
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     * @throws DatatypeConfigurationException 
     */
    public void testFactoryFinder(
    ) throws DatatypeConfigurationException{
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        assertNotNull(DatatypeFactory.class.getName(), datatypeFactory);
    }
    
}
