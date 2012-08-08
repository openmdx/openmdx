/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: InvokeOperationAction.java,v 1.2 2011/07/07 22:35:36 wfro Exp $
 * Description: ShowObjectView 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/07/07 22:35:36 $
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
package org.openmdx.portal.servlet.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefStruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.view.FieldGroup;
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.OperationTab;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;

public class InvokeOperationAction extends BoundAction {

	public final static int EVENT_ID = 22;

	@SuppressWarnings("unchecked")
    @Override
    public ActionPerformResult perform(
        ObjectView view,
        HttpServletRequest request,
        HttpServletResponse response,        
        String parameter,
        HttpSession session,
        Map<String,String[]> requestParameters,
        ViewsCache editViewsCache,
        ViewsCache showViewsCache      
    ) throws IOException, ServletException {
    	ObjectView nextView = view;
        ViewPort.Type nextViewPortType = null;
        ShowObjectView currentView = (ShowObjectView)view;
        ApplicationContext application = currentView.getApplicationContext();
    	SysLog.detail("invoking operation", parameter);
        int tabIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_TAB));
        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
        if(paneIndex < currentView.getOperationPane().length) {
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
                        requestParameters,
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
                                    PersistenceManager oldPm = nextView.refresh(true, false);
                                    if(nextView instanceof ShowObjectView) {
                                    	((ShowObjectView)nextView).setOperationTabResult(tab);
                                    }
                                    nextView.structToMap(
                                        param, 
                                        (Map)tab.getFieldGroup()[0].getObject(), 
                                        model.getElement(param.refTypeName()),
                                        false // do only map primitive type fields for input (prevents closed pm problems)
                                    );
                                    nextView.structToMap(
                                        result, 
                                        (Map)tab.getFieldGroup()[1].getObject(), 
                                        model.getElement(result.refTypeName()),
                                        true // map all field types for result
                                    );
                                    if(oldPm != null) {
                                    	oldPm.close();
                                    }
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
                                    nextView.refresh(true, true);
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
        return new ActionPerformResult(
            nextView,
            nextViewPortType
        );
    }
        
}
