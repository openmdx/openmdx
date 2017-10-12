/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: AbstractXMLStreamWriter
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
 * Copyright 2006 Envoi Solutions LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.base.json.stream;

import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public abstract class AbstractXMLStreamWriter implements XMLStreamWriter {
	
	ArrayList<String> serializedAsArrays = new ArrayList<String>();

    public void writeCData(String text) throws XMLStreamException {
        writeCharacters(text);
    }

    public void writeCharacters(char[] arg0, int arg1, int arg2) throws XMLStreamException {
        writeCharacters(new String(arg0, arg1, arg2));
    }
    
    public void writeEmptyElement(String prefix, String local, String ns) throws XMLStreamException {
        writeStartElement(prefix, local, ns);
        writeEndElement();
    }

    public void writeEmptyElement(String ns, String local) throws XMLStreamException {
        writeStartElement(local, ns);
        writeEndElement();
    }

    public void writeEmptyElement(String local) throws XMLStreamException {
        writeStartElement(local);
        writeEndElement();
    }

    public void writeStartDocument(String arg0, String arg1) throws XMLStreamException {
        writeStartDocument();
    }

    public void writeStartDocument(String arg0) throws XMLStreamException {
        writeStartDocument();
    }

    public void writeStartElement(String ns, String local) throws XMLStreamException {
        writeStartElement("", local, ns);
    }

    public void writeStartElement(String local) throws XMLStreamException {
        writeStartElement("", local, "");
    }

    public void writeComment(String arg0) throws XMLStreamException {
        // nothing to do
    }

    public void writeDTD(String arg0) throws XMLStreamException {
        // nothing to do
    }

    public void writeEndDocument() throws XMLStreamException {
        // nothing to do
    }
    
    public void serializeAsArray(String name) {
    	serializedAsArrays.add(name);
    }
    
    /**
     * @deprecated since 1.2 because of misspelling. Use serializeAsArray(String name) instead.
     */
    @Deprecated
    public void seriliazeAsArray(String name) {
    	serializedAsArrays.add(name);
    }
    
    public ArrayList<String> getSerializedAsArrays() {
    	return serializedAsArrays;
    }

}
