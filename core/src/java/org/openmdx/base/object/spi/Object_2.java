/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Object_2.java,v 1.6 2008/03/07 23:46:51 hburger Exp $
 * Description: Object_2 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/07 23:46:51 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.object.spi;

import javax.jdo.JDOFatalException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;

/**
 * Object_2
 *
 * @since openMDX 2.0
 */
public class Object_2
    implements PersistenceCapable
{

    /**
     * Constructor 
     */
    public Object_2() {
        this.objectId = null;
        this.persistenceManager = null;
        this.state = State.TRANSIENT;
    }

    /**
     * Constructor 
     *
     * @param persistenceManager
     * @param objectId
     */
    public Object_2(
        PersistenceManager persistenceManager,
        Object objectId
    ) {
        this.objectId = objectId;
        this.persistenceManager = persistenceManager;
        this.state = State.HOLLOW;
    }

    /**
     * 
     */
    private final static String JDO_HELPER_SUPPORT_ONLY = 
        "PersistenceCapable's methods should be accessed via java.jdo.JDOHelper methods only";

    /**
     * The object's state
     */
    private State state;

    /**
     * TODO Maybe the ersistence manager shpuld be derived from the obectId?
     */
    private PersistenceManager persistenceManager;
    
    /**
     * TODO Maybe the object envelope is itself part of the object id structure?
     */
    private Object objectId;

    
    /** Make an instance transient, removing it from management by this
     * <code>PersistenceManager</code>.
     *
     * <P>The instance loses its JDO identity and it is no longer associated
     * with any <code>PersistenceManager</code>.  The state of fields is preserved unchanged.
     */
    void makeTransient (){
        // TODO
    }
    
    /** Make an instance subject to transactional boundaries.
     *
     * <P>Transient instances normally do not observe transaction boundaries.
     * This method makes transient instances sensitive to transaction completion.
     * If an instance is modified in a transaction, and the transaction rolls back,
     * the state of the instance is restored to the state before the first change
     * in the transaction.
     *
     * <P>For persistent instances read in optimistic transactions, this method
     * allows the application to make the state of the instance part of the
     * transactional state.  At transaction commit, the state of the instance in
     * the cache is compared to the state of the instance in the data store.  If they
     * are not the same, then an exception is thrown.
     */
    void makeTransactional (){
        // TODO
    }

    /** Make an instance non-transactional after commit.
     *
     * <P>Normally, at transaction completion, instances are evicted from the
     * cache.  This method allows an application to identify an instance as
     * not being evicted from the cache at transaction completion.  Instead,
     * the instance remains in the cache with nontransactional state.
     */
    void makeNontransactional (){
        // TODO
    }
    
    /** 
     * Mark an instance as dirty.
     */
    void makeDirty (){
        if(this.jdoIsDirty()) {
            // do nothing
        } else if(jdoGetPersistenceManager().currentTransaction().isActive()) {
            if(jdoIsDeleted()) {
                throw new JDOFatalUserException(
                    "Can't modify a deleted instance" // ILLEGAL_STATE
                );
            } else if (jdoIsPersistent()) {
                this.state = State.PERSISTENT_DIRTY;
            } else if (this.state == State.TRANSIENT_CLEAN) {
                this.state = State.TRANSIENT_DIRTY;
            }
        } else {
            if(this.state == State.HOLLOW) {
                this.state = State.PERSISTENT_NONTRANSACTIONAL;
            }
        }
    }

    /** Delete the persistent instance from the data store.
     * This method must be called in an active transaction.
     * The data store object will be removed at commit.
     * Unlike <code>makePersistent</code>, which makes the closure of the instance persistent,
     * the closure of the instance is not deleted from the data store.
     * This method has no effect if the instance is already deleted in the
     * current transaction.
     */
    void deletePersistent (){
        // TODO
    }
    
    
    //------------------------------------------------------------------------
    // Implements PersistenceCapable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetPersistenceManager()
     */
    public PersistenceManager jdoGetPersistenceManager() {
        return this.persistenceManager;
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    public void jdoReplaceStateManager(
        StateManager sm
    ) throws SecurityException {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    public void jdoProvideField(int fieldNumber) {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    public void jdoProvideFields(int[] fieldNumbers) {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    public void jdoReplaceField(int fieldNumber) {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    public void jdoReplaceFields(int[] fieldNumbers) {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    public void jdoReplaceFlags() {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
    public void jdoCopyFields(Object other, int[] fieldNumbers) {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    public void jdoMakeDirty(String fieldName) {
        makeDirty();
        // TODO maybe the call should be delegated as well
        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetObjectId()
     */
    public Object jdoGetObjectId() {
        return this.objectId; 
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
     */
    public Object jdoGetTransactionalObjectId() {
        return this.objectId; 
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    public Object jdoGetVersion() {
        // TODO Muste be delegated
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDirty()
     */
    public boolean jdoIsDirty() {
        return this.state.interrogation().contains(State.Interrogation.DIRTY);
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsTransactional()
     */
    public boolean jdoIsTransactional() {
        return this.state.interrogation().contains(State.Interrogation.TRANSACTIONAL);
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsPersistent()
     */
    public boolean jdoIsPersistent() {
        return this.state.interrogation().contains(State.Interrogation.PERSISTENT);
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsNew()
     */
    public boolean jdoIsNew() {
        return this.state.interrogation().contains(State.Interrogation.NEW);
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDeleted()
     */
    public boolean jdoIsDeleted() {
        return this.state.interrogation().contains(State.Interrogation.DELETED);
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    public boolean jdoIsDetached() {
        return this.state.interrogation().contains(State.Interrogation.DETACHED);
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm) {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    public Object jdoNewObjectIdInstance() {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    public Object jdoNewObjectIdInstance(Object o) {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(Object oid) {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid) {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
     */
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        throw new JDOFatalException(JDO_HELPER_SUPPORT_ONLY); // NOT_SUPPORTED
    }

}
