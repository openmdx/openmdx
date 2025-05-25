/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: GridAddObjectAction 
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
#if JAVA_8
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
#else
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
#endif

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewPortFactory;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.component.Grid;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.ReferencePane;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.control.ReferencePaneControl;

/**
 * GridAddObjectAction
 *
 */
public class UiGridAddObjectAction extends BoundAction {

    public final static int EVENT_ID = 18;

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
            ApplicationContext app = currentView.getApplicationContext();
            try {
                List<ReferencePane> referencePanes = currentView.getChildren(ReferencePane.class);
                int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                if (paneIndex < referencePanes.size()) {
                    currentView.selectReferencePane(paneIndex);
                    Grid grid = referencePanes.get(paneIndex).getGrid();
                    if (grid != null) {
                        String feature = Action.getParameter(parameter, Action.PARAMETER_REFERENCE);
                        String addRemoveMode = Action.getParameter(parameter, Action.PARAMETER_NAME);
                        PersistenceManager pm = app.getNewPmData();
                        RefObject_1_0 target = (RefObject_1_0)pm.getObjectById(currentView.getObject().refGetPath());
                        @SuppressWarnings("unchecked")
                        Collection<RefObject_1_0> values = (Collection<RefObject_1_0>)target.refGetValue(feature);
                        // Remove non-existing objects from list
                        pm.currentTransaction().begin();
                        for(Iterator<RefObject_1_0> i = values.iterator(); i.hasNext();) {
                        	try {
                        		RefObject r = (RefObject)i.next();
                        		if(r == null) {
                        			i.remove();
                        		}
                        	} catch(Exception e) {
                        	    BasicException be = BasicException.toExceptionStack(e);
                        		if(be.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                                    i.remove();                        			
                        		}
                        	}
                        }
                        // Add/Remove objects         
                        Object[] parameterList = (Object[])requestParameters.get(WebKeys.REQUEST_PARAMETER_LIST);
                        StringTokenizer tokenizer = new StringTokenizer(
                            (parameterList != null) && (parameterList.length > 0) ? (String)parameterList[0] : "", 
                            " "
                        );
                        while (tokenizer.hasMoreTokens()) {
                            String refMofId = Action.getParameter(tokenizer.nextToken(), Action.PARAMETER_OBJECTXRI);
                            Path refPath = new Path(refMofId);
                            // Must be at least a segment
                            if(refPath.size() >= 5) {
                                RefObject_1_0 selectedObject = (RefObject_1_0)pm.getObjectById(refPath);
                                if ("+".equals(addRemoveMode)) {
                                    values.add(selectedObject);
                                }
                                else {
                                    values.remove(selectedObject);
                                }
                            }
                        }
                        pm.currentTransaction().commit();
                        referencePanes.get(paneIndex).paint(
                            p,
                            ReferencePaneControl.FRAME_CONTENT,
                            false
                        );
                        pm.close();
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
