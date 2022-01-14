/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Classic Descendant Wildcard Path Component Test
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
 * Classic Descendant Wildcard Path Component Test
 */
public class ClassicWildcardMultiSegmentTest {

	@Test
	public void whenWithoutPrefixThenDiscriminantIsEmpty(){
		// Arrange
		final String classicRepresentation = "%";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		// Act
		final boolean empty = ((String) testee.discriminant()).isEmpty();
		// Assert
		Assertions.assertEquals(classicRepresentation, testee.toClassicRepresentation(), "classicRepresentation");
		Assertions.assertTrue(empty, "emptyPrefix");
	}
	
	@Test
	public void whenWithoutPrefixThenSegmentIsACrossReference(){
		// Arrange
		final String classicRepresentation = "%";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		// Act
		final String xriRepresentation = testee.toXRIRepresentation();
		// Assert
		Assertions.assertEquals("($...)", xriRepresentation);
	}
	
	@Test
	public void whenWithPrefixThenDiscriminantIsPrefix(){
		// Arrange
		final String classicRepresentation = "foo%";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		// Act
		final String prefix = (String) testee.discriminant();
		// Assert
		Assertions.assertEquals(classicRepresentation, testee.toClassicRepresentation(), "classicRepresentation");
		Assertions.assertEquals("foo", prefix, "prefix");
	}
	
	@Test
	public void whenWithPrefixThenXRIRepresentationRepresentsTwoSegments(){
		// Arrange
		final String classicRepresentation = "foo%";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		// Act
		final String xriRepresentation = testee.toXRIRepresentation();
		// Assert
		Assertions.assertEquals("($.*foo)*($..)/($...)", xriRepresentation);
	}
	
	@Test
	public void whenWithoutPrefixThenAcceptAnyValue(){
		// Arrange
		final String classicRepresentation = "%";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		final XRISegment foo = XRISegment.valueOf(3, "foo");
		// Act
		final boolean match = testee.matches(foo);
		// Assert
		Assertions.assertTrue(match, "match");
	}
	
	@Test
	public void whenWithPrefixThenAcceptLongerValue(){
		// Arrange
		final String classicRepresentation = "fo%";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		final XRISegment foo = XRISegment.valueOf(3, "foo");
		// Act
		final boolean match = testee.matches(foo);
		// Assert
		Assertions.assertTrue(match, "match");
	}

	@Test
	public void whenWithPrefixThenAcceptSameValue(){
		// Arrange
		final String classicRepresentation = "foo%";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		final XRISegment foo = XRISegment.valueOf(3, "foo");
		// Act
		final boolean match = testee.matches(foo);
		// Assert
		Assertions.assertTrue(match, "match");
	}

	@Test
	public void whenWithPrefixThenRejectShorterValue(){
		// Arrange
		final String classicRepresentation = "foo%";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		final XRISegment foo = XRISegment.valueOf(3, "fo");
		// Act
		final boolean match = testee.matches(foo);
		// Assert
		Assertions.assertFalse(match, "match");
	}

	@Test
	public void whenWithPrefixThenRejectAnotherValue(){
		// Arrange
		final String classicRepresentation = "foo%";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		final XRISegment foo = XRISegment.valueOf(3, "bar");
		// Act
		final boolean match = testee.matches(foo);
		// Assert
		Assertions.assertFalse(match, "match");
	}
	
	@Test
	public void whenWithoutPrefixThenAcceptAnotherClassicWildcard(){
		// Arrange
		final String classicRepresentation = "%";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		final XRISegment bar = XRISegment.valueOf(3, ":bar*");
		// Act
		final boolean match = testee.matches(bar);
		// Assert
		Assertions.assertTrue(match, "match");
	}
	
	@Test
	public void whenWithoutPrefixThenAcceptItself(){
		// Arrange
		final String classicRepresentation = "%";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		// Act
		final boolean match = testee.matches(testee);
		// Assert
		Assertions.assertTrue(match, "match");
	}
	
	
}
