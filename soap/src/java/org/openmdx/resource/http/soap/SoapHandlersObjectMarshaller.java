/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: SoapHandlersObjectMarshaller.java,v 1.1 2007/03/22 15:32:53 wfro Exp $
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
package org.openmdx.resource.http.soap;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.resource.ResourceException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.resource.http.BeginRequest;
import org.openmdx.resource.http.ConnectorObjectMarshaller;
import org.openmdx.resource.http.EndRequest;
import org.openmdx.resource.http.InputStreamDelegate;
import org.openmdx.resource.http.OutputManager;
import org.openmdx.resource.marshaller.ObjectMarshaller;
import org.openmdx.resource.marshaller.StreamedRecordClassMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapHandlersObjectMarshaller implements ConnectorObjectMarshaller {
	/** Logger. * */
	private static final Logger LOG = LoggerFactory
			.getLogger(SoapHandlersObjectMarshaller.class);

	private ObjectMarshaller marshaller;

	private XMLStreamWriter writer;

	private PrintWriter printWriter;

	private XMLStreamReader parser;

	private StreamedRecordClassMarshaller streamMarshaller;

	public void initWrite(OutputStream output) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("init()");
		}
		marshaller = new ObjectMarshaller();
		streamMarshaller = new StreamedRecordClassMarshaller();
		streamMarshaller.setMarshallerToWrite(output);
		marshaller.addMarshaller(streamMarshaller);
		try {
			writer = marshaller.getOutputFactory()
					.createXMLStreamWriter(output);
			printWriter = new PrintWriter(output);
			StreamedSoapMessageHelper.writeStreamHeader(printWriter);
			printWriter.flush();
			writer.writeStartElement("object-stream");
			marshaller.writeObject(new BeginRequest(), writer);
		} catch (XMLStreamException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.INITIALIZATION_FAILURE,
					getParameters(),
					"Unable to initialise this handler for writing"));
		}
	}

	public void writeObject(Object toSend) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("writeObject(" + toSend + ")");
		}
		marshaller.writeObject(toSend, writer);
		try {
			writer.flush();
		} catch (XMLStreamException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.MEDIA_ACCESS_FAILURE, getParameters(),
					"Unable write on the stream with this handler"));
		}
	}

	public void flush() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("flush()");
		}
		marshaller.writeObject(new EndRequest(), writer);
		try {
			writer.writeEndDocument();
			writer.flush();
			StreamedSoapMessageHelper.writeStreamFooter(printWriter);
			printWriter.close();
		} catch (XMLStreamException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.MEDIA_ACCESS_FAILURE, getParameters(),
					"Unable flush the stream with this handler"));
		}
	}

	public Object readObject() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("readObject()");
		}
		Object object = marshaller.readObject(parser);
		if (object instanceof EndRequest) {
			object = null;
		}
		return object;
	}

	public void initRead(InputStream input, OutputManager manager)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("initRead()");
		}
		try {
			if (manager != null) {
				input = new InputStreamDelegate(input);
			}
			if (marshaller == null) {
				marshaller = new ObjectMarshaller();
				streamMarshaller = new StreamedRecordClassMarshaller();
				streamMarshaller.setMarshallerToRead(input, manager);
				marshaller.addMarshaller(streamMarshaller);
			} else {
				streamMarshaller.setMarshallerToRead(input, manager);
			}
			parser = marshaller.getInputFactory().createXMLStreamReader(input);
			marshaller.readObject(parser);
		} catch (XMLStreamException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.INITIALIZATION_FAILURE,
					getParameters(),
					"Unable to initialise the handler for reading"));
		}
	}

	private BasicException.Parameter[] getParameters() {
		BasicException.Parameter[] parameters = new BasicException.Parameter[2];
		parameters[0] = new BasicException.Parameter("marshallerType",
				"custom openmdx handlers");
		parameters[1] = new BasicException.Parameter("protocol", "Soap");
		return parameters;
	}
}
