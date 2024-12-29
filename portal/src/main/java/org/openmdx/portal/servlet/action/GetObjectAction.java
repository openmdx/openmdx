/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: GetObjectAction 
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.component.EditObjectView;
import org.openmdx.portal.servlet.component.Grid;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.ReferencePane;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.UiGrid;
import org.openmdx.portal.servlet.component.ViewMode;

/**
 * GetObjectAction
 *
 */
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
            String origin = Action.getParameter(parameter, Action.PARAMETER_ORIGIN);
            Map<Path,Action> nextPrevActions = currentView.getNextPrevActions();
            Map<Path,Action> historyActions = null;
            if(requestId == null || requestId.isEmpty()) {
            	if(nextPrevActions != null && nextPrevActions.containsKey(objectIdentity)) {
            		historyActions = currentView.getHistoryActions();
            	} else {
            		historyActions = currentView.createHistoryAppendCurrent();
            	}
            } else {
            	historyActions = new HashMap<Path,Action>();
            }
            if(origin != null && !origin.isEmpty()) {
            	try {
	            	int originPaneIndex = Integer.valueOf(origin);
	            	Grid selectedGrid = currentView.getChildren(ReferencePane.class).get(originPaneIndex).getGrid();
	            	if(selectedGrid instanceof UiGrid) {
                    	UiGrid selectedUiGrid = (UiGrid)selectedGrid;	            		
	            		nextPrevActions = new LinkedHashMap<Path,Action>();
	            		nextPrevActions.putAll(
            				selectedUiGrid.getSelectRowObjectActions()
	            		);
	            	}
            	} catch(Exception ignore) {}
            } 
            // Go back to requested view
            if(
                (requestId != null) && 
                showViewsCache.containsView(requestId)
            ) {
               nextView = showViewsCache.getView(requestId);
            } else if(this.isGetAndEdit()) {
            	PersistenceManager pm = app.getNewPmData();
                RefObject_1_0 object = (RefObject_1_0)pm.getObjectById(objectIdentity);                    
                nextView = new EditObjectView(
                    currentView.getId(),
                    null,
                    object.refGetPath(),
                    app,
                    historyActions,
                    nextPrevActions,
                    currentView.getLookupType(),
                    null, // do not propagate resourcePathPrefix
                    null, // do not propagate navigationTarget
                    ViewMode.STANDARD
                );
            } else if(this.isGetAndNew() && (forClass.length() > 0) && (forReference.length() > 0)) {
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
                    nextPrevActions,
                    currentView.getLookupType(),
                    parent,
                    forReference,
                    null, // do not propagate resourcePathPrefix
                    null, // do not propagate navigationTarget
                    ViewMode.STANDARD
                );
            } else {
                nextView = new ShowObjectView(
                    currentView.getId(),
                    null,
                    (RefObject_1_0)app.getNewPmData().getObjectById(objectIdentity),
                    app,
                    historyActions,
                    nextPrevActions,
                    currentView.getLookupType(),
                    null, // do not propagate resourcePathPrefix
                    null, // do not propagate navigationTarget
                    null // isReadOnly
                );
                // Show same grid in refreshed object
                if(this.isReload()) {
                    ShowObjectView showNextView = (ShowObjectView)nextView;
                    showNextView.refresh(true, true);
                    List<ReferencePane> nextViewReferencePanes = showNextView.getChildren(ReferencePane.class);
                    List<ReferencePane> currentViewReferencePanes = currentView.getChildren(ReferencePane.class);
                    if(nextViewReferencePanes.size() == currentViewReferencePanes.size()) {
                        for (int i = 0; i < currentViewReferencePanes.size(); i++) {
                        	nextViewReferencePanes.get(i).selectReference(currentViewReferencePanes.get(i).getSelectedReference());
                        }
                    }
                } else if ((referenceAsString.length() > 0) && (paneIndexAsString.length() > 0)) {
                    // SELECT_OBJECT and reference pane (specified with reference and pane index)
                    int reference = Integer.parseInt(referenceAsString);
                    int paneIndex = Integer.parseInt(paneIndexAsString);
                    ShowObjectView showNextView = (ShowObjectView)nextView;
                    List<ReferencePane> nextViewReferencePanes = showNextView.getChildren(ReferencePane.class);
                    if (paneIndex < nextViewReferencePanes.size()) {
                    	showNextView.selectReferencePane(paneIndex);
                    	nextViewReferencePanes.get(paneIndex).selectReference(reference);
                    }
                } else if (referenceName.length() > 0) {
                    // SELECT_OBJECT and reference pane (specified with reference name)
                    ShowObjectView showNextView = (ShowObjectView) nextView;
                    List<ReferencePane> nextViewReferencePanes = showNextView.getChildren(ReferencePane.class);
                    for(int i = 0; i < nextViewReferencePanes.size(); i++) {
                    	List<Action> selectReferenceActions = nextViewReferencePanes.get(i).getSelectReferenceActions();
                        for(int j = 0; j < selectReferenceActions.size(); j++) {
                            Action selectReferenceAction = selectReferenceActions.get(j);
                            if (selectReferenceAction.getParameter(Action.PARAMETER_REFERENCE_NAME).endsWith(referenceName)) {
                            	showNextView.selectReferencePane(i);
                            	nextViewReferencePanes.get(i).selectReference(j);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Throwables.log(e);
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
