/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: DataObjectManagerFactory_1.java,v 1.2 2009/06/08 17:09:03 hburger Exp $
 * Description: Data Object Manager Factory
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/08 17:09:03 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

package org.openmdx.base.accessor.rest;

import java.util.Collections;
import java.util.List;

import javax.resource.cci.Connection;

import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1;
import org.openmdx.base.resource.spi.TransactionManager;


/**
 * Data Object Manager Factory
 */
public class DataObjectManagerFactory_1
    extends AbstractPersistenceManagerFactory_1 {

    /**
     * Constructor 
     *
     * @param principalChain
     * @param connection
     * @param transactionManager
     */
    public DataObjectManagerFactory_1(
        List<String> principalChain,
        Connection connection,
        TransactionManager transactionManager
    ) {
        super(Collections.emptyMap());
        this.principalChain = principalChain;
        this.connection = connection;
        this.transactionManager = transactionManager;
    }

    /**
     * Implements <code>Serializabel</code>
     */
    private static final long serialVersionUID = 3660778587310596327L;

    /**
     * 
     */
    private final List<String> principalChain;
    
    /**
     * The shared connection
     */
    private final Connection connection;
    
    /**
     * The transaction manager object
     */
    private final TransactionManager transactionManager;
        
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1#newManager(java.util.List)
     */
    @Override
    protected DataObjectManager_1_0 newManager(
        List<String> principalChain
    ) {
        return new DataObjectManager_1(
            this,
            principalChain,
            this.connection,
            this.transactionManager
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1#newManager()
     */
    @Override
    protected DataObjectManager_1_0 newManager() {
        return newManager(this.principalChain);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1#getPersistenceManager()
     */
    @Override
    public final DataObjectManager_1_0 getPersistenceManager() {
        return (DataObjectManager_1_0) super.getPersistenceManager();
    }

}
