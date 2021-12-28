/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Java Bean URL Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2021, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.naming.java;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactory;

/**
 * Java Bean URL Test
 */
public class JavaBeanURLTest {

	/**
	 * Test set-up
	 * 
	 * @throws NamingException
	 */
	@BeforeAll
	public static void setUp() throws NamingException {
	    System.setProperty(
	        Context.INITIAL_CONTEXT_FACTORY, 
	        NonManagedInitialContextFactory.class.getName()
	    );
		Map<String, String> javaBeans = new HashMap<String, String>();
		javaBeans.put(
			"org.openmdx.comp.env.bean",
			"_new:" + JavaBean.class.getName() + "?" +
			"string=aString&" +
			"integer=(java.lang.Integer)-1"
		);
        javaBeans.put(
            "org.openmdx.comp.env.foreign",
            "jndi:java:comp%2Fenv%2Fbean?" + Context.INITIAL_CONTEXT_FACTORY + '=' + NonManagedInitialContextFactory.class.getName()
        );
        javaURLContextFactory.populate(javaBeans);
	}
	
	private void lookupAndValidate(
	    String jndiName
	) throws NamingException {
        JavaBean javaBean = (JavaBean) new InitialContext().lookup(jndiName);
        Assertions.assertEquals("aString", javaBean.getString(), "javaBean.string");
        Assertions.assertEquals(Integer.valueOf(-1), javaBean.getInteger(), "javaBean.integer");
	    
	}

	/**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    @Test
    public void testNormal(
    ) throws Throwable {
        lookupAndValidate("java:comp/env/bean");
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    @Test
    public void testStrange(
    ) throws Throwable {
        lookupAndValidate("java:comp/env/foreign");
    }

}
