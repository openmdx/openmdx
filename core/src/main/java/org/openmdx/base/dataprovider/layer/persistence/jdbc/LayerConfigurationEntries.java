/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: LayerConfigurationEntries 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

/**
 * The {@code PersistenceLayerConfigurationEntries} class contains
 * constants identifying the configuration entries of the dataprovider's
 * persistence layer.
 */
public class LayerConfigurationEntries {

  /**
   * Avoid instantiation
   */
  protected LayerConfigurationEntries() {
      super();
  }

  /**
   * The precision used for date/time values since {@code 1970-01-01T00:00:00Z} 
   * <em>(defaults to {@code MICROSECONDS}).</em>
   * <p>
   * The precision used for date/time values before {@code 1970-01-01T00:00:00Z} is
   * {@code MILLISECONDS}.
   * 
   * @see java.util.concurrent.TimeUnit
   */
  static public final String DATE_TIME_PRECISION = "dateTimePrecision";
  
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
     * {@code dbObject.columnName [AS format]}, The database plugin
     * handles autonum colums as follows:
     * <ul>
     * <li>autonum is applied for object slice creation with index 0
     * <li>when a column is specified as autonum and no value is supplied with
     * the create request, a value of the form
     * {@code NEXTVAL(sequence name)} is generated in the
     * {@code VALUES} clause of the {@code INSERT} statement
     * <li>a corresponding sequence with name
     * {@code namespace_columnName_SEQ} must exist. For databases which
     * do not support sequences a table {@code namespace_columnName_SEQ}
     * must exist with column nextval.
     * <li>if the optional format is specified with {@code AS format} a
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
   * EXCLUDE_TYPE list of types. List of exclude types.
   */
  static public final String EXCLUDE_TYPE = "excludeType";

  /**
   * INCLUDE_TYPE list of types. List of include types.
   */
  static public final String INCLUDE_TYPE = "includeType";

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
     * {@code dbObjectForQuery} defines the primary view name of a
     * database object for a the specified type which is used for querying
     * objects. The view must return the single-valued attributes of an object
     * if a secondary object for query is configured. It must return object
     * slices if no secondary object for query is configured.
     */
  static public final String DB_OBJECT_FOR_QUERY = "dbObjectForQuery";

  /**
     * {@code dbObjectForQuery2} defines the optional secondary view
     * name. The view must return the multi-valued attributes of an object.
     */
  static public final String DB_OBJECT_FOR_QUERY_2 = "dbObjectForQuery2";

  /**
     * {@code dbObjectsForQueryJoinColumn} specifies the column which is
     * used to join {@code dbObjectForQuery} and
     * {@code dbObjectForQuery2}. If not specified the default join
     * column is {@code object_id}. The SELECT is constructed as
     * {@code SELECT * FROM 'dbObject' WHERE 'join column' IN (SELECT
   * object_id FROM 'dbObject2' WHERE 'filter criteria')}.
     */
  static public final String DB_OBJECTS_FOR_QUERY_JOIN_COLUMN = "dbObjectsForQueryJoinColumn";
  
  /**
     * The {@code sliced} format of a DB_OBJECT contains an index-slice
     * of an object in one row. The schema of a DB_OBJECT with format 'sliced'
     * is: object_objectId, object_referenceId, object_idx, \u00aba0\u00bb, ...,
     * \u00aban\u00bb$
     */
  static public final String DB_OBJECT_FORMAT_SLICED = "sliced";
  
  /**
   * Optimizes slicing with separate multi-value tables.
   * Empty slices are unnecessary,
   */
  static public final String DB_OBJECT_FORMAT_SLICED2 = "sliced2";

  /**
     * The 'slicedNonIndexed' format of a DB_OBJECT contains one object in one
     * row. The schema of a DB_OBJECT with format 'slicedNonIndexed' is:
     * object_objectId, object_referenceId, \u00aba0\u00bb, ..., \u00aban\u00bb$
     */
  static public final String DB_OBJECT_FORMAT_SLICED_NON_INDEXED = "slicedNonIndexed";

  /**
   * The 'slicedNonIndexed' format of a DB_OBJECT contains one object in one
   * row. The schema of a DB_OBJECT with format 'slicedNonIndexed' is:
   * object_objectId, object_referenceId, \u00aba0\u00bb, ..., \u00aban\u00bb$
   */
 static public final String DB_OBJECT_FORMAT_SLICED2_NON_INDEXED = "sliced2NonIndexed";

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
  static public final String DB_OBJECT_FORMAT_SLICED2_PARENT_RID_ONLY = "sliced2ParentRidOnly";

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
  static public final String DB_OBJECT_FORMAT_SLICED2_NON_INDEXED_PARENT_RID_ONLY = "sliced2NonIndexedParentRidOnly";
  
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
     * attributes which are object id's. Default value is '_objectId'. Typical
     * value is '_oid'.
     */
  static public final String OBJECT_ID_ATTRIBUTES_SUFFIX = "objectIdAttributesSuffix";

  /**
     * The REFERENCE_ID_ATTRIBUTES_SUFFIX defines the suffix which is added to
     * attributes which are reference id's. Default value is '_referenceId'.
     * Typical value is '_rid'.
     */
  static public final String REFERENCE_ID_ATTRIBUTES_SUFFIX = "referenceIdAttributesSuffix";

  /**
   * The REFERENCE_ID_SUFFIX_ATTRIBUTES_SUFFIX defines the suffix which is added to
   * attributes which are reference id suffixes. Default value is '_referenceIdSuffix'.
   * Typical value is '_rsx'.
   */
static public final String REFERENCE_ID_SUFFIX_ATTRIBUTES_SUFFIX = "referenceIdSuffixAttributesSuffix";
  
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
     * p$$&lang;a&rang;_objId, p$$&lang;a&rang;_refId</li>
     * <li>3 - normalize object parent paths --> level 2 plus adds the
     * attributes p$$&lang;a&rang;_parent_objId, p$$&lang;a&rang;_parent_refId</li>
     * </ul>
     */
  static public final String PATH_NORMALIZE_LEVEL = "pathNormalizeLevel";

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
     * specified, the pattern is used for the view creation in get and find
     * operations and for generating the where clause in get operations:
     * <ul>
     * <li>without objectIdPattern
     * 
     * <pre>
     *  SELECT v1.*, v2.&lang;mixin attributes&rang; FROM
     *  (SELECT * FROM &lang;db object&rang;) v1,
     *  (SELECT object_oid, object_rid, &lang;mixin attributes&rang; FROM &lang;db object&rang;) v2
     *  WHERE
     *  v1.object_rid = v2.object_rid AND
     *  v1.object_oid = v2.object_oid
     * </pre>
     * 
     * </li>
     * <li>with objectIdPattern
     * 
     * <pre>
     *  SELECT v1.*, v2.&lang;mixin attributes&rang; FROM
     *  (SELECT * FROM &lang;db object&rang;) v1,
     *  (SELECT object_oid, object_rid, &lang;mixin attributes&rang; FROM &lang;db object) v2
     *  WHERE
     *  v1.object_rid = v2.object_rid AND
     *  v1.p$$object_oid$0 = v2.p$$object_oid$0 AND v1.p$$object_oid$1 = v2.p$$object_oid$1
     * </pre>
     * 
     * </li>
     * </ul>
     * <p>
     * p$$object_oid$0, $1, etc. represent the object_oid of the underlying
     * &lang;db object&rang; parsed into object components.
     */
  static public final String OBJECT_ID_PATTERN = "objectIdPattern";

  /**
   * Reference id pattern
   * <p>
   * Format: a white space separated list of 
   * {@code &lt;feature-name&gt;=&lt;reference-id-pattern&gt;} entries, 
   * e.g.<ul>
   * <li>{@code beforeImage=^([^.]+)[.](.+)$ object=^([^.]+)[.](.+)$}
   * </ul>
   */
  static public final String REFERENCE_ID_PATTERN = "referenceIdPattern";
  
  /**
     * NULL_AS_CHARACTER defines the CAST expression which is used to produce a
     * NULL string, e.g. CAST(NULL AS CHARACTER). Default value is 'NULL'.
     */
  static public final String NULL_AS_CHARACTER = "nullAsCharacter";
  
  /**
   * Defines how many result objects are included in each reply
   * if FetchPlan.FETCH_SIZE_OPTIMAL (0) is requested. Default value is 100.
   */
  static public final String OPTIMAL_FETCH_SIZE = "optimalFetchSize";

  /**
   * Defines how many rows are are returned at once to the database result 
   * set. A value of 0, the default, lets the database driver guess an optimal 
   * number.
   */
  static public final String ROW_BATCH_SIZE = "rowBatchSize";

  /**
   * Defines for how many objects the multi-value attributes are retrieved at 
   * once. Default value is 100.
   */
  static public final String OBJECT_BATCH_SIZE = "objectBatchSize";
  
  /**
   * If the result set is larger than result set limit and either 
   * FETCH_SIZE_GREEDY (0) or a fetch size greater than the result set limit 
   * has been requested then a TOO_LARGE_RESULT_SET exception is thrown.
   * Default value is 10000.
   */
  static public final String RESULT_SET_LIMIT = "resultSetLimit";
  
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
     * stringMacroColumn[0] = object_class 
     * stringMacroName[0] = CompoundBooking
     * stringMacroValue[0] = org:openmdx:booking1:CompoundBooking
     * <p>
     * stringMacroColumn[0] = object_class 
     * stringMacroName[0] = SingleLegBooking
     * stringMacroValue[0] = org:openmdx:booking1:SingleLegBooking
     */
  static public final String STRING_MACRO_COLUMN = "stringMacroColumn";
  static public final String STRING_MACRO_NAME = "stringMacroName";
  static public final String STRING_MACRO_VALUE = "stringMacroValue";
  
  /**
     * referenceIdFormat allows to define the format of reference ids.
     * referenceIdFormatRefTable is the default.
     */
  static public final String REFERENCE_ID_FORMAT = "referenceIdFormat";
  
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
     * <li>{@code BOOLEAN} <i>(ignore {@code booleanFalse} and {@code booleanTrue} configurations)</i>
     * <li>{@code CHARACTER} <i>(default)</i>
     * <li>{@code NUMERIC}
     * </ul>
     */
  static public final String BOOLEAN_TYPE = "booleanType";
  static public final String BOOLEAN_TYPE_BOOLEAN = "BOOLEAN";
  static public final String BOOLEAN_TYPE_YN = "YN";
  static public final String BOOLEAN_TYPE_CHARACTER = "CHARACTER";
  static public final String BOOLEAN_TYPE_NUMERIC = "NUMERIC";
  
  /**
   * Stands for the date type specified by the JDBC driver SQL property
   * {@code BOOLEAN.TYPE.STANDARD}, i.e. one of<ul>
     * <li>{@code BOOLEAN}
     * <li>{@code YN}
     * <li>{@code CHARACTER}
     * <li>{@code NUMERIC}
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
    * <li>{@code false} <i>(fix in case of
    * {@code booleanType BOOLEAN}
    * <li>{@code ##false##} <i>(default in case of
    * {@code booleanType CHARACTER})</i>
    * <li>{@code 0} <i>(default in case of
    * {@code booleanType NUMERIC})</i>
    * </ul>
    */
  static public final String BOOLEAN_FALSE = "booleanFalse";

  /**
    * The value corresponding to Boolean.TRUE, e.g.
    * <ul>
    * <li>{@code true} <i>(fix in case of
    * {@code booleanType BOOLEAN}
    * <li>{@code ##true##} <i>(default in case of
    * {@code booleanType CHARACTER})</i>
    * <li>{@code 1} <i>(default in case of
    * {@code booleanType NUMERIC})</i>
    * </ul>
    */
  static public final String BOOLEAN_TRUE = "booleanTrue";

  /**
     * The type used to store {@code org::w3c::duration} values, i.e. one
     * of
     * <ul>
     * <li>{@code STANDARD}  <i>(default)</i>
     * <li>{@code INTERVAL} <i>(domain defined by the database field definition)</i>
     * <li>{@code CHARACTER}
     * <li>{@code NUMERIC} <i>(domain <b>either</b> year-month <b>or</b> day-time intervals!)</i>
     * </ul>
     * 
     * @see LayerConfigurationEntries#DURATION_TYPE_STANDARD
     * @see LayerConfigurationEntries#DURATION_TYPE_INTERVAL
     * @see LayerConfigurationEntries#DURATION_TYPE_CHARACTER
     * @see LayerConfigurationEntries#DURATION_TYPE_NUMERIC
     */
  static public final String DURATION_TYPE = "durationType";
  
  /**
   * Stands for the date type specified by the JDBC driver SQL property
   * {@code DURATION.TYPE.STANDARD}, i.e. one of<ul>
   * <li>{@code INTERVAL}
   * <li>{@code CHARACTER} 
   * <li>{@code NUMERIC}
   * </ul>
   * 
   * @see LayerConfigurationEntries#DATE_TYPE_DATE
   * @see LayerConfigurationEntries#DATE_TYPE_CHARACTER
   * 
   * Note:<br>
   * <em>Will be supported after the resolution if issue #58, only!</em>
   */
  static public final String DURATION_TYPE_STANDARD = "STANDARD";

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
    * The type used to store {@code org::w3c::date} values, i.e. one of
    * <ul>
    * <li>{@code STANDARD}
    * <li>{@code DATE}
    * <li>{@code CHARACTER} <i>(default)</i>
    * </ul>
    * 
    * @see LayerConfigurationEntries#DATE_TYPE_STANDARD
    * @see LayerConfigurationEntries#DATE_TYPE_DATE
    * @see LayerConfigurationEntries#DATE_TYPE_CHARACTER
    */
  static public final String DATE_TYPE = "dateType";
  
  /**
    * Stands for the date type specified by the JDBC driver SQL property
    * {@code DATE.TYPE.STANDARD}, i.e. one of<ul>
    * <li>{@code DATE}
    * <li>{@code CHARACTER} <i>(default)</i>
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
     * The type used to store {@code org::w3c::dateTime} values, i.e. one
     * of
     * <ul>
     * <li>{@code STANDARD} <i>(default)</i>
     * <li>{@code TIMESTAMP}
     * <li>{@code TIMESTAMP_WITH_TIMEZONE}
     * <li>{@code CHARACTER}
     * <li>{@code NUMERIC}
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
   * {@code DATETIME.TYPE.STANDARD}, i.e. one of<ul>
   * <li>{@code TIMESTAMP}
   * <li>{@code TIMESTAMP_WITH_TIMEZONE}
   * <li>{@code CHARACTER} <i>(default)</i>
   * <li>{@code NUMERIC}
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
     * The type used to store {@code org::w3c::time} values, i.e. one of
     * <ul>
     * <li>{@code STANDARD}
     * <li>{@code TIME}
     * <li>{@code CHARACTER} <i>(default)</i>
     * <li>{@code NUMERIC}
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
   * {@code TIME.TYPE.STANDARD}, i.e. one of<ul>
   * <li>{@code TIME}
   * <li>{@code CHARACTER} <i>(default)</i>
   * <li>{@code NUMERIC}
   * </ul>
   * 
   * @see LayerConfigurationEntries#TIME_TYPE_TIME
   * @see LayerConfigurationEntries#TIME_TYPE_CHARACTER
   * @see LayerConfigurationEntries#TIME_TYPE_NUMERIC
   */
  static public final String TIME_TYPE_STANDARD = "STANDARD";

  /**
   * The time zone used to store {@code org::w3c::dateTime} values in
   * case of {@code TIMESTAMP WITH TIMEZONE} database fields, e.g.<ul>
   * <li>{@code UTC}
   * <li>{@code GMT+02:00}
   * <li>{@code Europe/Zurich}
   * <li>&#133;}
   * </ul>
   * <i>Defaults to {@code TimeZone.getDefault().getID()} in 
   * absence of an entry.</i>
   * <p>
   * @see java.util.TimeZone#getDefault()
   */
  static public final String DATETIME_TIMEZONE = "dateTimeZone";

  /**
   * The daylight saving time zone used to store {@code org::w3c::dateTime} values in
   * case of {@code TIMESTAMP WITH TIMEZONE} database fields, e.g.<ul>
   * <li>{@code Europe/Zurich CEST}
   * <li>{@code US/Eastern EDT}
   * <li>&#133;}
   * </ul>
   * <i>Defaults to the same value as DATETIME_TIMEZONE in absence of an entry leading 
   * to incorrect values when switching from daylight saving time back to standard time</i>
   * <p>
   * @see #DATETIME_TIMEZONE
   */
  static public final String DATETIME_DST_TIMEZONE = "dateTimeDaylightZone";
  
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
    * {@code RESULT_SET_TYPE_FORWARD_ONLY}. The result set types
    * {@code RESULT_SET_TYPE_SCROLL_INSENSITIVE} and
    * {@code RESULT_SET_TYPE_SCROLL_SENSITIVE} allow the plugin to use
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
   * Defines whether "instance of state" filter properties may be substituted by
   * core filter properties.
   */
  static public final String DISABLE_STATE_FILTER_SUBSTITUATION = "disableStateFilterSubstitution";

  /**
   * Tells whether views shall be used to retrieve redundant columns.<br>
   * Defaults to {@code true}.
   */
  static public final String USE_VIEWS_FOR_REDUNDANT_COLUMNS = "useViewsForRedundantColumns";
  
  /**
   * Tells whether the preferences table shall be used to amend the configuration.<br>
   * Defaults to {@code true}.
   */
  static public final String USE_PREFERENCES_TABLE = "usePreferencesTable";

  /**
   * If views are used for redundant columns and a removable reference id prefix is specified then
   * the suffix is put into the object's reference id suffix column.
   */
  static public final String REMOVABLE_REFERENCE_ID_PREFIX = "removableReferenceIdPrefix";

  /**
   * Do not try absolute positioning for the given DB object
   */
  static public final String DISABLE_ABSOLUTE_POSITIONING = "disableAbsolutePositioning"; 
      
  /**
   * The unit of work's parent RID is set if its provider is specified
   */
  static public final String UNIT_OF_WORK_PROVIDER = "unitOfWorkProvider";
  
  /**
   * The flag tells whether large object input streams shall be copied into memory
   * Defaults to {@code true}.
   */
  static public final String GET_LARGE_OBJECT_BY_VALUE = "getLargeObjectByValue";

  /**
   * Tells whether descendants shall be deleted by the persistence layer.<br>
   * Defaults to {@code true}.
   */
  static public final String CASCADE_DELETES = "cascadeDeletes";

  /*
   * Order nulls as empty string when ordering. If true, ORDER BY ... ASC is 
   * expanded to ORDER BY ... ASC NULLS FIRST. if false, ORDER BY ... DESC
   * is expanded to ORDER BY ... DESC NULLS LAST.
   * Defaults to {@code false}
   */
  static public final String ORDER_NULLS_AS_EMPTY = "orderNullsAsEmpty";
  
}
