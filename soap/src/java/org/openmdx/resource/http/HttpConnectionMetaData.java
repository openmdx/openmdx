/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: HttpConnectionMetaData.java,v 1.2 2007/03/22 15:32:52 wfro Exp $
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/22 15:32:52 $
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

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;

/**
 * Static information about Http connections.
 */
public class HttpConnectionMetaData implements ConnectionMetaData {
	/**
	 * The connection corresponding to this metat data information class.
	 */
	private JCAHttpConnection connection = null;

	/**
	 * Constructor.
	 * 
	 * @param newConnection
	 *            The connection corresponding to this metat data information
	 *            class.
	 */
	public HttpConnectionMetaData(JCAHttpConnection newConnection) {
		this.connection = newConnection;
	}

	/**
	 * @return OpenMDX
	 * @throws ResourceException
	 *             never throws this exception (we should use a RessourceBundle
	 *             and then throw an exception when error occurs trying to get
	 *             the information)
	 * @see javax.resource.cci.ConnectionMetaData#getEISProductName()
	 */
	public String getEISProductName() throws ResourceException {
		return "OpenMDX";
	}

	/**
	 * @return 2.x
	 * @throws ResourceException
	 *             never throws this exception (we should use a RessourceBundle
	 *             and then throw an exception when error occurs trying to get
	 *             the information)
	 * @see javax.resource.cci.ConnectionMetaData#getEISProductVersion()
	 */
	public String getEISProductVersion() throws ResourceException {
		return "2.x";
	}

	/**
	 * @return connection.getUser
	 * @throws ResourceException
	 *             never throws this exception (we should use a RessourceBundle
	 *             and then throw an exception when error occurs trying to get
	 *             the information) (non-Javadoc).
	 * @see javax.resource.cci.ConnectionMetaData#getUserName()
	 */
	public String getUserName() throws ResourceException {
		return this.connection.getUser();
	}
}
