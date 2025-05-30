/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: GridEventHandler 
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
import java.util.List;
import java.util.Map;

#if JAVA_8
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
#else
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

public class UiGridSelectFilterAction extends BoundAction {

	public final static int EVENT_ID = 1;

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
                Object[] parameterValues = (Object[])requestParameters.get(WebKeys.REQUEST_PARAMETER_FILTER_VALUES);
                int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                String filterValues = parameterValues == null ? null : (parameterValues.length > 0 ? (String) parameterValues[0] : null);
                int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                String filterName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                List<ReferencePane> referencePanes = currentView.getChildren(ReferencePane.class);
                if (paneIndex < referencePanes.size()) {
                    currentView.selectReferencePane(paneIndex);
                    referencePanes.get(paneIndex).selectReference(referenceIndex);
                    Grid grid = referencePanes.get(paneIndex).getGrid();
                    if(grid instanceof UiGrid) {
                    	UiGrid uiGrid = (UiGrid)grid;                    	
                        uiGrid.setShowRows(true);
                        uiGrid.selectFilter(filterName, filterValues);
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
