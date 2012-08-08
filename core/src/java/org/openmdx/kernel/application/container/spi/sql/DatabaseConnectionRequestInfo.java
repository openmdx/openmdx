/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DatabaseConnectionRequestInfo.java,v 1.4 2005/04/17 05:30:54 hburger Exp $
 * Description: Database Connection Request Info
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/04/17 05:30:54 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.application.container.spi.sql;

import java.io.Serializable;

import javax.resource.spi.ConnectionRequestInfo;


/**
 * Database Connection Request Info
 */
public class DatabaseConnectionRequestInfo
    implements Serializable, ConnectionRequestInfo 
{

    /**
     * Constructor
     * @param transactionIsolation the transaction isolation level; or
     * code>null</code> if it has not been specified.
     */
    public DatabaseConnectionRequestInfo(
        Integer transactionIsolation
    ) {
        this.transactionIsolation = transactionIsolation;
    }

    /**
     * Implements <code>Serializable</code>.
     */
    private static final long serialVersionUID = 3258689927104444210L;

    /**
     * The requested transaction isolation
     */
    private final Integer transactionIsolation;

    /**
     * @return Returns the transactionIsolation.
     */
    public Integer getTransactionIsolation() {
        return this.transactionIsolation;
    }
    
    /**
     * Checks whether this instance is equal to another. 
     */
    public boolean equals(
       Object that
    ){
        return this == that ?
            true :
        that == null || this.getClass() != that.getClass() ?
            false :
        this.transactionIsolation == null ?
            ((DatabaseConnectionRequestInfo) that).transactionIsolation == null :
            this.transactionIsolation.equals(((DatabaseConnectionRequestInfo) that).transactionIsolation);
    } 
     
    /**
     * Returns the hashCode of the ConnectionRequestInfo.
     */
    public int hashCode(){
        return this.getTransactionIsolation() == null ? 
            0 : 
            this.getTransactionIsolation().intValue();
    }
    

    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getClass() + ": transactionIsolation=" + getTransactionIsolation();
    }

}
