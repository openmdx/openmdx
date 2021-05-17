/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Standard DB Object
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.FastResultSet;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

@SuppressWarnings({"rawtypes","unchecked"})
public abstract class StandardDbObject extends DbObject {

    //-------------------------------------------------------------------------
    protected StandardDbObject(
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

        // index column
        this.indexColumn = this.database.getObjectIdxColumnName();

        // parse objectId into its components
        this.objectIdValues = new ArrayList();
        DbObjectConfiguration typeConfiguration = this.getConfiguration();
        if(
            (typeConfiguration != null) && 
            (typeConfiguration.getObjectIdPattern() != null) &&
            (this.getObjectId() != null)
        ) {
            Matcher matcher = typeConfiguration.getObjectIdPatternMatcher().matcher(this.getObjectId());
            if(matcher.matches()) {
                for(int i = 1; i <= matcher.groupCount(); i++) {
                    this.objectIdValues.add(matcher.group(i));
                }
            }
            else {
                this.objectIdValues.add(
                    this.database.getObjectId(
                        this.getObjectId()
                    )
                );
            }
        } 
        else {
            this.objectIdValues.add(
                this.database.getObjectId(
                    this.getObjectId()
                 )
             );
        }

        // object selectors
        this.objectIdColumn = new ArrayList();
        if((this.dbObjectConfiguration == null) || (this.dbObjectConfiguration.getObjectIdComponents() == 0)) {
            this.objectIdColumn.add(this.database.getObjectOidColumnName());        
        }
        else {
            for(int i = 0; i < this.dbObjectConfiguration.getObjectIdComponents(); i++) {
                this.objectIdColumn.add(
                    this.database.getPrivateAttributesPrefix() + this.database.getObjectOidColumnName() + "$" + i
                );
            }
        }

        // objectQualifierClause
        String objectIdClause = "";
        for(int i = 0; i < this.getObjectIdColumn().size(); i++) {
            objectIdClause += i == 0 ? "" : " AND ";
            String objectIdValue = i < this.getObjectIdValues().size() ?
                this.getObjectIdValues().get(i) :
                    null;
            if(objectIdValue != null && objectIdValue.indexOf("%") >= 0) {
                objectIdClause += "(v." + this.getObjectIdColumn().get(i) + " LIKE ?)";
            }
            else {
                objectIdClause += "(v." + this.getObjectIdColumn().get(i) + " = ?)";                
            }
        } 
        this.objectIdClause = objectIdClause;
        this.referenceClause = this.createReferenceClause(this.getReferenceColumn(), this.getReferenceValues(), this.isExtent());
    }

    //-------------------------------------------------------------------------
    protected StandardDbObject(
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

    //---------------------------------------------------------------------------  
    @Override
    public String getReferenceClause(
    ) throws ServiceException {
        return this.referenceClause;
    }

    //---------------------------------------------------------------------------  
    @Override
    public String getObjectIdClause(
    ) throws ServiceException {
        return this.objectIdClause;
    }

    //---------------------------------------------------------------------------
    @Override
    public List<String> getObjectIdValues(
    ) throws ServiceException {
        return this.objectIdValues;
    }

    //-------------------------------------------------------------------------
    @Override
    public String getIndexColumn(
    ) {
        return this.indexColumn;
    }

    /**
     * The method updates reference column and reference value collections
     * 
     * @param referenceColumn 
     * @param referenceValues 
     * @param extentQuery 
     * @return the reference clause
     * 
     * @throws ServiceException 
     */
    protected String createReferenceClause(
        List<String> referenceColumn, 
        List<Object> referenceValues, 
        boolean extentQuery
    ) throws ServiceException{
        referenceColumn.add(this.database.getObjectRidColumnName());
        String selectReferenceIdsStatement;
        if(extentQuery) {
            selectReferenceIdsStatement = this.database.getSelectReferenceIdsClause(
                conn,
                this.getReference(),
                referenceValues
            );
        } else {
            referenceValues.add(
                this.database.getReferenceId(
                    conn, 
                    this.getReference(),
                    !isQuery() || (this.getConfiguration().getDbObjectForQuery1() != null)
                )                    
            );
            selectReferenceIdsStatement = "= ?";
        }
        return "(v." + this.database.getObjectRidColumnName() + " " + selectReferenceIdsStatement + ")";
    }

    //-------------------------------------------------------------------------
    @Override
    public void remove(
    ) throws ServiceException {
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
            // Object (only if dbObject (=table) is configured)
            if(
                    ((type.size() == 1) || // catch all type
                            (type.size() == accessPath.size() && accessPath.isLike(type))) &&
                            (this.getConfiguration().getDbObjectForUpdate1().length() > 0)
            ) {
                statementParameters = new ArrayList<Object>();
                List<String> referenceIdColumns = new ArrayList<String>();
                for(
                    Iterator i = dbObjects.iterator();
                    i.hasNext();
                ) {
                    String dbObject = (String)i.next();
                    StringBuilder statement = new StringBuilder("DELETE FROM ").append(dbObject).append(" v WHERE ");
                    statementParameters.clear();
                    referenceIdColumns.clear();
                    statement.append(this.createReferenceClause(referenceIdColumns, statementParameters, true)).append(" AND ");
                    statement.append(this.getObjectIdClause());
                    statementParameters.addAll(this.getObjectIdValues());
                    try (
                        PreparedStatement ps = this.database.prepareStatement(
                            conn,
                            currentStatement = statement.toString()
                        )
                    ){
                        for(int j = 0; j < statementParameters.size(); j++) {
                            this.database.setPreparedStatementValue(
                                this.conn,
                                ps, 
                                j+1, 
                                statementParameters.get(j)
                            );
                        }
                        this.database.executeUpdate(ps, currentStatement, statementParameters);
                    }
                }
            }

            if(this.database.getDatabaseConfiguration().cascadeDeletes()) {
                // Composite objects (only if dbObject (=table) is configured)
                if(
                        ((type.size() == 1) || // catch all type
                                ((type.size() > accessPath.size()) && accessPath.isLike(type.getPrefix(accessPath.size())))) &&
                                (this.getConfiguration().getDbObjectForUpdate1() != null) &&
                                (this.getConfiguration().getDbObjectForUpdate1().length() > 0)        
                ) {
                    statementParameters = new ArrayList();
                    String selectReferenceIdsClause = this.database.getSelectReferenceIdsClause(
                        conn, 
                        accessPath.getDescendant(type.getSuffix(accessPath.size())),
                        statementParameters
                    );
                    for(
                            Iterator i = dbObjects.iterator();
                            i.hasNext();
                    ) {
                        String dbObject = (String)i.next();
                        String statement =
                            "DELETE FROM " + dbObject + 
                            " WHERE " + this.database.getObjectRidColumnName() + " " + selectReferenceIdsClause;
                        try(
                            PreparedStatement ps = this.database.prepareStatement(
                                conn,
                                currentStatement = statement
                            )
                        ){
                            for(int j = 0; j < statementParameters.size(); j++) {
                                this.database.setPreparedStatementValue(
                                    this.conn,
                                    ps, 
                                    j+1, 
                                    statementParameters.get(j)
                                );
                            }
                            this.database.executeUpdate(ps, currentStatement, statementParameters);
                        }
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
                new BasicException.Parameter(BasicException.Parameter.XRI, accessPath),
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
        }
    }

    //---------------------------------------------------------------------------  
    @Override
    public Path getObjectReference(
        FastResultSet frs
    ) throws SQLException, ServiceException {

        Object rid = frs.getObject(this.database.getObjectRidColumnName());
        if(rid == null) {
            throw new SQLException(
                "column " + 
                this.database.getObjectRidColumnName() + 
                " in result set not found"
            );
        }
        // Map rid to reference
        else {
            // If rid is undefined or set to 0 map to this.reference.
            // E.g. rid is undefined in case of format slicedParentRidOnly
            if((rid == null) || ((rid instanceof Number) && (((Number)rid).longValue() == 0))) {
                return this.getReference();
            }
            else {
                return this.database.getReference(
                    conn,
                    rid
                );
            }
        }
    }

    //---------------------------------------------------------------------------  
    @Override
    public String getObjectId(
        FastResultSet frs
    ) throws SQLException, ServiceException {
        String objectId = null;
        try {
            objectId = frs.getObject(this.database.getObjectOidColumnName()).toString();
        }
        catch(NullPointerException e) {
            throw new SQLException(
                "column " + 
                this.database.getObjectOidColumnName() + 
                " in result set not found"
            );
        }
        return objectId;
    }

    //---------------------------------------------------------------------------  
    @Override
    public int getIndex(
        FastResultSet frs
    ) throws SQLException {
        final String objectIdxColumnName = this.database.getObjectIdxColumnName();
        try {
            return ((Number)frs.getObject(objectIdxColumnName)).intValue();
        } catch(NullPointerException e) {
            throw new SQLException(
                "column " + objectIdxColumnName + " in result set not found"
            );
        }    
    }

    //---------------------------------------------------------------------------  
    @Override
    public boolean includeColumn(
        String columnName
    ) {
        return 
        !this.database.getObjectRidColumnName().equalsIgnoreCase(columnName) &&
        !this.database.getObjectOidColumnName().equalsIgnoreCase(columnName) &&
        !this.database.getObjectIdxColumnName().equalsIgnoreCase(columnName) &&
        !columnName.toLowerCase().startsWith(this.database.getPrivateAttributesPrefix()) &&
        !columnName.endsWith("_");
    }

    //---------------------------------------------------------------------------  
    @Override
    public String getTableName(
    ) {
        return this.dbObjectConfiguration.getDbObjectForUpdate1();
    }

    //---------------------------------------------------------------------------  
    @Override
    public String[] getJoinCriteria(
    ) {
        return this.dbObjectConfiguration.getJoinCriteria();
    }

    //---------------------------------------------------------------------------
    @Override
    public String getHint(
    ) throws ServiceException {
        return this.dbObjectConfiguration == null
        ? ""
            : this.dbObjectConfiguration.getDbObjectHint() == null
            ? ""
                : this.dbObjectConfiguration.getDbObjectHint();
    }

    //---------------------------------------------------------------------------
    protected Path toPath(
        Object filterValue
    ) {
        return filterValue instanceof Path ? 
            (Path) filterValue :
                new Path(filterValue.toString());
    }

    //---------------------------------------------------------------------------
    protected String toObjectIdQuery (
        Path path
    ) throws ServiceException {
        String pathComponentQuery = path.getLastSegment().toClassicRepresentation();
        return pathComponentQuery.startsWith(":") && pathComponentQuery.endsWith("*") 
        ? pathComponentQuery.substring(1, pathComponentQuery.length() - 1) + '%' 
            : pathComponentQuery;
    }

    //---------------------------------------------------------------------------
    @Override
    public FilterProperty mapToIdentityFilterProperty(
        FilterProperty p
    ) throws ServiceException {
        Object[] identityValues = p.getValues();
        String[] filterValues = new String[identityValues.length];
        for(
                int j = 0;
                j < identityValues.length;
                j++
        ) {
            Path filterPath = this.toPath(identityValues[j]);
            boolean referencePathMatches =
                this.getReference().isLike(filterPath.getParent()); 
            filterValues[j] = referencePathMatches ?
                this.toObjectIdQuery(filterPath) :
                    (filterPath.compareTo(this.getReference()) < 0 ? IDENTITY_UNDERFLOW : IDENTITY_OVERFLOW);
        }
        return
        new FilterProperty(
            p.quantor(),
            this.database.getFeatureName(
                this.getObjectIdColumn().get(0)
            ),
            p.operator(),
            (Object[])filterValues
        );
    }

    //---------------------------------------------------------------------------
    /**
     * Works fine provided that no object id is less than this value...
     */  
    protected static final String IDENTITY_UNDERFLOW = "        ;underflow";

    /**
     * Works fine provided that no object id is greater than this value...
     */  
    private static final String IDENTITY_OVERFLOW = "~~~~~~~~;overflow";

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 5890153090106609559L;

}

//---End of File -------------------------------------------------------------
