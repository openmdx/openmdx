/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Time Based Id Provider using Random based Node
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.id.spi;

import java.security.SecureRandom;
import java.util.logging.Level;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.format.DateTimeFormat;

/**
 * Time Based Id Provider 
 */
public abstract class TimeBasedIdGenerator extends TimeBasedIdBuilder {

    /**
     * The time frame reserved for this generator instance
     */
    private TimeFrame timeFrame;
    
    /**
     * Since System.currentTimeMillis() returns time from January 1st 1970,
     * and UUIDs need time from the beginning of gregorian calendar
     * (15-oct-1582), need to apply the offset:
     */
    private static final long OFFSET = 0x01b21dd213814000L;
    
    /**
     * Also, instead of getting time in units of 100nsecs, we get something
     * with max resolution of 1 msec... and need the multiplier as well
     */
    private static final long FRAME_SIZE = 10000L;
    
    /**
     * Remember the last used system timestamp to avoid returning the
     * same time frame twice.
     */
    private static volatile long lastReservation = -1L;
    
    /**
     * The random number generator used by this class to create clock sequences.
     */
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * The clock sequence
     */
    private static volatile int clockSequence = createClockSequence();
    
    
    /**
     * The 14 bit clock sequence value is used to set the clock sequence field 
     * of this UUID. The clock sequence field is used to guarantee temporal 
     * uniqueness in a time-based UUID.
     * <p>
     * The clock sequence has to be provided by a subclass.
     * 
     * @return this UUID prvider's clock sequence.
     */
    @Override
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
    @Override
    protected long getTimestamp(){
        return getTimeFrame().nextTimeStamp();
    }

    /**
     * Get a non-exhausted time frame
     */
    private TimeFrame getTimeFrame(
    ){
        if(this.timeFrame == null || this.timeFrame.isExhausted()) try {
            this.timeFrame = newTimeFrame();
            SysLog.detail("New time frame reserved", this.timeFrame);
        } catch (InterruptedException exception) {
            throw new RuntimeException(
                "Time frame acquisition failure",
                exception
            );
        }
        return this.timeFrame;
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
     * Reserves a time frame of a millisecond for a specific UUID generator 
     * instance.
     * 
     * @return a time frame reserved for a the calling UUID generator instance
     *
     * @throws InterruptedException 
     */
    private static synchronized TimeFrame newTimeFrame(
    ) throws InterruptedException{
        final long lastReservation = TimeBasedIdGenerator.lastReservation;
        final long nextReservation = nextReservation(lastReservation);
        TimeFrame timeFrame = toTimeFrame(lastReservation, nextReservation);
        TimeBasedIdGenerator.lastReservation = nextReservation;
        return timeFrame;
    }

    /**
     * Validates the reservation and create the corresponding time frame
     * 
     * @param lastReservation the last reservation (which maybe belongs to another generator)
     * @param nextReservation the next reservation for the calling generator
     * 
     * @return a new time frame for the calling generator
     */
    private static TimeFrame toTimeFrame(
        final long lastReservation,
        final long nextReservation
    ) {
        final int signum = Long.signum(nextReservation - lastReservation);
        switch (signum){
            case -1:
                //
                // Clock has been set back in the meanwhile
                // 
                changeClockSequence(lastReservation, nextReservation);
                break;
            case 0:
                throw new RuntimeException(
                    BasicException.newStandAloneExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "This time frame is already reserved",
                        new BasicException.Parameter("time-frame", new TimeFrame(nextReservation))
                    )
                );
            case 1:
                //
                // Normal 
                // 
                break;
            default:
                //
                // Should never happen!
                //
                throw new RuntimeException(
                    BasicException.newStandAloneExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "The signum is expectzed to be in the range [-1..1]",
                        new BasicException.Parameter("signum", signum)
                    )
                );
        }
        return new TimeFrame(nextReservation);
    }

    /**
     * Change the clock sequence when the clock has been set back
     * 
     * @param lastReservation the last reservation (which maybe belongs to another generator)
     * @param nextReservation the next reservation for the calling generator
     */
    private static void changeClockSequence(
        final long lastReservation,
        final long nextReservation
     ) {
        int clockSequence = (TimeBasedIdGenerator.clockSequence + 1) & 0x3FFF;
        SysLog.log(
                Level.WARNING,
                "Sys|Clock has been set back from {0} to {1}|Clock sequence will be changed from {2} to {3}",
                DateTimeFormat.EXTENDED_UTC_FORMAT.format(
                        #if CLASSIC_CHRONO_TYPES new java.util.Date #else java.time.Instant.ofEpochMilli#endif(lastReservation)
                ),
                DateTimeFormat.EXTENDED_UTC_FORMAT.format(
                        #if CLASSIC_CHRONO_TYPES new java.util.Date #else java.time.Instant.ofEpochMilli#endif(nextReservation)
                ),
                (long) TimeBasedIdGenerator.clockSequence,
                (long) clockSequence
        );
        TimeBasedIdGenerator.clockSequence = clockSequence;
    }

    /**
     * Find the next reservation
     * 
     * @param lastReservation the last reservation
     * 
     * @return the next reservation
     * 
     * @throws InterruptedException
     */
    private static long nextReservation(
        final long lastReservation
    ) throws InterruptedException {
        long currentMillisecond = System.currentTimeMillis();
        int i = 0;
        while(currentMillisecond == lastReservation) {
            i++;
            Thread.sleep(1L); // wait for 1 ms
            currentMillisecond = System.currentTimeMillis();
        }
        SysLog.log(Level.FINE, "Sys|Delay for acquiring a new time frame|{0} ms", Integer.valueOf(i));
        return currentMillisecond;
    }

    /**
     * Create a random based node
     */
    protected static long createRandomBasedNode(){
        long randomBasedNode = 
            (getRandom().nextLong() & 0x0000FEFF0FFFFFFFL) | // Random Address
            0x00000100E0000000L; // with the MAC and IP multicast bits set
        SysLog.info("The time based id generator has a random based node", new HexFormatter(randomBasedNode));
        return randomBasedNode;
    }
    
    /**
     * @return a random clock sequence
     */
    private static int createClockSequence() {
        int clockSequence = getRandom().nextInt(0x4000);
        SysLog.info("Clock sequence created", new HexFormatter(clockSequence));
        return clockSequence;
    }

    
    //------------------------------------------------------------------------
    // Class TimeFrame
    //------------------------------------------------------------------------
    
    /**
     * Represents a time frame allocated to a single generator
     */
    private static class TimeFrame {
    
        /**
         * Constructor 
         *
         * @param millisecond milliseconds passed since January 1st 1970
         */
        TimeFrame(
            long millisecond
        ) {
            this.millisecond = millisecond;
            this.currentTimeStamp = OFFSET + millisecond * FRAME_SIZE;
            this.limit = this.currentTimeStamp + FRAME_SIZE;
        }

        private final long millisecond;
        
        /**
         * Timestamp in 100-nanosecond units since 1582-10-15T00:00:00Z
         */
        private long currentTimeStamp;
        
        private final long limit;
        
        boolean isExhausted(){
            return this.currentTimeStamp == limit;
        }

        /**
         * Retrieve a 100-nanosecond unit since 1582-10-15T00:00:00Z
         * 
         * @return the next time stamp
         */
        long nextTimeStamp(){
            return this.currentTimeStamp++;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return DateTimeFormat.EXTENDED_UTC_FORMAT.format(
                    #if CLASSIC_CHRONO_TYPES new java.util.Date #else java.time.Instant.ofEpochMilli#endif(millisecond)) + "/P0.001S";
        }
        
    }
    
    
    //------------------------------------------------------------------------
    // Class HexFormatter
    //------------------------------------------------------------------------
    
    /**
     * Allows loggers to log nodes and clock sequences appropriately
     */
    private static class HexFormatter {

        HexFormatter(long node) {
            this.node = node;
        }

        private final long node;

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return Long.toHexString(node);
        }
        
    }

}
