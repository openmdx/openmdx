/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: FeatureMapper
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.omg.mof.spi.AbstractNames;
import org.omg.mof.spi.Identifier;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.kernel.exception.BasicException;

/**
 * Class FeatureMapper
 */
public class FeatureMapper implements Serializable {

    static enum MethodSignature {
        DEFAULT, RETURN_IS_VOID, PREDICATE
    }

    static enum Type {
        TEMRINAL, NON_TERMINAL, QUERY
    }

    static enum Kind {
        METHOD, QUERY_OPERATION, NON_QUERY_OPERATION
    }
    
    /**
     * Constructor 
     *
     * @param classDef
     * @param targetIntf
     */
    FeatureMapper(
        ModelElement_1_0 classDef,
        Class<?> targetIntf
    ){
        this.classDef = classDef;
        this.targetIntf = targetIntf;
    }

    /**
     * Convert qualified model names to simple model names
     * 
     * @param modelName a simple or qualified model name
     * 
     * @return the simple model name
     */
    private String getSimpleName(String modelName) {
        int i = modelName.lastIndexOf(":");
        return i < 0 ? modelName : modelName.substring(i + 1);
    }
    
    //------------------------------------------------------------------------
    Method getCollection(
        String mutatorName
    ){
        Method accessor = this.collections.get(mutatorName);
        if(accessor == null) {
            String accessorName = 'g' + mutatorName.substring(1);
            try {
                return Maps.putUnlessPresent(
                    this.collections,
                    mutatorName,
                    targetIntf.getMethod(accessorName)
                );
            } catch (Exception exception) {
                throw new UnsupportedOperationException(
                    accessorName + " requested by " + mutatorName + "(Collection)",
                    exception
                );
            }
        } else {
            return accessor;
        }
    }

    //------------------------------------------------------------------------
    Method getAccessor(
        Object feature
    ) throws ServiceException {
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
            featureName = (String)featureDef.objGetValue("name");
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
                    if(featureDef == null) { 
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_MEMBER_NAME,
                            "Feature not found in model repository",
                            new BasicException.Parameter ("className", this.classDef.jdoGetObjectId()),
                            new BasicException.Parameter ("featureName", featureName)
                        );
                    }
                }                    
                String beanGetterName = AbstractNames.openmdx2AccessorName(
                    featureName,
                    true, // forQuery
                    PrimitiveTypes.BOOLEAN.equals(featureDef.getModel().getElementType(featureDef).objGetValue("qualifiedName")), // isBoolean
                    ModelHelper.getMultiplicity(featureDef).isSingleValued()
                );
                String accessorName = Identifier.OPERATION_NAME.toIdentifier(beanGetterName);
                try {
                    return Maps.putUnlessPresent(
                        this.accessors,
                        featureName,
                        targetIntf.getMethod(accessorName)
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
        } else {
            return accessor;
        }
    }

    //------------------------------------------------------------------------
    Method getMutator(
        Object feature
    ) throws ServiceException {
        String featureName;
        ModelElement_1_0 featureDef;
        if(feature instanceof String) {
            featureDef = null;
            featureName = getSimpleName((String) feature);
        } else {
            featureDef = ((RefMetaObject_1) feature).getElementDef();
            featureName = (String) featureDef.objGetValue("name");
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
                String beanSetterName = AbstractNames.openmdx2AccessorName(
                    featureName,
                    false, // forQuery
                    PrimitiveTypes.BOOLEAN.equals(featureDef.getModel().getElementType(featureDef).objGetValue("qualifiedName")), // isBooleam
                    ModelHelper.getMultiplicity(featureDef).isSingleValued()
                );
                String mutatorName = Identifier.OPERATION_NAME.toIdentifier(beanSetterName);
                for(Method method : targetIntf.getMethods()) {
                    if(
                            method.getName().equals(mutatorName) &&
                            (method.getParameterTypes().length == 1) &&
                            (method.getReturnType() == void.class)
                            // Note: The argument type is ignored for the moment 
                    ){
                        return Maps.putUnlessPresent(
                            this.mutators,
                            featureName,
                            method
                        );
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
    ) throws ServiceException {
        String featureName;
        if(feature instanceof String) {
            featureName = (String) feature;
            if(featureName.indexOf(":") > 0) {
                featureName = featureName.substring(featureName.lastIndexOf(":") + 1);
            }                
        } else {
            ModelElement_1_0 featureDef = ((RefMetaObject_1) feature).getElementDef();
            featureName = (String) featureDef.objGetValue("name");
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
                    method.getName().equals(operationName) &&
                    (method.getReturnType() != void.class)
                ) {
                    return Maps.putUnlessPresent(
                        this.operations,
                        featureName, 
                        method
                    );
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
        if(oldMethod == null) {
            String methodName = source.getName();
            Class<?>[] methodArguments = source.getParameterTypes();
            for(Method newMethod : targetIntf.getMethods()) {
                if(
                    (newMethod.getName().equals(methodName)) &&
                    (newMethod.getParameterTypes().length == methodArguments.length)
                ) {
                    boolean areEqual = true;
                    for(int i = 0; i < methodArguments.length; i++) {
                        if(!newMethod.getParameterTypes()[i].isAssignableFrom(methodArguments[i])) {
                            areEqual = false;
                            break;
                        }
                    }
                    if(areEqual) {
                        return Maps.putUnlessPresent(
                            this.mapping,
                            source, 
                            newMethod
                        );
                    }
                }
            }
        }
        return oldMethod;
    }

    //-----------------------------------------------------------------------        
    @SuppressWarnings("rawtypes")
    ModelElement_1_0 getFeature(
        String methodName,
        MethodSignature mode
    ) throws ServiceException {
        String className = (String)this.classDef.objGetValue("qualifiedName");
        ConcurrentMap<String,ModelElement_1_0> features = allFeatures.get(className);
        if(features == null) {
            features = Maps.putUnlessPresent(
                allFeatures,
                className, 
                new ConcurrentHashMap<String,ModelElement_1_0>()
            );
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
            Map allFeature = classDef.objGetMap("allFeature");
            for(Iterator<?> i = allFeature.values().iterator(); i.hasNext(); ) {
                feature = (ModelElement_1_0)i.next();
                // Operation
                if(model.isOperationType(feature)) {
                    operations.add(feature);
                }
                // Structural feature
                else {
                    String featureName = (String)feature.objGetValue("name");
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
                String operationName = (String)operation.objGetValue("name");                
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
                    new BasicException.Parameter("method.name", methodName),
                    new BasicException.Parameter("class.name", className)
                );                
            }
        }
        return feature;
    }
    
    //-----------------------------------------------------------------------        
    private ModelElement_1_0 getFeatureDef(Object feature)
        throws ServiceException {
        ModelElement_1_0 featureDef = feature instanceof String ? this.classDef.getModel().getFeatureDef(
            this.classDef,
            getSimpleName((String)feature),
            false
        ) : ((RefMetaObject_1) feature).getElementDef();
        if(featureDef == null) {
            throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.NOT_FOUND, 
                "feature not found in class",
                new BasicException.Parameter("feature.name", feature),
                new BasicException.Parameter("class.name", this.classDef)
            );                
        }
        return featureDef;
    }
    
    //-----------------------------------------------------------------------        
    Multiplicity getMultiplicity(
        Object feature
    ) throws ServiceException {
        ModelElement_1_0 featureDef = getFeatureDef(feature);
        return ModelHelper.getMultiplicity(featureDef);
    }

    //-----------------------------------------------------------------------        
    Kind getKind(
        Object feature
    ) throws ServiceException {
        Boolean query = (Boolean) getFeatureDef(feature).objGetValue("isQuery");
        return 
            query == null ? Kind.METHOD : 
            query.booleanValue() ? Kind.QUERY_OPERATION : Kind.NON_QUERY_OPERATION;
    }
    
    //------------------------------------------------------------------------
    // Members
    //------------------------------------------------------------------------
    private static final long serialVersionUID = 4846709494755003575L;
    protected final static ConcurrentMap<String,ConcurrentMap<String,ModelElement_1_0>> allFeatures = 
        new ConcurrentHashMap<String,ConcurrentMap<String,ModelElement_1_0>>();
    
    private transient final ConcurrentMap<String,Method> accessors = new ConcurrentHashMap<String,Method>();
    private transient final ConcurrentMap<String,Method> mutators = new ConcurrentHashMap<String,Method>();
    private transient final ConcurrentMap<String,Method> operations = new ConcurrentHashMap<String,Method>();
    private transient final ConcurrentMap<Method,Method> mapping = new ConcurrentHashMap<Method,Method>();
    private transient final ConcurrentMap<String,Method> collections = new ConcurrentHashMap<String,Method>();
    private transient final ModelElement_1_0 classDef;    
    private transient final Class<?> targetIntf;    

}