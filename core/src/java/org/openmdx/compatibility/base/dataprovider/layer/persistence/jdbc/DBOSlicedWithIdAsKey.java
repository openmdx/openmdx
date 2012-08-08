/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DBOSlicedWithIdAsKey.java,v 1.23 2008/02/05 17:11:58 wfro Exp $
 * Description: SlicedDbObjectParentRidOnly class
 * Revision:    $Revision: 1.23 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/05 17:11:58 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.code.Multiplicities;

/**
 * Rows of this type do not contain the column object_rid. Instead, the rows
 * contain the columns parent_object__rid and parent_object__oid. This db 
 * object type is used when the calculation of object_rid is expensive
 * compared to the calculation of the object_parent columns.
 */
public class DBOSlicedWithIdAsKey
  extends SlicedDbObject 
{

    //-------------------------------------------------------------------------
    public DBOSlicedWithIdAsKey(
        AbstractDatabase_1 database,
        Connection conn, 
        DbObjectConfiguration typeConfiguration
    ) {
        super(
            database, 
            conn, 
            typeConfiguration
        );
    }
    
    //-------------------------------------------------------------------------
    public DBOSlicedWithIdAsKey(
        AbstractDatabase_1 database,
        Connection conn, 
        DbObjectConfiguration typeConfigurationEntry,
        Path accessPath, 
        boolean isExtent, 
        boolean isQuery
    ) throws ServiceException {
        super(
            database, 
            conn, 
            typeConfigurationEntry, 
            accessPath, 
            isExtent,
            isQuery
        );
        String rid = (String)this.database.getReferenceId(
            conn, 
            this.reference, 
            false
        );
        // oid
        this.objectIdValues.clear();
        this.objectIdValues.add(
            rid + "/" + this.getObjectId()
        );
        this.objectIdClause = "(v." + database.OBJECT_ID + " = ?)";
        this.objectIdColumn.clear();
        this.objectIdColumn.add(database.OBJECT_ID);
        if(isExtent) {
            this.referenceValues.clear();
            this.referenceValues.add(
                accessPath.endsWith(new String[]{"%"}) || accessPath.endsWith(new String[]{":*"}) 
                    ? rid + "/%" 
                    : rid
            );
            this.referenceClause = "(v." + database.OBJECT_ID + " LIKE ? " + this.database.getEscapeClause(conn) + ")";
        }
        else {
            this.referenceValues.clear();
            if(this.getJoinCriteria() == null) {
                String databaseProductName = "N/A";
                try {
                    DatabaseMetaData dbm = conn.getMetaData();
                    databaseProductName = dbm.getDatabaseProductName();
                } catch(Exception e) {}
                if(databaseProductName.startsWith("PostgreSQL")) {
                    this.referenceValues.clear();
                    this.referenceValues.add(rid + "/%");
                    this.referenceClause = "(v." + database.OBJECT_ID + " LIKE ?)";                     
                }
                else {
                    // By default do not use LIKE because
                    // <ul>
                    //   <li>LIKE is case insensitive
                    //   <li>Does not perform well for all databases
                    // </ul>
                    // rid is a LIKE pattern. Remove %
                    if(rid.endsWith("/%")) {           
                        this.referenceValues.add(rid.substring(0, rid.length()-1));
                        this.referenceValues.add(rid.substring(0, rid.length()-2) + "0");
                    }
                    else {
                        this.referenceValues.add(rid + "/");
                        this.referenceValues.add(rid + "0");            
                    }
                    this.referenceClause = "((v." + database.OBJECT_ID + " > ?) AND (v." + database.OBJECT_ID + " < ?))";
                }
            }
            else {
                String parentRid = (String)this.database.getReferenceId(
                    conn, 
                    this.reference.getParent().getParent(),
                    false
                );        
                this.referenceValues.add(
                    parentRid + "/" + this.reference.getParent().getBase()
                );
                // Default join with composite parent. Otherwise use configured join column
                this.referenceClause = "(vj." + this.getJoinCriteria()[1] + " = ?)";            
            }
        }
        this.referenceColumn.clear();
        // non index column for non-indexed sliced DB objects
        this.indexColumn = null;
        this.excludeAttributes.add("objectIdx");        
    }
    
    //---------------------------------------------------------------------------  
    public Path getObjectReference(
        FastResultSet frs
    ) throws SQLException, ServiceException {      
      String objectId = (String)frs.getObject("object_id");
      if(objectId == null) {
          throw new SQLException(
              "column object_id in result set not found"
          );
      }
      // Map objectId to reference
      else {          
          return this.database.getReference(
              conn,
              objectId.substring(0, objectId.lastIndexOf("/"))
          );
      }
    }
      
    //---------------------------------------------------------------------------  
    public String getObjectId(
        FastResultSet frs
    ) throws SQLException {
      String objectId = (String)frs.getObject("object_id");
      if(objectId == null) {
          throw new SQLException(
              "column object_id in result set not found"
          );
      }
      // oid is last component of object_id
      else {    
          return objectId.substring(objectId.lastIndexOf("/")+1);
      }
    }
          
    //---------------------------------------------------------------------------  
    public boolean includeColumn(
        String columnName
    ) {
        return 
          !"object_id".equalsIgnoreCase(columnName) &&
          !this.database.OBJECT_IDX.equalsIgnoreCase(columnName) &&
          !columnName.toLowerCase().startsWith(this.database.privateAttributesPrefix) &&
          !columnName.endsWith("_");
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
    public void remove(
    ) throws ServiceException {
      
      PreparedStatement ps = null;
      String currentStatement = null;
      Path accessPath = this.reference.getChild(this.objectId);
      Path type = this.getConfiguration().getType();
      List dbObjects = new ArrayList();
      if(this.getConfiguration().getDbObjectForUpdate1() != null) {
          dbObjects.add(
              this.getConfiguration().getDbObjectForUpdate1()
          );
      }
      if(this.getConfiguration().getDbObjectForUpdate2() != null) {
          dbObjects.add(
              this.getConfiguration().getDbObjectForUpdate2()
          );
      }
      try {
          for(
              Iterator i = dbObjects.iterator();
              i.hasNext();
          ) {
              String dbObject = (String)i.next();          
              // Object (only if dbObject (=table) is configured)
              if(
                  ((type.size() == 1) || // catch all type
                  (type.size() == accessPath.size() && accessPath.isLike(type)))
              ) {
                  List statementParameters = new ArrayList();
                  statementParameters.addAll(
                      this.getObjectIdValues()
                  );
                  String statement =
                      "DELETE FROM " + dbObject + " WHERE " + this.database.OBJECT_ID + " IN (?)";
                  ps = this.database.prepareStatement(
                      conn,
                      currentStatement = statement
                  );
                  for(int j = 0; j < statementParameters.size(); j++) {
                      this.database.setPreparedStatementValue(
                          this.conn,
                          ps, 
                          j+1, 
                          statementParameters.get(j)
                      );
                  }
                  SysLog.detail("statement", currentStatement);
                  ps.executeUpdate();
                  this.database.executeBatch(ps);
                  ps.close(); ps = null;
              }
              // Composite objects (only if dbObject (=table) is configured)
              if(
                  ((type.size() == 1) || // catch all type
                  ((type.size() > accessPath.size()) && accessPath.isLike(type.getPrefix(accessPath.size()))))
              ) {
                  List statementParameters = new ArrayList();
                  Object rid = this.database.getReferenceId(
                      this.conn,
                      accessPath.getDescendant(type.getSuffix(accessPath.size())),
                      false
                  );
                  String statement = null;
                  if((rid instanceof String) && ((String)rid).endsWith("%")) {
                      statement = "DELETE FROM " + dbObject + " WHERE " + this.database.OBJECT_ID + " LIKE ?"; 
                      statementParameters.add(rid);
                  }
                  else {
                      statement = "DELETE FROM " + dbObject + " WHERE (" + this.database.OBJECT_ID + " > ?) AND (" + this.database.OBJECT_ID + " < ?)";                       
                      statementParameters.add(rid  + "/");
                      statementParameters.add(rid  + "0");
                  }
                  ps = this.database.prepareStatement(
                      conn,
                      currentStatement = statement
                  );
                  for(int j = 0; j < statementParameters.size(); j++) {
                      this.database.setPreparedStatementValue(
                          this.conn,
                          ps, 
                          j+1, 
                          statementParameters.get(j)
                      );
                  }
                  SysLog.detail("statement", currentStatement);
                  SysLog.detail("parameters", statementParameters);
                  ps.executeUpdate();
                  this.database.executeBatch(ps);
                  ps.close(); ps = null;
              }
          }
      }
      catch(SQLException ex) {
          throw new ServiceException(
              ex, 
              StackedException.DEFAULT_DOMAIN,
              StackedException.MEDIA_ACCESS_FAILURE, 
              new BasicException.Parameter[]{
                  new BasicException.Parameter("path", accessPath),
                  new BasicException.Parameter("statement", currentStatement)
              },
              null
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
              null, 
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
    }
      
    //---------------------------------------------------------------------------
    public DataproviderObject[] sliceAndNormalizeObject(
        DataproviderObject object
    ) throws ServiceException {
    
        DbObjectConfiguration dbObjectConfiguration = this.getConfiguration();    
        DataproviderObject normalizedObject = new DataproviderObject(new Path(""));
        int pathNormalizeLevel = dbObjectConfiguration.getPathNormalizeLevel();
                 
        // Add size attributes
        if(this.database.isSetSizeColumns()) {
            ModelElement_1_0 classDef = this.database.model.getElement(
                object.values(SystemAttributes.OBJECT_CLASS).get(0)
            );
            for(
                Iterator i = this.database.model.getAttributeDefs(classDef, false, false).values().iterator();
                i.hasNext();
            ) {
                ModelElement_1_0 feature = (ModelElement_1_0)i.next();
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
        
        // Add parent id 
        if(pathNormalizeLevel > 0) {  
          Path parentObjectPath = object.path().getPrefix(object.path().size()-2);    
          if(parentObjectPath.size() >= 5) {
              normalizedObject.values(this.database.privateAttributesPrefix + "parent").add(
                  this.database.getReferenceId(
                      conn, 
                      parentObjectPath, 
                      true 
                  ) + "/" + parentObjectPath.getBase()
              );
          }
    
          // Add id for all attributes with values of type path
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
                  normalizedObject.values(attributeName).add(
                      this.database.getReferenceId(
                          conn, 
                          objectPath, 
                          true
                      ) + "/" + objectPath.getBase()
                  );
                  
                  // add parent id of path value
                  if(pathNormalizeLevel > 2) {
                    Path parentPath = objectPath.getPrefix(objectPath.size()-2);
                    if(parentPath.size() >= 5) {
                        normalizedObject.values(this.database.privateAttributesPrefix + attributeName + "Parent").add(
                            this.database.getReferenceId(
                                conn, 
                                parentPath, 
                                true
                            ) + "/" + parentPath.getBase()
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
        
        // Create partitioned objects
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

    //---------------------------------------------------------------------------
    protected String toObjectIdQuery (
      Path path
    ) throws ServiceException {
        Object rid = this.database.getReferenceId(this.conn, path, true);
        String pathComponentQuery = path.getBase();
        return pathComponentQuery.startsWith(":") && pathComponentQuery.endsWith("*") 
            ? rid + "/" + pathComponentQuery.substring(1, pathComponentQuery.length() - 1) + '%' 
            : rid + "/" + pathComponentQuery;
    }
  
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 4076702439130733210L;

}

//--- End of File -----------------------------------------------------------
