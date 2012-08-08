/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Jmi1AccessorFactory_1.java,v 1.2 2009/02/19 19:41:07 hburger Exp $
 * Description: Persistence Manager Factory 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/19 19:41:07 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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

import java.util.Map;

import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.security.auth.Subject;

import org.openmdx.application.dataprovider.accessor.Connection_1;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_1;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1;
import org.openmdx.base.accessor.view.Manager_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;

/**
 * Persistence Manager Factory
 *
 * @since openMDX 1.13
 */
public class Jmi1AccessorFactory_1 
extends AbstractPersistenceManagerFactory_1 
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6593277236725994669L;

    /**
     * Constructor 
     *
     * @param configuration
     */
    private Jmi1AccessorFactory_1(
        Map<String,Object> configuration
    ) {
        super(configuration);
    }

    /**
     * 
     */
    private transient Dataprovider_1_1Connection connection;

    /**
     * Get instance
     * 
     * @param properties
     * 
     * @return a new instance
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory(
        Map<String,Object> properties
    ){
        Jmi1AccessorFactory_1 persistenceManagerFactory = new Jmi1AccessorFactory_1(
            properties
        );
        if (properties.containsKey(ConfigurableProperty.ConnectionFactory.qualifiedName())) {
            //
            // Standard Property
            //
            Dataprovider_1ConnectionFactory connectionFactory = (Dataprovider_1ConnectionFactory)properties.get(
                ConfigurableProperty.ConnectionFactory.qualifiedName()
            );
            persistenceManagerFactory.setConnectionFactory(connectionFactory);
        } else if(properties.containsKey(Dataprovider_1ConnectionFactory.class.getName())) {
            //
            // Compatibility Mode
            //
            Dataprovider_1ConnectionFactory connectionFactory = (Dataprovider_1ConnectionFactory)properties.get(
                Dataprovider_1ConnectionFactory.class.getName()
            );
            persistenceManagerFactory.setConnectionFactory(connectionFactory);
        }
        persistenceManagerFactory.freeze();
        return persistenceManagerFactory;
    }


    //------------------------------------------------------------------------
    // Extends AbstractPersistenceManagerFactory
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManagerProxy()
     */
    public PersistenceManager getPersistenceManagerProxy() {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     */
    private Dataprovider_1_1Connection getConnection(
    ) throws ServiceException{
        if(this.connection == null) {
            Dataprovider_1ConnectionFactory connectionFactory = (Dataprovider_1ConnectionFactory) this.getConnectionFactory();
            if(connectionFactory == null) {
                String jndiName = this.getConnectionFactoryName();
                if(jndiName == null) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE,
                    "Neither the connection factory nor the connection factory name has been set"
                );
                this.connection = new LateBindingConnection_1(
                    jndiName
                );
            } else {
                this.connection = connectionFactory.createConnection();
            }
        }
        return this.connection;
    }

    /**
     * Create a new persistence manager for legacy delegation
     * 
     * @param serviceHeader
     * 
     * @return a new persistence manager for legacy delegation
     */
    protected PersistenceManager newPersistenceManager(
        ServiceHeader serviceHeader
    ){

        try {
            boolean containerManaged = isContainerManaged();
            boolean transactionPolicyIsNew = !containerManaged;
            RefPackage_1_1 refPackage = new RefRootPackage_1(
                this,
                new Manager_1(
                    new Connection_1(
                        new RequestCollection(
                            serviceHeader,
                            getConnection()
                        ),
                        transactionPolicyIsNew,
                        null, // userTransaction
                        containerManaged,
                        getOptimistic(),
                        "UUID" // defaultQualifierType
                    )
                ),
                null, // packageImpls
                null, // userObjects
                null // principals
            );
            refPackage.refModel();
            return refPackage.refPersistenceManager();  
        } catch (ServiceException exception) {
            throw new JDOFatalUserException(
                "Persistence manager establishment failed",
                exception
            );
        }
    }

    /**
     * Create a new persistence manager
     * <p>
     * The parameters may be kept by the instance.
     * 
     * @param notifier
     * @param connectionUsername
     * @param connectionPassword
     * 
     * @return a new persistence manager
     */
    @Override 
    protected synchronized PersistenceManager newManager(
    ){
        return newPersistenceManager(
            new ServiceHeader()
        );
    }

    /**
     * Create a new persistence manager
     * <p>
     * The parameters may be kept by the instance.
     * 
     * @param subject a subject with a single PasswordCredential
     * 
     * @return a new persistence manager
     */
    @Override 
    protected synchronized PersistenceManager newManager(
        Subject subject
    ){
        return newPersistenceManager(
            ServiceHeader.toServiceHeader(subject)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory#initialize(javax.jdo.PersistenceManager)
     */
    @Override
    protected void initialize(PersistenceManager persistenceManager) {
        initialize(persistenceManager, false);
    }

}
