/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: GridEventHandler 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.portal.servlet.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewPortFactory;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.control.ReferencePaneControl;
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;

public class GridSetColumnFilterAction extends BoundAction {

	public final static int EVENT_ID = 13;

	protected boolean isAddFilter(
	) {
		return false;
	}
	
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
    ) throws IOException {
        if(view instanceof ShowObjectView) {
            ShowObjectView currentView = (ShowObjectView)view;
            ViewPort p = ViewPortFactory.openPage(
                view,
                request,
                this.getWriter(request, response)
            );
            try {
            	boolean addFilter = false;
            	List<String> filterNames = new ArrayList<String>();
            	List<String> filterValues = new ArrayList<String>();
            	// single-field search
            	if(requestParameters.get(WebKeys.REQUEST_PARAMETER_FILTER_VALUES) != null) {
                    Object[] parameterValues = (Object[])requestParameters.get(WebKeys.REQUEST_PARAMETER_FILTER_VALUES);
                    String filterValue = parameterValues == null ? null : (parameterValues.length > 0 ? (String) parameterValues[0] : null);
                    addFilter = this.isAddFilter();
                    if ((filterValue != null) && filterValue.startsWith("+")) {
                        filterValue = filterValue.substring(1);
                        addFilter = true;
                    }
                    else if ((filterValue != null) && filterValue.startsWith("AND")) {
                        filterValue = filterValue.substring(3);
                        addFilter = true;
                    }
                    String filterName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                    filterNames.add(filterName);
                    filterValues.add(filterValue);
            	}
            	// form-based search
            	else {
            		for(Iterator<String> i = requestParameters.keySet().iterator(); i.hasNext(); ) {
            			String parameterName = i.next();
            			if(parameterName.startsWith(WebKeys.REQUEST_PARAMETER_FILTER_VALUES + ".")) {
            				filterNames.add(
            					parameterName.substring((WebKeys.REQUEST_PARAMETER_FILTER_VALUES + ".").length())
            				);
	                        Object[] parameterValues = (Object[])requestParameters.get(parameterName);
	                        String filterValue = parameterValues == null ? null : (parameterValues.length > 0 ? (String) parameterValues[0] : null);
            				filterValues.add(filterValue);
            			}
            			
            		}
            	}
                int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                ReferencePane[] referencePanes = currentView.getReferencePane();
                if(paneIndex < referencePanes.length) {
                    currentView.selectReferencePane(paneIndex);
                    referencePanes[paneIndex].selectReference(referenceIndex);
                    Grid grid = referencePanes[paneIndex].getGrid();
                    if (grid != null) {
                        grid.setShowRows(true);                                
                        for(int i = 0; i < filterNames.size(); i++) {
                        	if(filterValues.get(i) != null) {
                            	grid.setColumnFilter(
                            		filterNames.get(i), 
                            		filterValues.get(i), 
                            		addFilter, 
                            		Grid.getPageSizeParameter(requestParameters)
                            	);
                            	addFilter = true;
                        	}
                        }
                        if(requestParameters.get(WebKeys.REQUEST_PARAMETER_SHOW_SEARCH_FORM) != null) {
                        	grid.setShowSearchForm(
                        		Grid.getShowSearchFormParameter(requestParameters)
                        	);
                        }
                        if(!addFilter) {
                        	grid.setPage(
                        		grid.getCurrentPage(), 
                        		Grid.getPageSizeParameter(requestParameters)
                        	);
                        }
                        referencePanes[paneIndex].getReferencePaneControl().paint(
                            p,
                            ReferencePaneControl.FRAME_CONTENT,
                            false
                        );
                    }
                }
            }
            catch (Exception e) {
                ServiceException e0 = new ServiceException(e);
                SysLog.warning(e0.getMessage(), e0.getCause());
            }
            try {
                p.close(true);
            }
            catch (Exception e) {
                ServiceException e0 = new ServiceException(e);
                SysLog.warning(e0.getMessage(), e0.getCause());
            }            
        }       
        return new ActionPerformResult(
            ActionPerformResult.StatusCode.DONE
        );        
    }

}
