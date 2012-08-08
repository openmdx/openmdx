/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CharacterSink_1.java,v 1.1 2005/10/09 12:55:44 hburger Exp $
 * Description: Large Objects: Character Sink 1.0 JRMP Implementation
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/10/09 12:55:44 $
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
package org.openmdx.base.stream.rmi.jrmp;

import java.io.IOException;
import java.io.Writer;
import java.rmi.server.UnicastRemoteObject;

import org.openmdx.base.stream.rmi.cci.CharacterSink_1_0;

/**
 * Character Sink 1.0 JRMP Implementation
 */
public class CharacterSink_1
    extends UnicastRemoteObject
    implements CharacterSink_1_0
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3256446923500958514L;

    /**
     * 
     */
    protected Writer out;

    /**
     * Creates an <code>StreamSource_1</code> so that it uses <code>in</code>
     * as its source.
     *
     * @param   in
     *          the underlying character stream.
     *
     * @exception   IOException
     *              if an I/O error occurs.
     */
    public CharacterSink_1(
        Writer characterStream
    ) throws IOException {
        super();
        this.out = characterStream;
    }

    protected boolean isOpen(
    ){
        return this.out != null;
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
            this.out.close();
        } finally {
            this.out = null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.stream.cci.rmi.CharacterSink_1_0#writeCharacters(char[])
     */
    public void writeCharacters(char[] content) throws IOException {
        this.out.write(content);
        this.out.flush();
    }

}