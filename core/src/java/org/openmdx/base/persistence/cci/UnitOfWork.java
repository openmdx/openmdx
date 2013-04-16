/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Unit Of Work
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011-2012, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
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
package org.openmdx.base.persistence.cci;

import javax.jdo.PersistenceManager;


public interface UnitOfWork {

    /** Begin a unit of work.  The type of unit of work is determined by the
     * setting of the Optimistic flag.
     * @see #setOptimistic
     * @see #getOptimistic
     * @throws JDOUserException if units of work are managed by a container
     * in the managed environment, or if the unit of work is already active.
     */
    void begin();
    
    /** Commit the current unit of work.
     * @throws JDOUserException if units of work are managed by a container
     * in the managed environment, or if the unit of work is not active.
     */
    void commit();
    
    /** Roll back the current unit of work.
     * @throws JDOUserException if units of work are managed by a container
     * in the managed environment, or if the unit of work is not active.
     */
    void rollback();

    /** Returns whether there is a unit of work currently active.
     * @return <code>true</code> if the unit of work is active.
     */
    boolean isActive();
    
    /**
     * Returns the rollback-only status of the unit of work. When
     * begun, the rollback-only status is false. Either the 
     * application or the JDO implementation may set this flag
     * using setRollbackOnly.
     * @return <code>true</code> if the unit of work has been
     * marked for rollback.
     */
    boolean getRollbackOnly();

    /**
     * Sets the rollback-only status of the unit of work to <code>true</code>.
     * After this flag is set to <code>true</code>, the unit of work 
     * can no longer be committed, and any attempt to commit the 
     * unit of work will throw <code>JDOFatalDataStoreException<code>.
     */
    void setRollbackOnly();

    /** If <code>true</code>, allow persistent instances to be read without
     * a unit of work active.
     * If an implementation does not support this option, a 
     * <code>JDOUnsupportedOptionException</code> is thrown.
     * @param nontransactionalRead the value of the nontransactionalRead 
     * property
     */
    void setNontransactionalRead (boolean nontransactionalRead);
    
    /** If <code>true</code>, allows persistent instances to be read without
     * a unit of work active.
     * @return the value of the nontransactionalRead property
     */
    boolean getNontransactionalRead ();
    
    /** If <code>true</code>, allow persistent instances to be written without
     * a transaction active.
     * If an implementation does not support this option, a 
     * <code>JDOUnsupportedOptionException</code> is thrown.
     * @param nontransactionalWrite the value of the nontransactionalWrite 
     * property
     */
    void setNontransactionalWrite (boolean nontransactionalWrite);
    
    /** If <code>true</code>, allows persistent instances to be written without
     * a transaction active.
     * @return the value of the nontransactionalWrite property
     */
    boolean getNontransactionalWrite ();
    
    /** If <code>true</code>, at commit instances retain their values and the 
     * instances transition to persistent-nontransactional.
     * If an implementation does not support this option, a 
     * <code>JDOUnsupportedOptionException</code> is thrown.
     * @param retainValues the value of the retainValues property
     */
    void setRetainValues(boolean retainValues);
    
    /** If <code>true</code>, at commit time instances retain their field 
     * values.
     * @return the value of the retainValues property
     */
    boolean getRetainValues();
    
    /** If <code>true</code>, at rollback, fields of newly persistent instances 
     * are restored to 
     * their values as of the beginning of the unit of work, and the instances
     * revert to transient.  Additionally, fields of modified
     * instances of primitive types and immutable reference types
     * are restored to their values as of the beginning of the 
     * unit of work.
     * <P>If <code>false</code>, at rollback, the values of fields of 
     * newly persistent instances are unchanged and the instances revert to
     * transient.  Additionally, dirty instances transition to hollow.
     * If an implementation does not support this option, a 
     * <code>JDOUnsupportedOptionException</code> is thrown.
     * @param restoreValues the value of the restoreValues property
     */
    void setRestoreValues(boolean restoreValues);
    
    /** Return the current value of the restoreValues property.
     * @return the value of the restoreValues property
     */
    boolean getRestoreValues();
    
    /** Optimistic units of work do not hold data store locks until commit time.
     * If an implementation does not support this option, a 
     * <code>JDOUnsupportedOptionException</code> is thrown.
     * @param optimistic the value of the Optimistic flag.
     */
    void setOptimistic(boolean optimistic);
    
    /** Optimistic units of work do not hold data store locks until commit time.
     * @return the value of the Optimistic property.
     */
    boolean getOptimistic();
    
    /** Get the value for unit of work isolation level for this unit of work.
     * @return the unit of work isolation level
     * @see #setIsolationLevel(String)
     */
    String getIsolationLevel();

    /** Set the value for unit of work isolation level for this unit of work.
     * unit of work isolation levels are defined in javax.jdo.Constants.
     * If the requested level is not available, but a higher level is
     * available, the higher level is silently used. 
     * If the requested level is not available, and no higher level is
     * available, then JDOUnsupportedOptionException is thrown.
     * Five standard isolation levels are defined. Other isolation levels
     * might be supported by an implementation but are not standard.
     * <p>Standard values in order of low to high are:
     * <ul><li>read-uncommitted
     * </li><li>read-committed
     * </li><li>repeatable-read
     * </li><li>snapshot
     * </li><li>serializable
     * </li></ul>
     * @param level the unit of work isolation level
     * @see #getIsolationLevel()
     * @see Constants#TX_READ_UNCOMMITTED
     * @see Constants#TX_READ_COMMITTED
     * @see Constants#TX_REPEATABLE_READ
     * @see Constants#TX_SNAPSHOT
     * @see Constants#TX_SERIALIZABLE
     */
    void setIsolationLevel(String level);

    /** The user can specify a <code>Synchronization</code> instance to be 
     * notified on unit of work completions.  The <code>beforeCompletion</code> 
     * method is called prior to flushing instances to the data store.
     *
     * <P>The <code>afterCompletion</code> method is called after performing 
     * state transitions of persistent and unit of workal instances, following 
     * the data store commit or rollback operation.
     * <P>Only one <code>Synchronization</code> instance can be registered with 
     * the  <code>UnitOfWork</code>. If the application requires more than one 
     * instance to receive synchronization callbacks, then the single 
     * application instance is responsible for managing them, and forwarding 
     * callbacks to them.
     * @param sync the <code>Synchronization</code> instance to be notified; 
     * <code>null</code> for none
     */
    void setSynchronization(Synchronization sync);
    
    /** The user-specified <code>Synchronization</code> instance for this 
     * <code>UnitOfWork</code> instance.    
     * @return the user-specified <code>Synchronization</code> instance.
     */
    Synchronization getSynchronization();

    /** The <code>UnitOfWork</code> instance is always associated with exactly 
     * one <code>PersistenceManager</code>.
     *
     * @return the <code>PersistenceManager</code> for this 
     * <code>UnitOfWork</code> instance
     */
    PersistenceManager getPersistenceManager();

    /**
     * The invocation of this methods results in clearing the unit of work before close.
     * 
     * @exception IllegalStateException if the unit of work is non-optimistic
     */
    void setForgetOnly();
    
    /**
     * Tells whether the forget-only flag has been set.
     * 
     * @return <code>true</code> if the forget-only flag has been set.
     */
    boolean isForgetOnly();
	    
}
