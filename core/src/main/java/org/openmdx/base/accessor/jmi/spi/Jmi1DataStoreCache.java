/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Delegating Datastore Cache 
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

package org.openmdx.base.accessor.jmi.spi;

import java.util.Collection;
import java.util.function.Predicate;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.caching.datastore.CacheAdapter;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.kernel.jdo.JDODataStoreCache;


/**
 * JMI Data Store Cache
 * 
 * @since openMDX 2.17
 */
public class Jmi1DataStoreCache implements JDODataStoreCache {

    /**
     * Constructor 
     *
     * @param delegate the JDO Data Store Cache
     */
    Jmi1DataStoreCache(
        CacheAdapter delegate,
        Mapping_1_0 mapping
    ) {
        this.delegate = delegate;
        this.mapping = mapping;
    }

    private final CacheAdapter delegate;
    
    /**
     * The mapping allows to determine the model class name for
     * a given jmi1 interface.
     */
    private final Mapping_1_0 mapping;
    
    /* (non-Javadoc)Mapping_1_0
     * @see javax.jdo.datastore.DataStoreCache#evict(java.lang.Object)
     */
    @Override
    public void evict(Object xri) {
        delegate.evict(xri);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#evictAll()
     */
    @Override
    public void evictAll() {
        delegate.evictAll();
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#evictAll(java.lang.Object[])
     */
    @Override
    public void evictAll(Object... xris) {
        delegate.evictAll(xris);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#evictAll(java.util.Collection)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void evictAll(Collection xris) {
        delegate.evictAll(xris);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#evictAll(java.lang.Class, boolean)
     *
     * @deprecated with JDO 2.1
     * removed with JDO 3.2
     */
    @SuppressWarnings("rawtypes")
    @Deprecated
    public void evictAll(
        Class pcClass,
        boolean subclasses
    ) {
        evictAll(subclasses, pcClass);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#evictAll(boolean, java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void evictAll(
        boolean subclasses,
        Class pcClass
    ) {
        delegate.evictAll(toPredicate(subclasses, pcClass));
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#pin(java.lang.Object)
     */
    @Override
    public void pin(Object xri) {
        delegate.pin(xri);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#pinAll(java.util.Collection)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void pinAll(Collection xris) {
        delegate.pinAll(xris);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#pinAll(java.lang.Object[])
     */
    @Override
    public void pinAll(Object... xris) {
        delegate.pinAll(xris);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#pinAll(java.lang.Class, boolean)
     *
     * @deprecated with JDO 2.1
     * removed with JDO 3.2
     */
    @SuppressWarnings("rawtypes")
    @Deprecated
    public void pinAll(
        Class pcClass,
        boolean subclasses
    ) {
        pinAll(subclasses, pcClass);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#pinAll(boolean, java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void pinAll(
        boolean subclasses,
        Class pcClass
    ) {
        delegate.pinAll(toPredicate(subclasses, pcClass));
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#unpin(java.lang.Object)
     */
    @Override
    public void unpin(Object xri) {
        delegate.unpin(xri);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#unpinAll(java.util.Collection)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void unpinAll(Collection xris) {
        delegate.unpinAll(xris);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#unpinAll(java.lang.Object[])
     */
    @Override
    public void unpinAll(Object... xris) {
        delegate.unpinAll(xris);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#unpinAll(java.lang.Class, boolean)
     *
     * @deprecated with JDO 2.1
     * removed with JDO 3.2
     */
    @SuppressWarnings("rawtypes")
    @Deprecated
    public void unpinAll(
        Class pcClass,
        boolean subclasses
    ) {
        unpinAll(subclasses, pcClass);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#unpinAll(boolean, java.lang.Class)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void unpinAll(
        boolean subclasses,
        Class pcClass
    ) {
        delegate.unpinAll(toPredicate(subclasses, pcClass));
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.jdo.JDODataStoreCache#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap(Class<T> clazz) {
        return delegate.unwrap(clazz);
    }

    private Predicate<ObjectRecord> toPredicate(
        boolean subclasses,
        Class<?> pcClass
    ){
        try {
            return Jmi1ClassPredicate.newInstance(
                subclasses,
                mapping.getModelClassName(pcClass)
            );
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }  
    }

}
