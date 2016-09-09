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
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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
import java.util.Collections;
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

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jmi.reflect.RefObject;
import javax.resource.cci.MappedRecord;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.omg.mof.spi.Names;
import org.openmdx.application.dataprovider.cci.JmiHelper;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
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
	 * CodeContainer
	 *
	 */
	public static class CodeContainer {
		
		/**
		 * Constructor 
		 *
		 * @param id
		 * @param names
		 * @param codeEntries
		 */
		public CodeContainer(
			Path id,
			Set<String> names
		) {
			this.id = id;
			this.names = names;
			this.codeEntries = null;
		}

		/**
		 * Retrieve id.
		 *
		 * @return Returns the id.
		 */
		public Path getId() {
			return this.id;
		}
		
		/**
		 * Retrieve names.
		 *
		 * @return Returns the names.
		 */
		public Set<String> getNames(
		) {
			return this.names;
		}
		
		/**
		 * Retrieve code entries.
		 *
		 * @return Returns the entries.
		 */
		public SortedMap<Short,CodeEntry> getCodeEntries(
			PersistenceManager pm
		) {
			if(this.codeEntries == null) {
				Model_1_0 model = Model_1Factory.getModel();
				RefObject_1_0 codeEntryContainer = (RefObject_1_0)pm.getObjectById(this.id);
	    		SortedMap<Short,CodeEntry> codeEntries = new TreeMap<Short,CodeEntry>();
	            // Try to get query for code entry retrieval. Set fetch 
	            // plan to FetchPlan.ALL in order to improve performance. 
	    		Query entryQuery = null;
	    		try {
	    			ModelElement_1_0 entryDef = model.getElementType(
	    				model.getReferenceType(codeEntryContainer.refGetPath().getChild("entry"))
	    			);
	    			String entryClassName = Names.toClassName(
	    				(String)entryDef.getQualifiedName(),
	    				Names.JMI1_PACKAGE_SUFFIX
	    			);
	    			Class<?> entryClass = Classes.getApplicationClass(entryClassName);
	    			entryQuery = pm.newQuery(entryClass);
	    			entryQuery.getFetchPlan().setGroup(FetchPlan.ALL);	
	    			entryQuery.getFetchPlan().setFetchSize(Integer.MAX_VALUE);    			
	    		} catch(Exception ignore) {}
	    		@SuppressWarnings("unchecked")
	            RefContainer<RefObject_1_0> entries = (RefContainer<RefObject_1_0>)codeEntryContainer.refGetValue("entry");
	    		for(RefObject_1_0 entry: entryQuery == null ? entries : entries.refGetAll(entryQuery)) {
	                Short code = 0;
	                try {
	                    code = new Short(entry.refGetPath().getLastSegment().toClassicRepresentation());
	                } catch(Exception ignore) {}
	                @SuppressWarnings("unchecked")
	                List<String> shortTexts = (List<String>)entry.refGetValue("shortText");
	                @SuppressWarnings("unchecked")
	                List<String> longTexts = (List<String>)entry.refGetValue("longText");
	    			codeEntries.put(
	    				code,
	    				new CodeEntry(
	    					entry.refGetPath(),
	    					new ArrayList<String>(shortTexts),
	    					new ArrayList<String>(longTexts),
	    					(String)entry.refGetValue("iconKey"),
	    					(String)entry.refGetValue("color"),
	    					(String)entry.refGetValue("backColor"),        					
	    			        (Date)entry.refGetValue("validFrom"),
	    			        (Date)entry.refGetValue("validTo")
	    			    )
	    			);
	    		}
	    		this.codeEntries = codeEntries;
			}
			return this.codeEntries;
		}

		private final Path id;
		private final Set<String> names;
		private SortedMap<Short,CodeEntry> codeEntries;
	}

	/**
	 * CodeContainerManager
	 *
	 */
	static class CodeContainerManager {

		/**
		 * Constructor 
		 *
		 * @param codeSegment
		 */
		public CodeContainerManager(
			RefObject_1_0 codeSegment
		) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(codeSegment);
			this.pm = pm.getPersistenceManagerFactory().getPersistenceManager(
    			UserObjects.getPrincipalChain(pm).toString(),
    			null
    		);
			this.codeSegmentIdentity = codeSegment.refGetPath();
			this.refreshedAt = 0L;
		}

		/**
		 * Refresh codes.
		 * 
		 */
		public void refresh(
		) {
			this.refreshedAt = 0L;
		}

		/**
		 * Get code entries of given container.
		 * 
		 * @param name
		 * @return
		 */
		public SortedMap<Short,CodeEntry> getCodeEntries(
			String name
		) {
			if(System.currentTimeMillis() > this.refreshedAt + TTL) {
				PersistenceManager pm = this.pm.getPersistenceManagerFactory().getPersistenceManager(
	    			UserObjects.getPrincipalChain(this.pm).toString(),
	    			null
	    		);
				Model_1_0 model = Model_1Factory.getModel();
				RefObject_1_0 codeSegment = (RefObject_1_0)pm.getObjectById(this.codeSegmentIdentity);
		        // Try to get query for code entry container retrieval. Set fetch 
		        // plan to FetchPlan.ALL in order to improve performance. 
		        Query codeEntryContainerQuery = null;
				try {
					ModelElement_1_0 codeEntryContainerDef = model.getElementType(
						model.getReferenceType(codeSegment.refGetPath().getChild("valueContainer"))
					);
					String codeEntryContainerClassName = Names.toClassName(
						(String)codeEntryContainerDef.getQualifiedName(),
						Names.JMI1_PACKAGE_SUFFIX
					);
					Class<?> codeEntryContainerClass = Classes.getApplicationClass(codeEntryContainerClassName);
			        codeEntryContainerQuery = pm.newQuery(codeEntryContainerClass);
			        codeEntryContainerQuery.getFetchPlan().setGroup(FetchPlan.ALL);
			        codeEntryContainerQuery.getFetchPlan().setFetchSize(Integer.MAX_VALUE);
				} catch(Exception ignore) {}
		        @SuppressWarnings("unchecked")
		        RefContainer<RefObject_1_0> codeEntryContainers = (RefContainer<RefObject_1_0>)codeSegment.refGetValue("valueContainer");
		        Map<String,CodeContainer> codeContainers = new TreeMap<String,CodeContainer>();
		        for(RefObject_1_0 codeEntryContainer: codeEntryContainerQuery == null ? codeEntryContainers : codeEntryContainers.refGetAll(codeEntryContainerQuery)) {
		        	@SuppressWarnings("unchecked")
		            Set<String> codeEntryContainerNames = (Set<String>)codeEntryContainer.refGetValue("name");
		        	Set<String> containerNames = new HashSet<String>(codeEntryContainerNames);
		        	containerNames.add(
		        		codeEntryContainer.refGetPath().getLastSegment().toClassicRepresentation()
		        	);
		        	CodeContainer codeContainer = new CodeContainer(
		        		codeEntryContainer.refGetPath(),
		        		containerNames
		        	);
		        	for(String containerName: containerNames) {
		        		codeContainers.put(
		        			containerName,
		        			codeContainer
		        		);
		        	}
		        }
		        try {
		        	this.pm.close();
		        } catch(Exception ignore) {}
		        this.pm = pm;
				this.codeContainers = codeContainers;
				this.refreshedAt = System.currentTimeMillis();				
			}
			CodeContainer codeContainer = this.codeContainers.get(name);
			return codeContainer == null
				? null 
				: codeContainer.getCodeEntries(this.pm);
		}

		private long refreshedAt = 0L;
		private final Path codeSegmentIdentity;
		private PersistenceManager pm;
		private Map<String,CodeContainer> codeContainers = Collections.emptyMap();
	}

    /**
     * Constructor.
     *
     * @param codeSegment
     * @throws ServiceException
     */
    public Codes(
        RefObject_1_0 codeSegment
    ) {
    	this.codeContainerManager = new CodeContainerManager(codeSegment);
    }

    /**
     * Refresh codes.
     * 
     * @param pm
     * @throws ServiceException
     */
    public void refresh(
    ) {
    	if(this.codeContainerManager != null) {
    		this.codeContainerManager.refresh();
	        cachedShortTexts.clear();
	        cachedLongTexts.clear();
	        cachedIconKeys.clear();
	        cachedColors.clear();
	        cachedBackColors.clear();
    	}
    }

	/**
	 * Store codes to specified segment.
	 * 
	 * @param codeProviderIdentity
	 * @param segmentName
	 * @param pmf
	 * @param codes
	 */
    public static void storeCodes(
    	PersistenceManager pm,
    	Map<Path,ObjectRecord> codes
    	) throws ServiceException {
    	String messagePrefix = new Date() + "  ";
    	SysLog.info("Storing " + codes.size() + " code entries");
    	System.out.println(messagePrefix + "Storing " + codes.size() + " code entries");
    	// Load objects in multiple runs in order to resolve object dependencies.
    	Map<Path,RefObject> loadedObjects = new HashMap<Path,RefObject>(); 
    	for(int run = 0; run < 5; run++) {
    		boolean hasNewObjects = false;
    		for(
    			Iterator<ObjectRecord> j = codes.values().iterator(); 
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
    					newEntry.refInitialize(false, false, false);
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
							@SuppressWarnings("unchecked")
							RefContainer<RefObject_1_0> container = (RefContainer<RefObject_1_0>)parent.refGetValue(
								entryPath.getSegment(entryPath.size() - 2).toClassicRepresentation()
							);
							container.refAdd(
								QualifierType.REASSIGNABLE,
								entryPath.getSegment(entryPath.size() - 1).toClassicRepresentation(),
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
	 * 
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
		Map<Path,ObjectRecord> codes = new HashMap<Path,ObjectRecord>();
		// Derive class of value containers and entries from existing value containers
		String classNameContainer = null;
		String classNameEntry = null;
        @SuppressWarnings("unchecked")
        Collection<RefObject_1_0> valueContainers = (Collection<RefObject_1_0>)codeSegment.refGetValue("valueContainer");
        if(!valueContainers.isEmpty()) {
        	RefObject_1_0 valueContainer = valueContainers.iterator().next();
        	classNameContainer = valueContainer.refClass().refMofId();
        	@SuppressWarnings("unchecked")
            Collection<RefObject_1_0> entries = (Collection<RefObject_1_0>)valueContainer.refGetValue("entry");
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
     * Test whether entry is valid.
     * 
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
     * if all short texts are fallback texts (taken from locale 0).
     * 
     * @param name
     * @param locale
     * @param codeAsKey
     * @param key
     * @param includeAll
     * @return
     */
    private boolean prepareShortText(
        String name,
        short locale,
        boolean codeAsKey,
        String key,
        boolean includeAll
    ) {
    	SortedMap<Short,CodeEntry> codeEntries = this.codeContainerManager.getCodeEntries(name);
        if(codeEntries == null) {
            return false;
        }
        boolean hasLocaleSpecificTexts = false;
        SortedMap<Object,Object> shortTexts = new TreeMap<Object,Object>();
        for(Map.Entry<Short,CodeEntry> entry: codeEntries.entrySet()) {
            if(includeAll || entryIsValid(entry.getValue())) {
                List<String> texts = entry.getValue().getShortText();
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
        this.cachedShortTexts.put(
            key,
            shortTexts
        );
        return hasLocaleSpecificTexts;
    }

    /**
     * Returns true if at least one long text is locale-specific. Returns false
     * if all long texts are fallback texts (taken from locale 0).
     * 
     * @param name
     * @param locale
     * @param codeAsKey
     * @param key
     * @param includeAll
     * @return
     */
    private boolean prepareLongText(
        String name,
        short locale,
        boolean codeAsKey,
        String key,
        boolean includeAll
    ) {
    	SortedMap<Short,CodeEntry> codeEntries = this.codeContainerManager.getCodeEntries(name);
        if(codeEntries == null) {
            return false;
        }
        boolean hasLocaleSpecificTexts = false;
        SortedMap<Object,Object> longTexts = new TreeMap<Object,Object>();
        for(Map.Entry<Short,CodeEntry> entry: codeEntries.entrySet()) {
            if(includeAll || entryIsValid(entry.getValue())) {
                List<String> texts = entry.getValue().getLongText();
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
        this.cachedLongTexts.put(
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
    public Map<?,?> getLongText(
        String name,
        short locale,
        boolean codeAsKey,
        boolean includeAll
    ) {
        SortedMap<Object,Object> longTexts = null;
        String key = this.getKey(name, locale, codeAsKey, includeAll);
        if((longTexts = cachedLongTexts.get(key)) == null) {
            this.prepareLongText(
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
     * Get long texts ordered by code.
     * 
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
    	return (Map<Short,String>)this.getLongText(name, locale, true, includeAll);
    }

    /**
     * Get long texts ordered by text.
     * 
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
    	return (Map<String,Short>)this.getLongText(name, locale, false, includeAll);
    }

    /**
     * Get short texts.
     * 
     * @param name
     * @param locale
     * @param codeAsKey
     * @param includeAll
     * @return
     */
    public Map<?,?> getShortText(
        String name,
        short locale,
        boolean codeAsKey,
        boolean includeAll
    ) {
        SortedMap<Object,Object> shortTexts = null;
        String key = this.getKey(name, locale, codeAsKey, includeAll);
        if((shortTexts = this.cachedShortTexts.get(key)) == null) {
            this.prepareShortText(
                name,
                locale,
                codeAsKey,
                key,
                includeAll
            );
            shortTexts = this.cachedShortTexts.get(key);
        }
        return shortTexts;      
    }

    /**
     * Get short texts ordered by code.
     * 
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
    	return (Map<Short,String>)this.getShortText(name, locale, true, includeAll);
    }

    /**
     * Get short texts ordered by text.
     * 
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
    	return (Map<String,Short>)this.getShortText(name, locale, false, includeAll);
    }

    /**
     * Get icon keys.
     * 
     * @param name
     * @param includeAll
     * @return
     */
    public SortedMap<Short,String> getIconKeys(
        String name,
        boolean includeAll
    ) {
        SortedMap<Short,String> iconKeys = null;
        String key = name + ":" + includeAll;
        if((iconKeys = this.cachedIconKeys.get(key)) == null) {
        	SortedMap<Short,CodeEntry> codeEntries = this.codeContainerManager.getCodeEntries(name);
            if(codeEntries == null) {
                return null;
            }
            iconKeys = new TreeMap<Short,String>();
            for(Map.Entry<Short,CodeEntry> entry: codeEntries.entrySet()) {
                if(includeAll || entryIsValid(entry.getValue())) {
                    iconKeys.put(
                        entry.getKey(), 
                        entry.getValue().getIconKey()
                    );
                }
            }
            this.cachedIconKeys.put(
                key,
                iconKeys
            );
        }
        return iconKeys;
    }

    /**
     * Get colors.
     * 
     * @param name
     * @param includeAll
     * @return
     */
    public SortedMap<Short,String> getColors(
        String name,
        boolean includeAll
    ) {
        SortedMap<Short,String> colors = null;
        String key = name + ":" + includeAll;
        if((colors = this.cachedColors.get(key)) == null) {
        	SortedMap<Short,CodeEntry> codeEntries = this.codeContainerManager.getCodeEntries(name);
            if(codeEntries == null) {
                return null;
            }
            colors = new TreeMap<Short,String>();
            for(Map.Entry<Short,CodeEntry> entry: codeEntries.entrySet()) {
                if(includeAll || entryIsValid(entry.getValue())) {
                    colors.put(
                        entry.getKey(), 
                        entry.getValue().getColor()
                    );
                }
            }
            this.cachedColors.put(
                key,
                colors
            );
        }
        return colors;
    }

    /**
     * Get back colors.
     * 
     * @param name
     * @param includeAll
     * @return
     */
    public SortedMap<Short,String> getBackColors(
        String name,
        boolean includeAll
    ) {
        SortedMap<Short,String> backColors = null;
        String key = name + ":" + includeAll;
        if((backColors = this.cachedBackColors.get(key)) == null) {
        	SortedMap<Short,CodeEntry> codeEntries = this.codeContainerManager.getCodeEntries(name);
            if(codeEntries == null) {
                return null;
            }
            backColors = new TreeMap<Short,String>();
            for(Map.Entry<Short,CodeEntry> entry: codeEntries.entrySet()) {
                if(includeAll || entryIsValid(entry.getValue())) {
                    backColors.put(
                        entry.getKey(), 
                        entry.getValue().getBackColor()
                    );
                }
            }
            this.cachedBackColors.put(
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
        if((value != null) && !value.isEmpty()) {
            value = value.toUpperCase();
            SortedMap<Short,CodeEntry> codeEntries = this.codeContainerManager.getCodeEntries(feature);
            if(codeEntries != null) {
	            // Prio 1: Exact match short texts
	            for(Map.Entry<Short,CodeEntry> entry: codeEntries.entrySet()) {
	            	for(String text: entry.getValue().getShortText()) {
	                    if((text != null) && text.trim().equalsIgnoreCase(value)) {
	                    	return entry.getKey();
	                    }
	            	}
	            }
	        	// Prio 2: Exact match long text
	            for(Map.Entry<Short,CodeEntry> entry: codeEntries.entrySet()) {
	            	for(String text: entry.getValue().getLongText()) {
	                    if((text != null) && text.trim().equalsIgnoreCase(value)) {
	                    	return entry.getKey();
	                    }
	            	}
	            }
	            // Prio 3: Best match long text
	            Integer bestMatchingLength = null;
	            Short bestMatchingCode = null;
	            for(Map.Entry<Short,CodeEntry> entry: codeEntries.entrySet()) {
	            	for(String text: entry.getValue().getLongText()) {
	                    if((text != null) && text.trim().toUpperCase().indexOf(value) >= 0) {
	    	    			if(bestMatchingLength == null || text.length() < bestMatchingLength) {
	    	    				bestMatchingCode = entry.getKey();
	    	    				bestMatchingLength = text.length();
	    	    			}
	                    }
	            	}
	            }
	            if(bestMatchingCode != null) {
	            	return bestMatchingCode;
	            }
            }
        }
        return 0;
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 8069002786499927870L;
    
    // Time to live of cached elements is TTL unless refresh is explicitly called.
    private static final long TTL = 86400000L;
    
    private final CodeContainerManager codeContainerManager;  
    private final Map<String,SortedMap<Object,Object>> cachedShortTexts = new ConcurrentHashMap<String,SortedMap<Object,Object>>();
    private final Map<String,SortedMap<Object,Object>> cachedLongTexts = new ConcurrentHashMap<String,SortedMap<Object,Object>>();
    private final Map<String,SortedMap<Short,String>> cachedIconKeys = new ConcurrentHashMap<String,SortedMap<Short,String>>();
    private final Map<String,SortedMap<Short,String>> cachedColors = new ConcurrentHashMap<String,SortedMap<Short,String>>();
    private final Map<String,SortedMap<Short,String>> cachedBackColors = new ConcurrentHashMap<String,SortedMap<Short,String>>();

}

//--- End of File -----------------------------------------------------------
