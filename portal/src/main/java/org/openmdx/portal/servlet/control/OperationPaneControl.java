/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: OperationPaneControl
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.OperationPane;

/**
 * OperationPaneControl
 *
 */
public abstract class OperationPaneControl extends PaneControl implements Serializable {
  
    /**
     * Constructor 
     *
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param valueFactory
     * @param paneIndex
     * @param forClass
     */
    public OperationPaneControl(
        String id,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        int paneIndex,
        String forClass
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            paneIndex
        );
    }

    /**
     * Create new component.
     * 
     * @param view
     * @return
     */
    public abstract OperationPane newComponent(
    	ObjectView view
    );
        
    /**
     * Get localized tool tip for this pane.
     * 
     * @return
     */
    public abstract String getToolTip(
    );
    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258126938563294520L;
 
    public static final String FRAME_PARAMETERS = "Parameters";
    public static final String FRAME_RESULTS = "Results";

}

//--- End of File -----------------------------------------------------------
