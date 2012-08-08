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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.uses.org.apache.commons.httpclient.HttpClient;
import org.openmdx.uses.org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.openmdx.uses.org.apache.commons.httpclient.methods.PostMethod;
import org.openmdx.uses.org.apache.commons.httpclient.methods.StringRequestEntity;
import org.openmdx.uses.org.apache.commons.transaction.util.FileHelper;
import org.openmdx.uses.org.apache.servicemix.http.jetty.JettyServer;
import org.openmdx.uses.org.apache.servicemix.http.jetty.SslParameters;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.StringSource;
import org.openmdx.uses.org.apache.servicemix.jbi.util.ByteArrayDataSource;
import org.openmdx.uses.org.apache.servicemix.jbi.util.DOMUtil;
import org.openmdx.uses.org.apache.servicemix.jbi.util.FileUtil;
import org.openmdx.uses.org.apache.servicemix.soap.SoapFault;
import org.openmdx.uses.org.apache.servicemix.soap.marshalers.SoapMarshaler;
import org.openmdx.uses.org.apache.servicemix.soap.marshalers.SoapMessage;
import org.openmdx.uses.org.apache.servicemix.soap.marshalers.SoapReader;
import org.openmdx.uses.org.apache.servicemix.soap.marshalers.SoapWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.wsdl.util.xml.DOMUtils;

public class HttpSoapTest extends TestCase {

	protected JettyServer server;

	public static QName envelopeName = null;

	public static boolean validateRequest = false;

	public static boolean useDomMarshaler = false;

	private String targetURI = "http://localhost:8193/Service/";

	private String listenURI = "http://localhost:8193/Service/";

	// -----------------------------------------------------------------------
	protected void setUp() throws Exception {
		this.server = new JettyServer();
		this.server.init();
		this.server.start();
	}

	// -----------------------------------------------------------------------
	protected void tearDown() throws Exception {
		this.server.stop();
	}

	// -----------------------------------------------------------------------
	public static class SoapEchoServlet extends HttpServlet {

		private static final long serialVersionUID = 3534913260801386509L;

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
				SoapMarshaler marshaler = new SoapMarshaler(true,
						HttpSoapTest.useDomMarshaler);
				SoapReader reader = new SoapReader(marshaler);
				SoapMessage in = reader.read(request.getInputStream(), request
						.getContentType());
				String strMsg = DOMUtil.asXML(in.getDocument());
				if (HttpSoapTest.validateRequest) {
					System.err.println(strMsg);
					Element e = new SourceTransformer().toDOMElement(in
							.getDocument());
					QName expected = new QName(SoapMarshaler.SOAP_11_URI,
							SoapMarshaler.ENVELOPE);
					QName actual = DOMUtil.getQName(e);
					assertEquals(expected, actual);
					e = DOMUtil.getFirstChildElement(e);
					expected = new QName(SoapMarshaler.SOAP_11_URI,
							SoapMarshaler.BODY);
					actual = DOMUtil.getQName(e);
					assertEquals(expected, actual);
					e = DOMUtil.getFirstChildElement(e);
					expected = new QName(
							"http://ws.location.services.cardinal.com/",
							"listAllProvider");
					actual = DOMUtil.getQName(e);
					assertEquals(expected, actual);
					e = DOMUtil.getFirstChildElement(e);
					expected = new QName("", "clientSessionGuid");
					actual = DOMUtil.getQName(e);
					assertEquals(expected, actual);
				}
				// Echo reply
				SoapMessage out = new SoapMessage();
				out.setDocument(in.getDocument());
				if (in.getAttachments() != null) {
					for (Map.Entry e : (Collection<Map.Entry>) in
							.getAttachments().entrySet()) {
						out.addAttachment((String) e.getKey(), (DataHandler) e
								.getValue());
					}
				}
				out.setEnvelopeName(HttpSoapTest.envelopeName);
				SoapWriter writer = new SoapWriter(marshaler, out);
				manageStreams(response, writer);
				response.setContentType(writer.getContentType());
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (Exception e) {
				log.error(e);
				response
						.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}

		/**
		 * Method used to copy the input stream into a temp bytearray.
		 * 
		 * @param response
		 *            the servlet response on to finaly copy the input
		 * @param writer
		 *            the SoapWriter containing the input message.
		 * @throws Exception
		 *             Problem during stream copy.
		 */
		private void manageStreams(HttpServletResponse response,
				SoapWriter writer) throws Exception {

			ByteArrayOutputStream tempOutPut = new ByteArrayOutputStream();
			writer.write(tempOutPut);
			FileHelper.copy(new ByteArrayInputStream(tempOutPut.toByteArray()),
					response.getOutputStream(), new byte[2048]);
		}
	}

	// -----------------------------------------------------------------------
	public static class SoapFaultServlet extends HttpServlet {

		private static final long serialVersionUID = 3534913260801386509L;

		private final Log log = LogFactory.getLog(SoapFaultServlet.class);

		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			this.doPost(request, response);
		}

		protected void doPost(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			try {
				SoapMarshaler domMarshaler = new SoapMarshaler(true, true);
				SoapMessage message = new SoapMessage();
				message.setEnvelopeName(HttpSoapTest.envelopeName);
				SoapFault fault = new SoapFault(
						new QName("env:Fault", "fault"),
						"<hello xmlns='myuri'>this is a fault</hello>");
				message.setFault(fault);
				SoapWriter writer = new SoapWriter(domMarshaler, message);
				writer.write(response.getOutputStream());
				response.setContentType(writer.getContentType());
				response
						.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (Exception e) {
				log.error(e);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
	}

	// -----------------------------------------------------------------------
	public void testFaultOnParse() throws Exception {
		HttpSoapTest.envelopeName = new QName(SoapMarshaler.SOAP_12_URI,
				SoapMarshaler.ENVELOPE);
		HttpSoapTest.useDomMarshaler = false;
		Object echoServlet = this.server.createContext(getListenURI(),
				SoapEchoServlet.class.getName(), new SslParameters(), null,
				null);
		PostMethod method = new PostMethod(getTargetURI());
		method
				.setRequestEntity(new StringRequestEntity(
						"<hello>world</hello>"));
		int state = new HttpClient().executeMethod(method);
		assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, state);
		FileUtil.copyInputStream(method.getResponseBodyAsStream(), System.out);

		this.server.remove(echoServlet);
	}

	// -----------------------------------------------------------------------
	public void testSoap() throws Exception {
		HttpSoapTest.envelopeName = new QName(SoapMarshaler.SOAP_12_URI,
				SoapMarshaler.ENVELOPE);
		HttpSoapTest.validateRequest = false;
		HttpSoapTest.useDomMarshaler = false;
		Object echoServlet = this.server.createContext(getListenURI(),
				SoapEchoServlet.class.getName(), new SslParameters(), null,
				null);
		PostMethod method = null;
		for (int i = 0; i < 1; i++) {
			method = new PostMethod(getTargetURI());
			SoapMarshaler marshaler = new SoapMarshaler(true, false);
			SoapMessage message = new SoapMessage();
			message.setEnvelopeName(HttpSoapTest.envelopeName);
			message
					.setSource(new StringSource(
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?><getQuote encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><symbol>IBM</symbol></getQuote>"));
			SoapWriter writer = new SoapWriter(marshaler, message);
			ByteArrayOutputStream soapMessage = new ByteArrayOutputStream();
			writer.write(soapMessage);
			soapMessage.close();
			method.setRequestEntity(new InputStreamRequestEntity(
					new ByteArrayInputStream(soapMessage.toByteArray())));
			int state = new HttpClient().executeMethod(method);
			assertEquals(HttpServletResponse.SC_OK, state);
		}
		FileUtil.copyInputStream(method.getResponseBodyAsStream(), System.err);
		this.server.remove(echoServlet);
	}

	// -----------------------------------------------------------------------
	public void testSoapFault12() throws Exception {
		HttpSoapTest.envelopeName = new QName(SoapMarshaler.SOAP_12_URI,
				SoapMarshaler.ENVELOPE);
		HttpSoapTest.validateRequest = false;
		HttpSoapTest.useDomMarshaler = false;
		Object faultServlet = this.server.createContext(getListenURI(),
				SoapFaultServlet.class.getName(), new SslParameters(), null,
				null);
		PostMethod method = new PostMethod(getTargetURI());
		method.setRequestEntity(new InputStreamRequestEntity(Classes
				.getSystemResource("soap-request-12.xml").openStream()));
		new HttpClient().executeMethod(method);
		// assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, state);
		SourceTransformer st = new SourceTransformer();
		Node node = st.toDOMNode(new StreamSource(method
				.getResponseBodyAsStream()));
		System.err.println(st.toString(node));
		Element e = ((Document) node).getDocumentElement();
		assertEquals(new QName(SoapMarshaler.SOAP_12_URI,
				SoapMarshaler.ENVELOPE), DOMUtil.getQName(e));
		e = DOMUtil.getFirstChildElement(e);
		assertEquals(new QName(SoapMarshaler.SOAP_12_URI, SoapMarshaler.BODY),
				DOMUtil.getQName(e));
		e = DOMUtil.getFirstChildElement(e);
		assertEquals(new QName(SoapMarshaler.SOAP_12_URI, SoapMarshaler.FAULT),
				DOMUtil.getQName(e));

		this.server.remove(faultServlet);
	}

	// -----------------------------------------------------------------------
	public void testSoapFault11() throws Exception {
		HttpSoapTest.envelopeName = new QName(SoapMarshaler.SOAP_11_URI,
				SoapMarshaler.ENVELOPE);
		HttpSoapTest.validateRequest = false;
		HttpSoapTest.useDomMarshaler = false;
		Object faultServlet = this.server.createContext(getListenURI(),
				SoapFaultServlet.class.getName(), new SslParameters(), null,
				null);
		PostMethod method = new PostMethod(getTargetURI());
		method.setRequestEntity(new InputStreamRequestEntity(Classes
				.getSystemResource("soap-request.xml").openStream()));
		new HttpClient().executeMethod(method);
		String str = method.getResponseBodyAsString();
		System.err.println(str);
		// assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, state);
		SourceTransformer st = new SourceTransformer();
		Node node = st.toDOMNode(new StringSource(str));
		Element e = ((Document) node).getDocumentElement();
		assertEquals(new QName(SoapMarshaler.SOAP_11_URI,
				SoapMarshaler.ENVELOPE), DOMUtil.getQName(e));
		e = DOMUtils.getFirstChildElement(e);
		assertEquals(new QName(SoapMarshaler.SOAP_11_URI, SoapMarshaler.BODY),
				DOMUtil.getQName(e));
		e = DOMUtils.getFirstChildElement(e);
		assertEquals(new QName(SoapMarshaler.SOAP_11_URI, SoapMarshaler.FAULT),
				DOMUtil.getQName(e));

		this.server.remove(faultServlet);
	}

	// -----------------------------------------------------------------------
	public void testSoapXml() throws Exception {
		HttpSoapTest.envelopeName = new QName(SoapMarshaler.SOAP_12_URI,
				SoapMarshaler.ENVELOPE);
		HttpSoapTest.validateRequest = true;
		HttpSoapTest.useDomMarshaler = true;
		Object echoServlet = this.server.createContext(getListenURI(),
				SoapEchoServlet.class.getName(), new SslParameters(), null,
				null);
		PostMethod method = new PostMethod(getTargetURI());
		method.setRequestEntity(new InputStreamRequestEntity(Classes
				.getSystemResource("request.xml").openStream()));
		int state = new HttpClient().executeMethod(method);
		assertEquals(HttpServletResponse.SC_OK, state);

		this.server.remove(echoServlet);
	}

	// -----------------------------------------------------------------------
	public void testAttachments() throws Exception {
		HttpSoapTest.envelopeName = new QName(SoapMarshaler.SOAP_12_URI,
				SoapMarshaler.ENVELOPE);
		HttpSoapTest.validateRequest = false;
		HttpSoapTest.useDomMarshaler = false;
		Object echoServlet = this.server.createContext(getListenURI(),
				SoapEchoServlet.class.getName(), new SslParameters(), null,
				null);
		SoapMarshaler saxMarshaler = new SoapMarshaler(true, false);
		SoapMessage message = new SoapMessage();
		message.setDocument(new SourceTransformer()
				.toDOMDocument(new StringSource("<hello>world</hello>")));
		// Add attachment
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileUtil.copyInputStream(Classes.getSystemResource("servicemix.jpg")
				.openStream(), baos);
		DataSource ds = new ByteArrayDataSource(baos.toByteArray(),
				"image/jpeg");
		DataHandler dh = new DataHandler(ds);
		message.addAttachment("image", dh);
		// Send request
		SoapWriter writer = new SoapWriter(saxMarshaler, message);
		ByteArrayOutputStream requestEntity = new ByteArrayOutputStream();
		writer.write(requestEntity);
		requestEntity.close();
		PostMethod method = new PostMethod(getTargetURI());
		method.setRequestEntity(new InputStreamRequestEntity(
				new ByteArrayInputStream(requestEntity.toByteArray()), writer
						.getContentType()));
		int state = new HttpClient().executeMethod(method);
		// Validate reply
		assertEquals(HttpServletResponse.SC_OK, state);
		SoapReader reader = new SoapReader(saxMarshaler);
		message = reader.read(method.getResponseBodyAsStream(),
				"multipart/mime");
		assertEquals(1, message.getAttachments().size());

		this.server.remove(echoServlet);

	}

	public String getTargetURI() {
		return targetURI;
	}

	public String getListenURI() {
		return listenURI;
	}

}
