/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: LightweightConnectionManager
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.resource.spi.AbstractConnectionManager;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.uses.org.apache.commons.pool2.ObjectPool;
import org.openmdx.uses.org.apache.commons.pool2.PooledObject;
import org.openmdx.uses.org.apache.commons.pool2.PooledObjectFactory;
import org.openmdx.uses.org.apache.commons.pool2.impl.DefaultPooledObject;
import org.openmdx.uses.org.apache.commons.pool2.impl.GenericObjectPool;


/**
 * Lightweight Connection Manager
 */
public class LightweightConnectionManager
    extends AbstractConnectionManager
{

	/**
     * Constructor
     * 
	 * @param connectionClass the connection class 
	 * @param transactionManager the transaction manager to be used
     */
    public LightweightConnectionManager(
        Class<?> connectionClass, 
        TransactionManager transactionManager
    ) {
        super(connectionClass);
        this.transactionManager = transactionManager;
		this.managedConnectionPools = 
				new HashMap<ManagedConnectionFactory, Map<ConnectionRequestInfo,ObjectPool<ManagedConnection>>>(); 
		this.connectionRegistries =
				new IdentityHashMap<Transaction,ConnectionRegistry>();
    }

    /**
     * The transaction manager is used to retrieve the current transaction.
     */
    private final TransactionManager transactionManager;
    
	/**
     * The pools of unused connections
     */
    private final Map<ManagedConnectionFactory,Map<ConnectionRequestInfo,ObjectPool<ManagedConnection>>> managedConnectionPools;
    
    /**
     * Keep track of the connections registered with a transaction
     */
    private final Map<Transaction,ConnectionRegistry> connectionRegistries; 
    		
    
    /**
     * Implements <code>Serializable</code>.
     */
    private static final long serialVersionUID = 6149851064813039668L;
    

	//------------------------------------------------------------------------
    // Extends AbstractConnectionManager
    //------------------------------------------------------------------------    

    /**
     * Allocate a managed connection
     * 
     * @return a (maybe newly created) managed connection
     *
     * @throws ResourceException
     */
    @Override
    protected ManagedConnection allocateMangedConnection(
        ManagedConnectionFactory managedConnectionFactory,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
		final Map<ManagedConnection, ObjectPool<ManagedConnection>> connectionRegistry = getConnectionRegistry();
        ManagedConnection managedConnection = managedConnectionFactory.matchManagedConnections(
    		connectionRegistry.keySet(),
    		getSubject(), // SUbject without credentials!
            connectionRequestInfo
        );
        if(managedConnection == null) {
        	SysLog.info("connectionRegistries", new IndentingFormatter(this.connectionRegistries));
        	SysLog.info("managedConnectionPools", new IndentingFormatter(this.managedConnectionPools));
            managedConnection = createManagedConnection(
        		managedConnectionFactory, 
        		connectionRequestInfo,
				connectionRegistry, 
				managedConnection
			);
        }
        return managedConnection;        
    }

	private ManagedConnection createManagedConnection(ManagedConnectionFactory managedConnectionFactory,
		ConnectionRequestInfo connectionRequestInfo,
		final Map<ManagedConnection, ObjectPool<ManagedConnection>> connectionRegistry,
		ManagedConnection managedConnection
	) throws ResourceException {
		try {
			final ObjectPool<ManagedConnection> pool = getManagedConnectionPool(managedConnectionFactory, connectionRequestInfo);
			managedConnection = pool.borrowObject();
			connectionRegistry.put(managedConnection, pool);
		} catch (Exception exception) {
		    throw toResourceException(
		        exception, 
		        "Managed connection allocation failed"
		    );
		}
		return managedConnection;
	}

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

    private Map<ManagedConnection,ObjectPool<ManagedConnection>> getConnectionRegistry(        
    ) throws ResourceException {
        final Transaction transaction = getTransaction();
        Map<ManagedConnection,ObjectPool<ManagedConnection>> connectionRegistry = this.connectionRegistries.get(transaction);
        if(connectionRegistry == null) {
        	connectionRegistry = new ConnectionRegistry(transaction); // registry registers itself
        }
        return connectionRegistry;
    }

    private ObjectPool<ManagedConnection> getManagedConnectionPool(
		ManagedConnectionFactory managedConnectionFactory, 
        ConnectionRequestInfo connectionRequestInfo
    ){
    	Map<ConnectionRequestInfo, ObjectPool<ManagedConnection>> pools = this.managedConnectionPools.get(managedConnectionFactory);
    	if(pools == null) {
    		pools = new HashMap<ConnectionRequestInfo, ObjectPool<ManagedConnection>>();
    		this.managedConnectionPools.put(managedConnectionFactory, pools);
    	}
    	ObjectPool<ManagedConnection> pool = pools.get(connectionRequestInfo);
    	if(pool == null) {
    		pool = new GenericObjectPool<ManagedConnection>(
				new LightweightPoolableObjectFactory(managedConnectionFactory, connectionRequestInfo)
			);
    	}
    	return pool;
    }
    
    
    //------------------------------------------------------------------------
    // Class ManagedConnectionMap
    //------------------------------------------------------------------------
    
	/**
     * Transactional Connections
     */
    class ConnectionRegistry
        extends IdentityHashMap<ManagedConnection,ObjectPool<ManagedConnection>>
        implements Synchronization 
    {

    	ConnectionRegistry(Transaction transaction) throws ResourceException {
			this.transaction = transaction;
			register();
		}

		private void register() throws ResourceException {
			try {
				if(transaction != null) {
					transaction.registerSynchronization(this);
				}
				connectionRegistries.put(transaction, this);
			} catch (IllegalStateException e) {
				throw toResourceException(e, "Unable to register the ConnectionRegistry");
			} catch (RollbackException e) {
				throw toResourceException(e, "Unable to register the ConnectionRegistry");
			} catch (SystemException e) {
				throw toResourceException(e, "Unable to register the ConnectionRegistry");
			}
		}

		private void unregister() {
            clear();
			connectionRegistries.remove(transaction);
		}
		
		private final Transaction transaction;
    	
        /**
         * Implements <code>Serializable</code>.
         */
		private static final long serialVersionUID = 5465858924325900256L;

		/* (non-Javadoc)
         * @see javax.transaction.Synchronization#afterCompletion(int)
         */
        public void afterCompletion(int status) {
        	final boolean reUse = status == Status.STATUS_COMMITTED || status == Status.STATUS_ROLLEDBACK;
        	for(Map.Entry<ManagedConnection,ObjectPool<ManagedConnection>> entry : entrySet()) {
					final ObjectPool<ManagedConnection> pool = entry.getValue();
					final ManagedConnection connection = entry.getKey();
					if(reUse) {
						try {
							pool.returnObject(connection);
						} catch (Exception exception) {
							SysLog.warning("Could not return managed connection to pool", exception);						
						}
					} else {
						try {
							pool.invalidateObject(connection);
						} catch (Exception exception) {
							SysLog.warning("Could not invalidate managed connection", exception);						
						}
					}
        	}
        	unregister();
        }

        /* (non-Javadoc)
         * @see javax.transaction.Synchronization#beforeCompletion()
         */
        public void beforeCompletion() {
            //
        }
        
    }

    
	//------------------------------------------------------------------------
    // Class LightweightPoolableObjectFactory
    //------------------------------------------------------------------------
    
	/**
	 * Lightweight Poolable Object Factory
	 */
    class LightweightPoolableObjectFactory implements PooledObjectFactory<ManagedConnection> {

    	LightweightPoolableObjectFactory(
    		ManagedConnectionFactory managedConnectionFactory,
			ConnectionRequestInfo connectionRequestInfo
		) {
			this.managedConnectionFactory = managedConnectionFactory;
			this.connectionRequestInfo = connectionRequestInfo;
			this.subject = new Subject();
		}

		/**
    	 * Managed connection factory to be used as makeObject() argument
    	 */
    	private final ManagedConnectionFactory managedConnectionFactory;
    	
    	/**
    	 * Connection request info to be used as makeObject() argument
    	 */
    	private final ConnectionRequestInfo connectionRequestInfo;
    	
    	/**
    	 * A credentialless subject
    	 */
    	private final Subject subject;
    	
		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool2.PooledObjectFactory#destroyObject(org.openmdx.uses.org.apache.commons.pool2.PooledObject)
		 */
		@Override
		public void destroyObject(PooledObject<ManagedConnection> p) throws Exception {
			p.getObject().destroy();
		}

		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool2.PooledObjectFactory#validateObject(org.openmdx.uses.org.apache.commons.pool2.PooledObject)
		 */
		@Override
		public boolean validateObject(PooledObject<ManagedConnection> p) {
			ManagedConnection managedConnection = p.getObject();
		    if(managedConnection instanceof Validatable) {
		        //
		        // Validate the object
		        //
		        return ((Validatable)managedConnection).validate();
		    } else {
	            // 
	            // There is no validation method
	            //
	            return true;
		    }
		}

		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool2.PooledObjectFactory#activateObject(org.openmdx.uses.org.apache.commons.pool2.PooledObject)
		 */
		@Override
		public void activateObject(PooledObject<ManagedConnection> p) throws Exception {
	        Transaction transaction = getTransaction();
	        if(transaction != null) {
    	        transaction.enlistResource(p.getObject().getXAResource());
	        }
		}

		/* (non-Javadoc)
		 * @see org.openmdx.uses.org.apache.commons.pool2.PooledObjectFactory#passivateObject(org.openmdx.uses.org.apache.commons.pool2.PooledObject)
		 */
		@Override
		public void passivateObject(PooledObject<ManagedConnection> p) throws Exception {
			p.getObject().cleanup();
		}

		@Override
		public PooledObject<ManagedConnection> makeObject() throws Exception {
			return new DefaultPooledObject<ManagedConnection>(
				managedConnectionFactory.createManagedConnection(
					subject,
				    connectionRequestInfo
				)
			);
		}

    }
    
}
