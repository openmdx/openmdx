/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: HttpConnectionRequestInfo.java,v 1.2 2007/03/22 15:32:52 wfro Exp $
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

import javax.resource.spi.ConnectionRequestInfo;

/**
 * JCA 1.5 : JavaBean contains informations used to connect to a Server.
 */
public class HttpConnectionRequestInfo implements ConnectionRequestInfo {

	/**
	 * Url to use when opening a new connection.
	 */
	private String url = null;

	/**
	 * The name of the factory used to construct ObjectMarshaller.
	 */
	private String factoryName = null;

	/**
	 * Getter for the URL used to open a new connection.
	 * 
	 * @return url
	 */
	public final String getUrl() {
		return url;
	}

	/**
	 * Setter for the URL used to open a new connection.
	 * 
	 * @param newUrl
	 *            the new url
	 */
	public final void setUrl(String newUrl) {
		this.url = newUrl;
	}

	public final String getFactoryName() {
		return factoryName;
	}

	public final void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	/**
	 * Constructor.
	 * 
	 * @param newUrl
	 *            the url to use for the connection
	 */
	public HttpConnectionRequestInfo(String newUrl, String newFactoryName) {
		this.url = newUrl;
		this.factoryName = newFactoryName;
	}

	/**
	 * @return a string containing all attributes of the class and the class
	 *         name
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		String result = this.getClass() + " [" + this.getUrl() + "]";
		return result;
	}

	/**
	 * @param obj
	 *            objects to test
	 * @return true if ids are equals
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public final boolean equals(Object obj) {
		boolean resultBool = false;
		if (obj instanceof HttpConnectionRequestInfo) {
			resultBool = id == ((HttpConnectionRequestInfo) obj).id;
		}
		return resultBool;
	}

	/**
	 * identifies an instance of HttpConnectionRequestInfo.
	 */
	private static int counter;

	/**
	 * The unique identifier of the object.
	 */
	private int id;

	/**
	 * A lock on the object, used to have unique instance identifiers.
	 */
	private static final Object LOCK = new Object();
	{
		synchronized (LOCK) {
			id = counter++;
		}
	}

	/**
	 * @return the id
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		return id;
	}
}
