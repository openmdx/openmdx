/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: ObjectMarshaller.java,v 1.2 2007/03/23 14:34:51 wfro Exp $
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/23 14:34:51 $
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

import java.util.HashMap;
import java.util.Map;

import javax.resource.ResourceException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.kernel.exception.BasicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectMarshaller {
	/** Logger. * */
	private static final Logger LOG = LoggerFactory
			.getLogger(ObjectMarshaller.class);

	public static final int OBJECT_MARSHALLER_READONLY = 0;

	public static final int OBJECT_MARSHALLER_WRITEONLY = 1;

	public static final int OBJECT_MARSHALLER_READ_AND_WRITE = 2;

	private XMLInputFactory inputFactory;

	private XMLOutputFactory outputFactory;

	private Map<String, ClassMarshaller> marshallers;

	/**
	 * Initialise all known Marshaller and put theim into a map.
	 * 
	 */
	public ObjectMarshaller() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("ObjectMarshaller Creation");
		}
		marshallers = new HashMap<String, ClassMarshaller>();
		inputFactory = XMLInputFactory.newInstance();
		inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
		inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		outputFactory = XMLOutputFactory.newInstance();
		ClassMarshaller marshaller = new VSIRClassMarshaller(marshallers);
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new VSMRClassMarshaller(marshallers);
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new StringClassMarshaller();
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshallers.putAll(OpenMDXInteractionSpecClassMarshaller
				.getInteractionSpecHandlers());

		marshaller = new RequestEnvelopeClassMarshaller(marshallers, null);
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new BeginRequestClassMarshaller();
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new EndRequestClassMarshaller();
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new IntegerClassMarshaller();
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new ShortClassMarshaller();
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new LongClassMarshaller();
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new BigDecimalClassMarshaller();
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new BooleanClassMarshaller();
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new XmlGregorianCalendarClassMarshaller();
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new URIClassMarshaller();
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);

		marshaller = new XRIClassMarshaller();
		marshallers.put(marshaller.getHandledElementName(), marshaller);
		marshallers.put(marshaller.getHandledClassName(), marshaller);
	}

	/**
	 * Adds a marshaller that can be used to serialize or deserialize objects.
	 * 
	 * @param newMarshaller
	 *            the new marshaller to add.
	 */
	public void addMarshaller(ClassMarshaller newMarshaller)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("adding a marshaller " + newMarshaller);
		}
		this.marshallers.put(newMarshaller.getHandledElementName(),
				newMarshaller);
		this.marshallers
				.put(newMarshaller.getHandledClassName(), newMarshaller);
	}

	/**
	 * Reads an object from the given Stream.
	 * 
	 * @param parser
	 *            the xml parser used to read the object.
	 * @return The new object.
	 * @throws ResourceException
	 */
	public Object readObject(XMLStreamReader parser) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("readObject()");
		}
		Object result = null;
		try {
			while (true) {
				int event = parser.next();
				if (event == XMLStreamConstants.END_DOCUMENT) {
					parser.close();
					break;
				}
				if (event == XMLStreamConstants.START_ELEMENT) {
					if (marshallers.containsKey(parser.getAttributeValue(0))) {
						result = marshallers.get(parser.getAttributeValue(0))
								.unmarshal(parser);
						break;
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new ResourceException(
					new BasicException(e, BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.TRANSFORMATION_FAILURE, null,
							"ObjectMarshaller : error while reading an Object on the stream"));
		}
		return result;
	}

	public void writeObject(Object toWrite, XMLStreamWriter writer)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("writeObject(" + toWrite + ")");
		}
		try {
			ClassMarshaller handler = this.marshallers.get(toWrite.getClass()
					.getName());
			writer.writeStartElement("q".intern(), "marshall".intern(), handler
					.getHandledElementName());
			writer
					.writeNamespace("q".intern(), handler
							.getHandledElementName());
			handler.marshal(toWrite, writer);
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			throw new ResourceException(
					new BasicException(e, BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.TRANSFORMATION_FAILURE, null,
							"ObjectMarshaller : error while writing an Object on the stream"));
		}

	}

	public Map<String, ClassMarshaller> getHandlers() {
		return marshallers;
	}

	public XMLInputFactory getInputFactory() {
		return inputFactory;
	}

	public XMLOutputFactory getOutputFactory() {
		return outputFactory;
	}

	public static BasicException.Parameter[] getParameters(
			ClassMarshaller marshaller) {
		BasicException.Parameter[] parameters = new BasicException.Parameter[2];
		parameters[0] = new BasicException.Parameter("className", marshaller
				.getHandledClassName());
		parameters[1] = new BasicException.Parameter("elementName", marshaller
				.getHandledElementName());
		return parameters;
	}

}
