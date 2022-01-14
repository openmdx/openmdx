/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ShowInspectorControl 
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
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.wizards.WizardDefinition;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;

/**
 * ShowInspectorControl
 *
 */
public class ShowInspectorControl extends InspectorControl implements Serializable {

    /**
     * Constructor 
     *
     * @param id
     * @param perspective
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param wizardDefinitionFactory
     * @param inspectorDef
     * @param forClass
     */
    public ShowInspectorControl(
        String id, 
        int perspective,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        WizardDefinitionFactory wizardDefinitionFactory,
        org.openmdx.ui1.jmi1.Inspector inspectorDef,
        String forClass
    ) {
        super(
            id, 
            locale, 
            localeAsIndex,
            controlFactory,
            inspectorDef
        );
        SysLog.detail("Preparing operation and reference panes");
        List<org.openmdx.ui1.jmi1.OperationPane> paneOps = new ArrayList<org.openmdx.ui1.jmi1.OperationPane>();
        List<org.openmdx.ui1.jmi1.ReferencePane> paneRefs = new ArrayList<org.openmdx.ui1.jmi1.ReferencePane>();
        for(Object pane: inspectorDef.getMember()) {
            if (pane instanceof org.openmdx.ui1.jmi1.OperationPane) {
                paneOps.add((org.openmdx.ui1.jmi1.OperationPane)pane);
            } else if (pane instanceof org.openmdx.ui1.jmi1.ReferencePane) {
                if(!((org.openmdx.ui1.jmi1.ReferencePane)pane).getMember().isEmpty()) {
                    paneRefs.add((org.openmdx.ui1.jmi1.ReferencePane)pane);
                }
            }
        }
        // Operation pane
        SysLog.detail("Preparing operation panes");
        List<OperationPaneControl> operationPaneControls = new ArrayList<OperationPaneControl>();
        int index = 0;
        for(org.openmdx.ui1.jmi1.OperationPane pane: paneOps) {
            operationPaneControls.add(
                this.newUiOperationPaneControl(
                    pane.refGetPath().getLastSegment().toClassicRepresentation(), 
                    locale,
                    localeAsIndex,
                    controlFactory,
                    pane,
                    wizardDefinitionFactory,                    
                    index,
                    forClass
                )
            );
            index++;
        }
        this.operationPaneControls = operationPaneControls;
        // Reference pane
        SysLog.detail("Preparing reference panes");
        List<ReferencePaneControl> referencePaneControls = new ArrayList<ReferencePaneControl>();
        index = 0;
        for(org.openmdx.ui1.jmi1.ReferencePane paneRef: paneRefs) {
            referencePaneControls.add(
            	this.newUiReferencePaneControl(
	            	paneRef.refGetPath().getLastSegment().toClassicRepresentation(), 
	                perspective,
	                locale, 
	                localeAsIndex,
	                controlFactory,
	                paneRef,
	                forClass,
	                index
	            )
	        );
            index++;
        }
        this.referencePaneControls = referencePaneControls;
        // Wizards
        SysLog.detail("Preparing wizards");
        this.wizardControl = this.newUiWizardControl(
            this.uuidAsString(), 
            locale,
            localeAsIndex,
            controlFactory,
            wizardDefinitionFactory.findWizardDefinitions(forClass, locale, null)
        );
        SysLog.detail("Preparing wizards done");
    }

    /**
     * Create new instance of OperationPaneControl. Override
     * for custom-specific implementation.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param paneDef
     * @param wizardFactory
     * @param paneIndex
     * @param forClass
     * @return
     */
    protected UiOperationPaneControl newUiOperationPaneControl(
        String id,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.OperationPane paneDef,
        WizardDefinitionFactory wizardFactory,    
        int paneIndex,
        String forClass	    		
    ) {
    	return new UiOperationPaneControl(
    		id,
    		locale,
    		localeAsIndex,
    		controlFactory,
    		paneDef,
    		wizardFactory,
    		paneIndex,
    		forClass
    	);
    }

    /**
     * Create new instance of ReferencePaneControl. Override for
     * custom-specific implementation.
     * 
     * @param id
     * @param perspective
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param paneDef
     * @param containerClass
     * @param paneIndex
     * @return
     */
    protected UiReferencePaneControl newUiReferencePaneControl(
		String id,
		int perspective,
		String locale,
		int localeAsIndex,
		PortalExtension_1_0.ControlFactory controlFactory,
		org.openmdx.ui1.jmi1.ReferencePane paneDef,
		String containerClass,
		int paneIndex	    		
    ) {
    	return new UiReferencePaneControl(
    		id,
    		perspective,
    		locale,
    		localeAsIndex,
    		controlFactory,
    		paneDef,
    		containerClass,
    		paneIndex
    	);
    }

    /**
     * Create new instance of WizardControl. Override for
     * custom-specific implementation.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param wizardDefinitions
     * @return
     */
    protected UiWizardControl newUiWizardControl(
        String id,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        WizardDefinition[] wizardDefinitions	    		
    ) {
    	return new UiWizardControl(
    		id,
    		locale,
    		localeAsIndex,
    		controlFactory,
    		wizardDefinitions
    	);
    }

    /**
     * Get page size.
     * 
     * @param parameterMap
     * @return
     */
    public int getPageSizeParameter(
        Map<String,Object[]> parameterMap
    ) {
        Object[] pageSizes = parameterMap.get(WebKeys.REQUEST_PARAMETER_PAGE_SIZE);
        String pageSize = pageSizes == null ? null : (pageSizes.length > 0 ? (String) pageSizes[0] : null);
        return pageSize == null ? -1 : Integer.parseInt(pageSize);
    }

    /**
     * Select filter.
     * 
     * @param filterName
     * @param filterValues
     */
    public void selectFilter(
        String filterName, 
        String filterValues
    ) {
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.Control#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
     */
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing
    ) throws ServiceException {
        // Do not paint here. For more flexibility paint is implemented in JSP
    }
  
    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.InspectorControl#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		if(OperationPaneControl.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.operationPaneControls;
			return children;			
		} else if(ReferencePaneControl.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.referencePaneControls;
			return children;			
		} else if(WizardControl.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)Collections.singletonList(wizardControl);
			return children;			
		} else {
			return super.getChildren(type);
		}
	}

	// -------------------------------------------------------------------------
    // Variables
    // -------------------------------------------------------------------------
    private static final long serialVersionUID = 3257844376976635442L;
    
    protected List<OperationPaneControl> operationPaneControls;
    protected List<ReferencePaneControl> referencePaneControls;
    protected WizardControl wizardControl;

}

// --- End of File -----------------------------------------------------------
