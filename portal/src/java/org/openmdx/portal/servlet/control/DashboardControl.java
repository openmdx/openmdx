/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: DashboardControl 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2009-2014, OMEX AG, Switzerland
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
import java.util.Collections;
import java.util.List;

import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.View;

/**
 * DashboardControl
 *
 */
public class DashboardControl extends AbstractDashboardControl implements Serializable {
	
	/**
	 * Constructor.
	 * 
	 * @param id
	 * @param locale
	 * @param localeAsIndex
	 */
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
	
	/* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.AbstractDashboardControl#getDashboardStyle()
     */
    @Override
    protected String getDashboardStyle(
    ) {
	    return "width=\"100%\"";
    }

	/* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.AbstractDashboardControl#getDashboardIdSuffix(org.openmdx.portal.servlet.view.View)
     */
    @Override
    protected String getDashboardIdSuffix(
    	ViewPort p
    ) {
    	View view = p.getView();
    	return view instanceof ShowObjectView ?
    		((ShowObjectView)view).getObject().refClass().refMofId() : 
    			null;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.Control#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		return Collections.emptyList();
	}

	//-----------------------------------------------------------------------
	// Members
	//-----------------------------------------------------------------------
	private static final long serialVersionUID = 6773430723279101053L;

}
