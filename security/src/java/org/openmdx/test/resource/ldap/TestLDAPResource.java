/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestLDAPResource.java,v 1.1 2007/11/26 14:04:35 hburger Exp $
 * Description: Class Loading Test
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/26 14:04:35 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.test.resource.ldap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import netscape.ldap.LDAPSearchResults;
import netscape.ldap.LDAPv3;

import org.openmdx.base.application.deploy.Deployment;
import org.openmdx.base.application.deploy.InProcessDeployment;
import org.openmdx.resource.ldap.cci.ConnectionFactory;

/**
 * Class Loading Test
 */
public class TestLDAPResource extends TestCase {

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	protected TestLDAPResource(
		String name
	){
		super(name);
	}  

	/**
	 * 
	 * @param args
	 */
	public static void main(
		String[] args
	){
		TestRunner.run(suite());
	}

	/**
	 * 
	 * @return
	 */
	public static Test suite(
	){
	    TestSuite suite = new TestSuite();
	    suite.addTest(new TestLDAPResource("open"));
	    suite.addTest(new TestLDAPResource("fake"));
	    return suite;
	}
	
	protected ConnectionFactory ldapConnectionFactory;

	/**
	 * 
	 */
	protected synchronized void setUp(
	) throws Exception {  
		this.ldapConnectionFactory = (ConnectionFactory) deployment.context().lookup(
			"ldap/" + getName()
		);
    }

	/**
	 * 
	 */
	protected void tearDown(
	) {
		deployment.destroy();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void runTest(
	) throws Exception {
		System.out.println("Test " + getName() + " started");
		for(
			int i = 0;
			i < 3;
			i++
		) ldapTest(i);
		System.out.println("Test " + getName() + " completed");
	}
	
	private void ldapTest(
		int iteration
	) throws Exception {
		LDAPv3 ldap = (LDAPv3) this.ldapConnectionFactory.getConnection();
		try {
			LDAPSearchResults result = ldap.search(
				"dc=openldap,dc=org", // base
				LDAPv3.SCOPE_SUB,
				"(&(o=openLDAP)(ou=Project))", // filter
				new String[]{
					"cn","sn","givenName","mail","st","l","ou"	
				}, 
				false // attrsOnly 
			);
			for(
				int i = 0;
				result.hasMoreElements();
				i++
			){
				System.out.println("" + iteration + "." + i + ": " + result.next());
			}
		} finally {
			ldap.disconnect();
		}
	}
	
    /**
     * Define whether deployment details should logged to the console
     */
    final private static boolean LOG_DEPLOYMENT_DETAIL = true;
        
    /**
     * 
     */
    protected final static Deployment deployment = 
        new InProcessDeployment(
    		new String[]{
				"file:src/connector/openldap.rar",
				"file:src/connector/fake-ldap.rar"
    		},
            null,
            LOG_DEPLOYMENT_DETAIL ? System.out : null,
            System.err
    );

}
