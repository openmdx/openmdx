/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: PoXXStreamObjectMarshaller.java,v 1.1 2007/03/22 15:32:53 wfro Exp $
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/22 15:32:53 $
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
package org.openmdx.resource.http.pox;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.resource.ResourceException;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.resource.http.BeginRequest;
import org.openmdx.resource.http.ConnectorObjectMarshaller;
import org.openmdx.resource.http.EndRequest;
import org.openmdx.resource.http.OutputManager;
import org.openmdx.resource.http.XstreamInitialiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

public class PoXXStreamObjectMarshaller implements ConnectorObjectMarshaller {
	/** Logger. * */
	private static final Logger LOG = LoggerFactory
			.getLogger(PoXXStreamObjectMarshaller.class);

	private XStream xstream;

	private ObjectOutputStream objectOutput;

	private ObjectInputStream reader;

	public void initWrite(OutputStream output) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("initWrite()");
		}
		xstream = XstreamInitialiser.getXstream();
		try {
			objectOutput = xstream
					.createObjectOutputStream(new OutputStreamWriter(output));
			objectOutput.writeObject(new BeginRequest());
			objectOutput.flush();
		} catch (IOException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.INITIALIZATION_FAILURE,
					getParameters(),
					"Unable to initialise the handler for writing"));

		}

	}

	public void writeObject(Object toSend) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("sendObject()");
		}
		try {
			objectOutput.writeObject(toSend);
			objectOutput.flush();
		} catch (IOException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					getParameters(), "Unable to write on the stream"));
		}

	}

	public void flush() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("flush()");
		}
		try {
			objectOutput.writeObject(new EndRequest());
			objectOutput.close();
		} catch (IOException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					getParameters(), "Unable to flush the stream"));
		}

	}

	public Object readObject() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("readRecords()");
		}
		try {
			Object object = this.reader.readObject();
			if (object instanceof EndRequest) {
				object = null;
			}
			return object;
		} catch (IOException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.MEDIA_ACCESS_FAILURE, getParameters(),
					"Unable to read on the stream"));
		} catch (ClassNotFoundException e) {
			throw new ResourceException(
					new BasicException(
							e,
							BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.TRANSFORMATION_FAILURE,
							getParameters(),
							"Unable to read on the stream: "
									+ "impossible to read the first object of the stream"));

		}

	}

	public void initRead(InputStream input, OutputManager manager)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("initRead()");
		}
		try {
			if (xstream == null) {
				xstream = XstreamInitialiser.getXstream();
			}
			this.reader = xstream
					.createObjectInputStream(new InputStreamReader(input));
			this.reader.readObject();
		} catch (IOException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.INITIALIZATION_FAILURE,
					getParameters(),
					"Unable to initialise the handler for reading"));
		} catch (ClassNotFoundException e) {
			throw new ResourceException(
					new BasicException(
							e,
							BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.INITIALIZATION_FAILURE,
							getParameters(),
							"Unable to initialise the handler for reading: "
									+ "impossible to read the first object of the stream"));
		}
	}

	private BasicException.Parameter[] getParameters() {
		BasicException.Parameter[] parameters = new BasicException.Parameter[2];
		parameters[0] = new BasicException.Parameter("marshallerType",
				"XStream");
		parameters[1] = new BasicException.Parameter("protocol", "PoX");
		return parameters;
	}
}
