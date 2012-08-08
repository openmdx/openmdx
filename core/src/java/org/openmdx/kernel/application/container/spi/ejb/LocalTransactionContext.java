/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LocalTransactionContext.java,v 1.4 2008/10/08 14:21:32 hburger Exp $
 * Description: LocalTransactionContext
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/08 14:21:32 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.application.container.spi.ejb;

import java.lang.reflect.UndeclaredThrowableException;

import javax.ejb.EJBException;
import javax.ejb.TransactionRequiredLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRequiredException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocalTransactionContext
 */
class LocalTransactionContext
    extends AbstractTransactionContext 
{

    /**
     * Constructor
     * 
     * @param transactionManager
     * @param transactionAttribute
     */
    LocalTransactionContext(
        TransactionManager transactionManager,
        TransactionAttribute transactionAttribute
     ){
        super(transactionManager);
        try {
            super.start(transactionAttribute);
        } catch (TransactionRequiredException exception) {
            throw new TransactionRequiredLocalException(exception.getMessage());
        } catch (InvalidTransactionException exception) {
            throw new EJBException("Transaction Management Failure", exception);
        } catch (SystemException exception) {
            throw new EJBException("Transaction Management Failure", exception);
        } catch (NotSupportedException exception) {
            throw new EJBException("Transaction Management Failure", exception);
        }
    }

    /**
     * Abort an EJB business method invocation
     * 
     * @param cause the throwable causing the abort
     * 
     * @return the exception to be thrown
     * 
     * @throws EJBException
     */
    EJBException end(
         Throwable cause
    ){
        Logger logger = LoggerFactory.getLogger(LocalTransactionContext.class);
        try {
            super.endFail();
        } catch (Exception exception) {
            logger.error("Transaction Management Failure", exception);
        }
        logger.error("Caught non-application exception", cause);
        return new TransactionRolledbackLocalException(
             "Caught non-application exception",
             cause instanceof Exception ? (Exception)cause : new UndeclaredThrowableException(cause)
        );
    }

    /**
     * Terminate an EJB business method invocation
     * 
     * @throws EJBException
     */
    void end(
    ){
        try {
            super.endSuccess();
        } catch (InvalidTransactionException exception) {
            throw new EJBException("Transaction Management Failure", exception);
        } catch (SystemException exception) {
            throw new EJBException("Transaction Management Failure", exception);
        } catch (RollbackException exception) {
            throw new TransactionRolledbackLocalException(
                "The transaction has been rolled back instead of committed", 
                exception
            );
        } catch (HeuristicMixedException exception) {
            throw new EJBException(
                "A heuristic decision was made and some relevant updates have been committed and others have been rolled back",
                exception
            );
        } catch (HeuristicRollbackException exception) {
            throw new TransactionRolledbackLocalException(
                 "A heuristic decision was made and all relevant updates have been rolled back",
                 exception
            );
        }
    }

}
