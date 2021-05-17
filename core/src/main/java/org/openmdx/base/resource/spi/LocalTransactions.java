/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Local Transaction Support
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2012, OMEX AG, Switzerland
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

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransactionException;

import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.UnitOfWork;


/**
 * Transaction Factory
 */
public class LocalTransactions {

    /**
     * Constructor 
     */
    private LocalTransactions() {
        // Avoid instantiation
    }

    /**
     * Acquire a local transaction adapter
     * 
     * @param delegate a <code>LocalTransaction</code>
     * 
     * @return the local transaction adapter
     * 
     * @throws ResourceException
     */
    public static javax.resource.spi.LocalTransaction getLocalTransaction(
        javax.resource.cci.LocalTransaction delegate
    ) throws ResourceException {
        return new LocalTransactionWrapper(delegate);
    }
    
    /**
     * Acquire a persistence manager adapter
     * 
     * @param delegate the <code>Transaction</code>'s owner
     * 
     * @return a persistence manager adapter
     * 
     * @throws ResourceException
     */
    public static javax.resource.cci.LocalTransaction getLocalTransaction(
        PersistenceManager delegate
    ) throws ResourceException {
        return new PersistenceManagerWrapper(delegate);
    }

    
    //------------------------------------------------------------------------
    // Class PersistenceManagerWrapper
    //------------------------------------------------------------------------

    /**
     * Adapter delegating to a persistence manager's current transaction
     */
    static class PersistenceManagerWrapper implements  javax.resource.cci.LocalTransaction {

        /**
         * Constructor 
         *
         * @param persistenceManager
         */
        PersistenceManagerWrapper(PersistenceManager persistenceManager) {
            this.persistenceManager = persistenceManager;
        }

        /**
         * A persistence manager
         */
        private final PersistenceManager persistenceManager;

        private UnitOfWork currentUnitOfWork(){
            return PersistenceHelper.currentUnitOfWork(persistenceManager);
        }
        
        /* (non-Javadoc)
         * @see javax.resource.cci.LocalTransaction#begin()
         */
        public void begin(
        ) throws ResourceException {
            try {
                currentUnitOfWork().begin();
            } catch (JDOException exception) {
                throw new LocalTransactionException(
                    "Transaction start failure",
                    exception
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.LocalTransaction#commit()
         */
        public void commit(
        ) throws ResourceException {
            try {
                currentUnitOfWork().commit();
            } catch (JDOException exception) {
                throw new LocalTransactionException(
                    "Transaction commit failure",
                    exception
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.LocalTransaction#rollback()
         */
        public void rollback(
        ) throws ResourceException {
            try {
                currentUnitOfWork().rollback();
            } catch (JDOException exception) {
                throw new LocalTransactionException(
                    "Transaction rollback failure",
                    exception
                );
            }
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class LocalTransactionWrapper
    //------------------------------------------------------------------------
    
    /**
     * Adapter delegating to the connector's transaction
     */
    static class LocalTransactionWrapper implements  javax.resource.spi.LocalTransaction {

        /**
         * Constructor 
         *
         * @param delegate
         */
        LocalTransactionWrapper(
            javax.resource.cci.LocalTransaction delegate
        ){
            this.delegate = delegate;
        }

        /**
         * The connector's local transaction
         */
        private final javax.resource.cci.LocalTransaction delegate;
        
        /* (non-Javadoc)
         * @see javax.resource.spi.LocalTransaction#begin()
         */
        public void begin(
        ) throws ResourceException {
            this.delegate.begin();
        }

        /* (non-Javadoc)
         * @see javax.resource.spi.LocalTransaction#commit()
         */
        public void commit(
        ) throws ResourceException {
            this.delegate.commit();
        }

        /* (non-Javadoc)
         * @see javax.resource.spi.LocalTransaction#rollback()
         */
        public void rollback(
        ) throws ResourceException {
            this.delegate.rollback();
        }
        
    }
    
}
