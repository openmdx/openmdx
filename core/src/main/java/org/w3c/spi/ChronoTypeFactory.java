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

#if CLASSIC_CHRONO_TYPES
import java.util.Date;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.Duration;
#else
import java.time.LocalDate;
import java.time.Instant;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import javax.xml.datatype.XMLGregorianCalendar;
import org.w3c.spi2.Datatypes;
#endif

/**
 * Immutable Datatype Factory
 */
public interface ChronoTypeFactory {

    //------------------------------------------------------------------------
    // External Form
    //------------------------------------------------------------------------

    /**
     * Create an UTC based immutable date-time instance
     *
     * @param value the basic or extended representation
     *
     * @return a corresponding date-time instance
     *
     * @exception IllegalArgumentException
     * if the value can't be parsed
     */
    #if CLASSIC_CHRONO_TYPES Date #else Instant #endif newDateTime(
        String value
    );

    /**
     * Create a new immutable date instance
     *
     * @param value the basic or extended representation
     *
     * @return a corresponding date-time instance
     *
     * @exception IllegalArgumentException
     * if the value can't be parsed
     */
    #if CLASSIC_CHRONO_TYPES XMLGregorianCalendar #else LocalDate #endif newDate(
        String value
    );

    /**
     * Create a new immutable duration instance
     *
     * @param value the representation with designators
     *
     * @return a corresponding duration instance
     *
     * @exception IllegalArgumentException
     * if the value can't be parsed
     */
    #if CLASSIC_CHRONO_TYPES Duration #else TemporalAmount #endif newDuration(
        String value
    );

    /**
     * Create a new immutable duration instance
     *
     * @param value the representation with designators
     *
     * @return a corresponding duration instance
     *
     * @exception IllegalArgumentException
     * if the value can't be parsed
     */
    Duration newDurationDayTime(
        String value
    );

    /**
     * Create a new immutable duration instance
     *
     * @param value the representation with designators
     *
     * @return a corresponding duration instance
     *
     * @exception IllegalArgumentException
     * if the value can't be parsed
     */
    #if CLASSIC_CHRONO_TYPES Duration #else Period #endif newDurationYearMonth(
        String value
    );


    //------------------------------------------------------------------------
    // Canonical Form
    //------------------------------------------------------------------------

    /**
     * Retrieve the canonical form of the temporal amount (as opposed to the normalized form!)
     *
     * @param value an internal representation
     *
     * @return a duration containing seconds or months only
     */
    #if CLASSIC_CHRONO_TYPES Duration #else TemporalAmount #endif toCanonicalForm(
        #if CLASSIC_CHRONO_TYPES Duration #else TemporalAmount #endif value
    );


    //------------------------------------------------------------------------
    // From Internal Representation
    //------------------------------------------------------------------------

    /**
     * Retrieve an immutable date-time instance
     *
     * @param value an internal representation
     *
     * @return a corresponding date-time instance
     *
     * @exception IllegalArgumentException
     * if the value is not an org::w3c::dateTime instance
     */
    #if CLASSIC_CHRONO_TYPES Date #else Instant #endif toImmutableDateTime(
        #if CLASSIC_CHRONO_TYPES Date #else Instant #endif value
    );

    /**
     * Retrieve an immutable date instance
     *
     * @param value an internal representation
     *
     * @return a corresponding date-time instance
     *
     * @exception IllegalArgumentException
     * if the value is not an org::w3c::date instance
     */
    #if CLASSIC_CHRONO_TYPES XMLGregorianCalendar #else LocalDate #endif toImmutableDate(
            #if CLASSIC_CHRONO_TYPES XMLGregorianCalendar #else LocalDate #endif value
    );

}