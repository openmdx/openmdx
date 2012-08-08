/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DateState_1.java,v 1.7 2008/09/10 08:55:21 hburger Exp $
 * Description: DateStateExcludingEnd_1 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:21 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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

package org.openmdx.compatibility.base.dataprovider.layer.model;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.kernel.exception.BasicException;

/**
 * DateStateExcludingEnd_1
 */
public class DateState_1
extends AbstractState_1
{

    /**
     * Constructor 
     */
    public DateState_1() {
        super();
    }


    //------------------------------------------------------------------------
    // Extends Layer_1
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.AbstractState_1#prolog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[])
     */
    public void prolog(
        ServiceHeader header, 
        DataproviderRequest[] requests
    ) throws ServiceException {
        super.prolog(header, requests);
        this.now = new Date();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.OptimisticLocking_1#epilog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[], org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply[])
     */
    public void epilog(
        ServiceHeader header, 
        DataproviderRequest[] requests, 
        DataproviderReply[] replies
    ) throws ServiceException {
        this.now = null;
        super.epilog(header, requests, replies);
    }


    //------------------------------------------------------------------------
    // Extends AbstractState_1
    //------------------------------------------------------------------------

    private String add(
        String date,
        int days
    ) throws ServiceException{
        try {
            return DATE_FORMATTER.format(
                new Date(
                    DATE_FORMATTER.parse(date).getTime() +
                    1000L * 60L * 60L * 24L * days
                )
            );
        } catch (ParseException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                "Conversion between inclusive abd exclusive end dates failed",
                new BasicException.Parameter("date", date),
                new BasicException.Parameter("days", days)
            );
        }
    }


    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.AbstractState_1#excludingEnd()
     */
    protected boolean excludingEnd() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.AbstractState_1#toExclusiveValidTo(java.lang.String)
     */
    protected String toExclusiveValidTo(String validTo) throws ServiceException {
        return validTo == null ? null : add(validTo, +1);
    }


    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.AbstractState_1#toModelledValidTo(java.lang.String)
     */
    protected String toModelledValidTo(String validTo) throws ServiceException {
        return validTo == null ? null : add(validTo, -1);
    }


    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.AbstractState_1#validFromAttribute()
     */
    protected String validFromAttribute() {
        return State_1_Attributes.STATE_VALID_FROM;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.AbstractState_1#validToAttribute()
     */
    protected String validToAttribute() {
        return State_1_Attributes.STATE_VALID_TO;
    }


    protected Pattern validForPattern(){
        return DATE_PATTERN;
    }

    protected String stateTypeName(){
        return "org:openmdx:compatibility:state1:DateState";
    }

    protected String getRequestedFor(
        ServiceHeader header
    ){
        String requestedFor = header.getRequestedFor();
        if (requestedFor == null) {
            requestedFor = DATE_FORMAT.format(new Date());
        }
        return requestedFor;
    }

    protected String getRequestedAt(
        ServiceHeader header
    ){
        return DATE_TIME_FORMAT.format(
            this.now == null ? new Date() : this.now
        );
    }

    @SuppressWarnings("unchecked")
    protected String getRequestedAt(
        ServiceHeader header,
        DataproviderRequest request
    ){
        SparseList modifiedAt = request.object().getValues(SystemAttributes.MODIFIED_AT);
        String requestedAt = modifiedAt == null ? null : (String)modifiedAt.get(0);
        return requestedAt == null ? getRequestedAt(header) : requestedAt;
    }


    //------------------------------------------------------------------------
    // Class Members
    //------------------------------------------------------------------------

    protected static final DateFormat DATE_FORMAT = DateFormat.getInstance("yyyyMMdd");

    protected static final Pattern DATE_PATTERN = Pattern.compile("^[0-9]{8}$");


    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------

    private Date now;

    private static final DateFormat DATE_FORMATTER = DateFormat.getInstance("yyyyMMdd");

}
