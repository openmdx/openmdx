/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: SimplePort.java,v 1.22 2010/06/02 15:04:32 hburger Exp $
 * Description: Simple Port 
 * Revision:    $Revision: 1.22 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 15:04:32 $
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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

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
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.exception.BasicException;
import org.xml.sax.InputSource;

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
     * Use UTF-8 encoding
     */
    private final static String CHARACTER_ENCODING = "UTF-8";
    
    /**
     * Use "text/xml" as pretty printing default value.
     */
    private final static String DEFAULT_MIME_TYPE = "text/xml";
    
    /**
     * The REST server's URI
     */
    private String contextURL;

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
     * <li>application/xml
     * <li>application/vnd.openmdx.wbxml
     * </ul>
     */
    private String mimeType = DEFAULT_MIME_TYPE; 
    
    /**
     * The content type is evaluated lazily
     */
    private String contentType = null;
    
    /**
     * Retrieve the MIME type.
     *
     * @return Returns the mimeType.
     */
    public String getMimeType() {
        return this.mimeType;
    }
    
    /**
     * Retrieve the content type.
     *
     * @return Returns the content type.
     */
    protected String getContentType(){
        if(this.contentType == null) {
            this.contentType = this.mimeType + ";charset=" + CHARACTER_ENCODING; 
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
    public String getUri() {
        return this.contextURL;
    }
    
    /**
     * Set uri.
     * 
     * @param contextURL The uri to set.
     */
    public void setUri(
        String contextURL
    ) {
        this.contextURL =
            contextURL == null || !contextURL.endsWith("/") ? contextURL : 
            contextURL.substring(0, contextURL.length() - 1);
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
    protected String getAuthorization(
    ) throws UnsupportedEncodingException {
        if(this.authorization == null && this.userName != null && !"".equals(this.userName)) {
            this.authorization = "BASIC " + Base64.encode(
                (getUserName() + ":" + (this.password == null ? "" : this.password)).getBytes(CHARACTER_ENCODING)
            );        
        }
        return this.authorization;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
//  @Override
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new SimpleInteraction(connection, this.contextURL);
    }

    /**
     * Simple Interaction
     */
    class SimpleInteraction extends AbstractHttpInteraction {

        /**
         * Constructor 
         *
         * @param connection
         * @param contextURL
         * 
         * @throws ResourceException 
         */
        protected SimpleInteraction(
            Connection connection,
            String contextURL
        ) throws ResourceException {
            super(connection, contextURL);
            try {
                this.cookieURI = new URI(contextURL);
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
            switch(ispec.getFunction()){
                case GET: case DELETE: return new MessageWithoutBody(ispec, xri);
                default: return new MessageWithBody(ispec, xri, null);     
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.resource.spi.AbstractInteraction#open()
         */
        @Override
        protected void open(
        ) throws ResourceException {
            try {
                new MessageWithBody(
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
        private abstract class AbstractMessage implements Message, Closeable {
    
            /**
             * Constructor 
             *
             * @param interactionSpec
             * @param xri
             */
            AbstractMessage(
                RestInteractionSpec interactionSpec, 
                Path xri
            ) throws ServiceException {
                this.interactionSpec = interactionSpec;
                this.url = SimpleInteraction.this.toRequestURL(xri);
            }
    
            /**
             * The URL or URL prefix
             */
            protected final String url;
            
            /**
             * The REST interaction specification
             */
            protected final RestInteractionSpec interactionSpec;
            
            /**
             * The lazily created URL connection
             */
            protected HttpURLConnection urlConnection;
            
            /**
             * The lazily created request body 
             */
            private Target requestBody = null;
            
            /**
             * The lazily created response body 
             */
            private Source responseBody = null;

            /**
             * Provide the request body
             * 
             * @return the request body
             */
            protected abstract Target newRequestBody(
            ) throws IOException;
            
            /**
             * Create a response body
             * 
             * @return a new response body
             */
            protected Source newResponseBody(
            ) throws IOException {
                return new Source(
                    getUri(),
                    new InputSource(this.urlConnection.getInputStream()),
                    getMimeType(), 
                    this
                );                    
            }

            /**
             * Close the request body if it exists
             * 
             * @return <code>true</code> if there exists a request body
             *  
             * @throws ServiceException
             */
            protected boolean closeRequestBody(
            ) throws ServiceException {
                boolean exists = this.requestBody != null; 
                if(exists) try {
                    this.requestBody.close();
                } catch (XMLStreamException exception) {
                    throw new ServiceException (
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "Could not process REST request"
                    );
                }
                return exists;
            }
            
            /**
             * Return the query string
             * 
             * @return the URL encoded query string
             */
            protected String getQueryString(
            ) throws ServiceException {
                try {
                    return this.requestBody == null ? null : RestFormat.isBinary(getMimeType()) ?
                        // Do not URL encode Base64 encoded binary body
                        this.requestBody.toString() :
                            // URL encode if text
                            URLEncoder.encode(
                                this.requestBody.toString(),
                                CHARACTER_ENCODING
                            );
                } catch (Exception exception) {
                    throw new ServiceException(exception);
                }
            }
            
            /**
             * Retrieve the response code
             * 
             * @return the response code
             * 
             * @throws ServiceException
             */
            protected int getResponseCode(
            ) throws ServiceException{
                try {
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
                }
            }
            
            /**
             * Open a connection for the given URL
             * 
             * @param url
             * 
             * @throws ServiceException
             */
            protected void openConnection(
                String url
            ) throws ServiceException{
                try {
                    URLConnection urlConnection = new URL(url).openConnection();
                    if(urlConnection instanceof HttpURLConnection) {
                        this.urlConnection = (HttpURLConnection)urlConnection;
                    } else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "Unexpected URL connection",
                            new BasicException.Parameter("expected", HttpURLConnection.class.getName()),
                            new BasicException.Parameter("actual", urlConnection == null ? null : urlConnection.getClass().getName())
                        );
                    }
                    this.urlConnection.setRequestMethod(
                        this.interactionSpec.getFunctionName()
                    );
                    setRequestField(
                        "Accept",
                        getContentType()
                    );
                    String authorization = getAuthorization();
                    if(authorization != null) {
                        setRequestField("Authorization", authorization);
                    }
                    Map<String, List<String>> cookies = SimpleInteraction.this.cookieHandler.get(
                        SimpleInteraction.this.cookieURI, 
                        this.urlConnection.getRequestProperties()
                    );
                    for(Map.Entry<String, List<String>> entry : cookies.entrySet()) {
                        String key = entry.getKey();
                        if("Cookie2".equals(key)) {
                            StringBuilder values = new StringBuilder();
                            String separator = "";
                            for(String value : entry.getValue()) {
                                values.append(separator).append(value);
                                separator = "; ";
                            }
                            setRequestField(key, values.toString());
                        } else {
                            for(String value : entry.getValue()) {
                                setRequestField(key, value);
                            }
                        }
                    }
                    setRequestField(
                        "interaction-verb", 
                        Integer.toString(this.interactionSpec.getInteractionVerb())
                    );
                } catch (MalformedURLException exception) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "Can't create an URL for the given URI/XRI pair",
                        new BasicException.Parameter("contextURL", SimpleInteraction.this.contextURL),
                        new BasicException.Parameter("url", url)
                    );
                } catch (IOException exception) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "Can't open a connection for the given URL",
                        new BasicException.Parameter("contextURL", SimpleInteraction.this.contextURL),
                        new BasicException.Parameter("url", url)
                    );
                }
            }
            
            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#getRequestBody()
             */
        //  @Override
            public final Target getRequestBody(
            ) throws ServiceException {
                if(this.requestBody == null) try {
                    this.requestBody = newRequestBody();
                } catch (IOException exception) {
                    throw new ServiceException(exception);
                }
                return this.requestBody;
            }

            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#getResponseBody()
             */
        //  @Override
            public final Source getResponseBody(
            ) throws ServiceException {
                if(this.responseBody == null) try {
                    this.responseBody = newResponseBody();
                } catch (IOException exception) {
                    throw new ServiceException(exception);
                }
                return this.responseBody;
            }

            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#getResponseField(java.lang.String)
             */
        //  @Override
            public final String getResponseField(String key) {
                return this.urlConnection.getHeaderField(key);
            }
    
            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#setRequestField(java.lang.String, java.lang.String)
             */
        //  @Override
            public final void setRequestField(String key, String value) {
                this.urlConnection.setRequestProperty(key, value);
            }

            /* (non-Javadoc)
             * @see java.io.Closeable#close()
             */
        //  @Override
            public void close(
            ) throws IOException {
                if(this.urlConnection != null) {
                    this.urlConnection.disconnect();
                }
            }
            
        }
        
        /**
         * Message with body
         */
        class MessageWithBody extends AbstractMessage {
    
            /**
             * Constructor 
             *
             * @param interactionSpec
             * @param xri
             */
            MessageWithBody(
                RestInteractionSpec interactionSpec, 
                Path xri,
                String query
            ) throws ServiceException {
                super(interactionSpec, xri);
                openConnection(
                    query == null ? url : url + '?' + query
                );
                this.urlConnection.setDoOutput(
                    interactionSpec.getInteractionVerb() != InteractionSpec.SYNC_RECEIVE && 
                    query == null
                );
            }
    
            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#execute()
             */
        //  @Override
            public int execute(
            ) throws ServiceException {
                super.closeRequestBody();
                return super.getResponseCode();
            }
    
            /**
             * Create a request body
             * 
             * @return a new request body
             */
            @Override
            protected Target newRequestBody(
            ) throws IOException {
                return new Target(
                    getUri()
                ){
                   
                    /* (non-Javadoc)
                     * @see org.openmdx.application.rest.http.RestFormat.Target#newWriter()
                     */
                    @Override
                    protected XMLStreamWriter newWriter(
                    ) throws XMLStreamException {
                        String contentType = getContentType();
                        setRequestField(
                            "Content-Type",
                            contentType
                        );
                        try {
                            return RestFormat.getOutputFactory(
                                getMimeType()
                            ).createXMLStreamWriter(
                                MessageWithBody.this.urlConnection.getOutputStream()
                            );
                        } catch (IOException exception) {
                            throw toXMLStreamException(exception);
                        }
                    }
                    
                };
            }
    
        }

        /**
         * Message without body
         */
        class MessageWithoutBody extends AbstractMessage {
    
            /**
             * Constructor 
             *
             * @param interactionSpec
             * @param xri
             */
            MessageWithoutBody(
                RestInteractionSpec interactionSpec, 
                Path xri
            ) throws ServiceException {
                super(interactionSpec, xri);
            }
                
            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#execute()
             */
        //  @Override
            public int execute(
            ) throws ServiceException {
                super.openConnection(
                    super.closeRequestBody() ? this.url + '?' + super.getQueryString() : this.url
                );
                return super.getResponseCode();
            }
    
            /**
             * Create a request body
             * 
             * @return a new request body
             */
            @Override
            protected Target newRequestBody(
            ) throws IOException {
                final String mimeType = getMimeType();
                return RestFormat.isBinary(mimeType) ? new Target(
                    getUri()
                ){
                   
                    /**
                     * The body must be buffered as it will be used to build the URL's query string
                     */
                    private final BinaryStream target = new BinaryStream();
                    
                    /* (non-Javadoc)
                     * @see org.openmdx.application.rest.http.RestFormat.Target#newWriter()
                     */
                    @Override
                    protected XMLStreamWriter newWriter(
                    ) throws XMLStreamException {
                        return RestFormat.getOutputFactory(
                            mimeType
                        ).createXMLStreamWriter(
                            this.target
                        );
                    }

                    /* (non-Javadoc)
                     * @see java.lang.Object#toString()
                     */
                    @Override
                    public String toString() {
                        return this.target.toString();
                    }
                    
                } : new Target(
                    getUri()
                ){
                   
                    /**
                     * The body must be buffered as it will be used to build the URL's query string
                     */
                    private final StringWriter target = new StringWriter();
                    
                    /* (non-Javadoc)
                     * @see org.openmdx.application.rest.http.RestFormat.Target#newWriter()
                     */
                    @Override
                    protected XMLStreamWriter newWriter(
                    ) throws XMLStreamException {
                        return RestFormat.getOutputFactory(
                            mimeType
                        ).createXMLStreamWriter(
                            this.target
                        );
                    }

                    /* (non-Javadoc)
                     * @see java.lang.Object#toString()
                     */
                    @Override
                    public String toString() {
                        return this.target.toString();
                    }
                    
                };
            }
        
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class BinaryStream
    //------------------------------------------------------------------------
    
    /**
     * Binary Stream
     */
    static class BinaryStream extends ByteArrayOutputStream {

        /* (non-Javadoc)
         * @see java.io.ByteArrayOutputStream#toString()
         */
        @Override
        public synchronized String toString(
        ) {
            return Base64.encode(
                this.buf,
                0,
                this.count
            );
        }

    }
    
}
