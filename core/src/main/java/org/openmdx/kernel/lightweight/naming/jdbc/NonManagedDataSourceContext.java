/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Legacy Data Source Context 
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;

import org.openmdx.kernel.lightweight.transaction.LightweightTransactionManager;

/**
 * Legacy Data Source Context
 * 
 * @deprecated in favour of Atomikos' JDBC support
 */
@Deprecated
public class NonManagedDataSourceContext extends AbstractDataSourceContext {

    private static final String NON_XA_DATA_SOURCE_CLASS_NAME = "org.openmdx.kernel.lightweight.sql.LightweightDataSource";
    private static final Map<String,Object> NON_XA_DEFAULT_POOL_CONFIGURATION = Collections.emptyMap();
    private static final List<String> NON_XA_STRING_POOL_PROPERTIES = Collections.emptyList();
    private static final List<String> NON_XA_BOOLEAN_POOL_PROPERTIES = Collections.emptyList();
    private static final List<String> NON_XA_INTEGER_POOL_PROPERTIES = Arrays.asList(
    	"loginTimeout"
    );

	@Override
	protected DataSourceConfiguration createXaDataSourceConfiguration(
		String xaDataSourceClassName,
		Map<String, ?> queryParameters, 
		Hashtable<?, ?> environment
	) throws NamingException {
		throw new NamingException(NonManagedDataSourceContext.class.getSimpleName() + " has no XA support");
	}

	@Override
	protected DataSourceConfiguration createNonXaDataSourceConfiguration(
		String urlPath, 
		Map<String, ?> queryParameters,
		Hashtable<?, ?> environment
	) throws NamingException {
		final DataSourceConfiguration dataSourceConfiguration = new DataSourceConfiguration(
			NON_XA_DATA_SOURCE_CLASS_NAME,
			queryParameters, 
			environment,
			NON_XA_DEFAULT_POOL_CONFIGURATION,
			NON_XA_STRING_POOL_PROPERTIES,
			NON_XA_BOOLEAN_POOL_PROPERTIES,
			NON_XA_INTEGER_POOL_PROPERTIES
		);
		final String driverUrl = toDriverUrl(urlPath, dataSourceConfiguration.getDelegateConfiguration());
		final Map<String, Object> poolConfiguration = dataSourceConfiguration.getPoolConfiguration();
		poolConfiguration.put("driverUrl", driverUrl);
		poolConfiguration.put("transactionManager", LightweightTransactionManager.getInstance());
		return dataSourceConfiguration;
	}

}
