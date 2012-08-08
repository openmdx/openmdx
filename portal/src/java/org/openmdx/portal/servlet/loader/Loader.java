/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Loader.java,v 1.12 2011/09/16 09:01:41 wfro Exp $
 * Description: Loader
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/09/16 09:01:41 $
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;

import javax.servlet.ServletContext;

import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.PortalExtension_1_0;

public class Loader {

  //-------------------------------------------------------------------------
  public Loader(
      ServletContext context,
      PortalExtension_1_0 portalExtension
  ) {
      this.context = context;
      this.portalExtension = portalExtension;
  }
          
  //-------------------------------------------------------------------------
  /**
   * Map last path component to segment name, i.e. path
   * ../WEB-INF/config/ui/Root will be mapped to segment Root
   */ 
  protected String[] getSegmentName(
      String path
  ) {
      if(path.endsWith("/")) {
          path = path.substring(0, path.length() - 1);
      }
      path = path.substring(path.lastIndexOf("/") + 1);
      if(!Character.isDigit(path.charAt(0))) {
          path = "0-" + path;
      }
      return path.split("-");
  }
  
  //-------------------------------------------------------------------------
  protected String getAdminPrincipal(
      String path
  ) {
      String[] segmentName = this.getSegmentName(path);
      return this.portalExtension.getAdminPrincipal(segmentName[segmentName.length-1]);
  }
  
  //-------------------------------------------------------------------------
  protected List<String> getDirectories(
      String path
  ) {
      List<String> dirs = new ArrayList<String>();
      Set paths = this.context.getResourcePaths(path);
      if(paths != null) {
          for(
              Iterator i = paths.iterator(); 
              i.hasNext(); 
          ) {        
              String p = (String)i.next();
              if(p.endsWith("/")) {
                  dirs.add(p);
              }
          }
      }
      return dirs;
  }
  
  //-------------------------------------------------------------------------
  protected long getCRCForResourcePath(
    ServletContext context,
    String resourcePath
  ) {
    SysLog.info("Inspecting " + resourcePath);
    Set resources = context.getResourcePaths(resourcePath);
    CRC32 crc = new CRC32();
    if(resources != null) {
        for(
            Iterator i = resources.iterator(); 
            i.hasNext(); 
        ) {
          String path = (String)i.next();
          if(!path.endsWith("/")) {
            try {
              InputStream is = context.getResourceAsStream(path);
              int b;
              while((b = is.read()) >= 0) {
                crc.update(b);
              }
              is.close();
            }
            catch(IOException e) {}
          }
        }
    }
    return crc.getValue();
  }
  
  //-------------------------------------------------------------------------
  protected final ServletContext context;
  protected final PortalExtension_1_0 portalExtension;
  
}

//--- End of File -----------------------------------------------------------
