/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Date State View
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
package org.openmdx.state2.spi;

import org.openmdx.state2.cci.DateTimeStateContext;
import org.openmdx.state2.cci.ViewKind;
#if CLASSIC_CHRONO_TYPES import org.w3c.format.DateTimeFormat;#endif
import org.w3c.spi.DatatypeFactories;
#if CLASSIC_CHRONO_TYPES import org.w3c.spi.ImmutableDatatypeFactory;#endif

import java.time.Instant;

/**
 * Date State View Context
 */
public class DateTimeStateViewContext 
    extends StateViewContext<#if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif>
    implements DateTimeStateContext 
{

    /**
     * Constructor 
     * 
     * @param datatypeFactory the immutable datatype factory
     * @param validFrom the begin of the time range, or {@code null} for an unconstrained lower bound
     * @param invalidFrom the end of the time range, or {@code null} for an unconstrained upper bound
     * @param validFor the view's valid time point, or {@code null} for time range views
     * @param validAt the view's transaction time point, or {@code null} for time range views
     */
    private DateTimeStateViewContext(
        ImmutableDatatypeFactory datatypeFactory,
        ViewKind viewKind,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif validFor,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif validAt,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif validFrom,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif invalidFrom
    ) {
        super(
            viewKind,
            datatypeFactory.toDateTime(validFor), 
            datatypeFactory.toDateTime(validAt),
            datatypeFactory.toDateTime(validFrom),
            datatypeFactory.toDateTime(invalidFrom),
            false // includeUpperBound
        );
    }
    
    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = -2814664512154648676L;

    /**
     * Create a time point view context 
     *
     * @param validFor the view's valid time point
     * @param validAt the view's transaction time point
     */
    public static DateTimeStateViewContext newTimePointViewContext(
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif validFor,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif validAt
    ){
        return new DateTimeStateViewContext(
            DatatypeFactories.immutableDatatypeFactory(),
            ViewKind.TIME_POINT_VIEW,
            validFor,
            validAt, 
            null, // validFrom
            null // validTo
        );
    }

    /**
     * Create a time range view context 
     *
     * @param validFrom the begin of the time range, or {@code null} for an unconstrained lower bound
     * @param validTo the end of the time range, or {@code null} for an unconstrained upper bound
     */
    public static DateTimeStateViewContext newTimeRangeViewContext(
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif validFrom,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif validTo
    ){
        return new DateTimeStateViewContext(
            DatatypeFactories.immutableDatatypeFactory(),
            ViewKind.TIME_RANGE_VIEW,
            null, // validFor
            null, // validAt
            validFrom, 
            validTo
        );
    }
    
    
    //  ------------------------------------------------------------------------
    //  Implements DateStateContext
    //  ------------------------------------------------------------------------

    /**
     * Retrieve the time range view's upper bound.
     *
     * @return the time range view's upper bound.
     */
    public final #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif getInvalidFrom(
    ) {
        return super.getUpperBound();
    }


    //  ------------------------------------------------------------------------
    //  Extends AbstractStateContext
    //  ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.state2.spi.AbstractStateContext#toString(java.lang.Object)
     */
    @Override
    protected String toString(
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif timePoint
    ) {
        return DateTimeFormat.BASIC_UTC_FORMAT.format(timePoint);
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.spi.StateViewContext#now()
     */
    @Override
    protected #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif newValidAt() {
        return #if CLASSIC_CHRONO_TYPES new java.util.Date() #else Instant.now() #endif;
    }

}