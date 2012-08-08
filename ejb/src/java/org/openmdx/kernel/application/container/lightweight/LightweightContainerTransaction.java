/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LightweightContainerTransaction.java,v 1.1 2009/01/12 12:49:23 wfro Exp $
 * Description: LightweightContainerTransaction
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:23 $
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
package org.openmdx.kernel.application.container.lightweight;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openmdx.kernel.application.container.spi.ejb.ContainerTransaction;
import org.openmdx.kernel.application.container.spi.ejb.TransactionAttribute;
import org.openmdx.kernel.application.deploy.spi.Deployment;
import org.openmdx.kernel.collection.EnumMapping;


/**
 * LightweightContainerTransaction
 */
@SuppressWarnings("unchecked")
public class LightweightContainerTransaction 
    implements ContainerTransaction 
{

    /**
     * Constructor
     */
    public LightweightContainerTransaction(
        List delegate
    ) {
        this.delegate = delegate;
    }

    /**
     * A list of Deployment.ContainerTransaction entries.
     */
    private final List delegate;
    
    /**
     * Maps TransactionAttributes to their string representation
     */
    private final static EnumMapping mapping = new EnumMapping(TransactionAttribute.class);
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.ContainerTransaction#getTransactionAttribute(java.lang.String, java.lang.String, java.lang.String[])
     */
    public TransactionAttribute getTransactionAttribute(
        String methodInterface,
        String methodName,
        String[] methodParameters
    ){
        if(this.delegate == null) {
            //
            // Bean Managed Transactions
            //
            return TransactionAttribute.NOT_SUPPORTED;
        } else {
            //
            // Container Managed Transactions
            //
            List requestParameters = Arrays.asList(methodParameters);
            for(
                Iterator i = this.delegate.iterator();
                i.hasNext();
            ){
                Deployment.ContainerTransaction candidate = (Deployment.ContainerTransaction) i.next();
                for(
                    Iterator j = candidate.getMethod().iterator();
                    j.hasNext();
                ){
                    Deployment.Method method = (Deployment.Method) j.next();
                    if(
                        match(
                            methodInterface, methodName, requestParameters, 
                            method.getMethodIntf(), method.getMethodName(), method.getMethodParams()
                        )
                    ) return (TransactionAttribute) mapping.getKey(candidate.getTransAttribute());                
                }
            }
            return TransactionAttribute.SUPPORTS;
        }
    }

    /**
     * Test wether the given method matches one of the candidate's methods.
     * 
     * @param requestInterface
     * @param requestMethod
     * @param requestParameters
     * @param candidateInterface
     * @param candidateMethod
     * @param candidateParameters
     * 
     * @return <code>true</code> if the given method matches the candidate's method
     */
    protected static final boolean match(
        String requestInterface,
        String requestMethod,
        List requestParameters,
        String candidateInterface,
        String candidateMethod,
        List candidateParameters
    ){
        return (
            candidateInterface == null || candidateInterface.equals(requestInterface)
        ) && (
            "*".equals(candidateMethod) || candidateMethod.equals(requestMethod) 
        ) && (
            candidateParameters == null || candidateParameters.equals(requestParameters)
        );
        
    }
    
    static {
        mapping.put(TransactionAttribute.NOT_SUPPORTED, "NotSupported");
        mapping.put(TransactionAttribute.REQUIRED, "Required");
        mapping.put(TransactionAttribute.SUPPORTS , "Supports");
        mapping.put(TransactionAttribute.REQUIRES_NEW, "RequiresNew");
        mapping.put(TransactionAttribute.MANDATORY, "Mandatory");
        mapping.put(TransactionAttribute.NEVER, "Never");        
    }

}
