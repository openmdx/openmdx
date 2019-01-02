/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Container Managed Unit Of Work Synchronization 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2017, OMEX AG, Switzerland
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
package org.openmdx.base.transaction;

import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * A before completion failure leads to rollback-only.
 */
public class ContainerManagedUnitOfWorkSynchronization implements Synchronization {

    /**
     * The constructor is used by the enlist method only
     * 
     * @param delegate
     *            the synchronization object of a container managed unit of work
     */
    private ContainerManagedUnitOfWorkSynchronization(
        CloseableSynchronization delegate
    ) {
        this.delegate = delegate;
        this.callbackClassLoader = getContextClassLoader();
    }

    private final CloseableSynchronization delegate;
    private final ClassLoader callbackClassLoader;

    /**
     * Enlist the synchronization object of a container managed unit of work
     * 
     * @param delegate
     *            the synchronization object of a container managed unit of work
     */
    public static void enlist(
        CloseableSynchronization delegate
    ) {
        final TransactionSynchronizationRegistry transactionSynchronizationRegistry = TransactionSynchronizationRegistryFinder
            .getTransactionSynchronizationRegistry();
        final int transactionStatus = transactionSynchronizationRegistry.getTransactionStatus();
        if (transactionStatus != javax.transaction.Status.STATUS_ACTIVE) {
            throw new JmiServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "A container managed persistence manager can be created in an active transaction only",
                new BasicException.Parameter("status", transactionStatus)
            ).log();
        }
        SysLog.detail("Enlisting unit of work in current transaction", transactionSynchronizationRegistry.getTransactionKey());
        transactionSynchronizationRegistry.registerInterposedSynchronization(
            new ContainerManagedUnitOfWorkSynchronization(delegate)
        );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.transaction.Synchronization#afterCompletion(int)
     */
    @Override
    public void afterCompletion(
        int status
    ) {
        final Status transactionStatus = Status.valueOf(status);
        if (delegate.isClosed()) {
            SysLog.detail("No after completion callback invoked as the persistence manager is already closed", transactionStatus);
        } else {
            SysLog.detail("Invoking the unit of work's after completion callback", transactionStatus);
            final ClassLoader originalClassLoader = setCallbackClassLoader();
            try {
                delegate.afterCompletion(transactionStatus);
            } catch (RuntimeException exception) {
                SysLog.detail(
                    "After completion callback threw an exception",
                    exception.getMessage()
                );
                throw new RuntimeServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.GENERIC,
                    "After completion callback threw an exception",
                    new BasicException.Parameter("status", transactionStatus)
                );    
            } finally {
                resetOriginalClassLoader(originalClassLoader);
            }
            SysLog.detail("After completion callback successfully completed");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.transaction.Synchronization#beforeCompletion()
     */
    @Override
    public void beforeCompletion() {
        if (delegate.isClosed()) {
            SysLog.detail(
                "No before completion callback invoked as the persistence manager is already closed"
            );
        } else {
            SysLog.detail("Invoking the unit of work's before completion callback");
            final ClassLoader originalClassLoader = setCallbackClassLoader();
            try {
                delegate.beforeCompletion();
            } catch (RuntimeException exception) {
                SysLog.detail(
                    "Transaction is expected to be set to rollback-only due this before completion failure",
                    exception.getMessage()
                );
                throw new RuntimeServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ROLLBACK,
                    "Transaction is expected to be set to rollback-only due this before completion failure"
                );    
            } finally {
                resetOriginalClassLoader(originalClassLoader);
            }
            SysLog.detail("Before completion callback successfully completed");
        }
    }

    /**
     * Set the current thread's context class loader to the callback class loader
     * 
     * @return the current thread's actual class loader
     */
    private ClassLoader setCallbackClassLoader() {
        final ClassLoader actualClassLoader = getContextClassLoader();
        if(actualClassLoader != this.callbackClassLoader) {
            SysLog.detail(
                "Switch to the callback context class loader",
                System.identityHashCode(callbackClassLoader)
            );
            setContextClassLoader(callbackClassLoader);
        }
        return actualClassLoader;
    }

    /**
     * Reset the current thread's context class loader to the original class loader
     * 
     * @param originalClassLoader the current thread's original class loader
     */
    private void resetOriginalClassLoader(
        ClassLoader originalClassLoader
    ) {
        if(originalClassLoader != this.callbackClassLoader) {
            SysLog.detail(
                "Switch back to the original context class loader",
                System.identityHashCode(originalClassLoader)
            );
            setContextClassLoader(originalClassLoader);
        }
    }
    
    /**
     * Determine the the current thread's actual context class loader
     * 
     * @return the current thread's context class loader
     */
    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Replace the current thread's actual context class loader
     * 
     * @param classLoader the required context class loader
     */
    private static void setContextClassLoader(
        ClassLoader classLoader
    ) {
        Thread.currentThread().setContextClassLoader(classLoader);
    }

}
