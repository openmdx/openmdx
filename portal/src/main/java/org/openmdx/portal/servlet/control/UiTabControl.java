/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: TabControl 
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

import org.openmdx.portal.servlet.PortalExtension_1_0;

/**
 * TabControl
 *
 */
public abstract class UiTabControl extends Control implements Serializable {

	/**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param tabDef
     * @param paneIndex
     * @param tabIndex
     */
    public UiTabControl(
        String id,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.Tab tabDef,
        int paneIndex,
        int tabIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
        this.labels = tabDef == null ? null : tabDef.getTitle();
        this.toolTips = tabDef == null ? null : tabDef.getToolTip();
        this.iconKey = tabDef == null ? null : tabDef.getIconKey();
        this.paneIndex = paneIndex;
        this.tabIndex = tabIndex;
        List<UiFieldGroupControl> children = new ArrayList<UiFieldGroupControl>();
        if(tabDef != null) {
            for(org.openmdx.ui1.jmi1.FieldGroup fieldGroup: tabDef.<org.openmdx.ui1.jmi1.FieldGroup>getMember()) {
              children.add(
                  this.newFieldGroupControl(
                      fieldGroup.refGetPath().getLastSegment().toClassicRepresentation(),
                      locale,
                      localeAsIndex,
                      fieldGroup
                  )
              );
            }
        }
        this.fieldGroupControls = children;
    }

    /**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param formDefinition
     * @param children
     */
    public UiTabControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.FormDefinition formDefinition,        
        List<UiFieldGroupControl> children
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
        this.fieldGroupControls = children;
    }

    /**
     * Create new instance of FieldGroupControl. Override
     * for custom-specific implementation.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param fieldGroupDef
     * @return
     */
    protected UiFieldGroupControl newFieldGroupControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.FieldGroup fieldGroupDef
    ) {
    	return new UiFieldGroupControl(
    		id,
    		locale,
    		localeAsIndex,
    		fieldGroupDef
    	);	    			
    }
        
	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.Control#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		if(UiFieldGroupControl.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.fieldGroupControls;
			return children;
		} else {
			return Collections.emptyList();
		}
	}

    /**
     * Get name.
     * 
     * @return
     */
    public String getName(
    ) {
        return this.localeAsIndex < this.labels.size() ? 
            this.labels.get(this.localeAsIndex) : 
            this.labels.get(0);
    }
  
    /**
     * Get tab index.
     * 
     * @return
     */
    public int getTabIndex(
    ) {
        return this.tabIndex;
    }
    
    /**
     * Get pane index.
     * 
     * @return
     */
    public int getPaneIndex(
    ) {
        return this.paneIndex;
    }
    
    /**
     * Get tab id.
     * 
     * @return
     */
    public Integer getTabId(
    ) {
        return 100*(this.getPaneIndex() + 1) + this.getTabIndex();
    }
        
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
	private static final long serialVersionUID = -1876730217937917846L;

	protected final int paneIndex;
    protected final int tabIndex;
    protected final List<String> labels;
    protected final List<String> toolTips;
    protected final String iconKey;
    protected List<UiFieldGroupControl> fieldGroupControls;
    
}
