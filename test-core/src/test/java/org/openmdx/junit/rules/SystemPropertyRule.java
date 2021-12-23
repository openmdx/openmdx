/*
 * ====================================================================
 * Project:     openMDX/Test Core, http://www.openmdx.org/
 * Description: System Property Rule
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2016, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.junit.rules;

import java.util.HashMap;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SystemPropertyRule implements TestRule {

	/**
	 * The required properties are set during the statement evaluation
	 */
	private final Map<String, String> requiredProperties = new HashMap<String, String>();

	/**
	 * Defines a system property to be set during method execution
	 * 
	 * @param key
	 * @param value
	 * 
	 * @return this SystemPropertyRule
	 */
	public SystemPropertyRule setProperty(
		String key,
		String value
	){
		requiredProperties.put(key, value);
		return this;
	}
	
	@Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
            	final Map<String, String> savedProperties = setProperties();
                try {
                    base.evaluate();
                } finally {
                    resetProperties(savedProperties);
                }
            }

			private void resetProperties(Map<String, String> savedProperties) {
				for (Map.Entry<String, String> entry : savedProperties.entrySet()) {
					apply(entry);
				} 
			}

			private String apply(Map.Entry<String, String> entry) {
				final String key = entry.getKey();
				final String value = entry.getValue();
				return value == null ? System.clearProperty(key): System.setProperty(key, value);
			}

			private Map<String, String> setProperties() {
            	Map<String, String> savedProperties = new HashMap<String, String>();
				for (Map.Entry<String, String> entry : requiredProperties.entrySet()) {
					savedProperties.put(entry.getKey(), apply(entry));
				} 
				return savedProperties;
			}
        };
    }	

}
