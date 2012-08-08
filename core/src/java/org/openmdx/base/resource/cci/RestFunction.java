/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RestFunction.java,v 1.8 2010/02/16 18:39:10 hburger Exp $
 * Description: REST Functions
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/02/16 18:39:10 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.base.resource.cci;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.persistence.cci.Queries;

/**
 * REST Functions
 * <p>
 * The function either returns a record or updates a given output record<ul>
 * <li><b>Return Record</b><br>
 * The <code>execute()</code> method with a return record can be applied
 * to interactions belonging to either <em>remote</em> or <em>local</em> connections.<br>
 * The return record<ul>
 * <li>is <code>null</code> in case of {@link InteractionSpec#SYNC_SEND}<br>
 * <li>is an indexed record containing either <code>org:openmdx:kernel:Object</code> 
 * records or operation results.
 * </ul>
 * <li><b>Output Record</b><br>
 * The <code>execute()</code> method with an output record can be applied
 * only to interactions belonging to <em>local</em> connections.<br>
 * The output record
 * <ul>
 * <li>is ignored in case of {@link InteractionSpec#SYNC_SEND}, 
 * i.e. it can even be <code>null</code>
 * <li>must be an indexed record in case of {@link InteractionSpec#SYNC_SEND}<br>
 * which is modified by <code>add()</code> operations for either
 * <code>org:openmdx:kernel:Object</code> records or operation results.
 * </ul>
 * <li><b>Object Holder</b><br>
 * Each <code>org:openmdx:kernel:Object</code> holder has the following structure:<ul>
 * <li><code>objectId</code>, the object's resource identifier (an XRI 2 string)
 * <li><code>version</code>, the object's version (an opaque byte[])
 * <li><code>value</code>, some or all of the object's features (a mapped record)  
 * </ul>
 * </ul>
 */
public enum RestFunction {
    
    /**
     * Object retrieval or verification
     * <p>
     * The input record represents either a query request, a retrieval request or a 
     * verification request:<ul>
     * <li>An <code>org:openmdx:kernel:Query</code> input record identifies a query request.<ul>
     * <li><code>queryType</code>, the candidate class' MOF identifier (a string value)
     * <li><code>query</code>, filter and ordering (a {@link Queries#OPENMDXQL} string value)
     * <li><code>position</code>, the lower bound (a number value)
     * <li><code>size</code>, the maximal number of elements to be returned (a number value) 
     * <li><code>parameters</code>, the parameters (an indexed or mapped record)
     * </ul>
     * <li>An indexed input record identifies an object retrieval request.<ul>
     * <li>The objects are identified by the XRI 2 string representations of their 
     * object ids.
     * </ul>
     * <li>An <code>org:openmdx:kernel:Object</code> input record identifies a 
     * verification request.<ul>
     * <li><code>objectId</code>, the object's resource identifier (an XRI 2 string)
     * <li><code>version</code>, the object's version to be verified.
     * </ul>
     */
    GET,
    
    /**
     * Object modification
     * <p>
     * The input record represents either a touch or an update request:<ul>
     * <li>An <code>org:openmdx:kernel:Query</code> input record identifies a 
     * touch by query request.<ul>
     * <li><code>queryType</code>, the candidate class' MOF identifier (a string value)
     * <li><code>query</code>, filter and ordering (a {@link Queries#OPENMDXQL} string value)
     * <li><code>position</code>, the lower bound (a number value)
     * <li><code>size</code>, the maximal number of elements to be returned (a number value) 
     * <li><code>parameters</code>, the parameters (an indexed or mapped record)
     * </ul>
     * <li>An indexed input record identifies a touch request.<ul>
     * <li>The objects are identified by the XRI 2 string representations of their 
     * object ids.
     * </ul>
     * <li>An <code>org:openmdx:kernel:Object</code> input record identifies 
     * an update request.<ul>
     * <li><code>objectId</code>, the object's resource identifier (an XRI 2 string)
     * <li><code>version</code>, the object's version to be verified.
     * </ul>
     */
    PUT,
    
    /**
     * Object removal
     * <p>
     * The input record represents either a delete by query request, a forced delete
     * request or a verified delete request:<ul>
     * <li>An <code>org:openmdx:kernel:Query</code> input record identifies a query.<ul>
     * <li><code>queryType</code>, the candidate class' MOF identifier (a string value)
     * <li><code>query</code>, filter and ordering (a {@link Queries#OPENMDXQL} string value)
     * <li><code>position</code>, the lower bound (a number value)
     * <li><code>size</code>, the maximal number of elements to be returned (a number value) 
     * <li><code>parameters</code>, the parameters (an indexed or mapped record)
     * </ul>
     * <li>An indexed input record identifies a forced delete request.<ul>
     * <li>The objects are identified by the XRI 2 string representations of their 
     * object ids.
     * </ul>
     * <li>An <code>org:openmdx:kernel:Object</code> input record identifies a 
     * verified delete request.<ul>
     * <li><code>objectId</code>, the object's resource identifier (an XRI 2 string)
     * <li><code>version</code>, the object's version to be verified.
     * </ul>
     */
    DELETE,
    
    /**
     * Object creation or method invocation
     * <p>
     * The input is a mapped record<ul>
     * <li><b>Object Creation</b><ul>
     * <li>the key is the XRI 2 string representation of the object id
     * <li>the value is a mapped record for the object's values
     * <li>the return or output record member is the corresponding object holder
     * </ul>
     * <li><b>Method Invocation</b><ul>
     * <li>the key is the XRI 2 string representation of the method invocation URI
     * <li>the value record contains the method invocation arguments
     * <li>the return or output record member is the method invocation result
     * </ul>
     * </ul>
     */
    POST;

}
