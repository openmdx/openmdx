/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: User Transaction Support
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2010, OMEX AG, Switzerland
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
package org.openmdx.base.resource.spi;

import javax.resource.ResourceException;
import javax.transaction.UserTransaction;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.naming.ComponentEnvironment;

/**
 * Transaction Factory
 */
public class UserTransactions {

    /**
     * Constructor 
     */
    private UserTransactions() {
        // Avoid instantiation
    }

    /**
     * The string representation of the status values
     */
    private static final String[] STATI = {
        //
        // A transaction is associated with the target object and it is in the
        // active state. An implementation returns this status after a
        // transaction has been started and prior to a Coordinator issuing
        // any prepares, unless the transaction has been marked for rollback.
        //  
        "STATUS_ACTIVE",
        //
        // A transaction is associated with the target object and it has been
        // marked for rollback, perhaps as a result of a setRollbackOnly operation.
        //
        "STATUS_MARKED_ROLLBACK",
        //
        // A transaction is associated with the target object and it has been
        // prepared. That is, all subordinates have agreed to commit. The
        // target object may be waiting for instructions from a superior as to how
        // to proceed.
        //  
        "STATUS_PREPARED",
        //
        // A transaction is associated with the target object and it has been
        // committed. It is likely that heuristics exist; otherwise, the
        // transaction would have been destroyed and NoTransaction returned.
        //  
        "STATUS_COMMITTED",
        //
        // A transaction is associated with the target object and the outcome
        // has been determined to be rollback. It is likely that heuristics exist;
        // otherwise, the transaction would have been destroyed and NoTransaction
        // returned.
        //  
        "STATUS_ROLLEDBACK",
        //
        // A transaction is associated with the target object but its
        // current status cannot be determined. This is a transient condition
        // and a subsequent invocation will ultimately return a different status.
        //
        "STATUS_UNKNOWN",
        //
        // No transaction is currently associated with the target object. This
        // will occur after a transaction has completed.
        //
        "STATUS_NO_TRANSACTION",
        //
        // A transaction is associated with the target object and it is in the
        // process of preparing. An implementation returns this status if it
        // has started preparing, but has not yet completed the process. The
        // likely reason for this is that the implementation is probably
        // waiting for responses to prepare from one or more
        // Resources.
        // 
        "STATUS_PREPARING",
        //
        // A transaction is associated with the target object and it is in the
        // process of committing. An implementation returns this status if it
        // has decided to commit but has not yet completed the committing process. 
        // This occurs because the implementation is probably waiting for 
        // responses from one or more Resources.
        //  
        "STATUS_COMMITTING",
        //
        // A transaction is associated with the target object and it is in the
        // process of rolling back. An implementation returns this status if
        // it has decided to rollback but has not yet completed the process.
        // The implementation is probably waiting for responses from one or more
        // Resources.
        //  
        "STATUS_ROLLING_BACK"
    };
    
    /**
     * Convert the status value to a String
     * 
     * @param status
     * 
     * @return a readable status value
     */
    public static String getStatus(
        int status
    ){
        return  status >= 0 && status < STATI.length ? STATI[status] : Integer.toString(status); 
    }
    
    /**
     * Acquire the user transaction
     * 
     * @return the user transaction
     * 
     * @throws ResourceException
     */
    public static UserTransaction getUserTransaction(
    ) throws ResourceException {
        try {
            return ComponentEnvironment.lookup(UserTransaction.class);
        } catch (BasicException exception) {
            throw new ResourceException(
                "User transaction acquisition failure",
                exception
            );
        }
    }
    
}
