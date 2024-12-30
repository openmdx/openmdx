/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Caching Port
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
package org.openmdx.base.caching.port;

import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;

import org.openmdx.base.caching.datastore.CacheAdapter;
import org.openmdx.base.caching.virtualobjects.VirtualObjectProvider;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;

/**
 * Standard Caching Port
 * 
 * @since openMDX 2.17
 */
public class StandardCachingPort implements CachingPort {

    private VirtualObjectProvider virtualObjectProvider;
    private CacheAdapter cacheAdapter;

    @Override
    public void setVirtualObjectProvider(
        VirtualObjectProvider virtualObjectProvider
    ){
        this.virtualObjectProvider = virtualObjectProvider;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.caching.port.CachingPort#setCacheAdapter(org.openmdx.base.caching.datastore.CacheAdapter)
     */
    @Override
    public void setCacheAdapter(CacheAdapter cacheAdapter) {
        this.cacheAdapter = cacheAdapter;
    }
    
    //------------------------------------------------------------------------
    // Implements Port
    //------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public CachingInteraction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new CachingInteraction(connection);
    }

    
    //------------------------------------------------------------------------
    // Implements CachningPort
    //------------------------------------------------------------------------

    protected ObjectRecord getCachedObject(Path xri) {
        return this.virtualObjectProvider.provides(xri) ? 
            this.virtualObjectProvider.load(xri) :
        this.cacheAdapter.containsKey(xri) ?
            this.cacheAdapter.get(xri) :
        null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.caching.port.CachingPort#offer(org.openmdx.base.rest.cci.ObjectRecord)
     */
    @Override
    public void offer(ObjectRecord objectRecord) {
        this.cacheAdapter.offer(objectRecord);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.caching.port.CachingPort#containsKey(org.openmdx.base.naming.Path)
     */
    @Override
    public boolean containsKey(Path xri) {
        return 
            this.virtualObjectProvider.provides(xri) ||
            this.cacheAdapter.containsKey(xri);
    }


    //------------------------------------------------------------------------
    // Class CachingInteraction
    //------------------------------------------------------------------------

    /**
     * Caching Interaction
     */
    public class CachingInteraction extends AbstractRestInteraction {

        /**
         * Constructor
         */
        protected CachingInteraction(
            RestConnection connection
        ) {
            super(connection);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        ) throws ResourceException {
            final ObjectRecord object = getCachedObject(input.getResourceIdentifier());
            final boolean gotten = object != null;
            if(gotten) {
                output.add(object);
            }
            return gotten;
        }

    }

}
