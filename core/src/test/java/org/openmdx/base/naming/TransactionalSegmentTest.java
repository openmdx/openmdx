/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Transactional Path Component Test 
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
package org.openmdx.base.naming;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Transactional Path Component Test
 */
public class TransactionalSegmentTest {

	@Test
	public void whenOnlyComponentThenTransactionalObjectId(){
		// Arrange
		final UUID uuid = UUID.fromString("d8731124-db0d-431d-b405-6915c3e415a2");
		final XRISegment testee = new Path(uuid).getSegment(0);
		// Act
		final String classicRepresentation = testee.toClassicRepresentation();
		final String xriRepresentation = testee.toXRIRepresentation();
		// Assert
		Assertions.assertEquals("!($t*uuid*d8731124-db0d-431d-b405-6915c3e415a2)", classicRepresentation, "classicRepresentation");
		Assertions.assertEquals("!($t*uuid*d8731124-db0d-431d-b405-6915c3e415a2)", xriRepresentation, "xriRepresentation");
	}
	
	@Test
	public void whenLaterComponentThenPlaceholder(){
		// Arrange
		final UUID uuid = UUID.fromString("d8731124-db0d-431d-b405-6915c3e415a2");
		final XRISegment testee = XRISegment.valueOf(6, ":" + uuid);
		// Act
		final String classicRepresentation = testee.toClassicRepresentation();
		final String xriRepresentation = testee.toXRIRepresentation();
		// Assert
		Assertions.assertEquals(":d8731124-db0d-431d-b405-6915c3e415a2", classicRepresentation);
		Assertions.assertEquals(":d8731124-db0d-431d-b405-6915c3e415a2", xriRepresentation);
	}

	@Test
	public void whenOnlyComponentThenDiscriminantIsUUID(){
		// Arrange
		final UUID uuid = UUID.fromString("d8731124-db0d-431d-b405-6915c3e415a2");
		final XRISegment testee = new Path(uuid).getSegment(0);
		// Act
		final Object discriminant = testee.discriminant();
		// Assert
		Assertions.assertEquals(uuid, discriminant);
	}
	
	@Test
	public void whenLaterComponentThenDrscriminantIsUUID(){
		// Arrange
		final UUID uuid = UUID.fromString("d8731124-db0d-431d-b405-6915c3e415a2");
		final String classicRepresentation = ":" + uuid;
		final XRISegment testee = XRISegment.valueOf(6, classicRepresentation);
		// Act
		final Object discriminant = testee.discriminant();
		// Assert
		Assertions.assertEquals(uuid, discriminant);
	}

}
