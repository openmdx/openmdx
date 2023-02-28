/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: A Binary Sink
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SinkTest {

	@Test
	void when_relativePathFromRoot_then_uriWithoutLeadingSlash() {
		// Arrange
		final Sink testee = Testee.ROOT;
		// Act
		URI relative = testee.relativize(URI.create("/com/example/foo/bar"));
		// Assert
		Assertions.assertEquals("com/example/foo/bar", relative.getPath());
	}

	@Test
	void when_relativePathFromSamePrefix_then_uriWithoutPrefix() {
		// Arrange
		final Sink testee = new Testee("com/example");
		// Act
		URI relative = testee.relativize(URI.create("/com/example/foo/bar"));
		// Assert
		Assertions.assertEquals("foo/bar", relative.getPath());
	}
	
	@Test
	void when_relativePathFromDisjunctivetPrefix_then_uriStartWithDoubleDotSequence() {
		// Arrange
		final Sink testee = new Testee("com/sun");
		// Act
		URI relative = testee.relativize(URI.create("/org/eclipse/foo/bar"));
		// Assert
		Assertions.assertEquals("../../org/eclipse/foo/bar", relative.getPath());
	}

	/**
	 * At the moment we are satisfied with the non-normalized form 
	 * {@code "../../com/example/foo/bar"} (as opposed to {@code "../example/foo/bar"})!
	 */
	@Test
	void when_relativePathFromDifferentPrefix_then_uriStartWithDoubleDotSequence() {
		// Arrange
		final Sink testee = new Testee("com/sun");
		// Act
		URI relative = testee.relativize(URI.create("/com/example/foo/bar"));
		// Assert
		Assertions.assertEquals("../../com/example/foo/bar", relative.getPath());
	}
	
	/**
	 * Tests the Testee itself
	 */
	@Test
	void when_TesteeRoot_then_pathEqualsSlash() {
		// Arrange
		final Sink testee = Testee.ROOT;
		// Act
		URI path = testee.path();
		// Assert
		Assertions.assertEquals("/", path.getPath());
	}

	/**
	 * Tests the Testee itself
	 */
	@Test
	void when_TesteeOther_then_pathStartsAndEndsWithSlash() {
		// Arrange
		final Sink testee = new Testee("com/example/foo/bar");
		// Act
		URI path = testee.path();
		// Assert
		Assertions.assertEquals("/com/example/foo/bar/", path.getPath());
	}
	
	
	private static class Testee extends AbstractSink{
		
		
		private Testee() {
			super();
		}

		Testee(String path) {
			super(ROOT, path);
		}
		
		private final static Testee ROOT = new Testee();

		@Override
		public void accept(String name, String title, ByteArrayOutputStream sinkable) throws IOException {
			// nothing to do
		}

		@Override
		public Sink nested(String name) {
			return null;
		}

	}
	
}
