/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Plug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * The standard plug-in supports <em>normal</em> WBXML string tables
 */
public class StandardPlugIn extends AbstractPlugIn {
    
    /**
     * Constructor 
     */
    protected StandardPlugIn(
        String stringEncoding
    ){
        Charset charset = Charset.forName(stringEncoding);
        this.stringSink = new StringSink(charset.newEncoder());
        this.decoder =  charset.newDecoder();
    }

    /**
     * The re-usable decoder
     */
    private final CharsetDecoder decoder;
    
    /**
     * The string source
     */
    private StringSource stringSource;

    /**
     * The string sink
     */
    private final StringSink stringSink;

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.AbstractPlugIn#reset()
     */
    @Override
    public void reset() {
        super.reset();
        this.stringSource = null;
        this.stringSink.reset();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#setStringTable(byte[])ic
     */
    @Override
    public void setStringTable(
        ByteBuffer stringTable
    ) throws ServiceException{
        this.stringSource = new StringSource(
            this.decoder, stringTable
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#getStringTable()
     */
    @Override
    public ByteBuffer getStringTable() {
        return this.stringSink.getStringTable();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#getStringIndex(java.lang.String)
     */
    @Override
    public StringToken getStringToken(
        String value
    ) {
        return this.stringSink == null ? null : this.stringSink.getStringToken(value);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#resolveString(int)
     */
    @Override
    public CharSequence resolveString(
        int index
    ) throws ServiceException {
        CharSequence literal = this.stringSource == null ? null : this.stringSource.resolveString(index);
        if(literal != null) {
            return literal;
        } else throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "No such string table entry",
            new BasicException.Parameter("index", index)
        );
    }
    
    
    //------------------------------------------------------------------------
    // Class StringSink
    //------------------------------------------------------------------------

    /**
     * A String Sink
     */
    static class StringSink extends ByteArrayOutputStream {
        
        /**
         * Constructor 
         *
         * @param encoder
         */
        StringSink(
            CharsetEncoder encoder
        ){
            this.target = new OutputStreamWriter(this, encoder);
        }
        
        /**
         * The str
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
         * Retrieve or create a string token 
         * 
         * @param value the string value
         * 
         * @return the corresponding token
         */
        StringToken getStringToken(
            String value
        ){
            Integer index = this.cache.get(value);
            if(index != null) {
                return new StringToken(index.intValue(), false);
            }
            try {
                StringToken token = new StringToken(super.size(), true);
                target.write(value);
                target.write('\u0000');
                target.flush();
                return token;
            } catch (IOException exception) {
                return null;
            }
        }
     
        @Override
        public void reset(){
            super.reset();
            this.cache.clear();
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class StringSource
    //------------------------------------------------------------------------
    
    /**
     * String Source
     */
    static class StringSource extends ByteArrayInputStream {

        /**
         * Constructor 
         *
         * @param encoding
         * @param bytes
         */
        protected StringSource(
            CharsetDecoder decoder,
            ByteBuffer stringTable
        ) {
            super(stringTable.array(), stringTable.arrayOffset(), stringTable.remaining());
            this.decoder = decoder;
        }
        
        /**
         * The string source's decoder
         */
        private final CharsetDecoder decoder;
        
        /**
         * Resolve a string table entry
         * 
         * @param index the index into the string table
         * 
         * @return the corresponding value
         */
        CharSequence resolveString(
            int index
        ){
            if(index >= super.count) {
                return null;
            } else try {
                super.pos = index;
                this.decoder.reset();
                Reader source = new InputStreamReader(this, this.decoder);
                StringBuilder target = new StringBuilder();
                for(int utf16; (utf16 = source.read()) > 0;) {
                    target.append((char)utf16);
                }
                return target;
            } catch (IOException exception) {
                return null;
            }
        }
        
    }
    
}
