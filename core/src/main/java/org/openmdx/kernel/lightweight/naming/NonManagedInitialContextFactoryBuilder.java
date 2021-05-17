/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Non-Managed Context Factory Builder 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.naming;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

import org.openmdx.kernel.lightweight.naming.java.javaURLContextFactory;


/**
 * Non-Managed Context Factory Builder 
 */
public class NonManagedInitialContextFactoryBuilder implements InitialContextFactoryBuilder {

    /**
     * Constructor 
     * 
     * @throws NoInitialContextException 
     */
    private NonManagedInitialContextFactoryBuilder(
    ) throws NoInitialContextException {
        this.initialContextFactory = new NonManagedInitialContextFactory();
    }
    
    /**
     * The non-managed initial context factory
     */
    private final InitialContextFactory initialContextFactory;
    
    /**
     * Install the <code>NonManagedContextFactoryBuilder</code> singleton.
     * <p>
     * The <code>java:comp/env</code> environment may be populated either through
     * the <code>componentEnvironment</code> argument or through system properties.
     * 
     * @param componentEnvironment to initialize the <code>java:comp/env</code> 
     * environment.<p>
     * <code>java:comp/env/jdbc/MyDataSource</code> for example might be specified 
     * by an entry mapping <code>org.openmdx.comp.env.jdbc.MyDataSource</code>
     * to <code>jdbc:oracle:thin:@localhost:1521:XE?user=MyUserName&password=MyPassword&driver=oracle.jdbc.OracleDriver</code> 
     * 
     * @throws NamingException 
     */
    public static void install(
        Map<?,?> componentEnvironment
    ) throws NamingException {
        NamingManager.setInitialContextFactoryBuilder(
            new NonManagedInitialContextFactoryBuilder()
        );
        javaURLContextFactory.populate(componentEnvironment);
    }
        
    /* (non-Javadoc)
     * @see javax.naming.spi.InitialContextFactoryBuilder#createInitialContextFactory(java.util.Hashtable)
     */
    public InitialContextFactory createInitialContextFactory(
        Hashtable<?, ?> environment
    ) throws NamingException {
        return this.initialContextFactory;
    }
        
}
