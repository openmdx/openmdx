/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ShowObjectView 
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
package org.openmdx.portal.servlet.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

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
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewPortFactory;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.control.ShowErrorsControl;
import org.openmdx.portal.servlet.view.FieldGroup;
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.OperationTab;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;

public class InvokeOperationAction extends BoundAction {

	public final static int EVENT_ID = 22;

	/**
	 * Paint operation result.
	 * 
	 * @param view
	 * @param p
	 * @throws ServiceException
	 */
	public void paintOperationResult(
		OperationTab operationResult,
		ShowObjectView view,
		ViewPort p
	) throws ServiceException {
		ApplicationContext app = p.getApplicationContext();
		Texts_1_0 texts = app.getTexts();
        // Operation result (if any) and result has fields		
        FieldGroup fieldGroup = operationResult.getFieldGroup()[1];
        Attribute[][] attributes = fieldGroup.getAttribute();
        int nCols = attributes.length;
        int nRows = nCols > 0 ? attributes[0].length : 0;
        if((nCols > 0) && (nRows > 0)) {
            p.write("<div class=\"panelResult\" style=\"display: block;\">");
            p.write("  <table class=\"fieldGroup\">");
            for(int v = 0; v < nRows; v++) {
                p.write("<tr>");
                for(int u = 0; u < nCols; u++) {
                    Attribute attribute = attributes[u][v];
                    if(attribute == null) continue;
                    if(attribute.isEmpty()) {
                        p.write("<td class=\"label\"></td>");
                    }                                
                    else {
                        String label = attribute.getLabel();
                        AttributeValue valueHolder = attribute.getValue();
                        Object value = valueHolder.getValue(false);
                        String stringifiedValue = attribute.getStringifiedValue(
                            p, 
                            false, 
                            false
                        );
                        stringifiedValue = valueHolder instanceof TextValue
                        ? ((TextValue)valueHolder).isPassword() ? "*****" : stringifiedValue
                            : stringifiedValue;
                        String widthModifier = "";                                    
                        String readonlyModifier = valueHolder.isChangeable() ? "" : "readonly";
                        String lockedModifier = valueHolder.isChangeable() ? "" : "Locked";
                        String styleModifier = "style=\"";
                        // ObjectReference
                        if(value instanceof ObjectReference) {
                            Action selectAction = ((ObjectReference)value).getSelectObjectAction();
                            Action selectAndEditAction = ((ObjectReference)value).getSelectAndEditObjectAction();
                            p.write("<td class=\"label\"><span class=\"nw\">", label, ":</span></td>");
                            p.write("<td class=\"valueL\" ", widthModifier, "><div class=\"field\" title=\"", selectAction.getToolTip(), "\"><a href=\"\" onmouseover=\"javascript:this.href=", p.getEvalHRef(selectAction), ";onmouseover=function(){};\">", selectAction.getTitle(), "</a> [<a href=\"\" onclick=\"javascript:this.href=", p.getEvalHRef(selectAndEditAction), ";\">", texts.getEditTitle(), "</a>]</div></td>");
                        }
                        // other types
                        else {
                            valueHolder.paint(
                                attribute,
                                p,
                                null, // default id
                                null, // default label
                                view.getLookupObject(),
                                nCols,
                                0,
                                "",
                                styleModifier,
                                widthModifier,
                                "",
                                readonlyModifier,
                                lockedModifier,
                                stringifiedValue,
                                false // forEditing
                            );
                        }
                    }
                }
                p.write("</tr>");
            }
            p.write("  </table>");
            p.write("</div>");
        } else {
        	p.write("<div />");
        }
	}

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.action.BoundAction#perform(org.openmdx.portal.servlet.view.ObjectView, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, javax.servlet.http.HttpSession, java.util.Map, org.openmdx.portal.servlet.ViewsCache, org.openmdx.portal.servlet.ViewsCache)
	 */
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
    	SysLog.detail("Invoke operation", parameter);
		PersistenceManager pm = null;		
        ShowObjectView currentView = (ShowObjectView)view;
		try {
	        ApplicationContext app = currentView.getApplicationContext();
	        pm = app.getNewPmData();        
	        int tabIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_TAB));
	        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
	        OperationTab operationResult = null;
	        OperationTab operationTab = null;
	        if(paneIndex < currentView.getOperationPane().length) {
	            if (tabIndex >= currentView.getOperationPane()[paneIndex].getOperationTab().length) {
	                String message = "undefined operation";
	                String toolTip =  currentView.getOperationPane()[paneIndex].getOperationPaneControl().getToolTip();
	                String cause = "pane=" + toolTip + "; paneIndex=" + paneIndex + "; tabIndex=" + tabIndex;
	                SysLog.error(message, cause);
	                app.addErrorMessage(
	                    app.getTexts().getErrorTextCanNotInvokeOperation(), 
	                    new String[] {
	                        currentView.getRefObject().refMofId(), 
	                        toolTip, 
	                        message + "; " + cause 
	                    }
	                );
	            } else {
	                operationTab = currentView.getOperationPane()[paneIndex].getOperationTab()[tabIndex];
	                try {
	                    // only field group containing input parameter fields
	                    Map fieldMap = new HashMap();
	                    for (int j = 0; j < 1; j++) {
	                        FieldGroup fieldGroup = operationTab.getFieldGroup()[j];
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
	                    app.getPortalExtension().updateObject(
	                        paramValuesMap,
	                        requestParameters,
	                        fieldMap,
	                        app
	                    );
	                    // Set current filter of current pane as attribute value
	                    // of field
	                    // 'attributeFilter' if paramValuesMap contains the
	                    // field 'attributeFilter'
	                    if((app.getFilterCriteriaField() != null)
	                            && (paramValuesMap.keySet().contains(app.getFilterCriteriaField()))) {
	                        // Get submitted attribute filters
	                        Map<String,String> attributeFiltersAsMap = new LinkedHashMap<String,String>();
	                        if (paramValuesMap.get(app.getFilterCriteriaField()) != null) {
	                            StringTokenizer tokenizer = new StringTokenizer(
	                                (String) paramValuesMap.get(app.getFilterCriteriaField()), " ,;", false
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
	                        paramValuesMap.put(app.getFilterCriteriaField(), attributeFilters);
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
	                        app.getErrorMessages().clear();
	                        pm.currentTransaction().begin();
	                        result = (RefStruct)target.refInvokeOperation(
	                            operationTab.getOperationTabControl().getOperationName(), 
	                            Arrays.asList(new Object[]{param})
	                        );
	                        pm.currentTransaction().commit();
	                        // Handle operation result only if it must be displayed
	                        if(operationTab.getOperationTabControl().displayOperationResult()) {
		                        try {
		                            app.getPortalExtension().handleOperationResult(
		                                target, 
		                                operationTab.getOperationTabControl().getOperationName(), 
		                                param, 
		                                result
		                            );
		                            try {
		                                currentView.structToMap(
		                                    param, 
		                                    (Map)operationTab.getFieldGroup()[0].getObject(), 
		                                    model.getElement(param.refTypeName()),
		                                    false // do only map primitive type fields for input (prevents closed pm problems)
		                                );
		                                currentView.structToMap(
		                                    result, 
		                                    (Map)operationTab.getFieldGroup()[1].getObject(), 
		                                    model.getElement(result.refTypeName()),
		                                    true // map all field types for result
		                                );
		                               	operationResult = operationTab;
		                            }
		                            // As fallback go back to returnToView in case the refresh fails
		                            // for the object the operation was invoked on. This is
		                            // typically the case when the object was removed/moved by the operation
		                            catch (Exception e) {
		                            	SysLog.warning(e.getMessage(), e.getCause());
		                            	Throwable cause = e;
		                            	while(cause.getCause() != null) {
		                            		cause = cause.getCause();
		                            	}
		                                app.addErrorMessage(
		                                    app.getTexts().getErrorTextCanNotSetOperationResult(), 
		                                    new String[] {
		                                    	currentView.getRefObject().refMofId(), 
		                                    	operationTab.getOperationTabControl().getOperationName(), 
		                                    	cause.getMessage()
		                                    }
		                                );
		                            }
		                        } catch (Exception e) {
		                            ServiceException e0 = new ServiceException(e);
		                            SysLog.info(e0.getMessage(), e0.getCause());
		                        }
	                        }
	                    } catch(Exception e) {
	                        ServiceException e0 = new ServiceException(e);
	                        if(e0.getExceptionCode() == BasicException.Code.CONCURRENT_ACCESS_FAILURE) {
	                        	SysLog.detail(e0.getMessage(), e0.getCause());
	                        } else {
	                        	SysLog.warning(e0.getMessage(), e0.getCause());
	                        }
	                        try {
	                            pm.currentTransaction().rollback();
	                        } catch (Exception ignore) {}
	                        currentView.handleCanNotInvokeOperationException(
	                            e0,
	                            operationTab.getOperationTabControl().getOperationName()
	                        );
	                    }
	                } catch (ServiceException e) {
	                	SysLog.warning(e.getMessage(), e.getCause());
	                    app.addErrorMessage(
	                        app.getTexts().getErrorTextCanNotInvokeOperation(), 
	                        new String[] {
	                            currentView.getRefObject().refMofId(), 
	                            operationTab.getOperationTabControl().getOperationName(), 
	                            e.getMessage() 
	                        }
	                    );
	                }
	            }
	        }
	        // Paint operation result and/or errors
	        if(
	        	(operationTab == null || operationTab.getOperationTabControl().displayOperationResult()) &&
	        	(operationResult != null || !app.getErrorMessages().isEmpty())
	        ) {
		        ViewPort p = ViewPortFactory.openPage(
		            view,
		            request,
		            this.getWriter(request, response)
		        );
		        try {
		        	if(!app.getErrorMessages().isEmpty()) {
		        		ShowErrorsControl errorsControl = new ShowErrorsControl(
		        			"", 
		        			app.getLocale(), 
		        			app.getCurrentLocaleAsIndex()
		        		);
		        		errorsControl.paint(p, null, false);
		        	} else if(operationResult != null) {
		        		this.paintOperationResult(
		        			operationResult,
		        			currentView, 
		        			p
		        		);
		        	}
		        } catch (Exception e) {
		            ServiceException e0 = new ServiceException(e);
		            SysLog.warning(e0.getMessage(), e0.getCause());
		        }
		        try {
		            p.close(true);
		        } catch (Exception e) {
		            ServiceException e0 = new ServiceException(e);
		            SysLog.warning(e0.getMessage(), e0.getCause());
		        }
		        return new ActionPerformResult(
		            ActionPerformResult.StatusCode.DONE
		        );
	        }
	        // Refresh page
	        else {
		        return new ActionPerformResult(currentView);
	        }
		} finally {
			try {
				currentView.refresh(true, true);
			} catch(Exception ignore) {}
	        if(pm != null) {
	        	try {
	        		pm.close();
	        	} catch(Exception ignore) {}
	        }
		}
    }

}
