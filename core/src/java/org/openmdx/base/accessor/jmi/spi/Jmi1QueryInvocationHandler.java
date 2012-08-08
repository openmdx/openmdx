/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Jmi1QueryInvocationHandler.java,v 1.6 2010/06/08 12:54:19 hburger Exp $
 * Description: JMI Query Invocation Handler 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/08 12:54:19 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2010, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.jdo.Query;

import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.query.Quantifier;

/**
 * JMI Query Invocation Handler
 */
public class Jmi1QueryInvocationHandler implements InvocationHandler {

    public Jmi1QueryInvocationHandler(
        RefQuery_1 query
    ) {
        this.query = query;
    }

    /**
     * Retrieve the feature mapper
     * 
     * @return the feature mapper
     * @throws ServiceException 
     */
    protected FeatureMapper getFeatureMapper(
    ) throws ServiceException{
        if(this.featureMapper == null) {
            this.featureMapper = this.query.getFeatureMapper();
        }
        return this.featureMapper;
    }

    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        String methodName = method.getName();
        if(declaringClass == Object.class) {
            if("toString".equals(methodName)) {
                return this.query.toString();
            } 
            else if("hashCode".equals(methodName)) {
                return this.query.hashCode();
            } 
            else if ("equals".equals(methodName)) {
                return args[0] == proxy; // Identity
            }
        } 
        else if (
            declaringClass == Query.class ||
            declaringClass == RefQuery_1_0.class 
        ) {
            try {
                return method.invoke(
                    this.query, 
                    args
                );
            } catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        } 
        else if(methodName.startsWith("orderBy")) {
            //
            // orderBy
            //
            ModelElement_1_0 feature = this.getFeatureMapper().getFeature(
                methodName.substring(7),
                FeatureMapper.MethodSignature.PREDICATE                                
            );
            String featureName = (String)feature.objGetValue("name");
            return this.query.refGetOrder(
                featureName
            );
        } 
        else if(methodName.startsWith("thereExists")) {
            ModelElement_1_0 feature = this.getFeatureMapper().getFeature(
                methodName.substring(11),
                FeatureMapper.MethodSignature.PREDICATE                                
            );
            String featureName = (String)feature.objGetValue("name");
            return this.query.refGetPredicate(
                Quantifier.THERE_EXISTS,
                featureName
            );
        } 
        else if(methodName.startsWith("forAll")) {
            ModelElement_1_0 feature = this.getFeatureMapper().getFeature(
                methodName.substring(6),
                FeatureMapper.MethodSignature.PREDICATE                
            );
            String featureName = (String)feature.objGetValue("name");
            return this.query.refGetPredicate(
                Quantifier.FOR_ALL,
                featureName
            );
        } 
        else if(args == null || args.length == 0) {
            //
            // Predicate
            //
            ModelElement_1_0 feature = this.getFeatureMapper().getFeature(
                methodName,
                FeatureMapper.MethodSignature.PREDICATE                
            );
            String featureName = (String)feature.objGetValue("name");            
            return this.query.refGetPredicate(
                featureName
            );
        }
        throw new UnsupportedOperationException(methodName);
    }

    //-----------------------------------------------------------------------
    protected RefQuery_1 getQuery(
    ) {
        return this.query;
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private final RefQuery_1 query;
    private transient FeatureMapper featureMapper;
   
}
