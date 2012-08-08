/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceManagerFactory_2_0.java,v 1.6 2008/02/29 18:04:14 hburger Exp $
 * Description: Persistence Manager Factory Interface 2.0
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 18:04:14 $
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
package org.openmdx.base.object.spi;

import javax.jdo.PersistenceManager;

import org.openmdx.base.exception.ServiceException;

/**
 * Persistence Manager Factory Interface 2.0
 */
public interface PersistenceManagerFactory_2_0 {

    /** 
     * Get an instance of <code>PersistenceManager</code> from this factory.  
     * The instance has default values for options.
     *
     * @return a <code>PersistenceManager</code> instance with default options.
     * 
     * @throws ServiceException if the persistence manager acquisition fails
     */
    PersistenceManager getPersistenceManager(
    ) throws ServiceException;

    /** 
     * Get an instance of <code>PersistenceManager</code> from this factory.  
     * The instance has default values for options.  
     * The parameters <code>userid</code> and <code>password</code> are used 
     * when obtaining datastore connections from the connection pool.
     *
     * @param userid the userid for the connection
     * @param password the password for the connection
     * 
     * @return a <code>PersistenceManager</code> instance with default options.
     * 
     * @throws ServiceException if the persistence manager acquisition fails
     */
    PersistenceManager getPersistenceManager(
        String userid, 
        String password
    ) throws ServiceException;
    
}