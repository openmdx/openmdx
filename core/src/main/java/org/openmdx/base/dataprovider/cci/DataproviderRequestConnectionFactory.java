/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: DataproviderRequestConnectionFactory 
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
package org.openmdx.base.dataprovider.cci;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionSpec;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionSpec;
#endif

import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.spi.AbstractConnectionFactory;

/**
 * DataproviderRequestConnectionFactory
 *
 */
class DataproviderRequestConnectionFactory extends AbstractConnectionFactory {
    

    /**
     * Constructor 
     *
     * @param port the {@code RestConnection} {@code Port}
     * @param defaultConnectionSpec The default connection specification is used by the 
     * {@link #getConnection()} method
     */
    DataproviderRequestConnectionFactory(
        Port<RestConnection> port,
        RestConnectionSpec defaultConnectionSpec
    ) {
        this.port = port;
        this.defaultConnectionSpec = defaultConnectionSpec;
    }

    private final Port<RestConnection> port;
    
    /**
     * The default connection specification is used by the 
     * {@link #getConnection()} method.
     */
    private final RestConnectionSpec defaultConnectionSpec;

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 6682398895485311772L;    

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ConnectionFactory#getConnection()
     */
    @Override
    public DataproviderRequestConnection getConnection()
        throws ResourceException {
        return new DataproviderRequestConnection(this, defaultConnectionSpec, port);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ConnectionFactory#getConnection(javax.resource.cci.ConnectionSpec)
     */
    @Override
    public DataproviderRequestConnection getConnection(ConnectionSpec properties)
        throws ResourceException {
        return new DataproviderRequestConnection(this, (RestConnectionSpec) properties, port);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractConnectionFactory#isLocalTransactionDemarcationSupported()
     */
    @Override
    protected boolean isLocalTransactionDemarcationSupported() {
        return false;
    }
    
    
}
