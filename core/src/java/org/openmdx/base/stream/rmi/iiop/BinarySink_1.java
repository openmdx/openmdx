/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: BinarySink_1.java,v 1.1 2005/10/09 12:55:43 hburger Exp $
 * Description: Large Objects: Binary Object 1.0 IIOP Implementation
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/10/09 12:55:43 $
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
package org.openmdx.base.stream.rmi.iiop;

import java.io.IOException;
import java.io.OutputStream;

import javax.rmi.PortableRemoteObject;

import org.openmdx.base.stream.rmi.cci.BinarySink_1_0;

/**
 * Binary Sink 1.0 IIOP Implementation
 */
public class BinarySink_1
    extends PortableRemoteObject
    implements BinarySink_1_0
{

    /**
     * 
     */
    protected OutputStream out;

    /**
     * Creates an <code>StreamSource_1</code> so that it uses <code>in</code>
     * as its source.
     *
     * @param   in
     *          the underlying binary stream.
     *
     * @exception   IOException
     *              if an I/O error occurs.
     */
    public BinarySink_1(
        OutputStream binaryStream
    ) throws IOException {
        super();
        this.out = binaryStream;
    }

    protected boolean isOpen(
    ){
        return this.out != null;
    }


    //------------------------------------------------------------------------
    // Implements BinarySource_1_0      
    //------------------------------------------------------------------------

    /**
     * Closes this iterator and releases any system resources associated with
     * it. 
     * <p>
     * No read() operation must be invoked after close().
     *
     * @exception   IOException
     *              if an I/O error occurs.
     */
    public void close(
    ) throws IOException {
        if(isOpen()) try {
            this.out.close();
        } finally {
            this.out = null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.stream.cci.rmi.BinarySink_1_0#writeBytes(byte[])
     */
    public void writeBytes(byte[] content) throws IOException {
        if(!isOpen()) throw new IOException("Stream not open");
        this.out.write(content);
        this.out.flush();
    }
    
}
