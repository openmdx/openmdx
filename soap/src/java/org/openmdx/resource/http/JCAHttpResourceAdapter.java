/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: JCAHttpResourceAdapter.java,v 1.1 2007/03/22 15:32:52 wfro Exp $
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

import java.io.PrintWriter;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is the main class of the ResourceAdapter. To have more information
 * read the ra.xml deployment descriptor. It is a outbound JCA 1.5 connector.
 * 
 */
public class JCAHttpResourceAdapter implements ResourceAdapter {

	/** Logger. */
	private static final Log LOG = LogFactory
			.getLog(JCAHttpResourceAdapter.class);

	/**
	 * Resource adapter context.
	 */
	private BootstrapContext context = null;

	/**
	 * Default url to use when opening a new connection .
	 */
	private String url = null;

	/**
	 * Default factory to use when opening a new connection .
	 */
	private String factoryName = null;

	/**
	 * METADATA INFORMATION.
	 */
	private String[] interactionSpecsSupported = { HttpInteraction.class
			.getName() };

	/**
	 * METADATA INFORMATION.
	 */
	private String version = "0.1";

	/**
	 * METADATA INFORMATION.
	 */
	private String adapterVendorName = "OpenMDX";

	/**
	 * Getter for the default URL used to open a new connection .
	 * 
	 * @return default url
	 */
	public final String getUrl() {
		return url;
	}

	/**
	 * Setter for the default URL used to open a new connection .
	 * 
	 * @param newUrl
	 *            the url
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
	 * @param factoryName the factoryName to set
	 */
	public final void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	/**
	 * @return spec supported
	 * @see javax.resource.cci.ResourceAdapterMetaData#getInteractionSpecsSupported()
	 */
	public final String[] getInteractionSpecsSupported() {
		return interactionSpecsSupported;
	}

	/**
	 * @return the resource adapter's version
	 * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterVersion()
	 */
	public final String getAdapterVersion() {
		return this.version;
	}

	/**
	 * @return vendro name
	 * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterVendorName()
	 */
	public final String getAdapterVendorName() {
		return adapterVendorName;
	}

	/**
	 * The Factory used by this Adapter to create new physical connections.
	 */
	private JCAHttpManagedConnectionFactory managedConnectionFactory = null;

	/**
	 * METADATA INFORMATION.
	 */
	private String adapterName = "OpenMDXHttpConnector";

	/**
	 * METADATA INFORMATION.
	 */
	private String adapterShortDescription = "A Http JCA Connector for OpenMDX";

	/**
	 * METADATA INFORMATION.
	 */
	private String specVersion = "0.1";

	/**
	 * METADATA INFORMATION.
	 */
	private boolean isSupportsExecuteWithInputAndOutputRecord = true;

	/**
	 * METADATA INFORMATION.
	 */
	private boolean isSupportsExecuteWithInputRecordOnly = true;

	/**
	 * METADATA INFORMATION.
	 */
	private boolean isSupportsLocalTransactionDemarcation = true;

	/**
	 * @return adapter name
	 * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterName()
	 */
	public final String getAdapterName() {
		return adapterName;
	}

	/**
	 * @return short description
	 * @see javax.resource.cci.ResourceAdapterMetaData#getAdapterShortDescription()
	 */
	public final String getAdapterShortDescription() {
		return this.adapterShortDescription;
	}

	/**
	 * @return spec version
	 * @see javax.resource.cci.ResourceAdapterMetaData#getSpecVersion()
	 */
	public final String getSpecVersion() {
		return specVersion;
	}

	/**
	 * @return true if it can supports input and output records
	 * @see javax.resource.cci.ResourceAdapterMetaData#supportsExecuteWithInputAndOutputRecord()
	 */
	public final boolean supportsExecuteWithInputAndOutputRecord() {
		return isSupportsExecuteWithInputAndOutputRecord;
	}

	/**
	 * @return true if it can supports input only
	 * @see javax.resource.cci.ResourceAdapterMetaData#supportsExecuteWithInputRecordOnly()
	 */
	public final boolean supportsExecuteWithInputRecordOnly() {
		return isSupportsExecuteWithInputRecordOnly;
	}

	/**
	 * @return true if it can supports local transactions
	 * @see javax.resource.cci.ResourceAdapterMetaData#supportsLocalTransactionDemarcation()
	 */
	public final boolean supportsLocalTransactionDemarcation() {
		return isSupportsLocalTransactionDemarcation;
	}

	/**
	 * JCA 1.5: Callback used by the J2EE Container at deployment time. Used to
	 * get a reference to the ResourceAdapter context and WorkManager
	 * 
	 * @param ctx
	 *            the new context
	 * @throws ResourceAdapterInternalException
	 *             if a problem occurs during startup
	 * @see javax.resource.spi.ResourceAdapter#start(javax.resource.spi.BootstrapContext)
	 */
	public final void start(BootstrapContext ctx)
			throws ResourceAdapterInternalException {
		LOG.debug("start");
		context = ctx;
		managedConnectionFactory = new JCAHttpManagedConnectionFactory();
		try {
			managedConnectionFactory.setResourceAdapter(this);
			managedConnectionFactory.setLogWriter(new PrintWriter(System.out));
		} catch (ResourceException e) {
			LOG.error("Resource Adapter start", e);
		}
	}

	/**
	 * JCA 1.5 : Callback used when the rar is undeployed or the server shuts
	 * down.
	 * 
	 * @see javax.resource.spi.ResourceAdapter#stop()
	 */
	public final void stop() {
		LOG.debug("stop");
	}

	/**
	 * JCA 1.5: INBOUND Callback used by the J2EE container when a
	 * MessageListener corresponding to the correct interface (see the
	 * deployment descriptor) is deployed and bound to the corresponding queue
	 * (see the deployment descriptor).
	 * 
	 * @param factory
	 *            The MessageFactory corresponding to the listener.
	 * @param spec
	 *            tells the ResourceAdapter on which informations this
	 *            MessageEndpointFactory is listening. Here it contains
	 *            information about opencrx server.
	 * @throws ResourceException
	 *             if an error occus during activation
	 * @see javax.resource.spi.ResourceAdapter#endpointActivation(javax.resource.spi.endpoint.MessageEndpointFactory,
	 *      javax.resource.spi.ActivationSpec)
	 */
	public final void endpointActivation(MessageEndpointFactory factory,
			ActivationSpec spec) throws ResourceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("endpointActivation(" + factory + "," + spec + ")");
		}
		throw new NotSupportedException("Operation not supported");
	}

	/**
	 * JCA 1.5: INBOUND Callback used by the J2EE Container when a Listener is
	 * undeployed.
	 * 
	 * @param factory
	 *            The factory undeployed.
	 * @param spec
	 *            The spec for which this factory has been listening
	 * @see javax.resource.spi.ResourceAdapter#endpointDeactivation(javax.resource.spi.endpoint.MessageEndpointFactory,
	 *      javax.resource.spi.ActivationSpec)
	 */
	public final void endpointDeactivation(MessageEndpointFactory factory,
			ActivationSpec spec) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("endpointDeactivation");
		}
	}

	/**
	 * This ResourceAdapter does not implements the Transaction Management
	 * Contract. It throws a new NotSupportedException
	 * 
	 * @param arg0
	 *            spec of ressources needed
	 * @return Nothing.
	 * @throws ResourceException
	 *             It throws a new NotSupportedException
	 * @see javax.resource.spi.ResourceAdapter#getXAResources(javax.resource.spi.ActivationSpec[])
	 */
	public final XAResource[] getXAResources(ActivationSpec[] arg0)
			throws ResourceException {
		LOG.debug("getXAResources");
		throw new NotSupportedException("Operation not supported");
	}

	/**
	 * @return context hascode
	 * @see java.lang.Object#hashCode()
	 */
	public final int hashCode() {
		LOG.debug("hashCode");
		return context.hashCode();
	}

	protected BootstrapContext getContext() {
		return context;
	}

	/**
	 * @param obj
	 *            the object to be compared to
	 * @return true if contexts are equals
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public final boolean equals(Object obj) {
		boolean resultBool = false;
		if (obj instanceof JCAHttpResourceAdapter) {
			resultBool = this.context
					.equals(((JCAHttpResourceAdapter) obj).context);
		}
		return resultBool;
	}

}
