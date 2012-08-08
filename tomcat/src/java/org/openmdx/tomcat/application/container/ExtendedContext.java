/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ExtendedContext.java,v 1.3 2008/01/15 18:45:58 hburger Exp $
 * Description: Extended Context
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/15 18:45:58 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
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
package org.openmdx.tomcat.application.container;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.juli.logging.Log;

/**
 * Extended Context
 */
public class ExtendedContext extends StandardContext {

	/**
	 * @return the enterpriseApplication
	 */
	public final String getEnterpriseApplication() {
		return this.enterpriseApplication;
	}

	/**
	 * @param enterpriseApplication the enterpriseApplication to set
	 */
	public final void setEnterpriseApplication(
			String enterpriseApplication
	) {
		this.enterpriseApplication = enterpriseApplication;
	}

	/* (non-Javadoc)
	 * @see org.apache.catalina.core.StandardContext#start()
	 */
	@Override
	public synchronized void start() throws LifecycleException {
		Log log = getLogger();
		if(started) {
			log.info(logName() + " already started");
		} else {
			ApplicationContextFactory applicationContextFactory = null;
			Container host = getParent();
			if(host == null) {
				log.warn("Missing host for context " + logName());
			} else {
				Container engine = host.getParent();
				if(engine instanceof ExtendedEngine) {
					applicationContextFactory = ((ExtendedEngine)engine).getApplicationContextFactory();
				} else if (engine == null){
					log.warn("Missing engine for context " + getName());
				} else {
					log.warn("Engine for context " + getName() + " is not an instance of " + ExtendedEngine.class.getName());
				}
			}
			if(applicationContextFactory == null) {
				log.warn("Missing ApplicationContextFactory for context " + getName());
			} else {
				log.info("Using ApplicationContextFactory for context " + getName());
				ClassLoader applicationClasLoader = applicationContextFactory.getClassLoader(
						getEnterpriseApplication()
				);
				if(applicationClasLoader == null) {
					log.warn("Missing class loader for enterprise application " + getEnterpriseApplication());
				} else {
					log.info("Setting parent class loader for enterprise application " + getEnterpriseApplication() + " as parent class loader");
					setParentClassLoader(applicationClasLoader);
				}
			}
		}
		super.start();
	}


	/* (non-Javadoc)
	 * @see org.apache.catalina.core.StandardContext#processTlds()
	 */
	@Override
	protected void processTlds() throws LifecycleException {
		OverriddenTldConfig tldConfig = new OverriddenTldConfig();
		tldConfig.setContext(this);
		// (1)  check if the attribute has been defined
		//      on the context element.
		// (2) if the attribute wasn't defined on the context
		//     try the host.
		tldConfig.setTldValidation(
			getTldValidation() || (((StandardHost) getParent()).getXmlValidation()) 
		);
		tldConfig.setTldNamespaceAware(
			getTldNamespaceAware() || (((StandardHost) getParent()).getXmlNamespaceAware()) 
		);
		try {
			tldConfig.execute();
		} catch (Exception exception) {
			getLogger().error(
				"Error reading tld listeners " + exception.getMessage(), 
				exception
			); 
		}
	}

	/**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = -1975939746519030087L;

	/**
	 * The enterprise application id 
	 */
	private String enterpriseApplication;


}
