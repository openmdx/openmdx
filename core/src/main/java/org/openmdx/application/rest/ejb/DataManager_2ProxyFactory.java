/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Data Manager Proxy Factory
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
package org.openmdx.application.rest.ejb;

import java.util.Collections;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.connector.EntityManagerProxyFactory_2;
import org.openmdx.kernel.jdo.JDOPersistenceManagerFactory;

/**
 * Data Manager Proxy Factory
 */
public class DataManager_2ProxyFactory extends EntityManagerProxyFactory_2 {

    /**
     * Constructor
     *
     * @param overrides the configuration properties
     * @param configuration the configuration properties
     * @param defaults for missing configuration and override properties
     */
    protected DataManager_2ProxyFactory(
        Map<?, ?> overrides,
        Map<?, ?> configuration,
        Map<?, ?> defaults
    ) {
        super(overrides, configuration, defaults);
    }
    
    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 8315208977943985083L;

    /**
     * The method is used by JDOHelper to construct an instance of 
     * {@code PersistenceManagerFactory} based on user-specified 
     * properties.
     * 
     * @param props
     * 
     * @return a new {@code PersistenceManagerFactory}
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory (
        Map<?,?> props
    ){
        return getPersistenceManagerFactory(
            Collections.EMPTY_MAP,
            props
        );
    }

    /**
     * The method is used by {@code JDOHelper} to construct an instance of 
     * {@code PersistenceManagerFactory} based on user-specified properties.
     * 
     * @param overrides
     * @param props
     * 
     * @return a new {@code PersistenceManagerFactory}
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory (
        Map<?,?> overrides, 
        Map<?,?> props
    ){
        return new DataManager_2ProxyFactory(overrides, props, DEFAULT_CONFIGURATION);
    }

    /**
     * Create a new Port
     * 
     * @param connectionFactory
     * 
     * @return a new {@code Port} for the given connection factory
     * 
     * @throws ServiceException
     */
    @Override
    protected Port<RestConnection> newPort(
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
