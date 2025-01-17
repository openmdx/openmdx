/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Hello World Test Plug-In
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
import javax.resource.cci.MappedRecord;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.MappedRecord;
#endif

import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.AbstractRestInteraction;

/**
 * A JavaBean plug-in
 */
public class HelloWorldPlugIn implements Port<RestConnection> {

	private String greetings;
	
	/**
	 * @return the greetings
	 */
	public String getGreetings() {
		return this.greetings;
	}

	/**
	 * @param greetings the greetings to set
	 */
	public void setGreetings(String greetings) {
		this.greetings = greetings;
	}

	@Override
	public Interaction getInteraction(
		final RestConnection connection
	) throws ResourceException {
		return new AbstractRestInteraction(
			connection
		){

			/* (non-Javadoc)
			 * @see org.openmdx.base.rest.spi.AbstractRestInteraction#invoke(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.MessageRecord, org.openmdx.base.rest.cci.MessageRecord)
			 */
			@SuppressWarnings("unchecked")
			@Override
			public boolean invoke(
				RestInteractionSpec ispec,
				MessageRecord input, 
				MessageRecord output
			) throws ResourceException {
				final MappedRecord body = Records.getRecordFactory().createMappedRecord("test:Reply");
				body.put("greetings", getGreetings());
				output.setBody(body);
				return true;
			}
			
		};
	}
	
}
