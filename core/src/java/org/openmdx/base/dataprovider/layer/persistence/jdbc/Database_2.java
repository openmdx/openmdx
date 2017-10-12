/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Database_2
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2016, OMEX AG, Switzerland
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc;

import static org.openmdx.base.accessor.cci.SystemAttributes.OBJECT_CLASS;
import static org.openmdx.base.accessor.cci.SystemAttributes.OBJECT_INSTANCE_OF;
import static org.openmdx.base.naming.SpecialResourceIdentifiers.EXTENT_REFERENCES;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.jdo.FetchPlan;
import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.application.dataprovider.kernel.LateBindingDataSource;
import org.openmdx.application.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries;
import org.openmdx.application.dataprovider.spi.EmbeddedFlags;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.spi.LockAssertions;
import org.openmdx.base.accessor.spi.URIMarshaller;
import org.openmdx.base.aop1.Aspects;
import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes.BooleanMarshaller;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes.DurationMarshaller;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes.LargeObjectMarshaller;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes.SetLargeObjectMethod;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes.XMLGregorianCalendarMarshaller;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DBOSlicedWithIdAsKey;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DBOSlicedWithParentAndIdAsKey;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DbObject;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DbObjectConfiguration;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObject;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObject2;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObject2NonIndexed;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObject2NonIndexedParentRidOnly;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObject2ParentRidOnly;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObjectNonIndexed;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObjectNonIndexedParentRidOnly;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObjectParentRidOnly;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.DataTypes;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.FastResultSet;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.FetchSize;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.LikeFlavour;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.Persistency;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.URI_1Marshaller;
import org.openmdx.base.naming.XRISegment;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RequestRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.AbstractRestPort;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.text.conversion.SQLWildcards;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.kernel.url.protocol.XRI_1Protocols;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.cci2.RegularExpressionFlag;
import org.w3c.cci2.SparseArray;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;

/**
 * Database_2 implements a OO-to-Relational mapping and makes MappedRecords
 * persistent. Any JDBC-compliant data store can be used.
 * 
 * You can use the java.io.PrintStream.DriverManager.setLogStream() method to
 * log JDBC calls. This method sets the logging/tracing PrintStream used by
 * the DriverManager and all drivers.
 * Insert the following line at the location in your code where you want to
 * start logging JDBC calls: DriverManager.setLogStream(System.out);
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Database_2 extends AbstractRestPort implements Database_1_0, DataTypes {

    /**
     * Constructor 
     */
    public Database_2() {
        super();
    }

    /**
     * QueryExtension
     */
    static class QueryExtension {
        
        public QueryExtension(
            String id
        ) {
            this.id = id;
        }
        
        public String getId(
        ) {
            return this.id;
        }
        
        public String getClause(
        ) {
            return this.clause;
        }

        public void setClause(
            String clause
        ) {
            this.clause = clause;
        }
        
        public List<Object> getParams(
            String paramName
        ) {
            return this.parameters.get(paramName);
        }
        
        public void putParams(
            String paramName,
            List<Object> values
        ) {
            this.parameters.put(paramName, values);
        }

        private final String id;
        private String clause;
        private final Map<String,List<Object>> parameters = new HashMap<String,List<Object>>();
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.spi.OperationAwareLayer_1#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new RestInteraction(connection);
    }

    /**
     * Return true if type is instanceof org:openmdx:state2:BasicState.
     * 
     * @param model
     * @param type
     * @return
     * @throws ServiceException
     */
    protected boolean isStated(
        Model_1_0 model,
        Object type
    ) throws ServiceException{
        for(Object superType : model.getElement(type).objGetList("allSupertype")) {
            if("org:openmdx:state2:BasicState".equals(((Path)superType).getLastSegment().toClassicRepresentation())) return true;
        }
        return false;
    }

    /**
     * @return the getSIZE_SUFFIX
     */
    @Override
    public String getSizeSuffix(
    ) {
        return SIZE_SUFFIX;
    }

    /**
     * @return the OBJECT_OID
     */
    @Override
    public String getObjectOidColumnName(
    ) {
        return this.OBJECT_OID;
    }

    /**
     * @return the OBJECT_IDX
     */
    @Override
    public String getObjectIdxColumnName(
    ) {
        return this.OBJECT_IDX;
    }

    /**
     * @return the OBJECT_RID
     */
    @Override
    public String getObjectRidColumnName(
    ) {
        return this.OBJECT_RID;
    }

    //-----------------------------------------------------------------------
    // Configuration
    //-----------------------------------------------------------------------

    /**
     * Retrieve booleanType.
     *
     * @return Returns the booleanType.
     */
    public String getBooleanType(
    ) {
        return this.booleanType;
    }
    
    /**
     * Set booleanType.
     * 
     * @param booleanType The booleanType to set.
     */
    public void setBooleanType(
        String booleanType
    ) {
        this.booleanType = booleanType;
    }
    
    /**
     * Retrieve booleanFalse.
     *
     * @return Returns the booleanFalse.
     */
    public String getBooleanFalse() {
        return this.booleanFalse;
    }
    
    /**
     * Set booleanFalse.
     * 
     * @param booleanFalse The booleanFalse to set.
     */
    public void setBooleanFalse(String booleanFalse) {
        this.booleanFalse = booleanFalse;
    }
    
    /**
     * Retrieve booleanTrue.
     *
     * @return Returns the booleanTrue.
     */
    public String getBooleanTrue() {
        return this.booleanTrue;
    }
    
    /**
     * Set booleanTrue.
     * 
     * @param booleanTrue The booleanTrue to set.
     */
    public void setBooleanTrue(String booleanTrue) {
        this.booleanTrue = booleanTrue;
    }
    
    /**
     * Get boolean marshaller.
     * 
     * @return
     * @throws ServiceException
     */
    protected BooleanMarshaller getBooleanMarshaller(
    ) throws ServiceException {
        if(this.booleanMarshaller == null) {
            this.booleanMarshaller = BooleanMarshaller.newInstance(
                this.booleanFalse,
                this.booleanTrue,
                this
            );
        }
        return this.booleanMarshaller;
    }
    
    /**
     * Retrieve durationType.
     *
     * @return Returns the durationType.
     */
    public String getDurationType() {
        return this.durationType;
    }
    
    /**
     * Set durationType.
     * 
     * @param durationType The durationType to set.
     */
    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }
    
    /**
     * Get duration marshaller.
     * 
     * @return
     * @throws ServiceException
     */
    protected Marshaller getDurationMarshaller(
    ) throws ServiceException {
        if(this.durationMarshaller == null) {
            this.durationMarshaller = DurationMarshaller.newInstance(
                this.durationType
            );            
        }
        return this.durationMarshaller;
    }
    
    /**
     * Retrieve dateTimeType.
     *
     * @return Returns the dateTimeType.
     */
    public String getDateTimeType() {
        return this.dateTimeType;
    }
    
    /**
     * Set dateTimeType.
     * 
     * @param dateTimeType The dateTimeType to set.
     */
    public void setDateTimeType(String dateTimeType) {
        this.dateTimeType = dateTimeType;
    }

    /**
     * Retrieve dateType.
     *
     * @return Returns the dateType.
     */
    public String getDateType() {
        return this.dateType;
    }
    
    /**
     * Set dateType.
     * 
     * @param dateType The dateType to set.
     */
    public void setDateType(String dateType) {
        this.dateType = dateType;
    }
    
    /**
     * Retrieve timeType.
     *
     * @return Returns the timeType.
     */
    public String getTimeType() {
        return this.timeType;
    }
    
    /**
     * Set timeType.
     * 
     * @param timeType The timeType to set.
     */
    public void setTimeType(String timeType) {
        this.timeType = timeType;
    }

    /**
     * Retrieve dateTimeZone.
     *
     * @return Returns the dateTimeZone.
     */
    public String getDateTimeZone() {
        return this.dateTimeZone;
    }
    
    /**
     * Set dateTimeZone.
     * 
     * @param dateTimeZone The dateTimeZone to set.
     */
    public void setDateTimeZone(String dateTimeZone) {
        this.dateTimeZone = dateTimeZone;
    }
    
    /**
     * Retrieve dateTimeDaylightZone.
     *
     * @return Returns the dateTimeDaylightZone.
     */
    public String getDateTimeDaylightZone() {
        return this.dateTimeDaylightZone;
    }
    
    /**
     * Set dateTimeDaylightZone.
     * 
     * @param dateTimeDaylightZone The dateTimeDaylightZone to set.
     */
    public void setDateTimeDaylightZone(String dateTimeDaylightZone) {
        this.dateTimeDaylightZone = dateTimeDaylightZone;
    }
    
    /**
     * Retrieve dateTimePrecision.
     *
     * @return Returns the dateTimePrecision.
     */
    public String getDateTimePrecision() {
        return this.dateTimePrecision;
    }
    
    /**
     * Set dateTimePrecision.
     * 
     * @param dateTimePrecision The dateTimePrecision to set.
     */
    public void setDateTimePrecision(String dateTimePrecision) {
        this.dateTimePrecision = dateTimePrecision;
    }
    
    /**
     * Get calendar marshaller.
     * 
     * @return
     * @throws ServiceException
     */
    protected XMLGregorianCalendarMarshaller getCalendarMarshaller(
    ) throws ServiceException {
        if(this.calendarMarshaller == null) {
            this.calendarMarshaller = XMLGregorianCalendarMarshaller.newInstance(
                this.timeType,
                this.dateType,
                this.dateTimeType,         
                this.dateTimeZone,
                this.dateTimeDaylightZone == null ? dateTimeZone : this.dateTimeDaylightZone,
                this.dateTimePrecision, 
                this
            );            
        }
        return this.calendarMarshaller;
    }
    
    /**
     * Retrieve objectIdAttributesSuffix.
     *
     * @return Returns the objectIdAttributesSuffix.
     */
    public String getObjectIdAttributesSuffix() {
        return this.objectIdAttributesSuffix;
    }
    
    /**
     * Set objectIdAttributesSuffix.
     * 
     * @param objectIdAttributesSuffix The objectIdAttributesSuffix to set.
     */
    public void setObjectIdAttributesSuffix(
        String objectIdAttributesSuffix
    ) {
        this.objectIdAttributesSuffix = objectIdAttributesSuffix;
        this.OBJECT_OID = this.toOid("object");
    }

    /**
     * Retrieve referenceIdAttributesSuffix.
     *
     * @return Returns the referenceIdAttributesSuffix.
     */
    public String getReferenceIdAttributesSuffix() {
        return this.referenceIdAttributesSuffix;
    }
    
    /**
     * Set referenceIdAttributesSuffix.
     * 
     * @param referenceIdAttributesSuffix The referenceIdAttributesSuffix to set.
     */
    public void setReferenceIdAttributesSuffix(
        String referenceIdAttributesSuffix
    ) {
        this.referenceIdAttributesSuffix = referenceIdAttributesSuffix;
        this.OBJECT_RID = this.toRid("object");
    }

    /**
     * Retrieve referenceIdSuffixAttributesSuffix.
     *
     * @return Returns the referenceIdSuffixAttributesSuffix.
     */
    public String getReferenceIdSuffixAttributesSuffix() {
        return this.referenceIdSuffixAttributesSuffix;
    }
    
    /**
     * Set referenceIdSuffixAttributesSuffix.
     * 
     * @param referenceIdSuffixAttributesSuffix The referenceIdSuffixAttributesSuffix to set.
     */
    public void setReferenceIdSuffixAttributesSuffix(
        String referenceIdSuffixAttributesSuffix) {
        this.referenceIdSuffixAttributesSuffix = referenceIdSuffixAttributesSuffix;
    }

    public String getPrivateAttributesPrefix(
    ) {
        return this.privateAttributesPrefix;
    }
    
    /**
     * Set privateAttributesPrefix.
     * 
     * @param privateAttributesPrefix The privateAttributesPrefix to set.
     */
    public void setPrivateAttributesPrefix(String privateAttributesPrefix) {
        this.privateAttributesPrefix = privateAttributesPrefix.toLowerCase();
    }
    
    /**
     * Retrieve objectIdxColumn.
     *
     * @return Returns the objectIdxColumn.
     */
    public String getObjectIdxColumn() {
        return this.OBJECT_IDX;
    }

    /**
     * Set objectIdxColumn.
     * 
     * @param objectIdxColumn The objectIdxColumn to set.
     */
    public void setObjectIdxColumn(String objectIdxColumn) {
        this.OBJECT_IDX = objectIdxColumn;
    }

    /**
     * Retrieve resultSetType.
     *
     * @return Returns the resultSetType.
     */
    public String getResultSetType() {
        if(this.resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
            return LayerConfigurationEntries.RESULT_SET_TYPE_FORWARD_ONLY;
        } else if(this.resultSetType == ResultSet.TYPE_SCROLL_INSENSITIVE) {
            return LayerConfigurationEntries.RESULT_SET_TYPE_SCROLL_INSENSITIVE;
        } else if(resultSetType ==  ResultSet.TYPE_SCROLL_SENSITIVE) {
            return LayerConfigurationEntries.RESULT_SET_TYPE_SCROLL_SENSITIVE;
        } else {
            return null;
        }
    }

    /**
     * Set resultSetType.
     * 
     * @param resultSetType The resultSetType to set.
     */
    public void setResultSetType(String resultSetType) {
        if(LayerConfigurationEntries.RESULT_SET_TYPE_FORWARD_ONLY.equals(resultSetType)) {
            this.resultSetType = ResultSet.TYPE_FORWARD_ONLY;
        } else if(LayerConfigurationEntries.RESULT_SET_TYPE_SCROLL_INSENSITIVE.equals(resultSetType)) {
            this.resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        } else if(LayerConfigurationEntries.RESULT_SET_TYPE_SCROLL_SENSITIVE.equals(resultSetType)) {
            this.resultSetType = ResultSet.TYPE_SCROLL_SENSITIVE;
        }
    }
    
    /**
     * Retrieve allowsSqlSequenceFallback.
     *
     * @return Returns the allowsSqlSequenceFallback.
     */
    public boolean isAllowsSqlSequenceFallback() {
        return this.allowsSqlSequenceFallback;
    }
    
    /**
     * Set allowsSqlSequenceFallback.
     * 
     * @param allowsSqlSequenceFallback The allowsSqlSequenceFallback to set.
     */
    public void setAllowsSqlSequenceFallback(boolean allowsSqlSequenceFallback) {
        this.allowsSqlSequenceFallback = allowsSqlSequenceFallback;
    }
    
    /**
     * Retrieve ignoreCheckForDuplicates.
     *
     * @return Returns the ignoreCheckForDuplicates.
     */
    public boolean isIgnoreCheckForDuplicates() {
        return this.ignoreCheckForDuplicates;
    }
    
    /**
     * Set ignoreCheckForDuplicates.
     * 
     * @param ignoreCheckForDuplicates The ignoreCheckForDuplicates to set.
     */
    public void setIgnoreCheckForDuplicates(boolean ignoreCheckForDuplicates) {
        this.ignoreCheckForDuplicates = ignoreCheckForDuplicates;
    }
    
    /**
     * Retrieve singleValueAttributes.
     *
     * @return Returns the singleValueAttributes.
     */
    public SparseArray<String> getSingleValueAttribute(
    ) {
        return new TreeSparseArray<String>(this.singleValueAttributes);
    }

    /**
     * Set singleValueAttributes.
     * 
     * @param singleValueAttributes The singleValueAttributes to set.
     */
    public void setSingleValueAttribute(
        SparseArray<String> singleValueAttributes
    ) {
        this.singleValueAttributes.addAll(singleValueAttributes.values());
    }

    /**
     * Retrieve embeddedFeature.
     *
     * @return Returns the embeddedFeature.
     */
    public SparseArray<String> getEmbeddedFeature(
    ) {
        SparseArray<String> embeddedFeatures = new TreeSparseArray<String>();
        int index = 0;
        for(Map.Entry<String,Integer> embeddedFeature: this.embeddedFeatures.entrySet()) {
            embeddedFeatures.put(Integer.valueOf(index), embeddedFeature.getKey() + "[" + embeddedFeature.getValue() + "]");
            index++;
        }
        return embeddedFeatures;
    }

    /**
     * Set embeddedFeature.
     * 
     * @param embeddedFeature The embeddedFeature to set.
     */
    public void setEmbeddedFeature(
        SparseArray<String> embeddedFeatures
    ) {
        for(String embeddedFeature : embeddedFeatures) {
            int p = embeddedFeature.indexOf("[");
            if(p > 0) {
                // Store as (feature name, upper bound)
                this.embeddedFeatures.put(
                    embeddedFeature.substring(0, p),
                    Integer.valueOf(embeddedFeature.substring(p+1, embeddedFeature.indexOf("]")))
                );
            }
        }
    }

    /**
     * Retrieve nonPersistentFeature.
     *
     * @return Returns the nonPersistentFeature.
     */
    public SparseArray<String> getNonPersistentFeature(
    ) {
        return new TreeSparseArray<String>(this.nonPersistentFeatures);
    }

    /**
     * Set nonPersistentFeature.
     * 
     * @param nonPersistentFeature The nonPersistentFeature to set.
     */
    public void setNonPersistentFeature(
        SparseArray<String> nonPersistentFeature
    ) {
        this.nonPersistentFeatures.addAll(nonPersistentFeature.values());
    }

    /**
     * Retrieve nullAsCharacter.
     *
     * @return Returns the nullAsCharacter.
     */
    public String getNullAsCharacter() {
        return this.nullAsCharacter;
    }

    /**
     * Set nullAsCharacter.
     * 
     * @param nullAsCharacter The nullAsCharacter to set.
     */
    public void setNullAsCharacter(String nullAsCharacter) {
        this.nullAsCharacter = nullAsCharacter;
    }
    
    /**
     * Retrieve fetchSize.
     *
     * @return Returns the fetchSize.
     */
    public int getFetchSize() {
        return this.fetchSizeDefault;
    }
    
    /**
     * Set fetchSize.
     * 
     * @param fetchSize The fetchSize to set.
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSizeDefault = fetchSize;
    }
    
    /**
     * Retrieve fetchSizeOptimal.
     *
     * @return Returns the fetchSizeOptimal.
     */
    public int getFetchSizeOptimal() {
        return this.fetchSizeOptimal;
    }

    /**
     * Set fetchSizeOptimal.
     * 
     * @param fetchSizeOptimal The fetchSizeOptimal to set.
     */
    public void setFetchSizeOptimal(int fetchSizeOptimal) {
        this.fetchSizeOptimal = fetchSizeOptimal;
    }
    
    /**
     * Retrieve fetchSizeGreedy.
     *
     * @return Returns the fetchSizeGreedy.
     */
    public int getFetchSizeGreedy() {
        return this.fetchSizeGreedy;
    }
    
    /**
     * Set fetchSizeGreedy.
     * 
     * @param fetchSizeGreedy The fetchSizeGreedy to set.
     */
    public void setFetchSizeGreedy(int fetchSizeGreedy) {
        this.fetchSizeGreedy = fetchSizeGreedy;
    }
    
    /**
     * Get fetch size.
     * 
     * @return
     */
    protected FetchSize getFetchSizeT(
    ) {
        if(this.fetchSizeT == null) {
            this.fetchSizeT = new FetchSize( 
                this.fetchSizeDefault,
                this.fetchSizeOptimal,
                this.fetchSizeGreedy
            );            
        }
        return this.fetchSizeT;
    }
    
    /**
     * Retrieve referenceLookupStatementHint.
     *
     * @return Returns the referenceLookupStatementHint.
     */
    public String getReferenceLookupStatementHint() {
        return this.referenceLookupStatementHint;
    }
    
    /**
     * Set referenceLookupStatementHint.
     * 
     * @param referenceLookupStatementHint The referenceLookupStatementHint to set.
     */
    public void setReferenceLookupStatementHint(String referenceLookupStatementHint) {
        this.referenceLookupStatementHint = referenceLookupStatementHint;
    }

    /**
     * Get referenceIdFormat.
     * 
     * @return
     */
    @Override
    public String getReferenceIdFormat(
    ) {
        return this.referenceIdFormat;
    }
    
    /**
     * Set referenceIdFormat.
     * 
     * @param referenceIdFormat The referenceIdFormat to set.
     */
    public void setReferenceIdFormat(String referenceIdFormat) {
        this.referenceIdFormat = referenceIdFormat;
    }
    
    /**
     * Retrieve useNormalizedReferences.
     *
     * @return Returns the useNormalizedReferences.
     */
    @Override
    public boolean isUseNormalizedReferences() {
        return this.useNormalizedReferences;
    }

    /**
     * Set useNormalizedReferences.
     * 
     * @param useNormalizedReferences The useNormalizedReferences to set.
     */
    public void setUseNormalizedReferences(boolean useNormalizedReferences) {
        this.useNormalizedReferences = useNormalizedReferences;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0#isSetSizeColumns()
     */
    @Override
    public boolean isSetSizeColumns(
    ) {
        return this.setSizeColumns;
    }

    /**
     * Set setSizeColumns.
     * 
     * @param setSizeColumns The setSizeColumns to set.
     */
    public void setSetSizeColumns(boolean setSizeColumns) {
        this.setSizeColumns = setSizeColumns;
    }
    
    /**
     * Retrieve pathMacroName.
     *
     * @return Returns the pathMacroName.
     */
    public SparseArray<String> getPathMacroName() {
        return this.pathMacroName;
    }

    /**
     * Set pathMacroName.
     * 
     * @param pathMacroName The pathMacroName to set.
     */
    public void setPathMacroName(SparseArray<String> pathMacroName) {
        this.pathMacroName = pathMacroName;
    }
    
    /**
     * Retrieve pathMacroValue.
     *
     * @return Returns the pathMacroValue.
     */
    public SparseArray<String> getPathMacroValue() {
        return this.pathMacroValue;
    }

    /**
     * Set pathMacroValue.
     * 
     * @param pathMacroValue The pathMacroValue to set.
     */
    public void setPathMacroValue(SparseArray<String> pathMacroValue) {
        this.pathMacroValue = pathMacroValue;
    }
    
    /**
     * Get path macros.
     */
    protected Map<String,String> getPathMacros(
    ) {
        if(this.pathMacros == null) {
            this.pathMacros = new HashMap<String,String>();
            for(
                ListIterator<String> pathMacroNameIterator = this.pathMacroName.populationIterator();
                pathMacroNameIterator.hasNext();
            ) {
                Integer i = Integer.valueOf(pathMacroNameIterator.nextIndex());
                String pathMacroName = pathMacroNameIterator.next();
                this.pathMacros.put(
                    pathMacroName,
                    this.pathMacroValue.get(i)
                );
            }
        }
        return this.pathMacros;
    }
    
    /**
     * Retrieve stringMacroColumn.
     *
     * @return Returns the stringMacroColumn.
     */
    public SparseArray<String> getStringMacroColumn() {
        return this.stringMacroColumn;
    }
    
    /**
     * Set stringMacroColumn.
     * 
     * @param stringMacroColumn The stringMacroColumn to set.
     */
    public void setStringMacroColumn(SparseArray<String> stringMacroColumn) {
        this.stringMacroColumn = stringMacroColumn;
    }
    
    /**
     * Retrieve stringMacroName.
     *
     * @return Returns the stringMacroName.
     */
    public SparseArray<String> getStringMacroName() {
        return this.stringMacroName;
    }
    
    /**
     * Set stringMacroName.
     * 
     * @param stringMacroName The stringMacroName to set.
     */
    public void setStringMacroName(SparseArray<String> stringMacroName) {
        this.stringMacroName = stringMacroName;
    }

    /**
     * Retrieve stringMacroValue.
     *
     * @return Returns the stringMacroValue.
     */
    public SparseArray<String> getStringMacroValue() {
        return this.stringMacroValue;
    }
    
    /**
     * Set stringMacroValue.
     * 
     * @param stringMacroValue The stringMacroValue to set.
     */
    public void setStringMacroValue(SparseArray<String> stringMacroValue) {
        this.stringMacroValue = stringMacroValue;
    }

    /**
     * Get string macros.
     * 
     * @return
     */
    protected Map<String,List<String[]>> getStringMacros(
    ) {
        if(this.stringMacros == null) {
            this.stringMacros = new HashMap<String,List<String[]>>();
            for(
                ListIterator<String> stringMacroColumnIterator = this.stringMacroColumn.populationIterator();
                stringMacroColumnIterator.hasNext();
            ) {
                Integer i = Integer.valueOf(stringMacroColumnIterator.nextIndex());
                String stringMacroColumn = stringMacroColumnIterator.next(); 
                List<String[]> entries = this.stringMacros.get(stringMacroColumn); 
                if(entries == null) {
                    this.stringMacros.put(
                        stringMacroColumn,
                        entries = new ArrayList<String[]>()
                    );
                }
                entries.add(
                    new String[]{
                        this.stringMacroName.get(i),
                        this.stringMacroValue.get(i)
                    }
                );
            }
        }
        return this.stringMacros;
    }
    
    /**
     * Retrieve maxReferenceComponents.
     *
     * @return Returns the maxReferenceComponents.
     */
    public int getMaxReferenceComponents() {
        return this.maxReferenceComponents;
    }
    
    /**
     * Set maxReferenceComponents.
     * 
     * @param maxReferenceComponents The maxReferenceComponents to set.
     */
    public void setMaxReferenceComponents(int maxReferenceComponents) {
        this.maxReferenceComponents = maxReferenceComponents;
    }

    /**
     * Retrieve dataSource.
     *
     * @return Returns the dataSource.
     */
    public SparseArray<DataSource> getDataSource() {
        return this.dataSource;
    }

    /**
     * Set dataSource.
     * 
     * @param dataSource The dataSource to set.
     */
    public void setDataSource(SparseArray<DataSource> dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Retrieve datasourceName.
     *
     * @return Returns the datasourceName.
     */
    public SparseArray<String> getDatasourceName() {
        return this.datasourceName;
    }

    /**
     * Set datasourceName.
     * 
     * @param datasourceName The datasourceName to set.
     */
    public void setDatasourceName(
        SparseArray<String> datasourceName
    ) {
        this.datasourceName = datasourceName;
    }

    /**
     * Get request-specific data source.
     * 
     * @param request
     * @return
     * @throws ServiceException
     */
    protected DataSource getDataSource(
        RestInteractionSpec ispec,
        RequestRecord request
    ) throws ServiceException {
        if(this.dataSource == null) {
            List<DataSource> dataSources = new ArrayList<DataSource>();
            if(this.datasourceName != null) {
                for(String jndiName: this.datasourceName) {
                    dataSources.add(new LateBindingDataSource(jndiName));
                }
            }
            this.dataSource = new TreeSparseArray<DataSource>(dataSources);
        }
        if(this.dataSource.isEmpty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION, 
                "can not get connection manager"
            );
        }
        return this.dataSource.get(Integer.valueOf(0));
    }
    
    /**
     * Retrieve jdbcDriverSqlProperties.
     *
     * @return Returns the jdbcDriverSqlProperties.
     */
    public Properties getJdbcDriverSqlProperties(
    ) throws ServiceException {
        if(this.jdbcDriverSqlProperties == null) {
            // driver properties
            URL propertiesUrl = Resources.getResource(JDBC_DRIVER_SQL_PROPERTIES);
            if(propertiesUrl == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION, 
                    "Unable to find the JDBC driver properties",
                    new BasicException.Parameter("resource", JDBC_DRIVER_SQL_PROPERTIES)
                );
            } else try {
                this.jdbcDriverSqlProperties = new Properties();
                this.jdbcDriverSqlProperties.load(
                    propertiesUrl.openStream()
                );
            }  catch (IOException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION, 
                    "Unable to load the JDBC driver properties",
                    new BasicException.Parameter("resource", JDBC_DRIVER_SQL_PROPERTIES)
                );
            }            
        }
        return this.jdbcDriverSqlProperties;
    }
    
    /**
     * Retrieve disableStateFilterSubstitution.
     *
     * @return Returns the disableStateFilterSubstitution.
     */
    public boolean isDisableStateFilterSubstitution(
    ) {
        return !this.enableAspectFilterSubstitution;
    }
    
    /**
     * Set disableStateFilterSubstitution.
     * 
     * @param disableStateFilterSubstitution The disableStateFilterSubstitution to set.
     */
    public void setDisableStateFilterSubstitution(
        boolean disableStateFilterSubstitution
    ) {
        this.enableAspectFilterSubstitution = !disableStateFilterSubstitution;
    }

    /**
     * @param namespaceId the namespaceId to set
     */
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
        
    /**
     * Retrieve namespaceId.
     *
     * @return Returns the namespaceId.
     */
    public String getNamespaceId() {
        return this.namespaceId;
    }
    
    /**
     * Retrieve normalizeObjectIds.
     *
     * @return Returns the normalizeObjectIds.
     */
    @Override
    public boolean isNormalizeObjectIds() {
        return this.normalizeObjectIds;
    }
    
    /**
     * Set normalizeObjectIds.
     * 
     * @param normalizeObjectIds The normalizeObjectIds to set.
     */
    public void setNormalizeObjectIds(boolean normalizeObjectIds) {
        this.normalizeObjectIds = normalizeObjectIds;
    }
    
    /**
     * Retrieve useViewsForRedundantColumns.
     *
     * @return Returns the useViewsForRedundantColumns.
     */
    @Override
    public boolean isUseViewsForRedundantColumns() {
        return this.useViewsForRedundantColumns;
    }
    
    /**
     * Set useViewsForRedundantColumns.
     * 
     * @param useViewsForRedundantColumns The useViewsForRedundantColumns to set.
     */
    public void setUseViewsForRedundantColumns(boolean useViewsForRedundantColumns) {
        this.useViewsForRedundantColumns = useViewsForRedundantColumns;
    }
    
    /**
     * Retrieve usePreferencesTable.
     *
     * @return Returns the usePreferencesTable.
     */
    public boolean isUsePreferencesTable() {
        return this.usePreferencesTable;
    }
    
    /**
     * Set usePreferencesTable.
     * 
     * @param usePreferencesTable The usePreferencesTable to set.
     */
    public void setUsePreferencesTable(boolean usePreferencesTable) {
        this.usePreferencesTable = usePreferencesTable;
    }
    
    /**
     * Retrieve cascadeDeletes.
     *
     * @return Returns the cascadeDeletes.
     */
    @Override
    public boolean isCascadeDeletes() {
        return this.cascadeDeletes;
    }
    
    /**
     * Set cascadeDeletes.
     * 
     * @param cascadeDeletes The cascadeDeletes to set.
     */
    public void setCascadeDeletes(boolean cascadeDeletes) {
        this.cascadeDeletes = cascadeDeletes;
    }
    
    
    /**
     * Get orderNullsAsEmpty.
     * 
     * @return
     */
    public boolean isOrderNullsAsEmpty() {
        return this.orderNullsAsEmpty;
    }

    /**
     * Set orderNullsAsEmpty.
     * 
     * @return
     */
    public void setOrderNullsAsEmpty(boolean orderNullsAsEmpty) {
        this.orderNullsAsEmpty = orderNullsAsEmpty;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0#getDatabaseConfiguration()
     */
    @Override
    public DatabaseConfiguration getDatabaseConfiguration(
    ) throws ServiceException {
        if(this.databaseConfiguration == null) {
            this.databaseConfiguration = new DatabaseConfiguration(this);
        }
        return this.databaseConfiguration;
    }

    /**
     * Retrieve streamBufferDirectory.
     *
     * @return Returns the streamBufferDirectory.
     * 
     * @deprecated("For JRE 5/setStreamByValue support only")
     */
    protected String getStreamBufferDirectory(
    ) {
        return this.streamBufferDirectory == null ? null : this.streamBufferDirectory.toString();
    }

    /**
     * Set streamBufferDirectory.
     * 
     * @param streamBufferDirectory The streamBufferDirectory to set.
     */
    public void setStreamBufferDirectory(
        String streamBufferDirectory
    ) {
        this.streamBufferDirectory = new File(streamBufferDirectory);
    }
    
    /**
     * Retrieve columnNameFrom.
     *
     * @return Returns the columnNameFrom.
     */
    @Override
    public SparseArray<String> getColumnNameFrom() {
        return this.columnNameFrom;
    }

    /**
     * Set columnNameFrom.
     * 
     * @param columnNameFrom The columnNameFrom to set.
     */
    public void setColumnNameFrom(SparseArray<String> columnNameFrom) {
        this.columnNameFrom = columnNameFrom;
    }
    
    /**
     * Retrieve columnNameTo.
     *
     * @return Returns the columnNameTo.
     */
    @Override
    public SparseArray<String> getColumnNameTo() {
        return this.columnNameTo;
    }
    
    /**
     * Set columnNameTo.
     * 
     * @param columnNameTo The columnNameTo to set.
     */
    public void setColumnNameTo(SparseArray<String> columnNameTo) {
        this.columnNameTo = columnNameTo;
    }
    
    /**
     * Retrieve type.
     *
     * @return Returns the type.
     */
    @Override
    public SparseArray<Path> getType() {
        return this.type;
    }

    /**
     * Set type.
     * 
     * @param type The type to set.
     */
    public void setType(
        SparseArray<Path> type
    ) {
        this.type = type;
    }

    /**
     * Retrieve typeName.
     *
     * @return Returns the typeName.
     */
    @Override
    public SparseArray<String> getTypeName() {
        return this.typeName;
    }
    
    /**
     * Set typeName.
     * 
     * @param typeName The typeName to set.
     */
    public void setTypeName(SparseArray<String> typeName) {
        this.typeName = typeName;
    }
    
    /**
     * Retrieve dbObject.
     *
     * @return Returns the dbObject.
     */
    @Override
    public SparseArray<String> getDbObject() {
        return this.dbObject;
    }
    
    /**
     * Set dbObject.
     * 
     * @param dbObject The dbObject to set.
     */
    public void setDbObject(SparseArray<String> dbObject) {
        this.dbObject = dbObject;
    }
    
    /**
     * Retrieve dbObject2.
     *
     * @return Returns the dbObject2.
     */
    @Override
    public SparseArray<String> getDbObject2() {
        return this.dbObject2;
    }
    
    /**
     * Set dbObject2.
     * 
     * @param dbObject2 The dbObject2 to set.
     */
    public void setDbObject2(SparseArray<String> dbObject2) {
        this.dbObject2 = dbObject2;
    }
    
    /**
     * Retrieve dbObjectFormat.
     *
     * @return Returns the dbObjectFormat.
     */
    @Override
    public SparseArray<String> getDbObjectFormat() {
        return this.dbObjectFormat;
    }
    
    /**
     * Set dbObjectFormat.
     * 
     * @param dbObjectFormat The dbObjectFormat to set.
     */
    public void setDbObjectFormat(SparseArray<String> dbObjectFormat) {
        this.dbObjectFormat = dbObjectFormat;
    }

    /**
     * Retrieve pathNormalizeLevel.
     *
     * @return Returns the pathNormalizeLevel.
     */
    @Override
    public SparseArray<Integer> getPathNormalizeLevel() {
        return this.pathNormalizeLevel;
    }

    /**
     * Set pathNormalizeLevel.
     * 
     * @param pathNormalizeLevel The pathNormalizeLevel to set.
     */
    public void setPathNormalizeLevel(
        SparseArray<Integer> pathNormalizeLevel
    ) {
        this.pathNormalizeLevel = pathNormalizeLevel;
    }
    
    /**
     * Retrieve dbObjectForQuery.
     *
     * @return Returns the dbObjectForQuery.
     */
    @Override
    public SparseArray<String> getDbObjectForQuery() {
        return this.dbObjectForQuery;
    }
    
    /**
     * Set dbObjectForQuery.
     * 
     * @param dbObjectForQuery The dbObjectForQuery to set.
     */
    public void setDbObjectForQuery(SparseArray<String> dbObjectForQuery) {
        this.dbObjectForQuery = dbObjectForQuery;
    }

    /**
     * Retrieve dbObjectForQuery2.
     *
     * @return Returns the dbObjectForQuery2.
     */
    @Override
    public SparseArray<String> getDbObjectForQuery2() {
        return this.dbObjectForQuery2;
    }
    
    /**
     * Set dbObjectForQuery2.
     * 
     * @param dbObjectForQuery2 The dbObjectForQuery2 to set.
     */
    public void setDbObjectForQuery2(SparseArray<String> dbObjectForQuery2) {
        this.dbObjectForQuery2 = dbObjectForQuery2;
    }
    
    /**
     * Retrieve dbObjectsForQueryJoinColumn.
     *
     * @return Returns the dbObjectsForQueryJoinColumn.
     */
    @Override
    public SparseArray<String> getDbObjectsForQueryJoinColumn() {
        return this.dbObjectsForQueryJoinColumn;
    }

    /**
     * Set dbObjectsForQueryJoinColumn.
     * 
     * @param dbObjectsForQueryJoinColumn The dbObjectsForQueryJoinColumn to set.
     */
    public void setDbObjectsForQueryJoinColumn(
        SparseArray<String> dbObjectsForQueryJoinColumn
    ) {
        this.dbObjectsForQueryJoinColumn = dbObjectsForQueryJoinColumn;
    }
    
    /**
     * Retrieve dbObjectHint.
     *
     * @return Returns the dbObjectHint.
     */
    @Override
    public SparseArray<String> getDbObjectHint() {
        return this.dbObjectHint;
    }
    
    /**
     * Set dbObjectHint.
     * 
     * @param dbObjectHint The dbObjectHint to set.
     */
    public void setDbObjectHint(SparseArray<String> dbObjectHint) {
        this.dbObjectHint = dbObjectHint;
    }
    
    /**
     * Retrieve objectIdPattern.
     *
     * @return Returns the objectIdPattern.
     */
    @Override
    public SparseArray<String> getObjectIdPattern() {
        return this.objectIdPattern;
    }
    
    /**
     * Set objectIdPattern.
     * 
     * @param objectIdPattern The objectIdPattern to set.
     */
    public void setObjectIdPattern(SparseArray<String> objectIdPattern) {
        this.objectIdPattern = objectIdPattern;
    }
    
    /**
     * Retrieve joinTable.
     *
     * @return Returns the joinTable.
     */
    @Override
    public SparseArray<String> getJoinTable() {
        return this.joinTable;
    }
    
    /**
     * Set joinTable.
     * 
     * @param joinTable The joinTable to set.
     */
    public void setJoinTable(SparseArray<String> joinTable) {
        this.joinTable = joinTable;
    }
    
    /**
     * Retrieve joinColumnEnd1.
     *
     * @return Returns the joinColumnEnd1.
     */
    @Override
    public SparseArray<String> getJoinColumnEnd1() {
        return this.joinColumnEnd1;
    }
    
    /**
     * Set joinColumnEnd1.
     * 
     * @param joinColumnEnd1 The joinColumnEnd1 to set.
     */
    public void setJoinColumnEnd1(SparseArray<String> joinColumnEnd1) {
        this.joinColumnEnd1 = joinColumnEnd1;
    }
    
    /**
     * Retrieve joinColumnEnd2.
     *
     * @return Returns the joinColumnEnd2.
     */
    @Override
    public SparseArray<String> getJoinColumnEnd2() {
        return this.joinColumnEnd2;
    }

    /**
     * Set joinColumnEnd2.
     * 
     * @param joinColumnEnd2 The joinColumnEnd2 to set.
     */
    public void setJoinColumnEnd2(SparseArray<String> joinColumnEnd2) {
        this.joinColumnEnd2 = joinColumnEnd2;
    }
    
    /**
     * Retrieve unitOfWorkProvider.
     *
     * @return Returns the unitOfWorkProvider.
     */
    @Override
    public SparseArray<String> getUnitOfWorkProvider() {
        return this.unitOfWorkProvider;
    }
    
    /**
     * Set unitOfWorkProvider.
     * 
     * @param unitOfWorkProvider The unitOfWorkProvider to set.
     */
    public void setUnitOfWorkProvider(SparseArray<String> unitOfWorkProvider) {
        this.unitOfWorkProvider = unitOfWorkProvider;
    }
    
    /**
     * Retrieve removableReferenceIdPrefix.
     *
     * @return Returns the removableReferenceIdPrefix.
     */
    @Override    
    public SparseArray<String> getRemovableReferenceIdPrefix() {
        return this.removableReferenceIdPrefix;
    }
    
    /**
     * Set removableReferenceIdPrefix.
     * 
     * @param removableReferenceIdPrefix The removableReferenceIdPrefix to set.
     */
    public void setRemovableReferenceIdPrefix(
        SparseArray<String> removableReferenceIdPrefix
    ) {
        this.removableReferenceIdPrefix = removableReferenceIdPrefix;
    }
    
    /**
     * Retrieve disableAbsolutePositioning.
     *
     * @return Returns the disableAbsolutePositioning.
     */
    @Override
    public SparseArray<Boolean> getDisableAbsolutePositioning() {
        return this.disableAbsolutePositioning;
    }
    
    /**
     * Set disableAbsolutePositioning.
     * 
     * @param disableAbsolutePositioning The disableAbsolutePositioning to set.
     */
    public void setDisableAbsolutePositioning(
        SparseArray<Boolean> disableAbsolutePositioning
    ) {
        this.disableAbsolutePositioning = disableAbsolutePositioning;
    }

    /**
     * Retrieve referenceIdPattern.
     *
     * @return Returns the referenceIdPattern.
     */
    @Override
    public SparseArray<String> getReferenceIdPattern() {
        return this.referenceIdPattern;
    }
    
    /**
     * Set referenceIdPattern.
     * 
     * @param referenceIdPattern The referenceIdPattern to set.
     */
    public void setReferenceIdPattern(SparseArray<String> referenceIdPattern) {
        this.referenceIdPattern = referenceIdPattern;
    }
    
    /**
     * Retrieve autonumColumn.
     *
     * @return Returns the autonumColumn.
     */
    @Override
    public SparseArray<String> getAutonumColumn() {
        return this.autonumColumn;
    }
    
    /**
     * Set autonumColumn.
     * 
     * @param autonumColumn The autonumColumn to set.
     */
    public void setAutonumColumn(SparseArray<String> autonumColumn) {
        this.autonumColumn = autonumColumn;
    }
    
    /**
     * Retrieve getLargeObjectByValue.
     *
     * @return Returns the getLargeObjectByValue.
     */
    public boolean isGetLargeObjectByValue() {
        return this.getLargeObjectByValue;
    }
    
    /**
     * Set getLargeObjectByValue.
     * 
     * @param getLargeObjectByValue The getLargeObjectByValue to set.
     */
    public void setGetLargeObjectByValue(boolean getLargeObjectByValue) {
        this.getLargeObjectByValue = getLargeObjectByValue;
    }
    
    protected LargeObjectMarshaller getLargeObjectMarshaller(
    ) {
        if(this.largeObjectMarshaller == null) {
            this.largeObjectMarshaller = new LargeObjectMarshaller(this.getLargeObjectByValue);
        }
        return this.largeObjectMarshaller;
    }
    
    //-----------------------------------------------------------------------    

    /**
     * Close a connection
     * 
     * @param closeable the object to be closed
     */
    protected void close(
        Connection closeable
    ) {
        if(closeable != null) try {
            closeable.close();
        } catch(Throwable ignorable) {
            // Do not log
        }
    }

    /**
     * Close a prepared statement
     * 
     * @param closeable the object to be closed
     */
    protected void close(
        PreparedStatement closeable
    ) {
        if(closeable != null) try {
            closeable.close();
        } catch(Throwable ignorable) {
            // Do not log
        }
    }

    /**
     * Close a result set
     * 
     * @param closeable the object to be closed
     */
    protected void close(
        ResultSet closeable
    ) {
        if(closeable != null) try {
            closeable.close();
        } catch(Throwable ignorable) {
            // Do not log
        }
    }

    /**
     * Get rid for given attribute name.
     * 
     * @param name
     * @return
     */
    @Override
    public String toRid(
        String name
    ) {
        return name + this.referenceIdAttributesSuffix;
    }

    @Override
    public String toRsx(
        String name
    ){
        return name + this.referenceIdSuffixAttributesSuffix;
    }

    @Override
    public String toOid(
        String name
    ) {
        return name + this.objectIdAttributesSuffix;
    }

    public String toId(
        String name
    ) {
        return name + "_id";
    }

    public String toIdx(
        String name
    ) {
        return name + "_idx";
    }

    /**
     * Tells whether reference tables shall be used
     * 
     * @return <code>true</code> if reference tables are used
     */
    protected boolean useReferenceTables(
    ){
        return LayerConfigurationEntries.REFERENCE_ID_FORMAT_REF_TABLE.equals(this.getReferenceIdFormat());
    }

    /**
     * Get upper bound for embedded feature.
     * 
     * @param attributeName
     * @return upper bound for embedded feature. null if feature is not embedded.
     */
    public Integer getEmbeddedFeature(
       String attributeName
    ) {
        return this.embeddedFeatures.get(attributeName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0#externalizeStringValue(java.lang.String, java.lang.Object)
     */
    @Override
    public Object externalizeStringValue(
        String _columnName,
        Object value
    ) {
        String columnName = _columnName;
        if(value instanceof String || value instanceof URI) {
            // column name could be qualified, e.g. vm.<column name>
            int pos = columnName.indexOf(".");
            if(pos >= 0) {
                columnName = columnName.substring(pos+1);
            }
            List<String[]> stringReplacements = this.getStringMacros().get(columnName);
            if(stringReplacements != null) {
                for(String[] stringReplacement : stringReplacements) {
                    String macroName = stringReplacement[0];
                    String macroValue = stringReplacement[1];
                    // replace matching macro value with macro name
                    String stringValue = value.toString();
                    if(stringValue.startsWith(macroValue)) {
                        return macroName + stringValue.substring(macroValue.length());
                    }
                }
            }
        }
        return value;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0#internalizeStringValue(java.lang.String, java.lang.String)
     */
    @Override
    public String internalizeStringValue(
        String _columnName,
        String value
    ) {
        String columnName = _columnName;
        // column name could be qualified, e.g. vm.<column name>
        int pos = columnName.indexOf(".");
        if(pos >= 0) {
            columnName = columnName.substring(pos+1);
        }
        List<String[]> stringReplacements = this.getStringMacros().get(columnName);
        if(stringReplacements != null) {
            for(String[] stringReplacement : stringReplacements){ 
                String macroName = stringReplacement[0];
                String macroValue = stringReplacement[1];
                // replace matching macro name with macro value
                if(value.startsWith(macroName)) {
                    return macroValue + value.substring(macroName.length());
                }
            }
        }
        return value;
    }

    /**
     * Externalizes a Path value. The externalized value is stored on the database.
     * Also applies the path macros and returns the shortened path. For compatibility 
     * reasons returns URI notation if no macro replacement was applied, else XRI 
     * notation.
     * 
     * @param conn
     * @param source
     * @return
     * @throws ServiceException
     */
    public String externalizePathValue(
        Connection conn,
        Path source
    ) throws ServiceException {
        if(this.isUseNormalizedReferences()) {
            return this.getDatabaseConfiguration().normalizeObjectIds() ? this.getDatabaseConfiguration().buildObjectId(
                source
            ) : this.getReferenceId(
                conn,
                source,
                false
            ) + "/" + source.getLastSegment().toClassicRepresentation();
        } else {
            String converted = source.toXRI();
            boolean modified = false;
            for(Iterator<Entry<String,String>> i = this.getPathMacros().entrySet().iterator(); i.hasNext(); ) {
                Entry<String,String> e = i.next();
                String macroName = e.getKey();
                String macroValue = e.getValue();
                if(converted.indexOf(macroValue) >= 0) {
                    Pattern pattern = Pattern.compile(macroValue);
                    converted = pattern.matcher(converted).replaceFirst("xri:*" + macroName);
                    converted = pattern.matcher(converted).replaceAll("(*" + macroName + ")");
                    modified = true;
                }
            }
            return modified ? converted : source.toURI();
        }
    }

    /**
     * Internalizes an external, stringified path value. Also applies the path 
     * macros to expand the path.
     * 
     * @throws ServiceException 
     */
    @SuppressWarnings("deprecation")
    public Object internalizePathValue(
        String source
    ) throws ServiceException {
        if(this.getDatabaseConfiguration().normalizeObjectIds()) {
            return this.getDatabaseConfiguration().buildResourceIdentifier(source, false);
        } else {
            String converted = source;
            boolean modified = false;
            for(Iterator<Entry<String,String>> i = this.getPathMacros().entrySet().iterator(); i.hasNext(); ) {
                Entry<String,String> e = i.next();
                String macroName = e.getKey();
                String macroValue = e.getValue();
                if(converted.indexOf(macroName) >= 0) {
                    converted = Pattern.compile("xri:*" + macroName).matcher(converted).replaceFirst(macroValue);
                    converted = Pattern.compile("(*" + macroName + ")").matcher(converted).replaceAll(macroValue);
                    modified = true;
                }
            }
            Path convertedPath = new Path(converted);
            return modified ? (
                convertedPath.toXri().equals(converted) ? (Object)convertedPath : (Object)converted 
            ) : ( 
                convertedPath.toUri().equals(converted) ? (Object)convertedPath : (Object)converted
            );
        }
    }

    /**
     * Execute a query
     * 
     * @param ps
     * @param statement
     * @param statementParameters
     * @param fetchSize the requested fetch is, use FetchPlan.FETCH_SIZE_OPTIMAL as default
     * 
     * @return the query result
     * 
     * @throws SQLException
     */
    public ResultSet executeQuery(
        PreparedStatement ps,
        String statement,
        List<?> statementParameters, 
        int fetchSize
    ) throws SQLException {
        ps.setFetchSize(this.getFetchSizeT().adjust(fetchSize));
        ps.setFetchDirection(ResultSet.FETCH_FORWARD);
        SysLog.detail("statement", statement);
        SysLog.detail("parameters", statementParameters);
        long startTime = System.currentTimeMillis();
        ResultSet rs = ps.executeQuery();
        long duration = System.currentTimeMillis() - startTime;
        SysLog.detail("execution time", Long.valueOf(duration));
        return rs;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0#executeUpdate(java.sql.PreparedStatement, java.lang.String, java.util.List)
     */
    @Override
    public int executeUpdate(
        PreparedStatement ps,
        String statement,
        List<?> statementParameters
    ) throws SQLException {
        SysLog.detail("statement", statement);
        SysLog.detail("parameters", statementParameters);
        long startTime = System.currentTimeMillis();
        int rowCount = ps.executeUpdate();
        long duration = System.currentTimeMillis() - startTime;
        SysLog.detail("rowCount", Integer.valueOf(rowCount));
        SysLog.detail("execution time", Long.valueOf(duration));
        return rowCount;
    }

    private String getDbObjectForQuery1(
        Connection conn,
        DbObject dbObject,
        String dbObjectHint
    ) throws ServiceException {        
        DbObjectConfiguration entry = dbObject.getConfiguration();
        String dbObjectForQuery = entry.getDbObjectForQuery1();    
        if(dbObjectHint != null) {
            if(dbObjectHint.startsWith(XRI_1Protocols.SCHEME_PREFIX)) {
                Path path = new Path(dbObjectHint);
                DbObject referencedDbObject = this.createDbObject(
                    conn, 
                    path, 
                    true
                );
                dbObjectForQuery += referencedDbObject.getTableName();
            }
            else {
                dbObjectForQuery += dbObjectHint;
            }
        }
        if(!dbObject.getReferencedType().isPattern()) {
            // Assert existence of reference id
            this.getReferenceId(
                conn,
                dbObject.getReference(),
                true
            );
        }
        return dbObjectForQuery;
    }

    /**
     * Retrieve the catalog id
     * 
     * @param the actual catalog, or <code>null</code> if none is defined
     * 
     * @return the catalog id, i.e. either the catalog itself or "" if
     * the catalog is <code>null</code> 
     */
    private static String toCatalogId(String catalog) {
        return catalog == null ? "" : catalog;
    }
    
    /**
     * Determine the database product name
     * 
     * @param connection
     * 
     * @return the connection's database product name
     */
    protected String getDatabaseProductName(
        Connection connection
    ){
        try {
            final String catalogId = toCatalogId(connection.getCatalog());
            String databaseProductName = this.databaseProductNames.get(catalogId);
            if(databaseProductName == null) {
                databaseProductName = connection.getMetaData().getDatabaseProductName();
                this.databaseProductNames.put(catalogId, databaseProductName);
            }
            return databaseProductName;
        } catch (SQLException exception) {
            SysLog.detail("Database product name determination failure", exception);
            return "n/a";
        }
    }

    /**
     * Retrieve a JDBC driver configuration property
     * 
     * @param connection
     * @param suffix
     * 
     * @return a JDBC driver configuration property value
     */
    protected String getDriverProperty(
        Connection connection,
        String suffix
    ) throws ServiceException {
        return this.getJdbcDriverSqlProperties().getProperty(
            this.getDatabaseProductName(connection) + '.' + suffix
        );
    }

    /**
     * Retrieve a JDBC driver configuration property
     * 
     * @param connection
     * @param suffix
     * 
     * @return a JDBC driver configuration property value
     */
    protected String getDriverProperty(
        Connection connection,
        String suffix,
        String defaultValue
    ) throws ServiceException {
        return this.getJdbcDriverSqlProperties().getProperty(
            this.getDatabaseProductName(connection) + '.' + suffix,
            defaultValue
        );
    }

    /**
     * Computes the SQL select statement returning one object-index-slice per row for
     * the given path. The statement is appended to 'statement'. VIEW_MODE_OBJECT_ITERATION
     * must return at least slice 0. VIEW_MODE_OBJECT_RETRIEVAL must return all object
     * slices. 
     */
    protected String getView(
        Connection conn,
        DbObject dbObject,
        String dbObjectHint,
        short viewMode,
        String requestedColumnSelector,
        Set<String> requestedMixins
    ) throws ServiceException {
        Set<String> mixins = requestedMixins;
        String columnSelector = requestedColumnSelector == null ? 
            DEFAULT_COLUMN_SELECTOR : 
            requestedColumnSelector;
        String dbObjectForQuery1 = null;
        String dbObjectForQuery2 = null;
        DbObjectConfiguration typeConfiguration = dbObject.getConfiguration();

        String databaseProductName = getDatabaseProductName(conn);
        if(typeConfiguration != null) {
            if(typeConfiguration.getDbObjectForQuery1() != null) {
                dbObjectForQuery1 = this.getDbObjectForQuery1(
                    conn,
                    dbObject,
                    dbObjectHint
                );            
            } else {
                dbObjectForQuery1 = typeConfiguration.getDbObjectForUpdate1();
            }
            dbObjectForQuery2 = typeConfiguration.getDbObjectForQuery2() == null ? 
                typeConfiguration.getDbObjectForUpdate2() : 
                typeConfiguration.getDbObjectForQuery2();
            String[] dbObjectsForQuery = new String[]{
                dbObjectForQuery1, 
                dbObjectForQuery2
            };
            for(int i = 0; i < dbObjectsForQuery.length; i++) {
                String dbObjectForQuery = dbObjectsForQuery[i];
                if(dbObjectForQuery != null) {
                    // replace driver specific operations / functions
                    try {
                        if(dbObjectForQuery.indexOf("STRCAT.PREFIX") >= 0) {
                            dbObjectForQuery = Pattern.compile("STRCAT.PREFIX").matcher(dbObjectForQuery).replaceAll(
                                this.getDriverProperty(conn, "STRCAT.PREFIX")
                            );
                        }
                        if(dbObjectForQuery.indexOf("STRCAT.INFIX") >= 0) {
                            dbObjectForQuery = Pattern.compile("STRCAT.INFIX").matcher(dbObjectForQuery).replaceAll(
                                this.getDriverProperty(conn, "STRCAT.INFIX")
                            );
                        }
                        if(dbObjectForQuery.indexOf("STRCAT.SUFFIX") >= 0) {
                            dbObjectForQuery = Pattern.compile("STRCAT.SUFFIX").matcher(dbObjectForQuery).replaceAll(
                                this.getDriverProperty(conn, "STRCAT.SUFFIX")
                            );
                        }
                        if(dbObjectForQuery.indexOf("NULL.NUMERIC") >= 0) {
                            dbObjectForQuery = Pattern.compile("NULL.NUMERIC").matcher(dbObjectForQuery).replaceAll(
                                this.getDriverProperty(conn, "NULL.NUMERIC")
                            );
                        }
                        if(dbObjectForQuery.indexOf("NULL.CHARACTER") >= 0) {
                            dbObjectForQuery = Pattern.compile("NULL.CHARACTER").matcher(dbObjectForQuery).replaceAll(
                                this.getDriverProperty(conn, "NULL.CHARACTER")
                            );
                        }
                        if(dbObjectForQuery.indexOf("CORREL.SUBQUERY.BEGIN") >= 0) {
                            dbObjectForQuery = Pattern.compile("CORREL.SUBQUERY.BEGIN").matcher(dbObjectForQuery).replaceAll(
                                this.getDriverProperty(conn, "CORREL.SUBQUERY.BEGIN")
                            );
                        }
                        if(dbObjectForQuery.indexOf("CORREL.SUBQUERY.END") >= 0) {
                            dbObjectForQuery = Pattern.compile("CORREL.SUBQUERY.END").matcher(dbObjectForQuery).replaceAll(
                                this.getDriverProperty(conn, "CORREL.SUBQUERY.END")
                            );
                        }
                    } catch(Exception e) {
                        // ignore
                    }
                }    
            }
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "Missing type configuration",
                new BasicException.Parameter("db object", dbObject)            
            );        
        }

        boolean isComplex = dbObjectForQuery1.startsWith("SELECT") || dbObjectForQuery1.startsWith("select");

        // Join primary with slice 0 of secondary and add mixin attributes 
        // as private p$$ attributes
        if(viewMode == VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY) {
            /**
             * Generate a SELECT statement of the form
             * 
             * SELECT v.*, vm.<mixin attributes> FROM
             *   <primary db object for query> v,
             *   <secondary db object for query> vm
             * WHERE
             *   v.<dbObject selector $i> = vm.<dbObject selector $j>
             *   v.<getObjectIdColumn()> = vm.<getObjectIdColumn()>
             *
             * mixinAttributes must contain at least one attribute which is guaranteed to have
             * a value --> object_class. Otherwise no slice is generated for these objects
             * even if they exist and would match the filter.
             * 
             * If db object is not indexed the v x vm join is not required
             * The mixinAttributes are taken from v in this case.
             */
            if(
                (mixins != null) && 
                !mixins.isEmpty()
                ) {
                mixins = new HashSet<String>(mixins);
                mixins.add(SystemAttributes.OBJECT_CLASS);      
                mixins.removeAll(
                    dbObject.getExcludeAttributes()
                );
                String view = "";
                view += "SELECT " + dbObject.getHint() + " " + columnSelector;
                for(String mixin: mixins) {
                    int upperBound = this.getEmbeddedFeature(mixin) != null ? this.getEmbeddedFeature(mixin).intValue() : 1;
                        for(int j = 0; j < upperBound; j++) {
                            String columnName = this.getColumnName(conn, mixin, j, upperBound > 1, false, false);
                            String prefixedColumnName = this.getColumnName(conn, mixin, j, upperBound > 1, false, true);
                            view += ", " + (dbObject.getIndexColumn() == null ? "v." : "vm.") + columnName + " AS " + prefixedColumnName;
                        }
                }              
                // JOIN is required if primary is indexed
                if(dbObject.getIndexColumn() != null) {
                    boolean useInnerJoin = !databaseProductName.startsWith("DB2");  
                    if(useInnerJoin) {
                        view += isComplex ? 
                            " FROM (" + dbObjectForQuery1 + ") v INNER JOIN (" + (dbObjectForQuery2 == null ? dbObjectForQuery1 : dbObjectForQuery2) + ") vm" : 
                                " FROM " + dbObjectForQuery1 + " v INNER JOIN " + (dbObjectForQuery2 == null ? dbObjectForQuery1 : dbObjectForQuery2) + " vm";
                        view += " ON ";
                    }
                    else {
                        view += isComplex ? 
                            " FROM (" + dbObjectForQuery1 + ") v, (" + (dbObjectForQuery2 == null ? dbObjectForQuery1 : dbObjectForQuery2) + ") vm" : 
                                " FROM " + dbObjectForQuery1 + " v, " + (dbObjectForQuery2 == null ? dbObjectForQuery1 : dbObjectForQuery2) + " vm";
                        view += " WHERE ";              
                    }
                    int k = 0;
                    // Compare rid selectors of v, vm
                    for(String selector : dbObject.getReferenceColumn()) {
                        view += k == 0 ? "" : " AND ";
                        view += "(v." + selector + " = " + "vm." + selector + ")";
                        k++;
                    }
                    // Compare oid selectors of v, vm
                    for(Object objectIdColumn :  dbObject.getObjectIdColumn()) {
                        view += k == 0 ? "" : " AND ";
                        view += "(v." + objectIdColumn + " = " + "vm." + objectIdColumn + ")";
                        k++;
                    }
                    view += " WHERE (vm." + this.OBJECT_IDX + " = 0)";
                }
                // Non-indexed db object (--> secondary view is always null)
                else {
                    view += isComplex ? 
                        " FROM (" + dbObjectForQuery1 + ") v" : 
                            " FROM " + dbObjectForQuery1 + " v";
                    // Join criteria supported only for primary, non-indexed views with exactly one object id column
                    if(
                        (dbObject.getJoinCriteria() != null) &&
                        (dbObject.getObjectIdColumn().size() == 1)
                        ) {
                        String[] joinCriteria = dbObject.getJoinCriteria();
                        String objectIdColumn = dbObject.getObjectIdColumn().get(0);
                        view += " INNER JOIN " + joinCriteria[0] + " vj ON v." + objectIdColumn + " = vj." + joinCriteria[2];
                    }              
                    view += " WHERE (1=1)";
                }
                return view;
            }
            // No mixin attributes
            else {
                if(
                    (dbObject.getJoinCriteria() != null) &&
                    (dbObject.getObjectIdColumn().size() == 1)
                    ) {
                    String[] joinCriteria = dbObject.getJoinCriteria();
                    String objectIdColumn = dbObject.getObjectIdColumn().get(0);
                    return "SELECT " + columnSelector + " FROM " + dbObjectForQuery1 + " v INNER JOIN " + joinCriteria[0] + " vj ON v." + objectIdColumn + " = vj." + joinCriteria[2] + " WHERE (1=1)";
                } else {
                    return dbObjectForQuery1;
                }
            }
        }
        // Return columns of secondary db object
        else if(viewMode == VIEW_MODE_SECONDARY_COLUMNS) {
            return dbObjectForQuery2 == null ? dbObjectForQuery1 : dbObjectForQuery2;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED, 
                "Unsupported view mode",
                new BasicException.Parameter("reference", dbObject.getReference()),            
                new BasicException.Parameter("view mode", viewMode)
            );
        }
    }

    /**
     * Tells whether the class denotes an aspect base class to be to be checked indirectly only
     * through their core reference.
     *  
     * @param qualifiedClassName
     * 
     * @return <code>true</code> if the class shall be checked indirectly only
     */
    protected boolean isAspectBaseClass(
        String qualifiedClassName
    ){
        return Aspects.isAspectBaseClass(qualifiedClassName);
    }
    
    /**
     * Prepare an instance-of predicates 
     *  
     * @param request
     * @param qualifiedClassNames
     * @return the instance-of predicate
     * 
     * @throws ServiceException
     */    
    protected FilterProperty mapInstanceOfFilterProperty(
        QueryRecord request,
        Collection<String> qualifiedClassNames
    ) throws ServiceException {
        int aspectBaseClasses = 0;
        AspectBaseClasses: for(String qualifiedClassName : qualifiedClassNames) {
            if(isAspectBaseClass(qualifiedClassName)) {
                aspectBaseClasses++;
            } else {
                break AspectBaseClasses;
            }
        }
        if(aspectBaseClasses == qualifiedClassNames.size()) {
            List<FilterProperty> filterProperties = FilterProperty.getFilterProperties(request.getQueryFilter());
            for(FilterProperty filterProperty : filterProperties) {
                if(
                    "core".equals(filterProperty.name()) &&
                    Quantifier.valueOf(filterProperty.quantor()) == Quantifier.THERE_EXISTS
                ){
                    // Skip 'object_instanceof' predicate because a 'core' predicate is supplied as well
                    return null;
                } 
            }
            return new FilterProperty(
                Quantifier.THERE_EXISTS.code(),
                "core",  
                ConditionType.IS_NOT_IN.code()
            );
        }
        Set<String> subClasses = new HashSet<String>();
        for(String qualifiedClassName : qualifiedClassNames) {
            if(this.isBaseClass(qualifiedClassName)) {
                // Adding the filter property OBJECT_CLASS for base objects typically results
                // in a long list of subclasses which is expensive to process for database 
                // systems. Eliminating the base object filter could result in returning objects
                // which are not instance of the requested object class. However this should 
                // never happen because matching and non-matching objects must never be mixed in 
                // the same database table.
                return null;
            }
            subClasses.add(qualifiedClassName);
            for(Object path : this.getModel().getElement(qualifiedClassName).objGetList("allSubtype")) {
                subClasses.add(((Path)path).getLastSegment().toClassicRepresentation());
            }
        }
        return new FilterProperty(
            Quantifier.THERE_EXISTS.code(),
            OBJECT_CLASS,  
            ConditionType.IS_IN.code(),
            subClasses.toArray()
        );
    }
        
    /**
     * Tells whether the given connection supports scroll sensitive result sets
     * 
     * @param connection
     * 
     * @return <code>true</code> if the given connection supports scroll sensitive result sets
     */
    protected boolean allowScrollSensitiveResultSet(
        Connection connection
    ) {
        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            return Boolean.parseBoolean(this.getJdbcDriverSqlProperties().getProperty(databaseProductName + ".ALLOW.SCROLLSENSITIVE.RESULTSET"));            
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * Tells whether the given connection supports scroll insensitive result sets
     * 
     * @param connection
     * 
     * @return <code>true</code> if the given connection supports scroll insensitive result sets
     */
    protected boolean allowScrollInsensitiveResultSet(
        Connection connection
    ) {
        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            return Boolean.parseBoolean(this.getJdbcDriverSqlProperties().getProperty(databaseProductName + ".ALLOW.SCROLLINSENSITIVE.RESULTSET"));            
        } catch(Exception e) {
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#prepareStatement(java.sql.Connection, java.lang.String, boolean)
     */
    @Override
    public
    PreparedStatement prepareStatement(
        Connection conn,
        String statement,
        boolean updatable
    ) throws SQLException {
        return updatable ? conn.prepareStatement(
            statement,
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_UPDATABLE
        ) : conn.prepareStatement(
            statement,
            getReadOnlyResultSetType(conn),
            ResultSet.CONCUR_READ_ONLY
        );
    }

    /**
     * Get read-only result set type for given connection.
     * 
     * @param connection
     * @return
     */
    private int getReadOnlyResultSetType(
        Connection connection
    ){
        switch(this.resultSetType) {
            case ResultSet.TYPE_SCROLL_SENSITIVE:
                if(allowScrollSensitiveResultSet(connection)) return ResultSet.TYPE_SCROLL_SENSITIVE;
                // fall through
            case ResultSet.TYPE_SCROLL_INSENSITIVE:
                if(allowScrollInsensitiveResultSet(connection)) return ResultSet.TYPE_SCROLL_INSENSITIVE;
                // fall through
            default:
                return ResultSet.TYPE_FORWARD_ONLY;
            }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#isBlobColumnValue(java.lang.Object)
     */
    boolean isBlobColumnValue(
        Object val
    ) {
        return
        val instanceof byte[] ||
        val instanceof Blob;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#isClobColumnValue(java.lang.Object)
     */
    boolean isClobColumnValue(
        Object val
    ) {
        return
        val instanceof String ||
        val instanceof Clob;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#getBlobColumnValue(java.lang.Object, java.lang.String, org.openmdx.base.mof.cci.ModelElement_1_0)
     */
    Object getBlobColumnValue(
        Object val,
        String attributeName,
        ModelElement_1_0 attributeDef
    ) throws ServiceException, SQLException {
        return this.getLargeObjectMarshaller().getBinaryColumnValue(val, attributeName, attributeDef);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#getClobColumnValue(java.lang.Object, java.lang.String, org.openmdx.base.mof.cci.ModelElement_1_0)
     */
    Object getClobColumnValue(
        Object val,
        String attributeName,
        ModelElement_1_0 attributeDef
    ) throws ServiceException, SQLException {
        return this.getLargeObjectMarshaller().getCharacterColumnValue(val, attributeName, attributeDef);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#setClobColumnValue(java.sql.PreparedStatement, int, java.lang.Object)
     */
    void setClobColumnValue(
        PreparedStatement ps,
        int column,
        Object val
    ) throws SQLException, ServiceException {
        if(val instanceof String) {
            String value = (String)val;
            if(requiresStreaming(ps, value)){
                ps.setCharacterStream(column, new StringReader(value), value.length());
            } else {
                ps.setString(
                    column, 
                    value
                );
            }
        } else if(
            val instanceof CharacterLargeObject ||
            val instanceof Reader
         ) {
            CharacterLargeObject clob;
            if(val  instanceof CharacterLargeObject) {
                clob = (CharacterLargeObject) val;
            } else if(val instanceof String) {
                clob = CharacterLargeObjects.valueOf((String)val);
            } else {
                clob = CharacterLargeObjects.valueOf((Reader)val);
            }
            SetLargeObjectMethod setLargeObjectMethod = howToSetCharacterLargeObject(ps.getConnection());
            try {
                if(clob.getLength() == null && this.getLargeObjectMarshaller().isTallyingRequired(setLargeObjectMethod)) {
                    clob = this.tallyLargeObject(clob.getContent());
                }
                this.getLargeObjectMarshaller().setCharacterColumnValue(ps, column, clob, setLargeObjectMethod);
            } catch(IOException e) {
                throw new ServiceException(e);
            }
        } else if(val instanceof Clob) {
            ps.setClob(
                column,
                (Clob)val
            );
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "String type not supported. Supported are [String|Reader|CharacterLargeObject|Clob]",
                new BasicException.Parameter("type", val == null ? null : val.getClass().getName())
            );
        }
    }

    /**
     * PreparedStatement.setString() does not work for Oracle with strings > 8K
     * 
     * @param ps
     * @param value
     * 
     * @return <code>true</code> if we have to bring a large string into oracle
     * @throws SQLException 
     */
    private boolean requiresStreaming(
        PreparedStatement ps, 
        String value
    ) throws SQLException {
        return
            value.length() > 2000 &&
            "Oracle".equals(this.getDatabaseProductName(ps.getConnection()));
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#setBlobColumnValue(java.sql.PreparedStatement, int, java.lang.Object)
     */
    void setBlobColumnValue(
        PreparedStatement ps,
        int column,
        Object val
    ) throws SQLException, ServiceException {
        if(val instanceof byte[]) {
            ps.setBytes(
                column,
                (byte[])val
            );
        } else if(
            val instanceof InputStream ||
            val instanceof BinaryLargeObject
        ) {
            BinaryLargeObject blob = val instanceof InputStream ? BinaryLargeObjects.valueOf(
                (InputStream)val
            ) : (BinaryLargeObject) val;
            try {    
                SetLargeObjectMethod setLargeObjectMethod = howToSetBinaryLargeObject(ps.getConnection());
                if(blob.getLength() == null && this.getLargeObjectMarshaller().isTallyingRequired(setLargeObjectMethod)) {
                    blob = this.tallyLargeObject(blob.getContent());
                }
                this.getLargeObjectMarshaller().setBinaryColumnValue(ps, column, blob, setLargeObjectMethod);
            } catch(IOException e) {
                throw new ServiceException(e);
            }
        }  else if(val instanceof Blob) {
            ps.setBlob(
                column,
                (Blob)val
            );
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "binary type not supported. Supported are [byte[]|InputStream|BinaryLargeObject|Blob]",
                new BasicException.Parameter("type", val == null ? null : val.getClass().getName())
            );
        }
    }

    /**
     * Sets row count to object at given position. The implementation can optimize
     * positioning if lastRowCount != -1 and lastPosition != -1 and the driver/
     * database support absolute positioning. The object at lastPosition starts
     * at row lastRowCount.
     * 
     * @param dbObjectConfiguration
     */
    FastResultSet setPosition(
        ResultSet rs,
        int position,
        int lastPosition,
        int lastRowCount,
        boolean isIndexed, 
        DbObjectConfiguration dbObjectConfiguration
    ) throws ServiceException, SQLException {
        long startTime = System.currentTimeMillis();
        boolean hasMore = rs.next();
        // do not touch rs with hasMore==false
        // DB2 reports 'Invalid operation: result set closed'
        if(!hasMore) {
            return null;
        }
        FastResultSet frs = new FastResultSet(this, rs);
        if(position > 0) {
            boolean positioned = false;
            // Move forward to position by ResultSet.absolute()
            if(!isIndexed && !dbObjectConfiguration.isAbsolutePositioningDisabled() && frs.isAbsolutePositioningEnabled()) {
                try {
                    SysLog.log(Level.INFO, "Set absolute position to {0} for type {1}", Integer.valueOf(position), dbObjectConfiguration.getTypeName());
                    hasMore = frs.absolute(position+1);
                    positioned = true;
                } catch(SQLException e) {
                    SysLog.log(Level.SEVERE, "Absolute positioning failed for the type {0}. Falling back to positioning by iteration", dbObjectConfiguration.getTypeName());
                    dbObjectConfiguration.setAbsolutePositioningDisabled(true);
                }
            }
            // Move forward to position by iterating the result set
            if(!positioned) {
                if(!isIndexed) {
                    // PK not used
                    int count = 0;  
                    while(hasMore && ++count <= position) {
                        hasMore = frs.next();
                        // HSQLDB bug workaround: skipping > 128 rows without reading at least one column 
                        // results in an ArrayIndexOutOfBoundsException in class org.hsqldb.navigator.RowSetNavigatorClient.getCurrent(Unknown Source)
                        if(hasMore) {
                            frs.getObject(OBJECT_OID);
                        }
                        if(count % 1000 == 0) {
                            SysLog.log(Level.FINE, "Current position for type {0} is {1}", dbObjectConfiguration.getTypeName(), Integer.valueOf(count));
                        }
                    }
                } else if(frs.getColumnNames().contains(OBJECT_OID)) {
                    // PK format: id
                    int count = 0;  
                    String previousId = (String)frs.getObject(OBJECT_OID);
                    while(hasMore) {
                        String id = (String)frs.getObject(OBJECT_OID);
                        if(!id.equals(previousId)) {
                            count++;
                            previousId = id;
                        }
                        if(count >= position) break;
                        hasMore = frs.next();
                        if(count % 1000 == 0) {
                            SysLog.log(Level.FINE, "Current position for type {0} is {1}", dbObjectConfiguration.getTypeName(), Integer.valueOf(count));
                        }
                    }
                } else {
                    // PK format: rid/id
                    int count = 0;    
                    Object previousOid = frs.getObject(OBJECT_OID);
                    Object previousRid = frs.getObject(OBJECT_RID);        
                    while(hasMore) {
                        Object oid = frs.getObject(OBJECT_OID);
                        Object rid = frs.getObject(OBJECT_RID);          
                        if(
                            !oid.equals(previousOid) ||
                            (rid instanceof Comparable ? ((Comparable)rid).compareTo(previousRid) != 0 : !rid.equals(previousRid)) 
                        ) {
                            count++;
                            previousOid = oid;
                            previousRid = rid;
                        }
                        if(count >= position) break;
                        hasMore = frs.next();
                    }
                }
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        if(duration > 0) {
            SysLog.log(Level.FINE, "Position duration for type {0} is {1} ms", dbObjectConfiguration.getTypeName(), Long.valueOf(duration));
        }
        if(hasMore) {
            return frs;
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#resultSetUpdateLong(java.sql.ResultSet, java.lang.String, long)
     */
    void resultSetUpdateLong(
        ResultSet rs,
        String columnName,
        long value
    ) throws SQLException {
        rs.updateLong(
            columnName,
            value
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#resultSetUpdateInt(java.sql.ResultSet, java.lang.String, int)
     */
    void resultSetUpdateInt(
        ResultSet rs,
        String columnName,
        int value
    ) throws SQLException {
        rs.updateInt(
            columnName,
            value
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#resultSetUpdateString(java.sql.ResultSet, java.lang.String, java.lang.String)
     */
    void resultSetUpdateString(
        ResultSet rs,
        String columnName,
        String value
    ) throws SQLException {
        rs.updateString(
            columnName,
            value
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#resultSetUpdateRow(java.sql.ResultSet)
     */
    void resultSetUpdateRow(
        ResultSet rs
    ) throws SQLException {
        rs.updateRow();
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#resultSetGetRow(java.sql.ResultSet)
     */
    int resultSetGetRow(
        ResultSet rs
    ) throws SQLException {
        // not implemented yet
        return -1;
    }
    
    @Override
    public String getSelectReferenceIdsClause(
        Connection conn,
        Path pattern,
        List<Object> statementParameters
    ) throws ServiceException {
        Path referencePattern = pattern.isObjectPath() ? pattern.getParent() : pattern;
        if(useReferenceTables()) {
            return
                "IN (" + 
                this.getSelectReferenceIdsFormatRefTableClause(
                    conn,
                    referencePattern,
                    statementParameters
                ) + ")";
        } else {
            Object rid = this.getReferenceId(
                conn,
                referencePattern,
                false
            ); 
            if(rid instanceof String) {
                String srid = (String) rid;
                if(srid.endsWith("%") && !srid.endsWith("\\%")) {
                    statementParameters.add(srid);
                    return "LIKE ? " + getEscapeClause(conn);               
                } else {
                    statementParameters.add(unescape(srid));
                    return "IN (?)";
                }
            } else {
                statementParameters.add(rid);
                return "IN (?)";
            }
        }
    }

    /**
     * Return a statement of the form SELECT object_rid FROM .._REF WHERE 
     * (c$i = pathPattern.get(i)) AND ... 
     * In case the path component at i-th position is a wildcard (:*) the component 
     * does not have to be compared. In case of a trailing % stop comparison, i.e. 
     * don't care about the trailing path components.
     * 
     * The referenceIds can then be retrieved by:
     * 
     *   ps = this.prepareStatement(
     *     conn,
     *     getSelectReferenceIdsStatement(pathPattern)
     *   );
     *   rs = ps.executeQuery();
     *   while(rs.next()) {
     *     referenceIds.add(
     *       new Long(rs.getLong(OBJECT_RID))
     *     );
     *   }
     */
    private String getSelectReferenceIdsFormatRefTableClause(
        Connection conn,
        Path referencePattern,
        List<Object> statementParameters
    ) throws ServiceException {
        return 
            "SELECT " + this.referenceLookupStatementHint + " " + OBJECT_RID + 
            " FROM " + this.namespaceId + "_" + T_REF + 
            " WHERE " + this.getSelectReferenceIdsFromRefTableClause(conn, referencePattern, statementParameters);
    }

    String getSelectReferenceIdsFromRefTableClause(
        Connection conn,
        Path pathPattern,
        List<Object> statementParameters
    ) throws ServiceException {
        String currentClause = "";
        boolean isFirst = true;
        boolean matchExact = true;
        // c[i]
        for(
            int i = 0;
            i < this.maxReferenceComponents;
            i++
        ) {
            String c = i < pathPattern.size() ? pathPattern.getSegment(i).toClassicRepresentation() : "#";
            if("%".equals(c) && (i == pathPattern.size() - 1)) {
                matchExact = false;
                break;
            } 
            if(!":*".equals(c) && !"%".equals(c)) {
                if(!isFirst) {
                    currentClause += " AND ";
                }
                isFirst = false;
                if(statementParameters == null) {
                    currentClause += "(" + this.getColumnName(conn, "c", i, true, true, false) + " = '" + c + "')";
                }
                else {
                    currentClause += "(" + this.getColumnName(conn, "c", i, true, true, false) + " = ?)";
                    statementParameters.add(c);
                }
            }
        }
        if(!isFirst) {
            currentClause += " AND ";
        }
        // n
        if(statementParameters == null) { 
            currentClause += "(n " + (matchExact ? "=" : ">=") + " " + pathPattern.size() + ")";
        } else {
            currentClause += "(n " + (matchExact ? "=" : ">=") + " ?)";
            statementParameters.add(Integer.valueOf(pathPattern.size()));          
        }
        return currentClause;
    }

    private Set<Long> findReferenceIdsFormatRefTable(
        Connection conn,
        Path pathPattern
    ) throws ServiceException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        Set<Long> referenceIds = new HashSet<Long>();
        String statement = null;

        List<Object> statementParameters = new ArrayList<Object>();
        try {
            // get referenceId of reference
            ps = this.prepareStatement(
                conn,
                statement = this.getSelectReferenceIdsFormatRefTableClause(
                    conn,
                    pathPattern,
                    statementParameters
                )
            );
            for(int i = 0; i < statementParameters.size(); i++) {
                ps.setObject(i+1, statementParameters.get(i));
            }
            rs = this.executeQuery(
                ps,
                statement,
                statementParameters, 
                FetchPlan.FETCH_SIZE_OPTIMAL
                );
            referenceIds = new HashSet<Long>();
            while(rs.next()) {
                referenceIds.add(
                    Long.valueOf(rs.getLong(OBJECT_RID))
                );
            }
        } catch(SQLException exception) {
            throw new ServiceException(
                exception, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path pattern", pathPattern),
                new BasicException.Parameter("statement", statement),
                new BasicException.Parameter("parameters", statementParameters),
                new BasicException.Parameter("sqlErrorCode", exception.getErrorCode()), 
                new BasicException.Parameter("sqlState", exception.getSQLState())
            );
        } catch(ServiceException exception) {
            throw exception;
        } catch(NullPointerException exception) {
            throw new ServiceException(exception).log();
        } catch(RuntimeException exception) {
            throw new ServiceException(exception);
        } finally {
            close(ps);
            close(rs);
        }      
        return referenceIds;
    }

    @Override
    public Object getReferenceId(
        Connection conn,
        Path referencePattern,
        boolean forceCreate
    ) throws ServiceException {  
        Path reference = referencePattern.isContainerPath() ? referencePattern : referencePattern.getParent(); 
        return reference.isEmpty() ? null : useReferenceTables() ? (Object) this.getReferenceIdFormatRefTable(
            conn,
            reference,
            forceCreate
        ) : this.getDatabaseConfiguration().normalizeObjectIds() ? this.getDatabaseConfiguration().buildReferenceId(
            reference
        ) : this.getReferenceIdFormatTypeNameWithPathComponents(
            reference
        );
    }

    public String getObjectId(
        String oid
    ) throws ServiceException{
        return this.getDatabaseConfiguration().normalizeObjectIds() ? this.getDatabaseConfiguration().buildObjectId(
            oid
        ) : oid; 
    }

    public String getObjectId(
        Connection conn,
        Path resourceIdentifier
    ) throws ServiceException{
        return this.getDatabaseConfiguration().normalizeObjectIds() ? this.getDatabaseConfiguration().buildObjectId(
            resourceIdentifier
        ) : getReferenceId (
            conn,
            resourceIdentifier.getParent(),
            false
        ) + "/" + resourceIdentifier.getLastSegment().toClassicRepresentation();
    }

    private String getReferenceIdFormatTypeNameWithPathComponents(
        Path reference
    ) throws ServiceException {
        DbObjectConfiguration dbObjectConfiguration = this.getDatabaseConfiguration().getDbObjectConfiguration(
            reference
        );
        Path type = dbObjectConfiguration.getType();
        String typeName = dbObjectConfiguration.getTypeName();
        if(type.size() >= 2 && reference.isLike(type.getParent())) {
            StringBuilder equalRid = new StringBuilder(typeName);
            StringBuilder likeRid = new StringBuilder(escape(typeName));
            boolean subtree = false;
            for(
                int l = 0;
                l < reference.size();
                l++
            ) {
                String component = reference.getSegment(l).toClassicRepresentation();
                if(":*".equals(component)) {                  
                    if(!subtree){
                        likeRid.append("/%");
                        subtree = true;
                        equalRid = null;
                    } 
                } else if(!component.equals(type.getSegment(l).toClassicRepresentation())) {
                    if(equalRid != null) {
                        equalRid.append('/').append(component);
                    }
                    likeRid.append('/').append(escape(component));
                    subtree = false;
                }
            }
            return (equalRid == null ? likeRid : equalRid).toString();
        } else {
            return null;
        }
    }

    /**
     * 
     * @param connection
     * @param value
     * @return
     * @throws ServiceException
     */
    @Override
    public String getPlaceHolder(
        Connection connection,
        Object value
    ) throws ServiceException {
        boolean dateTime = 
            value instanceof java.util.Date ||
            value instanceof XMLGregorianCalendar && DatatypeConstants.DATETIME.equals(((XMLGregorianCalendar) value).getXMLSchemaType());
        boolean timestampWithTimezone = dateTime && LayerConfigurationEntries.DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE.equals(getDateTimeType(connection));
        return timestampWithTimezone ? getTimestampWithTimzoneExpression(connection) : "?";
    }

    /**
     * Retrieve the placeholder for timestamp with time zone expressions
     * 
     * @param connection
     * 
     * @return the timestamp with time zone expression placeholder
     * 
     * @throws ServiceException
     */
    private String getTimestampWithTimzoneExpression(
        Connection connection
    ) throws ServiceException{
        return getDriverProperty(
            connection,
            "TIMESTAMP.WITH.TIMEZONE.EXPRESSION",
            "?"
        );
    }

    public String getAutonumValue(
        Connection conn,
        String sequenceName,
        String asFormat
    ) throws ServiceException, SQLException {
        String autonumFormat = getDriverProperty(
            conn, 
            asFormat != null && asFormat.indexOf("AS CHAR") > 0 ? "AUTOINC.CHAR" : "AUTOINC.NUMERIC"
        );
        if("AUTO".equals(autonumFormat)) {
            return null;
        } else if(autonumFormat != null) {
            autonumFormat = autonumFormat.replace("${SEQUENCE_NAME}", sequenceName);
            autonumFormat = autonumFormat.replace("${AS_FORMAT}", asFormat == null ? "" : asFormat);
            return autonumFormat;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE , 
                "AUTONUM format undefined for database. It must be configured in in jdbc-driver-sql.properties.",
                new BasicException.Parameter("databaseName", getDatabaseProductName(conn))
            );
        }
    }

    /**
     * Finds the matching path in the _REF table and returns the corresponding
     * referenceId. If forceCreate is true then the referenceId is created on-demand
     */
    private Long getReferenceIdFormatRefTable(
        Connection conn,
        Path reference,
        boolean forceCreate
    ) throws ServiceException {  

        String currentStatement = null;
        PreparedStatement ps = null;

        Set<Long> referenceIds = this.findReferenceIdsFormatRefTable(
            conn,
            reference
        );

        // either return when found or create on demand
        List<Object> statementParameters = null;
        try {
            Long referenceId = Long.valueOf(-1);
            if(referenceIds.isEmpty()) {
                SysLog.trace("No referenceIds found for reference", reference + "; forceCreate=" + forceCreate);
                if(forceCreate) {

                    // path component names
                    String columnNamesPathComponents = "";
                    for(int i = 0; i < this.maxReferenceComponents; i++) {
                        columnNamesPathComponents += ", " + this.getColumnName(conn, "c", i, true, true, false);
                    }        
                    // SQL sequences
                    String dbObjectRef = this.namespaceId + "_" + T_REF;
                    String autonumValue = this.getAutonumValue(
                        conn,
                        dbObjectRef,
                        null
                    );
                    currentStatement =
                        "INSERT INTO " + dbObjectRef +
                        " (" + (autonumValue == null ? "" : (OBJECT_RID + ", ")) + "n" + columnNamesPathComponents + ")" +
                        " VALUES (" + (autonumValue == null ? "" : autonumValue + ", ") + "?";

                    // place holders for path components 
                    for(int i = 0; i < maxReferenceComponents; i++) {
                        currentStatement +=", ?";
                    }    
                    currentStatement += ")";

                    // insert statement
                    ps = this.prepareStatement(
                        conn,
                        currentStatement.toString()
                    );
                    statementParameters = new ArrayList<Object>();
                    int statementParameter1;
                    ps.setInt(
                        1,
                        statementParameter1 = reference.size()
                    );
                    statementParameters.add(Integer.valueOf(statementParameter1));
                    // fill reference components up to maxReferenceComponents.
                    // fill missing component columns with blanks. This allows to
                    // define a unique key on the component columns.
                    for(int i = 0; i < this.maxReferenceComponents; i++) {
                        String statementParameterN;
                        ps.setString(
                            i + 2, 
                            statementParameterN = i < reference.size() ? reference.getSegment(i).toClassicRepresentation() : "#"
                        );
                        statementParameters.add(statementParameterN);
                    }
                    executeUpdate(ps, currentStatement, statementParameters);

                    referenceIds = this.findReferenceIdsFormatRefTable(
                        conn,
                        reference
                    );
                    if(referenceIds.size() == 1) {
                        referenceId = referenceIds.iterator().next();
                    } else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE , 
                            "Can not find created reference",
                            new BasicException.Parameter("reference", reference)
                        );
                    }
                } else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND, 
                        "Object reference not found",
                        new BasicException.Parameter("reference", reference)
                    );
                }
            } else if(referenceIds.size() > 1) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND, 
                    "more than one referenceId found for given reference",
                    new BasicException.Parameter("reference", reference),
                    new BasicException.Parameter("referenceIds", referenceIds)
                );
            } else {
                referenceId = referenceIds.iterator().next();
            }
            return referenceId;
        } catch(SQLException exception) {
            throw new ServiceException(
                exception, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("reference", reference),
                new BasicException.Parameter("statement", currentStatement),
                new BasicException.Parameter("parameters", statementParameters),
                new BasicException.Parameter("sqlErrorCode", exception.getErrorCode()), 
                new BasicException.Parameter("sqlState", exception.getSQLState())
            );
        } catch(ServiceException exception) {
            throw exception;
        } catch(NullPointerException exception) {
            throw new ServiceException(exception).log();
        } catch(Exception exception) {
            throw new ServiceException(exception);
        } finally {
            close(ps);
        }
    }

    public Path getReference(
        Connection conn,
        Object referenceId
        ) throws ServiceException {
        return useReferenceTables() ? this.getReferenceFormatRefTable(
            conn,
            (Number)referenceId
        ) : this.getDatabaseConfiguration().normalizeObjectIds() ? this.getDatabaseConfiguration().buildResourceIdentifier(
            (String)referenceId, 
            true // resource
        ) : this.getReferenceFormatTypeNameWithComponents(
            conn,
            (String)referenceId
        );
    }

    private Path getReferenceFormatTypeNameWithComponents(
        Connection conn,
        String referenceId
    ) throws ServiceException {
        List<String> components = new ArrayList<String>();
        StringTokenizer t = new StringTokenizer(referenceId, "/");
        while(t.hasMoreTokens()) {
            components.add(t.nextToken());          
        }
        if(components.isEmpty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "No components found for reference id",
                new BasicException.Parameter("reference", referenceId)
            );          
        }
        DbObjectConfiguration dbObjectConfiguration = this.getDatabaseConfiguration().getDbObjectConfiguration(
            components.get(0)
        );
        Path type = dbObjectConfiguration.getType();
        String[] referenceComponents = new String[type.size()-1];
        int pos = 1;
        for(int i = 0; i < referenceComponents.length; i++) {
            if(":*".equals(type.getSegment(i).toClassicRepresentation())) {
                if(pos >= components.size()) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "Reference not valid for type",
                        new BasicException.Parameter("reference", referenceId),
                        new BasicException.Parameter("type", type)
                    );                            
                }
                referenceComponents[i] = components.get(pos);
                pos++;
            } else {
                referenceComponents[i] = type.getSegment(i).toClassicRepresentation();
            }
        }
        return new Path(referenceComponents);
    }

    /**
     * Get reference path for corresponding referenceId.
     */
    private Path getReferenceFormatRefTable(
        Connection conn,  
        Number referenceId
    ) throws ServiceException  {

        PreparedStatement ps = null;
        String statement = null;
        ResultSet rs = null;
        Path reference = null;

        List<Object> statementParameters = new ArrayList<Object>();
        try {
            ps = this.prepareStatement(
                conn,
                statement = "SELECT * FROM " + this.namespaceId + "_" + T_REF + " WHERE (" + OBJECT_RID + " = ?)"
            );
            ps.setLong(
                1, 
                referenceId == null ? -1 : referenceId.longValue()
            );
            statementParameters.add(referenceId);
            rs = this.executeQuery(
                ps,
                statement,
                statementParameters, 
                FetchPlan.FETCH_SIZE_OPTIMAL
            );
            if(rs.next()) {
                int n = rs.getInt(this.getColumnName(conn, "n", 0, false, false, false));
                String[] components = new String[n];
                for(int i = 0; i < n; i++) {
                    components[i] = rs.getString(
                        this.getColumnName(conn, "c", i, true, false, false)
                    );
                }
                reference = new Path(components);
            } else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND, 
                    "can not find reference id",
                    new BasicException.Parameter("referenceId", referenceId),
                    new BasicException.Parameter("table", this.namespaceId + "_" + T_REF)
                );
            }
        } catch(SQLException exception) {
            throw new ServiceException(
                exception, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("reference", reference),
                new BasicException.Parameter("statement", statement),
                new BasicException.Parameter("parameters", statementParameters),
                new BasicException.Parameter("sqlErrorCode", exception.getErrorCode()), 
                new BasicException.Parameter("sqlState", exception.getSQLState())
            );
        } catch(ServiceException exception) {
            throw exception;
        } catch(NullPointerException exception) {
            throw new ServiceException(exception).log();
        } catch(Exception exception) {
            throw new ServiceException(exception);
        } finally {
            close(rs);
            close(ps);
        }      
        return reference;    
    }

    /**
     * Map database-neutral column name to database-specific column name.
     * 
     * @param conn
     * @param columnName
     * @param ignoreReservedWords
     * @return
     * @throws ServiceException
     */
    public String getDatabaseSpecificColumnName(
        Connection conn,
        String columnName,
        boolean ignoreReservedWords
    ) throws ServiceException {
        String databaseProductName = conn == null ? null : this.getDatabaseProductName(conn);
        if(ignoreReservedWords) {
            return columnName;
        } else if(
            "HSQL Database Engine".equals(databaseProductName) &&
            (RESERVED_WORDS_HSQLDB.contains(columnName) || (columnName.indexOf("$") >= 0))
        ) {
            return '"' + columnName.toUpperCase() + '"';
        } else if(
            "Oracle".equals(databaseProductName) &&
            RESERVED_WORDS_ORACLE.contains(columnName)
        ) {
            return '"' + columnName + '"';
        } else {
            return columnName;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0#getColumnName(java.sql.Connection, java.lang.String, int, boolean, boolean, boolean)
     */
    @Override
    public String getColumnName(
        Connection conn,
        String attributeName,
        int index,
        boolean indexSuffixIfZero,
        boolean ignoreReservedWords, 
        boolean markAsPrivate
    ) throws ServiceException {
        ConcurrentMap<String, String> columnNames = markAsPrivate ? this.privateColumnNames : this.publicColumnNames;
        String columnName = columnNames.get(attributeName);
        if(columnName == null) {
            StringBuilder name = new StringBuilder();
            for(
                int i = 0; 
                i < attributeName.length(); 
                i++
            ) {
                char c = attributeName.charAt(i);
                if(Character.isUpperCase(c)) {
                    name.append('_').append(Character.toLowerCase(c));
                } else if(c == '_') {
                    if(i < attributeName.length()-1) {
                        // do not escape _<digit>
                        if(Character.isDigit(attributeName.charAt(i+1))) {
                            name.append('_');
                        }
                        // escape _<alpha> as __<uppercase alpha>
                        else {
                            name.append("__");
                        }
                    } else {
                        name.append('_');
                    }
                } else {
                    name.append(c);
                }
            }
            // from->to mapping
            columnName = name.toString();
            boolean isSizeColumn = columnName.endsWith(SIZE_SUFFIX);
            String lookupName = isSizeColumn ? columnName.substring(0, columnName.length()-1) : columnName;
            String mappedName;
            String privateAttributesPrefix = this.getPrivateAttributesPrefix();
            if(markAsPrivate ) {
                mappedName = this.getDatabaseConfiguration().getFromToColumnNameMapping().get(privateAttributesPrefix + lookupName);
                if(mappedName == null) {
                    mappedName = this.getDatabaseConfiguration().getFromToColumnNameMapping().get(lookupName);
                    if(mappedName != null){
                        mappedName = privateAttributesPrefix + mappedName;
                    }
                }
            } else {
                mappedName = this.getDatabaseConfiguration().getFromToColumnNameMapping().get(lookupName);
            }
            if(mappedName != null) {
                columnName = isSizeColumn ? mappedName + SIZE_SUFFIX : mappedName;
            } else if (markAsPrivate) {
                columnName = privateAttributesPrefix + columnName;
            }
            columnNames.putIfAbsent(
                attributeName,
                columnName
            );
        }    
        // append index
        if(indexSuffixIfZero || index > 0) {
            columnName += this.getEmbeddedFeature(attributeName) != null ? '_' : '$';
            columnName += String.valueOf(index);
        }
        columnName = this.getDatabaseSpecificColumnName(
            conn, 
            columnName, 
            ignoreReservedWords
        );
        return columnName;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0#getFeatureName(java.lang.String)
     */
    @Override
    public String getFeatureName(
        String columnName
    ) throws ServiceException {
        String mappedColumnName = columnName;
        // Indexed column?
        int nameLength = Math.max(mappedColumnName.lastIndexOf('$'), mappedColumnName.lastIndexOf('_'));
        // Non-indexed column
        if(
            (nameLength < 0) || 
            mappedColumnName.endsWith("_") || 
            mappedColumnName.endsWith("$") || 
            !Character.isDigit(mappedColumnName.charAt(nameLength+1))
        ) {
            // Do not change column name
        }
        // Indexed column
        else {
            String attributeNameOfNonIndexedColumn = this.getFeatureName(
                mappedColumnName.substring(0, nameLength)
                );     
            // Only embedded features can be indexed
            if(this.getEmbeddedFeature(attributeNameOfNonIndexedColumn) != null) {
                return attributeNameOfNonIndexedColumn;      
            }
        }

        // to->from mapping
        if(this.getDatabaseConfiguration().getToFromColumnNameMapping().containsKey(mappedColumnName)) {
            mappedColumnName = this.getDatabaseConfiguration().getToFromColumnNameMapping().get(mappedColumnName);
        }
        String featureName = this.featureNames.get(mappedColumnName);
        if(featureName == null) {        
            String name = "";
            boolean nextAsUpperCase = false;
            for(
                int i = 0; 
                i < mappedColumnName.length();
                i++
                ) {
                char c = Character.toLowerCase(mappedColumnName.charAt(i));
                if(c == '"') {
                    // Ignore quotes. Some databases, e.g. Postgres
                    // return columns containing special characters 
                    // with quotes
                } else if(!nextAsUpperCase && (c == '_')) {
                    nextAsUpperCase = true;
                } else {
                    if(nextAsUpperCase) {
                        if(Character.isDigit(c)) {
                            name += "_" + c;
                        } else {
                            name += Character.toUpperCase(c);
                        }
                    } else {  
                        name += c;
                    }
                    nextAsUpperCase = false;
                }
            }
            featureName = name.toString().intern();
            this.featureNames.putIfAbsent(
                mappedColumnName,
                featureName
            );
        }
        return featureName;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0#setPreparedStatementValue(java.sql.Connection, java.sql.PreparedStatement, int, java.lang.Object)
     */
    @Override
    public void setPreparedStatementValue(
        Connection conn,
        PreparedStatement ps,
        int position,
        Object value
    ) throws ServiceException, SQLException {
        Object normalizedValue;
        if(value instanceof java.util.Date) {
            normalizedValue = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(
                DateTimeFormat.EXTENDED_UTC_FORMAT.format((java.util.Date)value)
            );
        } else {
            normalizedValue = value;
        }
        if( normalizedValue instanceof URI) {
            ps.setString(
                position, 
                normalizedValue.toString()
            );
        } else if(normalizedValue instanceof Short) {
            ps.setShort(
                position, 
                ((Short)normalizedValue).shortValue()
            );
        } else if(normalizedValue instanceof Integer) {
            ps.setInt(
                position, 
                ((Integer)normalizedValue).intValue()
            );
        } else if(normalizedValue instanceof Long) {
            ps.setLong(
                position, 
                ((Long)normalizedValue).longValue()
            );
        } else if(normalizedValue instanceof BigDecimal) {
            ps.setBigDecimal(
                position, 
                ((BigDecimal)normalizedValue).setScale(ROUND_UP_TO_MAX_SCALE, BigDecimal.ROUND_UP)
            );
        } else if(normalizedValue instanceof Number) {
            ps.setString(
                position, 
                normalizedValue.toString()
            );
        } else if(normalizedValue instanceof Path) {
            ps.setString(
                position,
                this.externalizePathValue(conn, (Path)normalizedValue)
            );
        } else if(normalizedValue instanceof Boolean) {
            Object sqlValue = this.getBooleanMarshaller().marshal(normalizedValue, conn); 
            if(sqlValue instanceof Boolean) {
                ps.setBoolean(
                    position,
                    ((Boolean)sqlValue).booleanValue()
                );
            } else if(sqlValue instanceof Number) {
                ps.setInt(
                    position,
                    ((Number)sqlValue).intValue()
                );
            } else {
                ps.setString(
                    position, 
                    sqlValue.toString()
                );
            }          
        } else if(normalizedValue instanceof Duration) {
            Object sqlValue = this.getDurationMarshaller().marshal(normalizedValue); 
            if(sqlValue instanceof BigDecimal) {
                ps.setBigDecimal(
                    position,
                    (BigDecimal)sqlValue
                );
            } else if(sqlValue instanceof BigInteger) {
                ps.setBigDecimal(
                    position, 
                    new BigDecimal((BigInteger)sqlValue)
                );
            } else if(sqlValue instanceof String) {
                ps.setString(
                    position, 
                    (String)sqlValue
                );
            } else {
                ps.setObject(
                    position, 
                    sqlValue
                );
            }          
        } else if(normalizedValue instanceof XMLGregorianCalendar) {
            Object sqlValue = this.getCalendarMarshaller().marshal(normalizedValue, conn); 
            if(sqlValue instanceof Time) {
                ps.setTime(
                    position,
                    (Time)sqlValue
                );
            } else if(sqlValue instanceof Timestamp) {
                Timestamp timestamp = (Timestamp) sqlValue;
                ps.setTimestamp(
                    position,
                    timestamp
                );
            } else if(sqlValue instanceof Date) {
                ps.setDate(
                    position,
                    (Date)sqlValue
                );
            } else if(sqlValue instanceof String) {
                ps.setString(
                    position, 
                    (String)sqlValue
                );
            } else {
                ps.setObject(
                    position, 
                    sqlValue
                );
            }          
        } else if(
            (normalizedValue instanceof String) ||
            (normalizedValue instanceof Reader) ||
            (normalizedValue instanceof CharacterLargeObject)
        ) {
            this.setClobColumnValue(
                ps,
                position,
                normalizedValue
            );
        } else if(
            (normalizedValue instanceof byte[]) ||
            (normalizedValue instanceof InputStream) ||
            (normalizedValue instanceof BinaryLargeObject)
        ) {
            this.setBlobColumnValue(
                ps,
                position,
                normalizedValue
            );
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED, 
                "attribute type not supported",
                new BasicException.Parameter("value-type", normalizedValue == null ? null : normalizedValue.getClass().getName()),
                new BasicException.Parameter("position", position)
            );
        }
    }

    private ObjectRecord getObject(
        Connection conn,
        Path path,
        short attributeSelector,
        Map<String,AttributeSpecifier> attributeSpecifiers,
        boolean objectClassAsAttribute,
        DbObject dbObject,
        ResultSet rs,
        String objectClass,
        boolean checkIdentity, 
        boolean throwNotFoundException
    ) throws ServiceException, SQLException {
        List<ObjectRecord> objects = new ArrayList<ObjectRecord>();
        this.getObjects(
            conn,
            dbObject,
            rs,
            objects,
            attributeSelector,
            attributeSpecifiers,
            objectClassAsAttribute,
            0, -1, -1, 1,
            objectClass
        );
        if(objects.size() != 1) {
            if(!objects.isEmpty()) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "Exactly one object expected",
                    new BasicException.Parameter("request-path", path),
                    new BasicException.Parameter("cardinality", objects.size())
                );
            } else if (throwNotFoundException && checkIdentity) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND, 
                    "No object found",
                    new BasicException.Parameter("request-path", path),
                    new BasicException.Parameter("cardinality", objects.size())
                );
            } else {
                return null;
            }
        }
        ObjectRecord object = objects.get(0);
        if(checkIdentity && !object.getResourceIdentifier().startsWith(path)) {
            if(throwNotFoundException) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND, 
                    "invalid object path. no object found",
                    new BasicException.Parameter("request-path", path),
                    new BasicException.Parameter("object-path", object.getResourceIdentifier())
                );
            } else {
                return null;
            }
        }
        SysLog.detail("retrieved object", object);
        return object;
    }

    /**
     * Determines whether the given Model class is configuratively excluded from persistency or not
     * 
     * @return <code>true</code> if the given Model class is not configuratively excluded from persistency 
     */
    protected boolean isNotExcludedFromPersistency (
        String modelClass
    ){
        return !this.nonPersistentFeatures.contains(modelClass);
    }
        
    /**
     * Tells whether the given feature is persistent
     * 
     * @param featureDef the features meta-data
     * 
     * @return <code>true</code> if the given feature is persistent
     */
    public boolean isPersistent(
        ModelElement_1_0 featureDef
    ) throws ServiceException{
        return 
            isNotExcludedFromPersistency(featureDef.getQualifiedName()) &&
            Persistency.getInstance().isPersistentAttribute(featureDef);
    }

    protected Model_1_0 getModel(
    ) {
        return Model_1Factory.getModel();
    }

    protected Set<String> getAllSubtypes(
        String qualifiedTypeName
    ) throws ServiceException {
        if(qualifiedTypeName == null) return null;
        ModelElement_1_0 classDef = getModel().getDereferencedType(qualifiedTypeName);
        if("org:openmdx:base:BasicObject".equals(qualifiedTypeName)) {
            return null;
        } else {
            Set<String> allSubtypes = new HashSet<String>();
            for(Object path : classDef.objGetList("allSubtype")) {
                allSubtypes.add(((Path)path).getLastSegment().toClassicRepresentation());
            }
            return allSubtypes;
        }
    }
        
    /**
     * Lookup a feature's meta-data
     * 
     * @param objectClass
     * 
     * @param featureName
     * @return the feature's meta-data
     */
    private ModelElement_1_0 getFeatureDef(
        String objectClass,
        String featureName
    ){
        try {
            return getModel().getElement(objectClass).objGetMap("allFeature").get(featureName);
        } catch(Exception exception){
            return null;
        }
    }

    /**
     * Touch object feature (set empty value). The feature must be a modeled 
     * feature which are either non-derived or system attributes (otherwise 
     * the feature is not touched).
     * 
     * @param object
     * @param featureName
     * @param multiplicity
     * 
     * @throws ServiceException
     */
    private void touchAttributes(
        MappedRecord object,
        String featureName, 
        Multiplicity multiplicity
    ) throws ServiceException {
        Object_2Facade facade = Facades.asObject(object);
        ModelElement_1_0 featureDef = getFeatureDef(facade.getObjectClass(), featureName);
        if(featureDef != null && isPersistent(featureDef)) {
            facade.attributeValues(
                featureDef.getName(), 
                multiplicity
            );
        }
    }

    /**
     * Transfers attributes of 'attributeType' of objects contained in 'resultSet' 
     * to the 'objects'. 'objects' is a List to maintain the sort order from the
     * db. The objects are added in the order retrieved. 
     * <p> 
     * If 'resultSet' contains object_objectIds which are not consecutive, an 
     * exception is thrown. 
     * <p>
     * The resultSet must hold the following preconditions:
     * <ul>
     *   <li> all object_referencedIds must all be the same. </li>
     *   <li> resultSet must be sorted by object_objectId and object_name </li>
     * </ul>
     *
     * @param reference object reference used to create path of returned objects.
     *        objectPath = reference + objectId.
     *
     * @param rs jdbc result set containing the objects.
     *
     * @param objectList a list of DataproviderObjects contained in the result set rs.
     *
     * @param attributeType attribute type [T_STRING|T_DECIMAL|T_BINARY].
     *
     * @param attributeSelector requests attribute selector.
     *
     * @param attributeSpecifiers as map.
     *
     * @return true, if there are more rows in rs which are not read yet.
     *
     */
    protected boolean getObjects(
        Connection conn,
        DbObject dbObject,
        ResultSet rs,
        List<ObjectRecord> objects,
        short attributeSelector,
        Map<String,AttributeSpecifier> attributeSpecifiers,
        boolean objectClassAsAttribute,
        int position,
        int lastPosition,
        int lastRowCount,
        int maxObjectsToReturn,
        String primaryObjectClass
    ) throws ServiceException, SQLException {
        // set row count of rs to corresponding position
        FastResultSet frs = this.setPosition(
            rs,
            position,
            lastPosition,
            lastRowCount,
            dbObject.getIndexColumn() != null, 
            dbObject.getConfiguration()
        );
        boolean hasMore = frs != null;
        // get objects
        Map<Path,MappedRecord> candidates = new HashMap<Path,MappedRecord>();
        String previousObjectId = null;
        Map<String,ModelElement_1_0> featureDefs = null;
        ObjectRecord current = null;
        Set<Integer> processedIdxs = new HashSet<Integer>();
        String objectClass = null;
        while(hasMore) {
            Path objectPath = dbObject.getResourceIdentifier(frs);
            Path reference = objectPath.getParent();
            String objectId = objectPath.getLastSegment().toClassicRepresentation();
            int idx = dbObject.getIndex(frs);
            // try to get attribute definitions
            if(idx == 0) {
                // Get OBJECT_CLASS if not supplied
                objectClass = primaryObjectClass == null ? dbObject.getObjectClass(frs) : primaryObjectClass;
                featureDefs = null;
            } else {
                objectClass = primaryObjectClass;
            }
            if(featureDefs == null && objectClass != null) try {
                featureDefs = getModel().getElement(objectClass).objGetMap("allFeatureWithSubtype");
            } catch(Exception e) {
                SysLog.trace(
                    "class not found in model fall back to non model-driven mode",
                    objectClass
                    );
            }
            // skip to next object? If yes, create new one and add it to list
            if(
                !objectId.equals(previousObjectId) ||
                !reference.equals(current.getResourceIdentifier().getParent())
                ) {
                processedIdxs.clear();
                // add empty object to result set if necessary
                // check if this object was already read
                if(candidates.containsKey(objectPath)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.MEDIA_ACCESS_FAILURE, 
                        "Result set contains duplicates or non consecutive object ids",
                        new BasicException.Parameter("path", objectPath)
                    );
                }
                if(candidates.size() >= maxObjectsToReturn) {
                    return hasMore;
                }
                current = Facades.newObject(
                    objectPath,
                    objectClass
                ).getDelegate();
                objects.add(current);
                candidates.put(objectPath, null);
                previousObjectId = objectId;
            }
            // Duplicate indices are allowed. However, they are skipped
            if(!processedIdxs.contains(Integer.valueOf(idx))) {
                Object_2Facade currentFacade = Facades.asObject(current);
                boolean multivaluedAttributesRequested = areMultivaluedAttributesRequested(
                    attributeSpecifiers,
                    featureDefs
                );
                // Iterate through object attributes and add values
                for(String columnName: frs.getColumnNames()) {
                    String featureName = this.getFeatureName(columnName);  
                    ModelElement_1_0 featureDef = featureDefs == null ? null : featureDefs.get(featureName);
                    Multiplicity multiplicity = featureDef == null ? null : ModelHelper.getMultiplicity(featureDef);
                    // Always include reference columns. Otherwise dbObject decides
                    if(dbObject.includeColumn(columnName)) {
                        // Check whether attribute must be added
                        boolean addValue;
                        if(SystemAttributes.OBJECT_CLASS.equals(featureName)){
                            addValue = true;
                        } else {
                            switch(attributeSelector) {
                                case AttributeSelectors.NO_ATTRIBUTES:
                                    addValue = false;
                                    break;
                                case AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES:
                                    if(featureDef == null) {
                                        // Return in case the type is unknown
                                        addValue = true;
                                    } else if(multiplicity.isMultiValued()) {
                                        // Return multi-valued features if we have at least one attribute specifier
                                        addValue = multivaluedAttributesRequested || this.getEmbeddedFeature(featureName) != null;
                                    } else if(multiplicity.isStreamValued()){
                                        // Return stream-valued features upon request on
                                        addValue = attributeSpecifiers.containsKey(featureName);
                                    } else {
                                        // Return single-valued features
                                        addValue = true;
                                    }
                                    break;
                                case AttributeSelectors.ALL_ATTRIBUTES:
                                    // Must also return stream features
                                    addValue = true;
                                    break;
                                case AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES:
                                    addValue = SYSTEM_ATTRIBUTES.contains(featureName) || attributeSpecifiers.containsKey(featureName);
                                    break;
                                default:
                                    // Illegal Attribute Specifier
                                    addValue = false;
                            }
                        }
                        // Add value
                        if(addValue) {
                            String featureType;
                            if(featureDef == null) {
                                featureType = null;
                            } else try {
                                featureType = getModel().getElementType(featureDef).getQualifiedName();
                            } catch (Exception exception) {
                                throw new ServiceException(
                                    exception,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.INVALID_CONFIGURATION,
                                    "Unable to determine the element type",
                                    new BasicException.Parameter("feature", featureDef.jdoGetObjectId())
                                );
                            }
                            Object val = frs.getObject(columnName);
                            // Embedded attribute? If yes derive idx from column name suffix (instead of slice index)
                            int valueIdx = idx;
                            boolean isEmbedded;
                            if(this.getEmbeddedFeature(featureName) != null) {
                                valueIdx = Integer.valueOf(columnName.substring(columnName.lastIndexOf('_') + 1)).intValue();
                                isEmbedded = true;
                            } else {
                                isEmbedded = false;
                            }
                            // String, Clob
                            if(this.isClobColumnValue(val)) {
                                val = this.getClobColumnValue(val, featureName, featureDef);
                                if(val instanceof Reader || val instanceof CharacterLargeObject) {
                                    Object values = currentFacade.attributeValues(
                                        featureName,
                                        multiplicity
                                    );
                                    this.setValue(
                                        values,
                                        valueIdx,
                                        val,
                                        isEmbedded
                                    );
                                }  else if (
                                    // class type || PrimitiveTypes.PATH
                                    isReferenceFeature(conn, frs, featureName) ||
                                    ((featureType == null) && ((String)val).startsWith(URI_1Marshaller.OPENMDX_PREFIX)) ||
                                    ((featureType == null) && ((String)val).startsWith("xri:")) ||
                                    ((featureType != null) && (PrimitiveTypes.OBJECT_ID.equals(featureType) || getModel().isClassType(featureType)))
                                ) {
                                    if(this.isUseNormalizedReferences()) {
                                        //
                                        // Get path from normalized form (p$$<feature>_rid, p$$<feature>_oid
                                        //
                                        String referenceColumn = this.getColumnName(
                                            conn, 
                                            toRid(featureName), 
                                            0,
                                            false, 
                                            false, 
                                            true // markAsPrivate
                                        );
                                        String objectIdColumn = this.getColumnName(
                                            conn, 
                                            toOid(featureName), 
                                            0, 
                                            false,
                                            false,
                                            true // markAsPrivate
                                        );
                                        if(
                                            (frs.getColumnNames().contains(referenceColumn)) &&
                                            (frs.getColumnNames().contains(objectIdColumn))
                                        ) {
                                            //
                                            // Reference is stored in format p$$<feature>__rid, p$$<feature>__oid
                                            //
                                            Object rid = frs.getObject(referenceColumn);
                                            if(rid != null) {
                                                String oid = (String)frs.getObject(objectIdColumn);
                                                Object values = currentFacade.attributeValues(
                                                    featureName, 
                                                    multiplicity
                                                );
                                                Object value = this.getReference(
                                                    conn,
                                                    rid
                                                ).getChild(
                                                    this.getObjectId(oid)
                                                );
                                                this.setValue(
                                                    values, 
                                                    valueIdx, 
                                                    value,
                                                    isEmbedded
                                                );
                                            }
                                        } else {
                                            //
                                            // Reference is stored in compressed form in column
                                            //
                                            String ref = (String)frs.getObject(
                                                this.getColumnName(
                                                    conn, 
                                                    featureName, 
                                                    0, 
                                                    false, 
                                                    true, 
                                                    false // markAsPrivate
                                                )
                                            );
                                            Path resourceIdentifier;
                                            if (this.getDatabaseConfiguration().normalizeObjectIds()) {
                                                resourceIdentifier = this.getDatabaseConfiguration().buildResourceIdentifier(
                                                    ref, 
                                                    false // reference
                                                );
                                            } else {
                                                String rid = ref.substring(0, ref.lastIndexOf("/"));
                                                String oid = ref.substring(ref.lastIndexOf("/") + 1);
                                                resourceIdentifier = this.getReference(
                                                    conn,
                                                    rid
                                                ).getChild(
                                                    oid
                                                );
                                            }
                                            Object values = currentFacade.attributeValues(
                                                featureName,
                                                multiplicity
                                            );
                                            this.setValue(
                                                values, 
                                                valueIdx, 
                                                resourceIdentifier,
                                                isEmbedded
                                            );
                                        }
                                    }
                                    // get path from non-normalized, stringified form
                                    else {
                                        Object values = currentFacade.attributeValues(
                                            featureName,
                                            multiplicity
                                        );
                                        try {
                                            this.setValue(
                                                values, 
                                                valueIdx, 
                                                this.internalizePathValue((String)val),
                                                isEmbedded
                                            );
                                        } 
                                        catch(Exception e) {
                                            this.setValue(
                                                values, 
                                                valueIdx, 
                                                val,
                                                isEmbedded
                                            );
                                        }
                                    }    
                                } 
                                // string
                                else {
                                    if(val != null) {
                                        if(SystemAttributes.OBJECT_CLASS.equals(featureName)) {
                                            currentFacade.getValue().setRecordName(
                                                ((String)val).trim()
                                                );
                                            // Store object class as attribute
                                            if(objectClassAsAttribute) {
                                                Object target = currentFacade.attributeValues(
                                                    featureName,
                                                    Multiplicity.LIST
                                                );
                                                this.setValue(
                                                    conn,
                                                    target,
                                                    valueIdx,
                                                    val,
                                                    featureType,
                                                    isEmbedded
                                                );                                                
                                            }
                                        } else {
                                            Object target = currentFacade.attributeValues(
                                                featureName,
                                                multiplicity
                                            );
                                            try {
                                                this.setValue(
                                                    conn,
                                                    target,
                                                    valueIdx,
                                                    val,
                                                    featureType,
                                                    isEmbedded
                                                );
                                            } catch(IndexOutOfBoundsException exception) {
                                                throw new ServiceException(
                                                    exception,
                                                    BasicException.Code.DEFAULT_DOMAIN,
                                                    BasicException.Code.ASSERTION_FAILURE,
                                                    "Index out ouf bounds",
                                                    new BasicException.Parameter("object", currentFacade.getDelegate()),
                                                    new BasicException.Parameter("feature", featureName),
                                                    new BasicException.Parameter("multiplicity", multiplicity),
                                                    new BasicException.Parameter("target", target),
                                                    new BasicException.Parameter("index", valueIdx),
                                                    new BasicException.Parameter("value", val)
                                                );
                                            }
                                        }
                                    }
                                }
                            } 
                            // byte[], Blob
                            else if(this.isBlobColumnValue(val)) {
                                Object value = this.getBlobColumnValue(val, featureName, featureDef);
                                Object values = currentFacade.attributeValues(
                                    featureName, 
                                    multiplicity
                                );
                                this.setValue(
                                    values, 
                                    valueIdx, 
                                    value,
                                    isEmbedded
                                );
                            } 
                            // Null
                            else if(val == null) {
                                // Touch feature if value is null and feature is member of class
                                if(valueIdx == 0) {
                                    if(featureName.lastIndexOf(':') > 0) {
                                        throw new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.ASSERTION_FAILURE, 
                                            "Scoped features not supported",
                                            new BasicException.Parameter("object", current),
                                            new BasicException.Parameter("attribute", featureName)
                                        );                                        
                                    }
                                    this.touchAttributes(
                                        current,
                                        featureName, 
                                        multiplicity
                                    );
                                }
                            }
                            // Other types
                            else {
                                this.setValue(
                                    conn,
                                    currentFacade.attributeValues(featureName, multiplicity),
                                    valueIdx,
                                    val,
                                    featureType,
                                    isEmbedded
                                );
                            }
                        }
                    }
                }
            }
            hasMore = frs.next();
            processedIdxs.add(Integer.valueOf(idx));
        }
        return hasMore;
    }

    /**
     * Determine whether multi-valued attributes requested
     * 
     * @param attributeSpecifiers
     * @param featureDefs
     * 
     * @throws ServiceException
     */
    private boolean areMultivaluedAttributesRequested(
        Map<String, AttributeSpecifier> attributeSpecifiers,
        Map<String, ModelElement_1_0> featureDefs
    ) throws ServiceException {
        if(featureDefs != null){
            for(String featureName : attributeSpecifiers.keySet()) {
                ModelElement_1_0 featureDef = featureDefs.get(featureName);
                if(featureDef != null){
                    if(
                        ModelHelper.getMultiplicity(featureDef).isMultiValued() &&
                        this.getEmbeddedFeature(featureName) == null
                    ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isReferenceFeature(
        Connection conn,
        FastResultSet frs,
        String featureName
    ) throws ServiceException {
        return this.isUseNormalizedReferences() && (
            frs.getColumnNames().contains(this.getColumnName(conn, toRid(featureName), 0, false, false, true)) ||
            frs.getColumnNames().contains(this.getColumnName(conn, featureName + "Parent", 0, false, false, true))
        );
    }

    protected void setValue(
        Object target,
        int index,
        Object value,
        boolean allowFilling
    ) {
        if(target instanceof SparseArray) {
            ((SparseArray)target).put(
                Integer.valueOf(index), 
                value
            );                                        
        } else {
            List values = (List)target;
            // Fill value up to requested index if allowFilling = true
            if(allowFilling) {
                while(index > values.size()) {
                    values.add(value);
                }
            }
            if(index == values.size()) {
                values.add(value);
            } else {
                values.set(
                    index, 
                    value
                );
            }
        }
    }

    protected void setValue(
        Connection conn,      
        Object target,
        int index,
        Object val,
        String featureType,
        boolean isEmbedded
    ) throws ServiceException {
        if(
            PrimitiveTypes.BOOLEAN.equals(featureType) ||
            val instanceof Boolean
        ) {
            //
            // org:: w3c::boolean
            //
            this.setValue(
                target, 
                index, 
                this.getBooleanMarshaller().unmarshal(val, conn),
                isEmbedded
            );
        } else if(
            PrimitiveTypes.DATE.equals(featureType) &&
            val instanceof Timestamp
            ) {
            // Some Jdbc drivers / databases may return java.sql.Timestamp instead
            // of java.sql.Date even if the column type is Date. Force the conversion
            // from Timestamp to date.
            this.setValue(
                target, 
                index, 
                this.getCalendarMarshaller().unmarshal(val.toString().substring(0, 10)),
                isEmbedded
            );
        } else if(
            PrimitiveTypes.DATE.equals(featureType) ||
            PrimitiveTypes.DATETIME.equals(featureType) ||
            val instanceof Timestamp ||
            val instanceof Time
        ) {
            //
            // org::w3c::date
            // org::w3c::dateTime
            //
            this.setValue(
                target, 
                index, 
                this.getCalendarMarshaller().unmarshal(val),
                isEmbedded
            );
        } else if(PrimitiveTypes.DURATION.equals(featureType)) {
            //
            // org::w3c::duration
            //
            this.setValue(
                target, 
                index, 
                this.getDurationMarshaller().unmarshal(val),
                isEmbedded
            );
        } else if (PrimitiveTypes.ANYURI.equals(featureType)) {
            //
            // URIs
            //
            this.setValue(
                target, 
                index, 
                URIMarshaller.NORMALIZE.marshal(val),
                isEmbedded
            );
        } else if(val instanceof Number) {
            //
            // openMDX 1 clients expect all numbers to be returned 
            // as BigIntegers
            //
            Object value = val instanceof BigDecimal ? 
                val : 
                new BigDecimal(val.toString());
            this.setValue(
                target, 
                index, 
                value,
                isEmbedded
            );
        } else if(val instanceof String) {
            //
            // default 
            //
            String databaseProductName = this.getDatabaseProductName(conn);
            if("Oracle".equals(databaseProductName)) {
                // Oracle maps empty strings to null. Fill list with empty strings up to index.                
                if(target instanceof List) {
                    List values = (List)target;
                    while(index > values.size()) {
                        values.add("");
                    }
                }
            }            
            this.setValue(
                target, 
                index, 
                val,
                isEmbedded
            );
        } else {
            //
            // unknown 
            //
            if(PrimitiveTypes.STRING.equals(featureType)) {
                // As fall-back get value by invoking getValue() on val
                try {
                    Method getValueMethod = val.getClass().getMethod("getValue");
                    this.setValue(
                        target, 
                        index, 
                        getValueMethod.invoke(val),
                        isEmbedded
                    );
                    return;
                } catch(Exception ignore) { /* ignore */}
            }
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED, 
                "invalid column type. Supported are [Number|String|byte[]|Blob|Timestamp|Time]",
                new BasicException.Parameter("featureType", featureType),
                new BasicException.Parameter("columnType", val.getClass().getName())
            );  
        }
    }

    protected void filterToSqlClauses(
        Connection conn,
        DbObject dbObject,
        boolean statedObject,
        String viewAliasName,
        String view1,
        String view2,
        JoinType joinType,
        String joinColumn,
        boolean fixedViewAliasName,
        ModelElement_1_0 referencedType,
        List<FilterProperty> allFilterProperties,
        List<FilterProperty> primaryFilterProperties,
        List<String> includingClauses, 
        List<List<Object>> includingClausesValues, 
        List<String> exludingClauses, 
        List<List<Object>> exludingClausesValues
    ) throws ServiceException {
        List<Object> filterValues = new ArrayList<Object>();
        // Positive single-valued filter
        includingClauses.add(
            Database_2.this.filterToSqlClause(
                conn,
                dbObject,
                statedObject,
                viewAliasName,
                fixedViewAliasName,
                view1, 
                true, // joinViewIsPrimary
                dbObject.getIndexColumn() != null, // joinViewIsIndexed
                joinType,
                joinColumn, 
                referencedType, 
                primaryFilterProperties, 
                false, // negate
                filterValues
            )
        );
        includingClausesValues.add(filterValues);
        // Negative single-valued filter
        filterValues = new ArrayList<Object>();
        includingClauses.add(
            Database_2.this.filterToSqlClause(
                conn,
                dbObject,
                statedObject,
                viewAliasName,
                fixedViewAliasName, 
                view1,
                true,  // joinViewIsPrimary
                dbObject.getIndexColumn() != null, // joinViewIsIndexed
                joinType,
                joinColumn, 
                referencedType, 
                primaryFilterProperties, 
                true, // negate
                filterValues
            )
        );
        includingClausesValues.add(filterValues);

        // WHERE clauses for multi-valued filter properties
        for(
            Iterator<FilterProperty> i = allFilterProperties.iterator();
            i.hasNext();
        ) {
            FilterProperty p = i.next();
            if(!primaryFilterProperties.contains(p)) {
                // Positive clause
                filterValues = new ArrayList<Object>();
                includingClauses.add(
                    Database_2.this.filterToSqlClause(
                        conn,
                        dbObject,
                        statedObject,
                        toMultiValueView(viewAliasName),
                        fixedViewAliasName, 
                        view2, 
                        false, // joinViewIsPrimary
                        dbObject.getIndexColumn() != null, // joinViewIsIndexed
                        joinType,
                        joinColumn,
                        referencedType,
                        Collections.singletonList(p), 
                        false, // negate
                        filterValues
                    )
                );
                includingClausesValues.add(filterValues);
                // Negative clauses are only required if maxSlicesPerObject > 0. In this case
                // non-matching slices have to be subtracted from the plus result set.
                filterValues = new ArrayList<Object>();
                String excludingClause = Database_2.this.filterToSqlClause(
                    conn,
                    dbObject,
                    statedObject,
                    toMultiValueView(viewAliasName),
                    fixedViewAliasName, 
                    view2,
                    false, // joinViewIsPrimary 
                    dbObject.getIndexColumn() != null, // joinViewIsIndexed
                    joinType,
                    joinColumn,
                    referencedType,
                    Collections.singletonList(p), 
                    true, // negate
                    filterValues
                );
                if(!excludingClause.isEmpty()) {
                    exludingClauses.add(
                        excludingClause
                    );
                    exludingClausesValues.add(filterValues);
                }
            }
        }
    }

    /**
     * @param singleValueView
     * @return
     */
    protected String toMultiValueView(String singleValueView) {
        return singleValueView + "v";
    }

    /**
     * Generate a SQL clause corresponding to filter properties for the generic
     * tables. The generated SQL clause if of the form: ((object_name = ?) AND (object_val operator ?)) AND ...
     * <p>
     * <ul>
     *  <li> If minus is true, clauses are only generated for filter properties with operator FOR_ALL.</li>
     *  <li> If minus is false then clauses are generated for all filter properties.</li>
     * <p>
     * Clauses are only generated if the property type matches attributeType. The clause is
     * generated in the form of a prepared statement with ? as place holders. The corresponding
     * values are stored in filterValues. The type of a filter property is determined 
     * of its first value (get(0)).
     * 
     * @param conn
     * @param dbObject
     * @param statedObject 
     * @param viewAliasName
     * @param fixedViewAliasName avoid mix-in view alias references
     * @param view
     * @param viewIsPrimary
     * @param viewIsIndexed
     * @param joinType 
     * @param joinColumn
     * @param referencedType
     * @param filterProperties
     * @param negate
     * @param statementParameters
     * @return the SQL clause
     * 
     * @throws ServiceException
     */
    protected String filterToSqlClause(
        Connection conn,
        DbObject dbObject,
        boolean statedObject,
        String viewAliasName,
        boolean fixedViewAliasName,
        String view,
        boolean viewIsPrimary,
        boolean viewIsIndexed,
        JoinType joinType,
        String joinColumn,
        ModelElement_1_0 referencedType, 
        List<FilterProperty> filterProperties, 
        boolean negate, 
        List<Object> statementParameters
    ) throws ServiceException {
        StringBuilder clause = new StringBuilder();
        List<Object> clauseValues = new ArrayList<Object>();    
        boolean hasProperties = false;
        String operator = "";
        /**
         * Generate clause for all filter properties:
         * If negate --> negate(expr0) OR negate(expr1) ...
         * If !negate --> expr0 AND expr1 ... 
         */    
        List<ModelElement_1_0> filterPropertyDefs = Database_2.this.getFilterPropertyDefs(
            referencedType, 
            filterProperties
        );
        for(int i = 0; i < filterProperties.size(); i++) {
            FilterProperty filterProperty = filterProperties.get(i);
            ModelElement_1_0 filterPropertyDef = filterPropertyDefs.get(i);            
            /**
             * FOR_ALL --> all attribute values must match --> all slices must match
             * THERE_EXISTS --> at least one attribute value must match --> at least one row must match --> subtract all rows which do not match
             */
            if(filterProperty.quantor() == (negate ? Quantifier.FOR_ALL : Quantifier.THERE_EXISTS).code()) {
                // For embedded features the clause is of the form (expr0 OR expr1 OR ... OR exprN)
                // where N is the upper bound for the embedded feature 
                int upperBound = this.getEmbeddedFeature(filterProperty.name()) != null 
                    ? this.getEmbeddedFeature(filterProperty.name()).intValue() 
                    : 1;
                clause.append(operator).append("(");
                for(int idx = 0; idx < upperBound; idx++) {
                    String columnName = this.getColumnName(
                        conn, 
                        filterProperty.name(), 
                        idx, 
                        upperBound > 1, 
                        true, 
                        false // markAsPrivate
                    );
                    boolean mixInView = !fixedViewAliasName && viewIsPrimary && viewIsIndexed; 
                    columnName = viewAliasName + (
                        mixInView ? "m." : "."
                    ) + columnName;
                    if(idx > 0) {
                        clause.append(filterProperty.quantor() ==  Quantifier.FOR_ALL.code() ? " AND " : " OR ");
                    }
                    clause.append("(");
                    clause.append(
                        this.filterPropertyToSqlClause(
                            conn,
                            dbObject,
                            statedObject,
                            dbObject.getReference(),
                            viewAliasName,
                            filterProperty,
                            filterPropertyDef, 
                            negate && !viewIsPrimary, 
                            columnName, 
                            clauseValues, 
                            referencedType
                        )
                    );
                    if(
                        viewIsPrimary && 
                        (filterProperty.quantor() == Quantifier.FOR_ALL.code()) &&
                        (filterProperty.getValues().length == 0 || !(filterProperty.getValue(0) instanceof QueryFilterRecord))
                    ) {
                        clause.append(" OR (").append(columnName).append(" IS NULL)");
                    }
                    clause.append(")");
                }     
                clause.append(")");
                hasProperties = true;
            }
            if(hasProperties) {
                operator = negate && !viewIsPrimary ? " OR " : " AND ";
            }
        }
        if(hasProperties) {
            if(viewIsPrimary) {
                statementParameters.addAll(clauseValues);
                return clause.toString();
            } else {
                // View is the secondary db object containing the multi-valued columns
                // of an object. This is why reference clause 2 must be used for selection
                if(dbObject.getConfiguration().hasDbObject2()) {
                    statementParameters.addAll(clauseValues);
                    switch(joinType) {
                        case NONE:
                            return 
                                "(SELECT " + dbObject.getObjectIdColumn().get(0) + " FROM " + (view.startsWith("SELECT") ? "(" + view + ") " + viewAliasName : view + " " + viewAliasName + " ") +
                                " WHERE (" + clause + "))";
                        case SPECIFIED_COLUMN_WITH_OBJECT_ID:
                            return
                                "(" + (negate ? "NOT " : "") +  
                                "EXISTS (SELECT 1 FROM " + (view.startsWith("SELECT") ? "(" + view + ") " + viewAliasName : view + " " + viewAliasName + " ") +
                                " WHERE " + viewAliasName + "." + dbObject.getObjectIdColumn().get(0) + " = " + joinColumn + " AND " + 
                                " (" + clause + ")))";                                  
                        case OBJECT_RID_AND_OID:
                            return 
                                (negate ? "(NOT " : "(") +  
                                "EXISTS (SELECT 1 FROM " + (view.startsWith("SELECT") ? "(" + view + ")" : view) + " " + viewAliasName +
                                " WHERE " + viewAliasName.substring(0, viewAliasName.length() - 1) + "." + dbObject.getObjectIdColumn().get(0) + " = " + viewAliasName +  "." + dbObject.getObjectIdColumn().get(0) + " AND " +
                                viewAliasName.substring(0, viewAliasName.length() - 1) + "." + dbObject.getReferenceColumn().get(0) + " = " + viewAliasName +  "." + dbObject.getReferenceColumn().get(0) + " AND " +
                                "(" + clause + ")))";
                         default:
                             throw new ServiceException(
                                 BasicException.Code.DEFAULT_DOMAIN,
                                 BasicException.Code.BAD_PARAMETER,
                                 "Unexpected Join Type",
                                new BasicException.Parameter("JoinType", joinType) 
                             );
                    }
                } else {
                    statementParameters.addAll(dbObject.getReferenceValues());
                    statementParameters.addAll(clauseValues);
                    switch(joinType) {
                    case NONE:
                        return 
                            "(SELECT " + dbObject.getObjectIdColumn().get(0) + " FROM " + (view.startsWith("SELECT") ? "(" + view + ") " + viewAliasName : view + " " + viewAliasName + " ") +
                            " WHERE " + dbObject.getReferenceClause() + 
                            " AND (" + clause + "))";            
                    case SPECIFIED_COLUMN_WITH_OBJECT_ID:
                        return 
                            "(" + joinColumn + " " + (negate ? "NOT" : "") +  
                            " IN (SELECT " + dbObject.getObjectIdColumn().get(0) + " FROM " + (view.startsWith("SELECT") ? "(" + view + ") " + viewAliasName : view + " " + viewAliasName + " ") +
                            " WHERE " + dbObject.getReferenceClause() + 
                            " AND (" + clause + ")))";            
                     default:
                         throw new ServiceException(
                             BasicException.Code.DEFAULT_DOMAIN,
                             BasicException.Code.BAD_PARAMETER,
                             "Unexpected Join Type",
                            new BasicException.Parameter("JoinType", joinType) 
                         );
                    }
                }
            }
        } else {
            return "";
        }
    }

    /**
     * Add an IS_IN or IS_NOT_IN clause
     * 
     * @param connection 
     * @param dbObject 
     * @param columnName
     * @param negation true for IS_NOT_IN, false for IS_IN,
     * @param filterPropertyDef
     * @param clause
     * @param clauseValues
     * @param values
     */
    protected void isInToSqlClause(
        Connection connection,
        DbObject dbObject,
        String columnName,
        boolean negation,
        ModelElement_1_0 filterPropertyDef, 
        StringBuilder clause, 
        List<Object> clauseValues, 
        Object[] values
    ) throws ServiceException {
        clause.append(columnName).append(negation ? " NOT IN (" : " IN (");
        String separator = "";
        for(Object value : values){
            clause.append(
                separator
            ).append(
                getPlaceHolder(connection, value)
            );
            clauseValues.add(
                this.externalizeStringValue(columnName, value)
            );
            separator = ", ";
        }
        clause.append(")");
    }
    
    /**
     * Fill IS_LIKE or IS_UNLIKE clause
     * 
     * @param connection 
     * @param dbObject 
     * @param like 
     * @param filterPropertyDef
     * @param clause
     * @param clauseValues
     * @param value 
     * @param parent 
     * @param matchingPatterns
     */
    protected void isLikeToSqlClause(
        Connection connection,
        DbObject dbObject,
        String columnName,
        boolean like,
        ModelElement_1_0 filterPropertyDef, 
        StringBuilder clause,
        Collection<Object> clauseValues, 
        Path value
    ) throws ServiceException {
        Set<Path> matchingPatterns = new HashSet<Path>();
        boolean includeSubTree = "%".equals(value.getLastSegment().toClassicRepresentation());
        // If path ends with % get the set of matching types
        Path path = value;
        if(includeSubTree) {
            path = value.getParent();
            for(
                Iterator<DbObjectConfiguration> k = this.getDatabaseConfiguration().getDbObjectConfigurations().iterator(); 
                k.hasNext(); 
                ) {
                DbObjectConfiguration dbObjectConfiguration = k.next();
                if(
                    (dbObjectConfiguration.getType().size() >= path.size()) &&
                    path.isLike(dbObjectConfiguration.getType().getPrefix(path.size()))
                ) {
                    // Replace type pattern by filter value pattern
                    Path type = dbObjectConfiguration.getType();
                    String[] pattern = new String[type.size()]; 
                    int i = 0;
                    for(XRISegment p : type.getSegments()){
                        String c = p.toString();
                        pattern[i++] = ":*".equals(c) ? "%" : c;
                    }
                    matchingPatterns.add(new Path(pattern));
                }
            }
        } else {
            matchingPatterns.add(value);           
        }
        this.isLikeToSqlClause(
            connection, 
            dbObject, 
            columnName, 
            like, 
            filterPropertyDef, 
            clause, 
            clauseValues, 
            path, 
            value,
            matchingPatterns
        );
    }

    /**
     * Fill IS_LIKE or IS_UNLIKE clause
     * 
     * @param connection 
     * @param dbObject 
     * @param like 
     * @param filterPropertyDef
     * @param clause
     * @param clauseValues
     * @param path 
     * @param value 
     * @param matchingPatterns
     */
    protected void isLikeToSqlClause(
        Connection connection,
        DbObject dbObject,
        String columnName,
        boolean like,
        ModelElement_1_0 filterPropertyDef, 
        StringBuilder clause, 
        Collection<Object> clauseValues, 
        Path path, 
        Path value, 
        Set<Path> matchingPatterns
    ) throws ServiceException {
        String operator = "";
        for(Path pattern : matchingPatterns) {
            String externalized = this.externalizePathValue(
                connection, 
                path.getDescendant(pattern.getSuffix(path.size()))
            );
            clause.append(operator).append(like ? "(" : "(NOT (").append(columnName).append(" LIKE ? ").append(getEscapeClause(connection)).append(like ? ")" : "))");              
            int pos = externalized.indexOf("%");
            if(pos >= 0) {
                externalized = externalized.substring(0, pos+1);
            }
            if(externalized.startsWith("xri:")) {
                if(externalized.endsWith("***")) {
                    externalized = externalized.substring(0, externalized.length()-3) + "%";
                }
                pos = externalized.indexOf("**");
                if(pos >= 0) {
                    if(path != value && pos == externalized.length() - 2) {
                        externalized = externalized.substring(0, pos) + "%";
                    } else throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED, 
                        "only (***|%) wildcards at the end supported",
                        new BasicException.Parameter("value", value),
                        new BasicException.Parameter("path", externalized),
                        new BasicException.Parameter("position", pos)                          
                    );
                }
            } else if(externalized.startsWith("spice:")) {
                for(XRISegment p : value.getSegments()) {
                    String c = p.toString();
                    if(c.startsWith(":") && c.endsWith("*")) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED, 
                            "path component pattern ':<pattern>*' not supported",
                            new BasicException.Parameter("path", externalized)                   
                        );
                    }
                }
            }
            clauseValues.add(externalized);
            operator = like ? " OR " : " AND ";
        }        
    }

    /**
     * Apply a context provider to a provider indifferent XRI
     * 
     * @param xri
     * @param context
     * 
     * @return the (optional) provider
     */
    protected Path applyProvider(
        Path xri,
        Path context
    ){
        if(xri.size() > 2) {
            String requestedProvider = xri.getSegment(2).toClassicRepresentation();
            if(":*".equals(requestedProvider) && context != null && context.size() > 2){
                return new Path(
                    new String[]{
                        xri.getSegment(0).toClassicRepresentation(),
                        xri.getSegment(1).toClassicRepresentation(),
                        context.getSegment(2).toClassicRepresentation()
                    }
                ).getDescendant(
                    xri.getSuffix(3)
                );
            }
        }
        return xri;
    }
    
    /**
     * Appends an SQL clause to 'clause' according to filter property 'p'.
     * if 'negate' is true, then the operation is negated. The clause is
     * generated for SQL column with 'columnName'. The clause is generated
     * prepared statement using ? as place holders. The corresponding 
     * values are added to filterValues. The generated clause is of the
     * form (columnName operator literal). ANY or EACH are not handled
     * by this method.
     * @param statedObject 
     * @param referencedType 
     */
    private String filterPropertyToSqlClause(
        Connection conn,
        DbObject dbObject,
        boolean statedObject,
        Path reference,
        String viewAliasName,
        FilterProperty filterProperty,
        ModelElement_1_0 filterPropertyDef,
        boolean negate, 
        String columnName, 
        List<Object> clauseValues, 
        ModelElement_1_0 referencedType
    ) throws ServiceException {
        StringBuilder clause = new StringBuilder();
        final ConditionType operator;
        final Quantifier quantor;
        if(negate) {
            quantor = Quantifier.valueOf(Quantifier.invert(filterProperty.quantor()));
            operator = ConditionType.valueOf(ConditionType.invert(filterProperty.operator()));
        } else {
            quantor = Quantifier.valueOf(filterProperty.quantor());
            operator = ConditionType.valueOf(filterProperty.operator());
        }
        switch(operator) {

            /**
             * Evaluate the following:
             * THERE_EXISTS v IN A: v NOT IN Q (Special: if Q={} ==> true, iff A<>{}, false otherwise)
             * FOR_ALL v IN A: v NOT IN Q (Special: if Q={} ==> true) 
             */
            case IS_NOT_IN:
                // Q = {}
                if(filterProperty.getValues().length == 0) {
                    if(quantor == Quantifier.FOR_ALL) {
                        clause.append("(1=1)");
                    } else {
                        clause.append("(").append(columnName).append(" IS NOT NULL)");
                    }
                }

                // Q <> {}
                else {
                    // Complex filter value
                    if(
                        (filterProperty.getValues().length > 0) && 
                        (filterProperty.getValue(0) instanceof QueryFilterRecord)
                    ) {
                        if(filterPropertyDef.isReferenceType()) {
                            addComplexFilter(
                                conn,
                                dbObject,
                                statedObject,
                                reference,
                                viewAliasName,
                                filterProperty,
                                filterPropertyDef,
                                columnName,
                                ConditionType.IS_NOT_IN,
                                clauseValues, 
                                clause
                            );
                        } else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "Filter property with value of type " + QueryFilterRecord.class.getName() + " must be a Reference",
                                new BasicException.Parameter("filter.property", filterProperty),
                                new BasicException.Parameter("filter.definition", filterPropertyDef)                                
                            );
                        }
                    } else {
                        clause.append("(");
                        //
                        // Scalar filter value
                        //
                        this.isInToSqlClause(
                            conn,
                            dbObject,
                            columnName,
                            true,
                            filterPropertyDef, 
                            clause, 
                            clauseValues, 
                            filterProperty.getValues()
                        );
                        clause.append(")");

                    }
                }
                break;

                // IS_LESS
            case IS_LESS:
                clause.append("(").append(columnName).append(" < ").append(getPlaceHolder(conn, filterProperty.getValue(0))).append(")");
                clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(0)));
                break;

                // IS_LESS_OR_EQUAL
            case IS_LESS_OR_EQUAL:
                clause.append("(").append(columnName).append(" <= ").append(getPlaceHolder(conn, filterProperty.getValue(0))).append(")");
                clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(0)));
                break;

                // IS_IN         
                // Evaluate the following:
                // THERE_EXISTS v IN A: v IN Q (Special: if Q={} ==> false)
                // FOR_ALL v IN A: v IN Q (Special: if Q={} ==> true, iff A={}, false otherwise)
            case IS_IN:   

                // Q = {}
                if(filterProperty.getValues().length == 0) {
                    if(quantor == Quantifier.THERE_EXISTS) {
                        clause.append("(1=0)");
                    } else {
                        clause.append("(").append(columnName).append(" IS NULL)");
                    }
                } 

                // Q <> {}
                else {
                    // Complex filter value
                    if(
                        (filterProperty.getValues().length > 0) && 
                        (filterProperty.getValue(0) instanceof QueryFilterRecord)
                    ) {
                        if(filterPropertyDef.isReferenceType()) {
                            addComplexFilter(
                                conn,
                                dbObject,
                                statedObject,
                                reference,
                                viewAliasName,
                                filterProperty,
                                filterPropertyDef,
                                columnName,
                                ConditionType.IS_IN,
                                clauseValues, clause
                            );
                        } else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "Filter property with value of type " + QueryFilterRecord.class.getName() + " must be a Reference",
                                new BasicException.Parameter("filter.property", filterProperty),
                                new BasicException.Parameter("filter.definition", filterPropertyDef)                                
                            );
                        }
                    } else {
                        clause.append("(");
                        //
                        // Scalar filter value
                        //
                        this.isInToSqlClause(
                            conn,
                            dbObject,
                            columnName,
                            false,
                            filterPropertyDef, 
                            clause, 
                            clauseValues, filterProperty.getValues()
                        );
                        clause.append(")");

                    }
                }
                break;

                // IS_GREATER_OR_EQUAL
            case IS_GREATER_OR_EQUAL:
                clause.append("(").append(columnName).append(" >= ").append(getPlaceHolder(conn, filterProperty.getValue(0))).append(")");
                clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(0)));
                break;

                // IS_GREATER
            case IS_GREATER:
                clause.append("(").append(columnName).append(" > ").append(getPlaceHolder(conn, filterProperty.getValue(0))).append(")");
                clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(0)));
                break;

                // IS_BETWEEN
            case IS_BETWEEN:
                clause.append("((").append(columnName).append(" >= ").append(getPlaceHolder(conn, filterProperty.getValue(0))).append(") AND (").append(columnName).append(" <= ").append(getPlaceHolder(conn, filterProperty.getValue(1))).append("))");
                clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(0)));
                clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(1)));
                break;

                // IS_OUTSIDE
            case IS_OUTSIDE:
                clause.append("((").append(columnName).append(" < ").append(getPlaceHolder(conn, filterProperty.getValue(0))).append(") OR (").append(columnName).append(" > ").append(getPlaceHolder(conn, filterProperty.getValue(1))).append("))");
                clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(0)));
                clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(1)));
                break;

                // IS_LIKE
            case IS_LIKE:
                clause.append("(");
                for(
                    int j = 0; 
                    j < filterProperty.getValues().length; 
                    j++
                    ) {
                    if(j > 0) {
                        clause.append(" OR ");
                    }
                    Object v = filterProperty.getValue(j);
                    if(v instanceof Path) {
                        this.isLikeToSqlClause(
                            conn, 
                            dbObject, 
                            columnName, 
                            true, 
                            filterPropertyDef, 
                            clause, 
                            clauseValues, 
                            (Path) v
                        );
                    } else {
                        isLikeToSqlClause(conn, columnName, clauseValues, clause, v);
                    }
                }
                clause.append(")");
                break;

                // IS_UNLIKE
            case IS_UNLIKE:
                clause.append("(");
                for(
                    int j = 0; 
                    j < filterProperty.getValues().length; 
                    j++
                ) {
                    if(j > 0) {
                        clause.append(" AND ");
                    }
                    Object v = filterProperty.getValue(j);
                    if(v instanceof Path) {
                        Path vp = (Path)v;
                        Set<Path> matchingTypes = new HashSet<Path>();
                        matchingTypes.add(vp);
                        boolean includeSubTree = "%".equals(vp.getLastSegment().toClassicRepresentation());
                        // If path ends with % get the set of matching types
                        if(includeSubTree) {
                            vp = vp.getPrefix(vp.size()-1);
                            for(DbObjectConfiguration dbObjectConfiguration : this.getDatabaseConfiguration().getDbObjectConfigurations()) {
                                if(
                                    (dbObjectConfiguration.getType().size() > vp.size()) &&
                                    vp.isLike(dbObjectConfiguration.getType().getPrefix(vp.size()))
                                    ) {
                                    matchingTypes.add(
                                        dbObjectConfiguration.getType()
                                    );
                                }
                            }
                        }
                        // NOT LIKE clause for each path pattern
                        int ii = 0;
                        for(
                            Iterator<?> i = matchingTypes.iterator();
                            i.hasNext();
                            ii++
                        ) {
                            Path type = (Path)i.next();
                            if(ii > 0) {
                                clause.append(" AND ");
                            }
                            clause.append("(NOT (").append(columnName).append(" LIKE ? ").append(getEscapeClause(conn)).append("))");
                            String externalized = this.externalizePathValue(
                                conn,
                                vp.getDescendant(type.getSuffix(vp.size()))
                            );
                            int pos = externalized.indexOf("%");
                            if(pos >= 0) {
                                externalized = externalized.substring(0, pos+1);
                            }
                            if(externalized.startsWith("xri:")) {
                                if(externalized.endsWith("***")) {
                                    externalized = externalized.substring(0, externalized.length()-3) + "%";
                                }
                                pos = externalized.indexOf("**");
                                if(pos >= 0) {
                                    if(includeSubTree && pos == externalized.length() - 2) {
                                        externalized = externalized.substring(0, pos) + "%";
                                    } else throw new ServiceException(
                                        BasicException.Code.DEFAULT_DOMAIN,
                                        BasicException.Code.NOT_SUPPORTED, 
                                        "only (***|%) wildcards at the end supported",
                                        new BasicException.Parameter("value", v),
                                        new BasicException.Parameter("path", externalized),
                                        new BasicException.Parameter("position", pos)                          
                                        );
                                }
                            } else if(externalized.startsWith("spice:")) {
                                for(XRISegment p : ((Path)v).getSegments()){
                                    String c = p.toString();
                                    if(c.startsWith(":") && c.endsWith("*")) {
                                        throw new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.NOT_SUPPORTED, 
                                            "path component pattern ':<pattern>*' not supported",
                                            new BasicException.Parameter("path", externalized),
                                            new BasicException.Parameter("position", i)                          
                                            );
                                    }
                                }
                            }
                            clauseValues.add(externalized);
                        }
                    } else {
                        clause.append("NOT(");
                        isLikeToSqlClause(conn, columnName, clauseValues, clause, v);
                        clause.append(")");
                    }
                }
                clause.append(")");
                break;

                // SOUNDS_LIKE
            case SOUNDS_LIKE:
                clause.append("(SOUNDEX(").append(columnName).append(") IN (SOUNDEX(?)");
                clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(0)));
                for(
                    int j = 1; 
                    j < filterProperty.getValues().length; 
                    j++
                ) {
                    clause.append(", SOUNDEX(?)");
                    clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(j)));
                }
                clause.append("))");
                break;

                // SOUNDS_UNLIKE
            case SOUNDS_UNLIKE:
                clause.append("(SOUNDEX(").append(columnName).append(") NOT IN (SOUNDEX(?)");
                clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(0)));
                for(
                    int j = 1; 
                    j < filterProperty.getValues().length; 
                    j++
                ) {
                    clause.append(", SOUNDEX(?)");
                    clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(j)));
                }
                clause.append("))");
                break;

                // unsupported
            default: 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Unsupported operator", 
                    new BasicException.Parameter("operator", operator)
                );
        }
        return clause.toString();
    }

    private void isLikeToSqlClause(
        Connection conn, 
        String columnName,
        List<Object> clauseValues, 
        StringBuilder clause, 
        Object v
    ) throws ServiceException {
        EmbeddedFlags.FlagsAndValue flagsAndValue = this.embeddedFlags.parse((String)this.externalizeStringValue(columnName, v));
        EnumSet<RegularExpressionFlag> flagSet = flagsAndValue.getFlagSet();
        String externalized;
        if(flagSet.contains(RegularExpressionFlag.X_QUERY) || flagSet.contains(RegularExpressionFlag.JSON_QUERY)){
            externalized = flagsAndValue.getValue();
        } else {
            externalized = this.sqlWildcards.fromJDO(flagsAndValue.getValue());
        }
        if(flagSet.contains(RegularExpressionFlag.ACCENT_INSENSITIVE) && flagSet.contains(RegularExpressionFlag.CASE_INSENSITIVE)){
            isLikeToSqlClause(conn, columnName, clauseValues, clause, externalized, "CASE_AND_ACCENT.INSENSITIVITY", LikeFlavour.NOT_SUPPORTED);
        } else if(flagSet.contains(RegularExpressionFlag.ACCENT_INSENSITIVE)){
            isLikeToSqlClause(conn, columnName, clauseValues, clause, externalized, "ACCENT.INSENSITIVITY", LikeFlavour.NOT_SUPPORTED);
        } else if(flagSet.contains(RegularExpressionFlag.CASE_INSENSITIVE)){
            isLikeToSqlClause(conn, columnName, clauseValues, clause, externalized, "CASE.INSENSITIVITY", LikeFlavour.LOWER_SQL);
        } else if(flagSet.contains(RegularExpressionFlag.POSIX_EXPRESSION)){
            isLikeToSqlClause(conn, columnName, clauseValues, clause, externalized, "POSIX.EXPRESSION", LikeFlavour.NOT_SUPPORTED);
        } else if(flagSet.contains(RegularExpressionFlag.X_QUERY)){
            throw new UnsupportedOperationException("X_QUERY not yet supported"); // TODO
        } else if(flagSet.contains(RegularExpressionFlag.JSON_QUERY)){
            // @TODO hard-coded for PG. Better use LikeFlavour
            String operator = "@>";
            if(externalized.startsWith("?&") || externalized.startsWith("?|") || externalized.startsWith("@>")) {
                operator = externalized.substring(0, 2);
                externalized = externalized.substring(2);
            } else if(externalized.startsWith("?")) {
                operator = externalized.substring(0, 1);
                externalized = externalized.substring(1);
            }
            clause.append("(").append(columnName).append(" ").append(operator).append(" ? ").append(")");
            clauseValues.add(externalized);                  
        } else {
            clause.append("(").append(columnName).append(" LIKE ? ").append(getEscapeClause(conn)).append(")");
            clauseValues.add(externalized);
        }
    }

    private void isLikeToSqlClause(
        Connection conn, 
        String columnName,
        List<Object> clauseValues, 
        StringBuilder clause,
        String externalized, 
        String propertyName, 
        LikeFlavour defaultFlavour
    ) throws ServiceException {
        List<LikeFlavour> likeFlavours = LikeFlavour.parse(
            getDriverProperty(
                conn,
                propertyName,
                defaultFlavour.name()
            )
        );
        LikeFlavour.applyAll(likeFlavours, clause, clauseValues, columnName, externalized);
    }

    /**
     * @param filterPropertyDef
     * @param referencedType
     * @return
     * @throws ServiceException
     */
    protected ModelElement_1_0 getReferenceType(
        ModelElement_1_0 filterPropertyDef
    ) throws ServiceException {
        try {
            return filterPropertyDef.getModel().getElementType(filterPropertyDef);
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Unable to retrieve the element type",
                new BasicException.Parameter("filterProperty", filterPropertyDef.jdoGetObjectId())
            );
        }
    }
    
    /**
     * @param conn
     * @param dbObject
     * @param joinFromState 
     * @param reference
     * @param viewAliasName
     * @param filterProperty
     * @param filterPropertyDef
     * @param columnName
     * @param negation 
     * @param clauseValues
     * @param clause
     * @throws ServiceException
     */
    protected void addComplexFilter(
        Connection conn,
        DbObject dbObject,
        boolean joinFromState,
        Path reference,
        String viewAliasName,
        FilterProperty filterProperty,
        ModelElement_1_0 filterPropertyDef,
        String columnName,
        ConditionType condition, 
        List<Object> clauseValues, 
        StringBuilder clause
    ) throws ServiceException {
        Model_1_0 model = filterPropertyDef.getModel();
        ModelElement_1_0 referencedType = getReferenceType(filterPropertyDef);
        String joinClauseBegin = null;
        String joinClauseEnd = null;
        String joinColumn = null;
        DbObject joinObject = null;
        final boolean joinWithState = false; // TODO to be evaluated and USED for non RID/OID DBs, too!
        // Reference
        if(model.referenceIsStoredAsAttribute(filterPropertyDef)) {
            Path identityPattern = model.getIdentityPattern(referencedType);
            // Identity pattern may be null in case referenced type is an abstract
            // class. In this case try to get identity pattern for concrete subclasses
            // and assert that all identity patterns are equal and therefore are mapped
            // to the same DB object.
            if(identityPattern == null) {
                for(Object subtype: referencedType.objGetList("allSubtype")) {
                    ModelElement_1_0 subtypeDef = model.getElement(subtype);                    
                    identityPattern = model.getIdentityPattern(subtypeDef);
                    if(identityPattern != null) {
                        if(joinObject == null) {
                            joinObject = this.getDbObject(
                                conn,
                                null, // dbObjectConfiguration
                                applyProvider(identityPattern, reference),
                                null, // filter
                                true
                            );                            
                        } else {
                            DbObject joinObject2 = this.getDbObject(
                                conn,
                                null, // dbObjectConfiguration
                                applyProvider(identityPattern, reference),
                                null, // filter
                                true
                            );
                            if(
                                !joinObject.getObjectIdColumn().equals(joinObject2.getObjectIdColumn()) ||
                                !joinObject.getConfiguration().matchesJoinCriteria(joinObject2.getConfiguration())
                            ) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.NOT_SUPPORTED,
                                    "Join criteria for type is ambigous",
                                    new BasicException.Parameter("type", referencedType),
                                    new BasicException.Parameter("joinObject.1", joinObject),
                                    new BasicException.Parameter("joinObject.2", joinObject2)
                                );
                            }
                        }
                    }
                }
            } else {
                joinObject = this.getDbObject(
                    conn,
                    null, // dbObjectConfiguration
                    applyProvider(identityPattern, reference),
                    null, // filter
                    true
                );
            }
            joinClauseBegin = columnName;
            joinClauseEnd = "";
            joinColumn = joinObject.getObjectIdColumn().get(0);
        } else if(ModelHelper.isCompositeEnd(filterPropertyDef, true)) {
            //
            // Composite parent
            //
            joinObject = this.getDbObject(
                conn, 
                model.getIdentityPattern(referencedType), 
                null, 
                true
            );
            joinClauseBegin = viewAliasName + "." + this.getColumnName(
                conn, 
                "parent", 
                0, 
                false, // indexSuffixIfZero, 
                false, // ignoreReservedWords
                true // markAsPrivate
            );
            joinClauseEnd = "";
            joinColumn = joinObject.getObjectIdColumn().get(0);
        } else if(ModelHelper.isCompositeEnd(filterPropertyDef, false)) {
            //
            // Composite
            //
            if(model.getIdentityPattern(referencedType) == null) {
                joinObject = this.getDbObject(
                    conn, 
                    reference.getDescendant(":*", filterProperty.name()), 
                    null, 
                    true
                );                
            } else {
                joinObject = this.getDbObject(
                    conn, 
                    model.getIdentityPattern(referencedType), 
                    null, 
                    true
                );                
            }
            // No fallback in case the parent and the referenced type are abstract and root
            joinClauseBegin = viewAliasName + "." + dbObject.getObjectIdColumn().get(0);
            joinClauseEnd = "";
            joinColumn = this.getColumnName(
                conn, 
                "parent", 
                0, 
                false, // indexSuffixIfZero, 
                false, // ignoreReservedWords
                true // markAsPrivate
            );                                
        } else if(ModelHelper.isSharedEnd(filterPropertyDef, false)) {
            //
            // Shared
            //
            try {
                // Try to get db object for access path
                joinObject = this.getDbObject(
                    conn, 
                    reference.getDescendant(":*", filterProperty.name()), 
                    null, 
                    true
                );
            } catch(Exception ignore) {
                // Fallback to identity pattern if no pattern is configured for access path
                SysLog.warning("No pattern configured for shared association. Fallback to identity pattern of referenced type", reference.getDescendant(":*", filterProperty.name()));
                joinObject = this.getDbObject(
                    conn,
                    model.getIdentityPattern(referencedType), 
                    null, 
                    true
                );
            }
            if(joinObject.getJoinCriteria() != null && joinObject.getJoinCriteria().length == 3) {                                    
                String[] joinCriteria = joinObject.getJoinCriteria();
                joinClauseBegin =
                    viewAliasName + "." + dbObject.getObjectIdColumn().get(0) +
                    " IN (SELECT " + joinCriteria[1] + " FROM " + joinCriteria[0] + " WHERE " +  joinCriteria[2];
                joinClauseEnd = ")";
                joinColumn = joinObject.getObjectIdColumn().get(0);
            } else {
                joinClauseBegin = viewAliasName + "." + dbObject.getObjectIdColumn().get(0);
                joinClauseEnd = "";
                joinColumn = this.getColumnName(
                    conn, 
                    "parent", 
                    0, 
                    false, // indexSuffixIfZero, 
                    false, // ignoreReservedWords
                    true // markAsPrivate
               );                                
            }
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Unsupported aggregation",
                new BasicException.Parameter("filter.property", filterProperty),
                new BasicException.Parameter("filter.definition", filterPropertyDef)                                
            );
        }
        List<FilterProperty> allFilterProperties = FilterProperty.getFilterProperties(
            (QueryFilterRecord)filterProperty.getValue(0)
        );
        // Replace instance_of IN ... by object_class IN ...
        FilterProperty objectClassFilterProperty = null;
        for(Iterator<FilterProperty> i = allFilterProperties.iterator(); i.hasNext(); ) {
            FilterProperty p = i.next();
            if(SystemAttributes.OBJECT_INSTANCE_OF.equals(p.name()) && !p.values().isEmpty()) {
                Set<String> allSubtypes = this.getAllSubtypes((String)p.getValue(0));
                if(allSubtypes != null) {
                    objectClassFilterProperty = new FilterProperty(
                        p.quantor(),
                        SystemAttributes.OBJECT_CLASS,
                        p.operator(),
                        allSubtypes.toArray()
                    );
                }
                i.remove();
            }
        }
        if(objectClassFilterProperty != null) {
            allFilterProperties.add(objectClassFilterProperty);
        }
        List<FilterProperty> primaryFilterProperties = this.getPrimaryFilterProperties(
            referencedType, 
            allFilterProperties
        );
        DbObjectConfiguration joinObjectConfiguration = joinObject.getConfiguration();    
        String view1 = joinObjectConfiguration.getDbObjectForQuery1() == null 
            ? joinObjectConfiguration.getDbObjectForUpdate1() 
            : joinObjectConfiguration.getDbObjectForQuery1();
        String view2 = joinObjectConfiguration.getDbObjectForQuery2() == null 
            ? joinObjectConfiguration.getDbObjectForUpdate2() == null ? view1 : joinObjectConfiguration.getDbObjectForUpdate2() 
            : joinObjectConfiguration.getDbObjectForQuery1();
        // Positive clauses
        List<String> includingFilterClauses = new ArrayList<String>();
        List<List<Object>> includingFilterValues = new ArrayList<List<Object>>();
        List<String> excludingFilterClauses = new ArrayList<String>();
        List<List<Object>> excludingFilterValues = new ArrayList<List<Object>>();
        this.filterToSqlClauses(
            conn,
            joinObject,
            joinWithState,
            viewAliasName + "v",
            view1,
            view2,
            JoinType.SPECIFIED_COLUMN_WITH_OBJECT_ID, 
            viewAliasName + "v." + dbObject.getObjectIdColumn().get(0),
            false,  // stickyViewAlias
            referencedType,
            allFilterProperties,
            primaryFilterProperties,
            includingFilterClauses,
            includingFilterValues,
            excludingFilterClauses,
            excludingFilterValues
        );
        
        final Membership membership = new Membership(Quantifier.valueOf(filterProperty.quantor()), condition);
        clause.append(
            "(" + joinClauseBegin + (membership.isMember() ? " IN " : " NOT IN ") + "(SELECT " + joinColumn + " FROM " + view1 + " " + viewAliasName + "v WHERE " + (membership.isNegated() ? "(1=0)" : "(1=1)")
        );
        for(int i = 0; i < includingFilterClauses.size(); i++) {
            if(!includingFilterClauses.get(i).isEmpty()) {
                clause
                    .append(membership.isNegated() ? " OR NOT " : " AND ")
                    .append(includingFilterClauses.get(i));
                clauseValues.addAll(
                    includingFilterValues.get(i)
                );
            }
        }
        clause.append("))").append(joinClauseEnd);
        // Negative clauses
        includingFilterClauses.clear();
        includingFilterValues.clear();
        excludingFilterClauses.clear();
        excludingFilterValues.clear();   
        this.filterToSqlClauses(
            conn, 
            joinObject,
            joinWithState,
            toMultiValueView(viewAliasName), 
            view1, 
            view2, 
            JoinType.NONE, // no join column 
            null, // no join column    
            false, // stickyViewAlias
            referencedType, 
            allFilterProperties, 
            primaryFilterProperties, 
            includingFilterClauses, 
            includingFilterValues, 
            excludingFilterClauses, 
            excludingFilterValues
        );
        if(!excludingFilterClauses.isEmpty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Nested queries not supported for sliced tables",
                new BasicException.Parameter("filter.property", filterProperty),
                new BasicException.Parameter("filter.definition", filterPropertyDef)                                
            );
        }
    }

    //------------------------------------------------------------------------
    protected List<ModelElement_1_0> getFilterPropertyDefs(
        ModelElement_1_0 referencedTypeDef,
        List<FilterProperty> filterProperties
        ) throws ServiceException {
        List<ModelElement_1_0> filterPropertyDefs = new ArrayList<ModelElement_1_0>();
        // try to determine multiplicity of filter properties in order to optimize query
        Set<ModelElement_1_0> referencedTypes = null;
        try {
            // filter property can be an attribute of all super and subtypes of referenced type
            referencedTypes = new HashSet<ModelElement_1_0>();
            // All subtypes and their supertypes of referenced type
            for(Iterator<Object> i = referencedTypeDef.objGetList("allSubtype").iterator(); i.hasNext(); ) {
                ModelElement_1_0 subtype = getModel().getElement(i.next());
                referencedTypes.add(subtype);
                for(Iterator<Object> j = subtype.objGetList("allSupertype").iterator(); j.hasNext(); ) {
                    ModelElement_1_0 supertype = getModel().getElement(j.next());
                    referencedTypes.add(supertype);
                }
            }
        } catch(Exception e) {
            // ignore
        }
        loopFilterProperties: for(FilterProperty p: filterProperties) {
            try {
                if(referencedTypes != null) {
                    // get filter property name and eliminate prefixes such as role_id, etc.
                    String filterPropertyName = p.name();
                    filterPropertyName = filterPropertyName.substring(filterPropertyName.lastIndexOf('$') + 1);
                    // Qualified filter property name
                    if(filterPropertyName.indexOf(":") > 0) {
                        ModelElement_1_0 featureDef = getModel().findElement(filterPropertyName);
                        if(featureDef != null) {
                            filterPropertyDefs.add(featureDef);
                            continue loopFilterProperties;
                        }                        
                    }
                    // Non-qualified filter property name. Must look up.
                    else {
                        filterPropertyName = filterPropertyName.substring(filterPropertyName.lastIndexOf(':') + 1);
                        // try to find filter property in any of the subtypes of referencedType
                        for(ModelElement_1_0 subtype: referencedTypes) {
                            String qualifiedFilterPropertyName = subtype.getQualifiedName() + ":" + filterPropertyName;
                            ModelElement_1_0 featureDef = getModel().findElement(qualifiedFilterPropertyName);
                            if(featureDef != null) {
                                filterPropertyDefs.add(featureDef);
                                continue loopFilterProperties;
                            }
                        }
                    }
                    // No feature definition found for filter property
                    filterPropertyDefs.add(null);
                }
            } catch(ServiceException exception) {
                SysLog.warning(
                    "The following error occured when trying to determine multiplicity of filter property", 
                    exception
                );
            }
        }
        return filterPropertyDefs;
    }

    //------------------------------------------------------------------------
    protected List<FilterProperty> getPrimaryFilterProperties(
        ModelElement_1_0 referencedType,
        List<FilterProperty> filterProperties
    ) throws ServiceException {        
        List<FilterProperty> primaryFilterProperties = new ArrayList<FilterProperty>();
        List<ModelElement_1_0> filterPropertyDefs = this.getFilterPropertyDefs(
            referencedType, 
            filterProperties
        );        
        for(int i = 0; i < filterProperties.size(); i++) {
            FilterProperty filterProperty = filterProperties.get(i);
            ModelElement_1_0 filterPropertyDef = filterPropertyDefs.get(i);
            final boolean isPrimary;
            // Configured single-valued
            if(filterPropertyDef == null) {
                isPrimary = this.singleValueAttributes.contains(filterProperty.name());
            }
            // Associations for nested queries
            else if(
                filterPropertyDef.isReferenceType() && 
                !this.getModel().referenceIsStoredAsAttribute(filterPropertyDef)
            ) {
                isPrimary = true;
            }
            // Embedded
            else if(Database_2.this.getEmbeddedFeature(filterProperty.name()) != null) {
                isPrimary = true;
            }
            // Single-valued
            else {
                isPrimary = ModelHelper.getMultiplicity(filterPropertyDef).isSingleValued();
            }
            if(isPrimary) {
                primaryFilterProperties.add(filterProperty);
            }
        }
        return primaryFilterProperties;
    }

    protected void removePrivateAttributes(
        ObjectRecord object
    ) throws ServiceException {
        this.removeAttributes(
            object, 
            true, // removPrivate
            false, // removeNonPersistent
            true // removeSize
        );
    }

    protected void removeAttributes(
        ObjectRecord object, 
        boolean removePrivate, 
        boolean removeNonPersistent, 
        boolean removeSize
        ) throws ServiceException {
        Object_2Facade facade = Facades.asObject(object);
        MappedRecord value = facade.getValue();
        ModelElement_1_0 classifierDef = null;
        classifierDef = this.getModel().findElement(value.getRecordName());
        if(classifierDef == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "No classifier definition found",
                new BasicException.Parameter("object", value)
            );
        }
        for(
            Iterator<String> i = facade.getValue().keySet().iterator();
            i.hasNext();
            ) {
            String attributeName = i.next();
            if(attributeName.startsWith(getPrivateAttributesPrefix())) {
                //
                // Private attributes
                //
                if(removePrivate) {
                    i.remove();
                }
            } else if(attributeName.endsWith(SIZE_SUFFIX)) {
                //
                // Size attributes
                //
                if(removeSize) {
                    i.remove();
                }               
            } else if(!SystemAttributes.OBJECT_CLASS.equals(attributeName)){
                //
                // Application attributes
                //
                ModelElement_1_0 featureDef;
                if(classifierDef == null){
                    featureDef = null;
                } else {
                    featureDef = classifierDef.getModel().getFeatureDef(
                        classifierDef, 
                        attributeName, 
                        true // includeSubtypes
                        ); 
                    if(featureDef == null) {
                        SysLog.log(Level.FINE, "Sys|No feature definition found|{0}#{1}", value.getRecordName(), attributeName);
                    }
                }
                if(featureDef == null ? !SYSTEM_ATTRIBUTES.contains(attributeName) : (removeNonPersistent && !this.isPersistent(featureDef))) {
                    i.remove();
                }
            }
        }    
    }

    protected void normalizeDateTimeValues(
        MappedRecord object
        ) throws ServiceException {
        Object_2Facade facade = Facades.asObject(object);
        // replace values if necessary
        for(
            Iterator<String> i = facade.getValue().keySet().iterator();
            i.hasNext();
        ) {
            Object values = facade.getAttributeValues(i.next()); 
            if(values != null){
                ListIterator j = values instanceof SparseArray ?
                    ((SparseArray)values).populationIterator() :
                    ((List)values).listIterator();
                while(j.hasNext()) {
                    Object value = j.next();
                    if(value instanceof XMLGregorianCalendar) {
                        XMLGregorianCalendar datatypeValue = (XMLGregorianCalendar) value;
                        if(DatatypeConstants.DATETIME.equals(datatypeValue.getXMLSchemaType())) {
                            j.set(datatypeValue.toGregorianCalendar().getTime());
                        }
                    }
                }
            }
        }    
    }

    //------------------------------------------------------------------------
    protected void setLockAssertion(
        Object_2Facade facade,
        String versionAttribute
    ) throws ServiceException{
        StringBuilder lockAssertion = new StringBuilder(versionAttribute).append('=');
        Object versionValue = facade.attributeValue(versionAttribute);
        if(versionValue != null) {
            lockAssertion.append(versionValue);
        }
        facade.setVersion(
            UnicodeTransformation.toByteArray(lockAssertion.toString())
        );
    }

    //------------------------------------------------------------------------
    protected void setLockAssertion(
        MappedRecord object
    ) throws ServiceException{
        Object_2Facade facade = Facades.asObject(object);
        String objectClass = facade.getObjectClass();
        Model_1_0 model = getModel();
        if(
            isStated(model, objectClass) &&
            isNotExcludedFromPersistency("org:openmdx:base:Removable:removedAt")
        ){
            setLockAssertion(facade, SystemAttributes.REMOVED_AT);
        } else if(model.isSubtypeOf(objectClass, "org:openmdx:base:Modifiable")) {
            setLockAssertion(facade, SystemAttributes.MODIFIED_AT);
        }
    }

    protected void completeObject(
        Object object
    ) throws ServiceException {
        if(object instanceof ObjectRecord) {
            ObjectRecord objectRecord = (ObjectRecord)object;
            this.removePrivateAttributes(objectRecord);
            this.setLockAssertion(objectRecord);
            this.normalizeDateTimeValues(objectRecord);
        }
    }

    protected void completeReply(
        ResultRecord objects
    ) throws ServiceException {
        for(Object object : objects) {
            this.completeObject(object);
        }
    }

    protected DbObject createDbObject(
        Connection conn,
        DbObjectConfiguration dbObjectConfiguration,
        Path accessPath,
        boolean isQuery
    ) throws ServiceException {
        return this.getDbObject(
            conn,
            dbObjectConfiguration,
            accessPath,
            null,
            isQuery
        );
    }

    protected DbObject createDbObject(
        Connection conn,
        Path accessPath,
        boolean isQuery
    ) throws ServiceException {
        return this.getDbObject(
            conn,
            null,
            accessPath,
            null,
            isQuery
        );
    }

    public DbObject getDbObject(
        Connection conn,
        Path accessPath,
        List<FilterProperty> filter,
        boolean isQuery
    ) throws ServiceException {
        return this.getDbObject(
            conn,
            null,
            accessPath,
            filter,
            isQuery
        );
    }

    /**
     * DB Object retrieval
     * 
     * @param conn
     * @param dbObjectConfiguration
     * @param accessPath
     * @param filter
     * @param isQuery
     * @return the requested DB object
     * 
     * @throws ServiceException
     */
    protected DbObject getDbObject(
        Connection conn,
        DbObjectConfiguration dbObjectConfiguration,
        Path accessPath,
        List<FilterProperty> filter,
        boolean isQuery
    ) throws ServiceException {
        DbObjectConfiguration configuration = dbObjectConfiguration;
        Path adjustedAccessPath = accessPath;
        boolean isExtent = false;

        // extent access requires special treatment
        // get the set of referenceIds specified by filter property 'identity'
        if(filter != null && accessPath.isLike(EXTENT_REFERENCES)) {
            for(FilterProperty p : filter) {
                if(SystemAttributes.OBJECT_IDENTITY.equals(p.name())) {
                    if(p.values().size() > 1) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED, 
                            "at most one value allowed for filter property 'identity'",
                            new BasicException.Parameter("filter", filter),
                            new BasicException.Parameter(p.name(), p.values())
                            );                        
                    }
                    isExtent = true;
                    adjustedAccessPath = new Path(p.getValue(0).toString());
                }
            }
            if(!isExtent) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED, 
                    "extent lookups require at least a filter value for property 'identity'",
                    new BasicException.Parameter("filter", filter)
                );            
            }
        }

        if(configuration == null) {
            Path referencedType = adjustedAccessPath.size() % 2 == 0 ? adjustedAccessPath : adjustedAccessPath.getParent(); 
            configuration = this.getDatabaseConfiguration().getDbObjectConfiguration(
                referencedType
            );
        }

        if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_NON_INDEXED.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObjectNonIndexed(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2_NON_INDEXED.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2NonIndexed(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_PARENT_RID_ONLY.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObjectParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2_PARENT_RID_ONLY.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2ParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_NON_INDEXED_PARENT_RID_ONLY.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObjectNonIndexedParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2_NON_INDEXED_PARENT_RID_ONLY.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2NonIndexedParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_WITH_ID_AS_KEY.equals(configuration.getDbObjectFormat())) {
            return new DBOSlicedWithIdAsKey(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_WITH_PARENT_AND_ID_AS_KEY.equals(configuration.getDbObjectFormat())) {
            return new DBOSlicedWithParentAndIdAsKey(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else {
            try {
                return Classes.<DbObject>getApplicationClass(
                    configuration.getDbObjectFormat()
                ).getConstructor(
                    Database_1_0.class,
                    Connection.class,
                    DbObjectConfiguration.class,
                    Path.class,
                    boolean.class,
                    boolean.class
                ).newInstance(
                    this,
                    conn,
                    configuration,
                    adjustedAccessPath,
                    Boolean.valueOf(isExtent),
                    Boolean.valueOf(isQuery)
                );
            } catch(Exception exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE, 
                    "can not create DbObject",
                    new BasicException.Parameter("path", adjustedAccessPath),
                    new BasicException.Parameter("type", configuration.getDbObjectFormat())
                );
            }
        }
    }

    /**
     * Remove view prefix 'v.' from column names in clause
     */
    protected String removeViewPrefix(
        String _clause
    ) {
        String clause = _clause;
        int pos = 0;
        while((pos = clause.indexOf("v.")) >= 0) {
            clause = clause.substring(0, pos) + clause.substring(pos + 2);
        }
        return clause;
    }

    protected Connection getConnection(
        RestInteractionSpec ispec,
        RequestRecord request
    ) throws ServiceException, SQLException {
        return this.getDataSource(ispec, request).getConnection();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0#prepareStatement(java.sql.Connection, java.lang.String)
     */
    @Override
    public PreparedStatement prepareStatement(
        Connection conn,
        String statement
    ) throws SQLException {
        return Database_2.this.prepareStatement(
            conn,
            statement,
            false
        );
    }

    protected ObjectRecord getPartialObject(
        Connection conn,
        Path path,
        short attributeSelector,
        Map<String,AttributeSpecifier> attributeSpecifiers,
        boolean objectClassAsAttribute,
        DbObject dbObject,
        boolean primaryColumns,
        String objectClass, 
        boolean throwNotFoundException
    ) throws ServiceException {
        PreparedStatement ps = null;
        String currentStatement = null;
        ResultSet rs = null;
        List<Object> statementParameters = null;
        try {
            String view1 = Database_2.this.getView(
                conn,
                dbObject,
                null,
                VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY,
                null,
                null
            );
            String view2 = Database_2.this.getView(
                conn,
                dbObject,
                null,
                VIEW_MODE_SECONDARY_COLUMNS,
                null,
                null
            );
            // SELECT
            String statement = null;
            String prefix = null;
            String objectIdClause = dbObject.getObjectIdClause();
            String referenceClause = dbObject.getReferenceClause();
            boolean useReferenceClause = 
                (referenceClause.indexOf(objectIdClause) < 0) &&
                (referenceClause.indexOf(objectIdClause.replaceAll("= \\?", "> \\?")) < 0) &&
                (referenceClause.indexOf(objectIdClause.replaceAll("= \\?", "< \\?")) < 0);
            if(primaryColumns) {
                // Optimize reference clause in case the objectIdClause is more
                // restrictive than the reference clause.
                statement = view1.startsWith("SELECT") 
                    ? view1 + " AND " 
                        : "SELECT " + dbObject.getHint() + " * FROM " + view1 + " v WHERE ";
                statement += 
                    "(" + (useReferenceClause ? referenceClause + " AND " : "") + objectIdClause + ")";
                prefix = "v.";
            }
            // Secondary columns
            else {
                if(dbObject.getConfiguration().getDbObjectsForQueryJoinColumn() == null) {
                    statement = view1.startsWith("SELECT") 
                        ? view2 + " AND " 
                            : "SELECT " + dbObject.getHint() + " * FROM " + view2 + " v WHERE ";
                    statement +=
                        "(" +  (useReferenceClause ? referenceClause + " AND " : "") + objectIdClause + ")";
                    prefix = "v.";
                } else {            
                    statement = view1.startsWith("SELECT") 
                        ? "SELECT " + dbObject.getHint() + " vm.* FROM (" + view2 + ") vm" 
                            : "SELECT " + dbObject.getHint() + " vm.* FROM " + view2 + " vm";                 
                    statement += 
                        " INNER JOIN ";
                    statement += view1.startsWith("SELECT") 
                        ? "(" + view1 + ") v" 
                            : view1 + " v";
                    statement += 
                        " ON vm." + OBJECT_OID + " = v." + dbObject.getConfiguration().getDbObjectsForQueryJoinColumn();
                    statement += 
                        " WHERE ";
                    statement +=
                        "(" +  (useReferenceClause ? referenceClause + " AND " : "") + objectIdClause + ")";
                    prefix = "vm.";
                }
            }
            // ORDER BY required on secondary db object and on primary if
            // it contains indexed object slices
            if(primaryColumns) {
                if(dbObject.getIndexColumn() != null) {
                    statement += " ORDER BY " + prefix + dbObject.getIndexColumn();
                }
            } else {
                statement +=  " ORDER BY " + prefix + OBJECT_IDX;
            }
            ps = this.prepareStatement(
                conn,
                currentStatement = statement
            );
            statementParameters = new ArrayList<Object>();
            int pos = 1;
            if(useReferenceClause) {
                List<Object> referenceValues = dbObject.getReferenceValues();
                statementParameters.addAll(referenceValues);
                for(Object referenceValue : referenceValues){
                    Database_2.this.setPreparedStatementValue(
                        conn, 
                        ps, 
                        pos++, 
                        referenceValue
                    );
                }
            }
            List<Object> objectIdValues = dbObject.getObjectIdValues();
            statementParameters.addAll(objectIdValues);
            for(Object objectIdValue : objectIdValues) {
                Database_2.this.setPreparedStatementValue(
                    conn,
                    ps, 
                    pos++, 
                    objectIdValue
                );
            }      
            rs = Database_2.this.executeQuery(
                ps,
                currentStatement,
                statementParameters, 
                FetchPlan.FETCH_SIZE_OPTIMAL
            );
            ObjectRecord replyObj = Database_2.this.getObject(
                conn, 
                path,
                attributeSelector,
                attributeSpecifiers,
                objectClassAsAttribute,
                dbObject, 
                rs,
                objectClass,
                primaryColumns, // check for valid identity only when primary columns are fetched
                throwNotFoundException
            );      
            return replyObj;
        } catch(SQLException exception) {
            throw new ServiceException(
                exception, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("xri", path),
                new BasicException.Parameter("statement", currentStatement),
                new BasicException.Parameter("parameters", statementParameters),
                new BasicException.Parameter("sqlErrorCode", exception.getErrorCode()), 
                new BasicException.Parameter("sqlState", exception.getSQLState())
                );
        } finally {
            close(rs);
            close(ps);
        }
    }

    public void get(
        Connection conn,
        RestInteractionSpec ispec,
        Path path,
        short attributeSelector,
        Map<String,AttributeSpecifier> attributeSpecifiers,
        boolean objectClassAsAttribute,
        ResultRecord reply, 
        boolean throwNotFoundException
    ) throws ServiceException {
        try {
            DbObject dbObject = Database_2.this.createDbObject(
                conn,
                path,
                true
            );
            // Get primary attributes
            boolean hasSecondaryDbObject =
                (dbObject.getConfiguration().getDbObjectForQuery2() != null) ||
                (dbObject.getConfiguration().getDbObjectForUpdate2() != null);          
            ObjectRecord replyObj = this.getPartialObject(
                conn,
                path,
                attributeSelector,
                attributeSpecifiers,
                objectClassAsAttribute,
                dbObject,
                true, // primaryColumns
                null, 
                throwNotFoundException
            );
            if(replyObj != null) {
                // Get attributes from secondary db object
                if(hasSecondaryDbObject) {
                    MappedRecord replyObj2 = this.getPartialObject(
                        conn,
                        path,
                        attributeSelector,
                        attributeSpecifiers,
                        false, // objectClassAsAttribute
                        dbObject,
                        false, // primaryColumns
                        Object_2Facade.getObjectClass(replyObj), 
                        false // throwNotFoundException
                    );
                    if(replyObj2 != null) {
                        Object_2Facade.getValue(replyObj2).keySet().removeAll(
                            Object_2Facade.getValue(replyObj).keySet()
                        );
                        Object_2Facade.getValue(replyObj).putAll(
                            Object_2Facade.getValue(replyObj2)
                        );
                    }
                }
                reply.add(replyObj);
            }
        } catch(ServiceException exception) {
            if(exception.getCause().getExceptionCode() == BasicException.Code.NOT_FOUND) { 
                if(throwNotFoundException) { 
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND, 
                        "object not found",
                        new BasicException.Parameter("path", path)
                    ); 
                }
            } else {
                throw exception;
            }            
        } catch(NullPointerException exception) {
            throw new ServiceException(exception).log();
        } catch(RuntimeException exception) {
            throw new ServiceException(exception);
        }
        this.completeReply(reply);
    }

    /**
     * Explicit test for duplicates.
     */
    private void checkForDuplicates(
        Connection conn,
        RestInteractionSpec ispec,
        ObjectRecord request
    ) throws ServiceException {
        try {
            ResultRecord reply = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
            this.get(
                conn,
                ispec,
                request.getResourceIdentifier(),
                AttributeSelectors.ALL_ATTRIBUTES,
                Collections.<String,AttributeSpecifier>emptyMap(),
                false, // objectClassAsAttribute
                reply, 
                false // throwNotFoundException
            );
            if(reply.isEmpty()) return;
        } catch(ServiceException exception) {
            if(exception.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                return;
            } else {
                throw exception;
            }
        } catch(ResourceException e) {
            throw new ServiceException(e);
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.DUPLICATE, 
            "duplicate object",
            new BasicException.Parameter("path", request.getResourceIdentifier())
        );
    }

    /**
     * Test whether the exception is recoverable
     * 
     * @param exception
     * @param partitionedObjects
     * 
     * @return <code>true</code> if the exception is recoverable
     */
    private boolean isRecoverable(
        ServiceException exception,
        MappedRecord[] partitionedObjects
    ){
        if(
            BasicException.Code.DEFAULT_DOMAIN.equals(exception.getExceptionDomain()) &&
            BasicException.Code.INVALID_CARDINALITY == exception.getExceptionCode() &&
            exception.getMessage().indexOf("TRY_TO_FORGET") >= 0
        ) {
            MappedRecord slice0 = Object_2Facade.getValue(partitionedObjects[0]);
            for(int i = 1; i < partitionedObjects.length; i++){
               MappedRecord sliceI = Object_2Facade.getValue(partitionedObjects[i]);
               for(Object featureName : sliceI.keySet()) {
                   if(!"objectIdx".equals(featureName)){
                       Object value0 = slice0.get(featureName); 
                       Object valueI = sliceI.get(featureName);
                       if(value0 == null ? valueI != null : !value0.equals(valueI)) {
                           return false;
                       }
                   }
               }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Makes a new object persistent. 
     */
    public void create(
        Connection conn,
        RestInteractionSpec ispec,
        ObjectRecord object,
        ResultRecord reply
    ) throws ServiceException {
        try {
            if(this.ignoreCheckForDuplicates) {
                //
                // WARNING: the implementation assumes that
                // 'unique constraints' are set in order that INSERTs throw a 'duplicate row'
                // exception which is then mapped to a DUPLICATE ServiceException. 
                //
            } else {
                this.checkForDuplicates(
                    conn,
                    ispec,
                    object
                );
            }
            // Partition object into slices and create all slices
            DbObject dbObject = this.createDbObject(
                conn,
                object.getResourceIdentifier(),
                false
            );
            ObjectRecord clone = Object_2Facade.cloneObject(object);
            String type = Object_2Facade.getObjectClass(object);
            this.removeAttributes(
                clone, 
                false, // removePrivate
                true, // removeNonPersistent
                true // removeSize
            );
            ObjectRecord[] partitionedObjects = dbObject.sliceAndNormalizeObject(clone, true);            
            try {
                for(
                    int i = 0; 
                    i < partitionedObjects.length;
                    i++
                ) {      
                    dbObject = this.createDbObject(
                        conn,
                        partitionedObjects[i].getResourceIdentifier(),
                        false
                    );
                    dbObject.createObjectSlice(
                        i,
                        type,
                        partitionedObjects[i]
                    );
                }
            } catch (ServiceException exception) {
                if(isRecoverable(exception, partitionedObjects)) {
                    SysLog.warning("Recovering from invalid cardinality by ignoring equal values", object);
                    exception.log();
                } else {
                    throw exception;
                }
            }
            if(reply != null) {
                reply.add(object);
            }
        } catch(NullPointerException exception) {
            throw new ServiceException(exception).log();
        } catch(RuntimeException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * RestInteraction
     *
     */
    public class RestInteraction extends AbstractRestInteraction {

        @SuppressWarnings("synthetic-access")
        public RestInteraction(
            RestConnection connection
        ) throws ResourceException {
            super(
                connection, 
                newDelegateInteraction(connection)
            );
        }

        private Connection getConnection(
            RestInteractionSpec ispec,
            QueryRecord request
        ) throws ServiceException  {
            try {
                return Database_2.this.getConnection(ispec, request);
            } catch (SQLException exception) {
                throw new ServiceException(exception);
            }
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean get(
            RestInteractionSpec ispec, 
            QueryRecord request, 
            ResultRecord result
        ) throws ResourceException {      
            SysLog.detail("> get", request);
            try {
                Connection conn = getConnection(ispec, request);
                try {
                    short attributeSelector = AttributeSelectors.getAttributeSelector(request);
                    Map<String,AttributeSpecifier> attributeSpecifierAsMap = AttributeSpecifier.getAttributeSpecifierAsMap(request.getQueryFilter());
                    Database_2.this.get(
                        conn,
                        ispec,
                        request.getResourceIdentifier(),
                        attributeSelector,
                        // Attribute specifiers are ignored by Database_1 except in case of attributeSelector==SPECIFIED_AND_SYSTEM_ATTRIBUTES.
                        // Save the possibly expensive call to attributeSpecifierAsMap
                        attributeSelector == AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES || attributeSelector == AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES 
                            ? attributeSpecifierAsMap 
                            : Collections.<String,AttributeSpecifier>emptyMap(),
                        false, // objectClassAsAttribute
                        result, 
                        false // isPreferringNotFoundException
                    );
                } catch(RuntimeException exception) {
                    throw new ServiceException(exception);
                } finally {
                    Database_2.this.close(conn);
                }
                return true;
            } catch(ServiceException exception) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#find(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean find(
            RestInteractionSpec ispec, 
            QueryRecord request, 
            ResultRecord reply
        ) throws ResourceException {
            final Model_1_0 model = Model_1Factory.getModel();
            final Path xri = request.getResourceIdentifier();
            if(DatabasePreferences.isConfigurationRequest(xri)) {
                try {
                    final DatabaseConfiguration dbConfiguration = Database_2.this.getDatabaseConfiguration();
					DatabasePreferences.discloseConfiguration(
                        xri, 
                        reply, 
                        dbConfiguration.getDbObjectConfigurations(),
                        dbConfiguration.getFromToColumnNameMapping(),
                        Database_2.this.getStringMacros()
                    );
                } catch(ServiceException e) {
                    throw new ResourceException(e);
                }
            } else {
                SysLog.detail("> find", request);
                PreparedStatement ps = null;
                String currentStatement = null;
                ResultSet rs = null;
                Connection conn = null;
                String statement = null;
                List<Object> statementParameters = null;
                int lastPosition;
                int lastRowCount;
                boolean countResultSet = false;
                try {
                    conn = Database_2.this.getConnection(ispec, request);
                    short attributeSelector = AttributeSelectors.getAttributeSelector(request);
                    List<FilterProperty> attributeFilter = FilterProperty.getFilterProperties(request.getQueryFilter());
                    List<AttributeSpecifier> attributeSpecifiers = AttributeSpecifier.getAttributeSpecifiers(request.getQueryFilter());
                    Map<String,AttributeSpecifier> attributeSpecifiersAsMap = AttributeSpecifier.getAttributeSpecifierAsMap(request.getQueryFilter());
                    DbObject dbObject = null;
                    /**
                     * prepare SELECT statement
                     */
                    SysLog.trace(DataproviderOperations.toString(DataproviderOperations.ITERATION_START));
                    try {
                        dbObject = Database_2.this.getDbObject(
                            conn,
                            request.getResourceIdentifier(),
                            attributeFilter,
                            true
                        );
                    } catch(ServiceException exception) {
                        if(exception.getCause().getExceptionCode() != BasicException.Code.NOT_FOUND) {
                            throw exception;
                        }
                        SysLog.info(
                            "Could not create dbObject",
                            new IndentingFormatter(
                                ArraysExtension.asMap(
                                    new String[]{
                                        "reason", 
                                        "request path", 
                                        "filter"
                                    },
                                    new Object[]{
                                        exception.getMessage(),
                                        request.getResourceIdentifier(),
                                        attributeFilter
                                    }
                                )
                            )
                        );
                        exception.log();
                        reply.setHasMore(false);
                        reply.setTotal(0L);
                        SysLog.detail("< find");
                        Database_2.this.completeReply(reply);
                        return true;
                    }
                    // Collect query extension
                    Map<String,QueryExtension> queryExtensions = new HashMap<String,QueryExtension>();
                    for(FilterProperty p: attributeFilter) {
                        if(
                            p.name().startsWith(SystemAttributes.CONTEXT_PREFIX) &&
                            p.name().endsWith(SystemAttributes.OBJECT_CLASS) &&
                            Database_1_Attributes.QUERY_EXTENSION_CLASS.equals(p.getValue(0))
                        ) {
                            String id = p.name().substring(0, p.name().indexOf(SystemAttributes.OBJECT_CLASS));
                            queryExtensions.put(
                                id,
                                new QueryExtension(id)
                            );
                        }
                    }
                    // Get attribute and query filter. The query filter is passed as
                    // FilterProperty with context prefix QUERY_FILTER_CONTEXT
                    List<FilterProperty> filterProperties = new ArrayList<FilterProperty>();
                    String columnSelector = DEFAULT_COLUMN_SELECTOR;
                    boolean stated = false;
                    for(FilterProperty p: attributeFilter) {
                        // Test for query extension
                        QueryExtension queryExtension = null;
                        for(String id: queryExtensions.keySet()) {
                            if(p.name().startsWith(id)) {
                                queryExtension = queryExtensions.get(id);
                                break;
                            }
                        }
                        // The filter property 'identity' requires special handling. It
                        // is mapped to the filter property 'object_oid operator values'
                        // This mapping is not required in case of an extent search because
                        // dbObject is already correctly prepared
                        if(SystemAttributes.OBJECT_IDENTITY.equals(p.name())) {
                            if(!request.getResourceIdentifier().isLike(EXTENT_REFERENCES)) {
                                filterProperties.add(
                                    dbObject.mapToIdentityFilterProperty(p)
                                );
                            }
                        } else if(queryExtension != null) {
                            // Query extension clause
                            if(p.name().endsWith(Database_1_Attributes.QUERY_EXTENSION_CLAUSE)) {
                                String clause = (String)p.getValue(0);
                                countResultSet |= clause.indexOf(Database_1_Attributes.HINT_COUNT) >= 0;
                                {
                                    // !COLUMNS
                                    int start = clause.indexOf(Database_1_Attributes.HINT_COLUMN_SELECTOR);
                                    if(start >= 0) {
                                        int end = clause.indexOf("*/", start);
                                        columnSelector = clause.substring(
                                            start + Database_1_Attributes.HINT_COLUMN_SELECTOR.length(),
                                            end
                                        );
                                        clause =
                                            clause.substring(0, start) + 
                                            clause.substring(end + 2);                                
                                    }
                                }
                                // !ORDER BY
                                {
                                    int start = clause.indexOf(Database_1_Attributes.HINT_ORDER_BY);
                                    if(start >= 0) {
                                        int end = clause.indexOf("*/", start);
                                        String orderByClause = clause.substring(
                                            start + Database_1_Attributes.HINT_ORDER_BY.length(),
                                            end
                                        );
                                        StringTokenizer tokenizer = new StringTokenizer(orderByClause, ",", false);
                                        while(tokenizer.hasMoreTokens()) {
                                            String orderByAttribute = tokenizer.nextToken().trim();
                                            SortOrder sortOrder = SortOrder.UNSORTED;
                                            if(orderByAttribute.endsWith("ASC") || orderByAttribute.endsWith("asc")) {
                                                sortOrder = SortOrder.ASCENDING;
                                                orderByAttribute = orderByAttribute.substring(0, orderByAttribute.length() - 3).trim();
                                            } else if(orderByAttribute.endsWith("DESC") || orderByAttribute.endsWith("desc")) {
                                                sortOrder = SortOrder.DESCENDING;
                                                orderByAttribute = orderByAttribute.substring(0, orderByAttribute.length() - 4).trim();                                                
                                            }
                                            attributeSpecifiers.add(
                                                new AttributeSpecifier(
                                                    orderByAttribute,
                                                    0,
                                                    sortOrder.code()
                                                )
                                            );
                                        }
                                        clause =
                                            clause.substring(0, start) + 
                                            clause.substring(end + 2);                                
                                    }
                                }
                                queryExtension.setClause(clause);
                            } else {
                                // Query extension parameters
                                String paramName = p.name().substring(queryExtension.getId().length());                                
                                queryExtension.putParams(
                                    paramName,
                                    Arrays.asList(p.getValues())
                                );
                            }
                        } else if(OBJECT_INSTANCE_OF.equals(p.name())) {
                            if(
                                (p.operator() == ConditionType.IS_IN.code()) &&
                                (p.quantor()  == Quantifier.THERE_EXISTS.code())
                                ) {
                                FilterProperty mappedFilterProperty = Database_2.this.mapInstanceOfFilterProperty(
                                    request,
                                    (Collection<String>) ((Collection<?>) Arrays.asList(p.getValues()))
                                );
                                if(mappedFilterProperty != null) {
                                    for(Object superClass : mappedFilterProperty.values()) {
                                        if(stated) break;
                                        stated = isStated(model, superClass);
                                    }
                                    filterProperties.add(mappedFilterProperty);
                                } 
                            } else {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.ASSERTION_FAILURE,
                                    "Property " + OBJECT_INSTANCE_OF + " only accepts condition " + ConditionType.IS_IN + " and quantor " + Quantifier.THERE_EXISTS,
                                    new BasicException.Parameter("ispec", ispec),
                                    new BasicException.Parameter("input", request)
                                );
                            }                                     
                        } else {
                            // Attribute
                            filterProperties.add(p);              
                        }
                    }
                    lastPosition = -1;
                    lastRowCount = -1;
                    // Mixins
                    Set<String> mixins = new HashSet<String>();
                    // ORDER BY attributes
                    for(AttributeSpecifier attributeSpecifier: attributeSpecifiers) {
                        // Add to orderBy set unless the order is UNSORTED or is an expression
                        if(
                            attributeSpecifier.order() != SortOrder.UNSORTED.code() && 
                            !attributeSpecifier.name().startsWith("(")
                        ) {
                            String attributeName = attributeSpecifier.name();
                            mixins.add(attributeName);
                        }
                    }
                    // Prepare filter properties stored in primary dbObject
                    ModelElement_1_0 referencedTypeDef = model.getTypes(dbObject.getReference())[2];
                    List<FilterProperty> primaryFilterProperties = Database_2.this.getPrimaryFilterProperties(
                        referencedTypeDef,
                        filterProperties
                    );
                    // Add primary filter properties to mixins
                    List<ModelElement_1_0> primaryFilterPropertyDefs = Database_2.this.getFilterPropertyDefs(
                        referencedTypeDef, 
                        primaryFilterProperties
                    );
                    for(int i = 0; i < primaryFilterProperties.size(); i++) {
                        FilterProperty filterProperty = primaryFilterProperties.get(i);
                        ModelElement_1_0 filterPropertyDef = primaryFilterPropertyDefs.get(i);
                        if(
                            (filterPropertyDef == null) ||
                            !filterPropertyDef.isReferenceType() ||
                            model.referenceIsStoredAsAttribute(filterPropertyDef)
                        ) {
                            mixins.add(filterProperty.name());
                        }
                    }
                    // View returning primary attributes. Allows sorting and
                    // filtering with single-valued filter properties
                    String dbObjectHint = null;
                    for(QueryExtension queryExtension: queryExtensions.values()) {
                        String clause = queryExtension.getClause();
                        int posDbObjectHint = clause.indexOf(Database_1_Attributes.HINT_DBOBJECT);
                        if(posDbObjectHint >= 0) {
                            dbObjectHint = clause.substring(
                                posDbObjectHint + Database_1_Attributes.HINT_DBOBJECT.length(),
                                clause.indexOf("*/", posDbObjectHint)
                            );
                            break;
                        }
                    }
                    String view1WithMixinAttributes = Database_2.this.getView(
                        conn,
                        dbObject,
                        dbObjectHint,
                        VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY,
                        columnSelector,
                        mixins
                    );
                    // View returning multi-valued columns which allows filtering
                    // of multi-valued filter properties
                    String view2ForQuery = Database_2.this.getView(
                        conn,
                        dbObject,
                        dbObjectHint,
                        VIEW_MODE_SECONDARY_COLUMNS,
                        columnSelector,
                        null
                    );
                    List<String> includingClauses = new ArrayList<String>();
                    List<List<Object>> includingClausesValues = new ArrayList<List<Object>>();
                    List<String> exludingClauses = new ArrayList<String>();
                    List<List<Object>> excludingClausesValues = new ArrayList<List<Object>>();
                    String joinColumn = "v." + (dbObject.getConfiguration().getDbObjectsForQueryJoinColumn() == null ? dbObject.getObjectIdColumn().get(0) : dbObject.getConfiguration().getDbObjectsForQueryJoinColumn());
                    ModelElement_1_0 referencedType = model.getTypes(dbObject.getReference())[2];                 
                    Database_2.this.filterToSqlClauses(
                        conn,
                        dbObject, 
                        stated,
                        "v", 
                        view1WithMixinAttributes, 
                        view2ForQuery, 
                        JoinType.SPECIFIED_COLUMN_WITH_OBJECT_ID, 
                        joinColumn, 
                        false, // stickyViewAlias
                        referencedType, 
                        filterProperties, 
                        primaryFilterProperties, 
                        includingClauses, 
                        includingClausesValues, 
                        exludingClauses, 
                        excludingClausesValues 
                    );
                    /**
                     * get all slices of objects which match the reference and attribute filter
                     */
                    statement = "";
                    statementParameters = new ArrayList<Object>();
                    statement += view1WithMixinAttributes.startsWith("SELECT") ? 
                        view1WithMixinAttributes + " AND " + dbObject.getReferenceClause() : 
                            "SELECT " + dbObject.getHint() + " " + columnSelector + " FROM " + view1WithMixinAttributes + " v WHERE " + dbObject.getReferenceClause();
                        statementParameters.addAll(
                            dbObject.getReferenceValues()
                            );
                        // Add clause if object id is a pattern
                        if(dbObject.getObjectIdClause().indexOf("LIKE") >= 0) { 
                            statement += " AND " + dbObject.getObjectIdClause();
                            statementParameters.addAll(
                                dbObject.getObjectIdValues()
                            );
                        }
                        // Query extensions
                        for(QueryExtension queryExtension: queryExtensions.values()) {
                            String clause = queryExtension.getClause();
                            int pos = 0;
                            while(
                                (pos < clause.length()) && 
                                ((pos = clause.indexOf("?", pos)) >= 0)
                            ) {
                                int placeHolderEndPos = pos + 2;
                                while(placeHolderEndPos < clause.length() && Character.isDigit(clause.charAt(placeHolderEndPos))) {
                                    placeHolderEndPos++;
                                }
                                int index = Integer.valueOf(clause.substring(pos + 2, placeHolderEndPos)).intValue();
                                if(clause.startsWith("?s", pos)) {
                                    statementParameters.add(
                                        Database_2.this.sqlWildcards.fromJDO(((String)queryExtension.getParams("stringParam").get(index)))
                                    );
                                } else if(clause.startsWith("?i", pos)) {
                                    statementParameters.add(
                                        queryExtension.getParams("integerParam").get(index)
                                    );
                                } else if(clause.startsWith("?n", pos)) {
                                    statementParameters.add(
                                        queryExtension.getParams("decimalParam").get(index)
                                    );
                                } else if(clause.startsWith("?b", pos)) {
                                    statementParameters.add(
                                        queryExtension.getParams("booleanParam").get(index)
                                    );
                                } else if(clause.startsWith("?d", pos)) {
                                    // TODO CR20019719 verify whether replacement is done
                                    statementParameters.add(
                                        queryExtension.getParams("dateParam").get(index)
                                    );
                                } else if(clause.startsWith("?t", pos)) {
                                    statementParameters.add(
                                        queryExtension.getParams("dateTimeParam").get(index)
                                    );
                                }
                                pos++;
                            }
                            statement += " AND (";
                            statement += clause.replaceAll("(\\?[sinbdt]\\d+)", "?");
                            statement += ")";
                            String databaseProductName = conn.getMetaData().getDatabaseProductName();             
                            if(databaseProductName.startsWith("DB2")) {
                                statement = statement.replace("LIKE UPPER(?)", "LIKE ?"); 
                            }
                        }
                        // Positive attribute filter
                        for(
                            int i = 0; 
                            i < includingClauses.size(); 
                            i++
                        ) {
                            String filterClause = includingClauses.get(i);
                            if(filterClause.length() > 0) { 
                                statement += " AND ";
                                statement += filterClause;
                                statementParameters.addAll(
                                    includingClausesValues.get(i)
                                );
                            }
                        }
                        // Negative attribute filter
                        ExcludingClauses: for(String exludingClause : exludingClauses){
                            if(exludingClause.length() > 0) {
                                for(
                                    int i = 0; 
                                    i < exludingClauses.size(); 
                                    i++
                                ) {
                                    String filterClause = exludingClauses.get(i);
                                    if(filterClause.length() > 0) { 
                                        statement += " AND ";
                                        statement += filterClause;
                                        statementParameters.addAll(
                                            excludingClausesValues.get(i)
                                            );
                                    }
                                }
                                break ExcludingClauses;
                            }
                        }
                        // ORDER BY
                        boolean hasOrderBy = false;
                        for(AttributeSpecifier specifier: attributeSpecifiers) {
                            // only add to ORDER set if specified order
                            if(specifier.order() != SortOrder.UNSORTED.code()) {
                                if(!hasOrderBy) {
                                    statement += " ORDER BY"; 
                                }
                                boolean viewIsIndexed = dbObject.getIndexColumn() != null;              
                                statement += hasOrderBy ? ", " : " ";
                                if(specifier.name().startsWith("(")) {
                                    statement += 
                                        specifier.name() + 
                                        (specifier.order() == SortOrder.DESCENDING.code() 
                                            ? " DESC" + (Database_2.this.isOrderNullsAsEmpty() ? " NULLS LAST" : "")
                                            : " ASC" + (Database_2.this.isOrderNullsAsEmpty() ? " NULLS FIRST" : "")
                                        );
                                } else {
                                    // order on mixin view (vm.) in case of indexed slices, otherwise on primary view (v.)
                                    statement += 
                                        (viewIsIndexed ? "vm." : "v.") +
                                        getColumnName(conn, specifier.name(), 0, false, true, false) +
                                        (specifier.order() == SortOrder.DESCENDING.code() 
                                            ? " DESC" + (Database_2.this.isOrderNullsAsEmpty() ? " NULLS LAST" : "")
                                            : " ASC" + (Database_2.this.isOrderNullsAsEmpty() ? " NULLS FIRST" : "")
                                        );
                                }
                                hasOrderBy = true;
                            }
                        }
                        switch(getOrderAmendment(conn, dbObject)) {
                            case BY_OBJECT_ID:
                                // ORDER BY object identity is required. Otherwise, iteration
                                // may not be deterministic. 
                                if(!hasOrderBy) statement += " ORDER BY"; 
                                // rid
                                for(String referenceColumn : dbObject.getReferenceColumn()){
                                    statement += hasOrderBy ? ", " : " ";
                                    statement += "v." + referenceColumn;
                                    hasOrderBy = true;
                                }
                                // oid
                                for(Object objectIdColumn : dbObject.getObjectIdColumn()) {
                                    statement += hasOrderBy ? ", " : " ";
                                    statement += "v." + objectIdColumn;         
                                    hasOrderBy = true;
                                }
                                // idx
                                if(dbObject.getIndexColumn() != null) {
                                    statement += hasOrderBy ? ", " : " ";
                                    statement += "v." + dbObject.getIndexColumn();
                                    hasOrderBy = true;
                                }
                                break;
                            case INTRINSIC:
                                // The database's intrinsic ordering is (relatively) stable
                                // (compared to the probability of concurrent modifications)
                                break;
                        }
                        // Prepare and ...
                        ps = Database_2.this.prepareStatement(
                            conn,
                            currentStatement = statement.toString()
                        );
                        try {
                            //
                            // ... fill in statement parameters ...
                            //
                            for(
                                int i = 0, iLimit = statementParameters.size(); 
                                i < iLimit; 
                                i++
                            ) {
                                Database_2.this.setPreparedStatementValue(
                                    conn,
                                    ps,
                                    i+1, 
                                    statementParameters.get(i)
                                    );
                            }
                        } catch (ServiceException exception) {
                            throw new ServiceException(
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.GENERIC,
                                "Can't propagate the parameters to the prepared statement",
                                new BasicException.Parameter("statement", currentStatement)
                                );
                        }
                        // ... and finally execute
                        int requestedSize = Database_2.this.getFetchSizeT().adjust(
                            request.getSize() == null 
                                ? 0 
                                : request.getSize().intValue()
                            );
                        rs = Database_2.this.executeQuery(
                            ps,
                            statement.toString(),
                            statementParameters, 
                            requestedSize
                        );
                        // get selected objects
                        List<ObjectRecord> objects = new ArrayList<ObjectRecord>();
                        int replyPosition = request.getPosition() == null ? 0 : request.getPosition().intValue();
                        if(request.getPosition() != null && request.getPosition().longValue() < 0) {
                            if(requestedSize > replyPosition) requestedSize = replyPosition + 1;
                            replyPosition = replyPosition + 1 - requestedSize;
                        }
                        boolean hasMore = Database_2.this.getObjects(
                            conn,
                            dbObject,
                            rs,
                            objects,
                            attributeSelector,
                            attributeSpecifiersAsMap,
                            false, // objectClassAsAttribute
                            replyPosition,
                            lastPosition,
                            lastRowCount,
                            requestedSize,
                            null
                        );
                        // Complete requested attributes
                        if(!objects.isEmpty()) {
                            Object_2Facade facade2 = Facades.asObject(objects.get(0));
                            boolean fetchAll = false;          
                            if(attributeSelector == AttributeSelectors.ALL_ATTRIBUTES) {
                                fetchAll = true;
                            }
                            else if(attributeSelector == AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES) {
                                for(AttributeSpecifier attributeSpecifier: attributeSpecifiers) {
                                    if(!facade2.getValue().keySet().contains(attributeSpecifier.name())) {
                                        fetchAll = true;
                                        break;
                                    }
                                }
                            }
                            DbObject dbObject2 = Database_2.this.createDbObject(
                                conn,
                                facade2.getPath(),
                                true
                            );
                            // Additional fetch only if secondary dbObject is available
                            boolean hasSecondaryDbObject =
                                (dbObject2.getConfiguration().getDbObjectForQuery2() != null) ||
                                (dbObject2.getConfiguration().getDbObjectForUpdate2() != null);                
                            if(fetchAll && hasSecondaryDbObject) {
                                // Query to retrieve the secondary columns
                                // SELECT v.* FROM secondary table WHERE (object id clause 0) OR (object id clause 1) ... ORDER BY object ids, idx
                                List<Object> statementParameters2 = new ArrayList<Object>();
                                String statement2 =
                                    "SELECT v.* FROM " + (dbObject2.getConfiguration().getDbObjectForQuery2() == null ? dbObject2.getConfiguration().getDbObjectForUpdate2() : dbObject.getConfiguration().getDbObjectForQuery2()) + " v" +
                                        " WHERE (";
                                String separator = "";
                                for(ObjectRecord object : objects){
                                    dbObject2 = Database_2.this.createDbObject(
                                        conn, 
                                        object.getResourceIdentifier(), 
                                        true
                                    );
   
                                    statement2 += separator;
                                    statement2 += dbObject2.getObjectIdClause();
                                    statementParameters2.addAll(
                                        dbObject2.getObjectIdValues()
                                    );
                                    separator = " OR ";
                                }
                                statement2 += ") ORDER BY ";
                                separator = "";
                                for(Object objectIdColumn : dbObject2.getObjectIdColumn()) {
                                    statement2 += separator;
                                    statement2 += "v." + objectIdColumn; 
                                    separator = ",";
                                }
                                statement2 += ", v." + Database_2.this.OBJECT_IDX;
                                PreparedStatement ps2 = Database_2.this.prepareStatement(
                                    conn,
                                    currentStatement = statement2.toString()
                                );
                                for(
                                    int i = 0, iLimit = statementParameters2.size(); 
                                    i < iLimit; 
                                    i++
                                    ) {
                                    Database_2.this.setPreparedStatementValue(
                                        conn,
                                        ps2,
                                        i+1, 
                                        statementParameters2.get(i)
                                    );
                                }
                                ResultSet rs2 = Database_2.this.executeQuery(
                                    ps2,
                                    statement2.toString(),
                                    statementParameters2, FetchPlan.FETCH_SIZE_OPTIMAL
                                );
                                List<ObjectRecord> objects2 = new ArrayList<ObjectRecord>();
                                Database_2.this.getObjects(
                                    conn,
                                    dbObject2,
                                    rs2,
                                    objects2,
                                    AttributeSelectors.ALL_ATTRIBUTES,
                                    Collections.<String,AttributeSpecifier>emptyMap(),
                                    false, // objectClassAsAttribute
                                    0, -1, -1, requestedSize,
                                    facade2.getObjectClass()
                                );
                                // Add attributes of objects2 to objects
                                Map<Path,ObjectRecord> objects2AsMap = new HashMap<Path,ObjectRecord>();
                                for(ObjectRecord object2: objects2) {
                                    objects2AsMap.put(
                                        object2.getResourceIdentifier(), 
                                        object2
                                    );
                                }
                                for(ObjectRecord object: objects) {
                                    ObjectRecord object2 = objects2AsMap.get(object.getResourceIdentifier());
                                    if(object2 != null) {
                                        Object_2Facade.getValue(object2).keySet().removeAll(
                                            object.keySet()
                                        );
                                        Object_2Facade.getValue(object).putAll(
                                            Object_2Facade.getValue(object2)
                                        );
                                    }
                                }
                                rs2.close(); rs2 = null;
                                ps2.close(); ps2 = null;
                            }              
                        }
                        lastRowCount = Database_2.this.resultSetGetRow(rs);
                        rs.close(); rs = null;
                        ps.close(); ps = null;
                        SysLog.log(Level.FINE, "Sys|*** hasMore={0}|objects.size()={1}", Boolean.valueOf(hasMore), Integer.valueOf(objects.size()));
   
                        // reply 
                        reply.addAll(objects);
   
                        // context.HAS_MORE
                        reply.setHasMore(hasMore);
   
                        // Calculate context.TOTAL only when iterating
                        if(request.getResourceIdentifier().isContainerPath()) {      
                            // if !hasMore context.TOTAL = request.position() + objects.size()
                            long position = request.getPosition() == null ? 0L : request.getPosition().longValue();
                            if(!hasMore && ((position == 0) || !objects.isEmpty())) {
                                reply.setTotal(position + objects.size());
                            }
                            // Issue a SELECT COUNT(*) if the result set is not indexed and counting is requested
                            else if(
                                countResultSet &&
                                (dbObject.getIndexColumn() == null)
                            ) {
                                String countStatement = statement;
                                if(countStatement.startsWith("SELECT")) {
                                    countStatement = "SELECT COUNT(*) " + countStatement.substring(countStatement.indexOf("FROM"));
                                    if(countStatement.indexOf("ORDER BY") > 0) {
                                        countStatement = countStatement.substring(0, countStatement.indexOf("ORDER BY"));
                                    }
                                    ps = Database_2.this.prepareStatement(
                                        conn,
                                        currentStatement = countStatement.toString()
                                    );        
                                    for(
                                        int i = 0, iLimit = statementParameters.size(); 
                                        i < iLimit; 
                                        i++
                                    ) {
                                        Database_2.this.setPreparedStatementValue(
                                            conn,
                                            ps,
                                            i+1, 
                                            statementParameters.get(i)
                                        );
                                    }              
                                    rs = Database_2.this.executeQuery(
                                        ps,
                                        countStatement.toString(),
                                        statementParameters, 
                                        1
                                    );
                                    if(rs.next()) {
                                        reply.setTotal(rs.getInt(1));
                                    }
                                    rs.close(); rs = null;
                                    ps.close(); ps = null;
                                }
                            }
                        }
                } catch(SQLException exception) {
                    throw ResourceExceptions.initHolder(
                        new ResourceException(
                            "Error when executing SQL statement",
                            BasicException.newEmbeddedExceptionStack(
                                exception, 
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                                null,
                                new BasicException.Parameter("path", request.getResourceIdentifier()),
                                new BasicException.Parameter("statement", currentStatement),
                                new BasicException.Parameter("parameters", statementParameters),
                                new BasicException.Parameter("sqlErrorCode", exception.getErrorCode()), 
                                new BasicException.Parameter("sqlState", exception.getSQLState())
                            )
                         )
                     );
                } catch(ServiceException exception) {
                    throw ResourceExceptions.initHolder(
                        new ResourceException(
                            BasicException.newEmbeddedExceptionStack(exception)
                        )
                    );
                } catch(Exception exception) {
                    throw new ResourceException(exception);
                } finally {
                    Database_2.this.close(rs);
                    Database_2.this.close(ps);
                    Database_2.this.close(conn);
                }
                SysLog.trace("< find");
                try {
                    Database_2.this.completeReply(reply);
                } catch(ServiceException e) {
                    throw new ResourceException(e);
                }
            }
            return true;
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#create(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean create(
            RestInteractionSpec ispec, 
            ObjectRecord request, 
            ResultRecord reply
        ) throws ResourceException {
            SysLog.detail("> create", request);
            Connection conn = null; 
            try {
                conn = Database_2.this.getConnection(ispec, request);
                Database_2.this.create(
                    conn,
                    ispec,
                    request,
                    reply
                );
            } catch(ServiceException exception) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );                
            } catch(Exception exception) {
                throw new ResourceException(exception);
            } finally {
                Database_2.this.close(conn);
            }
            return true;
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean delete(
            RestInteractionSpec ispec, 
            ObjectRecord request
        ) throws ResourceException {
            SysLog.detail("> remove", request);
            Connection conn = null;    
            String currentStatement = null;
            List<Object> statementParameters = null;
            try {
                conn = Database_2.this.getConnection(ispec, request);
                Path accessPath = request.getResourceIdentifier();
                // Does object exist?
                ResultRecord reply = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
                Database_2.this.get(
                    conn,
                    ispec,
                    accessPath,
                    AttributeSelectors.NO_ATTRIBUTES,
                    Collections.<String,AttributeSpecifier>emptyMap(),
                    false, // objectClassAsAttribute
                    reply, 
                    true // throwNotFoundException
                );
                // Remove object ...
                Database_2.this.createDbObject(
                    conn,
                    request.getResourceIdentifier(),
                    true
                ).remove();
                // ... and its composites
                Map<Path,DbObjectConfiguration> processedDbObjectConfigurations = new HashMap<Path,DbObjectConfiguration>();
                for(DbObjectConfiguration dbObjectConfiguration : Database_2.this.getDatabaseConfiguration().getDbObjectConfigurations()) {
                    if(
                        (dbObjectConfiguration.getType().size() > accessPath.size()) &&
                        accessPath.isLike(dbObjectConfiguration.getType().getPrefix(accessPath.size())) &&
                        !processedDbObjectConfigurations.containsKey(dbObjectConfiguration.getType())
                    ) {
                        boolean processed = false;
                        // Check whether dbObjectConfiguration is already processed
                        for(
                            Iterator<DbObjectConfiguration> j = processedDbObjectConfigurations.values().iterator();
                            j.hasNext();
                        ) {
                            DbObjectConfiguration processedDbObjectConfiguration = j.next();
                            // dbObject is processed if type if 
                            // <ul>
                            //   <li>db object is composite to processed db object
                            //   <li>dbObjectForUpdate1 are equal
                            // </ul>
                            boolean dbObjectForUpdate1Matches = (dbObjectConfiguration.getDbObjectForUpdate1() == null) || (processedDbObjectConfiguration.getDbObjectForUpdate1() == null) ? 
                                dbObjectConfiguration.getDbObjectForUpdate1() == processedDbObjectConfiguration.getDbObjectForUpdate1() : 
                                    dbObjectConfiguration.getDbObjectForUpdate1().equals(processedDbObjectConfiguration.getDbObjectForUpdate1());                      
                                if(                              
                                    dbObjectForUpdate1Matches &&
                                    (dbObjectConfiguration.getType().size() > processedDbObjectConfiguration.getType().size()) &&
                                    dbObjectConfiguration.getType().getPrefix(processedDbObjectConfiguration.getType().size()).isLike(processedDbObjectConfiguration.getType())
                                    ) {
                                    processed = true;
                                    break;
                                }
                        }
                        // Remove if not processed
                        if(!processed) {
                            Database_2.this.createDbObject(
                                conn,
                                dbObjectConfiguration,
                                request.getResourceIdentifier(),
                                true
                            ).remove();
                            processedDbObjectConfigurations.put(
                                dbObjectConfiguration.getType(),
                                dbObjectConfiguration
                            );
                        }
                    }
                }
                // Clean up REF table
                if(!Database_2.this.isUseNormalizedReferences()) {
                    statementParameters = new ArrayList<Object>();
                    PreparedStatement ps = Database_2.this.prepareStatement(
                        conn,
                        currentStatement = 
                        "DELETE FROM " + Database_2.this.namespaceId + "_" + Database_2.T_REF + 
                        " WHERE " + Database_2.this.getSelectReferenceIdsFromRefTableClause(conn, request.getResourceIdentifier().getChild("%"), statementParameters)
                    );
                    for(int i = 0; i < statementParameters.size(); i++) {
                        Database_2.this.setPreparedStatementValue(
                            conn,
                            ps, 
                            i+1, 
                            statementParameters.get(i)
                        );
                    }
                    executeUpdate(ps, currentStatement, statementParameters);
                    ps.close(); ps = null;
                }
            } catch(SQLException exception) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        "Error when executing SQL statement",
                        BasicException.newEmbeddedExceptionStack(
                            exception, 
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.MEDIA_ACCESS_FAILURE, 
                            null,
                            new BasicException.Parameter("path", request.getResourceIdentifier()),
                            new BasicException.Parameter("errorCode", exception.getErrorCode()),
                            new BasicException.Parameter("statement", currentStatement),
                            new BasicException.Parameter("parameters", statementParameters),
                            new BasicException.Parameter("sqlErrorCode", exception.getErrorCode()), 
                            new BasicException.Parameter("sqlState", exception.getSQLState())
                        )
                     )
                );
            } catch(ServiceException exception) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } finally {
                Database_2.this.close(conn);
            }
            return true;
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#put(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean update(
            RestInteractionSpec ispec, 
            ObjectRecord object, 
            ResultRecord output
        ) throws ResourceException {
            SysLog.detail("> replace", object);
            PreparedStatement ps = null;
            String currentStatement = null;
            Connection conn = null;
            List<Object> objectIdValues = null;
            try {
                conn = Database_2.this.getConnection(ispec, object);
                DbObject dbObject = Database_2.this.createDbObject(
                    conn,
                    object.getResourceIdentifier(),
                    true
                );
                MappedRecord newValue = object.getValue();
                Object writeLock = object.getVersion();
                Object readLock = object.getLock();
                // Get current object with ALL_ATTRIBUTES. objectClassAsAttribute=true
                // asserts that empty rows (all columns with null values) are not truncated
                ResultRecord oldObject = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
                Database_2.this.get(
                    conn,
                    ispec,
                    object.getResourceIdentifier(),
                    AttributeSelectors.ALL_ATTRIBUTES,
                    Collections.<String,AttributeSpecifier>emptyMap(),
                    true, // objectClassAsAttribute
                    oldObject, 
                    true // throwNotFoundException
                );
                ObjectRecord obj = (ObjectRecord)oldObject.get(0);
                MappedRecord oldValue = obj.getValue();
                if(!newValue.isEmpty()) {
                    ObjectRecord[] oldSlices = dbObject.sliceAndNormalizeObject(obj, false);
                    // Replace attribute values
                    oldValue.putAll(newValue);
                    Database_2.this.removeAttributes(
                        obj, 
                        true, // removePrivate
                        true, // removeNonPersistent
                        true // removeSize
                    );
                    ObjectRecord[] newSlices = dbObject.sliceAndNormalizeObject(obj, true);
                    // Replace existing slices
                    for(
                        int i = 0; 
                        i < java.lang.Math.min(oldSlices.length, newSlices.length);
                        i++
                        ) {
                        if(!newSlices[i].equals(oldSlices[i])) {
                            if(i == 0) {
                                dbObject.replaceObjectSlice(
                                    i,
                                    newSlices[i],
                                    oldSlices[i], 
                                    toWriteLock(writeLock), 
                                    toReadLock(readLock)
                                );
                            } else {
                                dbObject.replaceObjectSlice(
                                    i,
                                    newSlices[i],
                                    oldSlices[i], 
                                    null, 
                                    null
                                );
                            }
                        }
                    }
                    // Remove extra old slices
                    if(oldSlices.length > newSlices.length) {
                        boolean isIndexed;
                        if(dbObject.getConfiguration().getDbObjectForUpdate2() != null) {
                            isIndexed = true;
                            ps = Database_2.this.prepareStatement(
                                conn,
                                currentStatement = 
                                "DELETE FROM " + dbObject.getConfiguration().getDbObjectForUpdate2() + 
                                " WHERE " + 
                                Database_2.this.removeViewPrefix(
                                    dbObject.getReferenceClause() + 
                                    " AND " + dbObject.getObjectIdClause() + 
                                    " AND (" + OBJECT_IDX + " >= ?)"
                                    )
                                );            
                        } else {
                            isIndexed = dbObject.getIndexColumn() != null;
                            ps = Database_2.this.prepareStatement(
                                conn,
                                currentStatement = 
                                "DELETE FROM " + dbObject.getConfiguration().getDbObjectForUpdate1() + 
                                " WHERE " + 
                                Database_2.this.removeViewPrefix(
                                    dbObject.getReferenceClause() + 
                                    " AND " + dbObject.getObjectIdClause() + 
                                    (isIndexed ? " AND (" + dbObject.getIndexColumn() + " >= ?)" : "")
                                )
                            );
                        }
                        int pos = 1;
                        List<Object> referenceValues = dbObject.getReferenceValues();
                        for(Object referenceValue : referenceValues) {
                            Database_2.this.setPreparedStatementValue(
                                conn,
                                ps, 
                                pos++, 
                                referenceValue
                            );
                        }
                        objectIdValues = dbObject.getObjectIdValues();
                        for(Object objectIdValue : objectIdValues) {
                            Database_2.this.setPreparedStatementValue(
                                conn,
                                ps, 
                                pos++, 
                                objectIdValue
                            );
                        }
                        if(isIndexed) {
                            ps.setInt(pos++, newSlices.length);
                        }
                        executeUpdate(ps, currentStatement, objectIdValues);
                    }
                    // Create extra new slices
                    if(newSlices.length > oldSlices.length) {
                        String objectClass = Object_2Facade.getObjectClass(object);
                        for(
                            int i = oldSlices.length;
                            i < newSlices.length;
                            i++
                        ) {
                            dbObject.createObjectSlice(
                                i,
                                objectClass,
                                newSlices[i]
                            );
                        }
                    }
                } else {
                    if(writeLock instanceof byte[]){
                        Object version = obj.getVersion();
                        if(Arrays.equals((byte[])writeLock, (byte[])version)) {
                            removePrivateAttributes(obj);
                        } else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                                "The object has been modified since it has been read",
                                new BasicException.Parameter("path", object.getResourceIdentifier()),
                                new BasicException.Parameter("expected", writeLock),
                                new BasicException.Parameter("actual", version)
                            );
                        }
                    } else if (writeLock != null){
                        SysLog.warning("Optimistic write lock expects a byte[] version", writeLock.getClass().getName());
                    }
                    if(LockAssertions.isReadLockAssertion(readLock)) {
                        java.util.Date transactionTime = LockAssertions.getTransactionTime(readLock);
                        java.util.Date modifiedAt = (java.util.Date) obj.getValue().get(SystemAttributes.MODIFIED_AT);
                        if(modifiedAt == null) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                                "The object's modification time can't be determined",
                                new BasicException.Parameter("path", object.getResourceIdentifier()),
                                new BasicException.Parameter("expected", readLock),
                                new BasicException.Parameter("actual")
                            );
                        } else if (transactionTime.before(modifiedAt)) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                                "The object has been modified since the unit of work has started",
                                new BasicException.Parameter("xri", object.getResourceIdentifier()),
                                new BasicException.Parameter("expected", readLock),
                                new BasicException.Parameter(
                                    "actual", SystemAttributes.MODIFIED_AT + '=' + DateTimeFormat.EXTENDED_UTC_FORMAT.format(modifiedAt)
                                )
                            );
                        }
                    } else if (readLock != null){
                        SysLog.warning("Optimistic read lock expects a modifiedAt<=transactionTime assertion", readLock);
                    }
                }
                return true;
            } catch(SQLException exception) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        "Error when executing SQL statement",
                        BasicException.newEmbeddedExceptionStack(
                            exception, 
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.MEDIA_ACCESS_FAILURE, 
                            null,
                            new BasicException.Parameter("path", object.getResourceIdentifier()),
                            new BasicException.Parameter("statement", currentStatement),
                            new BasicException.Parameter("parameters", objectIdValues),
                            new BasicException.Parameter("sqlErrorCode", exception.getErrorCode()), 
                            new BasicException.Parameter("sqlState", exception.getSQLState())
                        )
                    )
                );
            } catch(ServiceException exception) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } catch(Exception exception) {
                throw new ResourceException(exception);
            } finally {
                Database_2.this.close(ps);
                Database_2.this.close(conn);
            }
        }
    }

    /**
     * Create a temporary file to determine a large object's length
     * 
     * @return a new temporary file
     * 
     * @throws IOException
     * 
     * @deprecated("For JRE 5/setStreamByValue support only")
     */
    @Deprecated
    private File newTemporaryFile(
    ) throws IOException{
        File file = File.createTempFile(
            UUIDs.newUUID().toString(),
            null,
            this.streamBufferDirectory
        );
        temporaryFiles.add(file);
        return file;
    }

    /**
     * Create Binary Large Object
     * 
     * @param stream
     * @param length
     * 
     * @return the newly create BLOB
     * 
     * @throws IOException
     * 
     * @deprecated("For JRE 5/setStreamByValue support only")
     */
    @Deprecated
    protected BinaryLargeObject tallyLargeObject(
        InputStream stream
    ) throws IOException {
        File file = newTemporaryFile();
        OutputStream target = new FileOutputStream(file);
        long length;
        try {
            length = BinaryLargeObjects.streamCopy(stream, 0, target);
        } finally {
            stream.close();
            target.flush();
            target.close();
        }
        return BinaryLargeObjects.valueOf(
            new FileInputStream(file), 
            Long.valueOf(length)
        );
    }

    /**
     * Create Character Large Object
     * 
     * @param stream
     * @param length
     * 
     * @return the newly created CLOB
     * @throws IOException
     * 
     * @deprecated("For JRE 5/setStreamByValue support only")
     */
    @Deprecated
    protected CharacterLargeObject tallyLargeObject(
        Reader stream
    ) throws IOException{
        File file = newTemporaryFile();
        Writer target = new OutputStreamWriter(
            new FileOutputStream(file),
            "UTF-16"
        );
        long length;
        try {
            length = CharacterLargeObjects.streamCopy(stream, 0, target);
        } finally {
            stream.close();
            target.flush();
            target.close();
        }
        return CharacterLargeObjects.valueOf(
            new InputStreamReader(
                new FileInputStream(file), 
                "UTF-16"
            ), 
            Long.valueOf(length)
        );
    }

    /**
     * Retrieves the configured date time type
     * 
     * @param connection
     * 
     * @return the date time type to be used
     * 
     * @throws ServiceException if meta data retrieval fails
     */
    public String getDateTimeType(
        Connection connection
    ) throws ServiceException {
        return LayerConfigurationEntries.DATETIME_TYPE_STANDARD.equals(this.dateTimeType)? getDriverProperty(
            connection,
            "DATETIME.TYPE.STANDARD",
            LayerConfigurationEntries.DATETIME_TYPE_CHARACTER
        ) : this.dateTimeType;
    }

    /**
     * The escape clause

     * @param connection
     * 
     * @return the escape clause
     * 
     * @throws ServiceException
     */
    @Override
    public String getEscapeClause(
        Connection connection
    ) throws ServiceException{      
        return getDriverProperty(
            connection,
            "ESCAPE.CLAUSE",
            "" // "ESCAPE '\\'"
        );
    }

    /**
     * Add the escape character '\\' to an equal value
     * 
     * @param equalValue
     * 
     * @return the corresponding like value
     */
    public static String escape(
        String equalValue
    ){
        return equalValue.replaceAll(
            "([_%\\\\])", 
            "\\\\$1"
        );
    }

    /**
     * Remove the escape character '\\' from a like value
     * 
     * @param likeValue
     * 
     * @return the corresponding equals value
     */
    public static String unescape(
        String likeValue
    ){
        return likeValue.indexOf('\\') < 0 ? likeValue : likeValue.replaceAll(
            "\\\\([_%\\\\])", 
            "$1"
        );
    }

    /**
     * Retrieves the configured date type
     * 
     * @param connection
     * 
     * @return the date type to be used
     * 
     * @throws ServiceException if meta data retrieval fails
     */
    public String getDateType(
        Connection connection
    ) throws ServiceException {
        return LayerConfigurationEntries.DATE_TYPE_STANDARD.equals(this.dateType)? getDriverProperty(
            connection,
            "DATE.TYPE.STANDARD",
            LayerConfigurationEntries.DATETIME_TYPE_CHARACTER
        ) : this.dateType;       
    }

    /**
     * Retrieves the configured time type
     * 
     * @param connection
     * 
     * @return the time type to be used
     * 
     * @throws ServiceException if meta data retrieval fails
     */
    public String getTimeType(
        Connection connection
    ) throws ServiceException {
        return LayerConfigurationEntries.TIME_TYPE_STANDARD.equals(this.timeType)? getDriverProperty(
            connection,
            "TIME.TYPE.STANDARD",
            LayerConfigurationEntries.TIME_TYPE_CHARACTER
        ) : this.timeType;       
    }

    /**
     * Retrieves the configured boolean type
     * 
     * @param connection
     * 
     * @return the boolean type to be used
     * 
     * @throws ServiceException if meta data retrieval fails
     */
    public String getBooleanType(
        Connection connection
    ) throws ServiceException {
        return LayerConfigurationEntries.BOOLEAN_TYPE_STANDARD.equals(this.booleanType)  ? getDriverProperty(
            connection,
            "BOOLEAN.TYPE.STANDARD",
            LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER
        ) : this.booleanType;       
    }

    private SetLargeObjectMethod howToSetLargeObject(
        Connection connection, 
        String id
    ) throws ServiceException {
        return SetLargeObjectMethod.valueOf(
            getDriverProperty(
                connection,
                id,
                SetLargeObjectMethod.TALLYING.name()
            )
        );
    }

    protected OrderAmendment getOrderAmendment(
        Connection connection, 
        DbObject dbObject
    ) throws ServiceException {
        if(dbObject.getIndexColumn() == null) {
            return OrderAmendment.valueOf(
                getDriverProperty(
                    connection,
                    "ORDER.AMENDMENT",
                    OrderAmendment.BY_OBJECT_ID.name()
                )
            );
        } else {
            return OrderAmendment.BY_OBJECT_ID;
        }
    }

    public SetLargeObjectMethod howToSetBinaryLargeObject (
        Connection connection
    ) throws ServiceException {
        return howToSetLargeObject(connection, "SET.BINARY.LARGE.OBJECT");
    }

    public SetLargeObjectMethod howToSetCharacterLargeObject (
        Connection connection
    ) throws ServiceException {
        return howToSetLargeObject(connection, "SET.CHARACTER.LARGE.OBJECT");
    }
    
    /**
     * Retrieve the version
     *  
     * @param a PUT request
     * 
     * @return the version
     */
    protected static byte[] getVersion(
        ObjectRecord request
    ){
        return (byte[])request.getVersion();
    }

    /**
     * Build the lock assertion
     * 
     * @param version an encoded lock assertion
     * 
     * @return the decoded lock assertion
     * 
     * @throws ServiceException 
     */
    protected String toWriteLock(
        Object version
    ) throws ServiceException{
        if(version instanceof byte[]) {
            byte[] writeLock = (byte[]) version;
            return writeLock.length == 0 ? null : UnicodeTransformation.toString(
                writeLock, 
                0, // offset
                writeLock.length
            );
        } else if (version == null){
            return null;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Unsupported write lock class",
                new BasicException.Parameter("expected",byte[].class.getName()),
                new BasicException.Parameter("actual",version.getClass().getName())
            );
        }
    }

    /**
     * Build the read lock assertion
     * 
     * @param lock a lock assertion
     * 
     * @return the decoded lock assertion
     * 
     * @throws ServiceException 
     */
    protected String toReadLock(
        Object lock
    ) throws ServiceException{
        if(lock instanceof String) {
            String readLock = (String) lock;
            return readLock.length() == 0 ? null : readLock;
        } else if (lock == null){
            return null;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Unsupported write lock class",
                new BasicException.Parameter("expected",String.class.getName()),
                new BasicException.Parameter("actual",lock.getClass().getName())
            );
        }
    }

    /**
     * Tells whether the class denotes a base class not to be checked in the select
     * clause as they shall never be put into the same table with other classes not
     * matching this criteria.
     *  
     * @param qualifiedClassName
     * 
     * @return <code>true</code> if the class shall not be checked
     */
    protected boolean isBaseClass(
        String qualifiedClassName
    ){
        return BASE_CLASSES.contains(qualifiedClassName);
    }
        
    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0#isEmbeddedFeature(java.lang.String)
     */
    @Override
    public boolean isEmbeddedFeature(String featureName) {
        return embeddedFeatures.containsKey(featureName);
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------

    protected static final String T_REF = "REF";
    protected static final String UNDEF_OBJECT_CLASS = "#undef";
    protected static final String SIZE_SUFFIX = "_";
    protected static final int ROUND_UP_TO_MAX_SCALE = 15;

    protected BooleanMarshaller booleanMarshaller;
    protected Marshaller durationMarshaller;
    protected XMLGregorianCalendarMarshaller calendarMarshaller;
    protected String booleanType = LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER;
    protected String booleanFalse = null;
    protected String booleanTrue = null;
    protected String durationType = LayerConfigurationEntries.DURATION_TYPE_CHARACTER;
    protected String dateTimeType = LayerConfigurationEntries.DATETIME_TYPE_CHARACTER;
    protected String timeType = LayerConfigurationEntries.TIME_TYPE_CHARACTER;
    protected String dateType = LayerConfigurationEntries.DATE_TYPE_CHARACTER;
    protected String dateTimeZone = TimeZone.getDefault().getID();
    protected String dateTimeDaylightZone = null;
    protected String dateTimePrecision = TimeUnit.MICROSECONDS.name();
    protected boolean normalizeObjectIds = false;
    protected boolean useViewsForRedundantColumns = true;
    protected boolean usePreferencesTable = true;
    protected boolean cascadeDeletes = true;
    protected boolean orderNullsAsEmpty = false;
    protected SparseArray<String> columnNameFrom = new TreeSparseArray<String>();
    protected SparseArray<String> columnNameTo = new TreeSparseArray<String>();
    protected SparseArray<Path> type = new TreeSparseArray<Path>();
    protected SparseArray<String> typeName = new TreeSparseArray<String>();
    protected SparseArray<String> dbObject = new TreeSparseArray<String>();
    protected SparseArray<String> dbObject2 = new TreeSparseArray<String>();
    protected SparseArray<String> dbObjectFormat = new TreeSparseArray<String>();
    protected SparseArray<Integer> pathNormalizeLevel = new TreeSparseArray<Integer>();
    protected SparseArray<String> dbObjectForQuery = new TreeSparseArray<String>();
    protected SparseArray<String> dbObjectForQuery2 = new TreeSparseArray<String>();
    protected SparseArray<String> dbObjectsForQueryJoinColumn = new TreeSparseArray<String>();
    protected SparseArray<String> dbObjectHint = new TreeSparseArray<String>();
    protected SparseArray<String> objectIdPattern = new TreeSparseArray<String>();
    protected SparseArray<String> joinTable = new TreeSparseArray<String>();
    protected SparseArray<String> joinColumnEnd1 = new TreeSparseArray<String>();
    protected SparseArray<String> joinColumnEnd2 = new TreeSparseArray<String>();
    protected SparseArray<String> unitOfWorkProvider = new TreeSparseArray<String>();
    protected SparseArray<String> removableReferenceIdPrefix = new TreeSparseArray<String>();
    protected SparseArray<Boolean> disableAbsolutePositioning = new TreeSparseArray<Boolean>();
    protected SparseArray<String> referenceIdPattern = new TreeSparseArray<String>();
    protected SparseArray<String> autonumColumn =new TreeSparseArray<String>();
    protected boolean getLargeObjectByValue = true;

    // VIEW_MODE
    protected static final short VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY = 0;
    protected static final short VIEW_MODE_SECONDARY_COLUMNS = 1;

    protected static final Set<String> SYSTEM_ATTRIBUTES = new HashSet<String>(
        Arrays.asList(
            SystemAttributes.OBJECT_CLASS, 
            SystemAttributes.CREATED_AT,
            SystemAttributes.CREATED_BY, 
            SystemAttributes.MODIFIED_AT,
            SystemAttributes.MODIFIED_BY, 
            SystemAttributes.REMOVED_AT,
            SystemAttributes.REMOVED_BY,
            "stateVersion"
        )
    );

    public static final String DEFAULT_OID_SUFFIX = "_objectid";
    public static final String DEFAULT_RID_SUFFIX = "_referenceid";
    public static final String DEFAULT_RSX_SUFFIX = "_referenceIdSuffix";
    public static final String DEFAULT_COLUMN_SELECTOR = "v.*";
    public static final String DEFAULT_PRIVATE_ATTRIBUTE_PREFIX = "p$$";

    private String privateAttributesPrefix = DEFAULT_PRIVATE_ATTRIBUTE_PREFIX;
    private String objectIdAttributesSuffix = DEFAULT_OID_SUFFIX;
    private String referenceIdAttributesSuffix = DEFAULT_RID_SUFFIX;
    private String referenceIdSuffixAttributesSuffix = DEFAULT_RID_SUFFIX;
    
    // Reserved words
    protected static final Set<String> RESERVED_WORDS_HSQLDB = new HashSet<String>(
        Arrays.asList(   
            "position", 
            "POSITION"
        )
    );
    protected static final Set<String> RESERVED_WORDS_ORACLE = new HashSet<String>(
        Arrays.asList(   
            "resource", 
            "RESOURCE",
            "comment", 
            "COMMENT"
        )
    );

    // JDO case insensitive flag
    protected static final String JDO_CASE_INSENSITIVE_FLAG = "(?i)";

    /**
     * reference lookup statement hint
     */
    protected String referenceLookupStatementHint;

    /**
     * maximum rows allowed in generic search. There are as many rows as 
     * maximum number of values of attributes in filters and the number of 
     * attributes in specifiers
     */
    protected String namespaceId;

    /**
     * List of DbConnectionManagers. getConnection() returns as default
     * connectionManagers at position 0. A user-defined implementation of
     * getConnection() may return any of the configured connection managers.
     */
    protected SparseArray<DataSource> dataSource = null;
    protected SparseArray<String> datasourceName = null;

    /**
     * Configuration of plugin.
     */
    protected DatabaseConfiguration databaseConfiguration;

    /**
     * Result set type used for prepareStatement. It must be a valid value
     * defined by ResultSet.TYPE_... Default is TYPE_FORWARD_ONLY.
     */
    protected int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

    // driver features
    protected boolean allowsSqlSequenceFallback;

    /**
     * Allows to ignore the check-for-duplicate test on object creation. 
     * By default, the persistence plugin asserts that no two objects with 
     * the same identity are created.
     */
    protected boolean ignoreCheckForDuplicates;

    /**
     * configured single-valued filter properties. Allows
     * optimization of find operations
     */
    protected Set<String> singleValueAttributes = new HashSet<String>(
        Arrays.asList(
            SystemAttributes.OBJECT_CLASS,
            "object_stateId"
        )
    );

    /**
     * Configured embedded features. The elements are stored as
     * {key=embedded feature name, value=upper bound}
     */
    protected Map<String,Integer> embeddedFeatures = new TreeMap<String,Integer>();

    /**
     * macros
     */
    // key=column name, value=List of [macro name, macro value]
    protected Map<String,List<String[]>> stringMacros;
    protected SparseArray<String> stringMacroColumn = new TreeSparseArray<String>();
    protected SparseArray<String> stringMacroName = new TreeSparseArray<String>();
    protected SparseArray<String> stringMacroValue = new TreeSparseArray<String>();
    
    // key=macro name, value=macro value
    protected Map<String,String> pathMacros;
    protected SparseArray<String> pathMacroName = new TreeSparseArray<String>();
    protected SparseArray<String> pathMacroValue = new TreeSparseArray<String>();

    // referenceId format
    private String referenceIdFormat = LayerConfigurationEntries.REFERENCE_ID_FORMAT_REF_TABLE;

    // useNormalizedReferences. If true, all DB operations are performed on the 
    // normalized values of object references, i.e. rid, oid.
    private boolean useNormalizedReferences = false;

    // maximum reference components in REF table
    protected int maxReferenceComponents = 16;

    /**
     * If set to true the size of multi-valued attributes stored in column C
     * is stored in column C_.
     */
    private boolean setSizeColumns = false;

    /**
     * <columnName,attributeName> mapping. Calculating attribute names
     * from column names and vice versa is expensive. Therefore it
     * is cached.
     */
    protected final ConcurrentMap<String,String> featureNames = new ConcurrentHashMap<String,String>();
    protected final ConcurrentMap<String,String> publicColumnNames = new ConcurrentHashMap<String,String>();
    protected final ConcurrentMap<String,String> privateColumnNames = new ConcurrentHashMap<String,String>();
    protected final ConcurrentMap<String,String> databaseProductNames = new ConcurrentHashMap<String,String>();
    private final Set<String> nonPersistentFeatures = new HashSet<String>();

    /**
     * nullAsCharacter defines the string used to produce a type-safe
     * NULL string, e.g. CAST(NULL AS CHARACTER)
     */
    protected String nullAsCharacter = "NULL";

    /**
     * Embedding the configuration options {@link LayerConfigurationEntries#FETCH_SIZE},
     * {@link LayerConfigurationEntries#FETCH_SIZE_OPTIMAL} and 
     * {@link LayerConfigurationEntries#FETCH_SIZE_GREEDY}.
     */
    protected FetchSize fetchSizeT;
    protected int fetchSizeDefault = FetchPlan.FETCH_SIZE_OPTIMAL;
    protected int fetchSizeOptimal = 32;
    protected int fetchSizeGreedy = 1024;

    /**
     * Tells whether state filter substitution is enabled or disabled.
     */
    protected boolean enableAspectFilterSubstitution = true;

    /**
     * technical column names: object_oid, object_rid, object_idx
     * (may be used by DbObject implementations if required)
     */
    public String OBJECT_OID = this.toOid("object");
    public String OBJECT_IDX = this.toIdx("object");
    public String OBJECT_RID = this.toRid("object");
    
    protected Properties jdbcDriverSqlProperties = null;
    protected static final String JDBC_DRIVER_SQL_PROPERTIES = "org/openmdx/kernel/application/deploy/jdbc-driver-sql.properties";  

    protected final Collection<File> temporaryFiles = new ArrayList<File>();

    protected final SQLWildcards sqlWildcards = new SQLWildcards('\\');
    
    protected final EmbeddedFlags embeddedFlags = EmbeddedFlags.getInstance();

    /**
     * It's usually unwise the use these classes in  select statement
     */
    private static final Collection<String> BASE_CLASSES = Arrays.asList(
        "org:openmdx:base:BasicObject",
        "org:openmdx:base:ExtentCapable"
    );

    static enum JoinType {
        NONE,
        SPECIFIED_COLUMN_WITH_OBJECT_ID,
        OBJECT_RID_AND_OID
    }

    static enum OrderAmendment {
        INTRINSIC,
        BY_OBJECT_ID
    }

    /**
     * @deprecated("For JRE 5/setStreamByValue support only")
     */
    @Deprecated
    protected File streamBufferDirectory;

    /**
     * The large object marshaller instance
     */
    private LargeObjectMarshaller largeObjectMarshaller;
        
}

//---End of File -------------------------------------------------------------
