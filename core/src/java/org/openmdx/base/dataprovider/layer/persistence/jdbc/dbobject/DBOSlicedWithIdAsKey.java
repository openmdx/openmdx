/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: SlicedDbObjectParentRidOnly class
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.FastResultSet;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.SparseArray;

/**
 * Rows of this type do not contain the column object_rid. Instead, the rows
 * contain the columns parent_object__rid and parent_object__oid. This db 
 * object type is used when the calculation of object_rid is expensive
 * compared to the calculation of the object_parent columns.
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class DBOSlicedWithIdAsKey
extends SlicedDbObject 
{

    //-------------------------------------------------------------------------
    public DBOSlicedWithIdAsKey(
        Database_1_0 database,
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
        Database_1_0 database,
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
        this.objectIdClause = "(v." + database.getObjectOidColumnName() + " = ?)";
        this.objectIdColumn.clear();
        this.objectIdColumn.add(database.getObjectOidColumnName());
        if(isExtent) {
            this.getReferenceValues().clear();
            String base = accessPath.getLastSegment().toClassicRepresentation();
            this.getReferenceValues().add(
                base.equals("%") || base.equals(":*") ? 
                    rid + "/%" : 
                        rid
            );
            this.referenceClause = "(v." + database.getObjectOidColumnName() + " LIKE ? " + this.database.getEscapeClause(conn) + ")";
        }
        else {
            this.getReferenceValues().clear();
            if(this.getJoinCriteria() == null) {
                String databaseProductName = getDatabaseProductName(conn);
                String useLikeForOidMatching = System.getProperty(
                    "org.openmdx.persistence.jdbc.useLikeForOidMatching",
                    Boolean.toString(databaseProductName.startsWith("PostgreSQL"))
                );
                // Use like for oid matching (e.g. required for PostgreSQL databases with non-C locale)
                // Otherwise use the more efficient <> operators
                if(Boolean.valueOf(useLikeForOidMatching).booleanValue()) {
                    this.getReferenceValues().clear();
                    this.getReferenceValues().add(rid + "/%");
                    this.referenceClause = "(v." + database.getObjectOidColumnName() + " LIKE ?)";                     
                }
                else {
                    // By default do not use LIKE because
                    // <ul>
                    //   <li>LIKE is case insensitive
                    //   <li>Does not perform well for all databases
                    // </ul>
                    // rid is a LIKE pattern. Remove %
                    if(rid.endsWith("/%")) {           
                        this.getReferenceValues().add(rid.substring(0, rid.length()-1));
                        this.getReferenceValues().add(rid.substring(0, rid.length()-2) + "0");
                    }
                    else {
                        this.getReferenceValues().add(rid + "/");
                        this.getReferenceValues().add(rid + "0");            
                    }
                    this.referenceClause = "((v." + database.getObjectOidColumnName() + " > ?) AND (v." + database.getObjectOidColumnName() + " < ?))";
                }
            }
            else {
                this.getReferenceValues().add(
                    this.database.getObjectId(
                        conn,
                        this.getReference().getParent()
                    )
                );
                // Default join with composite parent. Otherwise use configured join column
                this.referenceClause = "(vj." + this.database.getDatabaseSpecificColumnName(conn, this.getJoinCriteria()[1], false) + " = ?)";            
            }
        }
        this.getReferenceColumn().clear();
        // non index column for non-indexed sliced DB objects
        this.indexColumn = null;
        this.excludeAttributes.add("objectIdx");        
    }

    /**
     * Determine the database product name
     * 
     * @param connection the database connection
     * 
     * @return the database product name, or <code>N/A</code> in case of exception
     */
    private String getDatabaseProductName(Connection connection) {
        try {
            return connection.getMetaData().getDatabaseProductName();
        } catch(Exception e) {
            return "N/A";
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObject#getObjectReference(org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.FastResultSet)
     */
    @Override
    public Path getObjectReference(
        FastResultSet frs
    ) throws SQLException, ServiceException {      
        if(this.database.getDatabaseConfiguration().normalizeObjectIds()) {
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

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObject#getObjectId(org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.FastResultSet)
     */
    @Override
    public String getObjectId(
        FastResultSet frs
    ) throws SQLException, ServiceException {
        if(this.database.getDatabaseConfiguration().normalizeObjectIds()) {
            return getResourceIdentifier().getLastSegment().toClassicRepresentation();
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

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.DbObject#getResourceIdentifier(org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.FastResultSet)
     */
    @Override
    public Path getResourceIdentifier(
        FastResultSet frs
    ) throws SQLException, ServiceException {
        if(this.database.getDatabaseConfiguration().normalizeObjectIds()) {
            String objectId = (String)frs.getObject("object_id");
            if(objectId == null) {
                throw new SQLException(
                    "column object_id in result set not found"
                );
            }
            return this.database.getDatabaseConfiguration().buildResourceIdentifier(objectId, false);
        } else {
            return super.getResourceIdentifier(frs);
        }        
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.StandardDbObject#includeColumn(java.lang.String)
     */
    @Override
    public boolean includeColumn(
        String columnName
    ) {
        return 
        !"object_id".equalsIgnoreCase(columnName) &&
        !this.database.getObjectIdxColumnName().equalsIgnoreCase(columnName) &&
        !columnName.toLowerCase().startsWith(this.database.getPrivateAttributesPrefix()) &&
        !columnName.endsWith(this.database.getSizeSuffix());
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObject#getIndex(org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.FastResultSet)
     */
    @Override
    public int getIndex(
        FastResultSet frs
    ) throws SQLException {
        if(frs.getColumnNames().contains(this.database.getObjectIdxColumnName())) {
            return ((Number)frs.getObject(this.database.getObjectIdxColumnName())).intValue();
        }
        else {
            return 0;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.StandardDbObject#remove()
     */
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
        List<Object> statementParameters = null;
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
                    statementParameters = new ArrayList<Object>();
                    statementParameters.addAll(
                        this.getObjectIdValues()
                    );
                    String statement =
                        "DELETE FROM " + dbObject + " WHERE " + this.database.getObjectOidColumnName() + " IN (?)";
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
                    statementParameters = new ArrayList<Object>();
                    Object rid = this.database.getReferenceId(
                        this.conn,
                        accessPath.getDescendant(type.getSuffix(accessPath.size())),
                        false
                    );
                    String statement = null;
                    String databaseProductName = getDatabaseProductName(conn);
                    String useLikeForOidMatching = System.getProperty(
                        "org.openmdx.persistence.jdbc.useLikeForOidMatching",
                        Boolean.toString(databaseProductName.startsWith("PostgreSQL"))
                    );
                    String ridAsString = rid.toString();
                    if(Boolean.parseBoolean(useLikeForOidMatching)) {    		
                        statement = "DELETE FROM " + dbObject + " WHERE " + this.database.getObjectOidColumnName() + " LIKE ?"; 
                        statementParameters.add(
                            ridAsString.endsWith("/%") 
                                ? ridAsString 
                                : ridAsString.endsWith("/")
                                    ? ridAsString + "%"
                                    : ridAsString + "/%"
                        );
                    } else {
                        if(ridAsString.endsWith("/%")) {
                            ridAsString = ridAsString.substring(0, ridAsString.length() - 2);
                        } else if(ridAsString.endsWith("/")) {
                            ridAsString = ridAsString.substring(0, ridAsString.length() - 1);
                        }
                        statement = "DELETE FROM " + dbObject + " WHERE (" + this.database.getObjectOidColumnName() + " > ?) AND (" + this.database.getObjectOidColumnName() + " < ?)";                       
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
        } catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", accessPath),
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
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.SlicedDbObject#sliceAndNormalizeObject(org.openmdx.base.rest.cci.ObjectRecord, boolean)
     */
    @Override
    public ObjectRecord[] sliceAndNormalizeObject(
        ObjectRecord object, boolean discardValuesProvidedByView
    ) throws ServiceException {
        DbObjectConfiguration dbObjectConfiguration = this.getConfiguration();    
        Object_2Facade facade = Facades.asObject(object);
        Object_2Facade normalizedObjectFacade = Facades.newObject(new Path(""),
		    facade.getObjectClass()
		);
        // Add object class as attribute. This way it can be handled as a standard feature
        if(facade.getAttributeValues(SystemAttributes.OBJECT_CLASS) == null) {
            facade.addToAttributeValuesAsList(
                SystemAttributes.OBJECT_CLASS,
                facade.getObjectClass()
            );
        }        
        int pathNormalizeLevel = dbObjectConfiguration.getPathNormalizeLevel();
        Model_1_0 model = this.getModel();
        // Add size attributes
        if(this.database.isSetSizeColumns()) {
            ModelElement_1_0 classDef = model.getElement(facade.getObjectClass());
            for(ModelElement_1_0 feature : model.getAttributeDefs(classDef, false, false).values()) {
                String featureName = feature.getName();
                if(
                    !this.database.isEmbeddedFeature(featureName) &&
                    this.database.isPersistent(feature) &&
                    ModelHelper.isChangeable(feature) &&
                    ModelHelper.getMultiplicity(feature).isMultiValued()
                ){
                    facade.replaceAttributeValuesAsListBySingleton(
                        featureName + this.database.getSizeSuffix(),
                    	Integer.valueOf(
                			facade.getAttributeValues(featureName) == null ? 0 : facade.getSizeOfAttributeValuesAsList(featureName)
                    	)
                    );
                }
            }
            // created_by, modified_by are derived and persistent
            if(model.isSubtypeOf(classDef, "org:openmdx:base:BasicObject")) {
                String featureName = SystemAttributes.CREATED_BY;
                Object values = facade.getAttributeValues(featureName);
                facade.replaceAttributeValuesAsListBySingleton(
                    featureName + this.database.getSizeSuffix(),
                    Integer.valueOf(
                        values instanceof SparseArray ? ((SparseArray)values).size() :
                        values instanceof List ? ((List)values).size() :
                        0
                    )
                );
                featureName = SystemAttributes.MODIFIED_BY;
                values = facade.getAttributeValues(featureName);
                facade.replaceAttributeValuesAsListBySingleton(
                    featureName + this.database.getSizeSuffix(),
                    Integer.valueOf(
                        values instanceof SparseArray ? ((SparseArray)values).size() :
                        values instanceof List ? ((List)values).size() :
                        0
                    )
                );
            }
        }
        // Add parent id 
        if(pathNormalizeLevel > 0) {  
            Path parentObjectPath = facade.getPath().getPrefix(facade.getPath().size()-2);    
            if(parentObjectPath.size() >= 5) {
                normalizedObjectFacade.addToAttributeValuesAsList(
                    this.database.getPrivateAttributesPrefix() + "parent",
                    this.database.getReferenceId(
                        conn, 
                        parentObjectPath, 
                        true 
                    ) + "/" + parentObjectPath.getLastSegment().toClassicRepresentation()
                );
            }
            // Add id for all attributes with values of type path
            if(pathNormalizeLevel > 1) {  
                Set<String> attributeNames = facade.getValue().keySet();
                for(String attributeName: attributeNames) {
                    List<Object> values = facade.getAttributeValuesAsReadOnlyList(attributeName);
                    int posNull = values.indexOf(null);
                    if(posNull >= 0) {
                        for(int i = posNull + 1; i < values.size(); i++) {
                            if(values.get(i) != null) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.ASSERTION_FAILURE, 
                                    "Collection type attribute contains nulls",
                                    new BasicException.Parameter("attribute", attributeName),
                                    new BasicException.Parameter("value class", values.getClass().getName()),
                                    new BasicException.Parameter("value", values)
                                );
                            }
                        }
                    }
                    if(!values.isEmpty() && (values.get(0) instanceof Path)) {
                        for(Object v: values) {
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
                            normalizedObjectFacade.addToAttributeValuesAsList(
                                attributeName,
                                this.database.getReferenceId(
                                    conn, 
                                    objectPath, 
                                    true
                                ) + "/" + objectPath.getLastSegment().toClassicRepresentation()
                            );
                            // Add parent id of path value
                            if(pathNormalizeLevel > 2) {
                                Path parentPath = objectPath.getPrefix(objectPath.size()-2);
                                if(parentPath.size() >= 5) {
                                    normalizedObjectFacade.addToAttributeValuesAsList(
                                        this.database.getPrivateAttributesPrefix() + attributeName + "Parent",
                                        this.database.getReferenceId(
                                            conn, 
                                            parentPath, 
                                            true
                                        ) + "/" + parentPath.getLastSegment().toClassicRepresentation()
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
            maxSize = java.lang.Math.max(maxSize, facade.getSizeOfAttributeValuesAsList(attributeName));
        }
        // Create partitioned objects
        ObjectRecord[] slices = new ObjectRecord[maxSize];
        for(
            Iterator<String> i = facade.getValue().keySet().iterator();
            i.hasNext();
        ) {
            String attributeName = i.next();            
            for(
                int j = 0; 
                j < facade.getSizeOfAttributeValuesAsList(attributeName);
                j++
            ) {
                if(slices[j] == null) {
                    slices[j] = Facades.newObject(
					    facade.getPath(),
					    facade.getObjectClass()
					).getDelegate();
                }
                // Embedded features are mapped to slice 0
                if(this.database.isEmbeddedFeature(attributeName)) {                    
                    Facades.asObject(slices[0]).addToAttributeValuesAsList(
                        attributeName + this.database.getSizeSuffix() + j,
                        facade.getAttributeValueFromList(attributeName,j)
                    );
                }
                // Map to slice with corresponding index
                else {               
                    MappedRecord target = slices[j].getValue();
                    target.put(
                        "objectIdx",
                        Integer.valueOf(j)
                    );
                    target.put(
                        attributeName,
                        facade.getAttributeValueFromList(attributeName,j)
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
        String pathComponentQuery = path.getLastSegment().toClassicRepresentation();
        return pathComponentQuery.startsWith(":") && pathComponentQuery.endsWith("*") ? 
            rid + "/" + pathComponentQuery.substring(1, pathComponentQuery.length() - 1) + '%' : 
            rid + "/" + pathComponentQuery;
    }

    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 4076702439130733210L;

}

//--- End of File -----------------------------------------------------------
