/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractMarshaller.java,v 1.2 2009/02/02 15:47:54 hburger Exp $
 * Description: RMI Mapping: DataproviderObject Interceptor Interface
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/02 15:47:54 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.transport.rmi;

import java.rmi.Remote;

import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.stream.rmi.cci.BinarySink_1OutputStream;
import org.openmdx.base.stream.rmi.cci.BinarySink_1_0;
import org.openmdx.base.stream.rmi.cci.BinarySource_1InputStream;
import org.openmdx.base.stream.rmi.cci.BinarySource_1_0;
import org.openmdx.base.stream.rmi.cci.CharacterSink_1Writer;
import org.openmdx.base.stream.rmi.cci.CharacterSink_1_0;
import org.openmdx.base.stream.rmi.cci.CharacterSource_1Reader;
import org.openmdx.base.stream.rmi.cci.CharacterSource_1_0;
import org.openmdx.base.stream.rmi.spi.StreamSynchronization_1_1;
import org.openmdx.kernel.security.ExecutionContext;

/**
 * Marshals remote streams and service exceptions
 */
public abstract class AbstractMarshaller {

    protected ExecutionContext getCallbackContext(
    ) {
        return null;
    }
    
    /**
     * The optional argument length is stored at position 1
     * 
     * @param values
     */
    protected static long getLength(
        SparseList<?> values
    ){
        Object length = values.get(1);
        return length instanceof Long ? ((Long)length).longValue() : -1L;
    }
    
    /**
     * Marshal an object
     *
     * @param   object
     *          The object to be inspected
     *
     * @exception   ServiceException    MEDIA_ACCESS
     *              if RMI transport is unavailable
     */ 
    abstract protected void marshal(
        DataproviderObject object
    ) throws ServiceException;
        
    /**
     * Unmarshal a stream
     */
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.rmi.spi.DataproviderObjectInterceptor#unmarshal(java.rmi.Remote)
     */
    protected Object unmarshal(Remote value) {
        if(value == null) {
            return null;
        }
        if(value instanceof BinarySource_1_0) {
            return new BinarySource_1InputStream(
                (BinarySource_1_0)value,
                getCallbackContext()
            );
        }
        if (value instanceof CharacterSource_1_0) {
            return new CharacterSource_1Reader(
                (CharacterSource_1_0)value,
                getCallbackContext()
            );
        }
        if (value instanceof BinarySink_1_0) {
            return new BinarySink_1OutputStream(
                (BinarySink_1_0)value,
                getCallbackContext()
            );
        }
        if (value instanceof CharacterSink_1_0) {
            return new CharacterSink_1Writer(
                (CharacterSink_1_0)value,
                getCallbackContext()
            );
        }
        throw new IllegalArgumentException(
            "Unable to unmarshal " + value.getClass().getName()
        );
    }

    /**
     * Unmarshal an object
     *
     * @param   object
     *          The object to be inspected
     */ 
    abstract protected void unmarshal(
        DataproviderObject object
    );
     
    /**
     * Intercept an object by applying the same rules in both directions.
     *  
     * @param   object
     *          The object to be inspected
     */
    abstract protected void intercept (
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
    abstract protected void intercept (
        DataproviderObject object, 
        StreamSynchronization_1_1 synchronization
    ) throws ServiceException;

}
