/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: WebKeys 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.portal.servlet;

public class WebKeys {

    public static final String SERVLET_NAME = "ObjectInspectorServlet";
    public static final String APPLICATION_KEY = WebKeys.SERVLET_NAME + ".ApplicationContext";
    public static final String VIEW_CACHE_KEY_SHOW = WebKeys.SERVLET_NAME + ".ViewCache.Show";
    public static final String VIEW_CACHE_KEY_EDIT = WebKeys.SERVLET_NAME + ".ViewCache.Edit";
    public static final String VIEW_CACHE_CACHED_SINCE = WebKeys.SERVLET_NAME + ".ViewCache.CachedSince";    
    public static final String CURRENT_VIEW_KEY = WebKeys.SERVLET_NAME + ".View";
    public static final String LOCALE_KEY = "locale";
    public static final String TIMEZONE_KEY = "timezone";
    public static final String REQUEST_ID = "requestId";
    public static final String REQUEST_EVENT = "event";
    public static final String REQUEST_PARAMETER = "parameter";
    public static final String REQUEST_PARAMETER_ENC = WebKeys.REQUEST_PARAMETER + ".enc";
    public static final String REQUEST_PARAMETER_LIST = WebKeys.REQUEST_PARAMETER + ".list";    
    public static final String REQUEST_PARAMETER_FILTER_VALUES = "filtervalues";
    public static final String REQUEST_PARAMETER_PAGE_SIZE = "pagesize";  
    public static final String REQUEST_PARAMETER_LOCALE = "locale";      
	public static final String REQUEST_PARAMETER_SHOW_SEARCH_FORM = "showSearchForm";    
    
    // Icons
    public static final String ICON_TYPE = ".gif";
    public static final String ICON_DEFAULT = "default" + WebKeys.ICON_TYPE;
    public static final String ICON_MISSING = "missing" + WebKeys.ICON_TYPE;
    public static final String ICON_SEARCH_INC = "search_inc" + WebKeys.ICON_TYPE;
    public static final String ICON_PAGE_WIDE = "page_wide" + WebKeys.ICON_TYPE;
    public static final String ICON_PAGE_NARROW = "page_narrow" + WebKeys.ICON_TYPE;
    public static final String ICON_DELETE = "delete" + WebKeys.ICON_TYPE;
    public static final String ICON_FIRST = "first" + WebKeys.ICON_TYPE;
    public static final String ICON_NEXT = "next" + WebKeys.ICON_TYPE;
    public static final String ICON_NEXT_DISABLED = "next_disabled" + WebKeys.ICON_TYPE;
    public static final String ICON_NEXT_FAST = "next_fast" + WebKeys.ICON_TYPE;
    public static final String ICON_NEXT_FAST_DISABLED = "next_fast_disabled" + WebKeys.ICON_TYPE;
    public static final String ICON_PREVIOUS = "previous" + WebKeys.ICON_TYPE;
    public static final String ICON_PREVIOUS_DISABLED = "previous_disabled" + WebKeys.ICON_TYPE;
    public static final String ICON_PREVIOUS_FAST = "previous_fast" + WebKeys.ICON_TYPE;
    public static final String ICON_PREVIOUS_FAST_DISABLED = "previous_fast_disabled" + WebKeys.ICON_TYPE;
    public static final String ICON_SORT_ANY = "sort_any" + WebKeys.ICON_TYPE;
    public static final String ICON_SORT_UP = "sort_up" + WebKeys.ICON_TYPE;
    public static final String ICON_SORT_DOWN = "sort_down" + WebKeys.ICON_TYPE;
    public static final String ICON_CANCEL = "cancel" + WebKeys.ICON_TYPE;
    public static final String ICON_SAVE = "save" + WebKeys.ICON_TYPE;
    public static final String ICON_EDIT = "edit" + WebKeys.ICON_TYPE;
    public static final String ICON_UP = "up" + WebKeys.ICON_TYPE;
    public static final String ICON_MENU_DOWN = "menu_down" + WebKeys.ICON_TYPE; 
    public static final String ICON_LOOKUP = "lookup" + WebKeys.ICON_TYPE;
    public static final String ICON_LOOKUP_GRID = "lookup_grid" + WebKeys.ICON_TYPE;
    public static final String ICON_LOOKUP_AUTOCOMPLETE_GRID = "lookup_auto_grid" + WebKeys.ICON_TYPE;
    public static final String ICON_AUTOCOMPLETE_SELECT = "autocomplete_select.png";
    public static final String ICON_FILTER_ALL = "filter_all" + WebKeys.ICON_TYPE;
    public static final String ICON_FILTER_DEFAULT = "filter_default" + WebKeys.ICON_TYPE;    
    public static final String ICON_FILTER_SET_AS_DEFAULT = "filter_set_as_default" + WebKeys.ICON_TYPE;
    public static final String ICON_SHOW_ROWS_ON_INIT = "enable_default_filter_on_init" + WebKeys.ICON_TYPE;
    public static final String ICON_HIDE_ROWS_ON_INIT = "disable_default_filter_on_init" + WebKeys.ICON_TYPE;
    public static final String ICON_FILTER_HELP = "filter_help" + WebKeys.ICON_TYPE;
    public static final String ICON_FILTER_CLASS = "filter_class" + WebKeys.ICON_TYPE;
    public static final String ICON_FILTER_EXT = "filter_ext" + WebKeys.ICON_TYPE;
    public static final String ICON_NOT_CHECKED_R = "notchecked_r" + WebKeys.ICON_TYPE; 
    public static final String ICON_CHECKED_R = "checked_r" + WebKeys.ICON_TYPE;
    public static final String ICON_COLLAPSE = "collapse" + WebKeys.ICON_TYPE; 
    public static final String ICON_EXPAND = "expand" + WebKeys.ICON_TYPE;
    public static final String ICON_CALENDAR = "cal" + WebKeys.ICON_TYPE; 
    public static final String ICON_CLONE_SMALL = "clonesmall" + WebKeys.ICON_TYPE;
    public static final String ICON_DELETE_SMALL = "deletesmall" + WebKeys.ICON_TYPE;
    public static final String ICON_PANEL_DOWN = "panel_down" + WebKeys.ICON_TYPE;
    public static final String ICON_CLOSE = "close" + WebKeys.ICON_TYPE;
    public static final String ICON_SHOW_GRID_CONTENT = "show_content" + WebKeys.ICON_TYPE;    
    public static final String ICON_SEARCH_PANEL = "search_panel" + WebKeys.ICON_TYPE;
    public static final String ICON_UI_MODE = "uiMode";
    public static final String ICON_HEADER_SHOW = "headerShow" + WebKeys.ICON_TYPE;
    public static final String ICON_HEADER_HIDE = "headerHide" + WebKeys.ICON_TYPE;
    public static final String ICON_LOGOFF_SELECTED = "logoffSelected" + WebKeys.ICON_TYPE;
    public static final String ICON_SAVE_SELECTED = "saveSelected" + WebKeys.ICON_TYPE;
    public static final String ICON_ROLE = "Role" + WebKeys.ICON_TYPE;    
       
    // Configuration parameters
    public static final String CONFIG_USER_HOME = "userHome";
    public static final String CONFIG_HTML_ENCODER = "httpEncoder";
    public static final String CONFIG_ROLE_MAPPER = "roleMapper";
    
    // Special Characters    
    public static final String TAB_GROUPING_CHARACTER = "\u00bb";
    
    // Pages
    public static final String ERROR_PAGE = "/Error.jsp";
    
    // Security
    public static final String LOCKED_VALUE = "\u2015";
	// Predefined actions used for hasPermission() methods
    public static final String REVOKE_PREFIX = "-";
    public static final String GRANT_PREFIX = "+";
    public static final String PERMISSION_REVOKE_SHOW = REVOKE_PREFIX + "show";
	public static final String PERMISSION_REVOKE_EDIT = REVOKE_PREFIX + "edit";
    public static final String PERMISSION_GRANT_SHOW = GRANT_PREFIX + "show";
	public static final String PERMISSION_GRANT_EDIT = GRANT_PREFIX + "edit";
    
}

//--- End of File -----------------------------------------------------------

