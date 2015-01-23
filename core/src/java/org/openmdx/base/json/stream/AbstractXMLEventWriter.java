/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: AbstractXMLEventWriter
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 * Copyright 2006 Envoi Solutions LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.base.json.stream;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * An XMLEventWriter that delegates to an XMLStreamWriter.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 21-Mar-2008
 */
public class AbstractXMLEventWriter implements XMLEventWriter {
	private XMLStreamWriter streamWriter;

	public AbstractXMLEventWriter(XMLStreamWriter streamWriter) {
		this.streamWriter = streamWriter;
	}

	public void add(XMLEvent event) throws XMLStreamException {
		if (event.isStartDocument()) {
			streamWriter.writeStartDocument();
		} else if (event.isStartElement()) {
			StartElement element = event.asStartElement();
			QName elQName = element.getName();
			if (elQName.getPrefix().length() > 0
					&& elQName.getNamespaceURI().length() > 0)
				streamWriter.writeStartElement(elQName.getPrefix(), elQName
						.getLocalPart(), elQName.getNamespaceURI());
			else if (elQName.getNamespaceURI().length() > 0)
				streamWriter.writeStartElement(elQName.getNamespaceURI(),
						elQName.getLocalPart());
			else
				streamWriter.writeStartElement(elQName.getLocalPart());

			// Add element namespaces
			Iterator<?> namespaces = element.getNamespaces();
			while (namespaces.hasNext()) {
				Namespace ns = (Namespace) namespaces.next();
				String prefix = ns.getPrefix();
				String nsURI = ns.getNamespaceURI();
				streamWriter.writeNamespace(prefix, nsURI);
			}

			// Add element attributes
			Iterator<?> attris = element.getAttributes();
			while (attris.hasNext()) {
				Attribute attr = (Attribute) attris.next();
				QName atQName = attr.getName();
				String value = attr.getValue();
				if (atQName.getPrefix().length() > 0
						&& atQName.getNamespaceURI().length() > 0)
					streamWriter.writeAttribute(atQName.getPrefix(), atQName
							.getNamespaceURI(), atQName.getLocalPart(), value);
				else if (atQName.getNamespaceURI().length() > 0)
					streamWriter.writeAttribute(atQName.getNamespaceURI(),
							atQName.getLocalPart(), value);
				else
					streamWriter.writeAttribute(atQName.getLocalPart(), value);
			}
		} else if (event.isCharacters()) {
			Characters chars = event.asCharacters();
			streamWriter.writeCharacters(chars.getData());
		} else if (event.isEndElement()) {
			streamWriter.writeEndElement();
		} else if (event.isEndDocument()) {
			streamWriter.writeEndDocument();
		} else {
			throw new XMLStreamException("Unsupported event type: " + event);
		}
	}

	public void add(XMLEventReader eventReader) throws XMLStreamException {
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			add(event);
		}
		close();
	}

	public void close() throws XMLStreamException {
		streamWriter.close();
	}

	public void flush() throws XMLStreamException {
		streamWriter.flush();
	}

	public NamespaceContext getNamespaceContext() {
		return streamWriter.getNamespaceContext();
	}

	public String getPrefix(String prefix) throws XMLStreamException {
		return streamWriter.getPrefix(prefix);
	}

	public void setDefaultNamespace(String namespace) throws XMLStreamException {
		streamWriter.setDefaultNamespace(namespace);
	}

	public void setNamespaceContext(NamespaceContext nsContext)
			throws XMLStreamException {
		streamWriter.setNamespaceContext(nsContext);
	}

	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		streamWriter.setPrefix(prefix, uri);
	}
}