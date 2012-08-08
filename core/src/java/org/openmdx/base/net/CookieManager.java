/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: CookieManager.java,v 1.2 2010/03/14 17:19:07 hburger Exp $
 * Description: Cookie Manager 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/14 17:19:07 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.net;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Cookie Manager
 * <p>
 * This cookie manager takes the port into account for cookie matching.
 */
public class CookieManager extends CookieHandler {

	/**
	 * Constructor
	 */
	public CookieManager() {
		super();
	}

	/**
	 * The cookie store
	 */
	private SortedSet<HttpCookie> cookies = new TreeSet<HttpCookie>();
	
	/* (non-Javadoc)
	 * @see java.net.CookieHandler#get(java.net.URI, java.util.Map)
	 */
	@Override
	public Map<String, List<String>> get(
		URI uri, 
		Map<String, List<String>> requestHeaders
	) throws IOException {
		List<String> cookies = new ArrayList<String>();
		for(
		    Iterator<HttpCookie> i = this.cookies.iterator();
		    i.hasNext();
		){
		    HttpCookie cookie = i.next();
		    if (cookie.isExpired()) {
		        i.remove();
		    } else if(cookie.matches(uri)) {
                cookies.add(cookie.toString());
            }
		}
		return Collections.singletonMap("Cookie", cookies);
	}

	/* (non-Javadoc)
	 * @see java.net.CookieHandler#put(java.net.URI, java.util.Map)
	 */
	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
		List<String> cookies = null;
		for(Map.Entry<String, List<String>> header : responseHeaders.entrySet()) {
			if("Set-Cookie2".equalsIgnoreCase(header.getKey())) {
				cookies = header.getValue();
			}
		}
		if(cookies == null) {
			for(Map.Entry<String, List<String>> header : responseHeaders.entrySet()) {
				if("Set-Cookie".equalsIgnoreCase(header.getKey())) {
					cookies = header.getValue();
				}
			}
		}
		if(cookies != null) {
			for(String text : cookies) {
				HttpCookie cookie = new HttpCookie(uri, text);
				if(cookie.isDiscard()) {
					this.cookies.remove(cookie);
				} else {
					if(this.cookies.contains(cookie)) {
						this.cookies.remove(cookie);
					}
					this.cookies.add(cookie);
				}
			}
		}
	}

}
