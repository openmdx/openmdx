/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CharacterSource_1.java,v 1.1 2005/10/09 12:55:42 hburger Exp $
 * Description: Large Objects: Character Source 1.0 IIOP Implementation
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/10/09 12:55:42 $
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
import java.io.Reader;

import javax.rmi.PortableRemoteObject;

import org.openmdx.base.stream.rmi.cci.CharacterSource_1_0;

/**
 * Character Source 1.0 IIOP Implementation
 */
public class CharacterSource_1
    extends PortableRemoteObject
    implements CharacterSource_1_0
{

    /**
     * 
     */
    protected Reader in;

    /**
     * 
     */
    private long length;

    /**
     * 
     */
    private long count = 0L;

    /**
     * Creates an <code>StreamSource_1</code> so that it uses <code>in</code>
     * as its source.
     *
     * @param   delegate
     *          the underlying character stream.
     * @param   length
     *          The number of bytes in the stream or -1 if it is unknown.
     *
     * @exception   IOException
     *              if an I/O error occurs.
     */
    public CharacterSource_1(
        Reader characterStream,
        long length
    ) throws IOException {
        super();
        this.in = characterStream;
        this.length = length;
    }

    /**
     * Creates an <code>StreamSource_1</code> so that it uses <code>in</code>
     * as its source.
     *
     * @param   delegate
     *          the underlying character stream.
     * @param   length
     *          The number of bytes in the stream or -1 if it is unknown.
     *
     * @exception   IOException
     *              if an I/O error occurs.
     */
    public CharacterSource_1(
        Reader characterStream
    ) throws IOException {
        this(characterStream, -1L);
    }

    private char[] trim(
        char[] source,
        int length
    ){
        char[] target = new char[length];
        System.arraycopy(source, 0, target, 0, length);
        return target;
    }

    protected boolean isOpen(
    ){
        return this.in != null;
    }
    
    
    //------------------------------------------------------------------------
    // Implements CharacterSource_1_0       
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
            this.in.close();
        } finally {
            this.in = null;
        }
    }

    /**
     * Retrieves a copy of the specified substring in the CLOB value 
     * designated by this CLOB object. The substring begins at the 
     * specified position and has up to capaciy consecutive characters. 
     * 
     * @param   capacity
     *          the number of consecutive characters to be copied 
     * 
     * @return  a character array containing up to capacity consecutive
     *          characters from the CLOB;
     *          or null in case of an end of stream condition
     *
     * @exception   IOException
     *              if an I/O error occurs.
     */
    public char[] readCharacters(
        int capacity
    ) throws IOException {
        if(!isOpen()) throw new IOException("Stream not open");
        char[] buffer = new char[capacity];
        int available = this.in.read(buffer);
        if(available == -1) {
            this.length = this.count;
            return null;
        } else {
            this.count += available;
            return available == buffer.length ? buffer : trim(buffer,available);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.stream.cci.Source_1_0#length()
     */
    public long length() throws IOException {
        return this.length;
    }

}
