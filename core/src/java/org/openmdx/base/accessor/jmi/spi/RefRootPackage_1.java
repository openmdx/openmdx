/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RefRootPackage_1.java,v 1.165 2009/06/09 12:45:17 hburger Exp $
 * Description: RefRootPackage_1 class
 * Revision:    $Revision: 1.165 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:17 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.InvalidObjectException;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.oasisopen.jmi1.RefContainer;
import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.spi.PersistenceManagerFactory_1_0;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.CachingMarshaller;
import org.openmdx.base.marshalling.CachingMarshaller_1_0;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.spi.AspectObjectAcessor;
import org.openmdx.base.persistence.spi.InstanceLifecycleNotifier;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
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
    implements CachingMarshaller_1_0, ExceptionListener 
{

    /**
     * Constructor 
     *
     * @param viewManager
     * @param delegate 
     * @param packageImpls
     * @param userObjects
     * @param userContext
     * @param persistenceManagerFactory
     * @param accessor 
     * @param principals 
     * @param viewContext
     */
    RefRootPackage_1(
        ConcurrentMap<InteractionSpec,RefRootPackage_1> viewManager,
        InteractionSpec interactionSpec,
        PersistenceManager delegate,
        Map<String,String>  packageImpls,
        Map<String, Object> userObjects,
        Object userContext,
        PersistenceManagerFactory persistenceManagerFactory,
        List<String> principalChain
    ) {
        super(
            null, // outermostPackage
            null // immediatePackage 
        );
        this.viewManager = viewManager;
        this.interactionSpec = interactionSpec;
        this.delegate = (PersistenceManager_1_0)delegate;
        this.packageImpls = packageImpls;
        this.userObjects = userObjects;
        this.userContext = userContext;
        String bindingPackageSuffix = persistenceManagerFactory instanceof PersistenceManagerFactory_1_0 ?
            ((PersistenceManagerFactory_1_0)persistenceManagerFactory).getBindingPackageSuffix() :
            null;
        this.bindingPackageSuffix = bindingPackageSuffix == null ? Names.JMI1_PACKAGE_SUFFIX : bindingPackageSuffix;
        this.loadedPackages = new ConcurrentHashMap<String,RefPackage>();
        this.marshaller = new RefObject_1Marshaller();
        this.persistenceManager = null; // Lazy initialization
        this.persistenceManagerFactory = persistenceManagerFactory;
        this.principalChain = principalChain == null ? NO_PRINCIPALS : principalChain;
        SysLog.detail("configured package implementations", this.packageImpls);
    }

    /**
     * Constructor 
     *
     * @param legacyDelegate
     * @param persistenceManagerFactory
     * @param standardDelegate
     * @param packageImpls
     * @param userObjects
     * @param principalChain
     */
    public RefRootPackage_1(
        PersistenceManagerFactory persistenceManagerFactory,
        PersistenceManager standardDelegate,
        Map<String,String> packageImpls,
        Map<String,Object> userObjects,
        List<String> principalChain
    ){
        this(
            null, // viewManager
            null, // viewContext
            standardDelegate,
            packageImpls, // objectFactory
            userObjects, // standardDelegate
            null, 
            persistenceManagerFactory, // context
            principalChain
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
        DataObjectManager_1_0 objectFactory,
        boolean isFinal
    ) {
        this(
            null, // lazy instantiation of viewManager
            null, // viewContext
            objectFactory, // delegate
            null,
            null,
            null, // packageImpls,
            new RefRootPackagePersistenceManagerFactory(
                objectFactory,
                null, // packageImpls
                null, // context
                Names.JMI1_PACKAGE_SUFFIX,
                isFinal
            ), // userObjects
            null // context
        );
    }
        
    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.CallbackRegistry#registerObjectById(java.lang.Object)
     */
    void registerCallbacks(
        RefObject_1_0 instance
    ) {
        if(this.registerCallbacks()) {
            for(InstanceCallbackAdapter_1 adapter : instance.refGetEventListeners(InstanceCallbackAdapter_1.class)) {
                if(adapter.getInstance() == instance) {
                    return;
                }
            }
            instance.refAddEventListener(
                InstanceCallbackAdapter_1.newInstance(instance)
            );
        }
    }

    //-------------------------------------------------------------------------
    public String refImplPackageName(
        String packageName
    ) {
        return this.packageImpls == null ? 
            null : 
            (String)this.packageImpls.get(packageName);
    }

    //-------------------------------------------------------------------------
    public String refBindingPackageSuffix(
    ) {
        return this.bindingPackageSuffix;
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
                    SysLog.detail("No user impl found", Arrays.asList(implPackageName, className));
                }                                    
            }
            if(userClass == null) {
                this.userImplConstructors.putIfAbsent(qualifiedClassName, NULL_CONSTRUCTOR);
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
                            Classes.getApplicationClass(bindingQualifiedInterfaceName),
                            Classes.getApplicationClass(cci2QualifiedInterfaceName)
                        );
                    }
                    catch(ClassNotFoundException e) {
                        SysLog.warning("Required interfaces not found on class path", Arrays.asList(bindingQualifiedInterfaceName, cci2QualifiedInterfaceName));
                    }
                }
                catch(NoSuchMethodException e) {
                    SysLog.detail("Missing constructor in user implementation. Trying fallback.", Arrays.asList(bindingQualifiedInterfaceName, cci2QualifiedInterfaceName));
                    try {
                        userImplConstructor = userClass.getConstructor(
                            Classes.getApplicationClass(cci2QualifiedInterfaceName),
                            Classes.getApplicationClass(cci2QualifiedInterfaceName)
                        );
                    }
                    catch(NoSuchMethodException e0) {
                        SysLog.warning("Missing constructor in user implementation", Arrays.asList(cci2QualifiedInterfaceName, cci2QualifiedInterfaceName));
                    }
                    catch(ClassNotFoundException e0) {
                        SysLog.warning("Required interface not found on class path", cci2QualifiedInterfaceName);
                    }
                }       
                Constructor<?> concurrent = this.userImplConstructors.putIfAbsent(
                    qualifiedClassName, 
                    userImplConstructor == null ? NULL_CONSTRUCTOR : userImplConstructor
                );
                if(concurrent != null) {
                    userImplConstructor = concurrent;
                }
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
        Jmi1Object_1_0 refDelegate,
        Object self,
        Object next
    ) {
        Constructor<?> userImplConstructor = getUserImplConstructor(qualifiedClassName);
        if(userImplConstructor == null) {
            return null;
        }
        try {
            return this.isTerminal() ? 
                userImplConstructor.newInstance(
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
                ) : 
                userImplConstructor.newInstance(
                    self,
                    next
                );
        } 
        catch(Exception e) {
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
        return 
            object instanceof RefObject ? ((RefRootPackage_1) ((RefObject)object).refOutermostPackage()).refInteractionSpec() :
            object instanceof ObjectView_1_0 ? ((ObjectView_1_0)object).getInteractionSpec() :
            InteractionSpecs.NULL;
    }
    
    //-------------------------------------------------------------------------
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        return 
            source instanceof RefStruct_1_0 ? ((RefStruct_1_0)source).refDelegate() : 
            this.marshaller.unmarshal(source);
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
        if(source instanceof RefStruct_1_0) {
            return this.refCreateStruct(((RefStruct_1_0)source).refDelegate());
        } else if (source instanceof Record) {
            return this.refCreateStruct((Record)source);
        }
        InteractionSpec interactionSpec = getInteractionsSpec(source);
        RefRootPackage_1 refPackage = this.refPackage(interactionSpec);
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
            return (RefObject)this.marshal(
                this.refDelegate().getObjectById(objectId)
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
    // RefPackage_1_0
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_0#refContainer(org.openmdx.compatibility.base.naming.Path)
     */
    public RefContainer refContainer(
        Path resourceIdentifier,
        Class<Container<RefObject>> containerClass
    ) {
        try {
            if(this.isTerminal()) {
                DataObject_1_0 parent = (DataObject_1_0) this.delegate.getObjectById(
                    resourceIdentifier.getParent()
                );
                RefContainer  container = new RefContainer_1(
                    this,
                    parent.objGetContainer(
                        resourceIdentifier.getBase()
                    )
                );
                return (RefContainer) Classes.newProxyInstance(
                    new Jmi1ContainerInvocationHandler(null, container),
                    containerClass == null ? Container.class : containerClass, 
                    RefContainer.class, 
                    Serializable.class
                );
            } 
            else {
                RefObject authority = (RefObject) this.delegate.getObjectById(
                    resourceIdentifier.getPrefix(1)
                );
                RefPackage_1_0 delegate = (RefPackage_1_0) authority.refOutermostPackage();
                RefContainer container = delegate.refContainer(resourceIdentifier, containerClass);
                return (RefContainer) Classes.newProxyInstance(
                    new Jmi1ContainerInvocationHandler(
                        new Jmi1ObjectInvocationHandler.StandardMarshaller(this),
                        container
                    ),
                    containerClass == null ? Container.class : containerClass, 
                    RefContainer.class, 
                    Serializable.class
                );
            }
        } 
        catch(RuntimeServiceException e) {
            throw new JmiServiceException(e);
        } 
        catch(JDOException e) {
            throw new JmiServiceException(e);
        } 
        catch (ServiceException exception) {
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
    public Model_1_0 refModel(
    ) {
        if(RefRootPackage_1.model == null) {
            RefRootPackage_1.model = Model_1Factory.getModel();
        }
        return RefRootPackage_1.model;
    }

    //-------------------------------------------------------------------------
    // RefBaseObject
    //-------------------------------------------------------------------------
    public RefPackage refImmediatePackage(
    ) {
        return this;
    }

    //-------------------------------------------------------------------------
    public RefRootPackage_1 refOutermostPackage(
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
                refPackage = (RefPackage) Classes.newProxyInstance(
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
    public String refMofId(
    ) {
        return null;
    }

    //-------------------------------------------------------------------------
    // Implements RefPackageFactory_1_1
    //-------------------------------------------------------------------------

    /**
     * Create a structure proxy without accessing the delegate
     * 
     * @param structName
     * @param delegate
     * 
     * @return the structure proxy without accessing the delegate
     */
    public RefStruct refCreateStruct(
        String structName,
        MappedRecord delegate
    ) {
        int simpleNamePosition = structName.lastIndexOf(':') + 1;
        String packagePrefix = structName.substring(0, simpleNamePosition).replace(':', '.');
        String className =  Identifier.CLASS_PROXY_NAME.toIdentifier(structName.substring(simpleNamePosition));
        String qualifiedStructClassName = packagePrefix + refOutermostPackage().refBindingPackageSuffix() + '.' + className;                         
        try {
            return (RefStruct) Classes.newProxyInstance(
                new Jmi1StructInvocationHandler(
                    refOutermostPackage(),
                    delegate
                ),
                Classes.<Object>getApplicationClass(qualifiedStructClassName)
            );
        } catch (ClassNotFoundException exception) {
            throw new JmiServiceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Struct construction failure",
                    new BasicException.Parameter("structName", structName)
                )
            );
        }
    }
    
    
    /**
     * Retrieve a context specific RefPackage
     * 
     * @param viewContext
     * 
     * @return a context specific RefPackage
     */
    public RefRootPackage_1 refPackage(
        InteractionSpec interactionSpec
    ){
        if(
            interactionSpec == InteractionSpecs.NULL ||
            (this.interactionSpec == null ? interactionSpec == null : this.interactionSpec.equals(interactionSpec))
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
                        this.delegate == null ? "<null>" : this.delegate.getClass().getName()
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
    @SuppressWarnings("unchecked")
    protected RefRootPackage_1 newRefPackage(
        InteractionSpec viewContext
    ) {
        try {
            PersistenceManager_1_0 delegate = this.isTerminal() ?
                this.delegate :
                ((Delegating_1_0<RefPackage_1_0>)this.delegate).objGetDelegate().refPersistenceManager();
            return new RefRootPackage_1(
                this.viewManager,
                viewContext,
                delegate.getPersistenceManager(viewContext), 
                this.packageImpls,
                this.userObjects,
                this.userContext, 
                this.persistenceManagerFactory,
                this.principalChain
            );
        } 
        catch(Exception e) {
            throw new JmiServiceException(e);
        }
    }
    
    //-------------------------------------------------------------------------
    // Implements RefPackage_1_0
    //-------------------------------------------------------------------------

    /**
     * Retrieve the delegate
     * 
     * @return either the legacy or the standard delegate
     */
    public PersistenceManager_1_0 refDelegate(){
        return this.delegate;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1#refLegacyDelegate()
     */
    @Override
    public boolean isTerminal(
    ) {
        return this.delegate instanceof DataObjectManager_1_0;
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
        return this.refCreateImpl(
            qualifiedClassName,
            null, // refDelegate
            self,
            next
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1#close()
     */
    @Override
    public void close(
    ) {
        if(this.delegate != null) {
            this.marshaller.clear();
            this.delegate.close();
            this.delegate = null;
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


    //-------------------------------------------------------------------------
    // Implements RefPackage_1_0
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
    // Implements RefPackage_1_0
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    /**
     * Retrieves the JDO Persistence Manager delegating to this package.
     * 
     * @return the JDO Persistence Manager delegating to this package.
     */
    public PersistenceManager_1_0 refPersistenceManager(
    ) {
        if(this.persistenceManager == null) {
            this.persistenceManager = new PersistenceManager_1(
                refPersistenceManagerFactory(),
                new InstanceLifecycleNotifier(),
                this
            );
            if(this.userContext != null) {
                this.persistenceManager.setUserObject(this.userContext);
            }
            if(this.userObjects != null) {
                for(Map.Entry<String,Object> userObject : this.userObjects.entrySet()) {
                    this.persistenceManager.putUserObject(
                        userObject.getKey(), 
                        userObject.getValue()
                    );
                }
                this.persistenceManager.putUserObject(
                    Principal[].class,
                    this.principalChain
                );
                Object aspectObjectAccessor = this.delegate.getUserObject(
                    AspectObjectAcessor.class
                );
                if(aspectObjectAccessor != null) {
                    this.persistenceManager.putUserObject(
                        AspectObjectAcessor.class, 
                        aspectObjectAccessor
                    );
                }
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
            if(this.delegate instanceof PersistenceManager_1) {
                RefRootPackage_1 delegate = ((PersistenceManager_1)this.delegate).objGetDelegate();
                mixedInInterfaces.addAll(
                    delegate.getMixedInInterfaces(qualifiedClassName)
                );
            }
            Set<Class<?>> concurrent = this.mixedInInterfaces.putIfAbsent(qualifiedClassName, mixedInInterfaces);
            if(concurrent != null) {
                mixedInInterfaces = concurrent;
            }
                
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
    public boolean registerCallbacks(
    ) {
        return InteractionSpecs.isNull(interactionSpec);
    }
    
    //-------------------------------------------------------------------------
    // Implements RefPackage_1_0
    //-------------------------------------------------------------------------

    //-----------------------------------------------------------------------
    public Object refCreateImpl(
        String qualifiedClassName,
        Jmi1Object_1_0 refDelegate
    ) {
        return this.refCreateImpl(
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
            BasicException basic = BasicException.toExceptionStack(e);
            this.cause = Throwables.initCause(
                new InvalidObjectException(
                    this,
                    e.getMessage()
                ),
                e,
                basic.getExceptionDomain(),
                basic.getExceptionCode()
            );
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
            return this.objectId.toXRI();
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
            if(RefRootPackage_1.this.isTerminal()){
                if(source instanceof DelegatingRefObject_1_0) {
                    throw new UnsupportedOperationException("Terminal RefPackage can not unmarshal DelegatingObjet_1_0");
                } 
                else if(source instanceof RefObject_1_0) {
                    return ((RefObject_1_0)source).refDelegate();
                }
            } 
            else {
                if(source instanceof DelegatingRefObject_1_0) {
                    return ((DelegatingRefObject_1_0)source).openmdxjdoGetDelegate();
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
                if(RefRootPackage_1.this.isTerminal()) {
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
    // Class RefRootPackagePersistenceManagerFactory
    //------------------------------------------------------------------------
    
    static class RefRootPackagePersistenceManagerFactory
        extends AbstractPersistenceManagerFactory_1 {

        RefRootPackagePersistenceManagerFactory(
            DataObjectManager_1_0 objectFactory,
            Map<String,String> packageImpls,
            Object context,
            String bindingPackageSuffix,
            boolean isFinal
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
        
        @Override
        protected PersistenceManager newManager(
            List<String> principalChain
        ) { 
            throw new JDOFatalUserException("Re-authentication is not supported");
        }

        @Override
        protected PersistenceManager newManager(
        ) {
            return new RefRootPackage_1(
                null, // lazy instantiation of viewManager
                null, // viewContext
                this.objectFactory, // delegate
                this.packageImpls,
                null,
                this.context, // userObjects
                this, // context
                null // persistenceManagerFactory
            ).refPersistenceManager();
        }

        private static final long serialVersionUID = 1935043108080407782L;
        private final DataObjectManager_1_0 objectFactory;        
        private final Map<String,String> packageImpls;        
        private final Object context;
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6682037424274032056L;

    /**
     * An empty principal set
     */
    private static final List<String> NO_PRINCIPALS = Collections.emptyList();
    
    /**
     * Contains the standard delegate
     */
    private PersistenceManager_1_0 delegate;

    private final InteractionSpec interactionSpec;
    private final Object userContext;
    protected ConcurrentMap<InteractionSpec,RefRootPackage_1> viewManager;
    private final RefObject_1Marshaller marshaller;
    private final PersistenceManagerFactory persistenceManagerFactory;
    private transient PersistenceManager_1_0 persistenceManager;
    private final List<String> principalChain;
    
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
    private static Model_1_0 model = null;

    protected final ConcurrentMap<String,Constructor<?>> userImplConstructors = new ConcurrentHashMap<String,Constructor<?>>();
    protected final ConcurrentMap<String,Set<Class<?>>> mixedInInterfaces = new ConcurrentHashMap<String,Set<Class<?>>>();
    
    /**
     * Holds the mapping of java proxy names to MOF qualified class names.
     */
    protected static final Map<String,String> classNames = new ConcurrentHashMap<String,String>();

    private static final Constructor<?> NULL_CONSTRUCTOR = newNullConstructor();
    
}

//--- End of File -----------------------------------------------------------
