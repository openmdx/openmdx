/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: AbstractXMLOutputFactory
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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;


public abstract class AbstractXMLOutputFactory extends XMLOutputFactory {

    public XMLEventWriter createXMLEventWriter(OutputStream out, String charset) throws XMLStreamException {
        return new AbstractXMLEventWriter(createXMLStreamWriter(out, charset));
    }

    public XMLEventWriter createXMLEventWriter(OutputStream out) throws XMLStreamException {
    	return new AbstractXMLEventWriter(createXMLStreamWriter(out));
    }

    public XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException {
    	return new AbstractXMLEventWriter(createXMLStreamWriter(result));
    }

    public XMLEventWriter createXMLEventWriter(Writer writer) throws XMLStreamException {
    	return new AbstractXMLEventWriter(createXMLStreamWriter(writer));
    }

    public XMLStreamWriter createXMLStreamWriter(final OutputStream out, final String characterSet) throws XMLStreamException {
        String charset = characterSet;
        if (charset == null) {
            charset = "UTF-8";
        }
        try {
            return createXMLStreamWriter(new OutputStreamWriter(out, charset));
        } catch (UnsupportedEncodingException e) {
            throw new XMLStreamException(e);
        }
    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream out) throws XMLStreamException {
        return createXMLStreamWriter(out, null);
    }

    public XMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException {
        // Can only support simplest of Result impls:
        if (result instanceof StreamResult) {
            StreamResult sr = (StreamResult) result;
            OutputStream out = sr.getOutputStream();
            if (out != null) {
                return createXMLStreamWriter(out);
            }
            Writer w = sr.getWriter();
            if (w != null) {
                return createXMLStreamWriter(w);
            }
            throw new UnsupportedOperationException("Only those javax.xml.transform.stream.StreamResult instances supported that have an OutputStream or Writer");
        }
        throw new UnsupportedOperationException("Only javax.xml.transform.stream.StreamResult type supported");
    }

    public abstract XMLStreamWriter createXMLStreamWriter(Writer writer) throws XMLStreamException;

    public Object getProperty(String arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isPropertySupported(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setProperty(String arg0, Object arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        
    }

}
