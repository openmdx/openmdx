/*
 * ====================================================================
 * Project:     openMDX/Dalvik, http://www.openmdx.org/
 * Description: HTTP Message 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
package org.openmdx.dalvik.rest.http.stream;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;

import javax.resource.cci.InteractionSpec;

import org.openmdx.application.rest.http.HttpContext;
import org.openmdx.application.rest.http.HttpInteraction;
import org.openmdx.application.rest.http.spi.Message;
import org.openmdx.application.rest.http.spi.Target;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.RestSource;
import org.openmdx.dalvik.rest.stream.RestFormatter;
import org.openmdx.dalvik.rest.stream.RestTarget;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLStreamException;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLStreamWriter;
import org.openmdx.kernel.exception.BasicException;
import org.xml.sax.InputSource;

/**
 * Abstract Message
 */
class AlternateMessage implements Message, Closeable {

    /**
     * Constructor 
     * 
     * @param httpContext 
     * @param httpInteraction
     * @param interactionSpec
     * @param uri 
     * @param xri
     */
    AlternateMessage(
        HttpContext httpContext, 
        HttpInteraction httpInteraction,
        RestInteractionSpec interactionSpec, 
        String uri, 
        HttpURLConnection urlConnection
    ){
        this.httpContext = httpContext;
        this.httpInteraction = httpInteraction;
        this.urlConnection = urlConnection;
        this.urlConnection.setDoOutput(
            interactionSpec.getInteractionVerb() != InteractionSpec.SYNC_RECEIVE
        );
    }

    final HttpContext httpContext;
    private final HttpInteraction httpInteraction;
    
    /**
     * The lazily created URL connection
     */
    protected HttpURLConnection urlConnection;
    
    /**
     * The lazily created request body 
     */
    private RestTarget requestBody = null;
    
    /**
     * The lazily created response body 
     */
    private RestSource responseBody = null;

    /**
     * Create a request body
     * 
     * @return a new request body
     */
    protected RestTarget newRequestBody(
    ) throws IOException {
        return new RestTarget(
            httpContext.getConnectionURL()
        ){
           
            /* (non-Javadoc)
             * @see org.openmdx.application.rest.http.RestFormat.Target#newWriter()
             */
            @Override
            protected XMLStreamWriter newWriter(
            ) throws XMLStreamException {
                String contentType = httpContext.getContentType();
                setRequestField(
                    "Content-Type",
                    contentType
                );
                try {
                    return RestFormatter.getOutputFactory(httpContext.getMimeType()).createXMLStreamWriter(
                        urlConnection.getOutputStream()
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
    protected RestSource newResponseBody(
    ) throws IOException {
        return new RestSource(
            httpContext.getConnectionURL(),
            new InputSource(this.urlConnection.getInputStream()),
            httpContext.getMimeType(), 
            this
        );                    
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#execute()
     */
    @Override
    public int execute(
    ) throws ServiceException {
        if(this.requestBody != null) try {
            this.requestBody.close();
        } catch (XMLStreamException exception) {
            throw new ServiceException (
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PROCESSING_FAILURE,
                "Could not submit REST request"
            );
        }
        try {
            return httpInteraction.getStatus(this.urlConnection);
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
    @Override
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
    @Override
    public final RestSource getResponseBody(
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
    @Override
    public final String getResponseField(String key) {
        return this.urlConnection.getHeaderField(key);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.rest.http.AbstractHttpInteraction.Message#setRequestField(java.lang.String, java.lang.String)
     */
    @Override
    public final void setRequestField(String key, String value) {
        this.urlConnection.setRequestProperty(key, value);
    }

    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close(
    ) throws IOException {
        if(this.urlConnection != null) {
            this.urlConnection.disconnect();
        }
    }
    
}