/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Data Source Context 
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

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.openmdx.kernel.lightweight.naming.spi.AbstractContext;
import org.openmdx.kernel.lightweight.naming.spi.ResourceContext;
import org.openmdx.kernel.loading.BeanFactory;

/**
 * Data Source Context
 */
public abstract class AbstractDataSourceContext extends ResourceContext {

	/**
	 * This prefix is used for XA Data Source URIs
	 */
	private static final String XA_DATA_SOURCE_URI_PREFIX = "jdbc:xa:";

	/**
	 * Tests whether the given URI has the non-XA JDBC URL prefix
	 * 
	 * @param uri the URI to be tested
	 * 
	 * @return {@code true} if the given name is a non-xa JDBC URL
	 */
	protected static boolean isXaDataSourceUri(String uri) {
		return uri.startsWith(XA_DATA_SOURCE_URI_PREFIX);
	}

	/**
	 * Constructor
	 */
	protected AbstractDataSourceContext() {
		super();
	}

	public Object lookup(String name) throws NamingException {
		Object dataSource = lookupLink(name);
		if (dataSource == null) {
			final DataSourceConfiguration dataSourceConfiguration = isXaDataSourceUri(name) ? createXaDataSourceConfiguration(
				getXaDataSourceClassName(name),
				getQueryParameters(name),
				environment
			) : createNonXaDataSourceConfiguration(
				getUrlPath(name),
				getQueryParameters(name),
				environment
			);
			dataSource = BeanFactory.<DataSource>newInstance(
				DataSource.class,	
				dataSourceConfiguration.getDataSourceClassName(),
				dataSourceConfiguration.getPoolConfiguration()
			).instantiate();
			bind(name, dataSource);
		}
		return dataSource;
	}

    /**
     * Parse the URI's query parameters
     * 
     * @param uri the URI
     * 
     * @return the URI's (mandatory) query parameters 
     */
    private static Map<String,?> getQueryParameters(
        String uri
    ){
    	final int beginOfQuery = uri.indexOf('?') + 1;
		return AbstractContext.getParameters(uri.substring(beginOfQuery));
    }

    /**
     * Parse the URI's XA Data Source Class Name
     * 
     * @param uri the URI
     * 
     * @return the URI's XA Data Source Class Name
     */
    protected static String getXaDataSourceClassName(
    	String uri
    ) {
    	final int delimiterPosition = uri.indexOf('?');
		return uri.substring(XA_DATA_SOURCE_URI_PREFIX.length(), delimiterPosition);
    }

    /**
     * Extract the URL path without query
     * 
     * @param uri the complete URI
     * 
     * @return the URL path without query
     */
    protected static String getUrlPath(
    	String uri
    ) {
    	final int delimiterPosition = uri.indexOf('?');
		return uri.substring(0, delimiterPosition);
    }
    
    /**
     * Compose the driver URL
     * 
     * @param urlPath the URL path
     * @param query the Query arguments
     * 
     * @return the driver URL
     */
    protected static String toDriverUrl(
    	String urlPath,
    	Properties query
    ) {
    	final StringBuilder url = new StringBuilder(urlPath);
    	char separator = '?';
    	for(String key : query.stringPropertyNames()) {
    		url.append(separator).append(key).append('=');
    		final String value = query.getProperty(key);
    		if(value!=null) {
    			url.append(value);
    		}
    	}
    	return url.toString();
    }
    
	/**
	 * The Data Source Factory method
	 * 
	 * @param dataSourceConfiguration the {@code DataSource} configuration
	 * @return a new DataSource to be bound to the JNDI tree
	 * 
	 * @throws NamingException in aces of failure
	 */
	protected DataSource createDataSource(
		DataSourceConfiguration dataSourceConfiguration
	) throws NamingException {
		return BeanFactory.<DataSource>newInstance(
			DataSource.class,	
			dataSourceConfiguration.getDataSourceClassName(),
			dataSourceConfiguration.getPoolConfiguration()
		).instantiate();
	}

    public void activate() {
    	jdbcURLContextFactory.setDataSourceContext(this);
    }
	
	protected abstract DataSourceConfiguration createXaDataSourceConfiguration(
		String xaDataSourceClassName,
		Map<String,?> queryParameters,
		Hashtable<?,?> environment
    ) throws NamingException;;

    protected abstract DataSourceConfiguration createNonXaDataSourceConfiguration(
		String urlPath,
		Map<String,?> queryParameters,
		Hashtable<?,?> environment
    ) throws NamingException;
 
}
