/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractHomeHandler.java,v 1.6 2011/06/21 22:54:53 hburger Exp $
 * Description: Abstract Enterprise Java Bean Home Invocation Handler
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/06/21 22:54:53 $
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

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.logging.Level;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.TransactionAttributeType;
import javax.transaction.TransactionManager;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.uses.org.apache.commons.pool.ObjectPool;

/**
 * Abstract Enterprise Java Bean Home Invocation Handler
 */
abstract class AbstractHomeHandler 
extends AbstractInvocationHandler 
implements HomeConfiguration
{

    /**
     * Constructor
     */
    protected AbstractHomeHandler() {
        super();
    }

    /**
     * The EJB instance pool
     */
    private ObjectPool instancePool = null;

    /**
     * The EJB's container transaction evaluator
     */
    private ContainerTransaction containerTransaction = null;

    /**
     * Obtain a Bean instance from the pool.
     *
     * @return a method-ready Bean instance.
     * 
     * @exception   BasicException
     *              if Bean instance acquisition fails
     */
	Object acquireBeanInstance(
    ) throws BasicException {
        try {
            return this.instancePool.borrowObject();
        } catch (Exception exception) {
            throw BasicException.newStandAloneExceptionStack(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.SYSTEM_EXCEPTION,
                "Enterprise Java Bean instance acquisition failed",
                new BasicException.Parameter("name", this.beanName),
                new BasicException.Parameter("url", this.jndiURL)
            );
        }        
    }

    /**
     * Return a Bean instance to the pool.
     *
     * @param ejbInstance an {@link #acquireBeanInstance acquired} Bean instance to be returned.
     */
    void returnBeanInstance(
        Object ejbInstance
    ){
        try {
            this.instancePool.returnObject(ejbInstance);
        } catch (Exception exception) {
            SysLog.log(
            	Level.WARNING,
                "Enterprise Java Bean release failure: " + this,
                exception
            );
        }        
    }

    /**
     * Invalidates a Bean object from the pool.
     *
     * @param ejbInstance an {@link #acquireBeanInstance acquired} Bean instance to be invalidated.
     */
    void invalidateBeanInstance(
        Object ejbInstance
    ){
        try {
            this.instancePool.invalidateObject(ejbInstance);
        } catch (Exception exception) {
            SysLog.log(
            	Level.WARNING,
                "Enterprise Java Bean invalidation failure: " + this,
                exception
            );
        }        
    }

    /**
     * 
     */
    TransactionAttributeType getTransactionAttribute(
        String methodInterface, 
        String methodName, 
        String[] methodParameters
    ) {
        return this.containerTransaction.getTransactionAttribute(
            methodInterface,
            methodName, 
            methodParameters
        );
    }

    /**
     * Retrieve an instance method
     * 
     * @param name
     * @param parameterTypes
     * 
     * @return the requested instance method
     * 
     * @throws NoSuchMethodException
     */
    Method getInstanceMethod (
        String name,
        Class<?>... parameterTypes
    ) throws NoSuchMethodException{
        return this.instanceClass.getMethod(name, parameterTypes);
    }


    //------------------------------------------------------------------------
    // Implements InvocationHandler
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
	public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable {
        Class<?> methodSource = method.getDeclaringClass();
        if(
                "remove".equals(method.getName()) && 
                (methodSource == EJBHome.class || methodSource == EJBLocalHome.class)
        ) {
            throw new RemoveException(
                this + " is a session, bot an entity bean"
            );
        } else {
            return super.invoke(proxy, method, args);
        }
    }

    //------------------------------------------------------------------------
    // Implements HomeConfiguration
    //------------------------------------------------------------------------

    /**
     * The home's JNDI url
     */
    private String jndiURL = null;

    /**
     * The EJB's name
     */
    private String beanName = null;

    /**
     * The container's transaction manager
     */
    private TransactionManager transactionManager = null;

    /**
     * The EJB instance class
     */
    private Class<SessionBean> instanceClass = null;

    /**
     * Activate an Enterprise Java Bean Home
     * @param beanName
     * @param jndiURL
     * @param contextSwitcher
     * @param instanceClass 
     * @param instancePool
     * @param containerTransaction
     * @param transactionManager 
     */
    public void initialize(
        String beanName,
        String jndiURL,
        ContextSwitcher contextSwitcher,
        Class<SessionBean> instanceClass, 
        ObjectPool instancePool, 
        ContainerTransaction containerTransaction, 
        TransactionManager transactionManager
    ){
        this.beanName = beanName;
        this.jndiURL = jndiURL;
        this.contextSwitcher = contextSwitcher;
        this.instanceClass = instanceClass;
        this.instancePool = instancePool;
        this.containerTransaction = containerTransaction;
        this.transactionManager = transactionManager;
    }

    /**
     * Create a local home instance
     * 
     * @return a new local home instance
     */
    public EJBLocalHome getLocalHome(
    ){
        return null;
    }

    /**
     * Create a home instance
     * @return a new home instance
     * 
     * @throws RemoteException 
     */
    public EJBHome getHome(
    ) throws RemoteException {
        return null;
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
        return null;
    }

    TransactionManager getTransactionManager() {
        return this.transactionManager;
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.rmi.server.RemoteObject#toString()
     */
    @Override
	public String toString() {
        return new StringBuilder(
            getClass().getName()
        ).append(
            '@'
        ).append(
            Integer.toHexString(System.identityHashCode(this))
        ).append(
            ": "
        ).append(
            this.beanName
        ).toString();
    }


    //------------------------------------------------------------------------
    // Implements ContextSwitcher
    //------------------------------------------------------------------------

    /**
     * The EJB instance pool
     */
    private ContextSwitcher contextSwitcher = null;

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.ContextSwitcher#
     */
    Object setBeanContext() {
        return this.contextSwitcher.setBeanContext();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.ContextSwitcher#setCallerContext(java.lang.Object)
     */
    void setCallerContext(Object callerContext) {
        this.contextSwitcher.setCallerContext(callerContext);
    }

}
