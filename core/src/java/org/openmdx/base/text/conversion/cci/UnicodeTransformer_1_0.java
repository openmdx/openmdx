/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UnicodeTransformer_1_0.java,v 1.5 2004/08/05 10:30:46 hburger Exp $
 * Description: openMDX Character Conversions: Unicode Transformation Interface
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/08/05 10:30:46 $
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
package org.openmdx.base.text.conversion.cci;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * openMDX Character Conversions
 * Unicode Transformation Interface
 */
public interface UnicodeTransformer_1_0 {

    /**
     * UTF-16 -> UTF-8
     * 
     * @param source
     * @param offset
     * @param length
     */
    byte[] utf8Array(
        char[] source,
        int offset,
        int length
    );

    /**
     * UTF-16 -> UTF-8
     * 
     * @param source
     * @param offset
     * @param length
     */
    byte[] utf8Array(
        String source
    );

    /**
     * UTF-32 -> UTF-8
     * 
     * @param source
     * @param offset
     * @param length
     */
    byte[] utf8Array(
        int[] source,
        int offset,
        int length
    );

    /**
     * UTF-8 -> UTF-16
     * 
     * @param source
     * @param offset
     * @param length
     */
    char[] utf16Array(
        byte[] source,
        int offset,
        int length
    );
        
    /**
     * UTF-8 -> UTF-16
     * 
     * @param source
     * @param offset
     * @param length
     */
    String utf16String(
        byte[] source,
        int offset,
        int length
    );

    /**
     * UTF-32 -> UTF-16
     * 
     * @param source
     * @param offset
     * @param length
     */
    char[] utf16Array(
        int[] source,
        int offset,
        int length
    );

    /**
     * UTF-8 -> UTF-32
     * 
     * @param source
     * @param offset
     * @param length
     */
    int[] utf32Array(
        byte[] source,
        int offset,
        int length
    );
        
    /**
     * UTF-16 -> UTF-32
     * 
     * @param source
     * @param offset
     * @param length
     */
    int[] utf32Array(
        char[] source,
        int offset,
        int length
    );

    /**
     * UTF-16 -> UTF-32
     * 
     * @param source
     */
    int[] utf32Array(
        String source
    );

    /**
     * UTF-8 -> UTF-16
     * 
     * @param in
     */
    Reader utf8Reader(
        InputStream in
    );

    /**
     * UTF-16 -> UTF-8
     * 
     * @param out
     */
    Writer utf8Writer(
        OutputStream out
    );  

}
