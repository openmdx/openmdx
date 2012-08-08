/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: BasicState_1.java,v 1.20 2010/07/09 16:30:03 hburger Exp $
 * Description: Basic State Plug-In
 * Revision:    $Revision: 1.20 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/07/09 16:30:03 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2010, OMEX AG, Switzerland
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

import static org.openmdx.base.accessor.cci.SystemAttributes.CREATED_AT;
import static org.openmdx.base.accessor.cci.SystemAttributes.REMOVED_AT;
import static org.openmdx.base.persistence.cci.Queries.ASPECT_QUERY;
import static org.openmdx.state2.cci.ViewKind.TIME_POINT_VIEW;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.ExceptionHelper;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.aop1.Removable_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.state2.cci.StateContext;
import org.openmdx.state2.cci.ViewKind;
import org.openmdx.state2.spi.DateStateContexts;
import org.openmdx.state2.spi.Parameters;
import org.openmdx.state2.spi.StateViewContext;

/**
 * Basic State Plug-In
 */
public abstract class BasicState_1<C extends StateContext<?>> 
    extends Removable_1
    implements Involved<DataObject_1_0> 
{

    /**
     * Constructor 
     *
     * @param self the plug-in holder
     * @param next the next plug-in
     * 
     * @throws ServiceException
     */
    protected BasicState_1(
        ObjectView_1_0 self, 
        Interceptor_1 next
    ) throws ServiceException{
        super(self, next);
        DataObject_1_0 delegate = self.objGetDelegate();
        this.enabled = !getModel().isInstanceof(delegate, "org:openmdx:state2:BasicState");
        if (!this.enabled && !delegate.jdoIsPersistent() && !delegate.jdoIsTransactional()) {
            initialize(delegate);
        }
    }

    /**
     * View Cache
     */
    private ConcurrentMap<String,Object> views = new ConcurrentHashMap<String,Object>();

    /**
     * <code>true</code> for state views.
     */
    private boolean enabled = false;

    /**
     *  
     */
    private transient Map<?,?> coreFeatures;
    
    /**
     * Tells whether this instances handles a state view, a transient state or an object
     * with unique valid time.
     * 
     * @return <code>true</code> for state views.
     */
    private boolean isView(){
        return this.enabled;
    }

    /**
     * Tells whether this instances handles a state view, a transient state or an object
     * with unique valid time.
     * 
     * @param feature the feature to be tested
     * 
     * @return <code>true</code> for state view features
     * 
     * @throws ServiceException  
     */
    private boolean isViewFeature(
        String feature
    ) throws ServiceException {
        if(this.enabled) {
            if(this.coreFeatures == null) {
                this.coreFeatures = (Map<?,?>)getModel().getElement(
                    this.self.objGetDelegate().objGetClass()
                ).objGetValue("allFeature");
            }
            return !this.coreFeatures.containsKey(feature);
        } else {
            return false;
        }
    }
    
    /**
     * Tests whether a given state is involved in the given context
     * 
     * @param candidate
     * @param context
     * @param exact <code>true</code> if the state must exactly match the context
     * @param includeRemoved
     * @return <code>true</code> if the candidate is involved
     * 
     * @throws ServiceException
     */
    protected boolean isInvolved(
        DataObject_1_0 candidate, 
        C context, 
        boolean exact, 
        boolean removed
    ) throws ServiceException {
        if(!candidate.jdoIsDeleted() && getModel().isInstanceof(candidate, getStateClass())) {
            //
            // Transaction Time Test
            // 
            if(DateStateContexts.isHistoryView(context)) {
                return candidate.jdoIsPersistent() && !candidate.jdoIsNew() && StateViewContext.compareTransactionTime(
                    context.getExistsAt(),
                    (Date)candidate.objGetValue(CREATED_AT),
                    (Date)candidate.objGetValue(REMOVED_AT)
                );
            } else {
                Object removedAt = candidate.objGetValue(REMOVED_AT);
                return removed ? IN_THE_FUTURE.equals(removedAt) : removedAt == null;
            }
        } else {
            return false;
        }
    }

    
    protected abstract void initialize(
        DataObject_1_0 dataObject
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
    @SuppressWarnings("unchecked")
    protected Collection<DataObject_1_0> getStates(
    ) throws ServiceException {
        DataObject_1_0 core = self.objGetDelegate();
        return (Collection<DataObject_1_0>) core.jdoGetPersistenceManager().newNamedQuery(
            null,
            ASPECT_QUERY
        ).execute(
            getStateClass(),
            core
        );
    }

    /**
     * Retrieve the view's context
     * 
     * @return the view's context
     */
    @SuppressWarnings("unchecked")
    protected final C getContext(
    ){
        return (C) this.self.getInteractionSpec();
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop1.Involved#getQueryAccess()
     */
    @Override
    public AccessMode getQueryAccessMode() {
        return Parameters.STRICT_QUERY && getContext().getViewKind() == ViewKind.TIME_RANGE_VIEW ? 
            AccessMode.UNDERLYING_STATE : 
            AccessMode.FOR_QUERY;
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.Involved#getInvolved(org.openmdx.state2.plugin.AccessMode)
     */
    public Iterable<DataObject_1_0> getInvolved(
        final AccessMode accessMode
    ){
        return new Iterable<DataObject_1_0>(){

            public Iterator<DataObject_1_0> iterator() {
                try {
                    Collection<DataObject_1_0> states = getStates();
                    if(accessMode == null) {
                        return new InvolvedStates(states, false, true);
                    } else switch (accessMode) {
                        case UNDERLYING_STATE: 
                            return new InvolvedStates(states, true, false);
                        case FOR_QUERY: 
                            return new InvolvedStates(states, false, false);
                        case FOR_UPDATE:
                            C context = getContext();
                            switch(getContext().getViewKind()) {
                                case TIME_POINT_VIEW :
                                    throw new ServiceException(
                                        BasicException.Code.DEFAULT_DOMAIN,
                                        BasicException.Code.NOT_SUPPORTED,
                                        "A time-point view is read-only",
                                        new BasicException.Parameter("xri", BasicState_1.this.jdoGetObjectId()),
                                        new BasicException.Parameter("viewKind", TIME_POINT_VIEW),
                                        new BasicException.Parameter("access", accessMode)
                                    );
                                case TIME_RANGE_VIEW: 
                                    Map<DataObject_1_0,BoundaryCrossing> pending = new HashMap<DataObject_1_0,BoundaryCrossing>();
                                    for(DataObject_1_0 state : states){
                                        //
                                        // Enable Updates
                                        //
                                        if(isInvolved(state, context, false, false)) {
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
                            return new InvolvedStates(states, false, false);
                        default:
                            throw new RuntimeServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "Unexpected access mode",
                                new BasicException.Parameter("accessMode", accessMode)
                            );
                    }
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }

        };

    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetList(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Object> objGetList(
        String feature
    ) throws ServiceException {
        if(isViewFeature(feature)) {
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
    @Override
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        if(isViewFeature(feature)) {
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
    @Override
    public SortedMap<Integer, Object> objGetSparseArray(
        String feature
    ) throws ServiceException {
        if(isViewFeature(feature)){
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
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        if(isViewFeature(feature)) {
            if("core".equals(feature)) {
                return this.self.objGetDelegate();
            } else {
                UniqueValue<Object> reply = new UniqueValue<Object>();
                for(DataObject_1_0 state : getInvolved(this.getQueryAccessMode())){
                    reply.set(state.objGetValue(feature));
                }
                return reply.get();
            }
        } else {
            Object value = super.objGetValue(feature); 
            if(
                value == null && 
                this.jdoIsPersistent() &&
                ("validTimeUnique".equals(feature) || "transactionTimeUnique".equals(feature))
            ) {
                return Boolean.valueOf(!this.enabled);
            }
            return value;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objSetValue(java.lang.String, java.lang.Object)
     */
    @Override
    public void objSetValue(
        String feature, 
        Object to
    ) throws ServiceException {
        if (isViewFeature(feature)) {
            if("core".equals(feature)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    "The core object can't be replaced",
                    ExceptionHelper.newObjectIdParameter("id", this),
                    new BasicException.Parameter("feature", feature),
                    ExceptionHelper.newObjectIdParameter("value", to)
                );
            } else if(isValidTimeFeature(feature)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "The valid time of a persistent state can't be modified",
                    ExceptionHelper.newObjectIdParameter("id", this),
                    new BasicException.Parameter("feature", feature),
                    new BasicException.Parameter("value", to)
                );
            } else {
                for(DataObject_1_0 state : getInvolved(AccessMode.FOR_UPDATE)){
                    state.objSetValue(feature, to);
                }
            }
        } else {
            if("core".equals(feature)) {
                if(to != null) { // do nothing during refInitialize()
                    DataObject_1_0 core = (DataObject_1_0) to;
                    DataObject_1_0 delegate = this.self.objGetDelegate();
                    if(core == delegate) {
                        core.objSetValue("validTimeUnique", Boolean.TRUE);
                        core.objSetValue("transactionTimeUnique", Boolean.TRUE);
                    } else {
                        Model_1_0 model = getModel();
                        ModelElement_1_0 classifierDef = model.getElement(core.objGetClass());
                        for(String coreFeature : core.objDefaultFetchGroup()) {
                            if(!"openmdxjdoVersion".equals(coreFeature)) {
                                ModelElement_1_0 featureDef = model.getFeatureDef(
                                    classifierDef,
                                    coreFeature,
                                    true // includeSubtypes
                                );
                                if(model.isAttributeType(featureDef)) {
                                    String multiplicity = (String)featureDef.objGetValue("multiplicity");
                                    if(Multiplicities.LIST.equals(multiplicity)) {
                                        List<Object> target = delegate.objGetList(coreFeature);
                                        target.clear();
                                        target.addAll(core.objGetList(coreFeature));
                                    } else if (Multiplicities.SET.equals(multiplicity)) {
                                        Set<Object> target = delegate.objGetSet(coreFeature);
                                        target.clear();
                                        target.addAll(core.objGetSet(coreFeature));
                                    } else if (Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                                        SortedMap<Integer,Object> target = delegate.objGetSparseArray(coreFeature);
                                        target.clear();
                                        target.putAll(core.objGetSparseArray(coreFeature));
                                    } else {
                                        delegate.objSetValue(
                                            coreFeature, 
                                            core.objGetValue(coreFeature)
                                        );
                                    }
                                }
                            }
                        }
                        //
                        // Replace state delegate by core delegate
                        //
                        delegate.objSetValue(feature, core);
                        this.self.objSetDelegate(core);
                        this.enabled = true;
                    }
                }
            } else {
                super.objSetValue(feature, to);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objDefaultFetchGroup()
     */
    @Override
    public Set<String> objDefaultFetchGroup(
    ) throws ServiceException {
        if(isView()){
            Set<String> defaultFetchGroup = null;
            for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                if(defaultFetchGroup == null) {
                    defaultFetchGroup = state.objDefaultFetchGroup();
                } else {
                    defaultFetchGroup.retainAll(state.objDefaultFetchGroup());
                }
            }
            if(defaultFetchGroup != null){
                defaultFetchGroup.addAll(super.objDefaultFetchGroup());
                return defaultFetchGroup;
            }
        }
        return super.objDefaultFetchGroup();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objRemove()
     */
    @Override
    public void objDelete(
    ) throws ServiceException {
        if(isView()) {
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
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objAddToUnitOfWork()
     */
    @Override
    public void objMakeTransactional(
    ) throws ServiceException {
        if(isView()) {
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
    @Override
    public void objMakeNontransactional(
    ) throws ServiceException {
        if(isView()) {
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
    @Override
    public void objRefresh(
    ) throws ServiceException {
        if(isView()) {
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
        if(isView()) {
            return getInvolved(null).iterator().hasNext();
        } else {
            return super.objIsRemoved();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objIsDeleted()
     */
    @Override
    public boolean jdoIsDeleted(
    ) {
        if(isView()) {
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
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetClass()
     */
    @Override
    public String objGetClass(
    ) throws ServiceException {
        if(isView()) {
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
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsDirty()
     */
    @Override
    public boolean jdoIsDirty(
    ) {
        if(isView()) {
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
    @Override
    public boolean jdoIsTransactional(
    ) {
        if(isView()) {
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
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    @Override
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
         * @param exact <code>true</code> if the state must exactly match the context
         * @param removed 
         * @throws ServiceException
         */
        InvolvedStates(
            Collection<DataObject_1_0> states, 
            boolean exact, 
            boolean removed
        ) throws ServiceException{
            this.candidates = states.iterator();
            this.exact = exact;
            this.removed = removed;
        }

        /**
         * <code>true</code> if the state must exactly match the context
         */
        private final boolean exact;

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
                    if(isInvolved(candidate, context, this.exact, this.removed)) {
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