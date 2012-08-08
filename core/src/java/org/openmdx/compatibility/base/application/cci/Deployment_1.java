/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Deployment_1.java,v 1.2 2008/09/16 17:31:21 hburger Exp $
 * Description: Deployment
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/16 17:31:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.application.cci;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.base.application.deploy.Deployment;
import org.openmdx.base.application.deploy.InProcessDeployment;
import org.openmdx.base.application.deploy.RemoteDeployment;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.spi.AbstractManagerFactory;
import org.openmdx.base.persistence.spi.ManagerFactory_2_0;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;

/**
 * Deployment
 */
public class Deployment_1 
    implements Closeable, Dataprovider_1ConnectionFactory, ManagerFactory_2_0 
{

    /**
     * Constructor 
     *
     * @param inProcess in-process deployment if <code>true</code>, remote deployment if <code>false</code>
     * @param connectorURL
     * @param applicationURL
     * @param logDeploymentDetails
     * @param entityManagerFactoryURI the entity manager's JNDI name
     * @param gatewayURI the gateway'sJNDI name
     * @param model
     */
    public Deployment_1(
        boolean inProcess,
        String connectorURL, 
        String applicationURL, 
        boolean logDeploymentDetails,
        String entityManagerFactoryURI, 
        String gatewayURI,
        String model
    ) { 
        this(
            inProcess,
            connectorURL == null ? EMPTY : new String[]{connectorURL},
            applicationURL == null ? EMPTY : new String[]{applicationURL},
            logDeploymentDetails,
            entityManagerFactoryURI,
            gatewayURI,
            model == null ? EMPTY : new String[]{model}
        );
    }
    
    /**
     * Constructor 
     *
     * @param inProcess in-process deployment if <code>true</code>, remote deployment if <code>false</code>
     * @param connectorURLs
     * @param applicationURLs
     * @param logDeploymentDetails
     * @param entityManagerFactoryURI the entity manager's JNDI name
     * @param gatewayURI the gateway'sJNDI name
     * @param models
     */
    public Deployment_1(
        boolean inProcess,
        String[] connectorURLs, 
        String[] applicationURLs, 
        boolean logDeploymentDetails,
        String entityManagerFactoryURI,
        String gatewayURI,
        String[] models
    ) {
        //
        // Persistence Manager Factory Configuration
        //
        this.properties.put(
            ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(),
            "org.openmdx.base.accessor.jmi1.AccessorFactory_2"
        );
        this.properties.put(
            ConfigurableProperty.ConnectionFactoryName.qualifiedName(),
            entityManagerFactoryURI
        );
        //
        // Model Deployment
        //
        Set<String> modelSet = new HashSet<String>(
            Arrays.asList(
                "org:un",
                "org:iso",
                "org:w3c",
                "org:openmdx:base",
                "org:openmdx:compatibility:datastore1",
                "org:openmdx:compatibility:document1",
                "org:openmdx:compatibility:state1",
                "org:openmdx:compatibility:sequence1",
                "org:openmdx:deployment1",
                "org:openmdx:generic1",
                "org:omg:model1"
            )
        );
        if(models != null) {
            modelSet.addAll(
                Arrays.asList(models)
            );
        }
        this.modelDeployment = new Model_1Deployment(modelSet);
        //
        // Connector and Application Deployment
        //
        this.dataproviderDeployment = inProcess ? new InProcessDeployment(
            connectorURLs,
            applicationURLs,
            logDeploymentDetails ? System.out : null,
            System.err
        ) : new RemoteDeployment (
            connectorURLs,
            applicationURLs,
            logDeploymentDetails ? System.out : null,
            System.err
        );
        //
        // Compatibility Deployment
        //
        this.connectionFactory = new Dataprovider_1Deployment(
            dataproviderDeployment,
            modelDeployment,
            gatewayURI
        );
    }

    private static final String[] EMPTY = {};
    
    /**
     * 
     */
    private PersistenceManagerFactory persistenceManagerFactory;
    
    /**
     * 
     */
    final private Map<String,Object> properties = new HashMap<String,Object>();
    
    /**
     * The model deployment is shared
     */
    final private Deployment modelDeployment;

    /**
     * The dataprovider deployment is shared
     */
    final private Deployment dataproviderDeployment;

    /**
     * DTD based deployment descriptors
     */
    protected final Dataprovider_1ConnectionFactory connectionFactory;

    
    //------------------------------------------------------------------------
    // Implements ManagerFactory_2_0
    //------------------------------------------------------------------------
    
    protected PersistenceManagerFactory getPersistenceManagerFactory(
    ) throws ResourceException {
        if(this.persistenceManagerFactory == null) {
            try {
                this.modelDeployment.context();
                this.dataproviderDeployment.context().close();
            } catch (ServiceException exception) {
                throw new ResourceException(
                    "Deployment failure",
                    exception
                );
            } catch (NamingException exception) {
                new ServiceException(exception).log();
            }
            this.persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(properties); 
        }
        return this.persistenceManagerFactory;
        
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager()
     */
    public PersistenceManager createManager(
    ) throws ResourceException {
        return getPersistenceManagerFactory().getPersistenceManager();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager(javax.security.auth.Subject)
     */
    public PersistenceManager createManager(
        Subject subject
    ) throws ResourceException {
        PasswordCredential credential = AbstractManagerFactory.getCredential(subject);
        return getPersistenceManagerFactory().getPersistenceManager(
            credential.getUserName(),
            String.valueOf(credential.getPassword())
        );
    }

    
    //------------------------------------------------------------------------
    // Implements Dataprovider_1ConnectionFactory
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory#createConnection()
     */
    public Dataprovider_1_1Connection createConnection(
    ) throws ServiceException {
        return this.connectionFactory.createConnection();
    }

    
    //------------------------------------------------------------------------
    // Implements Closeable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    public void close()
        throws IOException {
        // TODO Auto-generated method stub
        
    }
    
}