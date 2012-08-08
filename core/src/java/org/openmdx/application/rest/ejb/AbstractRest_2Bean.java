/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractRest_2Bean.java,v 1.1 2009/05/15 00:26:37 hburger Exp $
 * Description: Abstract EJB With Dataprovider 2 Support
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/15 00:26:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
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

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.openmdx.application.Version;
import org.openmdx.application.ejb.spi.SessionBean_1;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.kernel.exception.BasicException;

/**
 * Abstract EJB With Rest Support
 */
public abstract class AbstractRest_2Bean 
    extends SessionBean_1 
{

    /**
     * Constructor 
     */
    protected AbstractRest_2Bean() {
        super();
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 2296343735493563316L;

    /**
     * The connection meta data object
     */
    private ConnectionMetaData metaData;

    
    //------------------------------------------------------------------------
    // Extends SessionBean_1
    // -----------------------------------------------------------------------
  
    /**
     * Retrieve the connection user name
     * 
     * @return the connection user name
     */
    protected String getConnectionUserName(){
        return getSessionContext().getCallerPrincipal().getName();
    }

    /**
     * Activates the dataprovider Java server.
     */
    public void activate(
    ) throws Exception {
      super.activate();
      //
      // Provide Meta Data
      //
      this.metaData = new ConnectionMetaData (
      ){

          public String getEISProductName(
          ) throws ResourceException {
              return "openMDX/REST";
          }

          public String getEISProductVersion(
          ) throws ResourceException {
              return Version.getSpecificationVersion();
          }

          public String getUserName(
          ) throws ResourceException {
              return getConnectionUserName();
          }
          
      };
    }

    
    //------------------------------------------------------------------------
    // Provides RestConnection
    // -----------------------------------------------------------------------
  
    /** 
     * Gets the information on the underlying EIS instance represented
     * through an active connection.
     *
     * @return   ConnectionMetaData instance representing information 
     *           about the EIS instance
     * @throws   ResourceException  
     *                        Failed to get information about the 
     *                        connected EIS instance. Error can be
     *                        resource adapter-internal, EIS-specific
     *                        or communication related.
     */
    public ConnectionMetaData getMetaData(
    ) throws ResourceException {
        return this.metaData;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    public boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException {
        throw ResourceExceptions.initHolder(
            new NotSupportedException(
                "Execute with input and output record is not supported",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.NOT_SUPPORTED 
                )
            )
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
     */
    public Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException {
        throw ResourceExceptions.initHolder(
            new NotSupportedException(
                "Execute with input and return record is not supported",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.NOT_SUPPORTED 
                )
            )
        );
    }
    
}
