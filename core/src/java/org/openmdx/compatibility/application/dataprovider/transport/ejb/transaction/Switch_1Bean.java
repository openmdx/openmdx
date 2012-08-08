/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Switch_1Bean.java,v 1.1 2008/02/19 13:56:23 hburger Exp $
 * Description: Combined Data Provider & Persistence Manager Implementatio
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 13:56:23 $
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
package org.openmdx.compatibility.application.dataprovider.transport.ejb.transaction;

import javax.jdo.PersistenceManager;

import org.openmdx.base.object.spi.PersistenceManagerFactory_2_0;
import org.openmdx.base.object.spi.PersistenceManager_2_0;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1LocalConnection;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1LocalHome;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1_1Local;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.server.Dataprovider_1Bean;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.runtime1.layer.application.LayerConfigurationEntries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combined Data Provider & Persistence Manager Implementation
 */
public class Switch_1Bean 
    extends Dataprovider_1Bean 
    implements PersistenceManagerFactory_2_0, PersistenceManager_2_0
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -864519656076938154L;

    /**
     * 
     */
    private Path[] scopes;

    /**
     * The SLF4J logger
     */
    private Logger logger;
    
    /**
     * The configured persistence manager factories which were available during
     * activation.
     */
    private SparseList<PersistenceManagerFactory_2_0> persistenceManagerFactories;
    
    
    //------------------------------------------------------------------------
    // Extends Dataprovider_1Bean
    //------------------------------------------------------------------------

    /**
     * Activates the EJB
     */
    protected void activate(
        Configuration configuration
    ) throws Exception {
        this.logger = LoggerFactory.getLogger(Switch_1Bean.class);
        super.activate(configuration);
        SparseList<?> scopes = configuration.values(
            LayerConfigurationEntries.EXPOSED_PATH
        );
        this.scopes = new Path[scopes.size()];
        for(
            int i = 0;
            i < this.scopes.length;
            i++
        ){ 
            Object exposedPath = scopes.get(i);
            this.scopes[i] = exposedPath instanceof Path ?
                (Path)exposedPath : 
                new Path(exposedPath.toString())
            ;
        }
        this.persistenceManagerFactories = configuration.values(
            SharedConfigurationEntries.DATAPROVIDER_CONNECTION
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.application.dataprovider.transport.ejb.server.Dataprovider_1Bean#newDataproviderConnection(java.lang.String)
     */
    @Override
    protected Dataprovider_1_1Connection newDataproviderConnection(
        String jndiEntry
    ) {
        String jndiPath = DATAPROVIDER_NAME_CONTEXT + '/' + jndiEntry;
        try {
            return new Dataprovider_1LocalConnection<Dataprovider_1_1Local>(
                (Dataprovider_1_1Local) ((Dataprovider_1LocalHome)
                    getConfigurationContext().lookup(jndiPath)
                ).create()
            );
        } catch (Exception exception) {
            this.logger.error(
                "Acquisition of EJB object '" + jndiPath + "' failed", 
                exception
            );
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.application.dataprovider.transport.ejb.server.Dataprovider_1Bean#deactivate()
     */
    public void deactivate(
    ) throws Exception {
        this.scopes = null;
        super.deactivate();
        this.persistenceManagerFactories = null;
        this.logger = null;
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceManagerFactory_2_0
    //------------------------------------------------------------------------

    /**
     * Tells, which of the configured persistence manager factories should be 
     * used.
     * 
     * @param objectId
     * 
     * @return the persistence manager factory index; 
     * or <code>-1</code> if there is no matching persistence manager factory
     */
    protected int getPersistenceManagerFactoryIndex(
        Object oid 
    ){
        Path path = new Path(oid.toString());
        for (
            int i = 0;
            i < this.scopes.length;
            i++
        ) {
           if (path.startsWith(this.scopes[i])) return i;
        }
        return -1;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.object.cci.PersistenceManagerFactory_2_0#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager(
    ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.cci.PersistenceManagerFactory_2_0#getPersistenceManager(java.lang.String, java.lang.String)
     */
    public PersistenceManager getPersistenceManager(
        String userid,
        String password
    ) {
        // TODO Auto-generated method stub
        return null;
    }
   
}
