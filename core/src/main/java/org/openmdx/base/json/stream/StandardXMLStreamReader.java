/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: StandardXMLStreamReader
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

import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

public class StandardXMLStreamReader extends AbstractXMLStreamReader {
    private FastStack nodes;
    private String currentValue;
    private StandardNamespaceConvention convention;
    private String valueKey = "$";
    private static final String TYPE_TAG = "@type";
    private NamespaceContext ctx;
    private int popArrayNodes;

    public StandardXMLStreamReader(
        JSONObject obj
    ) throws JSONException, XMLStreamException {
        this(obj, new StandardNamespaceConvention());
    }

    private static JSONObject mapTypeTagRecursively(
        JSONObject obj
    ) throws JSONException {
        JSONObject mappedObj = obj;
        if(obj.has(TYPE_TAG)) {
            mappedObj = new JSONObject();
            String typeName = obj.getString(TYPE_TAG);
            obj.remove(TYPE_TAG);
            mappedObj.put(typeName, obj);
            for(String key: obj.keySet()) {
                Object value = obj.get(key);
                if(value instanceof JSONArray) {
                    JSONArray array = (JSONArray)value;
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject element = array.getJSONObject(i);
                        array.put(i, mapTypeTagRecursively(element));
                    }                
                } else if(value instanceof JSONObject) {
                    obj.put(key, mapTypeTagRecursively((JSONObject)value));
                }
            }
        }
        return mappedObj;
    }

    public StandardXMLStreamReader(
        JSONObject obj, 
        StandardNamespaceConvention con
   ) throws JSONException, XMLStreamException {
        JSONObject mappedObj = mapTypeTagRecursively(obj);
        String rootName = mappedObj.keys().next();
        this.convention = con;
        this.nodes = new FastStack();
        this.ctx = con;
        Object top = mappedObj.get(rootName);
        if (top instanceof JSONObject) {
            this.node = new Node(null, rootName, (JSONObject)top, convention);
        } else if (top instanceof JSONArray && !(((JSONArray)top).length() == 1 && ((JSONArray)top).get(0).equals(""))) {
            this.node = new Node(null, rootName, mappedObj, convention);
        } else {
            node = new Node(rootName, convention);
            convention.processAttributesAndNamespaces(node, mappedObj);
            currentValue = JSONObject.NULL.equals(top) ? null : top.toString();
        }
        nodes.push(node);
        event = START_DOCUMENT;
    }

    public int next() throws XMLStreamException {
        if (event == START_DOCUMENT) {
            event = START_ELEMENT;
        } else if (event == CHARACTERS) {
            event = END_ELEMENT;
            node = (Node) nodes.pop();
            currentValue = null;
        } else if (event == START_ELEMENT || event == END_ELEMENT) {
            if (event == END_ELEMENT && nodes.size() > 0) {
                node = (Node) nodes.peek();
                if (popArrayNodes > 0) {
                	nodes.pop();
                	if (node.getArray() != null) {
                		popArrayNodes--;
                		event = END_ELEMENT;
                		return event;
                	}
                }
            }
            if (currentValue != null) {
                event = CHARACTERS;
            } else if ((node.getKeys() != null && node.getKeys().hasNext()) || node.getArray() != null) {
                processElement();
            } else {
                if (nodes.size() > 0) {
                    event = END_ELEMENT;
                    node = (Node) nodes.pop();
                } else {
                    event = END_DOCUMENT;
                }
            }
        }
        // handle value in nodes with attributes
        if (nodes.size() > 0) {
        	Node next = (Node)nodes.peek();
        	if (event == START_ELEMENT && next.getName().getLocalPart().equals(valueKey)) {
        		event = CHARACTERS;
        		node = (Node)nodes.pop();
        	}
        }
        return event;
    }

    private void processElement() throws XMLStreamException {
        try {
            Object newObj = null;
            String nextKey = null;
            if (node.getArray() != null) {
                int index = node.getArrayIndex();
                if (index >= node.getArray().length()) {
                	
            		nodes.pop();

                	node = (Node) nodes.peek();

                	if (node == null)
                	{
                		event = END_DOCUMENT;
                		return;
                	}
                    
                    if ((node.getKeys() != null && node.getKeys().hasNext()) || node.getArray() != null) {
                        if (popArrayNodes > 0) {
                        	node = (Node) nodes.pop();
                        }
                    	processElement();
                    }
                    else {
                    	    event = END_ELEMENT;
                            node = (Node) nodes.pop();
                    }
                    return;
                }
                newObj = node.getArray().get(index++);
                nextKey = node.getName().getLocalPart();
                if (!"".equals(node.getName().getNamespaceURI())) {
                    nextKey = this.convention.getPrefix(node.getName().getNamespaceURI()) + "." + nextKey;
                }
                node.setArrayIndex(index);
            } else {
                nextKey = (String) node.getKeys().next();
                newObj = node.getObject().get(nextKey);
            }
            if (newObj instanceof String) {
                node = new Node(nextKey, convention);
                nodes.push(node);
                currentValue = (String) newObj;
                event = START_ELEMENT;
                return;
            } else if (newObj instanceof JSONArray) {
            	JSONArray array = (JSONArray) newObj;
                if (!processUniformArrayIfPossible(nextKey, array)) {
	            	node = new Node(nextKey, convention);
	                node.setArray(array);
	                node.setArrayIndex(0);
	                nodes.push(node);
                    processElement(); 
                }
                return;
            } else if (newObj instanceof JSONObject) {
                node = new Node((Node)nodes.peek(), nextKey, (JSONObject) newObj, convention);
                nodes.push(node);
                event = START_ELEMENT;
                return;
            } else {
                node = new Node(nextKey, convention);
                nodes.push(node);
                currentValue = JSONObject.NULL.equals(newObj) ? null : newObj.toString();
                event = START_ELEMENT;
                return;
            }
        } catch (JSONException e) {
            throw new XMLStreamException(e);
        }
    }

    private boolean processUniformArrayIfPossible(String arrayKey, JSONArray array) throws JSONException, XMLStreamException {
    	
    	if (!isAvoidArraySpecificEvents(arrayKey)) {
    		return false;
    	}
    	
    	int arrayLength = array.length();
    	int depth = 0;
    	String lastKey = null;
    	int parentIndex = nodes.size();
    	boolean isRoot = ((Node)nodes.get(0)).getName().getLocalPart().equals(arrayKey);
    	Node parent = !isRoot ? new Node(arrayKey, convention) : node;
        
    	for (int i = arrayLength - 1; i >= 0; i--) {
    		Object object = array.get(i);
    		if (object instanceof JSONObject) {
    			JSONObject jsonObject = (JSONObject)object;
    			// lets limit to single key JSONObjects for now
    			if (jsonObject.length() == 1) { 
    				String theKey = jsonObject.keys().next().toString();
    				if (lastKey == null || lastKey.equals(theKey)) {
    					lastKey = theKey;
    					depth++;
    					Node theNode = new Node(parent, theKey, jsonObject, convention);
    	                nodes.push(theNode);
    	            } else {
    					lastKey = null;
    					break;
    				}
    			}
    		}
    	}
    	if (lastKey == null) {
    		for (int i = 0; i < depth; i++) {
    			nodes.pop();
    		}
    		return false;
    	}
    	
    	parent.setArray(array);
        parent.setArrayIndex(arrayLength);
        if (!isRoot) {
            nodes.add(parentIndex, parent);
            nodes.push(parent);
            node = parent;
            event = START_ELEMENT;
        } else {
        	node = (Node)nodes.pop();
        	processElement();
        }
        popArrayNodes++;
        return true;
    }
    
    public void close() throws XMLStreamException {
        // nothing to do
    }

    public String getElementText() throws XMLStreamException {
        event = CHARACTERS;
        return currentValue;
    }

    public NamespaceContext getNamespaceContext() {
        return ctx;
    }

    public String getText() {
        return currentValue;
    }
    
	public void setValueKey(String valueKey) {
		this.valueKey = valueKey;
	}

	public boolean isAvoidArraySpecificEvents(String key) {
		Set<?> keys = convention.getPrimitiveArrayKeys();
		return keys != null && keys.contains(key);
	}
}
