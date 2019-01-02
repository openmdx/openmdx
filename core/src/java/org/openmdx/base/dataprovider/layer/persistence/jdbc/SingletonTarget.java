/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Singleton Target 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2017, OMEX AG, Switzerland
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

package org.openmdx.base.dataprovider.layer.persistence.jdbc;

import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_2_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.kernel.exception.BasicException;

/**
 * Singleton Target
 */
public class SingletonTarget extends AbstractTarget {

    /**
     * Constructor 
     *
     * @param completer
     */
    protected SingletonTarget(
        Database_2_0 completer
    ) {
        super(completer);
    }

    private ObjectRecord object = null;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.Target#isSaturated()
     */
    @Override
    public boolean isSaturated() {
        return object != null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.AbstractTarget#getObjectBatchSize()
     */
    @Override
    public int getObjectBatchSize() {
        return isSaturated() ? 0 : 1;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.Target#getStartPosition()
     */
    @Override
    public int getStartPosition() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.Target#propagate(org.openmdx.base.rest.cci.ObjectRecord)
     */
    @Override
    protected void propagate(
        ObjectRecord object
    )
        throws ServiceException {
        if(isSaturated()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "This request has cardinality 0..1"
            );
        }
        this.object = object;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.Target#close(boolean)
     */
    @Override
    public void close(
        boolean hasMore
    ) throws ServiceException {
        if(hasMore) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "This request should be completed at once",
                new BasicException.Parameter("hasMore", hasMore)
            );
        }
        close();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.Target#close(long)
     */
    @Override
    public void close(
        long total
    ) throws ServiceException {
        if(total > 1) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "This request has cardinality 0..1",
                new BasicException.Parameter("total", total)
            );
        }
        close( total == 1L && !isSaturated());
    }

    /**
     * Retrieve object.
     *
     * @return Returns the object.
     */
    public ObjectRecord getSingleton() {
        return this.object;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Target#getFetchSize()
     */
    @Override
    public int getFetchSize() {
        return 1;
    }
    
}
