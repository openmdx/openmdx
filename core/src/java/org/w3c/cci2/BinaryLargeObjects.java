/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: BinaryLargeObjects.java,v 1.2 2007/12/17 16:54:04 hburger Exp $
 * Description: Binary Large Object Factory 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/17 16:54:04 $
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject;

/**
 * Binary Large Object Factory
 */
public class BinaryLargeObjects {

    /**
     * To avoid instantiation
     */
    protected BinaryLargeObjects() {
        super();
    }

    public static BinaryLargeObject valueOf(
        byte[] source
    ){
        return new ArrayLargeObject(source);
    }
    
    /**
     * Array BLOB
     */
    private static class ArrayLargeObject implements BinaryLargeObject {
        
        /**
         * Constructor 
         *
         * @param value
         */
        public ArrayLargeObject(final byte[] value) {
            this.value = value;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent() {
            return new ByteArrayInputStream(this.value);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ){
            return Long.valueOf(this.value.length);
        }

        /**
         * 
         */
        private final byte[] value;

    }
       
    public static BinaryLargeObject valueOf(
        URL source
    ){
        return new URLLargeObject(source);
    }
    
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

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent(
        ) throws IOException {
            return this.url.openStream();
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ) throws IOException{
            return asLength(this.url.openConnection().getContentLength());
        }

        /**
         * 
         */
        private final URL url;

    }

    public static BinaryLargeObject valueOf(
        File source
    ){
        return new FileLargeObject(source);
    }
    
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
            return asLength(this.file.length());
        }

        /**
         * 
         */
        private final File file;

    }

    public static BinaryLargeObject valueOf(
        ReadableLargeObject delegate
    ){
        return new CompatibilityLargeObject(delegate);
    }
    
    /**
     * Compatibility CLOB
     */
    private static class CompatibilityLargeObject implements BinaryLargeObject {

        /**
         * Constructor 
         *
         * @param delegate
         */
        CompatibilityLargeObject(ReadableLargeObject delegate) {
            this.delegate = delegate;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent() throws IOException {
            try {
                return this.delegate.getBinaryStream();
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
        
        /**
         * 
         */
        private final ReadableLargeObject delegate;
        
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

}