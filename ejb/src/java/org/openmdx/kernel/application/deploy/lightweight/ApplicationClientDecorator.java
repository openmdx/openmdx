/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ApplicationClientDecorator.java,v 1.1 2009/01/12 12:49:23 wfro Exp $
 * Description: Application Client Decorator
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:23 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.deploy.lightweight;

import java.net.URL;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.openmdx.kernel.application.deploy.spi.Deployment;

/**
 * Application Client Decorator
 */
public class ApplicationClientDecorator 
    extends ModuleDecorator<Deployment.ApplicationClient>
    implements Deployment.ApplicationClient 
{

    public ApplicationClientDecorator(
        Deployment.ApplicationClient delegate
    ) {
        super(delegate);
    }

    public String getDisplayName() {
        return super.delegate.getDisplayName();
    }

    /**
     * @return
     */
    public String getCallbackHandler() {
        return super.delegate.getCallbackHandler();
    }

    /**
     * @return
     */
    public URL[] getModuleClassPath() {
        return super.delegate.getModuleClassPath();
    }

    /**
     * @return
     */
    public String getMainClass() {
        return super.delegate.getMainClass();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.ApplicationClient#populate(javax.naming.Context, java.util.Map)
     */
    public void populate(
        Context applicationClientContext, 
        Map<String,String> applicationClientEnvironment
    ) throws NamingException {
        super.delegate.populate(
            applicationClientContext,
            applicationClientEnvironment
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.ApplicationClient#deploy(javax.naming.Context, javax.naming.Reference)
     */
    public void deploy(
        Context containerContext, 
        Reference reference
    ) throws NamingException {
        super.delegate.deploy(containerContext, reference);
    }

}

