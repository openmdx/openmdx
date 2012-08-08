/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: GridEventHandler.java,v 1.30 2008/01/27 00:37:48 wfro Exp $
 * Description: ShowObjectView 
 * Revision:    $Revision: 1.30 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/27 00:37:48 $
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jmi.reflect.RefObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.HtmlPageFactory;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.control.ReferencePaneControl;
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.View;

public class GridEventHandler {

    //-----------------------------------------------------------------------
    public static void handleEvent(
        int event,
        ObjectView view,
        HttpServletRequest request,
        HttpServletResponse response,
        ApplicationContext application,
        String parameter,
        Map parameterMap,
        ViewsCache showViewsCache
    ) throws IOException {
        if(view instanceof ShowObjectView) {
            ShowObjectView showView = (ShowObjectView)view;
            HtmlPage p = HtmlPageFactory.openPage(
                view,
                request,
                EventHandlerHelper.getWriter(request, response)
            );
            p.setProperty(
                HtmlPage.PROPERTY_POPUP_IMAGES,
                new HashMap()
            );            
            switch(event) {
            
                case Action.EVENT_SELECT_REFERENCE:
                    try {
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                grid.setPage(grid.getCurrentPage(), Grid.getPageSizeParameter(parameterMap));
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch(ServiceException e) {
                        AppLog.warning(e.getMessage(), e.getCause(), 1);
                    }
                    break;
                        
                case Action.EVENT_PAGE_NEXT:
                    try {
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                grid.setPage(grid.getCurrentPage() + 1, Grid.getPageSizeParameter(parameterMap));
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
        
                case Action.EVENT_PAGE_PREVIOUS:
                    try {
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                grid.setPage(grid.getCurrentPage() - 1, Grid.getPageSizeParameter(parameterMap));
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
                        
                case Action.EVENT_SET_PAGE:
                    try {
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        int pageNumber = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PAGE));
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                grid.setPage(pageNumber, Grid.getPageSizeParameter(parameterMap));
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
                       
                case Action.EVENT_SELECT_FILTER:
                    try {
                        Object[] parameterValues = (Object[]) parameterMap.get(WebKeys.REQUEST_PARAMETER_FILTER_VALUES);
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        String filterValues = parameterValues == null ? null : (parameterValues.length > 0 ? (String) parameterValues[0] : null);
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        String filterName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if(grid != null) {
                                grid.setShowRows(true);
                                grid.selectFilter(filterName, filterValues);
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
    
                case Action.EVENT_SET_COLUMN_FILTER:
                case Action.EVENT_ADD_COLUMN_FILTER:
                    try {
                        Object[] parameterValues = (Object[]) parameterMap.get(WebKeys.REQUEST_PARAMETER_FILTER_VALUES);
                        String filterValues = parameterValues == null ? null : (parameterValues.length > 0 ? (String) parameterValues[0] : null);
                        boolean addFilter = (event == Action.EVENT_ADD_COLUMN_FILTER);
                        if ((filterValues != null) && filterValues.startsWith("+")) {
                            filterValues = filterValues.substring(1);
                            addFilter = true;
                        }
                        else if ((filterValues != null) && filterValues.startsWith("AND")) {
                            filterValues = filterValues.substring(3);
                            addFilter = true;
                        }
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        String filterName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                grid.setShowRows(true);
                                grid.setColumnFilter(filterName, filterValues, addFilter, Grid.getPageSizeParameter(parameterMap));
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
        
                case Action.EVENT_SET_ORDER_ASC:
                case Action.EVENT_ADD_ORDER_ASC:
                    try {
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        String featureName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                grid.setOrder(featureName, Orders.ASCENDING, event == Action.EVENT_ADD_ORDER_ASC);
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
        
                case Action.EVENT_SET_ORDER_DESC:
                case Action.EVENT_ADD_ORDER_DESC:
                    try {
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        String featureName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                grid.setOrder(featureName, Orders.DESCENDING, event == Action.EVENT_ADD_ORDER_DESC);
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
        
                case Action.EVENT_SET_ORDER_ANY:
                case Action.EVENT_ADD_ORDER_ANY:
                    try {
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        String featureName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                grid.setOrder(featureName, Orders.ANY, event == Action.EVENT_ADD_ORDER_ANY);
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
                        
                case Action.EVENT_SET_GRID_ALIGNMENT_WIDE:
                case Action.EVENT_SET_GRID_ALIGNMENT_NARROW:
                    try {
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                grid.setAlignment(
                                    event == Action.EVENT_SET_GRID_ALIGNMENT_WIDE 
                                        ? Grid.ALIGNMENT_WIDE
                                        : Grid.ALIGNMENT_NARROW
                                );
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
    
                case Action.EVENT_SET_CURRENT_FILTER_AS_DEFAULT:
                    try {
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                grid.setCurrentFilterAsDefault();
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
    
                case Action.EVENT_SET_SHOW_ROWS_ON_INIT:
                case Action.EVENT_SET_HIDE_ROWS_ON_INIT:
                    try {
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        int referenceIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_REFERENCE));
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            referencePanes[paneIndex].selectReference(referenceIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                grid.setShowGridContentOnInit(
                                    event == Action.EVENT_SET_SHOW_ROWS_ON_INIT
                                );
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
                    
                case Action.EVENT_ADD_OBJECT:
                    try {
                        ReferencePane[] referencePanes = showView.getReferencePane();
                        int paneIndex = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_PANE));
                        if (paneIndex < referencePanes.length) {
                            showView.selectReferencePane(paneIndex);
                            Grid grid = referencePanes[paneIndex].getGrid();
                            if (grid != null) {
                                String feature = Action.getParameter(parameter, Action.PARAMETER_REFERENCE);
                                String addRemoveMode = Action.getParameter(parameter, Action.PARAMETER_NAME);
                                RefPackage_1_0 rootPkg = (RefPackage_1_0)showView.getRefObject().refOutermostPackage();
                                Collection values = (Collection)showView.getRefObject().refGetValue(feature);
                                // test whether all referenced objects are accessible.
                                // If object is not accessible remove it from list.
                                // TODO: objects may be temporarly not available. In this
                                // case they should not be removed
                                rootPkg.refBegin();
                                for (Iterator i = values.iterator(); i.hasNext();) {
                                    RefObject r = (RefObject) i.next();
                                    if (r == null)
                                        i.remove();
                                }
                                // Add/Remove objects         
                                Object[] parameterList = (Object[])parameterMap.get(WebKeys.REQUEST_PARAMETER_LIST);
                                StringTokenizer tokenizer = new StringTokenizer(
                                    (parameterList != null) && (parameterList.length > 0) ? (String)parameterList[0] : "", 
                                    " "
                                );
                                while (tokenizer.hasMoreTokens()) {
                                    String refMofId = Action.getParameter(tokenizer.nextToken(), Action.PARAMETER_OBJECTXRI);
                                    Path refPath = new Path(refMofId);
                                    // Must be at least a segment
                                    if(refPath.size() >= 5) {
                                        RefObject referencedObject = ((RefPackage_1_0)showView.getRefObject().refOutermostPackage()).refObject(refPath.toXri());
                                        if ("+".equals(addRemoveMode)) {
                                            AppLog.trace("adding referenced object");
                                            values.add(referencedObject);
                                        }
                                        else {
                                            AppLog.trace("removing referenced object");
                                            values.remove(referencedObject);
                                        }
                                    }
                                }
                                rootPkg.refCommit();
                                referencePanes[paneIndex].getReferencePaneControl().paint(
                                    p,
                                    ReferencePaneControl.FRAME_CONTENT,
                                    false
                                );                          
                            }
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
    
                case Action.EVENT_GRID_GET_ROW_MENU:
                    try {
                        String targetRowXri = Action.getParameter(parameter, Action.PARAMETER_TARGETXRI);
                        String rowId = Action.getParameter(parameter, Action.PARAMETER_ROW_ID);
                        if(
                            (targetRowXri != null) && (targetRowXri.length() > 0) &&
                            (rowId != null) && (rowId.length() > 0)
                        ) {
                            String gridRowDetailsId = "gridRow" + rowId + "-details";
                            ShowObjectView selectedObjectView = new ShowObjectView(
                                view.getId(),
                                gridRowDetailsId,
                                targetRowXri,
                                application,
                                view.getHistoryActions(),
                                null,
                                new HashMap(),
                                view.getControlFactory()
                            );                            
                            selectedObjectView.createRequestId();
                            showViewsCache.addView(
                                selectedObjectView.getRequestId(),
                                selectedObjectView
                            );
                            HtmlEncoder_1_0 htmlEncoder = application.getHtmlEncoder();
                            p.write("<span class=\"gridMenu\" onclick=\"try{this.parentNode.parentNode.onclick();}catch(e){};\">");
                            p.write("  <ul id=\"nav\" class=\"nav\">");
                            p.write("    <li><a href=\"#\"><img id=\"gridRow", rowId, "-details-opener\" border=\"0\" height=\"16\" width=\"15\" align=\"bottom\" alt=\"\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_MENU_DOWN, "\" /></a>");
                            p.write("      <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                            Action getAttributesAction = selectedObjectView.getObjectReference().getGetAttributesAction(); 
                            p.write("        <li><a", p.getOnClick("javascript:$('", gridRowDetailsId, "').parentNode.className+=' wait';$('", gridRowDetailsId, "').innerHTML='';new Ajax.Updater('", gridRowDetailsId, "', ", selectedObjectView.getEvalHRef(getAttributesAction), ", {asynchronous:true, evalScripts: true, onComplete: function(){$('", gridRowDetailsId, "').parentNode.className='';}});$('gridRow", rowId, "-details-closer').style.display='block';return false;"), " href=\"#\">000 - ", htmlEncoder.encode(getAttributesAction.getTitle(), false), "</a></li>");                            
                            // Edit object action as macro
                            // 1. select object
                            // 2. switch into edit mode
                            Action editObjectAction = selectedObjectView.getObjectReference().getEditObjectAction(true);
                            if(editObjectAction.isEnabled()) {
                                Action editObjectMacro = new Action(
                                    Action.EVENT_MACRO,
                                    new Action.Parameter[]{
                                        new Action.Parameter(Action.PARAMETER_OBJECTXRI, selectedObjectView.getRefObject().refMofId()),
                                        new Action.Parameter(
                                            Action.PARAMETER_NAME, 
                                            Base64.encode(
                                                ("window.location.href=" + view.getEvalHRef(editObjectAction, View.REQUEST_ID_FORMAT_TEMPLATE)).getBytes()
                                            )
                                        ),
                                        new Action.Parameter(Action.PARAMETER_TYPE, Integer.toString(Action.MACRO_TYPE_JAVASCRIPT))                    
                                    },
                                    application.getTexts().getEditTitle(),
                                    true
                                );                            
                                p.write("        <li><a", p.getOnClick("javascript:window.open(", p.getEvalHRef(editObjectMacro, true), ", '_blank');return false;"), " href=\"#\">001 - ", htmlEncoder.encode(editObjectMacro.getTitle(), false), "</a></li>");
                            }
                            else {
                                p.write("        <li><a href=\"#\"><span>001 - ", htmlEncoder.encode(application.getTexts().getEditTitle(), false), "</span></a></li>");                                
                            }
                            ReferencePane[] referencePanes = selectedObjectView.getReferencePane();
                            List secondLevelMenuEntryActions = new ArrayList();
                            for(
                                int i = 0; 
                                i < referencePanes.length; 
                                i++
                            ) {
                                ReferencePane referencePane = referencePanes[i];
                                Action[] selectReferenceActions = referencePane.getSelectReferenceAction();
                                if(selectReferenceActions.length > 0) {
                                    for(
                                        int j = 0;
                                        j < selectReferenceActions.length;
                                        j++
                                    ) {                                        
                                        int detailTabIndex = 100*referencePane.getReferencePaneControl().getPaneIndex() + j;
                                        String title = selectReferenceActions[j].getTitle();
                                        if(title.startsWith(WebKeys.TAB_GROUPING_CHARACTER) || (title.length() < 2)) {
                                            secondLevelMenuEntryActions.add(
                                                new Object[]{new Integer(detailTabIndex), selectReferenceActions[j]}
                                            );
                                        }
                                        else {
                                            p.write("        <li><a", p.getOnClick("javascript:$('", gridRowDetailsId, "').parentNode.className+=' wait';$('", gridRowDetailsId, "').innerHTML='';new Ajax.Updater('", gridRowDetailsId, "', ", selectedObjectView.getEvalHRef(selectReferenceActions[j]), ", {asynchronous:true, evalScripts: true, onComplete: function(){$('", gridRowDetailsId, "').parentNode.className='';try{makeZebraTable('gridTable", gridRowDetailsId, "-", Integer.toString(detailTabIndex), "',1);}catch(e){};}});$('gridRow", rowId, "-details-closer').style.display='block';return false;"), " href=\"#\">", Integer.toString(i+1), (j < 10 ? "0" + Integer.toString(j) : Integer.toString(j)), " - ", htmlEncoder.encode(title, false), "</a></li>");
                                        }
                                    }
                                }
                            }
                            if(!secondLevelMenuEntryActions.isEmpty()) {
                                p.write("        <li>&#183;&#183;&#183;");
                                p.write("          <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                                int ii = 0;
                                for(Iterator i = secondLevelMenuEntryActions.iterator(); i.hasNext(); ii++) {
                                    Object[] entry = (Object[])i.next();
                                    int detailTabIndex = ((Number)entry[0]).intValue();
                                    Action selectReferenceAction = (Action)entry[1];
                                    String title = selectReferenceAction.getTitle();
                                    title = title.startsWith(WebKeys.TAB_GROUPING_CHARACTER)
                                        ? title.substring(1)
                                        : title;
                                    p.write("        <li><a", p.getOnClick("javascript:$('", gridRowDetailsId, "').parentNode.className+=' wait';$('", gridRowDetailsId, "').innerHTML='';new Ajax.Updater('", gridRowDetailsId, "', ", selectedObjectView.getEvalHRef(selectReferenceAction), ", {asynchronous:true, evalScripts: true, onComplete: function(){$('", gridRowDetailsId, "').parentNode.className='';try{makeZebraTable('gridTable", gridRowDetailsId, "-", Integer.toString(detailTabIndex), "',1);}catch(e){};}});$('gridRow", rowId, "-details-closer').style.display='block';return false;"), " href=\"#\">", Integer.toString(100*(referencePanes.length+1)+ii), " - ", htmlEncoder.encode(title, false), "</a></li>");
                                }
                                p.write("          </ul>");
                                p.write("        </li>");
                            }
                            p.write("      </ul>");
                            p.write("    </li>");
                            p.write("  </ul>");
                            p.write("  <script language=\"javascript\" type=\"text/javascript\">");
                            p.write("    try{");
                            p.write("      sfinit($('gridRow", rowId, "-details-opener').parentNode.parentNode.parentNode);");
                            p.write("      $('gridRow", rowId, "-details-opener').click();");
                            p.write("    } catch(e){}");
                            p.write("  </script>");                    
                            p.write("</span>");
                        }
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
                    
                case Action.EVENT_GET_OBJECT_ATTRIBUTES:                    
                    try {
                        showView.getAttributePane().getAttributePaneControl().paint(
                            p, 
                            false
                        );
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        AppLog.warning(e0.getMessage(), e0.getCause(), 1);
                    }
                    break;
                    
            }
            try {
                p.close(true);
            }
            catch (Exception e) {
                ServiceException e0 = new ServiceException(e);
                AppLog.warning(e0.getMessage(), e0.getCause(), 1);
            }            
        }        
    }

    //-------------------------------------------------------------------------
    public static boolean acceptsEvent(
        int event
    ) {
        return
            (event == Action.EVENT_SELECT_REFERENCE) ||
            (event == Action.EVENT_PAGE_NEXT) ||
            (event == Action.EVENT_SET_PAGE) ||
            (event == Action.EVENT_PAGE_PREVIOUS) ||
            (event == Action.EVENT_SELECT_FILTER) ||        
            (event == Action.EVENT_SET_COLUMN_FILTER) ||
            (event == Action.EVENT_ADD_COLUMN_FILTER) ||
            (event == Action.EVENT_SET_ORDER_ASC) ||
            (event == Action.EVENT_ADD_ORDER_ASC) ||
            (event == Action.EVENT_SET_ORDER_DESC) ||
            (event == Action.EVENT_ADD_ORDER_DESC) ||
            (event == Action.EVENT_SET_ORDER_ANY) ||
            (event == Action.EVENT_ADD_ORDER_ANY) ||
            (event == Action.EVENT_SET_GRID_ALIGNMENT_WIDE) ||
            (event == Action.EVENT_SET_GRID_ALIGNMENT_NARROW) ||
            (event == Action.EVENT_SET_CURRENT_FILTER_AS_DEFAULT) ||
            (event == Action.EVENT_ADD_OBJECT) ||
            (event == Action.EVENT_SET_SHOW_ROWS_ON_INIT) ||
            (event == Action.EVENT_GRID_GET_ROW_MENU) ||
            (event == Action.EVENT_GET_OBJECT_ATTRIBUTES) ||
            (event == Action.EVENT_SET_HIDE_ROWS_ON_INIT);
    }

}
