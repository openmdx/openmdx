/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RestConnection.java,v 1.3 2009/05/20 15:13:42 hburger Exp $
 * Description: REST Connection 2.0 Business Interface
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/20 15:13:42 $
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
package org.openmdx.base.rest.spi;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

/**
 * REST Connection 2.0 Business Interface
 */
public interface RestConnection {

    /** 
     * Executes an interaction represented by the InteractionSpec.
     * This form of invocation takes an input Record and returns an 
     * output Record if the execution of the Interaction has been
     * successful.
     *  
     * @param   ispec   InteractionSpec representing a target EIS 
     *                  data/function module   
     * @param   input   Input Record
     *
     * @return  output Record if execution of the EIS function has been 
     *          successful; null otherwise
     *
     * @throws  ResourceException   Exception if execute operation
     *                              fails. Examples of error cases
     *                              are:
     *         <UL>
     *           <LI> Resource adapter internal, EIS-specific or 
     *                communication error 
     *           <LI> Invalid specification of an InteractionSpec 
     *                or input record structure
     *           <LI> Errors in use of input Record or creation
     *                of an output Record
     *           <LI> Invalid connection associated with this 
     *                Interaction
     *         </UL>
     * @throws NotSupportedException Operation not supported 
     */
    Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException;

    /** 
     * Executes an interaction represented by the InteractionSpec.
     * This form of invocation takes an input Record and updates
     * the output Record. 
     *  
     * @param   ispec   InteractionSpec representing a target EIS 
     *                  data/function module   
     * @param   input   Input Record
     * @param   output  Output Record
     * 
     * @return  true if execution of the EIS function has been 
     *          successful and output Record has been updated; false
     *          otherwise
     *
     * @throws  ResourceException   Exception if execute operation
     *                              fails. Examples of error cases
     *                              are:
     *         <UL>
     *           <LI> Resource adapter internal, EIS-specific or 
     *                communication error 
     *           <LI> Invalid specification of an InteractionSpec, 
     *                input or output record structure
     *           <LI> Errors in use of input or output Record
     *           <LI> Invalid connection associated with this 
     *                Interaction
     *         </UL>
     * @throws NotSupportedException Operation not supported 
     *                             
     */
    boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException;

    /** 
     * Gets the information on the underlying EIS instance represented
     * through an active connection.
     *
     * @return   ConnectionMetaData instance representing information 
     *           about the EIS instance
     * @throws   ResourceException  
     *                        Failed to get information about the 
     *                        connected EIS instance. Error can be
     *                        resource adapter-internal, EIS-specific
     *                        or communication related.
     */
    ConnectionMetaData getMetaData(
    ) throws ResourceException;

}
