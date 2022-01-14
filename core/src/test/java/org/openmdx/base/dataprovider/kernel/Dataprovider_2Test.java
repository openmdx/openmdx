/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Embedded Data Provider
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
package org.openmdx.base.dataprovider.kernel;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.Record;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;


public class Dataprovider_2Test {

	@Test
	public void whenDataproviderIsInvokedThenGreetingsAreReturned() throws ResourceException{
		// Arrange
		Dataprovider_2 testee = new Dataprovider_2();
		testee.setConfiguration("xri://+resource/META-INF/helloWorld1Dataprovider.properties");
		final MessageRecord request = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(true).INVOKE;
		// Act
		final Interaction interaction = testee.getInteraction(null);
		final Record reply = interaction.execute(interactionSpec, request);
		// Assert
		Assertions.assertTrue(reply instanceof MessageRecord);
		MessageRecord message = (MessageRecord) reply;
		final Object greetings = message.getBody().get("greetings");
		Assertions.assertEquals("Hello world!", greetings);
	}
	
}
