/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: UiLoader.java,v 1.52 2009/06/13 18:48:08 wfro Exp $
 * Description: UiLoader
 * Revision:    $Revision: 1.52 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/13 18:48:08 $
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.servlet.ServletContext;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.importer.XmlImporter;
import org.openmdx.application.dataprovider.spi.Dataprovider_2Connection;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.accessor.rest.DataObjectManagerFactory_1;
import org.openmdx.base.accessor.view.ViewManager_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.ConnectionAdapter;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.portal.servlet.RoleMapper_1_0;

public class UiLoader
    extends Loader {

    //-------------------------------------------------------------------------
    public UiLoader(
        ServletContext context,
        RoleMapper_1_0 roleMapper,
        Model_1_0 model,
        Path providerPath
    ) throws ServiceException {
        super(
            context,
            roleMapper
        );
        this.providerPath = providerPath;
        this.uiRepository = UiLoader.createUiRepository(model);      
    }
  
    //-------------------------------------------------------------------------
    protected static org.openmdx.ui1.jmi1.Ui1Package getUiPackage(
        PersistenceManager pm
    ) {
        org.openmdx.ui1.jmi1.Ui1Package uiPkg = null;
        try {
            uiPkg = (org.openmdx.ui1.jmi1.Ui1Package)((RefObject)pm.newInstance(org.openmdx.ui1.jmi1.Segment.class)).refImmediatePackage();
        }
        catch(UnsupportedOperationException e) {        
            uiPkg = (org.openmdx.ui1.jmi1.Ui1Package)((Authority)pm.getObjectById(
                Authority.class,
                org.openmdx.ui1.jmi1.Ui1Package.AUTHORITY_XRI
            )).refImmediatePackage();
        }
        return uiPkg;
    }
    
    //-------------------------------------------------------------------------
    protected static class Dataprovider
        extends Layer_1 {

        public Dataprovider(
            List delegates
        ) throws Exception {
            this.delegates = delegates;
            this.activate(
                (short)5, 
                new Configuration(), 
                (Layer_1_0)delegates.get(0)
            );
        }

        public void deactivate(
        ) throws Exception {
            for(Iterator i = this.delegates.iterator(); i.hasNext(); ) {
                ((Layer_1_0)i.next()).deactivate();
            }          
        }
    
        private final List delegates;
    }
      
    //-------------------------------------------------------------------------
    /**
     * Constructs an in-memory ui repository
     */
    protected static Layer_1 createUiRepository(
        Model_1_0 model
    ) throws ServiceException {
        try {
            Configuration configuration = new Configuration();
            configuration.values("namespaceId").add("ui");
            Layer_1_0 persistencePlugin = (Layer_1_0)Classes.getApplicationClass(
                org.openmdx.application.dataprovider.layer.persistence.none.InMemory_1.class.getName()
            ).newInstance();
            persistencePlugin.activate((short)0, configuration, null);
            Layer_1_0 modelPlugin = (Layer_1_0)Classes.getApplicationClass(
                org.openmdx.application.dataprovider.layer.model.Standard_1.class.getName()
            ).newInstance();
            modelPlugin.activate((short)1, configuration, persistencePlugin);
            Layer_1_0 applicationPlugin = (Layer_1_0)Classes.getApplicationClass(
                org.openmdx.ui1.layer.application.Ui_1.class.getName()
            ).newInstance();
            applicationPlugin.activate((short)2, configuration, modelPlugin);
            Layer_1_0 typePlugin = (Layer_1_0)Classes.getApplicationClass(
                org.openmdx.application.dataprovider.layer.type.Strict_1.class.getName()
            ).newInstance();
            typePlugin.activate((short)3, configuration, applicationPlugin);
            Layer_1_0 interceptionPlugin = (Layer_1_0)Classes.getApplicationClass(
                org.openmdx.application.dataprovider.layer.interception.Standard_1.class.getName()
            ).newInstance();
            Configuration interceptionConfiguration = new Configuration(configuration);
            interceptionConfiguration.values("propagateSet").add(Boolean.TRUE);
            interceptionPlugin.activate((short)4, interceptionConfiguration, typePlugin);
       
            return new Dataprovider(
                Arrays.asList(
                    new Layer_1_0[]{
                        interceptionPlugin,
                        typePlugin,
                        applicationPlugin,
                        modelPlugin,
                        persistencePlugin
                    }
                )
            );
        }
        catch(ServiceException e) {
            throw e;
        }
        catch(Exception e) {
            throw new RuntimeServiceException(e);
        }
    }
    
    //-------------------------------------------------------------------------
    public PersistenceManager getRepository(
    ) throws ServiceException {
        if(this.pm == null) {
        	DataObjectManagerFactory_1 pmf = new DataObjectManagerFactory_1(
        		new ArrayList<String>(),
        		ConnectionAdapter.newInstance(
        			new Dataprovider_2Connection(this.uiRepository)
        		),
        		null
        	);
        	DataObjectManager_1_0 connection = pmf.getPersistenceManager();
            ViewManager_1 manager = new ViewManager_1(connection);
            RefRootPackage_1 rootPkg = new RefRootPackage_1(
                manager,
                true
            );
            this.pm = rootPkg.refPersistenceManager();
        }
        return this.pm;
    }
        
    //-------------------------------------------------------------------------
    /**
     * load ui config in case the ui config resources have changed
     */
    @SuppressWarnings("unchecked")
    public List<Path> load(
        String[] locale
    ) throws ServiceException {
        Map<String,Path> loadedUiSegments = new TreeMap<String,Path>();
        List dirs = this.getDirectories("/WEB-INF/config/ui/");
        for(
            Iterator i = dirs.iterator(); 
            i.hasNext(); 
        ) {
            String dir = (String)i.next();        
    
            // Check if any ui file was updated since last refresh
            long crc = 0;
            for(int j = 0; j < locale.length; j++) {
                if(locale[j] != null) {
                    crc += this.getCRCForResourcePath(
                      context,
                      dir + locale[j]
                    );
                }
            }
            if(
              (this.uiCRC.get(dir) == null) || 
              (((Long)this.uiCRC.get(dir)).longValue() != crc)
            ) {
              Map<Path,MappedRecord> mergedUiElements = new LinkedHashMap<Path,MappedRecord>();
              int fallbackLocaleIndex = 0;
              for(int j = 0; j < locale.length; j++) {
                fallbackLocaleIndex = 0;
                Set uiResources = null;
                if(locale[j] != null) {
                    String uiResourcesPath = dir + locale[j];
                    uiResources = context.getResourcePaths(uiResourcesPath);
                    // fall back to most recently loaded locale which matches the language
                    // if not found fall back to locale[0]
                    if(uiResources == null) {
                        for(int k = j-1; k >= 0; k--) {
                            if(
                                (locale[k] != null) && 
                                (locale[j].substring(0,2).equals(locale[k].substring(0,2)))
                            ) {
                                fallbackLocaleIndex = k;
                                break;
                            }
                        }
                        System.out.println(locale[j] + " not found. Fallback to " + locale[fallbackLocaleIndex]);
                    } 
                    else {
                    	uiResources = new TreeSet(uiResources);
                    }
                }
                try {
                  if(uiResources == null) {
                      uiResources = new TreeSet();
                      uiResources.add("./"); // empty dir
                  }
                  else {
                      System.out.println("Loading " + uiResources.size() + " ui files for locale " + locale[j]);
                  }                  
                  for(
                      Iterator k = uiResources.iterator(); 
                      k.hasNext(); 
                  ) {        
                      Map<Path,MappedRecord> uiElements = new LinkedHashMap<Path,MappedRecord>();
                      String path = (String)k.next();
                      if(!path.endsWith("/")) {
                          AppLog.detail("Loading", path);
                          try {
                              XmlImporter importer = new XmlImporter(
                                  uiElements,
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
                      // Merge entries
                      Set entrySet = j == 0 ? uiElements.entrySet() : mergedUiElements.entrySet();
                      for(Iterator l = entrySet.iterator(); l.hasNext(); ) {
                          Entry<Path,MappedRecord> e = (Entry)l.next();
                          // merge entry
                          if(
                              (j > 0) &&
                              (mergedUiElements.get(e.getKey()) != null)
                          ) {                      
                              MappedRecord mergedUiElement = mergedUiElements.get(e.getKey());   
                              ObjectHolder_2Facade mergedUiElementFacade;
                              try {
	                              mergedUiElementFacade = ObjectHolder_2Facade.newInstance(mergedUiElement);
                              }
                              catch(ResourceException e0) {
                            	  throw new ServiceException(e0);
                              }
                              // do not merge if label for locale 0 is not set
                              if(mergedUiElementFacade.getAttributeValues("label") != null) {
                                  if(mergedUiElementFacade.attributeValues("label").size() <= j) {
                                	  mergedUiElementFacade.attributeValues("label").add(
                                		  mergedUiElementFacade.attributeValues("label").get(fallbackLocaleIndex)
                                      );
                                  }
                                  // assert that shortLabel is set for locale i. 
                                  if(mergedUiElementFacade.getAttributeValues("shortLabel") == null) {
                                	  mergedUiElementFacade.attributeValues("shortLabel").add("");                                     
                                  }
                                  if(mergedUiElementFacade.attributeValues("shortLabel").size() <= j) {
                                	  mergedUiElementFacade.attributeValues("shortLabel").add(
                                		  mergedUiElementFacade.attributeValues("shortLabel").get(fallbackLocaleIndex)
                                      );
                                  }
                                  // assert that toolTip is set for locale i
                                  if(mergedUiElementFacade.getAttributeValues("toolTip") == null) {
                                	  mergedUiElementFacade.attributeValues("toolTip").add("");
                                  }
                                  if(mergedUiElementFacade.attributeValues("toolTip").size() <= j) {
                                	  mergedUiElementFacade.attributeValues("toolTip").add(
                                		  mergedUiElementFacade.attributeValues("toolTip").get(fallbackLocaleIndex)
                                      );
                                  }                                 
                                  // overwrite label, shortLabel and toolTip with uiElement values if available
                                  MappedRecord uiElement = uiElements.get(e.getKey());
                                  ObjectHolder_2Facade uiElementFacade;
                                  try {
	                                  uiElementFacade = ObjectHolder_2Facade.newInstance(uiElement);
                                  }
                                  catch (ResourceException e0) {
                                	  throw new ServiceException(e0);
                                  }
                                  if(uiElement != null) {
                                      if(!uiElementFacade.attributeValues("label").isEmpty()) {
                                          mergedUiElementFacade.attributeValues("label").set(
                                              j,
                                              uiElementFacade.attributeValue("label")
                                          );
                                      }
                                      if(!uiElementFacade.attributeValues("shortLabel").isEmpty()) {
                                          mergedUiElementFacade.attributeValues("shortLabel").set(
                                              j,
                                              uiElementFacade.attributeValue("shortLabel")
                                          );
                                      }
                                      // Take label of same locale as shortLabel fallback 
                                      // Do not take shortLabel of fallback locale 
                                      else {
                                          mergedUiElementFacade.attributeValues("shortLabel").set(
                                              j,
                                              mergedUiElementFacade.attributeValues("label").get(j)
                                          );                                      
                                      }
                                      if(!uiElementFacade.attributeValues("toolTip").isEmpty()) {
                                          mergedUiElementFacade.attributeValues("toolTip").set(
                                              j,
                                              uiElementFacade.attributeValue("toolTip")
                                          );
                                      }
                                  }
                              }
                          }
                          // add if it does not exist. Only add for locale=0 (en_US)
                          else if(j == 0) {
                              mergedUiElements.put(
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
              // Store ui elements. First remove existing elements
              try {
                  String[] segmentName = this.getSegmentName(dir);
                  RequestCollection store = new RequestCollection(
                      new ServiceHeader(),
                      this.uiRepository
                  );
                  // Assert existence of provider
                  try {
                	  MappedRecord uiProvider = ObjectHolder_2Facade.newInstance(
                		  this.providerPath,
                		  "org:openmdx:base:Provider"
                	  ).getDelegate();
                	  store.addCreateRequest(uiProvider);
                  }
                  catch(Exception e) {}
                  // Remove existing ui config
                  Path uiSegmentPath = this.providerPath.getDescendant("segment", segmentName[segmentName.length-1]);
                  try {
                	  store.addRemoveRequest(uiSegmentPath);
                  }
                  catch(Exception e) {}
                  // Re-create segment
                  try {
                	  MappedRecord uiSegment = ObjectHolder_2Facade.newInstance(
                		  uiSegmentPath,
                		  "org:openmdx:ui1:Segment"
                	  ).getDelegate();
                	  store.addCreateRequest(uiSegment);
                  }
                  catch(Exception e) {}
                  // Store ui elements
                  System.out.println("Storing " + mergedUiElements.size() + " ui elements");
                  store.beginBatch();
                  for(Iterator<MappedRecord> j = mergedUiElements.values().iterator(); j.hasNext(); ) {
                      MappedRecord uiElement = j.next();
                      store.addCreateRequest(
                          uiElement
                      );
                  }
                  store.endBatch();            
                  loadedUiSegments.put(
                      segmentName[0],
                      this.providerPath.getDescendant("segment", segmentName[segmentName.length-1])
                  );
              }
              catch(ServiceException e) {
                  AppLog.error("can not create ui config", e.getMessage());
              }              
              this.uiCRC.put(
                  dir,
                  new Long(crc)
              );
          }
        }    
        return new ArrayList<Path>(loadedUiSegments.values());
    }

    //-------------------------------------------------------------------------
    private Map uiCRC = new HashMap();
    private final Layer_1 uiRepository;
    private transient PersistenceManager pm = null;
    private final Path providerPath;
  
}

//--- End of File -----------------------------------------------------------
