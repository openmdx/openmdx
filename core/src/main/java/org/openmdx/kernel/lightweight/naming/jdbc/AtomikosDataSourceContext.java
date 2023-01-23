/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Atomikos Data Source Context 
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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.Name;
import javax.naming.NamingException;

/**
 * Atomikos Data Source Context
 */
public class AtomikosDataSourceContext extends AbstractDataSourceContext {

    private static final String DATA_SOURCE_CLASS_NAME = "com.atomikos.jdbc.AtomikosDataSourceBean";
    private static final List<String> XA_STRING_POOL_PROPERTIES = Arrays.asList(
    	"testQuery",
    	"uniqueResourceName"
    );
    private static final List<String> XA_BOOLEAN_POOL_PROPERTIES = Arrays.asList(
    	"concurrentConnectionValidation",
    	"localTransactionMode"
    );
    private static final List<String> XA_INTEGER_POOL_PROPERTIES = Arrays.asList(
    	"borrowConnectionTimeout",
    	"defaultIsolationLevel",
    	"loginTimeout",
    	"maintenanceInterval",
    	"maxIdleTime",
    	"maxLifetime",
    	"maxPoolSize",
    	"minPoolSize",
    	"poolSize",
    	"reapTimeout"
    );

    private static final String NON_XA_DATA_SOURCE_CLASS_NAME = "com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean";
    private static final List<String> NON_XA_STRING_POOL_PROPERTIES = Arrays.asList(
    	"testQuery",
    	"uniqueResourceName",
    	"driverClassName",
    	"password",
    	"user"
    );
    private static final List<String> NON_XA_BOOLEAN_POOL_PROPERTIES = Arrays.asList(
    	"concurrentConnectionValidation",
    	"localTransactionMode",
    	"readOnly"
    );
    private static final List<String> NON_XA_INTEGER_POOL_PROPERTIES = Arrays.asList(
    	"borrowConnectionTimeout",
    	"defaultIsolationLevel",
    	"loginTimeout",
    	"maintenanceInterval",
    	"maxIdleTime",
    	"maxLifetime",
    	"maxPoolSize",
    	"minPoolSize",
    	"poolSize",
    	"reapTimeout"
    );
    
    @Override
	protected DataSourceConfiguration createXaDataSourceConfiguration(
		String xaDataSourceClassName,
		Map<String, ?> queryParameters, 
		Hashtable<?, ?> environment
	) {
		final DataSourceConfiguration xaDataSourceConfiguration = new DataSourceConfiguration(
			DATA_SOURCE_CLASS_NAME,
			queryParameters, 
			environment,
			getDefaultPoolConfiguration(),
			XA_STRING_POOL_PROPERTIES,
			XA_BOOLEAN_POOL_PROPERTIES,
			XA_INTEGER_POOL_PROPERTIES
		);
		final Map<String, Object> poolConfiguration = xaDataSourceConfiguration.getPoolConfiguration();
		poolConfiguration.put("xaDataSourceClassName", xaDataSourceClassName);
		poolConfiguration.put("xaProperties", xaDataSourceConfiguration.getDelegateConfiguration());
		return xaDataSourceConfiguration;
	}

	@Override
    protected DataSourceConfiguration createNonXaDataSourceConfiguration(
		String urlPath,
		Map<String,?> queryParameters,
		Hashtable<?,?> environment
	) {
		final DataSourceConfiguration dataSourceConfiguration = new DataSourceConfiguration(
			NON_XA_DATA_SOURCE_CLASS_NAME,
			queryParameters, 
			environment,
			getDefaultPoolConfiguration(),
			NON_XA_STRING_POOL_PROPERTIES,
			NON_XA_BOOLEAN_POOL_PROPERTIES,
			NON_XA_INTEGER_POOL_PROPERTIES
		);
		final String driverUrl = toDriverUrl(urlPath, dataSourceConfiguration.getDelegateConfiguration());
		final Map<String, Object> poolConfiguration = dataSourceConfiguration.getPoolConfiguration();
		poolConfiguration.put("url", driverUrl);
		return dataSourceConfiguration;
	}

	@Override
	protected void closeConnectionFactory(Name name, Object connectionFactory) throws NamingException {
		try {
			connectionFactory.getClass().getMethod("close").invoke(connectionFactory);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException cause) {
			NamingException exception = new NamingException("Shutdown failure");
			exception.setRootCause(cause);
			exception.setResolvedObj(connectionFactory);
			exception.setResolvedName(name);
			throw exception;
		}
		super.closeConnectionFactory(name, connectionFactory);
	}

    private Map<String,?> getDefaultPoolConfiguration() {
		return Collections.singletonMap(
			"uniqueResourceName",
			"DataSource" + this.connectionFactories.size()
		);
    }
	
}
