/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: UiLoader.java,v 1.15 2008/02/12 19:32:19 wfro Exp $
 * Description: UiLoader
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/12 19:32:19 $
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletContext;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.importer.xml.XmlImporter;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Provider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.mapping.Names;
import org.openmdx.portal.servlet.RoleMapper_1_0;
import org.openmdx.uses.org.apache.commons.collections.MapUtils;

public class UiLoader
    extends Loader {

    //-------------------------------------------------------------------------
    public UiLoader(
        ServletContext context,
        RoleMapper_1_0 roleMapper,
        Model_1_3 model,
        Path providerPath
    ) throws ServiceException {
        super(
            context,
            roleMapper
        );
        this.providerPath = providerPath;
        this.uiRepository = createUiRepository(model);      
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
        Model_1_3 model
    ) throws ServiceException {
        try {
            Configuration configuration = new Configuration();
            configuration.values("namespaceId").add("ui");
            configuration.values(SharedConfigurationEntries.MODEL).add(model);
            Layer_1_0 persistencePlugin = (Layer_1_0)Classes.getApplicationClass(
                "org.openmdx.compatibility.base.dataprovider.layer.persistence.none.InMemory_1"
            ).newInstance();
            persistencePlugin.activate((short)0, configuration, null);
            Layer_1_0 modelPlugin = (Layer_1_0)Classes.getApplicationClass(
                "org.openmdx.model1.layer.model.Model_1"
            ).newInstance();
            modelPlugin.activate((short)1, configuration, persistencePlugin);
            Layer_1_0 applicationPlugin = (Layer_1_0)Classes.getApplicationClass(
                "org.openmdx.ui1.layer.application.Ui_1"
            ).newInstance();
            applicationPlugin.activate((short)2, configuration, modelPlugin);
            Layer_1_0 typePlugin = (Layer_1_0)Classes.getApplicationClass(
                "org.openmdx.model1.layer.type.Model_1"
            ).newInstance();
            typePlugin.activate((short)3, configuration, applicationPlugin);
            Layer_1_0 interceptionPlugin = (Layer_1_0)Classes.getApplicationClass(
                "org.openmdx.compatibility.base.dataprovider.layer.interception.Standard_1"
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
            Provider_1_0 dataprovider = new Provider_1(
                new RequestCollection(
                    new ServiceHeader(),
                    this.uiRepository
                ),
                false
            );
            Manager_1 manager = new Manager_1(
                new Connection_1(
                    dataprovider,
                    false
                )
            );
            RefRootPackage_1 rootPkg = new RefRootPackage_1(
                manager,
                Names.JMI1_PACKAGE_SUFFIX,
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
    public boolean load(
        String[] locale
    ) throws ServiceException {
        boolean resetSession = false;
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
              Map mergedUiElements = MapUtils.orderedMap(new HashMap());
              int fallbackLocaleIndex = 0;
              for(int j = 0; j < locale.length; j++) {
                fallbackLocaleIndex = 0;
                Set uiResources = new TreeSet();
                if(locale[j] != null) {
                    String uiResourcesPath = dir + locale[j];
                    uiResources = context.getResourcePaths(uiResourcesPath);
                    // fall back to most recently loaded locale which matches the language
                    // if not found fall back to locale[0]
                    if(uiResources == null) {
                        for(int k = j-1; k >= 0; k--) {
                            if(locale[j].substring(0,2).equals(locale[k].substring(0,2))) {
                                fallbackLocaleIndex = k;
                                break;
                            }
                        }
                        System.out.println(locale[j] + " not found. Fallback to " + locale[fallbackLocaleIndex]);
                    } else {
                    	uiResources = new TreeSet(uiResources);
                    }
                }
                try {
                  System.out.println("Loading " + uiResources.size() + " ui files for locale " + locale[j]);
                  for(Iterator k = uiResources.iterator(); k.hasNext(); ) {        
                      Map uiElements = MapUtils.orderedMap(new HashMap());
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
        
                      // merge entries
                      Set entrySet = j == 0 ? uiElements.entrySet() : mergedUiElements.entrySet();
                      for(Iterator l = entrySet.iterator(); l.hasNext(); ) {
                          Entry e = (Entry)l.next();
                          // merge entry
                          if(
                              (j > 0) &&
                              (mergedUiElements.get(e.getKey()) != null)
                          ) {                      
                              DataproviderObject mergedUiElement = (DataproviderObject)mergedUiElements.get(e.getKey());
                              
                              // do not merge if label for locale 0 is not set
                              if(mergedUiElement.getValues("label") != null) {
                                  if(mergedUiElement.values("label").size() <= j) {
                                      mergedUiElement.values("label").add(
                                          mergedUiElement.values("label").get(fallbackLocaleIndex)
                                      );
                                  }
                                  // assert that shortLabel is set for locale i. 
                                  if(mergedUiElement.getValues("shortLabel") == null) {
                                      mergedUiElement.values("shortLabel").add("");                                     
                                  }
                                  if(mergedUiElement.values("shortLabel").size() <= j) {
                                      mergedUiElement.values("shortLabel").add(
                                          mergedUiElement.values("shortLabel").get(fallbackLocaleIndex)
                                      );
                                  }
                                  // assert that toolTip is set for locale i
                                  if(mergedUiElement.getValues("toolTip") == null) {
                                      mergedUiElement.values("toolTip").add("");
                                  }
                                  if(mergedUiElement.values("toolTip").size() <= j) {
                                      mergedUiElement.values("toolTip").add(
                                          mergedUiElement.values("toolTip").get(fallbackLocaleIndex)
                                      );
                                  }
                                  
                                  // overwrite label, shortLabel and toolTip with uiElement values if available
                                  DataproviderObject uiElement = (DataproviderObject)uiElements.get(e.getKey());
                                  if(uiElement != null) {
                                      if(!uiElement.values("label").isEmpty()) {
                                          mergedUiElement.values("label").set(
                                              j,
                                              uiElement.values("label").get(0)
                                          );
                                      }
                                      if(!uiElement.values("shortLabel").isEmpty()) {
                                          mergedUiElement.values("shortLabel").set(
                                              j,
                                              uiElement.values("shortLabel").get(0)
                                          );
                                      }
                                      // Take label of same locale as shortLabel fallback 
                                      // Do not take shortLabel of fallback locale 
                                      else {
                                          mergedUiElement.values("shortLabel").set(
                                              j,
                                              mergedUiElement.values("label").get(j)
                                          );                                      
                                      }
                                      if(!uiElement.values("toolTip").isEmpty()) {
                                          mergedUiElement.values("toolTip").set(
                                              j,
                                              uiElement.values("toolTip").get(0)
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
                  PersistenceManager pm = this.getRepository();
                  org.openmdx.ui1.jmi1.Ui1Package uiPkg = (org.openmdx.ui1.jmi1.Ui1Package)((Authority)pm.getObjectById(
                      Authority.class,
                      org.openmdx.ui1.jmi1.Ui1Package.AUTHORITY_XRI
                  )).refImmediatePackage();
                  
                  String segmentName = this.getSegmentName(dir);
                  // Remove existing ui config
                  try {
                      pm.currentTransaction().begin();
                      org.openmdx.base.jmi1.Provider provider = (org.openmdx.base.jmi1.Provider)pm.getObjectById(
                          this.providerPath.toXri()
                      );
                      provider.getSegment(segmentName).refDelete();
                      pm.currentTransaction().commit();
                  }
                  catch(Exception e) {
                      try {
                          pm.currentTransaction().rollback();
                      } catch(Exception e0) {}
                  }    
                  // Re-create segment
                  try {
                    pm.currentTransaction().begin();
                    org.openmdx.base.jmi1.Provider provider = (org.openmdx.base.jmi1.Provider)pm.getObjectById(
                        this.providerPath.toXri()
                    );
                    provider.addSegment(
                        false,
                        segmentName,
                        uiPkg.getSegment().createSegment()
                    );
                    pm.currentTransaction().commit();
                  }
                  catch(Exception e) {
                      try {
                          pm.currentTransaction().rollback();
                      } catch(Exception e0) {}
                  }    
                  // Store ui elements
                  System.out.println("Storing " + mergedUiElements.size() + " ui elements");
                  RequestCollection store = new RequestCollection(
                      new ServiceHeader(),
                      this.uiRepository
                  );
                  store.beginBatch();
                  for(Iterator j = mergedUiElements.values().iterator(); j.hasNext(); ) {
                      DataproviderObject uiElement = (DataproviderObject)j.next();
                      store.addCreateRequest(
                          uiElement
                      );
                  }
                  store.endBatch();            
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
        return resetSession;
    }
      
    //-------------------------------------------------------------------------
    private Map uiCRC = new HashMap();
    private final Layer_1 uiRepository;
    private transient PersistenceManager pm = null;
    private final Path providerPath;
  
}

//--- End of File -----------------------------------------------------------
