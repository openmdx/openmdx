/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Connection_2_0Remote.java,v 1.4 2010/09/13 16:46:29 hburger Exp $
 * Description: Connection 2.0 Remote Interface
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/09/13 16:46:29 $
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
package org.openmdx.application.rest.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;
import javax.resource.ResourceException;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

/**
 * Connection 2.0 Remote Interface
 */
public interface Connection_2_0Remote extends EJBObject {

    /** 
     * Allows the execution of an interaction represented by the 
     * <code>InteractionSpec</code>.
     * This invocation takes an input <code>Record</code> and returns an  
     * output <code>Record</code> if the execution of the Interaction has been
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
    ) throws ResourceException, RemoteException;

}
