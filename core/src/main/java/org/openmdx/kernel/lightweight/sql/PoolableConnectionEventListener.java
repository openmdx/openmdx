/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Lightweight Connection Event Listener
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2016, OMEX AG, Switzerland
 * All rights reserved.
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
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,Connectionbjt
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
 * This product includes or is based on software developed by other
 * organization as listed in the NOTICE file.
 */
package org.openmdx.kernel.lightweight.sql;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

import org.openmdx.kernel.log.SysLog;
import org.openmdx.uses.org.apache.commons.pool2.ObjectPool;

/**
 * XA Connection Event Listener
 */
class PoolableConnectionEventListener implements ConnectionEventListener {

	/**
	 * Constructor
	 */
	PoolableConnectionEventListener(
		ObjectPool<ValidatablePooledConnection> pool
	) {
		this.pool = pool;
	}

	private final ObjectPool<ValidatablePooledConnection> pool;
	
	@Override
	public void connectionClosed(ConnectionEvent event) {
		try {
			pool.returnObject(getConnection(event));
		} catch (Exception exception) {
			SysLog.warning("Unable to return the managed connection to the pool",  exception);
		}
	}

	@Override
	public void connectionErrorOccurred(ConnectionEvent event) {
		try {
			pool.invalidateObject(getConnection(event));
		} catch (Exception exception) {
			SysLog.warning("Unable to invalidate the managed connection to the pool",  exception);
		}
	}

	private ValidatablePooledConnection getConnection(ConnectionEvent event) {
		return (ValidatablePooledConnection)event.getSource();
	}

}
