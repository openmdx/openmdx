/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: RequestEnvelopeClassMarshaller.java,v 1.1 2007/03/22 15:32:52 wfro Exp $
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
package org.openmdx.resource.marshaller;

import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.Record;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.base.transport.jca.OpenMdxInteractionSpec;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.resource.http.RequestEnvelope;

public class RequestEnvelopeClassMarshaller implements ClassMarshaller {
	private static final String NAME = "reqEnv";

	private Map<String, ClassMarshaller> handlers;

	private ClassMarshaller defaultHandler;

	public RequestEnvelopeClassMarshaller(
			Map<String, ClassMarshaller> handlers,
			ClassMarshaller defaultHandler) {
		this.handlers = handlers;
		this.defaultHandler = defaultHandler;
	}

	public String getHandledClassName() {
		return RequestEnvelope.class.getName();
	}

	public String getHandledElementName() {
		return NAME;
	}

	private ClassMarshaller getHandler(String name) {
		ClassMarshaller result = this.handlers.get(name);
		if (result == null) {
			result = defaultHandler;
		}
		return result;
	}

	public void marshal(Object toMarshal, XMLStreamWriter writer)
			throws ResourceException {
		RequestEnvelope envelope = (RequestEnvelope) toMarshal;
		try {
			writer.writeStartElement(NAME);
			writer.writeStartElement("spec");
			getHandler(envelope.getInteraction().getClass().getName()).marshal(
					envelope.getInteraction(), writer);
			writer.writeEndElement();
			writer.writeStartElement("record");
			getHandler(envelope.getRecord().getClass().getName()).marshal(
					envelope.getRecord(), writer);
			writer.writeEndElement();
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					ObjectMarshaller.getParameters(this),
					"Marshalling error on a RequestEnvelope Object"));
		}

	}

	public Object unmarshal(XMLStreamReader reader) throws ResourceException {
		OpenMdxInteractionSpec spec = null;
		Record record = null;
		try {
			while (true) {
				int event = reader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					String name = reader.getLocalName();
					if ("spec".equals(name)) {
						reader.next();
						if (reader.getLocalName() == null) {
							reader.next();
						}
						spec = (OpenMdxInteractionSpec) getHandler(
								reader.getLocalName()).unmarshal(reader);
					} else if ("record".equals(name)) {
						reader.next();
						if (reader.getLocalName() == null) {
							reader.next();
						}
						record = (Record) getHandler(reader.getLocalName())
								.unmarshal(reader);

					}
				} else if (event == XMLStreamConstants.END_ELEMENT) {
					if (NAME.equals(reader.getLocalName())) {
						break;
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					ObjectMarshaller.getParameters(this),
					"Unmarshalling error on a RequestEnvelope Object"));
		}

		return new RequestEnvelope(spec, record);
	}
}
