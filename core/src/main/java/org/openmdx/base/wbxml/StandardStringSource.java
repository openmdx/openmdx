/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard String Source 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;

/**
 * Standard String Source
 */
public class StandardStringSource implements StringSource {

    /**
     * Constructor 
     *
     * @param charset the encoding
     * @param in the string table content
     * @param byteCount the string table size
     * @throws ServiceException 
     */
    StandardStringSource(
        Charset charset,
        InputStream in,
        int byteCount
    ) throws IOException {
        if(!isSupported(charset)) throw new UnsupportedEncodingException(
            "The StandardStringSource supports the following character sets only: " + SUPPORTED_CHARACTER_SETS
        );
        this.charset = charset;
        this.content = new TreeMap<Integer, String>();
        populate(in, byteCount);
    }

    /**
     * The character set to be used
     */
    private final Charset charset;
    
    /**
     * The slice size
     */
    private final static int SLICE_SIZE = 1024;
        
    /**
     * The string table's character content is amended over time
     */
    protected final SortedMap<Integer,String> content;
    
    /**
     * The supported character sets
     */
    protected final static List<String> SUPPORTED_CHARACTER_SETS = Arrays.asList(
        "UTF-8",
        "ASCII"
    );

    /**
     * Tells whether the given character set is supported
     * 
     * @param charset
     * 
     * @return {@code true} if the given character set is supported 
     */
    public static boolean isSupported(
        Charset charset
    ){
        return SUPPORTED_CHARACTER_SETS.contains(charset.name());
    }
    
    /**
     * Populate the string table with its basic entries
     * 
     * @throws IOException  
     */
    private void populate(
        InputStream in,
        int tableSize 
    ) throws IOException {
        final CharsetDecoder decoder = charset.newDecoder();
        final byte[] binaryData = new byte[SLICE_SIZE];
        final char[] characterData = new char[SLICE_SIZE]; 
        final CharBuffer characterBuffer = CharBuffer.wrap(characterData);
        ByteBuffer binaryBuffer = ByteBuffer.wrap(binaryData);
        final StringBuilder value = new StringBuilder();
        int tableIndex = 0;
        int valueStart = 0;
        int valueSize = 0;
        boolean ascii = true;
        while(tableSize > tableIndex){
            int remaining = tableSize - tableIndex; 
            int sliceLimit = in.read(binaryData, 0, remaining > SLICE_SIZE ? SLICE_SIZE : remaining);
            if(sliceLimit < 0) {
                throw new EOFException(
                    "Missing bytes for string table, expected " + tableSize + "bytes, found " + tableIndex
                );
            }
            int sliceStart = 0;
            for(int sliceIndex = 0; sliceIndex < sliceLimit; sliceIndex++) {
                int character = binaryData[sliceIndex] & 0x000000ff;
                if(character == 0) { // end of value
                    Integer key = Integer.valueOf(valueStart);
                    int sliceSize = sliceIndex - sliceStart;
                    if(ascii && value.length() == 0) {
                        this.content.put(key, String.copyValueOf(characterData, sliceStart, sliceSize));
                    } else {
                        if(ascii) {
                            value.append(characterData, sliceStart, sliceSize);
                        } else {
                            decoder.reset().decode(
                                (ByteBuffer)binaryBuffer.position(sliceStart).limit(sliceIndex),
                                (CharBuffer) characterBuffer.reset(), 
                                true
                            );
                            value.append(characterBuffer.flip());
                        }
                        content.put(key, value.toString());
                    }
                    valueSize += sliceSize;
                    sliceStart = sliceIndex + 1;
                    value.setLength(0);
                    valueStart += valueSize + 1;
                    valueSize = 0;
                } else if (character < 128) { // stay in ASCII mode
                    characterData[sliceIndex] = (char) character;
                } else if (ascii){ // Switch to non-ASCII
                    int sliceSize = sliceIndex - sliceStart;
                    value.append(characterData, sliceStart, sliceSize);
                    valueSize += sliceSize;
                    sliceStart = sliceIndex;
                    ascii = false;
                }
            }
            {
                int sliceSize = sliceLimit - sliceStart;
                if(ascii) {
                    value.append(characterData, sliceStart, sliceSize);
                } else {
                    decoder.decode(
                        (ByteBuffer) binaryBuffer.position(sliceStart).limit(sliceLimit), 
                        (CharBuffer) characterBuffer.reset(), 
                        false
                    );
                    value.append(characterBuffer.flip());
                }
                valueSize += sliceSize;
            }
            tableIndex += sliceLimit;
        }
        SysLog.detail("WBXML String table loaded", tableSize + " bytes");
    }

    /**
     * Retrieve a sub-string
     * 
     * @param index
     * 
     * @return the requested sub-string
     */
    private String getSubString(
        Integer index
    ){
        Integer superKey = this.content.headMap(index).lastKey();
        String superValue = this.content.get(superKey);
        int offset = index.intValue() - superKey.intValue();
        for(int i = 0; i < offset; i++) {
           if(superValue.charAt(i) >= 128) {
               // non-ASCII prefix
               byte[] binaryContent = superValue.getBytes(this.charset);
               return this.charset.decode(ByteBuffer.wrap(binaryContent, offset, binaryContent.length - offset)).toString();
           }
        }
        // ASCII prefix
        return superValue.substring(offset);
    }
    
    
    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.StringSource#resolveString(int)
     */
    @Override
    public String resolveString(int index) {
        Integer key = Integer.valueOf(index);
        String value = this.content.get(key);
        if(value == null) {
            this.content.put(
               key, 
               value = getSubString(key)
            );
        }
        return value;
    }

}
