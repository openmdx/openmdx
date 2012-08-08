/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Serializer.java,v 1.1 2004/07/06 14:09:53 hburger Exp $
 * Description: Serializer
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/07/06 14:09:53 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.application.container.spi.rmi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Serializer
 */
public class Serializer implements ObjectOutput {
    
    /**
     * Constructor
     */
    public Serializer(
    ) throws IOException {
        objectStream = new ObjectOutputStream(
            byteStream = new ByteArrayOutputStream()
        );
    }

    /**
     * 
     */
    private ByteArrayOutputStream byteStream;
    
    /**
     * 
     */
    private ObjectOutput objectStream;
    
    /**
     * This call flushes the serializer, creates a deserializer for the
     * data written to the serializer and empties the serializer.
     * 
     * @return a deserializer for the data written to the serializer
     * 
     * @throws IOException
     */
    public ObjectInput getDeserializer(
    ) throws IOException{
        flush();
        ObjectInput deserializer = new ObjectInputStream(
            new ByteArrayInputStream(byteStream.toByteArray())
        );
        byteStream.reset();
        return deserializer;
    }
    
    
    //------------------------------------------------------------------------
    // Implements ObjectOutput
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.io.ObjectOutput#close()
     */
    public void close() throws IOException {
        byteStream = null;
        objectStream = null;
    }

    /**
     * @throws java.io.IOException
     */
    public void flush() throws IOException {
        objectStream.flush();
    }
    
    /**
     * @param b
     * @throws java.io.IOException
     */
    public void write(byte[] b) throws IOException {
        objectStream.write(b);
    }
    /**
     * @param b
     * @param off
     * @param len
     * @throws java.io.IOException
     */
    public void write(byte[] b, int off, int len) throws IOException {
        objectStream.write(b, off, len);
    }
    /**
     * @param b
     * @throws java.io.IOException
     */
    public void write(int b) throws IOException {
        objectStream.write(b);
    }
    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeBoolean(boolean v) throws IOException {
        objectStream.writeBoolean(v);
    }
    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeByte(int v) throws IOException {
        objectStream.writeByte(v);
    }
    /**
     * @param s
     * @throws java.io.IOException
     */
    public void writeBytes(String s) throws IOException {
        objectStream.writeBytes(s);
    }
    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeChar(int v) throws IOException {
        objectStream.writeChar(v);
    }
    /**
     * @param s
     * @throws java.io.IOException
     */
    public void writeChars(String s) throws IOException {
        objectStream.writeChars(s);
    }
    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeDouble(double v) throws IOException {
        objectStream.writeDouble(v);
    }
    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeFloat(float v) throws IOException {
        objectStream.writeFloat(v);
    }
    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeInt(int v) throws IOException {
        objectStream.writeInt(v);
    }
    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeLong(long v) throws IOException {
        objectStream.writeLong(v);
    }
    /**
     * @param obj
     * @throws java.io.IOException
     */
    public void writeObject(Object obj) throws IOException {
        objectStream.writeObject(obj);
    }
    /**
     * @param v
     * @throws java.io.IOException
     */
    public void writeShort(int v) throws IOException {
        objectStream.writeShort(v);
    }
    /**
     * @param str
     * @throws java.io.IOException
     */
    public void writeUTF(String str) throws IOException {
        objectStream.writeUTF(str);
    }

}
