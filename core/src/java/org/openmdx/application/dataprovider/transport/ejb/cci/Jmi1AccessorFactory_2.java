/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Jmi1AccessorFactory_2.java,v 1.2 2009/02/19 19:41:07 hburger Exp $
 * Description: AccessorFactory_2 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/19 19:41:07 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.transport.ejb.cci;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.resource.ResourceException;
import javax.security.auth.Subject;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.cci.EntityManagerFactory;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;

/**
 * Accessor Factory
 * <p><i> 
 * Note:<br>
 * The entity managers return jmi1 object's at the moment.
 * In future we will have to delegate to their cci2 instances!
 * </i> 
 */
public class Jmi1AccessorFactory_2
    extends AbstractPersistenceManagerFactory_1
{

    /**
     * Constructor 
     * <p><i> 
     * Note:<br>
     * The entity managers return jmi1 object's at the moment.
     * In future we will have to delegate to their cci2 instances!
     * </i> 
     *
     * @param configuration
     */
    protected Jmi1AccessorFactory_2(
        Map<String, Object> configuration
    ) {
        super(configuration);
    }

    /**
     * 
     */
    protected static final Map<String, Object> DEFAULT_CONFIGURATION = Collections.singletonMap(
        ConfigurableProperty.ConnectionFactoryName.qualifiedName(),
        (Object)"java:comp/env/ejb/EntityManagerFactory"
    );

    /**
     * The entity connection factory
     */
    private transient EntityManagerFactory managerFactory = null;

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3442827664670646249L;


    /**
     * JDO's standard factory method
     * 
     * @param properties
     * 
     * @return a new persistence manager factory instance
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory(
        Map<String, Object> properties
    ){
        Map<String, Object> configuration = new HashMap<String, Object>(DEFAULT_CONFIGURATION);
        configuration.putAll(properties);
        return new Jmi1AccessorFactory_2(configuration);
    }


    //------------------------------------------------------------------------
    // Extends AbstractPersistenceManagerFactory
    //------------------------------------------------------------------------

    /**
     * Retrieve the manager factory
     * 
     * @return the manager factory
     */
    private EntityManagerFactory getManagerFactory(){
        if(this.managerFactory == null) {
            Object connectionFactory = getConnectionFactory();
            this.managerFactory = EntityManagerFactory_2.getInstance(
                connectionFactory == null ? getConnectionFactoryName() : connectionFactory
            );
        }
        return this.managerFactory;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory#newPersistenceManager()
     */
    @Override
    protected synchronized PersistenceManager newManager(
    ) {
        try {
            return new RefRootPackage_1(
                this,
                getManagerFactory().getEntityManager()
            ).refPersistenceManager();
        } catch (ResourceException exception) {
            throw new JmiServiceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE,
                    "Manager acquisition failure"
                )
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory#newPersistenceManager(javax.security.auth.Subject)
     */
    @Override
    protected synchronized PersistenceManager newManager(
        Subject subject
    ) {
        try {
            return new RefRootPackage_1(
                this,
                getManagerFactory().getEntityManager(subject)
            ).refPersistenceManager();
        } catch (ResourceException exception) {
            throw new JmiServiceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE,
                    "Manager acquisition failure",
                    new BasicException.Parameter("subject", subject)
                )
            );
        }
    }

}
