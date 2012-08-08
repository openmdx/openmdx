/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TimeBasedIdGenerator.java,v 1.2 2007/10/10 16:06:06 hburger Exp $
 * Description: Time Based Id Provider using Random based Node
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:06 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
package org.openmdx.kernel.id.spi;

import java.security.SecureRandom;

/**
 * Time Based Id Provider Using Random Based Node
 */
public abstract class TimeBasedIdGenerator
    extends TimeBasedIdBuilder 
{

    /**
     * The 14 bit clock sequence value is used to set the clock sequence field 
     * of this UUID. The clock sequence field is used to guarantee temporal 
     * uniqueness in a time-based UUID.
     * <p>
     * The clock sequence has to be provided by a subclass.
     * 
     * @return this UUID prvider's clock sequence.
     */
    protected int getClockSequence(
    ){
        return TimeBasedIdGenerator.clockSequence;
    }

    /**
     * The timestamp is measured in 100-nanosecond units since midnight, 
     * October 15, 1582 UTC.
     * <p>
     * This method may be overridden by a subclass.
     * <p>
     * The 60 bit timestamp value is used to set the the time_low, time_mid, 
     * and time_hi fields of the UUID. 
     * 
     * @return the timestamp for the next UUID
     */
    protected long getTimestamp(){
        long timestamp = this.timestamp++;
        if(timestamp >= frameEnd){
            this.timestamp = getTimeFrame();
            this.frameEnd = this.timestamp + FRAME_SIZE;
            timestamp = this.timestamp++;
        }
        return timestamp;
    }    
    
    /**
     * Timestamp in 100-nanosecond units since 1582-10-15T00:00:00Z
     */
    private long timestamp = -1L;
    
    /**
     * Last timestamp reserved for this UUID generator instance
     */
    private long frameEnd = -1L;

    /* (non-Javadoc)
     * @see org.openmdx.kernel.id.spi.TimeBasedIdBuilder#getNode()
     */
    protected long getNode() {
        return getRandomBasedNode();
    }
    
    
    //------------------------------------------------------------------------
    // Time Frame Provider
    //------------------------------------------------------------------------
    
    /**
     * The random number generator used by this class and its sublcasses
     * 
     * @return a random number generator
     */
    protected static SecureRandom getRandom(
    ){
        return TimeBasedIdGenerator.random;
    }

    /**
     * Reserves a time frame of a millisecond for a specific
     * UUID generator instance.
     * 
     * @return a time frame of a millisecond to be reserved for a specific
     * UUID generator instance
     */
    protected static synchronized long getTimeFrame(
    ){
        return OFFSET + (
            getSystemTimeFrame() * FRAME_SIZE
        );
    }
    
    /**
     * Reserves a time frame of a millisecond for a specific
     * UUID generator instance.
     * 
     * @return a time frame of a millisecond to be reserved for a specific
     * UUID generator instance.
     */
    protected static long getSystemTimeFrame(
    ){
        long nextMillisecond = System.currentTimeMillis();
        if(nextMillisecond == lastMillisecond){
            if(savedMillisecond < lastMillisecond) return savedMillisecond++;
            do try {
                Thread.sleep(1L);
                nextMillisecond = System.currentTimeMillis();
            } catch (InterruptedException ie) {
                // ignore
            } while(nextMillisecond == lastMillisecond);
        }
        if (nextMillisecond < TimeBasedIdGenerator.lastMillisecond) {
            //
            // Clock has been set back in the meanwhile
            // 
            clockSequence = (clockSequence + 1) & 0x3FFF;
            savedMillisecond = nextMillisecond;
        } else {
            //
            // Standard completion
            // 
            savedMillisecond = TimeBasedIdGenerator.lastMillisecond + 1L;
        }
        TimeBasedIdGenerator.lastMillisecond = nextMillisecond;
        return nextMillisecond;
    }

    /**
     * Since System.currentTimeMillis() returns time from january 1st 1970,
     * and UUIDs need time from the beginning of gregorian calendar
     * (15-oct-1582), need to apply the offset:
     */
    private final static long OFFSET = 0x01b21dd213814000L;

    /**
     * Also, instead of getting time in units of 100nsecs, we get something
     * with max resolution of 1 msec... and need the multiplier as well
     */
    private final static long FRAME_SIZE = 10000L;

    /**
     * Remember the last used system timestamp to avoid returning the
     * same time frame twice.
     */
    private static long lastMillisecond = System.currentTimeMillis() - 1L;

    /**
     * Remember the second-last used system timestamp to allow reuse of
     * milliseconds anavailable due to low system clock resolution.
     */
    private static long savedMillisecond = lastMillisecond;
   
    /**
     * The random number generator used by this class to create clock sequences.
     */
    private static final SecureRandom random = new SecureRandom();

    /**
     * The clock sequence
     */
    private static int clockSequence = getRandom().nextInt(0x4000);

    /**
     * Return a random based node id
     * 
     * @return a random based node id
     */
    protected static long getRandomBasedNode(
    ){
        return TimeBasedIdGenerator.node;
    }

    /**
     * The random based node value
     */
    private static final long node = 
        (random.nextLong() & 0x0000FEFF0FFFFFFFL) | // Random Address
        0x00000100E0000000L; // with the MAC and IP multicast bits set
        
}
