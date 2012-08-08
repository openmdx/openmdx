/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Jmi1ObjectInvocationHandler.java,v 1.61 2008/06/27 14:15:53 hburger Exp $
 * Description: JMI 1 Object Invocation Handler 
 * Revision:    $Revision: 1.61 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/27 14:15:53 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.JDOHelper;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_3;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_4;
import org.openmdx.base.accessor.jmi.cci.RefObjectFactory_1.LegacyContainer;
import org.openmdx.base.collection.MarshallingCollection;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.spi.Entity_2_0;
import org.openmdx.base.persistence.spi.Marshaller;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.mapping.java.Identifier;
import org.w3c.cci2.AnyTypePredicate;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.Container;
import org.w3c.cci2.LargeObject;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;

/**
 * JMI 1 Object Invocation Handler
 */
public class Jmi1ObjectInvocationHandler 
    implements InvocationHandler, Serializable 
{

    /**
     * Constructor 
     *
     * @param delegate
     * @param refClass
     */
    public Jmi1ObjectInvocationHandler(
        PersistenceCapable delegate,
        Jmi1Class refClass
    ) {
        this(
            new StandardRefObject_1(delegate,refClass),
            refClass,
            false, // ignoreImpls, 
            false // legacyDelegate
        );
    }

    /**
     * Constructor 
     *
     * @param delegation
     * @param refClass
     */
    public Jmi1ObjectInvocationHandler(
        Object_1_0 delegation,
        Jmi1Class refClass
    ) {
        this(
            delegation,
            refClass,
            false // ignoreImpls
        );
    }

    
    /**
     * Constructor 
     *
     * @param delegation
     * @param refClass
     * @param ignoreImpls
     */
    public Jmi1ObjectInvocationHandler(
        Object_1_0 delegation,
        Jmi1Class refClass,
        boolean ignoreImpls
    ) {
        this(
            new LegacyRefObject_1(delegation,refClass),
            refClass,
            ignoreImpls, 
            true // legacyDelegate
        );
    }

    /**
     * Constructor 
     *
     * @param delegate
     * @param refClass
     * @param ignoreImpls
     * @param legacyDelegate
     */
    private Jmi1ObjectInvocationHandler(
        RefObject delegate,
        Jmi1Class refClass,
        boolean ignoreImpls, 
        boolean legacyDelegate
    ) {
        this.legacyDelegate = legacyDelegate;
        this.refDelegate = delegate;
        this.ignoreImpls = ignoreImpls;
        this.refClass = refClass;
        this.impls = new HashMap<String,Object>();
        this.featuresHavingNoImpl = refClass.refFeaturesHavingNoImpl();
        this.featureMapper = refClass.getFeatureMapper();
    }

    private static final long serialVersionUID = 7709571315051480193L;

    protected final boolean legacyDelegate;
    protected final RefObject refDelegate;
    protected final boolean ignoreImpls;
    protected final Jmi1Class refClass;
    protected final Map<String,Object> impls;
    protected final Set<ModelElement_1_0> featuresHavingNoImpl;
    protected final FeatureMapper featureMapper;
    
    //-----------------------------------------------------------------------
    private InvocationTarget getInvocationTarget(
        Object object,
        Method method, 
        boolean lenient
    ) {
        if(object != null) {
            String declaredName = method.getName();
            Class<?>[] declaredParameterTypes = method.getParameterTypes();
            int declaredParameterCount = declaredParameterTypes.length;
            ImplementedMethod: for(Method implementedMethod : object.getClass().getMethods()) {
                if(declaredName.equals(implementedMethod.getName())){
                    Class<?>[] implementedParameterTypes = implementedMethod.getParameterTypes();
                    if(declaredParameterCount == implementedParameterTypes.length) {
                        for(
                           int i = 0;
                           i < declaredParameterCount;
                           i++
                        ){
                            boolean matches = lenient ?
                                declaredParameterTypes[i].isAssignableFrom(implementedParameterTypes[i]) :
                                declaredParameterTypes[i] == implementedParameterTypes[i];
                            if(!matches) {
                                continue ImplementedMethod;
                            }
                        }
                    }
                    return new InvocationTarget(object, implementedMethod);
                }
            }
        }
        return null;
    }
            
    //-----------------------------------------------------------------------
    protected InvocationTarget getImpl(
        Object self,
        Object next,
        Method method, 
        ModelElement_1_0 feature, 
        boolean lenient
    ) {
        if(
            this.ignoreImpls ||
            this.featuresHavingNoImpl.contains(feature)
        ) {
            return null;
        }
        // Get impl for instance class
        String instanceQualifiedClassName = this.refClass.refMofId();
        RefPackage_1_4 refPackage = (RefPackage_1_4) refClass.refOutermostPackage();
        if(
            (this.impls.get(instanceQualifiedClassName) == null) &&
            !this.impls.containsKey(instanceQualifiedClassName)
        ) {        
            Object userImpl = this.legacyDelegate ? refPackage.refCreateImpl(
                instanceQualifiedClassName, 
                (RefObject_1_0)self
            ) : refPackage.refCreateImpl(
                instanceQualifiedClassName, 
                self,
                next
            );
            this.impls.put(
                instanceQualifiedClassName, 
                userImpl
            );
        }
        // Get impl for declaring class
        String qualifiedFeatureName = (String)feature.values("qualifiedName").get(0);
        String declaredQualifiedClassName = qualifiedFeatureName.substring(
            0, 
            qualifiedFeatureName.lastIndexOf(":")
        );
        if(
            (this.impls.get(declaredQualifiedClassName) == null) &&
            !this.impls.containsKey(declaredQualifiedClassName)
        ) {        
            Object userImpl = this.legacyDelegate ? refPackage.refCreateImpl(
                declaredQualifiedClassName, 
                (RefObject_1_0)self
            ) : refPackage.refCreateImpl(
                declaredQualifiedClassName, 
                self,
                next
            );
            this.impls.put(
                declaredQualifiedClassName, 
                userImpl
            );
        }
        Object impl = this.impls.get(instanceQualifiedClassName);
        InvocationTarget invocationTarget = getInvocationTarget(impl, method, lenient);
        if(invocationTarget != null){
            return invocationTarget;
        }
        impl = this.impls.get(declaredQualifiedClassName);
        invocationTarget = this.getInvocationTarget(impl, method, lenient);
        if(invocationTarget != null) {
            return invocationTarget;
        }
        this.featuresHavingNoImpl.add(feature);
        return null;
    }
    
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @SuppressWarnings("unchecked")
    public Object invoke(
        Object proxy, 
        Method method,
        Object[] args
    ) throws Throwable {
        String methodName = method.getName().intern(); 
        Class<?> declaringClass = method.getDeclaringClass();
        try {
            if (declaringClass == Object.class) {
                if("toString" == methodName) {
                    return this.refDelegate.toString();
                } else if("equals" == methodName) {
                    return args[0].equals(this.refDelegate); // TODO validate
                } else if("hashCode" == methodName) {
                    return this.refDelegate.hashCode();
                }
            } else if (this.legacyDelegate) {
                //
                // Object_1_0 delegate
                //
                if(declaringClass == PersistenceCapable.class) {
                    return method.invoke(
                        this.refDelegate, 
                        args
                    );
                } else if (
                    declaringClass == RefBaseObject.class ||
                    declaringClass == RefFeatured.class ||
                    declaringClass == RefObject.class ||
                    declaringClass == RefObject_1_0.class ||
                    declaringClass == RefObject_1_1.class ||
                    declaringClass == RefObject_1_2.class ||
                    declaringClass == RefObject_1_3.class
                ){
                    return method.invoke(
                        this.refDelegate, 
                        args
                    );
                } else {
                    ModelElement_1_0 feature = this.featureMapper.getFeature(
                        methodName, 
                        method.getReturnType() == void.class
                            ? FeatureMapper.MethodSignature.RETURN_IS_VOID
                            : FeatureMapper.MethodSignature.DEFAULT
                    );
                    InvocationTarget invocationTarget = this.getImpl(
                        proxy,
                        null,
                        method, 
                        feature, 
                        false // lenient
                    );
                    // Dispatch to user-defined implementation
                    if(invocationTarget != null) {
                        return invocationTarget.invoke(args);
                    }
                    // Generic delegate 
                    else {
                      boolean isOperation = feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.OPERATION);
                        String featureName = (String)feature.values("name").get(0);
                        // Getters
                        if(!isOperation && methodName.startsWith("get")) {
                            if((args == null) || (args.length == 0)) {           
                                Object value = this.refDelegate.refGetValue(
                                    featureName
                                );
                                return 
                                    value instanceof InputStream ? new Jmi1BinaryLargeObject(featureName, (InputStream)value) :
                                    value instanceof SortedMap ? SortedMaps.asSparseArray((SortedMap<Integer,?>)value) :
                                    value instanceof RefContainer ? Classes.newProxyInstance(
                                        new Jmi1ContainerInvocationHandler((RefContainer)value),
                                        method.getReturnType(), RefContainer.class, Serializable.class, LegacyContainer.class
                                    ) : 
                                    value;
                            }
                            // Query
                            else if(args.length == 1 && args[0] instanceof AnyTypePredicate) {
                                return ((RefContainer)
                                    this.refDelegate.refGetValue(featureName)
                                ).refGetAll(
                                    args[0]
                                );
                            }
                            // Qualifier
                            else {
                                Object qualifier;
                                if(args.length == 2 && args[0] instanceof Boolean) {
                                    qualifier = ((Boolean)args[0]).booleanValue() ? "!" + args[1] : args[1];
                                } else {
                                    qualifier = args[0];
                                }
                                return ((RefObject_1_3)this.refDelegate).refGetValue(
                                    featureName,
                                    qualifier
                                );
                            }
                        }
                        // Boolean getters
                        else if(!isOperation && methodName.startsWith("is")) {
                            if((args == null) || (args.length == 0)) {  
                                return this.refDelegate.refGetValue(
                                    featureName
                                );
                            }
                        }
                        // Setters
                        else if(!isOperation && methodName.startsWith("set")) {
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
                            methodName.startsWith("add") &&
                            args != null && 
                            args.length == 3 && 
                            args[0].getClass() == Boolean.class && (
                                args[1] instanceof String ||
                                args[1] instanceof Number ||
                                args[1].getClass().isPrimitive() 
                            )
                        ) {
                            ((RefObject_1_3)this.refDelegate).refAddValue(
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
            } else {
                //
                // Standard Delegate
                //
                if(declaringClass == PersistenceCapable.class) {
                    if("jdoGetPersistenceManager" == methodName) {
                        return ((RefPackage_1_1)this.refClass.refOutermostPackage()).refPersistenceManager();
                    } else {
                        return method.invoke(
                            ((Entity_2_0)this.refDelegate).openmdxjdoGetDataObject(), 
                            args
                        );
                    }
                } else if(declaringClass == Entity_2_0.class) {
                    return method.invoke(
                        this.refDelegate, 
                        args
                    );
                } else if(
                    declaringClass == RefBaseObject.class ||
                    declaringClass == RefFeatured.class ||
                    declaringClass == RefObject.class ||
                    declaringClass == RefObject_1_0.class
                ){
                    if("refGetValue" == methodName) {
                        if(args.length == 1) {
                            return standardDelegation(
                                proxy, 
                                this.featureMapper.getAccessor(args[0]), 
                                null, 
                                this.refClass.getMarshaller()
                            );
                        }
                        if(args.length == 3) {
                            LargeObject largeObject = (LargeObject) standardDelegation(
                                proxy, 
                                this.featureMapper.getAccessor(args[0]), 
                                null, 
                                this.refClass.getMarshaller()
                            );
                            long position = args[2] == null ? 0l : (Long)args[2];
                            if(largeObject instanceof BinaryLargeObject) {
                                ((BinaryLargeObject)largeObject).getContent(
                                    (OutputStream)args[1],
                                    position
                                );
                            } else {
                                ((CharacterLargeObject)largeObject).getContent(
                                    (Writer)args[1],
                                    position
                                );
                            }
                            return largeObject.getLength();
                        }
                    } else if ("refSetValue" == methodName && args.length == 2) {
                        return standardDelegation(
                            proxy,
                            this.featureMapper.getMutator(args[0]), 
                            new Object[]{args[1]}, 
                            this.refClass.getMarshaller()
                        );
                    } else if("refInvokeOperation" == methodName && args.length == 2){
                        return standardDelegation(
                            proxy,
                            this.featureMapper.getOperation(args[0]), 
                            ((List)args[1]).toArray(), 
                            this.refClass.getMarshaller()
                        );
                    } else {
                        StandardMarshaller marshaller = this.refClass.getMarshaller(); 
                        return marshaller.marshal(
                            method.invoke(
                                this.refDelegate, 
                                marshaller.unmarshal(args)
                            )
                        );
                    }
                } else {
                    ModelElement_1_0 feature = this.featureMapper.getFeature(
                        methodName,
                        method.getReturnType() == void.class
                            ? FeatureMapper.MethodSignature.RETURN_IS_VOID
                            : FeatureMapper.MethodSignature.DEFAULT
                    );
                    if(
                        !feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.OPERATION) &&
                        methodName.length() > 3 &&
                        args != null && args.length > 0
                     ) {
                        boolean makePersistent = methodName.startsWith("add"); 
                        if(
                             makePersistent ||
                             methodName.startsWith("get") 
                        ){
                            //
                            // RefContainer operations
                            //
                            RefContainer container = (RefContainer) standardDelegation(
                                proxy,
                                this.featureMapper.getAccessor(feature.values("name").get(0)), 
                                null, 
                                this.refClass.getMarshaller()
                            );
                            if(makePersistent) {
                                //
                                // Make Persistent
                                //
                                container.refAdd(jmiToRef(args));
                                return null;
                            } else if (args[0] instanceof AnyTypePredicate){
                                //
                                // Execute Query
                                //
                                return container.refGetAll(args[0]);
                            } else {
                                //
                                // Retrieve An Object
                                //
                                return container.refGet(jmiToRef(args));
                            }
                        }
                        if(methodName.startsWith("set")) {
                            boolean array = args[0] instanceof Object[];
                            if(array || args[0] instanceof Collection) {
                                //
                                // Replace collection content
                                //
                                Collection collection = (Collection) standardDelegation(
                                    proxy,
                                    this.featureMapper.getCollection(methodName), 
                                    null, 
                                    this.refClass.getMarshaller()
                                );
                                collection.clear();
                                collection.addAll(
                                    array ? Arrays.asList((Object[])args[0]) : (Collection)args[0]
                                );
                                return null;
                            }
                        }
                    }
                    return standardDelegation(
                        proxy,
                        this.featureMapper.getMethod(method), 
                        args, 
                        this.refClass.getMarshaller()
                    );
                }
            }
        } catch(InvocationTargetException e) {
            throw e.getTargetException();
        }
        throw new UnsupportedOperationException(methodName);
    }
    
    private static Object[] jmiToRef(
        Object[] source
    ){
        if(source == null) {
            return null;
        } else {
            int size = source.length;
            if(
                size == 1 &&
                source[0] instanceof String || source[0] instanceof Number
            ){
                return new Object[]{RefContainer.REASSIGNABLE, source[0]};
            }  else if(
                size == 2 &&
                source[0] instanceof String
            ){
                return new Object[]{RefContainer.REASSIGNABLE, source[0], source[1]};
            } else {
                Object[] target = new Object[size];
                for(
                    int i = 0, iLimit = size - 1;
                    i <= iLimit;
                    i++
                ){
                    target[i] = 
                        i % 2 == 1 || i == iLimit ? source[i] : 
                        ((Boolean)source[i]) ? RefContainer.PERSISTENT : RefContainer.REASSIGNABLE; 
                }
                return target;
            }
        }
    }
    
    /**
     * Standard Delegation
     * 
     * @param proxy 
     * @param method
     * @param args
     * @param marshaller 
     * 
     * @return the result
     * 
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ServiceException 
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     */
    protected Object standardDelegation(
        Object proxy,
        Method method, 
        Object[] args, 
        StandardMarshaller marshaller
    ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ServiceException, SecurityException, NoSuchMethodException{
        String methodName = method.getName();
        ModelElement_1_0 feature = this.featureMapper.getFeature(
            methodName, 
            method.getReturnType() == void.class
                ? FeatureMapper.MethodSignature.RETURN_IS_VOID
                : FeatureMapper.MethodSignature.DEFAULT
        );
        Object next = ((Entity_2_0)this.refDelegate).openmdxjdoGetDelegate(); 
        InvocationTarget invocationTarget = this.getImpl(
            proxy,
            next,
            method, 
            feature, 
            true // lenient
        );
        boolean hasVoidArg = (method.getParameterTypes().length == 0) && (args != null) && (args.length == 1) && (args[0] instanceof org.openmdx.base.cci2.Void);
        if(invocationTarget == null) {
            return marshaller.marshal(
                method.invoke(
                    next, 
                    hasVoidArg ? null : marshaller.unmarshal(args)
                )
            );
        } else {
            Object reply = invocationTarget.invoke(
                hasVoidArg ? null : args
            );
            if(reply instanceof Container && !(reply instanceof RefContainer)) {
                return Classes.newProxyInstance(
                    new Jmi1ContainerInvocationHandler(
                        null, // marshaller
                        (Container<?>) reply
                    ),
                    method.getReturnType(), RefContainer.class, Serializable.class
                );
            } else {
                return reply;
            }
        }
    }
    
    /**
     * MarshallingContainer
     */
    @SuppressWarnings("unchecked")
    static class MarshallingContainer
        extends MarshallingCollection
        implements RefContainer
     {

        /**
         * Constructor 
         *
         * @param marshaller
         * @param delegate
         */
        MarshallingContainer(
            StandardMarshaller marshaller,
            RefContainer delegate
        ){
            super(marshaller, (Collection) delegate);
            this.marshaller = marshaller;
            this.delegate = delegate;
        }

        /**
         * Implements <ocde>Serializable</code>
         */
        private static final long serialVersionUID = 6428539771257840145L;

        /**
         * 
         */
        protected final StandardMarshaller marshaller;
        
        /**
         * 
         */
        protected final RefContainer delegate;
        
        /* (non-Javadoc)
         * @see org.oasisopen.jmi1.RefContainer#refAdd(java.lang.Object[])
         */
        public void refAdd(Object... arguments) {
            this.delegate.refAdd(this.marshaller.unmarshal(arguments));
        }

        /* (non-Javadoc)
         * @see org.oasisopen.jmi1.RefContainer#refGet(java.lang.Object[])
         */
        public Object refGet(Object... arguments) {
            return this.marshaller.marshal(
                this.delegate.refGet(this.marshaller.unmarshal(arguments))
            );
        }

        /* (non-Javadoc)
         * @see org.oasisopen.jmi1.RefContainer#refGetAll(java.lang.Object)
         */
        public List<?> refGetAll(Object query) {
            return this.marshaller.marshal(
                this.delegate.refGetAll(query)
            );
        }

        /* (non-Javadoc)
         * @see org.oasisopen.jmi1.RefContainer#refRemove(java.lang.Object[])
         */
        public void refRemove(Object... arguments) {
            this.delegate.refRemove(this.marshaller.unmarshal(arguments));
        }

        /* (non-Javadoc)
         * @see org.oasisopen.jmi1.RefContainer#refRemoveAll(java.lang.Object)
         */
        public void refRemoveAll(Object query) {
            this.delegate.refRemoveAll(query);
        }

    }
    
    //------------------------------------------------------------------------
    // Class StandardMarshaller
    //------------------------------------------------------------------------
    
    /**
     * Class StandardMarshaller
     */
    public static class StandardMarshaller implements Marshaller {

        StandardMarshaller(
            org.openmdx.compatibility.base.marshalling.Marshaller delegate
        ){
            this.delegate = delegate;
        }
        
        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 3399640551686160240L;
        
        /**
         * 
         */
        private final org.openmdx.compatibility.base.marshalling.Marshaller delegate;
        
        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Marshaller#unmarshal(java.lang.Object)
         */
        public Object unmarshal(
            Object source
        ){
            try {
                return 
                    source instanceof Object[] ? unmarshal((Object[])source) :
                    this.delegate.unmarshal(source);
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Marshaller#marshal(java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        public Object marshal(
            Object source
        ){
            try {
                return 
                    source instanceof Object[] ? marshal((Object[])source) :
                    source instanceof PersistenceCapable ? this.delegate.marshal(source) : 
                    source instanceof List ? marshal((List)source) :
                    source instanceof Set ? new MarshallingSet(this, (Set)source) :
                    source instanceof SparseArray ? SortedMaps.asSparseArray(new MarshallingSortedMap(this, (SparseArray)source)) :
                    source instanceof Iterator ? new MarshallingIterator((Iterator)source) :
                    source instanceof Container ? Classes.newProxyInstance(
                        new Jmi1ContainerInvocationHandler(this, (Container)source),
                        source.getClass().getInterfaces()[0], RefContainer.class, Serializable.class 
                    ) :
                    source;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        @SuppressWarnings("unchecked")
        public final List marshal(
            List source
        ){
            return new MarshallingList(this, source);
        }
        
        /**
         * Unmarshal an array of objects
         * 
         * @param source
         * 
         * @return an array containing the unmarshalled objects
         */
        public final Object[] unmarshal(
            Object[] source
        ){
            if(source != null && source.length > 0) {
                for(
                    int i = 0, l = source.length;
                    i < l;
                    i++
                ){ 
                    Object s = source[i];
                    Object t = unmarshal(s);
                    if(s != t) {
                        Object[] target = new Object[source.length];
                        System.arraycopy(source, 0, target, 0, i);
                        target[i] = t;
                        for(
                            int j = i + 1;
                            j < l;
                            j++
                        ){
                            target[j] = unmarshal(source[j]);
                        }
                        return target;
                    }
                }
            }
            return source;
        }

        /**
         * Marshal an array of objects
         * 
         * @param source
         * 
         * @return an array containing the marshalled objects
         */
        public final Object[] marshal(
            Object[] source
        ){
            if(source != null && source.length > 0) {
                for(
                    int i = 0, l = source.length;
                    i < l;
                    i++
                ){ 
                    Object s = source[i];
                    Object t = marshal(s);
                    if(s != t) {
                        Object[] target = new Object[source.length];
                        System.arraycopy(source, 0, target, 0, i);
                        target[i] = t;
                        for(
                            int j = i + 1;
                            j < l;
                            j++
                        ){
                            target[j] = marshal(source[j]);
                        }
                        return target;
                    }
                }
            }
            return source;
        }

        /**
         * MarshallingIterator
         */
        class MarshallingIterator<T> implements Iterator<T> {
            
            /**
             * Constructor 
             *
             * @param delegate
             */
            MarshallingIterator(
                Iterator<?> delegate
            ){
                this.delegate = delegate;
            }
            
            /**
             * 
             */
            private final Iterator<?> delegate;

            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            @SuppressWarnings("unchecked")
            public T next() {
                return (T) marshal(this.delegate.next());
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                this.delegate.remove();
            }
            
        }
        
    }
    
    //-----------------------------------------------------------------------
    // Class LegacyRefObject_1
    //-----------------------------------------------------------------------

    /**
     * Legacy RefObject Implementation 1
     */
    static class LegacyRefObject_1 extends RefObject_1 {

        /**
         * Constructor 
         *
         * @param delegation
         * @param refClass
         */
        public LegacyRefObject_1(
            Object_1_0 delegation,
            RefClass refClass        
        ) {
            super(
                delegation,
                refClass
            );
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -8725046946085576144L;

    }

    
    //------------------------------------------------------------------------
    // StandardRefObject_1
    //------------------------------------------------------------------------
    
    /**
     * Standard RefObject Implementation 1
     */
    static class StandardRefObject_1 
        implements RefObject, RefObject_1_0, Entity_2_0, Serializable {

        /**
         * Constructor 
         *
         * @param delegate
         * @param refClass
         */
        StandardRefObject_1(
            Object delegate,
            RefClass refClass
        ){
            this.cciDelegate = delegate;
            this.refClass = refClass;
        }
        
        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 8761679181457452801L;

        private Object cciDelegate;
        
        private final RefClass refClass;        
            
        private RefObject metaObject = null;
        
        private final static String REFLECTIVE = 
            "This reflective method should be dispatched by the invocation " +
            "handler to its non-reflective counterpart"; 

        private final static String STANDARD = 
            "This JMI method is not supported by CCI delegates"; 

        private RefFeatured refObject (
            Path objectId
        ){
            return ((RefPackage_1_0)this.refClass.refOutermostPackage()).refObject(objectId.toXri());
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refGetPath()
         */
        public Path refGetPath(
        ) {
            return (Path) JDOHelper.getObjectId(this.cciDelegate);
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refClass()
         */
        public RefClass refClass() {
            return this.refClass;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refDelete()
         */
        public void refDelete() {
            JDOHelper.getPersistenceManager(
                this.cciDelegate
            ).deletePersistent(
                this.cciDelegate
            );
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refImmediateComposite()
         */
        public RefFeatured refImmediateComposite() {
            Path objectId = refGetPath();
            if(objectId == null) {
                return null;
            } else {
                int s = objectId.size();
                return s == 1 ? this : refObject(objectId.getPrefix(s - 2));
            }
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refIsInstanceOf(javax.jmi.reflect.RefObject, boolean)
         */
        public boolean refIsInstanceOf(
            RefObject objType,
            boolean considerSubtypes
        ) {
            try {
                Model_1_0 model = ((RefPackage_1_0)this.refClass.refOutermostPackage()).refModel();
                if (model.isClassType(objType)) {
                    return model.isSubtypeOf(this.refClass.refMofId(), objType.refMofId());
                } else throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("objType.refClass", objType.refClass().refMofId()),
                    },
                    "objType must be a class type"
                ); 
            } catch (ServiceException e) {
                throw new JmiServiceException(e, this);
            }  catch (RuntimeServiceException e) {
                throw new JmiServiceException(e, this);
            }
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refOutermostComposite()
         */
        public RefFeatured refOutermostComposite() {
            Path objectId = refGetPath();
            if(objectId == null) {
                return null;
            } else {
                int s = objectId.size();
                return s == 1 ? this : refObject(objectId.getPrefix(1));
            }
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refGetValue(javax.jmi.reflect.RefObject)
         */
        public Object refGetValue(
            RefObject feature
        ) {
            throw newUnsupportedOperationException(REFLECTIVE, feature);
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refGetValue(java.lang.String)
         */
        public Object refGetValue(
            String featureName
        ) {
            throw newUnsupportedOperationException(REFLECTIVE, featureName);
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refInvokeOperation(javax.jmi.reflect.RefObject, java.util.List)
         */
        @SuppressWarnings("unchecked")
        public Object refInvokeOperation(
            RefObject requestedOperation, 
            List args
        ) throws RefException {
            throw newUnsupportedOperationException(REFLECTIVE, requestedOperation);
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refInvokeOperation(java.lang.String, java.util.List)
         */
        @SuppressWarnings("unchecked")
        public Object refInvokeOperation(
            String requestedOperation, 
            List args
        ) throws RefException {
            throw newUnsupportedOperationException(REFLECTIVE, requestedOperation);
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refSetValue(javax.jmi.reflect.RefObject, java.lang.Object)
         */
        public void refSetValue(
            RefObject feature, 
            Object value
        ) {
            throw newUnsupportedOperationException(REFLECTIVE, feature);
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refSetValue(java.lang.String, java.lang.Object)
         */
        public void refSetValue(
            String featureName, 
            Object value
        ) {
            throw newUnsupportedOperationException(REFLECTIVE, featureName);
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refImmediatePackage()
         */
        public RefPackage refImmediatePackage() {
            return this.refClass.refImmediatePackage();
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refMetaObject()
         */
        public RefObject refMetaObject(
        ) {
            if (this.metaObject == null) try {
                this.metaObject = new RefMetaObject_1(
                    ((RefPackage_1_3)refClass.refOutermostPackage()).refModel().getElement(this.refClass().refMofId())
                );
            } catch (ServiceException e) {
                throw new JmiServiceException(e, this);
            }
            return this.metaObject;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refMofId()
         */
        public String refMofId() {
            Path objectId = refGetPath();
            return objectId == null ? null : objectId.toXri();
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refOutermostPackage()
         */
        public RefPackage refOutermostPackage() {
            return this.refClass.refOutermostPackage();
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refVerifyConstraints(boolean)
         */
        @SuppressWarnings("unchecked")
        public Collection refVerifyConstraints(boolean deepVerify) {
            if(this.cciDelegate instanceof RefObject) {
                return ((RefObject)this.cciDelegate).refVerifyConstraints(deepVerify);
            } else throw newUnsupportedOperationException(
                STANDARD, "refVerifyConstraints"
            ); 
        }

        /* (non-Javadoc)
         * org.openmdx.base.persistence.spi.Entity_2_0#openmdxjdoGetDelegate()
         */
        public Object openmdxjdoGetDelegate() {
            return this.cciDelegate;
        }

        
        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Entity_2_0#openmdxjdoGetDataObject()
         */
        public Object openmdxjdoGetDataObject() {
            return this.cciDelegate instanceof Entity_2_0 ?
                ((Entity_2_0)this.cciDelegate).openmdxjdoGetDataObject() :
                this.cciDelegate;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Entity_2_0#openmdxjdoSetDelegate(java.lang.Object)
         */
        public void openmdxjdoSetDelegate(Object delegate) {
            this.cciDelegate = delegate;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refAddEventListener(java.lang.String, java.util.EventListener)
         */
        public void refAddEventListener(
            String feature, 
            EventListener listener
        ) throws ServiceException {
            if(this.cciDelegate instanceof RefObject_1_0) {
                ((RefObject_1_0)this.cciDelegate).refAddEventListener(
                    feature,
                    listener
                );
            } else throw newUnsupportedOperationException(
                STANDARD, "refAddEventListener"
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refAddToUnitOfWork()
         */
        public void refAddToUnitOfWork(
        ) {
            JDOHelper.getPersistenceManager(
                this.cciDelegate
            ).makeTransactional(
                this.cciDelegate
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refAddValue(java.lang.String, java.lang.Object, java.lang.Object)
         */
        public void refAddValue(
            String featureName,
            Object qualifier,
            Object value
        ) {
            throw newUnsupportedOperationException(REFLECTIVE, featureName);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refContext()
         */
        public Object refContext() {
            return ((RefPackage_1_3) this.refOutermostPackage()).refUserContext();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refDefaultFetchGroup()
         */
        public Set<String> refDefaultFetchGroup() {
            if(this.cciDelegate instanceof RefObject_1_0) {
                return ((RefObject_1_0)this.cciDelegate).refDefaultFetchGroup();
            } else throw newUnsupportedOperationException(
                STANDARD,
                "refDefaultFetchGroup"
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refDelegate()
         */
        public Object_1_0 refDelegate() {
            throw newUnsupportedOperationException(
                STANDARD,
                "refDelegate"
            );        
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refFlush()
         */
        public void refFlush() {
            if(this.cciDelegate instanceof RefObject_1_0) {
                ((RefObject_1_0)this.cciDelegate).refFlush();
            } else throw newUnsupportedOperationException(
                STANDARD,
                "refFlush"
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refGetEventListeners(java.lang.String, java.lang.Class)
         */
        public EventListener[] refGetEventListeners(
            String feature,
            Class<? extends EventListener> listenerType
        ) throws ServiceException {
            if(this.cciDelegate instanceof RefObject_1_0) {
                return ((RefObject_1_0)this.cciDelegate).refGetEventListeners(
                    feature,
                    listenerType
                );
            } else throw newUnsupportedOperationException(
                STANDARD,
                "refGetEventListeners"
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refGetValue(javax.jmi.reflect.RefObject, java.lang.Object, boolean)
         */
        public Object refGetValue(
            RefObject feature,
            Object qualifier,
            boolean marshal
        ) {
            throw newUnsupportedOperationException(REFLECTIVE, feature);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refGetValue(java.lang.String, java.lang.Object, long)
         */
        public long refGetValue(String feature, Object value, long position) {
            throw newUnsupportedOperationException(REFLECTIVE, feature);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refInitialize(javax.jmi.reflect.RefObject)
         */
        public void refInitialize(RefObject source) {
            if(this.cciDelegate instanceof RefObject_1_0) {
                ((RefObject_1_0)this.cciDelegate).refInitialize(
                    source
                );
            } else throw newUnsupportedOperationException(
                STANDARD,
                "refInitialize"
            );        
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refInitialize(boolean, boolean)
         */
        public void refInitialize(
            boolean setRequiredToNull,
            boolean setOptionalToNull
        ) {
            if(this.cciDelegate instanceof RefObject_1_0) {
                ((RefObject_1_0)this.cciDelegate).refInitialize(
                    setRequiredToNull,
                    setOptionalToNull
                );
            } else throw newUnsupportedOperationException(
                STANDARD,
                "refInitialize"
            ); 
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refIsDeleted()
         */
        public boolean refIsDeleted() {
            return JDOHelper.isDeleted(this.cciDelegate);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refIsDirty()
         */
        public boolean refIsDirty() {
            return JDOHelper.isDirty(this.cciDelegate);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refIsNew()
         */
        public boolean refIsNew() {
            return JDOHelper.isNew(this.cciDelegate);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refIsPersistent()
         */
        public boolean refIsPersistent() {
            return JDOHelper.isPersistent(this.cciDelegate);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refIsWriteProtected()
         */
        public boolean refIsWriteProtected() {
            if(this.cciDelegate instanceof RefObject_1_0) {
                return ((RefObject_1_0)this.cciDelegate).refIsWriteProtected(
                );
            } else throw newUnsupportedOperationException(
                STANDARD,
                "refIsWriteProtected"
            ); 
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRefresh()
         */
        public void refRefresh() {
            JDOHelper.getPersistenceManager(
                this.cciDelegate
            ).refresh(
                this.cciDelegate
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRefreshAsynchronously()
         */
        public void refRefreshAsynchronously() {
            if(this.cciDelegate instanceof RefObject_1_0) {
                ((RefObject_1_0)this.cciDelegate).refRefreshAsynchronously(
                );
            } else throw newUnsupportedOperationException(
                STANDARD,
                "refRefreshAsynchronously"
            ); 
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRemoveEventListener(java.lang.String, java.util.EventListener)
         */
        public void refRemoveEventListener(
            String feature,
            EventListener listener
        ) throws ServiceException {
            if(this.cciDelegate instanceof RefObject_1_0) {
                ((RefObject_1_0)this.cciDelegate).refRemoveEventListener(
                    feature,
                    listener
                );
            } else throw newUnsupportedOperationException(
                STANDARD,
                "refRemoveEventListener"
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRemoveFromUnitOfWork()
         */
        public void refRemoveFromUnitOfWork() {
            JDOHelper.getPersistenceManager(
                this.cciDelegate
            ).makeNontransactional(
                this.cciDelegate
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRemoveValue(java.lang.String, java.lang.Object)
         */
        public void refRemoveValue(String featureName, Object qualifier) {
            throw newUnsupportedOperationException(REFLECTIVE, featureName);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRemoveValue(java.lang.String, javax.jmi.reflect.RefObject)
         */
        public void refRemoveValue(String featureName, RefObject value) {
            throw newUnsupportedOperationException(REFLECTIVE, featureName);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refSetValue(java.lang.String, java.lang.Object, long)
         */
        public void refSetValue(String feature, Object newValue, long length) {
            throw newUnsupportedOperationException(REFLECTIVE, feature);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refWriteProtect()
         */
        public void refWriteProtect() {
            if(this.cciDelegate instanceof RefObject_1_0) {
                ((RefObject_1_0)this.cciDelegate).refWriteProtect(
                );
            } else throw newUnsupportedOperationException(
                STANDARD,
                "refWriteProtect"
            ); 
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.spi.RefObject_2_0#setDelegate(java.lang.Object)
         */
        public void setDelegate(Object delegate) {
            this.cciDelegate = delegate;
        }
        
        private UnsupportedOperationException newUnsupportedOperationException(
            String message,
            String feature
        ){
            return new UnsupportedOperationException(
                newExceptionMessage(
                    message + ": Feature " + feature + " in class " + this.refClass.refMofId()
                )
            );
        }

        private UnsupportedOperationException newUnsupportedOperationException(
            String message,
            RefObject feature
        ){
            return new UnsupportedOperationException(
                newExceptionMessage(
                    message + ": Feature " + feature.refMofId()
                )
            );
        }

        private String newExceptionMessage(
            String message
        ){
            Object objectId = JDOHelper.getObjectId(this.cciDelegate);
            return objectId == null ?
                message :
                message + " on object " + objectId;
        }
        
    }
    
    
    //------------------------------------------------------------------------
    // Class Jmi1BinaryLargeObject
    //------------------------------------------------------------------------
    
    /**
     * 
     */
    private class Jmi1BinaryLargeObject implements BinaryLargeObject {

        /**
         * Constructor 
         *
         * @param featureName
         * @param value
         */
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
            return this.length;
        }
        
        protected transient InputStream initialValue = null;
        protected final String featureName;
        protected transient Long length = null;
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent(java.io.OutputStream, long)
         */
        public void getContent(
            OutputStream stream, 
            long position
        ) throws IOException {
            this.length = Jmi1ObjectInvocationHandler.this.refDelegate instanceof RefObject_1_0 ?
                ((RefObject_1_0)Jmi1ObjectInvocationHandler.this.refDelegate).refGetValue(
                    this.featureName, 
                    stream, 
                    position
                ) :
                position + BinaryLargeObjects.streamCopy(
                    getContent(), 
                    position,
                    stream
                );            
        }
        
    }

    //------------------------------------------------------------------------
    // Class InvocationTarget
    //------------------------------------------------------------------------

    /**
     * Invocation Target
     */
    static class InvocationTarget {

        /**
         * Constructor 
         *
         * @param object
         * @param method
         */
        InvocationTarget(Object object, Method method) {
            this.object = object;
            this.method = method;
        }       

        /**
         * 
         */
        private final Object object;
        
        /**
         * 
         */
        private final Method method;
        
        /**
         * @param arguments
         * 
         * @return
         * 
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */
        Object invoke(Object... arguments) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return this.method.invoke(this.object, arguments);
        }

    }

}
