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



import javax.xml.datatype.DatatypeConstants;
import #if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time #endif.Duration;

import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.spi.DatatypeFactories;

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
    public static final Duration ONE_DAY = DatatypeFactories.xmlDatatypeFactory(
    ).newDurationDayTime(
        true, // isPositive
        1, // day
        0, // hour
        0, // minute
        0 // second
    );
    /**
     * Minus one day
     */
    public static final Duration MINUS_ONE_DAY = DatatypeFactories.xmlDatatypeFactory(
    ).newDurationDayTime(
        false, // isPositive
        1, // day
        0, // hour
        0, // minute
        0 // second
    );


    //------------------------------------------------------------------------
    // Date States
    //------------------------------------------------------------------------
    
    /**
     * Tests whether validTo is greater than or equal to validFrom
     * 
     * @param validFrom
     * @param validTo
     * 
     * @throws IllegalArgumentException if validTo is less than validFrom 
     */
    public static void assertTimeRange(
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif validFrom,
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif validTo
    ){
        if(validFrom != null && validTo != null) {
            if(validTo.compare(validFrom) == DatatypeConstants.LESSER) {
                throw BasicException.initHolder(
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
        }
    }
    
    /**
     * Compare two XMLGregorianCalendar values where {@code null} is
     * considered to be smaller than every other value.
     * 
     * @param d1 the first value
     * @param d2 the second value
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareValidFrom(
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif d1,
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : -1
        ) : (
            d2 == null ? 1 : compare(d1,d2)
        );
    }

    /**
     * Compare two XMLGregorianCalendar values where {@code null} is
     * considered to be greater than every other value.
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareValidTo(
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif d1,
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : 1
        ) : (
            d2 == null ? -1 : compare(d1,d2)
        );
    }

    /**
     * Compare two XMLGregorianCalendar values where {@code null} is
     * considered to be lesser than every other value for {@code from}
     * and greater than every other value for {@code to}.
     * 
     * @param from
     * @param to
     * 
     * @return a negative integer, zero, or a positive integer as {@code from} 
     * is less than, equal to, or greater than {@code to}. 
     */
    public static int compareValidFromToValidTo(
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif from,
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif to
    ){
        return from == null || to == null ? -1 : compare(from,to);
    }

    /**
     * Compare two (mutable or immutable) {@code #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif} values
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as {@code from} 
     * is less than, equal to, or greater than {@code to}. 
     */
    public static int compare(
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif d1,
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif d2
    ){
        boolean i1 = d1 instanceof ImmutableDatatype<?>;
        boolean i2 = d2 instanceof ImmutableDatatype<?>;
        return 
            i1 == i2 ? d1.compare(d2) :
            i1 ? ((#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif)d1.clone()).compare(d2) :
            d1.compare((#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) d2.clone());
    }
        
    /**
     * Compare two (mutable or immutable) {@code XMLGregorianCalendar} values
     * 
     * @param d1
     * @param d2
     * 
     * @return true if the two values are either equal or both {@code null}
     */
    public static boolean equal(
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif d1,
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif d2
    ){
        boolean i1 = d1 instanceof ImmutableDatatype<?>;
        boolean i2 = d2 instanceof ImmutableDatatype<?>;
        return 
            d1 == d2 ? true :
            d1 == null || d2 == null ? false :
            i1 == i2 ? d1.equals(d2) :
            i1 ? d1.clone().equals(d2) :
            d1.equals(d2.clone());
    }
    
    //------------------------------------------------------------------------
    // Date-Time States
    //------------------------------------------------------------------------
    
    /**
     * Tests whether invalidFrom is greater than validFrom
     * 
     * @param validFrom
     * @param invalidFrom
     * 
     * @throws IllegalArgumentException if invalidFrom is less than or equal to validFrom 
     */
    public static void assertTimeRange(
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif validFrom,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif invalidFrom
    ){
        if(validFrom != null && invalidFrom != null) {
            if(invalidFrom.compareTo(validFrom) <= 0) {
                throw BasicException.initHolder(
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
        }
    }
    
    /**
     * Compare two Date values where {@code null} is
     * considered to be smaller than every other value.
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareValidFrom(
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif d1,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : -1
        ) : (
            d2 == null ? 1 : d1.compareTo(d2)
        );
    }

    /**
     * Compare two Date values where {@code null} is
     * considered to be greater than every other value.
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareInvalidFrom(
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif d1,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : 1
        ) : (
            d2 == null ? -1 : d1.compareTo(d2)
        );
    }

    /**
     * Compare two Date values where {@code null} is
     * considered to be lesser than every other value for {@code from}
     * and greater than every other value for {@code to}.
     * 
     * @param from
     * @param to
     * 
     * @return a negative integer, zero, or a positive integer as {@code from} 
     * is less than, equal to, or greater than {@code to}. 
     */
    public static int compareValidFromToValidTo(
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif from,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif to
    ){
        return from == null || to == null ? -1 : from.compareTo(to);
    }
    
    
    //------------------------------------------------------------------------
    // Existence
    //------------------------------------------------------------------------

    /**
     * Compare two Date values where {@code null} is
     * considered to be greater than every other value.
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareRemovedAt(
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif d1,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : 1
        ) : (
            d2 == null ? -1 : d1.compareTo(d2)
        );
    }
    
    
    //------------------------------------------------------------------------
    // Adjacence
    //------------------------------------------------------------------------
    
    /**
     * Retrieve the previous day
     * 
     * @param date
     * 
     * @return the previous day
     */
    public static #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif predecessor(
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif date
    ){
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif predecessor = (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) date.clone();
        predecessor.add(MINUS_ONE_DAY);
        return predecessor;
    }

    /**
     * Retrieve the next day
     * 
     * @param date
     * 
     * @return the next day
     */
    public static #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif successor(
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif date
    ){
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif successor = (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) date.clone();
        successor.add(ONE_DAY);
        return successor;
    }

}
