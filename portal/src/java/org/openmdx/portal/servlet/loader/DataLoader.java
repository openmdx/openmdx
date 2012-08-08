/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: DataLoader.java,v 1.14 2009/03/08 18:03:21 wfro Exp $
 * Description: DataLoader
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:03:21 $
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

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jmi.reflect.RefObject;
import javax.servlet.ServletContext;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.naming.Path;
import org.openmdx.compatibility.base.accessor.jmi.JmiHelper;
import org.openmdx.compatibility.base.dataprovider.importer.xml.XmlImporter;
import org.openmdx.portal.servlet.RoleMapper_1_0;
import org.openmdx.uses.org.apache.commons.collections.MapUtils;

public class DataLoader
    extends Loader {

  //-------------------------------------------------------------------------
  public DataLoader(
      ServletContext context,
      RoleMapper_1_0 roleMapper,            
      PersistenceManagerFactory pmf
  ) {
      super(
          context,
          roleMapper
      );
      this.pmf = pmf;
  }
    
    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
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
                    PersistenceManager store = this.pmf.getPersistenceManager(
                        this.getAdminPrincipal(dir),
                        null
                    );
                    // Load objects in multiple runs in order to resolve object dependencies.       
                    Map<Path,RefObject> loadedObjects = new HashMap<Path,RefObject>();
                    for(int runs = 0; runs < 5; runs++) {
                        boolean hasNewObjects = false;
                        store.currentTransaction().begin();
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
                            // create new entries, update existing
                            try {
                                RefObject_1_0 existing = null;
                                try {
                                    existing = (RefObject_1_0)store.getObjectById(
                                        entry.path()
                                    );
                                }
                                catch(Exception e) {}
                                if(existing != null) {
                                    loadedObjects.put(
                                        existing.refGetPath(), 
                                        existing
                                    );                                    
                                    boolean modified = JmiHelper.toRefObject(
                                        entry,
                                        existing,
                                        loadedObjects, // object cache
                                        store,
                                        true, // replace values
                                        true // remove trailing empty string
                                    );
                                    if(modified) {
                                        if("bootstrap".equals(location)) {
                                            System.out.println("Updating " + existing.refGetPath());
                                        }
                                    }
                                }
                                else {
                                    String qualifiedClassName = (String)entry.values(SystemAttributes.OBJECT_CLASS).get(0);
                                    String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(':'));
                                    RefObject_1_0 newEntry = (RefObject_1_0)((org.openmdx.base.jmi1.Authority)store.getObjectById(
                                        Authority.class,
                                        "xri://@openmdx*" + packageName.replace(":", ".")
                                    )).refImmediatePackage().refClass(qualifiedClassName).refCreateInstance(null);
                                    newEntry.refInitialize(false, false);
                                    JmiHelper.toRefObject(
                                        entry,
                                        newEntry,
                                        loadedObjects, // object cache
                                        store,
                                        true, // replace values
                                        true // remove trailing empty string
                                    );
                                    Path parentIdentity = entry.path().getParent().getParent();
                                    RefObject_1_0 parent = null;
                                    try {
                                        parent = loadedObjects.containsKey(parentIdentity)
                                            ? (RefObject_1_0)loadedObjects.get(parentIdentity)
                                            : (RefObject_1_0)store.getObjectById(parentIdentity);
                                    } 
                                    catch(Exception e) {}
                                    if(parent != null) {
                                        RefContainer container = (RefContainer)parent.refGetValue(
                                            entry.path().get(entry.path().size() - 2)
                                        );
                                        container.refAdd(
                                            QualifierType.REASSIGNABLE,
                                            entry.path().get(entry.path().size() - 1), 
                                            newEntry
                                        );
                                    }                                    
                                    if("bootstrap".equals(location)) {
                                        System.out.println("Creating " + entry.path());
                                    }
                                    loadedObjects.put(
                                        entry.path(), 
                                        newEntry
                                    );                                    
                                    hasNewObjects = true;
                                }
                            }
                            catch(Exception e) {
                                new ServiceException(e).log();
                                System.out.println("STATUS: " + e.getMessage() + " (for more info see log)");
                            }
                        }
                        store.currentTransaction().commit();
                        if(!hasNewObjects) break;
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
    private final PersistenceManagerFactory pmf;
  
}

//--- End of File -----------------------------------------------------------
