/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Transactions 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
package org.openmdx.base.persistence.spi;

import javax.jdo.JDOFatalInternalException;
import javax.jdo.Transaction;

import org.openmdx.kernel.loading.Classes;

/**
 * Transactions
 */
public class Transactions {

    /**
     * Constructor 
     */
    private Transactions() {
        // Avoid instantiation
    }

    /**
     * The transaction factory
     */
    private static TransactionFactory transactionFactory;
    
    /**
     * Retrieve transactionFactory.
     *
     * @return Returns the transactionFactory.
     */
    private static TransactionFactory getTransactionFactory() {
        if(transactionFactory == null) try {
            transactionFactory = Classes.newApplicationInstance(
                TransactionFactory.class, 
                "org.openmdx.application.persistence.adapter.TransactionAdapterFactory"
             );
        } catch (Exception exception) {
            throw new JDOFatalInternalException(
                "Transaction factory acquisition failure",
                exception
            );
        }
        return transactionFactory;
    }

    /**
     * Create a transaction adapter
     * 
     * @param unitOfWork the unit of worked to be wrapped
     * 
     * @return a transaction adapter for the given unit of work
     */
    public static Transaction toTransaction(
        UnitOfWork unitOfWork
    ){  
        return getTransactionFactory().toTransaction(unitOfWork);
    }

}
