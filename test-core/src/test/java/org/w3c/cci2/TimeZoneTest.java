/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: TestTimeZone 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007-2021, OMEX AG, Switzerland
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

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openmdx.junit5.BuildProperties;
import org.openmdx.junit5.OpenmdxTestCoreStandardExtension;
import org.openmdx.kernel.log.SysLog;

/**
 * TimeZone Test
 */
@ExtendWith(OpenmdxTestCoreStandardExtension.class)
public class TimeZoneTest {
	
	private static String format(final GregorianCalendar calendar) {
		return new StringBuilder(
		    64
		).append(
		    Integer.toString(calendar.get(GregorianCalendar.YEAR))
		).append(
		    '-'                            
		).append(
			Integer.toString(101 + calendar.get(GregorianCalendar.MONTH)).substring(1)
		).append(
		    '-'                            
		).append(
		    Integer.toString(100 + calendar.get(GregorianCalendar.DAY_OF_MONTH)).substring(1)
		).append(
		    'T'
		).append(
		    calendar.get(GregorianCalendar.HOUR_OF_DAY)
		).append(
		    ':'
		).append(
		    calendar.get(GregorianCalendar.MINUTE)
		).append(
		    ':'
		).append(
		    calendar.get(GregorianCalendar.SECOND)
		).append(
		    '.'
		).append(
		    String.valueOf(
		        1000 + calendar.get(GregorianCalendar.MILLISECOND)
		    ).substring(1)
		).append(
		    ' '
		).append(
		    TimeZone.getDefault().getID()
		).toString();
	}

	private static GregorianCalendar now() {
		final DatatypeFactory datatypeFactory = MutableDatatypeFactory.xmlDatatypeFactory();
		final GregorianCalendar calendar = datatypeFactory.newXMLGregorianCalendar(
		    new GregorianCalendar()
		).toGregorianCalendar(
		    null, // zone 
		    null, // locale 
		    null // defaults
		);
		return calendar;
	}
	
	@Test
	public void usesConfiguredTimezone() throws IOException {
		//
		// Arrange
		//
		final String configuredTimeZone = BuildProperties.getBuildProperties().getProperty(BuildProperties.TIMEZONE_KEY);
		//
		// Act
		//
		final String now = format(now());
		SysLog.info("It is now " + now);
		//
		// Assert
		//
		Assertions.assertTrue(now.endsWith(configuredTimeZone));
	}
    
}
