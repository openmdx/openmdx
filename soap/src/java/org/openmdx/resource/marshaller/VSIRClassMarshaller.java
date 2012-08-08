/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: VSIRClassMarshaller.java,v 1.2 2007/03/23 14:34:51 wfro Exp $
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

import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Record;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.Records;
import org.openmdx.kernel.exception.BasicException;

/**
 * This class marshal and unmarshal IndexedRecords.
 */
public class VSIRClassMarshaller extends AbstractRecordMarshaller {
	private static final String NAME = "i:VSIR";

	private static final String NAMESPACE = "i";

	private static Record templateRecord = null;

	public VSIRClassMarshaller(Map<String, ClassMarshaller> handlers) {
		super(handlers);
	}

	@Override
	protected void handleComplexObject(XMLStreamReader reader)
			throws ResourceException {
		try {
			int event = XMLStreamConstants.START_ELEMENT;
			while (true) {
				if (event == XMLStreamConstants.START_ELEMENT) {
					((IndexedRecord) getRecord()).add(this.getMarshaller(
							reader.getAttributeValue(0)).unmarshal(reader));
				} else if (event == XMLStreamConstants.END_ELEMENT) {
					if (NAME.equals(reader.getLocalName())) {
						break;
					}
				}
				event = reader.next();
			}
		} catch (XMLStreamException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					ObjectMarshaller.getParameters(this),
					"Unmarshalling error in the entry of an IndexedRecord"));

		}
	}

	@Override
	protected Record initRecord() throws ResourceException {
		return Records.getRecordFactory().createIndexedRecord(null);
	}

	public void marshal(Object toMarshal, XMLStreamWriter writer)
			throws ResourceException {
		IndexedRecord liste = (IndexedRecord) toMarshal;
		try {
			writer.writeStartElement(NAMESPACE, "VSIR".intern(), liste
					.getRecordName());
			writer.writeNamespace(NAMESPACE, liste.getRecordName());
			// writer.writeStartElement(NAME);
			for (Object entry : liste) {
				ClassMarshaller handlingValue = this.getMarshaller(entry
						.getClass().getName());
				writer.writeStartElement(NAMESPACE, "entry", this
						.getHandledClassName());
				writer.writeAttribute("xs:type".intern(), handlingValue
						.getHandledElementName());
				handlingValue.marshal(entry, writer);
				writer.writeEndElement();
			}
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					ObjectMarshaller.getParameters(this),
					"Marshalling when serializing an IndexedRecord"));
		}

	}

	public String getHandledClassName() {
		if (templateRecord == null) {
			try {
				templateRecord = Records.getRecordFactory()
						.createIndexedRecord(null);
			} catch (ResourceException e) {
				new ServiceException(e).log();
			}
		}
		return templateRecord.getClass().getName();
	}

	public String getHandledElementName() {
		return NAME;
	}

}
