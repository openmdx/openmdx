/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Database_1
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.persistence.jdbc;

import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.jdo.FetchPlan;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.sql.DataSource;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.kernel.LateBindingDataSource;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.dataprovider.spi.OperationAwareLayer_1;
import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.Database_2;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.w3c.cci2.SparseArray;

/**
 * Database_1 implements a OO-to-Relational mapping and makes MappedRecords
 * persistent. Any JDBC-compliant data store can be used.
 * 
 * You can use the java.io.PrintStream.DriverManager.setLogStream() method to
 * log JDBC calls. This method sets the logging/tracing PrintStream used by
 * the DriverManager and all drivers.
 * Insert the following line at the location in your code where you want to
 * start logging JDBC calls: DriverManager.setLogStream(System.out);
 * 
 */
public class Database_1 extends OperationAwareLayer_1 {

    protected Database_2 database;
    
    /**
     * Cache the data sources in the configuration and return them
     * 
     * @param configuration
     * 
     * @return the data sources
     */
    @SuppressWarnings("deprecation")
    private SparseArray<DataSource> getDatasources(
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
     * Create new delegate.
     * 
     * @return
     */
    protected Database_2 newDelegate(
    ) {
        return new Database_2();
    }

    /**
     * Get delegate.
     * 
     * @return
     */
    public Database_2 getDelegate(
    ) {
        return this.database;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.spi.OperationAwareLayer_1#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#activate(short, org.openmdx.application.configuration.Configuration, org.openmdx.application.dataprovider.spi.Layer_1)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1 delegation
    ) throws ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        this.database = newDelegate();
        this.database.setBooleanType(this.getConfigurationValue(
            LayerConfigurationEntries.BOOLEAN_TYPE,
            LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER
        ));
        this.database.setBooleanTrue(this.getConfigurationValue(
            LayerConfigurationEntries.BOOLEAN_TRUE,
            null
        ));
        this.database.setBooleanFalse(this.getConfigurationValue(
            LayerConfigurationEntries.BOOLEAN_FALSE,
            null
        ));
        this.database.setDurationType(this.getConfigurationValue(
            LayerConfigurationEntries.DURATION_TYPE,
            LayerConfigurationEntries.DURATION_TYPE_CHARACTER
        ));
        this.database.setDateTimeZone(this.getConfigurationValue(
            LayerConfigurationEntries.DATETIME_TIMEZONE,
            TimeZone.getDefault().getID()
        ));
        this.database.setDateTimeDaylightZone(this.getConfigurationValue(
            LayerConfigurationEntries.DATETIME_DST_TIMEZONE,
            this.database.getDateTimeZone()
        ));
        this.database.setTimeType(this.getConfigurationValue(
            LayerConfigurationEntries.TIME_TYPE,
            LayerConfigurationEntries.TIME_TYPE_CHARACTER
        ));
        this.database.setDateType(this.getConfigurationValue(
            LayerConfigurationEntries.DATE_TYPE,
            LayerConfigurationEntries.DATE_TYPE_CHARACTER
        ));
        this.database.setDateTimeType(this.getConfigurationValue(
            LayerConfigurationEntries.DATETIME_TYPE,
            LayerConfigurationEntries.DATETIME_TYPE_CHARACTER
        ));
        this.database.setDateTimePrecision(getConfigurationValue(
            LayerConfigurationEntries.DATE_TIME_PRECISION,
            TimeUnit.MICROSECONDS.name()
        ));
        this.database.setNamespaceId(configuration.getFirstValue(
            SharedConfigurationEntries.NAMESPACE_ID
        ));
        this.database.setObjectIdAttributesSuffix( getConfigurationValue(
            LayerConfigurationEntries.OBJECT_ID_ATTRIBUTES_SUFFIX,
            Database_2.DEFAULT_OID_SUFFIX
        ));
        this.database.setReferenceIdAttributesSuffix(getConfigurationValue(
            LayerConfigurationEntries.REFERENCE_ID_ATTRIBUTES_SUFFIX,
            Database_2.DEFAULT_RID_SUFFIX
        ));
        this.database.setReferenceIdSuffixAttributesSuffix(getConfigurationValue(
            LayerConfigurationEntries.REFERENCE_ID_SUFFIX_ATTRIBUTES_SUFFIX,
            Database_2.DEFAULT_RID_SUFFIX
        ));
        this.database.setPrivateAttributesPrefix(getConfigurationValue(
            LayerConfigurationEntries.PRIVATE_ATTRIBUTES_PREFIX,
            Database_2.DEFAULT_PRIVATE_ATTRIBUTE_PREFIX
        ).toLowerCase());
        this.database.setObjectIdxColumn(this.getConfigurationValue(
            LayerConfigurationEntries.OBJECT_IDX_COLUMN,
            this.database.toIdx("object")
        ));
        if(!configuration.values(LayerConfigurationEntries.RESULT_SET_TYPE).isEmpty()) {
            this.database.setResultSetType(configuration.getFirstValue(LayerConfigurationEntries.RESULT_SET_TYPE));
        }
        this.database.setAllowsSqlSequenceFallback(configuration.isEnabled(LayerConfigurationEntries.ALLOWS_SQL_SEQUENCE_FALLBACK, false));
        this.database.setIgnoreCheckForDuplicates(configuration.isEnabled(LayerConfigurationEntries.IGNORE_CHECK_FOR_DUPLICATES, false));
        if(!configuration.values(LayerConfigurationEntries.SINGLE_VALUE_ATTRIBUTE).isEmpty()) {
            this.database.setSingleValueAttribute(configuration.<String>values(LayerConfigurationEntries.SINGLE_VALUE_ATTRIBUTE));
        }
        if(!configuration.values(LayerConfigurationEntries.EMBEDDED_FEATURE).isEmpty()) {
            this.database.setEmbeddedFeature(configuration.<String>values(LayerConfigurationEntries.EMBEDDED_FEATURE));
        }
        if(!configuration.<String>values(LayerConfigurationEntries.NON_PERSISTENT_FEATURE).isEmpty()) {
            this.database.setNonPersistentFeature(configuration.<String>values(LayerConfigurationEntries.NON_PERSISTENT_FEATURE));
        }
        this.database.setNullAsCharacter(getConfigurationValue(
            LayerConfigurationEntries.NULL_AS_CHARACTER,
            "NULL"
        ));
        this.database.setFetchSize(getConfigurationValue(LayerConfigurationEntries.FETCH_SIZE, FetchPlan.FETCH_SIZE_OPTIMAL));
        this.database.setFetchSizeOptimal(getConfigurationValue(LayerConfigurationEntries.FETCH_SIZE_OPTIMAL, 32));
        this.database.setFetchSizeGreedy(getConfigurationValue(LayerConfigurationEntries.FETCH_SIZE_GREEDY, 1024));
        this.database.setReferenceLookupStatementHint(getConfigurationValue(
            LayerConfigurationEntries.REFERENCE_LOOKUP_STATEMENT_HINT,
            ""
        ));
        this.database.setReferenceIdFormat(getConfigurationValue(
            LayerConfigurationEntries.REFERENCE_ID_FORMAT,
            LayerConfigurationEntries.REFERENCE_ID_FORMAT_REF_TABLE
        ));
        this.database.setUseNormalizedReferences(configuration.isEnabled(LayerConfigurationEntries.USE_NORMALIZED_REFERENCES, false));
        this.database.setNormalizeObjectIds(configuration.isEnabled(LayerConfigurationEntries.USE_NORMALIZED_OBJECT_IDS, false));
        this.database.setSetSizeColumns(configuration.isEnabled(LayerConfigurationEntries.SET_SIZE_COLUMNS, false));
        this.database.setPathMacroName(configuration.<String>values(LayerConfigurationEntries.PATH_MACRO_NAME));
        this.database.setPathMacroValue(configuration.<String>values(LayerConfigurationEntries.PATH_MACRO_VALUE));
        this.database.setStringMacroColumn(configuration.<String>values(LayerConfigurationEntries.STRING_MACRO_COLUMN));
        this.database.setStringMacroName(configuration.<String>values(LayerConfigurationEntries.STRING_MACRO_NAME));
        this.database.setStringMacroValue(configuration.<String>values(LayerConfigurationEntries.STRING_MACRO_VALUE));
        this.database.setMaxReferenceComponents(getConfigurationValue(
            LayerConfigurationEntries.MAX_REFERENCE_COMPONENTS,
            16
        ));
        this.database.setDataSource(getDatasources(configuration));
        this.database.setDisableStateFilterSubstitution(configuration.isEnabled(LayerConfigurationEntries.DISABLE_STATE_FILTER_SUBSTITUATION, false));
        this.database.setGetLargeObjectByValue(configuration.isEnabled(LayerConfigurationEntries.GET_LARGE_OBJECT_BY_VALUE, true));
        this.database.setCascadeDeletes(configuration.isEnabled(LayerConfigurationEntries.CASCADE_DELETES, true));
        this.database.setColumnNameFrom(configuration.<String>values(LayerConfigurationEntries.COLUMN_NAME_FROM));
        this.database.setColumnNameTo(configuration.<String>values(LayerConfigurationEntries.COLUMN_NAME_TO));
        SparseArray<Path> types = new TreeSparseArray<Path>();
        for(ListIterator<Object> i = configuration.<Object>values(LayerConfigurationEntries.TYPE).populationIterator(); i.hasNext(); ) {
            int index = i.nextIndex();
            Object type = i.next();
            types.put(
                Integer.valueOf(index),
                type instanceof String ? new Path((String)type) : (Path)type
            );
        }
        this.database.setType(types);
        this.database.setTypeName(configuration.<String>values(LayerConfigurationEntries.TYPE_NAME));
        this.database.setDbObject(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT));
        this.database.setDbObject2(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT_2));
        this.database.setDbObjectFormat(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT_FORMAT));
        this.database.setPathNormalizeLevel(configuration.<Integer>values(LayerConfigurationEntries.PATH_NORMALIZE_LEVEL));
        this.database.setDbObjectForQuery(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT_FOR_QUERY));
        this.database.setDbObjectForQuery2(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT_FOR_QUERY_2));
        this.database.setDbObjectsForQueryJoinColumn(configuration.<String>values(LayerConfigurationEntries.DB_OBJECTS_FOR_QUERY_JOIN_COLUMN));
        this.database.setDbObjectHint(configuration.<String>values(LayerConfigurationEntries.DB_OBJECT_HINT));
        this.database.setObjectIdPattern(configuration.<String>values(LayerConfigurationEntries.OBJECT_ID_PATTERN));
        this.database.setJoinTable(configuration.<String>values(LayerConfigurationEntries.JOIN_TABLE));
        this.database.setJoinColumnEnd1(configuration.<String>values(LayerConfigurationEntries.JOIN_COLUMN_END1));
        this.database.setJoinColumnEnd2(configuration.<String>values(LayerConfigurationEntries.JOIN_COLUMN_END2));
        this.database.setUnitOfWorkProvider(configuration.<String>values(LayerConfigurationEntries.UNIT_OF_WORK_PROVIDER));
        this.database.setRemovableReferenceIdPrefix(configuration.<String>values(LayerConfigurationEntries.REMOVABLE_REFERENCE_ID_PREFIX));
        this.database.setDisableAbsolutePositioning(configuration.<Boolean>values(LayerConfigurationEntries.DISABLE_ABSOLUTE_POSITIONING));
        this.database.setReferenceIdPattern(configuration.<String>values(LayerConfigurationEntries.REFERENCE_ID_PATTERN));
        this.database.setAutonumColumn(configuration.<String>values(LayerConfigurationEntries.AUTONUM_COLUMN));
    }

    /**
     * LayerInteraction
     *
     */
    public class LayerInteraction extends OperationAwareLayer_1.LayerInteraction {

        private final Database_2.RestInteraction delegate;
        
        public LayerInteraction(
            RestConnection connection
        ) throws ResourceException {
            super(connection);
            this.delegate = (Database_2.RestInteraction)Database_1.this.database.getInteraction(connection);
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            try {
                return this.delegate.get(
                    ispec, 
                    input.getDelegate(), 
                    (ResultRecord)output
                );
            } catch(ResourceException e) {
                throw new ServiceException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#find(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            try {
                return this.delegate.find(
                    ispec, 
                    input.getDelegate(), 
                    (ResultRecord)output
                );
            } catch(ResourceException e) {
                throw new ServiceException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#create(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            try {
                return this.delegate.create(
                    ispec, 
                    input.getDelegate(), 
                    (ResultRecord)output
                );
            } catch(ResourceException e) {
                throw new ServiceException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            try {
                return this.delegate.delete(
                    ispec, 
                    input.getDelegate()
                );
            } catch(ResourceException e) {
                throw new ServiceException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#put(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            try {
                return this.delegate.update(
                    ispec, 
                    input.getDelegate(), 
                    (ResultRecord)output
                );
            } catch(ResourceException e) {
                throw new ServiceException(e);
            }
        }

    }

}
