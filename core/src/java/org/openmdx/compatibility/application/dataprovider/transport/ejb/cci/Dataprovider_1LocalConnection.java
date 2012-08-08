/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1LocalConnection.java,v 1.5 2008/02/29 18:24:50 hburger Exp $
 * Description: Local Dataprovider  Connection
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 18:24:50 $
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

import javax.jdo.PersistenceManager;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.object.spi.PersistenceManagerFactory_2_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_2Connection;
import org.openmdx.kernel.exception.BasicException;

/**
 * Local Dataprovider Connection
 */
public class Dataprovider_1LocalConnection<D extends Dataprovider_1_0Local>
    implements Dataprovider_1_2Connection
{

    /**
     * Constructor
     */
    public Dataprovider_1LocalConnection(
        D local
    ) throws ServiceException {
        this.ejbLocalObject = local;
    }

    
    /**
     * The delegation object.
     */
    private D ejbLocalObject;

    /**
     * Retrieve the underlying EJB accessor
     * 
     * @return the underlying EJB accessor
     */
    protected D getDelegate(){
        return this.ejbLocalObject;
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
            if(this.ejbLocalObject == null) throw new BasicException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                null,
                "Attempt to use removed dataprovider connection"
            );
            return this.ejbLocalObject.process(
                header, 
                workingUnits
            );
        } catch (Exception exception) {
            throw new RuntimeServiceException(exception).log();
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
        try {
            if (this.ejbLocalObject != null) this.ejbLocalObject.remove();
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.DEACTIVATION_FAILURE,
                null,
                "Failure during deactivation of dataprovider connection"
            );
        } finally {
            close();
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
    // Implements PersistenceManagerFactory_2_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManagerFactory_2_0#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager() throws ServiceException {
        return this.ejbLocalObject instanceof PersistenceManagerFactory_2_0 ?
            ((PersistenceManagerFactory_2_0)this.ejbLocalObject).getPersistenceManager() :
            null;
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManagerFactory_2_0#getPersistenceManager(java.lang.String, java.lang.String)
     */
    public PersistenceManager getPersistenceManager(
        String userid,
        String password
    ) throws ServiceException {
        return this.ejbLocalObject instanceof PersistenceManagerFactory_2_0 ? 
            ((PersistenceManagerFactory_2_0)this.ejbLocalObject).getPersistenceManager(userid, password) :
            null;
    }

    //------------------------------------------------------------------------
    // Implements PersistenceManagerFactory_1_0
    //------------------------------------------------------------------------


    /* (non-Javadoc)
     * @see org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.PersistenceManagerFactory_1_0#getPersistenceManager(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader)
     */
    public PersistenceManager getPersistenceManager(
        ServiceHeader serviceHeader
    ) throws ServiceException {
        return this.ejbLocalObject instanceof PersistenceManagerFactory_1_0 ? 
            ((PersistenceManagerFactory_1_0)this.ejbLocalObject).getPersistenceManager(serviceHeader) :
            null;
    }

}
