/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Date
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
package org.w3c.spi2;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAmount;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class DatatypesTest {

    @Test
    void incompleteExtendedFormatClassicDate(){
        //
        // Arrange
        //
        String external = "25-04-30";
        //
        // Act
        //
        XMLGregorianCalendar internal = Datatypes.create(XMLGregorianCalendar.class, external);
        //
        // Assert
        //
        Assertions.assertEquals("2025-04-30", internal.toString());
    }

    @Test
    void incompleteBasicFormatClassicDate(){
        //
        // Arrange
        //
        String external = "250430";
        //
        // Act
        //
        XMLGregorianCalendar internal = Datatypes.create(XMLGregorianCalendar.class, external);
        //
        // Assert
        //
        Assertions.assertEquals("2025-04-30", internal.toString());
    }

    @Test
    void basicFormatClassicDate(){
        //
        // Arrange
        //
        String external = "20000430";
        //
        // Act
        //
        XMLGregorianCalendar internal = Datatypes.create(XMLGregorianCalendar.class, external);
        //
        // Assert
        //
        Assertions.assertEquals("2000-04-30", internal.toString());
    }

    @Test
    void extendedFormatClassicDate(){
        //
        // Arrange
        //
        String external = "2000-04-30";
        //
        // Act
        //
        XMLGregorianCalendar internal = Datatypes.create(XMLGregorianCalendar.class, external);
        //
        // Assert
        //
        Assertions.assertEquals("2000-04-30", internal.toString());
    }

    @Test
    void basicFormatClassicDateTime(){
        //
        // Arrange
        //
        String external = "20071203T101530.000Z";
        //
        // Act
        //
        Date internal = Datatypes.create(Date.class, external);
        //
        // Assert
        //
        Assertions.assertEquals(1196676930000L, internal.getTime());
    }

    @Test
    void extendedFormatClassicDateTime(){
        //
        // Arrange
        //
        String external = "2007-12-03T10:15:30.000Z";
        //
        // Act
        //
        Date internal = Datatypes.create(Date.class, external);
        //
        // Assert
        //
        Assertions.assertEquals(1196676930000L, internal.getTime());
    }

    @Test
    void classicYearMonthDuration() {
        //
        // Arrange
        //
        String external = "P1Y2M";
        //
        // Act
        //
        javax.xml.datatype.Duration internal = Datatypes.create(javax.xml.datatype.Duration.class, external);
        //
        // Assert
        //
        Assertions.assertEquals(external, internal.toString());
    }

    @Test
    void classicYearMonthDayDuration() {
        //
        // Arrange
        //
        String external = "P1Y2M1D";
        //
        // Act
        //
        javax.xml.datatype.Duration internal = Datatypes.create(javax.xml.datatype.Duration.class, external);
        //
        // Assert
        //
        Assertions.assertEquals(external, internal.toString());
    }

    @Test
    void classicDayTimeDuration() {
        //
        // Arrange
        //
        String external = "P1DT2H3M";
        //
        // Act
        //
        javax.xml.datatype.Duration internal = Datatypes.create(javax.xml.datatype.Duration.class, external);
        //
        // Assert
        //
        Assertions.assertEquals(external, internal.toString());
    }

    @Test
    void contemporaryYearMonthDayDuration() {
        //
        // Arrange
        //
        String external = "P1Y2M1D";
        //
        // Act
        //
        Period internal = Datatypes.create(Period.class, external);
        //
        // Assert
        //
        Assertions.assertEquals(external, internal.toString());
    }

    @Test
    void contemporaryDayTimeDuration() {
        //
        // Arrange
        //
        String external = "P1DT2H3M";
        //
        // Act
        //
        java.time.Duration internal = Datatypes.create(java.time.Duration.class, external);
        //
        // Assert
        //
        Assertions.assertEquals("PT26H3M", internal.toString());
    }

    @Test
    void contemporaryYearMonthDuration() {
        //
        // Arrange
        //
        String external = "P1Y2M";
        //
        // Act
        //
        Period internal = Datatypes.create(Period.class, external);
        //
        // Assert
        //
        Assertions.assertEquals(external, internal.toString());
    }

    @Test
    void contemporaryYearMonthDayAmount() {
        //
        // Arrange
        //
        String external = "P1Y2M1D";
        //
        // Act
        //
        TemporalAmount internal = Datatypes.create(TemporalAmount.class, external);
        //
        // Assert
        //
        Assertions.assertEquals(external, internal.toString());
    }

    @Test
    void contemporaryDayTimeAmount() {
        //
        // Arrange
        //
        String external = "P1DT2H3M";
        //
        // Act
        //
        TemporalAmount internal = Datatypes.create(TemporalAmount.class, external);
        //
        // Assert
        //
        Assertions.assertEquals("PT26H3M", internal.toString());
    }

    @Test
    void contemporaryYearMonthAmount() {
        //
        // Arrange
        //
        String external = "P1Y2M";
        //
        // Act
        //
        TemporalAmount internal = Datatypes.create(TemporalAmount.class, external);
        //
        // Assert
        //
        Assertions.assertEquals(external, internal.toString());
    }

    @Test
    void incompleteExtendedFormatContemporaryDate(){
        //
        // Arrange
        //
        String external = "25-04-30";
        //
        // Act
        //
        LocalDate internal = Datatypes.create(LocalDate.class, external);
        //
        // Assert
        //
        Assertions.assertEquals("2025-04-30", internal.toString());
    }

    @Test
    void incompleteBasicFormatContemporaryDate(){
        //
        // Arrange
        //
        String external = "250430";
        //
        // Act
        //
        LocalDate internal = Datatypes.create(LocalDate.class, external);
        //
        // Assert
        //
        Assertions.assertEquals("2025-04-30", internal.toString());
    }

    @Test
    void basicFormatContemporaryDate(){
        //
        // Arrange
        //
        String external = "20000430";
        //
        // Act
        //
        LocalDate internal = Datatypes.create(LocalDate.class, external);
        //
        // Assert
        //
        Assertions.assertEquals("2000-04-30", internal.toString());
    }

    @Test
    void extendedFormatContemporaryDate(){
        //
        // Arrange
        //
        String external = "2000-04-30";
        //
        // Act
        //
        LocalDate internal = Datatypes.create(LocalDate.class, external);
        //
        // Assert
        //
        Assertions.assertEquals("2000-04-30", internal.toString());
    }

    @Test
    void basicFormatContemporaryDateTime(){
        //
        // Arrange
        //
        String external = "20071203T101530.000Z";
        //
        // Act
        //
        Instant internal = Datatypes.create(Instant.class, external);
        //
        // Assert
        //
        Assertions.assertEquals(1196676930000L, internal.toEpochMilli());
    }

    @Test
    void extendedFormatContemporaryDateTime(){
        //
        // Arrange
        //
        String external = "2007-12-03T10:15:30.000Z";
        //
        // Act
        //
        Instant internal = Datatypes.create(Instant.class, external);
        //
        // Assert
        //
        Assertions.assertEquals(1196676930000L, internal.toEpochMilli());
    }

}
