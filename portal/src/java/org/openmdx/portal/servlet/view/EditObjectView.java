/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: EditObjectView
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
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.CancelAction;
import org.openmdx.portal.servlet.action.SaveAction;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.control.EditInspectorControl;

public class EditObjectView
    extends ObjectView
    implements Serializable {
  
    //-------------------------------------------------------------------------
    /*
     * Constructor for existing objects.
     */
    public EditObjectView(
        String id,
        String containerElementId,
        Path editObjectIdentity,
        ApplicationContext application,
        Map<Path,Action> historyActions,
        String lookupType,
        String resourcePathPrefix,
        String navigationTarget,
        ViewMode mode
    ) throws ServiceException {
        this(
            id,
            containerElementId,
            // Handle edit with its own persistence manager
            (RefObject_1_0)application.getNewPmData().getObjectById(editObjectIdentity),
            editObjectIdentity,
            application,
            historyActions,
            lookupType,
            null, // parent
            null, // forReference
            resourcePathPrefix,
            navigationTarget,
            mode
        );
    }
  
    //-------------------------------------------------------------------------
    /**
     * editObjectRefMofId != null ==>
     * 1. read object with given refMofId
     * 2. apply changes to this object
     * 3. commit
     * <p>
     * editObjectRefMofId == null ==>
     *    parent == null (edit mode)
     *    1. apply changes to object
     *    2. commit
     *    parent != null (new mode)
     *    1. add object to parent.reference
     *    2. commit
     */
    public EditObjectView(
        String id,
        String containerElementId,
        RefObject_1_0 object,
        Path editObjectIdentity,
        ApplicationContext application,
        Map<Path,Action> historyActions,
        String lookupType,
        RefObject_1_0 parent,
        String forReference,
        String resourcePathPrefix,
        String navigationTarget,
        ViewMode mode
    ) throws ServiceException {
        super(
            id,
            containerElementId,
            object,
            application,
            historyActions,
            lookupType,
            resourcePathPrefix,
            navigationTarget,
            false // isReadOnly
        );
        this.editObjectIdentity = editObjectIdentity;    
        this.forReference = forReference;
        this.mode = mode;
        this.parent = parent;
        EditInspectorControl inspectorControl = this.app.createEditInspectorControl(
            id,
            object.refClass().refMofId()
        );
        this.inspectorControl = inspectorControl;
        // Attribute pane
        SysLog.detail("Preparing attribute pane");
        this.attributePane = new AttributePane(
            inspectorControl.getAttributePaneControl(),
            this,
            object
        );
    }
  
  //-------------------------------------------------------------------------
  public boolean isEditMode(
  ) {
    return this.parent == null;
  }
  
  //-------------------------------------------------------------------------
  public RefObject_1_0 getParent(
  ) {
      return this.parent;
  }
  
    //-------------------------------------------------------------------------
  	@Override
  	public PersistenceManager refresh(
  		boolean refreshData,
  		boolean closePm
  	) throws ServiceException {
  		for(int i = 0; i < this.getAttributePane().getAttributeTab().length; i++) {
  			this.getAttributePane().getAttributeTab()[i].refresh(refreshData);
  		}
  		return null;
  	}

	// -------------------------------------------------------------------------
	public void storeObject(
	    Map parameterMap,
	    Map<String, Attribute> attributeMap
	) throws ServiceException {
		// Collect all attributes
		for(AttributeTab tab: this.getAttributePane().getAttributeTab()) {
			for(FieldGroup fieldGroup: tab.getFieldGroup()) {
				Attribute[][] attributes = fieldGroup.getAttribute();
				for (int u = 0; u < attributes.length; u++) {
					for (int v = 0; v < attributes[u].length; v++) {
						Attribute attribute = attributes[u][v];
						if((attribute != null) && !attribute.isEmpty()) {
							attributeMap.put(
							    attribute.getValue().getName(),
							    attribute);
						}
					}
				}
			}
		}
		RefObject_1_0 target = this.getRefObject();
		if(this.isEditMode()) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(target);
			try {
				pm.currentTransaction().begin();
				this.app.getPortalExtension().updateObject(
				    target,
				    parameterMap,
				    attributeMap,
				    this.app
				);
				pm.currentTransaction().commit();
			} catch(Exception e) {
				try {
					pm.currentTransaction().rollback();
				} catch(Exception e1) {}
				throw new ServiceException(e);
			}			
		} else {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this.parent);
			try {
				pm.currentTransaction().begin();
				this.app.getPortalExtension().updateObject(
				    target,
				    parameterMap,
				    attributeMap,
				    this.app
				);
				if(this.app.getErrorMessages().isEmpty()) {
					Object[] qualifiers = (Object[])parameterMap.get("qualifier");
					if(qualifiers == null) {
						qualifiers = new String[] {
							UUIDConversion.toUID(UUIDs.newUUID())
						};
					}					
					pm.refresh(this.parent); // no CONCURRENT_MODIFICATION in case the parent was updated by some other user 
					Object container = this.parent.refGetValue(this.forReference);
					((RefContainer)container).refAdd(
					    org.oasisopen.cci2.QualifierType.REASSIGNABLE,
					    qualifiers.length > 0 ? (String) qualifiers[0] : "",
					    target
					);
					pm.currentTransaction().commit();
				} else {
					try {
						pm.currentTransaction().rollback();
					} catch(Exception e1) {}
				}
			} catch(Exception e) {
				try {
					pm.currentTransaction().rollback();				
				} catch(Exception e1) {}
				throw new ServiceException(e);
			}
		}
		this.editObjectIdentity = target.refGetPath();
	}
  
	//-------------------------------------------------------------------------
	public String getType(
	) {
		return ObjectView.VIEW_EDIT_OBJECT;
	}

	//-------------------------------------------------------------------------
	public Action getCancelAction(
	) {
		return new Action(
			CancelAction.EVENT_ID,
			new Action.Parameter[]{},
			this.app.getTexts().getCancelTitle(),
			WebKeys.ICON_CANCEL,
			true
		); 
	}

	//-------------------------------------------------------------------------
	public Action getSaveAction(
	) {
		return new Action(
			SaveAction.EVENT_ID,
			new Action.Parameter[]{},
			this.app.getTexts().getSaveTitle(),
			WebKeys.ICON_SAVE,
			true
		);
	}

	// -------------------------------------------------------------------------
	public EditInspectorControl getEditInspectorControl(
	) {
		return (EditInspectorControl)this.inspectorControl;
	}

    //-------------------------------------------------------------------------
    public RefObject_1_0 getParentObject(
    ) {
        return this.parent;
    }
    
    //-------------------------------------------------------------------------
    public String getForReference(
    ) {
        return this.forReference;
    }
    
    //-------------------------------------------------------------------------
    public ViewMode getMode(
    ) {
        return this.mode;
    }
    
    //-------------------------------------------------------------------------
    public Path getEditObjectIdentity(
    ) {
        return this.editObjectIdentity;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public RefObject_1_0 getLookupObject(
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(this.getRefObject());
        return this.isEditMode() ? 
            this.getEditObjectIdentity() != null ?
                (RefObject_1_0)pm.getObjectById(this.getEditObjectIdentity()) : 
                	this.getObjectReference().getObject() :
                		this.getParent();
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258411746451731505L;

    private RefObject_1_0 parent = null;
    private Path editObjectIdentity = null;
    private String forReference = null;
    private ViewMode mode = ViewMode.STANDARD;
  
}

//--- End of File -----------------------------------------------------------
