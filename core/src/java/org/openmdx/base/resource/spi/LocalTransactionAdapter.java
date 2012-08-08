/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: LocalTransactionAdapter.java,v 1.3 2009/06/08 17:07:22 hburger Exp $
 * Description: Local Transaction Adapter 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/08 17:07:22 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.base.resource.spi;

import javax.resource.ResourceException;
import javax.resource.cci.LocalTransaction;
import javax.resource.spi.LocalTransactionException;
import javax.transaction.Synchronization;

import org.openmdx.kernel.exception.BasicException;

/**
 * Local Transaction Adapter
 */
public class LocalTransactionAdapter implements TransactionManager {

    /**
     * Constructor 
     *
     * @param userTransaction
     */
    public LocalTransactionAdapter(
        LocalTransaction localTransaction
    ){
        this.delegate = localTransaction;
    }

    /**
     * The <code>UserTransaction</code> to delegate to
     */
    private final LocalTransaction delegate;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.OptimisticTransaction#commit(javax.transaction.Synchronization)
     */
    public void commit(
        Synchronization synchronization
    ) throws LocalTransactionException {
        try {
            this.delegate.begin();
            LocalTransactionException rollback;
            try {
                synchronization.beforeCompletion();
                rollback = null;
            } catch (RuntimeException exception) {
                rollback = ResourceExceptions.initHolder(
                    new LocalTransactionException(
                        "Unit of work set to rollback-only during commit",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ROLLBACK,
                            new BasicException.Parameter("phase", "PREPARING")
                        )
                    )
                );
            }
            if(rollback == null) {
                try {
                    this.delegate.commit();
                } catch (ResourceException exception) {
                    BasicException cause = BasicException.toExceptionStack(exception);
                    throw ResourceExceptions.initHolder(
                        new LocalTransactionException(
                            "The transaction could not be commited",
                            BasicException.newEmbeddedExceptionStack(
                                cause,
                                cause.getExceptionDomain(),
                                cause.getExceptionCode(),
                                new BasicException.Parameter("phase", "COMMITTING")
                            )
                        )
                    );
                }
            } else {
                try {
                    this.delegate.rollback();
                } catch (ResourceException exception) {
                    throw ResourceExceptions.initHolder(
                        rollback = ResourceExceptions.initHolder(
                            new LocalTransactionException(
                                "The transaction could not be rolled back properly",
                                BasicException.newEmbeddedExceptionStack(
                                    exception,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.TRANSACTION_FAILURE,
                                    new BasicException.Parameter("phase", "ROLLING_BACK")
                                ).getCause(
                                    null // initial cause
                                ).initCause(
                                    rollback
                                )
                             )
                         )
                    );
                }
                throw rollback;
            }
        } catch (LocalTransactionException exception) {
            throw exception;
        } catch (ResourceException exception) {
            throw ResourceExceptions.initHolder(
                new LocalTransactionException(
                    "The transaction manager encountered an unexpected error condition",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSACTION_FAILURE,
                        new BasicException.Parameter("phase", "UNKOWN")
                    )
                )
            );
        }
    }

}
