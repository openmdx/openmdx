/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ObjectHandler.java,v 1.2 2009/02/24 16:02:51 hburger Exp $
 * Description: Local Object Invocation Handler
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/24 16:02:51 $
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

import javax.ejb.EJBObject;

import org.openmdx.kernel.exception.BasicException;

/**
 * Local Object Invocation Handler
 */
class ObjectHandler
extends AbstractObjectHandler<HomeHandler>
{

    /**
     * Constructor
     * 
     * @param homeHandler
     */
    ObjectHandler(
        HomeHandler homeHandler
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
        if(GET_EJB_HOME.equals(method)) {
            return this.homeHandler.getHome();
        } if (IS_IDENTICAL.equals(method)) {
            return homeHandler.getHome().equals(((EJBObject)args[0]).getEJBHome());
        } else if (GET_HANDLE.equals(method)) {
            return new ObjectHandle(this.homeHandler.getHomeHandle());
        } else {
            return super.invoke(proxy, method, args);
        }
    }

    /**
     * EJBLocalHome getEJBLocalHome() 
     */
    protected final static Method GET_EJB_HOME = getMethod(
        EJBObject.class, 
        "getEJBHome"
    );

    /**
     * boolean isIdentical(EJBLocalObject obj)
     */
    private final static Method IS_IDENTICAL = getMethod(
        EJBObject.class, 
        "isIdentical", 
        EJBObject.class

    );

    /**
     * EJBLocalHome getEJBLocalHome() 
     */
    protected final static Method GET_HANDLE = getMethod(
        EJBObject.class, 
        "getHandle"
    );

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
        Object callerContext = this.homeHandler.setBeanContext();
        try {
            return new RemoteAction(
                this.homeHandler,
                this.homeHandler.getInstanceMethod(
                    methodName, 
                    getFormalClasses(argumentClassNames)
                ), 
                this.homeHandler.getTransactionAttribute(
                    "Remote", 
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
                new BasicException.Parameter("methodName", methodName),
                new BasicException.Parameter("argumentClasses", (Object[])argumentClassNames)
            );
        } catch (ClassNotFoundException exception) {
            throw BasicException.newStandAloneExceptionStack(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "The requested method is not supported by the given EJB instance",
                new BasicException.Parameter("object", this),
                new BasicException.Parameter("methodName", methodName),
                new BasicException.Parameter("argumentClasses", (Object[])argumentClassNames)
            );
        } finally {
            this.homeHandler.setCallerContext(callerContext);
        }
    }


    //------------------------------------------------------------------------
    // Class RemoteAction
    //------------------------------------------------------------------------

    /**
     * Remote Action
     */
    protected static class RemoteAction 
    extends AbstractAction<HomeHandler>
    implements Action
    {

        /**
         * Constructor 
         *
         * @param home
         * @param method
         * @param transactionAttribute
         */
        RemoteAction(
            HomeHandler home,
            Method method, 
            TransactionAttribute transactionAttribute
        ){
            super(home, method, transactionAttribute);
        }

        /* (non-Javadoc)
         * @see org.openmdx.kernel.application.container.spi.ejb.AbstractInvocationHandler.Action#invoke(java.lang.Object[])
         */
        public Object invoke(
            Object[] arguments
        ) throws Exception {
            InternalMethodInvocationArguments valueHolder = InternalMethodInvocationArguments.getInstance();
            valueHolder.put(arguments);
            Object callerContext = this.homeHandler.setBeanContext();
            try {
                RemoteTransactionContext transactionContext = new RemoteTransactionContext(
                    this.homeHandler.getTransactionManager(),
                    this.transactionAttribute
                );
                try {
                    valueHolder.put(delegateInvocation((Object[])valueHolder.get()));
                    transactionContext.end();
                } catch (BasicException exception) {
                    valueHolder.raise(transactionContext.end(exception));
                } catch (InvocationTargetException exception) {
                    transactionContext.end();
                    valueHolder.raise(exception.getCause());
                }                
            } finally {
                this.homeHandler.setCallerContext(callerContext);
            }
            return valueHolder.get();
        }

    }

}
