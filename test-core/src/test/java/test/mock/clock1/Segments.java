/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Segments 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

package test.mock.clock1;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefStruct;

import org.junit.jupiter.api.Assertions;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.rest.cci.VoidRecord;
import org.w3c.format.DateTimeFormat;

import test.openmdx.clock1.jmi1.Clock1Package;
import test.openmdx.clock1.jmi1.Segment;
import test.openmdx.clock1.jmi1.Time;

/**
 * Segments
 */
public class Segments {

    private static final long ONE_AND_HALF_AN_HOUR = 90*60*1000L; 
    
    /**
     * Retrieve the named segment
     * 
     * @param entityManager the entity manager
     * @param segmentName the name of the segment
     * 
     * @return the requested Segment
     */
   private static Segment getSegment(
        PersistenceManager entityManager,
        String segmentName
    ) {
        Authority authority = entityManager.getObjectById(Authority.class, Clock1Package.AUTHORITY_XRI);
        Provider provider = authority.getProvider("Java");
        return (Segment) provider.getSegment(segmentName);
    }

    static void validateMockedProvider(
        Segment segment
    ) {
        Provider provider = segment.getProvider();
        Assertions.assertEquals("xri://@openmdx*test.openmdx.clock1/provider/Mocked",  provider.refMofId(), getMockedSegmentName() + " segment's provider");
        Assertions.assertSame(JDOHelper.getPersistenceManager(segment),  JDOHelper.getPersistenceManager(provider), "Persistence Managers");
    }

    static void validateChangedTimePoint(Segment segment) {
        final Transaction transaction = JDOHelper.getPersistenceManager(segment).currentTransaction();
        transaction.begin();
        final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif expected = tryToChangeDateAndTime(segment);
        Assertions.assertEquals(expected,  segment.currentDateAndTime().getUtc(), "Time set back");
        transaction.commit();	
    }

    static void validateChangedTimePointReflectively(Segment segment) throws RefException {
        final Transaction transaction = JDOHelper.getPersistenceManager(segment).currentTransaction();
        transaction.begin();
        final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif expected = tryToChangeDateAndTimeReflectively(segment);
        Assertions.assertEquals(expected,  segment.currentDateAndTime().getUtc(), "Time set back");
        transaction.commit();
    }
    
    private static #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif tryToChangeDateAndTime(
        Segment segment
    ) {
        final Clock1Package clock1Package = (Clock1Package) segment.refImmediatePackage();
        final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif mockTimePoint = new #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif(System.currentTimeMillis() - ONE_AND_HALF_AN_HOUR);
        final Time value = clock1Package.createTime(mockTimePoint);
        segment.setDateAndTime(value);
        return mockTimePoint;
    }

    private static #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif tryToChangeDateAndTimeReflectively(
        Segment segment
    ) throws RefException {
        final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif mockTimePoint = new #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif(System.currentTimeMillis() - ONE_AND_HALF_AN_HOUR);
        final RefStruct param = segment.refImmediatePackage().refCreateStruct(
            "test:openmdx:clock1:Time", 
            Collections.singletonList(mockTimePoint)
        );
        segment.refInvokeOperation(
            "setDateAndTime", 
            Arrays.asList(param)
        );
        return mockTimePoint;
    }
    
    static void validateUnchangedTimePoint(Segment segment) {
        final Transaction transaction = JDOHelper.getPersistenceManager(segment).currentTransaction();
        transaction.begin();
        tryToChangeDateAndTime(segment);
        validateNormalTimePoint(segment);
        transaction.commit();
    }

    static void validateMockedTimePoint(
        Segment segment
    ) throws ParseException {
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif utc = getTimePoint(segment);
        validateMockedTimePoint(utc, "");
    }

    static void validateMockedTimePointReflectively(
        Segment segment
    ) throws ParseException, RefException {
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif utc = getTimePointReflectively(segment);
        validateMockedTimePoint(utc, "-reflectively");
    }
    
    private static void validateMockedTimePoint(
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif utc,
        String mode
    ) throws ParseException {
        Assertions.assertEquals(DateTimeFormat.BASIC_UTC_FORMAT.parse("20000401T120000.000Z"),  utc, "High Noon");
        System.out.println(getMockedSegmentName() + mode + ": " + DateTimeFormat.BASIC_UTC_FORMAT.format(utc)); // xri://@openmdx*test.openmdx.clock1/provider/Mocked
    }

    static void validateMockedDescription(
        Segment segment
    ) {
        try {
            segment.getDescription();
            Assertions.fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException exception) {
            // Excpected behaviour
        }
    }

    static Segment getMockedSegment(
        final PersistenceManager persistenceManager
    ) {
        return getSegment(persistenceManager, getMockedSegmentName());
    }

    static String getMockedSegmentName(
    ) {
        return "Mocked";
    }

    static void validateNormalProvider(
        final Segment segment
    ) {
        Provider provider = segment.getProvider();
        Assertions.assertEquals("xri://@openmdx*test.openmdx.clock1/provider/Java",  provider.refMofId(), getNormalSegmentName() + " segment's provider");
    }

    static void validateNormalTimePoint(
        final Segment segment
    ) {
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif utc = getTimePoint(segment);
        validateNormalTimePoint(utc, "");
    }

    private static #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif getTimePoint(
        final Segment segment
    ) {
        return segment.currentDateAndTime().getUtc();
    }

    private static #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif getTimePointReflectively(
        final Segment segment
    ) throws RefException {
        final RefStruct param = segment.refImmediatePackage().refCreateStruct(
            VoidRecord.NAME, 
            Collections.emptyList()
        );
        final RefStruct result = (RefStruct)segment.refInvokeOperation(
            "currentDateAndTime", 
            Arrays.asList(param)
        );
        final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif utc = (#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif) result.refGetValue("utc");
        return utc;
    }
    
    static void validateNormalTimePointReflectively(
        final Segment segment
    ) throws RefException {
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif utc = getTimePointReflectively(segment);
        validateNormalTimePoint(utc, "-reflectively");
    }
    
    private static void validateNormalTimePoint(
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif utc,
        String mode
    ) {
        long now = System.currentTimeMillis();
        Assertions.assertTrue(Math.abs(now - utc.getTime()) < 1000, "Time window < 1 s");
        System.out.println(getNormalSegmentName() + mode + ": " + DateTimeFormat.BASIC_UTC_FORMAT.format(utc));
    }

    static void validateNormalDescription(
        final Segment segment
    ) {
        Assertions.assertEquals("clock1 segment",  segment.getDescription(), "description");
    }

    static Segment getNormalSegment(
        final PersistenceManager persistenceManager
    ) {
        return getSegment(persistenceManager, getNormalSegmentName());
    }

    static String getNormalSegmentName(
    ) {
        return "Normal";
    }
}
