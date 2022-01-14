/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Plain Vanilla Path Component Test
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

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Plain Vanilla Path Component Test
 */
public class PlainVanillaSegmentTest {

    private Random random;
    
    @BeforeEach
    public void setup(){
        this.random = new Random();
    }
    
	@Test
	public void discriminantIsClassicRepresentation(){
		// Arrange
		final String classicRepresentation = "Test";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		// Act
		final String discriminant = (String) testee.discriminant();
		// Assert
		Assertions.assertEquals(classicRepresentation, discriminant);
	}

	@Test
	public void discriminantIsXRIRepresentation(){
		// Arrange
		final String xriRepresentation = "Test";
		final XRISegment testee = XRISegment.valueOf(3, xriRepresentation);
		// Act
		final String discriminant = (String) testee.discriminant();
		// Assert
		Assertions.assertEquals(xriRepresentation, discriminant);
	}
	
	@Test
	public void isNoPattern(){
		// Arrange
		final String classicRepresentation = "Test";
		final XRISegment testee = XRISegment.valueOf(3, classicRepresentation);
		// Act
		final boolean pattern = testee.isPattern();
		// Assert
		Assertions.assertFalse(pattern);
	}
	
    @Test
	public void authorityIsInternalized(){
        // Arrange
	    final String classicRepresentation = newObjectId();
        final XRISegment testee = XRISegment.valueOf(0, classicRepresentation);
        // Act
        final String unifiedRepresentation = testee.toClassicRepresentation();
        // Assert
        Assertions.assertTrue(testee instanceof AuthoritySegment);
        Assertions.assertTrue(isInternalized(unifiedRepresentation));
	}

    @Test
    public void providerIsInternalized(){
        final String classicRepresentation = newObjectId();
        final XRISegment testee = XRISegment.valueOf(2, classicRepresentation);
        // Act
        final String unifiedRepresentation = testee.toClassicRepresentation();
        // Assert
        Assertions.assertTrue(isInternalized(unifiedRepresentation));
    }

    @Test
    public void segmentIsInternalized(){
        final String classicRepresentation = newObjectId();
        final XRISegment testee = XRISegment.valueOf(4, classicRepresentation);
        // Act
        final String unifiedRepresentation = testee.toClassicRepresentation();
        // Assert
        Assertions.assertTrue(isInternalized(unifiedRepresentation));
    }

    @Test
    public void lowerReferenceIsInternalized(){
        final String classicRepresentation = newReferenceId("reference");
        final XRISegment testee = XRISegment.valueOf(1, classicRepresentation);
        // Act
        final String unifiedRepresentation = testee.toClassicRepresentation();
        // Assert
        Assertions.assertTrue(isInternalized(unifiedRepresentation));
    }

    @Test
    public void higherReferenceIsInternalized(){
        final String classicRepresentation = newReferenceId("reference");
        final XRISegment testee = XRISegment.valueOf(11, classicRepresentation);
        // Act
        final String unifiedRepresentation = testee.toClassicRepresentation();
        // Assert
        Assertions.assertTrue(isInternalized(unifiedRepresentation));
    }
    
    @Test
    public void objectIsNotInternalized(){
        final String classicRepresentation = newObjectId();
        final XRISegment testee = XRISegment.valueOf(6, classicRepresentation);
        // Act
        final String unifiedRepresentation = testee.toClassicRepresentation();
        // Assert
        Assertions.assertFalse(isInternalized(unifiedRepresentation));
    }

    private String newObjectId() {
        return deInternalize(newId());
    }

    private String newReferenceId(String prefix) {
        return deInternalize(prefix + newId());
    }
    
    private String newId() {
        final String classicRepresentation = String.valueOf(random.nextInt(1000000000));
        return classicRepresentation;
    }

    private static String deInternalize(final String classicRepresentation) {
        final String alternativeRepesentation = new String(classicRepresentation);
        return classicRepresentation == classicRepresentation.intern() ? alternativeRepesentation : classicRepresentation;
    }

    private static boolean isInternalized(
        String string
    ){
        return string == string.intern();
    }
    
}
