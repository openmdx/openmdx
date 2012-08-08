/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: NamingConfigurationProvider.java,v 1.3 2009/09/01 14:11:11 hburger Exp $
 * Description: Naming Configuration Provider
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/01 14:11:11 $
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
package org.openmdx.application.spi;

import java.util.Arrays;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.cci.ConfigurationSpecifier;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Naming Configuration Provider
 */
public class NamingConfigurationProvider implements ConfigurationProvider_1_0 {

    /**
     * Constructor 
     * @throws ServiceException 
     * 
     * @throws ServiceException  
     */
    public NamingConfigurationProvider(
        String namingContext,
        boolean strict
    ) throws ServiceException  {
        try {
            this.configurationContext = (Context)new InitialContext().lookup(namingContext);    
        } catch (Exception exception) {
            if(strict) {
                throw new ServiceException(
                    exception, 
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.ACTIVATION_FAILURE, 
                    "Could not retrieve component environmnet",
                    new BasicException.Parameter("class", getClass().getName()),
                    new BasicException.Parameter("context", namingContext)
                );
            }
        }
    }

    /**
     * the configuration context
     */
    private Context configurationContext;

    /**
     * Enumerates the names bound in the named context, along with the
     * class names of objects bound to them.
     *
     * @param name the name of the context to list
     * 
     * @return  an enumeration of the names and class names of the
     *      bindings in this context.  Each element of the
     *      enumeration is of type <tt>NameClassPair</tt>.
     *      
     * @throws  ServiceException if a naming exception is encountered
     */
    public NamingEnumeration<NameClassPair> list(
        String name
    ) throws ServiceException{
        try {
            return this.configurationContext.list(name);
        } catch (NamingException exception) {
            throw new ServiceException(exception);
        }
    }
        
    /* (non-Javadoc)
     * @see org.openmdx.application.cci.ConfigurationProvider_1_0#getConfiguration(java.lang.String[], java.util.Map)
     */
    public Configuration getConfiguration(
        String[] section,
        Map<String, ConfigurationSpecifier> specification
    ) throws ServiceException {
        return new EnvironmentConfiguration(
            this.configurationContext,
            section,
            specification
        );   
    }

    
    //------------------------------------------------------------------------
    // Class EnvironmentConfiguration
    //------------------------------------------------------------------------
    
    /**
     * Command option parser
     */
    static class EnvironmentConfiguration extends Configuration { 

        /**
         * Constructor
         *
         * @param       source
         *              the ejb context
         * @param       section
         *              the section to be parsed, my be null
         * @param       specification
         *              the configurations specification, may be null
         */
        @SuppressWarnings("unchecked")
        EnvironmentConfiguration(
            Context context,
            String[] section,
            Map specification
        ) throws ServiceException {
            super();
            if(context != null) {
                try {
                    Context base = context;
                    if(section != null) for(
                        int i = 0, iLimit = section.length - 1;
                        i < iLimit;
                        i++
                    ){
                        base = (Context) base.lookup(section[i]);
                    }
                    String sectionName =
                        section == null ? "" : section[section.length - 1];
                    for(
                        NamingEnumeration bindings = base.listBindings(sectionName);
                        bindings.hasMore();
                    ) try {
                        Binding binding = (Binding)bindings.next();
                        Object value = binding.getObject();
                        String name = binding.getName();
                        if(
                            SharedConfigurationEntries.WORK_AROUND_SUN_APPLICATION_SERVER_BINDINGS || 
                            !binding.isRelative()
                        ) {
                            name = name.substring(name.lastIndexOf('/') + 1);
                        }
                        if (
                            value instanceof String ||
                            value instanceof Boolean ||
                            value instanceof Integer ||
                            value instanceof Long ||
                            value instanceof Short
                        ) {
                            this.setValue(name,value, false);
                        }
                    } catch (NamingException exception) {
                        throw new ServiceException (
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN, 
                            BasicException.Code.ACTIVATION_FAILURE, 
                            "Naming exception in section",
                            new BasicException.Parameter("section", Arrays.toString(section))
                        );
                    }
                } catch (NamingException exception) {
                    // The section has no entries
                }
            }
        }

    }

}
