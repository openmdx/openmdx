/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LightweightUserTransaction.java,v 1.1 2009/09/07 15:00:30 hburger Exp $
 * Description: Lightweight User Transaction
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/07 15:00:30 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2009, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * User Transaction Implementation
 */
public class LightweightUserTransaction implements UserTransaction {

	/**
     * Constructor
     * 
	 * @param transactionManager 
	 */
	public LightweightUserTransaction(
        TransactionManager transactionManager
    ){
        this.transactionManager = transactionManager;
	}
    
    /**
     * The transaction manager to delegate to.
     */
    private final TransactionManager transactionManager;    

    /* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#begin()
	 */
	public void begin() throws NotSupportedException, SystemException {
        this.transactionManager.begin();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#commit()
	 */
	public void commit(
	) throws RollbackException, HeuristicMixedException,
			HeuristicRollbackException, SecurityException,
			IllegalStateException, SystemException 
	{
        this.transactionManager.commit();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#rollback()
	 */
	public void rollback(
	) throws IllegalStateException, SecurityException, SystemException {
        this.transactionManager.rollback();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#setRollbackOnly()
	 */
	public void setRollbackOnly() throws IllegalStateException, SystemException {
        this.transactionManager.setRollbackOnly();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#getStatus()
	 */
	public int getStatus() throws SystemException {
        return this.transactionManager.getStatus();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#setTransactionTimeout(int)
	 */
	public void setTransactionTimeout(int timeout) throws SystemException {
        this.transactionManager.setTransactionTimeout(timeout);
	}

}
