/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: InboundConnectionFactory_2.java,v 1.25 2010/10/26 16:52:54 hburger Exp $
 * Description: Inbound REST Connection Factory
 * Revision:    $Revision: 1.25 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/10/26 16:52:54 $
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
package org.openmdx.application.rest.spi;

import java.util.Collections;
import java.util.Map;

import javax.jdo.Constants;
import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.ResourceAdapterMetaData;

import org.openmdx.base.Version;
import org.openmdx.base.accessor.rest.InboundConnection_2;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.kernel.exception.BasicException;

/**
 * Inbound REST Connection Factory
 */
public class InboundConnectionFactory_2 implements ConnectionFactory {

    /**
     * Constructor 
     * 
     * @param entityManagerFactoryName name used for entity manager lookup, 
     * i.e. one of<ul>
     * <li><code>jdo:<em>&lang;JDO-name&rang;</em>
     * <li><code>java:comp/env/<em>&lang;JNDI-name&rang;</em>
     * </ul>
     * @param overrides configuration overrides taken into consideration in 
     * case of a JDO name
     */
    private InboundConnectionFactory_2(
        String entityManagerFactoryName,
        Map<?,?> overrides
    ){
        this.entityManagerFactoryName = entityManagerFactoryName;
        this.overrides = overrides;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3251983865328784983L;

    /**
     * An optional reference to the REST dispatcher
     */
    private Reference reference;

    /**
     * The entity manager factory's name
     */
    private final String entityManagerFactoryName;
    
    /**
     * @serial The connection factory configuration
     */
    final Map<?,?> overrides;
    
    /**
     * The persistence manager factory
     */
    private transient PersistenceManagerFactory persistenceManagerFactory;
    
    /**
     * Tests whether the "RefInitialize" option is active
     * 
     * @return <code>true</code> if refInitialize should be active
     */
    public boolean isRefinitializeOnCreate(){
        return Boolean.parseBoolean(
            (String)this.overrides.get(ConfigurableProperty.RefInitializeOnCreate.qualifiedName())
        );
    }
    
    /**
     * The resource adapter's metadata
     */
    private final ResourceAdapterMetaData metaData = new ResourceAdapterMetaData(){

        public String getAdapterName() {
            return "openMDX/REST";
        }

        public String getAdapterShortDescription() {
            return "openMDX/2 REST Resource Adapter";
        }

        public String getAdapterVendorName() {
            return "openMDX";
        }

        public String getAdapterVersion() {
            return Version.getSpecificationVersion();
        }

        public String[] getInteractionSpecsSupported() {
            return new String[]{RestInteractionSpec.class.getName()};
        }

        /**
         * Retrieve the JCA specification version
         * 
         * @return the JCA specification version
         */
        public String getSpecVersion() {
            return "1.5.";
        }

        public boolean supportsExecuteWithInputAndOutputRecord() {
            return true; 
        }

        public boolean supportsExecuteWithInputRecordOnly() {
            return true;
        }

        public boolean supportsLocalTransactionDemarcation(
        ) {
            Object transactionType = InboundConnectionFactory_2.this.overrides.get(
                ConfigurableProperty.TransactionType.qualifiedName()
            );
            if(transactionType == null) try {
                transactionType = InboundConnectionFactory_2.this.getPersistenceManagerFactory().getTransactionType();
            } catch (ResourceException exception) {
                throw new RuntimeException(
                    "Unable to determine the connection's transaction type",
                    exception
                    
                );
            }
            return Constants.RESOURCE_LOCAL.equals(transactionType);
        }
        
    };

    /**
     * Retrieve the connection factory's delegate
     * 
     * @return the delegate
     */
    protected PersistenceManagerFactory getPersistenceManagerFactory(
    ) throws ResourceException {
        if(this.persistenceManagerFactory == null) try {
            this.persistenceManagerFactory = this.entityManagerFactoryName.startsWith("jdo:") ? JDOHelper.getPersistenceManagerFactory(
                this.overrides,
                this.entityManagerFactoryName.substring(4)
            ) :  JDOHelper.getPersistenceManagerFactory(
                this.entityManagerFactoryName,
                (Context)null
            );
        } catch (JDOException exception) {
            throw ResourceExceptions.initHolder(
                new ResourceException(
                    "Persistence manager acquisition failure",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ACTIVATION_FAILURE
                   )
               )
            );
        }
        return this.persistenceManagerFactory;
    }
    
    /**
     * Create a new REST dispatcher
     * 
     * @param entityManagerFactoryName name used for entity manager lookup, 
     * i.e. one of<ul>
     * <li><code>jdo:<em>&lang;JDO-name&rang;</em>
     * <li><code>java:comp/env/<em>&lang;JNDI-name&rang;</em>
     * </ul>
     */
    public static ConnectionFactory newInstance(
        String entityManagerFactoryName
    ){
        return new InboundConnectionFactory_2(
            entityManagerFactoryName,
            Collections.EMPTY_MAP
        );
    }

    /**
     * Create a new REST dispatcher
     * 
     * @param entityManagerFactoryName name used for entity manager lookup, 
     * i.e. one of<ul>
     * <li><code>jdo:<em>&lang;JDO-name&rang;</em>
     * <li><code>java:comp/env/<em>&lang;JNDI-name&rang;</em>
     * </ul>
     * @param overrides configuration overrides taken into consideration in 
     * case of a JDO name
     */
    public static ConnectionFactory newInstance(
        String entityManagerFactoryName,
        Map<?,?> overrides
    ){
        return new InboundConnectionFactory_2(
            entityManagerFactoryName,
            overrides
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.Referenceable#setReference(javax.naming.Reference)
     */
    public void setReference(Reference reference) {
        this.reference = reference;
    }


    /* (non-Javadoc)
     * @see javax.naming.Referenceable#getReference()
     */
    public Reference getReference(
    ) throws NamingException {
        return this.reference;
    }


    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getConnection()
     */
    public Connection getConnection(
    ) throws ResourceException {
        return new InboundConnection_2(
            null, // REST ConnectionSpec
            this.getPersistenceManagerFactory().getPersistenceManager(), 
            this.isRefinitializeOnCreate()
        );
    }


    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getConnection(javax.resource.cci.ConnectionSpec)
     */
    public Connection getConnection(
        ConnectionSpec properties
    ) throws ResourceException {
        if(properties == null) {
            return getConnection();
        } else if (properties instanceof RestConnectionSpec) {
            RestConnectionSpec connectionSpec = (RestConnectionSpec) properties;
            PersistenceManager persistenceManager = getPersistenceManagerFactory().getPersistenceManager(
                connectionSpec.getUserName(),
                connectionSpec.getPassword()
            ); 
            Object tenant = connectionSpec.getTenant();
            if(tenant != null) {
                UserObjects.setTenant(persistenceManager, tenant);
            }
            return new InboundConnection_2(
                connectionSpec, 
                persistenceManager, 
                this.isRefinitializeOnCreate()
            );
        } else {
            throw ResourceExceptions.initHolder(
                new NotSupportedException(
                    "Unsupported connection spec",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", RestConnectionSpec.class.getName()),
                        new BasicException.Parameter("actual", properties.getClass().getName())
                    )
                )
            );
        }
    }


    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getMetaData()
     */
    public ResourceAdapterMetaData getMetaData(
    ) throws ResourceException {
        return this.metaData;
    }


    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getRecordFactory()
     */
    public ExtendedRecordFactory getRecordFactory(
    ) throws ResourceException {
        return Records.getRecordFactory();
    }

}