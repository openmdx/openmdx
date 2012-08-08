/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: SinkConnection_1.java,v 1.28 2008/03/19 17:13:11 hburger Exp $
 * Description: Propagating Connection
 * Revision:    $Revision: 1.28 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/19 17:13:11 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 *
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.generic.spi;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_3;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_3;
import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.Synchronization_1_0;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.base.transaction.UnitOfWork_1_1;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.naming.PathComponent;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * Propagating Connection
 */
@SuppressWarnings("unchecked")
public class SinkConnection_1 implements Synchronization_1_0 {

    /**
     * Constructor
     */
    protected SinkConnection_1(
    ){
        // Implements Serializable
    }

    /**
     * Constructor
     *
     * @param delegate
     * @param model
     *
     * @throws ServiceException
     */
    public SinkConnection_1(
        ObjectFactory_1_3 delegate,
        Model_1_0 model
    ){
        this.delegate = delegate;
        this.model = model;
        this.holders = new HashMap();
    }

    /**
     *
     */
    final static String DATE_STATE_CLASS_NAME = "org:openmdx:compatibility:state1:DateState";

    /**
     *
     */
    private UnitOfWork_1_1 unitOfWork = null;

    /**
     *
     */
    private final Map viewConnections = new HashMap();

    /**
     * The physical connection for commit
     */
    private ObjectFactory_1_3 delegate;

    /**
     *
     */
    private Map holders;

    /**
     *
     */
    private Model_1_0 model;

    /**
     *
     */
    private final static boolean DELEGATE_EVICT = false;

    public ViewConnection_1 getViewConnection(
        InteractionSpec context
    ) throws ServiceException{
        ViewConnection_1 viewConnection = (ViewConnection_1) this.viewConnections.get(context);
        if(viewConnection == null) {
            this.viewConnections.put(
                context,
                viewConnection = new ViewConnection_1 (
                    this,
                    getConnectionFactory().getConnection(context),
                    context
                )
            );
        }
        return viewConnection;
    }

    private Map getContainer(
        Path path, 
        boolean optional
    ){
        Map container = (Map) this.holders.get(path);
        if(container == null && !optional) {
            this.holders.put(
                path, 
                container = new TreeMap()
            );
        }
        return container;
    }
    
    void invalidate(
        SinkObject_1 object
    ) throws ServiceException{
        Path path = object.objGetPath();
        Map container = getContainer(path.getParent(), true);
        if(container != null) {
            container.remove(path.getBase());
        }
        if(DELEGATE_EVICT) {
            ((Manager_1) this.delegate).evict(object.objGetPath());
        }
    }

    /**
     * Retrieve the delegate connection
     *
     * @return the delegate connection
     */
    protected ObjectFactory_1_3 getDelegate(){
        return this.delegate;
    }

    /**
     * Retrieve the connection factory
     *
     * @return the connection factory
     */
    public ViewConnectionFactory_1_0 getConnectionFactory() {
        return (ViewConnectionFactory_1_0) this.delegate.getConnectionFactory();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.ObjectFactory_1_2#hasContainerManagedUnitOfWork()
     */
    public boolean hasContainerManagedUnitOfWork(
    ) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#close()
     */
    public void close(
    ) throws ServiceException {
        getDelegate().close();
    }

    /**
     * This method never returns <code>null</code>
     *
     * @param path
     * @param initializeCacheWithDelegate TODO
     * @param objectClass
     * @return
     *
     * @throws ServiceException
     */
    SinkObject_1 getObject(
        Path path,
        boolean dateStateInstance, 
        boolean initializeCacheWithDelegate
    ) throws ServiceException {
        Map container = getContainer(path.getParent(), false);
        SinkObject_1 object = (SinkObject_1) container.get(path.getBase());
        if(object == null) {
            container.put(
                path.getBase(),
                object = new SinkObject_1(
                    this,
                    path,
                    dateStateInstance, 
                    initializeCacheWithDelegate
                )
            );
        }
        return object;
    }

    /**
     * This method never returns <code>null</code>
     *
     * @param path
     * @param objectClass
     * @return
     *
     * @throws ServiceException
     */
    SinkObject_1 getObject(
        Path path,
        String objectClass
    ) throws ServiceException {
        Map container = getContainer(path.getParent(), false);
        SinkObject_1 object = (SinkObject_1) container.get(path.getBase());
        if(object == null) {
            container.put(
                path.getBase(),
                object = new SinkObject_1(
                    this,
                    path,
                    objectClass
                )
            );
        }
        return object;
    }

    /**
     * Retrieve a sink object to be referenced by one of its source objects
     *
     * @param sourceObject
     * @return a sink object with the same path as the source object
     *
     * @throws ServiceException
     */
    SinkObject_1 getObject(
        Object_1_0 sourceObject
    ) throws ServiceException{
        Path path = sourceObject.objGetPath();
        if(path == null) {
            return null;
        } else {
            Map container = getContainer(
                path.getParent(), 
                false // optional
            );
            SinkObject_1 object = (SinkObject_1) container.get(path.getBase());
            if(object == null) {
                container.put(
                    path.getBase(),
                    object = new SinkObject_1(
                        this,
                        path,
                        SinkObject_1.lenientGetObjectClass(sourceObject)
                    )
                );
            } else {
                object.lenientSetObjectClass(sourceObject);
            }
            return object;
        }
    }

    /**
     * This method may return <code>null</code>
     *
     * @param path
     *
     * @return
     */
    SinkObject_1 getObject(
        Path path
    ){
        Map container = getContainer(path.getParent(), true);
        return (SinkObject_1) (
            container == null ? null : container.get(path.getBase())
         );
    }

    SinkObject_1 marshal(
        Object_1_0 delegate
    ) throws ServiceException {
        Path path = delegate.objGetPath();
        SinkObject_1 object = new SinkObject_1(
            this,
            delegate, path.hashCode()
        );
        getContainer(
            path.getParent(),
            false
        ).put(
            path.getBase(),
            object
        );
        return object;
    }

    public UnitOfWork_1_1 getUnitOfWork(
    ) throws ServiceException {
        if(this.unitOfWork == null) {
            this.unitOfWork = new UnitOfWork(delegate.getUnitOfWork());
        }
        return this.unitOfWork;
    }

    /**
     * The afterBegin method notifies a provider or plug-in that a new
     * unit of work has started, and that the subsequent business methods on
     * the instance will be invoked in the context of the unit of work.
     */
    public void afterBegin(
    ) throws ServiceException {
        // nothing to do yet
    }

    /**
     * The beforeCompletion method notifies a provider or plug-in that a
     * unit of work is about to be committed.
     */
    public void beforeCompletion(
    ) throws ServiceException{
        for(
            Iterator h = this.holders.values().iterator();
            h.hasNext();
        ){
            for(
              Iterator i = ((Map)h.next()).values().iterator();
              i.hasNext();
            ){
                SinkObject_1 object = (SinkObject_1) i.next();
                if(!object.isHollow()) {
                    if(object.hasStateCache()) {
                        if(object.getDelegate() != object.getQualifiedDelegate()) {
                            if(object.getDelegate().objIsInUnitOfWork()) {
                                object.getDelegate().objRemoveFromUnitOfWork();
                            }
                        }
                        boolean carryOn = false;
                        for(
                            Iterator j = object.allStates(null, null).iterator();
                            j.hasNext();
                        ){
                            Object_1_0 state = (Object_1_0) j.next();
                            if(state.objIsDeleted()) {
                                state.objRemove();
                            } else if (state.objIsPersistent()) {
                                if(state.objIsDirty()) {
                                    if(state.objGetValue(State_1_Attributes.INVALIDATED_AT) != null) {
                                        if(state.objIsNew()) {
                                            state.objRemove();
                                        } else {
                                            ((Object_1_3)state).objMakeClean();
                                        }
                                        Path statePath = state.objGetPath();
                                        if(state.objIsNew() && statePath.equals(object.objGetPath())) {
                                            statePath.remove(statePath.size() - 1);
                                            statePath.add(PathComponent.createPlaceHolder());
                                            carryOn = true;
                                        }
                                    } else {
                                        Path objectPath = object.objGetPath();
                                        Path statePath = state.objGetPath();
    //                                  state.objSetValue('$' + State_1_Attributes.INVALIDATED_AT, null);
    //                                  state.objSetValue('$' + SystemAttributes.CREATED_AT, null);
    //                                  if(!state.objIsNew()) {
    //                                      state.objSetValue('$' + State_1_Attributes.STATE_VALID_FROM, null);
    //                                      state.objSetValue('$' + State_1_Attributes.STATE_VALID_TO, null);
    //                                  }
                                        if(!state.objIsDirty()) {
                                            if(state.objIsInUnitOfWork()) {
                                                state.objRemoveFromUnitOfWork();
                                            }
                                        } else if (carryOn) {
                                            //
                                            // The initial state must be created without state qualifier.
                                            //
                                            statePath.setTo(objectPath);
                                            object.setDelegate(state);
                                            carryOn = false;
                                        } else if (!statePath.equals(objectPath)){
                                            //
                                            // Allow the explorer to detect the dependency of later states
                                            // on their initial state.
                                            //
                                            state.objSetValue(State_1_Attributes.STATED_OBJECT, objectPath);
                                        }
                                        if(state.objIsDeleted()) {
                                            state.objRemove();
                                        }
                                    }
                                } else {
                                    if(state.objIsInUnitOfWork()) {
                                        state.objRemoveFromUnitOfWork();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The afterCompletion method notifies a a provider or plug-in that a
     * unit of work commit protocol has completed, and tells the instance
     * whether the unit of work has been committed or rolled back.
     */
    public void afterCompletion(
        boolean committed
    ){
        for(
            Iterator h = this.holders.values().iterator();
            h.hasNext();
        ){
            for(
                Iterator i = ((Map)h.next()).values().iterator();
                i.hasNext();
            ){
                SinkObject_1 object = (SinkObject_1) i.next();
                object.afterCompletion(committed);
            }
        }
        this.delegate.clear();
    }

    boolean containsInstancesOfDateState(
        String objectClass,
        String feature
    ) throws ServiceException {
        return this.model.isSubtypeOf(
            this.model.getFeatureDef(
                this.model.getElement(objectClass), // classifierDef
                feature,
                false // includeSubTypes
            ).values(
                "type"
            ).get(
                0
            ),
            DATE_STATE_CLASS_NAME
        );
    }

    boolean isInstanceOfDateState(
        Object_1_0 object
    ) throws ServiceException {
        return this.model.isInstanceof(
            object,
            DATE_STATE_CLASS_NAME
        );
    }

    boolean isAssigneableToDateState(
        String objectClass
    ) throws ServiceException {
        return this.model.isSubtypeOf(
            objectClass,
            DATE_STATE_CLASS_NAME
        );
    }

    Collection getSinkObjects(
        Path referenceFilter
    ) {
        return getContainer(
            referenceFilter, 
            false // optional
        ).values();
    }

    Model_1_0 getModel(
    ){
        return this.model;
    }


    //------------------------------------------------------------------------
    // Class UnitOfWork_1
    //------------------------------------------------------------------------

    class UnitOfWork implements UnitOfWork_1_1 {

        private UnitOfWork_1_0 delegate;

        /**
         * The begin() or first afterBegin() time point
         */
        private Date dateTime = null;

        /**
         * Tells whether afterBegin() should reset the time.
         */
        private boolean resetTime = true;
        
        /**
         * Constructor
         *
         * @param delegate
         */
        UnitOfWork(UnitOfWork_1_0 delegate) {
            this.delegate = delegate;
        }

        /**
         * Retrieve the Unit Of Work's start time
         * 
         * @return the Unit Of Work's start time
         */
        public Date getDateTime(
        ) throws ServiceException {
            if(this.dateTime == null) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("active", false)
                },
                "No unit of work active"
            );
            return this.dateTime;
        }
        
        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.Synchronization_1_0#afterBegin()
         */
        public void afterBegin(
        ) throws ServiceException {
            this.delegate.afterBegin();
            if(this.resetTime) {
                this.dateTime = new Date();
                this.resetTime = false;
            }
            SinkConnection_1.this.afterBegin();
        }

        /**
         * @param committed
         * @throws ServiceException
         * @see org.openmdx.base.transaction.Synchronization_1_0#afterCompletion(boolean)
         */
        public void afterCompletion(
            boolean committed
        ) throws ServiceException {
            SinkConnection_1.this.afterCompletion(committed);
            this.delegate.afterCompletion(committed);
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.Synchronization_1_0#beforeCompletion()
         */
        public void beforeCompletion(
        ) throws ServiceException {
            SinkConnection_1.this.beforeCompletion();
            this.resetTime = true;
            this.delegate.beforeCompletion();
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#begin()
         */
        public void begin(
        ) throws ServiceException {
            this.delegate.begin();
            this.dateTime = new Date();
            SinkConnection_1.this.afterBegin();
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#commit()
         */
        public void commit(
        ) throws ServiceException {
            SinkConnection_1.this.beforeCompletion();
            boolean committed = false;
            try {
                this.delegate.commit();
                committed = true;
            } finally {
                this.dateTime = null;
                SinkConnection_1.this.afterCompletion(committed);
            }
        }

        /**
         * @return
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isActive()
         */
        public boolean isActive() {
            return this.delegate.isActive();
        }

        /**
         * @return
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isOptimistic()
         */
        public boolean isOptimistic() {
            return this.delegate.isOptimistic();
        }

        /**
         * @return
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isTransactional()
         */
        public boolean isTransactional() {
            return this.delegate.isTransactional();
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#rollback()
         */
        public void rollback(
        ) throws ServiceException {
            try {
                this.delegate.rollback();
            } finally {
                SinkConnection_1.this.afterCompletion(false);
            }
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#verify()
         */
        public void verify(
        ) throws ServiceException {
            this.delegate.verify();
        }

    }

}
