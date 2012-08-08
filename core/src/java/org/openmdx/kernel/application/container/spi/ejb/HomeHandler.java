/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: HomeHandler.java,v 1.5 2008/09/10 08:55:20 hburger Exp $
 * Description: Home Handler
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.application.container.spi.ejb;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.HomeHandle;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;

/**
 * Home Invocation Handler
 * <p>
 * This method handles stateless session beans only at the moment.
 */
public class HomeHandler
extends AbstractHomeHandler
implements HomeHandle
{

    /**
     * 
     */
    private static final long serialVersionUID = 6319742409921022896L;


    public HomeHandler (
        String ejbHomeClass,
        String ejbObjectClass
    ) throws RemoteException {
        this.ejbHomeClass = ejbHomeClass;
        this.ejbObjectClass = ejbObjectClass;
        this.objectHandler = new ObjectHandler(this);
    }

    /**
     * 
     */
    private final String ejbHomeClass;

    /**
     * 
     */
    private final String ejbObjectClass;

    /**
     * 
     */
    private final InvocationHandler objectHandler;


    //------------------------------------------------------------------------
    // Extends AbstractInvocationHandler
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] arguments
    ) throws Throwable {
        if (GET_HOME_HANDLE.equals(method)) {
            return this.getHomeHandle();
        } else if (
                "create".equals(method.getName()) &&
                method.getParameterTypes().length == 0
        ) {
            //
            // EJBLocalHome.create()
            //
            return Classes.newProxyInstance(
                this.objectHandler,
                EJBObject.class, Classes.getApplicationClass(this.ejbObjectClass)
            ); 
        } else {
            return super.invoke(proxy, method, arguments);
        }
    }

    /**
     * EJBHome's getHomeHandle() 
     */
    protected final static Method GET_HOME_HANDLE = getMethod(
        EJBHome.class, 
        "getHomeHandle"
    );

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.AbstractHomeHandler#getHome()
     */
    @Override
    public EJBHome getHome(
    ) throws RemoteException {
        try {
            return Classes.newProxyInstance(
                this,
                EJBHome.class, Classes.getApplicationClass(this.ejbHomeClass)
            );
        } catch (Exception exception) {
            throw new RemoteException(
                "Acquisition of the EJBHome instance failed",
                new BasicException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.SYSTEM_EXCEPTION,
                    "Could bot create proxy delegating to the home handler",
                    new BasicException.Parameter("ejbHome", ejbHomeClass),
                    new BasicException.Parameter("ejbObject", ejbObjectClass),
                    new BasicException.Parameter("homeHandler", this)
                )
            );
        } 
    }

    /**
     * Retrieve a home handle
     * 
     * @return a home handle
     * 
     * @throws RemoteException 
     */
    public HomeHandle getHomeHandle(
    ) throws RemoteException {
        return this;
    }


    //------------------------------------------------------------------------
    // Implements HomeHandle
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.ejb.HomeHandle#getEJBHome()
     */
    public EJBHome getEJBHome(
    ) throws RemoteException {
        return getHome();
    }

}
