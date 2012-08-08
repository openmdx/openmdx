/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Numbers.java,v 1.4 2008/03/21 18:30:27 hburger Exp $
 * Description: Numbers 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:30:27 $
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

package org.openmdx.base.io;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Numbers
 */
public class Numbers {

    /**
     * Constructor 
     */
    private Numbers() {
        // Avoid instantiation
    }

    private static final byte TC_NULL = 0;
    private static final byte TC_BYTE = 1;
    private static final byte TC_SHORT = 2;
    private static final byte TC_INTEGER = 3;
    private static final byte TC_LONG = 4;
    private static final byte TC_BIG_INTEGER = 5;
    private static final byte TC_BIG_DECIMAL = 6;
    private static final byte TC_FLOAT = 7;
    private static final byte TC_DOUBLE = 8;

    /**
     * Write a number to a DataOutput
     * 
     * @param sink where the number should be written to
     * @param value the number to be written
     * 
     * @throws IOException 
     */
    public static void writeNumber(
        DataOutput sink,
        Number value
    ) throws IOException{
        if(value == null) {
            sink.writeByte(TC_NULL);
        } else {
            Class<?> c = value.getClass();
            if(c == Byte.class) {
                sink.writeByte(TC_BYTE);
                sink.writeByte(value.byteValue());
            } else if (c == Short.class) { 
                sink.writeByte(TC_SHORT);
                sink.writeByte(value.shortValue());
            } else if (c == Integer.class) {
                sink.writeByte(TC_SHORT);
                sink.writeShort(value.shortValue());
            } else if (c == Long.class) {
                sink.writeByte(TC_LONG);
                sink.writeLong(value.longValue());
            } else if (c == BigInteger.class) {
                BigInteger bigInteger = (BigInteger) value; 
                byte[] raw = bigInteger.toByteArray();
                sink.writeByte(TC_BIG_INTEGER);
                sink.write(raw);
            } else if (c == BigDecimal.class) {
                BigDecimal bigDecimal = (BigDecimal) value; 
                byte[] raw = bigDecimal.unscaledValue().toByteArray();
                int scale = bigDecimal.scale();
                sink.writeByte(TC_BIG_DECIMAL);
                sink.writeShort(raw.length);
                sink.write(raw);
                sink.writeInt(scale);
            } else if (c == Float.class) {
                sink.writeByte(TC_FLOAT);
                sink.writeFloat(value.floatValue());
            } else if (c == Double.class) {
                sink.writeByte(TC_DOUBLE);
                sink.writeDouble(value.doubleValue());
            } else throw new InvalidClassException(
                c.getName(),
                "Class can't be externalized to java.io.DataOutput"
            );
        }
    }

    /**
     * Read a number from DataInput
     * 
     * @param source where the number should be read from
     * 
     * @return value the read number
     * @throws IOException 
     * 
     * @throws IOException 
     */
    public static Number readNumber(
        DataInput source
    ) throws IOException{
        switch(source.readByte()) {
            case TC_NULL: 
                return null;
            case TC_BYTE:
                return new Byte(source.readByte());
            case TC_SHORT:
                return new Short(source.readShort());
            case TC_INTEGER:
                return new Integer(source.readInt());
            case TC_LONG:
                return new Long(source.readLong());
            case TC_BIG_INTEGER: {
                byte[] raw = new byte[source.readShort()];
                source.readFully(raw);
                return new BigInteger(raw);
            }
            case TC_BIG_DECIMAL: {
                byte[] raw = new byte[source.readShort()];
                source.readFully(raw);
                int scale = source.readShort();
                return new BigDecimal(new BigInteger(raw), scale);
            }
            case TC_FLOAT:
                return new Float(source.readFloat());
            case TC_DOUBLE:
                return new Double(source.readDouble());
            default: 
                throw new InvalidObjectException("Unknown number type");
        }
    }
        
}
