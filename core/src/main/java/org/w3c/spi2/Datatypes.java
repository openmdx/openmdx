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

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.util.Objects;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.spi.Parser;
import org.w3c.spi.PrimitiveTypeParsers;

/**
 * Date
 */
public class Datatypes {

    public static final Class<#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif> DATE_TIME_CLASS
            = #if CLASSIC_CHRONO_TYPES java.util.Date.class #else java.time.Instant.class #endif;

    public static final Class<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate #endif> DATE_CLASS
            = #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar.class #else java.time.LocalDate.class #endif;

    public static final Class<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.Duration #else java.time.temporal.TemporalAmount #endif> DURATION_CLASS
            = #if CLASSIC_CHRONO_TYPES javax.xml.datatype.Duration.class #else java.time.temporal.TemporalAmount.class #endif;

    public static final Class<#if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time #endif.Duration> DURATION_DAYTIME_CLASS
            = #if CLASSIC_CHRONO_TYPES javax.xml.datatype.Duration.class #else java.time.Duration.class #endif;

    public static final Class<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.Duration #else java.time.Period #endif> DURATION_YEARMONTH_CLASS
            = #if CLASSIC_CHRONO_TYPES javax.xml.datatype.Duration.class #else java.time.Period.class #endif;

    /**
     * Constructor 
     */
    private Datatypes() {
        // Avoid instantiation
    }
    
    /**
     * Create a value from its string representation 
     * 
     * @param valueClass the value's class
     * @param string the values string representation, which can be {@code null}
     * 
     * @return the value, or {@code null} if the value's string was {@code null}
     * 
     * @exception IllegalArgumentException if the string can't be parsed according to the
     * requested type
     */
    public static <V> V create(
        Class<V> valueClass,
        String string
    ){
    	final Parser primitiveTypeParser = PrimitiveTypeParsers.getExtendedParser();
    	return primitiveTypeParser.handles(valueClass) ? primitiveTypeParser.parse(
    		valueClass,
    		string
    	) : create(
    		valueClass, 
    		(Object)string
    	);
    }

    /**
     * Create a structure proxy
     * 
     * @param structureInterface the structure's interface
     * @param values the structure's members
     * 
     * @return a new structure
     */
    public static <S> S create(
        Class<S> structureInterface,
        Object... values
    ){
        return Structures.create(structureInterface, values);
    }        
    
    /**
     * Create a structure proxy
     * 
     * @param structureInterface the structure's interface
     * @param members the structure's members
     * 
     * @return
     */
    public static <S> S create(
        Class<S> structureInterface,
        Structures.Member<?>... members
    ){
        return Structures.create(structureInterface, members);
    }    

    /**
     * Associate a member name with its value
     * 
     * @param name the member's name
     * @param value the members possibly {@code null} value
     * 
     * @return a name/value pair
     */
    public static <T extends Enum<T>> Structures.Member<T> member(
        T name,
        Object value
    ){
        return new Structures.Member<T>(name, value);
    }
        
    /**
     * Create a qualified type name
     * 
     * @param components the qualified type name's components
     * 
     * @return the qualified type name
     */
    public static List<String> typeName(
        String... components
    ){
        return Collections.unmodifiableList(
            Arrays.asList(
                components
            )
        );  
    }

    #if CLASSIC_CHRONO_TYPES

    /**
     * Compare correctly even in case of different contemporary temporal amount types
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     * @throws RuntimeServiceException in case of an indeterminate result
     */
    public static int compare(javax.xml.datatype.Duration first, javax.xml.datatype.Duration second) {
        return toComparatorReply(first, second, first.compare(second));
    }

    /**
     * Compare correctly even in case of different contemporary temporal amount types
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     */
    public static boolean equals(javax.xml.datatype.Duration first, javax.xml.datatype.Duration second) {
        return first.compare(second) == javax.xml.datatype.DatatypeConstants.EQUAL;
    }

    /**
     * Compare correctly even in case mutable and immutable classic chrono type arguments are mixed
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     */
    public static int compare(java.util.Date first, java.util.Date second) {
        return comparable(first, second) ?
            first.compareTo(second) :
            makeComparable(first).compareTo(makeComparable(second));
    }

    /**
     * Compare correctly even in case mutable and immutable classic chrono type arguments are mixed
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     */
    public static boolean equals(java.util.Date first, java.util.Date second) {
        if(first == null || second == null) {
            return first == null && second == null;
        }
        return comparable(first, second) ?
            first.equals(second) :
            makeComparable(first).equals(makeComparable(second));
    }

    /**
     * Compare correctly even in case mutable and immutable classic chrono type arguments are mixed
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     */
    public static int compare(javax.xml.datatype.XMLGregorianCalendar first, javax.xml.datatype.XMLGregorianCalendar second) {
        return toComparatorReply(
            first,
            second,
            comparable(first, second) ?
                first.compare(second) :
                makeComparable(first).compare(makeComparable(second))
        );
    }

    /**
     * Compare correctly even in case mutable and immutable classic chrono type arguments are mixed
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     */
    public static boolean equals(javax.xml.datatype.XMLGregorianCalendar first, javax.xml.datatype.XMLGregorianCalendar second) {
        if(first == null || second == null) {
            return first == null && second == null;
        }
        return comparable(first, second) ?
            first.equals(second) :
            makeComparable(first).equals(makeComparable(second));
    }

    private static <T> boolean comparable(T first, T second){
        return first instanceof org.w3c.cci2.ImmutableDatatype<?> == second instanceof org.w3c.cci2.ImmutableDatatype<?>;
    }

    private static javax.xml.datatype.XMLGregorianCalendar makeComparable(javax.xml.datatype.XMLGregorianCalendar value) {
        return org.w3c.spi.DatatypeFactories.immutableDatatypeFactory().toImmutableDate(value);
    }

    private static java.util.Date makeComparable(java.util.Date value) {
        return org.w3c.spi.DatatypeFactories.immutableDatatypeFactory().toImmutableDateTime(value);
    }

    /**
     * Convert result to either a comparator return value or an exception
     *
     * @param first the first value to be compared
     * @param second the second value to be compared
     * @param datatypeResult the data type comparison result value
     *
     * @return the comparator return value
     */
    private static int toComparatorReply(
        Object first,
        Object second,
        int datatypeResult
    ){
        switch (datatypeResult) {
            case javax.xml.datatype.DatatypeConstants.LESSER: return -1;
            case javax.xml.datatype.DatatypeConstants.EQUAL: return 0;
            case javax.xml.datatype.DatatypeConstants.GREATER: return 1;
            case javax.xml.datatype.DatatypeConstants.INDETERMINATE: throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_QUERY_CRITERIA,
                "The relationship between the two given values is indeterminate",
                new BasicException.Parameter(
                    "values",
                    first,
                    second
                ),
                new BasicException.Parameter(
                    "class",
                    first.getClass().getName(),
                    second.getClass().getName()
                )
            );
            default: throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Unexpected datatype compare result",
                new BasicException.Parameter(
                    "values",
                    first,
                    second
                ),
                new BasicException.Parameter(
                    "class",
                    first.getClass().getName(),
                    second.getClass().getName()
                ),
                new BasicException.Parameter(
                    "result",
                    datatypeResult
                )
            );
        }
    }

    #else

    /**
     * Compare correctly even in case of different contemporary temporal amount types
     *
     * @param temporalAmount1 the 1st argument of the comparison
     * @param temporalAmount2 the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     * @throws RuntimeServiceException in case of an indeterminate result
     */
    public static int compare(
        java.time.temporal.TemporalAmount temporalAmount1,
        java.time.temporal.TemporalAmount temporalAmount2
    ) {
        if(temporalAmount1 instanceof java.time.Duration && temporalAmount2 instanceof java.time.Duration) {
            final java.time.Duration duration1 = (java.time.Duration) temporalAmount1;
            final java.time.Duration duration2 = (java.time.Duration) temporalAmount2;
            return duration1.compareTo(duration2);
        } else if(temporalAmount1 instanceof java.time.Period && temporalAmount2 instanceof java.time.Period) {
            final java.time.Period period1 = (java.time.Period) temporalAmount1;
            final java.time.Period period2 = (java.time.Period) temporalAmount2;
            if(period1.getDays() != period2.getDays()) {
                if(period1.toTotalMonths() == period2.toTotalMonths()) {
                    return period1.getDays() - period2.getDays();
                }
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Comparison between months and days is not (yet) supported",
                    new BasicException.Parameter("amounts", temporalAmount1, temporalAmount2)
                );
            } else {
                return Long.signum(period1.toTotalMonths() - period2.toTotalMonths());
            }
        } else if(temporalAmount1 instanceof java.time.Period && temporalAmount2 instanceof java.time.Duration) {
            final java.time.Period period1 = (java.time.Period) temporalAmount1;
            final java.time.Duration duration2 = (java.time.Duration) temporalAmount2;
            return -compare(duration2, period1);
        } else if(temporalAmount1 instanceof java.time.Duration && temporalAmount2 instanceof java.time.Period) {
            final java.time.Duration duration1 = (java.time.Duration) temporalAmount1;
            final java.time.Period period2 = (java.time.Period) temporalAmount2;
            return compare(duration1, period2);
        } else {
            throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Unsupported temporal amount",
                new BasicException.Parameter("amounts", temporalAmount1, temporalAmount2),
                new BasicException.Parameter("classes", temporalAmount1.getClass().getName(), temporalAmount2.getClass().getName())
            );
        }
    }

    private static int compare (java.time.Duration temporalAmount1, java.time.Period temporalAmount2) {
        if(temporalAmount2.toTotalMonths() == 0)  {
            final java.time.Duration difference = temporalAmount1.minusDays(temporalAmount2.getDays());
            return difference.isNegative() ? -1 : difference.isZero() ? 0 : +1;
        }
        throw new RuntimeServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "Comparison between months and days is not (yet) supported",
            new BasicException.Parameter("amounts", temporalAmount1, temporalAmount2)
        );
    }

    /**
     * Compare correctly even in case of different contemporary temporal amount types
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     */
    public static boolean equals(java.time.temporal.TemporalAmount first, java.time.temporal.TemporalAmount second) {
        if(first == null || second == null) {
            return first == null && second == null;
        }
        return compare(first, second) == 0;
    }

    /**
     * Create a date-time instance
     *
     * @param value an internal representation
     *
     * @return a corresponding date-time instance
     *
     * @exception IllegalArgumentException
     * if the value is not an org::w3c::dateTime instance
     */
    public static java.time.Instant toImmutableDateTime(
        java.time.Instant value
    ){
        return value;
    }

    /**
     * Create a date instance
     *
     * @param value an internal representation
     *
     * @return a corresponding date-time instance
     *
     * @exception IllegalArgumentException
     * if the value is not an org::w3c::date instance
     */
    public static java.time.LocalDate toImmutableDate(
        java.time.LocalDate value
    ){
        return value;
    }

    /**
     * Compare correctly even in case mutable and immutable classic chrono type arguments are mixed
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     */
    public static int compare(java.time.Instant first, java.time.Instant second) {
        return first.compareTo(second);
    }

    /**
     * Compare correctly even in case mutable and immutable classic chrono type arguments are mixed
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     */
    public static boolean equals(java.time.Instant first, java.time.Instant second) {
        return Objects.equals(first, second);
    }

    /**
     * Compare correctly even in case mutable and immutable classic chrono type arguments are mixed
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     */
    public static int compare(java.time.LocalDate first, java.time.LocalDate second) {
        return first.compareTo(second);
    }

    /**
     * Compare correctly even in case mutable and immutable classic chrono type arguments are mixed
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     */
    public static boolean equals(java.time.LocalDate first, java.time.LocalDate second) {
        return Objects.equals(first, second);
    }

    #endif

}
