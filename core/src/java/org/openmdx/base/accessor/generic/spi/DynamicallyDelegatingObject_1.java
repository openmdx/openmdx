/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DynamicallyDelegatingObject_1.java,v 1.5 2008/11/14 09:50:56 hburger Exp $
 * Description: DelegatingObject_1 class
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/14 09:50:56 $
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
package org.openmdx.base.accessor.generic.spi;

import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_1;
import org.openmdx.base.accessor.generic.cci.Object_1_2;
import org.openmdx.base.accessor.generic.cci.Object_1_3;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;

/**
 * A delegating object
 */
abstract class DynamicallyDelegatingObject_1
    extends AbstractObject_1
{

    /**
     * Constructor 
     *
     * @param object
     */
    protected DynamicallyDelegatingObject_1(
        Object_1_0 object
    ){
        this.delegate = object;
    }

    /**
     * Constructor
     */
    protected DynamicallyDelegatingObject_1(
    ){
        this.delegate = null;
    }
    
    /**
     * The object's identity
     */
    private Object_1_0 delegate;

    /**
     * Retrieve the object's delegate
     * 
     * @return the object's delegate
     */
    protected Object_1_0 getDelegate(
    ) throws ServiceException {
        ServiceException inaccessibilityReason = getInaccessibilityReason();
        if(inaccessibilityReason == null) {
            return this.delegate;
        } else {
            throw new ServiceException(inaccessibilityReason);
        }
    }
        
    /**
     * Replace the object's delegate
     * 
     * @param delegate the object's delegate
     */
    protected void setDelegate(
        Object_1_0 delegate
    ){
        if(this.delegate != null) {
            super.setInaccessibilityReason(null);
        }
        this.delegate = delegate;
    }
        
    
    //------------------------------------------------------------------------
    // Implements Object_1_3
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_3#objMakeClean()
     */
    public void objMakeClean() throws ServiceException {
        if(this.delegate instanceof Object_1_3) {
            ((Object_1_3)this.delegate).objMakeClean();
        } else {
            throw new UnsupportedOperationException(
                "objMakeClean() not supported by the delegate"
            );
        }
    }

    
    //------------------------------------------------------------------------
    // Implements Object_1_2
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.AbstractObject_1#setInaccessibilityReason(org.openmdx.base.exception.ServiceException)
     */
    @Override
    protected void setInaccessibilityReason(
        ServiceException inaccessibilityReason
    ) {
        if(inaccessibilityReason != null) {
            this.delegate = null;
        }
        super.setInaccessibilityReason(inaccessibilityReason);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.AbstractObject_1#objIsInaccessible()
     */
    @Override
    public boolean objIsInaccessible() {
        return 
            super.objIsInaccessible() ||
            this.delegate == null || 
            (this.delegate instanceof Object_1_2 && ((Object_1_2)this.delegate).objIsInaccessible());
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.AbstractObject_1#getInaccessibilityReason()
     */
    @Override
    public ServiceException getInaccessibilityReason(
    ) {
        return 
            super.objIsInaccessible() ? super.getInaccessibilityReason() :
            this.delegate instanceof Object_1_2 ? ((Object_1_2)this.delegate).getInaccessibilityReason() : 
            null;
    }

    
    //------------------------------------------------------------------------
    // Implements Object_1_1
    //------------------------------------------------------------------------
    

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_1#objIsHollow()
     */
    public boolean objIsHollow(
    ) {
        return 
            this.delegate == null ||
            (this.delegate instanceof Object_1_1 && ((Object_1_1)this.delegate).objIsHollow());
    }

    
    //--------------------------------------------------------------------------
    // Implements Object_1_0
    //--------------------------------------------------------------------------

    /**
     * Returns the object's model class.
     *
     * @return  the object's model class
     *
     * @exception   ServiceException  
     *              if the information is unavailable
     */
    public String objGetClass(
    ) throws ServiceException {
        return getDelegate().objGetClass();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetResourceIdentifier()
     */
    public Object objGetResourceIdentifier() {
        return this.delegate == null ? null : this.delegate.objGetResourceIdentifier();
    }

    /**
     * Returns a new set containing the names of the features in the default
     * fetch group.
     * <p>
     * The returned set is a copy of the original set, i.e. interceptors are
     * free to modify it before passing it on.
     *
     * @return  the names of the features in the default fetch group
     */
    public Set<String> objDefaultFetchGroup(
    ) throws ServiceException {
        return getDelegate().objDefaultFetchGroup();
    }
    
    /**
     * Refresh the state of the instance from its provider.
     *
     * @exception   ServiceException 
     *              if the object can't be synchronized
     */
    public void objRefresh(
    ) throws ServiceException {
        getDelegate().objRefresh();
    }

    /**
     * Mark an object as volatile, i.e POST_RELOAD InstanceCallbackEvents
     * may be fired. 
     *
     * @exception   ServiceException 
     *              if the object can't be made volatile.
     */
    public void objMakeVolatile(
    ) throws ServiceException {
        getDelegate().objMakeVolatile();
    }

    /**
     * Flush the state of the instance to its provider.
     * 
     * @return      true if all attributes could be flushed,
     *              false if some attributes contained placeholders
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              if the unit of work is optimistic
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is not persistent
     * @exception   ServiceException 
     *              if the object can't be synchronized
     */
    public boolean objFlush(
    ) throws ServiceException {
        return getDelegate().objFlush();
    }
    

    //--------------------------------------------------------------------------
    // Unit of work boundaries
    //--------------------------------------------------------------------------

    /**
     * After this call the object observes unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is locked 
     * @exception   ServiceException 
     *              if the object can't be added to the unit of work for
     *        another reason.
     */
    public void objAddToUnitOfWork(
    ) throws ServiceException {
        getDelegate().objAddToUnitOfWork();
    }
     
    /**
     * After this call the object ignores unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is dirty.
     * @exception   ServiceException 
     *              if the object can't be removed from its unit of work for
     *        another reason 
     */
    public void objRemoveFromUnitOfWork(
    ) throws ServiceException {
        getDelegate().objRemoveFromUnitOfWork();
    }

    //--------------------------------------------------------------------------
    // Life Cycle Operations
    //--------------------------------------------------------------------------

    /**
     * The copy operation makes a copy of the object. The copy is located in the
     * scope of the container passed as the first parameter and includes the
     * object's default fetch set.
     *
     * @return    an object initialized from the existing object.
     * 
     * @param     there
     *            the new object's container or <code>null</code>, in which case
     *            the object will not belong to any container until it is moved
     *            to a container.
     * @param     criteria
     *            The criteria is used to add the object to the container or 
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * @exception ServiceException
     *            if the copy operation fails.
     */
    public Object_1_0 objCopy(
        FilterableMap<String, Object_1_0> there,
        String criteria
    ) throws ServiceException {
        return getDelegate().objCopy(there,criteria);
    } 
    
    /**
     * The move operation moves the object to the scope of the container passed
     * as the first parameter. The object remains valid after move has
     * successfully executed.
     *
     * @param     there
     *            the object's new container.
     * @param     criteria
     *            The criteria is used to move the object to the container or 
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * @exception ServiceException  ILLEGAL_STATE
     *            if the object is persistent.
     * @exception ServiceException BAD_PARAMETER
     *            if <code>there</code> is <code>null</code>.
     * @exception ServiceException  
     *            if the move operation fails.
     */
    public void objMove(
        FilterableMap<String, Object_1_0> there,
        String criteria
    ) throws ServiceException {
        getDelegate().objMove(there,criteria);
    } 
     
    /**
     * Removes an object. 
     * <p>
     * Neither <code>getValue()</code> nor <code>setValue()</code>
     * calls are allowed after an <code>remove()</code> invocation and
     * <code>isDeleted()</code> will return <code>true</code> unless the
     * object has beeen transient.
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              If the object refuses to be removed.
     * @exception   ServiceException 
     *              if the object can't be removed
     */
    public void objRemove(
    ) throws ServiceException {
        getDelegate().objRemove();
    }


    //--------------------------------------------------------------------------
    // State Queries
    //--------------------------------------------------------------------------

    /**
     * Tests whether this object is dirty. Instances that have been modified,
     * deleted, or newly made persistent in the current unit of work return
     * true.
     * <p>
     * Transient instances return false. 
     * 
     * @return true if this instance has been modified in the current unit
     *         of work.
     */ 
    public boolean objIsDirty(
    ) throws ServiceException {
        return getDelegate().objIsDirty();
    }

    /**
     * Tests whether this object is persistent. Instances that represent
     * persistent objects in the data store return true. 
     * 
     * @return true if this instance is persistent.
     */
    public boolean objIsPersistent(
    ) throws ServiceException {
        return getDelegate().objIsPersistent();
    }

    /**
     * Tests whether this object has been newly made persistent. Instances
     * that have been made persistent in the current unit of work return true. 
     * <p>
     * Transient instances return false. 
     *
     * @return  true if this instance was made persistent in the current unit
     *          of work. 
     */
    public boolean objIsNew(
    ) throws ServiceException {
        return getDelegate().objIsNew();
    }

    /**
     * Tests whether this object has been deleted. Instances that have been
     * deleted in the current unit of work return true. 
     * Transient instances return false. 
     *
     * @return  true if this instance was deleted in the current unit of work.
     */
    public boolean objIsDeleted(
    ) throws ServiceException {
        return getDelegate().objIsDeleted();
    }

    /**
     * Tests whether this object belongs to the current unit of work.
     *
     * @return  true if this instance belongs to the current unit of work.
     */
    public boolean objIsInUnitOfWork(
    ) throws ServiceException {
        return getDelegate().objIsInUnitOfWork();
    }


    //--------------------------------------------------------------------------
    // Values
    //--------------------------------------------------------------------------

    /**
     * Set an attribute's value.
     * <p>
     * This method returns a <code>BAD_PARAMETER</code> exception unless the 
     * feature is single valued or a stream. 
     *
     * @param       feature
     *              the attribute's name
     * @param       to
     *              the object.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is write protected or the feature is a
     *        stream modified in the current unit of work.
     * @exception   ServiceException BAD_PARAMETER
     *              if the feature is multi-valued
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ServiceException 
     *              if the object is not accessible
     */
    public void objSetValue(
        String feature,
        Object to
    ) throws ServiceException {
        getDelegate().objSetValue(feature, to);
    }

    /**
     * Get a feature.
     *
     * @param       feature
     *              the feature's name
     *
     * @return      the object representing the feature;
     *              or null if the feature's value hasn't been set yet.
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ServiceException 
     *              if the object is not accessible
     */
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return getDelegate().objGetValue(feature);
    }
    
    /**
     * Get a List attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a list
     */
    public List<Object> objGetList(
        String feature
    ) throws ServiceException {
        return getDelegate().objGetList(feature);
    }
        
    /**
     * Get a Set attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a set
     */
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        return getDelegate().objGetSet(feature);
    }
    
    /**
     * Get a SparseArray attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature's value is not a sparse array
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    public SortedMap<Integer,Object> objGetSparseArray(
        String feature
    ) throws ServiceException {
        return getDelegate().objGetSparseArray(feature);
    }
        
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
        return getDelegate().objGetLargeObject(feature);
    }

    /**
     * Get a reference feature.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature is not a reference
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    public FilterableMap<String, Object_1_0> objGetContainer(
        String feature
    ) throws ServiceException {
        return getDelegate().objGetContainer(feature);
    }


    //--------------------------------------------------------------------------
    // Operations
    //--------------------------------------------------------------------------

    /**
     * Invokes an operation asynchronously.
     *
     * @param       operation
     *              The operation name
     * @param       arguments
     *              The operation's arguments object. 
     *
     * @return      a structure with the result's values if the accessor is
     *        going to populate it after the unit of work has committed
     *        or null if the operation's return value(s) will never be
     *        available to the accessor.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if no unit of work is in progress
     * @exception   ServiceException NOT_SUPPORTED
     *              if either asynchronous calls are not supported by the 
     *        manager or the requested operation is not supportd by the
     *        object.
     * @exception   ServiceException 
     *        if the invocation fails for another reason
     */
    public Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
            Structure_1_0 arguments
    ) throws ServiceException {
        return getDelegate().objInvokeOperationInUnitOfWork(
            operation,
            arguments
        );
    }

    /**
     * Invokes an operation synchronously.
     * <p>
     * Only query operations can be invoked synchronously unless the unit of
     * work is non-optimistic or committing.
     *
     * @param       operation
     *              The operation name
     * @param       arguments
     *              The operation's arguments object. 
     *
     * @return      the operation's return object
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if a non-query operation is called in an inappropriate
     *        state of the unit of work.
     * @exception   ServiceException NOT_SUPPORTED
     *              if either synchronous calls are not supported by the 
     *        manager or the requested operation is not supportd by the
     *        object.
     * @exception   ServiceException 
     *        if a checked exception is thrown by the implementation or
     *        the invocation fails for another reason.
     */
    public Structure_1_0 objInvokeOperation(
        String operation,
            Structure_1_0 arguments
    ) throws ServiceException {
        return getDelegate().objInvokeOperation(
            operation,
            arguments
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddEventListener(java.lang.String, java.util.EventListener)
     */
    public void objAddEventListener(
        String feature, 
        EventListener listener
    ) throws ServiceException {
        getDelegate().objAddEventListener(feature, listener);     
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveEventListener(java.lang.String, java.util.EventListener)
     */
    public void objRemoveEventListener(
        String feature, 
        EventListener listener
    ) throws ServiceException {
        getDelegate().objRemoveEventListener(feature, listener);      
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetEventListeners(java.lang.String, java.lang.Class)
     */
    public <T extends EventListener> T[] objGetEventListeners(
        String feature, 
        Class<T> listenerType
    ) throws ServiceException {
        return getDelegate().objGetEventListeners(feature,listenerType);
    }

}
