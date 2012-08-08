/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Connection_2.java,v 1.5 2008/06/27 13:56:09 hburger Exp $
 * Description: PersistenceManager_2_0 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/27 13:56:09 $
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
package org.openmdx.kernel.persistence.resource;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.openmdx.kernel.callback.CloseCallback;

/**
 * Accessor
 */
public abstract class Connection_2 implements PersistenceManager {
     
    /**
     * Constructor 
     */
    protected Connection_2() {
    }

    /**
     * Constructor 
     *
     * @param persistenceManagerFactory
     */
    protected Connection_2(
        PersistenceManagerFactory persistenceManagerFactory
    ) {
        setPersistenceManagerFactory(persistenceManagerFactory);
    }
    
    /**
     * The connection factory
     */
    private PersistenceManagerFactory persistenceManagerFactory;

    /**
     * Set persistenceManagerFactory.
     * 
     * @param persistenceManagerFactory The persistenceManagerFactory to set.
     */
    public void setPersistenceManagerFactory(
        PersistenceManagerFactory persistenceManagerFactory
    ) {
        if(this.persistenceManagerFactory == null) {
            this.persistenceManagerFactory = persistenceManagerFactory;
        } else if (persistenceManagerFactory != persistenceManagerFactory) throw new IllegalStateException(
            "The persistence manager's factory is already set"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getPersistenceManagerFactory()
     */
    public PersistenceManagerFactory getPersistenceManagerFactory() {
        if(this.persistenceManagerFactory != null) {
            return this.persistenceManagerFactory;
        } else throw new IllegalStateException(
            "The persistence manager's factory is not yet set"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#isClosed()
     */
    public boolean isClosed() {
        return this.persistenceManagerFactory == null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#close()
     */
    public void close() {        
        if(persistenceManagerFactory instanceof CloseCallback) {
            ((CloseCallback)this.persistenceManagerFactory).postClose(this);
        }
        this.persistenceManagerFactory = null;
    }
    
}
