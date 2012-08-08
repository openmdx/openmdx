/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: HttpInteraction.java,v 1.2 2007/03/22 15:32:52 wfro Exp $
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.cci.ResourceWarning;

import org.openmdx.base.transport.jca.OpenMdxInteractionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCA 1.5 (CCI) OUTBOUND : To use the Resource Adapter in a generic way. You
 * cannot use a connection without knowing the real method on it. So you should
 * use the Interaction, pass it the information to send and execute it.
 * 
 */
public class HttpInteraction implements Interaction {
	/** Logger. * */
	private static final Logger LOG = LoggerFactory
			.getLogger(HttpInteraction.class);

	private List<Record> enquedRecords = new ArrayList<Record>();

	/**
	 * The handle used by this interaction.
	 */
	private JCAHttpConnection connection = null;

	/**
	 * Interaction Inheritance.
	 */
	private ResourceWarning warnings = new ResourceWarning();

	/**
	 * @param conn
	 *            the handle on which this connection is bound
	 */
	public HttpInteraction(JCAHttpConnection conn) {
		this.connection = conn;
	}

	/**
	 * To close the connection handle.
	 * 
	 * @throws ResourceException
	 *             error occurs during the connection close.
	 * @see javax.resource.cci.Interaction#close()
	 */
	public void close() throws ResourceException {
		this.flushExecutions();
	}

	/**
	 * Getter for the connection handle.
	 * 
	 * @return the conenction bound to this interaction.
	 * @see javax.resource.cci.Interaction#getConnection()
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * JCA 1.5 (CCI): To send a message with the handle.
	 * 
	 * @param interactionSpec
	 *            he specs to use to specifie the type of message to use, here
	 *            there is only on type of message.
	 * @param recordInput
	 *            the record to send
	 * @param recordOutput
	 *            the record that will contain response
	 * @return true if execution is ok
	 * @throws ResourceException
	 *             exception during message sending
	 * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec,
	 *      javax.resource.cci.Record, javax.resource.cci.Record)
	 */
	public boolean execute(InteractionSpec interactionSpec, Record recordInput,
			Record recordOutput) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("execute(" + interactionSpec + ", " + recordInput + ", "
					+ recordOutput + ")");
		}
		boolean resultBool = false;
		resultBool = this.enqueMessage(interactionSpec, recordInput,
				recordOutput);

		if (InteractionSpec.SYNC_SEND_RECEIVE == ((OpenMdxInteractionSpec) interactionSpec)
				.getInteractionVerb()) {
			resultBool = this.flushExecutions();
		}
		return resultBool;
	}

	/**
	 * JCA 1.5 (CCI): To send a message with the handle. *
	 * 
	 * @param interactionSpec
	 *            he specs to use to specifie the type of message to use, here
	 *            there is only on type of message.
	 * @param recordInput
	 *            the record to send
	 * @return server response
	 * @throws ResourceException
	 *             exception during message sending
	 * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec,
	 *      javax.resource.cci.Record)
	 */
	public Record execute(InteractionSpec interactionSpec, Record recordInput)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("execute(" + interactionSpec + ", " + recordInput + ")");
		}
		Record result = null;
		this.connection.enqueRequest(interactionSpec, recordInput);
		List<Record> response = this.connection.flushConnection();
		if (response != null && response.size() > 0) {
			result = response.get(0);
		}
		this.connection.close();
		return result;
	}

	/**
	 * Used to enque a specific message on the physical connection.
	 * 
	 * @param spec
	 *            the spec to enque
	 * @param inputRecord
	 *            the parameters to add
	 * @param outputRecord
	 *            the record into which we want to store the server response
	 * @return true if everything is ok.
	 * @throws ResourceException
	 */
	private boolean enqueMessage(InteractionSpec spec, Record inputRecord,
			Record outputRecord) throws ResourceException {
		this.enquedRecords.add(outputRecord);
		this.connection.enqueRequest(spec, inputRecord);
		return true;
	}

	/**
	 * Flush messages and get server's response.
	 * 
	 * @return true if everything is ok.
	 * @throws ResourceException
	 */
	private boolean flushExecutions() throws ResourceException {
		boolean resultBool = false;
		List<Record> response = this.connection.flushConnection();
		if (response != null && response.size() > 0) {
			this.processResponse(response);
		}
		this.enquedRecords.clear();
		this.connection.close();
		return resultBool;
	}

	/**
	 * Process a specific response : copy received records into stored records.
	 * 
	 * @param response
	 *            the response sended by the server.
	 */
	private void processResponse(List<Record> response) {
		for (int i = 0; i < this.enquedRecords.size(); i++) {
			this.copyRecords(response.get(i), this.enquedRecords.get(i));
		}
	}

	private void copyRecords(Record from, Record to) {
		if (from instanceof IndexedRecord) {
			for (Object recordElement : (IndexedRecord) from) {
				((IndexedRecord) to).add(recordElement);
			}
		}
		if (from instanceof MappedRecord) {
			for (Map.Entry recordEntry : (Collection<Map.Entry>) ((MappedRecord) from)
					.entrySet()) {
				((MappedRecord) to).put(recordEntry.getKey(), recordEntry
						.getValue());
			}
		}
	}

	/**
	 * JCA 1.5.
	 * 
	 * @return Inheritance.
	 * @throws ResourceException
	 *             Inheritance
	 * @see javax.resource.cci.Interaction#getWarnings()
	 */
	public ResourceWarning getWarnings() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getWarnings");
		}
		return warnings;
	}

	/**
	 * JCA 1.5.
	 * 
	 * @throws ResourceException
	 *             Inheritance
	 * @see javax.resource.cci.Interaction#clearWarnings()
	 */
	public void clearWarnings() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("clearWarnings");
		}
		this.warnings = new ResourceWarning();
	}

}
