/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: RADIUS Connection Factory 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.resource.radius.client;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
#endif

import org.openmdx.resource.spi.AbstractConnectionFactory;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusConnection;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;

/**
 * RADIUS Connection Factory
 */
public class ConnectionFactory
	extends AbstractConnectionFactory<RadiusConnection,RadiusException>
{

    /**
     * Constructor
     * 
     * @param managedConnectionFactory a managed RADIUS connection factory
     * @param connectionManager a connection manager
     */
    public ConnectionFactory(
        ManagedConnectionFactory managedConnectionFactory, 
        ConnectionManager connectionManager
    ) {
    	super(
    		managedConnectionFactory,
    		connectionManager
    	);
    }

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 6296420695651960582L;

    
    //------------------------------------------------------------------------
    // Implements ConnectionFactory
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractConnectionFactory#toEISException(javax.resource.ResourceException)
     */
    @Override
    protected RadiusException toEISException(ResourceException exception) {
        return new RadiusException(
            exception,
            "RADIUS client could not be acquired"
        );
    }

}
