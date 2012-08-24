/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: RADIUS Connection
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.resource.radius.client;

import java.io.IOException;

import org.openmdx.resource.spi.AbstractConnection;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusConnection;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusPacket;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;


/**
 * RADIUS Connection
 */
public class Connection 
	extends AbstractConnection 
	implements RadiusConnection 
{

	/**
	 * Retrieve the physical connection
	 * 
	 * @return the physical connection
	 * 
	 * @throws RadiusException
	 */
	private RadiusConnection getDelegate(
	) throws RadiusException {
    	ManagedConnection managedConnection = (ManagedConnection) super.getManagedConnection();
    	if(managedConnection == null) throw new RadiusException(
    		"The RADIUS connection is already closed"
    	);
    	return managedConnection.getRadiusClient();
    }

	/* (non-Javadoc)
	 * @see org.openmdx.resource.radius.cci.Connection#account(org.openmdx.uses.net.sourceforge.jradiusclient.RadiusPacket)
	 */
	@Override
	public RadiusPacket account(
		RadiusPacket requestPacket
	) throws InvalidParameterException, RadiusException {
		return this.getDelegate().account(requestPacket);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.resource.radius.cci.Connection#authenticate(org.openmdx.uses.net.sourceforge.jradiusclient.RadiusPacket)
	 */
	@Override
	public RadiusPacket authenticate(
		RadiusPacket accessRequest
	) throws RadiusException, InvalidParameterException {
		return this.getDelegate().authenticate(accessRequest);
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		super.dissociateManagedConnection();
	}

}
