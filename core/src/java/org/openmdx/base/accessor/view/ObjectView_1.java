/*
 * ====================================================================
 * Description: Abstract Object_1
 * Revision:    $Revision: 1.50 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/30 13:08:44 $
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
package org.openmdx.base.accessor.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOHelper;
import javax.jdo.listener.ClearCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.StoreCallback;
import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.MarshallingObject_1;
import org.openmdx.base.aop1.PlugIn_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;

/**
 * Registers the the delegates with their manager
 */
class ObjectView_1 
    extends MarshallingObject_1<ViewManager_1> 
    implements ObjectView_1_0, Serializable, ClearCallback, DeleteCallback, StoreCallback
{

    /**
     * Constructor 
     * 
     * @param marshaller
     * @param dataObject
     * @param interactionSpec 
     * 
     * @throws ServiceException
     */
    ObjectView_1(
        ViewManager_1 marshaller,
        DataObject_1_0 dataObject
    ) throws ServiceException{
        super(
            null, 
            marshaller
        );
        this.dataObject = dataObject;
        this.hollow = true;
    }

    /**
     * This flag tells whether the object is hollow or not, i.e. whether its
     * plug-ins are already set up or not.
     */
    private transient boolean hollow;

    /**
     * The associated data object
     */
    private DataObject_1_0 dataObject;
    
    /**
     * 
     */
    private transient Interceptor_1 interceptor;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -7826608051595033293L;

    /**
     * Tells whether the instance is hollow
     * 
     * @return <code>true</code> if this instance is hollow
     */
    boolean isHollow(){
        return this.hollow;
    }
    
    //--------------------------------------------------------------------------
    // Extends Object
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.AbstractObject_1#toString()
     */
    @Override
    public String toString() {
        return toString(
            this, 
            String.valueOf(getInteractionSpec())
        );
    }

    //--------------------------------------------------------------------------
    // Implements Delegating_1_0
    //--------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Delegating_1_0#objGetDelegate()
     */
    public DataObject_1_0 objGetDelegate(
    ) {
        return this.dataObject;
    }    
        
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#getStateDelegate()
     */
    @Override
    protected DataObject_1_0 getStateDelegate() {
        return this.hollow ? this.dataObject : this.delegate;
    }

    
    //--------------------------------------------------------------------------
    // Implements ObjectView_1_0
    //--------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_6#objSetDelegate(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    public void objSetDelegate(
        DataObject_1_0 delegate
    ) throws ServiceException {
        this.setInaccessibilityReason(null);
        this.marshaller.register(delegate, this);
        this.dataObject = delegate;
        Interceptor_1 terminalPlugIn = null;
        for(
            Interceptor_1 nextPlugIn = this.interceptor == null ? getDelegate() : this.interceptor;
            nextPlugIn != null;
            nextPlugIn = nextPlugIn.getNext()
        ){
            terminalPlugIn = nextPlugIn;
        }
        terminalPlugIn.setDelegate(delegate);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_6#getInteractionSpec()
     */
    public final InteractionSpec getInteractionSpec(
    ){
        return getMarshaller().getInteractionSpec();
    }

    /**
     * Removes an object. 
     * <p>
     * Neither <code>getValue()</code> nor <code>setValue()</code>
     * calls are allowed after an <code>remove()</code> invocation and
     * <code>isDeleted()</code> will return <code>true</code> unless the
     * object has been transient.
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              If the object refuses to be removed.
     * @exception   ServiceException 
     *              if the object can't be removed
     */
    public void objDelete(
    ) throws ServiceException {
        getDelegate().objDelete();
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
    @Override
    public void objMakeTransactional(
    ) throws ServiceException {
        JDOHelper.getPersistenceManager(this.dataObject).makeTransactional(this.dataObject);
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
    @Override
    public void objMakeNontransactional(
    ) throws ServiceException {
        JDOHelper.getPersistenceManager(this.dataObject).makeNontransactional(this.dataObject);
    }

    //--------------------------------------------------------------------------
    // Implements ObjectView_1_0
    //--------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.MarshallingObject_1#openmdxjdoClone()
     */
    @Override
    public DataObject_1_0 openmdxjdoClone() {
        try {
            return (DataObject_1_0) getMarshaller().marshal(
                this.dataObject.openmdxjdoClone()
            );
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    public DataObjectManager_1_0 jdoGetPersistenceManager(
    ) {
        return this.getMarshaller();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0#getModel()
     */
    public Model_1_0 getModel() {
        return Model_1Factory.getModel();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.AbstractObject_1#setInaccessibilityReason(org.openmdx.base.exception.ServiceException)
     */
    @Override
    protected void setInaccessibilityReason(
        ServiceException inaccessibilityReason
    ) {
        super.setInaccessibilityReason(inaccessibilityReason);
    }

    @Override
    public boolean objIsContained(
    ){
        return this.dataObject.objIsContained();
    }
    
    //--------------------------------------------------------------------------
    // Implements Object_1_0
    //--------------------------------------------------------------------------

    /**
     * Returns the object's identity.
     *
     * @return  the object's identity;
     *          or null for transient objects
     */
    @Override
    public Path jdoGetObjectId(
    ) {
        return this.dataObject.jdoGetObjectId();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#jdoGetTransactionalObjectId()
     */
    @Override
    public UUID jdoGetTransactionalObjectId() {
        return this.dataObject.jdoGetTransactionalObjectId();
    }

    //--------------------------------------------------------------------------
    // Extends DynamicallyDelegatingObject_1
    //--------------------------------------------------------------------------

    /**
     * Hollow -> Persistent-Clean Transition
     */
    protected synchronized void initialize(
    ) throws ServiceException {
        if(this.hollow) {
            this.interceptor = new Interceptor_1(this);
            for(PlugIn_1_0 plugIn : super.marshaller.getPlugIn()) {
                this.interceptor = plugIn.getInterceptor(this, this.interceptor);
            }
            super.setDelegate(this.interceptor);
            this.interceptor = null;
            this.hollow = false;
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.DynamicallyDelegatingObject_1#getDelegate()
     */
    @Override
    protected Interceptor_1 getDelegate(
    ) throws ServiceException {
        if(this.hollow) {
            this.initialize();
        }
        return (Interceptor_1) super.getDelegate();
    }

    //--------------------------------------------------------------------------
    // Implements Serializable
    //--------------------------------------------------------------------------

    /**
     * Save the data of the <tt>Object_1_0</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The objects data
     */
    private synchronized void writeObject(
        java.io.ObjectOutputStream stream
    ) throws java.io.IOException {
        // stream.defaultWriteObject(); has nothing to do
    }

    /**
     * Reconstitute the <tt>Object_1_0</tt> instance from a stream (that is,
     * deserialize it).
     */
    private synchronized void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        // stream.defaultReadObject(); has nothing to do
        this.hollow = true;
        try {
            getMarshaller().register(getDelegate(), this);
        } catch (Exception exception) {
            throw (IOException) new IOException().initCause(exception);
        }
    }

    //--------------------------------------------------------------------------
    // Implements InstanceCallbacks
    //--------------------------------------------------------------------------

    /**
     * Retrieve the delegate plug-in
     * 
     * @return the delegate plug-in
     * 
     * @exception JDODataStoreException if the delegate is inaccessible.
     */
    private Interceptor_1 next(){
        try {
            return getDelegate();
        } catch (ServiceException exception) {
            throw new JDODataStoreException(
                "The delegate is unavailable", 
                exception
            );
        }
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreCallback#jdoPreStore()
     */
    public void jdoPreStore() {
        next().jdoPreStore();
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteCallback#jdoPreDelete()
     */
    public void jdoPreDelete() {
        next().jdoPreDelete();
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.ClearCallback#jdoPreClear()
     */
    public void jdoPreClear() {
        next().jdoPreClear();
    }

}