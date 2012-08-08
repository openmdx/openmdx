/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CallbackContext.java,v 1.5 2007/08/20 09:37:19 hburger Exp $
 * Description: Callback Context
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/08/20 09:37:19 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
package org.openmdx.weblogic.security.rmi;

import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.openmdx.kernel.security.ExecutionContext;


import weblogic.security.Security;
import weblogic.security.SubjectUtils;
import weblogic.transaction.TxHelper;


/**
 * This Security Context suspend a WebLogic transaction while executing an action.
 */
public class CallbackContext implements ExecutionContext {
    
    /**
     * WebLogic's Transaction Manager Singleton
     */
    private static final TransactionManager transactionManager = TxHelper.getTransactionManager();

    /**
     * Constructor
     */
    public CallbackContext() {
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.Context#execute(java.security.PrivilegedAction)
     */
    @SuppressWarnings("unchecked")
    public Object execute(
        PrivilegedAction action
    ) {
        Transaction transaction = null;
        try {
            transaction = transactionManager.suspend();
        } catch (SystemException securityException) {
            throw (SecurityException) new SecurityException(
                "Could not suspend transaction"
            ).initCause(
                securityException
            );
        }
        try {
            return Security.runAs(
                SubjectUtils.getAnonymousUser(),
                action
            );
        } finally {
            if(transaction != null) try {
                transactionManager.resume(transaction);
            } catch (Exception resumeTransactionException) {
                throw new RuntimeException(
                    "Could not resume transaction",
                    resumeTransactionException
                );
            }
        }        
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.Context#execute(java.security.PrivilegedExceptionAction)
     */
    @SuppressWarnings("unchecked")
    public Object execute(
        PrivilegedExceptionAction action
    ) throws PrivilegedActionException {
        Transaction transaction = null;
        try {
            transaction = transactionManager.suspend();
        } catch (SystemException securityException) {
            throw (SecurityException) new SecurityException(
                "Could not suspend transaction"
            ).initCause(
                securityException
            );
        }
        try {
            return Security.runAs(
                SubjectUtils.getAnonymousUser(),
                action
            );
        } catch (Exception exception) {
            throw new PrivilegedActionException(exception);
        } finally {
            if(transaction != null) try {
                transactionManager.resume(transaction);
            } catch (Exception resumeTransactionException) {
                throw new RuntimeException(
                    "Could not resume transaction",
                    resumeTransactionException
                );
            }
        }        
    }

    
    //------------------------------------------------------------------------
    // Implements Connection
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.resource.Connection#close()
     */
    public void close() {
    }

}
