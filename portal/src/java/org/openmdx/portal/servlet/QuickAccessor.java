/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: QuickAccessor.java,v 1.7 2009/01/06 14:12:01 wfro Exp $
 * Description: QuickAccess 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 14:12:01 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

package org.openmdx.portal.servlet;

import java.util.List;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.naming.Path;
import org.openmdx.base.text.conversion.Base64;

public class QuickAccessor {
    
    //-----------------------------------------------------------------------
    public QuickAccessor(
        Path targetIdentity,
        String name,
        String description,
        String iconKey,
        Number actionType,
        String actionName,
        List<String> actionParams
    ) {
        this.targetIdentity = targetIdentity;
        this.name = name;
        this.description = description;
        this.iconKey = iconKey;
        this.actionType = actionType == null 
            ? Action.MACRO_TYPE_NA 
            : actionType.intValue();
        this.actionName = actionName == null ? "" : actionName;
        this.actionParams = actionParams.toArray(new String[actionParams.size()]);
    }
    
    //-----------------------------------------------------------------------
    public String getName(
    ) {
        return this.name;
    }
    
    //-----------------------------------------------------------------------
    public Path getTargetIdentity(
    ) {
        return this.targetIdentity;
    }
    
    //-----------------------------------------------------------------------
    public Action getAction(
    ) {
        if(this.action == null) {
            try {
              this.action = new Action(
                  Action.EVENT_MACRO,
                  new Action.Parameter[]{
                      new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.targetIdentity.toXri()),
                      new Action.Parameter(Action.PARAMETER_NAME, Base64.encode(this.actionName.getBytes())),
                      new Action.Parameter(Action.PARAMETER_TYPE, Integer.toString(this.actionType))                    
                  },
                  this.name,
                  this.description,
                  this.iconKey,
                  true
              );
              this.action.setIconKey(
                  this.iconKey
              );
            }
            catch(Exception e) {
                AppLog.detail("Can not get action for quick accessor", e.getMessage());
            }
        }
        return this.action;
    }

    //-----------------------------------------------------------------------
    private final Path targetIdentity;
    private final String name;
    private final String description;
    private final String iconKey;
    private final int actionType;
    private final String actionName;
    @SuppressWarnings("unused")
    private final String[] actionParams;
    private Action action = null;
    
}
