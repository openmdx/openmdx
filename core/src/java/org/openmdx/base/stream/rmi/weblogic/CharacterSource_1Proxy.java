/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CharacterSource_1Proxy.java,v 1.2 2005/10/09 08:25:51 hburger Exp $
 * Description: Large Objects: Character Source 1.0 WebLogic Implementation
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/10/09 08:25:51 $
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
package org.openmdx.base.stream.rmi.weblogic;

import java.io.IOException;
import org.openmdx.base.stream.rmi.cci.CharacterSource_1_0;

/**
 * Character Source 1.0 WebLogic Implementation
 */
public class CharacterSource_1Proxy
    implements CharacterSource_1_0
{

    /**
     * 
     */
    protected CharacterSource_1_0 delegate;
    

    /**
     * Creates an <code>StreamSource_1</code> so that it uses <code>in</code>
     * as its source.
     *
     * @param   delegate
     *          the underlying character stream.
     *
     * @exception   IOException
     *              if an I/O error occurs.
     */
    public CharacterSource_1Proxy(
        CharacterSource_1_0 delegate
    ) throws IOException {
        super();
        this.delegate = delegate;
    }

    protected boolean isOpen(
    ){
        return this.delegate != null;
    }
    
    
    //------------------------------------------------------------------------
    // Implements Source_1_0       
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
            this.delegate.close();
        } finally {
            this.delegate = null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.stream.cci.Source_1_0#length()
     */
    public long length() throws IOException {
        if(!isOpen()) throw new IOException("Stream not open");
        return this.delegate.length();
    }
    
    
    //------------------------------------------------------------------------
    // Implements CharacterSource_1_0       
    //------------------------------------------------------------------------

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
        return this.delegate.readCharacters(capacity);
    }


}
