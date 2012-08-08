/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: StreamMarshaller.java,v 1.2 2008/02/04 15:48:55 wfro Exp $
 * Description: Inprocess Stream Marshaller
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/04 15:48:55 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.rmi.inprocess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Iterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.stream.cci.Source_1_0;
import org.openmdx.base.stream.rmi.spi.StreamSynchronization_1_1;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.transport.rmi.spi.DataproviderObjectInterceptor;
import org.openmdx.kernel.exception.BasicException;

/**
 * Marshals streams and service exceptions
 */
public class StreamMarshaller
    implements DataproviderObjectInterceptor
{
    
    //------------------------------------------------------------------------
    public static class BinaryHolder 
        implements Serializable  {
    
        public BinaryHolder(
            InputStream stream
        ) throws IOException {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             int b;
             while((b = stream.read()) != -1) {
                 out.write(b);
             }
             this.bytes = out.toByteArray();
        }
    
        public byte[] getBytes(
        ) {
            return this.bytes;
        }
        
        private static final long serialVersionUID = -839683168768777175L;
        private byte[] bytes;
    }
    
    //------------------------------------------------------------------------
    public static class BinarySource_1InputStream 
        extends ByteArrayInputStream 
        implements Source_1_0 {
        
        public BinarySource_1InputStream(
            byte[] bytes
        ) {
            super(bytes);
            this.length = bytes.length;
        }
              
        public long length(
        ) {
            return this.length;
        }
        
        private final long length;
        
    }
    
    //------------------------------------------------------------------------
    public static class CharacterHolder 
        implements Serializable  {
    
        public CharacterHolder(
            Reader reader
        ) throws IOException {
             CharArrayWriter out = new CharArrayWriter();
             int c;
             while((c = reader.read()) != -1) {
                 out.write(c);
             }
             this.chars = out.toCharArray();
        }
    
        public char[] getChars(
        ) {
            return this.chars;
        }
        
        private static final long serialVersionUID = 7856912513397409899L;
        private char[] chars;
    }
    
    //------------------------------------------------------------------------
    public static class CharacterSource_1Reader 
        extends CharArrayReader 
        implements Source_1_0 {
        
        public CharacterSource_1Reader(
            char[] chars
        ) {
            super(chars);
        }
              
        public long length(
        ) {
            return this.length();
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
        for(
            Iterator i = object.attributeNames().iterator();
            i.hasNext();
        ){
            String feature = (String)i.next();
            SparseList values = object.values(feature);
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
            } 
            catch (Exception exception){
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSFORMATION_FAILURE,
                    new BasicException.Parameter[]{
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
                    },
                    "marshalling of large object failed"
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
        for(
            Iterator i = object.attributeNames().iterator();
            i.hasNext();
        ){
            SparseList values = object.values((String)i.next());
            Object value = values.get(0);
            if(value instanceof BinaryHolder) {
                values.set(
                    0,
                    new BinarySource_1InputStream(
                        ((BinaryHolder)value).getBytes()
                    )
                );
            } 
            else if (value instanceof CharacterHolder) {
                values.set(
                    0, 
                    new CharacterSource_1Reader(
                        ((CharacterHolder)value).getChars()
                    )
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
        for(
            Iterator i = object.attributeNames().iterator();
            i.hasNext();
        ){
            String name = (String)i.next();
            SparseList values = object.values(name);
            Object value = values.get(0);
            if(value instanceof BinaryHolder) {
                values.set(
                    0,
                    new BinarySource_1InputStream(
                        ((BinaryHolder)value).getBytes()
                    )
                );
            } 
            else if (value instanceof CharacterHolder) {
                values.set(
                    0, 
                    new CharacterSource_1Reader(
                        ((CharacterHolder)value).getChars()
                    )
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
