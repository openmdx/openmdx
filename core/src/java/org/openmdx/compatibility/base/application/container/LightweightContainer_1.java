/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LightweightContainer_1.java,v 1.21 2008/03/26 19:29:57 hburger Exp $
 * Description: Application Framework 
 * Revision:    $Revision: 1.21 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/26 19:29:57 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 */
package org.openmdx.compatibility.base.application.container;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.Manageable_1_0;
import org.openmdx.compatibility.base.application.spi.AbstractApplicationContext_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer.Mode;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * A former implementation of a lightweight container.
 * <p>
 * @deprecated in favour of {@link 
 * org.openmdx.kernel.application.container.lightweight.LightweightContainer 
 * LightweightContainer} and its implementation classes.
 */
@SuppressWarnings("unchecked")
public class LightweightContainer_1
	implements Manageable_1_0
{ 

	/**
	 * Pass parameter as arguments.
	 * <p>
	 * This mthod allows the lightweight container's replacement during
	 * unit tests.
     * @deprecated
	 */
	public LightweightContainer_1(
		String id,
		Path[] connectorDeploymentUnits,
		Path[] providerDeploymentUnits
	) throws ServiceException {
		
	    LightweightContainer.getInstance(Mode.ENTERPRISE_APPLICATION_CONTAINER);
		this.connectorDeploymentUnits = connectorDeploymentUnits;
		this.providerDeploymentUnits = providerDeploymentUnits;

		new ApplicationContext_1(
			connectorDeploymentUnits.length > 0 ?
				connectorDeploymentUnits[0].get(8) :
			providerDeploymentUnits.length > 0 ?
				providerDeploymentUnits[0].get(8) :
				NOT_AVAILABLE,
			id
		);

		try {
			SysLog.info(
				CONNECTOR_DEPLOYMENT_UNITS, 
				Arrays.asList(this.connectorDeploymentUnits)
			);
			org.openmdx.compatibility.base.dataprovider.kernel.ConnectorContainer_1.deploy(
				this.connectorDeploymentUnits
			);
		} catch (Exception exception) {
			throw new ServiceException(
				exception,
			BasicException.Code.DEFAULT_DOMAIN,
			BasicException.Code.ACTIVATION_FAILURE,
				new BasicException.Parameter[]{
					new BasicException.Parameter(
						CONNECTOR_DEPLOYMENT_UNITS, 
						this.connectorDeploymentUnits
					)
				},
				"Establishment of connections failed"
			).log(); 
		}
		
		try {
			SysLog.info(
				PROVIDER_DEPLOYMENT_UNITS, 
				Arrays.asList(this.providerDeploymentUnits)
			);
			this.providerContainer = 
				new org.openmdx.compatibility.base.dataprovider.transport.none.DataproviderContainer_1();
			this.providerContainer.deploy(this.providerDeploymentUnits);
		} catch (RuntimeException exception) {
			throw new ServiceException(
				exception,
			BasicException.Code.DEFAULT_DOMAIN,
			BasicException.Code.ACTIVATION_FAILURE,
				new BasicException.Parameter[]{
					new BasicException.Parameter(
						CONNECTOR_DEPLOYMENT_UNITS, 
						this.providerDeploymentUnits
					)
				},
				"Establishment of providers failed"
			).log(); 
		}
	}		

	/**
	 * Allow dynamic class loading by passing the parameters as system
	 * property.
	 * @deprecated
	 */
	public LightweightContainer_1(
	) throws ServiceException {
		this(	
			createContainerId(),
			getDeploymentUnits(CONNECTOR_DEPLOYMENT_UNITS),
			getDeploymentUnits(PROVIDER_DEPLOYMENT_UNITS)
		);
	}

	 
	//------------------------------------------------------------------------
	// Implements Manageable_1_0
	//------------------------------------------------------------------------

	/**
     * The activate method initializes a layer or component.
	 * <p>
     * An activate() implementation of a subclass should be of the form:
     * <pre>
     *   {
	 *     super.activate();
	 *     local activation code...
     *   }
     * </pre>
     */
	public void activate (
	) throws Exception {
	    //
	}

	/**
     * The deactivate method releases a layer or component.
	 * <p>
     * A deactivate() implementation of a subclass should be of the form:
     * of the form:
     * <pre>
     *   {
	 *     local deactivation code...
	 *     super.deactivate();
     *   }
     * </pre>
     */
	public void deactivate (
	) throws Exception, ServiceException {
		this.providerContainer.undeploy(this.providerDeploymentUnits);
		org.openmdx.compatibility.base.dataprovider.kernel.ConnectorContainer_1.undeploy(
			this.connectorDeploymentUnits
		);
		this.providerContainer = null;
	}


	//------------------------------------------------------------------------
	// Instance members
	//------------------------------------------------------------------------
	
	/**
	 * The connector deployment units to be processed
	 */
	protected final Path[] connectorDeploymentUnits;
	 
	/**
	 * The provider deployment units to be processed
	 */
	protected final Path[] providerDeploymentUnits;
	 
	/**
	 * The provider container
	 */
	protected org.openmdx.compatibility.base.dataprovider.transport.none.DataproviderContainer_1 providerContainer;
		

	//------------------------------------------------------------------------
	// Class members
	//------------------------------------------------------------------------
	
	/**
	 * Allow the spcification of a comma separated list of deployment units
	 */
	private static Path[] getDeploymentUnits(
		String name
	){
		String deploymentUnits = System.getProperty(name);
		List result = new ArrayList();
		if (deploymentUnits != null) for (
			StringTokenizer tokenizer = new StringTokenizer(
				deploymentUnits,
				SEPARATOR
			);
			tokenizer.hasMoreTokens();
		) result.add(new Path(tokenizer.nextToken()));
		return Path.toPathArray(result);
	}
	 		
	/**
	 * Use the host name as container id
	 */
	private static String createContainerId(
	){
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (Exception exception) {
			return "localhost";
		}
	}

	/**
	 * Use this system property to define the connector deployment units to be
	 * deployed
	 */
	public final static String CONNECTOR_DEPLOYMENT_UNITS =
		"org.openmdx.compatibility.base.application.container.ConnectorDeploymentUnits";

	/**
	 * Use this system property to define the provider deployment units to be
	 * deployed
	 */
	public final static String PROVIDER_DEPLOYMENT_UNITS =
		"org.openmdx.compatibility.base.application.container.ProviderDeploymentUnits";

	/**
	 * The deployment unit paths are separated by commas.
	 */	
	public final static String SEPARATOR = ",";

	/**
	 * Indicator that some information is not available
	 */	
	public final static String NOT_AVAILABLE = "n/a";
	

        //------------------------------------------------------------------------
	// Classes
	//------------------------------------------------------------------------
	
	private static final class ApplicationContext_1 
	    extends AbstractApplicationContext_1
	    implements Manageable_1_0 
        {

		ApplicationContext_1(
			String domainName,
			String containerId
		){
			this.domainName = domainName;
			this.containerId = containerId;
		}
		
		public void activate(
		) throws java.lang.Exception {
		    //
		}

		public void deactivate(
		) throws java.lang.Exception {
		    //
		}

		public java.lang.String getDomainName(){
			return this.domainName;
		}

		public java.lang.String getContainerId(){
			return this.containerId;
		}

		private final String domainName;
		private final String containerId;
		
	}  	
        
        

}
