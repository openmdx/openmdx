/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LightweightDeploymentFactory.java,v 1.2 2009/04/21 16:18:25 hburger Exp $
 * Description: Lightweight Deployment Manager
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/21 16:18:25 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.application.deploy.enterprise;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

import org.openmdx.kernel.Version;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer.Mode;

/**
 * Lightweight Deployment Factory
 */
public class LightweightDeploymentFactory implements DeploymentFactory {

	public synchronized DeploymentManager getDeploymentManager(
		String uri, 
		String username,
		String password
	) throws DeploymentManagerCreationException {
		LightweightContainer.Mode mode = getMode(uri);
		if(uri == null) {
			return null;
		}
		DeploymentManager manager = this.managers[mode.ordinal()];
		if(manager != null) {
			return manager;
		}
		return this.managers[mode.ordinal()] = new LightweightDeploymentManager(
			mode
		);
	}

	private final DeploymentManager[] managers = new DeploymentManager[
		LightweightContainer.Mode.values().length                                                          
	];                                                             

	private static final String[] URI_PREFIX = {
		"xri://ejb.openmdx.org*", // preferred
		"xri://@openmdx*(+lightweight)*" // deprecated	
	};
	
	public DeploymentManager getDisconnectedDeploymentManager(
		String uri
	) throws DeploymentManagerCreationException {
		return null;
	}

	public String getDisplayName() {
		return "openMDX Lightweight Container Deployment Manager";
	}

	public String getProductVersion() {
		return Version.getSpecificationVersion();
	}

	static LightweightContainer.Mode getMode(
		String uri
	){
		if(uri != null) try {
			for (String uriPrefix : URI_PREFIX) {
				if(uri.toLowerCase().startsWith(uriPrefix)) {
					return Mode.valueOf(
						uri.substring(uriPrefix.length()).toUpperCase()
					);
				}
			}
		} catch (Exception exception) {
			// Tell the caller that we are not going to handle the uri
		}
		return null;
	}
	
	public boolean handlesURI(String uri) {
		return getMode(uri) != null;
	}

}
