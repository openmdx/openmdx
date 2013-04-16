/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Proxy Plug-In
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
package org.openmdx.base.rest.connector;

import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.rest.UnitOfWork_1;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.PathComponent;

/**
 * Proxy Plug-In
 */
class ProxyPlugIn_1 implements PlugIn_1_0 {

    /**
     * Constructor 
     */
    ProxyPlugIn_1() {
        super();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.PlugIn_1_0#newQualifier(org.openmdx.base.accessor.rest.DataObject_1, java.lang.String)
     */
    public String getQualifier(
        DataObject_1 object, 
        String qualifier
    ) {
        if(qualifier == null && object.getUnitOfWork().getOptimistic()) {
            return PathComponent.createPlaceHolder().toString();
        }
        return qualifier;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#setCore(org.openmdx.base.accessor.rest.DataObject_1, org.openmdx.base.accessor.rest.DataObject_1)
     */
    public void postSetCore(
        DataObject_1 target, 
        DataObject_1 core
    ) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#flush(org.openmdx.base.accessor.rest.UnitOfWork_1)
     */
    public void flush(
        UnitOfWork_1 unitOfWork, 
        boolean beforeCompletion
    ) {
        // nothing to do
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

	/* (non-Javadoc)
	 * @see org.openmdx.base.aop0.PlugIn_1_0#isExemptFromValidation(org.openmdx.base.mof.cci.ModelElement_1_0)
	 */
//  @Override
	public boolean isExemptFromValidation(DataObject_1 object, ModelElement_1_0 feature)
			throws ServiceException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.aop0.PlugIn_1_0#isAspect(org.openmdx.base.accessor.rest.DataObject_1)
	 */
//  @Override
	public Boolean isAspect(DataObject_1 object) {
		return null;
	}

}
