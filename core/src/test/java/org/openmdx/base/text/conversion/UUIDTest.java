/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test UUID 
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
package org.openmdx.base.text.conversion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.ietf.jgss.Oid;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.text.format.UUIDFormatter;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.openmdx.kernel.id.spi.TimeBasedIdBuilder;
import org.openmdx.kernel.log.SysLog;

/**
 * TestUUID
 */
public class UUIDTest {

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

    final static private String[] OBJECT_ID_UUID = {
        "3878a220-0f81-11dc-804a-0002a5d5c51b" // sunrise
    };
    
    final static private String[] OBJECT_ID_DOT = {
        "2.25.75063131677434771150912906774302803227" // sunrise
    };

    final static private InternalUUIDProvider provider = new InternalUUIDProvider();

    /**
     * Defines how many entries per test are written to the Log -
     * except for the allocation performance test of course.
     */
    final static int OUT_LIMIT = 1;

    final static int GENERATOR_COUNT = 3;
    
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
        SysLog.info("nsec per UUID", System.currentTimeMillis() - t);
    }

    /**
     * Test Java Random UID
     */
    @Test
    public void testUUIDs(
    ){
        UUIDGenerator[] generator = new UUIDGenerator[GENERATOR_COUNT];
        for(int g = 0; g < GENERATOR_COUNT; g++) {
            generator[g] = UUIDs.getGenerator();
        }
        for(int i = 0; i < 8; i++){
            UUID uuid = generator[i % GENERATOR_COUNT].next();
            Assertions.assertEquals(2, uuid.variant(), "Leach-Salz variant");

            Assertions.assertEquals(uuid, UUIDConversion.fromString(UUIDConversion.toUID(uuid)), "Base36");

            Assertions.assertEquals(uuid, UUIDConversion.fromString(UUIDConversion.toURN(uuid)), "urn");

            if(OUT_LIMIT > i) log("testUUIDs", uuid);
        }
        UUID[] uuids = new UUID[500000];
        for(int i=0; i < uuids.length; i++){
           uuids[i] = generator[i % GENERATOR_COUNT].next();
           Assertions.assertEquals(uuids[i], UUIDConversion.fromString(UUIDConversion.toUID(uuids[i])), "Base36");

        }
        Assertions.assertEquals(uuids.length, new HashSet<>(Arrays.asList(uuids)).size(), "Duplicates");

    }
    
    /**
     * Test NIL
     */
    @Test
    public void testNIL(
    ){
        UUID uuid = UUIDs.NIL;
        Assertions.assertEquals(new UUID(0L, 0L), uuid, "NIL value");

        Assertions.assertEquals("00000000-0000-0000-0000-000000000000", uuid.toString(), "NIL String");

        Assertions.assertEquals(uuid, UUIDConversion.fromString(UUIDConversion.toUID(uuid)), "toBase36");

        Assertions.assertEquals(uuid, UUIDConversion.fromString(UUIDConversion.toURN(uuid)), "toURN");

        if(OUT_LIMIT > 0) log("testNIL", uuid);
    }

    /**
     * Test Java Random UID
     */
    @Test
    public void testJavaRandomUUID(
    ){
        for(int i = 0; i < 8; i++){
            UUID uuid = UUID.randomUUID();
            Assertions.assertEquals(2, uuid.variant(), "Leach-Salz variant");
            Assertions.assertEquals(4, uuid.version(), "Randomly generated");
            Assertions.assertEquals(UUIDConversion.fromString(UUIDConversion.toUID(uuid)), uuid, "toBase36");
            Assertions.assertEquals(UUIDConversion.fromString(UUIDConversion.toURN(uuid)), uuid, "toURN");
            if(OUT_LIMIT > i) log("testJavaRandomUUID", uuid);
        }
        UUID[] uuids = new UUID[500000];
        for(int i=0; i < uuids.length; i++){
            uuids[i] = UUID.randomUUID();
            Assertions.assertEquals(uuids[i], UUIDConversion.fromString(UUIDConversion.toUID(uuids[i])), "Base36");

        }
        Assertions.assertEquals(uuids.length, new HashSet<>(Arrays.asList(uuids)).size(), "Duplicates");

    }

    /**
     * Test uuidgen random UUID
     */
    @Test
    public void testUuidgenRandomUUID(
    ){
        for(int i = 0; i < UUIDGEN_RANDOM_UUID.length; i++){
            UUID uuid = UUID.fromString(UUIDGEN_RANDOM_UUID[i]);
            Assertions.assertEquals(2, uuid.variant(), "Leach-Salz variant");

            Assertions.assertEquals(4, uuid.version(), "Randomly generated");

            Assertions.assertEquals(uuid.toString(), UUIDGEN_RANDOM_UUID[i], "UUID String");

            Assertions.assertEquals(UUIDConversion.fromString(UUIDConversion.toUID(uuid)), uuid, "toBase36");

            Assertions.assertEquals(UUIDConversion.fromString(UUIDConversion.toURN(uuid)), uuid, "toURN");

            if(OUT_LIMIT > i) log("testUuidgenRandomUUID", uuid);
        }
    }

    /**
     * Test uuidgen sequential UUID
     */
    @Test
    public void testUuidgenSequentialUUID(
    ){
        UUID last = null;
        for(int i = 0; i < UUIDGEN_SEQUENTIAL_UUID.length; i++){
            UUID uuid = UUID.fromString(UUIDGEN_SEQUENTIAL_UUID[i]);
            Assertions.assertEquals(2, uuid.variant(), "Leach-Salz variant");

            Assertions.assertEquals(1, uuid.version(), "Time-based version");

            Assertions.assertEquals(UUIDGEN_SEQUENTIAL_UUID[i], uuid.toString(), "UUID String" + i);

            Assertions.assertEquals(uuid, UUIDConversion.fromString(UUIDConversion.toUID(uuid)), "toBase36");

            Assertions.assertEquals(uuid, UUIDConversion.fromString(UUIDConversion.toURN(uuid)), "toURN");

            if(OUT_LIMIT > i) log("testUuidgenSequentialUUID", uuid);
            if(last != null){
                Assertions.assertEquals( uuid.node(),  last.node(), "Node");

                Assertions.assertTrue(uuid.timestamp() > last.timestamp(), "Sequence");
            }
            last = uuid;
        }
    }

    /**
     * Test the TimeBasedUUIDBuilder
     */
    @Test
    public void testTimeBasedUUIDBuilder(
    ) {
        for(int i = 0; i < UUIDGEN_SEQUENTIAL_UUID.length; i++){
            UUID original = UUID.fromString(UUIDGEN_SEQUENTIAL_UUID[i]);
            if(OUT_LIMIT > i) log("testTimeBasedUUIDBuilder original", original);
            UUID copy = provider.createUUID(original);
            if(OUT_LIMIT > i) log("testTimeBasedUUIDBuilder copy", copy);
            Assertions.assertEquals(original, copy, UUIDGEN_SEQUENTIAL_UUID[i]);

        }
    }

    /**
     * Test UUID based OID
     */
    @Test
    public void testOID(
    ) {
        for(int i = 0; i < OBJECT_ID_UUID.length; i++){
            UUID uuid = UUID.fromString(OBJECT_ID_UUID[i]);
            Assertions.assertEquals(OBJECT_ID_DOT[i], UUIDConversion.toOID(uuid), "OID");

        }
    }

    /**
     * Test UUID based Oid
     */
    @Test
    public void testOid(
    ) throws Exception {
        for(int i = 0; i < OBJECT_ID_UUID.length; i++){
            UUID uuid = UUID.fromString(OBJECT_ID_UUID[i]);
            Oid oid = new Oid(OBJECT_ID_DOT[i]);
            Assertions.assertEquals(oid, UUIDConversion.toOid(uuid), "Oid");
        }
    }
    
    /**
     * Log a UUID
     */
    private static void log(
        String message,
        UUID uuid
    ){
        SysLog.info(message, "UUID=" + new UUIDFormatter(uuid) + ", UID=" + UUIDConversion.toUID(uuid));
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
        @Override
        protected int getClockSequence() {
            return this.source.clockSequence();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.id.spi.TimeBasedUUIDProvider#getNode()
         */
        @Override
        protected long getNode() {
            return this.source.node();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.id.spi.TimeBasedUUIDProvider#getTimestamp()
         */
        @Override
        protected long getTimestamp() {
            return this.source.timestamp();
        }

    }
        
}
