/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Connection Factory Adapter 
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
package org.openmdx.base.rest.spi;

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
import org.openmdx.base.transaction.TransactionAttributeType;

/**
 * Connection Factory Adapter
 * <p>
 * This adapter allows a {@code ConnectionFactory} view on a
 * {@code Port}.
 */
public class RestConnectionFactory extends AbstractConnectionFactory {

	/**
     * Constructor 
     * 
     * @param port the REST {@code Port}
     * @param supportsLocalTransactionDemarcation 
     * @param transactionAttribute
     */
    public RestConnectionFactory(
        final Port<RestConnection> port,
        final boolean supportsLocalTransactionDemarcation,
        final TransactionAttributeType transactionAttribute
    ){
       this.port = port;
       this.transactionAttribute = transactionAttribute;
       this.supportsLocalTransactionDemarcation = supportsLocalTransactionDemarcation;
    }
    
    /**
     * Implements {@code Serializable}
     */
	private static final long serialVersionUID = -6265231975243515265L;

    /**
     * The underlying REST plug-in
     */
    private final Port<RestConnection> port;
    
    private final TransactionAttributeType transactionAttribute;
    
    private final boolean supportsLocalTransactionDemarcation;
    
    TransactionAttributeType getTransactionAttribute() {
		return this.transactionAttribute;
	}

	Port<RestConnection> getPort() {
		return this.port;
	}

	/* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ConnectionFactory#getConnection()
     */
    public RestConnection getConnection(
    ) throws ResourceException {
        return getConnection(
            new RestConnectionSpec(
                System.getProperty("user.name"),
                null
            )
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ConnectionFactory#getConnection(javax.resource.cci.ConnectionSpec)
     */
    public RestConnection getConnection(
        ConnectionSpec properties
    ) throws ResourceException {
        return ConnectionAdapter.newInstance(
    		this,
            (RestConnectionSpec) properties
        );
    }

	@Override
	protected boolean isLocalTransactionDemarcationSupported() {
		return this.supportsLocalTransactionDemarcation;
	}

	
}
