/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: No Operation Test Plug-In
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
package org.openmdx.base.dataprovider.layer.test;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.Record;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.Record;
#endif

import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RequestRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.kernel.log.SysLog;

/**
 * A JavaBean plug-in
 */
public class NoOpPlugIn implements Port<RestConnection> {

	private Port<RestConnection> delegate;
	
	/**
	 * @return the delegate
	 */
	public Port<RestConnection> getDelegate() {
		return this.delegate;
	}

	/**
	 * @param delegate the delegate to set
	 */
	public void setDelegate(Port<RestConnection> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Interaction getInteraction(
		final RestConnection connection
	) throws ResourceException {
		
		final Interaction delegate = getDelegate().getInteraction(connection);
		
		return new AbstractRestInteraction(
			connection
		){

			/* (non-Javadoc)
			 * @see org.openmdx.base.rest.spi.AbstractRestInteraction#pass(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.RequestRecord, javax.resource.cci.Record)
			 */
			@Override
			protected boolean pass(
				RestInteractionSpec ispec,
				RequestRecord input, 
				Record output
			) throws ResourceException {
				SysLog.info("On-op logs only");
				return delegate.execute(ispec, input, output);
			}
			
		};
	}
	
}
