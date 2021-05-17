/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract REST Port
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
package org.openmdx.base.rest.spi;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;

import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RestConnection;

/**
 * Abstract REST Port
 */
public abstract class AbstractRestPort implements Port<RestConnection> {

	/**
	 * The delegate
	 */
	private Port<RestConnection> delegate;

	
	/**
     * Constructor 
     */
    protected AbstractRestPort() {
        super();
    }

    /**
     * Retrieves the delegate
	 * 
	 * @return the delegate
	 */
	public Port<RestConnection> getDelegate() {
		return this.delegate;
	}

	/**
     * Sets the delegate port
	 * 
	 * @param delegate the delegate to set
	 */
	public void setDelegate(Port<RestConnection> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Determines whether this port has a delegate
	 * 
	 * @return <code>true</code> if this port has a delegate
	 */
	protected boolean hasDelegate(){
		return this.delegate != null;
	}

	/**
	 * Create a new delegate interaction if this port has a delegate
	 * 
	 * @param connection
	 * @return the delegate interaction ord <code>null</code> if this port has no delegate
	 * @throws ResourceException
	 */
	protected Interaction newDelegateInteraction(
		RestConnection connection
	) throws ResourceException {
		return hasDelegate() ? getDelegate().getInteraction(connection) : null;
	}

    protected boolean isIncomingTrafficEnabled(RestInteractionSpec ispec) {
        final int interactionVerb = ispec.getInteractionVerb();
        return InteractionSpec.SYNC_SEND == interactionVerb || InteractionSpec.SYNC_SEND_RECEIVE == interactionVerb;
    }

    protected boolean isOutgoingTrafficEnabled(RestInteractionSpec ispec) {
        final int interactionVerb = ispec.getInteractionVerb();
        return InteractionSpec.SYNC_RECEIVE == interactionVerb || InteractionSpec.SYNC_SEND_RECEIVE == interactionVerb;
    }
	
}
