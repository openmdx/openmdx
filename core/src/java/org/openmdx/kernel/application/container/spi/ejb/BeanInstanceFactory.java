/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: BeanInstanceFactory.java,v 1.16 2008/01/25 08:24:50 hburger Exp $
 * Description: Bean Instance Factory
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/25 08:24:50 $
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
package org.openmdx.kernel.application.container.spi.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.application.deploy.spi.Deployment;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory;

/**
 * Bean Instance Factory
 */
public class BeanInstanceFactory
    implements PoolableObjectFactory, ContextSwitcher 
{

    /**
     * Constructor 
     *
     * @param beanClassLoader
     * @param bean
     * @param sessionContext
     * @param contextSwitcher 
     * 
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public BeanInstanceFactory(
        ClassLoader beanClassLoader,
        Deployment.SessionBean bean, 
		SessionContext sessionContext, 
		ContextSwitcher contextSwitcher
    ) throws ClassNotFoundException, SecurityException, NoSuchMethodException{
        this.beanClassLoader = beanClassLoader;
        this.sessionContext = sessionContext;
        this.externalContextSwitcher = contextSwitcher;
        Object callerContext = setBeanContext();
        try {
            this.instanceClass = Classes.getApplicationClass(
                bean.getEjbClass()
            );
            this.ejbCreate = this.instanceClass.getMethod("ejbCreate", (Class[])null);
        } finally {
            setCallerContext(callerContext);
        }
    }

    /**
     * @return Returns the instanceClass.
     */
    public Class<SessionBean> getInstanceClass() {
        return this.instanceClass;
    }

    
    //------------------------------------------------------------------------
    // Implements PoolableObjectFactory
    //------------------------------------------------------------------------

    /**
     * 
     */
    final Class<SessionBean> instanceClass;
    
    /**
     * 
     */
    final Method ejbCreate;

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#makeObject()
     */
    public Object makeObject() throws Exception {
        Object callerContext = setBeanContext();
        try {
            SessionBean sessionBean = this.instanceClass.newInstance();
            sessionBean.setSessionContext(this.sessionContext);
            this.ejbCreate.invoke(sessionBean, (Object[])null);
            return sessionBean;
        } catch (InvocationTargetException exception) {
            throw new BasicException(
                exception.getTargetException(),
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                null,
                "EJB instance creation failed"
            );
        } finally {
            setCallerContext(callerContext);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#destroyObject(java.lang.Object)
     */
    public void destroyObject(Object obj) throws Exception {
       ((SessionBean)obj).ejbRemove();
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#validateObject(java.lang.Object)
     */
    public boolean validateObject(Object obj) {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#activateObject(java.lang.Object)
     */
    public void activateObject(Object obj) throws Exception {
        //
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#passivateObject(java.lang.Object)
     */
    public void passivateObject(Object obj) throws Exception {
        //
    }

    /**
     * The EJB's Session Context
     */
    private SessionContext sessionContext = null; 
    
    //------------------------------------------------------------------------
    // Implements ContextSwitcher
    //------------------------------------------------------------------------

    /**
     * 
     */
    private final ContextSwitcher externalContextSwitcher;

    /**
     * 
     */
    private final ClassLoader beanClassLoader;
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ExtendedEJBHome#setBeanContext()
     */
    public Object setBeanContext(
    ) {
        Thread thread = Thread.currentThread();
        Object callerContext = this.externalContextSwitcher == null ?
        	thread.getContextClassLoader() : 
        	new Object[] {
    			this.externalContextSwitcher.setBeanContext(),
    			thread.getContextClassLoader()
        	};
        thread.setContextClassLoader(this.beanClassLoader);
        return callerContext;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ExtendedEJBHome#setCallerContext(java.lang.Object)
     */
    public void setCallerContext(Object callerContext) {
    	if(this.externalContextSwitcher == null) {
            Thread.currentThread().setContextClassLoader((ClassLoader) callerContext);
    	} else {
    		Object[] callerContexts = (Object[]) callerContext;
            Thread.currentThread().setContextClassLoader((ClassLoader) callerContexts[1]);
            this.externalContextSwitcher.setCallerContext(callerContexts[0]);
    	}
    }

}
