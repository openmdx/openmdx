/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: CodesLoader class
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
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Codes;
import org.openmdx.portal.servlet.PortalExtension_1_0;

/**
 * CodesLoader
 *
 */
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
     * 
     * @param locale
     * @throws ServiceException
     */
	synchronized public void loadCodes(
		String[] locale
	) throws ServiceException {
		String messagePrefix = new Date() + "  ";
		System.out.println(messagePrefix + "Loading codes");
		SysLog.info("Loading codes");
		List<String> dirs = this.getDirectories("/WEB-INF/config/code/");
		// Iterate all code directories. Each directory may contain segment 
		// and locale specific code files
		for(String dir: dirs) {
			Map<Path,ObjectRecord> mergedCodes = new LinkedHashMap<Path,ObjectRecord>();
			int fallbackLocaleIndex = 0;
			// Iterate all configured locales
			for(int j = 0; j < locale.length; j++) {
				fallbackLocaleIndex = 0;
				Set<String> codeResourcePaths = new TreeSet<String>();
				if(locale[j] != null) {
					String codeResourcesPath = dir + locale[j];
					codeResourcePaths = new TreeSet<String>(this.context.getResourcePaths(codeResourcesPath));
				} else {
					codeResourcePaths.add("./"); // empty locale dir
				}
				try {
					for(String codeResourcePath: codeResourcePaths) {        
						Map<Path,ObjectRecord> codes = new LinkedHashMap<Path,ObjectRecord>();
						if(!codeResourcePath.endsWith("/")) {
							SysLog.info("Loading " + codeResourcePath);
							try {
								Importer.importObjects(
									Importer.asTarget(codes),
									Importer.asSource(context.getResource(codeResourcePath))
								);
							} catch(ServiceException e) {
								e.log();
								SysLog.info("STATUS: " + e.getMessage());
							}
						}
						// Merge entries
						Set<Entry<Path,ObjectRecord>> entrySet = null;
						if(j == 0) {
							entrySet = codes.entrySet();
							for(Iterator<Entry<Path,ObjectRecord>> l = entrySet.iterator(); l.hasNext(); ) {
								try {
									Entry<Path,ObjectRecord> e = l.next();
									Object_2Facade codeEntry = Object_2Facade.newInstance(e.getValue());
									if(codeEntry.getObjectClass().endsWith("CodeValueEntry")) {
										// Clear if shortText is null
										if(
											!codeEntry.attributeValuesAsList("shortText").isEmpty() &&
											codeEntry.attributeValue("shortText") == null
										) {
											codeEntry.clearAttributeValuesAsList("shortText");
										}
										// Clear if longText is null
										if(
											!codeEntry.attributeValuesAsList("longText").isEmpty() &&
											codeEntry.attributeValue("longText") == null
										) {
											codeEntry.clearAttributeValuesAsList("longText");
										}
									}
								} catch(Exception ignore) {}
							}
						} else {
							entrySet = mergedCodes.entrySet();
						}
						for(Iterator<Entry<Path,ObjectRecord>> l = entrySet.iterator(); l.hasNext(); ) {
							Entry<Path,ObjectRecord> e = l.next();
							// Merge entry
							if(
								(j > 0) &&
								(mergedCodes.get(e.getKey()) != null)
							) {                                                            
								MappedRecord mergedCodeEntry = mergedCodes.get(e.getKey());
								Object_2Facade mergedCodeEntryFacade;
								try {
									mergedCodeEntryFacade = Object_2Facade.newInstance(mergedCodeEntry);
								} catch (ResourceException e0) {
									throw new ServiceException(e0);
								}
								// Do not merge if shortText for locale 0 is not set
								if(mergedCodeEntryFacade.getObjectClass().endsWith("CodeValueEntry")) {
									if(mergedCodeEntryFacade.attributeValuesAsList("shortText").size() <= j) {
										if(fallbackLocaleIndex < mergedCodeEntryFacade.attributeValuesAsList("shortText").size()) {
											mergedCodeEntryFacade.attributeValuesAsList("shortText").add(
												mergedCodeEntryFacade.attributeValuesAsList("shortText").get(fallbackLocaleIndex)
											);
										}
									}
									// Assert that longText is set for locale i
									if(mergedCodeEntryFacade.getAttributeValues("longText") == null) {
										mergedCodeEntryFacade.attributeValuesAsList("longText").add("N/A");
									}
									if(mergedCodeEntryFacade.attributeValuesAsList("longText").size() <= j) {
										mergedCodeEntryFacade.attributeValuesAsList("longText").add(
											mergedCodeEntryFacade.attributeValuesAsList("longText").get(fallbackLocaleIndex)
										);
									}                                  
									// Overwrite shortText and longText with uiElement values if available
									MappedRecord codeEntry = codes.get(e.getKey());
									if(codeEntry != null) {
										Object_2Facade codeEntryFacade;
										try {
											codeEntryFacade = Object_2Facade.newInstance(codeEntry);
										} catch (ResourceException e0) {
											throw new ServiceException(e0);
										}
										if(
											!codeEntryFacade.attributeValuesAsList("shortText").isEmpty() &&
											codeEntryFacade.attributeValue("shortText") != null
										) {
											Object shortText = codeEntryFacade.attributeValue("shortText") == null
												? "" // no null values in shortText list allowed
												: codeEntryFacade.attributeValue("shortText");
											while(mergedCodeEntryFacade.attributeValuesAsList("shortText").size() <= j) {
												mergedCodeEntryFacade.attributeValuesAsList("shortText").add(shortText);
											}
											mergedCodeEntryFacade.attributeValuesAsList("shortText").set(j, shortText);
										}
										if(
											!codeEntryFacade.attributeValuesAsList("longText").isEmpty() &&
											codeEntryFacade.attributeValue("longText") != null
										) {
											Object longText = codeEntryFacade.attributeValue("longText") == null
												? "" // no null values in longText list allowed
												: codeEntryFacade.attributeValue("longText");
											while(mergedCodeEntryFacade.attributeValuesAsList("longText").size() <= j) {
												mergedCodeEntryFacade.attributeValuesAsList("longText").add(longText);
											}
											mergedCodeEntryFacade.attributeValuesAsList("longText").set(j, longText);
										}
									}
								}
							} else if(j == 0) {
								// Add if it does not exist. Only add for locale=0 (en_US)
								mergedCodes.put(
									e.getKey(),
									e.getValue()
								);
							} else {
								// locale > 0 requires that locale=0 exists. Complain if it
								// does not
								SysLog.warning("entry " + e.getKey() + " of locale " + locale[j] + " has no corresponding entry for locale " + locale[0] + ". Not loading");
							}
						}
					}
				} catch(MalformedURLException e) {
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
