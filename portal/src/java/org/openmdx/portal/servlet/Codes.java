/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Codes.java,v 1.15 2009/03/08 18:03:19 wfro Exp $
 * Description: Codes
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:03:19 $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jmi.reflect.RefObject;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

@SuppressWarnings("unchecked")
public final class Codes
implements Serializable {

    //-------------------------------------------------------------------------
    public Codes(
        RefObject_1_0 codeSegment
    ) {
        this.codeSegment = codeSegment;
        this.valueContainerMap = new HashMap();
        this.shortTextMap = new HashMap();
        this.longTextMap = new HashMap();
        this.iconKeyMap = new HashMap();
        this.colorMap = new HashMap();
        this.backColorMap = new HashMap();      
        this.refresh();
    }

    //-------------------------------------------------------------------------
    public void refresh(
    ) {
        this.valueContainerMap.clear();
        this.codeSegment.refRefresh();        
        Collection valueContainers = (Collection)codeSegment.refGetValue("valueContainer");
        for(
                Iterator i = valueContainers.iterator();
                i.hasNext();
        ) {
            RefObject valueContainer = (RefObject)i.next();
            AppLog.detail("preparing code value container", valueContainer.refMofId());
            Set name = (Set)valueContainer.refGetValue("name");
            for(Iterator j = name.iterator(); j.hasNext(); ) {
                this.valueContainerMap.put(
                    j.next(),
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
    private SortedMap cacheShortText(
        String valueContainerName,
        short locale,
        boolean codeAsKey,
        String shortTextId,
        boolean includeAll
    ) {
        RefObject valueContainer = (RefObject)this.valueContainerMap.get(valueContainerName);
        if(valueContainer == null) {
            return null;
        }
        SortedMap shortTexts = new TreeMap();
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
                Object text = texts.size() > locale
                ? texts.get(locale)
                    : texts.isEmpty() ? "" : texts.get(0);          
                if(codeAsKey) {
                    shortTexts.put(code, text);
                }
                else {
                    shortTexts.put(text, code);
                }
            }
        }
        this.shortTextMap.put(
            shortTextId,
            shortTexts
        );      
        return shortTexts;   
    }

    //-------------------------------------------------------------------------
    private SortedMap cacheLongText(
        String valueContainerName,
        short locale,
        boolean codeAsKey,
        String longTextId,
        boolean includeAll
    ) {
        RefObject valueContainer = (RefObject)this.valueContainerMap.get(valueContainerName);
        if(valueContainer == null) {
            return null;
        }
        SortedMap longTexts = new TreeMap();
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
                Object text = texts.size() > locale
                ? texts.get(locale)
                    : texts.isEmpty() ? "" : texts.get(0);
                if(codeAsKey) {
                    longTexts.put(code, text);
                }
                else {
                    if(text == null) {
                        throw new RuntimeServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CONFIGURATION, 
                            "text of code value entry can not be null",
                            new BasicException.Parameter("container name", valueContainerName),
                            new BasicException.Parameter("locale", locale),
                            new BasicException.Parameter("lookup id", longTextId),
                            new BasicException.Parameter("entry", entry.refMofId()),                            
                            new BasicException.Parameter("texts", texts)
                        );
                    }
                    longTexts.put(text, code);
                }
            }
        }
        this.longTextMap.put(
            longTextId,
            longTexts
        );      
        return longTexts;
    }

    //-------------------------------------------------------------------------
    public SortedMap getLongText(
        String name,
        short locale,
        boolean codeAsKey,
        boolean includeAll
    ) {
        SortedMap longTexts = null;
        String longTextId = name + ":" + locale + ":" + codeAsKey + ":" + includeAll;
        if((longTexts = (SortedMap)this.longTextMap.get(longTextId)) == null) {
            longTexts = this.cacheLongText(
                name,
                locale,
                codeAsKey,
                longTextId,
                includeAll
            );
        }
        return longTexts;
    }

    //-------------------------------------------------------------------------
    public SortedMap getShortText(
        String name,
        short locale,
        boolean codeAsKey,
        boolean includeAll
    ) {
        SortedMap shortTexts = null;
        String shortTextId = name + ":" + locale + ":" + codeAsKey + ":" + includeAll;
        if((shortTexts = (SortedMap)this.shortTextMap.get(shortTextId)) == null) {
            shortTexts = this.cacheShortText(
                name,
                locale,
                codeAsKey,
                shortTextId,
                includeAll
            );
        }
        return shortTexts;      
    }

    //-------------------------------------------------------------------------
    public SortedMap getIconKeys(
        String name,
        boolean includeAll
    ) {
        SortedMap iconKeys = null;
        if((iconKeys = (SortedMap)this.iconKeyMap.get(name + ":" + includeAll)) == null) {
            RefObject valueContainer = (RefObject)this.valueContainerMap.get(name);
            if(valueContainer == null) {
                return null;
            }
            iconKeys = new TreeMap();
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
                        entry.refGetValue("iconKey")
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
        SortedMap colors = null;
        if((colors = (SortedMap)this.colorMap.get(name + ":" + includeAll)) == null) {
            RefObject valueContainer = (RefObject)this.valueContainerMap.get(name);
            if(valueContainer == null) {
                return null;
            }
            colors = new TreeMap();
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
                        entry.refGetValue("color")
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
        SortedMap backColors = null;
        if((backColors = (SortedMap)this.backColorMap.get(name + ":" + includeAll)) == null) {
            RefObject valueContainer = (RefObject)this.valueContainerMap.get(name);
            if(valueContainer == null) {
                return null;
            }
            backColors = new TreeMap();
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
                        entry.refGetValue("backColor")
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
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 8069002786499927870L;

    private final RefObject_1_0 codeSegment;
    private final Map valueContainerMap;
    private final Map shortTextMap;
    private final Map longTextMap;
    private final Map iconKeyMap;
    private final Map colorMap;
    private final Map backColorMap;
}

//--- End of File -----------------------------------------------------------
