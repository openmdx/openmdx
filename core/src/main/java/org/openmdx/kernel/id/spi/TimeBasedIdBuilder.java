/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Time Based Id Provider
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.kernel.id.spi;

/**
 * Time Based Id Provider
 */
public abstract class TimeBasedIdBuilder {

    /**
     * The invocation order is<ol>
     * <li>nextMostSignificantBits()
     * <li>nextLeastSignificantBits()
     * </ol>
     *  
     * @return The most significant bits of the next id
     * 
     * @see #nextLeastSignificantBits()
     */
    protected long nextMostSignificantBits() {
        //
        // Timestamp retrieval may affect the other values
        //
        long t = getTimestamp();
        //
        // Set Version 1
        // 
        long mostSignificantBits = 1L << 12; 
        //
        // Set Timestamp
        // 
        mostSignificantBits |= (t & 0x00000000FFFFFFFFL) << 32; // time_low
        mostSignificantBits |= (t & 0x0000FFFF00000000L) >> 16; // time_mid
        mostSignificantBits |= (t & 0x0FFF000000000000L) >> 48; // time_hi
        //
        // Return the next id's most significant bits
        // 
        return mostSignificantBits;
    }

    /**
     * The invocation order is<ol>
     * <li>nextMostSignificantBits()
     * <li>nextLeastSignificantBits()
     * </ol>
     *  
     * @return The most significant bits of the next id
     * 
     * @see #nextMostSignificantBits()
     */
    protected long nextLeastSignificantBits() {
        //
        // Set Variant 2
        // 
        long leastSignificantBits = 2L << 62;
        //
        // Set Node
        // 
        leastSignificantBits |= getNode();
        //
        // Set ClockSequence
        // 
        leastSignificantBits |= ((long)getClockSequence()) << 48;
        //
        // Return the next id's leastt significant bits
        // 
        return leastSignificantBits;
    }

    /**
     * The 48 bit node value is used to set the node field of this UUID. This 
     * field is intended to hold the IEEE 802 address of the machine that 
     * generated this UUID to guarantee spatial uniqueness.
     * <p>
     * The node id has to be provided by a subclass.
     * 
     * @return this UUID prvider's host id.
     */
    protected abstract long getNode();
    
    /**
     * The 14 bit clock sequence value is used to set the clock sequence field 
     * of this UUID. The clock sequence field is used to guarantee temporal 
     * uniqueness in a time-based UUID.
     * <p>
     * The clock sequence has to be provided by a subclass.
     * 
     * @return this UUID prvider's clock sequence.
     */
    protected abstract int getClockSequence();

    /**
     * The timestamp is measured in 100-nanosecond units since 1582-10-15T00:00:00Z.
     * <p>
     * This method may be overridden by a subclass.
     * <p>
     * The 60 bit timestamp value is used to set the time_low, time_mid,
     * and time_hi fields of the UUID. 
     * 
     * @return the timestamp for the next UUID
     */
    protected abstract long getTimestamp();
       
}
