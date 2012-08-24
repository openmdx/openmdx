/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: DB Object
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;

/**
 * DbObject provides methods for type-specific object access.
 * AbstractDatabase_1 delegates type-specific object access operations
 * to the DbObject.
 * <p>
 * DbObject is an abstract class which must be subclassed by a
 * concrete, type-specific implementation. StandardDbObject is
 * a standard implementation supporting the OO-to-RR mappings GENERIC
 * and SLICED. These types are OK for most new applications.
 * <p>
 * Existing schemas and tables can be accessed by AbstractDatabase_1 by
 * user-defined DbObjects. A user-defined type is implemented by
 * subclassing DbObject and by implementing the abstract methods.
 * <p>
 * All DbObject implementations must expose the following object
 * model to AbstractDatabase:
 * <p>
 * <ul>
 *   <li>The id of an object is mapped to one or more columns (objectIdColumn)
 *   <li>The object reference is mapped to one or more columns (referenceColumn)
 *   <li>The slice index is mapped to one column (indexColumn)
 * </ul>
 * <p>
 * getView() must return a view which returns rows containing the columns
 * objectIdColumn, referenceIdColumn and indexColumn. These columns are then
 * mapped to the object reference path and object id by the DbObject
 * implementation.
 * <p>
 * A user-defined DbObject is configured with the fully qualified class
 * name for the configuration option dbObjectFormat, e.g.
 * <p>
 * <pre>
 * <org.openmdx.deployment1.DataproviderTypeStringProperty qualifiedName="PERSISTENCE:dbObjectFormat">
 *   <_object>
 *     <value>
 *       <_item>org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.SlicedDbObject</_item>
 *     </value>
 *   </_object>
 *   <_content/>
 * </org.openmdx.deployment1.DataproviderTypeStringProperty>
 * </pre>
 */
@SuppressWarnings({"rawtypes","unchecked"})
public abstract class DbObject implements Serializable {

	/**
	 * Constructor
	 * 
	 * @param database
	 * @param conn
	 * @param typeConfigurationEntry
	 * @param accessPath
	 * @param extent
	 * @param query
	 * 
	 * @throws ServiceException
	 */
	protected DbObject(
		AbstractDatabase_1 database, 
		Connection conn,
		DbObjectConfiguration typeConfigurationEntry,
		Path accessPath,
		boolean extent, 
		boolean query
	) throws ServiceException {
		this.conn = conn;
		this.database = database;
		this.resourceIdentifier = accessPath;
		if(accessPath.size() % 2 == 1) {
			this.objectId = accessPath.getBase();
			this.reference = accessPath.getParent();
		}
		else {
			this.objectId = null;
			this.reference = accessPath;
		}
		this.referencedType = this.reference;
		this.excludeAttributes = new HashSet();

		// Type configuration
		this.dbObjectConfiguration = typeConfigurationEntry;
		if(this.dbObjectConfiguration == null) {
			this.dbObjectConfiguration = this.database.configuration.getDbObjectConfiguration(
				this.getReferencedType()
			);
		}
		this.extent = extent;
		this.query = query;
		this.referenceColumn = new ArrayList<String>();
		this.referenceValues = new ArrayList<Object>();
	}

	/**
	 * Constructor
	 * 
	 * @param database
	 * @param conn
	 * @param typeConfiguration
	 */
	protected DbObject(
		AbstractDatabase_1 database, 
		Connection conn,
		DbObjectConfiguration typeConfiguration
	) {
		this.database = database;
		this.conn = conn;
		this.dbObjectConfiguration = typeConfiguration;
		this.resourceIdentifier = null;
		this.objectId = null;
		this.objectIdValues = null;
		this.reference = null;
		this.excludeAttributes = new HashSet();    
		this.extent = false;
		this.query = false;
		this.referenceColumn = null;
		this.referenceValues = null;
	}

	/**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = 5312916031308914829L;

	private Path referencedType;

	private final List<String> referenceColumn;
	private final List<Object> referenceValues;
	protected String indexColumn = null;
	protected List<String> objectIdColumn = null;
	protected String objectIdClause = null;
	protected String referenceClause = null;
	protected DbObjectConfiguration dbObjectConfiguration = null;
	protected List<String> objectIdValues = null;
	protected final Set<String> excludeAttributes;

	protected final AbstractDatabase_1 database;
	protected final Connection conn;

	private final String objectId;
	private final Path reference;
	private final Path resourceIdentifier;
	private final boolean extent;
	private final boolean query;

	
	//---------------------------------------------------------------------------  
	// Typically, these methods do not have to be implemented by subclasses
	// of DbObjecType.
	//---------------------------------------------------------------------------  

	//---------------------------------------------------------------------------
	protected Model_1_0 getModel(){
		return this.database.getModel();
	}

	//---------------------------------------------------------------------------
	public DbObjectConfiguration getConfiguration(
	) throws ServiceException {
		return this.dbObjectConfiguration;
	}

	//---------------------------------------------------------------------------
	public String getObjectId(
	) {
		return this.objectId;
	}

	//---------------------------------------------------------------------------
	public Path getReference(
	) {
		return this.reference;
	}

	//---------------------------------------------------------------------------
	public Path getResourceIdentifier(
	) {
		return this.resourceIdentifier;
	}

	//---------------------------------------------------------------------------  
	public Path getReferencedType(
	) throws ServiceException {
		return this.referencedType;
	}

	//---------------------------------------------------------------------------  
	public List<String> getReferenceColumn(
	) {
		return this.referenceColumn;
	}

	//---------------------------------------------------------------------------  
	public List getObjectIdColumn(
	) {
		return this.objectIdColumn;
	}

	/**
	 * Returns the qualified class name of the current object in the frs.
	 */
	//---------------------------------------------------------------------------  
	public String getObjectClass(
		FastResultSet frs
	) throws ServiceException, SQLException {
		String columnName = this.database.getColumnName(conn, SystemAttributes.OBJECT_CLASS, 0, false, false, false);
		return frs.getColumnNames().contains(columnName) ? frs.getObject(columnName).toString().trim() : null;
	}

	/**
	 * Returns the set of attributes which must not be included in SQL statements
	 */
	public Set getExcludeAttributes(
	) {
		return this.excludeAttributes;
	}

	//---------------------------------------------------------------------------  
	// These following abstract methods must be implemented by concrete subclasses 
	// of DbObject
	//---------------------------------------------------------------------------  

	/**
	 * Returns an SQL clause which selects the objects of the primary db object
	 * by their reference.
	 * <p>
	 * Example: StandardDbObject returns '(OBJECT_RID IN &lt;rid&gt;)'
	 */
	public abstract String getReferenceClause(
	) throws ServiceException;

	/**
	 * Returns the values for reference clause 1 which correspond to the ? placeholders 
	 */
	public List<Object> getReferenceValues(
	) throws ServiceException {
		return this.referenceValues;
	}

	/**
	 * Append the lock assertion unless it is <code>null</code>
	 * 
	 * @param statement
	 * @param parameters
	 * @param assertion
	 * 
	 * @return <code>true</code> if a lock assertion has beend appended
	 * 
	 * @throws ServiceException
	 */
	protected boolean appendLockAssertion (
		StringBuilder statement,
		List<Object> parameters,
		String assertion
	) throws ServiceException {
		boolean hasAssertion = assertion != null;
		if(hasAssertion) {
			LockAssertion lockAssertion = new LockAssertion(assertion);
			statement.append(
				" AND ("
			).append(
				this.database.getColumnName(
					this.conn, 
					lockAssertion.getFeature(), 
					0, // index
					false, // indexSuffixIfZero
					true, // forPreparedStatement
					false // markAsPrivate
				)
			);
			Object value = lockAssertion.getValue();
			if(value == null){
				statement.append(" IS NULL ");
			} else {
				statement.append(
					' '
				).append(
					lockAssertion.getRelation()
				).append(
					' '
				).append(
					this.database.getPlaceHolder(this.conn, value)
				);
				parameters.add(value);
			}
			statement.append(")");
		}
		return hasAssertion;
	}

	/**
	 * Returns an SQL clause whch select the objects by their object oid.
	 * <p>
	 * Example: StandardDbObject return '(OBJECT_OID IN <oid>)'
	 */
	public abstract String getObjectIdClause(
	) throws ServiceException;

	/**
	 * Returns the values which correspond to the ? placeholders of the
	 * objectId clause.
	 */
	public abstract List getObjectIdValues(
	) throws ServiceException;

	/**
	 * In case the DbObject supports multi-valued object slices this method
	 * returns the name of the column which stores the slice index. null
	 * indicates that no index column is supported.
	 */
	public abstract String getIndexColumn(
	);

	/**
	 * Maps the row frs (which corresponds to an object slice) to the object
	 * reference. This method maps the column values which represent the
	 * object reference to a reference path.
	 * <p>
	 * Example: StandardDbObject maps the column 'object_rid' with the help
	 * of the _REF table to the object reference.
	 */
	public Path getObjectReference(
		FastResultSet frs
	) throws SQLException, ServiceException {
		throw new UnsupportedOperationException(); 
	}

	/**
	 * Maps the row frs (which corresponds to an object slice) to the object
	 * id. This method maps the column values of getObjectIdColumn() to the
	 * object id.
	 * <p>
	 * Example: StandardDbObject returns the value of the column 
	 * 'object_oid'.
	 */
	public String getObjectId(
		FastResultSet frs
	) throws SQLException, ServiceException{
		throw new UnsupportedOperationException(); 
	}

	/**
	 * A subclass has to override either getResourceIdentifier() (the preferred way)
	 * or both getObjectReference() and getObjectId() (the deprecated way).
	 * <p><em>
	 * Note:</br>
	 * Failing to do either leads to an <code>UnsupportedOperationException</code>!
	 * </em>
	 * 
	 * @param frs fast result set
	 * 
	 * @return the resource identifier
	 * 
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public Path getResourceIdentifier(
		FastResultSet frs
	) throws SQLException, ServiceException{
		return getObjectReference(frs).getChild(getObjectId(frs));
	}

	/**
	 * Maps the row frs (which corresponds to an object slice) to the object slice
	 * index. This method maps the column value of getIndexColumn() to the slice
	 * index.
	 * <p>
	 * Example: StandardDbObject returns the value of the column object_idx.
	 */
	public abstract int getIndex(
		FastResultSet frs
	) throws SQLException;

	/**
	 * Returns true, if the given column is part of the object to be returned.
	 * Especially existing tables contain columns which are private, hidden or
	 * unused and which must not be mapped to application objects.
	 * <p>
	 * Example: StandardDbObject returns true, if the column does not start
	 * with the configured privateAttributePrefix and is not object_oid, 
	 * object_rid and object_idx.
	 */
	public abstract boolean includeColumn(
		String columnName
	);

	/**
	 * Return the configured hint for this DB object. Hints are added to
	 * SELECT statements in the form SELECT <hint> ... 
	 */
	public String getHint(
	) throws ServiceException {
		return "";
	}

	//---------------------------------------------------------------------------
	// The following methods must be implemented by an updatable DbObject
	// In case of read-only types, they should throw a NOT_SUPPORTED ServiceException.
	//---------------------------------------------------------------------------

	/**
	 * Remove specified object and all contained objects, i.e. objects which's
	 * path start with accessPath.
	 */
	public abstract void remove(
	) throws ServiceException;

	/**
	 * Create the i-th object slice
	 */
	public abstract void createObjectSlice(
		int index,
		String objectClass,
		MappedRecord object
	) throws ServiceException;

	/**
	 * Replaces the oldObject slice with the newObject slice.
	 * <p>
	 * Example: StandardDbObject implements object replacement with UPDATE
	 * operations.
	 *
	 * @param index
	 * @param newObject
	 * @param oldObject
	 * @param writeLock 
	 * @param readLock TODO
	 */
	public abstract void replaceObjectSlice(
		int index,
		MappedRecord newObject,
		MappedRecord oldObject, 
		String writeLock, String readLock
	) throws ServiceException;

	/**
	 * Returns underlying table/view name of this DbObject. This method is
	 * used by the create operation.
	 */
	public abstract String getTableName(
	);

	/**
	 * Returns the join criteria if the table returned by {@link #getTableName()} is 
	 * accessed via a join table. The table is then accessed with
	 * </pre> 
	 *   SELECT T v 
	 *   INNER JOIN joinTable vj
	 *   ON v.objectIdColumn = vj.joinColumnEnd1
	 * </pre>
	 * {@link #getReferenceClause()} must return a reference clause of the form
	 * (vj.joinColumnEnd2 = ?)
	 * 
	 * @return join criteria or null. The array contains 
	 *         0:join table, 1:join column end 1, 2:join column end 2.
	 */
	public abstract String[] getJoinCriteria(
	);

	/**
	 * Slices 'object' into one or more slices allowing to store into
	 * different rows (or tables). The path of a marhalled object is
	 * object.path().getDescendant(new String[]{"pi","0"}), where i is the
	 * slice number. In addition, for all attributes 'a' with values of type
	 * 'path', the attributes 'a'_reference_id and 'a'_object_id are added
	 * containing the reference and object ids of the path.
	 */
	abstract public MappedRecord[] sliceAndNormalizeObject(
		MappedRecord object, 
		boolean removeValuesProvidedByView
	) throws ServiceException;

	/**
	 * Map identity filter property p to a db object specific identity filter
	 * property.
	 */
	abstract public FilterProperty mapToIdentityFilterProperty(
		FilterProperty p
	) throws ServiceException;

	/**
	 * Retrieve extent.
	 *
	 * @return Returns the extent.
	 */
	protected boolean isExtent() {
		return this.extent;
	}

	/**
	 * Retrieve query.
	 *
	 * @return Returns the query.
	 */
	protected boolean isQuery() {
		return this.query;
	}

}
