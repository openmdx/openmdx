/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: InMemoryIterator.java,v 1.1 2009/05/26 14:31:21 wfro Exp $
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
package org.openmdx.application.dataprovider.layer.persistence.none;

import org.openmdx.application.dataprovider.layer.persistence.common.AbstractIterator;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.FilterProperty;

/**
 * JdbcIterator
 *
 * Stores the status of an JDBC SQL iterator. Stored is the prepared statement
 * and the parameters (values) for the prepared statement.
 */
class InMemoryIterator
  extends AbstractIterator {

  /**
     * 
     */
    private static final long serialVersionUID = 3905236814703769655L;
private final AttributeSpecifier[] attributeSpecifier;
  private final FilterProperty[] attributeFilter;

  //---------------------------------------------------------------------------
  InMemoryIterator(
    FilterProperty[] attributeFilter,
    AttributeSpecifier[] attributeSpecifier
  ) { 
    this.attributeFilter = attributeFilter;
    this.attributeSpecifier = attributeSpecifier;
  }
 
    /**
     */
    FilterProperty[] getAttributeFilter() {
        return attributeFilter;
    }
    
    /**
     */
    AttributeSpecifier[] getAttributeSpecifier() {
        return attributeSpecifier;
    }

}
