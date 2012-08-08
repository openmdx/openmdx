/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: HttpConnectionSpec.java,v 1.2 2007/03/22 15:32:52 wfro Exp $
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

import javax.resource.cci.ConnectionSpec;

/**
 * OUTBOUND: This JavaBean contains all information needed to obtain a
 * connection .
 * 
 */
public class HttpConnectionSpec implements ConnectionSpec {

	/**
	 * URL to use when opening a new connection .
	 */
	private String url = null;

	/**
	 * URL to use when opening a new connection .
	 */
	private String factoryName = null;

	/**
	 * Constructor.
	 * 
	 * @param newUrl
	 *            new url to use for the connection
	 */
	public HttpConnectionSpec(String newUrl, String factoryName) {
		this.url = newUrl;
		this.factoryName = factoryName;
	}

	/**
	 * Getter for the URL used to open a new connection .
	 * 
	 * @return url
	 */
	public final String getUrl() {
		return url;
	}

	/**
	 * Setter for the URL used to open a new connection .
	 * 
	 * @param newUrl
	 *            the new url
	 */
	public final void setUrl(String newUrl) {
		this.url = newUrl;
	}

	/**
	 * @return the factoryName
	 */
	public final String getFactoryName() {
		return factoryName;
	}

	/**
	 * @param factoryName
	 *            the factoryName to set
	 */
	public final void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	/**
	 * @param obj
	 *            the object to test equality
	 * @return true if url, provider... are equals
	 */
	public final boolean equals(Object obj) {
		boolean booleanResult = false;
		if (obj instanceof HttpConnectionSpec) {
			HttpConnectionSpec req = (HttpConnectionSpec) obj;
			boolean ur = false;
			ur = (url == null && req.url == null)
					|| (url != null && url.equals(req.url));
			booleanResult = ur;
		}
		return booleanResult;
	}

	/**
	 * @return utl.hashCode
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		return url.hashCode();
	}

	/**
	 * @return the string reprensting this object
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		String result = this.getClass() + " [" + this.getUrl() + "]";
		return result;
	}

}
