/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ReferencePaneControl
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
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.control.GridControl;
import org.openmdx.portal.servlet.control.ReferencePaneControl;

/**
 * ReferencePane
 *
 */
public class ReferencePane
    extends ControlState
    implements Serializable {
  
	/**
	 * Constructor 
	 *
	 * @param control
	 * @param view
	 * @param lookupType
	 */
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
		ApplicationContext app = view.getApplicationContext();
		Model_1_0 model = view.getApplicationContext().getModel();
		int initialReference = -1;
		for(int i = 0; i < control.getGridControl().length; i++) {
			try {
				boolean isRevokeShow = app.getPortalExtension().hasPermission(
					control.getGridControl()[i].getQualifiedReferenceName(), 
					view.getRefObject(), 
					app, 
					WebKeys.PERMISSION_REVOKE_SHOW
				);
				boolean showRowSelectors = GridControl.getShowRowSelectors(
					lookupType, 
					model.getElement(control.getGridControl()[i].getObjectContainer().getReferencedTypeName()), 
					model
				);
				if(initialReference == -1 && (lookupType == null || showRowSelectors) && !isRevokeShow) {
					initialReference = i;
				}
			}
			catch(Exception e) {}
		}
		// Init grids
		this.grid = new Grid[control.getGridControl().length];
		this.selectReference(initialReference == -1 ? 0 : initialReference);
	}

    /**
     * Get control for this reference pane view.
     * 
     * @return
     */
    public ReferencePaneControl getReferencePaneControl(
    ) {
        return (ReferencePaneControl)this.control;
    }
    
    /**
     * Returns true if control was refreshed while selecting it.
     * 
     * @param id
     * @return
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

    /**
     * Get selected reference.
     * 
     * @return
     */
    public int getSelectedReference(
    ) {
        this.selectedReference = java.lang.Math.min(
            this.selectedReference, 
            this.grid.length - 1
        );
        return this.selectedReference;
    }
    
    /**
     * Get grid view.
     * 
     * @return
     */
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
  
    /**
     * Get actions for selecting the references for this pane.
     * 
     * @return
     */
    public List<Action> getSelectReferenceActions(
    ) {
    	ApplicationContext app = this.view.getApplicationContext();
        List<Action> selectReferenceActions = new ArrayList<Action>();
        for(Action template: this.getReferencePaneControl().getSelectReferenceAction()) {
            List<Action.Parameter> parameters = new ArrayList<Action.Parameter>(
                Arrays.asList(template.getParameters())
            );
            parameters.add(
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.view.getObjectReference().getXRI())
            );
            selectReferenceActions.add(
            	new Action(
	                template.getEvent(),
	                (Action.Parameter[])parameters.toArray(new Action.Parameter[parameters.size()]),
	                app.getPortalExtension().getTitle(this.view.getObject(), template, template.getTitle(), app),
	                template.getToolTip(),
	                template.getIconKey(),
	                template.isEnabled()
	            )
            );
        }
        return selectReferenceActions;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.ControlState#refresh(boolean)
     */
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
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258132466186203704L;
  
    protected Grid[] grid = null;
    protected String lookupType = null;
    protected int selectedReference = -1;
    
}

//--- End of File -----------------------------------------------------------
