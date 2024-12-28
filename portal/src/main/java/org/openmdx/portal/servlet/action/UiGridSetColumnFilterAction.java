/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: GridSetColumnFilterAction 
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

#if JAVA_8
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
#else
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
#endif

import org.openmdx.kernel.exception.Throwables;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewPortFactory;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.component.Grid;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.ReferencePane;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.UiGrid;
import org.openmdx.portal.servlet.control.ReferencePaneControl;

/**
 * GridSetColumnFilterAction
 *
 */
public class UiGridSetColumnFilterAction extends BoundAction {

	public final static int EVENT_ID = 13;

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.action.BoundAction#perform(org.openmdx.portal.servlet.view.ObjectView, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, javax.servlet.http.HttpSession, java.util.Map, org.openmdx.portal.servlet.ViewsCache, org.openmdx.portal.servlet.ViewsCache)
     */
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
                    addFilter = false;
                    if ((filterValue != null) && filterValue.startsWith("+")) {
                        filterValue = filterValue.substring(1);
                        addFilter = true;
                    } else if ((filterValue != null) && filterValue.startsWith("AND")) {
                        filterValue = filterValue.substring(3);
                        addFilter = true;
                    }
                    String filterName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                    filterNames.add(filterName);
                    filterValues.add(filterValue);
            	} else {
            		addFilter = true;
                	// form-based search
            		for(Iterator<String> i = requestParameters.keySet().iterator(); i.hasNext(); ) {
            			String parameterName = i.next();
            			if(parameterName.startsWith(WebKeys.REQUEST_PARAMETER_FILTER_VALUES + ".")) {
            				filterNames.add(
            					parameterName.substring((WebKeys.REQUEST_PARAMETER_FILTER_VALUES + ".").length())
            				);
	                        Object[] parameterValues = (Object[])requestParameters.get(parameterName);
	                        String filterValue = parameterValues == null ? null : (parameterValues.length > 0 ? (String) parameterValues[0] : null);
	                        if ((filterValue != null) && filterValue.startsWith("+")) {
	                            filterValue = filterValue.substring(1);
	                            addFilter = true;
	                        }
            				filterValues.add(filterValue);
            			}
            		}
            	}
                int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                List<ReferencePane> referencePanes = currentView.getChildren(ReferencePane.class);
                if(paneIndex < referencePanes.size()) {
                    currentView.selectReferencePane(paneIndex);
                    referencePanes.get(paneIndex).selectReference(referenceIndex);
                    Grid grid = referencePanes.get(paneIndex).getGrid();
                    if(grid instanceof UiGrid) {
                    	UiGrid uiGrid = (UiGrid)grid;                    	
                        uiGrid.setShowRows(true);
                        String[] resetFilter = requestParameters.get(WebKeys.REQUEST_PARAMETER_RESET_FILTER);
                        if(resetFilter != null && resetFilter.length > 0 && Boolean.valueOf(resetFilter[0])) {
                        	uiGrid.selectFilter("All", "");
                        }
                        for(int i = 0; i < filterNames.size(); i++) {
                        	if(filterValues.get(i) != null) {
                            	uiGrid.setColumnFilter(
                            		filterNames.get(i), 
                            		filterValues.get(i), 
                            		addFilter, 
                            		UiGrid.getPageSizeParameter(requestParameters)
                            	);
                            	addFilter = true;
                        	}
                        }
                        if(requestParameters.get(WebKeys.REQUEST_PARAMETER_SHOW_SEARCH_FORM) != null) {
                        	uiGrid.setShowSearchForm(
                        		UiGrid.getShowSearchFormParameter(requestParameters)
                        	);
                        }
                        if(!addFilter) {
                        	uiGrid.setPage(
                        		uiGrid.getCurrentPage(), 
                        		UiGrid.getPageSizeParameter(requestParameters)
                        	);
                        }
                        referencePanes.get(paneIndex).paint(
                            p,
                            ReferencePaneControl.FRAME_CONTENT,
                            false
                        );
                    }
                }
            } catch (Exception e) {
                Throwables.log(e);
            }
            try {
                p.close(true);
            } catch (Exception e) {
                Throwables.log(e);
            }            
        }       
        return new ActionPerformResult(
            ActionPerformResult.StatusCode.DONE
        );        
    }

}
