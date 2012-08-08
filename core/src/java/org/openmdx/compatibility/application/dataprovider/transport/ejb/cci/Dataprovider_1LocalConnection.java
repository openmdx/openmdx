/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1LocalConnection.java,v 1.11 2008/09/10 08:55:24 hburger Exp $
 * Description: Local Dataprovider  Connection
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:24 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.application.dataprovider.transport.ejb.cci;

import javax.ejb.EJBException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import javax.transaction.Synchronization;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.spi.ManagerFactory_2_0;
import org.openmdx.base.persistence.spi.OptimisticTransaction_2_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_3Connection;
import org.openmdx.kernel.exception.BasicException;

/**
 * Local Dataprovider Connection
 */
public class Dataprovider_1LocalConnection
implements Dataprovider_1_3Connection
{

    /**
     * Constructor
     */
    public Dataprovider_1LocalConnection(
        Dataprovider_1_0Local local
    ) throws ServiceException {
        this.ejbLocalObject = local;
    }


    /**
     * The delegation object.
     */
    private Dataprovider_1_0Local ejbLocalObject;

    /**
     * Retrieve the underlying EJB accessor
     * 
     * @return the underlying EJB accessor
     * @throws BasicException 
     */
    protected final Dataprovider_1_0Local getDelegate(
    ) throws ServiceException{
        if(this.ejbLocalObject != null) {
            return this.ejbLocalObject;
        } else throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Attempt to use removed dataprovider connection"
        );
    }

    /**
     * Cast the delegate
     * 
     * @param extension
     * 
     * @return the delegate
     * @throws ServiceException  
     */
    @SuppressWarnings("unchecked")
    private <T> T getDelegate(
        Class<T> extension
    ) throws ServiceException {
        Dataprovider_1_0Local delegate = getDelegate();
        if(extension.isInstance(delegate)) {
            return (T)delegate;
        } else throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "The EJBLocalObject does not implement the required interface",
            new BasicException.Parameter("required", extension.getName()),
            new BasicException.Parameter("actual", delegate.getClass().getName())
        );
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
            return getDelegate().process(
                header, 
                workingUnits
            );
        } catch (Exception exception) {
            throw new RuntimeServiceException(exception).log();
        }
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0Connection
    //------------------------------------------------------------------------

    /**
     * Releases a dataprovider connection
     *
     * @exception   ServiceException    DEACTIVATION_FAILURE
     *              If deactivation of dataprovider connection fails
     */
    public void remove(
    ) throws ServiceException {
        if (this.ejbLocalObject != null) {
            try {
                this.ejbLocalObject.remove();
            } catch (Exception exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.DEACTIVATION_FAILURE,
                    "Failure during deactivation of dataprovider connection"
                );
            } finally {
                close();
            }
        }
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1_1Connection
    //------------------------------------------------------------------------

    /**
     * Close the connection.
     * <p>
     * This method does not remove the object it connects to.
     */
    public void close(
    ){
        this.ejbLocalObject = null;
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
        try {
            getDelegate(
                OptimisticTransaction_2_0.class
            ).commit(
                synchronization
            );
        } catch (TransactionRolledbackLocalException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ROLLBACK,
                "Unit of work has been rolled back"
            );
        } catch (EJBException exception) {
            throw new ServiceException(exception);
        }
    }


    //------------------------------------------------------------------------
    // Implements EntityManagerFactory_2_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.kernel.persistence.spi.EntityManagerFactory_2_0#createEntityManager(javax.security.auth.Subject)
     */
    public PersistenceManager createManager(
        Subject subject
    ) throws ResourceException {
        try {
            return getDelegate(
                ManagerFactory_2_0.class
            ).createManager(
                subject
            );
        } catch (ServiceException exception) {
            throw new ResourceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.persistence.spi.EntityManagerFactory_2_0#createEntityManager()
     */
    public PersistenceManager createManager(
    ) throws ResourceException {
        try {
            return getDelegate(
                ManagerFactory_2_0.class
            ).createManager(
            );
        } catch (ServiceException exception) {
            throw new ResourceException(exception);
        }
    }

}
