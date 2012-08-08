/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: DateTimeStateViewContext.java,v 1.3 2009/03/31 17:05:16 hburger Exp $
 * Description: Date State View
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/31 17:05:16 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
 * All rights reserved.
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

import java.util.Date;

import org.openmdx.base.text.format.DateFormat;
import org.openmdx.state2.cci.DateTimeStateContext;
import org.openmdx.state2.cci.ViewKind;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi.ImmutableDatatypeFactory;

/**
 * Date State View Context
 */
public class DateTimeStateViewContext 
    extends StateViewContext<Date>
    implements DateTimeStateContext 
{

    /**
     * Constructor 
     * 
     * @param datatypeFactory the immutable datatype factory
     * @param validFrom the begin of the time range, or <code>null</code> for an unconstrained lower bound
     * @param validTo the end of the time range, or <code>null</code> for an unconstrained upper bound
     * @param validFor the view's valid time point, or <code>null</code> for time range views
     * @param validAt the view's transaction time point, or <code>null</code> for time range views
     */
    private DateTimeStateViewContext(
        ImmutableDatatypeFactory datatypeFactory,
        ViewKind viewKind,
        Date validFor,
        Date validAt,
        Date validFrom, 
        Date invalidFrom
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
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -2814664512154648676L;

    /**
     * Create a time point view context 
     *
     * @param validFor the view's valid time point
     * @param validAt the view's transaction time point
     */
    public static DateTimeStateViewContext newTimePointViewContext(
        Date validFor,
        Date validAt
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
     * @param validFrom the begin of the time range, or <code>null</code> for an unconstrained lower bound
     * @param validTo the end of the time range, or <code>null</code> for an unconstrained upper bound
     */
    public static DateTimeStateViewContext newTimeRangeViewContext(
        Date validFrom,
        Date validTo
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
    public final Date getInvalidFrom(
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
        Date timePoint
    ) {
        return DateFormat.getInstance().format(timePoint);
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.spi.StateViewContext#now()
     */
    @Override
    protected Date newValidAt() {
        return new Date();
    }

}