/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: TextsFactory
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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.log.SysLog;

public class LayoutFactory
  implements Serializable {
  
  //-------------------------------------------------------------------------
  public LayoutFactory(
      String[] locales,
      List<String>[] layoutNames,
      Model_1_0 model
  ) {
      this.layouts = new HashMap<String,List<String>>();
      this.locales = Arrays.asList(locales);
      for(int i = 0; i < locales.length; i++) {
          this.layouts.put(
              locales[i],
              layoutNames[i]
          );
      }
      this.model = model;
      SysLog.info("loaded layouts=" + this.layouts.keySet());
  }
  
  //-------------------------------------------------------------------------
  public String[] getLocale(
  ) {
    return this.locales.toArray(new String[this.locales.size()]);    
  }
  
  //-------------------------------------------------------------------------
  public String getLayout(
     String locale,
     String forClass,
     boolean forEditing
  ) {
      try {
          int localeAsIndex = this.locales.indexOf(locale);
          int currentLocale = localeAsIndex;
          // Lookup layout for currentLocale. If not found perform
          // locale fallback back to locale 0
          while(true) {
              List<String> layouts = this.layouts.get(this.locales.get(currentLocale));
              List<String> candidates = new ArrayList<String>();
              for(
                  Iterator<String> j = layouts.iterator();
                  j.hasNext();
              ) {
                  String layout = (String)j.next();
                  if(!DEFAULT_SHOW_LAYOUT.equals(layout) && !DEFAULT_EDIT_LAYOUT.equals(layout)) {
                      String showEditPrefix = (forEditing ? "edit" : "show") + "-";
                      if(layout.indexOf(showEditPrefix) >= 0) {
                          String layoutClass = layout.substring(
                              layout.lastIndexOf(showEditPrefix) + showEditPrefix.length(),
                              layout.lastIndexOf(".")
                          ).replace('.', ':');
                          if(forClass.equals(layoutClass)) {
                              candidates.add(0, layout);
                          }
                          else if(model.isSubtypeOf(forClass, layoutClass)) {
                              candidates.add(layout);
                          }
                      }
                  }
              }
              if(!candidates.isEmpty()) {
                  return (String)candidates.get(0);
              }
              // Locale fallback
              if(currentLocale == 0) break;
              int fallbackLocaleIndex = 0;
              for(int j = currentLocale-1; j >= 0; j--) {
                  if((this.locales.get(j) != null) && ((String)this.locales.get(currentLocale)).substring(0,2).equals(((String)this.locales.get(j)).substring(0,2))) {
                      fallbackLocaleIndex = j;
                      break;
                  }
              }
              currentLocale = fallbackLocaleIndex;
          }
      }
      catch(Exception e) {
    	  SysLog.warning("Can not get layout", e.getMessage());
    	  SysLog.detail(e.getMessage(), e.getCause());
      }
      return forEditing 
         ? DEFAULT_EDIT_LAYOUT
         : DEFAULT_SHOW_LAYOUT;      
  }
  
  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------
  private static final long serialVersionUID = -2868782788454716240L;

  private final static String DEFAULT_SHOW_LAYOUT = "/WEB-INF/config/layout/en_US/show-Default.jsp";
  private final static String DEFAULT_EDIT_LAYOUT = "/WEB-INF/config/layout/en_US/edit-Default.jsp";
  
  private final Map<String,List<String>> layouts;
  private final List<String> locales;
  private final Model_1_0 model;
  
}

//--- End of File -----------------------------------------------------------
