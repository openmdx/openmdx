/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Byte array data formatter
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.kernel.text.format;

import org.openmdx.kernel.text.MultiLineStringRepresentation;

public class ByteArrayFormatter
        implements MultiLineStringRepresentation
{

    /**
     * Creates a new formatter for a byte buffer. The byte buffer will be 
     * formatted on demand when calling its {@code toString} method.
     * 
     * @param buf the input buffer.
     * @param offset the offset in the buffer of the first byte to format.
     * @param length the maximum number of bytes to read from the buffer 
     * @param header an optional header line (may be null) 
     */
    public ByteArrayFormatter(byte[] buf, int offset, int length, String header)
    {
        this.buf = buf;
        this.offset = (buf != null) ? offset : 0;
        this.length = (buf != null) ? length : 0;
        this.header = header;

        // some sanity checks
        if (buf != null) {
            if (this.offset < 0) {
                this.length += this.offset;
                this.offset = 0;
            }
            else if (this.offset >= buf.length) {
                this.offset = 0;
                this.length = 0;
            }

            if (this.length < 0) this.length = 0;

            if ((this.offset + this.length) >= buf.length) {
                this.length = buf.length - this.offset;
            }
        }
    }

    /**
     * Creates a new formatter for a byte buffer. The byte buffer will be 
     * formatted on demand when calling its {@code toString} method.
     * 
     * @param buf the input buffer.
     * @param offset the offset in the buffer of the first byte to format.
     * @param length the maximum number of bytes to read from the buffer 
     */
    public ByteArrayFormatter(byte[] buf, int offset, int length)
    {
        this(buf, offset, length, null);
    }


    /**
     * Creates a new formatter for a byte buffer. The byte buffer will be 
     * formatted on demand when calling its {@code toString} method.
     * 
     * @param buf the input buffer.
     */
    public ByteArrayFormatter(byte[] buf)
    {
        this(buf, 0, (buf != null) ? buf.length : 0);
    }


    /**
     * Returns a string representation of the byte buffer
     *
     * @return  a String
     */
    @Override
    public String toString()
    {
        return hexify();
    }


    /**
     * Produces a hex dump from a byte buffer.
     *
     * @return the hex dump
     */
    private String hexify()
    {
                if ((this.buf == null)
            || (this.length <= 0)
            || (this.offset >= this.buf.length)
           ) {

            return (this.header == null) ? new String() : this.header;
        }

                final StringBuilder hexBuf = new StringBuilder(this.length * 5);
                final StringBuilder hex = new StringBuilder();
                final StringBuilder ascii = new StringBuilder();

                int lineBytes = 0;
                int idx = 0;
                int value = 0;
        int line = 0; // line number [0...N]


        if (this.header != null) hexBuf.append(this.header).append('\n');

                while(idx < this.length) {
                        // process next line with 16 bytes or the remaining bytes
                        lineBytes = (this.length-idx) >= 16 ? 16 : this.length-idx;

                        // reuse string buffers
                        hex.setLength(0);
                        ascii.setLength(0);

                        // hexify
                        for(int ii=0; ii<lineBytes; ii++) {
                                value = this.buf[this.offset + idx];

                                hex.append(
                                    new HexadecimalFormatter(value>=0?value:256+value, 2)
                                ).append(
                                    ' '
                                );

                                if (ii==7) hex.append(' '); // make two coloumns

                                ascii.append(((value>=0x20)&&(value<=0x7E)) ? (char)value : '.');
                                idx++;
                        }

                        // fill up with blanks
                        if (lineBytes <= 7) hex.append(' ');
                        for(int ii=0; ii<(16-lineBytes)*3; ii++) hex.append(' ');
                        // for(int ii=lineBytes; ii<16; ii++) ascii.append(' ');

                        // fill the buffer
                        if (line > 0) hexBuf.append('\n');
                        hexBuf.append(
                            new HexadecimalFormatter(line*16, 6)
                        ).append(
                            "  "
                        ).append(
                            hex
                        );
                        hexBuf.append(
                           ' '
                       ).append(
                           ascii
                       );
            line++;
                }

                return hexBuf.toString();
    }


    private byte[] buf;
    private int offset;
    private int length;
    private String header;
}
