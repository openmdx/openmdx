/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: EntityManagerFactory_2Bean.java,v 1.8 2008/09/10 08:55:26 hburger Exp $
 * Description: Gateway_1Bean 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:26 $
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
package org.openmdx.compatibility.application.dataprovider.transport.ejb.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import javax.transaction.Synchronization;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.spi.ManagerFactory_2_0;
import org.openmdx.base.persistence.spi.OptimisticTransaction_2_0;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.LateBindingConnection_2;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.spi.AbstractDataprovider_1Bean;
import org.openmdx.compatibility.base.application.cci.ConfigurationSpecifier;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.kernel.PersistenceManagerFactory_2;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_2Connection;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_3Connection;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;
import org.openmdx.model1.mapping.Names;

/**
 * Gateway_1Bean
 */
public class EntityManagerFactory_2Bean
extends AbstractDataprovider_1Bean 
implements Dataprovider_1_0, ManagerFactory_2_0, OptimisticTransaction_2_0
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3915234304780769450L;

    /**
     * 
     */
    private Map<String,Dataprovider_1_3Connection> connections;

    /**
     * 
     */    
    protected Dataprovider_1_2Connection kernel;

    /**
     * 
     */
    private static final String[] PERSISTENCE_MANAGER_SECTION = {
        "PersistenceManager"
    };


    //------------------------------------------------------------------------
    // Extends SessionBean_1
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.application.dataprovider.transport.ejb.server.Dataprovider_1Bean#activate()
     */
    @Override
    public void activate(
    ) throws Exception {
        //
        // Connections
        //
        this.connections = new HashMap<String,Dataprovider_1_3Connection>();
        //
        // Delegate
        //
        super.activate();
    }

    /**
     * Make the objects available for garbage collection, but do NOT close 
     * them as they may still be in use by formerly returned entity
     * managers. That's why super.deactivate() is not called.
     * 
     * @see org.openmdx.compatibility.application.dataprovider.transport.ejb.server.Dataprovider_1Bean#deactivate()
     */
    @Override
    public void deactivate(
    ) throws Exception {
        this.kernel.close();
        this.connections = null; 
    }


    //------------------------------------------------------------------------
    // Extends AbstractDataprovider_1Bean
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.application.dataprovider.transport.ejb.server.Dataprovider_1Bean#newDataproviderConnection(java.lang.String)
     */
    @Override
    protected Dataprovider_1_3Connection newDataproviderConnection(
        String jndiEntry
    ) throws ServiceException {
        Dataprovider_1_3Connection connection = this.connections.get(jndiEntry);
        if(connection == null) this.connections.put(
            jndiEntry, 
            connection = new LateBindingConnection_2 (
                BEAN_ENVIRONMENT + '/' + DATAPROVIDER_NAME_CONTEXT + '/' + jndiEntry
            )
        );
        return connection;
    }

    /**
     * Activates the EJB
     * 
     * @param configuration
     */
    protected void activate(
        Configuration configuration
    ) throws Exception {
        super.activate(configuration);
        //
        // Get dataprovider connections
        //
        getDataproviderConnections(configuration);
        //
        // Get datasources
        //
        getDataSources(configuration);
        //
        // Acquire kernel
        //
        this.kernel = new PersistenceManagerFactory_2(
            configuration,
            this, 
            getSelf()
        );
    }

    /**
     * Apply the default values to the persistence manager configuration
     * 
     * @param target the persistence manager configuration
     * 
     * @throws ServiceException
     */
    protected void applyDefaultConfiguration(
        Map<String,Object> target
    ) throws ServiceException{
        target.put(
            ConfigurableProperty.BindingPackageSuffix.qualifiedName(),
            Names.JMI1_PACKAGE_SUFFIX
        );
        target.put(
            ConfigurableProperty.Optimistic.qualifiedName(),
            Boolean.TRUE.toString()
        );
        target.put(
            ConfigurableProperty.NontransactionalRead.qualifiedName(),
            Boolean.TRUE.toString()
        );
        target.put(
            ConfigurableProperty.Multithreaded.qualifiedName(),
            Boolean.FALSE.toString()
        );
        target.put(
            ConfigurableProperty.ContainerManaged.qualifiedName(),
            Boolean.TRUE.toString()
        );
    }

    /**
     * Apply the explicitly configured values to the persistence manager configuration
     * 
     * @param target the persistence manager configuration
     * 
     * @throws ServiceException
     */
    protected void applyExplicitConfiguration(
        Map<String,Object> target
    ) throws ServiceException{
        Configuration source = super.getConfiguration(
            PERSISTENCE_MANAGER_SECTION, // section 
            (Map<String,ConfigurationSpecifier>)null // specification
        );
        for(ConfigurableProperty property : ConfigurableProperty.values()) {
            if(source.containsEntry(property.name())) {
                target.put(
                    property.qualifiedName(), 
                    source.getFirstValue(property.name())
                );
            }
        }
    }


    //------------------------------------------------------------------------
    // Implements EntityManagerFactory_2_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.EntityManagerFactory_2_0#createEntityManager(javax.security.auth.Subject)
     */
    public PersistenceManager createManager(
        Subject subject
    ) throws ResourceException {
        return this.kernel.createManager(subject);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.EntityManagerFactory_2_0#createEntityManager()
     */
    public PersistenceManager createManager(
    ) throws ResourceException {
        return this.kernel.createManager();
    }


    //------------------------------------------------------------------------
    // Implements OptimisticTransaction_2_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.OptimisticTransaction_2_0#commit(javax.transaction.Synchronization)
     */
    public void commit(
        Synchronization synchronization
    ) throws ServiceException {
        SessionContext sessionContext = super.getSessionContext();
        if(sessionContext.getRollbackOnly()) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ROLLBACK,
            "Unit of work was marked for rollback only"
        );  
        try {
            synchronization.beforeCompletion();
        } catch (RuntimeException exception) {
            sessionContext.setRollbackOnly();
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ROLLBACK,
                "Unit of work set to rollback-only during commit"
            );
        }
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    /**
     * Process a set of working units
     *
     * @param   header          the service header
     * @param   requests    a collection of working units
     *
     * @return  a collection of unit of working replies
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest[] requests
    ) {      
        try {
            this.logger.debug("{} header: {} requests: {}", this.serverId, header, Arrays.asList(requests));
            UnitOfWorkReply[] replies = this.kernel.process(
                header,
                requests
            );
            this.logger.debug("{} header: {} replies: {}", this.serverId, header, Arrays.asList(replies));
            return replies;
        } catch (RuntimeException exception) {
            new RuntimeServiceException(exception).log();
            throw exception;    
        }
    }

}
