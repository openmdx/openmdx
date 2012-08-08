/*
 * ====================================================================
 * Project:     openMDX/Test SOAP, http://www.openmdx.org/
 * Name:        $Id: AbstractTestHttpConnectors.java,v 1.1 2007/03/22 15:33:58 wfro Exp $
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/22 15:33:58 $
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
package org.openmdx.resource.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.cci.RecordFactory;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.security.HashUserRealm;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.transport.jca.OpenMdxInteractionSpec;
import org.openmdx.base.transport.jca.QueryInteractionSpec;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.uses.org.apache.commons.transaction.util.FileHelper;
import org.openmdx.uses.org.apache.servicemix.http.jetty.JettyServer;
import org.openmdx.uses.org.apache.servicemix.http.jetty.SslParameters;

public abstract class AbstractTestHttpConnectors extends TestCase {

	protected JettyServer server;

	private static ExtendedRecordFactory recordFactory = Records
			.getRecordFactory();

	public static final int RECORD_SIZE = 20;

	public static final int BENCH_RECORD_SIZE = 20;

	public static final int BENCH_RECORD_NUMBER = 50;

	public static final int BENCH_QUERY_NUMBER = 50;

	// ---------------------- Echo Servlet -----------------------------

	public static abstract class EchoServlet extends HttpServlet {

		private static final long serialVersionUID = -8606601272548406884L;

		private ConnectorObjectMarshallerFactory factory;

		protected abstract String getFactoryName();

		protected abstract String getLogName();

		@Override
		public void init() throws ServletException {
			super.init();
			try {
				this.factory = MarshallerMetaFactory.getFacory(this
						.getFactoryName());
			} catch (ResourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			this.doPost(request, response);
		}

		protected void doPost(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			try {
				InputStream in = request.getInputStream();
				ConnectorObjectMarshaller marshaller = this.factory
						.getMarshaller();
				marshaller.initRead(in, new FileOutputManager());

				List<RequestEnvelope> envelopes = new ArrayList<RequestEnvelope>();
				Object envelope = null;
				while ((envelope = marshaller.readObject()) != null) {
					envelopes.add((RequestEnvelope) envelope);
				}
				response.setStatus(HttpServletResponse.SC_OK);
				writeResponse(response, marshaller, envelopes);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		protected void writeResponse(HttpServletResponse response,
				ConnectorObjectMarshaller marshaller,
				List<RequestEnvelope> envelopes) throws IOException,
				ResourceException {
			marshaller.initWrite(response.getOutputStream());
			for (RequestEnvelope envelope : envelopes) {
				marshaller.writeObject(envelope.getRecord());
			}
			marshaller.flush();
		}

		public static InputStream copyStream(InputStream stream, String fileName) {
			InputStream input = stream;
			try {
				OutputStream output = new FileOutputStream(new File(fileName));
				FileHelper.copy(input, output);
				output.close();
				input = new FileInputStream(new File(fileName));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return input;
		}
	}

	// ---------------------- Bench Servlet -----------------------------
	public static abstract class BenchServlet extends HttpServlet {

		private static final long serialVersionUID = -8606601272548406884L;

		protected static RecordFactory recordFactory = Records
				.getRecordFactory();

		private ConnectorObjectMarshallerFactory factory;

		protected abstract String getFactoryName();

		protected abstract String getLogName();

		@Override
		public void init() throws ServletException {
			super.init();
			try {
				this.factory = MarshallerMetaFactory.getFacory(this
						.getFactoryName());
			} catch (ResourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			this.doPost(request, response);
		}

		protected void doPost(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			try {
				long begin = System.currentTimeMillis();
				InputStream in = request.getInputStream();
				ConnectorObjectMarshaller marshaller = this.factory
						.getMarshaller();
				marshaller.initRead(in, new FileOutputManager());

				List<RequestEnvelope> envelopes = new ArrayList<RequestEnvelope>();
				Object envelope = null;
				while ((envelope = marshaller.readObject()) != null) {
					envelopes.add((RequestEnvelope) envelope);
				}
				response.setStatus(HttpServletResponse.SC_OK);
				writeResponse(response, marshaller, envelopes);
				System.out.println(this.getLogName()
						+ " server processing time (ms) = "
						+ (System.currentTimeMillis() - begin));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		protected void writeResponse(HttpServletResponse response,
				ConnectorObjectMarshaller marshaller,
				List<RequestEnvelope> envelopes) throws IOException,
				ResourceException {
			long begin = System.currentTimeMillis();
			marshaller.initWrite(response.getOutputStream());
			for (RequestEnvelope envelope : envelopes) {
				marshaller.writeObject(buildAllRecords());
			}
			marshaller.flush();
			System.out.println(this.getLogName() + " server write time (ms) = "
					+ (System.currentTimeMillis() - begin));
		}

		public static Record buildAllRecords() {
			IndexedRecord response = null;
			try {
				response = recordFactory.createIndexedRecord("response");
				for (int i = 0; i < BENCH_RECORD_NUMBER; i++) {
					Map mapped = recordFactory.createMappedRecord("response"
							+ i);
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
	}

	// -----------------------------------------------------------------------
	protected void setUp() throws Exception {
		this.server = new JettyServer();
		this.server.init();
		this.server.start();
		LightweightContainer container = LightweightContainer.getInstance();
		container.deployConnector(new URL(getConnectorURL()));
	}

	protected abstract String getConnectorURL();

	protected abstract String getConnectorJNDIName();

	protected abstract String getEchoServletClassName();

	protected abstract String getBenchServletClassName();

	protected abstract String getTargetURI();

	protected abstract String getListenURI();

	// -----------------------------------------------------------------------
	protected void tearDown() throws Exception {
		this.server.stop();
	}

	// -----------------------------------------------------------------------
	public void testSendQueryInteraction() throws Exception {
		sendQueryInteraction(getConnectorJNDIName(), getEchoServletClassName());
	}

	// -----------------------------------------------------------------------
	public void testSendQuerySecurityContext() throws Exception {
		sendQueryInSecurityContext(getConnectorJNDIName(),
				getEchoServletClassName());
	}

	// -----------------------------------------------------------------------
	public void testSendQueryInteractionAndEmptyRecord() throws Exception {
		sendQueryInteractionAndEmptyRecord(getConnectorJNDIName(),
				getEchoServletClassName());
	}

	// -----------------------------------------------------------------------
	public void testSendQueryInteractionAndNullRecord() throws Exception {
		sendQueryInteractionAndNullRecord(getConnectorJNDIName(),
				getEchoServletClassName());
	}

	// -----------------------------------------------------------------------
	public void testSendQueryInteractionInAndOut() throws Exception {
		sendQueryInteractionInAndOut(getConnectorJNDIName(),
				getEchoServletClassName());
	}

	// -----------------------------------------------------------------------
	public void testEnqueQueryInteractionInAndOut() throws Exception {
		this.enqueQueryInteractionInAndOut(getConnectorJNDIName(),
				getEchoServletClassName(), 3);
	}

	// -----------------------------------------------------------------------
	public void testEnqueQueryInteractionMappedRecord() throws Exception {
		this.enqueQueryInteractionAndMappedRecords(getConnectorJNDIName(),
				getEchoServletClassName(), 3);
	}

	// -----------------------------------------------------------------------
	public void testSoapEnqueQueryInteractionComplexRecord() throws Exception {
		this.enqueuQueryInteractionAndComplexRecords(getConnectorJNDIName(),
				getEchoServletClassName(), 3);
	}

	// -----------------------------------------------------------------------
	public void testBenchResponseTime() throws Exception {
		this.benchResponseTime(getConnectorJNDIName(),
				getBenchServletClassName());
	}

	// -----------------------------------------------------------------------
	public void testStreamedRecord() throws Exception {
		this.streamedRecordTests(getConnectorJNDIName(),
				getBenchServletClassName());
	}

	public void sendQueryInteraction(String connectorJNDIName,
			String servletClassName) throws Exception {
		Object echoServlet = this.server.createContext(getListenURI(),
				servletClassName, new SslParameters(), null, null);
		Interaction interaction = this.getInteraction(connectorJNDIName);
		long begin = System.currentTimeMillis();
		InteractionSpec spec = getQueryInteractionSpec("query",
				InteractionSpec.SYNC_RECEIVE);
		Record result = interaction.execute(spec, getIndexedRecord(
				((OpenMdxInteractionSpec) spec).getObjectId(), RECORD_SIZE));
		interaction.close();
		System.out.println(connectorJNDIName
				+ " sendQueryInteraction time (ms)= "
				+ (System.currentTimeMillis() - begin));
		assertNotNull(result);
		assertEquals(RECORD_SIZE, ((Collection) result).size());
		this.server.remove(echoServlet);
	}

	public void sendQueryInteractionAndEmptyRecord(String connectorJNDIName,
			String servletClassName) throws Exception {
		Object echoServlet = this.server.createContext(getListenURI(),
				servletClassName, new SslParameters(), null, null);
		Interaction interaction = this.getInteraction(connectorJNDIName);
		long begin = System.currentTimeMillis();
		InteractionSpec spec = getQueryInteractionSpec("query",
				InteractionSpec.SYNC_RECEIVE);
		Record result = interaction.execute(spec, getIndexedRecord(
				((OpenMdxInteractionSpec) spec).getObjectId(), 0));
		interaction.close();
		System.out.println(connectorJNDIName
				+ " sendQueryInteractionAndEmptyRecord time (ms)= "
				+ (System.currentTimeMillis() - begin));
		assertNotNull(result);
		assertEquals(0, ((Collection) result).size());
		this.server.remove(echoServlet);
	}

	public void sendQueryInteractionInAndOut(String connectorJNDIName,
			String servletClassName) throws Exception {
		Object echoServlet = this.server.createContext(getListenURI(),
				servletClassName, new SslParameters(), null, null);
		Interaction interaction = this.getInteraction(connectorJNDIName);
		long begin = System.currentTimeMillis();
		Record result = recordFactory.createIndexedRecord("outputRecord");
		InteractionSpec spec = getQueryInteractionSpec("query",
				InteractionSpec.SYNC_RECEIVE);
		interaction.execute(spec, getIndexedRecord(
				((OpenMdxInteractionSpec) spec).getObjectId(), RECORD_SIZE),
				result);
		interaction.close();
		System.out.println(connectorJNDIName
				+ " sendQueryInteractionInAndOut time (ms)= "
				+ (System.currentTimeMillis() - begin));
		assertNotNull(result);
		assertEquals(RECORD_SIZE, ((IndexedRecord) result).size());
		this.server.remove(echoServlet);
	}

	private void enqueQueryInteractionInAndOut(String connectorJNDIName,
			String servletClassName, int numberInteractions) throws Exception {
		Object echoServlet = this.server.createContext(getListenURI(),
				servletClassName, new SslParameters(), null, null);
		Interaction interaction = this.getInteraction(connectorJNDIName);
		long begin = System.currentTimeMillis();
		Record result = null;
		InteractionSpec spec = null;
		Record record = null;
		List<Record> results = new ArrayList<Record>();
		for (int i = 1; i < numberInteractions; i++) {
			spec = getQueryInteractionSpec("query",
					InteractionSpec.SYNC_RECEIVE);
			record = getIndexedRecord(((OpenMdxInteractionSpec) spec)
					.getObjectId(), i * RECORD_SIZE);
			result = recordFactory.createIndexedRecord("outputRecord");
			interaction.execute(spec, record, result);
			results.add(result);
		}
		spec = getQueryInteractionSpec("query",
				InteractionSpec.SYNC_SEND_RECEIVE);
		record = getIndexedRecord(
				((OpenMdxInteractionSpec) spec).getObjectId(),
				numberInteractions * RECORD_SIZE);
		result = recordFactory.createIndexedRecord("outputRecord");
		results.add(result);
		interaction.execute(spec, record, result);
		System.out.println(connectorJNDIName
				+ " enqueQueryInteractionInAndOut time (ms)= "
				+ (System.currentTimeMillis() - begin));
		for (int i = 1; i < numberInteractions + 1; i++) {
			assertEquals(i * RECORD_SIZE, ((IndexedRecord) results.get(i - 1))
					.size());
		}
		this.server.remove(echoServlet);
	}

	private void enqueQueryInteractionAndMappedRecords(
			String connectorJNDIName, String servletClassName,
			int numberInteractions) throws Exception {
		Object echoServlet = this.server.createContext(getListenURI(),
				servletClassName, new SslParameters(), null, null);
		Interaction interaction = this.getInteraction(connectorJNDIName);
		long begin = System.currentTimeMillis();
		Record result = null;
		InteractionSpec spec = null;
		Record record = null;
		List<Record> results = new ArrayList<Record>();
		for (int i = 1; i < numberInteractions; i++) {
			spec = getQueryInteractionSpec("query",
					InteractionSpec.SYNC_RECEIVE);
			record = getMappedRecord(((OpenMdxInteractionSpec) spec)
					.getObjectId(), i * RECORD_SIZE);
			result = recordFactory.createMappedRecord("outputRecord");
			interaction.execute(spec, record, result);
			results.add(result);
		}
		spec = getQueryInteractionSpec("query",
				InteractionSpec.SYNC_SEND_RECEIVE);
		record = getMappedRecord(((OpenMdxInteractionSpec) spec).getObjectId(),
				numberInteractions * RECORD_SIZE);
		result = recordFactory.createMappedRecord("outputRecord");
		results.add(result);
		interaction.execute(spec, record, result);
		System.out.println(connectorJNDIName
				+ " enqueQueryInteractionAndMappedRecords time (ms)= "
				+ (System.currentTimeMillis() - begin));
		for (int i = 1; i < numberInteractions + 1; i++) {
			assertEquals(i * RECORD_SIZE, ((MappedRecord) results.get(i - 1))
					.size());
		}
		this.server.remove(echoServlet);
	}

	private void enqueuQueryInteractionAndComplexRecords(
			String connectorJNDIName, String servletClassName,
			int numberInteractions) throws Exception {
		Object echoServlet = this.server.createContext(getListenURI(),
				servletClassName, new SslParameters(), null, null);
		Interaction interaction = this.getInteraction(connectorJNDIName);
		long begin = System.currentTimeMillis();
		Record result = null;
		InteractionSpec spec = null;
		Record record = null;
		List<Record> results = new ArrayList<Record>();
		for (int i = 1; i < numberInteractions; i++) {
			spec = getQueryInteractionSpec("query",
					InteractionSpec.SYNC_RECEIVE);
			record = getComplexRecord(((OpenMdxInteractionSpec) spec)
					.getObjectId(), i * RECORD_SIZE);
			result = recordFactory.createIndexedRecord("outputRecord");
			interaction.execute(spec, record, result);
			results.add(result);
		}
		spec = getQueryInteractionSpec("query",
				InteractionSpec.SYNC_SEND_RECEIVE);
		record = getComplexRecord(
				((OpenMdxInteractionSpec) spec).getObjectId(),
				numberInteractions * RECORD_SIZE);
		result = recordFactory.createIndexedRecord("outputRecord");
		results.add(result);
		interaction.execute(spec, record, result);
		System.out.println(connectorJNDIName
				+ " enqueQueryInteractionAndComplexRecords time(ms) = "
				+ (System.currentTimeMillis() - begin));
		for (int i = 1; i < numberInteractions + 1; i++) {
			assertEquals(i * RECORD_SIZE, ((IndexedRecord) results.get(i - 1))
					.size());
		}
		this.server.remove(echoServlet);
	}

	public void sendQueryInSecurityContext(String connectorJNDIName,
			String servletClassName) throws Exception {
		Object echoServlet = this.server.createContext(getListenURI(),
				servletClassName, new SslParameters(), "BASIC", getUserRealm());
		Interaction interaction = this.getInteraction(connectorJNDIName);
		InteractionSpec spec = getQueryInteractionSpec("query",
				InteractionSpec.SYNC_RECEIVE);
		Record result = interaction.execute(spec, getIndexedRecord(
				((OpenMdxInteractionSpec) spec).getObjectId(), RECORD_SIZE));
		interaction.close();
		assertNotNull(result);
		assertEquals(RECORD_SIZE, ((Collection) result).size());
		this.server.remove(echoServlet);
	}

	public void sendQueryInteractionAndNullRecord(String connectorJNDIName,
			String servletClassName) throws Exception {
		Object echoServlet = this.server.createContext(getListenURI(),
				servletClassName, new SslParameters(), null, null);
		Interaction interaction = this.getInteraction(connectorJNDIName);
		InteractionSpec spec = getQueryInteractionSpec("query",
				InteractionSpec.SYNC_RECEIVE);
		interaction.execute(spec, getIndexedRecord(
				((OpenMdxInteractionSpec) spec).getObjectId(), 0), null);
		interaction.close();
		this.server.remove(echoServlet);
	}

	public void benchResponseTime(String connectorJNDIName,
			String servletClassName) throws Exception {
		Object echoServlet = this.server.createContext(getListenURI(),
				servletClassName, new SslParameters(), null, null);
		Interaction interaction = this.getInteraction(connectorJNDIName);
		long begining = System.currentTimeMillis();
		InteractionSpec spec = null;
		List<IndexedRecord> records = new ArrayList<IndexedRecord>();
		for (int i = 0; i < BENCH_QUERY_NUMBER; i++) {
			spec = getQueryInteractionSpec("query" + i,
					InteractionSpec.SYNC_RECEIVE);
			IndexedRecord rec = recordFactory.createIndexedRecord("response "
					+ i);
			interaction.execute(spec, getIndexedRecord("queryRecord" + i,
					BENCH_RECORD_SIZE), rec);
			records.add(rec);
		}
		interaction.close();
		long ending = System.currentTimeMillis();
		long time = (ending - begining);
		assertTrue("It took more than one second : time (ms) = " + time,
				(time < (1000)));
		for (IndexedRecord record : records) {
			assertTrue(record.size() > 0);
		}
		System.out.println(connectorJNDIName + " Total Time (ms) = " + time);
		this.server.remove(echoServlet);
	}

	public void streamedRecordTests(String connectorJNDIName,
			String servletClassName) throws Exception {
		Object echoServlet = this.server.createContext(getListenURI(),
				servletClassName, new SslParameters(), null, null);
		Interaction interaction = this.getInteraction(connectorJNDIName);
		InteractionSpec spec = getQueryInteractionSpec("query",
				InteractionSpec.SYNC_RECEIVE);
		StreamedRecord streamed = new StreamedRecord();
		streamed.setRecordName("c:/temp/finaloutput");
		streamed.setRecordShortDescription("desc");
		File origin = new File("c:/temp/tocopy2");
		streamed.setInput(new FileInputStream(origin));
		interaction.execute(spec, streamed);
		interaction.close();
		this.server.remove(echoServlet);
		assertEquals(origin.length() + 1, new File("c:/temp/finaloutput")
				.length());
		FileHelper.deleteFile("c:/temp/finaloutput");
	}

	private HashUserRealm getUserRealm() {
		HashUserRealm realm = new HashUserRealm();
		try {
			realm.setConfig("src/resource/realm.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return realm;
	}

	private Interaction getInteraction(String connectorJNDIName)
			throws Exception {
		Context c = new InitialContext();
		Object o = c.lookup(connectorJNDIName);
		ConnectionFactory factory = (ConnectionFactory) PortableRemoteObject
				.narrow(o, ConnectionFactory.class);
		Connection conn = factory.getConnection(new HttpConnectionSpec(this
				.getTargetURI(), null));
		return conn.createInteraction();
	}

	private InteractionSpec getQueryInteractionSpec(String objectId, int verb) {
		QueryInteractionSpec query = new QueryInteractionSpec();
		query.setInteractionVerb(verb);
		query.setObjectId(objectId);
		query.setDeletePersistent(true);
		query.setFetchSize(new Integer(1000));
		query.setRangeFrom(10);
		query.setRangeTo(100);
		return query;
	}

	private Record getIndexedRecord(String recordName, int entryNumber) {
		IndexedRecord record = null;
		try {
			record = recordFactory.createIndexedRecord(recordName);
			for (int i = 0; i < entryNumber; i++) {
				record.add("Object" + i);
			}
		} catch (ResourceException e) {
			e.printStackTrace();
		}
		return record;
	}

	private Record getMappedRecord(String recordName, int entryNumber) {
		MappedRecord record = null;
		try {
			record = recordFactory.createMappedRecord(recordName);
			for (int i = 0; i < entryNumber; i++) {
				record.put("KEY" + i, "VALUE" + i);
			}
		} catch (ResourceException e) {
			e.printStackTrace();
		}
		return record;
	}

	private Record getComplexRecord(String recordName, int entryNumber) {
		IndexedRecord record = null;
		try {
			record = recordFactory.createIndexedRecord(recordName);
			for (int i = 0; i < entryNumber; i++) {
				MappedRecord mapped = recordFactory
						.createMappedRecord(recordName + "Mapped" + i);
				for (int j = 0; j < entryNumber; j++) {
					mapped.put("KEY" + j, "VALUE" + j);
				}
				record.add(mapped);
			}
		} catch (ResourceException e) {
			e.printStackTrace();
		}
		return record;
	}

}
