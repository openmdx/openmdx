/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Abstract Connection 
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
package org.openmdx.resource.spi;

/**
 * Abstract Connection
 */
public abstract class AbstractConnection implements AutoCloseable {

    /**
     * Constructor
     */
    protected AbstractConnection() {
        super();
    }

    /**
     * The managed connection associated with this connection handle
     */
    private AbstractManagedConnection<?> managedConnection;

    /**
     * Used by {@code AbstractManagedConnection}
     */
    void associateManagedConnection(
    	AbstractManagedConnection<?> managedConnection
    ){
        if(managedConnection == this.managedConnection){
            return;
        }
    	if(managedConnection != null && this.managedConnection != null){
    		this.managedConnection.dissociateConnection(this, false);
    	}
    	this.managedConnection = managedConnection;
    }

    /**
     * Retrieve the currently associated managed connection
     * 
     * @return the currently associated managed connection
     */
    protected AbstractManagedConnection<?> getManagedConnection(){
    	return this.managedConnection;
    }

    /**
     * Clears the manage connection
     */
    protected void dissociateManagedConnection(
    ){
    	if(this.managedConnection != null){
    		this.managedConnection.dissociateConnection(this, true);
    		this.managedConnection = null;
    	}
    }
    
}
