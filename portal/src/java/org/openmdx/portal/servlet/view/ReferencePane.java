/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ReferencePane.java,v 1.8 2008/08/12 16:38:07 wfro Exp $
 * Description: ReferencePaneControl
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/12 16:38:07 $
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
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmdx.application.log.AppLog;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.control.GridControl;
import org.openmdx.portal.servlet.control.ReferencePaneControl;

//-----------------------------------------------------------------------------
public class ReferencePane
    extends ControlState
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public ReferencePane(
        ReferencePaneControl control,
        ObjectView view,
        String lookupType
    ) {
        super(
            control,
            view
        );
        
        this.lookupType = lookupType;
        
        Model_1_0 model = view.getApplicationContext().getModel();
        int initialReference = -1;
        for(
            int i = 0; 
            i < control.getGridControl().length; 
            i++
        ) {
          try {
              if(
                  (initialReference == -1) &&
                  GridControl.getShowRowSelectors(
                      lookupType, 
                      model.getElement(control.getGridControl()[i].getObjectContainer().getReferencedTypeName()), 
                      model
                  )
              ) {
                  initialReference = i;
              }
          } catch(Exception e) {}
        }
    
        // init grid
        AppLog.detail("initializing grids");
        this.grid = new Grid[control.getGridControl().length];
        this.selectReference(
            initialReference == -1 
                ? 0 
                : initialReference
        );
        AppLog.detail("end initializing grids");
    }
      
    //-------------------------------------------------------------------------
    public ReferencePaneControl getReferencePaneControl(
    ) {
        return (ReferencePaneControl)this.control;
    }
    
    //-------------------------------------------------------------------------
    /**
     * Returns true if control was refreshed while selecting it.
     */
    public boolean selectReference(
        int id
    ) {
        boolean refreshed = false;
        this.selectedReference = id;
        if(
            (id >= 0) && 
            (id < this.grid.length) && 
            (this.grid[id] == null)
        ) {            
            if(this.getReferencePaneControl().getGridControl()[id].getObjectContainer().isReferenceIsStoredAsAttribute()) {
                this.grid[id] = new ReferenceGrid(                    
                    this.getReferencePaneControl().getGridControl()[id],
                    this.view,
                    this.lookupType
                );
            }
            else {
                this.grid[id] = new CompositeGrid(
                    this.getReferencePaneControl().getGridControl()[id],
                    this.view,
                    this.lookupType
                );
            }        
            refreshed = true;
        }
        return refreshed;
    }

    //-------------------------------------------------------------------------
    public int getSelectedReference(
    ) {
        this.selectedReference = java.lang.Math.min(
            this.selectedReference, 
            this.grid.length - 1
        );
        return this.selectedReference;
    }
    
    //-------------------------------------------------------------------------
    public Grid getGrid(
    ) {
        if(
          (this.selectedReference >= 0) && 
          (this.selectedReference < this.grid.length)
        ) {
          return this.grid[this.selectedReference];
        }
        else {
          return null;
        }
    }
  
    //-------------------------------------------------------------------------
    public Action[] getSelectReferenceAction(
    ) {
        Action[] actions = this.getReferencePaneControl().getSelectReferenceAction();
        Action[] actionsWithXri = new Action[actions.length];
        for(int i = 0; i < actions.length; i++) {
            List<Action.Parameter> parameters = new ArrayList<Action.Parameter>(
                Arrays.asList(actions[i].getParameters())
            );
            parameters.add(
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.view.getObjectReference().getObject().refMofId())
            );
            actionsWithXri[i] = new Action(
                actions[i].getEvent(),
                (Action.Parameter[])parameters.toArray(new Action.Parameter[parameters.size()]),
                actions[i].getTitle(),
                actions[i].getToolTip(),
                actions[i].getIconKey(),
                actions[i].isEnabled()
            );
        }        
        return actionsWithXri;
    }
      
    //-------------------------------------------------------------------------
    public void refresh(
        boolean refreshData
    ) {
        for(int i = 0; i < this.grid.length; i++) {
            if(this.grid[i] != null) {
                this.grid[i].refresh(refreshData);
            }
        }
    }
    
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258132466186203704L;
  
    protected Grid[] grid = null;
    protected String lookupType = null;
    protected int selectedReference = -1;
    
}

//--- End of File -----------------------------------------------------------
