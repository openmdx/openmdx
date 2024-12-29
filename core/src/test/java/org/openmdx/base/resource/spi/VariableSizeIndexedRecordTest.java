/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Variable Size Indexed Record Test
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.base.resource.spi;

import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;
import #if JAVA_8 javax.resource.cci.IndexedRecord #else jakarta.resource.cci.IndexedRecord #endif;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;

/**
 * Variable Size Indexed Record Test
 */
public class VariableSizeIndexedRecordTest {

	final static ExtendedRecordFactory FACTORY = StandardRecordFactory.getInstance();
	
	@Test
	public void whenEmptySetIsComparedToEmptyListThenNotEqual() throws ResourceException{
		// Arrange
		
		final IndexedRecord left = FACTORY.createIndexedRecord(Multiplicity.SET.code());
		final IndexedRecord right = FACTORY.createIndexedRecord(Multiplicity.LIST.code());
		// Act
		final boolean equal = left.equals(right);
		// Assert
		Assertions.assertFalse(equal);
	}
	
	@Test
	public void whenEmptyListIsComparedToEmptySetThenNotEqual() throws ResourceException{
		// Arrange
		final IndexedRecord left = FACTORY.createIndexedRecord(Multiplicity.LIST.code());
		final IndexedRecord right = FACTORY.createIndexedRecord(Multiplicity.SET.code());
		// Act
		final boolean equal = left.equals(right);
		// Assert
		Assertions.assertFalse(equal);
	}
	
	@Test
	public void whenEmptySetIsComparedToEmptySetThenEqual() throws ResourceException{
		// Arrange
		final IndexedRecord left = FACTORY.createIndexedRecord(Multiplicity.SET.code());
		final IndexedRecord right = FACTORY.createIndexedRecord(Multiplicity.SET.code());
		// Act
		final boolean equal = left.equals(right);
		// Assert
		Assertions.assertTrue(equal);
	}
	
	@Test
	public void whenEmptyListIsComparedToEmptyListThenEqual() throws ResourceException{
		// Arrange
		final IndexedRecord left = FACTORY.createIndexedRecord(Multiplicity.LIST.code());
		final IndexedRecord right = FACTORY.createIndexedRecord(Multiplicity.LIST.code());
		// Act
		final boolean equal = left.equals(right);
		// Assert
		Assertions.assertTrue(equal);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void whenVariableSizedSetComparedToFixedOneThenEqual() throws ResourceException{
		// Arrange
		final IndexedRecord left = FACTORY.createIndexedRecord(Multiplicity.SET.code());
		left.add("A");
		left.add("B");
		final IndexedRecord right = FACTORY.asIndexedRecord(
			Multiplicity.SET.code(), 
			null, // recordShortDescription
			new String[]{"B", "A"}
		);
		// Act
		final boolean equal = left.equals(right);
		// Assert
		Assertions.assertTrue(equal);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenDifferentOrderInSetThenEqual() throws ResourceException{
		// Arrange
		final IndexedRecord left = FACTORY.createIndexedRecord(Multiplicity.SET.code());
		left.add("A");
		left.add("B");
		final IndexedRecord right = FACTORY.createIndexedRecord(Multiplicity.SET.code());
		right.add("B");
		right.add("A");
		// Act
		final boolean equal = left.equals(right);
		// Assert
		Assertions.assertTrue(equal);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSameOrderInListThenEqual() throws ResourceException{
		// Arrange
		final IndexedRecord left = FACTORY.createIndexedRecord(Multiplicity.LIST.code());
		left.add("A");
		left.add("B");
		final IndexedRecord right = FACTORY.createIndexedRecord(Multiplicity.LIST.code());
		right.add("A");
		right.add("B");
		// Act
		final boolean equal = left.equals(right);
		// Assert
		Assertions.assertTrue(equal);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenDifferentOrderInListThenNotEqual() throws ResourceException{
		// Arrange
		final IndexedRecord left = FACTORY.createIndexedRecord(Multiplicity.LIST.code());
		left.add("A");
		left.add("B");
		final IndexedRecord right = FACTORY.createIndexedRecord(Multiplicity.LIST.code());
		right.add("B");
		right.add("A");
		// Act
		final boolean equal = left.equals(right);
		// Assert
		Assertions.assertFalse(equal);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenDifferentOrderInListThenDifferentHashCodel() throws ResourceException{
		// Arrange
		final IndexedRecord left = FACTORY.createIndexedRecord(Multiplicity.LIST.code());
		left.add("A");
		left.add("B");
		final IndexedRecord right = FACTORY.createIndexedRecord(Multiplicity.LIST.code());
		right.add("B");
		right.add("A");
		// Act
		final int leftHashCode = left.hashCode();
		final int rightHashCode = right.hashCode();
		// Assert
		Assertions.assertNotEquals(leftHashCode, rightHashCode);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenDifferentOrderInSetThenDifferentHashCodel() throws ResourceException{
		// Arrange
		final IndexedRecord left = FACTORY.createIndexedRecord(Multiplicity.SET.code());
		left.add("A");
		left.add("B");
		final IndexedRecord right = FACTORY.createIndexedRecord(Multiplicity.SET.code());
		right.add("B");
		right.add("A");
		// Act
		final int leftHashCode = left.hashCode();
		final int rightHashCode = right.hashCode();
		// Assert
		Assertions.assertEquals(leftHashCode, rightHashCode);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void whenVariableSizedListComparedToFixedOneThenEqual() throws ResourceException{
		// Arrange
		final IndexedRecord left = FACTORY.createIndexedRecord(Multiplicity.LIST.code());
		left.add("A");
		left.add("B");
		final IndexedRecord right = FACTORY.asIndexedRecord(
			Multiplicity.LIST.code(), 
			null, // recordShortDescription
			new String[]{"A", "B"}
		);
		// Act
		final boolean equal = left.equals(right);
		// Assert
		Assertions.assertTrue(equal);
	}

}
