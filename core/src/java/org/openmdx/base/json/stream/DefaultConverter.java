/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: DefaultConverter
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

/**
 * Default converter that tries to convert value to appropriate primitive (if fails, returns original string)
 *
 * @author <a href="mailto:dejan@nighttale.net">Dejan Bosanac</a>
 * @since 1.1
 */
public class DefaultConverter implements TypeConverter {
    /* Were there a constants class, this key would live there. */
    private static final String ENFORCE_32BIT_INTEGER_KEY = "jettison.mapped.typeconverter.enforce_32bit_integer";
    public static final boolean ENFORCE_32BIT_INTEGER = Boolean.getBoolean( ENFORCE_32BIT_INTEGER_KEY );
    private boolean enforce32BitInt = ENFORCE_32BIT_INTEGER; 

   	public void setEnforce32BitInt(boolean enforce32BitInt) {
		this.enforce32BitInt = enforce32BitInt;
	}

	public Object convertToJSONPrimitive(String text) {
        if(text == null) return text;
		Object primitive = null;
		// Attempt to convert to Integer
		try {
			primitive = enforce32BitInt ? Integer.valueOf(text) : Long.valueOf(text);
		} catch (Exception e) {/**/}
		// Attempt to convert to double
		if (primitive == null) {
			try {
				Double v = Double.valueOf(text);
                if( !v.isInfinite() && !v.isNaN() ) {
                    primitive = v;
                }
                else {
                    primitive = text;
                }
			} catch (Exception e) {/**/}
		}
		// Attempt to convert to boolean
		if (primitive == null) {
			if(text.trim().equalsIgnoreCase("true") || text.trim().equalsIgnoreCase("false")) {
				primitive = Boolean.valueOf(text);
			}
		}

		if (primitive == null || !primitive.toString().equals(text)) {
			// Default String
			primitive = text;
		}

		return primitive;
    }
}
