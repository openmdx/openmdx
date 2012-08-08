/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LightweightConnectionManager.java,v 1.2 2009/09/17 13:17:56 hburger Exp $
 * Description: LightweightConnectionManager
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/17 13:17:56 $
 * ====================================================================
 *
 * This software is published under the BSD licenseas listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.lightweight.resource;

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
import org.openmdx.kernel.resource.spi.AbstractConnectionManager;
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
     * @param credentials the credentials to be used to connect
     * @param connectionClass the connection class 
     * @param transactionManager the transaction manager to be used
     * @param maximumCapacity the maximum number of objects that can be borrowed from me at one time 
     * @param maximumWait the maximum amount of time to wait for an idle object when the pool is exhausted
     */
    public LightweightConnectionManager(
        Set<?> credentials, 
        Class<?> connectionClass,
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
     * 
     * @param credentials the credentials to be used to connect
     * @param connectionClass the connection class 
     * @param transactionManager the transaction manager to be used
     * @param maximumCapacity the maximum number of objects that can be borrowed from me at one time 
     * @param maximumWait the maximum amount of time to wait for an idle object when the pool is exhausted
     */
    public LightweightConnectionManager(
        Set<?> credentials, 
        String connectionClass,
        TransactionManager transactionManager, 
        Integer maximumCapacity, 
        Long maximumWait
    ) throws ResourceException {
        this(
            credentials,
            connectionClass,
            transactionManager,
            maximumCapacity,
            maximumWait,
            null, // maximumIdle
            null, // minimumIdle
            null, // testOnBorrow
            null, // testOnReturn
            null, // timeBetweeEvictionRuns
            null, // numberOfTestsPerEvictionRun
            null, // minimumEvictableIdleTime
            null // testWhileIdle
       );
    }

    /**
     * Constructor
     * 
     * @param credentials the credentials to be used to connect
     * @param connectionClass the connection class 
     * @param transactionManager the transaction manager to be used
     * @param maximumCapacity the maximum number of objects that can be borrowed from me at one time 
     * @param maximumWait the maximum amount of time to wait for an idle object when the pool is exhausted
     * @param maximumIdle maxIdle the maximum number of idle objects in my poo
     * @param minimumIdle minIdle the minimum number of idle objects in my pool
     * @param testOnBorrow whether or not to validate objects before they are returned by the borrowObject(} method
     * @param testOnReturn whether or not to validate objects after they are returned to the returnObject(} method
     * @param timeBetweeEvictionRuns the amount of time (in milliseconds) to sleep between examining idle objects for eviction
     * @param numberOfTestsPerEvictionRun the number of idle objects to examine per run within the idle object eviction thread (if any)
     * @param minimumEvictableIdleTime the minimum number of milliseconds an object can sit idle in the pool before it is eligable for eviction
     * @param testWhileIdle whether or not to validate objects in the idle object eviction thread, if any
     */
    public LightweightConnectionManager(
        Set<?> credentials, 
        String connectionClass,
        TransactionManager transactionManager, 
        Integer maximumCapacity, 
        Long maximumWait, 
        Integer maximumIdle, 
        Integer minimumIdle, 
        Boolean testOnBorrow, 
        Boolean testOnReturn, 
        Long timeBetweeEvictionRuns, 
        Integer numberOfTestsPerEvictionRun, 
        Long minimumEvictableIdleTime, 
        Boolean testWhileIdle
    ) throws ResourceException {
        super(credentials, connectionClass);
        this.transactionManager = transactionManager;
		this.managedConnectionPool = new GenericObjectPool(
			new LightweightPoolableObjectFactory(),
			maximumCapacity == null ? GenericObjectPool.DEFAULT_MAX_ACTIVE : maximumCapacity.intValue(),
			GenericObjectPool.WHEN_EXHAUSTED_BLOCK,
			maximumWait == null ? GenericObjectPool.DEFAULT_MAX_WAIT : maximumWait.longValue(),
		    maximumIdle == null ? GenericObjectPool.DEFAULT_MAX_IDLE : maximumIdle.intValue(),    
	        minimumIdle == null ? GenericObjectPool.DEFAULT_MIN_IDLE : minimumIdle.intValue(), 
            testOnBorrow == null ? GenericObjectPool.DEFAULT_TEST_ON_BORROW : testOnBorrow.booleanValue(),
            testOnReturn == null ? GenericObjectPool.DEFAULT_TEST_ON_RETURN : testOnReturn.booleanValue(),   
            timeBetweeEvictionRuns == null ? GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS : timeBetweeEvictionRuns.longValue(),
            numberOfTestsPerEvictionRun == null ? GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN :  numberOfTestsPerEvictionRun.intValue(),
            minimumEvictableIdleTime == null ? GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS : minimumEvictableIdleTime.longValue(),
            testWhileIdle == null ? GenericObjectPool.DEFAULT_TEST_WHILE_IDLE : testWhileIdle.booleanValue()    
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
    private static final Map<Transaction,ManagedConnectionSet> transactionalConnections = 
        new WeakHashMap<Transaction,ManagedConnectionSet>();
    
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
    protected Set<ManagedConnection> getManagedConnections(        
    ) throws ResourceException {
        Transaction transaction = getTransaction();
        if(transaction == null) {
        	throw new LocalTransactionException(
	            "No active transaction, managed connection can't be allocated"
	        );
        } else synchronized(transactionalConnections){
            ManagedConnectionSet managedConnectionSet = transactionalConnections.get(
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

    
    //------------------------------------------------------------------------
    // Class ManagedConnectionSet
    //------------------------------------------------------------------------
    
	/**
     * Managed Connection Set
     */
    class ManagedConnectionSet
        extends AbstractSet<ManagedConnection>
        implements Synchronization 
    {

        private static final long serialVersionUID = 3257009851963356210L;

        private final Set<ManagedConnection> delegate = new HashSet<ManagedConnection>();
        
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

        public Iterator<ManagedConnection> iterator(
        ) {
            return new Interceptor(this.delegate.iterator());
        }

        public int size() {
            return delegate.size();
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        public boolean add(ManagedConnection o) {
            return this.delegate.add(o);
        }        
        
        /**
         * Class Interceptor
         */
        class Interceptor implements Iterator<ManagedConnection> {
            
            public Interceptor(
                Iterator<ManagedConnection> delegate
            ) {
                this.delegate = delegate;
            }
            
            private final Iterator<ManagedConnection> delegate;

            private ManagedConnection current = null;
            
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            public ManagedConnection next() {
                return this.current = this.delegate.next();
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

    
	//------------------------------------------------------------------------
    // Class LightweightPoolableObjectFactory
    //------------------------------------------------------------------------
    
	/**
	 * Lightweight Poolable Object Factory
	 */
    class LightweightPoolableObjectFactory implements PoolableObjectFactory {

		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#activateObject(java.lang.Object)
		 */
		public void activateObject(Object obj) throws Exception {		
            ManagedConnection managedConnection = (ManagedConnection)obj;
	        Transaction transaction = getTransaction();
	        if(transaction != null) {
    	        transaction.enlistResource(managedConnection.getXAResource());
	        }
		}
	
		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#destroyObject(java.lang.Object)
		 */
		public void destroyObject(Object obj) throws Exception {
            ManagedConnection managedConnection = (ManagedConnection)obj;
            managedConnection.destroy();
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
            ManagedConnection managedConnection = (ManagedConnection)obj;
            managedConnection.cleanup();
		}
	
		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#validateObject(java.lang.Object)
		 */
		public boolean validateObject(Object obj) {
		    if(obj instanceof Validatable) {
		        //
		        // Validate the object
		        //
		        return ((Validatable)obj).validate();
		    } else {
	            // 
	            // There is no validation method
	            //
	            return true;
		    }
		}
	
	}
	
}
