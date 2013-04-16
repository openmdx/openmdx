/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract org::openmdx::state2 Plug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2012, OMEX AG, Switzerland
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
package org.openmdx.state2.aop0;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.JDOUserCallbackException;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.rest.UnitOfWork_1;
import org.openmdx.base.accessor.spi.ExceptionHelper;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.state2.spi.Propagation;

/**
 * Abstract org::openmdx::state2 Plug-In
 */
public abstract class AbstractPlugIn_1 implements PlugIn_1_0, StoreLifecycleListener, DeleteLifecycleListener {

    /**
     * Defines whether StateCapable objects may be deleted
     */
    private boolean stateCapableDeletable = false;

    /**
     * Retrieve stateCapableDeletable.
     *
     * @return Returns the stateCapableDeletable.
     */
    public boolean isStateCapableDeletable() {
        return this.stateCapableDeletable;
    }

    /**
     * Set stateCapableDeletable.
     * 
     * @param stateCapableDeletable The stateCapableDeletable to set.
     */
    public void setStateCapableDeletable(
        boolean stateCapableDeletable
    ) {
        this.stateCapableDeletable = stateCapableDeletable;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#flush(org.openmdx.base.accessor.rest.UnitOfWork_1)
     */
//  @Override
    public void flush(
        UnitOfWork_1 dataObjectManager, 
        boolean beforeCompletion
    ) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#setCore(org.openmdx.base.accessor.rest.DataObject_1, org.openmdx.base.accessor.rest.DataObject_1)
     */
//  @Override
    public void postSetCore(
        DataObject_1 target, 
        DataObject_1 core
    ) throws ServiceException {
        Model_1_0 model = target.getModel();
        if(model.isInstanceof(target, "org:openmdx:state2:BasicState")) {
            if(target == core) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "setCore() must no longer be used to set the validTimeUnique flag",
                    ExceptionHelper.newObjectIdParameter("target", target),
                    ExceptionHelper.newObjectIdParameter("core", core)
                );
            } else if(model.isInstanceof(core, "org:openmdx:state2:StateCapable")) {
                core.getAspect("org:openmdx:state2:BasicState").values().add(target);
            } else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "A BasicState's core object must be StateCapable",
                    ExceptionHelper.newObjectIdParameter("target", target),
                    ExceptionHelper.newObjectIdParameter("core", core),
                    new BasicException.Parameter("expected","org:openmdx:state2:StateCapable"),   
                    new BasicException.Parameter("actual",core == null ? null : core.objGetClass())
                );
            }
        }
    }

    /**
     * Build a qualifier
     * 
     * @param coreQualifier
     * @param stateQualifier
     * 
     * @return a newly created qualifier
     */
    protected abstract String newBasicStateQualifier(
        PathComponent coreQualifier,
        Integer stateQualifier
    );

    /**
     * Tests whether an object is of a given type
     * 
     * @param object
     * @param type the model class
     * 
     * @return <code>true</code> if the object is of the given type
     * @throws ServiceException
     */
    protected static boolean isInstanceOf(
        DataObject_1 object,
        String type
    ) throws ServiceException {
        return object.jdoGetPersistenceManager().getModel().isInstanceof(object, type);
    }

    /**
     * <code>null</code>-safe sucessor implementation
     * 
     * @param value
     * 
     * @return the next integer value, or <code>1</code> if <code>value</code> is <code>null</code>
     */
    private static Integer successor(
        Integer value
    ){
        return Integer.valueOf(value == null ? 0 : value.intValue() + 1);
    }
    
    /**
     * The getQualifier() dispatching method for BasicState instances
     * 
     * @param object
     * @param qualifier
     * 
     * @return the qualifier for a BasicState instance
     * 
     * @throws ServiceException
     */
    protected String getBasicStateQualifier(
        DataObject_1 object,
        String qualifier
    ) throws ServiceException {
        DataObject_1_0 core = (DataObject_1_0) object.objGetValue("core");
        if(qualifier == null) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "A state is added to the container through its core reference only",
                ExceptionHelper.newObjectIdParameter("id", this),
                new BasicException.Parameter("qualifier", qualifier),
                ExceptionHelper.newObjectIdParameter("core", core)
            );
        } else if(core == null) {
            // We are processing a proxy's request
            return qualifier;
        } else {
            PathComponent pathComponent = new PathComponent(qualifier);
            if(pathComponent.isPlaceHolder()){
                if(pathComponent.size() != 3) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "The qualifier has an unexpected format for a state's placeholder",
                    ExceptionHelper.newObjectIdParameter("id", this),
                    new BasicException.Parameter("qualifier",qualifier)
                );
                Integer id = successor((Integer) core.objGetValue("stateVersion"));
                core.objSetValue(
                    "stateVersion",
                    id
                );
                return newBasicStateQualifier(
                    new PathComponent(pathComponent.getParent().getSuffix(1)),
                    id
                );
            } else if (core.jdoIsPersistent()) {
                StringBuilder aspectQualifier = new StringBuilder(core.jdoGetObjectId().getBase());
                for(
                    int i = 1;
                    i < pathComponent.size();
                    i++
                ){
                    String aspectId = pathComponent.get(i);
                    if(!aspectId.startsWith("!") && !aspectId.startsWith("*")) {
                        aspectQualifier.append('*');
                    }
                    aspectQualifier.append(aspectId);
                }
                return aspectQualifier.toString();
            } else {
                return qualifier;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#getQualifier(org.openmdx.base.accessor.rest.DataObject_1, java.lang.String)
     */
//  @Override
    public String getQualifier(
        DataObject_1 object, 
        String qualifier
    ) throws ServiceException {
        return isInstanceOf(object, "org:openmdx:state2:BasicState") ? getBasicStateQualifier(object, qualifier) : qualifier;        
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#getPlugInObject(java.lang.Class)
     */
    public <T> T getPlugInObject(Class<T> type) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#callbackOnCascadedDeletes()
     */
    //  @Override
    public boolean requiresCallbackOnCascadedDelete(DataObject_1 object) {
        return false;
    }

    /**
     * Tells whether we are processing a state without its core object
     * 
     * @param object
     * 
     * @return <code>true</code> if we are processing a state without its core object
     * 
     * @throws ServiceException
     */
    protected boolean isStateOnly(DataObject_1 object) throws ServiceException {
        return isInstanceOf(object, "org:openmdx:state2:StateCapable");
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#isExemptFromValidation(org.openmdx.base.mof.cci.ModelElement_1_0)
     */
    public boolean isExemptFromValidation(
        DataObject_1 object, 
        ModelElement_1_0 feature
    ) throws ServiceException {
        Object qualifiedFeatureName = feature.objGetValue("qualifiedName");
        return 
            ("org:openmdx:state2:StateCapable:stateVersion".equals(qualifiedFeatureName)) ||
            ("org:openmdx:base:Modifiable:modifiedAt".equals(qualifiedFeatureName) && isStateOnly(object));
    }

	/* (non-Javadoc)
	 * @see org.openmdx.base.aop0.PlugIn_1_0#isAspect(org.openmdx.base.accessor.rest.DataObject_1)
	 */
//  @Override
	public Boolean isAspect(
		DataObject_1 object
	) throws ServiceException {
		return null;
	}

	
    //------------------------------------------------------------------------
    // Implements StoreLifecycleListener
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#postStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
    //  @Override
    public void postStore(
        InstanceLifecycleEvent event
    ) {
        // nothing to do
    }

    /**
     * 
     * @param event
     */
    protected void basicStatePreStore(
        InstanceLifecycleEvent event
    ) throws ServiceException {
        // nothing to do
    }

    /* (non-Javadoc)
	 * @see javax.jdo.listener.DeleteLifecycleListener#postDelete(javax.jdo.listener.InstanceLifecycleEvent)
	 */
	public void postDelete(InstanceLifecycleEvent event) {
		// Not yet supported
	}

	/* (non-Javadoc)
	 * @see javax.jdo.listener.DeleteLifecycleListener#preDelete(javax.jdo.listener.InstanceLifecycleEvent)
	 */
	public void preDelete(InstanceLifecycleEvent event) {
        DataObject_1 persistentInstance = (DataObject_1) event.getPersistentInstance();
        try {
            if(
            	!persistentInstance.jdoIsNew() && 
            	!this.isStateCapableDeletable() &&
            	isInstanceOf(persistentInstance, "org:openmdx:state2:StateCapable") &&
            	!isInstanceOf(persistentInstance, "org:openmdx:state2:BasicState")
            ) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "The configuration inhibits the removal of StateCapable instances unless they are new",
                new BasicException.Parameter("stateCapableDeletable", this.stateCapableDeletable),
                new BasicException.Parameter("state", ReducedJDOHelper.getObjectState(persistentInstance))
            );
        } catch (ServiceException exception) {
            throw new JDOUserCallbackException(
                "pre-delete callback failure",
                exception,
                persistentInstance
            );
        }
	}

	/* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#preStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
    //  @Override
    public void preStore(
        InstanceLifecycleEvent event
    ) {
        DataObject_1 persistentInstance = (DataObject_1) event.getPersistentInstance();
        if(persistentInstance.jdoIsDirty()) try {
            if(isInstanceOf(persistentInstance, "org:openmdx:state2:StateCapable")) {
                if(isInstanceOf(persistentInstance, "org:openmdx:state2:BasicState")){
                    basicStatePreStore(event);
                } else {
                    Map<String, DataObject_1_0> states = persistentInstance.getAspect("org:openmdx:state2:BasicState");
                    if(!states.isEmpty()) {
                        //
                        // Propagate all core attributes except stateVersion to all states
                        //
                        UnitOfWork_1 unitOfWork = persistentInstance.getUnitOfWork();
                        Set<String> dirtyFeatures = new HashSet<String>(
                            unitOfWork.getState(persistentInstance,false).dirtyFeatures(true)
                        );
                        dirtyFeatures.removeAll(Propagation.NON_PROPAGATED_ATTRIBUTES);
                        if(persistentInstance.jdoIsNew() || !dirtyFeatures.isEmpty()) {
                            Model_1_0 model = persistentInstance.jdoGetPersistenceManager().getModel(); 
                            Map<String, ModelElement_1_0> attributes = model.getAttributeDefs(
                                model.getElement(persistentInstance.objGetClass()),
                                false, // sub-types
                                true // includeDerived
                            );
                            for(DataObject_1_0 state: states.values()) {
                                for(String feature : dirtyFeatures) {
                                    Multiplicity multiplicity = ModelHelper.getMultiplicity(attributes.get(feature));
                                    switch(multiplicity) {
                                        case SINGLE_VALUE: case OPTIONAL: {
                                            Object source = persistentInstance.objGetValue(feature);
                                            Object target = state.objGetValue(feature);
                                            if(source == null ? target != null : !source.equals(target)) {
                                                state.objSetValue(feature, source);
                                            }
                                        }
                                        break;
                                        case LIST: {
                                            SortedMap<Integer,Object> source = persistentInstance.objGetSparseArray(feature); 
                                            SortedMap<Integer,Object> target = state.objGetSparseArray(feature);
                                            if(!target.equals(source)) {
                                                target.clear();
                                                target.putAll(source);
                                            }
                                        }
                                        break;
                                        case SET: {
                                            Set<Object> source = persistentInstance.objGetSet(feature); 
                                            Set<Object> target = state.objGetSet(feature);
                                            if(!target.equals(source)) {
                                                target.clear();
                                                target.addAll(source);
                                            }
                                        }
                                        break;
                                        case SPARSEARRAY: {
                                            SortedMap<Integer,Object> source = persistentInstance.objGetSparseArray(feature); 
                                            SortedMap<Integer,Object> target = state.objGetSparseArray(feature);
                                            if(!target.equals(source)) {
                                                target.clear();
                                                target.putAll(source);
                                            }
                                        }
                                        break;
                                        case STREAM:
                                            // Streams are not propagated to their states
                                            break;
                                        default: throw new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.NOT_SUPPORTED,
                                            "The given multiplicity is not supported for core atttributes",
                                            new BasicException.Parameter("multiplicity", multiplicity),
                                            new BasicException.Parameter("attribute", feature)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ServiceException exception) {
            throw new JDOUserCallbackException(
                "pre-store callback failure",
                exception,
                persistentInstance
            );
        }
    }

}