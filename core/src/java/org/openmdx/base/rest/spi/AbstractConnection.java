/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: AbstractConnection.java,v 1.8 2010/08/09 13:14:53 hburger Exp $
 * Description: Abstract Connection
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/09 13:14:53 $
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

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.ResultSetInfo;
import javax.resource.spi.IllegalStateException;

import org.openmdx.base.Version;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.kernel.exception.BasicException;

/**
 * Abstract Connection
 */
public abstract class AbstractConnection implements Connection {

    /**
     * Constructor 
     * 
     * @param connectionSpec 
     */
    protected AbstractConnection(
        final RestConnectionSpec connectionSpec
    ){
        this.closed = false;
        this.connectionSpec = connectionSpec;
        this.metaData = new ConnectionMetaData(){
            /**
             * It's an openMDX connection
             */
            public String getEISProductName(
            ) throws ResourceException {
                return "openMDX/REST";
            }

            /**
             * with the given openMDX version
             */
            public String getEISProductVersion(
            ) throws ResourceException {
                return Version.getSpecificationVersion();
            }

            /**
             * Use the stringified principal chain
             */
            public String getUserName(
            ) throws ResourceException {
                return connectionSpec == null ? null : connectionSpec.getUserName();
            }
            
        };
    }

    /**
     * The inbound connection's meta data
     */
    private final RestConnectionSpec connectionSpec;
    
    /**
     * The inbound connection's meta data
     */
    private final ConnectionMetaData metaData;
    
    /**
     * Tells whether the 
     */
    private boolean closed;
    
    /**
     * Retrieve connectionSpec.
     *
     * @return Returns the connectionSpec.
     */
    public final RestConnectionSpec getConnectionSpec(
    ) {
        return this.connectionSpec;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getMetaData()
     */
//  @Override
    public final ConnectionMetaData getMetaData(
    ) throws ResourceException {
        return this.metaData;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getResultSetInfo()
     */
//  @Override
    public final ResultSetInfo getResultSetInfo(
    ) throws ResourceException {
        throw ResourceExceptions.initHolder( 
            new NotSupportedException(
                "Result sets are not supported by REST connections",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED
                )
            )
        );
    }

    /**
     * Test whether the connection is open
     * 
     * @exception IllegalStateException if the connection is closed
     */
    protected void assertOpen(
    ) throws ResourceException {
        if(this.closed) {
            throw ResourceExceptions.initHolder( 
                new IllegalStateException(
                    "This REST connection is closed",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE
                    )
                )
            );
        }
    }
    
    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#close()
     */
//  @Override
    public void close(
    ) throws ResourceException {
        this.assertOpen();
        this.closed = true;
    }

}

