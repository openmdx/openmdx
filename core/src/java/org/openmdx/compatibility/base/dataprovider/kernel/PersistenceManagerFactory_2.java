/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceManagerFactory_2.java,v 1.7 2008/07/02 16:07:24 hburger Exp $
 * Description: Persistence Manager Factory
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/07/02 16:07:24 $
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

package org.openmdx.compatibility.base.dataprovider.kernel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.resource.ResourceException;
import javax.security.auth.Subject;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_4;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.spi.AbstractManagerFactory;
import org.openmdx.compatibility.base.application.cci.ConfigurationProvider_1_0;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Provider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_2Connection;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_5;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;

/**
 * Persistence Manager Factory
 */
public class PersistenceManagerFactory_2
    extends Dataprovider_1
    implements Dataprovider_1_2Connection
{

    /**
     * Constructor 
     *
     * @param dataproviderConfiguration
     * @param configurationProvider
     * @param self
     * 
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    public PersistenceManagerFactory_2(
        Configuration dataproviderConfiguration,
        ConfigurationProvider_1_0 configurationProvider,
        Dataprovider_1_0 self
    ) throws ServiceException {
        super(dataproviderConfiguration, configurationProvider, self);
        this.self = self;
        String[] section = {
            ManagerFactoryConfigurationEntries.PERSISTENCE_MANAGER, 
            ManagerFactoryConfigurationEntries.LEGACY_PLUG_IN
        };
        this.legacyPlugInConfiguration = getPlugInConfiguration(
            configurationProvider,
            section,
            Collections.EMPTY_MAP
        );        
    }
    
    /**
     * The legacy plug-in's configuration
     */
    private final Configuration legacyPlugInConfiguration;
    
    /**
     * The EJB entry 
     */
    private final Dataprovider_1_0 self;
    
    
    //------------------------------------------------------------------------
    // Implements ManagerFactory_2_0
    //------------------------------------------------------------------------
    
    @SuppressWarnings("unchecked")
    protected PersistenceManager newPersistenceManager(
        ServiceHeader serviceHeader
    ) throws ServiceException{
        RequestCollection delegation = new RequestCollection(
            serviceHeader,
            this.self
        );
        Provider_1_0 provider = new Provider_1(
            delegation,
            false // transactionPolicyIsNew
        );
        Connection_1_5 interaction = new Connection_1(
            provider,
            false, // containerManagedUnitOfWork
            "UUID" // defaultQualifierType
        ); 
        interaction.setModel(getModel());
        ObjectFactory_1_0 manager = new Manager_1(interaction);
        try {
            // 
            // Not static reference to RefRootPackage_1 because of compilation dependencies!
            //
            Class<RefPackage_1_4> rootPkgClass = Classes.getApplicationClass(
                "org.openmdx.base.accessor.jmi.spi.RefRootPackage_1"
            );
            Constructor<RefPackage_1_4> rootPkgCons = rootPkgClass.getConstructor(
                PersistenceManagerFactory.class,
                ObjectFactory_1_0.class,
                Map.class
            );
            RefPackage_1_4 refPackage = rootPkgCons.newInstance(
                null, // persistenceManagerFactory
                manager,
                this.legacyPlugInConfiguration.values(
                    IMPLEMENTATION_MAP
                ).get(
                    0
                )
            );
            PersistenceManager persistenceManager = refPackage.refPersistenceManager();
            EntityManagerFactory_2.propagateUserObjects(
                this.legacyPlugInConfiguration, 
                persistenceManager
            );
            return persistenceManager;
        } catch(InvocationTargetException e) {
            throw new ServiceException(
                BasicException.toStackedException(e.getTargetException())
            );
        } catch (Exception exception) {
            throw new ServiceException(exception);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager(javax.security.auth.Subject)
     */
    public PersistenceManager createManager(
        Subject subject
    ) throws ResourceException {
        try {
            return newEntityManager(
                newPersistenceManager(
                    AbstractManagerFactory.toServiceHeader(subject)
                )
            );
        } catch (ServiceException exception) {
            throw new ResourceException(
                "Persistence manager acquiwition failure",
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager()
     */
    public PersistenceManager createManager(
    ) throws ResourceException {
        try {
            return newEntityManager(
                newPersistenceManager(
                    new ServiceHeader()
                )
            );
        } catch (ServiceException exception) {
            throw new ResourceException(
                "Persistence manager acquiwition failure",
                exception
            );
        }
    }

}
