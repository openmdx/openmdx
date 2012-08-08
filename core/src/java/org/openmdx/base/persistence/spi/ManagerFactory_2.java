/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ManagerFactory_2.java,v 1.8 2008/09/10 08:55:27 hburger Exp $
 * Description: ManagerFactory_2 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:27 $
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
package org.openmdx.base.persistence.spi;

import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.EntityManagerFactory_2LocalHome;
import org.openmdx.kernel.exception.BasicException;

/**
 * ManagerFactory_2
 */
public class ManagerFactory_2
implements ManagerFactory_2_0
{

    /**
     * Constructor 
     *
     * @param delegate
     */
    private ManagerFactory_2(
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
    public static ManagerFactory_2_0 getInstance(
        PersistenceManagerFactory delegate
    ){
        return new ManagerFactory_2(delegate);
    }

    /**
     * Factory method 
     * 
     * @param the manager factory's JNDI name
     * 
     * @return a new ManagerFactory_2_0 instance
     */
    public static ManagerFactory_2_0 getInstance(
        String jndiName
    ){
        try {
            Context initialContext = new InitialContext();
            Object instance = null;
            try {
                instance = initialContext.lookup(jndiName);
                EntityManagerFactory_2LocalHome home = (EntityManagerFactory_2LocalHome)instance;
                return home.create();
            }
            catch(ClassCastException e){
                throw new BasicException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BIND_FAILURE,
                    "Can not cast instance to " + EntityManagerFactory_2LocalHome.class.getName(),
                    new BasicException.Parameter("instance", instance),
                    new BasicException.Parameter("classloader.instance", (instance == null ? null : instance.getClass().getClassLoader())),
                    new BasicException.Parameter("classloader.cast", EntityManagerFactory_2LocalHome.class.getClassLoader())
                );
            }            
            finally {
                initialContext.close();
            }
        } catch (Exception exception) {
            throw new JDOFatalDataStoreException(
                "Can not acquire " + ManagerFactory_2_0.class.getSimpleName() + 
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
    public static ManagerFactory_2_0 getInstance(
        Object object
    ){
        if(object == null) {
            return null;
        } else if(object instanceof ManagerFactory_2_0) {
            return (ManagerFactory_2_0)object;
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
    public PersistenceManager createManager(
        Subject subject
    ) throws ResourceException {
        PasswordCredential credential = AbstractManagerFactory.getCredential(subject);
        return this.delegate.getPersistenceManager(
            credential.getUserName(),
            String.valueOf(credential.getPassword())
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.ManagerFactory_2_0#createManager()
     */
    public PersistenceManager createManager(
    ) throws ResourceException {
        return this.delegate.getPersistenceManager();
    }

}
