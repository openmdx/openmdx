/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ConnectionFactoryAdapter.java,v 1.11 2010/12/22 00:14:56 hburger Exp $
 * Description: Connection Factory Adapter 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/12/22 00:14:56 $
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
package org.openmdx.base.rest.spi;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;

import org.openmdx.base.Version;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.transaction.TransactionAttributeType;

/**
 * Connection Factory Adapter
 * <p>
 * This adapter allows a <code>ConnectionFactory</code> view on a
 * <code>Port</code>.
 */
public class ConnectionFactoryAdapter implements ConnectionFactory {

    /**
     * Constructor 
     * 
     * @param port the REST <code>Port</code>
     * @param supportsLocalTransactionDemarcation 
     * @param transactionAttribute
     */
    public ConnectionFactoryAdapter(
        final Port port,
        final boolean supportsLocalTransactionDemarcation,
        final TransactionAttributeType transactionAttribute
    ){
       this.port = port;
       this.transactionAttribute = transactionAttribute;
       this.metaData = new ResourceAdapterMetaData(){

           /** 
            * Gets a tool displayable name of the resource adapter.
            *
            *  @return   String representing the name of the resource adapter
            */
           public String getAdapterName() {
               return "openMDX/REST";
           }

           /** 
            * Gets a tool displayable short desription of the resource
            * adapter.
            *
            * @return   String describing the resource adapter
            */
           public String getAdapterShortDescription() {
               return "openMDX/2 Plug-In Wrapper";
           }

           /** Gets the name of the vendor that has provided the resource 
            *  adapter.
            *
            *  @return   String representing name of the vendor that has 
            *            provided the resource adapter
            */
           public String getAdapterVendorName() {
               return "openMDX";
           }

           /** 
            * Gets the version of the resource adapter.
            *
            * @return   String representing version of the resource adapter
            */
           public String getAdapterVersion() {
               return Version.getSpecificationVersion();
           }

           /** 
            * Returns an array of fully-qualified names of InteractionSpec
            * types supported by the CCI implementation for this resource
            * adapter. Note that the fully-qualified class name is for 
            * the implementation class of an InteractionSpec. This method 
            * may be used by tools vendor to find information on the 
            * supported InteractionSpec types. The method should return 
            * an array of length 0 if the CCI implementation does not 
            * define specific InteractionSpec types.
            *
            * @return   Array of fully-qualified class names of
            *           InteractionSpec classes supported by this
            *           resource adapter's CCI implementation
            * @see      javax.resource.cci.InteractionSpec
            */
           public String[] getInteractionSpecsSupported() {
               return new String[]{RestInteractionSpec.class.getName()};
           }

           /** 
            * Returns a string representation of the version of the 
            * connector architecture specification that is supported by
            * the resource adapter.
            *
            * @return   String representing the supported version of 
            *           the connector architecture
            */
           public String getSpecVersion() {
               return "1.5.";
           }

           /** 
            * Returns true if the implementation class for the Interaction 
            * interface implements public boolean execute(InteractionSpec 
            * ispec, Record input, Record output) method; otherwise the 
            * method returns false.
            *
            * @return   boolean depending on method support
            * @see      javax.resource.cci.Interaction
            */
           public boolean supportsExecuteWithInputAndOutputRecord() {
               return false;
           }

           /** 
            * Returns true if the implementation class for the Interaction
            * interface implements public Record execute(InteractionSpec
            * ispec, Record input) method; otherwise the method returns 
            * false.
            *
            * @return   boolean depending on method support
            * @see      javax.resource.cci.Interaction
            */
           public boolean supportsExecuteWithInputRecordOnly() {
               return true;
           }

           /** 
            * Returns true if the resource adapter implements the LocalTransaction
            * interface and supports local transaction demarcation on the 
            * underlying EIS instance through the LocalTransaction interface.
            *
            * @return  true if resource adapter supports resource manager
            *          local transaction demarcation through LocalTransaction
            *          interface; false otherwise
            * @see     javax.resource.cci.LocalTransaction
            */
           public boolean supportsLocalTransactionDemarcation() {
               return supportsLocalTransactionDemarcation;
           }
           
       };
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 7517838833585266462L;

    /**
     * The underlying REST plug-in
     */
    private final Port port;
    
    /**
     * Implements <code>Referenceable</code>
     */
    private Reference reference;
    
    /**
     * 
     */
    private final TransactionAttributeType transactionAttribute;
    
    /**
     * The resource adapter's meta-data
     */
    private final ResourceAdapterMetaData metaData;
    
    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getConnection()
     */
    public Connection getConnection(
    ) throws ResourceException {
        return getConnection(
            new RestConnectionSpec(
                System.getProperty("user.name"),
                null
            )
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.ConnectionFactory#getConnection(javax.resource.cci.ConnectionSpec)
     */
    public Connection getConnection(
        ConnectionSpec properties
    ) throws ResourceException {
        return ConnectionAdapter.newInstance(
            this, 
            properties, 
            this.transactionAttribute,
            this.port
        );
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
