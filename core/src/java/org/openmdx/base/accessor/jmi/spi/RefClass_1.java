/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefClass_1.java,v 1.26 2008/09/10 08:55:23 hburger Exp $
 * Description: RefClass_1 class
 * Revision:    $Revision: 1.26 $
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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.listener.ClearCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.StoreCallback;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefEnum;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_4;
import org.openmdx.base.accessor.jmi.spi.Jmi1ObjectInvocationHandler.StandardMarshaller;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.spi.AbstractManagerFactory;
import org.openmdx.base.persistence.spi.Entity_2_0;
import org.openmdx.compatibility.base.event.InstanceCallbackListener;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.marshalling.CachingMarshaller_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.java.Identifier;
import org.openmdx.uses.org.apache.commons.collections.set.MapBackedSet;

//---------------------------------------------------------------------------
/**
 * Standard implementation of RefClass_1_0. The implementation does not
 * support class-level features.
 * <p>
 * This implementation supports efficient serialization. The immediate
 * package is the only member. Other members are transient.
 */
public abstract class RefClass_1
implements Jmi1Class, Serializable {

    /**
     * Constructor 
     *
     * @param refPackage
     */
    public RefClass_1(
        RefPackage_1_0 refPackage
    ) {
        this.immediatePackage = (RefPackage_1_4) refPackage;
    }


    //-------------------------------------------------------------------------
    // RefClass
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public RefObject refCreateInstance(
        List iargs
    ) {
        return this.refCreateInstance(
            iargs,
            this
        );
    }

    //-------------------------------------------------------------------------
    /**
     * @param refClass refClass is supplied as class when constructing the object, i.e.
     *        the method refClass() of the constructed object returns refClass.
     */
    @SuppressWarnings("unchecked")
    public RefObject refCreateInstance(
        List args,
        RefClass refClass
    ) {
        try {
            if(this.defaultImplConstructor == null) {

                /**
                 * extract the class name from the refMofId() which contains the
                 * fully qualified class name as ':' separated name components
                 */
                String packageName = refMofId().substring(0, refMofId().lastIndexOf(':'));
                String className = refMofId().substring(refMofId().lastIndexOf(":") + 1);
                String bindingPackageSuffix = this.immediatePackage.refBindingPackageSuffix();
                /**
                 * Check whether the instance-level implementation class must be loaded 
                 * from the standard location or from configured alternate location.
                 */

                // get the interface class
                String classNameIntf =
                    packageName.replace(':', '.') + "." +
                    bindingPackageSuffix + "." +
                    className;
                try {
                    this.intfClass = Classes.getApplicationClass(classNameIntf);
                }  catch(Throwable t) {
                    try {
                        classNameIntf =
                            packageName.replace(':', '.') + "." +
                            bindingPackageSuffix + "." +
                            Identifier.CLASS_PROXY_NAME.toIdentifier(className);
                        this.intfClass = Classes.getApplicationClass(classNameIntf);
                    } catch(ClassNotFoundException e0) {
                        throw new ServiceException(e0);
                    }
                }

                // get the constructor of generated instance-level implementation. This is used as default 
                // implementation when no user-defined implementation can be found. Moreover, the default 
                // instance-level objects are passed as parameter to the user-defined instance as delegation 
                // object in the tie approach.
                this.defaultImplConstructor = null;
                if(!Names.JMI1_PACKAGE_SUFFIX.equals(bindingPackageSuffix)) {
                    throw new JmiServiceException(
                        new ServiceException(
                            StackedException.DEFAULT_DOMAIN,
                            StackedException.NOT_SUPPORTED,
                            "Unsupported binding. Supported are " + Arrays.asList(Names.JMI1_PACKAGE_SUFFIX),
                            new BasicException.Parameter("binding.name", bindingPackageSuffix)
                        )
                    );
                }
                try {
                    this.defaultImplConstructor = Jmi1ObjectInvocationHandler.class.getConstructor(
                        this.hasLegacyDelegate() ? new Class[]{
                            Object_1_0.class,
                            Jmi1Class.class
                        } : new Class[]{
                                PersistenceCapable.class,
                                Jmi1Class.class  
                            }
                    );
                } 
                catch(NoSuchMethodException e0) {
                    throw new ServiceException(e0);
                }

                // Get the user-defined implementation class if present
                this.userImplConstructor = null;
                this.userImplConstructorTie = null;
                if(this.immediatePackage.refUseOpenMdx1ImplLookup()) {

                    ModelElement_1_0 classDef = null;
                    try {
                        classDef = this.immediatePackage.refModel().getElement(this.refMofId());
                        SysLog.trace("class definition found for", this.refMofId());
                    } catch(ServiceException e) {
                        SysLog.trace("class definition not found for", this.refMofId());
                        throw e;
                    }
                    boolean enforceTie = false;

                    // test for possible alternate locations of implementation 
                    String implPackageName = this.immediatePackage.refImplPackageName(packageName);
                    SysLog.trace("configured implementation for package", packageName + "=" + implPackageName);
                    while(
                            (classDef != null) &&
                            (implPackageName != null)
                    ) {
                        try {
                            SysLog.trace("testing candidate implementation", implPackageName + "." + className + "Impl");
                            Class userImplClass = Classes.getApplicationClass(implPackageName + "." + className + "Impl");
                            // Try to find a constructor with signature (<Interface>). In
                            // this case the user-defined class implements the tie approach.              
                            try {
                                this.userImplConstructorTie = userImplClass.getConstructor(
                                    new Class[]{
                                        this.intfClass
                                    }
                                );
                                break;
                            } catch(NoSuchMethodException e) {
                                //
                                // Try to find a constructor with signature (Object_1_0, RefClass). In this case
                                // the user-defined class extends the generated instance-level class
                                //
                                if(enforceTie) {
                                    throw new ServiceException(e);
                                }
                                try {
                                    this.userImplConstructor = userImplClass.getConstructor(
                                        new Class[]{
                                            Object_1_0.class,
                                            RefClass.class
                                        }
                                    );
                                    break;
                                } catch(NoSuchMethodException e1) {
                                    throw new ServiceException(e1);
                                }
                            }
                        }

                        // no user-defined implementation class found. Try to find an
                        // implementation of a superclass of the requested class. If there
                        // are no superclasses fall back to default implementation.
                        catch(ClassNotFoundException e) {
                            SysLog.trace("class not found", implPackageName + "." + className + "Impl");
                            SysLog.trace("getting alternate candidate classes (superclasses defined in the same model package)");
                            List candidates = new ArrayList();
                            for(
                                    Iterator i = classDef.values("supertype").iterator();
                                    i.hasNext();
                            ) {
                                ModelElement_1_0 candidate = this.immediatePackage.refModel().getElement(i.next());
                                if(((String)candidate.values("qualifiedName").get(0)).startsWith(packageName)) {
                                    candidates.add(candidate);
                                }
                            }
                            // no superclass --> no user-defined impl 
                            if(candidates.size() == 0) {
                                SysLog.trace("no candidates found");
                                break;
                            }
                            if(candidates.size() > 1) {
                                throw new ServiceException(
                                    StackedException.DEFAULT_DOMAIN,
                                    StackedException.ASSERTION_FAILURE,
                                    "multiple superclasses found for class within same package. multiple inheritance is not supported. To solve provide an implementation for class.",
                                    new BasicException.Parameter("class", classDef.values("qualifiedName").get(0)),
                                    new BasicException.Parameter("supertypes", candidates)
                                );
                            }
                            classDef = (ModelElement_1_0)candidates.get(0);

                            // get corresponding interface class
                            try {
                                String qualifiedClassName = (String)classDef.values("qualifiedName").get(0);
                                packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(':'));
                                className = (String)classDef.values("name").get(0);
                                this.intfClass = Classes.getApplicationClass(
                                    packageName.replace(':', '.') + ".cci." + className
                                );
                                implPackageName = this.immediatePackage.refImplPackageName(packageName);
                            }
                            catch(ClassNotFoundException e1) {
                                throw new ServiceException(e1);
                            }
                            enforceTie = true;
                        }
                    }
                }
            }

            // Prepare argument as Object_1_0
            Object delegateInstance = null;
            boolean legacyDelegate = hasLegacyDelegate();
            if(args == null) {
                delegateInstance = legacyDelegate ?
                    this.immediatePackage.refObjectFactory().createObject(this.refMofId()) :
                        this.immediatePackage.getDelegate().newInstance(getDelegateClass());
            } else if(args.size() != 1){
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.ASSERTION_FAILURE,
                    "args must either be null or have exactly one element",
                    new BasicException.Parameter [] {
                        new BasicException.Parameter("args", args)
                    }
                );
            } else {
                Object argument = args.get(0);
                if(legacyDelegate) {
                    if(argument instanceof Path){
                        //
                        // When Path get the object from accessor. The marshaller itself calls
                        // refCreateInstance with the delegate instance as parameter
                        //
                        delegateInstance = this.immediatePackage.refObjectFactory().getObject(argument);
                    } else if(argument instanceof Object_1_0) {
                        delegateInstance = argument;
                    } else if(argument instanceof RefObject_1_0) {
                        delegateInstance = ((RefObject_1_0)argument).refDelegate();
                    } else throw new ServiceException(
                        StackedException.DEFAULT_DOMAIN,
                        StackedException.ASSERTION_FAILURE,
                        "argument must be null or instanceof [Path|Object_1_0|RefObject_1_0]",
                        new BasicException.Parameter("args", args)
                    );
                } else {
                    if(argument instanceof Path) {
                        //
                        // When Path get the object from accessor. The marshaller itself calls
                        // refCreateInstance with the delegate instance as parameter
                        //
                        delegateInstance = this.immediatePackage.getDelegate().getObjectById(argument);
                    } else if (argument instanceof PersistenceCapable) {
                        delegateInstance = argument;
                    } else throw new ServiceException(
                        StackedException.DEFAULT_DOMAIN,
                        StackedException.ASSERTION_FAILURE,
                        "argument must be null or instanceof [Path|PersistenceCapable]",
                        new BasicException.Parameter("args", args)
                    );
                }
            }

            /**
             * Create an instance with the help of the constructors in the following order:
             * <ul>
             * <li>userImplConstructor != null --> create an user instance with the standard constructor</li>
             * <li>userImplConstructorTie != null --> create an user instance with the tie constructor</li>
             * <li>defaultImplConstructor != null -> create a default instance with the standard constructor</li>
             * </ul>
             */
            try {
                RefObject_1_0 instance = null;
                if(this.userImplConstructor != null) {
                    instance = (RefObject_1_0)this.userImplConstructor.newInstance(
                        delegateInstance,
                        refClass
                    );
                } 
                else {
                    Object defaultInstance = this.defaultImplConstructor.newInstance(
                        delegateInstance,
                        refClass
                    );
                    if(defaultInstance instanceof InvocationHandler) {
                        defaultInstance = Proxy.newProxyInstance(
                            this.getClass().getClassLoader(),
                            legacyDelegate ? 
                                new Class[]{this.intfClass, PersistenceCapable.class} :
                                    new Class[]{this.intfClass, PersistenceCapable.class, Entity_2_0.class},
                                    (InvocationHandler)defaultInstance
                        );
                    }
                    instance = (RefObject_1_0)defaultInstance;
                    if(this.userImplConstructorTie != null) {
                        RefObject_1_0 tieInstance = instance = (RefObject_1_0)this.userImplConstructorTie.newInstance(
                            instance
                        );
                        if(!this.intfClass.isInstance(instance)) {
                            instance = (RefObject_1_0)this.immediatePackage.refObject(
                                tieInstance,
                                (RefObject_1_0)defaultInstance
                            );
                        }
                    }
                }
                //
                // Instance Lifecycle Listener's openMDX 1 API
                // 
                if(instance instanceof InstanceCallbackListener) {
                    instance.refDelegate().objAddEventListener(
                        null,
                        (InstanceCallbackListener)instance
                    );
                }
                //
                // Instance Lifecycle Listener's openMDX 2 API
                // 
                if(
                        instance instanceof ClearCallback ||
                        instance instanceof StoreCallback ||
                        instance instanceof DeleteCallback ||
                        instance instanceof LoadCallback
                ) {
                    instance.refDelegate().objAddEventListener(
                        null,
                        InstanceCallbackAdapter_1.newInstance(instance)
                    );
                }
                //
                // Keep a reference to the newly created instance
                //
                if(args == null){
                    ((CachingMarshaller_1_0)refOutermostPackage()).cache(delegateInstance, instance);
                }
                return instance;
            } catch(InvocationTargetException e) {
                Throwable throwable = e.getTargetException();
                throw new ServiceException(
                    throwable instanceof Exception ? (Exception)throwable : e 
                );
            } catch(IllegalAccessException e) {
                throw new ServiceException(e);
            } catch(InstantiationException e) {
                throw new ServiceException(e);
            }

        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not remember all created instances. Therefore this
     * operation is not supported.
     */
    @SuppressWarnings("unchecked")
    public Collection refAllOfType() {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------  
    /**
     * RefClass_1 does not remember all created instances. Therefore this 
     * operation is not supported.
     */
    @SuppressWarnings("unchecked")
    public Collection refAllOfClass() {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------  
    /**
     * The call is delegated to the refImmediatePackage().
     */
    @SuppressWarnings("unchecked")
    public final RefStruct refCreateStruct(
        RefObject structType,
        List args
    ) {
        return this.immediatePackage.refCreateStruct(
            structType,
            args
        );
    }

    //-------------------------------------------------------------------------
    /**
     * The call is delegated to the refImmediatePackage().
     */
    @SuppressWarnings("unchecked")
    public final RefStruct refCreateStruct(
        String structName,
        List args
    ) {
        return this.immediatePackage.refCreateStruct(
            structName,
            args
        );
    }

    //-------------------------------------------------------------------------
    /**
     * The call is delegated to the refImmediatePackage().
     */
    public final RefEnum refGetEnum(
        RefObject enumType,
        String literalName
    ) {
        return this.immediatePackage.refGetEnum(
            enumType,
            literalName
        );
    }

    //-------------------------------------------------------------------------
    /**
     * The call is delegated to the refImmediatePackage().
     */
    public final RefEnum refGetEnum(
        String enumName,
        String literalName
    ) {
        return this.immediatePackage.refGetEnum(
            enumName,
            literalName
        );
    }

    //-------------------------------------------------------------------------
    // RefFeatured interface
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not support class-level features. This operation
     * is not supported.
     */
    public void refSetValue(
        RefObject feature,
        Object value
    ) throws JmiException {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not support class-level features. This operation
     * is not supported.
     */
    public void refSetValue(
        String featureName,
        Object value
    ) throws JmiException {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not support class-level features. This operation
     * is not supported.
     */
    public void refSetValue(
        String featureName,
        int index,
        Object value
    ) throws JmiException {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not support class-level features. This operation
     * is not supported.
     */
    public Object refGetValue(
        RefObject feature
    ) throws JmiException {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not support class-level features. This operation
     * is not supported.
     */
    public Object refGetValue(
        RefObject feature,
        int index
    ) throws JmiException {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not support class-level features. This operation
     * is not supported.
     */
    public Object refGetValue(
        String featureName
    ) throws JmiException {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not support class-level features. This operation
     * is not supported.
     */
    public Object refGetValue(
        String featureName,
        int index
    ) throws JmiException {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not support class-level features. This operation
     * is not supported.
     */
    @SuppressWarnings("unchecked")
    public Object refInvokeOperation(
        RefObject requestedOperation,
        List args
    ) throws JmiException {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not support class-level features. This operation
     * is not supported.
     */
    @SuppressWarnings("unchecked")
    public Object refInvokeOperation(
        String operationName,
        List args
    ) throws JmiException {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    // RefBaseObject
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    /**
     * Returns the ModelElement_1_0 of this class.
     */
    public RefObject refMetaObject(
    ) {
        try {
            return new RefMetaObject_1(
                this.immediatePackage.refModel().getElement(
                    "org:omg:model1:Class"
                )
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    public final RefPackage refImmediatePackage(
    ) {
        return this.immediatePackage;
    }

    //-------------------------------------------------------------------------
    public final RefPackage refOutermostPackage(
    ) {
        return this.immediatePackage.refOutermostPackage();
    }

    //-------------------------------------------------------------------------  
    /**
     * Must be implemented by the concrete subclass.
     */
    public String refMofId(
    ) throws JmiException {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /**
     * RefClass_1 does not supporte class-level features. Therefore verification
     * is always successful.
     */
    @SuppressWarnings("unchecked")
    public Collection refVerifyConstraints(
        boolean deepVerify
    ) {
        return new ArrayList();
    }

    //-------------------------------------------------------------------------
    // Implements Jmi1Class
    //-------------------------------------------------------------------------

    /**
     * Convert a model class name to the corresponding delegate class
     * 
     * @return the corresponding delegate class
     * 
     * @throws JDOFatalUserException 
     */
    public Class<?> getDelegateClass (
    ) {
        if(this.delegateClass == null) {
            PersistenceManagerFactory delegateManagerFactory = this.immediatePackage.getDelegate().getPersistenceManagerFactory();
            String bindingPackageSuffix = delegateManagerFactory instanceof AbstractManagerFactory 
            ? ((AbstractManagerFactory)delegateManagerFactory).getBindingPackageSuffix() 
                : null;
            String[] modelClass = refMofId().split(":");
            StringBuilder javaClass = new StringBuilder();
            int iLimit = modelClass.length - 1;
            for(
                    int i = 0;
                    i < iLimit;
                    i++
            ) {
                javaClass.append(
                    Identifier.PACKAGE_NAME.toIdentifier(modelClass[i])
                ).append(
                    '.'
                );
            }
            String className = javaClass.append(
                bindingPackageSuffix == null ? Names.CCI2_PACKAGE_SUFFIX : bindingPackageSuffix
            ).append(
                '.'
            ).append(
                Identifier.CLASS_PROXY_NAME.toIdentifier(modelClass[iLimit])
            ).toString();
            try {
                return Classes.getApplicationClass(className);
            } 
            catch (ClassNotFoundException exception) {
                throw new JDOFatalUserException(
                    "Delegate class evauluation failure",
                    new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        "Class not found",
                        new BasicException.Parameter("mofId", this.refMofId()),
                        new BasicException.Parameter("className", className)
                    )
                );
            }
        }
        return this.delegateClass;
    }

    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.Jmi1Class#hasLegacyDelegate()
     */
    public boolean hasLegacyDelegate(
    ) {
        return this.immediatePackage.hasLegacyDelegate();
    }

    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.Jmi1Class#getClassDef()
     */
    public FeatureMapper getFeatureMapper(
    ) {
        try {
            if(this.featureMapper == null) {
                ModelElement_1_0 classDef = this.immediatePackage.refModel().getElement(this.refMofId());   
                this.featureMapper = new FeatureMapper(
                    classDef,
                    this.hasLegacyDelegate() ?
                        this.getClass() :
                            this.getDelegateClass()
                );
            }
            return this.featureMapper;
        }
        catch(Exception e) {
            throw new JmiServiceException(e);
        }
    }

    //------------------------------------------------------------------------
    public StandardMarshaller getMarshaller(
    ) {
        if(this.marshaller == null) {
            this.marshaller = new StandardMarshaller(
                (org.openmdx.compatibility.base.marshalling.Marshaller) this.refOutermostPackage()
            );
        }
        return this.marshaller;
    }

    //-------------------------------------------------------------------------
    // RefClass_1_1
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Set<ModelElement_1_0> refFeaturesHavingNoImpl(
    ) {
        return this.featuresHavingNoImpl == null ? 
            this.featuresHavingNoImpl = MapBackedSet.decorate(new IdentityHashMap<ModelElement_1_0,Void>()):
                this.featuresHavingNoImpl;
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------

    /**
     * @serial
     */
    private RefPackage_1_4 immediatePackage = null;

    private transient Constructor<?> defaultImplConstructor = null;
    private transient Constructor<?> userImplConstructor = null;
    private transient Constructor<?> userImplConstructorTie = null;
    private transient Class<?> intfClass = null;
    private transient Class<?> delegateClass = null;
    private transient Set<ModelElement_1_0> featuresHavingNoImpl = null;
    private transient FeatureMapper featureMapper = null;
    private transient StandardMarshaller marshaller = null;

}

//--- End of File -----------------------------------------------------------

