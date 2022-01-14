/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: UUIDs Test 
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
package org.openmdx.kernel.id;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.id.cci.UUIDGenerator;

/**
 * UUIDs Test
 */
public class UUIDsTest {

    private final static int PROCESSOR_COUNT = 2;
    private final static int REPETIONS = 5;
    
    @Test
    public void testThreadLocalUUIDGenerator() throws InterruptedException{
        for(int i = 0; i < REPETIONS; i++) {
            AbstractUUIDProvider[] processors = UsingThreadLocalGenerator.create(PROCESSOR_COUNT);
            process(processors);
            validate(processors);
        }
    }
    
    @Test
    public void testInstancePrivateGenerator() throws InterruptedException{
        for(int i = 0; i < REPETIONS; i++) {
            AbstractUUIDProvider[] processors = UsingInstancePrivateGenerator.create(PROCESSOR_COUNT);
            process(processors);
            validate(processors);
        }
    }

    @Test
    public void testInternalGenerator() throws InterruptedException{
        AbstractUUIDProvider[] processors = UsingInternalGenerator.create(PROCESSOR_COUNT);
        process(processors);
        validate(processors);
        externalize(processors, false);
        externalize(processors, true);
    }    

    private void process(
        Thread[] processors
    ) throws InterruptedException {
        for(int i = 0; i < processors.length; i++){
            processors[i].start();
        }
        for(int i = 0; i < processors.length; i++){
            processors[i].join();
        }
    }
    
    private void validate(
        UUIDProvider[] uuidFactories
    ){
        int count = 0;
        final Set<UUID> uuids = new HashSet<UUID>();
        for(UUIDProvider uuidFactory : uuidFactories) {
            UUID[] generatedUUIDs = uuidFactory.getGeneratedUUIDs();
            count += generatedUUIDs.length;
            uuids.addAll(Arrays.asList(generatedUUIDs));
        }
        Assertions.assertEquals(count, uuids.size(),"Duplicate UUIDs");
        System.out.println("UUID count: " + count);
    }

    private void externalize(
        UUIDProvider[] uuidFactories,
        boolean uid
    ){
        long start = System.nanoTime();
        int i = 0;
        for(UUIDProvider uuidFactory : uuidFactories) {
            for(UUID uuid : uuidFactory.getGeneratedUUIDs()) {
                (uid ? UUIDConversion.toUID(uuid) : uuid.toString()).length();
                i++;
            }
        }
        long end = System.nanoTime();
        System.out.println((uid ? "UID" : "UUID") + " conversion takes " + ((end - start)/i) + " nanoseconds");
    }
    
    interface UUIDProvider {
        UUID[] getGeneratedUUIDs();
    }
    
    static abstract class AbstractUUIDProvider extends Thread implements UUIDProvider {

        private static int UUID_COUNT = 100000;
        private final UUID[] uuids = new UUID[UUID_COUNT];

        protected abstract UUID newUUID();
        
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            for(int i = 0; i < UUID_COUNT; i++) {
                uuids[i] = newUUID();
            }
        }

        /* (non-Javadoc)
         * @see test.openmdx.kernel.id.TestUUIDs.Factory#getGeneratedUUIDs()
         */
        @Override
        public UUID[] getGeneratedUUIDs() {
            return this.uuids;
        }
        
    }
    
    static class UsingThreadLocalGenerator extends AbstractUUIDProvider {
        
        /* (non-Javadoc)
         * @see test.openmdx.kernel.id.TestUUIDs.AbstractUUIDFactory#newUUID()
         */
        @Override
        protected UUID newUUID() {
            return UUIDs.newUUID();
        }

        static AbstractUUIDProvider[] create(
            int processorCount
        ) {
            System.out.println(UsingThreadLocalGenerator.class.getSimpleName());
            AbstractUUIDProvider[] processors = new AbstractUUIDProvider[processorCount];
            for(int i = 0; i < processorCount; i++){
                processors[i] = new UsingThreadLocalGenerator();
            }
            return processors;
        }

    }

    static class UsingInstancePrivateGenerator extends AbstractUUIDProvider {

        
        private final UUIDGenerator generator = UUIDs.getGenerator();
                
        /* (non-Javadoc)
         * @see test.openmdx.kernel.id.TestUUIDs.AbstractUUIDFactory#newUUID()
         */
        @Override
        protected UUID newUUID() {
            return generator.next();
        }

        static AbstractUUIDProvider[] create(
            int processorCount
        ) {
            System.out.println(UsingInstancePrivateGenerator.class.getSimpleName());
            AbstractUUIDProvider[] processors = new AbstractUUIDProvider[processorCount];
            for(int i = 0; i < processorCount; i++){
                processors[i] = new UsingInstancePrivateGenerator();
            }
            return processors;
        }
    }

    static class UsingInternalGenerator extends AbstractUUIDProvider {
                
        /* (non-Javadoc)
         * @see test.openmdx.kernel.id.TestUUIDs.AbstractUUIDFactory#newUUID()
         */
        @Override
        protected UUID newUUID() {
            return UUID.randomUUID();
        }

        static AbstractUUIDProvider[] create(
            int processorCount
        ) {
            System.out.println(UsingInternalGenerator.class.getSimpleName());
            AbstractUUIDProvider[] processors = new AbstractUUIDProvider[processorCount];
            for(int i = 0; i < processorCount; i++){
                processors[i] = new UsingInternalGenerator();
            }
            return processors;
        }
    }
    
}
