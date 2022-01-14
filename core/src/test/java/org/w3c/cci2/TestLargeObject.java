/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test Large Object 
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
package org.w3c.cci2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test Large Object
 */
public class TestLargeObject {
    
    private static byte[] binaryContent;
    private static char[] characterContent;
    
    @BeforeAll
    public static void setUp(){
        //
        // Binary Content
        //
        binaryContent = new byte[1000];
        for(
            int i = 0;
            i < binaryContent.length;
            i++
        ) {
            binaryContent[i] = (byte)((short)(i % 256));
        }
        //
        // Character Content
        //
        characterContent = new char[1000];
        for(
            int i = 0;
            i < characterContent.length;
            i++
        ) {
            characterContent[i] = (char) (' ' + i);
        }
    }
    
    @Test
    public void testBinaryObject(
    ) throws IOException, ClassNotFoundException {
        validate(BinaryLargeObjects.valueOf(binaryContent));
        validate(BinaryLargeObjects.valueOf(new ByteArrayInputStream(binaryContent)));
    }

    @Test
    public void testCharacterObject(
    ) throws IOException, ClassNotFoundException {
        validate(CharacterLargeObjects.valueOf(characterContent));
        validate(CharacterLargeObjects.valueOf(new CharArrayReader(characterContent)));
    }
    
    private void validate(
        BinaryLargeObject value
    ) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream target = new ObjectOutputStream(buffer);
        target.writeObject(value);
        ObjectInputStream source = new ObjectInputStream(
            new ByteArrayInputStream(buffer.toByteArray())
        );
        BinaryLargeObject copy = (BinaryLargeObject) source.readObject();
        Assertions.assertNotNull(copy.getLength());
        Assertions.assertEquals(binaryContent.length, copy.getLength().intValue());
        InputStream validation = copy.getContent();
        for(
            int i = 0;
            i < binaryContent.length;
            i++
        ) {
        	Assertions.assertEquals(0xFF & binaryContent[i], validation.read());        
        }
        Assertions.assertEquals(-1, validation.read());
    }

    private void validate(
        CharacterLargeObject value
    ) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream target = new ObjectOutputStream(buffer);
        target.writeObject(value);
        ObjectInputStream source = new ObjectInputStream(
            new ByteArrayInputStream(buffer.toByteArray())
        );
        CharacterLargeObject copy = (CharacterLargeObject) source.readObject();
        Assertions.assertNotNull(copy.getLength());
        Assertions.assertEquals(characterContent.length, copy.getLength().intValue());
        Reader validation = copy.getContent();
        for(
            int i = 0;
            i < characterContent.length;
            i++
        ) {
        	Assertions.assertEquals(characterContent[i], validation.read());        
        }
        Assertions.assertEquals(-1, validation.read());
    }

}
