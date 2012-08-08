/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: EditObjectEventHandler.java,v 1.42 2010/09/24 16:02:57 wfro Exp $
 * Description: EditObjectEventHandler 
 * Revision:    $Revision: 1.42 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/09/24 16:02:57 $
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
package org.openmdx.portal.servlet.eventhandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.ObjectCreationResult;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.ViewMode;

public class EditObjectEventHandler {

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static HandleEventResult handleEvent(
        int event,
        EditObjectView currentView,
        HttpServletRequest request,
        HttpServletResponse response,        
        String parameter,
        HttpSession session,
        Map parameterMap,
        ViewsCache editViewsCache,
        ViewsCache showViewsCache      
    ) {
        ObjectView nextView = currentView;
        ViewPort.Type nextViewPortType = null;
        ApplicationContext application = currentView.getApplicationContext();
        switch(event) {

            case Action.EVENT_SAVE: {
                Map attributeMap = new HashMap();    
                boolean hasErrors;
                try {  
                    currentView.storeObject(
                        parameterMap,
                        attributeMap
                    );
                    List errorMessages = application.getErrorMessages();
                    hasErrors = !errorMessages.isEmpty();
                    if(hasErrors) {
                        errorMessages.clear();                        
                    }
                }
                catch(Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning(e0.getMessage(), e0.getMessage());
                    SysLog.warning(e0.getMessage(), e0.getCause());
                    currentView.handleCanNotCommitException(e0.getCause());                    
                    hasErrors = true;
                }
                if(hasErrors) {
                    try {
                        RefObject_1_0 workObject = null;
                        try {
                        	// Try to clone current object and use it as new working object
                        	workObject = PersistenceHelper.clone(currentView.getRefObject());
                        }
                        catch(Exception e) {
                            // If cloning fails, create a new empty instance
                        	workObject = (RefObject_1_0)currentView.getRefObject().refClass().refCreateInstance(null);                        	
                            workObject.refInitialize(false, false);
                        }
                        // Initialize with received attribute values. This also updates the error messages
                        application.getPortalExtension().updateObject(
                            workObject,
                            parameterMap,
                            attributeMap,
                            application
                        );
                        nextView = new EditObjectView(
                            currentView.getId(),
                            currentView.getContainerElementId(),
                            workObject,
                            currentView.getEditObjectIdentity(),
                            application,
                            currentView.getHistoryActions(),
                            currentView.getLookupType(),
                            currentView.getRestrictToElements(),
                            currentView.getParentObject(),
                            currentView.getForReference(),
                            currentView.getMode()
                        );            
                    }
                    // Can not stay in edit object view. Return to returnToView as fallback
                    catch(Exception e1) {
                        nextView = currentView.getPreviousView(null);
                    }
                }
                else {
                    nextView = currentView.getPreviousView(showViewsCache);
                    if(nextView instanceof ShowObjectView) {
	                    if(!currentView.isEditMode()) {
	                        // Set created object as result if next view is ShowObjectView
	                        // This shows the reference to the newly created object the same
	                        // way as an operation result
	                        ObjectReference createdObject =
	                            new ObjectReference(
	                                currentView.getRefObject(),
	                                application
	                            );                    
	                        ((ShowObjectView)nextView).setCreateObjectResult(
	                            new ObjectCreationResult(
	                                currentView.getRefObject().refMofId(),
	                                createdObject.getLabel(),
	                                createdObject.getTitle(),
	                                createdObject.getIconKey()
	                            )
	                        );
	                    }
                    	try {
                    		nextView.refresh(true);
                    	} catch(Exception e) {
                    		new ServiceException(e).log();
                    	}                    	
                    }
                    // Object is saved. EditObjectView is not required any more. Remove 
                    // it from the set of open EditObjectViews.
                    editViewsCache.removeView(
                        currentView.getRequestId()
                    );
                    // Paint attributes if view is embedded
                    if(currentView.getMode() == ViewMode.EMBEDDED) {
                        nextViewPortType = ViewPort.Type.EMBEDDED;
                    }
                }
                break;
            }

            case Action.EVENT_CANCEL:
                editViewsCache.removeView(
                    currentView.getRequestId()
                );
                nextView = currentView.getPreviousView(showViewsCache);
                // If the view is embedded paint attribute pane
                if(currentView.getMode() == ViewMode.EMBEDDED) {
                    nextViewPortType = ViewPort.Type.EMBEDDED;
                }                
                break;

        }
        if(nextView instanceof ShowObjectView) {
            ((ShowObjectView)nextView).selectReferencePane(
                currentView.getForReference()
            );
        }
        return new HandleEventResult(
            nextView,
            nextViewPortType
        );
    }
    
    //-------------------------------------------------------------------------
    public static boolean acceptsEvent(
        int event
    ) {
        return
            (event == Action.EVENT_SAVE) ||
            (event == Action.EVENT_CANCEL);
    }

}
