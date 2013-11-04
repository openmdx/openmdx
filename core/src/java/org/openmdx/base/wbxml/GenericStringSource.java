/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Generic String Source 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
package org.openmdx.base.wbxml;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.HashMap;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;

/**
 * Generic String Source
 */
public class GenericStringSource implements StringSource {

    /**
     * Constructor 
     *
     * @param charset the encoding
     * @param in the string table content
     * @param byteCount the string table size
     * @throws ServiceException 
     */
    GenericStringSource(
        Charset charset,
        InputStream in,
        int byteCount
    ) throws IOException {
        this.decoder = charset.newDecoder();
        this.binaryContent = new byte[byteCount];
        this.characterContent = new HashMap<Integer, String>();
        populate(in, byteCount);
    }

    /**
     * The (re-usable) decoder
     */
    protected final CharsetDecoder decoder;
    
    /**
     * The string table's binary content is retrieved at the beginning
     */
    protected final byte[] binaryContent;
    
    /**
     * The string table's character content is amended over time
     */
    protected final Map<Integer,String> characterContent;

    /**
     * To decode slices
     */
    private final CharBuffer sliceBuffer = CharBuffer.allocate(64);
    
    /**
     * To compose slices
     */
    private final StringBuilder valueBuffer = new StringBuilder();
    
    /**
     * Populate the string source
     * 
     * @param in
     * @param byteCount
     * 
     * @throws IOException
     * @throws EOFException
     */
    private void populate(
        InputStream in, 
        int byteCount
    ) throws IOException {
        int read = 0;
        while(read < byteCount) {
            int batchSize = in.read(this.binaryContent, read, byteCount - read);
            if(batchSize < 0) {
                throw new EOFException(
                    "Missing bytes for string table, expected " + byteCount + "bytes, found " + read
                    );
            }
            read += batchSize;
        }
    }

    /**
     * Retrieve a string from the content
     * 
     * @param index
     * 
     * @return a string from the content
     */
    private String getString(
        int index
    ){
        this.decoder.reset();
        ByteBuffer binaryBuffer = ByteBuffer.wrap(this.binaryContent, index, this.binaryContent.length - index);
        this.valueBuffer.setLength(0);
        while (true){
            this.sliceBuffer.clear();
            CoderResult result = decoder.decode(binaryBuffer, this.sliceBuffer, true);
            if(result.isError()){
                throw new RuntimeException("Decoding failure: " + result);
            }
            this.sliceBuffer.flip();
            for(int length = 0, limit = this.sliceBuffer.remaining(); length < limit; length++) {
                if(sliceBuffer.get(length) == '\0'){
                    CharSequence slice = this.sliceBuffer.subSequence(0, length);
                    return (
                        valueBuffer.length() == 0 ? slice : valueBuffer.append(slice)
                    ).toString();
                }
            }
            valueBuffer.append(this.sliceBuffer);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.StringSource#resolveString(int)
     */
    @Override
    public String resolveString(int index) {
        if(index >= this.binaryContent.length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        Integer key = Integer.valueOf(index);
        String value = this.characterContent.get(key);
        if(value == null) {
            this.characterContent.put(
               key, 
               value = getString(index)
            );
        }
        return value;
    }

}
