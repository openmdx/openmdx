/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Node
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;


public class Node {
    
    JSONObject object;
    Map<QName,String> attributes;
    Map<String,String> namespaces;
    Iterator<?> keys;
    QName name;
    JSONArray array;
    int arrayIndex;
    String currentKey;
    Node parent;
    
    public Node(Node parent, String name, JSONObject object, Convention con) 
        throws JSONException, XMLStreamException {
        this.parent = parent;
        this.object = object;
        
        /* Should really use a _Linked_ HashMap to preserve
         * ordering (insert order) -- regular one has arbitrary ordering.
         * But there are some funky dependencies within unit tests
         * that will fail right now, need to investigate that bit more
         */
        this.namespaces = new LinkedHashMap<String,String>();
        this.attributes = new LinkedHashMap<QName,String>();
        
        con.processAttributesAndNamespaces(this, object);
        
        keys = object.keys();

        this.name = con.createQName(name, this);
    }

    public Node(String name, Convention con) throws XMLStreamException {
        this.name = con.createQName(name, this);
        this.namespaces = new HashMap<String,String>();
        this.attributes = new HashMap<QName,String>();
    }

    public Node(JSONObject object) {
        this.object = object;
        this.namespaces = new HashMap<String,String>();
        this.attributes = new HashMap<QName,String>();
    }

    public int getNamespaceCount() {
        return namespaces.size();
    }

    public String getNamespaceURI(String prefix) {
        String result = namespaces.get(prefix);
        if (result == null && parent != null) {
            result = parent.getNamespaceURI(prefix);
        }
        return result;
    }

    public String getNamespaceURI(final int index) {
        if (index < 0 || index >= getNamespaceCount()) {
            throw new IllegalArgumentException("Illegal index: element has "+getNamespaceCount()+" namespace declarations");
        }
        Iterator<?> itr = namespaces.values().iterator();
        int i = index;
        while (--i >= 0) {
            itr.next();
        }
        Object ns = itr.next();
        return ns == null ? "" : ns.toString();
    }

    public String getNamespacePrefix(String URI) {
        String result = null;
        for (Iterator<?> nsItr = namespaces.entrySet().iterator(); nsItr.hasNext();) {
            Map.Entry<?,?> e = (Map.Entry<?,?>) nsItr.next();
            if (e.getValue().equals(URI)) {
                result = (String) e.getKey();
            }
        }
        if (result == null && parent != null) {
            result = parent.getNamespacePrefix(URI);
        }
        return result;
    }

    public String getNamespacePrefix(final int index) {
        if (index < 0 || index >= getNamespaceCount()) {
            throw new IllegalArgumentException("Illegal index: element has "+getNamespaceCount()+" namespace declarations");
        }
        Iterator<?> itr = namespaces.keySet().iterator();
        int i = index;
        while (--i >= 0) {
            itr.next();
        }
        return itr.next().toString();
    }

    public void setNamespaces(Map<String,String> namespaces) {
        this.namespaces = namespaces;
    }

    public void setNamespace(String prefix, String uri) {
        namespaces.put(prefix, uri);
    }

    public Map<QName,String> getAttributes() {
        return attributes;
    }

    public void setAttribute(QName name, String value) {
        attributes.put(name, value);
    }

    public Iterator<?> getKeys() {
        return keys;
    }

    public QName getName() {
        return name;
    }

    public JSONObject getObject() {
        return object;
    }

    public void setObject(JSONObject object) {
        this.object = object;
    }

    public JSONArray getArray() {
        return array;
    }

    public void setArray(JSONArray array) {
        this.array = array;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }

    public void setArrayIndex(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    public String getCurrentKey() {
        return currentKey;
    }

    public void setCurrentKey(String currentKey) {
        this.currentKey = currentKey;
    }
    
    public String toString() {
		if (this.name != null) {
			return this.name.toString();
		} else {
			return super.toString();
		}
	}
    
}
