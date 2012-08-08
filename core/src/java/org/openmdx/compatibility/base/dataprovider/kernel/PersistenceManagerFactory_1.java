/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceManagerFactory_1.java,v 1.1 2008/11/07 17:47:41 hburger Exp $
 * Description: PersistenceManagerFactory_1 
 * Revision:    $Revision: 1.1 $
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;
import javax.security.auth.Subject;

import org.openmdx.base.persistence.spi.AbstractManagerFactory;
import org.openmdx.base.persistence.spi.ManagerFactory_2_0;

/**
 * PersistenceManagerFactory_1
 */
class PersistenceManagerFactory_1
    extends AbstractManagerFactory
{

    /**
     * Constructor 
     *
     * @param configuration
     */
    PersistenceManagerFactory_1(
        Map<String, Object> configuration
    ) {
        super(configuration);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -1727834811596343077L;

    /**
     * Public credential used as persistence manager request marker!
     */
    private static final Set<?> PERSISTENCE_MANAGER_REQUEST = Collections.singleton(
        ManagerFactoryConfigurationEntries.PERSISTENCE_MANAGER
    );
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractManagerFactory#newManager(javax.security.auth.Subject)
     */
    @Override
    protected PersistenceManager newManager(
        Subject subject
    ) { 
        ManagerFactory_2_0 managerFactory = (ManagerFactory_2_0) super.getConnectionFactory();
        try {
            return managerFactory.createManager(subject);
        } catch (ResourceException exception) {
            throw new JDOFatalUserException(
                "Persistence manager acquisition failure",
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractManagerFactory#newManager()
     */
    @Override
    protected PersistenceManager newManager(
    ) {
        return newManager(
            new Subject(
                true, // readOnly, 
                NO_PRINCIPALS,
                PERSISTENCE_MANAGER_REQUEST,
                NO_CREDENTIALS // private credentials           
            )
        );
    }

    /**
     * Create a read only subject based on the given credentials
     * 
     * @param username
     * @param password
     * 
     * @return a new subject
     */
    @Override
    protected Subject toSubject(
        String username,
        String password
    ){
        return toSubject(
            username, 
            password, 
            PERSISTENCE_MANAGER_REQUEST
        );
    }
    
}
