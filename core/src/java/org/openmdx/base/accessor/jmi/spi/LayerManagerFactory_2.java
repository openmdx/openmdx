/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Layer Manager Factory 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2010, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.base.persistence.spi.DelegatingPersistenceManagerFactory;

/**
 * Layer Manager Factory 
 */
class LayerManagerFactory_2 extends DelegatingPersistenceManagerFactory {

    /**
     * Constructor 
     *
     * @param persistenceManagerFactory
     * @param plugInConfiguration
     */
    @SuppressWarnings("unchecked")
    LayerManagerFactory_2 (
        PersistenceManagerFactory persistenceManagerFactory,        
        Configuration plugInConfiguration 
    ){
        this(
            persistenceManagerFactory,
            (Map<String,String>)plugInConfiguration.values("implementationMap").get(0), 
            (Map<String,Object>)plugInConfiguration.values("userObjects").get(0)
        );
    }

    /**
     * Constructor 
     *
     * @param delegate
     * @param implementationMap
     * @param userObjects
     */
    private LayerManagerFactory_2 (
        PersistenceManagerFactory delegate,
        Map<String,String> implementationMap, 
        Map<String,Object> userObjects
    ){
        this.delegate = delegate;
        this.plugInMapping = LayerManagerFactory_2.newPlugInMapping(
            delegate, 
            implementationMap
        );
        this.userObjects = userObjects;
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3412830449142819058L;

    /**
     * 
     */
    private final Mapping_1_0 plugInMapping;
    
    /**
     * 
     */
    private final Map<String,Object> userObjects;
    
    /**
     * 
     */
    private final PersistenceManagerFactory delegate;            

    /**
     * Retrieve the plug-in mapping
     */
    static Mapping_1_0 newPlugInMapping(
        PersistenceManagerFactory nextFactory,
        Map<String,String> packageImpls
    ){
        return new ImplementationMapping_1(
            nextFactory instanceof LayerManagerFactory_2 ? ((LayerManagerFactory_2)nextFactory).plugInMapping : null,
            packageImpls
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory#delegate()
     */
    @Override
    protected PersistenceManagerFactory delegate() {
        return this.delegate;
    }

    /**
     * Create a layer specific <code>PersistenceManager</code>
     * 
     * @param delegate
     * 
     * @return a layer specific <code>PersistenceManager</code>
     */
    protected PersistenceManager newLayerManager (
        PersistenceManager delegate
    ){
        return new RefRootPackage_1(
          this, // persistenceManagerFactory
          delegate,
          this.plugInMapping,
          this.userObjects
        ).refPersistenceManager();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory#getPersistenceManager()
     */
    @Override
    public final PersistenceManager getPersistenceManager() {
        return this.newLayerManager(
            super.getPersistenceManager()
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory#getPersistenceManager(java.lang.String, java.lang.String)
     */
    @Override
    public final PersistenceManager getPersistenceManager(
        String userid,
        String password
    ) {
        return this.newLayerManager(
            super.getPersistenceManager(userid, password)
        );
    }

}