/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ClassloaderContextTestCase.java,v 1.1 2009/04/03 15:08:16 hburger Exp $
 * Description: ClassloaderContextTestCase
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/03 15:08:16 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package test.openmdx.kernel.naming;

import java.util.Date;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.openmdx.kernel.naming.component.java.ComponentContextFactory;
import org.openmdx.kernel.naming.component.java.javaURLContextFactory;

/**
 * ClassloaderContextTestCase
 */
public class ClassloaderContextTestCase extends TestCase {
    
    private ClassLoader savedClassLoader;
    
    protected ClassLoader applicationClassLoader;
    
    protected Context compContext;
    
    protected Context javaContext; 
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        javaURLContextFactory javaContextFactory = new javaURLContextFactory();
        savedClassLoader = Thread.currentThread().getContextClassLoader();
        applicationClassLoader = savedClassLoader == null ?
            new ContextClassLoader() :
            new ContextClassLoader(savedClassLoader);
        javaContext = (Context) javaContextFactory.getObjectInstance(null, null, null, null);
        Hashtable<String, Object> environment = new Hashtable<String, Object>();
        environment.put(
            ClassLoader.class.getName(),
            applicationClassLoader
        );
        Context compContext = new ComponentContextFactory().getInitialContext(environment); 
        compContext.bind("today", new Date());
        super.setUp();
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        Thread.currentThread().setContextClassLoader(savedClassLoader);
        super.tearDown();
    }

    /**
     * Method testClassLoaderContext
     *
     * @throws NamingException
     */
    public void testClassLoaderContext() throws NamingException {
        Context initialContext = new InitialContext();
        Context javaContext = (Context) initialContext.lookup("java:");
        try {
            javaContext.lookup("comp");
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
        }
        
        Thread.currentThread().setContextClassLoader(
            new ContextClassLoader(applicationClassLoader)
        );
        Context compContext0 = (Context) javaContext.lookup("comp");
        Date today0 = (Date) compContext0.lookup("today");
        Context compContext1 = (Context) initialContext.lookup("java:comp");
        Date today1 = (Date) compContext1.lookup("today");
        assertSame("Bound Object", today0, today1);
        assertNotSame("Delegating Context", compContext0, compContext1);
        assertEquals("Delegating Context", compContext0, compContext1);
    }
    
    /**
     * ContextClassLoader
     * 
     * Subclass required as the ClassLoader's constructors are protected.
     */
    private static final class ContextClassLoader extends ClassLoader {        
        
        /**
         * Constructor
         */
        ContextClassLoader() {
            super();
        }

        /**
         * Constructor
         * 
         * @param parent
         */
        ContextClassLoader(ClassLoader parent) {
            super(parent);
        }
        
    }
    
    

}
