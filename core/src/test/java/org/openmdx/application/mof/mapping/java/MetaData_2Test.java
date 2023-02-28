/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Meta Data Provider Test
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
package org.openmdx.application.mof.mapping.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.mof1.SegmentClass;
import org.openmdx.base.mof1.SegmentFeatures;
import org.openmdx.kernel.loading.Resources;

/**
 * Meta Data Provider Test
 */
class MetaData_2Test {

	private static final String OPENMDXJDO_EXAMPLE = "org/openmdx/base/jpa3/Segment.openmdxjdo";
	
	@Test
	void myTest() {
		// Arrange
		final MetaData_2 testee = new MetaData_2(openmdxjdoBase());
		// Act
		final String providerColumnNme = testee
			.getClassMetaData(SegmentClass.QUALIFIED_NAME)
			.getFieldMetaData(SegmentFeatures.PROVIDER)
			.getColumn()
			.getName();
		// Assert
		Assertions.assertEquals("P$$PARENT", providerColumnNme);
	}

	private static String openmdxjdoBase() {
		String exmplePath = Resources.getResource(OPENMDXJDO_EXAMPLE).getPath();
		return exmplePath.substring(0, exmplePath.length() - OPENMDXJDO_EXAMPLE.length());
	}
	
}
