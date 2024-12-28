/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: DataproviderRequestConnection 
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
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.spi.UnavailableException;
#else
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.LocalTransaction;
import jakarta.resource.spi.UnavailableException;
#endif

import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.spi.AbstractConnection;
import org.openmdx.kernel.exception.BasicException;

/**
 * The REST connection
 */
class DataproviderRequestConnection extends AbstractConnection {

	/**
	 * Constructor
	 */
	DataproviderRequestConnection(
	    ConnectionFactory connectionFactory,
		RestConnectionSpec connectionSpec,
		Port<RestConnection> port
	) throws UnavailableException {
		super(connectionFactory, connectionSpec);
		if(port == null) {
			throw ResourceExceptions.initHolder(
				new UnavailableException(
					"Port must not be null",
					BasicException.newEmbeddedExceptionStack(
						BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.BAD_PARAMETER
					)
				)
			);
		}
		this.port = port;
	}

	private final Port<RestConnection> port;
	
	@Override
	public Interaction createInteraction() throws ResourceException {
		return port.getInteraction(this);
	}

	@Override
	public LocalTransaction getLocalTransaction() throws ResourceException {
		throw new NotSupportedException();
	}

}