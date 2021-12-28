/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Segments 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2020, OMEX AG, Switzerland
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

package test.mock.clock1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefStruct;

import org.junit.Assert;
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
        assertEquals(getMockedSegmentName() + " segment's provider", "xri://@openmdx*test.openmdx.clock1/provider/Mocked", provider.refMofId());
        assertSame("Persistence Managers", JDOHelper.getPersistenceManager(segment), JDOHelper.getPersistenceManager(provider));
    }

    static void validateChangedTimePoint(Segment segment) {
        final Transaction transaction = JDOHelper.getPersistenceManager(segment).currentTransaction();
        transaction.begin();
        final Date expected = tryToChangeDateAndTime(segment);
        Assert.assertEquals("Time set back", expected, segment.currentDateAndTime().getUtc());
        transaction.commit();	
    }

    static void validateChangedTimePointReflectively(Segment segment) throws RefException {
        final Transaction transaction = JDOHelper.getPersistenceManager(segment).currentTransaction();
        transaction.begin();
        final Date expected = tryToChangeDateAndTimeReflectively(segment);
        Assert.assertEquals("Time set back", expected, segment.currentDateAndTime().getUtc());
        transaction.commit();
    }
    
    private static Date tryToChangeDateAndTime(
        Segment segment
    ) {
        final Clock1Package clock1Package = (Clock1Package) segment.refImmediatePackage();
        final Date mockTimePoint = new Date(System.currentTimeMillis() - ONE_AND_HALF_AN_HOUR);
        final Time value = clock1Package.createTime(mockTimePoint);
        segment.setDateAndTime(value);
        return mockTimePoint;
    }

    private static Date tryToChangeDateAndTimeReflectively(
        Segment segment
    ) throws RefException {
        final Date mockTimePoint = new Date(System.currentTimeMillis() - ONE_AND_HALF_AN_HOUR);
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
        Date utc = getTimePoint(segment);
        validateMockedTimePoint(utc, "");
    }

    static void validateMockedTimePointReflectively(
        Segment segment
    ) throws ParseException, RefException {
        Date utc = getTimePointReflectively(segment);
        validateMockedTimePoint(utc, "-reflectively");
    }
    
    private static void validateMockedTimePoint(
        Date utc,
        String mode
    ) throws ParseException {
        assertEquals("High Noon", DateTimeFormat.BASIC_UTC_FORMAT.parse("20000401T120000.000Z"), utc);
        System.out.println(getMockedSegmentName() + mode + ": " + DateTimeFormat.BASIC_UTC_FORMAT.format(utc)); // xri://@openmdx*test.openmdx.clock1/provider/Mocked
    }

    static void validateMockedDescription(
        Segment segment
    ) {
        try {
            segment.getDescription();
            Assert.fail("IndexOutOfBoundsException expected");
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
        assertEquals(getNormalSegmentName() + " segment's provider", "xri://@openmdx*test.openmdx.clock1/provider/Java", provider.refMofId());
    }

    static void validateNormalTimePoint(
        final Segment segment
    ) {
        Date utc = getTimePoint(segment);
        validateNormalTimePoint(utc, "");
    }

    private static Date getTimePoint(
        final Segment segment
    ) {
        return segment.currentDateAndTime().getUtc();
    }

    private static Date getTimePointReflectively(
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
        final Date utc = (Date) result.refGetValue("utc");
        return utc;
    }
    
    static void validateNormalTimePointReflectively(
        final Segment segment
    ) throws RefException {
        Date utc = getTimePointReflectively(segment);
        validateNormalTimePoint(utc, "-reflectively");
    }
    
    private static void validateNormalTimePoint(
        Date utc, 
        String mode
    ) {
        long now = System.currentTimeMillis();
        Assert.assertTrue("Time window < 1 s", Math.abs(now - utc.getTime()) < 1000);
        System.out.println(getNormalSegmentName() + mode + ": " + DateTimeFormat.BASIC_UTC_FORMAT.format(utc));
    }

    static void validateNormalDescription(
        final Segment segment
    ) {
        assertEquals(
            "description", 
            "clock1 segment",
            segment.getDescription()
        );
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
