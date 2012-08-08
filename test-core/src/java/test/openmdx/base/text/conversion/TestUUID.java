/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestUUID.java,v 1.2 2009/05/04 13:50:53 hburger Exp $
 * Description: TestUUID 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/04 13:50:53 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

package test.openmdx.base.text.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.junit.Test;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.base.text.format.UUIDFormatter;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.openmdx.kernel.id.spi.TimeBasedIdBuilder;

/**
 * TestUUID
 */
public class TestUUID {

    final static private String[] UUIDGEN_RANDOM_UUID = new String[]{
        "3a01c263-3ac7-48c1-8e86-cd653bc2ad10",
        "70b3e197-ab79-49ae-9c2e-fa41960dd6ce",
        "a4f97cae-8351-4154-a198-4af58546283f",
        "2b23fafa-e8ad-486c-b241-22b80d1b632b",
        "f255d604-4a63-49c6-a989-3d81fcd46cc6",
        "b0c5ef15-1970-4ff3-876d-cf4f0477593f",
        "58f1d2cd-d671-4284-94e8-0be5e2390058",
        "0ab2a671-154b-4b80-bbbd-d9769e715426"
    };

    final static private String[] UUIDGEN_SEQUENTIAL_UUID = new String[]{
        "8eac6120-8974-11d8-9472-0010c61b123a",
        "8eac6121-8974-11d8-9472-0010c61b123a",
        "8eac6122-8974-11d8-9472-0010c61b123a",
        "8eac6123-8974-11d8-9472-0010c61b123a",
        "36ad9c80-8975-11d8-9472-0010c61b123a",
        "36ad9c81-8975-11d8-9472-0010c61b123a",
        "36ad9c82-8975-11d8-9472-0010c61b123a",
        "36ad9c83-8975-11d8-9472-0010c61b123a"
    };

    final static private InternalUUIDProvider provider = new InternalUUIDProvider();

    /**
     * Defines how many entries per test are written to System.out -
     * except for the allocation performance test of course.
     */
    final static int OUT_LIMIT = 1;

    /**
     * Test UUID generation performance
     */
    @Test
    public void testPerformance(
    ){
        UUIDGenerator generator = UUIDs.getGenerator();
        UUID uuid = generator.next();
        if(OUT_LIMIT > 0) log("testPerformance", uuid);
        long t = System.currentTimeMillis();
        for(int i = 0; i < 1000000; i++) uuid = generator.next();
        System.out.println((System.currentTimeMillis() - t) + " nsec per UUID");
    }

    /**
     * Test Java Random UID
     */
    @Test
    public void testUUIDs(
    ) throws Exception {
        UUIDGenerator generator = UUIDs.getGenerator();
        for(int i = 0; i < 8; i++){
            UUID uuid = generator.next();
            assertEquals("Leach-Salz variant", uuid.variant(), 2);
            assertEquals("Base36", uuid, UUIDConversion.fromString(UUIDConversion.toUID(uuid)));
            assertEquals("urn", uuid, UUIDConversion.fromString(UUIDConversion.toURN(uuid)));
            if(OUT_LIMIT > i) log("testUUIDs", uuid);
        }
        UUID[] uuids = new UUID[500000];
        for(int i=0; i < uuids.length; i++){
            uuids[i] = generator.next();
        }
        assertEquals(
            "Duplicates", 
            uuids.length, 
            new HashSet<UUID>(Arrays.asList(uuids)).size()
        );
    }

    /**
     * Test Java Random UID
     */
    @Test
    public void testNIL(
    ) throws Exception {
        UUID uuid = UUIDs.NIL;
        assertEquals("NIL value", new UUID(0L, 0L), uuid);
        assertEquals("NIL String", "00000000-0000-0000-0000-000000000000", uuid.toString());
        assertEquals("toBase36", uuid, UUIDConversion.fromString(UUIDConversion.toUID(uuid)));
        assertEquals("toURN", uuid, UUIDConversion.fromString(UUIDConversion.toURN(uuid)));
        if(OUT_LIMIT > 0) log("testNIL", uuid);
    }

    /**
     * Test Java Random UID
     */
    @Test
    public void testJavaRandomUUID(
    ) throws Exception {
        for(int i = 0; i < 8; i++){
            UUID uuid = UUID.randomUUID();
            assertEquals("Leach-Salz variant", uuid.variant(), 2);
            assertEquals("Randomly generated", uuid.version(), 4);
            assertEquals("toBase36", uuid, UUIDConversion.fromString(UUIDConversion.toUID(uuid)));
            assertEquals("toURN", uuid, UUIDConversion.fromString(UUIDConversion.toURN(uuid)));
            if(OUT_LIMIT > i) log("testJavaRandomUUID", uuid);
        }
    }

    /**
     * Test uuidgen random UUID
     */
    @Test
    public void testUuidgenRandomUUID(
    ) throws Exception {
        for(int i = 0; i < UUIDGEN_RANDOM_UUID.length; i++){
            UUID uuid = UUID.fromString(UUIDGEN_RANDOM_UUID[i]);
            assertEquals("Leach-Salz variant", uuid.variant(), 2);
            assertEquals("Randomly generated", uuid.version(),4);
            assertEquals("UUID String", UUIDGEN_RANDOM_UUID[i], uuid.toString());
            assertEquals("toBase36", uuid, UUIDConversion.fromString(UUIDConversion.toUID(uuid)));
            assertEquals("toURN", uuid, UUIDConversion.fromString(UUIDConversion.toURN(uuid)));
            if(OUT_LIMIT > i) log("testUuidgenRandomUUID", uuid);
        }
    }

    /**
     * Test uuidgen sequntial UUID
     */
    @Test
    public void testUuidgenSequentialUUID(
    ) throws Exception {
        UUID last = null;
        for(int i = 0; i < UUIDGEN_SEQUENTIAL_UUID.length; i++){
            UUID uuid = UUID.fromString(UUIDGEN_SEQUENTIAL_UUID[i]);
            assertEquals("Leach-Salz variant", uuid.variant(), 2);
            assertEquals("Time-based version", uuid.version(), 1);
            assertEquals("UUID String" + i, UUIDGEN_SEQUENTIAL_UUID[i], uuid.toString());
            assertEquals("toBase36", uuid, UUIDConversion.fromString(UUIDConversion.toUID(uuid)));
            assertEquals("toURN", uuid, UUIDConversion.fromString(UUIDConversion.toURN(uuid)));
            if(OUT_LIMIT > i) log("testUuidgenSequentialUUID", uuid);
            if(last != null){
                assertEquals("Node", uuid.node(), last.node());
                assertTrue("Sequence", uuid.timestamp() > last.timestamp());
            }
            last = uuid;
        }
    }

    /**
     * Test the TimeBasedUUIDBuilder
     */
    @Test
    public void testTimeBasedUUIDBuilder(
    ) throws Exception {
        for(int i = 0; i < UUIDGEN_SEQUENTIAL_UUID.length; i++){
            UUID original = UUID.fromString(UUIDGEN_SEQUENTIAL_UUID[i]);
            if(OUT_LIMIT > i) log("testTimeBasedUUIDBuilder original", original);
            UUID copy = provider.createUUID(original);
            if(OUT_LIMIT > i) log("testTimeBasedUUIDBuilder copy", copy);
            assertEquals(UUIDGEN_SEQUENTIAL_UUID[i],original,copy);
        }
    }

    /**
     * Log a UUID
     * 
     * @param message
     * @param uuid
     */
    private final static void log(
        String message,
        UUID uuid
    ){
        System.out.println(message + ": " + new UUIDFormatter(uuid) + "\tUID: " + UUIDConversion.toUID(uuid));
    }

    
    //------------------------------------------------------------------------
    // Class InternalUUIDProvider
    //------------------------------------------------------------------------

    /**
     * Internal UUID Provider
     */
    static private class InternalUUIDProvider extends TimeBasedIdBuilder {

        UUID source=null;

        /* (non-Javadoc)
         * @see org.openmdx.base.id.spi.UUIDProvider#createUUID()
         */
        UUID createUUID(
            UUID source
        ) {
            this.source = source;
            return new UUID(
                nextMostSignificantBits(),
                nextLeastSignificantBits()
             );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.id.spi.TimeBasedUUIDProvider#getClockSequence()
         */
        protected int getClockSequence() {
            return this.source.clockSequence();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.id.spi.TimeBasedUUIDProvider#getNode()
         */
        protected long getNode() {
            return this.source.node();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.id.spi.TimeBasedUUIDProvider#getTimestamp()
         */
        protected long getTimestamp() {
            return this.source.timestamp();
        }

    }
        
}
