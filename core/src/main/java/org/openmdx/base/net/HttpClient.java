/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: HTTP Client
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
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * HTTP Client
 * <p>
 * The HTTP client supports the use of different CookieHandlers in the same
 * virtual machine.
 */
public class HttpClient {

	/**
	 * Constructor
	 * 
	 * @param the cookie handler
	 */
	public HttpClient(
		CookieHandler cookieHandler
	){
		this.cookieHandler = cookieHandler == null ? new CookieManager() : cookieHandler;
	}

    /**
     * Constructor
     */
	public HttpClient(
    ){
		this.cookieHandler = null;
    }

    /**
     * The cookie handler, which may be <code>null</code>
     */
    private final CookieHandler cookieHandler;
	
    /**
     * Create a HTTP URL connection
     * 
     * @param url
     * @param secure <code>true</code> if a https connection is required
     * @param method
     * @param contentType the contentType, implying <code>doOutput(contentType!=null)</code>
     * 
     * @return the initialized connection
     * 
     * @throws IOException
     */
    public HttpURLConnection getConnection(
    	URL url,
    	boolean secure,
    	Method method,
    	String contentType
    ) throws IOException{
        if(secure && !"https".equalsIgnoreCase(url.getProtocol())) {
        	throw new IOException(
	            "The URL should use the https protocol"
	        );
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod(method.name());
        if(contentType != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", contentType);
        }
        if(this.cookieHandler != null) try {
        	Map<String,List<String>> headers = this.cookieHandler.get(connection.getURL().toURI(), connection.getRequestProperties());
        	for(Map.Entry<String, List<String>> entry : headers.entrySet()) {
        		StringBuilder values = new StringBuilder();
        		boolean empty = true;
        		for(String value : entry.getValue()) {
        			(empty ? values : values.append(", ")).append(value);
        			empty = false;
        		}
        		if(!empty){
	        		connection.setRequestProperty(entry.getKey(), values.toString());
        		}
        	}
        } catch (URISyntaxException exception) {
        	throw (IOException) new IOException(
        		"Cookie propagation failure"
        	).initCause(
        		exception
        	); 
        }
        return connection;
    }

    /**
     * Get the response code
     * 
     * @param connection
     * 
     * @throws IOException 
     */
    public int getResponseCode (
    	HttpURLConnection connection
    ) throws IOException{
    	int responseCode = connection.getResponseCode(); 
        if(this.cookieHandler != null) try {
        	this.cookieHandler.put(connection.getURL().toURI(), connection.getHeaderFields());
        } catch (URISyntaxException exception) {
        	throw (IOException) new IOException(
    			"Cookie retrieval failure"
        	).initCause(
        		exception
        	); 
        }
    	return responseCode;
    }

    
    //------------------------------------------------------------------------
    // EnumMethod
    //------------------------------------------------------------------------
    
    /**
     * The HTTP 1.1 methods according to RFC 2616 excluding CONNECT
     */
    public static enum Method {
    	HEAD,
    	GET,
    	POST,
    	PUT,
    	DELETE,
    	OPTIONS,
    	TRACE;
    }

}
