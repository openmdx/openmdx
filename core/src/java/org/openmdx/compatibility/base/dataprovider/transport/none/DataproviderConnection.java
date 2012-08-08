/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderConnection.java,v 1.4 2007/12/13 18:19:20 hburger Exp $
 * Description: Dataprovider_1_0Connection Implementation class
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/13 18:19:20 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.none;

import java.io.Serializable;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_0Connection;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 * @deprecated in favour of {@link 
 * org.openmdx.kernel.application.container.lightweight.LightweightContainer 
 * LightweightContainer} and its implementation classes.
 */
class DataproviderConnection
    implements Dataprovider_1_1Connection, Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3833183614451397683L;

    /**
     * Constructor
     */
    DataproviderConnection(
        ManagedDataproviderConnectionFactory connectionFactory
    ){
        this.connectionFactory = connectionFactory;
        this.registrationId = connectionFactory.getRegistrationId();
    }

    
    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    /**
     * Process a set of working units
     *
     * @param   header          the service header
     * @param   workingUnits    a collection of working units
     *
     * @return  a collection of working unit replies
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest[] workingUnits
    ){
        try {
            Dataprovider_1_0Connection connection = null;
            try {
                connection = getManagedConnectionFactory().getConnection();
            } catch (NullPointerException exception) {
                throw new BasicException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    null,
                    "Dataprovider connection has been removed"
                );
            }
            UnitOfWorkReply[] result = connection.process(header, workingUnits);
            getManagedConnectionFactory().returnConnection(connection);
            return result;
        } catch (Exception exception) {
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PROCESSING_FAILURE,
                null,
                "System exception, discarding Dataprovider instance"
            ).log();
        }
    }

        
    //------------------------------------------------------------------------
    // Implements LifeCycleObject_1_0
    //------------------------------------------------------------------------

    /**
     * Releases a dataprovider connection
     *
     * @exception   ServiceException    DEACTIVATION_FAILURE
     *              If decativation of dataprovider connection fails
     */
    public void remove(
    ) throws ServiceException {
        close();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_0Connection#close()
     */
    public void close() {
        this.connectionFactory = null;
    }

    /**
     * Get the connection factory.
     * If it is not initialised, use service locator to locate it.
     */
    private ManagedDataproviderConnectionFactory getManagedConnectionFactory(
    ) throws ServiceException {
        return this.connectionFactory != null ?
            this.connectionFactory :
            (ManagedDataproviderConnectionFactory) 
			org.openmdx.compatibility.base.application.container.SimpleServiceLocator.getInstance(
            ).lookup(
            CONTAINER_CONTEXT + ":" + new Path(new String[]{ this.registrationId })
        );
    }
    
    
    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------
    
    /**
     * The dataprovider managed connection factory.
     */
    private transient ManagedDataproviderConnectionFactory connectionFactory;
    
    /**
     * Registration id at which the connection factory is located in service locator.
     */
    private final String registrationId;
    
    /**
     * The base context for container.
     */
    final static String CONTAINER_CONTEXT = "org:openmdx:compatibility:dataprovider:container";
    
}
