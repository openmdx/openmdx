/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RefRootPackage_1.java,v 1.66 2008/07/06 21:01:45 wfro Exp $
 * Description: RefRootPackage_1 class
 * Revision:    $Revision: 1.66 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/07/06 21:01:45 $
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.spi;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;
import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_2;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_3;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_4;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.spi.MultithreadedObjectFactory_1;
import org.openmdx.base.accessor.generic.spi.ViewObject_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefClass_1_0;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackageFactory_1_2;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_2;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_4;
import org.openmdx.base.exception.NotSupportedException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.object.spi.InstanceLifecycleNotifier;
import org.openmdx.base.persistence.spi.Entity_2_0;
import org.openmdx.base.persistence.spi.OptimisticTransaction_2_0;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1Factory;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_3;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.marshalling.CachingMarshaller;
import org.openmdx.compatibility.base.marshalling.CachingMarshaller_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.spi.Model_1;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.java.Identifier;

/**
 * RefRootPackage_1 class. This is at the same time the JMI root package which 
 * acts as a factory for creating application-specific packages by calling 
 * refPackage().
 */
public class RefRootPackage_1
    extends RefPackage_1
    implements RefPackageFactory_1_2, CachingMarshaller_1_0, Serializable 
{

    /**
     * Constructor 
     *
     * @param objectFactory
     * @param persistenceManagerFactory
     * @param bindingPackageSuffix
     * @param optimisticTransaction TODO
     */
    RefRootPackage_1(
        ObjectFactory_1_4 objectFactory,
        PersistenceManagerFactory persistenceManagerFactory,
        String bindingPackageSuffix, 
        OptimisticTransaction_2_0 optimisticTransaction
    ) {
        this(
            null, // lazy instantiation of viewManager
            null, // viewContext
            true, // legacyDelegate
            persistenceManagerFactory.getMultithreaded() ? new MultithreadedObjectFactory_1 (
                objectFactory,
                optimisticTransaction
            ) : objectFactory,
            null, // standardDelegate
            null, // packageImpls
            null, // context
            bindingPackageSuffix,
            false, // throwNotFoundIfNull
            true, // useOpenMdx1ImplLookup
            false, // re-fetch
            persistenceManagerFactory,
            optimisticTransaction
        );
    }

    /**
     * Constructor 
     * <p>
     * Same as this(objectFactory, null, null)
     *
     * @param objectFactory
     * 
     * @deprecated
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory
    ) {
        this(
            objectFactory,
            true
        );
    }

    /**
     * Constructor 
     * <p>
     * Same as this(objectFactory, null, null)
     *
     * @param objectFactory
     * @param throwNotFoundIfNull
     * 
     * @deprecated
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        boolean throwNotFoundIfNull
    ) {
        this(
            objectFactory,
            null, // packageImpls
            null, // context
            throwNotFoundIfNull
        );
    }

    /**
     * Constructor 
     * <p>
     * Same as this(objectFactory, null, null)
     *
     * @param objectFactory
     * @param bindingPackageSuffix
     * @param throwNotFoundIfNull
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        String bindingPackageSuffix,
        boolean throwNotFoundIfNull
    ) {
        this(
            objectFactory,
            null, // packageImpls
            null, // context
            bindingPackageSuffix,
            throwNotFoundIfNull
        );
    }

    /**
     * Constructor 
     * <p>
     * Same as this(objectFactory, null, null)
     *
     * @param objectFactory
     * @param bindingPackageSuffix
     * @param throwNotFoundIfNull
     * @param useOpenMdx1UseImplLookup
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        String bindingPackageSuffix,
        boolean throwNotFoundIfNull,
        boolean useOpenMdx1UseImplLookup
    ) {
        this(
            objectFactory,
            null,
            null,
            bindingPackageSuffix,
            throwNotFoundIfNull,
            useOpenMdx1UseImplLookup
        );
    }

    /**
     * Constructor 
     * <p>
     * Same as this(objectFactory, null, null, defaultLocationSuffix, true).
     *
     * @param objectFactory
     * @param bindingPackageSuffix
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        String bindingPackageSuffix
    ) {
        this(
            objectFactory,
            null,
            null,
            bindingPackageSuffix,
            true
        );
    }

    /**
     * Constructor 
     * <p>
     * Same as this(objectFactory, packageImpls, context, "cci").
     *
     * @param objectFactory
     * @param packageImpls
     * @param context
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        Map<String,String>  packageImpls,
        Object context
    ) {
        this(
            objectFactory,
            packageImpls,
            context,
            Names.CCI1_PACKAGE_SUFFIX,
            false // throwNotFoundIfNull
        );
    }

    /**
     * Constructor 
     * <p>
     * Same as this(objectFactory, packageImpls, context, "accessor.jmi", throwNotFoundIfNull)
     *
     * @param objectFactory
     * @param packageImpls
     * @param context
     * @param throwNotFoundIfNull
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        Map<String,String>  packageImpls,
        Object context,
        boolean throwNotFoundIfNull
    ) {
        this(
            objectFactory,
            packageImpls,
            context,
            null,
            throwNotFoundIfNull
        );
    }

    /**
     * Constructor.
     *
     * @param objectFactory delegation object factory. This JMI implementation
     *        implements the facade pattern. The 'real' objects are managed by
     *        the specified object factory. The JMI classes put a typed JMI
     *        facade on top of the objects managed by the factory. All object
     *        handling is performed by the object factory.
     *
     * @param packageImpls Specifies the location of the JMI implementation 
     *        classes in the format [key=<qualified package name>,value=<java
     *        package name>]. The marshaller looks up the implementation classes
     *        at the specified location.
     *        
     * @param context user-specific context object. The context object is available
     *        from all instance-level JMI objects. The framework does 
     *        not read or update the context object.
     *
     * @param bindingPackageSuffix The default value is 'cci'. This suffix is
     *        required to construct a default package name for looking up the JMI
     *        interface and implementation classes which is: <qualified model name
     *        as java package name>.<bindingPackageSuffix>.<JMI interface or class name>,
     *        e.g. 'org.omg.model1.cci.StructuralFeature' or
     *        'org.omg.model1.cci.StructuralFeatureImpl'. In case of .NET
     *        deployment the defaultLocationSuffix should be set to 'cci.dotnet'.
     *
     * @param throwNotFoundIfNull if true RefObject.get<feature>(qualifier) always throws
     *        a NOT_FOUND exception independent of the multiplicity (0..1|1..1)
     *        of the modeled reference. This flag is for backwards compatibility.
     *        If false get<feature>(qualifier) throws a NOT_FOUND if the object
     *        was not found and multiplicity is 1..1. It returns null if the
     *        object was not found and multiplicity is 0..1.
     * @throws ServiceException 
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        Map<String,String>  packageImpls,
        Object context,
        String bindingPackageSuffix,
        boolean throwNotFoundIfNull
    ) {
        this(
            objectFactory,
            packageImpls,
            context,
            bindingPackageSuffix,
            throwNotFoundIfNull,
            true // useOpenMdx1ImplLookup
        );
    }

    /**
     * Constructor.
     *
     * @param persistenceManagerFactory the PersistenceManagerFactory
     *        representing this RefPackage.
     *        
     * @param objectFactory delegation object factory. This JMI implementation
     *        implements the facade pattern. The 'real' objects are managed by
     *        the specified object factory. The JMI classes put a typed JMI
     *        facade on top of the objects managed by the factory. All object
     *        handling is performed by the object factory.
     *        
     * @param packageImpls Specifies the location of the JMI implementation 
     *        classes in the format [key=<qualified package name>,value=<java
     *        package name>]. The marshaller looks up the implementation classes
     *        at the specified location.
     *        
     * @throws ServiceException 
     */
    @SuppressWarnings("unchecked")
    public RefRootPackage_1(
        PersistenceManagerFactory persistenceManagerFactory,
        ObjectFactory_1_0 objectFactory,
        Map<String,String> packageImpls
    ) {
        this(
            null, // lazy instantiation of viewManager
            null, // viewContext
            true, // legacyDelegate
            objectFactory,
            null, // standardDelegate
            packageImpls,
            null, // context
            Names.JMI1_PACKAGE_SUFFIX,
            false, // throwNotFoundIfNull
            false, // useOpenMdx1ImplLookup
            false, // re-fetch
            persistenceManagerFactory,
            null // optimisticTransaction
        );
    }
    
    /**
     * Constructor 
     * <p>
     * The standard delegate constructor
     * 
     * @param persistenceManagerFactory the persistence manager factory. 
     * @param persistenceManager the delegate persistence manager. 
     */
    public RefRootPackage_1(
        PersistenceManagerFactory persistenceManagerFactory,
        PersistenceManager entityManager
    ){
        this(
            null, // viewManager
            null, // viewContext
            false, // legacyDelegate
            null, // objectFactory
            entityManager, // standardDelegate
            null, // packageImpls
            null, // context
            Names.JMI1_PACKAGE_SUFFIX,
            false, // throwNotFoundIfNull,
            false, // useOpenMdx1ImplLookup
            true, // re-fetch 
            persistenceManagerFactory, 
            null // optimisticTransaction
        );
    }
    
    /**
     * Constructor 
     *
     * @param standardDelegate the delegate
     * @param packageImpls
     */
    public RefRootPackage_1(
        PersistenceManagerFactory persistenceManagerFactory,
        PersistenceManager standardDelegate,
        Map<String,String> packageImpls
    ){
        this(
            null, // viewManager
            null, // viewContext
            false, // legacyDelegate
            null, // objectFactory
            standardDelegate, // standardDelegate
            packageImpls,
            null, // context
            Names.JMI1_PACKAGE_SUFFIX,
            false, // throwNotFoundIfNull,
            false, // useOpenMdx1ImplLookup
            false, // re-fetch
            persistenceManagerFactory,
            null // optimisticTransaction
        );
    }

    /**
     * Constructor 
     *
     * @param objectFactory
     * @param packageImpls
     * @param context
     * @param bindingPackageSuffix
     * @param throwNotFoundIfNull
     * @param useOpenMdx1ImplLookup
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        Map<String,String> packageImpls,
        Object context,
        String bindingPackageSuffix,
        boolean throwNotFoundIfNull,
        boolean useOpenMdx1ImplLookup
    ) {
        this(
            null, // lazy instantiation of viewManager
            null, // viewContext
            true, // legacyDelegate
            objectFactory,
            null, // standardDelegate
            packageImpls,
            context,
            bindingPackageSuffix,
            throwNotFoundIfNull,
            useOpenMdx1ImplLookup, 
            false, // re-fetch
            null, // persistenceManagerFactory
            null // optimisticTransaction
        );
    }

    /**
     * Constructor
     * @param packageImpls Specifies the location of the JMI implementation 
     *        classes in the format [key=<qualified package name>,value=<java
     *        package name>]. The marshaller looks up the implementation classes
     *        at the specified location.
     * @param context user-specific context object. The context object is available
     *        from all instance-level JMI objects. The framework does 
     *        not read or update the context object.
     * @param bindingPackageSuffix The default value is 'cci'. This suffix is
     *        required to construct a default location for looking up the JMI
     *        interface and implementation classes which is: <qualified model name
     *        as java package name>.<bindingPackageSuffix>.<JMI interface or class name>,
     *        e.g. 'org.omg.model1.cci.StructuralFeature' or
     *        'org.omg.model1.cci.StructuralFeatureImpl'.
     * @param throwNotFoundIfNull if true RefObject.get<feature>(qualifier) always throws
     *        a NOT_FOUND exception independent of the multiplicity (0..1|1..1)
     *        of the modeled reference. This flag is for backwards compatibility.
     *        If false get<feature>(qualifier) throws a NOT_FOUND if the object
     *        was not found and multiplicity is 1..1. It returns null if the
     *        object was not found and multiplicity is 0..1.
     * @param useOpenMdx1ImplLookup if true user impls are looked up in openMDX 1
     *        compatibility mode. If false user impls are dispatched by RefObject
     *        proxies to user-defined impls.
     * @param consolidating tells whether delegates have to be re-fetched.
     * @param persistenceManagerFactory 
     *        The persistence manager factory, <code>null</code> for lazy initialization.
     * @param optimisticTransaction 
     * @param delegate the delegate, either a PersistenceManager instance or an
     *        ObjectFactory_1_0 instance. This JMI implementation
     *        implements the facade pattern. The 'real' objects are managed by
     *        the specified object factory. The JMI classes put a typed JMI
     *        facade on top of the objects managed by the factory. All object
     *        handling is performed by the object factory.
     * @param viewPackages the view package cache
     */
    private RefRootPackage_1(
        ViewManager_1 viewManager,
        Object viewContext,
        boolean legacyDelegate,
        ObjectFactory_1_0 objectFactory,
        PersistenceManager standardDelegate,
        Map<String,String>  packageImpls,
        Object context,
        String bindingPackageSuffix,
        boolean throwNotFoundIfNull,
        boolean useOpenMdx1ImplLookup, 
        boolean consolidating, 
        PersistenceManagerFactory persistenceManagerFactory, 
        OptimisticTransaction_2_0 optimisticTransaction
    ) {
        super(null, null);
        this.consolidating = consolidating;
        this.viewManager = viewManager;
        this.viewContext = viewContext;
        this.legacyDelegate = legacyDelegate;
        this.objectFactory = objectFactory;
        this.standardDelegate = standardDelegate;
        this.packageImpls = packageImpls;
        this.userContext = context;
        this.bindingPackageSuffix = bindingPackageSuffix == null ? 
            Names.CCI1_PACKAGE_SUFFIX : 
            bindingPackageSuffix;
        this.throwNotFoundIfNull = throwNotFoundIfNull;
        this.useOpenMdx1ImplLookup = useOpenMdx1ImplLookup;
        this.loadedPackages = new HashMap<String,RefPackage>();
        this.marshaller = new RefObject_1Marshaller(
            FORCE_CONCURRENT_MAP || (
                persistenceManagerFactory != null && persistenceManagerFactory.getMultithreaded()
            )
        );
        this.persistenceManager = null; // Lazy initialization
        this.persistenceManagerFactory = persistenceManagerFactory;
        this.optimisticTransaction = optimisticTransaction;
        SysLog.detail("configured package implementations", this.packageImpls);
    }
    
    //-------------------------------------------------------------------------
    public String refImplPackageName(
        String packageName
    ) {
        return this.packageImpls == null
            ? null
            : (String)this.packageImpls.get(packageName);
    }

    //-------------------------------------------------------------------------
    public boolean refUseOpenMdx1ImplLookup(
    ) {
        return this.useOpenMdx1ImplLookup;
    }

    //-------------------------------------------------------------------------
    public String refBindingPackageSuffix(
    ) {
        return this.bindingPackageSuffix;
    }

    //-------------------------------------------------------------------------
    public Object refUserContext(
    ) {
        return this.userContext;
    }

    //-------------------------------------------------------------------------
    boolean getThrowNotFoundIfNull(
    ) {
        return this.throwNotFoundIfNull;
    }
    
    //-------------------------------------------------------------------------
    /**
     * Re-fetch the delegate if necessary
     * 
     * @param persistenceCapable the delegate
     * 
     * @return the correct delegate
     */
    final Object getPersistenceCapable(
        Object persistenceCapable
    ){
        if(this.consolidating) {
            Object objectId = JDOHelper.getObjectId(persistenceCapable);
            return objectId == null ? persistenceCapable : this.standardDelegate.getObjectById(objectId);
        } else {
            return persistenceCapable;
        }
    }

    /**
     * Create an implementation instance
     * 
     * @param qualifiedClassName
     * @param refDelegate
     * @param self
     * @param next
     * 
     * @return the  implementation instance
     */
    private Object refCreateImpl(
        String qualifiedClassName,
        RefObject_1_0 refDelegate,
        Object self,
        Object next
    ) {
        if(this.classesNotHavingUserImpl.contains(qualifiedClassName)) {
            return null;
        }
        Constructor<?> userImplConstructor = this.userImplConstructors.get(qualifiedClassName);
        if(userImplConstructor == null) {
            String qualifiedPackageName = qualifiedClassName.substring(
                0, 
                qualifiedClassName.lastIndexOf(":")
            );
            String className = Identifier.CLASS_PROXY_NAME.toIdentifier(
                qualifiedClassName.substring(qualifiedClassName.lastIndexOf(":") + 1)
            );
            Class<?> userClass = null;
            String implPackageName = this.refImplPackageName(
                qualifiedPackageName
            );
            if(implPackageName != null) {
                try { 
                    userClass = Classes.getApplicationClass(implPackageName + "." + className + "Impl");
                }
                catch(ClassNotFoundException e) {
                    this.classesNotHavingUserImpl.add(qualifiedClassName);
                    SysLog.info("No user impl found", Arrays.asList(implPackageName, className));
                }                                    
            }
            // Fallback. Try to find impl in declaring package
            if(userClass == null) {
                implPackageName = this.refImplPackageName(
                    qualifiedPackageName
                );
                if(implPackageName != null) {
                    try { 
                        userClass = Classes.getApplicationClass(implPackageName + "." + className + "Impl");
                    }
                    catch(ClassNotFoundException e) {
                        SysLog.info("No user impl found", Arrays.asList(implPackageName, className));                    
                    }
                }
            }
            if(userClass != null) {
                String bindingPackageSuffix = this.refBindingPackageSuffix();
                String cci2QualifiedInterfaceName =
                    qualifiedPackageName.replace(':', '.') + "." +
                    Names.CCI2_PACKAGE_SUFFIX + "." +
                    Identifier.CLASS_PROXY_NAME.toIdentifier(className);                
                String bindingQualifiedInterfaceName =
                    qualifiedPackageName.replace(':', '.') + "." +
                    bindingPackageSuffix + "." +
                    Identifier.CLASS_PROXY_NAME.toIdentifier(className);    
                try { 
                    try {
                        userImplConstructor = userClass.getConstructor(
                            new Class[]{
                                Classes.getApplicationClass(bindingQualifiedInterfaceName),
                                Classes.getApplicationClass(cci2QualifiedInterfaceName)
                            }
                        );
                    }
                    catch(ClassNotFoundException e) {
                        SysLog.warning("Required interfaces not found on class path", Arrays.asList(bindingQualifiedInterfaceName, cci2QualifiedInterfaceName));
                    }
                }
                catch(NoSuchMethodException e) {
                    SysLog.info("Missing constructor in user implementation. Trying fallback.", Arrays.asList(bindingQualifiedInterfaceName, cci2QualifiedInterfaceName));
                    try {
                        userImplConstructor = userClass.getConstructor(
                            new Class[]{
                                Classes.getApplicationClass(cci2QualifiedInterfaceName),
                                Classes.getApplicationClass(cci2QualifiedInterfaceName)
                            }
                        );
                    }
                    catch(NoSuchMethodException e0) {
                        SysLog.warning("Missing constructor in user implementation", Arrays.asList(cci2QualifiedInterfaceName, cci2QualifiedInterfaceName));
                    }
                    catch(ClassNotFoundException e0) {
                        SysLog.warning("Required interface not found on class path", cci2QualifiedInterfaceName);
                    }
                }                    
                this.userImplConstructors.put(
                    qualifiedClassName, 
                    userImplConstructor
                );
            }
        }
        if(userImplConstructor == null) {
            this.classesNotHavingUserImpl.add(qualifiedClassName);
        } else {
            try {
                return this.legacyDelegate ? userImplConstructor.newInstance(
                    refDelegate,
                    Proxy.newProxyInstance(
                        refDelegate.getClass().getClassLoader(),
                        refDelegate.getClass().getInterfaces(),
                        new Jmi1ObjectInvocationHandler(
                            refDelegate.refDelegate(),
                            (Jmi1Class)refDelegate.refClass(),
                            true
                        )
                    )
                ) : userImplConstructor.newInstance(
                    self,
                    next
                );
            } catch(Exception e) {
                new ServiceException(e).log();
                // Do NOT mark qualifiedClassName as having no implementation
            }
        }
        return null;
    }


    //-------------------------------------------------------------------------
    // Marshaller
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        return this.marshaller.unmarshal(source);
    }

    //-------------------------------------------------------------------------  
    @SuppressWarnings("unchecked")
    public Object marshal(
        Object source
    ) throws ServiceException {
        return
            this.persistenceManager != null &&
            source instanceof PersistenceCapable &&
            ((PersistenceCapable)source).jdoGetPersistenceManager() == this.persistenceManager ? 
            source :
            this.marshaller.marshal(source);
    }

    //-------------------------------------------------------------------------  
    /**
     * Registers an object unless an object matching the unmarshalled object is
     * already registerd.
     *
     * @param   unmarshalled
     *          the unmarshalled object
     * @param   marshalled
     *          the marshalled object
     *
     * @return  true if no object matching the unmarshalled object is registered
     *          yet
     */
    public boolean cache(
        Object unmarshalled,
        Object marshalled
    ){
        return this.marshaller.cache(unmarshalled,marshalled);
    }

    //-------------------------------------------------------------------------  
    /**
     * Evicts an object
     *
     * @param   marshalled
     *          the marshalled object
     * *
     * @return  true if the unmarshalled object has been evicted by this call
     */
    public boolean evict(
        Object marshalled
    ){
        return this.marshaller.evict(marshalled);
    }
  
    
    //-------------------------------------------------------------------------
    // RefPackageFactory_1_2
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackageFactory_1_2#getOptimisticTransaction()
     */
    public OptimisticTransaction_2_0 getOptimisticTransaction() {
        return this.optimisticTransaction;
    }

    //-------------------------------------------------------------------------
    // RefPackage_1_0
    //-------------------------------------------------------------------------

    public RefObject refObject(
        String refMofId
    ) {
        try {
            Path objectId = new Path(refMofId);
            Object object = this.legacyDelegate ? refObjectFactory().getObject(
                objectId
            ) : getDelegate().getObjectById(
                objectId
            );
            return (RefObject)marshal(object);
        } catch(ServiceException e) {
            throw new JmiServiceException(e);
        } catch(RuntimeServiceException e) {
            throw new JmiServiceException(e);
        } catch(JDOException e) {
            throw new JmiServiceException(e);
        }
    }
    
    //-------------------------------------------------------------------------
    public RefStruct refCreateStruct(
        String structName,
        Object arg
    ) {
        String packageName = structName.substring(0, structName.lastIndexOf(':'));
        return ((RefPackage_1_0)this.refPackage(
            packageName
        )).refCreateStruct(
            structName,
            arg
        );
    }

    //-------------------------------------------------------------------------
    public Model_1_0 refModel(
    ) {
        if(RefRootPackage_1.model == null) {
            try {
                RefRootPackage_1.model = new Model_1();
            }
            catch(ServiceException e) {
                throw new JmiServiceException(e);
            }
            if(this.objectFactory instanceof ModelHolder_1_0) {
                ((ModelHolder_1_0)this.objectFactory).setModel(RefRootPackage_1.model);
            }
        }
        return RefRootPackage_1.model;
    }

    //-------------------------------------------------------------------------
    public ObjectFactory_1_0 refObjectFactory(
    ) {
        if(this.legacyDelegate) {
            return this.objectFactory;
        } else {
            throw new IllegalStateException(
                "This RefPackage instance has a PersistenceManager as delegate"
            );
        }
    }

    //-------------------------------------------------------------------------
    public UnitOfWork_1_0 refUnitOfWork(
    ) {
        if(this.unitOfWork == null) {
            if(this.legacyDelegate) {
                try {
                    this.unitOfWork = this.objectFactory.getUnitOfWork();
                } catch (ServiceException exception) {
                    throw new JmiServiceException(exception);
                }
            } else {
                this.unitOfWork = new UnitOfWork_1(
                    this.standardDelegate.currentTransaction()
                );
            }
        }
        return this.unitOfWork;
    }

    //-------------------------------------------------------------------------
    // RefBaseObject
    //-------------------------------------------------------------------------
    public RefPackage refImmediatePackage(
    ) {
        return this;
    }

    //-------------------------------------------------------------------------
    public RefPackage refOutermostPackage(
    ) {
        return this;
    }

    //-------------------------------------------------------------------------
    // RefPackage
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    /**
     * The root package does not have a meta object.
     * 
     * @return RefObject null, the root package does not have a meta object.
     */
    public RefObject refMetaObject(
    ) {
        return null;
    }

    //-------------------------------------------------------------------------
    public RefPackage refPackage(
        String _nestedPackageName
    ) {
        String nestedPackageName = _nestedPackageName;
        RefPackage refPackage = null;
        if(nestedPackageName.indexOf("." + this.bindingPackageSuffix) > 0) {
            nestedPackageName = nestedPackageName.substring(0, nestedPackageName.indexOf("." + this.bindingPackageSuffix)).replace('.', ':');
        }
        try {
            String loadedClassName = null;
            if((refPackage = this.loadedPackages.get(nestedPackageName)) == null) {    
                String packageName = nestedPackageName.substring(
                    nestedPackageName.lastIndexOf(':') + 1
                );
                if(!Names.JMI1_PACKAGE_SUFFIX.equals(this.bindingPackageSuffix)) {
                    throw new JmiServiceException(
                        new ServiceException(
                            StackedException.DEFAULT_DOMAIN,
                            StackedException.NOT_SUPPORTED,
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("binding.name", bindingPackageSuffix)
                            },
                            "Unsupported binding. Supported are " + Arrays.asList(Names.JMI1_PACKAGE_SUFFIX)
                        )
                    );
                }
                String jmi1PackageNameIntf = nestedPackageName.replace(':', '.') + "." + this.bindingPackageSuffix + "." + Identifier.CLASS_PROXY_NAME.toIdentifier(packageName) + "Package";
                refPackage = (RefPackage)Proxy.newProxyInstance(
                    this.getClass().getClassLoader(), 
                    new Class[]{
                        Classes.getApplicationClass(loadedClassName = jmi1PackageNameIntf),
                        RefPackage_1_4.class
                    }, 
                    new Jmi1PackageInvocationHandler(
                        nestedPackageName + ":" + packageName,
                        this, 
                        this
                    )
                );
                this.refModel().addModels(
                    Collections.singleton(nestedPackageName)
                );
                this.loadedPackages.put(
                    nestedPackageName,
                    refPackage
                );    
            }
            if(refPackage != null) {
                return refPackage;
            }
            throw new JmiServiceException(
                new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_FOUND,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("class", loadedClassName)
                    },
                    "package class not found"
                )
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
        catch(ClassNotFoundException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
    }

    //-------------------------------------------------------------------------
    public RefClass refClass(
        String qualifiedClassName
    ) {
        RefClass refClass = null;
        if((refClass = this.classes.get(qualifiedClassName)) == null) {
            String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(':'));
            return this.refPackage(
                packageName
            ).refClass(qualifiedClassName);
        }
        else {
            return refClass;
        }
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public RefStruct refCreateStruct(
        String structName,
        List args
    ) {
        String packagePrefix = structName.substring(0, structName.lastIndexOf(':'));
//      String packageName = packagePrefix.substring(packagePrefix.lastIndexOf(':') + 1);
        return this.refPackage(
            packagePrefix
        ).refCreateStruct(
            structName,
            args
        );
    }

    //-------------------------------------------------------------------------
    public String refMofId(
    ) {
        return null;
    }
  
    //-------------------------------------------------------------------------
    // Implements RefPackageFactory_1_0
    //-------------------------------------------------------------------------

    /**
     * Create a new RefPackage
     * 
     * @return a new RefPackage instance
     */
    public RefPackage_1_1 createRefPackage() {
        try {
            if(this.legacyDelegate) {
                ObjectFactory_1_2 objectFactory = (ObjectFactory_1_2)this.objectFactory;
                Connection_1Factory connectionFactory = objectFactory.getConnectionFactory();
                boolean containerManagedUnitOfWork = Boolean.TRUE.equals(
                    objectFactory.hasContainerManagedUnitOfWork()
                ); 
                Connection_1_3 connection = connectionFactory.getConnection(
                    containerManagedUnitOfWork
                ); 
                return new RefRootPackage_1(
                    connection,
                    this.packageImpls,
                    this.userContext,
                    this.bindingPackageSuffix,
                    this.throwNotFoundIfNull
                );
            } else {
                PersistenceManager entityManager = this.standardDelegate.getPersistenceManagerFactory().getPersistenceManager(); 
                return new RefRootPackage_1(
                    refPersistenceManagerFactory(),
                    entityManager
                );  
            }
        } catch (Exception exception) {
            throw new JmiServiceException(exception);
        }
    }
    
    //-------------------------------------------------------------------------
    // Implements RefPackageFactory_1_1
    //-------------------------------------------------------------------------

    /**
     * Create a context specific RefPackage
     * 
     * @param viewContext
     * 
     * @return a context specific RefPackage
     */
    public RefPackage_1_2 getRefPackage(
        InteractionSpec viewContext
    ){
        if(this.viewContext == viewContext) {
            return this;
        } else try {
            if(this.viewManager == null) {
                this.viewManager = new ViewManager_1(
                    (ObjectFactory_1_4)this.objectFactory, 
                    refModel()
                );
            }
            RefPackage_1_2 refPackage = this.viewManager.getView(viewContext);
            if(refPackage == null) {
                this.viewManager.addView(
                    viewContext,
                    refPackage = new RefRootPackage_1(
                        this.viewManager,
                        viewContext,
                        true, // legacyDelegate
                        this.viewManager.getViewConnection(viewContext),
                        null, // standardDelegate
                        this.packageImpls,
                        this.userContext,
                        this.bindingPackageSuffix,
                        this.throwNotFoundIfNull,
                        this.useOpenMdx1ImplLookup, false, null, optimisticTransaction
                    ) 
                );
            }
            return refPackage;
        } catch (Exception exception) {
            throw new JmiServiceException(
                new NotSupportedException(
                    exception,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter(
                            "objectFactory.class", 
                            this.objectFactory == null ? "n/a" : this.objectFactory.getClass().getName()
                        ),
                    },
                    "The given RefPackage is unable to create a view"
                )
            );
        }
    }
  
    //-------------------------------------------------------------------------
    // Implements RefPackage_1_4
    //-------------------------------------------------------------------------
        
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1#refLegacyDelegate()
     */
    @Override
    public boolean hasLegacyDelegate() {
        return this.legacyDelegate;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_4#getDelegate()
     */
    public PersistenceManager getDelegate() {
        if(this.legacyDelegate) {
            throw new JDOFatalInternalException(
                "This RefPackage has an ObjectFactory_1_0 as delegate"
            );
        } else {
            return this.standardDelegate;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1#refCreateImpl(java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object refCreateImpl(
        String qualifiedClassName,
        Object self,
        Object next
    ) {
        return refCreateImpl(
            qualifiedClassName,
            null, // refDelegate
            self,
            next
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1#getDataStoreConnection()
     */
    @Override
    public JDOConnection getDataStoreConnection() {
        return new DataStoreConnection(
            hasLegacyDelegate() ? this.objectFactory : this.standardDelegate
        );
    }
    
    
    //-------------------------------------------------------------------------
    // Implements RefPackage_1_3
    //-------------------------------------------------------------------------

    /**
     * Retrieves the JDO Persistence Manager Factory.
     * 
     * @return the JDO Persistence Manager Factory configured according to this package
     */
    public PersistenceManagerFactory refPersistenceManagerFactory (
    ) {
        return this.persistenceManagerFactory == null ? this.persistenceManagerFactory = new PersistenceManagerFactory_1(
            this
        ) : this.persistenceManagerFactory; 
    }
    
    /**
     * Empty the cache
     */
    public void clear(){
        this.marshaller.clear();
        if(this.legacyDelegate) {
            if(this.objectFactory instanceof ObjectFactory_1_3) {
                ((ObjectFactory_1_3)this.objectFactory).clear();
            }
        } else {
            this.standardDelegate.evictAll();
        }
    }
    
    
    //-------------------------------------------------------------------------
    // Implements RefPackage_1_2
    //-------------------------------------------------------------------------

    /**
     * Retrieve the RefPackage's view context
     * 
     * @return the RefPackage's view context in case of a view,
     * <code>null</code> otherwise
     */
    public Object refViewContext(
    ){
        return this.viewContext;
    }

    /**
     * Returns an new RefObject with initial values
     * @param allValuesDirty
     * @param object
     * 
     * @return
     */
    public RefObject refCloneObject (
        String mofId,
        RefObject object, 
        boolean allValuesDirty
    ){
        RefObject_1_0 refObject = (RefObject_1_0) object;
        RefClass_1_0 refClass = (RefClass_1_0) refClass(
            refObject.refClass().refMofId()
        );
        ViewObject_1_0 viewObject = (ViewObject_1_0) refObject.refDelegate();
        try {
            return refClass.refCreateInstance(
                Collections.singletonList(
                    new CloneableObject_1(
                        mofId == null ? null : new Path(mofId), 
                            viewObject.getSourceDelegate(), 
                            allValuesDirty
                    )
                )
            );
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }
  
    //-------------------------------------------------------------------------
    // Implements RefPackage_1_1
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    /**
     * Retrieves the JDO Persistence Manager delegating to this package.
     * 
     * @return the JDO Persistence Manager delegating to this package.
     */
    public PersistenceManager refPersistenceManager(
    ) {
        return this.persistenceManager == null ? this.persistenceManager = new PersistenceManager_1(
            refPersistenceManagerFactory(),
            new InstanceLifecycleNotifier(),
            this
        ) : this.persistenceManager;
    }

    //-------------------------------------------------------------------------
    /**
     * Merge interfaces
     * 
     * @param primary
     * @param secondary
     * 
     * @return an array containing both interfaces
     */
    private static Collection<Class<?>> getInterfaces (
        Class<?> primary,
        Class<?> secondary
    ){
        Set<Class<?>> merged = new LinkedHashSet<Class<?>>();
        addInterfaces(merged, primary);
        addInterfaces(merged, secondary);
        return merged;
    }

    //-------------------------------------------------------------------------
    /**
     * Add the interfaces of a class and their super-classes
     * 
     * @param interfaces
     * @param startClass
     */
    private static void addInterfaces(
        Collection<Class<?>> interfaces,
        Class<?> startClass
    ){
        for(
            Class<?> currentClass = startClass;
            currentClass != null;
            currentClass = currentClass.getSuperclass()
        ){
            for(Class<?> candidate : currentClass.getInterfaces()) {
                interfaces.add(candidate);
            }
        }
    }
  
    //-------------------------------------------------------------------------
    /**
     * Returns a proxy implementing the interfaces of both
     * <code>RefObject</code>s and delegating to<ol>
     * <li>the primary <code>RefObject</code> if possible
     * <li>the secondary <code>RefObject</code> as fallback
     * </ol>
     * 
     * @param primary its methods override the secondary object's methods
     * @param secondary its interfaces are implemented by the proxy object as well
     * 
     * @return a proxy object delegating to the primary or secondary object as appropriate
     */
    public RefObject refObject (
        RefObject primary,
        RefObject secondary
    ){
        return Classes.newProxyInstance(
            new RefObjectHandler(primary, secondary),
            getInterfaces(
                primary.getClass(),
                secondary.getClass()
            )
        );
    }

    //-------------------------------------------------------------------------
    // Class RefObjectHandler
    //-------------------------------------------------------------------------

    static class RefObjectHandler implements Serializable, InvocationHandler  {

        RefObjectHandler(
            RefObject primary,
            RefObject secondary
        ) {
            this.primary = primary;
            this.secondary = secondary;
            this.mapping = null;
        }

        private static final long serialVersionUID = -5495786050475461969L;
        private final RefObject primary;
        private final RefObject secondary;
        private transient Map<Method,Method> mapping;

        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(
            Object proxy, 
            Method method, 
            Object[] args
        ) throws Throwable {
            Method primaryMethod = getPrimaryMethod(method);
            try {
                return primaryMethod != null ?
                    primaryMethod.invoke(primary, args) :
                        method.invoke(secondary, args);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }

        }

        /**
         * Retrieves the primary method
         * 
         * @param method
         * 
         * @return
         */
        private synchronized Method getPrimaryMethod(
            Method secondaryMethod
        ){
            Method primaryMethod;
            boolean tested;
            if(this.mapping == null) {
                this.mapping = new IdentityHashMap<Method,Method>();
                primaryMethod = null;
                tested = false;
            } else {
                primaryMethod = mapping.get(secondaryMethod);
                tested = primaryMethod != null || mapping.containsKey(secondaryMethod); 
            }
            if(!tested) {
                try {
                    primaryMethod = primary.getClass().getMethod(
                        secondaryMethod.getName(), 
                        secondaryMethod.getParameterTypes()
                    );                    
                } catch (NoSuchMethodException exception) {
                    // remember it by setting the value to null
                }
                this.mapping.put(secondaryMethod, primaryMethod);
            }
            return primaryMethod;
        }       

    }

    //-------------------------------------------------------------------------
    // Class RefObject_1Marshaller
    //-------------------------------------------------------------------------
    
    class RefObject_1Marshaller
        extends CachingMarshaller 
   {
        
        /**
         * Constructor 
         *
         * @param multithreaded
         */
        RefObject_1Marshaller(
            boolean multithreaded
        ) {
            super(multithreaded);
        }

        private static final long serialVersionUID = -6856573596590312011L;

        //-----------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.marshalling.CachingMarshaller#cache(java.lang.Object, java.lang.Object)
         */
        public boolean cache(Object unmarshalled, Object marshalled) {
            if(marshalled instanceof RefObject_1_0) {
                RefPackage refPackage = ((RefObject)marshalled).refOutermostPackage();
                if(
                    RefRootPackage_1.this != refPackage &&
                    refPackage instanceof CachingMarshaller_1_0
                ){
                    return ((CachingMarshaller_1_0)refPackage).cache(unmarshalled, marshalled);
                }
            }
            return super.cache(unmarshalled, marshalled);
        }
        
        //-----------------------------------------------------------------------
        public Object unmarshal(
          Object source
        ) throws ServiceException {
            if(RefRootPackage_1.this.legacyDelegate){
                if(source instanceof Entity_2_0) {
                    Object object = source;
                    while(object instanceof Entity_2_0) {
                        object = ((Entity_2_0)object).openmdxjdoGetDelegate();
                    }
                    return ((RefObject_1_0)object).refDelegate();
                } else if(source instanceof RefObject_1_0) {
                    return ((RefObject_1_0)source).refDelegate();
                }
            } else {
                if(source instanceof Entity_2_0) {
                    return ((Entity_2_0)source).openmdxjdoGetDelegate();
                }
            }
            return source;
        }
    
        //-----------------------------------------------------------------------  
        public Object createMarshalledObject(
          Object source
        ) throws ServiceException {
            if(RefRootPackage_1.this.legacyDelegate){
                if(source instanceof Object_1_0) {
                    String className = ((Object_1_0)source).objGetClass();
                    if(className != null) {
                        return RefRootPackage_1.this.refClass(className).refCreateInstance(
                            Collections.singletonList(source)
                        );
                    } else  throw new ServiceException(
                        StackedException.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("path", ((Object_1_0)source).objGetPath()),
                        },
                        "object class can not be determined"
                    );
                }
            } else {
                if(source instanceof PersistenceCapable) {
                    Object persistenceCapable = getPersistenceCapable(source);
                    return RefRootPackage_1.this.refClass(
                        getMofClassName(
                            persistenceCapable.getClass().getInterfaces()[0].getName().intern(),
                            RefRootPackage_1.this.refModel()
                        )
                    ).refCreateInstance(
                        Collections.singletonList(persistenceCapable)
                    );
                }
            }
            return source;
        }
    
        //-----------------------------------------------------------------------  
        protected void clear() {
            super.clear();
        }
    
        //-----------------------------------------------------------------------  
        Object remove(
            Object unmarshalled
        ){
            return super.mapping.remove(unmarshalled);
        }

    }

    /**
     * Retrieve the object's MOF class name
     * 
     * @param javaClass
     * 
     * @return the object's MOF class name
     */
    public static String getMofClassName(
        String javaClassName,
        Model_1_0 model
    ) throws JmiServiceException {
        String qualifiedClassName = classNames.get(javaClassName);
        if(qualifiedClassName == null) {
            try {
                String[] components = javaClassName.split("\\.");
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < components.length-2; i++) {
                    if(i > 0) sb.append(":");
                    sb.append(components[i]);
                }
                String objectPackageName = sb.toString();
                String objectClassName = components[components.length-1];
                for(ModelElement_1_0 element: model.getContent()) {
                    String qualifiedElementName = (String)element.values("qualifiedName").get(0);
                    String qualifiedPackageName = qualifiedElementName.substring(0, qualifiedElementName.lastIndexOf(":"));
                    String elementName = (String)element.values("name").get(0);
                    if(
                        objectPackageName.equals(qualifiedPackageName) &&
                        Identifier.CLASS_PROXY_NAME.toIdentifier(elementName).equals(objectClassName)
                    ) {
                        classNames.put(
                            javaClassName, 
                            qualifiedElementName
                        );
                        qualifiedClassName = qualifiedElementName;
                        break;
                    }                    
                }
                if(qualifiedClassName == null) {
                    throw new JmiServiceException(
                        new ServiceException(
                            StackedException.DEFAULT_DOMAIN,
                            StackedException.NOT_FOUND,
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("java-class", javaClassName),
                                new BasicException.Parameter("mof-package", objectPackageName),
                                new BasicException.Parameter("mof-class", objectClassName)
                            },
                            "model element for requested class not found"
                        )
                    );
                }
            } catch(ServiceException e) {
                throw new JmiServiceException(e);
            }
        }
        return qualifiedClassName;
    }
    
    //-------------------------------------------------------------------------
    // Implements RefPackage_1_1
    //-------------------------------------------------------------------------

    //-----------------------------------------------------------------------
    public Object refCreateImpl(
        String qualifiedClassName,
        RefObject_1_0 refDelegate
    ) {
        return refCreateImpl(
            qualifiedClassName,
            refDelegate,
            null, // self,
            null // next
        );
    }
          
    
    //------------------------------------------------------------------------
    // Class DataStoreConnection
    //------------------------------------------------------------------------

    /**
     * Data Store Connection
     */
    static class DataStoreConnection implements JDOConnection {

        /**
         * Constructor 
         *
         * @param nativeConnection
         */
        DataStoreConnection(Object nativeConnection) {
            this.nativeConnection = nativeConnection;
        }

        /**
         * 
         */
        private Object nativeConnection;
        
        /* (non-Javadoc)
         * @see javax.jdo.datastore.JDOConnection#close()
         */
        public void close(
        ) {
            this.nativeConnection = null;
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.JDOConnection#getNativeConnection()
         */
        public Object getNativeConnection() {
            return this.nativeConnection;
        }

    }

    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    
    /**
     * 
     */
    private static final boolean FORCE_CONCURRENT_MAP = true;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 1272557331049377936L;

    /**
     * Defines, whether the delegates have to be re-fetched.
     */
    private final boolean consolidating;
    
    /**
     * Defines, whether objectFactory or standardDelegate is valid.
     */
    final boolean legacyDelegate;
    
    /**
     * Contains the legacy delegate
     */
    private final ObjectFactory_1_0 objectFactory;
    
    /**
     * Contains the standard delegate
     */
    private final PersistenceManager standardDelegate;
    
    private final Object viewContext;
    private final Object userContext;
    protected ViewManager_1 viewManager;
    private final RefObject_1Marshaller marshaller;
    private final boolean throwNotFoundIfNull;
    private PersistenceManagerFactory persistenceManagerFactory;
    private transient PersistenceManager persistenceManager;
    private final OptimisticTransaction_2_0 optimisticTransaction;
    
    /**
     * All root packages share the loaded packages.
     */
    private final Map<String,RefPackage> loadedPackages;

    /**
     * Map which contains (qualified package name, alternate implementation) entries, 
     * where packageName is a java package containing JMI classes, i.e. instance-level,
     * class-level and package-level classes. The default location is constructed
     * from the qualifiedPackageName + "cci". However this default
     * can be overridden by an alternateImplementation which is typically
     * required when implementing JMI plug-ins. When a requested class can
     * not be found on the alternateImplementation loading falls back
     * to the default location.
     */
    private final Map<String,String> packageImpls;

    /**
     * defaultLocationSuffix. The default location for implementation lookup is constructed
     * as <qualified model name as java package name>.<suffix>.<interface or
     * class name>.
     */
    private final String bindingPackageSuffix;

    /**
     * Holds the union of models of the loaded sub-packages.
     */
    private static Model_1_0 model = null;

    private UnitOfWork_1_0 unitOfWork = null;
    
    /**
     * If true, object impl classes are located by the openMDX 1 impl lookup algorithm, i.e.
     * an implementation is located by the hard-coded name <code>class name</code>Impl or
     * <code>superclass name</code>Impl. 
     * If false, objects are always marshaled to proxy objects which dispatch itself to
     * user-defined implementations.
     */
    private final boolean useOpenMdx1ImplLookup;
  
    protected final Map<String,Constructor<?>> userImplConstructors = new HashMap<String,Constructor<?>>();
    protected final Set<String> classesNotHavingUserImpl = new HashSet<String>();
    
    /**
     * Holds the mapping of java proxy names to MOF qualified class names.
     */
    protected static final Map<String,String> classNames = new IdentityHashMap<String,String>();

}

//--- End of File -----------------------------------------------------------
