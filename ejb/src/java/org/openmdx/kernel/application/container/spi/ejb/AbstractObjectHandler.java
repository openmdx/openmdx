/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractObjectHandler.java,v 1.3 2009/08/25 17:23:05 hburger Exp $
 * Description: Abstract Enterprise Java Bean Object Invocation Handler
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/08/25 17:23:05 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 *         
 */
package org.openmdx.kernel.application.container.spi.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.RemoveException;
import javax.ejb.TransactionAttributeType;

import org.openmdx.kernel.exception.BasicException;

/**
 * Abstract Enterprise Java Bean Object Invocation Handler
 */
abstract class AbstractObjectHandler<H extends AbstractHomeHandler>
    extends AbstractInvocationHandler 
{

    /**
     * Constructor
     */
    protected AbstractObjectHandler(
        H homeHandler
    ) {
        this.homeHandler = homeHandler;
    }

    /**
     * The object's EJBHome or EJBLocalHome
     */
    protected H homeHandler;


    //------------------------------------------------------------------------
    // Implements InvocationHandler
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        if(EJBLocalObject.class == declaringClass || EJBObject.class == declaringClass) {
            String methodName = method.getName().intern();
            if("getPrimaryKey" == methodName) throw new RemoveException(
                this + " is a session, not an entity bean"
            );
            if("remove" == methodName) {
                this.homeHandler = null;
                return null;
            }
        }
        return super.invoke(proxy, method, args);
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.rmi.server.RemoteObject#toString()
     */
    public String toString() {
        StringBuilder reply = new StringBuilder(
            getClass().getName()
        ).append(
            '@'
        ).append(
            Integer.toHexString(System.identityHashCode(this))
        );
        return this.homeHandler == null ?
            reply.append(" <removed>").toString() :
            reply.append(" created by ").append(this.homeHandler).toString();
    }

    
    //------------------------------------------------------------------------
    // Class AbstractAction
    //------------------------------------------------------------------------

    /**
     * Abstract Action
     */
    protected static class AbstractAction<H extends AbstractHomeHandler> {
    
        /**
         * Constructor 
         *
         * @param homeHandler
         * @param method
         * @param transactionAttribute
         */
        protected AbstractAction(
            H homeHandler,
            Method method, 
            TransactionAttributeType transactionAttribute
        ){
            this.homeHandler = homeHandler;
            this.method = method;
            this.transactionAttribute = transactionAttribute;
        }
        
        /**
         * 
         */
        protected final H homeHandler;

        /**
         * 
         */
        protected final Method method;
        
        /**
         * 
         */
        protected final TransactionAttributeType transactionAttribute;

        private BasicException.Parameter[] getParameters(
            Object ejbInstance,
            Object[] arguments
        ){
            return new BasicException.Parameter[] {
                new BasicException.Parameter("object", this),
                ejbInstance == null ?
                    new BasicException.Parameter("bean", "<invalidated>") :
                    new BasicException.Parameter("beanClass", ejbInstance.getClass().getName()),
                new BasicException.Parameter("methodName", this.method.getName()),
                new BasicException.Parameter("formalClasses", (Object[])getFormalTypes(this.method.getParameterTypes())),
                new BasicException.Parameter("actualClasses", (Object[])getActualTypes(arguments))
            };
        }

        /**
         * 
         * @throws BasicException, InvocationTargetException  
         * 
         */
		protected Object delegateInvocation (
            Object[] arguments
        ) throws BasicException, InvocationTargetException {
            Object beanInstance = this.homeHandler.acquireBeanInstance();
            try {
                return this.method.invoke(beanInstance, arguments);
            } catch (Error throwable) {
                this.homeHandler.invalidateBeanInstance(beanInstance);
                beanInstance = null;
                throw throwable;
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause();
                if(cause instanceof Exception && !(cause instanceof RuntimeException)) {    
                    throw exception;
                } else {
                    this.homeHandler.invalidateBeanInstance(beanInstance);
                    beanInstance = null;
                    throw BasicException.newStandAloneExceptionStack(
                        cause,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.SYSTEM_EXCEPTION,
                        "Enterprise Java Bean method delegation failure",
                        getParameters(beanInstance, arguments)
                    );
                }
            } catch (IllegalAccessException throwable) {
                throw BasicException.newStandAloneExceptionStack(
                    throwable,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.SYSTEM_EXCEPTION,
                    "Enterprise Java Bean method delegation failure",
                    getParameters(beanInstance, arguments)
                );
            } catch (IllegalArgumentException throwable) {
                throw BasicException.newStandAloneExceptionStack(
                    throwable,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.SYSTEM_EXCEPTION,
                    "Enterprise Java Bean method delegation failure",
                    getParameters(beanInstance, arguments)
                );
            } finally {
                if(beanInstance != null) {
                    this.homeHandler.returnBeanInstance(beanInstance);
                }
            }
            
        }
        
    }
    
}
