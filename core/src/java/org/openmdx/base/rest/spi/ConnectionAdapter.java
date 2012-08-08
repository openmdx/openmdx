/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ConnectionAdapter.java,v 1.3 2009/05/20 15:13:42 hburger Exp $
 * Description: REST Connection Adapter
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/20 15:13:42 $
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

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.Record;
import javax.resource.cci.ResourceWarning;
import javax.resource.cci.ResultSetInfo;

import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.kernel.exception.BasicException;

/**
 * Wraps a REST connection into a JCA Connection
 */
public class ConnectionAdapter implements Connection {

    /**
     * Constructor 
     *
     * @param physicalConnection
     */
    private ConnectionAdapter(
        RestConnection physicalConnection
    ) {
        this.physicalConnection = physicalConnection;
    }

    /**
     * The managed connection
     */
    private RestConnection physicalConnection;
    
    /**
     * Wrap a REST connection into a JCA connection 
     *  
     * @param delegate the REST connection 
     * 
     * @return the corresponding JCA connection
     * 
     * @throws ResourceException  
     */
    public static Connection newInstance(
        RestConnection delegate
    ) {
        return new ConnectionAdapter(delegate);
    }

    /**
     * Validate the managed connection's state
     * 
     * @return the delegate
     * 
     * @throws ResourceException
     */
    public RestConnection getDelegate(
    ) throws ResourceException {
        if(this.physicalConnection == null) throw ResourceExceptions.initHolder(
            new javax.resource.spi.IllegalStateException(
                "The connection is closed", 
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE
                )
            )
        );
        return this.physicalConnection;
    }
    
    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#close()
     */
    public void close(
    ) throws ResourceException {
        getDelegate();
        this.physicalConnection = null;
    }
    
    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#createInteraction()
     */
    public Interaction createInteraction(
    ) throws ResourceException {
        return new InteractionAdapter(
            getDelegate()
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getLocalTransaction()
     */
    public LocalTransaction getLocalTransaction(
    ) throws ResourceException {
        throw ResourceExceptions.initHolder(
            new javax.resource.NotSupportedException(
                "Local transactions are not supported", 
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED
                )
            )
        );    
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getMetaData()
     */
    public ConnectionMetaData getMetaData(
    ) throws ResourceException {
        return getDelegate().getMetaData();
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getResultSetInfo()
     */
    public ResultSetInfo getResultSetInfo(
    ) throws ResourceException {
        throw ResourceExceptions.initHolder(
            new javax.resource.NotSupportedException(
                "Result sets are not supported", 
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED
                )
            )
        );    
    }

    
    //--------------------------------------------------------------------
    // Class InteractionAdapter
    //--------------------------------------------------------------------
    
    /**
     * Interaction Adapter
     */
    class InteractionAdapter implements Interaction {

        /**
         * Constructor 
         *
         * @param physicalConnection
         */
        InteractionAdapter(
            RestConnection physicalConnection
        ){
            this.physicalConnection = physicalConnection;
        }
        
        /**
         * The interaction's connection
         */
        private RestConnection physicalConnection;
        
        /**
         * Chain of resource warnings
         */
        private ResourceWarning warnings = null;
        
        /**
         * Validate the interaction's state
         * 
         * @return the delegate
         * 
         * @throws ResourceException
         */
        protected RestConnection getDelegate(
        ) throws ResourceException {
            if(this.physicalConnection == null) throw ResourceExceptions.initHolder(
                new javax.resource.spi.IllegalStateException(
                    "The interaction is closed", 
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE
                    )
                )
            );
            return this.physicalConnection;
        }
                    
        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#clearWarnings()
         */
        public void clearWarnings(
        ) throws ResourceException {
            this.warnings = null;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#close()
         */
        public void close(
        ) throws ResourceException {
            getDelegate();
            this.physicalConnection = null;
            this.warnings = null;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        public Record execute(
            InteractionSpec ispec, 
            Record input
        ) throws ResourceException {
            return getDelegate().execute(ispec, input);
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        public boolean execute(
            InteractionSpec ispec, 
            Record input, 
            Record output
        ) throws ResourceException {
            throw ResourceExceptions.initHolder(
                new javax.resource.NotSupportedException(
                    "Use the connection's \"Record exceute(InteractionSpec,Record)\" method",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED
                    )
                )
            );
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#getConnection()
         */
        public Connection getConnection() {
            return ConnectionAdapter.this;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#getWarnings()
         */
        public ResourceWarning getWarnings(
        ) throws ResourceException {
            return this.warnings;
        }
        
    }

}