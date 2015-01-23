/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Object Record
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2014, OMEX AG, Switzerland
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
package org.openmdx.base.rest.cci;

import java.util.UUID;

import javax.resource.cci.MappedRecord;

/**
 * <code>org::openmdx::kernel::Object</code>
 */
public interface ObjectRecord extends RequestRecord {
    
    /**
     * An object record's name
     */
    String NAME = "org:openmdx:kernel:Object";
    
    /**
     * Retrieve the object's values
     * 
     * @return the object's values
     */
    MappedRecord getValue();
    
    /**
     * Set the object's values
     * 
     * @param value the object's values
     */
    void setValue(MappedRecord value);
    
    /**
     * Retrieve the object's version
     * 
     * @return the object's version
     */
    Object getVersion();
    
    /**
     * Set the object's lock
     * 
     * @param lock the object's version
     */
    void setVersion(Object lock);
    
    /**
     * Retrieve the object's lock
     * 
     * @return the object's lock
     */
    Object getLock();
    
    /**
     * Set the object's lock
     * 
     * @param lock the object's lock
     */
    void setLock(Object lock);

    /**
     * Retrieves the object's transient id
     * <p>
     * This value is used to correlate transient objects
     * between a persistence manager and its proxy.
     * 
     * @return the object's transient id
     */
    UUID getTransientObjectId();
    
    /**
     * Set the object's transient id
     * <p>
     * This value is used to correlate transient objects
     * between a persistence manager and its proxy.
     * 
     * @param transientObjectId
     */
    void setTransientObjectId(UUID transientObjectId);
    
    /**
     * Clone
     * 
     * @return a clone of this record
     */
    ObjectRecord clone();

    enum Member {
        lock,
        resourceIdentifier,
        transientObjectId,
        value,
        version
    }

}
