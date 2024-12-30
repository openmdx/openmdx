/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Dataprovider Request Processor Test
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
package org.openmdx.base.dataprovider.cci;

import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;
import #if JAVA_8 javax.resource.cci.MappedRecord #else jakarta.resource.cci.MappedRecord #endif;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.dataprovider.layer.persistence.none.InMemory_2;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.ExtentCollection;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.ConditionRecord;
import org.openmdx.base.rest.spi.FeatureOrderRecord;
import org.openmdx.kernel.exception.BasicException;


public class DataproviderRequestProcessorTest {

	@BeforeEach
	public void setUp() throws ResourceException{
		Port<RestConnection> volatileProvider = new VolatileProvider();
		testee = new DataproviderRequestProcessor(null, volatileProvider);
		helper = new DataproviderRequestProcessor(null, volatileProvider);
	}

	private Channel testee;
	private Channel helper;

	@Test
	public void whenAuthorityDoesNotExistThenCreateAndReturnItAsObjectRecord() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2");
		// Act
		final ObjectRecord objectRecord = testee.addGetRequest(xri);
		// Assert
		Assertions.assertEquals("org:openmdx:base:Authority", objectRecord.getValue().getRecordName());
		Assertions.assertEquals(xri, objectRecord.getResourceIdentifier());
	}
	
	@Test
	public void whenAuthorityDoesNotExistThenCreateAndReturnItInResultRecord() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2");
		// Act
		final ResultRecord resultRecord = testee.addFindRequest(xri);
		// Assert
		Assertions.assertEquals(1, resultRecord.size());
		final Object object = resultRecord.get(0);
		Assertions.assertTrue(object instanceof ObjectRecord);
		final ObjectRecord objectRecord = (ObjectRecord) object;
		Assertions.assertEquals("org:openmdx:base:Authority", objectRecord.getValue().getRecordName());
		Assertions.assertEquals(xri, objectRecord.getResourceIdentifier());
	}
	
	@Test
	public void whenProviderDoesNotExistThenCreateAndReturnItAsObjectRecord() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0");
		// Act
		final ObjectRecord objectRecord = testee.addGetRequest(xri);
		// Assert
		Assertions.assertEquals("org:openmdx:base:Provider", objectRecord.getValue().getRecordName());
		Assertions.assertEquals(xri, objectRecord.getResourceIdentifier());
	}
	
	@Test
	public void whenProviderDoesNotExistThenCreateAndReturnItInResultRecord() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0");
		// Act
		final ResultRecord resultRecord = testee.addFindRequest(xri);
		// Assert
		Assertions.assertEquals(1, resultRecord.size());
		final Object object = resultRecord.get(0);
		Assertions.assertTrue(object instanceof ObjectRecord);
		final ObjectRecord objectRecord = (ObjectRecord) object;
		Assertions.assertEquals("org:openmdx:base:Provider", objectRecord.getValue().getRecordName());
		Assertions.assertEquals(xri, objectRecord.getResourceIdentifier());
	}
	
	@Test
	public void whenSegmentDoesNotExistThenReturnNull() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		// Act
		final ObjectRecord objectRecord = testee.addGetRequest(xri);
		// Assert
		Assertions.assertNull(objectRecord);
	}
	
	@Test
	public void whenSegmentDoesNotExistThenReturnEmptyResultRecord() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		// Act
		final ResultRecord resultRecord = testee.addFindRequest(xri);
		// Assert
		Assertions.assertNotNull(resultRecord);
		Assertions.assertTrue(resultRecord.isEmpty());
	}

	@Test
	public void whenSegmentDoesNotExistThenItMayBeCreated() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		final MappedRecord value = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		final ObjectRecord input = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
		input.setResourceIdentifier(xri);
		input.setValue(value);
		// Act
		testee.addCreateRequest(input);
		// Assert
		Assertions.assertNotNull(retrieveObject(xri));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void whenSegmentDoesExistThenItMayBeUpdated() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		final MappedRecord originalValue = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		final String feature = "description";
		originalValue.put(feature, "original description");
		createObject(xri, originalValue);
		final MappedRecord value = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		final String newValue = "modified description";
		value.put(feature, newValue);
		final ObjectRecord input = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
		input.setResourceIdentifier(xri);
		input.setValue(value);
		// Act
		testee.addUpdateRequest(input);
		// Assert
		Assertions.assertEquals(retrieveObject(xri).getValue().get(feature), newValue);
	}

	@Test
	public void whenSegmentDoesExistThenItMayBeDeleted() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		final MappedRecord value = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		createObject(xri, value);
		// Act
		testee.addRemoveRequest(xri);
		// Assert
		Assertions.assertNull(retrieveObject(xri));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenOrderIsUnspecifiedThenResultIsOrderedByResourceIdentifier() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment");
		final String feature = "description";
		final Path xri1 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/1");
		final MappedRecord value1 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value1.put(feature, "101");
		createObject(xri1, value1);
		final Path xri2 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/2");
		final MappedRecord value2 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value2.put(feature, "102");
		createObject(xri2, value2);
		final Path xri3 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/3");
		final MappedRecord value3 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value3.put(feature, "200");
		createObject(xri3, value3);
		// Act
		final QueryRecord input = testee.newQueryRecordWithFilter(xri);
		final ConditionRecord condition = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			feature,
			ConditionType.IS_LIKE,
			"1.*"
		);
		input.getQueryFilter().getCondition().add(condition);
		input.setResourceIdentifier(xri);
		input.setQueryFilter(input.getQueryFilter());
		final ResultRecord resultRecord =testee.addFindRequest(input);
		// Assert
		Assertions.assertNotNull(resultRecord);
		Assertions.assertEquals(2, resultRecord.size());
		Assertions.assertEquals(xri1, ((ObjectRecord)resultRecord.get(0)).getResourceIdentifier());
		Assertions.assertEquals(xri2, ((ObjectRecord)resultRecord.get(1)).getResourceIdentifier());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void whenOrderIsSpecifiedThenResultIsOrderedByFeature() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment");
		final String feature = "description";
		final Path xri1 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/1");
		final MappedRecord value1 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value1.put(feature, "101");
		createObject(xri1, value1);
		final Path xri2 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/2");
		final MappedRecord value2 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value2.put(feature, "102");
		createObject(xri2, value2);
		final Path xri3 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/3");
		final MappedRecord value3 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value3.put(feature, "200");
		createObject(xri3, value3);
		// Act
		final QueryRecord input = testee.newQueryRecordWithFilter(xri);
		final ConditionRecord condition = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			feature,
			ConditionType.IS_LIKE,
			"1.*"
		);
		input.getQueryFilter().getCondition().add(condition);
		input.getQueryFilter().getOrderSpecifier().add(new FeatureOrderRecord(feature, SortOrder.DESCENDING));
		final ResultRecord resultRecord = testee.addFindRequest(input);
		// Assert
		Assertions.assertNotNull(resultRecord);
		Assertions.assertEquals(2, resultRecord.size());
		Assertions.assertEquals(xri2, ((ObjectRecord)resultRecord.get(0)).getResourceIdentifier());
		Assertions.assertEquals(xri1, ((ObjectRecord)resultRecord.get(1)).getResourceIdentifier());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenRetrievedFromExtentThenReturnNodesOfDifferentPreferences(
	) throws ResourceException{
		// Arrange
		final Path xri0 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		final MappedRecord value0 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Segment");
		createObject(xri0, value0);
		final Path xri1 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/1");
		final MappedRecord value1 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Preferences");
		value1.put("type","system");
		createObject(xri1, value1);
		final Path xri10 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/1/node/0");
		final MappedRecord value10 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Node");
		value10.put("absolutePath","/");
		createObject(xri10, value10);
		final Path xri11 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/1/node/1");
		final MappedRecord value11 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Node");
		value11.put("absolutePath","/1");
		value11.put("name","1");
		createObject(xri11, value11);
		final Path xri2 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/2");
		final MappedRecord value2 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Preferences");
		createObject(xri2, value2);
		final Path xri20 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/2/node/0");
		final MappedRecord value20 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Node");
		value20.put("absolutePath","/");
		createObject(xri20, value20);
		final Path xri21 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/2/node/1");
		final MappedRecord value21 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Node");
		value21.put("absolutePath","/1");
		value21.put("name","1");
		createObject(xri21, value21);
		// Act
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/extent");
		final QueryRecord input = testee.newQueryRecordWithFilter(xri);
		final ConditionRecord condition0 = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			SystemAttributes.OBJECT_INSTANCE_OF,
			ConditionType.IS_IN,
			"org:openmdx:preferences2:Node"
		);
		input.getQueryFilter().getCondition().add(condition0);
		final ConditionRecord condition1 = new ConditionRecord(
			Quantifier.THERE_EXISTS,
            SystemAttributes.OBJECT_IDENTITY,
			ConditionType.IS_LIKE,
            ExtentCollection.toIdentityPattern(
                new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/($...)")
            )
		);
		input.getQueryFilter().getCondition().add(condition1);
		final ConditionRecord condition2 = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			"name",
			ConditionType.IS_IN,
			"1"
		);
		input.getQueryFilter().getCondition().add(condition2);
		final ResultRecord resultRecord = testee.addFindRequest(input);
		// Assert
		Assertions.assertNotNull(resultRecord);
		Assertions.assertEquals(2, resultRecord.size());
		Assertions.assertEquals(xri11, ((ObjectRecord)resultRecord.get(0)).getResourceIdentifier());
		Assertions.assertEquals(xri21, ((ObjectRecord)resultRecord.get(1)).getResourceIdentifier());
	}

	@Test
	public void whenAuthorityDoesNotExistThenCreateAndReturnItAsObjectRecordLater() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2");
		// Act
		testee.beginBatch();
		final ObjectRecord objectRecord = testee.addGetRequest(xri);
		testee.endBatch();
		// Assert
		Assertions.assertEquals("org:openmdx:base:Authority", objectRecord.getValue().getRecordName());
		Assertions.assertEquals(xri, objectRecord.getResourceIdentifier());
	}
	
	@Test
	public void whenAuthorityDoesNotExistThenCreateAndReturnItInResultRecordLater() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2");
		// Act
		testee.beginBatch();
		final ResultRecord resultRecord = testee.addFindRequest(xri);
		testee.endBatch();
		// Assert
		Assertions.assertEquals(1, resultRecord.size());
		final Object object = resultRecord.get(0);
		Assertions.assertTrue(object instanceof ObjectRecord);
		final ObjectRecord objectRecord = (ObjectRecord) object;
		Assertions.assertEquals("org:openmdx:base:Authority", objectRecord.getValue().getRecordName());
		Assertions.assertEquals(xri, objectRecord.getResourceIdentifier());
	}
	
	@Test
	public void whenProviderDoesNotExistThenCreateAndReturnItAsObjectRecordLater() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0");
		// Act
		testee.beginBatch();
		final ObjectRecord objectRecord = testee.addGetRequest(xri);
		testee.endBatch();
		// Assert
		Assertions.assertEquals("org:openmdx:base:Provider", objectRecord.getValue().getRecordName());
		Assertions.assertEquals(xri, objectRecord.getResourceIdentifier());
	}
	
	@Test
	public void whenProviderDoesNotExistThenCreateAndReturnItInResultRecordLater() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0");
		// Act
		testee.beginBatch();
		final ResultRecord resultRecord = testee.addFindRequest(xri);
		testee.endBatch();
		// Assert
		Assertions.assertEquals(1, resultRecord.size());
		final Object object = resultRecord.get(0);
		Assertions.assertTrue(object instanceof ObjectRecord);
		final ObjectRecord objectRecord = (ObjectRecord) object;
		Assertions.assertEquals("org:openmdx:base:Provider", objectRecord.getValue().getRecordName());
		Assertions.assertEquals(xri, objectRecord.getResourceIdentifier());
	}
	
	@Test
	public void whenSegmentDoesNotExistThenReturnNullLater() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		// Act
		testee.beginBatch();
		final ObjectRecord objectRecord = testee.addGetRequest(xri);
		testee.endBatch();
		// Assert
		try {
			objectRecord.getValue();
			Assertions.fail("Object does not exist");
		} catch (RuntimeException expected) {
			final BasicException exceptionStack = BasicException.toExceptionStack(expected);
			Assertions.assertEquals(BasicException.Code.NO_RESPONSE, exceptionStack.getExceptionCode());
		}
	}
	
	@Test
	public void whenSegmentDoesNotExistThenReturnEmptyResultRecordLater() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		// Act
		testee.beginBatch();
		final ResultRecord resultRecord = testee.addFindRequest(xri);
		testee.endBatch();
		// Assert
		Assertions.assertNotNull(resultRecord);
		Assertions.assertTrue(resultRecord.isEmpty());
	}

	@Test
	public void whenSegmentDoesNotExistThenItMayBeCreatedLater() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		final MappedRecord value = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		final ObjectRecord input = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
		input.setResourceIdentifier(xri);
		input.setValue(value);
		// Act
		testee.beginBatch();
		testee.addCreateRequest(input);
		testee.endBatch();
		// Assert
		Assertions.assertNotNull(retrieveObject(xri));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void whenSegmentDoesExistThenItMayBeUpdatedLater() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		final MappedRecord originalValue = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		final String feature = "description";
		originalValue.put(feature, "original description");
		createObject(xri, originalValue);
		final MappedRecord value = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		final String newValue = "modified description";
		value.put(feature, newValue);
		final ObjectRecord input = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
		input.setResourceIdentifier(xri);
		input.setValue(value);
		// Act
		testee.beginBatch();
		testee.addUpdateRequest(input);
		testee.endBatch();
		// Assert
		Assertions.assertEquals(retrieveObject(xri).getValue().get(feature), newValue);
	}

	@Test
	public void whenSegmentDoesExistThenItMayBeDeletedLater() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		final MappedRecord value = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		createObject(xri, value);
		// Act
		testee.beginBatch();
		testee.addRemoveRequest(xri);
		testee.endBatch();
		// Assert
		Assertions.assertNull(retrieveObject(xri));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenOrderIsUnspecifiedThenResultIsOrderedByResourceIdentifierLater() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment");
		final String feature = "description";
		final Path xri1 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/1");
		final MappedRecord value1 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value1.put(feature, "101");
		createObject(xri1, value1);
		final Path xri2 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/2");
		final MappedRecord value2 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value2.put(feature, "102");
		createObject(xri2, value2);
		final Path xri3 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/3");
		final MappedRecord value3 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value3.put(feature, "200");
		createObject(xri3, value3);
		// Act
		testee.beginBatch();
		final QueryRecord input = testee.newQueryRecordWithFilter(xri);
		final ConditionRecord condition = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			feature,
			ConditionType.IS_LIKE,
			"1.*"
		);
		input.getQueryFilter().getCondition().add(condition);
		input.setResourceIdentifier(xri);
		input.setQueryFilter(input.getQueryFilter());
		final ResultRecord resultRecord =testee.addFindRequest(input);
		testee.endBatch();
		// Assert
		Assertions.assertNotNull(resultRecord);
		Assertions.assertEquals(2, resultRecord.size());
		Assertions.assertEquals(xri1, ((ObjectRecord)resultRecord.get(0)).getResourceIdentifier());
		Assertions.assertEquals(xri2, ((ObjectRecord)resultRecord.get(1)).getResourceIdentifier());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void whenOrderIsSpecifiedThenResultIsOrderedByFeatureLater() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment");
		final String feature = "description";
		final Path xri1 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/1");
		final MappedRecord value1 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value1.put(feature, "101");
		createObject(xri1, value1);
		final Path xri2 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/2");
		final MappedRecord value2 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value2.put(feature, "102");
		createObject(xri2, value2);
		final Path xri3 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/3");
		final MappedRecord value3 = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		value3.put(feature, "200");
		createObject(xri3, value3);
		// Act
		testee.beginBatch();
		final QueryRecord input = testee.newQueryRecordWithFilter(xri);
		final ConditionRecord condition = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			feature,
			ConditionType.IS_LIKE,
			"1.*"
		);
		input.getQueryFilter().getCondition().add(condition);
		input.getQueryFilter().getOrderSpecifier().add(new FeatureOrderRecord(feature, SortOrder.DESCENDING));
		final ResultRecord resultRecord = testee.addFindRequest(input);
		testee.endBatch();
		// Assert
		Assertions.assertNotNull(resultRecord);
		Assertions.assertEquals(2, resultRecord.size());
		Assertions.assertEquals(xri2, ((ObjectRecord)resultRecord.get(0)).getResourceIdentifier());
		Assertions.assertEquals(xri1, ((ObjectRecord)resultRecord.get(1)).getResourceIdentifier());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenRetrievedFromExtentThenReturnNodesOfDifferentPreferencesLater(
	) throws ResourceException{
		// Arrange
		final Path xri0 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		final MappedRecord value0 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Segment");
		createObject(xri0, value0);
		final Path xri1 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/1");
		final MappedRecord value1 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Preferences");
		value1.put("type","system");
		createObject(xri1, value1);
		final Path xri10 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/1/node/0");
		final MappedRecord value10 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Node");
		value10.put("absolutePath","/");
		createObject(xri10, value10);
		final Path xri11 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/1/node/1");
		final MappedRecord value11 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Node");
		value11.put("absolutePath","/1");
		value11.put("name","1");
		createObject(xri11, value11);
		final Path xri2 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/2");
		final MappedRecord value2 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Preferences");
		createObject(xri2, value2);
		final Path xri20 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/2/node/0");
		final MappedRecord value20 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Node");
		value20.put("absolutePath","/");
		createObject(xri20, value20);
		final Path xri21 = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/2/node/1");
		final MappedRecord value21 = Records.getRecordFactory().createMappedRecord("org:openmdx:preferences2:Node");
		value21.put("absolutePath","/1");
		value21.put("name","1");
		createObject(xri21, value21);
		// Act
		testee.beginBatch();
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/extent");
		final QueryRecord input = testee.newQueryRecordWithFilter(xri);
		final ConditionRecord condition0 = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			SystemAttributes.OBJECT_INSTANCE_OF,
			ConditionType.IS_IN,
			"org:openmdx:preferences2:Node"
		);
		input.getQueryFilter().getCondition().add(condition0);
		final ConditionRecord condition1 = new ConditionRecord(
			Quantifier.THERE_EXISTS,
            SystemAttributes.OBJECT_IDENTITY,
			ConditionType.IS_LIKE,
            ExtentCollection.toIdentityPattern(
                new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/($...)")
            )
		);
		input.getQueryFilter().getCondition().add(condition1);
		final ConditionRecord condition2 = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			"name",
			ConditionType.IS_IN,
			"1"
		);
		input.getQueryFilter().getCondition().add(condition2);
		final ResultRecord resultRecord = testee.addFindRequest(input);
		testee.endBatch();
		// Assert
		Assertions.assertNotNull(resultRecord);
		Assertions.assertEquals(2, resultRecord.size());
		Assertions.assertEquals(xri11, ((ObjectRecord)resultRecord.get(0)).getResourceIdentifier());
		Assertions.assertEquals(xri21, ((ObjectRecord)resultRecord.get(1)).getResourceIdentifier());
	}
	
	private void createObject(
		final Path xri, 
		final MappedRecord value
	) throws ResourceException {
		final ObjectRecord input = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
		input.setResourceIdentifier(xri);
		input.setValue(value);
		helper.addCreateRequest(input);
	}

	private ObjectRecord retrieveObject(Path xri) throws ResourceException {
		return helper.addGetRequest(xri);
	}
	
	static class VolatileProvider extends InMemory_2 {

		VolatileProvider() {
			dropNamespace(NAMESPACE_ID);
			setNamespaceId(NAMESPACE_ID);
		}

		private static final String NAMESPACE_ID = DataproviderRequestProcessorTest.class.getSimpleName(); 
		
	}
	
}
