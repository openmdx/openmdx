/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DatabaseConnectionRequestInfo.java,v 1.2 2010/06/02 13:46:07 hburger Exp $
 * Description: Database Connection Request Info
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 13:46:07 $
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
package org.openmdx.kernel.lightweight.sql;

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
     * 
     * @param transactionIsolation the transaction isolation level; or
     * code>null</code> if it has not been specified.
     * @param validationStatement the connection validation statement; or
     * code>null</code> if it has not been specified.
     * @param loginTimeOut 
     */
    public DatabaseConnectionRequestInfo(
        Integer transactionIsolation,
        String validationStatement, 
        Long loginTimeout
    ) {
        this.transactionIsolation = transactionIsolation;
        this.validationStatement = validationStatement;
        this.loginTimeout = loginTimeout;
    }

    /**
     * Implements <code>Serializable</code>.
     */
    private static final long serialVersionUID = 5762016497652692866L;

    /**
     * The requested transaction isolation
     */
    private final Integer transactionIsolation;

    /**
     * The connection validation statement
     */
    private final String validationStatement;
    
    /**
     * The login timeout
     */
    private Long loginTimeout;

    /**
     * @return Returns the transactionIsolation.
     */
    Integer getTransactionIsolation() {
        return this.transactionIsolation;
    }
    
    /**
     * Retrieve validationStatement.
     *
     * @return Returns the validationStatement.
     */
    String getValidationStatement() {
        return this.validationStatement;
    }

    /**
     * Retrieve loginTimeout.
     *
     * @return Returns the loginTimeout.
     */
     Long getLoginTimeout() {
         return this.loginTimeout;
     }
    
    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------    

    /**
     * Checks whether this instance is equal to another. 
     */
     @Override
    public boolean equals(
       Object that
    ){
        return this == that ?
            true :
        that == null || this.getClass() != that.getClass() ?
            false :
        (
            this.transactionIsolation == null ?
                ((DatabaseConnectionRequestInfo) that).transactionIsolation == null :
                 this.transactionIsolation.equals(((DatabaseConnectionRequestInfo) that).transactionIsolation)
        ) && (
                this.validationStatement == null ?
                    ((DatabaseConnectionRequestInfo) that).validationStatement == null :
                     this.validationStatement.equals(((DatabaseConnectionRequestInfo) that).validationStatement)
        );
    } 
     
    /**
     * Returns the hashCode of the ConnectionRequestInfo.
     */
     @Override
    public int hashCode(){
        int hashCode = this.getTransactionIsolation() == null ? 0 : this.getTransactionIsolation().intValue();
        hashCode += this.validationStatement == null ? 0 : this.validationStatement.hashCode();
        return hashCode;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
     @Override
    public String toString() {
        return getClass() + ": transactionIsolation=" + getTransactionIsolation();
    }

}
