/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Data Source Configuration
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.lightweight.naming.jdbc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

/**
 * Data Source Configuration
 */
class DataSourceConfiguration {

	DataSourceConfiguration(
		String dataSourceClassName,
		Map<String,?> queryParameters,
		Hashtable<?,?> environment,
		Map<String,?> defaultPoolConfiguration,
		Collection<String> stringPoolPropertyNames,
		Collection<String> booleanPoolPropertyNames,
		Collection<String> integerPoolPropertyNames
	){
		this.dataSourceClassName = dataSourceClassName;
		delegateConfiguration = new Properties();
		poolConfiguration = new HashMap<>(defaultPoolConfiguration);
		propagateDataSourceConfiguration(stringPoolPropertyNames, booleanPoolPropertyNames, integerPoolPropertyNames, queryParameters);
		propagatePoolConfiguration(stringPoolPropertyNames, booleanPoolPropertyNames, integerPoolPropertyNames, queryParameters, environment);
		
	}

	private final String dataSourceClassName;
	private final Properties delegateConfiguration;
	private final Map<String,Object> poolConfiguration;
	
	public Properties getDelegateConfiguration() {
		return delegateConfiguration;
	}

	public Map<String, Object> getPoolConfiguration() {
		return poolConfiguration;
	}
	
    public String getDataSourceClassName() {
		return dataSourceClassName;
	}

    private void propagateDataSourceConfiguration(
		Collection<String> stringPoolPropertyNames,
		Collection<String> booleanPoolPropertyNames,
		Collection<String> integerPoolPropertyNames,
		Map<String,?> queryParameters
    ) {
		for(Map.Entry<String,?> entry : queryParameters.entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
            if(
            	value instanceof String &&
            	!stringPoolPropertyNames.contains(key) &&
            	!booleanPoolPropertyNames.contains(key) &&
            	!integerPoolPropertyNames.contains(key)
            ) {
            	delegateConfiguration.setProperty(key, (String)value);
            }
    	}
    }
    
    private void propagatePoolConfiguration(
		Collection<String> stringPoolPropertyNames,
		Collection<String> booleanPoolPropertyNames,
		Collection<String> integerPoolPropertyNames,
		Map<String,?> queryParameters,
		Hashtable<?,?> environment
    ) {
		for(Map.Entry<String,?> entry : queryParameters.entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
            if(stringPoolPropertyNames.contains(key)) {
            	poolConfiguration.put(key, value);
            } else if (booleanPoolPropertyNames.contains(key)) {
            	poolConfiguration.put(key, Boolean.valueOf((String) value));
            } else if (integerPoolPropertyNames.contains(key)) {
            	poolConfiguration.put(key, Integer.valueOf((String)value));
            }
    	}
		for(Map.Entry<?,?> entry : environment.entrySet()) {
			final Object key = entry.getKey();
			final Object value = entry.getValue();
            if(stringPoolPropertyNames.contains(key) && value instanceof String) {
            	poolConfiguration.put((String)key, (String)value);
            } else if (booleanPoolPropertyNames.contains(key) && value instanceof Boolean) {
            	poolConfiguration.put((String)key, (Boolean)value);
            } else if (integerPoolPropertyNames.contains(key) && value instanceof Integer) {
            	poolConfiguration.put((String)key, (Integer)value);
            }
		}
    }
    
}
