/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: OpenMDXInteractionSpecClassMarshaller.java,v 1.2 2007/03/23 14:34:50 wfro Exp $
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/23 14:34:50 $
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
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.base.transport.jca.DeletePersistentInteractionSpec;
import org.openmdx.base.transport.jca.FlushInteractionSpec;
import org.openmdx.base.transport.jca.MakePersistentInteractionSpec;
import org.openmdx.base.transport.jca.OpenMdxInteractionSpec;
import org.openmdx.base.transport.jca.OperationInteractionSpec;
import org.openmdx.base.transport.jca.QueryInteractionSpec;
import org.openmdx.base.transport.jca.RetrieveInteractionSpec;
import org.openmdx.kernel.exception.BasicException;

/**
 * Manage all OpenMDXInteractionSpec.
 * 
 */
public abstract class OpenMDXInteractionSpecClassMarshaller implements
		ClassMarshaller {
	public abstract String getHandledClassName();

	public abstract String getHandledElementName();

	public void marshal(Object toMarshal, XMLStreamWriter writer)
			throws ResourceException {
		OpenMdxInteractionSpec spec = (OpenMdxInteractionSpec) toMarshal;
		try {
			writer.writeStartElement(this.getHandledElementName());

			writer.writeStartElement("oid".intern());
			writer.writeCharacters(spec.getObjectId());
			writer.writeEndElement();

			writer.writeStartElement("verb".intern());
			writer.writeCharacters("" + spec.getInteractionVerb());
			writer.writeEndElement();

			writer.writeStartElement("delPers".intern());
			writer.writeCharacters("" + spec.getDeletePersistent());
			writer.writeEndElement();

			if (spec.getFetchSize() != null) {
				writer.writeStartElement("fetchSize".intern());
				writer.writeCharacters(spec.getFetchSize().toString());
				writer.writeEndElement();
			}

			if (spec.getFunctionName() != null) {
				writer.writeStartElement("functionName".intern());
				writer.writeCharacters(spec.getFunctionName());
				writer.writeEndElement();
			}
			if (spec.getOperationName() != null) {
				writer.writeStartElement("operationName".intern());
				writer.writeCharacters(spec.getOperationName());
				writer.writeEndElement();
			}

			if (spec.getRangeFrom() != null) {
				writer.writeStartElement("rangeFrom".intern());
				writer.writeCharacters(spec.getRangeFrom().toString());
				writer.writeEndElement();
			}

			if (spec.getRangeTo() != null) {
				writer.writeStartElement("rangeTo".intern());
				writer.writeCharacters(spec.getRangeTo().toString());
				writer.writeEndElement();
			}
			writer.writeEndElement();

		} catch (XMLStreamException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					ObjectMarshaller.getParameters(this),
					"Marshalling error on a OpenMDXInteractionSpec Object"));

		}
	}

	public Object unmarshal(XMLStreamReader reader) throws ResourceException {
		OpenMdxInteractionSpec spec = this.getNewInteractionSpec();
		try {
			while (true) {
				int event = reader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					String name = reader.getLocalName();
					if ("oid".equals(name)) {
						spec.setObjectId(reader.getElementText());
					} else if ("verb".equals(name)) {
						spec.setInteractionVerb(Integer.parseInt(reader
								.getElementText()));
					} else if ("delPers".equals(name)) {
						spec.setDeletePersistent(Boolean.getBoolean(reader
								.getElementText()));
					} else {
						if (event != XMLStreamConstants.END_ELEMENT) {
							if ("fetchSize".equals(name)) {
								spec.setFetchSize(Integer.parseInt(reader
										.getElementText()));
							} else if ("functionName".equals(name)) {
								spec.setFunctionName(reader.getElementText());
							} else if ("operationName".equals(name)) {
								spec.setOperationName(reader.getElementText());
							} else if ("rangeFrom".equals(name)) {
								spec.setRangeFrom(Integer.parseInt(reader
										.getElementText()));
							} else if ("rangeTo".equals(name)) {
								spec.setRangeTo(Integer.parseInt(reader
										.getElementText()));
							}
						} else {
							if (this.getHandledElementName().equals(name)) {
								break;
							}
						}
					}
				} else if (event == XMLStreamConstants.END_ELEMENT) {
					if (this.getHandledElementName().equals(
							reader.getLocalName())) {
						break;
					}
				}
			}

		} catch (XMLStreamException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					ObjectMarshaller.getParameters(this),
					"Unmarshalling error on a OpenMDXInteractionSpec Object"));
		}
		return spec;
	}

	protected abstract OpenMdxInteractionSpec getNewInteractionSpec();

	public static class QueryInteractionSpecClassHandler extends
			OpenMDXInteractionSpecClassMarshaller {
		private static final String NAME = "querySpec";

		@Override
		public String getHandledClassName() {
			return QueryInteractionSpec.class.getName();
		}

		@Override
		public String getHandledElementName() {
			return NAME;
		}

		@Override
		protected OpenMdxInteractionSpec getNewInteractionSpec() {
			return new QueryInteractionSpec();
		}

	}

	public static class DeletePersistentInteractionSpecClassHandler extends
			OpenMDXInteractionSpecClassMarshaller {
		private static final String NAME = "deleteSpec";

		@Override
		public String getHandledClassName() {
			return DeletePersistentInteractionSpec.class.getName();
		}

		@Override
		public String getHandledElementName() {
			return NAME;
		}

		@Override
		protected OpenMdxInteractionSpec getNewInteractionSpec() {
			return new DeletePersistentInteractionSpec();
		}

	}

	public static class FlushInteractionSpecClassHandler extends
			OpenMDXInteractionSpecClassMarshaller {
		private static final String NAME = "flushSpec";

		@Override
		public String getHandledClassName() {
			return FlushInteractionSpec.class.getName();
		}

		@Override
		public String getHandledElementName() {
			return NAME;
		}

		@Override
		protected OpenMdxInteractionSpec getNewInteractionSpec() {
			return new FlushInteractionSpec();
		}

	}

	public static class MakePersistentInteractionSpecClassHandler extends
			OpenMDXInteractionSpecClassMarshaller {
		private static final String NAME = "persistSpec";

		@Override
		public String getHandledClassName() {
			return MakePersistentInteractionSpec.class.getName();
		}

		@Override
		public String getHandledElementName() {
			return NAME;
		}

		@Override
		protected OpenMdxInteractionSpec getNewInteractionSpec() {
			return new MakePersistentInteractionSpec();
		}

	}

	public static class OperationInteractionSpecClassHandler extends
			OpenMDXInteractionSpecClassMarshaller {
		private static final String NAME = "operationSpec";

		@Override
		public String getHandledClassName() {
			return OperationInteractionSpec.class.getName();
		}

		@Override
		public String getHandledElementName() {
			return NAME;
		}

		@Override
		protected OpenMdxInteractionSpec getNewInteractionSpec() {
			return new OperationInteractionSpec(null);
		}

	}

	public static class RetrieveInteractionSpecClassHandler extends
			OpenMDXInteractionSpecClassMarshaller {
		private static final String NAME = "retrieveSpec";

		@Override
		public String getHandledClassName() {
			return RetrieveInteractionSpec.class.getName();
		}

		@Override
		public String getHandledElementName() {
			return NAME;
		}

		@Override
		protected OpenMdxInteractionSpec getNewInteractionSpec() {
			return new RetrieveInteractionSpec();
		}

	}

	public static Map<String, ClassMarshaller> getInteractionSpecHandlers() {
		Map<String, ClassMarshaller> map = new HashMap<String, ClassMarshaller>();
		OpenMDXInteractionSpecClassMarshaller handler = null;

		handler = new QueryInteractionSpecClassHandler();
		map.put(handler.getHandledElementName(), handler);
		map.put(handler.getHandledClassName(), handler);

		handler = new DeletePersistentInteractionSpecClassHandler();
		map.put(handler.getHandledElementName(), handler);
		map.put(handler.getHandledClassName(), handler);

		handler = new FlushInteractionSpecClassHandler();
		map.put(handler.getHandledElementName(), handler);
		map.put(handler.getHandledClassName(), handler);

		handler = new MakePersistentInteractionSpecClassHandler();
		map.put(handler.getHandledElementName(), handler);
		map.put(handler.getHandledClassName(), handler);

		handler = new OperationInteractionSpecClassHandler();
		map.put(handler.getHandledElementName(), handler);
		map.put(handler.getHandledClassName(), handler);

		handler = new RetrieveInteractionSpecClassHandler();
		map.put(handler.getHandledElementName(), handler);
		map.put(handler.getHandledClassName(), handler);
		return map;
	}
}
