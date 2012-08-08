/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: BinaryLargeObjects.java,v 1.17 2010/03/02 18:27:36 hburger Exp $
 * Description: Binary Large Object Factory 
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/02 18:27:36 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2007, OMEX AG, Switzerland
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
package org.w3c.cci2;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Binary Large Object Factory
 */
public class BinaryLargeObjects {

    /**
     * Constructor
     */
    private BinaryLargeObjects() {
        // To avoid instantiation
    }

    /**
     * Default capacity for stream copy
     */
    private final static int CAPACITY = 10000;

    /**
     * Create a <code>BinaryLargeObject</code> facade for the given byte array
     * 
     * @param source
     * 
     * @return a <code>BinaryLargeObject</code> facade for the given byte array
     */
    public static BinaryLargeObject valueOf(
        byte[] source
    ){
        return new ArrayLargeObject(
            source,
            0,
            source.length
        );
    }

    /**
     * Create a <code>BinaryLargeObject</code> facade for the given byte array
     * 
     * @param source
     * @param offset
     * @param length
     * 
     * @return a <code>BinaryLargeObject</code> facade for the given byte array
     */
    public static BinaryLargeObject valueOf(
        byte[] source,
        int offset,
        int length
    ){
        return new ArrayLargeObject(
            source,
            offset,
            length
        );
    }

    /**
     * Create a <code>BinaryLargeObject</code> facade for the given URL
     * 
     * @param source
     * 
     * @return a <code>BinaryLargeObject</code> facade for the given URL
     */
    public static BinaryLargeObject valueOf(
        URL source
    ){
        return new URLLargeObject(source);
    }
    
    /**
     * Create a <code>BinaryLargeObject</code> facade for the given file
     * 
     * @param source
     * 
     * @return a <code>BinaryLargeObject</code> facade for the given file
     */
    public static BinaryLargeObject valueOf(
        File source
    ){
        return new FileLargeObject(source);
    }

    /**
     * Create a <code>BinaryLargeObject</code> facade for the given stream
     * 
     * @param source
     * 
     * @return a <code>BinaryLargeObject</code> facade for the given stream
     */
    public static BinaryLargeObject valueOf(
        InputStream source
    ){
        return new StreamLargeObject(source);
    }

    /**
     * Create a <code>BinaryLargeObject</code> facade for the given stream
     * 
     * @param source
     * @param length
     * 
     * @return a <code>BinaryLargeObject</code> facade for the given stream
     */
    public static BinaryLargeObject valueOf(
        InputStream source,
        Long length
    ){
        return new StreamLargeObject(source, length);
    }
    
    /**
     * A negative length is converted to <code>null</code>.
     * 
     * @param length
     * 
     * @return the length as object
     */
    public static Long asLength(
        long length
    ){
        return length < 0 ? null : Long.valueOf(length);
    }

    /**
     * Copy an input stream's content to an output stream
     * @param source
     * @param position
     * @param target
     * 
     * @return the number of byte written to the output stream
     * @throws IOException
     */
    public static long streamCopy(
        InputStream source,
        long position,
        OutputStream target
    ) throws IOException {
        byte[] buffer = new byte[CAPACITY];
        long count = 0l;
        if(position != 0l) {
            source.skip(position);
        }
        int n = source.read(buffer);
        while(n > 0) {
            count += n;
            target.write(buffer, 0, n);
            n = source.read(buffer);
        }
        return count;
    }

    
    //------------------------------------------------------------------------
    // Class ArrayLargeObject
    //------------------------------------------------------------------------

    /**
     * Array BLOB
     */
    private static class ArrayLargeObject implements BinaryLargeObject {

        /**
         * Constructor 
         *
         * @param value
         * @param offset
         * @param length
         */
        ArrayLargeObject(
            final byte[] value,
            final int offset,
            final int length
        ) {
            this.value = value;
            this.offset = offset;
            this.length = length;
        }

        private final byte[] value;
        private final int offset;
        private final int length;

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent() {
            return new ByteArrayInputStream(this.value, this.offset, this.length);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ){
            return Long.valueOf(this.length);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent(java.io.OutputStream, long)
         */
        public void getContent(
            OutputStream stream, 
            long position
        ) throws IOException {
            int offset = this.offset + (int)position;
            int length =  this.length - (int)position;
            if (length < 0) throw new EOFException(
                "Position " + position + " is larger than the objects length " + this.length
            );
            stream.write(this.value, offset, length);
        }

    }
       
    
    //------------------------------------------------------------------------
    // Class URLLargeObject
    //------------------------------------------------------------------------

    /**
     * URL BLOB
     */
    private static class URLLargeObject implements BinaryLargeObject {
        
        /**
         * Constructor 
         *
         * @param value
         */
        public URLLargeObject(final URL url) {
            this.url = url;
        }

        /**
         * 
         */
        private final URL url;

        /**
         * 
         */
        private transient URLConnection connection;
        
        /**
         * 
         */
        private transient Long length = null;
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent(
        ) throws IOException {
            return getConnection().getInputStream();
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ) throws IOException{
            return this.length == null ?
                this.length = asLength(getConnection().getContentLength()) :
                this.length;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent(java.io.OutputStream, long)
         */
        public void getContent(
            OutputStream stream, 
            long position
        ) throws IOException {
            this.length = position + streamCopy(
                getContent(),
                position,
                stream
            );
        }

        /**
         * Opens the URL connection
         * 
         * @return the URL connection
         * 
         * @throws IOException
         */
        protected URLConnection getConnection() throws IOException{
            return this.connection == null ?
                this.connection = url.openConnection() :
                this.connection;
        }
        
    }


    //------------------------------------------------------------------------
    // Class FileLargeObject
    //------------------------------------------------------------------------

    /**
     * File BLOB
     */
    private static class FileLargeObject implements BinaryLargeObject {
        
        /**
         * Constructor 
         *
         * @param value
         */
        public FileLargeObject(final File file) {
            this.file = file;
        }

        /**
         * 
         */
        private final File file;
        private transient Long length = null;

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent(
        ) throws IOException {
            return new FileInputStream(this.file);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ){
            return this.length == null ?
                this.length = asLength(this.file.length()) :
                this.length;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent(java.io.OutputStream, long)
         */
        public void getContent(
            OutputStream stream, 
            long position
        ) throws IOException {
            this.length = position + streamCopy(
                getContent(),
                position,
                stream
            );
        }

    }


    //------------------------------------------------------------------------
    // Class StreamLargeObject
    //------------------------------------------------------------------------

    /**
     * The underlying stream may be retrieved once only
     */
    public static class StreamLargeObject implements BinaryLargeObject {
        
        /**
         * Constructor 
         *
         * @param stream
         */
        public StreamLargeObject(
            final InputStream stream
        ){
            this(stream, getLength(stream));
        }

        /**
         * Constructor 
         *
         * @param stream
         * @param length
         */
        StreamLargeObject(
            final InputStream stream,
            final Long length
        ){
            this.stream = stream;
            this.length = length;
        }
        
        
        /**
         * 
         */
        private InputStream stream;

        /**
         * 
         */
        private Long length;

        /**
         * Determine the large objetc's size
         * 
         * @param stream
         * 
         * @return the large objetc's size
         */
        private static Long getLength(
            InputStream stream
        ){
            try {
                return stream instanceof ByteArrayInputStream ? Long.valueOf(stream.available()) : null;
            } catch (IOException exception) {
                return null;
            }
        }

        /**
         * Provide the input stream content
         * 
         * @return the content
         * 
         * @throws IOException
         */
        protected InputStream newContent(
        ) throws IOException {
            throw new IllegalStateException("The content may be retrieved once only");
        }
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent(
        ) throws IOException {
            if(this.stream == null) {
                return newContent();
            } else {
                InputStream stream = this.stream;
                this.stream = null;
                return stream;
            }
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent(java.io.OutputStream, long)
         */
        public void getContent(
            OutputStream stream, 
            long position
        ) throws IOException {
            Long length = position + streamCopy(
                getContent(),
                position,
                stream
            );
            if(this.length == null) {
                this.length = length;
            }
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ) throws IOException {
            return this.length;
        }
        
    }
       
}