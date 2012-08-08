/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: SimplePort.java,v 1.9 2010/03/19 12:32:55 hburger Exp $
 * Description: Simple Port 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/19 12:32:55 $
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
package org.openmdx.application.rest.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.net.CookieManager;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.RestFormat;
import org.openmdx.base.rest.spi.RestFormat.Source;
import org.openmdx.base.rest.spi.RestFormat.Target;
import org.openmdx.kernel.exception.BasicException;
import org.xml.sax.InputSource;

import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * The Simple Port depends on the JDK's URL functionality only
 */
public class SimplePort implements Port {

    /**
     * Constructor 
     */
    public SimplePort(
    ) {
    }

    /**
     * The REST server's URI
     */
    private String uri;

    /**
     * The BASIC authentication user name
     */
    private String userName = null;
    
    /**
     * The BASIC authentication user name    
     */
    private String password = null;
    
    /**
     * The BASIC authorization value
     */
    private String authorization = null;
    
    /**
     * The MIME type, one of<ul>
     * <li>text/xml (default)
     * <li>application/vnd.openmdx.wbxml
     * </ul>
     */
    private String mimeType = "text/xml"; 
    
    /**
     * Retrieve the MIME type.
     *
     * @return Returns the mimeType.
     */
    public String getMimeType() {
        return this.mimeType;
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
    }

    /**
     * Retrieve uri.
     *
     * @return Returns the uri.
     */
    public String getUri() {
        return this.uri;
    }
    
    /**
     * Set uri.
     * 
     * @param uri The uri to set.
     */
    public void setUri(
        String uri
    ) {
        this.uri =
            uri == null || uri.endsWith("/") ? uri : 
            uri + '/';
    }
    
    /**
     * Retrieve userName.
     *
     * @return Returns the userName.
     */
    public String getUserName() {
        return this.userName;
    }
    
    /**
     * Set userName.
     * 
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
        this.authorization = null; // setting the user name invalidates the authorization property
    }
    
    /**
     * Retrieve password.
     *
     * @return Returns the password.
     */
    public String getPassword() {
        throw new UnsupportedOperationException("The password is a write-only property");
    }
    
    /**
     * Set password.
     * 
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
        this.authorization = null; // setting the password invalidates the authorization property
    }

    /**
     * Retrieve the BASIC authorization value
     * 
     * @return the BASIC authorization value
     * 
     * @throws UnsupportedEncodingException
     */
    protected String getAuthorization() throws UnsupportedEncodingException{
        if(this.authorization == null && this.userName != null && !"".equals(this.userName)) {
            this.authorization = "BASIC " + Base64.encode(
                (getUserName() + ":" + (this.password == null ? "" : this.password)).getBytes("UTF-8")
            );        
        }
        return this.authorization;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new SimpleInteraction(connection, this.uri);
    }

    /**
     * Simple Interaction
     */
    class SimpleInteraction extends AbstractHttpInteraction {

        /**
         * Constructor 
         *
         * @param connection
         * @param uri
         * 
         * @throws ResourceException 
         */
        protected SimpleInteraction(
            Connection connection,
            String uri
        ) throws ResourceException {
            super(connection, uri);
            try {
                this.cookieURI = new URI(uri);
            } catch (URISyntaxException exception) {
                throw new ResourceException(exception);
            }
        }

        /**
         * The cookie URI
         */
        protected final URI cookieURI;
        
        /**
         * The cookie handler
         */
        protected final CookieHandler cookieHandler = new CookieManager();

        /* (non-Javadoc)
         * @see org.openmdx.application.rest.http.AbstractHttpInteraction#newMessage(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.naming.Path)
         */
        @Override
        protected Message newMessage(
            RestInteractionSpec ispec, 
            Path xri
        ) throws ServiceException {
            return new SimpleMessage(ispec, xri, null);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.resource.spi.AbstractInteraction#open()
         */
        @Override
        protected void open(
        ) throws ResourceException {
            try {
                new SimpleMessage(
                    CONNECT_SPEC, 
                    CONNECT_XRI, 
                    "UserName=" + getConnectionUserName()
                ).execute();
            } catch (ServiceException exception) {
                throw new ResourceException(exception);
            }
        }
    
        /**
         * Simple Message
         */
        class SimpleMessage implements Message {
    
            /**
             * Constructor 
             *
             * @param interactionSpec
             * @param xri
             */
            SimpleMessage(
                RestInteractionSpec interactionSpec, 
                Path xri,
                String query
            ) throws ServiceException {
                this.interactionSpec = interactionSpec;
                String servletPath = xri.toXRI();
                String url = uri + servletPath.substring(
                    servletPath.charAt(14) == '!' ? 14 : 15
                );
                try {
                    URLConnection urlConnection = new URL(
                        query == null ? url : url + '?' + query
                    ).openConnection();
                    if(urlConnection instanceof HttpURLConnection) {
                        this.urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    } else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "Unexpected URL connection",
                            new BasicException.Parameter("expected", HttpURLConnection.class.getName()),
                            new BasicException.Parameter("actual", urlConnection == null ? null : urlConnection.getClass().getName())
                        );
                    }
                } catch (MalformedURLException exception) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "Can't create an URL for the given URI/XRI pair",
                        new BasicException.Parameter("uri", uri),
                        new BasicException.Parameter("xri", servletPath),
                        new BasicException.Parameter("url", url)
                    );
                } catch (IOException exception) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "Can't open a connection for the given URL",
                        new BasicException.Parameter("uri", uri),
                        new BasicException.Parameter("xri", servletPath),
                        new BasicException.Parameter("url", url)
                    );
                }
                int interactionVerb = interactionSpec.getInteractionVerb();
                this.urlConnection.setDoOutput(interactionVerb != InteractionSpec.SYNC_RECEIVE && query == null);
                this.urlConnection.setDoInput(interactionVerb != InteractionSpec.SYNC_SEND);
            }
    
            private final RestInteractionSpec interactionSpec;
            
            private final HttpURLConnection urlConnection;
            
            private Target requestBody = null;
            
            private Source responseBody = null;
            
            protected OutputStream getOutputStream(
            ) throws IOException{
                return this.urlConnection.getOutputStream();
            }
            
            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#execute()
             */
            public int execute(
            ) throws ServiceException {
                try {
                    this.urlConnection.setRequestMethod(
                        this.interactionSpec.getFunctionName()
                    );
                    String authorization = getAuthorization();
                    if(authorization != null) {
                        setRequestField("Authorization", authorization);
                    }
                    SimpleInteraction.this.cookieHandler.get(
                        SimpleInteraction.this.cookieURI, 
                        this.urlConnection.getRequestProperties()
                    );
                    setRequestField(
                        "interaction-verb", 
                        Integer.toString(this.interactionSpec.getInteractionVerb())
                    );
                    if(this.requestBody != null) {
                        this.requestBody.close();
                    }
                    this.urlConnection.connect();
                    int status = this.urlConnection.getResponseCode();
                    SimpleInteraction.this.cookieHandler.put(
                        SimpleInteraction.this.cookieURI,
                        this.urlConnection.getHeaderFields()
                    );
                    return status;
                } catch (IOException exception) {
                    throw new ServiceException (
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "Could not process REST request"
                    );
                } catch (XMLStreamException exception) {
                    throw new ServiceException (
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "Could not process REST request"
                    );
                }
            }
    
            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#getRequestBody()
             */
            public Target getRequestBody(
            ) throws ServiceException {
                if(this.requestBody == null) {
                    this.requestBody = new Target(
                        getUri()
                    ){
                       
                        /* (non-Javadoc)
                         * @see org.openmdx.application.rest.http.RestFormat.Target#newWriter()
                         */
                        @Override
                        protected XMLStreamWriter newWriter(
                        ) throws XMLStreamException {
                            try {
                                return RestFormat.getOutputFactory(getMimeType()).createXMLStreamWriter(getOutputStream());
                            } catch (IOException exception) {
                                throw toXMLStreamException(exception);
                            }
                        }
                    };
                    
                }
                return this.requestBody;
            }
    
            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#getResponseBody()
             */
            public Source getResponseBody(
            ) throws ServiceException {
                if(this.responseBody == null) try {
                    this.responseBody = new Source(
                        getUri(),
                        new InputSource(this.urlConnection.getInputStream()),
                        getMimeType()
                    );
                } catch (IOException exception) {
                    throw new ServiceException(exception);
                }
                return this.responseBody;
            }
    
            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#getResponseField(java.lang.String)
             */
            public String getResponseField(String key) {
                return this.urlConnection.getHeaderField(key);
            }
    
            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#setRequestField(java.lang.String, java.lang.String)
             */
            public void setRequestField(String key, String value) {
                this.urlConnection.setRequestProperty(key, value);
            }
            
        }
        
    }

}
