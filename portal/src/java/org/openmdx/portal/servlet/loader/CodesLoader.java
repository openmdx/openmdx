/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: TextsLoader class
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

import java.net.MalformedURLException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManagerFactory;
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.servlet.ServletContext;

import org.openmdx.application.xml.Importer;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Codes;
import org.openmdx.portal.servlet.PortalExtension_1_0;

public class CodesLoader extends Loader {

	/**
	 * Constructor 
	 *
	 * @param codeProviderIdentity
	 * @param context
	 * @param portalExtension
	 * @param pmf
	 */
	public CodesLoader(
		ServletContext context,
		PortalExtension_1_0 portalExtension,      
		PersistenceManagerFactory pmf
	) {
		super(
			context,
			portalExtension
		);
		this.pmf = pmf;     
	}
    
    /**
     * Load and store codes.
     * @param locale
     * @throws ServiceException
     */
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
                            if(mergedCodeEntryFacade.getObjectClass().endsWith("CodeValueEntry")) {
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
            Codes.storeCodes(
            	this.pmf.getPersistenceManager(this.getAdminPrincipal(dir), null),
            	mergedCodes
            );
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
