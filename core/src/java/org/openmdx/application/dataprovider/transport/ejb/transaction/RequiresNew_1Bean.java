/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RequiresNew_1Bean.java,v 1.3 2009/01/11 18:24:48 wfro Exp $
 * Description: RequiresNew_1Bean
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/11 18:24:48 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application.dataprovider.transport.ejb.transaction;

import java.util.Date;

import javax.naming.NameNotFoundException;
import javax.transaction.UserTransaction;

import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.application.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.application.dataprovider.transport.ejb.cci.DataproviderWrapper_1_0;
import org.openmdx.application.dataprovider.transport.ejb.spi.AbstractDataprovider_1Bean;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.base.transaction.Synchronization_1_0;
import org.openmdx.base.transaction.TransactionManager_1;
import org.openmdx.kernel.exception.BasicException;


/**
 * RequiresNew_1Bean
 * 
 * This EJB should have the transaction attribute NotSupported!
 */
public class RequiresNew_1Bean
extends AbstractDataprovider_1Bean
implements DataproviderWrapper_1_0 
{

    /**
     * Serial Version UID to avoid warning.
     */
    private static final long serialVersionUID = 3257846588834788148L;

    /**
     * The transaction timeout in milliseconds.
     */
    protected long timeout;


    //------------------------------------------------------------------------
    // Implements Manageable_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.kernel.application.cci.Manageable_1_0#activate()
     */
    public void activate() throws Exception {
        super.activate();
        //
        // TRANSACTION/timeout
        //
        try {
            Object timeout = getConfigurationContext().lookup("TRANSACTION/timeout");
            try {
                this.timeout = ((Long)timeout).longValue();
            } catch(ClassCastException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "Transaction timeout must be specified in milliseconds",
                    new BasicException.Parameter("timeout", timeout)
                );
            }
            if(this.timeout < -1L) this.timeout = -1L;
        } catch(NameNotFoundException e) {
            this.timeout = -1L;
        }
    }


    //------------------------------------------------------------------------
    // Implements DataproviderWrapper_1_0
    //------------------------------------------------------------------------

    /**
     * Creates a Unit Of Work Id
     * 
     * @return a new unit of work id
     */
    protected String createUnitOfWorkId(){
        return uuidAsString();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.DataproviderWrapper_1_0#process(org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1_0Local, org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest[])
     */
    public UnitOfWorkReply[] process(
        final Dataprovider_1_0 processor, 
        final ServiceHeader header, 
        final UnitOfWorkRequest[] unitsOfWork
    ) {
        UserTransaction transaction = getSessionContext().getUserTransaction();
        final UnitOfWorkReply[] replies = new UnitOfWorkReply[unitsOfWork.length];
        final UnitOfWorkRequest[] request = new UnitOfWorkRequest[1];
        final ServiceHeader serviceHeader = new ServiceHeader(
            header.getPrincipalChain().toArray(
                new String[header.getPrincipalChain().size()]
            ),
            header.getCorrelationId(),
            header.traceRequest(),
            header.getQualityOfService(),
            DateFormat.getInstance().format(new Date()),
            header.getRequestedFor()
        );
        for (
            int i = 0;
            i < unitsOfWork.length;
            i++
        ){
            final int index = i;
            try {
                TransactionManager_1.execute(
                    transaction,
                    new Synchronization_1_0 () {

                        public void afterBegin(
                        ) throws ServiceException {
                            String unitOfWorkId = createUnitOfWorkId();
                            request[0] = unitsOfWork[index];
                            DataproviderRequest[] dataproviderRequests = request[0].getRequests();
                            for(
                                int i=0;
                                i<dataproviderRequests.length;
                                i++
                            ) dataproviderRequests[i].context(
                                DataproviderRequestContexts.UNIT_OF_WORK_ID
                            ).set(
                                0,
                                unitOfWorkId
                            );
                        }

                        public void beforeCompletion(
                        ) {
                            UnitOfWorkReply[] reply = processor.process(serviceHeader, request);
                            if(reply[0].failure()) {
                                throw new RuntimeServiceException(reply[0].getStatus());
                            } else {
                                replies[index] = reply[0];
                            }
                        }

                        public void afterCompletion(
                           int status
                        ) {
                            request[0] = null;
                        }

                    },
                    this.timeout
                );
            } 
            catch (ServiceException exception) {
                replies[index] = new UnitOfWorkReply(exception);
            }
        }
        return replies;
    }

}
