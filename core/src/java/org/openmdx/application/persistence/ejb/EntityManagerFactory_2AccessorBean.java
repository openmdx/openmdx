/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: EntityManagerFactory_2AccessorBean.java,v 1.9 2009/06/12 20:17:21 wfro Exp $
 * Description: EntityManagerFactory_2AccessorBean 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/12 20:17:21 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.openmdx.application.Version;
import org.openmdx.application.rest.ejb.Connection_2Home;
import org.openmdx.application.rest.spi.InboundConnectionFactory_2;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * EntityManagerFactory_2AccessorBean
 *
 */
public class EntityManagerFactory_2AccessorBean 
    implements SessionBean, SessionSynchronization
{
    
    //-----------------------------------------------------------------------
    /**
     * The entity manager factory name may be overridden by a sub-class
     * @return
     */
    protected String getEntityManagerFactoryName(
    ){
        return "java:comp/env/ejb/EntityManagerFactory";
    }

    //-----------------------------------------------------------------------
    /**
     * Retrieve the active connection
     * 
     * @return the active connection
     * 
     * @throws EJBException if the connection is not active
     */
    private ConnectionFactory getConnectionFactory(
        List<String> principalChain
    ) throws ResourceException {
        if(this.connectionFactory == null) {
            this.principalChain = principalChain;
            this.connectionFactory = InboundConnectionFactory_2.newInstance(
                this.getEntityManagerFactoryName(),
                principalChain
            );            
        }
        return this.connectionFactory;
    }

    //-----------------------------------------------------------------------
    protected Connection getConnection(
    ) throws ResourceException {
        if(this.connection != null) {
            return this.connection;
        } 
        else {
            throw new EJBException(
                "This EJB's connection is not active"             
            );
        }
    }

    //-----------------------------------------------------------------------
    /**
     * This method is shared by both <code>ejbCreate()</code> methods.
     * 
     * @throws CreateException
     */
    private Connection getConnection(
        List<String> principalChain
    ) throws ResourceException {
        if(this.connection == null) {
            this.connection = this.getConnectionFactory(principalChain).getConnection();
            this.connection.getLocalTransaction().begin();
        }
        if(
            ((principalChain == null) && (this.principalChain != null)) ||
            ((this.principalChain == null) && (principalChain != null)) ||
            !principalChain.equals(this.principalChain) 
        ) {
            throw new ResourceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Principal chain mismatch. Changing principal chain for stateful connections is not allowed",
                    new BasicException.Parameter("principalChain.requested", principalChain),
                    new BasicException.Parameter("principalChain.connection", this.principalChain)
                 )
             );
        }        
        return this.connection;
    }
    
    //-----------------------------------------------------------------------
    /**
     * A {@link Connection_2Home#create()} invocation leads here
     * 
     * @throws CreateException
     */
    public void ejbCreate(
    ) throws CreateException {
    }

    //-----------------------------------------------------------------------
    /**
     * Retrieve the connection user name
     * 
     * @return the connection user name
     */
    protected String getConnectionUserName(
    ){
        return this.sessionContext.getCallerPrincipal().getName();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate(
    ) throws EJBException, RemoteException {
        this.metaData = new ConnectionMetaData (
        ) {
            public String getEISProductName(
            ) throws ResourceException {
                return "openMDX/REST";
            }

            public String getEISProductVersion(
            ) throws ResourceException {
                return Version.getSpecificationVersion();
            }

            public String getUserName(
            ) throws ResourceException {
                return getConnectionUserName();
            }            
        };        
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate(
    ) throws EJBException, RemoteException {
        if(this.connection != null) {
            try {
                this.connection.close();
            } 
            catch (ResourceException exception) {
                throw Throwables.initCause(
                    new EJBException("Entity manager disposal failure"),
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.DEACTIVATION_FAILURE
                );
            } 
            finally {
                this.connection = null;
            }
        }
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove(
    ) throws EJBException, RemoteException {
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(
        SessionContext ctx
    ) throws EJBException, RemoteException {
        this.sessionContext = ctx;
    }

    //-----------------------------------------------------------------------
    public void afterBegin(
    ) throws EJBException, RemoteException {
        if(this.connection != null) {
            try {
                this.connection.getLocalTransaction().begin();
            }
            catch(ResourceException e) {
                throw Throwables.initCause(
                    new EJBException("Unable to begin transaction"),
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE
                );            
            }
        }
    }

    //-----------------------------------------------------------------------
    public void afterCompletion(
        boolean committed
    ) throws EJBException, RemoteException {
        try {
            Synchronization synchronization = (Synchronization)this.getConnection();
            synchronization.afterCompletion(
                committed ? Status.STATUS_COMMITTED : Status.STATUS_ROLLEDBACK
            );
        }
        catch(ResourceException e) {
            throw Throwables.initCause(
                new EJBException("Unable to get connection"),
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE
            );            
        }
    }

    //-----------------------------------------------------------------------
    public void beforeCompletion(
    ) throws EJBException, RemoteException {
        Synchronization synchronization;
        try {
            synchronization = (Synchronization)this.getConnection();
        } 
        catch (ResourceException e) {
            throw Throwables.initCause(
                new EJBException("Unable to get connection"),
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE
            );                        
        }
        try {
            synchronization.beforeCompletion();
        } 
        catch (RuntimeException exception) {
            this.sessionContext.setRollbackOnly();
            throw new EJBTransactionRolledbackException(
                "beforeCompletion() failed",
                exception
            );
        }
    }
    
    //-----------------------------------------------------------------------
    public ConnectionMetaData getMetaData(
    ) throws ResourceException {
        return this.metaData;
    }

    //-----------------------------------------------------------------------
    public Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException {
        Connection connection = this.getConnection(
            Arrays.asList(((RestInteractionSpec)ispec).getPrincipalChain())
        );
        Interaction interaction = connection.createInteraction();
        try {
            return interaction.execute(ispec, input);
        } 
        catch (ResourceException exception) {
            this.sessionContext.setRollbackOnly();
            throw exception;
        } 
        finally {
            interaction.close();
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    
    /**
     * Implements   {@link Serializable}
     */
    private static final long serialVersionUID = -1593128027032912087L;

    /**
     * @serial the connection factory is de-serialized on re-activation
     */
    private ConnectionFactory connectionFactory = null;
    
    private List<String> principalChain = null;
    
    /**
     * A new connection is established upon re-activation
     */
    private transient Connection connection = null;
    
    /**
     * The EJB session context
     */
    private SessionContext sessionContext;
    
    /**
     * The connection meta data object
     */
    private ConnectionMetaData metaData;
    
}
