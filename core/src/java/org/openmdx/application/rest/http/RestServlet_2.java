/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Servlet 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2011, OMEX AG, Switzerland
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceWarning;
import javax.resource.spi.CommException;
import javax.resource.spi.LocalTransactionException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Status;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.application.rest.spi.InboundConnectionFactory_2;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.HttpHeaderFieldContent;
import org.openmdx.base.io.HttpHeaderFieldValue;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.rest.spi.RestFormat;
import org.openmdx.base.rest.spi.RestFormat.Source;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.xml.stream.XMLOutputFactories;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.xml.sax.InputSource;

/**
 * REST Servlet
 */
public class RestServlet_2 extends HttpServlet {

    private final static int DEFAULT_POSITION = 0;
    private final static int DEFAULT_SIZE = 25;
    
    /**
     * Use "text/xml" as pretty printing default value.
     */
    private final static String DEFAULT_MIME_TYPE = "text/xml";
    
    /**
     * Note that our default encoding is "UTF-8" even for "text/xml" as 
     * opposed to the RFC 3023 specification which requires "US-ASCII" as
     * default encoding for "text/xml"! 
     */
    private final static String DEFAULT_CHARACTER_ENCODING = "UTF-8";
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -4403464830407956377L;
    
    /**
     * The servlet's connection factory
     */
    protected ConnectionFactory connectionFactory;

    /**
     * The REST Servlet's default login configuration entry
     */
    protected static final AppConfigurationEntry REMOTE_USER_LOGIN_CONFIGURATION = new AppConfigurationEntry(
        RemoteUserLoginModule.class.getName(),
        LoginModuleControlFlag.REQUIRED,
        Collections.<String,Object>emptyMap()
    );
    
    /**
     * The REST Servlet's standard login configuration
     */
    private final Configuration STANDARD_LOGIN_CONFIGURATION = new Configuration() {

        /**
         * Provide 
         * 
         * @param the application name, usually one of<ul>
         * <li><code>"connect"</code>
         * <li><code>"auto-connect"</code>
         * </ul>
         */
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(
            String name
        ) {
        	return getLoginConfiguration(name);
        }

        @Override
        public void refresh() {
           // nothing to do
        }

     };    
    
     /**
      * Provide the REST Servlet's login configuration
      * 
      * @param name
      * @return
      */
     protected AppConfigurationEntry[] getLoginConfiguration(
         String name
     ) {
         return new AppConfigurationEntry[]{
             REMOTE_USER_LOGIN_CONFIGURATION
         };
     }
     
    /**
     * Provides the login configuration 
     * 
     * @return the login configuration to be used
     */
    protected Configuration getLoginConfiguration(){
    	return STANDARD_LOGIN_CONFIGURATION;
    }
    
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

    protected Connection newConnection(
    	String configurationName,
    	CallbackHandler callbackHandler
    ) throws ResourceException {
    	try {
	        Subject subject = new Subject();
	        LoginContext loginContext = new LoginContext(
	            configurationName, 
	            subject, 
	            callbackHandler, 
	            getLoginConfiguration()
	        );
	        loginContext.login();
	        for(ConnectionSpec connectionSpec : subject.getPublicCredentials(ConnectionSpec.class)) {
	           return this.connectionFactory.getConnection(connectionSpec);
	        }
    	} catch (LoginException exception) {
            throw new SecurityException(
            	"Login for the configuration '" + configurationName + "' failed",
            	exception
            );
    	}
        throw new SecurityException (
        	"The login module for the configuration '" + configurationName + "' did not provide a JCA connection spec"
        );
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
            Boolean autoCommit = (Boolean) session.getAttribute("org.openmdx.rest.AutoCommit");
            if(autoCommit == null) {
                session.setAttribute(
                    "org.openmdx.rest.AutoCommit",
                    autoCommit = Boolean.TRUE
                );
            }
            return autoCommit ? this.newConnection(
                "auto-connect",
                new RequestCallbackHandler(request)
            ) : (Connection)session.getAttribute(
                Connection.class.getName()
            );
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
        return RestFormat.toResourceIdentifier(request.getServletPath());
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
        ServletTarget target = new ServletTarget(request, response);
        RestFormat.format(target, exceptionStack);
        try {
            target.close();
        } catch (XMLStreamException closeException) {
            SysLog.warning("Failure upon exception propagation", closeException);
        }
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
        ServletTarget target = new ServletTarget(request, response);
        RestFormat.format(target, exceptionStack);
        try {
            target.close();
        } catch (XMLStreamException closeException) {
            SysLog.warning("Failure upon exception propagation", closeException);
        }
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
            // Do not isolate units of works            
            Map<Object,Object> overrides = new HashMap<Object,Object>();
            overrides.put(
                ConfigurableProperty.Multithreaded.qualifiedName(),
                Boolean.FALSE.toString()
            );
            if(config.getInitParameter("RefInitializeOnCreate") != null) {
                log("The init parameter 'RefInitializeOnCreate' is no longer supported");
            }
            this.connectionFactory = InboundConnectionFactory_2.newInstance(
                entityManagerFactoryName == null ? "jdo:EntityManagerFactory" : 
                entityManagerFactoryName.indexOf(':') < 0 ? "jdo:" + entityManagerFactoryName :
                entityManagerFactoryName, 
                overrides
            );            
        } catch(Exception exception) {
            throw Throwables.log(
                new ServletException(exception)
            );            
        }
    }

    /**
     * Re-fetch objects after auto-commit
     * 
     * @param interaction
     * @param reply the list to be updated
     * 
     * @throws ResourceException
     */
    @SuppressWarnings(
        {"unchecked", "cast", "rawtypes"}
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
                            Multiplicity.LIST.toString(),
                            null,
                            xri
                        );
                    } else {
                        newRequest = factory.createIndexedRecord(
                            Multiplicity.LIST.toString()
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
            int status = Status.STATUS_ACTIVE;
            try {
                Record reply = interaction.execute(
                    interactionSpec, 
                    input
                );
                status = Status.STATUS_COMMITTING;
                transaction.commit();
                status = Status.STATUS_COMMITTED;
                if(reFetchAfterAutoCommit && reply instanceof IndexedRecord) {
                    reFetch(interaction, (IndexedRecord)reply);
                }
                return reply; 
            } catch (ResourceException exception) {
                switch(status) {
                    case Status.STATUS_ACTIVE:
                        try {
                            transaction.rollback();
                        } catch(Exception rollbackException) {
                            BasicException exceptionChain = BasicException.newEmbeddedExceptionStack(
                                rollbackException,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.HEURISTIC
                            );
                            exceptionChain.getCause(
                                null
                            ).initCause(
                                exception
                            );
                            throw ResourceExceptions.initHolder(
                                new LocalTransactionException(
                                    "Rollback triggered by execution failure failed itself",
                                    exceptionChain
                                )
                            );
                        }
                        throw ResourceExceptions.initHolder(
                            new LocalTransactionException(
                                "Transaction rolled back due to execution failure",
                                BasicException.newEmbeddedExceptionStack(
                                    exception,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.ROLLBACK
                                )
                            )
                        );
                    case Status.STATUS_COMMITTING:
                        throw exception;
                    case Status.STATUS_COMMITTED:
                        throw ResourceExceptions.initHolder(
                            new ResourceWarning(
                                "Re-fetch after auto-commit failed",
                                BasicException.newEmbeddedExceptionStack(
                                    exception,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.TRANSFORMATION_FAILURE
                                )
                            )
                        );
                    default:
                        throw exception;
                }
            }
        } else {
            return interaction.execute(
                interactionSpec, 
                input
            );                
        }
    }
    
    /**
     * Tells whether the request's query string shall be interpreted as REST input record
     * 
     * @param request
     * 
     * @return <code>true</code> if the query string shall be interpreted as REST input record
     */
    private static boolean hasQuery(
        HttpServletRequest request
    ){
        String query = request.getQueryString();
        return query != null && query.length() != 0;
    }

    /**
     * Retrieve the interaction spec. The HTTP function may be overridden by a function name parameter.
     * 
     * @param request
     * @param defaultFunction the default function, usually corresponding to the HTTP method
     * 
     * @return the interaction spec to be used
     * 
     * @throws ResourceException
     */
    private RestInteractionSpec getInteractionSpec(
        HttpServletRequest request,
        RestFunction defaultFunction
    ) throws ResourceException {
        String functionName = request.getParameter("FunctionName");
        RestFunction restFunction;
        if(functionName == null) {
            restFunction = defaultFunction;
        } else try {
            restFunction = RestFunction.valueOf(functionName);
        } catch (IllegalArgumentException exception) {
            throw new ResourceException (
                "Unsupported REST function name '" + functionName + "'",
                exception
            );
        }
        InteractionSpecs interactionSpecs = InteractionSpecs.getRestInteractionSpecs(this.isRetainValues(request));
        switch(restFunction) {
            case GET: return interactionSpecs.GET;
            case PUT: return interactionSpecs.PUT;
            case DELETE: return interactionSpecs.DELETE;
            case POST: return interactionSpecs.CREATE;
            default: return null;
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
        prolog(request, response);
        Interaction interaction = getInteraction(request);
        try {
            Path xri = this.getXri(request);
            String contentType = request.getContentType();
            if(
            	"application/x-www-form-urlencoded".equals(contentType) || 
            	(contentType == null && !hasQuery(request))
            ) {                
            	execute(  
                    interaction,
                    !isTransactionObjectIdentifier(xri) && isAutoCommitting(request), 
                    false, 
                    InteractionSpecs.getRestInteractionSpecs(false).DELETE, 
                    Query_2Facade.newInstance(xri).getDelegate()
                );
            } else {
                MappedRecord object = RestFormat.parseRequest(
                    RestServlet_2.getSource(request, response),
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
                if(this.isAutoCommitting(request)) {
                    Connection connection = interaction.getConnection();
                    interaction.close();
                    connection.close();
                } else {
                    interaction.close();
                }
            } catch (ResourceException exception) {
                Throwables.log(exception);
            }
        }
        epilog(request, response);
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
        prolog(request, response);
        Interaction interaction = getInteraction(request);
        try {
            Path xri = this.getXri(request);
            InteractionSpec get = InteractionSpecs.getRestInteractionSpecs(
                !this.isAutoCommitting(request) && this.isRetainValues(request)
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
                            Multiplicity.LIST.toString(),
                            null,
                            xri
                        );
                    } else {
                        input = factory.createIndexedRecord(
                    		Multiplicity.LIST.toString()
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
                        response.setStatus(HttpServletResponse.SC_OK);                    
                        ServletTarget target = new ServletTarget(request, response); 
                        RestFormat.format(target, Facades.asObject((MappedRecord)output.get(0)));
                        target.close();
                    }
                }
            } else {
                //
                // Collection
                //
                MappedRecord input;
                if(request.getHeader("interaction-verb") == null) {
                    Query_2Facade inputFacade = Facades.newQuery(xri);
                    String queryType = request.getParameter("queryType");
                    String query = request.getParameter("query"); 
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
                        inputFacade.setGroups(
                            Sets.asSet(groups)
                        );
                    }
                    input = inputFacade.getDelegate();
                } else {
                    input = RestFormat.parseRequest(
                        RestServlet_2.getSource(request, response),
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
                    response.setStatus(HttpServletResponse.SC_OK);
                    ServletTarget target = new ServletTarget(request, response); 
                    RestFormat.format(target, xri, output);
                    target.close();
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
                if(this.isAutoCommitting(request)) {
                    Connection connection = interaction.getConnection();
                    interaction.close();
                    connection.close();
                } else {
                    interaction.close();
                }
            } catch (ResourceException exception) {
                Throwables.log(exception);
            }
        }
        epilog(request, response);
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
        prolog(request, response);
        Path xri = this.getXri(request);
        if(isConnectionObjectIdentifier(xri)) {
            HttpSession session = request.getSession(true);
            Connection connection = (Connection)session.getAttribute(Connection.class.getName());
            if(connection == null) {
                try {
                    session.setAttribute(
                        "org.openmdx.rest.AutoCommit",
                        Boolean.FALSE
                    );
                    session.setAttribute(
                        Connection.class.getName(),
                        connection = this.newConnection(
                    		"connect",
                    		new RequestCallbackHandler(request)
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
                        response, 
                        exception
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
                    RestServlet_2.getSource(request, response),
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
                                Multiplicity.MAP.toString(),
                                null,
                                xri,
                                value
                            );
                        } else {
                            input = factory.createMappedRecord(Multiplicity.MAP.toString());
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
                        response.setStatus(HttpServletResponse.SC_OK);
                        ServletTarget target = new ServletTarget(request, response);
                        for(Object record : output){
                            RestFormat.format(
                                target, 
                                Facades.asObject((MappedRecord) record)
                            );
                        }
                        target.close();
                    }
                } else if(Query_2Facade.isDelegate(value)) {
                    boolean multivalued;
                    if(xri.size() % 2 == 0) {
                       multivalued = true;
                    } else if(xri.containsWildcard()) {
                       throw new UnsupportedOperationException("WILDCARD");// TODO
                    } else{
                       multivalued = false;
                    }
                    InteractionSpec get = InteractionSpecs.getRestInteractionSpecs(!this.isAutoCommitting(request) && this.isRetainValues(request)).GET;
                    IndexedRecord output = (IndexedRecord) interaction.execute(get, value);
                    if(output == null) {
                       response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    } else if(multivalued) {
                       response.setStatus(HttpServletResponse.SC_OK);
                       ServletTarget target = new ServletTarget(request, response);
                       RestFormat.format(target, xri, output);
                       target.close();
                    } else if(output.isEmpty()) {
                       response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    } else{
                       response.setStatus(HttpServletResponse.SC_OK);
                       ServletTarget target = new ServletTarget(request, response);
                       RestFormat.format(target, Facades.asObject((MappedRecord) output.get(0)));
                       target.close();
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
                        response.setStatus(HttpServletResponse.SC_OK);
                        ServletTarget target = new ServletTarget(request, response); 
                        RestFormat.format(target, reply);
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
                    if(this.isAutoCommitting(request)) {
                        Connection connection = interaction.getConnection();
                        interaction.close();
                        connection.close();
                    } else {
                        interaction.close();
                    }
                } catch (ResourceException exception) {
                    Throwables.log(exception);
                }
            }
        }
        epilog(request, response);
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
        prolog(request, response);
        Interaction interaction = getInteraction(request);
        try {
            MappedRecord input;
            Path xri = this.getXri(request);
            try {
                input = RestFormat.parseRequest(
                    RestServlet_2.getSource(request, response),
                    xri
                );
            } catch (ServiceException exception) {
                throw ResourceExceptions.initHolder(
                    new CommException(
                        "Request could not be parsed properly",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.TRANSFORMATION_FAILURE,
                            new BasicException.Parameter("xri", xri.toXRI())
                        )
                    )
                );
            }
            IndexedRecord output = (IndexedRecord) execute(  
                interaction,
                isAutoCommitting(request), 
                true, 
                this.getInteractionSpec(request, RestFunction.PUT), 
                input
            );
            if(output == null || output.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                ServletTarget target = new ServletTarget(request, response);
                for(Object record : output) {
                    RestFormat.format(
                        target, 
                        Facades.asObject((MappedRecord) record)
                    );
                }
                target.close();
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
                if(this.isAutoCommitting(request)) {
                    Connection connection = interaction.getConnection();
                    interaction.close();
                    connection.close();
                } else {
                    interaction.close();
                }
            } catch (ResourceException exception) {
                Throwables.log(exception);
            }
        }
        epilog(request, response);
    }

    /**
     * Commit the response
     * 
     * @param request
     * @param response
     * 
     * @throws IOException 
     */
    protected void epilog(
        HttpServletRequest request, 
        HttpServletResponse response
    ) throws IOException{
        //
        // Force any content in the buffer to be written to the client
        //
        response.flushBuffer();
    }
    
    /**
     * Set response content type and encoding
     * 
     * @param request
     * @param response
     */
    protected void prolog(
        HttpServletRequest request, 
        HttpServletResponse response
    ){
        //
        // Use accept headers
        //
        String characterEncoding = new HttpHeaderFieldValue(
            request.getHeaders("Accept-Charset")
        ).getPreferredContent(
            DEFAULT_CHARACTER_ENCODING
        ).getValue(
        );
        HttpHeaderFieldValue acceptType = new HttpHeaderFieldValue(request.getHeaders("Accept"));
        for(HttpHeaderFieldContent candidate : acceptType){
            String mimeType = candidate.getValue();
            if(XMLOutputFactories.isSupported(mimeType)) {
                prepare(
                    response,
                    mimeType,
                    candidate.getParameterValue("charset", characterEncoding)
                );
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
                prepare(
                    response,
                    mimeType,
                    requestType.getParameterValue("charset", characterEncoding)
                );
                return;
            }
        }
        //
        // Last resort
        //
        prepare(
            response,
            DEFAULT_MIME_TYPE,
            characterEncoding
        );
    }
    
    /**
     * Set content type and character encoding
     * 
     * @param response
     * @param mimeType
     * @param characterEncoding
     */
    private void prepare(
        HttpServletResponse response,
        String mimeType,
        String characterEncoding
    ){
        response.setContentType(mimeType + ";charset=" + characterEncoding);
        response.setCharacterEncoding(characterEncoding);
    }
    
    /**
     * Retrieve the decoded query string
     * 
     * @param request
     * 
     * @return the decoded query string; or <code>null</code> if the query is either missing or empty
     * 
     * @throws IOException
     */
    private static String getQueryString(
        HttpServletRequest request
    ) throws IOException{
        String query = request.getQueryString();
        return query == null || query.length() == 0 ? null : URLDecoder.decode(query, "UTF-8"); 
    }
    
    /**
     * Use the query string if a request has no body
     * 
     * @param request
     * 
     * @return the request's query string reader which may be empty but never <code>null</code>.
     * 
     * @throws IOException 
     */
    private static Reader getQueryReader(
        HttpServletRequest request
    ) throws IOException {
        String query = getQueryString(request);
        return new StringReader(query == null ? "" : query);
    }

    /**
     * Use the query string if a request has no body
     * 
     * @param request
     * 
     * @return the request's query string input stream which may be empty but never <code>null</code>.
     * 
     * @throws IOException 
     */
    private static InputStream getQueryInputStream(
        HttpServletRequest request
    ) throws IOException {
        String query = request.getQueryString(); // getQueryString(request);
        return new ByteArrayInputStream(
            query == null ? new byte[0] : Base64.decode(query)
        );
    }
    
    /**
     * Tells whether the query string should be treated as input record
     * 
     * @param request
     * 
     * @return <code>true</code> if the HTTP request usually has no body
     */
    private static boolean useQueryAsSource(
        HttpServletRequest request
    ){
        String method = request.getMethod();
        return "GET".equals(method) || "DELETE".equals(method);
    }
    
    /**
     * Retrieve the base URL
     * 
     * @return the base URL
     */
    protected static String getBase(
        HttpServletRequest request
    ) {
        String contextPath = request.getContextPath();
        StringBuffer url = request.getRequestURL();
        return url.substring(
            0,
            url.indexOf(contextPath) + contextPath.length()
        );
    }

    /**
     * Provide a <code>parse()</code> source
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * 
     * @return a <code>Source</code>
     * 
     * @throws ServiceException
     */
    private static Source getSource(
        HttpServletRequest request, 
        HttpServletResponse response
    ) throws ServiceException {
        try {
            HttpHeaderFieldValue contentHeader = new HttpHeaderFieldValue(
                request.getHeaders("Content-Type")
            ); 
            boolean query = contentHeader.isEmpty() && useQueryAsSource(request);
            HttpHeaderFieldContent contentType = query ? new HttpHeaderFieldContent(
                response.getContentType()
            ) : contentHeader.getPreferredContent(
                "application/xml;charset=UTF-8"
            );
            String mimeType = contentType.getValue();
            String encoding = contentType.getParameterValue("charset", null);
            InputSource inputSource = RestFormat.isBinary(mimeType) ? new InputSource(
                query ? getQueryInputStream(request) : request.getInputStream()
            ) : new InputSource (
                query ? getQueryReader(request) : request.getReader()
            ); 
            inputSource.setEncoding(encoding);
            return new Source(
                RestServlet_2.getBase(request), 
                inputSource, 
                mimeType, 
                null
            );
        } catch (IOException exception) {
            throw new ServiceException(exception);
        }
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
            super(RestServlet_2.getBase(request));
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
                HttpHeaderFieldContent contentType = new HttpHeaderFieldContent(this.response.getContentType());
                String mimeType = contentType.getValue();
                XMLOutputFactory xmlOutputFactory = RestFormat.getOutputFactory(mimeType);
                if((RestFormat.isBinary(mimeType))) {
                    String characterEncoding = contentType.getParameterValue(
                        "charset",
                        this.response.getCharacterEncoding()
                    );
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