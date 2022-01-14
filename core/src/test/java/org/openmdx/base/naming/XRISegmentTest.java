/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Path Component Test 
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Testee is the PathComponent, tested is its static valueOf method.
 */
public class XRISegmentTest {

	@Test
	public void whenColonAndAsteriskThenCreateClassicSegmentWildcardPathComponent(){
		// Arrange
		final String classicRepresentation = ":*";
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(4, classicRepresentation);
		// Assert
		validate(classicRepresentation, pathComponent, ClassicWildcardSegment.class);
	}

	@Test
	public void whenMultiColonThenCreatePlainVanillaSegmentPathComponent(){
		// Arrange
		final String classicRepresentation = "A:B:C";
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(4, classicRepresentation);
		// Assert
		validate(classicRepresentation, pathComponent, PlainVanillaSegment.class);
	}

	@Test
	public void whenNoColonThenCreatePlainVanillaSegmentPathComponent(){
		// Arrange
		final String classicRepresentation = "AAA";
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(4, classicRepresentation);
		// Assert
		validate(classicRepresentation, pathComponent, PlainVanillaSegment.class);
	}
	
	@Test
	public void whenStartsWithColonAndEndsWithAsteriskThenCreateClassicSegmentWildcardPathComponent(){
		// Arrange
		final String classicRepresentation = ":Test*";
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(4, classicRepresentation);
		// Assert
		validate(classicRepresentation, pathComponent, ClassicWildcardSegment.class);
	}

	@Test
	public void whenPercentThenCreateClassicWildcardPathComponent(){
		// Arrange
		final String classicRepresentation = "%";
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(4, classicRepresentation);
		// Assert
		validate(classicRepresentation, pathComponent, ClassicWildcardMultiSegment.class);
	}
	
	@Test
	public void whenStartsWithPercentThenCreateClassicWildcardPathComponent(){
		// Arrange
		final String classicRepresentation = "Test%";
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(4, classicRepresentation);
		// Assert
		validate(classicRepresentation, pathComponent, ClassicWildcardMultiSegment.class);
	}
	
	@Test
	public void whenUUIDCrossReferenceThenCreateTransactionalPathComponent(){
		// Arrange
		final String classicRepresentation = "!($t*uuid*d8731124-db0d-431d-b405-6915c3e415a2)";
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(0, classicRepresentation);
		// Assert
		Assertions.assertTrue(pathComponent instanceof TransactionalSegment);
		Assertions.assertEquals(pathComponent,  XRISegment.valueOf(0, classicRepresentation));
	}

	@Test
	public void whenUUIDPlaceholderThenCreateTransactionalPathComponent(){
		// Arrange
		final String classicRepresentation = ":d8731124-db0d-431d-b405-6915c3e415a2";
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(6, classicRepresentation);
		// Assert
		Assertions.assertTrue(pathComponent instanceof TransactionalSegment);
		Assertions.assertEquals(pathComponent, toSegment(classicRepresentation));
	}
	
	@Test
	public void whenClassicPlaceholderThenCreateTransactionalPathComponent(){
		// Arrange
		final String classicRepresentation = TransactionalSegment.getClassicRepresentationOfNewInstance();
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(6, classicRepresentation);
		// Assert
		Assertions.assertTrue(pathComponent instanceof TransactionalSegment);
		Assertions.assertEquals(pathComponent, toSegment(classicRepresentation));
	}
	

	@Test
	public void whenAnotherCrossReferenceThenCreateExtensibleCrossReferencePathComponent(){
		// Arrange
		final String classicRepresentation = "(java:comp/env)";
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(4, classicRepresentation);
		// Assert
		Assertions.assertTrue(pathComponent instanceof GeneralSegment);
		Assertions.assertEquals(pathComponent, toSegment(classicRepresentation));
	}

	@Test
	public void whenWildcardCrossReferenceThenCreateExtensibleCrossReferencePathComponent(){
		// Arrange
		final String classicRepresentation = "Test*($.)";
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(4, classicRepresentation);
		// Assert
		Assertions.assertTrue(pathComponent instanceof GeneralSegment);
		Assertions.assertEquals(pathComponent, toSegment(classicRepresentation));
	}

	@Test
	public void whenClassicCrossReferenceThenCreateClassicCrossReferencePathComponent(){
		// Arrange
		final String classicRepresentation = "org::openmdx::audit2/provider/Audit";
		// Act
		final XRISegment pathComponent = XRISegment.valueOf(4, classicRepresentation);
		// Assert
		Assertions.assertTrue(pathComponent instanceof ClassicCrossReferenceSegment);
		Assertions.assertEquals(pathComponent, toSegment(classicRepresentation));
	}

	private static XRISegment toSegment(
		String xriSegment
	){
		String embeddedSegment = xriSegment.replaceAll(":","::").replaceAll("/","//"); 
		return new Path("test:openmdx:org/provider/test/segment/" + embeddedSegment + "/extent").getSegment(4);
	}

	private static void validate(
		String classicRepresentation, 
		XRISegment xriSegment, 
		Class<? extends XRISegment> subClass
	) {
		Assertions.assertTrue(subClass == xriSegment.getClass(), classicRepresentation);
		Assertions.assertEquals(xriSegment, toSegment(classicRepresentation), classicRepresentation);
		Assertions.assertEquals(classicRepresentation, xriSegment.toClassicRepresentation(), classicRepresentation);
	}

}
