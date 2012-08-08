/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1Deployment.java,v 1.10 2008/11/27 16:46:56 hburger Exp $
 * Description: In-Process Dataprovider Connection Factory
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/27 16:46:56 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.application.cci;

import javax.jdo.PersistenceManager;
import javax.naming.Context;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import javax.transaction.Synchronization;

import org.openmdx.base.application.deploy.Deployment;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.spi.ManagerFactory_2_0;
import org.openmdx.base.persistence.spi.OptimisticTransaction_2_0;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_3Connection;
import org.openmdx.kernel.exception.BasicException;

/**
 * In-Process Dataprovider Connection Factory
 */
public class Dataprovider_1Deployment
    implements Dataprovider_1ConnectionFactory
{

    /**
     * Constructor
     * 
     * @param dataproviderDeployment
     * optional connector and application deployment argument
     * @param modelDeployment 
     * optional model deployment argument
     * @param jndiName where to look up the dataprovider's home
     */
    public Dataprovider_1Deployment(
        Deployment dataproviderDeployment,
        Deployment modelDeployment, 
        String jndiName 
    ){
        this.dataproviderDeployment = dataproviderDeployment;
        this.modelDeployment = modelDeployment;
        this.jndiName = jndiName;
    }

    /**
     * 
     */
    private final Deployment dataproviderDeployment;

    /**
     * 
     */
    private final Deployment modelDeployment;

    /**
     * 
     */
    private final String jndiName;

    /**
     * 
     */
    private int referenceCount = 0;


    //------------------------------------------------------------------------
    // Implements Dataprovider_1ConnectionFactory
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory#createConnection()
     */
    public synchronized Dataprovider_1_3Connection createConnection(
    ) throws ServiceException {
        try {
            if(this.modelDeployment != null) this.modelDeployment.context();
            Context initialContext = this.dataproviderDeployment.context();
            Dataprovider_1_1Connection connection;
            try {
                connection = Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
                    initialContext.lookup(this.jndiName)
                );
            } finally {
                initialContext.close();
            }
            referenceCount++;
            return new Dataprovider_1Connection(connection);
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                null
            );
        }
    }

    /**
     * Close the connection
     * 
     * @param connection
     */
    synchronized void closeConnection(
        Dataprovider_1_1Connection connection
    ){
        connection.close();
        if(--referenceCount == 0) this.dataproviderDeployment.destroy();
    }


    //------------------------------------------------------------------------
    // Class Dataprovider_1Connection
    //------------------------------------------------------------------------

    /**
     * Dataprovider_1Connection allows destruction of unreferenced deployments.
     */
    class Dataprovider_1Connection implements Dataprovider_1_3Connection {

        /**
         * Constructor
         * 
         * @param delegate
         */
        public Dataprovider_1Connection(
            Dataprovider_1_1Connection delegate
        ) {
            this.delegate = delegate;
        }

        /**
         * 
         */
        Dataprovider_1_1Connection delegate;

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection#close()
         */
        public synchronized void close() {
            if(this.delegate != null) {
                closeConnection(this.delegate);
                this.delegate = null;
            }
        }

        /**
         * @deprecated 
         * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection#remove()
         */
        public void remove() throws ServiceException {
            close();
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0#process(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest[])
         */
        public UnitOfWorkReply[] process(
            ServiceHeader header, 
            UnitOfWorkRequest... unitsOfWork
        ) {
            return this.delegate.process(header, unitsOfWork);
        }

        /* (non-Javadoc)
         * @see org.openmdx.kernel.persistence.spi.EntityManagerFactory_2_0#createEntityManager(javax.security.auth.Subject)
         */
        public PersistenceManager createManager(
            Subject subject
        ) throws ResourceException {
            if(this.delegate instanceof ManagerFactory_2_0) {
                return ((ManagerFactory_2_0)this.delegate).createManager(subject); 
            } else throw new ResourceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "The managed connection does not implement the required interface",
                    new BasicException.Parameter("required", ManagerFactory_2_0.class.getName()),
                    new BasicException.Parameter("actual", this.delegate.getClass().getName())
                )
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.kernel.persistence.spi.ManagerFactory_2_0#createManager()
         */
        public PersistenceManager createManager(
        ) throws ResourceException {
            if(this.delegate instanceof ManagerFactory_2_0) {
                return ((ManagerFactory_2_0)this.delegate).createManager(); 
            } else throw new ResourceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "The managed connection does not implement the required interface",
                    new BasicException.Parameter("required", ManagerFactory_2_0.class.getName()),
                    new BasicException.Parameter("actual", this.delegate.getClass().getName())
                )
            );
        }


        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.OptimisticTransaction_2_0#commit(javax.transaction.Synchronization)
         */
        public void commit(
            Synchronization synchronization
        )throws ServiceException {
            if(this.delegate instanceof OptimisticTransaction_2_0) {
                ((OptimisticTransaction_2_0)this.delegate).commit(synchronization);
            } else throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "The managed connection does not implement the reqired interface",
                new BasicException.Parameter("required", OptimisticTransaction_2_0.class.getName()),
                new BasicException.Parameter("actual", this.delegate.getClass().getName())
            );
        }

    }

}
