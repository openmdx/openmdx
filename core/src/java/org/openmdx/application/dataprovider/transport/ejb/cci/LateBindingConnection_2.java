/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LateBindingConnection_2.java,v 1.4 2009/06/08 17:12:04 hburger Exp $
 * Description: Late Binding Connection
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/08 17:12:04 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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

import java.util.Hashtable;

import javax.resource.spi.LocalTransactionException;
import javax.transaction.Synchronization;

import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1_3Connection;
import org.openmdx.base.resource.spi.TransactionManager;

/**
 * Late Binding Connection
 */
public class LateBindingConnection_2
    extends LateBindingConnection_1
    implements Dataprovider_1_3Connection
{

    /**
     * Constructor
     * 
     * @param dataproviderName the dataprovider connection factory's jndi name
     */
    public LateBindingConnection_2(
        String dataproviderName
    ) {
        super(dataproviderName);
    }

    /**
     * Constructor
     * 
     * @param dataproviderName the dataprovider connection factory's jndi name
     * @param environment the initial context's environment
     */
    public LateBindingConnection_2(
        String dataproviderName,
        Hashtable<?,?> environment
    ) {
        super(dataproviderName, environment);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 7877339443529668870L;

    
    //------------------------------------------------------------------------
    // Implements Dataprovider_1_3Connection
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.OptimisticTransaction_2_0#commit(javax.transaction.Synchronization)
     */
    public void commit(
        Synchronization synchronization
    ) throws LocalTransactionException {
        ((TransactionManager)getDelegate()).commit(synchronization);
    }

}
