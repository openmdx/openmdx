/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Database plug-in supporting RID/OID queries
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011-2014, OMEX AG, Switzerland
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DbObject;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.exception.BasicException;

/**
 * Database plug-in supporting RID/OID queries
 */
public class RidOidQueryDatabase_2 extends Database_2 {

    /**
     * Get rid of view prefix
     * 
     * @param joinColumns the join columns
     * 
     * @return the unqualified column name
     */
    private String getUnqualifiedObjectIdColumn(
        List<String> joinColumns
    ){
        String objectIdColumn = joinColumns.get(joinColumns.size() - 1);
        return objectIdColumn.lastIndexOf('.') < 0 ? objectIdColumn : objectIdColumn.substring(objectIdColumn.lastIndexOf('.') + 1);
    }
        
    /* (non-Javadoc)
	 * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#toMultiValueView(java.lang.String)
	 */
	@Override
	protected String toMultiValueView(String singleValueView) {
		return singleValueView;
	}

	/* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.Database_1#isAspectBaseClass(java.lang.String)
     */
    @Override
    protected boolean isAspectBaseClass(String qualifiedClassName) {
        return this.enableAspectFilterSubstitution && super.isAspectBaseClass(qualifiedClassName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#isBaseClass(java.lang.String)
     */
    @Override
    protected boolean isBaseClass(String qualifiedClassName) {
        return super.isBaseClass(qualifiedClassName) || (
            !this.enableAspectFilterSubstitution && super.isAspectBaseClass(qualifiedClassName)
        );
    }

    /**
     * Tells whether RID and OID shall be used for the corresponding filter property
     * 
     * @param filterPropertyDef
     * 
     * @return <code>true</code> if RID and OID shall be used 
     * @throws ServiceException  
     * 
     * @throws ServiceException 
     */
    private boolean useRidAndOid(
        ModelElement_1_0 filterPropertyDef
    ) throws ServiceException {
        return
            filterPropertyDef != null &&
            this.isUseNormalizedReferences() &&
            this.getModel().isReferenceType(filterPropertyDef) &&
            this.getModel().referenceIsStoredAsAttribute(filterPropertyDef);
    }

    /**
     * Provide the reference RID
     * 
     * @param columnName
     * 
     * @return the reference RID
     */
    protected String toRid(
        String tableAlias,
        String columnName
    ) {
        return (tableAlias == null ? "" : tableAlias + ".") + toRid(this.getPrivateAttributesPrefix() + columnName + "_");
    }

    /**
     * Provide the reference RID
     * 
     * @param columnName
     * 
     * @return the reference RID
     */
    protected String toOid(
        String tableAlias,
        String columnName
    ) {
        return (tableAlias == null ? "" : tableAlias + ".") + toOid(this.getPrivateAttributesPrefix() + columnName + "_");
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#isInToSqlClause(java.lang.String, org.openmdx.base.mof.cci.ModelElement_1_0, java.lang.StringBuilder, java.util.Collection, java.lang.Object[])
     */
    @Override
    protected void isInToSqlClause(
        Connection connection,
        DbObject dbObject,
        String column,
        ModelElement_1_0 filterPropertyDef,
        StringBuilder clause, 
        List<Object> clauseValues, 
        Object[] values
    ) throws ServiceException {
        if(useRidAndOid(filterPropertyDef)) {
            clause.append("(");
            int dot = column.indexOf('.');
            String tableAlias = column.substring(0, dot);
            String columnName = column.substring(dot + 1);
            String oidColumn = toOid(tableAlias, columnName);
            String operator = "";
            for(Object value : values) {
                clause.append(operator).append("(");
                Path uri = new Path(this.externalizePathValue(connection,(Path)value));
                String ridValue = uri.getParent().toClassicRepresentation();
                if("org:openmdx:base:Aspect:core".equals(filterPropertyDef.getQualifiedName())) {
                    clause.append(tableAlias).append(".").append(this.OBJECT_RID).append(" = ?");
                    clauseValues.add(ridValue);
                } else {
                    Map<String, Pattern> referenceIdPatterns = dbObject.getConfiguration().getReferenceIdPattern();
                    Pattern referenceIdPattern = referenceIdPatterns == null ? null : referenceIdPatterns.get(filterPropertyDef.getName()); 
                    if(referenceIdPattern == null) {
                        clause.append(toRid(tableAlias, columnName)).append(" = ?");
                        clauseValues.add(ridValue);
                    } else {
                        Matcher matcher = referenceIdPattern.matcher(ridValue);
                        if(matcher.matches()) {
                            int groups = matcher.groupCount();
                            for(
                                int group = 1; 
                                group <= groups; 
                                group++
                            ){
                                if(group > 1) clause.append(" AND ");
                                clause.append(toRid(tableAlias, columnName)).append('$').append(group-1).append(" = ?");
                                clauseValues.add(matcher.group(group));
                            }
                        } else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_QUERY_CRITERIA,
                                "Reference id patern mismatch",
                                new BasicException.Parameter("feature", filterPropertyDef.getQualifiedName()),
                                new BasicException.Parameter("ridValue", ridValue),
                                new BasicException.Parameter("pattern", referenceIdPattern.pattern())
                            );
                        }
                    }
                }
                clause.append(" AND ").append(oidColumn).append(" = ?)");
                clauseValues.add(uri.getLastSegment().toClassicRepresentation());
                operator = " OR ";
            }
            clause.append(")");
        } else {
            super.isInToSqlClause(
                connection,
                dbObject,
                column,
                filterPropertyDef,
                clause, clauseValues, values
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#isLikeToSqlClause(java.lang.String, org.openmdx.base.mof.cci.ModelElement_1_0, java.lang.StringBuilder, java.util.Collection, java.lang.Object[])
     */
    @Override
    protected void isLikeToSqlClause(
        Connection connection,
        DbObject dbObject,
        String column,
        boolean like,
        ModelElement_1_0 filterPropertyDef, 
        StringBuilder clause, 
        Collection<Object> clauseValues, 
        Path path, 
        Path value, 
        Set<Path> matchingPatterns
    ) throws ServiceException {
        if(useRidAndOid(filterPropertyDef)) {
            clause.append("(");
            int dot = column.indexOf('.');
            String tableAlias = column.substring(0, dot);
            String columnName = column.substring(dot + 1);
            String oparator = "";
            for(Path pattern : matchingPatterns) {
                clause.append(oparator).append("(");
                String externalized = this.externalizePathValue(
                    connection, 
                    path.getDescendant(pattern.getSuffix(path.size()))
                );
                String escapeClause = getEscapeClause(connection) ;
                Path uri = new Path(externalized);
                String oidValue = uri.getLastSegment().toClassicRepresentation();
                String ridValue = uri.getParent().toClassicRepresentation();
                Map<String, Pattern> referenceIdPatterns = dbObject.getConfiguration().getReferenceIdPattern();
                Pattern referenceIdPattern = referenceIdPatterns == null ? null : referenceIdPatterns.get(filterPropertyDef.getName()); 
                String ridColumn = toRid(tableAlias, columnName);
                if(referenceIdPattern == null) {
                    clause.append(ridColumn);
                    int rPos = ridValue.indexOf('%'); // TODO handle escape characters
                    if(rPos < 0) {
                        clause.append(" = ?");              
                        clauseValues.add(ridValue);
                    } else if (rPos > 0) {
                        clause.append(" LIKE ? ").append(escapeClause);              
                        clauseValues.add(ridValue.substring(0, rPos + 1)); // TODO is it really o.k. to ignore strings after a '%'
                    }
                } else {
                    Matcher matcher = referenceIdPattern.matcher(ridValue);
                    if(matcher.matches()) {
                        int groups = matcher.groupCount();
                        for(
                            int group = 1; 
                            group <= groups; 
                            group++
                        ){
                            if(group > 1) clause.append(" AND ");
                            clause.append(ridColumn).append('$').append(group-1);
                            String groupValue = matcher.group(group);
                            int rPos = groupValue.indexOf('%'); // TODO handle escape characters
                            if(rPos < 0){
                                clause.append(" = ?");
                                clauseValues.add(groupValue);
                            } else if (rPos > 0) {
                                clause.append(" LIKE ? ").append(escapeClause);              
                                clauseValues.add(groupValue.substring(0, rPos + 1)); // TODO is it really o.k. to ignore strings after a '%'
                            }
                        }
                    } else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_QUERY_CRITERIA,
                            "Reference id patern mismatch",
                            new BasicException.Parameter("feature", filterPropertyDef.getQualifiedName()),
                            new BasicException.Parameter("ridValue", ridValue),
                            new BasicException.Parameter("pattern", referenceIdPattern.pattern())
                        );
                    }
                }
                if(!"%".equals(oidValue)) {
                    clause.append(" AND ");
                    String oidColumn = this.toOid(tableAlias, columnName);
                    int oPos = oidValue.indexOf('%');
                    if(oPos < 0) {
                        clause.append(oidColumn).append(" = ?");              
                        clauseValues.add(oidValue);
                    } else {
                        clause.append(oidColumn).append(" LIKE ? ").append(escapeClause);              
                        clauseValues.add(oidValue.substring(0, oPos + 1));
                    }
                }
                oparator = " OR ";
                clause.append(")");
            }        
            clause.append(")");
        } else {
            super.isLikeToSqlClause(
                connection,
                dbObject,
                column,
                like,
                filterPropertyDef, 
                clause, 
                clauseValues, 
                path, 
                value, 
                matchingPatterns
            );
        }
    }    

    private void addIdColumns(
        List<String> to,
        String viewAliasName,
        DbObject joinObject, 
        boolean joinWithState
    ) {
         if(joinWithState) {
             to.add(toRid(viewAliasName, "core"));
             to.add(toOid(viewAliasName, "core"));
         } else {
            for(String column : joinObject.getReferenceColumn()) {
                to.add(viewAliasName + "." + column);
            }
            for(String column : joinObject.getObjectIdColumn()) {
                to.add(viewAliasName + "." + column);
            }
        }
    }

    private void addParentIdColumns(
        List<String> to,
        String joinViewAliasName
    ) {
        to.add(this.toRid(joinViewAliasName, "object_parent"));
        to.add(this.toOid(joinViewAliasName, "object_parent"));
    }

    @Override
    protected void addComplexFilter(
        Connection conn,
        DbObject dbObject,
        boolean joinFromState,
        Path reference,
        String viewAliasName,
        FilterProperty filterProperty,
        ModelElement_1_0 filterPropertyDef,
        String columnName,
        List<Object> clauseValues, 
        StringBuilder clause
    ) throws ServiceException {
        ModelElement_1_0 referencedType = getReferenceType(filterPropertyDef);        
        String joinViewAliasName = viewAliasName + "v";
        List<String> joinColumns1 = new ArrayList<String>();
        List<String> joinColumns2 = new ArrayList<String>();
        List<FilterProperty> allFilterProperties = FilterProperty.getFilterProperties(
            (QueryFilterRecord)filterProperty.getValue(0)
        );
        final boolean joinWithState = getJoinWithState(
            filterPropertyDef,
            allFilterProperties
        );
        final boolean storedAsAttribute = filterPropertyDef.getModel().referenceIsStoredAsAttribute(filterPropertyDef);
        final boolean fixedViewAliasName = !storedAsAttribute;
        final DbObject joinObject = this.getJoinObject(
            conn,
            dbObject,
            joinFromState,
            reference,
            viewAliasName,
            filterProperty,
            filterPropertyDef,
            columnName,
            referencedType,
            joinViewAliasName,
            joinColumns1,
            joinColumns2,
            joinWithState,
            storedAsAttribute
        );
        List<FilterProperty> primaryFilterProperties = this.getPrimaryFilterProperties(
            referencedType, 
            allFilterProperties
        );
        String view1 = joinObject.getConfiguration().getDbObjectForQuery1() == null ? 
            joinObject.getConfiguration().getDbObjectForUpdate1() : 
            joinObject.getConfiguration().getDbObjectForQuery1();
        String view2 = joinObject.getConfiguration().getDbObjectForQuery2() == null ? 
            joinObject.getConfiguration().getDbObjectForUpdate2() == null ? view1 : joinObject.getConfiguration().getDbObjectForUpdate2() :
            joinObject.getConfiguration().getDbObjectForQuery1();
        // Positive clauses
        List<String> includingFilterClauses = new ArrayList<String>();
        List<List<Object>> includingFilterValues = new ArrayList<List<Object>>();
        List<String> excludingFilterClauses = new ArrayList<String>();
        List<List<Object>> excludingFilterValues = new ArrayList<List<Object>>();   
        this.filterToSqlClauses(
            conn, 
            joinObject,
            joinWithState,
            joinViewAliasName, 
            view1, 
            view2, 
            JoinType.OBJECT_RID_AND_OID, 
            getUnqualifiedObjectIdColumn(joinColumns2), 
            fixedViewAliasName, 
            referencedType, 
            allFilterProperties, 
            primaryFilterProperties, 
            includingFilterClauses, 
            includingFilterValues, 
            excludingFilterClauses, 
            excludingFilterValues
        );
        boolean isForAll = filterProperty.quantor() == Quantifier.FOR_ALL.code();
        clause.append("(").append(isForAll ? "NOT " : "").append("EXISTS (SELECT 1 FROM ").append(view1).append(" ").append(joinViewAliasName).append(" WHERE (");
        for(int i = 0; i < joinColumns1.size(); i++) {
            clause.append(i == 0 ? "" : " AND ").append(joinColumns1.get(i)).append(" = ").append(joinColumns2.get(i));
        }
        clause.append(") AND (").append(isForAll ? "(1=0)" : "(1=1)");
        for(int i = 0, iLimit = includingFilterClauses.size(); i < iLimit; i++) {
            if(includingFilterClauses.get(i).length() > 0) {
                clause.append(
                    isForAll ? " OR NOT " : " AND "
                ).append(
                    includingFilterClauses.get(i)
                );
                clauseValues.addAll(
                    includingFilterValues.get(i)
                );
            }
        }
        
        // Negative clauses
        includingFilterClauses.clear();
        includingFilterValues.clear();
        excludingFilterClauses.clear();
        excludingFilterValues.clear();   
        this.filterToSqlClauses(
            conn, 
            joinObject,
            joinWithState,
            joinViewAliasName, 
            view1, 
            view2,  
            JoinType.NONE, 
            null, // no join column 
            fixedViewAliasName, 
            referencedType, 
            allFilterProperties, 
            primaryFilterProperties, 
            includingFilterClauses, 
            includingFilterValues, 
            excludingFilterClauses, 
            excludingFilterValues
        );
        for(int i = 0, iLimit = excludingFilterClauses.size(); i < iLimit; i++) {
            clause.append(
                ") AND ("
            ).append(
                excludingFilterClauses.get(i)
            );
            clauseValues.addAll(
                excludingFilterValues.get(i)
            );
        }
        clause.append(")))");
    }

    /**
     * Replace instance_of IN ... by object_class IN ...
     */
    private boolean getJoinWithState(
        ModelElement_1_0 filterPropertyDef,
        List<FilterProperty> allFilterProperties
    ) throws ServiceException {
        boolean joinWithState = false;
        FilterProperty objectClassFilterProperty = null;
        for(Iterator<FilterProperty> i = allFilterProperties.iterator(); i.hasNext(); ) {
            FilterProperty p = i.next();
            if(SystemAttributes.OBJECT_INSTANCE_OF.equals(p.name()) && !p.values().isEmpty()) {
                String instanceOf = (String)p.getValue(0);
                Set<String> allSubtypes = this.getAllSubtypes(instanceOf);
                if(allSubtypes != null) {
                    objectClassFilterProperty = new FilterProperty(
                        p.quantor(),
                        SystemAttributes.OBJECT_CLASS,
                        p.operator(),
                        allSubtypes.toArray()
                    );
                    joinWithState |= isStated(filterPropertyDef.getModel(), instanceOf);
                }
                i.remove();
            }
        }
        if(objectClassFilterProperty != null) {
            allFilterProperties.add(objectClassFilterProperty);
        }
        return joinWithState;
    }

    private DbObject getJoinObject(
        Connection conn,
        DbObject dbObject,
        boolean joinFromState,
        Path reference,
        String viewAliasName,
        FilterProperty filterProperty,
        ModelElement_1_0 filterPropertyDef,
        String columnName,
        ModelElement_1_0 referencedType,
        String joinViewAliasName,
        List<String> joinColumns1,
        List<String> joinColumns2,
        boolean joinWithState,
        final boolean storedAsAttribute
    ) throws ServiceException {
        final DbObject joinObject;
        // Reference
        if(storedAsAttribute) {
            Path identityPattern = filterPropertyDef.getModel().getIdentityPattern(referencedType);
            if(identityPattern == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_IMPLEMENTED,
                    "Joining descendants of root classes is not yet implemented for RID/OID DBs",
                    new BasicException.Parameter("filter.property", filterProperty),
                    new BasicException.Parameter("filter.definition", filterPropertyDef)
                );
            }
            joinObject = this.getDbObject(
                conn,
                null, // dbObjectConfiguration
                applyProvider(identityPattern, reference),
                null, // filter
                true
            );
            int dot = columnName.indexOf('.');
            String joinColumn1 = dot < 0  ? columnName : columnName.substring(dot + 1);
            joinColumns1.add(toRid(viewAliasName, joinColumn1));
            joinColumns1.add(toOid(viewAliasName, joinColumn1));
            addIdColumns(joinColumns2, joinViewAliasName, joinObject, joinWithState);
            if(joinColumns1.size() != joinColumns2.size()) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Column count mismatch between reference coumns and RID/OID columns",
                    new BasicException.Parameter("filter.property", filterProperty),
                    new BasicException.Parameter("filter.definition", filterPropertyDef),
                    new BasicException.Parameter("join1.ColumnExpression", joinColumns1),
                    new BasicException.Parameter("join2.ColumnName", joinColumns2)
                );
            }
        }
        // Composite parent
        else if(ModelHelper.isCompositeEnd(filterPropertyDef, true)) {
            joinObject = this.getDbObject(
                conn, 
                applyProvider(filterPropertyDef.getModel().getIdentityPattern(referencedType), reference), 
                null, // filter
                true
            );            
            addParentIdColumns(joinColumns1, viewAliasName);
            addIdColumns(joinColumns2, joinViewAliasName, joinObject, joinWithState);
        }
        // Composite
        else if(ModelHelper.isCompositeEnd(filterPropertyDef, false)) {
            joinObject = this.getDbObject(
                conn, 
                reference.getDescendant(":*", filterProperty.name()), 
                null, 
                true
            );
            addIdColumns(joinColumns1, viewAliasName, dbObject, joinFromState);
            addParentIdColumns(joinColumns2, joinViewAliasName);
        }
        // Shared
        else if(ModelHelper.isSharedEnd(filterPropertyDef, false)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED,
                "Join with shared association not yet implemented",
                new BasicException.Parameter("filter.property", filterProperty),
                new BasicException.Parameter("filter.definition", filterPropertyDef)                                
            );
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Unsupported aggregation",
                new BasicException.Parameter("filter.property", filterProperty),
                new BasicException.Parameter("filter.definition", filterPropertyDef)                                
            );
        }
        return joinObject;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#filterToSqlClause(java.sql.Connection, org.openmdx.application.dataprovider.layer.persistence.jdbc.DbObject, boolean, java.lang.String, boolean, java.lang.String, boolean, boolean, org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1.JoinType, java.lang.String, org.openmdx.base.mof.cci.ModelElement_1_0, java.util.List, boolean, java.util.List)
     */
    @Override
    protected String filterToSqlClause(
        Connection conn,
        DbObject dbObject,
        boolean statedObject,
        String viewAliasName,
        boolean fixedViewAliasName,
        String view,
        boolean viewIsPrimary,
        boolean viewIsIndexed,
        JoinType joinType,
        String joinColumn,
        ModelElement_1_0 referencedType,
        List<FilterProperty> filterProperties,
        boolean negate,
        List<Object> statementParameters
     ) throws ServiceException {
        boolean joinByRidAndOid = !viewIsPrimary && dbObject.getConfiguration().hasDbObject2();
        return super.filterToSqlClause(
            conn,
            dbObject,
            statedObject,
            joinByRidAndOid ? viewAliasName + "_" : viewAliasName,
            fixedViewAliasName,
            view,
            viewIsPrimary,
            viewIsIndexed,
            joinByRidAndOid ? JoinType.OBJECT_RID_AND_OID : joinType,
            joinColumn,
            referencedType,
            filterProperties,
            negate,
            statementParameters);
    }

}
