/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Servlet 
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

import javax.jdo.FetchGroup;
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
        Collections.<String, Object>emptyMap()
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
         * @param the
         *            application name, usually one of
         *            <ul>
         *            <li><code>"connect"</code>
         *            <li><code>"auto-connect"</code>
         *            </ul>
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
        return new AppConfigurationEntry[] {
            REMOTE_USER_LOGIN_CONFIGURATION
        };
    }

    /**
     * Provides the login configuration
     * 
     * @return the login configuration to be used
     */
    protected Configuration getLoginConfiguration() {
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
    ) {
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
    ) {
        String interactionVerb = request.getHeader("interaction-verb");
        return interactionVerb == null || Integer.parseInt(interactionVerb) != InteractionSpec.SYNC_SEND;
    }

    /**
     * Tells whether null values shall be serialized.
     * 
     * @param request
     * 
     * @return <code>true</code> if null values shall be serialized.
     */
    protected boolean isSerializeNulls(
        HttpServletRequest request
    ) {
        final String serializeNulls = request.getHeader("serialize-nulls");
        return serializeNulls == null ? false : Boolean.parseBoolean(serializeNulls);
    }

    /**
     * Rewrite given fetchGroup according rewrite-fetchgroup pattern.
     * 
     * @param request
     * @param fetchGroup
     * @return
     */
    protected String rewriteFetchGroup(
        HttpServletRequest request,
        String fetchGroup
    ) {
        String rewriteFetchGroup = request.getHeader("rewrite-fetchgroup");
        if(fetchGroup != null && rewriteFetchGroup != null) {
            String[] rewritePattern = rewriteFetchGroup.split("/");
            if(rewritePattern != null && rewritePattern.length == 2) {
                fetchGroup = fetchGroup.replaceAll(
                    rewritePattern[0],
                    rewritePattern[1]
                );
            }
        }
        return fetchGroup;
    }

    protected Connection newConnection(
        final String configurationName,
        final CallbackHandler callbackHandler
    )
        throws ResourceException {
        try {
            final Subject subject = new Subject();
            final LoginContext loginContext = new LoginContext(
                configurationName,
                subject,
                callbackHandler,
                getLoginConfiguration()
            );
            loginContext.login();
            for (final ConnectionSpec connectionSpec : subject.getPublicCredentials(ConnectionSpec.class)) {
                return this.connectionFactory.getConnection(connectionSpec);
            }
        } catch (LoginException exception) {
            throw new SecurityException(
                "Login for the configuration '" + configurationName + "' failed",
                exception
            );
        }
        throw new SecurityException(
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
        final HttpServletRequest request
    )
        throws ServletException {
        try {
            final HttpSession session = request.getSession(true);
            Boolean autoCommit = (Boolean) session.getAttribute("org.openmdx.rest.AutoCommit");
            if (autoCommit == null) {
                session.setAttribute(
                    "org.openmdx.rest.AutoCommit",
                    autoCommit = Boolean.TRUE
                );
            }
            return autoCommit.booleanValue() ? this.newConnection(
                "auto-connect",
                new RequestCallbackHandler(request)
            )
                : (Connection) session.getAttribute(
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
        final HttpServletRequest req
    )
        throws ServletException {
        final Connection connection = getConnection(req);
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
        final HttpServletRequest request
    ) {
        String path = request.getServletPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return RestFormatters.toResourceIdentifier(path);
    }

    /**
     * Map an openMDX exception code to a HTTP status code
     * 
     * @param exceptionCode
     *            an openMDX exception code
     * 
     * @return the corresponding HTTP status code
     */
    private static int toStatusCode(
        final int exceptionCode
    ) {
        return exceptionCode == BasicException.Code.NOT_FOUND ? HttpServletResponse.SC_NOT_FOUND
            : exceptionCode == BasicException.Code.AUTHORIZATION_FAILURE ? HttpServletResponse.SC_FORBIDDEN
                : exceptionCode == BasicException.Code.CONCURRENT_ACCESS_FAILURE ? HttpServletResponse.SC_PRECONDITION_FAILED
                    : exceptionCode == BasicException.Code.ILLEGAL_STATE ? HttpServletResponse.SC_CONFLICT
                        : exceptionCode == BasicException.Code.NOT_IMPLEMENTED ? HttpServletResponse.SC_NOT_IMPLEMENTED
                            : HttpServletResponse.SC_BAD_REQUEST;
    }

    /**
     * Exception handler
     * 
     * @param request
     * @param resp
     * @param exception
     */
    protected void handleException(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final ResourceException exception
    ) {
        BasicException exceptionStack = BasicException.toExceptionStack(exception);
        while (exceptionStack.getExceptionCode() == BasicException.Code.GENERIC &&
            exceptionStack.getCause() != null) {
            exceptionStack = exceptionStack.getCause();
        }
        if (exceptionStack.getExceptionCode() == BasicException.Code.NOT_FOUND) {
            SysLog.detail("Resource Exception", exceptionStack);
        } else {
            SysLog.warning("Resource Exception", exceptionStack);
        }
        response.setStatus(toStatusCode(exceptionStack.getExceptionCode()));
        try (ServletTarget target = new ServletTarget(request, response)) {
            restFormatter.format(
                target,
                exceptionStack
            );
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
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Exception exception
    ) {
        final BasicException exceptionStack = BasicException.toExceptionStack(exception);
        SysLog.error("Internal Server Error", exceptionStack);
        request.getSession().removeAttribute(Connection.class.getName());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        try (ServletTarget target = new ServletTarget(request, response)) {
            restFormatter.format(
                target,
                exceptionStack
            );
        } catch (IOException exception1) {
            SysLog.trace("Ignored close failure", exception1);
        }
    }

    /**
     * Servlet initialization
     * 
     * @param config
     *            servlet configuration
     * 
     * @exception ServletExcetpion
     */
    @Override
    public void init(
        final ServletConfig config
    )
        throws ServletException {
        super.init(config);
        try {
            final String entityManagerFactoryName = config.getInitParameter("entity-manager-factory-name");
            final Map<Object, Object> overrides = new HashMap<Object, Object>();
            overrides.put(
                ConfigurableProperty.IsolateThreads.qualifiedName(),
                Boolean.FALSE.toString()
            );
            overrides.put(
                ConfigurableProperty.Multithreaded.qualifiedName(),
                Boolean.TRUE.toString()
            );
            if (config.getInitParameter("RefInitializeOnCreate") != null) {
                log("The init parameter 'RefInitializeOnCreate' is no longer supported");
            }
            this.connectionFactory = InboundConnectionFactory_2.newInstance(
                entityManagerFactoryName == null ? "jdo:EntityManagerFactory"
                    : entityManagerFactoryName.indexOf(':') < 0 ? "jdo:" + entityManagerFactoryName : entityManagerFactoryName,
                overrides
            );
        } catch (Exception exception) {
            throw Throwables.log(
                new ServletException(exception)
            );
        }
    }

    /**
     * Re-fetch objects after auto-commit
     * 
     * @param interaction
     * @param reply
     *            the list to be updated
     * 
     * @throws ResourceException
     */
    @SuppressWarnings({ "unchecked", "cast", "rawtypes" })
    private void reFetch(
        final Interaction interaction,
        final IndexedRecord reply
    )
        throws ResourceException {
        for (final ListIterator i = ((IndexedRecord) reply).listIterator(); i.hasNext();) {
            final Object oldObject = i.next();
            if (oldObject instanceof Record) {
                final Record oldRecord = (Record) oldObject;
                if (org.openmdx.base.rest.spi.ObjectRecord.isCompatible(oldRecord)) {
                    final Path xri = Object_2Facade.getPath((MappedRecord) oldRecord);
                    QueryRecord query = null;
                    try {
                        query = Facades.newQuery(xri).getDelegate();
                    } catch (Exception ignore) {
                        // ignore
                    }
                    final IndexedRecord output = (IndexedRecord) interaction.execute(
                        InteractionSpecs.getRestInteractionSpecs(true).GET,
                        query
                    );
                    if (!output.isEmpty()) {
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
        final Interaction interaction,
        final boolean autoCommit,
        final boolean reFetchAfterAutoCommit,
        final RestInteractionSpec interactionSpec,
        final Record input
    )
        throws ResourceException {
        if (autoCommit) {
            final LocalTransaction transaction = interaction.getConnection().getLocalTransaction();
            transaction.begin();
            Status status = Status.STATUS_ACTIVE;
            try {
                final Record reply = interaction.execute(
                    interactionSpec,
                    input
                );
                status = Status.STATUS_COMMITTING;
                transaction.commit();
                status = Status.STATUS_COMMITTED;
                if (reFetchAfterAutoCommit && reply instanceof IndexedRecord) {
                    reFetch(interaction, (IndexedRecord) reply);
                }
                return reply;
            } catch (ResourceException exception) {
                switch (status) {
                    case STATUS_ACTIVE:
                        try {
                            transaction.rollback();
                        } catch (Exception rollbackException) {
                            final BasicException exceptionChain = BasicException.newEmbeddedExceptionStack(
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
        final HttpServletRequest request
    ) {
        final String query = request.getQueryString();
        return query != null && query.length() != 0;
    }

    /**
     * Retrieve the interaction spec. The HTTP function may be overridden by a function name parameter.
     * 
     * @param request
     * @param defaultFunction
     *            the default function, usually corresponding to the HTTP method
     * 
     * @return the interaction spec to be used
     * 
     * @throws ResourceException
     */
    private RestInteractionSpec getInteractionSpec(
        final HttpServletRequest request,
        final RestFunction defaultFunction
    )
        throws ResourceException {
        final String functionName = request.getParameter("FunctionName");
        final RestFunction restFunction;
        if (functionName == null) {
            restFunction = defaultFunction;
        } else
            try {
                restFunction = RestFunction.valueOf(functionName);
            } catch (IllegalArgumentException exception) {
                throw new ResourceException(
                    "Unsupported REST function name '" + functionName + "'",
                    exception
                );
            }
        InteractionSpecs interactionSpecs = InteractionSpecs.getRestInteractionSpecs(this.isRetainValues(request));
        switch (restFunction) {
            case GET:
                return interactionSpecs.GET;
            case PUT:
                return interactionSpecs.UPDATE;
            case DELETE:
                return interactionSpecs.DELETE;
            case POST:
                return interactionSpecs.CREATE;
            default:
                return null;
        }
    }

    /**
     * REST DELETE Request
     * 
     * @param request
     *            the HTTP Request
     * @param response
     *            the HTTP Response
     * 
     * @exception ServletException
     * @exception IOException
     */
    @Override
    protected void doDelete(
        final HttpServletRequest request,
        final HttpServletResponse response
    )
        throws ServletException,
        IOException {
        prolog(request, response);
        final Path xri = this.getXri(request);
        // Close connection|session
        // Closing the session also closes the connection
        if (isSessionIdentifier(xri) || isConnectionObjectIdentifier(xri)) {
            final HttpSession session = request.getSession(true);
            final Connection connection = (Connection) session.getAttribute(Connection.class.getName());
            if (connection != null) {
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
            if (isSessionIdentifier(xri)) {
                try {
                    session.invalidate();
                } catch (IllegalStateException exception) {
                    Throwables.log(exception);
                }
            }
        } else {
            final Interaction interaction = getInteraction(request);
            try {
                final String contentType = request.getContentType();
                if ("application/x-www-form-urlencoded".equals(contentType) ||
                    (contentType == null && !hasQuery(request))) {
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
            } catch (ResourceException exception) {
                this.handleException(
                    request,
                    response,
                    exception
                );
            } catch (Exception exception) {
                this.handleException(
                    request,
                    response,
                    exception
                );
            } finally {
                try {
                    if (this.isAutoCommitting(request)) {
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
     * @param request
     *            the HTTP Request
     * @param response
     *            the HTTP Response
     * 
     * @exception ServletException
     * @exception IOException
     */
    @Override
    protected void doGet(
        final HttpServletRequest request,
        final HttpServletResponse response
    )
        throws ServletException,
        IOException {
        final Model_1_0 model = Model_1Factory.getModel();
        final String servletPath = request.getServletPath();
        if (servletPath.startsWith("/api-ui")) {
            try {
                BinaryLargeObjects.streamCopy(
                    request.getServletContext().getResourceAsStream(servletPath),
                    0L,
                    response.getOutputStream()
                );
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if (servletPath.startsWith("/api")) {
            try {
                if (request.getParameter("xri") != null) {
                    response.sendRedirect(
                        request.getContextPath() +
                            new Path(request.getParameter("xri")).toClassicRepresentation() + "/:api"
                    );
                } else if (request.getParameter("type") != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    try (PrintWriter pw = response.getWriter()) {
                        new Swagger(
                            model.getElement(request.getParameter("type"))
                        ).writeAPI(
                            pw,
                            null, // host 
                            null, // basePath 
                            null // description
                        );
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
            } catch (Exception exception) {
                this.handleException(
                    request,
                    response, exception
                );
            }
        } else {
            prolog(request, response);
            final Interaction interaction = getInteraction(request);
            try {
                final Path xri = this.getXri(request);
                final InteractionSpec get = InteractionSpecs.getRestInteractionSpecs(
                    !this.isAutoCommitting(request) && this.isRetainValues(request)
                ).GET;
                final boolean serializeNulls = this.isSerializeNulls(request);
                // Object
                if (xri.isObjectPath()) {
                    if (xri.isPattern()) {
                        throw new UnsupportedOperationException("WILDCARD");//  TODO
                    } else {
                        final IndexedRecord output = (IndexedRecord) interaction.execute(
                            get,
                            Facades.newQuery(xri).getDelegate()
                        );
                        if (output == null) {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        } else if (output.isEmpty()) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        } else {
                            response.setStatus(HttpServletResponse.SC_OK);
                            try (ServletTarget target = new ServletTarget(request, response)) {
                                restFormatter.format(
                                    target,
                                    toObjectRecord(output.get(0)),
                                    serializeNulls
                                );
                            }
                        }
                    }
                } else {
                    if (":api".equals(xri.getLastSegment().toClassicRepresentation())) {
                        final IndexedRecord output = (IndexedRecord) interaction.execute(
                            get,
                            Facades.newQuery(xri.getParent()).getDelegate()
                        );
                        if (output == null) {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        } else if (output.isEmpty()) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        } else {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            try (final PrintWriter pw = response.getWriter()) {
                                final String basePath = request.getContextPath() + request.getServletPath().replace("/:api", "");
                                final String host;
                                {
                                    String requestURL = request.getRequestURL().toString();
                                    int pos1 = requestURL.indexOf("://");
                                    int pos2 = requestURL.indexOf("/", pos1 + 3);
                                    if (pos2 > pos1) {
                                        host = requestURL.substring(pos1 + 3, pos2);
                                    } else {
                                        host = null;
                                    }
                                }
                                final ObjectRecord object = this.toObjectRecord(output.get(0));
                                new Swagger(
                                    model.getElement(object.getValue().getRecordName())
                                ).writeAPI(
                                    pw,
                                    host,
                                    basePath,
                                    object.getResourceIdentifier().toXRI()
                                );
                            }
                        }
                    } else if (":api-ui".equals(xri.getLastSegment().toClassicRepresentation())) {
                        response.sendRedirect(
                            request.getContextPath() +
                                "/api-ui/index.html?url=" + request.getRequestURL().toString().replace(":api-ui", ":api")
                        );
                    } else {
                        // Object query
                        final MappedRecord input;
                        if (request.getHeader("interaction-verb") == null) {
                            final Query_2Facade inputFacade = Facades.newQuery(xri);
                            final String queryType = request.getParameter("queryType");
                            if (queryType == null) {
                                inputFacade.setQueryType(Model_1Factory.getModel().getTypes(xri.getChild(":*"))[2].getQualifiedName());
                            } else {
                                inputFacade.setQueryType(queryType);
                            }
                            final String query = request.getParameter("query");
                            if (query != null) {
                                inputFacade.setQuery(query);
                            }
                            final String position = request.getParameter("position");
                            if (position == null) {
                                inputFacade.setPosition(Integer.valueOf(DEFAULT_POSITION));
                            } else {
                                inputFacade.setPosition(Integer.valueOf(position));
                            }
                            final String size = request.getParameter("size");
                            if (size == null) {
                                inputFacade.setSize(Integer.valueOf(DEFAULT_SIZE));
                            } else {
                                inputFacade.setSize(Integer.valueOf(size));
                            }
                            final String[] groups = request.getParameterValues("groups");
                            if(groups == null || groups.length == 0) {
                                // fetchGroupName=ALL as default
                                inputFacade.setFetchGroupName(FetchGroup.ALL);
                            } else if(groups.length == 1) {
                                inputFacade.setFetchGroupName(
                                    this.rewriteFetchGroup(
                                        request,
                                        groups[0]
                                    )
                                );
                            } else {
                                throw new RuntimeServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.NOT_SUPPORTED,
                                    "At most one fetch group may be specified",
                                    new BasicException.Parameter("groups", (Object[]) groups)
                                );
                            }
                            input = inputFacade.getDelegate();
                        } else {
                            input = parseRequest(request, response);
                        }
                        final IndexedRecord output = (IndexedRecord) interaction.execute(
                            get,
                            input
                        );
                        if (output == null) {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        } else {
                            response.setStatus(HttpServletResponse.SC_OK);
                            try (ServletTarget target = new ServletTarget(request, response)) {
                                restFormatter.format(
                                    target,
                                    xri,
                                    output,
                                    serializeNulls
                                );
                            }
                        }
                    }
                }
            } catch (ResourceException exception) {
                this.handleException(
                    request,
                    response,
                    exception
                );
            } catch (Exception exception) {
                this.handleException(
                    request,
                    response, exception
                );
            } finally {
                try {
                    if (this.isAutoCommitting(request)) {
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
     * @param request
     *            the HTTP Request
     * @param response
     *            the HTTP Response
     * 
     * @exception ServletException
     * @exception IOException
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(
        final HttpServletRequest request,
        final HttpServletResponse response
    )
        throws ServletException,
        IOException {
        prolog(request, response);
        final Path xri = this.getXri(request);
        if (isConnectionObjectIdentifier(xri)) {
            final HttpSession session = request.getSession(true);
            Connection connection = (Connection) session.getAttribute(Connection.class.getName());
            if (connection == null) {
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
            final Interaction interaction = getInteraction(request);
            final boolean autoCommitting = this.isAutoCommitting(request);
            final boolean serializeNulls = this.isSerializeNulls(request);
            try {
                final MappedRecord value = parseRequest(request, response);
                if (org.openmdx.base.rest.spi.ObjectRecord.isCompatible(value)) {
                    final MappedRecord input;
                    if (xri.equals(Object_2Facade.getPath(value))) {
                        input = value;
                    } else {
                        final RecordFactory factory = this.connectionFactory.getRecordFactory();
                        if (factory instanceof ExtendedRecordFactory) {
                            input = ((ExtendedRecordFactory) factory).singletonMappedRecord(
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
                    final IndexedRecord output = (IndexedRecord) execute(
                        interaction,
                        !isTransactionObjectIdentifier(xri) && autoCommitting,
                        true,
                        InteractionSpecs.getRestInteractionSpecs(isRetainValues(request)).CREATE,
                        input
                    );
                    if (output == null || output.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    } else {
                        response.setStatus(HttpServletResponse.SC_OK);
                        try (final ServletTarget target = new ServletTarget(request, response)) {
                            for (final Object record : output) {
                                restFormatter.format(
                                    target,
                                    toObjectRecord(record),
                                    serializeNulls
                                );
                            }
                        }
                    }
                } else if (org.openmdx.base.rest.spi.QueryRecord.isCompatible(value)) {
                    QueryRecord query = (QueryRecord)value;
                    final boolean multivalued;
                    if (xri.size() % 2 == 0) {
                        multivalued = true;
                    } else if (xri.isPattern()) {
                        throw new UnsupportedOperationException("WILDCARD");// TODO
                    } else {
                        multivalued = false;
                    }
                    final InteractionSpec get = InteractionSpecs.getRestInteractionSpecs(
                        !autoCommitting && this.isRetainValues(request)
                    ).GET;
                    query.setFetchGroupName(
                        this.rewriteFetchGroup(
                            request,
                            query.getFetchGroupName()
                        )
                    );
                    final IndexedRecord output = (IndexedRecord) interaction.execute(
                        get,
                        query
                    );
                    if (output == null) {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    } else if (multivalued) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        try (ServletTarget target = new ServletTarget(request, response)) {
                            restFormatter.format(
                                target,
                                xri,
                                output,
                                serializeNulls
                            );
                        }
                    } else if (output.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    } else {
                        response.setStatus(HttpServletResponse.SC_OK);
                        try (final ServletTarget target = new ServletTarget(request, response)) {
                            restFormatter.format(
                                target,
                                toObjectRecord(output.get(0)),
                                serializeNulls
                            );
                        }
                    }
                } else {
                    final MappedRecord input = this.connectionFactory.getRecordFactory().createMappedRecord(MessageRecord.NAME);
                    if (input instanceof MessageRecord) {
                        ((MessageRecord) input).setResourceIdentifier(xri);
                        ((MessageRecord) input).setBody(value);
                    } else {
                        input.put("path", xri);
                        input.put("body", value);
                    }
                    final MappedRecord output = (MappedRecord) execute(
                        interaction,
                        !isTransactionCommitIdentifier(xri) && autoCommitting,
                        false,
                        InteractionSpecs.getRestInteractionSpecs(true).INVOKE,
                        input
                    );
                    if (output == null || output.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    } else {
                        final MessageRecord reply;
                        if (output instanceof MessageRecord) {
                            reply = (MessageRecord) output;
                        } else {
                            reply = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
                            reply.setResourceIdentifier(xri);
                            reply.setBody(value);
                        }
                        response.setStatus(HttpServletResponse.SC_OK);
                        try (final ServletTarget target = new ServletTarget(request, response)) {
                            restFormatter.format(
                                target,
                                "result",
                                reply,
                                serializeNulls
                            );
                        }
                    }
                }
            } catch (ResourceException exception) {
                this.handleException(
                    request,
                    response,
                    exception
                );
            } catch (Exception exception) {
                this.handleException(
                    request,
                    response, exception
                );
            } finally {
                try {
                    if (autoCommitting) {
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
     * @param request
     *            the HTTP Request
     * @param response
     *            the HTTP Response
     * 
     * @exception ServletException
     * @exception IOException
     */
    @Override
    protected void doPut(
        final HttpServletRequest request,
        final HttpServletResponse response
    )
        throws ServletException,
        IOException {
        prolog(request, response);
        final Interaction interaction = getInteraction(request);
        try {
            final MappedRecord input = parseRequest(request, response);
            final boolean serializeNulls = this.isSerializeNulls(request);
            final IndexedRecord output = (IndexedRecord) execute(
                interaction,
                isAutoCommitting(request),
                true,
                this.getInteractionSpec(request, RestFunction.PUT),
                input
            );
            if (output == null || output.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                try (final ServletTarget target = new ServletTarget(request, response)) {
                    for (Object record : output) {
                        restFormatter.format(
                            target,
                            toObjectRecord(record),
                            serializeNulls
                        );
                    }
                }
            }
        } catch (ResourceException exception) {
            this.handleException(
                request,
                response,
                exception
            );
        } catch (Exception exception) {
            this.handleException(
                request,
                response, exception
            );
        } finally {
            try {
                if (this.isAutoCommitting(request)) {
                    final Connection connection = interaction.getConnection();
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
        final HttpServletRequest request,
        final HttpServletResponse response
    )
        throws CommException {
        final Path xri = this.getXri(request);
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
                        new BasicException.Parameter(BasicException.Parameter.XRI, xri)
                    )
                )
            );
        }
    }

    private ObjectRecord toObjectRecord(
        final Object record
    )
        throws ServiceException {
        if (org.openmdx.base.rest.spi.ObjectRecord.isCompatible((Record) record)) {
            return (ObjectRecord) record;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Unexpected record type",
                new BasicException.Parameter("expected", org.openmdx.base.rest.cci.ObjectRecord.NAME),
                new BasicException.Parameter("actual", ((Record) record).getRecordName())
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
        final HttpServletRequest request,
        final HttpServletResponse response
    )
        throws IOException {
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
        final HttpServletRequest request,
        final HttpServletResponse response
    ) {
        //
        // Use accept headers
        //
        final String characterEncoding = new HttpHeaderFieldValue(
            request.getHeaders("Accept-Charset")
        ).getPreferredContent(
            DEFAULT_CHARACTER_ENCODING
        ).getValue();
        final HttpHeaderFieldValue acceptType = new HttpHeaderFieldValue(request.getHeaders("Accept"));
        for (final HttpHeaderFieldContent candidate : acceptType) {
            final String mimeType = candidate.getValue();
            if (XMLOutputFactories.isSupported(mimeType)) {
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
        final String contentType = request.getContentType();
        if (contentType != null) {
            final HttpHeaderFieldContent requestType = new HttpHeaderFieldContent(contentType);
            final String mimeType = requestType.getValue();
            if (XMLOutputFactories.isSupported(mimeType)) {
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
        final HttpServletResponse response,
        final String mimeType,
        final String characterEncoding
    ) {
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
        final HttpServletRequest request
    )
        throws IOException {
        final String query = request.getQueryString();
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
        final HttpServletRequest request
    )
        throws IOException {
        final String query = getQueryString(request);
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
        final HttpServletRequest request
    )
        throws IOException {
        final String query = request.getQueryString(); // getQueryString(request);
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
        final HttpServletRequest request
    ) {
        final String method = request.getMethod();
        return "GET".equals(method) || "DELETE".equals(method);
    }

    /**
     * Retrieve the base URL
     * 
     * @return the base URL
     */
    protected static String getBase(
        final HttpServletRequest request
    ) {
        final String contextPath = request.getContextPath();
        final StringBuffer url = request.getRequestURL();
        return url.substring(
            0,
            url.indexOf(contextPath) + contextPath.length()
        );
    }

    /**
     * Provide a <code>parse()</code> source
     * 
     * @param request
     *            the HTTP request
     * @param response
     *            the HTTP response
     * 
     * @return a <code>Source</code>
     * 
     * @throws ServiceException
     */
    private static RestSource getSource(
        final HttpServletRequest request,
        final HttpServletResponse response
    )
        throws SAXException {
        try {
            final HttpHeaderFieldValue contentHeader = new HttpHeaderFieldValue(
                request.getHeaders("Content-Type")
            );
            final boolean query = contentHeader.isEmpty() && useQueryAsSource(request);
            final HttpHeaderFieldContent contentType = query ? new HttpHeaderFieldContent(
                response.getContentType()
            )
                : contentHeader.getPreferredContent(
                    "application/xml;charset=UTF-8"
                );
            final String mimeType = contentType.getValue();
            final String encoding = contentType.getParameterValue("charset", null);
            final InputSource inputSource = RestFormatters.isBinary(mimeType) ? new InputSource(
                query ? getQueryInputStream(request) : request.getInputStream()
            )
                : new InputSource(
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
            final HttpServletRequest request,
            final HttpServletResponse response
        ) {
            super(RestServlet_2.getBase(request));
            this.response = response;
        }

        /**
         * The underlying HTTP response
         */
        private final HttpServletResponse response;

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.application.rest.http.RestFormat.Target#newWriter()
         */
        @Override
        protected XMLStreamWriter newWriter()
            throws XMLStreamException {
            try {
                final HttpHeaderFieldContent contentType = new HttpHeaderFieldContent(this.response.getContentType());
                final String mimeType = contentType.getValue();
                final XMLOutputFactory xmlOutputFactory = restFormatter.getOutputFactory(mimeType);
                if ((RestFormatters.isBinary(mimeType))) {
                    String characterEncoding = contentType.getParameterValue(
                        "charset",
                        this.response.getCharacterEncoding()
                    );
                    return characterEncoding == null ? xmlOutputFactory.createXMLStreamWriter(
                        this.response.getOutputStream()
                    )
                        : xmlOutputFactory.createXMLStreamWriter(
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