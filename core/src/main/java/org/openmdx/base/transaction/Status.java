/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.openmdx.base.transaction;

/**
 *  The Status interface defines the constants for transaction status codes.
 */
public enum Status {
    
    /**
     *  Status code indicating an active transaction.
     */
    STATUS_ACTIVE, // ordinal value 0

    /**
     *  Status code indicating a transaction that has been marked for
     *  rollback only.
     */
    STATUS_MARKED_ROLLBACK, // ordinal value 1

    /**
     *  Status code indicating a transaction that has completed the first
     *  phase of the two-phase commit protocol, but not yet begun the
     *  second phase.
     *  Probably the transaction is waiting for instruction from a superior
     *  coordinator on how to proceed.
     */
    STATUS_PREPARED, // ordinal value 2

    /**
     *  Status code indicating a transaction that has been committed.
     *  Probably heuristics still exists, or the transaction would no
     *  longer exist.
     */
    STATUS_COMMITTED, // ordinal value 3

    /**
     *  Status code indicating a transaction that has been rolled back.
     *  Probably heuristics still exists, or the transaction would no
     *  longer exist.
     */
    STATUS_ROLLEDBACK, // ordinal value 4

    /**
     *  Status code indicating that the transaction status could not be
     *  determined.
     */
    STATUS_UNKNOWN, // ordinal value 5

    /**
     *  Status code indicating that no transaction exists.
     */
    STATUS_NO_TRANSACTION, // ordinal value 6

    /**
     *  Status code indicating a transaction that has begun the first
     *  phase of the two-phase commit protocol, not not yet completed
     *  this phase.
     */
    STATUS_PREPARING, // ordinal value 7

    /**
     *  Status code indicating a transaction that has begun the second
     *  phase of the two-phase commit protocol, but not yet completed
     *  this phase.
     */
    STATUS_COMMITTING, // ordinal value 8

    /**
     *  Status code indicating a transaction that is in the process of
     *  rolling back.
     */
    STATUS_ROLLING_BACK; // ordinal value 9
 
    /**
     * Convert a javax.transaction.Status value into an org.openmdx.base.transaction.Status value
     * 
     * @param status a javax.transaction.Status value
     * 
     * @return an org.openmdx.base.transaction.Status value
     */
    public static Status valueOf(
        int status
    ){
        return values()[status];
    }
        
}
