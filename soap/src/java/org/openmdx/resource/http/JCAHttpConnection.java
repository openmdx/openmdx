/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: JCAHttpConnection.java,v 1.1 2007/03/22 15:32:52 wfro Exp $
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

import java.util.ArrayList;
import java.util.List;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.Record;
import javax.resource.cci.ResultSetInfo;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.resource.http.JCAHttpManagedConnection.ConnectionHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCA 1.5 : CCI. This is a handle on a real physical connection
 * (AbstractManagedConnection).
 */
public class JCAHttpConnection implements Connection {
	/** Logger. * */
	private static final Logger LOG = LoggerFactory
			.getLogger(JCAHttpConnection.class);

	/**
	 * The physical connection to the Server.
	 */
	private JCAHttpManagedConnection managedConnection = null;

	/**
	 * @throws ResourceException
	 */
	public final void remove() throws ResourceException {
		managedConnection.remove();
	}

	private boolean isClosed = false;

	/**
	 * Used to create a handle to a physical connection.
	 * 
	 * @param newManagedConnection
	 *            the physical connection associated to this handle
	 */
	public JCAHttpConnection(final JCAHttpManagedConnection newManagedConnection) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("new OpenMDXConnection(" + newManagedConnection + ")");
		}
		this.managedConnection = newManagedConnection;
	}

	/**
	 * Set a new physical connection to this handle.
	 * 
	 * @param connection
	 *            set this managed connection
	 */
	public final void setConnection(final JCAHttpManagedConnection connection) {
		this.managedConnection = connection;
	}

	/**
	 * JCA 1.5.
	 * 
	 * @return name of the user of the connection (login).
	 */
	public final String getUser() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getUser()");
		}
		return this.managedConnection.getUserName();
	}

	/**
	 * JCA 1.5: CCI, Interactions are used by clients who want to use this
	 * ResourceAdapter in a generic way.
	 * 
	 * @return a new Interaction
	 * @throws ResourceException
	 *             error during creation
	 * @see javax.resource.cci.Connection#createInteraction()
	 */
	public final Interaction createInteraction() throws ResourceException {
		return new HttpInteraction(this);
	}

	/**
	 * JCA 1.5 : This ResourceAdapter does not implement the Transaction
	 * Management Contract. It throws a new NotSupportedException
	 * 
	 * @return nothing
	 * @throws ResourceException
	 *             Always : not sypported operation
	 * @see javax.resource.cci.Connection#getLocalTransaction()
	 */
	public final LocalTransaction getLocalTransaction()
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getMetaData()");
		}
		throw new NotSupportedException("Pas de transaction local");
	}

	/**
	 * JCA 1.5 : This ResourceAdapter does not implement the Transaction
	 * Management Contract. It throws a new NotSupportedException
	 * 
	 * @return a new ConnectionMetadata relative to this connection
	 * @throws ResourceException
	 *             error during creation of metadata.
	 * @see javax.resource.cci.Connection#getMetaData()
	 */
	public final ConnectionMetaData getMetaData() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getMetaData()");
		}
		return new HttpConnectionMetaData(this);
	}

	/**
	 * JCA 1.5 : new NotSupportedException. *
	 * 
	 * @return nothing
	 * @throws ResourceException
	 *             Always : not sypported operation
	 * @see javax.resource.cci.Connection#getResultSetInfo()
	 */
	public final ResultSetInfo getResultSetInfo() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getResultSetInfo()");
		}
		throw new NotSupportedException("mode not supported!");
	}

	/**
	 * JCA 1.5 : To 'close' this handle, the real connection should'nt be
	 * closed. There may be other handle using it.
	 * 
	 * @throws ResourceException
	 * 
	 * @see javax.resource.cci.Connection#close()
	 */
	public final void close() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("close()");
		}
		if (!isClosed) {
			this.managedConnection.close(this);
			isClosed = true;
		}
	}

	/**
	 * Identifies an instance of AbstractHttpConnection.
	 */
	private static int counter;

	/**
	 * ID of this connection.
	 */
	private int id;

	/**
	 * LOCK on this object.
	 */
	private static final Object LOCK = new Object();
	{
		synchronized (LOCK) {
			id = counter++;
		}
	}

	/**
	 * @return the id of this connection.
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		return id;
	}

	/**
	 * @return the id of this object.
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		String result = this.getClass() + "[" + this.id + "]";
		return result;
	}

	/**
	 * Use the managed connection to send messages, keeping the physical
	 * connection alive.
	 * 
	 * @param spec
	 *            the interaction to send
	 * @param inputRecord
	 *            the interaction's parameters
	 * @throws ResourceException
	 *             If a problem was raised when connecting to the server.
	 */
	public void enqueRequest(InteractionSpec spec, Record inputRecord)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("enqueRequest(" + spec + ", " + inputRecord + " )");
		}
		if (!isClosed) {
			RequestEnvelope envelope = new RequestEnvelope(spec, inputRecord);
			this.getManagedConnection().sendMessage(this, envelope);
		}
	}

	public List<Record> flushConnection() throws ResourceException {
		List<Record> result = new ArrayList<Record>();
		if (!isClosed()) {
			ConnectionHandles response = (ConnectionHandles) this
					.getManagedConnection().flushConnection(this);
			try {
				Record record = null;
				while ((record = (Record) response.getMarshaller().readObject()) != null) {
					result.add(record);
				}
				response.getConnection().getInputStream().close();
			} catch (Exception e) {
				throw new ResourceException(
						new BasicException(e,
								BasicException.Code.DEFAULT_DOMAIN,
								BasicException.Code.COMMUNICATION_FAILURE,
								this.getManagedConnection().getParameters(),
								"Error when trying to read from the connection on the remote server"));

			}
		}
		return result;
	}

	protected int getId() {
		return id;
	}

	protected JCAHttpManagedConnection getManagedConnection() {
		return managedConnection;
	}

	protected boolean isClosed() {
		return isClosed;
	}

}
