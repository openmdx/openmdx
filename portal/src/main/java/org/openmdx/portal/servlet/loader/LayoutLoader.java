/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: LayoutLoader
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
package org.openmdx.portal.servlet.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.component.LayoutFactory;

public class LayoutLoader
    extends Loader {

  //-------------------------------------------------------------------------
  public LayoutLoader(
      ServletContext context,
      PortalExtension_1_0 portalExtension      
  ) {
      super(
          context,
          portalExtension
      );
  }
  
  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  synchronized public LayoutFactory loadLayouts(
      String[] locale,
      Model_1_0 model
  ) throws ServiceException {
	  String messagePrefix = new Date() + "  ";
    System.out.println(messagePrefix + "Loading layouts");
    SysLog.info("Loading layouts");
    // 2-dim list: first index=locale, second index = layout file name
    List<List<String>> layoutNames = new ArrayList<>();
    for(int i = 0; i < locale.length; i++) {
        Set<String> localeLayoutPaths = new HashSet<>();
        if(locale[i] != null) {
            localeLayoutPaths = context.getResourcePaths("/WEB-INF/config/layout/" + locale[i]);
            if(localeLayoutPaths == null) {
                localeLayoutPaths = Collections.emptySet();
            }
        }
        List<String> localeLayoutNames = new ArrayList<>();
        layoutNames.add(localeLayoutNames);
        for(Iterator<String> j = localeLayoutPaths.iterator(); j.hasNext(); ) {
            String path = j.next();
            if(!path.endsWith("/")) {            
                SysLog.info("Loading " + path);
                localeLayoutNames.add(path);
            }
        }
    }    
    System.out.println(messagePrefix + "Done (" + layoutNames.size() + " layouts)");
    SysLog.info("Done", layoutNames.size());
    return new LayoutFactory(
        locale,
        (List[])layoutNames.toArray(new List[layoutNames.size()]),
        model
    );
  }

}

//--- End of File -----------------------------------------------------------
