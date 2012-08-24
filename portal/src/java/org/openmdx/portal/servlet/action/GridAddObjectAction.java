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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewPortFactory;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.control.ReferencePaneControl;
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;

public class GridAddObjectAction extends BoundAction {

    public final static int EVENT_ID = 18;

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
                ReferencePane[] referencePanes = currentView.getReferencePane();
                int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                if (paneIndex < referencePanes.length) {
                    currentView.selectReferencePane(paneIndex);
                    Grid grid = referencePanes[paneIndex].getGrid();
                    if (grid != null) {
                        String feature = Action.getParameter(parameter, Action.PARAMETER_REFERENCE);
                        String addRemoveMode = Action.getParameter(parameter, Action.PARAMETER_NAME);
                        PersistenceManager pm = app.getNewPmData();
                        RefObject_1_0 target = (RefObject_1_0)pm.getObjectById(currentView.getRefObject().refGetPath());
                        Collection<RefObject_1_0> values = (Collection)target.refGetValue(feature);
                        // Remove non-existing objects from list
                        pm.currentTransaction().begin();
                        for(Iterator i = values.iterator(); i.hasNext();) {
                        	try {
                        		RefObject r = (RefObject) i.next();
                        	} catch(Exception e) {
                        		ServiceException e0 = new ServiceException(e);
                        		if(e0.getExceptionCode() == BasicException.Code.NOT_FOUND) {
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
                        referencePanes[paneIndex].getReferencePaneControl().paint(
                            p,
                            ReferencePaneControl.FRAME_CONTENT,
                            false
                        );                  
                        pm.close();
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
