/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Default Connection Manager
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.resource.spi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ValidatingManagedConnectionFactory;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ValidatingManagedConnectionFactory;
#endif
import javax.security.auth.Subject;

/**
 * A connection manager for a non-managed environment
 */
public class DefaultConnectionManager implements ConnectionManager, ConnectionEventListener {

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = -2509621636815666895L;
    
    private Set<ManagedConnection> idleConnections = new HashSet<>();
    
    private static final Subject USE_THE_MANAGED_CONNECTION_FACTORYS_CREDENTIALS = null;

    @Override
    public synchronized Object allocateConnection(
        ManagedConnectionFactory mcf,
        ConnectionRequestInfo cxRequestInfo
    ) throws ResourceException {
        final ManagedConnection mc = allocateManagedConnection(mcf, cxRequestInfo);
        return mc.getConnection(USE_THE_MANAGED_CONNECTION_FACTORYS_CREDENTIALS, cxRequestInfo);
    }

    private ManagedConnection allocateManagedConnection(
        ManagedConnectionFactory mcf,
        ConnectionRequestInfo cxRequestInfo
    ) throws ResourceException {
        ManagedConnection mc;
        do {
            mc = mcf.matchManagedConnections(idleConnections, USE_THE_MANAGED_CONNECTION_FACTORYS_CREDENTIALS, cxRequestInfo);
            if(mc == null) {
                return mcf.createManagedConnection(USE_THE_MANAGED_CONNECTION_FACTORYS_CREDENTIALS, cxRequestInfo);
            }
            idleConnections.remove(mc);
        } while (isInvalid(mcf, mc));
        return mc;
    }

    private boolean isInvalid(
        ManagedConnectionFactory mcf,
        ManagedConnection mc
    ) throws ResourceException {
        return 
            mc != null &&
            mcf instanceof ValidatingManagedConnectionFactory &&
            !((ValidatingManagedConnectionFactory)mcf).getInvalidConnections(Collections.singleton(mc)).isEmpty();
    }
        
    @Override
    public synchronized void connectionClosed(
        ConnectionEvent event
    ) {
        final ManagedConnection managedConnection = (ManagedConnection) event.getSource();
        this.idleConnections.add(managedConnection);
    }

    @Override
    public void localTransactionStarted(
        ConnectionEvent event
    ) {
        // Ignored by the simple connection manager
    }

    @Override
    public void localTransactionCommitted(
        ConnectionEvent event
    ) {
        // Ignored by the simple connection manager
    }

    @Override
    public void localTransactionRolledback(
        ConnectionEvent event
    ) {
        // Ignored by the simple connection manager
    }

    @Override
    public void connectionErrorOccurred(
        ConnectionEvent event
    ) {
        // Ignored by the simple connection manager
    }

}
