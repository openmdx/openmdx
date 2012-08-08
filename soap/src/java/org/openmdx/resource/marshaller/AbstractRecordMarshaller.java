/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: AbstractRecordMarshaller.java,v 1.2 2007/03/23 14:34:50 wfro Exp $
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

import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.Record;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.openmdx.kernel.exception.BasicException;

/**
 * This abstract class is used to marshal IndexedRecords and MappedRecords.
 * 
 */
abstract class AbstractRecordMarshaller implements ClassMarshaller {
	/**
	 * This method is used to implement specific handling of entries.
	 * 
	 * @param reader
	 *            the stream to read.
	 * @throws ResourceException
	 *             A reading problem
	 */
	protected abstract void handleComplexObject(XMLStreamReader reader)
			throws ResourceException;

	/**
	 * This method is used during the unmarshalling of a record to initialise a
	 * record with the correct class.
	 * 
	 * @return a new Instance of Record.
	 */
	protected abstract Record initRecord() throws ResourceException;

	/**
	 * When unmarshalling, the created record.
	 */
	private Record record;

	/**
	 * Needed to unmarshal or marshal entries of the record.
	 */
	private Map<String, ClassMarshaller> marshallers;

	/**
	 * Get the marshaller able to manage the given element name or class
	 * 
	 * @param name
	 *            the element name (reading) or class (writing)
	 * @return the marshaller able to manage the stream.
	 */
	protected ClassMarshaller getMarshaller(String name) {
		return marshallers.get(name);
	}

	/**
	 * Creates a new Marshaller
	 * 
	 * @param marshallers
	 *            the map of marshaller used to serialize or deserialize the
	 *            Record's entries.
	 */
	public AbstractRecordMarshaller(Map<String, ClassMarshaller> marshallers) {
		this.marshallers = marshallers;
	}

	// protected Map<String, ClassMarshaller> getHandlers() {
	// return marshallers;
	// }

	public Object unmarshal(XMLStreamReader reader) throws ResourceException {
		record = this.initRecord();
		try {
			if (this.getHandledElementName()
					.equals(reader.getAttributeValue(0))) {
				reader.next();
			}
			while (true) {
				record.setRecordName(reader.getAttributeValue(0));
				int event = reader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					handleComplexObject(reader);
					break;
				} else if (event == XMLStreamConstants.END_ELEMENT) {
					if (getHandledElementName().equals(reader.getLocalName())) {
						break;
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					ObjectMarshaller.getParameters(this),
					"Unmarshalling error on a Record"));
		}

		return record;
	}

	protected Record getRecord() {
		return record;
	}
}
