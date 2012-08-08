/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: DataLoader.java,v 1.6 2007/01/21 20:46:43 wfro Exp $
 * Description: DataLoader
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/01/21 20:46:43 $
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

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.QualityOfService;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.importer.xml.XmlImporter;
import org.openmdx.portal.servlet.RoleMapper_1_0;
import org.openmdx.uses.org.apache.commons.collections.MapUtils;

public class DataLoader
    extends Loader {

  //-------------------------------------------------------------------------
  public DataLoader(
      ServletContext context,
      RoleMapper_1_0 roleMapper,            
      Dataprovider_1_0 connection
  ) {
      super(
          context,
          roleMapper
      );
      this.connection = connection;
  }
    
    //-------------------------------------------------------------------------
    synchronized public void loadData(
        String location
    ) throws ServiceException {
        System.out.println("Loading data");      
        List dirs = this.getDirectories("/WEB-INF/config/" + location + "/");
        // Iterate all data directories. Each directory my contain
        // segment-specific data files
        for(
            Iterator i = dirs.iterator(); 
            i.hasNext(); 
        ) {
            String dir = (String)i.next();
            Set resourcePaths = context.getResourcePaths(dir);
            if(resourcePaths == null) return;
            resourcePaths = new TreeSet(resourcePaths);
            try {
                for(Iterator j = resourcePaths.iterator(); j.hasNext(); ) {        
                    String path = (String)j.next();
                    Map data = MapUtils.orderedMap(new HashMap());
                    XmlImporter importer = new XmlImporter(
                        data,
                        false
                    );
                    if(!path.endsWith("/")) {
                        System.out.println("Loading " + path);
                        try {
                            importer.process(
                                new String[]{context.getResource(path).toString()}
                            );
                        }
                        catch(ServiceException e) {
                            e.log();
                            System.out.println("STATUS: " + e.getMessage());
                        }
                    }
                    // storing data
                    System.out.println("Storing " + data.size() + " objects");
                    RequestCollection store = new RequestCollection(
                        new ServiceHeader(this.getAdminPrincipal(dir), null, false, new QualityOfService()),
                        this.connection
                    );
                    int kk = 0;
                    for(
                        Iterator k = data.values().iterator(); 
                        k.hasNext();
                        kk++
                    ) {
                        if((kk > 0) && (kk % 100 == 0)) {
                            System.out.println("Stored " + kk);
                        }
                        DataproviderObject entry = (DataproviderObject)k.next();
                        // create new entries, replace existing
                        try {
                            DataproviderObject existing = null;
                            try {
                                existing = new DataproviderObject(
                                    store.addGetRequest(
                                        entry.path()
                                    )
                                );
                            }
                            catch(ServiceException e) {}
                            if(existing != null) {
                                DataproviderObject existingOrig = new DataproviderObject(existing);
                                existing.addClones(entry, true);
                                if(!existing.equals(existingOrig)) {
                                    this.removeTrailingEmptyStrings(existing);
                                    if("bootstrap".equals(location)) {
                                        System.out.println("Replacing " + existing.path());
                                    }
                                    store.addReplaceRequest(existing);
                                }
                            }
                            else {
                                this.removeTrailingEmptyStrings(entry);
                                if("bootstrap".equals(location)) {
                                    System.out.println("Creating " + entry.path());
                                }
                                store.addCreateRequest(
                                    entry
                                );            
                            }
                        }
                        catch(ServiceException e) {
                            e.log();
                            System.out.println("STATUS: " + e.getMessage() + " (for more info see log)");
                        }
                    }
                }
            }
            catch(MalformedURLException e) {
                throw new ServiceException(e);
            }
        }
        System.out.println("Done");         
    }

    //-------------------------------------------------------------------------
    private final Dataprovider_1_0 connection;
  
}

//--- End of File -----------------------------------------------------------
