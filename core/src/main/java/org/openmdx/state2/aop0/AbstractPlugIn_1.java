/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract org::openmdx::state2 Plug-In
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
package org.openmdx.state2.aop0;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.JDOUserCallbackException;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.rest.UnitOfWork_1;
import org.openmdx.base.accessor.spi.ExceptionHelper;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.ClassicSegments;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.state2.spi.Propagation;
import org.openmdx.state2.spi.TechnicalAttributes;
import org.w3c.spi2.Datatypes;

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

    @Override
    public void flush(
        UnitOfWork_1 dataObjectManager, 
        boolean beforeCompletion
    ) {
        // nothing to do
    }

    @Override
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
                    ExceptionHelper.newObjectIdParameter(SystemAttributes.CORE, core)
                );
            } else if(model.isInstanceof(core, "org:openmdx:state2:StateCapable")) {
                core.getAspect("org:openmdx:state2:BasicState").values().add(target);
            } else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "A BasicState's core object must be StateCapable",
                    ExceptionHelper.newObjectIdParameter("target", target),
                    ExceptionHelper.newObjectIdParameter(SystemAttributes.CORE, core),
                    new BasicException.Parameter("expected","org:openmdx:state2:StateCapable"),   
                    new BasicException.Parameter("actual",core == null ? null : core.objGetClass())
                );
            }
        }
    }

    /**
     * Build a qualifier for a basic state instance
     * 
     * @param coreComponent the qualifier's core component
     * @param stateQualifier the qualifier's state component
     * 
     * @return a newly created qualifier
     */
    protected abstract String newBasicStateQualifier(
        String coreComponent,
        Integer stateQualifier
    );

    /**
     * Tests whether an object is of a given type
     * 
     * @param object the object to be examined
     * @param type the model class
     * 
     * @return {@code true} if the object is of the given type
     */
    protected static boolean isInstanceOf(
        DataObject_1 object,
        String type
    ) throws ServiceException {
        return object.jdoGetPersistenceManager().getModel().isInstanceof(object, type);
    }

    /**
     * {@code null}-safe sucessor implementation
     * 
     * @param value the predecessor or {@code null}
     * 
     * @return the next integer value, or {@code 1} if {@code value} is {@code null}
     */
    private static Integer successor(
        Integer value
    ){
        return value == null ? 0 : value + 1;
    }
    
    /**
     * The getQualifier() dispatching method for BasicState instances
     *
     * @return the qualifier for a BasicState instance
     */
    protected String getBasicStateQualifier(
        final DataObject_1 object,
        final String qualifier
    ) throws ServiceException {
    	final DataObject_1_0 core = (DataObject_1_0) object.objGetValue(SystemAttributes.CORE);
        if(qualifier == null) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "A state is added to the container through its core reference only",
                ExceptionHelper.newObjectIdParameter("id", this),
                new BasicException.Parameter("qualifier", qualifier),
                ExceptionHelper.newObjectIdParameter(SystemAttributes.CORE, core)
            );
        } else if(core == null) {
            // We are processing a proxy's request
            return qualifier;
        } else if(ClassicSegments.isPlaceholder(qualifier)){
            final Optional<String> coreComponent = ClassicSegments.getCoreComponentFromAspectQualifierPlaceholder(qualifier);
			if(coreComponent.isPresent()) {
                return newBasicStateQualifier(
                    coreComponent.get(),
                    nextAspectComponent(core)
                );
			} else {
	            throw new ServiceException(
	                BasicException.Code.DEFAULT_DOMAIN,
	                BasicException.Code.BAD_PARAMETER,
	                "The qualifier does not match the aspect qualifier placeholder pattern",
	                ExceptionHelper.newObjectIdParameter("id", this),
	                new BasicException.Parameter("qualifier", qualifier),
                    new BasicException.Parameter("pattern", "^:([^:]+)+:[^:]+$")
	            );
			}
        } else if (core.jdoIsPersistent()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Explicitely provided state components are not supported",
                ExceptionHelper.newObjectIdParameter("id", this),
                new BasicException.Parameter("qualifier", qualifier),
                ExceptionHelper.newObjectIdParameter(SystemAttributes.CORE, core)
            );
        } else {
            return qualifier;
        }
    }

    /**
     * Increments the state version and returns the new value
     * 
     * @param core the state's core object
     * 
     * @return the next aspect component
     */
    private Integer nextAspectComponent(final DataObject_1_0 core)
        throws ServiceException {
        final Integer apectComponent = successor((Integer) core.objGetValue(TechnicalAttributes.STATE_VERSION));
        core.objSetValue(
            TechnicalAttributes.STATE_VERSION,
            apectComponent
        );
        return apectComponent;
    }

    @Override
    public String getQualifier(
        DataObject_1 object, 
        String qualifier
    ) throws ServiceException {
        return isInstanceOf(object, "org:openmdx:state2:BasicState") ? getBasicStateQualifier(object, qualifier) : qualifier;        
    }

    public <T> T getPlugInObject(Class<T> type) {
        return null;
    }

    @Override
    public boolean requiresCallbackOnCascadedDelete(DataObject_1 object) {
        return false;
    }

    /**
     * Tells whether we are processing a state without its core object
     *
     * @return {@code true} if we are processing a state without its core object
     */
    protected boolean isStateOnly(DataObject_1 object) throws ServiceException {
        return isInstanceOf(object, "org:openmdx:state2:StateCapable");
    }
    
    public boolean isExemptFromValidation(
        DataObject_1 object, 
        ModelElement_1_0 feature
    ) throws ServiceException {
        Object qualifiedFeatureName = feature.getQualifiedName();
        return 
            ("org:openmdx:state2:StateCapable:stateVersion".equals(qualifiedFeatureName)) ||
            ("org:openmdx:base:Modifiable:modifiedAt".equals(qualifiedFeatureName) && isStateOnly(object));
    }

    @Override
	public Boolean isAspect(
		DataObject_1 object
	) throws ServiceException {
		return null;
	}

    //------------------------------------------------------------------------
    // Implements StoreLifecycleListener
    //------------------------------------------------------------------------

    @Override
    public void postStore(
        InstanceLifecycleEvent event
    ) {
        // nothing to do
    }

    /**
     * Allow the legacy support to inject its valid time unique behaviour
     */
    protected void basicStatePreStore(
        InstanceLifecycleEvent event
    ) throws ServiceException {
        // nothing to do
    }

	public void postDelete(InstanceLifecycleEvent event) {
		// Not yet supported
	}

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

    @Override
    public void preStore(
        InstanceLifecycleEvent event
    ) {
        DataObject_1 persistentInstance = (DataObject_1) event.getPersistentInstance();
        if(persistentInstance.jdoIsDirty()) try {
            if(isInstanceOf(persistentInstance, "org:openmdx:state2:StateCapable")) {
                if(isInstanceOf(persistentInstance, "org:openmdx:state2:BasicState")){
                    basicStatePreStore(event);
                } else {
                    corePreStore(persistentInstance);
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

	/**
	 * Propagate all core attributes except stateVersion to all states
	 */
	private void corePreStore(
		DataObject_1 persistentInstance
	) throws ServiceException {
		Map<String, DataObject_1_0> states = persistentInstance.getAspect("org:openmdx:state2:BasicState");
		if(!states.isEmpty()) {
		    UnitOfWork_1 unitOfWork = persistentInstance.getUnitOfWork();
		    Set<String> dirtyFeatures = new HashSet<>(
                unitOfWork.getState(persistentInstance, false).dirtyFeatures(true)
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
		                        final Object source = persistentInstance.objGetValue(feature);
                                final Object target = state.objGetValue(feature);
                                if(!Datatypes.equalsIgnoringMutability(source, target)) {
		                            state.objSetValue(feature, source);
		                        }
		                    }
		                    break;
		                    case LIST: {
                                final List<Object> source = persistentInstance.objGetList(feature);
                                final List<Object> target = state.objGetList(feature);
                                if(!Datatypes.equalsIgnoringMutability(source, target)) {
		                            target.clear();
		                            target.addAll(source);
		                        }
		                    }
		                    break;
		                    case SET: {
                                final Set<Object> source = persistentInstance.objGetSet(feature);
                                final Set<Object> target = state.objGetSet(feature);
                                if(!Datatypes.equalsIgnoringMutability(source, target)) {
		                            target.clear();
		                            target.addAll(source);
		                        }
		                    }
		                    break;
		                    case SPARSEARRAY: {
                                final SortedMap<Integer,Object> source = persistentInstance.objGetSparseArray(feature);
                                final SortedMap<Integer,Object> target = state.objGetSparseArray(feature);
                                if(!Datatypes.equalsIgnoringMutability(source, target)) {
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