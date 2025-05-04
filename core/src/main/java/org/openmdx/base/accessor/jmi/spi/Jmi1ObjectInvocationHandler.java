/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: JMI 1 Object Invocation Handler 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.listener.ClearCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.StoreCallback;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.oasisopen.jmi1.RefQualifier;
import org.omg.mof.spi.Identifier;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.accessor.jmi.spi.FeatureMapper.Kind;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.cci2.Aspect;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.Cloneable;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.jdo.listener.ConstructCallback;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.AnyTypePredicate;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
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
     * 
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

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 7709571315051480193L;
    
    protected final RefObject refDelegate;
    protected final Jmi1Class_1_0 refClass;
    private final AspectImplementationDescriptor[] aspectImplementationDescriptors;
    private final Object[] aspectImplementationInstances;
    private transient FeatureMapper featureMapper;
    private final ClassMapping_1_0 mapping;
    
    /**
     * Retrieve the validating marshaller
     * 
     * @return the validating marshaller
     */
    private ValidatingMarshaller getValidator(){
        return ((RefRootPackage_1)this.refClass.refOutermostPackage()).validatingMarshaller;
    }
    
    /**
     * Retrieve the mapping
     * 
     * @return the mapping
     */
    private Mapping_1_0 getMapping(){
        return ((Jmi1Package_1_0)this.refClass.refOutermostPackage()).refMapping(); 
    }
  
    /**
     * Retrieve the feature mapper
     * 
     * @return the feature mapper
     * 
     * @throws ServiceException 
     */
    protected FeatureMapper getFeatureMapper(
    ) throws ServiceException {
        if(this.featureMapper == null) {
            this.featureMapper = getMapping().getFeatureMapper(
                refClass.refMofId(), 
                refClass.isTerminal() ? FeatureMapper.Type.TEMRINAL : FeatureMapper.Type.NON_TERMINAL
            ); 
        }
        return this.featureMapper;
    }

    /**
     * Retrive a (maybe newly created) aspect implementation instance
     *  
     * @param index
     * @param self
     * @param next
     * @return the requested aspect implementation instance
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

    /**
     * Retrieve the proxy's invocation handler
     * 
     * @param proxy
     * 
     * @returne the proxy's invocation handler
     * @throws ServiceException
     */
    private static Jmi1ObjectInvocationHandler getInstance(
        Object proxy
    ) throws ServiceException {
        if(proxy != null) {
            if(Proxy.isProxyClass(proxy.getClass())) {
                InvocationHandler handler =  Proxy.getInvocationHandler(proxy);
                if(handler instanceof Jmi1ObjectInvocationHandler) {
                    return (Jmi1ObjectInvocationHandler) Proxy.getInvocationHandler(proxy);
                }
            }
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "The object is not a proxy handled by " + Jmi1ObjectInvocationHandler.class.getName(),
            new BasicException.Parameter("class", proxy == null ? null : proxy.getClass().getName())
        );
    }
    
    /**
     * Retrieve the aspect implementation instance
     * 
     * @param proxy the proxy object
     * @param aspectImplementationClass the requested class
     * 
     * @return the aspect implementation instance, or {@code null} if the aspectImplementationClass is not applicable to {@code proxy}
     */
    public static <T> T getAspectImplementationInstance(
        Object proxy,
        Class<T> aspectImplementationClass
    ) throws ServiceException {
        Jmi1ObjectInvocationHandler handler = getInstance(proxy);
        for(int i = 0; i < handler.aspectImplementationDescriptors.length; i++) {
            if(aspectImplementationClass.isAssignableFrom(handler.aspectImplementationDescriptors[i].implementationClass)) {
                Object next = ((DelegatingRefObject_1_0)handler.refDelegate).openmdxjdoGetDelegate(); 
                return aspectImplementationClass.cast(handler.getAspectImplementationInstance(i, proxy, next));
            }
        }
        return null;
    }
    
    /**
     * Retrieve the invocation target
     * 
     * @param self
     * @param next
     * @param method
     * 
     * @return the implementation for the given feature
     * 
     * @throws ServiceException
     */
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

    /**
     * Replace the content of the target map
     */
    @SuppressWarnings({
        "rawtypes", "unchecked"
    })
    private Void setMultivalue(
        Map target,
        Object source
    ){
        target.clear();
        if(source == null) {
            SysLog.warning("Use 'refGetValue(\"aFeature\").clear()' instead of 'refSetValue(\"aFeature\", null)'");
        } else {
            if(source instanceof Map) {
                target.putAll((Map) source);
            } else if (target instanceof SparseArray) {
                int i = 0;
                if(source instanceof Collection) {
                    for(Object value : (Collection)source){
                        target.put(Integer.valueOf(i++), value);
                    }
                } else if (source.getClass().isArray()) {
                    for(Object value : ArraysExtension.asList(source)){
                        target.put(Integer.valueOf(i++), value);
                    }
                } else {
                    throw new JmiServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "The given value is incompatible to the target",
                        new BasicException.Parameter("target", SparseArray.class.getName()),
                        new BasicException.Parameter("source", source.getClass().getName())
                    );
                }
            } else {
                throw new JmiServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "The given value is incompatible to the target",
                    new BasicException.Parameter("target", target.getClass().getName()),
                    new BasicException.Parameter("source", source.getClass().getName())
                );
            }
        }
        return null;
    }

    /**
     * Replace the content of the target map
     * 
     * @param target
     * @param source
     * 
     * @exception JmiServiceException
     */
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    private Void setMultivalue (
        Collection target,
        Object source
    ){
        target.clear();
        if(source == null) {
            SysLog.warning("Use 'refGetValue(\"aFeature\").clear()' instead of 'refSetValue(\"aFeature\", null)'");
        } else {
            if(source instanceof Collection) {
                target.addAll((Collection) source);
            } else if (source.getClass().isArray()) {
                target.addAll(ArraysExtension.asList(source));
            } else {
                throw new JmiServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "The given value is incompatible to the target",
                    new BasicException.Parameter("target", target.getClass().getName()),
                    new BasicException.Parameter("source", source.getClass().getName())
                );
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    #if CLASSIC_CHRONO_TYPES
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
                    return Integer.valueOf(System.identityHashCode(this));
                }
                if("toString".equals(methodName)) {
                    return this.refDelegate.toString();
                }
                if("equals".equals(methodName)) {
                    if(proxy == args[0]) {
                        return Boolean.TRUE;
                    }
                    boolean persistent = ReducedJDOHelper.isPersistent(proxy);
                    if(persistent != ReducedJDOHelper.isPersistent(args[0])) {
                        return Boolean.FALSE;
                    }
                    final Object thisId;
                    final Object thatId;
                    if (persistent) {
                        thisId = ReducedJDOHelper.getObjectId(proxy);
                        thatId = ReducedJDOHelper.getObjectId(args[0]);
                    } else {
                        thisId = ReducedJDOHelper.getTransactionalObjectId(proxy);
                        thatId = ReducedJDOHelper.getTransactionalObjectId(args[0]);
                    }
                    return Boolean.valueOf(thisId != null && thisId.equals(thatId));
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
                        declaringClass == RefFeatured.class &&
                        "refGetValue".equals(methodName)
                    ){
                        return marshal(
                            method.invoke(
                                this.refDelegate,
                                args
                            ),
                            getFeatureName(args[0]),
                            null
                        );
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
                        boolean operation = feature.isOperationType();
                        String featureName = feature.getName();
                        // Getters
                        if(!operation && methodName.startsWith("get")) {
                            if((args == null) || (args.length == 0)) {
                                return marshal(
                                    this.refDelegate.refGetValue(featureName),
                                    featureName,
                                    method.getReturnType()
                                );
                            } else if(args.length == 1 && AnyTypePredicate.class.isAssignableFrom(method.getParameterTypes()[0])) {
                            	//
                                // Query
                            	//
                                return ((RefContainer<?>)
                                    this.refDelegate.refGetValue(featureName)
                                ).refGetAll(
                                    args[0]
                                );
                            }
                            else {
                            	//
                                // Qualifier
                            	//
                                if(args.length == 1 && args[0] instanceof RefObject){
                                    Container<?> collection = (Container<?>) ((Jmi1Object_1_0)this.refDelegate).refGetValue(
                                        featureName,
                                        args[0]
                                    );
                                    return collection.getAll(null);
                                } else {
                                    Object qualifier;
                                    if(args.length == 2 && args[0] instanceof Boolean) {
                                        qualifier = ((Boolean)args[0]).booleanValue() ? "!" + args[1] : validateSubSegment(args[1]);
                                    } else {
                                        qualifier = validateSubSegment(args[0]);
                                    }
                                    return ((Jmi1Object_1_0)this.refDelegate).refGetValue(
                                        featureName,
                                        qualifier
                                    );
                                }
                            }
                        }
                        // Boolean getters
                        else if(!operation && methodName.startsWith("is")) {
                            if((args == null) || (args.length == 0)) {
                                return this.refDelegate.refGetValue(
                                    featureName
                                );
                            }
                        }
                        // Setters
                        else if(!operation && methodName.startsWith("set")) {
                            if((args != null) && (args.length == 1)) {
                                switch(ModelHelper.getMultiplicity(feature)){
                                    case OPTIONAL: case SINGLE_VALUE: case STREAM:
                                        this.refDelegate.refSetValue(featureName, args[0]);
                                        return null;
                                    case LIST: case SET:
                                        return setMultivalue((Collection) this.refDelegate.refGetValue(featureName), args[0]);
                                    case MAP: case SPARSEARRAY:
                                        return setMultivalue((Map)this.refDelegate.refGetValue(featureName), args[0]);
                                }
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
                            switch(args.length) {
                                case 1:
                                    return this.invokeCci(
                                        proxy,
                                        getFeatureMapper().getAccessor(args[0]),
                                        null,
                                        this.refClass.getMarshaller(),
                                        FeatureMapper.Kind.METHOD
                                    );
                                case 3:
                                    LargeObject largeObject = (LargeObject)this.invokeCci(
                                        proxy,
                                        getFeatureMapper().getAccessor(args[0]),
                                        null,
                                        this.refClass.getMarshaller(),
                                        FeatureMapper.Kind.METHOD
                                    );
                                    long position = args[2] == null ? 0l : ((Long)args[2]).longValue();
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
                            switch(getFeatureMapper().getMultiplicity(args[0])){
                                case OPTIONAL: case SINGLE_VALUE: case STREAM:
                                    return this.invokeCci(
                                        proxy,
                                        getFeatureMapper().getMutator(args[0]),
                                        new Object[]{args[1]},
                                        this.refClass.getMarshaller(),
                                        FeatureMapper.Kind.METHOD
                                    );
                                case LIST: case SET:
                                    return setMultivalue(
                                        (Collection) this.invokeCci(
                                            proxy,
                                            getFeatureMapper().getAccessor(args[0]),
                                            null,
                                            this.refClass.getMarshaller(),
                                            FeatureMapper.Kind.METHOD
                                        ),
                                        args[1]
                                    );
                                case MAP: case SPARSEARRAY:
                                    return setMultivalue(
                                        (Map) this.invokeCci(
                                            proxy,
                                            getFeatureMapper().getAccessor(args[0]),
                                            null,
                                            this.refClass.getMarshaller(),
                                            FeatureMapper.Kind.METHOD
                                        ),
                                        args[1]
                                    );
                            }
                        }
                        else if("refInvokeOperation".equals(methodName) && args.length == 2){
                            Object feature = args[0];
                            return this.invokeCci(
                                proxy,
                                getFeatureMapper().getOperation(feature),
                                ((List<?>)args[1]).toArray(),
                                this.refClass.getMarshaller(),
                                getFeatureMapper().getKind(feature)
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
                                FeatureMapper.Kind.METHOD
                            );
                        } else {
                            ModelElement_1_0 feature = getFeatureMapper().getFeature(
                                methodName,
                                method.getReturnType() == void.class ?
                                    FeatureMapper.MethodSignature.RETURN_IS_VOID :
                                    FeatureMapper.MethodSignature.DEFAULT
                            );
                            boolean operation = feature.isOperationType();
                            if(
                                !operation &&
                                methodName.length() > 3 &&
                                args != null &&
                                args.length > 0
                            ) {
                                if(methodName.startsWith("add")) {
                                    RefContainer<?> container = (RefContainer<?>) this.invokeCci(
                                        proxy,
                                        getFeatureMapper().getAccessor(feature.getName()),
                                        null,
                                        this.refClass.getMarshaller(),
                                        FeatureMapper.Kind.METHOD
                                    );
                                    container.refAdd(jmiToRef(args));
                                    return null;
                                } else if (methodName.startsWith("get")) {
                                    if(args.length != 1 || !(args[0] instanceof RefObject)) {
                                        RefContainer<?> container = (RefContainer<?>) this.invokeCci(
                                            proxy,
                                            getFeatureMapper().getAccessor(feature.getName()),
                                            null,
                                            this.refClass.getMarshaller(),
                                            FeatureMapper.Kind.METHOD
                                        );
                                        if(AnyTypePredicate.class.isAssignableFrom(method.getParameterTypes()[0])){
                                        	return container.refGetAll(args[0]);
                                        } else {
                                        	return container.refGet(jmiToRef(args));
                                        }
                                    }
                                } else if(methodName.startsWith("set")) {
                                    boolean array = args[0] instanceof Object[];
                                    if(array || args[0] instanceof Collection<?>) {
                                        //
                                        // Replace collection content
                                        //
                                        Collection collection = (Collection) this.invokeCci(
                                            proxy,
                                            getFeatureMapper().getCollection(methodName),
                                            null,
                                            this.refClass.getMarshaller(),
                                            FeatureMapper.Kind.METHOD
                                        );
                                        collection.clear();
                                        collection.addAll(
                                            array ? Arrays.asList((Object[])args[0]) : (Collection)args[0]
                                        );
                                        return null;
                                    }
                                }
                            }
                            Kind kind = operation ? (
                                Boolean.TRUE.equals(feature.objGetValue("isQuery")) ? Kind.QUERY_OPERATION : Kind.NON_QUERY_OPERATION
                            ) : Kind.METHOD;
                            return this.invokeCci(
                                proxy,
                                getFeatureMapper().getMethod(method),
                                args,
                                this.refClass.getMarshaller(),
                                kind
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
    #else
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
                    return Integer.valueOf(System.identityHashCode(this));
                }
                if("toString".equals(methodName)) {
                    return this.refDelegate.toString();
                }
                if("equals".equals(methodName)) {
                    if(proxy == args[0]) {
                        return Boolean.TRUE;
                    }
                    boolean persistent = ReducedJDOHelper.isPersistent(proxy);
                    if(persistent != ReducedJDOHelper.isPersistent(args[0])) {
                        return Boolean.FALSE;
                    }
                    final Object thisId;
                    final Object thatId;
                    if (persistent) {
                        thisId = ReducedJDOHelper.getObjectId(proxy);
                        thatId = ReducedJDOHelper.getObjectId(args[0]);
                    } else {
                        thisId = ReducedJDOHelper.getTransactionalObjectId(proxy);
                        thatId = ReducedJDOHelper.getTransactionalObjectId(args[0]);
                    }
                    return Boolean.valueOf(thisId != null && thisId.equals(thatId));
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
                                    declaringClass == Cloneable.class
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
                            declaringClass == RefFeatured.class &&
                                    "refGetValue".equals(methodName)
                    ){
                        return marshal(
                                method.invoke(
                                        this.refDelegate,
                                        args
                                ),
                                getFeatureName(args[0]),
                                null
                        );
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
                        boolean operation = feature.isOperationType();
                        String featureName = feature.getName();
                        // Getters
                        if(!operation && methodName.startsWith("get")) {
                            if((args == null) || (args.length == 0)) {
                                return marshal(
                                        this.refDelegate.refGetValue(featureName),
                                        featureName,
                                        method.getReturnType()
                                );
                            } else if(args.length == 1 && AnyTypePredicate.class.isAssignableFrom(method.getParameterTypes()[0])) {
                                //
                                // Query
                                //
                                return ((RefContainer<?>)
                                        this.refDelegate.refGetValue(featureName)
                                ).refGetAll(
                                        args[0]
                                );
                            }
                            else {
                                //
                                // Qualifier
                                //
                                if(args.length == 1 && args[0] instanceof RefObject){
                                    Container<?> collection = (Container<?>) ((Jmi1Object_1_0)this.refDelegate).refGetValue(
                                            featureName,
                                            args[0]
                                    );
                                    return collection.getAll(null);
                                } else {
                                    Object qualifier;
                                    if(args.length == 2 && args[0] instanceof Boolean) {
                                        qualifier = ((Boolean)args[0]).booleanValue() ? "!" + args[1] : validateSubSegment(args[1]);
                                    } else {
                                        qualifier = validateSubSegment(args[0]);
                                    }
                                    return ((Jmi1Object_1_0)this.refDelegate).refGetValue(
                                            featureName,
                                            qualifier
                                    );
                                }
                            }
                        }
                        // Boolean getters
                        else if(!operation && methodName.startsWith("is")) {
                            if((args == null) || (args.length == 0)) {
                                return this.refDelegate.refGetValue(
                                        featureName
                                );
                            }
                        }
                        // Setters
                        else if(!operation && methodName.startsWith("set")) {
                            if((args != null) && (args.length == 1)) {
                                switch(ModelHelper.getMultiplicity(feature)){
                                    case OPTIONAL: case SINGLE_VALUE: case STREAM:
                                        this.refDelegate.refSetValue(featureName, args[0]);
                                        return null;
                                    case LIST: case SET:
                                        return setMultivalue((Collection) this.refDelegate.refGetValue(featureName), args[0]);
                                    case MAP: case SPARSEARRAY:
                                        return setMultivalue((Map)this.refDelegate.refGetValue(featureName), args[0]);
                                }
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
                                    declaringClass == Cloneable.class
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
                            switch(args.length) {
                                case 1:
                                    return this.invokeCci(
                                            proxy,
                                            getFeatureMapper().getAccessor(args[0]),
                                            null,
                                            this.refClass.getMarshaller(),
                                            Kind.METHOD
                                    );
                                case 3:
                                    LargeObject largeObject = (LargeObject)this.invokeCci(
                                            proxy,
                                            getFeatureMapper().getAccessor(args[0]),
                                            null,
                                            this.refClass.getMarshaller(),
                                            Kind.METHOD
                                    );
                                    long position = args[2] == null ? 0l : ((Long)args[2]).longValue();
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
                            switch(getFeatureMapper().getMultiplicity(args[0])){
                                case OPTIONAL: case SINGLE_VALUE: case STREAM:
                                    return this.invokeCci(
                                            proxy,
                                            getFeatureMapper().getMutator(args[0]),
                                            new Object[]{args[1]},
                                            this.refClass.getMarshaller(),
                                            Kind.METHOD
                                    );
                                case LIST: case SET:
                                    return setMultivalue(
                                            (Collection) this.invokeCci(
                                                    proxy,
                                                    getFeatureMapper().getAccessor(args[0]),
                                                    null,
                                                    this.refClass.getMarshaller(),
                                                    Kind.METHOD
                                            ),
                                            args[1]
                                    );
                                case MAP: case SPARSEARRAY:
                                    return setMultivalue(
                                            (Map) this.invokeCci(
                                                    proxy,
                                                    getFeatureMapper().getAccessor(args[0]),
                                                    null,
                                                    this.refClass.getMarshaller(),
                                                    Kind.METHOD
                                            ),
                                            args[1]
                                    );
                            }
                        }
                        else if("refInvokeOperation".equals(methodName) && args.length == 2){
                            Object feature = args[0];
                            return this.invokeCci(
                                    proxy,
                                    getFeatureMapper().getOperation(feature),
                                    ((List<?>)args[1]).toArray(),
                                    this.refClass.getMarshaller(),
                                    getFeatureMapper().getKind(feature)
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
                                    Kind.METHOD
                            );
                        } else {
                            ModelElement_1_0 feature = getFeatureMapper().getFeature(
                                    methodName,
                                    method.getReturnType() == void.class ?
                                            FeatureMapper.MethodSignature.RETURN_IS_VOID :
                                            FeatureMapper.MethodSignature.DEFAULT
                            );
                            boolean operation = feature.isOperationType();
                            if(
                                    !operation &&
                                            methodName.length() > 3 &&
                                            args != null &&
                                            args.length > 0
                            ) {
                                if(methodName.startsWith("add")) {
                                    final RefArguments refArgs = RefArguments.newInstance(args);
                                    RefContainer<?> container = (RefContainer<?>) this.invokeCci(
                                            proxy,
                                            getFeatureMapper().getAccessor(feature.getName()),
                                            null,
                                            this.refClass.getMarshaller(),
                                            Kind.METHOD
                                    );
                                    ((RefContainer)container).refAdd(refArgs.qualifiers, refArgs.value);
                                    return null;
                                } else if (methodName.startsWith("get")) {
                                    if(args.length != 1 || !(args[0] instanceof RefObject)) {
                                        RefContainer<?> container = (RefContainer<?>) this.invokeCci(
                                                proxy,
                                                getFeatureMapper().getAccessor(feature.getName()),
                                                null,
                                                this.refClass.getMarshaller(),
                                                Kind.METHOD
                                        );
                                        if(AnyTypePredicate.class.isAssignableFrom(method.getParameterTypes()[0])){
                                            return container.refGetAll(args[0]);
                                        } else {
                                            final RefArguments refArgs = RefArguments.newInstance(args);
                                            ((RefContainer)container).refGet(refArgs.qualifiers);
                                        }
                                    }
                                } else if(methodName.startsWith("set")) {
                                    boolean array = args[0] instanceof Object[];
                                    if(array || args[0] instanceof Collection<?>) {
                                        //
                                        // Replace collection content
                                        //
                                        Collection collection = (Collection) this.invokeCci(
                                                proxy,
                                                getFeatureMapper().getCollection(methodName),
                                                null,
                                                this.refClass.getMarshaller(),
                                                Kind.METHOD
                                        );
                                        collection.clear();
                                        collection.addAll(
                                                array ? Arrays.asList((Object[])args[0]) : (Collection)args[0]
                                        );
                                        return null;
                                    }
                                }
                            }
                            Kind kind = operation ? (
                                    Boolean.TRUE.equals(feature.objGetValue("isQuery")) ? Kind.QUERY_OPERATION : Kind.NON_QUERY_OPERATION
                            ) : Kind.METHOD;
                            return this.invokeCci(
                                    proxy,
                                    getFeatureMapper().getMethod(method),
                                    args,
                                    this.refClass.getMarshaller(),
                                    kind
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
    #endif

    /**
     * Analyse refGetValue's first argument
     *
     * @param feature
     *
     * @return the feature's name
     */
    private static String getFeatureName(
        Object feature
    ){
    	String featureName;
        if(feature instanceof String) {
        	featureName = (String) feature;
        } else if (feature instanceof RefObject) {
        	featureName = ((RefObject)feature).refMofId();
        } else throw new IllegalArgumentException(
            "refGetValue expects a String or a RefObject as first argument: " + (feature == null ? "null" : feature.getClass().getName())
        );
        return featureName.substring(featureName.lastIndexOf(':') + 1);
    }

    /**
     * Proxify the value if necessary
     *
     * @param value
     * @param featureName
     * @param returnType
     *
     * @return the proxified value
     *
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    private Object marshal(
        Object value,
        String featureName,
        Class<?> returnType
    ) throws ServiceException {
        if(value instanceof InputStream){
            return new Jmi1BinaryLargeObject(
                featureName,
                (InputStream)value
            );
        }
        if(value instanceof SortedMap) {
            return SortedMaps.asSparseArray(
                (SortedMap<Integer,?>)value
            );
        }
        if(value instanceof RefContainer){
            return Classes.newProxyInstance(
                new Jmi1ContainerInvocationHandlerWithRefDelegate(this.getValidator(), (RefContainer<?>)value),
                returnType == null ? getReturnType(featureName) : returnType,
                RefContainer.class,
                PersistenceCapableCollection.class,
                Serializable.class
            );
        }
        if(value instanceof Reader){
            return new Jmi1CharacterLargeObject(
                featureName,
                (Reader)value
            );
        }
        getValidator().validate(value);
        return value;
    }

	/**
	 * Determine the feature's return type
	 */
	private Class<?> getReturnType(String featureName) throws ServiceException {
		try {
		    return this.refClass.getDelegateClass().getMethod(
		        Identifier.OPERATION_NAME.toIdentifier(
		            featureName,
		            null, // removablePrefix
		            "get", // prependablePrefix
		            null, // removableSuffix
		            null // appendableSuffix
		        )
		    ).getReturnType();
		} catch (NoSuchMethodException exception) {
		    throw new ServiceException(
		        exception,
		        BasicException.Code.DEFAULT_DOMAIN,
		        BasicException.Code.TRANSFORMATION_FAILURE,
		        "Unable to determine the container interface",
		        new BasicException.Parameter("parent-class", this.refClass.getDelegateClass().getName()),
		        new BasicException.Parameter("feature", featureName)
		    );
		}
	}

    /**
     * Validate an XRI sub-segment
     *
     * @param subSegment
     *
     * @return the validated XRI sub-segment
     *
     * @exception JmiException BAD_PARAMETER if the sub-segment is {@code null}Y
     */
    private static Object validateSubSegment(
    	Object subSegment
    ){
    	if(subSegment == null) {
	    	throw new JmiServiceException(
	    		BasicException.Code.DEFAULT_DOMAIN,
	    		BasicException.Code.BAD_PARAMETER,
	    		"Null is an invalid value for an XRI sub-segment"
	    	);
    	} else {
	    	return subSegment;
    	}
    }

    #if CLASSIC_CHRONO_TYPES
    /**
     * Convert JMI's non-reflective arguments to RefContainer arguments
     *
     * @param source non-reflective JMI arguments
     *
     * @return the corresponding RefContainer arguments
     */
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
                		validateSubSegment(source[i]) :
                        ((Boolean)source[i]).booleanValue() ? RefContainer.PERSISTENT : RefContainer.REASSIGNABLE;
                }
                return target;
            }
        }
    }
    #endif

    /**
     * Apply the given method to the next layer's CCI API
     *
     * @param proxy
     * @param method
     * @param args
     * @param marshaller
     * @param kind tells which kind of method or operation invocation has to be executed
     *
     * @return the methods return value
     *
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ServiceException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    protected Object invokeCci(
            Object proxy,
            Method method,
            Object[] args,
            StandardMarshaller marshaller,
            Kind kind
    ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ServiceException, SecurityException, NoSuchMethodException{
        DelegatingRefObject_1_0 delegate = (DelegatingRefObject_1_0)this.refDelegate;
        Object next = delegate.openmdxjdoGetDelegate();
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
                    kind != Kind.METHOD &&
                            next instanceof RefObject &&
                            args != null &&
                            args.length == 1 &&
                            args[0] instanceof RefStruct_1_0
            ) {
                RefStruct_1_0 in = (RefStruct_1_0) args[0];
                RefStruct_1_0 out;
                if(hasVoidArg) {
                    out =  (RefStruct_1_0) method.invoke(next);
                } else {
                    if(in != null) {
                        RefPackage_1_0 refPackage = (RefPackage_1_0) ((RefObject)next).refOutermostPackage();
                        in = (RefStruct_1_0) refPackage.refCreateStruct(in.refDelegate());
                    }
                    out =  (RefStruct_1_0) method.invoke(next, in);
                }
                if(out == null) {
                    return null;
                } else {
                    return ((RefPackage_1_0) ((RefObject)proxy).refOutermostPackage()).refCreateStruct(out.refDelegate());
                }
            } else {
                final Object[] arguments;
                if(hasVoidArg){
                    arguments = null;
                } else if(
                        args != null &&
                                args.length == 1 &&
                                "setCore".equals(method.getName()) && (
                                method.getDeclaringClass() == Aspect.class ||
                                        method.getDeclaringClass() == org.openmdx.base.jmi1.Aspect.class
                        )
                ){
                    arguments = new Object[]{
                            marshaller.getOutermostPackage().unmarshalUnchecked(args[0])
                    };
                } else {
                    arguments = marshaller.unmarshal(args);
                }
                try {
                    return marshaller.marshal(
                            method.invoke(
                                    next,
                                    arguments
                            )
                    );
                } catch (InvocationTargetException exception) {
                    Throwable cause = exception.getCause();
                    if(cause.getCause() == null) try{
                        //
                        // initCause() might succeed
                        //
                        Class<?> insufficientClass = this.getActualClass(cause);
                        List<String> insufficientInterfaces = new ArrayList<>();
                        for(Class<?> implemented : insufficientClass.getInterfaces()) {
                            insufficientInterfaces.add(implemented.getName());
                        }
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Object[] formalArgumentTypes = new String[parameterTypes.length];
                        for(int i = 0; i < formalArgumentTypes.length; i++) {
                            formalArgumentTypes[i] = parameterTypes[i].getName();
                        }
                        Object[] actualArgumentClass = new String[arguments == null ? 0 : arguments.length];
                        Object[] actualArgumentInterfaces = new String[actualArgumentClass.length];
                        Object[] matchingArgumentTypes = new Boolean[actualArgumentClass.length];
                        for(int i = 0; i < actualArgumentClass.length; i++) {
                            if(arguments[i] == null) {
                                actualArgumentClass[i] = null;
                                actualArgumentInterfaces[i] = null;
                                matchingArgumentTypes[i] = Boolean.valueOf(
                                        i < parameterTypes.length &&
                                                !parameterTypes[i].isPrimitive()
                                );
                            } else {
                                Class<?> argumentClass = arguments[i].getClass();
                                actualArgumentClass[i] = argumentClass.getName();
                                List<String> interfaces = new ArrayList<>();
                                for(Class<?> actualInterface : argumentClass.getInterfaces()) {
                                    interfaces.add(actualInterface.getName());
                                }
                                actualArgumentInterfaces[i] = interfaces.toString();
                                matchingArgumentTypes[i] = Boolean.valueOf(
                                        i < parameterTypes.length &&
                                                parameterTypes[i].isInstance(arguments[i])
                                );
                            }
                        }
                        Throwables.initCause(
                                cause,
                                null,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                new BasicException.Parameter("method", method.getDeclaringClass().getName() + "." + method.getName()),
                                new BasicException.Parameter("kind", kind),
                                new BasicException.Parameter("insufficient-class", insufficientClass.getName()),
                                new BasicException.Parameter("insufficient-interfaces", insufficientInterfaces),
                                new BasicException.Parameter("formal-argument-types", formalArgumentTypes),
                                new BasicException.Parameter("actual-argument-classes", actualArgumentClass),
                                new BasicException.Parameter("actual-argument-interfaces", actualArgumentInterfaces),
                                new BasicException.Parameter("matching-argument-types", matchingArgumentTypes),
                                new BasicException.Parameter("return-type", method.getReturnType().getName()),
                                new BasicException.Parameter("generic-return-type", method.getGenericReturnType())
                        );
                    } catch(Exception ignore) {
                        //
                        // initCause() didn't succeed, the cause might have been initialized to null explicitly
                        //
                    }
                    throw exception;
                }
            }
        }  else {
            final Object reply = invocationTarget.invoke(hasVoidArg ? null : args);
            if(kind == Kind.NON_QUERY_OPERATION) {
                final Object refObject = delegate.openmdxjdoGetDataObject();
                if(refObject instanceof RefObject_1_0) {
                    ((RefObject_1_0)refObject).refDelegate().objGetDelegate().touch();
                }
            }
            return reply instanceof Container<?> && !(reply instanceof RefContainer<?>) ? Classes.newProxyInstance(
                    new Jmi1ContainerInvocationHandlerWithCciDelegate(
                            this.getValidator(), // marshaller
                            (Container<?>) reply
                    ),
                    method.getReturnType(),
                    RefContainer.class,
                    PersistenceCapableCollection.class,
                    Serializable.class
            ) : reply;
        }
    }

    /**
     * Determine the actual class by parsing the ClassCastException message
     *
     * @param cause
     *
     * @return the class found at the beginning of the ClassCastException message
     */
    private Class<?> getActualClass(Throwable cause) {
        if(cause instanceof ClassCastException) {
            String message = cause.getMessage();
            if(message == null) {
                //
                // Just pass on the unspecific ClassCastException
                //
                return null;
            } else{
               int l = message.length();
               if(l > 0 && Character.isJavaIdentifierStart(message.charAt(0))) {
                   //
                   // Message might start with a Java class name
                   //
                   int i = 1;
                   while(i < l && Character.isJavaIdentifierPart(message.charAt(i))) {
                       i++;
                   }
                   try{
                       return Classes.getApplicationClass(message.substring(0, i));
                   } catch(ClassNotFoundException e) {
                       //
                       // It was not a Java class name
                       //
                       return null;
                   }
              } else {
                  //
                  // Message does not start with a Java class name
                  //
                  return null;
              }
           }
        } else{
            //
            // Just pass on other causes
            //
            return null;
        }
    }


    //------------------------------------------------------------------------
    // Class Jmi1BinaryLargeObject
    //------------------------------------------------------------------------

    /**
     * jmi1 BLOB
     */
    private class Jmi1BinaryLargeObject implements BinaryLargeObject {

        /**
         * Constructor
         *
         * @param featureName
         * @param value
         */
        Jmi1BinaryLargeObject(
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
            this.length = Long.valueOf(
                position + BinaryLargeObjects.streamCopy(
                    getContent(),
                    position,
                    stream
                )
            );
        }

    }


    //------------------------------------------------------------------------
    // Class Jmi1CharacterLargeObject
    //------------------------------------------------------------------------

    /**
     * jmi1 BLOB
     */
    private class Jmi1CharacterLargeObject implements CharacterLargeObject {

        /**
         * Constructor
         *
         * @param featureName
         * @param value
         */
        Jmi1CharacterLargeObject(
            String featureName,
            Reader value
        ) {
            this.featureName = featureName;
            this.initialValue = value;
        }

        protected transient Reader initialValue = null;
        protected final String featureName;
        protected transient Long length = null;

        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent()
         */
        public Reader getContent(
        ) throws IOException {
            Reader value = this.initialValue == null ?
                (Reader)Jmi1ObjectInvocationHandler.this.refDelegate.refGetValue(this.featureName) :
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

        /* (non-Javadoc)
         * @see org.w3c.cci2.CharacterLargeObject#getContent(java.io.Writer, long)
         */
        public void getContent(
            Writer stream,
            long position
        ) throws IOException {
            this.length = Long.valueOf(
                position + CharacterLargeObjects.streamCopy(
                    getContent(),
                    position,
                    stream
                )
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


    //------------------------------------------------------------------------
    // Class DelegatingRefObject_1
    //------------------------------------------------------------------------

    /**
     * The non-terminal RefObject implementation
     */
    private static class DelegatingRefObject_1
        implements DelegatingRefObject_1_0, Cloneable<RefObject>, Serializable {

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
         * Implements {@code Serializable}
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
            return (Path) ReducedJDOHelper.getObjectId(this.cciDelegate);
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
            ReducedJDOHelper.getPersistenceManager(
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
            } catch (RuntimeServiceException e) {
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
        @SuppressWarnings("rawtypes")
        public Object refInvokeOperation(
            RefObject requestedOperation,
            List args
        ) throws RefException {
            throw newUnsupportedOperationException(REFLECTIVE, requestedOperation);
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refInvokeOperation(java.lang.String, java.util.List)
         */
        @SuppressWarnings("rawtypes")
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
                objectId == null ? null : objectId.toXRI();
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
        @SuppressWarnings("rawtypes")
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
         * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_0#refInitialize(boolean, boolean)
         */
        @Override
        public void refInitialize(
            boolean setRequiredToNull,
            boolean setOptionalToNull,
            boolean emptyMultivalued
        ) {
            if(this.cciDelegate instanceof RefObject_1_0) {
                ((RefObject_1_0)this.cciDelegate).refInitialize(
                    setRequiredToNull,
                    setOptionalToNull,
                    emptyMultivalued
                );
            } else {
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
            Object objectId = ReducedJDOHelper.getObjectId(this.cciDelegate);
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

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Cloneable#openmdxjdoClone()
         */
        public RefObject openmdxjdoClone(String... exclude) {
            return this.refClass.refCreateInstance(
                Collections.singletonList(
                    ((Cloneable<?>)this.cciDelegate).openmdxjdoClone(exclude)
                )
            );
        }

    }

}
