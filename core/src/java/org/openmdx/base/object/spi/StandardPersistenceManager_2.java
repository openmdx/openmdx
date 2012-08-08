/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StandardPersistenceManager_2.java,v 1.4 2006/03/29 22:23:23 hburger Exp $
 * Description: StandardPersistenceManager_2 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/03/29 22:23:23 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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

package org.openmdx.base.object.spi;

import javax.jdo.PersistenceManagerFactory;

/**
 * StandardPersistenceManager_2
 * 
 * @since openMDX 2.0
 */
public class StandardPersistenceManager_2
    extends AbstractPersistenceManager
{

    /**
     * Constructor 
     *
     * @param factory
     * @param notifier
     * @param connectionUsername
     * @param connectionPassword
     */
    public StandardPersistenceManager_2(
        PersistenceManagerFactory factory,
        InstanceLifecycleNotifier notifier,
        String connectionUsername,
        String connectionPassword
     ) {
        super(
            factory, 
            notifier, 
            connectionUsername, 
            connectionPassword
         );
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceManager
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getMultithreaded()
     */
    public final boolean getMultithreaded() {
        return false;
    }

}
