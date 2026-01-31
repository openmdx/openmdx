/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: ValidTimes 
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

package org.openmdx.state2.spi;

import org.openmdx.kernel.exception.BasicException;
import org.w3c.time.ChronoTypes;

#if CLASSIC_CHRONO_TYPES
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import org.w3c.spi.DatatypeFactories;
import javax.xml.datatype.Duration;
#else
import java.time.Period;
import java.time.LocalDate;
import java.time.Instant;
#endif

/**
 * Valid Times
 */
public class Order {

    /**
     * Constructor 
     */
    private Order() {
        // Avoid instantiation
    }

    /**
     * Plus one day
     */
    #if CLASSIC_CHRONO_TYPES
    private static final Duration ONE_DAY = DatatypeFactories
        .xmlDatatypeFactory()
        .newDurationDayTime(
            true, // isPositive
            1, // day
            0, // hour
            0, // minute
            0 // second
        );
    #else
    private static final Period ONE_DAY = Period.ofDays(1);
    #endif

    /**
     * Minus one day
     */
    #if CLASSIC_CHRONO_TYPES
    private static final Duration MINUS_ONE_DAY = DatatypeFactories
        .xmlDatatypeFactory()
        .newDurationDayTime(
            false, // isPositive
            1, // day
            0, // hour
            0, // minute
            0 // second
        );
    #else
    private static final Period MINUS_ONE_DAY = Period.ofDays(-1);
    #endif

    //------------------------------------------------------------------------
    // Date States
    //------------------------------------------------------------------------
    
    /**
     * Tests whether validTo is greater than or equal to validFrom
     * 
     * @param validFrom the first value
     * @param validTo the second value
     * 
     * @throws IllegalArgumentException if validTo is less than validFrom 
     */
    public static void assertTimeRange(
        #if CLASSIC_CHRONO_TYPES XMLGregorianCalendar #else LocalDate #endif validFrom,
        #if CLASSIC_CHRONO_TYPES XMLGregorianCalendar #else LocalDate #endif validTo
    ){
        if(
            ChronoTypes.compare(
                validFrom,
                validTo,
                ChronoTypes.NullRepresents.NEGATIVE_AND_POSITIVE_INFINITY_RESPECTIVELY
            ) > 0
        ) throw BasicException.initHolder(
            new IllegalArgumentException(
                "validTo must be greater than or equal to validFrom",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("validFrom", validFrom),
                    new BasicException.Parameter("validTo", validTo)
                )
            )
        );
    }
    
    /**
     * Compare two org::w3c::date values where {@code null} is
     * considered to be smaller than every other value.
     * 
     * @param d1 the first value
     * @param d2 the second value
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareValidFrom(
        #if CLASSIC_CHRONO_TYPES XMLGregorianCalendar #else LocalDate #endif d1,
        #if CLASSIC_CHRONO_TYPES XMLGregorianCalendar #else LocalDate #endif d2
    ){
        return ChronoTypes.compare(d1, d2, ChronoTypes.NullRepresents.NEGATIVE_INFINITY);
    }

    /**
     * Compare two org::w3c::date values where {@code null} is
     * considered to be greater than every other value.
     * 
     * @param d1 the first value
     * @param d2 the second value
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareValidTo(
        #if CLASSIC_CHRONO_TYPES XMLGregorianCalendar #else LocalDate #endif d1,
        #if CLASSIC_CHRONO_TYPES XMLGregorianCalendar #else LocalDate #endif d2
    ){
        return ChronoTypes.compare(d1, d2, ChronoTypes.NullRepresents.POSITIVE_INFINITY);
    }

    /**
     * Compare two org::w3c::date values where {@code null} is
     * considered to be lesser than every other value for {@code from}
     * and greater than every other value for {@code to}.
     * 
     * @param from the first value
     * @param to the second value
     * 
     * @return a negative integer, zero, or a positive integer as {@code from} 
     * is less than, equal to, or greater than {@code to}. 
     */
    public static int compareValidFromToValidTo(
        #if CLASSIC_CHRONO_TYPES XMLGregorianCalendar #else LocalDate #endif from,
        #if CLASSIC_CHRONO_TYPES XMLGregorianCalendar #else LocalDate #endif to
    ){
        return ChronoTypes.compare(from, to, ChronoTypes.NullRepresents.NEGATIVE_AND_POSITIVE_INFINITY_RESPECTIVELY);
    }

    //------------------------------------------------------------------------
    // Date-Time States
    //------------------------------------------------------------------------
    
    /**
     * Tests whether invalidFrom is greater than validFrom
     * 
     * @param validFrom the first org::w3c::dateTime value
     * @param invalidFrom the second org::w3c::dateTime value
     * 
     * @throws IllegalArgumentException if invalidFrom is less than or equal to validFrom 
     */
    public static void assertTimeRange(
        #if CLASSIC_CHRONO_TYPES Date #else Instant #endif validFrom,
        #if CLASSIC_CHRONO_TYPES Date #else Instant #endif invalidFrom
    ){
        if(
            ChronoTypes.compare(
                validFrom,
                invalidFrom,
                ChronoTypes.NullRepresents.NEGATIVE_AND_POSITIVE_INFINITY_RESPECTIVELY
            ) >= 0
        ) throw BasicException.initHolder(
            new IllegalArgumentException(
                "invalidFrom must be greater than validFrom",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("validFrom", validFrom),
                    new BasicException.Parameter("invalidFrom", invalidFrom)
                )
            )
        );
    }
    
    /**
     * Compare two Date values where {@code null} is
     * considered to be smaller than every other value.
     * 
     * @param d1 the first value
     * @param d2 the second value
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareValidFrom(
        #if CLASSIC_CHRONO_TYPES Date #else Instant #endif d1,
        #if CLASSIC_CHRONO_TYPES Date #else Instant #endif d2
    ){
        return ChronoTypes.compare(d1, d2, ChronoTypes.NullRepresents.NEGATIVE_INFINITY);
    }

    /**
     * Compare two Date values where {@code null} is
     * considered to be greater than every other value.
     * 
     * @param d1 the first value
     * @param d2 the second value
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareInvalidFrom(
        #if CLASSIC_CHRONO_TYPES Date #else Instant #endif d1,
        #if CLASSIC_CHRONO_TYPES Date #else Instant #endif d2
    ){
        return ChronoTypes.compare(d1, d2, ChronoTypes.NullRepresents.POSITIVE_INFINITY);
    }

    /**
     * Compare two Date values where {@code null} is
     * considered to be lesser than every other value for {@code from}
     * and greater than every other value for {@code to}.
     * 
     * @param from the first value
     * @param to the second value
     * 
     * @return a negative integer, zero, or a positive integer as {@code from} 
     * is less than, equal to, or greater than {@code to}. 
     */
    public static int compareValidFromToValidTo(
        #if CLASSIC_CHRONO_TYPES Date #else Instant #endif from,
        #if CLASSIC_CHRONO_TYPES Date #else Instant #endif to
    ){
        return ChronoTypes.compare(from, to, ChronoTypes.NullRepresents.NEGATIVE_AND_POSITIVE_INFINITY_RESPECTIVELY);
    }
    
    
    //------------------------------------------------------------------------
    // Existence
    //------------------------------------------------------------------------

    /**
     * Compare two Date values where {@code null} is
     * considered to be greater than every other value.
     * 
     * @param d1 the first value
     * @param d2 the second value
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareRemovedAt(
        #if CLASSIC_CHRONO_TYPES Date #else Instant #endif d1,
        #if CLASSIC_CHRONO_TYPES Date #else Instant #endif d2
    ){
        return ChronoTypes.compare(d1, d2, ChronoTypes.NullRepresents.POSITIVE_INFINITY);
    }
    
    
    //------------------------------------------------------------------------
    // Adjacence
    //------------------------------------------------------------------------
    
    /**
     * Retrieve the next day
     *
     * @param date an org::w3c::date value
     *
     * @return the next day
     */
    #if CLASSIC_CHRONO_TYPES
    public static XMLGregorianCalendar successor(
        XMLGregorianCalendar date
    ){
        XMLGregorianCalendar successor = (XMLGregorianCalendar) date.clone();
        successor.add(ONE_DAY);
        return DatatypeFactories.immutableDatatypeFactory().toImmutableDate(successor);
    }
    #else
    public static LocalDate successor(
        LocalDate date
    ){
        return date.plus(ONE_DAY);
    }
    #endif

    /**
     * Retrieve the previous day
     *
     * @param date an org::w3c::date value
     *
     * @return the previous day
     */
    #if CLASSIC_CHRONO_TYPES
    public static XMLGregorianCalendar predecessor(
        XMLGregorianCalendar date
    ){
        XMLGregorianCalendar successor = (XMLGregorianCalendar) date.clone();
        successor.add(MINUS_ONE_DAY);
        return DatatypeFactories.immutableDatatypeFactory().toImmutableDate(successor);
    }
    #else
    public static LocalDate predecessor(
        LocalDate date
    ){
        return date.plus(MINUS_ONE_DAY);
    }
    #endif

}
