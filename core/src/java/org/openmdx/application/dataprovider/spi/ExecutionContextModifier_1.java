/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ExecutionContextModifier_1.java,v 1.1 2009/01/05 13:44:51 wfro Exp $
 * Description: ExecutionContextModifier_1
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:44:51 $
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
package org.openmdx.application.dataprovider.spi;

import java.security.Principal;

import javax.ejb.EJBContext;
import javax.servlet.http.HttpServletRequest;

import org.openmdx.application.dataprovider.cci.ExecutionContextModifier_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;


/**
 * ExecutionContextModifier_1
 */
public class ExecutionContextModifier_1
implements ExecutionContextModifier_1_0 
{

    /**
     * Constructor
     * 
     * @param trustPeer
     *        Defines whether the principal chain should be cleared or not. 
     * @param useName 
     *        Defines whether the principals name or string representation should be used.
     * 
     */
    public ExecutionContextModifier_1(
        boolean trustPeer, 
        boolean useName
    ) {
        this.trustPeer = trustPeer;
        this.useName = useName;
    }

    /**
     * Defines whether the principal chain should be cleared or not.
     */
    private final boolean trustPeer;

    /**
     * Defines whether the principals name or string representation should be used.
     */
    private final boolean useName;

    /**
     * Context based modifier
     * 
     * @param serviceHeader the service header to be modified
     */
    public void apply(      
        EJBContext context,
        ServiceHeader serviceHeader        
    ){
        apply(context.getCallerPrincipal(), serviceHeader);
    }

    /**
     * Request based modifier
     * 
     * @param serviceHeader the service header to be modified
     */
    public void apply(      
        HttpServletRequest request,
        ServiceHeader serviceHeader        
    ){
        apply(request.getUserPrincipal(), serviceHeader);
    }

    /**
     * Common method
     * 
     * @param principal
     * @param serviceHeader
     */
    protected void apply (
        Principal principal,
        ServiceHeader serviceHeader
    ){
        if(this.trustPeer){
            serviceHeader.addPrincipal(getPrincipal(principal));
        } else {
            serviceHeader.setPrincipal(getPrincipal(principal));
        }
    }

    /**
     * Retrieve the value to be stored in the service header's principal 
     * chain.
     * 
     * @param source
     * 
     * @return the principal's name or string representation
     */
    protected String getPrincipal(
        Principal source
    ){
        return source == null ? 
            null :
                this.useName ? 
                    source.getName() : 
                        source.toString();
    }

    /**
     * Create an ExecutionContextModifier_1_0 instance
     * <p>
     * The kind parameter may have one of the following values<ul>
     * <li>null (the default value: trust peer and do not modify the principal 
     *     chain) 
     * <li>setPrincipalName (clear the principal chain and add the principal's 
     *     name) 
     * <li>setPrincipalString (clear the principal chain and add the 
     *     principal's string representation)
     * <li>addPrincipalName (add the principal's name to the principal chain) 
     * <li>addPrincipalString (add the principal's string representation to 
     *     the principal chain)
     * <li>executionContextModifierClass (the fully qualified name of a 
     *     class implementing ${@link 
     *     org.openmdx.application.dataprovider.cci.ExecutionContextModifier_1_0
     *     ExecutionContextModifier_1_0} 
     * </ul>
     * 
     * @param kind defines which kind of execution context modifier should be created,
     * 
     * @return a newly created ExecutionContextModifier_1_0 instance
     * 
     * @throws ServiceException 
     */
    public static ExecutionContextModifier_1_0 newInstance(
        String kind
    ) throws ServiceException{
        try {
            return null == kind || "null".equals(kind) ? 
                null :
                    "setPrincipalName".equals(kind) ? new ExecutionContextModifier_1(
                        false, // trustPeer 
                        true // useName
                    ) : "setPrincipalString".equals(kind) ? new ExecutionContextModifier_1(
                        false, // trustPeer
                        false // useName
                    ) : "addPrincipalName".equals(kind) ? new ExecutionContextModifier_1(
                        true, // trustPeer
                        true // useName
                    ) : "addPrincipalString".equals(kind) ? new ExecutionContextModifier_1(
                        true, // trustPeer
                        false // useName
                    ) : (ExecutionContextModifier_1_0) Classes.getApplicationClass(
                        kind // execution context modifier class name
                    ).newInstance();
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Execution context modifier could not be created",
                new BasicException.Parameter("kind", kind)
            );
        }
    }

}
