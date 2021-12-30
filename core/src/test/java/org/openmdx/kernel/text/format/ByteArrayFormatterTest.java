/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Byte Array Formatter Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2021, OMEX AG, Switzerland
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
package org.openmdx.kernel.text.format;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Byte Array Formatter Test
 */
public class ByteArrayFormatterTest {

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected final static byte[] BUF_NULL = null;
    protected byte[] bufEmpty;
    protected byte[] buf1Byte;
    protected byte[] buf7Byte;
    protected byte[] buf8Byte;
    protected byte[] buf9Byte;
    protected byte[] buf15Byte;
    protected byte[] buf16Byte;
    protected byte[] buf17Byte;
    protected byte[] bufLarge;
    protected byte[] bufNegative;

    @BeforeEach
    public void setUp() 
    {
        bufEmpty = new byte[0];

        buf1Byte = new byte[] { 
             48 };
        
        buf8Byte = new byte[] { 
             48, 49, 50, 51, 52, 53, 54, 55 };
             
        buf9Byte = new byte[] { 
             48, 49, 50, 51, 52, 53, 54, 55, 56 };

        buf15Byte = new byte[] { 
             48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69};
             
        buf16Byte = new byte[] { 
             48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
             
        buf17Byte = new byte[] { 
             48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 
             71 };
        
        bufLarge = new byte[] { 
             48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 
             71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86,
             87, 88, 89, 90, 91, 92 };

        bufNegative = new byte[] { 
            -128, -127, -126, -3, -2, -1, 0, 1, 2, 3, 125, 126, 127 };
    }

    @Test
    public void testParanoic() 
    {
        Assertions.assertEquals("", new ByteArrayFormatter(this.bufLarge, -1, 0).toString(), "ByteArrayFormatter(bufLarge, -1, 0)");       

        Assertions.assertEquals("", new ByteArrayFormatter(this.bufLarge, 0, -0).toString(), "ByteArrayFormatter(bufLarge, 0, -1)");       

        Assertions.assertEquals("", new ByteArrayFormatter(this.bufLarge, bufLarge.length, 1).toString(), "ByteArrayFormatter(bufLarge, bufLarge.length, 1)");       

        Assertions.assertEquals("000000  30 31 32 33 34                                    01234", new ByteArrayFormatter(this.bufLarge, -4, 9).toString(), "ByteArrayFormatter(bufLarge, -4, 9)");       
    }

    @Test
    public void testBufNull() 
    {
        Assertions.assertEquals("", new ByteArrayFormatter(BUF_NULL).toString(), "ByteArrayFormatter(bufNull)");
            
        Assertions.assertEquals("", new ByteArrayFormatter(BUF_NULL, 0, 0).toString(), "ByteArrayFormatter(bufNull, 0, 0)");       

            
        Assertions.assertEquals("", new ByteArrayFormatter(BUF_NULL, 2, 6).toString(), "ByteArrayFormatter(bufNull, 2, 6)");       
    }
    
    @Test
    public void testBufEmpty() 
    {
        Assertions.assertEquals("", new ByteArrayFormatter(this.bufEmpty).toString(), "ByteArrayFormatter(bufEmpty)");
            
        Assertions.assertEquals("", new ByteArrayFormatter(this.bufEmpty, 0, 0).toString(), "ByteArrayFormatter(bufEmpty, 0, 0)");       
    }

    @Test
    public void test1Byte() 
    {
        Assertions.assertEquals("000000  30                                                0", new ByteArrayFormatter(this.buf1Byte).toString(), "ByteArrayFormatter(buf1Byte)"); 
            
        Assertions.assertEquals("000000  30                                                0", new ByteArrayFormatter(this.buf1Byte, 0, buf1Byte.length).toString(), "ByteArrayFormatter(buf1Byte, 0, buf1Byte.length)");
    }

    @Test
    public void test8Byte() 
    {
        Assertions.assertEquals("000000  30 31 32 33 34 35 36 37                           01234567", new ByteArrayFormatter(this.buf8Byte).toString(), "ByteArrayFormatter(buf8Byte)"); 
            
        Assertions.assertEquals("000000  30 31 32 33 34 35 36 37                           01234567", new ByteArrayFormatter(this.buf8Byte, 0, buf8Byte.length).toString(), "ByteArrayFormatter(buf8Byte, 0, buf8Byte.length)");       
    }

    @Test
    public void test9Byte() 
    {
        Assertions.assertEquals("000000  30 31 32 33 34 35 36 37  38                       012345678", new ByteArrayFormatter(this.buf9Byte).toString(), "ByteArrayFormatter(buf9Byte)"); 
            
        Assertions.assertEquals("000000  30 31 32 33 34 35 36 37  38                       012345678", new ByteArrayFormatter(this.buf9Byte, 0, buf9Byte.length).toString(), "ByteArrayFormatter(buf9Byte, 0, buf9Byte.length)");       
    }

    @Test
    public void test15Byte() 
    {
        Assertions.assertEquals("000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45     0123456789ABCDE", new ByteArrayFormatter(this.buf15Byte).toString(), "ByteArrayFormatter(buf15Byte)"); 
            
        Assertions.assertEquals("000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45     0123456789ABCDE", new ByteArrayFormatter(this.buf15Byte, 0, buf15Byte.length).toString(), "ByteArrayFormatter(buf15Byte, 0, buf15Byte.length)");       
    }

    @Test
    public void test16Byte() 
    {
        Assertions.assertEquals("000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45 46  0123456789ABCDEF", new ByteArrayFormatter(this.buf16Byte).toString(), "ByteArrayFormatter(buf16Byte)"); 
            
        Assertions.assertEquals("000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45 46  0123456789ABCDEF", new ByteArrayFormatter(this.buf16Byte, 0, buf16Byte.length).toString(), "ByteArrayFormatter(buf16Byte, 0, buf16Byte.length)");       
    }

    @Test
    public void test17Byte() 
    {
        Assertions.assertEquals("000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45 46  0123456789ABCDEF\n" +
		"000010  47                                                G", new ByteArrayFormatter(this.buf17Byte).toString(), "ByteArrayFormatter(buf17Byte)"); 
            
        Assertions.assertEquals("000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45 46  0123456789ABCDEF\n" +
		"000010  47                                                G", new ByteArrayFormatter(this.buf17Byte, 0, buf17Byte.length).toString(), "ByteArrayFormatter(buf17Byte, 0, buf17Byte.length)");       
    }

    @Test
    public void testNegative() 
    {
        Assertions.assertEquals("000000  80 81 82 FD FE FF 00 01  02 03 7D 7E 7F           ..........}~.", new ByteArrayFormatter(this.bufNegative).toString(), "ByteArrayFormatter(bufNegative)"); 
            
        Assertions.assertEquals("000000  80 81 82 FD FE FF 00 01  02 03 7D 7E 7F           ..........}~.", new ByteArrayFormatter(this.bufNegative, 0, bufNegative.length).toString(), "ByteArrayFormatter(bufNegative, 0, bufNegative.length)");       
    }

    @Test
    public void testOffset() 
    {
        Assertions.assertEquals("", new ByteArrayFormatter(this.bufLarge, 0, 0).toString(), "ByteArrayFormatter(bufLarge, 0, 0)");       

        Assertions.assertEquals("000000  32                                                2", new ByteArrayFormatter(this.bufLarge, 2, 1).toString(), "ByteArrayFormatter(bufLarge, 2, 1)");       

        Assertions.assertEquals("000000  32 33 34                                          234", new ByteArrayFormatter(this.bufLarge, 2, 3).toString(), "ByteArrayFormatter(bufLarge, 2, 3)");       

        Assertions.assertEquals("000000  59 5A 5B 5C                                       YZ[\\", new ByteArrayFormatter(this.bufLarge, 34, 4).toString(), "ByteArrayFormatter(bufLarge, 34, 4)");       

        Assertions.assertEquals("000000  59 5A 5B 5C                                       YZ[\\", new ByteArrayFormatter(this.bufLarge, 34, 6).toString(), "ByteArrayFormatter(bufLarge, 34, 6)");       
    }

    @Test
    public void testHeader() 
    {
        Assertions.assertEquals("\n000000  32 33 34                                          234", new ByteArrayFormatter(this.bufLarge, 2, 3, "").toString(), "ByteArrayFormatter(bufLarge, 2, 3, \"\")");       

        Assertions.assertEquals("Buffer\n" +
		"000000  32 33 34                                          234", new ByteArrayFormatter(this.bufLarge, 2, 3, "Buffer").toString(), "ByteArrayFormatter(bufLarge, 2, 3, \"Buffer:\")");       
    }
}
