/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/memory/TransactionalMapWrapper.java,v 1.1 2005/03/24 13:43:56 hburger Exp $
 * $Revision: 1.1 $
 * $Date: 2005/03/24 13:43:56 $
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.transaction.Status;

/**
 * Wrapper that adds transactional control to all kinds of maps that implement the {@link Map} interface.
 * This wrapper has rather weak isolation, but is simply, neven blocks and commits will never fail for logical
 * reasons. 
 * <br>
 * Start a transaction by calling {@link #startTransaction()}. Then perform the normal actions on the map and
 * finally either call {@link #commitTransaction()} to make your changes permanent or {@link #rollbackTransaction()} to
 * undo them.
 * <br>
 * <em>Caution:</em> Do not modify values retrieved by {@link #get(Object)} as this will circumvent the transactional mechanism.
 * Rather clone the value or copy it in a way you see fit and store it back using {@link #put(Object, Object)}.
 * <br>
 * <em>Note:</em> This wrapper guarantees isolation level <code>READ COMMITTED</code> only. I.e. as soon a value
 * is committed in one transaction it will be immediately visible in all other concurrent transactions.
 * 
 * @version $Revision: 1.1 $
 * @see OptimisticMapWrapper
 * @see PessimisticMapWrapper
 */
public class TransactionalMapWrapper implements Map, Status {

    /** The map wrapped. */
    protected Map wrapped;

    /** Factory to be used to create temporary maps for transactions. */
    protected MapFactory mapFactory;
    /** Factory to be used to create temporary sets for transactions. */
    protected SetFactory setFactory;

    private ThreadLocal activeTx = new ThreadLocal();

    /**
     * Creates a new transactional map wrapper. Temporary maps and sets to store transactional
     * data will be instances of {@link java.util.HashMap} and {@link java.util.HashSet}. 
     * 
     * @param wrapped map to be wrapped
     */
    public TransactionalMapWrapper(Map wrapped) {
        this(wrapped, new HashMapFactory(), new HashSetFactory());
    }

    /**
     * Creates a new transactional map wrapper. Temporary maps and sets to store transactional
     * data will be created and disposed using {@link MapFactory} and {@link SetFactory}.
     * 
     * @param wrapped map to be wrapped
     * @param mapFactory factory for temporary maps
     * @param setFactory factory for temporary sets
     */
    public TransactionalMapWrapper(Map wrapped, MapFactory mapFactory, SetFactory setFactory) {
        this.wrapped = Collections.synchronizedMap(wrapped);
        this.mapFactory = mapFactory;
        this.setFactory = setFactory;
    }

    /**
     * Checks if any write operations have been performed inside this transaction.
     * 
     * @return <code>true</code> if no write opertation has been performed inside the current transaction,
     * <code>false</code> otherwise
     */
    public boolean isReadOnly() {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            throw new IllegalStateException(
                "Active thread " + Thread.currentThread() + " not associated with a transaction!");
        }

        return txContext.readOnly;
    }

    /**
     * Checks whether this transaction has been marked to allow a rollback as the only
     * valid outcome. This can be set my method {@link #markTransactionForRollback()} or might
     * be set internally be any fatal error. Once a transaction is marked for rollback there
     * is no way to undo this. A transaction that is marked for rollback can not be committed,
     * also rolled back. 
     * 
     * @return <code>true</code> if this transaction has been marked for a roll back
     * @see #markTransactionForRollback()
     */
    public boolean isTransactionMarkedForRollback() {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            throw new IllegalStateException(
                "Active thread " + Thread.currentThread() + " not associated with a transaction!");
        }

        return (txContext.status == Status.STATUS_MARKED_ROLLBACK);
    }

    /**
     * Marks the current transaction to allow only a rollback as valid outcome. 
     *
     * @see #isTransactionMarkedForRollback()
     */
    public void markTransactionForRollback() {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            throw new IllegalStateException(
                "Active thread " + Thread.currentThread() + " not associated with a transaction!");
        }

        txContext.status = Status.STATUS_MARKED_ROLLBACK;
    }

    /**
     * Suspends the transaction associated to the current thread. I.e. the associated between the 
     * current thread and the transaction is deleted. This is useful when you want to continue the transaction
     * in another thread later. Call {@link #resumeTransaction(TxContext)} - possibly in another thread than the current - 
     * to resume work on the transaction.  
     * <br><br>
     * <em>Caution:</em> When calling this method the returned identifier
     * for the transaction is the only remaining reference to the transaction, so be sure to remember it or
     * the transaction will be eventually deleted (and thereby rolled back) as garbage.
     * 
     * @return an identifier for the suspended transaction, will be needed to later resume the transaction by
     * {@link #resumeTransaction(TxContext)} 
     * 
     * @see #resumeTransaction(TxContext)
     */
    public TxContext suspendTransaction() {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            throw new IllegalStateException(
                "Active thread " + Thread.currentThread() + " not associated with a transaction!");
        }

        txContext.suspended = true;
        setActiveTx(null);
        return txContext;
    }

    /**
     * Resumes a transaction in the current thread that has previously been suspened by {@link #suspendTransaction()}.
     * 
     * @param suspendedTx the identifier for the transaction to be resumed, delivered by {@link #suspendTransaction()} 
     * 
     * @see #suspendTransaction()
     */
    public void resumeTransaction(TxContext suspendedTx) {
        if (getActiveTx() != null) {
            throw new IllegalStateException(
                "Active thread " + Thread.currentThread() + " already associated with a transaction!");
        }

        if (suspendedTx == null) {
            throw new IllegalStateException("No transaction to resume!");
        }

        if (!suspendedTx.suspended) {
            throw new IllegalStateException("Transaction to resume needs to be suspended!");
        }

        suspendedTx.suspended = false;
        setActiveTx(suspendedTx);
    }

    /**
     * Returns the state of the current transaction.
     * 
     * @return state of the current transaction as decribed in the {@link Status} interface.
     */
    public int getTransactionState() {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            return STATUS_NO_TRANSACTION;
        }
        return txContext.status;
    }

    /**
     * Starts a new transaction and associates it with the current thread. All subsequent changes in the same
     * thread made to the map are invisible from other threads until {@link #commitTransaction()} is called.
     * Use {@link #rollbackTransaction()} to discard your changes. After calling either method there will be
     * no transaction associated to the current thread any longer. 
    	 * <br><br>
     * <em>Caution:</em> Be careful to finally call one of those methods,
     * as otherwise the transaction will lurk around for ever.
     *
     * @see #commitTransaction()
     * @see #rollbackTransaction()
     */
    public void startTransaction() {
        if (getActiveTx() != null) {
            throw new IllegalStateException(
                "Active thread " + Thread.currentThread() + " already associated with a transaction!");
        }
        setActiveTx(new TxContext());
    }

    /**
     * Discards all changes made in the current transaction and deletes the association between the current thread
     * and the transaction.
     * 
     * @see #startTransaction()
     * @see #commitTransaction()
     */
    public void rollbackTransaction() {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            throw new IllegalStateException(
                "Active thread " + Thread.currentThread() + " not associated with a transaction!");
        }

        // simply forget about tx
        txContext.dispose();
        setActiveTx(null);
    }

    /**
     * Commits all changes made in the current transaction and deletes the association between the current thread
     * and the transaction.
     *  
     * @see #startTransaction()
     * @see #rollbackTransaction()
     */
    public void commitTransaction() {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            throw new IllegalStateException(
                "Active thread " + Thread.currentThread() + " not associated with a transaction!");
        }

        if (txContext.status == Status.STATUS_MARKED_ROLLBACK) {
            throw new IllegalStateException("Active thread " + Thread.currentThread() + " is marked for rollback!");
        }

        txContext.merge();
        txContext.dispose();
        setActiveTx(null);
    }

    //
    // Map methods
    // 

    /**
     * @see Map#clear() 
     */
    public void clear() {
        TxContext txContext = getActiveTx();
        if (txContext != null) {
            txContext.clear();
        } else {
            wrapped.clear();
        }
    }

    /**
     * @see Map#size() 
     */
    public int size() {
        TxContext txContext = getActiveTx();
        if (txContext != null) {
            return txContext.size();
        } else {
            return wrapped.size();
        }
    }

    /**
     * @see Map#isEmpty() 
     */
    public boolean isEmpty() {
        TxContext txContext = getActiveTx();
        if (txContext == null) {
            return wrapped.isEmpty();
        } else {
            return txContext.isEmpty();
        }
    }

    /**
     * @see Map#containsKey(java.lang.Object) 
     */
    public boolean containsKey(Object key) {
        return (get(key) != null);
    }

    /**
     * @see Map#containsValue(java.lang.Object) 
     */
    public boolean containsValue(Object value) {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            return wrapped.containsValue(value);
        } else {
            return values().contains(value);
        }
    }

    /**
     * @see Map#values() 
     */
    public Collection values() {

        TxContext txContext = getActiveTx();

        if (txContext == null) {
            return wrapped.values();
        } else {
            // XXX expensive :(
            Collection values = new ArrayList();
            for (Iterator it = keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = get(key);
                // XXX we have no isolation, so get entry might have been deleted in the meantime
                if (value != null) {
                    values.add(value);
                }
            }
            return values;
        }
    }

    /**
     * @see Map#putAll(java.util.Map) 
     */
    public void putAll(Map map) {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            wrapped.putAll(map);
        } else {
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                txContext.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * @see Map#entrySet() 
     */
    public Set entrySet() {
        TxContext txContext = getActiveTx();
        if (txContext == null) {
            return wrapped.entrySet();
        } else {
            Set entrySet = new HashSet();
            // XXX expensive :(
            for (Iterator it = keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                Object value = get(key);
                // XXX we have no isolation, so get entry might have been deleted in the meantime
                if (value != null) {
                    entrySet.add(new HashEntry(key, value));
                }
            }
            return entrySet;
        }
    }

    /**
     * @see Map#keySet() 
     */
    public Set keySet() {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            return wrapped.keySet();
        } else {
            return txContext.keys();
        }
    }

    /**
     * @see Map#get(java.lang.Object) 
     */
    public Object get(Object key) {
        TxContext txContext = getActiveTx();

        if (txContext != null) {
            return txContext.get(key);
        } else {
            return wrapped.get(key);
        }
    }

    /**
     * @see Map#remove(java.lang.Object) 
     */
    public Object remove(Object key) {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            return wrapped.remove(key);
        } else {
            Object oldValue = get(key);
            txContext.remove(key);
            return oldValue;
        }
    }

    /**
     * @see Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            return wrapped.put(key, value);
        } else {
            Object oldValue = get(key);
            txContext.put(key, value);
            return oldValue;
        }

    }

    protected TxContext getActiveTx() {
        return (TxContext) activeTx.get();
    }

    protected void setActiveTx(TxContext txContext) {
        activeTx.set(txContext);
    }

    // mostly copied from org.apache.commons.collections.map.AbstractHashedMap
    protected static class HashEntry implements Map.Entry {
        /** The key */
        protected Object key;
        /** The value */
        protected Object value;

        protected HashEntry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object value) {
            Object old = this.value;
            this.value = value;
            return old;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Map.Entry)) {
                return false;
            }
            Map.Entry other = (Map.Entry) obj;
            return (getKey() == null ? other.getKey() == null : getKey().equals(other.getKey()))
                && (getValue() == null ? other.getValue() == null : getValue().equals(other.getValue()));
        }

        public int hashCode() {
            return (getKey() == null ? 0 : getKey().hashCode()) ^ (getValue() == null ? 0 : getValue().hashCode());
        }

        public String toString() {
            return new StringBuffer().append(getKey()).append('=').append(getValue()).toString();
        }
    }

    public class TxContext {
        protected Set deletes;
        protected Map changes;
        protected Map adds;
        protected int status;
        protected boolean cleared;
        protected boolean readOnly;
        protected boolean suspended = false;

        protected TxContext() {
            deletes = setFactory.createSet();
            changes = mapFactory.createMap();
            adds = mapFactory.createMap();
            status = Status.STATUS_ACTIVE;
            cleared = false;
            readOnly = true;
        }

        protected Set keys() {
            Set keySet = new HashSet();
            if (!cleared) {
                keySet.addAll(wrapped.keySet());
            }
            keySet.addAll(adds.keySet());
            return keySet;
        }

        protected Object get(Object key) {

            if (deletes.contains(key)) {
                // reflects that entry has been deleted in this tx 
                return null;
            }

            Object changed = changes.get(key);
            if (changed != null) {
                return changed;
            }

            Object added = adds.get(key);
            if (added != null) {
                return added;
            }

            if (cleared) {
                return null;
            } else {
                // not modified in this tx
                return wrapped.get(key);
            }
        }

        protected void put(Object key, Object value) {
            try {
                readOnly = false;
                deletes.remove(key);
                if (wrapped.get(key) != null) {
                    changes.put(key, value);
                } else {
                    adds.put(key, value);
                }
            } catch (RuntimeException e) {
                status = Status.STATUS_MARKED_ROLLBACK;
                throw e;
            } catch (Error e) {
                status = Status.STATUS_MARKED_ROLLBACK;
                throw e;
            }
        }

        protected void remove(Object key) {

            try {
                readOnly = false;
                changes.remove(key);
                adds.remove(key);
                if (wrapped.containsKey(key) && !cleared) {
                    deletes.add(key);
                }
            } catch (RuntimeException e) {
                status = Status.STATUS_MARKED_ROLLBACK;
                throw e;
            } catch (Error e) {
                status = Status.STATUS_MARKED_ROLLBACK;
                throw e;
            }
        }

        protected int size() {
            int size = (cleared ? 0 : wrapped.size());

            size -= deletes.size();
            size += adds.size();

            return size;
        }

        protected void clear() {
            readOnly = false;
            cleared = true;
            deletes.clear();
            changes.clear();
            adds.clear();
        }

        protected boolean isEmpty() {
            return (size() == 0); 
        }

        protected void merge() {
            if (!readOnly) {

                if (cleared) {
                    wrapped.clear();
                }

                wrapped.putAll(changes);
                wrapped.putAll(adds);

                for (Iterator it = deletes.iterator(); it.hasNext();) {
                    Object key = it.next();
                    wrapped.remove(key);
                }
            }
        }

        protected void dispose() {
            setFactory.disposeSet(deletes);
            deletes = null;
            mapFactory.disposeMap(changes);
            changes = null;
            mapFactory.disposeMap(adds);
            adds = null;
            status = Status.STATUS_NO_TRANSACTION;
        }
    }
}
