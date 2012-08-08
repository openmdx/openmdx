/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: EntityManagerFactory_2.java,v 1.5 2009/03/03 17:23:08 hburger Exp $
 * Description: ManagerFactory_2 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/03 17:23:08 $
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
package org.openmdx.application.dataprovider.transport.ejb.cci;

import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1;
import org.openmdx.base.persistence.cci.EntityManagerFactory;
import org.openmdx.kernel.exception.BasicException;

public class EntityManagerFactory_2
    implements EntityManagerFactory {

    /**
     * Constructor 
     *
     * @param delegate
     */
    private EntityManagerFactory_2(
        PersistenceManagerFactory delegate
    ) {
        this.delegate = delegate;
    }

    /**
     * 
     */
    private final PersistenceManagerFactory delegate;

    /**
     * Factory method 
     * 
     * @param delegate
     * 
     * @return a new ManagerFactory_2_0 instance
     */
    public static EntityManagerFactory getInstance(
        PersistenceManagerFactory delegate
    ){
        return new EntityManagerFactory_2(delegate);
    }

    /**
     * Factory method 
     * 
     * @param the manager factory's JNDI name
     * 
     * @return a new ManagerFactory_2_0 instance
     */
    public static EntityManagerFactory getInstance(
        String jndiName
    ){
        try {
            Context initialContext = new InitialContext();
            Object instance = null;
            try {
                instance = initialContext.lookup(jndiName);
                EntityManagerFactory_2LocalHome home = (EntityManagerFactory_2LocalHome)instance;
                return home.create();
            } catch(ClassCastException exception){
                throw BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BIND_FAILURE,
                    "Can not cast instance to " + EntityManagerFactory_2LocalHome.class.getName(),
                    new BasicException.Parameter(
                        "classLoaders", 
                        System.identityHashCode(instance == null ? null : instance.getClass().getClassLoader()),
                        System.identityHashCode(EntityManagerFactory_2LocalHome.class.getClassLoader())
                    )
                );
            } finally {
                initialContext.close();
            }
        } catch (Exception exception) {
            throw new JDOFatalDataStoreException(
                "Can not acquire " + EntityManagerFactory.class.getSimpleName() + 
                " by looking up " + EntityManagerFactory_2LocalHome.class.getName() + 
                " at " + jndiName,
                exception
            );
        }

    }

    /**
     * Factory method 
     * 
     * @param object
     * 
     * @return a new ManagerFactory_2_0 instance
     */
    public static EntityManagerFactory getInstance(
        Object object
    ){
        if(object == null) {
            return null;
        } else if(object instanceof EntityManagerFactory) {
            return (EntityManagerFactory)object;
        } else if(object instanceof PersistenceManagerFactory) {
            return getInstance ((PersistenceManagerFactory)object);
        } else if(object instanceof String) {
            return getInstance ((String)object);
        } else throw new JDOFatalDataStoreException(
            "Can not create an instance based on the given object: " +
            object.getClass().getName()
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager(javax.security.auth.Subject)
     */
    public PersistenceManager getEntityManager(
        Subject subject
    ) throws ResourceException {
        PasswordCredential credential = AbstractPersistenceManagerFactory_1.getCredential(subject);
        return this.delegate.getPersistenceManager(
            credential.getUserName(),
            String.valueOf(credential.getPassword())
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager()
     */
    public PersistenceManager getEntityManager(
    ) throws ResourceException {
        return this.delegate.getPersistenceManager();
    }

}
