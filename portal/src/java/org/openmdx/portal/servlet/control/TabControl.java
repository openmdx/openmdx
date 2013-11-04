/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ShowObjectView 
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class TabControl
    extends Control
    implements Serializable {

    //-----------------------------------------------------------------------
    public TabControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.Tab tab,
        int paneIndex,
        int tabIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
        this.labels = tab == null ? null : tab.getTitle();
        this.toolTips = tab == null ? null : tab.getToolTip();
        this.iconKey = tab == null ? null : tab.getIconKey();
        this.paneIndex = paneIndex;
        this.tabIndex = tabIndex;
        
        List<FieldGroupControl> fieldGroups = new ArrayList<FieldGroupControl>();
        if(tab != null) {
            for(
              Iterator i = tab.getMember().iterator(); 
              i.hasNext();
            ) {
              org.openmdx.ui1.jmi1.FieldGroup fieldGroup = (org.openmdx.ui1.jmi1.FieldGroup)i.next();
              fieldGroups.add(
                  controlFactory.createFieldGroupControl(
                      fieldGroup.refGetPath().getBase(),
                      locale,
                      localeAsIndex,
                      fieldGroup
                  )
              );
            }
        }
        this.fieldGroupControls = (FieldGroupControl[])fieldGroups.toArray(new FieldGroupControl[fieldGroups.size()]);
    }
  
    //-----------------------------------------------------------------------
    public TabControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.FormDefinition formDefinition,        
        List<FieldGroupControl> fieldGroups
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
        this.paneIndex = 0;
        this.tabIndex = 0;
        this.labels = formDefinition.getTitle();
        this.toolTips = formDefinition.getToolTip();
        this.iconKey = formDefinition.getIconKey();
        this.fieldGroupControls = (FieldGroupControl[])fieldGroups.toArray(new FieldGroupControl[fieldGroups.size()]);
    }
  
    //-------------------------------------------------------------------------
    public FieldGroupControl[] getFieldGroupControl(
    ) {
        return this.fieldGroupControls;
    }
          
    //-------------------------------------------------------------------------
    public String getName(
    ) {
        return this.localeAsIndex < this.labels.size() ? 
            this.labels.get(this.localeAsIndex) : 
            this.labels.get(0);
    }
  
    //-----------------------------------------------------------------------
    public int getTabIndex(
    ) {
        return this.tabIndex;
    }
    
    //-----------------------------------------------------------------------
    public int getPaneIndex(
    ) {
        return this.paneIndex;
    }
    
    //-------------------------------------------------------------------------
    public Integer getTabId(
    ) {
        return 100*(this.getPaneIndex() + 1) + this.getTabIndex();
    }
        
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected final int paneIndex;
    protected final int tabIndex;
    protected final List<String> labels;
    protected final List<String> toolTips;
    protected final String iconKey;
    protected final FieldGroupControl[] fieldGroupControls;
    
}
