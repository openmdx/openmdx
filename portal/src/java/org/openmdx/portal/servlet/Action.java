/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Action.java,v 1.38 2008/11/12 01:05:06 wfro Exp $
 * Description: Action
 * Revision:    $Revision: 1.38 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/12 01:05:06 $
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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class Action
  implements Serializable {

  //-------------------------------------------------------------------------
  public static class Parameter
    implements Serializable {
      
    public Parameter(
          String name,
          String value
      ) {
          this.name = name;
          this.value = value;
      }
      
      public String getName(
      ) {
          return this.name;
      }
      
      public String getValue(
      ) {
          return this.value;
      }
      
      private static final long serialVersionUID = 3257572801782296631L;
      private final String name;
      private final String value;
  }
  
  //-------------------------------------------------------------------------
  public Action(
      int event,
      Parameter[] parameters,
      String title,
      boolean isEnabled
  ) {
      this(
          event,
          parameters,
          title,
          null,
          isEnabled
      );
  }
  
  //-------------------------------------------------------------------------
  public Action(
    int event,
    Parameter[] parameters,
    String title,
    String iconKey,
    boolean isEnabled
  ) {
      this(
          event,
          parameters,
          title,
          null,
          iconKey,
          isEnabled
      );
  }
  
  //-------------------------------------------------------------------------
  public Action(
    int event,
    Parameter[] parameters,
    String title,
    String toolTip,
    String iconKey,
    boolean isEnabled
  ) {
      this.event = event;
      this.title = title;
      this.isEnabled = isEnabled;
      this.parameters = parameters;
      if(toolTip != null) {
          this.toolTip = toolTip;
      }
      if(iconKey != null) {
          this.iconKey = iconKey;
      }
  }
  
  //-------------------------------------------------------------------------
  public static Action getFindObjectAction(
      String feature,
      String id
  ) {
      return new Action(
          Action.EVENT_FIND_OBJECT,
          new Action.Parameter[]{
              new Action.Parameter(Action.PARAMETER_REFERENCE, feature),
              new Action.Parameter(Action.PARAMETER_ID, id)
          },
          "",
          WebKeys.ICON_LOOKUP, 
          true
      );
  }
  
  //-------------------------------------------------------------------------  
  /**
   * Returns the value of parameter with specified name. The parameter 
   * string is of the form name:(value)
   */
  public static String getParameter(
      String parameter,
      String name
  ) {
      int start = 0;
      if((start = parameter.indexOf(name + "*(")) < 0) {
          return "";
      }
      start += name.length() + 2;
      int end = start;
      int nesting = 1;
      while(
          (end < parameter.length()) && 
          (nesting > 0)
      ) {
          char c = parameter.charAt(end);
          if(c == '(') {
              nesting++;
          }
          else if(c == ')') {
              nesting--;
          }
          end++;
      }
      return parameter.substring(start, end - 1);
  }

  //-------------------------------------------------------------------------
  public int getEvent(
  ) {
    return this.event;
  }
  
  //-------------------------------------------------------------------------
  public String getTitle(
  ) {
    return this.title;
  }
  
  //-------------------------------------------------------------------------
  public String getToolTip(
  ) {
    return this.toolTip;
  }
  
    //-------------------------------------------------------------------------
    public String getParameter(
    ) {
        if(this.parameter == null) {
            StringBuilder parameter = new StringBuilder();
            if(this.parameters != null) {
                for(int i = 0; i < this.parameters.length; i++) {
                   	parameter.append(
                        (i == 0) ? "" : "*" 
                    ).append(
                    	parameters[i].getName()
                    ).append(
                    	"*("
                    ).append(
                    	parameters[i].getValue()
                    ).append(
                    	")"
                    );
                }
            }
            this.parameter = parameter.toString();
        }
        return this.parameter;
    }
    
    //-----------------------------------------------------------------------
    /**
     * Encode all characters which are not encoded by encodeURI
     */
    public String getParameterEncoded(
    ) {      
        String s = this.getParameter();
        StringBuilder t = new StringBuilder();
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(c == '+') {
            	t.append("%2B");
            }
            else if(c == '=') {
            	t.append("%3D");
            }
            else if(c == '$') {
            	t.append("%24");
            }
            else if(c == ',') {
            	t.append("%2C");
            }
            else if(c == '/') {
            	t.append("%2F");
            }
            else if(c == '?') {
            	t.append("%3F");
            }
            else if(c == ':') {
            	t.append("%3A");
            }
            else if(c == '@') {
            	t.append("%40");
            }
            else if(c == '&') {
            	t.append("%26");
            }
            else if(c == '#') {
            	t.append("%23");
            }
            else {
            	t.append(c);                
            }
        }
        return t.toString();
    }
      
    //-------------------------------------------------------------------------
    /**
     * Returns parameter value of specified parameter. Parameters are ; separated 
     * and of the form name=value.
     */
    public String getParameter(
        String name
    ) {
        if(this.parameters == null) {
            return "";
        }
        for(int i = 0; i < this.parameters.length; i++) {
            if(name.equals(this.parameters[i].getName())) {
                return this.parameters[i].getValue();
            }
        }
        return "";
    }
  
    //-------------------------------------------------------------------------
    public Parameter[] getParameters(
    ) {
        return this.parameters;
    }
    
    //-------------------------------------------------------------------------
    public String getIconKey(
    ) {
        return this.iconKey;
    }
  
    //-------------------------------------------------------------------------
    public void setIconKey(
        String iconKey
    ) {
        this.iconKey = iconKey;
    }
    
    //-------------------------------------------------------------------------
    public boolean isEnabled(
    ) {
        return this.isEnabled;
    }
  
    //-------------------------------------------------------------------------
    public String toString(
    ) {
        return "Action={event=" + this.event + ", " + ", parameter=" + this.getParameter() + ", title=" + this.title + "}";
    }
  
    //-------------------------------------------------------------------------
    public String getEncodedHRef(
    ) {
        return this.getEncodedHRef(null);            
    }
    
    //-------------------------------------------------------------------------
    public String getEncodedHRef(
        String requestId
    ) {
        String[] components = this.getHRef(
            requestId
        );
        StringBuilder href = new StringBuilder(components[0]);
        for(int i = 1; i < components.length; i+=2) try {
            href.append(
                i == 1 ? "?" : "&"
            ).append(
                components[i]
            ).append(
                "="
            ).append(
                URLEncoder.encode(components[i+1], "UTF-8")
            );
        } catch(UnsupportedEncodingException e) {}
        return href.toString();
    }
        
    //-------------------------------------------------------------------------
    public String[] getHRef(
    ) {
        return this.getHRef(null);
    }
    
    //-------------------------------------------------------------------------
    public String[] getHRef(
        String requestId
    ) {
        String actionParameter = this.getParameter();
        int n = 3;
        if(requestId != null) n+=2;
        if(actionParameter.length() > 0) n+=2;
        String[] components = new String[n];
        n = 0;
        // Servlet name
        // Return download URLs as SERVLET_NAME/<file name>?params. This is
        // an IE workaround. Setting the reply header fields is not sufficient.
        // Also see http://ppewww.ph.gla.ac.uk/~flavell/www/content-type.html
        StringBuilder href = new StringBuilder(WebKeys.SERVLET_NAME);      
        if(
            (this.getEvent() == Action.EVENT_DOWNLOAD_FROM_LOCATION) || 
            (this.getEvent() == Action.EVENT_DOWNLOAD_FROM_FEATURE)
        ) {
            href.append(
                "/"
            ).append(
                this.getParameter(Action.PARAMETER_NAME)
            );
        }
        components[n++] = href.toString();      
        // REQUEST_ID
        if(requestId != null) {
            components[n++] = WebKeys.REQUEST_ID;
            components[n++] = requestId;
        }      
        // REQUEST_EVENT
        components[n++] = WebKeys.REQUEST_EVENT;
        components[n++] = Integer.toString(this.getEvent());      
        // Parameter name
        if(actionParameter.length() > 0) {
            components[n++] = WebKeys.REQUEST_PARAMETER;
            components[n++] = actionParameter;
        }      
        return components;
    }

    //-------------------------------------------------------------------------
    public String getEvalHRef(        
    ) {
        return this.getEvalHRef(null);
    }
    
    //-------------------------------------------------------------------------
    public String getEvalHRef(
        String requestId
    ) {
        String[] components = this.getHRef(
            requestId
        );
        StringBuilder href = new StringBuilder("getEncodedHRef([");
        for(int i = 0; i < components.length; i++) {
            (
               i > 0 ? href.append(", ") : href
            ).append(
                "'"
            ).append(
                components[i]
            ).append(
                "'"
            );
        }
        return href.append("])").toString();
    }
            
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3616453392827103289L;

    private final int event;
    private String title = "N/A";
    private String toolTip = "N/A";
    private String parameter = null;
    private final Parameter[] parameters;
    private String iconKey = WebKeys.ICON_DEFAULT;
    private final boolean isEnabled;


    //-------------------------------------------------------------------------
    // Parameters
    //-------------------------------------------------------------------------
    public static final String PARAMETER_PANE = "pane";
    public static final String PARAMETER_PAGE = "page";
    public static final String PARAMETER_REFERENCE = "reference";
    public static final String PARAMETER_REFERENCE_NAME = "referenceName";
    public static final String PARAMETER_FOR_CLASS = "forClass";
    public static final String PARAMETER_FOR_REFERENCE = "forReference";
    public static final String PARAMETER_NAME = "name";
    public static final String PARAMETER_TYPE = "type";
    public static final String PARAMETER_ID = "id";
    public static final String PARAMETER_TARGETXRI = "targetXri";
    public static final String PARAMETER_ROW_ID = "rowId";
    public static final String PARAMETER_REQUEST_ID = "requestId";
    public static final String PARAMETER_OBJECTXRI = "xri";
    public static final String PARAMETER_TAB = "tab";
    public static final String PARAMETER_LOCALE = "locale";
    public static final String PARAMETER_MIME_TYPE = "mimeType";
    public static final String PARAMETER_LOCATION = "location";
    public static final String PARAMETER_FEATURE = "feature";
    public static final String PARAMETER_STATE = "state";
    public static final String PARAMETER_POSITION = "position";
    public static final String PARAMETER_FORMAT = "format";
    public static final String PARAMETER_SIZE = "size";
    public static final String PARAMETER_MODE = "mode";
    public static final String PARAMETER_SCOPE = "scope";
    public static final String PARAMETER_FILTER_BY_FEATURE = "filterByFeature";
    public static final String PARAMETER_FILTER_BY_TYPE = "filterByType";
    public static final String PARAMETER_FILTER_OPERATOR = "filterOperator";
    public static final String PARAMETER_ORDER_BY_FEATURE = "orderByFeature";
    
    //-------------------------------------------------------------------------
    // Events
    //-------------------------------------------------------------------------
    public final static int EVENT_NONE = 0;
    public final static int EVENT_SELECT_FILTER = 1;
    public final static int EVENT_PAGE_NEXT = 2;
    public final static int EVENT_PAGE_PREVIOUS = 3;
    public final static int EVENT_SET_PAGE = 4;
    public final static int EVENT_NEW_OBJECT = 5;
    public final static int EVENT_SELECT_OBJECT = 6;
    public final static int EVENT_SAVE = 8;
    public final static int EVENT_CANCEL = 9;
    public final static int EVENT_SET_ORDER_ASC = 10;
    public final static int EVENT_SET_ORDER_DESC = 11;
    public final static int EVENT_SET_ORDER_ANY = 12;
    public final static int EVENT_SET_COLUMN_FILTER = 13;
    public final static int EVENT_SELECT_LOCALE = 14;
    public final static int EVENT_SELECT_REFERENCE = 15;
    public final static int EVENT_EDIT = 16;
    public final static int EVENT_DELETE = 17;
    public final static int EVENT_ADD_OBJECT = 18;
    public final static int EVENT_SET_GRID_ALIGNMENT_NARROW = 19;
    public final static int EVENT_SET_GRID_ALIGNMENT_WIDE = 20;
    public final static int EVENT_FIND_OBJECT = 21;
    public final static int EVENT_INVOKE_OPERATION = 22;
    public final static int EVENT_DOWNLOAD_FROM_LOCATION = 23;
    public final static int EVENT_LOGOFF = 24;
    public final static int EVENT_RELOAD = 25;
    public final static int EVENT_ADD_COLUMN_FILTER = 26;
    public final static int EVENT_SELECT_AND_EDIT_OBJECT = 27;
    public final static int EVENT_MULTI_DELETE = 28;
    public final static int EVENT_SELECT_AND_NEW_OBJECT = 29;
    public final static int EVENT_SET_CURRENT_FILTER_AS_DEFAULT = 30;
    public final static int EVENT_SAVE_SETTINGS = 31;
    public final static int EVENT_ADD_ORDER_ASC = 32;
    public final static int EVENT_ADD_ORDER_DESC = 33;
    public final static int EVENT_ADD_ORDER_ANY = 34;
    public final static int EVENT_DOWNLOAD_FROM_FEATURE = 35;
    public final static int EVENT_SAVE_GRID = 36;
    public final static int EVENT_SET_SHOW_ROWS_ON_INIT = 37;
    public final static int EVENT_SET_HIDE_ROWS_ON_INIT = 38;
    public final static int EVENT_SET_PANEL_STATE = 39;
    public final static int EVENT_FIND_OBJECTS = 40;
    public final static int EVENT_INVOKE_WIZARD = 41;
    public final static int EVENT_MACRO = 43;
    public final static int EVENT_GRID_GET_ROW_MENU = 44;
    public final static int EVENT_OBJECT_GET_ATTRIBUTES = 45;
    public final static int EVENT_SET_ROLE = 47;
    public final static int EVENT_SELECT_PERSPECTIVE = 48;
    
    //-----------------------------------------------------------------------
    // Macro types
    //-----------------------------------------------------------------------
    public static final int MACRO_TYPE_NA = 0;
    public static final int MACRO_TYPE_JAVASCRIPT = 1;
      
}

//--- End of File -----------------------------------------------------------
