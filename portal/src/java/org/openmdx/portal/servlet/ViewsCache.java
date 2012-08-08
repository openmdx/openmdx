/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ViewsCache.java,v 1.8 2009/06/02 16:26:39 wfro Exp $
 * Description: ViewsCache 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/02 16:26:39 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.openmdx.portal.servlet.view.ObjectView;

/**
 * Holds the cached views stored per session. transient prevents serializing
 * of cached views.
 */
public class ViewsCache {

    //-----------------------------------------------------------------------
    public ViewsCache(
        Number viewsCacheSize
    ) {
        this.views = new LinkedHashMap(
            viewsCacheSize == null ? 
                DEFAULT_VIEWS_CACHE_SIZE : 
                viewsCacheSize.intValue(),
            0.75f,
            true
        );
    }

    //-----------------------------------------------------------------------
    public Map getViews(
    ) {
        return this.views;
    }

    //-----------------------------------------------------------------------
    public void clearViews(
        HttpSession session,
        long cachedSince          
    ) {
        synchronized(this.views) {
            this.views.clear();
            session.setAttribute(
                WebKeys.VIEW_CACHE_CACHED_SINCE, 
                new Long(cachedSince)
            );
        }
    }

    //-------------------------------------------------------------------------
    public void evictViews(
    ) {
    	synchronized(this.views) {
    		try {
	            for(Iterator<Entry<String,ObjectView>> i = this.views.entrySet().iterator(); i.hasNext(); ) {
	            	Entry<String,ObjectView> entry = i.next();
	                ObjectView view = entry.getValue();
	                view.getPersistenceManager().evictAll();
	            }
    		}
    		catch(Exception e) {} // ignore
    	}
    }
        
    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void removeDirtyViews(
    ) {
        synchronized(this.views) {
        	try {
	            for(Iterator<Entry<String,ObjectView>> i = this.views.entrySet().iterator(); i.hasNext(); ) {
	            	Entry<String,ObjectView> entry = i.next();
	                ObjectView view = entry.getValue();
	                if(view.getObjectReference().getObject() == null) {
	                    i.remove();
	                }
	            }
        	}
        	catch(Exception e) {} // ignore
        }
    }

    //-----------------------------------------------------------------------
    public ObjectView getView(
        String requestId
    ) {          
        return (ObjectView)this.views.get(requestId);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void addView(
        String requestId,
        ObjectView view
    ) {
        synchronized(this.views) {
            this.views.put(
                requestId, 
                view
            );
        }
    }

    //-----------------------------------------------------------------------
    public void removeView(
        String requestId
    ) {
        synchronized(this.views) {
            this.views.remove(requestId);
        }          
    }

    //-----------------------------------------------------------------------
    public boolean containsView(
        String requestId
    ) {
        return this.views.containsKey(requestId);
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final int DEFAULT_VIEWS_CACHE_SIZE = 5;
    private transient Map views = null;

}
