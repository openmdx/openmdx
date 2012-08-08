/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ShowObjectEventHandler.java,v 1.18 2008/01/27 00:37:48 wfro Exp $
 * Description: ShowObjectView 
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/27 00:37:48 $
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
package org.openmdx.portal.servlet.eventhandler;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefStruct;
import javax.servlet.http.HttpSession;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.url.protocol.XriProtocols;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.FieldGroup;
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.OperationTab;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.uses.org.apache.commons.collections.MapUtils;

public class ShowObjectEventHandler {

    // -------------------------------------------------------------------------
    public static ObjectView handleEvent(
        int event, 
        ShowObjectView currentView,        
        String parameter, 
        HttpSession session, 
        Map parameterMap, 
        ViewsCache showViewsCache 
    ) {
        ObjectView nextView = currentView;
        
        // handle events
        ApplicationContext application = currentView.getApplicationContext();
        RefPackage_1_0 pkg = (RefPackage_1_0) currentView.getRefObject().refOutermostPackage();
        switch (event) {

            case Action.EVENT_SELECT_OBJECT:
            case Action.EVENT_RELOAD:
            case Action.EVENT_SELECT_AND_EDIT_OBJECT:
            case Action.EVENT_SELECT_AND_NEW_OBJECT: {

                // parameter is of format
                // reference=n][;forReference=name][;forClass=name][;refMofId=id]
                String refMofId = Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI);
                try {
    
                    String paneIndexAsString = Action.getParameter(parameter, Action.PARAMETER_PANE);
                    String referenceAsString = Action.getParameter(parameter, Action.PARAMETER_REFERENCE);
                    String referenceName = Action.getParameter(parameter, Action.PARAMETER_REFERENCE_NAME);
                    String forReference = Action.getParameter(parameter, Action.PARAMETER_FOR_REFERENCE);
                    String forClass = Action.getParameter(parameter, Action.PARAMETER_FOR_CLASS);
                    String requestId = Action.getParameter(parameter, Action.PARAMETER_REQUEST_ID);
                   
                    RefPackage_1_0 dataPkg = application.getDataPackage();
                    Map historyActions = (requestId == null) || (requestId.length() == 0)
                        ? currentView.createHistoryAppendCurrent()
                        : new HashMap();
                    // Go back to requested view
                    if(
                        (requestId != null) && 
                        showViewsCache.containsView(requestId)
                    ) {
                       nextView = showViewsCache.getView(requestId);
                    }
                    // EVENT_SELECT_AND_EDIT_OBJECT
                    else if(event == Action.EVENT_SELECT_AND_EDIT_OBJECT) {
                        RefObject_1_0 nextObject = (RefObject_1_0) dataPkg.refObject(refMofId);                    
                        nextView = new EditObjectView(
                            currentView.getId(),
                            null,
                            nextObject,
                            nextObject.refMofId(),
                            application,
                            historyActions,
                            currentView.getLookupType(),
                            currentView.getRestrictToElements(),
                            false,
                            currentView.getControlFactory()
                        );
                    }
                    // EVENT_SELECT_AND_NEW_OBJECT
                    else if ((event == Action.EVENT_SELECT_AND_NEW_OBJECT) && (forClass.length() > 0) && (forReference.length() > 0)) {
                        RefObject_1_0 nextObject = (RefObject_1_0) dataPkg.refObject(refMofId);
                        RefObject_1_0 newObject = (RefObject_1_0) pkg.refClass(forClass).refCreateInstance(null);
                        nextView = new EditObjectView(
                            currentView.getId(),
                            null,
                            newObject,
                            null,
                            application,
                            historyActions,
                            currentView.getLookupType(),
                            currentView.getRestrictToElements(),
                            nextObject,
                            forReference,
                            false,
                            currentView.getControlFactory()
                        );
                    }
                    else {
                        nextView = new ShowObjectView(
                            currentView.getId(),
                            null,
                            refMofId,
                            application,
                            historyActions,
                            currentView.getLookupType(),
                            currentView.getRestrictToElements(),
                            currentView.getControlFactory()
                        );
                        // show same grid in refreshed object
                        if (event == Action.EVENT_RELOAD) {
                            ShowObjectView view = (ShowObjectView) nextView;
                            for (int i = 0; i < currentView.getReferencePane().length; i++) {
                                view.getReferencePane()[i].selectReference(currentView.getReferencePane()[i].getSelectedReference());
                            }
                        }
                        // SELECT_OBJECT and reference pane (specified with
                        // reference and pane index)
                        else if ((referenceAsString.length() > 0) && (paneIndexAsString.length() > 0)) {
                            int reference = Integer.parseInt(referenceAsString);
                            int paneIndex = Integer.parseInt(paneIndexAsString);
                            ShowObjectView view = (ShowObjectView) nextView;
                            if (paneIndex < view.getReferencePane().length) {
                                view.selectReferencePane(paneIndex);
                                view.getReferencePane()[paneIndex].selectReference(reference);
                            }
                        }
                        // SELECT_OBJECT and reference pane (specified with
                        // reference name)
                        else if (referenceName.length() > 0) {
                            ShowObjectView view = (ShowObjectView) nextView;
                            for (int i = 0; i < view.getReferencePane().length; i++) {
                                for (int j = 0; j < view.getReferencePane()[i].getSelectReferenceAction().length; j++) {
                                    Action selectReferenceAction = view.getReferencePane()[i].getSelectReferenceAction()[j];
                                    if (selectReferenceAction.getParameter(Action.PARAMETER_REFERENCE_NAME).endsWith(referenceName)) {
                                        view.selectReferencePane(i);
                                        view.getReferencePane()[i].selectReference(j);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotSelectObject(), new String[] { refMofId, e.getMessage() }
                    );
                    nextView = currentView;
                }
                break;
            }

            case Action.EVENT_MACRO: {

                // parameter is of format
                // [name=name][;type=type]
                String refMofId = Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI);
                try {
                    String actionName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                    if(actionName != null) {
                        actionName = new String(Base64.decode(actionName));
                    }
                    String actionType = Action.getParameter(parameter, Action.PARAMETER_TYPE);
                    nextView = new ShowObjectView(
                        currentView.getId(),
                        currentView.getContainerElementId(),
                        refMofId,
                        application,
                        currentView.createHistoryAppendCurrent(),
                        currentView.getLookupType(),
                        currentView.getRestrictToElements(),
                        currentView.getControlFactory()
                    );
                    nextView.setMacro(
                        new Object[]{
                            actionType == null ? new Short((short)0) : new Short(actionType),
                            actionName,
                            Collections.EMPTY_LIST
                        }
                    );
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotSelectObject(), new String[] { refMofId, e.getMessage() }
                    );
                    nextView = currentView;
                }
                break;
            }
                 
            case Action.EVENT_EDIT:
            case Action.EVENT_EDIT_MODAL:
                try {
                    nextView = new EditObjectView(
                        currentView.getId(),
                        currentView.getContainerElementId(),
                        currentView.getRefObject(),
                        null,
                        application,
                        currentView.createHistoryAppendCurrent(),
                        currentView.getLookupType(),
                        currentView.getRestrictToElements(),
                        event == Action.EVENT_EDIT_MODAL,
                        currentView.getControlFactory()
                    );
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotEditObject(), new String[] { currentView.getRefObject().refMofId(), e.getMessage() }
                    );
                }
                break;
    
            case Action.EVENT_NEW_OBJECT:
                try {
                    String forClass = Action.getParameter(parameter, Action.PARAMETER_FOR_CLASS);
                    String forReference = Action.getParameter(parameter, Action.PARAMETER_FOR_REFERENCE);
                    AppLog.detail("creating object", Action.PARAMETER_FOR_CLASS + "=" + forClass + "; " + Action.PARAMETER_FOR_REFERENCE + "=" + forReference);
                    RefObject_1_0 newObject = (RefObject_1_0) pkg.refClass(forClass).refCreateInstance(null);
                    nextView = new EditObjectView(
                        currentView.getId(),
                        currentView.getContainerElementId(),
                        newObject,
                        null,
                        application,
                        currentView.createHistoryAppendCurrent(),
                        currentView.getLookupType(),
                        currentView.getRestrictToElements(),
                        currentView.getRefObject(),
                        forReference,
                        false,
                        currentView.getControlFactory()
                    );
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotEditObject(), new String[] { currentView.getRefObject().refMofId(), e.getMessage() }
                    );
                }
                break;
    
            case Action.EVENT_SELECT_LOCALE:
                try {
                    String locale = Action.getParameter(parameter, Action.PARAMETER_LOCALE);
                    AppLog.trace("setting locale", locale);
                    application.setCurrentLocale(locale);
                    nextView = new ShowObjectView(
                        currentView.getId(),
                        currentView.getContainerElementId(),
                        currentView.getRefObject().refMofId(),
                        application,
                        currentView.getHistoryActions(),
                        currentView.getLookupType(),
                        currentView.getRestrictToElements(),
                        currentView.getControlFactory()
                    );
                    ShowObjectView view = (ShowObjectView) nextView;
                    for (int i = 0; i < currentView.getReferencePane().length; i++) {
                        view.getReferencePane()[i].selectReference(currentView.getReferencePane()[i].getSelectedReference());
                    }
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotSetLocale(),
                        new String[] { parameter, e.getMessage() });
                }
                break;
    
            case Action.EVENT_SELECT_GUI_MODE:
                try {
                    String guiLookName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                    AppLog.trace("setting gui look", guiLookName);
                    application.setCurrentGuiMode(guiLookName);
                    nextView = new ShowObjectView(
                        currentView.getId(),
                        currentView.getContainerElementId(),
                        currentView.getRefObject().refMofId(),
                        application,
                        currentView.getHistoryActions(),
                        currentView.getLookupType(),
                        currentView.getRestrictToElements(),
                        currentView.getControlFactory()
                    );
                    ShowObjectView view = (ShowObjectView) nextView;
                    for (int i = 0; i < currentView.getReferencePane().length; i++) {
                        view.getReferencePane()[i].selectReference(currentView.getReferencePane()[i].getSelectedReference());
                    }
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotSetLocale(),
                        new String[] { parameter, e.getMessage() });
                }
                break;
    
            case Action.EVENT_DELETE:
                try {
                    pkg.refBegin();
                    currentView.getRefObject().refDelete();
                    pkg.refCommit();
                    nextView = currentView.getPreviousView();
                }
                catch (JmiServiceException e) {
                    AppLog.warning(e.getMessage(), e.getCause(), 1);
                    try {
                        pkg.refRollback();
                    }
                    catch (Exception e0) {
                    }
                    currentView.handleCanNotCommitException(e.getExceptionStack());
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    try {
                        pkg.refRollback();
                    }
                    catch (Exception e1) {
                    }
                    currentView.handleCanNotCommitException(e0.getExceptionStack());
                }
                break;
    
            case Action.EVENT_MULTI_DELETE: {
                try {
                    StringTokenizer tokenizer = new StringTokenizer(parameter, " ");
                    while (tokenizer.hasMoreTokens()) {
                        String refMofId = Action.getParameter(tokenizer.nextToken(), Action.PARAMETER_OBJECTXRI);
                        pkg.refBegin();
                        pkg.refObject(refMofId).refDelete();
                        pkg.refCommit();
                    }
                    nextView.refresh(true);
                }
                catch (JmiServiceException e) {
                    AppLog.warning(e.getMessage(), e.getCause(), 1);
                    try {
                        pkg.refRollback();
                    }
                    catch (Exception e0) {
                    }
                    currentView.handleCanNotCommitException(e.getExceptionStack());
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    try {
                        pkg.refRollback();
                    }
                    catch (Exception e1) {
                    }
                    currentView.handleCanNotCommitException(e0.getExceptionStack());
                }
                break;
            }
    
            case Action.EVENT_INVOKE_OPERATION: {
    
                AppLog.detail("invoking operation", parameter);
                int tabIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_TAB));
                int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                if (paneIndex < currentView.getOperationPane().length) {
                    if (tabIndex >= currentView.getOperationPane()[paneIndex].getOperationTab().length) {
                        String message = "undefined operation";
                        String toolTip =  currentView.getOperationPane()[paneIndex].getOperationPaneControl().getToolTip();
                        String cause = "pane=" + toolTip + "; paneIndex=" + paneIndex + "; tabIndex=" + tabIndex;
                        AppLog.error(message, cause, 1);
                        application.addErrorMessage(
                            application.getTexts().getErrorTextCanNotInvokeOperation(), 
                            new String[] {
                                currentView.getRefObject().refMofId(), 
                                toolTip, 
                                message + "; " + cause 
                            }
                        );
                    }
                    else {
                        OperationTab tab = currentView.getOperationPane()[paneIndex].getOperationTab()[tabIndex];
                        try {
                            // only field group containing input parameter fields
                            Map fieldMap = new HashMap();
                            for (int j = 0; j < 1; j++) {
                                FieldGroup fieldGroup = tab.getFieldGroup()[j];
                                Attribute[][] attributes = fieldGroup.getAttribute();
                                for (int u = 0; u < attributes.length; u++) {
                                    for (int v = 0; v < attributes[u].length; v++) {
                                        Attribute attribute = attributes[u][v];
                                        if ((attribute != null) && !attribute.isEmpty()) {
                                            fieldMap.put(attribute.getValue().getName(), attribute);
                                        }
                                    }
                                }
                            }
    
                            // map request to parameter values
                            Map paramValuesMap = new HashMap();
                            application.getPortalExtension().updateObject(
                                paramValuesMap,
                                parameterMap,
                                fieldMap,
                                application,
                                (RefPackage_1_0) currentView.getRefObject().refOutermostPackage());
    
                            // Set current filter of current pane as attribute value
                            // of field
                            // 'attributeFilter' if paramValuesMap contains the
                            // field 'attributeFilter'
                            if ((application.getFilterCriteriaField() != null)
                                    && (paramValuesMap.keySet().contains(application.getFilterCriteriaField()))) {
                                // Get submitted attribute filters
                                Map attributeFiltersAsMap = MapUtils.orderedMap(new HashMap());
                                if (paramValuesMap.get(application.getFilterCriteriaField()) != null) {
                                    StringTokenizer tokenizer = new StringTokenizer(
                                        (String) paramValuesMap.get(application.getFilterCriteriaField()), " ,;", false
                                    );
                                    while (tokenizer.hasMoreTokens()) {
                                        String attributeFilter = tokenizer.nextToken();
                                        int pos = 0;
                                        if ((pos = attributeFilter.indexOf("=")) >= 0) {
                                            attributeFiltersAsMap.put(attributeFilter.substring(0, pos), attributeFilter.substring(pos + 1));
                                        }
                                    }
                                }
                                // Add currently active grid filters if not
                                // submitted
                                for (int i = 0; i < currentView.getReferencePane().length; i++) {
                                    ReferencePane pane = currentView.getReferencePane()[i];
                                    Grid currentGrid = pane.getGrid();
                                    if ((currentGrid != null) && (currentGrid.getCurrentFilter() != null)) {
                                        ByteArrayOutputStream bs = new ByteArrayOutputStream();
                                        XMLEncoder encoder = new XMLEncoder(bs);
                                        encoder.writeObject(currentGrid.getCurrentFilter());
                                        encoder.close();
                                        if(!attributeFiltersAsMap.keySet().contains(currentGrid.getGridControl().getObjectContainer().getReferenceName())) {
                                            attributeFiltersAsMap.put(
                                                currentGrid.getGridControl().getObjectContainer().getReferenceName(), 
                                                Base64.encode(bs.toByteArray())
                                            );
                                        }
                                    }
                                }
                                String attributeFilters = "";
                                int ii = 0;
                                for (Iterator i = attributeFiltersAsMap.entrySet().iterator(); i.hasNext(); ii++) {
                                    Entry e = (Entry) i.next();
                                    if (ii > 0)
                                        attributeFilters += ", ";
                                    attributeFilters += e.getKey() + "=" + e.getValue();
                                }
                                paramValuesMap.put(application.getFilterCriteriaField(), attributeFilters);
                            }
    
                            // get parameter definition
                            Model_1_0 model = ((RefPackage_1_0) currentView.getRefObject().refOutermostPackage()).refModel();
                            ModelElement_1_0 paramDef = null;
                            if (fieldMap.values().size() > 0) {
                                ModelElement_1_0 fieldDef = model.getElement(((Attribute) fieldMap.values().iterator().next())
                                        .getValue()
                                        .getName());
                                paramDef = model.getElement(fieldDef.values("container").get(0));
                            }
                            // no input parameters --> Void
                            else {
                                paramDef = model.getElement("org:openmdx:base:Void");
                            }
    
                            // prepare parameter values
                            List paramValues = new ArrayList();
                            for (Iterator j = paramDef.values("content").iterator(); j.hasNext();) {
                                ModelElement_1_0 fieldDef = model.getElement(j.next());
                                paramValues.add(paramValuesMap.get(fieldDef.values("qualifiedName").get(0)));
                            }
                            RefStruct param = ((RefPackage_1_0) currentView.getRefObject().refImmediatePackage()).refCreateStruct(
                                (String)paramDef.values("qualifiedName").get(0), 
                                paramValues
                            );
                            RefStruct result = null;
                            try {
                                AppLog.detail("invoking operation", "parameter=" + parameter + "; argument " + paramValues);
                                ((RefPackage_1_0)currentView.getRefObject().refOutermostPackage()).refBegin();
                                result = (RefStruct) currentView.getRefObject().refInvokeOperation(
                                    tab.getOperationTabControl().getOperationName(), 
                                    Arrays.asList(new Object[]{param})
                                );
                                ((RefPackage_1_0) currentView.getRefObject().refOutermostPackage()).refCommit();
                                // Test whether object is still accessable. If not
                                // go back to previous view. Notify other views about object update
                                try {
                                    currentView.getRefObject().refRefresh();
                                    EventHandlerHelper.notifyObjectModified(
                                        showViewsCache,
                                        currentView.getRefObject()
                                    );     
                                    RefObject_1_0 newTarget = application.getPortalExtension().handleOperationResult(
                                        currentView.getRefObject(), 
                                        tab.getOperationTabControl().getOperationName(), 
                                        param, 
                                        result
                                    );
                                    if(newTarget == null) {
                                        // Move values from param/result struct to OperationTabControl
                                        try {
                                            currentView.structToMap(
                                                param, 
                                                (Map)tab.getFieldGroup()[0].getObject(), 
                                                model.getElement(param.refTypeName())
                                            );
                                            currentView.structToMap(
                                                result, 
                                                (Map)tab.getFieldGroup()[1].getObject(), 
                                                model.getElement(result.refTypeName())
                                            );
                                        }
                                        catch (JmiServiceException e) {
                                            AppLog.warning(e.getMessage(), e.getCause(), 1);
                                            application.addErrorMessage(
                                                application.getTexts().getErrorTextCanNotSetOperationResult(), new String[] {currentView.getRefObject().refMofId(), tab.getOperationTabControl().getOperationName(), e.getMessage() }
                                            );
                                        }
                                        try {
                                            // Do not refresh view here because the servlet does a reload
                                            tab.getFieldGroup()[0].refresh(false);
                                            tab.getFieldGroup()[1].refresh(false);
                                            currentView.setOperationTabResult(tab);
                                        }
                                        // As fallback go back to returnToView in case the refresh fails
                                        // for the object the operation was invoked on. This is
                                        // typically the case when the object was removed/moved by the operation
                                        catch (Exception e) {
                                            nextView = currentView.getPreviousView();
                                            nextView.refresh(true);
                                        }
                                    }
                                    // Set nextView to new target
                                    else {
                                        Map historyActions = currentView.createHistoryAppendCurrent();
                                        nextView = new ShowObjectView(
                                            currentView.getId(),
                                            null,
                                            newTarget.refMofId(),
                                            application,
                                            historyActions,
                                            currentView.getLookupType(),
                                            currentView.getRestrictToElements(),
                                            currentView.getControlFactory()
                                        );
                                    }
                                }
                                catch (Exception e) {
                                    nextView = currentView.getPreviousView();
                                }
                            }
                            catch (RefException e) {
                                ServiceException e0 = new ServiceException(e);
                                AppLog.info(e0.getMessage(), e0.getCause(), 1);
                                try {
                                    ((RefPackage_1_0)currentView.getRefObject().refOutermostPackage()).refRollback();
                                }
                                catch (Exception e1) {
                                }
                                currentView.handleCanNotInvokeOperationException(
                                    e0.getExceptionStack(), tab.getOperationTabControl().getOperationName()
                                );
                            }
                            catch (JmiServiceException e) {
                                AppLog.info(e.getMessage(), e.getCause(), 1);
                                try {
                                    ((RefPackage_1_0) currentView.getRefObject().refOutermostPackage()).refRollback();
                                }
                                catch (Exception e0) {
                                }
                                currentView.handleCanNotInvokeOperationException(
                                    e.getExceptionStack(), tab.getOperationTabControl().getOperationName()
                                );
                            }
                        }
                        catch (ServiceException e) {
                            AppLog.warning(e.getMessage(), e.getCause(), 1);
                            application.addErrorMessage(
                                application.getTexts().getErrorTextCanNotInvokeOperation(), 
                                new String[] {
                                    currentView.getRefObject().refMofId(), 
                                    tab.getOperationTabControl().getOperationName(), 
                                    e.getMessage() 
                                }
                            );
                        }
                    }
                }
                break;
            }
    
            case Action.EVENT_SAVE_GRID: {
                if (((Object[]) parameterMap.get(Action.PARAMETER_PANE)).length > 0) {
                    int paneIndex = Integer.parseInt(((String[]) parameterMap.get(Action.PARAMETER_PANE))[0]);
                    if (paneIndex < currentView.getReferencePane().length) {
                        Grid grid = currentView.getReferencePane()[paneIndex].getGrid();
                        if (grid != null) {
                            for (
                                int i = 1; 
                                i < 999; // max 999 rows
                                i++
                            ) {
                                // Collect all fields of row i
                                Map row = new HashMap();
                                String refMofId = null;
                                boolean hasValues = false;
                                for (Iterator j = parameterMap.keySet().iterator(); j.hasNext();) {
                                    String fieldName = (String) j.next();
                                    if((fieldName.indexOf("[") >= 0) && (fieldName.indexOf("]") >= 0)) {
                                        int rowIndex = 
                                            (Integer.parseInt(
                                                fieldName.substring(
                                                    fieldName.indexOf("[") + 1, 
                                                    fieldName.indexOf("]"))
                                                ) % 100000
                                            ) / 100;
                                        if(i == rowIndex) {
                                            row.put(fieldName, parameterMap.get(fieldName));
                                            Object[] fieldValues = (Object[]) parameterMap.get(fieldName);
                                            if ((fieldValues.length > 0) && (((String) fieldValues[0]).length() > 0)) {
                                                hasValues = true;
                                                if (fieldName.startsWith("refMofId")) {
                                                    refMofId = (String) fieldValues[0];
                                                }
                                            }
                                        }
                                    }
                                }
                                if (hasValues) {
                                    EditObjectView editView = null;
                                    // Edit existing object
                                    if (refMofId.startsWith(XriProtocols.OPENMDX_PREFIX)) {
                                        try {
                                            RefObject_1_0 editObject = (RefObject_1_0) pkg.refObject(refMofId);                                    
                                            editView = new EditObjectView(
                                                currentView.getId(),
                                                currentView.getContainerElementId(),
                                                editObject, 
                                                null, 
                                                application, 
                                                MapUtils.orderedMap(new HashMap()), 
                                                currentView.getLookupType(),
                                                currentView.getRestrictToElements(),
                                                false,
                                                currentView.getControlFactory()
                                            );
                                        }
                                        catch(JmiServiceException e) {
                                            if(e.getExceptionCode() != BasicException.Code.NOT_FOUND) {
                                                throw e;
                                            }
                                        }
                                    }
                                    // Create new object from existing
                                    else if (refMofId.startsWith("clonedFrom:")) {
                                        RefObject_1_0 existingObject = (RefObject_1_0) pkg.refObject(refMofId.substring("clonedFrom:".length()));
                                        RefObject_1_0 newObject = (RefObject_1_0) pkg.refClass(existingObject.refClass().refMofId()).refCreateInstance(null);
                                        newObject.refInitialize(existingObject);
                                        editView = new EditObjectView(
                                            currentView.getId(), 
                                            currentView.getContainerElementId(),
                                            newObject, 
                                            null, 
                                            application, 
                                            MapUtils.orderedMap(new HashMap()), 
                                            currentView.getLookupType(),
                                            currentView.getRestrictToElements(),
                                            currentView.getRefObject(), 
                                            grid.getGridControl().getObjectContainer().getReferenceName(), 
                                            false,
                                            currentView.getControlFactory()
                                        );
                                    }
                                    // Create new object
                                    else {
                                        RefObject_1_0 newObject = (RefObject_1_0) pkg.refClass(refMofId).refCreateInstance(null);
                                        editView = new EditObjectView(
                                            currentView.getId(), 
                                            currentView.getContainerElementId(),
                                            newObject, 
                                            null, 
                                            application, 
                                            MapUtils.orderedMap(new HashMap()), 
                                            currentView.getLookupType(),
                                            currentView.getRestrictToElements(),
                                            currentView.getRefObject(), 
                                            grid.getGridControl().getObjectContainer().getReferenceName(), 
                                            false,
                                            currentView.getControlFactory()                                        
                                        );
                                    }
                                    // Process edit request
                                    if(editView != null) {
                                        try {
                                            editView.storeObject(row, new HashMap());
                                        }
                                        catch (Exception e) {
                                            try {
                                                pkg.refRollback();
                                            }
                                            catch (Exception e1) {
                                            }
                                            ServiceException e0 = new ServiceException(e);
                                            currentView.handleCanNotCommitException(e0.getExceptionStack());
                                        }
                                    }
                                }
                            }
                            try {
                                nextView.refresh(true);
                            }
                            catch (Exception e) {
                            }
                        }
                    }
                }
                break;
            }
                        
        }
        return nextView;
    }
    
    //-------------------------------------------------------------------------
    public static boolean acceptsEvent(
        int event
    ) {
        return
            (event == Action.EVENT_SELECT_OBJECT) ||
            (event == Action.EVENT_RELOAD) ||
            (event == Action.EVENT_SELECT_AND_EDIT_OBJECT) ||
            (event == Action.EVENT_SELECT_AND_NEW_OBJECT) ||
            (event == Action.EVENT_EDIT) ||
            (event == Action.EVENT_EDIT_MODAL) ||
            (event == Action.EVENT_NEW_OBJECT) ||
            (event == Action.EVENT_SELECT_LOCALE) ||
            (event == Action.EVENT_DELETE) ||
            (event == Action.EVENT_MULTI_DELETE) ||
            (event == Action.EVENT_INVOKE_OPERATION) ||
            (event == Action.EVENT_SELECT_GUI_MODE) ||
            (event == Action.EVENT_MACRO) ||
            (event == Action.EVENT_SAVE_GRID);
    }
    
}
