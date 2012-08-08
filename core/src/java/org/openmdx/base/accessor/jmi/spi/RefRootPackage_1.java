/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RefRootPackage_1.java,v 1.42 2008/02/19 13:43:56 hburger Exp $
 * Description: RefRootPackage_1 class
 * Revision:    $Revision: 1.42 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 13:43:56 $
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

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
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
import org.openmdx.base.accessor.generic.spi.ViewObject_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefClass_1_0;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackageFactory_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_2;
import org.openmdx.base.exception.NotSupportedException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.object.spi.InstanceLifecycleNotifier;
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
import org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.spi.Model_1;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.java.Identifier;

//---------------------------------------------------------------------------
/**
 * RefRootPackage_1 class. This is at the same time the JMI root package which 
 * acts as a factory for creating application-specific packages by calling 
 * refPackage().
 */
public class RefRootPackage_1
    extends RefPackage_1
    implements RefPackageFactory_1_1, CachingMarshaller_1_0, Serializable {

    //-------------------------------------------------------------------------
    /**
     * Constructor
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        PersistenceManagerFactory persistenceManagerFactory,
        String bindingPackageSuffix
    ) {
        this(
            objectFactory,
            bindingPackageSuffix,
            false
        );
        this.persistenceManagerFactory = persistenceManagerFactory;
    }

    //-------------------------------------------------------------------------
    /**
     * Constructor. Same as this(objectFactory, null, null)
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory
    ) {
        this(
            objectFactory,
            true
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Constructor. Same as this(objectFactory, null, null)
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        boolean throwNotFoundIfNull
    ) {
        this(
            objectFactory,
            null,
            null,
            throwNotFoundIfNull
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Constructor. Same as this(objectFactory, null, null)
     */
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        String bindingPackageSuffix,
        boolean throwNotFoundIfNull
    ) {
        this(
            objectFactory,
            null,
            null,
            bindingPackageSuffix,
            throwNotFoundIfNull
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Constructor. Same as this(objectFactory, null, null)
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

    //-------------------------------------------------------------------------
    /**
     * Constructor. Same as this(objectFactory, null, null, defaultLocationSuffix, true).
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

    //-------------------------------------------------------------------------
    /**
     * Constructor. Same as this(objectFactory, packageImpls, context, true).
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
            true
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Constructor. Same as this(objectFactory, packageImpls, context, "accessor.jmi", throwNotFoundIfNull)
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

    //-------------------------------------------------------------------------
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
            true
        );
    }

    //-------------------------------------------------------------------------
    public RefRootPackage_1(
        ObjectFactory_1_0 objectFactory,
        Map<String,String>  packageImpls,
        Object context,
        String bindingPackageSuffix,
        boolean throwNotFoundIfNull,
        boolean useOpenMdx1ImplLookup
    ) {
        this(
            null, // lazy instantiation of viewManager
            null, // viewContext
            objectFactory,
            packageImpls,
            context,
            bindingPackageSuffix,
            throwNotFoundIfNull,
            useOpenMdx1ImplLookup
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param viewPackages the view package cache
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
     *        required to construct a default location for looking up the JMI
     *        interface and implementation classes which is: <qualified model name
     *        as java package name>.<bindingPackageSuffix>.<JMI interface or class name>,
     *        e.g. 'org.omg.model1.cci.StructuralFeature' or
     *        'org.omg.model1.cci.StructuralFeatureImpl'.
     *
     * @param throwNotFoundIfNull if true RefObject.get<feature>(qualifier) always throws
     *        a NOT_FOUND exception independent of the multiplicity (0..1|1..1)
     *        of the modeled reference. This flag is for backwards compatibility.
     *        If false get<feature>(qualifier) throws a NOT_FOUND if the object
     *        was not found and multiplicity is 1..1. It returns null if the
     *        object was not found and multiplicity is 0..1.
     *        
     * @param useOpenMdx1ImplLookup if true user impls are looked up in openMDX 1
     *        compatibility mode. If false user impls are dispatched by RefObject
     *        proxies to user-defined impls.
     */
    private RefRootPackage_1(
        ViewManager_1 viewManager,
        Object viewContext,
        ObjectFactory_1_0 objectFactory,
        Map<String,String>  packageImpls,
        Object context,
        String bindingPackageSuffix,
        boolean throwNotFoundIfNull,
        boolean useOpenMdx1ImplLookup
    ) {
        super(null, null);
        this.viewManager = viewManager;
        this.viewContext = viewContext;
        this.objectFactory = objectFactory;
        this.packageImpls = packageImpls;
        this.userContext = context;
        this.bindingPackageSuffix = bindingPackageSuffix == null
        ? Names.CCI1_PACKAGE_SUFFIX
            : bindingPackageSuffix;
        this.throwNotFoundIfNull = throwNotFoundIfNull;
        this.useOpenMdx1ImplLookup = useOpenMdx1ImplLookup;
        this.loadedPackages = new HashMap<String,RefPackage>();
        this.marshaller = new RefObject_1Marshaller();
        this.persistenceManager = null; // Lazy initialization
        SysLog.detail("configured package implementations", this.packageImpls);
    }
    
    //-------------------------------------------------------------------------
    String refImplPackageName(
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
        return this.marshaller.marshal(source);
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
    // RefPackage_1_0
    //-------------------------------------------------------------------------

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
        try {
            if(RefRootPackage_1.model == null) {
                RefRootPackage_1.model = new Model_1();
                if(this.objectFactory instanceof ModelHolder_1_0) {
                    ((ModelHolder_1_0)this.objectFactory).setModel(RefRootPackage_1.model);
                }
            }
            return RefRootPackage_1.model;
        } catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    public ObjectFactory_1_0 refObjectFactory(
    ) {
        return this.objectFactory;
    }

    //-------------------------------------------------------------------------
    public UnitOfWork_1_0 refUnitOfWork(
    ) {
        try {
            return this.objectFactory.getUnitOfWork();
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
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
                // Load package from standard location
                try {
                    String cciPackageNameIntf = nestedPackageName.replace(':', '.') + "." + this.bindingPackageSuffix + "." + packageName + "Package";
                    String cciPackageNameImpl = cciPackageNameIntf + "Impl";
                    Class<?> jmiPackage = Classes.getApplicationClass(loadedClassName = cciPackageNameImpl);
                    Constructor<?> packageConstructor = jmiPackage.getConstructor(
                        new Class[]{
                            RefPackage.class,
                            RefPackage.class
                        }
                    );
                    refPackage = (RefPackage)packageConstructor.newInstance(
                        new Object[]{
                            this,
                            this
                        }
                    );
                }
                catch(ClassNotFoundException e) {
                    if(!Names.JMI1_PACKAGE_SUFFIX.equals(this.bindingPackageSuffix)) {
                        throw e;
                    }
                    String jmi1PackageNameIntf = nestedPackageName.replace(':', '.') + "." + this.bindingPackageSuffix + "." + Identifier.CLASS_PROXY_NAME.toIdentifier(packageName) + "Package";
                    refPackage = (RefPackage)Proxy.newProxyInstance(
                        this.getClass().getClassLoader(), 
                        new Class[]{
                            Classes.getApplicationClass(loadedClassName = jmi1PackageNameIntf),
                            RefPackage_1_1.class
                        }, 
                        new Jmi1PackageInvocationHandler(
                            nestedPackageName + ":" + packageName,
                            this, 
                            this
                        )
                    );
                }
                this.refModel().addModels(
                    Arrays.asList(
                        new String[]{nestedPackageName}
                    )
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
        catch(IllegalAccessException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
        catch(InstantiationException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
        catch(ClassNotFoundException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
        catch(NoSuchMethodException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
        catch(InvocationTargetException e) {
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
            Connection_1Factory connectionFactory = ((ObjectFactory_1_2)this.objectFactory).getConnectionFactory();
            boolean containerManagedUnitOfWork = Boolean.TRUE.equals(
                ((ObjectFactory_1_2)this.objectFactory).hasContainerManagedUnitOfWork()
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
        } catch (Exception exception) {
            throw new JmiServiceException(
                new NotSupportedException(
                    exception,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter(
                            "objectFactory.class", 
                            objectFactory == null ? "n/a" : objectFactory.getClass().getName()
                        ),
                    },
                    "The given RefPackage is unable to create a new independent one"
                )
            );
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
                        this.viewManager.getViewConnection(viewContext),
                        this.packageImpls,
                        this.userContext,
                        this.bindingPackageSuffix,
                        this.throwNotFoundIfNull,
                        this.useOpenMdx1ImplLookup
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
                            objectFactory == null ? "n/a" : objectFactory.getClass().getName()
                        ),
                    },
                    "The given RefPackage is unable to create a view"
                )
            );
        }
    }
  
    //-------------------------------------------------------------------------
    // Implements RefPackage_1_3
    //-------------------------------------------------------------------------

    /**
     * Empty the cache
     */
    public void clear(){
        this.marshaller.clear();
        if(this.objectFactory instanceof ObjectFactory_1_3) {
            ((ObjectFactory_1_3)this.objectFactory).clear();
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
        extends CachingMarshaller {

        private static final long serialVersionUID = -6856573596590312011L;

        //-----------------------------------------------------------------------
        public RefObject_1Marshaller(
        ) {
            super();
        }

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
          if(source instanceof RefObject_1_0) {
            return ((RefObject_1_0)source).refDelegate();
          }
          else {
            return source;
          }
        }
    
        //-----------------------------------------------------------------------  
        public Object createMarshalledObject(
          Object source
        ) throws ServiceException {
            // Object_1_0
            if(source instanceof Object_1_0) {
              String className = ((Object_1_0)source).objGetClass();
              if(className != null) {
                return RefRootPackage_1.this.refClass(className).refCreateInstance(
                  Arrays.asList(new Object[]{source})
                );
              }
              else {
                throw new ServiceException(
                  StackedException.DEFAULT_DOMAIN,
                  BasicException.Code.NOT_FOUND,
                  new BasicException.Parameter[]{
                    new BasicException.Parameter("path", ((Object_1_0)source).objGetPath()),
                  },
                  "object class can not be determined"
                );
              }
            }
            else {
              return source;
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.marshalling.CachingMarshaller#clear()
         */
        protected void clear() {
            super.clear();
        }
    
    }

    //-------------------------------------------------------------------------
    // Implements RefPackage_1_1
    //-------------------------------------------------------------------------

    //-----------------------------------------------------------------------
    public Object refCreateImpl(
        String qualifiedClassName,
        RefObject_1_0 refDelegate
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
                    SysLog.info("No user impl found", Arrays.asList(new String[]{implPackageName, className}));
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
                        SysLog.info("No user impl found", Arrays.asList(new String[]{implPackageName, className}));                    
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
                        SysLog.warning("Required interfaces not found on class path", Arrays.asList(new String[]{bindingQualifiedInterfaceName, cci2QualifiedInterfaceName}));
                    }
                }
                catch(NoSuchMethodException e) {
                    SysLog.info("Missing constructor in user implementation. Trying fallback.", Arrays.asList(new String[]{bindingQualifiedInterfaceName, cci2QualifiedInterfaceName}));
                    try {
                        userImplConstructor = userClass.getConstructor(
                            new Class[]{
                                Classes.getApplicationClass(cci2QualifiedInterfaceName),
                                Classes.getApplicationClass(cci2QualifiedInterfaceName)
                            }
                        );
                    }
                    catch(NoSuchMethodException e0) {
                        SysLog.warning("Missing constructor in user implementation", Arrays.asList(new String[]{cci2QualifiedInterfaceName, cci2QualifiedInterfaceName}));
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
            if(userImplConstructor != null) {
                try {
                    return userImplConstructor.newInstance(
                        refDelegate,
                        Proxy.newProxyInstance(
                            refDelegate.getClass().getClassLoader(),
                            refDelegate.getClass().getInterfaces(),
                            new Jmi1ObjectInvocationHandler(
                                refDelegate.refDelegate(),
                                refDelegate.refClass(),
                                true
                            )
                        )
                    );
                }
                catch(Exception e) {
                    new ServiceException(e).log();
                    // Do NOT mark qualifiedClassName as having no implementation
                    return null;
                }
            }
        }
        this.classesNotHavingUserImpl.add(qualifiedClassName);
        return null;
    }
            
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 1272557331049377936L;

    private final ObjectFactory_1_0 objectFactory;
    private final Object viewContext;
    private final Object userContext;
    protected ViewManager_1 viewManager;
    private final RefObject_1Marshaller marshaller;
    private final boolean throwNotFoundIfNull;
    private PersistenceManagerFactory persistenceManagerFactory = null;
    private transient PersistenceManager persistenceManager;

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
    
}

//--- End of File -----------------------------------------------------------
