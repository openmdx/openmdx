/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractMarshaller.java,v 1.1 2008/12/15 11:35:46 hburger Exp $
 * Description: Abstract Marshaller
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 11:35:46 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.rmi.spi;

import java.rmi.Remote;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.stream.rmi.cci.BinarySink_1OutputStream;
import org.openmdx.base.stream.rmi.cci.BinarySink_1_0;
import org.openmdx.base.stream.rmi.cci.BinarySource_1InputStream;
import org.openmdx.base.stream.rmi.cci.BinarySource_1_0;
import org.openmdx.base.stream.rmi.cci.CharacterSink_1Writer;
import org.openmdx.base.stream.rmi.cci.CharacterSink_1_0;
import org.openmdx.base.stream.rmi.cci.CharacterSource_1Reader;
import org.openmdx.base.stream.rmi.cci.CharacterSource_1_0;
import org.openmdx.kernel.security.ExecutionContext;

/**
 * Abstract Marshaller
 */
public abstract class AbstractMarshaller
    implements DataproviderObjectInterceptor
{

    /**
     * Constructor 
     */
    protected AbstractMarshaller() {
        super();
    }

    /**
     * Retrieve the appropriate callback context.
     * 
     * @return the callback context
     * 
     * @throws ServiceException
     */
    protected abstract ExecutionContext getCallbackContext(
    );

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.rmi.spi.DataproviderObjectInterceptor#unmarshal(java.rmi.Remote)
     */
    public Object unmarshal(Remote value) {
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

}
