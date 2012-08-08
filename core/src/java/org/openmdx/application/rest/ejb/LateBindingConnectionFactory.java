/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: LateBindingConnectionFactory.java,v 1.3 2009/06/08 17:10:49 hburger Exp $
 * Description: Late Binding REST Connection Factory 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/08 17:10:49 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.application.rest.ejb;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.UnavailableException;

import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.kernel.exception.BasicException;

/**
 * Late Binding REST Connection Factory
 */
public class LateBindingConnectionFactory implements ConnectionFactory {
    
    /**
     * Constructor 
     *
     * @param jndiName
     */
    private LateBindingConnectionFactory(
        String jndiName
    ){
        this.jndiName = jndiName;
    }

    /**
     * The connection factories JNDI name
     */
    private final String jndiName;
    
    /**
     * Implements <code>Referenceable</code>
     */
    private Reference reference = null;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -5139903145813477747L;

    /**
     * The delegate connection factory
     */
    private transient ConnectionFactory delegate = null;

    /**
     * The late binding REST connection factory builder method
     * 
     * @param jndiName
     * 
     * @return a new late binding connection factory
     */
    public static ConnectionFactory newInstance(
        String jndiName
    ){
        return new LateBindingConnectionFactory(jndiName);
    }

    /**
     * Return the connection factory to delegate to
     * 
     * @param delegate the delegate object
     * 
     * @return the delegate
     * 
     * @throws ResourceException  
     */
    protected ConnectionFactory getDelegate(
        Object delegate
    ) throws ResourceException {
        return 
            delegate instanceof ConnectionFactory ? (ConnectionFactory) delegate :
            Connection_2Factory.newInstance(delegate);
    }
    
    /**
     * Return the connection factory to delegate to
     * 
     * @return the delegate
     * 
     * @throws ResourceException  
     */
    protected ConnectionFactory getDelegate(
    ) throws ResourceException {
        if(this.delegate == null) try {
            this.delegate = getDelegate(
                new InitialContext().lookup(this.jndiName)
            );
        } catch (NamingException exception) {
            throw ResourceExceptions.initHolder(
                new UnavailableException(
                    "Connection factory lookup failure",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ACTIVATION_FAILURE,
                        new BasicException.Parameter("name",this.jndiName)
                    )
                )
            );
        }
        return this.delegate;
    }
    
    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getConnection()
     */
    public Connection getConnection(
    ) throws ResourceException {
        return getDelegate().getConnection();
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getConnection(javax.resource.cci.ConnectionSpec)
     */
    public Connection getConnection(
        ConnectionSpec properties
    ) throws ResourceException {
        return getDelegate().getConnection(properties);
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getMetaData()
     */
    public ResourceAdapterMetaData getMetaData(
    ) throws ResourceException {
        return getDelegate().getMetaData();
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getRecordFactory()
     */
    public RecordFactory getRecordFactory(
    ) throws ResourceException {
        return getDelegate().getRecordFactory();
    }

    /* (non-Javadoc)
     * @see javax.resource.Referenceable#setReference(javax.naming.Reference)
     */
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    /* (non-Javadoc)
     * @see javax.naming.Referenceable#getReference()
     */
    public Reference getReference(
    ) throws NamingException {
        return this.reference;
    }

}
