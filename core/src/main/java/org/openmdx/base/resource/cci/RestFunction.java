/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Functions
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
package org.openmdx.base.resource.cci;

/**
 * REST Functions
 * <p>
 * The function either returns a record or updates a given output record<ul>
 * <li><b>Return Record</b><br>
 * The {@code execute()} method with a return record can be applied
 * to interactions belonging to either <em>remote</em> or <em>local</em> connections.<br>
 * The return record<ul>
 * <li>is {@code null} in case of {@link InteractionSpec#SYNC_SEND}<br>
 * <li>is an indexed record containing either {@code org:openmdx:kernel:Object} 
 * records or operation results.
 * </ul>
 * <li><b>Output Record</b><br>
 * The {@code execute()} method with an output record can be applied
 * only to interactions belonging to <em>local</em> connections.<br>
 * The output record
 * <ul>
 * <li>is ignored in case of {@link InteractionSpec#SYNC_SEND}, 
 * i.e. it can even be {@code null}
 * <li>must be an indexed record in case of {@link InteractionSpec#SYNC_SEND}<br>
 * which is modified by {@code add()} operations for either
 * {@code org:openmdx:kernel:Object} records or operation results.
 * </ul>
 * <li><b>Object Holder</b><br>
 * Each {@code org:openmdx:kernel:Object} holder has the following structure:<ul>
 * <li>{@code path}, a path representing the object's resource identifier (an XRI)
 * <li>{@code version}, the object's version (an opaque byte[])
 * <li>{@code lock}, the object's lock (an opaque byte[])
 * <li>{@code value}, some or all of the object's features (a mapped record)  
 * </ul>
 * </ul>
 */
public enum RestFunction {
    
    /**
     * Object retrieval or verification
     * <p>
     * The input record represents either a query, a retrieval, a refresh or a 
     * verification request:<ul>
     * <li>An {@code org:openmdx:kernel:Query} input record identifies a query request.<ul>
     * <li>{@code queryType}, the candidate class' MOF identifier (a string value)
     * <li>{@code query}, filter and ordering (a {@link Queries#OPENMDXQL} string value)
     * <li>{@code position}, the lower bound (a number value)
     * <li>{@code size}, the maximal number of elements to be returned (a number value) 
     * <li>{@code parameters}, the parameters (an indexed or mapped record)
     * <li>{@code refresh}, a flag telling whether the client asks for the object to be refreshed
     * </ul>
     * <li>An indexed input record identifies an object retrieval request.<ul>
     * <li>The objects are identified by Path instances representing their XRIs.
     * </ul>
     * <li>An {@code org:openmdx:kernel:Object} input record identifies a 
     * verification request.<ul>
     * <li>{@code path}, the object's resource identifier (a Path)
     * <li>{@code version}, the object's version to be verified.
     * <li>{@code lock}, the object's lock to be verified.
     * </ul>
     */
    GET,
    
    /**
     * Object modification
     * <p>
     * The input record represents either a touch or an update request:<ul>
     * <li>An {@code org:openmdx:kernel:Query} input record identifies a 
     * touch by query request.<ul>
     * <li>{@code queryType}, the candidate class' MOF identifier (a string value)
     * <li>{@code query}, filter and ordering (a {@link Queries#OPENMDXQL} string value)
     * <li>{@code position}, the lower bound (a number value)
     * <li>{@code size}, the maximal number of elements to be returned (a number value) 
     * <li>{@code parameters}, the parameters (an indexed or mapped record)
     * </ul>
     * <li>An indexed input record identifies a touch request.<ul>
     * <li>The objects are identified by Path instances representing their XRIs.
     * </ul>
     * <li>An {@code org:openmdx:kernel:Object} input record identifies 
     * an update request.<ul>
     * <li>{@code path}, a path representing the object's resource identifier (an XRI)
     * <li>{@code version}, the object's version to be verified.
     * <li>{@code lock}, the object's lock to be verified.
     * </ul>
     */
    PUT,
    
    /**
     * Object removal
     * <p>
     * The input record represents either a delete by query request, a forced delete
     * request or a verified delete request:<ul>
     * <li>An {@code org:openmdx:kernel:Query} input record identifies a query.<ul>
     * <li>{@code queryType}, the candidate class' MOF identifier (a string value)
     * <li>{@code query}, filter and ordering (a {@link Queries#OPENMDXQL} string value)
     * <li>{@code position}, the lower bound (a number value)
     * <li>{@code size}, the maximal number of elements to be returned (a number value) 
     * <li>{@code parameters}, the parameters (an indexed or mapped record)
     * </ul>
     * <li>An indexed input record identifies a forced delete request.<ul>
     * <li>The objects are identified by Path instances representing their XRIs.
     * </ul>
     * <li>An {@code org:openmdx:kernel:Object} input record identifies a 
     * verifiedU delete request.<ul>
     * <li>{@code path}, a path representing the object's resource identifier (an XRI)
     * <li>{@code version}, the object's version to be verified.
     * <li>{@code lock}, the object's lock to be verified.
     * </ul>
     */
    DELETE,
    
    /**
     * Object creation or method invocation
     * <p>
     * The input is a mapped record<ul>
     * <li><b>Object Creation</b><ul>
     * <li>the key is a Path representing the object's resource identifier (an XRI)
     * <li>the value is a mapped record for the object's values
     * <li>the return or output record member is the corresponding object holder
     * </ul>
     * <li><b>Method Invocation</b><ul>
     * <li>the key is a Path representing the method's invocation XRI
     * <li>the value record contains the method invocation arguments
     * <li>the return or output record member is the method invocation result
     * </ul>
     * </ul>
     */
    POST;

}
