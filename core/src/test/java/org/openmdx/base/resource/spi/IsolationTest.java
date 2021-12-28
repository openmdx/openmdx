/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Isolation Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014-2021, OMEX AG, Switzerland
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
package org.openmdx.base.resource.spi;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.Freezable;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.VoidRecord;

/**
 * Isolation Test
 */
public class IsolationTest {

	@Test
	public void whenMutableThenReturnClone() throws ResourceException{
		// Arrange
		final MappedRecord record = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
		// Act
		final MappedRecord isolated = Isolation.isolate(record);
		// Assert
		Assertions.assertNotSame(record, isolated);
		Assertions.assertEquals(record, isolated);
	}
	
	@Test
	public void whenFrozenThenReturnOriginal() throws ResourceException{
		// Arrange
		final MappedRecord record = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
		((Freezable)record).makeImmutable();
		// Act
		final MappedRecord isolated = Isolation.isolate(record);
		// Assert
		Assertions.assertSame(record, isolated);
	}

	@Test
	public void whenImmutableThenReturnOriginal() throws ResourceException{
		// Arrange
		final MappedRecord record = Records.getRecordFactory().createMappedRecord(VoidRecord.class);
		// Act
		final MappedRecord isolated = Isolation.isolate(record);
		// Assert
		Assertions.assertSame(record, isolated);
	}
}
