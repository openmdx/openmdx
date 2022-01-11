/*
 * ====================================================================
 * Project:     openMDX, http://w.openmdx.org/
 * Description: Composed Predicate Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2019-2022, OMEX AG, Switzerland
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
package org.openmdx.resource.ldap.ldif;

import java.util.regex.Matcher;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.resource.ldap.ldif.ComposedPredicate.Operation;

public class ComposedPredicateTest {

	@Test
	public void and() {
		// Arrange
		final String filter = "(&(objectclass=user)(displayName=Jane Doe)";
		final Matcher matcher = ComposedPredicate.PATTERN.matcher(filter);
		// Act
		final boolean match = matcher.matches();
		final Operation operation = ComposedPredicate.Operation.fromOperator(matcher.group(1));
		// Assert
		Assertions.assertTrue(match);
		Assertions.assertEquals(Operation.AND, operation);
	}

	@Test
	public void or() {
		// Arrange
		final String filter = "(|(objectclass=user)(displayName=Jane Doe)";
		final Matcher matcher = ComposedPredicate.PATTERN.matcher(filter);
		// Act
		final boolean match = matcher.matches();
		final Operation operation = ComposedPredicate.Operation.fromOperator(matcher.group(1));
		// Assert
		Assertions.assertTrue(match);
		Assertions.assertEquals(Operation.OR, operation);
	}

	@Test
	public void not() {
		// Arrange
		final String filter = "(!(objectclass=user)(displayName=Jane Doe)";
		final Matcher matcher = ComposedPredicate.PATTERN.matcher(filter);
		// Act
		final boolean match = matcher.matches();
		final Operation operation = ComposedPredicate.Operation.fromOperator(matcher.group(1));
		// Assert
		Assertions.assertTrue(match);
		Assertions.assertEquals(Operation.NOT, operation);
	}

	@Test
	public void attribute() {
		// Arrange
		final String filter = "(objectclass=user)";
		// Act
		final boolean match = ComposedPredicate.PATTERN.matcher(filter).matches();
		// Assert
		Assertions.assertFalse(match);
	}

}
