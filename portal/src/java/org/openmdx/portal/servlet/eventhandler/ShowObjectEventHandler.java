/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ShowObjectEventHandler.java,v 1.66 2010/09/28 09:39:01 wfro Exp $
 * Description: ShowObjectView 
 * Revision:    $Revision: 1.66 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/09/28 09:39:01 $
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
package org.openmdx.portal.servlet.eventhandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefStruct;
import javax.servlet.http.HttpSession;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.FieldGroup;
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.OperationTab;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.ViewMode;

public class ShowObjectEventHandler {

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static HandleEventResult handleEvent(
        int event, 
        ShowObjectView currentView,        
        String parameter, 
        HttpSession session, 
        Map parameterMap, 
        ViewsCache showViewsCache 
    ) {
        ObjectView nextView = currentView;
        ViewPort.Type nextViewPortType = null;
        
        // handle events
        ApplicationContext application = currentView.getApplicationContext();
        switch (event) {

            case Action.EVENT_SELECT_OBJECT:
            case Action.EVENT_RELOAD:
            case Action.EVENT_SELECT_AND_EDIT_OBJECT:
            case Action.EVENT_SELECT_AND_NEW_OBJECT: {

                // parameter is of format
                // reference=n][;forReference=name][;forClass=name][;refMofId=id]
                String objectXri = Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI);
                Path objectIdentity = new Path(objectXri);
                try {    
                    String paneIndexAsString = Action.getParameter(parameter, Action.PARAMETER_PANE);
                    String referenceAsString = Action.getParameter(parameter, Action.PARAMETER_REFERENCE);
                    String referenceName = Action.getParameter(parameter, Action.PARAMETER_REFERENCE_NAME);
                    String forReference = Action.getParameter(parameter, Action.PARAMETER_FOR_REFERENCE);
                    String forClass = Action.getParameter(parameter, Action.PARAMETER_FOR_CLASS);
                    String requestId = Action.getParameter(parameter, Action.PARAMETER_REQUEST_ID);
                   
                    Map<Path,Action> historyActions = (requestId == null) || (requestId.length() == 0) ? 
                    	currentView.createHistoryAppendCurrent() : 
                    		new HashMap<Path,Action>();
                    // Go back to requested view
                    if(
                        (requestId != null) && 
                        showViewsCache.containsView(requestId)
                    ) {
                       nextView = showViewsCache.getView(requestId);
                    }
                    // EVENT_SELECT_AND_EDIT_OBJECT
                    else if(event == Action.EVENT_SELECT_AND_EDIT_OBJECT) {
                    	PersistenceManager pm = application.getNewPmData();
                        RefObject_1_0 object = (RefObject_1_0)pm.getObjectById(objectIdentity);                    
                        nextView = new EditObjectView(
                            currentView.getId(),
                            null,
                            object.refGetPath(),
                            application,
                            historyActions,
                            currentView.getLookupType(),
                            currentView.getRestrictToElements(),
                            ViewMode.STANDARD
                        );
                    }
                    // EVENT_SELECT_AND_NEW_OBJECT
                    else if ((event == Action.EVENT_SELECT_AND_NEW_OBJECT) && (forClass.length() > 0) && (forReference.length() > 0)) {
                    	PersistenceManager pm = application.getNewPmData();
                        RefObject_1_0 parent = (RefObject_1_0)pm.getObjectById(objectIdentity);
                        RefObject_1_0 newObject = (RefObject_1_0)parent.refOutermostPackage().refClass(forClass).refCreateInstance(null);
                        nextView = new EditObjectView(
                            currentView.getId(),
                            null,
                            newObject,
                            null,
                            application,
                            historyActions,
                            currentView.getLookupType(),
                            currentView.getRestrictToElements(),
                            parent,
                            forReference,
                            ViewMode.STANDARD
                        );
                    }
                    else {
                        nextView = new ShowObjectView(
                            currentView.getId(),
                            null,
                            objectIdentity,
                            application,
                            historyActions,
                            currentView.getLookupType(),
                            currentView.getRestrictToElements()
                        );
                        // Show same grid in refreshed object
                        if (event == Action.EVENT_RELOAD) {
                            ShowObjectView view = (ShowObjectView) nextView;
                            view.refresh(true);
                            if(view.getReferencePane().length == currentView.getReferencePane().length) {
                                for (int i = 0; i < currentView.getReferencePane().length; i++) {
                                    view.getReferencePane()[i].selectReference(currentView.getReferencePane()[i].getSelectedReference());
                                }
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
                    SysLog.warning(e0.getMessage(), e0.getCause());
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotSelectObject(), new String[] { objectXri, e.getMessage() }
                    );
                    nextView = currentView;
                }
                break;
            }

            case Action.EVENT_MACRO: {

                // parameter is of format
                // [name=name][;type=type]
                String objectXri = Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI);
                Path objectIdentity = new Path(objectXri);
                try {
                    String actionName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                    if(actionName != null) {
                        actionName = new String(Base64.decode(actionName));
                    }
                    String actionType = Action.getParameter(parameter, Action.PARAMETER_TYPE);
                    nextView = new ShowObjectView(
                        currentView.getId(),
                        currentView.getContainerElementId(),
                        objectIdentity,
                        application,
                        currentView.createHistoryAppendCurrent(),
                        currentView.getLookupType(),
                        currentView.getRestrictToElements()
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
                    SysLog.warning(e0.getMessage(), e0.getCause());
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotSelectObject(), new String[] { objectXri, e.getMessage() }
                    );
                    nextView = currentView;
                }
                break;
            }
                 
            case Action.EVENT_EDIT:
                try {
                    nextView = new EditObjectView(
                        currentView.getId(),
                        currentView.getContainerElementId(),
                        currentView.getRefObject().refGetPath(),
                        application,
                        currentView.createHistoryAppendCurrent(),
                        currentView.getLookupType(),
                        currentView.getRestrictToElements(),
                        ViewMode.valueOf(
                            Action.getParameter(parameter, Action.PARAMETER_MODE)
                        )
                    );
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning(e0.getMessage(), e0.getCause());
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotEditObject(), new String[] { currentView.getRefObject().refMofId(), e.getMessage() }
                    );
                }
                break;
    
            case Action.EVENT_NEW_OBJECT:
                try {
                    String forClass = Action.getParameter(parameter, Action.PARAMETER_FOR_CLASS);
                    String forReference = Action.getParameter(parameter, Action.PARAMETER_FOR_REFERENCE);
                    SysLog.detail("creating object", Action.PARAMETER_FOR_CLASS + "=" + forClass + "; " + Action.PARAMETER_FOR_REFERENCE + "=" + forReference);
                    PersistenceManager pm = application.getNewPmData();
                    RefObject_1_0 parent = (RefObject_1_0)pm.getObjectById(currentView.getRefObject().refGetPath()); 
                    RefObject_1_0 newObject = (RefObject_1_0)parent.refOutermostPackage().refClass(forClass).refCreateInstance(null);
                    newObject.refInitialize(false, false);
                    nextView = new EditObjectView(
                        currentView.getId(),
                        currentView.getContainerElementId(),
                        newObject,
                        null,
                        application,
                        currentView.createHistoryAppendCurrent(),
                        currentView.getLookupType(),
                        currentView.getRestrictToElements(),
                        parent,
                        forReference,
                        ViewMode.STANDARD
                    );
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning(e0.getMessage(), e0.getCause());
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotEditObject(), new String[] { currentView.getRefObject().refMofId(), e.getMessage() }
                    );
                }
                break;
    
            case Action.EVENT_SELECT_LOCALE:
                try {
                    String locale = Action.getParameter(parameter, Action.PARAMETER_LOCALE);
                    SysLog.trace("Setting locale", locale);
                    application.setCurrentLocale(locale);
                    nextView = new ShowObjectView(
                        currentView.getId(),
                        currentView.getContainerElementId(),
                        currentView.getRefObject().refGetPath(),
                        application,
                        currentView.getHistoryActions(),
                        currentView.getLookupType(),
                        currentView.getRestrictToElements()
                    );
                    ShowObjectView view = (ShowObjectView) nextView;
                    for (int i = 0; i < currentView.getReferencePane().length; i++) {
                        view.getReferencePane()[i].selectReference(currentView.getReferencePane()[i].getSelectedReference());
                    }
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning(e0.getMessage(), e0.getCause());
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotSetLocale(),
                        new String[] { parameter, e.getMessage() });
                }
                break;
    
            case Action.EVENT_SELECT_VIEWPORT:
                try {
                    String viewPortType = Action.getParameter(parameter, Action.PARAMETER_ID);
                    SysLog.trace("Setting view port type", viewPortType);
                    application.setCurrentViewPortType(ViewPort.Type.valueOf(viewPortType));
                    nextView = currentView;
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning(e0.getMessage(), e0.getCause());
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotSetLocale(),
                        new String[] { parameter, e.getMessage() });
                }
                break;
    
            case Action.EVENT_SELECT_PERSPECTIVE:
                try {
                    String perspective = Action.getParameter(parameter, Action.PARAMETER_ID);
                    SysLog.trace("Setting perspective", perspective);
                    application.setCurrentPerspective(Integer.valueOf(perspective));
                    nextView = new ShowObjectView(
                        currentView.getId(),
                        currentView.getContainerElementId(),
                        currentView.getRefObject().refGetPath(),
                        application,
                        currentView.getHistoryActions(),
                        currentView.getLookupType(),
                        currentView.getRestrictToElements()
                    );
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning(e0.getMessage(), e0.getCause());
                    application.addErrorMessage(
                        application.getTexts().getErrorTextCannotSetPerspective(),
                        new String[] { parameter, e.getMessage() });
                }
                break;
    
            case Action.EVENT_DELETE: {
            	PersistenceManager pm = JDOHelper.getPersistenceManager(
            		currentView.getRefObject()
            	);
                try {
                    pm.currentTransaction().begin();
                    currentView.getRefObject().refDelete();
                    pm.currentTransaction().commit();
                    nextView = currentView.getPreviousView(null);
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning(e0.getMessage(), e0.getCause());
                    try {
                        pm.currentTransaction().rollback();
                    } catch (Exception e1) {}
                    currentView.handleCanNotCommitException(e0.getCause());
                }
                break;
            }
    
            case Action.EVENT_MULTI_DELETE: {
            	PersistenceManager pm = application.getNewPmData();
                try {
                    StringTokenizer tokenizer = new StringTokenizer(parameter, " ");
                    while (tokenizer.hasMoreTokens()) {
                        String objectXri = Action.getParameter(tokenizer.nextToken(), Action.PARAMETER_OBJECTXRI);
                        Path objectIdentity = new Path(objectXri);
                        pm.currentTransaction().begin();
                        ((RefObject)pm.getObjectById(objectIdentity)).refDelete();
                        pm.currentTransaction().commit();
                    }
                    nextView.refresh(true);
                }
                catch (Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning(e0.getMessage(), e0.getCause());
                    try {
                        pm.currentTransaction().rollback();
                    } 
                    catch (Exception e1) {}
                    currentView.handleCanNotCommitException(e0.getCause());
                }
                break;
            }
    
            case Action.EVENT_INVOKE_OPERATION: {    
            	SysLog.detail("invoking operation", parameter);
                int tabIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_TAB));
                int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                if (paneIndex < currentView.getOperationPane().length) {
                    if (tabIndex >= currentView.getOperationPane()[paneIndex].getOperationTab().length) {
                        String message = "undefined operation";
                        String toolTip =  currentView.getOperationPane()[paneIndex].getOperationPaneControl().getToolTip();
                        String cause = "pane=" + toolTip + "; paneIndex=" + paneIndex + "; tabIndex=" + tabIndex;
                        SysLog.error(message, cause);
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
                                application
                            );
    
                            // Set current filter of current pane as attribute value
                            // of field
                            // 'attributeFilter' if paramValuesMap contains the
                            // field 'attributeFilter'
                            if ((application.getFilterCriteriaField() != null)
                                    && (paramValuesMap.keySet().contains(application.getFilterCriteriaField()))) {
                                // Get submitted attribute filters
                                Map<String,String> attributeFiltersAsMap = new LinkedHashMap<String,String>();
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
                                        if(!attributeFiltersAsMap.keySet().contains(currentGrid.getGridControl().getObjectContainer().getReferenceName())) {
                                            String filterAsXml = JavaBeans.toXML(currentGrid.getCurrentFilter());
                                            attributeFiltersAsMap.put(
                                                currentGrid.getGridControl().getObjectContainer().getReferenceName(), 
                                                Base64.encode(filterAsXml.getBytes())
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
                                paramDef = model.getElement(fieldDef.objGetValue("container"));
                            }
                            // no input parameters --> Void
                            else {
                                paramDef = model.getElement("org:openmdx:base:Void");
                            }
    
                            // prepare parameter values
                            List paramValues = new ArrayList();
                            for (Iterator j = paramDef.objGetList("content").iterator(); j.hasNext();) {
                                ModelElement_1_0 fieldDef = model.getElement(j.next());
                                paramValues.add(paramValuesMap.get(fieldDef.objGetValue("qualifiedName")));
                            }
                            PersistenceManager pm = application.getNewPmData();
                            RefObject_1_0 target = (RefObject_1_0)pm.getObjectById(
                            	currentView.getRefObject().refGetPath()
                            );
                            RefStruct param = ((RefPackage_1_0)target.refImmediatePackage()).refCreateStruct(
                                (String)paramDef.objGetValue("qualifiedName"), 
                                paramValues
                            );
                            RefStruct result = null;
                            try {
                                // Reset error messages not related to operation invocation
                                application.getErrorMessages().clear();
                                pm.currentTransaction().begin();
                                result = (RefStruct)target.refInvokeOperation(
                                    tab.getOperationTabControl().getOperationName(), 
                                    Arrays.asList(new Object[]{param})
                                );
                                pm.currentTransaction().commit();
                                // Test whether object is still accessible. If not
                                // go back to previous view. Notify other views about object update
                                try {
                                    RefObject_1_0 newTarget = application.getPortalExtension().handleOperationResult(
                                        target, 
                                        tab.getOperationTabControl().getOperationName(), 
                                        param, 
                                        result
                                    );
                                    if(newTarget == null) {
                                        try {
                                            nextView.refresh(true);
                                            currentView.setOperationTabResult(tab);
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
                                        // As fallback go back to returnToView in case the refresh fails
                                        // for the object the operation was invoked on. This is
                                        // typically the case when the object was removed/moved by the operation
                                        catch (Exception e) {
                                        	SysLog.warning(e.getMessage(), e.getCause());
                                            application.addErrorMessage(
                                                application.getTexts().getErrorTextCanNotSetOperationResult(), new String[] {currentView.getRefObject().refMofId(), tab.getOperationTabControl().getOperationName(), e.getMessage() }
                                            );
                                            nextView = currentView.getPreviousView(null);
                                            nextView.refresh(true);
                                        }
                                    }
                                    // Set nextView to new target
                                    else {
                                        Map historyActions = currentView.createHistoryAppendCurrent();
                                        nextView = new ShowObjectView(
                                            currentView.getId(),
                                            null,
                                            newTarget.refGetPath(),
                                            application,
                                            historyActions,
                                            currentView.getLookupType(),
                                            currentView.getRestrictToElements()
                                        );
                                    }
                                }
                                catch (Exception e) {
                                    ServiceException e0 = new ServiceException(e);
                                    SysLog.info(e0.getMessage(), e0.getCause());
                                    nextView = currentView.getPreviousView(null);
                                }
                            }
                            catch(Exception e) {
                                ServiceException e0 = new ServiceException(e);
                                if(e0.getExceptionCode() == BasicException.Code.CONCURRENT_ACCESS_FAILURE) {
                                	SysLog.detail(e0.getMessage(), e0.getCause());
                                }
                                else {
                                	SysLog.warning(e0.getMessage(), e0.getCause());
                                }
                                try {
                                    pm.currentTransaction().rollback();
                                }
                                catch (Exception e1) {
                                }
                                currentView.handleCanNotInvokeOperationException(
                                    e0.getCause(), tab.getOperationTabControl().getOperationName()
                                );
                            }
                            pm.close();
                        }
                        catch (ServiceException e) {
                        	SysLog.warning(e.getMessage(), e.getCause());
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
                                String objectXri = null;
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
                                                    objectXri = (String) fieldValues[0];
                                                }
                                            }
                                        }
                                    }
                                }
                                if(hasValues) {
                                    RefObject_1_0 parent = currentView.getRefObject();
                                    PersistenceManager pm = JDOHelper.getPersistenceManager(parent);
                                    EditObjectView editView = null;
                                    // Edit existing object
                                    if (objectXri.startsWith("xri://@openmdx") || objectXri.startsWith("xri:@openmdx:")) {
                                        Path objectIdentity = new Path(objectXri);
                                        try {
                                            editView = new EditObjectView(
                                                currentView.getId(),
                                                currentView.getContainerElementId(),
                                                objectIdentity, 
                                                application, 
                                                new LinkedHashMap<Path,Action>(), 
                                                currentView.getLookupType(),
                                                currentView.getRestrictToElements(),
                                                ViewMode.STANDARD
                                            );
                                        }
                                        catch(ServiceException e) {
                                        	SysLog.warning(e.getMessage(), e.getCause());
                                        }
                                    }
                                    // Create new object from existing
                                    else if (objectXri.startsWith("clonedFrom:")) {
                                        Path objectIdentity = new Path(objectXri.substring("clonedFrom:".length()));
                                        RefObject_1_0 existingObject = (RefObject_1_0)pm.getObjectById(objectIdentity);
                                        RefObject_1_0 newObject = (RefObject_1_0)parent.refOutermostPackage().refClass(existingObject.refClass().refMofId()).refCreateInstance(null);
                                        newObject.refInitialize(existingObject);
                                        try {
                                            editView = new EditObjectView(
                                                currentView.getId(), 
                                                currentView.getContainerElementId(),
                                                newObject, 
                                                null, 
                                                application, 
                                                new LinkedHashMap<Path,Action>(), 
                                                currentView.getLookupType(),
                                                currentView.getRestrictToElements(),
                                                parent, 
                                                grid.getGridControl().getObjectContainer().getReferenceName(), 
                                                ViewMode.STANDARD
                                            );
                                        }
                                        catch(ServiceException e) {
                                        	SysLog.warning(e.getMessage(), e.getCause());
                                        }
                                    }
                                    // Create new object
                                    else {
                                        RefObject_1_0 newObject = (RefObject_1_0)parent.refOutermostPackage().refClass(objectXri).refCreateInstance(null);
                                        try {
                                            editView = new EditObjectView(
                                                currentView.getId(), 
                                                currentView.getContainerElementId(),
                                                newObject, 
                                                null, 
                                                application, 
                                                new LinkedHashMap<Path,Action>(), 
                                                currentView.getLookupType(),
                                                currentView.getRestrictToElements(),
                                                parent, 
                                                grid.getGridControl().getObjectContainer().getReferenceName(), 
                                                ViewMode.STANDARD
                                            );
                                        }
                                        catch(ServiceException e) {
                                        	SysLog.warning(e.getMessage(), e.getCause());
                                        }
                                    }
                                    // Process edit request
                                    if(editView != null) {
                                        try {
                                            editView.storeObject(row, new HashMap());
                                        }
                                        catch (Exception e) {
                                            ServiceException e0 = new ServiceException(e);
                                            currentView.handleCanNotCommitException(e0.getCause());
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
                
            case Action.EVENT_OBJECT_GET_ATTRIBUTES: { 
                nextViewPortType = ViewPort.Type.EMBEDDED;
                break;
            }
            
        }
        return new HandleEventResult(
            nextView,
            nextViewPortType
        );
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
            (event == Action.EVENT_NEW_OBJECT) ||
            (event == Action.EVENT_SELECT_LOCALE) ||
            (event == Action.EVENT_DELETE) ||
            (event == Action.EVENT_MULTI_DELETE) ||
            (event == Action.EVENT_INVOKE_OPERATION) ||
            (event == Action.EVENT_MACRO) ||
            (event == Action.EVENT_OBJECT_GET_ATTRIBUTES) ||
            (event == Action.EVENT_SAVE_GRID) ||
            (event == Action.EVENT_SELECT_PERSPECTIVE) ||
            (event == Action.EVENT_SELECT_VIEWPORT);
    }
    
}
