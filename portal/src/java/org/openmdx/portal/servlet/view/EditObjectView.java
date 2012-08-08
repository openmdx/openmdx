/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: EditObjectView.java,v 1.28 2008/02/09 00:50:32 wfro Exp $
 * Description: EditObjectView
 * Revision:    $Revision: 1.28 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/09 00:50:32 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.util.Map;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.RefContainer_1_0;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.control.ControlFactory;
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
        RefObject_1_0 object,
        String editObjectRefMofId,
        ApplicationContext application,
        Map historyActions,
        String lookupType,
        Map restrictToElements, 
        boolean isModal,
        ControlFactory controlFactory
    ) {
        this(
            id,
            containerElementId,
            object,
            editObjectRefMofId,
            application,
            historyActions,
            lookupType,
            restrictToElements,       
            null,
            null,
            isModal,
            controlFactory
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
        String editObjectRefMofId,
        ApplicationContext application,
        Map historyActions,
        String lookupType,
        Map restrictToElements,       
        RefObject_1_0 parent,
        String forReference,
        boolean isModal,
        ControlFactory controlFactory
    ) {
        super(
            id,
            containerElementId,
            object,
            application,
            historyActions,
            lookupType,
            restrictToElements,
            controlFactory
        );
        this.editObjectRefMofId = editObjectRefMofId;    
        this.forReference = forReference;
        this.isModal = isModal;
        this.parent = parent;
        EditInspectorControl inspectorControl = controlFactory.createEditInspectorControl(
            id,
            application.getCurrentLocaleAsString(),
            application.getCurrentLocaleAsIndex(),
            this.objectReference.getInspector(),
            object.refClass().refMofId()
        );
        this.inspectorControl = inspectorControl;
        // Attribute pane
        AppLog.detail("Preparing attribute pane");
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
  
  public RefObject_1_0 getParent(
  ) {
      return this.parent;
  }
  
  //-------------------------------------------------------------------------
  public void refresh(
      boolean refreshData
  ) throws ServiceException {
      for(int i = 0; i < this.getAttributePane().getAttributeTab().length; i++) {
          this.getAttributePane().getAttributeTab()[i].refresh(refreshData);
      }
  }

  //-------------------------------------------------------------------------
  public void storeObject(
      Map parameterMap,
      Map attributeMap
  ) {
      RefPackage_1_0 pkg = (RefPackage_1_0)this.getRefObject().refOutermostPackage();
      pkg.refBegin();

      // Collect all attributes
      for(int i = 0; i < this.getAttributePane().getAttributeTab().length; i++) {
        AttributeTab tab = this.getAttributePane().getAttributeTab()[i];
        for(int j = 0; j < tab.getFieldGroup().length; j++) {
          FieldGroup fieldGroup = tab.getFieldGroup()[j];
          Attribute[][] attributes = fieldGroup.getAttribute();
          for(int u = 0; u < attributes.length; u++) {
            for(int v = 0; v < attributes[u].length; v++) {
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
      RefObject_1_0 target = this.editObjectRefMofId == null
          ? this.getRefObject()
          : (RefObject_1_0)pkg.refObject(this.editObjectRefMofId);
      this.editObjectRefMofId = target.refMofId();
      this.application.getPortalExtension().updateObject(
          target,
          parameterMap,
          attributeMap,
          this.application,
          (RefPackage_1_0)this.getRefObject().refOutermostPackage()
      );
      if(!this.isEditMode()) {
          Object[] qualifiers = (Object[])parameterMap.get("qualifier");
          if(qualifiers == null) {
              qualifiers = new String[]{
                  UUIDConversion.toUID(UUIDs.getGenerator().next())
              };    
          }
          // Assert that this.parent is retrieved from the same package as this.object
          // A reload during edit can change the package which would lead to an exception
          // when adding the object with refAddValue
          this.parent = (RefObject_1_0)((RefPackage_1_0)this.getRefObject().refOutermostPackage()).refObject(this.parent.refMofId());
          ((RefContainer_1_0)this.parent.refGetValue(this.forReference)).refAddValue(
              qualifiers.length > 0 ? (String)qualifiers[0] : "",
              (RefObject_1_0)this.object
          );
      }
      pkg.refCommit();    
  }
  
  //-------------------------------------------------------------------------
  public String getIconKey(
  ) {
    return this.getObjectReference().getInspector().getIconKey();
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
          Action.EVENT_CANCEL,
          new Action.Parameter[]{},
          this.application.getTexts().getCancelTitle(),
          WebKeys.ICON_CANCEL,
          true
     ); 
  }

  //-------------------------------------------------------------------------
  public Action getSaveAction(
  ) {
      return new Action(
          Action.EVENT_SAVE,
          new Action.Parameter[]{},
          this.application.getTexts().getSaveTitle(),
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
    public boolean isModal(
    ) {
        return this.isModal;
    }
    
    //-------------------------------------------------------------------------
    public String getEditObjectRefMofId(
    ) {
        return this.editObjectRefMofId;
    }
    
    //-------------------------------------------------------------------------
    public RefObject_1_0 getLookupObject(
    ) {
        return this.isEditMode()
            ? this.getObjectReference().getObject()
            : this.getParent();
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258411746451731505L;

    private RefObject_1_0 parent = null;
    private String editObjectRefMofId = null;
    private String forReference = null;
    private boolean isModal = false;
  
}

//--- End of File -----------------------------------------------------------