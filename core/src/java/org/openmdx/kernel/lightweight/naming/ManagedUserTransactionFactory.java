/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ManagedUserTransactionFactory.java,v 1.1 2011/06/12 12:56:00 hburger Exp $
 * Description: Managed User Transaction Factory 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/06/12 12:56:00 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.naming;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.openmdx.kernel.lightweight.transaction.LightweightTransactionManager;
import org.openmdx.kernel.lightweight.transaction.LightweightTransactionSynchronizationRegistry;
import org.openmdx.kernel.lightweight.transaction.LightweightUserTransaction;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.naming.ComponentEnvironment;

/**
 * Managed User Transaction Factory
 */
public class ManagedUserTransactionFactory implements ObjectFactory {

    /**
     * Tells whether a the lightweight transaction manager factory has been
     * registered as component environment entry.
     */
    private static boolean transactionManagerFactoryRegistered = false;
    
    /**
     * Tells whether a the lightweight transaction synchronization registry 
     * factory has been registered as component environment entry.
     */
    private static boolean transactionSynchronizationRegistryFactoryRegistered = false;
    
    /* (non-Javadoc)
     * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
     */
//  @Override
    public Object getObjectInstance(
        Object obj,
        Name name,
        Context nameCtx,
        Hashtable<?, ?> environment
    ) throws Exception {
        if (obj instanceof Reference) {
            Reference ref = (Reference) obj;
            if(!transactionManagerFactoryRegistered) {
                RefAddr configuration = ref.get("registerTransactionManagerFactory");
                if (
                    configuration == null || Boolean.parseBoolean(configuration.getContent().toString())){
                    ComponentEnvironment.register(new TransactionManagerFactory());
                    transactionManagerFactoryRegistered = true;
                }
            }
            if(!transactionSynchronizationRegistryFactoryRegistered) {
                RefAddr configuration = ref.get("registerTransactionSynchronizationRegistryFactory");
                if (configuration == null || Boolean.parseBoolean(configuration.getContent().toString())){
                    ComponentEnvironment.register(new TransactionSynchronizationRegistryFactory());
                    transactionSynchronizationRegistryFactoryRegistered = true;
                }
            }
        }
        return LightweightUserTransaction.getInstance();
    }

    
    //------------------------------------------------------------------------
    // Class TransactionManagerFactory
    //------------------------------------------------------------------------
    
    /**
     * Transaction Manager Factory
     */
    protected static class TransactionManagerFactory implements Factory<TransactionManager> {

        /* (non-Javadoc)
         * @see org.openmdx.kernel.loading.Factory#instantiate()
         */
    //  @Override
        public TransactionManager instantiate(
        ){
            return LightweightTransactionManager.getInstance();
        }

        /* (non-Javadoc)
         * @see org.openmdx.kernel.loading.Factory#getInstanceClass()
         */
    //  @Override
        public Class<? extends TransactionManager> getInstanceClass(
        ){
            return TransactionManager.class;
        }

    }

    
    //------------------------------------------------------------------------
    // Class TransactionSynchronizationRegistryFactory
    //------------------------------------------------------------------------
    
    /**
     * Transaction Synchronization Registry Factory
     */
    protected static class TransactionSynchronizationRegistryFactory implements Factory<TransactionSynchronizationRegistry> {

        /* (non-Javadoc)
         * @see org.openmdx.kernel.loading.Factory#instantiate()
         */
    //  @Override
        public TransactionSynchronizationRegistry instantiate(
        ){
            return LightweightTransactionSynchronizationRegistry.getInstance();
        }

        /* (non-Javadoc)
         * @see org.openmdx.kernel.loading.Factory#getInstanceClass()
         */
    //  @Override
        public Class<? extends TransactionSynchronizationRegistry> getInstanceClass(
        ){
            return TransactionSynchronizationRegistry.class;
        }

    }

}
