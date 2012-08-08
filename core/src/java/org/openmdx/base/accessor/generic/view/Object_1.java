/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Object_1.java,v 1.54 2008/12/15 03:15:36 hburger Exp $
 * Description: Abstract Object_1
 * Revision:    $Revision: 1.54 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:36 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.generic.view;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.StoreCallback;
import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.spi.Delegating_1;
import org.openmdx.base.accessor.generic.spi.MarshallingObject_1;
import org.openmdx.base.accessor.generic.spi.Object_1_5;
import org.openmdx.base.accessor.generic.spi.Object_1_6;
import org.openmdx.base.aop2.core.ContextCapable_1;
import org.openmdx.base.aop2.core.ExtentCapable_1;
import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_4;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.ModelUtils;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.state2.aop2.core.DateTimeState_1;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.DateTimeStateContext;

/**
 * Registers the the delegates with their manager
 */
class Object_1 
    extends MarshallingObject_1<Manager_1> 
    implements Object_1_6, Serializable, LoadCallback, StoreCallback, DeleteCallback
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
    Object_1(
        Manager_1 marshaller,
        Object_1_5 dataObject
    ) throws ServiceException{
        super(
            null, 
            marshaller
        );
        this.dataObject = dataObject;
        this.hollow = true;
        dataObject.objAddEventListener(
            null, // feature 
            marshaller
        );
    }

    /**
     * This flag tells whether the object is hollow or not, i.e. whether its
     * plug-ins are already set up or not.
     */
    private transient boolean hollow;

    /**
     * The associated data object
     */
    private Object_1_5 dataObject;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -8224702798301616975L;

    /**
     * Cache the feature queries
     */
    private transient ConcurrentMap<String,Iterable<?>> featureQueries = 
        new ConcurrentHashMap<String,Iterable<?>>();;
    
    /**
     * The object's classifier
     */
    private transient ModelElement_1_0 classifier;
    
    
    //--------------------------------------------------------------------------
    // Extends Object
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return Delegating_1.equal(this, that);    
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Delegating_1.hashCode(this);
    }

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
    // Implements Object_1_6
    //--------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Delegating_1_0#objGetDelegate()
     */
    public Object_1_5 objGetDelegate() {
        return this.dataObject;
    }    
        
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_6#objSetDelegate(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    public void objSetDelegate(
        Object_1_5 delegate
    ) {
        this.dataObject = delegate;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_6#getInteractionSpec()
     */
    public final InteractionSpec getInteractionSpec(
    ){
        return getMarshaller().getInteractionSpec();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#clone(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    public Object_1_0 cloneDelegate(
        Object_1_0 original, 
        Path identity
    ) throws ServiceException {
        return ((Connection_1_4)this.marshaller.getConnection()).cloneObject(
            identity, 
            original, 
            true // completelyDirty
        );
    }

        
    //--------------------------------------------------------------------------
    // Implements Object_1_5
    //--------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getAspect(java.lang.String)
     */
    public Map<String, Object_1_0> getAspect(
        String aspectClass
    ) throws ServiceException {
        Object_1_0 delegate = getDelegate();
        if(delegate instanceof Object_1_5) {
            return ((Object_1_5)delegate).getAspect(aspectClass);
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Aspect capability not supported",
                new BasicException.Parameter(
                    "delegateClass", 
                    delegate == null ? null : delegate.getClass().getName()
                ), new BasicException.Parameter(
                    "aspectClass",
                    aspectClass
                )
            );    
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    public ObjectFactory_1_0 getFactory() {
        return this.marshaller.getConnection();
    }

    
    //--------------------------------------------------------------------------
    // Implements Object_1_1
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0#getModel()
     */
    public Model_1_0 getModel() {
        return getMarshaller().getModel();
    }

    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0#setModel(org.openmdx.model1.accessor.basic.cci.Model_1_0)
     */
    public void setModel(Model_1_0 model) {
        throw new UnsupportedOperationException(
            "An object's model repository can't be replaced"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.DynamicallyDelegatingObject_1#objIsHollow()
     */
    @Override
    public boolean objIsHollow() {
        return this.hollow || super.objIsHollow();
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

    
    //--------------------------------------------------------------------------
    // Implements Object_1_0
    //--------------------------------------------------------------------------

    /**
     * Returns the object's identity.
     *
     * @return  the object's identity;
     *          or null for transient objects
     */
    public Path objGetPath(
    ) throws ServiceException {
        return this.dataObject == null ? null : this.dataObject.objGetPath();
    }

    /**
     * Retrieve the object's classifier
     * 
     * @return  the object's classifier
     * 
     * @throws ServiceException
     */
    protected ModelElement_1_0 getClassifier(
    ) throws ServiceException{
        return this.classifier == null ?
            this.classifier = getModel().getElement(objGetClass()) :
            this.classifier;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetIterable(java.lang.String)
     */
    public Iterable<?> objGetIterable(
        String featureName
    ) throws ServiceException {
        Iterable<?> reply = this.featureQueries.get(featureName);
        if(reply == null) {
            String multiplicity = ModelUtils.getMultiplicity(
                getModel().getFeatureDef(getClassifier(), featureName, false)
            );
            Iterable<?> concurrent = this.featureQueries.putIfAbsent(
                featureName, 
                reply = 
                    Multiplicities.LIST.equals(multiplicity) ? objGetList(featureName) :
                    Multiplicities.SET.equals(multiplicity) ? objGetSet(featureName) :
                    Multiplicities.SPARSEARRAY.equals(multiplicity) ? objGetSparseArray(featureName).values() :
                    new ValueCollection(featureName)
            );
            if(concurrent != null) {
                reply = concurrent;
            }
        }
        return reply;
    }

    
    //--------------------------------------------------------------------------
    // Extends DynamicallyDelegatingObject_1
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.DynamicallyDelegatingObject_1#objGetResourceIdentifier()
     */
    @Override
    public Object objGetResourceIdentifier() {
        return this.dataObject.objGetResourceIdentifier();
    }

    /**
     * Hollow -> Persistent-Clean Transition
     */
    protected synchronized void initialize(
    ) throws ServiceException{
        if(this.hollow) {
            Model_1_0 model = getModel();
            Object_1_0 delegate = this.dataObject;
            if (model.isInstanceof(this.dataObject, ContextCapable_1.CLASS)) {
                //
                // org::openmdx::base::ContextCapable
                //
                delegate = new ContextCapable_1(
                    this,
                    delegate
                );
            }
            //
            // State Views
            //
            InteractionSpec interactionSpec = super.getMarshaller().getInteractionSpec();
            boolean state2 = false;
            if(interactionSpec instanceof DateStateContext) {
                if(model.isInstanceof(this.dataObject, org.openmdx.compatibility.state1.aop2.core.DateState_1.CLASS)) {
                    //
                    // org::openmdx::compatibility::state1::DateState view
                    //
                    delegate = new org.openmdx.state2.aop2.core.DateState_1(this);                    
                } else if(
                    model.isInstanceof(this.dataObject, org.openmdx.state2.aop2.core.StateCapable_1.CLASS) &&
                    !model.isInstanceof(this.dataObject, org.openmdx.compatibility.state1.aop2.core.StateCapable_1.CLASS)
                ) {
                    //
                    // org::openmdx::state2::DateState View
                    //
                    delegate = new org.openmdx.state2.aop2.core.DateState_1(this);
                    state2 = true;
                }
            } else if(interactionSpec instanceof DateTimeStateContext) {
                if(model.isInstanceof(this.dataObject, DateTimeState_1.CLASS)) {
                    //
                    // org::openmdx::state2::DateTimeState View
                    //
                    delegate = new DateTimeState_1(this);
                    state2 = true;
                }
            }
            //
            // Aspect Capabilities
            //
            if(model.isInstanceof(this.dataObject, org.openmdx.compatibility.state1.aop2.core.DateState_1.CLASS)) {
                //
                // org::openmdx::compatibiliy::state1 Aspect
                //
                delegate = new org.openmdx.compatibility.state1.aop2.core.BasicState_1(
                    this,
                    delegate
                );                    
            } else if(state2) {
                //
                // org::openmdx::base Aspect 
                //
                delegate = new org.openmdx.state2.aop2.core.BasicState_1(
                    this,
                    delegate
                );                                            
            } else if(model.isInstanceof(this.dataObject, org.openmdx.base.aop2.core.Aspect_1.CLASS)) {
                //
                // org::openmdx::base Aspect 
                //
                delegate = new org.openmdx.base.aop2.core.Aspect_1(
                    this,
                    delegate
                );                                            
            }
            //
            // State Capabilities
            //
            if(model.isInstanceof(this.dataObject, org.openmdx.state2.aop2.core.StateCapable_1.CLASS)) {
                if(model.isInstanceof(this.dataObject, org.openmdx.compatibility.state1.aop2.core.StateCapable_1.CLASS)) {
                    //
                    // org::openmdx::compatibility::state1 State Capability
                    //
                    delegate = new org.openmdx.compatibility.state1.aop2.core.StateCapable_1(
                        this,
                        delegate
                    );                    
                } else {
                    //
                    // org::openmdx::state2 State Capability
                    //
                    delegate = new org.openmdx.state2.aop2.core.StateCapable_1(
                        this,
                        delegate
                    );                    
                }
            }
            if (model.isInstanceof(this.dataObject, ExtentCapable_1.CLASS)) {
                //
                // org::openmdx::base Extent Capability
                //
                delegate = new ExtentCapable_1(
                    this,
                    delegate
                );
            }
            super.setDelegate(delegate);
            this.hollow = false;
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.DynamicallyDelegatingObject_1#getDelegate()
     */
    @Override
    protected Object_1_0 getDelegate(
    ) throws ServiceException {
        if(this.hollow) {
            initialize();
        }
        return super.getDelegate();
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
        this.featureQueries = new ConcurrentHashMap<String,Iterable<?>>();
        try {
            getMarshaller().cache(getDelegate(), this);
        } catch (ServiceException exception) {
            throw new ExtendedIOException(exception);
        }
    }


    //--------------------------------------------------------------------------
    // Implements StoreCallback
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreCallback#jdoPreStore()
     */
    public void jdoPreStore() {
        try {
            Object_1_0 delegate = getDelegate();
            if(delegate instanceof StoreCallback) {
                ((StoreCallback)delegate).jdoPreStore();
            }
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }


    //--------------------------------------------------------------------------
    // Implements DeleteCallback
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteCallback#jdoPreDelete()
     */
    public void jdoPreDelete() {
        try {
            Object_1_0 delegate = getDelegate();
            if(delegate instanceof DeleteCallback) {
                ((DeleteCallback)delegate).jdoPreDelete();
            }
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }


    //--------------------------------------------------------------------------
    // Implements LoadCallback
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.LoadCallback#jdoPostLoad()
     */
    public void jdoPostLoad() {
        try {
            Object_1_0 delegate = getDelegate();
            if(delegate instanceof LoadCallback) {
                ((LoadCallback)delegate).jdoPostLoad();
            }
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    
    //--------------------------------------------------------------------------
    // Class ValueView
    //--------------------------------------------------------------------------

    /**
     * Value Collection
     */
    class ValueCollection implements Iterable<Object> {

        /**
         * Constructor 
         *
         * @param featureName
         */
        ValueCollection(
            String featureName
        ){
            this.featureName = featureName;     
        }
        
        /**
         * 
         */
        private final String featureName;

        /* (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
        public Iterator<Object> iterator() {
            try {
                Object value = objGetValue(this.featureName);
                return (
                    value == null ? Collections.emptySet() : Collections.singleton(value)
                ).iterator();
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }
        
    }

}