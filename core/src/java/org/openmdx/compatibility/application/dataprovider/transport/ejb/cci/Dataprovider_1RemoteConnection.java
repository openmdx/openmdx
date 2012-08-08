/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1RemoteConnection.java,v 1.5 2008/11/27 16:46:56 hburger Exp $
 * Description: Dataprovider_1_0RemoteConnection class
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/27 16:46:56 $
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

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.ejb.Handle;

import org.openmdx.base.exception.RemoteExceptions;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.dataprovider.transport.rmi.RMIMapper;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Dataprovider_1_0RemoteConnection
 */
public class Dataprovider_1RemoteConnection<D extends Dataprovider_1_0Remote>
implements Dataprovider_1_1Connection, Serializable
{

    /**
     * Constructor
     *
     * @param remote
     * 
     * @throws ServiceException
     */
    public Dataprovider_1RemoteConnection(
        D remote
    ) throws ServiceException {
        this.ejbObject = remote;
        try {
            this.ejbHandle = this.ejbObject.getHandle();      
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.ACTIVATION_FAILURE,
                "Could not acquire the provider's EJB handle"
            );
        }
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3258133569992734770L;

    /**
     * The delegation object.
     */
    private transient D ejbObject;

    /**
     * The delegation object handle.
     */
    private Handle ejbHandle;

    /**
     * Retrieve the underlying EJB accessor
     * 
     * @return the underlying EJB accessor
     */
    @SuppressWarnings("unchecked")
    protected D getDelegate() throws RemoteException, BasicException {
        if(this.ejbHandle == null) throw new BasicException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Attempt to use removed dataprovider connection"
        );
        return this.ejbObject == null ?
            this.ejbObject = (D)this.ejbHandle.getEJBObject() :
                this.ejbObject;
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
    @SuppressWarnings("unchecked")
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest... workingUnits
    ){
        try {
            UnitOfWorkRequest[] requests = RMIMapper.marshal(workingUnits);
            UnitOfWorkReply[] replies = null;
            try {
                SysLog.detail("Processing units of work", Integer.valueOf(requests.length));
                replies = getDelegate().process(header,requests);
            } catch (RemoteException exception){
                this.ejbObject = null;
                if(RemoteExceptions.isRetriable(exception)) {
                    try {
                        SysLog.info("Reconnecting", this.ejbHandle);
                        replies = getDelegate().process(header,requests);
                    } catch (RemoteException retryException){
                        this.ejbObject = null;
                    }
                } else {
                    throw exception;
                }
            }
            SysLog.detail("Processed units of work", Integer.valueOf(replies.length));
            return RMIMapper.unmarshal(replies);
        } catch (Exception exception) {
            throw new RuntimeServiceException(exception).log();
        }
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1_1Connection
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
            if (this.ejbObject != null) this.ejbObject.remove();
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

    /**
     * Close the connection.
     * <p>
     * This method does not remove the object it connects to.
     */
    public void close(
    ){
        this.ejbObject = null;
        this.ejbHandle = null;
    }

}
