/*
 * ====================================================================
 * Description: Abstract Object_1
 * Revision:    $Revision: 1.21 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/19 16:30:57 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.InstanceCallbacks;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.resource.cci.InteractionSpec;

import org.openmdx.application.dataprovider.accessor.PerformanceHelper;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.Delegating_1;
import org.openmdx.base.accessor.spi.MarshallingObject_1;
import org.openmdx.base.aop1.ContextCapable_1;
import org.openmdx.base.aop1.ExtentCapable_1;
import org.openmdx.base.aop1.Removable_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Model_1_6;
import org.openmdx.base.naming.Path;
import org.openmdx.compatibility.state1.aop1.StateCapableContainer_1;
import org.openmdx.compatibility.state1.aop1.StateContainer_1;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.state2.aop1.DateTimeState_1;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.DateTimeStateContext;

/**
 * Registers the the delegates with their manager
 */
class ObjectView_1 
    extends MarshallingObject_1<Manager_1> 
    implements ObjectView_1_0, Serializable, InstanceCallbacks
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
        Manager_1 marshaller,
        DataObject_1_0 dataObject
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
    private DataObject_1_0 dataObject;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -8224702798301616975L;

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
    public boolean equals(
        Object that
    ) {
        try {
            return Delegating_1.equal(this, that);
        }
        catch(Exception e) {
            throw new RuntimeServiceException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode(
    ) {
        try {
            return Delegating_1.hashCode(this);
        }
        catch(Exception e) {
            throw new RuntimeServiceException(e);
        }           
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
    public DataObject_1_0 objGetDelegate(
    ) {
        return this.dataObject;
    }    
        
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_6#objSetDelegate(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    public void objSetDelegate(
        DataObject_1_0 delegate
    ) {
        this.marshaller.cacheObject(delegate, this);
        this.dataObject = delegate;
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
    // Implements ObjectView_1_0
    //--------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.MarshallingObject_1#openmdxjdoClone()
     */
    @Override
    public DataObject_1_0 openmdxjdoClone() {
        try {
            return (DataObject_1_0) getMarshaller().marshal(
                objGetDelegate().openmdxjdoClone()
            );
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    public PersistenceManager jdoGetPersistenceManager(
    ) {
        return this.getMarshaller();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0#getModel()
     */
    public Model_1_6 getModel() {
        return this.getMarshaller().getModel();
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
    ) throws ServiceException {
        return objGetDelegate().objIsContained();
    }
    
    //--------------------------------------------------------------------------
    // Implements Object_1_0
    //--------------------------------------------------------------------------

    /**
     * Return the object class unless the object is hollow
     * 
     * @return the object class; or <code>null</code> if the object is hollow
     * 
     * @throws ServiceException 
     */
    private String getRecordName() throws ServiceException{
        return (String) this.dataObject.objGetValue(DataObject_1_0.RECORD_NAME_REQUEST);
    }
    
    /**
     * Returns the object's identity.
     *
     * @return  the object's identity;
     *          or null for transient objects
     */
    @Override
    public Object jdoGetObjectId(
    ) {
        try {
            Path resourceIdentifier = (Path)this.dataObject.jdoGetObjectId();
            if(
                 this.getInteractionSpec() == null &&
                 "org:openmdx:compatibility:state1:StateCapable".equals(getRecordName())
            ) {
                 return StateCapables.getStateCapable(resourceIdentifier);     
            }
            return resourceIdentifier;
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get object id",
                e,
                this
            );
        }
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
        if(this.classifier == null){
            String className = objGetClass();
            if(className == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    "Undetermined object class",
                    new BasicException.Parameter("objectId", this.jdoGetObjectId()),
                    new BasicException.Parameter("interactionSpec", this.getInteractionSpec())
                );
            }
            this.classifier = getModel().getElement(className);
        }
        return this.classifier;
    }
    
    //--------------------------------------------------------------------------
    // Extends MarshallingObject_1
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.MarshallingObject_1#getContainer(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Container_1_0 getContainer(
        String feature
    ) throws ServiceException {
        InteractionSpec interactionSpec = getInteractionSpec();
        Model_1_0 model = this.getModel();
        if(interactionSpec == null && "org:openmdx:compatibility:state1:StateCapable".equals(this.objGetClass())) {
            ModelElement_1_0 reference = model.getReferenceType((Path)this.dataObject.jdoGetObjectId());
            String type = ((Path)reference.objGetValue("type")).getBase();
            if(model.isSubtypeOf(type, "org:openmdx:compatibility:state1:DateState")) {
                return new StateContainer_1(
                    this, 
                    feature
               );
            } else {
                return this.dataObject.objGetContainer(feature);
            }
        } else if ("extent".equals(feature) && model.isInstanceof(this, "org:openmdx:base:Segment")) {
            return new org.openmdx.base.aop1.Extent_1(
                this,
                this.dataObject.objGetContainer(feature)
            );
        } else {
            Map<String, ModelElement_1_0> references = (Map<String, ModelElement_1_0>) getClassifier().objGetValue("reference");
            ModelElement_1_0 reference = references.get(feature);
            String type = ((Path)reference.objGetValue("type")).getBase();
            if(model.isSubtypeOf(type, "org:openmdx:compatibility:state1:StateCapable")) {
                return new StateCapableContainer_1(
                    this
                );
            } else if(model.isSubtypeOf(type, "org:openmdx:compatibility:state1:DateState")) {
                return interactionSpec instanceof DateStateContext ? new org.openmdx.compatibility.state1.aop1.StatedObjectContainer_1(
                    this,
                    this.dataObject.objGetContainer(feature)
                ) : new StateContainer_1(
                    this, 
                    feature
               );
            } else if (model.isSubtypeOf(type, "org:openmdx:state2:StateCapable")) {
                return interactionSpec instanceof DateStateContext ? new org.openmdx.state2.aop1.StatedObjectContainer_1(
                    this,
                    this.dataObject.objGetContainer(feature)
                ) : super.getContainer(
                    feature
                );
            } else {
                return super.getContainer(feature);
            }
        }
    }

    //--------------------------------------------------------------------------
    // Extends DynamicallyDelegatingObject_1
    //--------------------------------------------------------------------------

    /**
     * Hollow -> Persistent-Clean Transition
     */
    @SuppressWarnings("unchecked")
    protected synchronized void initialize(
    ) throws ServiceException {
        if(this.hollow) {
            Model_1_0 model = getModel();
            PlugIn_1 delegate = new PlugIn_1(this);
            String recordName = getRecordName();
            if(recordName == null){
                Path objectId = (Path) this.dataObject.jdoGetObjectId();
                if(objectId != null && objectId.size() > 1) {
                    ModelElement_1_0 referencedType = model.getTypes(objectId)[2];
                    for(Object superType : referencedType.objGetList("allSupertype")) {
                        if("org:openmdx:state2:StateCapable".equals(((Path)superType).getBase())) {
                            PerformanceHelper.retrieveContainer(this.dataObject);
                        }
                    }
                }
            }
            ModelElement_1_0 dataObjectType = model.getElement(this.dataObject.objGetClass());
            if (model.isSubtypeOf(dataObjectType, "org:openmdx:base:ContextCapable")) {
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
                if(model.isSubtypeOf(dataObjectType, "org:openmdx:compatibility:state1:StateCapable")) {
                    if(
                        !this.dataObject.jdoIsPersistent() || 
                        !StateCapables.isCoreObject((Path)this.jdoGetObjectId())
                    ) {
                        //
                        // org::openmdx::compatibility::state1::DateState view
                        //
                        delegate = new org.openmdx.compatibility.state1.aop1.DateState_1(
                            this,
                            false // attachCore
                        );                    
                    }
                } else if(model.isInstanceof(this.dataObject, "org:openmdx:state2:StateCapable")) {
                    state2 = true;
                    if(model.isSubtypeOf(dataObjectType, "org:openmdx:compatibility:state1:HistoryState")) {
                        //
                        // org::openmdx::compatibility::state1::DateState View
                        //
                        delegate = new org.openmdx.compatibility.state1.aop1.DateState_1(
                            this,
                            true // attachCore
                        );
                    } else {
                        //
                        // org::openmdx::state2::DateState View
                        //
                        delegate = new org.openmdx.state2.aop1.DateState_1(
                            this,
                            true // attachCore
                        );
                        
                    }
                }
            } else if(interactionSpec instanceof DateTimeStateContext) {
                if(model.isSubtypeOf(dataObjectType, "org:openmdx:state2:DateTimeState")) {
                    //
                    // org::openmdx::state2::DateTimeState View
                    //
                    state2 = true;
                    delegate = new DateTimeState_1(
                        this, 
                        true // attachCore
                     );
                }
            }
            //
            // Aspect Capabilities
            //
            if(model.isSubtypeOf(dataObjectType, "org:openmdx:compatibility:state1:DateState")) {
                //
                // org::openmdx::compatibiliy::state1 Aspect
                //
                delegate = new org.openmdx.compatibility.state1.aop1.BasicState_1(
                    this,
                    delegate
                );                    
            } else if(state2) {
                //
                // org::openmdx::base Aspect 
                //
                delegate = new org.openmdx.state2.aop1.BasicState_1(
                    this,
                    delegate
                );                                            
            } else if(model.isSubtypeOf(dataObjectType, "org:openmdx:base:Aspect")) {
                //
                // org::openmdx::base Aspect 
                //
                delegate = new org.openmdx.base.aop1.Aspect_1(
                    this,
                    delegate
                );                                            
            }
            //
            // Life Cycle Capabilities
            //
            if (model.isSubtypeOf(dataObjectType, "org:openmdx:base:Removable")) {
                //
                // org::openmdx::base Removable Capability
                //
                delegate = new Removable_1(
                    this,
                    delegate
                );
            }
            //
            // Extent Capability
            //
            if (model.isSubtypeOf(dataObjectType, "org:openmdx:base:ExtentCapable")) {
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
    protected PlugIn_1 getDelegate(
    ) throws ServiceException {
        if(this.hollow) {
            this.initialize();
        }
        return (PlugIn_1) super.getDelegate();
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
            getMarshaller().cacheObject(getDelegate(), this);
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
    private PlugIn_1 next(){
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
     * @see javax.jdo.listener.LoadCallback#jdoPostLoad()
     */
    public void jdoPostLoad() {
        next().jdoPostLoad();
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.ClearCallback#jdoPreClear()
     */
    public void jdoPreClear() {
        next().jdoPreClear();
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