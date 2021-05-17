/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Virtual Object Provider 
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
package org.openmdx.base.caching.virtualobjects;

import java.util.HashMap;
import java.util.Map;

import javax.cache.integration.CacheLoaderException;
import javax.resource.ResourceException;

import org.openmdx.base.accessor.rest.spi.ObjectRecords;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;

/**
 * Standard Virtual Object Provider
 * 
 * @since openMDX 2.17
 */
public class StandardVirtualObjects implements VirtualObjectProvider {

    /**
     * Any Authority is provided, even xri://@openmdx*org.openmdx.kernel.
     */
    private static final Path AUTHORITY_PATTERN = new Path("xri://@openmdx*($..)");
    
    /**
     * Any Provider is provided
     */
    private static final Path PROVIDER_PATTERN = new Path("xri://@openmdx*($..)/provider/($..)");
    
    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.caching.virtualobjects.VirtualObjectProvider#provides(Path)
     */
    @Override
    public boolean provides(Path key) {
        return key.isLike(AUTHORITY_PATTERN) || key.isLike(PROVIDER_PATTERN);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.cache.integration.CacheLoader#load(java.lang.Object)
     */
    @Override
    public ObjectRecord load(
        Path key
    ) throws CacheLoaderException {
        try {
            return 
                key.isLike(AUTHORITY_PATTERN) ? ObjectRecords.createVirtualObjectRecord(key, "org:openmdx:base:Authority") :
                key.isLike(PROVIDER_PATTERN) ? ObjectRecords.createVirtualObjectRecord(key, "org:openmdx:base:Provider") :
                null;
        } catch (ResourceException exception) {
            throw new CacheLoaderException(exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.cache.integration.CacheLoader#loadAll(java.lang.Iterable)
     */
    @Override
    public Map<Path, ObjectRecord> loadAll(Iterable<? extends Path> keys)
        throws CacheLoaderException {
        final Map<Path, ObjectRecord> result = new HashMap<>();
        for (Path key : keys) {
            final ObjectRecord object = load(key);
            if (object != null) {
                result.put(key, object);
            }
        }
        return result;
    }

}
