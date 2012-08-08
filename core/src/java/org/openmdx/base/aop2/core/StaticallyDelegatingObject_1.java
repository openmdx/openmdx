/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StaticallyDelegatingObject_1.java,v 1.2 2008/12/15 03:15:37 hburger Exp $
 * Description: DelegatingObject_1 class
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.aop2.core;

import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.StoreCallback;

import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_1;
import org.openmdx.base.accessor.generic.cci.Object_1_2;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.accessor.generic.spi.Delegating_1_0;
import org.openmdx.base.accessor.generic.spi.Object_1_5;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 * A delegating object
 */
public class StaticallyDelegatingObject_1 
    implements Object_1_1, Object_1_2, Object_1_5, Delegating_1_0, LoadCallback, StoreCallback, DeleteCallback
{

    /**
     * Constructor 
     *
     * @param object
     */
    protected StaticallyDelegatingObject_1(
        Object_1_0 delegate
    ){
        this.delegate = delegate;
    }

    /**
     * Constructor 
     */
    protected StaticallyDelegatingObject_1(
    ){
        // Sub-class implements Serializable
    }
    
    /**
     * The object's identity
     */
    private Object_1_0 delegate;

    /**
     * Retrieve delegate.
     *
     * @return Returns the delegate.
     */
    protected final Object_1_0 getDelegate() {
        return this.delegate;
    }
    
    
    //------------------------------------------------------------------------
    // Implements Delegating_1_0
    //------------------------------------------------------------------------    

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Delegating_1_0#objGetDelegate()
     */
    public Object_1_0 objGetDelegate(
    ) {
        return this.delegate instanceof Delegating_1_0 ? 
            (Object_1_0)((Delegating_1_0)this.delegate).objGetDelegate() :
            null;
    }


    //--------------------------------------------------------------------------
    // Implements Object_1_5
    //--------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    public ObjectFactory_1_0 getFactory(
    ) throws ServiceException {
        if(this.delegate instanceof Object_1_5) {
            return ((Object_1_5)this.delegate).getFactory();
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Factory retrieval not supported",
                new BasicException.Parameter(
                    "delegateClass", 
                    this.delegate == null ? null : this.delegate.getClass().getName()
                )
            );    
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getAspect(java.lang.String)
     */
    public Map<String, Object_1_0> getAspect(
        String aspectClass
    ) throws ServiceException {
        if(this.delegate instanceof Object_1_5) {
            return ((Object_1_5)this.delegate).getAspect(aspectClass);
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Aspect capability not supported",
                new BasicException.Parameter(
                    "delegateClass", 
                    this.delegate == null ? null : this.delegate.getClass().getName()
                ), new BasicException.Parameter(
                    "aspectClass",
                    aspectClass
                )
            );    
        }
    }

    
    //------------------------------------------------------------------------
    // Implements Object_1_4
    //------------------------------------------------------------------------
    
    /**
     * @return
     * @see org.openmdx.base.accessor.generic.cci.Object_1_2#getInaccessibilityReason()
     */
    public ServiceException getInaccessibilityReason() {
        return this.delegate instanceof Object_1_2 ? 
            ((Object_1_2)this.delegate).getInaccessibilityReason() :
            null;
    }


    /**
     * @param feature
     * @param listener
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddEventListener(java.lang.String, java.util.EventListener)
     */
    public void objAddEventListener(String feature, EventListener listener)
        throws ServiceException {
        this.delegate.objAddEventListener(feature, listener);
    }


    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddToUnitOfWork()
     */
    public void objAddToUnitOfWork()
        throws ServiceException {
        this.delegate.objAddToUnitOfWork();
    }


    /**
     * @param there
     * @param criteria
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objCopy(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    public Object_1_0 objCopy(
        FilterableMap<String, Object_1_0> there,
        String criteria)
        throws ServiceException {
        return this.delegate.objCopy(there, criteria);
    }


    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objDefaultFetchGroup()
     */
    public Set<String> objDefaultFetchGroup()
        throws ServiceException {
        return this.delegate.objDefaultFetchGroup();
    }


    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objFlush()
     */
    public boolean objFlush()
        throws ServiceException {
        return this.delegate.objFlush();
    }


    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetClass()
     */
    public String objGetClass()
        throws ServiceException {
        return this.delegate.objGetClass();
    }


    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetContainer(java.lang.String)
     */
    public FilterableMap<String, Object_1_0> objGetContainer(String feature)
        throws ServiceException {
        return this.delegate.objGetContainer(feature);
    }


    /**
     * @param <T>
     * @param feature
     * @param listenerType
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetEventListeners(java.lang.String, java.lang.Class)
     */
    public <T extends EventListener> T[] objGetEventListeners(
        String feature,
        Class<T> listenerType)
        throws ServiceException {
        return this.delegate.objGetEventListeners(feature, listenerType);
    }


    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetLargeObject(java.lang.String)
     */
    public LargeObject_1_0 objGetLargeObject(String feature)
        throws ServiceException {
        return this.delegate.objGetLargeObject(feature);
    }


    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetList(java.lang.String)
     */
    public List<Object> objGetList(String feature)
        throws ServiceException {
        return this.delegate.objGetList(feature);
    }


    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objGetPath()
     */
    public Path objGetPath()
        throws ServiceException {
        return this.delegate.objGetPath();
    }


    /**
     * @return
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetResourceIdentifier()
     */
    public Object objGetResourceIdentifier() {
        return this.delegate.objGetResourceIdentifier();
    }


    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetSet(java.lang.String)
     */
    public Set<Object> objGetSet(String feature)
        throws ServiceException {
        return this.delegate.objGetSet(feature);
    }


    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetSparseArray(java.lang.String)
     */
    public SortedMap<Integer, Object> objGetSparseArray(String feature)
        throws ServiceException {
        return this.delegate.objGetSparseArray(feature);
    }


    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetValue(java.lang.String)
     */
    public Object objGetValue(String feature)
        throws ServiceException {
        return this.delegate.objGetValue(feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#objGetIterable(java.lang.String)
     */
    public Iterable<?> objGetIterable(
        String featureName
    ) throws ServiceException {
        return this.delegate.objGetIterable(featureName);
    }

    /**
     * @param operation
     * @param arguments
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objInvokeOperation(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperation(
        String operation,
        Structure_1_0 arguments)
        throws ServiceException {
        return this.delegate.objInvokeOperation(operation, arguments);
    }


    /**
     * @param operation
     * @param arguments
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objInvokeOperationInUnitOfWork(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
        Structure_1_0 arguments)
        throws ServiceException {
        return this.delegate.objInvokeOperationInUnitOfWork(
            operation,
            arguments);
    }


    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsDeleted()
     */
    public boolean objIsDeleted()
        throws ServiceException {
        return this.delegate.objIsDeleted();
    }


    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsDirty()
     */
    public boolean objIsDirty()
        throws ServiceException {
        return this.delegate.objIsDirty();
    }


    /**
     * @return
     * @see org.openmdx.base.accessor.generic.cci.Object_1_1#objIsHollow()
     */
    public boolean objIsHollow() {
        return 
            this.delegate instanceof Object_1_1 && ((Object_1_1)this.delegate).objIsHollow();
    }


    /**
     * @return
     * @see org.openmdx.base.accessor.generic.cci.Object_1_2#objIsInaccessible()
     */
    public boolean objIsInaccessible() {
        return this.delegate instanceof Object_1_2 && ((Object_1_2)this.delegate).objIsInaccessible();
    }


    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsInUnitOfWork()
     */
    public boolean objIsInUnitOfWork()
        throws ServiceException {
        return this.delegate.objIsInUnitOfWork();
    }


    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsNew()
     */
    public boolean objIsNew()
        throws ServiceException {
        return this.delegate.objIsNew();
    }


    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsPersistent()
     */
    public boolean objIsPersistent()
        throws ServiceException {
        return this.delegate.objIsPersistent();
    }


    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objMakeVolatile()
     */
    public void objMakeVolatile()
        throws ServiceException {
        this.delegate.objMakeVolatile();
    }


    /**
     * @param there
     * @param criteria
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objMove(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    public void objMove(FilterableMap<String, Object_1_0> there, String criteria)
        throws ServiceException {
        this.delegate.objMove(there, criteria);
    }


    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRefresh()
     */
    public void objRefresh()
        throws ServiceException {
        this.delegate.objRefresh();
    }


    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objRemove()
     */
    public void objRemove()
        throws ServiceException {
        this.delegate.objRemove();
    }


    /**
     * @param feature
     * @param listener
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveEventListener(java.lang.String, java.util.EventListener)
     */
    public void objRemoveEventListener(String feature, EventListener listener)
        throws ServiceException {
        this.delegate.objRemoveEventListener(feature, listener);
    }


    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveFromUnitOfWork()
     */
    public void objRemoveFromUnitOfWork()
        throws ServiceException {
        this.delegate.objRemoveFromUnitOfWork();
    }


    /**
     * @param feature
     * @param to
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objSetValue(java.lang.String, java.lang.Object)
     */
    public void objSetValue(String feature, Object to)
        throws ServiceException {
        this.delegate.objSetValue(feature, to);
    }
    
    //--------------------------------------------------------------------------
    // Implements StoreCallback
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreCallback#jdoPreStore()
     */
    public void jdoPreStore() {
        if(this.delegate instanceof StoreCallback) {
            ((StoreCallback)this.delegate).jdoPreStore();
        }
    }


    //--------------------------------------------------------------------------
    // Implements DeleteCallback
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteCallback#jdoPreDelete()
     */
    public void jdoPreDelete() {
        if(this.delegate instanceof DeleteCallback) {
            ((DeleteCallback)this.delegate).jdoPreDelete();
        }
    }


    //--------------------------------------------------------------------------
    // Implements LoadCallback
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.LoadCallback#jdoPostLoad()
     */
    public void jdoPostLoad() {
        if(this.delegate instanceof LoadCallback) {
            ((LoadCallback)this.delegate).jdoPostLoad();
        }
    }

}
