/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Servlet 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2014, OMEX AG, Switzerland
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
package org.openmdx.application.rest.http.servlet;

import static org.openmdx.base.accessor.rest.spi.ControlObjects_2.isConnectionObjectIdentifier;
import static org.openmdx.base.accessor.rest.spi.ControlObjects_2.isControlObjectType;
import static org.openmdx.base.accessor.rest.spi.ControlObjects_2.isSessionIdentifier;
import static org.openmdx.base.accessor.rest.spi.ControlObjects_2.isTransactionCommitIdentifier;
import static org.openmdx.base.accessor.rest.spi.ControlObjects_2.isTransactionObjectIdentifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.application.rest.adapter.InboundConnectionFactory_2;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.HttpHeaderFieldContent;
import org.openmdx.base.io.HttpHeaderFieldValue;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.rest.spi.RestFormatters;
import org.openmdx.base.rest.spi.RestParser;
import org.openmdx.base.rest.spi.RestSource;
import org.openmdx.base.rest.stream.RestTarget;
import org.openmdx.base.rest.stream.StandardRestFormatter;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.transaction.Status;
import org.openmdx.base.xml.stream.XMLOutputFactories;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.BinaryLargeObjects;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
     * The eagerly acquired REST formatter instance
     */
    protected static final StandardRestFormatter restFormatter = RestFormatters.getFormatter();
    
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

    /**
     * Tells whether null values shall be serialized
     * 
     * @param request
     * 
     * @return <code>true</code> if null values shall be serialized.
     */
    protected boolean isSerializeNulls(
        HttpServletRequest request
    ){
        String serializeNulls = request.getHeader("serialize-nulls");
        return serializeNulls == null ? false : Boolean.parseBoolean(serializeNulls);
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
            return autoCommit.booleanValue() ? this.newConnection(
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
        String path = request.getServletPath();
        if(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return RestFormatters.toResourceIdentifier(path);
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
        if(exceptionStack.getExceptionCode() == BasicException.Code.NOT_FOUND) {
            SysLog.detail("Resource Exception", exceptionStack);
        } else {
            SysLog.warning("Resource Exception", exceptionStack);            
        }
        response.setStatus(toStatusCode(exceptionStack.getExceptionCode()));
        ServletTarget target = new ServletTarget(request, response);
        restFormatter.format(
            target,
            exceptionStack
        );
        try {
            target.close();
        } catch (IOException ignored) {
            SysLog.trace("Ignored close failure", ignored);
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
        restFormatter.format(
            target,
            exceptionStack
        );
        try {
            target.close();
        } catch (IOException ignored) {
            SysLog.trace("Ignored close failure", ignored);
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
            Map<Object,Object> overrides = new HashMap<Object,Object>();
            overrides.put(
                ConfigurableProperty.IsolateThreads.qualifiedName(),
                Boolean.FALSE.toString()
            );
            overrides.put(
                ConfigurableProperty.Multithreaded.qualifiedName(),
                Boolean.TRUE.toString()
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
        ) {
            Object oldObject = i.next();
            if(oldObject instanceof Record) {
                Record oldRecord = (Record) oldObject;
                if(org.openmdx.base.rest.spi.ObjectRecord.isCompatible(oldRecord)) {
                    Path xri = Object_2Facade.getPath((MappedRecord) oldRecord);
                    QueryRecord query = null;
                    try {
                        query = Facades.newQuery(xri).getDelegate();
                    } catch(Exception ignore) {
                        // ignore
                    }
                    IndexedRecord output = (IndexedRecord) interaction.execute(
                        InteractionSpecs.getRestInteractionSpecs(true).GET,
                        query
                    );
                    if(!output.isEmpty()) {
                        i.set(output.get(0));
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
            Status status = Status.STATUS_ACTIVE;
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
                    case STATUS_ACTIVE:
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
                    case STATUS_COMMITTING:
                        throw exception;
                    case STATUS_COMMITTED:
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
            case PUT: return interactionSpecs.UPDATE;
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
        Path xri = this.getXri(request);
        // Close connection|session
        // Closing the session also closes the connection
        if(isSessionIdentifier(xri) || isConnectionObjectIdentifier(xri)) {
            HttpSession session = request.getSession(true);
            Connection connection = (Connection)session.getAttribute(Connection.class.getName());
            if(connection != null) {
                try {
                    connection.close();
                } catch (ResourceException exception) {
                    Throwables.log(exception);
                }                
            }
            session.setAttribute(
                "org.openmdx.rest.AutoCommit",
                null
            );
            session.setAttribute(
                Connection.class.getName(),
                null
            );
            if(isSessionIdentifier(xri)) {
                try {
                    session.invalidate();
                } catch (IllegalStateException exception) {
                    Throwables.log(exception);
                }
            }
        } else {
            Interaction interaction = getInteraction(request);
            try {
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
                    final MappedRecord object = parseRequest(request, response);
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
    @Override
    protected void doGet(
        HttpServletRequest request, 
        HttpServletResponse response
    ) throws ServletException, IOException {
        Model_1_0 model = Model_1Factory.getModel();
        String servletPath = request.getServletPath();
        if(servletPath.startsWith("/api-ui")) {
            try {
                BinaryLargeObjects.streamCopy(
                    request.getServletContext().getResourceAsStream(servletPath), 
                    0L, 
                    response.getOutputStream()
                );
                response.setStatus(HttpServletResponse.SC_OK);
            } catch(Exception e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if(servletPath.startsWith("/api")) {
            try {
                if(request.getParameter("xri") != null) {
                    response.sendRedirect(
                        request.getContextPath() +
                        new Path(request.getParameter("xri")).toClassicRepresentation() + "/:api"
                    );                
                } else if(request.getParameter("type") != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    PrintWriter pw = response.getWriter();
                    new Swagger(
                        model.getElement(request.getParameter("type"))
                    ).writeAPI(
                        pw, 
                        null, // host 
                        null, // basePath 
                        null // description
                    );
                    pw.close();      
                } else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);                
                }
            } catch(Exception exception) {
                this.handleException(
                    request, 
                    response, exception
                );
            }
        } else {
            prolog(request, response);
            Interaction interaction = getInteraction(request);
            try {
                Path xri = this.getXri(request);
                InteractionSpec get = InteractionSpecs.getRestInteractionSpecs(
                    !this.isAutoCommitting(request) && this.isRetainValues(request)
                ).GET;
                boolean serializeNulls = this.isSerializeNulls(request);
                // Object
                if(xri.isObjectPath()) {
                    if(xri.isPattern()) {
                        throw new UnsupportedOperationException("WILDCARD");//  TODO
                    } else {
                        IndexedRecord output = (IndexedRecord) interaction.execute(
                            get, 
                            Facades.newQuery(xri).getDelegate()
                        );
                        if(output == null) {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);                    
                        }  else if(output.isEmpty()) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);                    
                        } else {
                            response.setStatus(HttpServletResponse.SC_OK);
                            ServletTarget target = new ServletTarget(request, response); 
                            restFormatter.format(
                                target,
                                toObjectRecord(output.get(0)),
                                serializeNulls
                            );
                            target.close();
                        }
                    }
                } else {
                    if(":api".equals(xri.getLastSegment().toClassicRepresentation())) {
                        IndexedRecord output = (IndexedRecord) interaction.execute(
                            get, 
                            Facades.newQuery(xri.getParent()).getDelegate()
                        );
                        if(output == null) {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);                    
                        }  else if(output.isEmpty()) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);                    
                        } else {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            PrintWriter pw = response.getWriter();
                            String basePath = request.getContextPath() + request.getServletPath().replace("/:api", "");
                            String host = null;
                            {
                                String requestURL = request.getRequestURL().toString();
                                int pos1 = requestURL.indexOf("://");
                                int pos2 = requestURL.indexOf("/", pos1 + 3);
                                if(pos2 > pos1) {
                                    host = requestURL.substring(pos1 + 3, pos2);
                                } else {
                                    host= null;
                                }
                            }
                            ObjectRecord object = this.toObjectRecord(output.get(0));
                            new Swagger(
                                model.getElement(object.getValue().getRecordName())
                            ).writeAPI(
                                pw, 
                                host, 
                                basePath, 
                                object.getResourceIdentifier().toXRI()
                            );
                            pw.close();
                        }
                    } else if(":api-ui".equals(xri.getLastSegment().toClassicRepresentation())) {
                        response.sendRedirect(
                            request.getContextPath() +
                            "/api-ui/index.html?url=" + request.getRequestURL().toString().replace(":api-ui", ":api")
                        );
                    } else {
                        // Object query
                        final MappedRecord input;
                        if(request.getHeader("interaction-verb") == null) {
                            Query_2Facade inputFacade = Facades.newQuery(xri);
                            String queryType = request.getParameter("queryType");
                            if(queryType == null){
                                inputFacade.setQueryType(Model_1Factory.getModel().getTypes(xri.getChild(":*"))[2].getQualifiedName());
                            } else {
                                inputFacade.setQueryType(queryType);
                            }
                            String query = request.getParameter("query"); 
                            if(query != null) {
                                inputFacade.setQuery(query);
                            }
                            String position = request.getParameter("position");
                            if(position == null) {
                                inputFacade.setPosition(Integer.valueOf(DEFAULT_POSITION));
                            } else {
                                inputFacade.setPosition(Integer.valueOf(position));
                            }
                            String size = request.getParameter("size");
                            if(size == null) {
                                inputFacade.setSize(Integer.valueOf(DEFAULT_SIZE));
                            } else {
                                inputFacade.setSize(Integer.valueOf(size));
                            }
                            String[] groups = request.getParameterValues("groups"); 
                            if(groups != null) {
                            	switch(groups.length) {
                            	case 0:
                                    inputFacade.setFetchGroupName(null);
                                    break;
                            	case 1:
                                    inputFacade.setFetchGroupName(groups[0]);
                                    break;
                            	default:
                                	throw new RuntimeServiceException(
                	                    BasicException.Code.DEFAULT_DOMAIN,
                	                    BasicException.Code.NOT_SUPPORTED,
                	                    "At most one fetch group may be specified",
                	                    new BasicException.Parameter("groups", (Object[])groups)
                	                );
                            	}
                            }
                            input = inputFacade.getDelegate();
                        } else {
                            input = parseRequest(request, response);
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
                            restFormatter.format(
                                target,
                                xri,
                                output,
                                serializeNulls
                            );
                            target.close();
                        }
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
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
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
                // ignore in case this session has already established a connection
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } else {
            Interaction interaction = getInteraction(request);
            boolean isAutoCommitting = this.isAutoCommitting(request);
            boolean serializeNulls = this.isSerializeNulls(request);            
            try {
                final MappedRecord value = parseRequest(request, response);
                if(org.openmdx.base.rest.spi.ObjectRecord.isCompatible(value)) {
                    MappedRecord input;
                    if(xri.equals(Object_2Facade.getPath(value))) {
                        input = value;
                    } else {
                        RecordFactory factory = this.connectionFactory.getRecordFactory();
                        if(factory instanceof ExtendedRecordFactory) {
                            input = ((ExtendedRecordFactory)factory).singletonMappedRecord(
                                Multiplicity.MAP.code(),
                                null,
                                xri,
                                value
                            );
                        } else {
                            input = factory.createMappedRecord(Multiplicity.MAP.code());
                            input.put(xri, value);
                        }
                    }
                    IndexedRecord output = (IndexedRecord) execute(
                        interaction,
                        !isTransactionObjectIdentifier(xri) && isAutoCommitting,
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
                            restFormatter.format(
                                target, 
                                toObjectRecord(record),
                                serializeNulls
                            );
                        }
                        target.close();
                    }
                } else if(org.openmdx.base.rest.spi.QueryRecord.isCompatible(value)) {
                    boolean multivalued;
                    if(xri.size() % 2 == 0) {
                       multivalued = true;
                    } else if(xri.isPattern()) {
                       throw new UnsupportedOperationException("WILDCARD");// TODO
                    } else{
                       multivalued = false;
                    }
                    InteractionSpec get = InteractionSpecs.getRestInteractionSpecs(!isAutoCommitting && this.isRetainValues(request)).GET;
                    IndexedRecord output = (IndexedRecord) interaction.execute(get, value);
                    if(output == null) {
                       response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    } else if(multivalued) {
                       response.setStatus(HttpServletResponse.SC_OK);
                       ServletTarget target = new ServletTarget(request, response);
                       restFormatter.format(
                           target,
                           xri,
                           output,
                           serializeNulls
                       );
                       target.close();
                    } else if(output.isEmpty()) {
                       response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    } else{
                       response.setStatus(HttpServletResponse.SC_OK);
                       ServletTarget target = new ServletTarget(request, response);
                       restFormatter.format(
                           target,
                           toObjectRecord(output.get(0)),
                           serializeNulls
                       );
                       target.close();
                    }
                } else {
                    MappedRecord input = this.connectionFactory.getRecordFactory().createMappedRecord(MessageRecord.NAME);
                    if(input instanceof MessageRecord) {
                        ((MessageRecord)input).setResourceIdentifier(xri);
                        ((MessageRecord)input).setBody(value);
                    } else {
                         input.put("path", xri);
                         input.put("body", value);
                    }
                    MappedRecord output = (MappedRecord) execute(  
                        interaction,
                        !isTransactionCommitIdentifier(xri) && isAutoCommitting, 
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
                            reply = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
                            reply.setResourceIdentifier(xri);
                            reply.setBody(value);
                        }
                        response.setStatus(HttpServletResponse.SC_OK);
                        ServletTarget target = new ServletTarget(request, response);
                        restFormatter.format(
                            target,
                            "result",
                            reply,
                            serializeNulls
                        );
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
                    if(isAutoCommitting) {
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
            MappedRecord input = parseRequest(request, response);
            boolean serializeNulls = this.isSerializeNulls(request);            
            IndexedRecord output = (IndexedRecord)execute(  
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
                    restFormatter.format(
                        target, 
                        toObjectRecord(record),
                        serializeNulls
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

	private MappedRecord parseRequest(
		HttpServletRequest request,
		HttpServletResponse response
	) throws CommException {
		Path xri = this.getXri(request);
		try {
		    return RestParser.parseRequest(
		        RestServlet_2.getSource(request, response),
		        xri
		    );
		} catch (SAXException exception) {
		    throw ResourceExceptions.initHolder(
		        new CommException(
		            "Request could not be parsed properly",
		            BasicException.newEmbeddedExceptionStack(
		                exception,
		                BasicException.Code.DEFAULT_DOMAIN,
		                BasicException.Code.TRANSFORMATION_FAILURE,
		                new BasicException.Parameter("xri", xri)
		            )
		        )
		    );
		}
	}

    private ObjectRecord toObjectRecord(Object record) throws ServiceException {
        if (org.openmdx.base.rest.spi.ObjectRecord.isCompatible((Record)record)) {
            return (ObjectRecord) record;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Unexpected record type",
                new BasicException.Parameter("expected", org.openmdx.base.rest.cci.ObjectRecord.NAME),
                new BasicException.Parameter("actual", ((Record)record).getRecordName())
            );
        }
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
    private static RestSource getSource(
        HttpServletRequest request, 
        HttpServletResponse response
    ) throws SAXException {
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
            InputSource inputSource = RestFormatters.isBinary(mimeType) ? new InputSource(
                query ? getQueryInputStream(request) : request.getInputStream()
            ) : new InputSource (
                query ? getQueryReader(request) : request.getReader()
            ); 
            inputSource.setEncoding(encoding);
            return new RestSource(
                RestServlet_2.getBase(request), 
                inputSource, 
                mimeType, 
                null
            );
        } catch (IOException exception) {
            throw new SAXException(exception);
        }
    }
    
    
    //------------------------------------------------------------------------
    // Class ServletTarget
    //------------------------------------------------------------------------
    
    /**
     * Servlet Output Target
     */
    static class ServletTarget extends RestTarget {
        
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
                XMLOutputFactory xmlOutputFactory = restFormatter.getOutputFactory(mimeType);
                if((RestFormatters.isBinary(mimeType))) {
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
            } catch (BasicException exception) {
                throw toXMLStreamException(exception);
            }
        }
        
    }
    
}