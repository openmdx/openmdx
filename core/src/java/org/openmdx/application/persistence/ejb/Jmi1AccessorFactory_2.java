/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Jmi1AccessorFactory_2.java,v 1.8 2009/06/12 10:01:29 wfro Exp $
 * Description: AccessorFactory_2 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/12 10:01:29 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
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
package org.openmdx.application.persistence.ejb;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;

import org.openmdx.application.rest.ejb.Connection_2Factory;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.accessor.rest.DataObjectManagerFactory_1;
import org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1;
import org.openmdx.base.accessor.view.ViewManager_1;
import org.openmdx.base.persistence.cci.EntityManagerFactory;
import org.openmdx.base.resource.spi.LocalTransactionAdapter;
import org.openmdx.base.resource.spi.TransactionManager;
import org.openmdx.base.resource.spi.UserTransactionAdapter;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;

/**
 * Accessor Factory
 * <p><i> 
 * Note:<br>
 * The entity managers return jmi1 object's at the moment.
 * In future we will have to delegate to their cci2 instances!
 * </i> 
 */
public class Jmi1AccessorFactory_2
    extends AbstractPersistenceManagerFactory_1 {

    //-----------------------------------------------------------------------
    private static class EntityManagerFactoryAdapter implements EntityManagerFactory {

        public EntityManagerFactoryAdapter(
            Object connectionFactory,
            TransactionManager transactionManager
        ) {
            this.connectionFactory = connectionFactory;
            this.transactionManager = transactionManager;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.cci.EntityManagerFactory#getEntityManager(java.util.List)
         */
        public PersistenceManager getEntityManager(
            List<String> principalChain
        ) throws ResourceException {
            Connection conn = null;
            if(this.connectionFactory instanceof ConnectionFactory) {
                conn = ((ConnectionFactory)this.connectionFactory).getConnection(); 
            }
            else {
                conn = Connection_2Factory.newInstance(this.connectionFactory).getConnection();
            }           
            DataObjectManagerFactory_1 factory = new DataObjectManagerFactory_1(
                principalChain,
                conn,
                this.transactionManager == null ?
                    new LocalTransactionAdapter(conn.getLocalTransaction()) :
                    this.transactionManager
            );
            return new ViewManager_1(
                factory.getPersistenceManager()
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.cci.EntityManagerFactory#getEntityManager()
         */
        public PersistenceManager getEntityManager(
        ) throws ResourceException {
            return this.getEntityManager(null);
        }
        
        private final Object connectionFactory;
        private final TransactionManager transactionManager;
    }
    
    //-----------------------------------------------------------------------
    /**
     * Constructor 
     * <p><i> 
     * Note:<br>
     * The entity managers return jmi1 object's at the moment.
     * In future we will have to delegate to their cci2 instances!
     * </i> 
     *
     * @param configuration
     */
    protected Jmi1AccessorFactory_2(
        Map<String, Object> configuration
    ) {
        super(configuration);
    }

    //-----------------------------------------------------------------------
    /**
     * JDO's standard factory method
     * 
     * @param properties
     * 
     * @return a new persistence manager factory instance
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory(
        Map<String, Object> properties
    ){
        Map<String, Object> configuration = new HashMap<String, Object>(DEFAULT_CONFIGURATION);
        configuration.putAll(properties);
        return new Jmi1AccessorFactory_2(configuration);
    }

    //------------------------------------------------------------------------
    // Extends AbstractPersistenceManagerFactory
    //------------------------------------------------------------------------

    //-----------------------------------------------------------------------
    /**
     * Retrieve the manager factory
     * 
     * @return the manager factory
     */
    private EntityManagerFactory getManagerFactory(
    ) {
        if(this.managerFactory == null) {
            Object connectionFactory = this.getConnectionFactory();
            if(connectionFactory == null) {
                try {
                    connectionFactory = new InitialContext().lookup(this.getConnectionFactoryName());
                    if(connectionFactory instanceof EntityManagerFactory_2LocalHome) {
                        EntityManagerFactory_2LocalHome localHome = (EntityManagerFactory_2LocalHome)connectionFactory;
                        this.managerFactory = localHome.create();
                    }
                    else if(connectionFactory instanceof ConnectionFactory) {
                        this.managerFactory = new EntityManagerFactoryAdapter(
                            connectionFactory, 
                            null
                        );                        
                    }
                    else {
                        this.managerFactory = new EntityManagerFactoryAdapter(
                            connectionFactory,
                            new UserTransactionAdapter()
                        );
                    }
                }
                catch (Exception exception) {
                    throw new JmiServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.GENERIC,
                        "EntityManagerFactory acquisition failure"
                    );
                }
            } 
            else {
                this.managerFactory = (EntityManagerFactory) connectionFactory;
            }
        }
        return this.managerFactory;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory#newPersistenceManager()
     */
    @Override
    protected synchronized PersistenceManager newManager(
    ) {
        try {
            PersistenceManager em = this.getManagerFactory().getEntityManager();
            return (em instanceof ViewManager_1 ?
                new RefRootPackage_1((ViewManager_1)em, true).refPersistenceManager() :
                em
            );
        } 
        catch (ResourceException exception) {
            throw new JmiServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Manager acquisition failure"
            );
        }
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory#newPersistenceManager(javax.security.auth.Subject)
     */
    @Override
    protected synchronized PersistenceManager newManager(
        List<String> principalChain
    ) {
        try {
            PersistenceManager em = this.getManagerFactory().getEntityManager(principalChain);
            return (em instanceof ViewManager_1 ?
                new RefRootPackage_1((DataObjectManager_1_0)em, true).refPersistenceManager() :
                em
            );
        } 
        catch (ResourceException exception) {
            throw new JmiServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Manager acquisition failure",
                new BasicException.Parameter("subject", principalChain)
            );
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    /**
     * The entity connection factory
     */
    private transient EntityManagerFactory managerFactory = null;

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3442827664670646249L;

    protected static final Map<String, Object> DEFAULT_CONFIGURATION = Collections.singletonMap(
        ConfigurableProperty.ConnectionFactoryName.qualifiedName(),
        (Object)"java:comp/env/ejb/EntityManagerFactory"
    );
    
}
