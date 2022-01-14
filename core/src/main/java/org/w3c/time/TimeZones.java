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

package org.w3c.time;

import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.datatype.DatatypeConstants;

/**
 * Time Zones
 */
public class TimeZones {

    private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();
    
    /**
     * Cache the time zones (but avoid concurrency locks)
     */
    private static final ConcurrentMap<Integer, TimeZone> TIME_ZONES = new ConcurrentHashMap<Integer, TimeZone>();
    
    /**
     * <p>
     * Returns a <code>java.util.TimeZone</code> for this class.
     * </p>
     *
     * <p>
     * If <code>zoneOffset</code> is <code>FIELD_UNDEFINED</code>, return
     * default time zone for this host.
     * (Same default as <code>java.util.GregorianCalendar</code>).
     * </p>DatatypeConstants
     *
     * @param zoneOffset
     *            the zone offset, or <code>DatatypeConstants.FIELD_UNDEFINED</code>
     *            in order to retrieve the default time zone
     *
     * @return TimeZone for this zoneoffset
     */
    public static TimeZone toTimeZone(
        int zoneOffset
    ) {
        if(zoneOffset == DatatypeConstants.FIELD_UNDEFINED) {
            return DEFAULT_TIME_ZONE;
        } 
        final Integer key = Integer.valueOf(zoneOffset);
        TimeZone existingTimeZone = TIME_ZONES.get(key);
        if (existingTimeZone == null) {
            final TimeZone newTimeZone = TimeZone.getTimeZone(toTimeZoneId(zoneOffset));
            existingTimeZone = TIME_ZONES.putIfAbsent(key, newTimeZone);
            if(existingTimeZone == null) {
                return newTimeZone;
            }
        }
        return existingTimeZone;
    }

    /**
     * Calculate a custom time zone id
     * 
     * @param timeZoneOffset
     * 
     * @return the corresponding time zone id
     */
    private static String toTimeZoneId(
        int timeZoneOffset
    ) {
        final char sign;
        final int absoluteOffset;
        if (timeZoneOffset < 0) {
            sign = '-';
            absoluteOffset = -timeZoneOffset;
        } else {
            sign = '+';
            absoluteOffset = timeZoneOffset;
        }
        String s = Integer.toString(
            10000 + 100 * (absoluteOffset / 60) + (absoluteOffset % 60)
        );
        return "GMT" + sign + s.substring(1, 3) + ':' + s.substring(3);
    }

}
