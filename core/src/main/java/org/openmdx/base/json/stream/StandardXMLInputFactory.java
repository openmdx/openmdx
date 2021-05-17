/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: StandardXMLInputFactory
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

import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.openmdx.base.rest.cci.QueryRecord;

public class StandardXMLInputFactory extends AbstractXMLInputFactory {


    private StandardNamespaceConvention convention;

    public StandardXMLInputFactory(Map<?,?> nstojns) {
        this(new Configuration(nstojns));
    }
    
    public StandardXMLInputFactory(Configuration config) {
        this.convention = new StandardNamespaceConvention(config);
    }
    
    private JSONObject newQuery(
    ) throws JSONException {
        return new JSONObject().put(
            QueryRecord.NAME.replace(":", "."),
            new JSONObject()
        );
    }

    public XMLStreamReader createXMLStreamReader(
        JSONTokener tokener
    ) throws XMLStreamException {
        try {
            JSONObject root = tokener.more() 
                ? createJSONObject(tokener)
                : newQuery();
            return new StandardXMLStreamReader(root, convention);
        } catch (JSONException e) {
            int column = e.getColumn();
            if (column == -1) {
                throw new XMLStreamException(e);
            } else {
                throw new XMLStreamException(
                    e.getMessage(),
                    new ErrorLocation(e.getLine(), e.getColumn()),
                    e
                );
            }
        }
    }
    
    protected JSONObject createJSONObject(JSONTokener tokener) throws JSONException {
    	return new JSONObject(tokener);
    }
    
    private static class ErrorLocation implements Location {

    	private int line = -1;
        private int column = -1;
    	
        public ErrorLocation(int line, int column) {
            this.line = line;
            this.column = column;
        }
        
		public int getCharacterOffset() {
			return 0;
		}

		public int getColumnNumber() {
			return column;
		}

		public int getLineNumber() {
			return line;
		}

		public String getPublicId() {
			return null;
		}

		public String getSystemId() {
			return null;
		}
    	
    }
}
