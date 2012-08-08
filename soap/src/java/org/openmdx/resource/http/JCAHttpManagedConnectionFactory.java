/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: JCAHttpManagedConnectionFactory.java,v 1.1 2007/03/22 15:32:52 wfro Exp $
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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JCA 1.5: This a part of the implementation of the Connection Management
 * contract. Used by the ConnectionManager( J2EE Container) to create and manage
 * many physical connections. The ConnectionManager is responsible for pooling
 * and managing these connections.
 */
public class JCAHttpManagedConnectionFactory implements
		ManagedConnectionFactory, ResourceAdapterAssociation {
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = 3030014058849603486L;

	/** Logger. * */
	private static final Log LOG = LogFactory
			.getLog(JCAHttpManagedConnectionFactory.class);

	/**
	 * JCA1.5.
	 */
	private PrintWriter logWriter = null;

	/**
	 * A reference to the resourceAdapter responsible for this factory.
	 */
	private ResourceAdapter resourceAdapter = null;

	/**
	 * Contains physical connections that are managed by this factory.
	 * <ul>
	 * <li>Key:</li>
	 * <li>Value:</li>
	 * </ul>
	 */
	private Map connections = new HashMap();

	/**
	 * JavaBean: Default URL to use when opening a new connection.
	 */
	private String url = null;

	/**
	 * JavaBean: Default factoryName to use when opening a new connection.
	 */
	private String factoryName = null;

	/**
	 * Getter for the default URL used to open a new connection.
	 * 
	 * @return default url
	 */
	public final String getUrl() {
		return url;
	}

	/**
	 * Setter for the default URL used to open a new connection.
	 * 
	 * @param newUrl
	 *            the new segment name
	 */
	public final void setUrl(final String newUrl) {
		this.url = newUrl;
	}

	/**
	 * @return the factoryName
	 */
	public final String getFactoryName() {
		return factoryName;
	}

	/**
	 * @param factoryName
	 *            the factoryName to set
	 */
	public final void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	/**
	 * Default constructor.
	 */
	public JCAHttpManagedConnectionFactory() {
	}

	/**
	 * JCA 1.5: Creates new Connection Factories to create connection handles
	 * bound to physical connections.
	 * 
	 * @param connManager
	 *            the connection manager of the server
	 * @return returns a new connection factory
	 * @throws ResourceException
	 *             error during creation of the ConnectionFactory
	 * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
	 */
	public synchronized Object createConnectionFactory(
			final ConnectionManager connManager) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("createConnectionFactory(" + connManager + ")");
		}
		JCAHttpConnectionFactory newFactory = new JCAHttpConnectionFactory(
				connManager, this);
		return newFactory;
	}

	/**
	 * JCA 1.5: Creates new Connection Factories to create connection handles
	 * bound to physical connections.
	 * 
	 * @return a new connection factory without any initialisation parameters
	 * @throws ResourceException
	 *             error during creation of the ConnectionFactory
	 * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory()
	 */
	public final Object createConnectionFactory() throws ResourceException {
		LOG.debug("createConnectionFactory()");
		return this.createConnectionFactory(null);
	}

	/**
	 * JCA 1.5 : The ConnectionManager (J2EE Container) uses this callback to
	 * ask for the creation of new physical connections.
	 * 
	 * @param subject
	 *            The user openning a connection
	 * @param connRequestInfo
	 *            info needed to pen the connection
	 * @return the new managed connection to opencrx
	 * @throws ResourceException
	 *             error during connection initialisation
	 * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject,
	 *      javax.resource.spi.ConnectionRequestInfo)
	 */
	public synchronized ManagedConnection createManagedConnection(
			final Subject subject, ConnectionRequestInfo connRequestInfo)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("createManagedConnection(" + connRequestInfo + ")");
		}
		if (connRequestInfo == null) {
			connRequestInfo = new HttpConnectionRequestInfo(this.getUrl(), this
					.getFactoryName());
		} else {
			HttpConnectionRequestInfo info = (HttpConnectionRequestInfo) connRequestInfo;
			if (info.getUrl() == null || "".equals(info.getUrl())) {
				info.setUrl(this.getUrl());
			}
			if (info.getFactoryName() == null
					|| "".equals(info.getFactoryName())) {
				info.setFactoryName(this.getFactoryName());
			}
		}
		JCAHttpManagedConnection newManagedConnection = new JCAHttpManagedConnection(
				subject, connRequestInfo);
		newManagedConnection.setLogWriter(getLogWriter());
		getConnections().put(connRequestInfo, newManagedConnection);
		return newManagedConnection;
	}

	/**
	 * JCA 1.5: The ConnectionManager (J2EE Container) uses this callback to
	 * know if a connection already exists in its pool. Its job is to manage the
	 * pool. It has to ask the ResourceAdapter to identify connections.
	 * 
	 * @param connectionsToMatch
	 *            set of collection to check
	 * @param connection
	 *            info on wich match the connections
	 * @param principal
	 *            user of the connections
	 * @return the conenction contained in connectionsToMatch that matches with
	 *         connection.
	 * @throws ResourceException
	 *             error during tests
	 * @see javax.resource.spi.ManagedConnectionFactory#matchManagedConnections(java.util.Set,
	 *      javax.security.auth.Subject,
	 *      javax.resource.spi.ConnectionRequestInfo)
	 */
	public final ManagedConnection matchManagedConnections(
			final Set connectionsToMatch, final Subject principal,
			final ConnectionRequestInfo connection) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("matchManagedConnections");
		}
		Iterator it = connectionsToMatch.iterator();
		JCAHttpManagedConnection mc = null;
		boolean sortie = false;
		while (it.hasNext() && !sortie) {
			Object obj = it.next();
			if (obj instanceof JCAHttpManagedConnection) {
				mc = (JCAHttpManagedConnection) obj;
				if (connection instanceof HttpConnectionRequestInfo) {
					if (mc.correspondTo((HttpConnectionRequestInfo) connection)) {
						sortie = true;
					}
				}
			}
		}
		return mc;
	}

	/**
	 * JCA 1.5.
	 * 
	 * @param writer
	 *            the writer of the instance
	 * @throws ResourceException
	 *             never thrown
	 * @see javax.resource.spi.ManagedConnectionFactory#setLogWriter(java.io.PrintWriter)
	 */
	public final void setLogWriter(final PrintWriter writer)
			throws ResourceException {
		logWriter = writer;
	}

	/**
	 * JCA 1.5.
	 * 
	 * @return the log writer
	 * @throws ResourceException
	 *             never thrown
	 * @see javax.resource.spi.ManagedConnectionFactory#getLogWriter()
	 */
	public final PrintWriter getLogWriter() throws ResourceException {
		return logWriter;
	}

	/**
	 * JCA 1.5 :get the resourceadapter.
	 * 
	 * @return the resource adapter corresponding to this factory
	 * @see javax.resource.spi.ResourceAdapterAssociation#getResourceAdapter()
	 */
	public final ResourceAdapter getResourceAdapter() {
		return resourceAdapter;
	}

	/**
	 * @param adapter
	 *            the resource adapter corresponding to the factory
	 * @throws ResourceException
	 *             never thrown
	 * @see javax.resource.spi.ResourceAdapterAssociation#setResourceAdapter(javax.resource.spi.ResourceAdapter)
	 */
	public final void setResourceAdapter(final ResourceAdapter adapter)
			throws ResourceException {
		resourceAdapter = adapter;
	}

	/**
	 * identifies an instance of ManagedConnectionFactory.
	 */
	private static int counter;

	/**
	 * Unique identifier of the instance.
	 */
	private int id;

	/**
	 * Lock used to calculate new identifier.
	 */
	private static final Object LOCK = new Object();
	{
		synchronized (LOCK) {
			id = counter++;
		}
	}

	/**
	 * @return id.
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		return id;
	}

	/**
	 * @return id
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		return this.getClass() + "[" + id + "]";
	}

	/**
	 * Closes physical connections.
	 * 
	 * @param info
	 *            the infos concerning the connection to close.
	 * @return true if its ok.
	 * @throws ResourceException
	 */
	public final synchronized boolean closeManagedConnection(
			final HttpConnectionRequestInfo info) throws ResourceException {
		JCAHttpManagedConnection connection = (JCAHttpManagedConnection) this
				.getConnections().get(info);
		connection.close();
		getConnections().remove(info);
		return true;
	}

	protected Map getConnections() {
		return connections;
	}

	protected int getId() {
		return id;
	}

	public final boolean equals(final Object obj) {
		boolean resultBool = false;
		if (obj instanceof JCAHttpManagedConnectionFactory) {
			if (obj != null) {
				resultBool = this.getId() == ((JCAHttpManagedConnectionFactory) obj)
						.getId();
			}
		}
		return resultBool;
	}
}
