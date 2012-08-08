/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Hub_1Bean.java,v 1.15 2008/03/25 23:20:19 hburger Exp $
 * Description: Hub_1Bean class
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/25 23:20:19 $
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
package org.openmdx.compatibility.application.dataprovider.transport.ejb.transaction;

import java.util.Enumeration;

import javax.naming.NameClassPair;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.TransactionManager_1;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.LateBindingConnection_1;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.TransactionPolicies;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.kernel.exception.BasicException;

/**
 * Hub_1Bean
 */
public class Hub_1Bean 
    extends UnitOfWork_1Bean 
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 3256442490944237872L;

    /**
     *
     */
    Dataprovider_1_1Connection nonTransactional;


    //------------------------------------------------------------------------
    // Implements Manageable_1_0
    //------------------------------------------------------------------------

    /**
     * The activate() method is used to initialize a managed object.
     * <p>
     * An activate() implementation of a subclass must be of the form:
     * <pre>
     *   {
     *     super.activate();
     *     local activation code...
     *   }
     * </pre>
     */
    public void activate(
    ) throws Exception {

        super.activate();
        this.nonTransactional = findConnection(
            new String[]{
                TransactionPolicies.NEVER,
                "NoOrNewTransaction",
                "noOrNew"
            }
        );
        super.joiningTransaction =  findConnection(
            new String[]{
                TransactionPolicies.MANDATORY,
                "JoiningTransaction",
                "joining"
            }
        );    
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
        this.nonTransactional.close();
        super.deactivate();
    }

    protected Dataprovider_1_1Connection findConnection(
        String[] entries
    ){
        try {
            for(
                Enumeration<NameClassPair> e = getConfigurationContext().list(DATAPROVIDER_NAME_CONTEXT);
                e.hasMoreElements();
            ){
                NameClassPair ncp = e.nextElement();
                String n = ncp.getName();
                for(
                    int i = 0;
                    i < entries.length;
                    i++
                ) if(
                    entries[i].equals(n)
                ) return new LateBindingConnection_1(
                    BEAN_ENVIRONMENT + 
                    '/' + DATAPROVIDER_NAME_CONTEXT +
                    '/' + n
                );
            }
        } catch (NamingException e) {
            // Silently return null
        }
        return null;
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    /**
     * Process a single of unit of work
     *
     * @param   header          the service header
     * @param   transaction     the user transaction
     * @param   unitOfWork      thr unit of work
     *
     * @return  the reply
     */
    protected UnitOfWorkReply process(
        ServiceHeader header,
        UserTransaction transaction,
        UnitOfWorkRequest unitOfWork
    ){
        boolean transactional = unitOfWork.isTransactionalUnit();
        try {
            super.preProcess(header,unitOfWork);
        } catch (ServiceException exception) {
            return new UnitOfWorkReply(exception);
        }
        if(transactional){
            if(super.joiningTransaction == null) return new UnitOfWorkReply(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("transactional",Boolean.TRUE),
                    },
                    "The current configuration does not support transactional units of work"
                )
            );
            try {
                TransactionManager_1.execute(
                    transaction,
                    this, 
                    1000L * getTimeoutInSeconds()
                );
                return super.postProcess();
            } catch (ServiceException exception) {
                return new UnitOfWorkReply(exception);
            }
        } else {
            if(this.nonTransactional == null) return new UnitOfWorkReply(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("transactional",Boolean.FALSE),
                    },
                    "The current configuration does not support non-transactional units of work"
                )
            );
            try {
                super.reply = this.nonTransactional.process(
                    super.header,
                    super.request
                );
                return super.postProcess();
            } catch (RuntimeException exception) {
                throw new RuntimeServiceException(
                    new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ABORT,
                        null,
                        "Non-transactional unit of work aborted with " +
                        "RuntimeException"
                    )
                ).log();
            }
        }
    }

}
