/*
 * ====================================================================
 * Project:     openMDX/Test SOAP, http://www.openmdx.org/
 * Name:        $Id: TestHttpConnectorsSoapXStream.java,v 1.1 2007/03/22 15:33:58 wfro Exp $
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

import org.openmdx.resource.http.soap.SoapXStreamConnectionObjectMarshallerFactory;

public class TestHttpConnectorsSoapXStream extends AbstractTestHttpConnectors {

	private final String targetURI = "http://localhost:8193/Service/";

	private final String listenURI = "http://localhost:8193/Service/";

	private static final String SOAP_CONNECTOR_URL = "file:src/ear/soap-connector.ear/soap-connector.rar";

	private static final String FACTORY_NAME = SoapXStreamConnectionObjectMarshallerFactory.class
			.getName();

	private static final String LOG_NAME = "SOAP-XSTREAM";

	private static final String CONNECTOR_NAME = "connector/soap";

	public static class SoapEchoServlet extends EchoServlet {

		private static final long serialVersionUID = -8606601272548406884L;

		@Override
		protected String getFactoryName() {
			return FACTORY_NAME;
		}

		@Override
		protected String getLogName() {
			return LOG_NAME;
		}
	}

	public static class SoapBenchServlet extends BenchServlet {

		private static final long serialVersionUID = -8606601272548406884L;

		@Override
		protected String getFactoryName() {
			return FACTORY_NAME;
		}

		@Override
		protected String getLogName() {
			return LOG_NAME;
		}
	}

	@Override
	protected String getConnectorURL() {
		return SOAP_CONNECTOR_URL;
	}

	@Override
	protected String getConnectorJNDIName() {
		return CONNECTOR_NAME;
	}

	@Override
	protected String getBenchServletClassName() {
		return SoapBenchServlet.class.getName();
	}

	@Override
	protected String getEchoServletClassName() {
		return SoapEchoServlet.class.getName();
	}

	@Override
	protected String getTargetURI() {
		return targetURI;
	}

	@Override
	protected String getListenURI() {
		return listenURI;
	}

	@Override
	public void streamedRecordTests(String connectorJNDIName,
			String servletClassName) throws Exception {

	}

}
