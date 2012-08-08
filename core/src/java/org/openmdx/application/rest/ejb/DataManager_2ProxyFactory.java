/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: DataManager_2ProxyFactory.java,v 1.16 2010/01/05 10:52:06 hburger Exp $
 * Description: Data Manager Proxy Factory
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/05 10:52:06 $
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
package org.openmdx.application.rest.ejb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManagerFactory;
import javax.resource.cci.ConnectionFactory;

import org.openmdx.application.rest.spi.EntityManagerProxyFactory_2;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.spi.Port;

/**
 * Data Manager Proxy Factory
 */
public class DataManager_2ProxyFactory extends EntityManagerProxyFactory_2 {

    /**
     * Constructor 
     *
     * @param configuration
     */
    protected DataManager_2ProxyFactory(
        Map<?,?> configuration
    ){
        super(configuration);
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 8315208977943985083L;

    /**
     * The method is used by JDOHelper to construct an instance of 
     * <code>PersistenceManagerFactory</code> based on user-specified 
     * properties.
     * 
     * @param props
     * 
     * @return a new <code>PersistenceManagerFactory</code>
     */
    @SuppressWarnings("unchecked")
    public static PersistenceManagerFactory getPersistenceManagerFactory (
        Map props
    ){
        return getPersistenceManagerFactory(
            Collections.EMPTY_MAP,
            props
        );
    }

    /**
     * The method is used by JDOHelper to construct an instance of 
     * <code>PersistenceManagerFactory</code> based on user-specified 
     * properties.
     * 
     * @param overrides
     * @param props
     * 
     * @return a new <code>PersistenceManagerFactory</code>
     */
    @SuppressWarnings("unchecked")
    public static PersistenceManagerFactory getPersistenceManagerFactory (
        Map overrides, 
        Map props
    ){
        Map<Object,Object> configuration = new HashMap<Object,Object>(DEFAULT_CONFIGURATION);
        configuration.putAll(props);
        configuration.putAll(overrides);
        return new DataManager_2ProxyFactory(configuration);
    }

    /**
     * Create a new Port
     * 
     * @param connectionFactory
     * 
     * @return a new <code>Port</code> for the given connection factory
     * 
     * @throws ServiceException
     */
    @Override
    protected Port newPort(
        final Object connectionFactory
    ) throws ServiceException{
        return connectionFactory instanceof ConnectionFactory ? super.newPort(
            connectionFactory
        ) : connectionFactory instanceof Connection_2LocalHome ? Connection_2LocalPort.newInstance(
            connectionFactory
        ) : Connection_2RemotePort.newInstance(
            connectionFactory
        );
    }
    
}
