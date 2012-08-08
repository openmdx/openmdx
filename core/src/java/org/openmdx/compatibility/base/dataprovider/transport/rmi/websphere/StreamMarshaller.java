/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StreamMarshaller.java,v 1.8 2008/12/15 11:35:46 hburger Exp $
 * Description: RMIMapper class
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 11:35:46 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.rmi.websphere;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.stream.cci.InputStream_1;
import org.openmdx.base.stream.cci.Reader_1;
import org.openmdx.base.stream.rmi.cci.BinarySink_1OutputStream;
import org.openmdx.base.stream.rmi.cci.BinarySink_1_0;
import org.openmdx.base.stream.rmi.cci.BinarySource_1InputStream;
import org.openmdx.base.stream.rmi.cci.BinarySource_1_0;
import org.openmdx.base.stream.rmi.cci.CharacterSink_1Writer;
import org.openmdx.base.stream.rmi.cci.CharacterSink_1_0;
import org.openmdx.base.stream.rmi.cci.CharacterSource_1Reader;
import org.openmdx.base.stream.rmi.cci.CharacterSource_1_0;
import org.openmdx.base.stream.rmi.iiop.BinarySink_1;
import org.openmdx.base.stream.rmi.iiop.BinarySink_1Proxy;
import org.openmdx.base.stream.rmi.iiop.BinarySource_1;
import org.openmdx.base.stream.rmi.iiop.BinarySource_1Proxy;
import org.openmdx.base.stream.rmi.iiop.CharacterSink_1;
import org.openmdx.base.stream.rmi.iiop.CharacterSink_1Proxy;
import org.openmdx.base.stream.rmi.iiop.CharacterSource_1;
import org.openmdx.base.stream.rmi.iiop.CharacterSource_1Proxy;
import org.openmdx.base.stream.rmi.spi.StreamSynchronization_1_0;
import org.openmdx.base.stream.rmi.spi.StreamSynchronization_1_1;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.transport.rmi.spi.AbstractMarshaller;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.security.ExecutionContext;
import org.openmdx.kernel.security.rmi.CallbackContext;

/**
 * Marshalls remote streams and service exceptions
 */
@SuppressWarnings("unchecked")
public class StreamMarshaller extends AbstractMarshaller {

    /**
     * Constructor
     */
    public StreamMarshaller(
    ){
        super();
    }

    /**
     * Retrieve the appropriate callback context.
     * 
     * @return the callback context
     * 
     * @throws ServiceException
     */
    protected ExecutionContext getCallbackContext(
    ){
        return CallbackContext.getInstance();
    }


    //------------------------------------------------------------------------
    // Streaming
    //------------------------------------------------------------------------

    /**
     * Marshal an object
     *
     * @param   object
     *          The object to be inspected
     *
     * @exception   ServiceException    MEDIA_ACCESS
     *              if RMI transport is unavailable
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
                        new BinarySource_1(
                            (InputStream)value,
                            getLength(values)
                        )
                    );
                } else if (value instanceof Reader) {
                    values.set(
                        0, 
                        new CharacterSource_1(
                            (Reader)value,
                            getLength(values)
                        )
                    );
                } else if (value instanceof OutputStream) {
                    values.set(
                        0, 
                        new BinarySink_1((OutputStream)value)
                    );
                } else if (value instanceof Writer){
                    values.set(
                        0, 
                        new CharacterSink_1((Writer)value)
                    );
                }
            } catch (Exception exception){
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

    /**
     * The optional argument length is stored at position 1
     * 
     * @param values
     */
    private static long getLength(
        SparseList values
    ){
        Object length = values.get(1);
        return length instanceof Long ? ((Long)length).longValue() : -1L;
    }

    /**
     * Unmarshal an object
     *
     * @param   object
     *          The object to be inspected
     */ 
    public void unmarshal(
        DataproviderObject object
    ){
        for(
                Iterator i = object.attributeNames().iterator();
                i.hasNext();
        ){
            SparseList values = object.values((String)i.next());
            Object value = values.get(0);
            if(value instanceof BinarySource_1_0) {
                values.set(
                    0,
                    new BinarySource_1InputStream(
                        (BinarySource_1_0)value,
                        getCallbackContext()
                    )
                );
            } else if (value instanceof CharacterSource_1_0) {
                values.set(
                    0, 
                    new CharacterSource_1Reader(
                        (CharacterSource_1_0)value,
                        getCallbackContext()
                    )
                );
            } else if (value instanceof BinarySink_1_0) {
                values.set(
                    0,
                    new BinarySink_1OutputStream(
                        (BinarySink_1_0)value,
                        getCallbackContext()
                    )
                );
            } else if (value instanceof CharacterSink_1_0) {
                values.set(
                    0,
                    new CharacterSink_1Writer(
                        (CharacterSink_1_0)value,
                        getCallbackContext()
                    )
                );
            } else if(value instanceof InputStream) {
                long length = getLength(values);
                if(length >= 0) {
                    values.clear();
                    values.set(
                        0, 
                        new InputStream_1(
                            (InputStream)value,
                            length
                        )
                    );
                }
            } else if (value instanceof Reader) {
                long length = getLength(values);
                if(length >= 0) {
                    values.clear();
                    values.set(
                        0, 
                        new Reader_1(
                            (Reader)value,
                            length
                        )
                    );
                }
            }
        }
    }

    /**
     * Intercept an object by applying the same rules in both directions.
     *  
     * @param   object
     *          The object to be inspected
     */
    public void intercept(DataproviderObject object) throws ServiceException {
        for(
                Iterator i = object.attributeNames().iterator();
                i.hasNext();
        ){
            String name = (String)i.next();
            SparseList values = object.values(name);
            Object value = values.get(0);
            try {
                if(value instanceof BinarySource_1_0) {
                    values.set(
                        0,
                        new BinarySource_1Proxy(
                            (BinarySource_1_0)value
                        )
                    );
                } else if (value instanceof CharacterSource_1_0) {
                    values.set(
                        0, 
                        new CharacterSource_1Proxy(
                            (CharacterSource_1_0)value
                        )
                    );
                } else if (value instanceof BinarySink_1_0) {
                    values.set(
                        0,
                        new BinarySink_1Proxy(
                            (BinarySink_1_0)value
                        )
                    );
                } else if (value instanceof CharacterSink_1_0) {
                    values.set(
                        0,
                        new CharacterSink_1Proxy(
                            (CharacterSink_1_0)value
                        )
                    );
                }
            } catch (IOException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSFORMATION_FAILURE,
                    "Creating a stream proxy failed",
                    new BasicException.Parameter("path", object.path()),
                    new BasicException.Parameter("feature", name),
                    new BasicException.Parameter("class", value.getClass().getName())
                );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.rmi.DataproviderObjectInterceptor#intercept(org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject, java.lang.String, java.io.File, int)
     */
    public void intercept(
        DataproviderObject object, 
        StreamSynchronization_1_1 synchronization
    ) throws ServiceException {
        if(synchronization == null) {
            intercept(object);
        } else {
            for(
                    Iterator i = object.attributeNames().iterator();
                    i.hasNext();
            ){
                String name = (String)i.next();
                SparseList values = object.values(name);
                Object value = values.get(0);
                try {
                    if(value instanceof BinarySource_1_0) {
                        StreamSynchronization_1_0.SourceEntry entry = synchronization.add(
                            (BinarySource_1_0)value
                        );
                        values.set(0, entry.getBinaryStream());
                        values.set(1, new Long(entry.getLength()));                        
                    } else if (value instanceof CharacterSource_1_0) {
                        StreamSynchronization_1_0.SourceEntry entry = synchronization.add(
                            (CharacterSource_1_0)value
                        );
                        values.set(0, entry.getCharacterStream());
                        values.set(1, new Long(entry.getLength()));                        
                    } else if (value instanceof BinarySink_1_0) {
                        StreamSynchronization_1_0.SinkEntry entry = synchronization.add(
                            (BinarySink_1_0)value
                        );
                        values.set(0, entry.getBinaryStream());
                    } else if (value instanceof CharacterSink_1_0) {
                        StreamSynchronization_1_0.SinkEntry entry = synchronization.add(
                            (CharacterSink_1_0)value
                        );
                        values.set(0, entry.getCharacterStream());
                    } else if (value instanceof InputStream) {
                        StreamSynchronization_1_0.SourceEntry entry = synchronization.add(
                            (InputStream)value
                        );
                        values.set(0, entry.getBinaryStream());
                        values.set(1, new Long(entry.getLength()));                        
                    } else if (value instanceof Reader) {
                        StreamSynchronization_1_0.SourceEntry entry = synchronization.add(
                            (Reader)value
                        );
                        values.set(0, entry.getCharacterStream());
                        values.set(1, new Long(entry.getLength()));                        
                    } else if (value instanceof OutputStream) {
                        StreamSynchronization_1_0.SinkEntry entry = synchronization.add(
                            (OutputStream)value
                        );
                        values.set(0, entry.getBinaryStream());
                    } else if (value instanceof Writer) {
                        StreamSynchronization_1_0.SinkEntry entry = synchronization.add(
                            (Writer)value
                        );
                        values.set(0, entry.getCharacterStream());
                    }
                } catch (IOException exception) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSFORMATION_FAILURE,
                        "Creating a stream proxy failed",
                        new BasicException.Parameter("unitOfWork", synchronization.toString()),
                        new BasicException.Parameter("path", object.path()),
                        new BasicException.Parameter("feature", name),
                        new BasicException.Parameter("class", value.getClass().getName())
                    );
                }
            }
        }
    }

}
