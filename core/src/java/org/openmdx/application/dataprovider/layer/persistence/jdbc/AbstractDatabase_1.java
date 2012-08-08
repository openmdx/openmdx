/*
 * ====================================================================
 * Name:        $Id: AbstractDatabase_1.java,v 1.145 2012/01/07 01:37:43 hburger Exp $
 * Description: AbstractDatabase_1 plugin
 * Revision:    $Revision: 1.145 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2012/01/07 01:37:43 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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

import static org.openmdx.base.accessor.cci.SystemAttributes.OBJECT_CLASS;
import static org.openmdx.base.accessor.cci.SystemAttributes.OBJECT_INSTANCE_OF;
import static org.openmdx.base.naming.SpecialResourceIdentifiers.EXTENT_REFERENCES;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.layer.persistence.common.AbstractPersistence_1;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.dataprovider.spi.OperationAwareLayer_1;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.spi.URIMarshaller;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.Persistency;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.URI_1;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.SQLWildcards;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.kernel.url.protocol.XRI_1Protocols;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.SparseArray;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;

//---------------------------------------------------------------------------
/**
 * Database_1 implements a OO-to-Relational mapping and makes MappedRecords
 * persistent. Any JDBC-compliant data store can be used.
 * 
 * You can use the java.io.PrintStream.DriverManager.setLogStream() method to
 * log JDBC calls. This method sets the logging/tracing PrintStream used by
 * the DriverManager and all drivers.
 * Insert the following line at the location in your code where you want to
 * start logging JDBC calls: DriverManager.setLogStream(System.out);
 */
@SuppressWarnings({"rawtypes","unchecked"})
abstract public class AbstractDatabase_1 extends AbstractPersistence_1 implements DataTypes {
    
    // --------------------------------------------------------------------------
    @Override
    public Interaction getInteraction(
        javax.resource.cci.Connection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }
        
    //---------------------------------------------------------------------------
    abstract int resultSetGetRow(
        ResultSet rs
    ) throws SQLException;

    //---------------------------------------------------------------------------
    abstract void resultSetUpdateLong(
        ResultSet rs,
        String columnName,
        long value
    ) throws SQLException;

    //---------------------------------------------------------------------------
    abstract void resultSetUpdateInt(
        ResultSet rs,
        String columnName,
        int value
    ) throws SQLException;

    //---------------------------------------------------------------------------
    abstract void resultSetUpdateString(
        ResultSet rs,
        String columnName,
        String value
    ) throws SQLException;

    //---------------------------------------------------------------------------
    abstract void resultSetUpdateRow(
        ResultSet rs
    ) throws SQLException;

    //---------------------------------------------------------------------------
    /**
     * Sets row count to object at given position. The implementation can optimize
     * positioning if lastRowCount != -1 and lastPosition != -1 and the driver/
     * database support absolute positioning. The object at lastPosition starts
     * at row lastRowCount. 
     */
    abstract FastResultSet setPosition(
        ResultSet rs,
        int position,
        int lastPosition,
        int lastRowCount,
        boolean isIndexed, 
        boolean absolutePositioningDisabled
    ) throws ServiceException, SQLException;

    //---------------------------------------------------------------------------
    abstract PreparedStatement prepareStatement(
        Connection conn,
        String statement,
        boolean updatable
    ) throws SQLException;

    //---------------------------------------------------------------------------
    @Override
    public void activate(
        short id, 
        Configuration configuration,
        Layer_1 delegation
    ) throws ServiceException {

        SysLog.detail(
            "activating", 
            "$Id: AbstractDatabase_1.java,v 1.145 2012/01/07 01:37:43 hburger Exp $"
        );

        super.activate( 
            id, 
            configuration, 
            delegation
        );

        // Boolean marshaller
        this.booleanType = getConfigurationValue(
            LayerConfigurationEntries.BOOLEAN_TYPE,
            LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER
        );
        this.booleanMarshaller = BooleanMarshaller.newInstance(
            this.getConfigurationValue(
                LayerConfigurationEntries.BOOLEAN_FALSE,
                null
            ),
            this.getConfigurationValue(
                LayerConfigurationEntries.BOOLEAN_TRUE,
                null
            ),
            this
        );

        // durationType
        this.durationMarshaller = DurationMarshaller.newInstance(
            getConfigurationValue(
                LayerConfigurationEntries.DURATION_TYPE,
                LayerConfigurationEntries.DURATION_TYPE_CHARACTER
            )
        );


        // XMLGregorianCalendar
        this.calendarMarshaller = XMLGregorianCalendarMarshaller.newInstance(
            this.timeType = getConfigurationValue(
                LayerConfigurationEntries.TIME_TYPE,
                LayerConfigurationEntries.TIME_TYPE_CHARACTER
            ),
            this.dateType = getConfigurationValue(
                LayerConfigurationEntries.DATE_TYPE,
                LayerConfigurationEntries.DATE_TYPE_CHARACTER
            ),
            this.dateTimeType = getConfigurationValue(
                LayerConfigurationEntries.DATETIME_TYPE,
                LayerConfigurationEntries.DATETIME_TYPE_CHARACTER
            ),         
            getConfigurationValue(
                LayerConfigurationEntries.DATETIME_TIMEZONE,
                TimeZone.getDefault().getID()
            ), 
            getConfigurationValue(
                LayerConfigurationEntries.DATE_TIME_PRECISION,
                TimeUnit.MICROSECONDS.name()
            ), 
            this
        );

        // namespaceId
        this.namespaceId = configuration.getFirstValue(
            SharedConfigurationEntries.NAMESPACE_ID
        );

        // objectIdAttributesSuffix
        this.objectIdAttributesSuffix = getConfigurationValue(
            LayerConfigurationEntries.OBJECT_ID_ATTRIBUTES_SUFFIX,
            DEFAULT_OID_SUFFIX
        );

        // referenceIdAttributesSuffix    
        this.referenceIdAttributesSuffix = getConfigurationValue(
            LayerConfigurationEntries.REFERENCE_ID_ATTRIBUTES_SUFFIX,
            DEFAULT_RID_SUFFIX
        );

        // referenceIdAttributesSuffix    
        this.referenceIdSuffixAttributesSuffix = getConfigurationValue(
            LayerConfigurationEntries.REFERENCE_ID_SUFFIX_ATTRIBUTES_SUFFIX,
            DEFAULT_RID_SUFFIX
        );
        
        // privateAttributesPrefix
        this.privateAttributesPrefix = getConfigurationValue(
            LayerConfigurationEntries.PRIVATE_ATTRIBUTES_PREFIX,
            DEFAULT_PRIVATE_ATTRIBUTE_PREFIX
        ).toLowerCase();

        OBJECT_OID = this.toOid("object");
        OBJECT_IDX = this.getConfigurationValue(
            LayerConfigurationEntries.OBJECT_IDX_COLUMN,
            this.toIdx("object")
        );
        OBJECT_RID = this.toRid("object");
        OBJECT_ID = this.toId("object");

        // Result set type
        this.resultSetType = ResultSet.TYPE_FORWARD_ONLY;
        if(!configuration.values(LayerConfigurationEntries.RESULT_SET_TYPE).isEmpty()) {
            String type = configuration.getFirstValue(LayerConfigurationEntries.RESULT_SET_TYPE);
            if(LayerConfigurationEntries.RESULT_SET_TYPE_FORWARD_ONLY.equals(type)) {
                this.resultSetType = ResultSet.TYPE_FORWARD_ONLY;
            }
            else if(LayerConfigurationEntries.RESULT_SET_TYPE_SCROLL_INSENSITIVE.equals(type)) {
                this.resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }
            else if(LayerConfigurationEntries.RESULT_SET_TYPE_SCROLL_SENSITIVE.equals(type)) {
                this.resultSetType = ResultSet.TYPE_SCROLL_SENSITIVE;
            }
            SysLog.info("resultSetType", type);
        }

        // allowsSqlSequenceFallback 
        this.allowsSqlSequenceFallback = configuration.isOn(LayerConfigurationEntries.ALLOWS_SQL_SEQUENCE_FALLBACK);

        // ignoreCheckForDuplicates 
        this.ignoreCheckForDuplicates = configuration.isOn(LayerConfigurationEntries.IGNORE_CHECK_FOR_DUPLICATES);

        // single-valued attributes
        this.singleValueAttributes = new HashSet();
        this.singleValueAttributes.add(SystemAttributes.OBJECT_CLASS);
        this.singleValueAttributes.add("object_stateId");
        if(!configuration.values(LayerConfigurationEntries.SINGLE_VALUE_ATTRIBUTE).isEmpty()) {
            this.singleValueAttributes.addAll(
                configuration.values(LayerConfigurationEntries.SINGLE_VALUE_ATTRIBUTE).values()
            );
        }

        // Embedded features
        this.embeddedFeatures = new TreeMap();
        if(!configuration.values(LayerConfigurationEntries.EMBEDDED_FEATURE).isEmpty()) {
            for(String embeddedFeature : configuration.<String>values(LayerConfigurationEntries.EMBEDDED_FEATURE)) {
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

        // Non-persistent features
        for(String nonPersistentFeature : configuration.<String>values(LayerConfigurationEntries.NON_PERSISTENT_FEATURE).values()) {
        	this.nonPersistentFeatures.add(nonPersistentFeature);
        }

        // nullAsCharacter
        this.nullAsCharacter = getConfigurationValue(
            LayerConfigurationEntries.NULL_AS_CHARACTER,
            "NULL"
        );

        // fetchSize
        this.fetchSize = getConfigurationValue(
            LayerConfigurationEntries.FETCH_SIZE,
            100
        );

        // referenceLookupStatementHint
        this.referenceLookupStatementHint = getConfigurationValue(
            LayerConfigurationEntries.REFERENCE_LOOKUP_STATEMENT_HINT,
            ""
        );

        // referenceIdFormat
        this.referenceIdFormat = getConfigurationValue(
            LayerConfigurationEntries.REFERENCE_ID_FORMAT,
            LayerConfigurationEntries.REFERENCE_ID_FORMAT_REF_TABLE
        );

        // useNormalizedReferences
        this.useNormalizedReferences = configuration.isOn(LayerConfigurationEntries.USE_NORMALIZED_REFERENCES);

        // useNormalizedReferences
        this.setSizeColumns = configuration.isOn(LayerConfigurationEntries.SET_SIZE_COLUMNS);

        // PATH_MACRO_NAME, PATH_MACRO_VALUE
        this.pathMacros = new HashMap<String,String>();
        for(
            ListIterator<String> pathMacroNameIterator = configuration.<String>values(LayerConfigurationEntries.PATH_MACRO_NAME).populationIterator();
            pathMacroNameIterator.hasNext();
        ){
            int i = pathMacroNameIterator.nextIndex();
            String pathMacroName = pathMacroNameIterator.next();
            this.pathMacros.put(
                pathMacroName,
                (String)configuration.values(LayerConfigurationEntries.PATH_MACRO_VALUE).get(i)
            );
        }

        // STRING_MACRO_COLUMN, STRING_MACRO_NAME, STRING_MACRO_VALUE
        this.stringMacros = new HashMap<String,List<String[]>>();
        for(
            ListIterator<String> stringMacroColumnIterator = configuration.<String>values(LayerConfigurationEntries.STRING_MACRO_COLUMN).populationIterator();
            stringMacroColumnIterator.hasNext();
        ){
            int i = stringMacroColumnIterator.nextIndex();
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
                    configuration.<String>values(LayerConfigurationEntries.STRING_MACRO_NAME).get(i),
                    configuration.<String>values(LayerConfigurationEntries.STRING_MACRO_VALUE).get(i)
                }       
            );
        }

        // maxReferenceColumns
        this.maxReferenceComponents = getConfigurationValue(
            LayerConfigurationEntries.MAX_REFERENCE_COMPONENTS,
            16
        );

        // connection
        this.dataSources = new ArrayList(
            configuration.values(
                SharedConfigurationEntries.DATABASE_CONNECTION_FACTORY
            ).values()
        );

        // driver properties
        this.jdbcDriverSqlProperties = new Properties();
        URL propertiesUrl = Classes.getApplicationResource(JDBC_DRIVER_SQL_PROPERTIES);
        if(propertiesUrl == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION, 
                "Loading resource '" + JDBC_DRIVER_SQL_PROPERTIES + "'failed"
            );
        } 
        else try {
            this.jdbcDriverSqlProperties.load(
                propertiesUrl.openStream()
            );
        } 
        catch (IOException e) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION, 
                "Loading resource '" + JDBC_DRIVER_SQL_PROPERTIES + "'failed"
            );
        }     

        this.enableStateFilterSubstitution = !configuration.isOn(
            LayerConfigurationEntries.DISABLE_STATE_FILTER_SUBSTITUATION
        );
        this.configuration = new DatabaseConfiguration(
            this.namespaceId,
            this.referenceIdFormat,
            this.useNormalizedReferences(),
            configuration.isOn(LayerConfigurationEntries.USE_NORMALIZED_OBJECT_IDS),
            configuration.isNotDisabled(LayerConfigurationEntries.USE_VIEWS_FOR_REDUNDANT_COLUMNS),
            configuration.isNotDisabled(LayerConfigurationEntries.USE_PREFERENCES_TABLE), 
            configuration.isNotDisabled(LayerConfigurationEntries.CASCADE_DELETES), 
            // The database configuration is always retrieved from the first configured connection manager
            this.dataSources.isEmpty() ? null : this.dataSources.get(0), 
            configuration
        );
    }

    //---------------------------------------------------------------------------
    /**
     * Return connection manager. The default implementation returns the connection
     * manager this.connectionManagers at index 0. A user-defined implementation of 
     * getConnectionManager() can return a manager from any of the configured connection 
     * managers according to a criteria derived from the request info.
     */
    protected DataSource getDataSource(
        DataproviderRequest request
    ) throws ServiceException {
        if(this.dataSources.isEmpty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION, 
                "can not get connection manager"
            );
        }
        return this.dataSources.get(0);
    }

    //---------------------------------------------------------------------------
    protected String toRid(
        String name
    ) {
        return name + this.referenceIdAttributesSuffix;
    }

    //---------------------------------------------------------------------------
    protected String toRsx(
        String name
    ){
        return name + this.referenceIdSuffixAttributesSuffix;
    }
    
    //---------------------------------------------------------------------------
    protected String toOid(
        String name
    ) {
        return name + this.objectIdAttributesSuffix;
    }

    //---------------------------------------------------------------------------
    String toId(
        String name
    ) {
        return name + "_id";
    }

    //---------------------------------------------------------------------------
    String toIdx(
        String name
    ) {
        return name + "_idx";
    }

    //---------------------------------------------------------------------------
    public String getReferenceIdFormat(
    ) {
        return this.referenceIdFormat;
    }

    /**
     * Tells whether reference tables shall be used
     * 
     * @return <code>true</code> if reference tables are used
     */
    protected boolean useReferenceTables(){
        return LayerConfigurationEntries.REFERENCE_ID_FORMAT_REF_TABLE.equals(this.getReferenceIdFormat());
    }
    
    //---------------------------------------------------------------------------
    public boolean useNormalizedReferences(
    ) {
        return this.useNormalizedReferences;
    }

    //---------------------------------------------------------------------------
    public boolean isSetSizeColumns(
    ) {
        return this.setSizeColumns;
    }

    //---------------------------------------------------------------------------
    public String getPrivateAttributesPrefix() {
        return this.privateAttributesPrefix;
    }

    //---------------------------------------------------------------------------
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
            List<String[]> stringReplacements = this.stringMacros.get(columnName);
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

    //---------------------------------------------------------------------------
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
        List<String[]> stringReplacements = this.stringMacros.get(columnName);
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

    //---------------------------------------------------------------------------
    /**
     * Externalizes a Path value. The externalized value is stored on the database.
     * Also applies the path macros and returns the shortened path. For compatibility 
     * reasons returns URI notation if no macro replacement was applied, else XRI 
     * notation.
     */
    public String externalizePathValue(
        Connection conn,
        Path source
    ) throws ServiceException {
        if(this.useNormalizedReferences()) {
            return this.configuration.normalizeObjectIds() ? this.configuration.buildObjectId(
                source
            ) : this.getReferenceId(
                conn,
                source,
                false
            ) + "/" + source.getBase();
        }
        else {
            String converted = source.toXRI();
            boolean modified = false;
            for(Iterator<Entry<String,String>> i = this.pathMacros.entrySet().iterator(); i.hasNext(); ) {
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

    //---------------------------------------------------------------------------
    /**
     * Internalizes an external, stringified path value. Also applies the path 
     * macros to expand the path.
     * @throws ServiceException 
     */
    @SuppressWarnings("deprecation")
    public Object internalizePathValue(
        String source
    ) throws ServiceException {
        if(this.configuration.normalizeObjectIds()) {
            return this.configuration.buildResourceIdentifier(source, false);
        } else {
            String converted = source;
            boolean modified = false;
            for(Iterator<Entry<String,String>> i = this.pathMacros.entrySet().iterator(); i.hasNext(); ) {
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
            return modified
            ? convertedPath.toXri().equals(converted) ? (Object)convertedPath : (Object)converted
                : convertedPath.toUri().equals(converted) ? (Object)convertedPath : (Object)converted;
        }
    }

    //---------------------------------------------------------------------------
    public ResultSet executeQuery(
        PreparedStatement ps,
        String statement,
        List<?> statementParameters
    ) throws SQLException {
        ps.setFetchSize(this.fetchSize);
        ps.setFetchDirection(ResultSet.FETCH_FORWARD);
        SysLog.detail("statement", statement);
        SysLog.detail("parameters", statementParameters);
        long startTime = System.currentTimeMillis();
        ResultSet rs = ps.executeQuery();
        long duration = System.currentTimeMillis() - startTime;
        SysLog.detail("execution time", Long.valueOf(duration));
        return rs;
    }

    //---------------------------------------------------------------------------
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

    //---------------------------------------------------------------------------
    boolean supportsSqlNumericNullCast(
        Connection conn
    ) {
        if(this.supportsSqlNumericNullCast == null) {      
            PreparedStatement ps = null;
            try {
                ps = this.prepareStatement(
                    conn, 
                    /* currentStatement = */ "SELECT CAST(NULL AS NUMERIC) FROM " + this.namespaceId + "_" + T_REF
                );
                ps.executeQuery();
                this.supportsSqlNumericNullCast = Boolean.TRUE;
            }
            catch(SQLException e) {
                this.supportsSqlNumericNullCast = Boolean.FALSE;
            }
            finally {
                try {
                    if(ps != null) ps.close();
                } catch(Throwable ex) {
                    // ignore
                }
            }
        }
        return this.supportsSqlNumericNullCast.booleanValue();
    }

    //---------------------------------------------------------------------------
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
        boolean containsWildcard = false;
        for(String referencedType : dbObject.getReferencedType()) {
            if(":*".equals(referencedType)) {
                containsWildcard = true;
                break;
            }
        }
        if(!containsWildcard) {
            // Assert existence of reference id
            this.getReferenceId(
                conn,
                dbObject.getReference(),
                true
            );
        }
        return dbObjectForQuery;
    }

    //---------------------------------------------------------------------------
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

        String databaseProductName = "N/A";
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            databaseProductName = dbm.getDatabaseProductName();
        } catch(Exception e) {
            // ignore
        }
        if(typeConfiguration != null) {
            if(typeConfiguration.getDbObjectForQuery1() != null) {
                dbObjectForQuery1 = this.getDbObjectForQuery1(
                    conn,
                    dbObject,
                    dbObjectHint
                );            
            }
            else {
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
                                this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".STRCAT.PREFIX")
                            );
                        }
                        if(dbObjectForQuery.indexOf("STRCAT.INFIX") >= 0) {
                            dbObjectForQuery = Pattern.compile("STRCAT.INFIX").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".STRCAT.INFIX")
                            );
                        }
                        if(dbObjectForQuery.indexOf("STRCAT.SUFFIX") >= 0) {
                            dbObjectForQuery = Pattern.compile("STRCAT.SUFFIX").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".STRCAT.SUFFIX")
                            );
                        }
                        if(dbObjectForQuery.indexOf("NULL.NUMERIC") >= 0) {
                            dbObjectForQuery = Pattern.compile("NULL.NUMERIC").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".NULL.NUMERIC")
                            );
                        }
                        if(dbObjectForQuery.indexOf("NULL.CHARACTER") >= 0) {
                            dbObjectForQuery = Pattern.compile("NULL.CHARACTER").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(conn.getMetaData().getDatabaseProductName() + ".NULL.CHARACTER")
                            );
                        }
                        if(dbObjectForQuery.indexOf("CORREL.SUBQUERY.BEGIN") >= 0) {
                            dbObjectForQuery = Pattern.compile("CORREL.SUBQUERY.BEGIN").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".CORREL.SUBQUERY.BEGIN")
                            );
                        }
                        if(dbObjectForQuery.indexOf("CORREL.SUBQUERY.END") >= 0) {
                            dbObjectForQuery = Pattern.compile("CORREL.SUBQUERY.END").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".CORREL.SUBQUERY.END")
                            );
                        }
                    } catch(Exception e) {
                        // ignore
                    }
                }    
            }
        }
        else {
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
                    int upperBound = this.embeddedFeatures.containsKey(mixin) ? 
                        ((Number)this.embeddedFeatures.get(mixin)).intValue() : 
                        1;
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
                        String objectIdColumn = (String)dbObject.getObjectIdColumn().get(0);
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
                    String objectIdColumn = (String)dbObject.getObjectIdColumn().get(0);
                    return "SELECT " + columnSelector + " FROM " + dbObjectForQuery1 + " v INNER JOIN " + joinCriteria[0] + " vj ON v." + objectIdColumn + " = vj." + joinCriteria[2] + " WHERE (1=1)";
                }
                else {
                    return dbObjectForQuery1;
                }
            }
        }
        // Return columns of secondary db object
        else if(viewMode == VIEW_MODE_SECONDARY_COLUMNS) {
            return dbObjectForQuery2 == null ? 
                dbObjectForQuery1 : 
                dbObjectForQuery2;
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED, 
                "Unsupported view mode",
                new BasicException.Parameter("reference", dbObject.getReference()),            
                new BasicException.Parameter("view mode", viewMode)
            );
        }
    }

    //---------------------------------------------------------------------------
    /**
     * val either contains a byte[] or a java.sql.Blob. Depending on the attributeDef
     * and the drivers capabilities this method returns either an ByteArrayInputStream
     * or a byte[].
     */
    abstract Object getBlobColumnValue(
        Object val,
        String attributeName,
        ModelElement_1_0 attributeDef
    ) throws ServiceException, SQLException;

    //---------------------------------------------------------------------------
    /**
     * val either contains a String or a java.sql.Clob. Depending on the attributeDef
     * and the drivers capabilities this method returns either a Reader or a String.
     */
    abstract Object getClobColumnValue(
        Object val,
        String attributeName,
        ModelElement_1_0 attributeDef
    ) throws ServiceException, SQLException;

    //---------------------------------------------------------------------------
    /**
     * Assigns val to spcecified column of prepared statement. val is either a
     * String or a Reader.
     */
    abstract void setClobColumnValue(
        PreparedStatement ps,
        int column,
        Object val
    ) throws SQLException, ServiceException;

    //---------------------------------------------------------------------------
    /**
     * Assigns val to specified column of prepared statement. val is either a
     * byte[] or InputStream.
     */
    abstract void setBlobColumnValue(
        PreparedStatement ps,
        int column,
        Object val
    ) throws SQLException, ServiceException;

    //---------------------------------------------------------------------------
    abstract boolean isBlobColumnValue(
        Object val
    );

    //---------------------------------------------------------------------------
    abstract boolean isClobColumnValue(
        Object val
    );

    //---------------------------------------------------------------------------
    public String getSelectReferenceIdsClause(
        Connection conn,
        Path pattern,
        List<Object> statementParameters
    ) throws ServiceException {
        Path referencePattern = pattern.size() % 2 == 1 ? pattern.getParent() : pattern;
        if(useReferenceTables()) {
            return
            "IN (" + 
            this.getSelectReferenceIdsFormatRefTableClause(
                conn,
                referencePattern,
                statementParameters
            ) + 
            ")";
        }
        else {
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

    //---------------------------------------------------------------------------
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

    //---------------------------------------------------------------------------
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
            String c = i < pathPattern.size() ? pathPattern.get(i) : "#";
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
        }
        else {
            currentClause += "(n " + (matchExact ? "=" : ">=") + " ?)";
            statementParameters.add(Integer.valueOf(pathPattern.size()));          
        }
        return currentClause;
    }

    //---------------------------------------------------------------------------
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
                statementParameters
            );
            referenceIds = new HashSet<Long>();
            while(rs.next()) {
                referenceIds.add(
                    Long.valueOf(rs.getLong(OBJECT_RID))
                );
            }
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path pattern", pathPattern),
                new BasicException.Parameter("statement", statement),
                new BasicException.Parameter("parameters", statementParameters),
                new BasicException.Parameter("sqlErrorCode", ex.getErrorCode()), 
                new BasicException.Parameter("sqlState", ex.getSQLState())
           );
        } catch(ServiceException exception) {
            throw exception;
        } catch(NullPointerException exception) {
            exception.printStackTrace();
            throw new ServiceException(exception);
        } catch(Exception exception) {
            throw new ServiceException(
                exception, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC, 
                exception.toString()
            );
        } finally {
            try {
                if(ps != null) ps.close();
            } catch(Throwable ex) {
                // ignore
            }
            try {
                if(rs != null) rs.close();
            } catch(Throwable ex) {
                // ignore
            }
        }      
        return referenceIds;
    }

    //---------------------------------------------------------------------------
    Object getReferenceId(
        Connection conn,
        Path referencePattern,
        boolean forceCreate
    ) throws ServiceException {  
        Path reference = referencePattern.size() % 2 == 0 ? referencePattern : referencePattern.getParent(); 
        return reference.isEmpty() ? null : useReferenceTables() ? (Object) this.getReferenceIdFormatRefTable(
            conn,
            reference,
            forceCreate
        ) : this.configuration.normalizeObjectIds() ? this.configuration.buildReferenceId(
            reference
        ) : this.getReferenceIdFormatTypeNameWithPathComponents(
            reference
        );
    }

    //---------------------------------------------------------------------------
    String getObjectId(
        String oid
    ) throws ServiceException{
        return this.configuration.normalizeObjectIds() ? this.configuration.buildObjectId(
            oid
        ) : oid; 
    }
    
    //---------------------------------------------------------------------------
    String getObjectId(
        Connection conn,
        Path resourceIdentifier
    ) throws ServiceException{
        return this.configuration.normalizeObjectIds() ? this.configuration.buildObjectId(
            resourceIdentifier
        ) : getReferenceId (
            conn,
            resourceIdentifier.getParent(),
            false
        ) + "/" + resourceIdentifier.getBase();
    }

    //---------------------------------------------------------------------------
    private String getReferenceIdFormatTypeNameWithPathComponents(
        Path reference
    ) throws ServiceException {
        DbObjectConfiguration dbObjectConfiguration =
            this.configuration.getDbObjectConfiguration(
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
                String component = reference.get(l);
                if(":*".equals(component)) {                  
                    if(!subtree){
                        likeRid.append("/%");
                        subtree = true;
                        equalRid = null;
                    } 
                } else if(!component.equals(type.get(l))) {
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
    String getPlaceHolder(
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
     * 
     * @param connection
     * @return
     * @throws ServiceException
     */
    private String getTimestampWithTimzoneExpression(
        Connection connection
    ) throws ServiceException{
        try {
            return this.jdbcDriverSqlProperties.getProperty(
                connection.getMetaData().getDatabaseProductName() + ".TIMESTAMP.WITH.TIMEZONE.EXPRESSION",
                "?"
            );
        } catch (SQLException exception) {
            throw new ServiceException(exception);
        }
    }

    //---------------------------------------------------------------------------
    public String getAutonumValue(
        Connection conn,
        String sequenceName,
        String asFormat
    ) throws ServiceException, SQLException {
        DatabaseMetaData dbm = conn.getMetaData();
        String autonumFormat = asFormat != null && asFormat.indexOf("AS CHAR") > 0 ?
            this.jdbcDriverSqlProperties.getProperty(dbm.getDatabaseProductName() + ".AUTOINC.CHAR") :
                this.jdbcDriverSqlProperties.getProperty(dbm.getDatabaseProductName() + ".AUTOINC.NUMERIC");
        if("AUTO".equals(autonumFormat)) {
            return null;
        } else if(autonumFormat != null) {
            autonumFormat = autonumFormat.replace("${SEQUENCE_NAME}", sequenceName);
            autonumFormat = autonumFormat.replace("${AS_FORMAT}", asFormat == null ? "" : asFormat);
            return autonumFormat;
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE , 
                "AUTONUM format undefined for database. It must be configured in in jdbc-driver-sql.properties.",
                new BasicException.Parameter("databaseName", dbm.getDatabaseProductName())
            );
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Finds the matching path in the _REF table and returns the corresponding
     * referenceId. If forceCreate is true then the referenceId is created on-demand
     */
    private Long getReferenceIdFormatRefTable(
        Connection conn,
        Path reference,
        boolean forceCreate
    ) throws ServiceException {  

        Long referenceId = Long.valueOf(-1);
        String currentStatement = null;
        PreparedStatement ps = null;

        Set<Long> referenceIds = this.findReferenceIdsFormatRefTable(
            conn,
            reference
        );

        // either return when found or create on demand
        List<Object> statementParameters = null;
        try {
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
                    statementParameters.add(statementParameter1);
                    // fill reference components up to maxReferenceComponents.
                    // fill missing component columns with blanks. This allows to
                    // define a unique key on the component columns.
                    for(int i = 0; i < this.maxReferenceComponents; i++) {
                        String statementParameterN;
                        ps.setString(
                            i + 2, 
                            statementParameterN = i < reference.size() ? reference.get(i) : "#"
                        );
                        statementParameters.add(statementParameterN);
                    }
                    executeUpdate(ps, currentStatement, statementParameters);
                    ps.close(); 
                    ps = null;

                    referenceIds = this.findReferenceIdsFormatRefTable(
                        conn,
                        reference
                    );
                    if(referenceIds.size() == 1) {
                        referenceId = referenceIds.iterator().next();
                    }
                    else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE , 
                            "Can not find created reference",
                            new BasicException.Parameter("reference", reference)
                        );
                    }
                }
                else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND, 
                        "Object reference not found",
                        new BasicException.Parameter("reference", reference)
                    );
                }
            }
            else if(referenceIds.size() > 1) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND, 
                    "more than one referenceId found for given reference",
                    new BasicException.Parameter("reference", reference),
                    new BasicException.Parameter("referenceIds", referenceIds)
                );
            }
            else {
                referenceId = referenceIds.iterator().next();
            }
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("reference", reference),
                new BasicException.Parameter("statement", currentStatement),
                new BasicException.Parameter("parameters", statementParameters),
                new BasicException.Parameter("sqlErrorCode", ex.getErrorCode()), 
                new BasicException.Parameter("sqlState", ex.getSQLState())
            );
        } catch(ServiceException exception) {
            throw exception;
        } catch(NullPointerException exception) {
            exception.printStackTrace();
            throw new ServiceException(exception);
        } catch(Exception exception) {
            throw new ServiceException(
                exception, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC, 
                exception.toString()
            );
        } finally {
            try {
                if(ps != null) ps.close();
            } catch(Throwable ex) {
                // ignore
            }
        }
        return referenceId;
    }

    //---------------------------------------------------------------------------
    public Path getReference(
        Connection conn,
        Object referenceId
    ) throws ServiceException {
        return useReferenceTables() ? this.getReferenceFormatRefTable(
            conn,
            (Number)referenceId
        ) : this.configuration.normalizeObjectIds() ? this.configuration.buildResourceIdentifier(
            (String)referenceId, 
            true // resource
        ) : this.getReferenceFormatTypeNameWithComponents(
            conn,
            (String)referenceId
        );
    }

    //---------------------------------------------------------------------------
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
        DbObjectConfiguration dbObjectConfiguration = this.configuration.getDbObjectConfiguration(
            components.get(0)
        );
        Path type = dbObjectConfiguration.getType();
        String[] referenceComponents = new String[type.size()-1];
        int pos = 1;
        for(int i = 0; i < referenceComponents.length; i++) {
            if(":*".equals(type.get(i))) {
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
            }
            else {
                referenceComponents[i] = type.get(i);
            }
        }
        return new Path(referenceComponents);
    }

    //---------------------------------------------------------------------------
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
                statementParameters
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
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND, 
                    "can not find reference id",
                    new BasicException.Parameter("referenceId", referenceId),
                    new BasicException.Parameter("table", this.namespaceId + "_" + T_REF)
                );
            }
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("reference", reference),
                new BasicException.Parameter("statement", statement),
                new BasicException.Parameter("parameters", statementParameters),
                new BasicException.Parameter("sqlErrorCode", ex.getErrorCode()), 
                new BasicException.Parameter("sqlState", ex.getSQLState())
            );
        } catch(ServiceException exception) {
            throw exception;
        } catch(NullPointerException exception) {
            exception.printStackTrace();
            throw new ServiceException(exception);
        } catch(Exception exception) {
            throw new ServiceException(
                exception, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC, 
                exception.toString()
            );
        } finally {
            try {
                if(rs != null) rs.close();
            } catch(Throwable ex) {
                // ignore
            }
            try {
                if(ps != null) ps.close();
            } catch(Throwable ex) {
                // ignore
            }
        }      
        return reference;    
    }

    //---------------------------------------------------------------------------
    /**
     * Map database-neutral column name to database-specific column name.
     */
    String getDatabaseSpecificColumnName(
        Connection conn,
        String columnName,
        boolean ignoreReservedWords
    ) throws ServiceException {
        String databaseProductName = null;
        try {
            databaseProductName = conn.getMetaData().getDatabaseProductName();
        } catch(Exception e) {}
        if(!ignoreReservedWords) {
            if(
                "HSQL Database Engine".equals(databaseProductName) &&
                (RESERVED_WORDS_HSQLDB.contains(columnName) || (columnName.indexOf("$") >= 0))
            ) {
                columnName = "\"" + columnName.toUpperCase() + "\"";
            }
            else if(
                "Oracle".equals(databaseProductName) &&
                RESERVED_WORDS_ORACLE.contains(columnName)
            ) {
                columnName = "\"" + columnName + "\"";
            }
        }
        return columnName;
    }

    //---------------------------------------------------------------------------    
    String getColumnName(
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
                }
                else if(c == '_') {
                    if(i < attributeName.length()-1) {
                        // do not escape _<digit>
                        if(Character.isDigit(attributeName.charAt(i+1))) {
                            name.append('_');
                        }
                        // escape _<alpha> as __<uppercase alpha>
                        else {
                            name.append("__");
                        }
                    }
                    else {
                        name.append('_');
                    }
                }
                else {
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
                mappedName = this.configuration.getFromToColumnNameMapping().get(privateAttributesPrefix + lookupName);
                if(mappedName == null) {
                    mappedName = this.configuration.getFromToColumnNameMapping().get(lookupName);
                    if(mappedName != null){
                        mappedName = privateAttributesPrefix + mappedName;
                    }
                }
            } else {
                mappedName = this.configuration.getFromToColumnNameMapping().get(lookupName);
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
            columnName += this.embeddedFeatures.containsKey(attributeName) ? '_' : '$';
            columnName += String.valueOf(index);
        }
        columnName = this.getDatabaseSpecificColumnName(
            conn, 
            columnName, 
            ignoreReservedWords
        );
        return columnName;
    }

    //---------------------------------------------------------------------------
    public String getFeatureName(
        String columnName
    ) {
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
            if(this.embeddedFeatures.containsKey(attributeNameOfNonIndexedColumn)) {
                return attributeNameOfNonIndexedColumn;      
            }        
        }

        // to->from mapping
        if(this.configuration.getToFromColumnNameMapping().containsKey(mappedColumnName)) {
            mappedColumnName = this.configuration.getToFromColumnNameMapping().get(mappedColumnName);
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
                }
                else if(!nextAsUpperCase && (c == '_')) {
                    nextAsUpperCase = true;
                }
                else {
                    if(nextAsUpperCase) {
                        if(Character.isDigit(c)) {
                            name += "_" + c;
                        }
                        else {
                            name += Character.toUpperCase(c);
                        }
                    }
                    else {  
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

    //---------------------------------------------------------------------------
    /**
     * Calls ps.set<T> where <T> is the type of value.
     *
     * @param ps initialized prepared statement
     * 
     * @param position value position starting with 1.
     *
     * @param value 
     *
     */
    public void setPreparedStatementValue(
        Connection conn,
        PreparedStatement ps,
        int position,
        Object value
    ) throws ServiceException, SQLException {
        if(value instanceof java.util.Date) {
            value = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(
                DateTimeFormat.EXTENDED_UTC_FORMAT.format((java.util.Date)value)
            );
        }
        if( value instanceof URI) {
            ps.setString(
                position, 
                value.toString()
            );
        }
        else if(value instanceof Short) {
            ps.setShort(
                position, 
                ((Short)value).shortValue()
            );
        }
        else if(value instanceof Integer) {
            ps.setInt(
                position, 
                ((Integer)value).intValue()
            );
        }
        else if(value instanceof Long) {
            ps.setLong(
                position, 
                ((Long)value).longValue()
            );
        }
        else if(value instanceof BigDecimal) {
            ps.setBigDecimal(
                position, 
                ((BigDecimal)value).setScale(ROUND_UP_TO_MAX_SCALE, BigDecimal.ROUND_UP)
            );
        }
        else if(value instanceof Number) {
            ps.setString(
                position, 
                value.toString()
            );
        }
        else if(value instanceof Path) {
            ps.setString(
                position,
                this.externalizePathValue(conn, (Path)value)
            );
        }
        else if(value instanceof Boolean) {
            Object sqlValue = this.booleanMarshaller.marshal(value, conn); 
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
        }
        else if(value instanceof Duration) {
            Object sqlValue = this.durationMarshaller.marshal(value); 
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
        }
        else if(value instanceof XMLGregorianCalendar) {
            Object sqlValue = this.calendarMarshaller.marshal(value, conn); 
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
        }
        else if(value instanceof String) {
            // ps.setString() does not work for Oracle with strings > 4K. Use proprietary 
            // setStringForClob() method. CLOB.createTemporary(conn, ...) does not work
            // for app servers (--> ClassCastException).
            try {
                if(
                    // Additional guard added...
                    ((String)value).length() > 32000 / 4 &&
                    // ... to avoid unnecessary getBytes() invocations
                    (ps.getClass().getName().indexOf("oracle") >= 0) &&                
                    (((String)value).getBytes("UTF-8").length > 32000)
                ) {
                    try {
                        Class<?> cl =  ps.getClass();
                        Method setStringForClob = cl.getMethod(
                            "setStringForClob",
                            new Class[]{int.class, String.class}
                        );
                        setStringForClob.invoke(
                            ps,
                            Integer.valueOf(position), value
                        );
                    }
                    catch(NoSuchMethodException e) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED, 
                            "Method setStringForClob() on prepared statement not found. Required to store strings of size > 32K. Use Oracle JDBC Driver version - 10",
                            new BasicException.Parameter("value", value)
                        );
                    }
                    catch(Exception e) {
                        throw new ServiceException(e);
                    }
                }
                else {
                    ps.setString(
                        position, 
                        (String)value
                    );
                }
            }
            catch(Exception e) {
                throw new ServiceException(e);
            }
        }
        else if(
            (value instanceof byte[]) ||
            (value instanceof InputStream) ||
            (value instanceof BinaryLargeObject)
        ) {
            this.setBlobColumnValue(
                ps,
                position,
                value
            );
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED, 
                "attribute type not supported",
                new BasicException.Parameter("value-type", value == null ? null : value.getClass().getName()),
                new BasicException.Parameter("position", position)
            );
        }
    }

    //---------------------------------------------------------------------------
    private MappedRecord getObject(
        Connection conn,
        Path path,
        short attributeSelector,
        Map attributeSpecifiers,
        boolean objectClassAsAttribute,
        DbObject dbObject,
        ResultSet rs,
        String objectClass,
        boolean checkIdentity
    ) throws ServiceException, SQLException {
        List<MappedRecord> objects = new ArrayList<MappedRecord>();
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
        	if(checkIdentity || !objects.isEmpty()) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND, 
                    objects.isEmpty() ? "No object found" : "Exactly one object expected",
                    new BasicException.Parameter("request-path", path),
                    new BasicException.Parameter("cardinality", objects.size())
                );
        	} else {
        		return null;
        	}
        }
        MappedRecord object = objects.get(0);
        if(checkIdentity && !Object_2Facade.getPath(object).startsWith(path)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND, 
                "invalid object path. no object found",
                new BasicException.Parameter("request-path", path),
                new BasicException.Parameter("object-path", Object_2Facade.getPath(object))
            );
        }
        SysLog.detail("retrieved object", objects);
        return object;
    }

    //---------------------------------------------------------------------------
    
    /**
     * Tells whether the given feature is persistent
     * 
     * @param featureDef the features meta-data
     * 
     * @return <code>true</code> if the given feature is persistent
     */
    protected boolean isPersistent(
		ModelElement_1_0 featureDef
    ) throws ServiceException{
        return 
            !this.nonPersistentFeatures.contains(featureDef.objGetValue("qualifiedName")) &&
            Persistency.getInstance().isPersistentAttribute(featureDef);
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
            return (ModelElement_1_0)((Map)getModel().getElement(objectClass).objGetValue("allFeature")).get(featureName);
        } catch(Exception e){
            return null;
        }
    }
    
    /**
     * Touch object feature (set empty value). The feature must be a modelled 
     * feature which are either non-derived or system attributes (otherwise 
     * the feature is not touched).
     */
    private void touchAttributes(
        MappedRecord object,
        String featureName
    ) throws ServiceException {
        Object_2Facade facade = Facades.asObject(object);
        ModelElement_1_0 featureDef = getFeatureDef(facade.getObjectClass(), featureName);
        if(featureDef != null && isPersistent(featureDef)) {
            facade.attributeValues((String) featureDef.objGetValue("name"));
        }
    }

    //---------------------------------------------------------------------------
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
        List<MappedRecord> objects,
        short attributeSelector,
        Map attributeSpecifiers,
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
            dbObject.getConfiguration().isAbsolutePositioningDisabled()
        );
        boolean hasMore = frs != null;
        // get objects
        Map<Path,MappedRecord> candidates = new HashMap<Path,MappedRecord>();
        String previousObjectId = null;
        Map<String,ModelElement_1_0> featureDefs = null;
        MappedRecord current = null;
        Set<Integer> processedIdxs = new HashSet<Integer>();
        String objectClass = null;
        while(hasMore) {
            Path objectPath = dbObject.getResourceIdentifier(frs);
            Path reference = objectPath.getParent();
            String objectId = objectPath.getBase();
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
                 featureDefs = (Map)getModel().getElement(objectClass).objGetValue("allFeatureWithSubtype");
            } catch(Exception e) {
                SysLog.trace(
                    "class not found in model fall back to non model-driven mode",
                    objectClass
                );
            }
            // skip to next object? If yes, create new one and add it to list
            if(
                !objectId.equals(previousObjectId) ||
                !reference.equals(Object_2Facade.getPath(current).getParent())
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
                // Iterate through object attributes and add values
                for(String columnName: frs.getColumnNames()) {
                    String featureName = this.getFeatureName(columnName);  
                    ModelElement_1_0 featureDef = featureDefs == null ? null : (ModelElement_1_0)featureDefs.get(featureName);
                    // Always include reference columns. Otherwise dbObject decides
                    if(dbObject.includeColumn(columnName)) {
                        // Check whether attribute must be added
                        boolean addValue = SystemAttributes.OBJECT_CLASS.equals(featureName);
                        switch(attributeSelector) {
                            case AttributeSelectors.NO_ATTRIBUTES:
                                break;
                            case AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES:
                                if(featureDef == null) {
                                    // Return in case the type is unknown
                                    addValue = true;
                                } else if(SYSTEM_ATTRIBUTES.contains(featureName)) {
                                    // Return system features
                                    addValue = true;
                                } else if(ModelHelper.getMultiplicity(featureDef).isMultiValued()) {
                                    // Return multi-valued features if we have at least one attribute specifier
                                    addValue = !attributeSpecifiers.isEmpty();
                                } else {
                                    // Return single-valued features
                                    addValue = true;
                                }
                                break;
                            case AttributeSelectors.ALL_ATTRIBUTES:
                                addValue = true;
                                break;
                            case AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES:
                                // check for system attributes
                                if(SYSTEM_ATTRIBUTES.contains(featureName)) {
                                    addValue = true;
                                } else {
                                    // check for specified attributes
                                    AttributeSpecifier specifier = (AttributeSpecifier)attributeSpecifiers.get(featureName);
                                    addValue = specifier != null;
                                }
                                break;
                        }
                        // Add value
                        if(addValue) {
                            String featureType;
                            if(featureDef == null) {
                                featureType = null;
                            } else try {
                                featureType = (String)getModel().getElementType(featureDef).objGetValue("qualifiedName");
                            } catch (Exception exception) {
                                throw new ServiceException(
                                    exception,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.INVALID_CONFIGURATION,
                                    "Unable to determine the element type",
                                    new BasicException.Parameter("feature", featureDef.jdoGetObjectId())
                                );
                            }
                            Multiplicity multiplicity = featureDef == null ? null : ModelHelper.getMultiplicity(featureDef);
                            Object val = frs.getObject(columnName);
                            // Embedded attribute? If yes derive idx from column name suffix (instead of slice index)
                            int valueIdx = idx;
                            boolean isEmbedded = false;
                            if(this.embeddedFeatures.containsKey(featureName)) {
                                valueIdx = Integer.valueOf(columnName.substring(columnName.lastIndexOf('_') + 1)).intValue();
                                isEmbedded = true;
                            }
                            // String, Clob
                            if(this.isClobColumnValue(val)) {
                                boolean isReferenceFeature = this.useNormalizedReferences() && (
                                    frs.getColumnNames().contains(this.getColumnName(conn, toRid(featureName), 0, false, false, true)) ||
                                    frs.getColumnNames().contains(this.getColumnName(conn, featureName + "Parent", 0, false, false, true))
                                );                                
                                val = this.getClobColumnValue(val, featureName, featureDef);
                                if(val instanceof Reader) {
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
                                } 
                                else if (
                                    // class type || PrimitiveTypes.PATH
                                    isReferenceFeature ||
                                    ((featureType == null) && ((String)val).startsWith(URI_1.OPENMDX_PREFIX)) ||
                                    ((featureType == null) && ((String)val).startsWith("xri:")) ||
                                    ((featureType != null) && (PrimitiveTypes.OBJECT_ID.equals(featureType) || getModel().isClassType(featureType)))
                                ) {
                                    if(this.useNormalizedReferences()) {
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
                                            if (this.configuration.normalizeObjectIds()) {
                                                resourceIdentifier = this.configuration.buildResourceIdentifier(
                                                    ref, 
                                                    false // reference
                                                );
                                            } 
                                            else {
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
                                        }
                                        else {
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
                                            } catch(IndexOutOfBoundsException e) {
                                                throw new ServiceException(
                                                    e,
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
                                        featureName
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

    //---------------------------------------------------------------------------
    protected void setValue(
        Object target,
        int index,
        Object value,
        boolean allowFilling
    ) {
        if(target instanceof SparseArray) {
            ((SparseArray)target).put(
                index, 
                value
            );                                        
        }
        else {
            List values = (List)target;
            // Fill value up to requested index if allowFilling = true
            if(allowFilling) {
                while(index > values.size()) {
                    values.add(value);
                }
            }
            if(index == values.size()) {
                values.add(value);
            }
            else {
                values.set(
                    index, 
                    value
                );
            }
        }
    }
    
    //---------------------------------------------------------------------------
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
                this.booleanMarshaller.unmarshal(val, conn),
                isEmbedded
            );
        }      
        else if(
            PrimitiveTypes.DATE.equals(featureType) &&
            val instanceof Timestamp
        ) {
            // Some Jdbc drivers / databases may return java.sql.Timestamp instead
            // of java.sql.Date even if the column type is Date. Force the conversion
            // from Timestamp to date.
            this.setValue(
                target, 
                index, 
                this.calendarMarshaller.unmarshal(val.toString().substring(0, 10)),
                isEmbedded
            );
        }
        //
        // org::w3c::date
        // org::w3c::dateTime
        //
        else if(
            PrimitiveTypes.DATE.equals(featureType) ||
            PrimitiveTypes.DATETIME.equals(featureType) ||
            val instanceof Timestamp ||
            val instanceof Time
        ) {
            this.setValue(
                target, 
                index, 
                this.calendarMarshaller.unmarshal(val),
                isEmbedded
            );
        } 
        //
        // org::w3c::duration
        //
        else if(PrimitiveTypes.DURATION.equals(featureType)) {
            this.setValue(
                target, 
                index, 
                this.durationMarshaller.unmarshal(val),
                isEmbedded
            );
        } 
        //
        // URIs
        //
        else if (PrimitiveTypes.ANYURI.equals(featureType)) {
            this.setValue(
                target, 
                index, 
                URIMarshaller.NORMALIZE.marshal(val),
                isEmbedded
            );
        }
        //
        // openMDX 1 clients expect all numbers to be returned 
        // as BigIntegers
        //
        else if(val instanceof Number) {
            Object value = val instanceof BigDecimal ? 
                val : 
                    new BigDecimal(val.toString());
            this.setValue(
                target, 
                index, 
                value,
                isEmbedded
            );
        } 
        //
        // default 
        //
        else if(val instanceof String) {
            this.setValue(
                target, 
                index, 
                val,
                isEmbedded
            );
        } 
        //
        // unknown 
        //
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED, 
                "invalid column type. Supported are [Number|String|byte[]|Blob|Timestamp|Time]",
                new BasicException.Parameter("featureType", featureType),
                new BasicException.Parameter("columnType", val.getClass().getName())
            );
        }
    }
    
    //---------------------------------------------------------------------------
    protected void filterToSqlClause(
        Connection conn,
        DbObject dbObject,
        String viewAliasName,
        String view1,
        String view2,
        String joinColumn,
        ModelElement_1_0 referencedType,
        List<FilterProperty> allFilterProperties,
        List<FilterProperty> primaryFilterProperties,
        List<String> includingClauses,
        List<List<Object>> includingClausesValues,
        List<String> exludingClauses,
        List<List<Object>> exludingClausesValues
    ) throws ServiceException {

        List<Object> filterValues = new ArrayList<Object>();
        
        // positive single-valued filter
        includingClauses.add(
            AbstractDatabase_1.this.filterToSqlClause(
                conn,
                dbObject,
                viewAliasName,
                view1,
                true, // joinViewIsPrimary
                dbObject.getIndexColumn() != null, // joinViewIsIndexed
                joinColumn,
                referencedType,
                primaryFilterProperties,
                false, // negate
                filterValues
            )
        );
        includingClausesValues.add(filterValues);

        // negative single-valued filter
        filterValues = new ArrayList<Object>();
        includingClauses.add(
            AbstractDatabase_1.this.filterToSqlClause(
                conn,
                dbObject,
                viewAliasName,
                view1,
                true, // joinViewIsPrimary
                dbObject.getIndexColumn() != null, // joinViewIsIndexed
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

                // plus clause
                filterValues = new ArrayList<Object>();
                includingClauses.add(
                    AbstractDatabase_1.this.filterToSqlClause(
                        conn,
                        dbObject,
                        viewAliasName,
                        view2,
                        false, // joinViewIsPrimary
                        dbObject.getIndexColumn() != null, // joinViewIsIndexed
                        joinColumn,
                        referencedType,
                        Collections.singletonList(p),
                        false, // negate
                        filterValues
                    )
                );
                includingClausesValues.add(filterValues);

                /**
                 * Minus clauses are only required if maxSlicesPerObject > 0. In this case
                 * non-matching slices have to be subtracted from the plus result set.
                 */
                filterValues = new ArrayList<Object>();
                exludingClauses.add(
                    AbstractDatabase_1.this.filterToSqlClause(
                        conn,
                        dbObject,
                        viewAliasName,
                        view2,
                        false, // joinViewIsPrimary
                        dbObject.getIndexColumn() != null, // joinViewIsIndexed
                        joinColumn,
                        referencedType,
                        Collections.singletonList(p),
                        true, // negate
                        filterValues
                    )
                );
                exludingClausesValues.add(filterValues);
            }
        }
    }
    
    //---------------------------------------------------------------------------
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
     */
    protected String filterToSqlClause(
        Connection conn,
        DbObject dbObject,
        String viewAliasName,
        String view,
        boolean viewIsPrimary,
        boolean viewIsIndexed,
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
        List<ModelElement_1_0> filterPropertyDefs = AbstractDatabase_1.this.getFilterPropertyDefs(
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
            if(filterProperty.quantor() == (negate ? Quantifier.FOR_ALL.code() : Quantifier.THERE_EXISTS.code())) {
                // For embedded features the clause is of the form (expr0 OR expr1 OR ... OR exprN)
                // where N is the upper bound for the embedded feature 
                int upperBound = this.embeddedFeatures.containsKey(filterProperty.name()) ? 
                    ((Number)this.embeddedFeatures.get(filterProperty.name())).intValue() : 
                        1;
                clause.append(operator).append("(");
                for(
                    int idx = 0; 
                    idx < upperBound; 
                    idx++
                ) {
                    String columnName = this.getColumnName(
                        conn, 
                        filterProperty.name(), 
                        idx, 
                        upperBound > 1, 
                        true, 
                        false // markAsPrivate
                    );
                    columnName = 
                        viewIsPrimary && viewIsIndexed ? 
                            viewAliasName + "m." + columnName : // get from mixin view 'vm'
                                viewAliasName + "." + columnName; // get from main view 'v'
                    if(idx > 0) {
                        clause.append(" OR ");
                    }
                    clause.append("(");
                    clause.append(
                        this.filterPropertyToSqlClause(
                            conn,
                            dbObject,
                            dbObject.getReference(),
                            viewAliasName,
                            filterProperty,
                            filterPropertyDef,
                            negate && !viewIsPrimary, 
                            columnName, 
                            clauseValues
                        )
                    );
                    if(
                        viewIsPrimary && 
                        (filterProperty.quantor() == Quantifier.FOR_ALL.code()) &&
                        (filterProperty.getValues().length == 0 || !(filterProperty.getValue(0) instanceof Filter))
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
            }
            else {
                // View is the secondary db object containing the multi-valued columns
                // of an object. This is why reference clause 2 must be used for selection
                boolean hasSecondaryDbObject =
                    (dbObject.getConfiguration().getDbObjectForQuery2() != null) ||
                    (dbObject.getConfiguration().getDbObjectForUpdate2() != null);
                if(hasSecondaryDbObject) {
                    statementParameters.addAll(clauseValues);
                    return 
                        (joinColumn == null ? "(" : "(" + joinColumn + " " + (negate ? "NOT" : "") +  " IN ") +
                        "(SELECT " + dbObject.getObjectIdColumn().get(0) + " FROM " + (view.startsWith("SELECT") ? "(" + view + ") " + viewAliasName : view + " " + viewAliasName + " ") +
                        " WHERE (" + clause + ")))";
                }
                else {
                    statementParameters.addAll(dbObject.getReferenceValues());
                    statementParameters.addAll(clauseValues);
                    return 
                        (joinColumn == null ? "(" : "(" + joinColumn + " " + (negate ? "NOT" : "") +  " IN ") +
                        "(SELECT " + dbObject.getObjectIdColumn().get(0) + " FROM " + (view.startsWith("SELECT") ? "(" + view + ") " + viewAliasName : view + " " + viewAliasName + " ") +
                        " WHERE " + dbObject.getReferenceClause() + 
                        " AND (" + clause + ")))";            
                }
            }
        }
        else {
            return "";
        }
    }

    /**
     * Add an IS_IN clause
     * 
     * @param connection 
     * @param dbObject 
     * @param columnName
     * @param filterPropertyDef
     * @param clause
     * @param clauseValues
     * @param values
     */
    protected void isInToSqlClause(
        Connection connection,
        DbObject dbObject,
        String columnName,
        ModelElement_1_0 filterPropertyDef,
        StringBuilder clause, 
        List<Object> clauseValues, 
        Object[] values
    ) throws ServiceException {
        clause.append(columnName).append(" IN (");
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
        boolean includeSubTree = "%".equals(value.getBase());
        // If path ends with % get the set of matching types
        Path path = value;
        if(includeSubTree) {
            path = value.getParent();
            for(
                Iterator<DbObjectConfiguration> k = this.configuration.getDbObjectConfigurations().iterator(); 
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
                    for(String c : type){
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
                for(String c : value) {
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
     * Appends an SQL clause to 'clause' according to filter property 'p'.
     * if 'negate' is true, then the operation is negated. The clause is
     * generated for SQL column with 'columnName'. The clause is generated
     * prepared statement using ? as place holders. The corresponding 
     * values are added to filterValues. The generated clause is of the
     * form (columnName operator literal). ANY or EACH are not handled
     * by this method.
     */
    private String filterPropertyToSqlClause(
        Connection conn,
        DbObject dbObject,
        Path reference,
        String viewAliasName,
        FilterProperty filterProperty,
        ModelElement_1_0 filterPropertyDef,
        boolean negate,
        String columnName, 
        List<Object> clauseValues
    ) throws ServiceException {
        StringBuilder clause = new StringBuilder();
        short operator = filterProperty.operator();
        int quantor = filterProperty.quantor();
        if(negate) {
            quantor = quantor == Quantifier.THERE_EXISTS.code() ? Quantifier.FOR_ALL.code() : Quantifier.THERE_EXISTS.code();
            operator = (short)-operator;
        }
        switch(ConditionType.valueOf(operator)) {

            /**
             * Evaluate the following:
             * THERE_EXISTS v IN A: v NOT IN Q (Special: if Q={} ==> true, iff A<>{}, false otherwise)
             * FOR_ALL v IN A: v NOT IN Q (Special: if Q={} ==> true) 
             */
            case IS_NOT_IN:
                // Q = {}
                if(filterProperty.getValues().length == 0) {
                    if(quantor == Quantifier.FOR_ALL.code()) {
                        clause.append("(1=1)");
                    }
                    else {
                        clause.append("(").append(columnName).append(" IS NOT NULL)");
                    }
                }

                // Q <> {}
                else {
                    clause.append("(").append(columnName).append(" NOT IN (").append(getPlaceHolder(conn, filterProperty.getValue(0)));
                    clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(0)));
                    for(
                        int j = 1; 
                        j < filterProperty.getValues().length; 
                        j++
                    ) {
                        clause.append(", ").append(getPlaceHolder(conn, filterProperty.getValue(j)));
                        clauseValues.add(this.externalizeStringValue(columnName, filterProperty.getValue(j)));
                    }
                    clause.append("))");
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
                    if(quantor == Quantifier.THERE_EXISTS.code()) {
                        clause.append("(1=0)");
                    }
                    else {
                        clause.append("(").append(columnName).append(" IS NULL)");
                    }
                } 

                // Q <> {}
                else {
                    // Complex filter value
                    if(
                        (filterProperty.getValues().length > 0) && 
                        (filterProperty.getValue(0) instanceof Filter)
                    ) {
                        Model_1_0 model = this.getModel();
                        if(filterPropertyDef.isReferenceType()) {
                            ModelElement_1_0 referencedType;
                            try {
                                referencedType = model.getElementType(filterPropertyDef);
                            } catch (Exception exception) {
                                throw new ServiceException(
                                    exception,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.INVALID_CONFIGURATION,
                                    "Unable to retrieve the element type",
                                    new BasicException.Parameter("filterProperty", filterPropertyDef.jdoGetObjectId())
                                );
                            }
                            String joinClauseBegin = null;
                            String joinClauseEnd = null;
                            String joinColumn = null;
                            DbObject joinObject = null;
                            // Reference
                            if(model.referenceIsStoredAsAttribute(filterPropertyDef)) {
                                joinObject = this.getDbObject(
                                    conn, 
                                    model.getIdentityPattern(referencedType), 
                                    null, 
                                    true
                                );
                                joinClauseBegin = columnName;
                                joinClauseEnd = "";
                                joinColumn = (String)joinObject.getObjectIdColumn().get(0);
                            }
                            // Composite parent
                            else if(ModelHelper.isCompositeEnd(filterPropertyDef, true)) {
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
                                joinColumn = (String)joinObject.getObjectIdColumn().get(0);
                            }
                            // Composite
                            else if(ModelHelper.isCompositeEnd(filterPropertyDef, false)) {
                                joinObject = this.getDbObject(
                                    conn, 
                                    reference.getDescendant(":*", filterProperty.name()), 
                                    null, 
                                    true
                                );
                                joinClauseBegin = viewAliasName + "." + (String)dbObject.getObjectIdColumn().get(0);
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
                            // Shared
                            else if(ModelHelper.isSharedEnd(filterPropertyDef, false)) {
                                joinObject = this.getDbObject(
                                    conn, 
                                    reference.getDescendant(":*", filterProperty.name()), 
                                    null, 
                                    true
                                );
                                if(joinObject.getJoinCriteria() != null && joinObject.getJoinCriteria().length == 3) {                                    
                                    String[] joinCriteria = joinObject.getJoinCriteria();
                                    joinClauseBegin =
                                        viewAliasName + "." + (String)dbObject.getObjectIdColumn().get(0) +
                                        " IN (SELECT " + joinCriteria[1] + " FROM " + joinCriteria[0] + " WHERE " +  joinCriteria[2];
                                    joinClauseEnd = ")";
                                    joinColumn = (String)joinObject.getObjectIdColumn().get(0);
                                }
                                else {
                                    joinClauseBegin = viewAliasName + "." + (String)dbObject.getObjectIdColumn().get(0);
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
                            }
                            else {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.NOT_SUPPORTED,
                                    "Unsupported aggregation",
                                    new BasicException.Parameter("filter.property", filterProperty),
                                    new BasicException.Parameter("filter.definition", filterPropertyDef)                                
                                );
                            }
                            Filter filter = (Filter)filterProperty.getValue(0);
                            List<FilterProperty> allFilterProperties = FilterProperty.getFilterProperties(filter);
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
                            String view1 = joinObject.getConfiguration().getDbObjectForQuery1() == null ? 
                                joinObject.getConfiguration().getDbObjectForUpdate1() : 
                                    joinObject.getConfiguration().getDbObjectForQuery1();
                            String view2 = joinObject.getConfiguration().getDbObjectForQuery2() == null ? 
                                joinObject.getConfiguration().getDbObjectForUpdate2() == null ?
                                    view1 :
                                        joinObject.getConfiguration().getDbObjectForUpdate2() :
                                            joinObject.getConfiguration().getDbObjectForQuery1();
                            // Positive clauses
                            List<String> includingFilterClauses = new ArrayList<String>();
                            List<List<Object>> includingFilterValues = new ArrayList<List<Object>>();
                            List<String> excludingFilterClauses = new ArrayList<String>();
                            List<List<Object>> excludingFilterValues = new ArrayList<List<Object>>();   
                            this.filterToSqlClause(
                                conn, 
                                joinObject,
                                viewAliasName + "v",
                                view1, 
                                view2, 
                                joinColumn, 
                                referencedType, 
                                allFilterProperties, 
                                primaryFilterProperties, 
                                includingFilterClauses, 
                                includingFilterValues, 
                                excludingFilterClauses, 
                                excludingFilterValues
                            );
                            boolean isForAll = filterProperty.quantor() == Quantifier.FOR_ALL.code();
                            clause.append(
                                "(" + joinClauseBegin + (isForAll ? " NOT" : "") + " IN (SELECT " + joinColumn + " FROM " + view1 + " " + viewAliasName + "v WHERE " + (isForAll ? "(1=0)" : "(1=1)")
                            );
                            for(int i = 0; i < includingFilterClauses.size(); i++) {
                                if(includingFilterClauses.get(i).length() > 0) {
                                    clause
                                        .append(isForAll ? " OR NOT " : " AND ")
                                        .append(includingFilterClauses.get(i));
                                    clauseValues.addAll(
                                        includingFilterValues.get(i)
                                    );
                                }
                            }
                            clause
                                .append("))")
                                .append(joinClauseEnd);
                            // Negative clauses
                            includingFilterClauses.clear();
                            includingFilterValues.clear();
                            excludingFilterClauses.clear();
                            excludingFilterValues.clear();   
                            this.filterToSqlClause(
                                conn, 
                                joinObject,
                                viewAliasName + "v",
                                view1, 
                                view2, 
                                null, // no join column 
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
                        else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "Filter property with value of type " + Filter.class.getName() + " must be a Reference",
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
                        String externalized = (String)this.externalizeStringValue(columnName, v);
                        externalized = this.sqlWildcards.fromJDO(externalized);
                        if(externalized.startsWith(JDO_CASE_INSENSITIVE_FLAG)) {
                            String databaseProductName = "N/A";
                            try {
                                DatabaseMetaData dbm = conn.getMetaData();
                                databaseProductName = dbm.getDatabaseProductName();
                            } 
                            catch(Exception e) {
                                // ignore
                            }
                            if(databaseProductName.startsWith("DB2")) {
                                clause.append("(UPPER(").append(columnName).append(") LIKE ? ").append(getEscapeClause(conn)).append(" OR LOWER(").append(columnName).append(") LIKE ? ").append(getEscapeClause(conn)).append(")");                                
                                clauseValues.add(
                                    externalized.substring(JDO_CASE_INSENSITIVE_FLAG.length()).toUpperCase()
                                );
                                clauseValues.add(
                                    externalized.substring(JDO_CASE_INSENSITIVE_FLAG.length()).toLowerCase()
                                );
                            }
                            else {
                                clause.append("(UPPER(").append(columnName).append(") LIKE UPPER(?) ").append(getEscapeClause(conn)).append(" OR LOWER(").append(columnName).append(") LIKE ? ").append(getEscapeClause(conn)).append(" OR UPPER(").append(columnName).append(") LIKE ? ").append(getEscapeClause(conn)).append(")");
                                clauseValues.add(
                                    externalized.substring(JDO_CASE_INSENSITIVE_FLAG.length())
                                );
                                clauseValues.add(
                                    externalized.substring(JDO_CASE_INSENSITIVE_FLAG.length()).toLowerCase()
                                );
                                clauseValues.add(
                                    externalized.substring(JDO_CASE_INSENSITIVE_FLAG.length()).toUpperCase()
                                );
                            }
                        }
                        else {
                            clause.append("(").append(columnName).append(" LIKE ? ").append(getEscapeClause(conn)).append(")");
                            clauseValues.add(externalized);                  
                        }
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
                        boolean includeSubTree = "%".equals(vp.getBase());
                        // If path ends with % get the set of matching types
                        if(includeSubTree) {
                            vp = vp.getPrefix(vp.size()-1);
                            for(DbObjectConfiguration dbObjectConfiguration : this.configuration.getDbObjectConfigurations()) {
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
                            Iterator i = matchingTypes.iterator();
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
                            }
                            else if(externalized.startsWith("spice:")) {
                                for(String c : (Path)v){
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
                    }
                    else {
                        String externalized = (String)this.externalizeStringValue(columnName, v);
                        externalized = this.sqlWildcards.fromJDO(externalized);
                        if(externalized.startsWith(JDO_CASE_INSENSITIVE_FLAG)) {
                            clause.append("(NOT (UPPER(").append(columnName).append(") LIKE ? ").append(getEscapeClause(conn)).append("))");
                            clauseValues.add(
                                externalized.substring(JDO_CASE_INSENSITIVE_FLAG.length()).toUpperCase()
                            );
                        }
                        else {
                            clause.append("(NOT (").append(columnName).append(" LIKE ? ").append(getEscapeClause(conn)).append("))");
                            clauseValues.add(externalized);                  
                        }
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
                    new BasicException.Parameter("operator", ConditionType.valueOf(operator))
                );
        }
        return clause.toString();
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
        }
        catch(Exception e) {
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
                            String qualifiedFilterPropertyName = subtype.objGetValue("qualifiedName") + ":" + filterPropertyName;
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
            }
            catch(ServiceException e) {
                SysLog.warning("The following error occured when trying to determine multiplicity of filter property");
                e.log();
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
            final boolean primaryFilterProprty;
            if(filterPropertyDef == null) {
                primaryFilterProprty = this.singleValueAttributes.contains(filterProperty.name());
            } else if(
                filterPropertyDef.isReferenceType() && 
                !this.getModel().referenceIsStoredAsAttribute(filterPropertyDef)
            ) {
            	primaryFilterProprty = true;
        	} else {
            	primaryFilterProprty = ModelHelper.getMultiplicity(filterPropertyDef).isSingleValued();
            }
            if(primaryFilterProprty) {
                primaryFilterProperties.add(filterProperty);
            }
        }
        return primaryFilterProperties;
    }

    //------------------------------------------------------------------------
    protected void removePrivateAttributes(
        MappedRecord object
    ) throws ServiceException {
        this.removeAttributes(
            object, 
            true, // removPrivate
            false, // removeNonPersistent
            true // removeSize
        );
    }

    //------------------------------------------------------------------------
    protected void removeAttributes(
        MappedRecord object, 
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

    //------------------------------------------------------------------------
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
    private void setLockAssertion(
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
    private void setLockAssertion(
        MappedRecord object
    ) throws ServiceException{
        Object_2Facade facade = Facades.asObject(object);
        String objectClass = facade.getObjectClass();
        Model_1_0 model = getModel();
        if(
        	model.isSubtypeOf(objectClass, "org:openmdx:base:Removable") &&
        	!this.nonPersistentFeatures.contains("org:openmdx:base:Removable:removedAt")
        ) {
            setLockAssertion(facade, "removedAt");
        } else if(model.isSubtypeOf(objectClass, "org:openmdx:base:Modifiable")) {
            setLockAssertion(facade, "modifiedAt");
        }
    }
    
    //------------------------------------------------------------------------
    protected void completeObject(
        MappedRecord object
    ) throws ServiceException {
        this.removePrivateAttributes(object);
        this.setLockAssertion(object);
        this.normalizeDateTimeValues(object);
    }

    //------------------------------------------------------------------------
    protected void completeReply(
        DataproviderReply reply
    ) throws ServiceException {
        for(MappedRecord object : reply.getObjects()) {
            this.completeObject(object);
        }
    }

    //---------------------------------------------------------------------------
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

    //---------------------------------------------------------------------------
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

    //---------------------------------------------------------------------------
    protected DbObject getDbObject(
        Connection conn,
        Path accessPath,
        FilterProperty[] filter,
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

    //---------------------------------------------------------------------------
    private DbObject getDbObject(
        Connection conn,
        DbObjectConfiguration dbObjectConfiguration,
        Path accessPath,
        FilterProperty[] filter,
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
                            new BasicException.Parameter("filter", (Object[])filter),
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
                    new BasicException.Parameter("filter", (Object[])filter)
                );            
            }
        }

        if(configuration == null) {
            Path referencedType = adjustedAccessPath.size() % 2 == 0 ? adjustedAccessPath : adjustedAccessPath.getParent(); 
            configuration = this.configuration.getDbObjectConfiguration(
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
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_NON_INDEXED.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObjectNonIndexed(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2_NON_INDEXED.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2NonIndexed(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_PARENT_RID_ONLY.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObjectParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2_PARENT_RID_ONLY.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2ParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_NON_INDEXED_PARENT_RID_ONLY.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObjectNonIndexedParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED2_NON_INDEXED_PARENT_RID_ONLY.equals(configuration.getDbObjectFormat())) {
            return new SlicedDbObject2NonIndexedParentRidOnly(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_WITH_ID_AS_KEY.equals(configuration.getDbObjectFormat())) {
            return new DBOSlicedWithIdAsKey(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_WITH_PARENT_AND_ID_AS_KEY.equals(configuration.getDbObjectFormat())) {
            return new DBOSlicedWithParentAndIdAsKey(
                this,
                conn,
                configuration,
                adjustedAccessPath,
                isExtent,
                isQuery
            );
        }
        else {
            try {
                return Classes.<DbObject>getApplicationClass(
                    configuration.getDbObjectFormat()
                ).getConstructor(
                    AbstractDatabase_1.class,
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
            } catch(Exception e) {
                throw new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE, 
                    "can not create DbObject",
                    new BasicException.Parameter("path", adjustedAccessPath),
                    new BasicException.Parameter("type", configuration.getDbObjectFormat())
                );
            }
        }
    }

    //---------------------------------------------------------------------------
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

    //---------------------------------------------------------------------------
    protected Connection getConnection(
        DataproviderRequest request
    ) throws ServiceException, SQLException {
        return getDataSource(request).getConnection();
    }

    //---------------------------------------------------------------------------
    protected void closeConnection(
        Connection conn
    ) throws ServiceException{
        if(conn != null) {
            try {
                conn.close();
            }
            catch(Exception e) {
                throw new ServiceException(e);
            }
        }
    }

    //---------------------------------------------------------------------------
    public PreparedStatement prepareStatement(
        Connection conn,
        String statement
    ) throws SQLException {
        return AbstractDatabase_1.this.prepareStatement(
            conn,
            statement,
            false
        );
    }
    
    //---------------------------------------------------------------------------
    protected MappedRecord getPartialObject(
        Connection conn,
        Path path,
        short attributeSelector,
        Map attributeSpecifiers,
        boolean objectClassAsAttribute,
        DbObject dbObject,
        boolean primaryColumns,
        String objectClass
    ) throws ServiceException {
        PreparedStatement ps = null;
        String currentStatement = null;
        ResultSet rs = null;
        List<Object> statementParameters = null;
        try {
            String view1 = AbstractDatabase_1.this.getView(
                conn,
                dbObject,
                null,
                VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY,
                null,
                null
            );
            String view2 = AbstractDatabase_1.this.getView(
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
                }
                else {            
                    statement = view1.startsWith("SELECT") 
                    ? "SELECT " + dbObject.getHint() + " vm.* FROM (" + view2 + ") vm" 
                        : "SELECT " + dbObject.getHint() + " vm.* FROM " + view2 + " vm";                 
                    statement += 
                        " INNER JOIN ";
                    statement += view1.startsWith("SELECT") 
                    ? "(" + view1 + ") v" 
                        : view1 + " v";
                    statement += 
                        " ON vm." + OBJECT_ID + " = v." + dbObject.getConfiguration().getDbObjectsForQueryJoinColumn();
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
                    statement += 
                        " ORDER BY " + prefix + dbObject.getIndexColumn();
                }
            }
            else {
                statement += 
                    " ORDER BY " + prefix + OBJECT_IDX;
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
                    AbstractDatabase_1.this.setPreparedStatementValue(
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
                AbstractDatabase_1.this.setPreparedStatementValue(
                    conn,
                    ps, 
                    pos++, 
                    objectIdValue
                );
            }      
            rs = AbstractDatabase_1.this.executeQuery(
                ps,
                currentStatement,
                statementParameters
            );
            MappedRecord replyObj = AbstractDatabase_1.this.getObject(
                conn, 
                path,
                attributeSelector,
                attributeSpecifiers,
                objectClassAsAttribute,
                dbObject, 
                rs,
                objectClass,
                primaryColumns // check for valid identity only if primary columns are fetched
            );      
            rs.close(); rs = null;
            ps.close(); ps = null;
            return replyObj;
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", path.toXRI()),
                new BasicException.Parameter("statement", currentStatement),
                new BasicException.Parameter("parameters", statementParameters),
                new BasicException.Parameter("sqlErrorCode", ex.getErrorCode()), 
                new BasicException.Parameter("sqlState", ex.getSQLState())
            );
        }
        finally {
            try {
                if(rs != null) rs.close();
            } catch(Throwable ex) {
                // ignore
            }
            try {
                if(ps != null) ps.close();
            } catch(Throwable ex) {
                // ignore
            }
        }
    }

    //---------------------------------------------------------------------------
    public DataproviderReply get(
        Connection conn,
        ServiceHeader header,
        Path path,
        short attributeSelector,
        Map attributeSpecifiers,
        boolean objectClassAsAttribute,
        DataproviderReply reply
    ) throws ServiceException {
        MappedRecord replyObj = null;
        try {
            DbObject dbObject = null;
            try {
                dbObject = AbstractDatabase_1.this.createDbObject(
                    conn,
                    path,
                    true
                );
            }
            catch(ServiceException e) {
                if(e.getCause().getExceptionCode() != BasicException.Code.NOT_FOUND) {
                    throw e;
                }
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND, 
                    "object not found",
                    new BasicException.Parameter("path", path)
                );
            }
            // Get primary attributes
            boolean hasSecondaryDbObject =
                (dbObject.getConfiguration().getDbObjectForQuery2() != null) ||
                (dbObject.getConfiguration().getDbObjectForUpdate2() != null);          
            replyObj = this.getPartialObject(
                conn,
                path,
                attributeSelector,
                attributeSpecifiers,
                objectClassAsAttribute,
                dbObject,
                true, // primaryColumns
                null
            );
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
                    Object_2Facade.getObjectClass(replyObj)
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
        } catch(ServiceException exception) {
            throw exception;
        } catch(NullPointerException exception) {
            exception.printStackTrace();
            throw new ServiceException(exception);
        } catch(Exception exception) {
            throw new ServiceException(
                exception, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC, 
                exception.toString()
            );
        }
        reply.getResult().add(replyObj);
        this.completeReply(
            reply
        );
        return reply;
    }

    //---------------------------------------------------------------------------
    
    /**
     * Explicit test for duplicates.
     */
    private void checkForDuplicates(
        Connection conn,
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException{
        try {
            this.get(
                conn,
                header,
                request.path(),
                AttributeSelectors.ALL_ATTRIBUTES,
                Collections.EMPTY_MAP,
                false, // objectClassAsAttribute
                new DataproviderReply()
            );
        } 
        catch(ServiceException e) {
            if(e.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                return;
            } else {
                throw e;
            }
        } 
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.DUPLICATE, 
            "duplicate object",
            new BasicException.Parameter("path", request.path())
        );
    }

    /**
     * Makes a new object persistent. 
     */
    public void create(
        Connection conn,
        ServiceHeader header,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        try {
            if(this.ignoreCheckForDuplicates) {
                //
                // WARNING: the implementation assumes that
                // 'unique constraints' are set in order that INSERTs throw a 'duplicate row'
                // exception which is then mapped to a DUPLICATE ServiceException. 
                //
            } 
            else {
                this.checkForDuplicates(
                    conn,
                    header,
                    request
                );
            }
            // Partition object into slices and create all slices
            DbObject dbObject = this.createDbObject(
                conn,
                request.path(),
                false
            );
            MappedRecord clone = Object_2Facade.cloneObject(request.object());
            this.removeAttributes(
                clone, 
                false, // removePrivate
                true, // removeNonPersistent
                true // removeSize
            );
            MappedRecord[] partitionedObjects = dbObject.sliceAndNormalizeObject(clone, true);            
            for(
                int i = 0; 
                i < partitionedObjects.length;
                i++
            ) {      
                dbObject = this.createDbObject(
                    conn,
                    Object_2Facade.getPath(partitionedObjects[i]),
                    false
                );
                dbObject.createObjectSlice(
                    i,
                    Object_2Facade.getObjectClass(request.object()),
                    partitionedObjects[i]
                );
            }
            if(reply.getResult() != null) {
                reply.getResult().add(
                    request.object()
                );
            }
        } catch(ServiceException exception) {
            throw exception;
        } catch(NullPointerException exception) {
            exception.printStackTrace();
            throw new ServiceException(exception);
        } catch(RuntimeException exception) {
            throw new ServiceException(
                exception, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC, 
                exception.toString()
            );
        }
    }

    //---------------------------------------------------------------------------
    protected static String getVersionField(
        String lockAssertion
    ) throws ServiceException{
        int i = lockAssertion.indexOf('=');
        if(i < 1) throw new  ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "Lock assertion must be of the form <columnName>=<expectedVersion>",
            new BasicException.Parameter("lockAssertion", lockAssertion)
        );
        return lockAssertion.substring(0, i);
    }
        
    //---------------------------------------------------------------------------
    protected static Object getVersionValue(
        String lockAssertion,
        String versionField
    ){
        String rawValue = lockAssertion.substring(versionField.length()+1);
        return "".equals(rawValue) ? null : DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(rawValue);
    }
    
    // --------------------------------------------------------------------------
    public class LayerInteraction extends OperationAwareLayer_1.LayerInteraction {
        
        //---------------------------------------------------------------------------
        public LayerInteraction(
            javax.resource.cci.Connection connection
        ) throws ResourceException {
            super(connection);
        }
        
        //---------------------------------------------------------------------------
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {        
            ServiceHeader header = this.getServiceHeader();
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
        
            SysLog.detail("> get", request.object());    
            configuration.load();        
            
            Connection conn = null;
            
            try {  
                conn = AbstractDatabase_1.this.getConnection(request);
                AbstractDatabase_1.this.get(
                    conn,
                    header,
                    request.path(),
                    request.attributeSelector(),
                    // Attribute specifiers are ignored by Database_1 except in case of attributeSelector==SPECIFIED_AND_SYSTEM_ATTRIBUTES.
                    // Save the possibly expensive call to attributeSpecifierAsMap
                    request.attributeSelector() == AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES || request.attributeSelector() == AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES ? request.attributeSpecifierAsMap() : Collections.emptyMap(),
                    false, // objectClassAsAttribute
                    reply
                );
            }
            catch(Exception e) {
                throw new ServiceException(e);
            }
            finally {
                try {
                    AbstractDatabase_1.this.closeConnection(conn);
                }
                catch(Throwable ex) {
                    // ignore
                }
            }
            return true;
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            
            SysLog.detail("> find", request.object());
            AbstractDatabase_1.this.configuration.load();
    
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
    
                conn = AbstractDatabase_1.this.getConnection(request);
                DbObject dbObject = null;
    
                /**
                 * prepare SELECT statement
                 */
                SysLog.trace(DataproviderOperations.toString(DataproviderOperations.ITERATION_START));
                try {
                    dbObject = AbstractDatabase_1.this.getDbObject(
                        conn,
                        request.path(),
                        request.attributeFilter(),
                        true
                    );
                }
                catch(ServiceException e) {
                    if(e.getCause().getExceptionCode() != BasicException.Code.NOT_FOUND) {
                        throw e;
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
                                    e.getMessage(),
                                    request.path(),
                                    request.attributeFilter()
                                }
                            )
                        )
                    );
                    e.log();
                    reply.setHasMore(
                        Boolean.FALSE
                    );
                    reply.setTotal(
                        Integer.valueOf(0)
                    );
                    SysLog.detail("< find");
                    AbstractDatabase_1.this.completeReply(reply);
                    return true;
                }
                // Check for query filter context
                String queryFilterContext = null;
                for(
                    int i = 0, iLimit = request.attributeFilter().length;
                    i < iLimit;
                    i++
                ) {
                    FilterProperty p = request.attributeFilter()[i];
                    if(
                        p.name().startsWith(SystemAttributes.CONTEXT_PREFIX) &&
                        p.name().endsWith(SystemAttributes.OBJECT_CLASS) &&
                        Database_1_Attributes.QUERY_FILTER_CLASS.equals(p.getValue(0))
                    ) {
                        queryFilterContext = p.name().substring(0, p.name().indexOf(SystemAttributes.OBJECT_CLASS));
                        break;
                    }
                }
    
                // Get attribute and query filter. The query filter is passed as
                // FilterProperty with context prefix QUERY_FILTER_CONTEXT
                List<String> orderBy = new ArrayList<String>();
                List<FilterProperty> filterProperties = new ArrayList<FilterProperty>();
                String queryFilterClause = null;
                String columnSelector = DEFAULT_COLUMN_SELECTOR;
                Map<String,List<Object>> queryFilterParameters = new HashMap<String,List<Object>>();
                for(FilterProperty p: request.attributeFilter()) {
                    // The filter property 'identity' requires special handling. It
                    // is mapped to the filter property 'object_oid operator values'
                    // This mapping is not required in case of an extent search because
                    // dbObject is already correctly prepared
                    if(SystemAttributes.OBJECT_IDENTITY.equals(p.name())) {
                        if(!request.path().isLike(EXTENT_REFERENCES)) {
                            filterProperties.add(
                                dbObject.mapToIdentityFilterProperty(p)
                            );
                        }
                    }
                    // Query filter context
                    else if((queryFilterContext != null) && p.name().startsWith(queryFilterContext)) {
                        // Clause
                        if((queryFilterContext + Database_1_Attributes.QUERY_FILTER_CLAUSE).equals(p.name())) {
                            queryFilterClause = (String)p.getValue(0);
                            countResultSet = queryFilterClause.indexOf(Database_1_Attributes.HINT_COUNT) >= 0;
                            {
                                // !COLUMNS
                                int start = queryFilterClause.indexOf(Database_1_Attributes.HINT_COLUMN_SELECTOR);
                                if(start >= 0) {
                                    int end = queryFilterClause.indexOf("*/", start);
                                    columnSelector = queryFilterClause.substring(
                                        start + Database_1_Attributes.HINT_COLUMN_SELECTOR.length(),
                                        end
                                    );
                                    queryFilterClause =
                                        queryFilterClause.substring(0, start) + 
                                        queryFilterClause.substring(end + 2);                                
                                }
                            }
                            // !ORDER BY
                            {
                                int start = queryFilterClause.indexOf(Database_1_Attributes.HINT_ORDER_BY);
                                if(start >= 0) {
                                    int end = queryFilterClause.indexOf("*/", start);
                                    String orderByClause = queryFilterClause.substring(
                                        start + Database_1_Attributes.HINT_ORDER_BY.length(),
                                        end
                                    );
                                    StringTokenizer tokenizer = new StringTokenizer(orderByClause, ",", false);
                                    while(tokenizer.hasMoreTokens()) {
                                        orderBy.add(tokenizer.nextToken());
                                    }
                                    queryFilterClause =
                                        queryFilterClause.substring(0, start) + 
                                        queryFilterClause.substring(end + 2);                                
                                }
                            }
                        }
                        // Parameters
                        else {
                            String paramName = p.name().substring(queryFilterContext.length());
                            queryFilterParameters.put(
                                paramName,
                                Arrays.asList(p.getValues())
                            );                      
                        }
                    }
                    else if(OBJECT_INSTANCE_OF.equals(p.name())) {
                        if(
                            (p.operator() == ConditionType.IS_IN.code()) &&
                            (p.quantor()  == Quantifier.THERE_EXISTS.code())
                        ) {
                            FilterProperty mappedFilterProperty = AbstractDatabase_1.this.mapInstanceOfFilterProperty(
                                request,
                                (Collection<String>) ((Collection<?>) Arrays.asList(p.getValues()))
                            );
                            if(mappedFilterProperty != null) {
                                filterProperties.add(mappedFilterProperty);
                            } 
                        } 
                        else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "Property " + OBJECT_INSTANCE_OF + " only accepts condition " + ConditionType.IS_IN + " and quantor " + Quantifier.THERE_EXISTS,
                                new BasicException.Parameter("ispec", ispec),
                                new BasicException.Parameter("input", input)
                            );
                        }                                     
                    }
                    // Attribute
                    else {
                        filterProperties.add(p);              
                    }
                }
                lastPosition = -1;
                lastRowCount = -1;
                // Mixins
                Set<String> mixins = new HashSet<String>();
                
                // ORDER BY attributes
                for(
                    int i = 0; 
                    i < request.attributeSpecifier().length; 
                    i++
                ) {
                    // Add to orderBy set unless the order is UNSORTED
                    if(request.attributeSpecifier()[i].order() != SortOrder.UNSORTED.code()) {
                        String attributeName = request.attributeSpecifier()[i].name();
                        orderBy.add(attributeName);
                        mixins.add(attributeName);
                    }
                }
                // Prepare filter properties stored in primary dbObject
                ModelElement_1_0 referencedTypeDef = this.getModel().getTypes(dbObject.getReference())[2];
                List<FilterProperty> primaryFilterProperties = AbstractDatabase_1.this.getPrimaryFilterProperties(
                    referencedTypeDef,
                    filterProperties
                );
                // Add filter properties which map to embedded features
                for(Iterator<FilterProperty> i = filterProperties.iterator(); i.hasNext(); ) {
                    FilterProperty p = i.next();
                    if(AbstractDatabase_1.this.embeddedFeatures.containsKey(p.name())) {
                        primaryFilterProperties.add(p);
                    }
                }
                // Add primary filter properties to mixins
                List<ModelElement_1_0> primaryFilterPropertyDefs = AbstractDatabase_1.this.getFilterPropertyDefs(
                    referencedTypeDef, 
                    primaryFilterProperties
                );
                for(int i = 0; i < primaryFilterProperties.size(); i++) {
                    FilterProperty filterProperty = primaryFilterProperties.get(i);
                    ModelElement_1_0 filterPropertyDef = primaryFilterPropertyDefs.get(i);
                    if(
                        (filterPropertyDef == null) ||
                        !filterPropertyDef.isReferenceType() ||
                        this.getModel().referenceIsStoredAsAttribute(filterPropertyDef)
                    ) {
                        mixins.add(filterProperty.name());
                    }
                }
                // View returning primary attributes. Allows sorting and
                // filtering with single-valued filter properties
                String dbObjectHint = null;
                int posDbObjectHint = queryFilterClause == null ? -1 : queryFilterClause.indexOf(Database_1_Attributes.HINT_DBOBJECT);
                if(posDbObjectHint >= 0) {
                    dbObjectHint = queryFilterClause.substring(
                        posDbObjectHint + Database_1_Attributes.HINT_DBOBJECT.length(),
                        queryFilterClause.indexOf("*/", posDbObjectHint)
                    );
                }                
                String view1WithMixinAttributes = AbstractDatabase_1.this.getView(
                    conn,
                    dbObject,
                    dbObjectHint,
                    VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY,
                    columnSelector,
                    mixins
                );
                // View returning multi-valued columns which allows filtering
                // of multi-valued filter properties
                String view2ForQuery = AbstractDatabase_1.this.getView(
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
                ModelElement_1_0 referencedType = this.getModel().getTypes(dbObject.getReference())[2];                 
                AbstractDatabase_1.this.filterToSqlClause(
                    conn, 
                    dbObject, 
                    "v",
                    view1WithMixinAttributes, 
                    view2ForQuery, 
                    joinColumn, 
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
    
                // oid is not filled as statementParameters because it its value is varying
                // in case of iteration requests. It is filled in explicitly (see below).
    
                // query filter
                if(queryFilterClause != null) {
                    int pos = 0;
                    while(
                        (pos < queryFilterClause.length()) && 
                        ((pos = queryFilterClause.indexOf("?", pos)) >= 0)
                    ) {
                        int index = Integer.valueOf(queryFilterClause.substring(pos+2, pos+3)).intValue();
                        if(queryFilterClause.startsWith("?s", pos)) {
                            statementParameters.add(
                                AbstractDatabase_1.this.sqlWildcards.fromJDO(((String)queryFilterParameters.get("stringParam").get(index)))
                            );
                        }
                        else if(queryFilterClause.startsWith("?i", pos)) {
                            statementParameters.add(
                                queryFilterParameters.get("integerParam").get(index)
                            );
                        }
                        else if(queryFilterClause.startsWith("?n", pos)) {
                            statementParameters.add(
                                queryFilterParameters.get("decimalParam").get(index)
                            );
                        }
                        else if(queryFilterClause.startsWith("?b", pos)) {
                            statementParameters.add(
                                queryFilterParameters.get("booleanParam").get(index)
                            );
                        }
                        else if(queryFilterClause.startsWith("?d", pos)) {
                        	// TODO CR20019719 verify whether replacement is done
                            statementParameters.add(
                                queryFilterParameters.get("dateParam").get(index)
                            );
                        }
                        else if(queryFilterClause.startsWith("?t", pos)) {
                            statementParameters.add(
                                queryFilterParameters.get("dateTimeParam").get(index)
                            );
                        }
                        pos++;
                    }
                    statement += " AND (";
                    statement += queryFilterClause.replaceAll("(\\?[sinbdt]\\d)", "?");
                    statement += ")";
                    String databaseProductName = conn.getMetaData().getDatabaseProductName();             
                    if(databaseProductName.startsWith("DB2")) {
                        statement = statement.replace("LIKE UPPER(?)", "LIKE ?"); 
                    }
                }
    
                // positive attribute filter
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
                for(
                    int i = 0; 
                    i < request.attributeSpecifier().length; 
                    i++
                ) {
                    AttributeSpecifier specifier = request.attributeSpecifier()[i];
                    // only add to ORDER set if specified order
                    if(request.attributeSpecifier()[i].order() != SortOrder.UNSORTED.code()) {
                        if(!hasOrderBy) statement += " ORDER BY"; 
                        boolean viewIsIndexed = dbObject.getIndexColumn() != null;              
                        statement += hasOrderBy ? ", " : " ";
                        // order on mixin view (vm.) in case of indexed slices, otherwise on primary view (v.)
                        statement += (viewIsIndexed ? "vm." : "v.") + getColumnName(conn, specifier.name(), 0, false, true, false) + (specifier.order() == SortOrder.DESCENDING.code() ? " DESC" : " ASC");
                        hasOrderBy = true;
                    }
                }
                // Order on reference and object columns only required if result set is indexed
                if(dbObject.getIndexColumn() != null) {
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
                    statement += hasOrderBy ? ", " : " ";
                    statement += "v." + dbObject.getIndexColumn();
                    hasOrderBy = true;
                }
                // Prepare and ...
                ps = AbstractDatabase_1.this.prepareStatement(
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
                        AbstractDatabase_1.this.setPreparedStatementValue(
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
                rs = AbstractDatabase_1.this.executeQuery(
                    ps,
                    statement.toString(),
                    statementParameters
                );
                // get selected objects
                List<MappedRecord> objects = new ArrayList<MappedRecord>();
                int requestedSize = request.size();
                int replyPosition = request.position();
                if(request.direction() == SortOrder.DESCENDING.code()) {
                    if(requestedSize > replyPosition) requestedSize = replyPosition + 1;
                    replyPosition = replyPosition + 1 - requestedSize;
                }
                boolean hasMore = AbstractDatabase_1.this.getObjects(
                    conn,
                    dbObject,
                    rs,
                    objects,
                    request.attributeSelector(),
                    request.attributeSpecifierAsMap(),
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
                    if(request.attributeSelector() == AttributeSelectors.ALL_ATTRIBUTES) {
                        fetchAll = true;
                    }
                    else if(request.attributeSelector() == AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES) {
                        for(int j = 0; j < request.attributeSpecifier().length; j++) {
                            if(!facade2.getValue().keySet().contains(request.attributeSpecifier()[j].name())) {
                                fetchAll = true;
                                break;
                            }
                        }
                    }                
                    DbObject dbObject2 = AbstractDatabase_1.this.createDbObject(
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
                        for(MappedRecord object : objects){
                            dbObject2 = AbstractDatabase_1.this.createDbObject(
                                conn, 
                                Object_2Facade.getPath(object), 
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
                        statement2 += ", v." + AbstractDatabase_1.this.OBJECT_IDX;
                        PreparedStatement ps2 = AbstractDatabase_1.this.prepareStatement(
                            conn,
                            currentStatement = statement2.toString()
                        );
                        for(
                            int i = 0, iLimit = statementParameters2.size(); 
                            i < iLimit; 
                            i++
                        ) {
                            AbstractDatabase_1.this.setPreparedStatementValue(
                                conn,
                                ps2,
                                i+1, 
                                statementParameters2.get(i)
                            );
                        }
                        ResultSet rs2 = AbstractDatabase_1.this.executeQuery(
                            ps2,
                            statement2.toString(),
                            statementParameters2
                        );
                        List<MappedRecord> objects2 = new ArrayList<MappedRecord>();
                        AbstractDatabase_1.this.getObjects(
                            conn,
                            dbObject2,
                            rs2,
                            objects2,
                            AttributeSelectors.ALL_ATTRIBUTES,
                            Collections.EMPTY_MAP,
                            false, // objectClassAsAttribute
                            0, -1, -1, requestedSize,
                            facade2.getObjectClass()
                        );
                        // Add attributes of objects2 to objects
                        Map<Path,MappedRecord> objects2AsMap = new HashMap<Path,MappedRecord>();
                        for(MappedRecord object2: objects2) {
                            objects2AsMap.put(
                                Object_2Facade.getPath(object2), 
                                object2
                            );
                        }
                        for(MappedRecord object: objects) {
                            MappedRecord object2 = objects2AsMap.get(Object_2Facade.getPath(object));
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
                lastRowCount = AbstractDatabase_1.this.resultSetGetRow(rs);
                rs.close(); rs = null;
                ps.close(); ps = null;
                SysLog.log(Level.FINE, "Sys|*** hasMore={0}|objects.size()={1}", hasMore, objects.size());
    
                // reply 
                reply.getResult().addAll(objects);
    
                // context.HAS_MORE
                reply.setHasMore(
                    Boolean.valueOf(hasMore)
                );
    
                // Calculate context.TOTAL only at iteration start
                if(request.operation() == DataproviderOperations.ITERATION_START) {      
                    // if !hasMore context.TOTAL = request.position() + objects.size()
                    if(!hasMore && ((request.position() == 0) || !objects.isEmpty())) {
                        reply.setTotal(
                            Integer.valueOf(request.position() + objects.size())
                        );
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
                            ps = AbstractDatabase_1.this.prepareStatement(
                                conn,
                                currentStatement = countStatement.toString()
                            );        
                            for(
                                int i = 0, iLimit = statementParameters.size(); 
                                i < iLimit; 
                                i++
                            ) {
                                AbstractDatabase_1.this.setPreparedStatementValue(
                                    conn,
                                    ps,
                                    i+1, 
                                    statementParameters.get(i)
                                );
                            }              
                            rs = AbstractDatabase_1.this.executeQuery(
                                ps,
                                countStatement.toString(),
                                statementParameters
                            );
                            if(rs.next()) {
                                reply.setTotal(
                                    Integer.valueOf(rs.getInt(1))
                                );
                            }
                            rs.close(); rs = null;
                            ps.close(); ps = null;
                        }
                    }
                }
            }
            catch(SQLException ex) {
                throw new ServiceException(
                    ex, 
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE, 
                    null,
                    new BasicException.Parameter("path", request.path()),
                    new BasicException.Parameter("statement", currentStatement),
                    new BasicException.Parameter("parameters", statementParameters),
                    new BasicException.Parameter("sqlErrorCode", ex.getErrorCode()), 
                    new BasicException.Parameter("sqlState", ex.getSQLState())
                    
                );
            }
            catch(ServiceException e) {
                throw e;
            }
            catch(Exception ex) {
                throw new ServiceException(
                    ex, 
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.GENERIC, 
                    ex.toString()
                );
            }
            finally {
                AbstractDatabase_1.this.closeConnection(conn);
            }
            SysLog.detail("< find");
            AbstractDatabase_1.this.completeReply(
                reply
            );
            return true;
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
        
            SysLog.detail("> create", request.object());
            AbstractDatabase_1.this.configuration.load();
            
            Connection conn = null; 
            
            try {
                conn = AbstractDatabase_1.this.getConnection(request);
                AbstractDatabase_1.this.create(
                    conn,
                    header,
                    request,
                    reply
                );
            }
            catch(Exception e) {
                throw new ServiceException(e);
            }
            finally {
                try {
                    AbstractDatabase_1.this.closeConnection(conn);
                }
                catch(Throwable ex) {
                    // ignore
                }
            }
            return true;
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
           
            SysLog.detail("> remove", request.object());
            AbstractDatabase_1.this.configuration.load();
            
            Connection conn = null;    
            String currentStatement = null;
            List<Object> statementParameters = null;
            MappedRecord replyObj = null;
            try {
                conn = AbstractDatabase_1.this.getConnection(request);
                Path accessPath = request.path();
                // Does object exist?
                replyObj = AbstractDatabase_1.this.get(
                    conn,
                    header,
                    accessPath,
                    AttributeSelectors.NO_ATTRIBUTES,
                    Collections.emptyMap(),
                    false, // objectClassAsAttribute
                    this.newDataproviderReply()
                ).getObject();
                // Remove object ...
                AbstractDatabase_1.this.createDbObject(
                    conn,
                    request.path(),
                    true
                ).remove();
                // ... and its composites
                Map<Path,DbObjectConfiguration> processedDbObjectConfigurations = new HashMap<Path,DbObjectConfiguration>();
                for(DbObjectConfiguration dbObjectConfiguration : AbstractDatabase_1.this.configuration.getDbObjectConfigurations()) {
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
                            AbstractDatabase_1.this.createDbObject(
                                conn,
                                dbObjectConfiguration,
                                request.path(),
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
                if(!AbstractDatabase_1.this.useNormalizedReferences()) {
                    statementParameters = new ArrayList<Object>();
                    PreparedStatement ps = AbstractDatabase_1.this.prepareStatement(
                        conn,
                        currentStatement = 
                            "DELETE FROM " + AbstractDatabase_1.this.namespaceId + "_" + AbstractDatabase_1.T_REF + 
                            " WHERE " + AbstractDatabase_1.this.getSelectReferenceIdsFromRefTableClause(conn, request.path().getChild("%"), statementParameters)
                    );
                    for(int i = 0; i < statementParameters.size(); i++) {
                        AbstractDatabase_1.this.setPreparedStatementValue(
                            conn,
                            ps, 
                            i+1, 
                            statementParameters.get(i)
                        );
                    }
                    executeUpdate(ps, currentStatement, statementParameters);
                    ps.close(); ps = null;
                }
            }
            catch(SQLException ex) {
                throw new ServiceException(
                    ex, 
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE, 
                    null,
                    new BasicException.Parameter("path", request.path()),
                    new BasicException.Parameter("errorCode", ex.getErrorCode()),
                    new BasicException.Parameter("statement", currentStatement),
                    new BasicException.Parameter("parameters", statementParameters),
                    new BasicException.Parameter("sqlErrorCode", ex.getErrorCode()), 
                    new BasicException.Parameter("sqlState", ex.getSQLState())
                );
            }
            finally {
                try {
                    AbstractDatabase_1.this.closeConnection(conn);
                }
                catch(Exception ex) {
                    // ignore
                }
            }
            if(reply.getResult() != null) {
                reply.getResult().add(replyObj);
                AbstractDatabase_1.this.completeReply(reply);
            }
            return true;
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            SysLog.detail("> replace", request.object());
            AbstractDatabase_1.this.configuration.load();
            
            PreparedStatement ps = null;
            String currentStatement = null;
            Connection conn = null;
            List<Object> objectIdValues = null;
            try {
                conn = AbstractDatabase_1.this.getConnection(request);
                MappedRecord object = request.object();            
                DbObject dbObject = AbstractDatabase_1.this.createDbObject(
                    conn,
                    Object_2Facade.getPath(object),
                    true
                );
                MappedRecord newValue = Object_2Facade.getValue(object);
                byte[] expectedVersion = getVersion(object);
                // Get current object with ALL_ATTRIBUTES. objectClassAsAttribute=true
                // asserts that empty rows (all columns with null values) are not truncated
                MappedRecord obj = AbstractDatabase_1.this.get(
                    conn,
                    header,
                    request.path(),
                    AttributeSelectors.ALL_ATTRIBUTES,
                    Collections.EMPTY_MAP,
                    true, // objectClassAsAttribute
                    this.newDataproviderReply()
                ).getObject();
                MappedRecord oldValue = Object_2Facade.getValue(obj);
                if(!newValue.isEmpty()) {
                    MappedRecord[] oldSlices = dbObject.sliceAndNormalizeObject(obj, false);
                    // Replace attribute values
                    oldValue.putAll(newValue);
                    AbstractDatabase_1.this.removeAttributes(
                        obj, 
                        true, // removePrivate
                        true, // removeNonPersistent
                        true // removeSize
                    );
                    MappedRecord[] newSlices = dbObject.sliceAndNormalizeObject(obj, true);
                    // Replace existing slices
                    for(
                        int i = 0; 
                        i < java.lang.Math.min(oldSlices.length, newSlices.length);
                        i++
                    ) {
                        if(!newSlices[i].equals(oldSlices[i])) {
                            dbObject.replaceObjectSlice(
                                i,
                                newSlices[i],
                                oldSlices[i], 
                                i == 0 ? getLockAssertion(expectedVersion) : null
                            );
                        }
                    }
                    // Remove extra old slices
                    if(oldSlices.length > newSlices.length) {
                        boolean isIndexed;
                        if(dbObject.getConfiguration().getDbObjectForUpdate2() != null) {
                            isIndexed = true;
                            ps = AbstractDatabase_1.this.prepareStatement(
                                conn,
                                currentStatement = 
                                    "DELETE FROM " + dbObject.getConfiguration().getDbObjectForUpdate2() + 
                                    " WHERE " + 
                                    AbstractDatabase_1.this.removeViewPrefix(
                                        dbObject.getReferenceClause() + 
                                        " AND " + dbObject.getObjectIdClause() + 
                                        " AND (" + OBJECT_IDX + " >= ?)"
                                    )
                            );            
                        }
                        else {
                            isIndexed = dbObject.getIndexColumn() != null;
                            ps = AbstractDatabase_1.this.prepareStatement(
                                conn,
                                currentStatement = 
                                    "DELETE FROM " + dbObject.getConfiguration().getDbObjectForUpdate1() + 
                                    " WHERE " + 
                                    AbstractDatabase_1.this.removeViewPrefix(
                                        dbObject.getReferenceClause() + 
                                        " AND " + dbObject.getObjectIdClause() + 
                                        (isIndexed ? " AND (" + dbObject.getIndexColumn() + " >= ?)" : "")
                                    )
                            );
                        }
                        int pos = 1;
                        List<Object> referenceValues = dbObject.getReferenceValues();
                        for(Object referenceValue : referenceValues) {
                            AbstractDatabase_1.this.setPreparedStatementValue(
                                conn,
                                ps, 
                                pos++, 
                                referenceValue
                            );
                        }
                        objectIdValues = dbObject.getObjectIdValues();
                        for(Object objectIdValue : objectIdValues) {
                            AbstractDatabase_1.this.setPreparedStatementValue(
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
                } else if(expectedVersion != null){
                    byte[] actualVersion = getVersion(obj);
                    if(Arrays.equals(expectedVersion, actualVersion)) {
                        removePrivateAttributes(obj);
                    } else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                            "The object has been modified in the meanwhile",
                            new BasicException.Parameter("path", Object_2Facade.getPath(object)),
                            new BasicException.Parameter("expected", expectedVersion),
                            new BasicException.Parameter("actual", actualVersion)
                        );
                    }
                }
                return true;
            }
            catch(SQLException ex) {
                throw new ServiceException(
                    ex, 
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE, 
                    null,
                    new BasicException.Parameter("path", request.path()),
                    new BasicException.Parameter("statement", currentStatement),
                    new BasicException.Parameter("parameters", objectIdValues),
                    new BasicException.Parameter("sqlErrorCode", ex.getErrorCode()), 
                    new BasicException.Parameter("sqlState", ex.getSQLState())
    
                );
            }
            catch(ServiceException e) {
                throw e;
            }
            catch(Exception ex) {
                throw new ServiceException(
                    ex, 
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.GENERIC, 
                    ex.toString()
                );
            }
            finally {
                try {
                    if(ps != null) ps.close();
                } catch(Throwable ex) {
                    // ignore
                }
                try {
                    AbstractDatabase_1.this.closeConnection(conn);
                } catch(Throwable ex) {
                    // ignore
                }
            }
        }
    }

    //---------------------------------------------------------------------------
    protected Blob createBlob(
        InputStream stream,
        long length
    ) throws IOException{
        return new GenericBlob(
            stream, 
            length, 
            this.temporaryFiles, 
            getStreamBufferDirectory(),
            this.uidAsString(),
            getChunkSize()
        );
    }

    //---------------------------------------------------------------------------
    protected Clob createClob(
        Reader stream,
        long length
    ) throws IOException{
        return new GenericClob(
            stream, 
            length, 
            this.temporaryFiles, 
            getStreamBufferDirectory(), 
            this.uidAsString(), 
            getChunkSize()
        );
    }

    //---------------------------------------------------------------------------

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
        try {
            return LayerConfigurationEntries.DATETIME_TYPE_STANDARD.equals(this.dateTimeType)? this.jdbcDriverSqlProperties.getProperty(
                connection.getMetaData().getDatabaseProductName() + ".DATETIME.TYPE.STANDARD",
                LayerConfigurationEntries.DATETIME_TYPE_CHARACTER
            ) : this.dateTimeType;
        } catch (SQLException exception) {
            throw new ServiceException(exception);
        }       
    }

    /**
     * The escape clause

     * @param connection
     * 
     * @return the escape clause
     * 
     * @throws ServiceException
     */
    protected String getEscapeClause(
        Connection connection
    ) throws ServiceException{      
        try {
            return this.jdbcDriverSqlProperties.getProperty(
                connection.getMetaData().getDatabaseProductName() + ".ESCAPE.CLAUSE",
                "" // "ESCAPE '\\'"
            );
        } catch (SQLException exception) {
            throw new ServiceException(exception);
        }       
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
        try {
            return LayerConfigurationEntries.DATE_TYPE_STANDARD.equals(this.dateType)? this.jdbcDriverSqlProperties.getProperty(
                connection.getMetaData().getDatabaseProductName() + ".DATE.TYPE.STANDARD",
                LayerConfigurationEntries.DATETIME_TYPE_CHARACTER
            ) : this.dateType;
        } catch (SQLException exception) {
            throw new ServiceException(exception);
        }       
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
        try {
            return LayerConfigurationEntries.TIME_TYPE_STANDARD.equals(this.timeType)? this.jdbcDriverSqlProperties.getProperty(
                connection.getMetaData().getDatabaseProductName() + ".TIME.TYPE.STANDARD",
                LayerConfigurationEntries.TIME_TYPE_CHARACTER
            ) : this.timeType;
        } catch (SQLException exception) {
            throw new ServiceException(exception);
        }       
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
        try {
            return LayerConfigurationEntries.BOOLEAN_TYPE_STANDARD.equals(this.booleanType)
            ? this.jdbcDriverSqlProperties.getProperty(
                connection.getMetaData().getDatabaseProductName() + ".BOOLEAN.TYPE.STANDARD",
                LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER
            ) 
            : this.booleanType;
        } 
        catch (SQLException exception) {
            throw new ServiceException(exception);
        }       
    }

    //---------------------------------------------------------------------------
    /**
     * Retrieve the version
     *  
     * @param a PUT request
     * 
     * @return the version
     */
    protected static byte[] getVersion(
        MappedRecord request
    ){
        return (byte[])Object_2Facade.getVersion(request);
    }
    
    //---------------------------------------------------------------------------
    /**
     * Build the lock assertion
     * 
     * @param version an encoded lock assertion
     * 
     * @return the decoded lock assertion
     */
    protected String getLockAssertion(
        byte[] version
    ){
        return version == null || version.length == 0 ? null : UnicodeTransformation.toString(
            version, 
            0, // offset
            version.length
        );
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


    //---------------------------------------------------------------------------
    protected FilterProperty mapInstanceOfFilterProperty(
        DataproviderRequest request,
        Collection<String> qualifiedClassNames
    ) throws ServiceException {
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
                subClasses.add(((Path)path).getBase());
            }
        }
        return new FilterProperty(
            Quantifier.THERE_EXISTS.code(),
            OBJECT_CLASS,  
            ConditionType.IS_IN.code(),
            subClasses.toArray()
        );
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------

    protected static final String T_REF = "REF";
    protected static final String UNDEF_OBJECT_CLASS = "#undef";
    protected static final String SIZE_SUFFIX = "_";
    
    protected BooleanMarshaller booleanMarshaller;
    protected Marshaller durationMarshaller;
    protected XMLGregorianCalendarMarshaller calendarMarshaller;
    protected String dateTimeType;
    protected String timeType;
    protected String dateType;
    protected String booleanType;

    protected static final int ROUND_UP_TO_MAX_SCALE = 15;

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

    protected static final String DEFAULT_OID_SUFFIX = "_objectid";
    protected static final String DEFAULT_RID_SUFFIX = "_referenceid";
    protected static final String DEFAULT_RSX_SUFFIX = "_referenceIdSuffix";
    protected static final String DEFAULT_COLUMN_SELECTOR = "v.*";
    protected static final String DEFAULT_PRIVATE_ATTRIBUTE_PREFIX = "p$$";
    
    private String privateAttributesPrefix;
    private String objectIdAttributesSuffix;
    private String referenceIdAttributesSuffix;
    private String referenceIdSuffixAttributesSuffix;

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
    protected List<DataSource> dataSources;

    /**
     * Configuration of plugin.
     */
    protected DatabaseConfiguration configuration;

    /**
     * Result set type used for prepareStatement. It must be a valid value
     * defined by ResultSet.TYPE_... Default is TYPE_FORWARD_ONLY.
     */
    protected int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

    // driver features
    protected boolean allowsSqlSequenceFallback;
    protected Boolean supportsSqlNumericNullCast = null;

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
    protected Set singleValueAttributes;

    /**
     * Configured embedded features. The elements are stored as
     * {key=embedded feature name, value=upper bound}
     */
    protected Map embeddedFeatures;

    /**
     * macros
     */
    // key=column name, value=List of [macro name, macro value]
    protected Map<String,List<String[]>> stringMacros;

    // key=macro name, value=macro value
    protected Map<String,String> pathMacros;

    // referenceId format
    private String referenceIdFormat;

    // useNormalizedReferences. If true, all DB operations are performed on the 
    // normalized values of object references, i.e. rid, oid.
    private boolean useNormalizedReferences;

    // maximum reference components in REF table
    protected int maxReferenceComponents;

    /**
     * If set to true the size of multi-valued attributes stored in column C
     * is stored in column C_.
     */
    private boolean setSizeColumns;

    /**
     * <columnName,attributeName> mapping. Calculating attribute names
     * from column names and vice versa is expensive. Therefore it
     * is cached.
     */
    protected final ConcurrentMap<String,String> featureNames = new ConcurrentHashMap<String,String>();
    protected final ConcurrentMap<String,String> publicColumnNames = new ConcurrentHashMap<String,String>();
    protected final ConcurrentMap<String,String> privateColumnNames = new ConcurrentHashMap<String,String>();
    private final Set<String> nonPersistentFeatures = new HashSet<String>();
    
    /**
     * nullAsCharacter defines the string used to produce a type-safe
     * NULL string, e.g. CAST(NULL AS CHARACTER)
     */
    protected String nullAsCharacter;

    /**
     * value of configuration option FETCH_SIZE.
     */
    protected int fetchSize;

    /**
     * Tells whether state filter substitution is enabled or disabled.
     */
    protected boolean enableStateFilterSubstitution;
    
    /**
     * technical column names: object_oid, object_rid, object_idx
     * (may be used by DbObject implementations if required)
     */
    public String OBJECT_OID;
    public String OBJECT_IDX;
    public String OBJECT_RID;
    public String OBJECT_ID;

    protected Properties jdbcDriverSqlProperties;
    protected static final String JDBC_DRIVER_SQL_PROPERTIES = "org/openmdx/kernel/application/deploy/jdbc-driver-sql.properties";  

    protected final Collection temporaryFiles = new ArrayList();

    protected final SQLWildcards sqlWildcards = new SQLWildcards('\\');
    
    /**
     * It's usually unwise the use these classes in  select statement
     */
    private static final Collection<String> BASE_CLASSES = Arrays.asList(
        "org:openmdx:base:BasicObject",
        "org:openmdx:base:ExtentCapable"
    );

}

//---End of File -------------------------------------------------------------
