/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Extensible Cross Reference Path Component Test
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
package org.openmdx.base.naming;

import org.junit.Assert;
import org.junit.Test;
import org.openmdx.kernel.id.UUIDs;

/**
 * Extensible Cross Reference Path Component Test
 */
public class GeneralSegmentTest {

	@Test
	public void whenForeignURIThenPathComponentIsNotAPattern(){
		// Arrange
		final String classicRepresentation = "(java:comp/env)";
		final XRISegment testee = XRISegment.valueOf(4, classicRepresentation);
		// Act
		final boolean pattern = testee.isPattern();
		// Assert
		Assert.assertFalse(pattern);
		Assert.assertEquals(classicRepresentation, testee.toXRIRepresentation());
	}

	@Test
	public void whenSubSegmentWithWildcardThenPathComponentIsAPattern(){
		// Arrange
		final String classicRepresentation = "Test*($.)";
		final XRISegment testee = XRISegment.valueOf(4, classicRepresentation);
		// Act
		final boolean pattern = testee.isPattern();
		// Assert
		Assert.assertTrue(pattern);
		Assert.assertEquals(classicRepresentation, testee.toXRIRepresentation());
	}

	@Test
	public void whenForeignURIThenUseCrossReferenceRepresentation(){
		final XRISegment testee = XRISegment.valueOf(4, "(java:comp/env)");
		// Act
		final String classicRepresentation = testee.toClassicRepresentation();
		// Assert
		Assert.assertEquals("(java:comp/env)", classicRepresentation);
	}

	@Test
	public void whenSubSegmentWithWildcardThenUsePlaceholderRepresentation(){
		final XRISegment testee = XRISegment.valueOf(4, "Test*($.)");
		// Act
		final String classicRepresentation = testee.toClassicRepresentation();
		// Assert
		Assert.assertEquals("Test*($.)", classicRepresentation);
	}

	@Test
	public void whenSingleSubSegmentWildcardThenAcceptSameLengthValue(){
		final XRISegment testee = XRISegment.valueOf(4, "Test*($.)");
		final XRISegment value = XRISegment.valueOf(4, "Test*bar");
		// Act
		final boolean match = testee.matches(value);
		// Assert
		Assert.assertTrue(match);
	}
	
	/**
	 * TODO change behaviour and document it
	 */
	@Test
	public void whenStarDelimitedSubSegmentThenAcceptBangDelimitedValue(){
		final XRISegment testee = XRISegment.valueOf(4, "Test*($.)");
		final XRISegment value = XRISegment.valueOf(4, "Test!bar");
		// Act
		final boolean match = testee.matches(value);
		// Assert
		Assert.assertTrue(match); // Should be false, shouldn't it?
	}

	@Test
	public void whenBangDelimitedSubSegmentThenRejectStarDelimitedValue(){
		final XRISegment testee = XRISegment.valueOf(4, "Test!($.)");
		final XRISegment value = XRISegment.valueOf(4, "Test*bar");
		// Act
		final boolean match = testee.matches(value);
		// Assert
		Assert.assertFalse(match);
	}

	@Test
	public void whenBangDelimitedSubSegmentThenAcceptBangDelimitedValue(){
		final XRISegment testee = XRISegment.valueOf(4, "Test!($.)");
		final XRISegment value = XRISegment.valueOf(4, "Test!bar");
		// Act
		final boolean match = testee.matches(value);
		// Assert
		Assert.assertFalse(match);
	}
	
	@Test
	public void whenDoubleSubSegmentPatternThenRejectShorterValue(){
		final XRISegment testee = XRISegment.valueOf(4, "Test*($.)");
		final XRISegment value = XRISegment.valueOf(4, "Test");
		// Act
		final boolean match = testee.matches(value);
		// Assert
		Assert.assertFalse(match);
	}

	@Test
	public void whenDoubleSubSegmentPatternThenRejectLongerValue(){
		final XRISegment testee = XRISegment.valueOf(4, "Test*($.)");
		final XRISegment value = XRISegment.valueOf(4, "Test*foo*bar");
		// Act
		final boolean match = testee.matches(value);
		// Assert
		Assert.assertFalse(match);
	}

	@Test
	public void whenPrefixWildcardThenRejectShorterValue(){
		final XRISegment testee = XRISegment.valueOf(4, "Test*($.*ba)");
		final XRISegment value = XRISegment.valueOf(4, "Test*b");
		// Act
		final boolean match = testee.matches(value);
		// Assert
		Assert.assertFalse(match);
	}

	@Test
	public void whenPrefixWildcardThenAcceptSameLengthValue(){
		final XRISegment testee = XRISegment.valueOf(4, "Test*($.*ba)");
		final XRISegment value = XRISegment.valueOf(4, "Test*ba");
		// Act
		final boolean match = testee.matches(value);
		// Assert
		Assert.assertTrue(match);
	}

	@Test
	public void wildcardIsComparableToPlainVanilla(){
        final XRISegment left = XRISegment.valueOf(4, "Test*($.*ba)");
        final XRISegment right = XRISegment.valueOf(4, "Test*ba");
        // Act
        final int value = left.compareTo(right);
        // Assert
        Assert.assertTrue(value != 0);
	}

    @Test
    public void plainVanillaIsComparableToWildcard(){
        // Arrange
        final XRISegment left = XRISegment.valueOf(4, "Test*ba");
        final XRISegment right = XRISegment.valueOf(4, "Test*($.*ba)");
        // Act
        final int value = left.compareTo(right);
        // Assert
        Assert.assertTrue(value != 0);
    }

    @Test 
    public void authoritySegmentIsComparableToTransactionalSegment(){
        // Arrange
        final XRISegment left = new Path(UUIDs.newUUID()).getLastSegment();
        final XRISegment right = new Path("xri://@openmdx*org.openmdx.base").getLastSegment();
        // Act
        final int value = left.compareTo(right);
        // Assert
        Assert.assertTrue(value != 0);
    }
    
    @Test 
    public void transactionalSegmentIsComparableToAuthoritySegment(){
        // Arrange
        final XRISegment left = new Path("xri://@openmdx*org.openmdx.base").getLastSegment();
        final XRISegment right = new Path(UUIDs.newUUID()).getLastSegment();
        // Act
        final int value = left.compareTo(right);
        // Assert
        Assert.assertTrue(value != 0);
    }
    
	@Test
	public void whenPrefixWildcardThenAcceptLongerValue(){
		final XRISegment testee = XRISegment.valueOf(4, "Test*($.*ba)");
		final XRISegment value = XRISegment.valueOf(4, "Test*bar");
		// Act
		final boolean match = testee.matches(value);
		// Assert
		Assert.assertTrue(match);
	}

}
