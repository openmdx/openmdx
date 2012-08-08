/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: BasicState_1.java,v 1.1 2008/12/15 03:15:34 hburger Exp $
 * Description: Compatibility State
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:34 $
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
package org.openmdx.compatibility.state1.aop2.core;

import javax.jmi.reflect.RefBaseObject;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.spi.Object_1_5;
import org.openmdx.base.accessor.generic.spi.Object_1_6;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Object_1;
import org.openmdx.compatibility.state1.spi.StateCapables;

/**
 * org::openmdx::compatibility:state1::BasicState Aspect
 */
public class BasicState_1 extends org.openmdx.base.aop2.core.Aspect_1 {
    /**
     * Constructor 
     *
     * @param self
     * @param next
     * 
     * @throws ServiceException
     */
    public BasicState_1(
        Object_1_6 self, 
        Object_1_0 next
    ) throws ServiceException {
        super(self, next);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.core.Aspect_1#objMove(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    @Override
    public void objMove(
        FilterableMap<String, Object_1_0> there, 
        String criteria
    ) throws ServiceException {
        Object_1_5 stateCapable = (Object_1_5) super.self.getFactory().getObject(
            StateCapables.getStateCapable((RefBaseObject) there, criteria)
        );
        stateCapable.objSetValue(Object_1.RECORD_NAME_REQUEST, org.openmdx.compatibility.state1.aop2.core.StateCapable_1.CLASS);
        super.setCore(stateCapable);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.core.Aspect_1#getCoreClass()
     */
    @Override
    protected String getCoreClass(
    ) throws ServiceException {
        return org.openmdx.compatibility.state1.aop2.core.StateCapable_1.CLASS;
    }
    
    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetContainer(java.lang.String)
     */
    public FilterableMap<String, Object_1_0> objGetContainer(
        String feature
    ) throws ServiceException {
        return self.getFactory().getObject(self.objGetResourceIdentifier()).objGetContainer(feature);
    }

}