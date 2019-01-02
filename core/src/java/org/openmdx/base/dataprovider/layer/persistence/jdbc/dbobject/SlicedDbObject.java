/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Sliced DB Object
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.FastResultSet;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.SparseArray;

@SuppressWarnings({"rawtypes","unchecked"})
public class SlicedDbObject extends StandardDbObject {

    //-------------------------------------------------------------------------
    public SlicedDbObject(
        Database_1_0 database, 
        Connection conn,
        DbObjectConfiguration dbObjectConfiguration,
        Path accessPath, 
        boolean isExtent,
        boolean isQuery
    ) throws ServiceException {
        super(
            database, 
            conn, 
            dbObjectConfiguration, 
            accessPath, 
            isExtent, 
            isQuery
        );
        // Automatically switch to non-indexed in case a secondary
        // db object is specified. 
        if(
            (dbObjectConfiguration.getDbObjectForQuery2() != null) ||
            (dbObjectConfiguration.getDbObjectForUpdate2() != null)
        ) {
            this.indexColumn = null;
            this.excludeAttributes.add("objectIdx");                
        }
    }

    //-------------------------------------------------------------------------
    public SlicedDbObject(
    	Database_1_0 database, 
        Connection conn,
        DbObjectConfiguration dbObjectConfiguration
    ) {
        super(
            database, 
            conn, 
            dbObjectConfiguration
        );
        // Automatically switch to non-indexed in case a secondary
        // db object is specified. 
        if(
                (dbObjectConfiguration.getDbObjectForQuery2() != null) ||
                (dbObjectConfiguration.getDbObjectForUpdate2() != null)
        ) {
            this.indexColumn = null;
            this.excludeAttributes.add("objectIdx");                
        }
    }

    //---------------------------------------------------------------------------  
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.DbObject#getResourceIdentifier(org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.FastResultSet)
     */
    @Override
    public Path getResourceIdentifier(
        FastResultSet frs
    ) throws SQLException, ServiceException {
        if(this.database.getDatabaseConfiguration().normalizeObjectIds()) {
            return getObjectReference(frs).getChild(getObjectId(frs));
        } else {
            return super.getResourceIdentifier(frs);
        }        
    }

    
    //---------------------------------------------------------------------------  
    @Override
    public Path getObjectReference(
        FastResultSet frs
    ) throws SQLException, ServiceException {      
        if(this.database.getDatabaseConfiguration().normalizeObjectIds()) {
            return this.database.getDatabaseConfiguration().buildResourceIdentifier(
                frs.getObject(this.database.getObjectRidColumnName()).toString(), 
                true // reference
            );
        } else {
            return super.getObjectReference(frs);
        }
    }

    //---------------------------------------------------------------------------  
    
    private boolean isConvertible(
        String segment
    ){
        return 
            segment != null &&
            segment.indexOf('/') > 0 &&
            !segment.startsWith("("); 
    }
    
    @Override
    public String getObjectId(
        FastResultSet frs
    ) throws SQLException, ServiceException {
        if(this.database.getDatabaseConfiguration().normalizeObjectIds()) {
            String component = frs.getObject(this.database.getObjectOidColumnName()).toString();
            return isConvertible(component) ? this.database.getDatabaseConfiguration().buildResourceIdentifier(component, false).toClassicRepresentation() : component;
        } else {
            return super.getObjectId(frs);
        }
    }

    
    //---------------------------------------------------------------------------  
    @Override
    public int getIndex(
        FastResultSet frs
    ) throws SQLException {
        final String objectIdxColumnName = this.database.getObjectIdxColumnName();
        if(frs.getColumnNames().contains(objectIdxColumnName)) {
            return ((Number)frs.getObject(objectIdxColumnName)).intValue();
        }
        else {
            return 0;
        }
    }

    //-------------------------------------------------------------------------
    protected Set getDbObjectColumns(
        String dbObject
    ) throws ServiceException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String currentStatement = null;
        Set columns = null;
        if((columns = (Set)dbObjectColumns.get(dbObject)) == null) {      
            try {
                ps = this.database.prepareStatement(
                    conn,
                    currentStatement = "SELECT * FROM " + dbObject + " WHERE 1=0"
                );
                rs = ps.executeQuery();
                FastResultSet frs = new FastResultSet(this.database, rs);
                columns = new HashSet();
                columns.addAll(
                    frs.getColumnNames()
                );
                dbObjectColumns.put(
                    dbObject,
                    columns
                );      
            }
            catch(SQLException ex) {
                throw new ServiceException(
                    ex, 
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE, 
                    null,
                    new BasicException.Parameter("statement", currentStatement)
                );
            }
            finally {
                try {              
                    if(ps != null) ps.close();
                    if(rs != null) rs.close();
                } catch(Throwable ex) {
                    // ignore
                }
            }
        }
        return columns;
    }

    //-------------------------------------------------------------------------
    protected void createObject(
        int index,
        ObjectRecord object,
        String objectClass,
        List referenceIdColumns,
        List referenceIdValues,
        List objectIdColumns,
        List objectIdValues,
        Set excludeAttributes
    ) throws ServiceException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String currentStatement = null;
        List statementParameters = new ArrayList();
        List statementParameterTypes = new ArrayList();
        List processedColumns = new ArrayList();
        Set processedAttributes = new HashSet();
        if(excludeAttributes != null) {
            processedAttributes.addAll(excludeAttributes);
        }
        Object_2Facade facade= Facades.asObject(object);
        try {        
            // Single-valued attributes at index 0 are stored in primary dbObject, multi-valued
            // attributes in secondary dbObjects. Multi-valued attributes (index > 0) are always
            // stored in secondary dbObject.
            List dbObjects = new ArrayList();
            boolean processAsSecondary;
            if(index > 0) {
                dbObjects.add(
                    this.getConfiguration().getDbObjectForUpdate2() == null ? 
                        this.getConfiguration().getDbObjectForUpdate1() : 
                        this.getConfiguration().getDbObjectForUpdate2()
                );
                processAsSecondary = true;
            }
            else {
                dbObjects.add(
                    this.getConfiguration().getDbObjectForUpdate1()
                );
                if(this.getConfiguration().getDbObjectForUpdate2() != null) {
                    dbObjects.add(
                        this.getConfiguration().getDbObjectForUpdate2()
                    );
                }
                processAsSecondary = false;
            }
            // Process all db objects
            for(
                Iterator d = dbObjects.iterator();
                d.hasNext();
                processAsSecondary = true
            ) {
                String dbObject = (String)d.next();            
                Set dbObjectColumns = this.getDbObjectColumns(dbObject);
                String statement = "INSERT INTO " + dbObject + " (";
                statementParameters.clear();
                int k = 0;      
                // All attributes for dbObject
                for(
                    Iterator i = facade.getValue().keySet().iterator(); 
                    i.hasNext();
                ) {
                    String attributeName = (String)i.next();
                    if((excludeAttributes == null || !excludeAttributes.contains(attributeName)) && (attributeName.indexOf(':') < 0)) {
                        Object attributeValues = facade.attributeValues(attributeName);
                        if(
                            (attributeValues instanceof Collection && ((Collection)attributeValues).isEmpty()) ||
                            (attributeValues instanceof SparseArray && ((SparseArray)attributeValues).isEmpty())
                        ) {
                            String columnName = this.database.getColumnName(
                                this.conn, 
                                attributeName, 
                                0, 
                                false, 
                                true, 
                                false // markAsPrivate
                            );
                            if(dbObjectColumns.contains(columnName)) {
                                processedAttributes.add(
                                    attributeName
                                );
                            }                      
                        }
                        else {
                            ListIterator j = attributeValues instanceof SparseArray ? ((SparseArray)attributeValues).populationIterator() : ((List)attributeValues).listIterator();
                            while(j.hasNext()) {
                                int valIndex = j.nextIndex();
                                Object value = j.next();
                                String columnName = this.database.getColumnName(
                                    this.conn, 
                                    attributeName, 
                                    valIndex, 
                                    false, 
                                    true, 
                                    false // markAsPrivate
                                );
                                if(dbObjectColumns.contains(columnName)) {
                                    if(!columnName.equals(this.database.getObjectIdxColumnName())) {
                                        if(k > 0) statement += ", ";
                                        statement += this.database.getColumnName(
                                            this.conn, 
                                            attributeName, 
                                            valIndex, 
                                            false, 
                                            false, // escape reserved words
                                            false // markAsPrivate
                                        );
                                        statementParameters.add(
                                            this.database.externalizeStringValue(columnName, value)
                                        );
                                        statementParameterTypes.add(
                                            value.getClass().getName()
                                        );
                                        processedColumns.add(
                                            columnName
                                        );
                                        k++;
                                    }
                                    processedAttributes.add(
                                        attributeName
                                    );
                                }
                            }
                        }
                    }
                    else {
                        processedAttributes.add(
                            attributeName
                        );                    
                    }
                }
                // Add autonum columns for slice 0
                List autonumColumns = null;
                List autonumValues = null;
                if(               
                    (index == 0) &&
                    !processAsSecondary
                ) {
                    for(
                        Iterator i = this.getConfiguration().getAutonumColumns().iterator();
                        i.hasNext();
                    ) {
                        String autonumColumn = (String)i.next();  
                        String autonumColumnName = autonumColumn;
                        // USETYPE allows to include type in sequence name
                        int posTyped = autonumColumn.indexOf(" TYPED ");
                        // AS <format> allows to cast/format the sequence number
                        int posAs = autonumColumn.indexOf(" AS ");
                        autonumColumnName = autonumColumnName.indexOf(" ") > 0 ?
                            autonumColumnName.substring(0, autonumColumnName.indexOf(" ")).trim() :
                            autonumColumnName.trim();
                        // Only add if not supplied explicitly as attribute
                        if(!processedColumns.contains(autonumColumnName)) {
                            if(autonumColumns == null) {
                                autonumColumns = new ArrayList();
                            }
                            autonumColumns.add(
                                autonumColumnName
                            );
                            String sequenceName = this.database.getNamespaceId() + "_" + autonumColumnName;
                            if(posTyped > 0) {
                                sequenceName += "_" + this.getConfiguration().getTypeName();
                            }
                            String autonumValue = this.database.getAutonumValue(
                                this.conn,
                                sequenceName,
                                posAs > 0 ? 
                                    autonumColumn.substring(posAs) : 
                                    null
                            );
                            // Emulate SQL sequence
                            if(autonumValue == null) {
                                String normalizedSequenceName = sequenceName.toUpperCase() + "_SEQ";
                                ps = this.database.prepareStatement(
                                    this.conn,
                                    currentStatement = "SELECT nextval FROM " + normalizedSequenceName
                                );
                                rs = ps.executeQuery();
                                if(rs.next()) {
                                    autonumValue = rs.getString("nextval");
                                    rs.close();
                                    ps.close();
                                    ps = this.database.prepareStatement(
                                        this.conn,
                                        currentStatement = "UPDATE " + normalizedSequenceName + " SET nextval = nextval + 1"
                                    );
                                    this.database.executeUpdate(ps, currentStatement, Collections.EMPTY_LIST);
                                    ps.close();
                                }
                                else {
                                    autonumValue = "0";
                                    ps = this.database.prepareStatement(
                                        this.conn,
                                        currentStatement = "INSERT INTO " + normalizedSequenceName + " (nextval) VALUES (0)"
                                    );                          
                                    this.database.executeUpdate(ps, currentStatement, Collections.EMPTY_LIST);
                                    ps.close();
                                }
                            }
                            if(autonumValues == null) {
                                autonumValues = new ArrayList();                   
                            }
                            autonumValues.add(autonumValue);
                        }
                    }
                }  
                // typeName
                String columnNameTypeName = this.database.getPrivateAttributesPrefix() + COLUMN_TYPE_NAME;
                if(dbObjectColumns.contains(columnNameTypeName)) {
                    if(k > 0) statement += ", ";
                    statement += columnNameTypeName;
                    statementParameters.add(
                        this.database.getObjectId(
                            this.getReference().getLastSegment().toClassicRepresentation()
                        )
                    );
                    k++;
                }
                // objectClass
                if(
                    !processedAttributes.contains(SystemAttributes.OBJECT_CLASS) && (
                            index == 0 || this.getConfiguration().getDbObjectForUpdate2() != null
                    )
                ){
                    String columnNameObjectClass = this.database.getColumnName(
                        this.conn, 
                        SystemAttributes.OBJECT_CLASS, 
                        0, 
                        false, 
                        true, 
                        false // markAsPrivate
                    );
                    if(dbObjectColumns.contains(columnNameObjectClass)) {
                        if(k > 0) statement += ", ";
                        statement += columnNameObjectClass;
                        statementParameters.add(objectClass);
                        k++;
                        processedAttributes.add(SystemAttributes.OBJECT_CLASS);                    
                    }                
                }
                // rid
                if(referenceIdColumns != null) {
                    for(Iterator i = referenceIdColumns.iterator(); i.hasNext(); ) {
                        if(k > 0) statement += ", ";
                        statement += i.next();
                        k++;
                    }
                    if(!referenceIdColumns.isEmpty()) {
                        statementParameters.addAll(referenceIdValues);
                    }
                }      
                // oid
                if(objectIdColumns != null) {
                    for(Iterator i = objectIdColumns.iterator(); i.hasNext(); ) {
                        if(k > 0) statement += ", ";
                        statement += i.next();
                        k++;
                    }
                    if(!objectIdColumns.isEmpty()) {
                        statementParameters.addAll(objectIdValues);
                    }
                }
                // idx
                if(this.getIndexColumn() != null) {
                    if(k > 0) statement += ", ";
                    statement += this.getIndexColumn();
                    statementParameters.add(Integer.valueOf(index));
                    k++;
                }
                // secondary tables require index
                else if(processAsSecondary) {
                    if(k > 0) statement += ", ";
                    statement += this.database.getObjectIdxColumnName();
                    statementParameters.add(Integer.valueOf(index));
                    k++;
                }
                // autonum columns
                if(autonumColumns != null) {
                    for(Iterator i = autonumColumns.iterator(); i.hasNext(); ) {
                        statement += ", ";
                        statement += i.next();
                    }
                }          
                statement += ")";

                // VALUE placeholders
                statement += " VALUES (";
                for(int i = 0; i < k; i++) {
                    if(i > 0) {
                        statement += ", ";
                    }
                    statement += this.database.getPlaceHolder(conn, statementParameters.get(i));
                }
                // autonum values
                if(autonumValues != null) {
                    for(Iterator i = autonumValues.iterator(); i.hasNext(); ) {
                        statement += ", ";
                        statement += i.next();
                    }          
                }
                statement += ")";          
                // prepare
                ps = this.database.prepareStatement(
                    this.conn, 
                    currentStatement = statement.toString(),
                    true // updatable
                );

                // fill in values
                for(int i = 0; i < statementParameters.size(); i++) {
                    Object value = statementParameters.get(i);
                    this.database.setPreparedStatementValue(
                        this.conn,
                        ps,
                        i+1,
                        value
                    );    
                }    
                this.database.executeUpdate(ps, currentStatement, statementParameters);
                ps.close(); ps = null;
            }
            if(!processedAttributes.containsAll(facade.getValue().keySet())) {
                Set nonProcessedAttributes = new HashSet(facade.getValue().keySet());
                nonProcessedAttributes.removeAll(processedAttributes);
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE, 
                    "Attributes can not be stored. Missing columns in db objects",
                    new BasicException.Parameter("object", object),
                    new BasicException.Parameter("processed attributes", processedAttributes),
                    new BasicException.Parameter("non-processed attributes", nonProcessedAttributes),
                    new BasicException.Parameter("db objects", dbObjects)
                );                
            }                              
        }
        catch(SQLException ex) {
            String sqlState = ex.getSQLState();
            throw new ServiceException(
                ex, 
                BasicException.Code.DEFAULT_DOMAIN,
                "23000".equals(sqlState) || "23505".equals(sqlState) ? BasicException.Code.DUPLICATE : BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", facade.getPath()),
                new BasicException.Parameter("statement", currentStatement),
                new BasicException.Parameter("values", statementParameters),
                new BasicException.Parameter("types", statementParameterTypes),
                new BasicException.Parameter("sqlErrorCode", ex.getErrorCode()), 
                new BasicException.Parameter("sqlState", sqlState)
            );
        }
        finally {
            try {
                if(rs != null) rs.close();
                if(ps != null) ps.close();
            } catch(Throwable ex) {
                // ignore
            }
        }
    }

    //-------------------------------------------------------------------------
    @Override
    public void createObjectSlice(
        int index,
        String objectClass,
        ObjectRecord object
    ) throws ServiceException {
        this.createObject(
            index,
            object,
            objectClass,
            this.getReferenceColumn(),
            this.getReferenceValues(),
            this.objectIdColumn,
            this.getObjectIdValues(),
            this.excludeAttributes
        );
    }

    //---------------------------------------------------------------------------  
    protected void replaceObjectSlice(
        int index,
        ObjectRecord newObject,
        ObjectRecord oldObject,
        List referenceIdColumns,
        List referenceIdValues,
        List objectIdColumns,
        List objectIdValues,
        Set excludeAttributes, 
        String writeLock, 
        String readLock
    ) throws ServiceException {
        PreparedStatement ps = null;
        String currentStatement = null;
        List<Object> statementParameters = new ArrayList<Object>();
        Set statementColumns = new HashSet();
        Set processedAttributes = new HashSet();
        if(excludeAttributes != null) {
            processedAttributes.addAll(excludeAttributes);
        }
        if(this.excludeAttributes != null) {
            processedAttributes.addAll(this.excludeAttributes);
        }            
        SysLog.detail("Processed attributes", processedAttributes);
        Object_2Facade newObjectFacade = Facades.asObject(newObject);
        Object_2Facade oldObjectFacade = Facades.asObject(oldObject);
        try {
            // Single-valued attributes at index 0 are stored in primary dbObject, multi-valued
            // attributes in secondary dbObjects. Multi-valued attributes (index > 0) are always
            // stored in secondary dbObject.
            List dbObjects = new ArrayList();
            boolean processAsSecondary;
            if(index > 0) {
                dbObjects.add(
                    this.getConfiguration().getDbObjectForUpdate2() == null
                    ? this.getConfiguration().getDbObjectForUpdate1()
                        : this.getConfiguration().getDbObjectForUpdate2()
                );
                processAsSecondary = true;
            }
            else {
                dbObjects.add(
                    this.getConfiguration().getDbObjectForUpdate1()
                );
                if(this.getConfiguration().getDbObjectForUpdate2() != null) {
                    dbObjects.add(
                        this.getConfiguration().getDbObjectForUpdate2()
                    );
                }
                processAsSecondary = false;
            }
            // Process all db objects
            for(
                Iterator d = dbObjects.iterator();
                d.hasNext();
                processAsSecondary = true
            ) {
                String dbObject = (String)d.next();
                Set dbObjectColumns = this.getDbObjectColumns(dbObject);
                StringBuilder statement = new StringBuilder("UPDATE ").append(dbObject).append(" SET ");
                statementParameters.clear();
                boolean hasParameters = false;
                // newValues
                for(
                    Iterator i = newObjectFacade.getValue().keySet().iterator(); 
                    i.hasNext();
                ) {
                    String attributeName = (String)i.next();
                    if(((excludeAttributes == null) || !excludeAttributes.contains(attributeName)) && attributeName.indexOf(':') < 0) {
                        List attributeValues = newObjectFacade.getAttributeValuesAsReadOnlyList(attributeName);   
                        if(attributeValues.isEmpty()) {
                            String columnName = this.database.getColumnName(this.conn, attributeName, 0, false, true, false);
                            if(dbObjectColumns.contains(columnName)) {
                                processedAttributes.add(attributeName);
                            }                      
                        }
                        else {
                            for(
                                ListIterator j = attributeValues.listIterator(); 
                                j.hasNext(); 
                            ) {
                                int valIndex = j.nextIndex();
                                Object value = j.next();
                                String columnName = this.database.getColumnName(this.conn, attributeName, valIndex, false, true, false);
                                if(dbObjectColumns.contains(columnName)) {
                                    if(!columnName.equals(this.database.getObjectIdxColumnName())) {
                                    	if(hasParameters) statement.append(", ");
                                        statement.append(
                                        	this.database.getColumnName(this.conn, attributeName, valIndex, false, false, false)
                                        ).append(
                                        	" = "
                                        ).append(
                                        	this.database.getPlaceHolder(this.conn, value)
                                        ); 
                                        statementColumns.add(columnName);
                                        statementParameters.add(
                                            this.database.externalizeStringValue(columnName, value)
                                        );
                                        hasParameters = true;
                                    }
                                    processedAttributes.add(
                                        attributeName
                                    );                            
                                }
                            }
                        }
                    }
                    else {
                        processedAttributes.add(
                            attributeName
                        );                    
                    }
                }
                // NULL oldValues
                for(
                    Iterator i = oldObjectFacade.getValue().keySet().iterator(); 
                    i.hasNext();
                ) {
                    String attributeName = (String)i.next();
                    if(((excludeAttributes == null) || !excludeAttributes.contains(attributeName)) && attributeName.indexOf(':') < 0) {
                        List attributeValues = oldObjectFacade.getAttributeValuesAsReadOnlyList(attributeName);
                        if(attributeValues.isEmpty()) {
                            String columnName = this.database.getColumnName(conn, attributeName, 0, false, true, false);
                            if(dbObjectColumns.contains(columnName)) {
                                processedAttributes.add(attributeName);
                            }                      
                        }
                        else {
                            for(
                                ListIterator j = attributeValues.listIterator(); 
                                j.hasNext(); 
                            ) {
                                int valIndex = j.nextIndex();
                                String columnName = this.database.getColumnName(conn, attributeName, valIndex, false, true, false);
                                if(dbObjectColumns.contains(columnName)) {
                                    if(
                                        !columnName.equals(this.database.getObjectIdxColumnName()) && 
                                        !statementColumns.contains(columnName)
                                    ) {
                                    	if(hasParameters) statement.append(", ");
                                        statement.append(columnName).append(" = NULL");
                                        statementColumns.add(columnName);
                                        hasParameters = true;
                                    }
                                    processedAttributes.add(attributeName);                            
                                }
                                j.next();
                            }
                        }
                    }
                    else {
                        processedAttributes.add(
                            attributeName
                        );                    
                    }
                }
                if(hasParameters) {

                    // WHERE
                    statement.append(" WHERE ");
                    boolean hasClause = false;

                    // rid
                    if(referenceIdColumns != null) {
                        for(Iterator i = referenceIdColumns.iterator(); i.hasNext(); ) {
                            if(hasClause) statement.append(" AND ");
                            statement.append("(").append(i.next()).append(" = ?)");
                            hasClause = true;
                        }
                        if(!referenceIdColumns.isEmpty()) {
                            statementParameters.addAll(referenceIdValues);
                        }
                    }

                    // oid
                    if(objectIdColumns != null) {
                        for(Iterator i = objectIdColumns.iterator(); i.hasNext(); ) {
                            if(hasClause) statement.append(" AND ");
                            statement.append("(").append(i.next()).append(" = ?)");
                            hasClause = true;
                        }
                        if(!objectIdColumns.isEmpty()) {
                            statementParameters.addAll(objectIdValues);
                        }
                    }                
                    // idx
                    if(this.getIndexColumn() != null) {
                        if(hasClause) statement.append(" AND ");
                        statement.append("(").append(this.getIndexColumn()).append(" = ?)");
                        statementParameters.add(Integer.valueOf(index));
                        hasClause = true;
                    }
                    // secondary tables require index
                    else if(processAsSecondary) {
                        if(hasClause) statement.append(" AND ");
                        statement.append("(").append(this.database.getObjectIdxColumnName()).append(" = ?)");
                        statementParameters.add(Integer.valueOf(index));
                        hasClause = true;                    
                    }
                    if(!processAsSecondary) {
                    	appendLockAssertion(statement, statementParameters, writeLock);
                    	appendLockAssertion(statement, statementParameters, readLock);
                    }
                    ps = this.database.prepareStatement(
                        this.conn, 
                        currentStatement = statement.toString()
                    );            
                    // fill in values
                    int ii = 1;
                    for(
                        Iterator i = statementParameters.iterator();
                        i.hasNext();
                        ii++
                    ) {
                        this.database.setPreparedStatementValue(
                            this.conn,
                            ps,
                            ii,
                            i.next()
                        );               
                    }
                    int rowCount = this.database.executeUpdate(ps, currentStatement, statementParameters);
                    if(rowCount != 1){
                        if(writeLock == null) {
                            SysLog.warning("Update failed", newObjectFacade.getPath());
                        } else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                                "The object has been modified since it was read",
                                new BasicException.Parameter("path", newObjectFacade.getPath()),
                                new BasicException.Parameter("assertion", writeLock), 
                                new BasicException.Parameter("sqlStatement", currentStatement),
                                new BasicException.Parameter("parameters", statementParameters),
                                new BasicException.Parameter("sqlRowCount", rowCount)
                            );
                        }
                    }

                    ps.close(); ps = null;
                }
            }
            Set nonProcessedAttributes = new HashSet(newObjectFacade.getValue().keySet());
            nonProcessedAttributes.removeAll(processedAttributes);
            if(true/*!nonProcessedAttributes.isEmpty()*/) {
                ModelElement_1_0 classifierDef = this.getModel().findElement(newObjectFacade.getObjectClass());
                if(classifierDef == null) {
                    SysLog.warning("No classifier definition found", newObject);                    
                }
                for(Iterator<String> i = nonProcessedAttributes.iterator(); i.hasNext(); ) {
                    String attributeName = i.next();
                    ModelElement_1_0 featureDef = classifierDef == null ? 
                        null : 
                            classifierDef.getModel().getFeatureDef(
                            classifierDef, 
                            attributeName, 
                            true // includeSubtypes
                        ); 
                    if(featureDef == null) {
                        SysLog.log(Level.WARNING, "Sys|No feature definition found|{0}#{1}", newObject.getRecordName(), attributeName);
                    }
                    if(featureDef != null && !ModelHelper.isChangeable(featureDef)) {
                        i.remove();
                    }                    
                }
                if(!nonProcessedAttributes.isEmpty()) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.MEDIA_ACCESS_FAILURE, 
                        "Some attributes can not be stored. Check for missing columns.",
                        new BasicException.Parameter("object", newObject),
                        new BasicException.Parameter("processed attributes", processedAttributes),
                        new BasicException.Parameter("non-processed attributes", nonProcessedAttributes),
                        new BasicException.Parameter("db objects", dbObjects)
                    );
                }
            }
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", newObjectFacade.getPath()),
                new BasicException.Parameter("statement", currentStatement),
                new BasicException.Parameter("values", statementParameters),
                new BasicException.Parameter("sqlErrorCode", ex.getErrorCode()), 
                new BasicException.Parameter("sqlState", ex.getSQLState())
            );
        }
        finally {
            try {
                if(ps != null) ps.close();
            } catch(Throwable ex) {
                // ignore
            }
        }
    }

    //---------------------------------------------------------------------------  
    @Override
    public void replaceObjectSlice(
        int index,
        ObjectRecord newObject,
        ObjectRecord oldObject, 
        String writeLock, 
        String readLock
    ) throws ServiceException {
        this.replaceObjectSlice(
            index, 
            newObject, 
            oldObject, 
            this.getReferenceColumn(),
            this.getReferenceValues(),
            this.objectIdColumn,
            this.getObjectIdValues(),
            null, 
            writeLock, 
            readLock
        );    
    }

    
    //---------------------------------------------------------------------------
    @Override
    public ObjectRecord[] sliceAndNormalizeObject(
        ObjectRecord object, 
        boolean removeValuesProvidedByView
    ) throws ServiceException {
        try {
            Object_2Facade facade = Facades.asObject(object);
            // Add object class as attribute. This way it can be handled as a standard feature
            if(facade.getAttributeValues(SystemAttributes.OBJECT_CLASS) == null) {
                facade.addToAttributeValuesAsList(
                    SystemAttributes.OBJECT_CLASS,
                    facade.getObjectClass()
                );
            }
            // Add size attributes
            if(this.database.isSetSizeColumns()) {
                ModelElement_1_0 classDef = getModel().getElement(
                    facade.getObjectClass()
                );
                for(ModelElement_1_0 feature : getModel().getAttributeDefs(classDef, false, false).values()) {
                    String featureName = feature.getName();                
                    if(
                        !this.database.isEmbeddedFeature(featureName) &&
                        this.database.isPersistent(feature) &&
                        ModelHelper.getMultiplicity(feature).isMultiValued() 
                        
                    ) {
                        facade.replaceAttributeValuesAsListBySingleton(
                            featureName + this.database.getSizeSuffix(),
                            Integer.valueOf(
                                facade.getAttributeValues(featureName) == null ? 0 :facade.getSizeOfAttributeValuesAsList(featureName)
                            )   
                        );
                    }
                }
                // created_by, modified_by are derived and persistent
                if(this.getModel().isSubtypeOf(classDef, "org:openmdx:base:BasicObject")) {            
                    String featureName = SystemAttributes.CREATED_BY;
                    facade.replaceAttributeValuesAsListBySingleton(
                        featureName + "_",
                        Integer.valueOf(
                            facade.getAttributeValues(featureName) == null ? 0 : 
                            facade.getSizeOfAttributeValuesAsList(featureName)
                        )
                    );
                    featureName = SystemAttributes.MODIFIED_BY;
                    facade.replaceAttributeValuesAsListBySingleton(
                        featureName + "_",
                        Integer.valueOf(
                            facade.getAttributeValues(featureName) == null ? 0 : 
                            facade.getSizeOfAttributeValuesAsList(featureName)
                        )
                    );
                }
            }
            /**
             * Add normalized paths to object as pair of (referenceId, objectId).
             * The new attributes are of the form $<attributeName>_referenceId and
             * $<attributeName>_objectId, respectively. The leading PRIVATE_ATTRIBUTES_PREFIX 
             * is an indicator to ignore the attribute on object retrieval.
             */
            DbObjectConfiguration dbObjectConfiguration = this.getConfiguration();    
            Object_2Facade normalizedObjectFacade = Facades.newObject(
                new Path(""),
                facade.getObjectClass()
            );
            this.normalizeObjectPaths(
                facade, 
                dbObjectConfiguration.getPathNormalizeLevel(), 
                normalizedObjectFacade, 
                removeValuesProvidedByView
            );
            normalizedObjectFacade.getValue().keySet().removeAll(
                facade.getValue().keySet()
            );
            facade.getValue().putAll(
                normalizedObjectFacade.getValue()
            );
            /**
             * Slice object
             */
            // get number of partitions
            int nSlices = 0;
            for(
                Iterator i = facade.getValue().keySet().iterator();
                i.hasNext();
            ) {
                String attributeName = (String)i.next();
                Object values = facade.attributeValues(attributeName);
                if(values instanceof SparseArray) {
                    SparseArray a = (SparseArray)values;
                    if(!a.isEmpty()) {
                        nSlices = java.lang.Math.max(
                            nSlices, 
                            ((Integer)a.lastKey()).intValue() + 1
                        );
                    } 
                } else {
                    List l = (List)values;
                    nSlices = java.lang.Math.max(
                        nSlices, 
                        l.size()
                    );
                }
            }
            // create partitioned objects
            ObjectRecord[] slices = new ObjectRecord[nSlices];
            for(
                Iterator i = facade.getValue().keySet().iterator();
                i.hasNext();
            ) {
                String attributeName = (String)i.next();
                Object values = facade.attributeValues(attributeName);
                int lastIndex = -1;
                if(values instanceof SparseArray) {
                    SparseArray a = (SparseArray)values;
                    if(!a.isEmpty()) {
                        lastIndex = ((Integer)a.lastKey()).intValue();
                    } 
                } else {
                    List l = (List)values;
                    lastIndex = l.size() - 1;
                }
                for(int j = 0; j <= lastIndex; j++) { 
                    if(slices[j] == null) {
                        slices[j] = Facades.newObject(
                            Object_2Facade.getPath(object),
                            facade.getObjectClass()
                        ).getDelegate();
                    }
                    // Embedded features are mapped to slice 0
                    if(this.database.isEmbeddedFeature(attributeName)) {
                        Object_2Facade sliceFacade = Facades.asObject(slices[0]);
                        sliceFacade.addToAttributeValuesAsList(
                            attributeName + "_" + j,
                            facade.getAttributeValueFromList(attributeName,j)
                        );
                    }
                    // Map to slice with corresponding index
                    else {                
                        Object_2Facade sliceFacade = Facades.asObject(slices[j]);
                        sliceFacade.replaceAttributeValuesAsListBySingleton("objectIdx", Integer.valueOf(j));
                        sliceFacade.replaceAttributeValuesAsListBySingleton(
                            attributeName,
                            values instanceof SparseArray ? ((SparseArray)values).get(Integer.valueOf(j)) :
                            j < ((List)values).size() ? ((List)values).get(j) : 
                            null
                        );
                    }
                }
            }
            if(dbObjectConfiguration.getUnitOfWorkProvider() != null && facade.getPath().size() >= 5 && slices.length > 0) {
                String unitOfWorkReferenceAttribute = this.database.toRid(this.database.getPrivateAttributesPrefix() + "unitOfWork"); 
                Object unitOfWorkReferenceId = this.database.getReferenceId(
                    conn, 
                    dbObjectConfiguration.getUnitOfWorkProvider().getDescendant("segment", facade.getPath().getSegment(4).toClassicRepresentation(), "unitOfWork"), 
                    true 
                );
                Facades.asObject(slices[0]).addToAttributeValuesAsList(
                    unitOfWorkReferenceAttribute,
                    unitOfWorkReferenceId
                );
            }
            return slices;
        } catch (RuntimeException e) {
            throw new ServiceException(e);
        }
        
    }

    /**
     * Normalize Objetc Paths
     * 
     * @param facade
     * @param normalizedObjectFacade
     * @param removeValuesProvidedByView 
     * @param pathBormalizationLevel
     * 
     * @throws ServiceException  
     */
    protected void normalizeObjectPaths(
        Object_2Facade facade,
        int pathNormalizeLevel,
        Object_2Facade normalizedObjectFacade, 
        boolean removeValuesProvidedByView
    ) throws ServiceException {
        // Add (rid,oid) object parent
        if(pathNormalizeLevel > 0) {  
            Path parentObjectPath = facade.getPath().getPrefix(facade.getPath().size()-2);    
            if(parentObjectPath.size() >= 5) {
                String name = this.database.getPrivateAttributesPrefix() + "objectParent";
                normalizedObjectFacade.addToAttributeValuesAsList(
                    this.database.toRid(name),
                    this.database.getReferenceId(
                        conn, 
                        parentObjectPath, 
                        true 
                    )
                );
                normalizedObjectFacade.addToAttributeValuesAsList(
                    this.database.toOid(name),
                    parentObjectPath.getLastSegment().toClassicRepresentation()
                );
            }
            //
            // add (rid, oid) for all attributes with values of type path
            //
            Set<String> processedAttributes = TRUST_INTERNALIZATION ? null : new HashSet<String>();
            if(pathNormalizeLevel > 1) {    
                Attribute: for(
                    Iterator i = facade.getValue().keySet().iterator();
                    i.hasNext();
                ) {
                    String attributeName = (String)i.next();
                    Object values = facade.attributeValues(attributeName);
                    Object firstValue = null;
                    ListIterator valuesIterator = null;
                    if(values instanceof SparseArray) {
                        SparseArray v = (SparseArray)values;
                        firstValue = v.isEmpty() ? null : v.get(v.firstKey());
                        valuesIterator = v.populationIterator();
                    }
                    else if(values instanceof List) {
                        List v = (List)values;
                        firstValue = v.isEmpty() ? null : v.get(0);     
                        valuesIterator = v.listIterator();
                    }
                    if(!TRUST_INTERNALIZATION && !processedAttributes.add(attributeName)){
                        SysLog.error(
                            "Object value contains duplicate key",
                            BasicException.newStandAloneExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "Object value contains duplicate key",
                                new BasicException.Parameter("path", facade.getPath()),
                                new BasicException.Parameter("attributeNames", facade.getValue().keySet()),
                                new BasicException.Parameter("processedAttributes", processedAttributes),
                                new BasicException.Parameter("duplicateAttributeName", attributeName),
                                new BasicException.Parameter("values", values),
                                new BasicException.Parameter("firstValueClass", firstValue == null ? "null" : firstValue.getClass().getName()),
                                new BasicException.Parameter("firstValue", firstValue)
                            )
                        );
                        continue Attribute;
                    }
                    if(firstValue instanceof Path) {
                        while(valuesIterator.hasNext()) {
                            Object v = valuesIterator.next();
                            if(!(v instanceof Path)) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.ASSERTION_FAILURE, 
                                    "value of attribute expected to be instance of path",
                                    new BasicException.Parameter("attribute", attributeName),
                                    new BasicException.Parameter("value class", v == null ? "null" : v.getClass().getName()),
                                    new BasicException.Parameter("value", v)
                                );
                            }
                            Path objectPath = (Path)v;
                            String name = this.database.getPrivateAttributesPrefix() + attributeName;
                            normalizedObjectFacade.addToAttributeValuesAsList(
                                this.database.toRid(name),
                                this.database.getReferenceId(
                                    this.conn, 
                                    objectPath, 
                                    true    
                                )
                            );
                            normalizedObjectFacade.addToAttributeValuesAsList(
                                this.database.toOid(name),
                                objectPath.getLastSegment().toClassicRepresentation()
                            );
                            // add parent of path value
                            if(pathNormalizeLevel > 2) {
                                Path parentPath = objectPath.getPrefix(objectPath.size()-2);
                                if(parentPath.size() >= 5) {
                                    normalizedObjectFacade.addToAttributeValuesAsList(
                                        this.database.toRid(name + "Parent"),
                                        this.database.getReferenceId(
                                            conn, 
                                            parentPath, 
                                            true
                                        )
                                    );
                                    normalizedObjectFacade.addToAttributeValuesAsList(
                                        this.database.toOid(name + "Parent"),
                                        parentPath.getLastSegment().toClassicRepresentation()
                                    );
                                }
                            }
                        }
                    }
                } 
            }
        }
    }

    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3256443624899490868L;
    protected static final String COLUMN_TYPE_NAME = "type_name";

    protected static final Map dbObjectColumns = new HashMap();
    
    /**
     * Tells whether the internalization shall be trusted or validated
     */
    private final static boolean TRUST_INTERNALIZATION = Boolean.getBoolean("org.openmdx.application.dataprovider.layer.persistence.jdbc.SlicedDbObject.TrustInternalization");
    		

}

//--- End of File -----------------------------------------------------------

