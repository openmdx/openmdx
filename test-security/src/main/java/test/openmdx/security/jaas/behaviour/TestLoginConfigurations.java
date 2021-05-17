/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: OTest Login Configurations
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
package test.openmdx.security.jaas.behaviour;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.junit.Test;

/**
 * Test Login Configurations
 */
public class TestLoginConfigurations {

	enum CorrelationId {
		Password,
		SystemAccount,
		UserAccount,
		InternalUser,
		ExternalUser
	}
	
	private Configuration configuration = new Configuration(){
		
		@Override
		public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
			if("appServer".equals(name)) {
				return new AppConfigurationEntry[]{
					new AppConfigurationEntry(
						DummyLoginModule.class.getName(),
						LoginModuleControlFlag.REQUISITE,
						Collections.singletonMap(DummyLoginModule.CORRELATION_ID, CorrelationId.Password)
					),
					new AppConfigurationEntry(
						DummyLoginModule.class.getName(),
						LoginModuleControlFlag.SUFFICIENT,
						Collections.singletonMap(DummyLoginModule.CORRELATION_ID, CorrelationId.SystemAccount)
					),
					new AppConfigurationEntry(
						DummyLoginModule.class.getName(),
						LoginModuleControlFlag.REQUIRED,
						Collections.singletonMap(DummyLoginModule.CORRELATION_ID, CorrelationId.UserAccount)
					),
					new AppConfigurationEntry(
						DummyLoginModule.class.getName(),
						LoginModuleControlFlag.REQUIRED,
						Collections.singletonMap(DummyLoginModule.CORRELATION_ID, CorrelationId.InternalUser)
					),
					new AppConfigurationEntry(
						DummyLoginModule.class.getName(),
						LoginModuleControlFlag.REQUIRED,
						Collections.singletonMap(DummyLoginModule.CORRELATION_ID, CorrelationId.ExternalUser)
					)
				};
			} else {
				throw new IllegalArgumentException("Unsupported login configuration " + name);
			}
		}
		
	};
	
	private void appServerLogin(
		Map<?,LoginModuleOutcome> outcome
    ) throws LoginException{
		LoginContext loginContext = new LoginContext(
			"appServer", 
			null, 
			new OutcomeCallbackHandler(outcome), 
			configuration
		);
		loginContext.login();
	}
	
	@Test
	public void testSpecialUser() throws LoginException{
		Map<CorrelationId, LoginModuleOutcome> outcome = new HashMap<CorrelationId, LoginModuleOutcome>();
		outcome.put(CorrelationId.Password, LoginModuleOutcome.SUCCESS);
		outcome.put(CorrelationId.SystemAccount, LoginModuleOutcome.SUCCESS);
		outcome.put(CorrelationId.UserAccount, LoginModuleOutcome.FAIL);
		outcome.put(CorrelationId.InternalUser, LoginModuleOutcome.FAIL);
		outcome.put(CorrelationId.ExternalUser, LoginModuleOutcome.IGNORE);
		appServerLogin(outcome);
	}
	
	@Test
	public void testInternalUser() throws LoginException{
		Map<CorrelationId, LoginModuleOutcome> outcome = new HashMap<CorrelationId, LoginModuleOutcome>();
		outcome.put(CorrelationId.Password, LoginModuleOutcome.SUCCESS);
		outcome.put(CorrelationId.SystemAccount, LoginModuleOutcome.IGNORE);
		outcome.put(CorrelationId.UserAccount, LoginModuleOutcome.SUCCESS);
		outcome.put(CorrelationId.InternalUser, LoginModuleOutcome.SUCCESS);
		outcome.put(CorrelationId.ExternalUser, LoginModuleOutcome.IGNORE);
		appServerLogin(outcome);
	}

	@Test
	public void testExternalUser() throws LoginException{
		Map<CorrelationId, LoginModuleOutcome> outcome = new HashMap<CorrelationId, LoginModuleOutcome>();
		outcome.put(CorrelationId.Password, LoginModuleOutcome.SUCCESS);
		outcome.put(CorrelationId.SystemAccount, LoginModuleOutcome.IGNORE);
		outcome.put(CorrelationId.UserAccount, LoginModuleOutcome.SUCCESS);
		outcome.put(CorrelationId.InternalUser, LoginModuleOutcome.IGNORE);
		outcome.put(CorrelationId.ExternalUser, LoginModuleOutcome.SUCCESS);
		appServerLogin(outcome);
	}

	@Test(expected=FailedLoginException.class)
	public void testUnknownUser() throws LoginException{
		Map<CorrelationId, LoginModuleOutcome> outcome = new HashMap<CorrelationId, LoginModuleOutcome>();
		outcome.put(CorrelationId.Password, LoginModuleOutcome.SUCCESS);
		outcome.put(CorrelationId.SystemAccount, LoginModuleOutcome.IGNORE);
		outcome.put(CorrelationId.UserAccount, LoginModuleOutcome.FAIL);
		outcome.put(CorrelationId.InternalUser, LoginModuleOutcome.IGNORE);
		outcome.put(CorrelationId.ExternalUser, LoginModuleOutcome.IGNORE);
		appServerLogin(outcome);
	}

	@Test
	public void testWhenOnly1RequiredModuleSucceedsThenLoginSucceeds() throws LoginException{
		Map<CorrelationId, LoginModuleOutcome> outcome = new HashMap<CorrelationId, LoginModuleOutcome>();
		outcome.put(CorrelationId.Password, LoginModuleOutcome.IGNORE);
		outcome.put(CorrelationId.SystemAccount, LoginModuleOutcome.IGNORE);
		outcome.put(CorrelationId.UserAccount, LoginModuleOutcome.IGNORE);
		outcome.put(CorrelationId.InternalUser, LoginModuleOutcome.IGNORE);
		outcome.put(CorrelationId.ExternalUser, LoginModuleOutcome.SUCCESS);
		appServerLogin(outcome);
	}

}
