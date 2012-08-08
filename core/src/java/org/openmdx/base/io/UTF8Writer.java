/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: UTF8Writer.java,v 1.3 2010/03/02 19:17:00 hburger Exp $
 * Description: UTF8 Writer 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/02 19:17:00 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Write UTF-8 characters to an output stream without buffering
 * <p>
 * The methods <code>flush()</code> and <code>close()</code>
 * are not delegated on to the underlying stream on purpose!
 */
public class UTF8Writer extends Writer {

    /**
     * Constructor 
     *
     * @param binaryStream
     */
    public UTF8Writer(
        OutputStream binaryStream
    ) {
        this.binaryStream = binaryStream;
    }

    /**
    * The delegate
    */
    private OutputStream binaryStream;

    /**
     * Java represents chars that are not in the Basic Multilingual
     * Plane (BMP) in UTF-16. This integer stores the first code unit 
     * for a code point encoded in two UTF-16 code units.
     */
    private int pending = 0;

    /**
     * Non-buffering write
     */
    @Override
    public final void write(
        int utf16
    ) throws IOException {
        //
        // Check in we are encoding at high and low surrogates
        //
        if (this.pending == 0) {
            //
            // Otherwise, encode char as defined in UTF-8
            //
            if (utf16 < 0x80) {
                //
                // 1 byte, 7 bits
                //
                this.binaryStream.write(utf16);
            } else if (utf16 < 0x800) {
                //
                // 2 bytes, 11 bits
                //
                this.binaryStream.write(0xC0 | (utf16 >> 6)); // first 5
                this.binaryStream.write(0x80 | (utf16 & 0x3F)); // second 6
            } else if (utf16 <= '\uFFFF') {
                char character = (char) utf16;
                if (
                    !Character.isHighSurrogate(character) &&
                    !Character.isLowSurrogate(character)
                ) {
                    //
                    // 3 bytes, 16 bits
                    //
                    this.binaryStream.write(0xE0 | (utf16 >> 12)); // first 4
                    this.binaryStream.write(0x80 | ((utf16 >> 6) & 0x3F)); // second 6
                    this.binaryStream.write(0x80 | (utf16 & 0x3F)); // third 6
                } else {
                    this.pending = utf16;
                }
            }
        } else {
            final int uc = (((pending & 0x3ff) << 10) | (utf16 & 0x3ff)) + 0x10000;
            if (uc < 0 || uc >= 0x200000) throw new IOException(
                "Atttempting to write invalid Unicode code point '" + uc + "'"
            );
            this.binaryStream.write(0xF0 | (uc >> 18));
            this.binaryStream.write(0x80 | ((uc >> 12) & 0x3F));
            this.binaryStream.write(0x80 | ((uc >> 6) & 0x3F));
            this.binaryStream.write(0x80 | (uc & 0x3F));
            pending = 0;
        }

    }

    /**
     * Non-buffering write
     */
    @Override
    public final void write(
        char utf16[]
    ) throws IOException {
        for (int i = 0; i < utf16.length; i++) {
            write(utf16[i]);
        }
    }

    /**
     * Immediately delegating write
     */
    @Override
    public final void write(
        char utf16[], 
        int offset, 
        int length
    ) throws IOException {
        for (int i = 0; i < length; i++) {
            write(utf16[offset + i]);
        }
    }

    /**
     * Non-buffering write
     */
    @Override
    public final void write(
        String utf16
    ) throws IOException {
        final int len = utf16.length();
        for (int i = 0; i < len; i++) {
            write(utf16.charAt(i));
        }
    }

    /**
     * Non-buffering write
     */
    @Override
    public final void write(
        String str, 
        int off, 
        int len
    ) throws IOException {
        for (int i = 0; i < len; i++) {
            write(str.charAt(off + i));
        }
    }
    /**
     * Immediately delegating write
     */

    /**
     * Non-delegating flush
     */
    @Override
    public final void flush(
    ) throws IOException {
        if (this.pending != 0) {
            throw new IOException(
               "Unable to flush an incomplete UNICODE code point"
            );
        }
    }

    /**
     * Non-delegating close
     */
    @Override
    public final void close(
    ) throws IOException {
        flush();
        this.binaryStream = null;
    }

}
