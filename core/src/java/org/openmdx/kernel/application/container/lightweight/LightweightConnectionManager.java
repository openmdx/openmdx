/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LightweightConnectionManager.java,v 1.8 2007/10/10 16:06:04 hburger Exp $
 * Description: LightweightConnectionManager
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:04 $
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
package org.openmdx.kernel.application.container.lightweight;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransactionException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.openmdx.kernel.log.SysLog;
import org.openmdx.uses.org.apache.commons.pool.ObjectPool;
import org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory;
import org.openmdx.uses.org.apache.commons.pool.impl.GenericObjectPool;


/**
 * Lightweight Connection Manager
 * <p>
 * This implementation uses one connection manager per managed connection
 * factory.
 */
public class LightweightConnectionManager
    extends AbstractConnectionManager
{

    /**
     * Constructor
     * 
     * @param credentials
     * @param connectionClass
     * @param transactionManager
     * @param initialCapacity 
     * @param maximumCapacity 
     * @param maximumWait 
     */
    public LightweightConnectionManager(
        Set credentials, 
        Class connectionClass,
        TransactionManager transactionManager, 
        Integer maximumCapacity, 
        Long maximumWait
    ) {
        super(credentials, connectionClass);
        this.transactionManager = transactionManager;
		this.managedConnectionPool = new GenericObjectPool(
			new LightweightPoolableObjectFactory(),
			maximumCapacity == null ? GenericObjectPool.DEFAULT_MAX_ACTIVE : maximumCapacity.intValue(),
			GenericObjectPool.WHEN_EXHAUSTED_BLOCK,
			maximumWait == null ? GenericObjectPool.DEFAULT_MAX_WAIT : maximumWait.longValue()			
		);
    }

    /**
     * Constructor
     * @param credentials
     * @param connectionClass
     * @param transactionManager
     * @param initialCapacity 
     * @param maximumCapacity 
     * @param maximumWait 
     * 
     * @throws ResourceException
     */
    public LightweightConnectionManager(
        Set credentials, 
        String connectionClass,
        TransactionManager transactionManager, 
        Integer maximumCapacity, 
        Long maximumWait
    ) throws ResourceException {
        super(credentials, connectionClass);
        this.transactionManager = transactionManager;
		this.managedConnectionPool = new GenericObjectPool(
			new LightweightPoolableObjectFactory(),
			maximumCapacity == null ? GenericObjectPool.DEFAULT_MAX_ACTIVE : maximumCapacity.intValue(),
			GenericObjectPool.WHEN_EXHAUSTED_BLOCK,
			maximumWait == null ? GenericObjectPool.DEFAULT_MAX_WAIT : maximumWait.longValue()			
		);
    }

    /**
     * Implements <code>Serializable</code>.
     */
    private static final long serialVersionUID = 3256719589399344438L;

    /**
     * The trasaction manager is used to retrieve the current transaction.
     */
    private final TransactionManager transactionManager;
    
    /**
     * Each value is a set of connections enlisted with the given transaction.
     */
    private static final Map transactionalConnections = new WeakHashMap();
    
	/**
     * A pool of unused connections
     */
    final ObjectPool managedConnectionPool;
    
	/**
	 * Subject to be used as makeObject() argument
	 */
    transient Subject currentSubject;

	/**
	 * Managed connection factory to be used as makeObject() argument
	 */
	transient ManagedConnectionFactory currentManagedConnectionFactory;
	
	/**
	 * Connection request info to be used as makeObject() argument
	 */
	transient ConnectionRequestInfo currentConnectionRequestInfo;


	//------------------------------------------------------------------------
    // Extends AbstractConnectionManager
    //------------------------------------------------------------------------    

    /**
     * Retrieve the current transaction from the transaction manager,
     */
    protected Transaction getTransaction(
    ) throws ResourceException {
        try {
            return this.transactionManager.getTransaction();
        } catch (SystemException exception) {
            throw toResourceException(
                exception, 
                "Current transaction retrieval failed"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.lightweight.AbstractConnectionManager#getManagedConnections()
     */
    protected Set getManagedConnections(        
    ) throws ResourceException {
        Transaction transaction = getTransaction();
        if(transaction == null) throw new LocalTransactionException(
            "No active transaction, managed connection can't be allocated"
        );
        synchronized(transactionalConnections){
            ManagedConnectionSet managedConnectionSet = (ManagedConnectionSet) transactionalConnections.get(
                transaction
            );
            if(managedConnectionSet == null) try {
                managedConnectionSet = new ManagedConnectionSet();
                transactionalConnections.put(transaction, managedConnectionSet);
                transaction.registerSynchronization(managedConnectionSet);
            } catch (Exception exception) {
                throw toResourceException(
                    exception, 
                    "Creation and registration of managed connection set failed"
                );
            }
            return managedConnectionSet;
        }
    }

    /**
     * Allocate a managed connection
     * 
     * @param subject
     * @param managedConnectionFactory
     * @param connectionRequestInfo
     * 
     * @return a (maybe newly created) managed connection
     * 
     * @throws ResourceException 
     */
    protected synchronized ManagedConnection allocateManagedConnection(
        Subject subject,
        ManagedConnectionFactory managedConnectionFactory, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException{
		this.currentSubject = subject;
		this.currentManagedConnectionFactory = managedConnectionFactory;
		this.currentConnectionRequestInfo = connectionRequestInfo;
        try {
            return (ManagedConnection) this.managedConnectionPool.borrowObject();
        } catch (Exception exception) {
            throw toResourceException(
                exception, 
                "Managed connection allocation failed"
            );
        } finally {
			this.currentSubject = null;
			this.currentManagedConnectionFactory = null;
			this.currentConnectionRequestInfo = null;
        }
    }
    
    /**
     * Pre-allocate managed connections
     * 
     * @parame initialCapacity
     * @param subject
     * @param managedConnectionFactory
     * @param connectionRequestInfo
     * 
     * @throws ResourceException 
     */
    public synchronized void preAllocateManagedConnection(
		Integer initialCapacity,
        ManagedConnectionFactory managedConnectionFactory, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException{
		if(initialCapacity == null) return;
		this.currentSubject = super.getSubject();
		this.currentManagedConnectionFactory = managedConnectionFactory;
		this.currentConnectionRequestInfo = connectionRequestInfo;
        try {
			for(
		        int i = initialCapacity.intValue();
				i > 0;
				i--
			) this.managedConnectionPool.addObject();
        } catch (Exception exception) {
            throw toResourceException(
                exception, 
                "Managed connection pre-allocation failed"
            );
        } finally {
			this.currentSubject = null;
			this.currentManagedConnectionFactory = null;
			this.currentConnectionRequestInfo = null;
        }
    }

	/**
     * Class ManagedConnectionSet
     */
    class ManagedConnectionSet
        extends AbstractSet
        implements Synchronization 
    {

        private static final long serialVersionUID = 3257009851963356210L;

        private final Set delegate = new HashSet();
        
        /* (non-Javadoc)
         * @see javax.transaction.Synchronization#afterCompletion(int)
         */
        public void afterCompletion(int status) {
            this.clear();
        }

        /* (non-Javadoc)
         * @see javax.transaction.Synchronization#beforeCompletion()
         */
        public void beforeCompletion() {
            //
        }

        public Iterator iterator(
        ) {
            return new Interceptor(this.delegate.iterator());
        }

        public int size() {
            return delegate.size();
        }
        
        public boolean add(Object o) {
            return this.delegate.add(o);
        }        
        
        /**
         * Class Interceptor
         */
        class Interceptor implements Iterator {
            
            public Interceptor(
                Iterator delegate
            ) {
                this.delegate = delegate;
            }
            
            private final Iterator delegate;

            private ManagedConnection current = null;
            
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            public Object next() {
                return this.current = (ManagedConnection) this.delegate.next();
            }

            public void remove() {
                this.delegate.remove();
                try {
                    managedConnectionPool.returnObject(this.current);
                } catch (Exception exception) {
					SysLog.warning("Could not return managed connection to pool", exception);						
                }
            } 
            
        }
        
    }

	
	/**
	 * 
	 * Lightweight Poolable Object Factory
	 *
	 */
	class LightweightPoolableObjectFactory implements PoolableObjectFactory {

		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#activateObject(java.lang.Object)
		 */
		public void activateObject(Object obj) throws Exception {		
	        Transaction transaction = getTransaction();
	        if(transaction == null) throw new LocalTransactionException(
	            "No active transaction, resource can't be enlisted"
	        );
	        transaction.enlistResource(((ManagedConnection)obj).getXAResource());
		}
	
		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#destroyObject(java.lang.Object)
		 */
		public void destroyObject(Object obj) throws Exception {
	        ((ManagedConnection)obj).destroy();
		}
	
		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#makeObject()
		 */
		public Object makeObject() throws Exception {
			return currentManagedConnectionFactory.createManagedConnection(
			    currentSubject,
			    currentConnectionRequestInfo
			);
		}
	
		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#passivateObject(java.lang.Object)
		 */
		public void passivateObject(Object obj) throws Exception {
	        ((ManagedConnection)obj).cleanup();
		}
	
		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#validateObject(java.lang.Object)
		 */
		public boolean validateObject(Object obj) {
			// 
			// There is no approprate managed connection validation method
			//
			return true;
		}
	
	}
	
}
