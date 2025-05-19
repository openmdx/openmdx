/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Local Transaction Support
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.LocalTransaction;
import javax.resource.spi.LocalTransactionException;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.LocalTransaction;
import jakarta.resource.spi.LocalTransactionException;
#endif

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
     * @param delegate a {@code LocalTransaction}
     *
     * @return the local transaction adapter
     *
     * @throws ResourceException
     */
    public static #if JAVA_8 javax #else jakarta #endif.resource.spi.LocalTransaction getLocalTransaction(
        LocalTransaction delegate
    ) throws ResourceException {
        return new LocalTransactionWrapper(delegate);
    }

    /**
     * Acquire a persistence manager adapter
     *
     * @param delegate the {@code Transaction}'s owner
     *
     * @return a persistence manager adapter
     *
     * @throws ResourceException
     */
    public static LocalTransaction getLocalTransaction(
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
    static class PersistenceManagerWrapper implements LocalTransaction {

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

        @Override
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

        @Override
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

        @Override
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
    static class LocalTransactionWrapper implements #if JAVA_8 javax #else jakarta #endif.resource.spi.LocalTransaction {

        /**
         * Constructor
         *
         * @param delegate
         */
        LocalTransactionWrapper(
            LocalTransaction delegate
        ){
            this.delegate = delegate;
        }

        /**
         * The connector's local transaction
         */
        private final LocalTransaction delegate;

        @Override
        public void begin(
        ) throws ResourceException {
            this.delegate.begin();
        }

        @Override
        public void commit(
        ) throws ResourceException {
            this.delegate.commit();
        }

        @Override
        public void rollback(
        ) throws ResourceException {
            this.delegate.rollback();
        }

    }

}
