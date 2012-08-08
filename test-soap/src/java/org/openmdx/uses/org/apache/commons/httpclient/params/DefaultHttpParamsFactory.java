/*
 * $Header: /cvsroot/openmdx/test-soap/src/java/org/openmdx/uses/org/apache/commons/httpclient/params/DefaultHttpParamsFactory.java,v 1.2 2007/03/22 15:37:58 wfro Exp $
 * $Revision: 1.2 $
 * $Date: 2007/03/22 15:37:58 $
 *
 * ====================================================================
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
 
package org.openmdx.uses.org.apache.commons.httpclient.params;

import java.util.ArrayList;
import java.util.Arrays;

import org.openmdx.uses.org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.openmdx.uses.org.apache.commons.httpclient.HttpVersion;
import org.openmdx.uses.org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.openmdx.uses.org.apache.commons.httpclient.cookie.CookiePolicy;
import org.openmdx.uses.org.apache.commons.httpclient.util.DateUtil;

/**
 * @since 3.0
 */
public class DefaultHttpParamsFactory implements HttpParamsFactory {

    private HttpParams httpParams;

    /**
     * 
     */
    public DefaultHttpParamsFactory() {
        super();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.params.HttpParamsFactory#getDefaultParams()
     */
    public synchronized HttpParams getDefaultParams() {
        if (httpParams == null) {
            httpParams = createParams();
        }

        return httpParams;
    }

    protected HttpParams createParams() {
        HttpClientParams params = new HttpClientParams(null);
        
        params.setParameter(HttpMethodParams.USER_AGENT, "Jakarta Commons-HttpClient/3.0.1");
        params.setVersion(HttpVersion.HTTP_1_1);
        params.setConnectionManagerClass(SimpleHttpConnectionManager.class);
        params.setCookiePolicy(CookiePolicy.RFC_2109);
        params.setHttpElementCharset("US-ASCII");
        params.setContentCharset("ISO-8859-1");
        params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        
        ArrayList datePatterns = new ArrayList();
        datePatterns.addAll(
            Arrays.asList(
                new String[] {
                    DateUtil.PATTERN_RFC1123,
                    DateUtil.PATTERN_RFC1036,
                    DateUtil.PATTERN_ASCTIME,
                    "EEE, dd-MMM-yyyy HH:mm:ss z",
                    "EEE, dd-MMM-yyyy HH-mm-ss z",
                    "EEE, dd MMM yy HH:mm:ss z",
                    "EEE dd-MMM-yyyy HH:mm:ss z",
                    "EEE dd MMM yyyy HH:mm:ss z",
                    "EEE dd-MMM-yyyy HH-mm-ss z",
                    "EEE dd-MMM-yy HH:mm:ss z",
                    "EEE dd MMM yy HH:mm:ss z",
                    "EEE,dd-MMM-yy HH:mm:ss z",
                    "EEE,dd-MMM-yyyy HH:mm:ss z",
                    "EEE, dd-MM-yyyy HH:mm:ss z",                
                }
            )
        );
        params.setParameter(HttpMethodParams.DATE_PATTERNS, datePatterns);
            
        // TODO: To be removed. Provided for backward compatibility
        String agent = null;
        try {
            agent = System.getProperty("httpclient.useragent");
        } catch (SecurityException ignore) {
        }
        if (agent != null) {        
            params.setParameter(HttpMethodParams.USER_AGENT, agent);
        }
        
        // TODO: To be removed. Provided for backward compatibility
        String preemptiveDefault = null;
        try {
            preemptiveDefault = System.getProperty("httpclient.authentication.preemptive");
        } catch (SecurityException ignore) {
        }
        if (preemptiveDefault != null) {
            preemptiveDefault = preemptiveDefault.trim().toLowerCase();
            if (preemptiveDefault.equals("true")) {
                params.setParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, Boolean.TRUE);
            } else if (preemptiveDefault.equals("false")) {
                params.setParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, Boolean.FALSE);
            }
        }
        
        // TODO: To be removed. Provided for backward compatibility
        String defaultCookiePolicy = null;
        try {
            defaultCookiePolicy = System.getProperty("apache.commons.httpclient.cookiespec");
        } catch (SecurityException ignore) {
        }
        if (defaultCookiePolicy != null) {
            if ("COMPATIBILITY".equalsIgnoreCase(defaultCookiePolicy)) {
                params.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            } else if ("NETSCAPE_DRAFT".equalsIgnoreCase(defaultCookiePolicy)) {
                params.setCookiePolicy(CookiePolicy.NETSCAPE);
            } else if ("RFC2109".equalsIgnoreCase(defaultCookiePolicy)) {
                params.setCookiePolicy(CookiePolicy.RFC_2109);
            }
        }

        return params;
    } 

}