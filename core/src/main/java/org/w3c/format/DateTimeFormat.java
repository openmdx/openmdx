/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: infrastructure: date format
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
package org.w3c.format;

/**
 * This class provides thread-safe Date Time Formatters
 */
public class DateTimeFormat {

    #if CLASSIC_CHRONO_TYPES

    /**
     * ISO 8601:2004 compliant extended format
     */
    public static final DateTimeFormatter<java.util.Date> EXTENDED_UTC_FORMAT = ClassicDateTimeFormatter.EXTENDED_UTC_FORMAT;

    /**
     * ISO 8601:2004 compliant basic format
     */
    public static final DateTimeFormatter<java.util.Date> BASIC_UTC_FORMAT = ClassicDateTimeFormatter.BASIC_UTC_FORMAT;

    /**
     * Netscape format used in Cookies
     */
    public static final DateTimeFormatter<java.util.Date> NETSCAPE_FORMAT = ClassicDateTimeFormatter.NETSCAPE_FORMAT;

    #else

    /**
     * ISO 8601:2004 compliant extended format
     */
    public static final DateTimeFormatter<java.time.Instant> EXTENDED_UTC_FORMAT = ContemporaryDateTimeFormatter.EXTENDED_UTC_FORMAT;

    /**
     * ISO 8601:2004 compliant basic format
     */
    public static final DateTimeFormatter<java.time.Instant> BASIC_UTC_FORMAT = ContemporaryDateTimeFormatter.BASIC_UTC_FORMAT;

    /**
     * Netscape format used in Cookies
     */
    public static final DateTimeFormatter<java.time.Instant> NETSCAPE_FORMAT = ContemporaryDateTimeFormatter.NETSCAPE_FORMAT;

    #endif

}