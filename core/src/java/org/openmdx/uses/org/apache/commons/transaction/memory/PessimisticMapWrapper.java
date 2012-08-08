/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/memory/PessimisticMapWrapper.java,v 1.2 2008/03/21 18:42:17 hburger Exp $
 * $Revision: 1.2 $
 * $Date: 2008/03/21 18:42:17 $
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

package org.openmdx.uses.org.apache.commons.transaction.memory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openmdx.uses.org.apache.commons.transaction.locking.GenericLock;
import org.openmdx.uses.org.apache.commons.transaction.locking.GenericLockManager;
import org.openmdx.uses.org.apache.commons.transaction.locking.LockManager;
import org.openmdx.uses.org.apache.commons.transaction.locking.MultiLevelLock;
import org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade;

/**
 * Wrapper that adds transactional control to all kinds of maps that implement the {@link Map} interface. By using
 * pessimistic transaction control (blocking locks) this wrapper has better isolation than {@link TransactionalMapWrapper}, but
 * also has less possible concurrency and may even deadlock. A commit, however, will never fail.
 * <br>
 * Start a transaction by calling {@link #startTransaction()}. Then perform the normal actions on the map and
 * finally either call {@link #commitTransaction()} to make your changes permanent or {@link #rollbackTransaction()} to
 * undo them.
 * <br>
 * <em>Caution:</em> Do not modify values retrieved by {@link #get(Object)} as this will circumvent the transactional mechanism.
 * Rather clone the value or copy it in a way you see fit and store it back using {@link #put(Object, Object)}.
 * <br>
 * <em>Note:</em> This wrapper guarantees isolation level <code>SERIALIZABLE</code>.
 * 
 * @version $Revision: 1.2 $
 * @see TransactionalMapWrapper
 * @see OptimisticMapWrapper
 */
@SuppressWarnings("unchecked")
public class PessimisticMapWrapper extends TransactionalMapWrapper {

    protected static final int READ = 1;
    protected static final int WRITE = 2;

    protected static final String GLOBAL_LOCK_NAME = "GLOBAL";

    protected LockManager lockManager;
    protected MultiLevelLock globalLock;
    protected long readTimeOut = 60000; /* FIXME: pass in ctor */

    /**
     * Creates a new pessimistic transactional map wrapper. Temporary maps and sets to store transactional
     * data will be instances of {@link java.util.HashMap} and {@link java.util.HashSet}. 
     * 
     * @param wrapped map to be wrapped
     */
    public PessimisticMapWrapper(Map wrapped, LoggerFacade logger) {
        this(wrapped, new HashMapFactory(), new HashSetFactory(), logger);
    }

    /**
     * Creates a new pessimistic transactional map wrapper. Temporary maps and sets to store transactional
     * data will be created and disposed using {@link MapFactory} and {@link SetFactory}.
     * 
     * @param wrapped map to be wrapped
     * @param mapFactory factory for temporary maps
     * @param setFactory factory for temporary sets
     */
    public PessimisticMapWrapper(Map wrapped, MapFactory mapFactory, SetFactory setFactory, LoggerFacade logger) {
        super(wrapped, mapFactory, setFactory);
        lockManager = new GenericLockManager(WRITE, logger);
        globalLock = new GenericLock(GLOBAL_LOCK_NAME, WRITE, logger);
    }

    public void startTransaction() {
        if (getActiveTx() != null) {
            throw new IllegalStateException(
                "Active thread " + Thread.currentThread() + " already associated with a transaction!");
        }
        LockingTxContext context = new LockingTxContext();
        setActiveTx(context);
    }

    public Collection values() {
        assureGlobalLock(READ);
        return super.values();
    }

    public Set entrySet() {
        assureGlobalLock(READ);
        return super.entrySet();
    }

    public Set keySet() {
        assureGlobalLock(READ);
        return super.keySet();
    }

    public Object remove(Object key) {
        // assure we get a write lock before super can get a read lock to avoid lots
        // of deadlocks
        assureWriteLock(key);
        return super.remove(key);
    }

    public Object put(Object key, Object value) {
        // assure we get a write lock before super can get a read lock to avoid lots
        // of deadlocks
        assureWriteLock(key);
        return super.put(key, value);
    }

    protected void assureWriteLock(Object key) {
        LockingTxContext txContext = (LockingTxContext) getActiveTx();
        if (txContext != null) {
            txContext.lock(lockManager.atomicGetOrCreateLock(key), WRITE);
            txContext.lock(globalLock, READ); // XXX fake intention lock (prohibits global WRITE)
        }
    }
    
    protected void assureGlobalLock(int level) {
        LockingTxContext txContext = (LockingTxContext) getActiveTx();
        if (txContext != null) {
            txContext.lock(globalLock, level); // XXX fake intention lock (prohibits global WRITE)
        }
    }
    
    public class LockingTxContext extends TxContext {
        protected Set locks;

        protected LockingTxContext() {
            super();
            locks = new HashSet();
        }

        protected Set keys() {
            lock(globalLock, READ);
            return super.keys();
        }

        protected Object get(Object key) {
            lock(lockManager.atomicGetOrCreateLock(key), READ);
            lock(globalLock, READ); // XXX fake intention lock (prohibits global WRITE)
            return super.get(key);
        }

        protected void put(Object key, Object value) {
            lock(lockManager.atomicGetOrCreateLock(key), WRITE);
            lock(globalLock, READ); // XXX fake intention lock (prohibits global WRITE)
            super.put(key, value);
        }

        protected void remove(Object key) {
            lock(lockManager.atomicGetOrCreateLock(key), WRITE);
            lock(globalLock, READ); // XXX fake intention lock (prohibits global WRITE)
            super.remove(key);
        }

        protected int size() {
            // XXX this is bad luck, we need a global read lock just for the size :( :( :(
            lock(globalLock, READ);
            return super.size();
        }

        protected void clear() {
            lock(globalLock, WRITE);
            super.clear();
        }

        protected void dispose() {
            super.dispose();
            for (Iterator it = locks.iterator(); it.hasNext();) {
                MultiLevelLock lock = (MultiLevelLock) it.next();
                lock.release(this);
            }
        }

        protected void finalize() throws Throwable {
            dispose();
            super.finalize();
        }

        protected void lock(MultiLevelLock lock, int level) throws LockException {
            boolean acquired = false;
            try {
                acquired = lock.acquire(this, level, true, true, readTimeOut);
            } catch (InterruptedException e) {
                throw new LockException("Interrupted", LockException.CODE_INTERRUPTED, GLOBAL_LOCK_NAME);
            }
            if (!acquired) {
                throw new LockException("Timed out", LockException.CODE_TIMED_OUT, GLOBAL_LOCK_NAME);
            }
            locks.add(lock);
        }
    }

}
