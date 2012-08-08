/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefObjectFactory_1.java,v 1.18 2009/02/24 15:48:55 hburger Exp $
 * Description: RefObjectFactory_1 class
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/24 15:48:55 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.Extent;
import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.resource.cci.InteractionSpec;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.mof.cci.PrimitiveTypes;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.LargeObject_1_0;
import org.openmdx.base.accessor.cci.PersistenceManager_1_0;
import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.accessor.jmi.cci.JavaNames;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.FetchSize;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingFilterableMap;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.CachingMarshaller;
import org.openmdx.base.marshalling.ExceptionListenerMarshaller;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Model_1_6;
import org.openmdx.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.BinaryLargeObject;

//---------------------------------------------------------------------------
/**
 * ObjectFactory facade delegating to a JmiAccessor_1. 
 */
public class RefObjectFactory_1
    extends CachingMarshaller 
    implements PersistenceManager_1_0 {

    /**
     * Constructor 
     *
     * @param refRootPackage
     */
    public RefObjectFactory_1(
        RefRootPackage_1 refRootPackage
    ) {
        super();
        this.refRootPackage = refRootPackage;
    }

    /**
     * @serial
     */
    final RefRootPackage_1 refRootPackage;

    //-------------------------------------------------------------------------
    // CachingMarshaller
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    public Object createMarshalledObject(
        Object source
    ) throws ServiceException {
        return source instanceof RefObject_1_0 ?
            new DelegatingObject(
                (RefObject_1_0)source,
                null
            ) :
                source;
    }

    //-------------------------------------------------------------------------
    public Object unmarshal(
        Object source
    ) {
        if(source instanceof DelegatingObject) {
            ((DelegatingObject)source).getDelegate();
            return ((DelegatingObject)source).refObject;
        }
        else {
            return source;
        }
    }

    //-------------------------------------------------------------------------
    // ObjectFactory_1_0
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    public void close(
    ) {
        //
    }

    public void evictAll(
    ) {
        //
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#isClosed()
     */
    public boolean isClosed(
    ){ 
        return this.refRootPackage.refObjectFactory().isClosed();
    }

    /**
     * Tells whether the persistence manager represented by this connection is multithreaded or not
     * 
     * @return <code> true</code> if the the persistence manager is multithreaded 
     */
    public boolean getMultithreaded(
    ) {
        return this.refRootPackage.refObjectFactory().getMultithreaded();
    }
    
    //-------------------------------------------------------------------------
    /**
     * Get an object from the object factory.
     * <p>
     * If an object with the given id is already in the cache it is returned,
     * otherwise a new object is returned.
     *
     * @param       accessPath
     *              Access path of object to be retrieved.
     *
     * @return      A persistent object
     */
    private DataObject_1_0 getObject(
        Path accessPath
    ) throws ServiceException{
        return new DelegatingObject(accessPath);  
    }

    //-------------------------------------------------------------------------
    public Object getObjectById(
        Object accessPath
    ) {
        try {
            return accessPath == null ?
                null :
                    getObject(
                        accessPath instanceof Path ? (Path)accessPath : new Path(accessPath.toString())
                    );
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get object",
                e,
                this
            );
        }
    }
 
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class... classes
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#checkConsistency()
     */
    public void checkConsistency() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#currentTransaction()
     */
    public Transaction currentTransaction(
    ) {
        return this.refRootPackage.refUnitOfWork();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistent(java.lang.Object)
     */
    public void deletePersistent(
        Object pc
    ) {
        try {
            DataObject_1_0 delegate = ((ObjectView_1_0)pc).objGetDelegate();
            JDOHelper.getPersistenceManager(delegate).deletePersistent(delegate);
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to delete object",
                e,
                this
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.lang.Object[])
     */
    public void deletePersistentAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void deletePersistentAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopy(java.lang.Object)
     */
    public <T> T detachCopy(T pc) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.util.Collection)
     */
    public <T> Collection<T> detachCopyAll(Collection<T> pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.lang.Object[])
     */
    public <T> T[] detachCopyAll(T... pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(boolean, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public void evictAll(boolean subclasses, Class pcClass) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getCopyOnAttach()
     */
    public boolean getCopyOnAttach() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchGroup(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public FetchGroup getFetchGroup(Class arg0, String arg1) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects()
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet)
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(EnumSet<ObjectState> states) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(Class... classes) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(EnumSet<ObjectState> states, Class... classes) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(boolean, java.lang.Object[])
     */
    public Object[] getObjectsById(boolean validate, Object... oids) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getServerDate()
     */
    public Date getServerDate() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(boolean, java.lang.Object[])
     */
    public void makeTransientAll(boolean useFetchPlan, Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(boolean, java.lang.Object[])
     */
    public void retrieveAll(boolean useFetchPlan, Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setCopyOnAttach(boolean)
     */
    public void setCopyOnAttach(boolean flag) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evict(java.lang.Object)
     */
    public void evict(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Object[])
     */
    public void evictAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void evictAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#flush()
     */
    public void flush() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDataStoreConnection()
     */
    public JDOConnection getDataStoreConnection() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDetachAllOnCommit()
     */
    public boolean getDetachAllOnCommit() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    public <T> Extent<T> getExtent(Class<T> persistenceCapableClass) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
     */
    public <T> Extent<T> getExtent(
        Class<T> persistenceCapableClass,
        boolean subclasses) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchPlan()
     */
    public FetchPlan getFetchPlan() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getIgnoreCache()
     */
    public boolean getIgnoreCache() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    public Object getObjectById(Object oid, boolean validate) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class, java.lang.Object)
     */
    public <T> T getObjectById(Class<T> cls, Object key) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectId(java.lang.Object)
     */
    public Object getObjectId(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectIdClass(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Class getObjectIdClass(Class cls) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Collection getObjectsById(Collection oids) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[])
     */
    public Object[] getObjectsById(Object... oids) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public Collection getObjectsById(Collection oids, boolean validate) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[], boolean)
     */
    public Object[] getObjectsById(Object[] oids, boolean validate) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getPersistenceManagerFactory()
     */
    public PersistenceManagerFactory getPersistenceManagerFactory() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSequence(java.lang.String)
     */
    public Sequence getSequence(String name) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getTransactionalObjectId(java.lang.Object)
     */
    public Object getTransactionalObjectId(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject()
     */
    public Object getUserObject() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject(java.lang.Object)
     */
    public Object getUserObject(Object key) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactional(java.lang.Object)
     */
    public void makeNontransactional(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.lang.Object[])
     */
    public void makeNontransactionalAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeNontransactionalAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistent(java.lang.Object)
     */
    public <T> T makePersistent(T pc) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.lang.Object[])
     */
    public <T> T[] makePersistentAll(T... pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.util.Collection)
     */
    public <T> Collection<T> makePersistentAll(Collection<T> pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactional(java.lang.Object)
     */
    public void makeTransactional(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.lang.Object[])
     */
    public void makeTransactionalAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeTransactionalAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object)
     */
    public void makeTransient(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
     */
    public void makeTransient(Object pc, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[])
     */
    public void makeTransientAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    public void makeTransientAll(Object[] pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newInstance(java.lang.Class)
     */
    public <T> T newInstance(Class<T> pcClass) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newNamedQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newNamedQuery(Class cls, String queryName) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newObjectIdInstance(java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object newObjectIdInstance(Class pcClass, Object key) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery()
     */
    public Query newQuery() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Object)
     */
    public Query newQuery(Object compiled) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String)
     */
    public Query newQuery(String query) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Extent cln) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String, java.lang.Object)
     */
    public Query newQuery(String language, Object query) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, String filter) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Extent cln, String filter) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln, String filter) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#putUserObject(java.lang.Object, java.lang.Object)
     */
    public Object putUserObject(Object key, Object val) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refresh(java.lang.Object)
     */
    public void refresh(
        Object pc
    ) {
        if(pc instanceof RefObject_1_0) {
            ((RefObject_1_0)pc).refRefresh();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll()
     */
    public void refreshAll() {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.lang.Object[])
     */
    public void refreshAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void refreshAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(javax.jdo.JDOException)
     */
    public void refreshAll(JDOException jdoe) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeUserObject(java.lang.Object)
     */
    public Object removeUserObject(Object key) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object)
     */
    public void retrieve(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
     */
    public void retrieve(Object pc, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[])
     */
    public void retrieveAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[], boolean)
     */
    public void retrieveAll(Object[] pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDetachAllOnCommit(boolean)
     */
    public void setDetachAllOnCommit(boolean flag) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(boolean flag) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setUserObject(java.lang.Object)
     */
    public void setUserObject(Object o) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");
    }
    
    //-------------------------------------------------------------------------
    // DataAccessService_1_0
    //-------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0#getModel()
     */
    public Model_1_0 getModel() {
        return this.refRootPackage.refModel();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataAccessService_1_0#getObjectFactory(javax.resource.cci.InteractionSpec)
     */
    public PersistenceManager_1_0 getPersistenceManager(
        InteractionSpec interactionSpec
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1");        
    }
    
    //-------------------------------------------------------------------------
    public DataObject_1_0 newInstance(
        String objectClass
    ) throws ServiceException {
        return new DelegatingObject(
            (RefObject_1_0)this.refRootPackage.refClass(objectClass).refCreateInstance(null),
            objectClass
        );
    }

    //-------------------------------------------------------------------------
    public Structure_1_0 createStructure(
        String type,
        List<String> fieldNames,
        List<?> fieldValues
    ) throws ServiceException {
        return this.refRootPackage.refObjectFactory().createStructure(
            type,
            fieldNames,
            fieldValues
        );
    }

    //---------------------------------------------------------------------------
    /**
     * Maps the generic Object_1_0 feature accessors to typed JMI methods.  
     */
    class DelegatingObject implements Serializable, ObjectView_1_0 {

        private static final long serialVersionUID = 3691040976072423476L;

        //-------------------------------------------------------------------------
        /**
         * @param identity identity of JMI object.
         */
        public DelegatingObject(
            Path identity
        ) throws ServiceException {
            this.identity = identity;
            this.refObject = null;
            this.qualifiedClassName = null;
        }

        //-------------------------------------------------------------------------
        public DelegatingObject(
            RefObject_1_0 delegation,
            String qualifiedClassName
        ) throws ServiceException {
            this.identity = delegation.refGetPath();
            this.refObject = delegation;
            this.qualifiedClassName = qualifiedClassName;
        }

        //------------------------------------------------------------------------
        public String toString(
        ) {
            if(this.refObject != null) {
                return this.refObject.toString();
            }
            else {
                return null;
            }
        }

        //------------------------------------------------------------------------
        ObjectView_1_0 getDelegate(
        ) {
            try {
                if(this.refObject == null) try {
                    this.refObject = (RefObject_1_0)RefObjectFactory_1.this.refRootPackage.refObject(this.identity);
                    RefObjectFactory_1.this.cacheObject(this.refObject, this);
                } catch(JmiServiceException e) {
                    throw new ServiceException(e);
                }
                return this.refObject.refDelegate();
            }
            catch(Exception e) {
                throw new JDOUserException(
                    "Unable to get delegate",
                    e,
                    this
                );
            }
        }

        //-------------------------------------------------------------------------
        public Model_1_6 getModel(
        ) {
            return RefObjectFactory_1.this.refRootPackage.refModel();
        }

        //-------------------------------------------------------------------------
        private ModelElement_1_0 getType(
            ModelElement_1_0 elementDef
        ) throws ServiceException {
            return this.getModel().getElementType(
                elementDef
            );
        }

        //-------------------------------------------------------------------------
        private String toBeanGetterName(
            ModelElement_1_0 attributeDef
        ) throws ServiceException {

            ModelElement_1_0 attributeType = this.getModel().getElementType(attributeDef);
            String name = (String)attributeDef.objGetValue("name");
            boolean isBoolean = "org:w3c:boolean".equals(attributeType.objGetValue("qualifiedName"));
            String beanName = Character.toUpperCase(name.charAt(0)) + name.substring(1);

            if(isBoolean) {
                if(name.startsWith("is")) {
                    beanName = name;
                }
                else {
                    beanName = "is" + beanName;
                }
            }
            else {
                beanName = "get" + beanName;
            }
            return beanName;
        }

        //-------------------------------------------------------------------------
        private String toBeanSetterName(
            ModelElement_1_0 attributeDef
        ) throws ServiceException {

            ModelElement_1_0 attributeType = this.getModel().getElementType(attributeDef);
            String name = (String)attributeDef.objGetValue("name");
            boolean isBoolean = "org:w3c:boolean".equals(attributeType.objGetValue("qualifiedName"));
            String beanName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            if(isBoolean) {
                if(name.startsWith("is")) {
                    beanName = "set" + name.substring(2);
                }
                else {
                    beanName = "set" + beanName;
                }
            }
            else {
                beanName = "set" + beanName;
            }  
            return beanName;
        }


        //-------------------------------------------------------------------------
        private Marshaller toRefStructMarshaller(
            String typeName
        ) {
            return new StructMarshaller(
                typeName, 
                (RefPackage_1_0)this.refObject.refClass().refOutermostPackage(), 
                true
            );
        }

        //-------------------------------------------------------------------------
        private Marshaller fromRefStructMarshaller(
            String typeName
        ) {
            return new StructMarshaller(
                typeName, 
                (RefPackage_1_0)this.refObject.refClass().refOutermostPackage(), 
                false
            );
        }

        //------------------------------------------------------------------------
        public Set<String> objDefaultFetchGroup(
        ) throws ServiceException {
            this.getDelegate();
            try {
                return this.refObject.refDefaultFetchGroup();
            }
            catch(JmiServiceException e) {
                throw new ServiceException(
                    e.getCause()
                );
            }
        }

        //------------------------------------------------------------------------
        /**
         * Get a stream's content
         */
        long getValue(
            String feature,
            Object stream,
            long position
        ) throws ServiceException {
            this.getDelegate();
            // TODO Maybe some dispatcher code should be added here...
            return this.refObject.refGetValue(feature, stream, position);
        }

        /**
         * Get method get<feature>() and invoke it. This way getValue() 
         * dispatches the calls to the implemented class methods. If a method 
         * get&lt;feature&gt; can not be found then refGetValue() is called and an
         * info message is logged.
         */
        @SuppressWarnings({
            "unchecked", "deprecation"
        })
        Object getValue(
            String feature
        ) throws ServiceException {

            if(feature.equals(DataObject_1_0.RECORD_NAME_REQUEST)) {
                return null;
            }
            this.getDelegate();

            String beanGetterName = null;
            ModelElement_1_0 featureDef = null;
            Model_1_0 model = this.getModel();

            ModelElement_1_0 classDef = model.getElement(this.refObject.refClass().refMofId());
            if(classDef == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "class not found",
                    new BasicException.Parameter("class", this.refObject.refClass().refMofId())
                );
            }
            featureDef = model.getFeatureDef(
                classDef,
                feature,
                false
            );
            if(featureDef == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "feature not found",
                    new BasicException.Parameter("class", this.refObject.refClass().refMofId()),
                    new BasicException.Parameter("feature", feature)
                );
            }

            Object values = null;
            ModelElement_1_0 featureType = this.getType(featureDef);
            String qualifiedTypeName = (String)featureType.objGetValue("qualifiedName");

            // invoke JMI method
            // get get<feature>() method and invoke it. Try to find
            // feature on current class and all superclasses
            beanGetterName = this.toBeanGetterName(featureDef);
            try { 
                Method getterMethod = this.refObject.getClass().getMethod(
                    beanGetterName,
                    new Class[]{}
                );
                try {
                    values = getterMethod.invoke(
                        this.refObject,
                        EMPTY_OBJECT_ARRAY
                    );
                }
                catch(InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if(t instanceof ServiceException) {
                        throw (ServiceException)t;
                    }
                    else if(t instanceof RefException_1) {
                        throw ((RefException_1)t).refGetServiceException();
                    }
                    else if(t instanceof JmiServiceException) {
                        throw new ServiceException(
                            ((JmiServiceException)t).getCause()
                        );
                    }
                    else if(t instanceof RuntimeServiceException) {
                        throw new ServiceException(
                            (RuntimeServiceException)t
                        );
                    }
                    throw new ServiceException(e); 
                }
                catch(IllegalAccessException e) {
                    throw new ServiceException(e);
                }
            }

            // not implemented by class --> fallback to generic objGetValue()
            catch(NoSuchMethodException e) {
                values = this.refObject.refGetValue(
                    new RefMetaObject_1(featureDef),
                    null,
                    true
                );
            }

            // optional value
            if(values == null) {
                return values;
            }

            // Container. objects in CONTAINER_TYPE are marshalled and implement Object_1_0
            else if(values instanceof org.openmdx.base.collection.Container || values instanceof RefContainer) {
                return values;
            }

            // List
            else if(values instanceof List) {      
                if(PrimitiveTypes.DATETIME.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingList(
                        DateTimeMarshaller.getInstance(),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.DATE.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingList(
                        DateMarshaller.getInstance(),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.ANYURI.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingList(
                        URIMarshaller.getInstance(false),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.DURATION.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingList(
                        DurationMarshaller.getInstance(),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.SHORT.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingList(
                        ShortMarshaller.getInstance(),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.INTEGER.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingList(
                        IntegerMarshaller.getInstance(),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.LONG.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingList(
                        LongMarshaller.getInstance(),
                        (List)values
                    );
                }
                else if(model.isStructureType(featureType)) {
                    return new MarshallingList(
                        this.fromRefStructMarshaller(qualifiedTypeName),
                        (List)values
                    );
                }
                else if(
                        this.getModel().isClassType(featureType) ||
                        PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingList(
                        RefObjectFactory_1.this,
                        (List)values
                    );
                }          
                else {
                    return values;
                }
            }          

            // Set
            else if(values instanceof Set) {
                if(PrimitiveTypes.DATETIME.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSet(
                        DateTimeMarshaller.getInstance(),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.DATE.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSet(
                        DateMarshaller.getInstance(),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.ANYURI.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSet(
                        URIMarshaller.getInstance(false),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.DURATION.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSet(
                        DurationMarshaller.getInstance(),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.SHORT.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSet(
                        ShortMarshaller.getInstance(),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.INTEGER.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSet(
                        IntegerMarshaller.getInstance(),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.LONG.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSet(
                        LongMarshaller.getInstance(),
                        (Set)values
                    );
                }
                else if(model.isStructureType(featureType)) {
                    return new MarshallingSet(
                        this.fromRefStructMarshaller(qualifiedTypeName),
                        (Set)values
                    );
                }
                else if(
                        this.getModel().isClassType(featureType) ||
                        PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingSet(
                        RefObjectFactory_1.this,
                        (Set)values
                    );
                }          
                else {
                    return values;
                }
            }

            // SparseArray
            else if(values instanceof SortedMap) {
                if(PrimitiveTypes.DATETIME.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSortedMap(
                        DateTimeMarshaller.getInstance(),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.DATE.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSortedMap(
                        DateMarshaller.getInstance(),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.ANYURI.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSortedMap(
                        URIMarshaller.getInstance(false),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.DURATION.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSortedMap(
                        DurationMarshaller.getInstance(),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.SHORT.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSortedMap(
                        ShortMarshaller.getInstance(),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.INTEGER.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSortedMap(
                        IntegerMarshaller.getInstance(),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.LONG.equals(featureType.objGetValue("qualifiedName"))) {
                    return new MarshallingSortedMap(
                        LongMarshaller.getInstance(),
                        (SortedMap)values
                    );
                }
                else if(model.isStructureType(featureType)) {
                    return new MarshallingSortedMap(
                        this.fromRefStructMarshaller(qualifiedTypeName),
                        (SortedMap)values
                    );
                }
                else if(
                        this.getModel().isClassType(featureType) ||
                        PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingSortedMap(
                        RefObjectFactory_1.this,
                        (SortedMap)values
                    );
                }          
                else {
                    return values;
                }
            }

            // single-valued          
            else {
                if(PrimitiveTypes.DATETIME.equals(featureType.objGetValue("qualifiedName"))) {
                    return DateTimeMarshaller.getInstance().marshal(values);
                }
                else if(PrimitiveTypes.DATE.equals(featureType.objGetValue("qualifiedName"))) {
                    return DateMarshaller.getInstance().marshal(values);
                }
                else if(PrimitiveTypes.ANYURI.equals(featureType.objGetValue("qualifiedName"))) {
                    return URIMarshaller.getInstance(false).marshal(values);
                }                
                else if(PrimitiveTypes.DURATION.equals(featureType.objGetValue("qualifiedName"))) {
                    return DurationMarshaller.getInstance().marshal(values);
                }
                else if(PrimitiveTypes.SHORT.equals(featureType.objGetValue("qualifiedName"))) {
                    return ShortMarshaller.getInstance().marshal(values);
                }
                else if(PrimitiveTypes.INTEGER.equals(featureType.objGetValue("qualifiedName"))) {
                    return IntegerMarshaller.getInstance().marshal(values);
                }
                else if(PrimitiveTypes.LONG.equals(featureType.objGetValue("qualifiedName"))) {
                    return LongMarshaller.getInstance().marshal(values);
                }
                else if(model.isStructureType(featureType)) {
                    return this.fromRefStructMarshaller(qualifiedTypeName).marshal(values);
                }
                else if(
                        this.getModel().isClassType(featureType) ||
                        PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new DelegatingObject(
                        (RefObject_1_0)values,
                        null
                    );
                }          
                else {
                    return values;
                }
            }
        }

        //------------------------------------------------------------------------
        private Class<?> getNativeTypeOrClass(
            Object feature,
            ModelElement_1_0 featureDef
        ) throws ServiceException {
            String typeName = (String)featureDef.objGetValue("qualifiedName");
            if (PrimitiveTypes.BOOLEAN.equals(typeName)) {
                return Boolean.TYPE;
            } else if(PrimitiveTypes.SHORT.equals(typeName)) {
                return Short.TYPE;
            } else if(PrimitiveTypes.INTEGER.equals(typeName)) {
                return Integer.TYPE;
            } else if(PrimitiveTypes.LONG.equals(typeName)) {
                return Long.TYPE;
            }
            else {
                return feature.getClass();
            }
        }

        //------------------------------------------------------------------------
        void setValue(
            String feature,
            Object to
        ) throws ServiceException {

            this.getDelegate();

            String beanSetterName = null;
            ModelElement_1_0 featureDef = null;
            Model_1_0 model = this.getModel();

            featureDef = model.getFeatureDef(
                model.getElement(this.refObject.refClass().refMofId()),
                feature,
                false
            );

            // set<feature>() method and invoke it.
            beanSetterName = this.toBeanSetterName(featureDef);
            try { 
                Method setterMethod = null;
                if(to != null) {
                    setterMethod = this.refObject.getClass().getMethod(
                        beanSetterName,
                        new Class[]{
                            to instanceof InputStream 
                            ? InputStream.class 
                                : to instanceof Reader 
                                ? Reader.class 
                                    : this.getNativeTypeOrClass(to, featureDef)
                        }
                    );
                }
                else {
                    Method[] setters = this.refObject.getClass().getMethods();
                    for(
                            int i = 0; 
                            i < setters.length;
                            i++
                    ) {
                        if(setters[i].getName().equals(beanSetterName)) {
                            setterMethod = setters[i];
                            break;
                        }
                    }
                    if(setterMethod == null) {
                        throw new NoSuchMethodException(beanSetterName);
                    }
                }
                try {
                    setterMethod.invoke(
                        this.refObject,
                        to
                    );
                }
                catch(InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if(t instanceof ServiceException) {
                        throw (ServiceException)t;
                    }
                    else if(t instanceof RefException_1) {
                        throw ((RefException_1)t).refGetServiceException();
                    }
                    else if(t instanceof JmiServiceException) {
                        throw new ServiceException(
                            ((JmiServiceException)t).getCause()
                        );
                    }
                    else if(t instanceof RuntimeServiceException) {
                        throw new ServiceException(
                            (RuntimeServiceException)t
                        );
                    }
                    throw new ServiceException(e); 
                }
                catch(IllegalAccessException e) {
                    throw new ServiceException(e);
                }
            }

            // not implemented by class --> fallback to generic objSetValue()
            catch(NoSuchMethodException e) {
                this.refObject.refDelegate().objSetValue(
                    feature, 
                    to
                );
            }
        }

        //------------------------------------------------------------------------
        public Object objGetValue(
            String feature
        ) throws ServiceException {
            return this.getValue(
                feature
            );
        }  

        //------------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        public List<Object> objGetList(
            String feature
        ) throws ServiceException {
            return (List<Object>)this.getValue(feature);
        }

        //------------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        public Set<Object> objGetSet(
            String feature
        ) throws ServiceException {
            return (Set<Object>)this.getValue(feature);
        }

        //------------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        public SortedMap<Integer,Object> objGetSparseArray(
            String feature
        ) throws ServiceException {
            return (SortedMap<Integer,Object>)this.getValue(feature);
        }

        //------------------------------------------------------------------------
        /**
         * Get a large object feature
         * <p> 
         * This method returns a new LargeObject.
         *
         * @param       feature
         *              The feature's name.
         *
         * @return      a large object which may be empty but never is null.
         *
         * @exception   ServiceException ILLEGAL_STATE
         *              if the object is deleted
         * @exception   ClassCastException
         *              if the feature's value is not a large object
         * @exception   ServiceException BAD_MEMBER_NAME
         *              if the object has no such feature
         */
        public LargeObject_1_0 objGetLargeObject(
            String feature
        ) throws ServiceException {
            return new LargeObject_1(feature);
        }

        //------------------------------------------------------------------------
        public Container_1_0 objGetContainer(
            String feature
        ) throws ServiceException {
            // get container from refObject
            Object container = this.getValue(feature);
            return container instanceof LegacyContainer ? 
                new StandardContainer(
                    RefObjectFactory_1.this,
                    (LegacyContainer)container
                ) : 
                new DirectAccessContainer(
                    (Container_1_0)container                 
                );        
        }

        //------------------------------------------------------------------------
        public void objMove(
            Container_1_0 there,
            String criteria
        ) throws ServiceException {
            DataObject_1_0 delegate = this.getDelegate();
            if(there instanceof StandardContainer){
                try {
                    ((StandardContainer)there).getDelegate().refAddValue(
                        criteria,
                        this.refObject
                    );
                } catch(JmiServiceException e) {
                    throw new ServiceException(
                        e.getCause()
                    );
                }
            } else if (there instanceof DirectAccessContainer) {
                this.refObject.refDelegate().objMove(
                    ((DirectAccessContainer)there).container,
                    criteria
                );
            } else {
                there.put(criteria, delegate);
            }
            this.identity = this.refObject.refGetPath();
        }

        //------------------------------------------------------------------------
        /**
         * Invokes the operation with signature <ResultClass> operation(<ParamsClass> params);
         * where <ResultClass> and <ParamsClass> are the result and parameter classes
         * of the operation refClass().refMofId() + ":" + operation. If the operation
         * is not defined then refInvokeOperation() is invoked instead.
         */  
        public Structure_1_0 objInvokeOperation(
            String operation,
            Structure_1_0 parameter
        ) throws ServiceException {
            this.getDelegate(); 

            ModelElement_1_0 operationDef = this.getModel().getFeatureDef(
                this.getModel().getElement(this.refObject.refClass().refMofId()),
                operation, 
                false
            );
            if(operationDef == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "operation not defined for class",
                    new BasicException.Parameter("class", this.refObject.refClass().refMofId()),
                    new BasicException.Parameter("operation", operation)
                );
            }

            // get the type names of 'in' parameter and 'result' 
            String qualifiedNameResultType = null;
            String qualifiedNameInParamType = null;
            String nameInParamType = null;
            for(
                    Iterator<?> i = operationDef.objGetList("content").iterator();
                    i.hasNext();
            ) {
                ModelElement_1_0 paramDef = this.getModel().getElement(i.next());
                ModelElement_1_0 paramDefType = this.getType(paramDef);
                if("in".equals(paramDef.objGetValue("name"))) {
                    qualifiedNameInParamType = (String)paramDefType.objGetValue("qualifiedName");
                    nameInParamType = (String)paramDefType.objGetValue("name");
                }
                else if("result".equals(paramDef.objGetValue("name"))) {
                    qualifiedNameResultType = (String)paramDefType.objGetValue("qualifiedName");
                }
            }
            if(qualifiedNameInParamType == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "no parameter with name \"in\" defined for operation",
                    new BasicException.Parameter("operation", operationDef)
                );
            }
            if(qualifiedNameResultType == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "no parameter with name \"result\" defined for operation",
                    new BasicException.Parameter("operation", operationDef)
                );
            }
            // Get operation to invoke
            try {
                RefRootPackage_1 rootPackage = (RefRootPackage_1)this.refObject.refOutermostPackage();
                Method method = null;
                boolean hasNoParams = false;
                try { 
                    // Try to find signature matching the binding suffix
                    String bindingPackageSuffix = rootPackage.refBindingPackageSuffix();
                    Class<?> inParamClass = Classes.getApplicationClass(
                        this.getModel().toJavaPackageName(
                            qualifiedNameInParamType, 
                            bindingPackageSuffix
                        ) +  
                        "." + ("cci".equals(bindingPackageSuffix) ? nameInParamType : JavaNames.toClassName(nameInParamType))
                    );
                    try {
                        method = this.refObject.getClass().getMethod(
                            JavaNames.toMethodName(operation),
                            new Class[]{
                                inParamClass
                            }
                        );
                    }
                    catch(NoSuchMethodException e) {
                        if(!"org:openmdx:base:Void".equals(qualifiedNameInParamType)) {
                            throw e;                    
                        }
                        method = this.refObject.getClass().getMethod(
                            JavaNames.toMethodName(operation),
                            new Class[]{}
                        );   
                        hasNoParams = true;
                    }
                }
                // Fallback to cci2
                catch(NoSuchMethodException e) {
                    Class<?> inParamClass = Classes.getApplicationClass(
                        this.getModel().toJavaPackageName(
                            qualifiedNameInParamType, 
                            "cci2"
                        ) +  
                        "." + 
                        JavaNames.toClassName(nameInParamType)
                    );
                    try {
                        method = this.refObject.getClass().getMethod(
                            JavaNames.toMethodName(operation),
                            new Class[]{
                                inParamClass
                            }
                        );
                    }
                    catch(NoSuchMethodException e0) {
                        if(!"org:openmdx:base:Void".equals(qualifiedNameInParamType)) {
                            throw e;                    
                        }
                        method = this.refObject.getClass().getMethod(
                            JavaNames.toMethodName(operation),
                            new Class[]{}
                        );      
                        hasNoParams = true;
                    }
                }
                // invoke operation
                try {
                    return (Structure_1_0)this.toRefStructMarshaller(
                        qualifiedNameResultType
                    ).unmarshal(
                        hasNoParams ? 
                            method.invoke(this.refObject, EMPTY_OBJECT_ARRAY) : 
                            method.invoke(
                                this.refObject, 
                                this.toRefStructMarshaller(qualifiedNameInParamType).marshal(parameter)
                            )
                    );
                } 
                catch(InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if(t instanceof Exception) {
                        throw new ServiceException((Exception)t);
                    }
                    else {
                        throw new ServiceException(
                            BasicException.toExceptionStack(t)
                        );
                    }
                }
                catch(IllegalAccessException e) {
                    throw new ServiceException(e);
                }
            }
            catch(ClassNotFoundException e) {
                throw new ServiceException(e);
            }
            catch(NoSuchMethodException e) {
                SysLog.info("method " + operation + " is not defined for class " + this.getClass().getName() + ". Invoking objInvokeOperation()");
                return this.refObject.refDelegate().objInvokeOperation(
                    operation,
                    parameter
                );
            }
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddToUnitOfWork()
         */
        public void objMakeTransactional(
        ) throws ServiceException {
            this.getDelegate().objMakeTransactional();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objClone(org.openmdx.compatibility.base.collection.Container, java.lang.String)
         */
        public DataObject_1_0 openmdxjdoClone(
        ) {
            return this.getDelegate().openmdxjdoClone();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetClass()
         */
        public String objGetClass(
        ) throws ServiceException {
            // The class name is unmodifiable
            return this.qualifiedClassName == null ?
                this.getDelegate().objGetClass() :
                    this.qualifiedClassName;
        }

        //-------------------------------------------------------------------------
        /** (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.DataObject_1_0#jdoGetObjectId()
         */
        public Path jdoGetObjectId(
        ){
            return this.identity;
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objInvokeOperationInUnitOfWork(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
         */
        public Structure_1_0 objInvokeOperationInUnitOfWork(
            String operation,
            Structure_1_0 arguments
        ) throws ServiceException {
            return this.getDelegate().objInvokeOperationInUnitOfWork(
                operation, 
                arguments
            );
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsDeleted()
         */
        public boolean jdoIsDeleted(
        ) {
            return this.getDelegate().jdoIsDeleted();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsDirty()
         */
        public boolean jdoIsDirty(
        ) {
            return this.getDelegate().jdoIsDirty();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsNew()
         */
        public boolean jdoIsNew(
        ) {
            return this.refObject == null ?
                this.qualifiedClassName != null & this.identity != null :
                    this.getDelegate().jdoIsNew();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsPersistent()
         */
        public boolean jdoIsPersistent(
        ) {
            return this.identity != null;
        }

        //-------------------------------------------------------------------------
        /**
         * Tests whether this object belongs to the current unit of work.
         *
         * @return  true if this instance belongs to the current unit of work.
         */
        public boolean jdoIsTransactional(
        ) {
            return this.getDelegate().jdoIsTransactional();
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
         */
        public void jdoCopyFields(Object other, int[] fieldNumbers) {
            this.getDelegate().jdoCopyFields(other, fieldNumbers);            
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
         */
        public void jdoCopyKeyFieldsFromObjectId(
            ObjectIdFieldConsumer fm,
            Object oid
        ) {
            this.getDelegate().jdoCopyKeyFieldsFromObjectId(fm, oid);
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
         */
        public void jdoCopyKeyFieldsToObjectId(Object oid) {
            this.getDelegate().jdoCopyKeyFieldsToObjectId(oid);            
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
         */
        public void jdoCopyKeyFieldsToObjectId(
            ObjectIdFieldSupplier fm,
            Object oid
        ) {
            this.getDelegate().jdoCopyKeyFieldsToObjectId(fm, oid);            
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
         */
        public Object jdoGetTransactionalObjectId() {
            return this.getDelegate().jdoGetTransactionalObjectId();
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
         */
        public Object jdoGetVersion() {
            return this.getDelegate().jdoGetVersion();
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
         */
        public boolean jdoIsDetached() {
            return this.getDelegate().jdoIsDetached();
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
         */
        public void jdoMakeDirty(String fieldName) {
            this.getDelegate().jdoMakeDirty(fieldName);
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
         */
        public PersistenceCapable jdoNewInstance(StateManager sm) {
            return this.getDelegate().jdoNewInstance(sm);
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
         */
        public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
            return this.getDelegate().jdoNewInstance(sm, oid);
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
         */
        public Object jdoNewObjectIdInstance() {
            return this.getDelegate().jdoNewObjectIdInstance();
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
         */
        public Object jdoNewObjectIdInstance(Object o) {
            return this.getDelegate().jdoNewObjectIdInstance(o);
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
         */
        public void jdoProvideField(int fieldNumber) {
            this.getDelegate().jdoProvideField(fieldNumber);
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
         */
        public void jdoProvideFields(int[] fieldNumbers) {
            this.getDelegate().jdoProvideFields(fieldNumbers);
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
         */
        public void jdoReplaceField(int fieldNumber) {
            this.getDelegate().jdoReplaceField(fieldNumber);
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
         */
        public void jdoReplaceFields(int[] fieldNumbers) {
            this.getDelegate().jdoReplaceFields(fieldNumbers);            
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
         */
        public void jdoReplaceFlags() {
            this.getDelegate().jdoReplaceFlags();
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
         */
        public void jdoReplaceStateManager(StateManager sm)
            throws SecurityException {
            this.getDelegate().jdoReplaceStateManager(sm);
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddEventListener(java.lang.String, java.util.EventListener)
         */
        public void objAddEventListener(
            String feature, 
            EventListener listener
        ) throws ServiceException {
            this.getDelegate().objAddEventListener(feature,listener);  // TODO add marshalling
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
         */
        public PersistenceManager jdoGetPersistenceManager(
        ) {
            return RefObjectFactory_1.this;
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.spi.Object_1_7#objIsContained()
         */
        public boolean objIsContained(
        ) {
            throw new UnsupportedOperationException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_IMPLEMENTED,
                    "objIsContained() not implemented by RefObjectFactory_1.DelegatingObject"
                )
            );
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_2#getInaccessibilityReason()
         */
        public ServiceException getInaccessibilityReason(
        ) {
            return null;
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_2#objIsInaccessible()
         */
        public boolean objIsInaccessible(
        ) {
            throw new UnsupportedOperationException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_IMPLEMENTED,
                    "objIsInaccessible() not implemented by RefObjectFactory_1.DelegatingObject"
                )
            );
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_3#objMakeClean()
         */
        public void objMakeClean(
        ) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED,
                "objMakeClean() not implemented by RefObjectFactory_1.DelegatingObject"
            );            
        }
        
        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveEventListener(java.lang.String, java.util.EventListener)
         */
        public void objRemoveEventListener(String feature, EventListener listener) throws ServiceException {
            this.getDelegate().objRemoveEventListener(feature,listener);  // TODO add marshalling
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetEventListener(java.lang.String, java.lang.Class)
         */
        public <T extends EventListener> T[] objGetEventListeners(
            String feature, 
            Class<T> listenerType
        ) throws ServiceException {
            return this.getDelegate().objGetEventListeners(feature,listenerType); // TODO add marshalling
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveFromUnitOfWork()
         */
        public void objMakeNontransactional(
        ) throws ServiceException {
            this.getDelegate().objMakeNontransactional();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objSetValue(java.lang.String, java.lang.Object)
         */
        public void objSetValue(
            String feature, 
            Object to
        ) throws ServiceException {
            this.setValue(
                feature,
                to
            );
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getAspect(java.lang.String)
         */
        public Map<String, DataObject_1_0> getAspect(
            String aspectClass
        ) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED,
                "getAspect() not implemented by RefObjectFactory_1.DelegatingObject"
            );
        }
        
        //-------------------------------------------------------------------------
        class LargeObject_1 implements LargeObject_1_0 {

            private final String feature;

            private long length = -1L;

            /**
             * Constructor
             * 
             * @param feature
             */
            LargeObject_1 (
                String feature
            ){
                this.feature = feature;
            }
            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#length()
             */
            public long length() throws ServiceException {
                return this.length;
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getBytes(long, int)
             */
            public byte[] getBytes(long position, int capacity) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "getBytes(long,int) not supported yet"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getBinaryStream()
             */
            public InputStream getBinaryStream() throws ServiceException {
                try {
                    Object value = getValue(this.feature);
                    return value instanceof InputStream
                    ? (InputStream)value
                        : ((BinaryLargeObject)value).getContent();
                }
                catch(Exception e) {
                    throw new ServiceException(e);
                }
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getBinaryStream(java.io.OutputStream, long)
             */
            public void getBinaryStream(OutputStream stream, long position) throws ServiceException {
                this.length = getValue(this.feature, stream, position);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getCharacters(long, int)
             */
            public char[] getCharacters(long position, int capacity) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "getCharacters(long,int) not supported yet"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getCharacterStream()
             */
            public Reader getCharacterStream() throws ServiceException {
                return (Reader)getValue(this.feature);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getCharacterStream(java.io.Writer, long)
             */
            public void getCharacterStream(Writer writer, long position) throws ServiceException {
                this.length = getValue(this.feature, writer, position);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#truncate(long)
             */
            public void truncate(long length) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBytes(long, byte[])
             */
            public void setBytes(long position, byte[] content) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBinaryStream(java.io.InputStream, long)
             */
            public void setBinaryStream(InputStream stream, long size) throws ServiceException {
                setValue(this.feature, stream);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBinaryStream(long)
             */
            public OutputStream setBinaryStream(long position) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacters(long, char[])
             */
            public void setCharacters(long position, char[] content) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacterStream(java.io.Reader, long)
             */
            public void setCharacterStream(Reader stream, long size) throws ServiceException {
                setValue(this.feature, stream);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacterStream(long)
             */
            public Writer setCharacterStream(long position) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "setCharacterStream(long) not supported yet"
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.view.ObjectView_1_0#getInteractionSpec()
         */
        public InteractionSpec getInteractionSpec(
        ) throws ServiceException {
            return this.getDelegate().getInteractionSpec();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.view.ObjectView_1_0#getMarshaller()
         */
        public Marshaller getMarshaller(
        ) {
            throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1.DelegatingObject");
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.view.ObjectView_1_0#objDelete()
         */
        public void objDelete(
        ) {
            this.getDelegate();
            this.refObject.refDelete();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.view.ObjectView_1_0#objGetDelegate()
         */
        public DataObject_1_0 objGetDelegate(
        ) {
            return this.getDelegate();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.view.ObjectView_1_0#objRefresh()
         */
        public void objRefresh(
        ) throws ServiceException {
            this.getDelegate();
            this.refObject.refRefresh();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.view.ObjectView_1_0#objSetDelegate(org.openmdx.base.accessor.cci.Object_1_0)
         */
        public void objSetDelegate(
            DataObject_1_0 delegate
        ) {
            throw new UnsupportedOperationException("Operation not supported by RefObjectFactory_1.DelegatingObject");
        }


        //-------------------------------------------------------------------------
        // Variables
        //-------------------------------------------------------------------------

        /**
         * @serial
         */
        private Path identity;

        /**
         * @serial
         */
        private String qualifiedClassName;
        transient RefObject_1_0 refObject = null; // lazy init

    }

    //---------------------------------------------------------------------------
    /**
     * Direct Access Container
     */
    @SuppressWarnings("unchecked")
    private class DirectAccessContainer 
        extends MarshallingFilterableMap 
        implements Container_1_0, Serializable 
    {

        /**
         * <code>serialVersionUID</code> to implement<code>Serializable</code>.
         */
        private static final long serialVersionUID = 3905801989500973104L;
        Container_1_0 container;

        DirectAccessContainer(
            Container_1_0 container
        ){
            super(
                RefObjectFactory_1.this, 
                new MarshallingFilterableMap(
                    RefObjectFactory_1.this.refRootPackage,
                    container
                )
            );
            this.container = container;
        }

        /**
         * Constructor 
         *
         * @param parent
         * @param feature
         * @throws ServiceException
         */
        public DirectAccessContainer(
            Path parent,
            String feature
        ) throws ServiceException {
            this(
                ((DataObject_1_0)RefObjectFactory_1.this.refRootPackage.refObjectFactory().getObjectById(
                    parent
                )).objGetContainer(feature)
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Container_1_0#superSet()
         */
        public Container_1_0 superSet() {
            throw new UnsupportedOperationException("Operation not supported by DirectAccessContainer");
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Container_1_0#getObjectId()
         */
        public Object getContainerId() {
            throw new UnsupportedOperationException("Operation not supported by DirectAccessContainer");
        }

    }

    //---------------------------------------------------------------------------
    /**
     * Container containing RefDelegatingObject_1's  
     */
    static class StandardContainer 
    extends AbstractMap<String,DataObject_1_0> 
    implements Serializable, Container_1_0, FetchSize
    {

        /**
         * <code>serialVersionUID</code> to implement<code>Serializable</code>.
         */
        private static final long serialVersionUID = 3257001038556051255L;

        @SuppressWarnings("deprecation")
        protected org.openmdx.base.collection.Container<?> refSelection;

        protected LegacyContainer refContainer;

        protected Marshaller marshaller;

        transient private Set<Map.Entry<String,DataObject_1_0>> entries = null;

        @SuppressWarnings("deprecation")
        private StandardContainer(
            Marshaller marshaller,
            LegacyContainer refContainer,
            org.openmdx.base.collection.Container<?> refSelection
        ) {
            this.marshaller = marshaller;
            this.refContainer = refContainer;
            this.refSelection = refSelection;
        }

        public StandardContainer(
            Marshaller marshaller,
            LegacyContainer refContainer
        ) {
            this(
                new ExceptionListenerMarshaller(marshaller), 
                refContainer, 
                refContainer
            );
        }

        LegacyContainer getDelegate(
        ){
            return this.refContainer;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        public Set<Map.Entry<String,DataObject_1_0>> entrySet(
        ) {
            return this.entries == null ? this.entries = new MarshallingSet<Map.Entry<String,DataObject_1_0>>(
                    new ContainerMarshaller(this.marshaller),
                    this.refSelection
            ) : this.entries;
        }

        /* (non-Javadoc)
         * @see org.openmdx.provider.object.cci.Container#subSet(java.lang.Object)
         */
        @SuppressWarnings("deprecation")
        public FilterableMap<String,DataObject_1_0> subMap(
            Object filter
        ) {
            try {
                return new StandardContainer(
                    this.marshaller,
                    this.refContainer,
                    this.refSelection.subSet(this.marshaller.unmarshal(filter))
                );
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.provider.object.cci.Container#toList(java.lang.Object)
         */
        @SuppressWarnings("deprecation")
        public List<DataObject_1_0> values(
            Object criteria
        ) {
            try {
                return new MarshallingSequentialList<DataObject_1_0>(
                        this.marshaller,
                        this.refSelection.toList(this.marshaller.unmarshal(criteria))
                );
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }


        /* (non-Javadoc)
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        public DataObject_1_0 put(
            String key, 
            DataObject_1_0 value
        ) {
            try {
                RefObject_1_0 refObject = (RefObject_1_0) this.marshaller.unmarshal(value);
                this.refContainer.refAddValue(
                    key, 
                    refObject
                );
                return null;
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

        /* (non-Javadoc)
         * @see java.util.Map#values()
         */
        public Collection<DataObject_1_0> values(
        ) {
            try {
                return new MarshallingSet<DataObject_1_0>(
                    this.marshaller,
                    this.refSelection, null
                );
            }
            catch(Exception e) {
                throw new RuntimeServiceException(e);
            }            
        }

        /* (non-Javadoc)
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public boolean containsValue(
            Object value
        ) {
            try {
                return this.refSelection.contains(this.marshaller.unmarshal(value));
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

        /* (non-Javadoc)
         * @see java.util.Map#get(java.lang.Object)
         */
        @SuppressWarnings("deprecation")
        public DataObject_1_0 get(
            Object key
        ) {
            try {
                return (DataObject_1_0) this.marshaller.marshal(this.refSelection.get(key));
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

        /* (non-Javadoc)
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty(
        ) {
            return this.refSelection.isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.Map#remove(java.lang.Object)
         */
        public DataObject_1_0 remove(
            String key
        ) {
            try {
                DataObject_1_0 oldValue = get(key);
                this.refSelection.remove(
                    this.marshaller.unmarshal(oldValue)
                );
                return oldValue;
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

        /* (non-Javadoc)
         * @see java.util.Map#size()
         */
        public int size() {
            return this.refSelection.size();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.collection.FetchSize#batchSize()
         */
        public int getFetchSize(
        ) {
            if(
                    this.refContainer instanceof FetchSize
            ) this.fetchSize = ((FetchSize)this.refContainer).getFetchSize();
            return this.fetchSize;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.collection.FetchSize#setFetchSize(int)
         */
        public void setFetchSize(
            int fetchSize
        ){
            this.fetchSize = fetchSize;
            if(
                    this.refContainer instanceof FetchSize
            ) ((FetchSize)this.refContainer).setFetchSize(fetchSize);
        }

        /**
         * The proposed fetch size
         */
        private int fetchSize = DEFAULT_FETCH_SIZE;

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Container_1_0#superSet()
         */
        public Container_1_0 superSet() {
            throw new UnsupportedOperationException("Operation not supported by StandardContainer");
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Container_1_0#getObjectId()
         */
        public Object getContainerId() {
            throw new UnsupportedOperationException("Operation not supported by StandardContainer");
        }

    }

    static class ContainerMarshaller
    implements Serializable, Marshaller
    {

        /**
         * <code>serialVersionUID</code> to implement<code>Serializable</code>.
         */
        private static final long serialVersionUID = 3257567287195349299L;

        /**
         * @serial
         */
        Marshaller marshaller;

        ContainerMarshaller(
            Marshaller marshaller
        ){
            this.marshaller = marshaller;
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.marshalling.Marshaller#marshal(java.lang.Object)
         */
        public Object marshal(Object source) throws ServiceException {
            return source instanceof RefObject_1_0 ? 
                new ContainerEntry<Object>(this.marshaller, (RefObject_1_0) source) :
                    source;
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.marshalling.Marshaller#unmarshal(java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        public Object unmarshal(Object source) throws ServiceException {
            return source instanceof Map.Entry ? 
                ((Map.Entry) source).getValue() :
                    source;
        }  

    }

    static class ContainerEntry<E> 
    implements Map.Entry<String,E>
    {

        RefObject_1_0 value;

        Marshaller marshaller;

        ContainerEntry(
            Marshaller marshaller,
            RefObject_1_0 value
        ){
            this.value = value;
            this.marshaller = marshaller;
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#getKey()
         */
        public String getKey() {
            Path path = this.value.refGetPath();
            return path == null ? null : path.getBase();
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#getValue()
         */
        @SuppressWarnings("unchecked")
        public E getValue(
        ) {
            try {
                return (E) this.marshaller.marshal(this.value);
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#setValue(java.lang.Object)
         */
        public E setValue(E value) {
            throw new UnsupportedOperationException();
        }

    }

    //-------------------------------------------------------------------------
    // Interface LegacyContainer
    //-------------------------------------------------------------------------

    /**
     * This interface keeps the legacy code running!
     */
    @SuppressWarnings("deprecation")
    public interface LegacyContainer
    extends org.openmdx.base.collection.Container<RefObject_1_0> 
    {

        /**
         * Adds object to the container with qualifier. Adding an object to a container
         * with add(value) is equivalent to refAddValue(null, value).
         * 
         * @throws JmiServiceException in case the object can not be added to the container.
         */
        public void refAddValue(
            String qualifier,
            RefObject_1_0 value
        );

    }

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3979265850337276212L;
    protected static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

}

//--- End of File -----------------------------------------------------------
