/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractEJBObject.java,v 1.2 2009/02/24 16:02:51 hburger Exp $
 * Description: Abstract EJBObject implementation
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/24 16:02:51 $
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
 */
package org.openmdx.compatibility.kernel.application.container.spi.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Arrays;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.RemoveException;

import org.openmdx.kernel.application.container.spi.ejb.RemoteTransactionContext;
import org.openmdx.kernel.application.container.spi.ejb.TransactionAttribute;
import org.openmdx.kernel.exception.BasicException;


/**
 * Abstract EJBObject implementation
 */
public abstract class AbstractEJBObject 
	extends AbstractRemoteObject
    implements EJBObject 
{

    /**
     * Constructor
     * 
     * @param home the EJB's home
     * @throws RemoteException
     */
    protected AbstractEJBObject(
        AbstractEJBHome home
    ) throws RemoteException{
        this.home = home;
    }

    /**
     * The bean's home interface.
     */
    private AbstractEJBHome home;

    
    //------------------------------------------------------------------------
    // Implements EJBObject
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.ejb.EJBObject#getEJBHome()
     */
    public EJBHome getEJBHome() throws RemoteException {
        return this.home;
    }
    
    /**
     * This method must be overridden by a subclass unless it represents a
     * session bean.
     * 
     * @see javax.ejb.EJBObject#getPrimaryKey()
     */
    public Object getPrimaryKey() throws RemoteException {
        throw new RemoteException(this.home.toString() + " is a SessionBean");
    }
    
    /**
     * This method must be overridden by a subclass unless it represents a
     * stateless session bean.
     * 
     * @see javax.ejb.EJBObject#remove()
     */
    public void remove() throws RemoteException, RemoveException {
        //
    }

    /**
     * This method must be overridden by a subclass unless it represents a
     * session bean.
     * 
     * @see javax.ejb.EJBObject#isIdentical(javax.ejb.EJBObject)
     */
    public boolean isIdentical(EJBObject that) throws RemoteException {
        return this.getHandle().equals(that.getHandle());
    }
    
    
    //------------------------------------------------------------------------
    // Class Action
    //------------------------------------------------------------------------

    /**
     * Action factory
     * 
     * @param name
     * @param parameterTypes
     * 
     * @return the requested instance method
     * 
     * @throws RemoteException 
     */
    protected Action getAction(
        String name,
        Class<?>[] parameterTypes
    ) throws RemoteException {
        String[] parameters = new String[parameterTypes.length];
        for(
            int i = 0;
            i < parameters.length;
            i++
        ) parameters[i] = parameterTypes[i].getName();
        try {
            return newAction(
                this.home,
                this.home.getInstanceMethod(name, parameterTypes), 
                this.home.getTransactionAttribute(
                    "Remote", 
                    name,
                    parameters
                )
            );
        } catch (NoSuchMethodException exception) {
            throw new RemoteException(
                "Method not supported by bean instance: " +
                name + Arrays.asList(parameters).toString().replace('[','(').replace(']',')'),
                exception
            );
        }
    }

    /**
     * Action
     */
    protected interface Action {

        /**
         * Remote method invocation
         * 
         * @param arguments
         * 
         * @return the return value
         * 
         * @throws RemoteException
         * @throws InvocationTargetException
         */
        Object invoke(
            Object... arguments
        ) throws RemoteException, InvocationTargetException;
        
    }
    
    /**
     * Action factory
     *  
     * @param home
     * @param method
     * @param transactionAttribute
     * 
     * @return the requested action
     * 
     * @throws RemoteException
     */
    protected Action newAction(
        AbstractEJBHome home,
        Method method, 
        TransactionAttribute transactionAttribute
    ) throws RemoteException {
        return new StandardAction(
            home,
            method,
            transactionAttribute
        );
    }

    /**
     * Standard Action
     */
    static class StandardAction implements Action {

        StandardAction(
            AbstractEJBHome home,
            Method method, 
            TransactionAttribute transactionAttribute
        ){
            this.home = home;
            this.method = method;
            this.transactionAttribute = transactionAttribute;
        }
        
        private final AbstractEJBHome home;
        
        private final Method method;
        
        private final TransactionAttribute transactionAttribute;
        
        public Object invoke(
            Object... arguments
        ) throws RemoteException, InvocationTargetException {
            Object callerContext = this.home.setBeanContext();
            try {
                RemoteTransactionContext transactionContext = new RemoteTransactionContext(
                     this.home.getTransactionManager(),
                     this.transactionAttribute
                );
                try {
                    Object ejbInstance = this.home.acquireBeanInstance();
                    try {
                        Object reply = this.method.invoke(ejbInstance, arguments);
                        this.home.returnBeanInstance(ejbInstance);
                        transactionContext.end();
                        return reply;
                    } catch (Throwable throwable) {
                        this.home.invalidateBeanInstance(ejbInstance);
                        throw throwable;
                    }
                } catch (Throwable throwable) {
                    throw transactionContext.end(BasicException.toExceptionStack(throwable));
                }
            } finally {
                this.home.setCallerContext(callerContext);
            }
        }
                
    }
    
}
