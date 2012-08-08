/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceManager_1.java,v 1.77 2009/06/09 12:45:17 hburger Exp $
 * Description: PersistenceManager_1 
 * Revision:    $Revision: 1.77 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:17 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.RefObject;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.spi.AbstractTransaction_1;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.Queries;
import org.openmdx.base.persistence.spi.AbstractPersistenceManager;
import org.openmdx.base.persistence.spi.InstanceLifecycleNotifier;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;

/**
 * PersistenceManager_1
 */
class PersistenceManager_1
    extends AbstractPersistenceManager
    implements PersistenceManager_1_0, Delegating_1_0<RefRootPackage_1>
{

    /**
     * Constructor 
     * 
     * @param factory 
     * @param notifier 
     * @param marshaller
     * @param unitOfWork
     */
    PersistenceManager_1(
        PersistenceManagerFactory factory, 
        InstanceLifecycleNotifier notifier,
        RefRootPackage_1 refPackage
    ) {
        super(
            factory,
            notifier
        );
        this.refPackage = refPackage;
        this.transaction = new AbstractTransaction_1(){

            @Override
            protected Transaction getDelegate() {
                return PersistenceManager_1.this.refPackage.refDelegate().currentTransaction();
            }

            public PersistenceManager getPersistenceManager() {
                return PersistenceManager_1.this;
            }
         
        };
        
    }

    /**
     * 
     */
    RefRootPackage_1 refPackage;

    /**
     * 
     */
    private final Transaction transaction;
    
    /**
     * Only a subset of the JDO methods is already implemented 
     */
    protected static final String OPENMDX_2_JDO = "This JDO operation is not yet supported";

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#isClosed()
     */
    public boolean isClosed(
    ) {
        return this.refPackage == null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#close()
     */
    public void close() {
        this.refPackage.close();
        this.refPackage = null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#currentTransaction()
     */
    public Transaction currentTransaction() {
        return this.transaction;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evict(java.lang.Object)
     */
    public void evict(Object pc) {
        // The hint is ignored at the moment...
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Class, boolean)
     */
    @SuppressWarnings("unchecked")
    public void evictAll(Class arg0, boolean arg1) {
        // The hint is ignored at the moment...
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll()
     */
    public void evictAll() {
        this.refPackage.refDelegate().evictAll();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects()
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects() {
        return new MarshallingSet(
            this.refPackage,
            this.refPackage.refDelegate().getManagedObjects()
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet)
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(EnumSet<ObjectState> states) {
        return new MarshallingSet(
            this.refPackage,
            this.refPackage.refDelegate().getManagedObjects(states)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(Class... classes) {
        return new MarshallingSet(
            this.refPackage,
            this.refPackage.refDelegate().getManagedObjects(classes)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(EnumSet<ObjectState> states, Class... classes) {
        return new MarshallingSet(
            this.refPackage,
            this.refPackage.refDelegate().getManagedObjects(states,classes)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refresh(java.lang.Object)
     */
    public void refresh(Object pc) {
        Object objectId = JDOHelper.getObjectId(pc);
        if(objectId != null) {
            PersistenceManager_1_0 delegate = this.refPackage.refDelegate(); 
            delegate.refresh(
                delegate.getObjectById(objectId)
            );
        }
    } 

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll()
     */
    public void refreshAll() {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery()
     */
    public Query newQuery() {
        throw new UnsupportedOperationException(
            "The the Class of the candidate instances must be specified when " +
            "creating a new Query instance."
        );            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Object)
     */
    public Query newQuery(Object compiled) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String)
     */
    public Query newQuery(String query) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String, java.lang.Object)
     */
    public Query newQuery(
        String language, 
        Object query
    ) {
        if(Queries.OPENMDXQL.equals(language)) {
            Query_2Facade openmdxQuery = null;
            try {
                openmdxQuery = Query_2Facade.newInstance((MappedRecord)query);
            } 
            catch(Exception e) {}
            if(openmdxQuery != null) {
                String queryString = openmdxQuery.getQuery();
                Path resourceIdentifier = openmdxQuery.getPath();
                Collection<?> candidates = null;
                if(resourceIdentifier != null) {
                    candidates = (Collection<?>)this.refPackage.refContainer(
                        resourceIdentifier, 
                        null
                    );
                }                
                Query cciQuery = null;
                if((queryString != null) && queryString.startsWith("<?xml")) {
                    org.openmdx.base.query.Filter filter = (org.openmdx.base.query.Filter)JavaBeans.fromXML(queryString);
                    cciQuery = (Query)this.refPackage.refCreateFilter(
                        openmdxQuery.getQueryType(),
                        filter
                    );
                    return org.openmdx.base.persistence.spi.Queries.prepareQuery(
                        cciQuery,
                        candidates,
                        null
                    );                    
                }
                else {
                    cciQuery = (Query)this.refPackage.refCreateFilter(
                        openmdxQuery.getQueryType(),
                        null,
                        null
                    );
                    return org.openmdx.base.persistence.spi.Queries.prepareQuery(
                        cciQuery,
                        candidates,
                        queryString
                    );
                }
            } 
            else {
                throw BasicException.initHolder(
                    new JDOFatalUserException(
                        "Unknown predicate for query",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter(
                                "expected", 
                                Map.class.getName()
                            ),
                            new BasicException.Parameter(
                                "actual",
                                query == null ? null : query.getClass().getName()
                            )
                        )
                    )
                );
            }
        } 
        else {
            throw BasicException.initHolder(
                new JDOFatalUserException(
                    "Unsupported query language",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter(
                            "accepted", 
                            org.openmdx.base.persistence.cci.Queries.OPENMDXQL
                        ),
                        new BasicException.Parameter(
                            "actual",
                            language
                        )
                    )
                )
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(
        Class cls
    ) {       
        String mofClassName = RefRootPackage_1.getMofClassName(
            cls.getName().intern(),
            this.refPackage.refModel()
        );        
        return (Query) this.refPackage.refCreateFilter(
            mofClassName, 
            null, // filterProperties 
            null // attributeSpecifiers
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Extent cln) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, String filter) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln, String filter) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Extent cln, String filter) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newNamedQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newNamedQuery(Class cls, String queryName) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
     */
    public <T> Extent<T> getExtent(
        Class<T> persistenceCapableClass,
        boolean subclasses
    ) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    public Object getObjectById(Object oid, boolean validate) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object)
     */
    public Object getObjectById(Object oid) {
        if(oid instanceof Path) {
            Object pc;
            Path resourceIdentifier = (Path) oid;
            if(resourceIdentifier.size() % 2 == 1) {
                pc = this.refPackage.refObject(resourceIdentifier);
                this.refPackage.registerCallbacks((RefObject_1_0)pc);
            } 
            else {
                pc = this.refPackage.refObject(
                    resourceIdentifier.getParent()
                ).refGetValue(
                    resourceIdentifier.getBase()
                );
            }
            return pc;
        } 
        else {
            Object pc = this.refPackage.refObject(
                oid.toString()
            );
            this.refPackage.registerCallbacks((RefObject_1_0)pc);
            return pc;            
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectId(java.lang.Object)
     */
    public Object getObjectId(Object pc) {
        return pc instanceof PersistenceCapable ?
            ((PersistenceCapable)pc).jdoGetObjectId() :
                null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getTransactionalObjectId(java.lang.Object)
     */
    public Object getTransactionalObjectId(Object pc) {
        return pc instanceof PersistenceCapable ?
            ((PersistenceCapable)pc).jdoGetTransactionalObjectId() :
                null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newObjectIdInstance(java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object newObjectIdInstance(Class pcClass, Object key) {
        return new Path(key.toString());
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractPersistenceManager#newInstance(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<T> pcClass) {
        return (T) this.refPackage.refClass(
            RefRootPackage_1.getMofClassName(
                pcClass.getName().intern(),
                this.refPackage.refModel()
            )
        ).refCreateInstance(
            null
        ); 
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistent(java.lang.Object)
     */
    public <T> T makePersistent(T pc) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistent(java.lang.Object)
     */
    public void deletePersistent(Object pc) {
        if(pc instanceof PersistenceCapable && pc instanceof RefObject) {
            PersistenceCapable jdoObject = (PersistenceCapable) pc;
            if(this != jdoObject.jdoGetPersistenceManager()) throw new JDOUserException(
                "The object is managed b a different PersistnceManager",
                jdoObject
            );
            RefObject jmiObject = (RefObject) pc;
            try {
                jmiObject.refDelete();
            } catch (JmiServiceException exception) {
                throw new JDOUserException(
                    "Deletion failure",
                    exception,
                    jdoObject
                );
            }
        } else throw new JDOUserException(
            "The object to be deleted is not an instance of PersistenceCapable and RefObject"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
     */
    public void makeTransient(Object pc, boolean useFetchPlan) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    public void makeTransientAll(Object[] pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactional(java.lang.Object)
     */
    public void makeTransactional(Object pc) {
        Object objectId = JDOHelper.getObjectId(pc);
        if(objectId != null) {
            PersistenceManager_1_0 delegate = this.refPackage.refDelegate(); 
            delegate.makeTransactional(
                delegate.getObjectById(objectId)
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactional(java.lang.Object)
     */
    public void makeNontransactional(Object pc) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
     */
    public void retrieve(Object pc, boolean useFetchPlan) {
        Object objectId = JDOHelper.getObjectId(pc);
        if(objectId != null) {
            PersistenceManager_1_0 delegate = this.refPackage.refDelegate(); 
            delegate.retrieve(
                delegate.getObjectById(objectId),
                useFetchPlan
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractPersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void retrieveAll(
        Collection pcs, 
        boolean useFetchPlan
    ) {
        if(pcs instanceof RefContainer) {
            String refMofId = ((RefContainer)pcs).refMofId();
            if(refMofId == null) {
                super.retrieveAll(pcs, useFetchPlan);
            } else if (this.refPackage.isTerminal()) {
                PersistenceManager_1_0 delegate = this.refPackage.refDelegate();
                Container_1_0 container =  (Container_1_0) delegate.getObjectById(new Path(refMofId));
                container.retrieveAll(useFetchPlan);
            } else {
                PersistenceManager_1_0 delegate = this.refPackage.refDelegate();
                RefContainer container =  (RefContainer) delegate.getObjectById(new Path(refMofId));
                delegate.retrieveAll(container, useFetchPlan);
            }
        } else {
            super.retrieveAll(pcs, useFetchPlan);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectIdClass(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Class<Path> getObjectIdClass(Class cls) {
        return RefObject_1_0.class.isAssignableFrom(cls) ? Path.class : null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopy(java.lang.Object)
     */
    public <T> T detachCopy(T pc) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#attachCopy(java.lang.Object, boolean)
     */
    public Object attachCopy(Object pc, boolean makeTransactional) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#flush()
     */
    public void flush() {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#checkConsistency()
     */
    public void checkConsistency() {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchPlan()
     */
    public FetchPlan getFetchPlan() {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSequence(java.lang.String)
     */
    public Sequence getSequence(String name) {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);            
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.AbstractPersistenceManager#getDataStoreConnection()
     */
    public JDOConnection getDataStoreConnection() {
        return this.refPackage.refDelegate().getDataStoreConnection();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getMultithreaded()
     */
    public boolean getMultithreaded(
    ){
        return this.getPersistenceManagerFactory().getMultithreaded();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag) {
        if(flag && !getMultithreaded()) throw new javax.jdo.JDOUnsupportedOptionException(
            "The " + ConfigurableProperty.Multithreaded.qualifiedName() + 
            " property can be activated at factory level only"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getServerDate()
     */
    public Date getServerDate() {
        throw new UnsupportedOperationException(OPENMDX_2_JDO);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setCopyOnAttach(boolean)
     */
    public void setCopyOnAttach(boolean flag) {
        // ignored by OPENMDX_1_JDO
    }


    //------------------------------------------------------------------------
    // Implements Delegating_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Delegating_1_0#objGetDelegate()
     */
    public RefRootPackage_1 objGetDelegate(
    ) {
        return this.refPackage;
    }
    

    //------------------------------------------------------------------------
    // Implements PersistenceManager_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#getPersistenceManager(javax.resource.cci.InteractionSpec)
     */
    public PersistenceManager_1_0 getPersistenceManager(
        InteractionSpec interactionSpec
    ) {
        return this.refPackage.refPackage(interactionSpec).refPersistenceManager();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#getFeatureReplacingObjectById(java.lang.Object, java.lang.String)
     */
    public Object getFeatureReplacingObjectById(
        Object objectId,
        String featureName
    ) {
        return this.refPackage.refDelegate().getFeatureReplacingObjectById(objectId, featureName);
    }

    
}
