/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Local REST Connection Port
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.application.rest.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.resource.spi.CommException;
import javax.resource.spi.ResourceAllocationException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.spi.AbstractInteraction;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.spi.ConnectionAdapter;
import org.openmdx.kernel.exception.BasicException;

/**
 * Local REST Connection Port
 */
class Connection_2LocalPort implements Port {
    
    /**
     * Constructor 
     * 
     * @param localHome
     */
    private Connection_2LocalPort(
        Connection_2LocalHome localHome
    ){
       this.localHome = localHome;
    }

    /**
     * The EJB Home
     */
    private final Connection_2LocalHome localHome;

    /**
     * Factory method
     * 
     * @param delegate 
     * 
     * @throws ServiceException if the port can't be acquired
     */
    static Port newInstance(
        Object delegate
    ) throws ServiceException {  
        try {
            return new Connection_2LocalPort(
                (Connection_2LocalHome) delegate
            );
        } catch (ClassCastException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Could not acquire the EJB's local home"
            );
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestPlugIn#getInteraction(javax.resource.cci.Connection)
     */
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        try {
            return new InteractionAdapter(
                this.localHome.create(
                    ((ConnectionAdapter)connection).getConnectionSpec()
                ),
                connection
            );
        } catch (CreateException exception) {
            throw ResourceExceptions.initHolder(
                new ResourceAllocationException(
                    "Local EJB connection can't be established",
                    BasicException.newEmbeddedExceptionStack(exception)
                )
            );
        }
    }

    
    //------------------------------------------------------------------------
    // Class InteractionAdapter
    //------------------------------------------------------------------------

    /**
     * Interaction Adapter
     */
    protected static class InteractionAdapter extends AbstractInteraction<Connection> {
        
        /**
         * Constructor 
         *
         * @param connection
         */
        protected InteractionAdapter(
            Connection_2_0Local delegate,
            Connection connection
        ) {
            super(connection);
            this.delegate = delegate;
        }

        /**
         * The delegate
         */
        private final Connection_2_0Local delegate;
        
        /* (non-Javadoc)
         * @see org.openmdx.base.resource.spi.Port#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        @Override
        public Record execute(
            InteractionSpec ispec, 
            Record input
        ) throws ResourceException {
            try{
                return this.delegate.execute(ispec, input);
            } catch (EJBException exception) {
                throw ResourceExceptions.initHolder(
                    new CommException(
                        "Local EJB Execution Failure", 
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }
        
    }

}
