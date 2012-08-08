/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: JCAHttpResourceAdapterMetaData.java,v 1.1 2007/03/22 15:32:52 wfro Exp $
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

import javax.resource.cci.ResourceAdapterMetaData;

/**
 * Static informations about the PoX ResourceAdapter.
 * 
 */
public class JCAHttpResourceAdapterMetaData implements ResourceAdapterMetaData {
	/**
	 * Resource adapter corresponding to this meta data.
	 */
	private JCAHttpResourceAdapter resourceAdapter = null;

	/**
	 * Constructor.
	 * 
	 * @param adapter
	 *            Resource adapter corresponding to this meta data.
	 */
	public JCAHttpResourceAdapterMetaData(JCAHttpResourceAdapter adapter) {
		this.resourceAdapter = adapter;
	}

	/**
	 * @return resourceAdapter.getAdapterVersion
	 * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterVersion()
	 */
	public String getAdapterVersion() {
		return resourceAdapter.getAdapterVersion();
	}

	/**
	 * @return resourceAdapter.getAdapterVendorName
	 * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterVendorName()
	 */
	public String getAdapterVendorName() {
		return resourceAdapter.getAdapterVendorName();
	}

	/**
	 * @return resourceAdapter.getAdapterName
	 * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterName()
	 */
	public String getAdapterName() {
		return resourceAdapter.getAdapterName();
	}

	/**
	 * @return resourceAdapter.getAdapterShortDescription
	 * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterShortDescription()
	 */
	public String getAdapterShortDescription() {
		return resourceAdapter.getAdapterShortDescription();
	}

	/**
	 * @return resourceAdapter.getSpecVersion
	 * @see javax.resource.cci.ResourceAdapterMetaData#getSpecVersion()
	 */
	public String getSpecVersion() {
		return resourceAdapter.getSpecVersion();
	}

	/**
	 * @return resourceAdapter.getInteractionSpecsSupported
	 * @see javax.resource.cci.ResourceAdapterMetaData#getInteractionSpecsSupported()
	 */
	public String[] getInteractionSpecsSupported() {
		return resourceAdapter.getInteractionSpecsSupported();
	}

	/**
	 * @return resourceAdapter.supportsExecuteWithInputAndOutputRecord
	 * @see javax.resource.cci.ResourceAdapterMetaData#supportsExecuteWithInputAndOutputRecord()
	 */
	public boolean supportsExecuteWithInputAndOutputRecord() {
		return resourceAdapter.supportsExecuteWithInputAndOutputRecord();
	}

	/**
	 * @return resourceAdapter.supportsExecuteWithInputRecordOnly
	 * @see javax.resource.cci.ResourceAdapterMetaData#supportsExecuteWithInputRecordOnly()
	 */
	public boolean supportsExecuteWithInputRecordOnly() {
		return resourceAdapter.supportsExecuteWithInputRecordOnly();
	}

	/**
	 * @return resourceAdapter.supportsLocalTransactionDemarcation
	 * @see javax.resource.cci.ResourceAdapterMetaData#supportsLocalTransactionDemarcation()
	 */
	public boolean supportsLocalTransactionDemarcation() {
		return resourceAdapter.supportsLocalTransactionDemarcation();
	}

}
