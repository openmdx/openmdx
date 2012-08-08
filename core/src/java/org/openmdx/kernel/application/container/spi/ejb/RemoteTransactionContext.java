/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RemoteTransactionContext.java,v 1.2 2008/03/27 19:16:28 hburger Exp $
 * Description: RemoteTransactionContext
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/27 19:16:28 $
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

import java.rmi.RemoteException;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRolledbackException;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;


/**
 * RemoteTransactionContext
 */
public class RemoteTransactionContext
    extends AbstractTransactionContext 
{

    /**
     * Constructor
     * 
     * @param transactionManager
     * @param transactionAttribute
     * 
     * @throws RemoteException 
     */
    public RemoteTransactionContext(
        TransactionManager transactionManager,
        TransactionAttribute transactionAttribute
    ) throws RemoteException {
        super(transactionManager);
        try {
            super.start(transactionAttribute);
        } catch (SystemException exception) {
            throw new RemoteException("Transaction Management Failure", exception);
        } catch (NotSupportedException exception) {
            throw new RemoteException("Transaction Management Failure", exception);
        }
    }

    /**
     * Abort an EJB business method invocation
     * 
     * @param cause a BasicException representing the throwable causing the abort
     * 
     * @return the exception to be thrown
     */
    public RemoteException end(
         BasicException cause
    ){
        try {
            super.endFail();
        } catch (Exception exception) {
            SysLog.error("Transaction Management Failure", exception);
        }
        SysLog.warning("Caught non-application exception", cause);
        return initCause(
             new TransactionRolledbackException("Caught non-application exception"),
             cause
        );
    }

    /**
     * Terminate an EJB business method invocation
     * 
     * @throws RemoteException
     */
    public void end(
    ) throws RemoteException {
        try {
            super.endSuccess();
        } catch (SystemException exception) {
            throw new RemoteException("Transaction Management Failure", exception);
        } catch (RollbackException exception) {
            throw initCause(
                new TransactionRolledbackException(
                    "The transaction has been rolled back instead of committed"
                ), 
                exception
            );
        } catch (HeuristicMixedException exception) {
            throw new RemoteException(
                "A heuristic decision was made and some relevant updates have been committed and others have been rolled back", 
                exception
            );
        } catch (HeuristicRollbackException exception) {
            throw initCause(
                new TransactionRolledbackException(
                    "A heuristic decision was made and all relevant updates have been rolled back"
                ), 
                exception
            );
        }
    }

    /**
     * Initialize a remote exception's detail field
     * 
     * @param exception
     * @param detail
     * 
     * @return the original exception
     */
    private static final RemoteException initCause(
        RemoteException exception,
        Throwable detail
    ){
        exception.detail = detail;
        return exception;
    }

}
