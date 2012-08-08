/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: TransientObjectView.java,v 1.1 2008/12/08 23:51:26 wfro Exp $
 * Description: FormView 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/08 23:51:26 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.attribute.Attribute;

public class TransientObjectView
    extends View
    implements Serializable {

    //-------------------------------------------------------------------------
    public TransientObjectView(
        Object object,
        ApplicationContext application,
        RefObject_1_0 lookupObject
    ) {
        super(
            UUIDs.getGenerator().next().toString(),
            null,
            object,
            application
        );
        this.lookupObject = lookupObject;
    }

    //-------------------------------------------------------------------------
    public String getType(
    ) {
        return VIEW_USER_DEFINED;
    }

    //-------------------------------------------------------------------------
    public RefObject_1_0 getLookupObject(
    ) {
        return this.lookupObject;
    }
    
    //-------------------------------------------------------------------------
    public Map getAsFieldMap(
        Attribute[] attributes
    ) {
        Map<String,Attribute> fieldMap = new HashMap<String,Attribute>();
        for(
            int i = 0; i < attributes.length; 
            i++
        ) {
            fieldMap.put(
                attributes[i].getValue().getName(),
                attributes[i]
            );
        }
        return fieldMap;
    }
    
    //-------------------------------------------------------------------------
    public Map<String,?> getAsParameterMap(
        Map<String,?> allParameters,
        String[] requiredParameters
    ) {
        Map<String,Object> parameterMap = new HashMap<String,Object>();
        for(
            int i = 0; i < requiredParameters.length; 
            i++
        ) {
            if(allParameters.get(requiredParameters[i]) != null) {
                parameterMap.put(
                    requiredParameters[i],
                    allParameters.get(requiredParameters[i])
                );
            }
        }
        return parameterMap;
    }
    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private final RefObject_1_0 lookupObject;
    
}
