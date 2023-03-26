/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Graphviz Attributes
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GraphvizAttributesTest {
	
	@Test
	void when_nothingDefined_then_null() {
		//
		// Arrange
		//
		final GraphvizAttributes testee = new GraphvizAttributes(new GraphvizStyle());
		// Act
		final String value = testee.getValue("foo");
		// Assert
		Assertions.assertNull(value);
	}

	@Test
	void when_defaultDefined_then_defaultValue() {
		//
		// Arrange
		//
		final GraphvizAttributes testee = new GraphvizAttributes(new GraphvizStyle());
		testee.setDefaultValue("foo","bar");
		// Act
		final String value = testee.getValue("foo");
		// Assert
		Assertions.assertEquals("bar", value);
	}
	@Test

	void when_strictDefined_then_strictValue() {
		//
		// Arrange
		//
		final GraphvizAttributes testee = new GraphvizAttributes(new GraphvizStyle());
		testee.setDefaultValue("foo","bar");
		testee.setStrictValue("foo","b a r");
		// Act
		final String value = testee.getValue("foo");
		// Assert
		Assertions.assertEquals("b a r", value);
	}

	@Test
	void when_noClassDefined_then_defaultValue() {
		//
		// Arrange
		//
		final GraphvizAttributes testee = new GraphvizAttributes(new GraphvizStyle());
		testee.setDefaultValue("compartments","maybe");
		// Act
		final String value = testee.getAttributes().get("compartments");
		// Assert
		Assertions.assertEquals("maybe", value);
	}
	
	@Test
	void when_defaultClass_then_defaultClassValue() {
		//
		// Arrange
		//
		final GraphvizAttributes testee = new GraphvizAttributes(new GraphvizStyle(GraphvizStyleTest.imageStyleSheetDefaultURL()));
		testee.setDefaultValue("_class", "uml_class declared_class");
		testee.setDefaultValue("compartments","maybe");
		// Act
		final String value = testee.getAttributes().get("compartments");
		// Assert
		Assertions.assertEquals("true", value);
	}

	@Test
	void when_otherClass_then_otherValue() {
		//
		// Arrange
		//
		final GraphvizAttributes testee = new GraphvizAttributes(new GraphvizStyle(GraphvizStyleTest.imageStyleSheetDefaultURL()));
		testee.setDefaultValue("compartments","maybe");
		testee.setDefaultValue("_class", "uml_class");
		testee.parseParameters("_class=imported_class,foo=bar");
		// Act
		final String value = testee.getAttributes().get("compartments");
		// Assert
		Assertions.assertEquals("false", value);
	}

	@Test
	void when_otherClassAndParameterValue_then_parameterValue() {
		//
		// Arrange
		//
		final GraphvizAttributes testee = new GraphvizAttributes(new GraphvizStyle(GraphvizStyleTest.imageStyleSheetDefaultURL()));
		testee.setDefaultValue("compartments","maybe");
		testee.setDefaultValue("_class", "uml_class");
		testee.parseParameters("compartments=\"for sure\",_class=\"uml_class imported_class\",foo=\"bar\"");
		// Act
		final String value = testee.getAttributes().get("compartments");
		// Assert
		Assertions.assertEquals("for sure", value);
	}

	@Test
	void when_parameterAndStrictValu_then_strictValue() {
		//
		// Arrange
		//
		final GraphvizAttributes testee = new GraphvizAttributes(new GraphvizStyle(GraphvizStyleTest.imageStyleSheetDefaultURL()));
		testee.setDefaultValue("compartments","maybe");
		testee.setDefaultValue("_class", "uml_class");
		testee.parseParameters("compartments=\"for sure\",_class=\"uml_class imported_class\",foo=\"bar\"");
		testee.setStrictValue("compartments","never");
		// Act
		final String value = testee.getAttributes().get("compartments");
		// Assert
		Assertions.assertEquals("never", value);
	}
	
	@Test
	void when_unknownClass_then_defaultValue() {
		//
		// Arrange
		//
		final GraphvizAttributes testee = new GraphvizAttributes(new GraphvizStyle(GraphvizStyleTest.imageStyleSheetDefaultURL()));
		testee.setDefaultValue("compartments","maybe");
		testee.setDefaultValue("_class", "uml_class");
		testee.parseParameters("foo=bar,_class=gugus");
		// Act
		final String value = testee.getAttributes().get("compartments");
		// Assert
		Assertions.assertEquals("maybe", value);
	}

}
