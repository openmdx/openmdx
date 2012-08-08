/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: CodesLoader.java,v 1.10 2008/05/05 22:28:25 wfro Exp $
 * Description: TextsLoader class
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/05 22:28:25 $
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jmi.reflect.RefObject;
import javax.servlet.ServletContext;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.compatibility.base.accessor.jmi.JmiHelper;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.importer.xml.XmlImporter;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.portal.servlet.RoleMapper_1_0;
import org.openmdx.uses.org.apache.commons.collections.MapUtils;

public class CodesLoader
    extends Loader {

  //-------------------------------------------------------------------------
  public CodesLoader(      
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
    synchronized public void loadCodes(
        String[] locale
    ) throws ServiceException {
        System.out.println("Loading codes");
        List dirs = this.getDirectories("/WEB-INF/config/code/");
        // Iterate all code directories. Each directory may contain segment 
        // and locale specific code files
        for(
            Iterator i = dirs.iterator(); 
            i.hasNext(); 
        ) {
            String dir = (String)i.next();        
            Map mergedCodes = MapUtils.orderedMap(new HashMap());
            int fallbackLocaleIndex = 0;
            // Iterate all configured locales
            for(int j = 0; j < locale.length; j++) {
              fallbackLocaleIndex = 0;
              Set codeResources = new TreeSet();
              if(locale[j] != null) {
                  String codeResourcesPath = dir + locale[j];
                  codeResources = new TreeSet(context.getResourcePaths(codeResourcesPath));
                  if(codeResources == null) {
                      for(int k = j-1; k >= 0; k--) {
                          if(locale[j].substring(0,2).equals(locale[k].substring(0,2))) {
                              fallbackLocaleIndex = k;
                              break;
                          }
                      }
                      System.out.println(locale[j] + " not found. Fallback to " + locale[fallbackLocaleIndex]);
                  }
              }
              try {
                for(Iterator k = codeResources.iterator(); k.hasNext(); ) {        
                    Map codes = MapUtils.orderedMap(new HashMap());
                    String path = (String)k.next();
                    if(!path.endsWith("/")) {
                        System.out.println("Loading " + path);
                        try {
                            XmlImporter importer = new XmlImporter(
                                codes,
                                false
                            );
                            importer.process(
                                new String[]{context.getResource(path).toString()}
                            );
                        }
                        catch(ServiceException e) {
                            e.log();
                            System.out.println("STATUS: " + e.getMessage());
                        }
                    }
                    // merge entries
                    Set entrySet = j == 0 ? codes.entrySet() : mergedCodes.entrySet();
                    for(Iterator l = entrySet.iterator(); l.hasNext(); ) {
                        Entry e = (Entry)l.next();
                        // merge entry
                        if(
                            (j > 0) &&
                            (mergedCodes.get(e.getKey()) != null)
                        ) {                                                            
                            DataproviderObject mergedCodeEntry = (DataproviderObject)mergedCodes.get(e.getKey());
                                                
                            // do not merge if shortText for locale 0 is not set
                            if(mergedCodeEntry.getValues("shortText") != null) {
                                if(mergedCodeEntry.values("shortText").size() <= j) {
                                    mergedCodeEntry.values("shortText").add(
                                        mergedCodeEntry.values("shortText").get(fallbackLocaleIndex)
                                    );
                                }
                                // assert that longText is set for locale i
                                if(mergedCodeEntry.getValues("longText") == null) {
                                    mergedCodeEntry.values("longText").add("N/A");
                                }
                                if(mergedCodeEntry.values("longText").size() <= j) {
                                    mergedCodeEntry.values("longText").add(
                                        mergedCodeEntry.values("longText").get(fallbackLocaleIndex)
                                    );
                                }
                                  
                                // overwrite shortText and longText with uiElement values if available
                                DataproviderObject codeEntry = (DataproviderObject)codes.get(e.getKey());
                                if(codeEntry != null) {
                                    if(codeEntry.values("shortText").size() > 0) {
                                        mergedCodeEntry.values("shortText").set(
                                            j,
                                            codeEntry.values("shortText").get(0)
                                        );
                                    }
                                    if(codeEntry.values("longText").size() > 0) {
                                        mergedCodeEntry.values("longText").set(
                                            j,
                                            codeEntry.values("longText").get(0)
                                        );
                                    }
                                 }
                            }
                          }
                          // add if it does not exist. Only add for locale=0 (en_US)
                          else if(j == 0) {
                              mergedCodes.put(
                                  e.getKey(),
                                  e.getValue()
                              );
                        }
                        // locale > 0 requires that locale=0 exists. Complain if it
                        // does not
                        else {
                            AppLog.warning("entry " + e.getKey() + " of locale " + locale[j] + " has no corresponding entry for locale " + locale[0] + ". Not loading");
                        }
                    }
                }
              }
              catch(MalformedURLException e) {
                  throw new ServiceException(e);
              }
            }
            
            // Store merged codes
            System.out.println("Storing " + mergedCodes.size() + " code entries");
            PersistenceManager store = this.pmf.getPersistenceManager(
                this.getAdminPrincipal(dir),
                null
            );
            Set codeSegmentIdentities = new HashSet();
            // Load objects in multiple runs in order to resolve object dependencies.
            Map<Path,RefObject> loadedObjects = new HashMap<Path,RefObject>(); 
            for(int runs = 0; runs < 5; runs++) {
                boolean hasNewObjects = false;
                store.currentTransaction().begin();
                for(
                    Iterator j = mergedCodes.values().iterator(); 
                    j.hasNext(); 
                ) {
                  DataproviderObject entry = (DataproviderObject)j.next();
                  codeSegmentIdentities.add(
                      entry.path().getPrefix(5)
                  );
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
                            entry.path(), 
                            existing
                        );
                        JmiHelper.toRefObject(
                            entry,
                            existing,
                            loadedObjects, // object cache
                            store,
                            true, // replace values
                            true // remove trailing empty string
                        );
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
                        } catch(Exception e) {}
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
        System.out.println("Done");
    }

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private final PersistenceManagerFactory pmf;
  
}

//--- End of File -----------------------------------------------------------
