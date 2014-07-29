/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: InspectorControl 
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jmi.reflect.RefStruct;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.action.SetPanelStateAction;

/**
 * InspectorControl
 *
 */
public abstract class InspectorControl extends Control implements Serializable {
  
	/**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param inspectorDef
     */
	public InspectorControl(
		String id,
		String locale,
		int localeAsIndex,
		PortalExtension_1_0.ControlFactory controlFactory,
		org.openmdx.ui1.jmi1.Inspector inspectorDef
		) {
		super(
			id,
			locale,
			localeAsIndex
		);
		this.inspector = inspectorDef;
		// Inspector
		SysLog.detail("inspector valid", "" + (inspectorDef != null));
		// Attribute pane
		SysLog.detail("Preparing attribute pane");
		org.openmdx.ui1.jmi1.AttributePane paneAttr = null;
		for(Object pane: inspectorDef.getMember()) {
			if(pane instanceof org.openmdx.ui1.jmi1.AttributePane) {
				paneAttr = (org.openmdx.ui1.jmi1.AttributePane) pane;
			}
		} 
		this.attributePaneControls = new ArrayList<AttributePaneControl>(
			Collections.singletonList(
				this.newUiAttributePaneControl(
					this.uuidAsString(),
					locale,
					localeAsIndex,
					controlFactory,
					paneAttr, 
					0
				)
			)
		);
	}

    /**
     * Create new instance of AttributePaneControl. Override for
     * custom-specific implementation.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param paneDef
     * @param paneIndex
     * @return
     */
    protected AttributePaneControl newUiAttributePaneControl(
        String id,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.AttributePane paneDef,
        int paneIndex	    		
    ) {
    	return new UiAttributePaneControl(
    		id,
    		locale,
    		localeAsIndex,
    		controlFactory,
    		paneDef,
    		paneIndex
    	);
    }

    /**
     * Get SetPanelState action.
     * 
     * @param panelName
     * @param panelState
     * @return
     */
    public Action getSetPanelStateAction(
        String panelName,
        int panelState
    ) {
        return new Action(
            SetPanelStateAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_NAME, panelName),
                new Action.Parameter(Action.PARAMETER_STATE, "" + panelState)
            },
            "N/A",
            true
        );      
    }
  
    /**
     * Each view has an id and a childId. Normally all views of the same
     * 'layer' have the same id, i.e. a view which creates a 'next' view
     * inherits the id. getChild(), createNewChild() allows the create 
     * new view layers.
     * <p>
     * A JSP wants to create a ShowObject dialog in a new window. This
     * can be done by creating a new child id and then issuing an event
     * (e.g. EVENT_FIND_OBJECT) which creates a view with the new child id.
     */
    public String getId(
    ) {
        return this.id;
    }
  
    /**
     * Returns classes which are in the composition hierarchy of
     * the specified type. Returns a map with the class name as
     * key and a set of reference names as members, whereas the
     * references are composite references of the class.
     * 
     * @param ofType
     * @param hierarchy
     * @throws ServiceException
     */
    public void createCompositionHierarchy(
		ModelElement_1_0 ofType,
		Map<String,Set<String>> hierarchy
	) throws ServiceException {
    	Model_1_0 model = ofType.getModel();
    	// Add ofType to hierarchy
    	String currentTypeName = (String)ofType.getQualifiedName();
    	if(hierarchy.get(currentTypeName) == null) {
    		hierarchy.put(
				currentTypeName,
				new HashSet<String>()
			);
    	}
    	// Get all types which are involved in composition hierarchy
    	List<ModelElement_1_0> typesToCheck = new ArrayList<ModelElement_1_0>();
    	if(!ofType.objGetList("compositeReference").isEmpty()) {
    		typesToCheck.add(ofType);
    	} else {
    		@SuppressWarnings({ "unchecked", "rawtypes" })
    		List<ModelElement_1_0> subtypes = (List)ofType.objGetList("allSubtype");
    		for(ModelElement_1_0 subtype: subtypes) {
    			if(
					!ofType.getQualifiedName().equals(subtype.getQualifiedName()) &&
					!subtype.objGetList("compositeReference").isEmpty()
				) {
    				typesToCheck.add(subtype);
    			}
    		}
    	}
    	for(ModelElement_1_0 type: typesToCheck) {
    		ModelElement_1_0 compositeReference = model.getElement(type.objGetValue("compositeReference"));
    		ModelElement_1_0 exposingType = model.getElement(compositeReference.getContainer());
    		this.createCompositionHierarchy(
				exposingType,
				hierarchy
			);
    		hierarchy.get(
				exposingType.getQualifiedName()
			).add(
				(String)compositeReference.getName()
			);
    	}
    }

    /**
     * Map struct to map.
     * 
     * @param from
     * @param to
     * @param structDef
     * @throws ServiceException
     */
    public void structToMap(
		RefStruct from,
		Map<String,Object> to,
		ModelElement_1_0 structDef
	) throws ServiceException {
    	@SuppressWarnings("unchecked")
    	List<ModelElement_1_0> fields = (List<ModelElement_1_0>)structDef.objGetMap("field").values();
    	for(ModelElement_1_0 field: fields) {
    		String fieldName = (String)field.getQualifiedName();
    		try {
    			to.put(
					fieldName,
					from.refGetValue(fieldName)
				);
    		} catch(Exception e) {
    			to.put(fieldName, null);
    		}
    	}
    }

    /**
     * Get inspector definition.
     * 
     * @return
     */
    public org.openmdx.ui1.jmi1.Inspector getInspector(
    ) {
        return this.inspector;
    }
    
    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.Control#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		if(type == AttributePaneControl.class) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.attributePaneControls;
			return children;			
		} else {
			return Collections.emptyList();
		}
	}

	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
	private static final long serialVersionUID = -8832256948156958703L;

	protected final org.openmdx.ui1.jmi1.Inspector inspector;
    protected final List<AttributePaneControl> attributePaneControls;

}

//--- End of File -----------------------------------------------------------
