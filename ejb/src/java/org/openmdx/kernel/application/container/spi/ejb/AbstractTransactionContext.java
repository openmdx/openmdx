/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractTransactionContext.java,v 1.2 2009/08/25 17:23:05 hburger Exp $
 * Description: TransactionContext
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/08/25 17:23:05 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.application.container.spi.ejb;

import javax.ejb.TransactionAttributeType;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRequiredException;



/**
 * TransactionContext
 */
public abstract class AbstractTransactionContext {

    /**
     * Constructor
     */
    protected AbstractTransactionContext(
        TransactionManager transactionManager
    ){
        this.transactionManager = transactionManager;
    }

    /**
     * 
     */
    private final TransactionManager transactionManager;

    /**
     * 
     */
    private Transaction suspendedTransaction = null;
    
    /**
     * 
     */
    private boolean transactionStarted = false;

    /**
     * Start an EJB business method invocation
     * @param transactionAttribute TODO
     * 
     * @throws NotSupportedException 
     * @throws SystemException 
     * @throws InvalidTransactionException 
     * @throws TransactionRequiredException 
     */
    protected void start(
        TransactionAttributeType transactionAttribute
    ) throws TransactionRequiredException, InvalidTransactionException, SystemException, NotSupportedException{
        if(this.transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION) {
            if(
                transactionAttribute == TransactionAttributeType.REQUIRED ||
                transactionAttribute == TransactionAttributeType.REQUIRES_NEW
            ){
                this.transactionManager.begin();
                this.transactionStarted = true;
            } else if(
                transactionAttribute == TransactionAttributeType.MANDATORY
            ) throw new TransactionRequiredException(
                "A method with transaction attribute Mandatory was called without a transaction context"
            );
        } else {
            if(
                transactionAttribute == TransactionAttributeType.NOT_SUPPORTED
            ) {
                this.suspendedTransaction = transactionManager.suspend();
            } else if(
                transactionAttribute == TransactionAttributeType.REQUIRES_NEW
            ) {
                this.suspendedTransaction = this.transactionManager.suspend();
                this.transactionManager.begin();
                this.transactionStarted = true;
            } else if(
                transactionAttribute == TransactionAttributeType.NEVER
            ) throw new InvalidTransactionException(
                "A method with transaction attribute Never was called with a transaction context"
            );
        }
    }

    /**
     * Terminate an EJB business method invocation
     * 
     * @throws SystemException 
     * @throws IllegalStateException 
     * @throws InvalidTransactionException 
     * @throws HeuristicRollbackException 
     * @throws HeuristicMixedException 
     * @throws RollbackException 
     * @throws SecurityException 
     */
    protected void endSuccess(
    ) throws InvalidTransactionException, IllegalStateException, SystemException, SecurityException, RollbackException, HeuristicMixedException, HeuristicRollbackException{
        if(this.transactionStarted) try {
            if(this.transactionManager.getStatus() == Status.STATUS_MARKED_ROLLBACK){
                this.transactionManager.rollback();
            } else {
                this.transactionManager.commit();
            }
        } finally {
            if(this.suspendedTransaction != null) this.transactionManager.resume(
                this.suspendedTransaction
            );
        }                    
    }
    
    /**
     * Abort an EJB business method invocation
     * 
     * @throws SystemException 
     * @throws SecurityException 
     * @throws IllegalStateException 
     * @throws InvalidTransactionException 
     */
    protected void endFail(
    ) throws IllegalStateException, SecurityException, SystemException, InvalidTransactionException{
        if(this.transactionStarted) try {
            this.transactionManager.rollback();
        } finally {
            if(this.suspendedTransaction != null) this.transactionManager.resume(
                this.suspendedTransaction
            );
//      } else {
//          this.transactionManager.setRollbackOnly();
        }                    
    }

}
