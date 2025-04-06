/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test Removable 
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
package org.openmdx.base.aop1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test Removable
 */
public class TestRemovable {

    #if CLASSIC_CHRONO_TYPES

    @Test
    public void testRemovedAtPlaceholder() throws java.text.ParseException{
        org.w3c.format.DateTimeFormat format = org.w3c.format.DateTimeFormat.getInstance("yyyyyMMdd'T'HHmmss.SSS'Z'");
        Assertions.Assertions.assertEquals(
            "100000101T000000.000Z", 
            format.format(Removable_1.IN_THE_FUTURE),
            "Future placeholder"
        );
    }

    #else

    @Test
    public void testRemovedAtPlaceholder() {
        java.time.format.DateTimeFormatter formatter = new java.time.format.DateTimeFormatterBuilder()
                .appendValue(java.time.temporal.ChronoField.YEAR, 4, 10, java.time.format.SignStyle.NEVER)
                .appendPattern("MMdd'T'HHmmss.SSS'Z'")
                .toFormatter()
                .withZone(java.time.ZoneOffset.UTC);
        Assertions.assertEquals(
                "100000101T000000.000Z",
                formatter.format(Removable_1.IN_THE_FUTURE),
                "Future placeholder"
        );
    }

    #endif
}
