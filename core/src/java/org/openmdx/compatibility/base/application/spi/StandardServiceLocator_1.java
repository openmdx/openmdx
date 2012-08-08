/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StandardServiceLocator_1.java,v 1.5 2008/03/21 18:45:22 hburger Exp $
 * Description: Service Locator Implementaion
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

import org.openmdx.base.exception.ServiceException;

/**
 * This service locator allowas native registration and lookup as well as
 * JNDI lookups.
 * <p>
 * @deprecated in favour of {@link javax.naming.InitialContext
 * Standard JNDI access}
 */
public class StandardServiceLocator_1
    extends JndiServiceLocator_1
{ 

    /**
     * Allow dynamic class loading
     */
    public StandardServiceLocator_1(
    ){
        StandardServiceLocator_1.instance = this;
    }

    /**
     * Get the most recently created StandardServiceLocator_1 instance.
     * <p>
     * This method never returns null.
     * 
     * @return the most recently created StandardServiceLocator_1 instance
     */
    public static StandardServiceLocator_1 getInstance(
    ){
        if(!hasInstance()) new StandardServiceLocator_1();
        return StandardServiceLocator_1.instance;
    }

    /**
     * Tells whether we are in an applicatioon server environment
     * 
     * @return true if a StandardServiceLocator_1 instance has been created
     */
    public static boolean hasInstance(){
        return StandardServiceLocator_1.instance != null;
    }
        
    /**
     * The standard service locator singleton
     */
    private static StandardServiceLocator_1 instance = null;

    
    //------------------------------------------------------------------------
    // Implements ServiceLocator_1_0
    //------------------------------------------------------------------------
        
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#lookup(java.lang.String)
     */
    public Object lookup(
    	String registrationId
    ) throws ServiceException {
        return getDelegate(registrationId).lookup(registrationId);
    }		

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#bind(java.lang.String, java.lang.Object)
     */
    public void bind(
    	String registrationId,
    	Object object
    ) throws ServiceException {
        getDelegate(registrationId).bind(
            registrationId,
            object
	);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#unbind(java.lang.String)
     */
    public void unbind(
    	String registrationId
    ) throws ServiceException {
        getDelegate(registrationId).unbind(registrationId);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.ServiceLocator_1_0#listBindings(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Enumeration listBindings(
    	String registrationId
    ) throws ServiceException {
        return getDelegate(registrationId).listBindings(registrationId);
    }

    /**
     * Checks whether therequest shall be delegated to a foreign JNDI implementation
     * or the SimpleServiceLocator_1. 
     */
    private static org.openmdx.compatibility.base.application.cci.ServiceLocator_1_0 getDelegate(
        String registrationId
    ){
        return registrationId.indexOf('/') >= 0 ? 
            foreignServiceLocator : 
        	org.openmdx.compatibility.base.application.container.SimpleServiceLocator.getInstance();
            
                
    }
    
    /**
     * Delegate to a foreign JNDI implementation by calling InitialContext() for each call.
     */
    final static private org.openmdx.compatibility.base.application.cci.ServiceLocator_1_0 foreignServiceLocator = new JndiServiceLocator_1();
    
}
