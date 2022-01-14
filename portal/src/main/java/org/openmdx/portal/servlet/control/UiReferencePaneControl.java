/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: UiReferencePaneControl
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.UiGridSelectReferenceAction;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.UiReferencePane;

/**
 * UiReferencePaneControl
 *
 */
public class UiReferencePaneControl extends ReferencePaneControl implements Serializable {
  
    /**
     * Constructor 
     *
     * @param id
     * @param perspective
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param paneDef
     * @param containerClass
     * @param paneIndex
     */
	public UiReferencePaneControl(
		String id,
		int perspective,
		String locale,
		int localeAsIndex,
		PortalExtension_1_0.ControlFactory controlFactory,
		org.openmdx.ui1.jmi1.ReferencePane paneDef,
		String containerClass,
		int paneIndex
	) {
		super(
			id,
			perspective,
			locale,
			localeAsIndex,
			controlFactory,
			containerClass,
			paneIndex
		);
		List<Action> references = new ArrayList<Action>();
		this.gridControls = new ArrayList<UiGridControl>();
		for(
			int i = 0; 
			i < paneDef.getMember().size(); 
			i++
		) {
			org.openmdx.ui1.jmi1.Tab tab = (org.openmdx.ui1.jmi1.Tab)paneDef.getMember().get(i);
			org.openmdx.ui1.jmi1.ObjectContainer objectContainer = (org.openmdx.ui1.jmi1.ObjectContainer)tab.getMember().get(0);
			String title = localeAsIndex < tab.getTitle().size() 
				? tab.getTitle().get(0).startsWith(WebKeys.TAB_GROUPING_CHARACTER) && !tab.getTitle().get(localeAsIndex).startsWith(WebKeys.TAB_GROUPING_CHARACTER) 
					? WebKeys.TAB_GROUPING_CHARACTER + tab.getTitle().get(localeAsIndex) 
					: tab.getTitle().get(localeAsIndex) 
				: !tab.getTitle().isEmpty() 
					? tab.getTitle().get(0) 
					: "NA";
			String toolTip = localeAsIndex < tab.getToolTip().size() 
				? tab.getToolTip().get(localeAsIndex) 
				: title;              
			references.add(
				new Action(
					UiGridSelectReferenceAction.EVENT_ID,
					new Action.Parameter[]{
						new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(this.getPaneIndex())),
						new Action.Parameter(Action.PARAMETER_REFERENCE, Integer.toString(i)),
						new Action.Parameter(Action.PARAMETER_REFERENCE_NAME, objectContainer.getReferenceName()),                      
					},
					title,
					toolTip,
					tab.getIconKey(),
					true
				)
			);
			this.gridControls.add(
				controlFactory.createGridControl(
					Integer.toString(i),
					perspective,
					locale,              
					localeAsIndex,
					(org.openmdx.ui1.jmi1.Tab)paneDef.getMember().get(i),
					this.getPaneIndex(),
					containerClass
				)
			);
		}
		this.selectReferenceActions = (Action[])references.toArray(new Action[references.size()]);
	}

	/**
	 * Create new component.
	 * 
	 * @param view
	 * @param lookupType
	 * @return
	 */
	@Override
	public UiReferencePane newComponent(
		ObjectView view,
		String lookupType
	) {
		return new UiReferencePane(
        	this,
            view,
            lookupType
        );
	}

	/**
     * Create new instance of GridControl. Override for
     * custom-specific implementation.
     *
	 * @param id
	 * @param perspective
	 * @param locale
	 * @param localeAsIndex
	 * @param tabDef
	 * @param paneIndex
	 * @param containerClass
	 * @return
	 */
	protected UiGridControl newUiGridControl(
        String id,
        int perspective,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory  controlFactory,
        org.openmdx.ui1.jmi1.Tab tabDef,
        int paneIndex,
        String containerClass
	) {
		return controlFactory.createGridControl(
			id,
			perspective,
			locale,              
			localeAsIndex,
			tabDef,
			this.getPaneIndex(),
			containerClass
		);
	}
	
    /**
     * Get action for selecting this reference pane.
     * 
     * @return
     */
    public Action[] getSelectReferenceAction(
    ) {
        return this.selectReferenceActions;
    }
    
	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.Control#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		if(UiGridControl.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.gridControls;
			return children;
		} else {
			return Collections.emptyList();
		}
	}

	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258132466186203704L;

    public static final String FRAME_VIEW = "View";
    public static final String FRAME_CONTENT = "Content";

    protected List<UiGridControl> gridControls;
    protected Action[] selectReferenceActions = null;
    
}

//--- End of File -----------------------------------------------------------
