/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Mapping Format Parser
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.application.mof.mapping.spi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.application.mof.mapping.cci.MappingTypes;

/**
 * Mapping Format Parser
 */
public class MappingFormatParserTest {
	
	@Test
	void when_noArguments_then_emptyArray(){
		//
		// Arrange
		//
		final String format = MappingTypes.JMI1;
		//
		// Act
		//
		final MappingFormatParser testee = new MappingFormatParser(format);
		//
		// Assert
		//
		Assertions.assertEquals(MappingTypes.JMI1, testee.getId());
		Assertions.assertArrayEquals(new String[0], testee.getArguments());
	}
	
	@Test
	void when_fullyQualified_then_classNameAndArguments(){
		//
		// Arrange
		//
		final String format = "com.example.TheMapper(left,right)";
		//
		// Act
		//
		final MappingFormatParser testee = new MappingFormatParser(format);
		//
		// Assert
		//
		Assertions.assertEquals("com.example.TheMapper", testee.getId());
		Assertions.assertArrayEquals(new String[] {"left","right"}, testee.getArguments());
	}

	@Test
	void when_colon_then_mappingTypeAndArgument(){
		//
		// Arrange
		//
		final String format = "pimdoc:~/mypimdoc.properties";
		//
		// Act
		//
		final MappingFormatParser testee = new MappingFormatParser(format);
		//
		// Assert
		//
		Assertions.assertEquals(MappingTypes.PIMDOC, testee.getId());
		Assertions.assertArrayEquals(new String[] {"~/mypimdoc.properties"}, testee.getArguments());
	}
	
}
