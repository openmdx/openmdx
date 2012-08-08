/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JdbcIterator.java,v 1.11 2009/01/06 13:14:44 wfro Exp $
 * Description: JDBC Iterator for find requests
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 13:14:44 $
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

import java.util.Arrays;
import java.util.List;

import org.openmdx.base.naming.Path;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.common.AbstractIterator;

/**
 * JdbcIterator
 *
 * Stores the status of an JDBC SQL iterator. Stored is the prepared statement
 * and the parameters (values) for the prepared statement.
 */
@SuppressWarnings("unchecked")
class JdbcIterator
  extends AbstractIterator {

  /**
     * 
     */
    private static final long serialVersionUID = 3258411720581003061L;
//---------------------------------------------------------------------------
  public JdbcIterator(
    Path referencedType,
    String statement,
    List statementParameters,
    int lastPosition,
    int lastRowCount
  ) { 
    this.referencedType = referencedType;
    this.statement = statement;
    this.statementParameters = statementParameters.toArray(
      new Object[statementParameters.size()]
    );
    this.lastPosition = lastPosition;
    this.lastRowCount = lastRowCount;
  }
 
  //---------------------------------------------------------------------------
  public Path getReferencedType(
  ) {
    return this.referencedType;
  }

  //---------------------------------------------------------------------------
  public String getStatement(
  ) {
    return this.statement;
  }

  //---------------------------------------------------------------------------
  public List getStatementParameters(
  ) {
    return Arrays.asList(
      this.statementParameters
    );
  }

  //---------------------------------------------------------------------------
  public int getLastPosition(
  ) {
    return this.lastPosition;
  }

  //---------------------------------------------------------------------------
  public int getLastRowCount(
  ) {
    return this.lastRowCount;
  }

  //---------------------------------------------------------------------------
  // Variables
  //---------------------------------------------------------------------------
  private final Path referencedType;
  private final String statement;
  private final Object[] statementParameters;
  private final int lastPosition;
  private final int lastRowCount;

}

//--- End of File -----------------------------------------------------------
