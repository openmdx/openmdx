/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Data Store Cache 2.0
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.rest.spi;

import java.util.Collection;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;


/**
 * Data Store Cache 2.0
 */
public interface DataStoreCache_2_0 {

    //------------------------------------------------------------------------
    // Eviction
    //------------------------------------------------------------------------
    
    /** 
     * Evict the parameter instance from the second-level cache.
     * 
     * @param xri the object id of the instance to evict.
     * 
     * @exception ServiceException in case of failure
     */
    void evict (
        Path xri
    ) throws ServiceException;

    /** 
     * Evict the parameter instances from the second-level cache.
     * All instances in the PersistenceManager's cache are evicted 
     * from the second-level cache.
     * 
     * @exception ServiceException in case of failure
     */
    void evictAll (
    ) throws ServiceException;

    /** 
     * Evict the parameter instances from the second-level cache.
     * @param xriPattern the object id pattern of the instance to evict.
     * 
     * @exception ServiceException in case of failure
     */
    void evictAll (
        Path xriPattern
    ) throws ServiceException;
    
    /**
     * Evict the parameter instances from the second-level cache.
     * 
     * @param xris the object ids of the instance to evict.
     * 
     * @exception ServiceException in case of failure
     */
    void evictAll (
        Collection<Path> xris
    ) throws ServiceException;

    /** 
     * Evict the parameter instances from the second-level cache.
     * 
     * @param pcClass the class of instances to evict
     * @param subclasses if true, evict instances of subclasses also
     * 
     * @exception ServiceException in case of failure
     */
    void evictAll (
        boolean subclasses, 
        String pcClass
    ) throws ServiceException;

    
    //------------------------------------------------------------------------
    // Pinning
    //------------------------------------------------------------------------

    /** 
     * Pin the parameter instance in the second-level cache.
     * 
     * @param xri the object id of the instance to pin.
     * 
     * @exception ServiceException in case of failure
     */
    void pin (
        Path xri
    ) throws ServiceException;

    /** 
     * Pin the parameter instances in the second-level cache.
     * 
     * @param xris the object ids of the instances to pin.
     * 
     * @exception ServiceException in case of failure
     */
    void pinAll (
        Collection<Path> xris
    ) throws ServiceException;

    /** 
     * Pin the parameter instances in the second-level cache.
     * @param xriPattern the object id pattern of the instances to pin.
     * 
     * @exception ServiceException in case of failure
     */
    void pinAll (
        Path xriPattern
    ) throws ServiceException;

    /** 
     * Pin instances in the second-level cache.
     * 
     * @param pcClass the class of instances to pin
     * @param subclasses if true, pin instances of subclasses also
     * 
     * @exception ServiceException in case of failure
     */
    void pinAll (
        boolean subclasses, 
        String pcClass
    ) throws ServiceException;

    /** 
     * Unpin the parameter instance from the second-level cache.
     * @param xri the object id of the instance to unpin.
     * 
     * @exception ServiceException in case of failure
     */
    void unpin(
        Path xri
    ) throws ServiceException;

    /** 
     * Unpin the parameter instances from the second-level cache.
     * @param xris the object ids of the instance to evict.
     * 
     * @exception ServiceException in case of failure
     */
    void unpinAll(
        Collection<Path> xris
    ) throws ServiceException;

    /** 
     * Unpin the parameter instances from the second-level cache.
     * 
     * @param xriPattern the object id pattern of the instances to evict.
     * 
     * @exception ServiceException in case of failure
     */
    void unpinAll(
        Path xriPattern
    ) throws ServiceException;

    /** 
     * Unpin instances from the second-level cache.
     * 
     * @param pcClass the class of instances to unpin
     * @param subclasses if true, unpin instances of subclasses also
     * 
     * @exception ServiceException in case of failure
     */
    void unpinAll(
        boolean subclasses, 
        String pcClass
    ) throws ServiceException;

}
