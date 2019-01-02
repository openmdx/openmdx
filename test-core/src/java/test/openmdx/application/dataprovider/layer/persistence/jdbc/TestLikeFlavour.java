/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test Like Flavour 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
 * OR TORT (INCLUDING NEGLIGENCE OR OTHJavaUpperERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package test.openmdx.application.dataprovider.layer.persistence.jdbc;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.LikeFlavour;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

public class TestLikeFlavour {

	@Test(expected=IllegalArgumentException.class) // Assert
	public void whenNoneThenSQLLower(){
		// Arrange
		// Act
		LikeFlavour.parse("  ");
	}

	@Test(expected=IllegalArgumentException.class) // Assert
	public void whenUnknownThenIllegalArgumentException(){
		// Arrange
		// Act
		LikeFlavour.parse("UNKNOWN");
	}

	public void whenNotSupportedThenUnsupportedOperationException() {
		// Arrange
		List<LikeFlavour> likeFlavour = LikeFlavour.parse("NOT_SUPPORTED");
		StringBuilder clause = new StringBuilder();
		// Act
		try {
			LikeFlavour.applyAll(likeFlavour, clause, null, null, null);
			Assert.fail("NOT_SUPPORTED expected");
		} catch (ServiceException expected) {
			// Assert
			Assert.assertEquals(BasicException.Code.NOT_SUPPORTED, expected.getExceptionCode()); 
		}
	}
	
	@Test
	public void whenUpperSQLThenUpperSQL(){
		// Arrange
		String configuration = "\tUPPER_SQL";
		// Act
		final List<LikeFlavour> likeFlavours = LikeFlavour.parse(configuration);
		// Assert
		Assert.assertEquals(Arrays.asList(LikeFlavour.UPPER_SQL), likeFlavours);
	}
	
	@Test
	public void whenUpperJavaAndLowerJavaThenUpperJavaAndLowerJava(){
		// Arrange
		String configuration = "\tUPPER_JAVA | LOWER_JAVA ";
		// Act
		final List<LikeFlavour> likeFlavours = LikeFlavour.parse(configuration);
		// Assert
		Assert.assertEquals(Arrays.asList(LikeFlavour.UPPER_JAVA, LikeFlavour.LOWER_JAVA), likeFlavours);
	}

}
