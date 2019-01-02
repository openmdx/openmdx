/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Database_1_0  
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.spi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DbObject;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DbObjectConfiguration;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.macros.MacroConfiguration;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RequestRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.text.conversion.SQLWildcards;

public interface Database_2_0 extends Database_1_0 {

    static enum JoinType {
        NONE, SPECIFIED_COLUMN_WITH_OBJECT_ID, OBJECT_RID_AND_OID
    }
    
    static enum OrderAmendment {
        INTRINSIC, BY_OBJECT_ID
    }

    /**
     * @return
     */
    boolean isOrderNullsAsEmpty();

    /**
     * @param ispec
     * @param request
     * @return
     * @throws ServiceException
     * @throws SQLException
     */
    Connection getConnection(
        RestInteractionSpec ispec,
        RequestRecord request
    ) throws ServiceException, SQLException;

    /**
     * @param conn
     * @param accessPath
     * @param filter
     * @param isQuery
     * @return
     * @throws ServiceException
     */
    DbObject getDbObject(
        Connection conn,
        Path accessPath,
        List<FilterProperty> filter,
        boolean isQuery
    ) throws ServiceException;

    /**
     * @param _clause
     * @return
     */
    String removeViewPrefix(
        String _clause
    );

    /**
     * @param request
     * @param qualifiedClassNames
     * @return
     * @throws ServiceException
     */
    FilterProperty mapInstanceOfFilterProperty(
        QueryRecord request,
        Collection<String> qualifiedClassNames
    ) throws ServiceException;

    /**
     * @param referencedType
     * @param filterProperties
     * @return
     * @throws ServiceException
     */
    List<FilterProperty> getPrimaryFilterProperties(
        ModelElement_1_0 referencedType,
        List<FilterProperty> filterProperties
    ) throws ServiceException;

    /**
     * @param referencedTypeDef
     * @param filterProperties
     * @return
     * @throws ServiceException
     */
    List<ModelElement_1_0> getFilterPropertyDefs(
        ModelElement_1_0 referencedTypeDef, List<FilterProperty> filterProperties
    )
        throws ServiceException;

    /**
     * @param conn
     * @param dbObject
     * @param dbObjectHint
     * @param viewMode
     * @param requestedColumnSelector
     * @param requestedMixins
     * @return
     * @throws ServiceException
     */
    String getView(
        Connection conn, DbObject dbObject, String dbObjectHint, short viewMode, String requestedColumnSelector,
        Set<String> requestedMixins
    )
        throws ServiceException;

    /**
     * @param conn
     * @param dbObject
     * @param statedObject
     * @param viewAliasName
     * @param view1
     * @param view2
     * @param joinType
     * @param joinColumn
     * @param fixedViewAliasName
     * @param referencedType
     * @param allFilterProperties
     * @param primaryFilterProperties
     * @param includingClauses
     * @param includingClausesValues
     * @param exludingClauses
     * @param exludingClausesValues
     * @throws ServiceException
     */
    void filterToSqlClauses(
        Connection conn, DbObject dbObject, boolean statedObject, String viewAliasName, String view1, String view2,
        JoinType joinType, String joinColumn, boolean fixedViewAliasName, ModelElement_1_0 referencedType,
        List<FilterProperty> allFilterProperties, List<FilterProperty> primaryFilterProperties,
        List<String> includingClauses, List<List<Object>> includingClausesValues, List<String> exludingClauses,
        List<List<Object>> exludingClausesValues
    )
        throws ServiceException;
    
    /**
     * @param object
     * @throws ServiceException
     */
    void removePrivateAttributes(
        ObjectRecord object
    )
        throws ServiceException;
    
    /**
     * @param connection
     * @param dbObject
     * @return
     * @throws ServiceException
     */
    OrderAmendment getOrderAmendment(
        Connection connection, DbObject dbObject
    )
        throws ServiceException;

    /**
     * @param ps
     * @param statement
     * @param statementParameters
     * @param maxRows
     * @return
     * @throws SQLException
     */
    ResultSet executeQuery(
        PreparedStatement ps,
        String statement,
        List<?> statementParameters,
        int maxRows
    ) throws SQLException;

    /**
     * @param conn
     * @param dbObject
     * @param rs
     * @param objects
     * @param attributeSelector
     * @param attributeSpecifiers
     * @param objectClassAsAttribute
     * @param position
     * @param primaryObjectClass
     * @param target TODO
     * @param maxObjectsToReturn
     * @return
     * @throws ServiceException
     * @throws SQLException
     */
    boolean getObjects(
        Connection conn, DbObject dbObject, ResultSet rs, List<ObjectRecord> objects, short attributeSelector,
        Map<String, AttributeSpecifier> attributeSpecifiers, boolean objectClassAsAttribute, int position, int objectBatchSize,
        String primaryObjectClass, Target target
    )
        throws ServiceException, SQLException;

    /**
     * @param conn
     * @param accessPath
     * @param isQuery
     * @return
     * @throws ServiceException
     */
    DbObject createDbObject(
        Connection conn, Path accessPath, boolean isQuery
    )
        throws ServiceException;

    /**
     * Macros allow value prefix replacements in specified columns
     * 
     * @return the macro configuration
     */
    MacroConfiguration getMacroConfiguration();
    
    /**
     * @param conn
     * @param ispec
     * @param object
     * @param reply
     * @throws ServiceException
     */
    void create(
        Connection conn, RestInteractionSpec ispec, ObjectRecord object, ResultRecord reply
    )
        throws ServiceException;

    /**
     * @param lock
     * @return
     * @throws ServiceException
     */
    String toReadLock(
        Object lock
    )
        throws ServiceException;

    /**
     * @param version
     * @return
     * @throws ServiceException
     */
    String toWriteLock(
        Object version
    )
        throws ServiceException;

    /**
     * @param conn
     * @param dbObjectConfiguration
     * @param accessPath
     * @param isQuery
     * @return
     * @throws ServiceException
     */
    DbObject createDbObject(
        Connection conn, DbObjectConfiguration dbObjectConfiguration, Path accessPath, boolean isQuery
    )
        throws ServiceException;

    /**
     * @return
     */
    SQLWildcards getSqlWildcards();

    /**
     * @param object
     * @param removePrivate
     * @param removeNonPersistent
     * @param removeSize
     * @throws ServiceException
     */
    void removeAttributes(
        ObjectRecord object, boolean removePrivate, boolean removeNonPersistent, boolean removeSize
    )
        throws ServiceException;

    /**
     * @param object
     * @throws ServiceException
     */
    void completeObject(
        ObjectRecord object
    )
        throws ServiceException;

    /**
     * @return
     */
    int getOptimalFetchSize();

    /**
     * @return
     */
    int getRowBatchSize();

    /**
     * @return
     */
    int getObjectBatchSize();

    /**
     * @return
     */
    int getResultSetLimit();
    
}
