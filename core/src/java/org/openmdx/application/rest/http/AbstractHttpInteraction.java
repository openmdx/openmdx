/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract HTTP Interaction
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2013, OMEX AG, Switzerland
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

import java.net.HttpURLConnection;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.resource.spi.CommException;

import org.openmdx.application.rest.http.spi.Message;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Closeables;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.RestFormatter;
import org.openmdx.base.rest.spi.RestFormatters;
import org.openmdx.base.rest.spi.RestParser;
import org.openmdx.base.text.conversion.URITransformation;
import org.openmdx.kernel.exception.BasicException;
import org.xml.sax.SAXException;

/**
 * Abstract HTTP Interaction
 */
public abstract class AbstractHttpInteraction extends AbstractRestInteraction {

    /**
     * Constructor 
     *
     * @param connection
     * @param contextURL
     */
    protected AbstractHttpInteraction(
        RestConnection connection,
        String contextURL
    ) {
        super(connection);
        this.contextURL = contextURL.endsWith("/") ? contextURL.substring(0, contextURL.length() - 1) : contextURL;
    }

    /**
     * The HTTP interaction's context URL
     */
    protected final String contextURL;
    
    /**
     * The eagerly acquired REST formatter instance
     */
    protected static final RestFormatter restFormatter = RestFormatters.getFormatter();

    /**
     * The interaction spec to create (virtual) connection objects
     */
    protected final static RestInteractionSpec CONNECT_SPEC = new RestInteractionSpec(
        RestFunction.POST,
        InteractionSpec.SYNC_SEND
    );
    
    /**
     * The interaction spec used to post queries
     */
    protected final static RestInteractionSpec QUERY_SPEC = new RestInteractionSpec(
        RestFunction.POST,
        InteractionSpec.SYNC_SEND_RECEIVE
    );
    
    /**
     * The interaction spec to remove (virtual) connection objects
     */
    protected final static RestInteractionSpec DELETE_SPEC = new RestInteractionSpec(
        RestFunction.PUT,
        InteractionSpec.SYNC_SEND
    );

    /**
     * The path to create (virtual) connection objects
     */
    protected static final Path CONNECT_XRI = new Path("xri://@openmdx*org.openmdx.kernel/connection");
    
    /**
     * Retrieve the connection's user name
     * 
     * @return the connection's user name
     * 
     * @exception ResourceException
     */
    protected String getConnectionUserName(
    ) throws ResourceException{
        return getConnection().getMetaData().getUserName();
    }

    /**
     * Create a handle
     * 
     * @param ispec
     * @param xri
     * 
     * @return a new handle
     * 
     * @throws ServiceException
     */
    protected abstract Message newMessage (
        RestInteractionSpec ispec,
        Path xri
    ) throws ResourceException;
        
    /**
     * Provide the XRI based URL
     * 
     * @param resourceIdentifier
     * 
     * @return the XRI based URL
     */
    protected String toRequestURL(
        Path resourceIdentifier
    ) throws ResourceException {
        String xri = resourceIdentifier.toXRI();
        return this.contextURL + '/' + URITransformation.encode(
            xri.substring(xri.charAt(14) == '*' ? 15 : 14)
        );
    }

    /**
     * Process an object request
     * 
     * @param interactionSpec
     * @param xri 
     * @param input
     * @param output
     * @return <code>true</code> if the message was processed successfully
     * 
     * @throws ServiceException
     */
    private boolean process(
        RestInteractionSpec interactionSpec,
        Path xri,
        ObjectRecord input,
        IndexedRecord output
	) throws ResourceException {
        Message message = newMessage(interactionSpec, xri);
        restFormatter.format(message.getRequestBody(), input);
        return process(message, output);
    }

    /**
     * Process an object request
     * 
     * @param interactionSpec
     * @param input
     * @param output
     * 
     * @return <code>true</code> if the message was processed successfully
     * 
     * @throws ServiceException
     */
    private boolean process(
        RestInteractionSpec interactionSpec,
        QueryRecord input,
        IndexedRecord output
    ) throws ResourceException {
        Message message = newMessage(interactionSpec, input.getResourceIdentifier());
        restFormatter.format(message.getRequestBody(), input);
        return process(message, output);
    }
    
    /**
     * Process a request
     * 
     * @param message
     * @param output
     * 
     * @return <code>true</code> if the message was processed successfully
     * 
     * @throws ServiceException
     */
    private boolean process(
        Message message,
        Record output
    ) throws ResourceException {
        int status = message.execute();
        if(status == HttpURLConnection.HTTP_OK){
            try {
				RestParser.parseResponse(
				    output,
				    message.getResponseBody()
				);
			} catch (SAXException exception) {
				throw ResourceExceptions.initHolder(
					new CommException(
						"Unable to parse the response",
						BasicException.newEmbeddedExceptionStack(exception)
					)
				);
			}
        } else if (status >= 400) {
        	throw getResourceException(status, message);
        } else {
            Closeables.close(message.getResponseBody());
        }
        return status >= 200 && status < 300;
    }

	private ResourceException getResourceException(
		int status, 
		Message message
	){
		try {
			return ResourceExceptions.toResourceException(
				RestParser.parseException(message.getResponseBody())
			);
		} catch (SAXException exception) {
		    return ResourceExceptions.initHolder(
	    		new ResourceException(
    				"HTTP REST request failed",
    				BasicException.newEmbeddedExceptionStack(
						BasicException.toExceptionStack(exception),
						BasicException.Code.DEFAULT_DOMAIN,
						toExceptionCode(status),
						new BasicException.Parameter("status", status)
					)
				)
    		);
		} catch (ResourceException exception) {
		    return ResourceExceptions.initHolder(
	    		new ResourceException(
    				"HTTP REST request failed as well as retrieving the remote exception",
    				BasicException.newEmbeddedExceptionStack(
						BasicException.toExceptionStack(exception),
						BasicException.Code.DEFAULT_DOMAIN,
						toExceptionCode(status),
						new BasicException.Parameter("status", status)
					)
				)
    		);
		}
	}
    
    /**
     * Map an openMDX exception code to a HTTP status code
     * 
     * @param exceptionCode an openMDX exception code
     * 
     * @return the corresponding HTTP status code
     */
    private static int toExceptionCode(
        int httpStatus
    ){
        return 
            httpStatus == HttpURLConnection.HTTP_NOT_FOUND ? BasicException.Code.NOT_FOUND :
            httpStatus == HttpURLConnection.HTTP_FORBIDDEN ? BasicException.Code.AUTHORIZATION_FAILURE :
            httpStatus == HttpURLConnection.HTTP_PRECON_FAILED ? BasicException.Code.CONCURRENT_ACCESS_FAILURE :
            httpStatus == HttpURLConnection.HTTP_CONFLICT ? BasicException.Code.ILLEGAL_STATE :
            httpStatus == HttpURLConnection.HTTP_NOT_IMPLEMENTED ? BasicException.Code.NOT_SUPPORTED :    
            httpStatus == HttpURLConnection.HTTP_BAD_REQUEST ? BasicException.Code.GENERIC :    
            BasicException.Code.SYSTEM_EXCEPTION;    
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#create(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
     */
    @Override
    public boolean create(
        RestInteractionSpec interactionSpec,
        ObjectRecord input,
        ResultRecord output
    ) throws ResourceException {
        return process(interactionSpec, input.getResourceIdentifier(), input, output);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
     */
    @Override
    public boolean delete(
        RestInteractionSpec interactionSpec,
        ObjectRecord input
    ) throws ResourceException {
        return process(
            RestFunction.DELETE == interactionSpec.getFunction() ? DELETE_SPEC : interactionSpec, 
            input.getResourceIdentifier(), 
            input, 
            null
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
     */
    @Override
    public boolean delete(
        RestInteractionSpec interactionSpec,
        QueryRecord input
    ) throws ResourceException {
        return process(
            RestFunction.DELETE == interactionSpec.getFunction() ? DELETE_SPEC : interactionSpec, 
            input, 
            null
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#find(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
     */
    @Override
    public boolean find(
        RestInteractionSpec interactionSpec,
        QueryRecord input,
        ResultRecord output
    ) throws ResourceException {
        return process(
            RestFunction.GET == interactionSpec.getFunction() ? QUERY_SPEC : interactionSpec, 
            input, 
            output
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
     */
    @Override
    public boolean get(
        RestInteractionSpec interactionSpec,
        QueryRecord input,
        ResultRecord output
    ) throws ResourceException {
        return process(
            RestFunction.GET == interactionSpec.getFunction() ? QUERY_SPEC : interactionSpec, 
            input, 
            output
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#invoke(org.openmdx.base.resource.spi.RestInteractionSpec, javax.resource.cci.MessageRecord, javax.resource.cci.MessageRecord)
     */
    @Override
    public boolean invoke(
        RestInteractionSpec interactionSpec,
        MessageRecord input,
        MessageRecord output
    ) throws ResourceException {
        Message message = newMessage(interactionSpec, input.getResourceIdentifier());
        restFormatter.format(message.getRequestBody(), "in", input);
        return process(message, output);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#move(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.naming.Path, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
     */
    @Override
    public boolean move(
        RestInteractionSpec interactionSpec,
        ObjectRecord input,
        ResultRecord output
    ) throws ResourceException {
        return process(interactionSpec, new Path(input.getTransientObjectId()), input, output);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#put(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
     */
    @Override
    public boolean update(
        RestInteractionSpec interactionSpec,
        ObjectRecord input,
        ResultRecord output
    ) throws ResourceException {
        return process(interactionSpec, input.getResourceIdentifier(), input, output);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#validate(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
     */
    @Override
    public boolean verify(
        RestInteractionSpec interactionSpec,
        ObjectRecord input
    ) throws ResourceException {
        return process(interactionSpec, input.getResourceIdentifier(), input, null);
    }

}
