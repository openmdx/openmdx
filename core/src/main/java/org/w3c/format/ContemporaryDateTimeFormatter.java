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

import org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes.DurationMarshaller;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;

import java.text.ParseException;
import java.time.Instant;

class ContemporaryDateTimeFormatter implements DateTimeFormatter<Instant> {

    private enum Format {EXTENDED_UTC, BASIC_UTC, NETSCAPE};

    private final Format format;

    private ContemporaryDateTimeFormatter(Format format){
        if (format == null) {
            throw new IllegalArgumentException("Format must not be null");
        }
        this.format = format;
    }

    /**
     * ISO 8601:2004 compliant extended format
     */
    final static DateTimeFormatter<Instant> EXTENDED_UTC_FORMAT = new ContemporaryDateTimeFormatter(Format.EXTENDED_UTC);

    /**
     * ISO 8601:2004 compliant basic format
     */
    final static DateTimeFormatter<Instant> BASIC_UTC_FORMAT = new ContemporaryDateTimeFormatter(Format.BASIC_UTC);

    /**
     * Netscape format used in cookies
     */
    final static DateTimeFormatter<Instant> NETSCAPE_FORMAT = new ContemporaryDateTimeFormatter(Format.NETSCAPE);

    /**
     * @param instant
     * @return
     */
    @Override
    public String format(Instant instant) {
        switch (format) {
            case BASIC_UTC: {
                return instant.toString().replaceAll("[-:]", "");
            }
            case EXTENDED_UTC: {
                return instant.toString();
            }
            case NETSCAPE: {
                return new java.text.SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z").format(instant);
            }
            default: throw new RuntimeServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Unsupported format",
                        new BasicException.Parameter("format", format),
                        new BasicException.Parameter("value", instant)
            );
        }
    }

    /**
     * @param dateTimeString
     * @return
     */
    @Override
    public Instant parse(String dateTimeString) throws ParseException {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }

        try {
            // Try direct ISO-8601 parsing first
            return Instant.parse(dateTimeString);
        } catch (Exception e) {
            try {
                // Handle basic format (no separators)
                if (format == Format.BASIC_UTC) {
                    String extended = dateTimeString
                            .replaceAll("(\\d{4})(\\d{2})(\\d{2})T(\\d{2})(\\d{2})(\\d{2})Z?",
                                    "$1-$2-$3T$4:$5:$6");
                    return Instant.parse(extended);
                }

                // For Netscape format, convert to ISO format
                if (format == Format.NETSCAPE) {
                    java.text.SimpleDateFormat netscapeFormat =
                            new java.text.SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z");
                    netscapeFormat.setLenient(true);
                    return netscapeFormat.parse(dateTimeString).toInstant();
                }

                throw new ParseException("Unparseable date: " + dateTimeString, 0);

            } catch (Exception nested) {
                throw new ParseException("Failed to parse date: " + dateTimeString, 0);
            }
        }


    }

}
