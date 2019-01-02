/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: UiReferencePane
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.text.conversion.HtmlEncoder;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Autocompleter_1_0;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.UserSettings;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.UiGridAddColumnFilterAction;
import org.openmdx.portal.servlet.action.UiGridGetRowMenuAction;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.BooleanValue;
import org.openmdx.portal.servlet.attribute.ObjectReferenceValue;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.control.ReferencePaneControl;
import org.openmdx.portal.servlet.control.UiGridControl;
import org.openmdx.portal.servlet.control.UiReferencePaneControl;
import org.openmdx.ui1.jmi1.ValuedField;

/**
 * UiReferencePane
 *
 */
public class UiReferencePane extends ReferencePane implements Serializable {
  
	/**
	 * Constructor 
	 *
	 * @param control
	 * @param view
	 * @param lookupType
	 */
	public UiReferencePane(
		ReferencePaneControl control,
		ObjectView view,
		String lookupType
	) {
		super(
			control,
			view,
			lookupType
		);
		ApplicationContext app = view.getApplicationContext();
		Model_1_0 model = view.getApplicationContext().getModel();
		// Children (grids) are initialized on-demand
		List<UiGridControl> gridControls = control.getChildren(UiGridControl.class);
		this.grids = new ArrayList<UiGrid>();
		for(int i = 0; i < gridControls.size(); i++) {
			this.grids.add(null);
		}
		// Index of initial reference / grid
		int initialReference = -1;
		for(int i = 0; i < gridControls.size(); i++) {
			try {
				boolean isRevokeShow = app.getPortalExtension().hasPermission(
					gridControls.get(i).getQualifiedReferenceName(), 
					view.getObject(), 
					app, 
					WebKeys.PERMISSION_REVOKE_SHOW
				);
				boolean showRowSelectors = UiGridControl.getShowRowSelectors(
					lookupType, 
					model.getElement(gridControls.get(i).getObjectContainer().getReferencedTypeName()), 
					model
				);
				if(initialReference == -1 && (lookupType == null || showRowSelectors) && !isRevokeShow) {
					initialReference = i;
				}
			} catch(Exception e) {}
		}
		this.selectReference(initialReference == -1 ? 0 : initialReference);
	}

    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Canvas#getView()
	 */
	@Override
	public ObjectView getView(
	) {
		return (ObjectView)this.view;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.component.ReferencePane#selectReference(int)
	 */
	@Override
    public boolean selectReference(
        int index
    ) {
        boolean refreshed = false;
        this.selectedReference = index;
        if(
            (index >= 0) && 
            (index < this.grids.size()) && 
            (this.grids.get(index) == null)
        ) {            
        	ObjectView view = this.getView();
        	UiGridControl gridControl = this.getReferencePaneControl().getChildren(UiGridControl.class).get(index);
            if(gridControl.getObjectContainer().isReferenceIsStoredAsAttribute()) {
                this.grids.set(
                	index, 
                	new UiReferenceGrid(                
                		gridControl,
	                    view,
	                    this.lookupType
	                )
                );
            } else {
                this.grids.set(
                	index, 
                	new UiCompositeGrid(
                		gridControl,
	                    view,
	                    this.lookupType
	                )
                );
            }
            refreshed = true;
        }
        return refreshed;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.component.ReferencePane#getSelectedReference()
	 */
	@Override
    public int getSelectedReference(
    ) {
        this.selectedReference = java.lang.Math.min(
            this.selectedReference, 
            this.grids.size() - 1
        );
        return this.selectedReference;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.component.ReferencePane#getGrid()
	 */
	@Override
    public UiGrid getGrid(
	) {
    	if(
			(this.selectedReference >= 0) && 
			(this.selectedReference < this.grids.size())
		) {
    		return this.grids.get(this.selectedReference);
    	} else {
    		return null;
    	}
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.component.ReferencePane#getSelectReferenceActions()
	 */
	@Override
    public List<Action> getSelectReferenceActions(
    ) {
    	ApplicationContext app = this.view.getApplicationContext();
    	ObjectView view = this.getView();
        List<Action> selectReferenceActions = new ArrayList<Action>();
        for(Action template: this.getSelectReferenceAction()) {
            List<Action.Parameter> parameters = new ArrayList<Action.Parameter>(
                Arrays.asList(template.getParameters())
            );
            parameters.add(
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getObjectReference().getXRI())
            );
            selectReferenceActions.add(
            	new Action(
	                template.getEvent(),
	                (Action.Parameter[])parameters.toArray(new Action.Parameter[parameters.size()]),
	                app.getPortalExtension().getTitle(view.getObject(), template, template.getTitle(), app),
	                template.getToolTip(),
	                template.getIconKey(),
	                template.isEnabled()
	            )
            );
        }
        return selectReferenceActions;
    }

    /**
     * Get control casted to ReferencePaneControl.
     * 
     * @return
     */
    public UiReferencePaneControl getReferencePaneControl(
    ) {
    	return (UiReferencePaneControl)this.control;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.ControlState#refresh(boolean)
     */
    @Override
    public void refresh(
        boolean refreshData
    ) {
        for(int i = 0; i < this.grids.size(); i++) {
            if(this.grids.get(i) != null) {
            	this.grids.get(i).refresh(refreshData);
            }
        }
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Component#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		if(type == Grid.class) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.grids;
			return children;
		} else {
			return Collections.emptyList();
		}
	}

    /**
     * Paint the filter menus.
     * 
     * @param p
     * @param grid
     * @param filters
     * @param updateTabScriptletPre
     * @param updateTabScriptletPost
     * @param groupingStyle
     * @throws ServiceException
     */
    protected void paintFilterMenus(
    	ViewPort p,
    	UiGrid grid,
    	org.openmdx.portal.servlet.Filter[] filters,
    	String updateTabScriptletPre,
    	String updateTabScriptletPost,
    	String groupingStyle
    ) throws ServiceException {
    	ApplicationContext app = p.getApplicationContext();
    	HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
    	Texts_1_0 texts = app.getTexts();
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
    			// Is selected filter member of this group?
    			boolean groupHasSelectedFilter = false;
    			int k = j;
    			while(k < filters.length && filterGroupName.equals(filters[k].getGroupName())) {
    				boolean isSelected = grid.getCurrentFilter().getName().equals(filters[k].getName());
    				if(isSelected) {
    					groupHasSelectedFilter = true;
    					break;
    				}
    				k++;
    			}
    			boolean hilite = groupHasSelectedFilter;
    			// Class filter group
    			if("1".equals(filterGroupName)) {
    				p.write("      <li class=\"", CssClass.dropdown.toString(), (hilite ? " " + CssClass.active : ""), "\" onclick=\"javascript:toggleMenu(this);\"><a href=\"#!\" class=\"", CssClass.dropdownToggle.toString(), "\" style=\"", STYLE_GRID_MENU_SMALL, "\">", p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_FILTER_CLASS, "\" border=\"0\" align=\"absmiddle\" alt=\"o\" title=\"", htmlEncoder.encode(texts.getClassFilterTitle(), false), "\""), "</a>");
    			} 
    			// All other filter groups
    			else {
    				p.write("      <li class=\"", CssClass.dropdown.toString(), (hilite ? " " + CssClass.active : ""), "\" onclick=\"javascript:toggleMenu(this);\"><a href=\"#!\" class=\"", CssClass.dropdownToggle.toString(), "\" style=\"", STYLE_GRID_MENU_SMALL, "\">", p.getImg("src=\"", p.getResourcePath("images/"), "filter_", htmlEncoder.encode(filterGroupName, false), p.getImgType(), "\" border=\"0\" align=\"absmiddle\" style=\"margin-right: 2px;\" alt=\"o\" title=\"", htmlEncoder.encode(filterGroupName, false), "\""), "</a>");
    			}
    			p.write("        <ul class=\"", CssClass.dropdownMenu.toString(), "\" role=\"menu\">");
    		} 
    		filterGroupName = filter.getGroupName();
    		if(!filter.hasParameter()) {
    			Action action = grid.getSelectFilterAction(filter);
    			boolean hilite = grid.getCurrentFilter().getName().equals(filter.getName());
    			if(filterGroupName.equals("0")) {
    				p.write("        <li class=\"", (hilite ? " " + CssClass.active : ""), "\"><a href=\"#\" style=\"", groupingStyle, ";", STYLE_GRID_MENU_SMALL, "\" \" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(action), updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"absmiddle\" alt=\"o\" title=\"", htmlEncoder.encode(action.getTitle(), false), "\""), "</a></li>");
    			} else {
    				p.write("        <li class=\"", (hilite ? " class=\"" + CssClass.active : ""), "\"><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(action), updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"absmiddle\" alt=\"o\" title=\"\""), " ", htmlEncoder.encode(texts.getSelectAllText(), false), " ", htmlEncoder.encode(action.getTitle(), false), "</a></li>");
    			}
    		}
    	}
    	// Close current filter group
    	if(!"0".equals(filterGroupName)) {
    		p.write("          </ul>");
    		p.write("        </li>");
    	}    	
    }
    
    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.component.Component#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
	 */
	@Override
	public void paint(
		ViewPort p, 
		String frame, 
		boolean forEditing
	) throws ServiceException {
		this.paint(
			p, 
			frame, 
			forEditing, 
			null
		);
	}

    /**
     * Get element id of search form.
     * 
     * @param formId
     * @param selectGridColumnAction
     * @return
     */
    protected String getSearchFormFieldId(
    	String formId,
    	Action selectGridColumnAction
   	) {
    	String filterName = selectGridColumnAction.getParameter(Action.PARAMETER_NAME);    	
    	return SEARCH_FORM_NAME + formId + "." + filterName;     	
    }
    
    /**
     * Get row menu action.
     * 
     * @param targetRowXri
     * @param rowId
     * @return
     */
	public Action getRowMenuAction(
		String targetRowXri,
		String rowId
	) {
		return new Action(
			UiGridGetRowMenuAction.EVENT_ID,
			new Action.Parameter[]{
				new Action.Parameter(Action.PARAMETER_TARGETXRI, targetRowXri),
				new Action.Parameter(Action.PARAMETER_ROW_ID, rowId)
			},
			"",
			true
		);
	}
  
	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.component.ReferencePane#getSelectReferenceAction()
	 */
	@Override
    public Action[] getSelectReferenceAction(
    ) {
        return this.getReferencePaneControl().getSelectReferenceAction();
    }

    /**
     * Return true if search form is shown by default for this grid.
     * 
     * @param grid
     * @param app
     * @return
     */
    protected boolean showSearchForm(
        UiGrid grid,
        ApplicationContext app
    ) {
        String propertyName = grid.getControl().getPropertyName(
            grid.getQualifiedReferenceName(),
            UserSettings.SHOW_SEARCH_FORM.getName()
        );
        return app.getSettings().getProperty(propertyName) != null 
        	? Boolean.valueOf(app.getSettings().getProperty(propertyName)).booleanValue() 
        	: false;        
    }

    /**
     * Paint search form.
     * 
     * @param p
     * @param grid
     * @param id
     * @param formStyle
     * @param labelStyle
     * @param buttonStyle
     * @param searchableColumns
     * @param updateTabScriptletPre
     * @param updateTabScriptletPost
     * @throws ServiceException
     */
    protected void paintSearchForm(
    	ViewPort p,
    	UiGrid grid,
    	String id,
    	String formStyle,
    	String labelStyle,
    	String buttonStyle,
    	List<Action> searchableColumns,
    	String updateTabScriptletPre,
    	String updateTabScriptletPost
    ) throws ServiceException {
    	ApplicationContext app = p.getApplicationContext();
    	PortalExtension_1_0 portalExtension = app.getPortalExtension();
    	HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
    	Texts_1_0 texts = app.getTexts();
    	boolean showSearchFormOnInit = this.showSearchForm(grid, app);
    	String formId = SEARCH_FORM_NAME + id;
    	p.write("  <form id=\"", formId, "\" class=\"", CssClass.dialog.toString(), "\" style=\"", formStyle, "\">");
		p.write("    <fieldset style=\"margin:-5px 0px 5px 0px;\">");
		p.write("    <div>");
		p.write("      <div class=\"", CssClass.row.toString(), "\">");
		p.write("        <div class=\"", CssClass.colMd3.toString(), "\">");
    	Action columnFilterAddAction = searchableColumns.get(0);
    	int count = 0;
    	for(int i = 0; i < searchableColumns.size(); i++) {
    		Action action = searchableColumns.get(i);
    		if(action.getTitle() != null && !action.getTitle().trim().isEmpty()) {
				if(count > 0 && count % 5 == 0) {	        		
					p.write("        </div>");
					p.write("        <div class=\"", CssClass.colMd3.toString(), "\">");
				}
    			String fieldName = action.getParameter(Action.PARAMETER_NAME);
				PortalExtension_1_0.SearchFieldDef searchFieldDef = portalExtension.getSearchFieldDef(
					grid.getQualifiedReferenceName(), 
					fieldName, 
					app
				);
				if(searchFieldDef != null) {
					String searchFieldId = formId + "_" + fieldName;
					p.write("<div class=\"", CssClass.row.toString(), "\">");					
					p.write("  <div class=\"", CssClass.colSm12.toString(), "\">");
					p.write("    <input type=\"text\" id=\"", searchFieldId, "\" name=\"", WebKeys.REQUEST_PARAMETER_FILTER_VALUES, ".", fieldName, "\" placeholder=\"", htmlEncoder.encode(action.getTitle(), false), "\" style=\"padding:4px;margin:2px;width:100%;\" value=\"", htmlEncoder.encode(grid.getFilterValue(fieldName), false), "\" />");
					p.write("  </div>");
					p.write("</div>");
	                p.write("<div class=\"", CssClass.autocomplete.toString(), "\" id=\"", searchFieldId, ".Update\" style=\"display:none;z-index:1200;\"></div>");
	                p.write("<script type=\"text/javascript\" language=\"javascript\" charset=\"utf-8\">");
	                p.write("  ", searchFieldId, "_ac = new Ajax.Autocompleter(");
	                p.write("    '", searchFieldId, "',");
	                p.write("    '", searchFieldId, ".Update',");
	                p.write("    '", p.getEncodedHRef(searchFieldDef.getFindValuesAction(p.getView().getObject(), app)), "',");
	                p.write("    {");
	                p.write("      ", "paramName: '", WebKeys.REQUEST_PARAMETER_FILTER_VALUES, "', ");
	                p.write("      ", "minChars: 0,");
	                p.write("      ", "afterUpdateElement: function(field, selectedItem){selectedItemHtml=selectedItem.innerHTML;field.value=selectedItemHtml;}");
	                p.write("    }");
	                p.write("  );");
	                p.write("</script>");
				} else {
					p.write("<div class=\"", CssClass.row.toString(), "\">");					
					p.write("  <div class=\"", CssClass.colSm12.toString(), "\">");
					p.write("    <input type=\"text\" id=\"", formId + "." + fieldName, "\" name=\"", WebKeys.REQUEST_PARAMETER_FILTER_VALUES, ".", fieldName, "\" placeholder=\"",  htmlEncoder.encode(action.getTitle(), false), "\" style=\"padding:4px;margin:2px;width:100%;\" value=\"", htmlEncoder.encode(grid.getFilterValue(fieldName), false), "\"/>");
					p.write("  </div>");
					p.write("</div>");
				}
    			count++;
    		}
    	}
		p.write("        </div>");
		p.write("      </div>");
		String showSearchFormFieldId = formId + "." + WebKeys.REQUEST_PARAMETER_SHOW_SEARCH_FORM;
		String resetFilterFieldId = formId + "." + WebKeys.REQUEST_PARAMETER_RESET_FILTER;
		p.write("      <div class=\"", CssClass.row.toString(), "\" style=\"margin:5px;\">");
		p.write("        <div class=\"", CssClass.colSm12.toString(), "\">");
		p.write("          <div class=\"", CssClass.row.toString(), "\">");
		p.write("            <div class=\"", CssClass.colSm12.toString(), "\">");
		p.write("              ", p.getImg("class=\"", CssClass.popUpButton.toString(), "\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_FILTER_HELP, "\" border=\"0\" alt=\"?\" align=\"top\" onclick=\"javascript:void(window.open('helpSearch_", app.getCurrentLocaleAsString(), ".html', 'Help', 'fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=yes,resizable=yes,directories=no,location=no,width=400'));\""));    		        	
		p.write("              &nbsp;&nbsp;&nbsp;&nbsp;");
		p.write("              <button class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), " ", CssClass.btnSm.toString(), "\" onclick=\"javascript:var searchForm=document.forms['", formId, "'];var params=Form.serialize(searchForm);", updateTabScriptletPre, p.getEvalHRef(columnFilterAddAction), ", data: params", updateTabScriptletPost, "\">", texts.getSearchText(), "</button>");
		p.write("              <button type=\"submit\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), " ", CssClass.btnSm.toString(), "\" onclick=\"javascript:$('", resetFilterFieldId, "').value='true';var searchForm=document.forms['", formId, "'];var params=Form.serialize(searchForm);", updateTabScriptletPre, p.getEvalHRef(columnFilterAddAction), ", data: params", updateTabScriptletPost, "\"><img align=\"absmiddle\" border=\"0\" title=\"All\" src=\"./images/filter_all.gif\">&nbsp;", texts.getSearchText(), "</button>");
		p.write("              <button class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), " ", CssClass.btnSm.toString(), "\" onclick=\"javascript:$('", id, "_filterArea').style.display='none';return false;\">", texts.getCancelTitle(), "</button>");
		p.write("              &nbsp;&nbsp;&nbsp;&nbsp;");
		p.write("              <input type=\"hidden\" id=\"", showSearchFormFieldId, "\" name=\"", WebKeys.REQUEST_PARAMETER_SHOW_SEARCH_FORM, "\" value=\"", (showSearchFormOnInit ? "true" : "false"), "\" />");
		p.write("              <input type=\"hidden\" id=\"", resetFilterFieldId, "\" name=\"", WebKeys.REQUEST_PARAMETER_RESET_FILTER, "\" value=\"false\"/>");
		p.write("              <span><input type=\"checkbox\" ", (showSearchFormOnInit ? "checked" : ""), " style=\"padding:0px;vertical-align:middle;\" onclick=\"javascript:if(this.checked){$('", showSearchFormFieldId, "').value='true'}else{$('", showSearchFormFieldId, "').value='false'};\" /><small style=\"vertical-align:middle;\">&nbsp;", texts.getShowTitle(), "</small></span>");
		p.write("            </div");
		p.write("          </div>");
		p.write("        </div>");
		p.write("      </div>");
		p.write("    </div>");
    	p.write("    </fieldset>");
    	p.write("  </form>");
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.component.ReferencePane#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean, java.util.List)
     */
    @Override
    public void paint(
    	ViewPort p,
    	String frame,
    	boolean forEditing,
    	List<String> grids
    ) throws ServiceException {
    	SysLog.detail("> paint");
    	ShowObjectView view = (ShowObjectView)p.getView();
    	ApplicationContext app = view.getApplicationContext();
    	ReferencePaneControl control = this.getReferencePaneControl();
    	int paneIndex = this.getPaneIndex();
    	Texts_1_0 texts = app.getTexts();
    	HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
    	String gridContentId = view.getContainerElementId() == null 
    		? "G_" + Integer.toString(paneIndex) 
    		: view.getContainerElementId();
    		// View
		int zIndex = (MAX_REFERENCE_PANE - paneIndex) * 10;
		if(ReferencePaneControl.FRAME_VIEW.equals(frame)) {
			int nReferences = this.getSelectReferenceAction().length;
			if(nReferences > 0) {
				p.write("<ul class=\"", CssClass.nav.toString(), " ", CssClass.navTabs.toString(), " ", CssClass.navCondensed.toString(), "\" style=\"position:relative;z-index:", Integer.toString(zIndex+1), ";\">");
				boolean isGroupTabActive = false;
				boolean isFirstGroupTab = true;
				int selectedReference = -1;
				for(int i = 0; i < nReferences; i++) {
					Action action = this.getSelectReferenceAction()[i];
					UiGridControl gridControl = control.getChildren(UiGridControl.class).get(i);
					boolean isRevokeShow = app.getPortalExtension().hasPermission(
						gridControl.getQualifiedReferenceTypeName(), 
						view.getObject(), 
						app,
						WebKeys.PERMISSION_REVOKE_SHOW
					);	            	
					if(
						!isRevokeShow && 
						(grids == null || grids.contains(gridControl.getObjectContainer().getReferenceName()))
					) {
						if(i == this.getSelectedReference()) {
							selectedReference = i;
						}
						String tabLabel = action.getTitle();
						boolean isGroupTab = tabLabel.startsWith(WebKeys.TAB_GROUPING_CHARACTER);
						if(isGroupTab) tabLabel = tabLabel.substring(1);
						String encodedTabLabel = htmlEncoder.encode(tabLabel, false);
						String encodedTabTitle = action.getToolTip() == null 
							? null 
							: htmlEncoder.encode(
								action.getToolTip().startsWith(WebKeys.TAB_GROUPING_CHARACTER) ? action.getToolTip().substring(1) : action.getToolTip(), 
								false
							  );
						if(!isGroupTabActive && isGroupTab) {
							isGroupTabActive = true;
							if(isFirstGroupTab) {
								isFirstGroupTab = false;
							}
							p.write("  <li class=\"", CssClass.hiddenPrint.toString(), "\"><a href=\"#\" onclick=\"javascript:gTabSelect(this, true);return false;\">", WebKeys.TAB_GROUPING_CHARACTER, "</a></li>");
						}
						if(isGroupTabActive && (!isGroupTab || (i == nReferences-1))) {
							isGroupTabActive = false;
						}
						String tabClass = i == this.getSelectedReference() 
							? CssClass.active.toString() 
							: isGroupTabActive 
								? CssClass.hidden.toString() 
								: CssClass.hiddenPrint.toString();
						p.write("  <li class=\"", tabClass, "\"><a href=\"#\" onclick=\"javascript:gTabSelect(this);jQuery.ajax({type: 'get', url: ", p.getEvalHRef(action), ", dataType: 'html', success: function(data){$('", gridContentId, "').innerHTML=data;evalScripts(data);}});return false;\" title=\"", (encodedTabTitle == null ? "" : encodedTabTitle), "\">", encodedTabLabel, "</a></li>");
					}
				}
				p.write("</ul>");
				if(selectedReference >= 0) {
					p.write("<div id=\"", gridContentId, "\" class=\"", CssClass.gContent.toString(), "\" style=\"position:relative;z-index:", Integer.toString(zIndex), ";\">");
					p.write("  <div class=\"", CssClass.loading.toString(), "\" style=\"height:40px;\"></div>");
					p.write("</div>");
					Action selectReferenceTabAction = this.getSelectReferenceAction()[selectedReference];
					p.write("<script language=\"javascript\" type=\"text/javascript\">");
		            p.write("    jQuery.ajax({type: 'get', url: ", p.getEvalHRef(selectReferenceTabAction), ", dataType: 'html', success: function(data){$('", gridContentId, "').innerHTML=data;evalScripts(data);}});");
		            p.write("</script>");
				}
			}
		} else if(ReferencePaneControl.FRAME_CONTENT.equals(frame)) {
			//
			// Content
			//
			UiGrid grid = this.getGrid();
			int tabIndex = paneIndex * 100 + this.getSelectedReference();
			String tabId = gridContentId + "_" + Integer.toString(tabIndex);
			String gridMenuId = tabId + "_gridMenu";
			String updateTabScriptletPre = null;
			String updateTabScriptletPost = null;
			updateTabScriptletPre = "jQuery.ajax({type: 'post', url: "; 
			updateTabScriptletPost = ", dataType: 'html', success: function(data){$('" + gridContentId + "').innerHTML=data;evalScripts(data);}});loadingIndicator($('" + gridContentId + "'));return false;";
			boolean isRevokeEdit = app.getPortalExtension().hasPermission(
				grid.getQualifiedReferenceTypeName(), 
				view.getObject(), 
				app,
				WebKeys.PERMISSION_REVOKE_EDIT
			);
			Action selectGridTabAction = this.getSelectReferenceActions().get(this.getSelectedReference());
			if(grid != null) {                
				// Get grid rows. This also brings all grid properties to a consistent state
				SysLog.detail("grid rows");
				// Rows are retrieved by separate persistence manager. This 
				// allows to release pm and row objects when we leave this scope
				PersistenceManager pm = app.getNewPmData();
				List<UiGrid.GridRow> rows = grid.getRows(pm);                            
				// Paging actions and indicators
				Action firstPageAction = grid.getFirstPageAction();
				boolean pageNextIsEnabled = grid.getCurrentPage() < grid.getLastPage();
				boolean pagePreviousIsEnabled = grid.getCurrentPage() > 0;
				Action pageNextAction = grid.getPageNextAction(pageNextIsEnabled);
				Action pageNextFastAction = grid.getPageNextFastAction(pageNextIsEnabled); 
				Action pagePreviousAction = grid.getPagePreviousAction(pagePreviousIsEnabled);
				Action pagePreviousFastAction = grid.getPagePreviousFastAction(pagePreviousIsEnabled); 
				org.openmdx.portal.servlet.Filter[] filters = grid.getFilters();
				// Sortable columns
				List<Action> sortableColumns = new ArrayList<Action>();
				if(grid.isComposite()) {
					for(Action action: grid.getColumnOrderActions()) {
						if((action.getEvent() != Action.EVENT_NONE) && !action.getParameter(Action.PARAMETER_NAME).equals("identity")) {
							sortableColumns.add(action);
						}
					}
				}                
				// Searchable columns
				List<Action> searchableColumns = new ArrayList<Action>();
				if(grid.isComposite()) {
					// Add action if identity is searchable 
					if(
						!grid.isReferenceStoredAsAttribute() &&
						app.getPortalExtension().getQuery(grid.getReferencedTypeName() + ":" + SystemAttributes.OBJECT_IDENTITY, "", 0, app) != null
					) {
						Action identitySearchAction = new Action(
							UiGridAddColumnFilterAction.EVENT_ID,
							new Action.Parameter[]{
								new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(paneIndex)),
								new Action.Parameter(Action.PARAMETER_REFERENCE, grid.getControl().getId()),       
								new Action.Parameter(Action.PARAMETER_NAME, SystemAttributes.OBJECT_IDENTITY)
							},
							app.getLabel(grid.getReferencedTypeName()),
							app.getTextsFactory().getTextsBundle(app.getCurrentLocaleAsString()).getSearchIncrementallyText(),
							WebKeys.ICON_SEARCH_INC,
							true
						);
						searchableColumns.add(identitySearchAction);
					}
					for(Action action: grid.getColumnSearchActions()) {
						if((action.getEvent() != Action.EVENT_NONE) && !action.getParameter(Action.PARAMETER_NAME).equals("identity")) {
							searchableColumns.add(action);
						}
					}
				}
				// Non-composite, multi-valued reference                
				if(!grid.isComposite()) {
					// Grid operations only if changeable and not embedded and view is not read-only
					if(
						!isRevokeEdit && 
						grid.isChangeable() && 
						(view.getContainerElementId() == null) && 
						!Boolean.TRUE.equals(view.isReadOnly())
					) {                    
						Action addObjectAction = grid.getAddObjectAction();
						Action removeObjectAction = grid.getRemoveObjectAction();
						Action moveUpObjectAction = grid.getMoveUpObjectAction();
						Action moveDownObjectAction = grid.getMoveDownObjectAction();
						String lookupId = org.openmdx.kernel.id.UUIDs.newUUID().toString();
						String adderFieldId = "addObject[" + tabId + "]";
						ObjectReference objectReference = view.getObjectReference();
						Autocompleter_1_0 autocompleter = app.getPortalExtension().getAutocompleter(
							app,
							objectReference.getObject(),
							addObjectAction.getParameter(Action.PARAMETER_REFERENCE),
							null
						);
						p.write("<div id=\"", gridMenuId, "\" class=\"", CssClass.menuOpPanel.toString(), " ", CssClass.hiddenPrint.toString(), "\">");
						p.write("  <table cellspacing=\"0\" cellpadding=\"0\" id=\"menuOp\" width=\"100%\">");
						p.write("    <tr>");
						p.write("      <td>");
						p.write("        <div id=\"", tabId, "_gridButtons\">");
						p.write("          <ul id=\"", CssClass.ssfNav.toString(), "\" class=\"", CssClass.ssfNav.toString(), "\" onmouseover=\"sfinit(this);\" >");
						p.write("            <li><a href=\"#\" onclick=\"javascript:return false;\">", htmlEncoder.encode(texts.getEditTitle(), false), "&nbsp;&nbsp;&nbsp;</a>");
						p.write("              <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
						p.write("                <li><a href=\"#\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(addObjectAction), "+'&amp;", WebKeys.REQUEST_PARAMETER_LIST, "=", Action.PARAMETER_OBJECTXRI, "*('+encodeURIComponent($F('", adderFieldId, "'))+')'", updateTabScriptletPost, "\">", htmlEncoder.encode(texts.getAddObjectTitle(), false), "</a></li>");
						p.write("                <li><a href=\"#\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(removeObjectAction), "+'&amp;", WebKeys.REQUEST_PARAMETER_LIST, "='+encodeURIComponent(getSelectedGridRows('" + tabId, "_gridTable',1))", updateTabScriptletPost, "\">", htmlEncoder.encode(texts.getRemoveObjectTitle(), false), "</a></li>");
						p.write("                <li><a href=\"#\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(moveUpObjectAction), "+'&amp;", WebKeys.REQUEST_PARAMETER_LIST, "='+encodeURIComponent(getSelectedGridRows('" + tabId, "_gridTable',1))", updateTabScriptletPost, "\">", htmlEncoder.encode(texts.getMoveUpObjectTitle(), false), "</a></li>");
						p.write("                <li><a href=\"#\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(moveDownObjectAction), "+'&amp;", WebKeys.REQUEST_PARAMETER_LIST, "='+encodeURIComponent(getSelectedGridRows('" + tabId, "_gridTable',1))", updateTabScriptletPost, "\">", htmlEncoder.encode(texts.getMoveDownObjectTitle(), false), "</a></li>");
						p.write("              </ul>");
						p.write("            </li>");
						p.write("          </ul>");
						p.write("        </div>");
						p.write("      </td>");
						p.write("    </tr>");
						p.write("  </table>");
						p.write("</div>");
						p.write("<table class=\"", CssClass.lookupTable.toString(), "\">");
						p.write("  <tr>");
						p.write("    <td class=\"", CssClass.lookupInput.toString(), "\">");
						if(pageNextIsEnabled || pagePreviousIsEnabled) {
							p.write("      ", p.getImg("src=\"", p.getResourcePath("images/"), "spacer", p.getImgType(), " width=\"28\" border=\"0\" align=\"bottom\" alt=\"\""));
						}
						Action findObjectAction = view.getFindObjectAction(
							addObjectAction.getParameter("reference"),
							lookupId
							);
						CharSequence imgTag = p.getImg("class=\"", CssClass.popUpButton.toString(), "\" border=\"0\" align=\"bottom\" alt=\"Click to open ObjectFinder\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_LOOKUP_AUTOCOMPLETE_GRID, "\" onclick=\"OF.findObject(", p.getEvalHRef(findObjectAction), ", $('", adderFieldId, ".Title'), $('", adderFieldId, "'), '", lookupId, "');\"");
						if(autocompleter == null) {
							p.write("      <input id=\"", adderFieldId, ".Title\" type=\"text\" name=\"", adderFieldId, ".Title\" value=\"\" />");
							p.write("      <input type=\"hidden\" id=\"", adderFieldId, "\" name=\"", adderFieldId, "\" value=\"\" />");
							p.write("      ", imgTag);
						} else {
							autocompleter.paint(                    
								p,
								adderFieldId,
								tabIndex,
								"addObject",
								null,
								false,
								"<td class=\"" + CssClass.lookupButtons + "\">",
								null,
								"class=\"" + CssClass.valueAC + "\"",
								imgTag,
								null // onChangeValueScript
							);
						}
						p.write("    </td>");
						p.write("  </tr>");
						p.write("</table>");
					} else {
						p.write("<div class=\"", CssClass.gridSpacerTop.toString(), "\"></div>");                        
					}
				} else {
					//
					// Composite objects
					//
					p.write("<div id=\"", gridMenuId, "" , "\" class=\"", CssClass.hiddenPrint.toString(), "\">");
					p.write("  <table class=\"", CssClass.table.toString(), " ", CssClass.tableCondensed.toString(), "\" cellspacing=\"0\" cellpadding=\"0\" style=\"width:100%;border-spacing:0;\">");
					p.write("    <tr>");
					//
					// Navigation
					//
					p.write("      <td class=\"", CssClass.menuOpPanelActions.toString(), " ", CssClass.colXs3.toString(), "\" style=\"border-bottom:0px;\">");
					p.write("        <ul class=\"", CssClass.nav.toString(), " ", CssClass.navPills.toString(), "\">");
					// Page 0
					p.write("        <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(firstPageAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), firstPageAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\"|<\""), "</a></li>");
					// Page previous
					if(pagePreviousIsEnabled) {
						p.write("        <li class=\"", CssClass.hiddenXs.toString(), "\"><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(pagePreviousFastAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousFastAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\"<<\""), "</a></li>");
						p.write("        <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(pagePreviousAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\"<\""), "</a></li>");
					} else {
						p.write("        <li class=\"", CssClass.hiddenXs.toString(), "\" ><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousFastAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\"<<\""), "</a></li>");
						p.write("        <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\"<\""), "</a></li>");
					}
					// Page next
					if(pageNextIsEnabled) {
						p.write("        <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(pageNextAction), " + '&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pageNextAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\">\""), "</a></li>");
						p.write("        <li class=\"", CssClass.hiddenXs.toString(), "\" ><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(pageNextFastAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pageNextFastAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\">>\""), "</a></li>");
					} else {
						p.write("        <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pageNextAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\">\""), "</a></li>");
						p.write("        <li class=\"", CssClass.hiddenXs.toString(), "\" ><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" >", p.getImg("src=\"", p.getResourcePath("images/"), pageNextFastAction.getIconKey(), "\" border=\"0\" align=\"top\" alt=\">>\""), "</a></li>");
					}
					// Page size
					p.write("        <input style=\"margin:0px;\" type=\"hidden\" name=\"pagesize\" id=\"pagesize", tabId, "\" size=2 value=\"", Integer.toString(grid.getPageSize()), "\"/>");
					// Show default filter button in case content is hidden
					if((filters.length > 1) && !grid.getShowRows()) {
						org.openmdx.portal.servlet.Filter filter = filters[0];
						Action action = grid.getSelectFilterAction(filter);
						p.write("        <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(action), updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_SHOW_GRID_CONTENT, "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"", htmlEncoder.encode(action.getTitle(), false), "\""), "</a></li>");
					}
					// Show/Hide search panel
					if(!searchableColumns.isEmpty()) {
						Action firstSearchableColumn = null;
						for(int i = 0; i < searchableColumns.size(); i++) {
							Action action = searchableColumns.get(i);
							if(action.getTitle() != null && !action.getTitle().trim().isEmpty()) {
								firstSearchableColumn = action;
								break;
							}
						}
						if(firstSearchableColumn == null) {
							p.write("        <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:ft=$('", tabId, "_filterArea');if(ft.style.display!='block'){ft.style.display='block';}else{ft.style.display='none';};return false;\">", p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_SEARCH_PANEL, "\" border=\"0\" align=\"bottom\" alt=\"v\""), "</a></li>");	                    	
						} else {
							boolean hilite = grid.hasFilterValues();
							p.write("        <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:ft=$('", tabId, "_filterArea');if(ft.style.display!='block'){ft.style.display='block';try{$('", this.getSearchFormFieldId(tabId, firstSearchableColumn), "').focus();}catch(e){};}else{ft.style.display='none';};return false;\">", p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_SEARCH_PANEL, "\" ", (hilite ? " class=\"" + CssClass.hilite + "\"" : ""), " border=\"0\" align=\"bottom\" alt=\"v\""), "</a></li>");
						}
					}
					p.write("       </ul>");
					p.write("      </td>");
					//
					// Default filter functions
					//
					p.write("      <td class=\"", CssClass.filterButtons.toString(), " ", CssClass.colXs2.toString(), " ", CssClass.colSm2.toString(), " ", CssClass.colMd3.toString(), "\" style=\"border-bottom:0px;\">");
					p.write("        <ul class=\"", CssClass.nav.toString(), " ", CssClass.navPills.toString(), "\" >");              
					this.paintFilterMenus(
						p, 
						grid, 
						filters, 
						updateTabScriptletPre, 
						updateTabScriptletPost,
						""
					);
					// Filter actions
					if(!Boolean.TRUE.equals(view.isReadOnly())) {
						Action setCurrentFilterAsDefaultAction = grid.getSetCurrentFilterAsDefaultAction();
						Action setDefaultFilterOnInitAction = grid.getSetShowGridContentOnInitAction();
						p.write("          <li class=\"", CssClass.hiddenXs.toString(), " ", CssClass.hiddenSm.toString(),  "\"><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(setCurrentFilterAsDefaultAction), updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), setCurrentFilterAsDefaultAction.getIconKey(), "\" border=\"0\" align=\"absmiddle\" alt=\"o\" title=\"", htmlEncoder.encode(setCurrentFilterAsDefaultAction.getTitle(), false), "\""), "</a></li>");
						p.write("          <li class=\"", CssClass.hiddenXs.toString(), " ", CssClass.hiddenSm.toString(),  "\"><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(setDefaultFilterOnInitAction), updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), setDefaultFilterOnInitAction.getIconKey(), "\" border=\"0\" align=\"absmiddle\" alt=\"v\" title=\"", htmlEncoder.encode(setDefaultFilterOnInitAction.getTitle(), false), "\""), "</a></li>");
					}
					p.write("        </ul>");
					p.write("      </td>");
					//
					// Menu
					//
					p.write("      <td class=\"", CssClass.filterButtons.toString(), " ", CssClass.hiddenXs.toString(), " ", CssClass.colSm2.toString(), " ", CssClass.colMd3.toString(), "\" style=\"border-bottom:0px;\">");					
					p.write("        <ul class=\"", CssClass.nav.toString(), " ", CssClass.navPills.toString(), "\">");
					Action multiDeleteAction = grid.getMultiDeleteAction();
					if(
						(grid.getObjectCreator() != null) &&
						(grid.getObjectCreator().length > 0) &&
						!isRevokeEdit && 
						!Boolean.TRUE.equals(view.isReadOnly())
					) {
						//
						// Menu New
						//
						p.write("              <li onclick=\"javascript:toggleMenu(this);\"><a href=\"#!\" class=\"", CssClass.dropdownToggle.toString(), "\" style=\"", STYLE_GRID_MENU_SMALL, "\">", htmlEncoder.encode(texts.getNewText(), false), "</a>");
						p.write("                <ul class=\"", CssClass.dropdownMenu.toString(), "\" role=\"menu\">");
						for(int j = 0; j < grid.getObjectCreator().length; j++) {
							Action action = grid.getObjectCreator()[j];
							p.write("                  <li><a href=\"#\" onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\">", p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""), "&nbsp;&nbsp;", htmlEncoder.encode(action.getTitle(), false), "</a></li>");
						}                
						p.write("                </ul>");
						p.write("              </li>");
						//
						// Menu Edit
						//
						p.write("              <li class=\"", CssClass.hiddenSm.toString(), "\" onclick=\"javascript:toggleMenu(this);\"><a href=\"#!\" class=\"", CssClass.dropdownToggle.toString(), "\" style=\"", STYLE_GRID_MENU_SMALL, "\">", htmlEncoder.encode(texts.getEditTitle(), false), "</a>");
						p.write("                <ul class=\"", CssClass.dropdownMenu.toString(), "\" role=\"menu\">");
						// Multi-delete
						if(this.getIsMultiDeleteEnabled() && (multiDeleteAction != null)) {
							p.write("                  <li><a href=\"#\" onclick=\"javascript:var para=getSelectedGridRows('", tabId, "_gridTable',1);if(para.length>1){$('parameter.list", tabId, "').value=para;};$('event.submit", tabId, "').value='", Integer.toString(multiDeleteAction.getEvent()), "';document.showForm", tabId, ".submit();\">", htmlEncoder.encode(multiDeleteAction.getTitle(), false), "</a></li>");
						} else {
							p.write("                  <li><a href=\"#\" onclick=\"javascript:;\"><span>", htmlEncoder.encode(texts.getDeleteTitle(), false), "</span></a></li>");                    
						}
						p.write("                </ul>");
						p.write("              </li>");
					}
					// Grid actions
					{
						List<Action> gridActions = app.getPortalExtension().getGridActions(view, grid);
						if(gridActions != null && !gridActions.isEmpty()) {
							p.write("              <li class=\"", CssClass.hiddenSm.toString(), "\" onclick=\"javascript:toggleMenu(this);\"><a href=\"#!\" class=\"", CssClass.dropdownToggle.toString(), "\" style=\"", STYLE_GRID_MENU_SMALL, "\">", htmlEncoder.encode(texts.getActionsTitle(), false), "</a>");
							p.write("                <ul class=\"", CssClass.dropdownMenu.toString(), "\" role=\"menu\">");
							for(Action action: gridActions) {
								p.write("                  <li><a href=\"#\" onclick=\"javascript:var para=getSelectedGridRows('", tabId, "_gridTable',1);if(para.length>1){$('parameter.list", tabId, "').value=para;};$('event.submit", tabId, "').value='", Integer.toString(action.getEvent()), "';$('parameter.size", tabId, "').value='", action.getParameter(Action.PARAMETER_SIZE), "';document.showForm", tabId, ".submit();\">", htmlEncoder.encode(action.getTitle(), false), "</a></li>");		                        	
							}
							p.write("                </ul>");
							p.write("              </li>");	                    		
						}
					}
					//
					// Menu View
					//                        
					p.write("              <li onclick=\"javascript:toggleMenu(this);\"><a href=\"#!\" class=\"", CssClass.dropdownToggle.toString(), "\" style=\"", STYLE_GRID_MENU_SMALL, "\">", htmlEncoder.encode(texts.getViewTitle(), false), "</a>");
					p.write("                <ul class=\"", CssClass.dropdownMenu.toString(), "\" role=\"menu\">");
					for(int i = 0; i < DEFAULT_PAGE_SIZES.length; i++) {
						String pageSize = Integer.toString(DEFAULT_PAGE_SIZES[i]);
						String showPagesText = texts.getShowRowsText();;
						showPagesText = showPagesText.replaceAll("\\$\\{0\\}", pageSize);
						p.write("                  <li><a href=\"#\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(selectGridTabAction), "+'&amp;pagesize=", pageSize, "'", updateTabScriptletPost, "\">", htmlEncoder.encode(showPagesText, false), "</a></li>");
					}
					{
						// Column ordering
						List<ValuedField> orderedColumnDefs = new ArrayList<ValuedField>(grid.getColumnDefs());
						orderedColumnDefs.add(0, null);
						orderedColumnDefs = grid.sortByColumnOrdering(orderedColumnDefs);
						for(int i = 1; i < orderedColumnDefs.size() - 1; i++) {
							ValuedField fieldDef = orderedColumnDefs.get(i);
							if(fieldDef != null) {
								Action swapColumnOrderAction = grid.getGridControl().getSwapColumnOrderAction(i, i + 1);
								String columnLabel = app.getCurrentLocaleAsIndex() < fieldDef.getLabel().size()
									? fieldDef.getLabel().get(app.getCurrentLocaleAsIndex())
									: fieldDef.getLabel().get(0);
								p.write("<li><a class=\"", CssClass.hiddenXs.toString(), "\" href=\"#\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(swapColumnOrderAction), updateTabScriptletPost, "\">", columnLabel, "&nbsp;<span class=\"glyphicon glyphicon-chevron-down\"></span></a></li>");
							}
						}
					}
					{
						// Reset column ordering
						Action swapColumnOrderAction = grid.getGridControl().getSwapColumnOrderAction(0, 0);
						p.write("<li><a class=\"", CssClass.hiddenXs.toString(), "\" href=\"#\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(swapColumnOrderAction), updateTabScriptletPost, "\"><span class=\"glyphicon glyphicon-th\"></span>&nbsp;", texts.getResetTitle(), "</a></li>");
					}
					p.write("                </ul>");
					p.write("              </li>");
					p.write("          </ul>");
					//
					// Actions Form: multi-delete and user-defined actions
					//
					{
						p.write("        <form id=\"showForm", tabId, "\" action=\"", p.getResourcePath(WebKeys.SERVLET_NAME), "\" name=\"showForm", tabId, "\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"post\" style=\"padding:0px;\">");
						p.write("          <input type=\"hidden\" name=\"requestId.submit\" value=\"", view.getRequestId(), "\" />");
						p.write("          <input type=\"hidden\" name=\"", Action.PARAMETER_REFERENCE, "\" value=\"", Integer.toString(this.getSelectedReference()), "\" />");
						p.write("          <input type=\"hidden\" name=\"", Action.PARAMETER_PANE, "\" value=\"", Integer.toString(paneIndex), "\" />");
						p.write("          <input id=\"parameter.size", tabId, "\" type=\"hidden\" name=\"", Action.PARAMETER_SIZE, "\" value=\"\" />");
						p.write("          <input id=\"event.submit", tabId, "\" type=\"hidden\" name=\"event.submit\" value=\"\" />");
						p.write("          <input id=\"parameter.list", tabId, "\" type=\"hidden\" name=\"parameter.list\" value=\"\" />");
						p.write("        </form>");                
					}
					p.write("      </td>");
					// 
					// Info
					//
					p.write("      <td class=\"", CssClass.menuOpPanelInfo.toString(), " ", CssClass.colXs1.toString(), " ", CssClass.hiddenXs.toString(), "\" style=\"border-bottom:0px;vertical-align:middle;\">");
					if(grid.getCurrentPage() > 0 || rows.size() > 0) {
						int startRow = grid.getCurrentPage() * grid.getPageSize();
						p.write("        (", Integer.toString(startRow + 1), "-", Integer.toString(startRow + rows.size()), (grid.getTotalRows() == null ? "" : "/" + Integer.toString(grid.getTotalRows())), ")");
					}
					p.write("      </td>");
					// 
					// End of panel
					//
					p.write("    </tr>");
					p.write("  </table>");
					p.write("</div>");
				}
				p.write("<div id=\"", tabId, "_grid\" style=\"display: block;\">");
				//
				// Filter menues and search form
				//
				boolean showSearchFormOnInit = this.showSearchForm(grid, app);
				p.write("<div id=\"", tabId, "_filterArea\" style=\"display:", (showSearchFormOnInit ? "block" : "none"), ";\">");
				p.write("  <div id=\"", tabId, "_customFilterArea\"></div>");                
				if(filters.length > 1) {
					p.write("  <div id=\"", tabId, "_defaultFilterArea\">");                
					p.write("    <table id=\"", tabId, "_filterTable\" class=\"", CssClass.filterTable.toString(), "\">");
					p.write("      <tr>");
					p.write("        <td>");
					// only show filter values input field if there is at least one sortable column
					if(!searchableColumns.isEmpty()) {
						this.paintSearchForm(
							p, 
							grid, 
							tabId, 
							"", // form style
							"", // label style
							"font-size:15px;padding:5px 5px 5px 5px;", // button style 
							searchableColumns, 
							updateTabScriptletPre, 
							updateTabScriptletPost
						);
					}
					p.write("        </td>");
					p.write("      </tr>");
					p.write("    </table>");
					p.write("  </div>"); // defaultFilterArea
				}
				p.write("</div>"); // filterArea
				// Table header
				p.write("<div class=\"", CssClass.tableResponsive.toString(), "\">");
				p.write("<table class=\"", "", CssClass.table.toString(), " ", CssClass.tableHover.toString(), " ", CssClass.tableStriped.toString(), " ", CssClass.tableCondensed.toString(), "\" id=\"", tabId, "_gridTable\">");
				p.write("  <thead>");
				p.write("  <tr>");
				if(grid.showRowSelectors()) {
					p.write("<th></th>");
				}
				List<Action> columnActions = grid.getColumnOrderActions();
				for(int j = 0; j < grid.getShowMaxMember(); j++) {
					Action columnAction = columnActions.get(j);
					ValuedField columnDef = j > 0 ? grid.getColumnDefs().get(j - 1) : null;
					if(j == 0) {
						p.write("<th width=\"40px;\">");
						p.write("  <table class=\"", CssClass.filterHeader.toString(), " ", CssClass.hiddenPrint.toString(), "\"><tr><td>");
						p.write("    <input id=\"multiselect", tabId, "\" type=\"checkbox\" onclick=\"javascript:selectGridRows('", tabId, "_gridTable',1, this.checked);\"/>");
						p.write("  </td></tr></table>");
						p.write("</th>");
					} else {
						CharSequence columnTitle = htmlEncoder.encode(columnAction.getTitle(), false).trim();
						if(columnTitle.length() == 0) {
							columnTitle = p.getImg("src=\"", p.getResourcePath("images/") + columnAction.getIconKey() + "\" border=\"0\" align=\"middle\" alt=\"o\" title=\"\"");
						}
						// column ordering 
						Action togglingColumnOrderAction = grid.getTogglingColumnOrderAction(
							columnAction.getParameter(Action.PARAMETER_NAME).toString()
						);
						p.write("<th>");
						p.write("  <table class=\"" + CssClass.filterHeader.toString(), "\">");
						p.write("    <tr>");
						// Ordering
						if(!grid.isComposite() || togglingColumnOrderAction.getEvent() == Action.EVENT_NONE) {
							String cssClass = "";
							if(columnDef != null && columnDef.getCssClassObjectContainer() != null) {
								cssClass += " " + columnDef.getCssClassObjectContainer();
								cssClass = cssClass.trim();
							}
							p.write("<td><div class=\"", cssClass, "\">", columnTitle, "</div></td>");
						} else {
							String cssClass = CssClass.filterCell.toString();
							String cssClassHover = CssClass.filterCell.toString() + "hover";
							if(columnDef != null && columnDef.getCssClassObjectContainer() != null) {
								cssClass += " " + columnDef.getCssClassObjectContainer();
								cssClass = cssClass.trim();
								cssClassHover += " " + columnDef.getCssClassObjectContainer();
								cssClassHover = cssClassHover.trim();
							}
							String iconKey = togglingColumnOrderAction.getIconKey();
							if(iconKey.startsWith(WebKeys.ICON_SORT_ANY)) {
								iconKey = "spacer" + p.getImgType();
							}
							p.write("<td class=\"", cssClass, "\" title=\"", htmlEncoder.encode(togglingColumnOrderAction.getToolTip(), false), "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(togglingColumnOrderAction), updateTabScriptletPost, ";\"", p.getOnMouseOver("javascript:this.className='", cssClassHover, "';"), p.getOnMouseOut("javascript:this.className='" + cssClass + "';"), "><div>", columnTitle, "</div></td>");
							p.write("<td>",p.getImg("src=\"", p.getResourcePath("images/"), iconKey, "\" title=\"", htmlEncoder.encode(togglingColumnOrderAction.getTitle(), false), "\" align=\"bottom\" alt=\"\""), "</td>");
						}
						p.write("    </tr>");
						p.write("  </table>");
						p.write("</th>");
					}
				}
				if(grid.showRowSelectors()) {
					p.write("<th></th>");
				}
				p.write("</tr>");
				p.write("</thead>");
				// Show grid
				for(int j = 0; j < rows.size(); j++) {
					long rowNumber = UiReferencePane.currentRowId++;
					String rowId = tabId + "_row" + rowNumber;                    
					List<Object> cells = rows.get(j).getCells();
					ObjectReference objRow = (ObjectReference)((AttributeValue)cells.get(0)).getValue(true);
					// row color
					String[] gridRowColors = app.getPortalExtension().getGridRowColors(objRow.getObject());
					String rowColor = gridRowColors == null ? null : gridRowColors[0];
					String rowBackColor = gridRowColors == null ? null : gridRowColors[1];
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
					p.write("<tr ", styleModifier, " onclick=\"javascript:selectGridRow(this);\" >");
					if((objRow != null) && grid.showRowSelectors()) {
						if((view.getLookupType() != null) && objRow.isInstanceof(view.getLookupType())) {
							p.write("<td class=\"", CssClass.gridColTypeCheck.toString(), "\"><span class=\"", CssClass.lookupSelector.toString(), "\"><input type=\"checkbox\" name=\"objselect\" value=\"\" onclick=\"OF.selectAndClose('", htmlEncoder.encode(objRow.getXRI(), false), "', '", htmlEncoder.encode(objRow.getTitleAsJavascriptArg(), false), "', '", view.getId(), "', window);\" /></span></td>");
						} else {
							p.write("<td class=\"", CssClass.gridColTypeCheck.toString(), "\"></td>");
						}
					}                    
					for(int k = 0; k < grid.getShowMaxMember(); k++) {
						boolean nowrap = true;
						AttributeValue valueHolder = (AttributeValue)cells.get(k);
						// cssClass
						String cssClass = app.getPortalExtension().getDefaultCssClassObjectContainer(valueHolder, app);
						if(valueHolder.getCssClassObjectContainer() != null) {
							cssClass = valueHolder.getCssClassObjectContainer();
						}
						Object value = valueHolder.getValue(
							// default is short-text
							cssClass == null || !cssClass.contains(CssClass.longText.toString())
						);
						String stringifiedValue = valueHolder.getStringifiedValue(
							p,
							true, 
							false,
							// default is short-text
							cssClass == null || !cssClass.contains(CssClass.longText.toString())
						);
						stringifiedValue = valueHolder instanceof TextValue 
							? ((TextValue)valueHolder).isPassword() ? "*****" : stringifiedValue 
							: stringifiedValue;
						// iconTag                        
						CharSequence iconTag = "";
						CharSequence iconSpacer = "";
						CharSequence divBegin = "";
						CharSequence divEnd = "";
						if(valueHolder.getIconKey() != null) {
							iconTag = p.getImg("src=\"", p.getResourcePath("images/"), valueHolder.getIconKey(), "\" align=\"absbottom\" border=\"0\" alt=\"\"");
							// Spacer only if icon and text
							if(!stringifiedValue.isEmpty()) {
								divBegin = "<div>";
								divEnd = "</div>";
								iconSpacer = p.getImg("src=\"", p.getResourcePath("images/"), "spacer", p.getImgType(), "\" width=\"5\" height=\"0\" align=\"middle\" border=\"0\" alt=\"\"");
							}
						}
						// Null or empty collection
						if(stringifiedValue.length() == 0) {
							stringifiedValue = "&nbsp;";
						}                        
						if(value == null) {
							p.debug("<!-- null -->");
							p.write("<td class=\"", cssClass, "\">", divBegin, iconTag, iconSpacer, stringifiedValue, divEnd, "</td>");
						} else if(valueHolder instanceof BooleanValue) {
							// Boolean
							String images = "";
							if(value instanceof Collection) {
								for(Iterator<?> e = ((Collection<?>)value).iterator(); e.hasNext(); ) {
									if(Boolean.TRUE.equals(e.next())) {
										images += p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_CHECKED_R, "\" alt=\"checked\"");
									} else {
										images += p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_NOT_CHECKED_R, "\" alt=\"not checked\"");
									}
								}
							} else {
								if(Boolean.TRUE.equals(value)) {
									images += p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_CHECKED_R, "\" alt=\"checked\"");
								} else {
									images += p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_NOT_CHECKED_R, "\" alt=\"not checked\"");
								}
							}
							p.debug("<!-- BooleanValue -->");
							p.write("<td ", (nowrap ? "nowrap" : ""), " class=\"", cssClass, "\">", images, "</td>");
						} else if(valueHolder instanceof ObjectReferenceValue) {
							// Object reference
							// Multi-valued
							if(value instanceof Collection) {
								int maxLenTitle = 0;
								List<Action> objRefActions = new ArrayList<Action>();
								List<String> objRefTitles = new ArrayList<String>();
								List<String> objRefToolTips = new ArrayList<String>();
								Iterator<?> e = ((Collection<?>)value).iterator();
								while(e.hasNext()) {
									try {
										ObjectReference objRef = (ObjectReference)e.next();
										Action action = objRef.getSelectObjectAction();
										String title = action.getTitle(); // is HTML-formatted
										String toolTip = "";
										if(title.startsWith(ObjectReference.TITLE_PREFIX_NO_PERMISSION)) {
											toolTip = title;                                     
											title = title.substring(0, ObjectReference.TITLE_PREFIX_NO_PERMISSION.length());
										} else if(title.startsWith(ObjectReference.TITLE_PREFIX_NOT_ACCESSIBLE)) {
											toolTip = title;                                     
											title = title.substring(0, ObjectReference.TITLE_PREFIX_NOT_ACCESSIBLE.length());
										}
										objRefActions.add(action);
										objRefTitles.add(title);
										objRefToolTips.add(toolTip);
										if(title.length() > maxLenTitle) {
											maxLenTitle = title.length();
										}
									} catch(Exception e0) {
										ServiceException e1 = new ServiceException(e0);
										List<?> params = Arrays.asList(
											objRow.getXRI(), 
											valueHolder.getFieldDef().featureName, 
											app.getLoginPrincipal(),
											e0.getMessage()
										);
										if(e1.getExceptionCode() == BasicException.Code.AUTHORIZATION_FAILURE) {
											SysLog.detail("Unable to retrieve object", params);
										} else {
											SysLog.warning("Unable to retrieve object (more info at detail level)", params);                                        	
										}
										SysLog.detail(e1.getMessage(), e1.getCause());
									}
								}
								p.write("<!-- ObjectReferenceValue -->"); // used as tag for multi-delete. Do not write as debug!
								p.write("<td ", (nowrap && maxLenTitle < NO_WRAP_THRESHOLD ? "nowrap" : ""), " class=\"", cssClass, "\">");
								for(int i = 0; i < objRefActions.size(); i++) {
									if(i > 0) {
										p.write("<br />");                                    
									}
									p.write("<a href=\"\" onmouseover=\"javascript:this.href=", p.getEvalHRef(objRefActions.get(i)), ";onmouseover=function(){};\" title=\"", htmlEncoder.encode(objRefToolTips.get(i), false), "\">", objRefTitles.get(i), "</a>");									
								}
								p.write("</td>");
							} else {
								// Single-valued
								ObjectReference objRef = (ObjectReference)value;
								Action action = objRef.getSelectObjectAction(
									new Action.Parameter(
										Action.PARAMETER_ORIGIN,
										Integer.toString(paneIndex)
									)										
								);                        
								String navigationTarget = view.getNavigationTarget();
								// Image only in first column
								if(k == 0) {      
									// Expand rows modifier
									p.write("<!-- ObjectReferenceValue -->"); // used as tag for multi-delete. Do not write as debug!
									p.write("<td><table id=\"gm\"><tr><td><a href=\"#\"" + (navigationTarget != null && !"_none".equals(navigationTarget) ? "target=\"" + navigationTarget + "\"" : "") + " onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\"" + ("_none".equals(navigationTarget) ? " onclick=\"javascript:return false;\"" : "") + ">", p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""), "</a></td><td><div id=\"", rowId, "_menu\" class=\"gridMenu\" onclick=\"try{this.parentNode.parentNode.onclick();}catch(e){};\"><div class=\"gridMenuIndicator\" onclick=\"javascript:jQuery.ajax({type: 'get', url: ", p.getEvalHRef(this.getRowMenuAction(objRef.getXRI(), rowId)), ", dataType: 'html', success: function(data){$('", rowId, "_menu').innerHTML=data;evalScripts(data);}});\">", p.getImg("border=\"0\" height=\"16\" width=\"16\" alt=\"\" src=\"", p.getResourcePath("images/"), "spacer.gif\""), "</div>", p.getImg("border=\"0\" align=\"bottom\" alt=\"\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_MENU_DOWN, "\" style=\"display:none;\""), "</div></td></tr></table></td>");
								} else {									
									// only text in columns > 0
									p.write("<!-- ObjectReferenceValue -->");
									String title = action.getTitle();
									String toolTip = "";
									if(title.startsWith(ObjectReference.TITLE_PREFIX_NO_PERMISSION)) {
										toolTip = title;                                     
										title = title.substring(0, ObjectReference.TITLE_PREFIX_NO_PERMISSION.length());
									} else if(title.startsWith(ObjectReference.TITLE_PREFIX_NOT_ACCESSIBLE)) {
										toolTip = title;                                     
										title = title.substring(0, ObjectReference.TITLE_PREFIX_NOT_ACCESSIBLE.length());
									}
									// Remove html tags. They will be escaped by html encoder anyway
									while(title.startsWith("<") && title.indexOf("/>") > 0) {
										title = title.substring(title.indexOf("/>") + 2);
									}
									if(action.getEvent() == Action.EVENT_NONE) {
										p.write("<td ", (nowrap && title.length() < NO_WRAP_THRESHOLD ? "nowrap" : ""), " class=\"", cssClass, "\"><div title=\"", htmlEncoder.encode(toolTip, false), "\">");
										app.getPortalExtension().renderTextValue(
											p,
											null, // no attribute
											htmlEncoder.encode(title, false), 
											false
										);
										p.write("</div></td>");
									} else {
										p.write("<td ", (nowrap && title.length() < NO_WRAP_THRESHOLD ? "nowrap" : ""), " class=\"", cssClass, "\"><a href=\"#\"" + (navigationTarget != null && !"_none".equals(navigationTarget) ? "target=\"" + navigationTarget + "\"" : "") + " onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\"" + ("_none".equals(navigationTarget) ? " onclick=\"javascript:return false;\"" : "") + " title=\"", htmlEncoder.encode(toolTip, false), "\">");
										app.getPortalExtension().renderTextValue(
											p,
											valueHolder,
											htmlEncoder.encode(title, false), 
											false
										);
										p.write("</a></td>");
									}
								}
							}
						} else {
							// Other field types
							org.openmdx.ui1.jmi1.ValuedField columnDef = grid.getColumnDefs().get(k - 1);
							boolean containsWiki = HtmlEncoder.containsWiki(stringifiedValue);
							boolean containsHtml = HtmlEncoder.containsHtml(stringifiedValue);
							if(
								(columnDef instanceof org.openmdx.ui1.jmi1.TextField ||
									columnDef instanceof org.openmdx.ui1.jmi1.TextBox) &&
									containsWiki
							) {
								String id = tabId + "-" + rowId + "-" + k;                        	
								p.debug("<!-- AttributeValue -->");
								p.write("<td class=\"", cssClass, "\">", divBegin, iconTag, iconSpacer);
								p.write("<div id=\"", id, "Value\" style=\'display:none'>");
								app.getPortalExtension().renderTextValue(
									p, 
									valueHolder, 
									stringifiedValue, 
									true
								);
								p.write("</div>");
								p.write("<div id=\"", id, "\"><script language=\"javascript\" type=\"text/javascript\">try{var w=Wiky.toHtml($('", id, "Value').innerHTML);if(w.startsWith('<p>')){w=w.substring(3);};if(w.endsWith('</p>')){w=w.substring(0,w.length-4);};w=w.strip();$('", id, "').update(w);}catch(e){$('", id, "').update($('", id, "Value').innerHTML);};</script></div>");                            
								p.write(divEnd, "</td>");
							} else {
								p.debug("<!-- AttributeValue -->");
								p.write("<td ", (nowrap && stringifiedValue.length() < NO_WRAP_THRESHOLD ? "nowrap" : ""), " class=\"", cssClass, "\">", divBegin, iconTag, iconSpacer);
								app.getPortalExtension().renderTextValue(
									p,
									valueHolder,
									containsHtml ? stringifiedValue : stringifiedValue.replaceAll("\n", "<br />"), 
									false
								);
								p.write(divEnd, "</td>");                        		
							}
						}
					}
					if((objRow != null) && grid.showRowSelectors()) {
						if((view.getLookupType() != null) && objRow.isInstanceof(view.getLookupType())) {
							p.write("<td class=\"", CssClass.gridColTypeCheck.toString(), "\"><span class=\"", CssClass.lookupSelector.toString(), "\"><input type=\"checkbox\" name=\"objselect\" value=\"\" onclick=\"javascript:OF.selectAndClose('", htmlEncoder.encode(objRow.getXRI(), false), "', '", htmlEncoder.encode(objRow.getTitleAsJavascriptArg(), false), "', '", view.getId(), "', window);\" /></span></td>");
						} else {
							p.write("<td class=\"", CssClass.gridColTypeCheck.toString(), "\"></td>");
						}
					}
					p.write("</tr>");
					// Row for object details (handled by EVENT_GRID_SHOW_ROW_DETAILS)
					p.write("<tr>");
					p.write("  <td colspan=\"", Integer.toString(grid.getShowMaxMember()), "\" style=\"padding:0px;border-top:0px;border-bottom:0px;\"><table class=\"", CssClass.tablePanel.toString(), "\">");
					p.write("    <td class=\"", CssClass.gridCloser.toString(), "\"><img class=\"imgCloser\" id=\"", rowId, "_details-closer\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_CLOSE, "\" alt=\"\" style=\"display:none;\" onclick=\"javascript:$('", rowId, "_details').innerHTML='';this.style.display='none';\" /></td>");
					p.write("    <td width=\"100%\"><div id=\"", rowId, "_details\" /></td>");
					p.write("  </table></td>");
					p.write("</tr>");
				}
				p.write("</table>");
				p.write("</div>");
				// Page indicators only for composite grids and if view is not embedded to save space
				if(view.getContainerElementId() == null) {
					// Only show pagers if there are more than 10 rows 
					if(rows.size() > 10) {                    
						if(grid.getAddObjectAction() == null) { 
							p.write("        <ul class=\"", CssClass.nav.toString(), " ", CssClass.navPills.toString(), " ", CssClass.hiddenPrint.toString(), "\">");
							// Page 0
							p.write("          <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(firstPageAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), firstPageAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a></li>");
							// Page previous
							if(pagePreviousIsEnabled) {
								p.write("          <li class=\"", CssClass.hiddenXs.toString(), "\"><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(pagePreviousFastAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousFastAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a></li>");
								p.write("          <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(pagePreviousAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a></li>");
							} else {
								p.write("          <li class=\"", CssClass.hiddenXs.toString(), "\"><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousFastAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a></li>");
								p.write("          <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pagePreviousAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a></li>");
							}
							// Page next
							if(pageNextIsEnabled) {
								p.write("          <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(pageNextAction), " + '&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pageNextAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"v\""), "</a></li>");
								p.write("          <li class=\"", CssClass.hiddenXs.toString(), "\"><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\" onclick=\"javascript:", updateTabScriptletPre, p.getEvalHRef(pageNextFastAction), "+'&amp;pagesize='+encodeURIComponent($F('pagesize", tabId, "'))", updateTabScriptletPost, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pageNextFastAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a></li>");
							} else {
								p.write("          <li><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pageNextAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a></li>");
								p.write("          <li class=\"", CssClass.hiddenXs.toString(), "\"><a href=\"#\" style=\"", STYLE_GRID_MENU_SMALL, "\">", p.getImg("src=\"", p.getResourcePath("images/"), pageNextFastAction.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"^\""), "</a></li>");
							}
							p.write("        </ul>");
						}
					}
				} else {
					p.write("<div class=\"", CssClass.gridSpacerBottom.toString(), "\"></div>");                        
				}
				p.write("</div>");
				pm.close();                
			}
		}        
		SysLog.detail("< paint");
    }

	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
	private static final long serialVersionUID = 3112089394206823174L;

    // Unique number for each generated row allows to generate unique id tags 
    // (e.g. required for row menu)
    private static long currentRowId = 0L;

    public static final String STYLE_GRID_MENU_SMALL = "padding-left:7px;padding-right:7px;";
    public static final int NO_WRAP_THRESHOLD = 40;
    public static final int MAX_REFERENCE_PANE = 100;
    public static final String SEARCH_FORM_NAME = "searchForm";
    public static final int[] DEFAULT_PAGE_SIZES = new int[]{5, 10, 20, 50, 100, 200, 500};  

    protected List<UiGrid> grids;
    protected int selectedReference = -1;
    
}

//--- End of File -----------------------------------------------------------
