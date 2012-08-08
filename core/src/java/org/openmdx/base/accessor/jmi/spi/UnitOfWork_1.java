/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: UnitOfWork_1.java,v 1.5 2008/09/10 08:55:23 hburger Exp $
 * Description: UnitOfWork_1 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:23 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.UnitOfWork_1_2;
import org.openmdx.kernel.exception.BasicException;

/**
 * UnitOfWork_1
 */
class UnitOfWork_1
implements UnitOfWork_1_2
{

    /**
     * Constructor 
     *
     * @param persistenceManager
     */
    UnitOfWork_1(
        PersistenceManager persistenceManager
    ) {
        this.persistenceManager = persistenceManager;
    }

    /**
     * 
     */
    private final PersistenceManager persistenceManager;

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#begin()
     */
    public void begin(
    ) throws ServiceException {
        try {
            this.persistenceManager.currentTransaction().begin();
        } catch (JDOException exception) {
            throw new ServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#commit()
     */
    public void commit(
    ) throws ServiceException {
        try {
            this.persistenceManager.currentTransaction().commit();
        } catch (JDOException exception) {
            throw new ServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#isActive()
     */
    public boolean isActive() {
        try {
            return this.persistenceManager.currentTransaction().isActive();
        } catch (JDOException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#isOptimistic()
     */
    public boolean isOptimistic() {
        try {
            return this.persistenceManager.currentTransaction().getOptimistic();
        } catch (JDOException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#isTransactional()
     */
    public boolean isTransactional() {
        return !this.persistenceManager.currentTransaction().getNontransactionalWrite();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#rollback()
     */
    public void rollback(
    ) throws ServiceException {
        try {
            this.persistenceManager.currentTransaction().rollback();
        } catch (JDOException exception) {
            throw new ServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#verify()
     */
    public void verify(
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_IMPLEMENTED,
            "Verification only mode not supported"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.Synchronization_1_0#afterBegin()
     */
    public void afterBegin(
    ) throws ServiceException {
        getSynchronization();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.Synchronization_1_0#afterCompletion(boolean)
     */
    public void afterCompletion(
        boolean committed
    ) throws ServiceException {
        try {
            getSynchronization().afterCompletion(
                committed ? Status.STATUS_COMMITTED : Status.STATUS_ROLLEDBACK
            );
        } catch (RuntimeException exception) {
            throw new ServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.Synchronization_1_0#beforeCompletion()
     */
    public void beforeCompletion(
    ) throws ServiceException {
        try {
            getSynchronization().beforeCompletion();
        } catch (RuntimeException exception) {
            throw new ServiceException(exception);
        }
    }

    private Synchronization getSynchronization() throws ServiceException{
        try {
            return (Synchronization)this.persistenceManager.currentTransaction();
        } catch (ClassCastException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Synchronization is not supported by the delegate",
                new BasicException.Parameter("class", this.persistenceManager.currentTransaction().getClass().getName())
            );
        }

    }


    //------------------------------------------------------------------------
    // Implements UnitOfWork_1_2
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_2#getRollbckOnly()
     */
    public boolean getRollbackOnly(
    ) {
        return this.persistenceManager.currentTransaction().getRollbackOnly();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_2#setRollbackOnly()
     */
    public void setRollbackOnly() {
        this.persistenceManager.currentTransaction().setRollbackOnly();
    }


}
