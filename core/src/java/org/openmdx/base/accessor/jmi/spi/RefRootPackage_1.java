/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RefRootPackage_1.java,v 1.134 2009/03/04 19:16:54 wfro Exp $
 * Description: RefRootPackage_1 class
 * Revision:    $Revision: 1.134 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/04 19:16:54 $
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

import java.beans.ExceptionListener;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.JDOException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.InvalidObjectException;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;
import javax.resource.cci.InteractionSpec;
import javax.security.auth.Subject;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.oasisopen.jmi1.RefContainer;
import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.PersistenceManagerFactory_1_0;
import org.openmdx.base.accessor.cci.PersistenceManager_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackageFactory_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_2;
import org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.view.InteractionSpecs;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.CachingMarshaller;
import org.openmdx.base.marshalling.CachingMarshaller_1_0;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Model_1_6;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.persistence.spi.InstanceLifecycleNotifier;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;
import org.w3c.cci2.Container;

/**
 * RefRootPackage_1 class. This is at the same time the JMI root package which 
 * acts as a factory for creating application-specific packages by calling 
 * refPackage().
 */
public class RefRootPackage_1
    extends RefPackage_1
    implements Jmi1Package_1_0, CachingMarshaller_1_0, RefPackageFactory_1_0, ExceptionListener 
{

    /**
     * Constructor 
     *
     * @param viewManager
     * @param legacyDelegate
     * @param objectFactory
     * @param standardDelegate
     * @param packageImpls
     * @param userObjects
     * @param userContext
     * @param persistenceManagerFactory
     * @param principals 
     * @param accessor 
     * @param viewContext
     */
    RefRootPackage_1(
        ConcurrentMap<InteractionSpec,RefRootPackage_1> viewManager,
        InteractionSpec interactionSpec,
        boolean legacyDelegate,
        PersistenceManager_1_0 objectFactory,
        PersistenceManager standardDelegate,
        Map<String,String>  packageImpls,
        Map<String, Object> userObjects,
        Object userContext,
        PersistenceManagerFactory persistenceManagerFactory,
        Set<? extends Principal> principals, 
        boolean accessor
    ) {
        super(
            null, // outermostPackage
            null // immediatePackage 
        );
        this.viewManager = viewManager;
        this.interactionSpec = interactionSpec;
        this.legacyDelegate = legacyDelegate;
        this.manager = objectFactory;
        this.standardDelegate = standardDelegate;
        this.packageImpls = packageImpls;
        this.userObjects = userObjects;
        this.userContext = userContext;
        String bindingPackageSuffix = persistenceManagerFactory instanceof PersistenceManagerFactory_1_0 ?
            ((PersistenceManagerFactory_1_0)persistenceManagerFactory).getBindingPackageSuffix() :
            null;
        this.bindingPackageSuffix = bindingPackageSuffix == null ? Names.JMI1_PACKAGE_SUFFIX : bindingPackageSuffix;
        this.loadedPackages = new ConcurrentHashMap<String,RefPackage>();
        this.marshaller = new RefObject_1Marshaller(
        );
        this.persistenceManager = null; // Lazy initialization
        this.persistenceManagerFactory = persistenceManagerFactory;
        this.principals = principals == null ? NO_PRINCIPALS : principals;
        this.accessor = accessor;
        SysLog.detail("configured package implementations", this.packageImpls);
    }

    /**
     * Constructor
     * <p><em>
     * Note:<br>
     * This constructor is invoked reflectively by PersistenceManagerFactory_2.
     * </em><p>
     * @param persistenceManagerFactory the PersistenceManagerFactory
     *        representing this RefPackage.
     * @param objectFactory delegation object factory. This JMI implementation
     *        implements the facade pattern. The 'real' objects are managed by
     *        the specified object factory. The JMI classes put a typed JMI
     *        facade on top of the objects managed by the factory. All object
     *        handling is performed by the object factory.
     * @param packageImpls Specifies the location of the JMI implementation 
     *        classes in the format [key=<qualified package name>,value=<java
     *        package name>]. The marshaller looks up the implementation classes
     *        at the specified location.
     * @param userObjects the user objects to be propagated to each persistence
     *        manager
     * @param principals the principals 
     *        
     * @throws ServiceException 
     * 
     * @see org.openmdx.application.dataprovider.kernel.ManagerFactory_2
     */
    public RefRootPackage_1(
        PersistenceManagerFactory persistenceManagerFactory,
        PersistenceManager_1_0 objectFactory,
        Map<String,String> packageImpls,
        Map<String,Object> userObjects,
        Set<? extends Principal> principals
    ) {
        this(
            null, // lazy instantiation of viewManager
            null, // viewContext
            true, // legacyDelegate
            objectFactory,
            null, // standardDelegate
            packageImpls,
            userObjects,
            null, // context
            persistenceManagerFactory, 
            principals, 
            false // accessor
        );
    }

    /**
     * Constructor 
     * <p><em>
     * Note:<br>
     * This constructor is invoked reflectively by PlugInManagerFactory_2.
     * </em><p>
     * @param persistenceManagerFactory
     * @param standardDelegate
     * @param packageImpls
     * @param userObjects the user objects to be propagated to each persistence
     *        manager
     * @param principals the principals 
     * 
     * @see org.openmdx.application.dataprovider.kernel.PlugInManagerFactory_2
     */
    public RefRootPackage_1(
        PersistenceManagerFactory persistenceManagerFactory,
        PersistenceManager standardDelegate,
        Map<String,String> packageImpls,
        Map<String,Object> userObjects,
        Set<? extends Principal> principals
    ){
        this(
            null, // viewManager
            null, // viewContext
            false, // legacyDelegate
            null, // objectFactory
            standardDelegate, // standardDelegate
            packageImpls,
            userObjects, 
            null, // context
            persistenceManagerFactory,  
            principals, 
            false // accessor
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
     * @throws ServiceException 
     */
    public RefRootPackage_1(
        PersistenceManager_1_0 objectFactory,
        Map<String,String>  packageImpls,
        Object context
    ) {
        this(
            null, // lazy instantiation of viewManager
            null, // viewContext
            true, // legacyDelegate
            objectFactory,
            null, // standardDelegate
            packageImpls,
            null, // userObjects
            context, // context
            new CompatibilityPersistenceManagerFactory(
                objectFactory,
                packageImpls,
                context,
                Names.JMI1_PACKAGE_SUFFIX
            ),
            null, // principals
            false // accessor
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
            null, // userObjects
            null, // context
            persistenceManagerFactory,  
            null, 
            true // accessor
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
     * @throws ServiceException 
     */
    public RefRootPackage_1(
        PersistenceManager_1_0 objectFactory
    ) {
        this(
            null, // lazy instantiation of viewManager
            null, // viewContext
            true, // legacyDelegate
            objectFactory,
            null, // standardDelegate
            null, // packageImpls,
            null, // userObjects
            null, // context
            new CompatibilityPersistenceManagerFactory(
                objectFactory,
                null, // packageImpls
                null, // context
                Names.JMI1_PACKAGE_SUFFIX
            ),
            null, // principals
            false // accessor
        );
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
    private Constructor<?> getUserImplConstructor(
        String qualifiedClassName
    ){
        Constructor<?> userImplConstructor = this.userImplConstructors.get(qualifiedClassName);
        if(userImplConstructor == NULL_CONSTRUCTOR) {
            return null;
        }
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
                } catch(ClassNotFoundException e) {
                    SysLog.info("No user impl found", Arrays.asList(implPackageName, className));
                }                                    
            }
            if(userClass == null) {
                this.userImplConstructors.put(qualifiedClassName, NULL_CONSTRUCTOR);
            } else {
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
                    userImplConstructor == null ? NULL_CONSTRUCTOR : userImplConstructor
                );
            }
        }
        return userImplConstructor;
    }
    
    //-------------------------------------------------------------------------
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
        Constructor<?> userImplConstructor = getUserImplConstructor(qualifiedClassName);
        if(userImplConstructor == null) {
            return null;
        }
        try {
            return this.legacyDelegate ? userImplConstructor.newInstance(
                refDelegate,
                Proxy.newProxyInstance(
                    refDelegate.getClass().getClassLoader(),
                    refDelegate.getClass().getInterfaces(),
                    new Jmi1ObjectInvocationHandler(
                        refDelegate.refDelegate(),
                        (Jmi1Class_1_0)refDelegate.refClass(),
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
            return null;
        }
    }



    //-------------------------------------------------------------------------
    // Implements ExceptionListener
    //-------------------------------------------------------------------------

    /**
     * Build an InvalidObjectException from a given cause
     * 
     * @param source
     * 
     * @return
     */
    protected InvalidObjectException toInvalidObjectException (
        Exception source
    ){
        return source instanceof InvalidObjectException ?
            (InvalidObjectException) source :
                new InaccessibleObject(source).getException();
    }

    /* (non-Javadoc)
     * @see java.beans.ExceptionListener#exceptionThrown(java.lang.Exception)
     */
    public void exceptionThrown(Exception cause) {
        throw toInvalidObjectException(cause);
    }

    //-------------------------------------------------------------------------
    // Implements Marshaller
    //-------------------------------------------------------------------------

    /**
     * Determines an object's interaction spec
     * 
     * @param object
     * 
     * @return an object's interaction spec
     */
    private static InteractionSpec getInteractionsSpec(
        Object object
    ) throws ServiceException {
        if(object instanceof RefObject) {
            RefPackage refPackage = ((RefObject)object).refOutermostPackage();
            if(refPackage instanceof RefPackage_1_2) {
                return ((RefPackage_1_2)refPackage).refInteractionSpec();
            }
        } else if (object instanceof ObjectView_1_0) {
            return ((ObjectView_1_0)object).getInteractionSpec();
        }
        return InteractionSpecs.NULL;
    }
    
    //-------------------------------------------------------------------------
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        return this.marshaller.unmarshal(source);
    }
    
    //-------------------------------------------------------------------------  
    public Object marshal(
        Object source
    ) throws ServiceException {
        if(
            this.persistenceManager != null && 
            JDOHelper.getPersistenceManager(source) == this.persistenceManager
        ) {
            return source;
        }
        InteractionSpec interactionSpec = getInteractionsSpec(source);
        RefRootPackage_1 refPackage = this.getRefPackage(interactionSpec);
        return (refPackage == this ? this.marshaller : refPackage).marshal(source);
    }

    //-------------------------------------------------------------------------  
    /**
     * Registers an object unless an object matching the unmarshalled object is
     * already registered.
     *
     * @param   unmarshalled
     *          the unmarshalled object
     * @param   marshalled
     *          the marshalled object
     *
     * @return  true if no object matching the unmarshalled object is registered
     *          yet
     */
    public boolean cacheObject(
        Object unmarshalled,
        Object marshalled
    ){
        return this.marshaller.cacheObject(unmarshalled,marshalled);
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
    public boolean evictObject(
        Object marshalled
    ){
        return this.marshaller.evictObject(marshalled);
    }
    
    //-------------------------------------------------------------------------
    // RefPackageFactory_1_3
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackageFactory_1_3#getUserObjects()
     */
    public Map<String, Object> getUserObjects() {
        return this.userObjects;
    }


    //-------------------------------------------------------------------------
    // RefPackage_1_6
    //-------------------------------------------------------------------------

    public RefObject refObject(
        Path objectId
    ) {
        try {
            return (RefObject) marshal(
                this.legacyDelegate ? refObjectFactory().getObjectById(objectId) : getDelegate().getObjectById(objectId)
            );
        } catch(ServiceException e) {
            throw new JmiServiceException(e);
        } catch(RuntimeServiceException e) {
            throw new JmiServiceException(e);
        } catch(JDOException e) {
            throw new JmiServiceException(e);
        }
    }
    
    
    //-------------------------------------------------------------------------
    // RefPackage_1_2
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_2#refContainer(org.openmdx.compatibility.base.naming.Path)
     */
    public RefContainer refContainer(
        Path resourceIdentifier,
        Class<Container<RefObject>> containerClass
    ) {
        try {
            RefContainer container;
            if(this.legacyDelegate) {
                DataObject_1_0 parent = (DataObject_1_0) this.manager.getObjectById(
                    resourceIdentifier.getParent()
                );
                container = new RefContainer_1(
                    this,
                    parent.objGetContainer(
                        resourceIdentifier.getBase()
                    )
                );
            } else {
                RefObject authority = (RefObject) this.standardDelegate.getObjectById(
                    resourceIdentifier.getPrefix(1)
                );
                RefPackage_1_2 delegate = (RefPackage_1_2) authority.refOutermostPackage();
                container = delegate.refContainer(resourceIdentifier, containerClass);
            }
            return Classes.newProxyInstance(
                new Jmi1ContainerInvocationHandler(container),
                containerClass == null ? Container.class : containerClass, RefContainer.class, Serializable.class
            );
        } catch(RuntimeServiceException e) {
            throw new JmiServiceException(e);
        } catch(JDOException e) {
            throw new JmiServiceException(e);
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    
    //-------------------------------------------------------------------------
    // RefPackage_1_0
    //-------------------------------------------------------------------------

    public RefObject refObject(
        String refMofId
    ) {
        return refMofId == null ? null : refObject(new Path(refMofId));
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
    public Model_1_6 refModel(
    ) {
        if(RefRootPackage_1.model == null) {
            RefRootPackage_1.model = Model_1Factory.getModel();
        }
        return RefRootPackage_1.model;
    }

    //-------------------------------------------------------------------------
    public PersistenceManager_1_0 refObjectFactory(
    ) {
        if(this.legacyDelegate) {
            return this.manager;
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
            this.unitOfWork = this.legacyDelegate ? 
                new LegacyUnitOfWork_1(
                    this.manager
                ) : 
                new JDOUnitOfWork_1(
                    this.standardDelegate
                );
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
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "Unsupported binding. Supported are " + Arrays.asList(Names.JMI1_PACKAGE_SUFFIX),
                            new BasicException.Parameter("binding.name", bindingPackageSuffix)
                        )
                    );
                }
                String jmi1PackageNameIntf = nestedPackageName.replace(':', '.') + "." + this.bindingPackageSuffix + "." + Identifier.CLASS_PROXY_NAME.toIdentifier(packageName) + "Package";
                refPackage = Classes.newProxyInstance(
                    new Jmi1PackageInvocationHandler(
                        nestedPackageName + ":" + packageName,
                        this, 
                        this
                    ),
                    Classes.getApplicationClass(loadedClassName = jmi1PackageNameIntf),
                    Jmi1Package_1_0.class
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
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "package class not found",
                    new BasicException.Parameter("class", loadedClassName)
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
        return new RefRootPackage_1(
            this.viewManager,
            this.interactionSpec,
            this.legacyDelegate,
            this.manager,
            this.standardDelegate,
            this.packageImpls,
            this.userObjects,
            this.userContext,
            this.persistenceManagerFactory,
            this.principals, this.accessor
        );
    }

    //-------------------------------------------------------------------------
    // Implements RefPackageFactory_1_1
    //-------------------------------------------------------------------------

    /**
     * Retrieve a context specific RefPackage
     * 
     * @param viewContext
     * 
     * @return a context specific RefPackage
     */
    public RefRootPackage_1 getRefPackage(
        InteractionSpec interactionSpec
    ){
        if(
            interactionSpec == InteractionSpecs.NULL ||
            this.interactionSpec == null ? interactionSpec == null : this.interactionSpec.equals(interactionSpec)
        ) {
            return this;
        }
        try {
            RefRootPackage_1 refPackage;
            if(this.viewManager == null) {
                this.viewManager = new ConcurrentHashMap<InteractionSpec,RefRootPackage_1>();
                this.viewManager.put(InteractionSpecs.NULL, this);
                refPackage = null;
            } else {
                refPackage = this.viewManager.get(
                    interactionSpec == null ? InteractionSpecs.NULL : interactionSpec
                );
            }
            if(refPackage == null) {
                RefRootPackage_1 oldPackage = this.viewManager.put(
                    interactionSpec,
                    refPackage = this.newRefPackage(interactionSpec) 
                );
                if(oldPackage != null) {
                    return oldPackage;
                }
            }
            return refPackage; 
        } catch (Exception exception) {
            throw new JmiServiceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "The given RefPackage is unable to create a view",
                    new BasicException.Parameter(
                        "objectFactory.class", 
                        this.manager == null ? "n/a" : this.manager.getClass().getName()
                    )
                )
            );
        }
    }

    /**
     * Create a context specific RefPackage
     * 
     * @param viewContext
     * 
     * @return a context specific RefPackage
     */
    protected RefRootPackage_1 newRefPackage(
        InteractionSpec viewContext
    ) {
        try {
            if(this.legacyDelegate) {
                PersistenceManager_1_0 legacyDelegate = ((PersistenceManagerFactory_1_0)this.manager.getPersistenceManagerFactory()).getPersistenceManager(viewContext);
                return new RefRootPackage_1(
                    this.viewManager,
                    viewContext,
                    this.legacyDelegate, 
                    legacyDelegate,
                    null, // standardDelegate
                    this.packageImpls,
                    this.userObjects, 
                    this.userContext,
                    this.persistenceManagerFactory,
                    this.principals, 
                    this.accessor
                );
            } 
            else {
                RefPackageFactory_1_0 delegateFactory = (RefPackageFactory_1_0) ((Delegating_1_0)this.standardDelegate).objGetDelegate();
                PersistenceManager_1 standardDelegate = (PersistenceManager_1) delegateFactory.getRefPackage(viewContext).refPersistenceManager();
                return new RefRootPackage_1(
                    this.viewManager,
                    viewContext,
                    this.legacyDelegate, 
                    null, // objectFactory,
                    standardDelegate, 
                    this.packageImpls,
                    this.userObjects, 
                    this.userContext,
                    this.persistenceManagerFactory,
                    this.principals, this.accessor
                );
            }
        }
        catch(Exception e) {
            throw new JmiServiceException(e);
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
            hasLegacyDelegate() ? this.manager : this.standardDelegate
        );
    }


    //-------------------------------------------------------------------------
    // Implements RefPackage_1_5
    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1#evictAll()
     */
    @Override
    public void evictAll(
    ) {
        if(this.legacyDelegate) {
            this.manager.evictAll();
        } 
        else {
            standardDelegate.evictAll();
        }
    }

    //-------------------------------------------------------------------------
    // Implements RefPackage_1_3
    //-------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1#close()
     */
    @Override
    public void close() {
        clear();
        if(hasLegacyDelegate()) {
            if(this.manager != null) try {
                this.manager.close();
            } catch (Exception exception) {
                SysLog.error("Close failure", exception);
            } finally {
                this.manager = null;
            }
        } else {
            if(this.standardDelegate != null) {
                this.standardDelegate.close();
                this.standardDelegate = null;
            }            
        }
    }

    /**
     * Retrieves the JDO Persistence Manager Factory.
     * 
     * @return the JDO Persistence Manager Factory configured according to this package
     */
    public PersistenceManagerFactory refPersistenceManagerFactory (
    ) {
        return this.persistenceManagerFactory; 
    }

    /**
     * Empty the cache
     */
    public void clear(
    ){
        this.marshaller.clear();
        if(this.legacyDelegate) {
            this.manager.clear();
        } 
        else {
            if(this.standardDelegate instanceof Delegating_1_0) {
                try {
                    Object delegate = ((Delegating_1_0)standardDelegate).objGetDelegate();
                    if(delegate instanceof Jmi1Package_1_0) {
                        ((Jmi1Package_1_0)delegate).clear();
                    }
                }
                catch(Exception e) {
                    throw new JmiServiceException(e);
                }
            }
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
    public InteractionSpec refInteractionSpec(
    ){
        return this.interactionSpec;
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
        if(this.persistenceManager == null) {
            this.persistenceManager = new PersistenceManager_1(
                refPersistenceManagerFactory(),
                new InstanceLifecycleNotifier(),
                this
            );
            if(this.userObjects != null) {
                for(Map.Entry<String,Object> userObject : this.userObjects.entrySet()) {
                    this.persistenceManager.putUserObject(
                        userObject.getKey(), 
                        userObject.getValue()
                    );
                }
                this.persistenceManager.putUserObject(
                    UserObjects.PRINCIPALS,
                    this.principals
                );
            }
        }
        return this.persistenceManager;
    }

    //-------------------------------------------------------------------------
    private static Constructor<?> newNullConstructor(){
        try {
            return Object.class.getConstructor();
        } catch (Exception exception) {
            SysLog.error(
                "Failed to acquire 'new Object()' aa null constructor, which will lead to RuntimeExceptions!",
                exception
            );
            return null;
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
                    String qualifiedElementName = (String)element.objGetValue("qualifiedName");
                    String qualifiedPackageName = qualifiedElementName.substring(0, qualifiedElementName.lastIndexOf(":"));
                    String elementName = (String)element.objGetValue("name");
                    if(
                        model.isClassType(element) && 
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
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_FOUND,
                            "model element for requested class not found",
                            new BasicException.Parameter("java-class", javaClassName),
                            new BasicException.Parameter("mof-package", objectPackageName),
                            new BasicException.Parameter("mof-class", objectClassName)
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
    // Implements RefPackage_1_6
    //-------------------------------------------------------------------------

    /**
     * Retrieve a class' mix-in interfaces
     * 
     * @param qualifiedClassName
     * 
     * @return the class' mix-in interfaces
     */
    public Set<Class<?>> getMixedInInterfaces(
        String qualifiedClassName 
    ) throws ServiceException {
        Set<Class<?>> mixedInInterfaces = this.mixedInInterfaces.get(qualifiedClassName);
        if(mixedInInterfaces == null) {
            mixedInInterfaces = new LinkedHashSet<Class<?>>();
            Constructor<?> mostDerivedConstructor = this.getUserImplConstructor(qualifiedClassName);
            if(mostDerivedConstructor != null) {
                mixedInInterfaces.addAll(
                    Classes.getInterfaces(mostDerivedConstructor.getDeclaringClass())
                );
            }
            Constructor<?> leastDerivedConstructor = this.getUserImplConstructor(
                this.refModel().getLeastDerived(qualifiedClassName)
            );
            if(leastDerivedConstructor != null) {
                mixedInInterfaces.addAll(
                    Classes.getInterfaces(leastDerivedConstructor.getDeclaringClass())
                );
            }
            if(this.standardDelegate instanceof PersistenceManager_1) {
                RefRootPackage_1 delegate = ((PersistenceManager_1)this.standardDelegate).objGetDelegate();
                mixedInInterfaces.addAll(
                    delegate.getMixedInInterfaces(qualifiedClassName)
                );
            }
            this.mixedInInterfaces.put(qualifiedClassName, mixedInInterfaces);
                
        }
        return mixedInInterfaces;
    }    
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1_6#getClassImplementingInterface(java.lang.String, java.lang.reflect.Method)
     */
    public String getClassImplementingInterface(
        String mostDerivedClassName,
        Class<?> declaringClass
    ) throws ServiceException {
        Constructor<?> mostDerivedConstructor = this.getUserImplConstructor(mostDerivedClassName);
        if(mostDerivedConstructor != null) {
            for(Class<?> candidate : mostDerivedConstructor.getDeclaringClass().getInterfaces()) {
                if(declaringClass.isAssignableFrom(candidate)) {
                    return mostDerivedClassName;
                }
            }
        }
        String leastDerivedClassName = this.refModel().getLeastDerived(mostDerivedClassName);
        Constructor<?> leastDerivedConstructor = this.getUserImplConstructor(leastDerivedClassName);
        if(leastDerivedConstructor != null && leastDerivedConstructor != mostDerivedConstructor) {
            for(Class<?> candidate : leastDerivedConstructor.getDeclaringClass().getInterfaces()) {
                if(declaringClass.isAssignableFrom(candidate)) {
                    return leastDerivedClassName;
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1_6#isAccessor()
     */
    public boolean isAccessor() {
        return this.accessor;
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


    //-------------------------------------------------------------------------
    // Class InaccessibleObject
    //-------------------------------------------------------------------------

    /**
     * InaccessibleObject
     */
    class InaccessibleObject implements RefObject {

        /**
         * Constructor 
         */
        InaccessibleObject(
            Exception e
        ) {
            this.cause = new InvalidObjectException(
                this,
                e.getMessage()
            );
            BasicException basic = BasicException.toStackedException(e, this.cause);
            this.cause.initCause(basic);
            String path = basic.getParameter("path");
            Path objectId;
            if(path == null) {
                objectId = null;
            } else try {
                objectId = new Path(path);
            } catch (RuntimeException exception) {
                objectId = null;
            }
            this.objectId = objectId;
        }

        private Path objectId;

        private final InvalidObjectException cause;

        InvalidObjectException getException(
        ){
            return this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refClass()
         */
        public RefClass refClass(
        ) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refDelete()
         */
        public void refDelete() {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refImmediateComposite()
         */
        public RefFeatured refImmediateComposite() {
            if(this.objectId == null) {
                throw this.cause;
            } else {
                int s = this.objectId.size() - 2;
                return s > 0 ? 
                    (RefFeatured)refPersistenceManager().getObjectById(this.objectId.getPrefix(s)) : 
                        null;
            }
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refIsInstanceOf(javax.jmi.reflect.RefObject, boolean)
         */
        public boolean refIsInstanceOf(
            RefObject objType, 
            boolean considerSubtypes
        ) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refOutermostComposite()
         */
        public RefFeatured refOutermostComposite() {
            if(this.objectId == null) {
                throw this.cause;
            } else {
                return this.objectId.size() > 2 ?
                    (RefFeatured)refPersistenceManager().getObjectById(this.objectId.getPrefix(1)) : 
                        null;
            }
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refGetValue(javax.jmi.reflect.RefObject)
         */
        public Object refGetValue(RefObject feature) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refGetValue(java.lang.String)
         */
        public Object refGetValue(String featureName) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refInvokeOperation(javax.jmi.reflect.RefObject, java.util.List)
         */
        @SuppressWarnings("unchecked")
        public Object refInvokeOperation(
            RefObject requestedOperation, 
            List args
        ) throws RefException {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refInvokeOperation(java.lang.String, java.util.List)
         */
        @SuppressWarnings("unchecked")
        public Object refInvokeOperation(
            String requestedOperation, 
            List args
        ) throws RefException {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refSetValue(javax.jmi.reflect.RefObject, java.lang.Object)
         */
        public void refSetValue(RefObject feature, Object value) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refSetValue(java.lang.String, java.lang.Object)
         */
        public void refSetValue(String featureName, Object value) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refImmediatePackage()
         */
        public RefPackage refImmediatePackage() {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refMetaObject()
         */
        public RefObject refMetaObject() {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refMofId()
         */
        public String refMofId() {
            return this.objectId.toXri();
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refOutermostPackage()
         */
        public RefPackage refOutermostPackage() {
            return RefRootPackage_1.this;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refVerifyConstraints(boolean)
         */
        @SuppressWarnings("unchecked")
        public Collection refVerifyConstraints(boolean deepVerify) {
            throw this.cause;
        }

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
                this.mapping = new ConcurrentHashMap<Method,Method>();
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
         */
        RefObject_1Marshaller(
        ) {
            super();
        }

        private static final long serialVersionUID = -6856573596590312011L;

        //-----------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.marshalling.CachingMarshaller#cache(java.lang.Object, java.lang.Object)
         */
        public boolean cacheObject(Object unmarshalled, Object marshalled) {
            if(marshalled instanceof RefObject_1_0) {
                RefPackage refPackage = ((RefObject)marshalled).refOutermostPackage();
                if(
                        RefRootPackage_1.this != refPackage &&
                        refPackage instanceof CachingMarshaller_1_0
                ){
                    return ((CachingMarshaller_1_0)refPackage).cacheObject(unmarshalled, marshalled);
                }
            }
            return super.cacheObject(unmarshalled, marshalled);
        }

        //-----------------------------------------------------------------------
        public Object unmarshal(
            Object source
        ) throws ServiceException {
            if(RefRootPackage_1.this.legacyDelegate){
                if(source instanceof Jmi1ObjectInvocationHandler.DelegatingRefObject) {
                    Object object = source;
                    while(object instanceof Jmi1ObjectInvocationHandler.DelegatingRefObject) {
                        object = ((Jmi1ObjectInvocationHandler.DelegatingRefObject)object).openmdxjdoGetDelegate();
                    }
                    return ((RefObject_1_0)object).refDelegate();
                } else if(source instanceof RefObject_1_0) {
                    return ((RefObject_1_0)source).refDelegate();
                }
            } else {
                if(source instanceof Jmi1ObjectInvocationHandler.DelegatingRefObject) {
                    return ((Jmi1ObjectInvocationHandler.DelegatingRefObject)source).openmdxjdoGetDelegate();
                }
            }
            return source;
        }

        //-----------------------------------------------------------------------  
        protected Object createMarshalledObject(
            Object source
        ) throws ServiceException {
            if(source instanceof PersistenceCapable) {
                String className;
                if(RefRootPackage_1.this.legacyDelegate) {
                    className = ((DataObject_1_0)source).objGetClass();
                    if(className == null){
                        if(refInteractionSpec() == null) throw new ServiceException(
                          BasicException.Code.DEFAULT_DOMAIN,
                          BasicException.Code.NOT_FOUND,
                          "Object class can not be determined",
                          new BasicException.Parameter(
                              "resourceIdentifier", 
                              PersistenceHelper.getCurrentObjectId(source)
                          ),
                          new BasicException.Parameter(
                              "interactionSpec"
                          )
                        );
                        return null;
                    }
                } else {
                    className = getMofClassName(
                        source.getClass().getInterfaces()[0].getName(),
                        RefRootPackage_1.this.refModel()
                    );
                }
                return RefRootPackage_1.this.refClass(className).refCreateInstance(
                    Collections.singletonList(source)
                );
            } else {
                return source;
            }
        }

        //-----------------------------------------------------------------------  
        public void clear() {
            super.clear();
        }

        //-----------------------------------------------------------------------  
        Object remove(
            Object unmarshalled
        ){
            return super.mapping.remove(unmarshalled);
        }

    }

    //------------------------------------------------------------------------
    // Class PersistenceManagerFactory_1
    //------------------------------------------------------------------------
    
    /**
     * PersistenceManagerFactory_1
     */
    static class CompatibilityPersistenceManagerFactory
        extends AbstractPersistenceManagerFactory_1
    {

        /**
         * Constructor 
         *
         * @param configuration
         */
        CompatibilityPersistenceManagerFactory(
            PersistenceManager_1_0 objectFactory,
            Map<String,String> packageImpls,
            Object context,
            String bindingPackageSuffix
        ) {
            super(
                bindingPackageSuffix == null ? Collections.EMPTY_MAP : Collections.singletonMap(
                    ConfigurableProperty.BindingPackageSuffix, 
                    bindingPackageSuffix
                )
            );
            this.objectFactory = objectFactory;
            this.packageImpls = packageImpls;
            this.context = context;
        }
        
        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 1935043108080407782L;

        /**
         * 
         */
        private final PersistenceManager_1_0 objectFactory;
        
        /**
         * 
         */
        private final Map<String,String> packageImpls;
        
        /**
         * 
         */
        private final Object context;
       
        
        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.AbstractManagerFactory#newManager(javax.security.auth.Subject)
         */
        @Override
        protected PersistenceManager newManager(
            Subject subject
        ) { 
            throw new JDOFatalUserException("Re-authentication is not supported");
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.AbstractManagerFactory#newManager()
         */
        @Override
        protected PersistenceManager newManager(
        ) {
            return new RefRootPackage_1(
                null, // lazy instantiation of viewManager
                null, // viewContext
                true, // legacyDelegate
                this.objectFactory,
                null, // standardDelegate
                this.packageImpls,
                null, // userObjects
                this.context, // context
                this, // persistenceManagerFactory
                null, // principals
                false // accessor
            ).refPersistenceManager();
        }

    }
    
    /**
     * JDOUnitOfWork_1
     */
    class JDOUnitOfWork_1 implements UnitOfWork_1_0 {

        /**
         * Constructor 
         *
         * @param persistenceManager
         */
        JDOUnitOfWork_1(
            PersistenceManager persistenceManager
        ) {
            this.persistenceManager = persistenceManager;
        }

        private final PersistenceManager persistenceManager;

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#begin()
         */
        public void begin(
        ) {
            this.persistenceManager.currentTransaction().begin();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#commit()
         */
        public void commit(
        ) {
            this.persistenceManager.currentTransaction().commit();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isActive()
         */
        public boolean isActive(
        ) {
            return this.persistenceManager.currentTransaction().isActive();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isOptimistic()
         */
        public boolean getOptimistic(
        ) {
            return this.persistenceManager.currentTransaction().getOptimistic();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isTransactional()
         */
        public boolean isTransactional(
        ) {
            return !this.persistenceManager.currentTransaction().getNontransactionalWrite();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#rollback()
         */
        public void rollback(
        ) {
            this.persistenceManager.currentTransaction().rollback();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.Synchronization_1_0#afterBegin()
         */
        public void afterBegin(
        ) throws ServiceException {
            this.getSynchronization();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.Synchronization_1_0#afterCompletion(boolean)
         */
        public void afterCompletion(
            boolean committed
        ) throws ServiceException {
            try {
                this.getSynchronization().afterCompletion(
                    committed ? Status.STATUS_COMMITTED : Status.STATUS_ROLLEDBACK
                );
            } 
            catch (Exception exception) {
                throw new ServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.Synchronization_1_0#beforeCompletion()
         */
        public void beforeCompletion(
        ) throws ServiceException {
            try {
                this.getSynchronization().beforeCompletion();
            } 
            catch(Exception exception) {
                throw new ServiceException(exception);
            }
        }

        public Synchronization getSynchronization(
        ) {
            return this.persistenceManager.currentTransaction().getSynchronization();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isContainerManaged()
         */
        public boolean isContainerManaged(
        ) {
            throw new UnsupportedOperationException("Operation not supported by JDOUnitOfWork_1");
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_2#getRollbckOnly()
         */
        public boolean getRollbackOnly(
        ) {
            return this.persistenceManager.currentTransaction().getRollbackOnly();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_2#setRollbackOnly()
         */
        public void setRollbackOnly(
        ) {
            this.persistenceManager.currentTransaction().setRollbackOnly();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getNontransactionalRead()
         */
        public boolean getNontransactionalRead(
        ) {
            return this.persistenceManager.currentTransaction().getNontransactionalRead();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getNontransactionalWrite()
         */
        public boolean getNontransactionalWrite(
        ) {
            return this.persistenceManager.currentTransaction().getNontransactionalWrite();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getPersistenceManager()
         */
        public PersistenceManager getPersistenceManager(
        ) {
            return this.persistenceManager.currentTransaction().getPersistenceManager();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getRestoreValues()
         */
        public boolean getRestoreValues(
        ) {
            return this.persistenceManager.currentTransaction().getRestoreValues();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getRetainValues()
         */
        public boolean getRetainValues(
        ) {
            return this.persistenceManager.currentTransaction().getRetainValues();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setNontransactionalRead(boolean)
         */
        public void setNontransactionalRead(
            boolean nontransactionalRead
        ) {
            this.persistenceManager.currentTransaction().setNontransactionalRead(nontransactionalRead);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setNontransactionalWrite(boolean)
         */
        public void setNontransactionalWrite(
            boolean nontransactionalWrite
        ) {
            this.persistenceManager.currentTransaction().setNontransactionalWrite(nontransactionalWrite);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setOptimistic(boolean)
         */
        public void setOptimistic(
            boolean optimistic
        ) {
            this.persistenceManager.currentTransaction().setOptimistic(optimistic);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setRestoreValues(boolean)
         */
        public void setRestoreValues(
            boolean restoreValues
        ) {
            this.persistenceManager.currentTransaction().setRestoreValues(restoreValues);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setRetainValues(boolean)
         */
        public void setRetainValues(
            boolean retainValues
        ) {
            this.persistenceManager.currentTransaction().setRetainValues(retainValues);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setSynchronization(javax.transaction.Synchronization)
         */
        public void setSynchronization(
            Synchronization sync
        ) {
            this.persistenceManager.currentTransaction().setSynchronization(sync);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getIsolationLevel()
         */
        public String getIsolationLevel() {
            throw new UnsupportedOperationException("Operation not supported by JDOUnitOfWork_1");
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setIsolationLevel(java.lang.String)
         */
        public void setIsolationLevel(String arg0) {
            throw new UnsupportedOperationException("Operation not supported by JDOUnitOfWork_1");
        }
        
    }
    
    /**
     * LegacyUnitOfWork_1
     *
     */
    public class LegacyUnitOfWork_1
        implements UnitOfWork_1_0
    {

        /**
         * Constructor 
         *
         * @param delegate
         */
        public LegacyUnitOfWork_1(
            PersistenceManager_1_0 delegate
        ){
            this.delegate = delegate;
        }

        /**
         * 
         */
        private final PersistenceManager_1_0 delegate;

        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#begin()
         */
        public void begin(
        ) {
            this.delegate.currentTransaction().begin();
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#commit()
         */
        public void commit(
        ) {
            this.delegate.currentTransaction().commit();
        }

        /**
         * @return
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isActive()
         */
        public boolean isActive(
        ) {
            return this.delegate.currentTransaction().isActive();
        }

        /**
         * @return
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#getOptimistic()
         */
        public boolean getOptimistic(
        ) {
            return this.delegate.currentTransaction().getOptimistic();
        }

        /**
         * @return
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isTransactional()
         */
        public boolean isTransactional(
        ) {
            try {
                UnitOfWork_1_0 uow = (UnitOfWork_1_0)this.delegate.currentTransaction();
                if(uow.isContainerManaged()) {
                    try {
                        return uow.isTransactional();
                    } 
                    catch (Exception exception) {
                        return false;
                    }
                }
                else {
                    return true;
                }
            }
            catch (Exception exception) {
                return false;
            }
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#rollback()
         */
        public void rollback(
        ) {
            this.delegate.currentTransaction().rollback();
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.Synchronization_1_0#afterBegin()
         */
        public Synchronization getSynchronization(
        ) {
            return this.delegate.currentTransaction().getSynchronization();
        }

        /**
         * @return
         * @see org.openmdx.base.transaction.UnitOfWork_1_2#getRollbackOnly()
         */
        public boolean getRollbackOnly(
        ) {
            return this.delegate.currentTransaction().getRollbackOnly();
        }

        /**
         * 
         * @throws ServiceException 
         * @see org.openmdx.base.transaction.UnitOfWork_1_2#setRollbackOnly()
         */
        public void setRollbackOnly(
        ) {
            this.delegate.currentTransaction().setRollbackOnly();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isContainerManaged()
         */
        public boolean isContainerManaged(
        ) throws ServiceException {
            UnitOfWork_1_0 uow = (UnitOfWork_1_0)this.delegate.currentTransaction();
            return uow.isContainerManaged();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getNontransactionalRead()
         */
        public boolean getNontransactionalRead(
        ) {
            return this.delegate.currentTransaction().getNontransactionalRead();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getNontransactionalWrite()
         */
        public boolean getNontransactionalWrite(
        ) {
            return this.delegate.currentTransaction().getNontransactionalWrite();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getPersistenceManager()
         */
        public PersistenceManager getPersistenceManager(
        ) {
            return this.delegate.currentTransaction().getPersistenceManager();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getRestoreValues()
         */
        public boolean getRestoreValues(
        ) {
            return this.delegate.currentTransaction().getRestoreValues();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getRetainValues()
         */
        public boolean getRetainValues(
        ) {
            return this.delegate.currentTransaction().getRetainValues();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setNontransactionalRead(boolean)
         */
        public void setNontransactionalRead(
            boolean nontransactionalRead
        ) {
            this.delegate.currentTransaction().setNontransactionalRead(nontransactionalRead);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setNontransactionalWrite(boolean)
         */
        public void setNontransactionalWrite(
            boolean nontransactionalWrite
        ) {
            this.delegate.currentTransaction().setNontransactionalWrite(nontransactionalWrite);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setOptimistic(boolean)
         */
        public void setOptimistic(
            boolean optimistic
        ) {
            this.delegate.currentTransaction().setOptimistic(optimistic);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setRestoreValues(boolean)
         */
        public void setRestoreValues(
            boolean restoreValues
        ) {
            this.delegate.currentTransaction().setRestoreValues(restoreValues);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setRetainValues(boolean)
         */
        public void setRetainValues(
            boolean retainValues
        ) {
            this.delegate.currentTransaction().setRetainValues(retainValues);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setSynchronization(javax.transaction.Synchronization)
         */
        public void setSynchronization(
            Synchronization sync
        ) {
            this.delegate.currentTransaction().setSynchronization(sync);
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getIsolationLevel()
         */
        public String getIsolationLevel() {
            throw new UnsupportedOperationException("Operation not supported by JDOUnitOfWork_1");
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setIsolationLevel(java.lang.String)
         */
        public void setIsolationLevel(String arg0) {
            throw new UnsupportedOperationException("Operation not supported by JDOUnitOfWork_1");
        }
        
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

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6682037424274032056L;

    /**
     * An empty proncipal set
     */
    private static final Set<Principal> NO_PRINCIPALS = Collections.emptySet();
    
    /**
     * Defines, whether objectFactory or standardDelegate is valid.
     */
    final boolean legacyDelegate;

    /**
     * Defines, whether instance callbacks should be registered
     */
    final boolean accessor;
    
    /**
     * Contains the legacy delegate
     */
    private PersistenceManager_1_0 manager;

    /**
     * Contains the standard delegate
     */
    private PersistenceManager standardDelegate;

    private final InteractionSpec interactionSpec;
    private final Object userContext;
    protected ConcurrentMap<InteractionSpec,RefRootPackage_1> viewManager;
    private final RefObject_1Marshaller marshaller;
    private final PersistenceManagerFactory persistenceManagerFactory;
    private transient PersistenceManager persistenceManager;
    private final Set<? extends Principal> principals;

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
     * The user objects have to be propagated to a <code>PersistenceManager</code>
     */
    private final Map<String,Object> userObjects;

    /**
     * defaultLocationSuffix. The default location for implementation lookup is constructed
     * as <qualified model name as java package name>.<suffix>.<interface or
     * class name>.
     */
    private final String bindingPackageSuffix;

    /**
     * Holds the union of models of the loaded sub-packages.
     */
    private static Model_1_6 model = null;

    private UnitOfWork_1_0 unitOfWork = null;

    protected final Map<String,Constructor<?>> userImplConstructors = new ConcurrentHashMap<String,Constructor<?>>();
    protected final Map<String,Set<Class<?>>> mixedInInterfaces = new ConcurrentHashMap<String,Set<Class<?>>>();
    
    /**
     * Holds the mapping of java proxy names to MOF qualified class names.
     */
    protected static final Map<String,String> classNames = new ConcurrentHashMap<String,String>();

    private static final Constructor<?> NULL_CONSTRUCTOR = newNullConstructor();
    
}

//--- End of File -----------------------------------------------------------
