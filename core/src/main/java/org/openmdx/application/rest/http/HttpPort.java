/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Simple Port 
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
package org.openmdx.application.rest.http;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Duration;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.spi.CommException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.kernel.exception.BasicException;

/**
 * The HTTP port depends on the JDK's URL functionality only
 */
public class HttpPort implements HttpContext, Port<RestConnection> {

    /**
     * Constructor 
     */
    public HttpPort(
    ) {
    }

    /**
     * Use UTF-8 encoding
     */
    protected final static String STANDARD_ENCODING = "UTF-8";
    
    /**
     * The REST server's URI
     */
    private String connectionURL;

    /**
     * The (optional) connect timeout
     */
    private Duration connectTimeout = null;
    
    /**
     * The (optional) read timeout
     */
    private Duration readTimeout = null;
    
    /**
     * The MIME type, one of<ul>
     * <li>text/xml
     * <li>application/xml
     * <li>application/vnd.openmdx.wbxml
     * </ul>
     */
    private String mimeType = getDefaultMimeType(); 
    
    /**
     * The content type is evaluated lazily
     */
    private String contentType = null;
    
    /**
     * Provide the optimal MIME type
     * 
     * @return the default MIME type
     */
    protected  String getDefaultMimeType(
    ){
        return "application/vnd.openmdx.wbxml";
    }
    
    /**
     * Retrieve the MIME type.
     *
     * @return Returns the mimeType.
     */
    @Override
    public String getMimeType() {
        return this.mimeType;
    }
    
    /**
     * Retrieve the content type.
     *
     * @return Returns the content type.
     */
    @Override
    public String getContentType(){
        if(this.contentType == null) {
            this.contentType = this.mimeType + ";charset=" + STANDARD_ENCODING; 
        }
        return this.contentType;
    }
    
    /**
     * Set MIME type, one of<ul>
     * <li>text/xml (default)
     * <li>application/vnd.openmdx.wbxml
     * </ul>
     * 
     * @param mimeType The MIME type to set.
     */
    public void setMimeType(
        String mimeType
    ) {
        this.mimeType = mimeType; 
        this.contentType = null;
    }

    /**
     * Retrieve uri.
     *
     * @return Returns the uri.
     */
    @Override
    public String getConnectionURL() {
        return this.connectionURL;
    }
    
    /**
     * Set uri.
     * 
     * @param connectionURL The uri to set.
     */
    public void setConnectionURL(
        String connectionURL
    ) {
        this.connectionURL =
            connectionURL == null || !connectionURL.endsWith("/") ? connectionURL : 
            connectionURL.substring(0, connectionURL.length() - 1);
    }
    
    /**
     * Retrieve connectTimeout.
     *
     * @return Returns the connectTimeout.
     */
    public String getConnectTimeout() {
        return this.connectTimeout == null ? null : this.connectTimeout.toString();
    }
    
    /**
     * Set connectTimeout.
     * 
     * @param connectTimeout The connectTimeout to set.
     */
    public void setConnectTimeout(String connectTimeout) {
        this.connectTimeout = connectTimeout == null ? null : Duration.parse(connectTimeout);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.rest.http.HttpContext#getConnectTimeout()
     */
    @Override
    public Duration getConnectionConnectTimeout() {
        return this.connectTimeout;
    }
    
    /**
     * Retrieve readTimeout.
     *
     * @return Returns the readTimeout.
     */
    public String getReadTimeout() {
        return this.readTimeout == null ? null : this.readTimeout.toString();
    }
    
    /**
     * Set readTimeout.
     * 
     * @param readTimeout The readTimeout to set.
     */
    public void setReadTimeout(String readTimeout) {
        this.readTimeout = readTimeout == null ? null : Duration.parse(readTimeout);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.rest.http.HttpContext#getReadTimeout()
     */
    @Override
    public Duration getConnectionReadTimeout() {
        return this.readTimeout;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(
    	RestConnection connection
    ) throws ResourceException {
        return new PlainVanillaInteraction(connection, this);
    }

    protected static String newQueryArgument(
        String name,
        String value
    )throws ResourceException {
        try {
            return name + "=" + URLEncoder.encode(value, STANDARD_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            throw new ResourceException(exception);
        }
    }

    /**
     * Create the URL
     * 
     * @param path
     * @param query
     * 
     * @return the message's URL 
     * @throws ServiceException 
     */
    @Override
    public URL newURL(
        String path, 
        String query
    ) throws ResourceException{
        try {
            return new URL(
                query == null || "".equals(query) ? path : (path + '?' + query)
             );
        } catch (MalformedURLException exception) {
        	throw ResourceExceptions.initHolder(
        		new CommException(
                    "Invalid URL",
                    BasicException.newEmbeddedExceptionStack(
		                exception,
		                BasicException.Code.DEFAULT_DOMAIN,
		                BasicException.Code.MEDIA_ACCESS_FAILURE,
		                new BasicException.Parameter(BasicException.Parameter.XRI, path),
		                new BasicException.Parameter("query", query)
		           )
		       )
            );
        } 
    }

}
