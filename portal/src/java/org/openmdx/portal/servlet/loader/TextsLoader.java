/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: TextsLoader.java,v 1.7 2008/04/04 17:01:12 hburger Exp $
 * Description: TextsLoader
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/04 17:01:12 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.loader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.RoleMapper_1_0;
import org.openmdx.portal.servlet.texts.TextsFactory;

public class TextsLoader
    extends Loader {

  //-------------------------------------------------------------------------
  public TextsLoader(
      ServletContext context,
      RoleMapper_1_0 roleMapper      
  ) {
      super(
          context,
          roleMapper
      );
  }
  
  //-------------------------------------------------------------------------
  synchronized public TextsFactory loadTexts(
    String[] locale
  ) throws ServiceException {
    System.out.println("Loading texts");
    // 2-dim list: first index=locale, second index = input stream
    List<List<InputStream>> textsInputStreams = new ArrayList<List<InputStream>>();
    int fallbackLocaleIndex = 0;
    for(int i = 0; i < locale.length; i++) {
        Set localeTextsPaths = new HashSet();
        if(locale[i] != null) {
            fallbackLocaleIndex = 0;
            localeTextsPaths = context.getResourcePaths("/WEB-INF/config/texts/" + locale[i]);
            if(localeTextsPaths == null) {
                for(int j = i-1; j >= 0; j--) {
                    if((locale[j] != null) && locale[i].substring(0,2).equals(locale[j].substring(0,2))) {
                        fallbackLocaleIndex = j;
                        break;
                    }
                }
                System.out.println(locale[i] + " not found. Fallback to " + locale[fallbackLocaleIndex]);
                localeTextsPaths = context.getResourcePaths("/WEB-INF/config/texts/" + locale[fallbackLocaleIndex]);
            }
        }
        List<InputStream> localeTextsInputStreams = new ArrayList<InputStream>();
        textsInputStreams.add(localeTextsInputStreams);
        for(Iterator j = localeTextsPaths.iterator(); j.hasNext(); ) {
            String path = (String)j.next();
            if(!path.endsWith("/")) {            
                System.out.println("Loading " + path);
                localeTextsInputStreams.add(
                    context.getResourceAsStream(path)
                );
            }
        }
    }
    return new TextsFactory(
        locale,
        (List[])textsInputStreams.toArray(new List[textsInputStreams.size()])
    );
  }
      
}

//--- End of File -----------------------------------------------------------
