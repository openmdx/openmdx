/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/file/ResourceManager.java,v 1.1 2005/03/24 13:42:53 hburger Exp $
 * $Revision: 1.1 $
 * $Date: 2005/03/24 13:42:53 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openmdx.uses.org.apache.commons.transaction.file;

import java.io.InputStream;
import java.io.OutputStream;

import javax.transaction.Status;

/**
 * Interface for resource managers.
 * 
 * A resource manager is an entity
 * that manages the processing and administration of resources.
 * 
 * What is specified here are methods
 * <ul> 
 * <li>for tasks related to starting and stopping of the resource manager
 * <li>for transaction management, like
 * starting, rolling back and committing of transactions  
 * <li>to set and get transaction timeouts
 * <li>to set the isolation level of a transaction
 * <li>for the general administration of resources
 * <li>for reading and writing of resources
 * </ul> 
 *  
 * @version $Revision: 1.1 $
 *
 */
public interface ResourceManager extends Status {

    /**
     * Isolation level <b>read uncommitted</b>: data written by other transactions can be read even before they commit  
     */
    public final static int ISOLATION_LEVEL_READ_UNCOMMITTED = 0;

    /**
     * Isolation level <b>read committed</b>: data written by other transactions can be read after they commit
     */
    public final static int ISOLATION_LEVEL_READ_COMMITTED = 10;

    /**
     * Isolation level <b>repeatable read</b>: data written by other transactions can be read after they commit if this transaction has not read this data before  
     */
    public final static int ISOLATION_LEVEL_REPEATABLE_READ = 50;

    /**
     * Isolation level <b>serializable</b>: result of other transactions will not influence the result of this transaction in any way
     */
    public final static int ISOLATION_LEVEL_SERIALIZABLE = 100;

    /**
     * Shutdown mode: Wait for all transactions to complete
     */
    public final static int SHUTDOWN_MODE_NORMAL = 0;

    /**
     * Shutdown mode: Try to roll back all active transactions
     */
    public final static int SHUTDOWN_MODE_ROLLBACK = 1;

    /**
     * Shutdown mode: Try to stop active transaction <em>NOW</em>, do no rollbacks
     */
    public final static int SHUTDOWN_MODE_KILL = 2;

    /**
     * Prepare result: resource manager guarantees a successful commit
     */
    public final static int PREPARE_SUCCESS = 1;

    /**
     * Prepare result: resource manager guarantees a successful commit as there is nothing to commit
     */
    public final static int PREPARE_SUCCESS_READONLY = 2;

    /**
     * Prepare result: transaction can not commit
     */
    public final static int PREPARE_FAILURE = -1;

    /**
     * Starts this resource manager. A resource manager must be started before transactions
     * can be started or any operations on transactions can be executed.
     * 
     * @throws ResourceManagerSystemException if start failed due to internal problems
     */
    public void start() throws ResourceManagerSystemException;

    /**
     * Tries to stop this resource manager within the given timeout.
     * 
     * @param mode one of {@link #SHUTDOWN_MODE_NORMAL}, {@link #SHUTDOWN_MODE_ROLLBACK}  or {@link #SHUTDOWN_MODE_KILL}
     * @param timeoutMSecs timeout for shutdown in milliseconds
     * @return <code>true</code> if resource manager stopped within given timeout
     * @throws ResourceManagerSystemException if something fatal hapened during shutdown
     */
    public boolean stop(int mode, long timeoutMSecs) throws ResourceManagerSystemException;

    /**
     * Tries to stop this resource manager within a default timeout.
     * 
     * @param mode one of predefined shutdown modes {@link #SHUTDOWN_MODE_NORMAL}, {@link #SHUTDOWN_MODE_ROLLBACK}  or {@link #SHUTDOWN_MODE_KILL}
     * or any other int representing a shutdown mode
     * @return <code>true</code> if resource manager stopped within given timeout
     * @throws ResourceManagerSystemException if anything fatal hapened during shutdown
     */
    public boolean stop(int mode) throws ResourceManagerSystemException;

    /**
     * Tries to bring this resource manager back to a consistent state. 
     * Might be called after system failure. An administrator might be forced
     * to fix system errors outside this resource manager to actually make
     * recovery possible. E.g. there may be a need for more disk space or
     * a network connection must be reestablished.
     * 
     * @return <code>true</code> upon successful recovery of the resource manager
     * @throws ResourceManagerSystemException if anything fatal hapened during shutdown
     */
    public boolean recover() throws ResourceManagerSystemException;

    /**
     * Gets the default isolation level as an integer. 
     * The higher the value the higher the isolation.
     *  
     * @return one of the predefined isolation levels {@link #ISOLATION_LEVEL_READ_UNCOMMITTED}, 
     * {@link #ISOLATION_LEVEL_READ_COMMITTED}, {@link #ISOLATION_LEVEL_REPEATABLE_READ} or {@link #ISOLATION_LEVEL_SERIALIZABLE} 
     * or any other int representing an isolation level
     * @throws ResourceManagerException if an error occured
     */
    public int getDefaultIsolationLevel() throws ResourceManagerException;

    /**
     * Gets an array of all isolation levels supported by this resource manager.
     * This array must not be <code>null</code> or empty as every resource manager has some sort of isolation level.
     * 
     * @return array of the predefined isolation levels {@link #ISOLATION_LEVEL_READ_UNCOMMITTED}, 
     * {@link #ISOLATION_LEVEL_READ_COMMITTED}, {@link #ISOLATION_LEVEL_REPEATABLE_READ} or {@link #ISOLATION_LEVEL_SERIALIZABLE} 
     * or any other int representing an isolation level
     * @throws ResourceManagerException if an error occured
     * @see #getDefaultIsolationLevel
     */
    public int[] getSupportedIsolationLevels() throws ResourceManagerException;

    /**
     * Tests if the specified isolation level is supported by this resource manager.
     * 
     * @param level isolation level whose support is to be tested 
     * @return <code>true</code> if the isolation level is supported
     * @throws ResourceManagerException if an error occured
     * @see #getDefaultIsolationLevel
     */
    public boolean isIsolationLevelSupported(int level) throws ResourceManagerException;

    /**
     * Gets the isolation level for the specified transaction. 
     * 
     * @param txId identifier for the concerned transaction
     * @return one of the predefined isolation levels {@link #ISOLATION_LEVEL_READ_UNCOMMITTED}, 
     * {@link #ISOLATION_LEVEL_READ_COMMITTED}, {@link #ISOLATION_LEVEL_REPEATABLE_READ} or {@link #ISOLATION_LEVEL_SERIALIZABLE} 
     * or any other int representing an isolation level
     * @throws ResourceManagerException if an error occured
     * @see #getDefaultIsolationLevel
     */
    public int getIsolationLevel(Object txId) throws ResourceManagerException;

    /**
     * Sets the isolation level for the specified transaction.
     * <br>
     * <em>Caution</em>: Implementations are likely to forbid changing the isolation level after any operations
     * have been executed inside the specified transaction.  
     * 
     * @param txId identifier for the concerned transaction
     * @param level one of the predefined isolation levels {@link #ISOLATION_LEVEL_READ_UNCOMMITTED}, 
     * {@link #ISOLATION_LEVEL_READ_COMMITTED}, {@link #ISOLATION_LEVEL_REPEATABLE_READ} or {@link #ISOLATION_LEVEL_SERIALIZABLE} 
     * or any other int representing an isolation level
     * @throws ResourceManagerException if an error occured
     * @see #getDefaultIsolationLevel
     */
    public void setIsolationLevel(Object txId, int level) throws ResourceManagerException;

    /**
     * Gets the default transaction timeout. After this time expires and the concerned transaction
     * has not finished - either rolled back or committed - the resource manager is allowed and
     * also encouraged - but not required - to abort the transaction and to roll it back. 
     * 
     * @return default transaction timeout
     * @throws ResourceManagerException if an error occured
     */
    public long getDefaultTransactionTimeout() throws ResourceManagerException;

    /**
     * Gets the transaction timeout of the specified transaction.
     * 
     * @param txId identifier for the concerned transaction
     * @return transaction timeout of the specified transaction in milliseconds
     * @throws ResourceManagerException if an error occured
     * @see #getDefaultTransactionTimeout
     */
    public long getTransactionTimeout(Object txId) throws ResourceManagerException;

    /**
     * Sets the transaction timeout of the specified transaction.
     * 
     * @param txId identifier for the concerned transaction
     * @param mSecs transaction timeout of the specified transaction in milliseconds
     * @throws ResourceManagerException if an error occured
     * @see #getDefaultTransactionTimeout
     */
    public void setTransactionTimeout(Object txId, long mSecs) throws ResourceManagerException;

    /**
     * Creates and starts a transaction using the specified transaction identifier.
     * The identifier needs to be unique to this resource manager.
     * As there is no transaction object returned all access to the transaction
     * needs to be addressed to this resource manager.
     * 
     * @param txId identifier for the transaction to be started
     * @throws ResourceManagerException if an error occured
     */
    public void startTransaction(Object txId) throws ResourceManagerException;

    /**
     * Prepares the transaction specified by the given transaction identifier for commit.
     * The preparation may either succeed ({@link #PREPARE_SUCCESS}), 
     * succeed as there is nothing to commit ({@link #PREPARE_SUCCESS_READONLY})
     * or fail ({@link #PREPARE_FAILURE}). If the preparation fails, commit will
     * fail as well and the transaction should be marked for rollback. However, if it 
     * succeeds the resource manager must guarantee that a following commit will succeed as well.
     * 
     * <br><br>
     * An alternative way to singal a <em>failed</em> status is to throw an exception.
     * 
     * @param txId identifier for the transaction to be prepared
     * @return result of the preparation effort, either {@link #PREPARE_SUCCESS}, {@link #PREPARE_SUCCESS_READONLY} or {@link #PREPARE_FAILURE}   
     * @throws ResourceManagerException alternative way to signal prepare failed
     */
    public int prepareTransaction(Object txId) throws ResourceManagerException;

    /**
     * Marks the transaction specified by the given transaction identifier for rollback.
     * This means, even though the transaction is not actually finished, no other operation
     * than <code>rollback</code> is permitted.
     * 
     * @param txId identifier for the transaction to be marked for rollback
     * @throws ResourceManagerException if an error occured
     */
    public void markTransactionForRollback(Object txId) throws ResourceManagerException;

    /**
     * Rolls back the transaction specified by the given transaction identifier. 
     * After roll back the resource manager is allowed to forget about
     * the associated transaction.
     * 
     * @param txId identifier for the transaction to be rolled back
     * @throws ResourceManagerException if an error occured
     */
    public void rollbackTransaction(Object txId) throws ResourceManagerException;

    /**
     * Commis the transaction specified by the given transaction identifier. 
     * After commit the resource manager is allowed to forget about
     * the associated transaction.
     * 
     * @param txId identifier for the transaction to be committed
     * @throws ResourceManagerException if an error occured
     */
    public void commitTransaction(Object txId) throws ResourceManagerException;

    /**
     * Gets the state of the transaction specified by the given transaction identifier.
     * The state will be expressed by an <code>int</code> code as defined 
     * in the {@link javax.transaction.Status} interface. 
     * 
     * @param txId identifier for the transaction for which the state is returned
     * @return state of the transaction as defined in {@link javax.transaction.Status}
     * @throws ResourceManagerException if an error occured
     */
    public int getTransactionState(Object txId) throws ResourceManagerException;

    /**
     * Explicitly locks a resource. Although locking must be done implicitly by methods 
     * creating, reading or modifying resources, there may be cases when you want to do this
     * explicitly.<br>
     * 
     *<br>
     * <em>Note</em>: By intention the order of parameters (<code>txId</code> does not come first) is different than in other methods of this interface. 
     * This is done to make clear locking affects all transactions, not only the locking one. 
     * This should be clear anyhow, but seems to be worth noting.
     * 
     * @param resourceId identifier for the resource to be locked 
     * @param txId identifier for the transaction that tries to acquire a lock
     * @param shared <code>true</code> if this lock may be shared by other <em>shared</em> locks
     * @param wait <code>true</code> if the method shall block when lock can not be acquired now
     * @param timeoutMSecs timeout in milliseconds
     * @param reentrant <code>true</code> if the lock should be acquired even when the <em>requesting transaction and no other</em> holds an incompatible lock
     * @return <code>true</code> when the lock has been acquired
     * @throws ResourceManagerException if an error occured
     */
    public boolean lockResource(
        Object resourceId,
        Object txId,
        boolean shared,
        boolean wait,
        long timeoutMSecs,
        boolean reentrant)
        throws ResourceManagerException;

    /**
     * Explicitly locks a resource in reentrant style. This method blocks until the lock
     * actually can be acquired or the transaction times out. 
     * 
     * @param resourceId identifier for the resource to be locked 
     * @param txId identifier for the transaction that tries to acquire a lock
     * @param shared <code>true</code> if this lock may be shared by other <em>shared</em> locks
     * @throws ResourceManagerException if an error occured
     * @see #lockResource(Object, Object, boolean, boolean, long, boolean)
     */
    public boolean lockResource(Object resourceId, Object txId, boolean shared) throws ResourceManagerException;

    /**
     * Explicitly locks a resource exclusively, i.e. for writing, in reentrant style. This method blocks until the lock
     * actually can be acquired or the transaction times out. 
     * 
     * @param resourceId identifier for the resource to be locked 
     * @param txId identifier for the transaction that tries to acquire a lock
     * @throws ResourceManagerException if an error occured
     * @see #lockResource(Object, Object, boolean)
     * @see #lockResource(Object, Object, boolean, boolean, long, boolean)
     */
    public boolean lockResource(Object resourceId, Object txId) throws ResourceManagerException;

    /**
     * Checks if a resource exists. 
     * 
     * @param txId identifier for the transaction in which the resource is to be checked for
     * @param resourceId identifier for the resource to check for 
     * @return <code>true</code> if the resource exists
     * @throws ResourceManagerException if an error occured
     */
    public boolean resourceExists(Object txId, Object resourceId) throws ResourceManagerException;

    /**
     * Checks if a resource exists wihtout being in a transaction. This means only take
     * into account resources already globally commited.
     * 
     * @param resourceId identifier for the resource to check for 
     * @return <code>true</code> if the resource exists
     * @throws ResourceManagerException if an error occured
     */
    public boolean resourceExists(Object resourceId) throws ResourceManagerException;

    /**
     * Deletes a resource.
     * 
     * @param txId identifier for the transaction in which the resource is to be deleted
     * @param resourceId identifier for the resource to be deleted
     * @throws ResourceManagerException if the resource does not exist or any other error occured
     */
    public void deleteResource(Object txId, Object resourceId) throws ResourceManagerException;

    /**
     * Deletes a resource.
     * 
     * @param txId identifier for the transaction in which the resource is to be deleted
     * @param resourceId identifier for the resource to be deleted
     * @param assureOnly if set to <code>true</code> this method will not throw an exception when the resource does not exist
     * @throws ResourceManagerException if the resource does not exist and <code>assureOnly</code> was not set to <code>true</code> or any other error occured
     */
    public void deleteResource(Object txId, Object resourceId, boolean assureOnly) throws ResourceManagerException;

    /**
     * Creates a resource.
     * 
     * @param txId identifier for the transaction in which the resource is to be created
     * @param resourceId identifier for the resource to be created
     * @throws ResourceManagerException if the resource already exist or any other error occured
     */
    public void createResource(Object txId, Object resourceId) throws ResourceManagerException;

    /**
     * Creates a resource.
     * 
     * @param txId identifier for the transaction in which the resource is to be created
     * @param resourceId identifier for the resource to be created
     * @param assureOnly if set to <code>true</code> this method will not throw an exception when the resource already exists
     * @throws ResourceManagerException if the resource already exists and <code>assureOnly</code> was not set to <code>true</code> or any other error occured
     */
    public void createResource(Object txId, Object resourceId, boolean assureOnly) throws ResourceManagerException;
    
	/**
	 * Opens a streamable resource for reading.
	 * 
	 * <br><br>
	 * <em>Important</em>: By contract, the application is responsible for closing the stream after its work is finished.
	 * 
	 * @param txId identifier for the transaction in which the streamable resource is to be openend
	 * @param resourceId identifier for the streamable resource to be opened
	 * @return stream to read from 
	 * @throws ResourceManagerException if the resource does not exist or any other error occured
	 */
	public InputStream readResource(Object txId, Object resourceId) throws ResourceManagerException;
    
	/**
	 * Opens a streamable resource for a single reading request not inside the scope of a transaction.
	 *  
	 * <br><br>
	 * <em>Important</em>: By contract, the application is responsible for closing the stream after its work is finished.
	 * 
	 * @param resourceId identifier for the streamable resource to be opened
	 * @return stream to read from 
	 * @throws ResourceManagerException if the resource does not exist or any other error occured
	 */
	public InputStream readResource(Object resourceId) throws ResourceManagerException;

	/**
	 * Opens a resource for writing. 
	 * 
	 * <br><br>
	 * <em>Important</em>: By contract, the application is responsible for closing the stream after its work is finished.
	 * 
	 * @param txId identifier for the transaction in which the streamable resource is to be openend
	 * @param resourceId identifier for the streamable resource to be opened
	 * @return stream to write to 
	 * @throws ResourceManagerException if the resource does not exist or any other error occured
	 */
	public OutputStream writeResource(Object txId, Object resourceId) throws ResourceManagerException;
}
