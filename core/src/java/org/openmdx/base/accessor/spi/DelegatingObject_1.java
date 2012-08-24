/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: DelegatingObject_1 class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.accessor.spi;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.resource.ResourceException;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 * A delegating object
 */
public abstract class DelegatingObject_1
    extends AbstractDataObject_1 {

    /**
     * Constructor 
     *
     * @param object
     */
    protected DelegatingObject_1(
        DataObject_1_0 object
    ){
        this.delegate = object;
    }

    /**
     * Constructor
     */
    protected DelegatingObject_1(
    ){
    }
    
    /**
     * The delegate
     */
    protected DataObject_1_0 delegate;

    /**
     * Retrieve the object's delegate
     * 
     * @return the object's delegate
     */
    protected DataObject_1_0 getDelegate(
    ) throws ServiceException {
        ServiceException inaccessibilityReason = this.getInaccessibilityReason();
        if(inaccessibilityReason == null) {
            return this.delegate;
        } 
        else {
            throw new ServiceException(inaccessibilityReason);
        }
    }
     
    /**
     * Retrieve the object's state delegate
     * 
     * @return the object's state delegate
     */
    protected DataObject_1_0 getStateDelegate (
    ){
        try {
            return getDelegate();
        } catch(Exception e) {
            throw new JDOUserException(
                "Unable to get object state",
                e,
                this
            );
        }        
    }
    
    /**
     * Replace the object's delegate
     * 
     * @param delegate the object's delegate
     */
    protected void setDelegate(
        DataObject_1_0 delegate
    ) {
        if(this.delegate != null) {
            super.setInaccessibilityReason(null);
        }
        this.delegate = delegate;
    }
    
    //------------------------------------------------------------------------
    // Implements DataObject_1_0
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
    public boolean objIsInaccessible(
    ){
        return 
            super.objIsInaccessible() ||
            this.delegate == null || 
            this.delegate.objIsInaccessible();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.AbstractObject_1#getInaccessibilityReason()
     */
    @Override
    public ServiceException getInaccessibilityReason(
    ) throws ServiceException {
        return 
            super.objIsInaccessible() ? super.getInaccessibilityReason() :
            this.delegate != null ? this.delegate.getInaccessibilityReason() : 
            null;
    }
    
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
        return this.getDelegate().objGetClass();
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
        return this.getDelegate().objDefaultFetchGroup();
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
    public void objMakeTransactional(
    ) throws ServiceException {
        DataObject_1_0 delegate = this.getDelegate();
        JDOHelper.getPersistenceManager(delegate).makeTransactional(delegate);
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
    public void objMakeNontransactional(
    ) throws ServiceException {
        DataObject_1_0 delegate = this.getDelegate();
        JDOHelper.getPersistenceManager(delegate).makeNontransactional(delegate);
    }

    //--------------------------------------------------------------------------
    // Life Cycle Operations
    //--------------------------------------------------------------------------

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
        Container_1_0 there,
        String criteria
    ) throws ServiceException {
        getDelegate().objMove(there,criteria);
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
    public boolean jdoIsDirty(
    ) {
        return this.getStateDelegate().jdoIsDirty();
    }

    /**
     * Tests whether this object is persistent. Instances that represent
     * persistent objects in the data store return true. 
     * 
     * @return true if this instance is persistent.
     */
    public boolean jdoIsPersistent(
    ) {
        return this.getStateDelegate().jdoIsPersistent();
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
    public boolean jdoIsNew(
    ){
        return this.getStateDelegate().jdoIsNew();
    }

    /**
     * Tests whether this object has been deleted. Instances that have been
     * deleted in the current unit of work return true. 
     * Transient instances return false. 
     *
     * @return  true if this instance was deleted in the current unit of work.
     */
    public boolean jdoIsDeleted(
    ) {
        return this.getStateDelegate().jdoIsDeleted();
    }

    /**
     * Tests whether this object belongs to the current unit of work.
     *
     * @return  true if this instance belongs to the current unit of work.
     */
    public boolean jdoIsTransactional(
    ) {
        return this.getStateDelegate().jdoIsTransactional();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Object_1_0#objIsContained()
     */
    public boolean objIsContained(
    ){
        return this.getStateDelegate().objIsContained();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#getContainer(boolean)
     */
//  @Override
    public Container_1_0 getContainer(
        boolean forEviction
    ) {
        return this.getStateDelegate().getContainer(forEviction);
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
    public void jdoCopyFields(
        Object other, 
        int[] fieldNumbers
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
     */
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(
        Object oid
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(
        ObjectIdFieldSupplier fm, 
        Object oid
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetObjectId()
     */
    public Path jdoGetObjectId(
    ) {
        return this.delegate.jdoGetObjectId();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
     */
    public UUID jdoGetTransactionalObjectId(
    ) {
        return this.delegate.jdoGetTransactionalObjectId();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    public Object jdoGetVersion(
    ) {
        return this.delegate.jdoGetVersion();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    public boolean jdoIsDetached(
    ) {
        return getStateDelegate().jdoIsDetached();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    public void jdoMakeDirty(
        String fieldName
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    public PersistenceCapable jdoNewInstance(
        StateManager sm
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
    public PersistenceCapable jdoNewInstance(
        StateManager sm, 
        Object oid
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    public Object jdoNewObjectIdInstance(
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    public Object jdoNewObjectIdInstance(
        Object o
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    public void jdoProvideField(
        int fieldNumber
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    public void jdoProvideFields(
        int[] fieldNumbers
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    public void jdoReplaceField(
        int fieldNumber
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    public void jdoReplaceFields(
        int[] fieldNumbers
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    public void jdoReplaceFlags(
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    public void jdoReplaceStateManager(
        StateManager sm
    ) throws SecurityException {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Cloneable#openmdxjdoClone()
     */
    public DataObject_1_0 openmdxjdoClone(String... exclude
    ) {
        throw new UnsupportedOperationException("Operation not supported by DelegatingObject_1");
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
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException {
        return getDelegate().objGetContainer(feature);
    }

    //--------------------------------------------------------------------------
    // Operations
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    public boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException {
        try {
            return getDelegate().execute(ispec, input, output);
        } catch (ServiceException exception) {
            throw BasicException.initHolder(
                new ResourceException(
                    "Method invocation failure",
                    BasicException.newEmbeddedExceptionStack(exception)
                )
            );
        }
    }

}
