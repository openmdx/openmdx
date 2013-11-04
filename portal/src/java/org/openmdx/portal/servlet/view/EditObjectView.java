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
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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
import org.openmdx.portal.servlet.action.CreateAction;
import org.openmdx.portal.servlet.action.SaveAction;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.control.EditInspectorControl;

/**
 * EditObjectView
 *
 */
public class EditObjectView extends ObjectView implements Serializable {
  
    /**
     * Constructor 
     *
     * @param id
     * @param containerElementId
     * @param editObjectIdentity
     * @param app
     * @param historyActions
     * @param lookupType
     * @param resourcePathPrefix
     * @param navigationTarget
     * @param viewMode
     * @throws ServiceException
     */
    public EditObjectView(
        String id,
        String containerElementId,
        Path editObjectIdentity,
        ApplicationContext app,
        Map<Path,Action> historyActions,
        String lookupType,
        String resourcePathPrefix,
        String navigationTarget,
        ViewMode viewMode
    ) throws ServiceException {
        this(
            id,
            containerElementId,
            editObjectIdentity,
            app,
            historyActions,
            lookupType,
            resourcePathPrefix,
            navigationTarget,
            viewMode,
            true // isEditMode
        );
    }

    /**
     * Constructor 
     *
     * @param id
     * @param containerElementId
     * @param editObjectIdentity
     * @param app
     * @param historyActions
     * @param lookupType
     * @param resourcePathPrefix
     * @param navigationTarget
     * @param viewMode
     * @param isEditMode
     * @throws ServiceException
     */
    public EditObjectView(
        String id,
        String containerElementId,
        Path editObjectIdentity,
        ApplicationContext app,
        Map<Path,Action> historyActions,
        String lookupType,
        String resourcePathPrefix,
        String navigationTarget,
        ViewMode viewMode,
        boolean isEditMode
    ) throws ServiceException {
        this(
            id,
            containerElementId,
            // Handle edit with its own persistence manager
            (RefObject_1_0)app.getNewPmData().getObjectById(editObjectIdentity),
            editObjectIdentity,
            app,
            historyActions,
            lookupType,
            null, // parent
            null, // forReference
            resourcePathPrefix,
            navigationTarget,
            viewMode,
            isEditMode
        );
        if(!isEditMode) {
        	PersistenceManager pm = JDOHelper.getPersistenceManager(this.getObject());
        	this.setParent(
        		(RefObject_1_0)pm.getObjectById(editObjectIdentity.getParent().getParent())
        	);
        	this.setForReference(editObjectIdentity.getParent().getBase());
        }
    }

    /**
     * Constructor 
     *
     * @param id
     * @param containerElementId
     * @param object
     * @param editObjectIdentity
     * @param app
     * @param historyActions
     * @param lookupType
     * @param parent
     * @param forReference
     * @param resourcePathPrefix
     * @param navigationTarget
     * @param viewMode
     * @throws ServiceException
     */
    public EditObjectView(
        String id,
        String containerElementId,
        RefObject_1_0 object,
        Path editObjectIdentity,
        ApplicationContext app,
        Map<Path,Action> historyActions,
        String lookupType,
        RefObject_1_0 parent,
        String forReference,
        String resourcePathPrefix,
        String navigationTarget,
        ViewMode viewMode
    ) throws ServiceException {
    	this(
    		id, 
    		containerElementId,
    		object,
    		editObjectIdentity, 
    		app, 
    		historyActions, 
    		lookupType, 
    		parent,
    		forReference,
    		resourcePathPrefix, 
    		navigationTarget,
    		viewMode,
    		false
    	);
    }

    /**
     * Constructor 
     *
     * @param id
     * @param containerElementId
     * @param object
     * @param editObjectIdentity
     * @param app
     * @param historyActions
     * @param lookupType
     * @param parent
     * @param forReference
     * @param resourcePathPrefix
     * @param navigationTarget
     * @param viewMode
     * @param isEditMode
     * @throws ServiceException
     */
    public EditObjectView(
        String id,
        String containerElementId,
        RefObject_1_0 object,
        Path editObjectIdentity,
        ApplicationContext app,
        Map<Path,Action> historyActions,
        String lookupType,
        RefObject_1_0 parent,
        String forReference,
        String resourcePathPrefix,
        String navigationTarget,
        ViewMode viewMode,
        boolean isEditMode
    ) throws ServiceException {
        super(
            id,
            containerElementId,
            object,
            app,
            historyActions,
            lookupType,
            resourcePathPrefix,
            navigationTarget,
            false // isReadOnly
        );
        this.editObjectIdentity = editObjectIdentity;    
        this.forReference = forReference;
        this.viewMode = viewMode;
        this.isEditMode = isEditMode;
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

    /**
     * Return true if view is in edit mode.
     * 
     * @return
     */
    public boolean isEditMode(
   	) {
    	return this.isEditMode;
    }

    /**
     * Get parent object of this view.
     * 
     * @return
     */
    public RefObject_1_0 getParent(
    ) {
    	return this.parent;
    }
  
    /**
	 * Set parent.
	 * 
	 * @param parent The parent to set.
	 */
	public void setParent(
		RefObject_1_0 parent
	) {
		this.parent = parent;
	}

  	/* (non-Javadoc)
  	 * @see org.openmdx.portal.servlet.view.ObjectView#refresh(boolean, boolean)
  	 */
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

	/**
	 * Store object. Map parameter map this view's object.
	 * 
	 * @param parameterMap
	 * @param attributeMap
	 * @throws ServiceException
	 */
	public void storeObject(
	    Map<String,String[]> parameterMap,
	    Map<String, Attribute> attributeMap,
	    boolean doCreate
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
							    attribute
							);
						}
					}
				}
			}
		}
		if(doCreate) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this.parent);
			// Create a new instance if existing object is persistent
			if(JDOHelper.isPersistent(this.object)) {
				this.object = (RefObject_1_0)this.getRefObject().refClass().refCreateInstance(null);
				((RefObject_1_0)this.object).refInitialize(false, false);				
			}
			Object target = this.getObject();
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
					// Prevent CONCURRENT_MODIFICATION in case the parent was updated by some other user
					pm.refresh(this.parent);
					Object container = this.parent.refGetValue(this.forReference);
					((RefContainer)container).refAdd(
					    org.oasisopen.cci2.QualifierType.REASSIGNABLE,
					    qualifiers.length > 0 ? (String) qualifiers[0] : "",
					    target
					);
					pm.currentTransaction().commit();
					this.editObjectIdentity = ((RefObject_1_0)target).refGetPath();
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
		} else {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this.getObject());
			try {
				pm.currentTransaction().begin();
				this.app.getPortalExtension().updateObject(
					// Update persistent object
					this.editObjectIdentity == null
						? this.getObject()
						: pm.getObjectById(this.editObjectIdentity),
				    parameterMap,
				    attributeMap,
				    this.app
				);
				if(this.app.getErrorMessages().isEmpty()) {
					pm.currentTransaction().commit();
				} else {
					try {
						pm.currentTransaction().rollback();
					} catch(Exception ignore) {}
				}
			} catch(Exception e) {
				try {
					pm.currentTransaction().rollback();
				} catch(Exception e1) {}
				throw new ServiceException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.View#getType()
	 */
	@Override
	public String getType(
	) {
		return ObjectView.VIEW_EDIT_OBJECT;
	}

	/**
	 * Get cancel action.
	 * 
	 * @return
	 */
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

	/**
	 * Get save action.
	 * 
	 * @return
	 */
	public Action getSaveAction(
	) {
		if(this.editObjectIdentity == null) {
			return null;
		} else {
			return new Action(
				SaveAction.EVENT_ID,
				new Action.Parameter[]{},
				this.app.getTexts().getSaveTitle(),
				WebKeys.ICON_SAVE,
				true
			);
		}
	}

	/**
	 * Get save as new action.
	 * 
	 * @return
	 */
	public Action getCreateAction(
	) {
		if(this.isEditMode) {
			return null;
		} else {
			return new Action(
				CreateAction.EVENT_ID,
				new Action.Parameter[]{},
				this.app.getTexts().getNewText(),
				WebKeys.ICON_SAVE,
				true
			);
		}
	}

	/**
	 * Get edit inspector control.
	 * 
	 * @return
	 */
	public EditInspectorControl getEditInspectorControl(
	) {
		return (EditInspectorControl)this.inspectorControl;
	}

    /**
     * Get parent of this view's object.
     * 
     * @return
     */
    public RefObject_1_0 getParentObject(
    ) {
        return this.parent;
    }
    
    /**
     * Objects are added to this reference.
     * 
     * @return
     */
    public String getForReference(
    ) {
        return this.forReference;
    }
    
    /**
	 * Set forReference.
	 * 
	 * @param forReference The forReference to set.
	 */
	public void setForReference(
		String forReference
	) {
		this.forReference = forReference;
	}

    /**
     * Get view mode.
     * 
     * @return
     */
    public ViewMode getMode(
    ) {
        return this.viewMode;
    }
    
    /**
     * Get edit object identity.
     * 
     * @return
     */
    public Path getEditObjectIdentity(
    ) {
        return this.editObjectIdentity;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.ObjectView#getLookupObject()
     */
    @Override
    public RefObject_1_0 getLookupObject(
    ) {
    	PersistenceManager pm = JDOHelper.getPersistenceManager(this.getRefObject());
        return this.isEditMode() 
        	?  this.getEditObjectIdentity() != null 
        		? (RefObject_1_0)pm.getObjectById(this.getEditObjectIdentity()) 
        		: this.getObjectReference().getObject() 
        	: this.getParent();
    }
    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258411746451731505L;

    private RefObject_1_0 parent = null;
	private Path editObjectIdentity = null;
    private String forReference = null;
	private final ViewMode viewMode;
    private final boolean isEditMode;
  
}

//--- End of File -----------------------------------------------------------
