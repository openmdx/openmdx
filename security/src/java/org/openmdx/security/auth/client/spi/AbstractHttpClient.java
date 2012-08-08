/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: AbstractHttpClient.java,v 1.2 2008/02/18 14:14:16 hburger Exp $
 * Description: Abstract HTTP Client
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/18 14:14:16 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.security.auth.client.spi;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Abstract HTTP Client
 */
public abstract class AbstractHttpClient {

	/**
	 * Constructor
	 * @param the authentication action
	 * 
	 * @throws MalformedURLException
	 */
	protected AbstractHttpClient(
	){
	}

    /**
     * Retrieve the authentication URL
     * 
     * @return the authentication URL
     */
    protected abstract URL getAuthenticationURL(
    );
    
    /**
     * Create a HTTP URL connection
     * 
     * @param secure <code>true</code> if a https connection is required
     * 
     * @return a HTTP connection
     * 
     * @throws IOException
     */
    private HttpURLConnection getConnection(
    	boolean secure
    ) throws IOException{
        URL url = getAuthenticationURL();
        if(
    		secure &&
    		!"https".equalsIgnoreCase(url.getProtocol())
    	) throw new IOException(
            "The authentication URL should use the https protocol"
        );
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        return connection;
    }

    /**
     * Create a POST connection
     * 
     * @param secure <code>true</code> if a https connection is required
     * 
     * @return a POST connection
     * 
     * @throws IOException
     */
    protected HttpURLConnection getPostConnection(
    	boolean secure
    ) throws IOException{
        HttpURLConnection connection = getConnection(secure);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        return connection;
    }

    /**
     * Create a POST connection
     * 
     * @param secure <code>true</code> if a https connection is required
     * 
     * @return a POST connection
     * 
     * @throws IOException
     */
    protected HttpURLConnection getHeadConnection(
    	boolean secure
    ) throws IOException{
        HttpURLConnection connection = getConnection(secure);
        connection.setRequestMethod("HEAD");
        return connection;
    }

    /**
     * Disconnects the connection
     * 
     * @param connection the connection to be disconnected;
     * may be <code>null</code>.
     */
    protected static void close(
    	HttpURLConnection connection
    ){
    	if(connection != null) {
    		connection.disconnect();
    	}
    }

    /**
     * Close a <code>Closeable</code>
     * 
     * @param closeable the object to be closed;
     * may be <code>null</code>.
     */
    protected static void close(
    	Closeable closeable
    ){
    	if(closeable != null) try {
			closeable.close();
		} catch (IOException ignored) {
		}
    }

}
