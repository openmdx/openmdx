/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: String Sink 
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * A String Sink
 */
class StringSink extends ByteArrayOutputStream {
    
    /**
     * Constructor 
     *
     * @param encoder
     */
    StringSink(
        Charset charset
    ){
        this.charset = charset;
        this.target = new OutputStreamWriter(this, charset.newEncoder());
    }
    
    /**
     * The character set
     */
    private final Charset charset;
    
    /**
     * The target
     */
    private final Writer target;
    
    /**
     * The string to token-value cache
     */
    private final Map<String,Integer> cache = new HashMap<String,Integer>();
    
    /**
     * Retrieve the string table's content in order to flush it
     * 
     * @return the string table's content
     */
    ByteBuffer getStringTable(){
        return ByteBuffer.wrap(super.buf, 0, super.count);
    }
    
    /**
     * Determine the length of the string in bytes
     * 
     * @param value
     * 
     * @return the length of the string in bytes
     */
    private int countBytes(
        String value
    ){
        return this.charset.encode(value).remaining();
    }
    
    /**
     * Retrieve a string token 
     * 
     * @param value the string value
     * 
     * @return the existing string token
     */
    StringToken findStringToken(
        String value
    ){
        Integer index = this.cache.get(value);
        if(index == null) {
            Candidates: for(Map.Entry<String, Integer> candidate : cache.entrySet()) {
                if(candidate.getKey().endsWith(value)) {
                    index = Integer.valueOf(candidate.getValue().intValue() + countBytes(candidate.getKey()) - countBytes(value));
                    this.cache.put(value, index);
                    break Candidates;
                }
            }
        }
        return index == null ? null : new StringToken(index.intValue(), false);
    }
    
    /**
     * Retrieve or create a string token 
     * 
     * @param value the string value
     * 
     * @return the corresponding token
     */
    StringToken getStringToken(
        String value
    ){
        StringToken token = findStringToken(value);
        if(token == null) {
            try {
                int index = super.size();
                token = new StringToken(index, true);
                target.write(value);
                target.write('\u0000');
                target.flush();
                this.cache.put(value, Integer.valueOf(index));
                return token;
            } catch (IOException exception) {
                return null;
            }
        } else {
            return token;
        }
    }

    @Override
    public synchronized void reset(){
        super.reset();
        this.cache.clear();
    }
    
}