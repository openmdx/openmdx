/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: InboundConnectionFactory_2.java,v 1.21 2009/12/30 10:49:34 hburger Exp $
 * Description: Inbound REST Connection Factory
 * Revision:    $Revision: 1.21 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/12/30 10:49:34 $
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

import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
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
     * @param overrides configuration overrides
     * @param entityManagerFactoryName name used for entity manager lookup
     */
    private InboundConnectionFactory_2(
        Map<?,?> overrides,
        String entityManagerFactoryName
    ){
        this.overrides = overrides;
        this.entityManagerFactoryName = entityManagerFactoryName;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3251983865328784983L;

    /**
     * An optional reference to the REST dispatcher
     */
    private Reference reference;

    private final String entityManagerFactoryName;
    
    /**
     * @serial The connection factory configuration
     */
    private final Map<?,?> overrides;
    
    /**
     * The persistence manager factory
     */
    private transient PersistenceManagerFactory persistenceManagerFactory;
    
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

        public boolean supportsLocalTransactionDemarcation() {
            return true;
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
            this.persistenceManagerFactory =  JDOHelper.getPersistenceManagerFactory(
                this.overrides,
                this.entityManagerFactoryName
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
     * @param entityManagerFactory the entity manager factory JNDI name
     */
    public static ConnectionFactory newInstance(
        String entityManagerFactoryName
    ){
        return new InboundConnectionFactory_2(
            Collections.EMPTY_MAP,
            entityManagerFactoryName
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
            this.getPersistenceManagerFactory().getPersistenceManager()
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
            return new InboundConnection_2(
                connectionSpec, 
                getPersistenceManagerFactory().getPersistenceManager(
                    connectionSpec.getUserName(),
                    connectionSpec.getPassword()
                )
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