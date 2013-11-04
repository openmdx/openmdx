/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Binary Large Object Factory 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2013, OMEX AG, Switzerland
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
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
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
     * Create a <code>BinaryLargeObject</code> copy of the given stream
     * 
     * @param source
     * @param length
     * 
     * @return a <code>BinaryLargeObject</code> copy of the given stream
     * 
     * @throws IOException  
     */
    public static BinaryLargeObject copyOf(
        InputStream source,
        Long length
    ) throws IOException {
        ByteArrayOutputStream buffer = length == null ? new ByteArrayOutputStream(
        ) : new ByteArrayOutputStream(
            length.intValue()
        );
        streamCopy(source, 0, buffer);
        return valueOf(buffer.toByteArray());
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
        if(position != 0l) {
            source.skip(position);
        }
        long count = 0l;
        for(int n; (n = source.read(buffer)) >= 0;){
            count += n;
            target.write(buffer, 0, n);
        }
        return count;
    }

    
    //------------------------------------------------------------------------
    // Class ArrayLargeObject
    //------------------------------------------------------------------------

    /**
     * Array BLOB
     */
    private static class ArrayLargeObject implements BinaryLargeObject, Serializable {

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

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 3886030128086687249L;

        private byte[] value;
        private int offset;
        private int length;

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

        private void writeObject(
            ObjectOutputStream stream
        ) throws IOException {
            stream.writeLong(this.length);
            stream.write(this.value, this.offset, this.length);
        }
        
        private void readObject(
            ObjectInputStream stream
        ) throws IOException, ClassNotFoundException {
            this.offset = 0;
            this.length = (int)stream.readLong();
            this.value = new byte[this.length];
            stream.read(this.value);
        }

    }
    
    
    //------------------------------------------------------------------------
    // Class AbstractLargeObject
    //------------------------------------------------------------------------

    /**
     * The underlying stream may be retrieved once only
     */
    private static abstract class AbstractLargeObject implements BinaryLargeObject, Serializable {
        
        /**
         * Constructor 
         *
         * @param stream
         */
        protected AbstractLargeObject(
            Long length
        ){
            this.length = length;
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -5837819381102619869L;

        private transient byte[] value;
        private transient Long length;

        /**
         * Provide the input stream content
         * 
         * @return the content
         * 
         * @throws IOException
         */
        protected InputStream newContent(
        ) throws IOException {
            return this.value == null ? null : new ByteArrayInputStream(this.value);
        }
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent(java.io.OutputStream, long)
         */
        public void getContent(
            OutputStream stream, 
            long position
        ) throws IOException {
            Long length = Long.valueOf(
                position + streamCopy(
                    getContent(),
                    position,
                    stream
                )
            );
            this.length = length;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ) throws IOException {
            return this.length;
        }
        
        protected void setLength(
            Long length
        ){
            this.length = length;
        }
        
        private void writeObject(
            ObjectOutputStream stream
        ) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            getContent(buffer, 0);
            stream.writeLong(this.length.longValue());
            buffer.writeTo(stream);
        }
        
        private void readObject(
            ObjectInputStream stream
        ) throws IOException, ClassNotFoundException {
            this.length = Long.valueOf(stream.readLong());
            this.value = new byte[this.length.intValue()];
            stream.read(this.value);
        }
        
    }
    
     
    //------------------------------------------------------------------------
    // Class URLLargeObject
    //------------------------------------------------------------------------

    /**
     * URL BLOB
     */
    private static class URLLargeObject extends AbstractLargeObject {
        
        /**
         * Constructor 
         *
         * @param url
         */
        public URLLargeObject(final URL url) {
            super(null);
            this.url = url;
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 3916312441998769493L;

        /**
         * 
         */
        private transient URL url;

        /**
         * 
         */
        private transient URLConnection connection;
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent(
        ) throws IOException {
            return this.url != null ? getConnection().getInputStream() : newContent();
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        @Override
        public Long getLength(
        ) throws IOException {
            Long length = super.getLength();
            if(length == null) {
                super.setLength(
                    length = asLength(getConnection().getContentLength())
                );
            }
            return length;
        }

        /**
         * Opens the URL connection
         * 
         * @return the URL connection
         * 
         * @throws IOException
         */
        protected URLConnection getConnection() throws IOException{
            if(this.connection == null) {
                this.connection = url.openConnection();
            }
            return this.connection;
        }
        
    }


    //------------------------------------------------------------------------
    // Class FileLargeObject
    //------------------------------------------------------------------------

    /**
     * File BLOB
     */
    private static class FileLargeObject extends AbstractLargeObject {
        
        /**
         * Constructor 
         *
         * @param file
         */
        public FileLargeObject(
            final File file
        ) {
            super(asLength(file.length()));
            this.file = file;
        }

        private transient File file;

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -1566865189136279414L;

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        @SuppressWarnings("resource")
        public InputStream getContent(
        ) throws IOException {
            return this.file != null ? new FileInputStream(this.file) : newContent();
        }

    }


    //------------------------------------------------------------------------
    // Class StreamLargeObject
    //------------------------------------------------------------------------

    /**
     * The underlying stream may be retrieved once only
     */
    public static class StreamLargeObject extends AbstractLargeObject {
        
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
        protected StreamLargeObject(
            final InputStream stream,
            final Long length
        ){
            super(length);
            this.stream = stream;
        }
        
        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -6473575658856881436L;

        /**
         * The stream may be read pnce only
         */
        private transient InputStream stream;

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
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent(
        ) throws IOException {
            InputStream content;
            if(this.stream == null) {
                content = newContent();
            } else {
                content = this.stream;
                this.stream = null;
            }
            if(content != null) {
                return content;
            }
            throw new IllegalStateException(
                "The stream may be  retrieved once only"
            );
        }

    }
       
}