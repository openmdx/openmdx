/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: UiLoader.java,v 1.79 2010/04/28 13:40:33 wfro Exp $
 * Description: UiLoader
 * Revision:    $Revision: 1.79 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/28 13:40:33 $
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jmi.reflect.RefObject;
import javax.resource.cci.MappedRecord;
import javax.servlet.ServletContext;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderRequestProcessor;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.kernel.EmbeddedDataprovider_1;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.xml.Importer;
import org.openmdx.base.accessor.jmi.spi.EntityManagerFactory_1;
import org.openmdx.base.accessor.rest.DataManagerFactory_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.ConnectionFactoryAdapter;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;
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
    protected static class Dataprovider extends Layer_1 {

        public Dataprovider(
            List delegates
        ) throws Exception {
            this.delegates = delegates;
            super.activate(
                (short)5, 
                new Configuration(), 
                (Layer_1)delegates.get(0)
            );
        }

        @Override
        public void deactivate(
        ) throws Exception {
            for(Iterator i = this.delegates.iterator(); i.hasNext(); ) {
                ((Layer_1)i.next()).deactivate();
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
            configuration.values("namespaceId").put(0, "ui");
            Layer_1 persistencePlugin = Classes.<Layer_1>getApplicationClass(
                org.openmdx.application.dataprovider.layer.persistence.none.InMemory_1.class.getName()
            ).newInstance();
            persistencePlugin.activate((short)0, configuration, null);
            Layer_1 modelPlugin = Classes.<Layer_1>getApplicationClass(
                org.openmdx.application.dataprovider.layer.model.Standard_1.class.getName()
            ).newInstance();
            modelPlugin.activate((short)1, configuration, persistencePlugin);
            Layer_1 applicationPlugin = Classes.<Layer_1>getApplicationClass(
                org.openmdx.ui1.layer.application.Ui_1.class.getName()
            ).newInstance();
            applicationPlugin.activate((short)2, configuration, modelPlugin);
            Layer_1 typePlugin = Classes.<Layer_1>getApplicationClass(
                org.openmdx.application.dataprovider.layer.type.Strict_1.class.getName()
            ).newInstance();
            typePlugin.activate((short)3, configuration, applicationPlugin);
            Layer_1 interceptionPlugin = Classes.<Layer_1>getApplicationClass(
                org.openmdx.application.dataprovider.layer.interception.Standard_1.class.getName()
            ).newInstance();
            Configuration interceptionConfiguration = new Configuration(configuration);
            interceptionConfiguration.values("propagateSet").put(0, Boolean.TRUE);
            interceptionPlugin.activate((short)4, interceptionConfiguration, typePlugin);
       
            return new Dataprovider(
                Arrays.asList(
                    new Layer_1[]{
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
        if(this.pm == null) try {
        	EmbeddedDataprovider_1 uiPort = new EmbeddedDataprovider_1();
        	uiPort.setDelegate(this.uiRepository);
        	ConnectionFactoryAdapter connectionFactoryAdapter = new ConnectionFactoryAdapter(
                uiPort,
            	false, // supportsLocalTransactionDemarcation
            	TransactionAttributeType.NEVER
            );
        	PersistenceManagerFactory dataObjectManagerFactory = DataManagerFactory_1.getPersistenceManagerFactory(
    			Collections.singletonMap(
    				ConfigurableProperty.ConnectionFactory.qualifiedName(),
    				connectionFactoryAdapter
    			)
    		);
        	Map<String,Object> entityManagerConfiguration = new HashMap<String,Object>();
        	entityManagerConfiguration.put(
            	ConfigurableProperty.ConnectionFactory.qualifiedName(),
            	dataObjectManagerFactory
            );
            entityManagerConfiguration.put(
            	ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(),
            	EntityManagerFactory_1.class.getName()
            );        	
            this.pm = JDOHelper.getPersistenceManagerFactory(
            	entityManagerConfiguration
            ).getPersistenceManager(
            	null, // userName
            	null // password
            );
        } catch (Exception exception) {
            throw new ServiceException(exception);
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
    	String messagePrefix = new Date() + "  ";
    	System.out.println(messagePrefix + "Loading ui configuration...");
    	SysLog.info("Loading ui configuration");
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
              Map<Path,MappedRecord> mergedUiElementDefs = new LinkedHashMap<Path,MappedRecord>();
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
                        SysLog.info(locale[j] + " not found. Fallback to " + locale[fallbackLocaleIndex]);
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
                      System.out.println(messagePrefix + "Loading " + uiResources.size() + " files for locale " + locale[j]);
                      SysLog.info("Loading " + uiResources.size() + " files for locale " + locale[j]);
                  }                  
                  for(
                      Iterator k = uiResources.iterator(); 
                      k.hasNext(); 
                  ) {        
                      Map<Path,MappedRecord> uiElementDefs = new LinkedHashMap<Path,MappedRecord>();
                      String path = (String)k.next();
                      if(!path.endsWith("/")) {
                    	  SysLog.detail("Loading", path);
                          try {
                        	  Importer.importObjects(
                        		  Importer.asTarget(uiElementDefs),
                        		  Importer.asSource(context.getResource(path))
                              );
                          }
                          catch(Exception e) {
                              new ServiceException(e).log();
                              System.out.println(messagePrefix + "STATUS: " + e.getMessage() + " (for more info see log)");
                          }
                      }        
                      // Merge entries
                      if(j == 0) {
                    	  mergedUiElementDefs.putAll(
                    		  uiElementDefs
                    	  );
                      }
                      else {
	                      for(Entry<Path,MappedRecord> e: mergedUiElementDefs.entrySet()) {
                              MappedRecord mergedUiElementDef = e.getValue();   
	                    	  MappedRecord mergedUiElementDefValue = Object_2Facade.getValue(mergedUiElementDef);
	                          // merge entry
                              // do not merge if label for locale 0 is not set
                              List<Object> mergedLabels = mergedUiElementDefValue.get("label") == null ?
                            	  null :
                            		  (List<Object>)mergedUiElementDefValue.get("label");
                              if(mergedLabels != null && !mergedLabels.isEmpty()) {
                                  if(mergedLabels.size() <= j) {
                                	  mergedLabels.add(
                                		  mergedLabels.get(fallbackLocaleIndex)
                                      );
                                  }
                                  List<Object> mergedShortLabels = (List<Object>)mergedUiElementDefValue.get("shortLabel");
                                  // assert that shortLabel is set for locale i. 
                                  if(mergedShortLabels == null) {
                                	  mergedUiElementDefValue.put(
                                		  "shortLabel", 
                                		  mergedShortLabels = new ArrayList<Object>()
                                	  );
                                  } 
                                  if(mergedShortLabels.isEmpty()) {
                                	  mergedShortLabels.add("");
                                  }
                                  if(mergedShortLabels.size() <= j) {
                                	  mergedShortLabels.add(
                                		  mergedShortLabels.get(fallbackLocaleIndex)
                                      );
                                  }
                                  // assert that toolTip is set for locale i
                                  List<Object> mergedToolTips = (List<Object>)mergedUiElementDefValue.get("toolTip");
                                  if(mergedToolTips == null) {
                                	  mergedUiElementDefValue.put(
                                		  "toolTip", 
                                		  mergedToolTips = new ArrayList<Object>()
                                	  );                                	  
                                  }
                                  if(mergedToolTips.isEmpty()) {
                                	  mergedToolTips.add("");
                                  }
                                  if(mergedToolTips.size() <= j) {
                                	  mergedToolTips.add(
                                		  mergedToolTips.get(fallbackLocaleIndex)
                                      );
                                  }                                 
                                  // overwrite label, shortLabel and toolTip with uiElement values if available
                                  MappedRecord uiElementDef = uiElementDefs.get(e.getKey());
                                  if(uiElementDef != null) {
                                	  MappedRecord elementDefValue = Object_2Facade.getValue(uiElementDef);
                                	  List<Object> labels = (List<Object>)elementDefValue.get("label");
                                      if(labels != null && !labels.isEmpty()) {
                                          mergedLabels.set(
                                              j,
                                              labels.get(0)
                                          );
                                      }
                                      List<Object> shortLabels = (List<Object>)elementDefValue.get("shortLabel");
                                      if(shortLabels != null && !shortLabels.isEmpty()) {
                                          mergedShortLabels.set(
                                              j,
                                              shortLabels.get(0)
                                          );
                                      }
                                      // Take label of same locale as shortLabel fallback 
                                      // Do not take shortLabel of fallback locale 
                                      else {
                                          mergedShortLabels.set(
                                              j,
                                              mergedLabels.get(j)
                                          );                                      
                                      }
                                      List<Object> toolTips = (List<Object>)elementDefValue.get("toolTip");
                                      if(toolTips != null && !toolTips.isEmpty()) {
                                          mergedToolTips.set(
                                              j,
                                              toolTips.get(0)
                                          );
                                      }
                                  }
                              }
	                      }
                      }
                  }
                }
                catch(Exception e) {
                    throw new ServiceException(e);
                }          
              }          
              // Store ui elements. First remove existing elements
              try {
                  String[] segmentName = this.getSegmentName(dir);
                  DataproviderRequestProcessor store = new DataproviderRequestProcessor(
                      new ServiceHeader(),
                      this.uiRepository
                  );
                  // Assert existence of provider
                  try {
                	  MappedRecord uiProvider = Object_2Facade.newInstance(
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
                	  MappedRecord uiSegment = Object_2Facade.newInstance(
                		  uiSegmentPath,
                		  "org:openmdx:ui1:Segment"
                	  ).getDelegate();
                	  store.addCreateRequest(uiSegment);
                  }
                  catch(Exception e) {}
                  // Store ui elements
                  System.out.println(messagePrefix + "Storing " + mergedUiElementDefs.size() + " ui elements for segment " + uiSegmentPath);
                  SysLog.info("Storing " + mergedUiElementDefs.size() + " ui elements for segment " + uiSegmentPath);
                  store.beginBatch();
                  for(Iterator<MappedRecord> j = mergedUiElementDefs.values().iterator(); j.hasNext(); ) {
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
            	  SysLog.error("can not create ui config", e.getMessage());
              }              
              this.uiCRC.put(
                  dir,
                  new Long(crc)
              );
          }
        }    
    	System.out.println(messagePrefix + "Done (" + loadedUiSegments.size() + " segments)");
    	SysLog.info("Done", loadedUiSegments.size());        
        return new ArrayList<Path>(loadedUiSegments.values());
    }

    //-------------------------------------------------------------------------
    private Map uiCRC = new HashMap();
    private final Layer_1 uiRepository;
    private transient PersistenceManager pm = null;
    private final Path providerPath;
}

//--- End of File -----------------------------------------------------------
