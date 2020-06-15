/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Access Control Port
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2020, OMEX AG, Switzerland
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
package test.openmdx.app1.layer.application;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.AbstractRestPort;

/**
 * Access Control Port
 */
public abstract class AccessControl_2 extends AbstractRestPort {

    private ConnectionFactory connectionFactory;

    /**
     * @param connectionFactory the connectionFactory to set
     * @deprecated
     */
    public void setConnectionFactory(
        ConnectionFactory connectionFactory
    ) {
        this.connectionFactory = connectionFactory;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(
        RestConnection connection
        ) throws ResourceException {
        return new RestInteraction(connection, newDelegateInteraction(connection));
    }
    
    /**
     * RestInteraction
     *
     */
    protected class RestInteraction extends AbstractRestInteraction {
        
        protected RestInteraction(
            RestConnection connection,
            Interaction delegate
        ) throws ResourceException {
            super(connection,  delegate);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        @Override
        public Record execute(
            InteractionSpec ispec,
            Record input
        ) throws ResourceException {
            assertConnectionFactory();
            return super.execute(ispec, input);
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        @Override
        public boolean execute(
            InteractionSpec ispec,
            Record input,
            Record output
        ) throws ResourceException {
            assertConnectionFactory();
            return super.execute(ispec, input, output);
        }

        private void assertConnectionFactory(
        ) throws ResourceException {
            final ConnectionFactory actualConnectionFactory = getConnection().getConnectionFactory();
            final ConnectionFactory expectedConnectionFactory = connectionFactory;
            final boolean ok = actualConnectionFactory == expectedConnectionFactory;
            if(!ok) {
//                throw new ResourceException("Connection Factory Mismatch");
            }
        }

    }

}
