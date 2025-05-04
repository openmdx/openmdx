/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: DurationMarshaller
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

#if CLASSIC_CHRONO_TYPES
import javax.xml.datatype.Duration;
#else
import java.time.temporal.TemporalAmount;
#endif

abstract class AbstractChronoTypeFactory implements ChronoTypeFactory {

    @Override
    public final #if CLASSIC_CHRONO_TYPES Duration #else TemporalAmount #endif newDuration(String externalRepresentation) {
        if(externalRepresentation == null) {
            return null;
        }
        final int y = externalRepresentation.indexOf('Y');
        final int m = externalRepresentation.indexOf('M');
        final int w = externalRepresentation.indexOf('W');
        final int d = externalRepresentation.indexOf('D');
        final int t = externalRepresentation.indexOf('T');
        final boolean time = t > 0;
        final boolean day = d > 0;
        final boolean week = w > 0;
        final boolean year = y > 0 ;
        final boolean month = m > 0 && (!time || m < t);
        if(year || month) {
            if(time) {
                return newDurationYearMonthDayTime(externalRepresentation);
            } else if (week || day ) {
                return newDurationYearMonthDay(externalRepresentation);
            } else {
                return newDurationYearMonth(externalRepresentation);
            }
        } else {
            return newDurationDayTime(externalRepresentation);
        }
    }

    protected abstract #if CLASSIC_CHRONO_TYPES Duration #else TemporalAmount #endif newDurationYearMonthDay(String value);
    protected abstract #if CLASSIC_CHRONO_TYPES Duration #else TemporalAmount #endif newDurationYearMonthDayTime(String value);

}
