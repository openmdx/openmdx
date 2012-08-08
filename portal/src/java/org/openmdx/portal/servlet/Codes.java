/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Codes.java,v 1.20 2010/12/13 15:20:08 wfro Exp $
 * Description: Codes
 * Revision:    $Revision: 1.20 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/12/13 15:20:08 $
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
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.jdo.JDOHelper;
import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

public final class Codes
implements Serializable {

    //-------------------------------------------------------------------------
    public Codes(
        RefObject_1_0 codeSegment
    ) {
        this.codeSegment = codeSegment;
        this.valueContainerMap = new ConcurrentHashMap<String,RefObject>();
        this.shortTextMap = new ConcurrentHashMap<String,SortedMap<Object,Object>>();
        this.longTextMap = new ConcurrentHashMap<String,SortedMap<Object,Object>>();
        this.iconKeyMap = new ConcurrentHashMap<String,SortedMap<Short,String>>();
        this.colorMap = new ConcurrentHashMap<String,SortedMap<Short,String>>();
        this.backColorMap = new ConcurrentHashMap<String,SortedMap<Short,String>>();      
        this.refresh();
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void refresh(
    ) {
        this.valueContainerMap.clear();
        JDOHelper.getPersistenceManager(this.codeSegment).refresh(this.codeSegment);        
        Collection valueContainers = (Collection)codeSegment.refGetValue("valueContainer");
        for(
            Iterator i = valueContainers.iterator();
            i.hasNext();
        ) {
            RefObject valueContainer = (RefObject)i.next();
            SysLog.detail("preparing code value container", valueContainer.refMofId());
            Set<String> names = (Set<String>)valueContainer.refGetValue("name");
            for(String name: names) {
                this.valueContainerMap.put(
                    name,
                    valueContainer
                );
            } 
        }
        this.shortTextMap.clear();
        this.longTextMap.clear();
        this.iconKeyMap.clear();
        this.colorMap.clear();
        this.backColorMap.clear();      
    }

    //-------------------------------------------------------------------------
    private boolean entryIsValid(
        RefObject entry
    ) {
        Date validFrom = (Date)entry.refGetValue("validFrom");
        Date validTo = (Date)entry.refGetValue("validTo");
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
    private boolean prepareShortText(
        String valueContainerName,
        short locale,
        boolean codeAsKey,
        String key,
        boolean includeAll
    ) {
        RefObject valueContainer = (RefObject)this.valueContainerMap.get(valueContainerName);
        if(valueContainer == null) {
            return false;
        }
        boolean hasLocaleSpecificTexts = false;
        SortedMap<Object,Object> shortTexts = new TreeMap<Object,Object>();
        Collection entries = (Collection)valueContainer.refGetValue("entry");
        for(Iterator i = entries.iterator(); i.hasNext(); ) {
            RefObject entry = (RefObject)i.next();
            if(includeAll || this.entryIsValid(entry)) {
                Short code = new Short((short)0);
                try {
                    code = new Short(new Path(entry.refMofId()).getBase());
                } 
                catch(Exception e) {}
                List texts = (List)entry.refGetValue("shortText");
                Object text;
                if(texts.size() > locale) {
                	text = texts.get(locale);
                	hasLocaleSpecificTexts = true;
                } else {
                	text = texts.isEmpty() ? "" : texts.get(0);
                }
                if(codeAsKey) {
                    shortTexts.put(code, text);
                } else {
                    shortTexts.put(text, code);
                }
            }
        }
        this.shortTextMap.put(
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
    private boolean prepareLongText(
        String valueContainerName,
        short locale,
        boolean codeAsKey,
        String key,
        boolean includeAll
    ) {
        RefObject valueContainer = (RefObject)this.valueContainerMap.get(valueContainerName);
        if(valueContainer == null) {
            return false;
        }
        boolean hasLocaleSpecificTexts = false;
        SortedMap<Object,Object> longTexts = new TreeMap<Object,Object>();
        Collection entries = (Collection)valueContainer.refGetValue("entry");
        for(Iterator i = entries.iterator(); i.hasNext(); ) {
            RefObject entry = (RefObject)i.next();
            if(includeAll || this.entryIsValid(entry)) {
                Short code = new Short((short)0);
                try {
                    code = new Short(new Path(entry.refMofId()).getBase());
                } 
                catch(Exception e) {}
                List texts = (List)entry.refGetValue("longText");
                Object text;
                if(texts.size() > locale) {
                	text = texts.get(locale);
                	hasLocaleSpecificTexts = true;
                } else {
                	text = texts.isEmpty() ? "" : texts.get(0);
                }
                if(codeAsKey) {
                    longTexts.put(code, text);
                } else {
                    if(text == null) {
                        throw new RuntimeServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CONFIGURATION, 
                            "text of code value entry can not be null",
                            new BasicException.Parameter("container name", valueContainerName),
                            new BasicException.Parameter("locale", locale),
                            new BasicException.Parameter("lookup id", key),
                            new BasicException.Parameter("entry", entry.refMofId()),                            
                            new BasicException.Parameter("texts", texts)
                        );
                    }
                    longTexts.put(text, code);
                }
            }
        }
        this.longTextMap.put(
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
        if((longTexts = this.longTextMap.get(key)) == null) {
            this.prepareLongText(
                name,
                locale,
                codeAsKey,
                key,
                includeAll
            );
            longTexts = this.longTextMap.get(key);
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
        if((shortTexts = this.shortTextMap.get(key)) == null) {
            this.prepareShortText(
                name,
                locale,
                codeAsKey,
                key,
                includeAll
            );
            shortTexts = this.shortTextMap.get(key);
        }
        return shortTexts;      
    }

    //-------------------------------------------------------------------------
    public SortedMap getIconKeys(
        String name,
        boolean includeAll
    ) {
        SortedMap<Short,String> iconKeys = null;
        if((iconKeys = this.iconKeyMap.get(name + ":" + includeAll)) == null) {
            RefObject valueContainer = (RefObject)this.valueContainerMap.get(name);
            if(valueContainer == null) {
                return null;
            }
            iconKeys = new TreeMap<Short,String>();
            Collection entries = (Collection)valueContainer.refGetValue("entry");
            for(Iterator i = entries.iterator(); i.hasNext(); ) {
                RefObject entry = (RefObject)i.next();
                if(includeAll || this.entryIsValid(entry)) {
                    Short code = new Short((short)0);
                    try {
                        code = new Short(new Path(entry.refMofId()).getBase());
                    } 
                    catch(Exception e) {}
                    iconKeys.put(
                        code, 
                        (String)entry.refGetValue("iconKey")
                    );
                }
            }
            this.iconKeyMap.put(
                name + ":" + includeAll,
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
        if((colors = this.colorMap.get(name + ":" + includeAll)) == null) {
            RefObject valueContainer = (RefObject)this.valueContainerMap.get(name);
            if(valueContainer == null) {
                return null;
            }
            colors = new TreeMap<Short,String>();
            Collection entries = (Collection)valueContainer.refGetValue("entry");
            for(Iterator i = entries.iterator(); i.hasNext(); ) {
                RefObject entry = (RefObject)i.next();
                if(includeAll || this.entryIsValid(entry)) {
                    Short code = new Short((short)0);
                    try {
                        code = new Short(new Path(entry.refMofId()).getBase());
                    } 
                    catch(Exception e) {}
                    colors.put(
                        code, 
                        (String)entry.refGetValue("color")
                    );
                }
            }
            this.colorMap.put(
                name + ":" + includeAll,
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
        if((backColors = this.backColorMap.get(name + ":" + includeAll)) == null) {
            RefObject valueContainer = (RefObject)this.valueContainerMap.get(name);
            if(valueContainer == null) {
                return null;
            }
            backColors = new TreeMap<Short,String>();
            Collection entries = (Collection)valueContainer.refGetValue("entry");
            for(Iterator i = entries.iterator(); i.hasNext(); ) {
                RefObject entry = (RefObject)i.next();
                if(includeAll || this.entryIsValid(entry)) {
                    Short code = new Short((short)0);
                    try {
                        code = new Short(new Path(entry.refMofId()).getBase());
                    } 
                    catch(Exception e) {}
                    backColors.put(
                        code, 
                        (String)entry.refGetValue("backColor")
                    );
                }
            }
            this.backColorMap.put(
                name + ":" + includeAll,
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
        if((value != null) && (value.length() > 0)) {
            value = value.toUpperCase();
            for(short locale = 0; locale < 255; locale++) {
            	String key = this.getKey(feature, locale, true, false);
            	if(this.longTextMap.get(key) == null) {            		
                    if(!this.prepareLongText(feature, locale, true, key, false)) {
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
    
    private final RefObject_1_0 codeSegment;
    private final Map<String,RefObject> valueContainerMap;
    private final Map<String,SortedMap<Object,Object>> shortTextMap;
    private final Map<String,SortedMap<Object,Object>> longTextMap;
    private final Map<String,SortedMap<Short,String>> iconKeyMap;
    private final Map<String,SortedMap<Short,String>> colorMap;
    private final Map<String,SortedMap<Short,String>> backColorMap;
}

//--- End of File -----------------------------------------------------------
