/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RidOidQueryDatabase_1.java,v 1.7 2011/09/05 22:39:47 hburger Exp $
 * Description: Database plug-in supporting RID/OID queries
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/09/05 22:39:47 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011, OMEX AG, Switzerland
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

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 * Database plug-in supporting RID/OID queries
 */
public class RidOidQueryDatabase_1 extends Database_1 {

    /**
     * Constructor 
     */
    public RidOidQueryDatabase_1() {
        super();
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.Database_1#isAspectBaseClass(java.lang.String)
     */
    @Override
    protected boolean isAspectBaseClass(String qualifiedClassName) {
        return this.enableStateFilterSubstitution && super.isAspectBaseClass(qualifiedClassName);
    }


    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.AbstractDatabase_1#isBaseClass(java.lang.String)
     */
    @Override
    protected boolean isBaseClass(String qualifiedClassName) {
        return super.isBaseClass(qualifiedClassName) || (
            !this.enableStateFilterSubstitution && super.isAspectBaseClass(qualifiedClassName)
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
            this.useNormalizedReferences() &&
            this.getModel().isReferenceType(filterPropertyDef) &&
            this.getModel().referenceIsStoredAsAttribute(filterPropertyDef);
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
            int pos = column.indexOf('.') + 1;
            String tablePrefix = column.substring(0, pos);
            String columnName = column.substring(pos);
            String oidColumn = tablePrefix + this.toOid(this.getPrivateAttributesPrefix() + columnName + "_");
            String operator = "";
            for(Object value : values) {
                clause.append(operator).append("(");
                Path uri = new Path(this.externalizePathValue(connection,(Path)value));
                String ridValue = uri.getParent().toString();
                if("org:openmdx:base:Aspect:core".equals(filterPropertyDef.objGetValue("qualifiedName"))) {
                    clause.append(tablePrefix).append(this.OBJECT_RID).append(" = ?");
                    clauseValues.add(ridValue);
                } else {
                    Map<String, Pattern> referenceIdPatterns = dbObject.getConfiguration().getReferenceIdPattern();
                    Pattern referenceIdPattern = referenceIdPatterns == null ? null : referenceIdPatterns.get(filterPropertyDef.objGetValue("name")); 
                    if(referenceIdPattern == null) {
                        clause.append(tablePrefix).append(this.toRid(this.getPrivateAttributesPrefix() + columnName + "_")).append(" = ?");
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
                                clause.append(tablePrefix).append(this.toRid(this.getPrivateAttributesPrefix() + columnName + "_")).append('$').append(group-1).append(" = ?");
                                clauseValues.add(matcher.group(group));
                            }
                        } else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_QUERY_CRITERIA,
                                "Reference id patern mismatch",
                                new BasicException.Parameter("feature", filterPropertyDef.objGetValue("qualifiedName")),
                                new BasicException.Parameter("ridValue", ridValue),
                                new BasicException.Parameter("pattern", referenceIdPattern.pattern())
                            );
                        }
                    }
                }
                clause.append(" AND ").append(oidColumn).append(" = ?)");
                clauseValues.add(uri.getBase());
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
            int pos = column.indexOf('.') + 1;
            String tablePrefix = column.substring(0, pos);
            String columnName = column.substring(pos);
            String oparator = "";
            for(Path pattern : matchingPatterns) {
                clause.append(oparator).append("(");
                String externalized = this.externalizePathValue(
                    connection, 
                    path.getDescendant(pattern.getSuffix(path.size()))
                );
                String escapeClause = getEscapeClause(connection) ;
                Path uri = new Path(externalized);
                String oidValue = uri.getBase();
                String ridValue = uri.getParent().toString();
                Map<String, Pattern> referenceIdPatterns = dbObject.getConfiguration().getReferenceIdPattern();
                Pattern referenceIdPattern = referenceIdPatterns == null ? null : referenceIdPatterns.get(filterPropertyDef.objGetValue("name")); 
                String ridColumn = tablePrefix + this.toRid(this.getPrivateAttributesPrefix() + columnName + "_");
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
                            new BasicException.Parameter("feature", filterPropertyDef.objGetValue("qualifiedName")),
                            new BasicException.Parameter("ridValue", ridValue),
                            new BasicException.Parameter("pattern", referenceIdPattern.pattern())
                        );
                    }
                }
                if(!"%".equals(oidValue)) {
                    clause.append(" AND ");
                    String oidColumn = tablePrefix + this.toOid(this.getPrivateAttributesPrefix() + columnName + "_");
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
                value, matchingPatterns
            );
        }
    }    

}
