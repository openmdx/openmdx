/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: State_1Iterator.java,v 1.1 2009/05/26 14:31:21 wfro Exp $
 * Description: JDBC Iterator for find requests
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/26 14:31:21 $
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
package org.openmdx.application.dataprovider.layer.model;

import org.openmdx.application.dataprovider.layer.persistence.common.AbstractIterator;
import org.openmdx.base.naming.Path;

/**
 * JdbcIterator
 *
 * Stores the status of an JDBC SQL iterator. Stored is the prepared statement
 * and the parameters (values) for the prepared statement.
 */
class State_1Iterator
  extends AbstractIterator {

  /**
     * 
     */
    private static final long serialVersionUID = 3907211533342618165L;
private final String _idCompletion;
  private final boolean _isStateful;
  private final Path _internalRequestPath;
  private final byte[] _iterator;
  
  //---------------------------------------------------------------------------
  State_1Iterator(
    boolean isStateful, 
    String idCompletion, 
    Path internalRequestPath,
    byte[] iterator
  ) { 
    _isStateful = isStateful;
    _idCompletion = idCompletion;
    _iterator = iterator;
    _internalRequestPath = internalRequestPath;
  }
  
  State_1Iterator(
    boolean isStateful, 
    String idCompletion, 
    byte[] iterator
  ) { 
    _isStateful = isStateful;
    _idCompletion = idCompletion;
    _iterator = iterator;
    _internalRequestPath = new Path("dummy"); 
  }
 
  /**
    * @return
   */
  public boolean isStateful() {
    return _isStateful;
  }
    
  public String getIdCompletion() {
    return _idCompletion;
  }
  
  public byte[] getIterator() {
    return _iterator;
  }
  
  public Path getInternalRequestPath() {
      return _internalRequestPath;
  }
}