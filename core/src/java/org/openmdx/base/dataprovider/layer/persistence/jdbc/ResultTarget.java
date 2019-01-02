/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: ResultTarget 
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

import javax.jdo.FetchPlan;
import javax.resource.ResourceException;

import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_2_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.Numbers;
import org.openmdx.kernel.exception.BasicException;

/**
 * Result Target
 */
class ResultTarget extends AbstractTarget {

    /**
     * Constructor
     * @throws ResourceException 
     */
    ResultTarget(
        Database_2_0 database,
        QueryRecord request,
        ResultRecord delegate
    ) throws ResourceException {
        super(database);
        this.delegate = delegate;
        this.startPosition = Numbers.getValue(request.getPosition(), 0);
        this.resultSetLimit = database.getResultSetLimit();
        this.fetchSize = getFetchSize(database, request);
    }

    private final ResultRecord delegate;
    private final int startPosition;
    private final int resultSetLimit;
    private final int fetchSize;

    
    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.Target#getStartPosition()
     */
    @Override
    public int getStartPosition() {
        return this.startPosition;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.Target#isSaturated()
     */
    @Override
    public boolean isSaturated() {
        return fetchSize != FetchPlan.FETCH_SIZE_GREEDY && count() >= fetchSize;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.AbstractTarget#getObjectBatchSize()
     */
    @Override
    public int getObjectBatchSize() {
        final int maximalBatchSize = super.getObjectBatchSize();
        if(fetchSize == FetchPlan.FETCH_SIZE_GREEDY) {
            return maximalBatchSize;
        }
        final int optimalBatchSize = fetchSize - count();
        return 
            optimalBatchSize > 0 && optimalBatchSize < maximalBatchSize ? optimalBatchSize :
            maximalBatchSize;    
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.dataprovider.layer.persistence.jdbc.Target#propagate(org.
     * openmdx.base.rest.cci.ObjectRecord)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void propagate(
        ObjectRecord object
    )
        throws ServiceException {
        if (count() >= this.resultSetLimit) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TOO_LARGE_RESULT_SET,
                "Error when executing SQL statement",
                new BasicException.Parameter("count", count()),
                new BasicException.Parameter("limit", resultSetLimit));
        }
        delegate.add(object);
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.Target#close(boolean)
     */
    @Override
    public void close(
        boolean hasMore
    ) {
        delegate.setHasMore(hasMore);
        if(!hasMore) {
            final int count = count();
            if(startPosition == 0 || count > 0) {
                delegate.setTotal(startPosition + count);
            }
        }            
        close();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.Target#close(long)
     */
    @Override
    public void close(
        long total
    ) {
        delegate.setTotal(total);
        delegate.setHasMore(total > startPosition + count());
        close();
    }

    private static int getFetchSize(
        Database_2_0 database, 
        QueryRecord request
    ) throws ResourceException {
        final int fetchSize = Numbers.getValue(request.getSize(), FetchPlan.FETCH_SIZE_OPTIMAL);
        if (fetchSize == FetchPlan.FETCH_SIZE_GREEDY || fetchSize > 0) {
            return fetchSize;
        }
        if (fetchSize == FetchPlan.FETCH_SIZE_OPTIMAL) {
            return database.getOptimalFetchSize();
        }
        throw ResourceExceptions.toResourceException(
            new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Unsupported fetch size",
                new BasicException.Parameter("path", request.getResourceIdentifier()),
                new BasicException.Parameter("fetchSize", fetchSize),
                new BasicException.Parameter(
                    "supported",
                    FetchPlan.FETCH_SIZE_GREEDY + " (FETCH_SIZE_GREEDY)",
                    FetchPlan.FETCH_SIZE_OPTIMAL + " (FETCH_SIZE_OPTIMAL)",
                    ">0 (requested fetch size)")));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.AbstractTarget#getFetchSize()
     */
    @Override
    public int getFetchSize() {
        return this.fetchSize;
    }

}
