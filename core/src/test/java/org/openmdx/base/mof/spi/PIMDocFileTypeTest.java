/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: PIMDoc File Type Test
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
package org.openmdx.base.mof.spi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.exception.RuntimeServiceException;

/**
 * PIMDoc File Type Test
 */
class PIMDocFileTypeTest {

	@Test
	void when_endingMatches_then_true(){
		// Arrange
		final String uri = "com/example/index.html";
		// Act
		final boolean matches = PIMDocFileType.TEXT.test(uri);
		// Assert
		Assertions.assertTrue(matches);
	}

	@Test
	void when_endingMismatches_then_false(){
		// Arrange
		final String uri = "com/example/index.html";
		// Act
		final boolean matches = PIMDocFileType.IMAGE.test(uri);
		// Assert
		Assertions.assertFalse(matches);
	}

	@Test
	void toImagefromText() {
		// Arrange
		final String uri = "com/example/index.html";
		// Act
		final String imageURI = PIMDocFileType.IMAGE.from(uri, PIMDocFileType.TEXT);
		// Assert
		Assertions.assertEquals("com/example/index.svg", imageURI);
	}

	@Test
	void when_mismatch_then_fail() {
		// Arrange
		final String uri = "com/example/index.html";
		// Act/Assert
		Assertions.assertThrows(RuntimeServiceException.class, () -> PIMDocFileType.IMAGE.from(uri, PIMDocFileType.GRAPHVIZ_SOURCE));
	}

}
