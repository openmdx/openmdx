/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: EntityManagerFactory_2.java,v 1.1 2008/06/30 15:41:09 hburger Exp $
 * Description: EntityManagerFactory_2 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/30 15:41:09 $
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
package org.openmdx.base.accessor.jmi.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.security.auth.Subject;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.persistence.spi.AbstractManagerFactory;

/**
 * EntityManagerFactory_2
 */
public class EntityManagerFactory_2
    extends AbstractManagerFactory
{

    /**
     * Constructor 
     *
     * @param configuration
     */
    public EntityManagerFactory_2(Map<String, Object> configuration) {
        super(configuration);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 2214307342768190978L;

    /**
     * 
     */
    protected static final Map<String, Object> DEFAULT_CONFIGURATION = Collections.emptyMap();

    /**
     * JDO's standard factory method
     * 
     * @param properties
     * 
     * @return a new persistence manager factory instance
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory(
        Map<String, Object> properties
    ){
        Map<String, Object> configuration = new HashMap<String, Object>(DEFAULT_CONFIGURATION);
        configuration.putAll(properties);
        return new EntityManagerFactory_2(configuration);
    }

    
    //------------------------------------------------------------------------
    // Extends AbstractPersistenceManagerFactory
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractManagerFactory#newManager(javax.security.auth.Subject)
     */
    @Override
    protected PersistenceManager newManager(Subject subject) {
        Object connectionFactory = getConnectionFactory();
        if(connectionFactory instanceof ObjectFactory_1_0) {
            
        } else {
            
        }
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractManagerFactory#newManager()
     */
    @Override
    protected PersistenceManager newManager() {
        // TODO Auto-generated method stub
        return null;
    }

}
