/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: FeatureMapper.java,v 1.5 2008/09/10 08:55:23 hburger Exp $
 * Description: FeatureMapper
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:23 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.mapping.AbstractNames;
import org.openmdx.model1.mapping.java.Identifier;

/**
 * Class FeatureMapper
 */
public class FeatureMapper {

    //------------------------------------------------------------------------
    public enum MethodSignature {

        DEFAULT, RETURN_IS_VOID, PREDICATE

    }

    //------------------------------------------------------------------------
    FeatureMapper(
        ModelElement_1_0 classDef,
        Class<?> targetIntf
    ){
        this.classDef = classDef;
        this.targetIntf = targetIntf;
    }

    //------------------------------------------------------------------------
    Method getCollection(
        String mutatorName
    ){
        Method accessor = this.collections.get(mutatorName);
        if(accessor == null) {
            String accessorName = 'g' + mutatorName.substring(1);
            try {
                this.collections.putIfAbsent(
                    mutatorName,
                    accessor = targetIntf.getMethod(accessorName)
                );
            } catch (Exception exception) {
                throw new UnsupportedOperationException(
                    accessorName + " requested by " + mutatorName + "(Collection)",
                    exception
                );
            }
        }
        return accessor;
    }

    //------------------------------------------------------------------------
    Method getAccessor(
        Object feature
    ) {
        String featureName;
        ModelElement_1_0 featureDef = null;
        if(feature instanceof String) {
            featureName = (String) feature;
            if(featureName.indexOf(":") > 0) {
                featureName = featureName.substring(featureName.lastIndexOf(":") + 1);
            }
        } 
        else {
            featureDef = ((RefMetaObject_1) feature).getElementDef();
            featureName = (String)featureDef.values("name").get(0);
        }
        Method accessor = this.accessors.get(featureName);
        if(accessor == null) { 
            try {
                if(featureDef == null){
                    Model_1_0 model = this.classDef.getModel();
                    featureDef = model.getFeatureDef(
                        this.classDef,
                        featureName,
                        false
                    );
                    if(featureDef == null) throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_MEMBER_NAME,
                        "Feature not found in model repository",
                        new BasicException.Parameter ("className", this.classDef.path().getBase()),
                        new BasicException.Parameter ("featureName", featureName)
                    );
                }                    
                String multiplicity = (String)featureDef.values("multiplicity").get(0);
                boolean isBoolean =
                    PrimitiveTypes.BOOLEAN.equals(featureDef.getModel().getDereferencedType(featureDef.values("type").get(0)).values("qualifiedName").get(0));
                boolean isSingleValued = 
                    (Multiplicities.SINGLE_VALUE.equals(multiplicity) || Multiplicities.OPTIONAL_VALUE.equals(multiplicity)); 
                String beanGetterName = AbstractNames.openmdx2AccessorName(
                    featureName,
                    true, // forQuery
                    isBoolean,
                    isSingleValued
                );
                String accessorName = Identifier.OPERATION_NAME.toIdentifier(beanGetterName);
                try {
                    this.accessors.putIfAbsent(
                        featureName,
                        accessor = targetIntf.getMethod(accessorName)
                    );
                } catch (Exception exception) {
                    throw new UnsupportedOperationException(
                        featureName,
                        exception
                    );
                }
            } catch (ServiceException exception) {
                throw new JmiServiceException(exception);
            }
        }
        return accessor;
    }

    //------------------------------------------------------------------------
    Method getMutator(
        Object feature
    ){
        String featureName;
        ModelElement_1_0 featureDef = null;
        if(feature instanceof String) {
            featureName = (String) feature;
            if(featureName.indexOf(":") > 0) {
                featureName = featureName.substring(featureName.lastIndexOf(":") + 1);
            }                
        } else {
            featureDef = ((RefMetaObject_1) feature).getElementDef();
            featureName = (String) featureDef.values("name").get(0);
        }
        Method mutator = this.mutators.get(featureName);
        if(mutator == null) {
            try {
                if(featureDef == null){
                    Model_1_0 model = this.classDef.getModel();
                    featureDef = model.getFeatureDef(
                        this.classDef,
                        featureName,
                        false
                    );
                }                                    
                String multiplicity = (String)featureDef.values("multiplicity").get(0);
                boolean isBoolean =
                    PrimitiveTypes.BOOLEAN.equals(featureDef.getModel().getDereferencedType(featureDef.values("type").get(0)).values("qualifiedName").get(0));
                boolean isSingleValued = 
                    (Multiplicities.SINGLE_VALUE.equals(multiplicity) || Multiplicities.OPTIONAL_VALUE.equals(multiplicity));                
                String beanSetterName = AbstractNames.openmdx2AccessorName(
                    featureName,
                    false, // forQuery
                    isBoolean,
                    isSingleValued
                );
                String mutatorName = Identifier.OPERATION_NAME.toIdentifier(beanSetterName);
                for(Method method : targetIntf.getMethods()) {
                    if(
                            method.getName().equals(mutatorName) &&
                            (method.getParameterTypes().length == 1) &&
                            (method.getReturnType() == void.class)
                            // Note: The argument type is ignored for the moment 
                    ){
                        this.mutators.putIfAbsent(featureName, method);
                        return method;
                    }
                }
                throw new UnsupportedOperationException(featureName);
            } catch (ServiceException exception) {
                throw new JmiServiceException(exception);
            }                
        } else {
            return mutator;
        }
    }

    //------------------------------------------------------------------------
    Method getOperation(
        Object feature
    ){
        String featureName;
        if(feature instanceof String) {
            featureName = (String) feature;
            if(featureName.indexOf(":") > 0) {
                featureName = featureName.substring(featureName.lastIndexOf(":") + 1);
            }                
        } else {
            ModelElement_1_0 featureDef = ((RefMetaObject_1) feature).getElementDef();
            featureName = (String) featureDef.values("name").get(0);
        }
        Method operation = this.mutators.get(featureName);
        if(operation == null) {
            String operationName = Identifier.OPERATION_NAME.toIdentifier(
                featureName,
                null, // removablePrefix
                null, // prependablePrefix
                null, // removableSuffix
                null // appendableSuffix
            );
            for(Method method : targetIntf.getMethods()) {
                if(
                        method.getName().equals(operationName)
                ){
                    this.operations.putIfAbsent(featureName, method);
                    return method;
                }
            }
            throw new UnsupportedOperationException(featureName);
        } else {
            return operation;
        }
    }

    //-----------------------------------------------------------------------        
    Method getMethod(
        Method source
    ){
        Method oldMethod = mapping.get(source);
        String methodName = source.getName();
        int methodArguments = source.getParameterTypes().length;
        if(oldMethod == null) {
            for(Method newMethod : targetIntf.getMethods()) {
                if(
                        newMethod.getName().equals(methodName) &&
                        newMethod.getParameterTypes().length == methodArguments
                        // Note: The argument types are ignored for the moment 
                ){
                    oldMethod = this.mapping.putIfAbsent(source, newMethod);
                    return oldMethod == null ? newMethod : oldMethod;
                }
            }
        }
        return oldMethod;
    }

    //-----------------------------------------------------------------------        
    @SuppressWarnings("unchecked")
    ModelElement_1_0 getFeature(
        String methodName,
        MethodSignature mode
    ) throws ServiceException {
        String className = (String)this.classDef.values("qualifiedName").get(0);
        ConcurrentMap<String,ModelElement_1_0> features = allFeatures.get(className);
        if(features == null) {
            allFeatures.putIfAbsent(
                className, 
                new ConcurrentHashMap<String,ModelElement_1_0>()
            );
            features = allFeatures.get(className);
        }
        if(mode == MethodSignature.RETURN_IS_VOID) {
            methodName = methodName + "@void";            
        }
        else if(mode == MethodSignature.PREDICATE) {
            methodName = Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1);        
            methodName = methodName + "@predicate";
        }
        ModelElement_1_0 feature = features.get(methodName);
        if(feature == null) {
            Model_1_0 model = this.classDef.getModel();
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
                    features.putIfAbsent(
                        Identifier.OPERATION_NAME.toIdentifier(
                            AbstractNames.openmdx2AccessorName(
                                featureName,
                                true, // forQuery
                                false, // forBoolean
                                true // singleValued
                            )
                        ),
                        feature
                    );
                    // non-boolean setter
                    features.putIfAbsent(
                        Identifier.OPERATION_NAME.toIdentifier(
                            AbstractNames.openmdx2AccessorName(
                                featureName,
                                false, // forQuery
                                false, // forBoolean
                                true // singleValued
                            )
                        ) + "@void", 
                        feature
                    );
                    // boolean query
                    features.putIfAbsent(
                        Identifier.OPERATION_NAME.toIdentifier(
                            AbstractNames.openmdx2AccessorName(
                                featureName,
                                true, // forQuery
                                true, // forBoolean
                                true // singleValued
                            )
                        ), 
                        feature
                    );
                    // boolean set
                    features.putIfAbsent(
                        Identifier.OPERATION_NAME.toIdentifier(
                            AbstractNames.openmdx2AccessorName(
                                featureName,
                                false, // forQuery
                                true, // forBoolean
                                true // singleValued
                            )
                        ) + "@void", 
                        feature
                    );
                    // add
                    features.putIfAbsent(
                        Identifier.OPERATION_NAME.toIdentifier(
                            Identifier.OPERATION_NAME.toIdentifier(
                                featureName,
                                null, // removablePrefix
                                "add", // prependablePrefix
                                null, // removableSuffix
                                null // appendableSuffix
                            )
                        ) + "@void", 
                        feature
                    );
                    // predicate
                    features.putIfAbsent(
                        Identifier.OPERATION_NAME.toIdentifier(
                            Identifier.OPERATION_NAME.toIdentifier(
                                featureName,
                                null, // removablePrefix
                                null, // prependablePrefix
                                null, // removableSuffix
                                null // appendableSuffix
                            )
                        ) + "@predicate", 
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
                            null, // removableSuffix
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
                    "feature not found in class",
                    new BasicException.Parameter [] {
                        new BasicException.Parameter("method.name", methodName),
                        new BasicException.Parameter("class.name", className)
                    }
                );                
            }
        }
        return feature;
    }

    //------------------------------------------------------------------------
    // Members
    //------------------------------------------------------------------------
    private final ConcurrentMap<String,Method> accessors = new ConcurrentHashMap<String,Method>();
    private final ConcurrentMap<String,Method> mutators = new ConcurrentHashMap<String,Method>();
    private final ConcurrentMap<String,Method> operations = new ConcurrentHashMap<String,Method>();
    private final ConcurrentMap<Method,Method> mapping = new ConcurrentHashMap<Method,Method>();
    private final ConcurrentMap<String,Method> collections = new ConcurrentHashMap<String,Method>();
    protected final static ConcurrentMap<String,ConcurrentMap<String,ModelElement_1_0>> allFeatures = 
        new ConcurrentHashMap<String,ConcurrentMap<String,ModelElement_1_0>>();

    private final ModelElement_1_0 classDef;    
    private final Class<?> targetIntf;    

}