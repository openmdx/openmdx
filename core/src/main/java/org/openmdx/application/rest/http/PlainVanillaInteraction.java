/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Plain Vanilla Interaction 
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

#if JAVA_8
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.CommException;
#else
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.CommException;
#endif

import org.openmdx.application.rest.http.spi.Message;
import org.openmdx.application.rest.http.spi.MessageFactory;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;

/**
 * The plain-vanilla interaction does nothing about authentication and cookies
 */
class PlainVanillaInteraction extends AbstractHttpInteraction implements HttpInteraction {

    /**
     * Constructor 
     *
     * @param connection
     * @param contextURL
     * 
     * @throws ResourceException 
     */
    protected PlainVanillaInteraction(
        RestConnection connection,
        HttpContext httpContext
    ) throws ResourceException {
        super(connection, httpContext.getConnectionURL());
        this.httpContext = httpContext;
    }

    private final HttpContext httpContext;
    
    private final static MessageFactory messageFactory = Classes.newPlatformInstance(
        "org.openmdx.application.rest.http.stream.StandardMessageFactory",
        MessageFactory.class
    );
    
    /**
     * Retrieve the response code
     * 
     * @param urlConnection the URL connection
     * 
     * @return the response code
     * 
     * @throws ServiceException
     */
    public int getStatus(
        HttpURLConnection urlConnection
    ) throws IOException{
        return urlConnection.getResponseCode();
    }

    Message newHttpMessage(
        HttpContext httpContext, 
        RestInteractionSpec interactionSpec,
        Path xri, 
        String query
    ) throws ResourceException{
        String uri = toRequestURL(xri);
        return messageFactory.newMessage(
            httpContext, 
            this, 
            interactionSpec, 
            uri, 
            newConnection(
                httpContext.newURL(uri, query),
                interactionSpec
            )
        );     
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.rest.http.AbstractHttpInteraction#newMessage(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.naming.Path)
     */
    @Override
    protected Message newMessage(
        RestInteractionSpec ispec, 
        Path xri
    ) throws ResourceException{
        return newHttpMessage(
            httpContext, 
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
        newHttpMessage(
		    httpContext, 
		    CONNECT_SPEC,
		    CONNECT_XRI, 
		    toQuery(userName)
		).execute();
    }

    /**
     * Create the query part
     * 
     * @param userName
     * 
     * @return the query part
     * 
     * @throws ResourceException
     */
    private String toQuery(
        String userName
    ) throws ResourceException {
        StringBuilder query = new StringBuilder(
            CallbackPrompts.BULK_LOAD + '=' + Boolean.FALSE
        );
        if(userName != null) {
            query.append('&').append(HttpPort.newQueryArgument("UserName", userName));
        }
        return query.toString();
    }
    
    /**
     * Create connection for the given URL
     */
    protected HttpURLConnection newConnection(
        final URL url,
        final RestInteractionSpec interactionSpec
    ) throws ResourceException{
        try {
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            final Duration connectTimeout = httpContext.getConnectionConnectTimeout();
            if(connectTimeout != null) {
                urlConnection.setConnectTimeout((int) connectTimeout.toMillis());
            }
            final Duration readTimeout = httpContext.getConnectionReadTimeout();
            if(readTimeout != null) {
                urlConnection.setReadTimeout((int) readTimeout.toMillis());
            }
            urlConnection.setRequestMethod(
                interactionSpec.getFunctionName()
            );
            urlConnection.setRequestProperty(
                "Accept",
                this.httpContext.getContentType()
            );
            urlConnection.setRequestProperty(
                "interaction-verb", 
                Integer.toString(interactionSpec.getInteractionVerb())
            );
            return urlConnection;
        } catch (ClassCastException exception) {
            throw ResourceExceptions.initHolder(
        		new NotSupportedException(
	        		"Unexpected URL connection",
	                BasicException.newEmbeddedExceptionStack(
	            		exception,
	                    BasicException.Code.DEFAULT_DOMAIN,
	                    BasicException.Code.NOT_SUPPORTED,
	                    new BasicException.Parameter("url", url),
	                    new BasicException.Parameter("expected", HttpURLConnection.class.getName()),
	                    new BasicException.Parameter("function", interactionSpec.getFunctionName())
	                )
	            )
        	);
        } catch (IOException exception) {
            throw ResourceExceptions.initHolder(
            	new CommException(
		            "Can't open a connection for the given URL",
		            BasicException.newEmbeddedExceptionStack(
		        		exception,
		                BasicException.Code.DEFAULT_DOMAIN,
		                BasicException.Code.BAD_PARAMETER,
		                new BasicException.Parameter("url", url),
		                new BasicException.Parameter("function", interactionSpec.getFunctionName())
		            )
		        )
            );
        } 
    }

    
    //--------------------------------------------------------------------
    // Class AbstractMessage
    //--------------------------------------------------------------------


}