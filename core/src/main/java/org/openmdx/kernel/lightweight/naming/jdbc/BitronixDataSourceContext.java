/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Bitronix Data Source Context 
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;


/**
 * Bitronix Data Source Context
 */
public class BitronixDataSourceContext extends AbstractDataSourceContext {

    private final static String DATA_SOURCE_CLASS_NAME = "bitronix.tm.resource.jdbc.PoolingDataSource";
    
    private static final List<String> XA_STRING_POOL_PROPERTIES = Arrays.asList(
        "cursorHoldability",
        "isolationLevel",
        "localAutoCommit",
        "testQuery",
        "uniqueName"
    );
    
    private static final List<String> XA_BOOLEAN_POOL_PROPERTIES = Arrays.asList(
        "allowLocalTransactions",
        "applyTransactionTimeout",
        "automaticEnlistingEnabled",
        "deferConnectionRelease",
        "enableJdbc4ConnectionTestbs", 
        "ignoreRecoveryFailures",
        "shareTransactionConnections",
        "useTmJoin",
        "deferConnectionRelease"
    );
    
    private static final List<String> XA_INTEGER_POOL_PROPERTIES = Arrays.asList(
        "acquireIncrement",
        "acquisitionInterval",
        "acquisitionTimeout",
        "loginTimeout",
        "maxIdleTime",
        "maxPoolSize",
        "minPoolSize",
        "preparedStatementCacheSize",
        "twoPcOrderingPosition"
    );        
    
    private static final Map<String,Object> XA_DEFAULT_POOL_CONFIGURATION = new HashMap<>();
    static {
    	XA_DEFAULT_POOL_CONFIGURATION.put("maxPoolSize", Integer.valueOf(8));
    	XA_DEFAULT_POOL_CONFIGURATION.put("ignoreRecoveryFailures", Boolean.TRUE);
    	XA_DEFAULT_POOL_CONFIGURATION.put("shareTransactionConnections", Boolean.TRUE);
    }
    
	@Override
	protected DataSourceConfiguration createXaDataSourceConfiguration(
		String xaDataSourceClassName,
		Map<String, ?> queryParameters, 
		Hashtable<?, ?> environment
	){
		final DataSourceConfiguration xaDataSourceConfiguration = new DataSourceConfiguration(
			DATA_SOURCE_CLASS_NAME,
			queryParameters, 
			environment,
			XA_DEFAULT_POOL_CONFIGURATION,
			XA_STRING_POOL_PROPERTIES,
			XA_BOOLEAN_POOL_PROPERTIES,
			XA_INTEGER_POOL_PROPERTIES
		);
		final Map<String, Object> poolConfiguration = xaDataSourceConfiguration.getPoolConfiguration();
		poolConfiguration.put("className", xaDataSourceClassName);
		poolConfiguration.put("driverProperties", xaDataSourceConfiguration.getDelegateConfiguration());
		return xaDataSourceConfiguration;
	}

	@Override
	protected DataSourceConfiguration createNonXaDataSourceConfiguration(
		String uriPath,
		Map<String, ?> queryParameters, 
		Hashtable<?, ?> environment
	) throws NamingException {
        throw Throwables.initCause(
            new NamingException("DriverManager-URLs not yet supported by " + BitronixDataSourceContext.class.getSimpleName()),
            null,
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.MEDIA_ACCESS_FAILURE,
            new BasicException.Parameter("uriPath", uriPath)
        );
	}

}
