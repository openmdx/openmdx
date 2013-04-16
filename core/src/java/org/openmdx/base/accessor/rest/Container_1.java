/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Container_1 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2011, OMEX AG, Switzerland
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

import static org.openmdx.base.naming.SpecialResourceIdentifiers.EXTENT_REFERENCES;

import java.io.Flushable;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Extension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * Container
 */
class Container_1
    extends AbstractContainer_1
    implements Flushable
{

    /**
     * Constructor for a persistent or transient container
     *
     * @param owner
     * @param feature
     */
    Container_1(
        DataObject_1 owner,
        String feature
    ){
        this.owner = owner;
        this.transientContainerId = new TransientContainerId(
            owner.jdoGetTransactionalObjectId(),
            feature
        );
        this.ignoreCache = !this.isComposite();
        this.validate = this.openmdxjdoGetDataObjectManager().isProxy() ? null : Boolean.FALSE;
        this.cache = this.ignoreCache || this.isStored() ? null : new ConcurrentHashMap<String,DataObject_1_0>();
        this.queries = new ConcurrentHashMap<String,BatchingList>();        
    }

    /**
     * A lazily initialized flag telling whether this container is an extent.
     */
    private Boolean extent;
    
    /**
     * The container id is lazily populated
     */
    private Path containerId;

    /**
     * The transient container id is based on the parent object's transient id and the container's feature name
     */
    private final TransientContainerId transientContainerId;

    /**
     * The owner might be replaced by an aspect's core object
     */
    private final DataObject_1 owner;

    /**
     * <code>true</code> if cache must be ignored.
     */
    private final boolean ignoreCache;

    /**
     * Tells whether all objects must be validated
     */
    private Boolean validate;

    /**
     * This collection<ul>
     * <li>does not include <em>PERSISTENT-DELETED</em> or <em>PERSISTENT-NEW-DELETED</em> instances
     * <li>does include <em>TRANSIENT</code> instances
     * </ul>
     */
    private ConcurrentMap<String, DataObject_1_0> cache = null;

    /**
     * The container's query type
     */
    private String queryType;

    /**
     * Query to list mapping
     */
    protected final ConcurrentMap<String,BatchingList> queries;

    /**
     * 
     */
    protected static final int BATCH_SIZE_GREEDY = Integer.MAX_VALUE;

    /**
     * The initial slice cache size
     */
    protected static final int INITIAL_SLICE_CACHE_SIZE = 8;

    /**
     * Determine whether the container is composite or not
     * 
     * @return <code>false</code> unless the container is composite
     */
    private boolean isComposite(
    ){
        if(this.owner.objDoesNotExist()) {
            return false;
        } else try {
            Model_1_0 model = this.openmdxjdoGetDataObjectManager().getModel();
            ModelElement_1_0 classDef = model.getElement(this.owner.objGetClass());
            ModelElement_1_0 reference = model.getFeatureDef(classDef, this.transientContainerId.getFeature(), true);
            ModelElement_1_0 referencedEnd = model.getElement(reference.objGetValue("referencedEnd"));
            return AggregationKind.COMPOSITE.equals(referencedEnd.objGetValue("aggregation"));
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * Tells whether all objects must be validated
     * 
     * @return <code>true</code> if all objects must be validated
     */
    private boolean mustValidate(
    ){
        if(this.validate == null) {
            Path xri = this.jdoGetObjectId();
            if(xri != null) try {
                this.validate = Boolean.valueOf(this.openmdxjdoGetDataObjectManager().getModel().containsSharedAssociation(xri));
            } catch (Exception exception) {
                this.validate = Boolean.TRUE;
            }
        }
        return Boolean.TRUE.equals(this.validate);
    } 

    /**
     * Add an object to the cache
     * @param key
     * @param value
     */
    void addToCache(
        String key,
        DataObject_1_0 value
    ){
        if(this.isRetrieved()) {
            this.cache.put(key, value);
        }
    }

    /**
     * Remove an object from the cache
     * 
     * @param key
     */
    void removeFromChache(
        String key
    ){
        if(this.isRetrieved()) {
            this.cache.remove(key);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#getFilter()
     */
    @Override
    protected ObjectFilter getFilter() {
        return null;
    }

    //  @Override
    public boolean jdoIsPersistent(){
        return this.containerId != null || this.owner.jdoIsPersistent();
    }

    //  @Override
    public Path jdoGetObjectId() {
        if(this.containerId == null && this.owner.jdoIsPersistent()){
            this.containerId = this.owner.jdoGetObjectId().getChild(this.transientContainerId.getFeature());
        }
        return this.containerId;
    }

    //  @Override
    public TransientContainerId jdoGetTransactionalObjectId() {
        return this.transientContainerId;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
//  @Override
    public PersistenceManager jdoGetPersistenceManager(){
        return this.owner.jdoGetPersistenceManager();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
    @Override
    public DataObjectManager_1 openmdxjdoGetDataObjectManager() {
        return this.owner.jdoGetPersistenceManager();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#container()
     */
    @Override
    public Container_1 container() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#getConditions()
     */
    @Override
    protected List<Condition> getConditions() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#getExtension()
     */
    @Override
    protected List<Extension> getExtensions() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1#isIgnoreCache()
     */
    @Override
    protected boolean isIgnoreCache() {
        return this.ignoreCache || this.isProxy();
    }

    /**
     * Test whether lookup for this object has already failed in the same unit of work.
     * 
     * @param key
     * @return <code>true</code> if lookup for this object has already failed in the same unit of work
     */
    private boolean notFound(
        Object key
    ){
        try {
            //
            // Test whether lookup for this object has already failed in the same unit of work.
            //
            UnitOfWork_1 unitOfWork = this.openmdxjdoGetDataObjectManager().currentUnitOfWork();
            if(unitOfWork.isActive()) {
                TransactionalState_1 state = unitOfWork.getState(this.owner, true);
                if(state != null) {
                    Set<?> notFound = (Set<?>) state.values(true).get(this.transientContainerId.getFeature());
                    if(notFound != null && notFound.contains(key)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (JDOException ignore) {
            return false;
        }
    }
    
//  @Override
    public DataObject_1_0 get(
        Object key
    ) {
        if(key instanceof String) {
            String qualifier = (String) key;
            if(this.isRetrieved()) {
                //
                // Retrieve the object from the cache
                //
            	return this.cache.get(qualifier);
            } else if(notFound(key)) {
                return null;
            } else try { 
            	DataObject_1_0 member =  this.openmdxjdoGetDataObjectManager().getObjectById(
                    this.jdoGetObjectId().getChild(qualifier), 
                    this.mustValidate()
                );
            	// TODO This tests a workaround for an erroneous JRE 5.0 behaviour!!!  
            	return this.jdoIsPersistent() == member.jdoIsPersistent() ? member : null;
            } catch (JDOObjectNotFoundException exception) {
                return null;
            }
        } else {
            return null; 
        }
    }

//  @Override
    public boolean containsKey(Object key) {
        if(this.isRetrieved()) {
            return this.cache.containsKey(key);
        } else if (key instanceof String){
            if(notFound(key)) {
                return false;
            } else try {
                DataObject_1_0 candidate = this.openmdxjdoGetDataObjectManager().getObjectById(
                    this.jdoGetObjectId().getChild((String)key), 
                    false
                );
                return candidate != null && !candidate.objDoesNotExist() && !candidate.jdoIsDeleted();
            } catch (JDOObjectNotFoundException exception) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * This method ignores the deleted flag
     * 
     * @return whether the object belongs to this container 
     * 
     * @see Container_1#containsValue(Object)
     */
    boolean containsObject(
        Object value
    ){
        if(value instanceof DataObject_1_0) {
            DataObject_1_0 dataObject = (DataObject_1_0) value;
            if(!dataObject.objIsInaccessible() && dataObject.objIsContained()){
                Container_1_0 container = dataObject.getContainer(true);
                if(container == null) {
                    //
                    // Compare the XRIs if both data object and container are persistent
                    //
                    if(
                        dataObject.jdoIsPersistent() && 
                        dataObject.jdoGetPersistenceManager() == this.openmdxjdoGetDataObjectManager()
                    ){
                        Path containerId = this.jdoGetObjectId();
                        if(containerId != null) {
                            //
                            // Compare the parent of the persistent object's XRI with the persistent container's id
                            //
                            Path objectId = dataObject.jdoGetObjectId(); 
                            if(
                                objectId.size() == containerId.size() + 1 &&
                                objectId.startsWith(containerId)
                            ){
                                //
                                // Speed-up future containsObject() invocations
                                //
                                ((DataObject_1)dataObject).setContainer(this);
                                //
                                // The XRIs did match
                                //
                                return true;
                            }
                        }
                    }
                } else  {
                    //
                    // Test the data object's container
                    //
                    return container.container() == Container_1.this.container();
                } 
            }
        }
        return false;
    }
    
//  @Override
    public boolean containsValue(
        Object value
    ) {
        return containsObject(value) && !ReducedJDOHelper.isDeleted(value);
    }

//  @Override
    public DataObject_1_0 put(
        String key, 
        DataObject_1_0 value
    ) {
        try {
            value.objMove(this, key);
        } catch (ServiceException exception) {
            throw new JDOUserException(
                "Cannot add object to container",
                exception
            );
        }
        return null;
    }

    @Override
    public DataObject_1_0 remove(Object key) {
        if(key instanceof String) {
            String qualifier = (String) key;
            DataObject_1_0 value = this.get(qualifier);
            if(value == null) {
                return null;
            } else {
                if(value.jdoIsPersistent()) {
                    this.openmdxjdoGetDataObjectManager().deletePersistent(value);
                    return value;
                } else {
                    return this.cache.remove(qualifier);
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Add the included objects to the cache
     */
    void amendAndDeployCache(
        ConcurrentMap<String,DataObject_1_0> cache
    ){
        for(DataObject_1_0 object : this.getIncluded()) {
            cache.put(
                object.jdoGetObjectId().getBase(),
                object
            );
        }
        this.cache = cache;
    }
    
    private void populateCache(
    ) {
        ConcurrentMap<String,DataObject_1_0> cache = new ConcurrentHashMap<String,DataObject_1_0>();
        for(DataObject_1_0 object : this.getStored()){
            if(!object.jdoIsDeleted()) {
                cache.put(
                    object.jdoGetObjectId().getBase(),
                    object
                );
            }                    
        }
        amendAndDeployCache(cache);
    }
    
    //  @Override
   public void openmdxjdoRetrieve(
       FetchPlan fetchPlan
   ) {
       if(!this.isRetrieved() && this.isStored() && !isExtent()) {
           populateCache();
       }
   }

   @Override
    protected boolean isExtent(){
        if(this.extent == null) {
            this.extent = Boolean.valueOf(
                this.jdoIsPersistent() && 
                this.jdoGetObjectId().isLike(EXTENT_REFERENCES)
            ); 
        }
        return this.extent.booleanValue();
    }
    
    @Override
    public Set<Entry<String, DataObject_1_0>> entrySet() {
        if(isExtent()) { 
            throw BasicException.initHolder(
                new JDOFatalUserException(
                    "Operation not supported for extent",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED
                    )
                )
            );
        } else {
            return super.entrySet();
        }
    }

//  @Override
    public void openmdxjdoEvict(
        boolean allMembers, 
        boolean allSubSets
    ){
        if(allMembers) {
            super.evictMembers();
        }
        if(allSubSets) {
            for(BatchingList stored : this.queries.values()) {
                stored.evict();
            }
        } else {
            super.evictStored();
        }
        if(this.isStored()) {
            this.cache = null;
        } 
    }

    /**
     * Retrieve the query type
     * 
     * @return the query type
     */
    String getQueryType(){
        if(this.queryType == null) try {
        	String ownerClass = DataObject_1.getRecordName(this.owner, true);
        	Model_1_0 model = this.openmdxjdoGetDataObjectManager().getModel();
        	ModelElement_1_0 referencedType = ownerClass == null ?
                model.getTypes(this.jdoGetObjectId())[2] :
                model.getElementType(model.getFeatureDef(model.getElement(ownerClass), this.transientContainerId.getFeature(), false));
	        this.queryType = (String)referencedType.objGetValue("qualifiedName");
        } catch (ServiceException exception) {
            exception.log();
        }
        return this.queryType;
    }

    @Override
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }
        if(object instanceof Container_1) {
            Container_1 that = (Container_1) object;
            return this.jdoIsPersistent() ? (
                that.jdoIsPersistent() && this.jdoGetObjectId().equals(that.jdoGetObjectId())
            ) : (
                !that.jdoIsPersistent() && this.jdoGetTransactionalObjectId().equals(that.jdoGetTransactionalObjectId())
            );
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (
            this.jdoIsPersistent() ? this.jdoGetObjectId() : this.transientContainerId.toPath()
        ).hashCode();
    }

    @Override
    public String toString() {
        return (
            this.jdoIsPersistent() ? this.jdoGetObjectId() : this.transientContainerId.toPath()
        ).toXRI();
    }

    ConcurrentMap<String, DataObject_1_0> getCache(){
        if(this.cache == null && !this.isIgnoreCache()) {
            BatchingList stored = this.queries.get("");
            if(stored != null && stored.isSmallerThanCacheThreshold()){
                this.openmdxjdoRetrieve(this.openmdxjdoGetDataObjectManager().getFetchPlan());
            }
        }
        return this.cache;
    }

    @Override
    public boolean isRetrieved(){
        return this.cache != null;
    }

    boolean isStored(){
        return this.owner.jdoIsPersistent() && !this.owner.jdoIsNew();
    }

    //  @Override
    public void flush(
    ) throws IOException {
        // Nothing to do in this implementation
    }

}
