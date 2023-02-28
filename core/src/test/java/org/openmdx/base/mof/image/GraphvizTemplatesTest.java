/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Graphviz Drawer Test
 * Owner: the original authors. 
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
package org.openmdx.base.mof.image;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Graphviz Drawer Test
 */
class GraphvizTemplatesTest {

	@Test
	void when_escaped_then_title(){
		// Arrange
		final String dot = "digraph \"Hello \\\"World\\\"\" {";
		final GraphvizTemplates testee = new GraphvizTemplates(null, null, null);
		// Act
		final Optional<String> title = testee.getTitle(dot);
		// Assert
		Assertions.assertTrue(title.isPresent());
		Assertions.assertEquals("Hello \"World\"", title.get());
	}

	@Test
	void when_quoted_then_title(){
		// Arrange
		final String dot = "digraph \"Hello World\" {";
		final GraphvizTemplates testee = new GraphvizTemplates(null, null, null);
		// Act
		final Optional<String> title = testee.getTitle(dot);
		// Assert
		Assertions.assertTrue(title.isPresent());
		Assertions.assertEquals("Hello World", title.get());
	}
	
	@Test
	void when_missing_then_noTitle(){
		// Arrange
		final String dot = "digraph {";
		final GraphvizTemplates testee = new GraphvizTemplates(null, null, null);
		// Act
		final Optional<String> title = testee.getTitle(dot);
		// Assert
		Assertions.assertFalse(title.isPresent());
	}
	
	@Test
	void when_name_then_title(){
		// Arrange
		final String dot = "digraph Hello_World_0 {";
		final GraphvizTemplates testee = new GraphvizTemplates(null, null, null);
		// Act
		final Optional<String> title = testee.getTitle(dot);
		// Assert
		Assertions.assertTrue(title.isPresent());
		Assertions.assertEquals("Hello_World_0", title.get());
	}

	@Test
	void when_html_then_title(){
		// Arrange
		final String dot = "digraph <Hello\n<b>World</b>> {";
		final GraphvizTemplates testee = new GraphvizTemplates(null, null, null);
		// Act
		final Optional<String> title = testee.getTitle(dot);
		// Assert
		Assertions.assertTrue(title.isPresent());
		Assertions.assertEquals("<Hello\n<b>World</b>>", title.get());
	}
	
	@Test
	void when_numeral_then_title(){
		// Arrange
		final String dot = "digraph -.9 {";
		final GraphvizTemplates testee = new GraphvizTemplates(null, null, null);
		// Act
		final Optional<String> title = testee.getTitle(dot);
		// Assert
		Assertions.assertTrue(title.isPresent());
		Assertions.assertEquals("-.9", title.get());
	}
	
	@Test
	void when_graphComment_the_ignored() {
		// Arrange
		final String dot = "/* digraph \"Hello Moon\" {\n} */\n// digraph HelloSunshine {}\ngraph \"Hello World\" {";
		final GraphvizTemplates testee = new GraphvizTemplates(null, null, null);
		// Act
		final Optional<String> title = testee.getTitle(dot);
		// Assert
		Assertions.assertTrue(title.isPresent());
		Assertions.assertEquals("Hello World", title.get());
	}

	@Test
	void when_digraphComment_the_ignored() {
		// Arrange
		final String dot = "/* digraph \"Hello Moon\" {\n} */\n// digraph HelloSunshine {}\ndigraph \"Hello World\"{";
		final GraphvizTemplates testee = new GraphvizTemplates(null, null, null);
		// Act
		final Optional<String> title = testee.getTitle(dot);
		// Assert
		Assertions.assertTrue(title.isPresent());
		Assertions.assertEquals("Hello World", title.get());
	}

}
