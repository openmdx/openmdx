/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: StreamMarshaller.java,v 1.5 2008/09/10 08:55:29 hburger Exp $
 * Description: Inprocess Stream Marshaller
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:29 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.rmi.inprocess;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.stream.cci.Source_1_0;
import org.openmdx.base.stream.rmi.spi.StreamSynchronization_1_1;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.transport.rmi.spi.DataproviderObjectInterceptor;
import org.openmdx.kernel.application.container.spi.rmi.ByteString;
import org.openmdx.kernel.application.container.spi.rmi.ByteStringInputStream;
import org.openmdx.kernel.application.container.spi.rmi.CharacterString;
import org.openmdx.kernel.application.container.spi.rmi.CharacterStringReader;
import org.openmdx.kernel.exception.BasicException;

/**
 * Marshals streams and service exceptions
 */
public class StreamMarshaller
implements DataproviderObjectInterceptor
{

    //------------------------------------------------------------------------
    public static class BinaryHolder implements Serializable  {

        /**
         * Constructor 
         *
         * @param stream
         * 
         * @throws IOException
         */
        public BinaryHolder(
            InputStream stream
        ) throws IOException {
            if(stream instanceof ByteStringInputStream) {
                this.string = ((ByteStringInputStream)stream).getString();
            } else {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int b;
                while((b = stream.read()) != -1) {
                    out.write(b);
                }
                this.string = new ByteString(out.toByteArray());
            }
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 1300727842760003691L;

        /**
         * The content
         */
        private final ByteString string;

        /**
         * Create an input stream
         * 
         * @return a newly created input stream
         */
        public BinarySource_1InputStream toStream(
        ){
            return new BinarySource_1InputStream(this.string);
        }

    }

    //------------------------------------------------------------------------
    public static class BinarySource_1InputStream 
    extends ByteStringInputStream 
    implements Source_1_0 
    {

        public BinarySource_1InputStream(
            ByteString bytes
        ) {
            super(bytes);
        }

        public long length(
        ) {
            return getString().length();
        }

    }

    //------------------------------------------------------------------------
    public static class CharacterHolder implements Serializable  {

        /**
         * Constructor 
         *
         * @param reader
         * 
         * @throws IOException
         */
        public CharacterHolder(
            Reader reader
        ) throws IOException {
            if(reader instanceof CharacterStringReader) {
                this.string = ((CharacterStringReader)reader).getString();
            } else {
                CharArrayWriter out = new CharArrayWriter();
                int c;
                while((c = reader.read()) != -1) {
                    out.write(c);
                }
                this.string = new CharacterString(out.toCharArray());
            }
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -1526645671101459098L;

        /**
         * The content
         */
        private final CharacterString string;

        /**
         * Create an input stream
         * 
         * @return a newly created input stream
         */
        public CharacterSource_1Reader toStream(
        ){
            return new CharacterSource_1Reader(this.string);
        }

    }

    //------------------------------------------------------------------------
    public static class CharacterSource_1Reader 
    extends CharacterStringReader 
    implements Source_1_0 
    {
        /**
         * Constructor 
         *
         * @param chars
         */
        public CharacterSource_1Reader(
            CharacterString chars
        ) {
            super(chars);
        }

        public long length(
        ) {
            return getString().length();
        }

    }

    //------------------------------------------------------------------------
    /**
     * Constructor
     */
    public StreamMarshaller(
    ){
        super();
    }

    //------------------------------------------------------------------------
    // Streaming
    //------------------------------------------------------------------------

    //-----------------------------------------------------------------------
    /**
     * Marshal an object
     *
     * @param   object The object to be marshaled
     *
     */ 
    public void marshal(
        DataproviderObject object
    ) throws ServiceException {
        for(String feature : object.attributeNames()){
            SparseList<Object> values = object.values(feature);
            Object value = values.get(0);
            try {
                if(value instanceof InputStream) {
                    values.set(
                        0, 
                        new BinaryHolder(
                            (InputStream)value
                        )
                    );
                } 
                else if(value instanceof Reader) {
                    values.set(
                        0, 
                        new CharacterHolder(
                            (Reader)value
                        )
                    );
                }
            }  catch (Exception exception){
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSFORMATION_FAILURE,
                    "marshalling of large object failed",
                    new BasicException.Parameter(
                        "path",
                        object == null ? null : object.path()
                    ),
                    new BasicException.Parameter(
                        "feature",
                        feature
                    ),
                    new BasicException.Parameter(
                        "class",
                        value.getClass().getName()
                    )
                );
            }   
        }        
    }

    //-----------------------------------------------------------------------
    /**
     * Unmarshal an object
     *
     * @param   object
     *          The object to be inspected
     */ 
    public void unmarshal(
        DataproviderObject object
    ) {
        for(String feature : object.attributeNames()){
            SparseList<Object> values = object.values(feature);
            Object value = values.get(0);
            if(value instanceof BinaryHolder) {
                values.set(
                    0,
                    ((BinaryHolder)value).toStream()
                );
            } 
            else if (value instanceof CharacterHolder) {
                values.set(
                    0, 
                    ((CharacterHolder)value).toStream()
                );
            } 
        }        
    }

    //-----------------------------------------------------------------------
    /**
     * Intercept an object by applying the same rules in both directions.
     *  
     * @param   object
     *          The object to be intercepted
     */
    public void intercept(
        DataproviderObject object
    ) throws ServiceException {
        for(String name : object.attributeNames()){
            SparseList<Object> values = object.values(name);
            Object value = values.get(0);
            if(value instanceof BinaryHolder) {
                values.set(
                    0,
                    ((BinaryHolder)value).toStream()
                );
            } 
            else if (value instanceof CharacterHolder) {
                values.set(
                    0, 
                    ((CharacterHolder)value).toStream()
                );
            } 
        }        
    }

    //-----------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.rmi.DataproviderObjectInterceptor#intercept(org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject, java.lang.String, java.io.File, int)
     */
    public void intercept(
        DataproviderObject object, 
        StreamSynchronization_1_1 synchronization
    ) throws ServiceException {
        this.intercept(object);
    }

}
