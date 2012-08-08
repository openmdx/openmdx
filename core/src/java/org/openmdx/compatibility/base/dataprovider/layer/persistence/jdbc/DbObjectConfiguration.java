/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DbObjectConfiguration.java,v 1.15 2009/01/13 23:51:12 wfro Exp $
 * Description: TypeConfigurationEntry class
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 23:51:12 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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

import java.util.List;
import java.util.regex.Pattern;

import javax.resource.ResourceException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;

//---------------------------------------------------------------------------
/**
 * Holder for a type configuration. A type is defined by the dbObject,
 * dbObjectFormat and pathNormalizeLevel. dbObject defines the table or
 * view name used to update and query objects. The dbObjectFormat specifies
 * the schema type of the table. 'slice' contains an index-slice of an
 * object per row, 'generic' contains one attribute value per row.
 * 'pathNormalizeLevel' is a write-only attribute and defines at what 
 * level attribute values of type path should be normalized.
 */
@SuppressWarnings("unchecked")
public class DbObjectConfiguration {
    
  //-------------------------------------------------------------------------
  DbObjectConfiguration(
    Path type,
    String typeName,
    String dbObject1,
    String dbObject2,
    String dbObjectFormat,
    String dbObjectForQuery1,
    String dbObjectForQuery2,
    String dbObjectsForQueryJoinColumn,
    int pathNormalizeLevel,
    String dbObjectHint,
    String objectIdPattern,
    List autonumColumns,
    String joinTable,
    String joinColumnEnd1,
    String joinColumnEnd2
  ) throws ServiceException { 

    // need an object path
    if(type.size() % 2 == 0) {
        this.type = type.getChild(":*");
    }
    else {
      this.type = type;
    }
    this.typeName = typeName;
    this.dbObject1 = (dbObject1 == null) || (dbObject1.length() == 0)
        ? null 
        : dbObject1;    
    this.dbObject2 = (dbObject2 == null) || (dbObject2.length() == 0)
        ? null 
        : dbObject2;    
    this.dbObjectFormat = dbObjectFormat;
    this.dbObjectForQuery1 = (dbObjectForQuery1 == null) || (dbObjectForQuery1.length() == 0) 
        ? null 
        : dbObjectForQuery1;
    this.dbObjectForQuery2 = (dbObjectForQuery2 == null) || (dbObjectForQuery2.length() == 0)
        ? null 
        : dbObjectForQuery2;    
    this.dbObjectsForQueryJoinColumn = dbObjectsForQueryJoinColumn;
    this.pathNormalizeLevel = pathNormalizeLevel;
    this.dbObjectHint = dbObjectHint;
    this.objectIdPattern = objectIdPattern;
    this.autonumColumns = autonumColumns;
    this.joinCriteria = (joinTable == null) || (joinColumnEnd1 == null) || (joinColumnEnd2 == null)
        ? null
        : new String[]{joinTable, joinColumnEnd1, joinColumnEnd2};
    this.objectIdComponents = 0;
    if(objectIdPattern != null) {
      this.objectIdPatternMatcher = Pattern.compile(objectIdPattern);
      for(int i = 0; i < objectIdPattern.length(); i++) {
        if('(' == objectIdPattern.charAt(i)) {
          this.objectIdComponents++;
        }
      }      
    }
    else {
      this.objectIdPatternMatcher = null;
    }                          
  }
  
    //---------------------------------------------------------------------------
    public String toString(
    ) {
        try {
            return Records.getRecordFactory().asMappedRecord(
                this.type.toString(), // recordName, 
                null, // recordShortDescription
                TO_STRING_FIELDS,
                new Object[]{this.typeName,this.dbObject1,this.dbObject2,this.dbObjectFormat,this.dbObjectForQuery1,this.dbObjectForQuery2,new Integer(this.pathNormalizeLevel),this.dbObjectHint,this.objectIdPattern,this.autonumColumns}
            ).toString();
        } catch (ResourceException exception) {
            return super.toString();
        }
    }

    
  //---------------------------------------------------------------------------
  public Path getType(
  ) {
    return this.type;
  }

  //---------------------------------------------------------------------------
  public String getTypeName(
  ) {
      return this.typeName;      
  }
  
  //---------------------------------------------------------------------------
  public String getDbObjectForUpdate1(
  ) {
    return this.dbObject1;
  }
  
  //---------------------------------------------------------------------------
  public String getDbObjectForUpdate2(
  ) {
    return this.dbObject2;
  }
  
  //---------------------------------------------------------------------------
  public String getDbObjectFormat(
  ) {
    return this.dbObjectFormat;
  }
  
  //---------------------------------------------------------------------------
  public String getDbObjectHint(
  ) {
    return this.dbObjectHint;
  }
  
  //---------------------------------------------------------------------------
  public String getObjectIdPattern(
  ) {
    return this.objectIdPattern;
  }

  //---------------------------------------------------------------------------
  public Pattern getObjectIdPatternMatcher(
  ) {
    return this.objectIdPatternMatcher;
  }

  //---------------------------------------------------------------------------
  public int getObjectIdComponents(
  ) {
    return this.objectIdComponents;
  }

  //---------------------------------------------------------------------------
  /**
   * The primary and the optional secondary view allow to split an object to two 
   * different tables. If no secondary view is specified the primary view must
   * return all object slices containing single- and multi-valued columns. If a 
   * secondary view is specified, the primary view must return all single-valued 
   * columns where the secondary all multi-valued columns. 
   */
  public String getDbObjectForQuery1(
  ) {
    return this.dbObjectForQuery1;
  }
  
  //---------------------------------------------------------------------------
  public String getDbObjectForQuery2(
  ) {
    return this.dbObjectForQuery2;
  }
  
  //---------------------------------------------------------------------------
  public String getDbObjectsForQueryJoinColumn(
  ) {
      return this.dbObjectsForQueryJoinColumn;
  }
  
  //---------------------------------------------------------------------------
  public int getPathNormalizeLevel(
  ) {
    return this.pathNormalizeLevel;
  }
  
  //---------------------------------------------------------------------------
  public List getAutonumColumns(
  ) {
    return this.autonumColumns;
  }
  
  //---------------------------------------------------------------------------
  public String[] getJoinCriteria(
  ) {
      return this.joinCriteria;
  }
  
  //---------------------------------------------------------------------------
  // Variables
  //---------------------------------------------------------------------------
  private final Path type;
  private final String typeName;
  private final String dbObject1;
  private final String dbObject2;
  private final String dbObjectFormat;
  private final String dbObjectForQuery1;
  private final String dbObjectForQuery2;
  private final String dbObjectsForQueryJoinColumn;
  private final int pathNormalizeLevel;
  private final String dbObjectHint;
  private final String objectIdPattern;
  private final Pattern objectIdPatternMatcher;
  private int objectIdComponents;
  private final List autonumColumns;
  private final String[] joinCriteria;

  private static final String[] TO_STRING_FIELDS = {
      "typeName","dbObject","dbObject2","dbObjectFormat","dbObjectForQuery","dbObjectForQuery2","pathNormalizeLevel","dbObjectHint","objectIdPattern","autonumColumns"
  };
}

//--- End of File -----------------------------------------------------------
