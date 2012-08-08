/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UnitOfWork_2.java,v 1.6 2008/02/19 13:57:33 hburger Exp $
 * Description: UnitOfWork_2 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 13:57:33 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */

package org.openmdx.base.object.spi;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.transaction.Synchronization;

/**
 * UnitOfWork_2
 * 
 * @since openMDX 2.0
 */
public class UnitOfWork_2 implements Transaction {

    /**
     * Constructor 
     */
    public UnitOfWork_2(
        PersistenceManager persistenceManager
    ) {
        this.persistenceManager = persistenceManager;
        PersistenceManagerFactory persistenceManagerFactory = persistenceManager.getPersistenceManagerFactory();
        this.restoreValues = persistenceManagerFactory.getRestoreValues();
        this.retainValues = persistenceManagerFactory.getRetainValues();
        this.nontransactionalWrite = persistenceManagerFactory.getNontransactionalWrite();
        this.nontransactionalRead = persistenceManagerFactory.getNontransactionalRead();
        this.optimistic = persistenceManagerFactory.getOptimistic();
    }
    
    /**
     * 
     */
    private PersistenceManager persistenceManager;

    /**
     * 
     */
    private boolean restoreValues;

    /**
     * 
     */
    private boolean retainValues;

    /**
     * 
     */
    private boolean nontransactionalWrite;

    /**
     * 
     */
    private boolean nontransactionalRead;

    /**
     * 
     */
    private boolean optimistic;

    /**
     * 
     */
    private Synchronization synchronization;
    

    //------------------------------------------------------------------------
    // Implements Transaction
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#begin()
     */
    public void begin() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#commit()
     */
    public void commit() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#rollback()
     */
    public void rollback() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#isActive()
     */
    public boolean isActive(
    ){
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getRollbackOnly()
     */
    public boolean getRollbackOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setRollbackOnly()
     */
    public void setRollbackOnly() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setNontransactionalRead(boolean)
     */
    public void setNontransactionalRead(
        boolean nontransactionalRead
    ) {
        this.nontransactionalRead = nontransactionalRead;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getNontransactionalRead()
     */
    public boolean getNontransactionalRead() {
        return this.nontransactionalRead;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setNontransactionalWrite(boolean)
     */
    public void setNontransactionalWrite(
        boolean nontransactionalWrite
    ) {
        this.nontransactionalWrite = nontransactionalWrite;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getNontransactionalWrite()
     */
    public boolean getNontransactionalWrite() {
        return this.nontransactionalWrite;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setRetainValues(boolean)
     */
    public void setRetainValues(
        boolean retainValues
    ) {
        this.retainValues = retainValues;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getRetainValues()
     */
    public boolean getRetainValues() {
        return this.retainValues;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setRestoreValues(boolean)
     */
    public void setRestoreValues(
        boolean restoreValues
    ) {
        this.restoreValues = restoreValues;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getRestoreValues()
     */
    public boolean getRestoreValues() {
        return this.restoreValues;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setOptimistic(boolean)
     */
    public void setOptimistic(boolean optimistic) {
        this.optimistic = optimistic;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getOptimistic()
     */
    public boolean getOptimistic() {
        return this.optimistic;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setSynchronization(javax.transaction.Synchronization)
     */
    public void setSynchronization(Synchronization sync) {
        this.synchronization = sync;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getSynchronization()
     */
    public Synchronization getSynchronization(
    ){
        return this.synchronization;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager(
    ) {
        return this.persistenceManager;
    }

}
