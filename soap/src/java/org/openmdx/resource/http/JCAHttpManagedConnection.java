/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: JCAHttpManagedConnection.java,v 1.1 2007/03/22 15:32:52 wfro Exp $
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/22 15:32:52 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland; France Telecom, France
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * 
 */
package org.openmdx.resource.http;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.openmdx.kernel.exception.BasicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCA 1.5 : Part of the Connection Management implementation.
 * AbstractHttpManagedConnections are physical conections to the Server. Clients
 * of the RessourceAdapter use handles(AbstractHttpConnection) to manage those
 * physical connections.
 */
public class JCAHttpManagedConnection implements ManagedConnection {
	/** Logger. * */
	private static final Logger LOG = LoggerFactory
			.getLogger(JCAHttpManagedConnection.class);

	private Map<JCAHttpConnection, ConnectionHandles> connectionHandles = new HashMap<JCAHttpConnection, ConnectionHandles>();

	// TODO initialise the factory
	private ConnectorObjectMarshallerFactory marshallerFactory;

	/**
	 * JCA 1.5.
	 */
	private PrintWriter logWriter;

	/**
	 * Subject used to connect.
	 */
	private Subject subject;

	/**
	 * Contains all the information needed to create a new connection to the
	 * Server.
	 */
	private HttpConnectionRequestInfo info;

	/**
	 * JCA 1.5: listeners for event in the lifecycle of the connection. Used by
	 * the ConnectionManager (J2EE Container) to manage his connection pool.
	 */
	private ArrayList connectionEventListeners = new ArrayList();

	/**
	 * All information needed to identify the connection and the real Server
	 * connection.
	 * 
	 * @param newSubject
	 *            the user that logs on the Server(JCA 1.5)
	 * @param newInfo
	 *            informations related to this connection
	 * @throws ResourceException
	 */
	public JCAHttpManagedConnection(final Subject newSubject,
			final ConnectionRequestInfo newInfo) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("new AbstractOpenMDXManagedConnection(" + info + ")");
		}
		this.subject = newSubject;
		this.info = (HttpConnectionRequestInfo) newInfo;
		this.marshallerFactory = MarshallerMetaFactory.getFacory(this.info
				.getFactoryName());
	}

	/**
	 * TO get a handle (AbstractHttpConnection) on this physical connection.
	 * 
	 * @param connectionInfo
	 *            info reuired to create a new connection handle
	 * @param newSubject
	 *            the subject to use
	 * @return A new connection handled by this managed connection
	 * @throws ResourceException
	 *             error during creation of the connection
	 * @see javax.resource.spi.ManagedConnection#getConnection(javax.security.auth.Subject,
	 *      javax.resource.spi.ConnectionRequestInfo)
	 */
	public Object getConnection(final Subject newSubject,
			final ConnectionRequestInfo connectionInfo)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getConnection(" + newSubject + "," + connectionInfo
					+ ")");
		}
		JCAHttpConnection connection = new JCAHttpConnection(this);
		connectionHandles.put(connection, null);
		return connection;
	}

	/**
	 * @throws ResourceException
	 * @see javax.resource.spi.ManagedConnection#destroy()
	 */
	public final void destroy() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("destroy");
		}
		this.close();
	}

	/**
	 * JCA 1.5 : close the physical connection.
	 * 
	 * @throws ResourceException
	 *             if an error occurs during connection close
	 * @see javax.resource.spi.ManagedConnection#cleanup()
	 */
	public final void cleanup() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("cleanup()");
		}
		this.close();
	}

	/**
	 * JCA 1.5 :add a new handle (AbstractHttpConnection) to this physical
	 * connection.
	 * 
	 * @param connection
	 *            the new handle
	 * @throws ResourceException
	 *             if the connection is of the wrong type
	 * @see javax.resource.spi.ManagedConnection#associateConnection(java.lang.Object)
	 */
	public final void associateConnection(final Object connection)
			throws ResourceException {
		if (connection instanceof JCAHttpConnection) {
			JCAHttpConnection cciCon = (JCAHttpConnection) connection;
			cciCon.setConnection(this);
			connectionHandles.put(cciCon, null);
		} else {
			throw new IllegalStateException("Invalid connection object: "
					+ connection);
		}
	}

	/**
	 * JCA 1.5: listener used by the container to manage his connection pool.
	 * 
	 * @param eventListener
	 *            the new listener to add
	 * @see javax.resource.spi.ManagedConnection#addConnectionEventListener(javax.resource.spi.ConnectionEventListener)
	 */
	public final void addConnectionEventListener(
			final ConnectionEventListener eventListener) {
		connectionEventListeners.add(eventListener);
	}

	/**
	 * JCA 1.5: listener used by the container to manage his connection pool.
	 * 
	 * @param eventListener
	 *            listener to remove
	 * @see javax.resource.spi.ManagedConnection#removeConnectionEventListener(javax.resource.spi.ConnectionEventListener)
	 */
	public final void removeConnectionEventListener(
			final ConnectionEventListener eventListener) {
		connectionEventListeners.remove(eventListener);
	}

	/**
	 * JCA 1.5 : This ResourceAdapter does not implements the Transaction
	 * Management Contract. It throws a new NotSupportedException.
	 * 
	 * @return never returns anything
	 * @throws ResourceException
	 *             always throws a NotSupportedException
	 * @see javax.resource.spi.ManagedConnection#getXAResource()
	 */
	public final XAResource getXAResource() throws ResourceException {
		throw new NotSupportedException("Operation not supported");
	}

	/**
	 * JCA 1.5 : This ResourceAdapter does not implements the Transaction
	 * Management Contract. It throws a new NotSupportedException. *
	 * 
	 * @return never returns anything
	 * @throws ResourceException
	 *             always throws a NotSupportedException
	 * @see javax.resource.spi.ManagedConnection#getLocalTransaction()
	 */
	public final LocalTransaction getLocalTransaction()
			throws ResourceException {
		throw new NotSupportedException("Operation not supported");
	}

	/**
	 * JCA 1.5 : (Container's Callback).
	 * 
	 * @return a class conatining meta data information about this conenction
	 * @throws ResourceException
	 *             error durng creation of the meta data object
	 * @see javax.resource.spi.ManagedConnection#getMetaData()
	 */
	public final ManagedConnectionMetaData getMetaData()
			throws ResourceException {
		return new HttpManagedConnectionMetaData(this);
	}

	/**
	 * JCA 1.5 : (Container's Callback).
	 * 
	 * @param writer
	 *            the new log writer
	 * @throws ResourceException
	 *             never throws any exception
	 * @see javax.resource.spi.ManagedConnection#setLogWriter(java.io.PrintWriter)
	 */
	public final void setLogWriter(final PrintWriter writer)
			throws ResourceException {
		logWriter = writer;
	}

	/**
	 * JCA 1.5 : Container's Callback.
	 * 
	 * @return the printwriter of this object
	 * @throws ResourceException
	 *             never throws any exception
	 * @see javax.resource.spi.ManagedConnection#getLogWriter()
	 */
	public final PrintWriter getLogWriter() throws ResourceException {
		return logWriter;
	}

	/**
	 * Returns the principal name who opened the connection.
	 * 
	 * @return a login
	 */
	public final String getUserName() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getUserName()");
		}
		Iterator ite = subject.getPrincipals().iterator();
		String result = "nobody";
		if (ite.hasNext()) {
			result = ((Principal) ite.next()).getName();
		}
		return result;
	}

	/**
	 * Called when a handle is closed (does not close the physical connection).
	 * 
	 * @param conn
	 *            connection to close
	 * @throws ResourceException
	 */
	public final void close(final JCAHttpConnection conn)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("close(" + conn + ")");
		}
		try {
			ConnectionHandles handle = this.connectionHandles.get(conn);
			if (handle != null) {
				if (handle.getConnection().getInputStream() != null) {
					handle.getConnection().getInputStream().close();
				}
			}
			ConnectionEvent ce = new ConnectionEvent(this,
					ConnectionEvent.CONNECTION_CLOSED);
			ce.setConnectionHandle(conn);

			for (Iterator ite = connectionEventListeners.iterator(); ite
					.hasNext(); ((ConnectionEventListener) ite.next())
					.connectionClosed(ce)) {
			}
		} catch (Exception e) {
			LOG.warn("Error on close : ", e);
		}
	}

	/**
	 * identifies an instance of a ManagedConnection.
	 */
	private static int counter;

	/**
	 * Unique id of the instance.
	 */
	private int id;

	/**
	 * Lock used to get unique id for instances.
	 */
	private static final Object LOCK = new Object();
	{
		synchronized (LOCK) {
			id = counter++;
		}
	}

	@Override
	public final boolean equals(final Object obj) {
		boolean resultBool = false;
		if (obj instanceof JCAHttpManagedConnection) {
			if (obj != null) {
				resultBool = this.getId() == ((JCAHttpManagedConnection) obj)
						.getId();
			}
		}
		return resultBool;
	}

	@Override
	public int hashCode() {
		return this.getId();
	}

	/**
	 * @return Returns the principal.
	 */
	public final Subject getSubject() {
		return subject;
	}

	/**
	 * @param newSubject
	 *            The principal to set.
	 */
	public final void setSubject(final Subject newSubject) {
		this.subject = newSubject;
	}

	/**
	 * Calls close on the connection.
	 * 
	 * @throws ResourceException
	 * 
	 */
	public final void remove() throws ResourceException {
		this.close();
	}

	/**
	 * @throws ResourceException
	 * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection#close()
	 */
	public final void close() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("close()");
		}
		Iterator ite = connectionHandles.keySet().iterator();
		JCAHttpConnection element = null;
		while (ite.hasNext()) {
			element = (JCAHttpConnection) ite.next();
			this.close(element);
		}
		connectionHandles.clear();
	}

	/**
	 * To know if this connection matches the information.
	 * 
	 * @param infoTest
	 *            informations to compare
	 * @return true if ther is a match
	 */
	public final boolean correspondTo(final HttpConnectionRequestInfo infoTest) {
		boolean resultBool = false;
		if (info != null) {
			boolean url = (this.info.getUrl() == null && infoTest.getUrl() == null)
					|| this.info.getUrl().equals(infoTest.getUrl());
			resultBool = url;
		}
		return resultBool;
	}

	/**
	 * Used to send a Message on this HttpConnection, the stream has to be still
	 * available after this method complets.
	 * 
	 * @param connection
	 *            the Handle that wishes to send a message.
	 * @param message
	 *            the message to be sent.
	 * @throws ResourceException
	 */
	public void sendMessage(JCAHttpConnection connection,
			RequestEnvelope envelope) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("sendMessage(" + connection + ", " + envelope + ")");
		}
		try {
			this.getConnectionHandle(connection).getMarshaller().writeObject(
					envelope);
		} catch (Exception e) {
			throw new ResourceException(
					new BasicException(e, BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.COMMUNICATION_FAILURE, this
									.getParameters(),
							"Error when trying to write on the connection to the remote server"));
		}
	}

	/**
	 * This method ends the messages and output stream, it reads the answer from
	 * the server.
	 * 
	 * @param connection
	 *            the handle that wishes to flush the stream.
	 * @return the server response, usualy a stream to be parsed by the handle.
	 * @throws ResourceException
	 */
	public Object flushConnection(JCAHttpConnection connection)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("flushConnection(" + connection + ")");
		}
		ConnectionHandles handle = this.getConnectionHandle(connection);
		try {
			handle.getMarshaller().flush();
			handle.getConnection().getOutputStream().close();

			InputStream in = handle.getConnection().getInputStream();
			handle.getMarshaller().initRead(in, null);
			this.connectionHandles.put(connection, null);
			return handle;
		} catch (Exception e) {
			throw new ResourceException(
					new BasicException(e, BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.COMMUNICATION_FAILURE, this
									.getParameters(),
							"Error when trying to read the connection on the remote server"));
		}
	}

	protected ConnectionHandles getConnectionHandle(JCAHttpConnection connection)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getConnectionHandle(" + connection + ")");
		}
		ConnectionHandles handle = this.connectionHandles.get(connection);
		if (handle == null) {
			try {
				URL url = new URL(this.getInfo().getUrl());
				Authenticator.setDefault(new SimpleAuthenticator());
				URLConnection urlConnection = url.openConnection();
				urlConnection.setDoOutput(true);
				urlConnection.setDoInput(true);
				if (urlConnection instanceof HttpURLConnection) {
					((HttpURLConnection) urlConnection)
							.setRequestMethod("POST");

				}

				ConnectorObjectMarshaller marshaller = marshallerFactory
						.getMarshaller();
				marshaller.initWrite(urlConnection.getOutputStream());
				handle = new ConnectionHandles(marshaller, urlConnection);
				this.connectionHandles.put(connection, handle);
			} catch (Exception e) {
				throw new ResourceException(
						new BasicException(e,
								BasicException.Code.DEFAULT_DOMAIN,
								BasicException.Code.COMMUNICATION_FAILURE, this
										.getParameters(),
								"Error when trying to establish the connection on the remote server"));
			}
		}
		return handle;
	}

	/**
	 * Creates parameter for BasicExceptions.
	 * 
	 * @return the configuration parameters of this connection.
	 */
	public BasicException.Parameter[] getParameters() {
		BasicException.Parameter[] parameters = new BasicException.Parameter[2];
		parameters[0] = new BasicException.Parameter("url", this.info.getUrl());
		PasswordCredential credential = (PasswordCredential) JCAHttpManagedConnection.this.subject
				.getPrivateCredentials().iterator().next();
		parameters[1] = new BasicException.Parameter("principal", credential
				.getUserName());
		return parameters;
	}

	/**
	 * Uses the Subject of this connection to authenticate the connector on the
	 * server.
	 */
	protected class SimpleAuthenticator extends Authenticator {
		public SimpleAuthenticator() {
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			PasswordCredential credential = (PasswordCredential) JCAHttpManagedConnection.this.subject
					.getPrivateCredentials().iterator().next();
			return new PasswordAuthentication(credential.getUserName(),
					credential.getPassword());
		}
	}

	protected HttpConnectionRequestInfo getInfo() {
		return info;
	}

	protected int getId() {
		return id;
	}

	public static final class ConnectionHandles {
		private ConnectorObjectMarshaller marshaller;

		private final URLConnection connection;

		public ConnectionHandles(final ConnectorObjectMarshaller marshaller,
				final URLConnection connection) {
			super();
			this.connection = connection;
			this.marshaller = marshaller;
		}

		public URLConnection getConnection() {
			return connection;
		}

		public ConnectorObjectMarshaller getMarshaller() {
			return marshaller;
		}

	}

}
