/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Lightweight Connection Factory
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.lightweight.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import javax.sql.ConnectionEventListener;

import org.openmdx.uses.org.apache.commons.pool2.PooledObject;
import org.openmdx.uses.org.apache.commons.pool2.PooledObjectFactory;
import org.openmdx.uses.org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Lightweight Connection Factory
 */
class PooledConnectionFactory implements PooledObjectFactory<ValidatablePooledConnection> {

	/**
	 * Constructor
	 */
	PooledConnectionFactory(
		String url, 
		Properties properties, 
		boolean autoCommit
	) {
		this.url = url;
		this.properties = properties;
		this.autoCommit = autoCommit;
	}

	/**
	 * A database url of the form <code>jdbc:subprotocol:subname</code>
	 */
	protected final String url;
	
	/**
     * A collection of arbitrary string tag/value pairs as connection arguments
	 */
    protected final Properties properties;
	
    /**
     * 
     */
    protected final boolean autoCommit;
    
    /**
     * The connection event listener is registered with the pooled connections
     */
    private ConnectionEventListener connectionEventListener;
    
    /**
     * Set the connection event listener
     */
    void setConnectionEventListener(ConnectionEventListener listener) {
    	this.connectionEventListener = listener;
    }
    
	@Override
	public PooledObject<ValidatablePooledConnection> makeObject() throws Exception {
        final Connection managedConnection = DriverManager.getConnection(this.url, properties);
        managedConnection.setAutoCommit(autoCommit);
        final ValidatablePooledConnection pooledConnection = autoCommit ? 
        		new LightweightACConnection(managedConnection) :
        		new LightweightXAConnection(managedConnection);
        pooledConnection.addConnectionEventListener(connectionEventListener);		
		return new DefaultPooledObject<ValidatablePooledConnection>(pooledConnection);
	}

	@Override
	public void destroyObject(PooledObject<ValidatablePooledConnection> p) throws Exception {
		p.getObject().close();
	}

	@Override
	public boolean validateObject(PooledObject<ValidatablePooledConnection> p) {
		return p.getObject().isValid();
	}

	@Override
	public void activateObject(PooledObject<ValidatablePooledConnection> p) throws Exception {
		p.getObject().activate();
	}

	@Override
	public void passivateObject(PooledObject<ValidatablePooledConnection> p) throws Exception {
		p.getObject().passivate();
	}

}
