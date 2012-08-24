/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Test Byte Array Formatter
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package test.openmdx.kernel.text.format;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openmdx.kernel.text.format.ByteArrayFormatter;

/**
 * Test Byte Array Formatter
 */
public class TestByteArrayFormatter {

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected byte[] bufNull;
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

    @Before
    public void setUp() 
    {
        bufNull  = null;
        
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
        assertEquals(
            "ByteArrayFormatter(bufLarge, -1, 0)", 
            "",
            new ByteArrayFormatter(this.bufLarge, -1, 0).toString());       

        assertEquals(
            "ByteArrayFormatter(bufLarge, 0, -1)", 
            "",
            new ByteArrayFormatter(this.bufLarge, 0, -0).toString());       

        assertEquals(
            "ByteArrayFormatter(bufLarge, bufLarge.length, 1)", 
            "",
            new ByteArrayFormatter(this.bufLarge, bufLarge.length, 1).toString());       

        assertEquals(
            "ByteArrayFormatter(bufLarge, -4, 9)", 
            "000000  30 31 32 33 34                                    01234",
            new ByteArrayFormatter(this.bufLarge, -4, 9).toString());       
    }

    @Test
    public void testBufNull() 
    {
        assertEquals(
            "ByteArrayFormatter(bufNull)",
            "",
            new ByteArrayFormatter(this.bufNull).toString());
            
        assertEquals(
            "ByteArrayFormatter(bufNull, 0, 0)",
            "", 
            new ByteArrayFormatter(this.bufNull, 0, 0).toString());       

            
        assertEquals(
            "ByteArrayFormatter(bufNull, 2, 6)",
            "", 
            new ByteArrayFormatter(this.bufNull, 2, 6).toString());       
    }
    
    @Test
    public void testBufEmpty() 
    {
        assertEquals(
            "ByteArrayFormatter(bufEmpty)",
            "",
            new ByteArrayFormatter(this.bufEmpty).toString());
            
        assertEquals(
            "ByteArrayFormatter(bufEmpty, 0, 0)", 
            "",
            new ByteArrayFormatter(this.bufEmpty, 0, 0).toString());       
    }

    @Test
    public void test1Byte() 
    {
        assertEquals(
            "ByteArrayFormatter(buf1Byte)",
            "000000  30                                                0",
            new ByteArrayFormatter(this.buf1Byte).toString()); 
            
        assertEquals(
            "ByteArrayFormatter(buf1Byte, 0, buf1Byte.length)", 
            "000000  30                                                0",
            new ByteArrayFormatter(this.buf1Byte, 0, buf1Byte.length).toString());
    }

    @Test
    public void test8Byte() 
    {
        assertEquals(
            "ByteArrayFormatter(buf8Byte)",
            "000000  30 31 32 33 34 35 36 37                           01234567",
            new ByteArrayFormatter(this.buf8Byte).toString()); 
            
        assertEquals(
            "ByteArrayFormatter(buf8Byte, 0, buf8Byte.length)", 
            "000000  30 31 32 33 34 35 36 37                           01234567",
            new ByteArrayFormatter(this.buf8Byte, 0, buf8Byte.length).toString());       
    }

    @Test
    public void test9Byte() 
    {
        assertEquals(
            "ByteArrayFormatter(buf9Byte)",
            "000000  30 31 32 33 34 35 36 37  38                       012345678",
            new ByteArrayFormatter(this.buf9Byte).toString()); 
            
        assertEquals(
            "ByteArrayFormatter(buf9Byte, 0, buf9Byte.length)", 
            "000000  30 31 32 33 34 35 36 37  38                       012345678",
            new ByteArrayFormatter(this.buf9Byte, 0, buf9Byte.length).toString());       
    }

    @Test
    public void test15Byte() 
    {
        assertEquals(
            "ByteArrayFormatter(buf15Byte)",
            "000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45     0123456789ABCDE",
            new ByteArrayFormatter(this.buf15Byte).toString()); 
            
        assertEquals(
            "ByteArrayFormatter(buf15Byte, 0, buf15Byte.length)", 
            "000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45     0123456789ABCDE",
            new ByteArrayFormatter(this.buf15Byte, 0, buf15Byte.length).toString());       
    }

    @Test
    public void test16Byte() 
    {
        assertEquals(
            "ByteArrayFormatter(buf16Byte)",
            "000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45 46  0123456789ABCDEF",
            new ByteArrayFormatter(this.buf16Byte).toString()); 
            
        assertEquals(
            "ByteArrayFormatter(buf16Byte, 0, buf16Byte.length)", 
            "000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45 46  0123456789ABCDEF",
            new ByteArrayFormatter(this.buf16Byte, 0, buf16Byte.length).toString());       
    }

    @Test
    public void test17Byte() 
    {
        assertEquals(
            "ByteArrayFormatter(buf17Byte)",
            "000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45 46  0123456789ABCDEF\n" +
            "000010  47                                                G",
            new ByteArrayFormatter(this.buf17Byte).toString()); 
            
        assertEquals(
            "ByteArrayFormatter(buf17Byte, 0, buf17Byte.length)", 
            "000000  30 31 32 33 34 35 36 37  38 39 41 42 43 44 45 46  0123456789ABCDEF\n" +
            "000010  47                                                G",
            new ByteArrayFormatter(this.buf17Byte, 0, buf17Byte.length).toString());       
    }

    @Test
    public void testNegative() 
    {
        assertEquals(
            "ByteArrayFormatter(bufNegative)",
            "000000  80 81 82 FD FE FF 00 01  02 03 7D 7E 7F           ..........}~.",
            new ByteArrayFormatter(this.bufNegative).toString()); 
            
        assertEquals(
            "ByteArrayFormatter(bufNegative, 0, bufNegative.length)", 
            "000000  80 81 82 FD FE FF 00 01  02 03 7D 7E 7F           ..........}~.",
            new ByteArrayFormatter(this.bufNegative, 0, bufNegative.length).toString());       
    }

    @Test
    public void testOffset() 
    {
        assertEquals(
            "ByteArrayFormatter(bufLarge, 0, 0)", 
            "",
            new ByteArrayFormatter(this.bufLarge, 0, 0).toString());       

        assertEquals(
            "ByteArrayFormatter(bufLarge, 2, 1)", 
            "000000  32                                                2",
            new ByteArrayFormatter(this.bufLarge, 2, 1).toString());       

        assertEquals(
            "ByteArrayFormatter(bufLarge, 2, 3)", 
            "000000  32 33 34                                          234",
            new ByteArrayFormatter(this.bufLarge, 2, 3).toString());       

        assertEquals(
            "ByteArrayFormatter(bufLarge, 34, 4)", 
            "000000  59 5A 5B 5C                                       YZ[\\",
            new ByteArrayFormatter(this.bufLarge, 34, 4).toString());       

        assertEquals(
            "ByteArrayFormatter(bufLarge, 34, 6)", 
            "000000  59 5A 5B 5C                                       YZ[\\",
            new ByteArrayFormatter(this.bufLarge, 34, 6).toString());       
    }

    @Test
    public void testHeader() 
    {
        assertEquals(
            "ByteArrayFormatter(bufLarge, 2, 3, \"\")",
            "\n000000  32 33 34                                          234",
            new ByteArrayFormatter(this.bufLarge, 2, 3, "").toString());       

        assertEquals(
            "ByteArrayFormatter(bufLarge, 2, 3, \"Buffer:\")",
            "Buffer\n" +
            "000000  32 33 34                                          234",
            new ByteArrayFormatter(this.bufLarge, 2, 3, "Buffer").toString());       
    }
}
