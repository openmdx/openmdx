/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ShowObjectView 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
import org.openmdx.base.rest.cci.VoidRecord;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewPortFactory;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.component.Grid;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.OperationPane;
import org.openmdx.portal.servlet.component.ReferencePane;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.UiGrid;
import org.openmdx.portal.servlet.component.UiOperationParam;
import org.openmdx.portal.servlet.component.UiOperationTab;
import org.openmdx.portal.servlet.control.ShowErrorsControl;

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
		UiOperationTab operationResult,
		ShowObjectView view,
		ViewPort p
	) throws ServiceException {
        UiOperationParam result = operationResult.getChildren(UiOperationParam.class).get(1);
        result.paintOperationResult(
        	p
        );
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
	        UiOperationTab operationResult = null;
	        UiOperationTab operationTab = null;
	        List<OperationPane> operationPanes = currentView.getChildren(OperationPane.class);
	        List<ReferencePane> referencePanes = currentView.getChildren(ReferencePane.class);
	        if(paneIndex < operationPanes.size()) {
	            if (tabIndex >= operationPanes.get(paneIndex).getChildren(UiOperationTab.class).size()) {
	                String message = "undefined operation";
	                String toolTip =  operationPanes.get(paneIndex).getToolTip();
	                String cause = "pane=" + toolTip + "; paneIndex=" + paneIndex + "; tabIndex=" + tabIndex;
	                SysLog.error(message, cause);
	                app.addErrorMessage(
	                    app.getTexts().getErrorTextCanNotInvokeOperation(), 
	                    new String[] {
	                        currentView.getObject().refMofId(), 
	                        toolTip, 
	                        message + "; " + cause 
	                    }
	                );
	            } else {
	                operationTab = operationPanes.get(paneIndex).getChildren(UiOperationTab.class).get(tabIndex);
	                try {
	                    // only field group containing input parameter fields
	                    Map<String,Attribute> fieldMap = new HashMap<String,Attribute>();
	                    UiOperationParam operationParam = operationTab.getChildren(UiOperationParam.class).get(0);
                        operationParam.initAttributeMap(
                        	fieldMap, 
                        	app
                        );
	                    // map request to parameter values
	                    Map<String,String> paramValuesMap = new HashMap<String,String>();
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
	                        for (int i = 0; i < referencePanes.size(); i++) {
	                            ReferencePane pane = referencePanes.get(i);
	                            Grid currentGrid = pane.getGrid();
	                            if(currentGrid instanceof UiGrid) {
	                            	UiGrid currentUiGrid = (UiGrid)currentGrid;                    	
		                            if (currentUiGrid.getCurrentFilter() != null) {
		                                if(!attributeFiltersAsMap.keySet().contains(currentUiGrid.getReferenceName())) {
		                                    String filterAsXml = JavaBeans.toXML(currentUiGrid.getCurrentFilter());
		                                    attributeFiltersAsMap.put(
		                                        currentUiGrid.getReferenceName(), 
		                                        Base64.encode(filterAsXml.getBytes())
		                                    );
		                                }
		                            }
	                            }
	                        }
	                        String attributeFilters = "";
	                        int ii = 0;
	                        for (Iterator<Map.Entry<String,String>> i = attributeFiltersAsMap.entrySet().iterator(); i.hasNext(); ii++) {
	                            Map.Entry<String,String> e = i.next();
	                            if (ii > 0)
	                                attributeFilters += ", ";
	                            attributeFilters += e.getKey() + "=" + e.getValue();
	                        }
	                        paramValuesMap.put(app.getFilterCriteriaField(), attributeFilters);
	                    }
	                    // get parameter definition
	                    Model_1_0 model = ((RefPackage_1_0) currentView.getObject().refOutermostPackage()).refModel();
	                    ModelElement_1_0 paramDef = null;
	                    if (fieldMap.values().size() > 0) {
	                        ModelElement_1_0 fieldDef = model.getElement(((Attribute) fieldMap.values().iterator().next())
	                                .getValue()
	                                .getName());
	                        paramDef = model.getElement(fieldDef.getContainer());
	                    }
	                    // no input parameters --> Void
	                    else {
	                        paramDef = model.getElement(VoidRecord.NAME);
	                    }
	                    // prepare parameter values
	                    List<Object> paramValues = new ArrayList<Object>();
	                    for(Iterator<Object> j = paramDef.objGetList("content").iterator(); j.hasNext();) {
	                        ModelElement_1_0 fieldDef = model.getElement(j.next());
	                        paramValues.add(paramValuesMap.get(fieldDef.getQualifiedName()));
	                    }
	                    RefObject_1_0 target = (RefObject_1_0)pm.getObjectById(
	                    	currentView.getObject().refGetPath()
	                    );
	                    RefStruct param = ((RefPackage_1_0)target.refImmediatePackage()).refCreateStruct(
	                        (String)paramDef.getQualifiedName(), 
	                        paramValues
	                    );
	                    RefStruct result = null;
	                    try {
	                        // Reset error messages not related to operation invocation
	                        app.getErrorMessages().clear();
	                        pm.currentTransaction().begin();
	                        result = (RefStruct)target.refInvokeOperation(
	                            operationTab.getOperationName(), 
	                            Arrays.asList(new Object[]{param})
	                        );
	                        pm.currentTransaction().commit();
	                        // Handle operation result only if it must be displayed
	                        if(operationTab.displayOperationResult()) {
		                        try {
		                            app.getPortalExtension().handleOperationResult(
		                                target, 
		                                operationTab.getOperationName(), 
		                                param, 
		                                result
		                            );
		                            try {
		                                currentView.structToMap(
		                                    param, 
		                                    (Map<String,Object>)operationTab.getChildren(UiOperationParam.class).get(0).getObject(), 
		                                    model.getElement(param.refTypeName()),
		                                    false // do only map primitive type fields for input (prevents closed pm problems)
		                                );
		                                currentView.structToMap(
		                                    result, 
		                                    (Map<String,Object>)operationTab.getChildren(UiOperationParam.class).get(1).getObject(), 
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
		                                    	currentView.getObject().refMofId(), 
		                                    	operationTab.getOperationName(), 
		                                    	cause.getMessage()
		                                    }
		                                );
		                            }
		                        } catch (Exception e) {
		                            Throwables.log(e);
		                        }
	                        }
	                    } catch(Exception e) {
	                        ServiceException serviceException = new ServiceException(e);
	                        if(serviceException.getExceptionCode() == BasicException.Code.CONCURRENT_ACCESS_FAILURE) {
	                        	SysLog.detail(serviceException.getMessage(), serviceException.getCause());
	                        } else {
	                        	SysLog.warning(serviceException.getMessage(), serviceException.getCause());
	                        }
	                        try {
	                            pm.currentTransaction().rollback();
	                        } catch (Exception ignore) {
	                			SysLog.trace("Exception ignored", ignore);
	                        }
	                        currentView.handleCanNotInvokeOperationException(
	                            serviceException,
	                            operationTab.getOperationName()
	                        );
	                    }
	                } catch (ServiceException e) {
	                	SysLog.warning(e.getMessage(), e.getCause());
	                    app.addErrorMessage(
	                        app.getTexts().getErrorTextCanNotInvokeOperation(), 
	                        new String[] {
	                            currentView.getObject().refMofId(), 
	                            operationTab.getOperationName(), 
	                            e.getMessage() 
	                        }
	                    );
	                }
	            }
	        }
	        // Paint operation result and/or errors
	        if(
	        	(operationTab == null || operationTab.displayOperationResult()) &&
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
		            Throwables.log(e);
		        }
		        try {
		            p.close(true);
		        } catch (Exception e) {
                    Throwables.log(e);
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
