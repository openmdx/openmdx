/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PlugInManagerFactory_2.java,v 1.3 2008/11/07 17:47:41 hburger Exp $
 * Description: AbstractLayerManagerFactory 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/07 17:47:41 $
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
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOFatalInternalException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.openmdx.base.accessor.jmi.cci.RefPackage_1_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory;

/**
 * AbstractLayerManagerFactory
 */
public class PlugInManagerFactory_2
    extends DelegatingPersistenceManagerFactory
{

    /**
     * Constructor 
     *
     * @param persistenceManager
     * @param plugInConfiguration
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    public PlugInManagerFactory_2 (
        PersistenceManager persistenceManager,        
        Configuration plugInConfiguration
    ) throws ServiceException {
        this(
            persistenceManager.getPersistenceManagerFactory(), 
            (Map<String,String>)plugInConfiguration.values(IMPLEMENTATION_MAP).get(0),
            (Map<String,Object>)plugInConfiguration.values(USER_OBJECT_MAP).get(0)
        );
    }

    /**
     * Constructor 
     *
     * @param handleClass
     * @param implementationMap
     * @param userObjects
     * @throws ServiceException
     */
    private PlugInManagerFactory_2 (
        PersistenceManagerFactory delegate,
        Map<String,String> implementationMap,
        Map<String,Object> userObjects
    ) throws ServiceException {
        this.delegate = delegate;
        this.implementationMap = implementationMap;
        this.userObjects = userObjects;
        try {
            Class<RefPackage_1_1> rootPackageClass = Classes.getApplicationClass(
                "org.openmdx.base.accessor.jmi.spi.RefRootPackage_1"
            );
            this.rootPackageConstructor = rootPackageClass.getConstructor(
                PersistenceManagerFactory.class,
                PersistenceManager.class,
                Map.class,
                Map.class,
                Set.class
            );
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INITIALIZATION_FAILURE,
                "JMI package cosntructor acquisition failed",
                new BasicException.Parameter(
                    "class",
                    "org.openmdx.base.accessor.jmi.spi.RefRootPackage_1"
                ),
                new BasicException.Parameter(
                    "arguments", 
                    PersistenceManagerFactory.class.getName(),
                    PersistenceManager.class.getName(),
                    Map.class.getName(),                            
                    Map.class.getName(),
                    Set.class.getName()
                )
            );
        }
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3412830449142819058L;

    /**
     * User-object map.
     */
    final static String USER_OBJECT_MAP = "userObjects";

    /**
     * Package  implementation map
     */
    final static String IMPLEMENTATION_MAP = "implementationMap";

    /**
     * 
     */
    private final Constructor<RefPackage_1_1> rootPackageConstructor;

    /**
     * 
     */
    private final Map<String,String> implementationMap;

    /**
     * 
     */
    private final Map<String,Object> userObjects;

    /**
     * 
     */
    private final PersistenceManagerFactory delegate;

    /* (non-Javadoc)
     * @see org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory#delegate()
     */
    @Override
    protected PersistenceManagerFactory delegate() {
        return this.delegate;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory#getPersistenceManager()
     */
    @Override
    public PersistenceManager getPersistenceManager() {
        try {
            return newPersistenceManager(
                super.getPersistenceManager()
            );
        } catch (ServiceException exception) {
            throw new JDOFatalInternalException(
                "Persistence manager acquisition failure",
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory#getPersistenceManager(java.lang.String, java.lang.String)
     */
    @Override
    public PersistenceManager getPersistenceManager(
        String userid,
        String password
    ) {
        try {
            return newPersistenceManager(
                super.getPersistenceManager(userid, password)
            );
        } catch (ServiceException exception) {
            throw new JDOFatalInternalException(
                "Persistence manager acquisition failure",
                exception
            );
        }
    }

    /**
     * Create a new plug-in manager
     * 
     * @param delegate
     * 
     * @return a new plug-in manager
     * 
     * @throws ServiceException
     */
    protected PersistenceManager newPersistenceManager (
        PersistenceManager delegate
    ) throws ServiceException {
        try {
            return this.rootPackageConstructor.newInstance(
                this, // persistenceManagerFactory
                delegate,
                this.implementationMap,
                this.userObjects,
                delegate.getUserObject(UserObjects.PRINCIPALS)
            ).refPersistenceManager();
        } catch(InvocationTargetException e) {
            throw new ServiceException(
                BasicException.toStackedException(e.getTargetException())
            );
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

}
