/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: StateCapable_1.java,v 1.10 2011/09/09 17:35:19 hburger Exp $
 * Description: org::openmdx::state2::StateCapable plug-in
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/09/09 17:35:19 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;


/**
 * org::openmdx::state2::StateCapable plug-in
 */
public class StateCapable_1 extends Interceptor_1 {

    /**
     * Constructor 
     *
     * @param self
     * @param next
     * @param validTimeUnique 
     * 
     * @throws ServiceException
     */
    public StateCapable_1(
        ObjectView_1_0 self, 
        Interceptor_1 next, 
        Boolean validTimeUnique
    ) throws ServiceException {
        super(self, next);
        this.validTimeUnique = validTimeUnique;
    }

    /**
     * The value has already been determined
     */
    private final Boolean validTimeUnique;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.Aspect_1#objGetValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        Object value = null;
        boolean validTimeUniqueRequest = "validTimeUnique".equals(feature);
        if(validTimeUniqueRequest) {
        	value = this.validTimeUnique;
        }
    	if (value == null) {
	        value = super.objGetValue(feature);
    	}
        if(value == null) {
        	if(validTimeUniqueRequest || "transactionTimeUnique".equals(feature)) {
				value = Boolean.valueOf(
                    super.getModel().isInstanceof(self, "org:openmdx:base:Aspect") && super.objGetValue("core") == null
                ); 
        	} else if("stateVersion".equals(feature)) {
                value = Integer.valueOf(-1);
            }
    	}
        return value;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.view.Interceptor_1#jdoPreStore()
     */
    @Override
    public void jdoPreStore() {
        super.jdoPreStore();
    }
    
}
