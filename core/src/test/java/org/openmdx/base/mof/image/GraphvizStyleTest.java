/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Graphviz Style Sheet Test
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

import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.application.mof.mapping.pimdoc.MagicFile;

/**
 * Graphviz Style Sheet Test
 */
public class GraphvizStyleTest {

	@Test
	void when_comment_then_removed() {
		//
		// Arrange
		//
		URL url = imageStyleSheetDefaultURL(); 
		//
		// Act
		//
		final String styleSheet = GraphvizStyle.readContent(url);
		//
		// Assert
		//
		Assertions.assertTrue(styleSheet.contains("graph["));
		Assertions.assertFalse(styleSheet.contains("http://www.openmdx.org"));
	}

	static URL imageStyleSheetDefaultURL() {
		return MagicFile.STYLE_SHEET.getDefault(MagicFile.Type.IMAGE);
	}
	
	@Test
	void when_umlDeclaredClass_then_withCompartment() {
		//
		// Arrange
		//
		GraphvizStyle testee = new GraphvizStyle(imageStyleSheetDefaultURL()); 
		//
		// Act
		//
		final String value = testee.getClassStyle("declared_class").get("compartments");
		//
		// Assert
		//
		Assertions.assertEquals("true", value);
	}

	@Test
	void when_umlImportedClass_then_withoutCompartment() {
		//
		// Arrange
		//
		GraphvizStyle testee = new GraphvizStyle(imageStyleSheetDefaultURL()); 
		//
		// Act
		//
		final String value = testee.getClassStyle("imported_class").get("compartments");
		//
		// Assert
		//
		Assertions.assertEquals("false", value);
	}

	@Test
	void when_umlClass_then_noCompartment() {
		//
		// Arrange
		//
		GraphvizStyle testee = new GraphvizStyle(imageStyleSheetDefaultURL()); 
		//
		// Act
		//
		final String value = testee.getClassStyle("uml_class").get("compartments");
		//
		// Assert
		//
		Assertions.assertNull(value);
	}
	
	@Test
	void when_node_then_shapeRecord() {
		//
		// Arrange
		//
		GraphvizStyle testee = new GraphvizStyle(imageStyleSheetDefaultURL()); 
		//
		// Act
		//
		final String value = testee.getElementStyle("graph").get("splines");
		//
		// Assert
		//
		Assertions.assertEquals("ortho", value);
	}

}
