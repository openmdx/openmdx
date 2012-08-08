/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ExecutionContext.java,v 1.4 2008/02/10 01:21:53 hburger Exp $
 * Description: Execution Context
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/10 01:21:53 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.security;

import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.openmdx.kernel.security.resource.Connection;

/**
 * This interface is used for Objects wrapping the environment
 * dependent execution context switching.
 */
public interface ExecutionContext extends Connection {

    /**
     * Perfom work in this context
     * 
     * @param action the code to be run in this context
     * 
     * @return the Object returned by the PrivilegedAction's run method.
     * 
     * @exception NullPointerException
     *            if the specified PrivilegedExceptionAction is null. 
     * @exception SecurityException
     *            if the caller does not have permission to invoke this method.
     */
    Object execute(
        PrivilegedAction<?> action
    );

    /**
     * Perfom work in this context
     * 
     * @param action the code to be run in this context
     * 
     * @return the Object returned by the PrivilegedExceptionAction's run method.
     * 
     * @exception PrivilegedActionException
     *            if the PrivilegedExceptionAction.run method throws a checked exception.
     * @exception NullPointerException
     *            if the specified PrivilegedExceptionAction is null. 
     * @exception SecurityException
     *            if the caller does not have permission to invoke this method.
     */
    Object execute(
        PrivilegedExceptionAction<?> action
    ) throws PrivilegedActionException;

}
