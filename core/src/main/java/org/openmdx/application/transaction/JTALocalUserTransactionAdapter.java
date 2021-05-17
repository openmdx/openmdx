/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: JTA User Transaction Adapter
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
package org.openmdx.application.transaction;

import javax.jdo.JDODataStoreException;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransactionException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.openmdx.base.transaction.Status;
import org.openmdx.base.transaction.LocalUserTransaction;
import org.openmdx.kernel.exception.BasicException;

/**
 * JTA User Transaction Adapter
 * <p>
 * Instantiated reflectively by org.openmdx.base.accessor.rest.spi.UserTransactions
 * 
 * @see org.openmdx.base.accessor.rest.spi.LocalUserTransactionAdapters
 */
public class JTALocalUserTransactionAdapter implements LocalUserTransaction {

    /**
     * Constructor 
     *
     * Invoked reflectively by org.openmdx.base.accessor.rest.spi.UserTransactions
     * 
     * @see org.openmdx.base.accessor.rest.spi.LocalUserTransactionAdapters.getJTAUserTransactionAdapter()
     */
    public JTALocalUserTransactionAdapter(
    ) throws ResourceException{
        this.delegate = UserTransactions.getUserTransaction();
    }

    /**
     * The optional user transaction object
     */
    private final UserTransaction delegate;

    /**
     * @throws ResourceException
     * @see javax.resource.spi.LocalTransaction#begin()
     */
//  @Override
    public void begin(
    ) throws ResourceException {
        try {
            this.delegate.begin();
        } catch (NotSupportedException exception) {
            throw new LocalTransactionException(
                "Transaction start failure",
                exception
            );
        } catch (SystemException exception) {
            throw new LocalTransactionException(
                "Transaction start failure",
                exception
            );
        }
    }

    /**
     * @throws ResourceException
     * @see javax.resource.spi.LocalTransaction#commit()
     */
//  @Override
    public void commit(
    ) throws ResourceException {
        try {
            this.delegate.commit();
        } catch (SystemException exception) {
            throw new LocalTransactionException(
                "Transaction commit failure",
                exception
            );
        } catch (SecurityException exception) {
            throw new LocalTransactionException(
                "Transaction commit failure",
                exception
            );
        } catch (IllegalStateException exception) {
            throw new LocalTransactionException(
                "Transaction commit failure",
                exception
            );
        } catch (RollbackException exception) {
            throw new LocalTransactionException(
                "Transaction commit failure",
                exception
            );
        } catch (HeuristicMixedException exception) {
            throw new LocalTransactionException(
                "Transaction commit failure",
                exception
            );
        } catch (HeuristicRollbackException exception) {
            throw new LocalTransactionException(
                "Transaction commit failure",
                exception
            );
        }
    }

    /**
     * @throws ResourceException
     * @see javax.resource.spi.LocalTransaction#rollback()
     */
//  @Override
    public void rollback(
    ) throws ResourceException {
        try {
            this.delegate.rollback();
        } catch (IllegalStateException exception) {
            throw new LocalTransactionException(
                "Transaction rollback failure",
                exception
            );
        } catch (SecurityException exception) {
            throw new LocalTransactionException(
                "Transaction rollback failure",
                exception
            );
        } catch (SystemException exception) {
            throw new LocalTransactionException(
                "Transaction rollback failure",
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.UserTransaction_2_0#setRollbackOnly()
     */
//  @Override
    public void setRollbackOnly() {
        try {
            this.delegate.setRollbackOnly();
        } catch (IllegalStateException exception) {
            throw new JDODataStoreException(
                "Could not set the transaction into rollback-only mode",
                BasicException.newEmbeddedExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE
                )
            );
        } catch (SystemException exception) {
            throw new JDODataStoreException(
                "Could not set the transaction into rollback-only mode",
                BasicException.newEmbeddedExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSACTION_FAILURE
                )
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.UserTransaction_2_0#isRollbackOnly()
     */
//  @Override
    public boolean isRollbackOnly(
    ) {
        try {
            return this.delegate.getStatus() == Status.STATUS_MARKED_ROLLBACK.ordinal();
        } catch (SystemException exception) {
            throw new JDODataStoreException(
                "Rollback-only query failed",
                BasicException.newEmbeddedExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSACTION_FAILURE
                )
            );
        }
    }

} 
