/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Reader InputStream
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2010, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.tools.ant.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Reader InputStream
 */
public class ReaderInputStream extends InputStream {

    /**
     * Constructor
     * 
     * @param buffer
     */
    private ReaderInputStream(
        Buffer buffer
    ){
        this.buffer = buffer;       
    }
    
    /**
     * Constructor
     * 
     * @param in
     * @param encoding
     *
     * @throws UnsupportedEncodingException 
     */
    public ReaderInputStream(
        Reader in,
        String encoding
    ) throws UnsupportedEncodingException {
        this(new Buffer(in, encoding));
    }

    /**
     * Constructor
     * 
     * @param in
     */
    public ReaderInputStream(
        Reader in
    ){
        this(new Buffer(in));
    }

    /**
     * 
     */
    private final Buffer buffer;
    
    /**
     * 
     */
    private byte[] binaryBuf = new byte[1];
    
    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    @Override
	public synchronized int read(
    ) throws IOException {
        return this.buffer.read(this.binaryBuf, 0, 1) == 0 ? -1 : this.binaryBuf[0];
    }
    
    /* (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (b == null) throw new NullPointerException();
        if (
            off < 0 || 
            off > b.length || 
            len < 0 ||
            off + len > b.length || 
            off + len < 0 
        ) throw new IndexOutOfBoundsException();
        if (len == 0) return 0;
        return this.buffer.read(b, off, len);
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#available()
     */
    @Override
	public synchronized int available() throws IOException {
        return this.buffer.available();
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    @Override
	public void close() throws IOException {
        this.buffer.close();
    }


    /**
     * Insert the given text into the reader input stream's buffer.
     * 
     * @param text
     * 
     * @throws IOException
     */
    public void insert(
    	String text
    ) throws IOException{
    	this.buffer.write(text);
    }
    
    
    //------------------------------------------------------------------------
    // Class Buffer
    //------------------------------------------------------------------------
    
    /**
     * 
     */
    static class Buffer extends OutputStream {

        /**
         * Constructor
         * 
         * @param source
         * @param encoding
         *
         * @throws UnsupportedEncodingException 
         */
        Buffer(
            Reader source,
            String encoding
        ) throws UnsupportedEncodingException{
            this.textSource = source;
            this.textSink = new OutputStreamWriter(this, encoding);
        }

        /**
         * Constructor
         * 
         * @param source
         */
        Buffer(
            Reader source
        ){
            this.textSource = source;
            this.textSink = new OutputStreamWriter(this);
        }
        
        protected Writer textSink;
        protected Reader textSource;
        private char[] textBuf = new char[8];

        private byte[] binaryBuf = new byte[1];
        
        private byte[] primaryBuf;
        private int primaryCount;
        private int primaryPos;
        private int primaryLength;
                
        private byte[] alternateBuf = new byte[]{};
        private int alternateCount = 0;
        private int alternatPos = 0;
        
        int available(
        ) throws IOException {
            return this.alternateCount - this.alternatPos;
        }
        
        void write(
        	String text
        ) throws IOException{
            this.primaryBuf = new byte[]{};
            this.primaryCount = 0;
            this.primaryPos = 0;
            this.primaryLength = 0;
            this.textSink.write(text);
        }
        
        int read(
            byte[] buf,
            int offset,
            int length
        ) throws IOException {
            int available = this.alternateCount - this.alternatPos;
            if(available > 0) {
                if(available > length) {
                    System.arraycopy(this.alternateBuf, this.alternatPos, buf, offset, length);
                    this.alternatPos += available;
                    return length;
                } else {
                    System.arraycopy(this.alternateBuf, this.alternatPos, buf, offset, available);
                    this.alternatPos = 0;
                    this.alternateCount = 0;
                    return available;
                }
            } else {
                this.primaryBuf = buf;
                this.primaryCount = offset;
                this.primaryPos = offset;
                this.primaryLength = offset + length;
                if(this.textBuf.length < length) this.textBuf = new char[
                    Math.max(2 * this.textBuf.length, length)
                ];
                int textCount = this.textSource.read(this.textBuf);
                if(textCount < 0) return -1;
                this.textSink.write(this.textBuf, 0, textCount);
                this.textSink.flush();
                this.primaryBuf = null;
                return this.primaryCount - this.primaryPos;
            }
        }

        /* (non-Javadoc)
         * @see java.io.OutputStream#write(int)
         */
        @Override
		public void write(int b) throws IOException {
            this.binaryBuf[0] = (byte) b;
            write(this.binaryBuf, 0, 1);
        }

        /* (non-Javadoc)
         * @see java.io.OutputStream#write(byte[], int, int)
         */
        @Override
		public void write(byte[] b, int off, int len) throws IOException {
            int primaryAvailable = this.primaryLength - this.primaryCount;
            if(primaryAvailable > len) {
                System.arraycopy(b, off, this.primaryBuf, this.primaryCount, len);
                this.primaryCount += len;
            } else {
                System.arraycopy(b, off, this.primaryBuf, this.primaryCount, primaryAvailable);
                this.primaryCount = this.primaryLength;
                int alternateLength = len - primaryAvailable;
                int alternateAvailable = this.alternateBuf.length - this.alternateCount;
                if(alternateAvailable < alternateLength) {
                    byte[] alternateBuf = new byte[
                        Math.max(2 * this.alternateBuf.length, alternateLength + this.alternateCount)
                    ];
                    System.arraycopy(this.alternateBuf, 0, alternateBuf, 0, this.alternateCount);
                    this.alternateBuf = alternateBuf;
                }
                System.arraycopy(b, off + primaryAvailable, this.alternateBuf, this.alternateCount, alternateLength);
                this.alternateCount += alternateLength;     
            }
            
        }

        /* (non-Javadoc)
         * @see java.io.OutputStream#close()
         */
        @Override
		public void close() throws IOException {
            if(this.textSource != null) this.textSource.close();
            this.textSource = null;
            this.textSink = null;
            this.alternateBuf = null;
        }       
        
    }

}
