/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ManagerFactory_2.java,v 1.20 2009/02/24 15:48:55 hburger Exp $
 * Description: Persistence Manager Factory
 * Revision:    $Revision: 1.20 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/24 15:48:55 $
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

package org.openmdx.application.dataprovider.kernel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.resource.ResourceException;
import javax.security.auth.Subject;

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.accessor.Connection_1;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.OptimisticTransaction_2_0;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1_2Connection;
import org.openmdx.base.accessor.cci.PersistenceManager_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_1;
import org.openmdx.base.accessor.view.Manager_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;

/**
 * Persistence Manager Factory
 */
public class ManagerFactory_2
    extends Dataprovider_1
    implements Dataprovider_1_2Connection
{

    /**
     * Constructor 
     *
     * @param dataproviderConfiguration
     * @param configurationProvider
     * @param self
     * @param sessionContext 
     * @throws ServiceException
     */
    public ManagerFactory_2(
        Configuration dataproviderConfiguration,
        ConfigurationProvider_1_0 configurationProvider,
        Dataprovider_1_0 self, 
        SessionContext sessionContext
    ) throws ServiceException {
        super(
            dataproviderConfiguration, 
            configurationProvider, 
            self
        );
        this.self = self;
        this.sessionContext = sessionContext;
        this.legacyPlugInConfiguration = getPlugInConfiguration(
            configurationProvider,
            LEGACY_PLUG_IN_CONFUGURATION_SECTION,
            NO_USER_OBJECTS
        );     
        Map<String,Object> persistenceManagerFactoryConfiguration = new HashMap<String,Object>();
        persistenceManagerFactoryConfiguration.put(
            ConfigurableProperty.ConnectionFactory.qualifiedName(), 
            self // an EntityManagerFactory instance!
        );
        this.persistenceManagerFactory = new PersistenceManagerFactory_1(
            persistenceManagerFactoryConfiguration
        );
    }
    
    private final static String[] LEGACY_PLUG_IN_CONFUGURATION_SECTION = {
        ManagerFactoryConfigurationEntries.PERSISTENCE_MANAGER, 
        ManagerFactoryConfigurationEntries.LEGACY_PLUG_IN
    };
    private final static Map<String,Object> NO_USER_OBJECTS = Collections.emptyMap();

    private final PersistenceManagerFactory persistenceManagerFactory;

    /**
     * The legacy plug-in's configuration
     */
    private final Configuration legacyPlugInConfiguration;
    
    /**
     * The EJB entry 
     */
    private final Dataprovider_1_0 self;
    
    /**
     * The EJB session context
     */
    private final SessionContext sessionContext;
    
    
    //------------------------------------------------------------------------
    // Implements ManagerFactory_2_0
    //------------------------------------------------------------------------
    
    protected PersistenceManager_1_0 newObjectFactory(
        PersistenceManager_1_0 interaction
    ) throws ServiceException {
        return new Manager_1(
            interaction
        );
    }

    /**
     * Create a new persistence manager
     * 
     * @param serviceHeader
     * @param principals
     * 
     * @return a newly created persistence manager
     *  
     * @throws ServiceException
     */
    protected PersistenceManager newPersistenceManager(
        ServiceHeader serviceHeader, 
        Set<? extends Principal> principals
    ) throws ServiceException{
        RequestCollection delegation = new RequestCollection(
            serviceHeader,
            this.self,
            isLenient()
        );
        PersistenceManager_1_0 interaction = this.self instanceof OptimisticTransaction_2_0 ? new Connection_1(
            delegation,
            false, // transactionPolicyIsNew
            (OptimisticTransaction_2_0) this.self,
            "UUID" // defaultQualifierType
        ) : new Connection_1(
            delegation,
            false, // transactionPolicyIsNew
            false, // containerManagedUnitOfWork
            "UUID" // defaultQualifierType
        ); 
        try {
            // 
            // Not static reference to RefRootPackage_1 because of compilation dependencies!
            //
            Class<RefPackage_1_1> rootPkgClass = Classes.getApplicationClass(
                "org.openmdx.base.accessor.jmi.spi.RefRootPackage_1"
            );
            Constructor<RefPackage_1_1> rootPkgCons = rootPkgClass.getConstructor(
                PersistenceManagerFactory.class,
                PersistenceManager_1_0.class,
                Map.class,
                Map.class,
                Set.class
            );            
            return rootPkgCons.newInstance(
                this.persistenceManagerFactory,
                newObjectFactory(interaction),
                this.legacyPlugInConfiguration.values(
                    PlugInManagerFactory_2.IMPLEMENTATION_MAP
                ).get(
                    0
                ),
                this.legacyPlugInConfiguration.values(
                    PlugInManagerFactory_2.USER_OBJECT_MAP
                ).get(
                    0
                ),
                principals
            ).refPersistenceManager();
        } catch(InvocationTargetException e) {
            throw new ServiceException(
                BasicException.toExceptionStack(e.getTargetException())
            );
        } catch (Exception exception) {
            throw new ServiceException(exception);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager(javax.security.auth.Subject)
     */
    public PersistenceManager getEntityManager(
        Subject subject
    ) throws ResourceException {
        if(subject.getPublicCredentials().contains(ManagerFactoryConfigurationEntries.PERSISTENCE_MANAGER)) {
            try {
                return newPersistenceManager(
                    ServiceHeader.toServiceHeader(subject), 
                    Collections.singleton(
                        this.sessionContext.getCallerPrincipal()
                    )
                );
            } catch (ServiceException exception) {
                throw new ResourceException(
                    "Persistence manager acquisition failure",
                    exception
                );
            }
        } else {
            try {
                return newEntityManager(
                    newPersistenceManager(
                        ServiceHeader.toServiceHeader(subject), 
                        subject.getPrincipals()
                    )
                );
            } catch (ServiceException exception) {
                throw new ResourceException(
                    "Entity manager acquisition failure",
                    exception
                );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager()
     */
    public PersistenceManager getEntityManager(
    ) throws ResourceException {
        try {
            return newEntityManager(
                newPersistenceManager(
                    new ServiceHeader(), 
                    Collections.singleton(
                        this.sessionContext.getCallerPrincipal()
                    )
                )
            );
        } catch (ServiceException exception) {
            throw new ResourceException(
                "Entity manager acquisition failure",
                exception
            );
        }
    }

}
