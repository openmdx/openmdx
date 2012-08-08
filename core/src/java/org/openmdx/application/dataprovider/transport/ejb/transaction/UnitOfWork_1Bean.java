/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: UnitOfWork_1Bean.java,v 1.3 2009/03/03 15:25:47 hburger Exp $
 * Description: Unit Of Work Bean
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/03 15:25:47 $
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
package org.openmdx.application.dataprovider.transport.ejb.transaction;

import java.io.File;
import java.util.Date;

import javax.naming.NameNotFoundException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.application.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.application.dataprovider.transport.ejb.spi.AbstractDataprovider_1Bean;
import org.openmdx.application.dataprovider.transport.rmi.RMIMapper;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.base.transaction.Synchronization_1_0;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.common.CommonConfigurationEntries;
import org.openmdx.kernel.exception.BasicException;

/**
 * Unit Of Work Bean
 */
@SuppressWarnings("serial")
public abstract class UnitOfWork_1Bean 
    extends AbstractDataprovider_1Bean 
    implements Dataprovider_1_0, Synchronization_1_0 {

    /**
     *
     */
    Dataprovider_1_1Connection joiningTransaction;

    /**
     * An EJB instance is single-threaded.
     */
    UnitOfWorkRequest[] request = new UnitOfWorkRequest[1];

    /**
     * An EJB instance is single-threaded.
     */
    UnitOfWorkReply[] reply = null;

    /**
     * An EJB instance is single-threaded.
     */
    private Synchronization_1_0 streamSynchronization;

    /**
     * An EJB instance is single-threaded.
     */
    ServiceHeader header = null;

    /**
     * Transaction Timeout
     */
    private int timeoutInSeconds;

    /**
     * 
     */   
    private boolean trustPeer;

    /**
     * 
     */   
    private boolean trustTransactionTime;

    /**
     * 
     */   
    private boolean ignorePrincipal;

    /**
     * 
     */   
    private boolean appendPrincipalName;

    /**
     * 
     */   
    private boolean appendPrincipalString;

    /**
     * 
     */
    private boolean keepDate;

    /**
     * Defines the directory to be used to buffer streams at transaction 
     * boundaries.
     */
    private File streamBufferDirectory;

    /**
     * Defines the chunk size used to buffer and flush streams.
     */
    private int streamChunkSize;

    /**
     * 
     */
    protected static final String DATAPROVIDER_NAME_CONTEXT = "ejb";

    /**
     * 
     */
    protected static final int DEFAULT_STREAM_CHUNK_SIZE = 10000;


    //------------------------------------------------------------------------
    // Extends SessionBean_1
    //------------------------------------------------------------------------

    /**
     * This bean's exception domain
     */
    final public String exDomain(
    ) {
        return BasicException.Code.DEFAULT_DOMAIN;
    }


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
            Object timeout = getConfigurationContext().lookup(
                "TRANSACTION/timeout"
            );
            Long timeoutInMillisecods;
            try {
                timeoutInMillisecods = (Long)timeout;
            } catch(ClassCastException e) {
                throw new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "Transaction timeout must be specified in milliseconds",
                    new BasicException.Parameter("timeout", timeout)
                );
            }
            this.timeoutInSeconds = timeoutInMillisecods.longValue() < 0L ? -1 :
                timeoutInMillisecods.longValue() < 1000L ? 1 :
                    (int) (timeoutInMillisecods.longValue() / 1000L);
        } catch(NameNotFoundException e) {
            this.timeoutInSeconds = -1;
        }
        //
        // PERSISTENCE/streamBufferDirectory
        //
        try {
            Object value = getConfigurationContext().lookup(
                "PERSISTENCE/" + CommonConfigurationEntries.STREAM_BUFFER_DIRECTORY
            );
            String pathname = value == null ? null : value.toString().trim();
            this.streamBufferDirectory = pathname == null || pathname.length() == 0 ? 
                null :
                    new File(pathname);
        } catch(NameNotFoundException e) {
            this.streamBufferDirectory = null;
        }
        //
        // PERSISTENCE/chunkSize
        //
        try {
            Object streamChunkSize = getConfigurationContext().lookup(
                "PERSISTENCE/" + CommonConfigurationEntries.CHUNNK_SIZE
            );
            try {
                this.streamChunkSize = ((Integer)streamChunkSize).intValue();
            } catch(ClassCastException e) {
                throw new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "The stream chunk sizes is an integer defining how many bytes or characters are transferred at once",
                    new BasicException.Parameter("streamChunkSize", streamChunkSize)
                );
            }
            if(this.streamChunkSize <= 0) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "The stream chunk size defining how many bytes or characters are transferred at once must be positive",
                new BasicException.Parameter("streamChunkSize", streamChunkSize)
            );
        } catch(NameNotFoundException e) {
            this.streamChunkSize = DEFAULT_STREAM_CHUNK_SIZE;
        }
        //
        // SECURITY/trustPeer
        //
        try {
            this.trustPeer = (
                    (Boolean)getConfigurationContext().lookup("SECURITY/trustPeer")
            ).booleanValue();
        } catch(NameNotFoundException e) {
            this.trustPeer = true;
        } catch(ClassCastException e) {
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "The trustPeer flag must be specified as Boolean"
            );
        }
        //
        // SECURITY/trustTransactionTime
        // 
        try {
            this.trustTransactionTime = (
                    (Boolean)getConfigurationContext().lookup("SECURITY/trustTransactionTime")
            ).booleanValue();
        } catch(NameNotFoundException e) {
            this.trustTransactionTime = false;
        } catch(ClassCastException e) {
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "The trustTransactionTime flag must be specified as Boolean"
            );
        }
        //
        // SECURITY/appendPrincipal
        // 
        try {
            Object appendPrincipal = getConfigurationContext().lookup("SECURITY/appendPrincipal");
            this.appendPrincipalName = "Name".equals(appendPrincipal);
            this.appendPrincipalString = "String".equals(appendPrincipal);
            this.ignorePrincipal = "false".equals(appendPrincipal); 
            if(
                    !this.appendPrincipalName & 
                    !this.appendPrincipalString &
                    !this.ignorePrincipal
            ) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "The appendPrincipal value must be one of [\"false\", \"Name\", \"String\"]",
                new BasicException.Parameter("appendPrincipal", appendPrincipal)
            );
        } catch(NameNotFoundException e) {
            this.ignorePrincipal = true;
            this.appendPrincipalName = false; 
            this.appendPrincipalString = false;
        }

    }

    /**
     * The deactivate() method is used to release a managed object's 
     * resources.
     * <p>
     * A deactivate() implementation of a subclass must be of the form:
     * <pre>
     *   {
     *     local deactivation code...
     *     super.deactivate();
     *   }
     * </pre>
     */
    public void deactivate(
    ) throws Exception {
        joiningTransaction.close();
        super.deactivate();
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    /**
     * Process a sequence of units of work
     *
     * @param   header          the service header
     * @param   unitsOfWork     thr units of work
     *
     * @return  a sequnce of unit of work replies
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest... unitsOfWork
    ){
        UserTransaction transaction = getSessionContext().getUserTransaction();
        UnitOfWorkRequest[] requests = RMIMapper.unmarshal(unitsOfWork);
        UnitOfWorkReply[] replies = new UnitOfWorkReply[unitsOfWork.length];
        for (
                int index = 0;
                index < unitsOfWork.length;
                index++
        ) replies[index] = process(header,transaction,requests[index]);
        return RMIMapper.marshal(replies);
    }

    /**
     * Adjust the Service Header
     * 
     * @param source
     * @return the adjusted header
     */
    private ServiceHeader adjustHeader(
        ServiceHeader source
    ){
        // 
        // Adjust Requested At
        //
        String requestedAt = this.keepDate ?
            source.getRequestedAt() :
                DateFormat.getInstance().format(new Date());
            //
            // Adjust Principal Chain 
            //
            String[] principalChain = new String[
                                                 (this.trustPeer ? source.getPrincipalChain().size() : 0) +
                                                 (this.ignorePrincipal ? 0 : 1)
                                                 ];
            if(this.trustPeer) source.getPrincipalChain().toArray(principalChain);
            if(this.appendPrincipalName) {
                String principal = this.getSessionContext().getCallerPrincipal().getName();
                logger.debug("appending principal.getName()|{}", principal);
                principalChain[principalChain.length - 1] = principal;
            }
            if(this.appendPrincipalString) {
                String principal = this.getSessionContext().getCallerPrincipal().toString();
                logger.debug("appending principal.toString()|{}", principal);
                principalChain[principalChain.length - 1] = principal;
            }
            if(this.ignorePrincipal) {
                logger.debug("not appending any principal");
            }
            ServiceHeader header = new ServiceHeader(
                principalChain,
                source.getCorrelationId(),
                source.traceRequest(),
                source.getQualityOfService(),
                requestedAt,
                source.getRequestedFor()
            );
            logger.debug("adjusted ServiceHeader|{}", header);
            return header;
    }

    /**
     * Pre-process a single of unit of work
     *
     * @param   header          the service header
     * @param   unitOfWork      thr unit of work
     *
     * @return  the reply
     * @throws ServiceException 
     */
    protected void preProcess(
        ServiceHeader header,
        UnitOfWorkRequest unitOfWork
    ) throws ServiceException{
        //
        // Adjust Transaction Policy
        //
        boolean transactionBoundary = unitOfWork.isTransactionalUnit();
        unitOfWork.setTransactionalUnit(false);
        // 
        // Determine timestamp policy
        //
        this.keepDate = (
                this.trustTransactionTime || !transactionBoundary
        ) && header.getRequestedAt() != null;
        //
        // Adjust the header if necessary
        //        
        this.header = this.keepDate && this.trustPeer && this.ignorePrincipal ?
            header :
                adjustHeader(header);
        //
        // Create Unit Of Work Id
        //
        String unitOfWorkId = super.uuidAsString();
        //
        // Request and Reply objects
        // 
        this.request[0] = unitOfWork;
        this.streamSynchronization = RMIMapper.intercept(
            this.request, 
            unitOfWorkId, 
            this.streamBufferDirectory, 
            this.streamChunkSize
        );
        this.reply = null;
        //
        // Propagate Unit Of Work Id
        //
        DataproviderRequest[] requests = unitOfWork.getRequests();
        for(
                int i=0;
                i<requests.length;
                i++
        ){
            DataproviderRequest request = requests[i];
            request.context(DataproviderRequestContexts.UNIT_OF_WORK_ID).set(0,unitOfWorkId);
        }
    }

    /**
     * Post-process a single unit of work
     * 
     * @return the unit of work reply
     */
    protected UnitOfWorkReply postProcess(
    ) {
        try {
            this.streamSynchronization.afterCompletion(
                !this.reply[0].failure() ?
                    Status.STATUS_COMMITTED :
                    Status.STATUS_ROLLEDBACK
            );
        } 
        catch (Exception exception) {
            return new UnitOfWorkReply(
                new ServiceException(exception)
            );
        }
        return RMIMapper.intercept(this.reply)[0];
    }

    /**
     * Process a single of unit of work
     *
     * @param   header          the service header
     * @param   transaction     the user transaction
     * @param   unitOfWork      the unit of work
     *
     * @return  the reply
     */
    abstract protected UnitOfWorkReply process(
        ServiceHeader header,
        UserTransaction transaction,
        UnitOfWorkRequest unitOfWork
    );

    protected int getTimeoutInSeconds(
    ){
        return this.timeoutInSeconds;
    }

    //------------------------------------------------------------------------
    // Implements Synchronization_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.Synchronization_1_0#afterBegin()
     */
    public void afterBegin() throws ServiceException {
        //
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.Synchronization_1_0#beforeCompletion()
     */
    public void beforeCompletion(
    ) {
        this.reply = this.joiningTransaction.process(
            this.header,
            this.request
        );
        if(this.reply[0].failure()) {
            throw new RuntimeServiceException(this.reply[0].getStatus());
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.Synchronization_1_0#afterCompletion(boolean)
     */
    public void afterCompletion(
        int status
    ) {
        //
    }

}
