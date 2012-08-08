/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractContainer.java,v 1.4 2008/01/08 16:16:31 hburger Exp $
 * Description: Abstract Container
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/08 16:16:31 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.delegation;

import java.io.Serializable;
import java.util.AbstractCollection;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.Container;


/**
 * Abstract Container
 */

abstract class AbstractContainer<E>
    extends AbstractCollection<E>
    implements Container<E>, Serializable






{

    /**
     * The next qualifier.
     */
    private long nextQualifier = SEQUENCE_INITIALIZATION_PENDING;

    /**
     * Initial qualifier value callback method.
     * 
     * @return the initial qualifier value; or -1L for UUIDs
     * 
     * @exception ServiceException
     */
    protected abstract long initialQualifier(
    ) throws ServiceException;

    /**
     * Get the next qualifier
     * 
     * @return the next qualifier,
     * or null if a UID shoid be used
     */
    synchronized String nextQualifier(
    ){
        if(this.nextQualifier == SEQUENCE_INITIALIZATION_PENDING) try {
            this.nextQualifier = initialQualifier();
        } catch (ServiceException exception) {
            // this.nextQualifier remains < 0L
        } finally {
            if(this.nextQualifier < 0L) this.nextQualifier = SEQUENCE_NOT_SUPPORTED;
        }
        return this.nextQualifier == SEQUENCE_NOT_SUPPORTED ?
            null :
            String.valueOf(this.nextQualifier++);
    }

    /**
     * Evict the object
     */
    protected void evict(
    ){
        this.nextQualifier = SEQUENCE_INITIALIZATION_PENDING;
    }

    /**
     * The current sequence state has to be requested from the subclass.
     */
    private static final long SEQUENCE_INITIALIZATION_PENDING = -2L;

    /**
     * The subclass has requested to use UUIDs instead of sequence values.
     */
    protected static final long SEQUENCE_NOT_SUPPORTED = -1L;

    /**
     * If sequences are supported by either the application or persistence 
     * layer.
     */
    protected static final long SEQUENCE_MIN_VALUE = 0L;

}
