/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Object Record Test
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
package org.openmdx.base.rest.spi;

import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.model1.mof1.SegmentClass;
import org.openmdx.base.mof1.AuthorityClass;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.cci.MessageRecord;

class RecordSerializationTest {

	@Test
	void when_serializingQueryRecord_then_deserializingClone() throws ResourceException {
		// Arrange 
		final Long position = Long.valueOf(4711);
		final QueryRecord original = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
		original.setQueryType(SegmentClass.QUALIFIED_NAME);
		original.setPosition(position);
		final RecordSerialization testee = new RecordSerialization();
		// Act 
		final byte[] serializedRecord = testee.serialize(original);
		final QueryRecord clone = (QueryRecord) testee.deserialize(serializedRecord);
		// Assert 
		Assertions.assertEquals(SegmentClass.QUALIFIED_NAME, clone.getQueryType());
		Assertions.assertEquals(position, clone.getPosition());
	}

	@Test
	void when_serializingResultRecord_then_deserializingClone() throws ResourceException {
		// Arrange 
		final Long total = Long.valueOf(4711);
		final ResultRecord original = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
		original.setTotal(total);
		final RecordSerialization testee = new RecordSerialization();
		// Act 
		final byte[] serializedRecord = testee.serialize(original);
		final ResultRecord clone = (ResultRecord) testee.deserialize(serializedRecord);
		// Assert 
		Assertions.assertEquals(total, clone.getTotal());
	}

	@Test
	void when_serializingMessgaRecord_then_deserializingClone() throws ResourceException {
		// Arrange 
		final Path xri = new Path(AuthorityClass.XRI);
		final MessageRecord original = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
		original.setResourceIdentifier(xri);
		final RecordSerialization testee = new RecordSerialization();
		// Act 
		final byte[] serializedRecord = testee.serialize(original);
		final MessageRecord clone = (MessageRecord) testee.deserialize(serializedRecord);
		// Assert 
		Assertions.assertEquals(xri, clone.getResourceIdentifier());
	}
	
	@Test
	void when_serializingNull_then_deserializingNull() throws ResourceException {
		// Arrange 
		final org.openmdx.base.rest.cci.QueryRecord original = null;
		final RecordSerialization testee = new RecordSerialization();
		// Act 
		final byte[] serializedRecord = testee.serialize(original);
		final org.openmdx.base.rest.cci.QueryRecord clone = (QueryRecord) testee.deserialize(serializedRecord);
		// Assert 
		Assertions.assertNull(clone);
	}

}