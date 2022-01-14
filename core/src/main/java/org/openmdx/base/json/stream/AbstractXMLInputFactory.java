/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: AbstractXMLInputFactory
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

import java.io.*;

import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public abstract class AbstractXMLInputFactory extends XMLInputFactory {

    private final static int INPUT_BUF_SIZE = 1024;

    private int bufSize = INPUT_BUF_SIZE;
    
    protected AbstractXMLInputFactory() {
    	
    }
    
    protected AbstractXMLInputFactory(int bufSize) {
    	this.bufSize = bufSize;
    }
    
    public XMLEventReader createFilteredReader(XMLEventReader arg0, EventFilter arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLStreamReader createFilteredReader(XMLStreamReader arg0, StreamFilter arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(InputStream arg0, String encoding) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(InputStream arg0) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(Reader arg0) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(Source arg0) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(String systemId, InputStream arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(String systemId, Reader arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(XMLStreamReader arg0) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLStreamReader createXMLStreamReader(InputStream is) throws XMLStreamException {
        return createXMLStreamReader(is, null);
    }

    public XMLStreamReader createXMLStreamReader(final InputStream is, final String characterSet) throws XMLStreamException {
        String charset = characterSet;
        /* !!! This is not really correct: should (try to) auto-detect
         * encoding, since JSON only allows 3 Unicode-based variants.
         * For now it's ok to default to UTF-8 though.
         */
        if (charset == null) {
            charset = "UTF-8";
        }
        try {
            String doc = readAll(is, charset);
            return createXMLStreamReader(createNewJSONTokener(doc));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    protected JSONTokener createNewJSONTokener(String doc) {
    	return new JSONTokener(doc);
    }
    
    /**
     * This helper method tries to read and decode input efficiently
     * into a result String.
     */
    private String readAll(InputStream in, String encoding)
        throws IOException
    {
        final byte[] buffer = new byte[bufSize];
        ByteArrayOutputStream bos = null;
        while (true) {
            int count = in.read(buffer);
            if (count < 0) { // EOF
                break;
            }
            /* Let's create buffer lazily, to be able to create something
             * that's not too small (many resizes) or too big (slower
             * to allocate): mostly to speed up handling of tiny docs.
             */
            if (bos == null) {
                int cap;
                if (count < 64) {
                    cap = 64;
                } else if (count == bufSize) {
                    // Let's assume there's more coming, not just this chunk
                    cap = bufSize * 4;
                } else {
                    cap = count;
                }
                bos = new ByteArrayOutputStream(cap);
            }
            bos.write(buffer, 0, count);
        }
        return (bos == null) ? "" : bos.toString(encoding);
    }
    
    public abstract XMLStreamReader createXMLStreamReader(JSONTokener tokener) throws XMLStreamException;

    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        try {
            return createXMLStreamReader(new JSONTokener(readAll(reader)));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    private String readAll(Reader r)
        throws IOException
    {
        // Let's see if it's a small doc, can read it all in a single buffer
        char[] buf = new char[bufSize];
        int len = 0;

        do {
            int count = r.read(buf, len, buf.length-len);
            if (count < 0) { // Got it all
                return (len == 0) ? "" : new String(buf, 0, len);
            }
            len += count;
        } while (len < buf.length);

        /* Filled the read buffer, need to coalesce. Let's assume there'll
         * be a bit more data coming
         */
        CharArrayWriter wrt = new CharArrayWriter(bufSize * 4);
        wrt.write(buf, 0, len);

        while ((len = r.read(buf)) != -1) {
            wrt.write(buf, 0, len);
        }
        return wrt.toString();
    }
    
    public XMLStreamReader createXMLStreamReader(Source src) throws XMLStreamException
    {
        // Can only support simplest of sources:
        if (src instanceof StreamSource) {
            StreamSource ss = (StreamSource) src;
            InputStream in = ss.getInputStream();
            String systemId = ss.getSystemId();
            if (in != null) {
                if (systemId != null) {
                    return createXMLStreamReader(systemId, in);
                }
                return createXMLStreamReader(in);
            }
            Reader r = ss.getReader();
            if (r != null) {
                if (systemId != null) {
                    return createXMLStreamReader(systemId, r);
                }
                return createXMLStreamReader(r);
            }
            throw new UnsupportedOperationException("Only those javax.xml.transform.stream.StreamSource instances supported that have an InputStream or Reader");
        }
        throw new UnsupportedOperationException("Only javax.xml.transform.stream.StreamSource type supported");
    }

    
    public XMLStreamReader createXMLStreamReader(String systemId, InputStream arg1) throws XMLStreamException {
        // How (if) should the system id be used?
        return createXMLStreamReader(arg1, null);
    }

    
    public XMLStreamReader createXMLStreamReader(String systemId, Reader r) throws XMLStreamException {
        return createXMLStreamReader(r);
    }

    
    public XMLEventAllocator getEventAllocator() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public Object getProperty(String arg0) throws IllegalArgumentException {
        // TODO: should gracefully handle standard properties
        throw new IllegalArgumentException();
    }

    
    public XMLReporter getXMLReporter() {
        return null;
    }

    
    public XMLResolver getXMLResolver() {
        return null;
    }

    
    public boolean isPropertySupported(String arg0) {
        return false;
    }

    
    public void setEventAllocator(XMLEventAllocator arg0) {
        // TODO Auto-generated method stub
        
    }

    
    public void setProperty(String arg0, Object arg1) throws IllegalArgumentException {
        // TODO: should gracefully handle standard properties
        throw new IllegalArgumentException();
    }

    
    public void setXMLReporter(XMLReporter arg0) {
        // TODO Auto-generated method stub
        
    }

    
    public void setXMLResolver(XMLResolver arg0) {
        // TODO Auto-generated method stub
        
    }

}
