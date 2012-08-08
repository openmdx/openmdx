<?xml version="1.0" encoding="ISO-8859-1"?>
/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ContainerTransaction.java,v 1.2 2006/02/19 22:00:31 wfro Exp $
 * Description: Container Transaction
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/02/19 22:00:31 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.container.spi.ejb;


/**
 * Container Transaction
 */
public interface ContainerTransaction {

    /**
     * Evaluate the transaction attribute for a given method.
     * 
     * @param methodInterface
     * The method-intf element allows a method element to
     * differentiate between the methods with the same name and
     * signature that are multiply defined across the home and
     * component interfaces (e.g, in both an enterprise bean's
     * remote and local interfaces or in both an enterprise bean's
     * home and remote interfaces, etc.); the component and web
     * service endpoint interfaces, and so on.
     * <p>
     * The methodInterface argument must be one of the following:<ul>
     * <li>Home
     * <li>Remote
     * <li>LocalHome
     * <li>Local
     * <li>ServiceEndpoint
     * </ul>
     * 
     * @param methodName
     * ThemethodName contains the name of an enterprise
     * bean method.
     * 
     * @param methodParameters
     * The methodParameters defines a list of the
     * fully-qualified Java type names of the method parameters.
     * 
     * @return the transaction attribute.
     */
    TransactionAttribute getTransactionAttribute(
        String methodInterface,
        String methodName,
        String[] methodParameters
    );

}
