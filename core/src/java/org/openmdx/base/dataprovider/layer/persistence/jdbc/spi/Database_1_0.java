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
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.DatabaseConfiguration;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DbObject;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RequestRecord;
import org.w3c.cci2.SparseArray;

public interface Database_1_0 {

	public DatabaseConfiguration getDatabaseConfiguration() throws ServiceException;

	public String getPlaceHolder(
		Connection connection, 
		Object value
	) throws ServiceException;

	public String getColumnName(
		Connection conn, 
		String attributeName,
		int index, 
		boolean indexSuffixIfZero, 
		boolean ignoreReservedWords, 
		boolean markAsPrivate
	) throws ServiceException;

	public void setPreparedStatementValue(
		Connection conn, 
		PreparedStatement ps,
		int position, 
		Object value
	) throws ServiceException, SQLException;

	public PreparedStatement prepareStatement(
		Connection conn, 
		String statement,
		boolean updatable
	) throws SQLException;

	public int executeUpdate(
		PreparedStatement ps, 
		String statement, 
		List<?> statementParameters
	) throws SQLException;

	public String getSelectReferenceIdsClause(
		Connection conn,
		Path pattern, 
		List<Object> statementParameters
	) throws ServiceException;

	public Object getReferenceId(
		Connection conn, 
		Path referencePattern, 
		boolean forceCreate
	) throws ServiceException;

	public String toOid(String name);

	public String toRid(String name);
	
	public String toRsx(String name);

	public PreparedStatement prepareStatement(
		Connection conn, 
		String statement
	) throws SQLException;

	public String getPrivateAttributesPrefix();

	public Object externalizeStringValue(
		String columnName, 
		Object value
	);

	public String internalizeStringValue(
		String columnName, 
		String value
	);

	public String getNamespaceId();

	public String getAutonumValue(
		Connection conn, 
		String sequenceName,
		String asFormat
	) throws ServiceException, SQLException;

	public String getObjectId(
		String oid
	) throws ServiceException;

	public boolean isSetSizeColumns();

	public boolean isEmbeddedFeature(
		String featureName
	);

	public boolean isPersistent(
		ModelElement_1_0 featureDef
	) throws ServiceException;

	public String getSizeSuffix();

	public Path getReference(
		Connection conn, 
		Object referenceId
	) throws ServiceException;

	public String getFeatureName(
		String columnName
	) throws ServiceException;

	public String getObjectId(
		Connection conn, 
		Path resourceIdentifier
	) throws ServiceException;

	public String getEscapeClause(
		Connection connection
	) throws ServiceException;

	public String getDatabaseSpecificColumnName(
		Connection conn,
		String columnName, 
		boolean ignoreReservedWords
	) throws ServiceException;

	public String getObjectRidColumnName();

	public String getObjectIdxColumnName();

	public String getObjectOidColumnName();
	 
    public SparseArray<String> getColumnNameFrom();

    public SparseArray<String> getColumnNameTo();
	    
    public SparseArray<Path> getType();

    public SparseArray<String> getTypeName();

    public SparseArray<String> getDbObject();

    public SparseArray<String> getDbObject2();

    public SparseArray<String> getDbObjectFormat();

    public SparseArray<Integer> getPathNormalizeLevel();

    public SparseArray<String> getDbObjectForQuery();

    public SparseArray<String> getDbObjectForQuery2();
	    
    public SparseArray<String> getDbObjectsForQueryJoinColumn();

    public SparseArray<String> getDbObjectHint();

    public SparseArray<String> getObjectIdPattern();

    public SparseArray<String> getJoinTable();

    public SparseArray<String> getJoinColumnEnd1();

    public SparseArray<String> getJoinColumnEnd2();

    public SparseArray<String> getUnitOfWorkProvider();

    public SparseArray<String> getRemovableReferenceIdPrefix();

    public SparseArray<Boolean> getDisableAbsolutePositioning();

    public SparseArray<String> getReferenceIdPattern();

    public SparseArray<String> getAutonumColumn();
    
    public boolean isUseNormalizedReferences();

    public String getReferenceIdFormat();
    
    public boolean isNormalizeObjectIds();
    
    public boolean isUseViewsForRedundantColumns();
    
    public boolean isCascadeDeletes();

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
        RestInteractionSpec ispec, RequestRecord request
    )
        throws ServiceException, SQLException;

    /**
     * @param conn
     * @param ispec
     * @param path
     * @param attributeSelector
     * @param attributeSpecifiers
     * @param objectClassAsAttribute
     * @param reply
     * @param throwNotFoundException
     * @throws ServiceException
     */
    void get(
        Connection conn, RestInteractionSpec ispec, Path path, short attributeSelector,
        Map<String, AttributeSpecifier> attributeSpecifiers, boolean objectClassAsAttribute, Target target,
        boolean throwNotFoundException
    )
        throws ServiceException;

    /**
     * @param conn
     * @param accessPath
     * @param filter
     * @param isQuery
     * @return
     * @throws ServiceException
     */
    DbObject getDbObject(
        Connection conn, Path accessPath, List<FilterProperty> filter, boolean isQuery
    )
        throws ServiceException;

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
        QueryRecord request, Collection<String> qualifiedClassNames
    )
        throws ServiceException;
    
}
