/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Immutable Datatype Factory
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
package org.w3c.spi;

import java.time.temporal.TemporalAmount;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

class StandardChronoTypeFactory extends AbstractChronoTypeFactory {

    /**
     * Constructor
     */
    private StandardChronoTypeFactory(){
        super();
    }

    static final ChronoTypeFactory INSTANCE = new StandardChronoTypeFactory();

    @Override
    public Instant newDateTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            // Handle the basic format: YYYYMMDDTHHmmss.SSSZ
            String transformed = value.substring(0, 4) + "-" +
                    value.substring(4, 6) + "-" +
                    value.substring(6, 8) + "T" +
                    value.substring(9, 11) + ":" +
                    value.substring(11, 13) + ":" +
                    value.substring(13);

            // Handle timezone offset without colon if present
            if (transformed.length() > 19) {  // Has timezone part
                int tzPos = transformed.length() - 5;
                if (transformed.charAt(tzPos) == '+' || transformed.charAt(tzPos) == '-') {
                    String prefix = transformed.substring(0, tzPos + 3);
                    String suffix = transformed.substring(tzPos + 3);
                    transformed = prefix + ":" + suffix;
                }
            }
            return Instant.parse(transformed);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid datetime format: " + value, e);
        }
    }

    @Override
    public LocalDate newDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + value, e);
        }
    }

    @Override
    public Period newDurationYearMonth(String value) {
        return newDurationYearMonthDay(value);
    }

    @Override
    protected Period newDurationYearMonthDay(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Period.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid period format: " + value, e);
        }
    }

    @Override
    protected TemporalAmount newDurationYearMonthDayTime(String value) {
        if (value == null) {
            return null;
        }
        throw new UnsupportedOperationException(
            "Combination of time with year is not supported in openMDX 3 and openMDX 5: " + value
        );
    }

    @Override
    public Duration newDurationDayTime(String value) {
        if (value == null) {
            return null;
        }
        try {
            // First try standard Duration parsing (for PT... formats)
            return Duration.parse(value);
        } catch (DateTimeParseException e) {
            // For period-based formats (P#Y, P#M, etc.), convert to an approximate duration
            try {
                if (value.startsWith("P") && (value.contains("Y") || value.contains("M"))) {
                    // Convert Period-style values to approximate Duration
                    Period period = Period.parse(value);
                    // Convert to days (approximation)
                    // Use 30 days for a month and 365 days for a year as an approximation
                    long days = period.getYears() * 365L + period.getMonths() * 30L + period.getDays();
                    return Duration.ofDays(days);
                }
                throw e; // Not a Period format either, rethrow
            } catch (Exception nestedEx) {
                throw new IllegalArgumentException("Invalid duration format: " + value, e);
            }
        }
    }

    @Override
    public TemporalAmount toCanonicalForm(
        TemporalAmount value
    ){
        if (value instanceof Period) {
            final Period period = (Period) value;
            if (period.getYears() != 0) {
                return Period.of(
                    0,
                    period.getMonths() + period.getYears() * 12,
                    period.getDays()
                );
            }
            if (period.getMonths() == 0 && period.getDays() != 0) {
                return Duration.ofDays(period.getDays());
            }
        }
        return value;
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
    @Override
    public Instant toImmutableDateTime(
        Instant value
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
    @Override
    public LocalDate toImmutableDate(
       LocalDate value
    ){
        return value;
    }

}
