/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: DashboardControl.java,v 1.12 2009/10/24 15:28:07 wfro Exp $
 * Description: DashboardControl 
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/10/24 15:28:07 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.View;

/**
 * DashboardControl
 *
 */
public class DashboardControl
	extends Control 
	implements Serializable {
	
	//-----------------------------------------------------------------------
	public DashboardControl(
        String id,
        String locale,
        int localeAsIndex
	) {
		super(
			id,
			locale,
			localeAsIndex
		);
	}
	//-----------------------------------------------------------------------
	public static class DashletDescr {

		public DashletDescr(
			String id,
			String name,
			String width,
			String label,
			String orderX,
			String orderY
		) {
			this.id = id;
			this.name = name;
			this.width = width;
			this.label = label;
			this.orderX = orderX;
			this.orderY = orderY;			
		}

		public final String id;
		public final String name;
		public final String width;
		public final String label;
		public final String orderX;
		public final String orderY;
		
	}
	
	//-----------------------------------------------------------------------
	protected void getDashboard(
		String dashboardId,
		Properties settings,
		String dashletFilter,
		Map<String,Map<String,DashletDescr>> dashboard
	) {
		if(settings.getProperty(dashboardId + "." + DASHBOARD_PROPERTY_DASHLETS) != null) {
			String[] dashlets = settings.getProperty(dashboardId + "." + "dashlets").split(";");
			for(String dashlet: dashlets) {
				String orderX = settings.getProperty(dashboardId + "." + dashlet + "." + DASHLET_PROPERTY_ORDERX);
				if(orderX == null) {
					orderX = "9999";
				}
				String orderY = settings.getProperty(dashboardId + "." + dashlet + "." + DASHLET_PROPERTY_ORDERY);
				if(orderY == null) {
					orderY = "9999";
				}
				String width = settings.getProperty(dashboardId + "." + dashlet + "." + DASHLET_PROPERTY_WIDTH);
				if(width == null) {
					width = "1";
				}
				String name = settings.getProperty(dashboardId + "." + dashlet + "." + DASHLET_PROPERTY_NAME);
				if(name == null) {
					name = "DefaultDashlet";
				}
				String label = settings.getProperty(dashboardId + "." + dashlet + "." + DASHLET_PROPERTY_LABEL);
				if(label == null) {
					label = name;
				}
				if(dashletFilter == null || label.startsWith(dashletFilter)) {
					if(dashboard.get(orderY) == null) {
						dashboard.put(
							orderY, 
							new TreeMap<String,DashletDescr>()
						);
					}						
					Map<String,DashletDescr> row = dashboard.get(orderY);
					row.put(
						orderX + "." + dashlet + (dashletFilter == null ? "" : "." + dashletFilter), 
						new DashletDescr(dashlet, name, width, label, orderX, orderY)
					);
				}
			}
		}
	}
	
	//-----------------------------------------------------------------------
	@Override
    public void paint(
    	ViewPort p, 
    	boolean forEditing
    ) throws ServiceException {
		ApplicationContext app = p.getApplicationContext();
		HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
		View view = p.getView();		
		if(view instanceof ShowObjectView) {
			ShowObjectView showView = (ShowObjectView)view;			
			String objectTypeName = showView.getRefObject().refClass().refMofId();
			String dashboardId = DashboardControl.class.getSimpleName() + "." + objectTypeName;
			Map<String,Map<String,DashletDescr>> dashboard = new TreeMap<String,Map<String,DashletDescr>>();
			this.getDashboard(
				dashboardId, 
				app.getSettings(), 
				null, // dashletFilter
				dashboard
			);
			Path userHomeAdminIdentity = app.getUserHomeIdentity() == null ?
				null :
					app.getUserHomeIdentity().getParent().getDescendant(
						app.getRoleMapper().getAdminPrincipal(app.getCurrentSegment())
					);
			// Merge dashlets from segment administrator. 
			// Only merge dashlets with label prefix "Public."
			// Do not merge in edit mode.
			boolean isAdmin = 
				(app.getUserHomeIdentity() == null) || 
				(app.getUserHomeIdentity().equals(userHomeAdminIdentity));
			if(!forEditing && !isAdmin) {
				try {
					Properties settings = app.getUserSettings(userHomeAdminIdentity);
	    			this.getDashboard(
	    				dashboardId, 
	    				settings, 
	    				SHARED_DASHLET_MARKER, // dashletFilter
	    				dashboard
	    			);	                
				} catch(Exception e) {}
			}
			if(!dashboard.isEmpty()) {
				p.write("<p>");
				p.write("<div id=\"", this.id, "\">");
				p.write("  <table class=\"dashboard\" width=\"100%\">");
				for(String y: dashboard.keySet()) {
					Map<String,DashletDescr> row = dashboard.get(y);						
					p.write("    <tr>");
					for(String x: row.keySet()) {
						DashletDescr dashletDescr = row.get(x);
						String dashletId = 
							(x.endsWith(SHARED_DASHLET_MARKER) ? SHARED_DASHLET_MARKER : "") +
							dashboardId + "." + dashletDescr.id;
						Action action = new Action(
							Action.EVENT_INVOKE_WIZARD,
							new Action.Parameter[]{
								new Action.Parameter(Action.PARAMETER_OBJECTXRI, showView.getRefObject().refGetPath().toXri()),
								new Action.Parameter(Action.PARAMETER_ID, dashletId)
							},
							"",
							true
						);
						CharSequence dashletHRef = p.getEvalHRef(action).toString().replace(WebKeys.SERVLET_NAME, "wizards/Dashboard/" + dashletDescr.name + ".jsp");
						p.write("      <td colspan=\"", dashletDescr.width,"\" height=\"100%\" valign=\"top\" style=\"border:1px solid #DDDDDD;\">");
						p.write("        <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">");
						if(dashletDescr.label != null && dashletDescr.label.length() > 0) {
							p.write("          <tr>");
							p.write("            <td class=\"DashletTitle\" title=\"V:", dashletDescr.orderY, ", H:", dashletDescr.orderX, ", W:", dashletDescr.width, "\" style=\"font-weight:bold;padding:2px;background-color:#EEEEEE;border:1px solid #DDDDDD;height:14px;\">", dashletDescr.label, "</td>");
							p.write("          </tr>");
						}
						p.write("          <tr>");
						p.write("            <td>");
						p.write("              <div id=\"", dashletId, "\">");
						if(forEditing) {
							p.write("                <p>");
							p.write("                <table width=\"100%\">");
							p.write("                  <tr>");
							p.write("                    <td>");
							p.write("                      <table>");
							// Id
							p.write("                        <tr>");
			                p.write("                          <td><label>Id:</label></td>");
			                p.write("                          <td><input type=\"text\" id=\"", dashletId, ".", DASHLET_PROPERTY_ID, "\" name=\"", dashletId, ".", DASHLET_PROPERTY_ID, "\" readonly style=\"\" value=\"", htmlEncoder.encode(dashletDescr.id, false), "\"/></td>");							
							p.write("                        </tr>");
							// Type
							p.write("                        <tr>");
			                p.write("                          <td><label>Name:</label></td>");
			                p.write("                          <td><input type=\"text\" id=\"", dashletId, ".", DASHLET_PROPERTY_NAME, "\" name=\"", dashletId, ".", DASHLET_PROPERTY_NAME, "\" readonly style=\"\" value=\"", htmlEncoder.encode(dashletDescr.name, false), "\"/></td>");							
							p.write("                        </tr>");
							// Label
							p.write("                        <tr>");
			                p.write("                          <td><label>Label:</label></td>");
			                p.write("                          <td><input type=\"text\" id=\"", dashletId, ".", DASHLET_PROPERTY_LABEL, "\" name=\"", dashletId, ".", DASHLET_PROPERTY_LABEL, "\" style=\"\" value=\"", htmlEncoder.encode(dashletDescr.label, false), "\"/></td>");							
							p.write("                        </tr>");
							// orderY
							p.write("                        <tr>");
			                p.write("                          <td><label>Vertical order:</label></td>");
			                p.write("                          <td>");							
			                p.write("                            <select id=\"", dashletId, ".", DASHLET_PROPERTY_ORDERY, "\" name=\"", dashletId, ".", DASHLET_PROPERTY_ORDERY, "\" style=\"\" />");							
			                for(int i = 0; i < 10; i++) {
			                	p.write("                              <option ", (i == Integer.valueOf(dashletDescr.orderY) ? " selected" : ""), " value=\"", Integer.toString(i), "\">", Integer.toString(i), "</option>");
			                }
			                p.write("                          </td>");							
							p.write("                        </tr>");
							// orderX
							p.write("                        <tr>");
			                p.write("                          <td><label>Horizontal order:</label></td>");
			                p.write("                          <td>");							
			                p.write("                            <select id=\"", dashletId, ".", DASHLET_PROPERTY_ORDERX, "\" name=\"", dashletId, ".", DASHLET_PROPERTY_ORDERX, "\" style=\"\" />");
			                for(int i = 0; i < 10; i++) {
			                	p.write("                              <option ", (i == Integer.valueOf(dashletDescr.orderX) ? " selected" : ""), " value=\"", Integer.toString(i), "\">", Integer.toString(i), "</option>");
			                }
			                p.write("                          </td>");							
							p.write("                        </tr>");
							// Width
							p.write("                        <tr>");
			                p.write("                          <td><label>Width:</label></td>");
			                p.write("                          <td>");							
			                p.write("                            <select id=\"", dashletId, ".", DASHLET_PROPERTY_WIDTH, "\" name=\"", dashletId, ".", DASHLET_PROPERTY_WIDTH, "\" style=\"\" />");							
			                for(int i = 1; i <= 10; i++) {
			                	p.write("                              <option ", (i == Integer.valueOf(dashletDescr.width) ? " selected" : ""), " value=\"", Integer.toString(i), "\">", Integer.toString(i), "</option>");
			                }
			                p.write("                          </td>");							
							p.write("                        </tr>");
							p.write("                      </table>");
							p.write("                    </td>");
							p.write("                    <td style=\"horizontal-align:right;vertical-align:bottom;\">");
							p.write("                      <input type=\"submit\" class=\"abutton\" name=\"Delete.", dashletId, "\" value=\"-\" onclick=\"javascript:$('Command').value=this.name;\" />");
							p.write("                    </td>");
							p.write("                  </tr>");
							p.write("                </table>");
							p.write("                <p>");
						}
						else {
							p.write("                <script language=\"javascript\" type=\"text/javascript\">");
	                        p.write("                  new Ajax.Updater('", dashletId, "', ", dashletHRef, ", {asynchronous:true, evalScripts: true});");							
							p.write("                </script>");
						}
						p.write("              </div>");
						p.write("            </td>");
						p.write("          </tr>");
						p.write("        </table>");
						p.write("      </td>");
					}
					p.write("    </tr>");
				}
				p.write("  </table>");
				p.write("</div>");
			}
		}
    }

	//-----------------------------------------------------------------------
	// Members
	//-----------------------------------------------------------------------
	public static final String SHARED_DASHLET_MARKER = "*";
	public static final String DASHBOARD_PROPERTY_DASHLETS = "dashlets";
	public static final String DASHLET_PROPERTY_ID = "id";
	public static final String DASHLET_PROPERTY_NAME = "name";
	public static final String DASHLET_PROPERTY_LABEL = "label";
	public static final String DASHLET_PROPERTY_ORDERX = "orderX";
	public static final String DASHLET_PROPERTY_ORDERY = "orderY";
	public static final String DASHLET_PROPERTY_WIDTH = "width";
	
}
