/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LightweightHomeFactory.java,v 1.3 2010/04/16 10:02:59 hburger Exp $
 * Description: Lightweight Home Factory
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/16 10:02:59 $
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
package org.openmdx.kernel.application.container.lightweight;

import java.lang.reflect.InvocationHandler;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.SessionBean;
import javax.transaction.TransactionManager;

import org.openmdx.kernel.application.container.spi.ejb.ContainerTransaction;
import org.openmdx.kernel.application.container.spi.ejb.ContextSwitcher;
import org.openmdx.kernel.application.container.spi.ejb.HomeConfiguration;
import org.openmdx.kernel.application.container.spi.ejb.HomeHandler;
import org.openmdx.kernel.application.container.spi.ejb.LocalHomeHandler;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.uses.org.apache.commons.pool.ObjectPool;

/**
 * Lightweight Home Factory
 * <p>
 * This method handles stateless session beans only at the moment.
 */
public class LightweightHomeFactory {

    /**
     * Constructor
     * 
     * @param ejbHomeInterface 
     * @param ejbObjectInterface 
     * @param ejbLocalHomeInterface
     * @param ejbLocalObjectInterface
     * @param ejbHomeClass 
     * 
     * @throws ClassNotFoundException
     */
    public LightweightHomeFactory (
        String ejbHomeInterface,
        String ejbObjectInterface, 
        String ejbLocalHomeInterface, 
        String ejbLocalObjectInterface, 
        String ejbHomeClass
    ) throws ClassNotFoundException {
        this.homeInterface = ejbHomeInterface;
        this.objectInterface = ejbObjectInterface;
        this.localHomeInterface = getClass(ejbLocalHomeInterface);
        this.localObjectInterface = getClass(ejbLocalObjectInterface);
        this.homeClass = getClass(ejbHomeClass);
    }

    /**
     * The EJB's home interface class
     */
    private final String homeInterface;

    /**
     * The EJB object's interface class
     */
    private final String objectInterface;

    /**
     * The EJB object's home implementation class
     */
    private final Class<?> homeClass;


    /**
     * The EJB's local home interface class
     */
    private final Class<EJBLocalHome> localHomeInterface;

    /**
     * The EJB's local interface class
     */
    private final Class<EJBLocalObject> localObjectInterface;

    /**
     * The EJB's home proxy handler
     */
    private HomeHandler homeHandler = null;

    /**
     * The EJB's local home interface instance if declared
     */
    private EJBLocalHome localHome = null;

    /**
     * The EJB's home interface instance if declared
     */
    private EJBHome home = null;
    
    /**
     * Retrieve a home handler instance
     * 
     * @return a home handler instance
     */
    public InvocationHandler getHomeHandler(
    ){
        return this.homeHandler;
    }

    /**
     * Retrieve a home instance
     * 
     * @return a home instance
     */
    public String getHomeClass(
    ){
        return this.homeInterface;
    }

    /**
     * Create a home instance
     * 
     * @return a new home instance
     */
    public EJBHome getHome(
    ){
        return this.home;    
    }
    
    
    /**
     * Create a local home instance
     * 
     * @return a new local home instance
     */
    public EJBLocalHome getLocalHome(
    ){
        return this.localHome;    
    }
    
    /**
     * Get a class by name
     * 
     * @param name the class' name or <code>null</code>
     * 
     * @return the requested class or <code>null</code> if name is <code>null</code>
     * 
     * @throws ClassNotFoundException
     */
    private static <T> Class<T> getClass(
         String name
    ) throws ClassNotFoundException{
        return name == null ? null : Classes.<T>getApplicationClass(name);
    }

    
    //------------------------------------------------------------------------
    // Implements HomeConfiguration
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.HomeConfiguration#initialize(java.lang.String, java.lang.String, org.openmdx.kernel.application.container.spi.ejb.ContextSwitcher, java.lang.Class, org.openmdx.uses.org.apache.commons.pool.ObjectPool, org.openmdx.kernel.application.container.spi.ejb.ContainerTransaction, javax.transaction.TransactionManager)
     */
    public void initialize(
         String beanName, 
         String jndiURL, 
         ContextSwitcher contextSwitcher, 
         Class<SessionBean> instanceClass, 
         ObjectPool instancePool, 
         ContainerTransaction containerTransaction, 
         TransactionManager transactionManager
    ) throws Exception {
        if(this.localHomeInterface != null) {
            LocalHomeHandler<EJBLocalHome> localHomeInvocationHandler = new LocalHomeHandler<EJBLocalHome>(
                this.localHomeInterface,
                this.localObjectInterface
            );
            localHomeInvocationHandler.initialize(
                beanName, 
                jndiURL, 
                contextSwitcher, 
                instanceClass,
                instancePool, 
                containerTransaction, 
                transactionManager
            );
            this.localHome = localHomeInvocationHandler.getLocalHome();
        }
        if(this.homeInterface != null) {
            if(LightweightContainer.getMode() == LightweightContainer.Mode.ENTERPRISE_JAVA_BEAN_CONTAINER) {
                this.home = null;
                this.homeHandler = new HomeHandler(
                    this.homeInterface,
                    this.objectInterface
                );
                this.homeHandler.initialize(
                    beanName,   
                    jndiURL, 
                    contextSwitcher, 
                    instanceClass,
                    instancePool, 
                    containerTransaction, 
                    transactionManager
                );
            } else {
                this.home = (EJBHome) this.homeClass.newInstance();
                    new HomeHandler(
                    this.homeInterface,
                    this.objectInterface
                );
                ((HomeConfiguration)this.home).initialize(
                    beanName, 
                    jndiURL, 
                    contextSwitcher, 
                    instanceClass,
                    instancePool, 
                    containerTransaction, 
                    transactionManager
                );
                this.homeHandler = null;
            }
        }
    }
    
}
