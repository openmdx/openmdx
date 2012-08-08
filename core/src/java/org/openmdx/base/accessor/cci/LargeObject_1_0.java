/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LargeObject_1_0.java,v 1.1 2009/01/09 01:08:38 wfro Exp $
 * Description: Large Object: Stream accessors
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/09 01:08:38 $
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
package org.openmdx.base.accessor.cci;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.openmdx.base.exception.ServiceException;


/**
 * Large Object: Stream accessors
 */
public interface LargeObject_1_0 {
    /**
     * Get the size of this object.
     * 
     * @return the object's size or -1 if its size is unknown.
     *
     * @exception   ServiceException
     *              in case of failure
     */
    long length(
    ) throws ServiceException;


    //------------------------------------------------------------------------
    // Binary Large Object (BLOB)
    //------------------------------------------------------------------------

    /**
     * Updates all or part of the BLOB value that this Blob object 
     * represents, as an array of bytes. This byte array contains up to 
     * length consecutive bytes starting at the specified position.
     * 
     * @param   position
     *          the ordinal position of the first byte in the BLOB value to 
     *          be extracted; the first byte is at position 0
     * @param   capacity
     *          the number of consecutive bytes to be copied 
     * 
     * @return  a byte array containing up to capacity consecutive bytes from
     *          the BLOB value, starting with the byte at the specified
     *          position
     *
     * @exception   IOException
     *              if an I/O error occurs.
     */
    byte[] getBytes(
        long position,
        int capacity
    ) throws ServiceException;

    /**
     * Retrieves a large object's content.
     * </ul>
     * @return  an input stream for the content
     * 
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is dirty
     * @exception   ServiceException NOT_AVAILABLE
     *              if no additional stream can be acquired through this
     *              BLOB accessor instance.
     */
    InputStream getBinaryStream(
    ) throws ServiceException;

    /**
     * Set an output stream to retrieves a large object's content.
     * </ul>
     * @param       stream
     *              an output stream receiving the object's content
     * @param       position
     *              index into the large object's content
     * 
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is dirty
     * @exception   ServiceException NOT_AVAILABLE
     *              if no additional stream can be acquired through this
     *              BLOB accessor instance.
     */
    void getBinaryStream(
        OutputStream stream,
        long position
    ) throws ServiceException;


    //------------------------------------------------------------------------
    // Character Large Object (CLOB)
    //------------------------------------------------------------------------

    /**
     * Retrieves a copy of the specified substring in the CLOB value 
     * designated by this CLOB object. The substring begins at the 
     * specified position and has up to capaciy consecutive characters. 
     * 
     * @param   position
     *          the first character of the substring to be extracted. The
     *          first character is at position 0.
     * @param   capacity
     *          the number of consecutive characters to be copied 
     * 
     * @return  a character array containing up to capacity consecutive
     *          characters from the CLOB, starting with the character at
     *          the specified position
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is dirty
     */
    char[] getCharacters(
        long position,
        int capacity
    ) throws ServiceException;

    /**
     * Retrieves a large object's content.
     * </ul>
     * @return  a reader for the content
     * 
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is dirty
     * @exception   ServiceException NOT_AVAILABLE
     *              if no additional stream can be acquired through this
     *              CLOB accessor instance.
     */
    Reader getCharacterStream(
    ) throws ServiceException;

    /**
     * Set an output stream to retrieves a large object's content.
     * </ul>
     * @param       writer
     *              a writer receiving the object's content
     * @param       position
     *              index into the large object's content
     * 
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is dirty
     * @exception   ServiceException NOT_AVAILABLE
     *              if no additional stream can be acquired through this
     *              CLOB accessor instance.
     */
    void getCharacterStream(
        Writer writer,
        long position
    ) throws ServiceException;

    /**
     * Truncates the LOB value that this WritableLargeObject represents to 
     * have the specified length.
     * 
     * @param length
     *        the length, to which the LOB value should be truncated
     * 
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object is not partially updateable.
     */
    void truncate (
        long length
    ) throws ServiceException;
        
        
    //------------------------------------------------------------------------
    // Binary Large Object (BLOB)
    //------------------------------------------------------------------------

    /**
     * Updates all or part of the BLOB value that this WritableLargeObject 
     * represents.
     * 
     * @param   position
     *          the first character of the substring to be extracted. The
     *          first character is at position 0.
     * @param   content
     *          the (partial) content to be put at the specified position 
     * 
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object is not partially updateable.
     */
    void setBytes(
        long position,
        byte[] content
    ) throws ServiceException;

    /**
     * Replaces a large object's content.
     * </ul>
     * @param   stream
     *          an InputStream for the new content
     * @param   size
     *          the object's size or -1 if it is unknown
     * 
     * @exception   ServiceException NOT_SUPPORTED
     *              if size is -1 but required by the underlaying implementation.
     */
    void setBinaryStream(
        InputStream stream,
        long size
    ) throws ServiceException;

    /**
     * Retrieves a stream that can be used to write to the BLOB value that
     * this WritableLargeObject object represents. 
     * <p>
     * The stream begins atthe specified position. 
     * 
     * @param   position
     *          the position in the BLOB value at which to start writing
     * 
     * @return  a java.io.OutputStream object to which data can be written 
     *
     * @exception   ServiceException    NOT_SUPPORTED
     *              if the object is not partially updateable.
     */
    OutputStream setBinaryStream(
        long position
    ) throws ServiceException;


    //------------------------------------------------------------------------
    // Character Large Object (CLOB)
    //------------------------------------------------------------------------

    /**
     * Updates all or part of the CLOB value that this WritableLargeObject 
     * represents.
     * 
     * @param   position
     *          the first character of the substring to be extracted. The
     *          first character is at position 0.
     * @param   content
     *          the (partial) content to be put at the specified position 
     * 
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object is either read-only or not partially 
     *              updateable.
     */
    void setCharacters(
        long position,
        char[] content
    ) throws ServiceException;

    /**
     * Replaces a large object's content.
     * </ul>
     * @param   stream
     *          a Reader for the new content
     * @param   size
     *          the object's size or -1 if it is unknown
     *
     * @exception   ServiceException    NOT_SUPPORTED
     *              if the object is read-only
     */
    void setCharacterStream(
        Reader stream,
        long size
    ) throws ServiceException;

    /**
     * Retrieves a stream that can be used to write to the CLOB value that
     * this WritableLargeObject represents. 
     * <p>
     * The stream begins atthe specified position. 
     * 
     * @param   position
     *          the position in the BLOB value at which to start writing
     * 
     * @return  a java.io.Writer object to which data can be written 
     *
     * @exception   ServiceException    NOT_SUPPORTED
     *              if the object is not partially updateable.
     */
    Writer setCharacterStream(
        long position
    ) throws ServiceException;
    
}
