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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
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
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.ViewMode;

public abstract class GetObjectAction extends BoundAction {

	protected abstract boolean isGetAndEdit(
	);
	
	protected abstract boolean isGetAndNew(
	);
	
	protected abstract boolean isReload(
	);
	
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
		ShowObjectView currentView = (ShowObjectView)view;
    	ObjectView nextView = currentView;
        ViewPort.Type nextViewPortType = null;
        ApplicationContext app = currentView.getApplicationContext();
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
            else if(this.isGetAndEdit()) {
            	PersistenceManager pm = app.getNewPmData();
                RefObject_1_0 object = (RefObject_1_0)pm.getObjectById(objectIdentity);                    
                nextView = new EditObjectView(
                    currentView.getId(),
                    null,
                    object.refGetPath(),
                    app,
                    historyActions,
                    currentView.getLookupType(),
                    null, // do not propagate resourcePathPrefix
                    null, // do not propagate navigationTarget
                    ViewMode.STANDARD
                );
            }
            // EVENT_SELECT_AND_NEW_OBJECT
            else if(this.isGetAndNew() && (forClass.length() > 0) && (forReference.length() > 0)) {
            	PersistenceManager pm = app.getNewPmData();
                RefObject_1_0 parent = (RefObject_1_0)pm.getObjectById(objectIdentity);
                RefObject_1_0 newObject = (RefObject_1_0)parent.refOutermostPackage().refClass(forClass).refCreateInstance(null);
                nextView = new EditObjectView(
                    currentView.getId(),
                    null,
                    newObject,
                    null,
                    app,
                    historyActions,
                    currentView.getLookupType(),
                    parent,
                    forReference,
                    null, // do not propagate resourcePathPrefix
                    null, // do not propagate navigationTarget
                    ViewMode.STANDARD
                );
            }
            else {
                nextView = new ShowObjectView(
                    currentView.getId(),
                    null,
                    objectIdentity,
                    app,
                    historyActions,                    
                    currentView.getLookupType(),
                    null, // do not propagate resourcePathPrefix
                    null, // do not propagate navigationTarget
                    null // isReadOnly
                );
                // Show same grid in refreshed object
                if(this.isReload()) {
                    ShowObjectView showNextView = (ShowObjectView)nextView;
                    showNextView.refresh(true, true);
                    if(showNextView.getReferencePane().length == currentView.getReferencePane().length) {
                        for (int i = 0; i < currentView.getReferencePane().length; i++) {
                        	showNextView.getReferencePane()[i].selectReference(currentView.getReferencePane()[i].getSelectedReference());
                        }
                    }
                }
                // SELECT_OBJECT and reference pane (specified with
                // reference and pane index)
                else if ((referenceAsString.length() > 0) && (paneIndexAsString.length() > 0)) {
                    int reference = Integer.parseInt(referenceAsString);
                    int paneIndex = Integer.parseInt(paneIndexAsString);
                    ShowObjectView showNextView = (ShowObjectView)nextView;
                    if (paneIndex < showNextView.getReferencePane().length) {
                    	showNextView.selectReferencePane(paneIndex);
                    	showNextView.getReferencePane()[paneIndex].selectReference(reference);
                    }
                }
                // SELECT_OBJECT and reference pane (specified with
                // reference name)
                else if (referenceName.length() > 0) {
                    ShowObjectView showNextView = (ShowObjectView) nextView;
                    for(int i = 0; i < showNextView.getReferencePane().length; i++) {
                    	List<Action> selectReferenceActions = showNextView.getReferencePane()[i].getSelectReferenceActions();
                        for(int j = 0; j < selectReferenceActions.size(); j++) {
                            Action selectReferenceAction = selectReferenceActions.get(j);
                            if (selectReferenceAction.getParameter(Action.PARAMETER_REFERENCE_NAME).endsWith(referenceName)) {
                            	showNextView.selectReferencePane(i);
                            	showNextView.getReferencePane()[i].selectReference(j);
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
            app.addErrorMessage(
                app.getTexts().getErrorTextCannotSelectObject(), new String[] { objectXri, e.getMessage() }
            );
            nextView = currentView;
        }
        return new ActionPerformResult(
            nextView,
            nextViewPortType
        );
    }
        
}
