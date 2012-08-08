/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderObjectInterceptor.java,v 1.4 2008/12/15 11:35:46 hburger Exp $
 * Description: RMI Mapping: DataproviderObject Interceptor Interface
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 11:35:46 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.compatibility.base.dataprovider.transport.rmi.spi;

import java.rmi.Remote;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.stream.rmi.spi.StreamSynchronization_1_1;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;

/**
 * Marshalls remote streams and service exceptions
 */
public interface DataproviderObjectInterceptor {

    /**
     * Marshal an object
     *
     * @param   object
     *          The object to be inspected
     *
     * @exception   ServiceException    MEDIA_ACCESS
     *              if RMI transport is unavailable
     */ 
    void marshal(
        DataproviderObject object
    ) throws ServiceException;
        
    /**
     * Unmarshal a stream
     */
    Object unmarshal (
        Remote stream
    );

    /**
     * Unmarshal an object
     *
     * @param   object
     *          The object to be inspected
     */ 
    void unmarshal(
        DataproviderObject object
    );
     
    /**
     * Intercept an object by applying the same rules in both directions.
     *  
     * @param   object
     *          The object to be inspected
     */
    void intercept (
        DataproviderObject object
    ) throws ServiceException;

    /**
     * Intercept an object buffering streams in case of transaction boundary.
     * 
     * @param object The object to be inspected
     * @param synchronization the synchronization object in case of a transaction
     * boundary, <code>null</code> otherwise.
     * 
     * @throws ServiceException
     */
    void intercept (
        DataproviderObject object, 
        StreamSynchronization_1_1 synchronization
    ) throws ServiceException;

}
