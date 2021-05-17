/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: OperationFieldGroupControl
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;

import org.openmdx.portal.servlet.UiContext;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.UiOperationParam;

/**
 * OperationFieldGroupControl
 *
 */
public class UiOperationParamControl extends UiFieldGroupControl implements Serializable {
  
	/**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param fieldGroupDef
     */
    public UiOperationParamControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.FieldGroup fieldGroupDef
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            fieldGroupDef
        );
    }
  
    /**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param uiContext
     * @param formFieldGroup
     */
    public UiOperationParamControl(
        String id,
        String locale,
        int localeAsIndex,
        UiContext uiContext,        
        org.openmdx.ui1.jmi1.FormFieldGroupDefinition formFieldGroup
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            uiContext,
            formFieldGroup
        );
    }
  
    /**
     * Create new component. 
     * 
     * @param view
     * @param object
     * @return
     */
    @Override
    public UiOperationParam newComponent(
    	ObjectView view,
    	Object object
    ) {
    	return new UiOperationParam(
    		this,
            view,
            object
        );
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
	private static final long serialVersionUID = -5723558037533066253L;
    
}

//--- End of File -----------------------------------------------------------
