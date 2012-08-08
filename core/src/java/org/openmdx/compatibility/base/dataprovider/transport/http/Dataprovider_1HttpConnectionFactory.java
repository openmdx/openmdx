/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1HttpConnectionFactory.java,v 1.7 2008/03/19 17:07:35 hburger Exp $
 * Description: Lightweight Container's Dataprovider Connection Factory 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/19 17:07:35 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.http;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.kernel.application.container.spi.http.DataproviderConnectionFactory;
import org.openmdx.kernel.exception.BasicException;

/**
 * The Lightweight Container's Dataprovider Connection Factory
 */
@SuppressWarnings("unchecked")
public class Dataprovider_1HttpConnectionFactory
        implements DataproviderConnectionFactory, Dataprovider_1ConnectionFactory
{

        /**
	 * Constructor
	 */
        public Dataprovider_1HttpConnectionFactory(
        ){
            super();
        }

    /**
     * 
     */
    private URL url = null;

    /**
     * Request properties
     */
    private Map requestProperties = new HashMap();

    /**
     * Initializer
     * 
     * @param subject
     * @param url
     * 
     * @throws MalformedURLException 
     */
    public void initialize(
        Subject subject,
        URL url
    ) throws MalformedURLException {
        this.url = new URL(url, "process");
        Map cookies = new HashMap();
        for(
            Iterator i = subject.getPrivateCredentials(PasswordCredential.class).iterator();
            i.hasNext();
        ) {
            PasswordCredential credential = (PasswordCredential) i.next();
            String name = credential.getUserName();
            if(name == null) {
                throw new NullPointerException(
                    "A PasswordCredential's user name was null"
                );
            } else {
                int j = name.indexOf(':');
                if(j < 0) try {
                    if(
                         this.requestProperties.put(
                             "Authorization",
                             "Basic " + Base64.encode(
                                 (name + ':' + getValue(credential)).getBytes(ENCODING)
                             )
                         ) != null
                    ) throw new IllegalArgumentException(
                        "Subject contains duplicate Basic authentication PasswordCredentials"
                    );
                } catch (UnsupportedEncodingException exception) {
                    throw new RuntimeServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("encoding", ENCODING)
                        },
                        "Encoding missing"
                    );
                } else if (name.startsWith("Cookie:")) {
                    if(
                        cookies.put(
                            name.substring(j + 1),
                            getValue(credential)
                        ) != null
                    ) throw new IllegalArgumentException(
                        "Subject contains duplicate cookie credential '" + name.substring(j + 1) + "'"
                    );
                } else {
                    throw new IllegalArgumentException(
                        "PasswordCredential type '" + name.substring(0, j) + "' not supported"
                    );
                }
            }
        }
        if(!cookies.isEmpty()) {
            StringBuilder cookieValue = new StringBuilder("$Version=1");
            for(
                Iterator i = cookies.entrySet().iterator();
                i.hasNext();
            ){
                Map.Entry e = (Entry) i.next();
                cookieValue.append(
                    ';'
                ).append(
                    e.getKey()
                ).append(
                    '='
                ).append(
                    e.getValue()
                );
            }
            this.requestProperties.put(
                "Cookie",
                cookieValue.toString()
            );
        }
    }

    /**
     * 
     * @param credential
     * @return
     */
    private static String getValue(
        PasswordCredential credential
    ){
        char[] password = credential.getPassword();
        return password == null ? "" : new String(password);
    }

    /* (non-Javadoc)
	 * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory#createConnection()
	 */
        public Dataprovider_1_1Connection createConnection(
        ) throws ServiceException {
                return new Dataprovider_1HttpConnection(this.url, this.requestProperties);
        }

    /**
     * The authorization value encoding to be used.
     */
    private static final String ENCODING = "UTF-8";

}
