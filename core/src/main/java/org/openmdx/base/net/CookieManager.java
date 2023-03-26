/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Cookie Manager 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	 * The {@code Cookie} store
	 */
	private SortedSet<HttpCookie> cookies1 = new TreeSet<HttpCookie>();

    /**
     * The {@code Cookie2} store
     */
    private SortedSet<HttpCookie> cookies2 = new TreeSet<HttpCookie>();
	
    /**
     * Tells whether a {@code Cookie2: Version="1"} hint shall be sent to the server.
     */
    private final static boolean INCLUDE_COOKIE2_HINT = false;
    
	/* (non-Javadoc)
	 * @see java.net.CookieHandler#get(java.net.URI, java.util.Map)
	 */
	@Override
	public Map<String, List<String>> get(
		URI uri, 
		Map<String, List<String>> requestHeaders
	) throws IOException {
	    Map<String, List<String>> target = new HashMap<String, List<String>>();
        get(uri, "Cookie", this.cookies1, target);
        get(uri, "Cookie2", this.cookies2, target);
	    return Collections.unmodifiableMap(target);
	}

	/**
	 * Put the matching cookies inot the target map
	 * 
	 * @param uri
	 * @param name
	 * @param store
	 * @param target
	 */
	private static void get(
        URI uri, 
        String name,
        SortedSet<HttpCookie> store,
        Map<String, List<String>> target
	){
        List<String> cookies = new ArrayList<String>();
        for(
            Iterator<HttpCookie> i = store.iterator();
            i.hasNext();
        ){
            HttpCookie cookie = i.next();
            if (cookie.isExpired()) {
                i.remove();
            } else if(cookie.matches(uri)) {
                cookies.add(cookie.toString());
            }
        }
        if(
            "Cookie2".equals(name) &&
            (INCLUDE_COOKIE2_HINT || !cookies.isEmpty()) 
        ) {
            cookies.add(0, "$Version=\"1\"");
        }
	    if(!cookies.isEmpty()) {
	        target.put(name, cookies);
	    }
	}
	
	/* (non-Javadoc)
	 * @see java.net.CookieHandler#put(java.net.URI, java.util.Map)
	 */
	@Override
	public void put(
	    URI uri, Map<String, 
	    List<String>> responseHeaders
	) throws IOException {
	    Set<Map.Entry<String, List<String>>> entries = responseHeaders.entrySet();
	    put(uri, "Set-Cookie", this.cookies1, entries);
        put(uri, "Set-Cookie2", this.cookies2, entries);
	}

	/**
	 * Put {@code Cookie} and {@code Cookie2} entries into the store
	 * 
	 * @param uri
	 * @param name
	 * @param store
	 * @param source
	 */
	private static void put(
	    URI uri, 
	    String name,
	    SortedSet<HttpCookie> store,
	    Set<Map.Entry<String, List<String>>> source
	){
	    for(Map.Entry<String, List<String>> header : source) {
            if(name.equalsIgnoreCase(header.getKey())) {
                for(String text : header.getValue()) {
                    HttpCookie cookie = new HttpCookie(uri, text);
                    if(cookie.isDiscard()) {
                        store.remove(cookie);
                    } else {
                        if(store.contains(cookie)) {
                            store.remove(cookie);
                        }
                        store.add(cookie);
                    }
                }
                return;
            }
        }
	}

}
