/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractState_1.java,v 1.20 2009/05/23 10:14:16 wfro Exp $
 * Description: asic State Plug-In
 * Revision:    $Revision: 1.20 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/23 10:14:16 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.state2.aop1;


import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.LargeObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.accessor.view.PlugIn_1;
import org.openmdx.base.aop1.Removable_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.state2.cci.StateContext;
import org.openmdx.state2.cci.ViewKind;
import org.openmdx.state2.spi.StateViewContext;

/**
 * Basic State Plug-In
 */
public abstract class AbstractState_1<C extends StateContext<?>> 
    extends PlugIn_1
    implements Involved<DataObject_1_0> {

    /**
     * Constructor 
     *
     * @param self the plug-in holder
     * @throws ServiceException
     */
    protected AbstractState_1(
        ObjectView_1_0 self,
        boolean attachCore
    ) throws ServiceException{
        super(self);
        this.self = self;
        DataObject_1_0 delegate = self.objGetDelegate();
        if(attachCore) {
            DataObject_1_0 core = (DataObject_1_0) delegate.objGetValue("core");
            this.enabled = !isValidTimeUnique(core);
            this.view = this.enabled && delegate.objIsContained();
            if(this.view && core != null) {
                self.objSetDelegate(
                    delegate = core
                );
            }
        } else {
            this.enabled = !isValidTimeUnique(delegate);
            this.view = this.enabled && delegate.objIsContained();
        }
        if(!delegate.jdoIsPersistent()) {
            propagateValidTime();
        }
    }

    /**
     * The plug-in holder
     */
    protected final ObjectView_1_0 self;
    
    /**
     * View Cache
     */
    private ConcurrentMap<String,Object> views = new ConcurrentHashMap<String,Object>();

    /**
     * Tells whether it's a state view or a transient state.
     */
    private boolean view;

    /**
     * Tells whether the valid time is unique or not
     */
    private boolean enabled;
    
    /**
     * Tells whether the object is not stated despite its corresponding model features.
     * 
     * @param core
     * 
     * @return <code>true</code> if the object is not stated
     * @throws ServiceException
     */
    private static boolean isValidTimeUnique(
        DataObject_1_0 core
    ) throws ServiceException{
        return core != null && Boolean.TRUE.equals(
            core.objGetValue("validTimeUnique")
        );
    }
    
    /**
     * Tests whether a given state is involved in the given context
     * 
     * @param candidate
     * @param context
     * @param includeRemoved
     * 
     * @return <code>true</code> if the candidate is involved
     * 
     * @throws ServiceException
     */
    protected boolean isInvolved(
        DataObject_1_0 candidate, 
        C context, 
        boolean removed
    ) throws ServiceException {
        if(!candidate.jdoIsDeleted() && getModel().isInstanceof(candidate, getStateClass())) {
            //
            // Transaction Time Test
            // 
            if(
                context.getViewKind() == ViewKind.TIME_POINT_VIEW &&
                context.getExistsAt() != null
            ) {
                return  
                    candidate.jdoIsPersistent() 
                    && !candidate.jdoIsNew()
                    && StateViewContext.compareTransactionTime(
                        context.getExistsAt(),
                        (Date)candidate.objGetValue(SystemAttributes.CREATED_AT),
                        (Date)candidate.objGetValue(SystemAttributes.REMOVED_AT)
                    );
            } else {
                Object removedAt = candidate.objGetValue(SystemAttributes.REMOVED_AT);
                return removed ? Removable_1.IN_THE_FUTURE.equals(removedAt) : removedAt == null;
            }
        } else {
            return false;
        }
    }

    protected abstract void propagateValidTime(
    ) throws ServiceException;
    
    protected abstract String getStateClass(
    );
    
    protected abstract boolean isValidTimeFeature(
        String featureName
    );
    
    /**
     * Clone or split state
     * 
     * @param source
     * @throws ServiceException
     */
    protected abstract void enableUpdate(
        Map<DataObject_1_0,BoundaryCrossing> source
    ) throws ServiceException;

    /**
     * Tells whether the state starts before the time range's start point or
     * ends after the time range's end point.
     * 
     * @param state
     * 
     * @return <code>true</code> if the state crosses at least one of the 
     * time range limits.
     */
    protected abstract BoundaryCrossing getBoundaryCrossing(
        DataObject_1_0 state
    ) throws ServiceException;

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.AbstractState_1#getStates()
     */
    protected Map<String, DataObject_1_0> getStates(
    ) throws ServiceException {
        return self.objGetDelegate().getAspect(getStateClass());
    }

    /**
     * Retrieve the view's context
     * 
     * @return the view's context
     */
    @SuppressWarnings("unchecked")
    protected final C getContext(
    ) throws ServiceException {
        return (C) this.self.getInteractionSpec();
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.Involved#getInvolved(org.openmdx.state2.plugin.AccessMode)
     */
    public Iterable<DataObject_1_0> getInvolved(
        final AccessMode accessMode
    ) throws ServiceException {
        return this.view ? new Iterable<DataObject_1_0>(){

            public Iterator<DataObject_1_0> iterator() {
                return stateIterator(accessMode);
            }
            
        } : Collections.singleton(
            this.self.objGetDelegate()
        );
        
    }

    /**
     * Create a place holder
     * 
     * @return a new place holder
     */
    protected String newPlaceHolder (){
        return PathComponent.createPlaceHolder().toString();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.BasicState_1#stateIterator(org.openmdx.state2.plugin.AccessMode)
     */
    protected Iterator<DataObject_1_0> stateIterator(
        AccessMode access
    ) {
        try {
            Collection<DataObject_1_0> states = getStates().values();
            if(AccessMode.FOR_UPDATE == access) {
                C context = getContext();
                switch(getContext().getViewKind()) {
                    case TIME_POINT_VIEW :
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "A time-point view is read-only",
                            new BasicException.Parameter("xri", this.self.jdoGetObjectId()),
                            new BasicException.Parameter("viewKind", ViewKind.TIME_POINT_VIEW),
                            new BasicException.Parameter("access", access)
                        );
                    case TIME_RANGE_VIEW: 
                        Map<DataObject_1_0,BoundaryCrossing> pending = new HashMap<DataObject_1_0,BoundaryCrossing>();
                        for(DataObject_1_0 state : states){
                            //
                            // Enable Updates
                            //
                            if(isInvolved(state, context, false)) {
                                BoundaryCrossing boundaryCrossing = getBoundaryCrossing(state);
                                if(!state.jdoIsNew() || boundaryCrossing != BoundaryCrossing.NONE) {
                                    pending.put(state, boundaryCrossing);
                                }
                            }
                        }
                        if(!pending.isEmpty()) {
                            enableUpdate(
                                pending
                            );
                        }
                        break;
                }
            }
            return new InvolvedStates(states, access == null);
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    
    //------------------------------------------------------------------------
    // Implements Object_1_0
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetLargeObject(java.lang.String)
     */
    public LargeObject_1_0 objGetLargeObject(
        String feature
    ) throws ServiceException {
        if(this.enabled) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Large objects must not be stated",
                new BasicException.Parameter("xri", this.jdoGetObjectId()),
                new BasicException.Parameter("feature",feature)
            );
        } else {
            return super.objGetLargeObject(feature);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetList(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<Object> objGetList(
        String feature
    ) throws ServiceException {
        if(this.enabled) {
            List<Object> reply = (List<Object>) this.views.get(feature);
            if(reply == null) {
                List<Object> concurrent = (List<Object>) this.views.putIfAbsent(
                    feature,
                    reply = ListView.newObjectList(this, feature)
                );
                if(concurrent != null) {
                    reply = concurrent;
                }
            }
            return reply;
        } else {
            return super.objGetList(feature);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetSet(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        if(this.enabled) {
            Set<Object> reply = (Set<Object>) this.views.get(feature);
            if(reply == null) {
                Set<Object> concurrent = (Set<Object>) this.views.putIfAbsent(
                    feature,
                    reply = SetView.newObjectSet(this, feature)
                );
                if(concurrent != null) {
                    reply = concurrent;
                }
            }
            return reply;
        } else {
            return super.objGetSet(feature);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetSparseArray(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public SortedMap<Integer, Object> objGetSparseArray(
        String feature
    ) throws ServiceException {
        if(this.enabled){
            SortedMap<Integer, Object> reply = (SortedMap<Integer, Object>) this.views.get(feature);
            if(reply == null) {
                SortedMap<Integer, Object> concurrent = (SortedMap<Integer, Object>) this.views.putIfAbsent(
                    feature,
                    reply = MapView.newObjectMap(this, feature)
                );
                if(concurrent != null) {
                    reply = concurrent;
                }
            }
            return reply;
        } else {
            return super.objGetSparseArray(feature);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetValue(java.lang.String)
     */
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        if(this.enabled) {
            if("core".equals(feature)) {
                DataObject_1_0 state = this.self.objGetDelegate();
                return view ? state : state.objGetValue(feature); 
            } else {
                UniqueValue<Object> reply = new UniqueValue<Object>();
                for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                    reply.set(state.objGetValue(feature));
                }
                return reply.get();
            }
        } else {
            return super.objGetValue(feature);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objSetValue(java.lang.String, java.lang.Object)
     */
    public void objSetValue(
        String feature, 
        Object to
    ) throws ServiceException {
        if("core".equals(feature)) {
            DataObject_1_0 dataObject = this.self.objGetDelegate(); 
            DataObject_1_0 coreObject = (DataObject_1_0) to;
            dataObject.objSetValue(feature, coreObject);
            this.enabled = !isValidTimeUnique(coreObject);
            this.view = this.enabled && this.self.getInteractionSpec() instanceof StateContext<?>;
            if(this.view) {
                this.self.objSetDelegate(coreObject);
            }
            if(this.enabled) {
                getStates().put(
                    newPlaceHolder(), 
                    dataObject
                );
            }
        } else if (this.enabled) {
            if(isValidTimeFeature(feature)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "The valid time of a persistent state can't be modified",
                    new BasicException.Parameter("xri", this.jdoGetObjectId()),
                    new BasicException.Parameter("feature", feature),
                    new BasicException.Parameter("value", to)
                );
            } else {
                for(DataObject_1_0 state : getInvolved(AccessMode.FOR_UPDATE)){
                    state.objSetValue(feature, to);
                }
            }
        } else {
            super.objSetValue(feature, to);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objMove(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    public void objMove(
        Container_1_0 there, 
        String criteria
    ) throws ServiceException {
        if(this.enabled) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Aspects made persistent with their core object",
                new BasicException.Parameter("xri", this.jdoGetObjectId()),
                new BasicException.Parameter("criteria", criteria)
            );
        } else {
            super.objMove(there, criteria);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objDefaultFetchGroup()
     */
    public Set<String> objDefaultFetchGroup(
    ) throws ServiceException {
        if(this.enabled){
            Set<String> defaultFetchGroup = null;
            for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                if(defaultFetchGroup == null) {
                    defaultFetchGroup = state.objDefaultFetchGroup();
                } else {
                    defaultFetchGroup.retainAll(state.objDefaultFetchGroup());
                }
            }
            return defaultFetchGroup == null ? new HashSet<String>() : defaultFetchGroup;
        } else {
            return super.objDefaultFetchGroup();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objRemove()
     */
    public void objDelete(
    ) throws ServiceException {
        if(this.enabled) {
            for(
                Iterator<DataObject_1_0> i = getInvolved(AccessMode.FOR_UPDATE).iterator();
                i.hasNext();
            ){
                i.next();
                i.remove();
            }
        } else {
            super.objDelete();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.view.ObjectView_1_0#getInteractionSpec()
     */
    public InteractionSpec getInteractionSpec() {
        throw new UnsupportedOperationException("Operation not supported for states");        
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.view.ObjectView_1_0#getMarshaller()
     */
    public Marshaller getMarshaller() {
        throw new UnsupportedOperationException("Operation not supported for states");        
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.view.ObjectView_1_0#objGetDelegate()
     */
    public DataObject_1_0 objGetDelegate() {
        throw new UnsupportedOperationException("Operation not supported for states");        
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.view.ObjectView_1_0#objSetDelegate(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    public void objSetDelegate(DataObject_1_0 delegate) {
        throw new UnsupportedOperationException("Operation not supported for states");    
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objAddToUnitOfWork()
     */
    public void objMakeTransactional(
    ) throws ServiceException {
        if(this.enabled) {
            for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                JDOHelper.getPersistenceManager(state).makeTransactional(state);
            }
        } else {
            super.objMakeTransactional();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objRemoveFromUnitOfWork()
     */
    public void objMakeNontransactional(
    ) throws ServiceException {
        if(this.enabled) {
            for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                JDOHelper.getPersistenceManager(state).makeNontransactional(state);
            }
        } else {
            super.objMakeNontransactional();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objRefresh()
     */
    public void objRefresh(
    ) throws ServiceException {
        if(this.enabled) {
            for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                JDOHelper.getPersistenceManager(state).refresh(state);
            }
        } else { 
            super.objRefresh();
        }
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.view.PlugIn_1#objIsRemoved()
     */
    @Override
    public boolean objIsRemoved(
    ) throws ServiceException {
        if(this.enabled) {
            return getInvolved(null).iterator().hasNext();
        } else {
            return super.objIsRemoved();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objIsDeleted()
     */
    public boolean jdoIsDeleted(
    ) {
        if(this.enabled) {
            try {
                return !getInvolved(AccessMode.FOR_QUERY).iterator().hasNext();
            } catch(Exception e) {
                throw new JDOUserException(
                    "Unable to get object state",
                    e,
                    this.self
                );
            }
        } else {
            return super.jdoIsDeleted();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
    public void jdoCopyFields(Object other, int[] fieldNumbers) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            super.jdoCopyFields(other, fieldNumbers);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
     */
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            super.jdoCopyKeyFieldsFromObjectId(fm, oid);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(Object oid) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            super.jdoCopyKeyFieldsToObjectId(oid);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            super.jdoCopyKeyFieldsToObjectId(fm, oid);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
     */
    public Path jdoGetTransactionalObjectId() {
        if(this.enabled) {
            return this.self.jdoGetTransactionalObjectId();
        } else {
            return super.jdoGetTransactionalObjectId();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    public Object jdoGetVersion() {
        if(this.enabled) {
            return this.self.jdoGetVersion();
        } else {
            return super.jdoGetVersion();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    public boolean jdoIsDetached() {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            return super.jdoIsDetached();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    public void jdoMakeDirty(String fieldName) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            super.jdoMakeDirty(fieldName);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");
        } else {
            return  super.jdoNewInstance(sm);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            return super.jdoNewInstance(sm, oid);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    public Object jdoNewObjectIdInstance() {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            return super.jdoNewObjectIdInstance();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    public Object jdoNewObjectIdInstance(Object o) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            return super.jdoNewObjectIdInstance(o);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    public void jdoProvideField(int fieldNumber) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            super.jdoProvideField(fieldNumber);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    public void jdoProvideFields(int[] fieldNumbers) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            super.jdoProvideFields(fieldNumbers);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    public void jdoReplaceField(int fieldNumber) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            super.jdoReplaceField(fieldNumber);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    public void jdoReplaceFields(int[] fieldNumbers) {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            super.jdoReplaceFields(fieldNumbers);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    public void jdoReplaceFlags() {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            super.jdoReplaceFlags();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    public void jdoReplaceStateManager(StateManager sm)
        throws SecurityException {
        if(this.enabled) {
            throw new UnsupportedOperationException("Operation not supported for states");        
        } else {
            super.jdoReplaceStateManager(sm);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddEventListener(java.lang.String, java.util.EventListener)
     */
    public void objAddEventListener(
        EventListener listener
    ) throws ServiceException {
        if(this.enabled) {
            this.self.objGetDelegate().objAddEventListener(listener);
        } 
        else {
            super.objAddEventListener(listener);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetClass()
     */
    public String objGetClass(
    ) throws ServiceException {
        if(this.enabled) {
            UniqueValue<String> reply = new UniqueValue<String>();
            for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                reply.set(state.objGetClass());
            }
            return reply.isEmpty() ? null : reply.get();
        } else {
            return super.objGetClass();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetEventListeners(java.lang.String, java.lang.Class)
     */
    public <T extends EventListener> T[] objGetEventListeners(
        Class<T> listenerType
    ) throws ServiceException {
        if(this.enabled) {
            throw new UnsupportedOperationException("This operation must not be applied to state views");
        } else {
            return super.objGetEventListeners(listenerType);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveEventListener(java.lang.String, java.util.EventListener)
     */
    public void objRemoveEventListener(
        EventListener listener
    ) throws ServiceException {
        if(this.enabled) {
            throw new UnsupportedOperationException("This operation must not be applied to state views");            
        } else {
            super.objRemoveEventListener(listener);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objClone(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    public DataObject_1_0 openmdxjdoClone(
    ) {
        if(this.enabled) {
            throw new UnsupportedOperationException("This operation must not be applied to state views");
        } else {
            return super.openmdxjdoClone();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objGetPath()
     */
    public Path jdoGetObjectId(
    ) {
        if(this.enabled) {
            return this.self.jdoGetObjectId();
        } else {
            return super.jdoGetObjectId();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsDirty()
     */
    public boolean jdoIsDirty(
    ) {
        if(this.enabled) {
            try {
                boolean dirty = false;
                for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                    dirty |= state.jdoIsDirty();
                }
                return dirty;
            }
            catch(Exception e) {
                throw new JDOUserException(
                    "Unable to get object state",
                    e,
                    this.self
                );
            }            
        } else {
            return super.jdoIsDirty();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsInUnitOfWork()
     */
    public boolean jdoIsTransactional(
    ) {
        if(this.enabled) {
            try {
                boolean inUnitOfWork = false;
                for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                    inUnitOfWork |= state.jdoIsTransactional();
                }
                return inUnitOfWork;
            }
            catch(Exception e) {
                throw new JDOUserException(
                    "Unable to get object state",
                    e,
                    this.self
                );
            }            
        } else {
            return super.jdoIsTransactional();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsNew()
     */
    public boolean jdoIsNew(
    ) {
        if(this.enabled) {
            try {
                return self.objGetDelegate().jdoIsNew();
            }
            catch(Exception e) {
                throw new JDOUserException(
                    "Unable to get object state",
                    e,
                    this.self
                );
            }            
        } else {
            return super.jdoIsNew();
        }
    }

    public boolean objIsContained(
    ) throws ServiceException {
        if(this.enabled) {
            return self.objGetDelegate().objIsContained();
        } else {
            return super.objIsContained();
        }
    }

    public ServiceException getInaccessibilityReason(
    ) throws ServiceException {
        if(this.enabled) {
            return self.objGetDelegate().getInaccessibilityReason();
        } else {
            return super.getInaccessibilityReason();
        }
    }

    public boolean objIsInaccessible(
    ) throws ServiceException {
        if(this.enabled) {
            return self.objGetDelegate().objIsInaccessible();
        } else {
            return super.objIsInaccessible();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsPersistent()
     */
    public boolean jdoIsPersistent(
    ) {
        if(this.enabled) {
            try {
                return self.objGetDelegate().jdoIsPersistent();
            } catch(Exception e) {
                throw new JDOUserException(
                    "Unable to get object state",
                    e,
                    this.self
                );
            }            
        } else {
            return super.jdoIsPersistent();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetContainer(java.lang.String)
     */
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException {
        if(this.enabled) {
            return self.objGetDelegate().objGetContainer(feature);
            
        } else {
            return super.objGetContainer(feature);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    @Override
    public boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException {
        if(this.enabled) {
            throw new NotSupportedException("This operation must not be applied to state views");
        } else {
            return super.execute(ispec, input, output);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getAspect(java.lang.String)
     */
    public Map<String, DataObject_1_0> getAspect(
        String aspectClass
    ) throws ServiceException {
        return this.self.objGetDelegate().getAspect(aspectClass);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    public PersistenceManager jdoGetPersistenceManager(
    ) {
        return this.self.jdoGetPersistenceManager();
    }
    
    
    //------------------------------------------------------------------------
    // Class InvolvedStates
    //------------------------------------------------------------------------    

    /**
     * Involved States
     */
    final class InvolvedStates implements Iterator<DataObject_1_0> {

        /**
         * Constructor 
         *
         * @param states
         * @param removed 
         * 
         * @throws ServiceException
         */
        InvolvedStates(
            Collection<DataObject_1_0> states, 
            boolean removed
        ) throws ServiceException{
            this.candidates = states.iterator();
            this.removed = removed;
        }

        /**
         * 
         */
        private final boolean removed;
        
        /**
         * 
         */
        private final Iterator<DataObject_1_0> candidates;
        
        /**
         * 
         */
        private DataObject_1_0 nextInvolved = null;

        /**
         * 
         */
        private DataObject_1_0 lastInvolved = null;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext(
        ) {
            try {
                C context = getContext();
                while(
                    this.nextInvolved == null && 
                    this.candidates.hasNext()
                ) {
                    DataObject_1_0 candidate = this.candidates.next();
                    if(isInvolved(candidate, context, this.removed)) {
                        this.nextInvolved = candidate;
                    }
                }
                return this.nextInvolved != null;
            } catch(Exception e) {
                throw new RuntimeServiceException(e);
            }                
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public DataObject_1_0 next() {
            if(hasNext()) {
                this.lastInvolved = this.nextInvolved;
                this.nextInvolved = null;
                return this.lastInvolved;
            } else {
                throw new NoSuchElementException("There is no next element");
            }
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove(
        ) {
            if(this.lastInvolved == null) {
                throw new IllegalStateException("No current element");
            }
            try {
                if(this.lastInvolved.jdoIsPersistent()) {
                    JDOHelper.getPersistenceManager(this.lastInvolved).deletePersistent(this.lastInvolved);
                } else {
                    this.candidates.remove();
                }
            } 
            catch (Exception exception) {
                throw new RuntimeServiceException(exception);
            } 
            finally {
                this.lastInvolved = null;
            }
        }
        
    }

    /**
     * Merge similar adjacent states
     */
    protected abstract void reduceStates(
    ) throws ServiceException;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.view.PlugIn_1#jdoPreStore()
     */
    @Override
    public void jdoPreStore() {
        try {
            reduceStates();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
        super.jdoPreStore();
    }

}