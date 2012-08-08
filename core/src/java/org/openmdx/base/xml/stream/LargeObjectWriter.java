/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: LargeObjectWriter.java,v 1.1 2010/03/02 18:27:36 hburger Exp $
 * Description: Large Object Writer
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/02 18:27:36 $
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
package org.openmdx.base.xml.stream;

import javax.xml.stream.XMLStreamException;

import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.CharacterLargeObject;

/**
 * Large Object Writer
 */
public interface LargeObjectWriter {

    /**
     * Writes a data section
     * 
     * @param data the data contained in the data Section, may not be null
     * @param offset
     * @param length
     * 
     * @throws XMLStreamException 
     */
    void writeBinaryData(
        byte[] data,
        int offset,
        int length
    ) throws XMLStreamException;
    
    /**
     * Writes a data section
     * 
     * @param data the data contained in the data Section, may not be null
     * 
     * @throws XMLStreamException 
     */
    void writeBinaryData(
        BinaryLargeObject data
    ) throws XMLStreamException;

    /**
     * Writes a data section
     * 
     * @param data the data contained in the data Section, may not be null
     * 
     * @throws XMLStreamException 
     */
    void writeCharacterData(
        CharacterLargeObject data
    ) throws XMLStreamException;

}
