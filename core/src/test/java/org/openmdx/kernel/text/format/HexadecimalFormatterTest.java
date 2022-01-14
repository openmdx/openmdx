/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Hexadecimal Formatter Test
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * Test Hexadecimal Formatter
 */
public class HexadecimalFormatterTest {

	/**
	 * Add an instance variable for each part of the fixture
	 */
	protected static final byte[] NULL_BYTES = null;
	protected byte[] noBytes;
	protected byte[] someBytes;

	@BeforeEach
	public void setUp() {
		noBytes = new byte[0];
		someBytes = new byte[] { -128, -127, -126, -3, -2, -1, 0, 1, 2, 3, 125, 126, 127 };
	}

	@Test
	public void testParanoic() {
		Assertions.assertEquals("  808182FDFEFF000102037D7E",
				new HexadecimalFormatter(this.someBytes, -1, someBytes.length).toString(),
				"HexadecimalFormatter(someBytes, -1, someBytes.length)");
		Assertions.assertEquals("  808182FDFEFF000102037D7E7F",
				new HexadecimalFormatter(this.someBytes, -1, someBytes.length + 1).toString(),
				"HexadecimalFormatter(someBytes, -1, someBytes.length + 1)");
		Assertions.assertEquals("  808182FDFEFF000102037D7E7F    ",
				new HexadecimalFormatter(this.someBytes, -1, someBytes.length + 3).toString(),
				"HexadecimalFormatter(someBytes, -1, someBytes.length + 3)");
		Assertions.assertEquals("", new HexadecimalFormatter(this.someBytes, -1, 0).toString(),
				"HexadecimalFormatter(someBytes, -1, 0)");
		try {
			new HexadecimalFormatter(this.someBytes, 0, -1);
			Assertions.fail("HexadecimalFormatter(someBytes, 0, -1)");
		} catch (IllegalArgumentException expected) {
		}
		Assertions.assertEquals("", new HexadecimalFormatter(this.someBytes, -1, 0).toString(),
				"HexadecimalFormatter(someBytes, -1, 0)");
		Assertions.assertEquals("  ", new HexadecimalFormatter(this.someBytes, someBytes.length, 1).toString(),
				"ByteArrayFormatter(bufLarge, bufLarge.length, 1)");
		Assertions.assertEquals("  808182FDFEFF000102037D7E7F    ",
				new HexadecimalFormatter(this.someBytes, -1, someBytes.length + 3).toString(),
				"HexadecimalFormatter(someBytes, -1, someBytes.length + 3)");

		Assertions.assertEquals("        808182FDFE", new HexadecimalFormatter(this.someBytes, -4, 9).toString(),
				"ByteArrayFormatter(bufLarge, -4, 9)");
	}

	@Test
	public void testNullBytes() {
		Assertions.assertEquals("null", new HexadecimalFormatter(NULL_BYTES).toString(),
				"HexadecimalFormatter(nullBytes)");
		Assertions.assertEquals("null", new HexadecimalFormatter(NULL_BYTES, 0, 0).toString(),
				"HexadecimalFormatter(nullBytes, 0, 0)");
		Assertions.assertEquals("null", new HexadecimalFormatter(NULL_BYTES, 2, 6).toString(),
				"HexadecimalFormatter(nullBytes, 2, 6)");
	}

	@Test
	public void testNoBytes() {
		Assertions.assertEquals("", new HexadecimalFormatter(this.noBytes).toString(), "HexadecimalFormatter(noBytes)");
		Assertions.assertEquals("", new HexadecimalFormatter(this.noBytes, 0, 0).toString(),
				"HexadecimalFormatter(noBytes, 0, 0)");
		Assertions.assertEquals("      ", new HexadecimalFormatter(this.noBytes, 2, 3).toString(),
				"HexadecimalFormatter(noBytes, 2, 3)");
	}

	@Test
	public void testSomeBytes() {
		Assertions.assertEquals("808182FDFEFF000102037D7E7F", new HexadecimalFormatter(this.someBytes).toString(),
				"HexadecimalFormatter(someBytes)");
		Assertions.assertEquals("808182FDFEFF000102037D7E7F",
				new HexadecimalFormatter(this.someBytes, 0, someBytes.length).toString(),
				"HexadecimalFormatter(someBytes, 0, someBytes.length)");
	}

	@Test
	public void testOffset() {
		Assertions.assertEquals("8182FDFEFF000102037D7E7F  ",
				new HexadecimalFormatter(this.someBytes, 1, someBytes.length).toString(),
				"HexadecimalFormatter(someBytes, 1, someBytes.length)");
		Assertions.assertEquals("8182FD", new HexadecimalFormatter(this.someBytes, 1, 3).toString(),
				"HexadecimalFormatter(someBytes, 1, 3)");
		Assertions.assertEquals("8182FDFEFF000102037D7E7F",
				new HexadecimalFormatter(this.someBytes, 1, someBytes.length - 1).toString(),
				"HexadecimalFormatter(someBytes, 1, someBytes.length-1)");
	}

	@Test
	public void testByte() {
		Assertions.assertEquals("80", new HexadecimalFormatter((byte) Byte.MIN_VALUE).toString(),
				"HexadecimalFormatter((byte)Byte.MIN_VALUE))");
		Assertions.assertEquals("FF", new HexadecimalFormatter((byte) -1).toString(),
				"HexadecimalFormatter((byte)-1))");
		Assertions.assertEquals("00", new HexadecimalFormatter((byte) 0).toString(), "HexadecimalFormatter((byte)0))");
		Assertions.assertEquals("01", new HexadecimalFormatter((byte) +1).toString(),
				"HexadecimalFormatter((byte)+1))");
		Assertions.assertEquals("7F", new HexadecimalFormatter((byte) Byte.MAX_VALUE).toString(),
				"HexadecimalFormatter((byte)Byte.MAX_VALUE))");
		Assertions.assertEquals("", new HexadecimalFormatter((byte) 0x21, 0).toString(),
				"HexadecimalFormatter((byte)0x21),0)");
		Assertions.assertEquals("1", new HexadecimalFormatter((byte) 0x21, 1).toString(),
				"HexadecimalFormatter((byte)0x21),1)");
		Assertions.assertEquals("21", new HexadecimalFormatter((byte) 0x21, 2).toString(),
				"HexadecimalFormatter((byte)0x21),2)");
		Assertions.assertEquals("021", new HexadecimalFormatter((byte) 0x21, 3).toString(),
				"HexadecimalFormatter((byte)0x21),3)");
		Assertions.assertEquals("0021", new HexadecimalFormatter((byte) 0x21, 4).toString(),
				"HexadecimalFormatter((byte)0x21),4)");
		Assertions.assertEquals("00021", new HexadecimalFormatter((byte) 0x21, 5).toString(),
				"HexadecimalFormatter((byte)0x21),5)");
		Assertions.assertEquals("000021", new HexadecimalFormatter((byte) 0x21, 6).toString(),
				"HexadecimalFormatter((byte)0x21),6)");
		Assertions.assertEquals("0000021", new HexadecimalFormatter((byte) 0x21, 7).toString(),
				"HexadecimalFormatter((byte)0x21),7)");
		Assertions.assertEquals("00000021", new HexadecimalFormatter((byte) 0x21, 8).toString(),
				"HexadecimalFormatter((byte)0x21),8)");
		Assertions.assertEquals("000000021", new HexadecimalFormatter((byte) 0x21, 9).toString(),
				"HexadecimalFormatter((byte)0x21),9)");
	}

	@Test
	public void testShort() {
		Assertions.assertEquals("8000", new HexadecimalFormatter((short) Short.MIN_VALUE).toString(),
				"HexadecimalFormatter((short)Short.MIN_VALUE))");
		Assertions.assertEquals("FF80", new HexadecimalFormatter((short) Byte.MIN_VALUE).toString(),
				"HexadecimalFormatter((short)Byte.MIN_VALUE))");
		Assertions.assertEquals("FFFF", new HexadecimalFormatter((short) -1).toString(),
				"HexadecimalFormatter((short)-1))");
		Assertions.assertEquals("0000", new HexadecimalFormatter((short) 0).toString(),
				"HexadecimalFormatter((short)0))");
		Assertions.assertEquals("0001", new HexadecimalFormatter((short) +1).toString(),
				"HexadecimalFormatter((short)+1))");
		Assertions.assertEquals("007F", new HexadecimalFormatter((short) Byte.MAX_VALUE).toString(),
				"HexadecimalFormatter((short)Byte.MAX_VALUE))");
		Assertions.assertEquals("7FFF", new HexadecimalFormatter((short) Short.MAX_VALUE).toString(),
				"HexadecimalFormatter((short)Short.MAX_VALUE))");
		Assertions.assertEquals("", new HexadecimalFormatter((short) 0x4321, 0).toString(),
				"HexadecimalFormatter((short)0x4321),0)");
		Assertions.assertEquals("1", new HexadecimalFormatter((short) 0x4321, 1).toString(),
				"HexadecimalFormatter((short)0x4321),1)");
		Assertions.assertEquals("21", new HexadecimalFormatter((short) 0x4321, 2).toString(),
				"HexadecimalFormatter((short)0x4321),2)");
		Assertions.assertEquals("321", new HexadecimalFormatter((short) 0x4321, 3).toString(),
				"HexadecimalFormatter((short)0x4321),3)");
		Assertions.assertEquals("4321", new HexadecimalFormatter((short) 0x4321, 4).toString(),
				"HexadecimalFormatter((short)0x4321),4)");
		Assertions.assertEquals("04321", new HexadecimalFormatter((short) 0x4321, 5).toString(),
				"HexadecimalFormatter((short)0x4321),5)");
		Assertions.assertEquals("004321", new HexadecimalFormatter((short) 0x4321, 6).toString(),
				"HexadecimalFormatter((short)0x4321),6)");
		Assertions.assertEquals("0004321", new HexadecimalFormatter((short) 0x4321, 7).toString(),
				"HexadecimalFormatter((short)0x4321),7)");
		Assertions.assertEquals("00004321", new HexadecimalFormatter((short) 0x4321, 8).toString(),
				"HexadecimalFormatter((short)0x4321),8)");
		Assertions.assertEquals("000004321", new HexadecimalFormatter((short) 0x4321, 9).toString(),
				"HexadecimalFormatter((short)0x4321),9)");
	}

	@Test
	public void testInteger() {
		Assertions.assertEquals("80000000", new HexadecimalFormatter((int) Integer.MIN_VALUE).toString(),
				"HexadecimalFormatter((int)Integer.MIN_VALUE))");
		Assertions.assertEquals("FFFF8000", new HexadecimalFormatter((int) Short.MIN_VALUE).toString(),
				"HexadecimalFormatter((int)Short.MIN_VALUE))");
		Assertions.assertEquals("FFFFFF80", new HexadecimalFormatter((int) Byte.MIN_VALUE).toString(),
				"HexadecimalFormatter((int)Byte.MIN_VALUE))");
		Assertions.assertEquals("FFFFFFFF", new HexadecimalFormatter((int) -1).toString(),
				"HexadecimalFormatter((int)-1))");
		Assertions.assertEquals("00000000", new HexadecimalFormatter((int) 0).toString(),
				"HexadecimalFormatter((int)0))");
		Assertions.assertEquals("00000001", new HexadecimalFormatter((int) +1).toString(),
				"HexadecimalFormatter((int)+1))");
		Assertions.assertEquals("0000007F", new HexadecimalFormatter((int) Byte.MAX_VALUE).toString(),
				"HexadecimalFormatter((int)Byte.MAX_VALUE))");
		Assertions.assertEquals("00007FFF", new HexadecimalFormatter((int) Short.MAX_VALUE).toString(),
				"HexadecimalFormatter((int)Short.MAX_VALUE))");
		Assertions.assertEquals("7FFFFFFF", new HexadecimalFormatter((int) Integer.MAX_VALUE).toString(),
				"HexadecimalFormatter((int)Integer.MAX_VALUE))");
		Assertions.assertEquals("", new HexadecimalFormatter((int) 0x87654321, 0).toString(),
				"HexadecimalFormatter((int)0x87654321),0)");
		Assertions.assertEquals("1", new HexadecimalFormatter((int) 0x87654321, 1).toString(),
				"HexadecimalFormatter((int)0x87654321),1)");
		Assertions.assertEquals("21", new HexadecimalFormatter((int) 0x87654321, 2).toString(),
				"HexadecimalFormatter((int)0x87654321),2)");
		Assertions.assertEquals("321", new HexadecimalFormatter((int) 0x87654321, 3).toString(),
				"HexadecimalFormatter((int)0x87654321),3)");
		Assertions.assertEquals("4321", new HexadecimalFormatter((int) 0x87654321, 4).toString(),
				"HexadecimalFormatter((int)0x87654321),4)");
		Assertions.assertEquals("54321", new HexadecimalFormatter((int) 0x87654321, 5).toString(),
				"HexadecimalFormatter((int)0x87654321),5)");
		Assertions.assertEquals("654321", new HexadecimalFormatter((int) 0x87654321, 6).toString(),
				"HexadecimalFormatter((int)0x87654321),6)");
		Assertions.assertEquals("7654321", new HexadecimalFormatter((int) 0x87654321, 7).toString(),
				"HexadecimalFormatter((int)0x87654321),7)");
		Assertions.assertEquals("87654321", new HexadecimalFormatter((int) 0x87654321, 8).toString(),
				"HexadecimalFormatter((int)0x87654321),8)");
		Assertions.assertEquals("F87654321", new HexadecimalFormatter((int) 0x87654321, 9).toString(),
				"HexadecimalFormatter((int)0x87654321),9)");
		Assertions.assertEquals("FF87654321", new HexadecimalFormatter((int) 0x87654321, 10).toString(),
				"HexadecimalFormatter((int)0x87654321),10)");
		Assertions.assertEquals("FFF87654321", new HexadecimalFormatter((int) 0x87654321, 11).toString(),
				"HexadecimalFormatter((int)0x87654321),11)");
		Assertions.assertEquals("FFFF87654321", new HexadecimalFormatter((int) 0x87654321, 12).toString(),
				"HexadecimalFormatter((int)0x87654321),12)");
	}

	@Test
	public void testLong() {
		Assertions.assertEquals("8000000000000000", new HexadecimalFormatter((long) Long.MIN_VALUE).toString(),
				"HexadecimalFormatter((long)Long.MIN_VALUE))");
		Assertions.assertEquals("FFFFFFFF80000000", new HexadecimalFormatter((long) Integer.MIN_VALUE).toString(),
				"HexadecimalFormatter((long)Integer.MIN_VALUE))");
		Assertions.assertEquals("FFFFFFFFFFFF8000", new HexadecimalFormatter((long) Short.MIN_VALUE).toString(),
				"HexadecimalFormatter((long)Short.MIN_VALUE))");
		Assertions.assertEquals("FFFFFFFFFFFFFF80", new HexadecimalFormatter((long) Byte.MIN_VALUE).toString(),
				"HexadecimalFormatter((long)Byte.MIN_VALUE))");
		Assertions.assertEquals("FFFFFFFFFFFFFFFF", new HexadecimalFormatter((long) -1).toString(),
				"HexadecimalFormatter((long)-1))");
		Assertions.assertEquals("0000000000000000", new HexadecimalFormatter((long) 0).toString(),
				"HexadecimalFormatter((long)0))");
		Assertions.assertEquals("0000000000000001", new HexadecimalFormatter((long) +1).toString(),
				"HexadecimalFormatter((long)+1))");
		Assertions.assertEquals("000000000000007F", new HexadecimalFormatter((long) Byte.MAX_VALUE).toString(),
				"HexadecimalFormatter((long)Byte.MAX_VALUE))");
		Assertions.assertEquals("0000000000007FFF", new HexadecimalFormatter((long) Short.MAX_VALUE).toString(),
				"HexadecimalFormatter((long)Short.MAX_VALUE))");
		Assertions.assertEquals("000000007FFFFFFF", new HexadecimalFormatter((long) Integer.MAX_VALUE).toString(),
				"HexadecimalFormatter((long)Integer.MAX_VALUE))");
		Assertions.assertEquals("7FFFFFFFFFFFFFFF", new HexadecimalFormatter((long) Long.MAX_VALUE).toString(),
				"HexadecimalFormatter((long)Long.MAX_VALUE))");
		Assertions.assertEquals("", new HexadecimalFormatter((long) 0x987654321L, 0).toString(),
				"HexadecimalFormatter((long)0x987654321L),0)");
		Assertions.assertEquals("1", new HexadecimalFormatter((long) 0x987654321L, 1).toString(),
				"HexadecimalFormatter((long)0x987654321L),1)");
		Assertions.assertEquals("21", new HexadecimalFormatter((long) 0x987654321L, 2).toString(),
				"HexadecimalFormatter((long)0x987654321L),2)");
		Assertions.assertEquals("321", new HexadecimalFormatter((long) 0x987654321L, 3).toString(),
				"HexadecimalFormatter((long)0x987654321L),3)");
		Assertions.assertEquals("4321", new HexadecimalFormatter((long) 0x987654321L, 4).toString(),
				"HexadecimalFormatter((long)0x987654321L),4)");
		Assertions.assertEquals("54321", new HexadecimalFormatter((long) 0x987654321L, 5).toString(),
				"HexadecimalFormatter((long)0x987654321L),5)");
		Assertions.assertEquals("654321", new HexadecimalFormatter((long) 0x987654321L, 6).toString(),
				"HexadecimalFormatter((long)0x987654321L),6)");
		Assertions.assertEquals("7654321", new HexadecimalFormatter((long) 0x987654321L, 7).toString(),
				"HexadecimalFormatter((long)0x987654321L),7)");
		Assertions.assertEquals("87654321", new HexadecimalFormatter((long) 0x987654321L, 8).toString(),
				"HexadecimalFormatter((long)0x987654321L),8)");
		Assertions.assertEquals("987654321", new HexadecimalFormatter((long) 0x987654321L, 9).toString(),
				"HexadecimalFormatter((long)0x987654321L),9)");
		Assertions.assertEquals("0987654321", new HexadecimalFormatter((long) 0x987654321L, 10).toString(),
				"HexadecimalFormatter((long)0x987654321L),10)");
		Assertions.assertEquals("00987654321", new HexadecimalFormatter((long) 0x987654321L, 11).toString(),
				"HexadecimalFormatter((long)0x987654321L),11)");
		Assertions.assertEquals("000987654321", new HexadecimalFormatter((long) 0x987654321L, 12).toString(),
				"HexadecimalFormatter((long)0x987654321L),12)");
	}

}
