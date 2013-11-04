/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Result Record
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.base.rest.spi;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.spi.VariableSizeIndexedRecord;

/**
 * Result Record
 */
public class ResultRecord 
    extends VariableSizeIndexedRecord
    implements org.openmdx.base.rest.cci.ResultRecord, Externalizable
{

    /**
     * Constructor 
     */
    public ResultRecord() {
        super(NAME);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -1447605619412752707L;

    /**
     * The eagerly acquired REST formatter instance
     */
    protected static final RestFormatter restFormatter = RestFormatters.getFormatter();
    
    /**
     * The base collections size
     */
    private Long total;
    
    /**
     * Tells whether more elements can be found on the base collection
     */
    private Boolean hasMore;

    
    //--------------------------------------------------------------------------
    // Implements ResultRecord
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.ResultRecord#setMore(boolean)
     */
    @Override
    public void setHasMore(boolean hasMore) {
        this.hasMore = Boolean.valueOf(hasMore);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.ResultRecord#getMore()
     */
    @Override
    public Boolean getHasMore() {
        return this.hasMore;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.ResultRecord#setTotal(long)
     */
    @Override
    public void setTotal(long total) {
        this.total = Long.valueOf(total);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.ResultRecordd#getTotal()
     */
    @Override
    public Long getTotal() {
        return this.total;
    }

    
    //--------------------------------------------------------------------------
    // Implements Externalizable
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal(
        ObjectInput in
    ) throws IOException, ClassNotFoundException {
        try {
            RestParser.parseResponse(
                this, 
                RestParser.asSource(in)
            );
        } catch (ServiceException exception) {
            throw (InvalidObjectException) new InvalidObjectException(
                exception.getMessage()
            ).initCause(
                exception.getCause()
            );
        }
    }

    /* (non-Javadoc)
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal(
        ObjectOutput out
    ) throws IOException {
        try {
            Target target = restFormatter.asTarget(out); 
            restFormatter.format(target, null, this);
            target.close();
        } catch (Exception exception) {
            throw (NotSerializableException) new NotSerializableException(
                exception.getMessage()
            ).initCause(
                exception.getCause()
            );
        }
    }

}
