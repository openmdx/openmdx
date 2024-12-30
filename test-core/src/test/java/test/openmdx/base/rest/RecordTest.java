/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Record Test 
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

package test.openmdx.base.rest;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.MappedRecord;
#endif

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.resource.Records;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;

/**
 * Test Record
 */
public class RecordTest {

    private static final int threadCount = 100;
   
    @Test
    public void testInternalization() throws ResourceException, InterruptedException{
          Worker[] workers = new Worker[threadCount];
          for(int i = 0; i < threadCount; i++) {
              workers[i] = new Worker("Worker-" + i);
          }
          for(int i = 0; i < threadCount; i++) {
              workers[i].start();
          }
          for(int i = 0; i < threadCount; i++) {
              workers[i].join();
              workers[i].validate();
          }
    }
    
    public static void main(
        String... arguments
    ){
        try {
            new RecordTest().testInternalization();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    
    static class Worker extends Thread {
        
        /**
         * Constructor 
         *
         * @param name
         * @throws ResourceException 
         */
        public Worker(
            String name
        ) throws ResourceException {
            super(name);
            source = UUIDs.getGenerator();
            target = Records.getRecordFactory().createMappedRecord("UUIDs");
        }

        private final UUIDGenerator source;
        private final MappedRecord target;
        private final static int entryCount = 1000;
        
        /* (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @SuppressWarnings("unchecked")
		@Override
        public void run() {
            for(int i = 0; i < entryCount; i++) {
                UUID value = source.next();
                String key = value.toString();
                target.put(key, value);
            }
        }

        public void validate(){
            for(Object e : target.entrySet()) {
                Map.Entry<?,?> o = (Entry<?, ?>) e;
                Assertions.assertEquals(o.getKey(), o.getValue().toString());
            }
            System.out.println(getName() + " with " + target.size() + " entries is valid");
        }
        
    }
    
}
