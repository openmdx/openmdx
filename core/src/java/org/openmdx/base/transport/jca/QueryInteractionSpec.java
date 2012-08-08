/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: QueryInteractionSpec.java,v 1.7 2007/03/07 09:26:37 wfro Exp $
 * Description: RemoveInteractionSpec
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/07 09:26:37 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006-2007, OMEX AG, Switzerland
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
package org.openmdx.base.transport.jca;

/**
 * Execute query on receiving persistence manager with the parameters supplied on the query 
 * object returned by <code>getQuery()</code>.
 * 
 * <ul>
 *   <li>Input: IndexedRecord with two elements:
 *     <ol>
 *       <li><code>IndexedRecord</code> of predicates
 *       <li><code>IndexedRecord</code> of parameters. A parameter are either of primitive type or <code>IndexedRecord</code>
 *     </ol>
 *   <li>Result: query result as <code>IndexedRecord</code> of features
 * </ul>
 */
public class QueryInteractionSpec
    extends OpenMdxInteractionSpec {

    public QueryInteractionSpec(
    ) {
        this.setFunctionName(
            this.getClass().getName()
        );
    }
    
    public QueryInteractionSpec(
        int fetchSize,
        int rangeFrom,
        int rangeTo      
    ) {
        this.setFunctionName(
            this.getClass().getName()
        );
        this.setFetchSize(fetchSize);
        this.setRangeFrom(rangeFrom);
        this.setRangeTo(rangeTo);
    }
    
    public QueryInteractionSpec(
        boolean deletePersistent,
        Integer fetchSize,
        Integer rangeFrom,
        Integer rangeTo      
    ) {
        this.setFunctionName(
            this.getClass().getName()           
        );
        this.setDeletePersistent(deletePersistent);
        this.setFetchSize(fetchSize);
        this.setRangeFrom(rangeFrom);
        this.setRangeTo(rangeTo);
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    
}