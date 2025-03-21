/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Basic State Plug-In
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import static org.openmdx.base.accessor.cci.SystemAttributes.CORE;
import static org.openmdx.base.accessor.cci.SystemAttributes.CREATED_AT;
import static org.openmdx.base.accessor.cci.SystemAttributes.REMOVED_AT;
import static org.openmdx.base.persistence.cci.Queries.ASPECT_QUERY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.JDOCanRetryException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.ExceptionHelper;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.aop1.Removable_1;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.state2.cci.StateContext;
import org.openmdx.state2.cci.ViewKind;
import org.openmdx.state2.spi.Parameters;
import org.openmdx.state2.spi.Propagation;
import org.openmdx.state2.spi.StateViewContext;
import org.openmdx.state2.spi.TechnicalAttributes;
import org.w3c.spi2.Datatypes;

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
        this.views = Maps.newMap(isMultithreaded());
        this.enabled = !getModel().isInstanceof(delegate, "org:openmdx:state2:BasicState");
        if (!this.enabled && !delegate.jdoIsPersistent() && !delegate.jdoIsTransactional()) {
            initialize(delegate);
        }
    }

    /**
     * View Cache
     */
    private final Map<String,Object> views;

    /**
     * {@code true} for state views.
     */
    private boolean enabled = false;

    /**
     *  
     */
    private transient Map<?,?> coreFeatures;

    /**
     * Lazily initalized for TIME_POINT access mode FOR_QUERY
     */
    private transient Iterable<DataObject_1_0> forTimePointQuery;

    /**
     * Lazily initalized for TIME_RANGE access mode FOR_QUERY
     */
    private transient Iterable<DataObject_1_0> forTimeRangeQuery;

    /**
     * Lazily initalized for TIME_RANGE access mode UNDERLYING_STATE
     */
    private transient Iterable<DataObject_1_0> forUnderlyingState;

    /**
     * Lazily initalized for TIME_RANGE access mode FOR_UPDATE
     */
    private transient Iterable<DataObject_1_0> forUpdate;

    /**
     * Tells whether this instances handles a state view, a transient state or an object
     * with unique valid time.
     * 
     * @param feature the feature to be tested
     * 
     * @return {@code true} for state view features
     * 
     * @throws ServiceException  
     */
    private boolean isViewFeature(
        String feature
    ) throws ServiceException {
        if(this.enabled) {
            if(this.coreFeatures == null) {
                this.coreFeatures = getModel().getElement(
                    this.self.objGetDelegate().objGetClass()
                ).objGetMap("allFeature");
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
     * @param accessMode 
     * @return {@code true} if the candidate is involved
     * 
     * @throws ServiceException
     */
    protected boolean isInvolved(
        DataObject_1_0 candidate, 
        C context, 
        AccessMode accessMode
    ) throws ServiceException {
        if(!candidate.jdoIsDeleted() && getModel().isInstanceof(candidate, "org:openmdx:state2:BasicState")) {
    		final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif removedAt = Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(REMOVED_AT);
        	switch(context.getViewKind()) {
	        	case TIME_POINT_VIEW:
	        		if(context.getExistsAt() == null) {
		        		return removedAt == null;
	        		} else {
	                    final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif createdAt = Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(CREATED_AT);
                        return 
	                    	candidate.jdoIsPersistent() && 
	                    	!candidate.jdoIsNew() &&
	                    	StateViewContext.compareTransactionTime(
	                    		context.getExistsAt(), 
	                    		createdAt, 
	                    		removedAt
	                    	);
	        		}
	        	case TIME_RANGE_VIEW:
	        		return removedAt == null;
        		default:
        			throw new RuntimeServiceException(
        				BasicException.Code.DEFAULT_DOMAIN,
        				BasicException.Code.ASSERTION_FAILURE,
        				"Unexpected view kind",
        				new BasicException.Parameter("context",context)
        			);
        	}
        } else {
            return false;
        }
    }

    
    protected abstract void initialize(
        DataObject_1_0 dataObject
    ) throws ServiceException;

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
     * @return {@code true} if the state crosses at least one of the 
     * time range limits.
     */
    protected abstract BoundaryCrossing getBoundaryCrossing(
        DataObject_1_0 state
    ) throws ServiceException;

    @SuppressWarnings("unchecked")
    protected Collection<DataObject_1_0> getStates(
        DataObject_1_0 core
    ){
        return (Collection<DataObject_1_0>) core.jdoGetPersistenceManager().newNamedQuery(
            null,
            ASPECT_QUERY
        ).execute(
            "org:openmdx:state2:BasicState",
            core
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.AbstractState_1#getStates()
     */
    protected Collection<DataObject_1_0> getStates(
    ) throws ServiceException {
        return getStates(self.objGetDelegate());
    }

    protected boolean isActive(
        DataObject_1_0 state
    ) throws ServiceException{
        return 
            !state.jdoIsDeleted() && 
            state.objGetValue(REMOVED_AT) == null;
    }

    protected boolean isNew(
        DataObject_1_0 state
    ) throws ServiceException{
        return 
            !state.jdoIsDeleted() && 
            state.jdoIsNew();
    }
    
    /**
     * Tells whether the state is to be removed when this unit of work completes
     * 
     * @param state the state to be inspected
     * @return {@code true} if the state is to be removed when this unit of work completes
     * 
     * @throws ServiceException if retrieving the {@code REMOVED_AT} value fails
     */
    protected boolean isToBeRemoved(
        DataObject_1_0 state
    ) throws ServiceException{
        return 
            !state.jdoIsDeleted() && 
            IN_THE_FUTURE.equals(state.objGetValue(REMOVED_AT));
    }
    
    protected abstract boolean interfersWith(
        DataObject_1_0 state
    ) throws ServiceException;
    
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
     * @see org.openmdx.state2.aop1.Involved#getQueryAccessMode()
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
    	ViewKind viewKind = getContext().getViewKind();
		switch(viewKind) {
	    	case TIME_RANGE_VIEW:
	    		switch(accessMode) {
		    		case FOR_QUERY:
			    		if(this.forTimeRangeQuery == null) {
			    			this.forTimeRangeQuery = new MultiStateCache(accessMode);
			    		}
			    		return this.forTimeRangeQuery;
		    		case UNDERLYING_STATE:
			    		if(this.forUnderlyingState == null) {
			    			this.forUnderlyingState = new SingleStateCache(accessMode);
			    		}
			    		return this.forUnderlyingState;
		    		case FOR_UPDATE:
			    		if(this.forUpdate == null) {
			    			this.forUpdate = new InvolvedStatesForUpdate();
			    		}
			    		return this.forUpdate;
	    		}
	    	case TIME_POINT_VIEW:
	    		switch(accessMode) {
		    		case FOR_QUERY:
			    		if(this.forTimePointQuery == null) {
			    			this.forTimePointQuery = new SingleStateCache(accessMode);
			    		}
			    		return this.forTimePointQuery;
                    default:
                        // fall through to exception
                        break;
				}
    	}
        throw new RuntimeServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "Illegal access mode for the given view kind",
            getIdParameter(),
            new BasicException.Parameter("viewKind", viewKind),
            ExceptionHelper.newObjectIdParameter("accessMode", accessMode)
        );
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
            final List<Object> existingCollection = (List<Object>) this.views.get(feature);
            if(existingCollection == null) {
                return (List<Object>) Maps.putUnlessPresent(
                    this.views,
                    feature,
                    ListView.newObjectList(this, feature)
                );
            } else {
                return existingCollection;
            }
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
            final Set<Object> existingCollection = (Set<Object>) this.views.get(feature);
            if(existingCollection == null) {
                return (Set<Object>) Maps.putUnlessPresent(
                    this.views, 
                    feature, 
                    SetView.newObjectSet(this, feature)
                );
            } else {
                return existingCollection;
            }
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
            final SortedMap<Integer, Object> existingCollection = (SortedMap<Integer, Object>) this.views.get(feature);
            if(existingCollection == null) {
                return (SortedMap<Integer, Object>) Maps.putUnlessPresent(
                    this.views,
                    feature,
                    MapView.newObjectMap(this, feature)
                );
            } else {
            	return existingCollection;
            }
        } else {
            return super.objGetSparseArray(feature);
        }
    }

    protected Boolean transactionTimeUniqueDefaultValue(){
    	return Boolean.valueOf(!this.enabled);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        if(isViewFeature(feature)) {
            if(CORE.equals(feature)) {
                return this.self.objGetDelegate();
            } else {
                UniqueValue<Object> reply = new UniqueValue<Object>();
                for(DataObject_1_0 state : getInvolved(this.getQueryAccessMode())){
                    reply.set(state.objGetValue(feature));
                }
                try {
                    return reply.get();
                } catch (ServiceException exception) {
                    throw new ServiceException(
                        exception, 
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE,
                        "The underlaying states do not allow the determination of a unique feature value",
                        ExceptionHelper.newObjectIdParameter(BasicException.Parameter.XRI, this)
                    );
                }
                
            }
        } else {
        	Object value = super.objGetValue(feature);
        	if(value == null) {
        		if(TechnicalAttributes.TRANSACTION_TIME_UNIQUE.equals(feature)) {
	        		value = transactionTimeUniqueDefaultValue();
        		}
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
            if(CORE.equals(feature)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    "The core object can't be replaced",
                    getIdParameter(),
                    new BasicException.Parameter("feature", feature),
                    ExceptionHelper.newObjectIdParameter("value", to)
                );
            } else if(isValidTimeFeature(feature)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "The valid time of a persistent state can't be modified",
                    getIdParameter(),
                    new BasicException.Parameter("feature", feature),
                    new BasicException.Parameter("value", to)
                );
            } else {
                for(DataObject_1_0 state : getInvolved(AccessMode.FOR_UPDATE)){
                    state.objSetValue(feature, to);
                }
            }
        } else {
            if(CORE.equals(feature)) {
                if(to != null) { // do nothing during refInitialize()
                    DataObject_1_0 core = (DataObject_1_0) to;
                    DataObject_1_0 delegate = this.self.objGetDelegate();
                    if(core == delegate) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "Use setValidTimeUnique(true) instead",
                            getIdParameter(),
                            new BasicException.Parameter("feature", feature),
                            new BasicException.Parameter("value", "<self>")
                        );
                    } else {
                        for(DataObject_1_0 state : this.getStates(core)) {
                            if(isActive(state) && interfersWith(state)) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.DUPLICATE,
                                    "The new state interfers with the existing ones"
                                );
                            }
                        }
                        Model_1_0 model = getModel();
                        ModelElement_1_0 classifierDef = model.getElement(core.objGetClass());
                        for(String coreFeature : core.objDefaultFetchGroup()) {
                            if(!Propagation.NON_PROPAGATED_ATTRIBUTES.contains(coreFeature)) {
                                ModelElement_1_0 featureDef = model.getFeatureDef(
                                    classifierDef,
                                    coreFeature,
                                    true // includeSubtypes
                                );
                                if(model.isAttributeType(featureDef)) {
                                	Multiplicity multiplicity = ModelHelper.getMultiplicity(featureDef);
                                    switch(multiplicity){
                                	    case OPTIONAL: case SINGLE_VALUE: {
                                            delegate.objSetValue(
                                                coreFeature, 
                                                core.objGetValue(coreFeature)
                                            );
                                	    } break;
	                                	case LIST: {
	                                        List<Object> target = delegate.objGetList(coreFeature);
	                                        target.clear();
	                                        target.addAll(core.objGetList(coreFeature));
	                                	} break;
	                                	case SET: {
	                                        Set<Object> target = delegate.objGetSet(coreFeature);
	                                        target.clear();
	                                        target.addAll(core.objGetSet(coreFeature));
	                                	} break;
	                                	case SPARSEARRAY: {
	                                        SortedMap<Integer,Object> target = delegate.objGetSparseArray(coreFeature);
	                                        target.clear();
	                                        target.putAll(core.objGetSparseArray(coreFeature));
	                                	} break;
	                                	case STREAM:
	                                	    // Streams are not replicated to their code attributes
	                                	break;	
                                        default: throw new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.NOT_SUPPORTED,
                                            "The given multiplicity is not supported for core atttributes",
                                            new BasicException.Parameter("multiplicity", multiplicity),
                                            new BasicException.Parameter("attribute", coreFeature)
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

    /**
     * Determine the Id parameter
     * 
     * @return
     */
    public BasicException.Parameter getIdParameter() {
        return ExceptionHelper.newObjectIdParameter("id", this);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objDefaultFetchGroup()
     */
    @Override
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
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objAddToUnitOfWork()
     */
    @Override
    public void objMakeTransactional(
    ) throws ServiceException {
        if(this.enabled) {
            for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                ReducedJDOHelper.getPersistenceManager(state).makeTransactional(state);
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
        if(this.enabled) {
            for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                ReducedJDOHelper.getPersistenceManager(state).makeNontransactional(state);
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
        if(this.enabled) {
            for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                ReducedJDOHelper.getPersistenceManager(state).refresh(state);
            }
        } else { 
            super.objRefresh();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objIsDeleted()
     */
    @Override
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
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetClass()
     */
    @Override
    public String objGetClass(
    ) throws ServiceException {
        if(this.enabled) {
            UniqueValue<String> reply = new UniqueValue<String>();
            for(DataObject_1_0 state : getInvolved(AccessMode.FOR_QUERY)){
                reply.set(state.objGetClass());
            }
            try {
                return reply.isEmpty() ? null : reply.get();
            } catch (ServiceException exception) {
                throw new ServiceException(
                    exception, 
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    "The underlaying states do not allow the determination of a unique object class",
                    getIdParameter()
                );
            }
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
    @Override
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
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    @Override
    public PersistenceManager jdoGetPersistenceManager(
    ) {
        return this.self.jdoGetPersistenceManager();
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
        	throw new JDOCanRetryException("State reduction failure", exception);
        }
        super.jdoPreStore();
    }

    
    /* (non-Javadoc)
	 * @see org.openmdx.base.accessor.view.Interceptor_1#jdoPreClear()
	 */
	@Override
	public void jdoPreClear() {
		super.jdoPreClear();
		this.forTimePointQuery = null;
		this.forTimeRangeQuery = null;
		this.forUnderlyingState = null;
		this.forUpdate = null;
	}

	/**
     * Retrieve the actual state version
     * 
     * @return the actual state version
     * 
     * @throws ServiceException  
     */
	int getStateVersion(
	) throws ServiceException {
		Number stateVersion = (Number)self.objGetDelegate().objGetValue(TechnicalAttributes.STATE_VERSION);
		return stateVersion == null ? Integer.MIN_VALUE : stateVersion.intValue();
	}

	/**
	 * Invalidate the given state. {@code Persistent-new} states are deleted 
	 * immediately, while {@code persistent} states will be removed at the
	 * end of the current unit of work.
	 * 
	 * @param state the state to be invalidated
	 * 
	 * @throws ServiceException
	 */
    protected void invalidate(
        DataObject_1_0 state
    ) throws ServiceException {
        if(state.jdoIsNew()) {
            ReducedJDOHelper.getPersistenceManager(state).deletePersistent(state);
        } else {
            state.objSetValue(REMOVED_AT, IN_THE_FUTURE);
        }
    }

    /**
     * Invalidate the given state. {@code Persistent-new} states are deleted 
     * immediately, while {@code persistent} states will be removed at the
     * end of the current unit of work.
     * 
     * @param state the state to be invalidated
     * 
     * @throws ServiceException
     */
    protected void reactivate(
        DataObject_1_0 state
    ) throws ServiceException {
        if(isToBeRemoved(state)) {
            state.objSetValue(REMOVED_AT, null);
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Only states to be removed at the end of the current transaction can be re-activated",
                new BasicException.Parameter(BasicException.Parameter.XRI, ReducedJDOHelper.getAnyObjectId(state)),
                new BasicException.Parameter("jdo-state", ReducedJDOHelper.getObjectState(state)),
                new BasicException.Parameter(REMOVED_AT, ReducedJDOHelper.getObjectState(state))
            );
        }
    }


    //------------------------------------------------------------------------
    // Class InvolvedStatesIterator
    //------------------------------------------------------------------------    

    /**
     * Involved States
     */
    class InvolvedStatesIterator implements Iterator<DataObject_1_0> {

        /**
         * Constructor 
         *
         * @param states         * 
         * @param testPersistency 
         * @param accessMode, or {@code null} if filtering is not required
         *  
         * @throws ServiceException
         */
        InvolvedStatesIterator(
        	Iterable<DataObject_1_0> states, 
            AccessMode accessMode, 
            boolean testPersistency
        ) throws ServiceException{
            this.candidates = states.iterator();
            this.accessMode = accessMode;
            this.testPersistency = testPersistency;
        }

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

        /**
         * The access mode
         */
        private final AccessMode accessMode;

        /**
         * 
         */
        private final boolean testPersistency;
        
        /**
         * The iterator is invoked in a given context
         */
        private final C context = getContext();
        
		protected boolean isAcceptable(DataObject_1_0 candidate) throws ServiceException {
			return (
				!testPersistency || candidate.jdoIsPersistent()
			) && (			
				accessMode == null || isInvolved(candidate, context, accessMode)
			);
		}

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext(
        ) {
            try {
                while(
                    this.nextInvolved == null && 
                    this.candidates.hasNext()
                ) {
                    DataObject_1_0 candidate = this.candidates.next();
                    if(isAcceptable(candidate)) {
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
                    ReducedJDOHelper.getPersistenceManager(this.lastInvolved).deletePersistent(this.lastInvolved);
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

	//------------------------------------------------------------------------
    // Class StateCache
    //------------------------------------------------------------------------    

    /**
     * The involved states may be cached if they are persistent
     */
    abstract class StateCache implements Iterable<DataObject_1_0> {

    	/**
    	 * Constructor
    	 * 
    	 * @param accessMode
    	 * @param cacheRequiresFiltering 
    	 */
    	StateCache(
			AccessMode accessMode, 
			boolean cacheRequiresFiltering
		) {
			this.accessMode = accessMode;
			this.cacheRequiresFiltering = cacheRequiresFiltering;
		}
		
    	/**
    	 * Remembers for which access mode the cache is used
    	 */
		protected final AccessMode accessMode;
		
		/**
		 * Tells whether the cache content requires filtering
		 */
		private final boolean cacheRequiresFiltering;
		
    	/**
    	 * The cache version is updated when the cache is refreshed.
    	 */
		private int cacheVersion;
		
		/**
		 * The cached states
		 */
    	private Iterable<DataObject_1_0> cachedStates;

    	/**
    	 * Tells whether the values may be cached
    	 * 
    	 * @return {@code true} if the values may be cached
    	 */
    	protected boolean isCacheable(){
    		return jdoIsPersistent();
    	}
    	
    	/**
    	 * Tells whether the cache can be used or must be (re-)built
    	 * 
    	 * @return {@code true} if the cache may be used without rebuilding
    	 * 
    	 * @throws ServiceException
    	 */
    	protected boolean isWarm() throws ServiceException{
    		return 
    			this.cachedStates != null && 
    			this.cacheVersion == getStateVersion(); 
    	}
    	
    	/**
    	 * Create a new cache based on the underlying iterator
    	 * 
    	 * @param delegate the underlying iterator
    	 * 
    	 * @return an up-to-date cache
    	 * 
    	 * @throws ServiceException
    	 */
    	protected abstract Iterable<DataObject_1_0> newCache(
    		Iterator<DataObject_1_0> delegate	
    	) throws ServiceException;
    	
        /**
         * Retrieve the underlying iterator
         * 
         * @param states
         * @param testInvolvement {@code true} if the states must be filtered 
         * @param testPersistency
         * @return a new Iterator
         * @throws ServiceException
         */
        protected final Iterator<DataObject_1_0> iterator(
        	Iterable<DataObject_1_0> states, 
    		boolean testInvolvement, 
    		boolean testPersistency
        ) throws ServiceException {
        	return testInvolvement ? new InvolvedStatesIterator(states, accessMode, testPersistency) : states.iterator();
        }

        /* (non-Javadoc)
		 * @see org.openmdx.state2.aop1.BasicState_1.InvolvedStates#iterator()
		 */
		public Iterator<DataObject_1_0> iterator() {
			try {
				if(isCacheable()){
					if(isWarm()) {
						return iterator(this.cachedStates, cacheRequiresFiltering, cacheRequiresFiltering);
					} else {
						this.cachedStates = newCache(iterator(getStates(), true, false));
						this.cacheVersion = getStateVersion();
						return iterator(this.cachedStates, false, false);
					} 
				} else {
					return iterator(getStates(), true, false);
				}
			} catch (ServiceException exception) {
				throw new RuntimeServiceException(exception);
			}
		}    	
    	
    }
    
    
    //------------------------------------------------------------------------
    // Class SingleStateCache
    //------------------------------------------------------------------------    
    
    /**
     * Use for<ul>
     * <li>access mode FOR_QUERY in TIME_POINT views 
     * <li>access mode UNDERLYING_STTAE in TIME_RAMGE views 
     * </ul>
     */
    class SingleStateCache extends StateCache {

    	/**
    	 * Constructor
    	 * 
    	 * @param accessMode
    	 */
		SingleStateCache(AccessMode accessMode) {
			super(accessMode, false);
			
		}
    	
    	private DataObject_1_0 cachedState;
    	private boolean invalidState;

    	@Override
    	protected boolean isWarm() throws ServiceException{
    		return super.isWarm() && (
    			this.invalidState || (
					this.cachedState.jdoIsPersistent() &&
					!this.cachedState.jdoIsDeleted() &&
					this.cachedState.objGetValue(REMOVED_AT) == null
    			)
    		);
    	}
    	
		/* (non-Javadoc)
		 * @see org.openmdx.state2.aop1.BasicState_1.StateCache#newCache(java.util.Iterator)
		 */
		@Override
		protected Iterable<DataObject_1_0> newCache(
			Iterator<DataObject_1_0> delegate
		) throws ServiceException {
			if(delegate.hasNext()) {
				this.cachedState = delegate.next();
				this.invalidState = this.cachedState.objGetValue(REMOVED_AT) != null;
				return Collections.singleton(this.cachedState);
			} else {
				this.invalidState = true;
				return Collections.emptySet();
			}
		}

    }


    //------------------------------------------------------------------------
    // Class MultiStateCache
    //------------------------------------------------------------------------    
    
    /**
     * Use <ul>
     * <li>directly for access mode FOR_QUERY in TIME_RAMGE views 
     * <li>indirectly access mode For_UPDATE in TIME_RAMGE views 
     * </ul>
     */
    class MultiStateCache extends StateCache {

    	/**
    	 * Constructor
    	 * 
    	 * @param accessMode
    	 */
    	MultiStateCache(AccessMode accessMode) {
			super(accessMode, true);
		}
    	
		/* (non-Javadoc)
		 * @see org.openmdx.state2.aop1.BasicState_1.StateCache#newCache(java.util.Iterator)
		 */
		@Override
		protected Iterable<DataObject_1_0> newCache(
			Iterator<DataObject_1_0> delegate
		) throws ServiceException {
			final List<DataObject_1_0> cachedStates = new ArrayList<DataObject_1_0>();
			while(delegate.hasNext()) {
				cachedStates.add(delegate.next());
			}
			return cachedStates;
		}

    }
    
    //------------------------------------------------------------------------
    // Class InvolvedStatesForUpdate
    //------------------------------------------------------------------------    
    
    /**
     * The involved states for updates
     */
    class InvolvedStatesForUpdate extends MultiStateCache {
    	
    	/**
    	 * Constructor
    	 * 
    	 * @param accessMode
    	 */
    	InvolvedStatesForUpdate() {
    		super(AccessMode.FOR_UPDATE);
		}
    	
        private final C context = getContext();    	


		@Override
		protected boolean isCacheable() {
			return false;
		}

		@Override
		public Iterator<DataObject_1_0> iterator() {
			try {
		       	Map<DataObject_1_0,BoundaryCrossing> pending = new HashMap<DataObject_1_0,BoundaryCrossing>();	       	
	            for(DataObject_1_0 state : getStates()){
	                if(isInvolved(state, context, accessMode)) {
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
			} catch (ServiceException exception) {
				throw new RuntimeServiceException(exception);
			}
 			return super.iterator();
		}

    }
}