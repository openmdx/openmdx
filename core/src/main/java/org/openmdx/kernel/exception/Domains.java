/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Exception Code 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.kernel.exception;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openmdx.base.collection.Maps;
import org.openmdx.kernel.log.SysLog;


/**
 * Exception Code
 */
public class Domains {
    
    /**
     * Constructor 
     *
     * @param locale
     */
    Domains(
    ){
        this(FAMILY);
    }

    /**
     * Constructor 
     *
     * @param locale
     */
    public Domains(
        Locale locale
    ){
        this.locale = locale;
    }
    
    /**
     * The locale to be used for the resource bundle access
     */
    private final Locale locale;
    
    /**
     * The locale used by the BasicException
     */
    private static final Locale FAMILY = new Locale("","","");

    /**
     * The registry is populated by the resource bundles
     */
    private final ConcurrentMap<String,Map<Integer,String>> domains = new ConcurrentHashMap<String,Map<Integer,String>>();
    
    /**
     * Externalize an exception code
     * 
     * @param exceptionDomain
     * @param exceptionCode
     * 
     * @return
     */
    public String toString(
        String exceptionDomain,
        int exceptionCode
    ){
        Map<Integer,String> domain = getDomain(
            exceptionCode > 0 ? exceptionDomain : "DefaultDomain"
        );
        String value = domain.get(Integer.valueOf(exceptionCode));
        return value == null ? Integer.toString(exceptionCode) : value;
    }
    
    /**
     * Look up a domain
     * 
     * @param domainId
     * 
     * @return the requested domain
     */
    private Map<Integer,String> getDomain(
        String domainId
    ){
        Map<Integer,String> domain = this.domains.get(domainId);
        return domain != null ? domain : Maps.putUnlessPresent(
            this.domains, 
            domainId, 
            newDomain(domainId)
        );
    }

    /**
     * Prepare a domain
     * 
     * @param domanId
     * 
     * @return the prepared domain
     */
    private Map<Integer,String> newDomain(
        String domainId
    ){
        Map<Integer,String> domain = new HashMap<Integer,String>();
        try {
            ResourceBundle resource = ResourceBundle.getBundle(
                "org.openmdx.exception.domain." + domainId, 
                this.locale
            );
            for(
                Enumeration<String> keys = resource.getKeys();
                keys.hasMoreElements();
            ){
                String key = keys.nextElement();
                try {
                    domain.put(
                        Integer.decode(key), 
                        resource.getString(key)
                    );
                } catch (Exception exception) {
                    SysLog.warning(
                        "Could not resolve exception code '" + key + "' for domain '" + domainId + "'", 
                        exception
                    );
                }
            }
        } catch (Exception e) {
            SysLog.info("Could not access resource bundle for domain '. Applying default." + domainId + "'", e);
        }
        return domain;
    }

}
