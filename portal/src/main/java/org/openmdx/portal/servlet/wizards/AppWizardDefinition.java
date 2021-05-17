/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: AppWizardDefinition
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2015, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.wizards;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Properties;

import org.openmdx.base.exception.ServiceException;

/**
 * AppWizardDefinition
 *
 */
public class AppWizardDefinition extends WizardDefinition implements Serializable {
    
	/**
     * Constructor 
     *
     * @param name
     * @param locale
     * @param index
     * @param is
     * @throws ServiceException
     */
    public AppWizardDefinition(
        String name,
        String locale,
        short index,
        InputStream is
    ) throws ServiceException {
        super(
            name,
            locale,
            index,
            is
        );
        try {
            Properties wizardProperties = new Properties();
            wizardProperties.load(is);
            this.label = wizardProperties.getProperty(LABEL_PROPERTY);
            this.toolTip = wizardProperties.getProperty(TOOL_TIP_PROPERTY);
            this.targetType = wizardProperties.getProperty(TARGET_TYPE_PROPERTY);
            this.openParameter = wizardProperties.getProperty(OPEN_PARAMETER_PROPERTY);
            this.forClass = Arrays.asList(wizardProperties.getProperty(FOR_CLASS_PROPERTY).replace(" ", "").split(","));
            this.order = Arrays.asList(wizardProperties.getProperty(ORDER_PROPERTY).replace(" ", "").split(","));
        } catch(IOException e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
	private static final long serialVersionUID = -1310481495729670668L;
    
    private static final String LABEL_PROPERTY = "label";
    private static final String TOOL_TIP_PROPERTY = "toolTip";
    private static final String TARGET_TYPE_PROPERTY = "targetType";
    private static final String OPEN_PARAMETER_PROPERTY = "openParameter";
    private static final String FOR_CLASS_PROPERTY = "forClass";
    private static final String ORDER_PROPERTY = "order";
    
}
