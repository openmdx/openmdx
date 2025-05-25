/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: UiLoader
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jmi.reflect.RefObject;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.servlet.ServletContext;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.MappedRecord;
import jakarta.servlet.ServletContext;
#endif

import org.openmdx.application.xml.Importer;
import org.openmdx.base.accessor.jmi.spi.EntityManagerFactory_1;
import org.openmdx.base.accessor.rest.DataManagerFactory_1;
import org.openmdx.base.dataprovider.cci.DataproviderRequestProcessor;
import org.openmdx.base.dataprovider.kernel.Dataprovider_2;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.RestConnectionFactory;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.transaction.TransactionAttributeType;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.w3c.time.SystemClock;

public class UiLoader
    extends Loader {

    //-------------------------------------------------------------------------
    public UiLoader(
        ServletContext context,
        PortalExtension_1_0 portalExtension,
        Model_1_0 model,
        Path providerPath
    ) throws ServiceException {
        super(
            context,
            portalExtension
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
    
    /**
     * Constructs an in-memory ui repository
     */
    protected static Port<RestConnection> createUiRepository(
        Model_1_0 model
    ) throws ServiceException {
        return new Dataprovider_2(UI_DATAPROVIDER_CONFIGURATION_URI);
    }

    //-------------------------------------------------------------------------
    public PersistenceManager getRepository(
    ) throws ServiceException {
        if(this.pm == null) {
        	try {
	        	RestConnectionFactory connectionFactoryAdapter = new RestConnectionFactory(
	                this.uiRepository,
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
    	String messagePrefix = SystemClock.getInstance().now() + "  ";
    	System.out.println(messagePrefix + "Loading ui configuration...");
    	SysLog.info("Loading ui configuration");
        Map<String,Path> loadedUiSegments = new TreeMap<String,Path>();
        List<String> dirs = this.getDirectories("/WEB-INF/config/ui/");
        for(
            Iterator<String> i = dirs.iterator(); 
            i.hasNext(); 
        ) {
            String dir = i.next();        
    
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
              ((this.uiCRC.get(dir)).longValue() != crc)
            ) {
              Map<Path,ObjectRecord> mergedUiElementDefs = new LinkedHashMap<Path,ObjectRecord>();
              int fallbackLocaleIndex = 0;
              for(int j = 0; j < locale.length; j++) {
                fallbackLocaleIndex = 0;
                Set<String> uiResources = null;
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
                    	uiResources = new TreeSet<String>(uiResources);
                    }
                }
                try {
                  if(uiResources == null) {
                      uiResources = new TreeSet<String>();
                      uiResources.add("./"); // empty dir
                  }
                  else {
                      System.out.println(messagePrefix + "Loading " + uiResources.size() + " files for locale " + locale[j]);
                      SysLog.info("Loading " + uiResources.size() + " files for locale " + locale[j]);
                  }                  
                  for(
                      Iterator<String> k = uiResources.iterator(); 
                      k.hasNext(); 
                  ) {        
                      Map<Path,ObjectRecord> uiElementDefs = new LinkedHashMap<Path,ObjectRecord>();
                      String path = k.next();
                      if(!path.endsWith("/")) {
                    	  SysLog.detail("Loading", path);
                          try {
                        	  Importer.importObjects(
                        		  Importer.asTarget(uiElementDefs),
                        		  Importer.asSource(context.getResource(path)),
                        		  null, // errorHandler
                        		  org.openmdx.application.xml.spi.ImportMode.CREATE
                              );
                          } catch(Exception e) {
                              Throwables.log(e);
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
	                      for(Entry<Path,ObjectRecord> e: mergedUiElementDefs.entrySet()) {
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
                	  Collections.<String>emptyList(),
                      this.uiRepository
                  );
                  // Assert existence of provider
                  try {
                	  ObjectRecord uiProvider = Object_2Facade.newInstance(
                		  this.providerPath,
                		  "org:openmdx:base:Provider"
                	  ).getDelegate();
                	  store.addCreateRequest(uiProvider);
                  } catch(Exception e) {}
                  // Remove existing ui config
                  Path uiSegmentPath = this.providerPath.getDescendant("segment", segmentName[segmentName.length-1]);
                  try {
                	  store.addRemoveRequest(uiSegmentPath);
                  }catch(Exception e) {}
                  // Re-create segment
                  try {
                	  ObjectRecord uiSegment = Object_2Facade.newInstance(
                		  uiSegmentPath,
                		  "org:openmdx:ui1:Segment"
                	  ).getDelegate();
                	  store.addCreateRequest(uiSegment);
                  } catch(Exception e) {}
                  // Store ui elements
                  System.out.println(messagePrefix + "Storing " + mergedUiElementDefs.size() + " ui elements for segment " + uiSegmentPath);
                  SysLog.info("Storing " + mergedUiElementDefs.size() + " ui elements for segment " + uiSegmentPath);
                  store.beginBatch();
                  for(Iterator<ObjectRecord> j = mergedUiElementDefs.values().iterator(); j.hasNext(); ) {
                	  ObjectRecord uiElement = j.next();
                      store.addCreateRequest(
                          uiElement
                      );
                  }
                  store.endBatch();
                  loadedUiSegments.put(
                      segmentName[0],
                      this.providerPath.getDescendant("segment", segmentName[segmentName.length-1])
                  );
              } catch(ResourceException e) {
            	  SysLog.error("can not create ui config", e.getMessage());
              }
              this.uiCRC.put(
                  dir,
                  Long.valueOf(crc)
              );
          }
        }
    	System.out.println(messagePrefix + "Done (" + loadedUiSegments.size() + " segments)");
    	SysLog.info("Done", loadedUiSegments.size());        
        return new ArrayList<Path>(loadedUiSegments.values());
    }

    //-------------------------------------------------------------------------
	private static final String UI_DATAPROVIDER_CONFIGURATION_URI = Resources.toResourceXRI("org/openmdx/ui1/ui-dataprovider.properties");
	
    private final Map<String,Long> uiCRC = new HashMap<String,Long>();
    private final Port<RestConnection> uiRepository;
    private transient PersistenceManager pm = null;
    private final Path providerPath;
}

//--- End of File -----------------------------------------------------------
