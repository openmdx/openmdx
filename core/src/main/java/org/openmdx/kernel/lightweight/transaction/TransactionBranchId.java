/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description:  X/Open Transaction Branch Identifier
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.lightweight.transaction;

import javax.transaction.xa.Xid;

import org.openmdx.kernel.text.format.HexadecimalFormatter;

/**
 *  X/Open Transaction Branch Identifier
 * 
 * @deprecated in favour of Atomikos' transaction manager
 */
@Deprecated
class TransactionBranchId extends TransactionId {

    /**
     * Constructor
     * 
     * @param globalTransactionIdMostSignificantBits
     * @param globalTransactionIdLeastSignificantBits
     */
    TransactionBranchId(
        TransactionId transactionId,
        int branchQualifier
    ){
        super(
           transactionId.mostSigBits,
           transactionId.leastSigBits
        );
        this.branchQualifier = branchQualifier;
    }

    /**
     * Constructor 
     */
    protected TransactionBranchId(
    ){
        // Serialization Constructor
    }

    /**
     * @serial
     */
    protected int branchQualifier;


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode() ^ this.branchQualifier;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Xid)) return false;
        if(!(obj.getClass() == TransactionBranchId.class)) return equal(this, (Xid) obj);
        TransactionBranchId that = (TransactionBranchId) obj;
        return
            this.mostSigBits == that.mostSigBits &&
            this.leastSigBits == that.leastSigBits &&
            this.branchQualifier == that.branchQualifier;
    }

    /**
     * Simplify toString subclassing
     * 
     * @return a string builder containing the Xid's String representation
     */
    @Override
    protected StringBuilder toStringBuilder(
    ){
        return super.toStringBuilder(
        ).append(
            '-'
        ).append(
            new HexadecimalFormatter(this.branchQualifier)
        );
    }


    //------------------------------------------------------------------------
    // Implements Xid
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.transaction.xa.Xid#getBranchQualifier()
     */
    @Override
    public byte[] getBranchQualifier() {
        return new byte[]{
            (byte)(branchQualifier >> 24),
            (byte)(branchQualifier >> 16),
            (byte)(branchQualifier >> 8),
            (byte)(branchQualifier )
        };
    }

}
