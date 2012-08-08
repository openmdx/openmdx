/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Database_1_Attributes.java,v 1.7 2009/01/27 18:55:56 wfro Exp $
 * Description: Database_1_Attributes 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/27 18:55:56 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc;


/**
 * Database_1_Attributes
 *
 */
public class Database_1_Attributes {

  /**
   * QueryFilter class.
   */
  public static final String QUERY_FILTER_CLASS = "org:openmdx:compatibility:datastore1:QueryFilter";

  /**
   * Query filter clause.
   */
  public static final String QUERY_FILTER_CLAUSE = "clause";

  /**
   * Query filter string parameters.
   */
  public static final String QUERY_FILTER_STRING_PARAM = "stringParam";

  /**
   * Query filter integer parameters.
   */
  public static final String QUERY_FILTER_INTEGER_PARAM = "integerParam";

  /**
   * Query filter boolean parameters.
   */
  public static final String QUERY_FILTER_BOOLEAN_PARAM = "booleanParam";

  /**
   * Query filter decimal parameters.
   */
  public static final String QUERY_FILTER_DECIMAL_PARAM = "decimalParam";

  /**
   * Query filter date parameters.
   */
  public static final String QUERY_FILTER_DATE_PARAM = "dateParam";

  /**
   * Query filter dateTime parameters.
   */
  public static final String QUERY_FILTER_DATETIME_PARAM = "dateTimeParam";
  
  /**
   * The hint allows to activate counting of the result set. To be activated
   * the query filter clause must include this hint, 
   * i.e. queryFilterClause.indexOf(HINT_COUNT) >= 0.
   */
  public static final String HINT_COUNT = "/*!COUNT*/";
  
  /**
   * The hint allows to specify a list of columns which are included in the 
   * result set. By default all columns are returned using SELECT v.* FROM T v ...,
   * i.e. the default column selector is v.*. This option allows to minimize
   * the by byte count of the result set or to add additional columns.
   */
  public static final String HINT_COLUMN_SELECTOR = "/*!COLUMNS ";

  /**
   * The hint allows to specify a list of columns which are added to the 
   * ORDER BY clause. 
   */
  public static final String HINT_ORDER_BY = "/*!ORDER BY ";
  
  /**
   * Hint for qualifying the dbObject used in queries. The format can be one of the following
   * <ul>
   *   <li>string not starting with 'xri:' in this case the string is interpreted as
   *       suffix and appended to the query object.
   *   <li>string starting with 'xri:' in this case the string is interpreted as Path. The
   *       path is mapped to its dbObject and dbObject.getTableName() is appended to the
   *       query object.
   */
  public static final String HINT_DBOBJECT = "/*!DBOBJECT ";
  
}
