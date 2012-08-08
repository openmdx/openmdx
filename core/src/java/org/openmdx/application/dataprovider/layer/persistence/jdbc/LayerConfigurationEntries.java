/*
 * ==================================================================== Project:
 * openmdx, http://www.openmdx.org/ Name: $Id: LayerConfigurationEntries.java,v
 * 1.37 2006/11/17 16:55:57 hburger Exp $ Description: Generated constants for
 * LayerConfigurationEntries Revision: $Revision: 1.1 $ Owner: OMEX AG,
 * Switzerland, http://www.omex.ch Date: $Date: 2009/05/26 14:31:21 $
 * ====================================================================
 * 
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  * Neither the name of the openMDX team nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as listed in
 * the NOTICE file.
 */
package org.openmdx.application.dataprovider.layer.persistence.jdbc;



/**
 * The <code>PersistenceLayerConfigurationEntries</code> class contains
 * constants identifying the configuration entries of the dataprovider's
 * persistence layer.
 */
public class LayerConfigurationEntries extends org.openmdx.application.dataprovider.layer.persistence.common.CommonConfigurationEntries {

  
  protected LayerConfigurationEntries() {
   // Avoid instantiation
  }


  /**
     * FORCE_CREATE forces creation of missing references if true. Allows to
     * create objects which do not have no parents.
     */
  static public final String FORCE_CREATE = "forceCreate";



  /**
     * OPTIMIZED_TYPE list of optimized types. Objects with matching paths are
     * stored in optimized tables (attributes in separate rows).
     */
  static public final String OPTIMIZED_TYPE = "optimizedType";



  /**
     * OPTIMIZED_TABLE list of optimized tables. For each optimizedType there
     * must be a corresponding optimized table. An optimized table can be any
     * string which is a valid SQL table name.
     */
  static public final String OPTIMIZED_TABLE = "optimizedTable";



  /**
     * CONNECTION_URL jdbc url which is used to connect to database
     */
  static public final String CONNECTION_URL = "connectionUrl";



  /**
     * If MODEL_DRIVEN is set to true, the plugin uses model information to
     * optimize SQL statements. Model information can be useful in case of find
     * operations to determine the multiplicity of filter attributes. In case of
     * 0..1 or 1..1 multiplicity queries can typically be optimized.
     */
  static public final String MODEL_DRIVEN = "modelDriven";



  /**
     * @deprecated COLUMN_NAME_LONG defines the corresponding long column name
     *             for a configured COLUMN_NAME_SHORT.
     */
  static public final String COLUMN_NAME_LONG = "columnNameLong";



  /**
     * @deprecated COLUMN_NAME_SHORT defines the corresponding short column name
     *             for a configured COLUMN_NAME_LONG.
     */
  static public final String COLUMN_NAME_SHORT = "columnNameShort";



  /**
     * COLUMN_NAME_FROM defines the corresponding 'from' column name for a
     * (from-column-name,to-column-name) mapping.
     */
  static public final String COLUMN_NAME_FROM = "columnNameFrom";



  /**
     * COLUMN_NAME_TO defines the corresponding 'to' column name for a
     * (from-column-name,to-column-name) mapping.
     */
  static public final String COLUMN_NAME_TO = "columnNameTo";

  /**
     * Allows to specify an autonum column. The format is
     * <code>dbObject.columnName [AS format]</code>, The database plugin
     * handles autonum colums as follows:
     * <ul>
     * <li>autonum is applied for object slice creation with index 0
     * <li>when a column is specified as autonum and no value is supplied with
     * the create request, a value of the form
     * <code>NEXTVAL(sequence name)</code> is generated in the
     * <code>VALUES</code> clause of the <code>INSERT</code> statement
     * <li>a corresponding sequence with name
     * <code>namespace_columnName_SEQ</code> must exist. For databases which
     * do not support sequences a table <code>namespace_columnName_SEQ</code>
     * must exist with column nextval.
     * <li>if the optional format is specified with <code>AS format</code> a
     * cast is applied to the sequence value, e.g. CAST(sequence name AS
     * CHAR(20)).
     */
  static public final String AUTONUM_COLUMN = "autonumColumn";
  
  /**
     * TYPE list of types. Objects with matching paths are stored in optimized
     * tables (attributes in separate rows).
     */
  static public final String TYPE = "type";

  /**
     * TYPE_NAME list of type names. Path references are stored as
     * <typeName>{"/" <path-component[i]>} where path-component[i] is
     * non-wildcard.
     */
  static public final String TYPE_NAME = "typeName";

  /**
     * 'dbObject' defines the primary table or view name of a database object
     * for the specified type which is used for querying and updating objects.
     * The DB_OBJECT must either be of format generic or optimized, i.e.
     * DB_OBJECT_FORMAT_GENERIC or DB_OBJECT_FORMAT_OPTIMIZED, respectively.
     */
  static public final String DB_OBJECT = "dbObject";

  /**
     * 'dbObject2' defines the optional secondary table or view name of a
     * database object for the specified type which is used for querying and
     * updating objects. The configuration of a secondary object is optional and
     * is used to store multi-valued attributes.
     */
  static public final String DB_OBJECT_2 = "dbObject2";

  /**
     * DB_OBJECT_FORMAT defines the format of DB_OBJECT.
     */
  static public final String DB_OBJECT_FORMAT = "dbObjectFormat";

  /**
     * DB_OBJECT_HINT defines the hint string which is inserted at SELECT
     * statements as follows: SELECT \u00abhint\u00bb * FROM ...
     */
  static public final String DB_OBJECT_HINT = "dbObjectHint";

  /**
     * <code>dbObjectForQuery</code> defines the primary view name of a
     * database object for a the specified type which is used for querying
     * objects. The view must return the single-valued attributes of an object
     * if a secondary object for query is configured. It must return object
     * slices if no secondary object for query is configured.
     */
  static public final String DB_OBJECT_FOR_QUERY = "dbObjectForQuery";

  /**
     * <code>dbObjectForQuery2</code> defines the optional secondary view
     * name. The view must return the multi-valued attributes of an object.
     */
  static public final String DB_OBJECT_FOR_QUERY_2 = "dbObjectForQuery2";

  /**
     * <code>dbObjectsForQueryJoinColumn</code> specifies the column which is
     * used to join <code>dbObjectForQuery</code> and
     * <code>dbObjectForQuery2</code>. If not specified the default join
     * column is <code>object_id</code>. The SELECT is constructed as
     * <code>SELECT * FROM 'dbObject' WHERE 'join column' IN (SELECT
   * object_id FROM 'dbObject2' WHERE 'filter criteria')</code>.
     */
  static public final String DB_OBJECTS_FOR_QUERY_JOIN_COLUMN = "dbObjectsForQueryJoinColumn";
  
  /**
     * The <code>sliced</code> format of a DB_OBJECT contains an index-slice
     * of an object in one row. The schema of a DB_OBJECT with format 'sliced'
     * is: object_objectId, object_referenceId, object_idx, \u00aba0\u00bb, ...,
     * \u00aban\u00bb$
     */
  static public final String DB_OBJECT_FORMAT_SLICED = "sliced";
  
  /**
     * The 'slicedNonIndexed' format of a DB_OBJECT contains one object in one
     * row. The schema of a DB_OBJECT with format 'slicedNonIndexed' is:
     * object_objectId, object_referenceId, \u00aba0\u00bb, ..., \u00aban\u00bb$
     */
  static public final String DB_OBJECT_FORMAT_SLICED_NON_INDEXED = "slicedNonIndexed";


  /**
     * The 'slicedParentRidOnly' format of a DB_OBJECT contains an index-slice
     * of an object in one row. The schema is: 0 as object_rid, object_oid,
     * p$$parent_object__rid, p$$parent_object__oid ... The sliced does NOT
     * contain the column object_rid. Instead, it provides rid and oid for the
     * parent object. This format is useful when the calculation of the column
     * object_rid is expensive compared to the calculation of the parent object
     * rid|oid.
     */
  static public final String DB_OBJECT_FORMAT_SLICED_PARENT_RID_ONLY = "slicedParentRidOnly";

  /**
     * The 'slicedNonIndexedParentRidOnly' format of a DB_OBJECT contains an
     * index-slice of an object in one row. The schema is: 0 as object_rid,
     * object_oid, p$$parent_object__rid, p$$parent_object__oid ... The sliced
     * does NOT contain the column object_rid. Instead, it provides rid and oid
     * for the parent object. This format is useful when the calculation of the
     * column object_rid is expensive compared to the calculation of the parent
     * object rid|oid.
     * <p>
     * The 'slicedNonIndexedParentRidOnly' format of a DB_OBJECT contains one
     * object in one row. The schema of a DB_OBJECT with format
     * 'slicedNonIndexed' is: object_objectId, object_referenceId,
     * \u00aba0\u00bb, ..., \u00aban\u00bb$
     */
  static public final String DB_OBJECT_FORMAT_SLICED_NON_INDEXED_PARENT_RID_ONLY = "slicedNonIndexedParentRidOnly";
  
  /**
     * The format 'slicedWithIdAsKey' contains object slices. The schema is:
     * object_id, p$$parent, <user defined attributes>. The primary key is
     * mapped to a single column. Object collections are accessed with the
     * clause object_id LIKE 'reference'/%.
     */
  static public final String DB_OBJECT_FORMAT_SLICED_WITH_ID_AS_KEY = "slicedWithIdAsKey";
  
  /**
    * The format 'slicedWithParentAndIdAsKey' contains object slices. The schema is:
    * object_id, p$$parent, <user defined attributes>. The primary key is mapped
    * to the columns p$$parent and object_id. Object collections are accessed with the
    * clause p$$parent = 'parent path'.
    */
  static public final String DB_OBJECT_FORMAT_SLICED_WITH_PARENT_AND_ID_AS_KEY = "slicedWithParentAndIdAsKey";

  /**
     * The OBJECT_ID_ATTRIBUTES_SUFFIX defines the suffix which is added to
     * attributes which are object id's. Default value is 'objectId'. Typical
     * value is 'oid'.
     */
  static public final String OBJECT_ID_ATTRIBUTES_SUFFIX = "objectIdAttributesSuffix";

  /**
     * The REFERENCE_ID_ATTRIBUTES_SUFFIX defines the suffix which is added to
     * attributes which are reference id's. Default value is 'referenceId'.
     * Typical value is 'rid'.
     */
  static public final String REFERENCE_ID_ATTRIBUTES_SUFFIX = "referenceIdAttributesSuffix";

  /**
     * Attributes which are used privately by the plugin are prefixed with the
     * specified prefix. Default value is 'p$$'.
     */
  static public final String PRIVATE_ATTRIBUTES_PREFIX = "privateAttributesPrefix";

  /**
   * Configures the column name used to store the object slice index. The default is 'object_idx'.
   */
  static public final String OBJECT_IDX_COLUMN = "objectIdxColumn";

  /**
     * PATH_NORMALIZE_LEVEL specifies at what level object values of type path
     * are normalized. The following levels are defined:
     * <ul>
     * <li>0 - none</li>
     * <li>1 - add parent --> adds the attributes p$$object_parent_refId,
     * p$$object_parent_objId</li>
     * <li>2 - normalize object paths --> level 1 plus adds the attributes
     * p$$\u00aba\u00bb_objId, p$$\u00aba\u00bb_refId</li>
     * <li>3 - normalize object parent paths --> level 2 plus adds the
     * attributes p$$\u00aba\u00bb_parent_objId, p$$\u00aba\u00bb_parent_refId</li>
     * </ul>
     */
  static public final String PATH_NORMALIZE_LEVEL = "pathNormalizeLevel";

  /**
     * SUPPORTS_SQL_SEQUENCE is set to true, if database supports SQL sequences
     * for autoincrement columns. Default value is true.
     * 
     * @deprecated the sequence type is auto-detected from now on. The option is
     *             not required any more.
     */
  static public final String SUPPORTS_SQL_SEQUENCE = "supportsSqlSequence";


  /**
     * ALLOWS_SQL_SEQUENCE_FALLBACK is set to true, if plugin is allowed to
     * fallback to supportSqlSequence=false mode automatically. Default value is
     * false.
     */
  static public final String ALLOWS_SQL_SEQUENCE_FALLBACK = "allowsSqlSequenceFallback";

  /**
   * IGNORE_CHECK_FOR_DUPLICATES allows to ignore the check-for-duplicate test on
   * object creation. By default, the persistence plugin asserts that no two objects 
   * with the same identity are created. An object is only created an object retrieval
   * with the same identity throws NOT_FOUND. The check is not performed if the option
   * is set to true. In this case it is strongly recommended to set primary key
   * constraints on the object_id / idx columns.
   */
  static public final String IGNORE_CHECK_FOR_DUPLICATES = "ignoreCheckForDuplicates";
  
  /**
   * Tells whether object ids are able to handle paths
   */
  static public final String USE_NORMALIZED_OBJECT_IDS = "normalizeObjectIds";
  
  /**
     * Returns the smallest defined integer constant or Integer.MAX_VALUE if no
     * integer constant is defined.
     * 
     * @return an int
     */
  static public int min()
  {
  return org.openmdx.application.dataprovider.cci.SharedConfigurationEntries.min();  // delegate
  }



  /**
     * Returns the biggest defined integer constant or Integer.MIN_VALUE if no
     * integer constant is defined.
     * 
     * @return an int
     */
  static public int max()
  {
  return org.openmdx.application.dataprovider.cci.SharedConfigurationEntries.max();  // delegate
  }



  /**
     * Returns a string representation of the passed code
     * 
     * @param code
     *            a code to be stringified
     * @return a stringified code
     */
  static public String toString(int code)
  {
      return org.openmdx.application.dataprovider.cci.SharedConfigurationEntries.toString(code);  // delegate
  }



  /**
     * Returns the code of the passed code's string representation. The string
     * representation is case insensitive.
     * 
     * @exception throws
     *                an <code>IllegalArgumentException</code> if the
     *                stringified code cannot be resolved
     * @param code
     *            a stringified code
     * @return a code
     */
  static public int fromString(String code)
  {  

    return org.openmdx.application.dataprovider.cci.SharedConfigurationEntries.fromString(code);  // delegate
  }

  
  /**
    * SINGLE_VALUE_ATTRIBUTE allows to define single-valued attributes for
    * cases where the attribute multiplicity can not be derived from the model.
    * This is often the case when plugins add implementation dependent
    * attributes (e.g. object_stateId added by state plugin
    */
  static public final String SINGLE_VALUE_ATTRIBUTE = "singleValueAttribute";
  
   /**
     * EMBEDDED_FEATURE[N] allows to embed multi-valued features into the primary dbObject.
     * If the multi-valued feature F is embedded it is stored in the primary table
     * as F_0, ..., F_N. 
     */
   static public final String EMBEDDED_FEATURE = "embeddedFeature";
   
   /**
    * NON_PERSISTENT_FEATURE[N] allows to configure non-persistent features. By default
    * all non-derived features of a class are made persistent. The option allows to 
    * override this default behaviour. The fully qualified model name must be configured.
    */
   static public final String NON_PERSISTENT_FEATURE = "nonPersistentFeature";
   
   /**
     * OBJECT_ID_PATTERN defines the regexp pattern of an object id. If
     * specified, the the pattern is used for the view creation in get and find
     * operations and for generating the where clause in get operations:
     * <ul>
     * <li>without objectIdPattern
     * 
     * <pre>
     *  SELECT v1.*, v2.\u00abmixin attributes\u00bb FROM
     *  (SELECT * FROM \u00abdb object\u00bb) v1,
     *  (SELECT object_oid, object_rid, \u00abmixin attributes\u00bb FROM \u00abdb object\u00bb) v2
     *  WHERE
     *  v1.object_rid = v2.object_rid AND
     *  v1.object_oid = v2.object_oid
     * </pre>
     * 
     * </li>
     * <li>with objectIdPattern
     * 
     * <pre>
     *  SELECT v1.*, v2.\u00abmixin attributes\u00bb FROM
     *  (SELECT * FROM \u00abdb object\u00bb) v1,
     *  (SELECT object_oid, object_rid, \u00abmixin attributes\u00bb FROM \u00abdb object) v2
     *  WHERE
     *  v1.object_rid = v2.object_rid AND
     *  v1.p$$object_oid$0 = v2.p$$object_oid$0 AND v1.p$$object_oid$1 = v2.p$$object_oid$1
     * </pre>
     * 
     * </li>
     * </ul>
     * <p>
     * p$$object_oid$0, $1, etc. represent the object_oid of the underlying
     * \u00abdb object\u00bb parsed into object components.
     */
  static public final String OBJECT_ID_PATTERN = "objectIdPattern";

  /**
     * NULL_AS_CHARACTER defines the CAST expression which is used to produce a
     * NULL string, e.g. CAST(NULL AS CHARACTER). Default value is 'NULL'.
     */
  static public final String NULL_AS_CHARACTER = "nullAsCharacter";
  
  /**
     * FETCH_SIZE allows to set the fetch size for prepared statements. If
     * defined the method ps.setFetchSize(fetchSize) is called. This option can
     * improve dramatically performance of some JDBC drivers. The default is set
     * to 100.
     */
  static public final String FETCH_SIZE = "fetchSize";
  
  /**
     * Path values are stored by default in the path.toUri() format. The path
     * macros allow to modify path values before they are stored. E.g. a path of
     * the form
     * xri:@openmdx:org.openmdx.test.app1/provider/Sliced/segment/Standard/...
     * can be modified to be stored as xri:*app1.Sliced.Standard/... with a
     * macro with name = app1.Sliced.Standard and the value =
     * xri:@openmdx:org.openmdx.test.app1/provider/Sliced/segment/Standard. This
     * feature can be used e.g. for path compression and therefore to save space
     * on database tables.
     * <p>
     * Macro names at the beginning of the path value are of the form xri:*M and
     * (xri:*M) at all other positions.
     * <p>
     * The macro replacement operates on the XRI notation of the path value.
     * <p>
     * The macro name should have the same ordering as the macro value, i.e.
     * m1.value >= m2.value ==> m1.name >= m2.name.
     */
  static public final String PATH_MACRO_NAME = "pathMacroName";
  static public final String PATH_MACRO_VALUE = "pathMacroValue";

  /**
     * String replacement macro definition for a specific column. If the value
     * of the specified column stringMacroColumn is equal to the value
     * stringMacroValue then it is replace with stringMacroName. Multiple
     * replacements can be defined for one column.
     * <p>
     * stringMacroColumn[0] = object_class stringMacroName[0] = CompoundBooking
     * stringMacroValue[0] = org:openmdx:booking1:CompoundBooking
     * <p>
     * stringMacroColumn[0] = object_class stringMacroName[0] = SingleLegBooking
     * stringMacroValue[0] = org:openmdx:booking1:SingleLegBooking
     */
  static public final String STRING_MACRO_COLUMN = "stringMacroColumn";
  static public final String STRING_MACRO_NAME = "stringMacroName";
  static public final String STRING_MACRO_VALUE = "stringMacroValue";
  
  /**
     * maxReferenceComponents defines the maximum number of c$i columns to use
     * in REF tables. c$i columns must be defined as NOT NULL. Unused columns
     * for a specific reference entry are set to ''. This allows to define a
     * unique constraint on the c$i columns.
     */
  static public final String MAX_REFERENCE_COMPONENTS = "maxReferenceComponents";
  
  /**
     * referenceLookupStatementHint allows to define the hint tag which is added
     * to SELECT statements for the REF table access.
     */
  static public final String REFERENCE_LOOKUP_STATEMENT_HINT = "referenceLookupStatementHint";

  /**
     * referenceIdFormat allows to define the format of reference ids.
     * referenceIdFormatRefTable is the default.
     */
  static public final String REFERENCE_ID_FORMAT = "referenceIdFormat";
  
  /**
     * With format refTable reference paths are stored in normalized form. The
     * reference path is stored in a reference table with the following schema:
     * 
     * CREATE TABLE <namespaceId>_REF ( object_rid BIGINT IDENTITY NOT NULL, n
     * INTEGER NOT NULL , c$0 VARCHAR(120) NOT NULL , c$1 VARCHAR(120) NOT NULL,
     * c$2 VARCHAR(120) NOT NULL, c$3 VARCHAR(120) NOT NULL, c$4 VARCHAR(120)
     * NOT NULL, c$5 VARCHAR(120) NOT NULL, c$6 VARCHAR(120) NOT NULL, c$7
     * VARCHAR(120) NOT NULL, c$8 VARCHAR(120) NOT NULL, c$9 VARCHAR(120) NOT
     * NULL, c$10 VARCHAR(120) NOT NULL, c$11 VARCHAR(120) NOT NULL, c$12
     * VARCHAR(120) NOT NULL, c$13 VARCHAR(120) NOT NULL, c$14 VARCHAR(120) NOT
     * NULL, c$15 VARCHAR(120) NOT NULL );
     * 
     * c$i hold the path components, n the number of components. object_rid
     * holds an auto-generated id which is used as foreign key to store the
     * reference.
     */
  static public final String REFERENCE_ID_FORMAT_REF_TABLE = "refTable";
  
  /**
     * With format typeWithPathComponents the reference path is stored as <type
     * name> {"/" c$i}, where <type name> is the name of a configured type
     * pattern matching the reference path and c$i are the path components at
     * the wildcard positions of the type pattern. This format requires
     * pathNormalizeLevel >= 2 for all configured types.
     */
  static public final String REFERENCE_ID_FORMAT_TYPE_WITH_PATH_COMPONENTS = "typeWithPathComponents";
    
  /**
     * useNormalizedReferences allows to specify whether columns holding
     * stringified object references should be used. With pathNormalizeLevel=2
     * references are stored in normalized form in the columns <reference>, p$$<reference
     * rid>, p$$<reference oid>. If set to false, the column <reference> is
     * leading and is used for query and update operations. If set to true, the
     * columns p$$<reference rid>, p$$<reference oid> are leading and used for
     * all DB operations. The default value is false. true requires a
     * pathNormalizeLevel >= 2 for all configured DB objects.
     */
  static public final String USE_NORMALIZED_REFERENCES = "useNormalizedReferences";
  
  /**
   * joinTable, joinColumnEnd1 and joinColumnEnd2 allow to access the configured db object
   * using a join table. The view to access the configured db object is constructed as
   * SELECT T v INNER JOIN joinTable vj ON v.object_id = vj.joinColumnEnd2
   * Normally a find request results (depending on the configured db object type) in
   * SELECT T v WHERE p$$parent = ?. With a join table the find request looks as
   * SELECT T v INNER JOIN joinTable vj ON v.object_id = vj.joinColumnEnd2 WHERE vj.joinColumnEnd1 = ?.
   */
  static public final String JOIN_TABLE = "joinTable";
  static public final String JOIN_COLUMN_END1 = "joinColumnEnd1";
  static public final String JOIN_COLUMN_END2 = "joinColumnEnd2";  
  
  /**
   * The size of multi-valued attributes are stored in their corresponding size columns
   * if the option setSizeColumns is active. The size of a multi-valued attribute stored
   * in column C is stored in column C_. 
   */
  static public final String SET_SIZE_COLUMNS = "setSizeColumns";
  
  /**
     * The type used to store boolean values, i.e. one of
     * <ul>
     * <li><code>BOOLEAN</code> <i>(ignore <code>booleanFalse</code> and
     * <code>booleanTrue</code> configurations)</i>
     * <li><code>CHARACTER</code> <i>(default)</i>
     * <li><code>NUMERIC</code>
     * </ul>
     */
  static public final String BOOLEAN_TYPE = "booleanType";
  static public final String BOOLEAN_TYPE_BOOLEAN = "BOOLEAN";
  static public final String BOOLEAN_TYPE_YN = "YN";
  static public final String BOOLEAN_TYPE_CHARACTER = "CHARACTER";
  static public final String BOOLEAN_TYPE_NUMERIC = "NUMERIC";
  
  /**
   * Stands for the date type specified by the JDBC driver SQL property
   * <code>BOOLEAN.TYPE.STANDARD</code>, i.e. one of<ul>
     * <li><code>BOOLEAN</code>
     * <li><code>YN</code>
     * <li><code>CHARACTER</code>
     * <li><code>NUMERIC</code>
   * </ul>
   * 
   * @see LayerConfigurationEntries#BOOLEAN_TYPE_BOOLEAN
   * @see LayerConfigurationEntries#BOOLEAN_TYPE_YN
   * @see LayerConfigurationEntries#BOOLEAN_TYPE_CHARACTER
   * @see LayerConfigurationEntries#BOOLEAN_TYPE_NUMERIC
   */  
  static public final String BOOLEAN_TYPE_STANDARD = "STANDARD";

  /**
    * The value corresponding to Boolean.FALSE, e.g.
    * <ul>
    * <li><code>false</code> <i>(fix in case of
    * <code>booleanType BOOLEAN</code>
    * <li><code>##false##</code> <i>(default in case of
    * <code>booleanType CHARACTER</code>)</i>
    * <li><code>0</code> <i>(default in case of
    * <code>booleanType NUMERIC</code>)</i>
    * </ul>
    */
  static public final String BOOLEAN_FALSE = "booleanFalse";

  /**
    * The value corresponding to Boolean.TRUE, e.g.
    * <ul>
    * <li><code>true</code> <i>(fix in case of
    * <code>booleanType BOOLEAN</code>
    * <li><code>##true##</code> <i>(default in case of
    * <code>booleanType CHARACTER</code>)</i>
    * <li><code>1</code> <i>(default in case of
    * <code>booleanType NUMERIC</code>)</i>
    * </ul>
    */
  static public final String BOOLEAN_TRUE = "booleanTrue";

  /**
     * The type used to store <code>org::w3c::duration</code> values, i.e. one
     * of
     * <ul>
     * <li><code>INTERVAL</code> <i>(domain defined by the database field
     * definition)</i>
     * <li><code>CHARACTER</code> <i>(default)</i>
     * <li><code>NUMERIC</code> <i>(domain <b>either</b> year-month <b>or</b>
     * day-time intervals!)</i>
     * </ul>
     * 
     * @see LayerConfigurationEntries#DURATION_TYPE_INTERVAL
     * @see LayerConfigurationEntries#DURATION_TYPE_CHARACTER
     * @see LayerConfigurationEntries#DURATION_TYPE_NUMERIC
     */
  static public final String DURATION_TYPE = "durationType";
  
  /**
    * SQL INTERVAL value
    */
  static public final String DURATION_TYPE_INTERVAL = "INTERVAL";
  
  /**
     * Basic ISO 8601 interval value, e.g. "P2Y6M" or "P2DT12H".
     */
  static public final String DURATION_TYPE_CHARACTER = "CHARACTER";
  
  /**
    * There are two flavours
    * <ul>
    * <li>year-month intervals are stored as (integral) numbers of months
    * <li>day-time intervals are stored as (fractional) numbers of seconds
    * </ul>
    */
  static public final String DURATION_TYPE_NUMERIC = "NUMERIC";
  
  /**
    * The type used to store <code>org::w3c::date</code> values, i.e. one of
    * <ul>
    * <li><code>STANDARD</code>
    * <li><code>DATE</code>
    * <li><code>CHARACTER</code> <i>(default)</i>
    * </ul>
    * 
    * @see LayerConfigurationEntries#DATE_TYPE_STANDARD
    * @see LayerConfigurationEntries#DATE_TYPE_DATE
    * @see LayerConfigurationEntries#DATE_TYPE_CHARACTER
    */
  static public final String DATE_TYPE = "dateType";
  
  /**
    * Stands for the date type specified by the JDBC driver SQL property
    * <code>DATE.TYPE.STANDARD</code>, i.e. one of<ul>
    * <li><code>DATE</code>
    * <li><code>CHARACTER</code> <i>(default)</i>
    * </ul>
    * 
    * @see LayerConfigurationEntries#DATE_TYPE_DATE
    * @see LayerConfigurationEntries#DATE_TYPE_CHARACTER
    */
  static public final String DATE_TYPE_STANDARD = "STANDARD";

  /**
    * SQL DATE value
    */
  static public final String DATE_TYPE_DATE = "DATE";
  
  /**
    * Basic ISO 8601 date value, e.g. "19700101"
    */
  static public final String DATE_TYPE_CHARACTER = "CHARACTER";
  
  /**
     * The type used to store <code>org::w3c::dateTime</code> values, i.e. one
     * of
     * <ul>
     * <li><code>STANDARD</code>
     * <li><code>TIMESTAMP</code>
     * <li><code>TIMESTAMP_WITH_TIMEZONE</code>
     * <li><code>CHARACTER</code> <i>(default)</i>
     * <li><code>NUMERIC</code>
     * </ul>
     * 
     * @see LayerConfigurationEntries#DATETIME_TYPE_STANDARD
     * @see LayerConfigurationEntries#DATETIME_TYPE_TIMESTAMP
     * @see LayerConfigurationEntries#DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE
     * @see LayerConfigurationEntries#DATETIME_TYPE_CHARACTER
     * @see LayerConfigurationEntries#DATETIME_TYPE_NUMERIC
     */
  static public final String DATETIME_TYPE = "dateTimeType";

  /**
   * Stands for the datetime type specified by the JDBC driver SQL property
   * <code>DATETIME.TYPE.STANDARD</code>, i.e. one of<ul>
   * <li><code>TIMESTAMP</code>
   * <li><code>TIMESTAMP_WITH_TIMEZONE</code>
   * <li><code>CHARACTER</code> <i>(default)</i>
   * <li><code>NUMERIC</code>
   * </ul>
   * 
   * @see LayerConfigurationEntries#DATETIME_TYPE_TIMESTAMP
   * @see LayerConfigurationEntries#DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE
   * @see LayerConfigurationEntries#DATETIME_TYPE_CHARACTER
   * @see LayerConfigurationEntries#DATETIME_TYPE_NUMERIC
   */
  static public final String DATETIME_TYPE_STANDARD = "STANDARD";

  /**
   * SQL TIMESTAMP value
   */
  static public final String DATETIME_TYPE_TIMESTAMP = "TIMESTAMP";
  
  /**
   * SQL TIMESTAMP WITH TIMEZONE value
   */
  static public final String DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE = "TIMESTAMP_WITH_TIMEZONE";

  /**
     * Basic ISO 8601 dateTime value, e.g. "19700101T000000.000Z"
     */
  static public final String DATETIME_TYPE_CHARACTER = "CHARACTER";
  
  /**
     * Number of seconds that have passed since 1970-01-01 00:00:00.000 UTC
     */
  static public final String DATETIME_TYPE_NUMERIC = "NUMERIC";
  
  /**
     * The type used to store <code>org::w3c::time</code> values, i.e. one of
     * <ul>
     * <li><code>STANDARD</code>
     * <li><code>TIME</code>
     * <li><code>CHARACTER</code> <i>(default)</i>
     * <li><code>NUMERIC</code>
     * </ul>
     * 
     * @see LayerConfigurationEntries#TIME_TYPE_STANDARD
     * @see LayerConfigurationEntries#TIME_TYPE_TIME
     * @see LayerConfigurationEntries#TIME_TYPE_CHARACTER
     * @see LayerConfigurationEntries#TIME_TYPE_NUMERIC
     */
  static public final String TIME_TYPE = "timeType";

  /**
   * Stands for the time type specified by the JDBC driver SQL property
   * <code>TIME.TYPE.STANDARD</code>, i.e. one of<ul>
   * <li><code>TIME</code>
   * <li><code>CHARACTER</code> <i>(default)</i>
   * <li><code>NUMERIC</code>
   * </ul>
   * 
   * @see LayerConfigurationEntries#TIME_TYPE_TIME
   * @see LayerConfigurationEntries#TIME_TYPE_CHARACTER
   * @see LayerConfigurationEntries#TIME_TYPE_NUMERIC
   */
  static public final String TIME_TYPE_STANDARD = "STANDARD";

  /**
   * The time zone used to store <code>org::w3c::dateTime</code> values in
   * case of <code>TIMESTAMP WITH TIMEZONE</code> database fields, e.g.<ul>
   * <li><code>UTC</code>
   * <li><code>GMT+02:00</code>
   * <li><code>Europe/Zurich</code>
   * <li>&#133;</code>
   * </ul>
   * <i>Defaults to <code>TimeZone.getDefault().getID()</code> in 
   * absence of an entry.</i>
   * <p>
   * @see java.util.TimeZone#getDefault()
   */
  static public final String DATETIME_TIMEZONE = "dateTimeZone";
    
  /**
     * SQL TIME value
     */
  static public final String TIME_TYPE_TIME = "TIME";
  
  /**
     * Basic ISO 8601 time value
     */
  static public final String TIME_TYPE_CHARACTER = "CHARACTER";
  
  /**
     * Number of seconds since midnight
     */
  static public final String TIME_TYPE_NUMERIC = "NUMERIC";

  /**
    * The result set type used for prepareStatement. The default is
    * <code>RESULT_SET_TYPE_FORWARD_ONLY</code>. The result set types
    * <code>RESULT_SET_TYPE_SCROLL_INSENSITIVE</code> and
    * <code>RESULT_SET_TYPE_SCROLL_SENSITIVE</code> allow the plugin to use
    * the method ResultSet.absolute(n) for iteration requests in case of
    * non-indexed db objects. This may speed up iteration requests
    * significantly. However, server-side scrolling may not work for all JDBC
    * drivers / databases.
    */
  static public final String RESULT_SET_TYPE = "resultSetType";
  static public final String RESULT_SET_TYPE_FORWARD_ONLY = "forwardOnly";
  static public final String RESULT_SET_TYPE_SCROLL_INSENSITIVE = "scrollInsensitive";
  static public final String RESULT_SET_TYPE_SCROLL_SENSITIVE = "scrollSensitive";

  /**
   * If set to true object caching at unit of work level is enabled. If enabled, retrieved
   * objects (get and find operations) are cached. The cache is cleared at the beginning of
   * a unit of work. 
   */
  static public final String USE_OBJECT_CACHE = "useObjectCache";
  
}
