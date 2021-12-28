/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Adaptive InputStream Reader Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2021, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.xml;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test Adaptive InputStream Reader
 */
public class AdaptiveInputStreamReaderTest {

    /**
     * Check a resource file's content
     * 
     * @param file
     * @param content
     * @throws IOException 
     */
    protected void validateEncoding(
        String file,
        String content,
        String byteOrderMark,
        String xmlDeclaration
    ) throws IOException { 
        try (
            AdaptiveInputStreamReader r = new AdaptiveInputStreamReader(
                AdaptiveInputStreamReaderTest.class.getResource(
                    file
                ).openStream(),
                null, // encoding
                true, // byteOrderMarkAware
                true, // xmlDeclarationAware
                true // propagate close
            )
        ){
    //      assertEquals("ByteOrderMark", byteOrderMark, r.getByteOrderMark());
    //      assertEquals("XMLDeclaration", xmlDeclaration, String.valueOf(r.getXMLDclaration()));
            for(
                int i = 0;
                i < content.length();
                i++
            ) {
                Assertions.assertEquals(
                    content.charAt(i),
                    r.read(),
                    "Content[" + i + "]"
                );
                    
            }
        }
    }

    /**
     * Test ASCII stream
     * @throws IOException 
     */
    @Test
    public void testASCII() throws IOException{
        validateEncoding(
            "US-ASCII.txt", 
            "ASCII", 
            null, 
            "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
        );
    }

    /**
     * Test ISO Latin 1 stream
     * @throws IOException 
     */
    @Test
    public void testISOLatin1() throws IOException{
        validateEncoding(
            "ISO-8859-1.txt", 
            "ISO Latin-1",
            null,
            "null"
        );
    }

    /**
     * Test UTF-8 stream
     * @throws IOException 
     */
    @Test
    public void testUTF8() throws IOException{
       validateEncoding(
           "UTF-8.txt", 
           "UTF-8",
           "UTF8",
           "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
       );
    }

    /**
     * Test UTF-16 Little Endian stream
     * @throws IOException 
     */
    @Test
    public void testUTF16LE() throws IOException{
       validateEncoding(
           "UTF-16LE.txt", 
           "UTF-16 Little Endian",
           "UnicodeLittleUnmarked",
           "<?xml version=\"1.0\" encoding=\"UTF-16\"?>"
       );
    }
    
    /**
     * Test UTF-16 Big Endian stream
     * @throws IOException 
     */
    @Test
    public void testUTF16BE() throws IOException{
       validateEncoding(
           "UTF-16BE.txt", 
           "UTF-16 Big Endian",
           "UTF8",
           "<?xml version=\"1.0\" encoding=\"UTF-16\"?>"
       );
    }

}
