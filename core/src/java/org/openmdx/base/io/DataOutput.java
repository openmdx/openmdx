/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: DataOutput.java,v 1.4 2008/01/04 23:22:10 hburger Exp $
 * Description: DataOutput Interface
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/04 23:22:10 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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

/**
 * DataOutput Interface
 */
public interface DataOutput
    extends java.io.DataOutput
{

    /**
     * Writes<ul>
     * <li>if the value is <code>null</code><ol>
     * <li>-1 as long
     * </ol>
     * <li>if the value is non-<code>null</code><ol>
     * <li>the length of the string as long
     * <li>the string's content in UTF-16BE encoding
     * </ul>
     * @param value the String to be written
     * 
     * @throws IOException
     */
    void writeString(
        String value
    ) throws IOException;

    /**
     * Writes<ol>
     * <li>the length of the array as short
     * <li>the arrays members
     * </ul>
     * @param value the string array
     * 
     * @throws IOException
     */
    void writeStrings(
        String[] value
    ) throws IOException;
    
    /**
     * Writes<ul>
     * <li>for the first occurance of a given value<ol>
     * <li>the string's transient id as short
     * <li>the length of the string as long
     * <li>the string's content in UTF-16BE
     * </ol>
     * <li>for all later occurances of a given value<ol>
     * <li>the string's transient id as unsigned short
     * </ol> 
     * </ul>
     * @param value the String to be written
     * 
     * @throws IOException
     */
    void writeInternalizedString(
        String value
    ) throws IOException;

    /**
     * Writes<ol>
     * <li>the length of the array as short
     * <li>the arrays members
     * </ul>
     * @param value the string array
     * 
     * @throws IOException
     */
    void writeInternalizedStrings(
        String[] value
    ) throws IOException;
    
    /**
     * Write<ol>
     * <li>the number's type
     * <li>the number's type specific representation unless the value is <code>null</code>
     * </ol>
     * 
     * @param value the number to be written
     */
    void writeNumber(
        Number value
    ) throws IOException;
    
    /**
     * Write<ol>
     * <li>the length of the array as short
     * <li>the arrays members
     * </ol>
     * 
     * @param value the numbers to be written
     */
    void writeNumbers(
        Number[] value
    ) throws IOException;
    
}
