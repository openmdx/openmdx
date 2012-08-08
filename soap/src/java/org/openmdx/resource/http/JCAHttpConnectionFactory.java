/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: JCAHttpConnectionFactory.java,v 1.1 2007/03/22 15:32:52 wfro Exp $
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

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;

import org.openmdx.base.resource.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCA 1.5 (CCI): Used to create handles to physical connections. This factory
 * is obtained from a JNDI lookup. It uses the ConnectionManager (J2EE
 * container) to create new physical connections managed in a pool by the
 * container.
 */
public class JCAHttpConnectionFactory implements ConnectionFactory,
		Serializable {

	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = 7888025370782443992L;

	/** Logger. * */
	private static final Logger LOG = LoggerFactory
			.getLogger(JCAHttpConnectionFactory.class);

	/**
	 * The ConnectionManager (J2EE Container) is responsible for managing a pool
	 * of connections, and to handle services like transaction and security.
	 */
	private ConnectionManager connectionManager = null;

	/**
	 * Real factory to be used by the ConnectionManager (J2EE Container) to
	 * create new physical connections.
	 */
	private JCAHttpManagedConnectionFactory managedConnectionFactory = null;

	/**
	 * JCA 1.5.
	 */
	private Reference reference = null;

	/**
	 * To create a new handle factory.
	 * 
	 * @param connManager
	 *            J2EE containers's ConnectionManager.
	 * @param factory
	 *            factory of physical connections to opencrx
	 */
	public JCAHttpConnectionFactory(ConnectionManager connManager,
			JCAHttpManagedConnectionFactory factory) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("new PoXConnectionFactory(" + connManager + "," + factory
					+ ")");
		}
		this.connectionManager = connManager;
		this.managedConnectionFactory = factory;
	}

	/**
	 * To get a handle and to ask the ConnectionManager (J2EE Container) for a
	 * new (possibly one in the pool) physical connection.
	 * 
	 * @return a new connection initialized with default parameters.
	 * @throws ResourceException
	 *             errors during allocation of a new connection
	 * @see javax.resource.cci.ConnectionFactory#getConnection()
	 */
	public Connection getConnection() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getConnection()");
		}
		Connection connection = null;
		if (connectionManager != null) {
			connection = (Connection) connectionManager.allocateConnection(
					managedConnectionFactory, null);
		} else {
			connection = new JCAHttpConnection(
					(JCAHttpManagedConnection) this.managedConnectionFactory
							.createManagedConnection(null, null));
		}
		return connection;
	}

	/**
	 * To get a handle and to ask the ConnectionManager (J2EE Container) for a
	 * new (possibly one in the pool) physical connection.
	 * 
	 * @param spec
	 *            Information needed to create a new connection.
	 * @return a new connection initialized with parameters.
	 * @throws ResourceException
	 *             errors during allocation of a new connection
	 * @see javax.resource.cci.ConnectionFactory#getConnection(javax.resource.cci.ConnectionSpec)
	 */
	public Connection getConnection(ConnectionSpec spec)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getConnection(" + spec + ")");
		}
		Connection connection = null;
		if (connectionManager != null) {
			connection = (Connection) connectionManager.allocateConnection(
					managedConnectionFactory, new HttpConnectionRequestInfo(
							((HttpConnectionSpec) spec).getUrl(),
							((HttpConnectionSpec) spec).getFactoryName()));
		} else {
			connection = new JCAHttpConnection(
					(JCAHttpManagedConnection) this.managedConnectionFactory
							.createManagedConnection(null,
									new HttpConnectionRequestInfo(
											((HttpConnectionSpec) spec)
													.getUrl(),
											((HttpConnectionSpec) spec)
													.getFactoryName())));
		}
		return connection;
	}

	/**
	 * JCA 1.5 (CCI): To get a RecordFactory. This factory creates new Records
	 * that should be used in a CCI way to manage data to be sent to the
	 * connection.
	 * 
	 * @return a new COnnection Factory, specific to PoX connections.
	 * @throws error
	 *             occurs during initialization of the factory
	 * @see javax.resource.cci.ConnectionFactory#getRecordFactory()
	 */
	public RecordFactory getRecordFactory() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getRecordFactory");
		}
		return Records.getRecordFactory();
	}

	/**
	 * JCA 1.5 :To get static information about the ResourceAdapter.
	 * 
	 * @see javax.resource.cci.ConnectionFactory#getMetaData()
	 */
	public ResourceAdapterMetaData getMetaData() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getMetaData");
		}
		return new JCAHttpResourceAdapterMetaData(
				(JCAHttpResourceAdapter) managedConnectionFactory
						.getResourceAdapter());
	}

	/**
	 * JCA 1.5. :
	 * 
	 * @see javax.resource.Referenceable#setReference(javax.naming.Reference)
	 */
	public void setReference(Reference newReference) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("setReference()");
		}
		this.reference = newReference;
	}

	/**
	 * JCA 1.5. :
	 * 
	 * @see javax.naming.Referenceable#getReference()
	 */
	public Reference getReference() throws NamingException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getReference()");
		}
		return reference;
	}

	/**
	 * Identifies an instance of a PoXConnectionFactory.
	 */
	private static int counter;

	/**
	 * Id of the factory.
	 */
	private int id;

	/**
	 * LOCK on the object.
	 */
	private static final Object LOCK = new Object();
	{
		synchronized (LOCK) {
			id = counter++;
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		boolean resultBool = false;
		if (obj instanceof JCAHttpConnectionFactory) {
			if (obj != null) {
				resultBool = this.id == ((JCAHttpConnectionFactory) obj).id;
			}
		}
		return resultBool;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return id;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.getClass() + "[" + id + "]";
	}

	protected ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	protected JCAHttpManagedConnectionFactory getManagedConnectionFactory() {
		return managedConnectionFactory;
	}

}
