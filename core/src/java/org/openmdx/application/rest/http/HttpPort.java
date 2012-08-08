/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: HttpPort.java,v 1.2 2010/11/19 09:59:43 hburger Exp $
 * Description: Simple Port 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/11/19 09:59:43 $
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

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.RestFormat;
import org.openmdx.base.rest.spi.RestFormat.Source;
import org.openmdx.base.rest.spi.RestFormat.Target;
import org.openmdx.kernel.exception.BasicException;
import org.xml.sax.InputSource;

/**
 * The abstract port depends on the JDK's URL functionality only
 */
public class HttpPort implements Port {

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
    private String contextURL;

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
    public String getConnectionURL() {
        return this.contextURL;
    }
    
    /**
     * Set uri.
     * 
     * @param contextURL The uri to set.
     */
    public void setConnectionURL(
        String contextURL
    ) {
        this.contextURL =
            contextURL == null || !contextURL.endsWith("/") ? contextURL : 
            contextURL.substring(0, contextURL.length() - 1);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
//  @Override
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new PlainVanillaInteraction(connection, this.contextURL);
    }

    protected String newQueryArgument(
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
    protected URL newURL(
        String path, 
        String query
    ) throws ServiceException{
        try {
            return new URL(
                query == null || "".equals(query) ? path : (path + '?' + query)
             );
        } catch (MalformedURLException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE,
                "Invalid URL",
                new BasicException.Parameter("path", path),
                new BasicException.Parameter("query", query)
            );
        } 
    }
    
    
    //------------------------------------------------------------------------
    // Class StandardInteraction
    //------------------------------------------------------------------------
    
    /**
     * The plain-vanilla interaction does nothing about authentication and cookies
     */
    protected class PlainVanillaInteraction extends AbstractHttpInteraction {

        /**
         * Constructor 
         *
         * @param connection
         * @param contextURL
         * 
         * @throws ResourceException 
         */
        protected PlainVanillaInteraction(
            Connection connection,
            String contextURL
        ) throws ResourceException {
            super(connection, contextURL);
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.rest.http.AbstractHttpInteraction#newMessage(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.naming.Path)
         */
        @Override
        protected Message newMessage(
            RestInteractionSpec ispec, 
            Path xri
        ) throws ServiceException {
            return new HttpMessage(
                ispec, 
                xri, 
                ispec == DELETE_SPEC ? "FunctionName=DELETE" : null
            );     
        }

        
        /* (non-Javadoc)
         * @see org.openmdx.base.resource.spi.AbstractInteraction#open()
         */
        @Override
        protected void open(
        ) throws ResourceException {
            String userName = getConnectionUserName();
            try {
                new HttpMessage(
                    CONNECT_SPEC, 
                    CONNECT_XRI,
                    userName == null ? null : newQueryArgument("UserName", userName)
                ).execute();
            } catch (ServiceException exception) {
                throw new ResourceException(exception);
            }
        }

        /**
         * Create connection for the given URL
         * 
         * @param uri
         * 
         * @throws ServiceException
         */
        protected HttpURLConnection newConnection(
            URL url,
            RestInteractionSpec interactionSpec
        ) throws ServiceException {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(
                    interactionSpec.getFunctionName()
                );
                urlConnection.setRequestProperty(
                    "Accept",
                    getContentType()
                );
                urlConnection.setRequestProperty(
                    "interaction-verb", 
                    Integer.toString(interactionSpec.getInteractionVerb())
                );
                return urlConnection;
            } catch (ClassCastException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Unexpected URL connection",
                    new BasicException.Parameter("url", url),
                    new BasicException.Parameter("expected", HttpURLConnection.class.getName())
                );
            } catch (IOException exception) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Can't open a connection for the given URL",
                    new BasicException.Parameter("url", url)
                );
            } 
        }

        
        //--------------------------------------------------------------------
        // Class AbstractMessage
        //--------------------------------------------------------------------
        
        /**
         * Abstract Message
         */
        private class HttpMessage implements Message, Closeable {
    
            /**
             * Constructor 
             *
             * @param interactionSpec
             * @param xri
             * @param uri 
             */
            protected HttpMessage(
                RestInteractionSpec interactionSpec, 
                Path xri,
                String query
            ) throws ServiceException {
                this.urlConnection = newConnection(
                    newURL(PlainVanillaInteraction.this.toRequestURL(xri), query),
                    interactionSpec
                );
                this.urlConnection.setDoOutput(
                    interactionSpec.getInteractionVerb() != InteractionSpec.SYNC_RECEIVE
                );
            }
    
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
             * Create a request body
             * 
             * @return a new request body
             */
            protected Target newRequestBody(
            ) throws IOException {
                return new Target(
                    getConnectionURL()
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
                                HttpMessage.this.urlConnection.getOutputStream()
                            );
                        } catch (IOException exception) {
                            throw toXMLStreamException(exception);
                        }
                    }
                    
                };
            }
            
            /**
             * Create a response body
             * 
             * @return a new response body
             */
            protected Source newResponseBody(
            ) throws IOException {
                return new Source(
                    getConnectionURL(),
                    new InputSource(this.urlConnection.getInputStream()),
                    getMimeType(), 
                    this
                );                    
            }

            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#execute()
             */
        //  @Override
            public int execute(
            ) throws ServiceException {
                if(this.requestBody != null) try {
                    this.requestBody.close();
                } catch (XMLStreamException exception) {
                    throw new ServiceException (
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "Could not process REST request"
                    );
                }
                return this.getResponseCode();
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
                    return this.urlConnection.getResponseCode();
                } catch (IOException exception) {
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

    }

}
