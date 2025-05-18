/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: StandardXMLStreamWriter
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

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

import org.openmdx.kernel.exception.Throwables;

public class StandardXMLStreamWriter extends AbstractXMLStreamWriter {
    
	private static final String MIXED_CONTENT_VALUE_KEY = "$";
    private static final String OBJECTS_KEY = "objects";
	StandardNamespaceConvention convention;
	protected Writer writer;
	private NamespaceContext namespaceContext;

	/**
	 * What key is used for text content, when an element has both text and
	 * other content?
	 */
	String valueKey = MIXED_CONTENT_VALUE_KEY;
	/** Stack of open elements. */
	private Stack<JSONProperty> stack = new Stack<JSONProperty>();
	/** Element currently being processed. */
	private JSONProperty current;

	/**
	 * JSON property currently being constructed. For efficiency, this is
	 * concretely represented as either a property with a String value or an
	 * Object value.
	 */
	private abstract class JSONProperty {
		private String key;
		public JSONProperty(String key) {
			this.key = key;
		}
		/** Get the key of the property. */
		public String getKey() {
			return key;
		}
		/** Get the value of the property */
		public abstract Object getValue();
		/** Add text */
		public abstract void addText(String text);
		/** Return a new property object with this property added */
		public abstract JSONPropertyObject withProperty(JSONProperty property, boolean add);
		public JSONPropertyObject withProperty(JSONProperty property) {
			return withProperty(property, true);
		}
	}

	/**
	 * Property with a String value.
	 * 
	 */
	private final class JSONPropertyString extends JSONProperty {
		private StringBuilder object = null;
		
		public JSONPropertyString(
		    String key
		) {
			super(key);
		}
		
		@Override
		public Object getValue() {
			return object == null ? null : object.toString();
		}
		
		@Override
		public void addText(String text) {
		    if(this.object == null) {
		        this.object = new StringBuilder();
		    }
			object.append(text);
		}
		
		@Override
		public JSONPropertyObject withProperty(
		    JSONProperty property, 
		    boolean add
		) {
			// Duplicate some code from JSONPropertyObject
			// because we can do things with fewer checks, and
			// therefore more efficiently.
			JSONObject jo = new JSONObject();
			try {
				// only add the text property if it's non-empty
				Object value = this.getValue();
				if (MIXED_CONTENT_VALUE_KEY == valueKey) {
				    if(value != null) {
				        String strValue = value.toString().trim();
				        if(!strValue.isEmpty()) {
				            jo.put(valueKey, strValue);
				        }
				    }
				}
				value = property.getValue();
				if(add && value instanceof String) {
				    value = convention.convertToJSONPrimitive((String)value);
				}
				if(getSerializedAsArrays().contains(property.getKey())) {
				    JSONArray values = new JSONArray();
				    values.put(value);
				    value = values;
				}
				jo.put(property.getKey(), value);
			} catch (JSONException e) {
				// Impossible by construction
				throw new AssertionError(e);				
			}
			return new JSONPropertyObject(getKey(), jo);
		}
	}

	/**
	 * Property with a JSONObject value.
	 * 
	 */
	private final class JSONPropertyObject extends JSONProperty {
	    private JSONObject object;
	    public JSONPropertyObject(String key, JSONObject object) {
	        super(key);
	        this.object = object;
	    }
	    
	    @Override
	    public Object getValue() {
	        return object;
	    }
	    
	    @Override
	    public void addText(final String t) {
	        String text = t;
	    	if (MIXED_CONTENT_VALUE_KEY == valueKey) {
	    		text = text.trim();
	    		if (text.isEmpty()) {
	    			return;
	    		}
	    	}
	        try {
	            text = object.getString(valueKey) + text;
	        } catch (JSONException e) {
	            // no existing text, that's fine
	        }
	        try {
	        	if (valueKey != null) {
	        		object.put(valueKey, text);
	        	}
	        } catch (JSONException e) {
	            // Impossible by construction
	            throw new AssertionError(e);
	        }
	    }
	    
	    @Override
	    public JSONPropertyObject withProperty(
	        JSONProperty property, 
	        boolean add
	    ) {
	        Object value = property.getValue();
	        if(add && value instanceof String) {
	            value = convention.convertToJSONPrimitive((String)value);
	        }
	        Object old = object.opt(property.getKey());
	        try {
	            if(old != null) {
	                JSONArray values;
	                // Convert an existing property to an array
	                // and append to the array
	                if (old instanceof JSONArray) {
	                    values = (JSONArray)old;
	                } else {
	                    values = new JSONArray();
	                    values.put(old);
	                }
	                values.put(value);
	                object.put(property.getKey(), values);
	            } else if(getSerializedAsArrays().contains(property.getKey())) {
	                JSONArray values = new JSONArray();
	                values.put(value);
	                object.put(property.getKey(), values);
	            } else {
	                // Add the property directly.
	                object.put(property.getKey(), value);
	            }
	        } catch (JSONException e) {
				Throwables.log(e);
	        }
            return this;
	    }
	}
	
	public StandardXMLStreamWriter(
	    StandardNamespaceConvention convention, 
	    Writer writer
	) {
		super();
	    this.serializeAsArray(OBJECTS_KEY);
	    this.serializeAsArray("_item");
		this.convention = convention;
		this.writer = writer;
		this.namespaceContext = convention;
	}

	@Override
	public NamespaceContext getNamespaceContext(
	) {
		return namespaceContext;
	}

	@Override
	public void setNamespaceContext(
	    NamespaceContext context
	) throws XMLStreamException {
		this.namespaceContext = context;
	}

	public String getTextKey() {
		return valueKey;
	}

	public void setValueKey(
	    String valueKey
	) {
		this.valueKey = valueKey;
	}

	@Override
	public void writeStartDocument(
	) throws XMLStreamException {
		// The document is an object with one property -- the root element
		current = new JSONPropertyObject(null, new JSONObject());
		stack.clear();
	}
	
	@Override
	public void writeStartElement(
        String prefix, 
        String local, 
        String ns
	) throws XMLStreamException {
		stack.push(current);
		// Map object-elements to key "objects" and the type to the attribute "type"
		if(local.indexOf(".") > 0) {
	        current = new JSONPropertyString(OBJECTS_KEY);
	        JSONPropertyString prop = new JSONPropertyString(
	            convention.createAttributeKey("", ns, "type")
	        );
	        prop.addText(local);
	        current = current.withProperty(prop, false);
		} else {
    		String key = convention.createKey(prefix, ns, local);
    		current = new JSONPropertyString(key);
		}
	}

	@Override
	public void writeAttribute(
        String prefix, 
        String ns, 
        String local, 
        String value
	) throws XMLStreamException {
		String key = convention.isElement(prefix, ns, local)
			? convention.createKey(prefix, ns, local)
			: convention.createAttributeKey(prefix, ns, local);
		JSONPropertyString prop = new JSONPropertyString(key);
		prop.addText(value);
		current = current.withProperty(prop, false);
	}

	@Override
	public void writeAttribute(
	    String ns, 
	    String local, 
	    String value
	) throws XMLStreamException {
		writeAttribute(null, ns, local, value);
	}

	@Override
	public void writeAttribute(
	    String local, 
	    String value
	) throws XMLStreamException {
		writeAttribute(null, local, value);
	}

	@Override
	public void writeCharacters(
	    String text
	) throws XMLStreamException {
	    current.addText(text);
	}
	
	@Override
	public void writeEndElement(
	) throws XMLStreamException {
		if (stack.isEmpty()) {
		    throw new XMLStreamException("Too many closing tags.");
		}
		current = stack.pop().withProperty(current);
	}

	@Override
	public void writeEndDocument(
	) throws XMLStreamException {
		if (!stack.isEmpty()) {
		    throw new XMLStreamException("Missing some closing tags.");
		}
		if(this.current != null) {
     		writeJSONObject((JSONObject)current.getValue());
    		try {
    			writer.flush();
    		} catch (IOException e) {
    			throw new XMLStreamException(e);
    		}
    		this.current = null;
		}
	}

	/**
	 * Normalize JSON object. Map p: { objects: [] } to p: [] for index properties
	 * and to p: {} for non-indexed properties
	 * 
	 * @param root
	 * @return
	 * @throws JSONException
	 */
	private Object normalizeJSONObject(
	    Object root
	) throws JSONException {
	    if(root instanceof JSONArray) {
	        JSONArray arr = (JSONArray)root;
	        for(int i = 0; i < arr.length(); i++) {
	            arr.put(i, this.normalizeJSONObject(arr.get(i)));
            }
	    } else if(root instanceof JSONObject) {
	        JSONObject object = (JSONObject)root;
	        if(object.length() == 1 && object.has(OBJECTS_KEY)) {
                String indexKey = convention.createAttributeKey("", null, "index");
	            if(object.get(OBJECTS_KEY) instanceof JSONArray) {
	                JSONArray arr = object.getJSONArray(OBJECTS_KEY);
	                return this.normalizeJSONObject(
	                    arr.length() == 1 && !arr.getJSONObject(0).has(indexKey) ? arr.getJSONObject(0) : arr
	                );
	            } else if(object.get(OBJECTS_KEY) instanceof JSONObject) {
	                JSONObject obj = object.getJSONObject(OBJECTS_KEY);
	                return this.normalizeJSONObject(
	                    obj.has(indexKey) ? new JSONArray().put(obj) : obj
	                );
	            }
	        } else {
        	    for(String key: object.keySet()) {
        	        object.put(key, this.normalizeJSONObject(object.opt(key)));
        	    }
    	    }
	    }
	    return root;
	}

	/**
	 * For clients who want to modify the output object before writing to override.
	 * 
	 */
	protected void writeJSONObject(
	    JSONObject root
	) throws XMLStreamException {
		try {
			if(root == null) {
			    writer.write("null");
			} else {
			    Object obj = this.normalizeJSONObject(root);
			    if(obj instanceof JSONArray) {
			        ((JSONArray)obj).write(writer);
			    } else if(obj instanceof JSONObject) {
			        ((JSONObject)obj).write(writer);
			    }
			}
		} catch (JSONException e) {
			throw new XMLStreamException(e);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	@Override
	public void close(
	) throws XMLStreamException {
	    // no-impl
	}

	@Override
	public void flush(
	) throws XMLStreamException {
        // no-impl
	}

	@Override
	public String getPrefix(
	    String arg0
	) throws XMLStreamException {
		return null;
	}

    @Override
	public Object getProperty(
	    String arg0
	) throws IllegalArgumentException {
		return null;
	}

    @Override
	public void setDefaultNamespace(
	    String arg0
	) throws XMLStreamException {
        // no-impl
	}

    @Override
	public void setPrefix(
	    String arg0, 
	    String arg1
	) throws XMLStreamException {
        // no-impl
	}

    @Override
	public void writeDefaultNamespace(
	    String arg0
	) throws XMLStreamException {
        // no-impl
	}

    @Override
	public void writeEntityRef(
	    String arg0
	) throws XMLStreamException {
        // no-impl
	}

    @Override
	public void writeNamespace(
	    String arg0, 
	    String arg1
	) throws XMLStreamException {
        // no-impl
	}

    @Override
	public void writeProcessingInstruction(
	    String arg0
	) throws XMLStreamException {
        // no-impl
	}

    @Override
	public void writeProcessingInstruction(
	    String arg0, 
	    String arg1
	) throws XMLStreamException {
        // no-impl
	}
    
}