/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LocalObjectHandler.java,v 1.3 2009/08/25 17:23:04 hburger Exp $
 * Description: Local Object Invocation Handler
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/08/25 17:23:04 $
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.TransactionAttributeType;

import org.openmdx.kernel.exception.BasicException;

/**
 * Local Object Invocation Handler
 */
class LocalObjectHandler<H extends EJBLocalHome>
extends AbstractObjectHandler<LocalHomeHandler<H>>
{

    /**
     * Constructor
     * 
     * @param homeHandler
     */
    LocalObjectHandler(
        LocalHomeHandler<H> homeHandler
    ){
        super(homeHandler);
    }

    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable{
        if(EJBLocalObject.class == method.getDeclaringClass()) {
            String methodName = method.getName().intern();
            if("getEJBLocalHome" == methodName) {
                return this.homeHandler.getLocalHome();
            } else if ("isIdentical" == methodName) {
                return this.homeHandler.getLocalHome().equals(((EJBLocalObject)args[0]).getEJBLocalHome());
            } else {
                throw new UnsupportedOperationException(methodName);
            }
        }
        return super.invoke(proxy, method, args);
    }

    /**
     * Action factory
     * 
     * @param methodName
     * @param argumentClasses
     * @param argumentClassNames
     * 
     * @return the requested instance method
     * 
     * @throws BasicException 
     */
	protected Action getAction(
        String methodName,
        Class<?>[] argumentClasses,
        String[] argumentClassNames
    ) throws BasicException {
        try {
            return new LocalAction<H>(
                    this.homeHandler,
                    this.homeHandler.getInstanceMethod(
                        methodName, 
                        argumentClasses
                    ), 
                    this.homeHandler.getTransactionAttribute(
                        "Local", 
                        methodName,
                        argumentClassNames
                    )
            );
        } catch (NoSuchMethodException exception) {
            throw BasicException.newStandAloneExceptionStack(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "The requested method is not supported by the given EJB instance",
                new BasicException.Parameter("object", this),
                new BasicException.Parameter("methodName", this),
                new BasicException.Parameter("argumentClasses", (Object[])argumentClassNames)
            );
        }
    }


    /**
     * Action
     */
    protected static class LocalAction<H extends EJBLocalHome> 
    extends AbstractAction<LocalHomeHandler<H>>
    implements Action
    {

        /**
         * Constructor 
         *
         * @param home
         * @param method
         * @param transactionAttribute
         */
        LocalAction(
            LocalHomeHandler<H> home,
            Method method, 
            TransactionAttributeType transactionAttribute
        ){
            super(home, method, transactionAttribute);
        }

        /* (non-Javadoc)
         * @see org.openmdx.kernel.application.container.spi.ejb.AbstractInvocationHandler.Action#invoke(java.lang.Object[])
         */
        public Object invoke(
            Object[] arguments
        ) throws Exception {
            Object callerContext = this.homeHandler.setBeanContext();
            try {
                LocalTransactionContext transactionContext = new LocalTransactionContext(
                    this.homeHandler.getTransactionManager(),
                    this.transactionAttribute
                );
                try {
                    Object reply = delegateInvocation(arguments);
                    transactionContext.end();
                    return reply;
                } catch (BasicException exception) {
                    throw transactionContext.end(exception);
                } catch (InvocationTargetException exception) {
                    Throwable throwable = exception.getCause();
                    transactionContext.end();
                    if(throwable instanceof Exception) {
                        throw (Exception)throwable;
                    } else {
                        throw BasicException.toExceptionStack(throwable);
                    }
                }                
            } finally {
                this.homeHandler.setCallerContext(callerContext);
            }
        }

    }

}