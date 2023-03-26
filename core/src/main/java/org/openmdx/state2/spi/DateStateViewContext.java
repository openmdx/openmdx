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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.ViewKind;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi.ImmutableDatatypeFactory;

/**
 * Date State View Context
 */
public class DateStateViewContext 
    extends StateViewContext<XMLGregorianCalendar>
    implements DateStateContext 
{

    /**
     * Constructor 
     *
     * @param validFor the view's valid time point
     * @param validAt the view's transaction time point, or {@code null} for an up-to-date view
     */
    private DateStateViewContext(
        XMLGregorianCalendar validFor,
        Date validAt
    ) {
        super(
            ViewKind.TIME_POINT_VIEW,
            validFor, 
            validAt,
            null, // validFrom
            null, // validTo
            true // includeUpperBound
        );
    }

    /**
     * Constructor 
     *
     * @param validFrom the begin of the time range, or {@code null} for an unconstrained lower bound
     * @param validTo the end of the time range, or {@code null} for an unconstrained upper bound
     */
    private DateStateViewContext(
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ) {
        super(
            ViewKind.TIME_RANGE_VIEW,
            null, // validFor 
            null, // validAt
            validFrom,
            validTo,
            true // includeUpperBound
        );
    }
    
    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = -5803852880487324874L;
    
    /**
     * Create a time point view context 
     *
     * @param validFor the view's valid time point
     * @param validAt the view's transaction time point, or {@code null} for an up-to-date view
     */
    public static DateStateViewContext newTimePointViewContext(
        XMLGregorianCalendar validFor,
        Date validAt
    ){
        ImmutableDatatypeFactory datatypeFactory = DatatypeFactories.immutableDatatypeFactory();
        return new DateStateViewContext(
            datatypeFactory.toDate(validFor),
            datatypeFactory.toDateTime(validAt)
        );
    }

    /**
     * Create a time range view context 
     *
     * @param validFrom the begin of the time range, or {@code null} for an unconstrained lower bound
     * @param validTo the end of the time range, or {@code null} for an unconstrained upper bound
     * 
     * @throws IllegalArgumentException if validTo is less than validFrom 
     */
    public static DateStateViewContext newTimeRangeViewContext(
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        ImmutableDatatypeFactory datatypeFactory = DatatypeFactories.immutableDatatypeFactory();
        XMLGregorianCalendar immutableValidFrom = datatypeFactory.toDate(validFrom);
        XMLGregorianCalendar immutableValidTo = datatypeFactory.toDate(validTo);
        Order.assertTimeRange(immutableValidFrom, immutableValidTo);
        return new DateStateViewContext(immutableValidFrom, immutableValidTo);
    }
    
    /**
     * Retrieve the current date
     * 
     * @return the current date
     */
    public static XMLGregorianCalendar today(
    ){
        GregorianCalendar calendar = new GregorianCalendar();
        return DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
            calendar.get(Calendar.YEAR), 
            calendar.get(Calendar.MONTH) + 1, 
            calendar.get(Calendar.DAY_OF_MONTH),
            DatatypeConstants.FIELD_UNDEFINED
        );
    }
    
    
    //  ------------------------------------------------------------------------
    //  Implements DateStateContext
    //  ------------------------------------------------------------------------

    /**
     * Retrieve validTo.
     *
     * @return Returns the validTo.
     */
    public final XMLGregorianCalendar getValidTo(
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
        XMLGregorianCalendar timePoint
    ) {
        String xmlFormat = timePoint.toXMLFormat();
        return new StringBuilder(8).append(
            xmlFormat.substring(0, xmlFormat.length() - 6)
        ).append(
            xmlFormat.substring(xmlFormat.length() - 5, xmlFormat.length() - 3)
        ).append(
            xmlFormat.substring(xmlFormat.length() - 2)
        ).toString();
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.spi.StateViewContext#now()
     */
    @Override
    protected XMLGregorianCalendar newValidAt() {
        return DateStateViewContext.today();
    }

}