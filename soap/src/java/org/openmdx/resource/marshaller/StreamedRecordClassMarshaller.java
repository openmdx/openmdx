/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: StreamedRecordClassMarshaller.java,v 1.2 2007/03/23 14:34:51 wfro Exp $
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.resource.ResourceException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.resource.http.OutputManager;
import org.openmdx.resource.http.StreamedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamedRecordClassMarshaller implements ClassMarshaller {
	/** Logger. * */
	private static final Logger LOG = LoggerFactory
			.getLogger(StreamedRecordClassMarshaller.class);

	private static final String NAME = "SRecord";

	private static final String BOUNDARY = "----45243562456243HGDJSGKHGSDSKHDGS----";

	private OutputStream output;

	private InputStream input;

	private OutputManager manager;

	public StreamedRecordClassMarshaller() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("StreamedRecordClassMarshaller creation");
		}
	}

	public void setMarshallerToRead(InputStream input, OutputManager manager) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("setMarshallerToRead(" + manager + ")");
		}

		this.input = input;
		this.manager = manager;
	}

	public void setMarshallerToWrite(OutputStream output) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("setMarshallerToWrite()");
		}
		this.output = output;
	}

	public String getHandledClassName() {
		return StreamedRecord.class.getName();
	}

	public String getHandledElementName() {
		return NAME;
	}

	public void marshal(Object toMarshal, XMLStreamWriter writer)
			throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("marshal()");
		}
		StreamedRecord record = (StreamedRecord) toMarshal;
		try {
			writer.writeStartElement(NAME);
			if (record.getRecordName() != null) {
				writer.writeStartElement("name".intern());
				writer.writeCharacters(record.getRecordName());
				writer.writeEndElement();
			}
			if (record.getRecordShortDescription() != null) {
				writer.writeStartElement("desc".intern());
				writer.writeCharacters(record.getRecordShortDescription());
				writer.writeEndElement();
			}
			writer.writeEmptyElement("boundary");
			writer.writeStartElement("input");
			writer.writeAttribute("type", "base64binary");
			writer.writeCharacters("\n");
			writer.flush();

			byte[] datas = new byte[2048];
			int read = -1;
			this.print(BOUNDARY, output);
			output.write('\n');
			while ((read = record.getInput().read(datas, 0, datas.length)) != -1) {
				output.write(datas, 0, read);
			}
			output.write('\n');
			this.print(BOUNDARY, output);
			output.write('\n');
			output.flush();
			writer.writeEndElement();
			writer.writeEndElement();
			writer.flush();
		} catch (XMLStreamException e1) {
			throw new ResourceException(new BasicException(e1,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					ObjectMarshaller.getParameters(this),
					"Marshalling error on a StreamedRecord Object"));
		} catch (IOException e) {
			throw new ResourceException(
					new BasicException(
							e,
							BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.TRANSFORMATION_FAILURE,
							ObjectMarshaller.getParameters(this),
							"Marshalling error on a StreamedRecord Object when trying to write the inputStream"));
		}
	}

	public Object unmarshal(XMLStreamReader reader) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("unmarshal()");
		}
		try {
			StreamedRecord record = new StreamedRecord();
			String name = null;
			while (true) {
				int event = reader.next();
				name = reader.getLocalName();
				if (event == XMLStreamConstants.START_ELEMENT) {
					if ("name".equals(name)) {
						record.setRecordName(reader.getElementText());
					} else if ("desc".equals(name)) {
						record.setRecordShortDescription(reader
								.getElementText());
					} else if ("boundary".equals(name)) {
						reader.next();
						break;
					}
				}
			}
			OutputStream out = manager.getOutput(record.getRecordName());
			// inputReader.read(new char[BOUNDARY.length()]);
			byte[] datas = new byte[8 * 2048];
			int read = -1;
			int count = 0;
			byte[] temp = BOUNDARY.getBytes();
			while ((read = this.readLine(input, datas, 0, datas.length)) != -1) {
				if (!compare(datas, temp)) {
					out.write(datas, 0, read);
				} else {
					count++;
					if (count > 1) {
						break;
					}
				}
			}
			out.close();
			reader.next();
			return record;
		} catch (IOException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.TRANSFORMATION_FAILURE,
					ObjectMarshaller.getParameters(this),
					"Unmarshalling error on a StreamedRecord Object"));
		} catch (XMLStreamException e) {
			throw new ResourceException(
					new BasicException(
							e,
							BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.TRANSFORMATION_FAILURE,
							ObjectMarshaller.getParameters(this),
							"Unmarshalling error on a StreamedRecord Object when trying to write on the output"));

		}
	}

	private boolean compare(byte[] bytes, byte[] temp) {
		int count = 0;
		for (int i = 0; i < temp.length && i < bytes.length; i++) {
			if (bytes[i] == temp[i]) {
				count++;
			} else {
				return false;
			}
			if (count == temp.length) {
				return true;
			}
		}
		return false;
	}

	public void print(String s, OutputStream out) throws IOException {
		if (s == null)
			s = "null";
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			out.write(c);
		}
	}

	public int readLine(InputStream in, byte[] b, int off, int len)
			throws IOException {
		if (len <= 0) {
			return 0;
		}
		int count = 0, c;
		while ((c = in.read()) != -1) {
			b[off++] = (byte) c;
			count++;
			if (c == '\n' || count == len) {
				break;
			}
		}
		return count > 0 ? count : -1;
	}

}
