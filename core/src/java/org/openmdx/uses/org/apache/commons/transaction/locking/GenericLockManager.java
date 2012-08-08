/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/locking/GenericLockManager.java,v 1.1 2005/03/24 13:43:56 hburger Exp $
 * $Revision: 1.1 $
 * $Date: 2005/03/24 13:43:56 $
 *
 * ====================================================================
 *
 * Copyright 1999-2004 The Apache Software Foundation 
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

package org.openmdx.uses.org.apache.commons.transaction.locking;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade;

/**
 * Manager for {@link GenericLock}s on resources.   
 * 
 * @version $Revision: 1.1 $
 */
public class GenericLockManager implements LockManager {

    protected Map globalLocks = new HashMap();
    protected int maxLockLevel = -1;
    protected LoggerFacade logger;

    public GenericLockManager(int maxLockLevel, LoggerFacade logger) throws IllegalArgumentException {
       if (maxLockLevel < 1)
           throw new IllegalArgumentException(
               "The maximum lock level must be at least 1 (" + maxLockLevel + " was specified)");
        this.maxLockLevel = maxLockLevel;
        this.logger = logger.createLogger("Locking");
    }

    public MultiLevelLock getLock(Object resourceId) {
        synchronized (globalLocks) {
            return (MultiLevelLock) globalLocks.get(resourceId);
        }
    }

    public MultiLevelLock atomicGetOrCreateLock(Object resourceId) {
        synchronized (globalLocks) {
            MultiLevelLock lock = getLock(resourceId);
            if (lock == null) {
                lock = createLock(resourceId);
            }
            return lock;
        }
    }

    public void removeLock(MultiLevelLock lock) {
        synchronized (globalLocks) {
            globalLocks.remove(lock);
        }
    }
    
    /**
     * Gets all locks as orignials, <em>no copies</em>.
     * 
     * @return collection holding all locks.
     */
    public Collection getLocks() {
        synchronized (globalLocks) {
           return globalLocks.values();
        }
    }

    protected GenericLock createLock(Object resourceId) {
        synchronized (globalLocks) {
            GenericLock lock = new GenericLock(resourceId, maxLockLevel, logger);
            globalLocks.put(resourceId, lock);
            return lock;
        }
    }
}
