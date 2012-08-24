/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: PlugIn_1 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPO
      SE ARE
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

package org.openmdx.preferences2.aop0;

import javax.jdo.JDOUserCallbackException;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.rest.UnitOfWork_1;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;


/**
 * PlugIn_1
 *
 */
public class PlugIn_1
    implements PlugIn_1_0, StoreLifecycleListener {

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

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#getQualifier(org.openmdx.base.accessor.rest.DataObject_1, java.lang.String)
     */
//  @Override
    public String getQualifier(
        DataObject_1 object, 
        String qualifier
    ) throws ServiceException {
        return qualifier;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#postSetCore(org.openmdx.base.accessor.rest.DataObject_1, org.openmdx.base.accessor.rest.DataObject_1)
     */
//  @Override
    public void postSetCore(
        DataObject_1 target, 
        DataObject_1 core
    ) throws ServiceException {
        // Nothing to do
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#flush(org.openmdx.base.accessor.rest.UnitOfWork_1, boolean)
     */
//  @Override
    public void flush(UnitOfWork_1 unitOfWork, boolean beforeCompletion) {
        // Nothing to do
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#getPlugInObject(java.lang.Class)
     */
//  @Override
    public <T> T getPlugInObject(
        Class<T> type
    ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#requiresCallbackOnCascadedDelete(org.openmdx.base.accessor.rest.DataObject_1)
     */
//  @Override
    public boolean requiresCallbackOnCascadedDelete(
        DataObject_1 object
    ) throws ServiceException {
        return false;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#isExemptFromValidation(org.openmdx.base.accessor.rest.DataObject_1, org.openmdx.base.mof.cci.ModelElement_1_0)
     */
//  @Override
    public boolean isExemptFromValidation(
        DataObject_1 object,
        ModelElement_1_0 feature
    ) throws ServiceException {
        return false;
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

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#preStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void preStore(InstanceLifecycleEvent event) {
        DataObject_1 persistentInstance = (DataObject_1) event.getPersistentInstance();
        if(persistentInstance.jdoIsNew()) try {
            if(isInstanceOf(persistentInstance, "org:openmdx:preferences2:Node")) {
                DataObject_1_0 parent = (DataObject_1_0) persistentInstance.objGetValue("parent");
                if(parent == null) {
                    persistentInstance.objSetValue("absolutePath", "/");
                } else {
                    String parentPath = (String) parent.objGetValue("absolutePath");
                    String name = (String) persistentInstance.objGetValue("name");
                    String delimiter = "/".equals(parentPath) ? "" : "/";
                    persistentInstance.objSetValue("absolutePath", parentPath + delimiter + name);
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

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#postStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void postStore(InstanceLifecycleEvent event) {
        // nothing to do
    }

}
