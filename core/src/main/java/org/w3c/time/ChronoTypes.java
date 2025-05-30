/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Time Zones
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
//#define CLASSIC_CHRONO_TYPES
//#define CLASSIC_CHRONO_TYPES

//#define CLASSIC_CHRONO_TYPES
package org.w3c.time;

import org.openmdx.kernel.exception.BasicException;

import java.time.LocalDate;
import java.util.regex.Pattern;
import javax.xml.datatype.DatatypeConstants;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

public class ChronoTypes {

    /**
     * Match YYYY[...]MMDD
     */
    public static final Pattern BASIC_DATE_PATTERN = Pattern.compile("^\\d{8,}$");

    /**
     * Match YYYY[...]-MM-DD
     */
    public static final Pattern EXTENDED_DATE_PATTERN = Pattern.compile("^\\d{4,}-\\d{2}-\\d{2}$");

    public static #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else LocalDate #endif createDate(int year, int month, int dayOfMonth) {
        return #if CLASSIC_CHRONO_TYPES org.w3c.spi.DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(year, month, dayOfMonth, DatatypeConstants.FIELD_UNDEFINED)
        #else LocalDate.of(year, month, dayOfMonth)
        #endif;
    }

    public static String completeCentury(String value) throws ParseException, NumberFormatException {

        #if CLASSIC_CHRONO_TYPES
        int firstHyphen = value.indexOf('-');
        if(
                firstHyphen != 2 &&
                        (firstHyphen >= 0 || value.length() != 6)
        ) {
            return value;
        } else {
            int y2 = Integer.parseInt(value.substring(0, 2));
            int y4 = SystemClock.getInstance().today().getYear();
            int d = y2 - y4 % 100;
            int c2 = y4 / 100 + (d <= -50 ? 1 : d > 50 ? -1 : 0);
            return String.valueOf(c2) + value;
        }
        #else
        if (value.matches("\\d{2}-\\d{2}-\\d{2}")) {
            int year = Integer.parseInt(value.substring(0, 2));
            int century = (year > 50) ? 19 : 20;
            return century + value;
        }
        return value;
        #endif
    }

    public static Number getDurationField(Duration duration, DatatypeConstants.Field field) {
        if (field.equals(DatatypeConstants.YEARS)) {
            return duration.toDays() / 365;
        } else if (field.equals(DatatypeConstants.MONTHS)) {
            return (duration.toDays() % 365) / 30;
        } else if (field.equals(DatatypeConstants.DAYS)) {
            return duration.toDays() % 30;
        } else if (field.equals(DatatypeConstants.HOURS)) {
            return duration.toHours() % 24;
        } else if (field.equals(DatatypeConstants.MINUTES)) {
            return duration.toMinutes() % 60;
        } else if (field.equals(DatatypeConstants.SECONDS)) {
            return duration.toMillis() / 1000.0 % 60;
        }
        return null;
    }

    /**
     * Checks if the left instant is before the right instant.
     *
     * @param left  the first instant
     * @param right the second instant
     * @return true if the left instant is before the right instant
     */
    public static boolean isBefore(Instant left, Instant right) {
        if (left == null || right == null) {
            return left != null; // null represents all eternity
        }
        return left.isBefore(right);
    }

    /**
     * Checks if the left instant is after the right instant.
     *
     * @param left  the first instant
     * @param right the second instant
     * @return true if the left instant is after the right instant
     */
    public static boolean isAfter(Instant left, Instant right) {
        if (left == null || right == null) {
            return left != null; // null represents the dawn of time
        }
        return left.isAfter(right);
    }

    /**
     * Gets the epoch time in milliseconds from an Instant.
     *
     * @param dateTime the instant to convert
     * @return the epoch time in milliseconds
     */
    public static long getEpochMilliseconds(Instant dateTime) {
        if (dateTime == null) {
            return 0L;
        }
        return dateTime.toEpochMilli();
    }

    /**
     * Create a org::w3c::dateTime instance from epoch milliseconds
     *
     * @param epochMilliSeconds the number of milliseconds since the begin of the epoch
     * @return a org::w3c::dateTime instance initialized with the given timestamp
     */
    public static #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif ofEpochMilliseconds(long epochMilliSeconds) {
        return #if CLASSIC_CHRONO_TYPES new Date(epochMilliSeconds) #else Instant.ofEpochMilli(epochMilliSeconds) #endif;
    }

    /**
     * Checks if the left date is before the right date.
     *
     * @param left  the first date
     * @param right the second date
     * @return true if the left date is before the right date
     */
    public static boolean isBefore(Date left, Date right) {
        if (left == null || right == null) {
            return false;
        }
        return left.before(right);
    }

    /**
     * Checks if the left date is after the right date.
     *
     * @param left  the first date
     * @param right the second date
     * @return true if the left date is after the right date
     */
    public static boolean isAfter(Date left, Date right) {
        if (left == null || right == null) {
            return false;
        }
        return left.after(right);
    }

    /**
     * Gets the epoch time in milliseconds from a Date.
     *
     * @param dateTime the date to convert
     * @return the epoch time in milliseconds
     */
    public static long getEpochMilliseconds(Date dateTime) {
        if (dateTime == null) {
            return 0L;
        }
        return dateTime.getTime();
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
            return first == second;
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
    public static int compare(
        javax.xml.datatype.XMLGregorianCalendar first,
        javax.xml.datatype.XMLGregorianCalendar second
    ) {
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
     * @param nullRepresents defines how {@code null}  is interpreted
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     */
    public static int compare(
            javax.xml.datatype.XMLGregorianCalendar first,
            javax.xml.datatype.XMLGregorianCalendar second,
            NullRepresents nullRepresents
    ) {
        if(first == null || second == null) {
            return nullRepresents.compareWithNull(first, second);
        } else {
            return toComparatorReply(
                    first,
                    second,
                    comparable(first, second) ?
                            first.compare(second) :
                            makeComparable(first).compare(makeComparable(second))
            );
        }
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
            case javax.xml.datatype.DatatypeConstants.INDETERMINATE: throw new IllegalArgumentException(
                "The relationship between the two given values is indeterminate",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_QUERY_CRITERIA,
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
                )
            );
            default: throw new IllegalArgumentException(
                "Unexpected datatype compare result",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
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
                )
            );
        }
    }

    /**
     * Compare correctly even in case mutable and immutable classic chrono type arguments are mixed
     *
     * @param first               the 1st argument of the comparison
     * @param second              the 1st argument of the comparison
     * @param nullRepresents      defines how {@code null}  is interpreted
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object where {@code null} is considered less than any other value.
     * @throws NullPointerException if the specified object is null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static int compare(
            Object first,
            Object second,
            NullRepresents nullRepresents
    ) {
        if(first == null || second == null) {
            return nullRepresents.compareWithNull(first, second);
        } else if (first instanceof java.util.Date && second instanceof java.util.Date) {
            return compare((java.util.Date) first, (java.util.Date) second);
        } else if (first instanceof javax.xml.datatype.XMLGregorianCalendar && second instanceof javax.xml.datatype.XMLGregorianCalendar) {
            return compare((javax.xml.datatype.XMLGregorianCalendar) first, (javax.xml.datatype.XMLGregorianCalendar) second);
        } else if (first instanceof Comparable && second instanceof Comparable) {
            return ((Comparable)first).compareTo(second);
        } else {
            throw new IllegalArgumentException(
                "Uncomparable arguments",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("values", first, second),
                    new BasicException.Parameter("classes", first.getClass().getName(), second.getClass().getName())
                )
            );
        }
    }

    #else

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
     * @throws IllegalArgumentException in case of an indeterminate result
     */
    public static int compare(
            java.time.temporal.TemporalAmount first,
            java.time.temporal.TemporalAmount second
    ) {
        if(first instanceof java.time.Duration && second instanceof java.time.Duration) {
            final java.time.Duration duration1 = (java.time.Duration) first;
            final java.time.Duration duration2 = (java.time.Duration) second;
            return duration1.compareTo(duration2);
        } else if(first instanceof java.time.Period && second instanceof java.time.Period) {
            final java.time.Period period1 = (java.time.Period) first;
            final java.time.Period period2 = (java.time.Period) second;
            if(period1.getDays() != period2.getDays()) {
                if(period1.toTotalMonths() == period2.toTotalMonths()) {
                    return period1.getDays() - period2.getDays();
                }
                throw new IllegalArgumentException(
                    "Comparison between months and days is not (yet) supported",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        new BasicException.Parameter("amounts", first, second)
                    )
                );
            } else {
                return Long.signum(period1.toTotalMonths() - period2.toTotalMonths());
            }
        } else if(first instanceof java.time.Period && second instanceof java.time.Duration) {
            final java.time.Period period1 = (java.time.Period) first;
            final java.time.Duration duration2 = (java.time.Duration) second;
            return -compare(duration2, period1);
        } else if(first instanceof java.time.Duration && second instanceof java.time.Period) {
            final java.time.Duration duration1 = (java.time.Duration) first;
            final java.time.Period period2 = (java.time.Period) second;
            return compare(duration1, period2);
        } else {
            throw new IllegalArgumentException(
                "Unsupported temporal amount",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    new BasicException.Parameter("amounts", first, second),
                    new BasicException.Parameter("classes", first.getClass().getName(), second.getClass().getName())
                )
            );
        }
    }

    private static int compare (java.time.Duration first, java.time.Period second) {
        if(second.toTotalMonths() == 0)  {
            final java.time.Duration difference = first.minusDays(second.getDays());
            return difference.isNegative() ? -1 : difference.isZero() ? 0 : +1;
        }
        throw new IllegalArgumentException(
            "Comparison between months and days is not (yet) supported",
            BasicException.newEmbeddedExceptionStack(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter("amounts", first, second)
            )
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
            return first == second;
        }
        return compare(first, second) == 0;
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
     * Compare with specific {@code null} interpretation
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     * @param nullRepresents      defines how {@code null}  is interpreted
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     */
    public static int compare(
        java.time.LocalDate first,
        java.time.LocalDate second,
        NullRepresents nullRepresents
    ) {
        if(first == null || second == null) {
            return nullRepresents.compareWithNull(first, second);
        } else {
            return first.compareTo(second);
        }
    }

    /**
     * Compare with specific {@code null} interpretation
     *
     * @param first the 1st argument of the comparison
     * @param second the 1st argument of the comparison
     * @param nullRepresents      defines how {@code null}  is interpreted
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     */
    public static int compare(
            java.time.Instant first,
            java.time.Instant second,
            NullRepresents nullRepresents
    ) {
        if(first == null || second == null) {
            return nullRepresents.compareWithNull(first, second);
        } else {
            return first.compareTo(second);
        }
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

    /**
     * Compare correctly even in case Period and Duration are mixed
     *
     * @param first               the 1st argument of the comparison
     * @param second              the 1st argument of the comparison
     * @param nullRepresents      defines how {@code null}  is interpreted
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to,
     * or greater than the specified object where {@code null} is considered less than any other value.
     * @throws NullPointerException if the specified object is null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static int compare(
         Object first,
         Object second,
         NullRepresents nullRepresents
    ) {
        if(first == null || second == null) {
            return nullRepresents.compareWithNull(first, second);
        } else if (first instanceof java.time.temporal.TemporalAmount && second instanceof java.time.temporal.TemporalAmount) {
            return compare((java.time.temporal.TemporalAmount) first, (java.time.temporal.TemporalAmount) second);
        } else if (first instanceof Comparable && second instanceof Comparable) {
            return ((Comparable)first).compareTo(second);
        } else {
            throw new IllegalArgumentException(
                "Uncomparable arguments",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("values", first, second),
                    new BasicException.Parameter("classes", first.getClass().getName(), second.getClass().getName())
                )
            );
        }
    }

    #endif

    public enum NullRepresents {
        NEGATIVE_INFINITY {
            @Override
            int compareWithNull(Object first, Object second) {
                if(first == null) {
                    return second == null ? 0 : -1;
                } else {
                    return +1;
                }
            }
        },
        POSITIVE_INFINITY {
            @Override
            int compareWithNull(Object first, Object second) {
                if(first == null) {
                    return second == null ? 0 : +1;
                } else {
                    return -1;
                }
            }
        },
        NEGATIVE_AND_POSITIVE_INFINITY_RESPECTIVELY {
            @Override
            int compareWithNull(Object first, Object second) {
                return -1;
            }
        },
        INDETERMINATE_TERM{
            @Override
            int compareWithNull(Object first, Object second) {
                throw new IllegalArgumentException(
                    "At least one of the two arguments is null leading to an indeterminate term",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("values", first, second)
                    )
                );
            }
        },
        IGNORABLE_TERM {;
            @Override
            int compareWithNull(Object first, Object second) {
                return 0; // null is ignored
            }
        };

        abstract int compareWithNull(Object first, Object second);
    }

}
