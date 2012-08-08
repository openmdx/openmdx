/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: InternalMethodInvocationArguments.java,v 1.3 2009/02/24 16:02:51 hburger Exp $
 * Description: Value Holder
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/24 16:02:51 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.application.container.spi.ejb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.io.Final;

/**
 * Allows to pass values to a remote object
 */
public class InternalMethodInvocationArguments {

    /**
     * Constructor 
     */
    protected InternalMethodInvocationArguments() {
    }

    /**
     * Validation is very expensive!
     */
    private final static boolean VALIDATE_SERIALIZED_OBJECT = false;

    /**
     * Reusing improves performance
     */
    static final boolean REUSE_VALUE_OBJECTS = true;
    
    /**
     * The marker character
     */
    static final char MARKER_CHARACTER = 0x5A29;

    /**
     * The marker object
     */
    static final Character MARKER_OBJECT = MARKER_CHARACTER;
    
    /**
     * The buffer is among the same thread's invocations
     */
    private final Buffer buffer = new Buffer();

    /**
     * The list of saved values is re-used between the sane thread's invocations
     */
    private final List<Object> values = REUSE_VALUE_OBJECTS ? new ArrayList<Object>() : null;
    
    /**
     * The index is reset before de-serialization
     */
    private int index = 0;
    
    /**
     * <code>true</code> if the serialized object should be thrown instead 
     * of returned.
     */
    private boolean throwable = false;

    /**
     * Re-use expensive objects such as byte buffers
     */
    private static ThreadLocal<InternalMethodInvocationArguments> instances = 
        new ThreadLocal<InternalMethodInvocationArguments>() {
        
        protected synchronized InternalMethodInvocationArguments initialValue() {
            return new InternalMethodInvocationArguments();
        }
    };

    /**
     * Retrieve an instance
     * 
     * @return a thread-local instance
     */
    public static InternalMethodInvocationArguments getInstance(){
        return InternalMethodInvocationArguments.instances.get();
    }
    
    /**
     * Serialize the exception
     * 
     * @param exception to be serialized
     * 
     * @throws RemoteException 
     */
    public void raise(
        Throwable throwable
    ) throws RemoteException {
        this.throwable = true;
        serialize(
            throwable instanceof Exception ? throwable : BasicException.toExceptionStack(throwable)
        );
    }

    /**
     * Serialize the object
     * 
     * @param object to be serialized
     * 
     * @throws RemoteException 
     */
    public void put(
        Object object
    ) throws RemoteException {
        this.throwable = false;
        serialize(object);
    }

    /**
     * De-serialize the object
     * 
     * @return the de-serialized object
     * 
     * @throws Exception 
     */
    public Object get(
    ) throws Exception {
        if(this.throwable) {
            throw (Exception) deserialize();
        } else {
            return deserialize();
        }
    }

    /**
     * Serialize the object
     * 
     * @param object to be serialized
     * 
     * @throws RemoteException 
     */
    private void serialize(
        Object object
    ) throws RemoteException {
        try {
            if(REUSE_VALUE_OBJECTS) {
                this.values.clear();
            }
            ObjectOutputStream sink = this.buffer.newSink();
            sink.writeObject(object);
            sink.close();
        } catch (IOException exception) {
            throw new RemoteException(
                "Serialization failure",
                BasicException.toExceptionStack(exception)
            );
        }
        if (VALIDATE_SERIALIZED_OBJECT) try {
            deserialize();
        } catch (Exception exception) {
            throw new RemoteException(
                "Check failure",
                BasicException.toExceptionStack(exception)
            );
        }
    }

    /**
     * De-serialize the object
     * 
     * @return the de-serialized object
     * 
     * @throws RemoteException 
     */
    private Object deserialize(
    ) throws RemoteException {
        try {
            if(REUSE_VALUE_OBJECTS) {
                this.index = 0;
            }
            ObjectInputStream source = this.buffer.newSource();
            Object object = source.readObject(); 
            if(REUSE_VALUE_OBJECTS && this.index != this.values.size()) {
                throw new StreamCorruptedException(
                    "Number of non-retrieved saved objects: " + (this.values.size() - this.index)
                );
            }
            return object;
        } catch (RemoteException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RemoteException(
                "Deserialization failure",
                BasicException.toExceptionStack(exception)
            );
        }
    }
    
    /**
     * Save an unmodifiable object
     * 
     * @param object
     */
    void save(
        Object object
    ){
        this.values.add(object);
    }

    /**
     * Retrieve a saved object
     * 
     * @return the saved object
     * @throws StreamCorruptedException 
     */
    Object retrieve(
    ) throws StreamCorruptedException{
        try {
            return this.values.get(this.index++);
        } catch (IndexOutOfBoundsException exception) {
            throw new StreamCorruptedException(
                "There are no more saved objects left"
            );
        }
    }

    
    //------------------------------------------------------------------------
    // Class Sink
    //------------------------------------------------------------------------
    
    /**
     * Sink
     */
    class Sink extends ObjectOutputStream {

        /**
         * Constructor 
         *
         * @param out
         * @throws IOException
         */
        public Sink(
            OutputStream out
        ) throws IOException {
            super(out);
            super.enableReplaceObject(REUSE_VALUE_OBJECTS);
        }

        /* (non-Javadoc)
         * @see java.io.ObjectOutputStream#replaceObject(java.lang.Object)
         */
        @Override
        protected Object replaceObject(
            Object object
        ) throws IOException {
            if(
                object instanceof String || 
                object instanceof Number || 
                object instanceof Final ||
                MARKER_OBJECT.equals(object)
            ) {
                InternalMethodInvocationArguments.this.save(object);
                return MARKER_CHARACTER; // char to Character conversion required!
            } else {
                return object;
            }
        }        
    }
       
    
    //------------------------------------------------------------------------
    // Class Source
    //------------------------------------------------------------------------
    
    /**
     * Source
     */
    class Source extends ObjectInputStream {

        /**
         * Constructor 
         *
         * @param in
         * @throws IOException
         */
        public Source(InputStream in)
        throws IOException {
            super(in);
            super.enableResolveObject(REUSE_VALUE_OBJECTS);
        }

        /**
         * Load the local class equivalent of the specified stream class
         * description. 
         * 
         * @param   desc an instance of class <code>ObjectStreamClass</code>
         * 
         * @return  a <code>Class</code> object corresponding to <code>desc</code>
         * 
         * @throws  IOException any of the usual input/output exceptions
         * @throws  ClassNotFoundException if class of a serialized object cannot
         *      be found
         */
        protected Class<?> resolveClass(
            ObjectStreamClass desc
        ) throws IOException, ClassNotFoundException {
            try {
                return Classes.getApplicationClass(desc.getName());
            } catch (ClassNotFoundException ex) {
                return super.resolveClass(desc);
            }
        }

        /* (non-Javadoc)
         * @see java.io.ObjectInputStream#resolveObject(java.lang.Object)
         */
        @Override
        protected Object resolveObject(
            Object obj
        ) throws IOException {
            return MARKER_OBJECT.equals(obj) ?
                InternalMethodInvocationArguments.this.retrieve() :
                obj;
        }
        
    }
    
    
    //------------------------------------------------------------------------
    // Class Buffer
    //------------------------------------------------------------------------
    
    /**
     * Buffer
     */
    final class Buffer extends ByteArrayOutputStream {

        /**
         * Create a sink stream
         * 
         * @return
         * 
         * @throws IOException
         */
        ObjectOutputStream newSink(
        ) throws IOException{
            this.reset();
            return new Sink(this);
        }
        
        /**
         * Create a source stream
         * 
         * @return
         * @throws IOException
         */
        ObjectInputStream newSource(
        ) throws IOException{
            return new Source( 
                new ByteArrayInputStream(
                    this.buf,
                    0,
                    this.count
                )
            );
        }

    }
        
}
