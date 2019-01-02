/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Database 2 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2017, OMEX AG, Switzerland
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

import static javax.jdo.FetchPlan.FETCH_SIZE_GREEDY;
import static org.openmdx.base.accessor.cci.SystemAttributes.OBJECT_CLASS;
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

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.application.dataprovider.kernel.LateBindingDataSource;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.spi.URIMarshaller;
import org.openmdx.base.aop1.Aspects;
import org.openmdx.base.collection.Sets;
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
import org.openmdx.base.dataprovider.layer.persistence.jdbc.macros.ClassicMacroConfiguration;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.macros.MacroConfiguration;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.macros.MacroHandler;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.DataTypes;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_2_0;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.FastResultSet;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.LikeFlavour;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Target;
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
import org.openmdx.base.query.spi.EmbeddedFlags;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RequestRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestPort;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Numbers;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.text.conversion.SQLWildcards;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.url.protocol.XRI_1Protocols;
import org.openmdx.state2.spi.TechnicalAttributes;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.cci2.RegularExpressionFlag;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;

/**
 * Database_2 implements a OO-to-Relational mapping and makes MappedRecords
 * persistent. Any JDBC-compliant data store can be used.
 * 
 * You can use the java.io.PrintStream.DriverManager.setLogStream() method to
 * log JDBC calls. This method sets the logging/tracing PrintStream used by the
 * DriverManager and all drivers. Insert the following line at the location in
 * your code where you want to start logging JDBC calls:
 * DriverManager.setLogStream(System.out);
 */
@SuppressWarnings({
    "rawtypes",
    "unchecked"
})
public class Database_2
    extends AbstractRestPort
    implements Database_2_0, DataTypes {

    /**
     * Constructor
     */
    public Database_2() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.application.dataprovider.spi.OperationAwareLayer_1#
     * getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(RestConnection connection)
        throws ResourceException {
        return new RestInteraction(this, connection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.rest.spi.AbstractRestPort#newDelegateInteraction(org.
     * openmdx.base.rest.cci.RestConnection)
     */
    @Override
    protected Interaction newDelegateInteraction(RestConnection connection)
        throws ResourceException {
        return super.newDelegateInteraction(connection);
    }

    /**
     * @return the getSIZE_SUFFIX
     */
    @Override
    public String getSizeSuffix() {
        return SIZE_SUFFIX;
    }

    /**
     * @return the OBJECT_OID
     */
    @Override
    public String getObjectOidColumnName() {
        return this.OBJECT_OID;
    }

    /**
     * @return the OBJECT_IDX
     */
    @Override
    public String getObjectIdxColumnName() {
        return this.OBJECT_IDX;
    }

    /**
     * @return the OBJECT_RID
     */
    @Override
    public String getObjectRidColumnName() {
        return this.OBJECT_RID;
    }

    // -----------------------------------------------------------------------
    // Configuration
    // -----------------------------------------------------------------------

    /**
     * Retrieve booleanType.
     *
     * @return Returns the booleanType.
     */
    public String getBooleanType() {
        return this.booleanType;
    }

    /**
     * Set booleanType.
     * 
     * @param booleanType
     *            The booleanType to set.
     */
    public void setBooleanType(String booleanType) {
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
     * @param booleanFalse
     *            The booleanFalse to set.
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
     * @param booleanTrue
     *            The booleanTrue to set.
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
    protected BooleanMarshaller getBooleanMarshaller()
        throws ServiceException {
        if (this.booleanMarshaller == null) {
            this.booleanMarshaller = BooleanMarshaller
                .newInstance(this.booleanFalse, this.booleanTrue, this);
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
     * @param durationType
     *            The durationType to set.
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
    protected Marshaller getDurationMarshaller()
        throws ServiceException {
        if (this.durationMarshaller == null) {
            this.durationMarshaller = DurationMarshaller.newInstance(this.durationType);
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
     * @param dateTimeType
     *            The dateTimeType to set.
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
     * @param dateType
     *            The dateType to set.
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
     * @param timeType
     *            The timeType to set.
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
     * @param dateTimeZone
     *            The dateTimeZone to set.
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
     * @param dateTimeDaylightZone
     *            The dateTimeDaylightZone to set.
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
     * @param dateTimePrecision
     *            The dateTimePrecision to set.
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
    protected XMLGregorianCalendarMarshaller getCalendarMarshaller()
        throws ServiceException {
        if (this.calendarMarshaller == null) {
            this.calendarMarshaller = XMLGregorianCalendarMarshaller.newInstance(
                this.timeType,
                this.dateType,
                this.dateTimeType,
                this.dateTimeZone,
                this.dateTimeDaylightZone == null ? dateTimeZone
                    : this.dateTimeDaylightZone,
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
     * @param objectIdAttributesSuffix
     *            The objectIdAttributesSuffix to set.
     */
    public void setObjectIdAttributesSuffix(String objectIdAttributesSuffix) {
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
     * @param referenceIdAttributesSuffix
     *            The referenceIdAttributesSuffix to set.
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
     * @param referenceIdSuffixAttributesSuffix
     *            The referenceIdSuffixAttributesSuffix to set.
     */
    public void setReferenceIdSuffixAttributesSuffix(
        String referenceIdSuffixAttributesSuffix
    ) {
        this.referenceIdSuffixAttributesSuffix = referenceIdSuffixAttributesSuffix;
    }

    public String getPrivateAttributesPrefix() {
        return this.privateAttributesPrefix;
    }

    /**
     * Set privateAttributesPrefix.
     * 
     * @param privateAttributesPrefix
     *            The privateAttributesPrefix to set.
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
     * @param objectIdxColumn
     *            The objectIdxColumn to set.
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
        if (this.resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
            return LayerConfigurationEntries.RESULT_SET_TYPE_FORWARD_ONLY;
        } else if (this.resultSetType == ResultSet.TYPE_SCROLL_INSENSITIVE) {
            return LayerConfigurationEntries.RESULT_SET_TYPE_SCROLL_INSENSITIVE;
        } else if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
            return LayerConfigurationEntries.RESULT_SET_TYPE_SCROLL_SENSITIVE;
        } else {
            return null;
        }
    }

    /**
     * Set resultSetType.
     * 
     * @param resultSetType
     *            The resultSetType to set.
     */
    public void setResultSetType(String resultSetType) {
        if (LayerConfigurationEntries.RESULT_SET_TYPE_FORWARD_ONLY
            .equals(resultSetType)) {
            this.resultSetType = ResultSet.TYPE_FORWARD_ONLY;
        } else if (LayerConfigurationEntries.RESULT_SET_TYPE_SCROLL_INSENSITIVE
            .equals(resultSetType)) {
            this.resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        } else if (LayerConfigurationEntries.RESULT_SET_TYPE_SCROLL_SENSITIVE
            .equals(resultSetType)) {
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
     * @param allowsSqlSequenceFallback
     *            The allowsSqlSequenceFallback to set.
     */
    public void setAllowsSqlSequenceFallback(
        boolean allowsSqlSequenceFallback
    ) {
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
     * @param ignoreCheckForDuplicates
     *            The ignoreCheckForDuplicates to set.
     */
    public void setIgnoreCheckForDuplicates(boolean ignoreCheckForDuplicates) {
        this.ignoreCheckForDuplicates = ignoreCheckForDuplicates;
    }

    /**
     * Retrieve singleValueAttributes.
     *
     * @return Returns the singleValueAttributes.
     */
    public SparseArray<String> getSingleValueAttribute() {
        return new TreeSparseArray<>(this.singleValueAttributes);
    }

    /**
     * Set singleValueAttributes.
     * 
     * @param singleValueAttributes
     *            The singleValueAttributes to set.
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
    public SparseArray<String> getEmbeddedFeature() {
        final SparseArray<String> embeddedFeatures = new TreeSparseArray<>();
        int index = 0;
        for (Map.Entry<String, Integer> embeddedFeature : this.embeddedFeatures
            .entrySet()) {
            embeddedFeatures.put(
                Integer.valueOf(index),
                embeddedFeature.getKey() + "[" + embeddedFeature.getValue()
                    + "]"
            );
            index++;
        }
        return embeddedFeatures;
    }

    /**
     * Set embeddedFeature.
     * 
     * @param embeddedFeature
     *            The embeddedFeature to set.
     */
    public void setEmbeddedFeature(SparseArray<String> embeddedFeature) {
        Map<String, Integer> embeddedFeatures = new TreeMap<String, Integer>();
        for (String entry : embeddedFeature) {
            int p = entry.indexOf("[");
            if (p > 0) {
                final String featureName = entry.substring(0, p);
                final String upperBound = entry.substring(p + 1, entry.indexOf("]"));
                embeddedFeatures.put(
                    featureName,
                    Integer.valueOf(upperBound)
                );
            }
        }
        this.embeddedFeatures = embeddedFeatures;
    }

    /**
     * Retrieve nonPersistentFeature.
     *
     * @return Returns the nonPersistentFeature.
     */
    public SparseArray<String> getNonPersistentFeature() {
        return new TreeSparseArray<>(this.nonPersistentFeatures);
    }

    /**
     * Set nonPersistentFeature.
     * 
     * @param nonPersistentFeature
     *            The nonPersistentFeature to set.
     */
    public void setNonPersistentFeature(
        SparseArray<String> nonPersistentFeature
    ) {
        this.nonPersistentFeatures = new HashSet<>(nonPersistentFeature.values());
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
     * @param nullAsCharacter
     *            The nullAsCharacter to set.
     */
    public void setNullAsCharacter(String nullAsCharacter) {
        this.nullAsCharacter = nullAsCharacter;
    }

    /**
     * Retrieve resultSetLimit.
     *
     * @return Returns the resultSetLimit.
     */
    @Override
    public int getResultSetLimit() {
        return this.resultSetLimit;
    }

    /**
     * Set resultSetLimit.
     * 
     * @param resultSetLimit
     *            The resultSetLimit to set.
     */
    public void setResultSetLimit(
        int resultSetLimit
    ) {
        this.resultSetLimit = resultSetLimit;
    }

    /**
     * Retrieve optimalFetchSize.
     *
     * @return Returns the optimalFetchSize.
     */
    @Override
    public int getOptimalFetchSize() {
        return this.optimalFetchSize;
    }

    /**
     * Set optimalFetchSize.
     * 
     * @param optimalFetchSize
     *            The optimalFetchSize to set.
     */
    public void setOptimalFetchSize(
        int optimalFetchSize
    ) {
        this.optimalFetchSize = optimalFetchSize;
    }

    /**
     * Retrieve rowFetchSize.
     *
     * @return Returns the rowFetchSize.
     */
    @Override
    public int getRowBatchSize() {
        return this.rowBatchSize;
    }

    /**
     * Set rowBatchSize.
     * 
     * @param rowBatchSize
     *            The rowBatchSize to set.
     */
    public void setRowBatchSize(
        int rowBatchSize
    ) {
        this.rowBatchSize = rowBatchSize;
    }

    /**
     * Retrieve objectBatchSize.
     *
     * @return Returns the objectBatchSize.
     */
    @Override
    public int getObjectBatchSize() {
        return this.objectBatchSize;
    }

    /**
     * Set objectBatchSize.
     * 
     * @param objectBatchSize
     *            The objectBatchSize to set.
     */
    public void setObjectBatchSize(
        int objectBatchSize
    ) {
        this.objectBatchSize = objectBatchSize;
    }

    /**
     * @deprecated in favour of {@link #getOptimalFetchSize()}
     */
    @Deprecated
    public int getFetchSizeOptimal() {
        SysLog.warning(
            "The configuration key '" +
                org.openmdx.application.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries.FETCH_SIZE +
                "' is deprecated and has been replaced by '" +
                org.openmdx.base.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries.OPTIMAL_FETCH_SIZE +
                "'. Please adapt you configuration accordingly!"
        );
        return this.getOptimalFetchSize();
    }

    /**
     * @deprecated in favour of {@link #setOptimalFetchSize(int)}
     */
    @Deprecated
    public void setFetchSizeOptimal(int fetchSizeOptimal) {
        SysLog.warning(
            "The configuration key '" +
                org.openmdx.application.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries.FETCH_SIZE +
                "' is deprecated and has been replaced by '" +
                org.openmdx.base.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries.OPTIMAL_FETCH_SIZE +
                "'. Please adapt you configuration accordingly!"
        );
        setOptimalFetchSize(fetchSizeOptimal);
    }

    /**
     * @deprecated without replacement
     */
    @Deprecated
    public int getFetchSize() {
        SysLog.warning(
            "The configuration key '" +
                org.openmdx.application.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries.FETCH_SIZE +
                "' is deprecated and unused. Please remove it from you configuration!"
        );
        return Integer.MIN_VALUE;
    }

    /**
     * @deprecated without replacement
     */
    @Deprecated
    public void setFetchSize(int fetchSize) {
        SysLog.warning(
            "The configuration key '" +
                org.openmdx.application.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries.FETCH_SIZE +
                "' is deprecated and unused. Please remove it from you configuration!"
        );
    }

    /**
     * @deprecated without replacement
     */
    @Deprecated
    public int getFetchSizeGreedy() {
        SysLog.warning(
            "The configuration key '" +
                org.openmdx.application.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries.FETCH_SIZE_GREEDY +
                "' is deprecated and unused. Please remove it from you configuration!"
        );
        return Integer.MIN_VALUE;
    }

    /**
     * @deprecated without replacement
     */
    @Deprecated
    public void setFetchSizeGreedy(int fetchSizeGreedy) {
        SysLog.warning(
            "The configuration key '" +
                org.openmdx.application.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries.FETCH_SIZE_GREEDY +
                "' is deprecated and unused. Please remove it from you configuration!"
        );
    }

    /**
     * Get referenceIdFormat.
     * 
     * @return
     */
    @Override
    public String getReferenceIdFormat() {
        return this.referenceIdFormat;
    }

    /**
     * Set referenceIdFormat.
     * 
     * @param referenceIdFormat
     *            The referenceIdFormat to set.
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
     * @param useNormalizedReferences
     *            The useNormalizedReferences to set.
     */
    public void setUseNormalizedReferences(boolean useNormalizedReferences) {
        this.useNormalizedReferences = useNormalizedReferences;
    }

    @Override
    public boolean isSetSizeColumns() {
        return this.setSizeColumns;
    }

    /**
     * Set setSizeColumns.
     * 
     * @param setSizeColumns
     *            The setSizeColumns to set.
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
     * @param pathMacroName
     *            The pathMacroName to set.
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
     * @param pathMacroValue
     *            The pathMacroValue to set.
     */
    public void setPathMacroValue(SparseArray<String> pathMacroValue) {
        this.pathMacroValue = pathMacroValue;
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
     * @param stringMacroColumn
     *            The stringMacroColumn to set.
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
     * @param stringMacroName
     *            The stringMacroName to set.
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
     * @param stringMacroValue
     *            The stringMacroValue to set.
     */
    public void setStringMacroValue(SparseArray<String> stringMacroValue) {
        this.stringMacroValue = stringMacroValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_2_0#getMacroConfiguration()
     */
    @Override
    public MacroConfiguration getMacroConfiguration() {
        return this.macroConfiguration;
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
     * @param dataSource
     *            The dataSource to set.
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
     * @param datasourceName
     *            The datasourceName to set.
     */
    public void setDatasourceName(SparseArray<String> datasourceName) {
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
    )
        throws ServiceException {
        if (this.dataSource == null) {
            final SparseArray<DataSource> dataSource = new TreeSparseArray<>();
            if (this.datasourceName != null) {
                for(ListIterator<String> i = datasourceName.populationIterator(); i.hasNext(); ) {
                    final Integer index = Integer.valueOf(i.nextIndex());
                    final String jndiName = i.next();
                    dataSource.put(index, new LateBindingDataSource(jndiName));
                }
            }
            this.dataSource = dataSource;
        }
        if (this.dataSource.isEmpty()) {
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
    public Properties getJdbcDriverSqlProperties()
        throws ServiceException {
        if (this.jdbcDriverSqlProperties == null) {
            final InputStream jdbcDriverConfiguration = Resources.getResourceAsStream(JDBC_DRIVER_SQL_PROPERTIES);
            if (jdbcDriverConfiguration == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "Unable to find the JDBC driver properties",
                    new BasicException.Parameter(
                        "resource",
                        JDBC_DRIVER_SQL_PROPERTIES
                    )
                );
            }
            try {
                final Properties jdbcDriverSqlProperties = new Properties();
                jdbcDriverSqlProperties.load(jdbcDriverConfiguration);
                this.jdbcDriverSqlProperties = jdbcDriverSqlProperties;
            } catch (IOException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "Unable to load the JDBC driver properties",
                    new BasicException.Parameter(
                        "resource",
                        JDBC_DRIVER_SQL_PROPERTIES
                    )
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
    public boolean isDisableStateFilterSubstitution() {
        return !this.enableAspectFilterSubstitution;
    }

    /**
     * Set disableStateFilterSubstitution.
     * 
     * @param disableStateFilterSubstitution
     *            The disableStateFilterSubstitution to set.
     */
    public void setDisableStateFilterSubstitution(
        boolean disableStateFilterSubstitution
    ) {
        this.enableAspectFilterSubstitution = !disableStateFilterSubstitution;
    }

    /**
     * @param namespaceId
     *            the namespaceId to set
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
     * @param normalizeObjectIds
     *            The normalizeObjectIds to set.
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
     * @param useViewsForRedundantColumns
     *            The useViewsForRedundantColumns to set.
     */
    public void setUseViewsForRedundantColumns(
        boolean useViewsForRedundantColumns
    ) {
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
     * @param usePreferencesTable
     *            The usePreferencesTable to set.
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
     * @param cascadeDeletes
     *            The cascadeDeletes to set.
     */
    public void setCascadeDeletes(boolean cascadeDeletes) {
        this.cascadeDeletes = cascadeDeletes;
    }

    /**
     * Get orderNullsAsEmpty.
     * 
     * @return
     */
    @Override
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

    @Override
    public DatabaseConfiguration getDatabaseConfiguration()
        throws ServiceException {
        if (this.databaseConfiguration == null) {
            this.databaseConfiguration = new DatabaseConfiguration(this);
        }
        return this.databaseConfiguration;
    }

    /**
     * Retrieve streamBufferDirectory.
     *
     * @return Returns the streamBufferDirectory.
     * 
     *         @deprecated("For JRE 5/setStreamByValue support only")
     */
    protected String getStreamBufferDirectory() {
        return this.streamBufferDirectory == null ? null
            : this.streamBufferDirectory.toString();
    }

    /**
     * Set streamBufferDirectory.
     * 
     * @param streamBufferDirectory
     *            The streamBufferDirectory to set.
     */
    public void setStreamBufferDirectory(String streamBufferDirectory) {
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
     * @param columnNameFrom
     *            The columnNameFrom to set.
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
     * @param columnNameTo
     *            The columnNameTo to set.
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
     * @param type
     *            The type to set.
     */
    public void setType(SparseArray<Path> type) {
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
     * @param typeName
     *            The typeName to set.
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
     * @param dbObject
     *            The dbObject to set.
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
     * @param dbObject2
     *            The dbObject2 to set.
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
     * @param dbObjectFormat
     *            The dbObjectFormat to set.
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
     * @param pathNormalizeLevel
     *            The pathNormalizeLevel to set.
     */
    public void setPathNormalizeLevel(SparseArray<Integer> pathNormalizeLevel) {
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
     * @param dbObjectForQuery
     *            The dbObjectForQuery to set.
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
     * @param dbObjectForQuery2
     *            The dbObjectForQuery2 to set.
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
     * @param dbObjectsForQueryJoinColumn
     *            The dbObjectsForQueryJoinColumn to set.
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
     * @param dbObjectHint
     *            The dbObjectHint to set.
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
     * @param objectIdPattern
     *            The objectIdPattern to set.
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
     * @param joinTable
     *            The joinTable to set.
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
     * @param joinColumnEnd1
     *            The joinColumnEnd1 to set.
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
     * @param joinColumnEnd2
     *            The joinColumnEnd2 to set.
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
     * @param unitOfWorkProvider
     *            The unitOfWorkProvider to set.
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
     * @param removableReferenceIdPrefix
     *            The removableReferenceIdPrefix to set.
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
     * @param disableAbsolutePositioning
     *            The disableAbsolutePositioning to set.
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
     * @param referenceIdPattern
     *            The referenceIdPattern to set.
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
     * @param autonumColumn
     *            The autonumColumn to set.
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
     * @param getLargeObjectByValue
     *            The getLargeObjectByValue to set.
     */
    public void setGetLargeObjectByValue(boolean getLargeObjectByValue) {
        this.getLargeObjectByValue = getLargeObjectByValue;
    }

    protected LargeObjectMarshaller getLargeObjectMarshaller() {
        if (this.largeObjectMarshaller == null) {
            this.largeObjectMarshaller = new LargeObjectMarshaller(this.getLargeObjectByValue);
        }
        return this.largeObjectMarshaller;
    }

    // -----------------------------------------------------------------------

    /**
     * Get rid for given attribute name.
     * 
     * @param name
     * @return
     */
    @Override
    public String toRid(String name) {
        return name + this.referenceIdAttributesSuffix;
    }

    @Override
    public String toRsx(String name) {
        return name + this.referenceIdSuffixAttributesSuffix;
    }

    @Override
    public String toOid(String name) {
        return name + this.objectIdAttributesSuffix;
    }

    public String toId(String name) {
        return name + "_id";
    }

    public String toIdx(String name) {
        return name + "_idx";
    }

    /**
     * Get upper bound for embedded feature.
     * 
     * @param attributeName
     * @return upper bound for embedded feature. null if feature is not
     *         embedded.
     */
    public Integer getEmbeddedFeature(String attributeName) {
        return this.embeddedFeatures.get(attributeName);
    }

    /**
     * column name could be qualified, e.g. vm.<column name>
     * 
     * @param columnName
     *            a simple or qualified column name
     * 
     * @return the simple column name
     */
    private String toSimpleColumnName(String columnName) {
        int pos = columnName.indexOf(".");
        return pos < 0 ? columnName : columnName.substring(pos + 1);
    }

    @Override
    public Object externalizeStringValue(
        String columnName,
        Object value
    ) {
        if (value instanceof String || value instanceof URI) {
            final MacroHandler macroHandler = getMacroConfiguration().getMacroHandler();
            return macroHandler.externalizeString(toSimpleColumnName(columnName), value.toString());
        } else {
            return value;
        }
    }

    @Override
    public String internalizeStringValue(
        String columnName,
        String value
    ) {
        if(value != null) {
            final MacroHandler macroHandler = getMacroConfiguration().getMacroHandler();
            return macroHandler.internalizeString(toSimpleColumnName(columnName), value);
        } else {
            return null;
        }
    }

    /**
     * Externalizes a Path value. The externalized value is stored on the
     * database. Also applies the path macros and returns the shortened path.
     * For compatibility reasons returns URI notation if no macro replacement
     * was applied, else XRI notation.
     */
    public String externalizePathValue(
        Connection conn,
        Path source
    )
        throws ServiceException {
        if (this.isUseNormalizedReferences()) {
            final DatabaseConfiguration dbConfiguration = this.getDatabaseConfiguration();
            return dbConfiguration.normalizeObjectIds()
                ? dbConfiguration.buildObjectId(source)
                : this.getReferenceId(conn, source, false) + "/"
                    + source.getLastSegment().toClassicRepresentation();
        } else {
            final MacroHandler macroHandler = getMacroConfiguration().getMacroHandler();
            return macroHandler.externalizePath(source);
        }
    }

    /**
     * Internalizes an external, stringified path value. Also applies the path
     * macros to expand the path.
     * 
     * @throws ServiceException
     */
    public Object internalizePathValue(String source)
        throws ServiceException {
        final DatabaseConfiguration dbConfiguration = this.getDatabaseConfiguration();
        if (dbConfiguration.normalizeObjectIds()) {
            return dbConfiguration.buildResourceIdentifier(
                source,
                false
            );
        } else {
            final MacroHandler macroHandler = getMacroConfiguration().getMacroHandler();
            return macroHandler.internalizePath(source);
        }
    }

    /**
     * Execute a query
     * 
     * @param ps
     * @param statement
     * @param statementParameters
     * @param rowBatchSize
     *            the requested fetch is, use FetchPlan.FETCH_SIZE_OPTIMAL as
     *            default
     * @return the query result
     * 
     * @throws SQLException
     */
    @Override
    public ResultSet executeQuery(
        PreparedStatement ps,
        String statement,
        List<?> statementParameters,
        int maxRows
    )
        throws SQLException {
        int fetchSize = this.getRowBatchSize();
        ps.setFetchSize(fetchSize);
        ps.setMaxRows(maxRows);
        ps.setFetchDirection(ResultSet.FETCH_FORWARD);
        SysLog.detail("statement", statement);
        SysLog.detail("parameters", statementParameters);
        SysLog.detail("fetchSize", fetchSize);
        SysLog.detail("maxRows", maxRows);
        long startTime = System.currentTimeMillis();
        ResultSet rs = ps.executeQuery();
        long duration = System.currentTimeMillis() - startTime;
        SysLog.detail("execution time", Long.valueOf(duration));
        return rs;
    }

    @Override
    public int executeUpdate(
        PreparedStatement ps,
        String statement,
        List<?> statementParameters
    )
        throws SQLException {
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
    )
        throws ServiceException {
        DbObjectConfiguration entry = dbObject.getConfiguration();
        String dbObjectForQuery = entry.getDbObjectForQuery1();
        if (dbObjectHint != null) {
            if (dbObjectHint.startsWith(XRI_1Protocols.SCHEME_PREFIX)) {
                Path path = new Path(dbObjectHint);
                DbObject referencedDbObject = this.createDbObject(conn, path, true);
                dbObjectForQuery += referencedDbObject.getTableName();
            } else {
                dbObjectForQuery += dbObjectHint;
            }
        }
        if (!dbObject.getReferencedType().isPattern()) {
            // Assert existence of reference id
            this.getReferenceId(conn, dbObject.getReference(), true);
        }
        return dbObjectForQuery;
    }

    /**
     * Retrieve the catalog id
     * 
     * @param the
     *            actual catalog, or <code>null</code> if none is defined
     * 
     * @return the catalog id, i.e. either the catalog itself or "" if the
     *         catalog is <code>null</code>
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
    protected String getDatabaseProductName(Connection connection) {
        try {
            final String catalogId = toCatalogId(connection.getCatalog());
            String databaseProductName = this.databaseProductNames.get(catalogId);
            if (databaseProductName == null) {
                databaseProductName = connection.getMetaData().getDatabaseProductName();
                this.databaseProductNames.put(catalogId, databaseProductName);
            }
            return databaseProductName;
        } catch (SQLException exception) {
            SysLog.detail(
                "Database product name determination failure",
                exception
            );
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
    )
        throws ServiceException {
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
    )
        throws ServiceException {
        final String driverPropertyName = this.getDatabaseProductName(connection) + '.' + suffix;
        final String systemPropertyName = "org.openmdx.persistence.jdbc." + driverPropertyName;
        final String value = System.getProperty(systemPropertyName);
        return value == null ? this.getJdbcDriverSqlProperties().getProperty(
            driverPropertyName,
            defaultValue
        ) : value;
    }

    /**
     * Computes the SQL select statement returning one object-index-slice per
     * row for the given path. The statement is appended to 'statement'.
     * VIEW_MODE_OBJECT_ITERATION must return at least slice 0.
     * VIEW_MODE_OBJECT_RETRIEVAL must return all object slices.
     */
    @Override
    public String getView(
        Connection conn,
        DbObject dbObject,
        String dbObjectHint,
        short viewMode,
        String requestedColumnSelector,
        Set<String> requestedMixins
    )
        throws ServiceException {
        Set<String> mixins = requestedMixins;
        String columnSelector = requestedColumnSelector == null
            ? DEFAULT_COLUMN_SELECTOR
            : requestedColumnSelector;
        String dbObjectForQuery1 = null;
        String dbObjectForQuery2 = null;
        DbObjectConfiguration typeConfiguration = dbObject.getConfiguration();

        String databaseProductName = getDatabaseProductName(conn);
        if (typeConfiguration != null) {
            if (typeConfiguration.getDbObjectForQuery1() != null) {
                dbObjectForQuery1 = this.getDbObjectForQuery1(conn, dbObject, dbObjectHint);
            } else {
                dbObjectForQuery1 = typeConfiguration.getDbObjectForUpdate1();
            }
            dbObjectForQuery2 = typeConfiguration.getDbObjectForQuery2() == null
                ? typeConfiguration.getDbObjectForUpdate2()
                : typeConfiguration.getDbObjectForQuery2();
            String[] dbObjectsForQuery = new String[] {
                dbObjectForQuery1,
                dbObjectForQuery2
            };
            for (int i = 0; i < dbObjectsForQuery.length; i++) {
                String dbObjectForQuery = dbObjectsForQuery[i];
                if (dbObjectForQuery != null) {
                    // replace driver specific operations / functions
                    try {
                        if (dbObjectForQuery.indexOf("STRCAT.PREFIX") >= 0) {
                            dbObjectForQuery = Pattern
                                .compile("STRCAT.PREFIX")
                                .matcher(dbObjectForQuery)
                                .replaceAll(
                                    this.getDriverProperty(
                                        conn,
                                        "STRCAT.PREFIX"
                                    )
                                );
                        }
                        if (dbObjectForQuery.indexOf("STRCAT.INFIX") >= 0) {
                            dbObjectForQuery = Pattern
                                .compile("STRCAT.INFIX")
                                .matcher(dbObjectForQuery)
                                .replaceAll(
                                    this.getDriverProperty(
                                        conn,
                                        "STRCAT.INFIX"
                                    )
                                );
                        }
                        if (dbObjectForQuery.indexOf("STRCAT.SUFFIX") >= 0) {
                            dbObjectForQuery = Pattern
                                .compile("STRCAT.SUFFIX")
                                .matcher(dbObjectForQuery)
                                .replaceAll(
                                    this.getDriverProperty(
                                        conn,
                                        "STRCAT.SUFFIX"
                                    )
                                );
                        }
                        if (dbObjectForQuery.indexOf("NULL.NUMERIC") >= 0) {
                            dbObjectForQuery = Pattern
                                .compile("NULL.NUMERIC")
                                .matcher(dbObjectForQuery)
                                .replaceAll(
                                    this.getDriverProperty(
                                        conn,
                                        "NULL.NUMERIC"
                                    )
                                );
                        }
                        if (dbObjectForQuery.indexOf("NULL.CHARACTER") >= 0) {
                            dbObjectForQuery = Pattern
                                .compile("NULL.CHARACTER")
                                .matcher(dbObjectForQuery)
                                .replaceAll(
                                    this.getDriverProperty(
                                        conn,
                                        "NULL.CHARACTER"
                                    )
                                );
                        }
                        if (dbObjectForQuery
                            .indexOf("CORREL.SUBQUERY.BEGIN") >= 0) {
                            dbObjectForQuery = Pattern
                                .compile("CORREL.SUBQUERY.BEGIN")
                                .matcher(dbObjectForQuery)
                                .replaceAll(
                                    this.getDriverProperty(
                                        conn,
                                        "CORREL.SUBQUERY.BEGIN"
                                    )
                                );
                        }
                        if (dbObjectForQuery
                            .indexOf("CORREL.SUBQUERY.END") >= 0) {
                            dbObjectForQuery = Pattern
                                .compile("CORREL.SUBQUERY.END")
                                .matcher(dbObjectForQuery)
                                .replaceAll(
                                    this.getDriverProperty(
                                        conn,
                                        "CORREL.SUBQUERY.END"
                                    )
                                );
                        }
                    } catch (Exception e) {
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

        boolean isComplex = dbObjectForQuery1.startsWith("SELECT")
            || dbObjectForQuery1.startsWith("select");

        // Join primary with slice 0 of secondary and add mixin attributes
        // as private p$$ attributes
        if (viewMode == VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY) {
            /**
             * Generate a SELECT statement of the form
             * 
             * SELECT v.*, vm.<mixin attributes> FROM <primary db object for
             * query> v, <secondary db object for query> vm WHERE v.<dbObject
             * selector $i> = vm.<dbObject selector $j> v.<getObjectIdColumn()>
             * = vm.<getObjectIdColumn()>
             *
             * mixinAttributes must contain at least one attribute which is
             * guaranteed to have a value --> object_class. Otherwise no slice
             * is generated for these objects even if they exist and would match
             * the filter.
             * 
             * If db object is not indexed the v x vm join is not required The
             * mixinAttributes are taken from v in this case.
             */
            if ((mixins != null) && !mixins.isEmpty()) {
                mixins = new HashSet<String>(mixins);
                mixins.add(SystemAttributes.OBJECT_CLASS);
                mixins.removeAll(dbObject.getExcludeAttributes());
                String view = "";
                view += "SELECT " + dbObject.getHint() + " " + columnSelector;
                for (String mixin : mixins) {
                    final int upperBound = Numbers.getValue(this.getEmbeddedFeature(mixin), 1);
                    for (int j = 0; j < upperBound; j++) {
                        String columnName = this.getColumnName(
                            conn,
                            mixin,
                            j,
                            upperBound > 1,
                            false,
                            false
                        );
                        String prefixedColumnName = this.getColumnName(
                            conn,
                            mixin,
                            j,
                            upperBound > 1,
                            false,
                            true
                        );
                        view += ", "
                            + (dbObject.getIndexColumn() == null ? "v." : "vm.")
                            + columnName + " AS " + prefixedColumnName;
                    }
                }
                // JOIN is required if primary is indexed
                if (dbObject.getIndexColumn() != null) {
                    boolean useInnerJoin = !databaseProductName.startsWith("DB2");
                    if (useInnerJoin) {
                        view += isComplex
                            ? " FROM (" + dbObjectForQuery1 + ") v INNER JOIN ("
                                + (dbObjectForQuery2 == null ? dbObjectForQuery1
                                    : dbObjectForQuery2)
                                + ") vm"
                            : " FROM " + dbObjectForQuery1
                                + " v INNER JOIN " + (dbObjectForQuery2 == null
                                    ? dbObjectForQuery1
                                    : dbObjectForQuery2)
                                + " vm";
                        view += " ON ";
                    } else {
                        view += isComplex
                            ? " FROM (" + dbObjectForQuery1 + ") v, ("
                                + (dbObjectForQuery2 == null ? dbObjectForQuery1
                                    : dbObjectForQuery2)
                                + ") vm"
                            : " FROM " + dbObjectForQuery1
                                + " v, " + (dbObjectForQuery2 == null
                                    ? dbObjectForQuery1
                                    : dbObjectForQuery2)
                                + " vm";
                        view += " WHERE ";
                    }
                    int k = 0;
                    // Compare rid selectors of v, vm
                    for (String selector : dbObject.getReferenceColumn()) {
                        view += k == 0 ? "" : " AND ";
                        view += "(v." + selector + " = " + "vm." + selector + ")";
                        k++;
                    }
                    // Compare oid selectors of v, vm
                    for (Object objectIdColumn : dbObject.getObjectIdColumn()) {
                        view += k == 0 ? "" : " AND ";
                        view += "(v." + objectIdColumn + " = " + "vm."
                            + objectIdColumn + ")";
                        k++;
                    }
                    view += " WHERE (vm." + this.OBJECT_IDX + " = 0)";
                }
                // Non-indexed db object (--> secondary view is always null)
                else {
                    view += isComplex ? " FROM (" + dbObjectForQuery1 + ") v"
                        : " FROM " + dbObjectForQuery1 + " v";
                    // Join criteria supported only for primary, non-indexed
                    // views with exactly one object id column
                    if ((dbObject.getJoinCriteria() != null)
                        && (dbObject.getObjectIdColumn().size() == 1)) {
                        String[] joinCriteria = dbObject.getJoinCriteria();
                        String objectIdColumn = dbObject.getObjectIdColumn().get(0);
                        view += " INNER JOIN " + joinCriteria[0] + " vj ON v."
                            + objectIdColumn + " = vj." + joinCriteria[2];
                    }
                    view += " WHERE (1=1)";
                }
                return view;
            }
            // No mixin attributes
            else {
                if ((dbObject.getJoinCriteria() != null)
                    && (dbObject.getObjectIdColumn().size() == 1)) {
                    String[] joinCriteria = dbObject.getJoinCriteria();
                    String objectIdColumn = dbObject.getObjectIdColumn().get(0);
                    return "SELECT " + columnSelector + " FROM "
                        + dbObjectForQuery1 + " v INNER JOIN " + joinCriteria[0]
                        + " vj ON v." + objectIdColumn + " = vj."
                        + joinCriteria[2] + " WHERE (1=1)";
                } else {
                    return dbObjectForQuery1;
                }
            }
        }
        // Return columns of secondary db object
        else if (viewMode == VIEW_MODE_SECONDARY_COLUMNS) {
            return dbObjectForQuery2 == null ? dbObjectForQuery1
                : dbObjectForQuery2;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Unsupported view mode",
                new BasicException.Parameter(
                    "reference",
                    dbObject.getReference()
                ),
                new BasicException.Parameter("view mode", viewMode)
            );
        }
    }

    /**
     * Tells whether the class denotes an aspect base class to be to be checked
     * indirectly only through their core reference.
     * 
     * @param qualifiedClassName
     * 
     * @return <code>true</code> if the class shall be checked indirectly only
     */
    protected boolean isAspectBaseClass(String qualifiedClassName) {
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
    @Override
    public FilterProperty mapInstanceOfFilterProperty(
        QueryRecord request,
        Collection<String> qualifiedClassNames
    )
        throws ServiceException {
        int aspectBaseClasses = 0;
        AspectBaseClasses: for (String qualifiedClassName : qualifiedClassNames) {
            if (isAspectBaseClass(qualifiedClassName)) {
                aspectBaseClasses++;
            } else {
                break AspectBaseClasses;
            }
        }
        if (aspectBaseClasses == qualifiedClassNames.size()) {
            List<FilterProperty> filterProperties = FilterProperty.getFilterProperties(request.getQueryFilter());
            for (FilterProperty filterProperty : filterProperties) {
                if (SystemAttributes.CORE.equals(filterProperty.name()) && Quantifier.valueOf(
                    filterProperty.quantor()
                ) == Quantifier.THERE_EXISTS) {
                    // Skip 'object_instanceof' predicate because a 'core'
                    // predicate is supplied as well
                    return null;
                }
            }
            return new FilterProperty(
                Quantifier.THERE_EXISTS.code(),
                SystemAttributes.CORE,
                ConditionType.IS_NOT_IN.code()
            );
        }
        Set<String> subClasses = new HashSet<String>();
        for (String qualifiedClassName : qualifiedClassNames) {
            if (this.isBaseClass(qualifiedClassName)) {
                // Adding the filter property OBJECT_CLASS for base objects
                // typically results
                // in a long list of subclasses which is expensive to process
                // for database
                // systems. Eliminating the base object filter could result in
                // returning objects
                // which are not instance of the requested object class. However
                // this should
                // never happen because matching and non-matching objects must
                // never be mixed in
                // the same database table.
                return null;
            }
            subClasses.add(qualifiedClassName);
            for (Object path : this
                .getModel()
                .getElement(qualifiedClassName)
                .objGetList("allSubtype")) {
                subClasses.add(
                    ((Path) path).getLastSegment().toClassicRepresentation()
                );
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
     * @return <code>true</code> if the given connection supports scroll
     *         sensitive result sets
     */
    protected boolean allowScrollSensitiveResultSet(Connection connection) {
        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            return Boolean.parseBoolean(
                this.getJdbcDriverSqlProperties().getProperty(
                    databaseProductName + ".ALLOW.SCROLLSENSITIVE.RESULTSET"
                )
            );
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tells whether the given connection supports scroll insensitive result
     * sets
     * 
     * @param connection
     * 
     * @return <code>true</code> if the given connection supports scroll
     *         insensitive result sets
     */
    protected boolean allowScrollInsensitiveResultSet(Connection connection) {
        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            return Boolean.parseBoolean(
                this.getJdbcDriverSqlProperties().getProperty(
                    databaseProductName
                        + ".ALLOW.SCROLLINSENSITIVE.RESULTSET"
                )
            );
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public PreparedStatement prepareStatement(
        Connection conn,
        String statement,
        boolean updatable
    )
        throws SQLException {
        return updatable ? conn.prepareStatement(
            statement,
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_UPDATABLE
        )
            : conn.prepareStatement(
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
    private int getReadOnlyResultSetType(Connection connection) {
        switch (this.resultSetType) {
            case ResultSet.TYPE_SCROLL_SENSITIVE:
                if (allowScrollSensitiveResultSet(connection))
                    return ResultSet.TYPE_SCROLL_SENSITIVE;
                // fall through
            case ResultSet.TYPE_SCROLL_INSENSITIVE:
                if (allowScrollInsensitiveResultSet(connection))
                    return ResultSet.TYPE_SCROLL_INSENSITIVE;
                // fall through
            default:
                return ResultSet.TYPE_FORWARD_ONLY;
        }
    }

    boolean isBlobColumnValue(Object val) {
        return val instanceof byte[] || val instanceof Blob;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.
     * AbstractDatabase_1#isClobColumnValue(java.lang.Object)
     */
    boolean isClobColumnValue(Object val) {
        return val instanceof String || val instanceof Clob;
    }

    Object getBlobColumnValue(
        Object val,
        String attributeName,
        ModelElement_1_0 attributeDef
    )
        throws ServiceException,
        SQLException {
        return this.getLargeObjectMarshaller().getBinaryColumnValue(
            val,
            attributeName,
            attributeDef
        );
    }

    Object getClobColumnValue(
        Object val,
        String attributeName,
        ModelElement_1_0 attributeDef
    )
        throws ServiceException,
        SQLException {
        return this.getLargeObjectMarshaller().getCharacterColumnValue(
            val,
            attributeName,
            attributeDef
        );
    }

    void setClobColumnValue(
        PreparedStatement ps,
        int column,
        Object val
    ) throws SQLException,
        ServiceException {
        if (val instanceof String) {
            String value = (String) val;
            if (requiresStreaming(ps, value)) {
                // ORA: Cannot bind stream to a ScrollableResultSet or UpdatableResultSet
                ps.setClob(
                    column,
                    new StringReader(value),
                    value.length()
                );
            } else {
                ps.setString(column, value);
            }
        } else if (val instanceof CharacterLargeObject
            || val instanceof Reader) {
            CharacterLargeObject clob;
            if (val instanceof CharacterLargeObject) {
                clob = (CharacterLargeObject) val;
            } else if (val instanceof String) {
                clob = CharacterLargeObjects.valueOf((String) val);
            } else {
                clob = CharacterLargeObjects.valueOf((Reader) val);
            }
            SetLargeObjectMethod setLargeObjectMethod = howToSetCharacterLargeObject(ps.getConnection());
            try {
                if (clob.getLength() == null
                    && this.getLargeObjectMarshaller().isTallyingRequired(
                        setLargeObjectMethod
                    )) {
                    clob = this.tallyLargeObject(clob.getContent());
                }
                this.getLargeObjectMarshaller().setCharacterColumnValue(
                    ps,
                    column,
                    clob,
                    setLargeObjectMethod
                );
            } catch (IOException e) {
                throw new ServiceException(e);
            }
        } else if (val instanceof Clob) {
            ps.setClob(column, (Clob) val);
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "String type not supported. Supported are [String|Reader|CharacterLargeObject|Clob]",
                new BasicException.Parameter(
                    "type",
                    val == null ? null : val.getClass().getName()
                )
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
    )
        throws SQLException {
        return value.length() > 2000
            && "Oracle".equals(this.getDatabaseProductName(ps.getConnection()));
    }

    void setBlobColumnValue(
        PreparedStatement ps,
        int column,
        Object val
    ) throws SQLException,
        ServiceException {
        if (val instanceof byte[]) {
            ps.setBytes(column, (byte[]) val);
        } else if (val instanceof InputStream
            || val instanceof BinaryLargeObject) {
            BinaryLargeObject blob = val instanceof InputStream
                ? BinaryLargeObjects.valueOf((InputStream) val)
                : (BinaryLargeObject) val;
            try {
                SetLargeObjectMethod setLargeObjectMethod = howToSetBinaryLargeObject(ps.getConnection());
                if(
                    blob.getLength() == null &&
                    this.getLargeObjectMarshaller().isTallyingRequired(setLargeObjectMethod)
                ) {
                    blob = this.tallyLargeObject(blob.getContent());
                }
                this.getLargeObjectMarshaller().setBinaryColumnValue(
                    ps,
                    column,
                    blob,
                    setLargeObjectMethod
                );
            } catch (IOException e) {
                throw new ServiceException(e);
            }
        } else if (val instanceof Blob) {
            ps.setBlob(column, (Blob) val);
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "binary type not supported. Supported are [byte[]|InputStream|BinaryLargeObject|Blob]",
                new BasicException.Parameter(
                    "type",
                    val == null ? null : val.getClass().getName()
                )
            );
        }
    }

    /**
     * Sets row count to object at given position.
     * 
     * @param dbObjectConfiguration
     */
    private FastResultSet setPosition(
        ResultSet rs,
        int position,
        boolean isIndexed,
        DbObjectConfiguration dbObjectConfiguration
    )
        throws ServiceException,
        SQLException {
        long startTime = System.currentTimeMillis();
        boolean hasMore = rs.next();
        // do not touch rs with hasMore==false
        // DB2 reports 'Invalid operation: result set closed'
        if (!hasMore) {
            return null;
        }
        FastResultSet frs = new FastResultSet(this, rs);
        if (position > 0) {
            boolean positioned = false;
            // Move forward to position by ResultSet.absolute()
            if (!isIndexed
                && !dbObjectConfiguration.isAbsolutePositioningDisabled()
                && frs.isAbsolutePositioningEnabled()) {
                try {
                    SysLog.log(
                        Level.INFO,
                        "Set absolute position to {0} for type {1}",
                        Integer.valueOf(position),
                        dbObjectConfiguration.getTypeName()
                    );
                    hasMore = frs.absolute(position + 1);
                    positioned = true;
                } catch (SQLException e) {
                    SysLog.log(
                        Level.SEVERE,
                        "Absolute positioning failed for the type {0}. Falling back to positioning by iteration",
                        dbObjectConfiguration.getTypeName()
                    );
                    dbObjectConfiguration.setAbsolutePositioningDisabled(true);
                }
            }
            // Move forward to position by iterating the result set
            if (!positioned) {
                if (!isIndexed) {
                    // PK not used
                    int count = 0;
                    while (hasMore && ++count <= position) {
                        hasMore = frs.next();
                        // HSQLDB bug workaround: skipping > 128 rows without
                        // reading at least one column
                        // results in an ArrayIndexOutOfBoundsException in class
                        // org.hsqldb.navigator.RowSetNavigatorClient.getCurrent(Unknown
                        // Source)
                        if (hasMore) {
                            frs.getObject(OBJECT_OID);
                        }
                        if (count % 1000 == 0) {
                            SysLog.log(
                                Level.FINE,
                                "Current position for type {0} is {1}",
                                dbObjectConfiguration.getTypeName(),
                                Integer.valueOf(count)
                            );
                        }
                    }
                } else if (frs.getColumnNames().contains(OBJECT_OID)) {
                    // PK format: id
                    int count = 0;
                    String previousId = (String) frs.getObject(OBJECT_OID);
                    while (hasMore) {
                        String id = (String) frs.getObject(OBJECT_OID);
                        if (!id.equals(previousId)) {
                            count++;
                            previousId = id;
                        }
                        if (count >= position)
                            break;
                        hasMore = frs.next();
                        if (count % 1000 == 0) {
                            SysLog.log(
                                Level.FINE,
                                "Current position for type {0} is {1}",
                                dbObjectConfiguration.getTypeName(),
                                Integer.valueOf(count)
                            );
                        }
                    }
                } else {
                    // PK format: rid/id
                    int count = 0;
                    Object previousOid = frs.getObject(OBJECT_OID);
                    Object previousRid = frs.getObject(OBJECT_RID);
                    while (hasMore) {
                        Object oid = frs.getObject(OBJECT_OID);
                        Object rid = frs.getObject(OBJECT_RID);
                        if (!oid.equals(previousOid)
                            || (rid instanceof Comparable
                                ? ((Comparable) rid).compareTo(previousRid) != 0
                                : !rid.equals(previousRid))) {
                            count++;
                            previousOid = oid;
                            previousRid = rid;
                        }
                        if (count >= position)
                            break;
                        hasMore = frs.next();
                    }
                }
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 0) {
            SysLog.log(
                Level.FINE,
                "Position duration for type {0} is {1} ms",
                dbObjectConfiguration.getTypeName(),
                Long.valueOf(duration)
            );
        }
        if (hasMore) {
            return frs;
        } else {
            return null;
        }
    }

    void resultSetUpdateLong(
        ResultSet rs,
        String columnName,
        long value
    )
        throws SQLException {
        rs.updateLong(columnName, value);
    }

    void resultSetUpdateInt(
        ResultSet rs,
        String columnName,
        int value
    )
        throws SQLException {
        rs.updateInt(columnName, value);
    }

    void resultSetUpdateString(
        ResultSet rs,
        String columnName,
        String value
    )
        throws SQLException {
        rs.updateString(columnName, value);
    }

    void resultSetUpdateRow(ResultSet rs)
        throws SQLException {
        rs.updateRow();
    }

    @Override
    public String getSelectReferenceIdsClause(
        Connection conn,
        Path pattern,
        List<Object> statementParameters
    )
        throws ServiceException {
        Path referencePattern = pattern.isObjectPath() ? pattern.getParent() : pattern;
        Object rid = this.getReferenceId(conn, referencePattern, false);
        if (rid instanceof String) {
            String srid = (String) rid;
            if (srid.endsWith("%") && !srid.endsWith("\\%")) {
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

    @Override
    public Object getReferenceId(
        Connection conn,
        Path referencePattern,
        boolean forceCreate
    )
        throws ServiceException {
        final Path reference = referencePattern.isContainerPath() ? referencePattern : referencePattern.getParent();
        return reference.isEmpty() ? null
            : this.getDatabaseConfiguration().normalizeObjectIds() ? this.getDatabaseConfiguration().buildReferenceId(reference)
                : this.getReferenceIdFormatTypeNameWithPathComponents(reference);
    }

    public String getObjectId(String oid)
        throws ServiceException {
        return this.getDatabaseConfiguration().normalizeObjectIds()
            ? this.getDatabaseConfiguration().buildObjectId(oid)
            : oid;
    }

    public String getObjectId(
        Connection conn,
        Path resourceIdentifier
    )
        throws ServiceException {
        return this.getDatabaseConfiguration().normalizeObjectIds()
            ? this.getDatabaseConfiguration().buildObjectId(resourceIdentifier)
            : getReferenceId(conn, resourceIdentifier.getParent(), false) + "/"
                + resourceIdentifier.getLastSegment().toClassicRepresentation();
    }

    private String getReferenceIdFormatTypeNameWithPathComponents(
        Path reference
    )
        throws ServiceException {
        DbObjectConfiguration dbObjectConfiguration = this.getDatabaseConfiguration().getDbObjectConfiguration(reference);
        Path type = dbObjectConfiguration.getType();
        String typeName = dbObjectConfiguration.getTypeName();
        if (type.size() >= 2 && reference.isLike(type.getParent())) {
            StringBuilder equalRid = new StringBuilder(typeName);
            StringBuilder likeRid = new StringBuilder(escape(typeName));
            boolean subtree = false;
            for (int l = 0; l < reference.size(); l++) {
                String component = reference.getSegment(l).toClassicRepresentation();
                if (":*".equals(component)) {
                    if (!subtree) {
                        likeRid.append("/%");
                        subtree = true;
                        equalRid = null;
                    }
                } else if (!component
                    .equals(type.getSegment(l).toClassicRepresentation())) {
                    if (equalRid != null) {
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
    )
        throws ServiceException {
        boolean dateTime = value instanceof java.util.Date
            || value instanceof XMLGregorianCalendar
                && DatatypeConstants.DATETIME
                    .equals(((XMLGregorianCalendar) value).getXMLSchemaType());
        boolean timestampWithTimezone = dateTime
            && LayerConfigurationEntries.DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE
                .equals(getDateTimeType(connection));
        return timestampWithTimezone
            ? getTimestampWithTimzoneExpression(connection)
            : "?";
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
    private String getTimestampWithTimzoneExpression(Connection connection)
        throws ServiceException {
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
    )
        throws ServiceException,
        SQLException {
        String autonumFormat = getDriverProperty(
            conn,
            asFormat != null && asFormat.indexOf("AS CHAR") > 0 ? "AUTOINC.CHAR"
                : "AUTOINC.NUMERIC"
        );
        if ("AUTO".equals(autonumFormat)) {
            return null;
        } else if (autonumFormat != null) {
            autonumFormat = autonumFormat.replace("${SEQUENCE_NAME}", sequenceName);
            autonumFormat = autonumFormat
                .replace("${AS_FORMAT}", asFormat == null ? "" : asFormat);
            return autonumFormat;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "AUTONUM format undefined for database. It must be configured in in jdbc-driver-sql.properties.",
                new BasicException.Parameter(
                    "databaseName",
                    getDatabaseProductName(conn)
                )
            );
        }
    }

    public Path getReference(
        Connection conn,
        Object referenceId
    )
        throws ServiceException {
        return this.getDatabaseConfiguration().normalizeObjectIds() ? this.getDatabaseConfiguration().buildResourceIdentifier(
            (String) referenceId,
            true // resource
        )
            : this.getReferenceFormatTypeNameWithComponents(
                conn,
                (String) referenceId
            );
    }

    private Path getReferenceFormatTypeNameWithComponents(
        Connection conn,
        String referenceId
    )
        throws ServiceException {
        List<String> components = new ArrayList<String>();
        StringTokenizer t = new StringTokenizer(referenceId, "/");
        while (t.hasMoreTokens()) {
            components.add(t.nextToken());
        }
        if (components.isEmpty()) {
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
        String[] referenceComponents = new String[type.size() - 1];
        int pos = 1;
        for (int i = 0; i < referenceComponents.length; i++) {
            if (":*".equals(type.getSegment(i).toClassicRepresentation())) {
                if (pos >= components.size()) {
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
    )
        throws ServiceException {
        final String databaseProductName = conn == null ? null : this.getDatabaseProductName(conn);
        if (ignoreReservedWords) {
            return columnName;
        } else if ("HSQL Database Engine".equals(databaseProductName)
            && (RESERVED_WORDS_HSQLDB.contains(columnName)
                || (columnName.indexOf("$") >= 0))) {
            return '"' + columnName.toUpperCase() + '"';
        } else if ("Oracle".equals(databaseProductName)
            && RESERVED_WORDS_ORACLE.contains(columnName)) {
            return '"' + columnName + '"';
        } else {
            return columnName;
        }
    }

    @Override
    public String getColumnName(
        Connection conn,
        String attributeName,
        int index,
        boolean indexSuffixIfZero,
        boolean ignoreReservedWords,
        boolean markAsPrivate
    )
        throws ServiceException {
        ConcurrentMap<String, String> columnNames = markAsPrivate ? this.privateColumnNames : this.publicColumnNames;
        String columnName = columnNames.get(attributeName);
        if (columnName == null) {
            StringBuilder name = new StringBuilder();
            for (int i = 0; i < attributeName.length(); i++) {
                char c = attributeName.charAt(i);
                if (Character.isUpperCase(c)) {
                    name.append('_').append(Character.toLowerCase(c));
                } else if (c == '_') {
                    if (i < attributeName.length() - 1) {
                        // do not escape _<digit>
                        if (Character.isDigit(attributeName.charAt(i + 1))) {
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
            String lookupName = isSizeColumn
                ? columnName.substring(0, columnName.length() - 1)
                : columnName;
            String mappedName;
            String privateAttributesPrefix = this.getPrivateAttributesPrefix();
            if (markAsPrivate) {
                mappedName = this
                    .getDatabaseConfiguration()
                    .getFromToColumnNameMapping()
                    .get(privateAttributesPrefix + lookupName);
                if (mappedName == null) {
                    mappedName = this
                        .getDatabaseConfiguration()
                        .getFromToColumnNameMapping()
                        .get(lookupName);
                    if (mappedName != null) {
                        mappedName = privateAttributesPrefix + mappedName;
                    }
                }
            } else {
                mappedName = this
                    .getDatabaseConfiguration()
                    .getFromToColumnNameMapping()
                    .get(lookupName);
            }
            if (mappedName != null) {
                columnName = isSizeColumn ? mappedName + SIZE_SUFFIX : mappedName;
            } else if (markAsPrivate) {
                columnName = privateAttributesPrefix + columnName;
            }
            columnNames.putIfAbsent(attributeName, columnName);
        }
        // append index
        if (indexSuffixIfZero || index > 0) {
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

    @Override
    public String getFeatureName(String columnName)
        throws ServiceException {
        String mappedColumnName = columnName;
        // Indexed column?
        int nameLength = Math.max(
            mappedColumnName.lastIndexOf('$'),
            mappedColumnName.lastIndexOf('_')
        );
        // Non-indexed column
        if ((nameLength < 0) || mappedColumnName.endsWith("_")
            || mappedColumnName.endsWith("$")
            || !Character.isDigit(mappedColumnName.charAt(nameLength + 1))) {
            // Do not change column name
        }
        // Indexed column
        else {
            String attributeNameOfNonIndexedColumn = this.getFeatureName(mappedColumnName.substring(0, nameLength));
            // Only embedded features can be indexed
            if (this
                .getEmbeddedFeature(attributeNameOfNonIndexedColumn) != null) {
                return attributeNameOfNonIndexedColumn;
            }
        }

        // to->from mapping
        if (this
            .getDatabaseConfiguration()
            .getToFromColumnNameMapping()
            .containsKey(mappedColumnName)) {
            mappedColumnName = this
                .getDatabaseConfiguration()
                .getToFromColumnNameMapping()
                .get(mappedColumnName);
        }
        String featureName = this.featureNames.get(mappedColumnName);
        if (featureName == null) {
            String name = "";
            boolean nextAsUpperCase = false;
            for (int i = 0; i < mappedColumnName.length(); i++) {
                char c = Character.toLowerCase(mappedColumnName.charAt(i));
                if (c == '"') {
                    // Ignore quotes. Some databases, e.g. Postgres
                    // return columns containing special characters
                    // with quotes
                } else if (!nextAsUpperCase && (c == '_')) {
                    nextAsUpperCase = true;
                } else {
                    if (nextAsUpperCase) {
                        if (Character.isDigit(c)) {
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
            this.featureNames.putIfAbsent(mappedColumnName, featureName);
        }
        return featureName;
    }

    @Override
    public void setPreparedStatementValue(
        Connection conn,
        PreparedStatement ps,
        int position,
        Object value
    )
        throws ServiceException,
        SQLException {
        Object normalizedValue;
        if (value instanceof java.util.Date) {
            normalizedValue = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(
                DateTimeFormat.EXTENDED_UTC_FORMAT
                    .format((java.util.Date) value)
            );
        } else {
            normalizedValue = value;
        }
        if (normalizedValue instanceof URI) {
            ps.setString(position, normalizedValue.toString());
        } else if (normalizedValue instanceof Short) {
            ps.setShort(position, ((Short) normalizedValue).shortValue());
        } else if (normalizedValue instanceof Integer) {
            ps.setInt(position, ((Integer) normalizedValue).intValue());
        } else if (normalizedValue instanceof Long) {
            ps.setLong(position, ((Long) normalizedValue).longValue());
        } else if (normalizedValue instanceof BigDecimal) {
            ps.setBigDecimal(
                position,
                ((BigDecimal) normalizedValue)
                    .setScale(ROUND_UP_TO_MAX_SCALE, BigDecimal.ROUND_UP)
            );
        } else if (normalizedValue instanceof Number) {
            ps.setString(position, normalizedValue.toString());
        } else if (normalizedValue instanceof Path) {
            ps.setString(
                position,
                this.externalizePathValue(conn, (Path) normalizedValue)
            );
        } else if (normalizedValue instanceof Boolean) {
            Object sqlValue = this.getBooleanMarshaller().marshal(normalizedValue, conn);
            if (sqlValue instanceof Boolean) {
                ps.setBoolean(position, ((Boolean) sqlValue).booleanValue());
            } else if (sqlValue instanceof Number) {
                ps.setInt(position, ((Number) sqlValue).intValue());
            } else {
                ps.setString(position, sqlValue.toString());
            }
        } else if (normalizedValue instanceof Duration) {
            Object sqlValue = this.getDurationMarshaller().marshal(normalizedValue);
            if (sqlValue instanceof BigDecimal) {
                ps.setBigDecimal(position, (BigDecimal) sqlValue);
            } else if (sqlValue instanceof BigInteger) {
                ps.setBigDecimal(
                    position,
                    new BigDecimal((BigInteger) sqlValue)
                );
            } else if (sqlValue instanceof String) {
                ps.setString(position, (String) sqlValue);
            } else {
                ps.setObject(position, sqlValue);
            }
        } else if (normalizedValue instanceof XMLGregorianCalendar) {
            Object sqlValue = this.getCalendarMarshaller().marshal(normalizedValue, conn);
            if (sqlValue instanceof Time) {
                ps.setTime(position, (Time) sqlValue);
            } else if (sqlValue instanceof Timestamp) {
                Timestamp timestamp = (Timestamp) sqlValue;
                ps.setTimestamp(position, timestamp);
            } else if (sqlValue instanceof Date) {
                ps.setDate(position, (Date) sqlValue);
            } else if (sqlValue instanceof String) {
                ps.setString(position, (String) sqlValue);
            } else {
                ps.setObject(position, sqlValue);
            }
        } else if ((normalizedValue instanceof String)
            || (normalizedValue instanceof Reader)
            || (normalizedValue instanceof CharacterLargeObject)) {
            this.setClobColumnValue(ps, position, normalizedValue);
        } else if ((normalizedValue instanceof byte[])
            || (normalizedValue instanceof InputStream)
            || (normalizedValue instanceof BinaryLargeObject)) {
            this.setBlobColumnValue(ps, position, normalizedValue);
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "attribute type not supported",
                new BasicException.Parameter(
                    "value-type",
                    normalizedValue == null ? null
                        : normalizedValue.getClass().getName()
                ),
                new BasicException.Parameter("position", position)
            );
        }
    }

    private ObjectRecord getObject(
        Connection conn,
        Path path,
        short attributeSelector,
        Map<String, AttributeSpecifier> attributeSpecifiers,
        boolean objectClassAsAttribute,
        DbObject dbObject,
        ResultSet rs,
        String objectClass,
        boolean checkIdentity,
        boolean throwNotFoundException
    )
        throws ServiceException,
        SQLException {
        List<ObjectRecord> objects = new ArrayList<ObjectRecord>(1);
        this.getObjects(
            conn,
            dbObject,
            rs,
            objects,
            attributeSelector,
            attributeSpecifiers,
            objectClassAsAttribute,
            0,
            1,
            objectClass,
            null
        );
        if (objects.size() != 1) {
            if (!objects.isEmpty()) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Exactly one object expected",
                    new BasicException.Parameter("request-path", path),
                    new BasicException.Parameter(
                        "cardinality",
                        objects.size()
                    )
                );
            } else if (throwNotFoundException && checkIdentity) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "No object found",
                    new BasicException.Parameter("request-path", path),
                    new BasicException.Parameter(
                        "cardinality",
                        objects.size()
                    )
                );
            } else {
                return null;
            }
        }
        ObjectRecord object = objects.get(0);
        if (checkIdentity && !object.getResourceIdentifier().startsWith(path)) {
            if (throwNotFoundException) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "invalid object path. no object found",
                    new BasicException.Parameter("request-path", path),
                    new BasicException.Parameter(
                        "object-path",
                        object.getResourceIdentifier()
                    )
                );
            } else {
                return null;
            }
        }
        SysLog.detail("retrieved object", object);
        return object;
    }

    /**
     * Determines whether the given Model class is configuratively excluded from
     * persistency or not
     * 
     * @return <code>true</code> if the given Model class is not configuratively
     *         excluded from persistency
     */
    protected boolean isNotExcludedFromPersistency(String modelClass) {
        return !this.nonPersistentFeatures.contains(modelClass);
    }

    /**
     * Tells whether the given feature is persistent
     * 
     * @param featureDef
     *            the features meta-data
     * 
     * @return <code>true</code> if the given feature is persistent
     */
    public boolean isPersistent(ModelElement_1_0 featureDef)
        throws ServiceException {
        return isNotExcludedFromPersistency(featureDef.getQualifiedName())
            && Persistency.getInstance().isPersistentAttribute(featureDef);
    }

    protected Model_1_0 getModel() {
        return Model_1Factory.getModel();
    }

    protected Set<String> getAllSubtypes(String qualifiedTypeName)
        throws ServiceException {
        if (qualifiedTypeName == null)
            return null;
        ModelElement_1_0 classDef = getModel().getDereferencedType(qualifiedTypeName);
        if ("org:openmdx:base:BasicObject".equals(qualifiedTypeName)) {
            return null;
        } else {
            Set<String> allSubtypes = new HashSet<String>();
            for (Object path : classDef.objGetList("allSubtype")) {
                allSubtypes.add(
                    ((Path) path).getLastSegment().toClassicRepresentation()
                );
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
    ) {
        try {
            return getModel()
                .getElement(objectClass)
                .objGetMap("allFeature")
                .get(featureName);
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * Touch object feature (set empty value). The feature must be a modeled
     * feature which are either non-derived or system attributes (otherwise the
     * feature is not touched).
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
    )
        throws ServiceException {
        Object_2Facade facade = Facades.asObject(object);
        ModelElement_1_0 featureDef = getFeatureDef(facade.getObjectClass(), featureName);
        if (featureDef != null && isPersistent(featureDef)) {
            facade.attributeValues(featureDef.getName(), multiplicity);
        }
    }

    /**
     * Transfers attributes of 'attributeType' of objects contained in
     * 'resultSet' to the 'objects'. 'objects' is a List to maintain the sort
     * order from the db. The objects are added in the order retrieved.
     * <p>
     * If 'resultSet' contains object_objectIds which are not consecutive, an
     * exception is thrown.
     * <p>
     * The resultSet must hold the following preconditions:
     * <ul>
     * <li>all object_referencedIds must all be the same.</li>
     * <li>resultSet must be sorted by object_objectId and object_name</li>
     * </ul>
     * 
     * @param rs
     *            jdbc result set containing the objects.
     * @param attributeSelector
     *            requests attribute selector.
     * @param attributeSpecifiers
     *            as map.
     * @param reference
     *            object reference used to create path of returned objects.
     *            objectPath = reference + objectId.
     * @param objectList
     *            a list of DataproviderObjects contained in the result set rs.
     * @param attributeType
     *            attribute type [T_STRING|T_DECIMAL|T_BINARY].
     * @return true, if there are more rows in rs which are not read yet.
     *
     */
    @Override
    public boolean getObjects(
        final Connection conn,
        final DbObject dbObject,
        final ResultSet rs,
        final List<ObjectRecord> objects,
        final short attributeSelector,
        final Map<String, AttributeSpecifier> attributeSpecifiers,
        final boolean objectClassAsAttribute,
        final int position,
        final int initialObjectBatchSize,
        final String primaryObjectClass,
        final Target propagationTarget
    )
        throws ServiceException,
        SQLException {
        // set row count of rs to corresponding position
        int currentObjectBatchSize = initialObjectBatchSize;
        FastResultSet frs = this.setPosition(
            rs,
            position,
            dbObject.getIndexColumn() != null,
            dbObject.getConfiguration()
        );
        boolean hasMore = frs != null;
        // get objects
        String previousObjectId = null;
        Map<String, ModelElement_1_0> featureDefs = null;
        ObjectRecord current = null;
        final Set<Integer> processedIdxs = new HashSet<Integer>();
        String objectClass = null;
        while (hasMore) {
            Path objectPath = dbObject.getResourceIdentifier(frs);
            Path reference = objectPath.getParent();
            String objectId = objectPath.getLastSegment().toClassicRepresentation();
            final int idx = dbObject.getIndex(frs);
            // try to get attribute definitions
            if (idx == 0) {
                // Get OBJECT_CLASS if not supplied
                objectClass = primaryObjectClass == null
                    ? dbObject.getObjectClass(frs)
                    : primaryObjectClass;
                featureDefs = null;
            } else {
                objectClass = primaryObjectClass;
            }
            if (featureDefs == null && objectClass != null)
                try {
                    featureDefs = getModel()
                        .getElement(objectClass)
                        .objGetMap("allFeatureWithSubtype");
                } catch (Exception e) {
                    SysLog.trace(
                        "class not found in model fall back to non model-driven mode",
                        objectClass
                    );
                }
            // skip to next object? If yes, create new one and add it to list
            if (!objectId.equals(previousObjectId) || !reference.equals(current.getResourceIdentifier().getParent())) {
                if (currentObjectBatchSize != FETCH_SIZE_GREEDY &&
                    objects.size() >= currentObjectBatchSize) {
                    if (propagationTarget == null) {
                        return true;
                    }
                    propagate(propagationTarget, conn, attributeSelector, attributeSpecifiers, dbObject, objects);
                    if (propagationTarget.isSaturated()) {
                        return true;
                    }
                    currentObjectBatchSize = propagationTarget.getObjectBatchSize();
                }
                processedIdxs.clear();
                // add empty object to result set if necessary
                current = Facades.newObject(objectPath, objectClass).getDelegate();
                objects.add(current);
                previousObjectId = objectId;
            }
            // Duplicate indices are allowed. However, they are skipped
            if (!processedIdxs.contains(Integer.valueOf(idx))) {
                Object_2Facade currentFacade = Facades.asObject(current);
                boolean multivaluedAttributesRequested = areMultivaluedAttributesRequested(
                    attributeSpecifiers,
                    featureDefs
                );
                // Iterate through object attributes and add values
                for (String columnName : frs.getColumnNames()) {
                    String featureName = this.getFeatureName(columnName);
                    ModelElement_1_0 featureDef = featureDefs == null ? null
                        : featureDefs.get(featureName);
                    Multiplicity multiplicity = featureDef == null ? null
                        : ModelHelper.getMultiplicity(featureDef);
                    // Always include reference columns. Otherwise dbObject
                    // decides
                    if (dbObject.includeColumn(columnName)) {
                        // Check whether attribute must be added
                        boolean addValue;
                        if (SystemAttributes.OBJECT_CLASS.equals(featureName)) {
                            addValue = true;
                        } else {
                            switch (attributeSelector) {
                                case AttributeSelectors.NO_ATTRIBUTES:
                                    addValue = false;
                                    break;
                                case AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES:
                                    if (featureDef == null) {
                                        // Return in case the type is unknown
                                        addValue = true;
                                    } else if (multiplicity.isMultiValued()) {
                                        // Return multi-valued features if we
                                        // have at least one attribute specifier
                                        addValue = multivaluedAttributesRequested
                                            || this.getEmbeddedFeature(
                                                featureName
                                            ) != null;
                                    } else if (multiplicity.isStreamValued()) {
                                        // Return stream-valued features upon
                                        // request on
                                        addValue = attributeSpecifiers
                                            .containsKey(featureName);
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
                                    addValue = SYSTEM_ATTRIBUTES.contains(featureName)
                                        || attributeSpecifiers
                                            .containsKey(featureName);
                                    break;
                                default:
                                    // Illegal Attribute Specifier
                                    addValue = false;
                            }
                        }
                        // Add value
                        if (addValue) {
                            String featureType;
                            if (featureDef == null) {
                                featureType = null;
                            } else
                                try {
                                    featureType = getModel()
                                        .getElementType(featureDef)
                                        .getQualifiedName();
                                } catch (Exception exception) {
                                    throw new ServiceException(
                                        exception,
                                        BasicException.Code.DEFAULT_DOMAIN,
                                        BasicException.Code.INVALID_CONFIGURATION,
                                        "Unable to determine the element type",
                                        new BasicException.Parameter(
                                            "feature",
                                            featureDef.jdoGetObjectId()
                                        )
                                    );
                                }
                            Object val = frs.getObject(columnName);
                            // Embedded attribute? If yes derive idx from column
                            // name suffix (instead of slice index)
                            int valueIdx = idx;
                            boolean isEmbedded;
                            if (this.getEmbeddedFeature(featureName) != null) {
                                valueIdx = Integer.parseInt(columnName.substring(columnName.lastIndexOf('_') + 1));
                                isEmbedded = true;
                            } else {
                                isEmbedded = false;
                            }
                            // String, Clob
                            if (this.isClobColumnValue(val)) {
                                val = this.getClobColumnValue(
                                    val,
                                    featureName,
                                    featureDef
                                );
                                if (val instanceof Reader
                                    || val instanceof CharacterLargeObject) {
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
                                } else if (
                                // class type || PrimitiveTypes.PATH
                                isReferenceFeature(conn, frs, featureName)
                                    || ((featureType == null)
                                        && ((String) val).startsWith(
                                            URI_1Marshaller.OPENMDX_PREFIX
                                        ))
                                    || ((featureType == null)
                                        && ((String) val).startsWith("xri:"))
                                    || ((featureType != null)
                                        && (PrimitiveTypes.OBJECT_ID
                                            .equals(featureType)
                                            || getModel()
                                                .isClassType(featureType)))) {
                                    if (this.isUseNormalizedReferences()) {
                                        //
                                        // Get path from normalized form
                                        // (p$$<feature>_rid, p$$<feature>_oid
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
                                        if ((frs.getColumnNames().contains(
                                            referenceColumn
                                        ))
                                            && (frs.getColumnNames().contains(
                                                objectIdColumn
                                            ))) {
                                            //
                                            // Reference is stored in format
                                            // p$$<feature>__rid,
                                            // p$$<feature>__oid
                                            //
                                            Object rid = frs.getObject(referenceColumn);
                                            if (rid != null) {
                                                String oid = (String) frs
                                                    .getObject(objectIdColumn);
                                                Object values = currentFacade
                                                    .attributeValues(
                                                        featureName,
                                                        multiplicity
                                                    );
                                                Object value = this
                                                    .getReference(conn, rid)
                                                    .getChild(
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
                                            // Reference is stored in compressed
                                            // form in column
                                            //
                                            String ref = (String) frs.getObject(
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
                                            if (this
                                                .getDatabaseConfiguration()
                                                .normalizeObjectIds()) {
                                                resourceIdentifier = this
                                                    .getDatabaseConfiguration()
                                                    .buildResourceIdentifier(
                                                        ref,
                                                        false // reference
                                                    );
                                            } else {
                                                String rid = ref.substring(
                                                    0,
                                                    ref.lastIndexOf("/")
                                                );
                                                String oid = ref.substring(
                                                    ref.lastIndexOf("/") + 1
                                                );
                                                resourceIdentifier = this
                                                    .getReference(conn, rid)
                                                    .getChild(oid);
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
                                    // get path from non-normalized, stringified
                                    // form
                                    else {
                                        Object values = currentFacade.attributeValues(
                                            featureName,
                                            multiplicity
                                        );
                                        try {
                                            this.setValue(
                                                values,
                                                valueIdx,
                                                this.internalizePathValue(
                                                    (String) val
                                                ),
                                                isEmbedded
                                            );
                                        } catch (Exception e) {
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
                                    if (val != null) {
                                        if (SystemAttributes.OBJECT_CLASS
                                            .equals(featureName)) {
                                            currentFacade
                                                .getValue()
                                                .setRecordName(
                                                    ((String) val).trim()
                                                );
                                            // Store object class as attribute
                                            if (objectClassAsAttribute) {
                                                Object target = currentFacade
                                                    .attributeValues(
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
                                            } catch (IndexOutOfBoundsException exception) {
                                                throw new ServiceException(
                                                    exception,
                                                    BasicException.Code.DEFAULT_DOMAIN,
                                                    BasicException.Code.ASSERTION_FAILURE,
                                                    "Index out ouf bounds",
                                                    new BasicException.Parameter(
                                                        "object",
                                                        currentFacade
                                                            .getDelegate()
                                                    ),
                                                    new BasicException.Parameter(
                                                        "feature",
                                                        featureName
                                                    ),
                                                    new BasicException.Parameter(
                                                        "multiplicity",
                                                        multiplicity
                                                    ),
                                                    new BasicException.Parameter(
                                                        "target",
                                                        target
                                                    ),
                                                    new BasicException.Parameter(
                                                        "index",
                                                        valueIdx
                                                    ),
                                                    new BasicException.Parameter(
                                                        "value",
                                                        val
                                                    )
                                                );
                                            }
                                        }
                                    }
                                }
                            }
                            // byte[], Blob
                            else if (this.isBlobColumnValue(val)) {
                                Object value = this.getBlobColumnValue(
                                    val,
                                    featureName,
                                    featureDef
                                );
                                Object values = currentFacade
                                    .attributeValues(featureName, multiplicity);
                                this.setValue(
                                    values,
                                    valueIdx,
                                    value,
                                    isEmbedded
                                );
                            }
                            // Null
                            else if (val == null) {
                                // Touch feature if value is null and feature is
                                // member of class
                                if (valueIdx == 0) {
                                    if (featureName.lastIndexOf(':') > 0) {
                                        throw new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.ASSERTION_FAILURE,
                                            "Scoped features not supported",
                                            new BasicException.Parameter(
                                                "object",
                                                current
                                            ),
                                            new BasicException.Parameter(
                                                "attribute",
                                                featureName
                                            )
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
                                    currentFacade.attributeValues(
                                        featureName,
                                        multiplicity
                                    ),
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
        if (propagationTarget != null) {
            propagate(propagationTarget, conn, attributeSelector, attributeSpecifiers, dbObject, objects);
        }
        return false;
    }

    private void propagate(
        final Target target,
        final Connection conn,
        final short attributeSelector,
        final Map<String, AttributeSpecifier> attributeSpecifiersAsMap,
        final DbObject dbObject,
        final List<ObjectRecord> objects
    )
        throws ServiceException {
        if (!objects.isEmpty()) {
            completeRequestedAttributes(
                conn,
                attributeSelector,
                attributeSpecifiersAsMap.keySet(),
                dbObject.getConfiguration(),
                objects
            );
        }
        for (ObjectRecord object : objects) {
            target.accept(object);
        }
        objects.clear();
    }

    /**
     * @param attributeSelector
     * @param attributeSpecifiers
     * @param facade2
     * @return
     */
    private boolean fetchAll(
        short attributeSelector,
        Set<String> attributeSpecifierNames,
        Object_2Facade facade2
    ) {
        if (attributeSelector == AttributeSelectors.ALL_ATTRIBUTES) {
            return true;
        } else if (attributeSelector == AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES) {
            for (String attributeSpecifierName : attributeSpecifierNames) {
                if (!facade2.getValue().keySet().contains(attributeSpecifierName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void completeRequestedAttributes(
        Connection conn,
        short attributeSelector,
        Set<String> attributeSpecifierNames,
        DbObjectConfiguration dbObjectConfiguration,
        List<ObjectRecord> objects
    )
        throws ServiceException {
        Object_2Facade facade2 = Facades.asObject(objects.get(0));
        if (fetchAll(attributeSelector, attributeSpecifierNames, facade2)) {
            final List<Object> statementParameters2 = new ArrayList<Object>();
            String currentStatement = null;
            PreparedStatement ps2 = null;
            ResultSet rs2 = null;
            try {
                DbObject dbObject2 = createDbObject(
                    conn,
                    facade2.getPath(),
                    true
                );
                // Additional fetch only if secondary dbObject is available
                boolean hasSecondaryDbObject = (dbObject2.getConfiguration().getDbObjectForQuery2() != null) ||
                    (dbObject2.getConfiguration().getDbObjectForUpdate2() != null);
                if (hasSecondaryDbObject) {
                    // Query to retrieve the secondary columns
                    // SELECT v.* FROM secondary table WHERE (object id clause 0) OR (object id clause 1) ... ORDER BY object ids, idx
                    String statement2 = "SELECT v.* FROM "
                        + (dbObject2.getConfiguration().getDbObjectForQuery2() == null ? dbObject2.getConfiguration()
                            .getDbObjectForUpdate2() : dbObjectConfiguration.getDbObjectForQuery2()) + " v"
                        + " WHERE (";
                    String separator = "";
                    for (ObjectRecord object : objects) {
                        dbObject2 = createDbObject(
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
                    for (Object objectIdColumn : dbObject2.getObjectIdColumn()) {
                        statement2 += separator;
                        statement2 += "v." + objectIdColumn;
                        separator = ",";
                    }
                    statement2 += ", v." + getObjectIdxColumnName();
                    ps2 = prepareStatement(
                        conn,
                        currentStatement = statement2.toString()
                    );
                    for (int i = 0, iLimit = statementParameters2.size(); i < iLimit; i++) {
                        setPreparedStatementValue(
                            conn,
                            ps2,
                            i + 1,
                            statementParameters2.get(i)
                        );
                    }
                    rs2 = this.executeQuery(
                        ps2,
                        statement2.toString(),
                        statementParameters2,
                        0 // no limit for maxRows
                    );
                    List<ObjectRecord> objects2 = new ArrayList<ObjectRecord>();
                    getObjects(
                        conn,
                        dbObject2,
                        rs2,
                        objects2,
                        AttributeSelectors.ALL_ATTRIBUTES,
                        Collections.<String, AttributeSpecifier>emptyMap(),
                        false, // objectClassAsAttribute
                        0,
                        FETCH_SIZE_GREEDY,
                        facade2.getObjectClass(),
                        null
                    );
                    // Add attributes of objects2 to objects
                    Map<Path, ObjectRecord> objects2AsMap = new HashMap<Path, ObjectRecord>();
                    for (ObjectRecord object2 : objects2) {
                        objects2AsMap.put(
                            object2.getResourceIdentifier(),
                            object2
                        );
                    }
                    for (ObjectRecord object : objects) {
                        ObjectRecord object2 = objects2AsMap.get(object.getResourceIdentifier());
                        if (object2 != null) {
                            Object_2Facade.getValue(object2).keySet().removeAll(
                                object.keySet()
                            );
                            Object_2Facade.getValue(object).putAll(
                                Object_2Facade.getValue(object2)
                            );
                        }
                    }
                }
            } catch (SQLException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE,
                    "Error when executing SQL statement",
                    new BasicException.Parameter("statement", currentStatement),
                    new BasicException.Parameter("parameters", statementParameters2),
                    new BasicException.Parameter("sqlErrorCode", exception.getErrorCode()),
                    new BasicException.Parameter("sqlState", exception.getSQLState())
                );
            } finally {
                Closeables.close(rs2);
                Closeables.close(ps2);
            }
        }
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
    )
        throws ServiceException {
        if (featureDefs != null) {
            for (String featureName : attributeSpecifiers.keySet()) {
                ModelElement_1_0 featureDef = featureDefs.get(featureName);
                if (featureDef != null) {
                    if (ModelHelper.getMultiplicity(featureDef).isMultiValued()
                        && this.getEmbeddedFeature(featureName) == null) {
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
    )
        throws ServiceException {
        return this.isUseNormalizedReferences()
            && (frs.getColumnNames().contains(
                this.getColumnName(
                    conn,
                    toRid(featureName),
                    0,
                    false,
                    false,
                    true
                )
            )
                || frs.getColumnNames().contains(
                    this.getColumnName(
                        conn,
                        featureName + "Parent",
                        0,
                        false,
                        false,
                        true
                    )
                ));
    }

    protected void setValue(
        Object target,
        int index,
        Object value,
        boolean allowFilling
    ) {
        if (target instanceof SparseArray) {
            ((SparseArray) target).put(Integer.valueOf(index), value);
        } else {
            List values = (List) target;
            // Fill value up to requested index if allowFilling = true
            if (allowFilling) {
                while (index > values.size()) {
                    values.add(value);
                }
            }
            if (index == values.size()) {
                values.add(value);
            } else {
                values.set(index, value);
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
    )
        throws ServiceException {
        if (PrimitiveTypes.BOOLEAN.equals(featureType)
            || val instanceof Boolean) {
            //
            // org:: w3c::boolean
            //
            this.setValue(
                target,
                index,
                this.getBooleanMarshaller().unmarshal(val, conn),
                isEmbedded
            );
        } else if (PrimitiveTypes.DATE.equals(featureType)
            && val instanceof Timestamp) {
            // Some Jdbc drivers / databases may return java.sql.Timestamp
            // instead
            // of java.sql.Date even if the column type is Date. Force the
            // conversion
            // from Timestamp to date.
            this.setValue(
                target,
                index,
                this.getCalendarMarshaller().unmarshal(
                    val.toString().substring(0, 10)
                ),
                isEmbedded
            );
        } else if (PrimitiveTypes.DATE.equals(featureType)
            || PrimitiveTypes.DATETIME.equals(featureType)
            || val instanceof Timestamp || val instanceof Time) {
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
        } else if (PrimitiveTypes.DURATION.equals(featureType)) {
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
        } else if (val instanceof Number) {
            //
            // openMDX 1 clients expect all numbers to be returned
            // as BigIntegers
            //
            Object value = val instanceof BigDecimal ? val
                : new BigDecimal(val.toString());
            this.setValue(target, index, value, isEmbedded);
        } else if (val instanceof String) {
            //
            // default
            //
            String databaseProductName = this.getDatabaseProductName(conn);
            if ("Oracle".equals(databaseProductName)) {
                // Oracle maps empty strings to null. Fill list with empty
                // strings up to index.
                if (target instanceof List) {
                    List values = (List) target;
                    while (index > values.size()) {
                        values.add("");
                    }
                }
            }
            this.setValue(target, index, val, isEmbedded);
        } else {
            //
            // unknown
            //
            if (PrimitiveTypes.STRING.equals(featureType)) {
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
                } catch (Exception ignore) {
                    /* ignore */}
            }
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "invalid column type. Supported are [Number|String|byte[]|Blob|Timestamp|Time]",
                new BasicException.Parameter("featureType", featureType),
                new BasicException.Parameter(
                    "columnType",
                    val.getClass().getName()
                )
            );
        }
    }

    @Override
    public void filterToSqlClauses(
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
    )
        throws ServiceException {
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
                true, // joinViewIsPrimary
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
        for (Iterator<FilterProperty> i = allFilterProperties.iterator(); i
            .hasNext();) {
            FilterProperty p = i.next();
            if (!primaryFilterProperties.contains(p)) {
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
                // Negative clauses are only required if maxSlicesPerObject > 0.
                // In this case
                // non-matching slices have to be subtracted from the plus
                // result set.
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
                if (!excludingClause.isEmpty()) {
                    exludingClauses.add(excludingClause);
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
     * tables. The generated SQL clause if of the form: ((object_name = ?) AND
     * (object_val operator ?)) AND ...
     * <p>
     * <ul>
     * <li>If minus is true, clauses are only generated for filter properties
     * with operator FOR_ALL.</li>
     * <li>If minus is false then clauses are generated for all filter
     * properties.</li>
     * <p>
     * Clauses are only generated if the property type matches attributeType.
     * The clause is generated in the form of a prepared statement with ? as
     * place holders. The corresponding values are stored in filterValues. The
     * type of a filter property is determined of its first value (get(0)).
     * 
     * @param conn
     * @param dbObject
     * @param statedObject
     * @param viewAliasName
     * @param fixedViewAliasName
     *            avoid mix-in view alias references
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
    )
        throws ServiceException {
        StringBuilder clause = new StringBuilder();
        List<Object> clauseValues = new ArrayList<Object>();
        boolean hasProperties = false;
        String operator = "";
        /**
         * Generate clause for all filter properties: If negate -->
         * negate(expr0) OR negate(expr1) ... If !negate --> expr0 AND expr1 ...
         */
        List<ModelElement_1_0> filterPropertyDefs = Database_2.this.getFilterPropertyDefs(referencedType, filterProperties);
        for (int i = 0; i < filterProperties.size(); i++) {
            FilterProperty filterProperty = filterProperties.get(i);
            ModelElement_1_0 filterPropertyDef = filterPropertyDefs.get(i);
            /**
             * FOR_ALL --> all attribute values must match --> all slices must
             * match THERE_EXISTS --> at least one attribute value must match
             * --> at least one row must match --> subtract all rows which do
             * not match
             */
            if (filterProperty.quantor() == (negate ? Quantifier.FOR_ALL : Quantifier.THERE_EXISTS).code()) {
                // For embedded features the clause is of the form (expr0 OR
                // expr1 OR ... OR exprN)
                // where N is the upper bound for the embedded feature
                final int upperBound = Numbers.getValue(this.getEmbeddedFeature(filterProperty.name()), 1);
                clause.append(operator).append("(");
                for (int idx = 0; idx < upperBound; idx++) {
                    String columnName = this.getColumnName(
                        conn,
                        filterProperty.name(),
                        idx,
                        upperBound > 1,
                        true,
                        false // markAsPrivate
                    );
                    boolean mixInView = !fixedViewAliasName && viewIsPrimary && viewIsIndexed;
                    columnName = viewAliasName + (mixInView ? "m." : ".") + columnName;
                    if (idx > 0) {
                        clause.append(
                            filterProperty.quantor() == Quantifier.FOR_ALL.code() ? " AND " : " OR "
                        );
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
                    if (viewIsPrimary &&
                        (filterProperty.quantor() == Quantifier.FOR_ALL.code()) &&
                        (filterProperty.getValues().length == 0 || !(filterProperty.getValue(0) instanceof QueryFilterRecord))) {
                        clause.append(" OR (").append(columnName).append(" IS NULL)");
                    }
                    clause.append(")");
                }
                clause.append(")");
                hasProperties = true;
            }
            if (hasProperties) {
                operator = negate && !viewIsPrimary ? " OR " : " AND ";
            }
        }
        if (hasProperties) {
            if (viewIsPrimary) {
                statementParameters.addAll(clauseValues);
                return clause.toString();
            } else {
                // View is the secondary db object containing the multi-valued
                // columns
                // of an object. This is why reference clause 2 must be used for
                // selection
                if (dbObject.getConfiguration().hasDbObject2()) {
                    statementParameters.addAll(clauseValues);
                    switch (joinType) {
                        case NONE:
                            return "(SELECT "
                                + dbObject.getObjectIdColumn().get(0) + " FROM "
                                + (view.startsWith("SELECT")
                                    ? "(" + view + ") " + viewAliasName
                                    : view + " " + viewAliasName + " ")
                                + " WHERE (" + clause + "))";
                        case SPECIFIED_COLUMN_WITH_OBJECT_ID:
                            return "(" + (negate ? "NOT " : "")
                                + "EXISTS (SELECT 1 FROM "
                                + (view.startsWith("SELECT")
                                    ? "(" + view + ") " + viewAliasName
                                    : view + " " + viewAliasName + " ")
                                + " WHERE "
                                + viewAliasName + "." + dbObject.getObjectIdColumn().get(0) + " = " + joinColumn + " AND "
                                + " (" + clause + ")))";
                        case OBJECT_RID_AND_OID:
                            return (negate ? "(NOT " : "(")
                                + "EXISTS (SELECT 1 FROM "
                                + (view.startsWith("SELECT") ? "(" + view + ")" : view)
                                + " " + viewAliasName + " WHERE "
                                + viewAliasName.substring(0, viewAliasName.length() - 1)
                                + "." + dbObject.getObjectIdColumn().get(0)
                                + " = " + viewAliasName + "."
                                + dbObject.getObjectIdColumn().get(0) + " AND "
                                + viewAliasName.substring(0, viewAliasName.length() - 1)
                                + "." + dbObject.getReferenceColumn().get(0)
                                + " = " + viewAliasName + "."
                                + dbObject.getReferenceColumn().get(0) + " AND "
                                + "(" + clause + ")))";
                        default:
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                "Unexpected Join Type",
                                new BasicException.Parameter(
                                    "JoinType",
                                    joinType
                                )
                            );
                    }
                } else {
                    statementParameters.addAll(dbObject.getReferenceValues());
                    statementParameters.addAll(clauseValues);
                    switch (joinType) {
                        case NONE:
                            return "(SELECT "
                                + dbObject.getObjectIdColumn().get(0) + " FROM "
                                + (view.startsWith("SELECT")
                                    ? "(" + view + ") " + viewAliasName
                                    : view + " " + viewAliasName + " ")
                                + " WHERE " + dbObject.getReferenceClause()
                                + " AND (" + clause + "))";
                        case SPECIFIED_COLUMN_WITH_OBJECT_ID:
                            return "(" + joinColumn + " "
                                + (negate ? "NOT" : "") + " IN (SELECT "
                                + dbObject.getObjectIdColumn().get(0) + " FROM "
                                + (view.startsWith("SELECT")
                                    ? "(" + view + ") " + viewAliasName
                                    : view + " " + viewAliasName + " ")
                                + " WHERE " + dbObject.getReferenceClause()
                                + " AND (" + clause + ")))";
                        default:
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                "Unexpected Join Type",
                                new BasicException.Parameter(
                                    "JoinType",
                                    joinType
                                )
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
     * @param negation
     *            true for IS_NOT_IN, false for IS_IN,
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
    )
        throws ServiceException {
        clause.append(columnName).append(negation ? " NOT IN (" : " IN (");
        String separator = "";
        for (Object value : values) {
            clause.append(separator).append(getPlaceHolder(connection, value));
            clauseValues.add(this.externalizeStringValue(columnName, value));
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
    )
        throws ServiceException {
        Set<Path> matchingPatterns = new HashSet<Path>();
        boolean includeSubTree = "%".equals(value.getLastSegment().toClassicRepresentation());
        // If path ends with % get the set of matching types
        Path path = value;
        if (includeSubTree) {
            path = value.getParent();
            for (Iterator<DbObjectConfiguration> k = this
                .getDatabaseConfiguration()
                .getDbObjectConfigurations()
                .iterator(); k.hasNext();) {
                DbObjectConfiguration dbObjectConfiguration = k.next();
                if ((dbObjectConfiguration.getType().size() >= path.size())
                    && path.isLike(
                        dbObjectConfiguration
                            .getType()
                            .getPrefix(path.size())
                    )) {
                    // Replace type pattern by filter value pattern
                    Path type = dbObjectConfiguration.getType();
                    String[] pattern = new String[type.size()];
                    int i = 0;
                    for (XRISegment p : type.getSegments()) {
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
    )
        throws ServiceException {
        String operator = "";
        for (Path pattern : matchingPatterns) {
            String externalized = this.externalizePathValue(
                connection,
                path.getDescendant(pattern.getSuffix(path.size()))
            );
            clause
                .append(operator)
                .append(like ? "(" : "(NOT (")
                .append(columnName)
                .append(" LIKE ? ")
                .append(getEscapeClause(connection))
                .append(like ? ")" : "))");
            int pos = externalized.indexOf("%");
            if (pos >= 0) {
                externalized = externalized.substring(0, pos + 1);
            }
            if (externalized.startsWith("xri:")) {
                if (externalized.endsWith("***")) {
                    externalized = externalized.substring(0, externalized.length() - 3)
                        + "%";
                }
                pos = externalized.indexOf("**");
                if (pos >= 0) {
                    if (path != value && pos == externalized.length() - 2) {
                        externalized = externalized.substring(0, pos) + "%";
                    } else
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "only (***|%) wildcards at the end supported",
                            new BasicException.Parameter("value", value),
                            new BasicException.Parameter("path", externalized),
                            new BasicException.Parameter("position", pos)
                        );
                }
            } else if (externalized.startsWith("spice:")) {
                for (XRISegment p : value.getSegments()) {
                    String c = p.toString();
                    if (c.startsWith(":") && c.endsWith("*")) {
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
    ) {
        if (xri.size() > 2) {
            String requestedProvider = xri.getSegment(2).toClassicRepresentation();
            if (":*".equals(requestedProvider) && context != null
                && context.size() > 2) {
                return new Path(
                    new String[] {
                        xri.getSegment(0).toClassicRepresentation(),
                        xri.getSegment(1).toClassicRepresentation(),
                        context.getSegment(2).toClassicRepresentation()
                    }
                ).getDescendant(xri.getSuffix(3));
            }
        }
        return xri;
    }

    /**
     * Appends an SQL clause to 'clause' according to filter property 'p'. if
     * 'negate' is true, then the operation is negated. The clause is generated
     * for SQL column with 'columnName'. The clause is generated prepared
     * statement using ? as place holders. The corresponding values are added to
     * filterValues. The generated clause is of the form (columnName operator
     * literal). ANY or EACH are not handled by this method.
     * 
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
    )
        throws ServiceException {
        StringBuilder clause = new StringBuilder();
        final ConditionType operator;
        final Quantifier quantor;
        if (negate) {
            quantor = Quantifier.valueOf(Quantifier.invert(filterProperty.quantor()));
            operator = ConditionType
                .valueOf(ConditionType.invert(filterProperty.operator()));
        } else {
            quantor = Quantifier.valueOf(filterProperty.quantor());
            operator = ConditionType.valueOf(filterProperty.operator());
        }
        switch (operator) {

            /**
             * Evaluate the following: THERE_EXISTS v IN A: v NOT IN Q (Special:
             * if Q={} ==> true, iff A<>{}, false otherwise) FOR_ALL v IN A: v
             * NOT IN Q (Special: if Q={} ==> true)
             */
            case IS_NOT_IN:
                // Q = {}
                if (filterProperty.getValues().length == 0) {
                    if (quantor == Quantifier.FOR_ALL) {
                        clause.append("(1=1)");
                    } else {
                        clause.append("(").append(columnName).append(
                            " IS NOT NULL)"
                        );
                    }
                }

                // Q <> {}
                else {
                    // Complex filter value
                    if ((filterProperty.getValues().length > 0)
                        && (filterProperty
                            .getValue(0) instanceof QueryFilterRecord)) {
                        if (filterPropertyDef.isReferenceType()) {
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
                                "Filter property with value of type "
                                    + QueryFilterRecord.class.getName()
                                    + " must be a Reference",
                                new BasicException.Parameter(
                                    "filter.property",
                                    filterProperty
                                ),
                                new BasicException.Parameter(
                                    "filter.definition",
                                    filterPropertyDef
                                )
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
                clause
                    .append("(")
                    .append(columnName)
                    .append(" < ")
                    .append(getPlaceHolder(conn, filterProperty.getValue(0)))
                    .append(")");
                clauseValues.add(
                    this.externalizeStringValue(
                        columnName,
                        filterProperty.getValue(0)
                    )
                );
                break;

            // IS_LESS_OR_EQUAL
            case IS_LESS_OR_EQUAL:
                clause
                    .append("(")
                    .append(columnName)
                    .append(" <= ")
                    .append(getPlaceHolder(conn, filterProperty.getValue(0)))
                    .append(")");
                clauseValues.add(
                    this.externalizeStringValue(
                        columnName,
                        filterProperty.getValue(0)
                    )
                );
                break;

            // IS_IN
            // Evaluate the following:
            // THERE_EXISTS v IN A: v IN Q (Special: if Q={} ==> false)
            // FOR_ALL v IN A: v IN Q (Special: if Q={} ==> true, iff A={},
            // false otherwise)
            case IS_IN:

                // Q = {}
                if (filterProperty.getValues().length == 0) {
                    if (quantor == Quantifier.THERE_EXISTS) {
                        clause.append("(1=0)");
                    } else {
                        clause
                            .append("(")
                            .append(columnName)
                            .append(" IS NULL)");
                    }
                }

                // Q <> {}
                else {
                    // Complex filter value
                    if ((filterProperty.getValues().length > 0)
                        && (filterProperty
                            .getValue(0) instanceof QueryFilterRecord)) {
                        if (filterPropertyDef.isReferenceType()) {
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
                                clauseValues,
                                clause
                            );
                        } else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "Filter property with value of type "
                                    + QueryFilterRecord.class.getName()
                                    + " must be a Reference",
                                new BasicException.Parameter(
                                    "filter.property",
                                    filterProperty
                                ),
                                new BasicException.Parameter(
                                    "filter.definition",
                                    filterPropertyDef
                                )
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
                            clauseValues,
                            filterProperty.getValues()
                        );
                        clause.append(")");

                    }
                }
                break;

            // IS_GREATER_OR_EQUAL
            case IS_GREATER_OR_EQUAL:
                clause
                    .append("(")
                    .append(columnName)
                    .append(" >= ")
                    .append(getPlaceHolder(conn, filterProperty.getValue(0)))
                    .append(")");
                clauseValues.add(
                    this.externalizeStringValue(
                        columnName,
                        filterProperty.getValue(0)
                    )
                );
                break;

            // IS_GREATER
            case IS_GREATER:
                clause
                    .append("(")
                    .append(columnName)
                    .append(" > ")
                    .append(getPlaceHolder(conn, filterProperty.getValue(0)))
                    .append(")");
                clauseValues.add(
                    this.externalizeStringValue(
                        columnName,
                        filterProperty.getValue(0)
                    )
                );
                break;

            // IS_BETWEEN
            case IS_BETWEEN:
                clause
                    .append("((")
                    .append(columnName)
                    .append(" >= ")
                    .append(getPlaceHolder(conn, filterProperty.getValue(0)))
                    .append(") AND (")
                    .append(columnName)
                    .append(" <= ")
                    .append(getPlaceHolder(conn, filterProperty.getValue(1)))
                    .append("))");
                clauseValues.add(
                    this.externalizeStringValue(
                        columnName,
                        filterProperty.getValue(0)
                    )
                );
                clauseValues.add(
                    this.externalizeStringValue(
                        columnName,
                        filterProperty.getValue(1)
                    )
                );
                break;

            // IS_OUTSIDE
            case IS_OUTSIDE:
                clause
                    .append("((")
                    .append(columnName)
                    .append(" < ")
                    .append(getPlaceHolder(conn, filterProperty.getValue(0)))
                    .append(") OR (")
                    .append(columnName)
                    .append(" > ")
                    .append(getPlaceHolder(conn, filterProperty.getValue(1)))
                    .append("))");
                clauseValues.add(
                    this.externalizeStringValue(
                        columnName,
                        filterProperty.getValue(0)
                    )
                );
                clauseValues.add(
                    this.externalizeStringValue(
                        columnName,
                        filterProperty.getValue(1)
                    )
                );
                break;

            // IS_LIKE
            case IS_LIKE:
                clause.append("(");
                for (int j = 0; j < filterProperty.getValues().length; j++) {
                    if (j > 0) {
                        clause.append(" OR ");
                    }
                    Object v = filterProperty.getValue(j);
                    if (v instanceof Path) {
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
                        isLikeToSqlClause(
                            conn,
                            columnName,
                            clauseValues,
                            clause,
                            v
                        );
                    }
                }
                clause.append(")");
                break;

            // IS_UNLIKE
            case IS_UNLIKE:
                clause.append("(");
                for (int j = 0; j < filterProperty.getValues().length; j++) {
                    if (j > 0) {
                        clause.append(" AND ");
                    }
                    Object v = filterProperty.getValue(j);
                    if (v instanceof Path) {
                        Path vp = (Path) v;
                        Set<Path> matchingTypes = new HashSet<Path>();
                        matchingTypes.add(vp);
                        boolean includeSubTree = "%".equals(
                            vp.getLastSegment().toClassicRepresentation()
                        );
                        // If path ends with % get the set of matching types
                        if (includeSubTree) {
                            vp = vp.getPrefix(vp.size() - 1);
                            for (DbObjectConfiguration dbObjectConfiguration : this
                                .getDatabaseConfiguration()
                                .getDbObjectConfigurations()) {
                                if ((dbObjectConfiguration.getType().size() > vp
                                    .size())
                                    && vp.isLike(
                                        dbObjectConfiguration
                                            .getType()
                                            .getPrefix(vp.size())
                                    )) {
                                    matchingTypes
                                        .add(dbObjectConfiguration.getType());
                                }
                            }
                        }
                        // NOT LIKE clause for each path pattern
                        int ii = 0;
                        for (Iterator<?> i = matchingTypes.iterator(); i
                            .hasNext(); ii++) {
                            Path type = (Path) i.next();
                            if (ii > 0) {
                                clause.append(" AND ");
                            }
                            clause
                                .append("(NOT (")
                                .append(columnName)
                                .append(" LIKE ? ")
                                .append(getEscapeClause(conn))
                                .append("))");
                            String externalized = this.externalizePathValue(
                                conn,
                                vp.getDescendant(type.getSuffix(vp.size()))
                            );
                            int pos = externalized.indexOf("%");
                            if (pos >= 0) {
                                externalized = externalized.substring(0, pos + 1);
                            }
                            if (externalized.startsWith("xri:")) {
                                if (externalized.endsWith("***")) {
                                    externalized = externalized.substring(
                                        0,
                                        externalized.length() - 3
                                    ) + "%";
                                }
                                pos = externalized.indexOf("**");
                                if (pos >= 0) {
                                    if (includeSubTree
                                        && pos == externalized.length() - 2) {
                                        externalized = externalized.substring(0, pos)
                                            + "%";
                                    } else
                                        throw new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.NOT_SUPPORTED,
                                            "only (***|%) wildcards at the end supported",
                                            new BasicException.Parameter(
                                                "value",
                                                v
                                            ),
                                            new BasicException.Parameter(
                                                "path",
                                                externalized
                                            ),
                                            new BasicException.Parameter(
                                                "position",
                                                pos
                                            )
                                        );
                                }
                            } else if (externalized.startsWith("spice:")) {
                                for (XRISegment p : ((Path) v).getSegments()) {
                                    String c = p.toString();
                                    if (c.startsWith(":") && c.endsWith("*")) {
                                        throw new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.NOT_SUPPORTED,
                                            "path component pattern ':<pattern>*' not supported",
                                            new BasicException.Parameter(
                                                "path",
                                                externalized
                                            ),
                                            new BasicException.Parameter(
                                                "position",
                                                i
                                            )
                                        );
                                    }
                                }
                            }
                            clauseValues.add(externalized);
                        }
                    } else {
                        clause.append("NOT(");
                        isLikeToSqlClause(
                            conn,
                            columnName,
                            clauseValues,
                            clause,
                            v
                        );
                        clause.append(")");
                    }
                }
                clause.append(")");
                break;

            // SOUNDS_LIKE
            case SOUNDS_LIKE:
                clause.append("(SOUNDEX(").append(columnName).append(
                    ") IN (SOUNDEX(?)"
                );
                clauseValues.add(
                    this.externalizeStringValue(
                        columnName,
                        filterProperty.getValue(0)
                    )
                );
                for (int j = 1; j < filterProperty.getValues().length; j++) {
                    clause.append(", SOUNDEX(?)");
                    clauseValues.add(
                        this.externalizeStringValue(
                            columnName,
                            filterProperty.getValue(j)
                        )
                    );
                }
                clause.append("))");
                break;

            // SOUNDS_UNLIKE
            case SOUNDS_UNLIKE:
                clause.append("(SOUNDEX(").append(columnName).append(
                    ") NOT IN (SOUNDEX(?)"
                );
                clauseValues.add(
                    this.externalizeStringValue(
                        columnName,
                        filterProperty.getValue(0)
                    )
                );
                for (int j = 1; j < filterProperty.getValues().length; j++) {
                    clause.append(", SOUNDEX(?)");
                    clauseValues.add(
                        this.externalizeStringValue(
                            columnName,
                            filterProperty.getValue(j)
                        )
                    );
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

    /**
     * Retrieve sqlWildcards.
     *
     * @return Returns the sqlWildcards.
     */
    @Override
    public SQLWildcards getSqlWildcards() {
        return this.sqlWildcards;
    }

    private void isLikeToSqlClause(
        Connection conn,
        String columnName,
        List<Object> clauseValues,
        StringBuilder clause,
        Object v
    )
        throws ServiceException {
        EmbeddedFlags.FlagsAndValue flagsAndValue = this.embeddedFlags
            .parse((String) this.externalizeStringValue(columnName, v));
        EnumSet<RegularExpressionFlag> flagSet = flagsAndValue.getFlagSet();
        String externalized;
        if (flagSet.contains(RegularExpressionFlag.X_QUERY)
            || flagSet.contains(RegularExpressionFlag.JSON_QUERY)) {
            externalized = flagsAndValue.getValue();
        } else {
            externalized = getSqlWildcards().fromJDO(flagsAndValue.getValue());
        }
        if (flagSet.contains(RegularExpressionFlag.ACCENT_INSENSITIVE)
            && flagSet.contains(RegularExpressionFlag.CASE_INSENSITIVE)) {
            isLikeToSqlClause(
                conn,
                columnName,
                clauseValues,
                clause,
                externalized,
                "CASE_AND_ACCENT.INSENSITIVITY",
                LikeFlavour.NOT_SUPPORTED
            );
        } else if (flagSet.contains(RegularExpressionFlag.ACCENT_INSENSITIVE)) {
            isLikeToSqlClause(
                conn,
                columnName,
                clauseValues,
                clause,
                externalized,
                "ACCENT.INSENSITIVITY",
                LikeFlavour.NOT_SUPPORTED
            );
        } else if (flagSet.contains(RegularExpressionFlag.CASE_INSENSITIVE)) {
            isLikeToSqlClause(
                conn,
                columnName,
                clauseValues,
                clause,
                externalized,
                "CASE.INSENSITIVITY",
                LikeFlavour.LOWER_SQL
            );
        } else if (flagSet.contains(RegularExpressionFlag.POSIX_EXPRESSION)) {
            isLikeToSqlClause(
                conn,
                columnName,
                clauseValues,
                clause,
                externalized,
                "POSIX.EXPRESSION",
                LikeFlavour.NOT_SUPPORTED
            );
        } else if (flagSet.contains(RegularExpressionFlag.X_QUERY)) {
            throw new UnsupportedOperationException(
                "X_QUERY not yet supported"
            ); // TODO
        } else if (flagSet.contains(RegularExpressionFlag.JSON_QUERY)) {
            // @TODO hard-coded for PG. Better use LikeFlavour
            String operator = "@>";
            if (externalized.startsWith("?&") || externalized.startsWith("?|")
                || externalized.startsWith("@>")) {
                operator = externalized.substring(0, 2);
                externalized = externalized.substring(2);
            } else if (externalized.startsWith("?")) {
                operator = externalized.substring(0, 1);
                externalized = externalized.substring(1);
            }
            clause
                .append("(")
                .append(columnName)
                .append(" ")
                .append(operator)
                .append(" ? ")
                .append(")");
            clauseValues.add(externalized);
        } else {
            clause
                .append("(")
                .append(columnName)
                .append(" LIKE ? ")
                .append(getEscapeClause(conn))
                .append(")");
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
    )
        throws ServiceException {
        List<LikeFlavour> likeFlavours = LikeFlavour.parse(
            getDriverProperty(conn, propertyName, defaultFlavour.name())
        );
        LikeFlavour.applyAll(
            likeFlavours,
            clause,
            clauseValues,
            columnName,
            externalized
        );
    }

    /**
     * @param filterPropertyDef
     * @param referencedType
     * @return
     * @throws ServiceException
     */
    protected ModelElement_1_0 getReferenceType(
        ModelElement_1_0 filterPropertyDef
    )
        throws ServiceException {
        try {
            return filterPropertyDef
                .getModel()
                .getElementType(filterPropertyDef);
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Unable to retrieve the element type",
                new BasicException.Parameter(
                    "filterProperty",
                    filterPropertyDef.jdoGetObjectId()
                )
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
    )
        throws ServiceException {
        final Model_1_0 model = filterPropertyDef.getModel();
        final ModelElement_1_0 referencedType = getReferenceType(filterPropertyDef);
        final boolean joinWithState = false; // TODO to be evaluated and USED for non RID/OID DBs, too!
        String joinClauseBegin = null;
        String joinClauseEnd = null;
        String joinColumn = null;
        DbObject joinObject = null;
        // Reference
        final Path classicIdentityPattern = model.getIdentityPattern(referencedType, false);
        if (model.referenceIsStoredAsAttribute(filterPropertyDef)) {
            Path identityPattern = classicIdentityPattern;
            // Identity pattern may be null in case referenced type is an abstract
            // class. In this case try to get identity pattern for concrete subclasses
            // and assert that all identity patterns are equal and therefore are mapped
            // to the same DB object.
            if (identityPattern == null) {
                for (Object subtype : referencedType.objGetList("allSubtype")) {
                    ModelElement_1_0 subtypeDef = model.getElement(subtype);
                    identityPattern = model.getIdentityPattern(subtypeDef, false);
                    if (identityPattern != null) {
                        if (joinObject == null) {
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
                            if (!joinObject.getObjectIdColumn().equals(
                                joinObject2.getObjectIdColumn()
                            )
                                || !joinObject
                                    .getConfiguration()
                                    .matchesJoinCriteria(
                                        joinObject2.getConfiguration()
                                    )) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.NOT_SUPPORTED,
                                    "Join criteria for type is ambigous",
                                    new BasicException.Parameter(
                                        "type",
                                        referencedType
                                    ),
                                    new BasicException.Parameter(
                                        "joinObject.1",
                                        joinObject
                                    ),
                                    new BasicException.Parameter(
                                        "joinObject.2",
                                        joinObject2
                                    )
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
        } else if (ModelHelper.isCompositeEnd(filterPropertyDef, true)) {
            //
            // Composite parent
            //
            joinObject = this.getDbObject(
                conn,
                classicIdentityPattern,
                null,
                true
            );
            joinClauseBegin = viewAliasName + "."
                + this.getColumnName(
                    conn,
                    "parent",
                    0,
                    false, // indexSuffixIfZero,
                    false, // ignoreReservedWords
                    true // markAsPrivate
                );
            joinClauseEnd = "";
            joinColumn = joinObject.getObjectIdColumn().get(0);
        } else if (ModelHelper.isCompositeEnd(filterPropertyDef, false)) {
            //
            // Composite
            //
            if (classicIdentityPattern == null) {
                joinObject = this.getDbObject(
                    conn,
                    reference.getDescendant(":*", filterProperty.name()),
                    null,
                    true
                );
            } else {
                joinObject = this.getDbObject(
                    conn,
                    classicIdentityPattern,
                    null,
                    true
                );
            }
            // No fallback in case the parent and the referenced type are
            // abstract and root
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
        } else if (ModelHelper.isSharedEnd(filterPropertyDef, false)) {
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
            } catch (Exception ignore) {
                // Fallback to identity pattern if no pattern is configured for
                // access path
                SysLog.warning(
                    "No pattern configured for shared association. Fallback to identity pattern of referenced type",
                    reference.getDescendant(":*", filterProperty.name())
                );
                joinObject = this.getDbObject(
                    conn,
                    classicIdentityPattern,
                    null,
                    true
                );
            }
            if (joinObject.getJoinCriteria() != null
                && joinObject.getJoinCriteria().length == 3) {
                String[] joinCriteria = joinObject.getJoinCriteria();
                joinClauseBegin = viewAliasName + "." + dbObject.getObjectIdColumn().get(0)
                    + " IN (SELECT " + joinCriteria[1] + " FROM "
                    + joinCriteria[0] + " WHERE " + joinCriteria[2];
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
                new BasicException.Parameter(
                    "filter.definition",
                    filterPropertyDef
                )
            );
        }
        List<FilterProperty> allFilterProperties = FilterProperty.getFilterProperties(
            (QueryFilterRecord) filterProperty.getValue(0)
        );
        // Replace instance_of IN ... by object_class IN ...
        FilterProperty objectClassFilterProperty = null;
        for (Iterator<FilterProperty> i = allFilterProperties.iterator(); i
            .hasNext();) {
            FilterProperty p = i.next();
            if (SystemAttributes.OBJECT_INSTANCE_OF.equals(p.name())
                && !p.values().isEmpty()) {
                Set<String> allSubtypes = this.getAllSubtypes((String) p.getValue(0));
                if (allSubtypes != null) {
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
        if (objectClassFilterProperty != null) {
            allFilterProperties.add(objectClassFilterProperty);
        }
        List<FilterProperty> primaryFilterProperties = this
            .getPrimaryFilterProperties(referencedType, allFilterProperties);
        DbObjectConfiguration joinObjectConfiguration = joinObject.getConfiguration();
        String view1 = joinObjectConfiguration.getDbObjectForQuery1() == null
            ? joinObjectConfiguration.getDbObjectForUpdate1()
            : joinObjectConfiguration.getDbObjectForQuery1();
        String view2 = joinObjectConfiguration.getDbObjectForQuery2() == null
            ? joinObjectConfiguration.getDbObjectForUpdate2() == null ? view1
                : joinObjectConfiguration.getDbObjectForUpdate2()
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
            false, // stickyViewAlias
            referencedType,
            allFilterProperties,
            primaryFilterProperties,
            includingFilterClauses,
            includingFilterValues,
            excludingFilterClauses,
            excludingFilterValues
        );

        final Membership membership = new Membership(
            Quantifier.valueOf(filterProperty.quantor()),
            condition
        );
        clause.append(
            "(" + joinClauseBegin
                + (membership.isMember() ? " IN " : " NOT IN ") + "(SELECT "
                + joinColumn + " FROM " + view1 + " " + viewAliasName
                + "v WHERE " + (membership.isNegated() ? "(1=0)" : "(1=1)")
        );
        for (int i = 0; i < includingFilterClauses.size(); i++) {
            if (!includingFilterClauses.get(i).isEmpty()) {
                clause
                    .append(membership.isNegated() ? " OR NOT " : " AND ")
                    .append(includingFilterClauses.get(i));
                clauseValues.addAll(includingFilterValues.get(i));
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
        if (!excludingFilterClauses.isEmpty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Nested queries not supported for sliced tables",
                new BasicException.Parameter("filter.property", filterProperty),
                new BasicException.Parameter(
                    "filter.definition",
                    filterPropertyDef
                )
            );
        }
    }

    // ------------------------------------------------------------------------
    @Override
    public List<ModelElement_1_0> getFilterPropertyDefs(
        ModelElement_1_0 referencedTypeDef,
        List<FilterProperty> filterProperties
    )
        throws ServiceException {
        List<ModelElement_1_0> filterPropertyDefs = new ArrayList<ModelElement_1_0>();
        // try to determine multiplicity of filter properties in order to
        // optimize query
        Set<ModelElement_1_0> referencedTypes = null;
        try {
            // filter property can be an attribute of all super and subtypes of
            // referenced type
            referencedTypes = new HashSet<ModelElement_1_0>();
            // All subtypes and their supertypes of referenced type
            for (Iterator<Object> i = referencedTypeDef.objGetList("allSubtype").iterator(); i
                .hasNext();) {
                ModelElement_1_0 subtype = getModel().getElement(i.next());
                referencedTypes.add(subtype);
                for (Iterator<Object> j = subtype.objGetList("allSupertype").iterator(); j
                    .hasNext();) {
                    ModelElement_1_0 supertype = getModel().getElement(j.next());
                    referencedTypes.add(supertype);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        loopFilterProperties: for (FilterProperty p : filterProperties) {
            try {
                if (referencedTypes != null) {
                    // get filter property name and eliminate prefixes such as
                    // role_id, etc.
                    String filterPropertyName = p.name();
                    filterPropertyName = filterPropertyName
                        .substring(filterPropertyName.lastIndexOf('$') + 1);
                    // Qualified filter property name
                    if (filterPropertyName.indexOf(":") > 0) {
                        ModelElement_1_0 featureDef = getModel().findElement(filterPropertyName);
                        if (featureDef != null) {
                            filterPropertyDefs.add(featureDef);
                            continue loopFilterProperties;
                        }
                    }
                    // Non-qualified filter property name. Must look up.
                    else {
                        filterPropertyName = filterPropertyName
                            .substring(filterPropertyName.lastIndexOf(':') + 1);
                        // try to find filter property in any of the subtypes of
                        // referencedType
                        for (ModelElement_1_0 subtype : referencedTypes) {
                            String qualifiedFilterPropertyName = subtype.getQualifiedName() + ":"
                                + filterPropertyName;
                            ModelElement_1_0 featureDef = getModel()
                                .findElement(qualifiedFilterPropertyName);
                            if (featureDef != null) {
                                filterPropertyDefs.add(featureDef);
                                continue loopFilterProperties;
                            }
                        }
                    }
                    // No feature definition found for filter property
                    filterPropertyDefs.add(null);
                }
            } catch (ServiceException exception) {
                SysLog.warning(
                    "The following error occured when trying to determine multiplicity of filter property",
                    exception
                );
            }
        }
        return filterPropertyDefs;
    }

    // ------------------------------------------------------------------------
    @Override
    public List<FilterProperty> getPrimaryFilterProperties(
        ModelElement_1_0 referencedType,
        List<FilterProperty> filterProperties
    )
        throws ServiceException {
        List<FilterProperty> primaryFilterProperties = new ArrayList<FilterProperty>();
        List<ModelElement_1_0> filterPropertyDefs = this.getFilterPropertyDefs(referencedType, filterProperties);
        for (int i = 0; i < filterProperties.size(); i++) {
            FilterProperty filterProperty = filterProperties.get(i);
            ModelElement_1_0 filterPropertyDef = filterPropertyDefs.get(i);
            final boolean isPrimary;
            // Configured single-valued
            if (filterPropertyDef == null) {
                isPrimary = this.singleValueAttributes.contains(filterProperty.name());
            }
            // Associations for nested queries
            else if (filterPropertyDef.isReferenceType() && !this
                .getModel()
                .referenceIsStoredAsAttribute(filterPropertyDef)) {
                isPrimary = true;
            }
            // Embedded
            else if (Database_2.this
                .getEmbeddedFeature(filterProperty.name()) != null) {
                isPrimary = true;
            }
            // Single-valued
            else {
                isPrimary = ModelHelper
                    .getMultiplicity(filterPropertyDef)
                    .isSingleValued();
            }
            if (isPrimary) {
                primaryFilterProperties.add(filterProperty);
            }
        }
        return primaryFilterProperties;
    }

    @Override
    public final void removePrivateAttributes(ObjectRecord object)
        throws ServiceException {
        this.removeAttributes(
            object,
            true, // removPrivate
            false, // removeNonPersistent
            true // removeSize
        );
    }

    @Override
    public void removeAttributes(
        ObjectRecord object,
        boolean removePrivate,
        boolean removeNonPersistent,
        boolean removeSize
    )
        throws ServiceException {
        Object_2Facade facade = Facades.asObject(object);
        MappedRecord value = facade.getValue();
        ModelElement_1_0 classifierDef = null;
        classifierDef = this.getModel().findElement(value.getRecordName());
        if (classifierDef == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "No classifier definition found",
                new BasicException.Parameter("object", value)
            );
        }
        for (Iterator<String> i = facade.getValue().keySet().iterator(); i
            .hasNext();) {
            String attributeName = i.next();
            if (attributeName.startsWith(getPrivateAttributesPrefix())) {
                //
                // Private attributes
                //
                if (removePrivate) {
                    i.remove();
                }
            } else if (attributeName.endsWith(SIZE_SUFFIX)) {
                //
                // Size attributes
                //
                if (removeSize) {
                    i.remove();
                }
            } else if (!SystemAttributes.OBJECT_CLASS.equals(attributeName)) {
                //
                // Application attributes
                //
                ModelElement_1_0 featureDef;
                if (classifierDef == null) {
                    featureDef = null;
                } else {
                    featureDef = classifierDef.getModel().getFeatureDef(
                        classifierDef,
                        attributeName,
                        true // includeSubtypes
                    );
                    if (featureDef == null) {
                        SysLog.log(
                            Level.FINE,
                            "Sys|No feature definition found|{0}#{1}",
                            value.getRecordName(),
                            attributeName
                        );
                    }
                }
                if (featureDef == null
                    ? !SYSTEM_ATTRIBUTES.contains(attributeName)
                    : (removeNonPersistent && !this.isPersistent(featureDef))) {
                    i.remove();
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    protected void setLockAssertion(
        Object_2Facade facade,
        String versionAttribute
    )
        throws ServiceException {
        StringBuilder lockAssertion = new StringBuilder(versionAttribute).append('=');
        Object versionValue = facade.attributeValue(versionAttribute);
        if (versionValue != null) {
            lockAssertion.append(versionValue);
        }
        facade.setVersion(
            UnicodeTransformation.toByteArray(lockAssertion.toString())
        );
    }

    // ------------------------------------------------------------------------
    protected final void setLockAssertion(MappedRecord object)
        throws ServiceException {
        Object_2Facade facade = Facades.asObject(object);
        String objectClass = facade.getObjectClass();
        Model_1_0 model = getModel();
        if (BasicStates.isStated(objectClass) && isNotExcludedFromPersistency(
            "org:openmdx:base:Removable:removedAt"
        )) {
            setLockAssertion(facade, SystemAttributes.REMOVED_AT);
        } else if (model
            .isSubtypeOf(objectClass, "org:openmdx:base:Modifiable")) {
            setLockAssertion(facade, SystemAttributes.MODIFIED_AT);
        }
    }

    @Override
    public void completeObject(ObjectRecord object)
        throws ServiceException {
        this.removePrivateAttributes(object);
        this.setLockAssertion(object);
        DateTimeValues.normalizeDateTimeValues(object);
    }

    @Override
    public DbObject createDbObject(
        Connection conn,
        DbObjectConfiguration dbObjectConfiguration,
        Path accessPath,
        boolean isQuery
    )
        throws ServiceException {
        return this.getDbObject(
            conn,
            dbObjectConfiguration,
            accessPath,
            null,
            isQuery
        );
    }

    @Override
    public DbObject createDbObject(
        Connection conn,
        Path accessPath,
        boolean isQuery
    )
        throws ServiceException {
        return this.getDbObject(conn, null, accessPath, null, isQuery);
    }

    @Override
    public DbObject getDbObject(
        Connection conn,
        Path accessPath,
        List<FilterProperty> filter,
        boolean isQuery
    )
        throws ServiceException {
        return this.getDbObject(conn, null, accessPath, filter, isQuery);
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
    )
        throws ServiceException {
        DbObjectConfiguration configuration = dbObjectConfiguration;
        Path adjustedAccessPath = accessPath;
        boolean isExtent = false;

        // extent access requires special treatment
        // get the set of referenceIds specified by filter property 'identity'
        if (filter != null && accessPath.isLike(EXTENT_REFERENCES)) {
            for (FilterProperty p : filter) {
                if (SystemAttributes.OBJECT_IDENTITY.equals(p.name())) {
                    if (p.values().size() > 1) {
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
            if (!isExtent) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "extent lookups require at least a filter value for property 'identity'",
                    new BasicException.Parameter("filter", filter)
                );
            }
        }

        if (configuration == null) {
            Path referencedType = adjustedAccessPath.size() % 2 == 0
                ? adjustedAccessPath
                : adjustedAccessPath.getParent();
            configuration = this.getDatabaseConfiguration().getDbObjectConfiguration(
                referencedType
            );
        }

        if (LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED
            .equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if (LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2
            .equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if (LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_NON_INDEXED
            .equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObjectNonIndexed(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if (LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2_NON_INDEXED
            .equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2NonIndexed(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if (LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_PARENT_RID_ONLY
            .equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObjectParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if (LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2_PARENT_RID_ONLY
            .equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2ParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if (LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_NON_INDEXED_PARENT_RID_ONLY
            .equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObjectNonIndexedParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if (LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2_NON_INDEXED_PARENT_RID_ONLY
            .equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2NonIndexedParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if (LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_WITH_ID_AS_KEY
            .equals(configuration.getDbObjectFormat())) {
            return new DBOSlicedWithIdAsKey(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        } else if (LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_WITH_PARENT_AND_ID_AS_KEY
            .equals(configuration.getDbObjectFormat())) {
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
                return Classes
                    .<DbObject>getApplicationClass(
                        configuration.getDbObjectFormat()
                    )
                    .getConstructor(
                        Database_1_0.class,
                        Connection.class,
                        DbObjectConfiguration.class,
                        Path.class,
                        boolean.class,
                        boolean.class
                    )
                    .newInstance(
                        this,
                        conn,
                        configuration,
                        adjustedAccessPath,
                        Boolean.valueOf(isExtent),
                        Boolean.valueOf(isQuery)
                    );
            } catch (Exception exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE,
                    "can not create DbObject",
                    new BasicException.Parameter("path", adjustedAccessPath),
                    new BasicException.Parameter(
                        "type",
                        configuration.getDbObjectFormat()
                    )
                );
            }
        }
    }

    /**
     * Remove view prefix 'v.' from column names in clause
     */
    @Override
    public String removeViewPrefix(String _clause) {
        String clause = _clause;
        int pos = 0;
        while ((pos = clause.indexOf("v.")) >= 0) {
            clause = clause.substring(0, pos) + clause.substring(pos + 2);
        }
        return clause;
    }

    @Override
    public Connection getConnection(
        RestInteractionSpec ispec,
        RequestRecord request
    )
        throws ServiceException,
        SQLException {
        return this.getDataSource(ispec, request).getConnection();
    }

    @Override
    public PreparedStatement prepareStatement(
        Connection conn,
        String statement
    )
        throws SQLException {
        return Database_2.this.prepareStatement(conn, statement, false);
    }

    protected ObjectRecord getPartialObject(
        Connection conn,
        Path path,
        short attributeSelector,
        Map<String, AttributeSpecifier> attributeSpecifiers,
        boolean objectClassAsAttribute,
        DbObject dbObject,
        boolean primaryColumns,
        String objectClass,
        boolean throwNotFoundException
    )
        throws ServiceException {
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
            boolean useReferenceClause = (referenceClause.indexOf(objectIdClause) < 0)
                && (referenceClause.indexOf(
                    objectIdClause.replaceAll("= \\?", "> \\?")
                ) < 0)
                && (referenceClause.indexOf(
                    objectIdClause.replaceAll("= \\?", "< \\?")
                ) < 0);
            if (primaryColumns) {
                // Optimize reference clause in case the objectIdClause is more
                // restrictive than the reference clause.
                statement = view1.startsWith("SELECT") ? view1 + " AND "
                    : "SELECT " + dbObject.getHint() + " * FROM " + view1
                        + " v WHERE ";
                statement += "(" + (useReferenceClause ? referenceClause + " AND " : "")
                    + objectIdClause + ")";
                prefix = "v.";
            }
            // Secondary columns
            else {
                if (dbObject
                    .getConfiguration()
                    .getDbObjectsForQueryJoinColumn() == null) {
                    statement = view1.startsWith("SELECT") ? view2 + " AND "
                        : "SELECT " + dbObject.getHint() + " * FROM " + view2
                            + " v WHERE ";
                    statement += "("
                        + (useReferenceClause ? referenceClause + " AND " : "")
                        + objectIdClause + ")";
                    prefix = "v.";
                } else {
                    statement = view1.startsWith("SELECT")
                        ? "SELECT " + dbObject.getHint() + " vm.* FROM ("
                            + view2 + ") vm"
                        : "SELECT " + dbObject.getHint() + " vm.* FROM " + view2
                            + " vm";
                    statement += " INNER JOIN ";
                    statement += view1.startsWith("SELECT")
                        ? "(" + view1 + ") v"
                        : view1 + " v";
                    statement += " ON vm." + OBJECT_OID + " = v." + dbObject
                        .getConfiguration()
                        .getDbObjectsForQueryJoinColumn();
                    statement += " WHERE ";
                    statement += "("
                        + (useReferenceClause ? referenceClause + " AND " : "")
                        + objectIdClause + ")";
                    prefix = "vm.";
                }
            }
            // ORDER BY required on secondary db object and on primary if
            // it contains indexed object slices
            if (primaryColumns) {
                if (dbObject.getIndexColumn() != null) {
                    statement += " ORDER BY " + prefix + dbObject.getIndexColumn();
                }
            } else {
                statement += " ORDER BY " + prefix + OBJECT_IDX;
            }
            ps = this.prepareStatement(conn, currentStatement = statement);
            statementParameters = new ArrayList<Object>();
            int pos = 1;
            if (useReferenceClause) {
                List<Object> referenceValues = dbObject.getReferenceValues();
                statementParameters.addAll(referenceValues);
                for (Object referenceValue : referenceValues) {
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
            for (Object objectIdValue : objectIdValues) {
                Database_2.this
                    .setPreparedStatementValue(conn, ps, pos++, objectIdValue);
            }
            rs = Database_2.this.executeQuery(
                ps,
                currentStatement,
                statementParameters,
                0 // no limit for maxRows
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
                primaryColumns, // check for valid identity only when primary
                // columns are fetched
                throwNotFoundException
            );
            return replyObj;
        } catch (SQLException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE,
                null,
                new BasicException.Parameter("xri", path),
                new BasicException.Parameter("statement", currentStatement),
                new BasicException.Parameter("parameters", statementParameters),
                new BasicException.Parameter(
                    "sqlErrorCode",
                    exception.getErrorCode()
                ),
                new BasicException.Parameter(
                    "sqlState",
                    exception.getSQLState()
                )
            );
        } finally {
            Closeables.close(rs);
            Closeables.close(ps);
        }
    }

    @Override
    public void get(
        Connection conn,
        RestInteractionSpec ispec,
        Path path,
        short attributeSelector,
        Map<String, AttributeSpecifier> attributeSpecifiers,
        boolean objectClassAsAttribute,
        Target reply,
        boolean throwNotFoundException
    )
        throws ServiceException {
        try {
            DbObject dbObject = Database_2.this.createDbObject(conn, path, true);
            // Get primary attributes
            boolean hasSecondaryDbObject = (dbObject.getConfiguration().getDbObjectForQuery2() != null)
                || (dbObject
                    .getConfiguration()
                    .getDbObjectForUpdate2() != null);
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
            if (replyObj != null) {
                // Get attributes from secondary db object
                if (hasSecondaryDbObject) {
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
                    if (replyObj2 != null) {
                        Object_2Facade.getValue(replyObj2).keySet().removeAll(
                            Object_2Facade.getValue(replyObj).keySet()
                        );
                        Object_2Facade.getValue(replyObj).putAll(
                            Object_2Facade.getValue(replyObj2)
                        );
                    }
                }
                reply.accept(replyObj);
            }
        } catch (ServiceException exception) {
            if (exception
                .getCause()
                .getExceptionCode() == BasicException.Code.NOT_FOUND) {
                if (throwNotFoundException) {
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
        } catch (NullPointerException exception) {
            throw new ServiceException(exception).log();
        } catch (RuntimeException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Explicit test for duplicates.
     */
    private void checkForDuplicates(
        Connection conn,
        RestInteractionSpec ispec,
        ObjectRecord request
    )
        throws ServiceException {
        try {
            SingletonTarget reply = new SingletonTarget(null);
            this.get(
                conn,
                ispec,
                request.getResourceIdentifier(),
                AttributeSelectors.ALL_ATTRIBUTES,
                Collections.<String, AttributeSpecifier>emptyMap(),
                false, // objectClassAsAttribute
                reply,
                false // throwNotFoundException
            );
            if (!reply.isSaturated())
                return;
        } catch (ServiceException exception) {
            if (exception.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                return;
            } else {
                throw exception;
            }
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.DUPLICATE,
            "duplicate object",
            new BasicException.Parameter(
                "path",
                request.getResourceIdentifier()
            )
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
    ) {
        if (BasicException.Code.DEFAULT_DOMAIN
            .equals(exception.getExceptionDomain())
            && BasicException.Code.INVALID_CARDINALITY == exception
                .getExceptionCode()
            && exception.getMessage().indexOf("TRY_TO_FORGET") >= 0) {
            MappedRecord slice0 = Object_2Facade.getValue(partitionedObjects[0]);
            for (int i = 1; i < partitionedObjects.length; i++) {
                MappedRecord sliceI = Object_2Facade.getValue(partitionedObjects[i]);
                for (Object featureName : sliceI.keySet()) {
                    if (!"objectIdx".equals(featureName)) {
                        Object value0 = slice0.get(featureName);
                        Object valueI = sliceI.get(featureName);
                        if (value0 == null ? valueI != null
                            : !value0.equals(valueI)) {
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
    @Override
    public void create(
        Connection conn,
        RestInteractionSpec ispec,
        ObjectRecord object,
        ResultRecord reply
    )
        throws ServiceException {
        try {
            if (this.ignoreCheckForDuplicates) {
                //
                // WARNING: the implementation assumes that
                // 'unique constraints' are set in order that INSERTs throw a
                // 'duplicate row'
                // exception which is then mapped to a DUPLICATE
                // ServiceException.
                //
            } else {
                this.checkForDuplicates(conn, ispec, object);
            }
            // Partition object into slices and create all slices
            DbObject dbObject = this
                .createDbObject(conn, object.getResourceIdentifier(), false);
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
                for (int i = 0; i < partitionedObjects.length; i++) {
                    dbObject = this.createDbObject(
                        conn,
                        partitionedObjects[i].getResourceIdentifier(),
                        false
                    );
                    dbObject.createObjectSlice(i, type, partitionedObjects[i]);
                }
            } catch (ServiceException exception) {
                if (isRecoverable(exception, partitionedObjects)) {
                    SysLog.warning(
                        "Recovering from invalid cardinality by ignoring equal values",
                        object
                    );
                    exception.log();
                } else {
                    throw exception;
                }
            }
            if (reply != null) {
                reply.add(object);
            }
        } catch (NullPointerException exception) {
            throw new ServiceException(exception).log();
        } catch (RuntimeException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Create a temporary file to determine a large object's length
     * 
     * @return a new temporary file
     * 
     * @throws IOException
     * 
     *             @deprecated("For JRE 5/setStreamByValue support only")
     */
    @Deprecated
    private File newTemporaryFile()
        throws IOException {
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
     *             @deprecated("For JRE 5/setStreamByValue support only")
     */
    @Deprecated
    protected BinaryLargeObject tallyLargeObject(InputStream stream)
        throws IOException {
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
        return BinaryLargeObjects
            .valueOf(new FileInputStream(file), Long.valueOf(length));
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
     *             @deprecated("For JRE 5/setStreamByValue support only")
     */
    @Deprecated
    protected CharacterLargeObject tallyLargeObject(Reader stream)
        throws IOException {
        File file = newTemporaryFile();
        Writer target = new OutputStreamWriter(new FileOutputStream(file), "UTF-16");
        long length;
        try {
            length = CharacterLargeObjects.streamCopy(stream, 0, target);
        } finally {
            stream.close();
            target.flush();
            target.close();
        }
        return CharacterLargeObjects.valueOf(
            new InputStreamReader(new FileInputStream(file), "UTF-16"),
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
     * @throws ServiceException
     *             if meta data retrieval fails
     */
    public String getDateTimeType(Connection connection)
        throws ServiceException {
        return LayerConfigurationEntries.DATETIME_TYPE_STANDARD
            .equals(this.dateTimeType)
                ? getDriverProperty(
                    connection,
                    "DATETIME.TYPE.STANDARD",
                    LayerConfigurationEntries.DATETIME_TYPE_CHARACTER
                )
                : this.dateTimeType;
    }

    /**
     * The escape clause
     * 
     * @param connection
     * 
     * @return the escape clause
     * 
     * @throws ServiceException
     */
    @Override
    public String getEscapeClause(Connection connection)
        throws ServiceException {
        return getDriverProperty(
            connection, "ESCAPE.CLAUSE", "" // "ESCAPE
                                                                     // '\\'"
        );
    }

    /**
     * Add the escape character '\\' to an equal value
     * 
     * @param equalValue
     * 
     * @return the corresponding like value
     */
    public static String escape(String equalValue) {
        return equalValue.replaceAll("([_%\\\\])", "\\\\$1");
    }

    /**
     * Remove the escape character '\\' from a like value
     * 
     * @param likeValue
     * 
     * @return the corresponding equals value
     */
    public static String unescape(String likeValue) {
        return likeValue.indexOf('\\') < 0 ? likeValue
            : likeValue.replaceAll("\\\\([_%\\\\])", "$1");
    }

    /**
     * Retrieves the configured date type
     * 
     * @param connection
     * 
     * @return the date type to be used
     * 
     * @throws ServiceException
     *             if meta data retrieval fails
     */
    public String getDateType(Connection connection)
        throws ServiceException {
        return LayerConfigurationEntries.DATE_TYPE_STANDARD
            .equals(this.dateType)
                ? getDriverProperty(
                    connection,
                    "DATE.TYPE.STANDARD",
                    LayerConfigurationEntries.DATETIME_TYPE_CHARACTER
                )
                : this.dateType;
    }

    /**
     * Retrieves the configured time type
     * 
     * @param connection
     * 
     * @return the time type to be used
     * 
     * @throws ServiceException
     *             if meta data retrieval fails
     */
    public String getTimeType(Connection connection)
        throws ServiceException {
        return LayerConfigurationEntries.TIME_TYPE_STANDARD
            .equals(this.timeType)
                ? getDriverProperty(
                    connection,
                    "TIME.TYPE.STANDARD",
                    LayerConfigurationEntries.TIME_TYPE_CHARACTER
                )
                : this.timeType;
    }

    /**
     * Retrieves the configured boolean type
     * 
     * @param connection
     * 
     * @return the boolean type to be used
     * 
     * @throws ServiceException
     *             if meta data retrieval fails
     */
    public String getBooleanType(Connection connection)
        throws ServiceException {
        return LayerConfigurationEntries.BOOLEAN_TYPE_STANDARD
            .equals(this.booleanType)
                ? getDriverProperty(
                    connection,
                    "BOOLEAN.TYPE.STANDARD",
                    LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER
                )
                : this.booleanType;
    }

    private SetLargeObjectMethod howToSetLargeObject(
        Connection connection,
        String id
    ) throws ServiceException {
        return SetLargeObjectMethod.valueOf(
            getDriverProperty(
                connection,
                id,
                SetLargeObjectMethod.SET_STREAM.name()
            )
        );
    }

    @Override
    public OrderAmendment getOrderAmendment(
        Connection connection,
        DbObject dbObject
    )
        throws ServiceException {
        if (dbObject.getIndexColumn() == null) {
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

    public SetLargeObjectMethod howToSetBinaryLargeObject(Connection connection)
        throws ServiceException {
        return howToSetLargeObject(connection, "SET.BINARY.LARGE.OBJECT");
    }

    public SetLargeObjectMethod howToSetCharacterLargeObject(
        Connection connection
    )
        throws ServiceException {
        return howToSetLargeObject(connection, "SET.CHARACTER.LARGE.OBJECT");
    }

    /**
     * Retrieve the version
     * 
     * @param a
     *            PUT request
     * 
     * @return the version
     */
    protected static byte[] getVersion(ObjectRecord request) {
        return (byte[]) request.getVersion();
    }

    /**
     * Build the lock assertion
     * 
     * @param version
     *            an encoded lock assertion
     * 
     * @return the decoded lock assertion
     * 
     * @throws ServiceException
     */
    @Override
    public String toWriteLock(Object version)
        throws ServiceException {
        if (version instanceof byte[]) {
            byte[] writeLock = (byte[]) version;
            return writeLock.length == 0 ? null
                : UnicodeTransformation.toString(
                    writeLock,
                    0, // offset
                    writeLock.length
                );
        } else if (version == null) {
            return null;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Unsupported write lock class",
                new BasicException.Parameter(
                    "expected",
                    byte[].class.getName()
                ),
                new BasicException.Parameter(
                    "actual",
                    version.getClass().getName()
                )
            );
        }
    }

    /**
     * Build the read lock assertion
     * 
     * @param lock
     *            a lock assertion
     * 
     * @return the decoded lock assertion
     * 
     * @throws ServiceException
     */
    @Override
    public String toReadLock(Object lock)
        throws ServiceException {
        if (lock instanceof String) {
            String readLock = (String) lock;
            return readLock.length() == 0 ? null : readLock;
        } else if (lock == null) {
            return null;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Unsupported write lock class",
                new BasicException.Parameter(
                    "expected",
                    String.class.getName()
                ),
                new BasicException.Parameter(
                    "actual",
                    lock.getClass().getName()
                )
            );
        }
    }

    /**
     * Tells whether the class denotes a base class not to be checked in the
     * select clause as they shall never be put into the same table with other
     * classes not matching this criteria.
     * 
     * @param qualifiedClassName
     * 
     * @return <code>true</code> if the class shall not be checked
     */
    protected boolean isBaseClass(String qualifiedClassName) {
        return BASE_CLASSES.contains(qualifiedClassName);
    }

    @Override
    public boolean isEmbeddedFeature(String featureName) {
        return embeddedFeatures.containsKey(featureName);
    }

    // ---------------------------------------------------------------------------
    // Variables
    // ---------------------------------------------------------------------------

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

    protected SparseArray<String> columnNameFrom = SortedMaps.emptySparseArray();

    protected SparseArray<String> columnNameTo = SortedMaps.emptySparseArray();

    protected SparseArray<Path> type = SortedMaps.emptySparseArray();

    protected SparseArray<String> typeName = SortedMaps.emptySparseArray();

    protected SparseArray<String> dbObject = SortedMaps.emptySparseArray();

    protected SparseArray<String> dbObject2 = SortedMaps.emptySparseArray();

    protected SparseArray<String> dbObjectFormat = SortedMaps.emptySparseArray();

    protected SparseArray<Integer> pathNormalizeLevel = SortedMaps.emptySparseArray();

    protected SparseArray<String> dbObjectForQuery = SortedMaps.emptySparseArray();

    protected SparseArray<String> dbObjectForQuery2 = SortedMaps.emptySparseArray();

    protected SparseArray<String> dbObjectsForQueryJoinColumn = SortedMaps.emptySparseArray();

    protected SparseArray<String> dbObjectHint = SortedMaps.emptySparseArray();

    protected SparseArray<String> objectIdPattern = SortedMaps.emptySparseArray();

    protected SparseArray<String> joinTable = SortedMaps.emptySparseArray();

    protected SparseArray<String> joinColumnEnd1 = SortedMaps.emptySparseArray();

    protected SparseArray<String> joinColumnEnd2 = SortedMaps.emptySparseArray();

    protected SparseArray<String> unitOfWorkProvider = SortedMaps.emptySparseArray();

    protected SparseArray<String> removableReferenceIdPrefix = SortedMaps.emptySparseArray();

    protected SparseArray<Boolean> disableAbsolutePositioning = SortedMaps.emptySparseArray();

    protected SparseArray<String> referenceIdPattern = SortedMaps.emptySparseArray();

    protected SparseArray<String> autonumColumn = SortedMaps.emptySparseArray();

    protected boolean getLargeObjectByValue = true;

    // VIEW_MODE
    protected static final short VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY = 0;

    protected static final short VIEW_MODE_SECONDARY_COLUMNS = 1;

    protected static final Set<String> SYSTEM_ATTRIBUTES = Sets.asSet(
        SystemAttributes.OBJECT_CLASS,
        SystemAttributes.CREATED_AT,
        SystemAttributes.CREATED_BY,
        SystemAttributes.MODIFIED_AT,
        SystemAttributes.MODIFIED_BY,
        SystemAttributes.REMOVED_AT,
        SystemAttributes.REMOVED_BY,
        TechnicalAttributes.STATE_VERSION
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
    protected static final Set<String> RESERVED_WORDS_HSQLDB = Sets.asSet("position", "POSITION");

    protected static final Set<String> RESERVED_WORDS_ORACLE = Sets.asSet("resource", "RESOURCE", "comment", "COMMENT");

    // JDO case insensitive flag
    protected static final String JDO_CASE_INSENSITIVE_FLAG = "(?i)";

    /**
     * maximum rows allowed in generic search. There are as many rows as maximum
     * number of values of attributes in filters and the number of attributes in
     * specifiers
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
     * Allows to ignore the check-for-duplicate test on object creation. By
     * default, the persistence plugin asserts that no two objects with the same
     * identity are created.
     */
    protected boolean ignoreCheckForDuplicates;

    /**
     * configured single-valued filter properties. Allows optimization of find
     * operations
     */
    protected Set<String> singleValueAttributes = new HashSet<String>(
        Arrays.asList(SystemAttributes.OBJECT_CLASS, "object_stateId")
    );

    /**
     * Configured embedded features. The elements are stored as {key=embedded
     * feature name, value=upper bound}
     */
    protected Map<String, Integer> embeddedFeatures = new TreeMap<String, Integer>();

    /**
     * macros
     */
    protected SparseArray<String> stringMacroColumn = SortedMaps.emptySparseArray();
    protected SparseArray<String> stringMacroName = SortedMaps.emptySparseArray();
    protected SparseArray<String> stringMacroValue = SortedMaps.emptySparseArray();
    protected SparseArray<String> pathMacroName = SortedMaps.emptySparseArray();
    protected SparseArray<String> pathMacroValue = SortedMaps.emptySparseArray();

    // referenceId format
    private String referenceIdFormat = LayerConfigurationEntries.REFERENCE_ID_FORMAT_TYPE_WITH_PATH_COMPONENTS;

    // useNormalizedReferences. If true, all DB operations are performed on the
    // normalized values of object references, i.e. rid, oid.
    private boolean useNormalizedReferences = false;

    /**
     * If set to true the size of multi-valued attributes stored in column C is
     * stored in column C_.
     */
    private boolean setSizeColumns = false;

    /**
     * <columnName,attributeName> mapping. Calculating attribute names from
     * column names and vice versa is expensive. Therefore it is cached.
     */
    protected final ConcurrentMap<String, String> featureNames = new ConcurrentHashMap<String, String>();

    protected final ConcurrentMap<String, String> publicColumnNames = new ConcurrentHashMap<String, String>();

    protected final ConcurrentMap<String, String> privateColumnNames = new ConcurrentHashMap<String, String>();

    protected final ConcurrentMap<String, String> databaseProductNames = new ConcurrentHashMap<String, String>();

    private Set<String> nonPersistentFeatures = Collections.emptySet();

    /**
     * nullAsCharacter defines the string used to produce a type-safe NULL
     * string, e.g. CAST(NULL AS CHARACTER)
     */
    protected String nullAsCharacter = "NULL";

    /**
     * This value is used as result set size if a fetch size of
     * {@link javax.jdo.FetchPlan.FETCH_SIZE_OPTIMAL} (0) has been
     * requested and more than optimal fetch size object records are
     * available.
     */
    private int optimalFetchSize = 100;

    /**
     * The configuration rowBatchSize defines how many rows are
     * returned at once to the database result set. A value of 0, the
     * default, lets the database driver guess an optimal number.
     */
    private int rowBatchSize = 0;

    /**
     * The configuration objectBatchSize defines for how many objects the
     * multi-valued attributes are retrieved at once'. Defaults to 100
     */
    private int objectBatchSize = 100;

    /**
     * A TOO_LARGE_RESULT_SET exception is thrown if the result set is larger
     * than result set limit and either
     * {@link javax.jdo.FetchPlan.FETCH_SIZE_GREEDY} (-1) or a fetch size
     * greater than the result set limit has been requested.
     */
    protected int resultSetLimit = 10000;

    /**
     * Tells whether state filter substitution is enabled or disabled.
     */
    protected boolean enableAspectFilterSubstitution = true;

    /**
     * technical column names: object_oid, object_rid, object_idx (may be used
     * by DbObject implementations if required)
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
     * It's usually unwise the use these classes in select statement
     */
    private static final Collection<String> BASE_CLASSES = Arrays.asList(
        "org:openmdx:base:BasicObject",
        "org:openmdx:base:ExtentCapable"
    );

    /**
     * @deprecated("For JRE 5/setStreamByValue support only")
     */
    @Deprecated
    protected File streamBufferDirectory;

    /**
     * The large object marshaller instance
     */
    private LargeObjectMarshaller largeObjectMarshaller;

    /**
     * The macro configuration is eagerly provided, but it's handler lazily
     */
    private final MacroConfiguration macroConfiguration = new ClassicMacroConfiguration(
        () -> getStringMacroColumn(),
        () -> getStringMacroName(),
        () -> getStringMacroValue(),
        () -> getPathMacroName(),
        () -> getPathMacroValue()
    );

}
