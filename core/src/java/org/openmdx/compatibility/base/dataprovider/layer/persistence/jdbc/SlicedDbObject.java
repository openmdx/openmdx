/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SlicedDbObject.java,v 1.49 2008/05/08 10:20:39 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.49 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/08 10:20:39 $
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
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.code.Multiplicities;

@SuppressWarnings("unchecked")
public class SlicedDbObject 
  extends StandardDbObject {

  //-------------------------------------------------------------------------
  public SlicedDbObject(
      AbstractDatabase_1 database, 
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
    AbstractDatabase_1 database, 
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
    public int getIndex(
        FastResultSet frs
    ) throws SQLException {
        if(frs.getColumnNames().contains(this.database.OBJECT_IDX)) {
            return ((Number)frs.getObject(this.database.OBJECT_IDX)).intValue();
        }
        else {
            return 0;
        }
    }
          
  //-------------------------------------------------------------------------
  private Set getDbObjectColumns(
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
                StackedException.DEFAULT_DOMAIN,
                StackedException.MEDIA_ACCESS_FAILURE, 
                new BasicException.Parameter[]{
                  new BasicException.Parameter("statement", currentStatement)
                },
                null
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
      DataproviderObject object,
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
            String statement = "INSERT INTO " + dbObject + " (";
            statementParameters.clear();
            int k = 0;      
            // All attributes for dbObject
            for(
                Iterator i = object.attributeNames().iterator(); 
                i.hasNext();
            ) {
                String attributeName = (String)i.next();
                if((excludeAttributes == null || !excludeAttributes.contains(attributeName)) && (attributeName.indexOf(':') < 0)) {
                  SparseList attributeValues = object.values(attributeName);
                  if(attributeValues.isEmpty()) {
                      String columnName = this.database.getColumnName(
                          this.conn, 
                          attributeName, 
                          0, 
                          false, 
                          true
                      );
                      if(dbObjectColumns.contains(columnName)) {
                          processedAttributes.add(
                              attributeName
                          );
                      }                      
                  }
                  else {
                      for(
                        ListIterator j = attributeValues.populationIterator(); 
                        j.hasNext(); 
                      ) {
                        int valIndex = j.nextIndex();
                        Object value = j.next();
                        String columnName = this.database.getColumnName(
                            this.conn, 
                            attributeName, 
                            valIndex, 
                            false, 
                            true
                        );
                        if(dbObjectColumns.contains(columnName)) {
                            if(!columnName.equals(this.database.OBJECT_IDX)) {
                                if(k > 0) statement += ", ";
                                statement += this.database.getColumnName(
                                    this.conn, 
                                    attributeName, 
                                    valIndex, 
                                    false, 
                                    false // escape reserved words
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
                    // AS <format> allows to cast/format the sequence number
                    int posAs = autonumColumn.indexOf(" AS ");
                    if(posAs > 0) {
                        autonumColumnName = autonumColumn.substring(0, posAs).trim();
                    }
                    // Only add if not supplied explicitely as attribute
                    if(!processedColumns.contains(autonumColumnName)) {
                        if(autonumColumns == null) {
                            autonumColumns = new ArrayList();
                        }
                        autonumColumns.add(
                            autonumColumnName
                        );
                        String sequenceName = this.database.namespaceId + "_" + autonumColumnName;
                        String autonumValue = this.database.getAutonumValue(
                            this.conn,
                            sequenceName,
                            dbObject,
                            posAs > 0 
                                ? autonumColumn.substring(posAs)
                                : null
                        );
                        // Emulate SQL sequence
                        if(autonumValue == null) {
                            ps = this.database.prepareStatement(
                                this.conn,
                                currentStatement = "SELECT nextval FROM " + sequenceName + "_SEQ"
                            );
                            rs = ps.executeQuery();
                            if(rs.next()) {
                                autonumValue = rs.getString("nextval");
                                rs.close();
                                ps.close();
                                ps = this.database.prepareStatement(
                                    this.conn,
                                    currentStatement = "UPDATE " + sequenceName + "_SEQ SET nextval = nextval + 1"
                                );
                                ps.executeUpdate();
                                ps.close();
                            }
                            else {
                                autonumValue = "0";
                                ps = this.database.prepareStatement(
                                    this.conn,
                                    currentStatement = "INSERT INTO " + sequenceName + "_SEQ (nextval) VALUES (0)"
                                );                          
                                ps.executeUpdate();
                                ps.close();
                            }
                        }
                        if(autonumValues == null) {
                            autonumValues = new ArrayList();                   
                        }
                        autonumValues.add(
                            posAs > 0
                                ? "CAST(" + autonumValue + autonumColumn.substring(posAs) + ")"
                                : autonumValue
                        );
                    }
                }
            }  
            // typeName
            String columnNameTypeName = this.database.privateAttributesPrefix + COLUMN_TYPE_NAME;
            if(dbObjectColumns.contains(columnNameTypeName)) {
                if(k > 0) statement += ", ";
                statement += columnNameTypeName;
                statementParameters.add(this.reference.getBase());
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
                    true
                );
                if(dbObjectColumns.contains(columnNameObjectClass)) {
                    if(k > 0) statement += ", ";
                    statement += columnNameObjectClass;
                    statementParameters.add(objectClass);
                    k++;
                    processedAttributes.add(
                        SystemAttributes.OBJECT_CLASS
                    );                    
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
                statementParameters.add(new Integer(index));
                k++;
            }
            // secondary tables require index
            else if(processAsSecondary) {
                if(k > 0) statement += ", ";
                statement += this.database.OBJECT_IDX;
                statementParameters.add(new Integer(index));
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
                currentStatement = statement.toString()
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
            SysLog.detail("statement", currentStatement);
            SysLog.detail("values", statementParameters);
            ps.executeUpdate();
            this.database.executeBatch(ps);
            ps.close(); ps = null;
        }
        if(!processedAttributes.containsAll(object.attributeNames())) {
            Set nonProcessedAttributes = new HashSet(object.attributeNames());
            nonProcessedAttributes.removeAll(processedAttributes);
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.MEDIA_ACCESS_FAILURE, 
                new BasicException.Parameter[]{
                  new BasicException.Parameter("object", object),
                  new BasicException.Parameter("processed attributes", processedAttributes),
                  new BasicException.Parameter("non-processed attributes", nonProcessedAttributes),
                  new BasicException.Parameter("db objects", dbObjects)
                },
                "Attributes can not be stored. Missing columns in db objects"
            );                
        }                              
    }
    catch(SQLException ex) {
        String sqlState = ex.getSQLState();
        throw new ServiceException(
            ex, 
            StackedException.DEFAULT_DOMAIN,
            "23000".equals(sqlState) || "23505".equals(sqlState) ? 
                BasicException.Code.DUPLICATE :
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
            new BasicException.Parameter[]{
              new BasicException.Parameter("path", object.path()),
              new BasicException.Parameter("statement", currentStatement),
              new BasicException.Parameter("values", statementParameters),
              new BasicException.Parameter("types", statementParameterTypes)
            },
            null
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
  public void createObjectSlice(
      int index,
      String objectClass,
      DataproviderObject object
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
  public void createMultiValuedObject(
    DataproviderObject object
  ) throws ServiceException {
    throw new ServiceException(
      StackedException.DEFAULT_DOMAIN,
      StackedException.NOT_SUPPORTED, 
      new BasicException.Parameter[]{
        new BasicException.Parameter("object", object)
      },
      "creation of multi-valued object not supported for sliced type"
    );   
  }
  
  //---------------------------------------------------------------------------  
  public boolean supportsObjectReplacement(
  ) {
    return true;
  }
  
  //---------------------------------------------------------------------------  
  protected void replaceObjectSlice(
    int index,
    DataproviderObject newObject,
    DataproviderObject oldObject,
    List referenceIdColumns,
    List referenceIdValues,
    List objectIdColumns,
    List objectIdValues,
    Set excludeAttributes
  ) throws ServiceException {
  
    PreparedStatement ps = null;
    String currentStatement = null;
    List statementParameters = new ArrayList();
    Set statementColumns = new HashSet();
    Set processedAttributes = new HashSet();
    if(excludeAttributes != null) {
        processedAttributes.addAll(excludeAttributes);
    }
    if(this.excludeAttributes != null) {
        processedAttributes.addAll(this.excludeAttributes);
    }    
    
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
            String statement = "UPDATE " + dbObject + " SET ";
            statementParameters.clear();
            boolean hasParameters = false;
            
            // newValues
            for(
                Iterator i = newObject.attributeNames().iterator(); 
                i.hasNext();
            ) {
                String attributeName = (String)i.next();
                if(((excludeAttributes == null) || !excludeAttributes.contains(attributeName)) && attributeName.indexOf(':') < 0) {
                  SparseList attributeValues = newObject.values(attributeName);   
                  if(attributeValues.isEmpty()) {
                      String columnName = this.database.getColumnName(conn, attributeName, 0, false, true);
                      if(dbObjectColumns.contains(columnName)) {
                          processedAttributes.add(
                              attributeName
                          );
                      }                      
                  }
                  else {
                      for(
                        ListIterator j = attributeValues.populationIterator(); 
                        j.hasNext(); 
                      ) {
                        int valIndex = j.nextIndex();
                        Object value = j.next();
                        String columnName = this.database.getColumnName(conn, attributeName, valIndex, false, true);
                        if(dbObjectColumns.contains(columnName)) {
                            if(!columnName.equals(this.database.OBJECT_IDX)) {
                                statement += hasParameters ? ", " : "";
                                statement += this.database.getColumnName(conn, attributeName, valIndex, false, false) + " = ";
                                statement += this.database.getPlaceHolder(conn, value); 
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
                Iterator i = oldObject.attributeNames().iterator(); 
                i.hasNext();
            ) {
                String attributeName = (String)i.next();
                if(((excludeAttributes == null) || !excludeAttributes.contains(attributeName)) && attributeName.indexOf(':') < 0) {
                  SparseList attributeValues = oldObject.values(attributeName);
                  if(attributeValues.isEmpty()) {
                      String columnName = this.database.getColumnName(conn, attributeName, 0, false, true);
                      if(dbObjectColumns.contains(columnName)) {
                          processedAttributes.add(
                              attributeName
                          );
                      }                      
                  }
                  else {
                      for(
                        ListIterator j = attributeValues.populationIterator(); 
                        j.hasNext(); 
                      ) {
                        int valIndex = j.nextIndex();
                        String columnName = this.database.getColumnName(conn, attributeName, valIndex, false, true);
                        if(dbObjectColumns.contains(columnName)) {
                            if(
                                !columnName.equals(this.database.OBJECT_IDX) && 
                                !statementColumns.contains(columnName)
                            ) {
                                statement += hasParameters ? ", " : "";
                                statement += columnName + " = NULL";
                                statementColumns.add(columnName);
                                hasParameters = true;
                            }
                            processedAttributes.add(
                                attributeName
                            );                            
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
                statement += " WHERE ";
                boolean hasClause = false;
                
                // rid
                if(referenceIdColumns != null) {
                    for(Iterator i = referenceIdColumns.iterator(); i.hasNext(); ) {
                        if(hasClause) statement += " AND ";
                        statement += "(" + i.next() + " = ?)";
                        hasClause = true;
                    }
                    if(!referenceIdColumns.isEmpty()) {
                        statementParameters.addAll(referenceIdValues);
                    }
                }
                
                // oid
                if(objectIdColumns != null) {
                    for(Iterator i = objectIdColumns.iterator(); i.hasNext(); ) {
                        if(hasClause) statement += " AND ";
                        statement += "(" + i.next() + " = ?)";
                        hasClause = true;
                    }
                    if(!objectIdColumns.isEmpty()) {
                        statementParameters.addAll(objectIdValues);
                    }
                }                
                // idx
                if(this.getIndexColumn() != null) {
                    if(hasClause) statement += " AND ";
                    statement += "(" + this.getIndexColumn() + " = ?)";
                    statementParameters.add(new Integer(index));
                    hasClause = true;
                }
                // secondary tables require index
                else if(processAsSecondary) {
                    if(hasClause) statement += " AND ";
                    statement += "(" + this.database.OBJECT_IDX + " = ?)";
                    statementParameters.add(new Integer(index));
                    hasClause = true;                    
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
                SysLog.detail("statement", currentStatement);
                SysLog.detail("values", statementParameters);
                ps.executeUpdate();
                this.database.executeBatch(ps);
                ps.close(); ps = null;
            }
        }
        if(!processedAttributes.containsAll(newObject.attributeNames())) {
            Set nonProcessedAttributes = new HashSet(newObject.attributeNames());
            nonProcessedAttributes.removeAll(processedAttributes);
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.MEDIA_ACCESS_FAILURE, 
                new BasicException.Parameter[]{
                  new BasicException.Parameter("object", newObject),
                  new BasicException.Parameter("processed attributes", processedAttributes),
                  new BasicException.Parameter("non-processed attributes", nonProcessedAttributes),
                  new BasicException.Parameter("db objects", dbObjects)
                },
                "Attributes can not be stored. Missing columns in db objects"
            );                
        }                              
    }
    catch(SQLException ex) {
      throw new ServiceException(
        ex, 
        StackedException.DEFAULT_DOMAIN,
        StackedException.MEDIA_ACCESS_FAILURE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("path", newObject.path()),
          new BasicException.Parameter("statement", currentStatement),
          new BasicException.Parameter("values", statementParameters)
        },
        null
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
  public void replaceObjectSlice(
    int index,
    DataproviderObject newObject,
    DataproviderObject oldObject
  ) throws ServiceException {
      this.replaceObjectSlice(
          index, 
          newObject, 
          oldObject, 
          this.getReferenceColumn(),
          this.getReferenceValues(),
          this.objectIdColumn,
          this.getObjectIdValues(),
          null
      );    
  }
  
    //---------------------------------------------------------------------------
    public DataproviderObject[] sliceAndNormalizeObject(
        DataproviderObject object
    ) throws ServiceException {
    
        // Add size attributes
        if(this.database.isSetSizeColumns()) {
            ModelElement_1_0 classDef = this.database.model.getElement(
                object.values(SystemAttributes.OBJECT_CLASS).get(0)
            );
            for(ModelElement_1_0 feature : this.database.model.getAttributeDefs(classDef, false, false).values()) {
                String featureName = (String)feature.values("name").get(0);                
                String featureQualifiedName = (String)feature.values("qualifiedName").get(0);
                if(
                    !this.database.embeddedFeatures.containsKey(featureName) &&
                    !this.database.nonPersistentFeatures.contains(featureQualifiedName)
                ) {
                    String multiplicity = (String)feature.values("multiplicity").get(0);
                    // multi-valued reference?
                    if(this.database.model.isReferenceType(feature)) {                    
                        ModelElement_1_0 end = this.database.model.getElement(
                            feature.values("referencedEnd").get(0)
                        );
                        if(!end.values("qualifierName").isEmpty()) {
                            multiplicity = Multiplicities.LIST;
                        }
                    }
                    if(                    
                        Multiplicities.MULTI_VALUE.equals(multiplicity) ||
                        Multiplicities.LIST.equals(multiplicity) ||
                        Multiplicities.SET.equals(multiplicity) ||
                        Multiplicities.SPARSEARRAY.equals(multiplicity)                    
                    ) {
                        object.clearValues(featureName + "_").add(
                            object.getValues(featureName) == null
                                ? new Integer(0)
                                : new Integer(object.values(featureName).size())
                        );
                    }
                }
            }
            // created_by, modified_by are derived and persistent
            if(this.database.model.isSubtypeOf(classDef, "org:openmdx:base:BasicObject")) {            
                String featureName = SystemAttributes.CREATED_BY;
                object.clearValues(featureName + "_").add(
                    object.getValues(featureName) == null
                        ? new Integer(0)
                        : new Integer(object.values(featureName).size())
                );
                featureName = SystemAttributes.MODIFIED_BY;
                object.clearValues(featureName + "_").add(
                    object.getValues(featureName) == null
                        ? new Integer(0)
                        : new Integer(object.values(featureName).size())
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
        DataproviderObject normalizedObject = new DataproviderObject(new Path(""));
        int pathNormalizeLevel = dbObjectConfiguration.getPathNormalizeLevel();
                 
        // Add (rid,oid) object parent 
        if(pathNormalizeLevel > 0) {  
          Path parentObjectPath = object.path().getPrefix(object.path().size()-2);    
          if(parentObjectPath.size() >= 5) {
              normalizedObject.values(this.database.privateAttributesPrefix + "objectParent" + this.database.referenceIdAttributesSuffix).add(
                  this.database.getReferenceId(
                      conn, 
                      parentObjectPath, 
                      true 
                  )
              );
              normalizedObject.values(this.database.privateAttributesPrefix + "objectParent" + this.database.objectIdAttributesSuffix).add(
                parentObjectPath.getBase()
              );
          }
    
          // add (rid, oid) for all attributes with values of type path
          if(pathNormalizeLevel > 1) {    
            for(
              Iterator i = object.attributeNames().iterator();
              i.hasNext();
            ) {
              String attributeName = (String)i.next();
              List values = object.values(attributeName);
              if((values.size() > 0) && (values.get(0) instanceof Path)) {
                for(
                  Iterator j = values.iterator();
                  j.hasNext();
                ) {
                  Object v = j.next();
                  if(!(v instanceof Path)) {
                    throw new ServiceException(
                      StackedException.DEFAULT_DOMAIN,
                      StackedException.ASSERTION_FAILURE, 
                      new BasicException.Parameter[]{
                        new BasicException.Parameter("attribute", attributeName),
                        new BasicException.Parameter("value class", (v == null ? "null" : v.getClass().getName())),
                        new BasicException.Parameter("value", v)
                      },
                      "value of attribute expected to be instance of path"
                    );
                  }
                  Path objectPath = (Path)v;
                  normalizedObject.values(this.database.privateAttributesPrefix + attributeName + this.database.referenceIdAttributesSuffix).add(
                      this.database.getReferenceId(
                          conn, 
                          objectPath, 
                          true
                      )
                  );
                  normalizedObject.values(this.database.privateAttributesPrefix + attributeName + this.database.objectIdAttributesSuffix).add(
                    objectPath.getBase()
                  );
                  
                  // add parent of path value
                  if(pathNormalizeLevel > 2) {
                    Path parentPath = objectPath.getPrefix(objectPath.size()-2);
                    if(parentPath.size() >= 5) {
                        normalizedObject.values(this.database.privateAttributesPrefix + attributeName + "Parent" + this.database.referenceIdAttributesSuffix).add(
                            this.database.getReferenceId(
                                conn, 
                                parentPath, 
                                true
                            )
                        );
                        normalizedObject.values(this.database.privateAttributesPrefix + attributeName + "Parent" + this.database.objectIdAttributesSuffix).add(
                          parentPath.getBase()
                        );
                    }
                  }
                }
              }
            } 
          }
        }
        object.addClones(normalizedObject, false);
        
        /**
         * Slice object
         */
        // get number of partitions
        int maxSize = 0;
        for(
          Iterator i = object.attributeNames().iterator();
          i.hasNext();
        ) {
          String attributeName = (String)i.next();
          maxSize = java.lang.Math.max(maxSize, object.values(attributeName).size());
        }
        
        // create partitioned objects
        DataproviderObject[] slices = new DataproviderObject[maxSize];
        for(
            Iterator i = object.attributeNames().iterator();
            i.hasNext();
        ) {
            String attributeName = (String)i.next();     
            for(
                int j = 0; 
                j < object.values(attributeName).size();
                j++
            ) {
                if(slices[j] == null) {
                    slices[j] = new DataproviderObject(object.path());
                }
                // Embedded features are mapped to slice 0
                if(this.database.embeddedFeatures.containsKey(attributeName)) {
                    slices[0].values(attributeName + "_" + j).add(
                        object.values(attributeName).get(j)
                    );
                }
                // Map to slice with corresponding index
                else {                
                    slices[j].values("objectIdx").add(
                        new Integer(j)
                    );
                    slices[j].values(attributeName).add(
                        object.values(attributeName).get(j)
                    );
                }
            }
        }
        return slices;
    }
  
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3256443624899490868L;
    private static final String COLUMN_TYPE_NAME = "type_name";
  
    protected static final Map dbObjectColumns = new HashMap();
  
}

//--- End of File -----------------------------------------------------------

