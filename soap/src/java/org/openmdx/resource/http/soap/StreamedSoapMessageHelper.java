/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: StreamedSoapMessageHelper.java,v 1.2 2007/03/22 15:32:53 wfro Exp $
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/22 15:32:53 $
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
package org.openmdx.resource.http.soap;

import java.io.PrintWriter;

/**
 * Helper class to be able to create Soap message headers and footers.
 * 
 */
public final class StreamedSoapMessageHelper {

	private static final String STREAM_HEADER_1 = "<soapenv:Envelope";

	private static final String STREAM_HEADER_2 = "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"";

	private static final String STREAM_HEADER_3 = "    xmlns:ws=\"http://ws.location.services.cardinal.com/\">";

	private static final String STREAM_HEADER_3_1 = "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";

	private static final String STREAM_HEADER_4 = "    <soapenv:Body>";

	private static final String STREAM_FOOTER_1 = "    </soapenv:Body>";

	private static final String STREAM_FOOTER_2 = "</soapenv:Envelope>";

	private StreamedSoapMessageHelper() {

	}

	/**
	 * @param writer
	 *            the writer on wich to write a header.
	 */
	public static void writeStreamHeader(PrintWriter writer) {
		writer.println(STREAM_HEADER_1);
		writer.println(STREAM_HEADER_2);
		writer.println(STREAM_HEADER_3);
		writer.println(STREAM_HEADER_3_1);
		writer.println(STREAM_HEADER_4);
		writer.flush();
	}

	/**
	 * This method does not close the writer.
	 * 
	 * @param writer
	 *            the writer on wich to write a footer.
	 */
	public static void writeStreamFooter(PrintWriter writer) {
		writer.println();
		writer.println(STREAM_FOOTER_1);
		writer.println(STREAM_FOOTER_2);
		writer.println();
	}
}
