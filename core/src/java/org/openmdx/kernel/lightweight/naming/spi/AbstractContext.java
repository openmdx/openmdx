/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractContext.java,v 1.3 2010/10/11 06:49:06 hburger Exp $
 * Description: Abstract Context
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/10/11 06:49:06 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.kernel.lightweight.naming.spi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NameParser;
import javax.naming.NamingException;


/**
 * Abstract Context
 */
public abstract class AbstractContext implements Context {
        
    /**
     * The Context's name parser
     */
    protected final static NameParser nameParser = new CompositNameParser();
    
    /**
     * Constructor
     */
    protected AbstractContext(
        Map<?,?> environment
    ) {
        this.environment = environment == null ? 
            new Hashtable<Object,Object>() :
            new Hashtable<Object,Object>(environment);
    }

    
    /**
     * The context's environment
     */
    protected final Hashtable<Object,Object> environment;

    /**
     * Decode url property values
     * 
     * @param encodedValue
     * 
     * @return the native value
     */
    protected static Object decode(
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

    /**
     * Parse the URL's parameters
     * 
     * @param query the URL
     * 
     * @return the URL's parameters
     */
    protected static Map<String,?> getParameters(
        String query
    ){
        if(query == null || query.length() == 0)  {
            return Collections.emptyMap();
        } else {
            Map<String,Object> properties = new HashMap<String, Object>();
            String[] entries = query.split("&");
            for(String entry : entries) {
                int e = entry.indexOf('=');
                if(e < 0) {
                    properties.put(entry, null);
                } else {
                    properties.put(
                        entry.substring(0, e), 
                        AbstractContext.decode(entry.substring(e+1))
                    );
                }
            }
            return properties;
        }
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#close()
     */
    public void close(
    ) throws NamingException {
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#addToEnvironment(java.lang.String, java.lang.Object)
     */
    public Object addToEnvironment(
        String propName, 
        Object propVal
    ) throws NamingException {
        return this.environment.put(propName, propVal);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#getEnvironment()
     */
    public Hashtable<?,?> getEnvironment() throws NamingException {
        return (Hashtable<?, ?>) this.environment.clone();
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#removeFromEnvironment(java.lang.String)
     */
    public Object removeFromEnvironment(String propName) throws NamingException {
        return this.environment.remove(propName);
    }

    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
            return getClass().getName() + ": " + getNameInNamespace();
        } catch (NamingException e) {
            return super.toString();
        }
    }
    
}
