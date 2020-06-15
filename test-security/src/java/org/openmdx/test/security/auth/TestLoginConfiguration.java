/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Test Login Configuration
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.security.auth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.openmdx.base.text.conversion.URLReader;
import org.openmdx.security.auth.login.configuration.URLConfiguration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestLoginConfiguration extends TestCase {

    /**
     * Constructor
     */
    public TestLoginConfiguration() {
        super();
    }

    /**
     * Constructor
     *
     * @param name
     */
    public TestLoginConfiguration(String name) {
        super(name);
    }

    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run(TestLoginConfiguration.suite());
    }

    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite(
    ) {
        return new TestSuite(TestLoginConfiguration.class);
    }

    private static final String[] urls = new String[]{
		"http://www.vcs.ethz.ch/fotos/treehouse/html/default.html",
		"http://www.w3.org"
	};

	private static final String[] historicalNames = new String[]{
		"ISO8859_1",
		"UTF8"
	};	
	
	/**
	 * Compares two AppConfigurationEntrys
	 * 
	 * @param message
	 * @param expected
	 * @param actual
	 */
	static public void assertEquals(
		String message,
		AppConfigurationEntry expected,
		AppConfigurationEntry actual
	){
		TestLoginConfiguration.assertEquals(
			message + ".loginModuleName",
			expected.getLoginModuleName(),
			actual.getLoginModuleName()
	    );
		TestLoginConfiguration.assertEquals(
			message + ".loginModuleName",
			expected.getControlFlag(),
			actual.getControlFlag()
	    );
		TestLoginConfiguration.assertEquals(
			message + ".options",
			expected.getOptions(),
			actual.getOptions()
	    );
	}

	public void testEncoding() throws MalformedURLException, IOException{
		for(int i = 0; i < TestLoginConfiguration.urls.length; i++){
			URL url = new URL(TestLoginConfiguration.urls[i]);
			try(URLReader urlReader = new URLReader(url)){
    			TestLoginConfiguration.assertEquals(TestLoginConfiguration.urls[i], TestLoginConfiguration.historicalNames[i], urlReader.getEncoding());
			}
		}
	}
	
	public void testLoginConfiguration(
	) throws IOException{
        Map<String,Object> sharedOptions = new HashMap<String,Object>();
        sharedOptions.put(
            "spiOption",
            Boolean.TRUE
        );
		Map<String,Object> kerberosOptions = new HashMap<String,Object>(sharedOptions);
		kerberosOptions.put(
			"ticketCache",
			System.getProperty("user.home") + System.getProperty("file.separator") + "tickets"
		);
		kerberosOptions.put(
			"useTicketCache",
			"true"
		);			
		AppConfigurationEntry[] expected = 	new AppConfigurationEntry[]{
			new AppConfigurationEntry(
				"com.sun.security.auth.module.UnixLoginModule",
				LoginModuleControlFlag.REQUIRED,
				sharedOptions
			),
			new AppConfigurationEntry(
				"com.sun.security.auth.module.Krb5LoginModule",
				LoginModuleControlFlag.OPTIONAL,
				kerberosOptions
			)
		};
        URLConfiguration localConfiguration = new URLConfiguration(
            new URL("xri://+resource/org/openmdx/test/security/auth/login.configuration"), 
            sharedOptions
        );
        System.out.println(localConfiguration);
		AppConfigurationEntry[] actual = localConfiguration.getAppConfigurationEntry(
			"Login"
		);
		//
		// AppConfigurationEntry's equal() method does not behave as expected...
		//
		for(
			int i = 0;
			i < expected.length;
			i++
		) {
			TestLoginConfiguration.assertEquals(
				"Login[" + i + "]",
				expected[i],
				actual[i]
		    );
		}
	}
	
}

