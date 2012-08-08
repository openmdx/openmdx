/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: SaveGridAction.java,v 1.2 2011/07/07 22:35:36 wfro Exp $
 * Description: ShowObjectView 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/07/07 22:35:36 $
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
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
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.ViewMode;

public class SaveGridAction extends BoundAction {

	public final static int EVENT_ID = 36;

	@SuppressWarnings("unchecked")
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
        ApplicationContext application = currentView.getApplicationContext();
        if (((Object[]) requestParameters.get(Action.PARAMETER_PANE)).length > 0) {
            int paneIndex = Integer.parseInt(((String[])requestParameters.get(Action.PARAMETER_PANE))[0]);
            if (paneIndex < currentView.getReferencePane().length) {
                Grid grid = currentView.getReferencePane()[paneIndex].getGrid();
                if (grid != null) {
                    for (
                        int i = 1; 
                        i < 999; // max 999 rows
                        i++
                    ) {
                        // Collect all fields of row i
                        Map row = new HashMap();
                        String objectXri = null;
                        boolean hasValues = false;
                        for (Iterator j = requestParameters.keySet().iterator(); j.hasNext();) {
                            String fieldName = (String) j.next();
                            if((fieldName.indexOf("[") >= 0) && (fieldName.indexOf("]") >= 0)) {
                                int rowIndex = 
                                    (Integer.parseInt(
                                        fieldName.substring(
                                            fieldName.indexOf("[") + 1, 
                                            fieldName.indexOf("]"))
                                        ) % 100000
                                    ) / 100;
                                if(i == rowIndex) {
                                    row.put(fieldName, requestParameters.get(fieldName));
                                    Object[] fieldValues = (Object[]) requestParameters.get(fieldName);
                                    if ((fieldValues.length > 0) && (((String) fieldValues[0]).length() > 0)) {
                                        hasValues = true;
                                        if (fieldName.startsWith("refMofId")) {
                                            objectXri = (String) fieldValues[0];
                                        }
                                    }
                                }
                            }
                        }
                        if(hasValues) {
                            RefObject_1_0 parent = currentView.getRefObject();
                            PersistenceManager pm = JDOHelper.getPersistenceManager(parent);
                            EditObjectView editView = null;
                            // Edit existing object
                            if (objectXri.startsWith("xri://@openmdx") || objectXri.startsWith("xri:@openmdx:")) {
                                Path objectIdentity = new Path(objectXri);
                                try {
                                    editView = new EditObjectView(
                                        currentView.getId(),
                                        currentView.getContainerElementId(),
                                        objectIdentity, 
                                        application, 
                                        new LinkedHashMap<Path,Action>(), 
                                        currentView.getLookupType(),
                                        currentView.getRestrictToElements(),
                                        ViewMode.STANDARD
                                    );
                                }
                                catch(ServiceException e) {
                                	SysLog.warning(e.getMessage(), e.getCause());
                                }
                            }
                            // Create new object from existing
                            else if (objectXri.startsWith("clonedFrom:")) {
                                Path objectIdentity = new Path(objectXri.substring("clonedFrom:".length()));
                                RefObject_1_0 existingObject = (RefObject_1_0)pm.getObjectById(objectIdentity);
                                RefObject_1_0 newObject = (RefObject_1_0)parent.refOutermostPackage().refClass(existingObject.refClass().refMofId()).refCreateInstance(null);
                                newObject.refInitialize(existingObject);
                                try {
                                    editView = new EditObjectView(
                                        currentView.getId(), 
                                        currentView.getContainerElementId(),
                                        newObject, 
                                        null, 
                                        application, 
                                        new LinkedHashMap<Path,Action>(), 
                                        currentView.getLookupType(),
                                        currentView.getRestrictToElements(),
                                        parent, 
                                        grid.getGridControl().getObjectContainer().getReferenceName(), 
                                        ViewMode.STANDARD
                                    );
                                }
                                catch(ServiceException e) {
                                	SysLog.warning(e.getMessage(), e.getCause());
                                }
                            }
                            // Create new object
                            else {
                                RefObject_1_0 newObject = (RefObject_1_0)parent.refOutermostPackage().refClass(objectXri).refCreateInstance(null);
                                try {
                                    editView = new EditObjectView(
                                        currentView.getId(), 
                                        currentView.getContainerElementId(),
                                        newObject, 
                                        null, 
                                        application, 
                                        new LinkedHashMap<Path,Action>(), 
                                        currentView.getLookupType(),
                                        currentView.getRestrictToElements(),
                                        parent, 
                                        grid.getGridControl().getObjectContainer().getReferenceName(), 
                                        ViewMode.STANDARD
                                    );
                                }
                                catch(ServiceException e) {
                                	SysLog.warning(e.getMessage(), e.getCause());
                                }
                            }
                            // Process edit request
                            if(editView != null) {
                                try {
                                    editView.storeObject(row, new HashMap());
                                }
                                catch (Exception e) {
                                    ServiceException e0 = new ServiceException(e);
                                    currentView.handleCanNotCommitException(e0.getCause());
                                }
                            }
                        }
                    }
                    try {
                        nextView.refresh(true, true);
                    }
                    catch (Exception e) {
                    }
                }
            }
        }
        return new ActionPerformResult(
            nextView,
            nextViewPortType
        );
    }
        
}
