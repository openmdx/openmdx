/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: XA DataSource Context 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012-2013, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.naming.jdbc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.lightweight.naming.spi.ResourceContext;
import org.openmdx.kernel.loading.BeanFactory;


/**
 * XA DataSource Context
 */
class XADataSourceContext extends ResourceContext {

    /**
     * Constructor 
     */
    XADataSourceContext(
    ){
        super();
    }

    private final static String XA_DATA_SOURCE_URL_PREFIX = "jdbc:xa:";
    
    private final static String XA_DATA_SOURCE_CLASS_NAME = "bitronix.tm.resource.jdbc.PoolingDataSource";
    
    private final static int MAX_POOL_SIZE_DEFAULT = 8;

    private static final List<String> STRING_POOL_PROPERTIES = Arrays.asList(
        "CursorHoldability",
        "IsolationLevel",
        "LocalAutoCommit",
        "TestQuery",
        "UniqueName"
    );
    
    private static final List<String> BOOLEAN_POOL_PROPERTIES = Arrays.asList(
        "AllowLocalTransactions",
        "ApplyTransactionTimeout",
        "AutomaticEnlistingEnabled",
        "DeferConnectionRelease",
        "EnableJdbc4ConnectionTestbs", 
        "IgnoreRecoveryFailures",
        "ShareTransactionConnections",
        "UseTmJoin",
        "DeferConnectionRelease"
    );
    
    private static final List<String> INTEGER_POOL_PROPERTIES = Arrays.asList(
        "AcquireIncrement",
        "AcquisitionInterval",
        "AcquisitionTimeout",
        "LoginTimeout",
        "MaxIdleTime",
        "MaxPoolSize",
        "MinPoolSize",
        "PreparedStatementCacheSize",
        "TwoPcOrderingPosition"
    );        
    
    /* (non-Javadoc)
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    public Object lookup(
        String name
    ) throws NamingException {
        Object dataSource = lookupLink(name);
        if(dataSource == null){
            bind(name, dataSource = newDataSource(name));
        }
        return dataSource;
    }
    
    private Map<String, Object> getPoolConfiguration(
        Map<String, ?> source,
        String className
    ){
        Map<String, Object> target = new HashMap<String, Object>();
        for(Map.Entry<String,?> entry : source.entrySet()) {
            String key = entry.getKey();
            if(STRING_POOL_PROPERTIES.contains(key)) {
                target.put(key, entry.getValue());
            } else if (BOOLEAN_POOL_PROPERTIES.contains(key)) {
                target.put(key, Boolean.valueOf((String) entry.getValue()));
            } else if (INTEGER_POOL_PROPERTIES.contains(key)) {
                target.put(key, Integer.valueOf((String) entry.getValue()));
            }
        }
        target.put("ClassName", className);
        if(!target.containsKey("MaxPoolSize")){
            target.put("MaxPoolSize", Integer.valueOf(MAX_POOL_SIZE_DEFAULT));
        }
        if(!target.containsKey("IgnoreRecoveryFailures")){
            target.put("IgnoreRecoveryFailures", Boolean.TRUE);
        }
        if(!target.containsKey("ShareTransactionConnections")){
            target.put("ShareTransactionConnections", Boolean.TRUE);
        }
        return target;
    }

    private Properties getDriverConfiguration(
        Map<String, ?> source
    ){
        Properties target = new Properties();
        for(Map.Entry<String,?> entry : source.entrySet()) {
            String key = entry.getKey();
            if(
                !STRING_POOL_PROPERTIES.contains(key) &&
                !BOOLEAN_POOL_PROPERTIES.contains(key) &&
                !INTEGER_POOL_PROPERTIES.contains(key)
            ){
                target.setProperty(key, (String) entry.getValue());
            }
        }
        return target;
    }
    
    private Object newDataSource(
        String uri
    ) throws NamingException {
        Map<String, ?> info = getInfo(uri);
        DataSource poolingDataSource = BeanFactory.<DataSource>newInstance(
    		DataSource.class,	
            XA_DATA_SOURCE_CLASS_NAME,
            getPoolConfiguration(info, getDataSourceClassName(uri))
        ).instantiate();
        applyDriverConfiguration(
            poolingDataSource,
            getDriverConfiguration(info)
        );
        return poolingDataSource;
    }

    /**
     * Use Bitronix specific methods
     * 
     * @param bitronixDataSource
     * @param driverConfiguration
     * 
     * @throws NamingException
     */
    private void applyDriverConfiguration(
        DataSource bitronixDataSource,
        Properties driverConfiguration
    ) throws NamingException{
        try {
            bitronixDataSource.getClass().getMethod("setDriverProperties", Properties.class).invoke(bitronixDataSource, driverConfiguration);
        } catch (Exception exception) {
            throw Throwables.initCause(
                new NamingException("Bitronix XA data source set-up failure"),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE
            );
        }
    }
    
    private Map<String,Object> getInfo(
        String specification
    ) {
        Map<String,Object> info = new HashMap<String,Object>();
        int q = specification.indexOf('?');
        if(q >= 0) {
            String[] entries = specification.substring(q+1).split("&");
            for(String entry : entries) {
                int e = entry.indexOf('=');
                if(e < 0) {
                    info.put(entry, null);
                } else {
                    info.put(
                        entry.substring(0, e),
                        entry.substring(e+1)
                    );
                }
            }
        }
        for(Map.Entry<?, ?> entry : environment.entrySet()) {
            Object k = entry.getKey();
            if(k instanceof String){
                info.put((String) k, entry.getValue());
            }
        }
        return info;
    }
    
    private String getDataSourceClassName(
        String uri
    ) throws NamingException {
        if(uri.startsWith(XA_DATA_SOURCE_URL_PREFIX)) {
            int p = XA_DATA_SOURCE_URL_PREFIX.length();
            int q = uri.indexOf('?');
            return q < 0 ? uri.substring(p) : uri.substring(p, q);  
        } else {
            throw Throwables.initCause(
                new NamingException("DriverManager-URLs not yet supported by XA DataSource context"),
                null,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE,
                new BasicException.Parameter("connectionURL", uri)
            );
        }
    }
    
}
