/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractState_1.java,v 1.1 2008/12/15 03:15:36 hburger Exp $
 * Description: asic State Plug-In
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:36 $
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
package org.openmdx.state2.aop2.core;

import java.util.Collection;
import java.util.Collections;
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

import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.accessor.generic.spi.Object_1_5;
import org.openmdx.base.accessor.generic.spi.Object_1_6;
import org.openmdx.base.aop2.core.Aspect_1;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.naming.PathComponent;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.state2.cci.StateContext;
import org.openmdx.state2.cci.ViewKind;

/**
 * Basic State Plug-In
 */
public abstract class AbstractState_1<C extends StateContext<?>> 
    implements Object_1_5, Involved<Object_1_0>
{

    /**
     * Constructor 
     *
     * @param self the plug-in holder
     * @throws ServiceException
     */
    protected AbstractState_1(
        Object_1_6 self
    ) throws ServiceException{
        this.self = self;
        this.view = self.objGetDelegate().objIsPersistent() && self.getInteractionSpec() instanceof StateContext;
    }

    /**
     * The plug-in holder
     */
    protected final Object_1_6 self;
    
    /**
     * Error message for operations not supported by state view
     */
    protected static final String NOT_SUPPORTED = "This operation must not be applied to state views";
    
    /**
     * org::openmdx::state2::BasicState's MOF id
     */
    public final static String CLASS = "org:openmdx:state2:BasicState";

    /**
     * View Cache
     */
    private ConcurrentMap<String,Object> views = new ConcurrentHashMap<String,Object>();

    /**
     * Tells whether it's a state view or a transient state.
     */
    private boolean view;

    /**
     * Tests whether a given state is involved in the given context
     * 
     * @param state
     * @param context 
     * 
     * @return <code>true</code> if the state is involved
     */
    protected abstract boolean isInvolved(
        Object_1_0 state, 
        C context
    ) throws ServiceException;
    
    /**
     * Retrieve the underlying states
     * 
     * @return the underlying states
     * 
     * @throws ServiceException
     */
    protected final Map<String,Object_1_0> getStates(
    ) throws ServiceException {
        return this.self.getAspect(CLASS);
    }
            
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
        Map<Object_1_0,BoundaryCrossing> source
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
        Object_1_0 state
    ) throws ServiceException;

    /**
     * Derive the interaction spec from the data object
     * 
     * @param dataObject
     * 
     * @return the derived interaction spec
     * @throws ServiceException
     */
    protected abstract StateContext<?> getInteractionSpec(
        Object_1_0 dataObject
    ) throws ServiceException;
    
    /**
     * Retrieve the view's context
     * 
     * @return the view's context
     */
    @SuppressWarnings("unchecked")
    protected final C getContext(){
        return (C) this.self.getInteractionSpec();
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.Involved#getInvolved(org.openmdx.state2.plugin.AccessMode)
     */
    public Iterable<Object_1_0> getInvolved(
        final AccessMode accessMode
    ){
        return this.view ? new Iterable<Object_1_0>(){

            public Iterator<Object_1_0> iterator() {
                return stateIterator(accessMode);
            }
            
        } : Collections.singleton(
            (Object_1_0)this.self.objGetDelegate()
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
    protected Iterator<Object_1_0> stateIterator(AccessMode access) {
        try {
            Map<String,Object_1_0> states = getStates();
            if(AccessMode.FOR_UPDATE == access) {
                C context = getContext();
                switch(getContext().getViewKind()) {
                    case TIME_POINT_VIEW :
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            "A time-point view is read-only",
                            new BasicException.Parameter("xri", this.self.objGetResourceIdentifier()),
                            new BasicException.Parameter("viewKind", ViewKind.TIME_POINT_VIEW),
                            new BasicException.Parameter("access", access)
                        );
                    case TIME_RANGE_VIEW: 
                        Map<Object_1_0,BoundaryCrossing> pending = new HashMap<Object_1_0,BoundaryCrossing>();
                        for(
                            Iterator<Map.Entry<String,Object_1_0>> i = states.entrySet().iterator();
                            i.hasNext();
                        ){
                            Map.Entry<String,Object_1_0> e = i.next();
                            //
                            // Enable Updates
                            //
                            Object_1_0 state = e.getValue();
                            if(isInvolved(state, context)) {
                                BoundaryCrossing boundaryCrossing = getBoundaryCrossing(state);
                                if(!state.objIsNew() || boundaryCrossing != BoundaryCrossing.NONE) {
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
            return new InvolvedStates(states.values());
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
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "Large objects must not be stated",
            new BasicException.Parameter("xri", objGetResourceIdentifier()),
            new BasicException.Parameter("feature",feature)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetIterable(java.lang.String)
     */
    public Iterable<?> objGetIterable(String featureName) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetList(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<Object> objGetList(
        String feature
    ) throws ServiceException {
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
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetSet(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
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
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetSparseArray(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public SortedMap<Integer, Object> objGetSparseArray(
        String feature
    ) throws ServiceException {
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
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetValue(java.lang.String)
     */
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        if(Aspect_1.CORE.equals(feature)) {
            Object_1_5 state = this.self.objGetDelegate();
            return view ? state : state.objGetValue(feature); 
        } else {
            UniqueValue<Object> reply = new UniqueValue<Object>();
            for(Object_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                reply.set(state.objGetValue(feature));
            }
            return reply.get();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objSetValue(java.lang.String, java.lang.Object)
     */
    public void objSetValue(
        String feature, 
        Object to
    ) throws ServiceException {
        if(Aspect_1.CORE.equals(feature)) {
            Object_1_5 state = this.self.objGetDelegate(); 
            state.objSetValue(feature, to);
            getStates().put(
                newPlaceHolder(), 
                state
            );
            if(this.view = this.self.getInteractionSpec() instanceof StateContext) {
                this.self.objSetDelegate((Object_1_5) to);
            }
        } else if (isValidTimeFeature(feature)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "The valid time of a persistent state can't be modified",
                new BasicException.Parameter("xri", objGetResourceIdentifier()),
                new BasicException.Parameter("feature", feature),
                new BasicException.Parameter("value", to)
            );
        } else {
            for(Object_1_0 state : getInvolved(AccessMode.FOR_UPDATE)){
                state.objSetValue(feature, to);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objMove(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    public void objMove(
        FilterableMap<String, Object_1_0> there, 
        String criteria
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "Aspects made persistent with their core object",
            new BasicException.Parameter("xri", objGetResourceIdentifier()),
            new BasicException.Parameter("criteria", criteria)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objDefaultFetchGroup()
     */
    public Set<String> objDefaultFetchGroup(
    ) throws ServiceException {
        Set<String> defaultFetchGroup = null;
        for(Object_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
            if(defaultFetchGroup == null) {
                defaultFetchGroup = state.objDefaultFetchGroup();
            } else {
                defaultFetchGroup.retainAll(state.objDefaultFetchGroup());
            }
        }
        return defaultFetchGroup == null ? new HashSet<String>() : defaultFetchGroup;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objRemove()
     */
    public void objRemove(
    ) throws ServiceException {
        for(
            Iterator<Object_1_0> i = getInvolved(AccessMode.FOR_UPDATE).iterator();
            i.hasNext();
        ){
            i.next();
            i.remove();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objAddToUnitOfWork()
     */
    public void objAddToUnitOfWork(
    ) throws ServiceException {
        for(Object_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
            state.objAddToUnitOfWork();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objRemoveFromUnitOfWork()
     */
    public void objRemoveFromUnitOfWork(
    ) throws ServiceException {
        for(Object_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
            state.objRemoveFromUnitOfWork();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objRefresh()
     */
    public void objRefresh(
    ) throws ServiceException {
        for(Object_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
            state.objRefresh();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objIsDeleted()
     */
    public boolean objIsDeleted(
    ) throws ServiceException {
        return !getInvolved(AccessMode.FOR_QUERY).iterator().hasNext();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddEventListener(java.lang.String, java.util.EventListener)
     */
    public void objAddEventListener(
        String feature, 
        EventListener listener
    ) throws ServiceException {
        this.self.objGetDelegate().objAddEventListener(feature, listener);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetClass()
     */
    public String objGetClass(
    ) throws ServiceException {
        UniqueValue<String> reply = new UniqueValue<String>();
        for(Object_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
            reply.set(state.objGetClass());
        }
        return reply.isEmpty() ? null : reply.get();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetEventListeners(java.lang.String, java.lang.Class)
     */
    public <T extends EventListener> T[] objGetEventListeners(
        String feature,
        Class<T> listenerType
    ) throws ServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetResourceIdentifier()
     */
    public Object objGetResourceIdentifier(
    ) {
        return this.self.objGetResourceIdentifier();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objMakeVolatile()
     */
    public void objMakeVolatile(
    ) throws ServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveEventListener(java.lang.String, java.util.EventListener)
     */
    public void objRemoveEventListener(
        String feature, 
        EventListener listener
    ) throws ServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objCopy(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    public Object_1_0 objCopy(
        FilterableMap<String, Object_1_0> there,
        String criteria
    ) throws ServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objFlush()
     */
    public boolean objFlush(
    ) throws ServiceException {
        boolean flushed = true;
        for(Object_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
            flushed &= state.objFlush();
        }
        return flushed;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objGetPath()
     */
    public Path objGetPath(
    ) throws ServiceException {
        return this.self.objGetPath();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsDirty()
     */
    public boolean objIsDirty(
    ) throws ServiceException {
        boolean dirty = false;
        for(Object_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
            dirty |= state.objIsDirty();
        }
        return dirty;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsInUnitOfWork()
     */
    public boolean objIsInUnitOfWork(
    ) throws ServiceException {
        boolean inUnitOfWork = false;
        for(Object_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
            inUnitOfWork |= state.objIsInUnitOfWork();
        }
        return inUnitOfWork;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsNew()
     */
    public boolean objIsNew(
    ) throws ServiceException {
        return false; // i.e. getCore().objIsNew()
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsPersistent()
     */
    public boolean objIsPersistent(
    ) throws ServiceException {
        return false; // i.e. getCore().objIsPersistent()
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetContainer(java.lang.String)
     */
    public FilterableMap<String, Object_1_0> objGetContainer(
        String feature
    ) throws ServiceException {
        return this.self.objGetDelegate().objGetContainer(feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objInvokeOperation(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperation(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objInvokeOperationInUnitOfWork(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getAspect(java.lang.String)
     */
    public Map<String, Object_1_0> getAspect(
        String aspectClass
    ) throws ServiceException {
        return this.self.objGetDelegate().getAspect(aspectClass);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    public ObjectFactory_1_0 getFactory(
    ) throws ServiceException {
        return this.self.getFactory();
    }

    
    //------------------------------------------------------------------------
    // Class InvolvedStates
    //------------------------------------------------------------------------    

    /**
     * Involved States
     */
    final class InvolvedStates implements Iterator<Object_1_0> {

        /**
         * Constructor 
         *
         * @param states
         * 
         * @throws ServiceException
         */
        InvolvedStates(
            Collection<Object_1_0> states
        ) throws ServiceException{
            this.candidates = states.iterator();
        }

        /**
         * 
         */
        private final Iterator<Object_1_0> candidates;
        
        /**
         * 
         */
        private Object_1_0 nextInvolved = null;

        /**
         * 
         */
        private Object_1_0 lastInvolved = null;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            C context = getContext();
            while(
                this.nextInvolved == null && 
                this.candidates.hasNext()
            ) {
                Object_1_0 candidate = this.candidates.next();
                try {
                    if(isInvolved(candidate, context)) {
                        this.nextInvolved = candidate;
                    }
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }
            return this.nextInvolved != null;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object_1_0 next() {
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
                if(this.lastInvolved.objIsPersistent()) {
                    this.lastInvolved.objRemove();
                } else {
                    this.candidates.remove();
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            } finally {
                this.lastInvolved = null;
            }
        }
        
    }
       
}