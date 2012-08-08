/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Jmi1PredicateInvocationHandler.java,v 1.15 2009/01/13 17:33:49 wfro Exp $
 * Description: Jmi1PackageInvocationHandler 
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 17:33:49 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
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
import java.util.Collection;
import java.util.Collections;

import javax.jdo.Query;

import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.accessor.jmi.cci.RefFilter_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.w3c.cci2.AnyTypePredicate;

/**
 * Jmi1ObjectInvocationHandler
 */
public class Jmi1PredicateInvocationHandler implements InvocationHandler {

    /**
     * Constructor 
     *
     * @param refPackage
     * @param filterType
     * @param filterProperties
     * @param attributeSpecifiers
     * @param delegateFilter
     * @param delegateQuantor
     * @param delegateName
     */
    public Jmi1PredicateInvocationHandler(
        RefPackage_1_0 refPackage,
        String filterType,
        FilterProperty[] filterProperties,
        AttributeSpecifier[] attributeSpecifiers,
        RefFilter_1_0 delegateFilter,
        Short delegateQuantor,
        String delegateName
    ) {
        this.delegate = new RefPredicate_1Proxy(
            refPackage,
            filterType,
            filterProperties,
            attributeSpecifiers,
            delegateFilter,
            delegateQuantor,
            delegateName
        );
    }

    /**
     * 
     */
    protected final RefPredicate_1 delegate;

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
                return this.delegate.toString();
            } 
            else if("hashCode".equals(methodName)) {
                return this.delegate.hashCode();
            } 
            else if ("equals".equals(methodName)) {
                return args[0] == proxy; // Identity
            }
        } 
        else if (
            declaringClass == Query.class ||
            declaringClass == RefFilter_1_0.class ||
            declaringClass == RefFilter_1_1.class 
        ) {
            try {
                return method.invoke(
                    this.delegate, 
                    args
                );
            } catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        } 
        else if (declaringClass == AnyTypePredicate.class) {
            if("equalTo".equals(methodName)) {
                this.delegate.equalTo(
                    args[0]
                );
                return null;
            } 
            else if("notEqualTo".equals(methodName)) {
                this.delegate.notEqualTo(
                    args[0]
                );
                return null;
            } 
            else if("elementOf".equals(methodName)) {
                if(args[0] == null) {
                    this.delegate.elementOf(
                        Collections.EMPTY_SET
                    );
                    return null;
                } 
                else if (args[0] instanceof Collection){
                    this.delegate.elementOf(
                        (Collection<?>)args[0]
                    );
                    return null;
                } 
                else if (args[0].getClass().isArray()) {
                    this.delegate.elementOf(
                        (Object[])args[0]
                    );
                    return null;
                } 
                else throw new IllegalArgumentException(
                    "Invalid argument for 'elementOf': " + args[0].getClass().getName()
                );
            } 
            else if("notAnElementOf".equals(methodName)) {
                if(args[0] == null) {
                    this.delegate.notAnElementOf(
                        Collections.EMPTY_SET
                    );
                    return null;
                } 
                else if (args[0] instanceof Collection){
                    this.delegate.notAnElementOf(
                        (Collection<?>)args[0]
                    );
                    return null;
                } 
                else if (args[0].getClass().isArray()) {
                    this.delegate.notAnElementOf(
                        (Object[])args[0]
                    );
                    return null;
                } 
                else throw new IllegalArgumentException(
                    "Invalid argument for 'notAnElementOf': " + args[0].getClass().getName()
                );
            }        
        }
        else if(methodName.startsWith("orderBy")) {
            //
            // orderBy
            //
            ModelElement_1_0 feature = this.delegate.getFeatureMapper().getFeature(
                methodName.substring(7),
                FeatureMapper.MethodSignature.PREDICATE                                
            );
            String featureName = (String)feature.objGetValue("name");
            return this.delegate.refGetOrder(
                featureName
            );
        } 
        else if(methodName.startsWith("thereExists")) {
            //
            // thereExists
            //
            if("thereExistsContext".equals(methodName)) {
                return this.delegate.refGetPredicate(
                    Quantors.THERE_EXISTS,
                    "org:openmdx:base:ContextCapable:context"
                );
            } 
            else {
                ModelElement_1_0 feature = this.delegate.getFeatureMapper().getFeature(
                    methodName.substring(11),
                    FeatureMapper.MethodSignature.PREDICATE                                
                );
                String featureName = (String)feature.objGetValue("name");
                return this.delegate.refGetPredicate(
                    Quantors.THERE_EXISTS,
                    featureName
                );
            }
        } 
        else if(methodName.startsWith("forAll")) {
            //
            // forAll
            //
            if("forAllContext".equals(methodName)){
                return this.delegate.refGetPredicate(
                    Quantors.FOR_ALL,
                    "org:openmdx:base:ContextCapable:context"
                );
            } 
            else {
                ModelElement_1_0 feature = this.delegate.getFeatureMapper().getFeature(
                    methodName.substring(6),
                    FeatureMapper.MethodSignature.PREDICATE                
                );
                String featureName = (String)feature.objGetValue("name");
                return this.delegate.refGetPredicate(
                    Quantors.FOR_ALL,
                    featureName
                );
            }
        } 
        else if(args == null || args.length == 0) {
            //
            // Predicate
            //
            ModelElement_1_0 feature = this.delegate.getFeatureMapper().getFeature(
                methodName,
                FeatureMapper.MethodSignature.PREDICATE                
            );
            String featureName = (String)feature.objGetValue("name");            
            return this.delegate.refGetPredicate(
                featureName
            );
        }
        throw new UnsupportedOperationException(methodName);
    }

    //------------------------------------------------------------------------
    // Class RefPredicate_1Proxy
    //------------------------------------------------------------------------
    
    /**
     * RefPredicate_1Proxy
     */
    public static class RefPredicate_1Proxy extends RefPredicate_1 {

        /**
         * Constructor 
         *
         * @param refPackage
         * @param filterType
         * @param filterProperties
         * @param attributeSpecifiers
         * @param delegateFilter
         * @param delegateQuantor
         * @param delegateName
         */
        public RefPredicate_1Proxy(
            RefPackage_1_0 refPackage,
            String filterType,
            FilterProperty[] filterProperties,
            AttributeSpecifier[] attributeSpecifiers,
            RefFilter_1_0 delegateFilter,
            Short delegateQuantor,
            String delegateName
        ) {
            super(
                refPackage,
                filterType,
                filterProperties,
                attributeSpecifiers,
                delegateFilter,
                delegateQuantor,
                delegateName
            );
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -6399595381625181656L;

    }

}
