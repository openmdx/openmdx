/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Object Record Test
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
package org.openmdx.base.rest.spi;

import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.Freezable;

/**
 * Object Record Test
 */
public class ObjectRecordTest {

	@Test
	public void whenMutableThenPathMayBeReplaced() throws ResourceException{
		// Arrange
		final Path oldPath = new Path("xri://@openmdx*org.openmdx.base");
		final Path newPath = new Path("xri://@openmdx*org.openmdx.state2");
		final ObjectRecord testee = (ObjectRecord) Records.getRecordFactory().createMappedRecord(ObjectRecord.NAME);
		testee.setResourceIdentifier(oldPath);
		// Act	
		testee.setResourceIdentifier(newPath);
		// Assert
		Assertions.assertFalse(testee.isImmutable());
		Assertions.assertSame(newPath, testee.getResourceIdentifier());
	}
	
	@Test
	public void whenImmutableThenPathMustNotBeReplaced() throws ResourceException{
		// Arrange
		final Path oldPath = new Path("xri://@openmdx*org.openmdx.base");
		final Path newPath = new Path("xri://@openmdx*org.openmdx.state2");
		final ObjectRecord testee = (ObjectRecord) Records.getRecordFactory().createMappedRecord(ObjectRecord.NAME);
		testee.setResourceIdentifier(oldPath);
		((Freezable)testee).makeImmutable();
		// Act/Assert
		try {
			testee.setResourceIdentifier(newPath);
			Assertions.fail("IllegalStateException expected");
		} catch (IllegalStateException expected) {
			Assertions.assertTrue(((Freezable)testee).isImmutable());
		}
	}
	
	@Test
	public void whenObjectRecordInterfaceIsRequestedThenObjectRecordImplementationInstanceIsCreated() throws ResourceException{
		// Arrange
		Class<org.openmdx.base.rest.cci.ObjectRecord> recordInterface = org.openmdx.base.rest.cci.ObjectRecord.class;
		// Act
		final org.openmdx.base.rest.cci.ObjectRecord testee = Records.getRecordFactory().createMappedRecord(recordInterface);
		// Assert
		Assertions.assertTrue(testee instanceof ObjectRecord);
	}

}
