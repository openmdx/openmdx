/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Lenient Datatype Comparator
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
package org.openmdx.base.query.lenient;

import java.util.Date;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.spi2.Datatypes;
import org.w3c.time.ChronoTypes;


/**
 * Allows comparison of XML Datatype classes
 */
class LenientClassicDurationComparator extends LenientNumberComparator {

    /**
     * Constructor
     */
    LenientClassicDurationComparator(
    ) {
        super();
    }

    @Override
    public boolean test(Object first, Object second) {
        return first instanceof Duration && (
            second instanceof Duration || second instanceof CharSequence
        );
    }

    @Override
    public int compare(
        Object first,
        Object second
    ) {
        if(second instanceof Duration) {
            return ChronoTypes.compare(
                (Duration) first,
                (Duration) second
            );
        } else {
            return ChronoTypes.compare(
                (Duration) first,
                Datatypes.create(Duration.class, second.toString())
            );
        }
    }

}
