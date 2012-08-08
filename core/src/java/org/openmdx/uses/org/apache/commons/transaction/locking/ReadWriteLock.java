/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/locking/ReadWriteLock.java,v 1.1 2005/03/24 13:43:56 hburger Exp $
 * $Revision: 1.1 $
 * $Date: 2005/03/24 13:43:56 $
 *
 * ====================================================================
 *
 * Copyright 2004 The Apache Software Foundation 
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

import org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade;

/**
 * Convenience implementation of a read/write lock based on {@link GenericLock}.
 * <br>
 * <br>
 * Reads are shared which means there can be any number of concurrent read
 * accesses allowed by this lock. Writes are exclusive. This means when there is
 * a write access no other access neither read nor write are allowed by this
 * lock. <br>
 * <br>
 * Calls to both {@link #acquireRead(Object, long)}and
 * {@link #acquireWrite(Object, long)}are blocking and reentrant. Blocking
 * means they will wait if they can not acquire the descired access, reentrant means that a lock
 * request by a specific owner will always be compatible with other accesses on this lock by the
 * same owner. E.g. if you already have a lock for writing and you try to acquire write access 
 * again you will not be blocked by this first lock, while others of course will be. This is the
 * natural way you already know from Java monitors and synchronized blocks.
 * 
 * @version $Revision: 1.1 $
 * @see GenericLock
 */
public class ReadWriteLock extends GenericLock {

    public static final int NO_LOCK = 0;

    public static final int READ_LOCK = 1;

    public static final int WRITE_LOCK = 2;

    /**
     * Creates a new read/write lock.
     * 
     * @param resourceId
     *            identifier for the resource associated to this lock
     * @param logger
     *            generic logger used for all kind of debug logging
     */
    public ReadWriteLock(Object resourceId, LoggerFacade logger) {
        super(resourceId, WRITE_LOCK, logger);
    }

    /**
     * Tries to acquire a blocking, reentrant read lock. A read lock is
     * compatible with other read locks, but not with a write lock.
     * 
     * @param ownerId
     *            a unique id identifying the entity that wants to acquire a
     *            certain lock level on this lock
     * @param timeoutMSecs
     *            if blocking is enabled by the <code>wait</code> parameter
     *            this specifies the maximum wait time in milliseconds
     * @return <code>true</code> if the lock actually was acquired
     * @throws InterruptedException
     *             when the thread waiting on this method is interrupted
     */
    public boolean acquireRead(Object ownerId, long timeoutMSecs) throws InterruptedException {
        return acquire(ownerId, READ_LOCK, true, true, timeoutMSecs);
    }

    /**
     * Tries to acquire a blocking, reentrant write lock. A write lock is
     * incompatible with any another read or write lock and is thus exclusive.
     * 
     * @param ownerId
     *            a unique id identifying the entity that wants to acquire a
     *            certain lock level on this lock
     * @param timeoutMSecs
     *            if blocking is enabled by the <code>wait</code> parameter
     *            this specifies the maximum wait time in milliseconds
     * @return <code>true</code> if the lock actually was acquired
     * @throws InterruptedException
     *             when the thread waiting on this method is interrupted
     */
    public boolean acquireWrite(Object ownerId, long timeoutMSecs) throws InterruptedException {
        return acquire(ownerId, WRITE_LOCK, true, true, timeoutMSecs);
    }
}