/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: CharacterLargeObjects.java,v 1.5 2008/04/21 16:52:08 hburger Exp $
 * Description: Object Relational Mapping 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/21 16:52:08 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2008, OMEX AG, Switzerland
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

import java.io.CharArrayReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject;

/**
 * Object Relational Mapping
 */
public class CharacterLargeObjects {

    /**
     * To avoid instantiation
     */
    protected CharacterLargeObjects() {
        super();
    }

    public static CharacterLargeObject valueOf(
        char[] source
    ){
        return new ArrayLargeObject(source);
    }
           
    /**
     * Array CLOB
     */
    private static class ArrayLargeObject implements CharacterLargeObject {
        
        /**
         * Constructor 
         *
         * @param value
         */
        public ArrayLargeObject(final char[] value) {
            this.value = value;
        }

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
    
    public static CharacterLargeObject valueOf(
        URL source
    ){
        return new URLLargeObject(source);
    }
    
    /**
     * URL CLOB
     */
    private static class URLLargeObject implements CharacterLargeObject {
        
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
        private transient Long length;
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent()
         */
        public Reader getContent() throws IOException {
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

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ){
            return this.length;
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

        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent(java.io.Writer, long)
         */
        public void getContent(
            Writer writer, 
            long position
        ) throws IOException {
            this.length = position + streamCopy(
                getContent(),
                position,
                writer
            );
        }
        
    }

    public static CharacterLargeObject valueOf(
        File source
    ){
        return new FileLargeObject(source);
    }
    
    /**
     * File CLOB
     */
    private static class FileLargeObject implements CharacterLargeObject {
        
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
        
        /**
         * 
         */
        private transient Long length = null;

        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent()
         */
        public Reader getContent(
        ) throws IOException {
            return new InputStreamReader(
                new FileInputStream(this.file),
                "UTF-8"
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ){
            return this.length;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent(java.io.Writer, long)
         */
        public void getContent(
            Writer writer, 
            long position
        ) throws IOException {
            this.length = position + streamCopy(
                getContent(),
                position,
                writer
            );
        }

    }

    public static CharacterLargeObject valueOf(
        ReadableLargeObject delegate
    ){
        return new CompatibilityLargeObject(delegate);
    }
    
    /**
     * Compatibility CLOB
     */
    private static class CompatibilityLargeObject implements CharacterLargeObject {

        /**
         * Constructor 
         *
         * @param delegate
         */
        CompatibilityLargeObject(ReadableLargeObject delegate) {
            this.delegate = delegate;
        }

        /**
         * 
         */
        private final ReadableLargeObject delegate;
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent()
         */
        public Reader getContent() throws IOException {
            try {
                return this.delegate.getCharacterStream();
            } catch (ServiceException exception) {
                throw new ExtendedIOException(exception);
            }
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ) throws IOException {
            try {
                return asLength(this.delegate.length());
            } catch (ServiceException exception) {
                throw new ExtendedIOException(exception);
            }
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent(java.io.Writer, long)
         */
        public void getContent(
            Writer writer, 
            long position
        ) throws IOException {
            try {
                this.delegate.getCharacterStream(
                    writer,
                    position
                );
            } catch (ServiceException exception) {
                throw new ExtendedIOException(exception);
            }
        }        
        
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
     * Copy a reader's content to a writer
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
        for(
            int i = source.read(buffer);
            i >= 0;
        ){
            count += i;
            target.write(buffer, 0, i);
        }
        return count;
    }

    /**
     * Default capacity for stream copy
     */
    private final static int CAPACITY = 10000;

}