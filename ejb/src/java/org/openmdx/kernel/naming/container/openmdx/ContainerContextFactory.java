/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ContainerContextFactory.java,v 1.1 2009/01/12 12:49:24 wfro Exp $
 * Description: Container Context Factory
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:24 $
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
package org.openmdx.kernel.naming.container.openmdx;

import javax.naming.Context;

import org.openmdx.kernel.naming.spi.ClassLoadertContextFactory;

/**
 * Container Context Factory
 */
public class ContainerContextFactory extends ClassLoadertContextFactory {

    /**
     * Constructor for the <code>openmdx:application</code> context. 
     */
    public ContainerContextFactory(
    ) {
        super(openmdxURLContextFactory.APPLICATION_CONTEXT);
    }

    /**
     * Constructor the <code>openmdx:</code>&lsaquo;contextName&rsaquo; context.
     * 
     * @param contextName 
     */
    public ContainerContextFactory(
        String contextName
    ) {
        super(contextName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.naming.spi.AbstractContextFactory#getInitialContext(java.lang.String, java.lang.ClassLoader, java.lang.String)
     */
    @Override
    protected Context getSubContext(
        String name,
        ClassLoader classLoader,
        String uri
    ) {
        return openmdxURLContextFactory.createContext(name, classLoader);
    }

}
