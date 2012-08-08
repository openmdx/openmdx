/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: InprocessMarshaller.java,v 1.2 2009/01/12 12:55:17 wfro Exp $
 * Description: Inprocess Stream Marshaller
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:55:17 $
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
package org.openmdx.application.dataprovider.transport.rmi;

import java.io.InputStream;
import java.io.Reader;
import java.rmi.Remote;

import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.stream.cci.Source_1_0;
import org.openmdx.base.stream.rmi.spi.StreamSynchronization_1_1;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.ByteString;
import org.w3c.cci2.ByteStringInputStream;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.cci2.CharacterString;
import org.w3c.cci2.CharacterStringReader;

/**
 * Marshals streams and service exceptions
 */
public class InprocessMarshaller extends AbstractMarshaller {

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.rmi.spi.DataproviderObjectInterceptor#unmarshal(java.rmi.Remote)
     */
    public Object unmarshal(Remote stream) {
        throw new UnsupportedOperationException();
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
    public InprocessMarshaller(
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
                        new BinaryLargeObjects.BinaryHolder(
                            (InputStream)value
                        )
                    );
                } 
                else if(value instanceof Reader) {
                    values.set(
                        0, 
                        new CharacterLargeObjects.CharacterHolder(
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
            if(value instanceof BinaryLargeObjects.BinaryHolder) {
                values.set(
                    0,
                    ((BinaryLargeObjects.BinaryHolder)value).toStream()
                );
            } 
            else if (value instanceof CharacterLargeObjects.CharacterHolder) {
                values.set(
                    0, 
                    ((CharacterLargeObjects.CharacterHolder)value).toStream()
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
            if(value instanceof BinaryLargeObjects.BinaryHolder) {
                values.set(
                    0,
                    ((BinaryLargeObjects.BinaryHolder)value).toStream()
                );
            } 
            else if (value instanceof CharacterLargeObjects.CharacterHolder) {
                values.set(
                    0, 
                    ((CharacterLargeObjects.CharacterHolder)value).toStream()
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
