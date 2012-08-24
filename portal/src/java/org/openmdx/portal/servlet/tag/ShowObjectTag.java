/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ShowObjectTag 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;

import org.openmdx.base.naming.Path;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewPortFactory;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.control.Control;
import org.openmdx.portal.servlet.control.PageEpilogControl;
import org.openmdx.portal.servlet.control.ReferencePaneControl;
import org.openmdx.portal.servlet.control.ShowInspectorControl;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;

public class ShowObjectTag extends BaseTag {

	public void setObject(
		Object newValue
	) {
		this.object = newValue;
	}
	
	public Object getObject(
	) {
		return this.object;
	}
	
	public boolean isShowAttributes() {
		return this.showAttributes;
	}

	public void setShowAttributes(
		boolean showAttributes
	) {
		this.showAttributes = showAttributes;
	}
	
	public String getGrids(
	) {
	    return this.grids;
	}

	public void setGrids(
		String includes
	) {
	    this.grids = includes;
	}    

	public String getNavigationTarget(
	) {
	    return this.navigationTarget;
	}
	    
	public void setNavigationTarget(
		String navigationTarget
	) {
	    this.navigationTarget = navigationTarget;
	}

	public String getResourcePathPrefix(
	) {
	    return this.resourcePathPrefix;
	}    

	public void setResourcePathPrefix(
		String resourcePathPrefix
	) {
	    this.resourcePathPrefix = resourcePathPrefix;
	}

	public ShowObjectView getView(
	) {
	    return this.view;
	}

	@Override
    public int doStartTag(
    ) throws JspException {
		try {
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			HttpSession session = pageContext.getSession();
			ApplicationContext app = (ApplicationContext)session.getAttribute(WebKeys.APPLICATION_KEY);
			ViewsCache viewsCache = (ViewsCache)session.getAttribute(WebKeys.VIEW_CACHE_KEY_SHOW);
			if(app != null && viewsCache != null) {
				this.view = new ShowObjectView(
					UUIDs.newUUID().toString(),
					this.getId(), // containerElementId
					this.getObject(),
					app,
					new LinkedHashMap<Path,Action>(),
					null, // lookupType
					this.resourcePathPrefix,
					this.navigationTarget,
					true // isReadOnly
				);
	            this.view.createRequestId();
                viewsCache.addView(
                   this.view.getRequestId(),
                   this.view
                );
			}
			// Render
			ShowInspectorControl inspectorControl = view.getShowInspectorControl();
			ViewPort p = ViewPortFactory.openPage(
				this.view,
				request,
				pageContext.getOut()
			);
			p.setResourcePathPrefix(this.resourcePathPrefix);
			if(this.showAttributes) {
				p.write("<div id=\"", this.getId(), "_attributes\">");
				Control attributes = inspectorControl.getAttributePaneControl();				
				attributes.paint(p, false);
				p.flush();
				p.write("</div>");
			}
			List<Action> selectGridActions = new ArrayList<Action>();
			for(ReferencePane referencePane: view.getReferencePane()) {
				ReferencePaneControl referencePaneControl = referencePane.getReferencePaneControl();
				referencePaneControl.paint(
					p,
					ReferencePaneControl.FRAME_VIEW,
					false, // forEditing
					this.grids == null ? null : Arrays.asList(this.grids.split(","))
				);
				selectGridActions.add(
					referencePaneControl.getSelectReferenceAction()[
						referencePane.getSelectedReference()
					]
				);
			}
			p.flush();
			PageEpilogControl epilogControl = new PageEpilogControl(
				this.getId(),
				app.getLocale(),
				app.getCurrentLocaleAsIndex()
			);
			epilogControl.paint(
				p, 
				null, // frame
				false, // forEditing
				false // globals
			);
	        p.flush();
		} catch(Exception e) {
			throw new JspException(e);
		}
		return EVAL_BODY_INCLUDE;
    }

	//-----------------------------------------------------------------------
	// Members
	//-----------------------------------------------------------------------
	private Object object;
	private boolean showAttributes = false;
	private String grids = null;
	private String navigationTarget = "_none";
	private String resourcePathPrefix = "../../";
	private ShowObjectView view = null;    

}
