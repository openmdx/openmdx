/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: X/Open Transaction Identifier
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

import java.util.Arrays;

import javax.transaction.xa.Xid;

import org.openmdx.kernel.text.format.HexadecimalFormatter;

/**
 * X/Open Transaction Identifier
 */
class TransactionId implements Xid {

    /**
     * Constructor
     */
    protected TransactionId(
        long globalTransactionIdMostSignificantBits,
        long globalTransactionIdLeastSignificantBits
    ){
        this.mostSigBits = globalTransactionIdMostSignificantBits;
        this.leastSigBits = globalTransactionIdLeastSignificantBits;
    }

    /**
     * Constructor 
     */
    protected TransactionId(){
        // Deserialization
    }

    /**
     * @serial The most significant 64 bits of this TransactionIdentifier.
     */
    protected long mostSigBits;

    /**
     * @serial The least significant 64 bits of this TransactionIdentifier.
     */
    protected long leastSigBits;


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Generic Xid Comparison
     */
    static boolean equal(
        Xid left,
        Xid right
    ){
        return
            left.getFormatId() == right.getFormatId() &&
            Arrays.equals(left.getGlobalTransactionId(), right.getGlobalTransactionId()) &&
            Arrays.equals(left.getBranchQualifier(), right.getBranchQualifier());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Xid)) return false;
        if(!(obj.getClass() == TransactionId.class)) return equal(this, (Xid) obj);
        TransactionId that = (TransactionId) obj;
        return
            this.mostSigBits == that.mostSigBits &&
            this.leastSigBits == that.leastSigBits;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        long value = this.mostSigBits ^ this.leastSigBits;
        value ^= value >> 32;
        return (int)value;
    }

    /**
     * Simplify toString subclassing
     * 
     * @return a string builder containing the Xid's String representation
     */
    protected StringBuilder toStringBuilder(
    ){
        return new StringBuilder(
           "Xid:"
        ).append(
            new HexadecimalFormatter(getFormatId(),4)
        ).append(
            '-'
        ).append(
            new HexadecimalFormatter(this.mostSigBits)
        ).append(
            new HexadecimalFormatter(this.leastSigBits)
        );
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return toStringBuilder().toString();
    }


    //------------------------------------------------------------------------
    // Implements Xid
    //------------------------------------------------------------------------

    /**
     * The Lightweight Containers Transaction Identifier Id.
     */
    private static final int FORMAT_ID = 0x4c43; // i.e. LC

    /**
     * The empty branch identifier
     */
    private static final byte[] BRANCH_ID = new byte[]{};

    /* (non-Javadoc)
     * @see javax.transaction.xa.Xid#getFormatId()
     */
    public final int getFormatId() {
        return FORMAT_ID;
    }

    /* (non-Javadoc)
     * @see javax.transaction.xa.Xid#getGlobalTransactionId()
     */
    public final byte[] getGlobalTransactionId() {
        return new byte[]{
            (byte)(mostSigBits >> 56),
            (byte)(mostSigBits >> 48),
            (byte)(mostSigBits >> 40),
            (byte)(mostSigBits >> 32),
            (byte)(mostSigBits >> 24),
            (byte)(mostSigBits >> 16),
            (byte)(mostSigBits >> 8),
            (byte)(mostSigBits ),
            (byte)(leastSigBits >> 56),
            (byte)(leastSigBits >> 48),
            (byte)(leastSigBits >> 40),
            (byte)(leastSigBits >> 32),
            (byte)(leastSigBits >> 24),
            (byte)(leastSigBits >> 16),
            (byte)(leastSigBits >> 8),
            (byte)(leastSigBits )
        };
    }

    /* (non-Javadoc)
     * @see javax.transaction.xa.Xid#getBranchQualifier()
     */
    public byte[] getBranchQualifier() {
        return BRANCH_ID;
    }

}
