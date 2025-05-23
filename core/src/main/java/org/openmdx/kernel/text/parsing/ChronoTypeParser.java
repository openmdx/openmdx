/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Immutable Primitive Type Parser 
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
package org.openmdx.kernel.text.parsing;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.time.Instant;
import java.time.Period;
import java.time.LocalDate;
import java.time.temporal.TemporalAmount;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeConstants;
import org.openmdx.kernel.text.parsing.AbstractParser;
import org.openmdx.kernel.text.spi.Parser;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi2.Datatypes;

/**
 * Immutable Primitive Type Parser
 */
public class ChronoTypeParser extends AbstractParser {

    /**
     * Constructor 
     */
    private ChronoTypeParser() {
        super();
    }

    /**
     * An instance
     */
    private static final Parser INSTANCE = new ChronoTypeParser();

    /**
     * The supported types
     */
    private static final Collection<Class<?>> SUPPORTED_TYPES = Arrays.asList(
        // org::w3c::date
        LocalDate.class,
        XMLGregorianCalendar.class,
        // org::w3c::dateTime
        Instant.class,
        Date.class,
        // org::w3c::duration
        TemporalAmount.class,
        javax.xml.datatype.Duration.class,
        // org::w3c::durationDayTime
        java.time.Duration.class,
        // org::w3c::durationYearMonth
        Period.class
    );
    
    /**
     * Retrieve an instance
     * 
     * @return an instance
     */
    public static Parser getInstance(){
        return INSTANCE;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.parsing.AbstractParser#supportedTypes()
     */
    @Override
    protected Collection<Class<?>> supportedTypes() {
        return SUPPORTED_TYPES;
    }

    /* (non-Javadoc)
	 * @see org.openmdx.kernel.text.parse.AbstractParser#parseAs(java.lang.Class, java.lang.String)
	 */
	@Override
	protected Object parseAs(
		String externalRepresentation,
		Class<?> valueClass
	) throws Exception {
		return
            valueClass == Datatypes.DATE_CLASS ? DatatypeFactories.immutableDatatypeFactory().newDate(externalRepresentation) :
            valueClass == Datatypes.DATE_TIME_CLASS ? DatatypeFactories.immutableDatatypeFactory().newDateTime(externalRepresentation) :
            valueClass == Datatypes.DURATION_CLASS ? DatatypeFactories.immutableDatatypeFactory().newDuration(externalRepresentation) :
            #if CLASSIC_CHRONO_TYPES
            valueClass == java.time.Duration.class ? java.time.Duration.parse(externalRepresentation) :
            valueClass == Period.class ? Period.parse(externalRepresentation) :
            valueClass == TemporalAmount.class ? toContemporaryDuration(externalRepresentation) :
            valueClass == LocalDate.class ? toContemporaryDate(externalRepresentation) :
            valueClass == Instant.class ? toContemporaryDateTime(externalRepresentation) :
            #else
            valueClass == Datatypes.DURATION_DAYTIME_CLASS ? DatatypeFactories.immutableDatatypeFactory().newDurationDayTime(externalRepresentation) :
            valueClass == Datatypes.DURATION_YEARMONTH_CLASS ? DatatypeFactories.immutableDatatypeFactory().newDurationYearMonth(externalRepresentation) :
            valueClass == javax.xml.datatype.Duration.class ? toClassicDuration(externalRepresentation) :
            valueClass == javax.xml.datatype.XMLGregorianCalendar.class ? toClassicDate(externalRepresentation) :
            valueClass == java.util.Date.class ? toClassicDateTime(externalRepresentation) :
            #endif
            super.parseAs(externalRepresentation, valueClass);
	}

    #if CLASSIC_CHRONO_TYPES

    /**
     * Create a contemporary org::w3c::duration instance
     *
     * @param externalRepresentation the standard representation
     *
     * @return a corresponding org::w3c::duration instance
     */
    private TemporalAmount toContemporaryDuration(
        String externalRepresentation
    ) throws ParseException {
        final javax.xml.datatype.Duration duration = DatatypeFactories.xmlDatatypeFactory().newDuration(externalRepresentation);
        if(duration.getYears() != 0 || duration.getMonths() != 0) {
            if(
                duration.getHours() == 0 &&
                duration.getMinutes() == 0 &&
                duration.getSeconds() == 0
            ){
                return Period.of(
                    duration.getSign() * duration.getYears(),
                    duration.getSign() * duration.getMonths(),
                    duration.getSign() * duration.getDays()
                );
            } else {
                throw new ParseException(
                    "Time can't be mixed with years or months",
                    0
                );
            }
        } else {
            return java.time.Duration.parse(externalRepresentation);
        }
    }

    /**
     * Create contemporary org::w3c::dateTime instance
     *
     * @param externalRepresentation the basic or extended representation
     *
     * @return a corresponding org::w3c::dateTime instance
     */
    private Instant toContemporaryDateTime(
        String externalRepresentation
    ){
        return externalRepresentation == null ?
            null :
            DatatypeFactories.immutableDatatypeFactory().newDateTime(externalRepresentation).toInstant();
    }

    /**
     * Create a contemporary org::w3c::date instance
     *
     * @param externalRepresentation the basic or extended representation
     *
     * @return a corresponding org::w3c::date instance
     */
    private LocalDate toContemporaryDate(
        String externalRepresentation
    ){

        if(externalRepresentation == null) {
            return null;
        }
        final javax.xml.datatype.XMLGregorianCalendar date = DatatypeFactories.immutableDatatypeFactory().newDate(externalRepresentation);
        return LocalDate.of(
            date.getYear(),
            date.getMonth(),
            date.getDay()
        );
    }

    #else

    /**
     * Create a classic org::w3c::duration instance
     *
     * @param externalRepresentation the standard representation
     *
     * @return a corresponding org::w3c::duration instance
     */
    private javax.xml.datatype.Duration toClassicDuration(
        String externalRepresentation
    ){
        return externalRepresentation == null ?
            null :
            DatatypeFactories.xmlDatatypeFactory().newDuration(externalRepresentation);
    }

    /**
     * Create an UTC based classic org::w3c::dateTime instance
     *
     * @param externalRepresentation the basic or extended representation
     *
     * @return a corresponding org::w3c::dateTime instance
     */
    private java.util.Date toClassicDateTime(
        String externalRepresentation
    ){
        return externalRepresentation == null ? null : java.util.Date.from(
            DatatypeFactories.immutableDatatypeFactory().newDateTime(externalRepresentation)
        );
    }

    /**
     * Create a classic org::w3c::date instance
     *
     * @param externalRepresentation the basic or extended representation
     *
     * @return a corresponding classic org::w3c::date instance
     */
    private XMLGregorianCalendar toClassicDate(
        String externalRepresentation
    ){
        if(externalRepresentation == null) {
            return null;
        }
        final LocalDate date = DatatypeFactories.immutableDatatypeFactory().newDate(externalRepresentation);
        return DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
            date.getYear(),
            date.getMonthValue(),
            date.getDayOfMonth(),
            DatatypeConstants.FIELD_UNDEFINED
        );
    }

    #endif

}
