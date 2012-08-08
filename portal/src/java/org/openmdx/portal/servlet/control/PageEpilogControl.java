/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: PageEpilogControl.java,v 1.61 2007/08/06 11:22:19 wfro Exp $
 * Description: PageEpilogControl 
 * Revision:    $Revision: 1.61 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/08/06 11:22:19 $
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
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.text.StringBuilders;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.View;

public class PageEpilogControl
    extends Control
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public PageEpilogControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory
        );
    }
    
    //-----------------------------------------------------------------------
    public void paint(
        HtmlPage p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        
        AppLog.detail("> paint");        
        
        ObjectView view = (ObjectView)p.getView();
        ApplicationContext app = view.getApplicationContext();
        boolean editMode = view instanceof EditObjectView;
        String guiLook = app.getCurrentGuiMode();
        boolean noLayoutManager = 
            guiLook.equals(WebKeys.SETTING_GUI_MODE_BASIC);
        boolean useYuiExtTabs = 
            guiLook.equals(WebKeys.SETTING_GUI_MODE_ADVANCED);
        
        int currentChartId = p.getProperty(HtmlPage.PROPERTY_CHART_ID) != null
            ? ((Integer)p.getProperty(HtmlPage.PROPERTY_CHART_ID)).intValue()
            : 0;
        int nActiveTab = p.getProperty(HtmlPage.PROPERTY_N_ACTIVE_TAB) != null
            ? ((Integer)p.getProperty(HtmlPage.PROPERTY_N_ACTIVE_TAB)).intValue()
            : 0;
          
        if(!editMode) {
            if(currentChartId > 0) {
                p.write("<script language=\"javascript\" type=\"text/javascript\" src=\"javascript/diagram.js\"></script>");
                p.write("<script language=\"javascript\" type=\"text/javascript\" src=\"javascript/chart.js\"></script>");
            }
        }
        
        // Init scripts
        p.write("<script language=\"javascript\" type=\"text/javascript\">");
        
        // Does page contains charts?
        if(!editMode) {
            if(currentChartId > 0) {
                p.write("pageHasCharts = true;");             
            }
            else {
                p.write("pageHasCharts = false;");            
            }
        }

        // window.onresize
        p.write("window.onresize=function() {  // is called from guicontrol.js showPanel()");
        for(int i = 0; i < currentChartId; i++) {
            p.write("    displayChart" + i, "();");
        }
        p.write("}");
        
        // dateSelected
        p.write("function dateSelected(calendar, date) {");
        p.write("  if (calendar.dateClicked) {");
        p.write("  };");
        p.write("}");
        SimpleDateFormat dateFormatter = DateValue.getLocalizedDateFormatter(
            null, 
            true, 
            app
        );
        List calendarIds = (List)p.getProperty(HtmlPage.PROPERTY_CALENDAR_IDS);
        for(Iterator i = calendarIds.iterator(); i.hasNext(); ) {
            String calendarId = (String)i.next();
            p.write("Calendar.setup({");
            p.write("  flat         : \"", calendarId, "\",");
            p.write("  onSelect     : dateSelected,");
            p.write("  daFormat     : \"", DateValue.getCalendarFormat(dateFormatter), "\",");
            p.write("  align        : \"Tc\",");
            p.write("  firstDay     : ", Integer.toString(dateFormatter.getCalendar().getFirstDayOfWeek()-1), ",");
            p.write("  singleClick  : true,");
            p.write("  showsTime    : false,");
            p.write("  weekNumbers  : true");
            p.write("});");
        }
        // Layout
        p.write("initLayout = function(){");
        p.write("    return {");
        p.write("      init : function(){");
        // No layout in edit mode
        if(!editMode) {
            // Breadcrums / Select parent actions
            if(noLayoutManager) {
                p.write("      var breadcrum = \"\";");                
            }
            else {
                p.write("      var breadcrum = \"&nbsp;&nbsp;&nbsp;&nbsp;\";");
            }
            Action[] selectParentActions = view.getSelectParentAction();
            for(int i = 0; i < selectParentActions .length; i++) {
                Action selectParentAction = selectParentActions[i];
                if(selectParentAction != null) {
                    String breadcrum = StringBuilders.newStringBuilder(
                        i > 0 ? " > " : "" 
                    ).append(
                         "<a href=\"#\""
                    ).append(
                        p.getOnClick("javascript:this.href=", p.getEvalHRef(selectParentAction), ";")
                    ).append(                        
                        ">"
                    ).append(
                        selectParentAction.getTitle()
                    ).append(
                        "</a>"
                    ).toString();
                    p.write("      breadcrum = breadcrum + \"", breadcrum.toString().replaceAll("\"", "\\\\\""), "\";");
                }
            }
            if(noLayoutManager) {
                p.write("      $('inspBreadcrum').innerHTML = breadcrum;");                
            }
            else {
                p.write("        layout = new YAHOO.ext.BorderLayout(document.body, {");
                p.write("                     hideOnLayout: true,");
                p.write("                     north: {");
                p.write("                        split:false,");
                p.write("                        initialSize: ", Integer.toString(this.panelSizeNorth), ",");
                p.write("                        titlebar: true,");
                p.write("                        collapsible: true");
                p.write("                     },");
                p.write("                     west: {");
                p.write("                        split:false,");
                p.write("                        initialSize: ", Integer.toString(this.panelSizeWest), ",");
                p.write("                        titlebar: true,");
                p.write("                        collapsible: true");
                p.write("                     },");
                p.write("                     center: {");
                p.write("                        titlebar: true,");
                p.write("                        autoScroll: true");
                p.write("                     }");
                p.write("        });");
                p.write("      layout.beginUpdate();");
                p.write("      layout.add('north',  new YAHOO.ext.ContentPanel('header',     {title: '', fitToFrame:true,  closable:false}));");
                p.write("      layout.add('west',   new YAHOO.ext.ContentPanel('navigation', {title: '', fitToFrame:false,  closable:false}));");
                p.write("      layout.add('center', new YAHOO.ext.ContentPanel('content',    {title: breadcrum, fitToFrame:true, closable:false}));");
                p.write("      $('header').parentNode.style.overflow = 'visible'; // required to show fly-out menues [IE bug]");
                p.write("      $('navigation').parentNode.style.overflow = 'visible'; // required to show fly-out menues [IE bug]");
                if(view.getPanelState("north") == PanelControl.PANEL_STATE_HIDE) {
                    p.write("      layout.getRegion('north').collapse(false);");
                }
                if(view.getPanelState("west") == PanelControl.PANEL_STATE_HIDE) {
                    p.write("      layout.getRegion('west').collapse(false);");
                }
                p.write("      layout.getRegion('north').addListener('expanded', function(){new Ajax.Request(", p.getEvalHRef(view.getSetPanelStateAction("north", PanelControl.PANEL_STATE_SHOW)), ", {asynchronous:true});});");
                p.write("      layout.getRegion('north').addListener('collapsed', function(){new Ajax.Request(", p.getEvalHRef(view.getSetPanelStateAction("north", PanelControl.PANEL_STATE_HIDE)), ", {asynchronous:true});});");
                p.write("      layout.getRegion('west').addListener('expanded', function(){new Ajax.Request(", p.getEvalHRef(view.getSetPanelStateAction("west", PanelControl.PANEL_STATE_SHOW)), ", {asynchronous:true});});");
                p.write("      layout.getRegion('west').addListener('collapsed', function(){new Ajax.Request(", p.getEvalHRef(view.getSetPanelStateAction("west", PanelControl.PANEL_STATE_HIDE)), ", {asynchronous:true});});");
                p.write("");
            }
        }
        // Prepare attribute inspector panel
        if(nActiveTab > 0) {
            if(useYuiExtTabs) {
                p.write("      var attributePanel = new YAHOO.ext.TabPanel('inspector');");
                p.write("      attributePanel.beginUpdate();");
                AttributeTabControl[] attributeTabControl = 
                    view.getAttributePane().getAttributePaneControl().getAttributeTabControl();
                int nAttributeTabs = attributeTabControl.length;
                for(int i = 0; i < nAttributeTabs; i++) {
                    AttributeTabControl tab = attributeTabControl[i];                            
                    p.write("      attributePanel.addTab('tab", Integer.toString(i), "', \"", tab.getName(), "\");");
                }
                p.write("      var allTab = attributePanel.addTab('tab", Integer.toString(nAttributeTabs+1), "', \"*\");");
                p.write("      allTab.on('activate', function(){");
                for(int i = 0; i < nAttributeTabs; i++) {
                    p.write("        attributePanel.getTab('tab", Integer.toString(i), "').show();");
                }
                p.write("      });");
                if(currentChartId > 0) {
                    p.write("      attributePanel.addListener('tabchange', function(tp, tab){if(pageHasCharts) {window.onresize();}});");
                }
                p.write("      attributePanel.endUpdate();");
                p.write("      attributePanel.activate('tab0');");
            }
        }
        // No grid panels in edit mode
        if(!editMode) {
            ShowObjectView showView = (ShowObjectView)view;
            // Prepare grid panels
            int nReferencePanes = showView.getReferencePane().length;
            for(int i = 0; i < nReferencePanes; i++) {
                ReferencePane referencePane = showView.getReferencePane()[i];                
                int paneIndex = referencePane.getReferencePaneControl().getPaneIndex();
                String paneId = Integer.toString(paneIndex);
                if(useYuiExtTabs) {
                    p.write("      gridPanel", paneId, " = new YAHOO.ext.TabPanel('gridPanel", paneId, "');");
                    p.write("      gridPanel", paneId, ".beginUpdate();");
                }
                boolean isGroupTabActive = false;
                int lastGroupTabIndex = 0;
                int nGridControl = referencePane.getReferencePaneControl().getGridControl().length;
                for(int j = 0; j < nGridControl; j++) {
                    ReferencePaneControl gridTab = referencePane.getReferencePaneControl();
                    Action selectReferenceTabAction = gridTab.getSelectReferenceAction()[j];     
                    int tabIndex = 100*paneIndex + j;
                    String tabId = Integer.toString(tabIndex);
                    // Tab grouping. Generate hide/show tabs for each group of
                    // tabs having a label starting with >>
                    String tabTitle = selectReferenceTabAction.getTitle();
                    boolean isGroupTab = tabTitle.startsWith(WebKeys.TAB_GROUPING_CHARACTER);
                    if(isGroupTab) {
                        tabTitle = tabTitle.substring(1);
                    }
                    // Prolog hidden tabs
                    if(!isGroupTabActive && isGroupTab) {
                        isGroupTabActive = true;
                        lastGroupTabIndex = j;
                        if(useYuiExtTabs) {
                            p.write("      var gridTab", tabId, "s = gridPanel", paneId, ".addTab('gridTab", tabId, "s', \"", WebKeys.TAB_GROUPING_CHARACTER, "\");");
                            p.write("      gridTab", tabId, "s.setTooltip('');");
                            p.write("      gridTab", tabId, "s.on('activate',function(){try{gridTab", tabId, "s.prevActiveTab.activate();}catch(e){}});");
                        }
                    }
                    // Add tab
                    if(useYuiExtTabs) {
                        p.write("      var gridTab", tabId, " = gridPanel", paneId, ".addTab('gridTab", tabId, "', \"", tabTitle.replaceAll("'", "\\\\'"), "\");");
                        p.write("      gridTab", tabId, ".getUpdateManager().loadScripts = true;");
                        p.write("      gridTab", tabId, ".setTooltip('", selectReferenceTabAction.getToolTip().replaceAll("'", "\\\\'"), "');");
                        p.write("      $('gridTab", tabId, "').parentNode.style.overflow = \'visible\';");                    
                        p.write("      var gridTab", tabId, "Mgr = gridTab", tabId, ".setUrl(", p.getEvalHRef(selectReferenceTabAction), ", null, false); // true=load once, false=load multiple");
                        p.write("      gridTab", tabId, "Mgr.onUpdate.subscribe(function(){try{gridTab", tabId, ".bodyEl.setHeight('');makeZebraTable('gridTable", tabId, "',1);}catch(e){}});");
                        if(isGroupTab) {
                            p.write("      gridPanel", paneId, ".hideTab('gridTab", tabId, "');");
                        }
                    }
                    else {
                        // Get content for selected grid
                        if(j == referencePane.getSelectedReference()) {
                            p.write("      new Ajax.Updater('gridContent", paneId, "', ", p.getEvalHRef(selectReferenceTabAction), ", {asynchronous:true, evalScripts: true, onComplete: function(){try{makeZebraTable('gridTable", tabId, "',1);}catch(e){};}});");
                        }
                    }
                    // Epilog hidden tabs. Special treatment if last tab of grid is a group tab 
                    if(isGroupTabActive && (!isGroupTab || (j == nGridControl-1))) {
                        isGroupTabActive = false;
                        if(useYuiExtTabs) {
                            // Activate hidden tabs if group tab is selected
                            p.write("      gridPanel", paneId, ".addListener('beforetabchange', function(tp, e, tab) {");
                            p.write("        if(tab == gridTab", Integer.toString(paneIndex*100 + lastGroupTabIndex), "s) {");                        
                            p.write("          gridPanel", paneId, ".hideTab('gridTab", Integer.toString(paneIndex*100 + lastGroupTabIndex), "s');");
                            for(int k = lastGroupTabIndex; k <= j; k++) {
                                p.write("          gridPanel", paneId, ".unhideTab('gridTab", Integer.toString(paneIndex*100 + k), "');");
                            }
                            p.write("          tab.prevActiveTab = tp.active;");
                            p.write("        }");
                            p.write("      });");
                            p.write("");
                        }
                    }                    
                }
                if(useYuiExtTabs) {
                    p.write("      gridPanel", paneId, ".addListener('beforetabchange', function(tp, e, tab){try{var h=tp.active.bodyEl.getHeight();tab.bodyEl.setHeight(h);}catch(e){}});");                    
                    p.write("      gridPanel", paneId, ".unhideTab('gridTab", Integer.toString(paneIndex*100 + referencePane.getSelectedReference()), "');");
                    p.write("      gridPanel", paneId, ".endUpdate();");
                    p.write("      gridTab", Integer.toString(paneIndex*100 + referencePane.getSelectedReference()), ".activate();");
                }
            }
            p.write("");
            if(!noLayoutManager) {
                p.write("      layout.endUpdate();");
            }
        }
        p.write("    }");
        p.write("  }");
        p.write("}();");
        p.write("YAHOO.ext.SSL_SECURE_URL = '", p.getHttpServletRequest().getContextPath(), "/blank.html';");
        p.write("YAHOO.ext.EventManager.ieDeferSrc = YAHOO.ext.SSL_SECURE_URL;");
        p.write("YAHOO.ext.UpdateManager.defaults.indicatorText = \"<div class='loading-indicator'>&nbsp;</div>\";");
        p.write("YAHOO.ext.UpdateManager.defaults.timeout = 60;");        
        p.write("YAHOO.ext.BasicDialog.prototype.syncHeightBeforeShow = true;");
        p.write("YAHOO.ext.EventManager.onDocumentReady(initLayout.init, initLayout, true);");
        // Declare variables for layout and grid panels. This way they can be accessed controls
        if(!editMode) {
            p.write("var layout = null;");
            ShowObjectView showView = (ShowObjectView)view;
            int nReferencePanes = showView.getReferencePane().length;
            for(int i = 0; i < nReferencePanes; i++) {
                ReferencePane referencePane = showView.getReferencePane()[i];                
                int referencePaneIndex = referencePane.getReferencePaneControl().getPaneIndex();
                String referencePaneId = Integer.toString(referencePaneIndex);
                p.write("var gridPanel", referencePaneId, " = null;");
            }
        }
        p.write("");
        p.write("function initPage() {");
        if(currentChartId > 0) {
            p.write("  window.onresize();");
        }
        if(view.getMacro() != null) {
            Object[] macro = view.getMacro();
            Number actionType = (Number)macro[0];
            String actionName = (String)macro[1];
            if(actionType.intValue() == Action.MACRO_TYPE_JAVASCRIPT) {
                actionName = actionName.replaceAll(View.REQUEST_ID_TEMPLATE, view.getRequestId());
                p.write("  ", actionName);
            }
            view.setMacro(null);
        }
        p.write("}");
        p.write("</script>");
        // Generate div for each popup image
        Map popupImages= (Map)p.getProperty(HtmlPage.PROPERTY_POPUP_IMAGES);
        for(Iterator i = popupImages.keySet().iterator(); i.hasNext(); ) {
            String imageId = (String)i.next();
            String imageSrc = (String)popupImages.get(imageId);
            p.write("  <div class=\"divImgPopUp\" id=\"divImgPopUp", imageId, "\" style=\"display: none;\" onmousedown=\"dragPopupStart(event, this.id);\" ondblclick=\"javascript:this.style.display='none'\" >");
            p.write("  ", p.getImg("class=\"popUpImg\" id=\"popUpImg", imageId, "\" src=\"", imageSrc, "\" alt=\"\""), "</div>");
        }

        AppLog.detail("< paint");        
    }

    //-----------------------------------------------------------------------
    public void setPanelSizeNorth(
        int newValue
    ) {
        this.panelSizeNorth = newValue;
        
    }
    
    //-----------------------------------------------------------------------
    public int getPanelSizeNorth(
    ) {
        return this.panelSizeNorth;
    }
    
    //-----------------------------------------------------------------------
    public void setPanelSizeWest(
        int newValue
    ) {
        this.panelSizeWest = newValue;
        
    }
    
    //-----------------------------------------------------------------------
    public int getPanelSizeWest(
    ) {
        return this.panelSizeWest;
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = -294211239994971237L;
    
    private int panelSizeNorth = 125;
    private int panelSizeWest = 230;
    
}
