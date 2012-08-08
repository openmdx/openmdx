/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1Bean.java,v 1.12 2008/03/06 19:03:24 hburger Exp $
 * Description: Transaction Controller
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/06 19:03:24 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004.2008, OMEX AG, Switzerland
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

import javax.transaction.UserTransaction;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.TransactionManager_1;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.LateBindingConnection_1;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.kernel.exception.BasicException;

/**
 * Transaction Controller
 */
public class Dataprovider_1Bean 
    extends UnitOfWork_1Bean 
    implements Dataprovider_1_0
{

    //------------------------------------------------------------------------
    // Implements Manageable_1_0
    //------------------------------------------------------------------------

    /**
     * 
     */
    private static final long serialVersionUID = 3258688788988572464L;


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
        joiningTransaction = new LateBindingConnection_1(
            BEAN_ENVIRONMENT + 
            '/' + DATAPROVIDER_NAME_CONTEXT +
            '/' + "server"
        );
    }
  

    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    /**
     * Process a single of unit of work
     *
     * @param   header          the service header
     * @param   transaction     the user transaction
     * @param   unitOfWork      the unit of work
     *
     * @return  the reply
     */
    protected UnitOfWorkReply process(
        ServiceHeader header,
        UserTransaction transaction,
        UnitOfWorkRequest unitOfWork
    ){
        try {
            super.preProcess(header,unitOfWork);
        } catch (ServiceException exception) {
            return new UnitOfWorkReply(exception);
        }
        try {
            TransactionManager_1.execute(
                transaction,
                this, 
                1000L * getTimeoutInSeconds()
            );            
            return super.postProcess();
        } catch (ServiceException exception) {
            return new UnitOfWorkReply(
                ! unitOfWork.isTransactionalUnit() && (
                    exception.getExceptionCode() == BasicException.Code.ROLLBACK ||
                    exception.getExceptionCode() == BasicException.Code.HEURISTIC
                ) && (
                    exception.getExceptionStack() != null &&
                    exception.getExceptionStack().getCause() instanceof BasicException 
                ) ? new ServiceException(
                    (BasicException)exception.getExceptionStack().getCause()
                ) : exception
            );          
        }
    }

}
