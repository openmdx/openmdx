/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: AbstractXMLStreamReader
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

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class AbstractXMLStreamReader implements XMLStreamReader {
    protected int event;
    protected Node node;
    
    public boolean isAttributeSpecified(int arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isCharacters() {
        return event == CHARACTERS;
    }

    public boolean isEndElement() {
        return event == END_ELEMENT;
    }

    public boolean isStandalone() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isStartElement() {
        return event == START_ELEMENT;
    }

    public boolean isWhiteSpace() {
        return false;
    }

    public int nextTag() throws XMLStreamException {
        int event = next();
        while (event != START_ELEMENT && event != END_ELEMENT) {
            event = next();
        }
        return event;
    }

    public int getEventType() {
        return event;
    }

    public void require(int arg0, String arg1, String arg2) throws XMLStreamException {
        // nothing to do
    }

    public int getAttributeCount() {
        return node.getAttributes().size();
    }

    public String getAttributeLocalName(int n) {
        return getAttributeName(n).getLocalPart();
    }

    public QName getAttributeName(int n) {
        Iterator<?> itr = node.getAttributes().keySet().iterator();
        QName name = null;
        for (int i = 0; i <= n; i++) {
            name = (QName) itr.next();
        }
        return name;
    }

    public String getAttributeNamespace(int n) {
        return getAttributeName(n).getNamespaceURI();
    }

    public String getAttributePrefix(int n) {
        return getAttributeName(n).getPrefix();
    }

    public String getAttributeValue(int n) {
        Iterator<?> itr = node.getAttributes().values().iterator();
        String name = null;
        for (int i = 0; i <= n; i++) {
            name = (String) itr.next();
        }
        return name;
    }

    public String getAttributeValue(String ns, String local) {
        return node.getAttributes().get(new QName(ns, local));
    }

    public String getAttributeType(int arg0) {
        return null;
    }

    public String getLocalName() {
        return getName().getLocalPart();
    }

    public QName getName() {
        return node.getName();
    }

    public String getNamespaceURI() {
        return getName().getNamespaceURI();
    }

    public int getNamespaceCount() {
        return node.getNamespaceCount();
    }

    public String getNamespacePrefix(int n) {
        return node.getNamespacePrefix(n);
    }

    public String getNamespaceURI(int n) {
        return node.getNamespaceURI(n);
    }

    public String getNamespaceURI(String prefix) {
        return node.getNamespaceURI(prefix);
    }

    public boolean hasName() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasNext() throws XMLStreamException {
        return event != END_DOCUMENT;
    }

    public boolean hasText() {
        return event == CHARACTERS;
    }

    public boolean standaloneSet() {
        return false;
    }

    public String getCharacterEncodingScheme() {
        return null;
    }

    public String getEncoding() {
        return null;
    }

    public Location getLocation() {
        return new Location() {

            public int getCharacterOffset() {
                return 0;
            }

            public int getColumnNumber() {
                return 0;
            }

            public int getLineNumber() {
                return -1;
            }

            public String getPublicId() {
                return null;
            }

            public String getSystemId() {
                return null;
            }
            
        };
    }

    public String getPIData() {
        return null;
    }

    public String getPITarget() {
        return null;
    }

    public String getPrefix() {
        return getName().getPrefix();
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
        return null;
    }

    public String getVersion() {
        return null;
    }

    public char[] getTextCharacters() {
    	String text = getText();
        return text != null ? text.toCharArray() : new char[]{};
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
    	String text = getText();
    	if (text != null) {
            text.getChars(sourceStart,sourceStart+length,target,targetStart);
            return length;
    	} else {
    		return 0;
    	}
    }

    public int getTextLength() {
    	String text = getText();
        return text != null ? text.length() : 0;
    }

    public int getTextStart() {
        return 0;
    }

}
