/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/memory/OptimisticMapWrapper.java,v 1.2 2008/03/21 18:42:17 hburger Exp $
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * Wrapper that adds transactional control to all kinds of maps that implement the {@link Map} interface. By using
 * a naive optimistic transaction control this wrapper has better isolation than {@link TransactionalMapWrapper}, but
 * may also fail to commit. 
 *  
 * <br>
 * Start a transaction by calling {@link #startTransaction()}. Then perform the normal actions on the map and
 * finally either call {@link #commitTransaction()} to make your changes permanent or {@link #rollbackTransaction()} to
 * undo them.
 * <br>
 * <em>Caution:</em> Do not modify values retrieved by {@link #get(Object)} as this will circumvent the transactional mechanism.
 * Rather clone the value or copy it in a way you see fit and store it back using {@link #put(Object, Object)}.
 * <br>
 * <em>Note:</em> This wrapper guarantees isolation level <code>SERIALIZABLE</code>.
 * <br>
 * <em>Caution:</em> This implementation might be slow when large amounts of data is changed in a transaction as much references will need to be copied around.
 * 
 * @version $Revision: 1.2 $
 * @see TransactionalMapWrapper
 * @see PessimisticMapWrapper
 */
@SuppressWarnings("unchecked")
public class OptimisticMapWrapper extends TransactionalMapWrapper {

    protected Set activeTransactions;

    /**
     * Creates a new optimistic transactional map wrapper. Temporary maps and sets to store transactional
     * data will be instances of {@link java.util.HashMap} and {@link java.util.HashSet}. 
     * 
     * @param wrapped map to be wrapped
     */
    public OptimisticMapWrapper(Map wrapped) {
        this(wrapped, new HashMapFactory(), new HashSetFactory());
    }

    /**
     * Creates a new optimistic transactional map wrapper. Temporary maps and sets to store transactional
     * data will be created and disposed using {@link MapFactory} and {@link SetFactory}.
     * 
     * @param wrapped map to be wrapped
     * @param mapFactory factory for temporary maps
     * @param setFactory factory for temporary sets
     */
    public OptimisticMapWrapper(Map wrapped, MapFactory mapFactory, SetFactory setFactory) {
        super(wrapped, mapFactory, setFactory);
        activeTransactions = Collections.synchronizedSet(new HashSet());
    }

    public void startTransaction() {
        if (getActiveTx() != null) {
            throw new IllegalStateException(
                "Active thread " + Thread.currentThread() + " already associated with a transaction!");
        }
        CopyingTxContext context = new CopyingTxContext();
        activeTransactions.add(context);
        setActiveTx(context);
    }

    public void rollbackTransaction() {
        TxContext txContext = getActiveTx();
        super.rollbackTransaction();
        activeTransactions.remove(txContext);
    }

    public void commitTransaction() throws ConflictException {
        commitTransaction(false);
    }

    public void commitTransaction(boolean force) throws ConflictException {
        TxContext txContext = getActiveTx();

        if (txContext == null) {
            throw new IllegalStateException(
                "Active thread " + Thread.currentThread() + " not associated with a transaction!");
        }

        if (txContext.status == STATUS_MARKED_ROLLBACK) {
            throw new IllegalStateException("Active thread " + Thread.currentThread() + " is marked for rollback!");
        }

        if (!force) {
            Object conflictKey = checkForConflicts();
            if (conflictKey != null) {
                throw new ConflictException(conflictKey);
            }
        }

        // be sure commit is atomic
        synchronized (this) {
            activeTransactions.remove(txContext);
            copyChangesToConcurrentTransactions();
            super.commitTransaction();
        }
    }

    // TODO: Shouldn't we return a collection rather than a single key here?
    public Object checkForConflicts() {
        CopyingTxContext txContext = (CopyingTxContext) getActiveTx();

        Set keys = txContext.changedKeys();
        Set externalKeys = txContext.externalChangedKeys();

        for (Iterator it2 = keys.iterator(); it2.hasNext();) {
            Object key = it2.next();
            if (externalKeys.contains(key)) {
                return key;
            }
        }
        return null;
    }

    protected void copyChangesToConcurrentTransactions() {
        CopyingTxContext thisTxContext = (CopyingTxContext) getActiveTx();

        for (Iterator it = activeTransactions.iterator(); it.hasNext();) {
            CopyingTxContext otherTxContext = (CopyingTxContext) it.next();

            // no need to copy data if the other transaction does not access global map anyway
            if (otherTxContext.cleared)
                continue;

            if (thisTxContext.cleared) {
                // we will clear everything, so we have to copy everything before
                otherTxContext.externalChanges.putAll(wrapped);
            } else // no need to check if we have already copied everthing
                {

                for (Iterator it2 = thisTxContext.changes.entrySet().iterator(); it2.hasNext();) {
                    Map.Entry entry = (Map.Entry) it2.next();
                    Object value = wrapped.get(entry.getKey());
                    if (value != null) {
                        // undo change
                        otherTxContext.externalChanges.put(entry.getKey(), value);
                    } else {
                        // undo add
                        otherTxContext.externalDeletes.add(entry.getKey());
                    }
                }

                for (Iterator it2 = thisTxContext.deletes.iterator(); it2.hasNext();) {
                    // undo delete
                    Object key = it2.next();
                    Object value = wrapped.get(key);
                    otherTxContext.externalChanges.put(key, value);
                }
            }
        }

    }

    public class CopyingTxContext extends TxContext {
        protected Map externalChanges;
        protected Map externalAdds;
        protected Set externalDeletes;

        protected CopyingTxContext() {
            super();
            externalChanges = mapFactory.createMap();
            externalDeletes = setFactory.createSet();
            externalAdds = mapFactory.createMap();
        }

        protected Set externalChangedKeys() {
            Set keySet = new HashSet();
            keySet.addAll(externalDeletes);
            keySet.addAll(externalChanges.keySet());
            keySet.addAll(externalAdds.keySet());
            return keySet;
        }

        protected Set changedKeys() {
            Set keySet = new HashSet();
            keySet.addAll(deletes);
            keySet.addAll(changes.keySet());
            keySet.addAll(adds.keySet());
            return keySet;
        }

        protected Set keys() {
            Set keySet = super.keys();
            keySet.removeAll(externalDeletes);
            keySet.addAll(externalAdds.keySet());
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
                if (externalDeletes.contains(key)) {
                    // reflects that entry has been deleted in this tx 
                    return null;
                }

                changed = externalChanges.get(key);
                if (changed != null) {
                    return changed;
                }

                added = externalAdds.get(key);
                if (added != null) {
                    return added;
                }

                // not modified in this tx
                return wrapped.get(key);
            }
        }

        protected int size() {
            int size = super.size();

            size -= externalDeletes.size();
            size += externalAdds.size();

            return size;
        }

        protected void clear() {
            super.clear();
            externalDeletes.clear();
            externalChanges.clear();
            externalAdds.clear();
        }

        protected void dispose() {
            super.dispose();
            setFactory.disposeSet(externalDeletes);
            externalDeletes = null;
            mapFactory.disposeMap(externalChanges);
            externalChanges = null;
            mapFactory.disposeMap(externalAdds);
            externalAdds = null;
        }

        protected void finalize() throws Throwable {
            activeTransactions.remove(this);
            super.finalize();
        }
    }
}
