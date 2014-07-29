/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ReferencePane
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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.control.ReferencePaneControl;

/**
 * ReferencePane
 *
 */
public abstract class ReferencePane extends Component implements Serializable {
  
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
	}
	
    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Canvas#getView()
	 */
	@Override
	public ObjectView getView(
	) {
		return (ObjectView)this.view;
	}

    /**
     * Returns true if control was refreshed while selecting it.
     * 
     * @param index
     * @return
     */
    public abstract boolean selectReference(
        int index
    );

    /**
     * Get selected reference.
     * 
     * @return
     */
    public abstract int getSelectedReference(
    );

    /**
     * Get grid view.
     * 
     * @return
     */
    public abstract Grid getGrid(
	);
  
    /**
     * Get actions for selecting the references for this pane.
     * 
     * @return
     */
    public abstract List<Action> getSelectReferenceActions(
    );

    /**
     * Get control casted to ReferencePaneControl.
     * 
     * @return
     */
    public ReferencePaneControl getReferencePaneControl(
    ) {
    	return (ReferencePaneControl)this.control;
    }

    /**
     * Get pane index.
     * 
     * @return
     */
    public int getPaneIndex(
    ) {
        return this.getReferencePaneControl().getPaneIndex();
    }    

    /**
     * Get action for selecting this reference pane.
     * 
     * @return
     */
    public Action[] getSelectReferenceAction(
    ) {
        return this.getReferencePaneControl().getSelectReferenceAction();
    }

    /**
     * Set multi-delete enabled option.
     * 
     * @param newValue
     */
    public void setIsMultiDeleteEnabled(
        boolean newValue
    ) {
        this.isMultiDeleteEnabled = newValue;
    }
    
    /**
     * Get multi-delete enabled option.
     * 
     * @return
     */
    public boolean getIsMultiDeleteEnabled(
    ) {
        return this.isMultiDeleteEnabled;
    }
     	
    /**
     * Paint reference pane for given grids.
     * 
     * @param p
     * @param frame
     * @param forEditing
     * @param grids
     * @throws ServiceException
     */
    public abstract void paint(
    	ViewPort p,
    	String frame,
    	boolean forEditing,
    	List<String> grids
    ) throws ServiceException;
    
	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258132466186203704L;

    protected boolean isMultiDeleteEnabled = true; 
    protected String lookupType;
    
}

//--- End of File -----------------------------------------------------------
