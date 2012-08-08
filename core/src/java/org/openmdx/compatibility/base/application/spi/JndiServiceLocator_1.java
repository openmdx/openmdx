/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JndiServiceLocator_1.java,v 1.8 2008/09/10 08:55:24 hburger Exp $
 * Description: JndiServiceLocator_1 class 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:24 $
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
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.compatibility.base.application.spi;

import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.Manageable_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.naming.Contexts;


/**
 * A JNDI based service locator
 * <p>
 * It's main features are<ul>
 * <li>NamingExceptions are mapped to ServiceExceptions
 * <li>Bind operations create intermediate contexts if necessary
 * </ul>
 * @deprecated in favour of {@link javax.naming.InitialContext
 * Standard JNDI access} 
 */
public class JndiServiceLocator_1
implements Manageable_1_0, org.openmdx.compatibility.base.application.cci.ServiceLocator_1_0
{ 

    /**
     * Allow dynamic class loading
     */
    protected JndiServiceLocator_1(
    ){
        super();
    }

    //------------------------------------------------------------------------
    // Implements Manageable_1_0
    //------------------------------------------------------------------------

    /**
     * The activate method initializes a layer or component.
     * <p>
     * An activate() implementation of a subclass should be of the form:
     * <pre>
     *   {
     *     super.activate();
     *     \u00ablocal activation code\u00bb
     *   }
     * </pre>
     */
    public void activate (
    ) throws Exception {
        //
    }

    /**
     * The deactivate method releases a layer or component.
     * <p>
     * A deactivate() implementation of a subclass should be of the form:
     * of the form:
     * <pre>
     *   {
     *     \u00ablocal deactivation code\u00bb
     *     super.deactivate();
     *   }
     * </pre>
     */
    public void deactivate (
    ) throws Exception {
        //
    }


    //------------------------------------------------------------------------
    // Implements ServiceLocator_1_0
    //------------------------------------------------------------------------

    /**
     * Gets or creates an initial context
     * 
     * @return  the service locator specific initial context
     */
    protected Context initialContext(
    ) throws NamingException {
        return new InitialContext();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#lookup(java.lang.String)
     */
    public Object lookup(
        String registrationId
    ) throws ServiceException {
        try {
            return initialContext().lookup(registrationId);
        } catch (NamingException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "No object bound to the given registrationId",
                new BasicException.Parameter("registrationId", registrationId)
            );
        }
    }		

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#bind(java.lang.String, java.lang.Object)
     */
    public void bind(
        String registrationId,
        Object object
    ) throws ServiceException {
        try {
            Contexts.bind(
                initialContext(),
                registrationId,
                object
            );
        } catch (NamingException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                "Could not bind object to the given registrationId",
                new BasicException.Parameter("registrationId", registrationId)
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#unbind(java.lang.String)
     */
    public void unbind(
        String registrationId
    ) throws ServiceException {
        try {
            initialContext().unbind(registrationId);
        } catch (NamingException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                "Unbind failed",
                new BasicException.Parameter("registrationId", registrationId)
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#listBindings(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Enumeration listBindings(
        String registrationId
    ) throws ServiceException {
        try {
            return initialContext().listBindings(registrationId);
        } catch (NamingException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                "Could not list bindings",
                new BasicException.Parameter("registrationId", registrationId)
            );
        }
    }

}
