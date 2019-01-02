/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Database_2Configuration 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2017, OMEX AG, Switzerland
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

import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.jdo.FetchPlan;
import javax.sql.DataSource;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.kernel.LateBindingDataSource;
import org.openmdx.application.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries;
import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.SparseArray;

/**
 * Database_2Configuration
 *
 */
@SuppressWarnings("deprecation")
public class Database_2Configuration {

    /**
     * Cache the data sources in the configuration and return them
     * 
     * @param configuration
     * 
     * @return the data sources
     */
    private static SparseArray<DataSource> getDatasources(
        Configuration configuration
    ) {
        final SparseArray<DataSource> dataSources = configuration.values(
            SharedConfigurationEntries.DATABASE_CONNECTION_FACTORY
        );
        if(dataSources.isEmpty()) {
            synchronized(dataSources) {
                if(dataSources.isEmpty()) {
                    final SparseArray<?>  dataSourceNames = configuration.values(
                        SharedConfigurationEntries.DATABASE_CONNECTION_FACTORY_NAME
                    );
                    for(Map.Entry<Integer, ?> entry : dataSourceNames.entrySet()) {
                        String jndiName = (String) entry.getValue();
                        dataSources.put(
                            entry.getKey(), 
                            new LateBindingDataSource(jndiName)
                        );
                    }
                }
            }
        }
        return dataSources.isEmpty() 
            ? new TreeSparseArray<DataSource>() 
            : new TreeSparseArray<DataSource>(dataSources);
    }

    /**
     * Retrieves a configuration value
     * 
     * @param source
     * @param key
     * @param defaultValue
     * 
     * @return the configuration value or its default
     */
    private static String getConfigurationValue(
        Configuration configuration,
        String key,
        String defaultValue
    ){
        return configuration.containsEntry(key) && !configuration.values(key).isEmpty() ?
            ((String)configuration.values(key).get(Integer.valueOf(0))) :
                defaultValue;
    }

    /**
     * Retrieves a configuration value
     * 
     * @param source
     * @param key
     * @param defaultValue
     * 
     * @return the configuration value or its default
     */
    private static int getConfigurationValue(
       Configuration configuration,
        String key,
        int defaultValue
    ){
        if(configuration.containsEntry(key)) {
            SparseArray<Object> values = configuration.values(key);
            if(!values.isEmpty()) {
                Object value = values.get(Integer.valueOf(0));
                if(value instanceof Number) {
                    return ((Number)value).intValue();
                } else if (value instanceof String){
                    return Integer.parseInt((String)value);
                } else if (value != null) {
                    throw BasicException.initHolder(
                        new IllegalArgumentException(
                            "Inappropriate configuration value",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter("value", value),
                                new BasicException.Parameter("supported", String.class.getName(), Integer.class.getName()),
                                new BasicException.Parameter("value", value.getClass().getName())
                            )
                        )
                    );
                }
            }
        }
        return  defaultValue;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#activate(short, org.openmdx.application.configuration.Configuration, org.openmdx.application.dataprovider.spi.Layer_1)
     */
    public static void activate(
        Database_2 database,
        Configuration configuration
    ) throws ServiceException {
        database.setBooleanType(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.BOOLEAN_TYPE,
            LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER
        ));
        database.setBooleanTrue(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.BOOLEAN_TRUE,
            null
        ));
        database.setBooleanFalse(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.BOOLEAN_FALSE,
            null
        ));
        database.setDurationType(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.DURATION_TYPE,
            LayerConfigurationEntries.DURATION_TYPE_CHARACTER
        ));
        database.setDateTimeZone(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.DATETIME_TIMEZONE,
            TimeZone.getDefault().getID()
        ));
        database.setDateTimeDaylightZone(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.DATETIME_DST_TIMEZONE,
            database.getDateTimeZone()
        ));
        database.setTimeType(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.TIME_TYPE,
            LayerConfigurationEntries.TIME_TYPE_CHARACTER
        ));
        database.setDateType(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.DATE_TYPE,
            LayerConfigurationEntries.DATE_TYPE_CHARACTER
        ));
        database.setDateTimeType(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.DATETIME_TYPE,
            LayerConfigurationEntries.DATETIME_TYPE_CHARACTER
        ));
        database.setDateTimePrecision(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.DATE_TIME_PRECISION,
            TimeUnit.MICROSECONDS.name()
        ));
        database.setNamespaceId(configuration.getFirstValue(
            SharedConfigurationEntries.NAMESPACE_ID
        ));
        database.setObjectIdAttributesSuffix( getConfigurationValue(
            configuration,
            LayerConfigurationEntries.OBJECT_ID_ATTRIBUTES_SUFFIX,
            Database_2.DEFAULT_OID_SUFFIX
        ));
        database.setReferenceIdAttributesSuffix(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.REFERENCE_ID_ATTRIBUTES_SUFFIX,
            Database_2.DEFAULT_RID_SUFFIX
        ));
        database.setReferenceIdSuffixAttributesSuffix(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.REFERENCE_ID_SUFFIX_ATTRIBUTES_SUFFIX,
            Database_2.DEFAULT_RID_SUFFIX
        ));
        database.setPrivateAttributesPrefix(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.PRIVATE_ATTRIBUTES_PREFIX,
            Database_2.DEFAULT_PRIVATE_ATTRIBUTE_PREFIX
        ).toLowerCase());
        database.setObjectIdxColumn(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.OBJECT_IDX_COLUMN,
            database.toIdx("object")
        ));
        if(!configuration.values(LayerConfigurationEntries.RESULT_SET_TYPE).isEmpty()) {
            database.setResultSetType(configuration.getFirstValue(LayerConfigurationEntries.RESULT_SET_TYPE));
        }
        database.setAllowsSqlSequenceFallback(configuration.isEnabled(LayerConfigurationEntries.ALLOWS_SQL_SEQUENCE_FALLBACK, false));
        database.setIgnoreCheckForDuplicates(configuration.isEnabled(LayerConfigurationEntries.IGNORE_CHECK_FOR_DUPLICATES, false));
        if(!configuration.values(LayerConfigurationEntries.SINGLE_VALUE_ATTRIBUTE).isEmpty()) {
            database.setSingleValueAttribute(configuration.<String>values(LayerConfigurationEntries.SINGLE_VALUE_ATTRIBUTE));
        }
        if(!configuration.values(LayerConfigurationEntries.EMBEDDED_FEATURE).isEmpty()) {
            database.setEmbeddedFeature(configuration.<String>values(LayerConfigurationEntries.EMBEDDED_FEATURE));
        }
        if(!configuration.<String>values(LayerConfigurationEntries.NON_PERSISTENT_FEATURE).isEmpty()) {
            database.setNonPersistentFeature(configuration.<String>values(LayerConfigurationEntries.NON_PERSISTENT_FEATURE));
        }
        database.setNullAsCharacter(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.NULL_AS_CHARACTER,
            "NULL"
        ));
        database.setFetchSize(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.FETCH_SIZE,
            FetchPlan.FETCH_SIZE_OPTIMAL
        ));
        database.setFetchSizeOptimal(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.FETCH_SIZE_OPTIMAL,
            32
        ));
        database.setFetchSizeGreedy(getConfigurationValue(
            configuration,
            LayerConfigurationEntries.FETCH_SIZE_GREEDY,
            1024
        ));
        database.setUseNormalizedReferences(configuration.isEnabled(LayerConfigurationEntries.USE_NORMALIZED_REFERENCES, false));
        database.setNormalizeObjectIds(configuration.isEnabled(LayerConfigurationEntries.USE_NORMALIZED_OBJECT_IDS, false));
        database.setSetSizeColumns(configuration.isEnabled(LayerConfigurationEntries.SET_SIZE_COLUMNS, false));
        database.setPathMacroName(configuration.<String>values(LayerConfigurationEntries.PATH_MACRO_NAME));
        database.setPathMacroValue(configuration.<String>values(LayerConfigurationEntries.PATH_MACRO_VALUE));
        database.setStringMacroColumn(configuration.<String>values(LayerConfigurationEntries.STRING_MACRO_COLUMN));
        database.setStringMacroName(configuration.<String>values(LayerConfigurationEntries.STRING_MACRO_NAME));
        database.setStringMacroValue(configuration.<String>values(LayerConfigurationEntries.STRING_MACRO_VALUE));
        database.setDataSource(getDatasources(configuration));
        database.setDisableStateFilterSubstitution(configuration.isEnabled(LayerConfigurationEntries.DISABLE_STATE_FILTER_SUBSTITUATION, false));
        database.setGetLargeObjectByValue(configuration.isEnabled(LayerConfigurationEntries.GET_LARGE_OBJECT_BY_VALUE, true));
        database.setCascadeDeletes(configuration.isEnabled(LayerConfigurationEntries.CASCADE_DELETES, true));
        database.setColumnNameFrom(configuration.<String>values(LayerConfigurationEntries.COLUMN_NAME_FROM));
        database.setColumnNameTo(configuration.<String>values(LayerConfigurationEntries.COLUMN_NAME_TO));
        SparseArray<Path> types = new TreeSparseArray<Path>();
        for(ListIterator<Object> i = configuration.<Object>values(LayerConfigurationEntries.TYPE).populationIterator(); i.hasNext(); ) {
            int index = i.nextIndex();
            Object type = i.next();
            types.put(
                Integer.valueOf(index),
                type instanceof String ? new Path((String)type) : (Path)type
            );
        }
        database.setType(types);
        database.setTypeName(configuration.<String>values(LayerConfigurationEntries.TYPE_NAME));
        database.setDbObject(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT));
        database.setDbObject2(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT_2));
        database.setDbObjectFormat(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT_FORMAT));
        database.setPathNormalizeLevel(configuration.<Integer>values(LayerConfigurationEntries.PATH_NORMALIZE_LEVEL));
        database.setDbObjectForQuery(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT_FOR_QUERY));
        database.setDbObjectForQuery2(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT_FOR_QUERY_2));
        database.setDbObjectsForQueryJoinColumn(configuration.<String>values(LayerConfigurationEntries.DB_OBJECTS_FOR_QUERY_JOIN_COLUMN));
        database.setDbObjectHint(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT_HINT));
        database.setObjectIdPattern(configuration.<String>values(LayerConfigurationEntries.OBJECT_ID_PATTERN));
        database.setJoinTable(configuration.<String>values(LayerConfigurationEntries.JOIN_TABLE));
        database.setJoinColumnEnd1(configuration.<String>values(LayerConfigurationEntries.JOIN_COLUMN_END1));
        database.setJoinColumnEnd2(configuration.<String>values(LayerConfigurationEntries.JOIN_COLUMN_END2));
        database.setUnitOfWorkProvider(configuration.<String>values(LayerConfigurationEntries.UNIT_OF_WORK_PROVIDER));
        database.setRemovableReferenceIdPrefix(configuration.<String>values(LayerConfigurationEntries.REMOVABLE_REFERENCE_ID_PREFIX));
        database.setDisableAbsolutePositioning(configuration.<Boolean>values(LayerConfigurationEntries.DISABLE_ABSOLUTE_POSITIONING));
        database.setReferenceIdPattern(configuration.<String>values(LayerConfigurationEntries.REFERENCE_ID_PATTERN));
        database.setAutonumColumn(configuration.<String>values(LayerConfigurationEntries.AUTONUM_COLUMN));
        database.setOrderNullsAsEmpty(configuration.isEnabled(LayerConfigurationEntries.ORDER_NULLS_AS_EMPTY, false));
    }
    
}
