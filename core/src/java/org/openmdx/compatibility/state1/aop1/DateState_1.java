/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DateState_1.java,v 1.8 2009/05/26 14:38:49 wfro Exp $
 * Description: Compatibility Date State
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/26 14:38:49 $
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
package org.openmdx.compatibility.state1.aop1;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.aop1.State_1_Attributes;
import org.openmdx.base.exception.ServiceException;

/**
 * To make the class org::openmdx::compatibiliy::state1::DateState public 
 */
public class DateState_1 extends org.openmdx.state2.aop1.DateState_1 {

    /**
     * Constructor 
     *
     * @param self
     * @param attachCore
     * @throws ServiceException
     */
    public DateState_1(
        ObjectView_1_0 self, 
        boolean attachCore
    ) throws ServiceException {
        super(self, attachCore);
    }

    private static final List<String> IGNORABLE_ATTRIBUTES = Arrays.asList(
        "stateValidFrom", "stateValidTo",
        SystemAttributes.CREATED_AT, SystemAttributes.CREATED_BY,
        SystemAttributes.REMOVED_AT, SystemAttributes.REMOVED_BY,
        SystemAttributes.MODIFIED_AT, SystemAttributes.MODIFIED_BY
    );

    /**
     * Tells which attributes shall be ignored when comparing two states for similarity
     * 
     * @return the set of attributes to  be ignored when comparing two states for similarity
     */
    protected Collection<String> ignorableAttributes(
    ){
        return IGNORABLE_ATTRIBUTES;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.AbstractState_1#getStateClass()
     */
    @Override
    protected String getStateClass() {
        return "org:openmdx:compatibility:state1:DateState";
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.DateState_1#objGetValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return super.objGetValue(
            State_1_Attributes.REMOVED_AT_ALIAS.equals(feature) ? SystemAttributes.REMOVED_AT : 
            State_1_Attributes.CREATED_AT_ALIAS.equals(feature) ? SystemAttributes.CREATED_AT :
            feature
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.Aspect_1#objGetSet(java.lang.String)
     */
    @Override
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        return super.objGetSet(
            State_1_Attributes.REMOVED_BY_ALIAS.equals(feature) ? SystemAttributes.REMOVED_BY : 
            feature
        );
    }

}