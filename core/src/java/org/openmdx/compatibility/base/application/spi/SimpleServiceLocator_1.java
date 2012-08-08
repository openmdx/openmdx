/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SimpleServiceLocator_1.java,v 1.5 2008/03/21 18:45:22 hburger Exp $
 * Description: SimpleServiceLocator_1 class 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:45:22 $
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
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.naming.initial.ContextFactory;

/**
 * SimpleServiceLocator_1
 *
 * @deprecated in favour of {@linkplain javax.naming.InitialContext
 * Standard JNDI access}
 */
@SuppressWarnings("unchecked")
public class SimpleServiceLocator_1 
    extends JndiServiceLocator_1 
{

    /**
     * Constructor
     */
    public SimpleServiceLocator_1(){
        try {
            Hashtable environment = new Hashtable();
            environment.put(
                Context.INITIAL_CONTEXT_FACTORY,
                ContextFactory.class.getName()
            );
            this.initialContext = new InitialContext(environment);
        } catch (NamingException e) {
            throw new RuntimeServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                null,
                "Initial context creation failed"
            );
        }
    }

    //------------------------------------------------------------------------
    // Extends JndiServiceLocator_1
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#bind(java.lang.String, java.lang.Object)
     */
    public void bind(
        String registrationId, 
        Object object
    ) throws ServiceException {
        super.bind(toJndiName(registrationId), object);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#listBindings(java.lang.String)
     */
    public Enumeration listBindings(
        String registrationId
    ) throws ServiceException {
        return super.listBindings(toJndiName(registrationId));
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#lookup(java.lang.String)
     */
    public Object lookup(
        String registrationId
    ) throws ServiceException {
        return super.lookup(toJndiName(registrationId));
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#unbind(java.lang.String)
     */
    public void unbind(
        String registrationId
    ) throws ServiceException {
        super.unbind(toJndiName(registrationId));
    }

    /**
     * Make the registration id JNDI compliant
     */
    protected static String toJndiName(
        String registrationId
    ){
        return replace(
            replace(
                replace(
                    registrationId, 
                    "//", "!!"
                ),
                "::", ";;"
            ), 
            ":", "/"
        );
    }

    /**
     * Avoid the corresponding String's replace operation to be JRE 1.1 
     * compliant.
     * 
     * @param source
     * @param pattern
     * @param replacement
     * @return a new String with the pattern replaced
     */
    private static String replace(
        String source,
        String pattern,
        String replacement
    ){
        StringBuilder target = new StringBuilder(source);
        for (
            int i = source.length();
            (i = source.lastIndexOf(pattern,i-1)) >= 0;
        ){
            target.replace(i, i+pattern.length(), replacement);
        }
        return target.toString();
    }
    
	
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.spi.JndiServiceLocator_1#initialContext()
     */
    public Context initialContext(
    ){
    	return this.initialContext;
    }

    /**
     * Save the initialContext to avoid interferences with Standar Service Locator's
     * new InitialContext() calls.
     */
    private final Context initialContext;

}
