/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Deployment_1.java,v 1.10 2009/03/09 17:11:21 hburger Exp $
 * Description: Deployment
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/09 17:11:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2009, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.deployment;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.resource.ResourceException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1;
import org.openmdx.base.deploy.LazyDeployment;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.persistence.cci.EntityManagerFactory;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;

/**
 * Deployment
 */
public class Deployment_1 
    implements Closeable, EntityManagerFactory, Dataprovider_1ConnectionFactory 
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
        String connectorURI, 
        String applicationURI, 
        boolean logDeploymentDetails,
        String entityManagerFactoryURI, 
        String gatewayURI,
        String model,
        String providerURI
    ) { 
        this(
            new LazyDeployment(
                connectorURI,
                applicationURI,
                providerURI == null ? toOpenMdxProviderURI(inProcess) : providerURI
            ),
            entityManagerFactoryURI,
            gatewayURI,
            model == null ? null : new String[]{model}
        );
    }
    
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
        String connectorURI, 
        String applicationURI, 
        boolean logDeploymentDetails,
        String entityManagerFactoryURI, 
        String gatewayURI,
        String model
    ) { 
        this(
            inProcess,
            connectorURI,
            applicationURI,
            logDeploymentDetails,
            entityManagerFactoryURI,
            gatewayURI,
            model,
            null // default providerURI
        );
    }
    
    /**
     * Constructor 
     *
     * @param inProcess in-process deployment if <code>true</code>, remote deployment if <code>false</code>
     * @param connectorURIs
     * @param applicationURIs
     * @param logDeploymentDetails
     * @param entityManagerFactoryURI the entity manager's JNDI name
     * @param gatewayURI the gateway'sJNDI name
     * @param models
     */
    public Deployment_1(
        boolean inProcess,
        String[] connectorURIs, 
        String[] applicationURIs, 
        boolean logDeploymentDetails,
        String entityManagerFactoryURI,
        String gatewayURI,
        String[] models
    ) {
        this(
            toOpenMdxProviderURI(inProcess),
            connectorURIs,
            applicationURIs,
            logDeploymentDetails,
            entityManagerFactoryURI,
            gatewayURI,
            models
        );
    }

    /**
     * Constructor 
     *
     * @param inProcess in-process deployment if <code>true</code>, remote deployment if <code>false</code>
     * @param connectorURIs
     * @param applicationURIs
     * @param logDeploymentDetails
     * @param entityManagerFactoryURI the entity manager's JNDI name
     * @param gatewayURI the gateway'sJNDI name
     * @param models
     */
    public Deployment_1(
        String providerURI,
        String[] connectorURIs, 
        String[] applicationURIs, 
        boolean logDeploymentDetails,
        String entityManagerFactoryURI,
        String gatewayURI,
        String[] models
    ) {
        this(
            new LazyDeployment(
                toURL(connectorURIs),
                toURL(applicationURIs), 
                providerURI
            ),
            entityManagerFactoryURI,
            gatewayURI,
            models
        );
    }

    /**
     * Constructor 
     * @param entityManagerFactoryURI the entity manager's JNDI name
     * @param gatewayURI the gateway'sJNDI name
     * @param models
     * @param connectorURLs
     * @param applicationURLs
     *
     * @throws ServiceException 
     */
    private Deployment_1(
        InitialContextFactory initialContextFactory,
        String entityManagerFactoryURI,
        String gatewayURI,
        String[] models
    ) {
        //
        // Entitiy Manager Factory Configuration
        //
    	this.entityManagerFactoryURI = entityManagerFactoryURI;
        //
        // Model Deployment
        //
        this.models = models;
        //
        // Connector and Application Deployment
        //
        this.initialContextFactory = initialContextFactory;
        //
        // Dataprovider Connection Factory Configuration
        //
        this.gatewayURI = gatewayURI;
    }
    
    /**
     * 
     */
    private String[] models;
    
    /**
     * Persistence Manager Factory Instances
     */
    private final Map<String,PersistenceManagerFactory> persistenceManagerFactories = new HashMap<String,PersistenceManagerFactory>();
    
    /**
     * Initial Context Factory Instance
     */
    private final InitialContextFactory initialContextFactory;

    /**
     * Dataprovider Connection Factory Configuration
     */
    private final String gatewayURI;
    
    /**
     * Entity Manager Factory Configuration
     */
    private final String entityManagerFactoryURI;
    
    private static String toOpenMdxProviderURI(
        boolean inProcess
    ){
        return "xri://@openmdx*(+lightweight)*" + (
            inProcess ? "ENTERPRISE_APPLICATION_CONTAINER" : "ENTERPRISE_JAVA_BEAN_SERVER"
        );
    }
    
    /**
     * 
     * @param uris
     * 
     * @return
     */
    private static URL[] toURL(
        String[] uris
    ){
        URL[] urls = new URL[uris.length];
        for(int i = 0; i < uris.length; i++) {
            
            try {
                urls[i] = new URL(uris[i]);
            } catch (MalformedURLException exception) {
                throw new RuntimeServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSFORMATION_FAILURE,
                    "Could not parse deployment URI",
                    new BasicException.Parameter("uri", uris[i]) 
                );
            }
        }
        return urls;
    }
    
    protected Context getInitialContext(
    ) throws NamingException, ServiceException{
        if(this.models != null) {
            Model_1Factory.getModel().addModels(
                Arrays.asList(models)
            );
            this.models = null;
        }
        return this.initialContextFactory.getInitialContext(null); 
    }
    
    
    //------------------------------------------------------------------------
    // Implements EntitiyManagerFactory
    //------------------------------------------------------------------------
    
    protected PersistenceManagerFactory getPersistenceManagerFactory(
    	String entityManagerFactoryURI
    ) throws ResourceException {
    	PersistenceManagerFactory persistenceManagerFactory = this.persistenceManagerFactories.get(entityManagerFactoryURI);
    	if(persistenceManagerFactory == null) {
	        try {
	            getInitialContext();
	        } catch (ServiceException exception) {
	            throw new ResourceException(
	                "Deployment failure",
	                exception
	            );
	        } catch (NamingException exception) {
	            new ServiceException(exception).log();
	        }
	        Map<String,Object> properties = new HashMap<String,Object>();
	        properties.put(
	            ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(),
	            "org.openmdx.application.dataprovider.transport.ejb.cci.Jmi1AccessorFactory_2"
	        );
	        properties.put(
	            ConfigurableProperty.ConnectionFactoryName.qualifiedName(),
	            entityManagerFactoryURI
	        );
	        this.persistenceManagerFactories.put(
	        	entityManagerFactoryURI,
	        	persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(properties)
	        ); 
    	}
    	return persistenceManagerFactory;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager()
     */
    public PersistenceManager getEntityManager(
    	String entityManagerFactoryURI
    ) throws ResourceException {
        return getPersistenceManagerFactory(entityManagerFactoryURI).getPersistenceManager();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager()
     */
    public PersistenceManager getEntityManager(
    ) throws ResourceException {
        return getPersistenceManagerFactory(this.entityManagerFactoryURI).getPersistenceManager();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager(javax.security.auth.Subject)
     */
    public PersistenceManager getEntityManager(
        Subject subject
    ) throws ResourceException {
        PasswordCredential credential = AbstractPersistenceManagerFactory_1.getCredential(subject);
        return getPersistenceManagerFactory(this.entityManagerFactoryURI).getPersistenceManager(
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
        if(this.gatewayURI == null) {
            return null;
        } else try {
            return Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
                getInitialContext().lookup(this.gatewayURI)
            );
        } catch (Exception exception) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_AVAILABLE,
                "Dataprovider Connection Acquisition Failure",
                new BasicException.Parameter("gatewayURI", this.gatewayURI)
            );
        }
    }

    
    //------------------------------------------------------------------------
    // Implements Closeable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    public void close(
    ) throws IOException {
    }

}