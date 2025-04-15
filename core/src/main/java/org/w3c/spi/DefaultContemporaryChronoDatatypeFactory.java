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

import org.w3c.format.DateTimeFormat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

public class DefaultContemporaryChronoDatatypeFactory implements ContemporaryChronoDatatypeFactory {

    @Override
    public Instant newDateTime(String value) {
        try {
            return parseAsInstant(value);
        } catch (DateTimeParseException | ParseException e) {
            throw new IllegalArgumentException("Invalid datetime format: " + value, e);
        }
    }

    private Instant parseAsInstant(String value) throws ParseException {
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

            final Instant parsed = Instant.parse(transformed);
            return DateTimeFormat.EXTENDED_UTC_FORMAT.parse(parsed.toString());
        } catch (StringIndexOutOfBoundsException e) {
            throw new ParseException("Invalid datetime format", 0);
        }

    }

    @Override
    public LocalDate newDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + value, e);
        }
    }

    @Override
    public Duration newDuration(String value) {
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

    private Period parseAsPeriod(String value) {
        if (value == null) {
            return null;
        }
        return Period.parse(value);
    }

    @Override
    public Duration newDuration(
            final boolean isPositive,
            BigInteger years,
            BigInteger months,
            BigInteger days,
            BigInteger hours,
            BigInteger minutes,
            BigDecimal seconds) {

        // Validate that all components are non-negative
        if ((years != null && years.compareTo(BigInteger.ZERO) < 0) ||
                (months != null && months.compareTo(BigInteger.ZERO) < 0) ||
                (days != null && days.compareTo(BigInteger.ZERO) < 0) ||
                (hours != null && hours.compareTo(BigInteger.ZERO) < 0) ||
                (minutes != null && minutes.compareTo(BigInteger.ZERO) < 0) ||
                (seconds != null && seconds.compareTo(BigDecimal.ZERO) < 0)) {
            throw new IllegalArgumentException("All duration components must be non-negative values");
        }

        // Convert year-month components to days (approximate)
        if (years != null && years.compareTo(BigInteger.ZERO) > 0) {
            // Approximate 1 year as 365.25 days
            BigInteger yearDays = years.multiply(BigInteger.valueOf(365)); // Simplified to 365 for integer math
            days = (days != null) ? days.add(yearDays) : yearDays;
        }

        if (months != null && months.compareTo(BigInteger.ZERO) > 0) {
            // Approximate 1 month as 30 days
            BigInteger monthDays = months.multiply(BigInteger.valueOf(30));
            days = (days != null) ? days.add(monthDays) : monthDays;
        }

        // Calculate total seconds and nanos
        long totalSeconds = 0;
        int nanos = 0;

        // Add days contribution to seconds
        if (days != null) {
            totalSeconds += days.longValue() * 24 * 60 * 60;
        }

        // Add hours
        if (hours != null) {
            totalSeconds += hours.longValue() * 60 * 60;
        }

        // Add minutes
        if (minutes != null) {
            totalSeconds += minutes.longValue() * 60;
        }

        // Add seconds and calculate nanoseconds
        if (seconds != null) {
            long wholeSeconds = seconds.longValue();
            totalSeconds += wholeSeconds;

            // Calculate nanoseconds from the fractional part of seconds
            BigDecimal fractionalPart = seconds.subtract(new BigDecimal(wholeSeconds));
            nanos = fractionalPart.movePointRight(9).intValue();
        }

        // Create the duration object
        Duration duration = Duration.ofSeconds(totalSeconds, nanos);

        // Apply the sign if needed
        if (!isPositive) {
            duration = duration.negated();
        }

        return duration;

    }

    /**
     * @param isPositive
     * @param year
     * @param month
     * @return
     */
    @Override
    public Period newPeriod(boolean isPositive, int year, int month) {
        if (!isPositive) {
            year = -year;
            month = -month;
        }
        return Period.of(year, month, 0);
    }

    /**
     * @param isPositive
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    @Override
    public Duration newDurationDayTime(boolean isPositive, int day, int hour, int minute, int second) {

        // All values are expected to be non-negative; the sign is controlled by isPositive
        if (day < 0 || hour < 0 || minute < 0 || second < 0) {
            throw new IllegalArgumentException("All duration components must be non-negative values");
        }

        // For a negative duration, we need to convert all components to seconds,
        // apply the sign, and then create the Duration
        long totalSeconds = (day * 24L * 60L * 60L) +
                (hour * 60L * 60L) +
                (minute * 60L) +
                second;

        // Apply the sign based on isPositive
        if (!isPositive) {
            totalSeconds = -totalSeconds;
        }

        // When creating a Duration, we need to preserve all components
        // For Java's Duration, we use ofSeconds for precision
        return Duration.ofSeconds(totalSeconds);
    }

    @Override
    public Instant toDateTime(Instant value) {
//        if (value == null) {
//            throw new IllegalArgumentException("Null datetime value");
//        }
        return value; // Instant is already immutable
    }

    @Override
    public LocalDate toDate(LocalDate value) {
//        if (value == null) {
//            throw new IllegalArgumentException("Null date value");
//        }
        return value; // LocalDate is already immutable
    }

    @Override
    public Duration toDuration(Duration value) {
        if (value == null) {
            throw new IllegalArgumentException("Null duration value");
        }
        return value; // Duration is already immutable
    }

    @Override
    public Period toPeriod(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null period value");
        }
        return Period.parse(value); // Period is already immutable
    }
}
