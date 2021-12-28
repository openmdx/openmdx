/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Registry Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2021, OMEX AG, Switzerland
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
package test.openmdx.base.accessor;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;
import org.openmdx.base.collection.WeakRegistry;
import org.openmdx.base.collection.Registry;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.id.UUIDs;

/**
 * Registry Test
 */
public class RegistryTest {

    private final static int DEPTH = 8;

    private final static int KEEP = 1000;

    private final static int RATIO = 10;


    @Test
    public void testRegistry() throws InterruptedException {
        Manager manager = new DataObjectManager();
        PersistenceCapable[] keep = new PersistenceCapable[KEEP];
        for(
            int l = 0;
            l < DEPTH;
            l++
        ){
            manager = new PersistenceManager(manager);
        } 
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long minimalMemory = runtime.totalMemory() - runtime.freeMemory();
        long[] usedMemory = new long[60];
        for(
           int i = 0;
           i < KEEP;
           i++
        ){
            for(
                int j = 0;
                j < RATIO;
                j++
            ){
                PersistenceCapable current = manager.newInstance();
                if(j == 0) {
                    keep[i] = current;
                }
            }
        }
        for(
           int t = 0;
           t < usedMemory.length;
           t++
        ){
            usedMemory[t] = runtime.totalMemory() - runtime.freeMemory() - minimalMemory;
            switch(t){
                case 20:
                    Arrays.fill(keep, null);
                    keep = null;
                    break;
                case 40:
                    manager.clear();
                    break;
                case 50:
                    manager.close();
                    manager = null;
                    break;
            }
            if(manager != null){
                manager.size();
            }
            runtime.gc();
            synchronized (this) {
                this.wait(100l);
            }
        }
        for(
            int t = 1;
            t < usedMemory.length;
            t++
         ){
            System.out.println("Freed memory in gc() invocation " + t + ": "  + (usedMemory[t-1] - usedMemory[t]) + " of " + usedMemory[t-1]);
         }
    }
    

    /**
     * PersistenceCapable
     */
    static interface PersistenceCapable
    {
        UUID getTransientObjectId();

        Path getPersistentObjectId();
    }

    /**
     * DataObject
     */
    static final class DataObject
        implements PersistenceCapable
    {

        /**
         * 
         */
        private final UUID transientObjectId = UUIDs.newUUID();

        /**
         * 
         */
        private final Path persistentObjectId = new Path(this.transientObjectId);

        /*
         * (non-Javadoc)
         * 
         * @seetest.openmdx.base.accessor.TestRegistry.PersistenceCapable#
         * jdoGetPersistentObjectId()
         */
//      @Override
        public Path getPersistentObjectId() {
            return this.persistentObjectId;
        }

        /*
         * (non-Javadoc)
         * 
         * @seetest.openmdx.base.accessor.TestRegistry.PersistenceCapable#
         * jdoGetTransientObjectId()
         */
//      @Override
        public UUID getTransientObjectId() {
            return this.transientObjectId;
        }
    }

    /**
     * DelegatingObject
     */
    static final class DelegatingObject
        implements PersistenceCapable
    {

        /**
         * Constructor
         * 
         * @param delegate
         */
        DelegatingObject(PersistenceCapable delegate) {
            this.delegate = delegate;
        }

        /**
         * 
         */
        private final PersistenceCapable delegate;

        /**
         * @return
         * @see test.openmdx.base.accessor.RegistryTest.PersistenceCapable#getPersistentObjectId()
         */
        public Path getPersistentObjectId() {
            return this.delegate.getPersistentObjectId();
        }

        /**
         * @return
         * @see test.openmdx.base.accessor.RegistryTest.PersistenceCapable#getTransientObjectId()
         */
        public UUID getTransientObjectId() {
            return this.delegate.getTransientObjectId();
        }

    }

    /**
     * Manager
     */
    static interface Manager {
    
        PersistenceCapable newInstance();
        void clear();
        void close();
        int[] size();
        
    }
    
    /**
     * Persistence Manager
     */
    static class PersistenceManager implements Manager {
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        public PersistenceManager(Manager delegate) {
            this.delegate = delegate;
        }
        
        private Registry<PersistenceCapable, PersistenceCapable> registry = new WeakRegistry<PersistenceCapable, PersistenceCapable>(false);
        private Manager delegate;
        
        /* (non-Javadoc)
         * @see test.openmdx.base.accessor.TestRegistry.Manager#newInstance()
         */
        @Override
        public PersistenceCapable newInstance() {
            PersistenceCapable nextInstance = this.delegate.newInstance();
            PersistenceCapable thisInstance = new DelegatingObject(nextInstance);
            this.registry.put(
                nextInstance,
                thisInstance
            );
            return thisInstance;
        }

        /* (non-Javadoc)
         * @see test.openmdx.base.accessor.TestRegistry.Manager#clear()
         */
        @Override
        public void clear() {
            this.delegate.clear();
            this.registry.clear();
        }

        /* (non-Javadoc)
         * @see test.openmdx.base.accessor.TestRegistry.Manager#close()
         */
        @Override
        public void close() {
            this.registry.clear();
            this.delegate.close();
            this.registry = null;
            this.delegate = null;
        }

        /* (non-Javadoc)
         * @see test.openmdx.base.accessor.TestRegistry.Manager#size()
         */
        @Override
        public int[] size() {
            int[] others = this.delegate.size();
            int[] sizes = new int[others.length + 1];
            System.arraycopy(others, 0, sizes, 0, others.length);
            sizes[others.length] = this.registry.values().size();
            return sizes;
        }
        
    }

    /**
     * Data Object Manager
     */
    static class DataObjectManager implements Manager {
        
        private Registry<UUID, Object> transientRegistry = new WeakRegistry<UUID, Object>(false);
        private Registry<Path, Object> persistentRegistry = new WeakRegistry<Path, Object>(false);

        /* (non-Javadoc)
         * @see test.openmdx.base.accessor.TestRegistry.Manager#newInstance()
         */
        @Override
        public PersistenceCapable newInstance(
        ) {
            PersistenceCapable instance = new DataObject();
            this.transientRegistry.put(instance.getTransientObjectId(), instance);
            this.persistentRegistry.put(instance.getPersistentObjectId(), instance);
            return instance;
        }

        /* (non-Javadoc)
         * @see test.openmdx.base.accessor.TestRegistry.Manager#clear()
         */
        @Override
        public void clear() {
            this.persistentRegistry.clear();
            this.transientRegistry.clear();
        } 

        /* (non-Javadoc)
         * @see test.openmdx.base.accessor.TestRegistry.Manager#close()
         */
        @Override
        public void close() {
            clear();
            this.persistentRegistry = null;
            this.transientRegistry = null;
        }

        /* (non-Javadoc)
         * @see test.openmdx.base.accessor.TestRegistry.Manager#size()
         */
        @Override
        public int[] size() {
            return new int[]{
                this.transientRegistry.values().size(),
                this.persistentRegistry.values().size()
            };
        }

    }

}
