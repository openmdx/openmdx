/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/locking/GenericLock.java,v 1.2 2008/03/21 18:42:16 hburger Exp $
 * $Revision: 1.2 $
 * $Date: 2008/03/21 18:42:16 $
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade;

/**
 * 
 * A generic implementaion of a simple multi level lock. 
 * 
 * <p>The idea is to have an ascending number of
 * lock levels ranging from <code>0</code> to <code>maxLockLevel</code> as specified in 
 * {@link #GenericLock(Object, int, LoggerFacade)}: the higher the lock level the stronger and more restrictive the lock. To determine which lock may coexist with other locks you have to imagine matching pairs of lock levels.
 * For each pair both parts allow for all lock levels less than or equal to the matching other part. Pairs are composed by the lowest and highest level not yet part of a pair and successively applying this 
 * method until no lock level is left. For an even amount of levels each level is part of exactly one pair.
 * For an odd amount the middle level is paired with itself. The highst lock level may coexist with the lowest one (<code>0</code>) which by definition means 
 * <code>NO LOCK</code>. This implies that you will have to specify at least one other lock level and thus 
 * set <code>maxLockLevel</code> to at least <code>1</code>.</p>
 * 
 * <p>Although this may sound complicated, in practice this is quite simple. Let us imagine you have three lock levels:
 * <ul>
 * <li><code>0</code>: <code>NO LOCK</code> (always needed by the
 * implementation of this lock)
 * <li><code>1</code>: <code>SHARED</code>
 * <li><code>2</code>: <code>EXCLUSIVE</code>
 * </ul>
 * Accordingly, you will have to set <code>maxLockLevel</code> to <code>2</code>.
 * Now, there are two pairs of levels
 * <ul>
 * <li><code>NO LOCK</code> with <code>EXCLUSIVE</code>
 * <li><code>SHARED</code> with <code>SHARED</code>
 * </ul>
 * This means when the current highest lock level is
 * <code>NO LOCK</code> everything less or equal to
 * <code>EXCLUSIVE</code> is allowed - which means every other lock
 * level. On the other side <code>EXCLUSIVE</code> allows exacly for
 * <code>NO LOCK</code> - which means nothing else. In conclusion,
 * <code>SHARED</code> allows for <code>SHARED</code> or <code>NO
 * LOCK</code>, but not for <code>EXCLUSIVE</code>. To make this very
 * clear have a look at this table, where <code>o</code> means
 * compatible or can coexist and <code>x</code> means incompatible or can
 * not coexist:</p>
 * <table>
 *   <tbody>
 *     <tr>
 *       <td align="center"></td>
 *       <td align="center">NO LOCK</td>
 *       <td align="center">SHARED</td><td align="center">EXCLUSIVE</td>
 *     </tr>
 *     <tr><td align="center">NO LOCK</td><td align="center">o</td><td align="center">o</td><td align="center">o</td>
 *     </tr>
 *     <tr><td align="center">SHARED</td><td align="center">o</td><td align="center">o</td><td align="center">x</td>
 *     </tr>
 *     <tr><td align="center">EXCLUSIVE</td><td align="center" align="center">o</td><td align="center">x</td><td align="center">x</td>
 *     </tr>
 *   </tbody>
 * </table>
 * 
 * </p> 
 * 
 * General limitations include:<br>
 * <ul>
 * <li>You are restricted to the scheme described above
 * <li>There are no preferences configurable for lock levels. This means whenever more than
 * one party waits for a lock you can not specify which one is to be preferred. Under certain circumstances
 * I might be nice to have the opportunity to give priority to parties either applying for 
 * higher or lower lock levels. It might even be desirable not only to give priority to higher or lower locks,
 * but to a special level. For example you do not want to prefer higher lock levels, but exactly lock 
 * level <code>3</code> which might stand for <code>WRITE</code> access.
 * <li>It is not possible to represent hierarchical locks, i.e. locks having descendants
 * that are locked whenever the lock itself is locked. Consequently, there also is no notion
 * of an intention lock.
 * <li>You can not specify a timeframe for the validity of a lock. This means an owner of a thread
 * can never lose a lock except when <em>actively</em> releasing it. This is bad when an owner either
 * forgets to release a lock or is not able to do so due to error states or abnormal termination.  
 * </ul>
 * 
 * @version $Revision: 1.2 $
 */
@SuppressWarnings("unchecked")
public class GenericLock implements MultiLevelLock {

    public static final int COMPATIBILITY_NONE = 0;
    public static final int COMPATIBILITY_REENTRANT = 1;
    public static final int COMPATIBILITY_SUPPORT = 2;
    public static final int COMPATIBILITY_REENTRANT_AND_SUPPORT = 3;
    
    private Object resourceId;
    private Map owners = new HashMap();
    private int maxLockLevel;
    protected LoggerFacade logger;

    /**
     * Creates a new lock.
     * 
     * @param resourceId identifier for the resource associated to this lock
     * @param maxLockLevel highest allowed lock level as described in class intro 
     * @param logger generic logger used for all kind of debug logging
     */
    public GenericLock(Object resourceId, int maxLockLevel, LoggerFacade logger) {
        if (maxLockLevel < 1)
            throw new IllegalArgumentException(
                "The maximum lock level must be at least 1 (" + maxLockLevel + " was specified)");
        this.resourceId = resourceId;
        this.maxLockLevel = maxLockLevel;
        this.logger = logger;
    }

    /**
     * Tests if a certain lock level could be acquired.
     * 
     * @param ownerId a unique id identifying the entity that wants to acquire a certain lock level on this lock
     * @param targetLockLevel the lock level to acquire
     * @param compatibility 
     *            {@link #COMPATIBILITY_NONE} if no additional compatibility is
     *            desired (same as reentrant set to false) ,
     *            {@link #COMPATIBILITY_REENTRANT} if lock level by the same
     *            owner shall not affect compatibility (same as reentrant set to
     *            true), or {@link #COMPATIBILITY_SUPPORT} if lock levels that
     *            are the same as the desired shall not affect compatibility, or finally
     * {@link #COMPATIBILITY_REENTRANT_AND_SUPPORT} which is a combination of reentrant and support
     * @return <code>true</code> if the lock could be acquired acquired at the time this method
     * was called
     */
    public boolean test(Object ownerId, int targetLockLevel, int compatibility) {
        boolean success = false;
        try {
            success = tryLock(ownerId, targetLockLevel, compatibility);
        } finally {
            release(ownerId);
        }
        return success;
    }
    
    /**
     * @see org.openmdx.uses.org.apache.commons.transaction.locking.MultiLevelLock#acquire(java.lang.Object,
     *      int, boolean, boolean, long)
     */
    public synchronized boolean acquire(Object ownerId, int targetLockLevel, boolean wait,
            boolean reentrant, long timeoutMSecs) throws InterruptedException {
        return acquire(ownerId, targetLockLevel, wait, reentrant ? COMPATIBILITY_REENTRANT
                : COMPATIBILITY_NONE, timeoutMSecs);
    }
        
    /**
     * Tries to acquire a certain lock level on this lock. Does the same as
     * {@link org.openmdx.uses.org.apache.commons.transaction.locking.MultiLevelLock#acquire(java.lang.Object, int, boolean, boolean, long)}
     * except that it allows for different compatibility settings. There is an
     * additional compatibility mode {@link #COMPATIBILITY_SUPPORT} that allows
     * equal lock levels not to interfere with each other. This is like an
     * additional shared compatibility and useful when you only want to make sure not to interfer
     * with lowe levels, but are fine with the same.
     * 
     * @param compatibility 
     *            {@link #COMPATIBILITY_NONE} if no additional compatibility is
     *            desired (same as reentrant set to false) ,
     *            {@link #COMPATIBILITY_REENTRANT} if lock level by the same
     *            owner shall not affect compatibility (same as reentrant set to
     *            true), or {@link #COMPATIBILITY_SUPPORT} if lock levels that
     *            are the same as the desired shall not affect compatibility, or finally
     * {@link #COMPATIBILITY_REENTRANT_AND_SUPPORT} which is a combination of reentrant and support
     * 
     * @see org.openmdx.uses.org.apache.commons.transaction.locking.MultiLevelLock#acquire(java.lang.Object,
     *      int, boolean, boolean, long)
     */
    public synchronized boolean acquire(
        Object ownerId,
        int targetLockLevel,
        boolean wait,
        int compatibility,
        long timeoutMSecs)
        throws InterruptedException {

        if (logger.isFinerEnabled()) {
	        logger.logFiner(
	            ownerId.toString()
	                + " trying to acquire lock for "
	                + resourceId.toString()
	                + " at level "
	                + targetLockLevel
	                + " at "
	                + System.currentTimeMillis());
        }

        if (tryLock(ownerId, targetLockLevel, compatibility)) {
            
            if (logger.isFinerEnabled()) {
	            logger.logFiner(
	                ownerId.toString()
	                    + " actually acquired lock for "
	                    + resourceId.toString()
	                    + " at "
	                    + System.currentTimeMillis());
            }

            return true;
        } else {
            if (!wait) {
                return false;
            } else {
                long started = System.currentTimeMillis();
                for (long remaining = timeoutMSecs;
                    remaining > 0;
                    remaining = timeoutMSecs - (System.currentTimeMillis() - started)) {

                    if (logger.isFinerEnabled()) {
	                    logger.logFiner(
	                        ownerId.toString()
	                            + " waiting on "
	                            + resourceId.toString()
	                            + " for msecs "
	                            + timeoutMSecs
	                            + " at "
	                            + System.currentTimeMillis());
                    }

                    wait(remaining);
                    if (tryLock(ownerId, targetLockLevel, compatibility)) {

                        if (logger.isFinerEnabled()) {
	                        logger.logFiner(
	                            ownerId.toString()
	                                + " waiting on "
	                                + resourceId.toString()
	                                + " eventually got the lock at "
	                                + System.currentTimeMillis());
                        }

                        return true;
                    }
                }
                return false;
            }
        }
    }

    /**
     * @see org.openmdx.uses.org.apache.commons.transaction.locking.MultiLevelLock#release(Object)
     */
    public synchronized void release(Object ownerId) {
        if (owners.remove(ownerId) != null) {
            if (logger.isFinerEnabled()) {
	            logger.logFiner(
	                ownerId.toString()
	                    + " releasing lock for "
	                    + resourceId.toString()
	                    + " at "
	                    + System.currentTimeMillis());
            }
            notifyAll();
        }
    }

    /**
     * @see org.openmdx.uses.org.apache.commons.transaction.locking.MultiLevelLock#getLockLevel(Object)
     */
    public synchronized int getLockLevel(Object ownerId) {
        LockOwner owner = (LockOwner) owners.get(ownerId);
        if (owner == null) {
            return 0;
        } else {
            return owner.lockLevel;
        }
    }

    /**
     * Gets the resource assotiated to this lock. 
     * 
     * @return identifier for the resource associated to this lock 
     */
    public Object getResourceId() {
        return resourceId;
    }

    /**
     * Gets the lowest lock level possible.
     * 
     * @return minimum lock level
     */
    public int getLevelMinLock() {
        return 0;
    }

    /**
     * Gets the highst lock level possible.
     * 
     * @return maximum lock level
     */
    public int getLevelMaxLock() {
        return maxLockLevel;
    }

    public Object getOwner() {
        LockOwner owner = getMaxLevelOwner();
        if (owner == null)
            return null;
        return owner.ownerId;
    }

    public synchronized String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(resourceId.toString()).append(":\n");

        for (Iterator it = owners.values().iterator(); it.hasNext();) {
            LockOwner owner = (LockOwner) it.next();
            buf.append("- ").append(owner.ownerId.toString()).append(": ").append(owner.lockLevel).append("\n");
        }
        return buf.toString();

    }

    protected synchronized LockOwner getMaxLevelOwner() {
        return getMaxLevelOwner(null, -1);
    }

    protected synchronized LockOwner getMaxLevelOwner(LockOwner reentrantOwner) {
        return getMaxLevelOwner(reentrantOwner, -1);
    }

    protected synchronized LockOwner getMaxLevelOwner(int supportLockLevel) {
        return getMaxLevelOwner(null, supportLockLevel);
    }

    protected synchronized LockOwner getMaxLevelOwner(LockOwner reentrantOwner, int supportLockLevel) {
        LockOwner maxOwner = null;
        for (Iterator it = owners.values().iterator(); it.hasNext();) {
            LockOwner owner = (LockOwner) it.next();
            if (owner.lockLevel != supportLockLevel && !owner.equals(reentrantOwner)
                    && (maxOwner == null || maxOwner.lockLevel < owner.lockLevel)) {
                maxOwner = owner;
            }
        }
        return maxOwner;
    }
    
    protected synchronized void setLockLevel(Object ownerId, LockOwner lock, int targetLockLevel) {
        // be sure there exists at most one lock per owner
        if (lock != null) {

            if (logger.isFinestEnabled()) {
	            logger.logFinest(
	                ownerId.toString()
	                    + " upgrading lock for "
	                    + resourceId.toString()
	                    + " to level "
	                    + targetLockLevel
	                    + " at "
	                    + System.currentTimeMillis());
            }

            lock.lockLevel = targetLockLevel;
        } else {

            if (logger.isFinestEnabled()) {
	            logger.logFinest(
	                ownerId.toString()
	                    + " getting new lock for "
	                    + resourceId.toString()
	                    + " at level "
	                    + targetLockLevel
	                    + " at "
	                    + System.currentTimeMillis());
            }

            owners.put(ownerId, new LockOwner(ownerId, targetLockLevel));
        }
    }

    protected synchronized boolean tryLock(Object ownerId, int targetLockLevel, int compatibility) {

        LockOwner myLock = (LockOwner) owners.get(ownerId);

        // determine highest owner        
        LockOwner highestOwner;
        if (compatibility == COMPATIBILITY_REENTRANT) {
            if (myLock != null && targetLockLevel <= myLock.lockLevel) {
                // we already have it
                return true;
            } else {
                // our own lock will not be compromised by ourself
                highestOwner = getMaxLevelOwner(myLock);
            }
        } else if (compatibility == COMPATIBILITY_SUPPORT) {
            // we are compatible with any other lock owner holding
            // the same lock level
            highestOwner = getMaxLevelOwner(targetLockLevel);

        } else if (compatibility == COMPATIBILITY_REENTRANT_AND_SUPPORT) {
            if (myLock != null && targetLockLevel <= myLock.lockLevel) {
                // we already have it
                return true;
            } else {
                // our own lock will not be compromised by ourself and same lock level 
                highestOwner = getMaxLevelOwner(myLock, targetLockLevel);
            }
        } else {
            highestOwner = getMaxLevelOwner();
        }

        // what is our current lock level?
        int currentLockLevel;
        if (highestOwner != null) {
            currentLockLevel = highestOwner.lockLevel;
        } else {
            currentLockLevel = getLevelMinLock();
        }

        // we are only allowed to acquire our locks if we do not compromise locks of any other lock owner
        if (targetLockLevel <= getLevelMaxLock() - currentLockLevel) {
            setLockLevel(ownerId, myLock, targetLockLevel);
            return true;
        } else {
            return false;
        }
    }

    private static class LockOwner {
        public Object ownerId;
        public int lockLevel;

        public LockOwner(Object ownerId, int lockLevel) {
            this.ownerId = ownerId;
            this.lockLevel = lockLevel;
        }
    }

}
