/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JDBC 2 Database Plug-In
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Quantifier;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;

/**
 * Concrete implementation of the AbstractDatabase_1 plugin using Jdbc2 driver
 * features. 
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class Database_1 extends AbstractDatabase_1 {

    /**
     * The large object marshaller instance
     */
    private LargeObjectMarshaller largeObjectMarshaller;

    /**
     * The list of aspect base classes may be extended by overriding isAspectBaseClass()
     */
    private static final Collection<String> ASPECT_BASE_CLASSES = Arrays.asList(
        "org:openmdx:state2:DateState",
        "org:openmdx:state2:DateTimeState",
        "org:openmdx:role2:Role"
    );
    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#activate(short, org.openmdx.application.configuration.Configuration, org.openmdx.application.dataprovider.spi.Layer_1)
     */
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1 delegation
    ) throws ServiceException {
        super.activate(id, configuration, delegation);
        this.largeObjectMarshaller = new LargeObjectMarshaller(
            configuration.isNotDisabled(LayerConfigurationEntries.GET_LARGE_OBJECT_BY_VALUE)
        );
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
        return ASPECT_BASE_CLASSES.contains(qualifiedClassName);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#mapInstanceOfFilterProperty(org.openmdx.application.dataprovider.cci.DataproviderRequest, java.util.Collection)
     */
    @Override
    protected FilterProperty mapInstanceOfFilterProperty(
        DataproviderRequest request,
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
            for(FilterProperty filterProperty : request.attributeFilter()) {
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
        return super.mapInstanceOfFilterProperty(request, qualifiedClassNames);
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
    ){
        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            return Boolean.parseBoolean(this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".ALLOW.SCROLLSENSITIVE.RESULTSET"));            
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
    ){
        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            return Boolean.parseBoolean(this.jdbcDriverSqlProperties.getProperty(databaseProductName + ".ALLOW.SCROLLINSENSITIVE.RESULTSET"));            
        } catch(Exception e) {
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#prepareStatement(java.sql.Connection, java.lang.String, boolean)
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    Object getBlobColumnValue(
        Object val,
        String attributeName,
        ModelElement_1_0 attributeDef
    ) throws ServiceException, SQLException {
        return this.largeObjectMarshaller.getBinaryColumnValue(val, attributeName, attributeDef);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#getClobColumnValue(java.lang.Object, java.lang.String, org.openmdx.base.mof.cci.ModelElement_1_0)
     */
    @Override
    Object getClobColumnValue(
        Object val,
        String attributeName,
        ModelElement_1_0 attributeDef
    ) throws ServiceException, SQLException {
        return this.largeObjectMarshaller.getCharacterColumnValue(val, attributeName, attributeDef);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#setClobColumnValue(java.sql.PreparedStatement, int, java.lang.Object)
     */
    @Override
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
                if(clob.getLength() == null && this.largeObjectMarshaller.isTallyingRequired(setLargeObjectMethod)) {
                    clob = this.tallyLargeObject(clob.getContent());
                }
                this.largeObjectMarshaller.setCharacterColumnValue(ps, column, clob, setLargeObjectMethod);
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
    @Override
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
                if(blob.getLength() == null && this.largeObjectMarshaller.isTallyingRequired(setLargeObjectMethod)) {
                    blob = this.tallyLargeObject(blob.getContent());
                }
                this.largeObjectMarshaller.setBinaryColumnValue(ps, column, blob, setLargeObjectMethod);
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

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#setPosition(java.sql.ResultSet, int, int, int, boolean, org.openmdx.application.dataprovider.layer.persistence.jdbc.DbObjectConfiguration)
     */
    @Override
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
                	SysLog.log(Level.INFO, "Set absolute position to {0} for type {1}", position, dbObjectConfiguration.getTypeName());
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
                        	frs.getObject(OBJECT_ID);
                        }
                        if(count % 1000 == 0) {
                        	SysLog.log(Level.FINE, "Current position for type {0} is {1}", dbObjectConfiguration.getTypeName(), count);
                        }
                    }
                } else if(frs.getColumnNames().contains(OBJECT_ID)) {
                    // PK format: id
                    int count = 0;  
                    String previousId = (String)frs.getObject(OBJECT_ID);
                    while(hasMore) {
                        String id = (String)frs.getObject(OBJECT_ID);
                        if(!id.equals(previousId)) {
                            count++;
                            previousId = id;
                        }
                        if(count >= position) break;
                        hasMore = frs.next();
                        if(count % 1000 == 0) {
                        	SysLog.log(Level.FINE, "Current position for type {0} is {1}", dbObjectConfiguration.getTypeName(), count);
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
        	SysLog.log(Level.FINE, "Position duration for type {0} is {1} ms", dbObjectConfiguration.getTypeName(), duration);
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    void resultSetUpdateRow(
        ResultSet rs
    ) throws SQLException {
        rs.updateRow();
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#resultSetGetRow(java.sql.ResultSet)
     */
    @Override
    int resultSetGetRow(
        ResultSet rs
    ) throws SQLException {
        // not implemented yet
        return -1;
    }

}

//---End of File -------------------------------------------------------------
