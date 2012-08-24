/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: openMDX Object Layer: Object Factory Interface
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.cci;

import java.util.UUID;

import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.exception.ServiceException;

/**
 * openMDX Data Access Service interface.
 */
public interface DataObjectManager_1_0 extends PersistenceManager_1_0 {

    /**
     * Create a transient object
     * 
     * @param       objectClass
     *              The model class of the object to be created
     * @param       transientObjectId, may be <code>null</code> to create a new one
     *
     * @return      a new object instance
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the object factory is closed
     * @exception   ServiceException    BAD_PARAMETER
     *              if the objectClass is an Aspect sub-class             
     */
    DataObject_1_0 newInstance(
        String objectClass, 
        UUID transientObjectId
    ) throws ServiceException;
    
    /**
     * Retrieve the optimal fetch size
     * 
     * @return the optimal fetch size
     */
    int getOptimalFetchSize(
    );

    /**
     * Retrieve the cache threshold value
     * 
     * @return the cache threshold value
     */
    int getCacheThreshold(
    );


}
