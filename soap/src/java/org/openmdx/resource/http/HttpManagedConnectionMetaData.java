/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: HttpManagedConnectionMetaData.java,v 1.2 2007/03/22 15:32:52 wfro Exp $
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
import javax.resource.spi.ManagedConnectionMetaData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCA 1.5 : This class is a part of the implementation of the Connection
 * Management contract. Contains the static properties of Http Connections.
 * 
 */
public class HttpManagedConnectionMetaData implements ManagedConnectionMetaData {
	/**
	 * Max number of connections.
	 */
	private static final int MAX_CONNECTIONS = 10;

	/** Logger. * */
	private static final Logger LOG = LoggerFactory
			.getLogger(HttpManagedConnectionMetaData.class);

	/**
	 * Connection represented by these datas.
	 */
	private JCAHttpManagedConnection connection;

	/**
	 * Constructor.
	 * 
	 * @param newConnection
	 *            Connection represented by these datas.
	 */
	public HttpManagedConnectionMetaData(
			JCAHttpManagedConnection newConnection) {
		this.connection = newConnection;
	}

	/**
	 * @return OpenMDX
	 * @throws ResourceException
	 *             never throws this exception
	 * @see javax.resource.spi.ManagedConnectionMetaData#getEISProductName()
	 */
	public final String getEISProductName() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getEISProductName");
		}
		return "OpenMDX";
	}

	/**
	 * @return 2.x
	 * @throws ResourceException
	 *             never throws this exception
	 * @see javax.resource.spi.ManagedConnectionMetaData#getEISProductVersion()
	 */
	public final String getEISProductVersion() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getEISProductVersion");
		}
		return "2.x";
	}

	/**
	 * @return MAX_CONNECTIONS
	 * @throws ResourceException
	 *             never throws this exception
	 * @see javax.resource.spi.ManagedConnectionMetaData#getMaxConnections()
	 */
	public final int getMaxConnections() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getMaxConnections");
		}
		return MAX_CONNECTIONS;
	}

	/**
	 * @return connections user name
	 * @throws ResourceException
	 *             never throws this exception
	 * @see javax.resource.spi.ManagedConnectionMetaData#getUserName()
	 */
	public final String getUserName() throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getUserName");
		}
		return connection.getUserName();
	}

}
