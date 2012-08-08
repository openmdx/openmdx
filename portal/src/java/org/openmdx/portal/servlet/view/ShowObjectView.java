/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ShowObjectView.java,v 1.60 2008/08/12 16:38:07 wfro Exp $
 * Description: ShowObjectView 
 * Revision:    $Revision: 1.60 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/12 16:38:07 $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.control.ControlFactory;
import org.openmdx.portal.servlet.control.ShowInspectorControl;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.uses.org.apache.commons.collections.MapUtils;

// ---------------------------------------------------------------------------
public class ShowObjectView 
    extends ObjectView 
    implements Serializable {

    // -------------------------------------------------------------------------
    public ShowObjectView(
        String id, 
        String containerElementId,
        Path objectIdentity, 
        ApplicationContext application, 
        Map<Path,Action> historyActions, 
        String lookupType,
        Map restrictToElements, 
        ControlFactory controlFactory
    ) throws ServiceException {
        super(
            id, 
            containerElementId,
            (RefObject_1_0)application.getPmData().getObjectById(
                objectIdentity
            ), 
            application, 
            historyActions,
            lookupType,
            restrictToElements,
            controlFactory
        );
        ShowInspectorControl inspectorControl = controlFactory.createShowInspectorControl(
            id,
            application.getCurrentLocaleAsString(),
            application.getCurrentLocaleAsIndex(),
            this.objectReference.getInspector(),
            this.getRefObject().refClass().refMofId()
        );
        this.inspectorControl = inspectorControl;
        
        // Attribute pane
        AppLog.detail("Preparing attribute pane");
        this.attributePane = new AttributePane(
            inspectorControl.getAttributePaneControl(),
            this,
            null //FieldGroup reads feature values from this.getObjectReference().getObject()
        );

        // Operation pane
        AppLog.detail("Preparing operation panes");
        this.operationPane = new OperationPane[inspectorControl.getOperationPaneControl().length];
        for (int i = 0; i < this.operationPane.length; i++) {
            this.operationPane[i] = new OperationPane(
                inspectorControl.getOperationPaneControl()[i],
                this
            );
        }

        // Reference pane
        AppLog.detail("Preparing reference panes");
        this.referencePane = new ReferencePane[inspectorControl.getReferencePaneControl().length];
        for(int i = 0; i < this.referencePane.length; i++) {
            this.referencePane[i] = new ReferencePane(
                inspectorControl.getReferencePaneControl()[i],
                this,
                lookupType
            );
        }
    }

    // -------------------------------------------------------------------------
    public void refresh(
        boolean refreshData
    ) throws ServiceException {
        super.refresh(refreshData);
        try {
            this.attributePane.refresh(
                refreshData
            );
            for (int i = 0; i < this.referencePane.length; i++) {
                this.referencePane[i].refresh(refreshData);
            }
        }
        catch (ServiceException e) {
            AppLog.detail("can not refresh", e.getMessage());
            new ServiceException(e).log();
        }
    }

    // -------------------------------------------------------------------------
    public void handleCanNotInvokeOperationException(
        BasicException e, 
        String operationName
    ) {
        BasicException e0 = e.getCause(this.application.getExceptionDomain());
        if (e0 == null) {
            this.application.addErrorMessage(
                application.getTexts().getErrorTextCanNotInvokeOperation(), 
                new String[] { 
                    this.getRefObject().refMofId(),
                    operationName, e.getMessage() 
                }
           );
        }
        else if (application.getTexts().getUserDefinedText(e0.getExceptionCode() + "") != null) {
            List<String> parameters = new ArrayList<String>();
            int i = 0;
            while (e0.getParameter("param" + i) != null) {
                parameters.add(e0.getParameter("param" + i));
                i++;
            }
            this.application.addErrorMessage(
                this.application.getTexts().getUserDefinedText(
                    e0.getExceptionCode() + ""), 
                    (String[]) parameters.toArray(new String[parameters.size()]
                )
            );
        }
        else {
            this.application.addErrorMessage(
                this.application.getTexts().getErrorTextCanNotInvokeOperation(), 
                new String[] { 
                    this.getRefObject().refMofId(),
                    operationName, 
                    e.getMessage() 
                }
            );
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Map createHistoryAppendCurrent(
    ) {
        Map historyActions = MapUtils.orderedMap(new HashMap());
        historyActions.putAll(this.historyActions);
        // Remove first (oldest) history entry in case MAX_HISTORY_ENTRIES is
        // reached
        if (historyActions.size() >= MAX_HISTORY_ENTRIES) {
            Iterator i = historyActions.entrySet().iterator();
            i.next();
            i.remove();
        }
        // Remove current and add it again. This moves current to the end
        historyActions.remove(
            this.objectReference.getObject().refGetPath()
        );
        historyActions.put(
            this.objectReference.getObject().refGetPath(), 
            new Action(
                Action.EVENT_SELECT_OBJECT,
                new Action.Parameter[] { 
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.getRefObject().refMofId()),
                    new Action.Parameter(Action.PARAMETER_REQUEST_ID, this.requestId)
                },
                this.objectReference.getTitle() + (this.objectReference.getTitle().length() > 0 ? " - " : "") + this.objectReference.getLabel(),
                this.objectReference.getIconKey(),
                true
            )
        );
        return historyActions;
    }

    //-------------------------------------------------------------------------
    public void handleCanNotCommitException(
        BasicException e
    ) {
      BasicException e0 = e.getCause(this.application.getExceptionDomain());
      if(e0 == null) {
          this.application.addErrorMessage(
            application.getTexts().getErrorTextCanNotCreateOrEditObject(),
            new String[]{e.getMessage()}
          );
      }
      else if(this.application.getTexts().getUserDefinedText(e0.getExceptionCode() + "") != null) {
          List<String> parameters = new ArrayList<String>();
          int i = 0;
          while(e0.getParameter("param" + i) != null) {
              parameters.add(e0.getParameter("param" + i));
              i++;
          }
          this.application.addErrorMessage(
              application.getTexts().getUserDefinedText(e0.getExceptionCode() + ""),
              parameters.toArray(new String[parameters.size()])
          );             
      }
      else {
          this.application.addErrorMessage(
              application.getTexts().getErrorTextCanNotCreateOrEditObject(),
              new String[]{e.getMessage()}
          );              
      }
    }
  
    // -------------------------------------------------------------------------
    public Action[] getSelectLocaleAction(
    ) {
        Texts_1_0[] texts = this.application.getTextsFactory().getTexts();
        Map<String,Action> actions = new TreeMap<String,Action>();
        for (int i = 0; i < texts.length; i++) {
            String locale = texts[i].getLocale();
            if ((locale != null) && !actions.keySet().contains(locale)) {
                actions.put(
                    locale, 
                    new Action(
                        Action.EVENT_SELECT_LOCALE, 
                        new Action.Parameter[]{ 
                           new Action.Parameter(Action.PARAMETER_LOCALE, locale) 
                        }, 
                        texts[i].getLocaleTitle(), 
                        true
                    )
                );
            }
        }
        return actions.values().toArray(new Action[actions.size()]);
    }

    // -------------------------------------------------------------------------
    public OperationPane[] getOperationPane(
    ) {
        return this.operationPane;
    }

    // -------------------------------------------------------------------------
    public ReferencePane[] getReferencePane(
    ) {
        return this.referencePane;
    }

    // -------------------------------------------------------------------------
    public ShowInspectorControl getShowInspectorControl(
    ) {
        return (ShowInspectorControl)this.inspectorControl;
    }

    // -------------------------------------------------------------------------
    public void selectReferencePane(
        int paneIndex
    ) {
        if (paneIndex < this.referencePane.length) {
            this.selectedReferencePaneIndex = paneIndex;
        }
    }

    // -------------------------------------------------------------------------
    public int getCurrentReferencePane() {
        return this.selectedReferencePaneIndex;
    }

    // -------------------------------------------------------------------------
    public Grid selectReferencePane(
        String referenceName
    ) {
        if(referenceName != null) {
            for(int i = 0; i < this.referencePane.length; i++) {
                Action[] selectReferenceAction = this.referencePane[i].getSelectReferenceAction();
                for(int j = 0; j < selectReferenceAction.length; j++) {
                    if(referenceName.equals(selectReferenceAction[j].getParameter(Action.PARAMETER_REFERENCE_NAME))) {
                        this.referencePane[i].selectReference(j);
                        return this.referencePane[i].getGrid();
                    }
                }
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    /**
     * The tab of the last invoked operation is set with setOperationTabResult.
     * resetOperationTabResult returns the tab of the last invoked operation and
     * resets it to null.
     */
    public OperationTab resetOperationTabResult(
    ) {
        OperationTab value = this.operationTabResult;
        this.operationTabResult = null;
        return value;
    }

    // -------------------------------------------------------------------------
    public void setOperationTabResult(
        OperationTab operationTabResult
    ) {
        this.operationTabResult = operationTabResult;
    }

    // -------------------------------------------------------------------------
    /**
     * The result of the last created object is set with setCreateObjectResult.
     * resetCreateObjectResult returns the reference of the last created object
     * and resets it null.
     */
    public ObjectCreationResult resetObjectCreationResult(
    ) {
        ObjectCreationResult value = this.objectCreationResult;
        this.objectCreationResult = null;
        return value;
    }

    // -------------------------------------------------------------------------
    public void setCreateObjectResult(
        ObjectCreationResult result
    ) {
        this.objectCreationResult = result;
    }

    // -------------------------------------------------------------------------
    public void selectFilter(
        String filterName, 
        String filterValues
    ) {
    }

    // -------------------------------------------------------------------------
    public String getType(
    ) {
        return ObjectView.VIEW_SHOW_OBJECT;
    }

    //-------------------------------------------------------------------------
    public RefObject_1_0 getLookupObject(
    ) {
        return this.getObjectReference().getObject();
    }
    
    // -------------------------------------------------------------------------
    // Variables
    // -------------------------------------------------------------------------
    private static final long serialVersionUID = 3257844376976635442L;
    
    private static final int MAX_HISTORY_ENTRIES = 10;

    protected final OperationPane[] operationPane;
    protected final ReferencePane[] referencePane;
    protected int selectedReferencePaneIndex = 0;
    protected OperationTab operationTabResult = null;
    protected ObjectCreationResult objectCreationResult = null;
    
}

// --- End of File -----------------------------------------------------------
