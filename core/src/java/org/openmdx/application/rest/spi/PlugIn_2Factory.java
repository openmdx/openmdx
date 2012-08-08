/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: PlugIn_2Factory.java,v 1.4 2009/05/26 16:45:48 hburger Exp $
 * Description: REST Plug-In Factory
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/26 16:45:48 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.application.rest.spi;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;

import org.openmdx.base.beans.Factory;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.ConnectionAdapter;
import org.openmdx.base.rest.spi.RestConnection;
import org.openmdx.base.rest.spi.RestPlugIn;
import org.openmdx.kernel.Version;
import org.openmdx.kernel.exception.BasicException;

/**
 * REST Plug-In Factory
 */
public class PlugIn_2Factory implements ConnectionFactory {

    /**
     * Constructor 
     *
     * @param plugInFactory
     * @param nextConnectionFactory
     * @param supportsExecuteWithInputAndOutputRecord
     * @param supportsExecuteWithInputRecordOnly
     */
    protected PlugIn_2Factory(
        Factory<RestConnection> plugInFactory,
        ConnectionFactory nextConnectionFactory,
        final boolean supportsExecuteWithInputAndOutputRecord,
        final boolean supportsExecuteWithInputRecordOnly
    ){
       this.plugInFactory = plugInFactory;
       this.nextConnectionFactory = nextConnectionFactory;
       this.metaData = new ResourceAdapterMetaData(){

           public String getAdapterName() {
               return "openMDX/REST plug-in";
           }

           public String getAdapterShortDescription() {
               return "openMDX/2 REST Plug-In Factory";
           }
           public String getAdapterVendorName() {
               return "OMEX AG";
           }

           public String getAdapterVersion() {
               return Version.getSpecificationVersion();
           }

           public String[] getInteractionSpecsSupported() {
               return new String[]{RestInteractionSpec.class.getName()};
           }

           /**
            * Retrieve the JCA specification version
            * 
            * @return the JCA specification version
            */
           public String getSpecVersion() {
               return "1.5.";
           }

           public boolean supportsExecuteWithInputAndOutputRecord() {
               return supportsExecuteWithInputRecordOnly;
           }

           public boolean supportsExecuteWithInputRecordOnly() {
               return supportsExecuteWithInputAndOutputRecord;
           }

           public boolean supportsLocalTransactionDemarcation() {
               return false;
           }
           
       };

    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 2007152371421358892L;

    /**
     * The Java Bean Factory
     */
    private final Factory<RestConnection> plugInFactory;
    
    /**
     * The resource adapter's metadata
     */
    private final ResourceAdapterMetaData metaData; 
    
    /**
     * Factory for the plug-in's next connection
     */
    private final ConnectionFactory nextConnectionFactory;

    /**
     * Create a plug-in factory
     * 
     * @param plugInFactory
     * @param nextConnectionFactory
     * @param supportsExecuteWithInputAndOutputRecord
     * @param supportsExecuteWithInputRecordOnly
     * @return
     * @throws ResourceException
     */
    public static ConnectionFactory newInstance(
        Factory<RestConnection> plugInFactory,
        ConnectionFactory nextConnectionFactory,
        final boolean supportsExecuteWithInputAndOutputRecord,
        final boolean supportsExecuteWithInputRecordOnly
    ) throws ResourceException {
        return new PlugIn_2Factory(
            plugInFactory,
            nextConnectionFactory,
            supportsExecuteWithInputAndOutputRecord,
            supportsExecuteWithInputRecordOnly
        );
    }

    /**
     * Create a plug-in factory
     * 
     * @param plugInFactory
     * @param nextConnectionFactory
     * @return
     * @throws ResourceException
     */
    public static ConnectionFactory newInstance(
        Factory<RestConnection> plugInFactory,
        ConnectionFactory nextConnectionFactory
    ) throws ResourceException {
        return new PlugIn_2Factory(
            plugInFactory,
            nextConnectionFactory,
            true, // supportsExecuteWithInputAndOutputRecord
            true // supportsExecuteWithInputRecordOnly
        );
    }
    
    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getConnection()
     */
    public Connection getConnection(
    ) throws ResourceException {
        try {
            RestConnection plugIn = this.plugInFactory.instantiate();
            if(this.nextConnectionFactory != null && plugIn instanceof RestPlugIn) {
                ((RestPlugIn)plugIn).setNext(this.nextConnectionFactory.getConnection());
            }
            return ConnectionAdapter.newInstance(plugIn);
        } catch (ServiceException exception) {
            throw BasicException.initHolder(
                new ResourceException(
                    "Plug-in creation failure",
                    BasicException.newEmbeddedExceptionStack(exception)
                )
           );
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getConnection(javax.resource.cci.ConnectionSpec)
     */
    public Connection getConnection(
        ConnectionSpec properties
    ) throws ResourceException {
        try {
            RestConnection plugIn = this.plugInFactory.instantiate();
            if(this.nextConnectionFactory != null && plugIn instanceof RestPlugIn) {
                ((RestPlugIn)plugIn).setNext(this.nextConnectionFactory.getConnection(properties));
            }
            return ConnectionAdapter.newInstance(plugIn);
        } catch (ServiceException exception) {
            throw BasicException.initHolder(
                new ResourceException(
                    "Plug-in creation failure",
                    BasicException.newEmbeddedExceptionStack(exception)
                )
           );
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getMetaData()
     */
    public ResourceAdapterMetaData getMetaData(
    ) throws ResourceException {
        return this.metaData;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getRecordFactory()
     */
    public RecordFactory getRecordFactory(
    ) throws ResourceException {
        return Records.getRecordFactory();
    }

    /* (non-Javadoc)
     * @see javax.resource.Referenceable#setReference(javax.naming.Reference)
     */
    public void setReference(Reference reference) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.naming.Referenceable#getReference()
     */
    public Reference getReference(
    ) throws NamingException {
        throw new UnsupportedOperationException();
    }
    
}