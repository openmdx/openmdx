/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Navigation Compartment Test
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
package org.openmdx.application.mof.mapping.pimdoc.text;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.application.mof.mapping.pimdoc.spi.PackagePatternComparator;

/**
 * Navigation Compartment Test
 */
class IndexMapper_DataCollectorTest {

	@Test
	void when_wildcardMatch_then_true() {
		//
		// Arrange
		//
		final IndexMapper.DataCollector testee = new IndexMapper.DataCollector();
		final String packagePattern = "org:openmdx:**";
		final String candidate = "org:openmdx:base:Segment";
		// 
		// Act		
		//
		final boolean result = testee.isPartOfPackageGroup(packagePattern, candidate);
		//
		// Assert
		//
		Assertions.assertTrue(result);
	}

	@Test
	void when_wildcardMismatch_then_false() {
		//
		// Arrange
		//
		final IndexMapper.DataCollector testee = new IndexMapper.DataCollector();
		final String packagePattern = "org:w3c:**";
		final String candidate = "org:openmdx:base:Segment";
		// 
		// Act		
		//
		final boolean result = testee.isPartOfPackageGroup(packagePattern, candidate);
		//
		// Assert
		//
		Assertions.assertFalse(result);
	}
	
	@Test
	void when_exactMatch_then_true() {
		//
		// Arrange
		//
		final IndexMapper.DataCollector testee = new IndexMapper.DataCollector();
		final String packagePattern = "org:openmdx:base:base";
		final String candidate = "org:openmdx:base:Segment";
		// 
		// Act		
		//
		final boolean result = testee.isPartOfPackageGroup(packagePattern, candidate);
		//
		// Assert
		//
		Assertions.assertTrue(result);
	}

	@Test
	void when_exactMismatch_then_false() {
		//
		// Arrange
		//
		final IndexMapper.DataCollector testee = new IndexMapper.DataCollector();
		final String packagePattern = "org:openmdx:openmdx";
		final String candidate = "org:openmdx:base:Segment";
		// 
		// Act		
		//
		final boolean result = testee.isPartOfPackageGroup(packagePattern, candidate);
		//
		// Assert
		//
		Assertions.assertFalse(result);
	}
	
	@Test
	void when_normalized_then_noEmptyValues() {
		//
		// Arrange
		//
		final IndexMapper.DataCollector testee = new IndexMapper.DataCollector();
		testee.addKey("X");
		testee.addKey("Y");
		testee.addKey("Z");
		testee.get("Y").add("y");
		// 
		// Act		
		//
		testee.normalize();
		//
		// Assert
		//
		Assertions.assertEquals(Collections.singleton("Y"), testee.keySet());
	}

	@Test
	void when_default_then_catchAll() {
		//
		// Arrange
		//
		final IndexMapper.DataCollector testee = new IndexMapper.DataCollector();
		testee.addKey(PackagePatternComparator.getCatchAllPattern());
		testee.addElement("com:example:AnyClass");
		// 
		// Act		
		//
		testee.normalize();
		//
		// Assert
		//
		Assertions.assertEquals(Collections.singletonMap(PackagePatternComparator.getCatchAllPattern(),Collections.singleton("com:example:AnyClass")), testee);
	}
	
}
