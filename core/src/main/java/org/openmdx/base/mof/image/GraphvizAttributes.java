/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Graphviz Attributes
 * Owner: the original authors. 
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.mof.image;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Graphviz Attributes
 */
class GraphvizAttributes {
	
	GraphvizAttributes(GraphvizStyle styleSheet, String... controlKeys) {
		this.styleSheet = styleSheet;
		this.controlKeys = Arrays.asList(controlKeys);
	}
	
	private final GraphvizStyle styleSheet;
	private final Properties defaultValues = new Properties();
	private final Properties styleValues = new Properties(defaultValues);
	private final Properties parameterValues = new Properties(styleValues);
	private final Properties strictValues = new Properties(parameterValues);
	private final Collection<String> controlKeys;
	
	public String toString() {
		return GraphvizTemplates.toAttributeList(getAttributes());
	}

	// Default visibility for testing
	Map<String, String> getAttributes() {
		applyStyles();
		final Map<String,String> attributes = new HashMap<String, String>();
		for(String key : strictValues.stringPropertyNames()) {
			if(!controlKeys.contains(key)) {
				attributes.put(key, getValue(key));
			}
		}
		return attributes;
	}

	private void applyStyles() {
		final String styles = strictValues.getProperty("_class","");
		for(String style : styles.split("\\s+")) {
			if(!style.isEmpty()) {
				styleValues.putAll(styleSheet.getClassStyle(style));
			}
		}
	}
		
	String getValue(String key) {
		return this.strictValues.getProperty(key);
	}
	
	void setDefaultValue(String key, String value) {
		this.defaultValues.put(key, value);
	}
	void setStrictValue(String key, String value) {
		if(value == null) {
			this.strictValues.remove(key);
		} else {
			this.strictValues.put(key, value);
		}
	}
	
	void parseParameters(CharSequence parameterList) {
		for (String parameter : parameterList.toString().split("\\s*,\\s*")) {
			if(!parameter.isEmpty()) {
	            String[] nv = parameter.split("=");
	            this.parameterValues.put(nv[0].toLowerCase(), GraphvizTemplates.unquote(nv[1]));
			}
		}
	}
	
}