/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Object Relational Mapping 
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
package org.w3c.cci2;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

/**
 * Object Relational Mapping
 */
public class CharacterLargeObjects {

    /**
     * Constructor
     */
    private CharacterLargeObjects() {
        // To avoid instantiation
    }

    /**
     * Default capacity for stream copy
     */
    private final static int CAPACITY = 10000;

    /**
     * Create a {@code CharacterLargeObject} facade for the given string
     * 
     * @param source
     * 
     * @return a {@code CharacterLargeObject} facade for the given string
     */
    public static CharacterLargeObject valueOf(
        String source
    ){
        return new StringLargeObject(source);
    }

    /**
     * Create a {@code CharacterLargeObject} facade for the given byte array
     * 
     * @param source
     * 
     * @return a {@code CharacterLargeObject} facade for the given byte array
     */
    public static CharacterLargeObject valueOf(
        char[] source
    ){
        return new ArrayLargeObject(source);
    }

    /**
     * Create a {@code CharacterLargeObject} facade for the given URL
     * 
     * @param source
     * 
     * @return a {@code CharacterLargeObject} facade for the given URL
     */
    public static CharacterLargeObject valueOf(
        URL source
    ){
        return new URLLargeObject(source);
    }
    
    /**
     * Create a {@code CharacterLargeObject} facade for the given file
     * 
     * @param source the file to be read
     * @param encoding the file's the character encoding 
     * 
     * @return a {@code CharacterLargeObject} facade for the given file
     */
    public static CharacterLargeObject valueOf(
        File source, 
        String encoding
    ){
        return new FileLargeObject(source, encoding);
    }
    
    /**
     * Create a {@code CharacterLargeObject} facade for the given stream
     * 
     * @param source
     * 
     * @return a {@code CharacterLargeObject} facade for the given stream
     */
    public static CharacterLargeObject valueOf(
        Reader source
    ){
        return new StreamLargeObject(source);
    }
    
    /**
     * Create a {@code CharacterLargeObject} facade for the given stream
     * 
     * @param source
     * @param length
     * 
     * @return a {@code CharacterLargeObject} facade for the given stream
     */
    public static CharacterLargeObject valueOf(
        Reader source,
        Long length
    ){
        return new StreamLargeObject(source, length);
    }
    
    /**
     * Create a {@code CharacterLargeObject} copy of the given stream
     * 
     * @param source
     * @param length
     * 
     * @return a {@code CharacterLargeObject} copy of the given stream
     * 
     * @throws IOException  
     */
    public static CharacterLargeObject copyOf(
        Reader source,
        Long length
    ) throws IOException {
        CharArrayWriter buffer = length == null ? new CharArrayWriter(
        ) : new CharArrayWriter(
            length.intValue()
        );
        streamCopy(source, 0, buffer);
        return valueOf(buffer.toCharArray());
    }
    
    /**
     * A negative length is converted to {@code null}.
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
     * Copy a reader's content to a writer
     * 
     * @param source
     * @param position
     * @param target
     * 
     * @return the number of character's written to the writer
     * 
     * @throws IOException
     */
    public static long streamCopy(
        Reader source,
        long position,
        Writer target
    ) throws IOException {
        char[] buffer = new char[CAPACITY];
        if(position != 0l) {
            source.skip(position);
        }
        long count = 0l;
        for(int i; (i = source.read(buffer)) >= 0;){
            count += i;
            target.write(buffer, 0, i);
        }
        return count;
    }

    
    //------------------------------------------------------------------------
    // Class StringLargeObject
    //------------------------------------------------------------------------
    
    /**
     * String  CLOB
     */
    private static class StringLargeObject implements CharacterLargeObject {

        /**
         * Constructor 
         *
         * @param value
         */
        StringLargeObject(
            String value
        ) {
            this.value = value;
        }

        /**
         * 
         */
        final String value;
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent()
         */
        public Reader getContent(
        ) throws IOException {
            return new StringReader(this.value);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent(java.io.Writer, long)
         */
        public void getContent(
            Writer writer, 
            long position
        ) throws IOException {
            writer.write(this.value, (int)position, this.value.length() - (int)position);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ) throws IOException {
            return Long.valueOf(this.value.length());
        }
        
    }
    

    //------------------------------------------------------------------------
    // Class ArrayLargeObject
    //------------------------------------------------------------------------
    
    /**
     * Array CLOB
     */
    private static class ArrayLargeObject implements CharacterLargeObject, Serializable {
        
        /**
         * Constructor 
         *
         * @param value
         */
        public ArrayLargeObject(final char[] value) {
            this.value = value;
        }

        /**
         * Implements {@code Serializable}
         */
        private static final long serialVersionUID = -9096693393561649452L;

        /**
         * 
         */
        private final char[] value;
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ){
            return Long.valueOf(this.value.length);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent()
         */
        public Reader getContent(
        ){
            return new CharArrayReader(this.value);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent(java.io.Writer, long)
         */
        public void getContent(
            Writer writer, 
            long position
        ) throws IOException {
            int length = (int) (this.value.length - position);
            if(length < 0) throw new EOFException(
                "Position " + position + " is larger than the objects length " + this.value.length
            );
            writer.write(this.value, (int) position, length);
        }

    }
    

    //------------------------------------------------------------------------
    // Class AbstractLargeObject
    //------------------------------------------------------------------------

    /**
     * The underlying stream may be retrieved once only
     */
    private static abstract class AbstractLargeObject implements CharacterLargeObject, Serializable {
        
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
         * Implements {@code Serializable}
         */
        private static final long serialVersionUID = -5837819381102619869L;

        private transient char[] value;
        private transient Long length;

        /**
         * Provide the input stream content
         * 
         * @return the content
         * 
         * @throws IOException
         */
        protected Reader newContent(
        ) throws IOException {
            return this.value == null ? null : new CharArrayReader(this.value);
        }
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent(java.io.OutputStream, long)
         */
        public void getContent(
            Writer stream, 
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
        
        private void writeObject(
            ObjectOutputStream stream
        ) throws IOException {
            if(value == null) {
                CharArrayWriter buffer = new CharArrayWriter();
                getContent(buffer, 0);
                stream.writeObject(buffer.toCharArray());
            } else {
                stream.writeObject(this.value);
            }
        }
        
        private void readObject(
            ObjectInputStream stream
        ) throws IOException, ClassNotFoundException {
            this.value = (char[]) stream.readObject();
            this.length = Long.valueOf(value.length);
        }
        
    }
    
     
    //------------------------------------------------------------------------
    // Class URLLargeObject
    //------------------------------------------------------------------------
    
    /**
     * URL CLOB
     */
    private static class URLLargeObject extends AbstractLargeObject {
        
        /**
         * Constructor 
         *
         * @param value
         */
        public URLLargeObject(final URL url) {
            super(null);
            this.url = url;
        }

        /**
         * 
         */
        private static final long serialVersionUID = -4494073113239913907L;

        /**
         * 
         */
        private transient URL url;

        /**
         * 
         */
        private transient URLConnection connection;
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent()
         */
        @Override
        public Reader getContent() throws IOException {
            if(this.url == null) {
                return newContent();
            } else {
                URLConnection connection = getConnection();
                String encoding = connection.getContentEncoding();
                InputStream stream = connection.getInputStream();
                return encoding == null ? new InputStreamReader(
                    stream
                ) : new InputStreamReader(
                    stream,
                    encoding
                );
            }
        }

        /**
         * Opens the URL connection
         * 
         * @return the URL connection
         * 
         * @throws IOException
         */
        protected URLConnection getConnection() throws IOException{
            if(this.connection == null){
                this.connection = url.openConnection();
            }
            return this.connection;
        }

    }


    //------------------------------------------------------------------------
    // Class FileLargeObject
    //------------------------------------------------------------------------

    /**
     * File CLOB
     */
    private static class FileLargeObject extends AbstractLargeObject {
        
        /**
         * Constructor 
         * 
         * @param file the file to be read
         * @param encoding the encoding to be used 
         */
        public FileLargeObject(
            File file, 
            String encoding
        ) {
            super(null);
            this.file = file;
            this.encoding = encoding;
        }

        /**
         * 
         */
        private static final long serialVersionUID = 2452652537639826230L;

        
        /**
         * 
         */
        private transient File file;

        /**
         * The file's encoding
         */
        private transient String encoding;
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent()
         */
//      @SuppressWarnings("resource")
        public Reader getContent(
        ) throws IOException {
            return this.file != null ? new InputStreamReader(
                new FileInputStream(this.file),
                encoding
            ) : this.newContent();
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
            final Reader stream
        ){
            this(
                stream, 
                getLength(stream)
            );
        }
        
        /**
         * Constructor 
         *
         * @param stream
         */
        protected StreamLargeObject(
            final Reader stream,
            Long length
        ){
            super(length);
            this.stream = stream;
        }
        
        /**
         * 
         */
        private static final long serialVersionUID = -7462838546162808783L;

        /**
         * 
         */
        private transient Reader stream;

        /**
         * Determine the large objetc's size
         * 
         * @param stream
         * 
         * @return the large objetc's size
         */
        private static Long getLength(
            Reader stream
        ){
            return null; // generally undeterminable
        }
        
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public Reader getContent(
        ) throws IOException {
            if(this.stream == null) {
                return newContent();
            } else {
                Reader stream = this.stream;
                this.stream = null;
                return stream;
            }
        }

    }
    
 }