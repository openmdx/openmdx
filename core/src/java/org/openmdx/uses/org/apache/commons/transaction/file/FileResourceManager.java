/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/file/FileResourceManager.java,v 1.6 2008/03/21 18:42:15 hburger Exp $
 * $Revision: 1.6 $
 * $Date: 2008/03/21 18:42:15 $
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
 * This file has been modified in order to fit into the openMDX framework.
 * Copyright (c) 2005-2006, OMEX AG, Switzerland
 */

package org.openmdx.uses.org.apache.commons.transaction.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmdx.uses.org.apache.commons.transaction.locking.GenericLock;
import org.openmdx.uses.org.apache.commons.transaction.locking.GenericLockManager;
import org.openmdx.uses.org.apache.commons.transaction.locking.LockManager;
import org.openmdx.uses.org.apache.commons.transaction.locking.MultiLevelLock;
import org.openmdx.uses.org.apache.commons.transaction.util.FileHelper;
import org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade;

/**
 * A resource manager for streamable objects stored in a file system.
 * 
 * It is intended for developper and "out of the box" use. 
 * It is <em>not</em> intended to be a real alternative for
 * a full blown DMBS (of course it can not be compared to an RDBMS at all).
 * 
 * Major features:<br>
 * <ul>
 * <li>Transactions performed with this class more or less comform to the widely accepted ACID properties
 * <li>Reading should be as fast as from the ordinary file system (at the cost of a bit slower commits) 
 * </ul>
 * 
 * Compared to a "real" DBMS major limitations are (in order of assumed severity):<br>
 * <ul>
 * <li>Number of simultaneuously open resources is limited to the number of available file descriptors
 * <li>It does not scale a bit
 * <li>Pessimistic transaction and locking scheme
 * <li>Isolation level currently is restricted to <em>read committed</em> and <em>repeated read</em> (which is not that bad)
 * </ul>
 * 
 * <em>Important</em>: If possible you should have the work and store directory located in the 
 * same file system. If not, you might get additional problems, as there are:
 * <ul>
 * <li>On commit it might be necessay to copy files instead of rename/relink them. This may lead to time consuming, 
 * overly blocking commit phases and higher risk of corrupted files
 * <li>Prepare phase might be too permissive, no check for sufficient memory on store file system is possible
 * </ul> 
 * 
 * General limitations include:<br>
 * <ul>
 * <li>Due to lack of synchronization on the transaction context level, every transaction may only be
 * accessed by a <em>single thread</em> throughout its full life. 
 * This means it is forbidden for a thread that has not started a transaction 
 * to perform any operations inside this transaction. However, threads associated
 * with different transactions can safely access these methods concurrently.
 * Reasons for the lack of synchronization are improved performance and simplicity (of the code of this class).
 * <li>There is no dedicated class for a transaction. Having such a class would be better practice and 
 * make certain actions more intuitive.
 * <li>Resource identifiers need a reasonsable string representation obtainable by <code>toString</code>.
 * More specifically, they will have to resolve to a <em>valid</em> file path that does note denote a directoy. 
 * If it does, you might be able to create it, but not to read or write anything 
 * from resp. to it. Valid string representations of a resource idenfier are 
 * for example "file" "/root" or "hjfhdfhuhuhsdufhdsufhdsufhdfuhdfduhduhduhdu". 
 * Invalid are for example "/" or "/root/". Invalid on some file systems are for example "c:" or "file://huhu".
 * <li>As there are no active processes inside this RM and it shares its threads with the application,
 * control over transactions is limited to points where the application calls the RM. 
 * In particular, this disables <em>active</em> terminmation of transactions upon timeout.
 * <li>There is no notion of a connection to this file manager. This means you can not connect from hosts other than
 * local and you will get problems when plugging this store into a J2EE store using connectors. 
 * <li>Methods should throw more specific exceptions
 * </ul>
 * 
 * <em>Caution</em>:<br>
 * The <code>txId</code> passed to many methods as an identifier for the
 * transaction concerned will function as a key in a <code>HashMap</code>.
 * Thus assure that <code>equals</code> and <code>hashCode</code> are both
 * properly implemented and match each other.
 * 
 *  
 * <em>Caution</em>: You will have to guarantee that no other process will access neither
 * the store or the working dir concurrently to this <code>FileResourceManager</code>.
 * 
 * <em>Special Caution</em>: Be very careful not to have two instances of
 * <code>FileResourceManager</code> working in the same store and/or working dir.
 *   
 * @version $Revision: 1.6 $
 */
@SuppressWarnings("unchecked")
public class FileResourceManager implements ResourceManager, ResourceManagerErrorCodes {

    // reflects the natural isolation level of this store
    protected static final int NATIVE_ISOLATION_LEVEL = ISOLATION_LEVEL_REPEATABLE_READ;
    protected static final int DEFAULT_ISOLATION_LEVEL = NATIVE_ISOLATION_LEVEL;

    protected static final int NO_LOCK = 0;
    protected static final int LOCK_ACCESS = NO_LOCK + 1;
    protected static final int LOCK_SHARED = NO_LOCK + 2;
    protected static final int LOCK_EXCLUSIVE = NO_LOCK + 3;
    protected static final int LOCK_COMMIT = NO_LOCK + 4;

    protected static final int OPERATION_MODE_STOPPED = 0;
    protected static final int OPERATION_MODE_STOPPING = 1;
    protected static final int OPERATION_MODE_STARTED = 2;
    protected static final int OPERATION_MODE_STARTING = 3;
    protected static final int OPERATION_MODE_RECOVERING = 4;

    protected static final String DEFAULT_PARAMETER_ENCODING = "ISO-8859-15";

    protected static final int DEFAULT_TIMEOUT_MSECS = 5000;
    protected static final int DEFAULT_COMMIT_TIMEOUT_FACTOR = 2;

    protected static final String WORK_CHANGE_DIR = "change";
    protected static final String WORK_DELETE_DIR = "delete";

    protected static final String CONTEXT_FILE = "transaction.log";

    /*
     * --- Static helper methods ---
     *
     *  
     */

    protected static void applyDeletes(File removeDir, File targetDir, File rootDir) throws IOException {
        if (removeDir.isDirectory() && targetDir.isDirectory()) {
            File[] files = removeDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File removeFile = files[i];
                File targetFile = new File(targetDir, removeFile.getName());
                if (removeFile.isFile()) {
                    if (targetFile.exists()) {
                        targetFile.delete();
                    }
                    // indicate, this has been done
                    removeFile.delete();
                } else {
                    applyDeletes(removeFile, targetFile, rootDir);
                }
                // delete empty target directories, except root dir
                if (!targetDir.equals(rootDir) && targetDir.list().length == 0) {
                    targetDir.delete();
                }
            }
        }
    }

    /*
     * --- object members ---
     * 
     *  
     */

    protected String workDir;
    protected String storeDir;
    protected boolean urlEncodePath = false;
    protected boolean cleanUp = true;
    protected boolean dirty = false;
    protected int operationMode = OPERATION_MODE_STOPPED;
    protected long defaultTimeout = DEFAULT_TIMEOUT_MSECS;
    protected boolean debug;

    protected LoggerFacade logger;

    protected Map globalTransactions;
    protected List globalOpenResources;
    protected LockManager lockManager;

    /*
     * --- ctor and general getter / setter methods ---
     *
     *  
     */

    /**
     * Creates a new resouce manager operation on the specified directories.
     * 
     * @param storeDir directory where main data should go after commit
     * @param workDir directory where transactions store temporary data
     * @param urlEncodePath if set to <code>true</code> encodes all paths to allow for any kind of characters
     * @param logger the logger to be used by this store  
     */
    public FileResourceManager(String storeDir, String workDir, boolean urlEncodePath, LoggerFacade logger) {
        this(storeDir, workDir, urlEncodePath, logger, false);
    }

    /**
     * Creates a new resouce manager operation on the specified directories.
     * 
     * @param storeDir directory where main data should go after commit
     * @param workDir directory where transactions store temporary data 
     * @param urlEncodePath if set to <code>true</code> encodes all paths to allow for any kind of characters
     * @param logger the logger to be used by this store
     * @param debug if set to <code>true</code> logs all locking information to "transaction.log" for debugging inspection 
     */
    public FileResourceManager(
        String storeDir,
        String workDir,
        boolean urlEncodePath,
        LoggerFacade logger,
        boolean debug) {
        this.workDir = workDir;
        this.storeDir = storeDir;
        this.urlEncodePath = urlEncodePath;
        this.logger = logger;
        this.debug = debug;
    }

    /**
     * Gets the store directory.
     * 
     * @return the store directory
     * @see #FileResourceManager(String, String, boolean, LoggerFacade)
     * @see #FileResourceManager(String, String, boolean, LoggerFacade, boolean)
     */
    public String getStoreDir() {
        return storeDir;
    }

    /**
     * Gets the working directory.
     * 
     * @return the work directory
     * @see #FileResourceManager(String, String, boolean, LoggerFacade)
     * @see #FileResourceManager(String, String, boolean, LoggerFacade, boolean)
     */
    public String getWorkDir() {
        return workDir;
    }

    /**
     * Gets the logger used by this resource manager. 
     * 
     * @return used logger 
     */
    public LoggerFacade getLogger() {
        return logger;
    }

    /*
     * --- public methods of interface ResourceManager ---
     *
     *  
     */

    public boolean lockResource(Object resourceId, Object txId) throws ResourceManagerException {
        return lockResource(resourceId, txId, false);
    }

    public boolean lockResource(Object resourceId, Object txId, boolean shared) throws ResourceManagerException {
        return lockResource(resourceId, txId, shared, true, Long.MAX_VALUE, true);
    }

    public boolean lockResource(
        Object resourceId,
        Object txId,
        boolean shared,
        boolean wait,
        long timeoutMSecs,
        boolean reentrant)
        throws ResourceManagerException {

        TransactionContext context = (shared ? txInitialSaneCheck(txId) : txInitialSaneCheckForWriting(txId));
        assureNotMarkedForRollback(context);
        fileInitialSaneCheck(txId, resourceId);

        // XXX allows locking of non existent resources (e.g. to prepare a create)
        int level = (shared ? getSharedLockLevel(context) : LOCK_EXCLUSIVE);
        MultiLevelLock rl;
        synchronized (lockManager) {
            rl = lockManager.atomicGetOrCreateLock(resourceId);
            context.getLocks().add(rl);
        }
        try {
            return rl.acquire(txId, level, wait, reentrant, Math.min(timeoutMSecs, context.timeoutMSecs));
        } catch (InterruptedException e) {
            throw new ResourceManagerSystemException(ERR_SYSTEM, txId, e);
        }
    }

    public int getDefaultIsolationLevel() {
        return DEFAULT_ISOLATION_LEVEL;
    }

    public int[] getSupportedIsolationLevels() throws ResourceManagerException {
        return new int[] { ISOLATION_LEVEL_READ_COMMITTED, ISOLATION_LEVEL_REPEATABLE_READ };
    }

    public boolean isIsolationLevelSupported(int level) throws ResourceManagerException {
        return (level == ISOLATION_LEVEL_READ_COMMITTED || level == ISOLATION_LEVEL_REPEATABLE_READ);
    }

    /**
     * Gets the default transaction timeout in <em>milliseconds</em>.
     */
    public long getDefaultTransactionTimeout() {
        return defaultTimeout;
    }

    /**
     * Sets the default transaction timeout.
     * 
     * @param timeout timeout in <em>milliseconds</em>
     */
    public void setDefaultTransactionTimeout(long timeout) {
        defaultTimeout = timeout;
    }

    public long getTransactionTimeout(Object txId) throws ResourceManagerException {
        assureRMReady();
        long msecs = 0;
        TransactionContext context = getContext(txId);
        if (context == null) {
            msecs = getDefaultTransactionTimeout();
        } else {
            msecs = context.timeoutMSecs;
        }
        return msecs;
    }

    public void setTransactionTimeout(Object txId, long mSecs) throws ResourceManagerException {
        assureRMReady();
        TransactionContext context = getContext(txId);
        if (context != null) {
            context.timeoutMSecs = mSecs;
        } else {
            throw new ResourceManagerException(ERR_NO_TX, txId);
        }
    }

    public int getIsolationLevel(Object txId) throws ResourceManagerException {
        assureRMReady();
        TransactionContext context = getContext(txId);
        if (context == null) {
            return DEFAULT_ISOLATION_LEVEL;
        } else {
            return context.isolationLevel;
        }
    }

    public void setIsolationLevel(Object txId, int level) throws ResourceManagerException {
        assureRMReady();
        TransactionContext context = getContext(txId);
        if (context != null) {
            if (level != ISOLATION_LEVEL_READ_COMMITTED || level != ISOLATION_LEVEL_REPEATABLE_READ) {
                context.isolationLevel = level;
            } else {
                throw new ResourceManagerException(ERR_ISOLATION_LEVEL_UNSUPPORTED, txId);
            }
        } else {
            throw new ResourceManagerException(ERR_NO_TX, txId);
        }
    }

    public synchronized void start() throws ResourceManagerSystemException {

        logger.logInfo("Starting RM at '" + storeDir + "' / '" + workDir + "'");

        operationMode = OPERATION_MODE_STARTING;

        globalTransactions = Collections.synchronizedMap(new HashMap());
        lockManager = new GenericLockManager(LOCK_COMMIT, logger);
        globalOpenResources = Collections.synchronizedList(new ArrayList());

        recover();
        sync();

        operationMode = OPERATION_MODE_STARTED;

        if (dirty) {
            logger.logWarning("Started RM, but in dirty mode only (Recovery of pending transactions failed)");
        } else {
            logger.logInfo("Started RM");
        }

    }

    public synchronized boolean stop(int mode) throws ResourceManagerSystemException {
        return stop(mode, getDefaultTransactionTimeout() * DEFAULT_COMMIT_TIMEOUT_FACTOR);
    }

    public synchronized boolean stop(int mode, long timeOut) throws ResourceManagerSystemException {

        logger.logInfo("Stopping RM at '" + storeDir + "' / '" + workDir + "'");

        operationMode = OPERATION_MODE_STOPPING;

        sync();
        boolean success = shutdown(mode, timeOut);

        releaseGlobalOpenResources();

        if (success) {
            operationMode = OPERATION_MODE_STOPPED;
            logger.logInfo("Stopped RM");
        } else {
            logger.logWarning("Failed to stop RM");
        }

        return success;
    }

    public synchronized boolean recover() throws ResourceManagerSystemException {
        if (operationMode != OPERATION_MODE_STARTED && operationMode != OPERATION_MODE_STARTING) {
            throw new ResourceManagerSystemException(
                ERR_SYSTEM,
                "Recovery is possible in started or starting resource manager only");
        }
        int oldMode = operationMode;
        operationMode = OPERATION_MODE_RECOVERING;

        recoverContexts();
        if (globalTransactions.size() > 0) {
            logger.logInfo("Recovering pending transactions");
        }

        dirty = !rollBackOrForward();

        operationMode = oldMode;
        return dirty;
    }

    public int getTransactionState(Object txId) throws ResourceManagerException {
        TransactionContext context = getContext(txId);

        if (context == null) {
            return STATUS_NO_TRANSACTION;
        } else {
            return context.status;
        }

    }

    public void startTransaction(Object txId) throws ResourceManagerException {

        if (logger.isFineEnabled()) logger.logFine("Starting Tx " + txId);

        assureStarted(); // can only start a new transaction when not already stopping
        if (txId == null || txId.toString().length() == 0) {
            throw new ResourceManagerException(ERR_TXID_INVALID, txId);
        }

        // be sure we are the only ones who create this tx 
        synchronized (globalTransactions) {
            TransactionContext context = getContext(txId);

            if (context != null) {
                throw new ResourceManagerException(ERR_DUP_TX, txId);
            }

            context = new TransactionContext(txId);
            context.init();
            globalTransactions.put(txId, context);

        }
    }

    public void markTransactionForRollback(Object txId) throws ResourceManagerException {
        assureRMReady();
        TransactionContext context = txInitialSaneCheckForWriting(txId);
        try {
            context.status = STATUS_MARKED_ROLLBACK;
            context.saveState();
        } finally {
            // be very sure to free locks and resources, as application might crash or otherwise forget to roll this tx back
            context.finalCleanUp();
        }
    }

    public int prepareTransaction(Object txId) throws ResourceManagerException {
        assureRMReady();
        // do not allow any further writing or commit or rollback when db is corrupt
        if (dirty) {
            throw new ResourceManagerSystemException(
                "Database is set to dirty, this *may* mean it is corrupt. No modifications are allowed until a recovery run has been performed!",
                ERR_SYSTEM,
                txId);
        }

        if (txId == null) {
            throw new ResourceManagerException(ERR_TXID_INVALID, txId);
        }

        TransactionContext context = getContext(txId);

        if (context == null) {
            return PREPARE_FAILURE;
        }

        synchronized (context) {

            sync();

            if (context.status != STATUS_ACTIVE) {
                context.status = STATUS_MARKED_ROLLBACK;
                context.saveState();
                return PREPARE_FAILURE;
            }

            if (logger.isFineEnabled()) logger.logFine("Preparing Tx " + txId);

            int prepareStatus = PREPARE_FAILURE;

            context.status = STATUS_PREPARING;
            context.saveState();
            // do all checks as early as possible
            context.closeResources();
            if (context.readOnly) {
                prepareStatus = PREPARE_SUCCESS_READONLY;
            } else {
                // do all checks as early as possible
                try {
                    context.upgradeLockToCommit();
                } catch (ResourceManagerException rme) {
                    // if this did not work, mark it for roll back as early as possible
                    markTransactionForRollback(txId);
                    throw rme;
                }
                prepareStatus = PREPARE_SUCCESS;
            }
            context.status = STATUS_PREPARED;
            context.saveState();
            if (logger.isFineEnabled()) logger.logFine("Prepared Tx " + txId);

            return prepareStatus;
        }
    }

    public void rollbackTransaction(Object txId) throws ResourceManagerException {
        assureRMReady();
        TransactionContext context = txInitialSaneCheckForWriting(txId);
        // needing synchronization in order not to interfer with shutdown thread
        synchronized (context) {
            try {

                if (logger.isFineEnabled()) logger.logFine("Rolling back Tx " + txId);

                context.status = STATUS_ROLLING_BACK;
                context.saveState();
                context.rollback();
                context.status = STATUS_ROLLEDBACK;
                context.saveState();
                globalTransactions.remove(txId);
                context.cleanUp();

                if (logger.isFineEnabled()) logger.logFine("Rolled back Tx " + txId);

                // any system or runtime exceptions or errors thrown in rollback means we are in deep trouble, set the dirty flag
            } catch (Error e) {
                setDirty(txId, e);
                throw e;
            } catch (RuntimeException e) {
                setDirty(txId, e);
                throw e;
            } catch (ResourceManagerSystemException e) {
                setDirty(txId, e);
                throw e;
            } finally {
                context.finalCleanUp();
                // tell shutdown thread this tx is finished
                context.notifyFinish();
            }
        }
    }

    public void commitTransaction(Object txId) throws ResourceManagerException {
        assureRMReady();
        TransactionContext context = txInitialSaneCheckForWriting(txId);
        assureNotMarkedForRollback(context);

        // needing synchronization in order not to interfer with shutdown thread
        synchronized (context) {
            try {

                if (logger.isFineEnabled()) logger.logFine("Committing Tx " + txId);

                context.status = STATUS_COMMITTING;
                context.saveState();
                context.commit();
                context.status = STATUS_COMMITTED;
                context.saveState();
                globalTransactions.remove(txId);
                context.cleanUp();

                if (logger.isFineEnabled()) logger.logFine("Committed Tx " + txId);

                // any system or runtime exceptions or errors thrown in rollback means we are in deep trouble, set the dirty flag
            } catch (Error e) {
                setDirty(txId, e);
                throw e;
            } catch (RuntimeException e) {
                setDirty(txId, e);
                throw e;
            } catch (ResourceManagerSystemException e) {
                setDirty(txId, e);
                throw e;
                // like "could not upgrade lock"
            } catch (ResourceManagerException e) {
                logger.logWarning("Could not commit tx " + txId + ", rolling back instead", e);
                rollbackTransaction(txId);
            } finally {
                context.finalCleanUp();
                // tell shutdown thread this tx is finished
                context.notifyFinish();
            }
        }
    }

    public boolean resourceExists(Object resourceId) throws ResourceManagerException {
        // create temporary light weight tx
        Object txId;
        TransactionContext context;
        synchronized (globalTransactions) {
            txId = generatedUniqueTxId();
            if (logger.isFinerEnabled())
                logger.logFiner("Creating temporary light weight tx " + txId + " to check for exists");
            context = new TransactionContext(txId);
            context.isLightWeight = true;
            // XXX higher isolation might be needed to make sure upgrade to commit lock always works
            context.isolationLevel = ISOLATION_LEVEL_READ_COMMITTED;
//            context.isolationLevel = ISOLATION_LEVEL_REPEATABLE_READ;
            globalTransactions.put(txId, context);
        }

        boolean exists = resourceExists(txId, resourceId);

        context.freeLocks();
        globalTransactions.remove(txId);
        if (logger.isFinerEnabled())
            logger.logFiner("Removing temporary light weight tx " + txId);

        return exists;
    }

    public boolean resourceExists(Object txId, Object resourceId) throws ResourceManagerException {
        assureLock(resourceId, txId, true);
        return (getPathForRead(txId, resourceId) != null);
    }

    public void deleteResource(Object txId, Object resourceId) throws ResourceManagerException {
        deleteResource(txId, resourceId, true);
    }

    public void deleteResource(Object txId, Object resourceId, boolean assureOnly) throws ResourceManagerException {

        if (logger.isFineEnabled()) logger.logFine(txId + " deleting " + resourceId);

        assureLock(resourceId, txId, false);

        if (getPathForRead(txId, resourceId) == null) {
            if (assureOnly) {
                return;
            }
            throw new ResourceManagerException("No such resource at '" + resourceId + "'", ERR_NO_SUCH_RESOURCE, txId);
        }
        String txDeletePath = getDeletePath(txId, resourceId);
        String mainPath = getMainPath(resourceId);
        try {

            // first undo change / create when there was one
            undoScheduledChangeOrCreate(txId, resourceId);

            // if there still is a file in main store, we need to schedule
            // a delete additionally
            if (FileHelper.fileExists(mainPath)) {
                FileHelper.createFile(txDeletePath);
            }
        } catch (IOException e) {
            throw new ResourceManagerSystemException(
                "Can not delete resource at '" + resourceId + "'",
                ERR_SYSTEM,
                txId,
                e);
        }
    }

    public void createResource(Object txId, Object resourceId) throws ResourceManagerException {
        createResource(txId, resourceId, true);
    }

    public void createResource(Object txId, Object resourceId, boolean assureOnly) throws ResourceManagerException {

        if (logger.isFineEnabled()) logger.logFine(txId + " creating " + resourceId);

        assureLock(resourceId, txId, false);

        if (getPathForRead(txId, resourceId) != null) {
            if (assureOnly) {
                return;
            }
            throw new ResourceManagerException(
                "Resource at '" + resourceId + "', already exists",
                ERR_RESOURCE_EXISTS,
                txId);
        }

        String txChangePath = getChangePath(txId, resourceId);
        try {

            // creation means either undoing a delete or actually scheduling a create
            if (!undoScheduledDelete(txId, resourceId)) {
                FileHelper.createFile(txChangePath);
            }

        } catch (IOException e) {
            throw new ResourceManagerSystemException(
                "Can not create resource at '" + resourceId + "'",
                ERR_SYSTEM,
                txId,
                e);
        }
    }

    /*
     * --- public methods of interface StreamableResourceManager ---
     *
     *  
     */

    public InputStream readResource(Object resourceId) throws ResourceManagerException {
        // create temporary light weight tx
        Object txId;
        synchronized (globalTransactions) {
            txId = generatedUniqueTxId();
            if (logger.isFinerEnabled())
                logger.logFiner("Creating temporary light weight tx " + txId + " for reading");
            TransactionContext context = new TransactionContext(txId);
            context.isLightWeight = true;
            // XXX higher isolation might be needed to make sure upgrade to commit lock always works
            context.isolationLevel = ISOLATION_LEVEL_READ_COMMITTED;
//            context.isolationLevel = ISOLATION_LEVEL_REPEATABLE_READ;
            globalTransactions.put(txId, context);
        }

        InputStream is = readResource(txId, resourceId);
        return is;
    }

    public InputStream readResource(Object txId, Object resourceId) throws ResourceManagerException {

        if (logger.isFineEnabled()) logger.logFine(txId + " reading " + resourceId);

        assureLock(resourceId, txId, true);

        String resourcePath = getPathForRead(txId, resourceId);
        if (resourcePath == null) {
            throw new ResourceManagerException("No such resource at '" + resourceId + "'", ERR_NO_SUCH_RESOURCE, txId);
        }

        File file = new File(resourcePath);
        try {
            FileInputStream stream = new FileInputStream(file);
            getContext(txId).registerResource(stream);
            return new InputStreamWrapper(stream, txId, resourceId);
        } catch (FileNotFoundException e) {
            throw new ResourceManagerSystemException("File '" + resourcePath + "' does not exist", ERR_SYSTEM, txId);
        }
    }

    public OutputStream writeResource(Object txId, Object resourceId) throws ResourceManagerException {

        if (logger.isFineEnabled()) logger.logFine(txId + " writing " + resourceId);

        assureLock(resourceId, txId, false);

        String resourcePath = getPathForWrite(txId, resourceId);
        if (resourcePath == null) {
            throw new ResourceManagerException("No such resource at '" + resourceId + "'", ERR_NO_SUCH_RESOURCE, txId);
        }

        File file = new File(resourcePath);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            TransactionContext context = getContext(txId);
            context.registerResource(stream);
            context.readOnly = false;
            return stream;
        } catch (FileNotFoundException e) {
            throw new ResourceManagerSystemException("File '" + resourcePath + "' does not exist", ERR_SYSTEM, txId);
        }
    }

    /*
     * --- additional public methods complementing implementation of interfaces ---
     *
     *  
     */

    /**
     * Resets the store by deleting work <em>and</em> store directory.
     */
    public synchronized void reset() {
        FileHelper.removeRec(new File(storeDir));
        FileHelper.removeRec(new File(workDir));
        new File(storeDir).mkdirs();
        new File(workDir).mkdirs();
    }

    /**
     * Synchronizes persistent data with caches. Is implemented with an empty
     * body, but called by other methods relying on synchronization. Subclasses
     * that utilize caching must implement this method reasonably.
     * 
     * @throws ResourceManagerSystemException if anything fatal hapened during synchonization
     */
    public synchronized void sync() throws ResourceManagerSystemException {
        //
    }

    /**
     * Generates a transaction identifier unique to this resource manager. To do so
     * it requires this resource manager to be started.
     * 
     * @return generated transaction identifier
     * @throws ResourceManagerSystemException if this resource manager has not been started, yet
     */
    public String generatedUniqueTxId() throws ResourceManagerSystemException {
        assureRMReady();
        String txId;
        synchronized (globalTransactions) {
            do {
                txId = Long.toHexString(System.currentTimeMillis());
                // XXX busy loop
            } while (getContext(txId) != null);
        }
        return txId;
    }

    /*
     * --- sane checks ---
     *
     *  
     */

    protected void fileInitialSaneCheck(Object txId, Object path) throws ResourceManagerException {
        if (path == null || path.toString().length() == 0) {
            throw new ResourceManagerException(ERR_RESOURCEID_INVALID, txId);
        }
    }

    protected void assureStarted() throws ResourceManagerSystemException {
        if (operationMode != OPERATION_MODE_STARTED) {
            throw new ResourceManagerSystemException("Resource Manager Service not started", ERR_SYSTEM, null);
        }
    }

    protected void assureRMReady() throws ResourceManagerSystemException {
        if (operationMode != OPERATION_MODE_STARTED && operationMode != OPERATION_MODE_STOPPING) {
            throw new ResourceManagerSystemException("Resource Manager Service not ready", ERR_SYSTEM, null);
        }
    }

    protected void assureNotMarkedForRollback(TransactionContext context) throws ResourceManagerException {
        if (context.status == STATUS_MARKED_ROLLBACK) {
            throw new ResourceManagerException(ERR_MARKED_FOR_ROLLBACK, context.txId);
        }
    }

    protected TransactionContext txInitialSaneCheckForWriting(Object txId) throws ResourceManagerException {
        assureRMReady();
        // do not allow any further writing or commit or rollback when db is corrupt
        if (dirty) {
            throw new ResourceManagerSystemException(
                "Database is set to dirty, this *may* mean it is corrupt. No modifications are allowed until a recovery run has been performed!",
                ERR_SYSTEM,
                txId);
        }
        return txInitialSaneCheck(txId);
    }

    protected TransactionContext txInitialSaneCheck(Object txId) throws ResourceManagerException {
        assureRMReady();
        if (txId == null) {
            throw new ResourceManagerException(ERR_TXID_INVALID, txId);
        }

        TransactionContext context = getContext(txId);

        if (context == null) {
            throw new ResourceManagerException(ERR_NO_TX, txId);
        }

        return context;
    }

    /*
     * --- General Helpers ---
     *
     *  
     */

    protected TransactionContext getContext(Object txId) {
        return (TransactionContext) globalTransactions.get(txId);
    }

    protected String assureLeadingSlash(Object pathObject) {
        String path = "";
        if (pathObject != null) {
            path = pathObject.toString();
            if (urlEncodePath) {
                try {
                    // XXX not allowed as for JDK1.4 
                    // except when provioded by openMDX.
                    path = URLEncoder.encode(path, "UTF-8");
                    // XXX weired replacement for the fine method above
                    // using this combination as a simple URLEncoder.encode without
                    // charset may fail depending on local settings
                    // for this reason the string will be encoded into base64 consisting
                    // of ascii characters only
                    // a further URL encoding is need as base64 might contain '/' which
                    // might be a problem for some file systems 
                    // path = new String(Base64.encodeBase64(path.getBytes("UTF-8")), "ASCII");
                    // path = URLEncoder.encode(path);
                } catch (UnsupportedEncodingException e) {
                    // ignore
                }
            }
            if (path.length() > 0 && path.charAt(0) != '/' && path.charAt(0) != '\\') {
                path = "/" + path;
            }
        }
        return path;
    }

    protected String getMainPath(Object path) {
        StringBuffer buf = new StringBuffer(storeDir.length() + path.toString().length() + 5);
        buf.append(storeDir).append(assureLeadingSlash(path));
        return buf.toString();
    }

    protected String getChangePath(Object txId, Object path) {
        StringBuffer buf = new StringBuffer(txId.toString().length() + path.toString().length()
                + WORK_CHANGE_DIR.length() + workDir.length() + 5);
        buf.append(workDir).append('/').append(txId.toString()).append('/').append(WORK_CHANGE_DIR).append(
                assureLeadingSlash(path));
        return buf.toString();
    }

    protected String getDeletePath(Object txId, Object path) {
        StringBuffer buf = new StringBuffer(txId.toString().length() + path.toString().length()
                + WORK_DELETE_DIR.length() + workDir.length() + 5);
        buf.append(workDir).append('/').append(txId.toString()).append('/').append(WORK_DELETE_DIR).append(
                assureLeadingSlash(path));
        return buf.toString();
    }

    protected boolean undoScheduledDelete(Object txId, Object resourceId) throws ResourceManagerException {
        String txDeletePath = getDeletePath(txId, resourceId);
        File deleteFile = new File(txDeletePath);
        if (deleteFile.exists()) {
            if (!deleteFile.delete()) {
                throw new ResourceManagerSystemException(
                    "Failed to undo delete of '" + resourceId + "'",
                    ERR_SYSTEM,
                    txId);
            }
            return true;
        }
        return false;
    }

    protected boolean undoScheduledChangeOrCreate(Object txId, Object resourceId) throws ResourceManagerException {
        String txChangePath = getChangePath(txId, resourceId);
        File changeFile = new File(txChangePath);
        if (changeFile.exists()) {
            if (!changeFile.delete()) {
                throw new ResourceManagerSystemException(
                    "Failed to undo change / create of '" + resourceId + "'",
                    ERR_SYSTEM,
                    txId);
            }
            return true;
        }
        return false;
    }

    protected String getPathForWrite(Object txId, Object resourceId) throws ResourceManagerException {
        try {
            // when we want to write, be sure to write to a local copy
            String txChangePath = getChangePath(txId, resourceId);
            if (!FileHelper.fileExists(txChangePath)) {
                FileHelper.createFile(txChangePath);
            }
            return txChangePath;
        } catch (IOException e) {
            throw new ResourceManagerSystemException(
                "Can not write to resource at '" + resourceId + "'",
                ERR_SYSTEM,
                txId,
                e);
        }
    }

    protected String getPathForRead(Object txId, Object resourceId) throws ResourceManagerException {

        String mainPath = getMainPath(resourceId);
        String txChangePath = getChangePath(txId, resourceId);
        String txDeletePath = getDeletePath(txId, resourceId);

        // now, this is gets bit complicated:

        boolean changeExists = FileHelper.fileExists(txChangePath);
        boolean deleteExists = FileHelper.fileExists(txDeletePath);
        boolean mainExists = FileHelper.fileExists(mainPath);
        boolean resourceIsDir =
            ((mainExists && new File(mainPath).isDirectory())
                || (changeExists && new File(txChangePath).isDirectory()));
        if (resourceIsDir) {
            logger.logWarning("Resource at '" + resourceId + "' maps to directory");
        }

        // first do some sane checks

        // this may never be, two cases are possible, both disallowing to have a delete together with a change
        // 1. first there was a change, than a delete -> at least delete file exists (when there is a file in main store)
        // 2. first there was a delete, than a change -> only change file exists
        if (!resourceIsDir && changeExists && deleteExists) {
            throw new ResourceManagerSystemException(
                "Inconsistent delete and change combination for resource at '" + resourceId + "'",
                ERR_TX_INCONSISTENT,
                txId);
        }

        // you should not have been allowed to delete a file that does not exist at all
        if (deleteExists && !mainExists) {
            throw new ResourceManagerSystemException(
                "Inconsistent delete for resource at '" + resourceId + "'",
                ERR_TX_INCONSISTENT,
                txId);
        }

        if (changeExists) {
            return txChangePath;
        } else if ((mainExists && !deleteExists)) {
            return mainPath;
        } else {
            return null;
        }
    }

    /*
     * --- Locking Helpers ---
     *
     *  
     */

    protected int getSharedLockLevel(TransactionContext context) throws ResourceManagerException {
        if (context.isolationLevel == ISOLATION_LEVEL_READ_COMMITTED
            || context.isolationLevel == ISOLATION_LEVEL_READ_UNCOMMITTED) {
            return LOCK_ACCESS;
        } else if (
            context.isolationLevel == ISOLATION_LEVEL_REPEATABLE_READ
                || context.isolationLevel == ISOLATION_LEVEL_SERIALIZABLE) {
            return LOCK_SHARED;
        } else {
            return LOCK_ACCESS;
        }
    }

    protected void assureLock(Object resourceId, Object txId, boolean shared) throws ResourceManagerException {

        if (!lockResource(resourceId, txId, shared)) {
            throw new ResourceManagerException(
                "Could not get lock for resource at '" + resourceId + "'",
                ERR_NO_LOCK,
                txId);
        }
    }

    /*
     * --- Resource Management ---
     *
     *  
     */

    protected void registerOpenResource(Object openResource) {
        if (logger.isFinerEnabled())
            logger.logFiner("Registering open resource " + openResource);
        globalOpenResources.add(openResource);
    }

    protected void releaseGlobalOpenResources() {
        ArrayList copy;
        synchronized (globalOpenResources) {
            // XXX need to copy in order to allow removal in releaseOpenResource  
            copy = new ArrayList(globalOpenResources);
            for (Iterator it = copy.iterator(); it.hasNext();) {
                Object stream = it.next();
                closeOpenResource(stream);
            }
        }
    }

    protected void closeOpenResource(Object openResource) {
        if (logger.isFinerEnabled()) logger.logFiner("Releasing resource " + openResource);
        globalOpenResources.remove(openResource);
        if (openResource instanceof InputStream) {
            InputStream is = (InputStream) openResource;
            try {
                is.close();
            } catch (IOException e) {
                // do not care, as it might have been closed somewhere else, before 
            }
        } else if (openResource instanceof OutputStream) {
            OutputStream os = (OutputStream) openResource;
            try {
                os.close();
            } catch (IOException e) {
                // do not care, as it might have been closed somewhere else, before 
            }
        }
    }

    /*
     * --- Recovery / Shutdown Support ---
     *
     *  
     */

    protected boolean rollBackOrForward() {
        boolean allCool = true;

        synchronized (globalTransactions) {
            ArrayList contexts = new ArrayList(globalTransactions.values());
            for (Iterator it = contexts.iterator(); it.hasNext();) {
                TransactionContext context = (TransactionContext) it.next();
                if (context.status == STATUS_COMMITTING) {
                    // roll forward
                    logger.logInfo("Rolling forward " + context.txId);

                    try {
                        context.commit();
                        context.status = STATUS_COMMITTED;
                        context.saveState();
                        globalTransactions.remove(context.txId);
                        context.cleanUp();
                    } catch (ResourceManagerException e) {
                        // this is not good, but what can we do now?
                        allCool = false;
                        logger.logSevere("Rolling forward of " + context.txId + " failed", e);
                    }
                } else if (context.status == STATUS_COMMITTED) {
                    logger.logInfo("Cleaning already commited " + context.txId);
                    globalTransactions.remove(context.txId);
                    try {
                        context.cleanUp();
                    } catch (ResourceManagerException e) {
                        // this is not good, but what can we do now?
                        allCool = false;
                        logger.logWarning("Cleaning of " + context.txId + " failed", e);
                    }
                } else {
                    // in all other cases roll back and warn when not rollback was explicitely selected for tx
                    if (context.status != STATUS_ROLLING_BACK
                        && context.status != STATUS_ROLLEDBACK
                        && context.status != STATUS_MARKED_ROLLBACK) {
                        logger.logWarning("Irregularly rolling back " + context.txId);
                    } else {
                        logger.logInfo("Rolling back " + context.txId);
                    }
                    try {
                        context.rollback();
                        context.status = STATUS_ROLLEDBACK;
                        context.saveState();
                        globalTransactions.remove(context.txId);
                        context.cleanUp();
                    } catch (ResourceManagerException e) {
                        logger.logWarning("Rolling back of " + context.txId + " failed", e);
                    }
                    // 
                }
            }

        }
        return allCool;
    }

    protected void recoverContexts() {
        File dir = new File(workDir);
        File[] files = dir.listFiles();
        if (files == null)
            return;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String txId = file.getName();
            // recover all transactions we do not already know
            if (!globalTransactions.containsKey(txId)) {

                logger.logInfo("Recovering " + txId);
                TransactionContext context;
                try {
                    context = new TransactionContext(txId);
                    context.recoverState();
                    globalTransactions.put(txId, context);
                } catch (ResourceManagerException e) {
                    // this is not good, but the best we get, just log as warning
                    logger.logWarning("Recovering of " + txId + " failed");
                }
            }
        }
    }

    protected boolean waitForAllTxToStop(long timeoutMSecs) {
        long startTime = System.currentTimeMillis();

        // be sure not to lock globalTransactions for too long, as we need to give
        // txs the chance to complete (otherwise deadlocks are very likely to occur)
        // instead iterate over a copy as we can be sure no new txs will be registered
        // after operation level has been set to stopping

        Collection transactionsToStop;
        synchronized (globalTransactions) {
            transactionsToStop = new ArrayList(globalTransactions.values());
        }
        for (Iterator it = transactionsToStop.iterator(); it.hasNext();) {
            long remainingTimeout = startTime - System.currentTimeMillis() + timeoutMSecs;

            if (remainingTimeout <= 0) {
                return false;
            }

            TransactionContext context = (TransactionContext) it.next();
            synchronized (context) {
                if (!context.finished) {
                    logger.logInfo(
                        "Waiting for tx " + context.txId + " to finish for " + remainingTimeout + " milli seconds");
                }
                while (!context.finished && remainingTimeout > 0) {
                    try {
                        context.wait(remainingTimeout);
                    } catch (InterruptedException e) {
                        return false;
                    }
                    remainingTimeout = startTime - System.currentTimeMillis() + timeoutMSecs;
                }
                if (context.finished) {
                    logger.logInfo("Tx " + context.txId + " finished");
                } else {
                    logger.logWarning("Tx " + context.txId + " failed to finish in given time");
                }
            }
        }

        return (globalTransactions.size() == 0);
    }

    protected boolean shutdown(int mode, long timeoutMSecs) {
        switch (mode) {
            case SHUTDOWN_MODE_NORMAL :
                return waitForAllTxToStop(timeoutMSecs);
            case SHUTDOWN_MODE_ROLLBACK :
                return rollBackOrForward();
            case SHUTDOWN_MODE_KILL :
                return true;
            default :
                return false;
        }
    }

    protected void setDirty(Object txId, Throwable t) {
        logger.logSevere(
            "Fatal error during critical commit/rollback of transaction " + txId + ", setting database to dirty.",
            t);
        dirty = true;
    }

    /**
     * Inner class to hold the complete context, i.e. all information needed, for a transaction.
     * 
     */
    protected class TransactionContext {

        protected Object txId;
        protected int status = STATUS_ACTIVE;
        protected int isolationLevel = DEFAULT_ISOLATION_LEVEL;
        protected long timeoutMSecs = getDefaultTransactionTimeout();
        protected long startTime;
        protected long commitTime = -1L;
        protected boolean isLightWeight = false;
        protected boolean readOnly = true;
        protected boolean finished = false;

        private Collection locks = Collections.synchronizedCollection(new ArrayList());
        // list of streams participating in this tx
        private List openResourcs = new ArrayList();

        public TransactionContext(Object txId) throws ResourceManagerException {
            this.txId = txId;
            startTime = System.currentTimeMillis();
        }

        public Collection getLocks() {
            return locks;
        }

        public long getRemainingTimeout() {
            long now = System.currentTimeMillis();
            return (startTime - now + timeoutMSecs);
        }

        public synchronized void init() throws ResourceManagerException {
            String baseDir = workDir + "/" + txId;
            String changeDir = baseDir + "/" + WORK_CHANGE_DIR;
            String deleteDir = baseDir + "/" + WORK_DELETE_DIR;

            new File(changeDir).mkdirs();
            new File(deleteDir).mkdirs();

            saveState();
        }

        public synchronized void rollback() throws ResourceManagerException {
            closeResources();
            freeLocks();
        }

        public synchronized void commit() throws ResourceManagerException {
            String baseDir = workDir + "/" + txId;
            String changeDir = baseDir + "/" + WORK_CHANGE_DIR;
            String deleteDir = baseDir + "/" + WORK_DELETE_DIR;

            closeResources();
            upgradeLockToCommit();
            try {
                applyDeletes(new File(deleteDir), new File(storeDir), new File(storeDir));
                FileHelper.moveRec(new File(changeDir), new File(storeDir));
            } catch (IOException e) {
                throw new ResourceManagerSystemException("Commit failed", ERR_SYSTEM, txId, e);
            }
            freeLocks();
            commitTime = System.currentTimeMillis();
        }

        public synchronized void notifyFinish() {
            finished = true;
            notifyAll();
        }

        public synchronized void cleanUp() throws ResourceManagerException {
            if (!cleanUp)
                return; // XXX for debugging only
            boolean clean = true;
            Exception cleanException = null;
            String baseDir = workDir + "/" + txId;
            FileHelper.removeRec(new File(baseDir));
            if (!clean) {
                throw new ResourceManagerSystemException(
                    "Clean up failed due to unreleasable lock",
                    ERR_SYSTEM,
                    txId,
                    cleanException);
            }
        }

        public synchronized void finalCleanUp() throws ResourceManagerException {
            closeResources();
            freeLocks();
        }

        public synchronized void upgradeLockToCommit() throws ResourceManagerException {
            for (Iterator it = locks.iterator(); it.hasNext();) {
                GenericLock lock = (GenericLock) it.next();
                // only upgrade if we had write access
                if (lock.getLockLevel(txId) == LOCK_EXCLUSIVE) {
                    try {
                        // in case of deadlocks, make failure of non-committing tx more likely
                        if (!lock
                            .acquire(
                                txId,
                                LOCK_COMMIT,
                                true,
                                true,
                                getDefaultTransactionTimeout() * DEFAULT_COMMIT_TIMEOUT_FACTOR)) {
                            throw new ResourceManagerException(
                                "Could not upgrade to commit lock for resource at '"
                                    + lock.getResourceId().toString()
                                    + "'",
                                ERR_NO_LOCK,
                                txId);
                        }
                    } catch (InterruptedException e) {
                        throw new ResourceManagerSystemException(ERR_SYSTEM, txId, e);
                    }
                }

            }
        }

        public synchronized void freeLocks() {
            for (Iterator it = locks.iterator(); it.hasNext();) {
                MultiLevelLock lock = (MultiLevelLock) it.next();
                lock.release(txId);
                it.remove();
            }
        }

        public synchronized void closeResources() {
            synchronized (globalOpenResources) {
                for (Iterator it = openResourcs.iterator(); it.hasNext();) {
                    Object stream = it.next();
                    closeOpenResource(stream);
                }
            }
        }

        public synchronized void registerResource(Object openResource) {
            synchronized (globalOpenResources) {
                registerOpenResource(openResource);
                openResourcs.add(openResource);
            }
        }

        public synchronized void saveState() throws ResourceManagerException {
            String statePath = workDir + "/" + txId + "/" + CONTEXT_FILE;
            File file = new File(statePath);
            BufferedWriter writer = null;
            try {
                OutputStream os = new FileOutputStream(file);
                writer = new BufferedWriter(new OutputStreamWriter(os, DEFAULT_PARAMETER_ENCODING));
                writer.write(toString());
            } catch (FileNotFoundException e) {
                String msg = "Saving status information to '" + statePath + "' failed! Could not create file";
                logger.logSevere(msg, e);
                throw new ResourceManagerSystemException(msg, ERR_SYSTEM, txId, e);
            } catch (IOException e) {
                String msg = "Saving status information to '" + statePath + "' failed";
                logger.logSevere(msg, e);
                throw new ResourceManagerSystemException(msg, ERR_SYSTEM, txId, e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        // ugnore
                    }

                }
            }
        }

        public synchronized void recoverState() throws ResourceManagerException {
            String statePath = workDir + "/" + txId + "/" + CONTEXT_FILE;
            File file = new File(statePath);
            BufferedReader reader = null;
            try {
                InputStream is = new FileInputStream(file);

                reader = new BufferedReader(new InputStreamReader(is, DEFAULT_PARAMETER_ENCODING));
                txId = reader.readLine();
                status = Integer.parseInt(reader.readLine());
                isolationLevel = Integer.parseInt(reader.readLine());
                timeoutMSecs = Long.parseLong(reader.readLine());
                startTime = Long.parseLong(reader.readLine());
            } catch (FileNotFoundException e) {
                String msg = "Recovering status information from '" + statePath + "' failed! Could not find file";
                logger.logSevere(msg, e);
                throw new ResourceManagerSystemException(msg, ERR_SYSTEM, txId);
            } catch (IOException e) {
                String msg = "Recovering status information from '" + statePath + "' failed";
                logger.logSevere(msg, e);
                throw new ResourceManagerSystemException(msg, ERR_SYSTEM, txId, e);
            } catch (Throwable t) {
                String msg = "Recovering status information from '" + statePath + "' failed";
                logger.logSevere(msg, t);
                throw new ResourceManagerSystemException(msg, ERR_SYSTEM, txId, t);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // ignore
                    }

                }
            }
        }

        public synchronized String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append(txId).append('\n');
            buf.append(Integer.toString(status)).append('\n');
            buf.append(Integer.toString(isolationLevel)).append('\n');
            buf.append(Long.toString(timeoutMSecs)).append('\n');
            buf.append(Long.toString(startTime)).append('\n');
            if (debug) {
                buf.append("----- Lock Debug Info -----\n");
                
                for (Iterator it = locks.iterator(); it.hasNext();) {
                    MultiLevelLock lock = (MultiLevelLock) it.next();
                    buf.append(lock.toString()+"\n");
                }
                
            }
            return buf.toString();
        }

    }

    private class InputStreamWrapper extends InputStream {
        private InputStream is;
        private Object txId;
        private Object resourceId;

        public InputStreamWrapper(InputStream is, Object txId, Object resourceId) {
            this.is = is;
            this.txId = txId;
            this.resourceId = resourceId;
        }

        public int read() throws IOException {
            return is.read();
        }

        public int read(byte b[]) throws IOException {
            return is.read(b);
        }

        public int read(byte b[], int off, int len) throws IOException {
            return is.read(b, off, len);
        }

        public int available() throws IOException {
            return is.available();
        }

        public void close() throws IOException {
            try {
                is.close();
            } finally {
                TransactionContext context;
                synchronized (globalTransactions) {
                    context = getContext(txId);
                    if (context == null) {
                        return;
                    }
                }
                synchronized (context) {
                    if (context.isLightWeight) {
                        if (logger.isFinerEnabled())
                            logger.logFiner("Upon close of resource removing temporary light weight tx " + txId);
                        context.freeLocks();
                        globalTransactions.remove(txId);
                    } else {
                        // release access lock in order to allow other transactions to commit
                        MultiLevelLock lock = lockManager.atomicGetOrCreateLock(resourceId);
                        if (lock.getLockLevel(txId) == LOCK_ACCESS) {
                            if (logger.isFinerEnabled()) {
	                            logger.logFiner(
	                                "Upon close of resource releasing access lock for tx "
	                                    + txId
	                                    + " on resource at "
	                                    + resourceId);
                            }
                            lock.release(txId);
                        }
                    }
                }
            }
        }

        public void mark(int readlimit) {
            is.mark(readlimit);
        }

        public void reset() throws IOException {
            is.reset();
        }

        public boolean markSupported() {
            return is.markSupported();

        }
    }
}
