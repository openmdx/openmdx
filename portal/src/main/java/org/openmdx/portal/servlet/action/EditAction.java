/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: EditAction 
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
import java.util.Map;

#if JAVA_8
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
#else
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
#endif

import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.component.EditObjectView;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.ViewMode;

/**
 * EditAction
 *
 */
public class EditAction extends BoundAction {

    public final static int EVENT_ID = 16;

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
    ) throws IOException, ServletException {
    	ObjectView nextView = view;
        ViewPort.Type nextViewPortType = null;        
        ApplicationContext app = view.getApplicationContext();
        try {
        	ObjectView currentView = (ObjectView)view;
        	Path objectIdentity = null;
        	Map<Path,Action> historyActions = null;
        	if(currentView instanceof ShowObjectView) {
        		objectIdentity = currentView.getObject().refGetPath();
        		historyActions = ((ShowObjectView)currentView).createHistoryAppendCurrent();
        	} else {
        		objectIdentity = new Path(Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI));
        		historyActions = new HashMap<Path,Action>();
    	        historyActions.put(
    	            objectIdentity, 
    	            new Action(
    	                SelectObjectAction.EVENT_ID,
    	                new Action.Parameter[] { 
    	                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, objectIdentity.toXRI())
    	                },
    	                "", // title
    	                true
    	            )
    	        );
        	}
            nextView = new EditObjectView(
                currentView.getId(),
                currentView.getContainerElementId(),
                objectIdentity,
                app,
                historyActions,
                currentView.getNextPrevActions(),
                currentView.getLookupType(),
                currentView.getResourcePathPrefix(),
                currentView.getNavigationTarget(),
                ViewMode.valueOf(
                    Action.getParameter(parameter, Action.PARAMETER_MODE)
                )
            );
        } catch (Exception e) {
            Throwables.log(e);
            app.addErrorMessage(
                app.getTexts().getErrorTextCannotEditObject(), new String[] {view.getObject().refMofId(), e.getMessage() }
            );
        }
        return new ActionPerformResult(
            nextView,
            nextViewPortType
        );
    }

}
