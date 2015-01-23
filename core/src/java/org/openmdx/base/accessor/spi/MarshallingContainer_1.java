/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Marshalling Filterable Map
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.spi;

import java.util.List;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.collection.MarshallingMap;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;

/**
 * A Marshalling Filterable Map
 */
public class MarshallingContainer_1
    extends MarshallingMap<String,DataObject_1_0>
    implements Container_1_0
{

    /**
     * Constructor 
     * 
     * @param marshaller
     * @param map
     */
    public MarshallingContainer_1(
        PersistenceManager persistenceManager,
        Marshaller marshaller, 
        Container_1_0 container
    ) {
        super(marshaller, container);
        this.persistenceManager = persistenceManager;
    }

    private final PersistenceManager persistenceManager;
    
    @Override
    public Container_1_0 subMap(
        QueryFilterRecord filter
    ) {
        return new MarshallingContainer_1(
            this.persistenceManager,
            super.marshaller,
            this.getDelegate().subMap(filter)
        );
    }

    @Override
    public Container_1_0 container() {
        return new MarshallingContainer_1(
            this.persistenceManager,
            super.marshaller,
            this.getDelegate().container()
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.MarshallingMap#getDelegate()
     */
    @Override
    protected Container_1_0 getDelegate() {
        return (Container_1_0) super.getDelegate();
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3545803160820921399L;

    
    //------------------------------------------------------------------------
    // Implements Container_1_0
    //------------------------------------------------------------------------
    
    @Override
    public List<DataObject_1_0> values(
        FetchPlan fetchPlan, FeatureOrderRecord... criteria
    ) {
        return new MarshallingSequentialList<DataObject_1_0>(
            super.marshaller,
            this.getDelegate().values(fetchPlan, criteria)
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
    @Override
    public PersistenceManager openmdxjdoGetDataObjectManager() {
        return this.getDelegate().openmdxjdoGetDataObjectManager();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#retrieveAll(boolean)
     */
    @Override
    public void openmdxjdoRetrieve(FetchPlan fetchPlan) {
        this.getDelegate().openmdxjdoRetrieve(fetchPlan);
    }

    @Override
    public boolean isRetrieved() {
        return this.getDelegate().isRetrieved();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoEvict()
     */
    @Override
    public void openmdxjdoEvict(boolean allMembers, boolean allSubSets) {
        this.getDelegate().openmdxjdoEvict(allMembers, allSubSets);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#refreshAll()
     */
    @Override
    public void openmdxjdoRefresh() {
        this.getDelegate().openmdxjdoRefresh();
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceCapable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
    @Override
    public PersistenceManager jdoGetPersistenceManager(){
        return this.persistenceManager;
    }

    @Override
    public boolean jdoIsPersistent() {
        return this.getDelegate().jdoIsPersistent();
   }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#getContainerId()
     */
    @Override
    public Object jdoGetObjectId() {
        return this.getDelegate().jdoGetObjectId();
    }

    @Override
    public Object jdoGetTransactionalObjectId() {
        return this.getDelegate().jdoGetTransactionalObjectId();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    @Override
    public void jdoReplaceStateManager(
        StateManager sm
    ) throws SecurityException {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    @Override
    public void jdoProvideField(int fieldNumber) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    @Override
    public void jdoProvideFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    @Override
    public void jdoReplaceField(int fieldNumber) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    @Override
    public void jdoReplaceFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    @Override
    public void jdoReplaceFlags() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
    @Override
    public void jdoCopyFields(Object other, int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    @Override
    public void jdoMakeDirty(String fieldName) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    @Override
    public Object jdoGetVersion() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDirty()
     */
    @Override
    public boolean jdoIsDirty() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsTransactional()
     */
    @Override
    public boolean jdoIsTransactional() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsNew()
     */
    @Override
    public boolean jdoIsNew() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDeleted()
     */
    @Override
    public boolean jdoIsDeleted() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    @Override
    public boolean jdoIsDetached() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    @Override
    public PersistenceCapable jdoNewInstance(StateManager sm) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
    @Override
    public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    @Override
    public Object jdoNewObjectIdInstance() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    @Override
    public Object jdoNewObjectIdInstance(Object o) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsToObjectId(Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }
    
    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    @Override
    public String toString() {
        return this.getDelegate().toString();
    }

}
