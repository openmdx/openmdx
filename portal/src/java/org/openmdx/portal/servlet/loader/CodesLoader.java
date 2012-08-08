/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: CodesLoader.java,v 1.31 2010/01/25 17:03:40 wfro Exp $
 * Description: TextsLoader class
 * Revision:    $Revision: 1.31 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/25 17:03:40 $
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jmi.reflect.RefObject;
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.servlet.ServletContext;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.dataprovider.cci.JmiHelper;
import org.openmdx.application.xml.Importer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.RoleMapper_1_0;

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
    	String messagePrefix = new Date() + "  ";
        System.out.println(messagePrefix + "Loading codes");
        SysLog.info("Loading codes");
        List dirs = this.getDirectories("/WEB-INF/config/code/");
        // Iterate all code directories. Each directory may contain segment 
        // and locale specific code files
        for(
            Iterator i = dirs.iterator(); 
            i.hasNext(); 
        ) {
            String dir = (String)i.next();        
            Map<Path,MappedRecord> mergedCodes = new LinkedHashMap<Path,MappedRecord>();
            int fallbackLocaleIndex = 0;
            // Iterate all configured locales
            for(int j = 0; j < locale.length; j++) {
              fallbackLocaleIndex = 0;
              Set<String> codeResources = new TreeSet<String>();
              if(locale[j] != null) {
                  String codeResourcesPath = dir + locale[j];
                  codeResources = new TreeSet<String>(this.context.getResourcePaths(codeResourcesPath));
                  if(codeResources == null) {
                      for(int k = j-1; k >= 0; k--) {
                          if(locale[j].substring(0,2).equals(locale[k].substring(0,2))) {
                              fallbackLocaleIndex = k;
                              break;
                          }
                      }
                      SysLog.info(locale[j] + " not found. Fallback to " + locale[fallbackLocaleIndex]);
                  }
              }
              try {
                for(Iterator k = codeResources.iterator(); k.hasNext(); ) {        
                    Map<Path,MappedRecord> codes = new LinkedHashMap<Path,MappedRecord>();
                    String path = (String)k.next();
                    if(!path.endsWith("/")) {
                        SysLog.info("Loading " + path);
                        try {
                            Importer.importObjects(
                                Importer.asTarget(codes),
                                Importer.asSource(context.getResource(path))
                             );
                        }
                        catch(ServiceException e) {
                            e.log();
                            SysLog.info("STATUS: " + e.getMessage());
                        }
                    }
                    // merge entries
                    Set<Entry<Path,MappedRecord>> entrySet = j == 0 ? 
                    	codes.entrySet() : 
                    	mergedCodes.entrySet();
                    for(Iterator<Entry<Path,MappedRecord>> l = entrySet.iterator(); l.hasNext(); ) {
                        Entry<Path,MappedRecord> e = l.next();
                        // merge entry
                        if(
                            (j > 0) &&
                            (mergedCodes.get(e.getKey()) != null)
                        ) {                                                            
                        	MappedRecord mergedCodeEntry = mergedCodes.get(e.getKey());
                            Object_2Facade mergedCodeEntryFacade;
                            try {
	                            mergedCodeEntryFacade = Object_2Facade.newInstance(mergedCodeEntry);
                            }
                            catch (ResourceException e0) {
                            	throw new ServiceException(e0);
                            }
                            // do not merge if shortText for locale 0 is not set
                            if("org:opencrx:kernel:code1:CodeValueEntry".equals(mergedCodeEntryFacade.getObjectClass())) {
                                if(mergedCodeEntryFacade.attributeValuesAsList("shortText").size() <= j) {
                                	if(fallbackLocaleIndex < mergedCodeEntryFacade.attributeValuesAsList("shortText").size()) {
	                                	mergedCodeEntryFacade.attributeValuesAsList("shortText").add(
	                                		mergedCodeEntryFacade.attributeValuesAsList("shortText").get(fallbackLocaleIndex)
	                                    );
                                	}
                                }
                                // assert that longText is set for locale i
                                if(mergedCodeEntryFacade.getAttributeValues("longText") == null) {
                                	mergedCodeEntryFacade.attributeValuesAsList("longText").add("N/A");
                                }
                                if(mergedCodeEntryFacade.attributeValuesAsList("longText").size() <= j) {
                                	mergedCodeEntryFacade.attributeValuesAsList("longText").add(
                                		mergedCodeEntryFacade.attributeValuesAsList("longText").get(fallbackLocaleIndex)
                                    );
                                }                                  
                                // overwrite shortText and longText with uiElement values if available
                                MappedRecord codeEntry = codes.get(e.getKey());
                                if(codeEntry != null) {
                                    Object_2Facade codeEntryFacade;
                                    try {
	                                    codeEntryFacade = Object_2Facade.newInstance(codeEntry);
                                    }
                                    catch (ResourceException e0) {
                                    	throw new ServiceException(e0);
                                    }
                                    if(!codeEntryFacade.attributeValuesAsList("shortText").isEmpty()) {
                                    	while(mergedCodeEntryFacade.attributeValuesAsList("shortText").size() <= j) {
                                    		mergedCodeEntryFacade.attributeValuesAsList("shortText").add(
                                                codeEntryFacade.attributeValue("shortText")
                                    	    );
                                    	}
                                    	mergedCodeEntryFacade.attributeValuesAsList("shortText").set(
                                            j,
                                            codeEntryFacade.attributeValue("shortText")
                                        );
                                    }
                                    if(!codeEntryFacade.attributeValuesAsList("longText").isEmpty()) {
                                    	while(mergedCodeEntryFacade.attributeValuesAsList("longText").size() <= j) {
                                    		mergedCodeEntryFacade.attributeValuesAsList("longText").add(
                                                codeEntryFacade.attributeValue("longText")
                                    	    );
                                    	}
                                    	mergedCodeEntryFacade.attributeValuesAsList("longText").set(
                                            j,
                                            codeEntryFacade.attributeValue("longText")
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
                        	SysLog.warning("entry " + e.getKey() + " of locale " + locale[j] + " has no corresponding entry for locale " + locale[0] + ". Not loading");
                        }
                    }
                }
              }
              catch(MalformedURLException e) {
                  throw new ServiceException(e);
              }
            }
            
            // Store merged codes
            SysLog.info("Storing " + mergedCodes.size() + " code entries");
            System.out.println(messagePrefix + "Storing " + mergedCodes.size() + " code entries");
            PersistenceManager store = this.pmf.getPersistenceManager(
                this.getAdminPrincipal(dir),
                null
            );
            Set<Path> codeSegmentIdentities = new HashSet<Path>();
            // Load objects in multiple runs in order to resolve object dependencies.
            Map<Path,RefObject> loadedObjects = new HashMap<Path,RefObject>(); 
            for(int runs = 0; runs < 5; runs++) {
                boolean hasNewObjects = false;
                store.currentTransaction().begin();
                for(
                    Iterator<MappedRecord> j = mergedCodes.values().iterator(); 
                    j.hasNext(); 
                ) {
                  MappedRecord entry = j.next();
                  Path entryPath = Object_2Facade.getPath(entry);
                  codeSegmentIdentities.add(
                	  entryPath.getPrefix(5)
                  );
                  // create new entries, update existing
                  try {
                    RefObject_1_0 existing = null;
                    try {
                      existing = (RefObject_1_0)store.getObjectById(
                    	  entryPath
                      );
                    }
                    catch(Exception e) {}
                    if(existing != null) {
                        loadedObjects.put(
                        	entryPath, 
                            existing
                        );
                        JmiHelper.toRefObject(
                            entry,
                            existing,
                            loadedObjects, // object cache
                            null // ignorable features
                        );
                    }
                    else {
                        String qualifiedClassName = Object_2Facade.getObjectClass(entry);
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
                            null // ignorable features
                        );
                        Path parentIdentity = entryPath.getParent().getParent();
                        RefObject_1_0 parent = null;
                        try {
                            parent = loadedObjects.containsKey(parentIdentity) ? 
                            	(RefObject_1_0)loadedObjects.get(parentIdentity) : 
                            	(RefObject_1_0)store.getObjectById(parentIdentity);
                        } 
                        catch(Exception e) {}
                        if(parent != null) {
                            RefContainer container = (RefContainer)parent.refGetValue(
                            	entryPath.get(entryPath.size() - 2)
                            );
                            container.refAdd(
                                QualifierType.REASSIGNABLE,
                                entryPath.get(entryPath.size() - 1),
                                newEntry
                            );
                        }
                        loadedObjects.put(
                        	entryPath, 
                            newEntry
                        );
                        hasNewObjects = true;
                    }
                  }
                  catch(Exception e) {
                    new ServiceException(e).log();
                    System.out.println(messagePrefix + "STATUS: " + e.getMessage() + " (for more info see log)");
                  }
                }
                store.currentTransaction().commit();
                if(!hasNewObjects) break;
            }
        }
        SysLog.info("Done");
        System.out.println(messagePrefix + "Done");
    }

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private final PersistenceManagerFactory pmf;
  
}

//--- End of File -----------------------------------------------------------
