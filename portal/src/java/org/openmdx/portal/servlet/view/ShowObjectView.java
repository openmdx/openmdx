/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ShowObjectView.java,v 1.79 2011/08/19 22:50:47 wfro Exp $
 * Description: ShowObjectView 
 * Revision:    $Revision: 1.79 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/08/19 22:50:47 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.SelectLocaleAction;
import org.openmdx.portal.servlet.action.SelectObjectAction;
import org.openmdx.portal.servlet.action.SelectPerspectiveAction;
import org.openmdx.portal.servlet.action.SelectViewportAction;
import org.openmdx.portal.servlet.control.ShowInspectorControl;
import org.openmdx.portal.servlet.texts.Texts_1_0;

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
        Map restrictToElements 
    ) throws ServiceException {
        super(
            id, 
            containerElementId,
            objectIdentity,
            application, 
            historyActions,
            lookupType,
            restrictToElements
        );
        ShowInspectorControl inspectorControl = this.app.createShowInspectorControl(
            id, 
            this.getRefObject().refClass().refMofId()
        );
        this.inspectorControl = inspectorControl;
        
        // Attribute pane
        SysLog.detail("Preparing attribute pane");
        this.attributePane = new AttributePane(
            inspectorControl.getAttributePaneControl(),
            this,
            null //FieldGroup reads feature values from this.getObjectReference().getObject()
        );

        // Operation pane
        SysLog.detail("Preparing operation panes");
        this.operationPane = new OperationPane[inspectorControl.getOperationPaneControl().length];
        for (int i = 0; i < this.operationPane.length; i++) {
            this.operationPane[i] = new OperationPane(
                inspectorControl.getOperationPaneControl()[i],
                this
            );
        }

        // Reference pane
        SysLog.detail("Preparing reference panes");
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
    @Override
    public PersistenceManager refresh(
        boolean refreshData,
        boolean closePm
    ) throws ServiceException {
        PersistenceManager oldPm = super.refresh(
        	refreshData,
        	closePm
        );
        try {
            this.attributePane.refresh(
                refreshData
            );
            for (int i = 0; i < this.referencePane.length; i++) {
                this.referencePane[i].refresh(refreshData);
            }
        }
        catch(ServiceException e) {
        	SysLog.detail("can not refresh", e.getMessage());
            new ServiceException(e).log();
        }
        return oldPm;
    }

    // -------------------------------------------------------------------------
    public void handleCanNotInvokeOperationException(
        BasicException e, 
        String operationName
    ) {
        BasicException e0 = e.getCause(this.app.getExceptionDomain());
        if (e0 == null) {
            this.app.addErrorMessage(
                app.getTexts().getErrorTextCanNotInvokeOperation(), 
                new String[] { 
                    this.getRefObject().refMofId(),
                    operationName, e.getMessage() 
                }
           );
        }
        else if (app.getTexts().getUserDefinedText(e0.getExceptionCode() + "") != null) {
            List<String> parameters = new ArrayList<String>();
            int i = 0;
            while (e0.getParameter("param" + i) != null) {
                parameters.add(e0.getParameter("param" + i));
                i++;
            }
            this.app.addErrorMessage(
                this.app.getTexts().getUserDefinedText(
                    e0.getExceptionCode() + ""), 
                    (String[]) parameters.toArray(new String[parameters.size()]
                )
            );
        }
        else {
            this.app.addErrorMessage(
                this.app.getTexts().getErrorTextCanNotInvokeOperation(), 
                new String[] { 
                    this.getRefObject().refMofId(),
                    operationName, 
                    e.getMessage() 
                }
            );
        }
    }

    // -------------------------------------------------------------------------
    public Map<Path,Action> createHistoryAppendCurrent(
    ) {
        Map<Path,Action> historyActions = new LinkedHashMap<Path,Action>();
        historyActions.putAll(this.historyActions);
        if(this.objectReference == null || this.objectReference.getObject() == null) {
        	return historyActions;
        }
        // Remove first (oldest) history entry in case MAX_HISTORY_ENTRIES is
        // reached
        if(historyActions.size() >= MAX_HISTORY_ENTRIES) {
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
                SelectObjectAction.EVENT_ID,
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
      BasicException e0 = e.getCause(this.app.getExceptionDomain());
      if(e0 == null) {
          this.app.addErrorMessage(
            app.getTexts().getErrorTextCanNotCreateOrEditObject(),
            new String[]{e.getMessage()}
          );
      }
      else if(this.app.getTexts().getUserDefinedText(e0.getExceptionCode() + "") != null) {
          List<String> parameters = new ArrayList<String>();
          int i = 0;
          while(e0.getParameter("param" + i) != null) {
              parameters.add(e0.getParameter("param" + i));
              i++;
          }
          this.app.addErrorMessage(
              app.getTexts().getUserDefinedText(e0.getExceptionCode() + ""),
              parameters.toArray(new String[parameters.size()])
          );             
      }
      else {
          this.app.addErrorMessage(
              app.getTexts().getErrorTextCanNotCreateOrEditObject(),
              new String[]{e.getMessage()}
          );              
      }
    }
  
    // -------------------------------------------------------------------------
    public Action[] getSelectLocaleAction(
    ) {
        Texts_1_0[] texts = this.app.getTextsFactory().getTexts();
        Map<String,Action> actions = new TreeMap<String,Action>();
        for (int i = 0; i < texts.length; i++) {
            String locale = texts[i].getLocale();
            if ((locale != null) && !actions.keySet().contains(locale)) {
                actions.put(
                    locale, 
                    new Action(
                        SelectLocaleAction.EVENT_ID, 
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

    //-------------------------------------------------------------------------  
    public Action[] getSelectPerspectiveAction(        
    ) {
        Path[] uiSegmentPaths = this.getApplicationContext().getUiContext().getUiSegmentPaths();
        List<Action> selectPerspectiveActions = new ArrayList<Action>();
        int ii = 0;
        for(Path uiSegmentPath: uiSegmentPaths) {
            org.openmdx.ui1.jmi1.Segment uiSegment = null;
            try {
                uiSegment = this.getApplicationContext().getUiContext().getUiSegment(ii);
            } 
            catch(Exception e) {}
            boolean isRevokeShow = this.app.getPortalExtension().hasPermission(
            	uiSegment, 
            	this.app,
            	WebKeys.PERMISSION_REVOKE_SHOW 
            );
            selectPerspectiveActions.add(
                new Action(
                    SelectPerspectiveAction.EVENT_ID,
                    new Action.Parameter[]{
                        new Action.Parameter(Action.PARAMETER_ID, String.valueOf(ii))
                    },
                    uiSegmentPath.getBase(),
                    !isRevokeShow
                )
            );
            ii++;
        }
        return selectPerspectiveActions.toArray(new Action[selectPerspectiveActions.size()]);
    }
      
    //-------------------------------------------------------------------------  
    public Action getToggleViewPortAction(
    ) {
    	ViewPort.Type newViewPortType = this.app.getCurrentViewPortType() == ViewPort.Type.STANDARD ?
    		ViewPort.Type.MOBILE :
    			ViewPort.Type.STANDARD;        		
        return new Action(
            SelectViewportAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(
                	Action.PARAMETER_ID, 
                	newViewPortType.toString()
                )
            },
            newViewPortType.toString(),
            true
        );
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
