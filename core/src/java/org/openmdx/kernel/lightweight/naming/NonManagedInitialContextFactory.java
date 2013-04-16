/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Non-Managed Initial Context Factory
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2011, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.naming;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import org.openmdx.kernel.lightweight.transaction.LightweightTransactionManager;
import org.openmdx.kernel.lightweight.transaction.LightweightTransactionSynchronizationRegistry;
import org.openmdx.kernel.lightweight.transaction.LightweightUserTransaction;

/**
 * Non-Managed Initial Context Factory
 */
public class NonManagedInitialContextFactory extends AbstractInitialContextFactory {

    /**
     * Constructor
     * 
     * @throws NoInitialContextException 
     */
    public NonManagedInitialContextFactory(
    ) throws NoInitialContextException {
        if(initialContext == null) {
            initialize();
        }
    }
    
    /**
     * The URL scheme specific initial context
     */
    static private volatile Context initialContext;

    /**
     * This initialization code <b>must not</b> be executed in a managed 
     * environment.
     * 
     * @throws NoInitialContextException 
     */
    private static synchronized void initialize(
    ) throws NoInitialContextException {
        if(initialContext == null) {
            initialContext = newInitialContext(
                NonManagedInitialContextFactory.class.getName(),
                LightweightTransactionManager.getInstance(),
                LightweightTransactionSynchronizationRegistry.getInstance(),
                LightweightUserTransaction.getInstance()
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    public Context getInitialContext(
        Hashtable<?, ?> environment
    ) throws NamingException {
        return (Context) initialContext.lookup("");
    }

}
