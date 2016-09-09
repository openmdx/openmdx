/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Container_1 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2013, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.rest;

import java.util.List;

import javax.jdo.FetchPlan;
import javax.jdo.JDODataStoreException;
import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.QueryExtensionRecord;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * Sub-Map
 */
final class Selection_1 extends AbstractContainer_1 {

    /**   
     * Standard Constructor 
     * 
     * @param superMap
     * @param selector
     */
    Selection_1(
        Container_1_0 superMap,
        DataObjectFilter selector
    ){
    	this.container = (Container_1) superMap.container();
        this.superMap = superMap;
        this.objectFilter = selector;
    }

    /**
     * 
     */
    private final Container_1 container;

    /**
     * 
     */
    private final DataObjectFilter objectFilter;
    
    /**
     * 
     */
    private final Container_1_0 superMap;
    
    @Override
    public void openmdxjdoEvict(
        boolean evictAllMembers, 
        boolean evictAllSubSets // TODO restrict to the SubSet's SubSets
    ){
        if(evictAllMembers) {
            super.evictMembers();
        }
        super.evictStored();
        this.superMap.openmdxjdoEvict(false, evictAllSubSets);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#getFilter()
     */
    @Override
    protected DataObjectFilter getFilter() {
        return this.objectFilter;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#container()
     */
    @Override
    public Container_1 container() {
        return this.container;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#getConditions()
     */
    @Override
    protected List<ConditionRecord> getConditions() {
        return objectFilter.getDelegate(this.openmdxjdoGetDataObjectManager());
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#getExtension()
     */
    @Override
    protected List<QueryExtensionRecord> getExtensions() {
        return objectFilter.getExtensions();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#isIgnoreCache()
     */
    @Override
    protected boolean isIgnoreCache() {
        return 
            this.container.isIgnoreCache() || 
            (this.getExtensions() != null && !this.getExtensions().isEmpty());
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#isRetrieved()
     */
    @Override
    public boolean isRetrieved() {
        return this.superMap.isRetrieved() || getStored().isRetrieved();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#openmdxjdoGetPersistenceManager()
     */
    @Override
    public DataObjectManager_1 openmdxjdoGetDataObjectManager() {
        return this.container.openmdxjdoGetDataObjectManager();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
    @Override
    public PersistenceManager jdoGetPersistenceManager(){
    	return this.container.jdoGetPersistenceManager();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetContainerId()
     */
    @Override
    public Path jdoGetObjectId() {
        throw new UnsupportedOperationException(
            "Query XRIs not yet supported"
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetTransientContainerId()
     */
    @Override
    public TransientContainerId jdoGetTransactionalObjectId() {
        throw new UnsupportedOperationException(
            "Query XRIs not yet supported"
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoIsPersistent()
     */
    @Override
    public boolean jdoIsPersistent() {
        return this.container.jdoIsPersistent();
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#isExtent()
     */
    @Override
    protected boolean isExtent() {
        return container().isExtent();
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#isPlainExtent()
     */
    @Override
    protected boolean isPlainExtent() {
        return isExtent() && this.objectFilter.isPlainExtent();
    }

    /**
     * Determines whether an object belongs to the container or extent
     * 
     * @param candidate
     * 
     * @return <code>true</code> if the object belongs to the container or extent
     */
    @Override
    protected boolean isInContainerOrExtent(
        Object candidate
    ){
        return isExtent() ? 
            ((Path) ReducedJDOHelper.getObjectId(candidate)).isLike(getFilter().getIdentityPattern()) :
            super.isInContainerOrExtent(candidate);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoRetrieve(javax.jdo.FetchPlan)
     */
    @Override
    public void openmdxjdoRetrieve(
        FetchPlan fetchPlan
    ) {
      if(!this.isRetrieved()) try {
          this.getStored().retrieveAll(fetchPlan);
      } catch (ServiceException exception) {
          throw new JDODataStoreException("retrieveAll() failure", exception);
      }
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        DataObject_1_0 candidate = this.superMap.get(key);
        return candidate != null && this.objectFilter.accept(candidate);
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return 
            this.isInContainerOrExtent(value) &&
            !ReducedJDOHelper.isDeleted(value) &&
            this.objectFilter.accept(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public DataObject_1_0 get(Object key) {
        DataObject_1_0 candidate = this.superMap.get(key);
        return candidate != null && this.objectFilter.accept(candidate) ? candidate : null;
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public DataObject_1_0 put(String key, DataObject_1_0 value) {
        if(this.objectFilter.accept(value)) {
            return this.superMap.put(key, value);
        }
        throw new IllegalArgumentException (
            "The object is not a member of this collection"
        );
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " of " + this.container;
    }

    /**
     * Tell the cache that the selection is empty
     */
    void markAsEmpty(
    ){
        if(hasStored()) {
            this.getStored().setTotal(0);
        }
    }

}
    