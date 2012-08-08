/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Jmi1ObjectInvocationHandler.java,v 1.122 2009/06/09 12:45:17 hburger Exp $
 * Description: JMI 1 Object Invocation Handler 
 * Revision:    $Revision: 1.122 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:17 $
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.jdo.JDOHelper;
import javax.jdo.listener.ClearCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.StoreCallback;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.resource.cci.Record;

import org.oasisopen.jmi1.RefContainer;
import org.omg.mof.spi.Identifier;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.collection.MarshallingCollection;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.jdo.listener.ConstructCallback;
import org.openmdx.kernel.exception.BasicException;
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
    implements InvocationHandler, Serializable {

    /**
     * <em>Reflectively</em> invoked constructor 
     *
     * @param delegate
     * @param refClass
     * 
     * @see RefClass_1#refCreateInstance
     */
    public Jmi1ObjectInvocationHandler(
        PersistenceCapable delegate,
        Jmi1Class_1_0 refClass
    ) {
        this(
            new DelegatingRefObject_1(delegate, refClass),
            refClass,
            false // ignoreImpls, 
        );
    }

    /**
     * <em>Reflectively</em> invoked constructor 
     *
     * @param delegation
     * @param refClass
     * 
     * @see RefClass_1#refCreateInstance
     */
    public Jmi1ObjectInvocationHandler(
        ObjectView_1_0 delegation,
        Jmi1Class_1_0 refClass
    ) {
        this(
            delegation,
            refClass,
            false // ignoreImpls
        );
    }

    //-----------------------------------------------------------------------
    public Jmi1ObjectInvocationHandler(
        ObjectView_1_0 delegation,
        Jmi1Class_1_0 refClass,
        boolean ignoreImpls
    ) {
        this(
            new RefObject_1(delegation, refClass),
            refClass,
            ignoreImpls 
        );
    }

    //-----------------------------------------------------------------------
    private Jmi1ObjectInvocationHandler(
        RefObject delegate,
        Jmi1Class_1_0 refClass,
        boolean ignoreImpls 
    ) {
        this.refDelegate = delegate;
        this.ignoreImpls = ignoreImpls;
        this.refClass = refClass;
        this.impls = new ConcurrentHashMap<String,Object>();
        this.featuresHavingNoImpl = refClass.refFeaturesHavingNoImpl();
        this.featureMapper = refClass.getFeatureMapper();
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 7709571315051480193L;
    
    protected final RefObject refDelegate;
    protected final boolean ignoreImpls;
    protected final Jmi1Class_1_0 refClass;
    protected final Map<String,Object> impls;
    protected final Set<ModelElement_1_0> featuresHavingNoImpl;
    protected final FeatureMapper featureMapper;

    //-----------------------------------------------------------------------
    private static class DelegatingRefObject_1 
        implements DelegatingRefObject_1_0, org.openmdx.base.persistence.spi.Cloneable<RefObject>, Serializable {

        /**
         * Constructor 
         *
         * @param delegate
         * @param refClass
         */
        DelegatingRefObject_1(
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
            } 
            else {
                int s = objectId.size();
                return s == 1 ? this : refOutermostPackage().refObject(objectId.getPrefix(s - 2));
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
                Model_1_0 model = refOutermostPackage().refModel();
                if (model.isClassType(objType)) {
                    return model.isSubtypeOf(this.refClass.refMofId(), objType.refMofId());
                } 
                else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "objType must be a class type",
                        new BasicException.Parameter("objType.refClass", objType.refClass().refMofId())
                    );
                }
            } 
            catch (ServiceException e) {
                throw new JmiServiceException(e, this);
            }  
            catch (RuntimeServiceException e) {
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
            } 
            else {
                int s = objectId.size();
                return s == 1 ? this : refOutermostPackage().refObject(objectId.getPrefix(1));
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
                   refOutermostPackage().refModel().getElement(this.refClass().refMofId())
                );
            } 
            catch (ServiceException e) {
                throw new JmiServiceException(e, this);
            }
            return this.metaObject;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refMofId()
         */
        public String refMofId(
        ) {
            Path objectId = refGetPath();
            return 
                objectId == null ? null : 
                objectId.toXRI();
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refOutermostPackage()
         */
        public Jmi1Package_1_0 refOutermostPackage(
        ) {
            return (Jmi1Package_1_0) this.refClass.refOutermostPackage();
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refVerifyConstraints(boolean)
         */
        @SuppressWarnings("unchecked")
        public Collection refVerifyConstraints(
            boolean deepVerify
        ) {
            if(this.cciDelegate instanceof RefObject) {
                return ((RefObject)this.cciDelegate).refVerifyConstraints(deepVerify);
            } 
            else {
                throw newUnsupportedOperationException(
                    STANDARD, "refVerifyConstraints"
                );
            }
        }

        /* (non-Javadoc)
         * org.openmdx.base.persistence.spi.Entity_2_0#openmdxjdoGetDelegate()
         */
        public Object openmdxjdoGetDelegate(
        ) {
            return this.cciDelegate;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Entity_2_0#openmdxjdoGetDataObject()
         */
        public Object openmdxjdoGetDataObject(
        ) {
            return this.cciDelegate instanceof DelegatingRefObject_1_0 ?
                ((DelegatingRefObject_1_0)this.cciDelegate).openmdxjdoGetDataObject() :
                this.cciDelegate;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Entity_2_0#openmdxjdoSetDelegate(java.lang.Object)
         */
        public void openmdxjdoSetDelegate(
            Object delegate
        ) {
            this.cciDelegate = delegate;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refAddEventListener(java.lang.String, java.util.EventListener)
         */
        public void refAddEventListener(
            EventListener listener
        ){
            if(this.cciDelegate instanceof RefObject_1_0) {
                ((RefObject_1_0)this.cciDelegate).refAddEventListener(
                    listener
                );
            } 
            else {
                throw newUnsupportedOperationException(
                    STANDARD, "refAddEventListener"
                );
            }
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
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refDefaultFetchGroup()
         */
        public Set<String> refDefaultFetchGroup(
        ) {
            if(this.cciDelegate instanceof RefObject_1_0) {
                return ((RefObject_1_0)this.cciDelegate).refDefaultFetchGroup();
            } 
            else {
                throw newUnsupportedOperationException(
                    STANDARD,
                    "refDefaultFetchGroup"
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refDelegate()
         */
        public ObjectView_1_0 refDelegate(
        ) {
            throw newUnsupportedOperationException(
                STANDARD,
                "refDelegate"
            );        
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refGetEventListeners(java.lang.String, java.lang.Class)
         */
        public <T extends EventListener> T[] refGetEventListeners(
            Class<T> listenerType
        ){
            if(this.cciDelegate instanceof RefObject_1_0) {
                return ((RefObject_1_0)this.cciDelegate).refGetEventListeners(
                    listenerType
                );
            } 
            else {
                throw newUnsupportedOperationException(
                    STANDARD,
                    "refGetEventListeners"
                );
            }
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
        public long refGetValue(
            String feature, 
            Object value, 
            long position
        ) {
            throw newUnsupportedOperationException(REFLECTIVE, feature);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refInitialize(javax.jmi.reflect.RefObject)
         */
        public void refInitialize(
            RefObject source
        ) {
            if(this.cciDelegate instanceof RefObject_1_0) {
                ((RefObject_1_0)this.cciDelegate).refInitialize(
                    source
                );
            } 
            else {
                throw newUnsupportedOperationException(
                    STANDARD,
                    "refInitialize"
                );
            }
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
            } 
            else {
                throw newUnsupportedOperationException(
                    STANDARD,
                    "refInitialize"
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRemoveEventListener(java.lang.String, java.util.EventListener)
         */
        public void refRemoveEventListener(
            EventListener listener
        ){
            if(this.cciDelegate instanceof RefObject_1_0) {
                ((RefObject_1_0)this.cciDelegate).refRemoveEventListener(
                    listener
                );
            } 
            else {
                throw newUnsupportedOperationException(
                    STANDARD,
                    "refRemoveEventListener"
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRemoveValue(java.lang.String, java.lang.Object)
         */
        public void refRemoveValue(
            String featureName, 
            Object qualifier
        ) {
            throw newUnsupportedOperationException(REFLECTIVE, featureName);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refRemoveValue(java.lang.String, javax.jmi.reflect.RefObject)
         */
        public void refRemoveValue(
            String featureName, 
            RefObject value
        ) {
            throw newUnsupportedOperationException(REFLECTIVE, featureName);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refSetValue(java.lang.String, java.lang.Object, long)
         */
        public void refSetValue(
            String feature, 
            Object newValue, 
            long length
        ) {
            throw newUnsupportedOperationException(REFLECTIVE, feature);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.spi.RefObject_2_0#setDelegate(java.lang.Object)
         */
        public void setDelegate(
            Object delegate
        ) {
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

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.cciDelegate.hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return this.cciDelegate.toString();
        }
        
        //--------------------------------------------------------------------
        // Implements Cloneable
        //--------------------------------------------------------------------
        
        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Cloneable#openmdxjdoClone()
         */
        public RefObject openmdxjdoClone() {
            return this.refClass.refCreateInstance(
                Collections.singletonList(
                    PersistenceHelper.clone(this.cciDelegate)
                )
            );
        }
        
    }
    
    //-----------------------------------------------------------------------
    private boolean isDelegatingRefObject(
    ) {
        return this.refDelegate instanceof DelegatingRefObject_1;
    }
    
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
                        ) {
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
    private Object getImpl(
        String qualifiedClassName,
        Object self,
        Object next
    ){
        Object instance = this.impls.get(qualifiedClassName);
        if(instance == Maps.NULL) {
            return null;
        } else if (instance == null){ 
            RefRootPackage_1 refPackage = (RefRootPackage_1) refClass.refOutermostPackage();
            instance = this.isDelegatingRefObject() ? 
                refPackage.refCreateImpl(
                    qualifiedClassName, 
                    self,
                    next
                ) :
                refPackage.refCreateImpl(
                    qualifiedClassName, 
                    (RefObject_1_0)self
                );
            this.impls.put(
                qualifiedClassName, 
                instance == null ? Maps.NULL : instance
            );
        }
        return instance;
    }
    
    //-----------------------------------------------------------------------
    protected InvocationTarget getImpl(
        Object self,
        Object next,
        Method method, 
        boolean lenient
    ) throws ServiceException {
        if(this.ignoreImpls) {
            return null;
        }
        RefRootPackage_1 refPackage = (RefRootPackage_1) refClass.refOutermostPackage();
        String instanceQualifiedClassName = this.refClass.refMofId();
        Class<?> declaringClass = method.getDeclaringClass();
        if(refPackage.getMixedInInterfaces(instanceQualifiedClassName).contains(declaringClass)) {
            String classImplementingInterface = refPackage.getClassImplementingInterface(
                instanceQualifiedClassName, 
                declaringClass
            ); 
            return classImplementingInterface == null ? null : this.getInvocationTarget(
                this.getImpl(classImplementingInterface, self, next),
                method, 
                lenient
            );
        }
        ModelElement_1_0 feature = this.featureMapper.getFeature(
            method.getName(), 
            method.getReturnType() == void.class ? 
                FeatureMapper.MethodSignature.RETURN_IS_VOID : 
                FeatureMapper.MethodSignature.DEFAULT
        );
        if(this.featuresHavingNoImpl.contains(feature)) {
            return null;
        }
        InvocationTarget invocationTarget = this.getInvocationTarget(
            this.getImpl(instanceQualifiedClassName, self, next), 
            method, 
            lenient
        );
        if(invocationTarget != null){
            return invocationTarget;
        }
        // Get impl for declaring class
        String qualifiedFeatureName = (String)feature.objGetValue("qualifiedName");
        String declaredQualifiedClassName = qualifiedFeatureName.substring(
            0, 
            qualifiedFeatureName.lastIndexOf(":")
        );
        invocationTarget = this.getInvocationTarget(
            this.getImpl(declaredQualifiedClassName, self, next), 
            method, 
            lenient
        );
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
        String methodName = method.getName(); 
        Class<?> declaringClass = method.getDeclaringClass();
        try {
            if (declaringClass == Object.class) {
                if("hashCode".equals(methodName)) {
                    Object id = PersistenceHelper.getCurrentObjectId(proxy);
                    return id == null ? System.identityHashCode(this) : id.hashCode();
                }
                else if("toString".equals(methodName)) {
                    return this.refDelegate.toString();
                } 
                else if("equals".equals(methodName)) {
                    if(args[0] == null) {
                        return false;
                    }
                    Object left = PersistenceHelper.getCurrentObjectId(args[0]);
                    Object right = PersistenceHelper.getCurrentObjectId(proxy);
                    return left == null ? args[0] == proxy : left.equals(right);
                } 
            } 
            //
            // ref delegation
            //
            else if(!this.isDelegatingRefObject()) {
                if(
                    declaringClass == PersistenceCapable.class ||
                    declaringClass == org.openmdx.base.persistence.spi.Cloneable.class
                ) {                    
                    return method.invoke(
                        this.refDelegate, 
                        args
                    );
                } 
                else if(
                    declaringClass == LoadCallback.class ||
                    declaringClass == StoreCallback.class ||
                    declaringClass == ClearCallback.class ||
                    declaringClass == DeleteCallback.class ||
                    declaringClass == ConstructCallback.class
                ) {
                    throw new UnsupportedOperationException("Callbacks not supported for non-cci delegates. Callback was " + method);
                }
                else if (
                    (declaringClass == RefBaseObject.class) ||
                    (declaringClass == RefObject.class) ||
                    (declaringClass == RefFeatured.class) ||
                    (declaringClass == RefObject_1_0.class) ||
                    (declaringClass == Jmi1Object_1_0.class)
                ) {
                    return method.invoke(
                        this.refDelegate, 
                        args
                    );
                } 
                else {
                    // Dispatch cci method to generic ref-methods
                    ModelElement_1_0 feature = this.featureMapper.getFeature(
                        methodName, 
                        method.getReturnType() == void.class ? 
                            FeatureMapper.MethodSignature.RETURN_IS_VOID : 
                            FeatureMapper.MethodSignature.DEFAULT
                    );
                    boolean isOperation = feature.objGetClass().equals(ModelAttributes.OPERATION);
                    String featureName = (String)feature.objGetValue("name");
                    // Getters
                    if(!isOperation && methodName.startsWith("get")) {
                        if((args == null) || (args.length == 0)) {           
                            Object value = this.refDelegate.refGetValue(
                                featureName
                            );
                            return value instanceof InputStream ? new Jmi1BinaryLargeObject(
                                featureName, 
                                (InputStream)value
                            ) : value instanceof SortedMap ? SortedMaps.asSparseArray(
                                (SortedMap<Integer,?>)value
                            ) : value instanceof RefContainer ? Classes.newProxyInstance(
                                new Jmi1ContainerInvocationHandler(null, (RefContainer)value),
                                method.getReturnType(), RefContainer.class, Serializable.class, LegacyContainer.class
                            ) : value;
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
                            } 
                            else {
                                qualifier = args[0];
                            }
                            return ((Jmi1Object_1_0)this.refDelegate).refGetValue(
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
                        if((args != null) && (args.length == 1)) {                      
                            this.refDelegate.refSetValue(
                                featureName,
                                args[0] instanceof BinaryLargeObject ? 
                                    ((BinaryLargeObject)args[0]).getContent() : 
                                    args[0]
                            );
                            return null;
                        }
                    }
                    // Adders with signature (boolean idIsPersistent, PrimitiveType qualifier, Object object)
                    else if(
                        methodName.startsWith("add") &&
                        (args != null) && 
                        (args.length == 3) && 
                        (args[0].getClass() == Boolean.class) && (
                            args[1] instanceof String ||
                            args[1] instanceof Number ||
                            args[1].getClass().isPrimitive() 
                        )
                    ) {
                        ((Jmi1Object_1_0)this.refDelegate).refAddValue(
                            Identifier.ATTRIBUTE_NAME.toIdentifier(
                                featureName,
                                "add", // removablePrefix
                                null, // prependablePrefix
                                null, // removableSuffix
                                null // appendableSuffix
                            ), 
                            ((Boolean)args[0]).booleanValue() ? "!" + args[1] : args[1], 
                            args[2]
                        );
                        return null;
                    }
                    // Adders with signature (PrimitiveType qualifier, Object object)
                    else if(
                        methodName.startsWith("add") &&
                        (args != null) && 
                        (args.length == 2) && (
                            args[0] instanceof String ||
                            args[0] instanceof Number ||
                            args[0].getClass().isPrimitive() 
                        )
                    ) {
                        ((Jmi1Object_1_0)this.refDelegate).refAddValue(
                            Identifier.ATTRIBUTE_NAME.toIdentifier(
                                featureName,
                                "add", // removablePrefix
                                null, // prependablePrefix
                                null, // removableSuffix
                                null // appendableSuffix
                            ), 
                            args[0], 
                            args[1]
                        );
                        return null;
                    }
                    // Operations
                    else {
                        return this.refDelegate.refInvokeOperation(
                            featureName, 
                            args == null ? Collections.EMPTY_LIST : Arrays.asList(args)
                        );
                    }
                }
            } 
            //
            // cci delegation
            //
            else {
                if(declaringClass == PersistenceCapable.class) {
                    if("jdoGetPersistenceManager".equals(methodName)) {
                        return ((RefPackage_1_0)this.refClass.refOutermostPackage()).refPersistenceManager();
                    } 
                    else {
                        return method.invoke(
                            ((DelegatingRefObject_1_0)this.refDelegate).openmdxjdoGetDataObject(), 
                            args
                        );
                    }
                } 
                else if(
                    declaringClass == DelegatingRefObject_1_0.class ||
                    declaringClass == org.openmdx.base.persistence.spi.Cloneable.class
                ) {
                    return method.invoke(
                        this.refDelegate, 
                        args
                    );
                } 
                else if(
                    declaringClass == LoadCallback.class ||
                    declaringClass == StoreCallback.class ||
                    declaringClass == ClearCallback.class ||
                    declaringClass == DeleteCallback.class ||
                    declaringClass == ConstructCallback.class
                ) {
                    boolean done = false;
                    // Assert existence of impls
                    this.getImpl(
                    	this.refClass.refMofId(), 
                    	proxy, 
                       ((DelegatingRefObject_1_0)this.refDelegate).openmdxjdoGetDelegate()
                    );
                    for(Object impl : this.impls.values()) {
                        if(declaringClass.isInstance(impl)) {
                            method.invoke(impl, args);
                            done = true;
                        }
                    }
                    if(done) {
                        return null;
                    } 
                    else if (declaringClass.isInstance(this.refDelegate)) {
                        return method.invoke(this.refDelegate, args);
                    } 
                    else {
                    	return null;
                    } 
                } 
                else if(
                    declaringClass == RefBaseObject.class ||
                    declaringClass == RefFeatured.class ||
                    declaringClass == RefObject.class ||
                    declaringClass == RefObject_1_0.class
                ){
                    if("refGetValue".equals(methodName)) {
                        if(args.length == 1) {
                            return this.invokeCci(
                                proxy, 
                                this.featureMapper.getAccessor(args[0]), 
                                null, 
                                this.refClass.getMarshaller(), 
                                false // operation
                            );
                        }
                        if(args.length == 3) {
                            LargeObject largeObject = (LargeObject)this.invokeCci(
                                proxy, 
                                this.featureMapper.getAccessor(args[0]), 
                                null, 
                                this.refClass.getMarshaller(), 
                                false // operation
                            );
                            long position = args[2] == null ? 0l : (Long)args[2];
                            if(largeObject instanceof BinaryLargeObject) {
                                ((BinaryLargeObject)largeObject).getContent(
                                    (OutputStream)args[1],
                                    position
                                );
                            } 
                            else {
                                ((CharacterLargeObject)largeObject).getContent(
                                    (Writer)args[1],
                                    position
                                );
                            }
                            return largeObject.getLength();
                        }
                    } 
                    else if ("refSetValue".equals(methodName) && args.length == 2) {
                        return this.invokeCci(
                            proxy,
                            this.featureMapper.getMutator(args[0]), 
                            new Object[]{args[1]}, 
                            this.refClass.getMarshaller(), 
                            false // operation
                        );
                    } 
                    else if("refInvokeOperation".equals(methodName) && args.length == 2){
                        return this.invokeCci(
                            proxy,
                            this.featureMapper.getOperation(args[0]), 
                            ((List)args[1]).toArray(), 
                            this.refClass.getMarshaller(), 
                            true // operation
                        );
                    } 
                    else {
                        StandardMarshaller marshaller = this.refClass.getMarshaller(); 
                        return marshaller.marshal(
                            method.invoke(
                                this.refDelegate, 
                                marshaller.unmarshal(args)
                            )
                        );
                    }
                } 
                else {
                    RefRootPackage_1 refPackage = (RefRootPackage_1)this.refClass.refOutermostPackage();
                    if(refPackage.getMixedInInterfaces(refClass.refMofId()).contains(declaringClass)) {
                        return this.invokeCci(
                            proxy,
                            method, 
                            args, 
                            this.refClass.getMarshaller(), 
                            false // operation
                        );
                    } else {
                        ModelElement_1_0 feature = this.featureMapper.getFeature(
                            methodName,
                            method.getReturnType() == void.class ? 
                                FeatureMapper.MethodSignature.RETURN_IS_VOID : 
                                FeatureMapper.MethodSignature.DEFAULT
                        );
                        boolean operation = feature.objGetClass().equals(ModelAttributes.OPERATION); 
                        if(
                            !operation &&
                            (methodName.length() > 3) &&
                            (args != null && args.length > 0)
                        ) {
                            boolean makePersistent = methodName.startsWith("add"); 
                            if(
                                makePersistent ||
                                methodName.startsWith("get") 
                            ){
                                //
                                // RefContainer operations
                                //
                                RefContainer container = (RefContainer) this.invokeCci(
                                    proxy,
                                    this.featureMapper.getAccessor(feature.objGetValue("name")), 
                                    null, 
                                    this.refClass.getMarshaller(), 
                                    false // operation
                                );
                                if(makePersistent) {
                                    //
                                    // Make Persistent
                                    //
                                    container.refAdd(jmiToRef(args));
                                    return null;
                                } 
                                else if (args[0] instanceof AnyTypePredicate){
                                    //
                                    // Execute Query
                                    //
                                    return container.refGetAll(args[0]);
                                } 
                                else {
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
                                    Collection collection = (Collection) this.invokeCci(
                                        proxy,
                                        this.featureMapper.getCollection(methodName), 
                                        null, 
                                        this.refClass.getMarshaller(), 
                                        false // operation
                                    );
                                    collection.clear();
                                    collection.addAll(
                                        array ? Arrays.asList((Object[])args[0]) : (Collection)args[0]
                                    );
                                    return null;
                                }
                            }
                        }
                        return this.invokeCci(
                            proxy,
                            this.featureMapper.getMethod(method), 
                            args, 
                            this.refClass.getMarshaller(), 
                            operation
                        );
                    }
                }
            }
        } 
        catch(InvocationTargetException e) {
            throw e.getTargetException();
        }
        throw new UnsupportedOperationException(methodName);
    }

    //-----------------------------------------------------------------------
    private static Object[] jmiToRef(
        Object[] source
    ){
        if(source == null) {
            return null;
        } 
        else {
            int size = source.length;
            if(
                (size == 1) &&
                (source[0] instanceof String || source[0] instanceof Number)
            ){
                return new Object[]{RefContainer.REASSIGNABLE, source[0]};
            }  
            else if(
                size == 2 &&
                (source[0] instanceof String)
            ){
                return new Object[]{RefContainer.REASSIGNABLE, source[0], source[1]};
            } 
            else {
                Object[] target = new Object[size];
                for(
                    int i = 0, iLimit = size - 1;
                    i <= iLimit;
                    i++
                ){
                    target[i] = i % 2 == 1 || i == iLimit ? 
                        source[i] : 
                        ((Boolean)source[i]) ? RefContainer.PERSISTENT : RefContainer.REASSIGNABLE; 
                }
                return target;
            }
        }
    }

    //-----------------------------------------------------------------------
    protected Object invokeCci(
        Object proxy,
        Method method, 
        Object[] args, 
        StandardMarshaller marshaller, 
        boolean operation
    ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ServiceException, SecurityException, NoSuchMethodException{
        Object next = ((DelegatingRefObject_1_0)this.refDelegate).openmdxjdoGetDelegate(); 
        InvocationTarget invocationTarget = this.getImpl(
            proxy,
            next,
            method, 
            true // lenient
        );
        boolean hasVoidArg = 
            (method.getParameterTypes().length == 0) && 
            (args != null) && (args.length == 1) && 
            (args[0] instanceof org.openmdx.base.cci2.Void);
        if(invocationTarget == null) {
            if(
                operation && 
                next instanceof RefObject && 
                args != null && 
                args.length == 1 &&
                args[0] instanceof RefStruct_1_0
            ) {
                RefStruct_1_0 in = (RefStruct_1_0) args[0];
                RefStruct_1_0 out = null;
                if(hasVoidArg) {
                    out = (RefStruct_1_0)method.invoke(next);
                }
                else {
                    out = (RefStruct_1_0)method.invoke(
                        next,
                        in == null ? null : ((RefPackage_1_0) ((RefObject)next).refOutermostPackage()).refCreateStruct(in.refDelegate())
                    );                    
                }
                return out == null ? 
                    null : 
                    ((RefPackage_1_0) ((RefObject)proxy).refOutermostPackage()).refCreateStruct(out.refDelegate());
            } else {
                return marshaller.marshal(
                    method.invoke(
                        next, 
                        hasVoidArg ? null : marshaller.unmarshal(args)
                    )
                );
            } 
        }  else {
            Object reply = invocationTarget.invoke(
                hasVoidArg ? null : args
            );
            if(reply instanceof Container<?> && !(reply instanceof RefContainer)) {
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

    
    //-----------------------------------------------------------------------
    /**
     * MarshallingContainer
     */
    @SuppressWarnings("unchecked")
    static class MarshallingContainer extends MarshallingCollection
        implements RefContainer {

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

        private static final long serialVersionUID = 6428539771257840145L;
        protected final StandardMarshaller marshaller;
        protected final RefContainer delegate;

        /**
         * @return
         * @see javax.jmi.reflect.RefBaseObject#refImmediatePackage()
         */
        public RefPackage refImmediatePackage() {
            return this.delegate.refImmediatePackage();
        }

        /**
         * @return
         * @see javax.jmi.reflect.RefBaseObject#refMetaObject()
         */
        public RefObject refMetaObject() {
            return this.delegate.refMetaObject();
        }

        /**
         * @return
         * @see javax.jmi.reflect.RefBaseObject#refMofId()
         */
        public String refMofId() {
            return this.delegate.refMofId();
        }

        /**
         * @return
         * @see javax.jmi.reflect.RefBaseObject#refOutermostPackage()
         */
        public RefPackage refOutermostPackage() {
            return this.delegate.refOutermostPackage();
        }

        /**
         * @param deepVerify
         * @return
         * @see javax.jmi.reflect.RefBaseObject#refVerifyConstraints(boolean)
         */
        public Collection refVerifyConstraints(boolean deepVerify) {
            return this.delegate.refVerifyConstraints(deepVerify);
        }

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
        public long refRemoveAll(Object query) {
            return this.delegate.refRemoveAll(query);
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
            RefRootPackage_1 delegate
        ){
            this.delegate = delegate;
        }

        private static final long serialVersionUID = 3399640551686160240L;
        private final RefRootPackage_1 delegate;

        /**
         * Retrieve the marshaller's delegate
         * 
         * @return the outermost package
         */
        Jmi1Package_1_0 getOutermostPackage(){
            return this.delegate;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Marshaller#unmarshal(java.lang.Object)
         */
        public Object unmarshal(
            Object source
        ){
            try {
                return 
                    source instanceof RefStruct_1_0 ? ((RefStruct_1_0)source).refDelegate() :  
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
                return source instanceof Object[] ? marshal(
                    (Object[])source
                ) : source instanceof PersistenceCapable ? this.delegate.marshal(
                    source
                ) : source instanceof List ? marshal(
                    (List)source
                ) : source instanceof Set ? new MarshallingSet(
                    this, 
                    (Set)source
                ) : source instanceof SparseArray ? SortedMaps.asSparseArray(
                    new MarshallingSortedMap(this, (SparseArray)source)
                ) : source instanceof Iterator ? new MarshallingIterator(
                    (Iterator)source
                ) : source instanceof Container ? Classes.newProxyInstance(
                    new Jmi1ContainerInvocationHandler(this, (Container)source),
                    source.getClass().getInterfaces()[0], 
                    RefContainer.class, 
                    Serializable.class 
                ) : source instanceof Record ? delegate.refCreateStruct(
                    (Record)source
                ) : source;
            }  catch (ServiceException exception) {
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
            InputStream value = this.initialValue == null ? 
                (InputStream)Jmi1ObjectInvocationHandler.this.refDelegate.refGetValue(this.featureName) : 
                this.initialValue;
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
            this.length = position + BinaryLargeObjects.streamCopy(
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

        InvocationTarget(
            Object object, 
            Method method
        ) {
            this.object = object;
            this.method = method;
        }       

        private final Object object;
        private final Method method;

        Object invoke(
            Object... arguments
        ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return this.method.invoke(
                this.object, 
                arguments
            );
        }

    }

}
