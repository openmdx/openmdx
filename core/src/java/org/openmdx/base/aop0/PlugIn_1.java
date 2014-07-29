/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Plug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2014, OMEX AG, Switzerland
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
package org.openmdx.base.aop0;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.jdo.JDOUserCallbackException;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.DataObjectManager_1;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.rest.UnitOfWork_1;
import org.openmdx.base.aop1.Removable_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * Standard Plug-In
 * <p>
 * This plug-in supports the following id types<ol>
 * <li><b>UUID</b> <i>(default)</i><br>
 * <li><b>UID</b> (a base 36 encoded UUID)
 * <li><b>OID</b> (a UUID based OID)
 * <li><b>URN</b> (a UUID based URN cross reference)
 * <li><b>XRI</b> (a UUID based XRI cross reference)
 * </ol>
 * <p>
 * The resulting qualifiers for <em>sunrise</em> are<ol>
 * <li>UUID: <em>3878a220-0f81-11dc-804a-0002a5d5c51b</em>
 * <li>UID: <em>3UWYS3X0IS06405P3BJUDWX7F</em>
 * <li>OID: <em>2.25.75063131677434771150912906774302803227</em>
 * <li>URN: <em>(urn:uuid:3878a220-0f81-11dc-804a-0002a5d5c51b)</em>
 * <li>XRI: <em>($t*uuid*3878a220-0f81-11dc-804a-0002a5d5c51b)</em>
 * </ol>
 */
public class PlugIn_1 
    implements PlugIn_1_0, DeleteLifecycleListener, StoreLifecycleListener
{

    /**
     * <code>UUID</code> is the default qualifier type.
     */
    private QualifierType defaultQualifierType = QualifierType.UUID;
    
    /**
     * Retrieve the default qualifier type.
     *
     * @return the default qualifier type
     */
    public String getDefaultQualifierType() {
        return this.defaultQualifierType.name();
    }
    
    /**
     * Set the default qualifier type.
     * 
     * @param defaultQualifierType The default qualifier type to set.
     */
    public void setDefaultQualifierType(
        String defaultQualifierType
    ) {
        this.defaultQualifierType = QualifierType.valueOf(defaultQualifierType);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#setCore(org.openmdx.base.accessor.rest.DataObject_1, org.openmdx.base.accessor.rest.DataObject_1)
     */
    @Override
    public void postSetCore(
        DataObject_1 target, 
        DataObject_1 core
    ) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#flush(org.openmdx.base.accessor.rest.UnitOfWork_1)
     */
    @Override
    public void flush(
        UnitOfWork_1 unitOfWork, 
        boolean beforeCompletion
    ) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.PlugIn_1_0#newQualifier(org.openmdx.base.accessor.rest.DataObject_1, java.lang.String)
     */
    @Override
    public String getQualifier(
        DataObject_1 object, 
        String qualifier
    ) {
        if(qualifier == null && object.isProxy()) {
            return PathComponent.createPlaceHolder().toString();
        } else if(qualifier == null || PathComponent.isPlaceHolder(qualifier)) {
            UUID uuid = UUIDs.newUUID();
            switch(this.defaultQualifierType) {
                case UID: return UUIDConversion.toUID(uuid);
                case OID: return UUIDConversion.toOID(uuid);
                case URN: return "(" + UUIDConversion.toURN(uuid) + ")";
                case XRI: return "(" + UUIDConversion.toXRI(uuid) + ")";
                case UUID: default: return uuid.toString();
            }
        }
        return qualifier;
    }

    /* (non-Javadoc)
	 * @see org.openmdx.base.aop0.PlugIn_1_0#getPlugInObject(java.lang.Class)
	 */
	public <T> T getPlugInObject(Class<T> type) {
		return null;
	}
    
    /* (non-Javadoc)
	 * @see org.openmdx.base.aop0.PlugIn_1_0#isExemptFromValidation(org.openmdx.base.mof.cci.ModelElement_1_0)
	 */
	public boolean isExemptFromValidation(DataObject_1 object, ModelElement_1_0 feature) {
		return false;
	}

	
    //------------------------------------------------------------------------
    // Implements StoreLifecycleListener
    //------------------------------------------------------------------------

	/* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#postStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void postStore(InstanceLifecycleEvent event) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#preStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void preStore(InstanceLifecycleEvent event) {
        DataObject_1 persistentInstance = (DataObject_1) event.getPersistentInstance();
        DataObjectManager_1 dataObjectManager = persistentInstance.jdoGetPersistenceManager(); 
        Model_1_0 model = dataObjectManager.getModel(); 
        try {
            UnitOfWork_1 unitOfWork = persistentInstance.getUnitOfWork();
            if(model.isInstanceof(persistentInstance, "org:openmdx:base:Creatable")){
                if(persistentInstance.jdoIsNew()) {
                    persistentInstance.objSetValue(SystemAttributes.CREATED_AT, unitOfWork.getTransactionTime());
                    List<Object> createdBy = persistentInstance.objGetList(SystemAttributes.CREATED_BY);
                    createdBy.clear();
                    createdBy.addAll(UserObjects.getPrincipalChain(dataObjectManager));
                } else {
                    Set<String> dirtyFeatures = unitOfWork.getState(persistentInstance,false).dirtyFeatures(false);
                    dirtyFeatures.remove(SystemAttributes.CREATED_AT);
                    dirtyFeatures.remove(SystemAttributes.CREATED_BY);
                }
            }
            if(
                model.isInstanceof(persistentInstance, "org:openmdx:base:Modifiable") &&
                !model.isInstanceof(persistentInstance, "org:openmdx:base:Aspect")
            ){
                if(persistentInstance.jdoIsDirty() && !persistentInstance.jdoIsDeleted()) {
                    persistentInstance.objSetValue(SystemAttributes.MODIFIED_AT, unitOfWork.getTransactionTime());
                    List<Object> modifiedBy = persistentInstance.objGetList(SystemAttributes.MODIFIED_BY);
                    modifiedBy.clear();
                    modifiedBy.addAll(UserObjects.getPrincipalChain(dataObjectManager));                
                } else {
                    Set<String> dirtyFeatures = unitOfWork.getState(persistentInstance,false).dirtyFeatures(false);
                    dirtyFeatures.remove(SystemAttributes.MODIFIED_AT);
                    dirtyFeatures.remove(SystemAttributes.MODIFIED_BY);
                }
            }
            if(model.isInstanceof(persistentInstance, "org:openmdx:base:Removable")){
                if(Removable_1.IN_THE_FUTURE.equals(persistentInstance.objGetValue(SystemAttributes.REMOVED_AT))) {
                    persistentInstance.objSetValue(SystemAttributes.REMOVED_AT, unitOfWork.getTransactionTime());
                    List<Object> removedBy = persistentInstance.objGetList(SystemAttributes.REMOVED_BY);
                    removedBy.clear();
                    removedBy.addAll(UserObjects.getPrincipalChain(dataObjectManager));
                } else {
                    Set<String> dirtyFeatures = unitOfWork.getState(persistentInstance,false).dirtyFeatures(false);
                    dirtyFeatures.remove(SystemAttributes.REMOVED_AT);
                    dirtyFeatures.remove(SystemAttributes.REMOVED_BY);
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
    
    //------------------------------------------------------------------------
    // Implements DeleteLifecycleListener
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteLifecycleListener#postDelete(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void postDelete(InstanceLifecycleEvent event) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteLifecycleListener#preDelete(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void preDelete(InstanceLifecycleEvent event) {
        DataObject_1 persistentInstance = (DataObject_1) event.getPersistentInstance();
        try {
            Model_1_0 model = Model_1Factory.getModel(); 
            if(model.isInstanceof(persistentInstance, "org:openmdx:base:Removable")){
                if(persistentInstance.jdoIsNew()){
                    return; // new objects may be deleted
                }
                if(model.isInstanceof(persistentInstance, "org:openmdx:base:Aspect")) {
                    DataObject_1 core = (DataObject_1)persistentInstance.objGetValue("core");
                    if(core == null || core.jdoIsDeleted()) {
                    	// a core object's aspects may be deleted together with the core object
                    	// or if the core object is lacking due to model misuse (e.g. stated objects
                    	//  used in a non-stated way).
                        return;
                    }
                }
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    "A Removable object can't be deleted unless it is new",
                    new BasicException.Parameter("id", persistentInstance.jdoGetObjectId()),
                    new BasicException.Parameter("state", ReducedJDOHelper.getObjectState(persistentInstance))
                );
            }
        } catch (ServiceException exception) {
            throw new JDOUserCallbackException(
                "pre-delete callback failure",
                exception,
                persistentInstance
            );
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#callbackOnCascadedDeletes()
     */
    @Override
    public boolean requiresCallbackOnCascadedDelete(DataObject_1 object) {
        return false;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.base.aop0.PlugIn_1_0#isAspect(org.openmdx.base.accessor.rest.DataObject_1)
	 */
    @Override
	public Boolean isAspect(
		DataObject_1 object
	) throws ServiceException {
		return Boolean.valueOf(object.isAspect());
	}

	
    //------------------------------------------------------------------------
    // Enum QualifierType
    //------------------------------------------------------------------------
    
    /**
     * Qualifier Type
     */
    static enum QualifierType {
        
        /**
         * <b>UUID</b> <i>(default)</i>
         */
        UUID,
        
        /**
         * <b>UID</b> (a base 36 encoded UUID)
         */
        UID,
        
        /**
         * <b>OID</b> (a UUID based OID)
         */
        OID,
        
        /**
         * <b>URN</b> (a UUID based URN cross reference)
         */
        URN,
        
        /**
         * <b>XRI</b> (a UUID based XRI cross reference)
         */
        XRI
        
    }

}
