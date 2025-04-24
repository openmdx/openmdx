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

import java.util.regex.Pattern;
import javax.xml.datatype.DatatypeConstants;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class ChronoUtils {

    /**
     * Match YYYY[...]MMDD
     */
    public static final Pattern BASIC_DATE_PATTERN = Pattern.compile("^\\d{8,}$");

    /**
     * Match YYYY[...]-MM-DD
     */
    public static final Pattern EXTENDED_DATE_PATTERN = Pattern.compile("^\\d{4,}-\\d{2}-\\d{2}$");

    public static #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate #endif createDate(int year, int month, int dayOfMonth) {
        return #if CLASSIC_CHRONO_TYPES org.w3c.spi.DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(year, month, dayOfMonth, DatatypeConstants.FIELD_UNDEFINED)
        #else java.time.LocalDate.of(year, month, dayOfMonth)
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
            return duration.toMillis() / 1000 % 60;
//            return duration.getSeconds() % 60;
//            return duration.getSeconds();
//            return duration.toSeconds() % 60;
//            return (duration.toMinutes() * 60) % 60;
        } //else if (field.equals(DatatypeConstants.MILLISECONDS)) {
            //return duration.toMillis() % 1000;
        //}
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
            return false;
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
            return false;
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

}
