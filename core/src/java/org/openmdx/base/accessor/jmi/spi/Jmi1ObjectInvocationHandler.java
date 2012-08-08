/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Jmi1ObjectInvocationHandler.java,v 1.143 2010/08/30 15:40:57 wfro Exp $
 * Description: JMI 1 Object Invocation Handler 
 * Revision:    $Revision: 1.143 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/30 15:40:57 $
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

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
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.jdo.listener.ConstructCallback;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.loading.Classes;
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
public class Jmi1ObjectInvocationHandler implements InvocationHandler, Serializable {

    /**
     * Constructor 
     * @param refClass
     * @param delegate
     * @throws ServiceException 
     *
     * @see RefClass_1#refCreateInstance
     */
    Jmi1ObjectInvocationHandler(
        Jmi1Class_1_0 refClass,
        PersistenceCapable delegate,
        ClassMapping_1_0 mapping
    ){
        this.refClass = refClass;
        this.refDelegate = refClass.isTerminal() ? new RefObject_1(
            (ObjectView_1_0)delegate, 
            refClass
        ) : new DelegatingRefObject_1(
            delegate, 
            refClass
        );
        this.mapping = mapping;
        this.aspectImplementationDescriptors = mapping.getAspectImplementationDescriptors();     
        this.aspectImplementationInstances = new Object[
            this.aspectImplementationDescriptors.length                                    
        ];
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 7709571315051480193L;
    
    protected final RefObject refDelegate;
    protected final Jmi1Class_1_0 refClass;
    private final AspectImplementationDescriptor[] aspectImplementationDescriptors;
    private final Object[] aspectImplementationInstances;
    private transient FeatureMapper featureMapper;
    private final ClassMapping_1_0 mapping;
    
    /**
     * Retrieve the mapping
     * 
     * @return the mapping
     */
    private final Mapping_1_0 getMapping(){
        return ((Jmi1Package_1_0)this.refClass.refOutermostPackage()).refMapping(); 
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
            this.featureMapper = getMapping().getFeatureMapper(
                refClass.refMofId(), 
                refClass.isTerminal() ? FeatureMapper.Type.TEMRINAL : FeatureMapper.Type.NON_TERMINAL
            ); 
        }
        return this.featureMapper;
    }

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
                    DelegatingRefObject_1.STANDARD, "refVerifyConstraints"
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
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refDefaultFetchGroup()
         */
        public Set<String> refDefaultFetchGroup(
        ) {
            if(this.cciDelegate instanceof RefObject_1_0) {
                return ((RefObject_1_0)this.cciDelegate).refDefaultFetchGroup();
            } 
            else {
                throw newUnsupportedOperationException(
                    DelegatingRefObject_1.STANDARD,
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
                DelegatingRefObject_1.STANDARD,
                "refDelegate"
            );        
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
                    DelegatingRefObject_1.STANDARD,
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
                    DelegatingRefObject_1.STANDARD,
                    "refInitialize"
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refSetValue(java.lang.String, java.lang.Object, long)
         */
        public void refSetValue(
            String feature, 
            Object newValue, 
            long length
        ) {
            throw newUnsupportedOperationException(DelegatingRefObject_1.REFLECTIVE, feature);
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
    /**
     * Retrive a (maybe newly created) aspect implementation instance
     *  
     * @param index
     * @param self
     * @param next
     * @returnthe requeste aspect implementation instance
     *  
     * @throws ServiceException
     */
    protected Object getAspectImplementationInstance(
        int index,
        Object self,
        Object next
    ) throws ServiceException{
        if(this.aspectImplementationInstances[index] == null) try {
            this.aspectImplementationInstances[index] = this.aspectImplementationDescriptors[index].implementationConstructor.newInstance(self, next);
        } catch (Exception exception) {
            throw new ServiceException(exception);
        }
        return this.aspectImplementationInstances[index];
    }
    
    //-----------------------------------------------------------------------
    protected InvocationTarget getImpl(
        Object self,
        Object next,
        Method method
    ) throws ServiceException {
        InvocationDescriptor descriptor = this.mapping.getInvocationDescriptor(method);
        return descriptor == null ? null : new InvocationTarget(
            this.getAspectImplementationInstance(descriptor.index, self, next),
            descriptor.method
        );
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
                    return System.identityHashCode(this);
                }
                if("toString".equals(methodName)) {
                    return this.refDelegate.toString();
                } 
                if("equals".equals(methodName)) {
                    if(proxy == args[0]) {
                        return true;
                    } 
                    boolean persistent = JDOHelper.isPersistent(proxy);
                    if(persistent != JDOHelper.isPersistent(args[0])) {
                        return false;
                    }
                    Object thisId;
                    Object thatId;
                    if (persistent) {
                        thisId = JDOHelper.getObjectId(proxy);
                        thatId = JDOHelper.getObjectId(args[0]);
                    } else {
                        thisId = JDOHelper.getTransactionalObjectId(proxy);
                        thatId = JDOHelper.getTransactionalObjectId(args[0]);
                    }
                    return thisId != null && thisId.equals(thatId);
                } 
            } 
            //
            // ref delegation
            //
            else {
                this.refClass.assertOpen();
                if(this.refClass.isTerminal()) {
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
                        ModelElement_1_0 feature = getFeatureMapper().getFeature(
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
                                    new Jmi1ContainerInvocationHandler((Marshaller)null, (RefContainer)value),
                                    method.getReturnType(), 
                                    RefContainer.class, 
                                    PersistenceCapableCollection.class,
                                    Serializable.class
                                ) : value;
                            }
                            // Query
                            else if(
                                args.length == 1 && (
                                    args[0] == null || args[0] instanceof AnyTypePredicate
                                ) 
                            ){
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
                            (args[0].getClass() == Boolean.class) && 
                            (
                                args[1] instanceof String ||
                                args[1] instanceof Number ||
                                args[1].getClass().isPrimitive() 
                            ) &&
                            (args[2] instanceof RefObject_1_0)                            
                        ) {
                            ((Jmi1Object_1_0)this.refDelegate).refAddValue(
                                Identifier.ATTRIBUTE_NAME.toIdentifier(
                                    featureName,
                                    "add", // removablePrefix
                                    null, // prependablePrefix
                                    null, // removableSuffix
                                    null // appendableSuffix
                                ), 
                                ((Boolean)args[0]).booleanValue() ? "!" + args[1] : args[1], // qualifier 
                                args[2] // value
                            );
                            return null;
                        }
                        // Adders with signature (PrimitiveType qualifier, Object object)
                        else if(
                            methodName.startsWith("add") &&
                            (args != null) && 
                            (args.length == 2) && 
                            (
                                args[0] instanceof String ||
                                args[0] instanceof Number ||
                                args[0].getClass().isPrimitive() 
                            ) &&
                            (args[1] instanceof RefObject_1_0)                            
                        ) {
                            ((Jmi1Object_1_0)this.refDelegate).refAddValue(
                                Identifier.ATTRIBUTE_NAME.toIdentifier(
                                    featureName,
                                    "add", // removablePrefix
                                    null, // prependablePrefix
                                    null, // removableSuffix
                                    null // appendableSuffix
                                ), 
                                args[0], // qualifier
                                args[1] // value
                            );
                            return null;
                        }
                        // Adders with signature (Object object)
                        else if(
                            methodName.startsWith("add") &&
                            (args != null) && 
                            (args.length == 1) && 
                            (args[0] instanceof RefObject_1_0)
                        ) {
                            ((Jmi1Object_1_0)this.refDelegate).refAddValue(
                                Identifier.ATTRIBUTE_NAME.toIdentifier(
                                    featureName,
                                    "add", // removablePrefix
                                    null, // prependablePrefix
                                    null, // removableSuffix
                                    null // appendableSuffix
                                ), 
                                null, // qualifier 
                                args[0] // value
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
                        for(
                            int i = 0; 
                            i < this.aspectImplementationDescriptors.length; 
                            i++
                        ){
                            if(declaringClass.isAssignableFrom(this.aspectImplementationDescriptors[i].implementationClass)) {
                                method.invoke(
                                    this.getAspectImplementationInstance(i, proxy, ((DelegatingRefObject_1_0)this.refDelegate).openmdxjdoGetDelegate())
                                );
                            }
                        }
                        return declaringClass.isInstance(this.refDelegate) ? method.invoke(this.refDelegate, args) : null;
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
                                    getFeatureMapper().getAccessor(args[0]), 
                                    null, 
                                    this.refClass.getMarshaller(), 
                                    false // operation
                                );
                            }
                            if(args.length == 3) {
                                LargeObject largeObject = (LargeObject)this.invokeCci(
                                    proxy, 
                                    getFeatureMapper().getAccessor(args[0]), 
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
                                getFeatureMapper().getMutator(args[0]), 
                                new Object[]{args[1]}, 
                                this.refClass.getMarshaller(), 
                                false // operation
                            );
                        } 
                        else if("refInvokeOperation".equals(methodName) && args.length == 2){
                            return this.invokeCci(
                                proxy,
                                getFeatureMapper().getOperation(args[0]), 
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
                        if(this.mapping.isMixedInInterfaces(declaringClass)) {
                            return this.invokeCci(
                                proxy,
                                method, 
                                args, 
                                this.refClass.getMarshaller(), 
                                false // operation
                            );
                        } else {
                            ModelElement_1_0 feature = getFeatureMapper().getFeature(
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
                                if(methodName.startsWith("add")) {
                                    RefContainer container = (RefContainer) this.invokeCci(
                                        proxy,
                                        getFeatureMapper().getAccessor(feature.objGetValue("name")), 
                                        null, 
                                        this.refClass.getMarshaller(), 
                                        false // operation
                                    );
                                    container.refAdd(jmiToRef(args));
                                    return null;
                                } else if (methodName.startsWith("get")) { 
                                    RefContainer container = (RefContainer) this.invokeCci(
                                        proxy,
                                        getFeatureMapper().getAccessor(feature.objGetValue("name")), 
                                        null, 
                                        this.refClass.getMarshaller(), 
                                        false // operation
                                    );
                                    return args[0] instanceof AnyTypePredicate || args[0] == null ? container.refGetAll(
                                        args[0]
                                    ) : container.refGet(
                                        jmiToRef(args)
                                    );
                                } else if(methodName.startsWith("set")) {
                                    boolean array = args[0] instanceof Object[];
                                    if(array || args[0] instanceof Collection) {
                                        //
                                        // Replace collection content
                                        //
                                        Collection collection = (Collection) this.invokeCci(
                                            proxy,
                                            getFeatureMapper().getCollection(methodName), 
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
                                getFeatureMapper().getMethod(method), 
                                args, 
                                this.refClass.getMarshaller(), 
                                operation
                            );
                        }
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
                (source[0] instanceof RefObject_1_0)
            ){
                return new Object[]{RefContainer.REASSIGNABLE, null, source[0]};
            }  
            else if(
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
            method
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
                Object[] arguments = hasVoidArg ? null : marshaller.unmarshal(args); 
                try {
                    return marshaller.marshal(
                        method.invoke(
                            next, 
                            arguments
                        )
                    );
                } catch (RuntimeException exception) {
                    boolean addCause = exception.getCause() == null;
                    if(addCause) try {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Object[] formalTypes = new String[parameterTypes.length];
                        for(int i = 0; i < formalTypes.length; i++) {
                            formalTypes[i] = parameterTypes[i].getName();
                        }
                        Object[] actualClass = new String[arguments == null ? 0 : arguments.length];
                        Object[] actualInterfaces = new String[actualClass.length];
                        Object[] match = new Boolean[actualClass.length];
                        for(int i = 0; i < actualClass.length; i++) {
                            if(arguments[i] == null) {
                                actualClass[i] = null;
                                actualInterfaces[i] = null;
                                match[i] = Boolean.valueOf(
                                    i < parameterTypes.length && 
                                    !parameterTypes[i].isPrimitive()
                                );
                            } else {
                                Class<?> argumentClass = arguments[i].getClass(); 
                                actualClass[i] = argumentClass.getName();
                                List<String> interfaces = new ArrayList<String>();
                                for(Class<?> actualInterface : argumentClass.getInterfaces()) {
                                    interfaces.add(actualInterface.getName());
                                }
                                actualInterfaces[i] = interfaces.toString();
                                match[i] = Boolean.valueOf(
                                    i < parameterTypes.length && 
                                    parameterTypes[i].isInstance(arguments[i])
                                );
                            }
                        }
                        Throwables.initCause(
                            exception,
                            null,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            new BasicException.Parameter("method", method.getDeclaringClass().getName() + "." + method.getName() + "()"),
                            new BasicException.Parameter("formal-argument-type", formalTypes),
                            new BasicException.Parameter("actual-argument-class", actualClass),
                            new BasicException.Parameter("actual-argument-interfaces", actualInterfaces),
                            new BasicException.Parameter("matching-argument-type", match)
                        );
                    } catch (Exception ignore) {
                        // leave the cause as it was...
                    }
                    throw exception;
                } catch (InvocationTargetException exception) {
                    Throwable cause = exception.getCause();
                    if(cause instanceof ClassCastException) try {
                        String message = cause.getMessage();
                        boolean addCause = cause.getCause() == null;
                        for(
                            int i = 0, iLimit = message.length();
                            addCause && i < iLimit;
                            i++
                        ){
                            char c = message.charAt(i);
                            addCause = i == 0 ? Character.isJavaIdentifierStart(c) : Character.isJavaIdentifierPart(c);
                        }
                        if(addCause){
                            Class<?> actual = Classes.getApplicationClass(message);
                            List<String> interfaces = new ArrayList<String>();
                            for(Class<?> implemented : actual.getInterfaces()) {
                                interfaces.add(implemented.getName());
                            }
                            Throwables.initCause(
                                cause,
                                null,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                new BasicException.Parameter("method", method.getDeclaringClass().getName() + "." + method.getName() + "()"),
                                new BasicException.Parameter("class", message),
                                new BasicException.Parameter("interfaces", interfaces),
                                new BasicException.Parameter("return-type", method.getReturnType().getName()),
                                new BasicException.Parameter("generic-return-type", method.getGenericReturnType())
                            );
                        }
                    } catch (Exception ignore) {
                        // leave the cause as it was...
                    }
                    throw exception;
                }
            } 
        }  else {
            Object reply = invocationTarget.invoke(
                hasVoidArg ? null : args
            );
            if(reply instanceof Container<?> && !(reply instanceof RefContainer<?>)) {
                return Classes.newProxyInstance(
                    new Jmi1ContainerInvocationHandler(
                        null, // marshaller
                        (Container<?>) reply
                    ),
                    method.getReturnType(), 
                    RefContainer.class, 
                    PersistenceCapableCollection.class,
                    Serializable.class
                );
            } else {
                return reply;
            }
        }
    }

    
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
                    PersistenceCapableCollection.class,
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
            return source instanceof AbstractSequentialList ? new MarshallingSequentialList(
                this,
                source
            ) : new MarshallingList(
                this, 
                source
            );
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
