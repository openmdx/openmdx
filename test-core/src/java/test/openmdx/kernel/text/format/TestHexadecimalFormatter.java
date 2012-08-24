/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test Hexadecimal Formatter
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
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openmdx.kernel.text.format.HexadecimalFormatter;

/**
 * Test Hexadecimal Formatter
 */
public class TestHexadecimalFormatter {

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected byte[] nullBytes;
    protected byte[] noBytes;
    protected byte[] someBytes;

    @Before
    public void setUp() 
    {
        nullBytes  = null;
        
        noBytes = new byte[0];

        someBytes = new byte[] { 
            -128, -127, -126, -3, -2, -1, 0, 1, 2, 3, 125, 126, 127 };
    }

    @Test
    public void testParanoic() 
    {
        assertEquals(
            "HexadecimalFormatter(someBytes, -1, someBytes.length)",
            "  808182FDFEFF000102037D7E",
            new HexadecimalFormatter(this.someBytes, -1, someBytes.length).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter(someBytes, -1, someBytes.length + 1)",
            "  808182FDFEFF000102037D7E7F",
            new HexadecimalFormatter(this.someBytes, -1, someBytes.length + 1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter(someBytes, -1, someBytes.length + 3)",
            "  808182FDFEFF000102037D7E7F    ",
            new HexadecimalFormatter(this.someBytes, -1, someBytes.length + 3).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter(someBytes, -1, 0)",
            "",
            new HexadecimalFormatter(this.someBytes, -1, 0).toString()
        ); 
        try {
            new HexadecimalFormatter(this.someBytes, 0, -1);
            fail("HexadecimalFormatter(someBytes, 0, -1)");
        } catch (IllegalArgumentException expected){
        }
        assertEquals(
            "HexadecimalFormatter(someBytes, -1, 0)",
            "",
            new HexadecimalFormatter(this.someBytes, -1, 0).toString()
        ); 
        assertEquals(
            "ByteArrayFormatter(bufLarge, bufLarge.length, 1)", 
            "  ",
            new HexadecimalFormatter(this.someBytes, someBytes.length, 1).toString()
        );       
        assertEquals(
            "HexadecimalFormatter(someBytes, -1, someBytes.length + 3)",
            "  808182FDFEFF000102037D7E7F    ",
            new HexadecimalFormatter(this.someBytes, -1, someBytes.length + 3).toString()
        ); 

        assertEquals(
            "ByteArrayFormatter(bufLarge, -4, 9)", 
            "        808182FDFE",
            new HexadecimalFormatter(this.someBytes, -4, 9).toString()
        );       
    }

    @Test
    public void testNullBytes() 
    {
        assertEquals(
            "HexadecimalFormatter(nullBytes)",
            "null",
            new HexadecimalFormatter(this.nullBytes).toString()
        );            
        assertEquals(
            "HexadecimalFormatter(nullBytes, 0, 0)",
            "null", 
            new HexadecimalFormatter(this.nullBytes, 0, 0).toString()
        );
        assertEquals(
            "HexadecimalFormatter(nullBytes, 2, 6)",
            "null", 
            new HexadecimalFormatter(this.nullBytes, 2, 6).toString()
        );       
    }
    
    @Test
    public void testNoBytes() 
    {
        assertEquals(
            "HexadecimalFormatter(noBytes)",
            "",
            new HexadecimalFormatter(this.noBytes).toString()
        );
        assertEquals(
            "HexadecimalFormatter(noBytes, 0, 0)",
            "", 
            new HexadecimalFormatter(this.noBytes, 0, 0).toString()
        );
        assertEquals(
            "HexadecimalFormatter(noBytes, 2, 3)",
            "      ", 
            new HexadecimalFormatter(this.noBytes, 2, 3).toString()
        );       
    }

    @Test
    public void testSomeBytes() 
    {
        assertEquals(
            "HexadecimalFormatter(someBytes)",
            "808182FDFEFF000102037D7E7F",
            new HexadecimalFormatter(this.someBytes).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter(someBytes, 0, someBytes.length)",
            "808182FDFEFF000102037D7E7F",
            new HexadecimalFormatter(this.someBytes, 0, someBytes.length).toString()
        ); 
    }

    @Test
    public void testOffset() 
    {
        assertEquals(
            "HexadecimalFormatter(someBytes, 1, someBytes.length)",
            "8182FDFEFF000102037D7E7F  ",
            new HexadecimalFormatter(this.someBytes, 1, someBytes.length).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter(someBytes, 1, 3)",
            "8182FD",
            new HexadecimalFormatter(this.someBytes, 1, 3).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter(someBytes, 1, someBytes.length-1)",
            "8182FDFEFF000102037D7E7F",
            new HexadecimalFormatter(this.someBytes, 1, someBytes.length-1).toString()
        ); 
    }

    @Test
    public void testByte(
    ){
        assertEquals(
            "HexadecimalFormatter((byte)Byte.MIN_VALUE))",
            "80",
            new HexadecimalFormatter((byte)Byte.MIN_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)-1))",
            "FF",
            new HexadecimalFormatter((byte)-1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)0))",
            "00",
            new HexadecimalFormatter((byte)0).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)+1))",
            "01",
            new HexadecimalFormatter((byte)+1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)Byte.MAX_VALUE))",
            "7F",
            new HexadecimalFormatter((byte)Byte.MAX_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)0x21),0)",
            "",
            new HexadecimalFormatter((byte)0x21,0).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)0x21),1)",
            "1",
            new HexadecimalFormatter((byte)0x21,1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)0x21),2)",
            "21",
            new HexadecimalFormatter((byte)0x21,2).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)0x21),3)",
            "021",
            new HexadecimalFormatter((byte)0x21,3).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)0x21),4)",
            "0021",
            new HexadecimalFormatter((byte)0x21,4).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)0x21),5)",
            "00021",
            new HexadecimalFormatter((byte)0x21,5).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)0x21),6)",
            "000021",
            new HexadecimalFormatter((byte)0x21,6).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)0x21),7)",
            "0000021",
            new HexadecimalFormatter((byte)0x21,7).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)0x21),8)",
            "00000021",
            new HexadecimalFormatter((byte)0x21,8).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((byte)0x21),9)",
            "000000021",
            new HexadecimalFormatter((byte)0x21,9).toString()
        ); 
    }

    @Test
    public void testShort(
    ){
        assertEquals(
            "HexadecimalFormatter((short)Short.MIN_VALUE))",
            "8000",
            new HexadecimalFormatter((short)Short.MIN_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)Byte.MIN_VALUE))",
            "FF80",
            new HexadecimalFormatter((short)Byte.MIN_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)-1))",
            "FFFF",
            new HexadecimalFormatter((short)-1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)0))",
            "0000",
            new HexadecimalFormatter((short)0).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)+1))",
            "0001",
            new HexadecimalFormatter((short)+1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)Byte.MAX_VALUE))",
            "007F",
            new HexadecimalFormatter((short)Byte.MAX_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)Short.MAX_VALUE))",
            "7FFF",
            new HexadecimalFormatter((short)Short.MAX_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)0x4321),0)",
            "",
            new HexadecimalFormatter((short)0x4321,0).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)0x4321),1)",
            "1",
            new HexadecimalFormatter((short)0x4321,1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)0x4321),2)",
            "21",
            new HexadecimalFormatter((short)0x4321,2).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)0x4321),3)",
            "321",
            new HexadecimalFormatter((short)0x4321,3).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)0x4321),4)",
            "4321",
            new HexadecimalFormatter((short)0x4321,4).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)0x4321),5)",
            "04321",
            new HexadecimalFormatter((short)0x4321,5).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)0x4321),6)",
            "004321",
            new HexadecimalFormatter((short)0x4321,6).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)0x4321),7)",
            "0004321",
            new HexadecimalFormatter((short)0x4321,7).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)0x4321),8)",
            "00004321",
            new HexadecimalFormatter((short)0x4321,8).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((short)0x4321),9)",
            "000004321",
            new HexadecimalFormatter((short)0x4321,9).toString()
        ); 
    }

    @Test
    public void testInteger(
    ){
        assertEquals(
            "HexadecimalFormatter((int)Integer.MIN_VALUE))",
            "80000000",
            new HexadecimalFormatter((int)Integer.MIN_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)Short.MIN_VALUE))",
            "FFFF8000",
            new HexadecimalFormatter((int)Short.MIN_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)Byte.MIN_VALUE))",
            "FFFFFF80",
            new HexadecimalFormatter((int)Byte.MIN_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)-1))",
            "FFFFFFFF",
            new HexadecimalFormatter((int)-1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0))",
            "00000000",
            new HexadecimalFormatter((int)0).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)+1))",
            "00000001",
            new HexadecimalFormatter((int)+1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)Byte.MAX_VALUE))",
            "0000007F",
            new HexadecimalFormatter((int)Byte.MAX_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)Short.MAX_VALUE))",
            "00007FFF",
            new HexadecimalFormatter((int)Short.MAX_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)Integer.MAX_VALUE))",
            "7FFFFFFF",
            new HexadecimalFormatter((int)Integer.MAX_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),0)",
            "",
            new HexadecimalFormatter((int)0x87654321,0).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),1)",
            "1",
            new HexadecimalFormatter((int)0x87654321,1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),2)",
            "21",
            new HexadecimalFormatter((int)0x87654321,2).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),3)",
            "321",
            new HexadecimalFormatter((int)0x87654321,3).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),4)",
            "4321",
            new HexadecimalFormatter((int)0x87654321,4).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),5)",
            "54321",
            new HexadecimalFormatter((int)0x87654321,5).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),6)",
            "654321",
            new HexadecimalFormatter((int)0x87654321,6).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),7)",
            "7654321",
            new HexadecimalFormatter((int)0x87654321,7).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),8)",
            "87654321",
            new HexadecimalFormatter((int)0x87654321,8).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),9)",
            "F87654321",
            new HexadecimalFormatter((int)0x87654321,9).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),10)",
            "FF87654321",
            new HexadecimalFormatter((int)0x87654321,10).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),11)",
            "FFF87654321",
            new HexadecimalFormatter((int)0x87654321,11).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((int)0x87654321),12)",
            "FFFF87654321",
            new HexadecimalFormatter((int)0x87654321,12).toString()
        ); 
    }
    
    @Test
    public void testLong(
    ){
        assertEquals(
            "HexadecimalFormatter((long)Long.MIN_VALUE))",
            "8000000000000000",
            new HexadecimalFormatter((long)Long.MIN_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)Integer.MIN_VALUE))",
            "FFFFFFFF80000000",
            new HexadecimalFormatter((long)Integer.MIN_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)Short.MIN_VALUE))",
            "FFFFFFFFFFFF8000",
            new HexadecimalFormatter((long)Short.MIN_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)Byte.MIN_VALUE))",
            "FFFFFFFFFFFFFF80",
            new HexadecimalFormatter((long)Byte.MIN_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)-1))",
            "FFFFFFFFFFFFFFFF",
            new HexadecimalFormatter((long)-1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0))",
            "0000000000000000",
            new HexadecimalFormatter((long)0).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)+1))",
            "0000000000000001",
            new HexadecimalFormatter((long)+1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)Byte.MAX_VALUE))",
            "000000000000007F",
            new HexadecimalFormatter((long)Byte.MAX_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)Short.MAX_VALUE))",
            "0000000000007FFF",
            new HexadecimalFormatter((long)Short.MAX_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)Integer.MAX_VALUE))",
            "000000007FFFFFFF",
            new HexadecimalFormatter((long)Integer.MAX_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)Long.MAX_VALUE))",
            "7FFFFFFFFFFFFFFF",
            new HexadecimalFormatter((long)Long.MAX_VALUE).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),0)",
            "",
            new HexadecimalFormatter((long)0x987654321L,0).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),1)",
            "1",
            new HexadecimalFormatter((long)0x987654321L,1).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),2)",
            "21",
            new HexadecimalFormatter((long)0x987654321L,2).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),3)",
            "321",
            new HexadecimalFormatter((long)0x987654321L,3).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),4)",
            "4321",
            new HexadecimalFormatter((long)0x987654321L,4).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),5)",
            "54321",
            new HexadecimalFormatter((long)0x987654321L,5).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),6)",
            "654321",
            new HexadecimalFormatter((long)0x987654321L,6).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),7)",
            "7654321",
            new HexadecimalFormatter((long)0x987654321L,7).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),8)",
            "87654321",
            new HexadecimalFormatter((long)0x987654321L,8).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),9)",
            "987654321",
            new HexadecimalFormatter((long)0x987654321L,9).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),10)",
            "0987654321",
            new HexadecimalFormatter((long)0x987654321L,10).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),11)",
            "00987654321",
            new HexadecimalFormatter((long)0x987654321L,11).toString()
        ); 
        assertEquals(
            "HexadecimalFormatter((long)0x987654321L),12)",
            "000987654321",
            new HexadecimalFormatter((long)0x987654321L,12).toString()
        ); 
    }

}
