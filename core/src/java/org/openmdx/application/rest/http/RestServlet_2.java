/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RestServlet_2.java,v 1.58 2010/04/28 12:49:02 hburger Exp $
 * Description: REST Servlet 
 * Revision:    $Revision: 1.58 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/28 12:49:02 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2010, OMEX AG, Switzerland
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

import static org.openmdx.base.accessor.rest.spi.ControlObjects_2.isConnectionObjectIdentifier;
import static org.openmdx.base.accessor.rest.spi.ControlObjects_2.isControlObjectType;
import static org.openmdx.base.accessor.rest.spi.ControlObjects_2.isTransactionCommitIdentifier;
import static org.openmdx.base.accessor.rest.spi.ControlObjects_2.isTransactionObjectIdentifier;

import java.io.IOException;
import java.util.ListIterator;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.application.rest.spi.InboundConnectionFactory_2;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.HttpHeaderFieldContent;
import org.openmdx.base.io.HttpHeaderFieldValue;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.rest.spi.RestFormat;
import org.openmdx.base.xml.stream.XMLOutputFactories;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;

/**
 * REST Servlet
 */
public class RestServlet_2 extends HttpServlet {

    private static final long serialVersionUID = -4403464830407956377L;
    private final static int DEFAULT_POSITION = 0;
    private final static int DEFAULT_SIZE = 25;
    protected ConnectionFactory connectionFactory = null;

    /**
     * Tells whether the session is in auto-commit mode
     * 
     * @param request
     * 
     * @return <code>true</code> if the session is in auto-commit mode
     */
    protected boolean isAutoCommitting(
        HttpServletRequest request
    ){
        return Boolean.TRUE.equals(
            request.getSession(true).getAttribute("org.openmdx.rest.AutoCommit")
        );
    }

    /**
     * Tells whether the values shall be retained
     * 
     * @param request
     * 
     * @return <code>true</code> if the values shall be retained
     */
    protected boolean isRetainValues(
        HttpServletRequest request
    ){
        String interactionVerb = request.getHeader("interaction-verb");
        return interactionVerb == null || Integer.parseInt(interactionVerb) != InteractionSpec.SYNC_SEND;
    }
    
    /**
     * Retrieve the connection associated with the current session
     * 
     * @param request
     * 
     * @return the connection associated with the current session
     * 
     * @throws ServletException
     */
    protected Connection getConnection(
        HttpServletRequest request
    ) throws ServletException {
        try {
            HttpSession session = request.getSession(true);
            Connection connection = (Connection)session.getAttribute(Connection.class.getName());
            if(connection == null) {
                session.setAttribute(
                    "org.openmdx.rest.AutoCommit",
                    Boolean.TRUE
                );
                session.setAttribute(
                    Connection.class.getName(), 
                    connection = this.connectionFactory.getConnection(
                        new RestConnectionSpec(
                            request.getRemoteUser(), 
                            session.getId()
                        )
                    )
                );
            }
            return connection;
        } catch (ResourceException exception) {
            throw Throwables.log(
                new ServletException(
                    "Connection could not be established",
                    exception
                )
            );
        }
    }

    /**
     * Retrieve an interaction associated with the current connection
     * 
     * @param req
     * 
     * @return an interaction associated with the current connection
     * 
     * @throws ServletException
     */
    protected Interaction getInteraction(
        HttpServletRequest req
    ) throws ServletException {
        Connection connection = getConnection(req);
        try {
            return connection.createInteraction();
        } catch (ResourceException exception) {
            throw Throwables.log(
                new ServletException(
                    "Interaction could not be created",
                    exception
                )
            );
        }
    }

    /**
     * Extract the XRI from the URL
     * 
     * @param request
     * 
     * @return the requets's XRI
     */
    private Path getXri(
        HttpServletRequest request 
    ) {
        String uri = request.getServletPath().substring(1);
        return new Path(
            uri.startsWith("@openmdx") ? uri : 
            uri.startsWith("!") ? "xri://@openmdx" + uri :
            "xri://@openmdx*" + uri
        );
    }

    /**
     * Map an openMDX exception code to a HTTP status code
     * 
     * @param exceptionCode an openMDX exception code
     * 
     * @return the corresponding HTTP status code
     */
    private static int toStatusCode(
        int exceptionCode
    ){
        return
            exceptionCode == BasicException.Code.NOT_FOUND ? HttpServletResponse.SC_NOT_FOUND :
            exceptionCode == BasicException.Code.AUTHORIZATION_FAILURE ? HttpServletResponse.SC_FORBIDDEN :
            exceptionCode == BasicException.Code.CONCURRENT_ACCESS_FAILURE ? HttpServletResponse.SC_PRECONDITION_FAILED :
            exceptionCode == BasicException.Code.ILLEGAL_STATE ? HttpServletResponse.SC_CONFLICT :
            exceptionCode == BasicException.Code.NOT_IMPLEMENTED ? HttpServletResponse.SC_NOT_IMPLEMENTED :
            HttpServletResponse.SC_BAD_REQUEST;
    }
    
    /**
     * Exception handler
     * 
     * @param request 
     * @param resp
     * @param exception
     */
    protected void handleException(
        HttpServletRequest request,
        HttpServletResponse response, 
        ResourceException exception
    ) {
        BasicException exceptionStack = BasicException.toExceptionStack(exception);
        while(
            exceptionStack.getExceptionCode() == BasicException.Code.GENERIC && 
            exceptionStack.getCause() != null
        ) {
            exceptionStack = exceptionStack.getCause();
        }
        SysLog.detail("Resource Exception", exceptionStack);
        response.setStatus(toStatusCode(exceptionStack.getExceptionCode()));
        RestFormat.format(
            new ServletTarget(request, response), 
            exceptionStack
        );
    }

    /**
     * Exception handler
     * 
     * @param request 
     * @param response
     * @param exception
     */
    protected void handleException(
        HttpServletRequest request, 
        HttpServletResponse response, 
        Exception exception
    ) {
        BasicException exceptionStack = BasicException.toExceptionStack(exception);
        SysLog.error("Internal Server Error", exceptionStack);
        request.getSession().removeAttribute(Connection.class.getName());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        RestFormat.format(
            new ServletTarget(request, response), 
            exceptionStack
        );
    }
    
    /**
     * Servlet initialization
     * 
     * @param config servlet configuration
     * 
     * @exception ServletExcetpion
     */
    @Override
    public void init(
        ServletConfig config
    ) throws ServletException {
        super.init(config);
        try {
            String entityManagerFactoryName = config.getInitParameter("entity-manager-factory-name");
            this.connectionFactory = InboundConnectionFactory_2.newInstance(
                entityManagerFactoryName == null ? "EntityManagerFactory" : entityManagerFactoryName
            );
        } catch(Exception exception) {
            throw Throwables.log(
                new ServletException(exception)
            );            
        }
    }

    /**
     * Re-fetch objects afert auto-commit
     * 
     * @param interaction
     * @param reply the list to be updated
     * 
     * @throws ResourceException
     */
    @SuppressWarnings(
        {"unchecked", "cast"}
    )
    private void reFetch(
        Interaction interaction,
        IndexedRecord reply
    ) throws ResourceException {
        for(
            ListIterator i = ((IndexedRecord)reply).listIterator();
            i.hasNext();
        ){
            Object oldObject = i.next();
            if(oldObject instanceof Record) {
                Record oldRecord = (Record) oldObject;
                if(Object_2Facade.isDelegate(oldRecord)) {
                    Path xri = Object_2Facade.getPath((MappedRecord) oldRecord);
                    RecordFactory factory = this.connectionFactory.getRecordFactory();
                    IndexedRecord newRequest;
                    if(factory instanceof ExtendedRecordFactory) {
                        newRequest = ((ExtendedRecordFactory)factory).singletonIndexedRecord(
                            Multiplicities.LIST,
                            null,
                            xri
                        );
                    } else {
                        newRequest = factory.createIndexedRecord(
                            Multiplicities.LIST
                        );
                        newRequest.add(xri);
                    }
                    Record newReply = interaction.execute(
                        InteractionSpecs.getRestInteractionSpecs(true).GET,
                        newRequest
                    );
                    if(newReply instanceof IndexedRecord) {
                        IndexedRecord newResultRecord = (IndexedRecord) newReply;
                        if(!newResultRecord.isEmpty()) {
                            i.set(newResultRecord.get(0));
                        }
                    }
                }
            }
        }
        
    }
    
    /**
     * Execution
     * 
     * @param interaction
     * @param autoCommit
     * @param reFetchAfterAutoCommit
     * @param interactionSpec
     * @param input
     * 
     * @return the reply
     * 
     * @throws ResourceException
     */
    protected Record execute(
        Interaction interaction,
        boolean autoCommit,
        boolean reFetchAfterAutoCommit,
        RestInteractionSpec interactionSpec, 
        Record input
    ) throws ResourceException {
        if(autoCommit) {
            LocalTransaction transaction = interaction.getConnection().getLocalTransaction(); 
            transaction.begin();
            try {
                Record reply = interaction.execute(
                    interactionSpec, 
                    input
                );                
                transaction.commit();
                if(reFetchAfterAutoCommit && reply instanceof IndexedRecord) {
                    reFetch(interaction, (IndexedRecord)reply);
                }
                return reply; 
            } catch (ResourceException exception) {
                transaction.rollback();
                throw exception;
            }
        } else {
            return interaction.execute(
                interactionSpec, 
                input
            );                
        }
    }
    
    /**
     * REST DELETE Request
     * 
     * @param request the HTTP Request
     * @param response the HTTP Response
     *  
     * @exception ServletException
     * @exception IOException
     */
    @Override
    protected void doDelete(
        HttpServletRequest request, 
        HttpServletResponse response
    ) throws ServletException, IOException {
        prepare(request, response);
        Interaction interaction = getInteraction(request);
        try {
            Path xri = this.getXri(request);
            if(request.getContentType() == null) {
                execute(  
                    interaction,
                    !isTransactionObjectIdentifier(xri) && isAutoCommitting(request), 
                    false, 
                    InteractionSpecs.getRestInteractionSpecs(false).DELETE, 
                    Query_2Facade.newInstance(xri).getDelegate()
                );
            } else {
                MappedRecord object = RestFormat.parseRequest(
                    RestFormat.asSource(request),
                    xri
                );
                execute(  
                    interaction,
                    !isControlObjectType(object.getRecordName()) && isAutoCommitting(request), 
                    false, 
                    InteractionSpecs.getRestInteractionSpecs(false).DELETE, 
                    object
                );
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);                    
        } catch(ResourceException exception) {
            this.handleException(
                request,
                response, 
                exception
            );
        } catch(Exception exception) {
            this.handleException(
                request, 
                response, 
                exception
            );
        } finally {
            try {
                interaction.close();
            } catch (ResourceException exception) {
                Throwables.log(exception);
            }
        }
    }

    /**
     * HTTP GET Request
     * 
     * @param request the HTTP Request
     * @param response the HTTP Response
     *  
     * @exception ServletException
     * @exception IOException
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(
        HttpServletRequest request, 
        HttpServletResponse response
    ) throws ServletException, IOException {
        prepare(request, response);
        Interaction interaction = getInteraction(request);
        try {
            Path xri = this.getXri(request);
            boolean autoCommit = this.isAutoCommitting(request);
            InteractionSpec get = InteractionSpecs.getRestInteractionSpecs(
                !autoCommit && this.isRetainValues(request)
            ).GET;
            // Object
            if(xri.size() % 2 == 1) {
                if(xri.containsWildcard()) {
                    throw new UnsupportedOperationException("WILDCARD");//  TODO
                } else {
                    IndexedRecord input;
                    RecordFactory factory = this.connectionFactory.getRecordFactory();
                    if(factory instanceof ExtendedRecordFactory) {
                        input = ((ExtendedRecordFactory)factory).singletonIndexedRecord(
                            Multiplicities.LIST,
                            null,
                            xri
                        );
                    } else {
                        input = factory.createIndexedRecord(
                            Multiplicities.LIST
                        );
                        input.add(xri);
                    }
                    IndexedRecord output = (IndexedRecord) interaction.execute(
                        get, 
                        input
                    );
                    if(output == null) {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);                    
                    }  else if(output.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);                    
                    } else {
                        RestFormat.format(
                            new ServletTarget(request, response), 
                            Object_2Facade.newInstance((MappedRecord)output.get(0))
                        );
                        response.setStatus(HttpServletResponse.SC_OK);                    
                    }
                }
            } else {
                //
                // Collection
                //
                MappedRecord input;
                if(request.getHeader("interaction-verb") == null) {
                    Query_2Facade inputFacade = Query_2Facade.newInstance();
                    String queryType = request.getParameter("queryType");
                    String query = request.getParameter("query"); 
                    inputFacade.setPath(xri);
                    inputFacade.setQueryType(
                        queryType == null ? (String)Model_1Factory.getModel().getTypes(xri.getChild(":*"))[2].objGetValue("qualifiedName") : queryType
                    );
                    inputFacade.setQuery(query);
                    String position = request.getParameter("position");
                    inputFacade.setPosition(
                        position == null ? Integer.valueOf(DEFAULT_POSITION) : Integer.valueOf(position)
                    );
                    String size = request.getParameter("size");
                    inputFacade.setSize(
                        Integer.valueOf(size == null ? DEFAULT_SIZE : Integer.parseInt(size))
                    );
                    String[] groups = request.getParameterValues("groups"); 
                    if(groups != null) {
                        inputFacade.setGroup(
                            Sets.asSet(groups)
                        );
                    }
                    input = inputFacade.getDelegate();
                } else {
                    input = RestFormat.parseRequest(
                        RestFormat.asSource(request),
                        null
                    );
                }
                IndexedRecord output = (IndexedRecord) interaction.execute(
                    get, 
                    input
                );
                if(output == null) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);                    
                } else {
                    RestFormat.format(
                        new ServletTarget(request, response), 
                        xri, 
                        output
                    );
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            }
        } catch(ResourceException exception) {
            this.handleException(
                request, 
                response, 
                exception
            );
        } catch(Exception exception) {
            this.handleException(
                request, 
                response, exception
            );
        } finally {
            try {
                interaction.close();
            } catch (ResourceException exception) {
                Throwables.log(exception);
            }
        }
    }

    /**
     * HTTP POST Request
     * 
     * @param request the HTTP Request
     * @param response the HTTP Response
     *  
     * @exception ServletException
     * @exception IOException
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(
        HttpServletRequest request, 
        HttpServletResponse response
    ) throws ServletException, IOException {
        prepare(request, response);
        Path xri = this.getXri(request);
        if(isConnectionObjectIdentifier(xri)) {
            HttpSession session = request.getSession(true);
            Connection connection = (Connection)session.getAttribute(Connection.class.getName());
            if(connection == null) {
                String userName = request.getParameter("UserName");
                String password;
                if(userName == null) {
                    userName = request.getRemoteUser();
                    password = session.getId();
                } else {
                    password = request.getParameter("Password");
                }
                try {
                    session.setAttribute(
                        "org.openmdx.rest.AutoCommit",
                        Boolean.FALSE
                    );
                    session.setAttribute(
                        Connection.class.getName(), 
                        connection = this.connectionFactory.getConnection(
                            new RestConnectionSpec(
                                userName, 
                                password
                            )
                        )
                    );
                } catch (ResourceException exception) {
                    handleException(
                        request,
                        response, 
                        exception
                    );
                } catch (Exception exception) {
                    handleException(
                        request, 
                        response, exception
                    );
                }
            } else {
                handleException(
                    request, 
                    response, 
                    ResourceExceptions.initHolder(
                        new ResourceException(
                            "This session has already established a connection",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ILLEGAL_STATE,
                                new BasicException.Parameter(
                                    "AutoCommit",
                                    isAutoCommitting(request)
                                )
                            )
                        )
                    )
                );
            }
        } else {
            Interaction interaction = getInteraction(request);
            try {
                MappedRecord value = RestFormat.parseRequest(
                    RestFormat.asSource(request),
                    xri
                ); 
                if(Object_2Facade.isDelegate(value)) {
                    MappedRecord input;
                    if(xri.equals(Object_2Facade.getPath(value))) {
                        input = value;
                    } else {
                        RecordFactory factory = this.connectionFactory.getRecordFactory();
                        if(factory instanceof ExtendedRecordFactory) {
                            input = ((ExtendedRecordFactory)factory).singletonMappedRecord(
                                Multiplicities.MAP,
                                null,
                                xri,
                                value
                            );
                        } else {
                            input = factory.createMappedRecord(Multiplicities.MAP);
                            input.put(xri, value);
                        }
                    }
                    IndexedRecord output = (IndexedRecord) execute(
                        interaction,
                        !isTransactionObjectIdentifier(xri) && isAutoCommitting(request),
                        true, 
                        InteractionSpecs.getRestInteractionSpecs(isRetainValues(request)).CREATE, 
                        input
                    );
                    if(output == null || output.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    } else {
                        ServletTarget target = new ServletTarget(request, response);
                        for(Object record : output){
                            RestFormat.format(
                                target, 
                                Object_2Facade.newInstance((MappedRecord) record)
                            );
                        }
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                } else {
                    MappedRecord input = this.connectionFactory.getRecordFactory().createMappedRecord(MessageRecord.NAME);
                    if(input instanceof MessageRecord) {
                        ((MessageRecord)input).setPath(xri);
                        ((MessageRecord)input).setBody(value);
                    } else {
                         input.put("path", xri);
                         input.put("body", value);
                    }
                    MappedRecord output = (MappedRecord) execute(  
                        interaction,
                        !isTransactionCommitIdentifier(xri) && isAutoCommitting(request), 
                        false, 
                        InteractionSpecs.getRestInteractionSpecs(true).INVOKE, 
                        input
                    );   
                    if(output == null || output.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    } else {
                        MessageRecord reply;
                        if(output instanceof MessageRecord) {
                            reply = (MessageRecord) output;
                        } else {
                            reply = (MessageRecord) Records.getRecordFactory().createMappedRecord(MessageRecord.NAME);
                            reply.setPath(xri);
                            reply.setBody(value);
                        }
                        RestFormat.format(
                            new ServletTarget(request, response), 
                            reply
                        );
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                }
            } catch(ResourceException exception) {
                this.handleException(
                    request, 
                    response, 
                    exception
                );
            } catch(Exception exception) {
                this.handleException(
                    request, 
                    response, exception
                );
            } finally {
                try {
                    interaction.close();
                } catch (ResourceException exception) {
                    Throwables.log(exception);
                }
            }
        }
    }

    /**
     * HTTP PUT Request
     * 
     * @param request the HTTP Request
     * @param response the HTTP Response
     *  
     * @exception ServletException
     * @exception IOException
     */
    @Override
    protected void doPut(
        HttpServletRequest request, 
        HttpServletResponse response
    ) throws ServletException, IOException {
        prepare(request, response);
        Interaction interaction = getInteraction(request);
        try {
            IndexedRecord output = (IndexedRecord) execute(  
                interaction,
                isAutoCommitting(request), 
                true, 
                InteractionSpecs.getRestInteractionSpecs(isRetainValues(request)).PUT, 
                RestFormat.parseRequest(
                    RestFormat.asSource(request),
                    this.getXri(request)
                )
            );
            if(output == null || output.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                ServletTarget target = new ServletTarget(request, response);
                for(Object record : output) {
                    RestFormat.format(
                        target, 
                        Object_2Facade.newInstance((MappedRecord) record)
                    );
                }
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch(ResourceException exception) {
            this.handleException(
                request, 
                response, 
                exception
            );
        } catch(Exception exception) {
            this.handleException(
                request, 
                response, exception
            );
        } finally {
            try {
                interaction.close();
            } catch (ResourceException exception) {
                Throwables.log(exception);
            }
        }
    }

    /**
     * Set response content type and encoding
     * 
     * @param request
     * @param response
     */
    protected void prepare(
        HttpServletRequest request, 
        HttpServletResponse response
    ){
        //
        // Use accept headers
        //
        String characterEncoding = new HttpHeaderFieldValue(
            request.getHeaders("Accept-Charset")
        ).getPreferredContent(
            "UTF-8"
        ).getValue(
        );
        HttpHeaderFieldValue acceptType = new HttpHeaderFieldValue(request.getHeaders("Accept"));
        for(HttpHeaderFieldContent candidate : acceptType){
            String mimeType = candidate.getValue();
            if(XMLOutputFactories.isSupported(mimeType)) {
                response.setContentType(mimeType);
                response.setCharacterEncoding(candidate.getParameterValue("charset", characterEncoding));
                return;
            }
        }
        //
        // Fallback: reply similar to request
        //
        String contentType = request.getContentType();
        if(contentType != null) {
            HttpHeaderFieldContent requestType = new HttpHeaderFieldContent(contentType);
            String mimeType = requestType.getValue();
            if(XMLOutputFactories.isSupported(mimeType)) {
                response.setContentType(mimeType);
                response.setCharacterEncoding(requestType.getParameterValue("charset", characterEncoding));
                return;
            }
        }
        //
        // Last resort
        //
        response.setContentType("text/xml"); 
        // Note that our default encoding is "UTF-8" even for "text/xml" as 
        // opposed to the RFC 3023 specification which requires "US-ASCII" as
        // default encoding for "text/xml"! 
        response.setCharacterEncoding(characterEncoding); 
    }
    
    
    //------------------------------------------------------------------------
    // Class ServletTarget
    //------------------------------------------------------------------------
    
    /**
     * Servlet Output Target
     */
    static class ServletTarget extends RestFormat.Target {
        
        /**
         * Constructor 
         *
         * @param request
         * @param response
         * 
         * @throws ServiceException
         */
        ServletTarget(
            HttpServletRequest request,
            HttpServletResponse response
        ) {
            super(RestFormat.getBase(request));
            this.response = response;
        }

        /**
         * The underlying HTTP response
         */
        private final HttpServletResponse response;

        /* (non-Javadoc)
         * @see org.openmdx.application.rest.http.RestFormat.Target#newWriter()
         */
        @Override
        protected XMLStreamWriter newWriter(
        ) throws XMLStreamException {
            try {
                String mimeType = this.response.getContentType();
                XMLOutputFactory xmlOutputFactory = RestFormat.getOutputFactory(mimeType);
                if(("application/vnd.openmdx.wbxml".equals(mimeType))) {
                    String characterEncoding = this.response.getCharacterEncoding();
                    return characterEncoding == null ? xmlOutputFactory.createXMLStreamWriter(
                        this.response.getOutputStream()
                    ) : xmlOutputFactory.createXMLStreamWriter(
                        this.response.getOutputStream(),
                        characterEncoding
                    );
                } else {
                    return xmlOutputFactory.createXMLStreamWriter(
                        this.response.getWriter()
                    );
                }
            } catch (IOException exception) {
                throw toXMLStreamException(exception);
            }
        }
        
    }
    
}