/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: DataManager_2Bean.java,v 1.5 2010/04/26 16:05:49 hburger Exp $
 * Description: Data Manager 2 Bean 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/26 16:05:49 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2010, OMEX AG, Switzerland
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
package org.openmdx.application.rest.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.openmdx.application.rest.spi.InboundConnectionFactory_2;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * Data Manager 2 Bean
 */
public class DataManager_2Bean implements SessionBean {
    
    /**
     * Implements {@link Serializable}
     */
    private static final long serialVersionUID = -1593128027032912087L;

    /**
     * The connection factory is shared among the bean the instances
     */
    private static final ConnectionFactory connectionFactory = InboundConnectionFactory_2.newInstance(
        "EntityManagerFactory"
    );
    
    /**
     * A connection is established when the bean is created
     */
    private transient Connection connection = null;
    
    /**
     * The EJB session context
     */
//  private SessionContext sessionContext;

    /**
     * The REST connection spec
     */
    private RestConnectionSpec connectionSpec;

    /**
     * Connect to an entity manager
     * 
     * @param connectionSpec
     * 
     * @throws EJBException
     */
    private void connect(
    ) throws EJBException {
        if(this.connection == null) {
            try {
                this.connection = connectionFactory.getConnection(
                    this.connectionSpec
                );
            } catch (ResourceException exception) {
                throw Throwables.initCause(
                    new EJBException("Entity manager acquisition failure"),
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE
                );
            }
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Disconnect from the entity manager
     */
    private void disconnect(
    ) throws EJBException {
        if(this.connection != null) {
            try {
                this.connection.close();
            } catch (ResourceException exception) {
                throw Throwables.initCause(
                    new EJBException("Entity manager disposal failure"),
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.DEACTIVATION_FAILURE
                );
            } finally {
                this.connection = null;
            }
        }
    }
    
    //-----------------------------------------------------------------------
    /**
     * A {@link Connection_2Home#create()} invocation leads here
     * 
     * @param         connectionSpec the REST <code>ConnectionSpec</code> 
     * 
     * @throws CreateException
     */
    public void ejbCreate(
        ConnectionSpec connectionSpec
    ) throws CreateException {
        try {
            this.connectionSpec = (RestConnectionSpec) connectionSpec;
        } catch (ClassCastException exception) {
            throw Throwables.initCause(
                new CreateException("Invalid ConnectionSpec"),
                exception, // cause
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter(
                    "expected", 
                    RestConnectionSpec.class.getName()
                ),
                new BasicException.Parameter(
                    "actual", 
                    connectionSpec == null ? null : connectionSpec.getClass().getName()
                )
            );
        }
        connect();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate(
    ) throws EJBException, RemoteException {
        // TODO restore unit of work
        connect();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate(
    ) throws EJBException, RemoteException {
        // TODO save unit of work
        disconnect();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove(
    ) throws EJBException, RemoteException {
        disconnect();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(
        SessionContext sessionContext
    ) throws EJBException, RemoteException {
//      this.sessionContext = sessionContext;
    }

    //-----------------------------------------------------------------------
    /**
     * Connection 2.0's execute method
     * 
     * @param ispec
     * @param input
     * 
     *  @return  output Record if execution of the EIS function has been 
     *           successful; null otherwise
     * 
     * @throws ResourceException
     */
    public Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException {
        Interaction interaction = this.connection.createInteraction();
        try {
            return interaction.execute(ispec, input);
        }  finally {
            interaction.close();
        }
    }
    
}
