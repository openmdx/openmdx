/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StringUnicodeTransformer_1.java,v 1.1 2009/05/26 13:10:13 wfro Exp $
 * Description: SPICE Character Conversions: String Unicode Transformation 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/26 13:10:13 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.base.text.conversion;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;

import org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0;

/**
 * @author hburger
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class StringUnicodeTransformer_1 
    extends AbstractUnicodeTransformer_1
    implements UnicodeTransformer_1_0 
{

    /**
     * 
     */
    public StringUnicodeTransformer_1() {
        super();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0#utf8Array(char[], int, int)
     */
    public byte[] utf8Array(char[] source, int offset, int length) {
        return utf8Array(new String(source, offset, length));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0#utf8Array(java.lang.String)
     */
    public byte[] utf8Array(String source) {
        try {
            return source.getBytes(CHARSET_NAME);
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeException("Lack of " + CHARSET_NAME + " support");
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0#utf16Array(byte[], int, int)
     */
    public char[] utf16Array(byte[] source, int offset, int length) {
        return utf16String(source,offset,length).toCharArray();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0#utf16String(byte[], int, int)
     */
    public String utf16String(byte[] source, int offset, int length) {
        try {
            return new String(source, offset, length, CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Lack of " + CHARSET_NAME + " support");
        }
    }

    /**
     * UTF-8 -> UTF-16
     * 
     * @param in
     */
    public Reader utf8Reader(
        InputStream in
    ){
        try {
            return new InputStreamReader(in, CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Lack of " + CHARSET_NAME + " support");
        }
    }

    /**
     * UTF-16 -> UTF-8
     * 
     * @param out
     */
    public Writer utf8Writer(
        OutputStream out
    ){
        try {
            return new OutputStreamWriter(out, CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Lack of " + CHARSET_NAME + " support");
        }
    }


    //------------------------------------------------------------------------
    // Class features
    //------------------------------------------------------------------------

    /**
     * This transformer is based on built-in UTF-8 support.
     */
    static String CHARSET_NAME = "UTF-8";
    
    /**
     * Encoded ISO-8859-1 test element  
     */
    final static private byte[] ENCODED = new byte[]{(byte)0xc3,(byte)0xa4};

    /**
     * Decoded ISO-8859-1 test element  
     */
    final static private String DECODED = "\u00e4";
        
    /**
     * 
     */
    public static boolean isSupported(
    ){
        try {
            return Arrays.equals(
                DECODED.getBytes(StringUnicodeTransformer_1.CHARSET_NAME), 
                ENCODED
            ) && DECODED.equals(
                new String(ENCODED,StringUnicodeTransformer_1.CHARSET_NAME)
            );
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        
    }

    /**
     * To verify a JDK's native UTF-8 support
     * 
     * @param arguments
     */
    public static void main(
        String[] arguments
    ){
        System.out.println (
            "StringUnicodeTransformer_1 is " + (isSupported() ? "" : " not") +  "supported"
        );
    }

}
