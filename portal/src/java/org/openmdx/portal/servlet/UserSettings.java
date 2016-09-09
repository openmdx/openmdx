/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: UserSettings 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2010-2011, OMEX AG, Switzerland
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

package org.openmdx.portal.servlet;

/**
 * UserSettings
 *
 */
public enum UserSettings {

	LOCALE_NAME("Locale.Name"),
	TIMEZONE_NAME("TimeZone.Name"),
	PERSPECTIVE_ID("Perspective.ID"),
	SHOW_ROWS_ON_INIT("Page.DefaultFilterOnInit"),
	SHOW_SEARCH_FORM("Page.ShowSearchForm"),
	PAGE_ALIGNMENT("Page.Alignment"),
    TOP_NAVIGATION_SHOW_MAX("TopNavigation.ShowMax"),
	DEFAULT_FILTER("Filter.Default"),
	PAGE_SIZE("Page.Size"),
	PAGE_COLUMN_ORDERING("Page.ColumnOrdering"),
	ROOT_OBJECT_STATE("RootObject"),
	WORKSPACE_ID("Workspace.ID"),	
	HIDE_WORKSPACE_DASHBOARD("WorkspaceDashboard.Hide"),
	AUTOSTART_URL("Autostart.URL"),
	ANCHOR_USER_DIALOG("UserDialog.Anchor");

	private final String name;
	
	private UserSettings(
		String name
	) {
		this.name = name;
	}
	
	public String getName(
	) {
	    return this.name;
	}    
	
}
