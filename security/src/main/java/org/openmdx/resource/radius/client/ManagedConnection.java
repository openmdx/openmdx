/*
 * ====================================================================
 * Project:     opeMDX/Security, http://www.openmdx.org/
 * Description: Managed RADIUS Connection 
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

import java.io.IOException;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.security.PasswordCredential;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.security.PasswordCredential;
#endif
import javax.security.auth.Subject;

import org.openmdx.resource.spi.AbstractManagedConnection;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusConnection;

/**
 * Managed RADIUS Connection
 */
public class ManagedConnection extends AbstractManagedConnection<ManagedConnectionFactory> {

    /**
     * Constructor 
     */
    ManagedConnection(
        ManagedConnectionFactory factory,
        PasswordCredential credential, 
        RadiusConnection radiusClient
    ) {
    	super(factory,"RADIUS","1.0", credential, null);
        this.radiusClient = radiusClient;
    }

    /**
     * The physical connection
     */
    private RadiusConnection radiusClient;

    /**
     * Retrieve the physical connection
     * 
     * @return the the physical connection
     */
    RadiusConnection getRadiusClient() {
    	return this.radiusClient;
    }

    @Override
    public void destroy(
    ) throws ResourceException {
        try {
            this.radiusClient.close();
        } catch (IOException exception) {
            throw new ResourceException(
                "Could not close the RADIUS client's sockets",
                exception
            );
        }
        super.destroy();
    }

    /* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractManagedConnection#newConnection()
     */
    @Override
    protected Object newConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) {
    	return new Connection();
    }

    @Override
    protected boolean matches(
        Object credential,
        ConnectionRequestInfo connectionRequestInfo
    ) {
        return credentialsMatch(credential);
    }

}
