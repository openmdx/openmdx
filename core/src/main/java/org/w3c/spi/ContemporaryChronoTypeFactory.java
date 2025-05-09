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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

#if !CLASSIC_CHRONO_TYPES
class ContemporaryChronoTypeFactory extends AbstractChronoTypeFactory {

    /**
     * Constructor
     */
    private ContemporaryChronoTypeFactory(){
        super();
    }

    static final ChronoTypeFactory INSTANCE = new ContemporaryChronoTypeFactory();

    @Override
    public Instant newDateTime(String value) {

        if (value == null) {
            return null;
        }

        try {

            String toParse = value;
            boolean isBasicFormat = !(value.length() - value.replace(":", "").length() == 2
                    && value.length() - value.replace("-", "").length() == 2);

            if (isBasicFormat) {

                String ymd = value.substring(0, value.indexOf("T"));
                String y = ymd.substring(0, ymd.length() - 4);

                int idx = y.length();
                StringBuilder sb = new StringBuilder();
                sb.append(value, 0, idx).append('-');
                sb.append(value, idx, idx + 2).append('-');
                idx += 2;
                sb.append(value, idx, idx + 2);
                sb.append('T');
                int timeSegmentIdx = ymd.length() + 1;
                sb.append(value, timeSegmentIdx, timeSegmentIdx + 2).append(':');
                timeSegmentIdx += 2;
                sb.append(value, timeSegmentIdx, timeSegmentIdx + 2).append(':');
                timeSegmentIdx += 2;
                boolean hasMillis = value.contains(".");
                sb.append(value, timeSegmentIdx, timeSegmentIdx + (!hasMillis ? 3 : 7));

                toParse = sb.toString();
            }

            return Instant.parse(toParse);

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
#endif