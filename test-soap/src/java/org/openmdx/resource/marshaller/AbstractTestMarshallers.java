/*
 * ====================================================================
 * Project:     openMDX/Test SOAP, http://www.openmdx.org/
 * Name:        $Id: AbstractTestMarshallers.java,v 1.2 2007/08/26 20:41:45 wfro Exp $
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/08/26 20:41:45 $
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;

import org.ietf.jgss.Oid;
import org.openmdx.base.resource.Records;
import org.openmdx.base.transport.jca.OpenMdxInteractionSpec;
import org.openmdx.base.transport.jca.QueryInteractionSpec;
import org.openmdx.resource.http.BeginRequest;
import org.openmdx.resource.http.ConnectorObjectMarshaller;
import org.openmdx.resource.http.ConnectorObjectMarshallerFactory;
import org.openmdx.resource.http.FileOutputManager;
import org.openmdx.resource.http.MarshallerMetaFactory;
import org.openmdx.resource.http.RequestEnvelope;
import org.openmdx.resource.http.StreamedRecord;
import org.openmdx.uses.org.apache.commons.transaction.util.FileHelper;
import org.openxri.XRI;

public abstract class AbstractTestMarshallers extends TestCase {
	private static final int BENCH_RECORD_SIZE = 20;

	private static final int BENCH_RECORD_NUMBER = 50;

	private ConnectorObjectMarshallerFactory factory;

	private static RecordFactory recordFactory = Records.getRecordFactory();

	protected abstract String getFactoryName();

	protected abstract String getTestName();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		factory = MarshallerMetaFactory.getFacory(getFactoryName());
	}

	public void testCustomWrite() throws Exception {
		List<Record> records = new ArrayList<Record>();
		for (int j = 0; j < BENCH_RECORD_NUMBER; j++) {
			IndexedRecord record = recordFactory
					.createIndexedRecord("StringRecordTest");
			for (int i = 0; i < BENCH_RECORD_SIZE; i++) {
				record.add("String" + i);
			}
			records.add(record);
		}
		ConnectorObjectMarshaller marshaller = factory.getMarshaller();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		long begin1 = System.currentTimeMillis();
		marshaller.initWrite(baos);
		for (Record item : records) {
			marshaller.writeObject(item);
		}
		marshaller.flush();
		baos.close();
		long end1 = System.currentTimeMillis();
		System.out.println(getTestName() + " Write Time (ms) = "
				+ (end1 - begin1));
	}

	public void testWriteAndReadOneRequestEnvelope() throws Exception {
		RequestEnvelope envelope = null;
		QueryInteractionSpec spec = new QueryInteractionSpec();
		spec.setObjectId("objectId");
		spec.setDeletePersistent(false);
		spec.setInteractionVerb(0);
		envelope = new RequestEnvelope(spec, recordFactory
				.createIndexedRecord("recordName"));
		envelope = (RequestEnvelope) writeAndReadObject(envelope);
		assertEquals(new QueryInteractionSpec().getFunctionName(),
				((OpenMdxInteractionSpec) envelope.getInteraction())
						.getFunctionName());

	}

	public void testWriteAndReadOneIndexedRecord() throws Exception {
		IndexedRecord record = recordFactory.createIndexedRecord("name");
		for (int i = 0; i < 10; i++) {
			record.add("entity" + i);
		}
		record = (IndexedRecord) writeAndReadObject(record);
		assertEquals("name", record.getRecordName());
		assertEquals(10, record.size());
	}

	public void testWriteAndReadOneMappedRecord() throws Exception {
		MappedRecord record = recordFactory.createMappedRecord("name");
		for (int i = 0; i < 10; i++) {
			record.put("key" + i, "entity" + i);
		}
		record = (MappedRecord) writeAndReadObject(record);
		assertEquals("name", record.getRecordName());
		assertEquals(10, record.size());
	}

	public void testWriteAndReadComplexRecord() throws Exception {
		IndexedRecord record = (IndexedRecord) buildAllRecords();
		record = (IndexedRecord) writeAndReadObject(record);
		assertEquals("response", record.getRecordName());
		assertEquals(BENCH_RECORD_NUMBER, record.size());
		assertEquals(BENCH_RECORD_SIZE, ((MappedRecord) record.get(0)).size());
	}

	public void testWriteAndReadInteger() throws Exception {
		Integer integer = (Integer) writeAndReadObject(new Integer(4));
		assertEquals(4, integer.intValue());
	}

	public void testWriteAndReadShort() throws Exception {
		Short short_ = (Short) writeAndReadObject(new Short((short) 4));
		assertEquals(4, short_.shortValue());
	}

	public void testWriteAndReadLong() throws Exception {
		Long long_ = (Long) writeAndReadObject(new Long(4));
		assertEquals(4, long_.longValue());
	}

	public void testWriteAndReadBigDecimal() throws Exception {
		BigDecimal decimal = (BigDecimal) writeAndReadObject(new BigDecimal(4));
		assertEquals(4, decimal.intValue());
	}

	public void testWriteAndReadBoolean() throws Exception {
		Boolean boolean_ = (Boolean) writeAndReadObject(new Boolean(true));
		assertEquals(true, boolean_.booleanValue());
	}

	public void testWriteAndReadXmlGregorianCalendar() throws Exception {
		XMLGregorianCalendar cal = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar();
		cal.setDay(3);
		cal.setYear(3);
		cal.setMonth(2);
		cal.setDay(3);
		cal.setHour(5);
		cal.setMinute(2);
		cal.setSecond(2);
		cal = (XMLGregorianCalendar) writeAndReadObject(cal);
		assertEquals(5, cal.getHour());
		assertEquals(2, cal.getSecond());
	}

	public void testWriteAndReadURI() throws Exception {
		URI uri = new URI("http", "localhost", "/hop", "fragment");
		URI newUri = (URI) writeAndReadObject(uri);
		assertEquals("http", newUri.getScheme());
		assertEquals("localhost", newUri.getHost());
		assertEquals("/hop", newUri.getPath());
		assertEquals("fragment", newUri.getFragment());
		assertEquals(uri, newUri);
	}

	public void testWriteAndReadXRI() throws Exception {
		XRI xri = XRI.fromURINormalForm(new URI("xri", "localhost", "/hop",
				"fragment").normalize().toString());
		XRI newXri = (XRI) writeAndReadObject(xri);
		assertEquals("fragment", newXri.getFragment().toString());
		assertEquals(xri.toString(), newXri.toString());

	}

	public void testWriteAndReadBeginRequest() throws Exception {
		Object result = writeAndReadObject(new BeginRequest());
		assertTrue(result instanceof BeginRequest);
	}

	public void testWriteAndReadIndexedRecordWithAllObjectTypes()
			throws Exception {
		IndexedRecord record = recordFactory.createIndexedRecord("All");
		record.add("String");
		record.add(new Integer(4));
		record.add(new Long(20));
		record.add(new Boolean(true));
		record.add(new BigDecimal(5.9));
		record.add(new Short((short) 1));
		XMLGregorianCalendar cal = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar();
		cal.setDay(3);
		cal.setYear(3);
		cal.setMonth(2);
		cal.setDay(3);
		cal.setHour(5);
		cal.setMinute(2);
		cal.setSecond(2);
		record.add(cal);
		record.add(new URI("http", "localhost", "/hop", "fragment"));
		record.add(XRI.fromURINormalForm(new URI("xri", "localhost", "/hop",
				"fragment").normalize().toString()));
		IndexedRecord record2 = (IndexedRecord) writeAndReadObject(record);
		assertEquals(record.size(), record2.size());
		for (int i = 0; i < record.size(); i++) {
			if (i == 8) {
				assertEquals(record.get(i).toString(), record2.get(i)
						.toString());
			} else {
				assertEquals(record.get(i), record2.get(i));
			}
		}
	}

	public void testWriteAndReadMappedRecordWithAllObjectTypes()
			throws Exception {
		MappedRecord record = recordFactory.createMappedRecord("All");
		record.setRecordShortDescription("Description");
		record.put("String", "String");
		record.put("integer", new Integer(4));
		record.put("long", new Long(20));
		record.put("boolean", new Boolean(true));
		record.put("decimal", new BigDecimal(5.9));
		record.put("short", new Short((short) 1));
		XMLGregorianCalendar cal = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar();
		cal.setDay(3);
		cal.setYear(3);
		cal.setMonth(2);
		cal.setDay(3);
		cal.setHour(5);
		cal.setMinute(2);
		cal.setSecond(2);
		record.put("calendat", cal);
		record.put("uri", new URI("http", "localhost", "/hop", "fragment"));
		record.put("xri", XRI.fromURINormalForm(new URI("xri", "localhost",
				"/hop", "fragment").normalize().toString()));

		MappedRecord record2 = (MappedRecord) writeAndReadObject(record);
		assertEquals(record.size(), record2.size());
		int i = 0;
		for (Map.Entry entry : (Collection<Map.Entry>) record.entrySet()) {
			if (i == 8) {
				assertNotNull(record2.get(entry.getKey()));
				assertEquals(record.get(entry.getKey()).toString(), record2
						.get(entry.getKey()).toString());
			} else {
				assertNotNull(record2.get(entry.getKey()));
				assertEquals(record.get(entry.getKey()), record2.get(entry
						.getKey()));
			}
			i++;
		}
	}

	public void testHandlersWriteAndReadStreamedRecord() throws Exception {
		// Init the streamed record
		StreamedRecord record = new StreamedRecord();
		record.setRecordName("c:/temp/finaloutput");
		File tempFile = new File("c:/temp/temp");
		File origin = new File("c:/temp/tocopy2");
		FileInputStream tocopy = new FileInputStream(origin);
		record.setInput(tocopy);
		// prepare the communication stream
		FileOutputManager manager = new FileOutputManager();
		FileOutputStream temp = new FileOutputStream(tempFile);

		ConnectorObjectMarshaller marshaller = factory.getMarshaller();
		marshaller.initWrite(temp);
		marshaller.writeObject(record);
		marshaller.flush();
		temp.close();

		InputStream stream = new FileInputStream(tempFile);
		marshaller.initRead(stream, manager);

		record = (StreamedRecord) marshaller.readObject();
		assertEquals("c:/temp/finaloutput", record.getRecordName());
		stream.close();
		File endFile = new File("c:/temp/finaloutput");
		assertEquals(origin.length(), endFile.length() - 1);
		endFile.delete();
		tempFile.delete();
	}

	public void testHandlersWriteAndReadStreamedRecordAndOtherObjects()
			throws Exception {
		// Init the streamed record
		StreamedRecord record = new StreamedRecord();
		record.setRecordName("c:/temp/finaloutput");
		File tempFile = new File("c:/temp/temp");
		File origin = new File("c:/temp/tocopy2");
		FileInputStream tocopy = new FileInputStream(origin);
		record.setInput(tocopy);
		// prepare the communication stream
		FileOutputManager manager = new FileOutputManager();
		FileOutputStream temp = new FileOutputStream(tempFile);

		MappedRecord mapped = recordFactory.createMappedRecord("All");
		mapped.setRecordShortDescription("Description");
		mapped.put("streamed", record);
		mapped.put("String", "String");
		mapped.put("integer", new Integer(4));
		mapped.put("long", new Long(20));
		mapped.put("boolean", new Boolean(true));
		mapped.put("bigdecimal", new BigDecimal(5.9));
		mapped.put("short", new Short((short) 1));
		XMLGregorianCalendar cal = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar();
		cal.setDay(3);
		cal.setYear(3);
		cal.setMonth(2);
		cal.setDay(3);
		cal.setHour(5);
		cal.setMinute(2);
		cal.setSecond(2);
		mapped.put("cal", cal);
		mapped.put("uri", new URI("http", "localhost", "/hop", "fragment"));
		mapped.put("xri", XRI.fromURINormalForm(new URI("xri", "localhost",
				"/hop", "fragment").normalize().toString()));

		ConnectorObjectMarshaller marshaller = factory.getMarshaller();
		marshaller.initWrite(temp);
		marshaller.writeObject(mapped);
		marshaller.flush();
		temp.close();

		InputStream stream = new FileInputStream(tempFile);
		marshaller.initRead(stream, manager);

		mapped = (MappedRecord) marshaller.readObject();
		stream.close();
		assertTrue(mapped.get("streamed") instanceof StreamedRecord);
		File endFile = new File("c:/temp/finaloutput");
		assertEquals(origin.length(), endFile.length() - 1);
		endFile.delete();
		tempFile.delete();
	}

	public static InputStream copyOut(ByteArrayOutputStream out, String fileName)
			throws FileNotFoundException, IOException {
		FileHelper.copy(new ByteArrayInputStream(out.toByteArray()),
				new FileOutputStream(new File(fileName)));
		return new FileInputStream(new File(fileName));

	}

	private static Record buildAllRecords() {
		IndexedRecord response = null;
		try {
			response = recordFactory.createIndexedRecord("response");
			for (int i = 0; i < BENCH_RECORD_NUMBER; i++) {
				Map mapped = recordFactory.createMappedRecord("response" + i);
				for (int j = 0; j < BENCH_RECORD_SIZE; j++) {
					mapped.put("ObjectKey" + j, "ObjectValue" + j);
				}
				response.add(mapped);
			}
		} catch (ResourceException e) {
			e.printStackTrace();
		}
		return response;
	}

	private static Record buildAllRecords2() {
		IndexedRecord response = null;
		try {
			long begin = System.currentTimeMillis();
			response = recordFactory.createIndexedRecord("response");
			Map map = new HashMap<Integer, String>();
			map.put(new Integer(3), "3");
			map.put(new Integer(4), "4");
			map.put(new Integer(5), "5");
			map.put(new Integer(6), "6");
			for (int i = 0; i < BENCH_RECORD_NUMBER; i++) {
				// System.out.println("Record : " + i);
				Map mapped = recordFactory.createMappedRecord("response" + i);
				for (int j = 0; j < BENCH_RECORD_SIZE; j++) {
					// System.out.println("Record Entry : " + j);
					mapped.put("ObjectKey" + j, map);
				}
				response.add(mapped);
			}
			System.out.println("Response creation time (ms)= "
					+ (System.currentTimeMillis() - begin));
		} catch (ResourceException e) {
			e.printStackTrace();
		}
		return response;
	}

	private Object writeAndReadObject(Object toWriteAndRead)
			throws ResourceException, IOException {
		ConnectorObjectMarshaller marshaller = factory.getMarshaller();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshaller.initWrite(baos);
		marshaller.writeObject(toWriteAndRead);
		marshaller.flush();
		baos.close();

		InputStream stream = new ByteArrayInputStream(baos.toByteArray());
		FileHelper.copy(stream, new FileOutputStream("c:/temp/test.xml"));
		stream = new FileInputStream("c:/temp/test.xml");
		marshaller.initRead(stream, null);
		return marshaller.readObject();
	}
}
