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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.log.SysLog;
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

public class UiGridMoveUpObjectAction extends BoundAction {

    public final static int EVENT_ID = 51;

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
                        PersistenceManager pm = app.getNewPmData();
                        RefObject_1_0 target = (RefObject_1_0)pm.getObjectById(currentView.getObject().refGetPath());
                        @SuppressWarnings({"unchecked"})
                        Collection<RefObject_1_0> unorderedValues = (Collection<RefObject_1_0>)target.refGetValue(feature);
                        if(unorderedValues instanceof List) {
                        	List<RefObject_1_0> values = (List<RefObject_1_0>)unorderedValues;
	                        Object[] parameterList = (Object[])requestParameters.get(WebKeys.REQUEST_PARAMETER_LIST);
	                        StringTokenizer tokenizer = new StringTokenizer(
	                            (parameterList != null) && (parameterList.length > 0) ? 
	                            	(String)parameterList[0] : 
	                            		"", 
	                            " "
	                        );
	                        try {
		                        pm.currentTransaction().begin();
		                        List<Path> selectedObjectPaths = new ArrayList<Path>();
		                        while (tokenizer.hasMoreTokens()) {
		                            String selectedObjectIdentity = Action.getParameter(tokenizer.nextToken(), Action.PARAMETER_OBJECTXRI);
		                            if(selectedObjectIdentity != null) {
		                            	Path selectedObjectPath = new Path(selectedObjectIdentity);
		                            	if(selectedObjectPath.size() > 5) {
		                            		selectedObjectPaths.add(selectedObjectPath);
		                            	}
		                            }
		                        }
		                        for(Path selectedObjectPath: selectedObjectPaths) {
                                    try {
                                    	RefObject_1_0 selectedObject = (RefObject_1_0)pm.getObjectById(selectedObjectPath);
                                    	int pos = values.indexOf(selectedObject);
                                    	if(pos > 0) {
                                    		RefObject_1_0 temp = values.get(pos - 1);
                                    		values.set(pos - 1, selectedObject);
                                    		values.set(pos, temp);
                                    	}
                                	} catch(Exception e) {}
		                        }
	                        	pm.currentTransaction().commit();
	                        } catch(Exception e) {
	                        	try {
	                        		pm.currentTransaction().rollback();
	                        	} catch(Exception e0) {}
	                        }
                        }
                        referencePanes.get(paneIndex).paint(
                            p,
                            ReferencePaneControl.FRAME_CONTENT,
                            false
                        );
                        pm.close();
                    }
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
        }       
        return new ActionPerformResult(
            ActionPerformResult.StatusCode.DONE
        );        
    }

}
