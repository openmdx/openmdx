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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Autocompleter_1_0;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.DataBinding;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.CancelAction;
import org.openmdx.portal.servlet.action.CreateAction;
import org.openmdx.portal.servlet.action.EditAsNewAction;
import org.openmdx.portal.servlet.action.SaveAction;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.control.AttributePaneControl;
import org.openmdx.portal.servlet.control.EditInspectorControl;
import org.openmdx.portal.servlet.control.EditObjectTitleControl;
import org.openmdx.portal.servlet.control.ShowErrorsControl;
import org.openmdx.ui1.jmi1.ElementDefinition;

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
        Map<Path,Action> nextPrevActions,
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
            nextPrevActions,
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
        Map<Path,Action> nextPrevActions,
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
            nextPrevActions,
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
        	this.setForReference(editObjectIdentity.getParent().getLastSegment().toClassicRepresentation());
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
        Map<Path,Action> nextPrevActions,
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
    		nextPrevActions,
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
        Map<Path,Action> nextPrevActions,
        String lookupType,
        RefObject_1_0 parent,
        String forReference,
        String resourcePathPrefix,
        String navigationTarget,
        ViewMode viewMode,
        boolean isEditMode
    ) throws ServiceException {
        super(
    		app.getPortalExtension().getControlFactory().createEditInspectorControl(
	            id,
	            app.getCurrentPerspective(),
	            app.getCurrentLocaleAsString(),
	            app.getCurrentLocaleAsIndex(),
	            app.getInspector(object.refClass().refMofId()),
	            object.refClass().refMofId()
	        ),
            id,
            containerElementId,
            object,
            app,
            historyActions,
            nextPrevActions,
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
        // Attribute pane
        SysLog.detail("Preparing attribute pane");
		List<AttributePane> attributePanes = new ArrayList<AttributePane>();
		for(AttributePaneControl attributePaneControl: this.control.getChildren(AttributePaneControl.class)) {
			attributePanes.add(
				attributePaneControl.newComponent(
		            this,
		            object
		        )
			);
		}
        this.attributePanes = attributePanes;
    }

    /**
     * Get control casted to EditInspectorControl.
     * 
     * @return
     */
    protected EditInspectorControl getEditInspectorControl(
    ) {
    	return (EditInspectorControl)this.control;
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
  	public void refresh(
  		boolean refreshData
  	) throws ServiceException {
  		for(AttributePane attributePane: this.getChildren(AttributePane.class)) {
	  		for(UiAttributeTab attributeTab: attributePane.getChildren(UiAttributeTab.class)) {
	  			attributeTab.refresh(refreshData);
	  		}
  		}
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
		for(AttributePane attributePane: this.getChildren(AttributePane.class)) {
			for(UiAttributeTab attributeTab: attributePane.getChildren(UiAttributeTab.class)) {
				for(UiFieldGroup fieldGroup: attributeTab.getChildren(UiFieldGroup.class)) {
					fieldGroup.initAttributeMap(
						attributeMap, 
						this.app
					);
				}
			}
		}
		if(doCreate) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this.parent);
			// Create a new instance if existing object is persistent
			if(JDOHelper.isPersistent(this.object)) {
				this.object = (RefObject_1_0)this.getObject().refClass().refCreateInstance(null);
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
					DataBinding dataBinding = null;
					try {
						ElementDefinition elementDefinition = this.app.getUiElementDefinition(
							this.parent.refClass().refMofId() + ":" + this.forReference
						);
						dataBinding = elementDefinition == null 
							? null 
							: elementDefinition.getDataBindingName() == null 
								? null
								: this.app.getPortalExtension().getDataBinding(elementDefinition.getDataBindingName());
					} catch(Exception ignore) {}
					if(dataBinding != null) {
						dataBinding.setValue(
							this.parent, 
							this.forReference, 
							target,
							this.app
						);
					} else {
						Object container = this.parent.refGetValue(this.forReference);
						((RefContainer<?>)container).refAdd(
						    org.oasisopen.cci2.QualifierType.REASSIGNABLE,
						    qualifiers.length > 0 ? (String) qualifiers[0] : "",
						    target
						);
					}
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
	 * Get OK action.
	 * 
	 * @return
	 */
	public Action getOkAction(
	) {
		if(this.editObjectIdentity == null) {
			return null;
		} else {
			return new Action(
				SaveAction.EVENT_ID,
				new Action.Parameter[]{},
				this.app.getTexts().getOkTitle(),
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
    	PersistenceManager pm = JDOHelper.getPersistenceManager(this.getObject());
        return this.isEditMode() 
        	?  this.getEditObjectIdentity() != null 
        		? (RefObject_1_0)pm.getObjectById(this.getEditObjectIdentity()) 
        		: this.getObjectReference().getObject() 
        	: this.getParent();
    }

	/**
     * Get form name.
     * 
     * @param view
     * @return
     */
    public String getFormName(
    ) {
        String formName = this.getId();
        return this.getContainerElementId() == null
            ? formName
            : formName + "-" + this.getContainerElementId();        
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
        ApplicationContext app = p.getApplicationContext();
        Texts_1_0 texts = app.getTexts();
        EditInspectorControl control = this.getEditInspectorControl();
        HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
        if(forEditing) {
            String formId = this.getFormName();
            Model_1_0 model = app.getModel();
            // Render lookup field for existing objects
            boolean hasLookupField = false;
            if(this.getMode() == ViewMode.STANDARD) {            	
	            String forReferenceQualifiedName = null;
	            try {
	            	forReferenceQualifiedName = this.getParent() == null
		            	? null
		            	: (String)model.getFeatureDef(model.getElement(this.getParent().refClass().refMofId()), this.getForReference(), false).getQualifiedName();
	            } catch(Exception ignore) {}
	            if(forReferenceQualifiedName != null) {
		            Autocompleter_1_0 lookupExistingObjectAutocompleter = app.getPortalExtension().getAutocompleter(
		            	app, 
		            	this.getParent(), 
		            	forReferenceQualifiedName,
		            	this.getInspector().getForClass()
		            );
		            if(lookupExistingObjectAutocompleter != null) {
		            	String fieldId = formId + "LookupExisting";
		            	String formDetailsId = formId + "_details";
		            	String label = app.getTexts().getSelectExistingText();		            	
		            	p.write("<table class=\"", CssClass.tableLayout.toString(), "\" cellspacing=\"8\">");
		            	p.write("  <tr><td>");
		            	p.write("  <div class=\"", CssClass.qualifier.toString(), "\" style=\"font-weight:normal;width:100%;padding:10px;\">");
		            	p.write("    <table class=\"", CssClass.fieldGroup.toString(), "\">");
		            	p.write("      <tr>");
		                p.write("        <td class=\"", CssClass.fieldLabel.toString(), "\" title=\"", htmlEncoder.encode(label, false), "\"><span class=\"", CssClass.nw.toString(), "\">", htmlEncoder.encode(label, false), ":", "</span></td>");            
		                p.write("        <td>");
		                String onChangeValueScript =
		                	"xri=this.value;" +
		                    "$('" + formDetailsId + "').parentNode.className+=' wait';" +
		                	"$('" + formDetailsId + "').innerHTML='';" +
		                    "jQuery.ajax({type: 'get', url: '" + p.getResourcePathPrefix() + "'+getEncodedHRef(['" + WebKeys.SERVLET_NAME + "', '" + Action.PARAMETER_REQUEST_ID + "', '" + this.getRequestId() + "', 'event', '" + EditAsNewAction.EVENT_ID + "', 'parameter', 'xri*('+xri+')*mode*(" + ViewMode.EMBEDDED + ")']), dataType: 'html', success: function(data){$('" + formDetailsId + "').innerHTML=data;evalScripts(data);$('" + formDetailsId + "').parentNode.className='';}});";
		                lookupExistingObjectAutocompleter.paint(
		                    p,
		                    fieldId,
		                    1, // tabIndex
		                    forReferenceQualifiedName,
		                    null, // currentValue
		                    false,
		                    null,
		                    "class=\"" + CssClass.autocompleterInput + "\"",
		                    "class=\"" + CssClass.valueL + " " + CssClass.valueAC + "\"",
		                    null, // imgTag
		                    onChangeValueScript
		                );
		                p.write("        </td>");
		                p.write("        <td class=\"", CssClass.addon.toString(), "\">");
		                String lookupId = UUIDs.newUUID().toString();
	                    Action findObjectAction = Action.getFindObjectAction(
	                    	forReferenceQualifiedName, 
	                    	lookupId
	                    );
	                    p.write("  ", p.getImg("class=\"", CssClass.popUpButton.toString(), "\" border=\"0\" alt=\"Click to open ObjectFinder\" src=\"", p.getResourcePath("images/"), findObjectAction.getIconKey(), "\" onclick=\"javascript:OF.findObject(", p.getEvalHRef(findObjectAction), ", $('", fieldId, ".Title'), $('", fieldId, "'), '", lookupId, "');\""));                    		                
		                p.write("        </td>");
		                p.write("      </tr>");
		                p.write("    </table>");
		                p.write("  </div>");
		            	p.write("  <div>&nbsp;</div>");
		            	p.write("  </td></tr>");
		            	p.write("</table>");
		            	p.write("<div style=\"min-height:30px;\">");
		            	p.write("  <div id=\"", formDetailsId, "\">");
		            	hasLookupField = true;
		            }
	            }
            }
            p.write("<form name=\"", formId, "\" id=\"", formId, "\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"post\" action=\"\">");
            p.write("  <table cellspacing=\"8\" class=\"", CssClass.tableLayout.toString(), "\">");
            // Errors
            int count = 0;            
            if(!app.getErrorMessages().isEmpty()) {
            	for(ShowErrorsControl showErrorsControl: control.getChildren(ShowErrorsControl.class)) {
                    p.write("    <tr>");
                    p.write("      <td>", (count > 0 ? "<br />" : ""));
            		showErrorsControl.paint(p, null, forEditing);
                    p.write("      </td>");
                    p.write("    </tr>");
                    count++;
            	}
            }
            // Title
            if(this.viewMode != ViewMode.EMBEDDED || !this.isEditMode()) {
            	for(EditObjectTitleControl editObjectTitleControl: control.getChildren(EditObjectTitleControl.class)) {
                    p.write("    <tr>");
                    p.write("      <td>", (count > 0 ? "<br />" : ""));
                    editObjectTitleControl.paint(p, null, forEditing);
                    p.write("      </td>");
                    p.write("    </tr>");
                    count++;
            	}
            }
            for(AttributePane attributePane: this.getChildren(AttributePane.class)) {
                p.write("    <tr>");
                p.write("      <td>", (count > 0 ? "<br />" : ""));
                attributePane.paint(p, null, forEditing);
                p.write("      </td>");
                p.write("    </tr>");
                count++;
            }
            if(!this.isEditMode()) {
                boolean showQualifier = 
                    app.getPortalExtension().hasUserDefineableQualifier(
                        this.getInspector(), 
                        app
                    );
                p.write("    <tr>");
                p.write("      <td class=\"", CssClass.panel.toString(), "\">");
                if(showQualifier) {
                    p.write("        <span class=\"", CssClass.qualifierText.toString(), "\">", texts.getQualifierText(), "</span><br />");
                }
                p.write("        <input class=\"", CssClass.qualifier.toString(), "\" type=\"", (showQualifier ? "text" : "hidden"), "\" name=\"qualifier\" value=\"", org.openmdx.base.text.conversion.UUIDConversion.toUID(org.openmdx.kernel.id.UUIDs.newUUID()), "\">");
                p.write("      </td>");
                p.write("    </tr>");
            }
            p.write("  <tr>");
            p.write("    <td>");
            p.write("      <input type=\"hidden\" name=\"requestId.submit\" value=\"", this.getRequestId(), "\">");
            Action cancelAction = this.getCancelAction();
            Action okAction = this.getOkAction();
            Action createAction = this.getCreateAction();
            p.write("      <input type=\"hidden\" id=\"event.submit\" name=\"event.submit\" value=\"\" >");
            String containerElementId = this.getContainerElementId() == null
                ? "aPanel"
                : this.getContainerElementId();
            if(okAction != null) {
            	if(this.isEditMode()) {
            		p.write("      <a id=\"editSave-", containerElementId, "\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" href=\"#\" onclick=\"javascript:$('event.submit').value='", Integer.toString(okAction.getEvent()), "';var editForm=document.forms['", formId, "'];var params=Form.serialize(editForm);jQuery.ajax({type: 'post', url: ", p.getEvalHRef(okAction), ", dataType: 'html', data: params, success: function(data){$('", containerElementId, "').innerHTML=data;evalScripts(data);}});return false;\">", okAction.getTitle(), "</a>");
            	} else {
            		p.write("      <a id=\"editSave-", containerElementId, "\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" href=\"#\" onclick=\"javascript:$('event.submit').value='", Integer.toString(okAction.getEvent()), "';document.forms['", formId, "'].submit();\">", okAction.getTitle(), "</a>");
            	}
            }
            if(createAction != null) {
            	if(this.isEditMode()) {
            		p.write("      <a id=\"editCreate-", containerElementId, "\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" href=\"#\" onclick=\"javascript:$('event.submit').value='", Integer.toString(okAction.getEvent()), "';var editForm=document.forms['", formId, "'];var params=Form.serialize(editForm);jQuery.ajax({type: 'post', url: ",  p.getEvalHRef(createAction), ", dataType: 'html', data: params, success: function(data){$('", containerElementId, "').innerHTML=data;evalScripts(data);}});return false;\">", createAction.getTitle(), "</a>");
            	} else {
            		p.write("      <a id=\"editCreate-", containerElementId, "\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" href=\"#\" onclick=\"javascript:$('event.submit').value='", Integer.toString(createAction.getEvent()), "';document.forms['", formId, "'].submit();\">", createAction.getTitle(), "</a>");
            	}
            }
            if(cancelAction != null) {
            	if(this.isEditMode()) {
            		p.write("      <a id=\"editCancel-", containerElementId, "\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" href=\"#\" onclick=\"javascript:jQuery.ajax({type: 'get', url: ", p.getEvalHRef(cancelAction), ", dataType: 'html', success: function(data){$('", containerElementId, "').innerHTML=data;evalScripts(data);}});return false;\">", cancelAction.getTitle(), "</a>");            	
            	} else {
            		p.write("      <a id=\"editCancel-", containerElementId, "\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" href=\"#\" onclick=\"javascript:this.href=", p.getEvalHRef(cancelAction), ";\">", cancelAction.getTitle(), "</a>");                                          	
            	}
            }
            p.write("    </td>");
            p.write("  </tr>");
            p.write("  </table>");
            p.write("  <br />");
            p.write("</form>");
            if(this.getContainerElementId() != null) {
                p.write("<div class=\"", CssClass.gridSpacerBottom.toString(), "\"></div>");                        
            }
            if(hasLookupField) {
            	p.write("  </div>");
            	p.write("</div>");
            }
        }
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
