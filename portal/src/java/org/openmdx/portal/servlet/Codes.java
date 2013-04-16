/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Codes
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import javax.resource.cci.MappedRecord;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.dataprovider.cci.JmiHelper;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Codes
 *
 */
public final class Codes implements Serializable {

	/**
	 * CodeEntry
	 *
	 */
	public static class CodeEntry {
		
		public CodeEntry(
			Path id,
			List<String> shortText,
			List<String> longText,
			String iconKey,
			String color,
			String backColor,
			Date validFrom,
			Date validTo
		) {
			this.id = id;
			this.shortText = shortText;
			this.longText = longText;
			this.iconKey = iconKey;
			this.color = color;
			this.backColor = backColor;
			this.validFrom = validFrom;
			this.validTo = validTo;
		}
		
		public Path getId() {
			return this.id;
		}
		
		public List<String> getShortText(
		) {
			return this.shortText;
        }
        
		public List<String> getLongText() {
			return this.longText;
		}
        
		public String getIconKey() {
			return this.iconKey;
        }        

		public String getColor() {
	        return this.color;
		}	        

		public String getBackColor() {
	        return this.backColor;
		}	        

		public Date getValidFrom() {
	        return this.validFrom;
		}

		public Date getValidTo() {
	        return this.validTo;
		}
		
		private final Path id;
		private final List<String> shortText;
		private final List<String> longText;
		private final String iconKey;
		private final String color;
		private final String backColor;
		private final Date validFrom;
		private final Date validTo;
	}
	
    /**
     * Constructor 
     *
     * @param codeSegment
     * @throws ServiceException
     */
    public Codes(
        RefObject_1_0 codeSegment
    ) {
    	if(System.currentTimeMillis() > refreshedAt + TTL) {
    		codeSegmentIdentity = codeSegment.refGetPath();
    		PersistenceManager pm = JDOHelper.getPersistenceManager(codeSegment);
    		refresh(pm);
    	}
    }

    /**
     * Refresh codes.
     * @param pm
     * @throws ServiceException
     */
    public static void refresh(
    	PersistenceManager pm
    ) {
    	if(codeSegmentIdentity != null) {
        	RefObject_1_0 codeSegment = (RefObject_1_0)pm.getObjectById(codeSegmentIdentity);
    		Map<String,SortedMap<Short,CodeEntry>> codeEntries = loadCodes(codeSegment);
	        cachedCodeEntryContainer = codeEntries;
	        cachedShortTexts.clear();
	        cachedLongTexts.clear();
	        cachedIconKeys.clear();
	        cachedColors.clear();
	        cachedBackColors.clear();
    		refreshedAt = System.currentTimeMillis();
    	}
    }

	/**
	 * Store codes to specified segment.
	 * @param codeProviderIdentity
	 * @param segmentName
	 * @param pmf
	 * @param codes
	 */
    public static void storeCodes(
    	PersistenceManager pm,
    	Map<Path,MappedRecord> codes
    	) throws ServiceException {
    	String messagePrefix = new Date() + "  ";
    	SysLog.info("Storing " + codes.size() + " code entries");
    	System.out.println(messagePrefix + "Storing " + codes.size() + " code entries");
    	// Load objects in multiple runs in order to resolve object dependencies.
    	Map<Path,RefObject> loadedObjects = new HashMap<Path,RefObject>(); 
    	for(int run = 0; run < 5; run++) {
    		boolean hasNewObjects = false;
    		for(
    			Iterator<MappedRecord> j = codes.values().iterator(); 
    			j.hasNext(); 
    		) {
    			MappedRecord entry = j.next();
    			Path entryPath = Object_2Facade.getPath(entry);
    			// Create new entries, update existing
				RefObject_1_0 existing = null;
				try {
					existing = (RefObject_1_0)pm.getObjectById(entryPath);
				} catch(Exception ignore) {}
				try {
					pm.currentTransaction().begin();
    				if(existing != null) {
    					loadedObjects.put(
    						entryPath, 
    						existing
    					);
    					// Object exists: if it was created on first run it does not have to be updated 
    					// on subsequent runs. If it was updated on first run do not update it again.
    					if(run == 0) {
    						JmiHelper.toRefObject(
    							entry,
    							existing,
    							loadedObjects, // object cache
    							null, // ignorable features
    							true // compareWithBeforeImage
    						);
    					}
    				} else {
    					String qualifiedClassName = Object_2Facade.getObjectClass(entry);
    					String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(':'));
    					RefObject_1_0 newEntry = (RefObject_1_0)pm.getObjectById(
    						Authority.class,
    						"xri://@openmdx*" + packageName.replace(":", ".")
    					).refImmediatePackage().refClass(qualifiedClassName).refCreateInstance(null);
    					newEntry.refInitialize(false, false);
    					JmiHelper.toRefObject(
    						entry,
    						newEntry,
    						loadedObjects, // object cache
    						null, // ignorable features
    						true // compareWithBeforeImage
    					);
    					Path parentIdentity = entryPath.getParent().getParent();
    					RefObject_1_0 parent = null;
    					try {
    						parent = loadedObjects.containsKey(parentIdentity) 
    							? (RefObject_1_0)loadedObjects.get(parentIdentity) 
    							: (RefObject_1_0)pm.getObjectById(parentIdentity);
    					} catch(Exception ignore) {}
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
    				pm.currentTransaction().commit();
    			} catch(Exception e) {
					try {
						pm.currentTransaction().rollback();
					} catch(Exception ignore) {}
    				new ServiceException(e).log();
    				System.out.println(messagePrefix + "STATUS: " + e.getMessage() + " (for more info see log)");
    			}
    		}
    		if(!hasNewObjects) break;
    	}
    }

	/**
	 * Store bundles in code segment.
	 * @param codeSegmentIdentity
	 * @param pm
	 * @param bundles
	 */
	public static void storeBundles(
		Path codeSegmentIdentity,
		String containerName,
		PersistenceManager pm,
		List<ResourceBundle> bundles
	) throws ServiceException {
		RefObject_1_0 codeSegment = (RefObject_1_0)pm.getObjectById(codeSegmentIdentity);
		Map<Path,MappedRecord> codes = new HashMap<Path,MappedRecord>();
		// Derive class of value containers and entries from existing value containers
		String classNameContainer = null;
		String classNameEntry = null;
        @SuppressWarnings("unchecked")
        Collection<RefObject_1_0> valueContainers = (Collection)codeSegment.refGetValue("valueContainer");
        if(!valueContainers.isEmpty()) {
        	RefObject_1_0 valueContainer = valueContainers.iterator().next();
        	classNameContainer = valueContainer.refClass().refMofId();
        	@SuppressWarnings("unchecked")
            Collection<RefObject_1_0> entries = (Collection)valueContainer.refGetValue("entry");
        	if(!entries.isEmpty()) {
        		classNameEntry = entries.iterator().next().refClass().refMofId();
        	}
        }
        // Map bundles to CodeValueContainer / CodeValueEntry
        if(classNameContainer != null && classNameEntry != null) {
        	Path valueContainerIdentity = codeSegmentIdentity.getDescendant("valueContainer", containerName);        	
        	Object_2Facade valueContainer = null;
        	try {
        		valueContainer = Object_2Facade.newInstance(
	        		valueContainerIdentity, 
	        		classNameContainer
	        	);
        	} catch(Exception e) {}
        	@SuppressWarnings("unchecked")
            List<Object> names = (List<Object>)valueContainer.attributeValues("name");
        	names.add(containerName);
        	codes.put(
        		valueContainerIdentity, 
        		valueContainer.getDelegate()
        	);
        	ResourceBundle primaryBundle = bundles.get(0);
    		Set<String> keys = new TreeSet<String>(primaryBundle.keySet());
    		int codeIndex = 0;
    		for(String key: keys) {
    			Path entryIdentity = valueContainerIdentity.getDescendant("entry", Integer.toString(codeIndex));
    			Object_2Facade entry = null;
                try {
	                entry = Object_2Facade.newInstance(
	                	entryIdentity,
	                	classNameEntry
	                );
                } catch(Exception e) {}
    			@SuppressWarnings("unchecked")
                List<Object> shortTexts = (List<Object>)entry.attributeValues("shortText", Multiplicity.LIST);
    			shortTexts.add(key);
    			@SuppressWarnings("unchecked")
                List<Object> longTexts = (List<Object>)entry.attributeValues("longText", Multiplicity.LIST);
    			longTexts.add(primaryBundle.getObject(key));    			
    			for(int localeIndex = 1; localeIndex < bundles.size(); localeIndex++) {
    				longTexts.add(
    					bundles.get(localeIndex).containsKey(key) ? bundles.get(localeIndex).getObject(key) : ""
    				);
    			}
        		codes.put(
        			entryIdentity,
        			entry.getDelegate()
        		);
        		codeIndex++;
    		}
        }
		storeCodes(
			pm, 
			codes
		);
	}

	/**
	 * Load codes.
	 * @param codeProviderIdentity
	 * @param segmentName
	 * @param pmf
	 * @return
	 * @throws ServiceException
	 */
	public static Map<String,SortedMap<Short,CodeEntry>> loadCodes(
		RefObject_1_0 codeSegment
	) {
    	Map<String,SortedMap<Short,CodeEntry>> codeEntryContainers = new ConcurrentHashMap<String,SortedMap<Short,CodeEntry>>();    	
        @SuppressWarnings("unchecked")
        Collection<RefObject_1_0> valueContainers = (Collection)codeSegment.refGetValue("valueContainer");
        for(RefObject_1_0 valueContainer: valueContainers) {
        	Set<String> containerNames = new HashSet<String>( 
        		(Set<String>)valueContainer.refGetValue("name")
        	);
        	containerNames.add(
        		valueContainer.refGetPath().getBase()
        	);
        	for(String containerName: containerNames) {
        		SortedMap<Short,CodeEntry> codeEntries = new TreeMap<Short,CodeEntry>();
        		@SuppressWarnings("unchecked")
                Collection<RefObject_1_0> entries = (Collection)valueContainer.refGetValue("entry");
        		for(RefObject_1_0 entry: entries) {
                    Short code = 0;
                    try {
                        code = new Short(entry.refGetPath().getBase());
                    } catch(Exception e) {}        			
        			codeEntries.put(
        				code,
        				new CodeEntry(
        					entry.refGetPath(),
        					new ArrayList<String>((List<String>)entry.refGetValue("shortText")),
        					new ArrayList<String>((List<String>)entry.refGetValue("longText")),
        					(String)entry.refGetValue("iconKey"),
        					(String)entry.refGetValue("color"),
        					(String)entry.refGetValue("backColor"),        					
        			        (Date)entry.refGetValue("validFrom"),
        			        (Date)entry.refGetValue("validTo")
        			    )
        			);
        		}
        		codeEntryContainers.put(
        			containerName,
        			codeEntries
        		);
        	}
        }
        return codeEntryContainers;
	}
	
    /**
     * @param codeEntry
     * @return
     */
    private static boolean entryIsValid(
        CodeEntry codeEntry
    ) {
        Date validFrom = codeEntry.getValidFrom();
        Date validTo = codeEntry.getValidTo();
        Date current = new Date();
        return 
	        ((validFrom == null) || validFrom.before(current)) &&
	        ((validTo == null) || validTo.after(current));
    }

    /**
     * Returns true if at least one short text is locale-specific. Returns false
     * if all short texts are fallback texts (taken from locale 0)
     * @param name
     * @param locale
     * @param codeAsKey
     * @param key
     * @param includeAll
     * @return
     */
    private static boolean prepareShortText(
        String name,
        short locale,
        boolean codeAsKey,
        String key,
        boolean includeAll
    ) {
    	SortedMap<Short,CodeEntry> codeEntryContainer = cachedCodeEntryContainer.get(name);
        if(codeEntryContainer == null) {
            return false;
        }
        boolean hasLocaleSpecificTexts = false;
        SortedMap<Object,Object> shortTexts = new TreeMap<Object,Object>();
        for(Map.Entry<Short,CodeEntry> entry: codeEntryContainer.entrySet()) {
            if(includeAll || entryIsValid(entry.getValue())) {
                List texts = entry.getValue().getShortText();
                Object text;
                if(texts.size() > locale) {
                	text = texts.get(locale);
                	hasLocaleSpecificTexts = true;
                } else {
                	text = texts.isEmpty() ? "" : texts.get(0);
                }
                if(codeAsKey) {
                    shortTexts.put(entry.getKey(), text);
                } else {
                    shortTexts.put(text, entry.getKey());
                }
            }
        }
        cachedShortTexts.put(
            key,
            shortTexts
        );
        return hasLocaleSpecificTexts;
    }

    /**
     * Returns true if at least one long text is locale-specific. Returns false
     * if all long texts are fallback texts (taken from locale 0). 
     * @param name
     * @param locale
     * @param codeAsKey
     * @param key
     * @param includeAll
     * @return
     */
    private static boolean prepareLongText(
        String name,
        short locale,
        boolean codeAsKey,
        String key,
        boolean includeAll
    ) {
    	SortedMap<Short,CodeEntry> codeEntryContainer = cachedCodeEntryContainer.get(name);
        if(codeEntryContainer == null) {
            return false;
        }
        boolean hasLocaleSpecificTexts = false;
        SortedMap<Object,Object> longTexts = new TreeMap<Object,Object>();
        for(Map.Entry<Short,CodeEntry> entry: codeEntryContainer.entrySet()) {
            if(includeAll || entryIsValid(entry.getValue())) {
                List texts = entry.getValue().getLongText();
                Object text;
                if(texts.size() > locale) {
                	text = texts.get(locale);
                	hasLocaleSpecificTexts = true;
                } else {
                	text = texts.isEmpty() ? "" : texts.get(0);
                }
                if(codeAsKey) {
                    longTexts.put(entry.getKey(), text);
                } else {
                    if(text == null) {
                    	ServiceException e = new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CONFIGURATION, 
                            "text of code value entry can not be null",
                            new BasicException.Parameter("container.name", name),
                            new BasicException.Parameter("locale", locale),
                            new BasicException.Parameter("lookup.key", key),
                            new BasicException.Parameter("entry.id", entry.getValue().getId()),                            
                            new BasicException.Parameter("texts", texts)
                        );
                    	e.log();
                    	text = "N/A";
                    }
                    longTexts.put(text, entry.getKey());
                }
            }
        }
        cachedLongTexts.put(
            key,
            longTexts
        );
        return hasLocaleSpecificTexts;
    }
	
    /**
     * @param name
     * @param locale
     * @param codeAsKey
     * @param includeAll
     * @return
     */
    private String getKey(
    	String name,
    	short locale,
    	boolean codeAsKey,
    	boolean includeAll
    ) {
    	return name + ":" + locale + ":" + codeAsKey + ":" + includeAll;
    }
    
    /**
     * @param name
     * @param locale
     * @param codeAsKey
     * @param includeAll
     * @return
     */
    public Map getLongText(
        String name,
        short locale,
        boolean codeAsKey,
        boolean includeAll
    ) {
        SortedMap<Object,Object> longTexts = null;
        String key = this.getKey(name, locale, codeAsKey, includeAll);
        if((longTexts = cachedLongTexts.get(key)) == null) {
            prepareLongText(
                name,
                locale,
                codeAsKey,
                key,
                includeAll
            );
            longTexts = cachedLongTexts.get(key);
        }
        return longTexts;
    }

    /**
     * @param name
     * @param locale
     * @param includeAll
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<Short,String> getLongTextByCode(
    	String name,
        short locale,
        boolean includeAll
    ) {
    	return this.getLongText(name, locale, true, includeAll);
    }

    /**
     * @param name
     * @param locale
     * @param includeAll
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String,Short> getLongTextByText(
    	String name,
        short locale,
        boolean includeAll
    ) {
    	return this.getLongText(name, locale, false, includeAll);
    }

    /**
     * @param name
     * @param locale
     * @param codeAsKey
     * @param includeAll
     * @return
     */
    public Map getShortText(
        String name,
        short locale,
        boolean codeAsKey,
        boolean includeAll
    ) {
        SortedMap<Object,Object> shortTexts = null;
        String key = this.getKey(name, locale, codeAsKey, includeAll);
        if((shortTexts = cachedShortTexts.get(key)) == null) {
            prepareShortText(
                name,
                locale,
                codeAsKey,
                key,
                includeAll
            );
            shortTexts = cachedShortTexts.get(key);
        }
        return shortTexts;      
    }

    /**
     * @param name
     * @param locale
     * @param includeAll
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<Short,String> getShortTextByCode(
        String name,
        short locale,
        boolean includeAll
    ) {
    	return this.getShortText(name, locale, true, includeAll);
    }

    /**
     * @param name
     * @param locale
     * @param includeAll
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String,Short> getShortTextByText(
        String name,
        short locale,
        boolean includeAll
    ) {
    	return this.getShortText(name, locale, false, includeAll);
    }

    /**
     * @param name
     * @param includeAll
     * @return
     */
    public SortedMap getIconKeys(
        String name,
        boolean includeAll
    ) {
        SortedMap<Short,String> iconKeys = null;
        String key = name + ":" + includeAll;
        if((iconKeys = cachedIconKeys.get(key)) == null) {
        	SortedMap<Short,CodeEntry> codeEntryContainer = cachedCodeEntryContainer.get(name);
            if(codeEntryContainer == null) {
                return null;
            }
            iconKeys = new TreeMap<Short,String>();
            for(Map.Entry<Short,CodeEntry> entry: codeEntryContainer.entrySet()) {
                if(includeAll || entryIsValid(entry.getValue())) {
                    iconKeys.put(
                        entry.getKey(), 
                        entry.getValue().getIconKey()
                    );
                }
            }
            cachedIconKeys.put(
                key,
                iconKeys
            );
        }
        return iconKeys;
    }

    /**
     * @param name
     * @param includeAll
     * @return
     */
    public SortedMap getColors(
        String name,
        boolean includeAll
    ) {
        SortedMap<Short,String> colors = null;
        String key = name + ":" + includeAll;
        if((colors = cachedColors.get(key)) == null) {
        	SortedMap<Short,CodeEntry> codeEntryContainer = cachedCodeEntryContainer.get(name);
            if(codeEntryContainer == null) {
                return null;
            }
            colors = new TreeMap<Short,String>();
            for(Map.Entry<Short,CodeEntry> entry: codeEntryContainer.entrySet()) {
                if(includeAll || entryIsValid(entry.getValue())) {
                    colors.put(
                        entry.getKey(), 
                        entry.getValue().getColor()
                    );
                }
            }
            cachedColors.put(
                key,
                colors
            );
        }
        return colors;
    }

    /**
     * @param name
     * @param includeAll
     * @return
     */
    public SortedMap getBackColors(
        String name,
        boolean includeAll
    ) {
        SortedMap<Short,String> backColors = null;
        String key = name + ":" + includeAll;
        if((backColors = cachedBackColors.get(key)) == null) {
        	SortedMap<Short,CodeEntry> codeEntryContainer = cachedCodeEntryContainer.get(name);
            if(codeEntryContainer == null) {
                return null;
            }
            backColors = new TreeMap<Short,String>();
            for(Map.Entry<Short,CodeEntry> entry: codeEntryContainer.entrySet()) {
                if(includeAll || entryIsValid(entry.getValue())) {
                    backColors.put(
                        entry.getKey(), 
                        entry.getValue().getBackColor()
                    );
                }
            }
            cachedBackColors.put(
                key,
                backColors
            );
        }
        return backColors;
    }

    /**
     * @param value
     * @param feature
     * @return
     */
    public short findCodeFromValue(
        String value,
        String feature
    ) {
        short code = 0;
        if((value != null) && !value.isEmpty()) {
            value = value.toUpperCase();
            for(short locale = 0; locale < 255; locale++) {
            	String key = this.getKey(feature, locale, true, false);
            	// Prio 1: exact match for short texts
            	{
	            	if(cachedShortTexts.get(key) == null) {            		
	                    if(!prepareShortText(feature, locale, true, key, false)) {
	                    	return code;
	                    }
	            	}
	                @SuppressWarnings("unchecked")
                    Map<Object,Object> shortTexts = this.getShortText(feature, locale, true, false);
	                for(Map.Entry<Object,Object> entry: shortTexts.entrySet()) {
	                    String text = ((String)entry.getValue()).trim();
	                    if((text != null) && text.toUpperCase().equals(value)) {
	                    	return (Short)entry.getKey();
	                    }
	                }
            	}
            	// Prio 2: try with long texts
            	{
	            	if(cachedLongTexts.get(key) == null) {            		
	                    if(!prepareLongText(feature, locale, true, key, false)) {
	                    	return code;
	                    }
	            	}
	                @SuppressWarnings("unchecked")
	                Map<Object,Object> longTexts = this.getLongText(feature, locale, true, false);
	                Integer lastMatchLength = null;
	                Short lastMatchCode = null;
	                for(Map.Entry<Object,Object> entry: longTexts.entrySet()) {
	                    String text = ((String)entry.getValue()).trim();
	                    if((text != null) && text.toUpperCase().indexOf(value) >= 0) {
	    	    			if(lastMatchLength == null || text.length() < lastMatchLength) {
	    	    				lastMatchCode = (Short)entry.getKey();
	    	    				lastMatchLength = text.length();
	    	    			}
	                    }
	                }
	                if(lastMatchCode != null) {
	                	return lastMatchCode;
	                }
            	}
            }
        }
        return code;
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 8069002786499927870L;
    
    // Time to live of cached elements is TTL unless refresh is explicitly called.
    private static final long TTL = 86400000L;
    
    private static long refreshedAt = 0L;
    private static Path codeSegmentIdentity = null;
    private static Map<String,SortedMap<Short,CodeEntry>> cachedCodeEntryContainer = new HashMap<String,SortedMap<Short,CodeEntry>>();    
    private static final Map<String,SortedMap<Object,Object>> cachedShortTexts = new ConcurrentHashMap<String,SortedMap<Object,Object>>();
    private static final Map<String,SortedMap<Object,Object>> cachedLongTexts = new ConcurrentHashMap<String,SortedMap<Object,Object>>();
    private static final Map<String,SortedMap<Short,String>> cachedIconKeys = new ConcurrentHashMap<String,SortedMap<Short,String>>();
    private static final Map<String,SortedMap<Short,String>> cachedColors = new ConcurrentHashMap<String,SortedMap<Short,String>>();
    private static final Map<String,SortedMap<Short,String>> cachedBackColors = new ConcurrentHashMap<String,SortedMap<Short,String>>();

}

//--- End of File -----------------------------------------------------------
