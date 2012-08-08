/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ConnectorContext.java,v 1.6 2010/08/03 14:02:01 hburger Exp $
 * Description: DataSource Context 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/03 14:02:01 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.naming.eis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.lightweight.naming.spi.ResourceContext;
import org.openmdx.kernel.loading.BeanFactory;
import org.openmdx.kernel.loading.Factory;


/**
 * DataSource Context
 */
class ConnectorContext extends ResourceContext {

    /**
     * Constructor 
     *
     * @param transactionManager
     */
    ConnectorContext(
    ){
        super();
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    public Object lookup(
        String name
    ) throws NamingException {
        Object connectionFactory = lookupLink(name);
        if(connectionFactory == null) try {
            bind(
                name,
                connectionFactory = newManagedConnectionFactory(name).createConnectionFactory()
            );
        } catch (ResourceException exception) {
            throw Throwables.initCause(
                new NamingException("Lazy connector set-up failed"),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE,
                new BasicException.Parameter("uri", name)
            );
        }
        return connectionFactory;
    }

    /**
     * Create a managed connection factory
     * 
     * @param uri
     * 
     * @return a new managed connection factory
     * 
     * @throws ResourceException
     */
    private ManagedConnectionFactory newManagedConnectionFactory(
        String uri
    ) throws ResourceException{
        int q = uri.indexOf('?');
        String managedConnectionFactoryClass = uri.substring(
            4, // "eis" scheme specific part
            q < 0 ? uri.length() : q
        );
        Map<String,Object> configuration = new HashMap<String,Object>();
        if(q > 0) {
            for(String c : uri.substring(q + 1).split("&")) {
                int e = c.indexOf('=');
                if(e < 0) {
                    configuration.put(c, null);
                } else {
                    configuration.put(
                        c.substring(0, e),
                        decode(c.substring(e + 1))
                    );
                }
            }
        }
        Factory<ManagedConnectionFactory> managedConnectionFactoryBuilder = BeanFactory.newInstance(
            managedConnectionFactoryClass,
            configuration
        );
        return managedConnectionFactoryBuilder.instantiate();
    }
    
    /**
     * Decode url property values
     * 
     * @param encodedValue
     * 
     * @return the native value
     */
    private static Object decode(
        String encodedValue
    ){
        if(encodedValue.startsWith("(java.lang.Boolean)")) {
            return Boolean.valueOf(encodedValue.substring(19));
        } else if(encodedValue.startsWith("(java.lang.Integer)")){
            return Integer.valueOf(encodedValue.substring(19));
        } else if (encodedValue.startsWith("(java.lang.Long)")){
            return Long.valueOf(encodedValue.substring(16));
        } else if (encodedValue.startsWith("(java.lang.Short)")){
            return Short.valueOf(encodedValue.substring(17));
        } else if (encodedValue.startsWith("(java.lang.Byte)")){
            return Byte.valueOf(encodedValue.substring(16));
        } else if (encodedValue.startsWith("(java.lang.String)")){
            return encodedValue.substring(18);
        } else if (encodedValue.startsWith("(java.math.BigInteger)")){
            return new BigInteger(encodedValue.substring(22));
        } else if (encodedValue.startsWith("(java.math.BigDecimal)")){
            return new BigDecimal(encodedValue.substring(22));
//      } else if(encodedValue.indexOf('%') >= 0) {
//          return URITransformation.decode(encodedValue);
        } else {
            return encodedValue;
        }
    }
     
}
