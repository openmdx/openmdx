/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject;

import java.sql.Connection;
import java.sql.SQLException;

import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.FastResultSet;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * This class implements a non-indexed sliced db object. If indexing
 * is not required then this type may improve performance on queries
 * dramatically. This is often the case with user-defined views.
 */
public class SlicedDbObjectNonIndexed 
  extends SlicedDbObject {

//-------------------------------------------------------------------------
  public SlicedDbObjectNonIndexed(
    Database_1_0 database, 
    Connection conn,
    DbObjectConfiguration dbObjectConfiguratioin, 
    Path accessPath,
    boolean isExtent,
    boolean isQuery
  ) throws ServiceException {
    super(
      database, 
      conn, 
      dbObjectConfiguratioin, 
      accessPath, 
      isExtent,
      isQuery
    );
    // non index column for non-indexed sliced DB objects
    this.indexColumn = null;
    this.excludeAttributes.add("objectIdx");
  }

  //-------------------------------------------------------------------------
  public SlicedDbObjectNonIndexed(
    Database_1_0 database, 
    Connection conn,
    DbObjectConfiguration dbObjectConfiguration
  ) {
    super(
      database,
      conn, 
      dbObjectConfiguration
    );
    // non index column for non-indexed sliced DB objects
    this.indexColumn = null;
    this.excludeAttributes.add("objectIdx");
  }
  
  //-------------------------------------------------------------------------
  @Override
  public int getIndex(
    FastResultSet frs
  ) throws SQLException {
      return 0;
  }

  //-------------------------------------------------------------------------
  // Members
  //-------------------------------------------------------------------------
  private static final long serialVersionUID = 3257849870223226167L;
  
}

//--- End of File -----------------------------------------------------------
