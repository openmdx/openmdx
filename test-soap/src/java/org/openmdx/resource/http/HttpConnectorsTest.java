/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.resource.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.security.HashUserRealm;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.transport.jca.OpenMdxInteractionSpec;
import org.openmdx.base.transport.jca.QueryInteractionSpec;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.resource.http.BeginRequest;
import org.openmdx.resource.http.EndRequest;
import org.openmdx.resource.http.RequestEnvelope;
import org.openmdx.resource.http.soap.ObjectInputStreamReader;
import org.openmdx.resource.http.soap.ObjectOutputStreamWriter;
import org.openmdx.resource.http.soap.StreamedSoapMessageHelper;
import org.openmdx.uses.org.apache.commons.transaction.util.FileHelper;
import org.openmdx.uses.org.apache.servicemix.http.jetty.JettyServer;
import org.openmdx.uses.org.apache.servicemix.http.jetty.SslParameters;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class HttpConnectorsTest extends TestCase {

	private final String targetURI = "http://localhost:8193/Service/";

	private final String listenURI = "http://localhost:8193/Service/";

	private static final int RECORD_SIZE = 2;

	protected JettyServer server;

	private static ExtendedRecordFactory recordFactory = Records
			.getRecordFactory();

	private static final String SOAP_CONNECTOR_URL = "file:src/ear/soap-connector.ear/soap-connector.rar";

	private static final String POX_CONNECTOR_URL = "file:src/ear/pox-connector.ear/pox-connector.rar";

	protected Map applicationClientEnvironment = new HashMap();

	// -----------------------------------------------------------------------
	protected void setUp() throws Exception {
		this.server = new JettyServer();
		this.server.init();
		this.server.start();

		LightweightContainer container = LightweightContainer.getInstance();
		container.deployConnector(new URL(POX_CONNECTOR_URL));
		container.deployConnector(new URL(SOAP_CONNECTOR_URL));

	}

	// -----------------------------------------------------------------------
	protected void tearDown() throws Exception {
		this.server.stop();
	}

	public static class SoapEchoServlet extends HttpServlet {

		private static final long serialVersionUID = -8606601272548406884L;

		private final Log log = LogFactory.getLog(SoapEchoServlet.class);

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
				InputStreamReader reader = new InputStreamReader(in);
				ObjectInputStreamReader objectReader = new ObjectInputStreamReader(
						reader);
				String s = objectReader.readLine();
				while (s != null && !s.endsWith("<soapenv:Body>")) {
					s = objectReader.readLine();
				}
				XStream xstream = new XStream(new StaxDriver());
				ObjectInputStream objectInput = xstream
						.createObjectInputStream(objectReader);
				List<RequestEnvelope> interactions = new ArrayList<RequestEnvelope>();

				Object object = objectInput.readObject();
				object = objectInput.readObject();
				while (!(object instanceof EndRequest)) {
					interactions.add((RequestEnvelope) object);
					object = objectInput.readObject();
				}
				objectInput.close();
				// Read SOAP envelope end
				s = objectReader.readLine();
				while (s != null && !s.trim().endsWith("</soapenv:Envelope>")) {
					s = objectReader.readLine();
				}
				reader.close();

				PrintWriter writer = new PrintWriter(response.getOutputStream());
				StreamedSoapMessageHelper.writeStreamHeader(writer);

				ObjectOutputStream objectStream = xstream
						.createObjectOutputStream(new ObjectOutputStreamWriter(
								writer));
				objectStream
						.writeObject(new BeginRequest());
				objectStream.flush();

				for (RequestEnvelope envelope : interactions) {
					objectStream.writeObject(envelope.getRecord());
				}
				objectStream
						.writeObject(new EndRequest());
				objectStream.flush();
				objectStream.close();

				writer.println();
				writer.println("    </soapenv:Body>");
				writer.println("</soapenv:Envelope>");

				writer.close();
				// response.setStatus(HttpServletResponse.SC_OK);
			} catch (Exception e) {
				log.error(e);
				response
						.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}

		public InputStream copyStream(InputStream stream, String fileName) {
			InputStream input = stream;
			try {
				// ByteArrayOutputStream output = new ByteArrayOutputStream();
				OutputStream output = new FileOutputStream(new File(fileName));
				FileHelper.copy(input, output);
				output.flush();
				output.close();
				// input = new ByteArrayInputStream(output.toByteArray());
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

	public static class POXEchoServlet extends HttpServlet {

		private static final long serialVersionUID = -8606601272548406884L;

		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			this.doPost(request, response);
		}

		protected void doPost(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			try {
				XStream xstream = new XStream(new StaxDriver());
				InputStream in = request.getInputStream();
				ObjectInputStream streamIn = xstream
						.createObjectInputStream(new InputStreamReader(in));

				List<RequestEnvelope> envelopes = new ArrayList<RequestEnvelope>();
				Object object = streamIn.readObject();
				object = streamIn.readObject();
				while (!(object instanceof EndRequest)) {
					envelopes.add((RequestEnvelope) object);
					object = streamIn.readObject();
				}
				response.setStatus(HttpServletResponse.SC_OK);
				ObjectOutputStream streamOut = xstream
						.createObjectOutputStream(new OutputStreamWriter(
								response.getOutputStream()));
				streamOut.writeObject(new BeginRequest());
				for (RequestEnvelope envelope : envelopes) {
					streamOut.writeObject(envelope.getRecord());
				}
				streamOut.writeObject(new EndRequest());
				streamOut.flush();
				streamOut.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// -----------------------------------------------------------------------
	public void testSoapSendQueryInteraction() throws Exception {
		sendQueryInteraction("connector/soap", SoapEchoServlet.class.getName());
	}

	// -----------------------------------------------------------------------
	public void testSoapSendQuerySecurityContext() throws Exception {
		sendQueryInSecurityContext("connector/soap", SoapEchoServlet.class
				.getName());
	}

	// -----------------------------------------------------------------------
	public void testSoapSendQueryInteractionAndEmptyRecord() throws Exception {
		sendQueryInteractionAndEmptyRecord("connector/soap",
				SoapEchoServlet.class.getName());
	}

	// -----------------------------------------------------------------------
	public void testSoapSendQueryInteractionAndNullRecord() throws Exception {
		sendQueryInteractionAndNullRecord("connector/soap",
				SoapEchoServlet.class.getName());
	}

	// -----------------------------------------------------------------------
	public void testSoapSendQueryInteractionInAndOut() throws Exception {
		sendQueryInteractionInAndOut("connector/soap", SoapEchoServlet.class
				.getName());
	}

	// -----------------------------------------------------------------------
	public void testSoapEnqueQueryInteractionInAndOut() throws Exception {
		this.enqueQueryInteractionInAndOut("connector/soap",
				SoapEchoServlet.class.getName(), 3);
	}

	// -----------------------------------------------------------------------
	public void testSoapEnqueQueryInteractionMappedRecord() throws Exception {
		this.enqueQueryInteractionAndMappedRecords("connector/soap",
				SoapEchoServlet.class.getName(), 3);
	}

	// -----------------------------------------------------------------------
	public void testSoapEnqueQueryInteractionComplexRecord() throws Exception {
		this.enqueuQueryInteractionAndComplexRecords("connector/soap",
				SoapEchoServlet.class.getName(), 3);
	}

	// -----------------------------------------------------------------------
	// POX
	// -----------------------------------------------------------------------
	public void testPoXSendQueryInteraction() throws Exception {
		sendQueryInteraction("connector/pox", POXEchoServlet.class.getName());
	}

	// -----------------------------------------------------------------------
	public void testPoXSendQueryInteractionAndEmptyRecord() throws Exception {
		sendQueryInteractionAndEmptyRecord("connector/pox",
				POXEchoServlet.class.getName());
	}

	// -----------------------------------------------------------------------
	public void testPoXSendQueryInteractionAndNullRecord() throws Exception {
		sendQueryInteractionAndNullRecord("connector/pox", POXEchoServlet.class
				.getName());
	}

	// -----------------------------------------------------------------------
	public void testPoXSendQueryInteractionInAndOut() throws Exception {
		sendQueryInteractionInAndOut("connector/pox", POXEchoServlet.class
				.getName());
	}

	// -----------------------------------------------------------------------
	public void testPoXEnqueQueryInteractionInAndOut() throws Exception {
		this.enqueQueryInteractionInAndOut("connector/pox",
				POXEchoServlet.class.getName(), 3);
	}

	// -----------------------------------------------------------------------
	public void testPoXSendQuerySecurityContext() throws Exception {
		sendQueryInSecurityContext("connector/pox", POXEchoServlet.class
				.getName());
	}

	// -----------------------------------------------------------------------
	public void testPoXEnqueQueryInteractionMappedRecord() throws Exception {
		this.enqueQueryInteractionAndMappedRecords("connector/pox",
				POXEchoServlet.class.getName(), 3);
	}

	// -----------------------------------------------------------------------
	public void testPoXEnqueQueryInteractionComplexRecord() throws Exception {
		this.enqueuQueryInteractionAndComplexRecords("connector/pox",
				POXEchoServlet.class.getName(), 3);
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
				+ " enqueuQueryInteractionAndComplexRecords time(ms) = "
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

	private HashUserRealm getUserRealm() {
		HashUserRealm realm = new HashUserRealm();
		try {
			realm.setConfig("src/resources/realm.properties");
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
				.getTargetURI()));
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

	public String getTargetURI() {
		return targetURI;
	}

	public String getListenURI() {
		return listenURI;
	}

}
