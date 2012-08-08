/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/locking/LockManager.java,v 1.1 2005/03/24 13:43:56 hburger Exp $
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

/**
 * 
 * A manager for multi level locks on resources. Encapsulates creation, removal, and retrieval of locks.
 * Each resource can have at most a single lock. However, it may be possible for more than one
 * accessing entity to have influence on this lock via different lock levels that may be 
 * provided by the according implementation of {@link MultiLevelLock}. 
 * 
 * @version $Revision: 1.1 $
 * @see MultiLevelLock
 */
public interface LockManager {

    /**
     * Either gets an existing lock on the specified resource or creates one if none exists. 
     * This methods guarantees to do this atomically. 
     * 
     * @param resourceId the resource to get or create the lock on
     * @return the lock for the specified resource
     */
    public MultiLevelLock atomicGetOrCreateLock(Object resourceId);

    /**
     * Gets an existing lock on the specified resource. If none exists it returns <code>null</code>. 
     * 
     * @param resourceId the resource to get the lock for
     * @return the lock on the specified resource
     */
    public MultiLevelLock getLock(Object resourceId);

    /**
     * Removes the specified lock from the associated resource. 
     * 
     * @param lock the lock to be removed
     */
    public void removeLock(MultiLevelLock lock);
}
