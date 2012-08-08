/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ObjectCreationResult.java,v 1.3 2007/01/21 20:47:09 wfro Exp $
 * Description: CreateOperationResult
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/01/21 20:47:09 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.view;

import org.openmdx.portal.servlet.Action;

public class ObjectCreationResult {

    //-----------------------------------------------------------------------
    public ObjectCreationResult(
        String refMofId,
        String label,
        String title,
        String iconKey
    ) {
        this.selectObjectAction = new Action(
            Action.EVENT_SELECT_OBJECT,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, refMofId)
            },
            label,
            title,
            iconKey,
            true
        );
        this.editObjectAction = new Action(
            Action.EVENT_SELECT_AND_EDIT_OBJECT,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, refMofId)
            },
            label,
            title,
            iconKey,
            true
        );
    }
    
    //-----------------------------------------------------------------------
    public Action getSelectObjectAction(
    ) {
        return this.selectObjectAction;
    }
    
    //-----------------------------------------------------------------------
    public Action getEditObjectAction(
    ) {
        return this.editObjectAction;
    }
    
    //-----------------------------------------------------------------------
    private final Action selectObjectAction;
    private final Action editObjectAction;
    
}

//--- End of File -----------------------------------------------------------
