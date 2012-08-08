/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: EditObjectEventHandler.java,v 1.11 2008/01/27 00:37:49 wfro Exp $
 * Description: ShowObjectView 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/27 00:37:49 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.eventhandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.ObjectCreationResult;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ShowObjectView;

public class EditObjectEventHandler {

    //-------------------------------------------------------------------------
    public static ObjectView handleEvent(
        int event,
        EditObjectView currentView,
        String parameter,
        HttpSession session,
        Map parameterMap,
        ViewsCache currentEditViews,
        ViewsCache currentShowViews      
    ) {

        ObjectView nextView = currentView;
        ApplicationContext application = currentView.getApplicationContext();
        switch(event) {

            case Action.EVENT_SAVE: {
                RefPackage_1_0 pkg = (RefPackage_1_0)currentView.getRefObject().refOutermostPackage();
                Map attributeMap = new HashMap();    
                try {  
                    currentView.storeObject(
                        parameterMap,
                        attributeMap
                    );
                    nextView = currentView.getPreviousView();
                    if(!currentView.isEditMode() && (nextView instanceof ShowObjectView)) {
                        // Refresh derived attributes of newly created objects
                        currentView.getRefObject().refRefresh();
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
                    EventHandlerHelper.notifyObjectModified(
                        currentShowViews,
                        currentView.getRefObject()
                    );
                    // Object is saved. EditObjectView is not required any more. Remove 
                    // it from the set of open EditObjectViews.
                    currentEditViews.removeView(
                        currentView.getRequestId()
                    );
                    // If view is modal set 'window.close();' macro
                    if(currentView.isModal()) {
                        nextView.setMacro(
                            new Object[]{
                                new Integer(Action.MACRO_TYPE_JAVASCRIPT),
                                "window.location.href='close-window.html';",
                                Collections.EMPTY_LIST
                            }
                        );
                    }
                }
                // In case of an exception stay with edit object view and
                // let the user fix the input data
                catch(JmiServiceException e) {
                    AppLog.warning(e.getMessage(), e.getCause(), 1);
                    try {
                        pkg.refRollback();
                    } 
                    catch(Exception e0) {}
                    try {
                        // create a new empty instance ...
                        RefObject_1_0 workObject = (RefObject_1_0)pkg.refClass(currentView.getRefObject().refClass().refMofId()).refCreateInstance(null);
                        // ... and initialize with received attribute values
                        application.getPortalExtension().updateObject(
                            workObject,
                            parameterMap,
                            attributeMap,
                            application,
                            pkg
                        );
                        nextView = new EditObjectView(
                            currentView.getId(),
                            currentView.getContainerElementId(),
                            workObject,
                            currentView.getEditObjectRefMofId(),
                            application,
                            currentView.getHistoryActions(),
                            currentView.getLookupType(),
                            currentView.getRestrictToElements(),
                            currentView.getParentObject(),
                            currentView.getForReference(),
                            currentView.isModal(),
                            currentView.getControlFactory()
                        );                
                        // Current view is dirty. Remove it and replace it by newly created. 
                        currentEditViews.removeView(
                            currentView.getRequestId()
                        );
                    }
                    // Can not stay in edit object view. Return to returnToView
                    // as fallback
                    catch(Exception e0) {
                        nextView =  currentView.getPreviousView();
                    }
                    currentView.handleCanNotCommitException(e.getExceptionStack());
                }
                catch(Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    try {
                        pkg.refRollback();
                    } 
                    catch(Exception e1) {}
                    try {
                        // create a new empty instance ...
                        RefObject_1_0 workObject = (RefObject_1_0)pkg.refClass(currentView.getRefObject().refClass().refMofId()).refCreateInstance(null);
                        // ... and initialize with received attribute values
                        application.getPortalExtension().updateObject(
                            workObject,
                            parameterMap,
                            attributeMap,
                            application,
                            pkg
                        );
                        nextView = new EditObjectView(
                            currentView.getId(),
                            currentView.getContainerElementId(),
                            workObject,
                            currentView.getEditObjectRefMofId(),
                            application,
                            currentView.getHistoryActions(),
                            currentView.getLookupType(),
                            currentView.getRestrictToElements(),
                            currentView.getParentObject(),
                            currentView.getForReference(),
                            currentView.isModal(),
                            currentView.getControlFactory()
                        );
                        // Current view is dirty. Remove it and replace it by newly created. 
                        currentEditViews.removeView(
                            currentView.getRequestId()
                        );
                    }
                    // Can not stay in edit object view. Return to returnToView
                    // as fallback
                    catch(Exception e1) {
                        nextView = currentView.getPreviousView();
                    }
                    currentView.handleCanNotCommitException(e0.getExceptionStack());
                }
                break;
            }

            case Action.EVENT_CANCEL:
                currentEditViews.removeView(
                    currentView.getRequestId()
                );
                nextView = currentView.getPreviousView();
                // If view is modal set 'window.close();' macro
                if(currentView.isModal()) {
                    nextView.setMacro(
                        new Object[]{
                            new Integer(Action.MACRO_TYPE_JAVASCRIPT),
                            "window.location.href='close-window.html';",
                            Collections.EMPTY_LIST
                        }
                    );
                }
                break;

        }
        if(nextView instanceof ShowObjectView) {
            ((ShowObjectView)nextView).selectReferencePane(
                currentView.getForReference()
            );
        }
        return nextView;
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
