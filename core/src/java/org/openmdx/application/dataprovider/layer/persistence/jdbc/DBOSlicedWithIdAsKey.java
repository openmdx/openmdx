/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DBOSlicedWithIdAsKey.java,v 1.3 2009/06/02 15:00:07 wfro Exp $
 * Description: SlicedDbObjectParentRidOnly class
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/02 15:00:07 $
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
package org.openmdx.application.dataprovider.layer.persistence.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.kernel.exception.BasicException;

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
        DbObjectConfiguration typeConfiguration,
        Path accessPath, 
        boolean isExtent, 
        boolean isQuery
    ) throws ServiceException {
        super(
            database, 
            conn, 
            typeConfiguration, 
            accessPath, 
            isExtent,
            isQuery
        );
        String rid = (String)this.database.getReferenceId(
            conn, 
            this.getReference(), 
            false
        );
        String oid = this.database.getObjectId(
            conn, 
            this.getResourceIdentifier()
        );
        // oid
        this.objectIdValues.clear();
        this.objectIdValues.add(oid);
        this.objectIdClause = "(v." + database.OBJECT_ID + " = ?)";
        this.objectIdColumn.clear();
        this.objectIdColumn.add(database.OBJECT_ID);
        if(isExtent) {
            this.referenceValues.clear();
            String base = accessPath.getBase();
            this.referenceValues.add(
                base.equals("%") || base.equals(":*") ? 
                    rid + "/%" : 
                        rid
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
                String useLikeForOidMatching = System.getProperty(
                    "org.openmdx.persistence.jdbc.useLikeForOidMatching",
                    Boolean.toString(databaseProductName.startsWith("PostgreSQL"))
                );
                // Use like for oid matching (e.g. required for PostgreSQL databases with non-C locale)
                // Otherwise use the more efficient <> operators
                if(Boolean.valueOf(useLikeForOidMatching).booleanValue()) {
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
                this.referenceValues.add(
                    this.database.getObjectId(
                        conn,
                        this.getReference().getParent()
                    )
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
    @Override
    public Path getObjectReference(
        FastResultSet frs
    ) throws SQLException, ServiceException {      
        if(this.database.configuration.normalizeObjectIds()) {
            return getResourceIdentifier().getParent();
        } else {
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
    }

    //---------------------------------------------------------------------------  
    @Override
    public String getObjectId(
        FastResultSet frs
    ) throws SQLException {
        if(this.database.configuration.normalizeObjectIds()) {
            return getResourceIdentifier().getBase();
        } else {
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
    }

    //---------------------------------------------------------------------------  
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.DbObject#getResourceIdentifier(org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.FastResultSet)
     */
    @Override
    public Path getResourceIdentifier(
        FastResultSet frs
    ) throws SQLException, ServiceException {
        if(this.database.configuration.normalizeObjectIds()) {
            String objectId = (String)frs.getObject("object_id");
            if(objectId == null) {
                throw new SQLException(
                    "column object_id in result set not found"
                );
            }
            return this.database.configuration.buildResourceIdentifier(objectId, false);
        } else {
            return super.getResourceIdentifier(frs);
        }        
    }

    //---------------------------------------------------------------------------  
    @Override
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
    @Override
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
    @Override
    public void remove(
    ) throws ServiceException {

        PreparedStatement ps = null;
        String currentStatement = null;
        Path accessPath = this.getResourceIdentifier();
        Path type = this.getConfiguration().getType();
        List<String> dbObjects = new ArrayList<String>();
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
                Iterator<String> i = dbObjects.iterator();
                i.hasNext();
            ) {
                String dbObject = i.next();          
                // Object (only if dbObject (=table) is configured)
                if(
                    ((type.size() == 1) || // catch all type
                    (type.size() == accessPath.size() && accessPath.isLike(type)))
                ) {
                    List<Object> statementParameters = new ArrayList<Object>();
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
                    this.database.executeUpdate(ps, currentStatement, statementParameters);
                    ps.close(); ps = null;
                }
                // Composite objects (only if dbObject (=table) is configured)
                if(
                    ((type.size() == 1) || // catch all type
                    ((type.size() > accessPath.size()) && accessPath.isLike(type.getPrefix(accessPath.size()))))
                ) {
                    List<Object> statementParameters = new ArrayList<Object>();
                    Object rid = this.database.getReferenceId(
                        this.conn,
                        accessPath.getDescendant(type.getSuffix(accessPath.size())),
                        false
                    );
                    String statement = null;
                    String databaseProductName = "N/A";
                    try {
                        DatabaseMetaData dbm = this.conn.getMetaData();
                        databaseProductName = dbm.getDatabaseProductName();
                    } catch(Exception e) {}
                    String useLikeForOidMatching = System.getProperty(
                        "org.openmdx.persistence.jdbc.useLikeForOidMatching",
                        Boolean.toString(databaseProductName.startsWith("PostgreSQL"))
                    );
                    String ridAsString = rid.toString();
                    if(Boolean.valueOf(useLikeForOidMatching).booleanValue()) {    		
                        statement = "DELETE FROM " + dbObject + " WHERE " + this.database.OBJECT_ID + " LIKE ?"; 
                        statementParameters.add(ridAsString.endsWith("%") ? ridAsString : ridAsString + "%");
                    }
                    else {
                        if(ridAsString.endsWith("/%")) {
                            ridAsString = ridAsString.substring(0, ridAsString.length() - 2);
                        }
                        else if(ridAsString.endsWith("/")) {
                            ridAsString = ridAsString.substring(0, ridAsString.length() - 1);
                        }
                        statement = "DELETE FROM " + dbObject + " WHERE (" + this.database.OBJECT_ID + " > ?) AND (" + this.database.OBJECT_ID + " < ?)";                       
                        statementParameters.add(ridAsString + "/");
                        statementParameters.add(ridAsString + "0");
                    }
                    ps = this.database.prepareStatement(
                        this.conn,
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
                    this.database.executeUpdate(ps, currentStatement, statementParameters);
                    ps.close(); ps = null;
                }
            }
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", accessPath),
                new BasicException.Parameter("statement", currentStatement)
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
        }
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public MappedRecord[] sliceAndNormalizeObject(
        MappedRecord object
    ) throws ServiceException {
        DbObjectConfiguration dbObjectConfiguration = this.getConfiguration();    
        ObjectHolder_2Facade facade = null;
        MappedRecord normalizedObject = null;
        ObjectHolder_2Facade normalizedObjectFacade = null;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
            normalizedObject = ObjectHolder_2Facade.newInstance(new Path("")).getDelegate();
            normalizedObjectFacade = ObjectHolder_2Facade.newInstance(normalizedObject);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        // Add object class as attribute. This way it can be handled as a standard feature
        if(facade.getAttributeValues(SystemAttributes.OBJECT_CLASS) == null) {
            facade.attributeValues(SystemAttributes.OBJECT_CLASS).add(
                facade.getObjectClass()
            );
        }        
        int pathNormalizeLevel = dbObjectConfiguration.getPathNormalizeLevel();
        Model_1_0 model = this.getModel();
        // Add size attributes
        if(this.database.isSetSizeColumns()) {
            ModelElement_1_0 classDef = model.getElement(facade.getObjectClass());
            for(ModelElement_1_0 feature : model.getAttributeDefs(classDef, false, false).values()) {
                String featureName = (String)feature.objGetValue("name");
                String featureQualifiedName = (String)feature.objGetValue("qualifiedName");                
                if(
                    !this.database.embeddedFeatures.containsKey(featureName) &&
                    !this.database.nonPersistentFeatures.contains(featureQualifiedName)
                ) {                
                    String multiplicity = (String)feature.objGetValue("multiplicity");
                    boolean isChangeable = true;
                    // multi-valued reference?
                    if(model.isReferenceType(feature)) {
                        isChangeable = !model.referenceIsDerived(feature);
                        ModelElement_1_0 end = model.getElement(
                            feature.objGetValue("referencedEnd")
                        );
                        if(!end.objGetList("qualifierName").isEmpty()) {
                            multiplicity = Multiplicities.LIST;
                        }
                    }
                    else {
                        isChangeable = 
                            !((Boolean)feature.objGetValue("isDerived")).booleanValue() && 
                            ((Boolean)feature.objGetValue("isChangeable")).booleanValue();
                    }
                    if(
                        isChangeable &&
                        (Multiplicities.MULTI_VALUE.equals(multiplicity) ||
                        Multiplicities.LIST.equals(multiplicity) ||
                        Multiplicities.SET.equals(multiplicity) ||
                        Multiplicities.SPARSEARRAY.equals(multiplicity))                    
                    ) {
                        SparseList<?> source = facade.getAttributeValues(featureName); 
                        SparseList target = facade.attributeValues(featureName + "_");
                        target.clear();
                        target.add(
                            Integer.valueOf(
                                source == null ? 0 : source.size()
                            )
                        );
                    }
                }
            }
            // created_by, modified_by are derived and persistent
            if(model.isSubtypeOf(classDef, "org:openmdx:base:BasicObject")) {
                String featureName = SystemAttributes.CREATED_BY;
                facade.clearAttributeValues(featureName + "_").add(
                    Integer.valueOf(facade.getAttributeValues(featureName) == null ? 0 : facade.getAttributeValues(featureName).size())
                );
                featureName = SystemAttributes.MODIFIED_BY;
                facade.clearAttributeValues(featureName + "_").add(
                    Integer.valueOf(facade.getAttributeValues(featureName) == null ? 0 : facade.getAttributeValues(featureName).size())
                );
            }
        }
        // Add parent id 
        if(pathNormalizeLevel > 0) {  
            Path parentObjectPath = facade.getPath().getPrefix(facade.getPath().size()-2);    
            if(parentObjectPath.size() >= 5) {
                normalizedObjectFacade.attributeValues(this.database.privateAttributesPrefix + "parent").add(
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
                    Iterator<String> i = facade.getValue().keySet().iterator();
                    i.hasNext();
                ) {
                    String attributeName = i.next();
                    List<Object> values = facade.attributeValues(attributeName);
                    if((values.size() > 0) && (values.get(0) instanceof Path)) {
                        for(
                            Iterator<Object> j = values.iterator();
                            j.hasNext();
                        ) {
                            Object v = j.next();
                            if(!(v instanceof Path)) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.ASSERTION_FAILURE, 
                                    "value of attribute expected to be instance of path",
                                    new BasicException.Parameter("attribute", attributeName),
                                    new BasicException.Parameter("value class", (v == null ? "null" : v.getClass().getName())),
                                    new BasicException.Parameter("value", v)
                                );
                            }
                            Path objectPath = (Path)v;
                            normalizedObjectFacade.attributeValues(attributeName).add(
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
                                    normalizedObjectFacade.attributeValues(this.database.privateAttributesPrefix + attributeName + "Parent").add(
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
        int maxSize = 0;
        for(
            Iterator<String> i = facade.getValue().keySet().iterator();
            i.hasNext();
        ) {
            String attributeName = i.next();
            maxSize = java.lang.Math.max(maxSize, facade.attributeValues(attributeName).size());
        }
        // Create partitioned objects
        MappedRecord[] slices = new MappedRecord[maxSize];
        for(
            Iterator<String> i = facade.getValue().keySet().iterator();
            i.hasNext();
        ) {
            String attributeName = i.next();            
            for(
                int j = 0; 
                j < facade.attributeValues(attributeName).size();
                j++
            ) {
                if(slices[j] == null) {
                    try {
                        slices[j] = ObjectHolder_2Facade.newInstance(facade.getPath()).getDelegate();
                    } 
                    catch (ResourceException e) {
                        throw new ServiceException(e);
                    }
                }
                // Embedded features are mapped to slice 0
                if(this.database.embeddedFeatures.containsKey(attributeName)) {                    
                    try {
                        ObjectHolder_2Facade.newInstance(slices[0]).attributeValues(
                            attributeName + "_" + j
                        ).add(
                            facade.attributeValues(attributeName).get(j)
                        );
                    } 
                    catch (ResourceException e) {
                        throw new ServiceException(e);
                    }
                }
                // Map to slice with corresponding index
                else {               
                    ObjectHolder_2Facade sliceFacade = null;
                    try {
                        sliceFacade = ObjectHolder_2Facade.newInstance(slices[j]);
                    } 
                    catch (ResourceException e) {
                        throw new ServiceException(e);
                    }
                    sliceFacade.attributeValues("objectIdx").add(
                        Integer.valueOf(j)
                    );
                    sliceFacade.attributeValues(attributeName).add(
                        facade.attributeValues(attributeName).get(j)
                    );
                }
            }
        }
        return slices;
    }

    //---------------------------------------------------------------------------
    @Override
    protected String toObjectIdQuery (
        Path path
    ) throws ServiceException {
        Object rid = this.database.getReferenceId(this.conn, path, true);
        String pathComponentQuery = path.getBase();
        return pathComponentQuery.startsWith(":") && pathComponentQuery.endsWith("*") ? 
            rid + "/" + pathComponentQuery.substring(1, pathComponentQuery.length() - 1) + '%' : 
            rid + "/" + pathComponentQuery;
    }

    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 4076702439130733210L;

}

//--- End of File -----------------------------------------------------------
