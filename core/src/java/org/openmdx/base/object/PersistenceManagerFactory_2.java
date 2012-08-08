/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceManagerFactory_2.java,v 1.13 2008/02/08 16:52:21 hburger Exp $
 * Description: Persistence Manager Factory Implementation 
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:52:21 $
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.object;

import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.openmdx.base.object.spi.AbstractPersistenceManagerFactory;
import org.openmdx.base.object.spi.InstanceLifecycleNotifier;
import org.openmdx.base.object.spi.PersistenceManager_2;

/**
 * Persistence Manager Factory Implementation
 *
 * @since openMDX 2.0
 */
public class PersistenceManagerFactory_2 
    extends AbstractPersistenceManagerFactory 
{

    /**
     * Implements <code>Serializable</code<
     */
    private static final long serialVersionUID = 4589403178120841132L;

    /**
     * Constructor 
     *
     * @param configuration
     * @param configurable
     */
    private PersistenceManagerFactory_2(
        Map<String,Object> configuration
    ) {
        super(configuration);
    }

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
        return new PersistenceManagerFactory_2(properties);
    }

        
    //------------------------------------------------------------------------
    // Extends AbstractPersistenceManagerFactory
    //------------------------------------------------------------------------

    /**
     * Create a new persistence manager
     * <p>
     * The parameters may be kept by the instance.
     * @param notifier
     * @param connectionUsername
     * @param connectionPassword
     * 
     * @return a new persistence manager
     */
    protected synchronized PersistenceManager newPersistenceManager(
        InstanceLifecycleNotifier notifier,
        String connectionUsername,
        String connectionPassword
    ){
        return new PersistenceManager_2 (
            this,
            notifier,
            connectionUsername,
            connectionPassword
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.AbstractPersistenceManagerFactory#newPersistenceManager(org.openmdx.base.object.spi.InstanceLifecycleNotifier)
     */
    protected PersistenceManager newPersistenceManager(
        InstanceLifecycleNotifier notifier
    ) {
        return new PersistenceManager_2 (
            this,
            notifier
        );
    }
    
}
