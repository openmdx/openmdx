/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Codes.java,v 1.22 2011/11/18 13:36:43 wfro Exp $
 * Description: Codes
 * Revision:    $Revision: 1.22 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/18 13:36:43 $
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

public final class Codes implements Serializable {

	static class CodeEntry {
		
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
	
    //-------------------------------------------------------------------------
    public Codes(
        RefObject_1_0 codeSegment
    ) {
    	if(System.currentTimeMillis() > refreshedAt + TTL) {
    		PersistenceManager pm = JDOHelper.getPersistenceManager(codeSegment);
    		codeSegmentIdentity = codeSegment.refGetPath();
    		refresh(pm);
    	}
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static void refresh(
    	PersistenceManager pm
    ) {
    	if(codeSegmentIdentity != null) {
    		RefObject_1_0 codeSegment = (RefObject_1_0)pm.getObjectById(codeSegmentIdentity);
	    	Map<String,SortedMap<Short,CodeEntry>> codeEntryContainers = new ConcurrentHashMap<String,SortedMap<Short,CodeEntry>>();    	
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
	        cachedCodeEntryContainer = codeEntryContainers;
	        cachedShortTexts.clear();
	        cachedLongTexts.clear();
	        cachedIconKeys.clear();
	        cachedColors.clear();
	        cachedBackColors.clear();
    		refreshedAt = System.currentTimeMillis();
    	}
    }

    //-------------------------------------------------------------------------
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

    //-------------------------------------------------------------------------
    /**
     * @return Returns true if at least one short text is locale-specific. Returns false
     *         if all short texts are fallback texts (taken from locale 0).  
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

    //-------------------------------------------------------------------------
    /**
     * @return Returns true if at least one long text is locale-specific. Returns false
     *         if all long texts are fallback texts (taken from locale 0).  
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

    //-------------------------------------------------------------------------
    private String getKey(
    	String name,
    	short locale,
    	boolean codeAsKey,
    	boolean includeAll
    ) {
    	return name + ":" + locale + ":" + codeAsKey + ":" + includeAll;
    }
    
    //-------------------------------------------------------------------------
    public SortedMap<Object,Object> getLongText(
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

    //-------------------------------------------------------------------------
    public SortedMap<Object,Object> getShortText(
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

    //-------------------------------------------------------------------------
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

    //-------------------------------------------------------------------------
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

    //-------------------------------------------------------------------------
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

    //-------------------------------------------------------------------------
    public short findCodeFromValue(
        String value,
        String feature
    ) {
        short code = 0;
        if((value != null) && !value.isEmpty()) {
            value = value.toUpperCase();
            for(short locale = 0; locale < 255; locale++) {
            	String key = this.getKey(feature, locale, true, false);
            	if(cachedLongTexts.get(key) == null) {            		
                    if(!prepareLongText(feature, locale, true, key, false)) {
                    	return code;
                    }
            	}
                Map<Object,Object> longTexts = this.getLongText(feature, locale, true, false);
                for(Map.Entry<Object,Object> entry: longTexts.entrySet()) {
                    String longText = ((String)entry.getValue()).trim();
                    if((longText != null) && longText.toUpperCase().startsWith(value)) {
                    	return (Short)entry.getKey();
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
