/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ManagedConnectionCache_2_0.java,v 1.3 2010/05/17 10:01:03 hburger Exp $
 * Description: Managed Connection Cache 2.0
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/05/17 10:01:03 $
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

import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;

/**
 * Managed Connection Cache 2.0
 */
public interface ManagedConnectionCache_2_0 {

    /**
     * Tells whether the default fetch set of a given object is cached
     *
     * @param mode the cache to be inspected
     * @param xri the object id
     * 
     * @return <code>true</code> if the default fetch set of a given object is cached
     * 
     * @exception ServiceException in case of failure
     */
    boolean isAvailable(
        Mode mode, 
        Path xri
    ) throws ServiceException;
    
    /**
     * Retrieve object from the cache
     * 
     * @param xri the object id
     * @return the cached object; or <code>null</code> if it is not available
     * 
     * @exception ServiceException in case of failure
     */
    MappedRecord peek(
        Path xri
    ) throws ServiceException;
    
    /**
     * Offer values to the cache
     *
     * @param mode offer the value to the given cache, or <code>null</code> for any cache
     * @param object the object to cache
     * 
     * @return <code>true</code> if the object has been cached
     * @exception ServiceException in case of failure
     */
    boolean put(
        Mode mode,
        ObjectRecord object
    ) throws ServiceException;    
    
    
    //------------------------------------------------------------------------
    // Enum Mode
    //------------------------------------------------------------------------

    /**
     * The caching mode
     */
    enum Mode {
        BASIC,
        PINNING,
        AUTOMATIC
    }
    
}
