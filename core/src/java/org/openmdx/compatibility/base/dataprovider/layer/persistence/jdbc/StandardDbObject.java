/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StandardDbObject.java,v 1.51 2008/12/01 10:50:15 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.51 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/01 10:50:15 $
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.pattern.cci.Matcher_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

@SuppressWarnings("unchecked")
public abstract class StandardDbObject 
extends DbObject {

    //-------------------------------------------------------------------------
    protected StandardDbObject(
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
            isExtent
        );

        // reference selectors
        this.referenceColumn = new ArrayList();
        this.referenceColumn.add(this.database.OBJECT_RID);

        // index column
        this.indexColumn = this.database.OBJECT_IDX;

        // parse objectId into its components
        this.objectIdValues = new ArrayList();
        DbObjectConfiguration typeConfiguration = this.getConfiguration();
        if(
                (typeConfiguration != null) && 
                (typeConfiguration.getObjectIdPattern() != null) &&
                (this.getObjectId() != null)
        ) {
            Matcher_1_0 matcher = typeConfiguration.getObjectIdPatternMatcher().matcher(this.getObjectId());
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
        } else {
            this.objectIdValues.add(
                this.database.getObjectId(
                    this.getObjectId()
                 )
             );
        }

        // object selectors
        this.objectIdColumn = new ArrayList();
        if((this.dbObjectConfiguration == null) || (this.dbObjectConfiguration.getObjectIdComponents() == 0)) {
            this.objectIdColumn.add(this.database.OBJECT_OID);        
        }
        else {
            for(int i = 0; i < this.dbObjectConfiguration.getObjectIdComponents(); i++) {
                this.objectIdColumn.add(
                    this.database.privateAttributesPrefix + this.database.OBJECT_OID + "$" + i
                );
            }
        }

        // objectQualifierClause
        String objectIdClause = "";
        for(int i = 0; i < this.getObjectIdColumn().size(); i++) {
            objectIdClause += i == 0 ? "" : " AND ";
            objectIdClause += "(v." + this.getObjectIdColumn().get(i) + " = ?)";
        } 
        this.objectIdClause = objectIdClause;

        // referenceClause
        String selectReferenceIdsStatement;
        this.referenceValues = new ArrayList();
        if(isExtent) {
            selectReferenceIdsStatement = this.database.getSelectReferenceIdsClause(
                conn,
                this.getReference(),
                this.referenceValues
            );
        }
        else {
            this.referenceValues.add(
                this.database.getReferenceId(
                    conn, 
                    this.getReference(),
                    !isQuery || (typeConfiguration.getDbObjectForQuery1() != null)
                )                    
            );
            selectReferenceIdsStatement = "= ?";
        }
        this.referenceClause = "(v." + this.database.OBJECT_RID + " " + selectReferenceIdsStatement + ")";
    }

    //-------------------------------------------------------------------------
    protected StandardDbObject(
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

    //---------------------------------------------------------------------------  
    public String getReferenceClause(
    ) throws ServiceException {
        return this.referenceClause;
    }

    //---------------------------------------------------------------------------  
    public List<Object> getReferenceValues(
    ) {
        return this.referenceValues;
    }

    //---------------------------------------------------------------------------  
    public String getObjectIdClause(
    ) throws ServiceException {
        return this.objectIdClause;
    }

    //---------------------------------------------------------------------------
    public List<String> getObjectIdValues(
    ) throws ServiceException {
        return this.objectIdValues;
    }

    //-------------------------------------------------------------------------
    public String getIndexColumn(
    ) {
        return this.indexColumn;
    }

    //-------------------------------------------------------------------------
    public void remove(
    ) throws ServiceException {

        PreparedStatement ps = null;
        String currentStatement = null;
        Path accessPath = this.getResourceIdentifier();
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

            // Object (only if dbObject (=table) is configured)
            if(
                    ((type.size() == 1) || // catch all type
                            (type.size() == accessPath.size() && accessPath.isLike(type))) &&
                            (this.getConfiguration().getDbObjectForUpdate1().length() > 0)
            ) {
                List statementParameters = new ArrayList();
                String selectReferenceIdsClause = this.database.getSelectReferenceIdsClause(
                    conn, 
                    this.getReference(), 
                    statementParameters
                );
                statementParameters.add(
                    this.database.getObjectId(
                        this.getObjectId()
                    )
                );
                for(
                        Iterator i = dbObjects.iterator();
                        i.hasNext();
                ) {
                    String dbObject = (String)i.next();
                    String statement =
                        "DELETE FROM " + dbObject + 
                        " WHERE " + this.database.OBJECT_RID + " " + selectReferenceIdsClause + " AND " + this.database.OBJECT_OID + " IN (?)";
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
                    ps.close(); ps = null;
                }
            }

            // Composite objects (only if dbObject (=table) is configured)
            if(
                    ((type.size() == 1) || // catch all type
                            ((type.size() > accessPath.size()) && accessPath.isLike(type.getPrefix(accessPath.size())))) &&
                            (this.getConfiguration().getDbObjectForUpdate1() != null) &&
                            (this.getConfiguration().getDbObjectForUpdate1().length() > 0)        
            ) {
                List statementParameters = new ArrayList();
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
                        " WHERE " + this.database.OBJECT_RID + " " + selectReferenceIdsClause;
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
    public Path getObjectReference(
        FastResultSet frs
    ) throws SQLException, ServiceException {

        Object rid = frs.getObject(this.database.OBJECT_RID);
        if(rid == null) {
            throw new SQLException(
                "column " + 
                this.database.OBJECT_RID + 
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
    public String getObjectId(
        FastResultSet frs
    ) throws SQLException, ServiceException {
        String objectId = null;
        try {
            objectId = frs.getObject(this.database.OBJECT_OID).toString();
        }
        catch(NullPointerException e) {
            throw new SQLException(
                "column " + 
                this.database.OBJECT_OID + 
                " in result set not found"
            );
        }
        return objectId;
    }

    //---------------------------------------------------------------------------  
    public int getIndex(
        FastResultSet frs
    ) throws SQLException {
        int idx = 0;
        try {
            idx = ((Number)frs.getObject(this.database.OBJECT_IDX)).intValue();
        }
        catch(NullPointerException e) {
            throw new SQLException(
                "column " + 
                this.database.OBJECT_IDX + 
                " in result set not found"
            );
        }    
        return idx;
    }

    //---------------------------------------------------------------------------  
    public boolean includeColumn(
        String columnName
    ) {
        return 
        !this.database.OBJECT_RID.equalsIgnoreCase(columnName) &&
        !this.database.OBJECT_OID.equalsIgnoreCase(columnName) &&
        !this.database.OBJECT_IDX.equalsIgnoreCase(columnName) &&
        !columnName.toLowerCase().startsWith(this.database.privateAttributesPrefix) &&
        !columnName.endsWith("_");
    }

    //---------------------------------------------------------------------------  
    public String getTableName(
    ) {
        return this.dbObjectConfiguration.getDbObjectForUpdate1();
    }

    //---------------------------------------------------------------------------  
    public String[] getJoinCriteria(
    ) {
        return this.dbObjectConfiguration.getJoinCriteria();
    }

    //---------------------------------------------------------------------------
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
        String pathComponentQuery = path.getBase();
        return pathComponentQuery.startsWith(":") && pathComponentQuery.endsWith("*") 
        ? pathComponentQuery.substring(1, pathComponentQuery.length() - 1) + '%' 
            : pathComponentQuery;
    }

    //---------------------------------------------------------------------------
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
            this.database.getAttributeName(
                (String)this.getObjectIdColumn().get(0)
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

}

//---End of File -------------------------------------------------------------
