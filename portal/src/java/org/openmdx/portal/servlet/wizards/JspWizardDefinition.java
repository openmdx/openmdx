/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Reports
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import org.openmdx.base.exception.ServiceException;

public class JspWizardDefinition
    extends WizardDefinition
    implements Serializable {
    
    //-----------------------------------------------------------------------
    public JspWizardDefinition(
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            // Read JSP file line by line and inspect for wizard tags
            boolean isFirstForClassTag = true;
            boolean isFirstOrderTag = true;
            while(reader.ready()) {
                String l = reader.readLine();            
                int pos = 0;
                if((pos = l.indexOf(LABEL_TAG)) >= 0) {
                    this.label = l.substring(
                        pos + LABEL_TAG.length(),
                        l.indexOf("\"", pos + LABEL_TAG.length() + 1)
                    );
                }
                if((pos = l.indexOf(TOOL_TIP_TAG)) >= 0) {
                    this.toolTip = l.substring(
                        pos + TOOL_TIP_TAG.length(),
                        l.indexOf("\"", pos + TOOL_TIP_TAG.length() + 1)
                    );
                }
                if((pos = l.indexOf(TARGET_TYPE_TAG)) >= 0) {
                    this.targetType = l.substring(
                        pos + TARGET_TYPE_TAG.length(),
                        l.indexOf("\"", pos + TARGET_TYPE_TAG.length() + 1)
                    );
                }
                if((pos = l.indexOf(OPEN_PARAMETER_TAG)) >= 0) {
                    this.openParameter = l.substring(
                        pos + OPEN_PARAMETER_TAG.length(),
                        l.indexOf("\"", pos + OPEN_PARAMETER_TAG.length() + 1)
                    );
                }
                if((pos = l.indexOf(FOR_CLASS_TAG)) >= 0) {
                    if(isFirstForClassTag) {
                        this.forClass.clear();
                        isFirstForClassTag = false;
                    }
                    this.forClass.add(
                        l.substring(
                            pos + FOR_CLASS_TAG.length(),
                            l.indexOf("\"", pos + FOR_CLASS_TAG.length() + 1)
                        )
                    );
                }
                if((pos = l.indexOf(ORDER_TAG)) >= 0) {
                    if(isFirstOrderTag) {
                        this.order.clear();
                        isFirstOrderTag = false;                        
                    }
                    this.order.add( 
                        l.substring(
                            pos + ORDER_TAG.length(),
                            l.indexOf("\"", pos + ORDER_TAG.length() + 1)
                        )
                    );
                }
            }
        }
        catch(IOException e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 379308474038714868L;
    
    private static final String LABEL_TAG = "<meta name=\"label\" content=\"";
    private static final String TOOL_TIP_TAG = "<meta name=\"toolTip\" content=\"";
    private static final String TARGET_TYPE_TAG = "<meta name=\"targetType\" content=\"";
    private static final String OPEN_PARAMETER_TAG = "<meta name=\"openParameter\" content=\"";
    private static final String FOR_CLASS_TAG = "<meta name=\"forClass\" content=\"";
    private static final String ORDER_TAG = "<meta name=\"order\" content=\"";
    
}

//--- End of File -----------------------------------------------------------
