/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Jmi1ObjectInvocationHandler.java,v 1.31 2008/02/19 13:35:47 wfro Exp $
 * Description: Jmi1PackageInvocationHandler 
 * Revision:    $Revision: 1.31 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 13:35:47 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jmi.reflect.RefClass;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.jmi.cci.RefClass_1_1;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_3;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.java.Identifier;
import org.w3c.cci2.AnyTypePredicate;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.ComparableTypePredicate;
import org.w3c.cci2.MatchableTypePredicate;
import org.w3c.cci2.MultivaluedFeaturePredicate;
import org.w3c.cci2.PartiallyOrderedTypePredicate;
import org.w3c.cci2.ResourceIdentifierTypePredicate;
import org.w3c.cci2.StringTypePredicate;

/**
 * Jmi1ObjectInvocationHandler
 *
 */
public class Jmi1ObjectInvocationHandler implements InvocationHandler, Serializable {
    
    //-----------------------------------------------------------------------
    public Jmi1ObjectInvocationHandler(
        Object_1_0 delegation,
        RefClass refClass
    ) {
        this(
            delegation,
            refClass,
            false
        );
    }
    
    //-----------------------------------------------------------------------
    public Jmi1ObjectInvocationHandler(
        Object_1_0 delegation,
        RefClass refClass,
        boolean ignoreImpls
    ) {
        this.refDelegate = new RefObject_1Proxy(
            delegation,
            refClass
        );
        this.ignoreImpls = ignoreImpls;
        this.instanceQualifiedClassName = refClass.refMofId();
        this.impls = new HashMap<String,Object>();        
        this.rootPkg = (RefPackage_1_3)refClass.refOutermostPackage();
        this.featuresHavingNoImpl = ((RefClass_1_1)refClass).refFeaturesHavingNoImpl();
    }

    //-----------------------------------------------------------------------
    public static class RefObject_1Proxy extends RefObject_1 {
        
        public RefObject_1Proxy(
            Object_1_0 delegation,
            RefClass refClass        
        ) {
            super(
                delegation,
                refClass
            );
        }
        
        private static final long serialVersionUID = -8725046946085576144L;
    }

    //-----------------------------------------------------------------------
    private class Jmi1BinaryLargeObject implements BinaryLargeObject {

        public Jmi1BinaryLargeObject(            
            String featureName,
            InputStream value
        ) {
            this.featureName = featureName;
            this.initialValue = value;
        }
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent(
        ) throws IOException {
            InputStream value = this.initialValue == null
                ? (InputStream)Jmi1ObjectInvocationHandler.this.refDelegate.refGetValue(this.featureName)
                : this.initialValue;
            this.initialValue = null;
            return value;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ) throws IOException {
            return null;
        }
        
        protected transient InputStream initialValue = null;
        protected final String featureName;
    }
    
    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected ModelElement_1_0 getFeature(
        String methodName
    ) throws ServiceException {
        String className = this.refDelegate.refClass().refMofId();
        Map<String,ModelElement_1_0> features = allFeatures.get(className);
        if(features == null) {
            allFeatures.put(
                className, 
                features = new HashMap<String,ModelElement_1_0>()
            );
        }
        ModelElement_1_0 feature = features.get(methodName);
        if(feature == null) {
            Model_1_0 model = this.refDelegate.getModel();
            ModelElement_1_0 classDef = model.getElement(className);
            List<ModelElement_1_0> operations = new ArrayList<ModelElement_1_0>();
            for(Iterator i = ((Map)classDef.values("allFeature").get(0)).values().iterator(); i.hasNext(); ) {
                feature = (ModelElement_1_0)i.next();
                // Operation
                if(model.isOperationType(feature)) {
                    operations.add(feature);
                }
                // Structural feature
                else {
                    String featureName = (String)feature.values("name").get(0);
                    // non-boolean getter
                    features.put(
                        Identifier.OPERATION_NAME.toIdentifier(
                            Names.openmdx2AccessorName(
                                featureName,
                                true, // forQuery
                                false, // forBoolean
                                true // singleValued
                            )
                        ),
                        feature
                    );
                    // non-boolean setter
                    features.put(
                        Identifier.OPERATION_NAME.toIdentifier(
                            Names.openmdx2AccessorName(
                                featureName,
                                false, // forQuery
                                false, // forBoolean
                                true // singleValued
                            )
                        ), 
                        feature
                    );
                    // boolean query
                    features.put(
                        Identifier.OPERATION_NAME.toIdentifier(
                            Names.openmdx2AccessorName(
                                featureName,
                                true, // forQuery
                                true, // forBoolean
                                true // singleValued
                            )
                        ), 
                        feature
                    );
                    // boolean set
                    features.put(
                        Identifier.OPERATION_NAME.toIdentifier(
                            Names.openmdx2AccessorName(
                                featureName,
                                false, // forQuery
                                true, // forBoolean
                                true // singleValued
                            )
                        ), 
                        feature
                    );
                    // add
                    features.put(
                        Identifier.OPERATION_NAME.toIdentifier(
                            Identifier.OPERATION_NAME.toIdentifier(
                                featureName,
                                null, // removablePrefix
                                "add", // prependablePrefix
                                null // appendableSuffix
                            )
                        ), 
                        feature
                    );
                }
            }
            // Postprocess operations
            for(ModelElement_1_0 operation: operations) {
                String operationName = (String)operation.values("name").get(0);                
                // In case a feature accessor with the same name as an operation exists
                // it is overriden, i.e. operations have precedence
                features.put(
                    Identifier.OPERATION_NAME.toIdentifier(
                        Identifier.ATTRIBUTE_NAME.toIdentifier(
                            operationName,
                            null, // removablePrefix
                            null, // prependablePrefix
                            null // appendableSuffix
                        )
                    ),
                    operation
                );
            }                
            feature = features.get(methodName);
            if(feature == null) {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.NOT_FOUND, 
                    new BasicException.Parameter [] {
                      new BasicException.Parameter("method.name", methodName),
                      new BasicException.Parameter("class.name", className)
                    },
                    "feature not found in class"
                );                
            }
        }
        return feature;
    }
    
    //-----------------------------------------------------------------------
    public boolean implementsMethod(
        Object impl,
        Method method
    ) {
        if(impl == null) return false;
        boolean implementsMethod = false;
        try {
            implementsMethod = impl.getClass().getMethod(method.getName(), method.getParameterTypes()) != null;
            return implementsMethod;
        }
        catch(NoSuchMethodException e) {}
        return implementsMethod;
    }
            
    //-----------------------------------------------------------------------
    protected Object getImpl(
        Object proxy,
        Method method,
        ModelElement_1_0 feature
    ) {
        String featureName = (String)feature.values("name").get(0);
        String qualifiedFeatureName = (String)feature.values("qualifiedName").get(0);
        if(this.featuresHavingNoImpl.contains(featureName)) {
            return null;
        }
        // Get impl for instance class
        if(
            (this.impls.get(this.instanceQualifiedClassName) == null) &&
            !this.impls.containsKey(this.instanceQualifiedClassName)
        ) {        
            Object userImpl = this.rootPkg.refCreateImpl(
                this.instanceQualifiedClassName, 
                (RefObject_1_0)proxy
            );
            this.impls.put(
                this.instanceQualifiedClassName, 
                userImpl
            );
        }
        // Get impl for declaring class
        String declaredQualifiedClassName = qualifiedFeatureName.substring(
            0, 
            qualifiedFeatureName.lastIndexOf(":")
        );
        if(
            (this.impls.get(declaredQualifiedClassName) == null) &&
            !this.impls.containsKey(declaredQualifiedClassName)
        ) {        
            Object userImpl = this.rootPkg.refCreateImpl(
                declaredQualifiedClassName, 
                (RefObject_1_0)proxy
            );
            this.impls.put(
                declaredQualifiedClassName, 
                userImpl
            );
        }
        Object impl = this.impls.get(this.instanceQualifiedClassName);
        if(this.implementsMethod(impl, method)) {
            return impl;
        }
        impl = this.impls.get(declaredQualifiedClassName);
        if(this.implementsMethod(impl, method)) {
            return impl;
        }
        this.featuresHavingNoImpl.add(featureName);
        return null;
    }
    
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method,
        Object[] args
    ) throws Throwable {
        // RefObject
        if(
            method.getName().startsWith("ref") && 
            (method.getName().length() > 3) &&
            Character.isUpperCase(method.getName().charAt(3))
        ) {
            try {
                return method.invoke(
                    this.refDelegate, 
                    args
                );
            }
            catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
        // Object
        else if("toString".equals(method.getName())) {
            return this.refDelegate.toString();
        }
        else if("equals".equals(method.getName())) {
            return args[0].equals(this.refDelegate);
        }
        else if("hashCode".equals(method.getName())) {
            return this.refDelegate.hashCode();
        }
        else {
            ModelElement_1_0 feature = this.getFeature(method.getName());
            String featureName = (String)feature.values("name").get(0);
            Object impl = null;
            if(!this.ignoreImpls) {
                impl = this.getImpl(
                    proxy,
                    method,
                    feature
                );
            }
            // Dispatch to user-defined implementation
            if(impl != null) {
                Method implMethod = impl.getClass().getMethod(method.getName(), method.getParameterTypes());
                try {
                    return implMethod.invoke(
                        impl,
                        args
                    );
                }
                catch(InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
            // Generic delegate 
            else {
                // Getters
                if(method.getName().startsWith("get")) {
                    if((args == null) || (args.length == 0)) {           
                        Object value = this.refDelegate.refGetValue(
                            featureName
                        );
                        return value instanceof InputStream
                            ? new Jmi1BinaryLargeObject(featureName, (InputStream)value)
                            : value;
                    }
                    // Query
                    else if(
                        (args.length == 1) &&
                        (args[0] instanceof AnyTypePredicate ||
                        args[0] instanceof ComparableTypePredicate ||
                        args[0] instanceof MatchableTypePredicate ||
                        args[0] instanceof MultivaluedFeaturePredicate ||
                        args[0] instanceof PartiallyOrderedTypePredicate ||
                        args[0] instanceof ResourceIdentifierTypePredicate ||
                        args[0] instanceof StringTypePredicate)
                    ) {
                        return ((org.openmdx.compatibility.base.collection.Container<?>)this.refDelegate.refGetValue(
                            featureName
                        )).toList(args[0]);
                    }
                    // Qualifier
                    else {
                        return this.refDelegate.refGetValue(
                            featureName,
                            args[0]
                        );
                    }
                }
                // Boolean getters
                else if(method.getName().startsWith("is")) {
                    if((args == null) || (args.length == 0)) {  
                        return this.refDelegate.refGetValue(
                            featureName
                        );
                    }
                }
                // Setters
                else if(method.getName().startsWith("set")) {
                    if((args != null) || (args.length == 1)) {                      
                        this.refDelegate.refSetValue(
                            featureName,
                            args[0] instanceof BinaryLargeObject
                               ? ((BinaryLargeObject)args[0]).getContent()
                               : args[0]
                        );
                        return null;
                    }
                }
                // Adders with signature (boolean idIsPersistent, PrimitiveType qualifier, Object object)
                else if(
                    method.getName().startsWith("add") &&
                    (args != null) && 
                    (args.length == 3 && (args[0].getClass() == Boolean.class) && (args[1].getClass().isPrimitive() || args[1].getClass() == String.class))                    
                ) {
                    this.refDelegate.refAddValue(
                        Identifier.ATTRIBUTE_NAME.toIdentifier(
                            featureName,
                            "add", // removablePrefix
                            null, // prependablePrefix
                            null, // removableSuffix
                            null // appendableSuffix
                        ), 
                        args[1], 
                        args[2]
                    );
                    return null;
                }
                // Operations
                else {
                    List<Object> iargs = new ArrayList<Object>();
                    if(args != null) {
                        for(int i = 0; i < args.length; i++) {
                            iargs.add(args[i]);
                        }
                    }
                    return this.refDelegate.refInvokeOperation(
                        featureName, 
                        iargs
                    );
                }
            }
        }
        throw new UnsupportedOperationException(method.getName());
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 7709571315051480193L;

    protected final RefObject_1 refDelegate;
    protected final boolean ignoreImpls;
    protected final String instanceQualifiedClassName;
    protected final Map<String,Object> impls;
    protected final RefPackage_1_3 rootPkg;
    protected final Set<String> featuresHavingNoImpl;
    protected final static Map<String,Map<String,ModelElement_1_0>> allFeatures = 
        new HashMap<String,Map<String,ModelElement_1_0>>();

}
