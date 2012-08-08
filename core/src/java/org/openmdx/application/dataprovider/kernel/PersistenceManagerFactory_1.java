/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceManagerFactory_1.java,v 1.6 2009/05/29 17:04:09 hburger Exp $
 * Description: PersistenceManagerFactory_1 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/29 17:04:09 $
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

package org.openmdx.application.dataprovider.kernel;

import java.util.List;
import java.util.Map;

import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;

import org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1;
import org.openmdx.base.persistence.cci.EntityManagerFactory;

/**
 * PersistenceManagerFactory_1
 */
class PersistenceManagerFactory_1
    extends AbstractPersistenceManagerFactory_1
{

    /**
     * Constructor 
     *
     * @param configuration
     */
    protected PersistenceManagerFactory_1(
        Map<String, Object> configuration
    ) {
        super(configuration);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -1727834811596343077L;

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1#getConnectionFactory()
     */
    @Override
    public EntityManagerFactory getConnectionFactory(
    ) {
        return (EntityManagerFactory) super.getConnectionFactory();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractManagerFactory#newManager(javax.security.auth.Subject)
     */
    @Override
    protected PersistenceManager newManager(
        List<String> principalChain
    ) { 
        try {
            return getConnectionFactory().getEntityManager(principalChain);
        } catch (ResourceException exception) {
            throw new JDOFatalUserException(
                "Persistence manager acquisition failure",
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1#newManager()
     */
    @Override
    protected PersistenceManager newManager() {
        try {
            return getConnectionFactory().getEntityManager();
        } catch (ResourceException exception) {
            throw new JDOFatalUserException(
                "Persistence manager acquisition failure",
                exception
            );
        }
    }

}
