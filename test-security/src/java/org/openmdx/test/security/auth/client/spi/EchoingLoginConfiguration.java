/*
 * ====================================================================
 * Project:     openMDX/Security
 * Name:        $Id: EchoingLoginConfiguration.java,v 1.1 2009/03/11 16:32:33 hburger Exp $
 * Description: Echoing Login Configuration
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/11 16:32:33 $
 * ====================================================================
 *
 * Copyright (c) 2007, OMEX AG, Switzerland
 * All rights reserved.
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
 */
package org.openmdx.test.security.auth.client.spi;

import java.util.Collections;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/**
 * Echoing Login Configuration
 */
public class EchoingLoginConfiguration extends Configuration {

	/**
	 * Constructor
	 */
	public EchoingLoginConfiguration(
	) {
		Map<String,?> options = Collections.emptyMap();
		this.appConfigurationEntries = new AppConfigurationEntry[]{
			new AppConfigurationEntry(
				EchoingLoginModule.class.getName(),
				AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
				options
			)
		};
	}

	@Override
	public AppConfigurationEntry[] getAppConfigurationEntry(
		String name
	) {
		return this.appConfigurationEntries;
	}

	@Override
	public void refresh() {
	}

	/**
	 * The Remote Login Configuration Entries
	 */
	private final AppConfigurationEntry[] appConfigurationEntries;

}
