/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: QueryTag 
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

import javax.servlet.jsp.JspException;

import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.Filter;
import org.openmdx.portal.servlet.Filters;
import org.openmdx.portal.servlet.component.Grid;
import org.openmdx.portal.servlet.component.ReferencePane;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.UiGrid;

public class QueryTag extends BaseTag {

	public void setQuery(
		Object newValue
	) {
		this.query = newValue;
	}
	
	public Object getQuery(
	) {
		return this.query;
	}
	
	public String getName(
	) {
	    return this.name;
	}

	public void setName(
		String newValue
	) {
	    this.name = newValue;
	}    

	@Override
    public int doStartTag(
    ) throws JspException {
		if(this.getParent() instanceof ShowObjectTag) {
			ShowObjectView view = ((ShowObjectTag)this.getParent()).getView();
			if(view != null) {
				for(ReferencePane referencePane: view.getChildren(ReferencePane.class)) {
					for(Action selectReferenceAction: referencePane.getSelectReferenceActions()) {
						if(this.name.equals(selectReferenceAction.getParameter(Action.PARAMETER_REFERENCE_NAME))) {
							referencePane.selectReference(
								Integer.valueOf(selectReferenceAction.getParameter(Action.PARAMETER_REFERENCE))
							);
							if(this.query instanceof RefQuery_1_0) {
								Grid grid = referencePane.getGrid();
								if(grid instanceof UiGrid) {
			                    	UiGrid uiGrid = (UiGrid)grid;                    										
									Filter defaultFilter = uiGrid.getFilter(Filters.DEFAULT_FILTER_NAME);
									QueryFilterRecord query = ((RefQuery_1_0)this.query).refGetFilter();
									if(defaultFilter != null) {
										uiGrid.setFilter(
											Filters.DEFAULT_FILTER_NAME, 
											new Filter(
												defaultFilter.getName(),
												defaultFilter.getLabel(),
												defaultFilter.getGroupName(),
												defaultFilter.getIconKey(),
												defaultFilter.getOrder(),
												query.getCondition(),
												query.getOrderSpecifier(),
												query.getExtension()
											)
										);
									}
									uiGrid.selectFilter(
										Filters.DEFAULT_FILTER_NAME, 
										"" // filterValues
									);
								}
							}
						}
					}
				}
			}
		}
		return EVAL_BODY_INCLUDE;
    }

	//-----------------------------------------------------------------------
	// Members
	//-----------------------------------------------------------------------
	private static final long serialVersionUID = -7944478980009666356L;

	private Object query = null;
	private String name = null;

}
