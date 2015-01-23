/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: In-Memory Layer Pug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
package org.openmdx.base.dataprovider.layer.persistence.none;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.ExtentCollection;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.ConditionRecord;
import org.openmdx.base.rest.spi.FeatureOrderRecord;

public class InMemory_2Test {

	private static final String NAMESPACE_ID = InMemory_2Test.class.getSimpleName(); 

	@Before
	public void setUp(){
		InMemory_2.dropNamespace(NAMESPACE_ID);
		testee = new InMemory_2();
		testee.setNamespaceId(NAMESPACE_ID);
	}

	private InMemory_2 testee;

	@Test
	public void whenAuthorityDoesNotExistThenCreateAndReturnItAsResultRecord() throws ResourceException{
		// Arrange
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).GET;
		final Path xri = new Path("xri://@openmdx*test.InMemory_2");
		final QueryRecord input = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
		input.setResourceIdentifier(xri);
		// Act
		final Interaction interaction = testee.getInteraction(null);
		final Record output = interaction.execute(interactionSpec, input);
		// Assert
		Assert.assertTrue(output instanceof ResultRecord);
		ResultRecord resultRecord = (ResultRecord) output;
		Assert.assertEquals(1, resultRecord.size());
		final Object object = resultRecord.get(0);
		Assert.assertTrue(object instanceof ObjectRecord);
		final ObjectRecord objectRecord = (ObjectRecord) object;
		Assert.assertEquals("org:openmdx:base:Authority", objectRecord.getValue().getRecordName());
		Assert.assertEquals(xri, objectRecord.getResourceIdentifier());
	}
	
	@Test
	public void whenAuthorityDoesNotExistThenCreateAndReturnItInResultRecord() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2");
		final QueryRecord input = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
		final ResultRecord output = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
		input.setResourceIdentifier(xri);
		// Act
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).GET;
		final Interaction interaction = testee.getInteraction(null);
		final boolean success = interaction.execute(interactionSpec, input, output);
		interaction.close();
		// Assert
		Assert.assertTrue(success);
		Assert.assertEquals(1, output.size());
		final Object object = output.get(0);
		Assert.assertTrue(object instanceof ObjectRecord);
		final ObjectRecord objectRecord = (ObjectRecord) object;
		Assert.assertEquals("org:openmdx:base:Authority", objectRecord.getValue().getRecordName());
		Assert.assertEquals(xri, objectRecord.getResourceIdentifier());
	}

	@Test
	public void whenProviderDoesNotExistThenCreateAndReturnItAsResultRecord() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0");
		final QueryRecord input = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
		input.setResourceIdentifier(xri);
		// Act
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).GET;
		final Interaction interaction = testee.getInteraction(null);
		final Record output = interaction.execute(interactionSpec, input);
		interaction.close();
		// Assert
		Assert.assertTrue(output instanceof ResultRecord);
		ResultRecord resultRecord = (ResultRecord) output;
		Assert.assertEquals(1, resultRecord.size());
		final Object object = resultRecord.get(0);
		Assert.assertTrue(object instanceof ObjectRecord);
		final ObjectRecord objectRecord = (ObjectRecord) object;
		Assert.assertEquals("org:openmdx:base:Provider", objectRecord.getValue().getRecordName());
		Assert.assertEquals(xri, objectRecord.getResourceIdentifier());
	}
	
	@Test
	public void whenProviderDoesNotExistThenCreateAndReturnItInResultRecord() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0");
		final QueryRecord input = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
		final ResultRecord output = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
		input.setResourceIdentifier(xri);
		// Act
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).GET;
		final Interaction interaction = testee.getInteraction(null);
		final boolean success = interaction.execute(interactionSpec, input, output);
		interaction.close();
		// Assert
		Assert.assertTrue(success);
		Assert.assertEquals(1, output.size());
		final Object object = output.get(0);
		Assert.assertTrue(object instanceof ObjectRecord);
		final ObjectRecord objectRecord = (ObjectRecord) object;
		Assert.assertEquals("org:openmdx:base:Provider", objectRecord.getValue().getRecordName());
		Assert.assertEquals(xri, objectRecord.getResourceIdentifier());
	}

	@Test
	public void whenSegmentDoesNotExistThenReturnNull() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		final QueryRecord input = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
		input.setResourceIdentifier(xri);
		// Act
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).GET;
		final Interaction interaction = testee.getInteraction(null);
		final Record output = interaction.execute(interactionSpec, input);
		interaction.close();
		// Assert
		Assert.assertNull(output);
	}
	
	@Test
	public void whenSegmentDoesNotExistThenReturnFalseAndLeaveResultRecordEmpty() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		final QueryRecord input = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
		input.setResourceIdentifier(xri);
		final ResultRecord output = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
		// Act
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).GET;
		final Interaction interaction = testee.getInteraction(null);
		final boolean success = interaction.execute(interactionSpec, input, output);
		interaction.close();
		// Assert
		Assert.assertFalse(success);
		Assert.assertTrue(output.isEmpty());
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
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).CREATE;
		final Interaction interaction = testee.getInteraction(null);
		final boolean success = interaction.execute(interactionSpec, input, null);
		interaction.close();
		// Assert
		Assert.assertTrue(success);
		Assert.assertNotNull(retrieveObject(xri));
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
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).UPDATE;
		final Interaction interaction = testee.getInteraction(null);
		final boolean success = interaction.execute(interactionSpec, input, null);
		interaction.close();
		// Assert
		Assert.assertTrue(success);
		Assert.assertEquals(retrieveObject(xri).getValue().get(feature), newValue);
	}

	@Test
	public void whenSegmentDoesExistThenItMayBeDeleted() throws ResourceException{
		// Arrange
		final Path xri = new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0");
		final MappedRecord value = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Segment");
		createObject(xri, value);
		// Act
		final ObjectRecord input = retrieveObject(xri);
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).DELETE;
		final Interaction interaction = testee.getInteraction(null);
		final boolean success = interaction.execute(interactionSpec, input, null);
		interaction.close();
		// Assert
		Assert.assertTrue(success);
		Assert.assertNull(retrieveObject(xri));
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
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).GET;
		final QueryFilterRecord filter = Records.getRecordFactory().createMappedRecord(QueryFilterRecord.class);
		final ConditionRecord condition = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			feature,
			ConditionType.IS_LIKE,
			"1.*"
		);
		filter.getCondition().add(condition);
		final QueryRecord input = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
		input.setResourceIdentifier(xri);
		input.setQueryFilter(filter);
		final Interaction interaction = testee.getInteraction(null);
		final ResultRecord resultRecord = (ResultRecord) interaction.execute(interactionSpec, input);
		interaction.close();
		// Assert
		Assert.assertNotNull(resultRecord);
		Assert.assertEquals(2, resultRecord.size());
		Assert.assertEquals(xri1, ((ObjectRecord)resultRecord.get(0)).getResourceIdentifier());
		Assert.assertEquals(xri2, ((ObjectRecord)resultRecord.get(1)).getResourceIdentifier());
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
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).GET;
		final QueryFilterRecord filter = Records.getRecordFactory().createMappedRecord(QueryFilterRecord.class);
		final ConditionRecord condition = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			feature,
			ConditionType.IS_LIKE,
			"1.*"
		);
		filter.getCondition().add(condition);
		filter.getOrderSpecifier().add(new FeatureOrderRecord(feature, SortOrder.DESCENDING));
		final QueryRecord input = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
		input.setResourceIdentifier(xri);
		input.setQueryFilter(filter);
		final Interaction interaction = testee.getInteraction(null);
		final ResultRecord resultRecord = (ResultRecord) interaction.execute(interactionSpec, input);
		interaction.close();
		// Assert
		Assert.assertNotNull(resultRecord);
		Assert.assertEquals(2, resultRecord.size());
		Assert.assertEquals(xri2, ((ObjectRecord)resultRecord.get(0)).getResourceIdentifier());
		Assert.assertEquals(xri1, ((ObjectRecord)resultRecord.get(1)).getResourceIdentifier());
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
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).GET;
		final QueryFilterRecord filter = Records.getRecordFactory().createMappedRecord(QueryFilterRecord.class);
		final ConditionRecord condition0 = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			SystemAttributes.OBJECT_INSTANCE_OF,
			ConditionType.IS_IN,
			"org:openmdx:preferences2:Node"
		);
		filter.getCondition().add(condition0);
		final ConditionRecord condition1 = new ConditionRecord(
			Quantifier.THERE_EXISTS,
            SystemAttributes.OBJECT_IDENTITY,
			ConditionType.IS_LIKE,
            ExtentCollection.toIdentityPattern(
                new Path("xri://@openmdx*test.InMemory_2/provider/0/segment/0/preferences/($...)")
            )
		);
		filter.getCondition().add(condition1);
		final ConditionRecord condition2 = new ConditionRecord(
			Quantifier.THERE_EXISTS,
			"name",
			ConditionType.IS_IN,
			"1"
		);
		filter.getCondition().add(condition2);
		final QueryRecord input = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
		input.setResourceIdentifier(xri);
		input.setQueryFilter(filter);
		final Interaction interaction = testee.getInteraction(null);
		final ResultRecord resultRecord = (ResultRecord) interaction.execute(interactionSpec, input);
		interaction.close();
		// Assert
		Assert.assertNotNull(resultRecord);
		Assert.assertEquals(2, resultRecord.size());
		Assert.assertEquals(xri11, ((ObjectRecord)resultRecord.get(0)).getResourceIdentifier());
		Assert.assertEquals(xri21, ((ObjectRecord)resultRecord.get(1)).getResourceIdentifier());
	}
	
	private void createObject(
		final Path xri, 
		final MappedRecord value
	) throws ResourceException {
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).CREATE;
		final ObjectRecord input = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
		input.setResourceIdentifier(xri);
		input.setValue(value);
		final Interaction interaction = testee.getInteraction(null);
		interaction.execute(interactionSpec, input);
		interaction.close();
	}

	private ObjectRecord retrieveObject(Path xri) throws ResourceException {
		final RestInteractionSpec interactionSpec = InteractionSpecs.getRestInteractionSpecs(false).GET;
		final QueryRecord input = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
		input.setResourceIdentifier(xri);
		final Interaction interaction = testee.getInteraction(null);
		ResultRecord resultRecord = (ResultRecord) interaction.execute(interactionSpec, input);
		interaction.close();
		final ObjectRecord objectRecord = resultRecord == null ? null : (ObjectRecord) resultRecord.get(0);
		return objectRecord;
	}

}
