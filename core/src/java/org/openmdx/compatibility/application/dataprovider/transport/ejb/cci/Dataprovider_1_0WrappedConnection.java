/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1_0WrappedConnection.java,v 1.3 2008/11/27 16:46:56 hburger Exp $
 * Description: Wrapped Dataprovider Local Connection
 * Revision:    $Revision: 1.3 $
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
package org.openmdx.compatibility.application.dataprovider.transport.ejb.cci;

import javax.ejb.EJBLocalObject;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_0Connection;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.kernel.exception.BasicException;

/**
 * Wrapped Dataprovider Local Connection
 */
public class Dataprovider_1_0WrappedConnection
implements Dataprovider_1_1Connection
{

    /**
     * Constructor
     */
    public Dataprovider_1_0WrappedConnection(
        DataproviderWrapper_1_0Local wrapper,
        Dataprovider_1_0 local
    ) throws ServiceException {
        this.wrapper = wrapper;
        this.processor = local;
    }

    /**
     * The wrapper EJB.
     */
    private DataproviderWrapper_1_0Local wrapper;

    /**
     * The dataprovider.
     */
    private Dataprovider_1_0 processor;


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
        UnitOfWorkRequest... workingUnits
    ){
        if(this.processor == null || this.wrapper == null) throw new RuntimeServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Attempt to use removed dataprovider connection"
        );
        try {
            return this.wrapper.process(this.processor, header, workingUnits);
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
            if (this.wrapper != null) this.wrapper.remove();
            if (this.processor instanceof EJBLocalObject) ((EJBLocalObject)this.processor).remove();
            if (this.processor instanceof Dataprovider_1_0Connection) ((Dataprovider_1_0Connection)this.processor).remove();
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
        this.wrapper = null;
        this.processor = null;
    }

}
