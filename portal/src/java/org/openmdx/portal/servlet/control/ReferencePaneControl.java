/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ReferencePaneControl.java,v 1.139 2008/06/01 11:26:18 wfro Exp $
 * Description: ReferencePaneControl
 * Revision:    $Revision: 1.139 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/01 11:26:18 $
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Autocompleter_1_0;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.BooleanValue;
import org.openmdx.portal.servlet.attribute.CodeValue;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.attribute.NullValue;
import org.openmdx.portal.servlet.attribute.NumberValue;
import org.openmdx.portal.servlet.attribute.ObjectReferenceValue;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.Grid;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;

//-----------------------------------------------------------------------------
public class ReferencePaneControl
    extends PaneControl
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public ReferencePaneControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.ReferencePane pane,
        String containerClass,
        int paneIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory,
            pane,
            paneIndex
        );
        
        List<Action> references = new ArrayList<Action>();
        this.gridControl = new GridControl[pane.getMember().size()];
        for(
            int i = 0; 
            i < pane.getMember().size(); 
            i++
        ) {
          org.openmdx.ui1.jmi1.Tab tab = (org.openmdx.ui1.jmi1.Tab)pane.getMember().get(i);
          org.openmdx.ui1.jmi1.ObjectContainer objectContainer = (org.openmdx.ui1.jmi1.ObjectContainer)tab.getMember().get(0);
          String title = 
              localeAsIndex < tab.getTitle().size()
                  ? tab.getTitle().get(localeAsIndex)
                  : tab.getTitle().get(0);
          String toolTip = 
              localeAsIndex < tab.getToolTip().size()
                  ? tab.getToolTip().get(localeAsIndex)
                  : title;              
          references.add(
              new Action(
                  Action.EVENT_SELECT_REFERENCE,
                  new Action.Parameter[]{
                      new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(this.getPaneIndex())),
                      new Action.Parameter(Action.PARAMETER_REFERENCE, Integer.toString(i)),
                      new Action.Parameter(Action.PARAMETER_REFERENCE_NAME, objectContainer.getReferenceName()),                      
                  },
                  title,
                  toolTip,
                  tab.getIconKey(),
                  true
              )
          );      
          this.gridControl[i] = controlFactory.createGridControl(
              Integer.toString(i),
              locale,
              localeAsIndex,
              (org.openmdx.ui1.jmi1.Tab)pane.getMember().get(i),
              this.getPaneIndex(),
              containerClass
          );
        }
        this.selectReferenceActions = (Action[])references.toArray(new Action[references.size()]);

    }
      
    //-------------------------------------------------------------------------
    public Action getRowMenuAction(
        String targetRowXri,
        long rowId
    ) {
        return new Action(
            Action.EVENT_GRID_GET_ROW_MENU,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_TARGETXRI, targetRowXri),
                new Action.Parameter(Action.PARAMETER_ROW_ID, Long.toString(rowId))
            },
            "",
            true
        );
    }
        
    //-------------------------------------------------------------------------
    public GridControl[] getGridControl(
    ) {
        return this.gridControl;
    }
  
    //-------------------------------------------------------------------------
    public Action[] getSelectReferenceAction(
    ) {
        return this.selectReferenceActions;
    }
    
    //-------------------------------------------------------------------------
    public void setIsMultiDeleteEnabled(
        boolean newValue
    ) {
        this.isMultiDeleteEnabled = newValue;
    }
    
    //-------------------------------------------------------------------------
    public boolean getIsMultiDeleteEnabled(
    ) {
        return this.isMultiDeleteEnabled;
    }
    
    //-------------------------------------------------------------------------
    private Autocompleter_1_0 getAutocompleter(
        RefObject_1_0 target,
        String qualifiedFeatureName,
        ApplicationContext application
    ) {
        return application.getPortalExtension().getAutocompleter(
            application,
            target,
            qualifiedFeatureName
        );
    }
    
    //---------------------------------------------------------------------------------
    @Override
    public void paint(
        HtmlPage p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        AppLog.detail("> paint");
       
        ShowObjectView view = (ShowObjectView)p.getView();
        ApplicationContext app = view.getApplicationContext();
        ReferencePane referencePane = view.getReferencePane()[this.getPaneIndex()];
        Texts_1_0 texts = app.getTexts();
        HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
            
        int paneIndex = this.getPaneIndex();
        String containerId = view.getContainerElementId() == null
            ? "gridContent" + Integer.toString(paneIndex)
            : view.getContainerElementId();
        
        // View
        int zIndex = (view.getReferencePane().length - this.getPaneIndex()) * 10;
        if(FRAME_VIEW.equals(frame)) {
            p.write("<div class=\"gTabPanel\" style=\"position:relative;z-index:", Integer.toString(zIndex+1), ";\">");
            boolean isGroupTabActive = false;
            int nReferences = this.getSelectReferenceAction().length;
            for(int i = 0; i < nReferences; i++) {
                Action action = this.getSelectReferenceAction()[i];
                int tabIndex = 100*paneIndex + i;
                String tabId = view.getContainerElementId() == null
                    ? Integer.toString(tabIndex)
                    : view.getContainerElementId() + "-" + Integer.toString(tabIndex);                                    
                String tabTitle = action.getTitle();
                boolean isGroupTab = tabTitle.startsWith(WebKeys.TAB_GROUPING_CHARACTER);
                if(isGroupTab) tabTitle = tabTitle.substring(1);
                if(!isGroupTabActive && isGroupTab) {
                    isGroupTabActive = true;
                    p.write("  <a href=\"#\" onclick=\"javascript:gTabSelect(this, true);return false;\">", WebKeys.TAB_GROUPING_CHARACTER, "</a>");                        
                }
                if(isGroupTabActive && (!isGroupTab || (i == nReferences-1))) {
                    isGroupTabActive = false;
                }
                String tabClass = i == referencePane.getSelectedReference()
                    ? "selected"
                    : isGroupTabActive
                        ? "hidden"
                        : "";
                p.write("  <a href=\"#\" class=\"", tabClass, "\"", p.getOnClick("javascript:gTabSelect(this);new Ajax.Updater('", containerId, "', ", p.getEvalHRef(action), ", {asynchronous:true, evalScripts: true, onComplete: function(){try{makeZebraTable('gridTable", tabId, "',1);}catch(e){};}});return false;"), ">", tabTitle, "</a>");
            }
            p.write("</div>");
            p.write("<div id=\"", containerId, "\" class=\"gContent\" style=\"position:relative;z-index:", Integer.toString(zIndex), ";\">");
            p.write("  <div class=\"loading\" style=\"height:40px;\"></div>");
            p.write("</div>");                
        }
        // Content
        else if(FRAME_CONTENT.equals(frame)) {
            Grid grid = referencePane.getGrid();
            GridControl gridControl = grid.getGridControl();
            int tabIndex = paneIndex*100 + referencePane.getSelectedReference();
            String tabId = view.getContainerElementId() == null
                ? Integer.toString(tabIndex)
                : view.getContainerElementId() + "-" + Integer.toString(tabIndex);                
            String updateTabScriptletPre = null;
            String updateTabScriptletPost = null;
            updateTabScriptletPre = "new Ajax.Updater('" + containerId + "', ";
            updateTabScriptletPost = ", {asynchronous:true, evalScripts:true, onComplete: function(){try{makeZebraTable('gridTable" + tabId + "',1);}catch(e){};}});loadingIndicator($('" + containerId + "'));return false;";
            Action selectGridTabAction = referencePane.getSelectReferenceAction()[referencePane.getSelectedReference()];
            if(grid != null) {
                
                // Get grid rows. This also brings all grid properties to a consistent state
                AppLog.detail("grid rows");
                Object[] rows = grid.getRow();            
                
                // Paging actions and indicators
                Action firstPageAction = grid.getFirstPageAction();
                boolean pageNextIsEnabled = grid.getCurrentPage() < grid.getLastPage();
                boolean pagePreviousIsEnabled = grid.getCurrentPage() > 0;
                Action pageNextAction = grid.getPageNextAction(pageNextIsEnabled);
                Action pageNextFastAction = grid.getPageNextFastAction(pageNextIsEnabled); 
                Action pagePreviousAction = grid.getPagePreviousAction(pagePreviousIsEnabled);
                Action pagePreviousFastAction = grid.getPagePreviousFastAction(pagePreviousIsEnabled); 
                org.openmdx.portal.servlet.Filter[] filters = grid.getFilters();

                // Non-composite, multi-valued reference
                if(grid.getAddObjectAction() != null) {
                    // Grid operations only for grids in non-embedded views
                    if(view.getContainerElementId() == null) {                    
                        Action addObjectAction = grid.getAddObjectAction();
                        Action removeObjectAction = grid.getRemoveObjectAction();
                        String lookupId = org.openmdx.kernel.id.UUIDs.getGenerator().next().toString();
                        String adderFieldId = "addObject[" + tabId + "]";
                        ObjectReference objectReference = view.getObjectReference();
                        Autocompleter_1_0 autocompleter = this.getAutocompleter(
                            objectReference.getObject(),
                            addObjectAction.getParameter("reference"),
                            app
                        );                                
                        p.write("<div id=\"menuOpPanel\" class=\"menuOpPanel\">");
                        p.write("  <table cellspacing=\"0\" cellpadding=\"0\" id=\"menuOp\" width=\"100%\">");
                        p.write("    <tr>");
                        p.write("      <td>");
                        p.write("        <div id=\"showGridButtons", tabId, "\">");
                        p.write("          <ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");
                        p.write("            <li><a href=\"#\" onclick=\"javascript:return false;\">", htmlEncoder.encode(texts.getEditTitle(), false), "&nbsp;&nbsp;&nbsp;</a>");
                        p.write("              <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                        p.write("                <li><a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(addObjectAction), "+'&amp;", WebKeys.REQUEST_PARAMETER_LIST, "=", Action.PARAMETER_OBJECTXRI, "*('+encodeURIComponent($F('", adderFieldId, "'))+')'", updateTabScriptletPost), ">", htmlEncoder.encode(texts.getAddObjectTitle(), false), "</a></li>");
                        p.write("                <li><a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(removeObjectAction), "+'&amp;", WebKeys.REQUEST_PARAMETER_LIST, "='+encodeURIComponent(getSelectedGridRows('gridTable", tabId, "',1))", updateTabScriptletPost), ">", htmlEncoder.encode(texts.getRemoveObjectTitle(), false), "</a></li>");
                        p.write("              </ul>");
                        p.write("            </li>");
                        p.write("          </ul>");
                        p.write("        </div>");
                        p.write("      </td>");
                        p.write("    </tr>");
                        p.write("  </table>");
                        p.write("</div>");
                        p.write("<table class=\"lookupTable\">");
                        p.write("  <tr>");
                        p.write("    <td class=\"lookupInput\">");
                        if(pageNextIsEnabled || pagePreviousIsEnabled) {
                            p.write("      ", p.getImg("src=\"", p.getResourcePath("images/"), "spacer", p.getImgType(), " width=\"28\" border=\"0\" align=\"bottom\" alt=\"\""));
                        }
                        Action findObjectAction = view.getFindObjectAction(
                            addObjectAction.getParameter("reference"),
                            lookupId
                        );
                        CharSequence imgTag = p.getImg("class=\"popUpButton\" border=\"0\" align=\"bottom\" alt=\"Click to open ObjectFinder\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_LOOKUP_AUTOCOMPLETE_GRID, "\"", p.getOnClick("OF.findObject(", p.getEvalHRef(findObjectAction), ", $('", adderFieldId, ".Title'), $('", adderFieldId, "'), '", lookupId, "');"));
                        if(autocompleter == null) {
                            p.write("      <input id=\"", adderFieldId, ".Title\" type=\"text\" name=\"", adderFieldId, ".Title\" value=\"\" />");
                            p.write("      <input type=\"hidden\" id=\"", adderFieldId, "\" name=\"", adderFieldId, "\" value=\"\" />");
                            p.write("      ", imgTag);
                        }
                        else {
                            autocompleter.paint(                    
                                p,
                                null,
                                tabIndex,
                                "addObject",
                                null,
                                false,
                                "<td class=\"lookupButtons\">",
                                null,
                                "class=\"valueAC\"",
                                imgTag
                            );
                        }
                        p.write("    </td>");
                        p.write("  </tr>");
                        p.write("</table>");
                    }
                    else {
                        p.write("<div class=\"gridSpacerTop\"></div>");                        
                    }
                }
                // Composite objects
                else {
                    p.write("<div id=\"menuOpPanel" , "\" class=\"menuOpPanel\">");
                    p.write("  <table cellspacing=\"0\" cellpadding=\"0\" id=\"menuOp\" width=\"100%\">");
                    p.write("    <tr>");
                    //
                    // Navigation
                    //
                    p.write("      <td class=\"menuOpPanelActions\">");                    
                    // Page 0
                    p.write("        <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(firstPageAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), firstPageAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\"|<\""), "</a>");
                    // Page previous
                    if(pagePreviousIsEnabled) {
                        p.write("        <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(pagePreviousFastAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousFastAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\"<<\""), "</a>");
                        p.write("        <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(pagePreviousAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\"<\""), "</a>");
                    }
                    else {
                        p.write("        ", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousFastAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\"<<\""));
                        p.write("        ", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\"<\""));
                    }
                    // Page next
                    if(pageNextIsEnabled) {
                        p.write("        <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(pageNextAction), " + '&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), pageNextAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\">\""), "</a>");
                        p.write("        <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(pageNextFastAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), pageNextFastAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\">>\""), "</a>");
                    }
                    else {
                        p.write("        ", p.getImg("src=\"", p.getResourcePath("images/"), pageNextAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\">\""));
                        p.write("        ", p.getImg("src=\"", p.getResourcePath("images/"), pageNextFastAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\">>\""));
                    }
                    // Hidden page size input field
                    p.write("        <input style=\"margin:0px;\" type=\"hidden\" name=\"pagesize\" id=\"pagesize", tabId, "\" size=2 value=\"", Integer.toString(grid.getPageSize()), "\"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                    // Show default filter button in case content is hidden
                    if((filters.length > 1) && !grid.getShowRows()) {
                        org.openmdx.portal.servlet.Filter filter = filters[0];
                        Action action = grid.getSelectFilterAction(filter);
                        p.write("        <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(action), updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_SHOW_GRID_CONTENT, "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"", htmlEncoder.encode(action.getTitle(), false), "\""), "</a>");
                        p.write("        ", p.getImg("border=\"0\" width=\"4\" alt=\"\" src=\"", p.getResourcePath("images/"), "spacer.gif\""));
                    }
                    // 
                    // Show filter table
                    //
                    p.write("        <a href=\"#\"", p.getOnClick("javascript:ft=$('filterArea", tabId, "');if(ft.style.display!='block'){ft.style.display='block';}else{ft.style.display='none';};try{$('filtervalue", tabId, "').focus();}catch(e){};return false;"), ">", p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_SEARCH_PANEL, "\" border=\"0\" align=\"bottom\" alt=\"v\""), "</a>");
                    p.write("      </td>");
                    //
                    // Menu
                    //
                    p.write("      <td class=\"menuOpPanelMenus\">");
                    p.write("        <div id=\"showGridButtons", tabId, "\">");
                    p.write("          <ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");
                    Action multiDeleteAction = grid.getMultiDeleteAction();
                    if(
                        (grid.getObjectCreator() != null) &&
                        (grid.getObjectCreator().length > 0)
                    ) {
                        //
                        // Menu New
                        //
                        p.write("              <li><a href=\"#\" onclick=\"javascript:return false;\">", htmlEncoder.encode(texts.getNewText(), false), "&nbsp;&nbsp;&nbsp;</a>");
                        p.write("                <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                        for(int j = 0; j < grid.getObjectCreator().length; j++) {
                            Action action = grid.getObjectCreator()[j];
                            p.write("                  <li><a href=\"#\"", p.getOnClick("javascript:this.href=", p.getEvalHRef(action), ";"), ">", p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""), "&nbsp;&nbsp;", htmlEncoder.encode(action.getTitle(), false), "</a></li>");
                        }                
                        p.write("                </ul>");
                        p.write("              </li>");
                        //
                        // Menu Edit
                        //
                        p.write("              <li><a href=\"#\" onclick=\"javascript:return false;\">", htmlEncoder.encode(texts.getEditTitle(), false), "&nbsp;&nbsp;&nbsp;</a>");
                        p.write("                <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                        // In-place edit
                        if(gridControl.inPlaceEditable() && (view.getLookupType() == null)) {
                            p.write("                  <li><a href=\"#\"", p.getOnClick("javascript:$('editGrid", tabId, "').style.display='block';$('showGrid", tabId, "').style.display='none';$('showGridButtons", tabId, "').style.display='none';"), ">", htmlEncoder.encode(texts.getEditTitle(), false), "</a></li>");
                        }
                        else {
                            p.write("                  <li><a href=\"#\"", p.getOnClick("javascript:;"), "><span>", htmlEncoder.encode(texts.getEditTitle(), false), "</span></a></li>");
                        }               
                        // Multi-delete
                        if(referencePane.getReferencePaneControl().getIsMultiDeleteEnabled() && (multiDeleteAction != null)) {
                            p.write("                  <li><a href=\"#\"", p.getOnClick("javascript:var para=getSelectedGridRows('gridTable", tabId, "',1);if(para.length>1) {$('multideleteIDlist", tabId, "').value=para;};document.showForm", tabId, ".submit();"), ">", htmlEncoder.encode(multiDeleteAction.getTitle(), false), "</a></li>");
                        }
                        else {
                            p.write("                  <li><a href=\"#\"", p.getOnClick("javascript:;"), "><span>", htmlEncoder.encode(texts.getDeleteTitle(), false), "</span></a></li>");                    
                        }
                        p.write("                </ul>");
                        p.write("              </li>");
                    }
                    //
                    // Menu View
                    //                        
                    p.write("              <li><a href=\"#\" onclick=\"javascript:return false;\">", htmlEncoder.encode(texts.getViewTitle(), false), "&nbsp;&nbsp;&nbsp;</a>");
                    p.write("                <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                    for(int i = 0; i < DEFAULT_PAGE_SIZES.length; i++) {
                        String pageSize = Integer.toString(DEFAULT_PAGE_SIZES[i]);
                        String showPagesText = texts.getShowRowsText();;
                        showPagesText = showPagesText.replaceAll("\\$\\{0\\}", pageSize);
                        p.write("                  <li><a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(selectGridTabAction), "+'&amp;pagesize=", pageSize, "'", updateTabScriptletPost, "\""), ">", htmlEncoder.encode(showPagesText, false), "</a></li>");
                    }
                    p.write("                </ul>");
                    p.write("              </li>");
                    p.write("          </ul>");
                    p.write("        </div>");
                    //
                    // Form Multi-Delete
                    //
                    p.write("        <form id=\"showForm", tabId, "\" name=\"showForm", tabId, "\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"post\" action=\"\" style=\"padding:0px;\">");
                    if(multiDeleteAction != null) {
                        p.write("          <input type=\"hidden\" name=\"requestId.submit\" value=\"", view.getRequestId(), "\" />");
                        p.write("          <input type=\"hidden\" name=\"event.submit\" value=\"", Integer.toString(multiDeleteAction.getEvent()), "\" />");
                        p.write("          <input type=\"hidden\" name=\"parameter.list\" value=\"\" id=\"multideleteIDlist", tabId, "\" />");
                    }
                    p.write("        </form>");                
                    p.write("      </td>");
                    // 
                    // Info
                    //
                    p.write("      <td class=\"menuOpPanelInfo\">");
                    p.write("        (", Integer.toString(grid.getCurrentPage()*grid.getPageSize()+1), "-", Integer.toString(grid.getCurrentPage()*grid.getPageSize() + rows.length), ")");
                    p.write("      </td>");
                    // 
                    // End of panel
                    //
                    p.write("    </tr>");
                    p.write("  </table>");
                    p.write("</div>");
                }
                p.write("<div id=\"showGrid", tabId, "\" style=\"display: block;\">");
                //
                // Filter
                // Skip filter in case there is only the 'All' filter
                // only display filter table if any filter exists
                //
                AppLog.detail("grid: filters");
                boolean showFilterArea = 
                    ((grid.getCurrentFilterValues() != null) &&  (grid.getCurrentFilterValues().trim().length() > 0));
                p.write("<div id=\"filterArea", tabId, "\" style=\"display:", (showFilterArea ? "block" : "none"), ";\">");
                p.write("  <div id=\"customFilterArea", tabId, "\"></div>");                
                if(filters.length > 1) {
                  p.write("  <div id=\"defaultFilterArea", tabId, "\">");                
                  p.write("    <table id=\"filterTable", tabId, "\", class=\"filterTable\">");
                  p.write("      <tr>");
                  // only show filter values input field if there is at least one sortable column
                  int nSortableColumns = 0;
                  for(int j = 0; j < gridControl.getColumnFilterSetActions().length; j++) {
                    Action action = gridControl.getColumnFilterSetActions()[j];
                    if((action.getEvent() != Action.EVENT_NONE) && !action.getParameter(Action.PARAMETER_NAME).equals("identity")) {
                      nSortableColumns++;
                    }
                  } 
                  if(nSortableColumns > 0) {
                      p.write("        <td class=\"filterInput\">");
                      p.write("          ", p.getImg("class=\"popUpButton\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_FILTER_HELP, "\" border=\"0\" alt=\"?\" align=\"bottom\"", p.getOnClick("javascript:void(window.open('helpSearch_", app.getCurrentLocaleAsString(), ".html', 'Help', 'fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=yes,resizable=yes,directories=no,location=no,width=400'));")));
                      p.write("          <input style=\"margin:0px;\" type=\"text\" name=\"", WebKeys.REQUEST_PARAMETER_FILTER_VALUES, "\" id=\"filtervalue", tabId, "\" value=\"", app.getHtmlEncoder().encode(grid.getCurrentFilterValues(), true), "\" />");
                      p.write("          <input style=\"margin:0px;\" id=\"filteradd", tabId, "\" type=\"checkbox\" title=\"", htmlEncoder.encode(texts.getAddFilterTitle(), false), "\">&nbsp;&nbsp;&nbsp;");
                      p.write("        </td>");
                  }
                  
                  p.write("        <td class=\"filterButtons\">");
                  p.write("          <ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");              
                  String filterGroupName = null;
                  for(int j = 0; j < filters.length; j++) {
                      org.openmdx.portal.servlet.Filter filter = filters[j];
                      // Next filter group
                      if(
                          (filterGroupName != null) &&
                          !filterGroupName.equals(filter.getGroupName())
                      ) {
                          // Filter group 0 does not have sub-levels
                          if(!"0".equals(filterGroupName)) {
                              p.write("          </ul>");
                              p.write("        </li>");
                          }
                          filterGroupName = filter.getGroupName();
                          // Class filter group
                          if("1".equals(filterGroupName)) {
                              p.write("      <li><a href=\"#\" onclick=\"javascript:return false;\">", p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_FILTER_CLASS, "\" border=\"0\" align=\"absmiddle\" alt=\"o\" title=\"", htmlEncoder.encode(texts.getClassFilterTitle(), false), "\""), "</a>");
                          } 
                          // All other filter groups
                          else {
                              p.write("      <li><a href=\"#\" onclick=\"javascript:return false;\">", p.getImg("src=\"", p.getResourcePath("images/"), "filter_", htmlEncoder.encode(filterGroupName, false), p.getImgType(), "\" border=\"0\" align=\"absmiddle\" style=\"margin-right: 2px;\" alt=\"o\" title=\"", htmlEncoder.encode(filterGroupName, false), "\""), "</a>");
                          }
                          p.write("        <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                      } 
                      filterGroupName = filter.getGroupName();
                      if(!filter.hasParameter()) {
                          Action action = grid.getSelectFilterAction(filter);
                          if(filterGroupName.equals("0")) {
                              p.write("        <li><a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(action), updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"absmiddle\" alt=\"o\" title=\"", htmlEncoder.encode(action.getTitle(), false), "\""), "</a></li>");
                          } 
                          else {
                              p.write("        <li><a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(action), updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"absmiddle\" alt=\"o\" title=\"\""), " ", htmlEncoder.encode(texts.getSelectAllText(), false), " ", htmlEncoder.encode(action.getTitle(), false), "</a></li>");
                          }
                      }
                  }
                  // Close current filter group
                  if(!"0".equals(filterGroupName)) {
                      p.write("          </ul>");
                      p.write("        </li>");
                  }
                  // Additional search columns 
                  if(gridControl.getColumnTypes().length > gridControl.getShowMaxMember()) {
                      p.write("        <li><a href=\"#\" onclick=\"javascript:return false;\">", p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_FILTER_EXT, "\" border=\"0\" align=\"absmiddle\" alt=\"o\" title=\"\""), "</a>");
                      p.write("          <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                      for(int j = gridControl.getShowMaxMember(); j < gridControl.getColumnTypes().length; j++) {
                          Action columnFilterSetAction = gridControl.getColumnFilterSetActions()[j];
                          Action columnFilterAddAction = gridControl.getColumnFilterAddActions()[j];
                          // Only show if filterable
                          if(columnFilterSetAction.getEvent() != Action.EVENT_NONE) {
                              p.write("            <li><a href=\"#\"", p.getOnClick("javascript:if($('filteradd", tabId, "').checked) {", updateTabScriptletPre, p.getEvalHRef(columnFilterAddAction), "+'&amp;", WebKeys.REQUEST_PARAMETER_FILTER_VALUES, "='+encodeURIComponent($F('filtervalue", tabId, "'))+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "} else {", updateTabScriptletPre, p.getEvalHRef(columnFilterSetAction), "+'&amp;", WebKeys.REQUEST_PARAMETER_FILTER_VALUES, "='+encodeURIComponent($F('filtervalue", tabId, "'))+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "};"), ">", htmlEncoder.encode(columnFilterSetAction.getTitle(), false), "</a></li>");
                          }
                      }
                      p.write("          </ul>");
                      p.write("        </li>");
                  }
                  // Filter actions
                  Action setCurrentFilterAsDefaultAction = grid.getSetCurrentFilterAsDefaultAction();
                  Action setDefaultFilterOnInitAction = grid.getSetShowGridContentOnInitAction();
                  p.write("            <li><a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(setCurrentFilterAsDefaultAction), updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), setCurrentFilterAsDefaultAction.getIconKey(), "\" border=\"0\" align=\"absmiddle\" alt=\"o\" title=\"", htmlEncoder.encode(setCurrentFilterAsDefaultAction.getTitle(), false), "\""), "</a></li>");
                  p.write("            <li><a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(setDefaultFilterOnInitAction), updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), setDefaultFilterOnInitAction.getIconKey(), "\" border=\"0\" align=\"absmiddle\" alt=\"v\" title=\"", htmlEncoder.encode(setDefaultFilterOnInitAction.getTitle(), false), "\""), "</a></li>");
                  
                  p.write("          </ul>");
                  p.write("        </td>");
    
                  p.write("      </tr>");
                  p.write("    </table>");
                  p.write("  </div>"); // defaultFilterArea
                }
                p.write("</div>"); // filterArea
                
                Action gridAlignmentAction = grid.getAlignmentAction();                              
                String gridClassNameSuffix = grid.getAlignment() == Grid.ALIGNMENT_NARROW
                   ? "Full"
                   : "";
                AppLog.detail("grid: header");
                p.write("<table class=\"gridTable", gridClassNameSuffix, "\" id=\"gridTable", tabId, "\">");
                p.write("  <tr class=\"gridTableHeader", gridClassNameSuffix, "\">");
                if(grid.showRowSelectors()) {
                  p.write("<td class=\"gridColTypeCheck\"></td>");
                }
                for(int j = 0; j < gridControl.getShowMaxMember(); j++) {
                  int columnType = gridControl.getColumnTypes()[j];
                  Action columnFilterSetAction = gridControl.getColumnFilterSetActions()[j];
                  Action columnFilterAddAction = gridControl.getColumnFilterAddActions()[j];
                  
                  if(j == 0) {
                    p.write("<td class=\"gridColTypeIcon-3\">");
                    p.write("  <table class=\"filterHeader\"><tr><td>");
                    p.write("    <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(gridAlignmentAction), updateTabScriptletPost), ">", p.getImg("class=\"borderedimg\" src=\"", p.getResourcePath("images/"), gridAlignmentAction.getIconKey(), "\" title=\"", htmlEncoder.encode(gridAlignmentAction.getTitle(), false), "\" align=\"bottom\"", p.getOnMouseOver("javascript:this.className='borderedimghover';"), p.getOnMouseOut("javascript:this.className='borderedimg';"), " alt=\"\""), "</a><input id=\"multiselect", tabId, "\" type=\"checkbox\"", p.getOnClick("javascript:selectGridRows('gridTable", tabId, "',1, this.checked);"), " />");
                    p.write("  </td></tr></table>");
                    p.write("</td>");
                  }
                  else {
                    String classModifier = null;
                    if(columnType == Grid.COLUMN_TYPE_DATE) {
                      classModifier = "class=\"gridColTypeDate\"";
                    }
                    else if(grid.getAlignment() == Grid.ALIGNMENT_NARROW) {
                      classModifier = "class=\"gridColTypeNormal\"";
                    }
                    else {
                      classModifier = (j < 3) && (j < gridControl.getColumnFilterSetActions().length-1) 
                        ? "class=\"gridColTypeNormal\"" 
                        : "class=\"gridColTypeWide\"";
                    }
                    p.write("<td ",  classModifier, ">");
                    p.write("  <table class=\"filterHeader\">");
                    p.write("    <tr>");
                                    
                    // Filtering
                    CharSequence columnTitle = htmlEncoder.encode(columnFilterSetAction.getTitle(), false).trim();
                    if(columnTitle.length() == 0) {
                        columnTitle = p.getImg("src=\"", p.getResourcePath("images/") + columnFilterSetAction.getIconKey() + "\" border=\"0\" align=\"middle\" alt=\"o\" title=\"\"");
                    }
                    if(columnFilterSetAction.getEvent() == Action.EVENT_NONE) {
                        p.write("<td><div class=\"textfilter\">", columnTitle, "</div></td>");
                    }
                    else {
                        p.write("<td class=\"filterCell\" title=\"", htmlEncoder.encode(columnFilterSetAction.getToolTip(), false), "\"", p.getOnClick("javascript:if($('filteradd", tabId, "').checked) {", updateTabScriptletPre, p.getEvalHRef(columnFilterAddAction), "+'&amp;", WebKeys.REQUEST_PARAMETER_FILTER_VALUES, "='+encodeURIComponent($F('filtervalue", tabId, "'))+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "} else {", updateTabScriptletPre, p.getEvalHRef(columnFilterSetAction), "+'&amp;", WebKeys.REQUEST_PARAMETER_FILTER_VALUES, "='+encodeURIComponent($F('filtervalue", tabId, "'))+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "};"), p.getOnMouseOver("javascript:this.className='filterCellhover';"), p.getOnMouseOut("javascript:this.className='filterCell';"), " ><div class=\"textfilter\">", columnTitle, "</div></td>");
                    }
                    // column ordering 
                    Action columnOrderSetAction = grid.getColumnOrderSetAction(
                        columnFilterSetAction.getParameter(Action.PARAMETER_NAME).toString()
                    );
                    Action columnOrderAddAction = grid.getColumnOrderAddAction(
                        columnFilterSetAction.getParameter(Action.PARAMETER_NAME).toString()
                    );

                    // ordering
                    if(columnOrderSetAction.getEvent() != Action.EVENT_NONE) {
                        p.write("<td>", p.getImg("class=\"borderedimg\" src=\"", p.getResourcePath("images/"), columnOrderSetAction.getIconKey(), "\" title=\"", htmlEncoder.encode(columnOrderSetAction.getTitle(), false), "\" align=\"bottom\"", p.getOnClick("javascript:if($('filteradd", tabId, "') && ($('filteradd", tabId, "').checked)) {", updateTabScriptletPre, p.getEvalHRef(columnOrderAddAction), updateTabScriptletPost, "} else {", updateTabScriptletPre, p.getEvalHRef(columnOrderSetAction), updateTabScriptletPost, "};"), p.getOnMouseOver("javascript:this.className='borderedimghover';"), p.getOnMouseOut("javascript:this.className='borderedimg';"), " alt=\"\""), "</td>");
                    }                      
                    p.write("    </tr>");
                    p.write("  </table>");
                    p.write("</td>");
                  }
                }
                if(grid.showRowSelectors()) {
                  p.write("<td class=\"gridColTypeCheck\"></td>");
                }
                p.write("</tr>");
                // Show grid
                for(int j = 0; j < rows.length; j++) {
                    AppLog.detail("grid row", Integer.toString(j));
                    long rowId = ReferencePaneControl.currentRowId++;
                    
                    Object[] row = (Object[])rows[j];
                    Object[] nextRow = (j+1) < rows.length
                      ? (Object[])rows[j+1]
                      : null;
    
                    // row color
                    String rowColor = null;
                    String rowBackColor = null;
                    if(app.getPortalExtension().hasGridColours(gridControl.getObjectContainer().getReferencedTypeName())) {
                        for(int k = 0; k < gridControl.getShowMaxMember(); k++) { // to color based on ALL cells, use row.length instead of grid.getShowMaxMember()
                            if((rowBackColor = ((AttributeValue)row[k]).getBackColor()) != null) {
                                rowColor = ((AttributeValue)row[k]).getColor();
                                break;
                            }
                        }
                    }
                    
                    // style modifier
                    String styleModifier = "";
                    styleModifier += (rowColor == null) || "inherit".equals(rowColor)
                      ? ""
                      : "color:" + rowColor + ";";
                    styleModifier += (rowBackColor == null) || "inherit".equals(rowBackColor)
                      ? ""
                      : "background-color:" + rowBackColor + ";";
                    styleModifier = styleModifier.length() == 0
                        ? styleModifier
                        : "style=\"" + styleModifier + "\"";
                    
                    ObjectReference objRow = (ObjectReference)((AttributeValue)row[0]).getValue(true);
                    String rowLevelId = objRow.getGridRowLevelId();
                    rowLevelId = rowLevelId == null
                        ? null
                        : "gridTable" + tabId + "-" + rowLevelId;
                    String rowIdModifier = rowLevelId == null
                        ? ""
                        : "id=\"" + rowLevelId + "\"";
                    p.write("<tr class=\"gridTableRow", gridClassNameSuffix, (j % 2 == 0 ? "" : " odd"),  "\" ", styleModifier, " ", rowIdModifier, " >");
                    if((objRow != null) && grid.showRowSelectors()) {
                        if((view.getLookupType() != null) && objRow.isInstanceof(view.getLookupType())) {
                          p.write("<td class=\"gridColTypeCheck\"><input type=\"checkbox\" name=\"objselect\" value=\"\"", p.getOnClick("OF.selectAndClose('", htmlEncoder.encode(objRow.refMofId(), false), "', '", htmlEncoder.encode(objRow.getTitleEscapeQuote(), false), "', '", view.getId(), "', window);"), " /></td>");
                        }
                        else {
                          p.write("<td class=\"gridColTypeCheck\"></td>");
                        }
                    }                    
                    for(int k = 0; k < gridControl.getShowMaxMember(); k++) {
                        AttributeValue valueHolder = (AttributeValue)row[k];
                        Object value = valueHolder.getValue(true);
                        String stringifiedValue = valueHolder.getStringifiedValue(
                            p, 
                            true, 
                            false, 
                            true
                        );
                        stringifiedValue = valueHolder instanceof TextValue
                          ? ((TextValue)valueHolder).isPassword() ? "*****" : stringifiedValue
                          : stringifiedValue;
                        // null or empty collection
                        if(stringifiedValue.length() == 0) {
                          stringifiedValue = "&nbsp;";
                        }
                        
                        // iconTag
                        CharSequence iconTag = valueHolder.getIconKey() == null
                            ? ""
                            : "" + p.getImg("src=\"", p.getResourcePath("images/"), valueHolder.getIconKey(), "\" align=\"middle\" border=\"0\" alt=\"\"") + p.getImg("src=\"", p.getResourcePath("images/"), "spacer", p.getImgType(), "\" width=\"5\" height=\"0\" align=\"middle\" border=\"0\" alt=\"\"");
    
                        if(value == null) {
                          p.debug("<!-- null -->");
                          p.write("<td>",  iconTag, "",  stringifiedValue, "</td>");
                        }
                        else if(valueHolder instanceof BooleanValue) {
                            String images = "";
                            if(value instanceof Collection) {
                              for(Iterator e = ((Collection)value).iterator(); e.hasNext(); ) {
                                if(Boolean.TRUE.equals(e.next())) {
                                  images += p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_CHECKED_R, "\" alt=\"checked\"");
                                }
                                else {
                                  images += p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_NOT_CHECKED_R, "\" alt=\"not checked\"");
                                }
                              }
                            }
                            else {
                              if(Boolean.TRUE.equals(value)) {
                                images += p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_CHECKED_R, "\" alt=\"checked\"");
                              }
                              else {
                                images += p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_NOT_CHECKED_R, "\" alt=\"not checked\"");
                              }
                            }
                            p.debug("<!-- BooleanValue -->");
                            p.write("<td>",  iconTag, "",  images, "</td>");
                        }
                        else if(valueHolder instanceof ObjectReferenceValue) {
                            // Multi-valued
                            if(value instanceof Collection) {
                                p.write("<!-- ObjectReferenceValue -->"); // used as tag for multi-delete. Do not write as debug!
                                p.write("<td>");
                                boolean isFirst = true;
                                for(Iterator e = ((Collection)value).iterator(); e.hasNext(); ) {
                                    ObjectReference objRef = (ObjectReference)e.next();
                                    Action action = objRef.getSelectObjectAction();
                                    if(!isFirst) {
                                        p.write("<br />");                                    
                                    }
                                    // Detailed message as tool tip. Otherwise grid layout will be destroyed
                                    String title = action.getTitle();
                                    String toolTip = "";
                                    if(title.startsWith(ObjectReference.TITLE_PREFIX_NO_PERMISSION)) {
                                        toolTip = title;                                     
                                        title = title.substring(0, ObjectReference.TITLE_PREFIX_NO_PERMISSION.length());
                                    }
                                    else if(title.startsWith(ObjectReference.TITLE_PREFIX_NOT_ACCESSIBLE)) {
                                        toolTip = title;                                     
                                        title = title.substring(0, ObjectReference.TITLE_PREFIX_NOT_ACCESSIBLE.length());
                                    }
                                    p.write("<a href=\"\"", p.getOnClick("javascript:this.href=", p.getEvalHRef(action), ";"), " title=\"", htmlEncoder.encode(toolTip, false), "\">", htmlEncoder.encode(title, false), "</a>");
                                    isFirst = false;                                
                                }                            
                                p.write("</td>");
                            }
                            // Single-valued
                            else {
                                ObjectReference objRef = (ObjectReference)value;
                                Action action = objRef.getSelectObjectAction();                        
                                // Image only in first column
                                if(k == 0) {                          
                                    // Expand rows modifier
                                    ObjectReference nextObjRef = nextRow == null
                                       ? null
                                       : (ObjectReference)((AttributeValue)nextRow[0]).getValue(true);
                                    CharSequence expandRowsModifier = (rowLevelId != null) && (nextRow != null) && (nextObjRef.getGridRowNestingLevel() > objRef.getGridRowNestingLevel())
                                        ? "" + p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_COLLAPSE, "\" border=\"0\" align=\"bottom\" alt=\"*\" title=\"\"", p.getOnClick("javascript:updateCollapsableRows('gridTable", tabId, "', 1, '", rowLevelId, "', ", Integer.toString(objRef.getGridRowNestingLevel()), ", (this.src.indexOf('collapse')>0)); if(this.src.indexOf('collapse')>0) {this.src='", p.getResourcePath("images/"), WebKeys.ICON_EXPAND, "';} else {this.src='", p.getResourcePath("images/"), WebKeys.ICON_COLLAPSE, "';};")) + p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_EXPAND, "\" style=\"display:none;\" border=\"0\" align=\"bottom\" alt=\"*\" title=\"\"")
                                        : "";            
                                    p.write("<!-- ObjectReferenceValue -->"); // used as tag for multi-delete. Do not write as debug!
                                    p.write("<td class=\"gridColTypeIcon-3\"><div style=\"float:left;\">", p.getImg("src=\"", p.getResourcePath("images/"), "spacer", p.getImgType(), "\" alt=\"\" width=\"", Integer.toString(16*objRef.getGridRowNestingLevel()), "\" height=\"0\""), "<a href=\"#\"", p.getOnMouseOver("javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};"), ">", p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" style=\"cursor:pointer;\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""), "</a></div><span id=\"", "gridRow", Long.toString(rowId), "-menu\" class=\"gridMenu\" onclick=\"try{this.parentNode.parentNode.onclick();}catch(e){};\"><a href=\"#\" class=\"gridMenuIndicator\" ", p.getOnClick("javascript:new Ajax.Updater('gridRow", Long.toString(rowId), "-menu', ", p.getEvalHRef(this.getRowMenuAction(objRef.refMofId(), rowId)), ", {asynchronous:true, evalScripts: true, onComplete: function(){}});return false;"), ">", p.getImg("border=\"0\" height=\"16\" width=\"15\" alt=\"\" src=\"", p.getResourcePath("images/"), "spacer.gif\""), "</a>", p.getImg("border=\"0\" align=\"bottom\" alt=\"\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_MENU_DOWN, "\" style=\"display:none;\""), "</span>", expandRowsModifier, "</td>");
                                }
                                // only text in columns > 0
                                else {
                                    p.write("<!-- ObjectReferenceValue -->");
                                    String title = action.getTitle();
                                    String toolTip = "";
                                    if(title.startsWith(ObjectReference.TITLE_PREFIX_NO_PERMISSION)) {
                                        toolTip = title;                                     
                                        title = title.substring(0, ObjectReference.TITLE_PREFIX_NO_PERMISSION.length());
                                    }
                                    else if(title.startsWith(ObjectReference.TITLE_PREFIX_NOT_ACCESSIBLE)) {
                                        toolTip = title;                                     
                                        title = title.substring(0, ObjectReference.TITLE_PREFIX_NOT_ACCESSIBLE.length());
                                    }
                                    p.write("<td><a href=\"\"", p.getOnClick("javascript:this.href=", p.getEvalHRef(action), ";"), " title=\"", htmlEncoder.encode(toolTip, false), "\">");
                                    app.getPortalExtension().renderTextValue(p, htmlEncoder.encode(title, false));
                                    p.write("</a></td>");
                                }
                            }
                        }
                        else {
                          p.debug("<!-- AttributeValue -->");
                          p.write("<td>",  iconTag, "",  stringifiedValue, "</td>");
                        }
                    }
                    if((objRow != null) && grid.showRowSelectors()) {
                        if((view.getLookupType() != null) && objRow.isInstanceof(view.getLookupType())) {
                          p.write("<td class=\"gridColTypeCheck\"><input type=\"checkbox\" name=\"objselect\" value=\"\"", p.getOnClick("OF.selectAndClose('", htmlEncoder.encode(objRow.refMofId(), false), "', '", htmlEncoder.encode(objRow.getTitleEscapeQuote(), false), "', '", view.getId(), "', window);"), " /></td>");
                        }
                        else {
                          p.write("<td class=\"gridColTypeCheck\"></td>");
                        }
                    }
                    p.write("</tr>");
                    // Row for object details (handled by EVENT_GRID_SHOW_ROW_DETAILS)
                    p.write("<tr class=\"rowDetails\">");
                    p.write("  <td colspan=\"", Integer.toString(gridControl.getShowMaxMember()), "\"><table class=\"tablePanel\">");
                    p.write("    <td class=\"gridCloser\"><img class=\"imgCloser\" id=\"gridRow", Long.toString(rowId), "-details-closer\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_CLOSE, "\" alt=\"\" style=\"display:none;\" onclick=\"javascript:$('gridRow", Long.toString(rowId), "-details').innerHTML='';this.style.display='none';\" /></td>");
                    p.write("    <td width=\"100%\"><div id=\"", "gridRow", Long.toString(rowId), "-details\" /></td>");
                    p.write("  </table></td>");
                    p.write("</tr>");
                }
                p.write("</table>");
                // Page indicators only for composite grids and if view is not embedded to save space
                if(view.getContainerElementId() == null) {
                    // Only show pagers if there are more than 10 rows 
                    if(rows.length > 10) {                    
                        if(grid.getAddObjectAction() == null) { 
                            // Page 0
                            p.write("        <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(firstPageAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), firstPageAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a>");
                            // Page previous
                            if(pagePreviousIsEnabled) {
                                p.write("        <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(pagePreviousFastAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousFastAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a>");
                                p.write("        <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(pagePreviousAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a>");
                            }
                            else {
                                p.write("        ", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousFastAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""));
                                p.write("        ", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""));
                            }
                            // Page next
                            if(pageNextIsEnabled) {
                                p.write("        <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(pageNextAction), " + '&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), pageNextAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"v\""), "</a>");
                                p.write("        <a href=\"#\"", p.getOnClick("javascript:", updateTabScriptletPre, p.getEvalHRef(pageNextFastAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost), ">", p.getImg("src=\"", p.getResourcePath("images/"), pageNextFastAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a>");
                            }
                            else {
                                p.write("        ", p.getImg("src=\"", p.getResourcePath("images/"), pageNextAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""));
                                p.write("        ", p.getImg("src=\"", p.getResourcePath("images/"), pageNextFastAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""));
                            }
                        }
                    }
                }
                else {
                    p.write("<div class=\"gridSpacerBottom\"></div>");                        
                }                                        
                p.write("</div>");
                // Template rows. Only generate if grid is not embedded, not in lookup mode and is editable
                if(
                    (view.getContainerElementId() == null) && 
                    gridControl.inPlaceEditable() && 
                    (view.getLookupType() == null)
                ) {
                   AppLog.detail("edit template grid");
                   p.write("<div id=\"editGrid", tabId, "\" style=\"display: none;\">");
                   p.write("  <form id=\"editForm", tabId, "\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"post\" action=\"\" style=\"padding:0px;\" onSubmit=\"javascript:$('tr-editForm", tabId, "').disabled='true'; $('bl-editForm", tabId, "').disabled='true'; $('br-editForm", tabId, "').disabled='true';\" >");
                   p.write("    <table style=\"border-collapse: collapse;\">");
                   p.write("      <tr>");
                   p.write("        <td width=\"100%\">");
                   p.write("        </td>");
                   p.write("        <td nowrap>");
                   p.write("          <div style=\"float: right; padding-top:1px;\">");               
                   Action saveAction = grid.getSaveAction();
                   p.write("            <input type=\"hidden\" name=\"requestId.submit\" value=\"", view.getRequestId(), "\" />");
                   p.write("            <input type=\"hidden\" name=\"event.submit\" value=\"", Integer.toString(saveAction.getEvent()), "\" />");
                   p.write("            <input class=\"flatsubmit\" type=\"submit\" title=\"", htmlEncoder.encode(saveAction.getTitle(), false), "\" value=\"", htmlEncoder.encode(saveAction.getTitle(), false), "\" id=\"tr-editForm", tabId, "\"", p.getOnMouseOver("javascript:this.className='flatsubmithover';"), p.getOnMouseOut("javascript:this.className='flatsubmit';"), " />");
                   p.write("            <input class=\"flatsubmit\" type=\"submit\" title=\"", htmlEncoder.encode(texts.getCancelTitle(), false), "\" value=\"", texts.getCancelTitle(), "\"", p.getOnClick("javascript:$('editGrid", tabId, "').style.display='none'; $('showGrid", tabId, "').style.display='block';$('showGridButtons", tabId, "').style.display='block'; if($('menuCrPanel", tabId, "')) {$('menuCrPanel", tabId, "').style.display='block';}; return false;"), p.getOnMouseOver("javascript:this.className='flatsubmithover';"), p.getOnMouseOut("javascript:this.className='flatsubmit';"), " />");
                   p.write("          </div>");
                   p.write("        </td>");                              
                   p.write("      </tr>");
                   p.write("    </table>");
                   boolean showCloneOp = false;
                   if(grid.getObjectCreator() != null) {
                     if(grid.getObjectCreator().length > 0) {
                       showCloneOp = true;
                       p.write("<table>");
                       p.write("  <tr>");
                       p.write("    <td width=\"100%\">");
                       p.write("      <script language=\"javascript\" type=\"text/javascript\">");
                       p.write("        rowIndex", tabId, " = " + rows.length);
                       p.write("      </script>");
                       p.write("      <div id=\"menuOpPanel\" class=\"menuOpPanel\" >");
                       p.write("        <table cellspacing=\"0\" cellpadding=\"0\" id=\"menuOp\" width=\"100%\">");
                       p.write("          <tr>");
                       p.write("            <td>");
                       p.write("              <ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");
                       p.write("                <li><a href=\"#\">", htmlEncoder.encode(texts.getNewText(), false), "&nbsp;&nbsp;&nbsp;</a>");
                       p.write("                  <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                       for(int j = 0; j < grid.getObjectCreator().length; j++) {
                           Action action = grid.getObjectCreator()[j];
                           String forClass = action.getParameter(Action.PARAMETER_FOR_CLASS);
                           p.write("                    <li><a href=\"#\"", p.getOnClick("javascript:rowIndex", tabId, "++;insertGridRow('editGridTable", tabId, "','Cr", tabId, "N-", forClass, "', ", Integer.toString(paneIndex), "*100000);"), ">", p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"top\" alt=\"o\" title=\"\""), "&nbsp;&nbsp;", htmlEncoder.encode(action.getTitle(), false), "</a></li>");
                       }
                       p.write("                  </ul>");
                       p.write("                </li>");
                       p.write("              </ul>");
                       p.write("            </td>");
                       p.write("          </tr>");
                       p.write("        </table>");
                       p.write("      </div>");
                       p.write("    </td>");
                       p.write("  </tr>");
                       p.write("</table>");
                     }
                  }
    
                  Object[] templateRows = grid.getTemplateRow();
                  if(templateRows != null) {
                    p.debug("<!-- table containing template rows -->");
                    p.write("<table style=\"display:none;\">");
                      for(int j = 0; j < templateRows.length; j++) {
                          AppLog.detail("edit grid template row " + j);    
                          Object[] templateRow = (Object[])templateRows[j];
                          String forClass = (String)((Map)((AttributeValue)templateRow[0]).getObject()).get(SystemAttributes.OBJECT_CLASS);
                          p.write("<tr id=\"Cr", tabId, "N-", forClass, "\" class=\"gridTableRowFullEdit\">");
                          int templateGridFieldIndex = 100000*tabIndex + 1;
                          for(int k = 0; k < gridControl.getShowMaxMember(); k++) {
                            AttributeValue valueHolder = (AttributeValue)templateRow[k];
                            boolean isEditable = !(valueHolder instanceof NullValue) && valueHolder.isChangeable() && valueHolder.isSingleValued();
                            String feature = isEditable ? valueHolder.getName() : null;
    
                            if(valueHolder instanceof BooleanValue) {
                              if(isEditable) {
                                p.debug("<!-- Boolean Template -->");
                                p.write("<td>");
                                p.write("  <input type=\"hidden\" class=\"valueL\" name=\"", feature, "[", Integer.toString(templateGridFieldIndex), "].false", "\" tabIndex=\"", Integer.toString(templateGridFieldIndex), "\" value=\"false\">");
                                p.write("  <input type=\"checkbox\" name=\"", feature, "[", Integer.toString(templateGridFieldIndex), "].true", "\" tabindex=\"", Integer.toString(templateGridFieldIndex+1), "\" value=\"\">");
                                templateGridFieldIndex++;
                                p.write("</td>");
                              }
                              else {
                                p.debug("<!-- Boolean Template -->");
                                p.write("<td class=\"locked\"></td>");
                              }
                            }
                            else if(valueHolder instanceof DateValue) {
                              if(isEditable) {
                                  p.debug("<!-- Date Template -->");
                                  p.write("<td nowrap>");
                                  p.write("  <table class=\"gridSplit\"><tr><td width=\"100%\"><input type=\"text\" class=\"valueRG\" id=\"cal_field", Integer.toString(templateGridFieldIndex), "\" name=\"", feature, "[", Integer.toString(templateGridFieldIndex), "]", "\" tabindex=\"", Integer.toString(templateGridFieldIndex), "\" value=\"\"></td>");
                                  if(((DateValue)valueHolder).isDate()) {
                                      SimpleDateFormat dateFormatter = ((DateValue)valueHolder).getLocalizedDateFormatter();
                                      p.write("<td>", p.getImg("class=\"popUpButton\" id=\"cal_trigger", Integer.toString(templateGridFieldIndex), "\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_CALENDAR, "\""));
                                      p.write("<script language=\"javascript\" type=\"text/javascript\">");
                                      p.write("   Calendar.setup({");
                                      p.write("     inputField   : \"cal_field", Integer.toString(templateGridFieldIndex), "\",");
                                      p.write("     ifFormat     : \"", DateValue.getCalendarFormat(dateFormatter), "\",");
                                      p.write("     firstDay     : ", Integer.toString(dateFormatter.getCalendar().getFirstDayOfWeek()-1), ",");
                                      p.write("     timeFormat   : \"24\",");
                                      p.write("     button       : \"cal_trigger", Integer.toString(templateGridFieldIndex), "\",");
                                      p.write("     align        : \"Tr\",");
                                      p.write("     singleClick  : true,");
                                      p.write("     showsTime    : false");
                                      p.write("   });");
                                      p.write("</script></td></tr></table>");
                                  }
                                  else {
                                      SimpleDateFormat dateTimeFormatter = ((DateValue)valueHolder).getLocalizedDateTimeFormatter();
                                      p.write("<td>", p.getImg("class=\"popUpButton\" id=\"cal_trigger", Integer.toString(templateGridFieldIndex), "\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_CALENDAR, "\""));
                                      p.write("<script language=\"javascript\" type=\"text/javascript\">");
                                      p.write("  Calendar.setup({");
                                      p.write("    inputField   : \"cal_field", Integer.toString(templateGridFieldIndex), "\",");
                                      p.write("    ifFormat     : \"", DateValue.getCalendarFormat(dateTimeFormatter), "\",");
                                      p.write("    firstDay     : ", Integer.toString(dateTimeFormatter.getCalendar().getFirstDayOfWeek()-1), ",");
                                      p.write("    timeFormat   : \"24\",");
                                      p.write("    button       : \"cal_trigger", Integer.toString(templateGridFieldIndex), "\",");
                                      p.write("    align        : \"Tr\",");
                                      p.write("    singleClick  : true,");
                                      p.write("    showsTime    : true");
                                      p.write("  });");
                                      p.write("</script></td></tr></table>");
                                  }
                                  p.write("</td>");
                              }
                              else {
                                p.debug("<!-- Date Template -->");
                                p.write("<td class=\"locked\"></td>");
                              }
                            }
                            else if(valueHolder instanceof CodeValue) {
                              if(isEditable) { // and also isSingleValued()
                                SortedMap longTextsT = ((CodeValue)valueHolder).getLongText(false, false);
    
                                String longTextsAsJsArray = "";
                                for(Iterator options = longTextsT.keySet().iterator(); options.hasNext(); ) {
                                  longTextsAsJsArray += longTextsAsJsArray.length() == 0 ? "" : ",";
                                  longTextsAsJsArray += "'" + options.next() + "'";
                                }
                                p.debug("<!-- Code Template -->");
                                p.write("<td>");
                                p.write("  <select class=\"valueLG\" name=\"", feature, "[", Integer.toString(templateGridFieldIndex), "]", "\" tabindex=\"", Integer.toString(templateGridFieldIndex), "\">");
                                for(Iterator options = longTextsT.entrySet().iterator(); options.hasNext(); ) {
                                    Map.Entry option = (Map.Entry)options.next();
                                    p.write("<option value=\"", option.getKey().toString(), "\">", option.getKey().toString());
                                }
                                p.write("  </select>");
                                p.write("</td>");
                              }
                              else {
                                p.debug("<!-- Code Template -->");
                                p.write("<td class=\"locked\"></td>");
                              }
                            }
                            else if(valueHolder instanceof NumberValue) {
                                if(isEditable) {
                                    p.debug("<!-- Number Template -->");
                                    p.write("<td><input class=\"valueRG\" type=\"text\" tabindex=\"", Integer.toString(templateGridFieldIndex), "\" name=\"", feature, "[", Integer.toString(templateGridFieldIndex), "]\", \"\" value=\"\"></td>");
                                }
                                else {
                                    p.write("<td class=\"locked\"></td>");
                                }
                            }
                            else if(valueHolder instanceof ObjectReferenceValue) {
    
                              // column=0
                              if(k == 0) {
                                p.debug("<!-- ObjectReference Template -->");
                                p.write("<td class=\"gridColTypeIconEdit-3\">");
                                p.write("  ", p.getImg("src=\"", p.getResourcePath("images/"), app.getIconKey(forClass), "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""), p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_CLONE_SMALL, "\" class=\"popUpButton\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"", texts.getCloneTitle(), "\"", p.getOnClick("javascript:rowIndex", tabId, "++;cloneGridRow('editGridTable", tabId, "', this, rowIndex", tabId, "*100, 100);"), p.getOnMouseOver("javascript:this.className='popUpButtonhover';"), p.getOnMouseOut("javascript:this.className='popUpButton';")), p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_DELETE_SMALL, "\" class=\"popUpButton\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"", htmlEncoder.encode(texts.getDeleteTitle(), false), "\"", p.getOnClick("javascript:deleteGridRow(this);"), p.getOnMouseOver("javascript:this.className='popUpButtonhover';"), p.getOnMouseOut("javascript:this.className='popUpButton';")));
                                p.write("  <input type=\"hidden\" name=\"refMofId[", Integer.toString(templateGridFieldIndex), "]\" tabindex=\"", Integer.toString(templateGridFieldIndex), "\" value=\"", forClass, "\">");
                                p.write("</td>");
                              }
                              // columns > 0
                              else {
                                if(isEditable) {
                                  String lookupId = org.openmdx.kernel.id.UUIDs.getGenerator().next().toString();
                                  Action findObjectAction = view.getFindObjectAction(
                                      feature, 
                                      lookupId
                                  );
                                  p.debug("<!-- ObjectReference Template -->");
                                  p.write("<td nowrap>");
                                  p.write("  <table class=\"gridSplit\"><tr><td width=\"100%\"><input type=\"text\" class=\"valueLG\" name=\"", feature, "[", Integer.toString(templateGridFieldIndex), "]", ".Title\" tabindex=\"", Integer.toString(templateGridFieldIndex), "\" value=\"\"></td><td>", p.getImg("class=\"popUpButton\" border=\"0\" alt=\"Click to open ObjectFinder\" src=\"", p.getResourcePath("images/"), findObjectAction.getIconKey(), "\" name=\"", feature, "[", Integer.toString(templateGridFieldIndex+1), "]\"", p.getOnClick("javascript:var IDsplit = this.id.split(/[\\[|\\]]/); var tidx = parseInt(IDsplit[1])-1; var par1 = '", feature, "' + '[' + tidx.toString() + ']'; var par2 = par1 + '.Title'; OF.findObject(", p.getEvalHRef(findObjectAction), ", document.forms['editForm", tabId, "'].elements[par2], document.forms['editForm", tabId, "'].elements[par1], '", lookupId, "');")));
                                  p.write("  <input type=\"hidden\" class=\"valueLLocked\" name=\"", feature, "[", Integer.toString(templateGridFieldIndex), "]", "\" tabindex=\"", Integer.toString(templateGridFieldIndex+2), "\" value=\"\">");
                                  p.write("  </td></tr></table>");
                                  templateGridFieldIndex = templateGridFieldIndex + 2;
                                  p.write("</td>");
                                }
                                else {
                                  p.debug("<!-- ObjectReference Template -->");
                                  p.write("<td class=\"locked\"></td>");
                                }
                              }
                            }
                            else {
                                if(isEditable) {
                                    String inputType = valueHolder instanceof TextValue
                                        ? ((TextValue)valueHolder).isPassword() ? "password" : "text"
                                        : "text";
                                    p.debug("<!-- Text Template -->");
                                    p.write("<td><input class=\"valueLG\" type=\"", inputType, "\" tabindex=\"", Integer.toString(templateGridFieldIndex), "\" name=\"", feature, "[", Integer.toString(templateGridFieldIndex), "]", "\" value=\"\"></td>");
                                }
                                else {
                                    p.debug("<!-- Text Template -->");
                                    p.write("<td class=\"locked\"></td>");
                                }
                            }
                            templateGridFieldIndex++;
                          }
                        p.write("</tr>");
                      }
                      p.write("</table>");
                  }
                  // Editable rows
                  p.debug("<!-- edit grid table -->"); 
                  p.write("<table class=\"gridTableFullEdit\" id=\"editGridTable", tabId, "\">");
                  p.write("  <tr class=\"gridTableHeaderFullEdit\">");
                  for(int j = 0; j < gridControl.getShowMaxMember(); j++) {
                    Action columnFilterSetAction = gridControl.getColumnFilterSetActions()[j];
                    if(j == 0) {
                      p.write("<td class=\"gridColTypeIconEdit-3\">");
                      p.write("  <table class=\"filterHeader\"><tr><td>", p.getImg("src=\"", p.getResourcePath("images/"), "spacer", p.getImgType(), "\" alt=\"\" width=\"46\" height=\"0\""), "</td></tr></table>");
                      p.write("</td>");
                    }
                    else {
                      p.write("<td class=\"gridColTypeNormal\">");
                      p.write("  <table class=\"filterHeader\">");
                      p.write("    <tr>");
                      p.write("      <td><div class=\"textfilter\">&nbsp;", htmlEncoder.encode(columnFilterSetAction.getTitle(), false), "</div></td>");
                      p.write("    </tr>");
                      p.write("  </table>");
                      p.write("</td>");
                    }
                  }
                  p.write("</tr>");
                  int editGridFieldIndex = 0;
                  for(int j = 0; j < rows.length; j++) {
                      AppLog.detail("edit grid row " + j);
    
                      editGridFieldIndex = tabIndex*100000 + (j+1)*100;
                      Object[] row = (Object[])rows[j];
    
                      // row color
                      String rowColor = null;
                      String rowBackColor = null;
                      for(int k = 0; k < gridControl.getShowMaxMember(); k++) { // to color based on ALL cells, use row.length instead of grid.getShowMaxMember()
                        if((rowBackColor = ((AttributeValue)row[k]).getBackColor()) != null) {
                          rowColor = ((AttributeValue)row[k]).getColor();
                          break;
                        }
                      }
                    
                      // style modifier
                      String styleModifier = "";
                      styleModifier += (rowColor == null) || "inherit".equals(rowColor)
                          ? ""
                          : "color:" + rowColor + ";";
                      styleModifier += (rowBackColor == null) || "inherit".equals(rowBackColor)
                          ? ""
                          : "background-color:" + rowBackColor + ";";
                      styleModifier = styleModifier.length() == 0
                          ? ""
                          : "style=\"" + styleModifier + "\"";
                      p.write("<tr class=\"gridTableRowFullEdit\" ", styleModifier, " >");
                      ObjectReference objRow = (ObjectReference)((AttributeValue)row[0]).getValue(false);
                      Map objFeatures = null;
                      try {
                          objFeatures = app.getModel().getAttributeDefs(
                              ((RefMetaObject_1)objRow.getObject().refMetaObject()).getElementDef(), 
                              false, 
                              true
                          );
                      } catch(Exception e) {}
                      boolean rowIsChangeable = false;
                        
                      for(int k = 0; k < gridControl.getShowMaxMember(); k++) {
    
                          AttributeValue valueHolder = (AttributeValue)row[k];
                          Object value = valueHolder.getValue(false);
                          // Only make cell editable if feature is member of object                          
                          boolean featureIsMemberOfClass = objFeatures == null
                              ? true
                              : objFeatures.keySet().contains(valueHolder.getFieldDef().featureName);
                          boolean isEditable =
                              featureIsMemberOfClass &&
                              rowIsChangeable && 
                              !(valueHolder instanceof NullValue) && 
                              valueHolder.isChangeable() && 
                              valueHolder.isSingleValued();
                          String feature = isEditable ? valueHolder.getName() : null;
                        
                          // Stringified value show
                          String stringifiedValueShow = valueHolder.getStringifiedValue(
                              p, 
                              false, 
                              false, 
                              false
                          );
                          stringifiedValueShow = valueHolder instanceof TextValue
                            ? ((TextValue)valueHolder).isPassword() ? "*****" : stringifiedValueShow
                            : stringifiedValueShow;                              
                          // null or empty collection
                          if(stringifiedValueShow.length() == 0) {
                            stringifiedValueShow = "&nbsp;";
                          }
    
                          // Stringified value edit
                          String stringifiedValueEdit = "";
                          if(isEditable) {
                            stringifiedValueEdit = valueHolder.getStringifiedValue(
                                p, 
                                false, 
                                true, 
                                false
                            );
                          }
    
                          CharSequence iconTag = valueHolder.getIconKey() == null
                              ? ""
                              : "" + p.getImg("src=\"", p.getResourcePath("images/"), valueHolder.getIconKey(), "\" align=\"middle\" border=\"0\" alt=\"\"") + p.getImg("src=\"", p.getResourcePath("images/"), "spacer", p.getImgType(), "\" width=\"5\" height=\"0\" align=\"middle\" border=\"0\" alt=\"\"");
     
                        // BooleanValue
                        if(valueHolder instanceof BooleanValue) {
                            if(isEditable) {
                              String checkedModifier = "true".equals(stringifiedValueEdit) ? "checked" : "";
                              p.debug("<!-- BooleanValue -->");
                              p.write("<td>");
                              p.write("  <input type=\"hidden\" class=\"valueL\" name=\"", feature, "[", Integer.toString(editGridFieldIndex), "].false", "\" value=\"false\">");
                              p.write("  <input type=\"checkbox\" name=\"",  feature, "[", Integer.toString(editGridFieldIndex), "].true", "\" ", checkedModifier, " tabindex=\"", Integer.toString(editGridFieldIndex), "\" value=\"true\">");
                              p.write("</td>");
                            }
                            else {
                              p.debug("<!-- BooleanValue -->");
                              p.write("<td class=\"locked\">",  iconTag, "",  stringifiedValueShow, "</td>");
                            }
                        }
                        // DateValue
                        else if(valueHolder instanceof DateValue) {
                            if(isEditable) {
                              p.debug("<!-- DateValue -->");
                              p.write("<td nowrap>");
                              p.write("  <table class=\"gridSplit\"><tr><td width=\"100%\"><input type=\"text\" class=\"valueRG\" id=\"cal_field", Integer.toString(editGridFieldIndex), "\" name=\"",  feature, "[", Integer.toString(editGridFieldIndex), "]", "\" tabindex=\"", Integer.toString(editGridFieldIndex), "\" value=\"", stringifiedValueEdit, "\"></td>");
                              if(((DateValue)valueHolder).isDate()) {
                                  SimpleDateFormat dateFormatter = ((DateValue)valueHolder).getLocalizedDateFormatter();
                                  p.write("<td>", p.getImg("class=\"popUpButton\" id=\"cal_trigger", Integer.toString(editGridFieldIndex), "\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_CALENDAR, "\""));
                                  p.write("  <script language=\"javascript\" type=\"text/javascript\">");
                                  p.write("    Calendar.setup({");
                                  p.write("      inputField   : \"cal_field", Integer.toString(editGridFieldIndex), "\",");
                                  p.write("      ifFormat     : \"", DateValue.getCalendarFormat(dateFormatter), "\",");
                                  p.write("      firstDay     : ", Integer.toString(dateFormatter.getCalendar().getFirstDayOfWeek()-1), ",");
                                  p.write("      timeFormat   : \"24\",");
                                  p.write("      button       : \"cal_trigger", Integer.toString(editGridFieldIndex), "\",");
                                  p.write("      align        : \"Tr\",");
                                  p.write("      singleClick  : true,");
                                  p.write("      showsTime    : false");
                                  p.write("    });");
                                  p.write("  </script></td></tr></table>");
                              }
                              else {
                                  SimpleDateFormat dateTimeFormatter = ((DateValue)valueHolder).getLocalizedDateTimeFormatter();
                                  p.write("<td>", p.getImg("class=\"popUpButton\" id=\"cal_trigger", Integer.toString(editGridFieldIndex), "\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_CALENDAR, "\""));
                                  p.write("  <script language=\"javascript\" type=\"text/javascript\">");
                                  p.write("    Calendar.setup({");
                                  p.write("      inputField   : \"cal_field", Integer.toString(editGridFieldIndex), "\",");
                                  p.write("      ifFormat     : \"", DateValue.getCalendarFormat(dateTimeFormatter), "\",");
                                  p.write("      firstDay     : ", Integer.toString(dateTimeFormatter.getCalendar().getFirstDayOfWeek()-1), ",");
                                  p.write("      timeFormat   : \"24\",");
                                  p.write("      button       : \"cal_trigger", Integer.toString(editGridFieldIndex), "\",");
                                  p.write("      align        : \"Tr\",");
                                  p.write("      singleClick  : true,");
                                  p.write("      showsTime    : true");
                                  p.write("    });");
                                  p.write("  </script></td></tr></table>");
                              }
                              p.write("</td>");
                            }
                            else {
                              p.debug("<!-- DateValue -->");
                              p.write("<td class=\"locked\">",  iconTag, "",  stringifiedValueShow, "</td>");
                            }
                        }
                        // CodeValue
                        else if(valueHolder instanceof CodeValue) {
                            if(isEditable) { // and also isSingleValued()
                              SortedMap longTextsT = ((CodeValue)valueHolder).getLongText(false, false);
    
                              String longTextsAsJsArray = "";
                              for(Iterator options = longTextsT.keySet().iterator(); options.hasNext(); ) {
                                longTextsAsJsArray += longTextsAsJsArray.length() == 0 ? "" : ",";
                                longTextsAsJsArray += "'" + options.next() + "'";
                              }            
                              p.write("<td>");
                              p.write("  <select class=\"valueLG\" name=\"", feature, "[", Integer.toString(editGridFieldIndex), "]", "\" tabindex=\"", Integer.toString(editGridFieldIndex), "\">");
                              for(Iterator options = longTextsT.entrySet().iterator(); options.hasNext(); ) {
                                Map.Entry option = (Map.Entry)options.next();
                                String selectedModifier = option.getKey().equals(valueHolder.getValue(false)) ? "selected" : "";
                                p.write("<option ",  selectedModifier, " value=\"", option.getKey().toString(), "\">", option.getKey().toString());
                              }
                              p.write("  </select>");
                              p.write("</td>");
                            }
                            else {
                              p.debug("<!-- CodeValue -->");
                              p.write("<td class=\"locked\">",  iconTag, "", stringifiedValueShow, "</td>");
                            }
                        }
                        // NumberValue
                        else if(valueHolder instanceof NumberValue) {
                            if(isEditable) {
                                p.debug("<!-- NumberValue -->");
                                Autocompleter_1_0 autocompleter = valueHolder.getAutocompleter(
                                    objRow.getObject()
                                );
                                // Selectable values
                                if(autocompleter != null) {
                                    p.write("<td>");
                                    autocompleter.paint(
                                        p, 
                                        null,
                                        editGridFieldIndex,
                                        feature,
                                        valueHolder,
                                        true,
                                        null,
                                        null,
                                        "class=\"valueL\"",
                                        null
                                    );
                                    p.write("</td>");
                                }
                                else {
                                    p.write("<td><input class=\"valueRG\" type=\"text\" tabindex=\"", Integer.toString(editGridFieldIndex), "\" name=\"", feature, "[", Integer.toString(editGridFieldIndex), "]"+ "\" value=\"", stringifiedValueEdit, "\"></td>");
                                }
                            }
                            else {
                                p.write("<td class=\"locked\">",  iconTag, "",  stringifiedValueShow, "</td>");
                            }
                        }
                        // ObjectReferenceValue
                        else if(valueHolder instanceof ObjectReferenceValue) {
                            // Image only in first column
                            if(k == 0) {
                              Action action = ((ObjectReference)value).getSelectObjectAction();
                              rowIsChangeable = ((ObjectReference)value).getInspector() == null
                                  ? false
                                  : ((ObjectReference)value).getInspector().isChangeable(); 
                              p.write("<!-- ObjectReferenceValue -->"); // used as tag for multi-delete. Do not write as debug!
                              p.write("<td class=\"gridColTypeIconEdit-3\">");
                              p.write(p.getImg("src=\"", p.getResourcePath("images/"), "spacer", p.getImgType(), "\" alt=\"\" width=\"", Integer.toString(16*objRow.getGridRowNestingLevel()), "\" height=\"0\""), p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""));
                              if(rowIsChangeable) {
                                  if (showCloneOp) {
                                    p.write(p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_CLONE_SMALL, "\" class=\"popUpButton\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"", texts.getCloneTitle(), "\"", p.getOnClick("javascript:rowIndex", tabId, "++;cloneGridRow('editGridTable", tabId, "',this, ", Integer.toString(paneIndex), "*10000, rowIndex", tabId, "*100, 100);"), p.getOnMouseOver("javascript:this.className='popUpButtonhover';"), p.getOnMouseOut("javascript:this.className='popUpButton';")));
                                  }
                                  p.write(p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_DELETE_SMALL, "\" class=\"popUpButton\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"", htmlEncoder.encode(texts.getDeleteTitle(), false), "\"", p.getOnClick("javascript:deleteGridRow(this);"), p.getOnMouseOver("javascript:this.className='popUpButtonhover';"), p.getOnMouseOut("javascript:this.className='popUpButton';"), " style=\"display: none;\""));
                              }
                              p.write("  <input type=\"hidden\" name=\"refMofId",  "[", Integer.toString(editGridFieldIndex), "]", "\" tabindex=\"", Integer.toString(editGridFieldIndex), "\" value=\"", objRow.refMofId(), "\">");
                              p.write("</td>");
                            }
                            // columns > 0
                            else {
                                // Multi-valued
                                if(value instanceof Collection) {
                                    p.write("<!-- ObjectReferenceValue -->"); // used as tag for multi-delete. Do not write as debug!
                                    p.write("<td>");
                                    boolean isFirst = true;
                                    for(Iterator e = ((Collection)value).iterator(); e.hasNext(); ) {
                                        ObjectReference objRef = (ObjectReference)e.next();
                                        Action action = objRef.getSelectObjectAction();
                                        if(!isFirst) {
                                            p.write("<br />");                                    
                                        }                                
                                        p.write("<a href=\"\"", p.getOnClick("javascript:this.href=", p.getEvalHRef(action), ";"), ">", htmlEncoder.encode(action.getTitle(), false), "</a>");
                                        isFirst = false;                                
                                    }                            
                                    p.write("</td>");
                                }
                                // Single-valued
                                else {
                                    if(isEditable) {
                                        ObjectReference objectReference = (ObjectReference)valueHolder.getValue(false);
                                        String lookupId = org.openmdx.kernel.id.UUIDs.getGenerator().next().toString();
                                        Action findObjectAction = view.getFindObjectAction(
                                            feature, 
                                            lookupId
                                        );
                                        p.write("<td nowrap>");
                                        p.write("  <table class=\"gridSplit\"><tr><td width=\"100%\"><input type=\"text\" class=\"valueLG\" name=\"", feature, "[", Integer.toString(editGridFieldIndex), "]", ".Title\" tabindex=\"", Integer.toString(editGridFieldIndex), "\" value=\"", (objectReference == null ? "" : htmlEncoder.encode(objectReference.getTitle(), false)), "\"></td><td>", p.getImg("class=\"popUpButton\" border=\"0\" alt=\"Click to open ObjectFinder\" src=\"", p.getResourcePath("images/"), findObjectAction.getIconKey(), "\" name=\"", feature, "[", Integer.toString(editGridFieldIndex+1), "]", "\" id=\"", feature, "[", Integer.toString(editGridFieldIndex+1), "]\"", p.getOnClick("javascript:var IDsplit = this.id.split(/[\\[|\\]]/); var tidx = parseInt(IDsplit[1])-1; var par1 = '", feature, "' + '[' + tidx.toString() + ']'; var par2 = par1 + '.Title'; OF.findObject(", p.getEvalHRef(findObjectAction), ", document.forms['editForm", tabId, "'].elements[par2], document.forms['editForm", tabId, "'].elements[par1], '", lookupId, "');")));
                                        p.write("  <input type=\"hidden\" class=\"valueLLocked\" name=\"", feature, "[", Integer.toString(editGridFieldIndex), "]", "\" value=\"", (objectReference == null ? "" : objectReference.refMofId()), "\">");
                                        p.write("  </td></tr></table>");
                                        p.write("</td>");
                                    }
                                    else {
                                        Action action = ((ObjectReference)value).getSelectObjectAction();
                                        p.write("<!-- ObjectReferenceValue -->"); // used as tag for multi-delete. Do not write as debug!
                                        p.write("<td class=\"locked\"><a href=\"#\"", p.getOnClick("javascript:this.href=", p.getEvalHRef(action), ";"), ">", htmlEncoder.encode(action.getTitle(), false), "</a></td>");
                                    }
                                }
                            }
                        }
                        // TextValue
                        else {
                            if(isEditable) {
                                String inputType = valueHolder instanceof TextValue
                                    ? ((TextValue)valueHolder).isPassword() ? "password" : "text"
                                    : "text"; 
                                p.debug("<!-- TextValue -->");
                                Autocompleter_1_0 autocompleter = valueHolder.getAutocompleter(
                                    objRow.getObject()
                                );
                                // Selectable values
                                if(autocompleter != null) {                              
                                    p.write("<td>");
                                    autocompleter.paint(
                                        p,
                                        null,
                                        editGridFieldIndex,
                                        feature,
                                        valueHolder,
                                        false,
                                        null,
                                        null,
                                        "class=\"valueL\"",
                                        null
                                    );
                                    p.write("</td>");
                                }
                                else {
                                    p.write("<td><input class=\"valueLG\" type=\"", inputType, "\" tabindex=\"", Integer.toString(editGridFieldIndex), "\" name=\"", feature, "[", Integer.toString(editGridFieldIndex), "]\" value=\"", stringifiedValueEdit, "\"></td>");
                                }
                            }
                            else {
                                p.write("<td class=\"locked\">", iconTag, "", stringifiedValueShow, "</td>");
                            }
                        }
                        editGridFieldIndex++;
                      }
                      p.write("</tr>");
                  }
                  p.write("  </table>");
                  p.write("  <input type=\"hidden\" name=\"requestId.submit\" value=\"", view.getRequestId(), "\" />");
                  p.write("  <input type=\"hidden\" name=\"event.submit\" value=\"", Integer.toString(saveAction.getEvent()), "\" />");
                  p.write("  <input type=\"hidden\" name=\"", Action.PARAMETER_PANE, "\" value=\"", saveAction.getParameter(Action.PARAMETER_PANE), "\" />");
                  p.write("  <div style=\"float: right; padding-top:1px;\">");
                  p.write("    <input class=\"flatsubmit\" type=\"submit\" title=\"", htmlEncoder.encode(saveAction.getTitle(), false), "\" value=\"", htmlEncoder.encode(saveAction.getTitle(), false), "\" id=\"bl-editForm", tabId, "\"", p.getOnMouseOver("javascript:this.className='flatsubmithover';"), p.getOnMouseOut("javascript:this.className='flatsubmit';"), " />");
                  p.write("    <input class=\"flatsubmit\" type=\"submit\" title=\"", htmlEncoder.encode(texts.getCancelTitle(), false), "\" value=\"", texts.getCancelTitle(), "\"", p.getOnClick("javascript:$('editGrid", tabId, "').style.display='none'; $('showGrid", tabId, "').style.display='block';$('showGridButtons", tabId, "').style.display='block'; if($('menuCrPanel", tabId, "')) {$('menuCrPanel", tabId, "').style.display='block';}; return false;"), p.getOnMouseOver("javascript:this.className='flatsubmithover';"), p.getOnMouseOut("javascript:this.className='flatsubmit';"), " />");
                  p.write("  </div>");
                  p.write("  <div style=\"padding-top:1px;\">");
                  p.write("    <input class=\"flatsubmit\" type=\"submit\" title=\"", htmlEncoder.encode(saveAction.getTitle(), false), "\" value=\"", htmlEncoder.encode(saveAction.getTitle(), false), "\" id=\"br-editForm", tabId, "\"", p.getOnMouseOver("javascript:this.className='flatsubmithover';"), p.getOnMouseOut("javascript:this.className='flatsubmit';"), " />");
                  p.write("    <input class=\"flatsubmit\" type=\"submit\" title=\"", htmlEncoder.encode(texts.getCancelTitle(), false), "\" value=\"", texts.getCancelTitle(), "\"", p.getOnClick("javascript:$('editGrid", tabId, "').style.display='none'; $('showGrid", tabId, "').style.display='block';$('showGridButtons", tabId, "').style.display='block'; if ($('menuCrPanel", tabId, "')) {$('menuCrPanel", tabId, "').style.display='block';}; return false;"), p.getOnMouseOver("javascript:this.className='flatsubmithover';"), p.getOnMouseOut("javascript:this.className='flatsubmit';"), " />");
                  p.write("  </div>");
                  p.write(" </form>");
                  p.write("</div>");
                }                
            }
        }
        
        AppLog.detail("< paint");
    }
    
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258132466186203704L;

    public static final String FRAME_VIEW = "View";
    public static final String FRAME_CONTENT = "Content";

    public static final int[] DEFAULT_PAGE_SIZES = new int[]{5, 10, 20, 50, 100, 200, 500};  

    // Unique number for each generated row allows to generate unique id tags 
    // (e.g. required for row menu)
    private static long currentRowId = 0L;
    
    protected boolean isMultiDeleteEnabled = true;
    protected Action[] selectReferenceActions = null;
    protected GridControl[] gridControl = null;
    
}

//--- End of File -----------------------------------------------------------
