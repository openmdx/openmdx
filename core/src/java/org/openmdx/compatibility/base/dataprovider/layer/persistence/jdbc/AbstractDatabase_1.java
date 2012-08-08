/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractDatabase_1.java,v 1.253 2008/09/10 18:10:13 hburger Exp $
 * Description: AbstractDatabase_1 plugin
 * Revision:    $Revision: 1.253 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 18:10:13 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.pattern.StringExpression;
import org.openmdx.base.text.pattern.cci.Pattern_1_0;
import org.openmdx.compatibility.base.application.cci.DbConnectionManager_1_0;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.common.AbstractIterator;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.common.AbstractPersistence_1;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.kernel.uri.scheme.OpenMDXSchemes;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;
import org.w3c.cci2.BinaryLargeObject;

//---------------------------------------------------------------------------
/**
 * Database_1 implements a OO-to-Relational mapping and makes DataproviderObjects
 * persistent. Any JDBC-compliant data store can be used.
 * 
 * You can use the java.io.PrintStream.DriverManager.setLogStream() method to
 * log JDBC calls. This method sets the logging/tracing PrintStream used by
 * the DriverManager and all drivers.
 * Insert the following line at the location in your code where you want to
 * start logging JDBC calls: DriverManager.setLogStream(System.out);
 */
@SuppressWarnings("unchecked")
abstract public class AbstractDatabase_1 extends AbstractPersistence_1 
implements DataTypes 
{

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
        boolean isIndexed
    ) throws ServiceException, SQLException;

    //---------------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    public void activate(
        short id, 
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception, ServiceException {

        SysLog.detail(
            "activating", 
            "$Id: AbstractDatabase_1.java,v 1.253 2008/09/10 18:10:13 hburger Exp $"
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

        //
        // XML Datatype Usgae
        //
        boolean xmlDatatypes = configuration.isOn(
            SharedConfigurationEntries.XML_DATATYPES
        );

        // durationType
        this.durationMarshaller = DurationMarshaller.newInstance(
            getConfigurationValue(
                LayerConfigurationEntries.DURATION_TYPE,
                LayerConfigurationEntries.DURATION_TYPE_CHARACTER
            ), 
            xmlDatatypes
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
            xmlDatatypes,
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

        // privateAttributesPrefix
        this.privateAttributesPrefix = getConfigurationValue(
            LayerConfigurationEntries.PRIVATE_ATTRIBUTES_PREFIX,
            "p$$"
        ).toLowerCase();

        OBJECT_OID = this.toOid("object");
        OBJECT_IDX = this.getConfigurationValue(
            LayerConfigurationEntries.OBJECT_IDX_COLUMN,
            this.toIdx("object")
        );
        OBJECT_RID = this.toRid("object");
        OBJECT_ID = this.toId("object");

        // batchSize
        this.batchSize = getConfigurationValue(
            SharedConfigurationEntries.BATCH_SIZE,
            DEFAULT_BATCH_SIZE // LayerConfigurationEntries.BATCH_SIZE configuration was mandatory before
        );

        // Result set type
        this.resultSetType = ResultSet.TYPE_FORWARD_ONLY;
        if(!configuration.values(LayerConfigurationEntries.RESULT_SET_TYPE).isEmpty()) {
            String type = (String)configuration.values(LayerConfigurationEntries.RESULT_SET_TYPE).get(0);
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

        // load model if modelDriven is active
        if(!configuration.values(SharedConfigurationEntries.MODEL).isEmpty()) {
            this.model = (Model_1_0)configuration.values(SharedConfigurationEntries.MODEL).get(0);
        }
        else {
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.INVALID_CONFIGURATION, 
                "A model must be configured with options 'modelPackage' and 'packageImpl'"
            );
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
                configuration.values(LayerConfigurationEntries.SINGLE_VALUE_ATTRIBUTE)
            );
        }

        // Embedded features
        this.embeddedFeatures = new TreeMap();
        if(!configuration.values(LayerConfigurationEntries.EMBEDDED_FEATURE).isEmpty()) {
            for(Iterator i = configuration.values(LayerConfigurationEntries.EMBEDDED_FEATURE).iterator(); i.hasNext(); ) {
                String embeddedFeature = (String)i.next();
                int p;
                if((p = embeddedFeature.indexOf("[")) > 0) {
                    // Store as (feature name, upper bound)
                    this.embeddedFeatures.put(
                        embeddedFeature.substring(0, p),
                        Integer.valueOf(embeddedFeature.substring(p+1, embeddedFeature.indexOf("]")))
                    );
                }
            }
        }

        // Non-persistent features
        this.nonPersistentFeatures = new TreeSet();
        this.nonPersistentFeatures.addAll(
            configuration.values(LayerConfigurationEntries.NON_PERSISTENT_FEATURE)
        );

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

        // useObjectCache
        if(configuration.isOn(LayerConfigurationEntries.USE_OBJECT_CACHE)) {
            this.useObjectCache = true;
        }

        /**
         * Old configuration format for TypeConfigurationEntry with
         * OPTIMIZED_TYPE and OPTIMIZED_TABLE
         */
        if(!configuration.values(LayerConfigurationEntries.OPTIMIZED_TYPE).isEmpty()) {
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.INVALID_CONFIGURATION, 
                "the configuration options '" + LayerConfigurationEntries.OPTIMIZED_TYPE + "' and '" + LayerConfigurationEntries.OPTIMIZED_TABLE + "' are not supported anymore. Use '" + LayerConfigurationEntries.TYPE + "' and '" + LayerConfigurationEntries.DB_OBJECT_FORMAT + "' instead."
            ); 
        }

        // deprecated options COLUMN_NAME_SHORT, COLUMN_NAME_LONG
        if(!configuration.values(LayerConfigurationEntries.COLUMN_NAME_LONG).isEmpty()) {
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.INVALID_CONFIGURATION, 
                "the configuration option '" + LayerConfigurationEntries.COLUMN_NAME_LONG + "' is not supported anymore. Use '" + LayerConfigurationEntries.COLUMN_NAME_FROM + "' instead."
            );
        }

        // PATH_MACRO_NAME, PATH_MACRO_VALUE
        this.pathMacros = new HashMap();
        for(int i = 0; i < configuration.values(LayerConfigurationEntries.PATH_MACRO_NAME).size(); i++) {
            this.pathMacros.put(
                configuration.values(LayerConfigurationEntries.PATH_MACRO_NAME).get(i),
                configuration.values(LayerConfigurationEntries.PATH_MACRO_VALUE).get(i)
            );
        }
        SysLog.info("pathMacros", this.pathMacros);

        // STRING_MACRO_COLUMN, STRING_MACRO_NAME, STRING_MACRO_VALUE
        this.stringMacros = new HashMap();
        for(int i = 0; i < configuration.values(LayerConfigurationEntries.STRING_MACRO_COLUMN).size(); i++) {
            this.stringMacros.put(
                configuration.values(LayerConfigurationEntries.STRING_MACRO_COLUMN).get(i),
                new ArrayList()
            );
        }
        for(int i = 0; i < configuration.values(LayerConfigurationEntries.STRING_MACRO_COLUMN).size(); i++) {
            ((List)this.stringMacros.get(
                configuration.values(LayerConfigurationEntries.STRING_MACRO_COLUMN).get(i)
            )).add(
                new String[]{
                    (String)configuration.values(LayerConfigurationEntries.STRING_MACRO_NAME).get(i),
                    (String)configuration.values(LayerConfigurationEntries.STRING_MACRO_VALUE).get(i)
                }       
            );
        }
        SysLog.info("stringMacros", this.stringMacros);

        // maxReferenceColumns
        this.maxReferenceComponents = getConfigurationValue(
            LayerConfigurationEntries.MAX_REFERENCE_COMPONENTS,
            16
        );

        // connection
        this.connectionManagers = configuration.values(
            SharedConfigurationEntries.DATABASE_CONNECTION_FACTORY
        );

        // driver properties
        this.jdbcDriverSqlProperties = new Properties();
        URL propertiesUrl = Classes.getApplicationResource(JDBC_DRIVER_SQL_PROPERTIES);
        if(propertiesUrl == null) {
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.INVALID_CONFIGURATION, 
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
                StackedException.DEFAULT_DOMAIN,
                StackedException.INVALID_CONFIGURATION, 
                "Loading resource '" + JDBC_DRIVER_SQL_PROPERTIES + "'failed"
            );
        }     

        this.configuration = new DatabaseConfiguration(
            this.namespaceId,
            this.referenceIdFormat,
            this.useNormalizedReferences,
            // The database configuration is always retrieved from the first configured connection manager
            this.connectionManagers.isEmpty()
            ? null
                : (DbConnectionManager_1_0)this.connectionManagers.get(0),
                configuration
        );
    }

    //---------------------------------------------------------------------------
    public void executeBatch(
        PreparedStatement ps
    ) throws ServiceException, SQLException {
        try {
            Method executeBatch = ps.getClass().getMethod("executeBatch", new Class[]{});
            executeBatch.invoke(ps, (Object[])null);
        }
        catch(NoSuchMethodException e) {
            // ignore
        }
        catch(IllegalAccessException e) {
            throw new ServiceException(e);
        }
        catch(InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if(t instanceof SQLException) {
                throw (SQLException)t;
            }
            throw new RuntimeServiceException(e);
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Return connection manager. The default implementation returns the connection
     * manager this.connectionManagers at index 0. A user-defined implementation of 
     * getConnectionManager() can return a manager from any of the configured connection 
     * managers according to a criteria derived from the request info.
     */
    protected DbConnectionManager_1_0 getConnectionManager(
        DataproviderRequest request
    ) throws ServiceException {
        if(this.connectionManagers.isEmpty()) {
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.INVALID_CONFIGURATION, 
                "can not get connection manager"
            );
        }
        return ((DbConnectionManager_1_0)this.connectionManagers.get(0));
    }

    //---------------------------------------------------------------------------
    protected Connection getConnection(
        DataproviderRequest request
    ) throws ServiceException{
        String unitOfWorkId = (String) request.context(DataproviderRequestContexts.UNIT_OF_WORK_ID).get(0);
        this.unitOfWorkId = unitOfWorkId == null || unitOfWorkId.length() == 0 ? uidAsString() : "noUnitOfWorkId";
        return this.getConnectionManager(request).getConnection();
    }

    //---------------------------------------------------------------------------
    protected void closeConnection(
        DataproviderRequest request,
        Connection conn
    ) throws ServiceException{
        if(conn != null) {
            this.getConnectionManager(request).closeConnection(conn);
        }
    }

    //---------------------------------------------------------------------------
    abstract PreparedStatement prepareStatement(
        Connection conn,
        String statement,
        boolean updatable
    ) throws SQLException;

    //---------------------------------------------------------------------------
    public PreparedStatement prepareStatement(
        Connection conn,
        String statement
    ) throws SQLException {
        return this.prepareStatement(
            conn,
            statement,
            false
        );
    }

    //---------------------------------------------------------------------------
    String toRid(
        String name
    ) {
        return name + this.referenceIdAttributesSuffix;
    }

    //---------------------------------------------------------------------------
    String toOid(
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
    public Object externalizeStringValue(
        String _columnName,
        Object value
    ) {
        String columnName = _columnName;
        if(value instanceof String) {
            // column name could be qualified, e.g. vm.<column name>
            int pos = columnName.indexOf(".");
            if(pos >= 0) {
                columnName = columnName.substring(pos+1);
            }
            List stringReplacements = (List)this.stringMacros.get(columnName);
            if(stringReplacements != null) {
                for(int i = 0; i < stringReplacements.size(); i++) {
                    String macroName = ((String[])stringReplacements.get(i))[0];
                    String macroValue = ((String[])stringReplacements.get(i))[1];
                    // replace matching macro value with macro name
                    if(((String)value).startsWith(macroValue)) {
                        return macroName + ((String)value).substring(macroValue.length());
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
        List stringReplacements = (List)this.stringMacros.get(columnName);
        if(stringReplacements != null) {
            for(int i = 0; i < stringReplacements.size(); i++) {
                String macroName = ((String[])stringReplacements.get(i))[0];
                String macroValue = ((String[])stringReplacements.get(i))[1];
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
        if(this.useNormalizedReferences) {
            return this.getReferenceId(
                conn,
                source,
                false
            ) + "/" + source.getBase();
        }
        else {
            String converted = source.toXri();
            boolean modified = false;
            for(Iterator i = this.pathMacros.entrySet().iterator(); i.hasNext(); ) {
                Entry e = (Entry)i.next();
                String macroName = (String)e.getKey();
                String macroValue = (String)e.getValue();
                if(converted.indexOf(macroValue) >= 0) {
                    Pattern_1_0 pattern = StringExpression.compile(macroValue);
                    converted = pattern.matcher(converted).replaceFirst("xri:*" + macroName);
                    converted = pattern.matcher(converted).replaceAll("(*" + macroName + ")");
                    modified = true;
                }
            }
            return modified ? converted : source.toUri();
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Internalizes an external, stringified path value. Also applies the path 
     * macros to expand the path.
     */
    public Object internalizePathValue(
        String source
    ) {
        String converted = source;
        boolean modified = false;
        for(Iterator i = this.pathMacros.entrySet().iterator(); i.hasNext(); ) {
            Entry e = (Entry)i.next();
            String macroName = (String)e.getKey();
            String macroValue = (String)e.getValue();
            if(converted.indexOf(macroName) >= 0) {
                converted = StringExpression.compile("xri:*" + macroName).matcher(converted).replaceFirst(macroValue);
                converted = StringExpression.compile("(*" + macroName + ")").matcher(converted).replaceAll(macroValue);
                modified = true;
            }
        }
        Path convertedPath = new Path(converted);
        return modified
        ? convertedPath.toXri().equals(converted) ? (Object)convertedPath : (Object)converted
            : convertedPath.toUri().equals(converted) ? (Object)convertedPath : (Object)converted;
    }

    //---------------------------------------------------------------------------
    public ResultSet executeQuery(
        PreparedStatement ps,
        String statement,
        List statementParameters
    ) throws SQLException {
        ps.setFetchSize(this.fetchSize);
        ps.setFetchDirection(ResultSet.FETCH_FORWARD);
        SysLog.detail("statement", statement);
        SysLog.detail("parameters", statementParameters);
        long startTime = System.currentTimeMillis();
        ResultSet rs = ps.executeQuery();
        long duration = System.currentTimeMillis() - startTime;
        SysLog.detail("execution time", new Long(duration));
        return rs;
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
        DbObjectConfiguration entry,
        DbObject dbObject
    ) throws ServiceException {
        String view = entry.getDbObjectForQuery1();    
        boolean containsWildcard = false;
        for(int i = 0; i < dbObject.getReferencedType().size(); i++) {
            if(":*".equals(dbObject.getReferencedType().get(i))) {
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
        return view;
    }

    //---------------------------------------------------------------------------
    /**
     * Computes the SQL select statement returning one object-index-slice per row for
     * the given path. The statement is appended to 'statement'. VIEW_MODE_OBJECT_ITERATION
     * must return at least slice 0. VIEW_MODE_OBJECT_RETRIEVAL must return all object
     * slices. 
     */
    private String getView(
        Connection conn,
        DbObject dbObject,
        short viewMode,
        String requestedColumnSelector,
        Set requestedMixinAttributes
    ) throws ServiceException {
        Set mixinAttributes = requestedMixinAttributes;
        String columnSelector = requestedColumnSelector == null
        ? DEFAULT_COLUMN_SELECTOR
            : requestedColumnSelector;
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
                    typeConfiguration,
                    dbObject
                );            
            }
            else {
                dbObjectForQuery1 = typeConfiguration.getDbObjectForUpdate1();
            }
            dbObjectForQuery2 = typeConfiguration.getDbObjectForQuery2() == null
            ? typeConfiguration.getDbObjectForUpdate2()
                : typeConfiguration.getDbObjectForQuery2();
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
                            dbObjectForQuery = StringExpression.compile("STRCAT.PREFIX").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".STRCAT.PREFIX")
                            );
                        }
                        if(dbObjectForQuery.indexOf("STRCAT.INFIX") >= 0) {
                            dbObjectForQuery = StringExpression.compile("STRCAT.INFIX").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".STRCAT.INFIX")
                            );
                        }
                        if(dbObjectForQuery.indexOf("STRCAT.SUFFIX") >= 0) {
                            dbObjectForQuery = StringExpression.compile("STRCAT.SUFFIX").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".STRCAT.SUFFIX")
                            );
                        }
                        if(dbObjectForQuery.indexOf("NULL.NUMERIC") >= 0) {
                            dbObjectForQuery = StringExpression.compile("NULL.NUMERIC").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".NULL.NUMERIC")
                            );
                        }
                        if(dbObjectForQuery.indexOf("NULL.CHARACTER") >= 0) {
                            dbObjectForQuery = StringExpression.compile("NULL.CHARACTER").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(conn.getMetaData().getDatabaseProductName() + ".NULL.CHARACTER")
                            );
                        }
                        if(dbObjectForQuery.indexOf("CORREL.SUBQUERY.BEGIN") >= 0) {
                            dbObjectForQuery = StringExpression.compile("CORREL.SUBQUERY.BEGIN").matcher(dbObjectForQuery).replaceAll(
                                this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".CORREL.SUBQUERY.BEGIN")
                            );
                        }
                        if(dbObjectForQuery.indexOf("CORREL.SUBQUERY.END") >= 0) {
                            dbObjectForQuery = StringExpression.compile("CORREL.SUBQUERY.END").matcher(dbObjectForQuery).replaceAll(
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
                StackedException.DEFAULT_DOMAIN,
                StackedException.ASSERTION_FAILURE, 
                "Missing type configuration",
                new BasicException.Parameter("db object", dbObject)            
            );        
        }

        boolean isComplex = 
            dbObjectForQuery1.startsWith("SELECT") 
            || dbObjectForQuery1.startsWith("select");

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
                    (mixinAttributes != null) && 
                    !mixinAttributes.isEmpty()
            ) {
                mixinAttributes = new HashSet(mixinAttributes);
                mixinAttributes.add(SystemAttributes.OBJECT_CLASS);      
                mixinAttributes.removeAll(
                    dbObject.getExcludeAttributes()
                );
                String view = "";

                view += "SELECT " + dbObject.getHint() + " " + columnSelector;
                for(
                        Iterator i = mixinAttributes.iterator();
                        i.hasNext();
                ) {
                    String attributeName = (String)i.next();    
                    int upperBound = this.embeddedFeatures.containsKey(attributeName)
                    ? ((Number)this.embeddedFeatures.get(attributeName)).intValue()
                        : 1;
                    for(int j = 0; j < upperBound; j++) {
                        String columnName = this.getColumnName(conn, attributeName, j, upperBound > 1, true);
                        String prefixedColumnName = this.getColumnName(conn, this.privateAttributesPrefix + attributeName, j, upperBound > 1, true);
                        view += ", " + (dbObject.getIndexColumn() == null ? "v." : "vm.") + columnName + " AS " + prefixedColumnName;
                    }
                }              
                // JOIN is required if primary is indexed
                if(dbObject.getIndexColumn() != null) {
                    boolean useInnerJoin = !databaseProductName.startsWith("DB2");  
                    if(useInnerJoin) {
                        view += isComplex
                        ? " FROM (" + dbObjectForQuery1 + ") v INNER JOIN (" + (dbObjectForQuery2 == null ? dbObjectForQuery1 : dbObjectForQuery2) + ") vm"
                            : " FROM " + dbObjectForQuery1 + " v INNER JOIN " + (dbObjectForQuery2 == null ? dbObjectForQuery1 : dbObjectForQuery2) + " vm";
                        view += " ON ";
                    }
                    else {
                        view += isComplex
                        ? " FROM (" + dbObjectForQuery1 + ") v, (" + (dbObjectForQuery2 == null ? dbObjectForQuery1 : dbObjectForQuery2) + ") vm"
                            : " FROM " + dbObjectForQuery1 + " v, " + (dbObjectForQuery2 == null ? dbObjectForQuery1 : dbObjectForQuery2) + " vm";
                        view += " WHERE ";              
                    }
                    int k = 0;
                    // Compare rid selectors of v, vm
                    for(int i = 0; i < dbObject.getReferenceColumn().size(); i++) {
                        String selector = (String)dbObject.getReferenceColumn().get(i);
                        view += k == 0 ? "" : " AND ";
                        view += "(v." + selector + " = " + "vm." + selector + ")";
                        k++;
                    }
                    // Compare oid selectors of v, vm
                    for(int i = 0; i < dbObject.getObjectIdColumn().size(); i++) {
                        String objectIdColumn = (String)dbObject.getObjectIdColumn().get(i);
                        view += k == 0 ? "" : " AND ";
                        view += "(v." + objectIdColumn + " = " + "vm." + objectIdColumn + ")";
                        k++;
                    }
                    view += " WHERE (vm." + this.OBJECT_IDX + " = 0)";
                }
                // Non-indexed db object (--> secondary view is always null)
                else {
                    view += isComplex
                    ? " FROM (" + dbObjectForQuery1 + ") v"
                        : " FROM " + dbObjectForQuery1 + " v";
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
            return dbObjectForQuery2 == null
            ? dbObjectForQuery1
                : dbObjectForQuery2;
        }
        else {
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.NOT_SUPPORTED, 
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
    ) throws SQLException;

    //---------------------------------------------------------------------------
    /**
     * val either contains a String or a java.sql.Clob. Depending on the attributeDef
     * and the drivers capabilities this method returns either a Reader or a String.
     */
    abstract Object getClobColumnValue(
        Object val,
        String attributeName,
        ModelElement_1_0 attributeDef
    ) throws SQLException;

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
        List statementParameters
    ) throws ServiceException {
        Path referencePattern = pattern.size() % 2 == 1
        ? pattern.getParent()
            : pattern;
        if(LayerConfigurationEntries.REFERENCE_ID_FORMAT_REF_TABLE.equals(this.getReferenceIdFormat())) {
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
                if(srid.endsWith("%")) {
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
        List statementParameters
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
        List statementParameters
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
            String c = i < pathPattern.size() 
            ? pathPattern.get(i)
                : "#";
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
                    currentClause += "(" + this.getColumnName(conn, "c", i, true, true) + " = '" + c + "')";
                }
                else {
                    currentClause += "(" + this.getColumnName(conn, "c", i, true, true) + " = ?)";
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
            statementParameters.add(new Integer(pathPattern.size()));          
        }
        return currentClause;
    }

    //---------------------------------------------------------------------------
    private Set findReferenceIdsFormatRefTable(
        Connection conn,
        Path pathPattern
    ) throws ServiceException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        Set referenceIds = new HashSet();
        String statement = null;

        try {
            // get referenceId of reference
            List statementParameters = new ArrayList();
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
            referenceIds = new HashSet();
            while(rs.next()) {
                referenceIds.add(
                    new Long(rs.getLong(OBJECT_RID))
                );
            }
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path pattern", pathPattern),
                new BasicException.Parameter("statement", statement)
            );
        }
        catch(ServiceException e) {
            throw e;
        }
        catch(Exception ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.GENERIC, 
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
        if(referencePattern.size() < 2) {
            return null;
        }
        Path reference = referencePattern.size() % 2 == 0
        ? referencePattern
            : referencePattern.getParent(); 
        return LayerConfigurationEntries.REFERENCE_ID_FORMAT_REF_TABLE.equals(this.getReferenceIdFormat()) ? (Object) this.getReferenceIdFormatRefTable(
            conn,
            reference,
            forceCreate
        ) : this.getReferenceIdFormatTypeNameWithPathComponents(
            reference
        );
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
        String rid = null;
        if((type.size() >= 2) && reference.isLike(type.getParent())) {
            rid = typeName;
            for(
                    int l = 0;
                    l < reference.size();
                    l++
            ) {
                if(
                        ":*".equals(reference.get(l)) ||
                        !reference.get(l).equals(type.get(l))
                ) {                  
                    rid += "/" + (":*".equals(reference.get(l)) ? "%" : reference.get(l));
                }
            }
        }
        return rid;
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
        return value instanceof XMLGregorianCalendar && DatatypeConstants.DATETIME.equals(
            ((XMLGregorianCalendar) value).getXMLSchemaType()
        ) && LayerConfigurationEntries.DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE.equals(
            getDateTimeType(connection)
        ) ? getTimestampWithTimzoneExpression(connection) : "?";
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
        String dbObject,
        String asFormat
    ) throws ServiceException, SQLException {

        DatabaseMetaData dbm = conn.getMetaData();
        String autonumFormat = this.jdbcDriverSqlProperties.getProperty(dbm.getDatabaseProductName() + ".AUTOINC");          
        SysLog.detail("autonum format ", autonumFormat);

        if(AUTOINC_FORMAT_NEXTVAL.equals(autonumFormat)) {
            return "NEXTVAL('" + sequenceName + "_SEQ')";
        }
        else if(AUTOINC_FORMAT_NEXTVAL_FOR.equals(autonumFormat)) {          
            return asFormat != null
            ? "CAST(NEXTVAL FOR " + sequenceName + "_SEQ " + asFormat + ")"
                : "NEXTVAL FOR " + sequenceName + "_SEQ";
        }
        else if(AUTOINC_FORMAT_NEXT_VALUE_FOR.equals(autonumFormat)) {          
            return "(SELECT NEXT VALUE FOR " + sequenceName + "_SEQ FROM (SELECT DISTINCT 1 FROM INFORMATION_SCHEMA.SYSTEM_VIEWS))";
        }
        else if(AUTOINC_FORMAT_AUTO.equals(autonumFormat)) {
            return null;
        }
        else if(AUTOINC_FORMAT_SEQUENCE.equals(autonumFormat)) {
            return sequenceName + "_SEQ.NEXTVAL";
        }
        else {
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.ASSERTION_FAILURE , 
                "AUTONUM format not supported. Use [NEXTVAL|NEXTVALFOR|NEXTVALUEFOR|AUTO|SEQUENCE]",
                new BasicException.Parameter("sequenceName", sequenceName)
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

        Long referenceId = new Long(-1);
        String currentStatement = null;
        PreparedStatement ps = null;

        Set referenceIds = this.findReferenceIdsFormatRefTable(
            conn,
            reference
        );

        // either return when found or create on demand
        try {
            if(referenceIds.size() == 0) {
                SysLog.trace("No referenceIds found for reference", reference + "; forceCreate=" + forceCreate);
                if(forceCreate) {

                    // path component names
                    String columnNamesPathComponents = "";
                    for(int i = 0; i < this.maxReferenceComponents; i++) {
                        columnNamesPathComponents += ", " + this.getColumnName(conn, "c", i, true, true);
                    }        
                    // SQL sequences
                    String dbObjectRef = this.namespaceId + "_" + T_REF;
                    String autonumValue = this.getAutonumValue(
                        conn,
                        dbObjectRef,
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
                    ps.setInt(
                        1,
                        reference.size()
                    );
                    // fill reference components up to maxReferenceComponents.
                    // fill missing component columns with blanks. This allows to
                    // define a unique key on the component columns.
                    for(int i = 0; i < this.maxReferenceComponents; i++) {
                        ps.setString(
                            i + 2, 
                            i < reference.size() ? reference.get(i) : "#"
                        );
                    }
                    ps.executeUpdate();
                    executeBatch(ps);
                    ps.close(); 
                    ps = null;

                    referenceIds = this.findReferenceIdsFormatRefTable(
                        conn,
                        reference
                    );
                    if(referenceIds.size() == 1) {
                        referenceId = (Long)referenceIds.iterator().next();
                    }
                    else {
                        throw new ServiceException(
                            StackedException.DEFAULT_DOMAIN,
                            StackedException.ASSERTION_FAILURE , 
                            "Can not find created reference",
                            new BasicException.Parameter("reference", reference)
                        );
                    }
                }
                else {
                    throw new ServiceException(
                        StackedException.DEFAULT_DOMAIN,
                        StackedException.NOT_FOUND, 
                        "Object reference not found",
                        new BasicException.Parameter("reference", reference)
                    );
                }
            }
            else if(referenceIds.size() > 1) {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_FOUND, 
                    "more than one referenceId found for given reference",
                    new BasicException.Parameter("reference", reference),
                    new BasicException.Parameter("referenceIds", referenceIds)
                );
            }
            else {
                referenceId = (Long)referenceIds.iterator().next();
            }
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("reference", reference),
                new BasicException.Parameter("statement", currentStatement)
            );
        }
        catch(ServiceException e) {
            throw e;
        }
        catch(Exception ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.GENERIC, 
                ex.toString()
            );
        }
        finally {
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
        if(LayerConfigurationEntries.REFERENCE_ID_FORMAT_REF_TABLE.equals(this.getReferenceIdFormat())) {
            return this.getReferenceFormatRefTable(
                conn,
                (Number)referenceId
            );
        }
        else {
            return this.getReferenceFormatTypeNameWithComponents(
                conn,
                (String)referenceId
            );
        }
    }

    //---------------------------------------------------------------------------
    private Path getReferenceFormatTypeNameWithComponents(
        Connection conn,
        String referenceId
    ) throws ServiceException {
        List components = new ArrayList();
        StringTokenizer t = new StringTokenizer(referenceId, "/");
        while(t.hasMoreTokens()) {
            components.add(t.nextToken());          
        }
        if(components.size() == 0) {
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.ASSERTION_FAILURE, 
                "No components found for reference id",
                new BasicException.Parameter("reference", referenceId)
            );          
        }
        DbObjectConfiguration dbObjectConfiguration = this.configuration.getDbObjectConfiguration(
            (String)components.get(0)
        );
        Path type = dbObjectConfiguration.getType();
        String[] referenceComponents = new String[type.size()-1];
        int pos = 1;
        for(int i = 0; i < referenceComponents.length; i++) {
            if(":*".equals(type.get(i))) {
                if(pos >= components.size()) {
                    throw new ServiceException(
                        StackedException.DEFAULT_DOMAIN,
                        StackedException.ASSERTION_FAILURE, 
                        "Reference not valid for type",
                        new BasicException.Parameter("reference", referenceId),
                        new BasicException.Parameter("type", type)
                    );                            
                }
                referenceComponents[i] = (String)components.get(pos);
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

        try {
            List statementParameters = new ArrayList();
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
                int n = rs.getInt(this.getColumnName(conn, "n", 0, false, false));
                reference = new Path("");
                for(int i = 0; i < n; i++) {
                    reference = reference.add(
                        rs.getString(
                            this.getColumnName(conn, "c", i, true, false)
                        )
                    );
                }
            }
            else {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_FOUND, 
                    "can not find reference id",
                    new BasicException.Parameter("referenceId", referenceId),
                    new BasicException.Parameter("table", this.namespaceId + "_" + T_REF)
                );
            }
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("reference", reference),
                new BasicException.Parameter("statement", statement)
            );
        }
        catch(ServiceException e) {
            throw e;
        }
        catch(Exception ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.GENERIC, 
                ex.toString()
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
        return reference;    
    }

    //---------------------------------------------------------------------------
    String getColumnName(
        Connection conn,
        String attributeName,
        int index,
        boolean indexSuffixIfZero,
        boolean ignoreReservedWords
    ) throws ServiceException {
        String columnName = (String)this.columnNames.get(attributeName);
        if(columnName == null) {
            String name = "";
            for(
                    int i = 0; 
                    i < attributeName.length(); 
                    i++
            ) {
                char c = attributeName.charAt(i);
                if(Character.isUpperCase(c)) {
                    name += "_" + Character.toLowerCase(c);
                }
                else if(c == '_') {
                    if(i < attributeName.length()-1) {
                        // do not escape _<digit>
                        if(Character.isDigit(attributeName.charAt(i+1))) {
                            name += "_";
                        }
                        // escape _<alpha> as __<uppercase alpha>
                        else {
                            name += "__";
                        }
                    }
                    else {
                        name += "_";
                    }
                }
                else {
                    name += c;
                }
            }
            // from->to mapping
            columnName = name.toString();
            boolean isSizeColumn = columnName.endsWith("_");
            columnName = isSizeColumn
            ? columnName.substring(0, columnName.length()-1)
                : columnName;
            if(this.configuration.getFromToColumnNameMapping().containsKey(columnName)) {
                columnName = (String)this.configuration.getFromToColumnNameMapping().get(columnName);
            }
            columnName = isSizeColumn
            ? columnName + "_"
                : columnName;
            this.columnNames.put(
                attributeName,
                columnName
            );
        }    
        // append index
        if(indexSuffixIfZero || (index > 0)) {
            if(this.embeddedFeatures.containsKey(attributeName)) {
                columnName = columnName + "_" + String.valueOf(index);              
            }
            else {
                columnName = columnName + "$" + String.valueOf(index);
            }
        }
        else {
            // DB-specific handling
            String databaseProductName = null;
            try {
                databaseProductName = conn.getMetaData().getDatabaseProductName();
            } catch(Exception e) {}
            if(
                    !ignoreReservedWords &&
                    "HSQL Database Engine".equals(databaseProductName) &&
                    RESERVED_WORDS_HSQLDB.contains(columnName)
            ) {
                columnName = "\"" + columnName.toUpperCase() + "\"";
            }
        }
        return columnName;
    }

    //---------------------------------------------------------------------------
    public String getAttributeName(
        String _columnName
    ) {
        String columnName = _columnName;
        // Indexed column?
        int nameLength = columnName.lastIndexOf('$') > columnName.lastIndexOf('_')
        ? columnName.lastIndexOf('$')
            : columnName.lastIndexOf('_');
        // Non-indexed column
        if(
                (nameLength < 0) || 
                columnName.endsWith("_") || 
                columnName.endsWith("$") || 
                !Character.isDigit(columnName.charAt(nameLength+1))
        ) {
            // Do not change column name
        }
        // Indexed column
        else {
            String attributeNameOfNonIndexedColumn = this.getAttributeName(
                columnName.substring(0, nameLength)
            );     
            // Only embedded features can be indexed
            if(this.embeddedFeatures.containsKey(attributeNameOfNonIndexedColumn)) {
                return attributeNameOfNonIndexedColumn;      
            }        
        }

        // to->from mapping
        if(this.configuration.getToFromColumnNameMapping().containsKey(columnName)) {
            columnName = (String)this.configuration.getToFromColumnNameMapping().get(columnName);
        }
        String attributeName = (String)this.attributeNames.get(columnName);
        if(attributeName == null) {        
            String name = "";
            boolean nextAsUpperCase = false;
            for(
                    int i = 0; 
                    i < columnName.length();
                    i++
            ) {
                char c = Character.toLowerCase(columnName.charAt(i));
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
            attributeName = name.toString();
            this.attributeNames.put(
                columnName,
                attributeName
            );
        }
        return attributeName;
    }

    //---------------------------------------------------------------------------
    /**
     * Calls ps.set<T> where <T> is the type of value.
     *
     * @param ps initialized prepared statement
     * 
     * @param position value position starting with 1.
     *
     * @param value can be instanceof Boolean, String, Path, Number, byte[]
     *
     */
    public void setPreparedStatementValue(
        Connection conn,
        PreparedStatement ps,
        int position,
        Object value
    ) throws ServiceException, SQLException {
        if(value instanceof Short) {
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
        else if(value instanceof java.util.Date) {
            ps.setTimestamp(
                position,
                new Timestamp(((java.util.Date)value).getTime())
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
                ps.setString(
                    position, 
                    sqlValue.toString()
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
                        Class cl =  ps.getClass();
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
                            StackedException.DEFAULT_DOMAIN,
                            StackedException.NOT_SUPPORTED, 
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
                StackedException.DEFAULT_DOMAIN,
                StackedException.NOT_SUPPORTED, 
                "attribute type not supported",
                new BasicException.Parameter("value type", value == null ? null : value.getClass().getName())
            );
        }
        // SysLog.trace("value", value);
    }

    //---------------------------------------------------------------------------
    private DataproviderObject getObject(
        Connection conn,
        DataproviderRequest request,
        DbObject dbObject,
        ResultSet rs,
        String objectClass,
        boolean checkIdentity
    ) throws ServiceException, SQLException {

        List objects = new ArrayList();

        this.getObjects(
            conn,
            dbObject,
            rs,
            objects,
            request.attributeSelector(),
            request.attributeSpecifierAsMap(),
            0, -1, -1, 1,
            objectClass
        );

        if(
                (objects.size() != 1) ||
                (checkIdentity && !((DataproviderObject)objects.get(0)).path().startsWith(request.path()))
        ) {
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.NOT_FOUND, 
                "invalid object path. no object found",
                new BasicException.Parameter("path", request.path())
            );
        }
        SysLog.detail("retrieved object", objects);
        return (DataproviderObject)objects.get(0);
    }

    //---------------------------------------------------------------------------
    /**
     * Touch object feature (set empty value). The feature must be a modeled feature
     * otherwise the feature is touched.
     */
    private void touchNonDerivedFeature(
        DataproviderObject object,
        String view,
        String featureName
    ) throws ServiceException {
        if(object.getValues(view + SystemAttributes.OBJECT_CLASS) != null) {
            ModelElement_1_0 classDef = null;
            try {
                classDef = this.model.getElement(
                    object.getValues(view + SystemAttributes.OBJECT_CLASS).get(0)
                );
            } catch(Exception e){
                // ignore
            }
            if(classDef != null) {
                ModelElement_1_0 featureDef = (ModelElement_1_0)((Map)classDef.values("allFeature").get(0)).get(featureName);
                if(
                        (featureDef != null) &&
                        ((this.model.isAttributeType(featureDef) && !((Boolean)featureDef.values("isDerived").get(0)).booleanValue()) || 
                                (this.model.isReferenceType(featureDef) && this.model.referenceIsStoredAsAttribute(featureDef) && !this.model.referenceIsDerived(featureDef)))
                ) {
                    // Cache scoped features. This reduces string allocations
                    Map scopedFeatures = (Map)this.scopedFeatures.get(view);
                    if(scopedFeatures == null) {
                        this.scopedFeatures.put(
                            view,
                            scopedFeatures = new HashMap()
                        );
                    }
                    String scopedFeature = (String)scopedFeatures.get(
                        featureDef.values("name").get(0)
                    );
                    if(scopedFeature == null) {
                        scopedFeatures.put(
                            featureDef.values("name").get(0),
                            scopedFeature = view + (String)featureDef.values("name").get(0)
                        );
                    }
                    object.values(scopedFeature);
                }
            }
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
    private boolean getObjects(
        Connection conn,
        DbObject dbObject,
        ResultSet rs,
        List objects,
        short attributeSelector,
        Map attributeSpecifiers,
        int position,
        int lastPosition,
        int lastRowCount,
        int maxObjectsToReturn,
        String primaryObjectClass
    ) throws ServiceException, SQLException {

        // set row count of rs to corresponding position
        ResultSetMetaData rsmd = rs.getMetaData();
        List columnNames = FastResultSet.getColumnNames(rsmd);
        Map attributeNames = new HashMap();

        // <rid, reference> mapping
        for(Iterator i = columnNames.iterator(); i.hasNext();) {
            String columnName = (String)i.next();
            attributeNames.put(
                columnName,
                this.getAttributeName(columnName)
            );
        }
        FastResultSet frs = this.setPosition(
            rs,
            position,
            lastPosition,
            lastRowCount,
            dbObject.getIndexColumn() != null
        );
        boolean hasMore = frs != null;

        // get objects
        Map candidates = new HashMap();
        String previousObjectId = null;
        Map featureDefs = null;
        DataproviderObject current = null;
        Set processedIdxs = new HashSet();
        String objectClass = null;

        while(hasMore) {

            Path reference = dbObject.getObjectReference(frs);
            String objectId = dbObject.getObjectId(frs);
            int idx = dbObject.getIndex(frs);

            // try to get attribute definitions
            if(idx == 0) {

                // Get OBJECT_CLASS if not supplied
                objectClass = primaryObjectClass == null
                ? dbObject.getObjectClass(frs)
                    : primaryObjectClass;

                featureDefs = null;
                if(objectClass != null) {      
                    try {
                        featureDefs = (Map)this.model.getElement(objectClass).values("allFeatureWithSubtype").get(0);
                    }
                    // if class not found in model fall back to non model-driven mode
                    catch(Exception e) {
                        featureDefs = null;
                    }
                }
            }

            // skip to next object? If yes, create new one and add it to list
            if(
                    !objectId.equals(previousObjectId) ||
                    !reference.equals(current.path().getParent())
            ) {

                processedIdxs.clear();

                // add empty object to result set if necessary
                // check if this object was already read
                Path objectPath = reference.getChild(objectId);
                if(candidates.containsKey(objectPath)) {
                    throw new ServiceException(
                        StackedException.DEFAULT_DOMAIN,
                        StackedException.MEDIA_ACCESS_FAILURE, 
                        "Result set contains duplicates or non consecutive object ids",
                        new BasicException.Parameter("path", objectPath)
                    );
                }

                if(candidates.size() >= maxObjectsToReturn) {
                    return hasMore;
                }
                current = new DataproviderObject(objectPath);
                objects.add(current);
                current.values(SystemAttributes.OBJECT_CLASS).add(objectClass);
                candidates.put(objectPath, null);
                previousObjectId = objectId;
            }

            // Duplicate indices are allowed. However, they are skipped
            if(!processedIdxs.contains(new Integer(idx))) {

                // Iterate through object attributes and add values
                for(
                        Iterator i = frs.getColumnNames().iterator();
                        i.hasNext();
                ) { 
                    String columnName = (String)i.next();
                    String featureName = (String)attributeNames.get(columnName);  
                    boolean isReferenceFeature =        
                        this.useNormalizedReferences &&
                        (frs.getColumnNames().contains(this.getColumnName(conn, this.privateAttributesPrefix + featureName + this.referenceIdAttributesSuffix, 0, false, true)) ||
                                frs.getColumnNames().contains(this.getColumnName(conn, this.privateAttributesPrefix + featureName + "Parent", 0, false, true))
                        );
                    ModelElement_1_0 featureDef = featureDefs == null 
                    ? null 
                        : (ModelElement_1_0)featureDefs.get(featureName);

                    // Always include reference columns. Otherwise dbObject decides
                    if(
                            dbObject.includeColumn(columnName)
                    ) {

                        // check whether attribute must be added
                        boolean addValue = SystemAttributes.OBJECT_CLASS.equals(featureName);

                        switch(attributeSelector) {
                            case AttributeSelectors.NO_ATTRIBUTES:
                                break;

                            case AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES:
                            case AttributeSelectors.ALL_ATTRIBUTES:
                                addValue = true;
                                break;

                            case AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES:
                                // check for system attributes
                                if(Arrays.binarySearch(SYSTEM_ATTRIBUTES, featureName) >= 0) {
                                    addValue = true;
                                }
                                else {
                                    // check for specified attributes
                                    AttributeSpecifier specifier = (AttributeSpecifier)attributeSpecifiers.get(featureName);
                                    if(specifier != null) {
                                        addValue = (idx >= specifier.position()) && (idx < specifier.position() + specifier.size());
                                    }
                                }
                                break;
                        }

                        // add value
                        if(addValue) {
                            String featureType = featureDef == null
                            ? null
                                : (String)this.model.getDereferencedType(featureDef.values("type").get(0)).values("qualifiedName").get(0);                
                            Object val = frs.getObject(columnName);
                            // Embedded attribute? If yes derive idx from column name suffix (instead of slice index)
                            int valueIdx = idx;
                            if(this.embeddedFeatures.containsKey(featureName)) {
                                valueIdx = Integer.valueOf(columnName.substring(columnName.lastIndexOf('_') + 1)).intValue();
                            }
                            // String, Clob
                            if(this.isClobColumnValue(val)) {
                                val = this.getClobColumnValue(val, featureName, featureDef);
                                if(val instanceof Reader) {
                                    current.values(featureName).set(
                                        valueIdx, 
                                        val
                                    );                    
                                } 
                                else if (
                                        // class type || PrimitiveTypes.PATH
                                        isReferenceFeature ||
                                        ((featureType == null) && ((String)val).startsWith(OpenMDXSchemes.URI_PREFIX)) ||
                                        ((featureType == null) && ((String)val).startsWith("xri:")) ||
                                        ((featureType != null) && (PrimitiveTypes.OBJECT_ID.equals(featureType) || this.model.isClassType(featureType)))
                                ) {
                                    // Get path from normalized form (p$$<feature>_rid, p$$<feature>_oid
                                    if(this.useNormalizedReferences) {
                                        String referenceColumn = this.privateAttributesPrefix + featureName + this.referenceIdAttributesSuffix;
                                        String objectIdColumn = this.privateAttributesPrefix + featureName + this.objectIdAttributesSuffix;
                                        // Reference is stored in format p$$<feature>__rid, p$$<feature>__oid
                                        if(
                                                (frs.getColumnNames().contains(referenceColumn)) &&
                                                (frs.getColumnNames().contains(objectIdColumn))
                                        ) {
                                            Object rid = frs.getObject(
                                                this.getColumnName(
                                                    conn, 
                                                    referenceColumn, 0, false, false
                                                )
                                            );
                                            String oid = (String)frs.getObject(
                                                this.getColumnName(
                                                    conn, 
                                                    objectIdColumn, 0, false, false
                                                )
                                            );
                                            if(rid != null) {
                                                current.values(featureName).set(
                                                    valueIdx, 
                                                    this.getReference(
                                                        conn,
                                                        rid
                                                    ).getChild(oid)                                
                                                );
                                            }
                                        }
                                        // Reference is stored in compressed form in column
                                        else {
                                            String ref = (String)frs.getObject(
                                                this.getColumnName(
                                                    conn, 
                                                    featureName, 
                                                    0, 
                                                    false, 
                                                    true
                                                )
                                            );
                                            String rid = ref.substring(0, ref.lastIndexOf("/"));
                                            String oid = ref.substring(ref.lastIndexOf("/") + 1);
                                            current.values(featureName).set(
                                                valueIdx, 
                                                this.getReference(
                                                    conn,
                                                    rid
                                                ).getChild(oid)                                
                                            );
                                        }
                                    } 
                                    // get path from non-normalized, stringified form
                                    else {
                                        try {    
                                            current.values(featureName).set(
                                                valueIdx, 
                                                this.internalizePathValue((String)val)
                                            );
                                        } catch(Exception e) {
                                            current.values(featureName).set(
                                                valueIdx, 
                                                val
                                            );
                                        }
                                    }    
                                } 
                                // string
                                else {
                                    if(val != null) {
                                        this.setValue(
                                            conn,
                                            current.values(featureName),
                                            valueIdx,
                                            val,
                                            featureType
                                        );
                                    }
                                }
                            } 
                            // byte[], Blob
                            else if(this.isBlobColumnValue(val)) {
                                current.values(featureName).set(
                                    valueIdx,
                                    this.getBlobColumnValue(val, featureName, featureDef)
                                );
                            } 
                            // Null
                            else if(val == null) {
                                // Touch feature if value is null and feature is member of class
                                if(valueIdx == 0) {
                                    String view = featureName.indexOf(":") > 0
                                    ? featureName.substring(0, featureName.lastIndexOf(":") + 1)
                                        : "";
                                    this.touchNonDerivedFeature(
                                        current,
                                        view,
                                        featureName
                                    );
                                }
                            }
                            // Other types
                            else {
                                this.setValue(
                                    conn,
                                    current.values(featureName),
                                    valueIdx,
                                    val,
                                    featureType
                                );
                            }
                        }
                    }
                }
            }
            hasMore = frs.next();
            processedIdxs.add(new Integer(idx));
        }
        return hasMore;
    }

    //---------------------------------------------------------------------------
    private void setValue(
        Connection conn,      
        SparseList target,
        int idx,
        Object val,
        String featureType
    ) throws ServiceException{
        if(
                PrimitiveTypes.BOOLEAN.equals(featureType) ||
                val instanceof Boolean
        ) {
            //
            // org:: w3c::boolean
            //
            target.set(
                idx, 
                this.booleanMarshaller.unmarshal(val, conn)
            );
        }      
        else if(
                PrimitiveTypes.DATE.equals(featureType) &&
                val instanceof Timestamp
        ) {
            // Some Jdbc drivers / databases may return java.sql.Timestamp instead
            // of java.sql.Date even if the column type is Date. Force the conversion
            // from Timestamp to date.
            target.set(
                idx, 
                this.calendarMarshaller.unmarshal(val.toString().substring(0, 10))
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
            target.set(
                idx, 
                this.calendarMarshaller.unmarshal(val)
            );
        } 
        //
        // org::w3c::duration
        //
        else if(PrimitiveTypes.DURATION.equals(featureType)) {
            target.set(
                idx, 
                this.durationMarshaller.unmarshal(val)
            );
        } 
        //
        // openMDX 1 clients expect all numbers to be returned 
        // as BigIntegers
        //
        else if(val instanceof Number) {
            target.set(
                idx, 
                val instanceof BigDecimal 
                ? val 
                    : new BigDecimal(val.toString())
            );
        } 
        //
        // default 
        //
        else if(val instanceof String) {
            target.set(
                idx, 
                val
            );
        } 
        //
        // unknown 
        //
        else {
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.NOT_SUPPORTED, 
                "invalid column type. Supported are [Number|String|byte[]|Blob|Timestamp|Time]",
                new BasicException.Parameter("featureType", featureType),
                new BasicException.Parameter("columnType", val.getClass().getName())
            );
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
    private String attributeFilterToSqlClause(
        Connection conn,
        String view,
        DbObject dbObject,
        List filterProperties,
        boolean negate,
        Collection filterValues,
        boolean singleValued
    ) throws ServiceException {

        String clause = "";
        List clauseFilterValues = new ArrayList();    
        boolean hasProperties = false;
        boolean viewIsIndexed = dbObject.getIndexColumn() != null;
        String operator = "";

        /**
         * Generate clause for all filter properties:
         * If minus --> negate(expr0) OR negate(expr1) ...
         * If !minus --> expr0 AND expr1 ... 
         */    
        for(
                Iterator i = filterProperties.iterator();
                i.hasNext();
        ) {
            FilterProperty p = (FilterProperty)i.next();
            /**
             * FOR_ALL --> all attribute values must match --> all slices must match
             * THERE_EXISTS --> at least one attribute value must match --> at least one row must match --> subtract all rows which do not match
             */
            if(
                    (!negate && (p.quantor() == Quantors.THERE_EXISTS)) ||
                    (negate && (p.quantor() == Quantors.FOR_ALL))
            ) {
                // For embedded features the clause is of the form (expr0 OR expr1 OR ... OR exprN)
                // where N is the upper bound for the embedded feature 
                int upperBound = this.embeddedFeatures.containsKey(p.name())
                ? ((Number)this.embeddedFeatures.get(p.name())).intValue()
                    : 1;
                clause += operator + "(";
                for(
                        int idx = 0; 
                        idx < upperBound; 
                        idx++
                ) {
                    String columnName = this.getColumnName(conn, p.name(), idx, upperBound > 1, true);
                    String filterPropertyName = 
                        singleValued && viewIsIndexed
                        ? "vm." + columnName // get from mixin view 'vm'
                            : "v." + columnName; // get from main view 'v'
                    if(idx > 0) {
                        clause += " OR ";
                    }
                    clause += "(";
                    clause += this.filterPropertyToSql(
                        conn,
                        p, 
                        negate && !singleValued,
                        filterPropertyName, 
                        clauseFilterValues
                    );
                    if(singleValued && (p.quantor() == Quantors.FOR_ALL)) {
                        clause += " OR (" + filterPropertyName + " IS NULL)";
                    }
                    clause += ")";
                }     
                clause += ")";
                hasProperties = true;
            }
            if(hasProperties) {
                operator = negate && !singleValued ? " OR " : " AND ";
            }
        }

        if(hasProperties) {
            if(singleValued) {
                filterValues.addAll(clauseFilterValues);
                return clause;
            }
            else {
                // View is the secondary db object containing the multi-valued colums
                // of an object. This is why reference clause 2 must be used for selection
                boolean hasSecondaryDbObject =
                    (dbObject.getConfiguration().getDbObjectForQuery2() != null) ||
                    (dbObject.getConfiguration().getDbObjectForUpdate2() != null);
                if(hasSecondaryDbObject) {
                    filterValues.addAll(clauseFilterValues);
                    return 
                    "(v." + (dbObject.getConfiguration().getDbObjectsForQueryJoinColumn() == null ? dbObject.getObjectIdColumn().get(0) : dbObject.getConfiguration().getDbObjectsForQueryJoinColumn()) + " " + (negate ? "NOT" : "") +  " IN " +
                    "(SELECT " + dbObject.getObjectIdColumn().get(0) + " FROM " + (view.startsWith("SELECT") ? "(" + view + ") v" : view + " v ") +
                    " WHERE (" + clause + ")))";
                }
                else {
                    filterValues.addAll(dbObject.getReferenceValues());
                    filterValues.addAll(clauseFilterValues);
                    return 
                    "(v." + (dbObject.getConfiguration().getDbObjectsForQueryJoinColumn() == null ? dbObject.getObjectIdColumn().get(0) : dbObject.getConfiguration().getDbObjectsForQueryJoinColumn()) + " " + (negate ? "NOT" : "") +  " IN " +
                    "(SELECT " + dbObject.getObjectIdColumn().get(0) + " FROM " + (view.startsWith("SELECT") ? "(" + view + ") v" : view + " v ") +
                    " WHERE " + dbObject.getReferenceClause() + 
                    " AND (" + clause + ")))";            
                }
            }
        }
        else {
            return "";
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Appends an SQL clause to 'clause' according to filter property 'p'.
     * if 'negate' is true, then the operation is negated. The clause is
     * generated for SQL column with 'columnName'. The clause is generated
     * prepared statement using ? as place holders. The corresponding 
     * values are added to filterValues. The generated clause is of the
     * form (columnName operator literal). ANY or EACH are not handled
     * by this method.
     *
     */
    private String filterPropertyToSql(
        Connection conn,
        FilterProperty p,
        boolean negate,
        String columnName,
        Collection filterValues
    ) throws ServiceException {

        String clause = "";
        int operator = p.operator();
        int quantor = p.quantor();
        if(negate) {
            quantor = quantor == Quantors.THERE_EXISTS ? Quantors.FOR_ALL : Quantors.THERE_EXISTS;
            operator = - operator;
        }

        switch(operator) {

            /**
             * Evaluate the following:
             * THERE_EXISTS v IN A: v NOT IN Q (Special: if Q={} ==> true, iff A<>{}, false otherwise)
             * FOR_ALL v IN A: v NOT IN Q (Special: if Q={} ==> true) 
             */
            case FilterOperators.IS_NOT_IN:
                // Q = {}
                if(p.getValues().length == 0) {
                    if(quantor == Quantors.FOR_ALL) {
                        clause += "(1=1)";
                    }
                    else {
                        clause += "(" + columnName + " IS NOT NULL)";
                    }
                }

                // Q <> {}
                else {
                    clause += "(" + columnName + " NOT IN (" + getPlaceHolder(conn, p.getValue(0));
                    filterValues.add(this.externalizeStringValue(columnName, p.getValue(0)));
                    for(
                            int j = 1; 
                            j < p.getValues().length; 
                            j++
                    ) {
                        clause += ", " + getPlaceHolder(conn, p.getValue(j));
                        filterValues.add(this.externalizeStringValue(columnName, p.getValue(j)));
                    }
                    clause += "))";
                }
                break;

                // IS_LESS
            case FilterOperators.IS_LESS:
                clause += "(" + columnName + " < " + getPlaceHolder(conn, p.getValue(0)) + ")";
                filterValues.add(this.externalizeStringValue(columnName, p.getValue(0)));
                break;

                // IS_LESS_OR_EQUAL
            case FilterOperators.IS_LESS_OR_EQUAL:
                clause += "(" + columnName + " <= " + getPlaceHolder(conn, p.getValue(0)) + ")";
                filterValues.add(this.externalizeStringValue(columnName, p.getValue(0)));
                break;

                // IS_IN         
                // Evaluate the following:
                // THERE_EXISTS v IN A: v IN Q (Special: if Q={} ==> false)
                // FOR_ALL v IN A: v IN Q (Special: if Q={} ==> true, iff A={}, false otherwise)
            case FilterOperators.IS_IN:   

                // Q = {}
                if(p.getValues().length == 0) {
                    if(quantor == Quantors.THERE_EXISTS) {
                        clause += "(1=0)";
                    }
                    else {
                        clause += "(" + columnName + " IS NULL)";
                    }
                } 

                // Q <> {}
                else {
                    clause += "(" + columnName + " IN (?";
                    filterValues.add(this.externalizeStringValue(columnName, p.getValue(0)));
                    for(
                            int j = 1; 
                            j < p.getValues().length; 
                            j++
                    ) {
                        clause += ", ?";
                        filterValues.add(this.externalizeStringValue(columnName, p.getValue(j)));
                    }
                    clause += "))";
                }
                break;

                // IS_GREATER_OR_EQUAL
            case FilterOperators.IS_GREATER_OR_EQUAL:
                clause += "(" + columnName + " >= " + getPlaceHolder(conn, p.getValue(0)) + ")";
                filterValues.add(this.externalizeStringValue(columnName, p.getValue(0)));
                break;

                // IS_GREATER
            case FilterOperators.IS_GREATER:
                clause += "(" + columnName + " > " + getPlaceHolder(conn, p.getValue(0)) + ")";
                filterValues.add(this.externalizeStringValue(columnName, p.getValue(0)));
                break;

                // IS_BETWEEN
            case FilterOperators.IS_BETWEEN:
                clause += "((" + columnName + " >= " + getPlaceHolder(conn, p.getValue(0)) + ") AND (" + columnName + " <= " + getPlaceHolder(conn, p.getValue(1)) + "))";
                filterValues.add(this.externalizeStringValue(columnName, p.getValue(0)));
                filterValues.add(this.externalizeStringValue(columnName, p.getValue(1)));
                break;

                // IS_OUTSIDE
            case FilterOperators.IS_OUTSIDE:
                clause += "((" + columnName + " < " + getPlaceHolder(conn, p.getValue(0)) + ") OR (" + columnName + " > " + getPlaceHolder(conn, p.getValue(1)) + "))";
                filterValues.add(this.externalizeStringValue(columnName, p.getValue(0)));
                filterValues.add(this.externalizeStringValue(columnName, p.getValue(1)));
                break;

                // IS_LIKE
            case FilterOperators.IS_LIKE:
                clause += "(";
                for(
                        int j = 0; 
                        j < p.getValues().length; 
                        j++
                ) {
                    if(j > 0) {
                        clause += " OR ";
                    }
                    Object v = p.getValue(j);
                    if(v instanceof Path) {            
                        Path vp = (Path)v;
                        Set matchingPatterns = new HashSet();
                        boolean includeSubTree = "%".equals(vp.getBase());
                        // If path ends with % get the set of matching types
                        if(includeSubTree) {
                            vp.remove(vp.size()-1);
                            for(
                                    Iterator k = this.configuration.getDbObjectConfigurations().iterator(); 
                                    k.hasNext(); 
                            ) {
                                DbObjectConfiguration dbObjectConfiguration = (DbObjectConfiguration)k.next();
                                if(
                                        (dbObjectConfiguration.getType().size() > vp.size()) &&
                                        vp.isLike(dbObjectConfiguration.getType().getPrefix(vp.size()))
                                ) {
                                    // Replace type pattern by filter value pattern
                                    Path type = dbObjectConfiguration.getType();
                                    Path pattern = new Path("");
                                    for(int l = 0; l < type.size(); l++) {
                                        if(type.get(l).equals(":*")) {
                                            pattern.add("%");
                                        }
                                        else {
                                            pattern.add(type.get(l));
                                        }
                                    }
                                    matchingPatterns.add(pattern);
                                }
                            }
                        }
                        else {
                            matchingPatterns.add(vp);           
                        }
                        // LIKE clause for each path pattern
                        int ii = 0;
                        for(
                                Iterator i = matchingPatterns.iterator();
                                i.hasNext();
                                ii++
                        ) {
                            Path pattern = (Path)i.next();
                            if(ii > 0) {
                                clause += " OR ";
                            }
                            clause += "(" + columnName + " LIKE ? " + getEscapeClause(conn) + ")";              
                            String externalized = this.externalizePathValue(
                                conn, 
                                new Path(vp).addAll(pattern.getSuffix(vp.size()))
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
                                        StackedException.DEFAULT_DOMAIN,
                                        StackedException.NOT_SUPPORTED, 
                                        "only (***|%) wildcards at the end supported",
                                        new BasicException.Parameter("value", v),
                                        new BasicException.Parameter("path", externalized),
                                        new BasicException.Parameter("position", pos)                          
                                    );
                                }
                            }
                            else if(externalized.startsWith("spice:")) {
                                for(int k = 0; k < ((Path)v).size(); k++) {
                                    String c = ((Path)v).get(k);
                                    if(c.startsWith(":") && c.endsWith("*")) {
                                        throw new ServiceException(
                                            StackedException.DEFAULT_DOMAIN,
                                            StackedException.NOT_SUPPORTED, 
                                            "path component pattern ':<pattern>*' not supported",
                                            new BasicException.Parameter("path", externalized),
                                            new BasicException.Parameter("position", i)                          
                                        );
                                    }
                                }
                            }
                            filterValues.add(externalized);
                        }
                    }
                    else {
                        Object externalized = this.externalizeStringValue(columnName, v);
                        if(
                                (externalized instanceof String) &&
                                ((String)externalized).startsWith(JDO_CASE_INSENSITIVE_FLAG)
                        ) {
                            clause += "(UPPER(" + columnName + ") LIKE ? " + getEscapeClause(conn) + ")";
                            filterValues.add(
                                ((String)externalized).substring(JDO_CASE_INSENSITIVE_FLAG.length()).toUpperCase()
                            );
                        }
                        else {
                            clause += "(" + columnName + " LIKE ? " + getEscapeClause(conn) + ")";
                            filterValues.add(externalized);                  
                        }
                    }
                }
                clause += ")";
                break;

                // IS_UNLIKE
            case FilterOperators.IS_UNLIKE:
                clause += "(";
                for(
                        int j = 0; 
                        j < p.getValues().length; 
                        j++
                ) {
                    if(j > 0) {
                        clause += " AND ";
                    }
                    Object v = p.getValue(j);
                    if(v instanceof Path) {
                        Path vp = (Path)v;
                        Set matchingTypes = new HashSet();
                        matchingTypes.add(vp);
                        boolean includeSubTree = "%".equals(vp.getBase());
                        // If path ends with % get the set of matching types
                        if(includeSubTree) {
                            vp.remove(vp.size()-1);
                            for(
                                    Iterator i = this.configuration.getDbObjectConfigurations().iterator(); 
                                    i.hasNext(); 
                            ) {
                                DbObjectConfiguration dbObject = (DbObjectConfiguration)i.next();
                                if(
                                        (dbObject.getType().size() > vp.size()) &&
                                        vp.isLike(dbObject.getType().getPrefix(vp.size()))
                                ) {
                                    matchingTypes.add(
                                        dbObject.getType()
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
                                clause += " AND ";
                            }
                            clause += "(NOT (" + columnName + " LIKE ? " + getEscapeClause(conn) + "))";
                            String externalized = this.externalizePathValue(
                                conn, 
                                new Path(vp).addAll(type.getSuffix(vp.size()))
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
                                        StackedException.DEFAULT_DOMAIN,
                                        StackedException.NOT_SUPPORTED, 
                                        "only (***|%) wildcards at the end supported",
                                        new BasicException.Parameter("value", v),
                                        new BasicException.Parameter("path", externalized),
                                        new BasicException.Parameter("position", pos)                          
                                    );
                                }
                            }
                            else if(externalized.startsWith("spice:")) {
                                for(int k = 0; k < ((Path)v).size(); k++) {
                                    String c = ((Path)v).get(k);
                                    if(c.startsWith(":") && c.endsWith("*")) {
                                        throw new ServiceException(
                                            StackedException.DEFAULT_DOMAIN,
                                            StackedException.NOT_SUPPORTED, 
                                            "path component pattern ':<pattern>*' not supported",
                                            new BasicException.Parameter("path", externalized),
                                            new BasicException.Parameter("position", i)                          
                                        );
                                    }
                                }
                            }
                            filterValues.add(externalized);
                        }
                    }
                    else {
                        Object externalized = this.externalizeStringValue(columnName, v);
                        if(
                                (externalized instanceof String) &&
                                ((String)externalized).startsWith(JDO_CASE_INSENSITIVE_FLAG)
                        ) {
                            clause += "(NOT (UPPER(" + columnName + ") LIKE ? " + getEscapeClause(conn) + "))";
                            filterValues.add(
                                ((String)externalized).substring(JDO_CASE_INSENSITIVE_FLAG.length()).toUpperCase()
                            );
                        }
                        else {
                            clause += "(NOT (" + columnName + " LIKE ? " + getEscapeClause(conn) + "))";
                            filterValues.add(externalized);                  
                        }
                    }
                }
                clause += ")";
                break;

                // SOUNDS_LIKE
            case FilterOperators.SOUNDS_LIKE:
                clause += "(SOUNDEX(" + columnName + ") IN (SOUNDEX(?)";
                filterValues.add(this.externalizeStringValue(columnName, p.getValue(0)));
                for(
                        int j = 1; 
                        j < p.getValues().length; 
                        j++
                ) {
                    clause += ", SOUNDEX(?)";
                    filterValues.add(this.externalizeStringValue(columnName, p.getValue(j)));
                }
                clause += "))";
                break;

                // SOUNDS_UNLIKE
            case FilterOperators.SOUNDS_UNLIKE:
                clause += "(SOUNDEX(" + columnName + ") NOT IN (SOUNDEX(?)";
                filterValues.add(this.externalizeStringValue(columnName, p.getValue(0)));
                for(
                        int j = 1; 
                        j < p.getValues().length; 
                        j++
                ) {
                    clause += ", SOUNDEX(?)";
                    filterValues.add(this.externalizeStringValue(columnName, p.getValue(j)));
                }
                clause += "))";
                break;

                // unsupported
            default: 
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.BAD_PARAMETER,
                    "Unsupported operator", 
                    new BasicException.Parameter("operator", FilterOperators.toString(operator))
                );
        }
        return clause;
    }

    //------------------------------------------------------------------------
    private List getSingleValuedFilterProperties(
        Path referenceFilter,
        List attributeFilter
    ) throws ServiceException {

        List singleValuedFilterProperties = new ArrayList();

        // try to determine multiplicity of filter properties in order to optimize query
        Set referencedTypes = null;
        try {
            ModelElement_1_0 referencedTypeDef = this.model == null 
            ? null 
                : this.model.getTypes(referenceFilter)[2];
            // filter property can be an attribute of all super and subtypes of referenced type
            SysLog.trace("referenced type", referencedTypeDef.path());
            referencedTypes = new HashSet();
            // All subtypes and their supertypes of referenced type
            for(Iterator i = referencedTypeDef.values("allSubtype").iterator(); i.hasNext(); ) {
                ModelElement_1_0 subtype = this.model.getElement(i.next());
                referencedTypes.add(subtype);
                for(Iterator j = subtype.values("allSupertype").iterator(); j.hasNext(); ) {
                    ModelElement_1_0 supertype = this.model.getElement(j.next());
                    referencedTypes.add(supertype);
                }
            }
        }
        catch(Exception e) {
            // ignore
        }        
        for(
                Iterator i = attributeFilter.iterator();
                i.hasNext();
        ) {
            FilterProperty p = (FilterProperty)i.next();

            /**
             * Try to determine the modeled multiplicity of the filter property.
             * Set 0..n as default multiplicity. If no model is configured are the
             * multiplicity can not be determined for some reason take the default
             * multiplicity and issue a warning.
             */
            String filterPropertyMultiplicity = this.singleValueAttributes.contains(p.name())
            ? Multiplicities.SINGLE_VALUE
                : Multiplicities.MULTI_VALUE; 
            try {
                if(referencedTypes != null) {

                    // get filter property name and eliminate prefixes such as role_id, etc.
                    String filterPropertyName = p.name();
                    filterPropertyName = filterPropertyName.substring(filterPropertyName.lastIndexOf('$') + 1);

                    // try to find filter property in any of the subtypes of referencedType
                    boolean found = false;
                    for(
                            Iterator j = referencedTypes.iterator();
                            j.hasNext();
                    ) {
                        ModelElement_1_0 subtype = (ModelElement_1_0)j.next();
                        String qualifiedFilterPropertyName = subtype.values("qualifiedName").get(0) + ":" + filterPropertyName;
                        SysLog.trace("lookup of model element", qualifiedFilterPropertyName);
                        ModelElement_1_0 featureDef = this.model.findElement(qualifiedFilterPropertyName);
                        if(featureDef != null) {
                            filterPropertyMultiplicity = (String)featureDef.values("multiplicity").get(0);                  
                            // Reference stored as attribute. Multiplicity is 0..1 if there is no qualifier
                            if(featureDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.REFERENCE)) {
                                ModelElement_1_0 referencedEnd = this.model.getElement(
                                    featureDef.values("referencedEnd").get(0)
                                );
                                if(referencedEnd.values("qualifierType").size() > 0 ) {
                                    filterPropertyMultiplicity = Multiplicities.MULTI_VALUE;
                                }
                            }
                            found = true;
                            break;
                        }
                    }
                    SysLog.trace("filter property " + filterPropertyName + " found=" + found);
                    SysLog.trace("filter property multiplicity " + filterPropertyMultiplicity);
                }
            }
            catch(ServiceException e) {
                SysLog.warning("The following error occured when trying to determine multiplicity of filter property");
                e.log();
            }

            // multiplicity *..1          
            if(
                    Multiplicities.OPTIONAL_VALUE.equals(filterPropertyMultiplicity) || 
                    Multiplicities.SINGLE_VALUE.equals(filterPropertyMultiplicity)
            ) {
                SysLog.trace("filter property " + p.name() + " is single-valued");
                singleValuedFilterProperties.add(p);
            }
        }
        return singleValuedFilterProperties;
    }

    //------------------------------------------------------------------------
    private void removePrivateAttributes(
        DataproviderObject object
    ) {
        // remove attributes with private prefix
        for(
                Iterator i = object.attributeNames().iterator();
                i.hasNext();
        ) {
            String attributeName = (String)i.next();
            if(attributeName.startsWith(this.privateAttributesPrefix)) {
                i.remove();
            }
        }    
    }

    //------------------------------------------------------------------------
    private void completeObject(
        DataproviderObject object
    ) {
        // segment-level objects managed by Database_1 provide the namespace 'Database'
        if(object.path().size() == 5) {
            object.clearValues(DATASTORE_PREFIX + SystemAttributes.OBJECT_CLASS).add(
                "org:openmdx:datastore1:Database"
            );
        }
        this.removePrivateAttributes(object);
    }

    //------------------------------------------------------------------------
    private DataproviderReply completeReply(
        DataproviderReply reply
    ) {
        for(int i = 0; i < reply.getObjects().length; i++) {
            this.completeObject(
                reply.getObjects()[i]
            );
        }
        return reply;
    }

    //---------------------------------------------------------------------------
    private DataproviderObject addBeforeImage(
        DataproviderObject obj,
        DataproviderObject beforeImage
    ) throws ServiceException {
        for(
                Iterator i = beforeImage.attributeNames().iterator();
                i.hasNext();
        ) {
            String attributeName = (String)i.next();
            if(attributeName.indexOf(':')<0) {
                obj.clearValues(SystemAttributes.CONTEXT_PREFIX+"BeforeImage:" + attributeName).addAll(
                    beforeImage.values(attributeName)
                );
            }
        }
        return obj;
    }

    //---------------------------------------------------------------------------
    private DbObject createDbObject(
        Connection conn,
        DbObjectConfiguration dbObjectConfiguration,
        Path accessPath,
        boolean isQuery
    ) throws ServiceException {
        return this.createDbObject(
            conn,
            dbObjectConfiguration,
            accessPath,
            null,
            isQuery
        );
    }

    //---------------------------------------------------------------------------
    private DbObject createDbObject(
        Connection conn,
        Path accessPath,
        boolean isQuery
    ) throws ServiceException {
        return this.createDbObject(
            conn,
            null,
            accessPath,
            null,
            isQuery
        );
    }

    //---------------------------------------------------------------------------
    private DbObject createDbObject(
        Connection conn,
        Path accessPath,
        FilterProperty[] filter,
        boolean isQuery
    ) throws ServiceException {
        return this.createDbObject(
            conn,
            null,
            accessPath,
            filter,
            isQuery
        );
    }

    //---------------------------------------------------------------------------
    private DbObject createDbObject(
        Connection conn,
        DbObjectConfiguration _dbObjectConfiguration,
        Path _accessPath,
        FilterProperty[] filter,
        boolean isQuery
    ) throws ServiceException {
        DbObjectConfiguration dbObjectConfiguration = _dbObjectConfiguration;
        Path accessPath = _accessPath;
        boolean isExtent = false;

        // extent access requires special treatment
        // get the set of referenceIds specified by filter property 'identity'
        if(filter != null && accessPath.isLike(EXTENT_PATTERN)) {
            for(
                    int i = 0;
                    i < filter.length;
                    i++
            ) {
                FilterProperty p = filter[i];
                if(SystemAttributes.OBJECT_IDENTITY.equals(p.name())) {
                    if(p.values().size() > 1) {
                        throw new ServiceException(
                            StackedException.DEFAULT_DOMAIN,
                            StackedException.NOT_SUPPORTED, 
                            "at most one value allowed for filter property 'identity'",
                            new BasicException.Parameter("filter", (Object[])filter)
                        );                        
                    }
                    isExtent = true;
                    accessPath = new Path(p.values().iterator().next().toString());
                }
            }
            if(!isExtent) {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED, 
                    "extent lookups require at least a filter value for property 'identity'",
                    new BasicException.Parameter("filter", (Object[])filter)
                );            
            }
        }

        if(dbObjectConfiguration == null) {
            Path referencedType = accessPath.size() % 2 == 0 ? accessPath : accessPath.getParent(); 
            dbObjectConfiguration = this.configuration.getDbObjectConfiguration(
                referencedType
            );
        }

        if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED.equals(dbObjectConfiguration.getDbObjectFormat())) {
            return new SlicedDbObject(
                this,
                conn,
                dbObjectConfiguration,
                accessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_NON_INDEXED.equals(dbObjectConfiguration.getDbObjectFormat())) {
            return new SlicedDbObjectNonIndexed(
                this,
                conn,
                dbObjectConfiguration,
                accessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_PARENT_RID_ONLY.equals(dbObjectConfiguration.getDbObjectFormat())) {
            return new SlicedDbObjectParentRidOnly(
                this,
                conn,
                dbObjectConfiguration,
                accessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_NON_INDEXED_PARENT_RID_ONLY.equals(dbObjectConfiguration.getDbObjectFormat())) {
            return new SlicedDbObjectNonIndexedParentRidOnly(
                this,
                conn,
                dbObjectConfiguration,
                accessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_WITH_ID_AS_KEY.equals(dbObjectConfiguration.getDbObjectFormat())) {
            return new DBOSlicedWithIdAsKey(
                this,
                conn,
                dbObjectConfiguration,
                accessPath,
                isExtent,
                isQuery
            );
        }
        else if(LayerConfigurationEntries.DB_OBJECT_FORMAT_SLICED_WITH_PARENT_AND_ID_AS_KEY.equals(dbObjectConfiguration.getDbObjectFormat())) {
            return new DBOSlicedWithParentAndIdAsKey(
                this,
                conn,
                dbObjectConfiguration,
                accessPath,
                isExtent,
                isQuery
            );
        }
        else {
            DbObject dbObject = null;
            try {
                Class dbObjectClass = Class.forName(dbObjectConfiguration.getDbObjectFormat());
                Constructor constructor = dbObjectClass.getConstructor(
                    new Class[]{
                        AbstractDatabase_1.class,
                        Connection.class,
                        DbObjectConfiguration.class,
                        Path.class,
                        boolean.class,
                        boolean.class
                    }
                );
                dbObject = (DbObject)constructor.newInstance(
                    this,
                    conn,
                    dbObjectConfiguration,
                    accessPath,
                    Boolean.valueOf(isExtent),
                    Boolean.valueOf(isQuery)
                );
            }
            catch(Exception e) {
                throw new ServiceException(
                    e,
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.MEDIA_ACCESS_FAILURE, 
                    "can not create DbObject",
                    new BasicException.Parameter("path", accessPath),
                    new BasicException.Parameter("type", dbObjectConfiguration.getDbObjectFormat())
                );
            }
            return dbObject;
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Remove view prefix 'v.' from column names in clause
     */
    private String removeViewPrefix(
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
    // Layer_1_0
    //---------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#prolog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[])
     */
    public void prolog(
        ServiceHeader header, 
        DataproviderRequest[] requests
    ) throws ServiceException {
        super.prolog(
            header, 
            requests
        );
        this.configuration.activate();
        this.unitOfWorkId = "";
    }

    //---------------------------------------------------------------------------
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies  ) throws ServiceException {
        for(
                Iterator i = this.temporaryFiles.iterator();
                i.hasNext();
        ){
            File f = (File) i.next();
            if(!f.delete()) SysLog.warning(
                "Could not delete temporary file",
                f
            );
            i.remove();          
        }
        super.epilog(
            header,
            requests,
            replies
        );    
    }

    //---------------------------------------------------------------------------
    // Layer_1_2
    //---------------------------------------------------------------------------

    public void prolog(
        ServiceHeader header, UnitOfWorkRequest request
    ) {
        this.objectCache.clear();
    }

    //---------------------------------------------------------------------------
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        SysLog.detail("> get", request.object());    
        Connection conn = null;
        DataproviderReply reply = null;
        try {  
            conn = this.getConnection(request);
            reply = this.get(
                conn,
                header,
                request
            );
        }
        catch(ServiceException e) {
            throw e;
        }
        finally {
            try {
                this.closeConnection(request, conn);
            }
            catch(Throwable ex) {
                // ignore
            }
        }
        return reply;
    }

    //---------------------------------------------------------------------------
    protected DataproviderObject getPartialObject(
        Connection conn,
        DataproviderRequest request,        
        DbObject dbObject,
        boolean primaryColumns,
        String objectClass
    ) throws ServiceException {
        PreparedStatement ps = null;
        String currentStatement = null;
        ResultSet rs = null;
        try {
            String view1 = this.getView(
                conn,
                dbObject,
                VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY,
                null,
                null
            );
            String view2 = this.getView(
                conn,
                dbObject,
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
            List statementParameters = new ArrayList();
            int pos = 1;
            if(useReferenceClause) {
                List referenceValues = dbObject.getReferenceValues();
                statementParameters.addAll(referenceValues);
                for(int i = 0; i < referenceValues.size(); i++) {
                    this.setPreparedStatementValue(
                        conn, 
                        ps, 
                        pos++, 
                        referenceValues.get(i)
                    );
                }
            }
            List objectIdValues = dbObject.getObjectIdValues();
            statementParameters.addAll(objectIdValues);
            for(int i = 0; i < objectIdValues.size(); i++) {
                this.setPreparedStatementValue(
                    conn,
                    ps, 
                    pos++, 
                    objectIdValues.get(i)
                );
            }      
            rs = this.executeQuery(
                ps,
                currentStatement,
                statementParameters
            );
            DataproviderObject replyObj = this.getObject(
                conn, 
                request, 
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
                StackedException.DEFAULT_DOMAIN,
                StackedException.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", request.path()),
                new BasicException.Parameter("statement", currentStatement)
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
        DataproviderRequest request
    ) throws ServiceException {

        DataproviderObject replyObj = null;
        if(this.useObjectCache && this.objectCache.containsKey(request.path())) {
            replyObj = new DataproviderObject(
                (DataproviderObject)this.objectCache.get(request.path())
            );
        }
        else {
            try {
                DbObject dbObject = null;
                try {
                    dbObject = this.createDbObject(
                        conn,
                        request.path(),
                        true
                    );
                }
                catch(ServiceException e) {
                    if(e.getExceptionStack().getExceptionCode() != BasicException.Code.NOT_FOUND) {
                        throw e;
                    }
                    throw new ServiceException(
                        StackedException.DEFAULT_DOMAIN,
                        StackedException.NOT_FOUND, 
                        "object not found",
                        new BasicException.Parameter("path", request.path())
                    );
                }
                // Get primary attributes
                boolean hasSecondaryDbObject =
                    (dbObject.getConfiguration().getDbObjectForQuery2() != null) ||
                    (dbObject.getConfiguration().getDbObjectForUpdate2() != null);          
                replyObj = this.getPartialObject(
                    conn,
                    request,
                    dbObject,
                    true,
                    null
                );
                // Get attributes from secondary db object
                if(hasSecondaryDbObject) {
                    DataproviderObject replyObj2 = this.getPartialObject(
                        conn,
                        request,
                        dbObject,
                        false,
                        (String)replyObj.values(SystemAttributes.OBJECT_CLASS).get(0)
                    );
                    replyObj.addClones(
                        replyObj2,
                        false
                    );
                }
            }
            catch(ServiceException e) {
                throw e;
            }
            catch(Exception ex) {
                throw new ServiceException(
                    ex, 
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.GENERIC, 
                    ex.toString()
                );
            }
            // Only cache if replyObj contains all persistent attributes. replyObj contains all 
            // persistent attributes in case the attribute selector is ALL_ATTRIBUTES or 
            // SPECIFIED_AND_TYPICAL_ATTRIBUTES. 
            if(
                    this.useObjectCache &&
                    (request.attributeSelector() == AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES) ||
                    (request.attributeSelector() == AttributeSelectors.ALL_ATTRIBUTES) 
            ) {
                this.objectCache.put(
                    replyObj.path(), 
                    new DataproviderObject(replyObj)
                );
            }
        }
        return this.completeReply(
            new DataproviderReply(replyObj)
        );
    }

    //---------------------------------------------------------------------------
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {

        PreparedStatement ps = null;
        String currentStatement = null;
        ResultSet rs = null;
        Connection conn = null;
        DataproviderReply reply = null;
        String statement = null;
        List statementParameters = null;
        Path referencedType = null;
        int lastPosition;
        int lastRowCount;
        boolean countResultSet = false;

        SysLog.detail("> find");
        try {

            conn = getConnection(request);
            DbObject dbObject = null;

            /**
             * prepare SELECT statement
             */
            // Continue
            if(request.operation() == DataproviderOperations.ITERATION_CONTINUATION) {
                SysLog.trace(DataproviderOperations.toString(DataproviderOperations.ITERATION_CONTINUATION));
                JdbcIterator jdbcIterator = (JdbcIterator)AbstractIterator.deserialize(
                    (byte[])request.context(DataproviderReplyContexts.ITERATOR).get(0)
                );
                referencedType = jdbcIterator.getReferencedType();
                statement = jdbcIterator.getStatement();
                statementParameters = jdbcIterator.getStatementParameters();
                lastPosition = jdbcIterator.getLastPosition();
                lastRowCount = jdbcIterator.getLastRowCount();
                dbObject = this.createDbObject(
                    conn,
                    request.path(),
                    // Reconstruct identity filter from referenced type in case of extent
                    new FilterProperty[]{
                        new FilterProperty(
                            Quantors.THERE_EXISTS, 
                            SystemAttributes.OBJECT_IDENTITY, 
                            FilterOperators.IS_LIKE,
                            referencedType
                        )
                    },
                    true
                );
            }

            // First
            else {
                SysLog.trace(DataproviderOperations.toString(DataproviderOperations.ITERATION_START));
                try {
                    dbObject = this.createDbObject(
                        conn,
                        request.path(),
                        request.attributeFilter(),
                        true
                    );
                }
                catch(ServiceException e) {
                    if(e.getExceptionStack().getExceptionCode() != BasicException.Code.NOT_FOUND) {
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
                    reply = new DataproviderReply(Collections.EMPTY_LIST);
                    // HAS_MORE
                    reply.context(
                        DataproviderReplyContexts.HAS_MORE
                    ).set(
                        0, 
                        Boolean.FALSE
                    );
                    // TOTAL
                    reply.context(
                        DataproviderReplyContexts.TOTAL
                    ).set(
                        0, 
                        INTEGER_0
                    );
                    // ATTRIBUTE_SELECTOR
                    reply.context(DataproviderReplyContexts.ATTRIBUTE_SELECTOR).set(
                        0,
                        new Short(request.attributeSelector())
                    );
                    SysLog.detail("< find");
                    return this.completeReply(reply);
                }
                referencedType = dbObject.getReferencedType();

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
                List attributeFilter = new ArrayList();
                String queryFilterClause = null;
                String columnSelector = DEFAULT_COLUMN_SELECTOR;
                Map queryFilterParameters = new HashMap();
                for(
                        int i = 0, iLimit = request.attributeFilter().length;
                        i < iLimit;
                        i++
                ) {
                    FilterProperty p = request.attributeFilter()[i];
                    // The filter property 'identity' requires special handling. It
                    // is mapped to the filter property 'object_oid operator values'
                    // This mapping is not required in case of an extent search because
                    // dbObject is already correctly prepared
                    if(SystemAttributes.OBJECT_IDENTITY.equals(p.name())) {
                        if(!request.path().isLike(EXTENT_PATTERN)) {
                            attributeFilter.add(
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
                            int posColumnSelector = queryFilterClause.indexOf(Database_1_Attributes.HINT_COLUMN_SELECTOR);
                            if(posColumnSelector >= 0) {
                                columnSelector = queryFilterClause.substring(
                                    posColumnSelector + Database_1_Attributes.HINT_COLUMN_SELECTOR.length(),
                                    queryFilterClause.indexOf("*/", posColumnSelector)
                                );
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
                    // Attribute
                    else {
                        attributeFilter.add(p);              
                    }
                }

                lastPosition = -1;
                lastRowCount = -1;

                // prepare orderBy attributes. In addition all orderBy attributes are required attributes
                List orderBy = new ArrayList();
                for(
                        int i = 0; 
                        i < request.attributeSpecifier().length; 
                        i++
                ) {
                    // only add to orderBy set if unspecified order
                    if(request.attributeSpecifier()[i].order() != Orders.ANY) {
                        orderBy.add(
                            request.attributeSpecifier()[i].name()
                        );
                    }
                }

                // Prepare filter properties stored in primary dbObject
                List primaryFilterProperties = this.getSingleValuedFilterProperties(
                    dbObject.getReference(),
                    attributeFilter
                );
                // Add filter properties which map to embedded features
                for(Iterator i = attributeFilter.iterator(); i.hasNext(); ) {
                    FilterProperty p = (FilterProperty)i.next();
                    if(this.embeddedFeatures.containsKey(p.name())) {
                        primaryFilterProperties.add(p);
                    }
                }

                // mixinAttributes = orderBy + singleValuedAttributes
                Set mixinAttributes = new HashSet(orderBy);
                for(Iterator i = primaryFilterProperties.iterator(); i.hasNext(); ) {
                    mixinAttributes.add(((FilterProperty)i.next()).name());
                }

                // View returning primary attributes. Allows sorting and
                // filtering with single-valued filter properties
                String view1WithMixinAttributes = this.getView(
                    conn,
                    dbObject,
                    VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY,
                    columnSelector,
                    mixinAttributes
                );

                // View returning multi-valued columns which allows filtering
                // of multi-valued filter properties
                String view2ForQuery = this.getView(
                    conn,
                    dbObject,
                    VIEW_MODE_SECONDARY_COLUMNS,
                    columnSelector,
                    null
                );

                List positiveAttributeFilterClauses = new ArrayList();
                List positiveAttributeFilterValues = new ArrayList();
                List negativeAttributeFilterClauses = new ArrayList();
                List negativeAttributeFilterValues = new ArrayList();
                List attributeFilterValues = null;                   

                // WHERE clause for single-valued filter properties          
                attributeFilterValues = new ArrayList();

                // positive single-valued filter
                positiveAttributeFilterClauses.add(
                    this.attributeFilterToSqlClause(
                        conn,
                        view1WithMixinAttributes,
                        dbObject,
                        primaryFilterProperties,
                        false, // negate
                        attributeFilterValues,
                        true
                    )
                );
                positiveAttributeFilterValues.add(attributeFilterValues);

                // negative single-valued filter
                attributeFilterValues = new ArrayList();
                positiveAttributeFilterClauses.add(
                    this.attributeFilterToSqlClause(
                        conn,
                        view1WithMixinAttributes,
                        dbObject,
                        primaryFilterProperties,
                        true, // negate
                        attributeFilterValues,
                        true
                    )
                );
                positiveAttributeFilterValues.add(attributeFilterValues);

                // WHERE clauses for multi-valued filter properties
                for(
                        Iterator i = attributeFilter.iterator();
                        i.hasNext();
                ) {
                    FilterProperty p = (FilterProperty)i.next();
                    if(!primaryFilterProperties.contains(p)) {

                        // plus clause
                        attributeFilterValues = new ArrayList();
                        positiveAttributeFilterClauses.add(
                            this.attributeFilterToSqlClause(
                                conn,
                                view2ForQuery,
                                dbObject,
                                Arrays.asList(new FilterProperty[]{p}),
                                false, // negate
                                attributeFilterValues,
                                false
                            )
                        );
                        positiveAttributeFilterValues.add(attributeFilterValues);

                        /**
                         * Minus clauses are only required if maxSlicesPerObject > 0. In this case
                         * non-matching slices have to be subtracted from the plus result set.
                         */
                        attributeFilterValues = new ArrayList();
                        negativeAttributeFilterClauses.add(
                            this.attributeFilterToSqlClause(
                                conn,
                                view2ForQuery,
                                dbObject,
                                Arrays.asList(new FilterProperty[]{p}),
                                true, // negate
                                attributeFilterValues,
                                false
                            )
                        );
                        negativeAttributeFilterValues.add(attributeFilterValues);
                    }
                }

                /**
                 * get all slices of objects which match the reference and attribute filter
                 */
                statement = "";
                statementParameters = new ArrayList();
                statement += view1WithMixinAttributes.startsWith("SELECT")
                ? view1WithMixinAttributes + " AND " + dbObject.getReferenceClause()
                    : "SELECT " + dbObject.getHint() + " " + columnSelector + " FROM " + view1WithMixinAttributes + " v WHERE " + dbObject.getReferenceClause();
                statementParameters.addAll(
                    dbObject.getReferenceValues()
                );

                // oid is not filled as statementParameters because it its value is varying
                // in case of iteration requests. It is filled in explicitely (see below).

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
                                ((List)queryFilterParameters.get("stringParam")).get(index)
                            );
                        }
                        else if(queryFilterClause.startsWith("?i", pos)) {
                            statementParameters.add(
                                ((List)queryFilterParameters.get("integerParam")).get(index)
                            );
                        }
                        else if(queryFilterClause.startsWith("?n", pos)) {
                            statementParameters.add(
                                ((List)queryFilterParameters.get("decimalParam")).get(index)
                            );
                        }
                        else if(queryFilterClause.startsWith("?b", pos)) {
                            statementParameters.add(
                                ((List)queryFilterParameters.get("booleanParam")).get(index)
                            );
                        }
                        else if(queryFilterClause.startsWith("?d", pos)) {
                            statementParameters.add(
                                ((List)queryFilterParameters.get("dateParam")).get(index)
                            );
                        }
                        else if(queryFilterClause.startsWith("?t", pos)) {
                            statementParameters.add(
                                ((List)queryFilterParameters.get("dateTimeParam")).get(index)
                            );
                        }
                        pos++;
                    }
                    statement += " AND (";
                    statement += queryFilterClause.replaceAll("(\\?[sinbdt]\\d)", "?");
                    statement += ")";
                }

                // positive attribute filter
                for(
                        int i = 0; 
                        i < positiveAttributeFilterClauses.size(); 
                        i++
                ) {
                    String filterClause = (String)positiveAttributeFilterClauses.get(i);
                    if(filterClause.length() > 0) { 
                        statement += " AND ";
                        statement += filterClause;
                        statementParameters.addAll(
                            (List)positiveAttributeFilterValues.get(i)
                        );
                    }
                }

                // negative attribute filter
                boolean hasNegativeAttributeFilterClauses = false;
                for(
                        int i = 0; 
                        i < negativeAttributeFilterClauses.size(); 
                        i++
                ) {
                    hasNegativeAttributeFilterClauses = hasNegativeAttributeFilterClauses || (((String)negativeAttributeFilterClauses.get(i)).length() > 0);
                }
                if(hasNegativeAttributeFilterClauses) {
                    for(
                            int i = 0; 
                            i < negativeAttributeFilterClauses.size(); 
                            i++
                    ) {
                        String filterClause = (String)negativeAttributeFilterClauses.get(i);
                        if(filterClause.length() > 0) { 
                            statement += " AND ";
                            statement += filterClause;
                            statementParameters.addAll(
                                (ArrayList)negativeAttributeFilterValues.get(i)
                            );
                        }
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
                    if(request.attributeSpecifier()[i].order() != Orders.ANY) {
                        if(!hasOrderBy) statement += " ORDER BY"; 
                        boolean viewIsIndexed = dbObject.getIndexColumn() != null;              
                        statement += hasOrderBy ? ", " : " ";
                        // order on mixin view (vm.) in case of indexed slices, otherwise on primary view (v.)
                        statement += (viewIsIndexed ? "vm." : "v.") + getColumnName(conn, specifier.name(), 0, false, true) + (specifier.order() == Directions.DESCENDING ? " DESC" : " ASC");
                        hasOrderBy = true;
                    }
                }
                // Order on reference and object columns only required if result set is indexed
                if(dbObject.getIndexColumn() != null) {
                    if(!hasOrderBy) statement += " ORDER BY"; 
                    // rid
                    for(int i = 0; i < dbObject.getReferenceColumn().size(); i++) {
                        statement += hasOrderBy ? ", " : " ";
                        statement += "v." + dbObject.getReferenceColumn().get(i);
                        hasOrderBy = true;
                    }
                    // oid
                    for(int i = 0; i < dbObject.getObjectIdColumn().size(); i++) {
                        statement += hasOrderBy ? ", " : " ";
                        statement += "v." + dbObject.getObjectIdColumn().get(i);         
                        hasOrderBy = true;
                    }
                    // idx
                    statement += hasOrderBy ? ", " : " ";
                    statement += "v." + dbObject.getIndexColumn();
                    hasOrderBy = true;
                }
            }

            // fill statement parameters    
            ps = this.prepareStatement(
                conn,
                currentStatement = statement.toString()
            );

            // fill in statementParameters
            for(
                    int i = 0, iLimit = statementParameters.size(); 
                    i < iLimit; 
                    i++
            ) {
                this.setPreparedStatementValue(
                    conn,
                    ps,
                    i+1, 
                    statementParameters.get(i)
                );
            }

            // execute
            rs = this.executeQuery(
                ps,
                statement.toString(),
                statementParameters
            );

            // get selected objects
            List objects = new ArrayList();
            int replySize = (request.size() >= BATCH_MODE_SIZE_MIN) && (request.size() <= BATCH_MODE_SIZE_MAX)
            ? request.size()
                : java.lang.Math.min(request.size(), this.batchSize);
            int replyPosition = request.position();
            if(request.direction() == Directions.DESCENDING) {
                if(replySize > replyPosition) replySize = replyPosition + 1;
                replyPosition = replyPosition + 1 - replySize;
            }
            boolean hasMore = this.getObjects(
                conn,
                dbObject,
                rs,
                objects,
                request.attributeSelector(),
                request.attributeSpecifierAsMap(),
                replyPosition,
                lastPosition,
                lastRowCount,
                replySize,
                null
            );
            // Complete requested attributes
            for(int i = 0; i < objects.size(); i++) {
                DataproviderObject object = (DataproviderObject)objects.get(i);
                boolean fetchAll = false;          
                if(request.attributeSelector() == AttributeSelectors.ALL_ATTRIBUTES) {
                    fetchAll = true;
                }
                else if(request.attributeSelector() == AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES) {
                    for(int j = 0; j < request.attributeSpecifier().length; j++) {
                        if(!object.attributeNames().contains(request.attributeSpecifier()[j].name())) {
                            fetchAll = true;
                            break;
                        }
                    }
                }
                if(fetchAll) {
                    try {
                        objects.set(
                            i,
                            this.get(
                                conn,
                                header,
                                new DataproviderRequest(
                                    object,
                                    DataproviderOperations.OBJECT_RETRIEVAL,
                                    AttributeSelectors.ALL_ATTRIBUTES,
                                    null
                                )
                            ).getObject()
                        );
                    }
                    catch(ServiceException e) {
                        e.log();
                    }
                }              
            }
            lastRowCount = this.resultSetGetRow(rs);
            rs.close(); rs = null;
            ps.close(); ps = null;
            SysLog.detail("*** hasMore", hasMore + "; objects.size()="  + objects.size());

            // reply 
            reply = new DataproviderReply(objects);

            // context.HAS_MORE
            reply.context(
                DataproviderReplyContexts.HAS_MORE
            ).set(
                0, 
                Boolean.valueOf(hasMore)
            );

            // Calculate context.TOTAL only at iteration start
            if(request.operation() == DataproviderOperations.ITERATION_START) {      
                // if !hasMore context.TOTAL = request.position() + objects.size()
                if(!hasMore && ((request.position() == 0) || objects.size() > 0)) {
                    reply.context(
                        DataproviderReplyContexts.TOTAL
                    ).set(
                        0,
                        new Integer(request.position() + objects.size())
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
                        ps = this.prepareStatement(
                            conn,
                            currentStatement = countStatement.toString()
                        );        
                        for(
                                int i = 0, iLimit = statementParameters.size(); 
                                i < iLimit; 
                                i++
                        ) {
                            this.setPreparedStatementValue(
                                conn,
                                ps,
                                i+1, 
                                statementParameters.get(i)
                            );
                        }              
                        rs = this.executeQuery(
                            ps,
                            countStatement.toString(),
                            statementParameters
                        );
                        if(rs.next()) {
                            reply.context(
                                DataproviderReplyContexts.TOTAL
                            ).set(
                                0,
                                new Integer(rs.getInt(1))
                            );
                        }
                        rs.close(); rs = null;
                        ps.close(); ps = null;
                    }
                }
            }

            // context.ITERATOR
            reply.context(DataproviderReplyContexts.ITERATOR).set(
                0,
                AbstractIterator.serialize(
                    new JdbcIterator(
                        referencedType,
                        statement, 
                        statementParameters,
                        request.position() + objects.size(),
                        lastRowCount
                    )
                )
            );

            // context.ATTRIBUTE_SELECTOR
            reply.context(DataproviderReplyContexts.ATTRIBUTE_SELECTOR).set(
                0,
                new Short(request.attributeSelector())
            );
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", request.path()),
                new BasicException.Parameter("statement", currentStatement),
                new BasicException.Parameter("parameters", statementParameters)
            );
        }
        catch(ServiceException e) {
            throw e;
        }
        catch(Exception ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.GENERIC, 
                ex.toString()
            );
        }
        finally {
            this.closeConnection(request, conn);
        }
        SysLog.detail("< find");
        return this.completeReply(
            reply
        );
    }

    //---------------------------------------------------------------------------
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        SysLog.detail("> create", request.object());
        Connection conn = null; 
        DataproviderReply reply = null; 
        try {
            conn = this.getConnection(request);
            reply = this.create(
                conn,
                header,
                request
            );
        }
        catch(ServiceException e) {
            throw e;
        }
        finally {
            try {
                this.closeConnection(request, conn);
            }
            catch(Throwable ex) {
                // ignore
            }
        }
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
                new DataproviderRequest(
                    new DataproviderObject(request.path()),
                    DataproviderOperations.OBJECT_RETRIEVAL,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null
                )
            );
        } catch(ServiceException e) {
            if(e.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                return;
            } else {
                throw e;
            }
        }      
        SysLog.trace("duplicate object");
        throw new ServiceException(
            StackedException.DEFAULT_DOMAIN,
            StackedException.DUPLICATE, 
            "duplicate object",
            new BasicException.Parameter("path", request.path())
        );
    }

    /**
     * Makes a new object persistent. 
     */
    public DataproviderReply create(
        Connection conn,
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        try {
            SparseList instanceOf = request.object().values(SystemAttributes.OBJECT_CLASS);
            String objectClass = (String)instanceOf.get(0);
            if(instanceOf.size() > 1) {
                //
                // Attribute object_class is stored as single-valued attribute, i.e.
                // the super-classes are not stored. They are reconstructed in get() and find()
                // operations.
                //
                instanceOf.clear();
                instanceOf.add(objectClass);
            }        
            if(this.ignoreCheckForDuplicates) {
                //
                // WARNING: the implementation assumes that
                // 'unique constraints' are set in order that INSERTs throw a 'duplicate row'
                // exception which is then mapped to a DUPLICATE ServiceException. 
                //
            } else {
                checkForDuplicates(
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
            DataproviderObject[] partitionedObjects = dbObject.sliceAndNormalizeObject(
                new DataproviderObject(request.object())
            );
            for(
                    int i = 0; 
                    i < partitionedObjects.length;
                    i++
            ) {      
                dbObject = this.createDbObject(
                    conn,
                    partitionedObjects[i].path(),
                    false
                );
                dbObject.createObjectSlice(
                    i,
                    objectClass,
                    partitionedObjects[i]
                );
            }
            return new DataproviderReply(
                request.object()
            );
        } catch(ServiceException e) {
            throw e;
        } catch(Exception ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.GENERIC, 
                ex.toString()
            );
        }
    }

    //---------------------------------------------------------------------------
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {

        SysLog.detail("> remove", request.object());

        // does object exist?
        DataproviderObject replyObj = null;
        replyObj = this.get(
            header,
            request
        ).getObject();      

        Connection conn = null;    
        conn = this.getConnection(request);
        String currentStatement = null;
        try {
            Path accessPath = request.path();

            // Remove object ...
            DbObject dbObject = this.createDbObject(
                conn,
                request.path(),
                true
            );
            dbObject.remove();

            // ... and its composites
            Map processedDbObjectConfigurations = new HashMap();
            for(
                    Iterator i = this.configuration.getDbObjectConfigurations().iterator();
                    i.hasNext();
            ) {
                DbObjectConfiguration dbObjectConfiguration = (DbObjectConfiguration)i.next();
                if(
                        (dbObjectConfiguration.getType().size() > accessPath.size()) &&
                        accessPath.isLike(dbObjectConfiguration.getType().getPrefix(accessPath.size())) &&
                        !processedDbObjectConfigurations.containsKey(dbObjectConfiguration.getType())
                ) {
                    boolean processed = false;
                    // Check whether dbObjectConfiguration is already processed
                    for(
                            Iterator j = processedDbObjectConfigurations.values().iterator();
                            j.hasNext();
                    ) {
                        DbObjectConfiguration processedDbObjectConfiguration = (DbObjectConfiguration)j.next();
                        // dbObject is processed if type if 
                        // <ul>
                        //   <li>db object is composite to processed db object
                        //   <li>dbObjectForUpdate1 are equal
                        // </ul>
                        boolean dbObjectForUpdate1Matches = (dbObjectConfiguration.getDbObjectForUpdate1() == null) || (processedDbObjectConfiguration.getDbObjectForUpdate1() == null) 
                        ? dbObjectConfiguration.getDbObjectForUpdate1() == processedDbObjectConfiguration.getDbObjectForUpdate1() 
                            : dbObjectConfiguration.getDbObjectForUpdate1().equals(processedDbObjectConfiguration.getDbObjectForUpdate1());                      
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
                        dbObject = this.createDbObject(
                            conn,
                            dbObjectConfiguration,
                            request.path(),
                            true
                        );
                        dbObject.remove();
                        processedDbObjectConfigurations.put(
                            dbObjectConfiguration.getType(),
                            dbObjectConfiguration
                        );
                    }
                }
            }

            // Clean up REF table
            if(!this.useNormalizedReferences) {
                List statementParameters = new ArrayList();
                PreparedStatement ps = this.prepareStatement(
                    conn,
                    currentStatement = 
                        "DELETE FROM " + this.namespaceId + "_" + AbstractDatabase_1.T_REF + 
                        " WHERE " + this.getSelectReferenceIdsFromRefTableClause(conn, request.path().getChild("%"), statementParameters)
                );
                for(int i = 0; i < statementParameters.size(); i++) {
                    this.setPreparedStatementValue(
                        conn,
                        ps, 
                        i+1, 
                        statementParameters.get(i)
                    );
                }
                SysLog.detail("statement", currentStatement);
                ps.executeUpdate();
                this.executeBatch(ps);
                ps.close(); ps = null;
            }
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", request.path()),
                new BasicException.Parameter("errorCode", ex.getErrorCode()),
                new BasicException.Parameter("statement", currentStatement)
            );
        }
        finally {
            try {
                this.closeConnection(request, conn);
            }
            catch(Exception ex) {
                // ignore
            }
        }
        // To be safe remove all cached objects
        if(this.useObjectCache) {
            this.objectCache.clear();
        }
        return this.completeReply(
            new DataproviderReply(replyObj)
        );
    }

    //---------------------------------------------------------------------------
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {

        SysLog.detail("> modify", request.object());

        PreparedStatement ps = null;
        String currentStatement = null;
        Connection conn = this.getConnection(request);

        try {
            DataproviderObject object = request.object();
            DbObject dbObject = this.createDbObject(
                conn,
                object.path(),
                true
            );

            RequestCollection requests = new RequestCollection(
                header, 
                this
            );

            // get current object with ALL_ATTRIBUTES
            DataproviderObject toReplace = new DataproviderObject(
                requests.addGetRequest(
                    request.path(),
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null
                )
            );

            // modify attribute values
            for(
                    Iterator i = request.object().attributeNames().iterator();
                    i.hasNext();
            ) {
                String attributeName = (String)i.next();
                List valuesToReplace = toReplace.values(attributeName);
                for(
                        ListIterator j = object.values(attributeName).listIterator();
                        j.hasNext();
                ) {
                    int index = j.nextIndex();
                    Object value = j.next();
                    if(value != null) {
                        valuesToReplace.set(index, value);
                    }
                }
            }

            // remove current object
            ps = this.prepareStatement(
                conn,
                currentStatement = "DELETE FROM " + dbObject.getConfiguration().getDbObjectForUpdate1() + " WHERE " + this.removeViewPrefix(dbObject.getReferenceClause() + " AND " + dbObject.getObjectIdClause())
            );
            int pos = 1;
            List referenceValues = dbObject.getReferenceValues();
            for(int i = 0; i < referenceValues.size(); i++) {
                this.setPreparedStatementValue(
                    conn,
                    ps, 
                    pos++, 
                    referenceValues.get(i)
                );
            }      
            List objectIdValues = dbObject.getObjectIdValues();
            for(int i = 0; i < objectIdValues.size(); i++) {
                this.setPreparedStatementValue(
                    conn,
                    ps, 
                    pos++, 
                    objectIdValues.get(i)
                );
            }
            SysLog.detail("statement", currentStatement);
            ps.executeUpdate();

            // create with replaced attributes
            return new DataproviderReply(
                new DataproviderObject(
                    requests.addCreateRequest(
                        toReplace, 
                        request.attributeSelector(), 
                        null
                    )
                )
            );  
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", request.path()),
                new BasicException.Parameter("statement", currentStatement)
            );
        }
        catch(ServiceException e) {
            throw e;
        }
        catch(Exception ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.GENERIC, 
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
                this.closeConnection(request, conn);
            } catch(Throwable ex) {
                // ignore
            }
        }
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {

        SysLog.detail("> replace", request.object());

        PreparedStatement ps = null;
        String currentStatement = null;
        Connection conn = this.getConnection(request);

        try {
            DataproviderObject object = request.object();            
            DbObject dbObject = this.createDbObject(
                conn,
                object.path(),
                true
            );

            // extent supports object replacement
            if(dbObject.supportsObjectReplacement()) {

                // get current object with ALL_ATTRIBUTES
                DataproviderObject obj = new DataproviderObject(
                    this.get(
                        conn,
                        header,
                        new DataproviderRequest(
                            new DataproviderObject(request.path()),
                            DataproviderOperations.OBJECT_RETRIEVAL,
                            AttributeSelectors.ALL_ATTRIBUTES,
                            null            
                        )
                    ).getObject()
                );
                if(this.useObjectCache) {
                    this.objectCache.remove(obj.path());
                }              
                DataproviderObject beforeImage = new DataproviderObject(obj);
                DataproviderObject[] oldSlices = dbObject.sliceAndNormalizeObject(
                    obj
                );

                // Replace attribute values
                obj.addClones(
                    request.object(),
                    true
                );
                this.removePrivateAttributes(obj);
                DataproviderObject[] newSlices = dbObject.sliceAndNormalizeObject(
                    obj
                );

                // replace existing slices
                for(
                        int i = 0; 
                        i < java.lang.Math.min(oldSlices.length, newSlices.length);
                        i++
                ) {
                    if(!newSlices[i].equals(oldSlices[i])) {
                        dbObject.replaceObjectSlice(
                            i,
                            newSlices[i],
                            oldSlices[i]
                        );
                    }
                }

                // remove extra old slices
                if(oldSlices.length > newSlices.length) {
                    boolean isIndexed;
                    if(dbObject.getConfiguration().getDbObjectForUpdate2() != null) {
                        isIndexed = true;
                        ps = this.prepareStatement(
                            conn,
                            currentStatement = 
                                "DELETE FROM " + dbObject.getConfiguration().getDbObjectForUpdate2() + 
                                " WHERE " + 
                                this.removeViewPrefix(
                                    dbObject.getReferenceClause() + 
                                    " AND " + dbObject.getObjectIdClause() + 
                                    " AND (" + OBJECT_IDX + " >= ?)"
                                )
                        );            
                    }
                    else {
                        isIndexed = dbObject.getIndexColumn() != null;
                        ps = this.prepareStatement(
                            conn,
                            currentStatement = 
                                "DELETE FROM " + dbObject.getConfiguration().getDbObjectForUpdate1() + 
                                " WHERE " + 
                                this.removeViewPrefix(
                                    dbObject.getReferenceClause() + 
                                    " AND " + dbObject.getObjectIdClause() + 
                                    (isIndexed ? " AND (" + dbObject.getIndexColumn() + " >= ?)" : "")
                                )
                        );
                    }
                    int pos = 1;
                    List referenceValues = dbObject.getReferenceValues();
                    for(int i = 0; i < referenceValues.size(); i++) {
                        this.setPreparedStatementValue(
                            conn,
                            ps, 
                            pos++, 
                            referenceValues.get(i)
                        );
                    }
                    List objectIdValues = dbObject.getObjectIdValues();
                    for(int i = 0; i < objectIdValues.size(); i++) {
                        this.setPreparedStatementValue(
                            conn,
                            ps, 
                            pos++, 
                            objectIdValues.get(i)
                        );
                    }
                    if(isIndexed) {
                        ps.setInt(pos++, newSlices.length);
                    }
                    SysLog.detail("statement", currentStatement);
                    ps.executeUpdate();
                }

                // create extra new slices
                if(newSlices.length > oldSlices.length) {
                    String objectClass = 
                        (String)object.values(SystemAttributes.OBJECT_CLASS).get(0);
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
                return this.completeReply(
                    new DataproviderReply(
                        this.addBeforeImage(
                            obj,
                            beforeImage
                        )
                    )
                );
            }

            // implement replacement by remove/create
            else { 
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED, 
                    "DbObject must support object replacement",
                    new BasicException.Parameter("dbObject.type", dbObject.getConfiguration().getType())
                );
            }
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", request.path()),
                new BasicException.Parameter("statement", currentStatement)
            );
        }
        catch(ServiceException e) {
            throw e;
        }
        catch(Exception ex) {
            throw new ServiceException(
                ex, 
                StackedException.DEFAULT_DOMAIN,
                StackedException.GENERIC, 
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
                this.closeConnection(request, conn);
            } catch(Throwable ex) {
                // ignore
            }
        }
    }

    //---------------------------------------------------------------------------
    protected DataproviderObject otherOperation(
        ServiceHeader header,
        DataproviderRequest request,
        String operation, Path replyPath
    ) throws ServiceException {
        return super.otherOperation(
            header, 
            request, 
            operation, 
            replyPath
        );
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
            this.unitOfWorkId, 
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
            this.unitOfWorkId, 
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
     * Parses the string argument as a boolean. The boolean returned represents 
     * the value true if the string argument is not <code>null</code> and is 
     * equal, ignoring case, to the string <code>"true"</code>.
     * <p>
     * This method may be replaced by <code>Boolean.parseBoolean(String)</code> 
     * from JRE 5.0 on.
     * 
     * @param s the String containing the boolean representation to be parsed 
     * 
     * @return the boolean represented by the string argument
     */
    static boolean parseBoolean(
        String s
    ) {
        return "true".equalsIgnoreCase(s);
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------

    protected static final String T_REF = "REF";
    protected static final String UNDEF_OBJECT_CLASS = "#undef";

    protected BooleanMarshaller booleanMarshaller;
    protected Marshaller durationMarshaller;
    protected XMLGregorianCalendarMarshaller calendarMarshaller;
    protected String dateTimeType;
    protected String timeType;
    protected String dateType;
    protected String booleanType;

    protected static final int ROUND_UP_TO_MAX_SCALE = 15;

    // AUTOINC_FORMAT
    protected static final String AUTOINC_FORMAT_NEXTVAL = "NEXTVAL";
    protected static final String AUTOINC_FORMAT_NEXTVAL_FOR = "NEXTVALFOR";
    protected static final String AUTOINC_FORMAT_NEXT_VALUE_FOR = "NEXTVALUEFOR";
    protected static final String AUTOINC_FORMAT_AUTO = "AUTO";
    protected static final String AUTOINC_FORMAT_SEQUENCE = "SEQUENCE";

    // VIEW_MODE
    protected static final short VIEW_MODE_ADD_MIXIN_COLUMNS_TO_PRIMARY = 0;
    protected static final short VIEW_MODE_SECONDARY_COLUMNS = 1;

    /**
     * Request size range triggering batch mode (overrides configured batchSize)
     */
    protected static final int BATCH_MODE_SIZE_MIN = 500;
    protected static final int BATCH_MODE_SIZE_MAX = 2000;

    // sort for binary search later 
    protected static final String[] SYSTEM_ATTRIBUTES = new String[] {
        SystemAttributes.OBJECT_CLASS, 
        SystemAttributes.CREATED_AT,
        SystemAttributes.CREATED_BY, 
        SystemAttributes.MODIFIED_BY, 
        SystemAttributes.MODIFIED_AT
    };

    protected static final int DEFAULT_BATCH_SIZE = 50;
    protected static final String DEFAULT_OID_SUFFIX = "_objectid";
    protected static final String DEFAULT_RID_SUFFIX = "_referenceid";
    protected static final String DEFAULT_COLUMN_SELECTOR = "v.*";
    protected String privateAttributesPrefix;
    protected String objectIdAttributesSuffix;
    protected String referenceIdAttributesSuffix;

    // Reserved words
    protected static final Set<String> RESERVED_WORDS_HSQLDB = new HashSet<String>(
            Arrays.asList(   
                "position", 
                "POSITION"
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
    protected int batchSize;

    /**
     * List of DbConnectionManagers. getConnection() returns as default
     * connectionManagers at position 0. A user-defined implementation of
     * getConnection() may return any of the configured connection managers.
     */
    protected List connectionManagers;

    /**
     * Configuration of plugin.
     */
    protected DatabaseConfiguration configuration;

    /**
     * Result set type used for prepareStatement. It must be a valid value
     * defined by ResultSet.TYPE_... Default is TYPE_FORWARD_ONLY.
     */
    protected int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

    /**
     * Model is set when option modelDriven is set to true
     */  
    protected Model_1_0 model;

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
     * Set of fully qualified model names of non-persistent features.
     */
    protected Set nonPersistentFeatures;

    /**
     * macros
     */
    // key=column name, value=List of [macro name, macro value]
    protected Map stringMacros;

    // key=macro name, value=macro value
    protected Map pathMacros;

    // referenceId format
    protected String referenceIdFormat;

    // useNormalizedReferences. If true, all DB operations are performed on the 
    // normalized values of object references, i.e. rid, oid.
    protected boolean useNormalizedReferences;

    // maximum reference components in REF table
    protected int maxReferenceComponents;

    /**
     * If set to true the size of multi-valued attributes stored in column C
     * is stored in column C_.
     */
    protected boolean setSizeColumns;

    /**
     * <columnName,attributeName> mapping. Calculating attribute names
     * from column names and vice versa is expensive. Therefore it
     * is cached.
     */
    protected Map attributeNames = new HashMap();
    protected Map columnNames = new HashMap();
    /**
     * 2-dim map with entries <view, <feature name, scoped feature name>> 
     * In order to reduce String operations in touchNonDerivedFeatures()
     * the map contains scoped feature names. 
     */
    protected final Map scopedFeatures = new HashMap();

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
     * Enables object caching at unit of work level.
     */
    protected boolean useObjectCache = true;
    // object cache at unit of work level
    protected final Map objectCache = new HashMap();

    /**
     * technical column names: object_oid, object_rid, object_idx
     * (may be used by DbObject implementations if required)
     */
    public String OBJECT_OID;
    public String OBJECT_IDX;
    public String OBJECT_RID;
    public String OBJECT_ID;

    protected final static String DATASTORE_PREFIX = 
        SystemAttributes.CONTEXT_PREFIX + "Datastore:";

    final static protected Path EXTENT_PATTERN = new Path("xri:@openmdx:**/provider/**/segment/**/extent");

    protected Properties jdbcDriverSqlProperties = null;
    protected static final String JDBC_DRIVER_SQL_PROPERTIES = "org/openmdx/kernel/application/deploy/jdbc-driver-sql.properties";  

    protected final Collection temporaryFiles = new ArrayList();
    protected String unitOfWorkId;

    static final Integer INTEGER_0 = new Integer(0);

    static {
        // must guarantee sort order for binary search access
        Arrays.sort(SYSTEM_ATTRIBUTES);
    }

}

//---End of File -------------------------------------------------------------
