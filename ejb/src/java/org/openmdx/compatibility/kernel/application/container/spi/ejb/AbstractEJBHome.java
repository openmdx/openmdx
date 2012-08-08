/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractEJBHome.java,v 1.4 2009/08/25 17:23:06 hburger Exp $
 * Description: Abstract Enterprise Java Bean Home
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/08/25 17:23:06 $
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

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.TransactionAttributeType;
import javax.transaction.TransactionManager;

import org.openmdx.kernel.application.container.spi.ejb.ContainerTransaction;
import org.openmdx.kernel.application.container.spi.ejb.ContextSwitcher;
import org.openmdx.kernel.application.container.spi.ejb.HomeConfiguration;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.naming.initial.ContextFactory;
import org.openmdx.uses.org.apache.commons.pool.ObjectPool;

/**
 * Abstract Enterprise Java Bean Home
 */
@SuppressWarnings("serial")
public abstract class AbstractEJBHome
extends AbstractRemoteObject
implements HomeConfiguration, EJBHome 
{

    /**
     * Constructor
     * @throws RemoteException
     */
    protected AbstractEJBHome(
    ) throws RemoteException{
        super();
    }

    /**
     * The EJB instance pool
     */
    private ObjectPool instancePool = null;

    /**
     * Obtain a Bean instance from the pool.
     *
     * @return a method-ready Bean instance.
     * 
     * @exception   RemoteException
     *              if Bean instance acquisitioon fails
     */
    Object acquireBeanInstance(
    ) throws RemoteException {
        try {
            return this.instancePool.borrowObject();
        } catch (Exception exception) {
            throw new RemoteException(
                this.beanName + " instance acquisition failed", 
                exception
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
            BasicException failure = BasicException.newStandAloneExceptionStack(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.SYSTEM_EXCEPTION,
                "Enterprise Java Bean instance release failed",
                new BasicException.Parameter("name", this.beanName),
                new BasicException.Parameter("url", this.jndiURL)
            );
            SysLog.warning(failure.getMessage(), failure.getCause());
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
            BasicException failure = BasicException.newStandAloneExceptionStack(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.SYSTEM_EXCEPTION,
                "Enterprise Java Bean instance invalidation failed",
                new BasicException.Parameter("name", this.beanName),
                new BasicException.Parameter("url", this.jndiURL)
            );
            SysLog.warning(failure.getMessage(), failure.getCause());
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



    //------------------------------------------------------------------------
    // Implements EJBHome
    //------------------------------------------------------------------------

    /**
     * This method must be overridden by a subclass unless it represents a
     * session bean home.
     * 
     * @see javax.ejb.EJBHome#remove(java.lang.Object)
     */
    public void remove(Object obejct) throws RemoteException, RemoveException {
        throw new RemoteException(this.beanName + " is a Session Bean");
    }

    /**
     * This method must be overridden by a subclass unless it represents a
     * session bean home.
     * 
     * @see javax.ejb.EJBHome#remove(javax.ejb.Handle)
     */
    public void remove(Handle handle) throws RemoteException, RemoveException {
        throw new RemoteException(this.beanName + " is a Session Bean");
    }

    /* (non-Javadoc)
     * @see javax.ejb.EJBHome#getEJBMetaData()
     */
    public EJBMetaData getEJBMetaData() throws RemoteException {
        return null; //... 
    }

    /* (non-Javadoc)
     * @see javax.ejb.EJBHome#getHomeHandle()
     */
    public HomeHandle getHomeHandle() throws RemoteException {
        return new URLContextHomeHandle(this.jndiURL, ContextFactory.getProviderURL());
    }


    //------------------------------------------------------------------------
    // Implements Home
    //------------------------------------------------------------------------

    /**
     * The home's JNDI url
     */
    private String jndiURL = null;

    /**
     * The home's JNDI url
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
     * The EJB's container transaction evaluator
     */
    private ContainerTransaction containerTransaction = null;

    /**
     * Deploy an Enterprise Java Bean Home
     * @param beanName
     * @param jndiURL
     * @param contextSwitcher
     * @param instancePool
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
     * Undeploy an Enterprise Java Bean Home
     */
    public void deactivate(
    ){
        this.contextSwitcher = null;
        this.instancePool = null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.Home#getTransactionManager()
     */
    public TransactionManager getTransactionManager() {
        return this.transactionManager;
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
        Class<?>[] parameterTypes
    ) throws NoSuchMethodException{
        return this.instanceClass.getMethod(name, parameterTypes);
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.rmi.server.RemoteObject#toString()
     */
    public String toString() {
        return super.toString() + ": " + this.beanName;
    }


    //------------------------------------------------------------------------
    // Delegates to ContextSwitcher
    //------------------------------------------------------------------------

    /**
     * The EJB instance pool
     */
    private ContextSwitcher contextSwitcher = null;

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.ContextSwitcher#setBeanContext()
     */
    protected Object setBeanContext() {
        return this.contextSwitcher.setBeanContext();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.ContextSwitcher#setCallerContext(java.lang.Object)
     */
    protected void setCallerContext(Object callerContext) {
        this.contextSwitcher.setCallerContext(callerContext);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.HomeConfiguration#getHome()
     */
    public EJBHome getHome() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.HomeConfiguration#getLocalHome()
     */
    public EJBLocalHome getLocalHome() {
        return null;
    }


}
